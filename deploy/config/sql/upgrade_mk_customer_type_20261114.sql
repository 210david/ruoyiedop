-- ============================================================
-- 客户类型字典对齐金蝶客户分类（mk_customer_type）
-- 日期：2026-11-14
-- 说明：1 企业客户 → 直营客户；新增 4 代理商、5 其他
-- 幂等：可重复执行
-- ============================================================
SET NAMES utf8mb4;

-- 1. 字典类型（客户类型）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '客户类型', 'mk_customer_type', '0', 'admin', NOW(), '客户分类（对齐金蝶客户分类）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_customer_type');

-- 2. 原「企业客户」更名为「直营客户」（金蝶客户分类）
UPDATE sys_dict_data
SET dict_label = '直营客户', update_by = 'admin', update_time = NOW()
WHERE dict_type = 'mk_customer_type' AND dict_value = '1' AND dict_label <> '直营客户';

-- 3. 补充 代理商 / 其他
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '代理商', '4', 'mk_customer_type', '', '', 'N', '0', 'admin', NOW(), '金蝶客户分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_customer_type' AND dict_value = '4');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '其他', '5', 'mk_customer_type', '', 'info', 'N', '0', 'admin', NOW(), '金蝶客户分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_customer_type' AND dict_value = '5');
