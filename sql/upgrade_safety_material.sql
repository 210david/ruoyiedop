-- =============================================
-- 危化品管理升级脚本
-- 1. 增加危化品编号字段（自动编码）
-- 2. 去掉是否危化品字段
-- 3. 危化品分类改为字典
-- 4. 单位改为通用单位（wms_unit字典）
-- 说明：本脚本可重复执行（幂等设计）
-- =============================================

-- =============================================
-- 一、表结构变更：增加 material_code，删除 is_hazardous
-- =============================================

-- 增加 material_code 字段（如果不存在）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'material_code');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `material_code` VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''危化品编号'' AFTER `material_id`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除 is_hazardous 字段（如果存在）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'is_hazardous');
SET @sql = IF(@col_exists = 1, 'ALTER TABLE `biz_safety_material` DROP COLUMN `is_hazardous`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加唯一索引（如果不存在）
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND INDEX_NAME = 'uk_material_code');
SET @sql = IF(@idx_exists = 0, 'ALTER TABLE `biz_safety_material` ADD UNIQUE KEY `uk_material_code` (`material_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 二、为已有数据生成危化品编号（如果为空）
-- =============================================
UPDATE biz_safety_material SET material_code = CONCAT('WH', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(material_id, 4, '0')) WHERE material_code = '' OR material_code IS NULL;

-- =============================================
-- 三、字典数据：危化品分类（safety_hazard_class）
-- =============================================
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '第1类 爆炸品', '1', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '第2.1类 易燃气体', '2', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '第2.2类 非易燃无毒气体', '3', 'safety_hazard_class', 'info', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '第2.3类 毒性气体', '4', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '第3类 易燃液体', '5', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '第4.1类 易燃固体', '6', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 7, '第4.2类 自燃物品', '7', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 8, '第4.3类 遇水放出易燃气体物质', '8', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 9, '第5.1类 氧化性物质', '9', 'safety_hazard_class', 'primary', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 10, '第5.2类 有机过氧化物', '10', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 11, '第6.1类 毒性物质', '11', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 12, '第6.2类 感染性物质', '12', 'safety_hazard_class', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 13, '第7类 放射性物质', '13', 'safety_hazard_class', 'info', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 14, '第8类 腐蚀性物质', '14', 'safety_hazard_class', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 15, '第9类 杂项危险物质', '15', 'safety_hazard_class', 'info', '', 'N', '0', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- =============================================
-- 四、字典数据：剧毒/易制爆标识（safety_toxic_flag）
-- =============================================
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '无', '0', 'safety_toxic_flag', 'info', '', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '剧毒', '1', 'safety_toxic_flag', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '易制爆', '2', 'safety_toxic_flag', 'warning', '', 'N', '0', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- =============================================
-- 五、编号规则：危化品编号（safety_material）
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_material', 'safety', '危化品编号', 'WH', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '危化品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_material');

-- =============================================
-- 六、验证数据
-- =============================================
SELECT '危化品管理升级完成' AS result;
SELECT material_id, material_code, material_name, hazard_class FROM biz_safety_material WHERE del_flag = '0' LIMIT 5;
