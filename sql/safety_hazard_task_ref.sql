-- =============================================
-- 安全生产管理 - 隐患表增加排查任务关联字段
-- 功能：排查任务执行反馈时，联动创建隐患记录并关联到任务
-- 说明：本脚本幂等，可重复执行
-- =============================================

-- 1. 给隐患表添加 task_id 列（关联排查任务）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_hazard' AND COLUMN_NAME = 'task_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_hazard` ADD COLUMN `task_id` BIGINT DEFAULT NULL COMMENT ''关联排查任务ID'' AFTER `source_type`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 添加索引便于按任务查询隐患
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_hazard' AND INDEX_NAME = 'idx_task_id');
SET @sql = IF(@idx_exists = 0, 'ALTER TABLE `biz_safety_hazard` ADD INDEX `idx_task_id` (`task_id`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 验证
SELECT CONCAT('biz_safety_hazard 表 task_id 列: ', IF(COUNT(*) > 0, '已添加', '不存在')) AS info
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_hazard' AND COLUMN_NAME = 'task_id';
