-- 质量目标：补充导出权限
-- 适用于已执行过 qms_p1_p2_all.sql 的环境
-- 找到"质量目标"菜单ID
SET @qmsTargetId = (SELECT menu_id FROM sys_menu WHERE perms = 'qms:target:list' AND menu_type = 'C' LIMIT 1);

-- 插入导出权限（如果不存在）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '目标导出', @qmsTargetId, 5, '', 'F', '0', '0', 'qms:target:export', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'qms:target:export' AND parent_id = @qmsTargetId
);

-- 将导出权限授予管理员角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms = 'qms:target:export'
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm JOIN sys_menu m ON rm.menu_id = m.menu_id
    WHERE m.perms = 'qms:target:export' AND rm.role_id = 1
);
