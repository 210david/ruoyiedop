-- ============================================================
-- 综合办公：印章“外带使用中”改造
-- 1) 状态字典：使用中 -> 外带使用中（仅携出盖章占用）
-- 2) 历史数据纠正：现场盖章不应占用，恢复为正常
-- 执行日期：2026-11-18
-- ============================================================

UPDATE sys_dict_data
SET dict_label = '外带使用中'
WHERE dict_type = 'oa_seal_status' AND dict_value = '3';

-- 已将现场盖章流程关联的印章状态错误置为“使用中”的，恢复为正常
UPDATE biz_oa_seal s
JOIN flow_data_17 f ON f.process_instance_id = s.flow_instance_id
SET s.status = '0'
WHERE s.status = '3'
  AND (f.use_mode IS NULL OR f.use_mode <> '携出盖章');
