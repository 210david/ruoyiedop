-- ----------------------------------------------------------------------------
-- EDOP × WorkBuddy 连接器：服务账号初始化脚本
-- 脚本: edop_mcp_service_account.sql
-- 用途: 新建 MCP 网关专用只读服务账号 edop-mcp 及其角色（仅 5 个查询工具所需权限）
-- 规范: 主规范 3.4 菜单权限 / 3.5 数据权限；按 perms 动态授权，幂等可重跑
-- 注意: 密码哈希对应明文 Icx7RZ-2N9XDjQk3（已写入 mcp-gateway/config.json，勿入库、勿提交）
-- ----------------------------------------------------------------------------

-- 1. 服务账号角色（只读，全部数据权限——服务账号代表平台级查询，
--    客户交付时如需部门隔离，将 data_scope 改为 '5' 并调整部门归属）
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 'EDOP-MCP只读服务', 'edop_mcp_readonly', 99, '1', 1, 1, '0', '0', 'admin', NOW(), 'WorkBuddy 连接器专用只读服务账号角色（勿人工登录）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'edop_mcp_readonly' AND del_flag = '0');

-- 2. 服务账号（部门挂载到一级部门下第一个部门）
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT (SELECT dept_id FROM sys_dept WHERE status = '0' AND del_flag = '0' ORDER BY parent_id, order_num LIMIT 1),
       'edop-mcp', 'MCP服务账号', '00', '', '', '2', '',
       '$2a$10$bbMq9tKvDJT2Of744WRJgO/ipX7HZKgrSDzuQ0BbGXolRZQBxnBTO',
       '0', '0', 'admin', NOW(), 'WorkBuddy 连接器专用服务账号（勿人工登录、勿改密）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'edop-mcp' AND del_flag = '0');

-- 3. 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u, sys_role r
WHERE u.user_name = 'edop-mcp' AND u.del_flag = '0'
  AND r.role_key = 'edop_mcp_readonly' AND r.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id);

-- 4. 角色-菜单授权（按权限字符动态关联，覆盖 5 个工具所需 8 个权限点：
--    bi:dashboard:view / bi:metric:list / wms:inventory:list / wms:statistics:list
--    marketing:order:list / marketing:report:view / pms:order:list / pms:dashboard:view）
--    注意：RuoYi 菜单树授权需连带父级 M/C 菜单，否则 hasPermi 部分实现会校验菜单链，
--    此处同时授权按钮本身与其父级菜单，保证权限解析完整。
INSERT INTO sys_role_menu (role_id, menu_id)
WITH RECURSIVE target_menus AS (
    SELECT m.menu_id, m.parent_id
    FROM sys_menu m
    WHERE m.perms IN (
        'bi:dashboard:view', 'bi:metric:list',
        'wms:inventory:list', 'wms:statistics:list',
        'marketing:order:list', 'marketing:report:view',
        'pms:order:list', 'pms:dashboard:view'
    )
    UNION ALL
    SELECT p.menu_id, p.parent_id
    FROM sys_menu p
    JOIN target_menus t ON p.menu_id = t.parent_id
)
SELECT r.role_id, tm.menu_id
FROM sys_role r
CROSS JOIN (SELECT DISTINCT menu_id FROM target_menus) tm
WHERE r.role_key = 'edop_mcp_readonly' AND r.del_flag = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = r.role_id AND rm.menu_id = tm.menu_id
  );

-- 5. 校验查询（执行后人工核对）
SELECT '角色' AS item, role_id, role_name, role_key, data_scope FROM sys_role WHERE role_key = 'edop_mcp_readonly' AND del_flag = '0';
SELECT '账号' AS item, user_id, user_name, nick_name, status FROM sys_user WHERE user_name = 'edop-mcp' AND del_flag = '0';
SELECT '授权菜单数' AS item, COUNT(*) AS menu_count FROM sys_role_menu rm JOIN sys_role r ON r.role_id = rm.role_id WHERE r.role_key = 'edop_mcp_readonly';
