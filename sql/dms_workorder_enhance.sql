-- =============================================
-- DMS 工单管理增强 - 增量SQL脚本
-- 包含：SLA超时监控、工单操作日志、SLA定时任务、AI系统参数
-- =============================================

-- =============================================
-- 一、工单表增加SLA相关字段
-- =============================================
ALTER TABLE dms_work_order ADD COLUMN sla_response_deadline DATETIME DEFAULT NULL COMMENT 'SLA响应截止时间（派工时效）' AFTER arrive_time;
ALTER TABLE dms_work_order ADD COLUMN sla_process_deadline DATETIME DEFAULT NULL COMMENT 'SLA处理截止时间（处理时效）' AFTER sla_response_deadline;
ALTER TABLE dms_work_order ADD COLUMN sla_timeout_status VARCHAR(10) DEFAULT '0' COMMENT 'SLA超时状态(0正常 1响应超时 2处理超时)' AFTER sla_process_deadline;
ALTER TABLE dms_work_order ADD COLUMN sla_escalated VARCHAR(10) DEFAULT '0' COMMENT 'SLA是否已升级(0否 1是)' AFTER sla_timeout_status;

-- =============================================
-- 二、工单操作日志表（状态流转审计）
-- =============================================
CREATE TABLE IF NOT EXISTS dms_work_order_log (
    log_id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    order_id        BIGINT       NOT NULL                COMMENT '工单ID',
    order_no        VARCHAR(64)  DEFAULT NULL            COMMENT '工单号',
    from_status     VARCHAR(10)  DEFAULT NULL            COMMENT '变更前状态',
    to_status       VARCHAR(10)  DEFAULT NULL            COMMENT '变更后状态',
    action          VARCHAR(64)  DEFAULT NULL            COMMENT '操作类型(dispatch/accept/process/complete/verify/reject/cancel/reassign/create)',
    operator_id     BIGINT       DEFAULT NULL            COMMENT '操作人ID',
    operator_name   VARCHAR(64)  DEFAULT NULL            COMMENT '操作人名称',
    remark          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (log_id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单操作日志';

-- =============================================
-- 三、注册Quartz定时任务：SLA超时检查
-- 每30分钟执行一次
-- =============================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('SLA超时检查', 'DEFAULT', 'dmsTask.slaTimeoutCheck', '0 */30 * * * ?', '3', '1', '0', 'admin', sysdate(), '每30分钟检查工单SLA超时并升级');

-- =============================================
-- 四、AI功能相关系统参数
-- =============================================
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('AI功能总开关',     'dms.ai.enabled',         'false',                            'Y', 'admin', sysdate(), 'AI功能总开关(true/false)'),
('AI大模型API地址',   'dms.ai.api_url',          '',                                 'Y', 'admin', sysdate(), '大模型API地址(如 https://api.deepseek.com/v1/chat/completions)'),
('AI大模型API Key',   'dms.ai.api_key',          '',                                 'Y', 'admin', sysdate(), '大模型API密钥'),
('AI模型名称',        'dms.ai.model_name',       'deepseek-chat',                    'Y', 'admin', sysdate(), '大模型名称(如 deepseek-chat/qwen-plus/ernie-bot)'),
('AI智能报修开关',    'dms.ai.repair_assistant', 'false',                            'Y', 'admin', sysdate(), '智能报修助手开关'),
('AI智能派工开关',    'dms.ai.dispatch_recommend','false',                            'Y', 'admin', sysdate(), '智能派工推荐开关'),
('AI知识问答开关',    'dms.ai.knowledge_qa',     'false',                             'Y', 'admin', sysdate(), '维修知识问答开关'),
('AI月度调用上限',    'dms.ai.monthly_limit',    '1000',                              'Y', 'admin', sysdate(), 'AI功能月度调用次数上限');
