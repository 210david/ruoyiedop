-- 检查是否有与SH20260730008关联的入库单
SELECT * FROM wms_inbound_order WHERE remark LIKE '%SH20260730008%' OR supplier_id = 4 ORDER BY order_id DESC LIMIT 10;

-- 检查采购订单明细表结构
DESCRIBE pms_purchase_order_detail;

-- 检查采购订单3的明细
SELECT * FROM pms_purchase_order_detail WHERE order_id = 3;
