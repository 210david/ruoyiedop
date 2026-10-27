-- =====================================================
-- P3-15: 活动报名链接功能 - 安全相关字段迁移
-- =====================================================
-- 1. mk_activity 表增加 register_token 字段（报名链接安全令牌）
-- 2. mk_activity_participant 表增加 sign_in_code 字段（签到码）

-- 活动表增加报名令牌字段
ALTER TABLE `mk_activity` ADD COLUMN `register_token` VARCHAR(64) DEFAULT NULL COMMENT '报名链接安全令牌' AFTER `del_flag`;

-- 参与人表增加签到码字段
ALTER TABLE `mk_activity_participant` ADD COLUMN `sign_in_code` VARCHAR(32) DEFAULT NULL COMMENT '签到码（报名成功后生成）' AFTER `sign_time`;

-- 为已有活动生成默认令牌（基于活动ID + UUID）
UPDATE `mk_activity` SET `register_token` = REPLACE(UUID(), '-', '') WHERE `register_token` IS NULL AND `del_flag` = '0';

-- 为签到码创建索引（签到时需要根据签到码快速查找）
CREATE INDEX `idx_sign_in_code` ON `mk_activity_participant` (`sign_in_code`);

-- 为报名去重创建联合索引（活动ID + 手机号）
CREATE INDEX `idx_activity_phone` ON `mk_activity_participant` (`activity_id`, `contact_phone`);
