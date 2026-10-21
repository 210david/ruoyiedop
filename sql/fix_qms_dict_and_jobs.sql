-- =============================================
-- 修复QMS乱码字典和重复定时任务
-- =============================================

-- 1. 修复乱码字典类型
UPDATE sys_dict_type SET dict_name = '电子签名业务类型' WHERE dict_type = 'qms_esig_biz_type';
UPDATE sys_dict_type SET dict_name = '车间节点类型' WHERE dict_type = 'qms_workshop_node_type';

-- 2. 删除乱码字典数据并重新插入正确的
DELETE FROM sys_dict_data WHERE dict_type = 'qms_esig_biz_type';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '检验判定', 'insp_judge',  'qms_esig_biz_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '检验员判定'),
(2, 'NCR审批',  'ncr_approve', 'qms_esig_biz_type', '', 'success', 'N', '0', 'admin', sysdate(), 'NCR审批'),
(3, 'NCR处置',  'ncr_disposition', 'qms_esig_biz_type', '', 'warning', 'N', '0', 'admin', sysdate(), 'NCR处置'),
(4, 'NCR验证',  'ncr_verify',  'qms_esig_biz_type', '', 'info', 'N', '0', 'admin', sysdate(), 'NCR验证');

DELETE FROM sys_dict_data WHERE dict_type = 'qms_workshop_node_type';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '公司',   '1', 'qms_workshop_node_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '公司节点'),
(2, '工厂',   '2', 'qms_workshop_node_type', '', 'success', 'N', '0', 'admin', sysdate(), '工厂节点'),
(3, '车间',   '3', 'qms_workshop_node_type', '', 'warning', 'N', '0', 'admin', sysdate(), '车间节点'),
(4, '工位',   '4', 'qms_workshop_node_type', '', 'info',    'N', '0', 'admin', sysdate(), '工位节点');

-- 3. 删除重复的定时任务（保留最新的一组: 110, 111）
DELETE FROM sys_job WHERE job_id IN (106, 107, 108, 109);
