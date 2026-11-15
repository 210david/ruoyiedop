-- =============================================================
-- 升级脚本：AI流程生成新增"备注说明"字段
-- 日期：2026-11-15
-- 适用：已存在 flow_ai_gen_task 表的环境（幂等，可重复执行）
-- 说明：remark 保存用户填写的生成提示要求，重新解析时一并送入提示词
-- =============================================================

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'flow_ai_gen_task'
      AND COLUMN_NAME = 'remark'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `flow_ai_gen_task` ADD COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT ''备注说明（用户补充的生成要求）'' AFTER `category`',
    'SELECT ''remark 字段已存在，跳过'' AS notice'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 校验
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'flow_ai_gen_task'
  AND COLUMN_NAME = 'remark';
