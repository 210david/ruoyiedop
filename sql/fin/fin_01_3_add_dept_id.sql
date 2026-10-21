-- ============================================================
-- 财务模块 · 数据权限归属字段补齐（强制 · 对齐 unified-development-standards.md 3.5）
-- 执行前提：fin_01_schema.sql 已执行
-- 【约束】仅 ALTER fin_* 表，不触碰任何既有表
--
-- 规范要求（3.5.2）：
--   事务单据/记录类  必须带 dept_id
--   人员归属型      必须带 dept_id + user_id
--   BizDeptFillAspect 对 *ServiceImpl.insert*(..) 自动填充 dept_id
--   （仅当 deptId 为空时填充；反射调用 setDeptId(Long)）
--
-- 财务模块归属分类：
--   事务单据/记录  → fin_voucher / fin_voucher_entry / fin_cash_flow
--                    / fin_cost_carry_forward / fin_closing / fin_ledger
--   人员归属型     → fin_voucher（制单人所属部门）
--   主数据/共享    → fin_subject / fin_period / fin_fiscal_config
--                    / fin_material_tax / fin_cost_method_config
--                    / fin_inventory_cost / fin_inventory_valuation
--                    / fin_partner / fin_partner_bank / fin_bank_account
--                    （按 3.5.1 豁免，不加归属字段）
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 凭证主表：制单人所属部门（人员归属型）
-- ------------------------------------------------------------
ALTER TABLE `fin_voucher`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID（BizDeptFillAspect 自动填充）' AFTER `rule_key`,
  ADD COLUMN `user_id` BIGINT DEFAULT NULL COMMENT '制单人ID（对应 user_id 列，DataScope userAlias 用）' AFTER `dept_id`,
  ADD KEY `idx_voucher_dept` (`dept_id`),
  ADD KEY `idx_voucher_user` (`user_id`);

-- ------------------------------------------------------------
-- 2. 凭证分录：随凭证继承部门，便于按部门汇总账簿
-- ------------------------------------------------------------
ALTER TABLE `fin_voucher_entry`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID（随凭证继承）' AFTER `period`,
  ADD KEY `idx_entry_dept` (`dept_id`, `period`);

-- ------------------------------------------------------------
-- 3. 资金流水：出纳所属部门
-- ------------------------------------------------------------
ALTER TABLE `fin_cash_flow`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID' AFTER `period`,
  ADD KEY `idx_flow_dept` (`dept_id`, `period`);

-- ------------------------------------------------------------
-- 4. 成本结转流水：仓库所属部门
-- ------------------------------------------------------------
ALTER TABLE `fin_cost_carry_forward`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID' AFTER `period`,
  ADD KEY `idx_cf_dept` (`dept_id`, `period`);

-- ------------------------------------------------------------
-- 5. 结账检查记录：财务经理所属部门
-- ------------------------------------------------------------
ALTER TABLE `fin_closing`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID' AFTER `year_month`,
  ADD KEY `idx_closing_dept` (`dept_id`);

-- ------------------------------------------------------------
-- 6. 账簿汇总：按部门辅助核算
-- ------------------------------------------------------------
ALTER TABLE `fin_ledger`
  ADD COLUMN `dept_id` BIGINT NOT NULL DEFAULT 0 COMMENT '归属部门ID（0 表示全公司汇总）' AFTER `period`,
  ADD KEY `idx_ledger_dept` (`dept_id`, `period`);

-- ------------------------------------------------------------
-- 7. 往来单位：负责人归属（人员归属型，Q7 决策零侵入——
--    不改 mk_customer / wms_supplier，仅在本表补负责人）
-- ------------------------------------------------------------
ALTER TABLE `fin_partner`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID' AFTER `credit_used`,
  ADD COLUMN `user_id` BIGINT DEFAULT NULL COMMENT '负责人ID' AFTER `dept_id`,
  ADD KEY `idx_partner_dept` (`dept_id`),
  ADD KEY `idx_partner_user` (`user_id`);

-- ------------------------------------------------------------
-- 8. 发票与银行账户：开票员 / 出纳归属
-- ------------------------------------------------------------
ALTER TABLE `fin_partner_bank`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID' AFTER `partner_id`,
  ADD KEY `idx_bank_dept` (`dept_id`);

ALTER TABLE `fin_bank_account`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID' AFTER `account_type`,
  ADD KEY `idx_account_dept` (`dept_id`);

-- ------------------------------------------------------------
-- 验证：确认 dept_id / user_id 已加入
-- ------------------------------------------------------------
SELECT table_name, column_name, is_nullable, column_type
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name LIKE 'fin\_%'
  AND column_name IN ('dept_id', 'user_id')
ORDER BY table_name, column_name;
-- 期望：fin_voucher(dept_id,user_id) / fin_voucher_entry(dept_id)
--       / fin_cash_flow(dept_id) / fin_cost_carry_forward(dept_id)
--       / fin_closing(dept_id) / fin_ledger(dept_id)
--       / fin_partner(dept_id,user_id) / fin_partner_bank(dept_id)
--       / fin_bank_account(dept_id)
