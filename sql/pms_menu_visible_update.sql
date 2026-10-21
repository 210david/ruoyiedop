-- =============================================
-- 更新采购管理菜单可见性（显示新开发模块）
-- =============================================

-- 显示采购计划菜单
UPDATE sys_menu SET visible = '0' WHERE menu_name = '采购计划' AND component = 'pms/plan/index';

-- 显示询比价管理菜单
UPDATE sys_menu SET visible = '0' WHERE menu_name = '询比价管理' AND component = 'pms/inquiry/index';

-- 验证结果
SELECT menu_name, path, component, visible, status
FROM sys_menu
WHERE menu_name IN ('采购计划', '询比价管理')
ORDER BY menu_name;

SELECT '菜单可见性更新完成' AS result;
