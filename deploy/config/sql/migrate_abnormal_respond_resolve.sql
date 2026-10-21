-- ============================================
-- 异常管理：完善响应/处理关闭字段
-- 数据库：ry-vue
-- ============================================

-- 响应阶段新增字段
ALTER TABLE mms_abnormal ADD COLUMN response_measure VARCHAR(500) DEFAULT NULL COMMENT '响应措施（初步应对措施）' AFTER response_time;
ALTER TABLE mms_abnormal ADD COLUMN impact_scope VARCHAR(200) DEFAULT NULL COMMENT '影响范围（对生产的影响描述）' AFTER response_measure;
ALTER TABLE mms_abnormal ADD COLUMN estimated_restore_time DATETIME DEFAULT NULL COMMENT '预计恢复时间' AFTER impact_scope;
ALTER TABLE mms_abnormal ADD COLUMN response_remark VARCHAR(500) DEFAULT NULL COMMENT '响应备注' AFTER estimated_restore_time;

-- 处理关闭阶段新增字段
ALTER TABLE mms_abnormal ADD COLUMN handle_method VARCHAR(200) DEFAULT NULL COMMENT '处理方式' AFTER handle_result;
ALTER TABLE mms_abnormal ADD COLUMN root_cause VARCHAR(500) DEFAULT NULL COMMENT '根本原因分析' AFTER handle_method;
ALTER TABLE mms_abnormal ADD COLUMN preventive_measure VARCHAR(500) DEFAULT NULL COMMENT '预防措施' AFTER root_cause;
ALTER TABLE mms_abnormal ADD COLUMN downtime_minutes INT DEFAULT NULL COMMENT '实际停机时长(分钟)' AFTER preventive_measure;
ALTER TABLE mms_abnormal ADD COLUMN need_pursuit CHAR(1) DEFAULT '0' COMMENT '是否需要追纠(0=否,1=是)' AFTER downtime_minutes;
