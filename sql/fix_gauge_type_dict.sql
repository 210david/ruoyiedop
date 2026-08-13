-- =============================================
-- 修复 量检具类型字典(qms_gauge_type)
-- 将旧的量具名称(游标卡尺/千分尺/...)替换为分类大类(称量类/分析类/...)
-- 执行后需刷新字典缓存（重启应用 或 调用刷新缓存接口）
-- =============================================

-- 1. 先删除旧的字典数据（无论旧值是否存在，都先清掉）
DELETE FROM sys_dict_data WHERE dict_type = 'qms_gauge_type';

-- 2. 确保字典类型存在
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '量检具类型', 'qms_gauge_type', '0', 'admin', sysdate(), '量检具类型分类'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_gauge_type');

-- 3. 插入新的字典数据（分类大类）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '称量类', '称量类', 'qms_gauge_type', '', 'primary', 'N', '0', 'admin', sysdate(), '天平、水分仪等称量仪器'),
(2, '分析类', '分析类', 'qms_gauge_type', '', 'success', 'N', '0', 'admin', sysdate(), '光谱仪、色谱仪等分析仪器'),
(3, '量具类', '量具类', 'qms_gauge_type', '', 'info',    'N', '0', 'admin', sysdate(), '卡尺、千分尺、量块等量具'),
(4, '监测类', '监测类', 'qms_gauge_type', '', 'warning', 'N', '0', 'admin', sysdate(), '温度记录仪等监测仪器'),
(5, '其他',   '其他',   'qms_gauge_type', '', '',        'N', '0', 'admin', sysdate(), '其他类型量检具');

-- 4. 验证
SELECT dict_sort, dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' ORDER BY dict_sort;
