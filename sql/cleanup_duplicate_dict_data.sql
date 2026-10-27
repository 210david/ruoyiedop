-- 清理 sys_dict_data 重复字典项（字典初始化 SQL 多次执行导致）
-- 策略：按 (dict_type, dict_value, dict_label) 分组，保留 dict_code 最小的一条，删除其余
-- 范围：全表（含 hr_/dms_/qms_ 等所有模块字典）
-- 幂等：可重复执行，无重复时无影响
DELETE d1 FROM sys_dict_data d1
JOIN sys_dict_data d2
  ON d1.dict_type = d2.dict_type
 AND d1.dict_value = d2.dict_value
 AND d1.dict_label = d2.dict_label
 AND d1.dict_code > d2.dict_code;

-- 验证：以下查询应返回空
-- SELECT dict_type, dict_value, dict_label, COUNT(*) cnt FROM sys_dict_data
-- GROUP BY dict_type, dict_value, dict_label HAVING cnt > 1;
