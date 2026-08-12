-- =============================================
-- 修复：安全管理 - 证照管理点击404错误
-- 根因：Vue Router 路由名称冲突
--       sys_menu 表中有两个菜单 path 均为 'cert' 且 route_name 均为空：
--         - menu_id=4651 证照管理 (safety/arch/cert/index)  → 路由名 "Cert"
--         - menu_id=4864 证书管理 (safety/training/cert/index) → 路由名 "Cert"
--       后端 buildMenus() 的 getRouteName() 方法在 route_name 为空时回退到 path，
--       首字母大写后两者都生成 "Cert"。
--       Vue Router 4 中同名的后注册路由会覆盖先注册的，
--       导致 /safety/arch/cert 路由被 /safety/training/cert 覆盖，访问时 404。
-- 修复：为两个 cert 菜单分别设置不同的 route_name。
-- 本脚本幂等，可重复执行。
-- =============================================

-- 1. 为证照管理菜单设置唯一的 route_name
UPDATE sys_menu SET route_name = 'SafetyArchCert'
WHERE menu_id = 4651 AND (route_name IS NULL OR route_name = '' OR route_name = 'SafetyArchCert');

-- 2. 为培训证书管理菜单设置唯一的 route_name
UPDATE sys_menu SET route_name = 'SafetyTrainingCert'
WHERE menu_id = 4864 AND (route_name IS NULL OR route_name = '' OR route_name = 'SafetyTrainingCert');

-- 3. 通用检查：查找其他可能存在相同 path + 空 route_name 冲突的菜单对
--    （仅查询提示，不做修改，需人工确认后处理）
SELECT a.menu_id AS menu_id_1, a.menu_name AS name_1, a.parent_id AS parent_1,
       b.menu_id AS menu_id_2, b.menu_name AS name_2, b.parent_id AS parent_2,
       a.path AS conflict_path
FROM sys_menu a
JOIN sys_menu b ON a.path = b.path AND a.menu_id < b.menu_id
WHERE a.menu_type = 'C' AND a.status = '0'
  AND b.menu_type = 'C' AND b.status = '0'
  AND (a.route_name IS NULL OR a.route_name = '')
  AND (b.route_name IS NULL OR b.route_name = '')
ORDER BY a.path;

-- 4. 验证修复结果
SELECT menu_id, menu_name, path, component, route_name
FROM sys_menu WHERE menu_id IN (4651, 4864) ORDER BY menu_id;
