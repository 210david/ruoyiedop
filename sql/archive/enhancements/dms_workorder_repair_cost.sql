-- =============================================
-- DMS 工单管理增强 - 维修处理增加维修费用字段
-- 增量SQL脚本
-- =============================================

-- 工单表增加维修费用字段
ALTER TABLE dms_work_order ADD COLUMN repair_cost DECIMAL(12,2) DEFAULT NULL COMMENT '维修费用（元）' AFTER task_checklist;
