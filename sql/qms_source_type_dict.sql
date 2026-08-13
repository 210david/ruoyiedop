-- =============================================
-- 检验任务来源类型字典
-- =============================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('检验任务来源类型', 'qms_source_type', '0', 'admin', sysdate(), '检验任务来源类型');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '采购入库', 'arrival', 'qms_source_type', '', 'primary', 'Y', '0', 'admin', sysdate()),
(2, '生产工单', 'production', 'qms_source_type', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '出货交付', 'delivery', 'qms_source_type', '', 'warning', 'N', '0', 'admin', sysdate()),
(4, '手动创建', 'manual', 'qms_source_type', '', 'info', 'N', '0', 'admin', sysdate());
