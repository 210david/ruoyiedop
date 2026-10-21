-- 查看当前安全菜单结构
SELECT menu_id, menu_name, parent_id, order_num, path, component, menu_type, perms
FROM sys_menu
WHERE parent_id IN (
    SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0
)
OR menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0)
ORDER BY parent_id, order_num;

-- 查看一企一档下的子菜单
SELECT menu_id, menu_name, parent_id, order_num, path, component, menu_type, perms
FROM sys_menu
WHERE parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '一企一档' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0))
ORDER BY order_num;
