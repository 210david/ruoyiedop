-- =============================================
-- DMS PM计划增强 - 增量SQL脚本
-- 包含：自动派工字段、任务清单结构化、偏差跟踪、Quartz定时任务
-- =============================================

-- =============================================
-- 一、PM计划表增加自动派工人员字段
-- =============================================
ALTER TABLE dms_pm_plan ADD COLUMN assignee_id BIGINT DEFAULT NULL COMMENT '默认派工人ID' AFTER auto_assign;
ALTER TABLE dms_pm_plan ADD COLUMN assignee_name VARCHAR(64) DEFAULT NULL COMMENT '默认派工人名称' AFTER assignee_id;

-- =============================================
-- 二、工单表增加任务清单和偏差原因字段
-- =============================================
ALTER TABLE dms_work_order ADD COLUMN task_checklist TEXT DEFAULT NULL COMMENT '任务清单（JSON数组，含完成状态）' AFTER spare_parts_used;
ALTER TABLE dms_work_order ADD COLUMN deviation_reason VARCHAR(500) DEFAULT NULL COMMENT '偏差原因（PM工单未按时完成时记录）' AFTER complete_remark;

-- =============================================
-- 三、注册Quartz定时任务：PM计划自动生成工单
-- 每天凌晨1点执行
-- =============================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('PM计划自动生成工单', 'DEFAULT', 'dmsTask.pmAutoGenerate', '0 0 1 * * ?', '3', '1', '0', 'admin', sysdate(), '每天凌晨1点扫描到期PM计划，提前N天自动生成工单');
