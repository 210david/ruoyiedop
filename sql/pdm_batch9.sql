-- =============================================================
-- PDM 第九批：产品档案停产/复产按钮权限
-- =============================================================

-- 产品停产按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '产品停产', 11100, 9, '', '', 1, 0, 'F', '0', '0', 'pdm:product:discontinue', '#', 'admin', NOW(), '产品档案停产按钮'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pdm:product:discontinue');

-- 产品复产按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '产品复产', 11100, 10, '', '', 1, 0, 'F', '0', '0', 'pdm:product:resume', '#', 'admin', NOW(), '产品档案复产按钮'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pdm:product:resume');
