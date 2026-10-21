-- =============================================
-- 安全生产管理 - 编号规则种子数据
-- 功能：安全生产管理模块各业务单据自动编码
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- =============================================
-- 0. 确保 mk_number_rule 表结构完整（兼容所有 MySQL 版本）
-- 补充动态前缀相关列，避免 selectRuleForUpdate 查询报错
-- =============================================

-- 0.1 添加 module 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'module');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产)'' AFTER `rule_code`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 0.2 添加 prefix_field 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field` VARCHAR(50) DEFAULT NULL COMMENT ''动态前缀关联字段名'' AFTER `connector`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 0.3 添加 prefix_field_dict_type 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field_dict_type');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_dict_type` VARCHAR(100) DEFAULT NULL COMMENT ''动态前缀关联字典类型'' AFTER `prefix_field`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 0.4 添加 prefix_field_enabled 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field_enabled');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_enabled` CHAR(1) DEFAULT ''0'' COMMENT ''是否启用动态前缀(0=否,1=是)'' AFTER `prefix_field_dict_type`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 0.5 创建前缀映射子表（如不存在）
CREATE TABLE IF NOT EXISTS `mk_number_rule_prefix` (
  `prefix_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `rule_id`       BIGINT(20)    NOT NULL                 COMMENT '规则ID',
  `field_value`   VARCHAR(50)   NOT NULL                 COMMENT '字段值（字典值）',
  `field_label`   VARCHAR(100)  DEFAULT ''               COMMENT '字段标签（字典标签）',
  `prefix`        VARCHAR(20)   DEFAULT ''               COMMENT '对应前缀',
  `enabled`       CHAR(1)       DEFAULT '1'              COMMENT '是否启用（0=否,1=是）',
  `current_seq`   BIGINT(20)    DEFAULT 0                COMMENT '当前序列号（按动态前缀重置时使用）',
  `current_date_str` VARCHAR(20) DEFAULT ''              COMMENT '当前日期串',
  `create_by`     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME                               COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME                               COMMENT '更新时间',
  PRIMARY KEY (`prefix_id`),
  KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编号规则动态前缀映射表';

-- 0.6 如果子表已存在但缺少 current_seq / current_date_str 列，也补上
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule_prefix' AND COLUMN_NAME = 'current_seq');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule_prefix` ADD COLUMN `current_seq` BIGINT(20) DEFAULT 0 COMMENT ''当前序列号'' AFTER `enabled`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule_prefix' AND COLUMN_NAME = 'current_date_str');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule_prefix` ADD COLUMN `current_date_str` VARCHAR(20) DEFAULT '' COMMENT ''当前日期串'' AFTER `current_seq`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 一、初始化安全生产管理编号规则（幂等插入）
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_area', 'safety', '区域编码', 'QY', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '安全生产区域编码，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_area');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_equipment', 'safety', '特种设备编号', 'SB', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '特种设备编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_equipment');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_risk_point', 'safety', '风险点编号', 'FX', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '风险点编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_risk_point');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_inspection_task', 'safety', '巡检任务编号', 'XJ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '巡检任务编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_inspection_task');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_hazard', 'safety', '隐患编号', 'YH', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '隐患编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_hazard');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_drill', 'safety', '演练编号', 'YL', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '应急演练编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_drill');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_incident', 'safety', '事故编号', 'SG', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '事故编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_incident');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_material', 'safety', '危化品编号', 'WH', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '危化品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_material');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_emergency_material', 'safety', '应急物资编码', 'WZ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '应急物资编码，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_emergency_material');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_emergency_plan', 'safety', '预案编号', 'YA', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '应急预案编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_emergency_plan');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_training_plan', 'safety', '培训计划编号', 'PX', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '培训计划编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_training_plan');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_training_course', 'safety', '课程编号', 'KC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '培训课程编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_training_course');

-- 注：特种人员工号为手动输入，无需自动编码规则（safety_worker 已移除）

-- =============================================
-- 二、安全生产管理 - 编码规则菜单（挂在安全生产管理目录下）
-- query 字段使用 JSON 格式（前端 sidebar 通过 JSON.parse 解析）
-- =============================================

-- 查找安全生产管理根菜单ID
SET @safety_parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0 LIMIT 1);

-- 插入编码规则菜单（幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编码规则', @safety_parent_id, 99, 'numberRule', 'mk/numberRule/index', '{"module":"safety"}', 'SafetyNumberRule', 1, 0, 'C', '0', '0', 'safety:numberRule:list', 'number', 'admin', sysdate(), '安全生产编码规则管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @safety_parent_id AND component = 'mk/numberRule/index');

SET @safety_rule_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @safety_parent_id AND component = 'mk/numberRule/index' LIMIT 1);

-- 插入按钮权限（幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则查询', @safety_rule_menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:query', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则新增', @safety_rule_menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:add', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则修改', @safety_rule_menu_id, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:edit', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则删除', @safety_rule_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:remove', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则导出', @safety_rule_menu_id, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'safety:numberRule:export', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:numberRule:export');

-- =============================================
-- 三、给admin角色(1)和普通角色(2)分配编码规则菜单权限（幂等）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'safety:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'safety:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- =============================================
-- 四、验证数据
-- =============================================
SELECT '安全生产编号规则初始化完成' AS result;
SELECT rule_id, rule_code, module, rule_name, prefix, status FROM mk_number_rule WHERE module = 'safety' ORDER BY rule_code;
SELECT menu_id, menu_name, parent_id, query, perms FROM sys_menu WHERE perms LIKE 'safety:numberRule:%' ORDER BY menu_id;
