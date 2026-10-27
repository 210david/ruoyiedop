-- =============================================
-- Flowable工作流模块 - 菜单+字典（表结构由Flowable引擎启动时自动创建 act_* 表）
-- =============================================
SET NAMES utf8mb4;

-- ========== 删除旧菜单（幂等） ==========
DELETE FROM sys_menu WHERE menu_name = '工作流管理' AND parent_id = 0;

-- ========== 一级菜单 ==========
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('工作流管理', 0, 16, 'flowable', NULL, 1, 0, 'M', '0', '0', '', 'example', 'admin', sysdate(), '工作流管理目录');
SET @flowMenuId = LAST_INSERT_ID();

-- ========== 子菜单 ==========
-- 流程模型
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('流程模型', @flowMenuId, 1, 'model', 'flowable/model/index', 1, 0, 'C', '0', '0', 'flowable:model:list', 'tree-table', 'admin', sysdate(), '流程模型设计与部署');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('模型查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('模型新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('模型修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('模型删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:remove', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('模型部署', @p, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:deploy', '#', 'admin', sysdate(), '');

-- 流程定义
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('流程定义', @flowMenuId, 2, 'definition', 'flowable/definition/index', 1, 0, 'C', '0', '0', 'flowable:definition:list', 'component', 'admin', sysdate(), '已部署流程版本管理');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定义查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:definition:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定义挂起激活', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:definition:state', '#', 'admin', sysdate(), '');

-- 我的待办
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('我的待办', @flowMenuId, 3, 'task', 'flowable/task/index', 1, 0, 'C', '0', '0', 'flowable:task:todo', 'email', 'admin', sysdate(), '待办/已办任务');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('任务办理', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:task:complete', '#', 'admin', sysdate(), '');

-- 我发起的流程
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('我发起的流程', @flowMenuId, 4, 'process', 'flowable/process/index', 1, 0, 'C', '0', '0', 'flowable:process:myList', 'log', 'admin', sysdate(), '我发起的流程与撤销');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('流程查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('流程发起', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:start', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('流程撤销', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:cancel', '#', 'admin', sysdate(), '');

-- ========== 字典：流程分类 ==========
DELETE FROM sys_dict_type WHERE dict_type = 'flowable_category';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('流程分类', 'flowable_category', '0', 'admin', sysdate(), '工作流流程分类');
SET @dictTypeId = LAST_INSERT_ID();

DELETE FROM sys_dict_data WHERE dict_type = 'flowable_category';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '行政办公', 'oa', 'flowable_category', '', 'primary', 'Y', '0', 'admin', sysdate(), '请假/用印等行政流程'),
(2, '人事流程', 'hr', 'flowable_category', '', 'success', 'N', '0', 'admin', sysdate(), '入职/转正/离职等'),
(3, '财务流程', 'fin', 'flowable_category', '', 'warning', 'N', '0', 'admin', sysdate(), '报销/付款申请等'),
(4, '采购流程', 'pms', 'flowable_category', '', 'danger', 'N', '0', 'admin', sysdate(), '采购/退货审批等'),
(5, '业务流程', 'biz', 'flowable_category', '', 'info', 'N', '0', 'admin', sysdate(), '其他业务审批');
