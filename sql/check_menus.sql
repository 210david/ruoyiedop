-- 查询采购管理下的所有菜单
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

SELECT '=== 采购管理二级目录 ===' AS info;
SELECT menu_name, menu_type, icon, order_num 
FROM sys_menu 
WHERE parent_id = @pmsParentId AND menu_type = 'M'
ORDER BY order_num;

SELECT '=== 采购管理三级菜单 ===' AS info;
SELECT m1.menu_name AS '二级目录', m2.menu_name AS '三级菜单', m2.icon, m2.perms
FROM sys_menu m1
JOIN sys_menu m2 ON m2.parent_id = m1.menu_id
WHERE m1.parent_id = @pmsParentId AND m2.menu_type = 'C'
ORDER BY m1.order_num, m2.order_num;

-- 查询供应商相关菜单
SELECT '=== 供应商相关菜单 ===' AS info;
SELECT menu_id, menu_name, parent_id, menu_type, icon, perms, component
FROM sys_menu 
WHERE menu_name LIKE '%供应商%' OR perms LIKE '%supplier%'
ORDER BY parent_id, order_num;
