-- =============================================
-- 安全生产管理 - 补充培训课程编号规则
-- 功能：修复课程管理新增时课程编码为空的问题
-- 说明：本脚本可重复执行（幂等设计）
-- 执行后需重启后端服务使Service代码生效
-- =============================================

-- 插入培训课程编号规则（幂等）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_training_course', 'safety', '课程编号', 'KC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '培训课程编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_training_course');

-- 验证
SELECT rule_id, rule_code, module, rule_name, prefix, status FROM mk_number_rule WHERE rule_code = 'safety_training_course';
