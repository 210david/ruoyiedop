-- =============================================
-- 质量追溯链接入：发货环节写谱系（成品批次→发货客户）
-- 1. qms_lot_genealogy 加客户列（shipment 谱系事件关联）
-- 2. 字典 qms_genealogy_source 增加 shipment=发货
-- 幂等：可重复执行
-- =============================================

-- 一、谱系表加客户列
SET @has_cid = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_lot_genealogy' AND COLUMN_NAME = 'customer_id');
SET @sql = IF(@has_cid = 0,
    'ALTER TABLE qms_lot_genealogy ADD COLUMN customer_id BIGINT COMMENT ''客户ID（发货事件关联）'' AFTER supplier_name', NULL);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_cname = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qms_lot_genealogy' AND COLUMN_NAME = 'customer_name');
SET @sql = IF(@has_cname = 0,
    'ALTER TABLE qms_lot_genealogy ADD COLUMN customer_name VARCHAR(200) COMMENT ''客户名称（发货事件关联）'' AFTER customer_id', NULL);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 二、字典：谱系来源类型增加 发货(shipment)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
SELECT 9, '发货', 'shipment', 'qms_genealogy_source', 'N', '0', 'admin', sysdate(), '发货确认自动写入（成品批次→发货客户）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'qms_genealogy_source' AND dict_value = 'shipment');
