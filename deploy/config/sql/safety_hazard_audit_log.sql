-- 隐患审批日志表
CREATE TABLE IF NOT EXISTS biz_safety_hazard_audit_log (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    hazard_id     BIGINT       NOT NULL                 COMMENT '隐患ID',
    audit_action  VARCHAR(2)   DEFAULT NULL             COMMENT '审批动作（1通过 2驳回 3提交 4开始整改 5整改提交 6验收通过 7验收不通过）',
    audit_by      VARCHAR(64)  DEFAULT NULL             COMMENT '审批人',
    audit_time    DATETIME     DEFAULT NULL             COMMENT '审批时间',
    audit_remark  VARCHAR(500) DEFAULT NULL             COMMENT '审批意见',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐患审批日志表';
