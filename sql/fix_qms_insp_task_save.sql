-- =============================================
-- 修复：质量管理 - 检验任务新增保存提示"系统繁忙，请稍后重试"
-- 根因分析（来自 sys-error.log）：
--   1. qms_insp_item 表缺少 defect_id 列 → BadSqlGrammarException (Unknown column 'defect_id')
--   2. mk_number_rule 表缺少 module/prefix_field/prefix_field_dict_type/prefix_field_enabled 列
--      → BadSqlGrammarException (selectRuleForUpdate SQL查询报错)
--   3. mk_number_rule 表中缺少 rule_code='qms_insp_task' 的编号规则数据
--      → generateNumber 抛出 ServiceException 或 task_no 为 null
--   4. QmsInspItem JAR包过旧（不含 BaseEntity 的 createTime setter）
--      → 需要重新编译 ruoyi-qms 模块（见脚本末尾说明）
-- 影响范围：检验任务新增/保存/查看/复检
-- 本脚本幂等，可重复执行
-- =============================================

-- =============================================
-- 1. 修复 qms_insp_item 表缺失的列
-- =============================================

-- 1.1 添加 defect_id 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_id` BIGINT DEFAULT NULL COMMENT ''缺陷代码库ID（关联qms_defect_code）'' AFTER `item_result`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 添加 defect_code 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_code');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_code` VARCHAR(64) DEFAULT NULL COMMENT ''不良代码（冗余，不合格时）'' AFTER `defect_id`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.3 添加 defect_name 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_name');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_name` VARCHAR(128) DEFAULT NULL COMMENT ''不良名称（冗余）'' AFTER `defect_code`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.4 添加 defect_level 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_level');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_level` VARCHAR(10) DEFAULT NULL COMMENT ''缺陷等级（1致命 2严重 3一般 4轻微）'' AFTER `defect_name`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.5 添加 create_by 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'create_by');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `qms_insp_item` ADD COLUMN `create_by` VARCHAR(64) DEFAULT '''' COMMENT ''创建者'' AFTER `defect_level`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.6 添加 create_time 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'create_time');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `qms_insp_item` ADD COLUMN `create_time` DATETIME DEFAULT NULL COMMENT ''创建时间'' AFTER `create_by`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 2. 修复 mk_number_rule 表缺失的列
-- =============================================

-- 2.1 添加 module 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'module');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产,qms=质量管理)'' AFTER `rule_code`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.2 添加 prefix_field 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field` VARCHAR(50) DEFAULT NULL COMMENT ''动态前缀关联字段名'' AFTER `connector`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.3 添加 prefix_field_dict_type 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field_dict_type');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_dict_type` VARCHAR(100) DEFAULT NULL COMMENT ''动态前缀关联字典类型'' AFTER `prefix_field`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.4 添加 prefix_field_enabled 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field_enabled');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_enabled` CHAR(1) DEFAULT ''0'' COMMENT ''是否启用动态前缀(0=否,1=是)'' AFTER `prefix_field_dict_type`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 3. 创建前缀映射子表（如不存在）
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

-- =============================================
-- 4. 插入检验任务编号规则（幂等）
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `prefix_field_enabled`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_insp_task', 'qms', '检验任务编号', 'IQC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', '0', 'admin', sysdate(), '检验任务编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'qms_insp_task');

-- =============================================
-- 5. 同步动态前缀序列号（防止编号重复）
--    当编号规则从固定前缀切换为动态前缀后，已有任务的序列号未同步到前缀映射表，
--    导致 generateNumber 生成重复编号 → Duplicate entry → "系统繁忙"
-- =============================================

-- 5.1 同步 IQC 前缀映射序列号（已有2条IQC任务: IQC-2026081201, IQC-2026081202）
SET @iqc_count = (SELECT COUNT(*) FROM qms_insp_task WHERE task_no LIKE 'IQC-%' AND del_flag = '0');
SET @iqc_prefix_id = (SELECT prefix_id FROM mk_number_rule_prefix WHERE rule_id = (SELECT rule_id FROM mk_number_rule WHERE rule_code = 'qms_insp_task') AND field_value = 'IQC');
SET @today_str = DATE_FORMAT(NOW(), '%Y%m%d');
SET @sql = IF(@iqc_prefix_id IS NOT NULL AND @iqc_count > 0,
    CONCAT('UPDATE mk_number_rule_prefix SET current_seq = ', @iqc_count, ', current_date_str = ''', @today_str, ''' WHERE prefix_id = ', @iqc_prefix_id),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.2 同步 IPQC 前缀映射序列号
SET @ipqc_count = (SELECT COUNT(*) FROM qms_insp_task WHERE task_no LIKE 'IPQC-%' AND del_flag = '0');
SET @ipqc_prefix_id = (SELECT prefix_id FROM mk_number_rule_prefix WHERE rule_id = (SELECT rule_id FROM mk_number_rule WHERE rule_code = 'qms_insp_task') AND field_value = 'IPQC');
SET @sql = IF(@ipqc_prefix_id IS NOT NULL AND @ipqc_count > 0,
    CONCAT('UPDATE mk_number_rule_prefix SET current_seq = ', @ipqc_count, ', current_date_str = ''', @today_str, ''' WHERE prefix_id = ', @ipqc_prefix_id),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.3 同步 FQC 前缀映射序列号
SET @fqc_count = (SELECT COUNT(*) FROM qms_insp_task WHERE task_no LIKE 'FQC-%' AND del_flag = '0');
SET @fqc_prefix_id = (SELECT prefix_id FROM mk_number_rule_prefix WHERE rule_id = (SELECT rule_id FROM mk_number_rule WHERE rule_code = 'qms_insp_task') AND field_value = 'FQC');
SET @sql = IF(@fqc_prefix_id IS NOT NULL AND @fqc_count > 0,
    CONCAT('UPDATE mk_number_rule_prefix SET current_seq = ', @fqc_count, ', current_date_str = ''', @today_str, ''' WHERE prefix_id = ', @fqc_prefix_id),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.4 同步 OQC 前缀映射序列号
SET @oqc_count = (SELECT COUNT(*) FROM qms_insp_task WHERE task_no LIKE 'OQC-%' AND del_flag = '0');
SET @oqc_prefix_id = (SELECT prefix_id FROM mk_number_rule_prefix WHERE rule_id = (SELECT rule_id FROM mk_number_rule WHERE rule_code = 'qms_insp_task') AND field_value = 'OQC');
SET @sql = IF(@oqc_prefix_id IS NOT NULL AND @oqc_count > 0,
    CONCAT('UPDATE mk_number_rule_prefix SET current_seq = ', @oqc_count, ', current_date_str = ''', @today_str, ''' WHERE prefix_id = ', @oqc_prefix_id),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 6. 验证
-- =============================================
SELECT '===== 修复完成，验证数据 =====' AS info;

-- 验证 qms_insp_item 表列
SELECT CONCAT('qms_insp_item 表列数: ', COUNT(*)) AS info 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item';

SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item'
ORDER BY ORDINAL_POSITION;

-- 验证 mk_number_rule 表列
SELECT CONCAT('mk_number_rule 表列数: ', COUNT(*)) AS info 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule';

-- 验证检验任务编号规则
SELECT rule_id, rule_code, module, rule_name, prefix, status 
FROM mk_number_rule 
WHERE rule_code = 'qms_insp_task';

-- 验证 mk_number_rule_prefix 表
SELECT CONCAT('mk_number_rule_prefix 表是否存在: ', IF(COUNT(*) > 0, '是', '否')) AS info 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule_prefix';

-- =============================================
-- 6. 重要说明：还需要重新编译 ruoyi-qms 模块
-- =============================================
-- 错误日志显示: "There is no setter for property named 'createTime' in 'class com.ruoyi.qms.domain.QmsInspItem'"
-- 这是因为本地 Maven 仓库中的 ruoyi-qms-3.9.2.jar 是旧版本，
-- QmsInspItem 类尚未继承 BaseEntity（或旧版本不含 createTime setter）。
--
-- 修复方法：
--   在项目根目录执行:
--     mvn install -pl ruoyi-qms -am -DskipTests
--   或全量编译:
--     mvn install -DskipTests
--   然后重启应用:
--     cd ruoyi-admin && mvn spring-boot:run
