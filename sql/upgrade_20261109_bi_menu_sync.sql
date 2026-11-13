-- ============================================================
-- upgrade_20261109_bi_menu_sync.sql
-- 决策支持(BI)菜单同步：对齐标准仓 ry-vue 手动调整结果
-- 背景：标准仓部分菜单为前端手动调整，未生成 SQL；本分支仓从
--       标准仓同步后菜单与标准仓不一致，本脚本补齐差异。
-- 差异清单（标准仓 ry-vue 为准，逐条核对于 2026-11-09）：
--   1) 5 条统计报表菜单 path 由重复的 dashboard 改为唯一路由名
--      （多个模块下同 path=dashboard 会导致前端路由冲突）：
--        4120  营销统计分析     dashboard   -> mk-dashboard
--        4544  质量统计分析     dashboard   -> qms-dashboard
--        4726  安全生产驾驶舱   dashboard   -> safety-dashboard
--        5001  生产统计分析     dashboard   -> mms-dashboard
--        905894 投标分析       dashboard   -> bid-dashboard
--   2) 1 条 icon 对齐：
--        4215  经销商专项报表   chart -> documentation
-- 保留项：本分支仓特有按钮 food:yield:*（产出率核算，9062180/9062181）
--        为分支特有功能，标准仓无此菜单，不删除、不修改。
-- 不涉及：menu_id/perms/component/route_name 均不变，sys_role_menu
--        角色关联不受影响（仅改 path/icon）。
-- 幂等性：重复执行无副作用（等值更新）。
-- ============================================================

-- 决策支持 > 统计分析 > 营销管理 > 营销统计分析
UPDATE sys_menu SET path = 'mk-dashboard' WHERE menu_id = 4120 AND path = 'dashboard';

-- 决策支持 > 统计分析 > 质量管理 > 质量统计分析
UPDATE sys_menu SET path = 'qms-dashboard' WHERE menu_id = 4544 AND path = 'dashboard';

-- 决策支持 > 统计分析 > 安全生产管理 > 安全生产驾驶舱
UPDATE sys_menu SET path = 'safety-dashboard' WHERE menu_id = 4726 AND path = 'dashboard';

-- 决策支持 > 统计分析 > 生产管控 > 生产统计分析
UPDATE sys_menu SET path = 'mms-dashboard' WHERE menu_id = 5001 AND path = 'dashboard';

-- 决策支持 > 统计分析 > 营销管理 > 投标分析
UPDATE sys_menu SET path = 'bid-dashboard' WHERE menu_id = 905894 AND path = 'dashboard';

-- 决策支持 > 统计分析 > 营销管理 > 经销商专项报表（icon 对齐）
UPDATE sys_menu SET icon = 'documentation' WHERE menu_id = 4215 AND icon = 'chart';

-- ---------------- 验证（应返回 6 行，均为标准仓目标值） ----------------
SELECT menu_id, menu_name, path, icon
FROM sys_menu
WHERE menu_id IN (4120, 4215, 4544, 4726, 5001, 905894)
ORDER BY menu_id;
