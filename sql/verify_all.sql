-- 1. 验证收货单状态
SELECT receive_id, receive_no, status, total_qty, qualified_qty, unqualified_qty, audit_by, audit_time, audit_opinion, inbound_order_id, del_flag FROM pms_receive WHERE receive_id = 8;

-- 2. 验证审核日志
SELECT * FROM pms_receive_audit_log WHERE receive_id = 8;

-- 3. 验证入库单
SELECT order_id, order_no, order_type, supplier_id, warehouse_id, status, total_qty, remark FROM wms_inbound_order WHERE remark LIKE '%SH20260730008%';

-- 4. 验证采购订单已收数量
SELECT d.detail_id, d.material_name, d.quantity, d.received_qty FROM pms_purchase_order_detail d WHERE d.order_id = 3;
