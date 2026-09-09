-- =============================================
-- 修复 safety_hazard_level 字典数据
-- 问题：upgrade_safety_material_v2.sql 中 DELETE 了原数据后又 INSERT 了"危险源等级"的一级~四级数据
-- 但后续执行可能失败，导致 safety_hazard_level 字典数据为空
-- 本脚本恢复为正确的隐患等级数据：一般隐患 / 重大隐患
-- =============================================

-- 先删除可能存在的错误数据
DELETE FROM sys_dict_data WHERE dict_type = 'safety_hazard_level';

-- 重新插入正确的隐患等级数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '一般隐患', '1', 'safety_hazard_level', 'warning', '', 'Y', '0', 'admin', NOW(), '危害较小，能够及时整改的隐患'),
(2, '重大隐患', '2', 'safety_hazard_level', 'danger', '', 'N', '0', 'admin', NOW(), '危害严重，可能造成重大生产安全事故的隐患');

-- 验证结果
SELECT '=== 修复后的 safety_hazard_level ===' AS info;
SELECT dict_code, dict_sort, dict_label, dict_value, dict_type, css_class FROM sys_dict_data WHERE dict_type = 'safety_hazard_level' ORDER BY dict_sort;
