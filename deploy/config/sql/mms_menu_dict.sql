-- MMS Production Control Module - Menu & Dictionary SQL
USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. 主目录菜单：生产管控
-- ============================================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5000, '生产管控', 0, 20, 'mms', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate(), '生产管控目录');

-- ============================================================
-- 2. 子菜单（菜单类型 C）
-- ============================================================
-- 2.1 看板
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5001, '生产看板', 5000, 1, 'dashboard', 'mms/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'mms:dashboard:list', 'monitor', 'admin', sysdate(), '');

-- 2.2 BOM管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5010, 'BOM管理', 5000, 2, 'bom', 'mms/bom/index', '', '', 1, 0, 'C', '0', '0', 'mms:bom:list', 'tree-table', 'admin', sysdate(), '');

-- 2.3 工艺路线
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5020, '工艺路线', 5000, 3, 'route', 'mms/route/index', '', '', 1, 0, 'C', '0', '0', 'mms:route:list', 'tree-table', 'admin', sysdate(), '');

-- 2.4 工序管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5030, '工序管理', 5000, 4, 'process', 'mms/process/index', '', '', 1, 0, 'C', '0', '0', 'mms:process:list', 'cascader', 'admin', sysdate(), '');

-- 2.5 资源管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5040, '资源管理', 5000, 5, 'resource', 'mms/resource/index', '', '', 1, 0, 'C', '0', '0', 'mms:resource:list', 'build', 'admin', sysdate(), '');

-- 2.6 班次管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5050, '班次管理', 5000, 6, 'shift', 'mms/shift/index', '', '', 1, 0, 'C', '0', '0', 'mms:shift:list', 'time-range', 'admin', sysdate(), '');

-- 2.7 需求管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5060, '需求管理', 5000, 7, 'demand', 'mms/demand/index', '', '', 1, 0, 'C', '0', '0', 'mms:demand:list', 'documentation', 'admin', sysdate(), '');

-- 2.8 主生产计划
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5070, '主生产计划', 5000, 8, 'mps', 'mms/mps/index', '', '', 1, 0, 'C', '0', '0', 'mms:mps:list', 'plan', 'admin', sysdate(), '');

-- 2.9 齐套管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5080, '齐套管理', 5000, 9, 'kit', 'mms/kit/index', '', '', 1, 0, 'C', '0', '0', 'mms:kit:list', 'checkbox', 'admin', sysdate(), '');

-- 2.10 工单管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5090, '工单管理', 5000, 10, 'workorder', 'mms/workorder/index', '', '', 1, 0, 'C', '0', '0', 'mms:workorder:list', 'form', 'admin', sysdate(), '');

-- 2.11 领料管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5100, '领料管理', 5000, 11, 'issue', 'mms/issue/index', '', '', 1, 0, 'C', '0', '0', 'mms:issue:list', 'shopping', 'admin', sysdate(), '');

-- 2.12 退料管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5110, '退料管理', 5000, 12, 'return', 'mms/return/index', '', '', 1, 0, 'C', '0', '0', 'mms:return:list', 'rollback', 'admin', sysdate(), '');

-- 2.13 报工管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5120, '报工管理', 5000, 13, 'report', 'mms/report/index', '', '', 1, 0, 'C', '0', '0', 'mms:report:list', 'edit', 'admin', sysdate(), '');

-- 2.14 质量检验
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5130, '质量检验', 5000, 14, 'qc', 'mms/qc/index', '', '', 1, 0, 'C', '0', '0', 'mms:qc:list', 'validCode', 'admin', sysdate(), '');

-- 2.15 异常管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5140, '异常管理', 5000, 15, 'abnormal', 'mms/abnormal/index', '', '', 1, 0, 'C', '0', '0', 'mms:abnormal:list', 'bug', 'admin', sysdate(), '');

-- 2.16 完工入库
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (5150, '完工入库', 5000, 16, 'finish', 'mms/finish/index', '', '', 1, 0, 'C', '0', '0', 'mms:finish:list', 'checkbox', 'admin', sysdate(), '');

-- ============================================================
-- 3. 按钮权限（菜单类型 F）
-- ============================================================

-- BOM 按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5011, 'BOM查询', 5010, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:query', '#', 'admin', sysdate(), ''),
(5012, 'BOM新增', 5010, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:add', '#', 'admin', sysdate(), ''),
(5013, 'BOM修改', 5010, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:edit', '#', 'admin', sysdate(), ''),
(5014, 'BOM删除', 5010, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:remove', '#', 'admin', sysdate(), ''),
(5015, 'BOM导出', 5010, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:export', '#', 'admin', sysdate(), '');

-- 工艺路线按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5021, '路线查询', 5020, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:route:query', '#', 'admin', sysdate(), ''),
(5022, '路线新增', 5020, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:route:add', '#', 'admin', sysdate(), ''),
(5023, '路线修改', 5020, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:route:edit', '#', 'admin', sysdate(), ''),
(5024, '路线删除', 5020, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:route:remove', '#', 'admin', sysdate(), ''),
(5025, '路线导出', 5020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:route:export', '#', 'admin', sysdate(), '');

-- 工序按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5031, '工序查询', 5030, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:process:query', '#', 'admin', sysdate(), ''),
(5032, '工序新增', 5030, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:process:add', '#', 'admin', sysdate(), ''),
(5033, '工序修改', 5030, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:process:edit', '#', 'admin', sysdate(), ''),
(5034, '工序删除', 5030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:process:remove', '#', 'admin', sysdate(), ''),
(5035, '工序导出', 5030, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:process:export', '#', 'admin', sysdate(), '');

-- 资源按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5041, '资源查询', 5040, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:query', '#', 'admin', sysdate(), ''),
(5042, '资源新增', 5040, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:add', '#', 'admin', sysdate(), ''),
(5043, '资源修改', 5040, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:edit', '#', 'admin', sysdate(), ''),
(5044, '资源删除', 5040, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:remove', '#', 'admin', sysdate(), ''),
(5045, '资源导出', 5040, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:resource:export', '#', 'admin', sysdate(), '');

-- 班次按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5051, '班次查询', 5050, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:query', '#', 'admin', sysdate(), ''),
(5052, '班次新增', 5050, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:add', '#', 'admin', sysdate(), ''),
(5053, '班次修改', 5050, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:edit', '#', 'admin', sysdate(), ''),
(5054, '班次删除', 5050, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:remove', '#', 'admin', sysdate(), ''),
(5055, '班次导出', 5050, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:shift:export', '#', 'admin', sysdate(), '');

-- 需求按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5061, '需求查询', 5060, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:query', '#', 'admin', sysdate(), ''),
(5062, '需求新增', 5060, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:add', '#', 'admin', sysdate(), ''),
(5063, '需求修改', 5060, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:edit', '#', 'admin', sysdate(), ''),
(5064, '需求删除', 5060, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:remove', '#', 'admin', sysdate(), ''),
(5065, '需求导出', 5060, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:export', '#', 'admin', sysdate(), '');

-- MPS按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5071, '计划查询', 5070, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:query', '#', 'admin', sysdate(), ''),
(5072, '计划新增', 5070, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:add', '#', 'admin', sysdate(), ''),
(5073, '计划修改', 5070, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:edit', '#', 'admin', sysdate(), ''),
(5074, '计划删除', 5070, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:remove', '#', 'admin', sysdate(), ''),
(5075, '计划导出', 5070, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:mps:export', '#', 'admin', sysdate(), '');

-- 齐套按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5081, '齐套查询', 5080, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:query', '#', 'admin', sysdate(), ''),
(5082, '齐套新增', 5080, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:add', '#', 'admin', sysdate(), ''),
(5083, '齐套修改', 5080, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:edit', '#', 'admin', sysdate(), ''),
(5084, '齐套删除', 5080, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:remove', '#', 'admin', sysdate(), ''),
(5085, '齐套导出', 5080, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:kit:export', '#', 'admin', sysdate(), '');

-- 工单按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5091, '工单查询', 5090, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:query', '#', 'admin', sysdate(), ''),
(5092, '工单新增', 5090, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:add', '#', 'admin', sysdate(), ''),
(5093, '工单修改', 5090, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:edit', '#', 'admin', sysdate(), ''),
(5094, '工单删除', 5090, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:remove', '#', 'admin', sysdate(), ''),
(5095, '工单导出', 5090, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:workorder:export', '#', 'admin', sysdate(), '');

-- 领料按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5101, '领料查询', 5100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:query', '#', 'admin', sysdate(), ''),
(5102, '领料新增', 5100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:add', '#', 'admin', sysdate(), ''),
(5103, '领料修改', 5100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:edit', '#', 'admin', sysdate(), ''),
(5104, '领料删除', 5100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:remove', '#', 'admin', sysdate(), ''),
(5105, '领料导出', 5100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:issue:export', '#', 'admin', sysdate(), '');

-- 退料按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5111, '退料查询', 5110, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:return:query', '#', 'admin', sysdate(), ''),
(5112, '退料新增', 5110, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:return:add', '#', 'admin', sysdate(), ''),
(5113, '退料修改', 5110, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:return:edit', '#', 'admin', sysdate(), ''),
(5114, '退料删除', 5110, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:return:remove', '#', 'admin', sysdate(), ''),
(5115, '退料导出', 5110, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:return:export', '#', 'admin', sysdate(), '');

-- 报工按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5121, '报工查询', 5120, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:report:query', '#', 'admin', sysdate(), ''),
(5122, '报工新增', 5120, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:report:add', '#', 'admin', sysdate(), ''),
(5123, '报工修改', 5120, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:report:edit', '#', 'admin', sysdate(), ''),
(5124, '报工删除', 5120, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:report:remove', '#', 'admin', sysdate(), ''),
(5125, '报工导出', 5120, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:report:export', '#', 'admin', sysdate(), '');

-- 质检按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5131, '质检查询', 5130, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:query', '#', 'admin', sysdate(), ''),
(5132, '质检新增', 5130, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:add', '#', 'admin', sysdate(), ''),
(5133, '质检修改', 5130, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:edit', '#', 'admin', sysdate(), ''),
(5134, '质检删除', 5130, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:remove', '#', 'admin', sysdate(), ''),
(5135, '质检导出', 5130, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:qc:export', '#', 'admin', sysdate(), '');

-- 异常按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5141, '异常查询', 5140, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:query', '#', 'admin', sysdate(), ''),
(5142, '异常新增', 5140, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:add', '#', 'admin', sysdate(), ''),
(5143, '异常修改', 5140, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:edit', '#', 'admin', sysdate(), ''),
(5144, '异常删除', 5140, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:remove', '#', 'admin', sysdate(), ''),
(5145, '异常导出', 5140, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:export', '#', 'admin', sysdate(), '');

-- 完工入库按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(5151, '入库查询', 5150, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:query', '#', 'admin', sysdate(), ''),
(5152, '入库新增', 5150, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:add', '#', 'admin', sysdate(), ''),
(5153, '入库修改', 5150, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:edit', '#', 'admin', sysdate(), ''),
(5154, '入库删除', 5150, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:remove', '#', 'admin', sysdate(), ''),
(5155, '入库导出', 5150, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:finish:export', '#', 'admin', sysdate(), '');

-- ============================================================
-- 4. 角色菜单分配（admin 角色 = 1）
-- ============================================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 5000), (1, 5001),
(1, 5010), (1, 5011), (1, 5012), (1, 5013), (1, 5014), (1, 5015),
(1, 5020), (1, 5021), (1, 5022), (1, 5023), (1, 5024), (1, 5025),
(1, 5030), (1, 5031), (1, 5032), (1, 5033), (1, 5034), (1, 5035),
(1, 5040), (1, 5041), (1, 5042), (1, 5043), (1, 5044), (1, 5045),
(1, 5050), (1, 5051), (1, 5052), (1, 5053), (1, 5054), (1, 5055),
(1, 5060), (1, 5061), (1, 5062), (1, 5063), (1, 5064), (1, 5065),
(1, 5070), (1, 5071), (1, 5072), (1, 5073), (1, 5074), (1, 5075),
(1, 5080), (1, 5081), (1, 5082), (1, 5083), (1, 5084), (1, 5085),
(1, 5090), (1, 5091), (1, 5092), (1, 5093), (1, 5094), (1, 5095),
(1, 5100), (1, 5101), (1, 5102), (1, 5103), (1, 5104), (1, 5105),
(1, 5110), (1, 5111), (1, 5112), (1, 5113), (1, 5114), (1, 5115),
(1, 5120), (1, 5121), (1, 5122), (1, 5123), (1, 5124), (1, 5125),
(1, 5130), (1, 5131), (1, 5132), (1, 5133), (1, 5134), (1, 5135),
(1, 5140), (1, 5141), (1, 5142), (1, 5143), (1, 5144), (1, 5145),
(1, 5150), (1, 5151), (1, 5152), (1, 5153), (1, 5154), (1, 5155);

-- ============================================================
-- 5. 字典数据
-- ============================================================

-- BOM类型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, 'BOM类型', 'mms_bom_type', '0', 'admin', sysdate(), 'BOM类型列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '标准BOM', '0', 'mms_bom_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '替代BOM', '1', 'mms_bom_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(3, '配方', '2', 'mms_bom_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '临时BOM', '3', 'mms_bom_type', '', 'warning', 'N', '0', 'admin', sysdate(), '');

-- 通用状态字典（MMS专用）
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, 'MMS状态', 'mms_status', '0', 'admin', sysdate(), 'MMS通用状态');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '启用', '0', 'mms_status', '', 'success', 'Y', '0', 'admin', sysdate(), ''),
(2, '停用', '1', 'mms_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');

-- 需求类型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '需求类型', 'mms_demand_type', '0', 'admin', sysdate(), '需求类型列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '销售订单', '0', 'mms_demand_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '安全库存', '1', 'mms_demand_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(3, '生产备货', '2', 'mms_demand_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '其他', '3', 'mms_demand_type', '', 'warning', 'N', '0', 'admin', sysdate(), '');

-- 优先级字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '优先级', 'mms_priority', '0', 'admin', sysdate(), '优先级列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '高', '0', 'mms_priority', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(2, '中', '1', 'mms_priority', '', 'warning', 'Y', '0', 'admin', sysdate(), ''),
(3, '低', '2', 'mms_priority', '', 'info', 'N', '0', 'admin', sysdate(), '');

-- 工单状态字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '工单状态', 'mms_wo_status', '0', 'admin', sysdate(), '工单状态列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '已创建', '0', 'mms_wo_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已审核', '1', 'mms_wo_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '已派发', '2', 'mms_wo_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '执行中', '3', 'mms_wo_status', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(5, '已暂停', '4', 'mms_wo_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(6, '已完工', '5', 'mms_wo_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(7, '已关闭', '6', 'mms_wo_status', '', 'info', 'N', '0', 'admin', sysdate(), '');

-- 报工审核状态字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '报工状态', 'mms_report_status', '0', 'admin', sysdate(), '报工状态列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待审核', '0', 'mms_report_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已审核', '1', 'mms_report_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '已驳回', '2', 'mms_report_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');

-- 质检类型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '质检类型', 'mms_qc_type', '0', 'admin', sysdate(), '质检类型列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '首检', '0', 'mms_qc_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '过程检', '1', 'mms_qc_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '完工检', '2', 'mms_qc_type', '', 'warning', 'N', '0', 'admin', sysdate(), '');

-- 质检结果字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '质检结果', 'mms_qc_result', '0', 'admin', sysdate(), '质检结果列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '合格', '0', 'mms_qc_result', '', 'success', 'Y', '0', 'admin', sysdate(), ''),
(2, '不合格', '1', 'mms_qc_result', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(3, '让步接收', '2', 'mms_qc_result', '', 'warning', 'N', '0', 'admin', sysdate(), '');

-- 异常类型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '异常类型', 'mms_abnormal_type', '0', 'admin', sysdate(), '异常类型列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '设备故障', '0', 'mms_abnormal_type', '', 'danger', 'Y', '0', 'admin', sysdate(), ''),
(2, '质量异常', '1', 'mms_abnormal_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '物料短缺', '2', 'mms_abnormal_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '人员缺岗', '3', 'mms_abnormal_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(5, '工艺异常', '4', 'mms_abnormal_type', '', 'danger', 'N', '0', 'admin', sysdate(), ''),
(6, '其他', '5', 'mms_abnormal_type', '', 'info', 'N', '0', 'admin', sysdate(), '');

-- 异常严重程度字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '异常严重度', 'mms_severity', '0', 'admin', sysdate(), '异常严重程度列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '一般', '0', 'mms_severity', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '严重', '1', 'mms_severity', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '紧急', '2', 'mms_severity', '', 'danger', 'N', '0', 'admin', sysdate(), '');

-- 资源类型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '资源类型', 'mms_resource_type', '0', 'admin', sysdate(), '资源类型列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '设备', '0', 'mms_resource_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '产线', '1', 'mms_resource_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '工作中心', '2', 'mms_resource_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '工位', '3', 'mms_resource_type', '', 'warning', 'N', '0', 'admin', sysdate(), '');

-- 工序类型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '工序类型', 'mms_process_type', '0', 'admin', sysdate(), '工序类型列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '加工', '0', 'mms_process_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '装配', '1', 'mms_process_type', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(3, '检验', '2', 'mms_process_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(4, '搬运', '3', 'mms_process_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(5, '包装', '4', 'mms_process_type', '', 'success', 'N', '0', 'admin', sysdate(), '');

-- 工单类型字典
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(NULL, '工单类型', 'mms_wo_type', '0', 'admin', sysdate(), '工单类型列表');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '正常', '0', 'mms_wo_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
(2, '返工', '1', 'mms_wo_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
(3, '试产', '2', 'mms_wo_type', '', 'info', 'N', '0', 'admin', sysdate(), ''),
(4, '补料', '3', 'mms_wo_type', '', 'danger', 'N', '0', 'admin', sysdate(), '');