-- ============================================================
-- 财务模块基线修正脚本（P0 骨架 · 增量）
-- 执行前提：fin_01_schema.sql 已执行
-- 【约束】仅 ALTER fin_* 表，不触碰任何既有表
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 修正 1：fin_inventory_cost.batch_no 必须 NOT NULL
-- 原因：MySQL 唯一索引中若任一列为 NULL，该行不被视为重复，
--       会导致「同物料+仓库+库位」在不同批次重复建行，成本台账失真。
-- ------------------------------------------------------------
ALTER TABLE `fin_inventory_cost`
  MODIFY COLUMN `batch_no` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '批次号（非空，空串参与唯一索引）';

-- ------------------------------------------------------------
-- 修正 2：fin_ledger 辅助核算列不允许 NULL
-- 原因：账簿按 (account_code, period, aux_type, aux_id) 唯一，
--       NULL 会导致同一科目同一期间产生多条看似不冲突的账簿行。
-- ------------------------------------------------------------
ALTER TABLE `fin_ledger`
  MODIFY COLUMN `aux_type` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '辅助核算类型（空串参与唯一索引）',
  MODIFY COLUMN `aux_id`   BIGINT      NOT NULL DEFAULT 0  COMMENT '辅助核算ID（0 表示无辅助核算）';

-- ------------------------------------------------------------
-- 修正 3：fin_cost_carry_forward 幂等键列不允许 NULL
-- 原因：uk_cf_source 唯一索引依赖这四列，NULL 会使幂等失效，
--       导致出库结转重复生成成本结转流水。
-- ------------------------------------------------------------
ALTER TABLE `fin_cost_carry_forward`
  MODIFY COLUMN `source_module` VARCHAR(20) NOT NULL COMMENT '来源模块 wms/mms/mk',
  MODIFY COLUMN `source_type`   VARCHAR(30) NOT NULL COMMENT '来源类型 outbound/issue/sale_return/scrap';

-- ------------------------------------------------------------
-- 修正 4：fin_cash_flow 幂等键不允许 NULL（已在 01 建立，此处兜底）
-- ------------------------------------------------------------
ALTER TABLE `fin_cash_flow`
  MODIFY COLUMN `source_module` VARCHAR(20) NOT NULL DEFAULT 'manual' COMMENT '来源模块 pms/mk/hr/manual';

-- ------------------------------------------------------------
-- 修正 5：科目编码长度与排序，补充层级索引
-- ------------------------------------------------------------
ALTER TABLE `fin_subject`
  ADD KEY `idx_subject_level` (`level`, `category`);

-- ------------------------------------------------------------
-- 验证
-- ------------------------------------------------------------
SELECT table_name, column_name, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND ((table_name = 'fin_inventory_cost'  AND column_name = 'batch_no')
    OR (table_name = 'fin_ledger'          AND column_name IN ('aux_type','aux_id'))
    OR (table_name = 'fin_cost_carry_forward' AND column_name IN ('source_module','source_type'))
    OR (table_name = 'fin_cash_flow'       AND column_name = 'source_module'))
ORDER BY table_name, column_name;
-- 期望：全部 is_nullable = NO
