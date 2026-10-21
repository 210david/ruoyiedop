-- =============================================
-- 车间/产线/工位 测试数据（清理旧数据 + 插入新数据）
-- 模拟中小型制造企业：3个车间、6条产线、15个工位
-- =============================================

-- 1. 清理旧测试数据
UPDATE qms_workshop SET del_flag = '2' WHERE del_flag = '0';

-- 2. 插入车间（node_type=1）
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
(0, 'WS01', '加工车间', '1', NULL, NULL, NULL, 1, '0', 'admin', NOW(), '负责零部件机加工'),
(0, 'WS02', '装配车间', '1', NULL, NULL, NULL, 2, '0', 'admin', NOW(), '负责产品总装'),
(0, 'WS03', '包装车间', '1', NULL, NULL, NULL, 3, '0', 'admin', NOW(), '负责成品包装入库');

-- 3. 插入产线（node_type=2），parent_id 对应上面插入的车间ID
-- 加工车间(WS01)下2条产线
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'WS01' AND del_flag = '0') AS t1), 'L01', 'CNC加工线', '2', '0', 30, NULL, 1, '0', 'admin', NOW(), '数控加工产线，主要加工铝件'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'WS01' AND del_flag = '0') AS t1), 'L02', '普通机加线', '2', '1', 60, NULL, 2, '0', 'admin', NOW(), '半自动加工产线，加工钢件');

-- 装配车间(WS02)下2条产线
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'WS02' AND del_flag = '0') AS t1), 'L03', '总装线A', '2', '0', 45, NULL, 1, '0', 'admin', NOW(), '自动化总装产线'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'WS02' AND del_flag = '0') AS t1), 'L04', '总装线B', '2', '1', 50, NULL, 2, '0', 'admin', NOW(), '半自动总装产线'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'WS02' AND del_flag = '0') AS t1), 'L05', '手工装配线', '2', '2', 120, NULL, 3, '1', 'admin', NOW(), '手工装配产线（已停用）');

-- 包装车间(WS03)下1条产线
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'WS03' AND del_flag = '0') AS t1), 'L06', '包装线', '2', '1', 20, NULL, 1, '0', 'admin', NOW(), '半自动包装产线');

-- 4. 插入工位（node_type=3），parent_id 对应上面插入的产线ID
-- CNC加工线(L01)下3个工位
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L01' AND del_flag = '0') AS t1), 'S01', '上料工位', '3', NULL, NULL, '0', 1, '0', 'admin', NOW(), '原料上料'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L01' AND del_flag = '0') AS t1), 'S02', 'CNC加工工位', '3', NULL, NULL, '0', 2, '0', 'admin', NOW(), '数控加工'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L01' AND del_flag = '0') AS t1), 'S03', '检验工位', '3', NULL, NULL, '1', 3, '0', 'admin', NOW(), '首件检验');

-- 普通机加线(L02)下2个工位
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L02' AND del_flag = '0') AS t1), 'S04', '车削工位', '3', NULL, NULL, '0', 1, '0', 'admin', NOW(), '车床加工'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L02' AND del_flag = '0') AS t1), 'S05', '铣削工位', '3', NULL, NULL, '0', 2, '0', 'admin', NOW(), '铣床加工');

-- 总装线A(L03)下4个工位
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L03' AND del_flag = '0') AS t1), 'S06', '预装工位', '3', NULL, NULL, '0', 1, '0', 'admin', NOW(), '零部件预装'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L03' AND del_flag = '0') AS t1), 'S07', '总装工位', '3', NULL, NULL, '0', 2, '0', 'admin', NOW(), '主体总装'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L03' AND del_flag = '0') AS t1), 'S08', '调试工位', '3', NULL, NULL, '0', 3, '0', 'admin', NOW(), '功能调试'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L03' AND del_flag = '0') AS t1), 'S09', '检验工位', '3', NULL, NULL, '1', 4, '0', 'admin', NOW(), '成品检验');

-- 总装线B(L04)下3个工位
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L04' AND del_flag = '0') AS t1), 'S10', '组装工位', '3', NULL, NULL, '0', 1, '0', 'admin', NOW(), '半自动组装'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L04' AND del_flag = '0') AS t1), 'S11', '紧固工位', '3', NULL, NULL, '0', 2, '0', 'admin', NOW(), '螺栓紧固'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L04' AND del_flag = '0') AS t1), 'S12', '包装工位', '3', NULL, NULL, '2', 3, '0', 'admin', NOW(), '产品包装');

-- 包装线(L06)下3个工位
INSERT INTO qms_workshop (parent_id, workshop_code, workshop_name, node_type, line_type, cycle_time, station_type, order_num, status, create_by, create_time, remark) VALUES
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L06' AND del_flag = '0') AS t1), 'S13', '贴标工位', '3', NULL, NULL, '2', 1, '0', 'admin', NOW(), '产品贴标'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L06' AND del_flag = '0') AS t1), 'S14', '装箱工位', '3', NULL, NULL, '2', 2, '0', 'admin', NOW(), '成品装箱'),
((SELECT workshop_id FROM (SELECT workshop_id FROM qms_workshop WHERE workshop_code = 'L06' AND del_flag = '0') AS t1), 'S15', '暂存工位', '3', NULL, NULL, '3', 3, '0', 'admin', NOW(), '成品暂存待入库');
