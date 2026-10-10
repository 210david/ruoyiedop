-- ============================================================
-- 财务管理模块 · 菜单与按钮权限
-- 工程：RuoYi-Vue3
--
-- 【对齐规范】unified-development-standards.md 3.4 菜单权限规范
--   三级模型：M 目录 / C 菜单（页面）/ F 按钮
--   perms 命名：{模块}:{业务}:{动作}
--   路由由后端 sys_menu 表驱动，route_name 必须全局唯一
--
-- 【零侵入】全部为 INSERT 新记录，不 UPDATE/DELETE 任何既有菜单。
--           不使用 pms_menu_new_structure.sql 中的
--           DELETE FROM sys_role_menu WHERE menu_id IN (...) 写法。
-- 【幂等】WHERE NOT EXISTS，可重复执行
--
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_02_menu.sql
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一级目录：财务管理
-- order_num 取 12（介于质量管理 11 与 人力资源 15 之间）
-- icon 用 money（工程图标库既有取值，营销管理亦用 money，此处为不同目录可接受）
-- ------------------------------------------------------------
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '财务管理', 0, 12, 'fin', NULL, '', '', 1, 0,
       'M', '0', '0', '', 'money', 'admin', sysdate(), '财务管理目录'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_name = '财务管理' AND parent_id = 0
);
SET @finDirId = (SELECT menu_id FROM sys_menu WHERE menu_name = '财务管理' AND parent_id = 0 LIMIT 1);

-- ------------------------------------------------------------
-- 二级目录 + 页面（C）
-- ------------------------------------------------------------

-- 2.1 往来单位
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '往来单位', @finDirId, 1, 'partner', NULL, '', '', 1, 0,
       'M', '0', '0', '', 'peoples', 'admin', sysdate(), '往来单位目录'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_name = '往来单位' AND parent_id = @finDirId
);
SET @finPartnerDirId = (SELECT menu_id FROM sys_menu WHERE menu_name = '往来单位' AND parent_id = @finDirId LIMIT 1);

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '往来单位管理', @finPartnerDirId, 1, 'info', 'fin/partner/index', '', 'FinPartner', 1, 0,
       'C', '0', '0', 'fin:partner:list', 'peoples', 'admin', sysdate(), '往来单位管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinPartner'
);
SET @finPartnerId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinPartner' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '往来查询', @finPartnerId, 1, '', 'F', '0', '0', 'fin:partner:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:partner:query' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '往来新增', @finPartnerId, 2, '', 'F', '0', '0', 'fin:partner:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:partner:add' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '往来修改', @finPartnerId, 3, '', 'F', '0', '0', 'fin:partner:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:partner:edit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '往来删除', @finPartnerId, 4, '', 'F', '0', '0', 'fin:partner:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:partner:remove' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '往来导出', @finPartnerId, 5, '', 'F', '0', '0', 'fin:partner:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:partner:export' AND menu_type = 'F');

-- 2.2 会计科目（FM-6 基础，无独立二级目录，直接挂财务管理下）
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '会计科目', @finDirId, 2, 'subject', 'fin/subject/index', '', 'FinSubject', 1, 0,
       'C', '0', '0', 'fin:subject:list', 'tree-table', 'admin', sysdate(), '会计科目树维护页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinSubject'
);
SET @finSubjectId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinSubject' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '科目查询', @finSubjectId, 1, '', 'F', '0', '0', 'fin:subject:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:subject:query' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '科目新增', @finSubjectId, 2, '', 'F', '0', '0', 'fin:subject:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:subject:add' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '科目修改', @finSubjectId, 3, '', 'F', '0', '0', 'fin:subject:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:subject:edit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '科目删除', @finSubjectId, 4, '', 'F', '0', '0', 'fin:subject:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:subject:remove' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '科目导入', @finSubjectId, 5, '', 'F', '0', '0', 'fin:subject:import', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:subject:import' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '科目导出', @finSubjectId, 6, '', 'F', '0', '0', 'fin:subject:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:subject:export' AND menu_type = 'F');

-- 2.3 存货成本（FM-5 地基）
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '存货成本', @finDirId, 3, 'cost', 'fin/cost/index', '', 'FinCost', 1, 0,
       'C', '0', '0', 'fin:cost:list', 'redis-list', 'admin', sysdate(), '存货成本台账与结转'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinCost'
);
SET @finCostId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinCost' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '成本查询', @finCostId, 1, '', 'F', '0', '0', 'fin:cost:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:cost:query' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '成本结转', @finCostId, 2, '', 'F', '0', '0', 'fin:cost:carry', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:cost:carry' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '计价方法配置', @finCostId, 3, '', 'F', '0', '0', 'fin:cost:method', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:cost:method' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '物料税率维护', @finCostId, 4, '', 'F', '0', '0', 'fin:cost:tax', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:cost:tax' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '存货计价表', @finCostId, 5, '', 'F', '0', '0', 'fin:cost:valuation', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:cost:valuation' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '成本导出', @finCostId, 6, '', 'F', '0', '0', 'fin:cost:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:cost:export' AND menu_type = 'F');

-- 2.4 应付管理（FM-3）
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '应付管理', @finDirId, 4, 'ap', 'fin/ap/index', '', 'FinAp', 1, 0,
       'C', '0', '0', 'fin:ap:list', 'shopping', 'admin', sysdate(), '应付单与付款核销'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinAp'
);
SET @finApId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinAp' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应付查询', @finApId, 1, '', 'F', '0', '0', 'fin:ap:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ap:query' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应付新增', @finApId, 2, '', 'F', '0', '0', 'fin:ap:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ap:add' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应付修改', @finApId, 3, '', 'F', '0', '0', 'fin:ap:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ap:edit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应付删除', @finApId, 4, '', 'F', '0', '0', 'fin:ap:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ap:remove' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '付款登记', @finApId, 5, '', 'F', '0', '0', 'fin:ap:pay', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ap:pay' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '付款核销', @finApId, 6, '', 'F', '0', '0', 'fin:ap:writeoff', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ap:writeoff' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '进项税管理', @finApId, 7, '', 'F', '0', '0', 'fin:ap:inputTax', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ap:inputTax' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应付导出', @finApId, 8, '', 'F', '0', '0', 'fin:ap:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ap:export' AND menu_type = 'F');

-- 2.5 应收管理（FM-2）
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '应收管理', @finDirId, 5, 'ar', 'fin/ar/index', '', 'FinAr', 1, 0,
       'C', '0', '0', 'fin:ar:list', 'shopping', 'admin', sysdate(), '应收单与收款核销'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinAr'
);
SET @finArId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinAr' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应收查询', @finArId, 1, '', 'F', '0', '0', 'fin:ar:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:query' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应收新增', @finArId, 2, '', 'F', '0', '0', 'fin:ar:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:add' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应收修改', @finArId, 3, '', 'F', '0', '0', 'fin:ar:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:edit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应收删除', @finArId, 4, '', 'F', '0', '0', 'fin:ar:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:remove' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '销项发票', @finArId, 5, '', 'F', '0', '0', 'fin:ar:invoice', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:invoice' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '收款登记', @finArId, 6, '', 'F', '0', '0', 'fin:ar:receipt', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:receipt' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '收款核销', @finArId, 7, '', 'F', '0', '0', 'fin:ar:writeoff', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:writeoff' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '坏账计提', @finArId, 8, '', 'F', '0', '0', 'fin:ar:badDebt', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:badDebt' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '应收导出', @finArId, 9, '', 'F', '0', '0', 'fin:ar:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:ar:export' AND menu_type = 'F');

-- 2.6 资金管理（FM-4）
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '资金管理', @finDirId, 6, 'fund', 'fin/fund/index', '', 'FinFund', 1, 0,
       'C', '0', '0', 'fin:fund:list', 'edit', 'admin', sysdate(), '资金账户与流水对账'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinFund'
);
SET @finFundId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinFund' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资金查询', @finFundId, 1, '', 'F', '0', '0', 'fin:fund:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:fund:query' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '账户新增', @finFundId, 2, '', 'F', '0', '0', 'fin:fund:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:fund:add' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '账户修改', @finFundId, 3, '', 'F', '0', '0', 'fin:fund:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:fund:edit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '账户删除', @finFundId, 4, '', 'F', '0', '0', 'fin:fund:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:fund:remove' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '流水登记', @finFundId, 5, '', 'F', '0', '0', 'fin:fund:flow', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:fund:flow' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '银行对账', @finFundId, 6, '', 'F', '0', '0', 'fin:fund:reconcile', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:fund:reconcile' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资金导出', @finFundId, 7, '', 'F', '0', '0', 'fin:fund:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:fund:export' AND menu_type = 'F');

-- 2.7 总账凭证（FM-6 核心）
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '总账凭证', @finDirId, 7, 'gl', 'fin/gl/index', '', 'FinGl', 1, 0,
       'C', '0', '0', 'fin:gl:voucher', 'documentation', 'admin', sysdate(), '记账凭证与总账'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinGl'
);
SET @finGlId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinGl' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '凭证查询', @finGlId, 1, '', 'F', '0', '0', 'fin:gl:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:query' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '凭证录入', @finGlId, 2, '', 'F', '0', '0', 'fin:gl:entry:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:entry:add' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '凭证修改', @finGlId, 3, '', 'F', '0', '0', 'fin:gl:voucher:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:voucher:edit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '凭证删除', @finGlId, 4, '', 'F', '0', '0', 'fin:gl:voucher:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:voucher:remove' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '凭证审核', @finGlId, 5, '', 'F', '0', '0', 'fin:gl:voucher:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:voucher:audit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '凭证反审核', @finGlId, 6, '', 'F', '0', '0', 'fin:gl:voucher:cancel', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:voucher:cancel' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '红字冲销', @finGlId, 7, '', 'F', '0', '0', 'fin:gl:voucher:redflush', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:voucher:redflush' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '记账规则配置', @finGlId, 8, '', 'F', '0', '0', 'fin:gl:rule', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:rule' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '期末结账', @finGlId, 9, '', 'F', '0', '0', 'fin:gl:close', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:close' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '凭证导出', @finGlId, 10, '', 'F', '0', '0', 'fin:gl:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:gl:export' AND menu_type = 'F');

-- 2.8 财务报表（FM-7）
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '财务报表', @finDirId, 8, 'report', 'fin/report/index', '', 'FinReport', 1, 0,
       'C', '0', '0', 'fin:report:index', 'chart', 'admin', sysdate(), '三大报表与账龄分析'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinReport'
);
SET @finReportId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinReport' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '科目余额表', @finReportId, 1, '', 'F', '0', '0', 'fin:report:balance', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:report:balance' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '试算平衡表', @finReportId, 2, '', 'F', '0', '0', 'fin:report:trial', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:report:trial' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资产负债表', @finReportId, 3, '', 'F', '0', '0', 'fin:report:assets', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:report:assets' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '利润表', @finReportId, 4, '', 'F', '0', '0', 'fin:report:profit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:report:profit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '现金流量表', @finReportId, 5, '', 'F', '0', '0', 'fin:report:cashflow', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:report:cashflow' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '往来对账表', @finReportId, 6, '', 'F', '0', '0', 'fin:report:ledger', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:report:ledger' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '账龄分析表', @finReportId, 7, '', 'F', '0', '0', 'fin:report:aging', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:report:aging' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '报表导出', @finReportId, 8, '', 'F', '0', '0', 'fin:report:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:report:export' AND menu_type = 'F');

-- 2.9 财务配置（全局参数）
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '财务配置', @finDirId, 9, 'config', 'fin/config/index', '', 'FinConfig', 1, 0,
       'C', '0', '0', 'fin:config:list', 'clipboard', 'admin', sysdate(), '财务全局参数与会计期间'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE route_name = 'FinConfig'
);
SET @finConfigId = (SELECT menu_id FROM sys_menu WHERE route_name = 'FinConfig' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '配置查询', @finConfigId, 1, '', 'F', '0', '0', 'fin:config:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:config:query' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '配置修改', @finConfigId, 2, '', 'F', '0', '0', 'fin:config:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:config:edit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '期间初始化', @finConfigId, 3, '', 'F', '0', '0', 'fin:config:periodInit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:config:periodInit' AND menu_type = 'F');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '期间解锁', @finConfigId, 4, '', 'F', '0', '0', 'fin:config:periodUnlock', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:config:periodUnlock' AND menu_type = 'F');

-- ------------------------------------------------------------
-- 授权给 admin 角色（RuoYi 约定：新增菜单需同步授权 admin）
-- 仅 INSERT sys_role_menu，不动其他角色
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_type IN ('M', 'C')
  AND (m.route_name IN ('FinPartner', 'FinSubject', 'FinCost', 'FinAp', 'FinAr',
                        'FinFund', 'FinGl', 'FinReport', 'FinConfig')
       OR m.menu_id = @finDirId
       OR m.menu_id = @finPartnerDirId)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_type = 'F'
  AND m.perms LIKE 'fin:%'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- ------------------------------------------------------------
-- 验证
-- ------------------------------------------------------------
-- 期望：2 个目录（M）+ 9 个页面（C）+ 74 个按钮（F）
SELECT menu_type, COUNT(*) AS cnt
FROM sys_menu
WHERE perms LIKE 'fin:%'
   OR route_name LIKE 'Fin%'
   OR menu_id = @finDirId
   OR menu_id = @finPartnerDirId
GROUP BY menu_type;

-- 财务菜单树
SELECT menu_id, menu_name, parent_id, menu_type, perms, component, route_name
FROM sys_menu
WHERE perms LIKE 'fin:%' OR route_name LIKE 'Fin%'
   OR menu_id IN (@finDirId, @finPartnerDirId)
ORDER BY parent_id, order_num;

-- 确认既有菜单未被改动（各模块页面数不变）
SELECT perms AS sample_perms, COUNT(*) AS cnt
FROM sys_menu
WHERE perms LIKE 'pms:%'
GROUP BY perms
ORDER BY perms
LIMIT 3;