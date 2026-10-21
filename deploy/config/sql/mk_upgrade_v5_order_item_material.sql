-- =============================================
-- 订单明细增加物料ID字段 升级脚本
-- 版本: v5
-- 日期: 2026-08-19
-- 说明: mk_order_item 表增加 material_id 列，
--       用于关联物料主数据(wms_material)，支持需求管理选择订单时带出产品编码
-- =============================================

-- 1. 为 mk_order_item 表增加 material_id 字段（幂等执行）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'mk_order_item' AND column_name = 'material_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE mk_order_item ADD COLUMN material_id BIGINT COMMENT ''物料ID（关联wms_material）'' AFTER line_no', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 为 material_id 创建索引
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'mk_order_item' AND index_name = 'idx_order_item_material');
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_order_item_material ON mk_order_item(material_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
