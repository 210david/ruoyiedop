-- =============================================
-- 编号规则动态前缀增强
-- 功能：支持按业务字段值（如物料类型）动态设置编号前缀
-- =============================================

-- =============================================
-- 1. 主表增加动态前缀配置字段
-- =============================================
-- 使用存储过程安全添加列（兼容MySQL不支持IF NOT EXISTS的情况）
DROP PROCEDURE IF EXISTS `add_prefix_columns`;
DELIMITER $$
CREATE PROCEDURE `add_prefix_columns`()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field') THEN
        ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field` VARCHAR(50) DEFAULT NULL COMMENT '动态前缀关联字段名（如 materialType）' AFTER `connector`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field_dict_type') THEN
        ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_dict_type` VARCHAR(100) DEFAULT NULL COMMENT '动态前缀关联字典类型（如 wms_material_type）' AFTER `prefix_field`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule' AND COLUMN_NAME = 'prefix_field_enabled') THEN
        ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_enabled` CHAR(1) DEFAULT '0' COMMENT '是否启用动态前缀（0=否,1=是）' AFTER `prefix_field_dict_type`;
    END IF;
END$$
DELIMITER ;
CALL `add_prefix_columns`();
DROP PROCEDURE IF EXISTS `add_prefix_columns`;

-- =============================================
-- 2. 创建前缀映射子表
-- =============================================
DROP TABLE IF EXISTS `mk_number_rule_prefix`;
CREATE TABLE `mk_number_rule_prefix` (
  `prefix_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `rule_id`       BIGINT(20)    NOT NULL                 COMMENT '规则ID',
  `field_value`   VARCHAR(50)   NOT NULL                 COMMENT '字段值（字典值，如 0/1/2/3）',
  `field_label`   VARCHAR(100)  DEFAULT ''               COMMENT '字段标签（字典标签，如 原材料/半成品）',
  `prefix`        VARCHAR(20)   DEFAULT ''               COMMENT '对应前缀（如 YL/BC/CP）',
  `enabled`       CHAR(1)       DEFAULT '1'              COMMENT '是否启用（0=否,1=是）',
  `create_by`     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME                               COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME                               COMMENT '更新时间',
  PRIMARY KEY (`prefix_id`),
  KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编号规则动态前缀映射表';

-- =============================================
-- 3. 配置物料编码规则：启用动态前缀
-- =============================================
UPDATE `mk_number_rule`
SET `prefix_field` = 'materialType',
    `prefix_field_dict_type` = 'wms_material_type',
    `prefix_field_enabled` = '1',
    `prefix` = ''
WHERE `rule_code` = 'wms_material';

-- =============================================
-- 4. 初始化物料类型前缀映射
-- =============================================
INSERT INTO `mk_number_rule_prefix` (`rule_id`, `field_value`, `field_label`, `prefix`, `enabled`, `create_by`, `create_time`)
SELECT r.rule_id, '0', '原材料', 'YL', '1', 'admin', sysdate()
FROM `mk_number_rule` r
WHERE r.rule_code = 'wms_material'
  AND NOT EXISTS (SELECT 1 FROM `mk_number_rule_prefix` p WHERE p.rule_id = r.rule_id AND p.field_value = '0');

INSERT INTO `mk_number_rule_prefix` (`rule_id`, `field_value`, `field_label`, `prefix`, `enabled`, `create_by`, `create_time`)
SELECT r.rule_id, '1', '半成品', 'BC', '1', 'admin', sysdate()
FROM `mk_number_rule` r
WHERE r.rule_code = 'wms_material'
  AND NOT EXISTS (SELECT 1 FROM `mk_number_rule_prefix` p WHERE p.rule_id = r.rule_id AND p.field_value = '1');

INSERT INTO `mk_number_rule_prefix` (`rule_id`, `field_value`, `field_label`, `prefix`, `enabled`, `create_by`, `create_time`)
SELECT r.rule_id, '2', '成品', 'CP', '1', 'admin', sysdate()
FROM `mk_number_rule` r
WHERE r.rule_code = 'wms_material'
  AND NOT EXISTS (SELECT 1 FROM `mk_number_rule_prefix` p WHERE p.rule_id = r.rule_id AND p.field_value = '2');

INSERT INTO `mk_number_rule_prefix` (`rule_id`, `field_value`, `field_label`, `prefix`, `enabled`, `create_by`, `create_time`)
SELECT r.rule_id, '3', '辅料', 'FL', '1', 'admin', sysdate()
FROM `mk_number_rule` r
WHERE r.rule_code = 'wms_material'
  AND NOT EXISTS (SELECT 1 FROM `mk_number_rule_prefix` p WHERE p.rule_id = r.rule_id AND p.field_value = '3');

-- =============================================
-- 5. 清理旧的按类型拆分的规则（wms_material_0~3）
-- =============================================
DELETE FROM `mk_number_rule` WHERE `rule_code` IN ('wms_material_0', 'wms_material_1', 'wms_material_2', 'wms_material_3');
