-- ============================================================
-- MMS 排产管理 - 删除多余的"排产管理"列表菜单，保留"排产工作台"
-- 执行时间: 2026-08-19
-- 说明: 排产工作台(甘特图)已完全覆盖排产管理(列表)的功能，
--       且体验更好(可视化拖拽)，排产管理列表菜单多余，予以删除。
--       按钮权限(F类菜单)迁移到排产工作台下。
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 一、查出两个菜单的 ID 并存入变量
-- ============================================================

-- 排产管理（旧列表菜单，待删除）
SET @legacy_menu_id = (
    SELECT menu_id FROM sys_menu
    WHERE perms = 'mms:schedule:list' AND path = 'schedule' AND component = 'mms/schedule/index'
    LIMIT 1
);

-- 排产工作台（甘特图菜单，保留）
SET @gantt_menu_id = (
    SELECT menu_id FROM sys_menu
    WHERE perms = 'mms:schedule:list' AND path = 'gantt' AND menu_type = 'C'
    LIMIT 1
);

-- ============================================================
-- 二、将按钮权限的 parent_id 迁移到"排产工作台"菜单
-- ============================================================

UPDATE sys_menu SET parent_id = @gantt_menu_id
WHERE parent_id = @legacy_menu_id AND menu_type = 'F';

-- ============================================================
-- 三、删除"排产管理"列表菜单本身
-- ============================================================

DELETE FROM sys_menu WHERE menu_id = @legacy_menu_id;

-- ============================================================
-- 四、清理 sys_role_menu 中的孤立记录
-- ============================================================

DELETE FROM sys_role_menu WHERE menu_id = @legacy_menu_id;

-- ============================================================
-- 五、验证结果
-- ============================================================

SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms
FROM sys_menu
WHERE perms LIKE 'mms:schedule:%'
ORDER BY parent_id, order_num;
