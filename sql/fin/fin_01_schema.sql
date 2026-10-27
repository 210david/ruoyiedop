-- ============================================================
-- 财务管理模块 · 表结构基线（P0 骨架）
-- 工程：RuoYi-Vue3 (Java 17 / Spring Boot 4.0.6 / MySQL 8)
--
-- 【约束条款】本脚本仅新增 fin_* 表，不修改任何既有表结构。
-- 【命名规范】科目表用 fin_subject（避开 fin_account），资金账户用 fin_bank_account。
-- 【精度规范】金额 DECIMAL(14,2)、数量 DECIMAL(18,2)、
--              成本单价 DECIMAL(18,4)（含税除税精度保护）、比率 DECIMAL(8,4)
-- 【审计规范】全部表必备 del_flag/create_by/create_time/update_by/update_time/remark
-- 【追溯规范】全部业务表带 source_module + source_type + source_id 三元组
-- 【多账套】全部表预留 company_id（可空）
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 一、FM-6 总账（地基二，必须先于 FM-5 报表可用）
-- ============================================================

-- 1.1 会计科目表
CREATE TABLE IF NOT EXISTS `fin_subject` (
  `subject_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '科目ID',
  `subject_code`    VARCHAR(20)  NOT NULL                COMMENT '科目编码',
  `subject_name`    VARCHAR(100) NOT NULL                COMMENT '科目名称',
  `parent_id`       BIGINT       DEFAULT NULL            COMMENT '上级科目ID',
  `parent_code`     VARCHAR(20)  DEFAULT NULL            COMMENT '上级科目编码（冗余，便于树查询）',
  `level`           INT          NOT NULL DEFAULT 1      COMMENT '级次 1-4',
  `category`        VARCHAR(10)  NOT NULL                COMMENT '类别 ASSET/LIABILITY/EQUITY/COST/INCOME',
  `direction`       VARCHAR(10)  NOT NULL                COMMENT '余额方向 DEBIT借/CREDIT贷',
  `is_leaf`         CHAR(1)      DEFAULT '1'             COMMENT '是否明细科目 0否 1是（非明细禁止录凭证）',
  `is_cash_equiv`   CHAR(1)      DEFAULT '0'             COMMENT '是否现金等价物（现金流量表归集）',
  `cash_flow_category` VARCHAR(10) DEFAULT NULL          COMMENT '现金流分类 OPERATING/INVESTING/FINANCING',
  `aux_types`       VARCHAR(200) DEFAULT NULL            COMMENT '辅助核算类型，逗号分隔 C/S/D/P',
  `company_id`      BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `sort_order`      INT          DEFAULT 0               COMMENT '排序号',
  `status`          CHAR(1)      DEFAULT '0'             COMMENT '状态 0正常 1停用（停用保留历史数据）',
  `del_flag`        CHAR(1)      DEFAULT '0'             COMMENT '删除标志 0存在 2删除',
  `create_by`       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`     DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`     DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `uk_subject_code` (`subject_code`),
  KEY `idx_subject_parent` (`parent_id`),
  KEY `idx_subject_category` (`category`, `status`),
  CONSTRAINT `chk_subject_level` CHECK (`level` BETWEEN 1 AND 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计科目表';

-- 1.2 会计期间表
CREATE TABLE IF NOT EXISTS `fin_period` (
  `period_id`    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '期间ID',
  `company_id`   BIGINT      DEFAULT NULL            COMMENT '预留账套ID',
  `year_month`   VARCHAR(6)  NOT NULL                COMMENT '会计期间 YYYYMM',
  `period_name`  VARCHAR(20) DEFAULT NULL            COMMENT '期间名称，如 2026年10月',
  `status`       VARCHAR(10) DEFAULT 'OPEN'          COMMENT '状态 OPEN未结账/CLOSED已结账',
  `close_by`     VARCHAR(64) DEFAULT NULL            COMMENT '结账人',
  `close_time`   DATETIME    DEFAULT NULL            COMMENT '结账时间',
  `unlock_by`    VARCHAR(64) DEFAULT NULL            COMMENT '解锁人（仅财务经理）',
  `unlock_time`  DATETIME    DEFAULT NULL            COMMENT '解锁时间',
  `unlock_reason` VARCHAR(500) DEFAULT NULL          COMMENT '解锁原因（强制留痕）',
  `del_flag`     CHAR(1)     DEFAULT '0'             COMMENT '删除标志',
  `create_by`    VARCHAR(64) DEFAULT ''              COMMENT '创建者',
  `create_time`  DATETIME    DEFAULT NULL            COMMENT '创建时间',
  `update_by`    VARCHAR(64) DEFAULT ''              COMMENT '更新者',
  `update_time`  DATETIME    DEFAULT NULL            COMMENT '更新时间',
  `remark`       VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (`period_id`),
  UNIQUE KEY `uk_year_month` (`year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计期间表';

-- 1.3 财务全局配置表
CREATE TABLE IF NOT EXISTS `fin_fiscal_config` (
  `config_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key`    VARCHAR(50)  NOT NULL                COMMENT '配置键，如 cost_method',
  `config_value`  VARCHAR(200) DEFAULT NULL            COMMENT '配置值',
  `config_name`   VARCHAR(100) DEFAULT NULL            COMMENT '配置名称',
  `config_group`  VARCHAR(50)  DEFAULT NULL            COMMENT '配置分组',
  `company_id`    BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `del_flag`      CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务全局配置表';

-- 1.4 记账凭证主表
CREATE TABLE IF NOT EXISTS `fin_voucher` (
  `voucher_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '凭证ID',
  `voucher_no`    VARCHAR(32)  NOT NULL                COMMENT '凭证号',
  `company_id`    BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `voucher_date`  DATE         NOT NULL                COMMENT '凭证日期',
  `period`        VARCHAR(6)   NOT NULL                COMMENT '会计期间 YYYYMM（由 voucher_date 派生，不允许手填）',
  `voucher_type`  VARCHAR(10)  DEFAULT 'MANUAL'        COMMENT '类型 MANUAL手工/AUTO自动/CANCEL红字冲销',
  -- 五源追溯（手工凭证各字段填空串，保证唯一索引始终生效）
  `source_module` VARCHAR(20)  NOT NULL DEFAULT 'manual' COMMENT '来源模块 pms/mk/wms/hr/manual',
  `source_type`   VARCHAR(30)  NOT NULL DEFAULT ''       COMMENT '来源单据类型 invoice/shipment/outbound/payroll',
  `source_id`     BIGINT       NOT NULL DEFAULT 0        COMMENT '来源单据ID（手工为0）',
  `source_key`    VARCHAR(64)  DEFAULT NULL            COMMENT '来源单据号（冗余，便于人工排查）',
  `rule_key`      VARCHAR(64)  NOT NULL DEFAULT ''      COMMENT '命中的记账规则键（手工录入为空串，保证空串参与唯一索引）',
  -- 金额与状态
  `total_amount`  DECIMAL(14,2) DEFAULT 0              COMMENT '价税合计（借方合计）',
  `entry_count`   INT          DEFAULT 0               COMMENT '分录行数',
  `status`        VARCHAR(10)  DEFAULT 'DRAFT'         COMMENT '状态 DRAFT草稿/AUDITED已审核/POSTED已记账/CANCELLED已冲销',
  `attach_count`  INT          DEFAULT 0               COMMENT '附件数',
  `audit_by`      VARCHAR(64)  DEFAULT NULL            COMMENT '审核人',
  `audit_time`    DATETIME     DEFAULT NULL            COMMENT '审核时间',
  `audit_opinion` VARCHAR(500) DEFAULT NULL            COMMENT '审核意见',
  `del_flag`      CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`voucher_id`),
  -- 【P0-3 幂等核心】自动记账唯一键；含 del_flag 允许作废后重建
  UNIQUE KEY `uk_source` (`source_module`, `source_type`, `source_id`, `rule_key`, `del_flag`),
  UNIQUE KEY `uk_voucher_no` (`voucher_no`),
  KEY `idx_period` (`period`, `status`),
  KEY `idx_source` (`source_module`, `source_type`, `source_id`),
  KEY `idx_voucher_date` (`voucher_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账凭证主表';

-- 1.5 记账凭证分录表
CREATE TABLE IF NOT EXISTS `fin_voucher_entry` (
  `entry_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分录ID',
  `voucher_id`     BIGINT       NOT NULL                COMMENT '凭证ID',
  `company_id`     BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `line_no`        INT          NOT NULL DEFAULT 1      COMMENT '行号',
  `period`         VARCHAR(6)   NOT NULL                COMMENT '会计期间（冗余自凭证，加速账簿汇总）',
  `account_code`   VARCHAR(20)  NOT NULL                COMMENT '会计科目编码',
  `account_name`   VARCHAR(100) DEFAULT NULL            COMMENT '科目名称（冗余）',
  `direction`      VARCHAR(10)  NOT NULL                COMMENT '方向 DEBIT借/CREDIT贷',
  `amount`         DECIMAL(14,2) NOT NULL               COMMENT '金额（正数，红字用 direction 表达）',
  `currency`       VARCHAR(10)  DEFAULT 'CNY'           COMMENT '币种（V1 固定 CNY）',
  `exchange_rate`  DECIMAL(18,6) DEFAULT 1              COMMENT '汇率（V1 固定 1）',
  `tax_amount`     DECIMAL(14,2) DEFAULT 0              COMMENT '税额',
  -- 辅助核算四类（Q2 决策）
  `aux_customer_id` BIGINT      DEFAULT NULL            COMMENT '辅助核算-客户',
  `aux_supplier_id` BIGINT      DEFAULT NULL            COMMENT '辅助核算-供应商',
  `aux_dept_id`     BIGINT      DEFAULT NULL            COMMENT '辅助核算-部门',
  `aux_project_id`  BIGINT      DEFAULT NULL            COMMENT '辅助核算-项目',
  `summary`         VARCHAR(200) DEFAULT NULL           COMMENT '摘要',
  `del_flag`        CHAR(1)     DEFAULT '0'             COMMENT '删除标志',
  `create_by`       VARCHAR(64) DEFAULT ''              COMMENT '创建者',
  `create_time`     DATETIME    DEFAULT NULL            COMMENT '创建时间',
  `update_by`       VARCHAR(64) DEFAULT ''              COMMENT '更新者',
  `update_time`     DATETIME    DEFAULT NULL            COMMENT '更新时间',
  `remark`          VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (`entry_id`),
  KEY `idx_voucher` (`voucher_id`),
  KEY `idx_subject_period` (`account_code`, `period`),
  KEY `idx_aux_customer` (`aux_customer_id`),
  KEY `idx_aux_supplier` (`aux_supplier_id`),
  CONSTRAINT `chk_entry_amount` CHECK (`amount` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账凭证分录表';

-- 1.6 单据→凭证规则表（可配置，禁止硬编码）
CREATE TABLE IF NOT EXISTS `fin_voucher_rule` (
  `rule_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_key`       VARCHAR(64)  NOT NULL                COMMENT '规则键，如 pms_invoice_approved',
  `rule_name`      VARCHAR(100) NOT NULL                COMMENT '规则名称',
  `source_module`  VARCHAR(20)  NOT NULL                COMMENT '来源模块 pms/mk/wms/hr',
  `source_type`    VARCHAR(30)  NOT NULL                COMMENT '来源单据类型',
  `trigger_status` VARCHAR(20)  DEFAULT NULL            COMMENT '触发状态（空表示不限）',
  `account_code`   VARCHAR(20)  NOT NULL                COMMENT '目标科目编码',
  `direction`      VARCHAR(10)  NOT NULL                COMMENT '方向 DEBIT/CREDIT',
  `amount_expr`    VARCHAR(50)  NOT NULL                COMMENT '金额表达式 total_amount/invoice_amount/tax_amount',
  `aux_type`       VARCHAR(10)  DEFAULT NULL            COMMENT '辅助核算类型 CUSTOMER/SUPPLIER',
  `summary_tpl`    VARCHAR(200) DEFAULT NULL            COMMENT '摘要模板，支持 {字段名}',
  `priority`       INT          DEFAULT 0               COMMENT '优先级（多规则匹配时升序）',
  `status`         CHAR(1)      DEFAULT '0'             COMMENT '状态 0启用 1停用',
  `del_flag`       CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`    DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`    DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`         VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_rule_key` (`rule_key`),
  KEY `idx_rule_source` (`source_module`, `source_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据生成凭证规则表';

-- 1.7 结账检查表
CREATE TABLE IF NOT EXISTS `fin_closing` (
  `closing_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '结账记录ID',
  `company_id`   BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `year_month`   VARCHAR(6)   NOT NULL                COMMENT '会计期间 YYYYMM',
  `check_code`   VARCHAR(30)  NOT NULL                COMMENT '检查项编码',
  `check_name`   VARCHAR(100) NOT NULL                COMMENT '检查项名称',
  `check_result` CHAR(1)      DEFAULT '0'             COMMENT '检查结果 0未通过 1通过',
  `check_detail` VARCHAR(500) DEFAULT NULL            COMMENT '检查详情（失败原因）',
  `check_by`     VARCHAR(64)  DEFAULT NULL            COMMENT '检查人',
  `check_time`   DATETIME     DEFAULT NULL            COMMENT '检查时间',
  `del_flag`     CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`    VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`  DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`    VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`  DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`       VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`closing_id`),
  UNIQUE KEY `uk_period_check` (`year_month`, `check_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='期末结账检查表';

-- 1.8 账簿汇总表（期末结账时物化）
CREATE TABLE IF NOT EXISTS `fin_ledger` (
  `ledger_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '账簿ID',
  `company_id`      BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `account_code`    VARCHAR(20)  NOT NULL                COMMENT '科目编码',
  `period`          VARCHAR(6)   NOT NULL                COMMENT '会计期间',
  `aux_type`        VARCHAR(10)  DEFAULT NULL            COMMENT '辅助核算类型',
  `aux_id`          BIGINT       DEFAULT NULL            COMMENT '辅助核算ID',
  `opening_debit`   DECIMAL(14,2) DEFAULT 0              COMMENT '期初借方',
  `opening_credit`  DECIMAL(14,2) DEFAULT 0              COMMENT '期初贷方',
  `period_debit`    DECIMAL(14,2) DEFAULT 0              COMMENT '本期借方发生额',
  `period_credit`   DECIMAL(14,2) DEFAULT 0              COMMENT '本期贷方发生额',
  `closing_debit`   DECIMAL(14,2) DEFAULT 0              COMMENT '期末借方',
  `closing_credit`  DECIMAL(14,2) DEFAULT 0              COMMENT '期末贷方',
  `del_flag`        CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`     DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`     DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`ledger_id`),
  UNIQUE KEY `uk_ledger_dim` (`account_code`, `period`, `aux_type`, `aux_id`),
  KEY `idx_ledger_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账簿汇总表（结账物化）';

-- ============================================================
-- 二、FM-5 存货成本（地基一）
-- ============================================================

-- 2.1 物料税率档案（Q4 决策新增）
CREATE TABLE IF NOT EXISTS `fin_material_tax` (
  `material_tax_id` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '税率档案ID',
  `material_id`     BIGINT       NOT NULL                COMMENT '物料ID（wms_material.material_id，只读引用）',
  `material_code`   VARCHAR(64)  NOT NULL                COMMENT '物料编码（冗余）',
  `tax_rate`        DECIMAL(8,4) NOT NULL DEFAULT 0.1300 COMMENT '税率 0.1300=13%',
  `tax_included`    CHAR(1)      DEFAULT '1'             COMMENT '单价是否含税 1含税 0不含税（Q4 统一含税，保留为兼容开关）',
  `effective_date`  DATE         NOT NULL                COMMENT '生效日期',
  `expire_date`     DATE         DEFAULT NULL            COMMENT '失效日期（为空长期有效）',
  `company_id`      BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `status`          CHAR(1)      DEFAULT '0'             COMMENT '状态 0启用 1停用',
  `del_flag`        CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`     DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`     DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`          VARCHAR(500) DEFAULT NULL            COMMENT '备注（零税率物料必须显式维护 tax_rate=0）',
  PRIMARY KEY (`material_tax_id`),
  UNIQUE KEY `uk_material_date` (`material_id`, `effective_date`),
  KEY `idx_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料税率档案表';

-- 2.2 存货成本台账
CREATE TABLE IF NOT EXISTS `fin_inventory_cost` (
  `cost_id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '成本记录ID',
  `company_id`       BIGINT        DEFAULT NULL            COMMENT '预留账套ID',
  `material_id`      BIGINT        NOT NULL                COMMENT '物料ID',
  `material_code`    VARCHAR(64)   NOT NULL                COMMENT '物料编码（冗余）',
  `material_name`    VARCHAR(255)  DEFAULT NULL            COMMENT '物料名称（冗余）',
  `warehouse_id`     BIGINT        NOT NULL                COMMENT '仓库ID',
  `location_id`      BIGINT        NOT NULL DEFAULT 0      COMMENT '库位ID',
  `batch_no`         VARCHAR(64)   NOT NULL DEFAULT ''    COMMENT '批次号（非空，空串参与唯一索引）',
  `qty`              DECIMAL(18,2) NOT NULL DEFAULT 0      COMMENT '结存数量',
  `unit_cost`        DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '不含税单位成本（4位小数，Q4精度保护）',
  `total_cost`       DECIMAL(14,2) NOT NULL DEFAULT 0      COMMENT '结存成本金额',
  `cost_method`      VARCHAR(20)   DEFAULT 'WEIGHTED_AVG'  COMMENT '计价方法 WEIGHTED_AVG加权平均/FIFO先进先出',
  -- 追溯
  `source_module`    VARCHAR(20)   DEFAULT NULL            COMMENT '来源模块 wms/pms',
  `source_type`      VARCHAR(30)   DEFAULT NULL            COMMENT '来源单据类型 inbound/purchase_return',
  `source_id`        BIGINT        DEFAULT NULL            COMMENT '来源单据ID',
  `inbound_order_id` BIGINT        DEFAULT NULL            COMMENT '入库单ID（反查成本来源）',
  `last_in_date`     DATE          DEFAULT NULL            COMMENT '最近入库日期',
  `version`          INT           NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  `del_flag`         CHAR(1)       DEFAULT '0'             COMMENT '删除标志',
  `create_by`        VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`      DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_by`        VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`      DATETIME      DEFAULT NULL            COMMENT '更新时间',
  `remark`           VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`cost_id`),
  UNIQUE KEY `uk_cost_dim` (`material_id`, `warehouse_id`, `location_id`, `batch_no`),
  KEY `idx_cost_material` (`material_id`),
  KEY `idx_cost_inbound` (`inbound_order_id`),
  KEY `idx_cost_warehouse` (`warehouse_id`),
  CONSTRAINT `chk_cost_qty` CHECK (`qty` >= 0),
  CONSTRAINT `chk_cost_unit` CHECK (`unit_cost` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存货成本台账表';

-- 2.3 成本结转流水（总账取数来源）
CREATE TABLE IF NOT EXISTS `fin_cost_carry_forward` (
  `cf_id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '结转ID',
  `cf_no`         VARCHAR(32)   NOT NULL                COMMENT '结转单号',
  `company_id`    BIGINT        DEFAULT NULL            COMMENT '预留账套ID',
  `cost_date`     DATE          NOT NULL                COMMENT '结转日期',
  `period`        VARCHAR(6)    NOT NULL                COMMENT '会计期间',
  `source_module` VARCHAR(20)   NOT NULL                COMMENT '来源模块 wms/mms/mk',
  `source_type`   VARCHAR(30)   NOT NULL                COMMENT '来源类型 outbound/issue/sale_return/scrap',
  `source_id`     BIGINT        NOT NULL                COMMENT '来源单据ID',
  `material_id`   BIGINT        NOT NULL                COMMENT '物料ID',
  `material_code` VARCHAR(64)   DEFAULT NULL            COMMENT '物料编码',
  `qty`           DECIMAL(18,2) NOT NULL                COMMENT '结转数量（正数）',
  `unit_cost`     DECIMAL(18,4) NOT NULL                COMMENT '单位成本',
  `total_cost`    DECIMAL(14,2) NOT NULL                COMMENT '结转金额',
  `target_type`   VARCHAR(20)   DEFAULT NULL            COMMENT '结转目标 SALES_COST销售成本/PROD_COST生产成本/EXPENSE费用',
  `target_key`    VARCHAR(64)   DEFAULT NULL            COMMENT '结转目标键（如订单号）',
  `voucher_id`    BIGINT        DEFAULT NULL            COMMENT '关联凭证ID',
  `del_flag`      CHAR(1)       DEFAULT '0'             COMMENT '删除标志',
  `create_by`     VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME      DEFAULT NULL            COMMENT '更新时间',
  `remark`        VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`cf_id`),
  UNIQUE KEY `uk_cf_no` (`cf_no`),
  UNIQUE KEY `uk_cf_source` (`source_module`, `source_type`, `source_id`, `material_id`, `del_flag`),
  KEY `idx_cf_period` (`period`),
  KEY `idx_cf_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本结转流水表';

-- 2.4 计价方法配置
CREATE TABLE IF NOT EXISTS `fin_cost_method_config` (
  `config_id`     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `material_id`   BIGINT        DEFAULT NULL            COMMENT '物料ID（空=全局默认）',
  `cost_method`   VARCHAR(20)   NOT NULL DEFAULT 'WEIGHTED_AVG' COMMENT '计价方法 WEIGHTED_AVG/FIFO',
  `tax_included`  CHAR(1)       DEFAULT '1'             COMMENT '入库单价是否含税（Q4 统一含税，此为兼容开关）',
  `effective_date` DATE         DEFAULT NULL            COMMENT '生效日期',
  `company_id`    BIGINT        DEFAULT NULL            COMMENT '预留账套ID',
  `status`        CHAR(1)       DEFAULT '0'             COMMENT '状态 0启用 1停用',
  `del_flag`      CHAR(1)       DEFAULT '0'             COMMENT '删除标志',
  `create_by`     VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME      DEFAULT NULL            COMMENT '更新时间',
  `remark`        VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`config_id`),
  KEY `idx_cfg_material` (`material_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计价方法配置表';

-- 2.5 期末存货计价
CREATE TABLE IF NOT EXISTS `fin_inventory_valuation` (
  `val_id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '估值ID',
  `company_id`      BIGINT        DEFAULT NULL            COMMENT '预留账套ID',
  `period`          VARCHAR(6)    NOT NULL                COMMENT '会计期间',
  `material_id`     BIGINT        NOT NULL                COMMENT '物料ID',
  `material_code`   VARCHAR(64)   DEFAULT NULL            COMMENT '物料编码',
  `warehouse_id`    BIGINT        DEFAULT NULL            COMMENT '仓库ID',
  `qty`             DECIMAL(18,2) NOT NULL DEFAULT 0      COMMENT '结存数量',
  `unit_cost`       DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '单位成本',
  `total_value`     DECIMAL(14,2) NOT NULL DEFAULT 0      COMMENT '存货价值',
  `provision_amount` DECIMAL(14,2) DEFAULT 0             COMMENT '跌价准备',
  `del_flag`        CHAR(1)       DEFAULT '0'             COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL            COMMENT '更新时间',
  `remark`          VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`val_id`),
  UNIQUE KEY `uk_val_dim` (`period`, `material_id`, `warehouse_id`),
  KEY `idx_val_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='期末存货计价表';

-- ============================================================
-- 三、FM-1 往来单位
-- ============================================================

-- 3.1 往来单位（映射 wms_supplier / mk_customer，零侵入）
CREATE TABLE IF NOT EXISTS `fin_partner` (
  `partner_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '往来单位ID',
  `company_id`      BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `partner_code`    VARCHAR(64)  DEFAULT NULL            COMMENT '单位编码',
  `partner_name`    VARCHAR(255) NOT NULL                COMMENT '单位名称',
  `partner_type`    VARCHAR(10)  NOT NULL                COMMENT '类型 SUPPLIER/CUSTOMER/BOTH',
  -- 映射既有主数据（只读引用，零侵入）
  `source_module`   VARCHAR(20)  DEFAULT NULL            COMMENT '来源模块 wms/mk',
  `source_id`       BIGINT       DEFAULT NULL            COMMENT '来源主数据ID（wms_supplier.supplier_id 或 mk_customer.customer_id）',
  `tax_number`      VARCHAR(64)  DEFAULT NULL            COMMENT '税号',
  `invoice_title`   VARCHAR(255) DEFAULT NULL            COMMENT '发票抬头',
  `invoice_address` VARCHAR(500) DEFAULT NULL            COMMENT '开票地址',
  `invoice_phone`   VARCHAR(20)  DEFAULT NULL            COMMENT '开票电话',
  -- 结算条件
  `payment_days`    INT          DEFAULT 0               COMMENT '账期天数',
  `settlement_type` VARCHAR(20)  DEFAULT NULL            COMMENT '结算方式（字典 wms_settlement_type）',
  `currency`        VARCHAR(10)  DEFAULT 'CNY'           COMMENT '结算币种',
  `credit_limit`    DECIMAL(14,2) DEFAULT 0              COMMENT '信用额度',
  `credit_used`     DECIMAL(14,2) DEFAULT 0              COMMENT '已用信用额度',
  `status`          CHAR(1)      DEFAULT '0'             COMMENT '状态 0正常 1停用',
  `del_flag`        CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`     DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`     DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`partner_id`),
  KEY `idx_partner_source` (`source_module`, `source_id`),
  KEY `idx_partner_type` (`partner_type`, `status`),
  KEY `idx_partner_name` (`partner_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务往来单位表';

-- 3.2 往来单位银行账户（P0-5 加密；Q7 决策：不改 mk_customer，财务侧独立存）
CREATE TABLE IF NOT EXISTS `fin_partner_bank` (
  `bank_id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '银行账户ID',
  `partner_id`        BIGINT       NOT NULL                COMMENT '往来单位ID',
  `company_id`        BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `account_name`      VARCHAR(255) NOT NULL                COMMENT '账户名称',
  `bank_name`         VARCHAR(128) DEFAULT NULL            COMMENT '开户银行',
  `bank_branch`       VARCHAR(128) DEFAULT NULL            COMMENT '开户支行',
  `account_number_encrypt` VARCHAR(256) DEFAULT NULL       COMMENT '银行账号密文（AES-128-ECB，密文列）',
  `account_tail`      VARCHAR(8)   DEFAULT NULL            COMMENT '账号后4位（明文，用于展示检索，禁止改密文时留空）',
  `is_default`        CHAR(1)      DEFAULT '0'             COMMENT '是否默认账户',
  `status`            CHAR(1)      DEFAULT '0'             COMMENT '状态 0正常 1停用',
  `del_flag`          CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`         VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`       DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`         VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`       DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`bank_id`),
  KEY `idx_bank_partner` (`partner_id`, `is_default`),
  KEY `idx_bank_tail` (`account_tail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='往来单位银行账户表（账号密文存储）';

-- ============================================================
-- 四、FM-4 资金（Q1 决策：资金账户表名 fin_bank_account）
-- ============================================================

-- 4.1 银行/现金账户
CREATE TABLE IF NOT EXISTS `fin_bank_account` (
  `account_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `company_id`    BIGINT       DEFAULT NULL            COMMENT '预留账套ID',
  `account_code`  VARCHAR(32)  NOT NULL                COMMENT '账户编码',
  `account_name`  VARCHAR(100) NOT NULL                COMMENT '账户名称',
  `account_type`  VARCHAR(20)  NOT NULL                COMMENT '账户类型 BANK银行/CASH现金/ALIPAY/WeChat/WECHAT',
  `bank_name`     VARCHAR(128) DEFAULT NULL            COMMENT '开户银行',
  `bank_branch`   VARCHAR(128) DEFAULT NULL            COMMENT '开户支行',
  `account_number_encrypt` VARCHAR(256) DEFAULT NULL   COMMENT '银行账号密文（AES-128-ECB）',
  `account_tail`  VARCHAR(8)   DEFAULT NULL            COMMENT '账号后4位（明文展示检索）',
  `currency`      VARCHAR(10)  DEFAULT 'CNY'           COMMENT '币种',
  `opening_balance` DECIMAL(14,2) DEFAULT 0            COMMENT '期初余额',
  `current_balance` DECIMAL(14,2) DEFAULT 0            COMMENT '当前余额',
  `status`        CHAR(1)      DEFAULT '0'             COMMENT '状态 0正常 1停用',
  `del_flag`      CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `uk_account_code` (`account_code`),
  KEY `idx_account_tail` (`account_tail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金账户表（银行/现金/第三方支付）';

-- 4.2 资金流水
CREATE TABLE IF NOT EXISTS `fin_cash_flow` (
  `flow_id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `flow_no`         VARCHAR(32)   NOT NULL                COMMENT '流水号',
  `company_id`      BIGINT        DEFAULT NULL            COMMENT '预留账套ID',
  `account_id`      BIGINT        NOT NULL                COMMENT '资金账户ID',
  `flow_date`       DATE          NOT NULL                COMMENT '流水日期',
  `period`          VARCHAR(6)    NOT NULL                COMMENT '会计期间',
  `direction`       VARCHAR(10)   NOT NULL                COMMENT '方向 IN收/OUT付',
  `biz_type`        VARCHAR(20)   NOT NULL                COMMENT '业务类型 RECEIPT收款/PAYMENT付款/EXPENSE费用/SALARY工资/TAX税费/TRANSFER内部转账',
  `amount`          DECIMAL(14,2) NOT NULL                COMMENT '金额',
  `balance_after`   DECIMAL(14,2) DEFAULT 0               COMMENT '交易后余额',
  -- 五源追溯（非空约束保证幂等唯一索引生效）
  `source_module`   VARCHAR(20)   NOT NULL DEFAULT 'manual' COMMENT '来源模块 pms/mk/hr/manual',
  `source_type`     VARCHAR(30)   NOT NULL DEFAULT ''       COMMENT '来源单据类型',
  `source_id`       BIGINT        NOT NULL DEFAULT 0        COMMENT '来源单据ID',
  `source_key`      VARCHAR(64)   DEFAULT NULL            COMMENT '来源单据号',
  `voucher_id`      BIGINT        DEFAULT NULL            COMMENT '关联凭证ID',
  `reconcile_status` CHAR(1)      DEFAULT '0'             COMMENT '对账状态 0未对 1已对',
  `del_flag`        CHAR(1)       DEFAULT '0'             COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL            COMMENT '更新时间',
  `remark`          VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`flow_id`),
  UNIQUE KEY `uk_flow_no` (`flow_no`),
  UNIQUE KEY `uk_flow_source` (`source_module`, `source_type`, `source_id`, `del_flag`),
  KEY `idx_flow_account_date` (`account_id`, `flow_date`),
  KEY `idx_flow_period` (`period`),
  KEY `idx_flow_reconcile` (`reconcile_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表';

-- ============================================================
-- 说明：FM-2 应收（7 张）、FM-3 应付（6 张）、
--       FM-7 报表视图（5 个）在 P3/P4/P7 阶段随对应子模块一并交付。
--       本基线只建 P0/P1/P2 所需的 16 张表。
-- ============================================================
