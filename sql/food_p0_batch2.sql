-- =============================================================================
-- 食品行业批次效期能力 P0-批次二 初始化脚本（传丰环境 RUOYI_CFSP）
-- 依据《功能点与页面全景清单》P0-批次二：出入库批次化+FEFO三重校验+盘点移库批次化+临期预警闭环+mk发货校验
-- 内容：wms/mk 单据表加批次字段 + 临期预警定时任务注册
-- 边界：仅传丰CFSP环境，永不同步标准 RuoYi-Vue3
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、单据表加批次字段
-- -----------------------------------------------------------------------------

-- 1. 入库明细：关联批次 + 外部批次号（供应商批次映射）
ALTER TABLE `wms_inbound_order_detail`
  ADD COLUMN `batch_id`     bigint(20)  DEFAULT NULL COMMENT '批次ID（food_batch）' AFTER `batch_no`,
  ADD COLUMN `ext_batch_no` varchar(64) DEFAULT NULL COMMENT '外部批次号（供应商批次）' AFTER `batch_id`;

-- 2. 出库明细：关联批次 + FEFO推荐批次 + 换批原因（授权换批留痕）
ALTER TABLE `wms_outbound_order_detail`
  ADD COLUMN `batch_id`      bigint(20)   DEFAULT NULL COMMENT '批次ID（food_batch）' AFTER `batch_no`,
  ADD COLUMN `fefo_batch_no` varchar(64)  DEFAULT NULL COMMENT 'FEFO推荐批次号' AFTER `batch_id`,
  ADD COLUMN `change_reason` varchar(255) DEFAULT NULL COMMENT '换批原因（与FEFO推荐不一致时必填）' AFTER `fefo_batch_no`;

-- 3. 营销发货明细：出库携带批次
ALTER TABLE `mk_shipment_detail`
  ADD COLUMN `batch_no` varchar(64) DEFAULT NULL COMMENT '发货批次号（出库自动携带）' AFTER `unit`;

-- -----------------------------------------------------------------------------
-- 二、临期预警/过期自动冻结定时任务
-- -----------------------------------------------------------------------------

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('食品批次临期预警与过期自动冻结', 'DEFAULT', 'foodBatchAlertTask.pushAlerts()', '0 0 8 * * ?', '3', '1', '0', 'admin', sysdate(), '扫描临期批次推送消息中心，过期批次按效期策略自动冻结');

-- 验证
SELECT 'inbound_detail' AS tbl, COUNT(*) AS has_batch_id FROM information_schema.columns
 WHERE table_schema = 'RUOYI_CFSP' AND table_name = 'wms_inbound_order_detail' AND column_name = 'batch_id'
UNION ALL
SELECT 'outbound_detail', COUNT(*) FROM information_schema.columns
 WHERE table_schema = 'RUOYI_CFSP' AND table_name = 'wms_outbound_order_detail' AND column_name = 'fefo_batch_no'
UNION ALL
SELECT 'shipment_detail', COUNT(*) FROM information_schema.columns
 WHERE table_schema = 'RUOYI_CFSP' AND table_name = 'mk_shipment_detail' AND column_name = 'batch_no'
UNION ALL
SELECT 'sys_job', COUNT(*) FROM sys_job WHERE invoke_target = 'foodBatchAlertTask.pushAlerts()';
