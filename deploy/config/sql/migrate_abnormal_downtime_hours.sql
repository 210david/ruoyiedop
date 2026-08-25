-- ============================================
-- 异常管理：停机时长字段从分钟改为小时
-- 数据库：ry-vue
-- ============================================

-- 1. 修改字段名：downtime_minutes → downtime_hours
ALTER TABLE mms_abnormal CHANGE COLUMN downtime_minutes downtime_hours DECIMAL(8,2) DEFAULT NULL COMMENT '实际停机时长(小时)' AFTER preventive_measure;

-- 2. 迁移已有数据（分钟转小时，保留2位小数）
UPDATE mms_abnormal SET downtime_hours = ROUND(downtime_hours / 60, 2) WHERE downtime_hours IS NOT NULL AND downtime_hours > 0;
