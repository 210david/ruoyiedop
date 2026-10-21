-- 滇味鲜核心业务单据链种子数据（采购/营销/仓储/设备管理，模板数据供用户核实调整）
-- 1. 采购链：采购订单（辣椒）→ 收货单 → 入库单
INSERT INTO pms_purchase_order (order_no, supplier_id, supplier_name, warehouse_id, status, order_date, expect_delivery_date, actual_delivery_date, total_qty, total_amount, received_amount, paid_amount, buyer_id, buyer_name, dept_id, dept_name, payment_method, payment_days, settlement_type, currency, delivery_address, del_flag, create_by, create_time, remark) VALUES
('CG20261017001', 1, '陆良县芳华辣椒种植专业合作社', 1, '5', '2026-10-15', '2026-10-17', '2026-10-17', 1000, 12000.00, 12000.00, 0, 14, '曹阳', 202, '采购组', '1', 30, '1', 'CNY', '陆良县青山工业园区滇味鲜原料库', '0', 'admin', NOW(), '干辣椒秋季备货采购（已全部入库）');
SET @po = LAST_INSERT_ID();
INSERT INTO pms_purchase_order_detail (order_id, material_id, material_code, material_name, unit, qty, unit_price, amount, received_qty, del_flag, create_by, create_time) VALUES
(@po, 1, 'YL0001', '干辣椒', '公斤', 1000, 12.00, 12000.00, 1000, '0', 'admin', NOW());
INSERT INTO pms_receive (receive_no, order_id, order_no, supplier_id, supplier_name, warehouse_id, status, receive_date, total_qty, qualified_qty, unqualified_qty, inspector_name, del_flag, create_by, create_time, dept_id) VALUES
('SH20261017001', @po, 'CG20261017001', 1, '陆良县芳华辣椒种植专业合作社', 1, '3', '2026-10-17', 1000, 1000, 0, '张莉', '0', 'admin', NOW(), 202);
SET @rcv = LAST_INSERT_ID();
INSERT INTO wms_inbound_order (order_no, order_type, supplier_id, warehouse_id, status, inbound_date, complete_date, total_qty, total_amount, del_flag, create_by, create_time, dept_id) VALUES
('RK20261017001', '1', 1, 1, '3', '2026-10-17', '2026-10-17', 1000, 12000.00, '0', 'admin', NOW(), 201);
SET @rk = LAST_INSERT_ID();
INSERT INTO wms_inbound_order_detail (order_id, material_id, plan_qty, received_qty, putaway_qty, batch_no, production_date, expiry_date, unit_price, del_flag, create_by, create_time) VALUES
(@rk, 1, 1000, 1000, 1000, 'GJ20261015-01', '2026-10-15', '2027-10-15', 12.00, '0', 'admin', NOW());
UPDATE pms_receive SET inbound_order_id=@rk WHERE receive_id=@rcv;
INSERT INTO wms_inventory (location_id, material_id, warehouse_id, batch_no, production_date, expiry_date, qty, lock_qty, del_flag, create_by, create_time) VALUES
(0, 1, 1, 'GJ20261015-01', '2026-10-15', '2027-10-15', 1000, 0, '0', 'admin', NOW());

-- 2. 采购链：玻璃瓶包材采购（未到货，状态已下单）
INSERT INTO pms_purchase_order (order_no, supplier_id, supplier_name, warehouse_id, status, order_date, expect_delivery_date, total_qty, total_amount, received_amount, paid_amount, buyer_id, buyer_name, dept_id, dept_name, payment_method, payment_days, settlement_type, currency, delivery_address, del_flag, create_by, create_time, remark) VALUES
('CG20261017002', 6, '曲靖市宏发玻璃制品有限公司', 3, '3', '2026-10-17', '2026-11-01', 5000, 4250.00, 0, 0, 14, '曹阳', 202, '采购组', '2', 45, '2', 'CNY', '陆良县青山工业园区滇味鲜包材库', '0', 'admin', NOW(), '玻璃瓶月度采购（待到货）');
INSERT INTO pms_purchase_order_detail (order_id, material_id, material_code, material_name, unit, qty, unit_price, amount, received_qty, del_flag, create_by, create_time) VALUES
(LAST_INSERT_ID(), 7, 'FL0001', '玻璃瓶', '个', 5000, 0.85, 4250.00, 0, '0', 'admin', NOW());

-- 3. 仓储链：成品期初库存（发酵半成品 + 成品）
INSERT INTO wms_inbound_order (order_no, order_type, supplier_id, warehouse_id, status, inbound_date, complete_date, total_qty, total_amount, del_flag, create_by, create_time, dept_id) VALUES
('RK20261017002', '2', NULL, 4, '3', '2026-10-16', '2026-10-16', 2600, 0, '0', 'admin', NOW(), 201);
SET @rk2 = LAST_INSERT_ID();
INSERT INTO wms_inbound_order_detail (order_id, material_id, plan_qty, received_qty, putaway_qty, batch_no, production_date, expiry_date, del_flag, create_by, create_time) VALUES
(@rk2, 11, 2600, 2600, 2600, 'FJ20260601-01', '2026-06-01', '2027-06-01', '0', 'admin', NOW());
INSERT INTO wms_inventory (location_id, material_id, warehouse_id, batch_no, production_date, expiry_date, qty, lock_qty, del_flag, create_by, create_time) VALUES
(0, 11, 4, 'FJ20260601-01', '2026-06-01', '2027-06-01', 2600, 0, '0', 'admin', NOW());
INSERT INTO wms_inbound_order (order_no, order_type, supplier_id, warehouse_id, status, inbound_date, complete_date, total_qty, total_amount, del_flag, create_by, create_time, dept_id) VALUES
('RK20261017003', '2', NULL, 5, '3', '2026-10-16', '2026-10-16', 3600, 0, '0', 'admin', NOW(), 201);
SET @rk3 = LAST_INSERT_ID();
INSERT INTO wms_inbound_order_detail (order_id, material_id, plan_qty, received_qty, putaway_qty, batch_no, production_date, expiry_date, del_flag, create_by, create_time) VALUES
(@rk3, 12, 1200, 1200, 1200, 'CP20260920-01', '2026-09-20', '2027-09-20', '0', 'admin', NOW()),
(@rk3, 13, 1200, 1200, 1200, 'CP20260920-02', '2026-09-20', '2027-09-20', '0', 'admin', NOW()),
(@rk3, 14, 800, 800, 800, 'CP20260920-03', '2026-09-20', '2027-09-20', '0', 'admin', NOW()),
(@rk3, 15, 400, 400, 400, 'CP20260920-04', '2026-09-20', '2027-09-20', '0', 'admin', NOW());
INSERT INTO wms_inventory (location_id, material_id, warehouse_id, batch_no, production_date, expiry_date, qty, lock_qty, del_flag, create_by, create_time) VALUES
(0, 12, 5, 'CP20260920-01', '2026-09-20', '2027-09-20', 1200, 0, '0', 'admin', NOW()),
(0, 13, 5, 'CP20260920-02', '2026-09-20', '2027-09-20', 1200, 0, '0', 'admin', NOW()),
(0, 14, 5, 'CP20260920-03', '2026-09-20', '2027-09-20', 800, 0, '0', 'admin', NOW()),
(0, 15, 5, 'CP20260920-04', '2026-09-20', '2027-09-20', 400, 0, '0', 'admin', NOW());

-- 4. 营销链：销售订单（师宗一中食堂供货）→ 发货单
INSERT INTO mk_order (order_no, customer_id, order_amount, shipped_qty, order_status, user_id, dept_id, del_flag, create_by, create_time, remark) VALUES
('XS20261017001', 1, 7200.00, 500, '3', 4, 104, '0', 'admin', NOW(), '师宗县第一中学2026-2027学年食堂调味品首批供货（部分发货）');
SET @so = LAST_INSERT_ID();
INSERT INTO mk_order_item (order_id, line_no, material_id, material_code, product_name, unit, quantity, shipped_qty, unit_price, subtotal, create_time) VALUES
(@so, 1, 12, 'CP0001', '豆瓣酱', '瓶', 300, 300, 12.00, 3600.00, NOW()),
(@so, 2, 13, 'CP0002', '香辣酱', '瓶', 200, 200, 12.00, 2400.00, NOW()),
(@so, 3, 15, 'CP0004', '酱腌菜', '袋', 300, 0, 4.00, 1200.00, NOW());
INSERT INTO mk_shipment (shipment_no, order_id, order_no, customer_id, customer_name, warehouse_id, warehouse_name, status, shipment_date, total_qty, total_amount, receiver_name, receiver_phone, receiver_address, shipper_name, del_flag, create_by, create_time) VALUES
('FH20261017001', @so, 'XS20261017001', 1, '师宗县第一中学', 5, '成品库', '2', '2026-10-17', 500, 6000.00, '李老师', NULL, '师宗县第一中学食堂', '刘洋', '0', 'admin', NOW());
SET @fh = LAST_INSERT_ID();
INSERT INTO mk_shipment_detail (shipment_id, product_name, material_id, material_code, material_name, unit, batch_no, order_qty, ship_qty, unit_price, amount, del_flag, create_by, create_time) VALUES
(@fh, '豆瓣酱', 12, 'CP0001', '豆瓣酱', '瓶', 'CP20260920-01', 300, 300, 12.00, 3600.00, '0', 'admin', NOW()),
(@fh, '香辣酱', 13, 'CP0002', '香辣酱', '瓶', 'CP20260920-02', 200, 200, 12.00, 2400.00, '0', 'admin', NOW());
INSERT INTO wms_inventory (location_id, material_id, warehouse_id, batch_no, production_date, expiry_date, qty, lock_qty, del_flag, create_by, create_time) VALUES
(0, 12, 5, 'CP20260920-01', '2026-09-20', '2027-09-20', -300, 0, '0', 'admin', NOW()),
(0, 13, 5, 'CP20260920-02', '2026-09-20', '2027-09-20', -200, 0, '0', 'admin', NOW());

-- 5. 设备管理链：点检路线 + 保养计划
INSERT INTO dms_inspection_route (route_name, route_code, dept_id, dept_name, cycle_type, cycle_value, equipment_ids, inspection_items, status, del_flag, create_by, create_time) VALUES
('发酵车间点检路线', 'DJ01', 200, '生产部', '2', 1, '1,2,4', '翻酱机传动润滑；发酵池沿口密封；发酵槽温湿度显示', '0', '0', 'admin', NOW()),
('灌装车间点检路线', 'DJ02', 200, '生产部', '2', 1, '10,9', '灌装封口机传动防护罩；定量加料装置计量精度', '0', '0', 'admin', NOW());
INSERT INTO dms_pm_plan (plan_name, equipment_id, category_id, trigger_type, cycle_value, cycle_unit, task_list, estimated_hours, advance_days, assignee_name, next_execute_time, status, del_flag, create_by, create_time) VALUES
('翻酱机月度润滑保养', 4, (SELECT category_id FROM dms_equipment_category WHERE category_name='酿造发酵设备' LIMIT 1), '2', 1, '月', '传动链条润滑；减速机油位检查；清洁翻酱桨叶', 2, 3, '邓超', DATE_ADD(CURDATE(), INTERVAL 15 DAY), '0', '0', 'admin', NOW()),
('发酵池季度检查保养', 1, (SELECT category_id FROM dms_equipment_category WHERE category_name='酿造发酵设备' LIMIT 1), '2', 3, '月', '池体检查；沿口密封更换；排水通道疏通', 4, 5, '邓超', DATE_ADD(CURDATE(), INTERVAL 30 DAY), '0', '0', 'admin', NOW()),
('灌装封口机月度保养', 10, (SELECT category_id FROM dms_equipment_category WHERE category_name='灌装包装设备' LIMIT 1), '2', 1, '月', '封口温度校准；传动防护罩紧固；计量机构清洁', 3, 3, '邓超', DATE_ADD(CURDATE(), INTERVAL 20 DAY), '0', '0', 'admin', NOW());
