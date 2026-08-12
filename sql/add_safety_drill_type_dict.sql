-- =============================================
-- 补充演练类型字典数据（字典类型已存在，仅缺字典数据）
-- =============================================

-- 字典数据（先清除可能存在的脏数据，再插入）
DELETE FROM sys_dict_data WHERE dict_type = 'safety_drill_type';

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '综合演练', '1', 'safety_drill_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '专项演练', '2', 'safety_drill_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '现场处置演练', '3', 'safety_drill_type', '', '', 'N', '0', 'admin', NOW(), '');

-- 验证结果
SELECT '--- 演练类型字典数据 ---' AS info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'safety_drill_type' ORDER BY dict_sort;
