-- ============================================================
-- 采购订单表增加关联合同字段
-- ============================================================

ALTER TABLE pms_purchase_order ADD COLUMN contract_id BIGINT DEFAULT NULL COMMENT '关联合同ID' AFTER request_no;
ALTER TABLE pms_purchase_order ADD COLUMN contract_no VARCHAR(64) DEFAULT NULL COMMENT '关联合同编号（冗余）' AFTER contract_id;

-- ============================================================
-- 确保付款方式、结算方式字典数据存在
-- ============================================================

-- 字典类型（如果不存在则插入）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '付款方式', 'wms_payment_method', '0', 'admin', sysdate(), '付款方式字典'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'wms_payment_method');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '结算方式', 'wms_settlement_type', '0', 'admin', sysdate(), '结算方式字典'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'wms_settlement_type');

-- 付款方式字典数据（如果不存在则插入）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '现结', '0', 'wms_payment_method', '', 'success', 'Y', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_payment_method' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '月结', '1', 'wms_payment_method', '', 'primary', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_payment_method' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '预付', '2', 'wms_payment_method', '', 'warning', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_payment_method' AND dict_value = '2');

-- 结算方式字典数据（如果不存在则插入）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '银行转账', '0', 'wms_settlement_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_settlement_type' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '承兑汇票', '1', 'wms_settlement_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_settlement_type' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '现金', '2', 'wms_settlement_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_settlement_type' AND dict_value = '2');
