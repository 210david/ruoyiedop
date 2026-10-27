-- 手动修复 SH20260731001 的状态为已入库
UPDATE pms_receive SET status = '3', update_time = sysdate() WHERE receive_no = 'SH20260731001';

-- 验证
SELECT receive_id, receive_no, status, inbound_order_id FROM pms_receive WHERE receive_no = 'SH20260731001';
