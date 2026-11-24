-- ============================================================
-- safety_task_type 字典显示名调整（避免与 WBS 任务类型混淆）
-- 日期：2026-11-24
--
-- 背景：系统中存在两个都叫「任务类型」的字典：
--   safety_task_type   → 安全生产模块「双重预防 - 排查任务」用（日常/综合/专项/季节性/节假日排查）
--   pjm_wbs_task_type  → 项目管理模块「WBS 任务分解」用（单项/单位/分部/分项/检验批/工作项/里程碑/其他）
-- 两者业务含义完全不同，在「系统管理-字典管理」中难以区分，故将前者显示名改为「安全排查类型」。
--
-- 重要：仅修改 dict_name / remark（展示层），dict_type 编码 'safety_task_type' 保持不变，
--       前端引用 proxy.useDict('safety_task_type') 取的是编码，不受影响，功能零变更。
-- 幂等：UPDATE 语句可重复执行，结果一致。
-- ============================================================

UPDATE `sys_dict_type`
SET `dict_name` = '安全排查类型',
    `remark`    = '安全排查任务类型（日常/综合/专项/季节性/节假日排查），区别于 WBS 任务类型 pjm_wbs_task_type'
WHERE `dict_type` = 'safety_task_type';

-- 执行后需清理字典缓存，或等待缓存过期；也可在「系统管理-字典管理」点刷新。
-- Redis 清理命令（本分支 database=3）：
--   redis-cli -n 3 --scan --pattern "sys_dict*" | xargs -r -I{} redis-cli -n 3 del {}
