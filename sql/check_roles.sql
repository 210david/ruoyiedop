USE `ry-vue`;
SELECT role_id, role_name, role_key, remark FROM sys_role WHERE role_id IN (1,2,110,111) ORDER BY role_id;
SELECT role_id, role_name, role_key FROM sys_role ORDER BY role_id LIMIT 20;
