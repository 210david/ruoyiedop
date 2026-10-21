-- =============================================
-- 迁移脚本：移除行业模板表，改用字典 qms_industry_type
-- 说明：本脚本用于已有数据库升级，可重复执行
-- 日期：2026-08-12
-- =============================================

-- 1. qms_inspect_std: tpl_id -> industry_type
-- 先加列，再迁移数据，最后删旧列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'qms_inspect_std' AND column_name = 'industry_type');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE qms_inspect_std ADD COLUMN industry_type VARCHAR(32) COMMENT ''行业类型（字典 qms_industry_type）'' AFTER judge_rule', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 迁移数据：从 qms_industry_tpl 带回 industry_type
UPDATE qms_inspect_std s
INNER JOIN qms_industry_tpl t ON s.tpl_id = t.tpl_id
SET s.industry_type = t.industry_type
WHERE s.tpl_id IS NOT NULL;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'qms_inspect_std' AND column_name = 'tpl_id');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE qms_inspect_std DROP COLUMN tpl_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. qms_material_attr: tpl_id -> industry_type
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'qms_material_attr' AND column_name = 'industry_type');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE qms_material_attr ADD COLUMN industry_type VARCHAR(32) COMMENT ''行业类型（字典 qms_industry_type）'' AFTER inspect_types', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE qms_material_attr a
INNER JOIN qms_industry_tpl t ON a.tpl_id = t.tpl_id
SET a.industry_type = t.industry_type
WHERE a.tpl_id IS NOT NULL;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'qms_material_attr' AND column_name = 'tpl_id');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE qms_material_attr DROP COLUMN tpl_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. qms_doc: tpl_id -> industry_type
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'qms_doc' AND column_name = 'industry_type');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE qms_doc ADD COLUMN industry_type VARCHAR(32) COMMENT ''行业类型（字典 qms_industry_type）'' AFTER dept_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE qms_doc d
INNER JOIN qms_industry_tpl t ON d.tpl_id = t.tpl_id
SET d.industry_type = t.industry_type
WHERE d.tpl_id IS NOT NULL;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE table_schema = DATABASE() AND table_name = 'qms_doc' AND column_name = 'tpl_id');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE qms_doc DROP COLUMN tpl_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. 删除行业模板菜单及权限
DELETE FROM sys_menu WHERE perms LIKE 'qms:tpl:%' OR (menu_name = '行业模板' AND component = 'qms/tpl/index');

-- 5. 删除行业模板表
DROP TABLE IF EXISTS qms_industry_tpl;

-- 6. 验证
SELECT '迁移完成：行业模板表已移除，相关表已改用 industry_type 字段' AS result;
