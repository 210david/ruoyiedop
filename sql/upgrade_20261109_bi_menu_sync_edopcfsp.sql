-- ============================================================
-- upgrade_20261109_bi_menu_sync_edopcfsp.sql
-- 决策支持(BI)菜单同步：对齐标准仓 ry-vue（前端手动调整部分）
-- 目标库：edop_cfsp（D:/EDOP/CFSP/backend 实际连接库，后端端口 81）
-- 日期：2026-11-09
--
-- 差异来源：标准仓部分菜单为前端手动调整，未生成 SQL；本分支库
--           同步自旧快照，存在以下 4 类差异（已逐条与标准库核对）：
--
-- 1) 9 个指标看板 parent 错位（挂到决策支持根节点，应为指标看板分析）：
--      9062279 销售看板 / 9062280 采购看板 / 9062281 生产看板
--      9062282 库存看板 / 9062283 质量看板 / 9062284 安全看板
--      9062285 设备看板 / 9062286 人力看板 / 9062287 投标看板
--      9062229(决策支持根) -> 9062238(指标看板分析)
-- 2) 5 条统计报表菜单 path 重复 dashboard，改为唯一路由名
--    （同 path 会引发前端路由冲突）：
--      4120  营销统计分析     -> mk-dashboard
--      4544  质量统计分析     -> qms-dashboard
--      4726  安全生产驾驶舱   -> safety-dashboard
--      5001  生产统计分析     -> mms-dashboard
--      905894 投标分析       -> bid-dashboard
-- 3) 1 条 icon 对齐：4215 经销商专项报表 chart -> documentation
-- 4) 补插标准库有而本库缺失的 2 条菜单（产品设计统计分析）：
--      11440 统计分析   parent=9062247(产品设计) comp=pdm/statistics/index
--      11447 统计导出   parent=11440 perms=pdm:statistics:export
--    角色授权：本库仅超级管理员授权产品设计（超管不依赖 sys_role_menu），
--    标准库对新菜单亦无角色绑定，故无需同步 sys_role_menu。
--
-- 幂等性：所有 UPDATE 带前置条件，INSERT 前查重，重复执行无副作用。
-- ============================================================

-- 1) 9 个看板归位到指标看板分析
UPDATE sys_menu SET parent_id = 9062238
WHERE menu_id BETWEEN 9062279 AND 9062287 AND parent_id = 9062229;

-- 2) 5 条 path 唯一化
UPDATE sys_menu SET path = 'mk-dashboard'     WHERE menu_id = 4120  AND path = 'dashboard';
UPDATE sys_menu SET path = 'qms-dashboard'    WHERE menu_id = 4544  AND path = 'dashboard';
UPDATE sys_menu SET path = 'safety-dashboard' WHERE menu_id = 4726  AND path = 'dashboard';
UPDATE sys_menu SET path = 'mms-dashboard'    WHERE menu_id = 5001  AND path = 'dashboard';
UPDATE sys_menu SET path = 'bid-dashboard'    WHERE menu_id = 905894 AND path = 'dashboard';

-- 3) icon 对齐
UPDATE sys_menu SET icon = 'documentation' WHERE menu_id = 4215 AND icon = 'chart';

-- 4) 补插缺失菜单（若不存在）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, route_name, create_by, create_time, remark)
SELECT 11440, '统计分析', 9062247, 1, 'statistics', 'pdm/statistics/index', 1, 0, 'C', '0', '0', 'pdm:statistics:view', 'chart', 'PdmStatistics', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 11440);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, route_name, create_by, create_time, remark)
SELECT 11447, '统计导出', 11440, 1, '', '', 1, 0, 'F', '0', '0', 'pdm:statistics:export', '#', '', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 11447);

-- ---------------- 验证 ----------------
-- 验证1：9 个看板应全部挂在指标看板分析(9062238)下
SELECT m.menu_id, m.menu_name, m.parent_id, p.menu_name AS parent_name, m.order_num
FROM sys_menu m LEFT JOIN sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_id BETWEEN 9062279 AND 9062287 ORDER BY m.order_num;

-- 验证2：5 条 path 应为唯一路由名
SELECT menu_id, menu_name, path FROM sys_menu WHERE menu_id IN (4120, 4544, 4726, 5001, 905894);

-- 验证3：icon
SELECT menu_id, menu_name, icon FROM sys_menu WHERE menu_id = 4215;

-- 验证4：补插菜单
SELECT menu_id, menu_name, parent_id, path, component, perms FROM sys_menu WHERE menu_id IN (11440, 11447);
