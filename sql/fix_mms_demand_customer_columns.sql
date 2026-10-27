-- 修复 mms_demand 表缺少 customer_id 和 customer_no 列的问题
-- 问题原因：初始建表脚本 mms_production_control.sql 中未包含 customer_id 和 customer_no 列，
-- 但实体类 MmsDemand 和 Mapper XML 中引用了这些字段，导致查询时报 "Unknown column 'customer_id'" 错误，
-- 前端表现为"系统繁忙，请稍后重试"。

-- 检查并添加 customer_id 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_demand' AND COLUMN_NAME = 'customer_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE mms_demand ADD COLUMN customer_id BIGINT(20) DEFAULT NULL COMMENT ''客户ID'' AFTER priority', 'SELECT ''customer_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 customer_no 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_demand' AND COLUMN_NAME = 'customer_no');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE mms_demand ADD COLUMN customer_no VARCHAR(50) DEFAULT '''' COMMENT ''客户编号'' AFTER customer_id', 'SELECT ''customer_no already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
