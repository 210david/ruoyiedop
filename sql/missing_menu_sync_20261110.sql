-- ============================================================
-- missing_menu_sync_20261110.sql
-- 目的：按标准仓库(ry-vue)同步分支库(ruoyi_yhny)缺失的 8 个菜单 + 角色授权
-- 说明：1) 紧急程度管理(9062229) 不同步——分支库前后端代码均缺失，先补代码再挂菜单
--       2) 分支库 9063048/9063052/9063053/9063061 已被占用，统一新分配 9063100-9063107 段
-- 幂等：INSERT ... WHERE NOT EXISTS 防重，可重复执行
-- 回退：见文件末尾注释
-- ============================================================
USE ruoyi_yhny;

-- ---------- 1. 菜单本体（ID 映射：标准 -> 分支） ----------
-- 9063048 质量数据报表   -> 9063100 (父 4813 报表中心)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063100, '质量数据报表', 4813, 1, 'list', 'qms/report/index', '', '', 1, 0, 'C', '0', '0', 'qms:report:list', 'documentation', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063100);

-- 9063052 产品设计驾驶舱 -> 9063101 (父 11000 产品设计)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063101, '产品设计驾驶舱', 11000, 99, 'statistics', 'pdm/statistics/index', '', '', 1, 0, 'C', '0', '0', 'pdm:statistics:view', 'chart', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063101);

-- 9063053 人力资源统计报表 -> 9063102 (父 905538 人力资源管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063102, '人力资源统计报表', 905538, 8, 'statistics', 'hr/statistics/index', '', '', 1, 0, 'C', '0', '0', 'hr:statistics:list', 'chart', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063102);

-- 9063061 设备大屏       -> 9063103 (父 3006 统计分析)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063103, '设备大屏', 3006, 2, 'screen', 'dms/equipment/screen', '', '', 1, 0, 'C', '0', '0', 'dms:equipment:query', 'monitor', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063103);

-- 9063064 推广统计分析   -> 9063104 (父 4006 数据分析)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063104, '推广统计分析', 4006, 2, 'promotion', 'mk/promotionReport/index', '', '', 1, 0, 'C', '0', '0', 'marketing:promotionReport:view', 'dashboard', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063104);

-- 9063066 质量管理驾驶舱 -> 9063105 (父 4813 报表中心)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063105, '质量管理驾驶舱', 4813, 2, 'dashboard', 'qms/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'qms:dashboard:view', 'dashboard', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063105);

-- 9063067 生产驾驶舱     -> 9063106 (父 5000 生产管控)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063106, '生产驾驶舱', 5000, 10, 'dashboard', 'mms/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'mms:dashboard:list', 'dashboard', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063106);

-- 9063070 投标数据分析   -> 9063107 (父 905886 投标管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063107, '投标数据分析', 905886, 8, 'dashboard', 'bid/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'bid:dashboard:view', 'chart', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063107);

-- ---------- 2. 角色授权（按标准库同角色/同名角色映射） ----------
-- 映射：9063048->9063100, 9063052->9063101, 9063053->9063102,
--       9063061->9063103, 9063064->9063104, 9063066->9063105,
--       9063067->9063106, 9063070->9063107
-- 超级管理员(role_id=1)与各非管理员角色，角色ID或角色名与标准库一致才授权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT r.role_id, m.bid
FROM sys_role r
JOIN (
    SELECT 9063048 sid, 9063100 bid UNION ALL
    SELECT 9063052 sid, 9063101 bid UNION ALL
    SELECT 9063053 sid, 9063102 bid UNION ALL
    SELECT 9063061 sid, 9063103 bid UNION ALL
    SELECT 9063064 sid, 9063104 bid UNION ALL
    SELECT 9063066 sid, 9063105 bid UNION ALL
    SELECT 9063067 sid, 9063106 bid UNION ALL
    SELECT 9063070 sid, 9063107 bid
) m
JOIN `ry-vue`.sys_role_menu rm ON rm.menu_id = m.sid
JOIN `ry-vue`.sys_role r3 ON r3.role_id = rm.role_id
WHERE (r.role_id = r3.role_id OR r.role_name = r3.role_name)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = r.role_id AND x.menu_id = m.bid);

-- ---------- 3. 校验 ----------
-- 应返回 8 行（8 个新菜单）
SELECT 'NEW_MENUS' tag, menu_id, menu_name, parent_id FROM sys_menu WHERE menu_id BETWEEN 9063100 AND 9063107;
-- 应无冲突（0 行）
SELECT 'ID_CONFLICT' tag, menu_id, menu_name FROM sys_menu WHERE menu_id BETWEEN 9063100 AND 9063107 AND menu_name NOT IN
 ('质量数据报表','产品设计驾驶舱','人力资源统计报表','设备大屏','推广统计分析','质量管理驾驶舱','生产驾驶舱','投标数据分析');

-- 回退（如需撤销本次同步，执行以下语句）：
-- DELETE FROM sys_role_menu WHERE menu_id BETWEEN 9063100 AND 9063107;
-- DELETE FROM sys_menu WHERE menu_id BETWEEN 9063100 AND 9063107;
