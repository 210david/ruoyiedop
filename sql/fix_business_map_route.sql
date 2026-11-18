-- ---------------------------------------------------------------------------
-- 修复「业务全景图」菜单点击 404
--
-- 原因：
--   sys_menu 中业务全景图原为「一级 C 类型菜单」（parent_id = 0）。
--   RuoYi 后端 buildMenus 对一级 C 菜单会包一层 path = "/" 的 Layout 父路由，
--   而系统里已有另一个一级 C 菜单（消息）同样生成 path = "/" 的父路由。
--   前端 addRoute 后存在两条相同 path 的顶级记录，Vue Router 匹配时后插入的
--   「消息」记录优先，其 children 里没有 /business-map，最终落到 catch-all → 404。
--
-- 修复：
--   改为 RuoYi 标准结构「M 目录（业务全景图） + C 子菜单（index）」，
--   父路由 path 唯一（/business-map），不再产生 path = "/" 的重复记录。
--   侧边栏只有一个可见子项时会自动折叠为单条菜单，点击直达 /business-map/index。
--
-- 执行：mysql -uroot -p ry-vue < sql/fix_business_map_route.sql （幂等，可重复执行）
-- ---------------------------------------------------------------------------

-- 1. 原一级 C 菜单 → M 目录（component 用 Layout，perms 留给子菜单）
--    注意：RuoYi 对「一级 M 目录」会自动补前导 '/'，所以 path 必须写 business-map（不能带 '/'），
--    否则 getRouterPath 会拼成 '//business-map'，前端路由再次 404。
UPDATE sys_menu
   SET menu_type = 'M',
       component = 'Layout',
       perms     = '',
       path      = 'business-map',
       parent_id = 0,
       visible   = '0',
       status    = '0',
       update_by = 'admin',
       update_time = NOW()
 WHERE menu_type IN ('C', 'M') AND (path = '/business-map' OR path = 'business-map') AND parent_id = 0;

-- 2. 补齐子菜单（指向真实页面组件）
SET @mapMenuId = (SELECT menu_id FROM sys_menu WHERE path = 'business-map' AND menu_type = 'M' LIMIT 1);

--    父目录 route_name 置空：避免父子路由 name 同名（vue-router 会告警且 keep-alive 失效）
UPDATE sys_menu SET route_name = '' WHERE menu_id = @mapMenuId;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon,
                      create_by, create_time, remark)
SELECT '业务全景图', @mapMenuId, 1, 'index', 'business-map/index', '', 'BusinessMap',
       1, 0, 'C', '0', '0', 'business:map:view', 'guide',
       'admin', sysdate(), '新手引导：业务模块全景与数据流向'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE parent_id = @mapMenuId AND path = 'index'
);

-- 3. 子菜单授权（沿用父菜单已有的角色范围）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT rm.role_id, c.menu_id
FROM sys_role_menu rm
JOIN sys_menu c ON c.parent_id = rm.menu_id AND c.path = 'index' AND c.menu_type = 'C'
WHERE rm.menu_id = @mapMenuId
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = c.menu_id
  );

-- 4. 新手任务里的入口路径同步为子菜单路径（原 /business-map 变成目录，直接 push 会白屏）
UPDATE sys_role_onboard_task
   SET menu_path = '/business-map/index',
       update_time = NOW()
 WHERE menu_path = '/business-map';

-- 5. 校验：应看到 1 条 M（path=business-map）+ 1 条 C（path=index）
SELECT m.menu_id, m.menu_name, m.parent_id, m.path, m.component, m.menu_type, m.perms,
       (SELECT COUNT(1) FROM sys_role_menu rm WHERE rm.menu_id = m.menu_id) AS granted_roles
  FROM sys_menu m
 WHERE m.path = 'business-map' OR m.parent_id = @mapMenuId
 ORDER BY m.parent_id, m.order_num;
