-- =============================================
-- QMS 检验标准库补充脚本
-- 1. 添加 qms_std 编号规则种子数据
-- 2. 添加行业模板种子数据
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- =============================================
-- 一、添加检验标准编号规则
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_std', 'qms', '检验标准编号', 'STD', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '检验标准库编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_std');

-- =============================================
-- 二、添加行业模板种子数据
-- =============================================
INSERT INTO `qms_industry_tpl` (`tpl_code`, `tpl_name`, `industry_type`, `audit_force`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT 'TPL-DEFAULT', '通用质量模板', '1', '0', '0', '0', 'admin', sysdate(), '新能源材料行业默认质量模板'
WHERE NOT EXISTS (SELECT 1 FROM qms_industry_tpl WHERE tpl_code='TPL-DEFAULT');

INSERT INTO `qms_industry_tpl` (`tpl_code`, `tpl_name`, `industry_type`, `audit_force`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT 'TPL-AL', '绿色铝加工质量模板', '2', '0', '0', '0', 'admin', sysdate(), '绿色铝精加工行业质量模板'
WHERE NOT EXISTS (SELECT 1 FROM qms_industry_tpl WHERE tpl_code='TPL-AL');

INSERT INTO `qms_industry_tpl` (`tpl_code`, `tpl_name`, `industry_type`, `audit_force`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT 'TPL-CHEM', '精细化工质量模板', '3', '1', '0', '0', 'admin', sysdate(), '高端精细化工行业质量模板（强制审计）'
WHERE NOT EXISTS (SELECT 1 FROM qms_industry_tpl WHERE tpl_code='TPL-CHEM');

INSERT INTO `qms_industry_tpl` (`tpl_code`, `tpl_name`, `industry_type`, `audit_force`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT 'TPL-EQUIP', '先进装备质量模板', '4', '0', '0', '0', 'admin', sysdate(), '先进装备制造行业质量模板'
WHERE NOT EXISTS (SELECT 1 FROM qms_industry_tpl WHERE tpl_code='TPL-EQUIP');

INSERT INTO `qms_industry_tpl` (`tpl_code`, `tpl_name`, `industry_type`, `audit_force`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT 'TPL-FOOD', '高原食品质量模板', '5', '1', '0', '0', 'admin', sysdate(), '高原特色食品加工行业质量模板（强制审计+电子签名）'
WHERE NOT EXISTS (SELECT 1 FROM qms_industry_tpl WHERE tpl_code='TPL-FOOD');

-- =============================================
-- 三、验证数据
-- =============================================
SELECT '检验标准库补充脚本执行完成' AS result;
SELECT rule_code, rule_name, module, prefix FROM mk_number_rule WHERE rule_code = 'qms_std';
SELECT tpl_id, tpl_code, tpl_name, industry_type, status FROM qms_industry_tpl WHERE del_flag = '0' ORDER BY tpl_id;
