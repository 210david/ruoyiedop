-- 教育培训菜单权限补全

-- 获取教育培训目录ID
SELECT @trainingMenuId := menu_id FROM sys_menu WHERE menu_name = '教育培训' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- ---- 课程管理 ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('课程管理', @trainingMenuId, 1, 'course', 'safety/training/course/index', 1, 0, 'C', '0', '0', 'safety:training:course:list', 'education', 'admin', NOW(), '课程管理菜单');
SET @courseMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('课程查询', @courseMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:query', '#', 'admin', NOW(), ''),
('课程新增', @courseMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:add', '#', 'admin', NOW(), ''),
('课程修改', @courseMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:edit', '#', 'admin', NOW(), ''),
('课程删除', @courseMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:remove', '#', 'admin', NOW(), ''),
('课程导出', @courseMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:training:course:export', '#', 'admin', NOW(), '');

-- 调整培训计划排序（原order=1改为2）
UPDATE sys_menu SET order_num = 2 WHERE menu_name = '培训计划' AND parent_id = @trainingMenuId;

-- 调整培训记录排序（原order=2改为3）
UPDATE sys_menu SET order_num = 3 WHERE menu_name = '培训记录' AND parent_id = @trainingMenuId;

-- ---- 参训人员/成绩 ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('参训人员', @trainingMenuId, 4, 'attendee', 'safety/training/attendee/index', 1, 0, 'C', '0', '0', 'safety:training:attendee:list', 'people', 'admin', NOW(), '参训人员及成绩菜单');
SET @attendeeMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('参训人员查询', @attendeeMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:query', '#', 'admin', NOW(), ''),
('参训人员新增', @attendeeMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:add', '#', 'admin', NOW(), ''),
('参训人员修改', @attendeeMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:edit', '#', 'admin', NOW(), ''),
('参训人员删除', @attendeeMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:remove', '#', 'admin', NOW(), ''),
('参训人员导出', @attendeeMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:training:attendee:export', '#', 'admin', NOW(), '');

-- ---- 证书管理 ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('证书管理', @trainingMenuId, 5, 'cert', 'safety/training/cert/index', 1, 0, 'C', '0', '0', 'safety:training:cert:list', 'documentation', 'admin', NOW(), '培训证书菜单');
SET @certMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('证书查询', @certMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:query', '#', 'admin', NOW(), ''),
('证书新增', @certMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:add', '#', 'admin', NOW(), ''),
('证书修改', @certMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:edit', '#', 'admin', NOW(), ''),
('证书删除', @certMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:remove', '#', 'admin', NOW(), ''),
('证书导出', @certMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:training:cert:export', '#', 'admin', NOW(), '');

-- ---- 学时统计 ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('学时统计', @trainingMenuId, 6, 'hours', 'safety/training/hours/index', 1, 0, 'C', '0', '0', 'safety:training:hours:list', 'chart', 'admin', NOW(), '学时统计菜单');

-- 将新增菜单分配给管理员角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'safety:training:course:%'
OR perms LIKE 'safety:training:attendee:%'
OR perms LIKE 'safety:training:cert:%'
OR perms LIKE 'safety:training:hours:%';
