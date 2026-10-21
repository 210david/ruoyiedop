-- =============================================
-- DMS 设备台账增强 - 增加资产编号、设备等级、质保期限字段
-- 增量SQL脚本
-- =============================================

-- 一、设备表增加三个字段
ALTER TABLE dms_equipment ADD COLUMN asset_code VARCHAR(128) DEFAULT NULL COMMENT '资产编号' AFTER equipment_name;
ALTER TABLE dms_equipment ADD COLUMN equipment_level VARCHAR(10) DEFAULT NULL COMMENT '设备等级（字典 dms_equipment_level）' AFTER equipment_status;
ALTER TABLE dms_equipment ADD COLUMN warranty_date DATE DEFAULT NULL COMMENT '质保期限' AFTER equipment_level;

-- 二、新增字典类型：设备等级
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('设备等级', 'dms_equipment_level', '0', 'admin', sysdate(), '设备重要等级');

-- 三、新增字典数据：设备等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '关键设备', '0', 'dms_equipment_level', '', 'danger', 'N', '0', 'admin', sysdate(), 'A类-关键设备，故障影响生产主线'),
(2, '重要设备', '1', 'dms_equipment_level', '', 'warning', 'N', '0', 'admin', sysdate(), 'B类-重要设备，故障影响部分生产'),
(3, '一般设备', '2', 'dms_equipment_level', '', 'success', 'Y', '0', 'admin', sysdate(), 'C类-一般设备，故障影响较小'),
(4, '辅助设备', '3', 'dms_equipment_level', '', 'info', 'N', '0', 'admin', sysdate(), 'D类-辅助设备，故障不影响生产');
