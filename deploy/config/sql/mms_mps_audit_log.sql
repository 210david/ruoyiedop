-- 主生产计划审核日志表
-- 用于记录MPS审批的完整审核历程（通过/驳回）
DROP TABLE IF EXISTS `mms_mps_audit_log`;
CREATE TABLE `mms_mps_audit_log` (
  `log_id`        BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `mps_id`        BIGINT(20)    NOT NULL                 COMMENT '主计划ID',
  `audit_action`  VARCHAR(10)   NOT NULL                 COMMENT '审核动作（1通过 2驳回）',
  `audit_by`      VARCHAR(64)   DEFAULT ''               COMMENT '审核人',
  `audit_time`    DATETIME      DEFAULT NULL             COMMENT '审核时间',
  `audit_remark`  VARCHAR(500)  DEFAULT NULL             COMMENT '审核意见',
  PRIMARY KEY (`log_id`),
  KEY `idx_mps_id` (`mps_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='主生产计划审核日志表';
