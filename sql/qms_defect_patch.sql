-- =============================================
-- QMS 缺陷代码补充脚本
-- 1. 添加 qms_defect 编号规则种子数据
-- 2. 添加 缺陷分类字典(qms_defect_category)
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- =============================================
-- 一、添加缺陷代码编号规则
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_defect', 'qms', '缺陷代码编号', 'DF', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '缺陷代码编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_defect');

-- =============================================
-- 二、添加缺陷分类字典
-- =============================================
-- 字典类型（幂等）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '缺陷分类', 'qms_defect_category', '0', 'admin', sysdate(), '质量缺陷分类'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_defect_category');

-- 字典数据（幂等）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '外观缺陷', '1', 'qms_defect_category', '', 'primary', 'N', '0', 'admin', sysdate(), '外观类缺陷'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_defect_category' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '尺寸缺陷', '2', 'qms_defect_category', '', 'warning', 'N', '0', 'admin', sysdate(), '尺寸类缺陷'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_defect_category' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '性能缺陷', '3', 'qms_defect_category', '', 'danger', 'N', '0', 'admin', sysdate(), '性能类缺陷'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_defect_category' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '包装缺陷', '4', 'qms_defect_category', '', 'info', 'N', '0', 'admin', sysdate(), '包装类缺陷'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_defect_category' AND dict_value = '4');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '工艺缺陷', '5', 'qms_defect_category', '', 'success', 'N', '0', 'admin', sysdate(), '工艺类缺陷'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_defect_category' AND dict_value = '5');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '材料缺陷', '6', 'qms_defect_category', '', 'warning', 'N', '0', 'admin', sysdate(), '材料类缺陷'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_defect_category' AND dict_value = '6');

-- =============================================
-- 三、验证数据
-- =============================================
SELECT '缺陷代码补充脚本执行完成' AS result;
SELECT rule_code, rule_name, module, prefix FROM mk_number_rule WHERE rule_code = 'qms_defect';
SELECT dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'qms_defect_category' ORDER BY dict_sort;
