-- =============================================
-- DMS 巡检路线 + 点检任务 测试数据（10条路线 + 10条任务）
-- 数据库: ry-vue  编码: utf8mb4
-- 执行方式: mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue < dms_inspection_test_data.sql
-- =============================================

-- 防止重复插入：先清理旧测试数据（route_id >= 1000 的测试数据）
DELETE FROM dms_inspection_task  WHERE route_id >= 1000;
DELETE FROM dms_inspection_route WHERE route_id  >= 1000;
ALTER TABLE dms_inspection_route AUTO_INCREMENT = 1000;
ALTER TABLE dms_inspection_task  AUTO_INCREMENT = 1000;

-- =============================================
-- 一、巡检路线 10条
-- =============================================
INSERT INTO dms_inspection_route
(route_id, route_name, route_code, dept_id, dept_name, cycle_type, cycle_value, equipment_ids, inspection_items, status, del_flag, create_by, create_time, remark)
VALUES
(1000, '生产车间日常巡检路线', 'RT-TEST-001', 200, '生产部',   '0', 1,  '[1,2]',
 '{"common":[{"item":"设备外观检查","type":"check","abnormalRequired":true},{"item":"设备周围卫生","type":"check","abnormalRequired":false}],"devices":[{"equipmentId":1,"items":[{"item":"主轴温度","type":"number","unit":"℃","abnormalRequired":true}]},{"equipmentId":2,"items":[{"item":"刀库位置","type":"check","abnormalRequired":false}]}]}',
 '0', '0', 'admin', '2026-08-01 09:00:00', '生产车间日检路线'),

(1001, '研发实验室周巡检路线',   'RT-TEST-002', 103, '研发部',   '1', 7,  '[2,5]',
 '{"common":[{"item":"安全防护装置","type":"check","abnormalRequired":true}],"devices":[{"equipmentId":2,"items":[{"item":"导轨润滑","type":"check","abnormalRequired":false},{"item":"主轴转速","type":"number","unit":"rpm","abnormalRequired":true}]},{"equipmentId":5,"items":[{"item":"冷却液液位","type":"check","abnormalRequired":true}]}]}',
 '0', '0', 'admin', '2026-08-03 10:00:00', '研发部周检路线'),

(1002, '市场部设备月度巡检路线', 'RT-TEST-003', 104, '市场部',   '2', 30, '[3,4]',
 '{"common":[{"item":"设备清洁度","type":"check","abnormalRequired":false},{"item":"电气线路","type":"check","abnormalRequired":true}],"devices":[{"equipmentId":3,"items":[{"item":"主轴跳动","type":"number","unit":"μm","abnormalRequired":true}]},{"equipmentId":4,"items":[{"item":"气压系统","type":"check","abnormalRequired":true}]}]}',
 '0', '0', 'admin', '2026-08-05 14:00:00', '市场部月检路线'),

(1003, '综合管理部日常点检路线', 'RT-TEST-004', 105, '综合管理部', '0', 1,  '[1,6]',
 '{"common":[{"item":"设备运行声音","type":"check","abnormalRequired":true},{"item":"操作面板","type":"check","abnormalRequired":false}],"devices":[{"equipmentId":1,"items":[{"item":"液压系统压力","type":"number","unit":"MPa","abnormalRequired":true}]},{"equipmentId":6,"items":[{"item":"刀库状态","type":"check","abnormalRequired":false}]}]}',
 '0', '0', 'admin', '2026-08-07 08:30:00', '综合管理部日检路线'),

(1004, 'CNC加工中心周检路线',    'RT-TEST-005', 200, '生产部',   '1', 7,  '[4,5,6]',
 '{"common":[{"item":"润滑系统","type":"check","abnormalRequired":true},{"item":"冷却系统","type":"check","abnormalRequired":true}],"devices":[{"equipmentId":4,"items":[{"item":"主轴温度","type":"number","unit":"℃","abnormalRequired":true},{"item":"刀库换刀","type":"check","abnormalRequired":false}]},{"equipmentId":5,"items":[{"item":"导轨精度","type":"check","abnormalRequired":true}]},{"equipmentId":6,"items":[{"item":"数控系统","type":"check","abnormalRequired":false}]}]}',
 '0', '0', 'admin', '2026-08-10 11:00:00', 'CNC加工中心周检'),

(1005, '多功能设备月度路线',     'RT-TEST-006', 103, '研发部',   '2', 30, '[1,2,3]',
 '{"common":[{"item":"安全门","type":"check","abnormalRequired":true}],"devices":[{"equipmentId":1,"items":[{"item":"电气柜散热","type":"check","abnormalRequired":false}]},{"equipmentId":2,"items":[{"item":"伺服电机温度","type":"number","unit":"℃","abnormalRequired":true}]},{"equipmentId":3,"items":[{"item":"工作台精度","type":"check","abnormalRequired":true}]}]}',
 '0', '0', 'admin', '2026-08-12 15:00:00', '多功能设备月检'),

(1006, '生产车间自定义周期路线', 'RT-TEST-007', 200, '生产部',   '3', 15, '[1,4]',
 '{"common":[{"item":"设备基础螺栓","type":"check","abnormalRequired":true},{"item":"急停按钮","type":"check","abnormalRequired":true}],"devices":[{"equipmentId":1,"items":[{"item":"主轴振动","type":"number","unit":"mm/s","abnormalRequired":true}]},{"equipmentId":4,"items":[{"item":"气压值","type":"number","unit":"MPa","abnormalRequired":true}]}]}',
 '0', '0', 'admin', '2026-08-15 09:30:00', '自定义15天周期'),

(1007, '市场部立式铣床巡检路线', 'RT-TEST-008', 104, '市场部',   '0', 1,  '[3]',
 '{"common":[{"item":"外观检查","type":"check","abnormalRequired":false}],"devices":[{"equipmentId":3,"items":[{"item":"主轴温升","type":"number","unit":"℃","abnormalRequired":true},{"item":"切削液浓度","type":"text","abnormalRequired":false}]}]}',
 '0', '0', 'admin', '2026-08-18 10:00:00', '立式铣床日检'),

(1008, '综合管理部加工中心路线', 'RT-TEST-009', 105, '综合管理部', '1', 7,  '[5,6]',
 '{"common":[{"item":"设备卫生","type":"check","abnormalRequired":false}],"devices":[{"equipmentId":5,"items":[{"item":"主轴精度","type":"check","abnormalRequired":true}]},{"equipmentId":6,"items":[{"item":"刀库位置","type":"check","abnormalRequired":true}]}]}',
 '0', '0', 'admin', '2026-08-20 13:00:00', '加工中心周检'),

(1009, '摇臂钻床专项巡检路线',   'RT-TEST-010', 104, '市场部',   '2', 30, '[4]',
 '{"common":[{"item":"润滑状态","type":"check","abnormalRequired":true}],"devices":[{"equipmentId":4,"items":[{"item":"主轴转速","type":"number","unit":"rpm","abnormalRequired":true},{"item":"摇臂锁紧","type":"check","abnormalRequired":true}]}]}',
 '1', '0', 'admin', '2026-08-22 16:00:00', '已停用的月检路线');

-- =============================================
-- 二、点检任务 10条
-- =============================================
INSERT INTO dms_inspection_task
(task_id, task_no, route_id, route_name, plan_date, inspector_id, inspector_name, task_status, start_time, complete_time, abnormal_count, result_data, photo_urls, del_flag, create_by, create_time, remark)
VALUES
-- 1. 已完成任务（正常）
(1000, 'INS-TEST-001', 1000, '生产车间日常巡检路线', '2026-08-25', 2,   '李华',   '2',
 '2026-08-25 08:30:00', '2026-08-25 09:15:00', 0,
 '{"common":[{"item":"设备外观检查","result":"正常"},{"item":"设备周围卫生","result":"正常"}],"devices":[{"equipmentId":1,"items":[{"item":"主轴温度","result":"42","unit":"℃","normal":true}]}]}',
 '[]', '0', 'admin', '2026-08-25 01:00:00', '正常完成'),

-- 2. 已完成任务（有异常）
(1001, 'INS-TEST-002', 1000, '生产车间日常巡检路线', '2026-08-26', 100, '张涛',   '2',
 '2026-08-26 08:20:00', '2026-08-26 09:00:00', 1,
 '{"common":[{"item":"设备外观检查","result":"异常","abnormalNote":"设备表面有油污泄漏"},{"item":"设备周围卫生","result":"正常"}],"devices":[{"equipmentId":2,"items":[{"item":"刀库位置","result":"正常"}]}]}',
 '["/profile/upload/2026/08/26/abnormal1.jpg"]', '0', 'admin', '2026-08-26 01:00:00', '发现异常项1个'),

-- 3. 执行中任务
(1002, 'INS-TEST-003', 1001, '研发实验室周巡检路线', '2026-08-27', 110, '王伟',   '1',
 '2026-08-27 08:45:00', NULL, 0,
 NULL, NULL, '0', 'admin', '2026-08-27 01:00:00', '执行中'),

-- 4. 待执行任务
(1003, 'INS-TEST-004', 1002, '市场部设备月度巡检路线', '2026-08-28', 113, '张敏',   '0',
 NULL, NULL, 0, NULL, NULL, '0', 'admin', '2026-08-27 01:00:00', '待执行'),

-- 5. 待执行任务
(1004, 'INS-TEST-005', 1003, '综合管理部日常点检路线', '2026-08-27', 2,   '李华',   '0',
 NULL, NULL, 0, NULL, NULL, '0', 'admin', '2026-08-27 01:00:00', '待执行'),

-- 6. 已完成任务（多异常）
(1005, 'INS-TEST-006', 1004, 'CNC加工中心周检路线', '2026-08-24', 111, '刘洋',   '2',
 '2026-08-24 09:00:00', '2026-08-24 10:30:00', 2,
 '{"common":[{"item":"润滑系统","result":"异常","abnormalNote":"润滑油不足"},{"item":"冷却系统","result":"异常","abnormalNote":"冷却液泄漏"}],"devices":[{"equipmentId":4,"items":[{"item":"主轴温度","result":"65","unit":"℃","normal":false,"abnormalNote":"温度偏高"}]},{"equipmentId":5,"items":[{"item":"导轨精度","result":"正常"}]},{"equipmentId":6,"items":[{"item":"数控系统","result":"正常"}]}]}',
 '["/profile/upload/2026/08/24/oil_leak.jpg","/profile/upload/2026/08/24/temp_high.jpg"]', '0', 'admin', '2026-08-24 01:00:00', '发现2项异常'),

-- 7. 已逾期任务
(1006, 'INS-TEST-007', 1005, '多功能设备月度路线', '2026-08-20', 112, '陈杰',   '3',
 NULL, NULL, 0, NULL, NULL, '0', 'admin', '2026-08-20 01:00:00', '未按时执行已逾期'),

-- 8. 待执行任务
(1007, 'INS-TEST-008', 1006, '生产车间自定义周期路线', '2026-08-30', 115, '孙丽',   '0',
 NULL, NULL, 0, NULL, NULL, '0', 'admin', '2026-08-27 01:00:00', '待执行'),

-- 9. 执行中任务
(1008, 'INS-TEST-009', 1007, '市场部立式铣床巡检路线', '2026-08-27', 116, '周伟',   '1',
 '2026-08-27 08:50:00', NULL, 0,
 '{"common":[{"item":"外观检查","result":"正常"}]}',
 NULL, '0', 'admin', '2026-08-27 01:00:00', '执行中'),

-- 10. 已完成任务（正常）
(1009, 'INS-TEST-010', 1008, '综合管理部加工中心路线', '2026-08-25', 2,   '李华',   '2',
 '2026-08-25 13:00:00', '2026-08-25 14:20:00', 0,
 '{"common":[{"item":"设备卫生","result":"正常"}],"devices":[{"equipmentId":5,"items":[{"item":"主轴精度","result":"正常"}]},{"equipmentId":6,"items":[{"item":"刀库位置","result":"正常"}]}]}',
 '[]', '0', 'admin', '2026-08-25 01:00:00', '正常完成');
