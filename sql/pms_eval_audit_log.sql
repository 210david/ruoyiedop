-- =============================================
-- 供应商评估审核日志表
-- 记录每次审批操作（通过/驳回），支持完整审核历程展示
-- =============================================
CREATE TABLE IF NOT EXISTS `pms_supplier_eval_audit_log` (
  `log_id`        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `eval_id`       BIGINT       NOT NULL                 COMMENT '评估ID',
  `audit_action`  VARCHAR(2)   NOT NULL                 COMMENT '审核动作（1通过 2驳回）',
  `audit_by`      VARCHAR(64)  NOT NULL                 COMMENT '审核人',
  `audit_time`    DATETIME     NOT NULL                 COMMENT '审核时间',
  `audit_remark`  VARCHAR(500) DEFAULT NULL             COMMENT '审核意见',
  PRIMARY KEY (`log_id`),
  KEY `idx_eval_id` (`eval_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商评估审核日志';
