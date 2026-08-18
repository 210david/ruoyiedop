@echo off
echo === Cleaning duplicate MMS dict data ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "DELETE d1 FROM sys_dict_data d1 INNER JOIN sys_dict_data d2 WHERE d1.dict_type = d2.dict_type AND d1.dict_value = d2.dict_value AND d1.dict_code > d2.dict_code AND d1.dict_type LIKE 'mms_%%';"
echo.
echo === After cleanup - MMS Dict Data Counts ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_type, COUNT(*) AS cnt FROM sys_dict_data WHERE dict_type LIKE 'mms_%%' GROUP BY dict_type ORDER BY dict_type;"
