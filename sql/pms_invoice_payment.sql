-- =============================================
-- 发票多次付款功能数据库迁移
-- 1. 创建发票付款记录表 pms_invoice_payment
-- 2. pms_contract 增加 paid_amount 字段
-- 3. pms_purchase_order 增加 paid_amount 字段
-- 4. pms_invoice 增加 pay_amount 字段（如不存在）
-- =============================================

-- 1. 发票付款记录表
CREATE TABLE IF NOT EXISTS pms_invoice_payment (
    payment_id       BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '付款记录ID',
    invoice_id       BIGINT        NOT NULL                 COMMENT '发票ID',
    payment_amount   DECIMAL(14,2) NOT NULL DEFAULT 0       COMMENT '本次付款金额',
    payment_date     DATE                                   COMMENT '付款日期',
    payment_method   VARCHAR(20)   DEFAULT '0'              COMMENT '付款方式（字典 wms_payment_method）',
    bank_name        VARCHAR(128)                           COMMENT '开户银行',
    bank_account     VARCHAR(64)                            COMMENT '银行账号',
    payer            VARCHAR(64)                            COMMENT '付款人',
    del_flag         CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
    create_by        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                               COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                               COMMENT '更新时间',
    remark           VARCHAR(500)                           COMMENT '备注',
    PRIMARY KEY (payment_id),
    KEY idx_payment_invoice (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票付款记录表';

-- 2. 合同表增加已付金额字段
ALTER TABLE pms_contract ADD COLUMN paid_amount DECIMAL(14,2) DEFAULT 0 COMMENT '已付金额' AFTER contract_amount;

-- 3. 采购订单表增加已付金额字段
ALTER TABLE pms_purchase_order ADD COLUMN paid_amount DECIMAL(14,2) DEFAULT 0 COMMENT '已付金额' AFTER received_amount;

-- 4. 发票表增加付款金额字段（本次付款金额）
-- 注意：MySQL 8.0.28 不支持 ADD COLUMN IF NOT EXISTS，如已存在请跳过
-- ALTER TABLE pms_invoice ADD COLUMN pay_amount DECIMAL(14,2) DEFAULT 0 COMMENT '本次付款金额' AFTER payment_amount;
