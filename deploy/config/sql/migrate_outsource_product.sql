-- ============================================================
-- 外协管理表增加产品信息字段
-- 数据库: ry-vue
-- 说明: 增加产品ID、产品编码、产品名称、规格型号、单位字段
-- ============================================================

USE `ry-vue`;

-- 增加产品信息字段
ALTER TABLE `mms_outsource` ADD COLUMN `product_id` BIGINT DEFAULT NULL COMMENT '产品ID' AFTER `process_name`;
ALTER TABLE `mms_outsource` ADD COLUMN `product_code` VARCHAR(64) DEFAULT NULL COMMENT '产品编码' AFTER `product_id`;
ALTER TABLE `mms_outsource` ADD COLUMN `product_name` VARCHAR(200) DEFAULT NULL COMMENT '产品名称' AFTER `product_code`;
ALTER TABLE `mms_outsource` ADD COLUMN `spec_model` VARCHAR(200) DEFAULT NULL COMMENT '规格型号' AFTER `product_name`;
ALTER TABLE `mms_outsource` ADD COLUMN `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位' AFTER `spec_model`;
