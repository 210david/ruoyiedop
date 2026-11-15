-- =============================================
-- AI 流程生成（上传流程方案 → AI 生成表单与流程草稿）
-- 日期：2026-11-15
-- 说明：生成结果默认仅保存为草稿（不自动部署），由管理员确认后自行部署
-- =============================================
SET NAMES utf8mb4;

-- ========== 1. 任务表（幂等） ==========
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

-- ========== 2. 字典：AI流程生成状态 ==========
DELETE FROM sys_dict_data WHERE dict_type = 'flow_ai_gen_status';
DELETE FROM sys_dict_type WHERE dict_type = 'flow_ai_gen_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('AI流程生成状态', 'flow_ai_gen_status', '0', 'admin', sysdate(), 'AI流程生成任务状态');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '解析中', '0', 'flow_ai_gen_status', '', 'info',    'Y', '0', 'admin', sysdate(), 'AI正在解析流程方案'),
(2, '待确认', '1', 'flow_ai_gen_status', '', 'warning', 'N', '0', 'admin', sysdate(), 'AI解析完成，等待人工确认后生成'),
(3, '已生成', '2', 'flow_ai_gen_status', '', 'success', 'N', '0', 'admin', sysdate(), '已生成流程表单与流程模型草稿'),
(4, '失败',   '3', 'flow_ai_gen_status', '', 'danger',  'N', '0', 'admin', sysdate(), '解析或生成失败');

-- ========== 3. 菜单（挂在流程管理下） ==========
SET @fm = (SELECT menu_id FROM sys_menu WHERE menu_name = '流程管理' AND parent_id = 1 LIMIT 1);

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

-- ========== 4. 角色授权：与「流程模型」保持同等范围 + 超级管理员 ==========
SET @aiMenu    = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:aigen:list' LIMIT 1);
SET @aiQuery   = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:aigen:query' LIMIT 1);
SET @aiAnalyze = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:aigen:analyze' LIMIT 1);
SET @aiGen     = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:aigen:generate' LIMIT 1);
SET @aiRemove  = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:aigen:remove' LIMIT 1);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, x.menu_id FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id
CROSS JOIN (SELECT @aiMenu AS menu_id UNION ALL SELECT @aiQuery UNION ALL SELECT @aiAnalyze UNION ALL SELECT @aiGen UNION ALL SELECT @aiRemove) x
WHERE m.perms = 'flowable:model:list';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, @aiMenu), (1, @aiQuery), (1, @aiAnalyze), (1, @aiGen), (1, @aiRemove);

-- ========== 5. 校验 ==========
SELECT menu_id, menu_name, parent_id, order_num, path, component, perms
FROM sys_menu WHERE perms LIKE 'flowable:aigen%' ORDER BY order_num;

SELECT dict_type, dict_label, dict_value, status FROM sys_dict_data
WHERE dict_type = 'flow_ai_gen_status' ORDER BY dict_sort;

-- =============================================
-- 回滚：
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'flowable:aigen%');
-- DELETE FROM sys_menu WHERE perms LIKE 'flowable:aigen%';
-- DELETE FROM sys_dict_data WHERE dict_type = 'flow_ai_gen_status';
-- DELETE FROM sys_dict_type WHERE dict_type = 'flow_ai_gen_status';
-- DROP TABLE IF EXISTS flow_ai_gen_task;
-- =============================================
