-- 检查 SH20260731001 的状态和关联入库单
SELECT receive_id, receive_no, status, inbound_order_id, order_id FROM pms_receive WHERE receive_no='SH20260731001';

-- 检查关联的入库单状态
SELECT order_id, order_no, status, total_qty, putaway_qty FROM wms_inbound_order WHERE order_id = (SELECT inbound_order_id FROM pms_receive WHERE receive_no='SH20260731001');

-- 检查入库单明细
SELECT detail_id, order_id, material_id, plan_qty, received_qty, putaway_qty FROM wms_inbound_order_detail WHERE order_id = (SELECT inbound_order_id FROM pms_receive WHERE receive_no='SH20260731001');
