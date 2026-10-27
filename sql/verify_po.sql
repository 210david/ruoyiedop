-- 验证采购订单已收数量
SELECT detail_id, material_name, qty, received_qty FROM pms_purchase_order_detail WHERE order_id = 3;
