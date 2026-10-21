-- 修复所有缺少route_name的菜单，避免路由名称冲突
-- 安全驾驶舱
UPDATE sys_menu SET route_name = 'SafetyDashboard' WHERE menu_id = 4726 AND (route_name IS NULL OR route_name = '');

-- 检查所有C类型菜单中path相同但route_name为空的
-- 电子签名
UPDATE sys_menu SET route_name = 'QmsEsig' WHERE menu_id = 4545 AND (route_name IS NULL OR route_name = '');

-- 检查车间管理
UPDATE sys_menu SET route_name = 'QmsWorkshop' WHERE menu_id = 4539 AND (route_name IS NULL OR route_name = '');
