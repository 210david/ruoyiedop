-- 修复del_flag
UPDATE pms_receive_detail SET del_flag = '0' WHERE receive_id = 3;

-- 验证
SELECT detail_id, del_flag, qualified_qty FROM pms_receive_detail WHERE receive_id = 3;
