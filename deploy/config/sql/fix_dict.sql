-- Fix marketing_order_status dictionary: remove all and re-insert correct values
DELETE FROM sys_dict_data WHERE dict_type = 'marketing_order_status';

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1,  '草稿',    '0', 'marketing_order_status', '', 'info',    'Y', '0', 'admin', now(), ''),
(2,  '待审核',  '1', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', now(), ''),
(3,  '已发货',  '2', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', now(), ''),
(4,  '已签收',  '3', 'marketing_order_status', '', 'success', 'N', '0', 'admin', now(), ''),
(5,  '已完成',  '4', 'marketing_order_status', '', 'success', 'N', '0', 'admin', now(), ''),
(6,  '已取消',  '5', 'marketing_order_status', '', 'danger',  'N', '0', 'admin', now(), ''),
(7,  '退货中',  '6', 'marketing_order_status', '', 'warning', 'N', '0', 'admin', now(), ''),
(8,  '已退货',  '7', 'marketing_order_status', '', 'danger',  'N', '0', 'admin', now(), ''),
(9,  '已确认',  '8', 'marketing_order_status', '', 'primary', 'N', '0', 'admin', now(), ''),
(10, '已驳回',  '9', 'marketing_order_status', '', 'danger',  'N', '0', 'admin', now(), '');
