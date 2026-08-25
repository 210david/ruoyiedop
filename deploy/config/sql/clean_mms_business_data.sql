-- ============================================================
-- 清除生产管控(mms)模块所有业务数据（保留基础数据）
-- 基础数据保留的表：
--   mms_bom, mms_bom_detail (BOM管理)
--   mms_route, mms_route_process, mms_route_version_log, mms_route_audit_log (工艺路线)
--   mms_process (工序管理)
--   mms_resource (资源管理)
--   mms_shift (班次管理)
--   mms_team (班组管理)
--   mms_factory_calendar (工厂日历)
--   mms_factory, mms_line, mms_station, mms_workshop (车间产线)
--   mms_code_rule (编码规则)
--   mk_number_rule, mk_number_rule_prefix (编码规则跨模块)
-- ============================================================
USE `ry-vue`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 工单管理相关（先删快照和日志，再删主表）
-- ============================================================
DELETE FROM mms_wo_bom_snapshot;        -- 工单BOM快照
DELETE FROM mms_wo_route_snapshot;      -- 工单工艺快照
DELETE FROM mms_work_order_audit_log;   -- 工单审核日志
DELETE FROM mms_work_order;             -- 工单主表
DELETE FROM mms_work_order_bom;         -- 旧表(已废弃)
DELETE FROM mms_work_order_routing;     -- 旧表(已废弃)

-- ============================================================
-- 2. 派工管理
-- ============================================================
DELETE FROM mms_dispatch;

-- ============================================================
-- 3. 齐套管理
-- ============================================================
DELETE FROM mms_kit_check_detail;       -- 齐套明细
DELETE FROM mms_kit_check;              -- 齐套主表

-- ============================================================
-- 4. 领料/退料管理
-- ============================================================
DELETE FROM mms_issue_detail;           -- 领料明细
DELETE FROM mms_issue;                  -- 领料主表
DELETE FROM mms_return_detail;          -- 退料明细
DELETE FROM mms_return_material;        -- 退料主表

-- ============================================================
-- 6. 报工管理
-- ============================================================
DELETE FROM mms_work_report;

-- ============================================================
-- 7. 质量检验
-- ============================================================
DELETE FROM mms_qc;

-- ============================================================
-- 8. 异常管理
-- ============================================================
DELETE FROM mms_abnormal;

-- ============================================================
-- 9. 停机记录
-- ============================================================
DELETE FROM mms_downtime;

-- ============================================================
-- 10. 完工入库
-- ============================================================
DELETE FROM mms_finish_receipt;

-- ============================================================
-- 11. 需求管理
-- ============================================================
DELETE FROM mms_demand;

-- ============================================================
-- 12. 主生产计划
-- ============================================================
DELETE FROM mms_mps_audit_log;          -- MPS审核日志
DELETE FROM mms_mps;                    -- MPS主表

-- ============================================================
-- 13. 排产管理
-- ============================================================
DELETE FROM mms_schedule_task;          -- 排产任务
DELETE FROM mms_schedule;               -- 旧表(已废弃)

-- ============================================================
-- 14. 产能负荷（计算生成的数据）
-- ============================================================
DELETE FROM mms_capacity_load;

-- ============================================================
-- 15. 外协管理
-- ============================================================
DELETE FROM mms_outsource;

-- ============================================================
-- 16. 分析报表相关（统计/快照数据）
-- ============================================================
DELETE FROM mms_oee_snapshot;           -- OEE快照
DELETE FROM mms_stat_daily;             -- 生产日报
DELETE FROM mms_batch_trace;            -- 批次追溯

-- ============================================================
-- 17. 重置自增ID
-- ============================================================
ALTER TABLE mms_wo_bom_snapshot AUTO_INCREMENT = 1;
ALTER TABLE mms_wo_route_snapshot AUTO_INCREMENT = 1;
ALTER TABLE mms_work_order_audit_log AUTO_INCREMENT = 1;
ALTER TABLE mms_work_order AUTO_INCREMENT = 1;
ALTER TABLE mms_work_order_bom AUTO_INCREMENT = 1;
ALTER TABLE mms_work_order_routing AUTO_INCREMENT = 1;
ALTER TABLE mms_dispatch AUTO_INCREMENT = 1;
ALTER TABLE mms_kit_check_detail AUTO_INCREMENT = 1;
ALTER TABLE mms_kit_check AUTO_INCREMENT = 1;
ALTER TABLE mms_issue_detail AUTO_INCREMENT = 1;
ALTER TABLE mms_issue AUTO_INCREMENT = 1;
ALTER TABLE mms_return_detail AUTO_INCREMENT = 1;
ALTER TABLE mms_return_material AUTO_INCREMENT = 1;
ALTER TABLE mms_work_report AUTO_INCREMENT = 1;
ALTER TABLE mms_qc AUTO_INCREMENT = 1;
ALTER TABLE mms_abnormal AUTO_INCREMENT = 1;
ALTER TABLE mms_downtime AUTO_INCREMENT = 1;
ALTER TABLE mms_finish_receipt AUTO_INCREMENT = 1;
ALTER TABLE mms_demand AUTO_INCREMENT = 1;
ALTER TABLE mms_mps_audit_log AUTO_INCREMENT = 1;
ALTER TABLE mms_mps AUTO_INCREMENT = 1;
ALTER TABLE mms_schedule_task AUTO_INCREMENT = 1;
ALTER TABLE mms_schedule AUTO_INCREMENT = 1;
ALTER TABLE mms_capacity_load AUTO_INCREMENT = 1;
ALTER TABLE mms_outsource AUTO_INCREMENT = 1;
ALTER TABLE mms_oee_snapshot AUTO_INCREMENT = 1;
ALTER TABLE mms_stat_daily AUTO_INCREMENT = 1;
ALTER TABLE mms_batch_trace AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 18. 验证清除结果
-- ============================================================
SELECT TABLE_NAME, TABLE_ROWS AS remaining_rows
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ry-vue' AND TABLE_NAME LIKE 'mms_%'
AND TABLE_NAME NOT IN (
  -- 基础数据表（保留）
  'mms_bom', 'mms_bom_detail',
  'mms_route', 'mms_route_process', 'mms_route_version_log', 'mms_route_audit_log',
  'mms_process', 'mms_resource', 'mms_shift', 'mms_team',
  'mms_factory_calendar', 'mms_factory', 'mms_line', 'mms_station', 'mms_workshop',
  'mms_code_rule'
)
ORDER BY TABLE_NAME;
