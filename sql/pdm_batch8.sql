-- =============================================================
-- PDM 第八批：设计方案停用/恢复按钮权限
-- =============================================================

-- 方案停用按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '方案停用', 11400, 9, '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:disable', '#', 'admin', NOW(), '设计方案停用按钮'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pdm:scheme:disable');

-- 方案恢复按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '方案恢复', 11400, 10, '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:enable', '#', 'admin', NOW(), '设计方案恢复按钮'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pdm:scheme:enable');
