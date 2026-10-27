-- 修复工单状态字典数据
-- 先修复现有条目的label和list_class
UPDATE sys_dict_data SET dict_label = '草稿', list_class = 'info', dict_sort = 1 WHERE dict_type = 'mms_workorder_status' AND dict_value = '0';
UPDATE sys_dict_data SET dict_label = '已下达', list_class = 'primary', dict_sort = 2 WHERE dict_type = 'mms_workorder_status' AND dict_value = '1';
UPDATE sys_dict_data SET dict_label = '执行中', list_class = 'success', dict_sort = 3 WHERE dict_type = 'mms_workorder_status' AND dict_value = '2';

-- 插入新的字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
(4, '已完工', '3', 'mms_workorder_status', '', 'success', 'N', '0', 'admin', sysdate(), '已完工'),
(5, '已关闭', '4', 'mms_workorder_status', '', 'info', 'N', '0', 'admin', sysdate(), '已关闭'),
(6, '已暂停', '5', 'mms_workorder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '已暂停'),
(7, '已作废', '6', 'mms_workorder_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已作废');

-- 验证
SELECT dict_value, dict_label, list_class, dict_sort FROM sys_dict_data WHERE dict_type = 'mms_workorder_status' ORDER BY CAST(dict_value AS UNSIGNED);
