-- =============================================
-- 生产管控 - 编号规则种子数据
-- 功能：生产管控模块各业务单据自动编码
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- =============================================
-- 0. 确保 mk_number_rule 表结构完整（兼容所有 MySQL 版本）
-- 补充动态前缀相关列，避免 selectRuleForUpdate 查询报错
-- =============================================

-- 0.1 添加 module 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'module');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产,qms=质量管理,mms=生产管控)'' AFTER `rule_code`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 更新 module 字段注释，包含 mms
SET @comment_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'module' AND COLUMN_COMMENT LIKE '%mms%');
SET @sql2 = IF(@comment_exists = 0, 'ALTER TABLE `mk_number_rule` MODIFY COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产,qms=质量管理,mms=生产管控)''', 'SELECT 1');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

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
-- 0.7 旧数据迁移：将旧版脚本插入的 mms 编号规则记录的 module 字段更新为 'mms'
-- 旧版脚本（mms_production_control_part3.sql）插入时没有 module 字段，需要补充
-- =============================================
UPDATE `mk_number_rule` SET `module` = 'mms' WHERE `module` IS NULL OR `module` = '' OR `module` = 'mk'
AND `rule_code` IN (
    'mms_bom', 'mms_route', 'mms_process', 'mms_resource', 'mms_shift',
    'mms_demand', 'mms_mps', 'mms_kit', 'mms_kit_check',
    'mms_work_order', 'mms_workorder', 'mms_issue', 'mms_return_material', 'mms_return',
    'mms_report', 'mms_work_report', 'mms_qc', 'mms_abnormal', 'mms_finish', 'mms_finish_receipt',
    'mms_flow_card', 'mms_dispatch', 'mms_downtime', 'mms_outsource', 'mms_schedule'
);

-- 如果旧版脚本使用了 date_pattern / reset_cycle 等旧列名，尝试迁移数据到新列名
-- 注意：如果旧列已不存在，这些语句会被忽略
SET @col_old_date = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'date_pattern');
SET @sql_mig1 = IF(@col_old_date > 0, 'UPDATE `mk_number_rule` SET `date_format` = `date_pattern` WHERE `date_format` IS NULL AND `date_pattern` IS NOT NULL', 'SELECT 1');
PREPARE stmt_mig1 FROM @sql_mig1;
EXECUTE stmt_mig1;
DEALLOCATE PREPARE stmt_mig1;

SET @col_old_reset = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'reset_cycle');
SET @sql_mig2 = IF(@col_old_reset > 0, 'UPDATE `mk_number_rule` SET `reset_type` = CASE WHEN `reset_cycle` = ''day'' THEN ''1'' WHEN `reset_cycle` = ''month'' THEN ''2'' WHEN `reset_cycle` = ''year'' THEN ''3'' ELSE ''0'' END WHERE `reset_type` IS NULL AND `reset_cycle` IS NOT NULL', 'SELECT 1');
PREPARE stmt_mig2 FROM @sql_mig2;
EXECUTE stmt_mig2;
DEALLOCATE PREPARE stmt_mig2;

-- =============================================
-- 一、初始化生产管控编号规则（幂等插入）
-- =============================================

-- BOM编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_bom', 'mms', 'BOM编号', 'BOM', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), 'BOM物料清单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_bom');

-- 工艺路线编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_route', 'mms', '工艺路线编号', 'RT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '工艺路线编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_route');

-- 主生产计划编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_mps', 'mms', '主生产计划编号', 'MPS', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '主生产计划编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_mps');

-- 生产工单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_work_order', 'mms', '生产工单编号', 'WO', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '生产工单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_work_order');

-- 需求计划编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_demand', 'mms', '需求计划编号', 'DD', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '需求计划编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_demand');

-- 发料单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_issue', 'mms', '发料单编号', 'FL', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '发料单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_issue');

-- 退料单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_return_material', 'mms', '退料单编号', 'TL', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '退料单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_return_material');

-- 质检单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_qc', 'mms', '质检单编号', 'ZJ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '质检单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_qc');

-- 完工入库单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_finish_receipt', 'mms', '完工入库单编号', 'RK', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '完工入库单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_finish_receipt');

-- 齐套检查单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_kit_check', 'mms', '齐套检查单编号', 'KC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '齐套检查单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_kit_check');

-- 报工单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_work_report', 'mms', '报工单编号', 'RP', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '报工单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_work_report');

-- 异常单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_abnormal', 'mms', '异常单编号', 'EX', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '异常单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_abnormal');

-- 工序编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_process', 'mms', '工序编号', 'GX', '', '0', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '工序编号，永不重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_process');

-- 产能资源编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_resource', 'mms', '产能资源编号', 'RES', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '产能资源编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_resource');

-- 班次编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_shift', 'mms', '班次编号', 'SFT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '班次编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_shift');

-- 派工单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_dispatch', 'mms', '派工单编号', 'DI', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '派工单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_dispatch');

-- 流程卡编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_flow_card', 'mms', '流程卡编号', 'FC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '流程卡编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_flow_card');

-- 停机记录编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_downtime', 'mms', '停机记录编号', 'DT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '停机记录编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_downtime');

-- 委外单编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_outsource', 'mms', '委外单编号', 'OUT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '委外单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_outsource');

-- 排班计划编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'mms_schedule', 'mms', '排班计划编号', 'SC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '排班计划编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mms_schedule');

-- =============================================
-- 二、生产管控 - 编码规则菜单（挂在生产管控目录下）
-- query 字段使用 JSON 格式（前端 sidebar 通过 JSON.parse 解析）
-- =============================================

-- 查找生产管控根菜单ID
SET @mms_parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '生产管控' AND parent_id = 0 LIMIT 1);

-- 插入编码规则菜单（幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编码规则', @mms_parent_id, 99, 'numberRule', 'mms/numberRule/index', '{"module":"mms"}', 'MmsNumberRule', 1, 0, 'C', '0', '0', 'mms:numberRule:list', 'number', 'admin', sysdate(), '生产管控编码规则管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @mms_parent_id AND component = 'mms/numberRule/index');

SET @mms_rule_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @mms_parent_id AND component = 'mms/numberRule/index' LIMIT 1);

-- 插入按钮权限（幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则查询', @mms_rule_menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:numberRule:query', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:numberRule:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则新增', @mms_rule_menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:numberRule:add', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:numberRule:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则修改', @mms_rule_menu_id, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:numberRule:edit', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:numberRule:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则删除', @mms_rule_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:numberRule:remove', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:numberRule:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则导出', @mms_rule_menu_id, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:numberRule:export', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:numberRule:export');

-- =============================================
-- 三、给admin角色(1)和普通角色(2)分配编码规则菜单权限（幂等）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'mms:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'mms:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- =============================================
-- 四、验证数据
-- =============================================
SELECT '生产管控编号规则初始化完成' AS result;
SELECT rule_id, rule_code, module, rule_name, prefix, status FROM mk_number_rule WHERE module = 'mms' ORDER BY rule_code;
SELECT menu_id, menu_name, parent_id, query, perms FROM sys_menu WHERE perms LIKE 'mms:numberRule:%' ORDER BY menu_id;
