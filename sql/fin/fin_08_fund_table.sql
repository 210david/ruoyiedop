-- ============================================================
-- 财务模块 · P5 资金管理（新增表与字典）
--
-- 【边界】fin_bank_account 与 fin_cash_flow 是财务模块全新表，
--   不与任何既有模块重叠，是唯一无边界纠纷的模块。
--   PMS 的 pms_invoice_payment 与 MK 的 mk_payment_record 均为采购/销售侧单据，
--   财务侧通过只读同步生成资金流水，不重复登记付款。
--
-- 幂等：全部 WHERE NOT EXISTS，可重复执行
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_08_fund_table.sql
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一、对账表：银行流水导入与账实核对
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_bank_statement;
CREATE TABLE fin_bank_statement (
    statement_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '对账单ID',
    company_id         BIGINT       DEFAULT 0                COMMENT '预留账套ID',

    account_id         BIGINT       NOT NULL                COMMENT '资金账户ID',
    account_name       VARCHAR(100) DEFAULT NULL            COMMENT '账户名称（冗余）',

    statement_date     DATE         NOT NULL                COMMENT '对账日期',
    period             VARCHAR(6)   NOT NULL                COMMENT '会计期间',

    begin_balance      DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '账面期初余额',
    bank_begin_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00  COMMENT '银行期初余额',
    bank_end_balance   DECIMAL(14,2) DEFAULT NULL            COMMENT '银行期末余额',
    book_end_balance   DECIMAL(14,2) DEFAULT NULL            COMMENT '账面期末余额',

    diff_amount        DECIMAL(14,2) DEFAULT NULL            COMMENT '差异金额（银行-账面）',

    status             VARCHAR(10)  NOT NULL DEFAULT 'IMPORTED' COMMENT '状态 IMPORTED已导入/RECONCILED已对平/DIFF有差异',

    file_name          VARCHAR(255) DEFAULT NULL            COMMENT '导入文件名',
    import_by          VARCHAR(64)  DEFAULT NULL            COMMENT '导入人',
    import_time        DATETIME     DEFAULT NULL            COMMENT '导入时间',

    dept_id            BIGINT       DEFAULT NULL            COMMENT '归属部门ID',
    user_id            BIGINT       DEFAULT NULL            COMMENT '上传人ID',

    del_flag           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志 0存在 2删除',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME     DEFAULT NULL            COMMENT '更新时间',
    remark             VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (statement_id),

    KEY idx_stmt_account (account_id),
    KEY idx_stmt_date (statement_date),
    KEY idx_stmt_status (status),
    KEY idx_stmt_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行对账单表（资金对账）';

-- ------------------------------------------------------------
-- 二、对账单明细（银行流水行）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS fin_bank_statement_item;
CREATE TABLE fin_bank_statement_item (
    item_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    statement_id       BIGINT       NOT NULL                COMMENT '对账单ID',

    trans_date         DATE         NOT NULL                COMMENT '交易日期',
    trans_type         VARCHAR(20)  DEFAULT NULL            COMMENT '交易类型 收入/支出/转账/手续费/利息',
    trans_amount       DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '交易金额（支出为负，收入为正）',
    balance_after      DECIMAL(14,2) DEFAULT NULL            COMMENT '交易后余额',
    counterparty       VARCHAR(255) DEFAULT NULL            COMMENT '对方单位',
    counterparty_account VARCHAR(64) DEFAULT NULL          COMMENT '对方账号尾号',
    summary            VARCHAR(500) DEFAULT NULL            COMMENT '摘要',
    voucher_no         VARCHAR(64)  DEFAULT NULL            COMMENT '银行流水号',

    -- 匹配到的财务流水（对账勾稽用）
    flow_id            BIGINT       DEFAULT NULL            COMMENT '匹配的财务流水ID',
    match_status       CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '匹配状态 0未匹配 1已匹配 2人工确认',

    del_flag           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志 0存在 2删除',
    create_time        DATETIME     DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (item_id),

    KEY idx_stmt_item_stmt (statement_id),
    KEY idx_stmt_item_date (trans_date),
    KEY idx_stmt_item_flow (flow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行对账单明细表';

-- ------------------------------------------------------------
-- 三、字典：资金账户类型
-- ------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '银行账户', 'BANK', 'fin_account_type', '', 'primary', 'N', '0', 'admin', sysdate(), '资金账户类型（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_account_type' AND dict_value='BANK');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '现金', 'CASH', 'fin_account_type', '', 'success', 'Y', '0', 'admin', sysdate(), '资金账户类型（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_account_type' AND dict_value='CASH');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '第三方支付', 'THIRD_PARTY', 'fin_account_type', '', 'warning', 'N', '0', 'admin', sysdate(), '支付宝/微信等（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_account_type' AND dict_value='THIRD_PARTY');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '银行承兑汇票', 'NOTE', 'fin_account_type', '', 'info', 'N', '0', 'admin', sysdate(), '票据保证金账户（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_account_type' AND dict_value='NOTE');

-- ------------------------------------------------------------
-- 四、字典：资金流水业务类型
-- ------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '收款', 'IN', 'fin_flow_biz_type', '', 'success', 'N', '0', 'admin', sysdate(), '资金流水业务类型（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_flow_biz_type' AND dict_value='IN');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '付款', 'OUT', 'fin_flow_biz_type', '', 'danger', 'N', '0', 'admin', sysdate(), '资金流水业务类型（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_flow_biz_type' AND dict_value='OUT');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '银行转账', 'TRANSFER', 'fin_flow_biz_type', '', 'primary', 'N', '0', 'admin', sysdate(), '内部账户互转（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_flow_biz_type' AND dict_value='TRANSFER');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '手续费', 'FEE', 'fin_flow_biz_type', '', 'warning', 'N', '0', 'admin', sysdate(), '银行手续费（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_flow_biz_type' AND dict_value='FEE');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '利息收入', 'INTEREST', 'fin_flow_biz_type', '', 'success', 'N', '0', 'admin', sysdate(), '存款利息（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_flow_biz_type' AND dict_value='INTEREST');

-- ------------------------------------------------------------
-- 五、字典：资金账户状态（启用/停用）
-- ------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '启用', '0', 'fin_account_status', '', 'success', 'Y', '0', 'admin', sysdate(), '资金账户状态（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_account_status' AND dict_value='0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '停用', '1', 'fin_account_status', '', 'info', 'N', '0', 'admin', sysdate(), '资金账户状态（2026-10-09 新增）'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='fin_account_status' AND dict_value='1');

-- ------------------------------------------------------------
-- 验证
-- ------------------------------------------------------------
SELECT table_name, table_comment FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('fin_bank_statement','fin_bank_statement_item')
ORDER BY table_name;

SELECT dict_type, COUNT(*) AS items FROM sys_dict_data
WHERE dict_type IN ('fin_account_type','fin_flow_biz_type','fin_account_status','fin_deduct_status')
GROUP BY dict_type ORDER BY dict_type;