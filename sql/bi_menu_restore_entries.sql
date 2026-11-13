-- =============================================
-- 决策支持菜单重组增量：恢复原模块统计报表入口（两边都保留）
-- 背景：bi_menu_restructure.sql 将各模块统计报表挪入决策支持>统计分析（移动语义），
--       原模块报表目录被隐藏。本脚本在原模块位置新建指向同一页面的菜单项
--       （同 component/perms/icon，route_name 加 Legacy 前缀避免与决策支持下路由名冲突），
--       并取消隐藏相关目录，实现模块内与决策支持下双入口。
-- 幂等：先查后插/先查后更，可重复执行
-- =============================================

-- 一、新建原位置菜单项（17 个）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓储统计分析', 2000, 7, 'statistics', 'wms/statistics/index', 'LegacyWmsStatistics',
    1, 0, 'C', '0', '0', 'wms:statistics:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyWmsStatistics' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '库存流水报表', 3006, 3, 'partflow', 'dms/partstock/index', 'LegacyDmsPartFlow',
    1, 0, 'C', '0', '0', 'dms:partstock:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyDmsPartFlow' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '销售人员分析', 4006, 2, 'salesperson', 'mk/salespersonReport/index', 'LegacyMkSalesperson',
    1, 0, 'C', '0', '0', 'marketing:report:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkSalesperson' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '经销商专项报表', 4006, 3, 'dealerReport', 'mk/dealerReport/index', 'LegacyMkDealerReport',
    1, 0, 'C', '0', '0', 'marketing:dealerReport:list', 'documentation', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkDealerReport' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '学时统计', 4691, 7, 'hours', 'safety/training/hours/index', 'LegacySafetyTrainingHours',
    1, 0, 'C', '0', '0', 'safety:training:hours:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacySafetyTrainingHours' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '质量数据报表', 4813, 1, 'list', 'qms/report/index', 'LegacyQmsReportList',
    1, 0, 'C', '0', '0', 'qms:report:list', 'documentation', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyQmsReportList' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'OEE分析', 5235, 2, 'oee', 'mms/oee/index', 'LegacyMmsOee',
    1, 0, 'C', '0', '0', 'mms:oee:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMmsOee' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '生产日报', 5235, 3, 'daily', 'mms/daily/index', 'LegacyMmsDaily',
    1, 0, 'C', '0', '0', 'mms:daily:list', 'documentation', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMmsDaily' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '工单完工分析', 5235, 5, 'finishAnalysis', 'mms/finishAnalysis/index', 'LegacyMmsFinishAnalysis',
    1, 0, 'C', '0', '0', 'mms:finishAnalysis:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMmsFinishAnalysis' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '产品设计驾驶舱', 11000, 99, 'statistics', 'pdm/statistics/index', 'LegacyPdmStatistics',
    1, 0, 'C', '0', '0', 'pdm:statistics:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyPdmStatistics' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '人力资源统计报表', 905538, 8, 'statistics', 'hr/statistics/index', 'LegacyHrStatistics',
    1, 0, 'C', '0', '0', 'hr:statistics:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyHrStatistics' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '用量统计', 905788, 11, 'usage', 'ai/usage/index', 'LegacyAiUsage',
    1, 0, 'C', '0', '0', 'ai:usage:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyAiUsage' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '资产负债表', 905998, 1, 'balanceSheet', 'fin/report/index', 'LegacyFinBalanceSheet',
    1, 0, 'C', '0', '0', 'fin:report:assets', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinBalanceSheet' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '利润表', 905998, 2, 'incomeStatement', 'fin/report/index', 'LegacyFinIncomeStatement',
    1, 0, 'C', '0', '0', 'fin:report:profit', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinIncomeStatement' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '现金流量表', 905998, 3, 'cashFlow', 'fin/report/index', 'LegacyFinCashFlow',
    1, 0, 'C', '0', '0', 'fin:report:cashflow', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinCashFlow' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '往来对账', 905998, 4, 'reconciliation', 'fin/report/index', 'LegacyFinReconciliation',
    1, 0, 'C', '0', '0', 'fin:report:ledger', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinReconciliation' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '账龄分析', 905998, 5, 'aging', 'fin/report/index', 'LegacyFinAging',
    1, 0, 'C', '0', '0', 'fin:report:aging', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinAging' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '人力资源统计报表', 905538, 8, 'statistics', 'hr/statistics/index', 'LegacyHrStatistics',
    1, 0, 'C', '0', '0', 'hr:statistics:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyHrStatistics' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '用量统计', 905788, 11, 'usage', 'ai/usage/index', 'LegacyAiUsage',
    1, 0, 'C', '0', '0', 'ai:usage:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyAiUsage' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '资产负债表', 905998, 1, 'balanceSheet', 'fin/report/index', 'LegacyFinBalanceSheet',
    1, 0, 'C', '0', '0', 'fin:report:assets', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinBalanceSheet' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '利润表', 905998, 2, 'incomeStatement', 'fin/report/index', 'LegacyFinIncomeStatement',
    1, 0, 'C', '0', '0', 'fin:report:profit', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinIncomeStatement' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '现金流量表', 905998, 3, 'cashFlow', 'fin/report/index', 'LegacyFinCashFlow',
    1, 0, 'C', '0', '0', 'fin:report:cashflow', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinCashFlow' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '往来对账', 905998, 4, 'reconciliation', 'fin/report/index', 'LegacyFinReconciliation',
    1, 0, 'C', '0', '0', 'fin:report:ledger', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinReconciliation' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '账龄分析', 905998, 5, 'aging', 'fin/report/index', 'LegacyFinAging',
    1, 0, 'C', '0', '0', 'fin:report:aging', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinAging' AND menu_type='C');

-- =============================================
-- 二、取消隐藏原报表目录（visible='1' → '0'，幂等）
-- 采购>统计分析(4460)无统计报表子菜单，保持隐藏
-- =============================================
UPDATE sys_menu SET visible='0' WHERE menu_id=4006 AND visible='1';
UPDATE sys_menu SET visible='0' WHERE menu_id=4813 AND visible='1';
UPDATE sys_menu SET visible='0' WHERE menu_id=3006 AND visible='1';
UPDATE sys_menu SET visible='0' WHERE menu_id=905998 AND visible='1';

-- =============================================
-- 三、admin 角色授权（幂等）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, n.menu_id FROM sys_menu n
WHERE n.route_name LIKE 'Legacy%' AND n.menu_type='C'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id=1 AND x.menu_id=n.menu_id);

-- =============================================
-- 四、普通角色授权迁移（按 perms 匹配：角色持有决策支持下挪动菜单 → 授权对应新菜单）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, n.menu_id
FROM sys_menu n
JOIN sys_menu o ON o.perms = n.perms
  AND o.menu_id IN (2110,3108,4156,4215,4851,5215,5216,5345,11440,906148,905791,906110,906111,906112,906113,906114)
JOIN sys_role_menu rm ON rm.menu_id = o.menu_id
WHERE n.route_name LIKE 'Legacy%'
  AND n.parent_id IN (2000,3006,4006,4691,4813,5235,11000,905538,905998,906110,906111,906112,906113,906114)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = n.menu_id);
