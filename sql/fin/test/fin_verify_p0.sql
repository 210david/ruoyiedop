-- ============================================================
-- 财务模块基线约束验证脚本（P0 骨架）
-- 用途：验证 fin_01_schema.sql 的关键约束真实生效
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/test/fin_verify_p0.sql
-- 说明：本脚本只做 SELECT 验证与临时表测试，不修改业务数据
-- ============================================================

SET NAMES utf8mb4;

SELECT '=== 1. 表数量核对 ===' AS test_item;
SELECT COUNT(*) AS fin_table_count FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name LIKE 'fin\_%';
-- 期望 17

SELECT '=== 2. P0-6 约束：CHECK 是否生效 ===' AS test_item;
SELECT table_name, constraint_name
FROM information_schema.table_constraints
WHERE table_schema = DATABASE()
  AND constraint_type = 'CHECK'
  AND table_name LIKE 'fin\_%'
ORDER BY table_name;
-- 期望：fin_inventory_cost(2) / fin_subject(1) / fin_voucher_entry(1)

SELECT '=== 3. P0-3 幂等：凭证幂等唯一索引 ===' AS test_item;
SHOW INDEX FROM fin_voucher WHERE Key_name = 'uk_source';
-- 期望 5 列：source_module/source_type/source_id/rule_key/del_flag

SELECT '=== 4. P0-3 NULL 陷阱验证：唯一索引列是否全部 NOT NULL ===' AS test_item;
SELECT column_name, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'fin_voucher'
  AND column_name IN ('source_module','source_type','source_id','rule_key','del_flag')
ORDER BY ordinal_position;
-- 期望：全部 is_nullable=NO（否则 NULL 会使唯一索引失效）

SELECT '=== 5. P0-5 加密：账号密文列与尾号列 ===' AS test_item;
SELECT table_name, column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('fin_bank_account','fin_partner_bank')
  AND column_name IN ('account_number_encrypt','account_tail')
ORDER BY table_name, column_name;
-- 期望：account_number_encrypt varchar(256) + account_tail varchar(8)

SELECT '=== 6. P0-2 并发：成本台账唯一维度与乐观锁 ===' AS test_item;
SELECT column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'fin_inventory_cost'
  AND column_name IN ('material_id','warehouse_id','location_id','batch_no','version')
ORDER BY ordinal_position;
-- 期望 version int NOT NULL DEFAULT 0

SELECT '=== 7. 精度规范核对 ===' AS test_item;
SELECT table_name, column_name, column_type
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name LIKE 'fin\_%'
  AND column_name IN ('amount','unit_cost','tax_rate','qty','total_cost')
ORDER BY table_name, column_name;
-- 期望：amount/total_cost 为 decimal(14,2)
--       unit_cost 为 decimal(18,4)（含税除税精度保护）
--       tax_rate 为 decimal(8,4)

SELECT '=== 8. 索引完整性 ===' AS test_item;
SELECT COUNT(DISTINCT table_name) AS tables_with_index
FROM information_schema.statistics
WHERE table_schema = DATABASE() AND table_name LIKE 'fin\_%';

SELECT '=== 9. 字符集与引擎核对（应全部 InnoDB + utf8mb4）===' AS test_item;
SELECT DISTINCT engine, table_collation
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name LIKE 'fin\_%';

SELECT '=== 10. 审计字段完整性（每表应含 5 个审计字段）===' AS test_item;
SELECT table_name,
       SUM(column_name = 'del_flag')       AS has_del_flag,
       SUM(column_name = 'create_by')      AS has_create_by,
       SUM(column_name = 'create_time')    AS has_create_time,
       SUM(column_name = 'update_by')      AS has_update_by,
       SUM(column_name = 'update_time')    AS has_update_time,
       SUM(column_name = 'company_id')     AS has_company_id
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name LIKE 'fin\_%'
GROUP BY table_name
ORDER BY table_name;
-- 期望：17 张表全部为 1（fin_* 每张都带 company_id 预留多账套）
