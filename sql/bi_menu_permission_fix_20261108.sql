-- =============================================
-- 决策支持菜单权限收尾修复（2026-11-08）
-- 依据：全库权限核查结论 + 用户 2026-11-08 确认的两项处理
--
-- 1) 「指标看板分析」(9063031) 目录被授予 36 个非 admin 角色，但其下 9 个 BI
--    主题看板仅 admin 授权。RuoYi 的 SidebarItem 在无可见子菜单时会把目录本身
--    渲染成一个点不开的空菜单项，等于侧边栏多出一个空壳。
--    处理：收回这 36 条目录授权；不扩大任何数据可见范围，BI 看板仍仅 admin 可见。
--    注意：请不要把本段反向写回 bi_menu_restructure.sql 第七节——该节原本就只
--          授权 admin（WHERE role_id=1），不会把普通角色授权补回来。
--
-- 2) 「经销商专项报表」(4215，决策支持>统计分析>营销管理) 授权数为 0，
--    决策支持下的入口对所有人不可见；原模块 Legacy 入口 9063046 仅 admin 授权。
--    处理：把 9063046 的角色授权同步给 4215，使两个入口对同一批角色可见。
--
-- 备份：sql/backup_bi_menu_permission_fix_20261108.sql（36 条待删授权原行）
-- 幂等：DELETE 对不存在行无影响；INSERT 按主键 (role_id, menu_id) 去重，可重复执行
-- 范围：仅 sys_role_menu，不动菜单定义；后端无需重启，刷新页面即生效
-- =============================================

-- 一、执行前复核
SELECT '待收回授权(应为36)' AS scope, COUNT(*) AS cnt FROM sys_role_menu
WHERE menu_id = 9063031 AND role_id <> 1;
SELECT '4215/9063046 授权对比' AS scope, menu_id, COUNT(*) AS cnt FROM sys_role_menu
WHERE menu_id IN (4215, 9063046) GROUP BY menu_id ORDER BY menu_id;

-- 二、收回「指标看板分析」目录的非 admin 授权（保留 admin）
DELETE FROM sys_role_menu WHERE menu_id = 9063031 AND role_id <> 1;

-- 三、把 Legacy 入口 9063046 的授权同步给决策支持入口 4215
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT t.role_id, 4215
FROM (SELECT role_id FROM sys_role_menu WHERE menu_id = 9063046) t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu x WHERE x.role_id = t.role_id AND x.menu_id = 4215
);

-- 四、执行后复核
SELECT '9063031 剩余授权(应仅admin 1条)' AS scope, role_id FROM sys_role_menu
WHERE menu_id = 9063031 ORDER BY role_id;
SELECT '4215/9063046 授权对比(应各1条)' AS scope, menu_id, COUNT(*) AS cnt FROM sys_role_menu
WHERE menu_id IN (4215, 9063046) GROUP BY menu_id ORDER BY menu_id;
WITH RECURSIVE t AS (
  SELECT menu_id, menu_type FROM sys_menu WHERE menu_id = 9062263
  UNION ALL
  SELECT m.menu_id, m.menu_type FROM sys_menu m JOIN t ON m.parent_id = t.menu_id
)
SELECT '决策支持子树内零授权C菜单(应为0)' AS scope, COUNT(*) AS cnt
FROM t WHERE t.menu_type = 'C'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.menu_id = t.menu_id);

-- 回滚（如需）：
--   mysql -u root -p ry-vue --default-character-set=utf8mb4 < sql/backup_bi_menu_permission_fix_20261108.sql
--   DELETE FROM sys_role_menu WHERE menu_id = 4215 AND role_id = 1;
-- =============================================
