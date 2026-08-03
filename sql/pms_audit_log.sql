-- =============================================
-- 采购计划审核日志表
-- 记录每次审批操作（通过/驳回），支持完整审核历程展示
-- =============================================
CREATE TABLE IF NOT EXISTS `pms_plan_audit_log` (
  `log_id`        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `plan_id`       BIGINT       NOT NULL                 COMMENT '计划ID',
  `audit_action`  VARCHAR(2)   NOT NULL                 COMMENT '审核动作（2通过 6驳回）',
  `audit_by`      VARCHAR(64)  NOT NULL                 COMMENT '审核人',
  `audit_time`    DATETIME     NOT NULL                 COMMENT '审核时间',
  `audit_remark`  VARCHAR(500) DEFAULT NULL             COMMENT '审核意见',
  PRIMARY KEY (`log_id`),
  KEY `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购计划审核日志';

-- =============================================
-- 采购申请审核日志表
-- 记录每次审批操作（通过/驳回），支持完整审核历程展示
-- =============================================
CREATE TABLE IF NOT EXISTS `pms_request_audit_log` (
  `log_id`        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `request_id`    BIGINT       NOT NULL                 COMMENT '申请ID',
  `audit_action`  VARCHAR(2)   NOT NULL                 COMMENT '审核动作（2通过 6驳回）',
  `audit_by`      VARCHAR(64)  NOT NULL                 COMMENT '审核人',
  `audit_time`    DATETIME     NOT NULL                 COMMENT '审核时间',
  `audit_remark`  VARCHAR(500) DEFAULT NULL             COMMENT '审核意见',
  PRIMARY KEY (`log_id`),
  KEY `idx_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请审核日志';
