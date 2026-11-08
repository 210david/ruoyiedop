-- =============================================
-- 决策支持菜单重组（看板 + 统计分析 二级菜单归集）
-- 内容：
--   1) 决策支持下新建「看板」「统计分析」两个二级目录（M）
--   2) 九域看板（9 个）从决策支持平铺挪入「看板」；各模块看板（11 个）挪入「看板」
--   3) 统计分析下按模块名称分一级目录，各模块统计报表归入对应模块目录
--   4) 各模块原报表目录挪空后隐藏（visible='1'），不删除
-- 原则：挪动只改 parent_id/order_num/path（防同父路由冲突），不改 menu_id/perms/component，
--       sys_role_menu 角色权限关联不受影响
-- 幂等：先查后插/先查后更，可重复执行
-- =============================================

-- =============================================
-- 一、新建「看板」「统计分析」二级目录
-- =============================================
SET @biId = (SELECT menu_id FROM sys_menu WHERE menu_name = '决策支持' AND menu_type = 'M' AND parent_id = 0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '看板', @biId, 3, 'board', NULL, '',
    1, 0, 'M', '0', '0', '', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '看板' AND parent_id = @biId AND menu_type = 'M');
SET @boardId = (SELECT menu_id FROM sys_menu WHERE menu_name = '看板' AND parent_id = @biId AND menu_type = 'M' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '统计分析', @biId, 4, 'statistics', NULL, '',
    1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '统计分析' AND parent_id = @biId AND menu_type = 'M');
SET @statsId = (SELECT menu_id FROM sys_menu WHERE menu_name = '统计分析' AND parent_id = @biId AND menu_type = 'M' LIMIT 1);

-- =============================================
-- 二、统计分析下按模块名称分一级目录（10 个，名称/图标与一级模块一致）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓储管理', @statsId, 1, 'wms', NULL, '', 1, 0, 'M', '0', '0', '', 'scan', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '仓储管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '设备管理', @statsId, 2, 'dms', NULL, '', 1, 0, 'M', '0', '0', '', 'server', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '设备管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '营销管理', @statsId, 3, 'mk', NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '营销管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '安全生产管理', @statsId, 4, 'safety', NULL, '', 1, 0, 'M', '0', '0', '', 'shield', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '质量管理', @statsId, 5, 'qms', NULL, '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '生产管控', @statsId, 6, 'mms', NULL, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '生产管控' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '产品设计', @statsId, 7, 'pdm', NULL, '', 1, 0, 'M', '0', '0', '', 'component', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '产品设计' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '人力资源管理', @statsId, 8, 'hr', NULL, '', 1, 0, 'M', '0', '0', '', 'people', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '人力资源管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'AI中心', @statsId, 9, 'ai', NULL, '', 1, 0, 'M', '0', '0', '', 'skill', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'AI中心' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '财务管理', @statsId, 10, 'fin', NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '财务管理' AND parent_id = @statsId AND menu_type = 'M');

SET @stWms = (SELECT menu_id FROM sys_menu WHERE menu_name='仓储管理' AND parent_id=@statsId LIMIT 1);
SET @stDms = (SELECT menu_id FROM sys_menu WHERE menu_name='设备管理' AND parent_id=@statsId LIMIT 1);
SET @stMk  = (SELECT menu_id FROM sys_menu WHERE menu_name='营销管理' AND parent_id=@statsId LIMIT 1);
SET @stSafety = (SELECT menu_id FROM sys_menu WHERE menu_name='安全生产管理' AND parent_id=@statsId LIMIT 1);
SET @stQms = (SELECT menu_id FROM sys_menu WHERE menu_name='质量管理' AND parent_id=@statsId LIMIT 1);
SET @stMms = (SELECT menu_id FROM sys_menu WHERE menu_name='生产管控' AND parent_id=@statsId LIMIT 1);
SET @stPdm = (SELECT menu_id FROM sys_menu WHERE menu_name='产品设计' AND parent_id=@statsId LIMIT 1);
SET @stHr  = (SELECT menu_id FROM sys_menu WHERE menu_name='人力资源管理' AND parent_id=@statsId LIMIT 1);
SET @stAi  = (SELECT menu_id FROM sys_menu WHERE menu_name='AI中心' AND parent_id=@statsId LIMIT 1);
SET @stFin = (SELECT menu_id FROM sys_menu WHERE menu_name='财务管理' AND parent_id=@statsId LIMIT 1);

-- =============================================
-- 三、九域看板（9 个）从决策支持平铺挪入「看板」（order 保持 3-11）
-- =============================================
UPDATE sys_menu SET parent_id = @boardId
WHERE @boardId IS NOT NULL AND parent_id = @biId
  AND menu_id IN (9063013,9063014,9063015,9063016,9063017,9063021,9063022,9063023,9063024);

-- =============================================
-- 四、各模块看板挪入「看板」（order 20-30；path 冲突的加模块前缀防同父路由重复）
-- =============================================
UPDATE sys_menu SET parent_id=@boardId, order_num=20 WHERE menu_id=3090  AND @boardId IS NOT NULL AND parent_id=3006;   -- 数据看板(设备)   path=overview 唯一
UPDATE sys_menu SET parent_id=@boardId, order_num=21 WHERE menu_id=3091  AND @boardId IS NOT NULL AND parent_id=3006;   -- 设备大屏         path=screen 唯一
UPDATE sys_menu SET parent_id=@boardId, order_num=22, path='mk-dashboard'     WHERE menu_id=4120   AND @boardId IS NOT NULL AND parent_id=4006;   -- 营销看板
UPDATE sys_menu SET parent_id=@boardId, order_num=23 WHERE menu_id=4583  AND @boardId IS NOT NULL AND parent_id=4006;   -- 推广看板         path=promotion 唯一
UPDATE sys_menu SET parent_id=@boardId, order_num=24, path='bid-dashboard'    WHERE menu_id=905894 AND @boardId IS NOT NULL AND parent_id=905886; -- 投标看板
UPDATE sys_menu SET parent_id=@boardId, order_num=25, path='pms-dashboard'    WHERE menu_id=4461   AND @boardId IS NOT NULL AND parent_id=4460;   -- 采购看板
UPDATE sys_menu SET parent_id=@boardId, order_num=26, path='qms-dashboard'    WHERE menu_id=4544   AND @boardId IS NOT NULL AND parent_id=4813;   -- 质量看板
UPDATE sys_menu SET parent_id=@boardId, order_num=27, path='mms-dashboard'    WHERE menu_id=5001   AND @boardId IS NOT NULL AND parent_id=5000;   -- 生产看板
UPDATE sys_menu SET parent_id=@boardId, order_num=28, path='safety-dashboard' WHERE menu_id=4726   AND @boardId IS NOT NULL AND parent_id=4638;   -- 安全驾驶舱
UPDATE sys_menu SET parent_id=@boardId, order_num=29 WHERE menu_id=4070  AND @boardId IS NOT NULL AND parent_id=4003;   -- 商机看板         path=kanban 唯一
UPDATE sys_menu SET parent_id=@boardId, order_num=30, path='qms-trace-dashboard' WHERE menu_id=4844 AND @boardId IS NOT NULL AND parent_id=4833; -- 谱系完整度

-- =============================================
-- 五、各模块统计报表挪入「统计分析」对应模块目录（子菜单 path 不冲突，保持原 path）
-- =============================================
UPDATE sys_menu SET parent_id=@stWms, order_num=1 WHERE menu_id=2110    AND @stWms IS NOT NULL AND parent_id=2000;     -- 仓储统计分析(仓储)
UPDATE sys_menu SET parent_id=@stDms, order_num=1 WHERE menu_id=3108    AND @stDms IS NOT NULL AND parent_id=3006;     -- 库存流水报表(设备)
UPDATE sys_menu SET parent_id=@stMk,  order_num=1 WHERE menu_id=4156    AND @stMk  IS NOT NULL AND parent_id=4006;     -- 销售人员分析(营销)
UPDATE sys_menu SET parent_id=@stMk,  order_num=2 WHERE menu_id=4215    AND @stMk  IS NOT NULL AND parent_id=4006;     -- 经销商专项报表(营销)
UPDATE sys_menu SET parent_id=@stSafety, order_num=1 WHERE menu_id=4870 AND @stSafety IS NOT NULL AND parent_id=4691;  -- 学时统计(安全)
UPDATE sys_menu SET parent_id=@stQms, order_num=1 WHERE menu_id=4851    AND @stQms IS NOT NULL AND parent_id=4813;     -- 数据报表(质量)
UPDATE sys_menu SET parent_id=@stMms, order_num=1 WHERE menu_id=5215    AND @stMms IS NOT NULL AND parent_id=5235;     -- OEE分析(生产)
UPDATE sys_menu SET parent_id=@stMms, order_num=2 WHERE menu_id=5216    AND @stMms IS NOT NULL AND parent_id=5235;     -- 生产日报(生产)
UPDATE sys_menu SET parent_id=@stMms, order_num=3 WHERE menu_id=5345    AND @stMms IS NOT NULL AND parent_id=5235;     -- 工单完工分析(生产)
UPDATE sys_menu SET parent_id=@stPdm, order_num=1 WHERE menu_id=11440   AND @stPdm IS NOT NULL AND parent_id=11000;    -- 统计分析(产品设计)
UPDATE sys_menu SET parent_id=@stHr,  order_num=1 WHERE menu_id=906148  AND @stHr  IS NOT NULL AND parent_id=905538;   -- 统计报表(人力)
UPDATE sys_menu SET parent_id=@stAi,  order_num=1 WHERE menu_id=905791  AND @stAi  IS NOT NULL AND parent_id=905788;   -- 用量统计(AI)
-- 财务报表目录(905998)下 5 张报表直接入「统计分析>财务管理」，目录挪空后隐藏
UPDATE sys_menu SET parent_id=@stFin, order_num=1 WHERE menu_id=906110 AND @stFin IS NOT NULL AND parent_id=905998;    -- 资产负债表
UPDATE sys_menu SET parent_id=@stFin, order_num=2 WHERE menu_id=906111 AND @stFin IS NOT NULL AND parent_id=905998;    -- 利润表
UPDATE sys_menu SET parent_id=@stFin, order_num=3 WHERE menu_id=906112 AND @stFin IS NOT NULL AND parent_id=905998;    -- 现金流量表
UPDATE sys_menu SET parent_id=@stFin, order_num=4 WHERE menu_id=906113 AND @stFin IS NOT NULL AND parent_id=905998;    -- 往来对账
UPDATE sys_menu SET parent_id=@stFin, order_num=5 WHERE menu_id=906114 AND @stFin IS NOT NULL AND parent_id=905998;    -- 账龄分析

-- =============================================
-- 六、各模块原报表目录挪空后隐藏（visible='1'，不删除，保留角色关联）
-- 说明：完工分析(5235)下仍保留「批次追溯」，不隐藏
-- =============================================
UPDATE sys_menu SET visible='1' WHERE menu_id=4006 AND NOT EXISTS
  (SELECT 1 FROM (SELECT 1 FROM sys_menu WHERE parent_id=4006 AND visible='0' AND status='0') t);
UPDATE sys_menu SET visible='1' WHERE menu_id=4460 AND NOT EXISTS
  (SELECT 1 FROM (SELECT 1 FROM sys_menu WHERE parent_id=4460 AND visible='0' AND status='0') t);
UPDATE sys_menu SET visible='1' WHERE menu_id=4813 AND NOT EXISTS
  (SELECT 1 FROM (SELECT 1 FROM sys_menu WHERE parent_id=4813 AND visible='0' AND status='0') t);
UPDATE sys_menu SET visible='1' WHERE menu_id=3006 AND NOT EXISTS
  (SELECT 1 FROM (SELECT 1 FROM sys_menu WHERE parent_id=3006 AND visible='0' AND status='0') t);
-- 财务报表(905998)目录下剩余为 F 按钮权限（不构成菜单显示），直接隐藏
UPDATE sys_menu SET visible='1' WHERE menu_id=905998;

-- =============================================
-- 七、admin 角色授权（幂等；普通角色请在 系统管理-角色管理 自行勾选）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.parent_id IN (@biId, @statsId) AND m.menu_type='M'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=m.menu_id);
