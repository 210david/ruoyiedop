-- ============================================================
-- 升级脚本：编号规则补充（幂等）
-- 日期：2026-11-03
-- 主题：qms_supplier_rectify 供应商整改单号 / hr_payroll_batch 工资批次编号
-- 说明：safety 模块三条编号规则（biz_safety_rule / biz_safety_rule_check /
--       biz_safety_rule_learn_task）见 add_upgrade_safety_rule_20261103.sql 第 8~10 节
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 < 本文件
-- ============================================================

-- 1) 供应商整改单号：ZG-年份-流水（幂等）
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, connector, status, auto_enabled, create_by, create_time, remark)
SELECT 'qms_supplier_rectify', 'qms', '供应商整改单号', 'ZG', 'yyyy', '1', 4, 1, 1, '-', '0', '1', 'admin', NOW(), '供应商整改单号：ZG-年份-流水' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'qms_supplier_rectify');

-- 2) 工资批次编号：GZPC-年月-流水，按月重置（幂等）
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, connector, status, auto_enabled, create_by, create_time, remark)
SELECT 'hr_payroll_batch', 'hr', '工资批次编号', 'GZPC', 'yyyyMM', '1', 3, 1, 1, '-', '0', '1', 'admin', NOW(), '工资批次编号：GZPC-年月-流水' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'hr_payroll_batch');

-- 验证
SELECT rule_code, prefix, date_format, reset_type, seq_length, connector FROM mk_number_rule WHERE rule_code IN ('qms_supplier_rectify', 'hr_payroll_batch');
