-- HR模块路由名称修复脚本
-- 修复因Vue Router路由名称冲突导致的部分页面404问题
-- 原因：HR模块的菜单path（如shift/contract/leave）与其他模块冲突，
--       导致route_name首字母大写后名称重复（如"Shift"），Vue Router注册失败
USE `ry-vue`;

-- 修复一级菜单route_name
UPDATE sys_menu SET route_name = 'Hr' WHERE path = 'hr' AND parent_id = 0 AND menu_type = 'M';

-- 修复所有C类型子菜单route_name（加Hr前缀确保全局唯一）
UPDATE sys_menu SET route_name = CONCAT('Hr', UPPER(SUBSTRING(path, 1, 1)), SUBSTRING(path, 2)) 
WHERE component LIKE 'hr/%' AND menu_type = 'C' AND route_name IS NULL;

-- 验证
SELECT menu_id, menu_name, path, route_name, component 
FROM sys_menu 
WHERE component LIKE 'hr/%' AND menu_type = 'C' 
ORDER BY order_num;
