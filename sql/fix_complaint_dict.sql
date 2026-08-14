-- =====================================================
-- 客诉台账字典修复脚本
-- 1. 更新客诉类型 list_class（回显样式）
-- 2. 更新客诉状态 list_class（回显样式）
-- 3. 新增严重程度字典 qms_complaint_severity
-- 4. 新增处理结果字典 qms_complaint_handle_result
-- =====================================================

-- 1. 更新客诉类型的 list_class
UPDATE sys_dict_data SET list_class = 'primary' WHERE dict_type = 'qms_complaint_type' AND dict_value = 'quality';
UPDATE sys_dict_data SET list_class = 'warning' WHERE dict_type = 'qms_complaint_type' AND dict_value = 'delivery';
UPDATE sys_dict_data SET list_class = 'info' WHERE dict_type = 'qms_complaint_type' AND dict_value = 'service';

-- 2. 更新客诉状态的 list_class
UPDATE sys_dict_data SET list_class = 'info' WHERE dict_type = 'qms_complaint_status' AND dict_value = '0';
UPDATE sys_dict_data SET list_class = 'warning' WHERE dict_type = 'qms_complaint_status' AND dict_value = '1';
UPDATE sys_dict_data SET list_class = 'primary' WHERE dict_type = 'qms_complaint_status' AND dict_value = '2';
UPDATE sys_dict_data SET list_class = 'success' WHERE dict_type = 'qms_complaint_status' AND dict_value = '3';

-- 3. 新增严重程度字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('客诉严重程度', 'qms_complaint_severity', '0', 'admin', sysdate(), '客诉严重程度');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '严重', '1', 'qms_complaint_severity', 'danger', 'N', '0', 'admin', sysdate()),
(2, '一般', '2', 'qms_complaint_severity', 'warning', 'Y', '0', 'admin', sysdate()),
(3, '轻微', '3', 'qms_complaint_severity', 'info', 'N', '0', 'admin', sysdate());

-- 4. 新增处理结果字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('客诉处理结果', 'qms_complaint_handle_result', '0', 'admin', sysdate(), '客诉处理结果');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '退货', '1', 'qms_complaint_handle_result', 'danger', 'N', '0', 'admin', sysdate()),
(2, '换货', '2', 'qms_complaint_handle_result', 'warning', 'N', '0', 'admin', sysdate()),
(3, '赔偿', '3', 'qms_complaint_handle_result', 'primary', 'N', '0', 'admin', sysdate()),
(4, '让步接收', '4', 'qms_complaint_handle_result', 'info', 'N', '0', 'admin', sysdate()),
(5, '其他', '5', 'qms_complaint_handle_result', 'success', 'N', '0', 'admin', sysdate());
