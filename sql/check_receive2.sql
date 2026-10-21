-- 检查更新人信息
SELECT receive_id, receive_no, status, update_by, update_time, create_by FROM pms_receive WHERE receive_no='SH20260730008';

-- 检查审核日志表结构
DESCRIBE pms_receive_audit_log;

-- 检查所有审核日志
SELECT * FROM pms_receive_audit_log ORDER BY log_id;
