-- ============================================================
-- 工艺路线管理升级 - P0/P1/P2 全量增强
-- 1. 主表增加字段（生效日期、审核信息、默认车间/产线等）
-- 2. 工序明细表增加字段（准备时间、设备ID、模具、SOP、参数模板等）
-- 3. 新增工艺路线版本变更记录表（P2）
-- 4. 更新状态字典为 草稿/已启用/已审核/已停用
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. mms_route 主表增加字段
-- ============================================================
-- 检查并添加 effective_date
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `effective_date` DATE DEFAULT NULL COMMENT ''生效日期'' AFTER `version`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'effective_date');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 expire_date
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `expire_date` DATE DEFAULT NULL COMMENT ''失效日期'' AFTER `effective_date`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'expire_date');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 audit_by
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `audit_by` VARCHAR(64) DEFAULT '''' COMMENT ''审核人'' AFTER `status`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'audit_by');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 audit_time
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT ''审核时间'' AFTER `audit_by`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'audit_time');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 default_workshop
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `default_workshop` VARCHAR(100) DEFAULT '''' COMMENT ''默认车间'' AFTER `audit_time`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'default_workshop');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 default_line
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `default_line` VARCHAR(100) DEFAULT '''' COMMENT ''默认产线'' AFTER `default_workshop`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'default_line');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 total_std_time
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `total_std_time` DECIMAL(10,2) DEFAULT 0.00 COMMENT ''总标准工时(自动汇总)'' AFTER `default_line`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'total_std_time');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 process_count
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `process_count` INT(11) DEFAULT 0 COMMENT ''工序数量'' AFTER `total_std_time`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'process_count');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 状态改为：0=草稿 1=已启用 2=已审核 3=已停用（兼容旧数据）
-- 旧 status='0' 保持为草稿，旧 status='1' 改为 '3'(停用)
UPDATE `mms_route` SET `status` = '3' WHERE `status` = '1';

-- ============================================================
-- 2. mms_route_process 工序明细表增加字段（P1: 资源绑定/工艺参数/作业指导）
-- ============================================================
-- prep_time
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `prep_time` DECIMAL(10,2) DEFAULT 0.00 COMMENT ''准备时间(小时)'' AFTER `std_time`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'prep_time');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- resource_id
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `resource_id` BIGINT(20) DEFAULT NULL COMMENT ''绑定产能单元ID'' AFTER `is_outsource`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'resource_id');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- resource_name
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `resource_name` VARCHAR(100) DEFAULT '''' COMMENT ''产能单元名称'' AFTER `resource_id`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'resource_name');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- equip_id
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `equip_id` BIGINT(20) DEFAULT NULL COMMENT ''绑定设备ID'' AFTER `resource_name`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'equip_id');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- equip_name
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `equip_name` VARCHAR(100) DEFAULT '''' COMMENT ''设备名称'' AFTER `equip_id`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'equip_name');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- mold_id
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `mold_id` BIGINT(20) DEFAULT NULL COMMENT ''模具/工装ID'' AFTER `equip_name`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'mold_id');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- mold_name
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `mold_name` VARCHAR(100) DEFAULT '''' COMMENT ''模具/工装名称'' AFTER `mold_id`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'mold_name');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- sop_id
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `sop_id` BIGINT(20) DEFAULT NULL COMMENT ''作业指导书ID'' AFTER `mold_name`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'sop_id');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- sop_no
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `sop_no` VARCHAR(50) DEFAULT '''' COMMENT ''作业指导书编号'' AFTER `sop_id`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'sop_no');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- sop_name
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `sop_name` VARCHAR(200) DEFAULT '''' COMMENT ''作业指导书名称'' AFTER `sop_no`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'sop_name');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- param_template
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `param_template` TEXT DEFAULT NULL COMMENT ''工艺参数模板JSON'' AFTER `sop_name`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'param_template');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- remark (for mms_route_process, only if not exists)
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT ''备注'' AFTER `param_template`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'remark');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- process_type
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `process_type` VARCHAR(20) DEFAULT '''' COMMENT ''工序类型'' AFTER `process_name`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'process_type');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- workshop
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `workshop` VARCHAR(100) DEFAULT '''' COMMENT ''车间'' AFTER `process_type`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'workshop');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- line
SET @s = CONCAT('ALTER TABLE `mms_route_process` ADD COLUMN `line` VARCHAR(100) DEFAULT '''' COMMENT ''产线'' AFTER `workshop`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route_process' AND COLUMN_NAME = 'line');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. 新增工艺路线版本变更记录表（P2）
-- ============================================================
DROP TABLE IF EXISTS `mms_route_version_log`;
CREATE TABLE `mms_route_version_log` (
  `log_id`         BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `route_id`       BIGINT(20)    NOT NULL                 COMMENT '工艺路线ID',
  `route_no`       VARCHAR(50)   DEFAULT ''               COMMENT '路线编号',
  `old_version`    VARCHAR(10)   DEFAULT ''               COMMENT '原版本号',
  `new_version`    VARCHAR(10)   DEFAULT ''               COMMENT '新版本号',
  `old_status`    VARCHAR(10)   DEFAULT ''               COMMENT '原状态',
  `new_status`    VARCHAR(10)   DEFAULT ''               COMMENT '新状态',
  `change_type`    VARCHAR(20)   DEFAULT ''               COMMENT '变更类型(create/update/enable/audit/disable/copy)',
  `change_desc`    VARCHAR(500)  DEFAULT ''               COMMENT '变更描述',
  `change_by`      VARCHAR(64)   DEFAULT ''               COMMENT '变更人',
  `change_time`    DATETIME      DEFAULT NULL             COMMENT '变更时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_route_id` (`route_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='工艺路线版本变更记录表';

-- ============================================================
-- 4. 更新字典：工艺路线状态 草稿/已启用/已审核/已停用
-- ============================================================
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'mms_route_status';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'mms_route_status';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('工艺路线状态', 'mms_route_status', '0', 'admin', sysdate(), '工艺路线状态：草稿/待审核/已审核/已停用');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '草稿',   '0', 'mms_route_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '待审核', '1', 'mms_route_status', '', 'primary', 'N', '0', 'admin', sysdate(), '待审核'),
(3, '已审核', '2', 'mms_route_status', '', 'success', 'N', '0', 'admin', sysdate(), '已审核'),
(4, '已停用', '3', 'mms_route_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已停用');

-- ============================================================
-- 5. 新增字典：工艺路线变更类型
-- ============================================================
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '路线变更类型', 'mms_route_change_type', '0', 'admin', sysdate(), '工艺路线变更类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'mms_route_change_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '创建',   'create',  'mms_route_change_type', '', 'primary', 'N', '0', 'admin', sysdate(), '创建路线'),
(2, '修改',   'update',  'mms_route_change_type', '', 'warning', 'N', '0', 'admin', sysdate(), '修改路线'),
(3, '启用',   'enable',  'mms_route_change_type', '', 'success', 'N', '0', 'admin', sysdate(), '启用路线'),
(4, '审核',   'audit',   'mms_route_change_type', '', 'success', 'N', '0', 'admin', sysdate(), '审核通过'),
(5, '停用',   'disable', 'mms_route_change_type', '', 'danger',  'N', '0', 'admin', sysdate(), '停用路线'),
(6, '复制',   'copy',    'mms_route_change_type', '', 'info',    'N', '0', 'admin', sysdate(), '复制为新版本')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- ============================================================
-- 6. 补充菜单按钮权限
-- ============================================================
-- 工艺路线状态操作按钮权限（启用/审核/停用/复制）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工艺路线状态操作', 5020, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:route:status', '#', 'admin', sysdate(), '工艺路线状态流转操作'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'mms:route:status' AND `parent_id` = 5020);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工艺路线复制', 5020, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:route:copy', '#', 'admin', sysdate(), '工艺路线复制新版本'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'mms:route:copy' AND `parent_id` = 5020);
