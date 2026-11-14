-- ============================================================
-- 供应商表新增「税率」字段（金蝶供应商同步写入 rate）
-- 日期：2026-11-14
-- 幂等：已存在 rate 列时跳过，可重复执行
-- ============================================================
SET NAMES utf8mb4;

SET @has_rate := (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'wms_supplier'
      AND column_name = 'rate'
);

SET @ddl := IF(@has_rate = 0,
    'ALTER TABLE `wms_supplier` ADD COLUMN `rate` varchar(20) DEFAULT NULL COMMENT ''税率'' AFTER `tax_number`',
    'SELECT ''wms_supplier.rate 已存在，跳过'' AS msg');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
