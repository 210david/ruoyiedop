-- =============================================
-- HR人力资源管理模块 - 菜单三级结构重组
-- =============================================
-- 将原来的28个扁平二级菜单，按业务分类为7个二级目录(M) + 28个三级菜单(C)
USE `ry-vue`;
SET NAMES utf8mb4;

-- ========== 1. 创建7个二级目录 ==========
-- 组织人事
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name)
VALUES ('组织人事', 905538, 1, 'org', NULL, 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '组织人事目录', 'HrOrg');
SET @hrOrgId = LAST_INSERT_ID();

-- 入转调离
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name)
VALUES ('入转调离', 905538, 2, 'lifecycle', NULL, 1, 0, 'M', '0', '0', '', 'guide', 'admin', sysdate(), '入转调离目录', 'HrLifecycle');
SET @hrLifecycleId = LAST_INSERT_ID();

-- 合同管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name)
VALUES ('合同管理', 905538, 3, 'contract-mgmt', NULL, 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '合同管理目录', 'HrContractMgmt');
SET @hrContractMgmtId = LAST_INSERT_ID();

-- 考勤管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name)
VALUES ('考勤管理', 905538, 4, 'attendance', NULL, 1, 0, 'M', '0', '0', '', 'time', 'admin', sysdate(), '考勤管理目录', 'HrAttendanceMgmt');
SET @hrAttendanceMgmtId = LAST_INSERT_ID();

-- 薪资管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name)
VALUES ('薪资管理', 905538, 5, 'salary', NULL, 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '薪资管理目录', 'HrSalaryMgmt');
SET @hrSalaryMgmtId = LAST_INSERT_ID();

-- 社保税务
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name)
VALUES ('社保税务', 905538, 6, 'tax', NULL, 1, 0, 'M', '0', '0', '', 'shield', 'admin', sysdate(), '社保税务目录', 'HrTaxMgmt');
SET @hrTaxMgmtId = LAST_INSERT_ID();

-- 综合管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name)
VALUES ('综合管理', 905538, 7, 'extra', NULL, 1, 0, 'M', '0', '0', '', 'skill', 'admin', sysdate(), '综合管理目录', 'HrExtraMgmt');
SET @hrExtraMgmtId = LAST_INSERT_ID();


-- ========== 2. 将28个C菜单移到对应的二级目录下 ==========

-- --- 组织人事 ---
UPDATE sys_menu SET parent_id = @hrOrgId, order_num = 1 WHERE menu_id = 905539; -- 组织架构
UPDATE sys_menu SET parent_id = @hrOrgId, order_num = 2 WHERE menu_id = 905545; -- 岗位管理
UPDATE sys_menu SET parent_id = @hrOrgId, order_num = 3 WHERE menu_id = 905550; -- 员工档案

-- --- 入转调离 ---
UPDATE sys_menu SET parent_id = @hrLifecycleId, order_num = 1 WHERE menu_id = 905556; -- 入职管理
UPDATE sys_menu SET parent_id = @hrLifecycleId, order_num = 2 WHERE menu_id = 905561; -- 转正管理
UPDATE sys_menu SET parent_id = @hrLifecycleId, order_num = 3 WHERE menu_id = 905566; -- 调动管理
UPDATE sys_menu SET parent_id = @hrLifecycleId, order_num = 4 WHERE menu_id = 905571; -- 离职管理

-- --- 合同管理 ---
UPDATE sys_menu SET parent_id = @hrContractMgmtId, order_num = 1 WHERE menu_id = 905576; -- 劳动合同

-- --- 考勤管理 ---
UPDATE sys_menu SET parent_id = @hrAttendanceMgmtId, order_num = 1 WHERE menu_id = 905581; -- 班次管理
UPDATE sys_menu SET parent_id = @hrAttendanceMgmtId, order_num = 2 WHERE menu_id = 905586; -- 排班管理
UPDATE sys_menu SET parent_id = @hrAttendanceMgmtId, order_num = 3 WHERE menu_id = 905591; -- 考勤记录
UPDATE sys_menu SET parent_id = @hrAttendanceMgmtId, order_num = 4 WHERE menu_id = 905596; -- 请假管理
UPDATE sys_menu SET parent_id = @hrAttendanceMgmtId, order_num = 5 WHERE menu_id = 905601; -- 加班管理
UPDATE sys_menu SET parent_id = @hrAttendanceMgmtId, order_num = 6 WHERE menu_id = 905606; -- 考勤月报
UPDATE sys_menu SET parent_id = @hrAttendanceMgmtId, order_num = 7 WHERE menu_id = 905611; -- 假期余额

-- --- 薪资管理 ---
UPDATE sys_menu SET parent_id = @hrSalaryMgmtId, order_num = 1 WHERE menu_id = 905616; -- 薪资项字典
UPDATE sys_menu SET parent_id = @hrSalaryMgmtId, order_num = 2 WHERE menu_id = 905621; -- 定薪调薪
UPDATE sys_menu SET parent_id = @hrSalaryMgmtId, order_num = 3 WHERE menu_id = 905626; -- 工资条
UPDATE sys_menu SET parent_id = @hrSalaryMgmtId, order_num = 4 WHERE menu_id = 905631; -- 计件方案
UPDATE sys_menu SET parent_id = @hrSalaryMgmtId, order_num = 5 WHERE menu_id = 905636; -- 计件报工

-- --- 社保税务 ---
UPDATE sys_menu SET parent_id = @hrTaxMgmtId, order_num = 1 WHERE menu_id = 905641; -- 社保公积金
UPDATE sys_menu SET parent_id = @hrTaxMgmtId, order_num = 2 WHERE menu_id = 905646; -- 个税参数
UPDATE sys_menu SET parent_id = @hrTaxMgmtId, order_num = 3 WHERE menu_id = 905651; -- 专项附加扣除
UPDATE sys_menu SET parent_id = @hrTaxMgmtId, order_num = 4 WHERE menu_id = 905656; -- 薪酬发放

-- --- 综合管理 ---
UPDATE sys_menu SET parent_id = @hrExtraMgmtId, order_num = 1 WHERE menu_id = 905661; -- 证书台账
UPDATE sys_menu SET parent_id = @hrExtraMgmtId, order_num = 2 WHERE menu_id = 905666; -- 职业健康
UPDATE sys_menu SET parent_id = @hrExtraMgmtId, order_num = 3 WHERE menu_id = 905671; -- 劳务用工
UPDATE sys_menu SET parent_id = @hrExtraMgmtId, order_num = 4 WHERE menu_id = 905676; -- 临时工日结


-- ========== 3. 优化图标 ==========
-- 部分图标不够贴切，统一调整
UPDATE sys_menu SET icon = 'tree' WHERE menu_id = 905539;   -- 组织架构 -> tree
UPDATE sys_menu SET icon = 'post' WHERE menu_id = 905545;   -- 岗位管理 -> post
UPDATE sys_menu SET icon = 'user' WHERE menu_id = 905550;   -- 员工档案 -> user
UPDATE sys_menu SET icon = 'enter' WHERE menu_id = 905556;  -- 入职管理 -> enter
UPDATE sys_menu SET icon = 'checkbox' WHERE menu_id = 905561; -- 转正管理 -> checkbox
UPDATE sys_menu SET icon = 'swap' WHERE menu_id = 905566;   -- 调动管理 -> swap
UPDATE sys_menu SET icon = 'exit-fullscreen' WHERE menu_id = 905571; -- 离职管理 -> exit-fullscreen
UPDATE sys_menu SET icon = 'documentation' WHERE menu_id = 905576; -- 劳动合同 -> documentation
UPDATE sys_menu SET icon = 'time' WHERE menu_id = 905581;   -- 班次管理 -> time
UPDATE sys_menu SET icon = 'date' WHERE menu_id = 905586;   -- 排班管理 -> date
UPDATE sys_menu SET icon = 'monitor' WHERE menu_id = 905591; -- 考勤记录 -> monitor
UPDATE sys_menu SET icon = 'edit' WHERE menu_id = 905596;   -- 请假管理 -> edit
UPDATE sys_menu SET icon = 'time-range' WHERE menu_id = 905601; -- 加班管理 -> time-range
UPDATE sys_menu SET icon = 'chart' WHERE menu_id = 905606;   -- 考勤月报 -> chart
UPDATE sys_menu SET icon = 'list' WHERE menu_id = 905611;   -- 假期余额 -> list
UPDATE sys_menu SET icon = 'dict' WHERE menu_id = 905616;   -- 薪资项字典 -> dict
UPDATE sys_menu SET icon = 'money' WHERE menu_id = 905621;  -- 定薪调薪 -> money
UPDATE sys_menu SET icon = 'documentation' WHERE menu_id = 905626; -- 工资条 -> documentation
UPDATE sys_menu SET icon = 'component' WHERE menu_id = 905631; -- 计件方案 -> component
UPDATE sys_menu SET icon = 'form' WHERE menu_id = 905636;   -- 计件报工 -> form
UPDATE sys_menu SET icon = 'shield' WHERE menu_id = 905641; -- 社保公积金 -> shield
UPDATE sys_menu SET icon = 'rate' WHERE menu_id = 905646;   -- 个税参数 -> rate
UPDATE sys_menu SET icon = 'money' WHERE menu_id = 905651;  -- 专项附加扣除 -> money
UPDATE sys_menu SET icon = 'money' WHERE menu_id = 905656;  -- 薪酬发放 -> money
UPDATE sys_menu SET icon = 'skill' WHERE menu_id = 905661;  -- 证书台账 -> skill
UPDATE sys_menu SET icon = 'eye-open' WHERE menu_id = 905666; -- 职业健康 -> eye-open
UPDATE sys_menu SET icon = 'people' WHERE menu_id = 905671; -- 劳务用工 -> people
UPDATE sys_menu SET icon = 'money' WHERE menu_id = 905676;  -- 临时工日结 -> money


-- ========== 4. 验证结果 ==========
SELECT m1.menu_id, m1.menu_name, m1.parent_id, m1.order_num, m1.path, m1.component, m1.icon, m1.menu_type, 
       m2.menu_name as parent_name, m3.menu_name as grandparent_name
FROM sys_menu m1 
LEFT JOIN sys_menu m2 ON m1.parent_id = m2.menu_id
LEFT JOIN sys_menu m3 ON m2.parent_id = m3.menu_id
WHERE m1.menu_type = 'C' AND m1.component LIKE 'hr/%'
ORDER BY m1.parent_id, m1.order_num;
