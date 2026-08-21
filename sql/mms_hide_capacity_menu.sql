-- =============================================
-- 隐藏产能负荷菜单（中小企业一期不需要排产/产能负荷功能）
-- 方案A：仅隐藏菜单入口，代码和表结构原样保留
-- 将来二期做排产/产能负荷时恢复 visible='0' 即可
-- =============================================

-- 隐藏产能负荷菜单页面
UPDATE sys_menu SET visible = '1' WHERE perms = 'mms:capacity:list' AND menu_type = 'C';

-- 验证结果
SELECT menu_id, menu_name, path, component, visible, status, perms
FROM sys_menu
WHERE perms LIKE 'mms:capacity%'
ORDER BY menu_id;

SELECT '产能负荷菜单已隐藏（visible=1）' AS result;
