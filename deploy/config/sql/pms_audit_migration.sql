-- ============================================================
-- 采购管理模块 - 审核功能数据库迁移脚本
-- 为询比价、采购合同、采购订单、收货验收、退货管理、发票结算增加审核功能
-- ============================================================

-- ============================================================
-- 1. 询比价管理 (pms_inquiry)
-- ============================================================
-- 增加审核字段
ALTER TABLE pms_inquiry ADD COLUMN audit_by VARCHAR(64) DEFAULT NULL COMMENT '审核人';
ALTER TABLE pms_inquiry ADD COLUMN audit_time DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE pms_inquiry ADD COLUMN audit_opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见';

-- 创建询比价审核日志表
CREATE TABLE IF NOT EXISTS pms_inquiry_audit_log (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    inquiry_id    BIGINT       NOT NULL                COMMENT '询价ID',
    audit_action  VARCHAR(1)   DEFAULT NULL            COMMENT '审核动作（1通过 2驳回）',
    audit_by      VARCHAR(64)  DEFAULT NULL            COMMENT '审核人',
    audit_time    DATETIME     DEFAULT NULL            COMMENT '审核时间',
    audit_remark  VARCHAR(500) DEFAULT NULL            COMMENT '审核意见',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询比价审核日志';

-- ============================================================
-- 2. 采购合同 (pms_contract)
-- ============================================================
-- 增加审核字段
ALTER TABLE pms_contract ADD COLUMN audit_by VARCHAR(64) DEFAULT NULL COMMENT '审核人';
ALTER TABLE pms_contract ADD COLUMN audit_time DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE pms_contract ADD COLUMN audit_opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见';

-- 创建采购合同审核日志表
CREATE TABLE IF NOT EXISTS pms_contract_audit_log (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    contract_id   BIGINT       NOT NULL                COMMENT '合同ID',
    audit_action  VARCHAR(1)   DEFAULT NULL            COMMENT '审核动作（1通过 2驳回）',
    audit_by      VARCHAR(64)  DEFAULT NULL            COMMENT '审核人',
    audit_time    DATETIME     DEFAULT NULL            COMMENT '审核时间',
    audit_remark  VARCHAR(500) DEFAULT NULL            COMMENT '审核意见',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购合同审核日志';

-- ============================================================
-- 3. 采购订单 (pms_purchase_order)
-- 注：pms_purchase_order 已有 audit_by, audit_time, audit_opinion 字段
-- ============================================================
-- 创建采购订单审核日志表
CREATE TABLE IF NOT EXISTS pms_order_audit_log (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    order_id      BIGINT       NOT NULL                COMMENT '订单ID',
    audit_action  VARCHAR(1)   DEFAULT NULL            COMMENT '审核动作（1通过 2驳回）',
    audit_by      VARCHAR(64)  DEFAULT NULL            COMMENT '审核人',
    audit_time    DATETIME     DEFAULT NULL            COMMENT '审核时间',
    audit_remark  VARCHAR(500) DEFAULT NULL            COMMENT '审核意见',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单审核日志';

-- ============================================================
-- 4. 收货验收 (pms_receive)
-- ============================================================
-- 增加审核字段
ALTER TABLE pms_receive ADD COLUMN audit_by VARCHAR(64) DEFAULT NULL COMMENT '审核人';
ALTER TABLE pms_receive ADD COLUMN audit_time DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE pms_receive ADD COLUMN audit_opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见';

-- 创建收货验收审核日志表
CREATE TABLE IF NOT EXISTS pms_receive_audit_log (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    receive_id    BIGINT       NOT NULL                COMMENT '收货ID',
    audit_action  VARCHAR(1)   DEFAULT NULL            COMMENT '审核动作（1通过 2驳回）',
    audit_by      VARCHAR(64)  DEFAULT NULL            COMMENT '审核人',
    audit_time    DATETIME     DEFAULT NULL            COMMENT '审核时间',
    audit_remark  VARCHAR(500) DEFAULT NULL            COMMENT '审核意见',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货验收审核日志';

-- ============================================================
-- 5. 退货管理 (pms_return)
-- ============================================================
-- 增加审核字段
ALTER TABLE pms_return ADD COLUMN audit_by VARCHAR(64) DEFAULT NULL COMMENT '审核人';
ALTER TABLE pms_return ADD COLUMN audit_time DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE pms_return ADD COLUMN audit_opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见';

-- 创建退货审核日志表
CREATE TABLE IF NOT EXISTS pms_return_audit_log (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    return_id     BIGINT       NOT NULL                COMMENT '退货ID',
    audit_action  VARCHAR(1)   DEFAULT NULL            COMMENT '审核动作（1通过 2驳回）',
    audit_by      VARCHAR(64)  DEFAULT NULL            COMMENT '审核人',
    audit_time    DATETIME     DEFAULT NULL            COMMENT '审核时间',
    audit_remark  VARCHAR(500) DEFAULT NULL            COMMENT '审核意见',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货审核日志';

-- ============================================================
-- 6. 发票结算 (pms_invoice)
-- ============================================================
-- 增加审核字段
ALTER TABLE pms_invoice ADD COLUMN audit_by VARCHAR(64) DEFAULT NULL COMMENT '审核人';
ALTER TABLE pms_invoice ADD COLUMN audit_time DATETIME DEFAULT NULL COMMENT '审核时间';
ALTER TABLE pms_invoice ADD COLUMN audit_opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见';

-- 创建发票结算审核日志表
CREATE TABLE IF NOT EXISTS pms_invoice_audit_log (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    invoice_id    BIGINT       NOT NULL                COMMENT '发票ID',
    audit_action  VARCHAR(1)   DEFAULT NULL            COMMENT '审核动作（1通过 2驳回）',
    audit_by      VARCHAR(64)  DEFAULT NULL            COMMENT '审核人',
    audit_time    DATETIME     DEFAULT NULL            COMMENT '审核时间',
    audit_remark  VARCHAR(500) DEFAULT NULL            COMMENT '审核意见',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票结算审核日志';
