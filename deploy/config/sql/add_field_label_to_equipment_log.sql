-- 设备变更日志表增加 field_label 列（变更字段中文名）
ALTER TABLE dms_equipment_log ADD COLUMN field_label VARCHAR(100) NULL COMMENT '变更字段中文名（如：设备状态、安装位置）' AFTER change_type;

-- 存量数据回填：从 change_reason 中提取字段名（change_reason 格式为 "XXX变更"）
UPDATE dms_equipment_log SET field_label = SUBSTRING(change_reason, 1, CHAR_LENGTH(change_reason) - 2) WHERE change_reason LIKE '%变更' AND field_label IS NULL;
