-- =============================================================
-- 线索操作日志表 mk_lead_log
-- 记录线索的完整操作历史（创建、领取申请、审批通过/退回、分配、退回公海、转化、无效等）
-- =============================================================

DROP TABLE IF EXISTS `mk_lead_log`;
CREATE TABLE `mk_lead_log` (
  `log_id`          BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
  `lead_id`         BIGINT(20)   NOT NULL                 COMMENT '线索ID',
  `action_type`     VARCHAR(32)  NOT NULL                 COMMENT '操作类型（create/apply/approve/reject/assign/release/convert/invalidate/status_change）',
  `action_desc`     VARCHAR(500) DEFAULT NULL             COMMENT '操作描述',
  `operator_id`     BIGINT(20)   DEFAULT NULL             COMMENT '操作人ID',
  `operator_name`   VARCHAR(64)  DEFAULT NULL             COMMENT '操作人名称',
  `operate_time`    DATETIME     DEFAULT NULL             COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_lead_id` (`lead_id`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索操作日志表';
