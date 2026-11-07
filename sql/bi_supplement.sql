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

-- 给 admin 角色分配五域看板权限（幂等）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms IN ('bi:board:sales:view', 'bi:board:purchase:view', 'bi:board:production:view',
                'bi:board:inventory:view', 'bi:board:quality:view')
  AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);
