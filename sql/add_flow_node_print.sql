-- ============================================================
-- 流程节点打印模板 flow_node_print
--
-- 对标泛微：打印按钮不再固定打印「当前节点显示模板」，
-- 每个节点可以单独配置一个打印模板；未配置时自动回退到该节点的显示模板。
--
-- 模板内容为「表单库中某张表单」的字段 + 布局快照：
-- 配置时把 fields_json / layout_json 复制一份存下来，
-- 之后表单库改版不会影响已配置节点（与节点表单模板同一口径）。
--
-- 2026-11-10
-- ============================================================

CREATE TABLE IF NOT EXISTS `flow_node_print` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_id` varchar(64) NOT NULL COMMENT '流程模型ID',
  `model_key` varchar(100) NOT NULL COMMENT '流程模型Key（与部署后流程定义Key一致）',
  `node_id` varchar(100) NOT NULL COMMENT 'BPMN节点ID',
  `node_name` varchar(200) DEFAULT NULL COMMENT '节点名称',
  `template_form_id` varchar(64) DEFAULT NULL COMMENT '打印模板来源表单ID（flow_form.form_id）',
  `template_name` varchar(200) DEFAULT NULL COMMENT '打印模板名称',
  `fields_json` longtext COMMENT '打印模板字段快照',
  `layout_json` longtext COMMENT '打印模板布局快照',
  `create_by` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_node` (`model_id`, `node_id`),
  KEY `idx_model_key` (`model_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程节点打印模板';
