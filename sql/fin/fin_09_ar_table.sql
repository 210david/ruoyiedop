-- ============================================================
-- 财务模块 · P4 应收管理（新增表）
--
-- 【边界 · 与 MK 收款记录的分工】参照 P3 应付的设计
--   实测生产库：mk_payment_record 已有 7 条收款记录（合计 140 万元），
--   通过 plan_id 关联 mk_payment_plan，且有 confirm_status 确认状态。
--   若财务侧再建一套收款登记，必然重复收款。
--
--   分工：
--     · 应收单（fin_ar_order）—— 以 mk_shipment 为数据源生成
--     · 收款事实仍以 mk_payment_record 为准，财务只读同步
--     · 核销（fin_ar_writeoff）—— 财务经理确认销账，与出纳收款分开留痕
--
-- 【重要 · 税额拆分】实测 mk_shipment 只有 total_amount（价税合计），
--   没有税额与不含税金额字段。故 ar_amount_excl_tax / ar_tax_amount
--   由本表按 tax_rate 拆分计算，tax_rate 取自 fin_fiscal_config 的
--   默认销售税率，可在财务配置中调整。
--
-- 幂等：全部 WHERE NOT EXISTS，可重复执行
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_09_ar_table.sql
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一、应收单主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_ar_order;
CREATE TABLE fin_ar_order (
    ar_id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '应收单ID',
    company_id         BIGINT       DEFAULT 0                COMMENT '预留账套ID',

    ar_no              VARCHAR(32)  NOT NULL                COMMENT '应收单号（AR+yyyyMMdd+4位）',
    source_module      VARCHAR(20)  NOT NULL DEFAULT 'mk'   COMMENT '来源模块 mk/manual',
    source_type        VARCHAR(30)  NOT NULL                COMMENT '来源类型 shipment/manual',
    source_id          BIGINT       NOT NULL DEFAULT 0       COMMENT '来源单据ID（mk_shipment.shipment_id）',
    source_key         VARCHAR(64)  DEFAULT NULL            COMMENT '来源单据号（发货单号）',

    partner_id         BIGINT       DEFAULT NULL            COMMENT '往来单位ID（fin_partner）',
    partner_name       VARCHAR(255) DEFAULT NULL            COMMENT '客户名称（冗余）',
    customer_id        BIGINT       DEFAULT NULL            COMMENT '客户ID（mk_shipment.customer_id，只读引用）',

    ar_date            DATE         NOT NULL                COMMENT '应收日期（发货日期）',
    period             VARCHAR(6)   NOT NULL                COMMENT '会计期间（由 ar_date 派生）',

    amount_excl_tax    DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '不含税金额（由total_amount 按税率拆分）',
    tax_amount         DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '销项税额',
    total_amount       DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '价税合计（= mk_shipment.total_amount）',
    received_amount    DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '已收金额（从 mk_payment_record 同步）',
    bad_debt_amount    DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '已计提坏账',

    tax_rate           DECIMAL(10,4) NOT NULL DEFAULT 0.1300 COMMENT '税率（默认13%，可配置）',

    -- 销项发票信息（应收开票后回填）
    invoice_no         VARCHAR(64)  DEFAULT NULL            COMMENT '销项发票号码',
    invoice_code       VARCHAR(64)  DEFAULT NULL            COMMENT '销项发票代码',
    invoice_date       DATE         DEFAULT NULL            COMMENT '开票日期',

    due_date           DATE         DEFAULT NULL            COMMENT '到期日（按客户账期推算）',
    payment_days       INT          NOT NULL DEFAULT 0     COMMENT '账期天数',

    status             VARCHAR(10)  NOT NULL DEFAULT 'OPEN' COMMENT '状态 OPEN未核销/PARTIAL部分核销/SETTLED已核销/VOID已作废',

    dept_id            BIGINT       DEFAULT NULL            COMMENT '归属部门ID',
    user_id            BIGINT       DEFAULT NULL            COMMENT '制单人ID',

    del_flag           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志 0存在 2删除',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME     DEFAULT NULL            COMMENT '更新时间',
    remark             VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (ar_id),

    -- 幂等：同一发货单只生成一张应收单
    UNIQUE KEY uk_ar_source (source_module, source_type, source_id, del_flag),
    KEY idx_ar_period (period),
    KEY idx_ar_partner (partner_id),
    KEY idx_ar_status (status),
    KEY idx_ar_due (due_date),
    KEY idx_ar_dept (dept_id),
    KEY idx_ar_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收单表（FM-2 应收管理）';

-- ------------------------------------------------------------
-- 二、收款核销记录
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_ar_writeoff;
CREATE TABLE fin_ar_writeoff (
    writeoff_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '核销ID',
    ar_id              BIGINT       NOT NULL                COMMENT '应收单ID',
    receipt_id         BIGINT       DEFAULT NULL            COMMENT '收款记录ID（mk_payment_record.record_id）',

    writeoff_amount    DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '本次核销金额',
    writeoff_date      DATE         NOT NULL                COMMENT '核销日期',
    period             VARCHAR(6)   NOT NULL                COMMENT '会计期间',

    dept_id            BIGINT       DEFAULT NULL            COMMENT '归属部门ID',
    user_id            BIGINT       DEFAULT NULL            COMMENT '核销人ID',
    del_flag           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志 0存在 2删除',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '核销人',
    create_time        DATETIME     DEFAULT NULL            COMMENT '核销时间',
    remark             VARCHAR(500) DEFAULT NULL            COMMENT '核销备注',
    PRIMARY KEY (writeoff_id),

    KEY idx_arw_ar (ar_id),
    KEY idx_arw_receipt (receipt_id),
    KEY idx_arw_date (writeoff_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收核销记录表（财务经理操作留痕）';

-- ------------------------------------------------------------
-- 三、坏账计提台账
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_bad_debt;
CREATE TABLE fin_bad_debt (
    bad_debt_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '坏账计提ID',
    company_id         BIGINT       DEFAULT 0                COMMENT '预留账套ID',

    provision_no       VARCHAR(32)  NOT NULL                COMMENT '计提单号（fin_bad_debt 规则）',
    period             VARCHAR(6)   NOT NULL                COMMENT '计提所属期间',

    ar_id              BIGINT       DEFAULT NULL            COMMENT '关联应收单ID（单笔计提）',
    partner_id         BIGINT       DEFAULT NULL            COMMENT '客户ID',
    partner_name       VARCHAR(255) DEFAULT NULL            COMMENT '客户名称',

    receivable_amount  DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '计提基数（应收账款余额）',
    provision_ratio    DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '计提比例（如 0.0500=5%）',
    provision_amount   DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '本次计提金额',

    provision_type     VARCHAR(20)  NOT NULL DEFAULT 'PERIOD' COMMENT '计提方式 PERIOD期末按比例/AGE按账龄/SPECIFIC单项',

    dept_id            BIGINT       DEFAULT NULL            COMMENT '归属部门ID',
    user_id            BIGINT       DEFAULT NULL            COMMENT '计提人ID',

    del_flag           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志 0存在 2删除',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME     DEFAULT NULL            COMMENT '创建时间',
    remark             VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (bad_debt_id),

    UNIQUE KEY uk_bd_no (provision_no),
    KEY idx_bd_period (period),
    KEY idx_bd_ar (ar_id),
    KEY idx_bd_partner (partner_id),
    KEY idx_bd_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='坏账计提台账';

-- ------------------------------------------------------------
-- 四、财务配置：默认销售税率
-- ------------------------------------------------------------
INSERT INTO fin_fiscal_config (config_key, config_value, config_name, config_group, create_by, create_time, remark)
SELECT 'output_tax_rate', '0.13', '默认销售税率', 'tax', 'admin', sysdate(), '应收单按此税率拆分不含税金额与销项税额（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM fin_fiscal_config WHERE config_key='output_tax_rate');

INSERT INTO fin_fiscal_config (config_key, config_value, config_name, config_group, create_by, create_time, remark)
SELECT 'ar_aging_ratio', '{"1-30":0.05,"31-60":0.10,"61-90":0.30,"90+":0.50}', '应收账款账龄计提比例', 'bad_debt', 'admin', sysdate(), '按账龄区间计提坏账的比例（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM fin_fiscal_config WHERE config_key='ar_aging_ratio');

-- ------------------------------------------------------------
-- 验证
-- ------------------------------------------------------------
SELECT table_name, table_comment FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('fin_ar_order','fin_ar_writeoff','fin_bad_debt')
ORDER BY table_name;

SELECT config_key, config_value, config_name FROM fin_fiscal_config
WHERE config_key IN ('output_tax_rate','ar_aging_ratio');