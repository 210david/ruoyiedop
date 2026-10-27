-- =============================================
-- 工作流菜单重构（参照泛微e9前后台分离模式）
-- 前台：流程中心（流程发起/我的待办/我的请求）
-- 后台：系统管理→流程管理（模型/定义/表单管理）
-- =============================================
SET NAMES utf8mb4;

-- ========== 清理旧菜单树 ==========
-- 先清理已知业务权限节点，避免直接删除父菜单后遗留孤儿按钮
DELETE FROM sys_menu WHERE perms LIKE 'flowable:%';
DELETE FROM sys_menu WHERE menu_name IN ('工作流管理', '流程中心') AND parent_id = 0;

-- ========== 前台一级菜单：流程中心 ==========
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('流程中心', 0, 3, 'flow', NULL, 1, 0, 'M', '0', '0', '', 'example', 'admin', sysdate(), '业务用户流程入口（参照泛微前端）');
SET @fc = LAST_INSERT_ID();

-- 流程发起（新建流程）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('流程发起', @fc, 1, 'launch', 'flowable/launch/index', 1, 0, 'C', '0', '0', 'flowable:process:start', 'guide', 'admin', sysdate(), '按已部署流程发起新的审批');
-- 我的待办
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('我的待办', @fc, 2, 'task', 'flowable/task/index', 1, 0, 'C', '0', '0', 'flowable:task:todo', 'email', 'admin', sysdate(), '待办/已办任务办理');
-- 我的请求
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('我的请求', @fc, 3, 'process', 'flowable/process/index', 1, 0, 'C', '0', '0', 'flowable:process:myList', 'log', 'admin', sysdate(), '我发起的流程与撤销');
-- 请假示例
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('请假示例', @fc, 4, 'leave', 'flowable/leave/index', 1, 0, 'C', '0', '0', 'flowable:leave:list', 'form', 'admin', sysdate(), '请假审批流程示例');

-- ========== 后台：系统管理 → 流程管理 ==========
SET @sysId = (SELECT menu_id FROM sys_menu WHERE menu_name = '系统管理' AND parent_id = 0 LIMIT 1);
DELETE FROM sys_menu WHERE menu_name = '流程管理' AND parent_id = @sysId;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('流程管理', @sysId, 8, 'flowmgr', NULL, 1, 0, 'M', '0', '0', '', 'component', 'admin', sysdate(), '流程引擎后台配置（参照泛微后端引擎）');
SET @fm = LAST_INSERT_ID();

-- 流程模型
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('流程模型', @fm, 1, 'model', 'flowable/model/index', 1, 0, 'C', '0', '0', 'flowable:model:list', 'tree-table', 'admin', sysdate(), '流程图设计与部署');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('模型查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:query',  '#', 'admin', sysdate(), ''),
('模型新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:add',    '#', 'admin', sysdate(), ''),
('模型修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:edit',   '#', 'admin', sysdate(), ''),
('模型删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:remove', '#', 'admin', sysdate(), ''),
('模型部署', @p, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:model:deploy', '#', 'admin', sysdate(), '');

-- 流程定义
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('流程定义', @fm, 2, 'definition', 'flowable/definition/index', 1, 0, 'C', '0', '0', 'flowable:definition:list', 'component', 'admin', sysdate(), '已部署流程版本管理');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('定义查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:definition:query', '#', 'admin', sysdate(), ''),
('定义挂起激活', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:definition:state', '#', 'admin', sysdate(), '');

-- 表单管理（新增，含数据表生成）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('表单管理', @fm, 3, 'form', 'flowable/form/index', 1, 0, 'C', '0', '0', 'flowable:form:list', 'form', 'admin', sysdate(), '流程表单设计与数据表生成（建模引擎）');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('表单查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:query',  '#', 'admin', sysdate(), ''),
('表单新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:add',    '#', 'admin', sysdate(), ''),
('表单修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:edit',   '#', 'admin', sysdate(), ''),
('表单删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:remove', '#', 'admin', sysdate(), ''),
('数据表生成', @p, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:form:table',  '#', 'admin', sysdate(), '');

-- 任务办理按钮（迁移保留）
SET @td = (SELECT menu_id FROM sys_menu WHERE menu_name = '我的待办' AND parent_id = @fc LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('任务办理', @td, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:task:complete', '#', 'admin', sysdate(), '');
SET @pc = (SELECT menu_id FROM sys_menu WHERE menu_name = '我的请求' AND parent_id = @fc LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('流程查询', @pc, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:query',  '#', 'admin', sysdate(), ''),
('流程撤销', @pc, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:process:cancel', '#', 'admin', sysdate(), '');
SET @lv = (SELECT menu_id FROM sys_menu WHERE menu_name = '请假示例' AND parent_id = @fc LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('请假查询', @lv, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:leave:query',   '#', 'admin', sysdate(), ''),
('请假新增', @lv, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:leave:add',     '#', 'admin', sysdate(), ''),
('请假提交', @lv, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:leave:submit',  '#', 'admin', sysdate(), ''),
('请假删除', @lv, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:leave:remove',  '#', 'admin', sysdate(), '');

-- ========== 表单设计存储表 ==========
CREATE TABLE IF NOT EXISTS `flow_form` (
  `form_id`     bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '表单ID',
  `form_key`    varchar(60)  NOT NULL COMMENT '表单Key',
  `form_name`   varchar(100) NOT NULL COMMENT '表单名称',
  `fields_json` text         COMMENT '字段定义JSON [{field,label,type,required}]',
  `table_name`  varchar(64)  DEFAULT NULL COMMENT '生成的数据表名',
  `del_flag`    char(1)      DEFAULT '0',
  `create_by`   varchar(64)  DEFAULT '',
  `create_time` datetime     DEFAULT NULL,
  `update_by`   varchar(64)  DEFAULT '',
  `update_time` datetime     DEFAULT NULL,
  `remark`      varchar(500) DEFAULT NULL,
  PRIMARY KEY (`form_id`),
  UNIQUE KEY `uk_form_key` (`form_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程表单定义';

-- ========== 表单描述字段（泛微e9字段设计改造，2026-09-17） ==========
-- flow_form 增加 form_desc 表单描述列（重复执行报 Duplicate column 可忽略）
ALTER TABLE flow_form ADD COLUMN form_desc varchar(500) DEFAULT NULL COMMENT '表单描述' AFTER form_name;

-- ========== 流程分类字段（表单管理增加流程分类，2026-10-11） ==========
-- flow_form 增加 category 流程分类列（重复执行报 Duplicate column 可忽略），字典：flowable_category
ALTER TABLE flow_form ADD COLUMN category varchar(50) DEFAULT NULL COMMENT '流程分类（字典 flowable_category）' AFTER form_desc;
