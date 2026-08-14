-- =============================================
-- 补充 NCR来源类型字典数据
-- 问题：NCR台账"来源类型"下拉框无值，因 qms_ncr_source 字典未定义
-- =============================================

-- 1. 新增字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('NCR来源类型', 'qms_ncr_source', '0', 'admin', sysdate(), '不合格品报告来源类型');

-- 2. 新增字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '检验', 'inspection', 'qms_ncr_source', '', 'primary', 'Y', '0', 'admin', sysdate()),
(2, '客诉', 'complaint', 'qms_ncr_source', '', 'danger', 'N', '0', 'admin', sysdate()),
(3, '巡检', 'patrol', 'qms_ncr_source', '', 'warning', 'N', '0', 'admin', sysdate());
