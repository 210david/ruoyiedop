-- =============================================
-- 退货管理状态字典更新：新增"已驳回"状态
-- 退货审批支持驳回流程，驳回后可修改重新提交
-- =============================================

-- 新增退货状态字典：已驳回(5)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '已驳回', '5', 'pms_return_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_return_status' AND dict_value='5');

-- 验证更新结果
SELECT dict_sort, dict_label, dict_value, list_class
FROM sys_dict_data
WHERE dict_type = 'pms_return_status'
ORDER BY dict_sort;
