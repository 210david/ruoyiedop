-- 检查 safety_hazard_level 字典数据
SELECT '=== safety_hazard_level ===' AS info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type, css_class FROM sys_dict_data WHERE dict_type = 'safety_hazard_level' ORDER BY dict_sort;

-- 检查是否有其他字典被误用了
SELECT '=== safety_hazard_type ===' AS info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'safety_hazard_type' ORDER BY dict_sort;
