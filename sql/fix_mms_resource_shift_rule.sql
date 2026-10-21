-- =============================================
-- 快速修复：补充资源管理和班次管理的编号规则
-- 功能：确保编码管理中能看到"产能资源编号"和"班次编号"条目
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- 1. 确保 module 列存在
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'module');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块'' AFTER `rule_code`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 将旧版脚本插入的 mms 规则记录的 module 更新为 'mms'
UPDATE `mk_number_rule` SET `module` = 'mms'
WHERE (`module` IS NULL OR `module` = '' OR `module` = 'mk')
AND `rule_code` LIKE 'mms_%';

-- 3. 确保 reset_type 列有值（旧版使用 reset_cycle，需迁移）
UPDATE `mk_number_rule` SET `reset_type` = '1'
WHERE `reset_type` IS NULL AND `rule_code` LIKE 'mms_%';

-- 4. 确保 date_format 列有值
UPDATE `mk_number_rule` SET `date_format` = 'yyyyMMdd'
WHERE (`date_format` IS NULL OR `date_format` = '') AND `rule_code` LIKE 'mms_%';

-- 5. 确保 seq_length 列有值
UPDATE `mk_number_rule` SET `seq_length` = 4
WHERE `seq_length` IS NULL AND `rule_code` LIKE 'mms_%';

-- 6. 确保 seq_start 列有值
UPDATE `mk_number_rule` SET `seq_start` = 1
WHERE `seq_start` IS NULL AND `rule_code` LIKE 'mms_%';

-- 7. 确保 step 列有值
UPDATE `mk_number_rule` SET `step` = 1
WHERE `step` IS NULL AND `rule_code` LIKE 'mms_%';

-- 8. 确保 current_seq 列有值
UPDATE `mk_number_rule` SET `current_seq` = 0
WHERE `current_seq` IS NULL AND `rule_code` LIKE 'mms_%';

-- 9. 确保 connector 列有值
UPDATE `mk_number_rule` SET `connector` = '-'
WHERE `connector` IS NULL AND `rule_code` LIKE 'mms_%';

-- 10. 确保 status 列有值
UPDATE `mk_number_rule` SET `status` = '0'
WHERE (`status` IS NULL OR `status` = '') AND `rule_code` LIKE 'mms_%';

-- 11. 插入产能资源编号规则（如不存在）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_resource', 'mms', '产能资源编号', 'RES', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '产能资源编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'mms_resource');

-- 12. 插入班次编号规则（如不存在）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_shift', 'mms', '班次编号', 'SFT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '班次编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'mms_shift');

-- 13. 验证结果
SELECT rule_id, rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, status
FROM mk_number_rule
WHERE module = 'mms'
ORDER BY rule_code;
