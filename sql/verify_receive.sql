-- 验证记录存在且del_flag='0'
SELECT receive_id, receive_no, status, del_flag, audit_by, audit_time FROM pms_receive WHERE receive_id = 8;
SELECT receive_id, receive_no, status, del_flag FROM pms_receive WHERE receive_no = 'SH20260730008';
