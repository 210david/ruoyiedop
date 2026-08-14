-- 删除质量文档状态中的"审批中"状态（值=1）
-- 审批状态是多余的，文档状态简化为：草稿(0)、生效(2)、作废(3)
DELETE FROM sys_dict_data WHERE dict_type = 'qms_doc_status' AND dict_value = '1';

-- 如果有文档的状态是"1"(审批中)，将其改为"0"(草稿)
UPDATE qms_doc SET doc_status = '0' WHERE doc_status = '1';
