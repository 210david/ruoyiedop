SELECT detail_id, receive_id, material_id, material_name, receive_qty, qualified_qty, unqualified_qty 
FROM pms_receive_detail 
WHERE receive_id = 3 AND del_flag = '0';
