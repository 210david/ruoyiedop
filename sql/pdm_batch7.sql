-- ----------------------------
-- 技术变更审批记录表（参照采购申请审核日志 pms_request_audit_log）
-- 记录变更单每次审批动作（通过/驳回），支持审批历史时间轴展示
-- ----------------------------
DROP TABLE IF EXISTS pdm_change_audit_log;
CREATE TABLE pdm_change_audit_log (
  log_id        BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  change_id     BIGINT(20)   NOT NULL                COMMENT '变更单ID',
  audit_action  CHAR(1)      NOT NULL                COMMENT '审批动作（1通过 2驳回）',
  audit_by      VARCHAR(64)  DEFAULT ''              COMMENT '审批人',
  audit_time    DATETIME                             COMMENT '审批时间',
  audit_remark  VARCHAR(500) DEFAULT ''              COMMENT '审批意见',
  PRIMARY KEY (log_id),
  KEY idx_change_id (change_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术变更审批记录表';
