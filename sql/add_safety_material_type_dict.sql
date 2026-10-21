-- =============================================
-- 添加应急物资类别字典 (safety_material_type)
-- =============================================

-- 1. 字典类型（先清除可能存在的脏数据）
DELETE FROM sys_dict_type WHERE dict_type = 'safety_material_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('物资类别', 'safety_material_type', '0', 'admin', NOW(), '应急物资类别字典');

-- 2. 字典数据（先清除可能存在的脏数据）
DELETE FROM sys_dict_data WHERE dict_type = 'safety_material_type';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '防护用品',   '1', 'safety_material_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(2, '救援设备',   '2', 'safety_material_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '医疗急救',   '3', 'safety_material_type', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(4, '消防器材',   '4', 'safety_material_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(5, '通讯设备',   '5', 'safety_material_type', '', 'info',    'N', '0', 'admin', NOW(), ''),
(6, '应急食品饮水', '6', 'safety_material_type', '', 'info',    'N', '0', 'admin', NOW(), ''),
(7, '其他',       '9', 'safety_material_type', '', 'default', 'N', '0', 'admin', NOW(), '');

-- 3. 验证结果
SELECT '--- 物资类别字典类型 ---' AS info;
SELECT dict_name, dict_type, status FROM sys_dict_type WHERE dict_type = 'safety_material_type';

SELECT '--- 物资类别字典数据 ---' AS info;
SELECT dict_sort, dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'safety_material_type' ORDER BY dict_sort;
