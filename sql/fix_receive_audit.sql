-- ============================================================
-- 收货验收审核功能 - 数据库修复脚本
-- 用于修复缺少审核字段或审核日志表的数据库
-- 执行前请先备份数据库
-- ============================================================

-- 1. 检查并添加 pms_receive 表的审核字段
-- 使用存储过程安全地添加字段（如果字段已存在则跳过）

-- 添加 audit_by 字段
SET @db_name = DATABASE();
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS 
     WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'pms_receive' AND COLUMN_NAME = 'audit_by') = 0,
    'ALTER TABLE pms_receive ADD COLUMN audit_by VARCHAR(64) DEFAULT NULL COMMENT ''审核人''',
    'SELECT ''audit_by already exists'' AS result'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 audit_time 字段
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS 
     WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'pms_receive' AND COLUMN_NAME = 'audit_time') = 0,
    'ALTER TABLE pms_receive ADD COLUMN audit_time DATETIME DEFAULT NULL COMMENT ''审核时间''',
    'SELECT ''audit_time already exists'' AS result'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 audit_opinion 字段
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS 
     WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'pms_receive' AND COLUMN_NAME = 'audit_opinion') = 0,
    'ALTER TABLE pms_receive ADD COLUMN audit_opinion VARCHAR(500) DEFAULT NULL COMMENT ''审核意见''',
    'SELECT ''audit_opinion already exists'' AS result'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 创建审核日志表（如果不存在）
CREATE TABLE IF NOT EXISTS pms_receive_audit_log (
    log_id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    receive_id       BIGINT        NOT NULL                 COMMENT '收货ID',
    audit_action     VARCHAR(1)    DEFAULT NULL             COMMENT '审核动作（1通过 2驳回）',
    audit_by         VARCHAR(64)   DEFAULT NULL             COMMENT '审核人',
    audit_time       DATETIME      DEFAULT NULL             COMMENT '审核时间',
    audit_remark     VARCHAR(500)  DEFAULT NULL             COMMENT '审核意见',
    PRIMARY KEY (log_id),
    KEY idx_audit_log_receive (receive_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货验收审核日志';

-- 3. 验证修复结果
SELECT '=== 修复结果验证 ===' AS info;
SELECT 
    COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'pms_receive' 
  AND COLUMN_NAME IN ('audit_by', 'audit_time', 'audit_opinion')
ORDER BY ORDINAL_POSITION;

SELECT '=== 审核日志表检查 ===' AS info;
SELECT 
    TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'pms_receive_audit_log';

SELECT '=== 已有审核日志数据 ===' AS info;
SELECT COUNT(*) AS log_count FROM pms_receive_audit_log;

-- 4. 如果有已审核但缺少审核日志的收货单，可以手动补充
-- 以下查询找出已审核但没有审核日志的收货单
SELECT '=== 缺少审核日志的已审核收货单 ===' AS info;
SELECT r.receive_id, r.receive_no, r.status, r.audit_by, r.audit_time, r.audit_opinion
FROM pms_receive r
LEFT JOIN pms_receive_audit_log l ON r.receive_id = l.receive_id
WHERE r.del_flag = '0' 
  AND r.audit_by IS NOT NULL 
  AND l.log_id IS NULL;
