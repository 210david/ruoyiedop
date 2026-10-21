-- ============================================================
-- 财务模块 · 会计科目模板数据（4 级树）
--
-- 【用途】财务模块上线初始化，提供符合《小企业会计准则》的一级至三级科目。
--
-- 【重要 · 需财务顾问评审】
--   本模板为通用版本，正式上线前须由财务顾问按企业实际业务调整：
--   - 核算行业（制造业/商贸/服务）差异
--   - 是否需要成本中心辅助核算
--   - 明细科目粒度
--   本脚本仅 INSERT，不含 UPDATE/DELETE，评审后如需调整走增量脚本。
--
-- 【科目编码规则】
--   资产 1001-1999 / 负债 2001-2999 / 所有者权益 4001-4999
--   成本 5001-5999 / 损益 6001-6999
--
-- 【幂等】全部 WHERE NOT EXISTS，可重复执行
--
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_04_seed_subject.sql
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 工具：插入科目（重复执行靠 WHERE NOT EXISTS 保证）
-- 用法：调用后 SELECT LAST_INSERT_ID() 取 ID
-- 这里为可读性，分块手写 INSERT，不使用存储过程（避免权限与清理负担）
-- ------------------------------------------------------------

-- ===== 一、资产类 =====

-- 1001 库存现金
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, is_cash_equiv, cash_flow_category, sort_order, status, create_by, create_time, remark)
SELECT '1001', '库存现金', NULL, NULL, 1, 'ASSET', 'DEBIT', '0', '1', 'OPERATING', 1, '0', 'admin', sysdate(), '现金等价物，现金流量表经营活动'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1001' AND del_flag='0');

-- 1002 银行存款
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, is_cash_equiv, cash_flow_category, sort_order, status, create_by, create_time, remark)
SELECT '1002', '银行存款', NULL, NULL, 1, 'ASSET', 'DEBIT', '0', '1', 'OPERATING', 2, '0', 'admin', sysdate(), '现金等价物'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1002' AND del_flag='0');

-- 1012 其他货币资金
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, is_cash_equiv, cash_flow_category, sort_order, status, create_by, create_time, remark)
SELECT '1012', '其他货币资金', NULL, NULL, 1, 'ASSET', 'DEBIT', '1', '1', 'OPERATING', 3, '0', 'admin', sysdate(), '支付宝/微信等第三方支付'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1012' AND del_flag='0');

SET @p1002 = (SELECT subject_id FROM fin_subject WHERE subject_code='1002' AND del_flag='0' LIMIT 1);

-- 1002.01 基本户
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, is_cash_equiv, cash_flow_category, sort_order, status, create_by, create_time, remark)
SELECT '100201', '基本存款账户', @p1002, '1002', 2, 'ASSET', 'DEBIT', '1', '1', 'OPERATING', 1, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='100201' AND del_flag='0');

-- 1122 应收账款
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, is_cash_equiv, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '1122', '应收账款', NULL, NULL, 1, 'ASSET', 'DEBIT', '0', '0', 'C', 20, '0', 'admin', sysdate(), '辅助核算-客户'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1122' AND del_flag='0');

-- 1123 预付账款
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '1123', '预付账款', NULL, NULL, 1, 'ASSET', 'DEBIT', '1', 'S', 21, '0', 'admin', sysdate(), '辅助核算-供应商'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1123' AND del_flag='0');

-- 1221 其他应收款
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '1221', '其他应收款', NULL, NULL, 1, 'ASSET', 'DEBIT', '1', 'S,D', 22, '0', 'admin', sysdate(), '备用金/押金等'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1221' AND del_flag='0');

-- 1403 原材料
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '1403', '原材料', NULL, NULL, 1, 'ASSET', 'DEBIT', '1', 'D', 30, '0', 'admin', sysdate(), '存货计价由 fin_inventory_cost 维护'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1403' AND del_flag='0');

-- 1405 库存商品
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '1405', '库存商品', NULL, NULL, 1, 'ASSET', 'DEBIT', '1', 'D', 31, '0', 'admin', sysdate(), '存货计价由 fin_inventory_cost 维护'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1405' AND del_flag='0');

-- 1601 固定资产
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '1601', '固定资产', NULL, NULL, 1, 'ASSET', 'DEBIT', '1', 'D', 40, '0', 'admin', sysdate(), 'P4 固定资产模块启用'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1601' AND del_flag='0');

-- 1602 累计折旧
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '1602', '累计折旧', NULL, NULL, 1, 'ASSET', 'CREDIT', '1', 41, '0', 'admin', sysdate(), '固定资产抵减'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1602' AND del_flag='0');

-- 1701 无形资产
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '1701', '无形资产', NULL, NULL, 1, 'ASSET', 'DEBIT', '1', 42, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1701' AND del_flag='0');

-- 1801 长期待摊费用
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '1801', '长期待摊费用', NULL, NULL, 1, 'ASSET', 'DEBIT', '1', 43, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='1801' AND del_flag='0');

-- ===== 二、负债类 =====

-- 2202 应付账款
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '2202', '应付账款', NULL, NULL, 1, 'LIABILITY', 'CREDIT', '1', 'S', 10, '0', 'admin', sysdate(), '辅助核算-供应商'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='2202' AND del_flag='0');

-- 2203 预收账款
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '2203', '预收账款', NULL, NULL, 1, 'LIABILITY', 'CREDIT', '1', 'C', 11, '0', 'admin', sysdate(), '辅助核算-客户'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='2203' AND del_flag='0');

-- 2211 应付职工薪酬
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '2211', '应付职工薪酬', NULL, NULL, 1, 'LIABILITY', 'CREDIT', '0', 'D', 20, '0', 'admin', sysdate(), '工资计提与发放'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='2211' AND del_flag='0');

SET @p2211 = (SELECT subject_id FROM fin_subject WHERE subject_code='2211' AND del_flag='0' LIMIT 1);

-- 2211.01 工资
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '221101', '工资', @p2211, '2211', 2, 'LIABILITY', 'CREDIT', '1', 1, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='221101' AND del_flag='0');

-- 2211.02 社会保险费
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '221102', '社会保险费', @p2211, '2211', 2, 'LIABILITY', 'CREDIT', '1', 2, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='221102' AND del_flag='0');

-- 2211.03 住房公积金
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '221103', '住房公积金', @p2211, '2211', 2, 'LIABILITY', 'CREDIT', '1', 3, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='221103' AND del_flag='0');

-- 2221 应交税费
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '2221', '应交税费', NULL, NULL, 1, 'LIABILITY', 'CREDIT', '0', 30, '0', 'admin', sysdate(), '总账'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='2221' AND del_flag='0');

SET @p2221 = (SELECT subject_id FROM fin_subject WHERE subject_code='2221' AND del_flag='0' LIMIT 1);

-- 2221.01 应交增值税
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '222101', '应交增值税', @p2221, '2221', 2, 'LIABILITY', 'CREDIT', '0', 1, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='222101' AND del_flag='0');

SET @p222101 = (SELECT subject_id FROM fin_subject WHERE subject_code='222101' AND del_flag='0' LIMIT 1);

-- 2221.0101 进项税额
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '22210101', '进项税额', @p222101, '222101', 3, 'LIABILITY', 'DEBIT', '1', 1, '0', 'admin', sysdate(), '借方余额'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='22210101' AND del_flag='0');

-- 2221.0102 销项税额
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '22210102', '销项税额', @p222101, '222101', 3, 'LIABILITY', 'CREDIT', '1', 2, '0', 'admin', sysdate(), '贷方余额'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='22210102' AND del_flag='0');

-- 2221.0103 已交税金
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '22210103', '已交税金', @p222101, '222101', 3, 'LIABILITY', 'DEBIT', '1', 3, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='22210103' AND del_flag='0');

-- 2221.02 未交增值税
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '222102', '未交增值税', @p2221, '2221', 2, 'LIABILITY', 'CREDIT', '1', 2, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='222102' AND del_flag='0');

-- 2241 其他应付款
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '2241', '其他应付款', NULL, NULL, 1, 'LIABILITY', 'CREDIT', '1', 'S', 40, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='2241' AND del_flag='0');

-- ===== 三、所有者权益 =====

-- 4001 实收资本
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '4001', '实收资本', NULL, NULL, 1, 'EQUITY', 'CREDIT', '1', 1, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='4001' AND del_flag='0');

-- 4103 本年利润
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '4103', '本年利润', NULL, NULL, 1, 'EQUITY', 'CREDIT', '1', 3, '0', 'admin', sysdate(), '年末结转至利润分配'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='4103' AND del_flag='0');

-- 4104 利润分配
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '4104', '利润分配', NULL, NULL, 1, 'EQUITY', 'CREDIT', '0', 4, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='4104' AND del_flag='0');

SET @p4104 = (SELECT subject_id FROM fin_subject WHERE subject_code='4104' AND del_flag='0' LIMIT 1);

-- 4104.01 未分配利润
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '410401', '未分配利润', @p4104, '4104', 2, 'EQUITY', 'CREDIT', '1', 1, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='410401' AND del_flag='0');

-- 4104.02 盈余公积
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '410402', '盈余公积', @p4104, '4104', 2, 'EQUITY', 'CREDIT', '1', 2, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='410402' AND del_flag='0');

-- ===== 四、成本类 =====
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '5001', '生产成本', NULL, NULL, 1, 'COST', 'DEBIT', '1', 'D', 1, '0', 'admin', sysdate(), '制造成本归集（P5 生产成本启用后使用）'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='5001' AND del_flag='0');

INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '5101', '制造费用', NULL, NULL, 1, 'COST', 'DEBIT', '1', 'D', 2, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='5101' AND del_flag='0');

-- ===== 五、损益类 =====

-- 6001 主营业务收入
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '6001', '主营业务收入', NULL, NULL, 1, 'INCOME', 'CREDIT', '1', 'C', 1, '0', 'admin', sysdate(), '辅助核算-客户'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='6001' AND del_flag='0');

-- 6401 主营业务成本
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '6401', '主营业务成本', NULL, NULL, 1, 'INCOME', 'DEBIT', '1', 'D', 5, '0', 'admin', sysdate(), '由 fin_cost_carry_forward 结转'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='6401' AND del_flag='0');

-- 6601 销售费用
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '6601', '销售费用', NULL, NULL, 1, 'INCOME', 'DEBIT', '1', 'D', 10, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='6601' AND del_flag='0');

-- 6602 管理费用
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, aux_types, sort_order, status, create_by, create_time, remark)
SELECT '6602', '管理费用', NULL, NULL, 1, 'INCOME', 'DEBIT', '1', 'D', 11, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='6602' AND del_flag='0');

-- 6603 财务费用
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '6603', '财务费用', NULL, NULL, 1, 'INCOME', 'DEBIT', '1', 12, '0', 'admin', sysdate(), '含手续费与利息'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='6603' AND del_flag='0');

-- 6801 所得税费用
INSERT INTO fin_subject (subject_code, subject_name, parent_id, parent_code, level, category, direction, is_leaf, sort_order, status, create_by, create_time, remark)
SELECT '6801', '所得税费用', NULL, NULL, 1, 'INCOME', 'DEBIT', '1', 20, '0', 'admin', sysdate(), '季末预提，年末汇算清缴'
WHERE NOT EXISTS (SELECT 1 FROM fin_subject WHERE subject_code='6801' AND del_flag='0');

-- ------------------------------------------------------------
-- 会计期间初始化（当前年度 12 个月 + 上一年度）
-- ------------------------------------------------------------
SET @curYear = YEAR(CURDATE());
SET @lastYear = @curYear - 1;

INSERT INTO fin_period (`year_month`, period_name, status, create_by, create_time, remark)
SELECT CONCAT(@lastYear, LPAD(m.m, 2, '0')), CONCAT(@lastYear, '年', m.m, '月'), 'OPEN', 'admin', sysdate(), '年度初始化'
FROM (SELECT 1 m UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12) m
WHERE NOT EXISTS (SELECT 1 FROM fin_period p WHERE p.`year_month` = CONCAT(@lastYear, LPAD(m.m, 2, '0')));

INSERT INTO fin_period (`year_month`, period_name, status, create_by, create_time, remark)
SELECT CONCAT(@curYear, LPAD(m.m, 2, '0')), CONCAT(@curYear, '年', m.m, '月'), 'OPEN', 'admin', sysdate(), '年度初始化'
FROM (SELECT 1 m UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12) m
WHERE NOT EXISTS (SELECT 1 FROM fin_period p WHERE p.`year_month` = CONCAT(@curYear, LPAD(m.m, 2, '0')));

-- ------------------------------------------------------------
-- 财务全局配置
-- ------------------------------------------------------------
INSERT INTO fin_fiscal_config (config_key, config_value, config_name, config_group, create_by, create_time, remark)
SELECT 'cost_method', 'WEIGHTED_AVG', '默认计价方法', 'cost', 'admin', sysdate(), 'weighted_avg=加权平均 fifo=先进先出'
WHERE NOT EXISTS (SELECT 1 FROM fin_fiscal_config WHERE config_key='cost_method');

INSERT INTO fin_fiscal_config (config_key, config_value, config_name, config_group, create_by, create_time, remark)
SELECT 'auto_voucher', 'true', '自动记账开关', 'voucher', 'admin', sysdate(), 'true=业务单据审核后自动生成凭证'
WHERE NOT EXISTS (SELECT 1 FROM fin_fiscal_config WHERE config_key='auto_voucher');

INSERT INTO fin_fiscal_config (config_key, config_value, config_name, config_group, create_by, create_time, remark)
SELECT 'default_currency', 'CNY', '默认币种', 'common', 'admin', sysdate(), 'V1 仅支持人民币'
WHERE NOT EXISTS (SELECT 1 FROM fin_fiscal_config WHERE config_key='default_currency');

-- ------------------------------------------------------------
-- 凭证规则模板（单据→凭证映射，可在界面上调整）
-- ------------------------------------------------------------
INSERT INTO fin_voucher_rule (rule_key, rule_name, source_module, source_type, trigger_status, account_code, direction, amount_expr, aux_type, summary_tpl, priority, status, create_by, create_time, remark)
SELECT 'pms_invoice_approved', '采购发票审核-应付账款', 'pms', 'invoice', '2', '2202', 'DEBIT', 'total_amount', 'SUPPLIER',
       '采购发票入账 {billNo}', 1, '0', 'admin', sysdate(), '进项发票'
WHERE NOT EXISTS (SELECT 1 FROM fin_voucher_rule WHERE rule_key='pms_invoice_approved');

INSERT INTO fin_voucher_rule (rule_key, rule_name, source_module, source_type, trigger_status, account_code, direction, amount_expr, aux_type, summary_tpl, priority, status, create_by, create_time, remark)
SELECT 'pms_invoice_material', '采购发票审核-原材料', 'pms', 'invoice', '2', '1403', 'CREDIT', 'amount_excl_tax', NULL,
       '原材料入库 {billNo}', 2, '0', 'admin', sysdate(), '不含税金额'
WHERE NOT EXISTS (SELECT 1 FROM fin_voucher_rule WHERE rule_key='pms_invoice_material');

INSERT INTO fin_voucher_rule (rule_key, rule_name, source_module, source_type, trigger_status, account_code, direction, amount_expr, aux_type, summary_tpl, priority, status, create_by, create_time, remark)
SELECT 'pms_invoice_tax', '采购发票审核-进项税', 'pms', 'invoice', '2', '22210101', 'CREDIT', 'tax_amount', NULL,
       '进项税额 {billNo}', 3, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_voucher_rule WHERE rule_key='pms_invoice_tax');

INSERT INTO fin_voucher_rule (rule_key, rule_name, source_module, source_type, trigger_status, account_code, direction, amount_expr, aux_type, summary_tpl, priority, status, create_by, create_time, remark)
SELECT 'mk_shipment_shipped', '发货-应收账款', 'mk', 'shipment', '1', '1122', 'DEBIT', 'total_amount', 'CUSTOMER',
       '销售发货确认应收 {billNo}', 1, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_voucher_rule WHERE rule_key='mk_shipment_shipped');

INSERT INTO fin_voucher_rule (rule_key, rule_name, source_module, source_type, trigger_status, account_code, direction, amount_expr, aux_type, summary_tpl, priority, status, create_by, create_time, remark)
SELECT 'mk_shipment_revenue', '发货-主营业务收入', 'mk', 'shipment', '1', '6001', 'CREDIT', 'total_amount', 'CUSTOMER',
       '销售收入确认 {billNo}', 2, '0', 'admin', sysdate(), '商贸口径，不含税拆分需扩展'
WHERE NOT EXISTS (SELECT 1 FROM fin_voucher_rule WHERE rule_key='mk_shipment_revenue');

INSERT INTO fin_voucher_rule (rule_key, rule_name, source_module, source_type, trigger_status, account_code, direction, amount_expr, aux_type, summary_tpl, priority, status, create_by, create_time, remark)
SELECT 'hr_payroll_paid', '薪酬发放', 'hr', 'payroll', '3', '2211', 'DEBIT', 'total_amount', NULL,
       '发放工资 {billNo}', 1, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_voucher_rule WHERE rule_key='hr_payroll_paid');

INSERT INTO fin_voucher_rule (rule_key, rule_name, source_module, source_type, trigger_status, account_code, direction, amount_expr, aux_type, summary_tpl, priority, status, create_by, create_time, remark)
SELECT 'hr_payroll_bank', '薪酬发放-银行', 'hr', 'payroll', '3', '1002', 'CREDIT', 'total_amount', NULL,
       '银行代发工资 {billNo}', 2, '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM fin_voucher_rule WHERE rule_key='hr_payroll_bank');

-- ------------------------------------------------------------
-- 验证 1：科目树
-- ------------------------------------------------------------
SELECT level AS 级次, COUNT(*) AS 科目数
FROM fin_subject WHERE del_flag='0'
GROUP BY level ORDER BY level;
-- 期望：1级 20 个左右，2级 4 个，3级 3 个

-- ------------------------------------------------------------
-- 验证 2：期初余额录入指引（需财务填入）
-- ------------------------------------------------------------
SELECT s.subject_code, s.subject_name, '' AS 期初余额, '' AS 期初方向
FROM fin_subject s
WHERE s.del_flag='0' AND s.status='0' AND s.is_leaf='1'
ORDER BY s.subject_code;
-- 导出后交财务填列，作为系统上线期初数

-- ------------------------------------------------------------
-- 验证 3：凭证规则
-- ------------------------------------------------------------
SELECT rule_key, rule_name, source_module, source_type, account_code, direction, amount_expr
FROM fin_voucher_rule WHERE status='0' ORDER BY source_module, priority;