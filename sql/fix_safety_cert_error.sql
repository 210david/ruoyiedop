-- =============================================
-- 修复：安全生产管理 - 新增保存时提示"系统繁忙，请稍后重试"
-- 根因：mk_number_rule 表缺少 prefix_field/prefix_field_dict_type/prefix_field_enabled 列
--       导致 selectRuleForUpdate SQL查询报 BadSqlGrammarException
--       （Spring DataAccessException 继承 RuntimeException，被全局异常处理器拦截）
--       和/或缺少安全生产编号规则数据
-- 影响范围：安全生产管理模块全部12个调用 generateNumber 的Service均受影响
-- 本脚本幂等，可重复执行

-- =============================================
-- 1. 检查并添加 mk_number_rule 表缺失的列
-- =============================================

-- 1.1 添加 module 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'module');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产)'' AFTER `rule_code`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 添加 prefix_field 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field` VARCHAR(50) DEFAULT NULL COMMENT ''动态前缀关联字段名'' AFTER `connector`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.3 添加 prefix_field_dict_type 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field_dict_type');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_dict_type` VARCHAR(100) DEFAULT NULL COMMENT ''动态前缀关联字典类型'' AFTER `prefix_field`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.4 添加 prefix_field_enabled 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field_enabled');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_enabled` CHAR(1) DEFAULT ''0'' COMMENT ''是否启用动态前缀(0=否,1=是)'' AFTER `prefix_field_dict_type`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 2. 创建前缀映射子表（如不存在）
-- =============================================
CREATE TABLE IF NOT EXISTS `mk_number_rule_prefix` (
  `prefix_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `rule_id`       BIGINT(20)    NOT NULL                 COMMENT '规则ID',
  `field_value`   VARCHAR(50)   NOT NULL                 COMMENT '字段值（字典值）',
  `field_label`   VARCHAR(100)  DEFAULT ''               COMMENT '字段标签（字典标签）',
  `prefix`        VARCHAR(20)   DEFAULT ''               COMMENT '对应前缀',
  `enabled`       CHAR(1)       DEFAULT '1'              COMMENT '是否启用（0=否,1=是）',
  `current_seq`   BIGINT(20)    DEFAULT 0                COMMENT '当前序列号（按动态前缀重置时使用）',
  `current_date_str` VARCHAR(20) DEFAULT ''              COMMENT '当前日期串',
  `create_by`     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME                               COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME                               COMMENT '更新时间',
  PRIMARY KEY (`prefix_id`),
  KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编号规则动态前缀映射表';

-- 如果子表已存在但缺少 current_seq / current_date_str 列，也补上
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule_prefix' AND COLUMN_NAME = 'current_seq');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule_prefix` ADD COLUMN `current_seq` BIGINT(20) DEFAULT 0 COMMENT ''当前序列号'' AFTER `enabled`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule_prefix' AND COLUMN_NAME = 'current_date_str');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule_prefix` ADD COLUMN `current_date_str` VARCHAR(20) DEFAULT '' COMMENT ''当前日期串'' AFTER `current_seq`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 3. 插入或更新安全生产全部编号规则数据（幂等，使用 ON DUPLICATE KEY UPDATE）
-- =============================================

-- 3.1 区域编码
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_area', 'safety', '区域编码', 'QY', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '安全生产区域编码，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '区域编码', `prefix` = 'QY',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.2 特种设备编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_equipment', 'safety', '特种设备编号', 'SB', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '特种设备编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '特种设备编号', `prefix` = 'SB',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.3 风险点编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_risk_point', 'safety', '风险点编号', 'FX', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '风险点编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '风险点编号', `prefix` = 'FX',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.4 巡检任务编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_inspection_task', 'safety', '巡检任务编号', 'XJ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '巡检任务编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '巡检任务编号', `prefix` = 'XJ',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.5 隐患编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_hazard', 'safety', '隐患编号', 'YH', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '隐患编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '隐患编号', `prefix` = 'YH',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 5,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.6 演练编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_drill', 'safety', '演练编号', 'YL', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '应急演练编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '演练编号', `prefix` = 'YL',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.7 事故编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_incident', 'safety', '事故编号', 'SG', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '事故编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '事故编号', `prefix` = 'SG',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.8 危化品编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_material', 'safety', '危化品编号', 'WH', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '危化品编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '危化品编号', `prefix` = 'WH',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.9 应急物资编码
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_emergency_material', 'safety', '应急物资编码', 'WZ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '应急物资编码，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '应急物资编码', `prefix` = 'WZ',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.10 应急预案编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_emergency_plan', 'safety', '预案编号', 'YA', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '应急预案编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '预案编号', `prefix` = 'YA',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.11 培训计划编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_training_plan', 'safety', '培训计划编号', 'PX', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '培训计划编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '培训计划编号', `prefix` = 'PX',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.12 培训课程编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('safety_training_course', 'safety', '课程编号', 'KC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '培训课程编号，每日重置')
ON DUPLICATE KEY UPDATE
    `module` = 'safety', `rule_name` = '课程编号', `prefix` = 'KC',
    `date_format` = 'yyyyMMdd', `reset_type` = '1', `seq_length` = 4,
    `seq_start` = 1, `step` = 1, `connector` = '-',
    `prefix_field_enabled` = '0', `status` = '0';

-- 3.13 特种人员工号 —— 已移除（工号为手动输入，无需自动编码规则）
DELETE FROM `mk_number_rule` WHERE rule_code = 'safety_worker';

-- 3.14 证照编号 —— 已移除（证照编号为手动输入，无需自动编码规则）
DELETE FROM `mk_number_rule` WHERE rule_code = 'safety_cert';

-- =============================================
-- 4. 验证
-- =============================================
SELECT '===== 修复完成，验证数据 =====' AS info;
SELECT rule_id, rule_code, module, rule_name, prefix, connector, prefix_field_enabled, status FROM mk_number_rule WHERE module = 'safety' ORDER BY rule_code;
SELECT CONCAT('mk_number_rule 表列数: ', COUNT(*)) AS info FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule';
SELECT CONCAT('mk_number_rule_prefix 表是否存在: ', IF(COUNT(*) > 0, '是', '否')) AS info FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule_prefix';
