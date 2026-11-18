-- =============================================
-- 业务全景图 - 菜单权限 SQL
-- 变更内容：新增「业务全景图」一级菜单（C 类型）
-- 页面文件：ruoyi-ui/src/views/business-map/index.vue
-- 说明：可重复执行，已存在则跳过插入
-- =============================================

-- 1. 新增业务全景图菜单（一级菜单，C 类型）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon,
                      create_by, create_time, remark)
SELECT '业务全景图', 0, 1, '/business-map', 'business-map/index', '', 'BusinessMap',
       1, 0, 'C', '0', '0', 'business:map:view', 'guide',
       'admin', sysdate(), '新手引导：业务模块全景与数据流向'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE path = '/business-map' AND menu_type = 'C'
);

-- 2. 取得菜单 ID
SET @mapMenuId = (SELECT menu_id FROM sys_menu WHERE path = '/business-map' AND menu_type = 'C' LIMIT 1);

-- 3. 授权给现有全部有效角色（可按实际需要删减）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, @mapMenuId
FROM sys_role r
WHERE r.del_flag = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = @mapMenuId
  );

-- 4. 校验
SELECT m.menu_id, m.menu_name, m.path, m.component, m.perms,
       (SELECT COUNT(1) FROM sys_role_menu rm WHERE rm.menu_id = m.menu_id) AS granted_roles
FROM sys_menu m
WHERE m.path = '/business-map';
