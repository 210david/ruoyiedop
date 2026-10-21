-- 合同审批记录表
CREATE TABLE IF NOT EXISTS mk_contract_approve_log (
    log_id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    contract_id     BIGINT        NOT NULL                 COMMENT '合同ID',
    action_type     VARCHAR(10)   NOT NULL                 COMMENT '操作类型（1提交审批 2审批通过 3审批驳回 4终止）',
    approve_opinion VARCHAR(500)                           COMMENT '审批意见',
    approve_by      VARCHAR(64)                            COMMENT '审批人',
    approve_time    DATETIME                               COMMENT '审批时间',
    PRIMARY KEY (log_id),
    KEY idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同审批记录表';
