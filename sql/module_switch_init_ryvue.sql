-- =============================================================================
-- 模块开关功能初始化脚本（标准基础环境 ry-vue）
-- 依据《传丰ERP模块开关功能实施方案 V1.1》，自传丰环境同步
-- 内容：sys_module 建表 + sys_role/sys_job 扩展列 + 数据回填 + 菜单注册
-- 与传丰版差异：模块初始状态与基础环境当前菜单状态一致（业务模块全部启用，仅系统工具已停用）
-- 注意：执行前请先执行文末"回滚脚本"章节中的备份语句
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、新建模块表 sys_module
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_module` (
  `module_id`     bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '模块ID',
  `module_name`   varchar(50)  NOT NULL                COMMENT '模块名称',
  `module_key`    varchar(50)  NOT NULL                COMMENT '模块标识',
  `root_menu_id`  bigint(20)   DEFAULT NULL            COMMENT '顶层菜单ID',
  `status`        char(1)      NOT NULL DEFAULT '0'    COMMENT '模块状态（0启用 1停用）',
  `is_core`       char(1)      NOT NULL DEFAULT '0'    COMMENT '是否核心模块（1=核心，不可关闭 0=普通）',
  `order_num`     int(4)       DEFAULT 0               COMMENT '显示顺序',
  `create_by`     varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL            COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`module_id`),
  UNIQUE KEY `uk_module_key` (`module_key`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统模块开关表';

-- -----------------------------------------------------------------------------
-- 二、角色/任务表扩展列
-- -----------------------------------------------------------------------------
ALTER TABLE `sys_role`
  ADD COLUMN `module_key` varchar(50) DEFAULT NULL COMMENT '所属模块标识（关联sys_module.module_key，NULL为平台级角色）' AFTER `role_category`;

ALTER TABLE `sys_job`
  ADD COLUMN `module_key` varchar(50) DEFAULT NULL COMMENT '所属模块标识（关联sys_module.module_key，NULL为平台级任务）' AFTER `job_group`;

CREATE INDEX `idx_sys_role_module_key` ON `sys_role` (`module_key`);
CREATE INDEX `idx_sys_job_module_key`  ON `sys_job` (`module_key`);

-- -----------------------------------------------------------------------------
-- 三、模块种子数据（15个模块，初始状态与基础环境当前菜单状态一致）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `remark`) VALUES
('系统管理',     'system', 1,      '0', '1', 1,  'admin', '核心基础模块，不可关闭'),
('消息中心',     'msg',    905404, '0', '1', 2,  'admin', '核心基础模块，不可关闭'),
('营销管理',     'mk',     4000,   '0', '0', 10, 'admin', NULL),
('生产管控',     'mms',    5000,   '0', '0', 11, 'admin', NULL),
('质量管理',     'qms',    4727,   '0', '0', 12, 'admin', NULL),
('采购管理',     'pms',    4197,   '0', '0', 13, 'admin', NULL),
('仓储物流',     'wms',    2000,   '0', '0', 14, 'admin', NULL),
('AI中心',       'ai',     905788, '0', '0', 15, 'admin', NULL),
('流程中心',     'flow',   905824, '0', '0', 20, 'admin', NULL),
('设备管理',     'dms',    3000,   '0', '0', 21, 'admin', NULL),
('安全生产管理', 'safety', 4638,   '0', '0', 22, 'admin', NULL),
('人力资源管理', 'hr',     905538, '0', '0', 23, 'admin', NULL),
('财务管理',     'fin',    905938, '0', '0', 24, 'admin', NULL),
('招投标管理',   'bid',    905886, '0', '0', 25, 'admin', NULL),
('系统工具',     'tool',   3,      '1', '0', 26, 'admin', '与当前菜单停用状态一致');

-- -----------------------------------------------------------------------------
-- 四、角色回填 module_key（基础环境角色ID与传丰一致，已核实）
-- -----------------------------------------------------------------------------
UPDATE `sys_role` SET `module_key` = 'mms'    WHERE role_id BETWEEN 600 AND 603;
UPDATE `sys_role` SET `module_key` = 'qms'    WHERE role_id BETWEEN 120 AND 126;
UPDATE `sys_role` SET `module_key` = 'mk'     WHERE role_id BETWEEN 300 AND 304;
UPDATE `sys_role` SET `module_key` = 'wms'    WHERE role_id BETWEEN 400 AND 404;
UPDATE `sys_role` SET `module_key` = 'pms'    WHERE role_id BETWEEN 500 AND 505;
UPDATE `sys_role` SET `module_key` = 'dms'    WHERE role_id BETWEEN 200 AND 203;
UPDATE `sys_role` SET `module_key` = 'safety' WHERE role_id IN (110, 111);
UPDATE `sys_role` SET `module_key` = 'fin'    WHERE role_id IN (101, 610, 611, 612);
-- 平台级角色（1,100,102,103,104,606,608）保持 NULL，无需回填

-- -----------------------------------------------------------------------------
-- 五、定时任务回填 module_key（基础环境任务ID与传丰一致，已核实）
-- -----------------------------------------------------------------------------
UPDATE `sys_job` SET `module_key` = 'dms' WHERE job_id BETWEEN 100 AND 104;
UPDATE `sys_job` SET `module_key` = 'pms' WHERE job_id = 105;
UPDATE `sys_job` SET `module_key` = 'qms' WHERE job_id IN (110, 111);
UPDATE `sys_job` SET `module_key` = 'mk'  WHERE job_id BETWEEN 112 AND 116;
-- 平台级任务（1,2,3）保持 NULL

-- -----------------------------------------------------------------------------
-- 六、菜单注册：系统管理 → 模块开关（menu_id 取 906200+，当前 MAX=906150）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (906200, '模块开关', 1, 17, 'module', 'system/module/index', '', '', 1, 0, 'C', '0', '0', 'system:module:list', 'switch', 'admin', sysdate(), '业务模块启停管理菜单');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (906201, '模块开关修改', 906200, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:module:edit', '#', 'admin', sysdate(), '');

-- 授权超级管理员角色（admin 角色实际全量放行，此处按方案同步登记）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 906200), (1, 906201);

-- =============================================================================
-- 回滚脚本（仅注释备用，需要时手工解除注释执行）
-- =============================================================================
-- DELETE FROM `sys_role_menu` WHERE menu_id IN (906200, 906201);
-- DELETE FROM `sys_menu` WHERE menu_id IN (906200, 906201);
-- UPDATE `sys_role` SET `module_key` = NULL;
-- UPDATE `sys_job` SET `module_key` = NULL;
-- DROP INDEX `idx_sys_role_module_key` ON `sys_role`;
-- DROP INDEX `idx_sys_job_module_key` ON `sys_job`;
-- ALTER TABLE `sys_role` DROP COLUMN `module_key`;
-- ALTER TABLE `sys_job` DROP COLUMN `module_key`;
-- DROP TABLE IF EXISTS `sys_module`;
