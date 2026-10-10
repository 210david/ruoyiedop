-- ============================================================
-- 财务模块 · 编号规则初始化（对齐 unified-development-standards.md 2.6）
--
-- 【已获批跨模块调用】2026-10-08 项目负责人确认，财务模块允许调用
-- ruoyi-mk 的 IMkNumberRuleService.generateNumber(rule_code) 生成单据号。
-- 该实现内部已用 SELECT ... FOR UPDATE 行锁 + @Transactional，并发安全。
--
-- 【零侵入】本脚本仅向既有 mk_number_rule 表 INSERT 新记录，
--          不 UPDATE / DELETE 任何既有规则，不修改 MK 代码。
--
-- 【命名前缀风格】沿用工程既有拼音缩写惯例（如 GYS 供应商 / IN 入库 / OUT 出库）
--
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_05_code_rule.sql
-- 幂等：全部使用 WHERE NOT EXISTS，可重复执行
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一、记账凭证号
--    PV + yyyyMMdd + 5位流水，按天重置
--    示例：PV2026100800001
-- ------------------------------------------------------------
INSERT INTO mk_number_rule
    (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length,
     seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled,
     status, create_by, create_time, remark)
SELECT 'fin_voucher', 'fin', '记账凭证号', 'PV', 'yyyyMMdd', '1', 5,
       1, 1, 0, '', '', '0',
       '0', 'admin', sysdate(), '财务记账凭证编号，按天重置，由 IMkNumberRuleService 生成'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'fin_voucher');

-- ------------------------------------------------------------
-- 二、应收单号
--    AR + yyyyMMdd + 4位流水，按天重置
--    示例：AR20261008001
-- ------------------------------------------------------------
INSERT INTO mk_number_rule
    (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length,
     seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled,
     status, create_by, create_time, remark)
SELECT 'fin_ar_order', 'fin', '应收单号', 'AR', 'yyyyMMdd', '1', 4,
       1, 1, 0, '', '', '0',
       '0', 'admin', sysdate(), '财务应收单编号，按天重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'fin_ar_order');

-- ------------------------------------------------------------
-- 三、应付单号
--    AP + yyyyMMdd + 4位流水，按天重置
--    示例：AP20261008001
-- ------------------------------------------------------------
INSERT INTO mk_number_rule
    (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length,
     seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled,
     status, create_by, create_time, remark)
SELECT 'fin_ap_order', 'fin', '应付单号', 'AP', 'yyyyMMdd', '1', 4,
       1, 1, 0, '', '', '0',
       '0', 'admin', sysdate(), '财务应付单编号，按天重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'fin_ap_order');

-- ------------------------------------------------------------
-- 四、收款单号
-- ------------------------------------------------------------
INSERT INTO mk_number_rule
    (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length,
     seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled,
     status, create_by, create_time, remark)
SELECT 'fin_receipt', 'fin', '收款单号', 'SK', 'yyyyMMdd', '1', 4,
       1, 1, 0, '', '', '0',
       '0', 'admin', sysdate(), '财务收款单编号，按天重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'fin_receipt');

-- ------------------------------------------------------------
-- 五、付款单号
-- ------------------------------------------------------------
INSERT INTO mk_number_rule
    (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length,
     seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled,
     status, create_by, create_time, remark)
SELECT 'fin_payment', 'fin', '付款单号', 'FK', 'yyyyMMdd', '1', 4,
       1, 1, 0, '', '', '0',
       '0', 'admin', sysdate(), '财务付款单编号，按天重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'fin_payment');

-- ------------------------------------------------------------
-- 六、成本结转单号
-- ------------------------------------------------------------
INSERT INTO mk_number_rule
    (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length,
     seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled,
     status, create_by, create_time, remark)
SELECT 'fin_cost_cf', 'fin', '成本结转单号', 'CB', 'yyyyMMdd', '1', 4,
       1, 1, 0, '', '', '0',
       '0', 'admin', sysdate(), '财务成本结转单编号，按天重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'fin_cost_cf');

-- ------------------------------------------------------------
-- 七、报销单号（P4 可选，FM-8 费用报销）
-- ------------------------------------------------------------
INSERT INTO mk_number_rule
    (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length,
     seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled,
     status, create_by, create_time, remark)
SELECT 'fin_expense', 'fin', '报销单号', 'BX', 'yyyyMMdd', '1', 4,
       1, 1, 0, '', '', '0',
       '0', 'admin', sysdate(), '财务报销单编号，按天重置（P4 费用报销启用）'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'fin_expense');

-- ------------------------------------------------------------
-- 八、往来单位编号
-- ------------------------------------------------------------
INSERT INTO mk_number_rule
    (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length,
     seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled,
     status, create_by, create_time, remark)
SELECT 'fin_partner', 'fin', '往来单位编号', 'WL', 'yyyy', '3', 4,
       1, 1, 0, '', '', '0',
       '0', 'admin', sysdate(), '财务往来单位编号，按年重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'fin_partner');

-- ------------------------------------------------------------
-- 九、关于 module 字段的展示口径（已处理）
--
--     核查结论（2026-10-04 实测）：mk_number_rule.module 的前端下拉与
--     Excel 导出走的是 MkNumberRule 实体上的 @Excel(readConverterExp=...)
--     硬编码映射，并不对应 sys_dict_type 中的任何字典类型。
--
--     既有映射为：mk/dms/wms/pms/safety/qms/mms，原不含 fin。
--     【已获批并完成】2026-10-08 项目负责人确认允许新增 fin 映射，
--     已在 ruoyi-mk 的 MkNumberRule.module 上追加 fin=财务管理。
--
--     另一处：SysRole.roleCategory（角色分类）走 sys_role_category 字典表，
--     已由 fin_06_role_category_dict.sql 补入 dict_sort=9 的「财务管理」项。
-- ------------------------------------------------------------

-- ------------------------------------------------------------
-- 验证：确认 8 条财务规则已就绪
-- ------------------------------------------------------------
SELECT rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, status
FROM mk_number_rule
WHERE module = 'fin'
ORDER BY rule_code;
-- 期望：8 行，status 全为 0

-- ------------------------------------------------------------
-- 验证：确认未污染既有规则（执行后 fin 计数为 8，原有模块计数不变）
-- ------------------------------------------------------------
SELECT module, COUNT(*) AS rule_count
FROM mk_number_rule
GROUP BY module
ORDER BY module;