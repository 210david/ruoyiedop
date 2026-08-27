-- 消息表新增 biz_status 字段：记录消息创建时的业务单据状态
-- 用于判断业务是否已流转过该提醒节点，如果已流转则消息自动失效
ALTER TABLE sys_message ADD COLUMN biz_status VARCHAR(10) DEFAULT NULL COMMENT '消息创建时的业务状态（用于判断是否已处理）';

-- 新增字典：消息处理状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '消息处理状态', 'sys_message_biz_status', '0', 'admin', sysdate(), '消息对应业务单据的处理状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'sys_message_biz_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待处理', '0', 'sys_message_biz_status', '', 'warning', 'N', '0', 'admin', sysdate(), null
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'sys_message_biz_status' and dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已处理', '1', 'sys_message_biz_status', '', 'info', 'N', '0', 'admin', sysdate(), null
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'sys_message_biz_status' and dict_value = '1');
