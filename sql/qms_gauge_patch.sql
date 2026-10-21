-- =============================================
-- QMS 量检具台账 补充脚本
-- 1. 添加 qms_gauge 编号规则种子数据（自动编号）
-- 2. 添加 量检具类型字典(qms_gauge_type)
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- =============================================
-- 一、添加量检具编号规则
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_gauge', 'qms', '量检具编号', 'Gauge', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '量检具编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_gauge');

-- =============================================
-- 二、添加量检具类型字典
-- =============================================
-- 字典类型（幂等）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '量检具类型', 'qms_gauge_type', '0', 'admin', sysdate(), '量检具类型分类'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'qms_gauge_type');

-- 字典数据（幂等）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '称量类', '称量类', 'qms_gauge_type', '', 'primary', 'N', '0', 'admin', sysdate(), '天平、水分仪等称量仪器'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '称量类');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '分析类', '分析类', 'qms_gauge_type', '', 'success', 'N', '0', 'admin', sysdate(), '光谱仪、色谱仪等分析仪器'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '分析类');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '量具类', '量具类', 'qms_gauge_type', '', 'info', 'N', '0', 'admin', sysdate(), '卡尺、千分尺、量块等量具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '量具类');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '监测类', '监测类', 'qms_gauge_type', '', 'warning', 'N', '0', 'admin', sysdate(), '温度记录仪等监测仪器'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '监测类');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '其他', '其他', 'qms_gauge_type', '', '', 'N', '0', 'admin', sysdate(), '其他类型量检具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '其他');

-- =============================================
-- 三、验证数据
-- =============================================
SELECT '量检具台账补充脚本执行完成' AS result;
SELECT rule_code, rule_name, module, prefix FROM mk_number_rule WHERE rule_code = 'qms_gauge';
SELECT dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' ORDER BY dict_sort;
