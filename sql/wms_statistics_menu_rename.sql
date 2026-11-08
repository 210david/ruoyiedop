-- =============================================
-- 菜单更名：仓储管理 / 统计报表 → 仓储统计分析
-- 背景：wms/statistics/index 同一页面存在双入口
--   1) 仓储管理(2000) 下模块内入口（route_name=LegacyWmsStatistics，menu_id 动态生成）
--   2) 决策支持 > 统计分析 > 仓储管理 下入口（menu_id=2110，route_name=WmsStatistics）
--   两条指向同一组件，统一更名避免侧边栏同页不同名。
-- 仅改显示名称：menu_id / parent_id / path / component / route_name / perms / icon 均不变，
--   角色授权(sys_role_menu)与前端路由无需调整，后端无需重启，刷新页面即可生效。
-- 幂等：按 component + 位置定位 + menu_name 过滤，可重复执行。
-- =============================================

UPDATE sys_menu
SET menu_name   = '仓储统计分析',
    update_by   = 'admin',
    update_time = sysdate()
WHERE menu_type = 'C'
  AND component = 'wms/statistics/index'
  AND (parent_id = 2000 OR menu_id = 2110)
  AND menu_name <> '仓储统计分析';

-- 复核：应返回 2 条，menu_name 均为「仓储统计分析」
SELECT menu_id, menu_name, parent_id, menu_type, visible, component, route_name, perms
FROM sys_menu
WHERE component = 'wms/statistics/index' AND menu_type = 'C'
ORDER BY menu_id;

-- 回滚（如需）：
-- UPDATE sys_menu SET menu_name='统计报表' WHERE menu_type='C'
--   AND component='wms/statistics/index' AND (parent_id=2000 OR menu_id=2110);
