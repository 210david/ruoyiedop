-- ============================================
-- 消息中心升级脚本 - 增加角色权限过滤
-- 日期: 2026-08-26
-- 说明: 1. sys_message表增加recipient_role_key字段
--       2. 为sys_message_read表增加索引优化
--       3. 更新sys_message.sql建表脚本同步
-- ============================================

-- 1. 添加 recipient_role_key 字段（如果不存在）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_message' AND COLUMN_NAME = 'recipient_role_key');
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE sys_message ADD COLUMN recipient_role_key VARCHAR(100) DEFAULT NULL COMMENT ''接收角色权限标识（为空则全员可见，否则拥有该权限的用户可见'' AFTER recipient_id',
    'SELECT ''recipient_role_key already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 为recipient_role_key添加索引
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_message' AND INDEX_NAME = 'idx_recipient_role_key');
SET @sql = IF(@idx_exists = 0,
    'ALTER TABLE sys_message ADD KEY idx_recipient_role_key (recipient_role_key)',
    'SELECT ''idx_recipient_role_key already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 为sys_message_read表message_id添加索引（优化按消息ID查询）
SET @idx2_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_message_read' AND INDEX_NAME = 'idx_message_id');
SET @sql = IF(@idx2_exists = 0,
    'ALTER TABLE sys_message_read ADD KEY idx_message_id (message_id)',
    'SELECT ''idx_message_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
