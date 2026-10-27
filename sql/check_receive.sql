SELECT receive_id, receive_no, status, total_qty, qualified_qty, unqualified_qty, audit_by, audit_time, inbound_order_id FROM pms_receive WHERE receive_no='SH20260730008';
SELECT detail_id, receive_id, material_name, receive_qty, qualified_qty, unqualified_qty FROM pms_receive_detail WHERE receive_id = (SELECT receive_id FROM pms_receive WHERE receive_no='SH20260730008');
SELECT * FROM pms_receive_audit_log WHERE receive_id = (SELECT receive_id FROM pms_receive WHERE receive_no='SH20260730008');
