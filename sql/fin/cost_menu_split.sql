-- ============================================================
-- 存货成本菜单拆分（路径A：component 复用 + 按路由 path 激活页签）
-- 目标：存货成本页 3 个页签拆为 3 个独立菜单，与其他菜单页面一致（一页一菜单）
--   日常核算(906101)
--   ├─ 存货成本(905953，原改造为「成本台账」，path=ledger)
--   ├─ 结转流水(906141，新增)
--   └─ 物料税率维护(906142，新增)
-- 三个菜单 component 均复用 fin/cost/index，页面按路由 path 激活对应页签并隐藏页签栏
-- 回退：见文末回退语句
-- ============================================================

-- 1. 原「存货成本」改造为「成本台账」（component 不变，path 改为 ledger）
UPDATE sys_menu
SET menu_name = '成本台账', path = 'ledger', remark = '原存货成本改造：成本台账页签独立菜单'
WHERE menu_id = 905953;

-- 2. 新增「结转流水」「物料税率维护」两个独立菜单（is_cache=0，同一组件跨路由不缓存）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906141, '结转流水', 906101, 5, 'carry', 'fin/cost/index', '', '', 1, 0, 'C', '0', '0', 'fin:cost:list', 'list', 'admin', NOW(), '存货成本页签独立菜单（结转流水）');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906142, '物料税率维护', 906101, 6, 'tax', 'fin/cost/index', '', '', 1, 0, 'C', '0', '0', 'fin:materialTax:list', 'edit', 'admin', NOW(), '存货成本页签独立菜单（物料税率维护）');

-- ============================================================
-- 回退语句（如需撤销拆分）
-- DELETE FROM sys_menu WHERE menu_id IN (906141, 906142);
-- UPDATE sys_menu SET menu_name='存货成本', path='cost', remark='' WHERE menu_id=905953;
-- ============================================================
