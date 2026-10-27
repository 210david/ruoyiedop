-- ============================================================
-- !! 执行要求：mysql --default-character-set=utf8mb4
--    （Windows 下 mysql 客户端默认字符集非 utf8mb4，直接 source 含中文的
--     UTF-8 脚本会把中文写成 U+FFFD 乱码，且原字符丢失无法自动恢复）
-- 标准仓库同步测试发现的数据缺口修复（2026-10-16）
-- 1) 昨日同步价格与返利时遗漏营销编号规则（mk_price_policy 等4条）
-- 2) 价格政策/返利管理 两个菜单图标弄反
-- ============================================================

-- 1. 补营销编号规则（序号归零，从1开始）
INSERT INTO `ry-vue`.mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field, prefix_field_dict_type, prefix_field_enabled, status, create_by, create_time, remark)
SELECT rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, 0, NULL, connector, prefix_field, prefix_field_dict_type, prefix_field_enabled, status, 'admin', NOW(), remark
FROM RUOYI_CFSP.mk_number_rule WHERE rule_code IN ('mk_bid','mk_price_policy','mk_rebate_policy','mk_rebate_settle');

-- 2. 修正菜单图标（价格政策=shopping，返利管理=money）
UPDATE `ry-vue`.sys_menu SET icon='shopping' WHERE menu_id=9062211;
UPDATE `ry-vue`.sys_menu SET icon='money'    WHERE menu_id=9062221;
