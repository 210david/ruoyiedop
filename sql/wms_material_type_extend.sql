-- 扩展物料类型字典：增加服务(4)和工程(5)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (5, '服务', '4', 'wms_material_type', '', 'warning', 'N', '0', 'admin', NOW()),
       (6, '工程', '5', 'wms_material_type', '', 'danger', 'N', '0', 'admin', NOW());

-- 同时增加单位字典中的"项"和"次"，方便服务/工程使用
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT 7, '项', '6', 'wms_unit', '', 'primary', 'N', '0', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_unit' AND dict_value = '6');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT 8, '次', '7', 'wms_unit', '', 'primary', 'N', '0', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_unit' AND dict_value = '7');

SELECT '物料类型字典扩展完成' AS result;
