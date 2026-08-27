-- =============================================
-- HR人力资源管理模块 - 菜单+字典+预置数据
-- =============================================
SET NAMES utf8mb4;

-- ========== 删除旧菜单 ==========
DELETE FROM sys_menu WHERE menu_name = '人力资源管理' AND parent_id = 0;

-- ========== 一级菜单 ==========
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('人力资源管理', 0, 15, 'hr', NULL, 1, 0, 'M', '0', '0', '', 'people', 'admin', sysdate(), '人力资源管理目录');
SET @hrMenuId = LAST_INSERT_ID();

-- ========== 子菜单（每个页面：C菜单 + 5个F按钮） ==========
-- 组织架构
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构', @hrMenuId, 1, 'organization', 'hr/organization/index', 1, 0, 'C', '0', '0', 'hr:organization:list', 'tree', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:remove', '#', 'admin', sysdate(), '');

-- 岗位管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理', @hrMenuId, 2, 'position', 'hr/position/index', 1, 0, 'C', '0', '0', 'hr:position:list', 'post', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:position:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:position:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:position:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:position:remove', '#', 'admin', sysdate(), '');

-- 员工档案
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案', @hrMenuId, 3, 'employee', 'hr/employee/index', 1, 0, 'C', '0', '0', 'hr:employee:list', 'user', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:remove', '#', 'admin', sysdate(), '');

-- 入职管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理', @hrMenuId, 4, 'entry', 'hr/entry/index', 1, 0, 'C', '0', '0', 'hr:entry:list', 'guide', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:remove', '#', 'admin', sysdate(), '');

-- 转正管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理', @hrMenuId, 5, 'regular', 'hr/regular/index', 1, 0, 'C', '0', '0', 'hr:regular:list', 'checkbox', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:remove', '#', 'admin', sysdate(), '');

-- 调动管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理', @hrMenuId, 6, 'transfer', 'hr/transfer/index', 1, 0, 'C', '0', '0', 'hr:transfer:list', 'swap', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:remove', '#', 'admin', sysdate(), '');

-- 离职管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理', @hrMenuId, 7, 'leave', 'hr/leave/index', 1, 0, 'C', '0', '0', 'hr:leave:list', 'exit-full', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:remove', '#', 'admin', sysdate(), '');

-- 劳动合同
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同', @hrMenuId, 8, 'contract', 'hr/contract/index', 1, 0, 'C', '0', '0', 'hr:contract:list', 'documentation', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('合同查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('合同新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('合同修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('合同删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:remove', '#', 'admin', sysdate(), '');

-- 班次管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理', @hrMenuId, 9, 'shift', 'hr/shift/index', 1, 0, 'C', '0', '0', 'hr:shift:list', 'time', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:remove', '#', 'admin', sysdate(), '');

-- 排班管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理', @hrMenuId, 10, 'schedule', 'hr/schedule/index', 1, 0, 'C', '0', '0', 'hr:schedule:list', 'date', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:remove', '#', 'admin', sysdate(), '');

-- 考勤记录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录', @hrMenuId, 11, 'attendance', 'hr/attendance/index', 1, 0, 'C', '0', '0', 'hr:attendance:list', 'monitor', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤修改', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:edit', '#', 'admin', sysdate(), '');

-- 请假管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理', @hrMenuId, 12, 'leaveRequest', 'hr/leaveRequest/index', 1, 0, 'C', '0', '0', 'hr:leaveRequest:list', 'edit', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:remove', '#', 'admin', sysdate(), '');

-- 加班管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理', @hrMenuId, 13, 'overtime', 'hr/overtime/index', 1, 0, 'C', '0', '0', 'hr:overtime:list', 'time-range', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:remove', '#', 'admin', sysdate(), '');

-- 考勤月报
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报', @hrMenuId, 14, 'monthly', 'hr/monthly/index', 1, 0, 'C', '0', '0', 'hr:monthly:list', 'chart', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('月报查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('月报封账', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:seal', '#', 'admin', sysdate(), '');

-- 薪资项字典
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典', @hrMenuId, 15, 'salaryItem', 'hr/salaryItem/index', 1, 0, 'C', '0', '0', 'hr:salaryItem:list', 'list', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:remove', '#', 'admin', sysdate(), '');

-- 定薪调薪
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪', @hrMenuId, 16, 'salaryPlan', 'hr/salaryPlan/index', 1, 0, 'C', '0', '0', 'hr:salaryPlan:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:remove', '#', 'admin', sysdate(), '');

-- 工资条
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条', @hrMenuId, 17, 'payslip', 'hr/payslip/index', 1, 0, 'C', '0', '0', 'hr:payslip:list', 'documentation', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条试算', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:calc', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条锁定', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:seal', '#', 'admin', sysdate(), '');

-- 计件管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件管理', @hrMenuId, 18, 'piecework', 'hr/piecework/index', 1, 0, 'C', '0', '0', 'hr:piecework:list', 'component', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件删除', @p, 4, '#', '', 1,