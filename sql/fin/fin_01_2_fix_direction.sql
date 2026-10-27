-- ============================================================
-- 财务模块基线修正脚本 2（P0 骨架 · 字段长度）
-- 执行前提：fin_01_schema.sql 已执行
-- 【约束】仅 ALTER fin_* 表，不触碰任何既有表
--
-- 修正内容：direction 字段长度不足
--   缺陷：fin_voucher_entry.direction 定义为 VARCHAR(4)，
--         无法容纳 'DEBIT'(5字符) 与 'CREDIT'(6字符)，
--         导致插入分录时报 ERROR 1406 Data too long。
--   影响：fin_subject / fin_voucher_entry / fin_voucher_rule / fin_cash_flow 共 4 张表
--   修正：统一放宽为 VARCHAR(10)
-- ============================================================

SET NAMES utf8mb4;

ALTER TABLE `fin_subject`
  MODIFY COLUMN `direction` VARCHAR(10) NOT NULL COMMENT '余额方向 DEBIT借/CREDIT贷';

ALTER TABLE `fin_voucher_entry`
  MODIFY COLUMN `direction` VARCHAR(10) NOT NULL COMMENT '方向 DEBIT借/CREDIT贷';

ALTER TABLE `fin_voucher_rule`
  MODIFY COLUMN `direction` VARCHAR(10) NOT NULL COMMENT '方向 DEBIT/CREDIT';

ALTER TABLE `fin_cash_flow`
  MODIFY COLUMN `direction` VARCHAR(10) NOT NULL COMMENT '方向 IN收/OUT付';

-- ------------------------------------------------------------
-- 验证：4 张表 direction 长度应为 10
-- ------------------------------------------------------------
SELECT table_name, column_name, column_type
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name LIKE 'fin\_%'
  AND column_name = 'direction'
ORDER BY table_name;
-- 期望：4 行，column_type 全部为 varchar(10)
