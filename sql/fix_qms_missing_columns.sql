-- =============================================
-- 修复：质量管理 - 检验任务列表提示"系统繁忙，请稍后重试"
-- 根因分析（来自 sys-error.log）：
--   1. qms_insp_task 表缺少 inspect_method 列 → BadSqlGrammarException (Unknown column 'inspect_method')
--   2. qms_insp_task 表缺少 gauge_no 列（增量迁移 qms_add_gauge_no.sql 未执行）
--   3. qms_ncr 表缺少 current_node / max_node / dispose_qty / dispose_remark 列
--      （qms_all.sql 中的 ALTER TABLE 未执行）
--   4. qms_insp_item 表缺少 defect_id/defect_code/defect_name/defect_level/defect_qty/create_by/create_time 列
--      （fix_qms_insp_task_save.sql 中的 ALTER TABLE 未执行或不完整）
--   影响范围：检验任务列表、检验任务详情、检验结果录入、NCR台账列表
-- 本脚本幂等，可重复执行
-- =============================================

-- =============================================
-- 1. 修复 qms_insp_task 表缺失的列
-- =============================================

-- 1.1 添加 inspect_method 列（检验水平 S-1~S-4, I~III）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_task' AND COLUMN_NAME = 'inspect_method');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_task` ADD COLUMN `inspect_method` VARCHAR(10) DEFAULT ''II'' COMMENT ''检验水平（S-1/S-2/S-3/S-4/I/II/III）'' AFTER `inspect_level`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 添加 gauge_no 列（量检具编号冗余）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_task' AND COLUMN_NAME = 'gauge_no');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_task` ADD COLUMN `gauge_no` VARCHAR(64) DEFAULT NULL COMMENT ''量检具编号（冗余）'' AFTER `gauge_name`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.3 将现有OQC类型任务的检验水平更新为S-4
UPDATE qms_insp_task SET inspect_method = 'S-4' WHERE task_type = 'OQC' AND del_flag = '0' AND (inspect_method IS NULL OR inspect_method = 'II');

-- =============================================
-- 2. 修复 qms_ncr 表缺失的列
-- =============================================

-- 2.1 添加 current_node 列（当前审批节点）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_ncr' AND COLUMN_NAME = 'current_node');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_ncr` ADD COLUMN `current_node` INT DEFAULT 0 COMMENT ''当前审批节点（0=未提交）'' AFTER `ncr_status`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.2 添加 max_node 列（最大审批节点数）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_ncr' AND COLUMN_NAME = 'max_node');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_ncr` ADD COLUMN `max_node` INT DEFAULT 0 COMMENT ''最大审批节点数'' AFTER `current_node`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.3 添加 dispose_qty 列（处置数量）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_ncr' AND COLUMN_NAME = 'dispose_qty');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_ncr` ADD COLUMN `dispose_qty` DECIMAL(18,4) DEFAULT NULL COMMENT ''处置数量'' AFTER `close_time`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.4 添加 dispose_remark 列（处置说明）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_ncr' AND COLUMN_NAME = 'dispose_remark');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_ncr` ADD COLUMN `dispose_remark` VARCHAR(500) DEFAULT NULL COMMENT ''处置说明'' AFTER `dispose_qty`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 3. 修复 qms_insp_item 表缺失的列
-- =============================================

-- 3.1 添加 defect_id 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_id');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_id` BIGINT DEFAULT NULL COMMENT ''缺陷代码库ID（关联qms_defect_code）'' AFTER `item_result`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.2 添加 defect_code 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_code');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_code` VARCHAR(64) DEFAULT NULL COMMENT ''不良代码（冗余，不合格时）'' AFTER `defect_id`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.3 添加 defect_name 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_name');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_name` VARCHAR(128) DEFAULT NULL COMMENT ''不良名称（冗余）'' AFTER `defect_code`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.4 添加 defect_level 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_level');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_level` VARCHAR(10) DEFAULT NULL COMMENT ''缺陷等级（1致命 2严重 3一般 4轻微）'' AFTER `defect_name`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.5 添加 defect_qty 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_qty');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_qty` INT DEFAULT 0 COMMENT ''缺陷数量（该缺陷出现的次数）'' AFTER `defect_level`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.6 添加 create_by 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'create_by');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_item` ADD COLUMN `create_by` VARCHAR(64) DEFAULT '''' COMMENT ''创建者'' AFTER `defect_qty`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.7 添加 create_time 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'create_time');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_item` ADD COLUMN `create_time` DATETIME DEFAULT NULL COMMENT ''创建时间'' AFTER `create_by`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 4. 验证
-- =============================================
SELECT '===== 修复完成，验证数据 =====' AS info;

-- 验证 qms_insp_task 表列
SELECT CONCAT('qms_insp_task 表列数: ', COUNT(*)) AS info
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_task';

SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_task'
ORDER BY ORDINAL_POSITION;

-- 验证 qms_ncr 表列
SELECT CONCAT('qms_ncr 表列数: ', COUNT(*)) AS info
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_ncr';

SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_ncr'
ORDER BY ORDINAL_POSITION;
