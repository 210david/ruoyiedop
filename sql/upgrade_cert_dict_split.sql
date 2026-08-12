-- =================================================================
-- 升级脚本：证照管理/证书管理 字典拆分 + 字段命名统一
-- 日期：2026-08-11
-- 说明：
--   1. 将原 safety_cert_type（证照类型）拆分为两个独立字典：
--      - safety_license_type：企业资质证照类型（一企一档/证照管理使用）
--      - safety_cert_type：培训证书类型（教育培训/证书管理使用）
--   2. 统一 biz_safety_training_cert 表字段命名，与 biz_safety_cert 保持一致：
--      cert_code → cert_no, valid_to → expire_date, cert_url → attachment
-- =================================================================

-- ----------------------------------------------------------------
-- 1. 新建 safety_license_type 字典（企业资质证照类型）
-- ----------------------------------------------------------------
DELETE FROM sys_dict_type WHERE dict_type = 'safety_license_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('证照类型', 'safety_license_type', '0', 'admin', NOW(), '企业资质证照类型');

DELETE FROM sys_dict_data WHERE dict_type = 'safety_license_type';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '安全生产许可证', '1', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '危化品经营许可证', '2', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '排污许可证', '3', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '营业执照', '4', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '消防验收合格证', '5', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 6, '安全生产标准化证书', '6', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), ''),
(NULL, 7, '其他', '9', 'safety_license_type', '', '', 'N', '0', 'admin', NOW(), '');

-- ----------------------------------------------------------------
-- 2. 确保 safety_cert_type 字典为培训证书类型（防止被旧证照数据覆盖）
-- ----------------------------------------------------------------
DELETE FROM sys_dict_type WHERE dict_type = 'safety_cert_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('培训证书类型', 'safety_cert_type', '0', 'admin', NOW(), '培训证书类型');

DELETE FROM sys_dict_data WHERE dict_type = 'safety_cert_type';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(NULL, 1, '三级安全教育合格证', '1', 'safety_cert_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '特种作业操作证', '2', 'safety_cert_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '安全培训合格证', '3', 'safety_cert_type', '', 'info', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '其他', '4', 'safety_cert_type', '', 'warning', 'N', '0', 'admin', NOW(), '');

-- ----------------------------------------------------------------
-- 3. 统一 biz_safety_training_cert 表字段命名
--    cert_code → cert_no, valid_to → expire_date, cert_url → attachment
-- ----------------------------------------------------------------
ALTER TABLE biz_safety_training_cert CHANGE COLUMN cert_code cert_no VARCHAR(64) NOT NULL COMMENT '证书编号';
ALTER TABLE biz_safety_training_cert CHANGE COLUMN valid_to expire_date DATE COMMENT '有效期至';
ALTER TABLE biz_safety_training_cert CHANGE COLUMN cert_url attachment VARCHAR(500) COMMENT '证书文件地址';

-- 重命名唯一索引（兼容 MySQL 8.0，不支持 DROP INDEX IF EXISTS 语法）
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_training_cert' AND INDEX_NAME = 'uk_cert_code');
SET @sql = IF(@idx_exists > 0, 'ALTER TABLE biz_safety_training_cert DROP INDEX uk_cert_code', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_training_cert' AND INDEX_NAME = 'uk_cert_no');
SET @sql = IF(@idx_exists = 0, 'ALTER TABLE biz_safety_training_cert ADD UNIQUE KEY uk_cert_no (cert_no)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 4. 删除多余的 safety_cert 证照编号规则（页面上已改为手动填写）
-- ----------------------------------------------------------------
DELETE FROM `mk_number_rule` WHERE rule_code = 'safety_cert';
