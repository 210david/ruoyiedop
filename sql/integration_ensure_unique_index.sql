-- =============================================================
-- 确保 integration_user_binding 的「一一绑定」唯一索引存在
-- 版本: v1.0  日期: 2026-11-07
--
-- 背景：uk_connector_employee (connector_type, employee_id) 约束
--       「同一连接器下一个员工只能绑定一个外部账号」。该索引在
--       integration_wecom.sql 中曾被注释为「需手工执行」，新环境
--       只跑脚本会缺失约束，导致并发/误操作可重复绑定同一员工。
--
-- 说明：employee_id 允许为 NULL（未匹配的绑定行），MySQL 唯一索引
--       允许多个 NULL，不影响未匹配数据。
-- 幂等：已存在同名索引则跳过，可重复执行。
-- =============================================================

SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'integration_user_binding'
     AND INDEX_NAME = 'uk_connector_employee'
);

SET @ddl := IF(@idx_exists = 0,
  'ALTER TABLE `integration_user_binding` ADD UNIQUE KEY `uk_connector_employee` (`connector_type`, `employee_id`)',
  'SELECT ''uk_connector_employee 已存在，跳过'' AS result');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 复核：应能看到 uk_connector_employee
SHOW INDEX FROM `integration_user_binding` WHERE Key_name = 'uk_connector_employee';
