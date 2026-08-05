-- =============================================
-- 修复发货管理和退货管理的字典数据
-- 问题：marketing_shipment_status 字典未在 mk_all.sql 中定义，
--       导致发货状态显示不正确（如确认发货后显示"待审核"而非"已发货"）
-- =============================================

-- =============================================
-- 一、修复发货状态字典 marketing_shipment_status
-- =============================================

-- 1. 删除旧的字典数据（如果存在）
DELETE FROM sys_dict_data WHERE dict_type = 'marketing_shipment_status';

-- 2. 删除旧的字典类型（如果存在）
DELETE FROM sys_dict_type WHERE dict_type = 'marketing_shipment_status';

-- 3. 重新插入字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('发货状态', 'marketing_shipment_status', '0', 'admin', sysdate(), '发货管理状态');

-- 4. 重新插入字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待发货', '0', 'marketing_shipment_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已发货', '1', 'marketing_shipment_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '已签收', '2', 'marketing_shipment_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '已取消', '3', 'marketing_shipment_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');

-- =============================================
-- 二、修复退货状态字典 marketing_return_status
-- =============================================

-- 1. 删除旧的字典数据（如果存在）
DELETE FROM sys_dict_data WHERE dict_type = 'marketing_return_status';

-- 2. 删除旧的字典类型（如果存在）
DELETE FROM sys_dict_type WHERE dict_type = 'marketing_return_status';

-- 3. 重新插入字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('退货状态', 'marketing_return_status', '0', 'admin', sysdate(), '营销退货状态');

-- 4. 重新插入字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待审批', '0', 'marketing_return_status', '', 'warning', 'Y', '0', 'admin', sysdate(), ''),
(2, '已通过', '1', 'marketing_return_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '已驳回', '2', 'marketing_return_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(4, '已退款', '3', 'marketing_return_status', '', 'info', 'N', '0', 'admin', sysdate(), '');
