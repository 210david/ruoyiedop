-- =============================================================================
-- 模块开关补齐脚本：系统集成（integration）
-- 适用：分支仓（无决策支持 bi 模块）首次接入系统集成模块
-- 版本：v1.0  日期：2026-11-07
--
-- 背景：系统集成（顶层菜单 9063000「系统集成」）已上线，需在 sys_module
--       登记后才能被「系统管理 → 模块开关」按模块停用/启用；同时该模块的
--       4 个定时任务（钉钉/企业微信 通讯录 + 考勤同步）在 INSERT 时未写
--       module_key，必须回填，否则模块停用后任务仍会照跑（幽灵任务）。
--
-- 规范：design/unified-development-standards.md 第 1.2 节「步骤 6：模块开关登记」
-- 幂等：模块登记用 NOT EXISTS 防重；任务回填仅改空值，可重复执行
-- 执行：mysql -uroot -p 库名 < module_switch_add_integration.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、登记系统集成模块（幂等）
--     root_menu_id = 9063000（系统集成目录），级联范围含钉钉集成 / 企业微信集成
--     order_num = 28：为后续决策支持（bi，27）预留位置
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '系统集成', 'integration', 9063000, '0', '0', 28, 'admin', sysdate(), '系统集成模块（钉钉/企业微信），级联菜单 9063000 及其子菜单、定时任务 module_key=integration'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'integration');

-- -----------------------------------------------------------------------------
-- 二、定时任务回填 module_key（级联停用/恢复依赖该字段，为空则不会被级联）
--     注意：括号必须包住 OR 分支，否则会因 AND 优先级误改全部 NULL 任务
-- -----------------------------------------------------------------------------
UPDATE `sys_job` SET `module_key` = 'integration'
 WHERE (`module_key` IS NULL OR `module_key` = '')
   AND (   `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
        OR `invoke_target` LIKE 'wecomAttendanceSyncTask%');

-- -----------------------------------------------------------------------------
-- 三、角色回填 module_key
--     说明：本模块未设专属角色（菜单权限复用现有角色 + sys_role_menu 授权），
--           无角色级联对象，故默认不处理。后续若新增 integration_* 角色，
--           可解除下方注释执行回填。
-- -----------------------------------------------------------------------------
-- UPDATE `sys_role` SET `module_key` = 'integration' WHERE `role_key` LIKE 'integration\_%' AND (`module_key` IS NULL OR `module_key` = '');

-- -----------------------------------------------------------------------------
-- 四、执行结果复核
-- -----------------------------------------------------------------------------
-- 4.1 模块清单（应能看到 integration，job_count = 4）
SELECT m.module_id, m.module_name, m.module_key, m.root_menu_id, m.status, m.is_core, m.order_num,
       (SELECT COUNT(*) FROM `sys_role` r WHERE r.module_key = m.module_key AND r.del_flag = '0') AS role_count,
       (SELECT COUNT(*) FROM `sys_job` j WHERE j.module_key = m.module_key) AS job_count
  FROM `sys_module` m
 WHERE m.module_key = 'integration';

-- 4.2 级联菜单范围（含目录、页面与按钮级权限）
WITH RECURSIVE sub_menu AS (
  SELECT menu_id FROM `sys_menu` WHERE menu_id = 9063000
  UNION ALL
  SELECT cm.menu_id FROM `sys_menu` cm INNER JOIN sub_menu sm ON cm.parent_id = sm.menu_id
)
SELECT '系统集成(integration)' AS module_name, COUNT(*) AS cascade_menu_count FROM sub_menu;

-- 4.3 定时任务归属复核（应无 NULL）
SELECT job_id, job_name, invoke_target, module_key, status FROM `sys_job`
 WHERE invoke_target LIKE 'dingtalkAttendanceSyncTask%'
    OR invoke_target LIKE 'wecomAttendanceSyncTask%'
 ORDER BY job_id;

-- =============================================================================
-- 回滚脚本（仅注释备用，需要时手工解除注释执行）
-- 注意：回滚后该模块将从模块开关页面消失，其定时任务不再受模块级停用控制
-- =============================================================================
-- DELETE FROM `sys_module` WHERE `module_key` = 'integration';
-- UPDATE `sys_job` SET `module_key` = NULL
--  WHERE `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
--     OR `invoke_target` LIKE 'wecomAttendanceSyncTask%';
