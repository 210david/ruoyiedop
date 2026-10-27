-- =============================================
-- 修复：质量管理 - 检验任务查看提示"系统繁忙，请稍后重试"
-- 根因分析：
--   QmsInspItemMapper.xml 的 selectInspItemListByTaskId 查询引用了 defect_qty 列，
--   但 qms_insp_item 表中不存在该列，导致 BadSqlGrammarException (Unknown column 'defect_qty')
--   被 GlobalExceptionHandler.handleRuntimeException 拦截，返回"系统繁忙，请稍后重试"。
-- 影响范围：检验任务查看（getInfo）、检验结果录入（handleInspect）、检验报告（getReport）
-- 本脚本幂等，可重复执行
-- =============================================

-- 添加 defect_qty 列（缺陷数量：该缺陷出现的次数）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item' AND COLUMN_NAME = 'defect_qty');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `qms_insp_item` ADD COLUMN `defect_qty` INT DEFAULT 0 COMMENT ''缺陷数量（该缺陷出现的次数）'' AFTER `defect_level`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证
SELECT CONCAT('qms_insp_item 表列数: ', COUNT(*)) AS info
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item';

SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_insp_item'
ORDER BY ORDINAL_POSITION;
