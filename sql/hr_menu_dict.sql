-- HR模块菜单(自动生成)
SET NAMES utf8mb4;

-- 删除旧菜单(先删子菜单再删父菜单)
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'hr:%');
DELETE FROM sys_menu WHERE perms LIKE 'hr:%';
DELETE FROM sys_menu WHERE menu_name = '人力资源管理' AND parent_id = 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('人力资源管理', 0, 15, 'hr', NULL, 1, 0, 'M', '0', '0', '', 'people', 'admin', sysdate(), '人力资源管理目录');
SET @hrMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构', @hrMenuId, 1, 'organization', 'hr/organization/index', 1, 0, 'C', '0', '0', 'hr:organization:list', 'tree', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:remove', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构导出', @p, 5, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:export', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理', @hrMenuId, 2, 'position', 'hr/position/index', 1, 0, 'C', '0', '0', 'hr:position:list', 'post', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:position:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:position:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:position:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:position:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案', @hrMenuId, 3, 'employee', 'hr/employee/index', 1, 0, 'C', '0', '0', 'hr:employee:list', 'user', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:remove', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案导出', @p, 5, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:export', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理', @hrMenuId, 4, 'entry', 'hr/entry/index', 1, 0, 'C', '0', '0', 'hr:entry:list', 'guide', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理', @hrMenuId, 5, 'regular', 'hr/regular/index', 1, 0, 'C', '0', '0', 'hr:regular:list', 'checkbox', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理', @hrMenuId, 6, 'transfer', 'hr/transfer/index', 1, 0, 'C', '0', '0', 'hr:transfer:list', 'swap', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理', @hrMenuId, 7, 'leave', 'hr/leave/index', 1, 0, 'C', '0', '0', 'hr:leave:list', 'exit-full', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同', @hrMenuId, 8, 'contract', 'hr/contract/index', 1, 0, 'C', '0', '0', 'hr:contract:list', 'documentation', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理', @hrMenuId, 9, 'shift', 'hr/shift/index', 1, 0, 'C', '0', '0', 'hr:shift:list', 'time', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理', @hrMenuId, 10, 'schedule', 'hr/schedule/index', 1, 0, 'C', '0', '0', 'hr:schedule:list', 'date', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录', @hrMenuId, 11, 'attendance', 'hr/attendance/index', 1, 0, 'C', '0', '0', 'hr:attendance:list', 'monitor', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理', @hrMenuId, 12, 'leaveRequest', 'hr/leaveRequest/index', 1, 0, 'C', '0', '0', 'hr:leaveRequest:list', 'edit', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理', @hrMenuId, 13, 'overtime', 'hr/overtime/index', 1, 0, 'C', '0', '0', 'hr:overtime:list', 'time-range', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报', @hrMenuId, 14, 'monthly', 'hr/monthly/index', 1, 0, 'C', '0', '0', 'hr:monthly:list', 'chart', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额', @hrMenuId, 15, 'leaveBalance', 'hr/leaveBalance/index', 1, 0, 'C', '0', '0', 'hr:leaveBalance:list', 'edit', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveBalance:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveBalance:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveBalance:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveBalance:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典', @hrMenuId, 16, 'salaryItem', 'hr/salaryItem/index', 1, 0, 'C', '0', '0', 'hr:salaryItem:list', 'list', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪', @hrMenuId, 17, 'salaryPlan', 'hr/salaryPlan/index', 1, 0, 'C', '0', '0', 'hr:salaryPlan:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条', @hrMenuId, 18, 'payslip', 'hr/payslip/index', 1, 0, 'C', '0', '0', 'hr:payslip:list', 'documentation', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案', @hrMenuId, 19, 'pieceworkPlan', 'hr/pieceworkPlan/index', 1, 0, 'C', '0', '0', 'hr:piecework:list', 'component', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工', @hrMenuId, 20, 'pieceworkRecord', 'hr/pieceworkRecord/index', 1, 0, 'C', '0', '0', 'hr:piecework:list', 'component', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金', @hrMenuId, 21, 'socialSecurity', 'hr/socialSecurity/index', 1, 0, 'C', '0', '0', 'hr:socialSecurity:list', 'security', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数', @hrMenuId, 22, 'taxRate', 'hr/taxRate/index', 1, 0, 'C', '0', '0', 'hr:taxRate:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:taxRate:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:taxRate:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:taxRate:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:taxRate:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除', @hrMenuId, 23, 'specialDeduction', 'hr/specialDeduction/index', 1, 0, 'C', '0', '0', 'hr:specialDeduction:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:specialDeduction:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:specialDeduction:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:specialDeduction:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:specialDeduction:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放', @hrMenuId, 24, 'payroll', 'hr/payroll/index', 1, 0, 'C', '0', '0', 'hr:payroll:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账', @hrMenuId, 25, 'certificate', 'hr/certificate/index', 1, 0, 'C', '0', '0', 'hr:certificate:list', 'skill', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:certificate:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:certificate:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:certificate:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:certificate:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康', @hrMenuId, 26, 'healthCheck', 'hr/healthCheck/index', 1, 0, 'C', '0', '0', 'hr:healthCheck:list', 'chart', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:healthCheck:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:healthCheck:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:healthCheck:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:healthCheck:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工', @hrMenuId, 27, 'laborWorker', 'hr/laborWorker/index', 1, 0, 'C', '0', '0', 'hr:laborWorker:list', 'people', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:laborWorker:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:laborWorker:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:laborWorker:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:laborWorker:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结', @hrMenuId, 28, 'tempSettlement', 'hr/tempSettlement/index', 1, 0, 'C', '0', '0', 'hr:tempSettlement:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:tempSettlement:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:tempSettlement:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:tempSettlement:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:tempSettlement:remove', '#', 'admin', sysdate(), '');


-- ========== HR字典数据 ==========

-- 性别
DELETE FROM sys_dict_type WHERE dict_type = 'hr_gender';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('性别', 'hr_gender', '0', 'admin', sysdate(), '性别');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '男', '0', 'hr_gender', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '女', '1', 'hr_gender', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '未知', '2', 'hr_gender', '0', 'admin', sysdate(), '');

-- 婚姻状况
DELETE FROM sys_dict_type WHERE dict_type = 'hr_marital_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('婚姻状况', 'hr_marital_status', '0', 'admin', sysdate(), '婚姻状况');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '未婚', '0', 'hr_marital_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '已婚', '1', 'hr_marital_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '离异', '2', 'hr_marital_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '丧偶', '3', 'hr_marital_status', '0', 'admin', sysdate(), '');

-- 员工状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_employee_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('员工状态', 'hr_employee_status', '0', 'admin', sysdate(), '员工状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '在职', '0', 'hr_employee_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '试用期', '1', 'hr_employee_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '离职', '2', 'hr_employee_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '黑名单', '3', 'hr_employee_status', '0', 'admin', sysdate(), '');

-- 用工类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_employee_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('用工类型', 'hr_employee_type', '0', 'admin', sysdate(), '用工类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '正式', '0', 'hr_employee_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '劳务派遣', '1', 'hr_employee_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '临时工', '2', 'hr_employee_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '实习生', '3', 'hr_employee_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '退休返聘', '4', 'hr_employee_type', '0', 'admin', sysdate(), '');

-- 工时制度
DELETE FROM sys_dict_type WHERE dict_type = 'hr_work_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('工时制度', 'hr_work_type', '0', 'admin', sysdate(), '工时制度');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '标准工时', '0', 'hr_work_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '综合工时', '1', 'hr_work_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '不定时', '2', 'hr_work_type', '0', 'admin', sysdate(), '');

-- 组织层级
DELETE FROM sys_dict_type WHERE dict_type = 'hr_org_level';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('组织层级', 'hr_org_level', '0', 'admin', sysdate(), '组织层级');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '公司', '1', 'hr_org_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '分厂', '2', 'hr_org_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '部门', '3', 'hr_org_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '班组', '4', 'hr_org_level', '0', 'admin', sysdate(), '');

-- 岗位序列
DELETE FROM sys_dict_type WHERE dict_type = 'hr_position_seq';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('岗位序列', 'hr_position_seq', '0', 'admin', sysdate(), '岗位序列');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '管理', 'M', 'hr_position_seq', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '技术', 'P', 'hr_position_seq', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '技能', 'W', 'hr_position_seq', '0', 'admin', sysdate(), '');

-- 学历
DELETE FROM sys_dict_type WHERE dict_type = 'hr_edu_level';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('学历', 'hr_edu_level', '0', 'admin', sysdate(), '学历');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '博士', '1', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '硕士', '2', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '本科', '3', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '大专', '4', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '高中', '5', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (6, '中专', '6', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (7, '初中', '7', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (8, '其他', '8', 'hr_edu_level', '0', 'admin', sysdate(), '');

-- 合同类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_contract_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('合同类型', 'hr_contract_type', '0', 'admin', sysdate(), '合同类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '固定期限', '0', 'hr_contract_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '无固定期限', '1', 'hr_contract_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '以完成一定工作', '2', 'hr_contract_type', '0', 'admin', sysdate(), '');

-- 审批状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_audit_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('审批状态', 'hr_audit_status', '0', 'admin', sysdate(), '审批状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '待审', '0', 'hr_audit_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '通过', '1', 'hr_audit_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '驳回', '2', 'hr_audit_status', '0', 'admin', sysdate(), '');

-- 考勤状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_attendance_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('考勤状态', 'hr_attendance_status', '0', 'admin', sysdate(), '考勤状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '正常', '0', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '迟到', '1', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '早退', '2', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '旷工', '3', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '漏卡', '4', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (6, '请假', '5', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (7, '出差', '6', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (8, '加班', '7', 'hr_attendance_status', '0', 'admin', sysdate(), '');

-- 假别
DELETE FROM sys_dict_type WHERE dict_type = 'hr_leave_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('假别', 'hr_leave_type', '0', 'admin', sysdate(), '假别');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '事假', '1', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '病假', '2', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '年假', '3', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '婚假', '4', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '产假', '5', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (6, '陪产假', '6', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (7, '丧假', '7', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (8, '工伤假', '8', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (9, '调休', '9', 'hr_leave_type', '0', 'admin', sysdate(), '');

-- 加班类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_overtime_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('加班类型', 'hr_overtime_type', '0', 'admin', sysdate(), '加班类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '工作日', '0', 'hr_overtime_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '休息日', '1', 'hr_overtime_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '法定节假日', '2', 'hr_overtime_type', '0', 'admin', sysdate(), '');

-- 薪资项类别
DELETE FROM sys_dict_type WHERE dict_type = 'hr_salary_item_category';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('薪资项类别', 'hr_salary_item_category', '0', 'admin', sysdate(), '薪资项类别');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '固定项', '0', 'hr_salary_item_category', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '考勤联动', '1', 'hr_salary_item_category', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '计件项', '2', 'hr_salary_item_category', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '手工项', '3', 'hr_salary_item_category', '0', 'admin', sysdate(), '');

-- 支付状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_pay_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('支付状态', 'hr_pay_status', '0', 'admin', sysdate(), '支付状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '未付', '0', 'hr_pay_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '已付', '1', 'hr_pay_status', '0', 'admin', sysdate(), '');

-- 证书类别
DELETE FROM sys_dict_type WHERE dict_type = 'hr_cert_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('证书类别', 'hr_cert_type', '0', 'admin', sysdate(), '证书类别');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '特种作业证', '0', 'hr_cert_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '健康证', '1', 'hr_cert_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '职业资格证', '2', 'hr_cert_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '特种设备证', '3', 'hr_cert_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '其他', '4', 'hr_cert_type', '0', 'admin', sysdate(), '');

-- 预警状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_remind_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('预警状态', 'hr_remind_status', '0', 'admin', sysdate(), '预警状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '正常', '0', 'hr_remind_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '临期', '1', 'hr_remind_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '过期', '2', 'hr_remind_status', '0', 'admin', sysdate(), '');

-- 体检类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_health_check_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('体检类型', 'hr_health_check_type', '0', 'admin', sysdate(), '体检类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '岗前', '0', 'hr_health_check_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '在岗', '1', 'hr_health_check_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '离岗', '2', 'hr_health_check_type', '0', 'admin', sysdate(), '');

-- 体检结论
DELETE FROM sys_dict_type WHERE dict_type = 'hr_health_check_result';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('体检结论', 'hr_health_check_result', '0', 'admin', sysdate(), '体检结论');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '合格', '0', 'hr_health_check_result', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '不合格', '1', 'hr_health_check_result', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '疑似职业病', '2', 'hr_health_check_result', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '其他', '3', 'hr_health_check_result', '0', 'admin', sysdate(), '');

-- 用工形态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_labor_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('用工形态', 'hr_labor_type', '0', 'admin', sysdate(), '用工形态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '劳务派遣', '0', 'hr_labor_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '季节临时工', '1', 'hr_labor_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '实习生', '2', 'hr_labor_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '退休返聘', '3', 'hr_labor_type', '0', 'admin', sysdate(), '');

-- 封账状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_sealed_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('封账状态', 'hr_sealed_status', '0', 'admin', sysdate(), '封账状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '未封', '0', 'hr_sealed_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '已封', '1', 'hr_sealed_status', '0', 'admin', sysdate(), '');

-- 经济补偿
DELETE FROM sys_dict_type WHERE dict_type = 'hr_compensation_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('经济补偿', 'hr_compensation_type', '0', 'admin', sysdate(), '经济补偿');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, 'N', '0', 'hr_compensation_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, 'N+1', '1', 'hr_compensation_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '2N', '2', 'hr_compensation_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '无', '3', 'hr_compensation_type', '0', 'admin', sysdate(), '');

-- 调动类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_transfer_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('调动类型', 'hr_transfer_type', '0', 'admin', sysdate(), '调动类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '部门', '1', 'hr_transfer_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '岗位', '2', 'hr_transfer_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '薪酬', '3', 'hr_transfer_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '班制', '4', 'hr_transfer_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '地点', '5', 'hr_transfer_type', '0', 'admin', sysdate(), '');

-- 离职类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_leave_emp_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('离职类型', 'hr_leave_emp_type', '0', 'admin', sysdate(), '离职类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '主动辞职', '0', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '协商解除', '1', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '违纪辞退', '2', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '合同到期', '3', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '退休', '4', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');

-- 参保状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_social_security_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('参保状态', 'hr_social_security_status', '0', 'admin', sysdate(), '参保状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '在缴', '0', 'hr_social_security_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '停缴', '1', 'hr_social_security_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '未参保', '2', 'hr_social_security_status', '0', 'admin', sysdate(), '');

-- ========== 预置班次数据 ==========
INSERT INTO biz_hr_shift (shift_code, shift_name, start_time, end_time, cross_midnight, work_hours, rest_hours, meal_minutes, night_shift, night_allowance, applicable_to, order_num, status, create_by, create_time) VALUES
('S001','长白班','08:00','17:00',0,8.0,1.0,60,0,0,'职能/常白班产线',1,'0','admin',sysdate()),
('S002','两班倒白','08:00','20:00',0,12.0,1.0,60,0,0,'食品加工旺季',2,'0','admin',sysdate()),
('S003','两班倒夜','20:00','08:00',1,12.0,1.0,60,1,20,'食品加工旺季',3,'0','admin',sysdate()),
('S004','三班早','08:00','16:00',0,8.0,0.5,30,0,0,'半连续产线',4,'0','admin',sysdate()),
('S005','三班中','16:00','24:00',0,8.0,0.5,30,0,0,'半连续产线',5,'0','admin',sysdate()),
('S006','三班夜','00:00','08:00',0,8.0,0.5,30,1,20,'半连续产线',6,'0','admin',sysdate());

-- ========== 预置薪资项 ==========
INSERT INTO biz_hr_salary_item (item_code, item_name, item_category, calc_rule, is_add, order_num, status, create_by, create_time) VALUES
('basic_salary','基本工资','0','定薪单确定',1,1,'0','admin',sysdate()),
('position_salary','岗位工资','0','按岗位-岗级标准表自动带出',1,2,'0','admin',sysdate()),
('skill_allowance','技能等级津贴','0','按员工技能等级查表',1,3,'0','admin',sysdate()),
('seniority_pay','工龄工资','0','按司龄阶梯计算',1,4,'0','admin',sysdate()),
('night_allowance','夜班津贴','1','当月夜班次数×每次标准',1,5,'0','admin',sysdate()),
('high_temp_allowance','高温津贴','1','高温月份高温岗位出勤天×日标准',1,6,'0','admin',sysdate()),
('hazard_allowance','有毒有害津贴','1','岗位标记职业危害等级对应档位',1,7,'0','admin',sysdate()),
('full_attendance','全勤奖','1','当月无迟到早退请假旷工',1,8,'0','admin',sysdate()),
('meal_allowance','餐补','1','出勤天×日标准',1,9,'0','admin',sysdate()),
('piecework_pay','计件工资','2','合格品数量×计件单价',1,10,'0','admin',sysdate()),
('overtime_pay','加班费','1','见计算规则',1,11,'0','admin',sysdate()),
('performance_bonus','绩效奖金','3','本期手工录入',1,12,'0','admin',sysdate()),
('absence_deduction','缺勤扣款','1','事假/病假按日工资扣减',0,13,'0','admin',sysdate()),
('social_insurance','社保公积金个人','1','按基数×个人比例',0,14,'0','admin',sysdate()),
('income_tax','个人所得税','1','累计预扣预缴',0,15,'0','admin',sysdate()),
('other_add','其他加项','3','逐月手工录入',1,16,'0','admin',sysdate()),
('other_deduct','其他扣项','3','逐月手工录入',0,17,'0','admin',sysdate());

-- ========== 预置个税税率表 ==========
INSERT INTO biz_hr_tax_rate (level, min_amount, max_amount, rate, quick_deduction, effective_date, status, create_by, create_time) VALUES
(1, 0, 36000, 0.03, 0, '2026-01-01', '0', 'admin', sysdate()),
(2, 36000, 144000, 0.10, 2520, '2026-01-01', '0', 'admin', sysdate()),
(3, 144000, 300000, 0.20, 16920, '2026-01-01', '0', 'admin', sysdate()),
(4, 300000, 420000, 0.25, 31920, '2026-01-01', '0', 'admin', sysdate()),
(5, 420000, 660000, 0.30, 52920, '2026-01-01', '0', 'admin', sysdate()),
(6, 660000, 960000, 0.35, 85920, '2026-01-01', '0', 'admin', sysdate()),
(7, 960000, NULL, 0.45, 181920, '2026-01-01', '0', 'admin', sysdate());

-- ========== 预置组织架构 ==========
INSERT INTO biz_hr_organization (org_code, org_name, parent_id, order_num, org_level, cost_center, attendance_org, status, create_by, create_time) VALUES
('ORG001','总公司',0,1,1,1,1,'0','admin',sysdate()),
('ORG002','一分厂',1,1,2,1,1,'0','admin',sysdate()),
('ORG003','二分厂',1,2,2,1,1,'0','admin',sysdate()),
('ORG004','综合管理部',1,3,3,1,0,'0','admin',sysdate()),
('ORG005','生产一车间',2,1,3,1,1,'0','admin',sysdate()),
('ORG006','生产二车间',3,1,3,1,1,'0','admin',sysdate());

-- ========== 给admin角色分配HR菜单权限 ==========
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'hr:%';
