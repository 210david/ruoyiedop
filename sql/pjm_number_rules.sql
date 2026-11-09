-- ============================================================
-- PJM 项目管理模块编号规则初始化（幂等：已存在则跳过，可重复执行）
-- 规则编码与生成器调用约定一致：
--   PjmProjectServiceImpl      -> PJM_PROJECT
--   PjmChangeServiceImpl       -> PJM_CHANGE
--   PjmPaymentServiceImpl      -> PJM_PAYMENT
--   PjmTaskWorklogServiceImpl  -> PJM_WORKLOG
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用）
-- ============================================================

-- 回滚语句（如需移除本批规则时执行）：
-- DELETE FROM mk_number_rule WHERE rule_code IN ('PJM_PROJECT','PJM_CHANGE','PJM_PAYMENT','PJM_WORKLOG');

-- 项目编号（每日重置，如 XM202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_PROJECT', 'pjm', '项目编号', 'XM', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '项目编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_PROJECT');

-- 变更单编号（每日重置，如 BG202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_CHANGE', 'pjm', '变更单编号', 'BG', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '变更单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_CHANGE');

-- 付款单编号（每日重置，如 FK202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_PAYMENT', 'pjm', '付款单编号', 'FK', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '付款单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_PAYMENT');

-- 工作日志编号（每日重置，如 RZ202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_WORKLOG', 'pjm', '工作日志编号', 'RZ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '工作日志编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_WORKLOG');
