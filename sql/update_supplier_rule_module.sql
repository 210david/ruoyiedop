-- =============================================
-- 将供应商编号规则的 module 从 'wms' 改为 'pms'
-- 使其在采购管理/编号规则页面中显示
-- =============================================

-- 更新供应商编号规则的 module
UPDATE mk_number_rule 
SET module = 'pms', 
    remark = CONCAT(remark, ' (从wms迁移到pms模块管理)')
WHERE rule_code = 'wms_supplier' AND module = 'wms';

-- 验证更新结果
SELECT '供应商编号规则模块更新完成' AS result;
SELECT rule_id, rule_code, module, rule_name, remark 
FROM mk_number_rule 
WHERE rule_code = 'wms_supplier';

-- 检查采购管理模块下的所有编号规则
SELECT '采购管理模块(pms)下的所有编号规则' AS info;
SELECT rule_id, rule_code, rule_name, module, prefix, remark 
FROM mk_number_rule 
WHERE module = 'pms' 
ORDER BY rule_code;
