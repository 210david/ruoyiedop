-- 检查审核结果
SELECT receive_id, receive_no, status, inbound_order_id, audit_by FROM pms_receive WHERE receive_id = 3;

-- 检查入库单
SELECT order_id, order_no, order_type, warehouse_id, status, total_qty FROM wms_inbound_order WHERE order_id = (SELECT inbound_order_id FROM pms_receive WHERE receive_id = 3);
