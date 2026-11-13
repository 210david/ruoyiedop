-- ============================================================
-- 定时任务模块归属回填（补 sys_job.module_key 漏项）
-- 日期：2026-11-07
--
-- 背景：模块开关停用模块时，SysModuleServiceImpl.disableModuleJobs()
--       的过滤条件是 module_key = #{moduleKey}。module_key 为空的任务
--       永远不会随模块停用而停止，形成“模块已停、任务照跑”的幽灵任务；
--       enableModuleJobs() 同样无法恢复。
-- 规范：design/unified-development-standards.md 第 1.2 节「步骤 6：模块开关登记」
--
-- 安全性：
--   1) 仅更新 module_key 为 NULL/空的记录，不覆盖任何已有归属；
--   2) 按 invoke_target 精确匹配，不依赖 job_id（各分支库 id 不一致）；
--   3) 可重复执行，二次执行影响 0 行。
--
-- 归属判定：以 Task 类所在 Maven 模块为准；代码在公共模块但服务特定业务
--           的按功能菜单挂载的一级模块归属（如 food 批次任务归 wms）。
-- ============================================================

-- 1. 食品批次临期预警与过期自动冻结 → 仓储物流（wms）
--    代码在 com.ruoyi.food.task，功能菜单挂在「仓储管理 → 批次管理」下
UPDATE sys_job SET module_key = 'wms'
 WHERE invoke_target LIKE 'foodBatchAlertTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 2. 健康证到期提醒 → 人力资源（hr）
--    代码在 com.ruoyi.hr.task.HrCertAlertTask
UPDATE sys_job SET module_key = 'hr'
 WHERE invoke_target LIKE 'hrCertAlertTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 3. 考试状态兜底任务 → 安全生产（safety）
--    代码在 com.ruoyi.safety.task.SafetyExamTask（注意：不属 hr）
UPDATE sys_job SET module_key = 'safety'
 WHERE invoke_target LIKE 'safetyExamTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 4. 安全守则学习逾期提醒 → 安全生产（safety）
--    代码在 com.ruoyi.safety.task.SafetyRuleOverdueTask
UPDATE sys_job SET module_key = 'safety'
 WHERE invoke_target LIKE 'safetyRuleOverdueTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 5. 客诉供应商整改逾期升级提醒 → 质量管理（qms）
--    代码在 com.ruoyi.qms.task.SupplierRectifyOverdueTask
UPDATE sys_job SET module_key = 'qms'
 WHERE invoke_target LIKE 'supplierRectifyOverdueTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 6. 营销业绩月度归集 → 人力资源（hr）
--    代码在 com.ruoyi.hr.task.HrPerfAggTask（按代码模块归属，非营销 mk）
UPDATE sys_job SET module_key = 'hr'
 WHERE invoke_target LIKE 'hrPerfAggTask.%'
   AND (module_key IS NULL OR module_key = '');

-- ------------------------------------------------------------
-- 验收 1：应返回 0 行（无归属不明的定时任务）
SELECT job_id, job_name, invoke_target
  FROM sys_job
 WHERE module_key IS NULL OR module_key = '';

-- 验收 2：模块与任务数对应表，便于发现漏回填
SELECT m.module_key, m.module_name, COUNT(j.job_id) AS job_count
  FROM sys_module m
  LEFT JOIN sys_job j ON j.module_key = m.module_key
 GROUP BY m.module_key, m.module_name
 ORDER BY m.order_num;
