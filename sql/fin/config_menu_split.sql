-- ============================================================
-- 财务配置菜单拆分（路径A：component 复用 + 按路由 path 激活页签）
-- 目标：财务配置页 4 个页签拆为 4 个独立菜单，与其他菜单页面一致（一页一菜单）
--   基础设置(906100)
--   ├─ 往来单位(905940) / 会计科目(905946)
--   ├─ 期末结账(906007，原财务配置改造)
--   ├─ 凭证规则(906131，新增)
--   ├─ 财务参数(906132，新增)
--   └─ 成本方法(906133，新增)
-- 四个菜单 component 均复用 fin/config/index，页面按路由 path 激活对应页签并隐藏页签栏
-- 按钮权限(906008/906009/906011)仍挂 906007，权限串全局有效，不受菜单拆分影响
-- 回退：见文末回退语句
-- ============================================================

-- 1. 原「财务配置」改造为「期末结账」（component 不变，path 改为 closing）
UPDATE sys_menu
SET menu_name = '期末结账', path = 'closing', remark = '原财务配置改造：期末结账页签独立菜单'
WHERE menu_id = 906007;

-- 2. 新增「凭证规则」「财务参数」「成本方法」三个独立菜单（is_cache=0，同一组件跨路由不缓存）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906131, '凭证规则', 906100, 4, 'rule',  'fin/config/index', '', '', 1, 0, 'C', '0', '0', 'fin:config:list', 'edit', 'admin', NOW(), '财务配置页签独立菜单（凭证规则）');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906132, '财务参数', 906100, 5, 'param', 'fin/config/index', '', '', 1, 0, 'C', '0', '0', 'fin:config:list', 'tool', 'admin', NOW(), '财务配置页签独立菜单（财务参数）');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906133, '成本方法', 906100, 6, 'method', 'fin/config/index', '', '', 1, 0, 'C', '0', '0', 'fin:config:list', 'chart', 'admin', NOW(), '财务配置页签独立菜单（成本方法）');

-- ============================================================
-- 回退语句（如需撤销拆分）
-- DELETE FROM sys_menu WHERE menu_id IN (906131, 906132, 906133);
-- UPDATE sys_menu SET menu_name='财务配置', path='config', remark='' WHERE menu_id=906007;
-- ============================================================

-- ============================================================
-- 后续迁移记录（2026-10-11，菜单归属调整，DB 已直接生效）
--   906007 期末结账：基础设置(906100) → 日常核算(906101, order_num=0)
--   906142 物料税率维护：日常核算(906101) → 基础设置(906100, order_num=7)
--   依据：期末结账为按期间执行的业务操作，归日常核算；凭证规则/财务参数/
--   成本方法/物料税率维护为基础数据维护，归基础设置。
--   回退：UPDATE sys_menu SET parent_id=906100 WHERE menu_id=906007;
--         UPDATE sys_menu SET parent_id=906101, order_num=6 WHERE menu_id=906142;
-- ============================================================
