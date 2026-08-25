-- =============================================
-- 修复脚本：补充生产管控缺失的编号规则
-- 问题：工单下达时提示"编号规则'mms_dispatch'不存在"
-- 原因：mms_number_rule.sql 种子数据中缺少 mms_dispatch、
--       mms_downtime、mms_outsource、mms_schedule 的 INSERT 语句
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- 派工单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_dispatch', 'mms', '派工单编号', 'DI', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '派工单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_dispatch');

-- 停机记录编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_downtime', 'mms', '停机记录编号', 'DT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '停机记录编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_downtime');

-- 委外单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_outsource', 'mms', '委外单编号', 'OUT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '委外单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_outsource');

-- 排班计划编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_schedule', 'mms', '排班计划编号', 'SC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '排班计划编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_schedule');

-- 验证
SELECT rule_id, rule_code, module, rule_name, prefix, status FROM mk_number_rule WHERE rule_code IN ('mms_dispatch','mms_downtime','mms_outsource','mms_schedule') ORDER BY rule_code;
