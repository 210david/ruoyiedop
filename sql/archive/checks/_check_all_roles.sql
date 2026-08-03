-- 查看现有全部角色
SELECT role_id, role_name, role_key, role_sort, data_scope, status, remark
FROM sys_role
WHERE del_flag = '0'
ORDER BY role_id;
