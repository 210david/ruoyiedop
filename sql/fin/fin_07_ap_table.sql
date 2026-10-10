-- ============================================================
-- 财务模块 · P3 应付管理（新增表）
-- 工程：RuoYi-Vue3
--
-- 【设计决策 · 与 PMS 付款的边界】
--   实测生产库发现 pms_invoice_payment 已有 7 条付款记录，
--   且 pms_invoice.payment_amount 已记录已付金额。
--   若财务侧再建一套付款登记，必然出现重复付款。
--
--   因此本设计明确分工：
--     · 财务应付单（fin_ap_order）—— 以 pms_invoice 为数据源，
--       只做「应付单据」与「核销」，不重复登记付款
--     · 付款事实仍以 pms_invoice_payment 为准，
--       财务通过只读扫描同步 payment_amount 到应付单 paid_amount
--     · fin_payment_record 保留（出纳登记备用场景），
--       但默认从 pms 同步，manual 来源才手工登记
--
--   核销（writeoff）= 财务确认付款并核销应付，
--   由财务经理操作并留痕，是会计动作而非出纳动作。
--
-- 【零侵入】不修改 pms_invoice / pms_invoice_payment 任何字段与代码。
--
-- 幂等：全部 WHERE NOT EXISTS，可重复执行
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_07_ap_table.sql
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一、应付单主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_ap_order;
CREATE TABLE fin_ap_order (
    ap_id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '应付单ID',
    company_id         BIGINT       DEFAULT 0                COMMENT '预留账套ID',

    ap_no              VARCHAR(32)  NOT NULL                COMMENT '应付单号（fin_ap_order 规则生成 AP+yyyyMMdd+4位）',
    source_module      VARCHAR(20)  NOT NULL DEFAULT 'pms'  COMMENT '来源模块 pms/manual',
    source_type        VARCHAR(30)  NOT NULL                COMMENT '来源类型 invoice/manual',
    source_id          BIGINT       NOT NULL DEFAULT 0       COMMENT '来源单据ID（pms_invoice.invoice_id）',
    source_key         VARCHAR(64)  DEFAULT NULL            COMMENT '来源单据号（发票号）',

    partner_id         BIGINT       DEFAULT NULL            COMMENT '往来单位ID（fin_partner.partner_id）',
    partner_name       VARCHAR(255) DEFAULT NULL            COMMENT '往来单位名称（冗余）',
    supplier_id        BIGINT       DEFAULT NULL            COMMENT '供应商ID（pms_invoice.supplier_id，只读引用）',

    ap_date            DATE         NOT NULL                COMMENT '应付日期（发票日期）',
    period             VARCHAR(6)   NOT NULL                COMMENT '会计期间（由 ap_date 派生）',

    amount_excl_tax    DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '不含税金额',
    tax_amount         DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '税额',
    total_amount       DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '价税合计',
    paid_amount        DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '已付金额（从 pms 同步）',

    tax_rate           DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '税率（自 pms_invoice.tax_rate 同步，10位4小数）',
    invoice_no         VARCHAR(64)  DEFAULT NULL            COMMENT '发票号码',
    invoice_code       VARCHAR(64)  DEFAULT NULL            COMMENT '发票代码',
    invoice_date       DATE         DEFAULT NULL            COMMENT '发票日期',

    due_date           DATE         DEFAULT NULL            COMMENT '到期日（按供应商账期推算）',
    payment_days       INT          NOT NULL DEFAULT 0     COMMENT '账期天数（自 wms_supplier.payment_days 同步）',

    status             VARCHAR(10)  NOT NULL DEFAULT 'OPEN' COMMENT '状态 OPEN未核销/PARTIAL部分核销/SETTLED已核销/VOID已作废',

    dept_id            BIGINT       DEFAULT NULL            COMMENT '归属部门ID（BizDeptFillAspect 自动填充）',
    user_id            BIGINT       DEFAULT NULL            COMMENT '制单人ID',

    del_flag           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志 0存在 2删除',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME     DEFAULT NULL            COMMENT '更新时间',
    remark             VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (ap_id),

    -- 幂等：同一来源单据只生成一张应付单
    UNIQUE KEY uk_ap_source (source_module, source_type, source_id, del_flag),
    KEY idx_ap_period (period),
    KEY idx_ap_partner (partner_id),
    KEY idx_ap_status (status),
    KEY idx_ap_due (due_date),
    KEY idx_ap_dept (dept_id),
    KEY idx_ap_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应付单表（FM-3 应付管理）';

-- ------------------------------------------------------------
-- 二、付款/收款流水（出纳登记）
--    与 pms_invoice_payment 的关系见文件头说明：
--    默认从 PMS 同步（source_module='pms'），
--    手工登记须显式 source_module='manual' 以便区分
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_payment_record;
CREATE TABLE fin_payment_record (
    payment_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '付款流水ID',
    company_id         BIGINT       DEFAULT 0                COMMENT '预留账套ID',

    payment_no         VARCHAR(32)  NOT NULL                COMMENT '付款单号（fin_payment 规则 FK+yyyyMMdd+4位）',
    source_module      VARCHAR(20)  NOT NULL DEFAULT 'pms'  COMMENT '来源模块 pms/manual',
    source_id          BIGINT       NOT NULL DEFAULT 0       COMMENT '来源流水ID（pms_invoice_payment.payment_id）',
    source_key         VARCHAR(64)  DEFAULT NULL            COMMENT '来源单号',

    pay_type            VARCHAR(10)  NOT NULL DEFAULT 'PAY' COMMENT '类型 PAY付款/CASH收款/REFUND退款',
    partner_id          BIGINT       DEFAULT NULL            COMMENT '往来单位ID',
    partner_name        VARCHAR(255) DEFAULT NULL            COMMENT '往来单位名称',

    ap_id               BIGINT       DEFAULT NULL            COMMENT '关联应付单ID',
    ar_id               BIGINT       DEFAULT NULL            COMMENT '关联应收单ID（应收共用本表）',

    amount              DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '付款金额',
    payment_date        DATE         NOT NULL                COMMENT '付款日期',
    period              VARCHAR(6)   NOT NULL                COMMENT '会计期间（由 payment_date 派生）',

    payment_method      VARCHAR(20)  NOT NULL DEFAULT '0'   COMMENT '付款方式（字典 wms_payment_method）',
    bank_name           VARCHAR(128) DEFAULT NULL            COMMENT '付款银行',
    bank_account_tail   VARCHAR(8)   DEFAULT NULL            COMMENT '银行账号尾号（仅尾号，不存全号）',

    voucher_id          BIGINT       DEFAULT NULL            COMMENT '关联付款凭证ID',
    status              VARCHAR(10)  NOT NULL DEFAULT 'CONFIRMED' COMMENT '状态 DRAFT待确认/CONFIRMED已确认/VOID已作废',

    dept_id             BIGINT       DEFAULT NULL            COMMENT '归属部门ID',
    user_id             BIGINT       DEFAULT NULL            COMMENT '登记人ID',

    del_flag            CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志 0存在 2删除',
    create_by           VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by           VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME     DEFAULT NULL            COMMENT '更新时间',
    remark              VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (payment_id),

    -- 幂等：从 PMS 同步时按来源去重
    UNIQUE KEY uk_payment_source (source_module, source_id, del_flag),
    KEY idx_payment_period (period),
    KEY idx_payment_partner (partner_id),
    KEY idx_payment_ap (ap_id),
    KEY idx_payment_date (payment_date),
    KEY idx_payment_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款流水表（出纳登记，PMS 同步或手工登记）';

-- ------------------------------------------------------------
-- 三、核销记录（财务经理操作，留痕）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_ap_writeoff;
CREATE TABLE fin_ap_writeoff (
    writeoff_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '核销ID',
    ap_id              BIGINT       NOT NULL                COMMENT '应付单ID',
    payment_id         BIGINT       DEFAULT NULL            COMMENT '付款流水ID（手工登记时关联）',

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

    KEY idx_writeoff_ap (ap_id),
    KEY idx_writeoff_date (writeoff_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应付核销记录表（财务经理操作留痕）';

-- ------------------------------------------------------------
-- 四、进项税明细（进项税额抵扣台账）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_input_tax;
CREATE TABLE fin_input_tax (
    tax_id             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '进项税ID',
    company_id         BIGINT       DEFAULT 0                COMMENT '预留账套ID',

    ap_id              BIGINT       DEFAULT NULL            COMMENT '应付单ID',
    partner_id         BIGINT       DEFAULT NULL            COMMENT '往来单位ID（供应商）',
    partner_name       VARCHAR(255) DEFAULT NULL            COMMENT '供应商名称',

    invoice_code       VARCHAR(64)  DEFAULT NULL            COMMENT '发票代码',
    invoice_no         VARCHAR(64)  DEFAULT NULL            COMMENT '发票号码',
    invoice_date       DATE         NOT NULL                COMMENT '开票日期',

    tax_rate           DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '税率',
    amount_excl_tax    DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '不含税金额',
    tax_amount         DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '进项税额',

    deduct_status      VARCHAR(10)  NOT NULL DEFAULT 'PENDING' COMMENT '抵扣状态 PENDING待抵扣/DEDUCTED已抵扣/EXPIRED已过期/VOID已作废',
    deduct_period      VARCHAR(6)   DEFAULT NULL            COMMENT '抵扣所属期间',
    voucher_id         BIGINT       DEFAULT NULL            COMMENT '关联进项税凭证ID',

    dept_id            BIGINT       DEFAULT NULL            COMMENT '归属部门ID',
    user_id            BIGINT       DEFAULT NULL            COMMENT '经办人ID',

    del_flag           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志 0存在 2删除',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME     DEFAULT NULL            COMMENT '更新时间',
    remark             VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (tax_id),

    KEY idx_tax_ap (ap_id),
    KEY idx_tax_partner (partner_id),
    KEY idx_tax_date (invoice_date),
    KEY idx_tax_deduct (deduct_status),
    KEY idx_tax_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进项税明细表（进项税额抵扣台账）';

-- ------------------------------------------------------------
-- 五、字典：进项税抵扣状态
-- ------------------------------------------------------------
INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 1, '待抵扣', 'PENDING', 'fin_deduct_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '进项税抵扣状态（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_deduct_status' AND dict_value='PENDING');

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 2, '已抵扣', 'DEDUCTED', 'fin_deduct_status', '', 'success', 'N', '0', 'admin', sysdate(), '进项税抵扣状态（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_deduct_status' AND dict_value='DEDUCTED');

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 3, '已过期', 'EXPIRED', 'fin_deduct_status', '', 'info', 'N', '0', 'admin', sysdate(), '超过抵扣期限（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_deduct_status' AND dict_value='EXPIRED');

-- ------------------------------------------------------------
-- 验证
-- ------------------------------------------------------------
SELECT table_name, table_comment FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('fin_ap_order','fin_payment_record','fin_ap_writeoff','fin_input_tax')
ORDER BY table_name;

SELECT dict_value, dict_label FROM sys_dict_data
WHERE dict_type = 'fin_deduct_status' ORDER BY dict_sort;