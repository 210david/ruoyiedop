-- =============================================
-- 补充事故等级、提醒状态字典数据（字典类型已存在，仅缺字典数据）
-- 事故等级依据《生产安全事故报告和调查处理条例》(国务院令第493号)
-- =============================================

-- ---------- 事故等级 ----------
DELETE FROM sys_dict_data WHERE dict_type = 'safety_incident_level';

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '特别重大事故', '1', 'safety_incident_level', 'danger', '', 'N', '0', 'admin', NOW(), '造成30人以上死亡，或100人以上重伤，或1亿元以上直接经济损失'),
(NULL, 2, '重大事故',     '2', 'safety_incident_level', 'danger', '', 'N', '0', 'admin', NOW(), '造成10人以上30人以下死亡，或50人以上100人以下重伤，或5000万元以上1亿元以下直接经济损失'),
(NULL, 3, '较大事故',     '3', 'safety_incident_level', 'warning', '', 'N', '0', 'admin', NOW(), '造成3人以上10人以下死亡，或10人以上50人以下重伤，或1000万元以上5000万元以下直接经济损失'),
(NULL, 4, '一般事故',     '4', 'safety_incident_level', 'info', '', 'Y', '0', 'admin', NOW(), '造成3人以下死亡，或10人以下重伤，或1000万元以下直接经济损失');

-- ---------- 提醒状态 ----------
DELETE FROM sys_dict_data WHERE dict_type = 'safety_remind_status';

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '待处理', '0', 'safety_remind_status', 'warning', '', 'Y', '0', 'admin', NOW(), '新增提醒待处理'),
(NULL, 2, '处理中', '1', 'safety_remind_status', 'primary', '', 'N', '0', 'admin', NOW(), '正在处理'),
(NULL, 3, '已处理', '2', 'safety_remind_status', 'success', '', 'N', '0', 'admin', NOW(), '已处理完成');

-- ---------- 验证结果 ----------
SELECT '--- 事故等级字典数据 ---' AS info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type, css_class FROM sys_dict_data WHERE dict_type = 'safety_incident_level' ORDER BY dict_sort;

SELECT '--- 提醒状态字典数据 ---' AS info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type, css_class FROM sys_dict_data WHERE dict_type = 'safety_remind_status' ORDER BY dict_sort;
