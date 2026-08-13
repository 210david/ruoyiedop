-- Safety Management Module - Role Permission Setup Script

USE `ry-vue`;

-- Step 1: Update role info for safety_officer
UPDATE sys_role SET 
    role_name = '安全员',
    role_key = 'safety_officer',
    role_sort = 7,
    data_scope = '3',
    menu_check_strictly = 1,
    dept_check_strictly = 1,
    status = '0',
    remark = 'Safety Officer: daily inspection, hazard reporting and rectification, expiry reminder handling'
WHERE role_id = 110;

-- Step 2: Update role info for safety_manager
UPDATE sys_role SET 
    role_name = '安全主管',
    role_key = 'safety_manager',
    role_sort = 8,
    data_scope = '2',
    menu_check_strictly = 1,
    dept_check_strictly = 1,
    status = '0',
    remark = 'Safety Manager: safety management coordination, hazard approval, data analysis'
WHERE role_id = 111;

-- Step 3: Clear old permissions
DELETE FROM sys_role_menu WHERE role_id = 110 AND menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:%' OR 
    (menu_name IN ('安全生产管理','一企一档','风险管控','教育培训','应急管理','到期提醒','安全驾驶舱') AND menu_type = 'M')
);

DELETE FROM sys_role_menu WHERE role_id = 111 AND menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:%' OR 
    (menu_name IN ('安全生产管理','一企一档','风险管控','教育培训','应急管理','到期提醒','安全驾驶舱') AND menu_type = 'M')
);

-- Step 4: Get menu IDs
SET @safetyMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);
SET @yqdMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '一企一档' AND parent_id = @safetyMenuId);
SET @skMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '风险管控' AND parent_id = @safetyMenuId);
SET @pxMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '教育培训' AND parent_id = @safetyMenuId);
SET @yjMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '应急管理' AND parent_id = @safetyMenuId);
SET @dqMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '到期提醒' AND parent_id = @safetyMenuId);
SET @dpMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '安全驾驶舱' AND parent_id = @safetyMenuId);
SET @bmMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @safetyMenuId);

-- Step 5: Assign menu directories to safety_officer (role_id=110)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES 
(110, @safetyMenuId),
(110, @yqdMenuId),
(110, @skMenuId),
(110, @pxMenuId),
(110, @yjMenuId),
(110, @dqMenuId),
(110, @dpMenuId);

-- Step 6: Assign function permissions to safety_officer (exclude remove, approve, export, verify, numberRule)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 110, menu_id FROM sys_menu WHERE perms LIKE 'safety:%'
  AND perms NOT LIKE 'safety:%:remove'
  AND perms NOT LIKE 'safety:%:approve'
  AND perms NOT LIKE 'safety:%:export'
  AND perms NOT LIKE 'safety:%:verify'
  AND perms NOT LIKE 'safety:numberRule:%';

-- Step 7: Assign menu directories to safety_manager (role_id=111)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES 
(111, @safetyMenuId),
(111, @yqdMenuId),
(111, @skMenuId),
(111, @pxMenuId),
(111, @yjMenuId),
(111, @dqMenuId),
(111, @dpMenuId),
(111, @bmMenuId);

-- Step 8: Assign all safety permissions to safety_manager
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 111, menu_id FROM sys_menu WHERE perms LIKE 'safety:%';

-- Step 9: Verify results
SELECT '=== Role Info ===' AS info;
SELECT role_id, role_name, role_key, data_scope, remark FROM sys_role WHERE role_id IN (110, 111);

SELECT '=== Safety Officer Permission Count ===' AS info;
SELECT COUNT(*) as total_permissions FROM sys_role_menu rm 
JOIN sys_menu m ON rm.menu_id = m.menu_id 
WHERE rm.role_id = 110 AND (m.perms LIKE 'safety:%' OR m.menu_type = 'M');

SELECT '=== Safety Manager Permission Count ===' AS info;
SELECT COUNT(*) as total_permissions FROM sys_role_menu rm 
JOIN sys_menu m ON rm.menu_id = m.menu_id 
WHERE rm.role_id = 111 AND (m.perms LIKE 'safety:%' OR m.menu_type = 'M');

SELECT '=== Safety Officer Sample Permissions ===' AS info;
SELECT m.menu_name, m.perms FROM sys_role_menu rm 
JOIN sys_menu m ON rm.menu_id = m.menu_id 
WHERE rm.role_id = 110 AND m.perms LIKE 'safety:%' 
ORDER BY m.perms LIMIT 10;

SELECT '=== Setup Complete ===' AS info;
