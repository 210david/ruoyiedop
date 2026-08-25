-- ============================================================
-- 停机记录改造脚本
-- 1. 停机时长字段：minutes(分钟/int) → hours(小时/decimal)
-- 2. 停机类型字典：删除"计划停机"选项（由停机分类字段区分计划/非计划）
-- 数据库：ry-vue
-- ============================================================

USE `ry-vue`;

-- ============================================================
-- 一、停机时长字段：分钟 → 小时
-- ============================================================

-- 1. 修改字段名和类型：minutes(int) → hours(decimal(8,2))
ALTER TABLE `mms_downtime` CHANGE COLUMN `minutes` `hours` DECIMAL(8,2) DEFAULT NULL COMMENT '停机时长(小时)' AFTER `reason`;

-- 2. 迁移已有数据（分钟转小时，保留2位小数）
UPDATE `mms_downtime` SET `hours` = ROUND(`hours` / 60, 2) WHERE `hours` IS NOT NULL AND `hours` > 0;

-- ============================================================
-- 二、停机类型字典：删除"计划停机"选项
-- 计划/非计划由停机分类(dt_category)字段区分，停机类型只保留具体原因分类
-- ============================================================

-- 删除"计划停机"选项
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'mms_downtime_type' AND `dict_value` = '0';

-- 更新字典类型备注
UPDATE `sys_dict_type` SET `remark` = '停机类型字典（故障/换型/物料/其他），计划/非计划由停机分类字段区分' WHERE `dict_type` = 'mms_downtime_type';

-- 更新已有数据中 dt_type='0' 的记录改为 '9'（其他停机），因为计划停机已删除
UPDATE `mms_downtime` SET `dt_type` = '9' WHERE `dt_type` = '0';
