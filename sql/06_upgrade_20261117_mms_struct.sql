-- =============================================================
-- 升级脚本: upgrade_20261117_mms_struct.sql
-- 主题: 生产管控(mms)工艺路线/报工表补列 —— 修复"打开工艺路线 500 系统繁忙"
-- 现象: mms/route/{routeId} 500, 日志 Unknown column 'rp.std_days' in 'field list'
-- 原因: MmsRouteProcess 实体与 Mapper 已引用工艺参数列, 但库表缺这些列
--       (开发库 2026-11-16 由 fix_mms_route_process_params_20261116.sql 补列,
--        该脚本未随 20261114 交付包发布, 服务器缺列)
-- 幂等: 每列先查 information_schema 再决定是否 ALTER, 重复执行无副作用
-- 目标库: ruoyi_ylsp
-- 执行: mysql -uroot -p --default-character-set=utf8mb4 ruoyi_ylsp < 本文件
-- 日期: 2026-11-17
-- =============================================================

SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 一、mms_route_process（工艺路线工序）补 10 列
-- -------------------------------------------------------------
SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `std_days` decimal(10,2) NULL DEFAULT NULL COMMENT ''标准时长(天)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='std_days');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `min_days` decimal(10,2) NULL DEFAULT NULL COMMENT ''最短时长(天)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='min_days');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `max_days` decimal(10,2) NULL DEFAULT NULL COMMENT ''最长时长(天)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='max_days');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `temp_min` decimal(10,2) NULL DEFAULT NULL COMMENT ''温度下限(℃)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='temp_min');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `temp_max` decimal(10,2) NULL DEFAULT NULL COMMENT ''温度上限(℃)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='temp_max');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `humidity_min` decimal(10,2) NULL DEFAULT NULL COMMENT ''湿度下限(%)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='humidity_min');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `humidity_max` decimal(10,2) NULL DEFAULT NULL COMMENT ''湿度上限(%)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='humidity_max');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `ccp_flag` char(1) NULL DEFAULT ''0'' COMMENT ''CCP检验点(0否 1是)''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='ccp_flag');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `loss_type` varchar(20) NULL DEFAULT NULL COMMENT ''损耗类型''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='loss_type');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_route_process` ADD COLUMN `loss_rate_max` decimal(10,4) NULL DEFAULT NULL COMMENT ''最大损耗率''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_route_process' AND column_name='loss_rate_max');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- -------------------------------------------------------------
-- 二、mms_work_report（报工）补 2 列
-- -------------------------------------------------------------
SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_work_report` ADD COLUMN `batch_id` bigint NULL DEFAULT NULL COMMENT ''生产批次ID''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_work_report' AND column_name='batch_id');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s = (SELECT IF(COUNT(*)=0,'ALTER TABLE `mms_work_report` ADD COLUMN `batch_code` varchar(50) NULL DEFAULT NULL COMMENT ''生产批次号''','DO 0') FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mms_work_report' AND column_name='batch_code');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- -------------------------------------------------------------
-- 三、验证（应返回 12）
-- -------------------------------------------------------------
SELECT CONCAT('mms_route_process 新列命中 ', COUNT(*), ' / 10') AS result
  FROM information_schema.columns
 WHERE table_schema=DATABASE() AND table_name='mms_route_process'
   AND column_name IN ('std_days','min_days','max_days','temp_min','temp_max',
                       'humidity_min','humidity_max','ccp_flag','loss_type','loss_rate_max');
SELECT CONCAT('mms_work_report 新列命中 ', COUNT(*), ' / 2') AS result
  FROM information_schema.columns
 WHERE table_schema=DATABASE() AND table_name='mms_work_report'
   AND column_name IN ('batch_id','batch_code');
