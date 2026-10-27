-- =============================================
-- 修复流程请假按钮孤儿菜单
-- 原因：重建“流程中心”时仅删除父菜单，旧的 flowable:leave:* 按钮仍引用已删除的父级。
-- 结果：菜单管理树可能把这些孤儿按钮错误展示在“系统管理”附近。
-- 本脚本幂等，可重复执行。
-- =============================================
SET NAMES utf8mb4;
START TRANSACTION;

SET @flowCenterId = (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_name = '流程中心' AND parent_id = 0
    ORDER BY menu_id DESC
    LIMIT 1
);

SET @leaveMenuId = (
    SELECT menu_id
    FROM sys_menu
    WHERE perms = 'flowable:leave:list'
      AND menu_type = 'C'
      AND parent_id = @flowCenterId
    ORDER BY menu_id DESC
    LIMIT 1
);

-- 当前平台已采用人力资源“请假管理”，流程示例页不存在时直接清理残留按钮；
-- 若流程示例页仍存在，则把按钮重新挂回该页面并规范按钮字段。
DELETE FROM sys_role_menu
WHERE menu_id IN (
    SELECT menu_id
    FROM sys_menu
    WHERE perms LIKE 'flowable:leave:%'
      AND perms <> 'flowable:leave:list'
      AND @leaveMenuId IS NULL
);

DELETE FROM sys_menu
WHERE perms LIKE 'flowable:leave:%'
  AND perms <> 'flowable:leave:list'
  AND @leaveMenuId IS NULL;

UPDATE sys_menu
SET parent_id = @leaveMenuId,
    menu_type = 'F',
    path = '#',
    component = '',
    visible = '0',
    status = '0',
    icon = '#',
    update_by = 'admin',
    update_time = NOW()
WHERE perms LIKE 'flowable:leave:%'
  AND perms <> 'flowable:leave:list'
  AND @leaveMenuId IS NOT NULL;

COMMIT;

-- 验证：应返回 0 行
SELECT m.menu_id, m.menu_name, m.parent_id, m.menu_type, m.perms
FROM sys_menu m
LEFT JOIN sys_menu p ON p.menu_id = m.parent_id
WHERE m.parent_id <> 0
  AND p.menu_id IS NULL;
