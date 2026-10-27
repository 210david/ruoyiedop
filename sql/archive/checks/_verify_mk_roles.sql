-- 验证新创建的MK角色
SELECT role_id, role_name, role_key, role_sort, data_scope, status, remark
FROM sys_role
WHERE role_id IN (300, 301, 302, 303, 304)
ORDER BY role_id;
