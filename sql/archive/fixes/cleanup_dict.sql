-- 删除重复的字典数据，每组只保留dict_code最小的一条
DELETE d1 FROM sys_dict_data d1
INNER JOIN sys_dict_data d2
ON d1.dict_type = d2.dict_type
AND d1.dict_value = d2.dict_value
AND d1.dict_code > d2.dict_code
WHERE d1.dict_type = 'dms_eq_change_type';
