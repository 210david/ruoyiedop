-- =============================================
-- DMS 点检巡检模块增强 - 增量SQL脚本
-- =============================================

-- 1. 巡检路线表增加点检项序号支持（inspection_items已支持JSON，无需改表结构）
--    JSON格式升级为：[{"item":"检查温度","type":"check","order":1},{"item":"记录压力","type":"number","unit":"MPa","order":2}]

-- 2. 点检任务表增加照片字段
ALTER TABLE dms_inspection_task ADD COLUMN photo_urls TEXT DEFAULT NULL COMMENT '点检照片URL（JSON数组）' AFTER result_data;

-- 3. 注册定时任务：自动生成点检任务（每天凌晨1点）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('点检任务自动生成', 'DEFAULT', 'dmsTask.inspectionAutoGenerate', '0 0 1 * * ?', '3', '1', '0', 'admin', sysdate(), '每天凌晨1点根据巡检路线周期自动生成点检任务');

-- 4. 注册定时任务：逾期点检任务标记（每天凌晨2点）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('点检任务逾期标记', 'DEFAULT', 'dmsTask.inspectionOverdueCheck', '0 0 2 * * ?', '3', '1', '0', 'admin', sysdate(), '每天凌晨2点将过期未完成的点检任务标记为逾期');
