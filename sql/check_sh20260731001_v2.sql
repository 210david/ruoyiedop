-- 检查 SH20260731001 的状态和关联入库单
SELECT order_id, order_no, status, total_qty FROM wms_inbound_order WHERE order_id = 28;
