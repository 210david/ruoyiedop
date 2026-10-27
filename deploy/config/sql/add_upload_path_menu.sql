-- ============================================================
-- 上传路径配置菜单 + 权限
-- 挂载位置：系统管理 下，与「参数设置」同级
-- 幂等：重复执行不会产生重复菜单
-- ============================================================

-- 1. 菜单（系统管理下的子菜单）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '上传路径配置', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '系统管理' AND parent_id = 0 LIMIT 1) t), 11,
       'uploadPath', 'system/uploadPath/index', '', '', 1, 0, 'C', '0', '0', 'system:uploadPath:query', 'folder',
       'admin', NOW(), '文件上传根目录配置与迁移'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:uploadPath:query');

-- 2. 按钮权限：修改、迁移
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '上传路径修改', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'system:uploadPath:query' LIMIT 1) t), 1,
       '', '', '', '', 1, 0, 'F', '0', '0', 'system:uploadPath:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:uploadPath:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '上传路径迁移', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'system:uploadPath:query' LIMIT 1) t), 2,
       '', '', '', '', 1, 0, 'F', '0', '0', 'system:uploadPath:migrate', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:uploadPath:migrate');
