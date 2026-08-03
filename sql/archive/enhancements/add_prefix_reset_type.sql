-- ============================================================
-- 增加"按动态前缀重置"功能
-- 1. mk_number_rule_prefix 表增加 current_seq, current_date_str 字段
-- 2. mk_number_reset_type 字典增加"按动态前缀"选项
-- ============================================================

-- 使用存储过程安全添加列（兼容各版本 MySQL）
DROP PROCEDURE IF EXISTS `add_prefix_seq_columns`;
DELIMITER $$
CREATE PROCEDURE `add_prefix_seq_columns`()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule_prefix' AND COLUMN_NAME = 'current_seq') THEN
        ALTER TABLE `mk_number_rule_prefix` ADD COLUMN `current_seq` BIGINT DEFAULT 0 COMMENT '当前序列号（按动态前缀重置时使用）' AFTER `enabled`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_number_rule_prefix' AND COLUMN_NAME = 'current_date_str') THEN
        ALTER TABLE `mk_number_rule_prefix` ADD COLUMN `current_date_str` VARCHAR(20) DEFAULT '' COMMENT '当前日期串（按动态前缀重置时使用）' AFTER `current_seq`;
    END IF;
END$$
DELIMITER ;
CALL `add_prefix_seq_columns`();
DROP PROCEDURE IF EXISTS `add_prefix_seq_columns`;

-- 添加字典数据：按动态前缀重置
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`)
SELECT 5, '按动态前缀', '4', 'mk_number_reset_type', '', 'primary', 'N', '0', '每个动态前缀维护独立序号', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mk_number_reset_type' AND `dict_value` = '4');

-- 清除字典缓存（如果使用了 Redis）
-- 应用层重启后会自动刷新字典缓存
