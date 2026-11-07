-- =====================================================================
-- 安全生产模块-考试功能 P1 增量升级脚本（FR-7/8/10/11/12/13）
-- 依赖：safety_exam_all.sql（P0 全量脚本）已执行
-- 幂等性：可重复执行；新增列通过 information_schema 判断
-- 执行库：ry-vue
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、biz_safety_exam 增加补考相关字段（FR-8 补考管理）
--   retake_from_id  补考来源场次ID（NULL=非补考）
--   retake_count    补考代数（0=首次考试，1=第1次补考...）
--   score_strategy  成绩覆盖策略（1=取最高 2=取最新，回写参训人时生效）
-- ---------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'retake_from_id');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN retake_from_id bigint NULL COMMENT ''补考来源场次ID'' AFTER record_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'retake_count');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN retake_count int NOT NULL DEFAULT 0 COMMENT ''补考代数（0=首次考试）'' AFTER retake_from_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'score_strategy');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN score_strategy char(1) NULL DEFAULT ''2'' COMMENT ''成绩覆盖策略（1取最高 2取最新）'' AFTER retake_count',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 二、交卷方式字典补充「无操作锁卷」（FR-13 答题页基础防护）
-- ---------------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '无操作锁卷', '4', 'safety_exam_submit_type', NULL, 'warning', 'N', '0', 'admin', NOW(), 'FR-13：无操作超时自动锁卷交卷'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_exam_submit_type' AND dict_value = '4');
