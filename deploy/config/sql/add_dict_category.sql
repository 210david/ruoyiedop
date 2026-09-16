-- ----------------------------
-- 字典分类管理 - 添加 dict_category 字段和字典数据
-- ----------------------------

-- 1. 给 sys_dict_type 表添加 dict_category 字段
-- 如果列已存在会报错，可先检查
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_dict_type' AND COLUMN_NAME = 'dict_category');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `sys_dict_type` ADD COLUMN `dict_category` varchar(50) DEFAULT ''system'' COMMENT ''字典分类（业务模块）'' AFTER `dict_type`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 自动根据 dict_type 前缀对现有字典进行分类
-- 系统管理模块（sys_ 开头）
-- 2. 自动根据 dict_type 前缀对现有字典进行分类（用 FIND_IN_SET 避免重复更新）
UPDATE `sys_dict_type` SET `dict_category` = 'dms' WHERE `dict_type` LIKE 'dms_%';
UPDATE `sys_dict_type` SET `dict_category` = 'pms' WHERE `dict_type` LIKE 'pms_%';
UPDATE `sys_dict_type` SET `dict_category` = 'mk' WHERE `dict_type` LIKE 'mk_%';
UPDATE `sys_dict_type` SET `dict_category` = 'wms' WHERE `dict_type` LIKE 'wms_%';
UPDATE `sys_dict_type` SET `dict_category` = 'safety' WHERE `dict_type` LIKE 'safety_%';
UPDATE `sys_dict_type` SET `dict_category` = 'qms' WHERE `dict_type` LIKE 'qms_%';
UPDATE `sys_dict_type` SET `dict_category` = 'mms' WHERE `dict_type` LIKE 'mms_%';
UPDATE `sys_dict_type` SET `dict_category` = 'hr' WHERE `dict_type` LIKE 'hr_%';
-- sys_ 开头和其他字典默认归为系统管理
UPDATE `sys_dict_type` SET `dict_category` = 'system' WHERE `dict_type` LIKE 'sys_%' OR `dict_category` IS NULL OR `dict_category` = '';

-- 3. 添加字典分类字典类型（如果已存在则跳过）
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '字典分类', 'sys_dict_category', '0', 'admin', sysdate(), '字典所属业务模块分类'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'sys_dict_category');

-- 4. 添加字典分类字典数据（如果已存在则跳过）
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT * FROM (
  SELECT 1 AS dict_sort, '系统管理' AS dict_label, 'system' AS dict_value, 'sys_dict_category' AS dict_type, '' AS css_class, 'primary' AS list_class, 'Y' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time, '系统管理模块字典' AS remark
  UNION ALL
  SELECT 2, '设备管理', 'dms', 'sys_dict_category', '', 'success', 'N', '0', 'admin', sysdate(), '设备管理模块字典'
  UNION ALL
  SELECT 3, '采购管理', 'pms', 'sys_dict_category', '', 'warning', 'N', '0', 'admin', sysdate(), '采购管理模块字典'
  UNION ALL
  SELECT 4, '营销管理', 'mk', 'sys_dict_category', '', 'danger', 'N', '0', 'admin', sysdate(), '营销管理模块字典'
  UNION ALL
  SELECT 5, '制造管理', 'mms', 'sys_dict_category', '', 'primary', 'N', '0', 'admin', sysdate(), '制造管理模块字典'
  UNION ALL
  SELECT 6, '仓储管理', 'wms', 'sys_dict_category', '', 'info', 'N', '0', 'admin', sysdate(), '仓储管理模块字典'
  UNION ALL
  SELECT 7, '安全生产管理', 'safety', 'sys_dict_category', '', 'warning', 'N', '0', 'admin', sysdate(), '安全生产管理模块字典'
  UNION ALL
  SELECT 8, '质量管理', 'qms', 'sys_dict_category', '', 'success', 'N', '0', 'admin', sysdate(), '质量管理模块字典'
  UNION ALL
  SELECT 9, '人力资源', 'hr', 'sys_dict_category', '', 'info', 'N', '0', 'admin', sysdate(), '人力资源模块字典'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'sys_dict_category' AND `dict_value` = tmp.dict_value);
