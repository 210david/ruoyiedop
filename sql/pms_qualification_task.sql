-- =============================================
-- 供应商资质过期检查 - 定时任务配置
-- 在 Quartz 定时任务中注册此任务
-- =============================================

-- 注册定时任务（如果不存在则插入）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '供应商资质过期检查', 'PMS', 'pmsQualificationTask.checkExpiredQualifications()', '0 0 8 * * ?', '3', '1', '0', 'admin', sysdate(), '每天早上8点检查供应商资质是否过期，自动将过期资质状态更新为已过期'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_job WHERE job_name = '供应商资质过期检查' AND invoke_target = 'pmsQualificationTask.checkExpiredQualifications()'
);

-- 验证结果
SELECT job_name, job_group, invoke_target, cron_expression, status, remark
FROM sys_job
WHERE invoke_target LIKE 'pmsQualificationTask%';
