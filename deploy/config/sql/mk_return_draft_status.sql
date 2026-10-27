-- 营销退货状态新增"草稿"状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (0, '草稿', '4', 'marketing_return_status', '', 'info', 'N', '0', 'admin', NOW(), '草稿状态');
