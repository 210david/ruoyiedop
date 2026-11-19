-- ============================================================
-- 调整顶部导航菜单顺序：综合办公移至流程中心之后
-- 综合办公(9064000) order_num 13 -> 4，紧跟流程中心(905824, order_num 3)
-- 执行日期：2026-11-19
-- ============================================================

UPDATE sys_menu
SET order_num = 4,
    update_time = NOW()
WHERE menu_id = 9064000;

-- 验证：顶级菜单顺序
-- SELECT menu_id, menu_name, order_num, path FROM sys_menu WHERE parent_id = 0 ORDER BY order_num;
