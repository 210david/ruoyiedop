-- 费用报销表单：是否冲抵借款=否 时自动清空借款金额/本次冲抵金额（幂等，逐条追加）
UPDATE `ry-vue`.flow_form
SET ext_json = JSON_ARRAY_APPEND(ext_json, '$.rules', CAST('{"id":"rule_clear_loan_when_no","name":"非冲抵时清空借款金额","type":"derive","enabled":true,"target":"loan_amount","trigger":["need_loan_offset"],"expr":{"fn":"if","args":[{"fn":"eq","args":[{"field":"need_loan_offset"},{"value":"否"}]},{"value":""},{"field":"loan_amount"}]},"desc":"是否冲抵借款=否时清空借款金额"}' AS JSON))
WHERE form_name LIKE '%报销%'
  AND NOT JSON_CONTAINS(JSON_EXTRACT(ext_json, '$.rules[*].id'), JSON_QUOTE('rule_clear_loan_when_no'));

UPDATE `ry-vue`.flow_form
SET ext_json = JSON_ARRAY_APPEND(ext_json, '$.rules', CAST('{"id":"rule_clear_offset_when_no","name":"非冲抵时清空本次冲抵金额","type":"derive","enabled":true,"target":"offset_amount","trigger":["need_loan_offset"],"expr":{"fn":"if","args":[{"fn":"eq","args":[{"field":"need_loan_offset"},{"value":"否"}]},{"value":""},{"field":"offset_amount"}]},"desc":"是否冲抵借款=否时清空本次冲抵金额"}' AS JSON))
WHERE form_name LIKE '%报销%'
  AND NOT JSON_CONTAINS(JSON_EXTRACT(ext_json, '$.rules[*].id'), JSON_QUOTE('rule_clear_offset_when_no'));

SELECT JSON_EXTRACT(ext_json, '$.rules[*].id') FROM `ry-vue`.flow_form WHERE form_name LIKE '%报销%';
