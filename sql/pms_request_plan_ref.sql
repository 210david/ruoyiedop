-- =============================================
-- 采购申请表增加关联采购计划字段
-- 业务背景：PRD 2.3.2 采购计划审批通过后，可基于计划明细一键生成采购申请
-- =============================================

ALTER TABLE pms_purchase_request
    ADD COLUMN plan_id BIGINT NULL COMMENT '关联采购计划ID' AFTER audit_opinion,
    ADD COLUMN plan_no VARCHAR(64) NULL COMMENT '关联采购计划单号' AFTER plan_id;

-- 添加索引
ALTER TABLE pms_purchase_request
    ADD KEY idx_request_plan (plan_id);
