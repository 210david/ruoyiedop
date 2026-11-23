-- =============================================================
-- 集成模块绑定口径调整：外部平台用户绑定到平台组织用户（sys_user）
-- 版本: v1.0  日期: 2026-11-23
--
-- 背景：原人工绑定/自动匹配均以 HR 员工档案（biz_hr_employee）为绑定对象，
--       前端选人组件已统一改为组织用户（sys_user），导致绑定时报
--       「工号 xxx 未找到对应员工档案」。本次将绑定权威对象调整为 sys_user，
--       employee_no/employee_id 降级为考勤落库的派生信息（所选用户关联了
--       员工档案时回填，未关联留空，考勤同步按未映射跳过）。
--
-- 幂等：可重复执行。
-- =============================================================

-- 1. 新增 user_id 列（绑定权威对象：sys_user.user_id）
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'integration_user_binding'
     AND COLUMN_NAME = 'user_id'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `integration_user_binding` ADD COLUMN `user_id` BIGINT DEFAULT NULL COMMENT ''平台组织用户ID(sys_user.user_id，绑定权威对象)'' AFTER `employee_id`',
  'SELECT ''user_id 已存在，跳过'' AS result');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 存量数据迁移：按档案关联（biz_hr_employee.user_id）回填 user_id
--    仅迁移当前仍以 employee_id 绑定且 user_id 为空的行
UPDATE integration_user_binding b
JOIN biz_hr_employee e ON b.employee_id = e.employee_id AND e.user_id IS NOT NULL
SET b.user_id = e.user_id
WHERE b.user_id IS NULL AND b.employee_id IS NOT NULL;

-- 3. 唯一索引迁移：uk_connector_employee (connector_type, employee_id)
--    → uk_connector_user (connector_type, user_id)，与一一绑定新口径一致
SET @old_idx := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'integration_user_binding'
     AND INDEX_NAME = 'uk_connector_employee'
);
SET @ddl := IF(@old_idx > 0,
  'ALTER TABLE `integration_user_binding` DROP INDEX `uk_connector_employee`',
  'SELECT ''uk_connector_employee 不存在，跳过删除'' AS result');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @new_idx := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'integration_user_binding'
     AND INDEX_NAME = 'uk_connector_user'
);
SET @ddl := IF(@new_idx = 0,
  'ALTER TABLE `integration_user_binding` ADD UNIQUE KEY `uk_connector_user` (`connector_type`, `user_id`)',
  'SELECT ''uk_connector_user 已存在，跳过'' AS result');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 自检：迁移后不应存在「同一用户被同一连接器多个外部账号绑定」的数据
SELECT connector_type, user_id, COUNT(*) AS cnt
FROM integration_user_binding
WHERE user_id IS NOT NULL
GROUP BY connector_type, user_id
HAVING COUNT(*) > 1;
-- 预期：0 行。若出现非 0 行，说明存量数据本身违反一一绑定，需人工核对解绑后再建索引。

-- 5. 复核：user_id 列与 uk_connector_user 索引均应存在
SHOW COLUMNS FROM `integration_user_binding` LIKE 'user_id';
SHOW INDEX FROM `integration_user_binding` WHERE Key_name = 'uk_connector_user';
