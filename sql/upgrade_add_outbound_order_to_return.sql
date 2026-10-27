-- 退货单添加出库单关联字段
-- 作者: 系统升级
-- 日期: 2026-07-22
-- 说明: 退货单审核通过后自动生成出库单,需要将出库单ID回写到退货单

USE `ry-vue`;

-- 添加出库单关联字段（audit_by/audit_time/audit_opinion已存在，仅添加outbound_order_id）
ALTER TABLE `pms_return`
ADD COLUMN `outbound_order_id` BIGINT DEFAULT NULL COMMENT '出库单ID' AFTER `audit_opinion`;

-- 添加索引
ALTER TABLE `pms_return`
ADD INDEX `idx_return_outbound` (`outbound_order_id`);
