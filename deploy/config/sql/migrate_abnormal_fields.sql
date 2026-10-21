-- =====================================================
-- 异常管理表新增字段：产品信息 + 处理人/处理时间
-- 数据库: ry-vue
-- =====================================================

-- 1. 新增产品相关字段
ALTER TABLE `mms_abnormal` ADD COLUMN `product_code` varchar(64) DEFAULT NULL COMMENT '产品编号' AFTER `resource_name`;
ALTER TABLE `mms_abnormal` ADD COLUMN `product_name` varchar(200) DEFAULT NULL COMMENT '产品名称' AFTER `product_code`;
ALTER TABLE `mms_abnormal` ADD COLUMN `spec_model` varchar(200) DEFAULT NULL COMMENT '规格型号' AFTER `product_name`;
ALTER TABLE `mms_abnormal` ADD COLUMN `unit` varchar(20) DEFAULT NULL COMMENT '单位' AFTER `spec_model`;

-- 2. 新增处理人/处理时间字段
ALTER TABLE `mms_abnormal` ADD COLUMN `handle_by` varchar(64) DEFAULT NULL COMMENT '处理人' AFTER `response_time`;
ALTER TABLE `mms_abnormal` ADD COLUMN `handle_time` datetime DEFAULT NULL COMMENT '处理时间' AFTER `handle_by`;
