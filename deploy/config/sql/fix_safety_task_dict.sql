-- Fix safety_task_type and safety_task_status dictionary data (missing dict_data entries)
-- 字典类型已存在但字典数据为空，补充字典数据

-- =============================================
-- 一、确保字典类型存在（已存在则跳过）
-- =============================================
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('任务类型', 'safety_task_type', '0', 'admin', NOW(), '排查任务类型'),
('任务状态', 'safety_task_status', '0', 'admin', NOW(), '排查任务状态');

-- =============================================
-- 二、清除旧数据并重新插入字典数据
-- =============================================
DELETE FROM sys_dict_data WHERE dict_type IN ('safety_task_type', 'safety_task_status');

-- 任务类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '日常排查',   '1', 'safety_task_type', '', '',          'N', '0', 'admin', NOW(), ''),
(2, '综合排查',   '2', 'safety_task_type', '', '',          'N', '0', 'admin', NOW(), ''),
(3, '专项排查',   '3', 'safety_task_type', '', '',          'N', '0', 'admin', NOW(), ''),
(4, '季节性排查', '4', 'safety_task_type', '', '',          'N', '0', 'admin', NOW(), ''),
(5, '节假日排查', '5', 'safety_task_type', '', '',          'N', '0', 'admin', NOW(), '');

-- 任务状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待执行', '0', 'safety_task_status', '', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '执行中', '1', 'safety_task_status', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(3, '已完成', '2', 'safety_task_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
(4, '已作废', '3', 'safety_task_status', '', 'info',    'N', '0', 'admin', NOW(), '');
