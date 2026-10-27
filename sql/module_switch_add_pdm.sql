-- =============================================================================
-- 模块开关补齐脚本：产品设计（pdm）
-- 背景：产品设计模块（ruoyi-pdm，顶层菜单 11000）已上线，但 sys_module 未登记，
--       导致「系统管理 → 模块开关」页面无产品设计开关项，无法按模块停用/启用。
-- 适用：已执行过 module_switch_init 的环境（增量执行，幂等）
-- 执行：mysql -uroot -p 库名 < module_switch_add_pdm.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、登记模块（幂等：已存在则跳过）
-- 说明：root_menu_id 取产品设计顶层目录 11000；is_core='0' 允许停用；
--       status='0' 启用，如需默认停用改为 '1'
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '产品设计', 'pdm', 11000, '0', '0', 16, 'admin', sysdate(), '产品设计模块（PDM），级联菜单 11000 及其子菜单'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'pdm');

-- -----------------------------------------------------------------------------
-- 二、角色回填 module_key（产品设计角色由 pdm_batch10_roles.sql 创建时已带
--     module_key='pdm'；此处兜底历史数据，保证角色统计与级联停用可用）
-- -----------------------------------------------------------------------------
UPDATE `sys_role` SET `module_key` = 'pdm'
 WHERE `role_key` IN ('pdm_manager', 'pdm_engineer', 'pdm_viewer')
   AND (`module_key` IS NULL OR `module_key` = '');

-- -----------------------------------------------------------------------------
-- 三、定时任务回填（产品设计模块当前无定时任务，无需回填）
-- -----------------------------------------------------------------------------

-- -----------------------------------------------------------------------------
-- 四、菜单权限：模块开关菜单已在 module_switch_init 注册，无需重复
-- -----------------------------------------------------------------------------

-- -----------------------------------------------------------------------------
-- 五、执行结果复核
-- -----------------------------------------------------------------------------
SELECT m.module_id, m.module_name, m.module_key, m.root_menu_id, m.status, m.is_core, m.order_num,
       (SELECT COUNT(*) FROM `sys_role` r WHERE r.module_key = m.module_key AND r.del_flag = '0') AS role_count,
       (SELECT COUNT(*) FROM `sys_job` j WHERE j.module_key = m.module_key) AS job_count
  FROM `sys_module` m
 WHERE m.module_key = 'pdm';

-- 复核：菜单树级联范围（应包含 11000 目录及其全部子菜单/按钮）
WITH RECURSIVE sub_menu AS (
  SELECT menu_id, menu_name FROM `sys_menu` WHERE menu_id = 11000
  UNION ALL
  SELECT cm.menu_id, cm.menu_name FROM `sys_menu` cm INNER JOIN sub_menu sm ON cm.parent_id = sm.menu_id
)
SELECT COUNT(*) AS cascade_menu_count FROM sub_menu;

-- =============================================================================
-- 回滚脚本（仅注释备用，需要时手工解除注释执行）
-- =============================================================================
-- DELETE FROM `sys_module` WHERE `module_key` = 'pdm';
