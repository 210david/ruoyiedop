-- 重置 SH20260730008 为待审核状态，以便通过API重新审核
UPDATE pms_receive SET status = '5', audit_by = NULL, audit_time = NULL, audit_opinion = NULL, inbound_order_id = NULL WHERE receive_id = 8;

-- 删除之前补录的审核日志
DELETE FROM pms_receive_audit_log WHERE receive_id = 8;

-- 验证
SELECT receive_id, receive_no, status, audit_by, audit_time, inbound_order_id FROM pms_receive WHERE receive_id = 8;
