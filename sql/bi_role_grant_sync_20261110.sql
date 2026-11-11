-- ============================================================
-- bi_role_grant_sync_20261110.sql
-- 目的：同步标准仓库(ry-vue)决策支持(BI)菜单的非管理员角色授权到分支库(ruoyi_yhny)
-- 根因：分支库 BI 菜单重排为 9063031-9063062 段后，仅超级管理员(role_id=1)有授权，
--       非管理员角色全部缺失，导致登录后看不到"决策支持"菜单（与标准仓库不一致）
-- 幂等：NOT EXISTS 防重，可重复执行
-- 回退：见文件末尾注释
-- ============================================================
USE ruoyi_yhny;

-- 标准(ry-vue) -> 分支(ruoyi_yhny) BI 菜单 ID 映射（按菜单名核对）
DROP TEMPORARY TABLE IF EXISTS bi_map;
CREATE TEMPORARY TABLE bi_map (sid INT PRIMARY KEY, bid INT, mname VARCHAR(64));
INSERT INTO bi_map VALUES
 (9062263, 9063031, '决策支持'),
 (9063032, 9063050, '统计分析'),
 (9063033, 9063052, '仓储管理'),
 (9063034, 9063053, '设备管理'),
 (9063035, 9063054, '营销管理'),
 (9063036, 9063056, '安全生产管理'),
 (9063037, 9063057, '质量管理'),
 (9063038, 9063058, '生产管控'),
 (9063039, 9063059, '产品设计'),
 (9063040, 9063060, '人力资源管理'),
 (9063041, 9063061, 'AI中心'),
 (9063042, 9063062, '财务管理');

-- 校验映射名一致（应返回 0 行）
SELECT 'MAPPING_MISMATCH' tag, m.sid, m.bid, m.mname
FROM bi_map m
LEFT JOIN `ry-vue`.sys_menu s ON s.menu_id = m.sid AND s.menu_name = m.mname
LEFT JOIN ruoyi_yhny.sys_menu b ON b.menu_id = m.bid AND b.menu_name = m.mname
WHERE s.menu_id IS NULL OR b.menu_id IS NULL;

-- 按"角色ID相同且角色名一致"同步授权（排除超级管理员，其默认可见全部）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, m.bid
FROM `ry-vue`.sys_role_menu rm
JOIN bi_map m ON m.sid = rm.menu_id
JOIN sys_role r ON r.role_id = rm.role_id
JOIN `ry-vue`.sys_role r3 ON r3.role_id = rm.role_id AND r3.role_name = r.role_name
WHERE rm.role_id <> 1
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = m.bid);

DROP TEMPORARY TABLE IF EXISTS bi_map;

-- 回退（如需撤销本次同步，执行以下语句）：
-- DELETE rm FROM sys_role_menu rm
-- JOIN sys_role r ON r.role_id = rm.role_id
-- WHERE rm.role_id <> 1
--   AND rm.menu_id IN (9063031,9063050,9063052,9063053,9063054,9063056,9063057,9063058,9063059,9063060,9063061,9063062)
--   AND NOT EXISTS (SELECT 1 FROM `ry-vue`.sys_role_menu x
--                   WHERE x.role_id = rm.role_id
--                     AND x.menu_id IN (9062263,9063032,9063033,9063034,9063035,9063036,9063037,9063038,9063039,9063040,9063041,9063042));
