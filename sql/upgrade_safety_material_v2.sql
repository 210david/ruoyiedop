-- ============================================================
-- 危化品管理升级脚本 V2
-- Phase1: 出入库管理 + 库存动态联动 + 库存预警 + 储存区域关联
-- Phase2: SDS结构化信息 + 重大危险源辨识 + 合规检查清单 + 供应商/客户管理
-- 说明：本脚本可重复执行（幂等设计）
-- ============================================================

-- =============================================
-- 一、ALTER TABLE: biz_safety_material 增加SDS结构化字段和库存预警字段
-- =============================================

-- 安全库存下限
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'safety_stock_min');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `safety_stock_min` DECIMAL(18,2) DEFAULT 0 COMMENT ''安全库存下限'' AFTER `current_stock`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 安全库存上限
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'safety_stock_max');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `safety_stock_max` DECIMAL(18,2) DEFAULT 0 COMMENT ''安全库存上限'' AFTER `safety_stock_min`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 供应商ID
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'default_supplier_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `default_supplier_id` BIGINT DEFAULT NULL COMMENT ''默认供应商ID'' AFTER `safety_stock_max`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 理化特性
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'physical_properties');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `physical_properties` TEXT COMMENT ''理化特性（外观、气味、密度、熔沸点、闪点等）''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 危险性概述
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'hazard_summary');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `hazard_summary` TEXT COMMENT ''危险性概述''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 急救措施
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'first_aid_measures');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `first_aid_measures` TEXT COMMENT ''急救措施''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 灭火方法
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'fire_fighting');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `fire_fighting` TEXT COMMENT ''灭火方法与灭火剂''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 泄漏应急处理
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'leak_handling');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `leak_handling` TEXT COMMENT ''泄漏应急处理''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 储存注意事项
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'storage_precaution');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `storage_precaution` TEXT COMMENT ''储存注意事项''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 接触控制/个人防护
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'exposure_control');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `exposure_control` TEXT COMMENT ''接触控制/个人防护装备(PPE)''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 稳定性和反应性
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'stability_reactivity');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `stability_reactivity` TEXT COMMENT ''稳定性和反应性''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 毒理学信息
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'toxicological_info');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `toxicological_info` TEXT COMMENT ''毒理学信息''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 生态学信息
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'ecological_info');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `ecological_info` TEXT COMMENT ''生态学信息''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 废弃处置
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'disposal_info');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `disposal_info` TEXT COMMENT ''废弃处置''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 运输信息
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'transport_info');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `transport_info` TEXT COMMENT ''运输信息''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 闪点
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'flash_point');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `flash_point` VARCHAR(64) DEFAULT NULL COMMENT ''闪点(℃)''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 爆炸极限
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_safety_material' AND COLUMN_NAME = 'explosion_limit');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `biz_safety_material` ADD COLUMN `explosion_limit` VARCHAR(128) DEFAULT NULL COMMENT ''爆炸极限(%V/V)''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================
-- 二、新建表：出入库记录表 biz_safety_material_record
-- =============================================
CREATE TABLE IF NOT EXISTS biz_safety_material_record (
    record_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    record_code      VARCHAR(64)  NOT NULL                 COMMENT '出入库单号',
    material_id      BIGINT       NOT NULL                 COMMENT '危化品ID',
    material_code    VARCHAR(64)                           COMMENT '危化品编号',
    material_name    VARCHAR(128)                          COMMENT '危化品名称',
    record_type      CHAR(1)      NOT NULL                 COMMENT '记录类型（1入库 2出库）',
    quantity         DECIMAL(18,2) NOT NULL                COMMENT '数量',
    unit             VARCHAR(20)                           COMMENT '单位',
    before_stock     DECIMAL(18,2)                         COMMENT '操作前库存',
    after_stock      DECIMAL(18,2)                         COMMENT '操作后库存',
    supplier_id      BIGINT                                COMMENT '供应商/客户ID',
    supplier_name    VARCHAR(128)                          COMMENT '供应商/客户名称',
    recipient        VARCHAR(64)                           COMMENT '领用人',
    purpose          VARCHAR(500)                          COMMENT '用途',
    batch_no         VARCHAR(64)                           COMMENT '批次号',
    operator         VARCHAR(64)                           COMMENT '操作人',
    record_time      DATETIME                              COMMENT '出入库时间',
    attachment       VARCHAR(500)                          COMMENT '附件',
    enterprise_id    BIGINT                                COMMENT '企业ID',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (record_id),
    UNIQUE KEY uk_record_code (record_code),
    KEY idx_material_id (material_id),
    KEY idx_record_type (record_type),
    KEY idx_record_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危化品出入库记录表';

-- =============================================
-- 三、新建表：供应商/客户管理表 biz_safety_material_supplier
-- =============================================
CREATE TABLE IF NOT EXISTS biz_safety_material_supplier (
    supplier_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '供应商ID',
    supplier_code    VARCHAR(64)  NOT NULL                 COMMENT '供应商/客户编号',
    supplier_name    VARCHAR(128) NOT NULL                 COMMENT '供应商/客户名称',
    supplier_type    CHAR(1)      NOT NULL DEFAULT '1'     COMMENT '类型（1供应商 2客户）',
    credit_code      VARCHAR(18)                           COMMENT '统一社会信用代码',
    contact_person   VARCHAR(64)                           COMMENT '联系人',
    contact_phone    VARCHAR(20)                           COMMENT '联系电话',
    address          VARCHAR(500)                          COMMENT '地址',
    business_scope   VARCHAR(500)                          COMMENT '经营范围',
    license_attachment VARCHAR(500)                        COMMENT '营业执照/许可证附件',
    enterprise_id    BIGINT                                COMMENT '企业ID',
    status           CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag         CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by        VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time      DATETIME                              COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time      DATETIME                              COMMENT '更新时间',
    remark           VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (supplier_id),
    UNIQUE KEY uk_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危化品供应商/客户管理表';

-- =============================================
-- 四、新建表：重大危险源辨识表 biz_safety_major_hazard
-- =============================================
CREATE TABLE IF NOT EXISTS biz_safety_major_hazard (
    hazard_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '危险源ID',
    hazard_code        VARCHAR(64)  NOT NULL                 COMMENT '危险源编号',
    hazard_name        VARCHAR(128) NOT NULL                 COMMENT '危险源名称',
    unit_name          VARCHAR(128)                          COMMENT '辨识单元名称',
    unit_desc          VARCHAR(500)                          COMMENT '单元描述',
    r_value            DECIMAL(18,4)                         COMMENT 'R值（辨识度）',
    critical_quantity  DECIMAL(18,4)                         COMMENT '临界量(t)',
    actual_quantity    DECIMAL(18,4)                         COMMENT '实际存在量(t)',
    ratio              DECIMAL(18,4)                         COMMENT '比值(实际/临界)',
    hazard_level       CHAR(1)                               COMMENT '危险源等级（1一级 2二级 3三级 4四级）',
    identification_basis VARCHAR(500)                       COMMENT '辨识依据',
    identification_date DATE                                 COMMENT '辨识日期',
    next_identification_date DATE                            COMMENT '下次辨识日期',
    responsible_person VARCHAR(64)                          COMMENT '责任人',
    responsible_phone  VARCHAR(20)                          COMMENT '责任人电话',
    area_id            BIGINT                                COMMENT '所在区域ID',
    area_name          VARCHAR(128)                          COMMENT '所在区域名称',
    attachment         VARCHAR(500)                          COMMENT '附件',
    enterprise_id     BIGINT                                COMMENT '企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (hazard_id),
    UNIQUE KEY uk_hazard_code (hazard_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重大危险源辨识表';

-- =============================================
-- 五、新建表：合规检查清单表 biz_safety_compliance_check
-- =============================================
CREATE TABLE IF NOT EXISTS biz_safety_compliance_check (
    check_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '检查ID',
    check_code        VARCHAR(64)  NOT NULL                 COMMENT '检查编号',
    check_name        VARCHAR(128) NOT NULL                 COMMENT '检查名称',
    material_id       BIGINT                                COMMENT '关联危化品ID',
    material_name     VARCHAR(128)                          COMMENT '关联危化品名称',
    area_id           BIGINT                                COMMENT '检查区域ID',
    area_name         VARCHAR(128)                          COMMENT '检查区域名称',
    check_type        VARCHAR(32)                           COMMENT '检查类型（1日常检查 2专项检查 3定期检查）',
    check_items       TEXT                                  COMMENT '检查项（JSON数组）',
    check_date        DATE                                  COMMENT '检查日期',
    checker           VARCHAR(64)                           COMMENT '检查人',
    check_result      CHAR(1)                               COMMENT '检查结果（1合格 2不合格 3待整改）',
    problems          TEXT                                  COMMENT '发现问题',
    rectification     TEXT                                  COMMENT '整改要求',
    rectify_deadline  DATE                                  COMMENT '整改期限',
    rectify_status    CHAR(1)      DEFAULT '0'              COMMENT '整改状态（0未整改 1整改中 2已完成）',
    attachment        VARCHAR(500)                          COMMENT '附件',
    enterprise_id     BIGINT                                COMMENT '企业ID',
    status            CHAR(1)      DEFAULT '0'              COMMENT '状态',
    del_flag          CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by         VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME                              COMMENT '创建时间',
    update_by         VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME                              COMMENT '更新时间',
    remark            VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (check_id),
    UNIQUE KEY uk_check_code (check_code),
    KEY idx_material_id (material_id),
    KEY idx_check_type (check_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危化品合规检查清单表';

-- =============================================
-- 六、编号规则
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_material_record', 'safety', '出入库单号', 'CR', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '危化品出入库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_material_record');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_material_supplier', 'safety', '供应商编号', 'GYS', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '危化品供应商编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_material_supplier');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_major_hazard', 'safety', '危险源编号', 'WX', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '重大危险源编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_major_hazard');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'safety_compliance_check', 'safety', '检查编号', 'HC', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '-', '0', 'admin', sysdate(), '合规检查编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='safety_compliance_check');

-- =============================================
-- 七、字典类型与字典数据
-- =============================================

-- 出入库类型
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('出入库类型', 'safety_record_type', '0', 'admin', NOW(), '危化品出入库记录类型');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_record_type';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '入库', '1', 'safety_record_type', 'primary', '', 'N', '0', 'admin', NOW(), ''),
(2, '出库', '2', 'safety_record_type', 'warning', '', 'N', '0', 'admin', NOW(), '');

-- 供应商类型
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('供应商类型', 'safety_supplier_type', '0', 'admin', NOW(), '危化品供应商/客户类型');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_supplier_type';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '供应商', '1', 'safety_supplier_type', 'primary', '', 'Y', '0', 'admin', NOW(), ''),
(2, '客户', '2', 'safety_supplier_type', 'success', '', 'N', '0', 'admin', NOW(), '');

-- 危险源等级
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('危险源等级', 'safety_hazard_level', '0', 'admin', NOW(), '重大危险源等级');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_hazard_level';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '一级', '1', 'safety_hazard_level', 'danger', '', 'N', '0', 'admin', NOW(), '最高级别'),
(2, '二级', '2', 'safety_hazard_level', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(3, '三级', '3', 'safety_hazard_level', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(4, '四级', '4', 'safety_hazard_level', 'info', '', 'N', '0', 'admin', NOW(), '最低级别');

-- 合规检查类型
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('合规检查类型', 'safety_compliance_type', '0', 'admin', NOW(), '危化品合规检查类型');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_compliance_type';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '日常检查', '1', 'safety_compliance_type', 'primary', '', 'Y', '0', 'admin', NOW(), ''),
(2, '专项检查', '2', 'safety_compliance_type', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(3, '定期检查', '3', 'safety_compliance_type', 'info', '', 'N', '0', 'admin', NOW(), '');

-- 合规检查结果
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('合规检查结果', 'safety_check_result', '0', 'admin', NOW(), '合规检查结果');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_check_result';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '合格', '1', 'safety_check_result', 'success', '', 'N', '0', 'admin', NOW(), ''),
(2, '不合格', '2', 'safety_check_result', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(3, '待整改', '3', 'safety_check_result', 'warning', '', 'N', '0', 'admin', NOW(), '');

-- 整改状态
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('整改状态', 'safety_rectify_status', '0', 'admin', NOW(), '整改状态');
DELETE FROM sys_dict_data WHERE dict_type = 'safety_rectify_status';
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '未整改', '0', 'safety_rectify_status', 'danger', '', 'N', '0', 'admin', NOW(), ''),
(2, '整改中', '1', 'safety_rectify_status', 'warning', '', 'N', '0', 'admin', NOW(), ''),
(3, '已完成', '2', 'safety_rectify_status', 'success', '', 'N', '0', 'admin', NOW(), '');

-- 提醒类型新增"危化品库存预警"（类型8）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 8, '库存预警', '8', 'safety_remind_type', 'danger', '', 'N', '0', 'admin', NOW(), '危化品库存预警'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_remind_type' AND dict_value = '8');

-- =============================================
-- 八、菜单权限
-- =============================================

-- 获取一企一档父菜单ID
SET @archMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '一企一档' AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0) AS tmp) LIMIT 1);

-- 出入库管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '出入库管理', @archMenuId, 7, 'materialRecord', 'safety/arch/materialRecord/index', 1, 0, 'C', '0', '0', 'safety:materialRecord:list', 'swap', 'admin', NOW(), '危化品出入库管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:materialRecord:list');
SET @recordMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:materialRecord:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '出入库查询', @recordMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:materialRecord:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:materialRecord:query');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '出入库新增', @recordMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:materialRecord:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:materialRecord:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '出入库修改', @recordMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:materialRecord:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:materialRecord:edit');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '出入库删除', @recordMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:materialRecord:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:materialRecord:remove');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '出入库导出', @recordMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:materialRecord:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:materialRecord:export');

-- 供应商/客户管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商管理', @archMenuId, 8, 'supplier', 'safety/arch/supplier/index', 1, 0, 'C', '0', '0', 'safety:supplier:list', 'peoples', 'admin', NOW(), '危化品供应商/客户管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:supplier:list');
SET @supplierMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:supplier:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商查询', @supplierMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:supplier:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:supplier:query');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商新增', @supplierMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:supplier:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:supplier:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商修改', @supplierMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:supplier:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:supplier:edit');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商删除', @supplierMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:supplier:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:supplier:remove');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商导出', @supplierMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:supplier:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:supplier:export');

-- 重大危险源辨识菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '重大危险源', @archMenuId, 9, 'majorHazard', 'safety/arch/majorHazard/index', 1, 0, 'C', '0', '0', 'safety:majorHazard:list', 'bug', 'admin', NOW(), '重大危险源辨识'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:majorHazard:list');
SET @majorHazardMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:majorHazard:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '危险源查询', @majorHazardMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:majorHazard:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:majorHazard:query');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '危险源新增', @majorHazardMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:majorHazard:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:majorHazard:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '危险源修改', @majorHazardMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:majorHazard:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:majorHazard:edit');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '危险源删除', @majorHazardMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:majorHazard:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:majorHazard:remove');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '危险源导出', @majorHazardMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:majorHazard:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:majorHazard:export');

-- 合规检查清单菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '合规检查', @archMenuId, 10, 'complianceCheck', 'safety/arch/complianceCheck/index', 1, 0, 'C', '0', '0', 'safety:complianceCheck:list', 'documentation', 'admin', NOW(), '危化品合规检查清单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:complianceCheck:list');
SET @complianceMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:complianceCheck:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '检查查询', @complianceMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:complianceCheck:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:complianceCheck:query');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '检查新增', @complianceMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:complianceCheck:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:complianceCheck:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '检查修改', @complianceMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:complianceCheck:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:complianceCheck:edit');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '检查删除', @complianceMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:complianceCheck:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:complianceCheck:remove');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '检查导出', @complianceMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:complianceCheck:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:complianceCheck:export');

-- =============================================
-- 九、验证
-- =============================================
SELECT '危化品管理升级V2完成' AS result;
