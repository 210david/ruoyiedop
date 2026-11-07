-- =============================================
-- 决策支持模块增量补充（bi_supplement.sql）
-- 内容：F3 五域主题看板菜单（销售/采购/生产/库存/质量，5 个 C 菜单共用 bi/board/index 组件）
-- 前置：需先执行 bi_all.sql（决策支持目录已存在）；可重复执行（先查后插）
-- =============================================

-- 定位「决策支持」一级目录（增量执行不用 LAST_INSERT_ID，避免依赖 bi_all.sql 执行时机）
SET @biId = (SELECT menu_id FROM sys_menu WHERE menu_name = '决策支持' AND menu_type = 'M' LIMIT 1);

-- F3 五域主题看板（C=菜单，5 个菜单共用组件 bi/board/index，按 path 尾段区分域）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '销售看板', @biId, 3, 'board-sales', 'bi/board/index', 'BiBoardSales',
    1, 0, 'C', '0', '0', 'bi:board:sales:view', 'money', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:sales:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '采购看板', @biId, 4, 'board-purchase', 'bi/board/index', 'BiBoardPurchase',
    1, 0, 'C', '0', '0', 'bi:board:purchase:view', 'shopping', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:purchase:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '生产看板', @biId, 5, 'board-production', 'bi/board/index', 'BiBoardProduction',
    1, 0, 'C', '0', '0', 'bi:board:production:view', 'build', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:production:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '库存看板', @biId, 6, 'board-inventory', 'bi/board/index', 'BiBoardInventory',
    1, 0, 'C', '0', '0', 'bi:board:inventory:view', 'dict', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:inventory:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '质量看板', @biId, 7, 'board-quality', 'bi/board/index', 'BiBoardQuality',
    1, 0, 'C', '0', '0', 'bi:board:quality:view', 'validCode', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:quality:view');

-- P1 增补（使用反馈）：安全/设备/人力/投标 四域独立看板（共用组件，权限动态校验 bi:board:{domain}:view）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '安全看板', @biId, 8, 'board-safety', 'bi/board/index', 'BiBoardSafety',
    1, 0, 'C', '0', '0', 'bi:board:safety:view', 'warning', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:safety:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '设备看板', @biId, 9, 'board-equipment', 'bi/board/index', 'BiBoardEquipment',
    1, 0, 'C', '0', '0', 'bi:board:equipment:view', 'server', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:equipment:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '人力看板', @biId, 10, 'board-hr', 'bi/board/index', 'BiBoardHr',
    1, 0, 'C', '0', '0', 'bi:board:hr:view', 'education', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:hr:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '投标看板', @biId, 11, 'board-bid', 'bi/board/index', 'BiBoardBid',
    1, 0, 'C', '0', '0', 'bi:board:bid:view', 'documentation', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:bid:view');

-- 给 admin 角色分配九域看板权限（幂等）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms IN ('bi:board:sales:view', 'bi:board:purchase:view', 'bi:board:production:view',
                'bi:board:inventory:view', 'bi:board:quality:view',
                'bi:board:safety:view', 'bi:board:equipment:view', 'bi:board:hr:view', 'bi:board:bid:view')
  AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);
