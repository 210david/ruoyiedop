-- 巡检周期类型字典：增加"自定义"类型
-- 值: 0=日检(默认1天), 1=周检(默认7天), 2=月检(默认30天), 3=自定义(必填)
-- 使用方法: mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 < update_inspection_cycle_dict.sql

-- 先查看现有数据
SELECT dict_code, dict_sort, dict_label, dict_value, status FROM sys_dict_data WHERE dict_type = 'dms_inspection_cycle' ORDER BY dict_sort;

-- 如果 value='3' 不存在则插入"自定义"类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '自定义', '3', 'dms_inspection_cycle', '', 'warning', 'N', '0', 'admin', NOW(), '自定义周期天数'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'dms_inspection_cycle' AND dict_value = '3');

-- 验证结果
SELECT dict_code, dict_sort, dict_label, dict_value, status FROM sys_dict_data WHERE dict_type = 'dms_inspection_cycle' ORDER BY dict_sort;
