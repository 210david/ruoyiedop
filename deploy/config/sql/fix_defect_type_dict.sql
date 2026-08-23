-- 修复缺陷类型字典乱码
DELETE FROM sys_dict_data WHERE dict_type = 'mms_defect_type';
DELETE FROM sys_dict_type WHERE dict_type = 'mms_defect_type';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('缺陷类型', 'mms_defect_type', '0', 'admin', sysdate(), '质检缺陷类型');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '外观缺陷', '0', 'mms_defect_type', '', 'warning', 'Y', '0', 'admin', sysdate(), '表面划痕、变色、变形等'),
(2, '尺寸超差', '1', 'mms_defect_type', '', 'danger',  'N', '0', 'admin', sysdate(), '尺寸不符合图纸要求'),
(3, '性能不合格', '2', 'mms_defect_type', '', 'danger',  'N', '0', 'admin', sysdate(), '功能/性能测试不合格'),
(4, '装配不良', '3', 'mms_defect_type', '', 'warning', 'N', '0', 'admin', sysdate(), '装配不到位/错装/漏装'),
(5, '材料缺陷', '4', 'mms_defect_type', '', 'warning', 'N', '0', 'admin', sysdate(), '原材料不合格导致的缺陷'),
(6, '焊接缺陷', '5', 'mms_defect_type', '', 'danger',  'N', '0', 'admin', sysdate(), '虚焊/漏焊/焊渣等'),
(7, '电气缺陷', '6', 'mms_defect_type', '', 'danger',  'N', '0', 'admin', sysdate(), '短路/断路/绝缘不良等'),
(8, '其他',     '7', 'mms_defect_type', '', 'info',     'N', '0', 'admin', sysdate(), '其他类型缺陷');
