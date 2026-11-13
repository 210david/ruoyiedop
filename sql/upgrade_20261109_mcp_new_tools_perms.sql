-- ============================================================
-- 增量升级脚本：MCP 网关新增 4 个模块查询工具的权限授权
-- 日期：2026-11-09
-- 主题：mcp-gateway lyjs 分支新增 query_projects / query_safety_hazards /
--       query_quality_tasks / query_bid_projects 四个工具的接口权限
-- 前置：先执行 sql/edop_mcp_service_account.sql（创建 edop-mcp 账号与
--       edop_mcp_readonly 角色并授权标准 5 工具权限点）
-- 幂等：全部 NOT EXISTS 保护，可重复执行
-- ============================================================

-- 1. 项目管理列表权限菜单（pjm 模块原本无 pjm:project:list 权限字符）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '项目列表查询', 906203, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'pjm:project:list', '#', 'admin', NOW(), 'MCP 只读服务账号权限（LYJS 分支）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:project:list');

-- 2. 服务账号角色授权 4 个新工具接口权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key = 'edop_mcp_readonly' AND r.del_flag = '0'
  AND m.perms IN ('pjm:project:list', 'safety:hazard:list', 'qms:task:list', 'bid:project:list')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

-- 3. 验证（应返回 4 行）
SELECT m.perms, r.role_key
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key = 'edop_mcp_readonly'
  AND m.perms IN ('pjm:project:list', 'safety:hazard:list', 'qms:task:list', 'bid:project:list')
ORDER BY m.perms;

-- 4. 逐用户模式业务角色授权（方案 A，2026-11-09 用户确认）
--    说明：网关 HTTP 部署走逐用户鉴权（grant_token 换发用户 JWT，@DataScope 按人裁剪），
--    服务账号权限仅覆盖 stdio/service 模式；实际使用网关查询的用户角色需持有对应权限点。
--    4.1 项目管理查询 → 管理类 4 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key IN ('common_user', 'dept_leader', 'business_manager', 'general_manager')
  AND r.del_flag = '0' AND r.status = '0'
  AND m.perms = 'pjm:project:list'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

--    4.2 投标管理查询 → 采购类 5 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key IN ('pms_director', 'pms_manager', 'pms_buyer', 'pms_receiver', 'pms_supplier_mgr')
  AND r.del_flag = '0' AND r.status = '0'
  AND m.perms = 'bid:project:list'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

--    4.3 验证（应返回 9 行：4 个管理类角色持 pjm:project:list，5 个采购类角色持 bid:project:list）
SELECT r.role_key, GROUP_CONCAT(m.perms ORDER BY m.perms) AS granted
FROM sys_role r
JOIN sys_role_menu rm ON rm.role_id = r.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key IN ('common_user', 'dept_leader', 'business_manager', 'general_manager',
                     'pms_director', 'pms_manager', 'pms_buyer', 'pms_receiver', 'pms_supplier_mgr')
  AND m.perms IN ('pjm:project:list', 'bid:project:list')
GROUP BY r.role_key
ORDER BY r.role_key;
