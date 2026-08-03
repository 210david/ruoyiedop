-- 测试selectReceiveDetailByReceiveId
SELECT detail_id, receive_id, order_detail_id, material_id, material_code, material_name,
       spec_model, unit, receive_qty, qualified_qty, unqualified_qty, unit_price, amount,
       batch_no, production_date, expiry_date,
       del_flag, create_by, create_time, update_by, update_time, remark
FROM pms_receive_detail
WHERE receive_id = 3 AND del_flag = '0'
ORDER BY detail_id ASC;
