-- 滇味鲜普通用户与组织数据完善（依据分析报告，模板数据供用户核实调整）
-- 1. 补充岗位
INSERT INTO sys_post (post_code, post_name, post_sort, status, create_by, create_time) VALUES
('equipment_maintainer', '设备维修员', 14, '0', 'admin', NOW()),
('safety_officer', '安全员', 15, '0', 'admin', NOW());

-- 2. 补充模块角色（菜单权限后续由管理员在角色管理中分配）
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, module_key, status, create_by, create_time) VALUES
('设备管理员', 'dms_manager', 14, '1', 'dms', '0', 'admin', NOW()),
('安全管理员', 'safety_manager', 15, '1', 'safety', '0', 'admin', NOW());
SET @role_dms = (SELECT role_id FROM sys_role WHERE role_key='dms_manager' LIMIT 1);
SET @role_safety = (SELECT role_id FROM sys_role WHERE role_key='safety_manager' LIMIT 1);
SET @pwd = '$2a$10$l/ugJNvtulSATCq25XdItuFWgq9Qrn6H6zyBdVwVKwwhWgS85tEE.';

-- 3. 普通用户（18 人，密码统一 dwx@123456，供用户核实调整）
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, sex, password, status, del_flag, create_by, create_time, remark) VALUES
(101, 'zhangli',  '张莉', '00', '1', @pwd, '0', '0', 'admin', NOW(), '品控主管'),
(101, 'limin',    '李敏', '00', '1', @pwd, '0', '0', 'admin', NOW(), '质检员'),
(101, 'wangfang', '王芳', '00', '1', @pwd, '0', '0', 'admin', NOW(), '质检员'),
(104, 'chenqiang','陈强', '00', '0', @pwd, '0', '0', 'admin', NOW(), '销售经理'),
(104, 'liuyang',  '刘洋', '00', '0', @pwd, '0', '0', 'admin', NOW(), '销售代表'),
(104, 'zhaomin',  '赵敏', '00', '1', @pwd, '0', '0', 'admin', NOW(), '销售代表'),
(105, 'zhouting', '周婷', '00', '1', @pwd, '0', '0', 'admin', NOW(), '人事专员'),
(105, 'wujuan',   '吴娟', '00', '1', @pwd, '0', '0', 'admin', NOW(), '行政专员'),
(105, 'zhenghao', '郑浩', '00', '0', @pwd, '0', '0', 'admin', NOW(), '安全员'),
(201, 'fengjun',  '冯军', '00', '0', @pwd, '0', '0', 'admin', NOW(), '仓库主管'),
(201, 'xuna',     '许娜', '00', '1', @pwd, '0', '0', 'admin', NOW(), '入库管理员'),
(201, 'jianglei', '蒋磊', '00', '0', @pwd, '0', '0', 'admin', NOW(), '出库管理员'),
(202, 'caoyang',  '曹阳', '00', '0', @pwd, '0', '0', 'admin', NOW(), '采购经理'),
(202, 'pengxue',  '彭雪', '00', '1', @pwd, '0', '0', 'admin', NOW(), '采购员'),
(200, 'luwei',    '卢伟', '00', '0', @pwd, '0', '0', 'admin', NOW(), '车间主任'),
(200, 'yuanfang', '袁芳', '00', '1', @pwd, '0', '0', 'admin', NOW(), '生产计划员'),
(200, 'dengchao', '邓超', '00', '0', @pwd, '0', '0', 'admin', NOW(), '设备维修员'),
(200, 'yuyang',   '于洋', '00', '0', @pwd, '0', '0', 'admin', NOW(), '普工');

-- 4. 用户-角色绑定
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 122 FROM sys_user WHERE user_name='zhangli';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 120 FROM sys_user WHERE user_name IN ('limin','wangfang');
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 301 FROM sys_user WHERE user_name='chenqiang';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 302 FROM sys_user WHERE user_name IN ('liuyang','zhaomin');
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 700 FROM sys_user WHERE user_name='zhouting';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 100 FROM sys_user WHERE user_name='wujuan';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, @role_safety FROM sys_user WHERE user_name='zhenghao';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 400 FROM sys_user WHERE user_name='fengjun';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 401 FROM sys_user WHERE user_name='xuna';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 402 FROM sys_user WHERE user_name='jianglei';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 501 FROM sys_user WHERE user_name='caoyang';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 502 FROM sys_user WHERE user_name='pengxue';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 602 FROM sys_user WHERE user_name='luwei';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 601 FROM sys_user WHERE user_name='yuanfang';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, @role_dms FROM sys_user WHERE user_name='dengchao';
INSERT INTO sys_user_role (user_id, role_id)
SELECT user_id, 603 FROM sys_user WHERE user_name='yuyang';

-- 5. 用户-岗位绑定
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 11 FROM sys_user WHERE user_name='zhangli';
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 12 FROM sys_user WHERE user_name IN ('limin','wangfang');
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 13 FROM sys_user WHERE user_name IN ('chenqiang','liuyang','zhaomin');
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 3 FROM sys_user WHERE user_name='zhouting';
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 4 FROM sys_user WHERE user_name='wujuan';
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 15 FROM sys_user WHERE user_name='zhenghao';
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 6 FROM sys_user WHERE user_name IN ('fengjun','xuna','jianglei');
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 7 FROM sys_user WHERE user_name IN ('caoyang','pengxue');
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 8 FROM sys_user WHERE user_name='luwei';
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 9 FROM sys_user WHERE user_name='yuanfang';
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 14 FROM sys_user WHERE user_name='dengchao';
INSERT INTO sys_user_post (user_id, post_id)
SELECT user_id, 10 FROM sys_user WHERE user_name='yuyang';

-- 6. 员工档案（18 人，与用户一一对应，模板数据供用户核实调整）
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0001', '张莉', '1', 101, 11, '品控主管', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='zhangli';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0002', '李敏', '1', 101, 12, '质检员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='limin';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0003', '王芳', '1', 101, 12, '质检员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='wangfang';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0004', '陈强', '0', 104, 13, '销售专员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='chenqiang';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0005', '刘洋', '0', 104, 13, '销售专员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='liuyang';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0006', '赵敏', '1', 104, 13, '销售专员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='zhaomin';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0007', '周婷', '1', 105, 3, '人事专员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='zhouting';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0008', '吴娟', '1', 105, 4, '行政专员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='wujuan';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0009', '郑浩', '0', 105, 15, '安全员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='zhenghao';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0010', '冯军', '0', 201, 6, '库管员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='fengjun';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0011', '许娜', '1', 201, 6, '库管员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='xuna';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0012', '蒋磊', '0', 201, 6, '库管员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='jianglei';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0013', '曹阳', '0', 202, 7, '采购员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='caoyang';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0014', '彭雪', '1', 202, 7, '采购员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='pengxue';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0015', '卢伟', '0', 200, 8, '车间负责人', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='luwei';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0016', '袁芳', '1', 200, 9, '生产计划员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='yuanfang';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0017', '邓超', '0', 200, 14, '设备维修员', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='dengchao';
INSERT INTO biz_hr_employee (employee_no, employee_name, gender, org_id, position_id, position_name, employee_status, entry_date, create_by, create_time)
SELECT 'EMP0018', '于洋', '0', 200, 10, '普工', '0', CURDATE(), 'admin', NOW() FROM sys_user WHERE user_name='yuyang';
