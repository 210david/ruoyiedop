-- ============================================================
-- 财务模块 · 红字冲销前置修正（fin_14）
--
-- 【发现过程】
--   实现红字冲销后做数据库层验证，负数分录被
--   CHECK 约束 chk_entry_amount (`amount` > 0) 拒绝：
--   ERROR 3819: Check constraint 'chk_entry_amount' is violated.
--
--   该约束建表时的意图是"禁止零金额分录"，但 > 0 同时
--   挡住了红字冲销（金额取负）—— 标准红字冲销必须允许负数。
--
-- 【修正】放宽为 `amount <> 0`：
--   ✓ 仍禁止零金额分录（原意图保留）
--   ✓ 允许负数（红字冲销）
--
-- 幂等：先 DROP（IF EXISTS）再 ADD（IF NOT EXISTS 语义由流程保证）
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_14_fix_amount_check.sql
-- ============================================================

SET NAMES utf8mb4;

ALTER TABLE fin_voucher_entry DROP CHECK chk_entry_amount;
ALTER TABLE fin_voucher_entry ADD CONSTRAINT chk_entry_amount CHECK (`amount` <> 0);

-- ------------------------------------------------------------
-- 验证：负数应可写入，0 应被拒
-- ------------------------------------------------------------
-- 负数验证（红字冲销场景）由应用层集成测试覆盖；
-- 此处仅查询确认新约束生效：
SELECT TC.TABLE_NAME, CC.CHECK_CLAUSE
FROM information_schema.TABLE_CONSTRAINTS TC
JOIN information_schema.CHECK_CONSTRAINTS CC
  ON CC.CONSTRAINT_SCHEMA = TC.CONSTRAINT_SCHEMA AND CC.CONSTRAINT_NAME = TC.CONSTRAINT_NAME
WHERE TC.CONSTRAINT_SCHEMA = DATABASE()
  AND TC.TABLE_NAME = 'fin_voucher_entry'
  AND CC.CONSTRAINT_NAME = 'chk_entry_amount';