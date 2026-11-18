UPDATE `ry-vue`.act_hi_varinst SET TEXT_ = '148' WHERE PROC_INST_ID_ = 'c641d567-e2e4-11f1-9814-84a938328f53' AND NAME_ = 'total_amount';
UPDATE `ry-vue`.act_hi_varinst SET TEXT_ = '壹佰肆拾捌圆整' WHERE PROC_INST_ID_ = 'c641d567-e2e4-11f1-9814-84a938328f53' AND NAME_ = 'total_amount_cn';
SELECT NAME_, TEXT_, VAR_TYPE_ FROM `ry-vue`.act_hi_varinst WHERE PROC_INST_ID_ = 'c641d567-e2e4-11f1-9814-84a938328f53' AND NAME_ IN ('total_amount', 'total_amount_cn');
SELECT NAME_, TEXT_, VAR_TYPE_ FROM `ry-vue`.act_ru_variable WHERE PROC_INST_ID_ = 'c641d567-e2e4-11f1-9814-84a938328f53' AND NAME_ IN ('total_amount', 'total_amount_cn');
