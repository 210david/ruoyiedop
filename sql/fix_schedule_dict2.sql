-- 修复排产管理字典数据（中文乱码）
USE `ry-vue`;

-- 先删除旧的乱码数据
DELETE FROM sys_dict_data WHERE dict_type IN ('mms_schedule_status','mms_schedule_strategy');
DELETE FROM sys_dict_type WHERE dict_type IN ('mms_schedule_status','mms_schedule_strategy');

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('排产状态', 'mms_schedule_status', '0', 'admin', sysdate(), '排产计划状态');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('排产策略', 'mms_schedule_strategy', '0', 'admin', sysdate(), '排产策略类型');

-- 排产状态 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1, '待确认', '0', 'mms_schedule_status', '', 'info', 'Y', '0', 'admin', sysdate(), '待确认');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (2, '已确认', '1', 'mms_schedule_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已确认');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (3, '已下达', '2', 'mms_schedule_status', '', 'success', 'N', '0', 'admin', sysdate(), '已下达');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (4, '已取消', '3', 'mms_schedule_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已取消');

-- 排产策略 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1, '正排', '1', 'mms_schedule_strategy', '', 'primary', 'Y', '0', 'admin', sysdate(), '正排（从计划开始向后推算）');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (2, '倒排', '2', 'mms_schedule_strategy', '', 'warning', 'N', '0', 'admin', sysdate(), '倒排（从交期向前推算）');
