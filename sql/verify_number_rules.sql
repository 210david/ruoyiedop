-- 补种编号规则（数据清理后缺失，本次FEFO验证所需）
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field, prefix_field_dict_type, prefix_field_enabled, status, create_by, create_time, remark)
VALUES
('wms_inbound', 'wms', '入库单号规则', 'RK', 'yyyyMMdd', '1', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('wms_outbound', 'wms', '出库单号规则', 'CK', 'yyyyMMdd', '1', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('wms_stocktake', 'wms', '盘点单号规则', 'PD', 'yyyyMMdd', '1', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('wms_move', 'wms', '移库单号规则', 'YK', 'yyyyMMdd', '1', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('wms_material', 'wms', '物料编码规则', 'MAT', NULL, '0', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('wms_supplier', 'wms', '供应商编码规则', 'SUP', NULL, '0', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('wms_warehouse', 'wms', '仓库编码规则', 'WH', NULL, '0', 3, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('order', 'mk', '销售订单号规则', 'SO', 'yyyyMMdd', '1', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('shipment', 'mk', '发货单号规则', 'SH', 'yyyyMMdd', '1', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种'),
('return', 'mk', '退货单号规则', 'RT', 'yyyyMMdd', '1', 4, 1, 1, 0, NULL, '', NULL, NULL, '0', '0', 'admin', NOW(), 'FEFO验证补种');

SELECT rule_code, rule_name, prefix FROM mk_number_rule WHERE rule_code IN ('wms_inbound','wms_outbound','shipment','order');
