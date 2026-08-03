-- 重置收货单为待审核状态
UPDATE pms_receive SET status = '5', audit_by = NULL, audit_time = NULL, audit_opinion = NULL, inbound_order_id = NULL WHERE receive_id = 3;

-- 删除审核日志
DELETE FROM pms_receive_audit_log WHERE receive_id = 3;

-- 验证
SELECT receive_id, receive_no, status, inbound_order_id FROM pms_receive WHERE receive_id = 3;
