-- ----------------------------
-- 数据变更日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_change_log`;
CREATE TABLE `sys_data_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `entity_name` varchar(100) NOT NULL COMMENT '实体名称',
  `entity_id` varchar(50) NOT NULL COMMENT '实体ID',
  `field_name` varchar(100) DEFAULT NULL COMMENT '字段名称',
  `old_value` text COMMENT '旧值',
  `new_value` text COMMENT '新值',
  `change_type` varchar(20) NOT NULL COMMENT '变更类型 INSERT/UPDATE/DELETE',
  `operate_by` varchar(64) DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_entity_name_id` (`entity_name`,`entity_id`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据变更日志表';
