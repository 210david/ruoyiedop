-- =====================================================
-- 商机阶段变更日志表 mk_opportunity_stage_log
-- =====================================================
DROP TABLE IF EXISTS `mk_opportunity_stage_log`;
CREATE TABLE `mk_opportunity_stage_log` (
  `log_id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `opportunity_id`   BIGINT       NOT NULL                COMMENT '商机ID',
  `from_stage_code`   VARCHAR(50)  DEFAULT NULL            COMMENT '变更前阶段编码',
  `from_stage_name`   VARCHAR(100) DEFAULT NULL            COMMENT '变更前阶段名称',
  `to_stage_code`     VARCHAR(50)  DEFAULT NULL            COMMENT '变更后阶段编码',
  `to_stage_name`     VARCHAR(100) DEFAULT NULL            COMMENT '变更后阶段名称',
  `action_type`       VARCHAR(20)  DEFAULT NULL            COMMENT '操作类型(advance推进/retreat退回/win赢单/lose输单/reopen重开)',
  `operator_id`       BIGINT       DEFAULT NULL            COMMENT '操作人ID',
  `operator_name`    VARCHAR(50)  DEFAULT NULL            COMMENT '操作人名称',
  `operate_time`      DATETIME     DEFAULT NULL            COMMENT '操作时间',
  `remark`            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`log_id`),
  KEY `idx_opportunity_id` (`opportunity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机阶段变更日志';
