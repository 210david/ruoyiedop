-- 修复 SH20260730008 (receive_id=8) 的数据
-- 1. 修正状态为部分验收（有不合格物料50个）
UPDATE pms_receive SET status = '1', audit_by = 'admin', audit_time = '2026-07-30 23:08:08', audit_opinion = '审核通过（系统补录）' WHERE receive_id = 8 AND receive_no = 'SH20260730008';

-- 2. 补录审核日志
INSERT INTO pms_receive_audit_log (receive_id, audit_action, audit_by, audit_time, audit_remark)
VALUES (8, '2', 'admin', '2026-07-30 23:08:08', '审核通过（系统补录，原审核未记录日志）');

-- 3. 验证修复结果
SELECT receive_id, receive_no, status, total_qty, qualified_qty, unqualified_qty, audit_by, audit_time, audit_opinion, inbound_order_id FROM pms_receive WHERE receive_id = 8;
SELECT * FROM pms_receive_audit_log WHERE receive_id = 8;
