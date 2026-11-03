-- =============================================
-- 需求一：安全生产台账——安全守则记录模块（源禾农业 2026-11）
-- SR-01（决策后口径）：守则关联“安全生产制度名称+附件”为选填弱关联（YHNY 无独立制度模块）
-- SR-02 生效守则不可删除，只可废止并留痕
-- SR-03 学习任务发起后名单只可追加不可减少
-- SR-04 检查记录“不符合”项必须填整改责任人/期限/结果，闭环前不允许归档
-- 1) biz_safety_rule 守则库（含版本号与状态机）
-- 2) biz_safety_rule_version 历史版本快照
-- 3) biz_safety_rule_learn_task / biz_safety_rule_learn 学习任务与签阅留痕
-- 4) biz_safety_rule_check 守则执行检查记录（整改闭环）
-- 5) 字典、菜单、逾期提醒定时任务
-- =============================================

-- ----------------------------
-- 1. 守则库
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_safety_rule` (
    `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '守则ID',
    `rule_no` varchar(64) NOT NULL COMMENT '守则编号',
    `rule_name` varchar(200) NOT NULL COMMENT '守则名称',
    `dept_id` bigint NULL COMMENT '适用部门ID',
    `dept_name` varchar(100) NULL COMMENT '适用部门',
    `post_name` varchar(100) NULL COMMENT '适用岗位',
    `work_scene` varchar(200) NULL COMMENT '适用作业场景',
    `rule_content` longtext NULL COMMENT '守则内容（富文本）',
    `attachment` varchar(500) NULL COMMENT '附件（图片/文件）',
    `regulation_name` varchar(200) NULL COMMENT '关联安全生产制度名称（SR-01 弱关联，选填）',
    `regulation_attachment` varchar(500) NULL COMMENT '关联制度附件',
    `version_no` int DEFAULT 1 COMMENT '版本号',
    `effective_date` date NULL COMMENT '生效日期',
    `expire_date` date NULL COMMENT '失效日期',
    `rule_status` char(1) DEFAULT '0' COMMENT '状态（0草稿 1生效 2废止）',
    `author` varchar(64) NULL COMMENT '编制人',
    `approver` varchar(64) NULL COMMENT '审批人',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`rule_id`),
    UNIQUE KEY `uk_safety_rule_no` (`rule_no`),
    KEY `idx_safety_rule_dept` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='安全守则库';

-- ----------------------------
-- 2. 历史版本快照
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_safety_rule_version` (
    `version_id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本ID',
    `rule_id` bigint NOT NULL COMMENT '守则ID',
    `rule_no` varchar(64) NULL COMMENT '守则编号',
    `version_no` int NOT NULL COMMENT '版本号',
    `rule_name` varchar(200) NULL COMMENT '守则名称',
    `dept_name` varchar(100) NULL COMMENT '适用部门',
    `post_name` varchar(100) NULL COMMENT '适用岗位',
    `work_scene` varchar(200) NULL COMMENT '适用作业场景',
    `rule_content` longtext NULL COMMENT '守则内容',
    `attachment` varchar(500) NULL COMMENT '附件',
    `regulation_name` varchar(200) NULL COMMENT '关联制度名称',
    `effective_date` date NULL COMMENT '生效日期',
    `expire_date` date NULL COMMENT '失效日期',
    `rule_status` char(1) NULL COMMENT '当时状态',
    `change_note` varchar(500) NULL COMMENT '变更说明',
    `change_by` varchar(64) NULL COMMENT '变更人',
    `change_time` datetime NULL COMMENT '变更时间',
    PRIMARY KEY (`version_id`),
    KEY `idx_safety_rule_version_rule` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='安全守则历史版本';

-- ----------------------------
-- 3. 学习任务与签阅记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_safety_rule_learn_task` (
    `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学习任务ID',
    `task_no` varchar(64) NULL COMMENT '任务编号',
    `task_name` varchar(200) NULL COMMENT '任务名称',
    `rule_id` bigint NOT NULL COMMENT '守则ID',
    `rule_name` varchar(200) NULL COMMENT '守则名称',
    `rule_version_no` int NULL COMMENT '学习版本号',
    `target_desc` varchar(500) NULL COMMENT '学习对象说明（部门/岗位/人员）',
    `deadline` date NULL COMMENT '完成期限',
    `task_status` char(1) DEFAULT '0' COMMENT '状态（0进行中 1已完成 2已逾期）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`task_id`),
    KEY `idx_safety_rule_task_rule` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='安全守则学习任务';

CREATE TABLE IF NOT EXISTS `biz_safety_rule_learn` (
    `learn_id` bigint NOT NULL AUTO_INCREMENT COMMENT '签阅记录ID',
    `task_id` bigint NOT NULL COMMENT '学习任务ID',
    `rule_id` bigint NOT NULL COMMENT '守则ID',
    `user_id` bigint NULL COMMENT '签阅人ID',
    `user_name` varchar(64) NULL COMMENT '签阅人',
    `dept_id` bigint NULL COMMENT '部门ID',
    `dept_name` varchar(100) NULL COMMENT '部门名称',
    `sign_status` char(1) DEFAULT '0' COMMENT '状态（0待签阅 1已签阅 2逾期）',
    `sign_time` datetime NULL COMMENT '签阅时间',
    `sign_ip` varchar(64) NULL COMMENT '签阅IP',
    `sign_terminal` varchar(32) DEFAULT 'PC' COMMENT '签阅终端（本期固定PC，字段预留）',
    `exam_score` decimal(5,2) NULL COMMENT '测试成绩（联动AI安全培训试题）',
    `is_pass` char(1) NULL COMMENT '是否通过（0否 1是）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`learn_id`),
    KEY `idx_safety_rule_learn_task` (`task_id`),
    KEY `idx_safety_rule_learn_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='安全守则签阅记录';

-- ----------------------------
-- 4. 守则执行检查记录（整改闭环）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_safety_rule_check` (
    `check_id` bigint NOT NULL AUTO_INCREMENT COMMENT '检查记录ID',
    `check_no` varchar(64) NULL COMMENT '检查编号',
    `check_date` date NULL COMMENT '检查日期',
    `checker` varchar(64) NULL COMMENT '检查人',
    `dept_id` bigint NULL COMMENT '被检查部门ID',
    `dept_name` varchar(100) NULL COMMENT '被检查部门/岗位',
    `rule_id` bigint NULL COMMENT '关联守则ID',
    `rule_name` varchar(200) NULL COMMENT '关联守则名称',
    `check_result` char(1) NULL COMMENT '检查结果（1符合 2不符合 3不涉及）',
    `problem_desc` varchar(500) NULL COMMENT '不符合描述',
    `rectify_requirement` varchar(500) NULL COMMENT '整改要求',
    `rectify_user` varchar(64) NULL COMMENT '整改责任人',
    `rectify_deadline` date NULL COMMENT '整改期限',
    `rectify_result` varchar(500) NULL COMMENT '整改结果',
    `reviewer` varchar(64) NULL COMMENT '复查人',
    `review_time` datetime NULL COMMENT '复查时间',
    `check_status` char(1) DEFAULT '0' COMMENT '状态（0待整改 1待复查 2已闭环 3已归档）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`check_id`),
    KEY `idx_safety_rule_check_rule` (`rule_id`),
    KEY `idx_safety_rule_check_dept` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='安全守则执行检查记录';

-- ----------------------------
-- 5. 字典
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('安全守则状态', 'safety_rule_status', '0', 'admin', NOW(), '安全守则状态'),
('守则检查结果', 'safety_rule_check_result', '0', 'admin', NOW(), '守则执行检查结果'),
('守则签阅状态', 'safety_rule_learn_status', '0', 'admin', NOW(), '守则学习签阅状态');

DELETE FROM sys_dict_data WHERE dict_type IN ('safety_rule_status','safety_rule_check_result','safety_rule_learn_status');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '草稿', '0', 'safety_rule_status', '', 'info',    'N', '0', 'admin', NOW(), ''),
(NULL, 2, '生效', '1', 'safety_rule_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '废止', '2', 'safety_rule_status', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(NULL, 1, '符合',   '1', 'safety_rule_check_result', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '不符合', '2', 'safety_rule_check_result', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(NULL, 3, '不涉及', '3', 'safety_rule_check_result', '', 'info',    'N', '0', 'admin', NOW(), ''),
(NULL, 1, '待签阅', '0', 'safety_rule_learn_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '已签阅', '1', 'safety_rule_learn_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '逾期',   '2', 'safety_rule_learn_status', '', 'danger',  'N', '0', 'admin', NOW(), '');

-- ----------------------------
-- 6. 菜单：安全生产管理（4638）下新增「安全守则」及其页面
-- ----------------------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('安全守则', 4638, 9, 'rule', NULL, 1, 0, 'M', '0', '0', NULL, 'documentation', 'admin', NOW(), '安全守则编制、学习签阅与执行检查');

SET @ruleMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('守则库', @ruleMenuId, 1, 'list', 'safety/rule/index', 1, 0, 'C', '0', '0', 'safety:rule:list', 'list', 'admin', NOW(), '安全守则库');

SET @ruleLibId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('守则查询', @ruleLibId, 1, '', '', 1, 0, 'F', '0', '0', 'safety:rule:query',  '#', 'admin', NOW(), ''),
('守则新增', @ruleLibId, 2, '', '', 1, 0, 'F', '0', '0', 'safety:rule:add',    '#', 'admin', NOW(), ''),
('守则修改', @ruleLibId, 3, '', '', 1, 0, 'F', '0', '0', 'safety:rule:edit',   '#', 'admin', NOW(), ''),
('守则发布', @ruleLibId, 4, '', '', 1, 0, 'F', '0', '0', 'safety:rule:publish','#', 'admin', NOW(), ''),
('守则废止', @ruleLibId, 5, '', '', 1, 0, 'F', '0', '0', 'safety:rule:abolish','#', 'admin', NOW(), ''),
('守则删除', @ruleLibId, 6, '', '', 1, 0, 'F', '0', '0', 'safety:rule:remove', '#', 'admin', NOW(), ''),
('守则导出', @ruleLibId, 7, '', '', 1, 0, 'F', '0', '0', 'safety:rule:export', '#', 'admin', NOW(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('学习任务', @ruleMenuId, 2, 'learn', 'safety/ruleLearn/index', 1, 0, 'C', '0', '0', 'safety:ruleLearn:list', 'education', 'admin', NOW(), '守则学习任务与签阅管理');

SET @ruleLearnId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('任务查询', @ruleLearnId, 1, '', '', 1, 0, 'F', '0', '0', 'safety:ruleLearn:query', '#', 'admin', NOW(), ''),
('任务新增', @ruleLearnId, 2, '', '', 1, 0, 'F', '0', '0', 'safety:ruleLearn:add',   '#', 'admin', NOW(), ''),
('名单追加', @ruleLearnId, 3, '', '', 1, 0, 'F', '0', '0', 'safety:ruleLearn:append','#', 'admin', NOW(), ''),
('签阅登记', @ruleLearnId, 4, '', '', 1, 0, 'F', '0', '0', 'safety:ruleLearn:sign',   '#', 'admin', NOW(), ''),
('任务导出', @ruleLearnId, 5, '', '', 1, 0, 'F', '0', '0', 'safety:ruleLearn:export', '#', 'admin', NOW(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('执行检查', @ruleMenuId, 3, 'check', 'safety/ruleCheck/index', 1, 0, 'C', '0', '0', 'safety:ruleCheck:list', 'checkbox', 'admin', NOW(), '守则执行检查与整改闭环');

SET @ruleCheckId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('检查查询', @ruleCheckId, 1, '', '', 1, 0, 'F', '0', '0', 'safety:ruleCheck:query',  '#', 'admin', NOW(), ''),
('检查新增', @ruleCheckId, 2, '', '', 1, 0, 'F', '0', '0', 'safety:ruleCheck:add',    '#', 'admin', NOW(), ''),
('检查修改', @ruleCheckId, 3, '', '', 1, 0, 'F', '0', '0', 'safety:ruleCheck:edit',   '#', 'admin', NOW(), ''),
('整改复查', @ruleCheckId, 4, '', '', 1, 0, 'F', '0', '0', 'safety:ruleCheck:review', '#', 'admin', NOW(), ''),
('检查归档', @ruleCheckId, 5, '', '', 1, 0, 'F', '0', '0', 'safety:ruleCheck:archive','#', 'admin', NOW(), ''),
('检查删除', @ruleCheckId, 6, '', '', 1, 0, 'F', '0', '0', 'safety:ruleCheck:remove', '#', 'admin', NOW(), ''),
('检查导出', @ruleCheckId, 7, '', '', 1, 0, 'F', '0', '0', 'safety:ruleCheck:export', '#', 'admin', NOW(), '');

-- ----------------------------
-- 7. 定时任务：守则学习逾期提醒（SR-03 配套）
-- ----------------------------
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('安全守则学习逾期提醒', 'DEFAULT', 'safetyRuleOverdueTask.handle()', '0 30 8 * * ?', '3', '1', '0', 'admin', NOW(), '每日检查守则学习任务签阅情况，逾期未签阅置为逾期并提醒（SR-03）');

-- 验证
SELECT '--- 守则相关新表 ---' AS info;
SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('biz_safety_rule','biz_safety_rule_version','biz_safety_rule_learn_task','biz_safety_rule_learn','biz_safety_rule_check');
SELECT '--- 守则菜单 ---' AS info;
SELECT menu_id, menu_name, parent_id, path, perms FROM sys_menu WHERE menu_name LIKE '%守则%' OR perms LIKE 'safety:rule%';
