-- ============================================================
-- 财务模块 · 凭证规则方向修正（2026-10-09）
--
-- 【发现的问题】
--   实测 fin_voucher_rule 的 3 条采购发票规则方向与会计常识相反：
--     rule_id=1 应付账款 2202  方向=DEBIT  → 应为 CREDIT（负债增加记贷）
--     rule_id=2 原材料   1403  方向=CREDIT → 应为 DEBIT （资产增加记借）
--     rule_id=3 进项税   22210101 方向=CREDIT → 应为 DEBIT（可抵扣资产记借）
--
--   若照此执行，生成的采购凭证会借贷颠倒且无法平衡。
--   引擎侧已加借贷平衡校验兜底（不平衡直接拒绝生成），
--   但根因应在数据层修正。
--
-- 【正确的采购入库分录】
--     借：原材料（不含税金额）
--     借：应交税费——应交增值税（进项税额）（税额）
--     贷：应付账款（价税合计）
--
-- 幂等：可重复执行
-- 执行：mysql -uroot -p -D ry-vue < sql/fin/fin_11_fix_rule_direction.sql
-- ============================================================

SET NAMES utf8mb4;

-- 应付账款：负债增加，应记贷方
UPDATE fin_voucher_rule SET direction = 'CREDIT'
WHERE rule_key = 'pms_invoice_approved'
  AND account_code = '2202'
  AND direction = 'DEBIT';

-- 原材料：资产增加，应记借方
UPDATE fin_voucher_rule SET direction = 'DEBIT'
WHERE rule_key = 'pms_invoice_material'
  AND account_code = '1403'
  AND direction = 'CREDIT';

-- 进项税额：可抵扣资产增加，应记借方
-- 注意：22210101 的 category 已在 fin_10 中改为 ASSET，方向须为 DEBIT
UPDATE fin_voucher_rule SET direction = 'DEBIT'
WHERE rule_key = 'pms_invoice_tax'
  AND account_code = '22210101'
  AND direction = 'CREDIT';

-- 同步修正更新时间
UPDATE fin_voucher_rule SET update_time = sysdate()
WHERE rule_key IN ('pms_invoice_approved', 'pms_invoice_material', 'pms_invoice_tax');

-- ------------------------------------------------------------
-- 验证
-- ------------------------------------------------------------
SELECT rule_name,
       account_code   AS 科目,
       direction      AS 方向,
       amount_expr    AS 金额表达式,
       CASE
         WHEN rule_name LIKE '%应付账款%' AND direction = 'CREDIT' THEN '✓ 正确'
         WHEN rule_name LIKE '%原材料%'   AND direction = 'DEBIT'  THEN '✓ 正确'
         WHEN rule_name LIKE '%进项税%'   AND direction = 'DEBIT'  THEN '✓ 正确'
         WHEN rule_name LIKE '%应收%'     AND direction = 'DEBIT'  THEN '✓ 正确'
         WHEN rule_name LIKE '%收入%'     AND direction = 'CREDIT' THEN '✓ 正确'
         WHEN rule_name LIKE '%薪酬发放'  AND direction = 'DEBIT'  THEN '✓ 正确'
         WHEN rule_name LIKE '%银行%'     AND direction = 'CREDIT' THEN '✓ 正确'
         ELSE '✗ 需人工确认'
       END AS 校验
FROM fin_voucher_rule
ORDER BY priority, rule_id;

-- ------------------------------------------------------------
-- 借贷平衡自检：三组规则各自的借贷合计应相等
-- ------------------------------------------------------------
SELECT source_type AS 来源类型,
       SUM(CASE WHEN direction = 'DEBIT'  THEN 1 ELSE 0 END) AS 借方行数,
       SUM(CASE WHEN direction = 'CREDIT' THEN 1 ELSE 0 END) AS 贷方行数,
       CASE WHEN SUM(CASE WHEN direction = 'DEBIT' THEN 1 ELSE 0 END)
               = SUM(CASE WHEN direction = 'CREDIT' THEN 1 ELSE 0 END)
            THEN '✓ 行数对称'
            ELSE '✗ 行数不对称，需补充分录'
       END AS 检查
FROM fin_voucher_rule
WHERE del_flag = '0' AND status = '0'
GROUP BY source_type;