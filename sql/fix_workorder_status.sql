-- ============================================================
-- 修复工单状态与排产状态不一致的脏数据
-- 问题：排产已下达(status=0)，但工单状态仍为新建(status=0)
--       导致已排产工单仍出现在排产工作台左侧待排产列表
-- 修复：将存在已下达排产记录的工单状态改为已下达(1)
-- ============================================================

USE `ry-vue`;

-- 1. 查看受影响的工单（先执行此SELECT确认）
SELECT wo.work_order_id, wo.work_order_no, wo.status AS wo_status,
       s.schedule_id, s.schedule_no, s.status AS schedule_status
FROM mms_work_order wo
INNER JOIN mms_schedule_task s ON wo.work_order_id = s.work_order_id
WHERE wo.status = '0'        -- 工单状态为新建
  AND s.status = '0'         -- 排产状态为已下达
  AND s.del_flag = '0'
  AND wo.del_flag = '0';

-- 2. 修复：将这些工单状态更新为已下达(1)
UPDATE mms_work_order wo
INNER JOIN mms_schedule_task s ON wo.work_order_id = s.work_order_id
SET wo.status = '1',
    wo.update_by = 'admin',
    wo.update_time = NOW()
WHERE wo.status = '0'
  AND s.status = '0'
  AND s.del_flag = '0'
  AND wo.del_flag = '0';
