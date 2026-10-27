-- 修复严重程度字典（中文被损坏成问号）
UPDATE sys_dict_data SET dict_label = '严重' WHERE dict_type = 'qms_complaint_severity' AND dict_value = '1';
UPDATE sys_dict_data SET dict_label = '一般' WHERE dict_type = 'qms_complaint_severity' AND dict_value = '2';
UPDATE sys_dict_data SET dict_label = '轻微' WHERE dict_type = 'qms_complaint_severity' AND dict_value = '3';

-- 修复处理结果字典（中文被损坏成问号）
UPDATE sys_dict_data SET dict_label = '退货退款' WHERE dict_type = 'qms_complaint_handle_result' AND dict_value = '1';
UPDATE sys_dict_data SET dict_label = '换货处理' WHERE dict_type = 'qms_complaint_handle_result' AND dict_value = '2';
UPDATE sys_dict_data SET dict_label = '折扣补偿' WHERE dict_type = 'qms_complaint_handle_result' AND dict_value = '3';
UPDATE sys_dict_data SET dict_label = '质量改善' WHERE dict_type = 'qms_complaint_handle_result' AND dict_value = '4';
UPDATE sys_dict_data SET dict_label = '其他处理' WHERE dict_type = 'qms_complaint_handle_result' AND dict_value = '5';
