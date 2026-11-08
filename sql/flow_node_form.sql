-- 流程节点表单模板：节点级表单设计（字段/权限/布局）落库，避免轻量修改触发流程重新部署
-- 一个流程模型的一个节点仅一条记录；发起节点(node_type=start)、审批节点(node_type=userTask)、结束节点(node_type=end)
CREATE TABLE IF NOT EXISTS `flow_node_form` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_id` varchar(64) NOT NULL COMMENT '流程模型ID',
  `model_key` varchar(100) NOT NULL COMMENT '流程模型Key（与部署后流程定义Key一致）',
  `node_id` varchar(100) NOT NULL COMMENT 'BPMN节点ID',
  `node_type` varchar(20) NOT NULL DEFAULT 'userTask' COMMENT 'start/userTask/end',
  `node_name` varchar(200) DEFAULT NULL COMMENT '节点名称',
  `form_name` varchar(200) DEFAULT NULL COMMENT '节点模板名称',
  `form_key` varchar(64) DEFAULT NULL COMMENT '引用的数据表表单Key',
  `fields_json` longtext COMMENT '字段快照 [{field,label,form,dtype,required,...}]',
  `layout_json` longtext COMMENT '布局快照 {mode:grid,grid:{...}}',
  `create_by` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_node` (`model_id`,`node_id`),
  KEY `idx_model_key` (`model_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点表单模板';
