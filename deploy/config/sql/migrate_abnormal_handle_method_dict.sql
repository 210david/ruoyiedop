-- ============================================
-- 新增异常处理方式字典
-- 数据库：ry-vue
-- ============================================

-- 1. 新增字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('异常处理方式', 'mms_abnormal_handle_method', '0', 'admin', NOW(), '异常处理关闭时的处理方式');

-- 2. 新增字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
(1, '更换零件', '0', 'mms_abnormal_handle_method', '', 'primary', 'N', '0', 'admin', NOW(), '更换故障零部件'),
(2, '设备维修', '1', 'mms_abnormal_handle_method', '', 'primary', 'N', '0', 'admin', NOW(), '设备维修调试'),
(3, '调整参数', '2', 'mms_abnormal_handle_method', '', 'info', 'N', '0', 'admin', NOW(), '调整工艺参数'),
(4, '切换备用', '3', 'mms_abnormal_handle_method', '', 'success', 'N', '0', 'admin', NOW(), '切换备用设备/产线'),
(5, '返工处理', '4', 'mms_abnormal_handle_method', '', 'warning', 'N', '0', 'admin', NOW(), '对受影响产品返工'),
(6, '让步接收', '5', 'mms_abnormal_handle_method', '', 'warning', 'N', '0', 'admin', NOW(), '质量让步接收'),
(7, '补充物料', '6', 'mms_abnormal_handle_method', '', 'info', 'N', '0', 'admin', NOW(), '补充短缺物料'),
(8, '人员调配', '7', 'mms_abnormal_handle_method', '', 'info', 'N', '0', 'admin', NOW(), '调配人员支援'),
(9, '停线整改', '8', 'mms_abnormal_handle_method', '', 'danger', 'N', '0', 'admin', NOW(), '停线全面整改'),
(10, '其他', '9', 'mms_abnormal_handle_method', '', '', 'N', '0', 'admin', NOW(), '其他处理方式');
