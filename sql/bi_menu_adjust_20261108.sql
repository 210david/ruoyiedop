-- =============================================
-- 决策支持菜单调整（2026-11-08）
--  1) 「统计分析」下新增「采购管理」目录，位于「营销管理」之后（order 4，原 4~10 顺延为 5~11）
--  2) 「指标看板分析」最下面的采购看板(4461, pms/dashboard/index) 挪入 统计分析>采购管理，
--     更名「采购统计分析」；path/route_name/component/perms 不变，角色授权(menu_id)不受影响
--  3) 谱系完整度(4844)、商机看板(4070) 从决策支持移除：
--     原按项目规范用 visible='1' 隐藏（软删除），2026-11-08 按用户要求升级为**物理删除**，
--     同时清理 sys_role_menu 授权；独立执行脚本见 bi_menu_purge_hidden_20261108.sql。
--     两者在原模块均有可见 Legacy 入口（9063069 质量追溯 / 9063062 商机管理），功能不受影响。
--  4) 「指标看板分析」下九域看板之一的采购看板(9063014, bi/board/index) 保留不动。
-- 幂等：先查后插 / 按目标值更新 / DELETE 对不存在记录无影响，可重复执行；
--       新目录 menu_id 由自增生成，脚本内不写死；二级目录按 path 定位而非 menu_name，
--       用户在菜单管理里改名后重跑本脚本不会新建重复空目录。
-- 仅调整菜单数据：后端无需重启，刷新页面即生效。
-- =============================================

SET @biId    = (SELECT menu_id FROM sys_menu WHERE menu_name='决策支持' AND menu_type='M' AND parent_id=0 LIMIT 1);
SET @statsId = (SELECT menu_id FROM sys_menu WHERE path='statistics' AND parent_id=@biId AND menu_type='M' LIMIT 1);
SET @boardId = (SELECT menu_id FROM sys_menu WHERE path='board' AND parent_id=@biId AND menu_type='M' LIMIT 1);

-- 一、新建「采购管理」目录（统计分析第 4 位，图标与一级采购管理 4197 一致）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '采购管理', @statsId, 4, 'pms', NULL, '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采购管理' AND parent_id=@statsId AND menu_type='M');

SET @stPms = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=@statsId AND menu_type='M' LIMIT 1);

-- 二、原第 4~10 位目录顺延为 5~11（固定目标值，重复执行结果一致）
UPDATE sys_menu SET order_num = CASE menu_name
      WHEN '安全生产管理' THEN 5
      WHEN '质量管理'     THEN 6
      WHEN '生产管控'     THEN 7
      WHEN '产品设计'     THEN 8
      WHEN '人力资源管理' THEN 9
      WHEN 'AI中心'       THEN 10
      WHEN '财务管理'     THEN 11
    END
WHERE parent_id = @statsId AND menu_type = 'M'
  AND menu_name IN ('安全生产管理','质量管理','生产管控','产品设计','人力资源管理','AI中心','财务管理');

-- 三、采购看板(4461) 挪入 统计分析>采购管理 并更名
UPDATE sys_menu
SET parent_id   = @stPms,
    order_num   = 1,
    menu_name   = '采购统计分析',
    update_by   = 'admin',
    update_time = sysdate()
WHERE menu_id = 4461 AND @stPms IS NOT NULL
  AND (parent_id <> @stPms OR menu_name <> '采购统计分析');

-- 四、从决策支持物理移除两项（含可能存在的子菜单/按钮与角色授权）
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_id IN (4070, 4844) OR parent_id IN (4070, 4844)
  ) t
);
DELETE FROM sys_menu WHERE parent_id IN (4070, 4844);
DELETE FROM sys_menu WHERE menu_id IN (4070, 4844);

-- 五、admin 授权新目录（幂等；普通角色在 系统管理-角色管理 自行勾选）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, @stPms FROM DUAL
WHERE @stPms IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = @stPms);

-- 六、复核
SELECT '统计分析目录' AS scope, menu_id, menu_name, order_num, visible
FROM sys_menu WHERE parent_id = @statsId AND menu_type = 'M' ORDER BY order_num;
SELECT '采购统计分析双入口' AS scope, menu_id, menu_name, parent_id, order_num, visible, component, route_name
FROM sys_menu WHERE component = 'pms/dashboard/index' AND menu_type = 'C' ORDER BY menu_id;
SELECT '看板可见菜单' AS scope, menu_id, menu_name, order_num, visible
FROM sys_menu WHERE parent_id = @boardId AND menu_type = 'C' AND visible = '0' ORDER BY order_num;
SELECT '已物理删除项残留(应为0)' AS scope, COUNT(*) AS cnt
FROM sys_menu WHERE menu_id IN (4070, 4844) OR parent_id IN (4070, 4844);

-- 回滚（如需）：
--   恢复被删除的 4070/4844：mysql -u root -p ry-vue --default-character-set=utf8mb4 < sql/backup_bi_menu_purge_20261108.sql
-- DELETE FROM sys_role_menu WHERE menu_id = @stPms;
-- DELETE FROM sys_menu WHERE menu_id = @stPms;
-- UPDATE sys_menu SET parent_id = @boardId, order_num = 25, menu_name = '采购看板' WHERE menu_id = 4461;
-- 原目录排序恢复：安全生产管理=4 ... 财务管理=10（按 bi_menu_restructure.sql 二节原值）
