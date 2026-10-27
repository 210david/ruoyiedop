-- 外协管理表新增"工单计划数量"字段
-- 用于记录外协单关联工单的产品计划数量，方便用户在填写外协数量时参考
USE ry-vue;

ALTER TABLE mms_outsource ADD COLUMN plan_qty DECIMAL(18,4) NULL COMMENT '工单计划数量' AFTER supplier_name;

-- 从工单表回填已有外协记录的 plan_qty
UPDATE mms_outsource o
INNER JOIN mms_work_order w ON o.work_order_id = w.work_order_id
SET o.plan_qty = w.plan_qty
WHERE o.del_flag = '0' AND o.plan_qty IS NULL;
