-- ============================================================
-- 资金管理菜单拆分：「资金账户与流水」拆为「资金账户」「资金流水」两个独立菜单
-- 前置：fin_menu_restructure.sql 已执行（906120/906121 已存在）
-- 回退：见文末回退语句
-- ============================================================

-- 1. 原「资金账户与流水」更名为「资金账户」（页面按路由 path 展示资金账户区）
UPDATE sys_menu SET menu_name='资金账户', remark='原资金账户与流水页签拆分（资金账户页）', update_by='admin', update_time=NOW()
WHERE menu_id=906120;

-- 2. 新增「资金流水」独立菜单（独立 SFC 组件，避免同一组件多路由导致客户端跳转白板）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906122, '资金流水', 905979, 2, 'fundFlow', 'fin/fund/flow', '', '', 1, 0, 'C', '0', '0', 'fin:fund:flow', 'money', 'admin', NOW(), '资金流水页（独立组件，路径避免与流程中心顶级目录 flow 重名导致路由 name 冲突）');

-- 2.1 「银行对账」改为独立 SFC 组件（fin/fund/reconcile）
UPDATE sys_menu SET component='fin/fund/reconcile', remark='银行对账页（独立组件）' WHERE menu_id=906121;

-- 3. 「银行对账」顺延排序
UPDATE sys_menu SET order_num=3 WHERE menu_id=906121;

-- ============================================================
-- 回退语句（如需撤销拆分）
-- DELETE FROM sys_menu WHERE menu_id=906122;
-- UPDATE sys_menu SET menu_name='资金账户与流水', order_num=1, remark='原资金管理页签独立菜单（默认资金账户页签）' WHERE menu_id=906120;
-- UPDATE sys_menu SET order_num=2 WHERE menu_id=906121;
-- ============================================================
