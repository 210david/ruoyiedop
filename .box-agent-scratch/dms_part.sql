INSERT INTO dms_inspection_route (route_name, route_code, dept_id, dept_name, cycle_type, cycle_value, equipment_ids, inspection_items, status, del_flag, create_by, create_time) VALUES
('发酵车间点检路线', 'DJ01', 200, '生产部', '2', 1, '1,2,4', '翻酱机传动润滑；发酵池沿口密封；发酵槽温湿度显示', '0', '0', 'admin', NOW()),
('灌装车间点检路线', 'DJ02', 200, '生产部', '2', 1, '10,9', '灌装封口机传动防护罩；定量加料装置计量精度', '0', '0', 'admin', NOW());
INSERT INTO dms_pm_plan (plan_name, equipment_id, category_id, trigger_type, cycle_value, cycle_unit, task_list, estimated_hours, advance_days, assignee_name, next_execute_time, status, del_flag, create_by, create_time) VALUES
('翻酱机月度润滑保养', 4, (SELECT category_id FROM dms_equipment_category WHERE category_name='酿造发酵设备' LIMIT 1), '2', 1, '月', '传动链条润滑；减速机油位检查；清洁翻酱桨叶', 2, 3, '邓超', DATE_ADD(CURDATE(), INTERVAL 15 DAY), '0', '0', 'admin', NOW()),
('发酵池季度检查保养', 1, (SELECT category_id FROM dms_equipment_category WHERE category_name='酿造发酵设备' LIMIT 1), '2', 3, '月', '池体检查；沿口密封更换；排水通道疏通', 4, 5, '邓超', DATE_ADD(CURDATE(), INTERVAL 30 DAY), '0', '0', 'admin', NOW()),
('灌装封口机月度保养', 10, (SELECT category_id FROM dms_equipment_category WHERE category_name='灌装包装设备' LIMIT 1), '2', 1, '月', '封口温度校准；传动防护罩紧固；计量机构清洁', 3, 3, '邓超', DATE_ADD(CURDATE(), INTERVAL 20 DAY), '0', '0', 'admin', NOW());
