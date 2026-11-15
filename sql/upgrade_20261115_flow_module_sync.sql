-- =============================================================
-- 流程管理模块同步脚本（前端 + 后端 flowable 模块升级配套）
-- 日期：2026-11-15
-- 内容：
--   1) 补齐进程模块缺失的 13 张业务表（IF NOT EXISTS，可重复执行）
--   2) 初始化紧急程度档位、流程编号回填
--   3) 补齐字典（签字意见常用语、AI流程生成状态）
--   4) 补齐菜单：流程监控 / 流程定义 / 表单管理 / 紧急程度管理 / AI流程生成
--   5) 下线普通用户的「流程撤销」按钮
-- 说明：菜单按 (parent_id, path) 或 perms 前缀幂等清理后重建，可重复执行
-- =============================================================
SET NAMES utf8mb4;

-- ========== 1. 表结构 ==========

CREATE TABLE IF NOT EXISTS `flow_ai_gen_task` (
  `task_id`      bigint       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name`    varchar(200) DEFAULT NULL COMMENT '任务名称',
  `process_name` varchar(200) DEFAULT NULL COMMENT 'AI识别的流程名称',
  `process_key`  varchar(100) DEFAULT NULL COMMENT 'AI识别的流程Key',
  `category`     varchar(64)  DEFAULT NULL COMMENT '流程分类（字典 flowable_category）',
  `remark`       varchar(500) DEFAULT NULL COMMENT '备注说明（用户补充的生成要求）',
  `file_name`    varchar(255) DEFAULT NULL COMMENT '方案文件名',
  `file_path`    varchar(500) DEFAULT NULL COMMENT '方案文件存储路径',
  `file_type`    varchar(20)  DEFAULT NULL COMMENT '文件类型（pdf/docx/md/txt）',
  `source_text`  longtext     COMMENT '方案抽取文本',
  `ai_raw_output` longtext    COMMENT 'AI原始输出',
  `result_json`  longtext     COMMENT '结构化结果JSON（人工确认后回写）',
  `status`       char(1)      DEFAULT '0' COMMENT '状态（字典 flow_ai_gen_status：0解析中 1待确认 2已生成 3失败）',
  `error_msg`    varchar(1000) DEFAULT NULL COMMENT '失败原因',
  `model_id`     varchar(64)  DEFAULT NULL COMMENT '生成后的流程模型ID',
  `form_id`      bigint       DEFAULT NULL COMMENT '生成后的表单ID',
  `form_key`     varchar(64)  DEFAULT NULL COMMENT '生成后的表单Key',
  `node_count`   int          DEFAULT '0' COMMENT '流程节点数',
  `field_count`  int          DEFAULT '0' COMMENT '表单字段数',
  `del_flag`     char(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by`    varchar(64)  DEFAULT NULL COMMENT '创建人',
  `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT NULL COMMENT '更新人',
  `update_time`  datetime     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`task_id`),
  KEY `idx_flow_ai_gen_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI流程生成任务表';

CREATE TABLE IF NOT EXISTS `flow_attach` (
  `attach_id`     bigint       NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `instance_id`   varchar(64)  NOT NULL COMMENT '流程实例ID',
  `task_id`       varchar(64)  DEFAULT NULL COMMENT '上传所在任务ID',
  `task_name`     varchar(100) DEFAULT NULL COMMENT '上传所在节点名称',
  `file_name`     varchar(255) NOT NULL COMMENT '文件名',
  `file_url`      varchar(500) NOT NULL COMMENT '文件访问地址',
  `file_size`     bigint       DEFAULT '0' COMMENT '文件大小(字节)',
  `uploader_id`   bigint       DEFAULT NULL COMMENT '上传人用户ID',
  `uploader_name` varchar(64)  DEFAULT NULL COMMENT '上传人姓名',
  `create_time`   datetime     DEFAULT NULL COMMENT '上传时间',
  PRIMARY KEY (`attach_id`),
  KEY `idx_attach_instance` (`instance_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程附件表';

CREATE TABLE IF NOT EXISTS `flow_cc` (
  `cc_id`        bigint       NOT NULL AUTO_INCREMENT COMMENT '抄送ID',
  `instance_id`  varchar(64)  NOT NULL COMMENT '流程实例ID',
  `process_key`  varchar(64)  DEFAULT NULL COMMENT '流程定义Key',
  `process_name` varchar(200) DEFAULT NULL COMMENT '流程名称',
  `business_key` varchar(64)  DEFAULT NULL COMMENT '业务主键',
  `title`        varchar(255) DEFAULT NULL COMMENT '流程标题',
  `task_id`      varchar(64)  DEFAULT NULL COMMENT '来源任务ID',
  `task_name`    varchar(100) DEFAULT NULL COMMENT '来源节点名称',
  `sender_id`    bigint       DEFAULT NULL COMMENT '抄送人(触发人)用户ID',
  `sender_name`  varchar(64)  DEFAULT NULL COMMENT '抄送人姓名',
  `receiver_id`  bigint       NOT NULL COMMENT '接收人用户ID',
  `receiver_name` varchar(64) DEFAULT NULL COMMENT '接收人姓名',
  `read_flag`    char(1)      DEFAULT '0' COMMENT '阅读状态（0未读 1已读）',
  `read_time`    datetime     DEFAULT NULL COMMENT '阅读时间',
  `create_time`  datetime     DEFAULT NULL COMMENT '抄送时间',
  PRIMARY KEY (`cc_id`),
  KEY `idx_cc_receiver` (`receiver_id`,`read_flag`,`create_time`),
  KEY `idx_cc_instance` (`instance_id`,`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程抄送（待阅）表';

CREATE TABLE IF NOT EXISTS `flow_delegate` (
  `delegate_id` bigint       NOT NULL AUTO_INCREMENT COMMENT '转办记录ID',
  `instance_id` varchar(64)  NOT NULL COMMENT '流程实例ID',
  `task_id`     varchar(64)  DEFAULT NULL COMMENT '任务ID',
  `task_name`   varchar(255) DEFAULT NULL COMMENT '环节名称',
  `from_id`     bigint       DEFAULT NULL COMMENT '转办人ID',
  `from_name`   varchar(64)  DEFAULT NULL COMMENT '转办人姓名',
  `to_id`       bigint       DEFAULT NULL COMMENT '被转办人ID',
  `to_name`     varchar(64)  DEFAULT NULL COMMENT '被转办人姓名',
  `comment`     varchar(500) DEFAULT NULL COMMENT '转办说明',
  `create_time` datetime     DEFAULT NULL COMMENT '转办时间',
  PRIMARY KEY (`delegate_id`),
  KEY `idx_instance` (`instance_id`),
  KEY `idx_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程转办记录';

CREATE TABLE IF NOT EXISTS `flow_draft` (
  `draft_id`        bigint       NOT NULL AUTO_INCREMENT COMMENT '草稿ID',
  `process_key`     varchar(64)  NOT NULL COMMENT '流程定义Key（模型Key）',
  `process_name`    varchar(200) DEFAULT NULL COMMENT '流程名称',
  `form_id`         bigint       DEFAULT NULL COMMENT '关联表单ID',
  `title`           varchar(255) DEFAULT NULL COMMENT '流程标题',
  `form_data`       longtext     COMMENT '表单数据JSON（dynData + detailData）',
  `start_user_id`   bigint       NOT NULL COMMENT '发起人用户ID',
  `start_user_name` varchar(64)  DEFAULT NULL COMMENT '发起人登录名',
  `start_nick_name` varchar(64)  DEFAULT NULL COMMENT '发起人姓名',
  `dept_id`         bigint       DEFAULT NULL COMMENT '发起人部门ID',
  `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time`     datetime     DEFAULT NULL COMMENT '最后保存时间',
  PRIMARY KEY (`draft_id`),
  KEY `idx_draft_user` (`start_user_id`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程草稿表';

CREATE TABLE IF NOT EXISTS `flow_no_serial` (
  `no_key`      varchar(191) NOT NULL COMMENT '规则键：流程Key|周期',
  `seq`         bigint       NOT NULL DEFAULT '0' COMMENT '当前流水号',
  `update_time` datetime     DEFAULT NULL COMMENT '最后取号时间',
  PRIMARY KEY (`no_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程编号流水号计数器';

CREATE TABLE IF NOT EXISTS `flow_node_form` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_id`    varchar(64)  NOT NULL COMMENT '流程模型ID',
  `model_key`   varchar(100) NOT NULL COMMENT '流程模型Key（与部署后流程定义Key一致）',
  `node_id`     varchar(100) NOT NULL COMMENT 'BPMN节点ID',
  `node_type`   varchar(20)  NOT NULL DEFAULT 'userTask' COMMENT 'start/userTask/end',
  `node_name`   varchar(200) DEFAULT NULL COMMENT '节点名称',
  `form_name`   varchar(200) DEFAULT NULL COMMENT '节点模板名称',
  `form_key`    varchar(64)  DEFAULT NULL COMMENT '引用的数据表表单Key',
  `fields_json` longtext     COMMENT '字段快照 [{field,label,form,dtype,required,...}]',
  `layout_json` longtext     COMMENT '布局快照 {mode:grid,grid:{...}}',
  `create_by`   varchar(64)  DEFAULT NULL,
  `create_time` datetime     DEFAULT NULL,
  `update_by`   varchar(64)  DEFAULT NULL,
  `update_time` datetime     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_node` (`model_id`,`node_id`),
  KEY `idx_model_key` (`model_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点表单模板';

CREATE TABLE IF NOT EXISTS `flow_node_print` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_id`         varchar(64)  NOT NULL COMMENT '流程模型ID',
  `model_key`        varchar(100) NOT NULL COMMENT '流程模型Key（与部署后流程定义Key一致）',
  `node_id`          varchar(100) NOT NULL COMMENT 'BPMN节点ID',
  `node_name`        varchar(200) DEFAULT NULL COMMENT '节点名称',
  `template_form_id` varchar(64)  DEFAULT NULL COMMENT '打印模板来源表单ID（flow_form.form_id）',
  `template_name`    varchar(200) DEFAULT NULL COMMENT '打印模板名称',
  `fields_json`      longtext     COMMENT '打印模板字段快照',
  `layout_json`      longtext     COMMENT '打印模板布局快照',
  `create_by`        varchar(64)  DEFAULT NULL,
  `create_time`      datetime     DEFAULT NULL,
  `update_by`        varchar(64)  DEFAULT NULL,
  `update_time`      datetime     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_node` (`model_id`,`node_id`),
  KEY `idx_model_key` (`model_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点打印模板';

CREATE TABLE IF NOT EXISTS `flow_node_viewer` (
  `viewer_id`   bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instance_id` varchar(64) NOT NULL COMMENT '流程实例ID',
  `node_id`     varchar(128) NOT NULL COMMENT '节点ID（BPMN activityId）',
  `task_id`     varchar(64) DEFAULT NULL COMMENT '最近一次查看的任务ID',
  `user_id`     bigint      NOT NULL COMMENT '查看人用户ID',
  `user_name`   varchar(64) DEFAULT '' COMMENT '查看人姓名（冗余存储）',
  `view_time`   datetime    DEFAULT NULL COMMENT '首次查看时间',
  PRIMARY KEY (`viewer_id`),
  UNIQUE KEY `uk_inst_node_user` (`instance_id`,`node_id`,`user_id`),
  KEY `idx_inst` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点查看记录（流程图「已查看人员」）';

CREATE TABLE IF NOT EXISTS `flow_opinion_preset` (
  `preset_id`   bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     bigint      NOT NULL COMMENT '所属用户ID（数据边界，接口只操作本人数据）',
  `content`     varchar(200) NOT NULL COMMENT '常用意见内容（纯文本）',
  `show_order`  int         DEFAULT '0' COMMENT '排序（越小越靠前）',
  `create_by`   varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime    DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime    DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`preset_id`),
  KEY `idx_user_order` (`user_id`,`show_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签字意见-个人常用意见';

CREATE TABLE IF NOT EXISTS `flow_procdef_ext` (
  `procdef_id`  varchar(64)  NOT NULL COMMENT '流程定义ID（ACT_RE_PROCDEF.ID_）',
  `proc_key`    varchar(64)  DEFAULT '' COMMENT '流程Key（ACT_RE_PROCDEF.KEY_）',
  `version`     int          DEFAULT NULL COMMENT '版本号（ACT_RE_PROCDEF.VERSION_）',
  `remark`      varchar(500) DEFAULT '' COMMENT '版本备注说明',
  `active_flag` char(1)      DEFAULT '0' COMMENT '是否活动版本（1当前活动版本 0非活动）',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`procdef_id`),
  KEY `idx_proc_key` (`proc_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义扩展（版本备注/活动版本）';

CREATE TABLE IF NOT EXISTS `flow_request` (
  `request_id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '流程编号（流程请求主键，自增数字）',
  `instance_id`            varchar(64)  NOT NULL COMMENT 'Flowable 流程实例ID（内部UUID）',
  `process_definition_key` varchar(128) DEFAULT NULL COMMENT '流程定义Key',
  `business_key`           varchar(64)  DEFAULT NULL COMMENT '业务主键',
  `create_time`            datetime     DEFAULT NULL COMMENT '登记时间（历史数据取流程发起时间）',
  PRIMARY KEY (`request_id`),
  UNIQUE KEY `uk_instance_id` (`instance_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100001 DEFAULT CHARSET=utf8mb4 COMMENT='流程请求登记表（流程实例对外短数字编号）';

CREATE TABLE IF NOT EXISTS `flow_urgency_level` (
  `level_id`     bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `level_value`  varchar(20) NOT NULL COMMENT '档位值（存业务表 urgency 列）',
  `default_name` varchar(20) NOT NULL COMMENT '系统默认名（普通/紧急/特急）',
  `custom_name`  varchar(100) DEFAULT NULL COMMENT '用户自定义名（为空则用 default_name）',
  `show_order`   int         DEFAULT '0' COMMENT '排序（越小越靠前）',
  `is_sealed`    char(1)     DEFAULT '0' COMMENT '是否封存：0否 1是（封存后新发起不可选）',
  `is_remind`    char(1)     DEFAULT '0' COMMENT '是否触发提醒分级：0否 1是',
  `status`       char(1)     DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_by`    varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time`  datetime    DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time`  datetime    DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`level_id`),
  UNIQUE KEY `uk_level_value` (`level_value`),
  KEY `idx_show_order` (`show_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='紧急程度配置（驱动系统字段 urgency 选项）';

-- ========== 2. 初始数据 ==========

INSERT INTO flow_urgency_level (level_value, default_name, custom_name, show_order, is_sealed, is_remind, status, create_by, create_time)
SELECT t.v, t.n, NULL, t.o, '0', t.r, '0', 'admin', NOW()
FROM (SELECT '1' AS v, '普通' AS n, 1 AS o, '0' AS r
      UNION ALL SELECT '2', '紧急', 2, '1'
      UNION ALL SELECT '3', '特急', 3, '1') t
WHERE NOT EXISTS (SELECT 1 FROM flow_urgency_level WHERE level_value = t.v);

-- 流程编号回填（按发起时间顺序发号；已登记的实例自动跳过）
INSERT IGNORE INTO `flow_request` (`instance_id`, `process_definition_key`, `business_key`, `create_time`)
SELECT `ID_`, SUBSTRING_INDEX(`PROC_DEF_ID_`, ':', 1), `BUSINESS_KEY_`, `START_TIME_`
FROM `ACT_HI_PROCINST`
ORDER BY `START_TIME_`;

-- ========== 3. 字典 ==========

DELETE FROM sys_dict_data WHERE dict_type = 'flow_opinion_preset';
DELETE FROM sys_dict_type WHERE dict_type = 'flow_opinion_preset';
INSERT INTO sys_dict_type (dict_name, dict_type, dict_category, status, create_by, create_time, remark)
VALUES ('流程签字意见常用语', 'flow_opinion_preset', 'system', '0', 'admin', NOW(),
        '办理页「常用意见」系统默认短语；个人可在办理页「设置」中自定义覆盖');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '同意', '1', 'flow_opinion_preset', NULL, 'default', 'N', '0', 'admin', NOW(), '签字意见常用语'),
(2, '情况属实，同意办理', '2', 'flow_opinion_preset', NULL, 'default', 'N', '0', 'admin', NOW(), '签字意见常用语'),
(3, '同意，请领导审批', '3', 'flow_opinion_preset', NULL, 'default', 'N', '0', 'admin', NOW(), '签字意见常用语'),
(4, '请补充相关材料后再提交', '4', 'flow_opinion_preset', NULL, 'default', 'N', '0', 'admin', NOW(), '签字意见常用语'),
(5, '不同意，请修改后重新提交', '5', 'flow_opinion_preset', NULL, 'default', 'N', '0', 'admin', NOW(), '签字意见常用语');

DELETE FROM sys_dict_data WHERE dict_type = 'flow_ai_gen_status';
DELETE FROM sys_dict_type WHERE dict_type = 'flow_ai_gen_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('AI流程生成状态', 'flow_ai_gen_status', '0', 'admin', sysdate(), 'AI流程生成任务状态');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '解析中', '0', 'flow_ai_gen_status', '', 'info',    'Y', '0', 'admin', sysdate(), 'AI正在解析流程方案'),
(2, '待确认', '1', 'flow_ai_gen_status', '', 'warning', 'N', '0', 'admin', sysdate(), 'AI解析完成，等待人工确认后生成'),
(3, '已生成', '2', 'flow_ai_gen_status', '', 'success', 'N', '0', 'admin', sysdate(), '已生成流程表单与流程模型草稿'),
(4, '失败',   '3', 'flow_ai_gen_status', '', 'danger',  'N', '0', 'admin', sysdate(), '解析或生成失败');

-- ========== 4. 菜单 ==========

SET @fm = (SELECT menu_id FROM sys_menu WHERE parent_id = 1 AND menu_name = '流程管理' LIMIT 1);
SET @fc = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'flow' LIMIT 1);

-- 4.1 下线普通用户的「流程撤销」按钮
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:process:cancel');
DELETE FROM sys_menu WHERE perms = 'flowable:process:cancel';

-- 4.2 流程监控（后台）+ 前台流程中心入口
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:monitor%');
DELETE FROM sys_menu WHERE perms LIKE 'flowable:monitor%';
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE parent_id = @fc AND path = 'monitor');
DELETE FROM sys_menu WHERE parent_id = @fc AND path = 'monitor';

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('流程监控', @fm, 3, 'monitor', 'flowable/monitor/index', 1, 0, 'C', '0', '0', 'flowable:monitor:list', 'monitor', 'admin', sysdate(), '全流程实例监控与管理员干预（跳转节点/改派/强制结束/撤销/删除）');
SET @pm = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('监控查询', @pm, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:query',     '#', 'admin', sysdate(), '查看干预面板与节点信息'),
('流程干预', @pm, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:intervene', '#', 'admin', sysdate(), '强制跳转任意节点 / 改派办理人'),
('流程结束', @pm, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:end',       '#', 'admin', sysdate(), '强制结束 / 撤销流程'),
('流程删除', @pm, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:remove',    '#', 'admin', sysdate(), '删除流程实例及其历史记录'),
('流程重新发起', @pm, 5, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:restart', '#', 'admin', sysdate(), '以原单表单数据新建一条流程（新单、新流程编号）'),
('流程复活',     @pm, 6, '#', '', 1, 0, 'F', '0', '0', 'flowable:monitor:revive',  '#', 'admin', sysdate(), '已结束/已中断流程重建运行实例并停在指定节点（强权限）');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('流程监控', @fc, 6, 'monitor', 'flowable/monitor/index', NULL, 'FlowMonitor', 1, 0,
        'C', '0', '0', 'flowable:monitor:list', 'monitor', 'admin', sysdate(), '管理员流程监控入口（与 系统管理→流程管理→流程监控 指向同一页面）');

-- 4.3 流程定义
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:definition%');
DELETE FROM sys_menu WHERE perms LIKE 'flowable:definition%';
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('流程定义', @fm, 2, 'definition', 'flowable/definition/index', 1, 0, 'C', '0', '0', 'flowable:definition:list', 'component', 'admin', sysdate(), '已部署流程版本管理：多版本查看/查看XML/备注/挂起激活/设为活动版本');
SET @pm = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('版本查询', @pm, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:definition:query', '#', 'admin', sysdate(), '查看历史版本 XML / 节点清单'),
('版本状态', @pm, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:definition:state', '#', 'admin', sysdate(), '版本备注 / 挂起激活 / 设为活动版本');

-- 4.4 表单管理（已有则跳过）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '表单管理', @fm, 4, 'form', 'flowable/form/index', 1, 0, 'C', '0', '0', 'flowable:form:list', 'form', 'admin', sysdate(), '流程表单模板管理'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @fm AND path = 'form');
SET @formMenu = (SELECT menu_id FROM sys_menu WHERE parent_id = @fm AND path = 'form' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.n, @formMenu, t.o, '#', '', 1, 0, 'F', '0', '0', t.p, '#', 'admin', sysdate(), ''
FROM (SELECT '表单查询' AS n, 1 AS o, 'flowable:form:query' AS p
      UNION ALL SELECT '表单新增', 2, 'flowable:form:add'
      UNION ALL SELECT '表单修改', 3, 'flowable:form:edit'
      UNION ALL SELECT '表单删除', 4, 'flowable:form:remove'
      UNION ALL SELECT '数据表生成', 5, 'flowable:form:table') t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = t.p);

-- 4.5 紧急程度管理
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:urgency%');
DELETE FROM sys_menu WHERE perms LIKE 'flowable:urgency%';
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('紧急程度管理', @fm, 6, 'urgency', 'flowable/urgency/index', 1, 0, 'C', '0', '0', 'flowable:urgency:list', 'star', 'admin', sysdate(), '流程紧急程度配置（系统字段 urgency 档位）');
SET @pm = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('紧急程度查询', @pm, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:urgency:query',  '#', 'admin', sysdate(), ''),
('紧急程度新增', @pm, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:urgency:add',    '#', 'admin', sysdate(), ''),
('紧急程度修改', @pm, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:urgency:edit',   '#', 'admin', sysdate(), ''),
('紧急程度删除', @pm, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:urgency:remove', '#', 'admin', sysdate(), '');

-- 4.6 AI流程生成
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:aigen%');
DELETE FROM sys_menu WHERE perms LIKE 'flowable:aigen%';
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('AI流程生成', @fm, 7, 'aigen', 'flowable/aigen/index', 1, 0, 'C', '0', '0', 'flowable:aigen:list', 'magic-stick', 'admin', sysdate(), '上传流程方案，AI自动生成流程表单与流程草稿');
SET @pm = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('任务查询', @pm, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:aigen:query',    '#', 'admin', sysdate(), '查看任务详情与解析结果'),
('方案解析', @pm, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:aigen:analyze',  '#', 'admin', sysdate(), '上传方案并调用AI解析'),
('生成草稿', @pm, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:aigen:generate', '#', 'admin', sysdate(), '按确认结果生成表单与流程模型草稿'),
('任务删除', @pm, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:aigen:remove',   '#', 'admin', sysdate(), '删除AI流程生成任务');

-- 4.7 菜单顺序统一：模型 → 流程定义 → 流程监控 → 表单 → 分类 → 紧急程度 → AI流程生成
UPDATE sys_menu SET order_num = CASE path
        WHEN 'model'      THEN 1
        WHEN 'definition' THEN 2
        WHEN 'monitor'    THEN 3
        WHEN 'form'       THEN 4
        WHEN 'category'   THEN 5
        WHEN 'urgency'    THEN 6
        WHEN 'aigen'      THEN 7
        ELSE order_num END
WHERE parent_id = @fm;

-- 4.8 角色授权：与「流程模型」保持同等范围 + 超级管理员
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, m2.menu_id
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id AND m.perms = 'flowable:model:list'
JOIN sys_menu m2 ON (m2.parent_id = @fm AND m2.menu_type = 'C')
                OR (m2.parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE parent_id = @fm AND menu_type = 'C') x)
                    AND m2.menu_type = 'F');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 1, menu_id FROM sys_menu WHERE parent_id = @fm OR parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE parent_id = @fm) y);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, m2.menu_id
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id AND m.perms = 'flowable:model:list'
JOIN sys_menu m2 ON m2.parent_id = @fc AND m2.path = 'monitor';

-- 4.9 流程监控新动作授权：重新发起跟随「流程干预」，复活仅超级管理员
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id,
       (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:monitor:restart' LIMIT 1)
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id AND m.perms = 'flowable:monitor:intervene';
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms = 'flowable:monitor:revive';

-- ========== 5. 校验 ==========
SELECT menu_id, menu_name, parent_id, order_num, path, component, perms
FROM sys_menu WHERE parent_id = @fm OR (parent_id = @fc AND path = 'monitor')
ORDER BY parent_id, order_num;

SELECT dict_type, dict_label, dict_value FROM sys_dict_data
WHERE dict_type IN ('flow_opinion_preset', 'flow_ai_gen_status') ORDER BY dict_type, dict_sort;

-- =============================================================
-- 回滚（如需）：
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:%');
-- 仅删除本次新增菜单：
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:monitor%' OR perms LIKE 'flowable:definition%' OR perms LIKE 'flowable:urgency%' OR perms LIKE 'flowable:aigen%');
-- DELETE FROM sys_menu WHERE perms LIKE 'flowable:monitor%' OR perms LIKE 'flowable:definition%' OR perms LIKE 'flowable:urgency%' OR perms LIKE 'flowable:aigen%';
-- DELETE FROM sys_dict_data WHERE dict_type IN ('flow_opinion_preset','flow_ai_gen_status');
-- DELETE FROM sys_dict_type WHERE dict_type IN ('flow_opinion_preset','flow_ai_gen_status');
-- DROP TABLE IF EXISTS flow_ai_gen_task, flow_attach, flow_cc, flow_delegate, flow_draft, flow_no_serial,
--   flow_node_form, flow_node_print, flow_node_viewer, flow_opinion_preset, flow_procdef_ext, flow_request, flow_urgency_level;
-- =============================================================
