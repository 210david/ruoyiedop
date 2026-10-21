-- =============================================
-- 隐藏工厂日历菜单（中小企业一期不需要排产/日历功能）
-- 方案A：仅隐藏菜单入口，代码和表结构原样保留
-- 将来二期做排产/OEE 时恢复 visible='0' 即可
-- =============================================

-- 隐藏工厂日历菜单页面
UPDATE sys_menu SET visible = '1' WHERE perms = 'mms:calendar:list' AND menu_type = 'C';

-- 验证结果
SELECT menu_id, menu_name, path, component, visible, status, perms
FROM sys_menu
WHERE perms LIKE 'mms:calendar%'
ORDER BY menu_id;

SELECT '工厂日历菜单已隐藏（visible=1）' AS result;
