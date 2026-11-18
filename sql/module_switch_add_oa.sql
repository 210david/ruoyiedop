-- ---------------------------------------------------------------------------
-- 模块开关补齐与路由修正
--
-- 1) 补登「综合办公(oa)」模块：该模块有顶级菜单但 sys_module 无登记，
--    导致后台「模块开关」管不到它，工作台「自定义常用功能」里永远显示。
-- 2) 修正「决策支持」菜单 path：原值 '/bi'（带前导斜杠），
--    RuoYi 对一级 M 目录会自动补 '/'，最终路由为 '//bi'，点击 404。
--
-- 执行：mysql -uroot -p ry-vue < sql/module_switch_add_oa.sql （幂等，可重复执行）
-- ---------------------------------------------------------------------------

-- 1. 修正决策支持（一级 M 目录 path 不能带前导斜杠）
UPDATE sys_menu SET path = 'bi', update_time = NOW()
 WHERE menu_id = 9062263 AND path = '/bi';

-- 2. 补登综合办公模块
INSERT INTO sys_module (module_name, module_key, root_menu_id, status, is_core, order_num, create_by, create_time, remark)
SELECT '综合办公', 'oa', 9064000, '0', '0', 20, 'admin', NOW(), '综合办公模块（印章/车辆/司机/保险），级联菜单 9064000 及其子菜单'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE module_key = 'oa');

-- 3. 校验：所有顶级业务目录都应能在 sys_module 找到对应开关
SELECT m.menu_id, m.menu_name, m.path,
       (SELECT sm.module_key FROM sys_module sm WHERE sm.root_menu_id = m.menu_id) AS bound_key
  FROM sys_menu m
 WHERE m.parent_id = 0 AND m.menu_type = 'M' AND m.status = '0'
 ORDER BY m.order_num;
