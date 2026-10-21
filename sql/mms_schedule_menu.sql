-- ============================================================
-- MMS 排产管理 - 菜单 + 字典 + 编号规则
-- 执行时间: 2026-08-19
-- 说明: 为排产管理页面添加菜单、按钮权限、字典数据和编号规则
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 〇、mms_schedule_task 表补充字段
-- 说明：原表缺少 schedule_no / product_id / product_code / product_name / strategy
-- ============================================================

-- 检查并添加 schedule_no
SET @s = CONCAT('ALTER TABLE `mms_schedule_task` ADD COLUMN `schedule_no` VARCHAR(50) DEFAULT '''' COMMENT ''排产编号'' AFTER `task_id`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_schedule_task' AND COLUMN_NAME = 'schedule_no');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 product_id
SET @s = CONCAT('ALTER TABLE `mms_schedule_task` ADD COLUMN `product_id` BIGINT(20) DEFAULT NULL COMMENT ''产品ID'' AFTER `work_order_no`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_schedule_task' AND COLUMN_NAME = 'product_id');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 product_code
SET @s = CONCAT('ALTER TABLE `mms_schedule_task` ADD COLUMN `product_code` VARCHAR(50) DEFAULT '''' COMMENT ''产品编码'' AFTER `product_id`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_schedule_task' AND COLUMN_NAME = 'product_code');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 product_name
SET @s = CONCAT('ALTER TABLE `mms_schedule_task` ADD COLUMN `product_name` VARCHAR(200) DEFAULT '''' COMMENT ''产品名称'' AFTER `product_code`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_schedule_task' AND COLUMN_NAME = 'product_name');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检查并添加 strategy
SET @s = CONCAT('ALTER TABLE `mms_schedule_task` ADD COLUMN `strategy` CHAR(1) DEFAULT ''1'' COMMENT ''排产策略(1正排 2倒排)'' AFTER `setup_minutes`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_schedule_task' AND COLUMN_NAME = 'strategy');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 一、字典类型
-- ============================================================

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '排产状态', 'mms_schedule_status', '0', 'admin', sysdate(), '排产计划状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_schedule_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '排产策略', 'mms_schedule_strategy', '0', 'admin', sysdate(), '排产策略类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mms_schedule_strategy');

-- ============================================================
-- 二、字典数据
-- ============================================================

-- 排产状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待确认', '0', 'mms_schedule_status', '', 'info', 'Y', '0', 'admin', sysdate(), '待确认'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mms_schedule_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已确认', '1', 'mms_schedule_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已确认'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mms_schedule_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已下达', '2', 'mms_schedule_status', '', 'success', 'N', '0', 'admin', sysdate(), '已下达'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mms_schedule_status' AND dict_value = '2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已取消', '3', 'mms_schedule_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已取消'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mms_schedule_status' AND dict_value = '3');

-- 排产策略
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '正排', '1', 'mms_schedule_strategy', '', 'primary', 'Y', '0', 'admin', sysdate(), '正排（从计划开始向后推算）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mms_schedule_strategy' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '倒排', '2', 'mms_schedule_strategy', '', 'warning', 'N', '0', 'admin', sysdate(), '倒排（从交期向前推算）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mms_schedule_strategy' AND dict_value = '2');

-- ============================================================
-- 三、编号规则
-- ============================================================

INSERT INTO `mk_number_rule`(`rule_code`,`module`,`rule_name`,`prefix`,`date_format`,`seq_length`,`reset_type`,`status`,`remark`,`create_by`,`create_time`)
SELECT 'mms_schedule', 'mms', '排产编号', 'SCH', 'yyyyMMdd', 4, '0', '0', '排产管理编号规则', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code = 'mms_schedule');

-- ============================================================
-- 四、菜单权限
-- ============================================================

-- 排产管理菜单（挂在"生产排产"目录下，parent_id=9020）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产管理', 9020, 4, 'schedule', 'mms/schedule/index', '', '', 1, 0, 'C', '0', '0', 'mms:schedule:list', 'date', 'admin', sysdate(), '排产管理菜单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:list');

-- 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产查询', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:schedule:list' LIMIT 1) t), 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:schedule:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产新增', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:schedule:list' LIMIT 1) t), 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:schedule:add', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产修改', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:schedule:list' LIMIT 1) t), 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:schedule:edit', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产删除', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:schedule:list' LIMIT 1) t), 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:schedule:remove', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产导出', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:schedule:list' LIMIT 1) t), 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:schedule:export', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:export');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产确认', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:schedule:list' LIMIT 1) t), 6, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:schedule:confirm', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:confirm');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产下达', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:schedule:list' LIMIT 1) t), 7, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:schedule:release', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:release');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排产取消', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:schedule:list' LIMIT 1) t), 8, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:schedule:cancel', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:schedule:cancel');

-- ============================================================
-- 五、角色菜单分配（admin 角色 = 1）
-- ============================================================

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN (
  'mms:schedule:list', 'mms:schedule:query', 'mms:schedule:add',
  'mms:schedule:edit', 'mms:schedule:remove', 'mms:schedule:export',
  'mms:schedule:confirm', 'mms:schedule:release', 'mms:schedule:cancel'
) ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);
