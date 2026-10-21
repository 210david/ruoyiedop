-- =============================================
-- CAPA台账：为 qms_capa 表增加 responsible_person_id 字段
-- 用于存储责任人用户ID（配合组织人员选择器使用）
-- =============================================

ALTER TABLE qms_capa ADD COLUMN responsible_person_id BIGINT DEFAULT NULL COMMENT '责任人用户ID' AFTER responsible_person;
