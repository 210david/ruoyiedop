-- =============================================
-- 质量管理模块 - 编号规则种子数据
-- 功能：质量管理模块各业务单据自动编码
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- =============================================
-- 0. 确保 mk_number_rule 表结构完整（兼容所有 MySQL 版本）
-- 补充动态前缀相关列，避免 selectRuleForUpdate 查询报错
-- =============================================

-- 0.1 添加 module 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'module');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产,qms=质量管理)'' AFTER `rule_code`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 更新 module 字段注释，包含 qms
SET @comment_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'module' AND COLUMN_COMMENT LIKE '%qms%');
SET @sql2 = IF(@comment_exists = 0, 'ALTER TABLE `mk_number_rule` MODIFY COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产,qms=质量管理)''', 'SELECT 1');
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

-- =============================================
-- 一、初始化质量管理编号规则（幂等插入）
-- =============================================

-- 不合格品报告编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_ncr', 'qms', '不合格品报告编号', 'NCR', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), 'NCR不合格品报告编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_ncr');

-- CAPA纠正预防措施编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_capa', 'qms', 'CAPA编号', 'CAPA', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), 'CAPA纠正预防措施编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_capa');

-- 客诉编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_complaint', 'qms', '客诉编号', 'CC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '客户投诉编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_complaint');

-- 内审计划编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_audit_plan', 'qms', '内审计划编号', 'IA', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '内部审核计划编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_audit_plan');

-- 材料评审编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_mr', 'qms', '材料评审编号', 'MR', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '材料评审报告编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_mr');


-- 检验任务编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_insp_task', 'qms', '检验任务编号', 'IQC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '检验任务编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_insp_task');

-- 文档编号
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'qms_doc', 'qms', '文档编号', 'DOC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '受控文档编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='qms_doc');

-- =============================================
-- 二、质量管理 - 编码规则菜单（挂在质量管理目录下）
-- query 字段使用 JSON 格式（前端 sidebar 通过 JSON.parse 解析）
-- =============================================

-- 查找质量管理根菜单ID
SET @qms_parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = 0 LIMIT 1);

-- 插入编码规则菜单（幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编码规则', @qms_parent_id, 99, 'numberRule', 'qms/numberRule/index', '{"module":"qms"}', 'QmsNumberRule', 1, 0, 'C', '0', '0', 'qms:numberRule:list', 'number', 'admin', sysdate(), '质量管理编码规则管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @qms_parent_id AND component = 'qms/numberRule/index');

SET @qms_rule_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '编码规则' AND parent_id = @qms_parent_id AND component = 'qms/numberRule/index' LIMIT 1);

-- 插入按钮权限（幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则查询', @qms_rule_menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'qms:numberRule:query', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'qms:numberRule:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则新增', @qms_rule_menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'qms:numberRule:add', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'qms:numberRule:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则修改', @qms_rule_menu_id, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'qms:numberRule:edit', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'qms:numberRule:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则删除', @qms_rule_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'qms:numberRule:remove', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'qms:numberRule:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则导出', @qms_rule_menu_id, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'qms:numberRule:export', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'qms:numberRule:export');

-- =============================================
-- 三、给admin角色(1)和普通角色(2)分配编码规则菜单权限（幂等）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'qms:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'qms:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- =============================================
-- 四、验证数据
-- =============================================
SELECT '质量管理编号规则初始化完成' AS result;
SELECT rule_id, rule_code, module, rule_name, prefix, status FROM mk_number_rule WHERE module = 'qms' ORDER BY rule_code;
SELECT menu_id, menu_name, parent_id, query, perms FROM sys_menu WHERE perms LIKE 'qms:numberRule:%' ORDER BY menu_id;
