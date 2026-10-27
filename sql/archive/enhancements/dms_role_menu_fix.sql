-- 修复DMS权限的角色映射
-- 注意：第一部分（INSERT INTO sys_menu）已在之前的导入中完成

-- 为超级管理员角色分配新增权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m 
WHERE m.perms IN ('dms:partstock:remove', 'dms:partalert:remove');
