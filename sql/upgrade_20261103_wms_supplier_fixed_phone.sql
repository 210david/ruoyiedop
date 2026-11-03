-- =============================================================
-- 升级脚本：供应商管理新增"固定电话"字段
-- 日期：2026-11-03
-- 适用：已存在 wms_supplier 表的环境（幂等，可重复执行）
-- 说明：
--   1. wms_supplier 新增 fixed_phone（固定电话）字段， VARCHAR(20)，可空
--   2. 联系电话、固定电话均不强制必填（应用层已调整，无需数据变更）
-- =============================================================

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wms_supplier'
      AND COLUMN_NAME = 'fixed_phone'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `wms_supplier` ADD COLUMN `fixed_phone` VARCHAR(20) DEFAULT NULL COMMENT ''固定电话'' AFTER `contact_phone`',
    'SELECT ''fixed_phone 字段已存在，跳过'' AS notice'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
