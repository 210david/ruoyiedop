-- 给版本变更记录表添加审核意见字段
ALTER TABLE mms_route_version_log ADD COLUMN audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核意见' AFTER change_desc;
