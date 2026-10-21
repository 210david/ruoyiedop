-- =============================================
-- 新增特种作业类别字典类型和数据
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '特种作业类别', 'safety_job_type', '0', 'admin', NOW(), '特种作业人员作业类别');

-- 字典数据
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '电工作业', '1', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '焊接与热切割作业', '2', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '高处作业', '3', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '制冷与空调作业', '4', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '起重机械作业', '5', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '有限空间作业', '6', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 7, '危险化学品安全作业', '7', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 8, '煤矿安全作业', '8', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 9, '金属非金属矿山安全作业', '9', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 10, '石油天然气安全作业', '10', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 11, '冶金（有色）生产安全作业', '11', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 12, '烟花爆竹安全作业', '12', 'safety_job_type', '', '', 'N', '0', 'admin', NOW(), '');

-- 验证结果
SELECT '--- 字典类型 ---' AS info;
SELECT dict_id, dict_name, dict_type FROM sys_dict_type WHERE dict_type = 'safety_job_type';
SELECT '--- 字典数据 ---' AS info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'safety_job_type' ORDER BY dict_sort;
