-- =====================================================
-- 修复: biz_safety_hazard 表缺少审批相关字段
-- 错误现象: 隐患管理菜单提示"系统繁忙，请稍后重试"
-- 根因: Unknown column 'h.approve_person_id' in 'field list'
-- =====================================================

USE `ry-vue`;

-- 添加审批人ID
ALTER TABLE `biz_safety_hazard` ADD COLUMN `approve_person_id` BIGINT COMMENT '审批人ID' AFTER `verify_comment`;

-- 添加审批人
ALTER TABLE `biz_safety_hazard` ADD COLUMN `approve_person` VARCHAR(64) COMMENT '审批人' AFTER `approve_person_id`;

-- 添加审批时间
ALTER TABLE `biz_safety_hazard` ADD COLUMN `approve_time` DATETIME COMMENT '审批时间' AFTER `approve_person`;

-- 添加审批结果
ALTER TABLE `biz_safety_hazard` ADD COLUMN `approve_result` VARCHAR(10) COMMENT '审批结果' AFTER `approve_time`;

-- 添加审批意见
ALTER TABLE `biz_safety_hazard` ADD COLUMN `approve_comment` TEXT COMMENT '审批意见' AFTER `approve_result`;
