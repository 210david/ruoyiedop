-- 检查出库单号列是否存在
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry-vue' AND TABLE_NAME = 'mk_shipment' AND COLUMN_NAME = 'outbound_order_no';

-- 检查已确认发货的记录是否有出库单号
SELECT shipment_id, shipment_no, status, outbound_order_no FROM mk_shipment WHERE status = '1' AND del_flag = '0';
