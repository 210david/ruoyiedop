-- 重置SH20260730003为待审核状态
UPDATE pms_receive SET status = '5', audit_by = NULL, audit_time = NULL, audit_opinion = NULL WHERE receive_id = 3;

-- 删除旧审核日志
DELETE FROM pms_receive_audit_log WHERE receive_id = 3;

-- 删除旧入库单关联（保留入库单记录但取消关联）
UPDATE pms_receive SET inbound_order_id = NULL WHERE receive_id = 3;

-- 删除重复的收货明细（保留detail_id较小的记录）
DELETE FROM pms_receive_detail WHERE detail_id IN (23, 24, 25);

-- 验证
SELECT receive_id, receive_no, status, audit_by, inbound_order_id FROM pms_receive WHERE receive_id = 3;
SELECT detail_id, receive_id, material_id, material_name, qualified_qty FROM pms_receive_detail WHERE receive_id = 3;
