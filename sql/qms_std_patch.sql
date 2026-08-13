-- =============================================
-- QMS 检验标准库补充脚本
-- 添加 qms_std 编号规则种子数据
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_std', 'qms', '检验标准编号', 'STD', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '检验标准库编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_std');

-- =============================================
-- 验证数据
-- =============================================
SELECT '检验标准库补充脚本执行完成' AS result;
SELECT rule_code, rule_name, module, prefix FROM mk_number_rule WHERE rule_code = 'qms_std';
