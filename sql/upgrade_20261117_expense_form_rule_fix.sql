-- 费用报销表单（form_id=21）交互规则修复（2026-11-17）
-- 需求口径（用户确认）：
--   借款金额 / 本次冲抵金额 两格默认只读；「是否冲抵借款」默认「否」；
--   选「是」→ 解除只读 + 必填（红星 + 提交校验）；选「否」→ 恢复只读。
-- 实现：
--   1) 布局保持 attr:ro（默认只读），不再误改为 edit；
--   2) 新增两条 readonly 规则（action.readonly=false）：选「是」时引擎显式解除只读；
--      选「否」时规则不命中、states 全量重建，字段自动回落设计器只读；
--   3) 已有 required 规则（ai_1794819329096_1/2）负责选「是」后的必填校验；
--   4) 新增「合计大写自动生成」derive 规则；停用误报的「大写等于小写」校验规则。
-- 幂等：可重复执行

-- 1) 布局还原为默认只读（flow_form 主布局 + flow_node_form 节点模板快照）
UPDATE `ry-vue`.flow_form
SET layout_json = REPLACE(layout_json, '"field":"loan_amount","attr":"edit"', '"field":"loan_amount","attr":"ro"')
WHERE form_id = 21
  AND layout_json LIKE '%"field":"loan_amount","attr":"edit"%';

UPDATE `ry-vue`.flow_form
SET layout_json = REPLACE(layout_json, '"field":"offset_amount","attr":"edit"', '"field":"offset_amount","attr":"ro"')
WHERE form_id = 21
  AND layout_json LIKE '%"field":"offset_amount","attr":"edit"%';

UPDATE `ry-vue`.flow_node_form
SET layout_json = REPLACE(layout_json, '"field":"loan_amount","attr":"edit"', '"field":"loan_amount","attr":"ro"'),
    update_time = NOW()
WHERE model_key = 'expenseReimburse'
  AND layout_json LIKE '%"field":"loan_amount","attr":"edit"%';

UPDATE `ry-vue`.flow_node_form
SET layout_json = REPLACE(layout_json, '"field":"offset_amount","attr":"edit"', '"field":"offset_amount","attr":"ro"'),
    update_time = NOW()
WHERE model_key = 'expenseReimburse'
  AND layout_json LIKE '%"field":"offset_amount","attr":"edit"%';

-- 2) 新增「选是→解除只读」规则（借款金额 / 本次冲抵金额）
UPDATE `ry-vue`.flow_form
SET ext_json = JSON_ARRAY_APPEND(ext_json, '$.rules', CAST('{"id":"rule_loan_editable","name":"冲抵借款时借款金额可填写","type":"readonly","enabled":true,"target":"loan_amount","trigger":["need_loan_offset"],"when":{"op":"and","conditions":[{"cmp":"eq","left":{"field":"need_loan_offset"},"right":{"value":"是"}}]},"action":{"readonly":false},"message":""}' AS JSON))
WHERE form_id = 21
  AND NOT JSON_CONTAINS(JSON_EXTRACT(ext_json, '$.rules[*].id'), JSON_QUOTE('rule_loan_editable'));

UPDATE `ry-vue`.flow_form
SET ext_json = JSON_ARRAY_APPEND(ext_json, '$.rules', CAST('{"id":"rule_offset_editable","name":"冲抵借款时本次冲抵金额可填写","type":"readonly","enabled":true,"target":"offset_amount","trigger":["need_loan_offset"],"when":{"op":"and","conditions":[{"cmp":"eq","left":{"field":"need_loan_offset"},"right":{"value":"是"}}]},"action":{"readonly":false},"message":""}' AS JSON))
WHERE form_id = 21
  AND NOT JSON_CONTAINS(JSON_EXTRACT(ext_json, '$.rules[*].id'), JSON_QUOTE('rule_offset_editable'));

-- 3) 合计大写自动生成
UPDATE `ry-vue`.flow_form
SET ext_json = JSON_ARRAY_APPEND(ext_json, '$.rules', CAST('{"id":"rule_rmb_upper_cn","name":"合计大写自动生成","type":"derive","enabled":true,"target":"total_amount_cn","trigger":["total_amount"],"expr":{"fn":"rmbUpper","args":[{"field":"total_amount"}]},"when":{"op":"and","conditions":[]},"action":{},"message":""}' AS JSON))
WHERE form_id = 21
  AND NOT JSON_CONTAINS(JSON_EXTRACT(ext_json, '$.rules[*].id'), JSON_QUOTE('rule_rmb_upper_cn'));

-- 4) 停用误报校验规则（大写已自动生成，无需人工比对）
UPDATE `ry-vue`.flow_form
SET ext_json = JSON_REPLACE(
    ext_json,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(ext_json, 'one', 'ai_1794911766162_1', NULL, '$.rules[*].id')), '.id', '.enabled'),
    CAST('false' AS JSON)
)
WHERE form_id = 21
  AND JSON_SEARCH(ext_json, 'one', 'ai_1794911766162_1', NULL, '$.rules[*].id') IS NOT NULL;
