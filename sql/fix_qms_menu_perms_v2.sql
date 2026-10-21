-- =============================================
-- QMS 菜单权限补全修复脚本
-- 修复内容：
--   1. 物料质量属性 - 补充 export 按钮权限
--   2. SQM推送日志 - 补充 export 和 remove 按钮权限
--   3. 电子签名 - 补充 add 按钮权限
-- 日期：2026-08-15
-- 说明：幂等设计，可重复执行
-- =============================================

-- =============================================
-- 1、物料质量属性 - 补充"属性导出"按钮权限
-- =============================================
SET @qmsMatAttrId = (SELECT menu_id FROM sys_menu WHERE perms = 'qms:materialattr:list' AND menu_type = 'C' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '属性导出', @qmsMatAttrId, 5, '', 'F', '0', '0', 'qms:materialattr:export', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'qms:materialattr:export' AND parent_id = @qmsMatAttrId
);

-- 授权admin角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms = 'qms:materialattr:export'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);


-- =============================================
-- 2、SQM推送日志 - 补充"导出"和"删除"按钮权限
-- =============================================
SET @qmsSqmPushLogId = (SELECT menu_id FROM sys_menu WHERE perms = 'qms:sqmPushLog:list' AND menu_type = 'C' LIMIT 1);

-- 补充"推送日志导出"按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '推送日志导出', @qmsSqmPushLogId, 2, '', 'F', '0', '0', 'qms:sqmPushLog:export', 'admin', sysdate()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'qms:sqmPushLog:export' AND parent_id = @qmsSqmPushLogId
);

-- 补充"推送日志删除"按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '推送日志删除', @qmsSqmPushLogId, 3, '', 'F', '0', '0', 'qms:sqmPushLog:remove', 'admin', sysdate()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'qms:sqmPushLog:remove' AND parent_id = @qmsSqmPushLogId
);

-- 授权admin角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN ('qms:sqmPushLog:export', 'qms:sqmPushLog:remove')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);


-- =============================================
-- 3、电子签名 - 补充"签名新增"按钮权限
-- =============================================
SET @qmsEsigId = (SELECT menu_id FROM sys_menu WHERE perms = 'qms:esig:list' AND menu_type = 'C' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '签名新增', @qmsEsigId, 3, '', 'F', '0', '0', 'qms:esig:add', 'admin', sysdate()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'qms:esig:add' AND parent_id = @qmsEsigId
);

-- 授权admin角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms = 'qms:esig:add'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);


-- =============================================
-- 4、验证修复结果
-- =============================================
SELECT '===== QMS菜单权限补全修复完成 =====' AS result;

-- 验证物料质量属性按钮权限
SELECT '物料质量属性按钮权限' AS module, menu_name, perms, menu_type
FROM sys_menu WHERE parent_id = @qmsMatAttrId ORDER BY order_num;

-- 验证SQM推送日志按钮权限
SELECT 'SQM推送日志按钮权限' AS module, menu_name, perms, menu_type
FROM sys_menu WHERE parent_id = @qmsSqmPushLogId ORDER BY order_num;

-- 验证电子签名按钮权限
SELECT '电子签名按钮权限' AS module, menu_name, perms, menu_type
FROM sys_menu WHERE parent_id = @qmsEsigId ORDER BY order_num;
