-- =============================================
-- 安全生产管理 - 补充培训计划类型字典
-- 功能：修复培训计划页面"计划类型"下拉框无数据的问题
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '培训计划类型', 'safety_training_plan_type', '0', 'admin', NOW(), '培训计划类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'safety_training_plan_type');

-- 字典数据
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT NULL, 1, '三级安全教育', '1', 'safety_training_plan_type', '', 'primary', 'N', '0', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_training_plan_type' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT NULL, 2, '特种作业培训', '2', 'safety_training_plan_type', '', 'success', 'N', '0', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_training_plan_type' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT NULL, 3, '粉尘防爆培训', '3', 'safety_training_plan_type', '', 'info', 'N', '0', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_training_plan_type' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT NULL, 4, '有限空间培训', '4', 'safety_training_plan_type', '', 'warning', 'N', '0', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_training_plan_type' AND dict_value = '4');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT NULL, 5, '危化品培训', '5', 'safety_training_plan_type', '', 'danger', 'N', '0', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_training_plan_type' AND dict_value = '5');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT NULL, 6, '应急处置培训', '6', 'safety_training_plan_type', '', 'primary', 'N', '0', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_training_plan_type' AND dict_value = '6');

-- 验证
SELECT dict_label, dict_value, dict_type, status FROM sys_dict_data WHERE dict_type = 'safety_training_plan_type' ORDER BY dict_sort;
