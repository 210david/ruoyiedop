-- =============================================
-- 质量管理菜单图标优化更新脚本
-- =============================================

-- 更新SN追溯菜单图标（原barcode在项目中无对应svg，改为scan）
UPDATE sys_menu SET icon = 'scan' WHERE menu_name = 'SN追溯' AND perms = 'qms:trace:sn:list';

-- 验证更新结果
SELECT menu_name, icon, component, perms
FROM sys_menu
WHERE perms LIKE 'qms:trace:sn%'
ORDER BY order_num;
