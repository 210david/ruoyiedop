-- ============================================================
-- 停机记录：移除status字段
-- 停机记录本身就是一条记录，是否恢复通过endTime是否为空即可判断
-- 数据库：ry-vue
-- ============================================================

USE `ry-vue`;

-- 1. 删除status列
ALTER TABLE `mms_downtime` DROP COLUMN `status`;

-- 2. 删除停机状态字典
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'mms_downtime_status';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'mms_downtime_status';
