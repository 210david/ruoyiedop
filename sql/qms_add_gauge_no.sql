-- 为检验任务表增加量检具编号冗余字段
ALTER TABLE qms_insp_task ADD COLUMN gauge_no VARCHAR(64) DEFAULT NULL COMMENT '量检具编号（冗余）' AFTER gauge_name;
