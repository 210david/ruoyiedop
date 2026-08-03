-- 修复 del_flag
UPDATE pms_receive SET del_flag = '0' WHERE receive_id = 8;

-- 验证
SELECT receive_id, receive_no, status, del_flag FROM pms_receive WHERE receive_id = 8;
