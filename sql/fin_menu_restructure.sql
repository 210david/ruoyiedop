-- ============================================================
-- 财务模块菜单重构（路径A：4个分组 + 页签独立菜单）
-- 目标结构（对齐其他模块三级菜单规范）：
--   财务管理(905938)
--   ├─ 基础设置(906100)      往来单位 / 会计科目 / 财务配置
--   ├─ 日常核算(906101)      总账凭证 / 应收 / 应付 / 存货成本 / 进项税抵扣 / 存货跌价准备
--   ├─ 资金管理(905979, C→M) 资金账户与流水 / 银行对账（原页签独立菜单）
--   └─ 财务报表(905998, C→M) 资产负债表 / 利润表 / 现金流量表 / 往来对账 / 账龄分析（原页签独立菜单）
-- 回退：见文末回退语句
-- ============================================================

-- 1. 原「资金管理」「财务报表」C 菜单改造为 M 分组目录
UPDATE sys_menu SET menu_type='M', component=NULL, perms='', query='' WHERE menu_id IN (905979, 905998);

-- 2. 新增「基础设置」「日常核算」两个分组目录
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906100, '基础设置', 905938, 1, 'base', NULL, '', '', 1, 0, 'M', '0', '0', '', 'clipboard', 'admin', NOW(), '财务模块菜单重构：基础设置分组');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906101, '日常核算', 905938, 2, 'daily', NULL, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', NOW(), '财务模块菜单重构：日常核算分组');

-- 3. 原一级菜单归入对应分组
UPDATE sys_menu SET parent_id=906100, order_num=1 WHERE menu_id=905939;  -- 往来单位(M)及其子页
UPDATE sys_menu SET parent_id=906100, order_num=2 WHERE menu_id=905946;  -- 会计科目
UPDATE sys_menu SET parent_id=906100, order_num=3 WHERE menu_id=906007;  -- 财务配置
UPDATE sys_menu SET parent_id=906101, order_num=1 WHERE menu_id=905987;  -- 总账凭证
UPDATE sys_menu SET parent_id=906101, order_num=2 WHERE menu_id=905969;  -- 应收管理
UPDATE sys_menu SET parent_id=906101, order_num=3 WHERE menu_id=905960;  -- 应付管理
UPDATE sys_menu SET parent_id=906101, order_num=4 WHERE menu_id=905953;  -- 存货成本
UPDATE sys_menu SET parent_id=906101, order_num=5 WHERE menu_id=906012;  -- 进项税抵扣
UPDATE sys_menu SET parent_id=906101, order_num=6 WHERE menu_id=906013;  -- 存货跌价准备

-- 4. 财务报表分组(905998)：原页签独立为菜单（component 复用 fin/report/index，页面按路由 path 激活对应页签）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906110, '资产负债表', 905998, 1, 'balanceSheet',   'fin/report/index', '', '', 1, 0, 'C', '0', '0', 'fin:report:assets',   'chart', 'admin', NOW(), '原财务报表页签独立菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906111, '利润表',     905998, 2, 'incomeStatement', 'fin/report/index', '', '', 1, 0, 'C', '0', '0', 'fin:report:profit',   'chart', 'admin', NOW(), '原财务报表页签独立菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906112, '现金流量表', 905998, 3, 'cashFlow',        'fin/report/index', '', '', 1, 0, 'C', '0', '0', 'fin:report:cashflow', 'chart', 'admin', NOW(), '原财务报表页签独立菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906113, '往来对账',   905998, 4, 'reconciliation',  'fin/report/index', '', '', 1, 0, 'C', '0', '0', 'fin:report:ledger',   'chart', 'admin', NOW(), '原财务报表页签独立菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906114, '账龄分析',   905998, 5, 'aging',           'fin/report/index', '', '', 1, 0, 'C', '0', '0', 'fin:report:aging',    'chart', 'admin', NOW(), '原财务报表页签独立菜单');

-- 5. 资金管理分组(905979)：原页签独立为菜单（component 复用 fin/fund/index，页面按路由 path 激活对应页签）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906120, '资金账户与流水', 905979, 1, 'account',   'fin/fund/index', '', '', 1, 0, 'C', '0', '0', 'fin:fund:list', 'edit', 'admin', NOW(), '原资金管理页签独立菜单（默认资金账户页签）');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906121, '银行对账',       905979, 2, 'reconcile', 'fin/fund/index', '', '', 1, 0, 'C', '0', '0', 'fin:fund:list', 'edit', 'admin', NOW(), '原资金管理页签独立菜单（激活银行对账页签）');

-- ============================================================
-- 回退语句（如需撤销重构）
-- DELETE FROM sys_menu WHERE menu_id IN (906100,906101,906110,906111,906112,906113,906114,906120,906121);
-- UPDATE sys_menu SET parent_id=905938 WHERE menu_id IN (905939,905946,905953,905960,905969,905987,906007,906012,906013);
-- UPDATE sys_menu SET menu_type='C', component='fin/fund/index',   perms='fin:fund:list',   path='fund'   WHERE menu_id=905979;
-- UPDATE sys_menu SET menu_type='C', component='fin/report/index', perms='fin:report:index', path='report' WHERE menu_id=905998;
-- ============================================================
