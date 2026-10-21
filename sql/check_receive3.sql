-- 检查收货单的仓库ID
SELECT receive_id, receive_no, warehouse_id, order_id, order_no, supplier_id, supplier_name FROM pms_receive WHERE receive_id = 8;

-- 检查收货明细的物料ID
SELECT detail_id, receive_id, material_id, material_code, material_name, qualified_qty, order_detail_id FROM pms_receive_detail WHERE receive_id = 8;

-- 检查采购订单明细的已收数量
SELECT d.order_detail_id, d.material_name, d.quantity, d.received_qty, d.amount, d.received_amount
FROM pms_purchase_order_detail d
WHERE d.order_id = (SELECT order_id FROM pms_receive WHERE receive_id = 8);
