-- =============================================
-- 订单退货明细表增加发货数量字段
-- =============================================
ALTER TABLE `mk_order_return_item` ADD COLUMN `ship_qty` decimal(14,2) DEFAULT NULL COMMENT '发货数量' AFTER `order_qty`;
