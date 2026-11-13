-- =============================================
-- 决策支持模块：软删除菜单物理清除 + 重复空壳目录清理（2026-11-08）
-- 背景：
--   1) bi_menu_adjust_20261108.sql 曾把 4070/4844 用 visible='1' 隐藏（软删除），
--      本次按用户要求改为物理删除（含 sys_role_menu 授权）。
--   2) 9063031 已由用户改名为「指标看板分析」，但系统随后又生成一条同 parent、
--      同 path='board' 的空目录 9063090「看板」（无任何子菜单），导致侧边栏仍显示「看板」，
--      属于重复空壳，一并清理。
-- 范围：仅决策支持(9062263)子树内 visible='1' 的 4070/4844 + 空壳 9063090。
--       其他业务模块自有的历史隐藏菜单不动：
--       111 数据监控、2072/2073 仓储、3007/3126 设备AI、4923 质量、
--       5120/5185/5214 生产（均不属于决策支持，且部分为 status='1' 停用功能）。
-- 前置：4070/4844 在原模块均有可见 Legacy 入口（9063062 商机看板 / 9063069 谱系完整度），
--       同 component / perms，物理删除后功能入口不受影响。
-- 备份：执行前已导出 sql/backup_bi_menu_purge_20261108.sql（3 条菜单 + 16 条授权原行），
--       回滚：mysql -u root -p ry-vue --default-character-set=utf8mb4 < backup_bi_menu_purge_20261108.sql
-- 幂等：DELETE 对不存在的记录无影响，可重复执行。
-- 仅动菜单数据，后端无需重启，刷新页面即生效。
-- =============================================

-- 一、执行前复核（应为 3 条菜单 / 16 条授权 / 0 条子菜单）
SELECT '待删菜单' AS scope, menu_id, menu_name, parent_id, menu_type, visible, component, route_name
FROM sys_menu WHERE menu_id IN (4070, 4844, 9063090) ORDER BY menu_id;
SELECT '待删授权' AS scope, menu_id, COUNT(*) AS role_cnt
FROM sys_role_menu WHERE menu_id IN (4070, 4844, 9063090) GROUP BY menu_id ORDER BY menu_id;
SELECT '子菜单检查(应为空)' AS scope, menu_id, menu_name, parent_id
FROM sys_menu WHERE parent_id IN (4070, 4844, 9063090);

-- 二、物理删除：先清角色授权 -> 再清可能存在的子菜单/按钮 -> 最后删主记录
--     （用派生表包装，规避 MySQL "can't specify target table for update in FROM clause"）
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_id IN (4070, 4844, 9063090)
       OR parent_id IN (4070, 4844, 9063090)
  ) t
);
DELETE FROM sys_menu WHERE parent_id IN (4070, 4844, 9063090);
DELETE FROM sys_menu WHERE menu_id IN (4070, 4844, 9063090);

-- 三、执行后复核
SELECT '残留菜单(应为0)' AS scope, COUNT(*) AS cnt
FROM sys_menu WHERE menu_id IN (4070, 4844, 9063090) OR parent_id IN (4070, 4844, 9063090);
SELECT '残留授权(应为0)' AS scope, COUNT(*) AS cnt
FROM sys_role_menu WHERE menu_id IN (4070, 4844, 9063090);
SELECT '决策支持一级目录' AS scope, menu_id, menu_name, path, order_num, visible
FROM sys_menu WHERE parent_id = 9062263 AND menu_type = 'M' ORDER BY order_num, menu_id;
SELECT '同父path冲突(应为空)' AS scope, parent_id, path, COUNT(*) AS cnt
FROM sys_menu WHERE menu_type IN ('M','C') AND status='0'
GROUP BY parent_id, path HAVING COUNT(*) > 1;
SELECT '原模块Legacy入口(应各1条可见)' AS scope, menu_id, menu_name, parent_id, visible, component
FROM sys_menu WHERE component IN ('mk/opportunity/kanban','qms/trace/dashboard') AND menu_type='C' ORDER BY component, menu_id;

-- 回滚（如需）：
--   mysql -u root -p ry-vue --default-character-set=utf8mb4 < sql/backup_bi_menu_purge_20261108.sql
--   注意：回滚后 4070/4844 恢复为 visible='1' 隐藏态，9063090 恢复为可见空目录，
--         如需彻底回到「隐藏」状态，只导入备份中的 4070/4844 两行即可。
