-- =====================================================
-- 合同变更审批流程增强 - 新增"变更审批中"状态
-- 状态流: 已生效(2) -> 变更审批中(5) -> 已生效(2)
-- =====================================================

-- 1. 新增字典数据：合同状态 - 变更审批中(5)
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
VALUES (6, '变更审批中', '5', 'marketing_contract_status', '', 'warning', 'N', '0', 'admin', NOW(), '变更审批中状态');
