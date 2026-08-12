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
SELECT 1, '游标卡尺', '1', 'qms_gauge_type', '', 'primary', 'N', '0', 'admin', sysdate(), '游标卡尺类量具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '千分尺', '2', 'qms_gauge_type', '', 'success', 'N', '0', 'admin', sysdate(), '千分尺类量具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '百分表', '3', 'qms_gauge_type', '', 'info', 'N', '0', 'admin', sysdate(), '百分表/千分表类量具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '高度尺', '4', 'qms_gauge_type', '', 'warning', 'N', '0', 'admin', sysdate(), '高度尺/深度尺类量具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '4');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '量块', '5', 'qms_gauge_type', '', 'primary', 'N', '0', 'admin', sysdate(), '量块类标准量具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '5');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '塞规', '6', 'qms_gauge_type', '', 'info', 'N', '0', 'admin', sysdate(), '塞规/环规类量具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '6');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, '其他', '9', 'qms_gauge_type', '', '', 'N', '0', 'admin', sysdate(), '其他类型量检具'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' AND dict_value = '9');

-- =============================================
-- 三、验证数据
-- =============================================
SELECT '量检具台账补充脚本执行完成' AS result;
SELECT rule_code, rule_name, module, prefix FROM mk_number_rule WHERE rule_code = 'qms_gauge';
SELECT dict_label, dict_value, dict_type FROM sys_dict_data WHERE dict_type = 'qms_gauge_type' ORDER BY dict_sort;
