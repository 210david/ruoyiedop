-- 删除齐套检查状态中的"待检查"字典项
-- 原因：一键齐套检查后端直接生成"已通过(1)"或"缺料(2)"状态，不存在"待检查(0)"状态
DELETE FROM sys_dict_data WHERE dict_type = 'mms_kit_status' AND dict_value = '0';
