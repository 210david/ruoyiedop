-- 查看SH20260730003的基本信息
SELECT receive_id, receive_no, status, warehouse_id, order_id, order_no, supplier_id, supplier_name, qualified_qty, unqualified_qty, total_qty, inbound_order_id, del_flag FROM pms_receive WHERE receive_no='SH20260730003';

-- 查看收货明细
SELECT detail_id, receive_id, material_id, material_code, material_name, qualified_qty, unqualified_qty, order_detail_id, unit_price FROM pms_receive_detail WHERE receive_id = (SELECT receive_id FROM pms_receive WHERE receive_no='SH20260730003');

-- 查看审核日志
SELECT * FROM pms_receive_audit_log WHERE receive_id = (SELECT receive_id FROM pms_receive WHERE receive_no='SH20260730003');
