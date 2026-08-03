-- 检查SH20260730003审核后的状态
SELECT receive_id, receive_no, status, inbound_order_id, audit_by, audit_time FROM pms_receive WHERE receive_id = 3;

-- 检查入库单
SELECT order_id, order_no, order_type, supplier_id, warehouse_id, status, total_qty, remark FROM wms_inbound_order WHERE order_id = (SELECT inbound_order_id FROM pms_receive WHERE receive_id = 3);

-- 检查审核日志
SELECT * FROM pms_receive_audit_log WHERE receive_id = 3 ORDER BY log_id DESC;
