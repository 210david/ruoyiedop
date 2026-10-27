-- =====================================================
-- 操作撤销日志表（数据恢复支持）
-- 说明：在支持撤销的操作执行前保存数据快照，允许在有效期内撤销操作
-- =====================================================
drop table if exists sys_undo_log;
CREATE TABLE `sys_undo_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `entity_type` varchar(100) NOT NULL COMMENT '实体类型（表名）',
  `entity_id` varchar(50) NOT NULL COMMENT '实体ID',
  `data_snapshot` longtext COMMENT '数据快照(JSON格式)',
  `operate_type` varchar(20) NOT NULL COMMENT '操作类型（INSERT/UPDATE/DELETE）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '操作人',
  `create_time` datetime DEFAULT NULL COMMENT '操作时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间(默认保留24小时)',
  PRIMARY KEY (`id`),
  KEY `idx_entity` (`entity_type`,`entity_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='操作撤销日志表';
