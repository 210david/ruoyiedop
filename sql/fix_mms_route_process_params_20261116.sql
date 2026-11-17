-- 修复：工艺路线详情 / 报工 报 "Unknown column" 导致前端提示"系统繁忙"（2026-11-16）
-- 现象：mms/route/{routeId} 500，日志 Unknown column 'rp.std_days' in 'field list'
-- 原因：MmsRouteProcess 实体与 MmsRouteMapper.xml 使用工艺参数列，但 mms_route_process 表未建这些列
-- 说明：本脚本仅需执行一次；重复执行会因列已存在而报 1060，可忽略

ALTER TABLE `mms_route_process`
  ADD COLUMN `std_days`      decimal(10,2) NULL DEFAULT NULL COMMENT '标准时长(天)' AFTER `process_type`,
  ADD COLUMN `min_days`      decimal(10,2) NULL DEFAULT NULL COMMENT '最短时长(天)' AFTER `std_days`,
  ADD COLUMN `max_days`      decimal(10,2) NULL DEFAULT NULL COMMENT '最长时长(天)' AFTER `min_days`,
  ADD COLUMN `temp_min`      decimal(10,2) NULL DEFAULT NULL COMMENT '温度下限(℃)' AFTER `max_days`,
  ADD COLUMN `temp_max`      decimal(10,2) NULL DEFAULT NULL COMMENT '温度上限(℃)' AFTER `temp_min`,
  ADD COLUMN `humidity_min`  decimal(10,2) NULL DEFAULT NULL COMMENT '湿度下限(%)' AFTER `temp_max`,
  ADD COLUMN `humidity_max`  decimal(10,2) NULL DEFAULT NULL COMMENT '湿度上限(%)' AFTER `humidity_min`,
  ADD COLUMN `ccp_flag`      char(1)       NULL DEFAULT '0'    COMMENT 'CCP检验点(0否 1是)' AFTER `humidity_max`,
  ADD COLUMN `loss_type`     varchar(20)   NULL DEFAULT NULL   COMMENT '损耗类型' AFTER `ccp_flag`,
  ADD COLUMN `loss_rate_max` decimal(10,4) NULL DEFAULT NULL   COMMENT '最大损耗率' AFTER `loss_type`;

-- 报工表补生产批次字段（MmsWorkReportMapper.xml 已引用，表未建列）
ALTER TABLE `mms_work_report`
  ADD COLUMN `batch_id`   bigint      NULL DEFAULT NULL COMMENT '生产批次ID' AFTER `work_order_no`,
  ADD COLUMN `batch_code` varchar(50) NULL DEFAULT NULL COMMENT '生产批次号' AFTER `batch_id`;
