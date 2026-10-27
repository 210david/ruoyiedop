-- ============================================================
-- !! 执行要求：mysql --default-character-set=utf8mb4
--    （Windows 下 mysql 客户端默认字符集非 utf8mb4，直接 source 含中文的
--     UTF-8 脚本会把中文写成 U+FFFD 乱码，且原字符丢失无法自动恢复）
-- 信用控制 + 政策取价 同步标准仓库（ry-vue 库）
-- 内容：mk_customer 信用列 + mk_credit_status 字典
-- 说明：信用控制无独立表，欠款占用实时计算（fin_ar_order + mk_order/mk_invoice）
-- 执行时间：2026-10-16
-- ============================================================

-- 1. 客户表增加信用字段
ALTER TABLE `ry-vue`.mk_customer
  ADD COLUMN credit_limit decimal(14,2) DEFAULT 0.00 COMMENT '信用额度上限（0=不启用信用控制）',
  ADD COLUMN credit_days int DEFAULT 0 COMMENT '账期天数',
  ADD COLUMN credit_status char(1) DEFAULT '0' COMMENT '信用状态（0正常 1冻结）';

-- 2. 信用状态字典
INSERT INTO `ry-vue`.sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '信用状态', 'mk_credit_status', '0', 'admin', NOW(), '营销客户信用状态'
WHERE NOT EXISTS (SELECT 1 FROM `ry-vue`.sys_dict_type WHERE dict_type = 'mk_credit_status');

INSERT INTO `ry-vue`.sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time)
SELECT 1, '正常', '0', 'mk_credit_status', 'success', '0', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `ry-vue`.sys_dict_data WHERE dict_type = 'mk_credit_status' AND dict_value = '0');

INSERT INTO `ry-vue`.sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time)
SELECT 2, '冻结', '1', 'mk_credit_status', 'danger', '0', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `ry-vue`.sys_dict_data WHERE dict_type = 'mk_credit_status' AND dict_value = '1');
