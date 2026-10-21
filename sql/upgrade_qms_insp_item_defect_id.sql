-- 增量迁移：检验单项实测表增加 defect_id 字段（关联缺陷代码库）
-- 用途：检验结果录入时，缺陷代码从缺陷代码库中选择，自动带出缺陷名称和缺陷等级
-- 执行方式：直接在数据库中运行此脚本

ALTER TABLE qms_insp_item ADD COLUMN defect_id BIGINT COMMENT '缺陷代码库ID（关联qms_defect_code）' AFTER item_result;
