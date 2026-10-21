-- ============================================================
-- 安全培训记录表增加培训讲师ID字段（配合UserPicker组件改造）
-- ============================================================

ALTER TABLE biz_safety_training_record ADD COLUMN trainer_id BIGINT DEFAULT NULL COMMENT '培训讲师ID（关联sys_user.user_id）' AFTER plan_id;
