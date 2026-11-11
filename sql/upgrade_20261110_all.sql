-- ============================================================================
-- EDOP-CFSP 增量升级脚本（合并入口）: upgrade_20261110_all.sql
-- 日期: 2026-11-10
-- 适用: 已有数据的生产库（基线 2026-11-09 upgrade_20261109_bi_integration_exam_mcp.sql 已全部执行）
-- 内容: 1. 模块开关补齐——决策支持(bi) + 系统集成(integration) 登记 sys_module
--          （注：20261109 合并脚本只登记了 integration，漏了 bi 模块登记，本脚本补齐，
--           integration 部分幂等重跑无副作用）
--       2. 部门表「负责人」调整为「部门领导」，新增 leader_id(人员ID)
-- 规范: 全部幂等可重复执行；按分节顺序执行
-- 来源: 代码仓 sql/module_switch_add_bi_integration.sql + sql/upgrade_20261110_dept_leader_id.sql
-- ============================================================================
-- >>> 来源文件: module_switch_add_bi_integration.sql
-- =============================================================================
-- 模块开关补齐脚本：决策支持（bi）、系统集成（integration）
-- 背景：决策支持（顶层菜单 9062263「决策支持」）与系统集成（顶层菜单
--       9063000「系统集成」）两个模块已上线，但 sys_module 未登记，导致
--       「系统管理 → 模块开关」页面缺少这两项，无法按模块停用/启用；
--       同时系统集成的 4 个定时任务 module_key 为空，即使登记模块也无法级联。
-- 适用：已执行过 module_switch_init_ryvue.sql 的环境（增量执行，幂等，可重复跑）
-- 执行：mysql -uroot -p 库名 < module_switch_add_bi_integration.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、登记决策支持模块（幂等：已存在则跳过）
--     root_menu_id = 9062263（决策支持目录），级联范围含驾驶舱/指标字典/各业务看板
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '决策支持', 'bi', 9062263, '0', '0', 27, 'admin', sysdate(), '决策支持模块（BI），级联菜单 9062263 及其子菜单、定时任务 module_key=bi'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'bi');

-- -----------------------------------------------------------------------------
-- 二、登记系统集成模块（幂等）
--     root_menu_id = 9063000（系统集成目录），级联范围含钉钉集成 / 企业微信集成
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '系统集成', 'integration', 9063000, '0', '0', 28, 'admin', sysdate(), '系统集成模块（钉钉/企业微信），级联菜单 9063000 及其子菜单、定时任务 module_key=integration'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'integration');

-- -----------------------------------------------------------------------------
-- 三、定时任务回填 module_key（级联停用/恢复依赖该字段，为空则不会被级联）
--     3.1 系统集成：钉钉通讯录/考勤同步、企业微信通讯录/考勤同步
--     3.2 决策支持：BI 指标快照/对账/阈值预警（新建时已写 bi，此处兜底历史数据）
-- -----------------------------------------------------------------------------
UPDATE `sys_job` SET `module_key` = 'integration'
 WHERE (`module_key` IS NULL OR `module_key` = '')
   AND (   `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
        OR `invoke_target` LIKE 'wecomAttendanceSyncTask%');

UPDATE `sys_job` SET `module_key` = 'bi'
 WHERE (`module_key` IS NULL OR `module_key` = '')
   AND `invoke_target` LIKE 'biMetricSnapshotTask%';

-- -----------------------------------------------------------------------------
-- 四、角色回填 module_key
--     说明：这两模块当前未设专属角色（菜单权限复用现有角色 + sys_role_menu 授权），
--           无角色级联对象，故默认不处理。后续若新增 bi_*/integration_* 角色，
--           可解除下方注释执行回填。
-- -----------------------------------------------------------------------------
-- UPDATE `sys_role` SET `module_key` = 'bi'          WHERE `role_key` LIKE 'bi\_%'          AND (`module_key` IS NULL OR `module_key` = '');
-- UPDATE `sys_role` SET `module_key` = 'integration' WHERE `role_key` LIKE 'integration\_%' AND (`module_key` IS NULL OR `module_key` = '');

-- -----------------------------------------------------------------------------
-- 五、执行结果复核
-- -----------------------------------------------------------------------------
-- 5.1 模块清单（应能看到 bi / integration，job_count 分别为 3 / 4）
SELECT m.module_id, m.module_name, m.module_key, m.root_menu_id, m.status, m.is_core, m.order_num,
       (SELECT COUNT(*) FROM `sys_role` r WHERE r.module_key = m.module_key AND r.del_flag = '0') AS role_count,
       (SELECT COUNT(*) FROM `sys_job` j WHERE j.module_key = m.module_key) AS job_count
  FROM `sys_module` m
 ORDER BY m.order_num;

-- 5.2 级联菜单范围（含目录、页面与按钮级权限，实际范围随菜单增减变化）
WITH RECURSIVE sub_menu AS (
  SELECT menu_id FROM `sys_menu` WHERE menu_id = 9062263
  UNION ALL
  SELECT cm.menu_id FROM `sys_menu` cm INNER JOIN sub_menu sm ON cm.parent_id = sm.menu_id
)
SELECT '决策支持(bi)' AS module_name, COUNT(*) AS cascade_menu_count FROM sub_menu;

WITH RECURSIVE sub_menu2 AS (
  SELECT menu_id FROM `sys_menu` WHERE menu_id = 9063000
  UNION ALL
  SELECT cm.menu_id FROM `sys_menu` cm INNER JOIN sub_menu2 sm ON cm.parent_id = sm.menu_id
)
SELECT '系统集成(integration)' AS module_name, COUNT(*) AS cascade_menu_count FROM sub_menu2;

-- 5.3 定时任务归属复核（应无 NULL）
SELECT job_id, job_name, invoke_target, module_key, status FROM `sys_job`
 WHERE invoke_target LIKE 'biMetricSnapshotTask%'
    OR invoke_target LIKE 'dingtalkAttendanceSyncTask%'
    OR invoke_target LIKE 'wecomAttendanceSyncTask%'
 ORDER BY job_id;

-- =============================================================================
-- 回滚脚本（仅注释备用，需要时手工解除注释执行）
-- 注意：回滚后这两个模块将从模块开关页面消失，其定时任务不再受模块级停用控制
-- =============================================================================
-- DELETE FROM `sys_module` WHERE `module_key` IN ('bi', 'integration');
-- UPDATE `sys_job` SET `module_key` = NULL
--  WHERE `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
--     OR `invoke_target` LIKE 'wecomAttendanceSyncTask%';

-- >>> 来源文件: upgrade_20261110_dept_leader_id.sql
-- ============================================================

-- ---------- 1. sys_dept 新增 leader_id ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column leader_id bigint default null comment ''部门领导(人员ID,关联sys_user.user_id)'' after leader',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 2. 按姓名回填 leader_id ----------
update sys_dept d
set d.leader_id = (
    select u.user_id from sys_user u
    where u.nick_name = d.leader and u.del_flag = '0'
    order by u.user_id limit 1
)
where d.leader_id is null
  and d.leader is not null and d.leader <> '';
