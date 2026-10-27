-- 采购合同状态字典修正
-- 1. 更新状态2: 执行中 → 变更审批中（与实际业务逻辑一致）
-- 2. 新增状态5: 待审核
-- 3. 新增状态6: 驳回

-- 更新状态2的标签和样式
UPDATE sys_dict_data SET dict_label = '变更审批中', list_class = 'warning', update_time = sysdate()
WHERE dict_type = 'pms_contract_status' AND dict_value = '2';

-- 新增状态5: 待审核
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '待审核', '5', 'pms_contract_status', '', 'warning', 'N', '0', 'admin', sysdate(), '待审核状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'pms_contract_status' AND dict_value = '5');

-- 新增状态6: 驳回
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, '驳回', '6', 'pms_contract_status', '', 'danger', 'N', '0', 'admin', sysdate(), '审批驳回状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'pms_contract_status' AND dict_value = '6');

-- 更新实体类Excel注解提示（仅注释，不影响数据库）
-- 0=草稿,1=已签订,2=变更审批中,3=已到期,4=已终止,5=待审核,6=驳回
