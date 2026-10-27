-- 修复已有菜单权限：将 safety:arch: 改为 safety:
UPDATE sys_menu SET perms = REPLACE(perms, 'safety:arch:', 'safety:') WHERE perms LIKE 'safety:arch:%';
