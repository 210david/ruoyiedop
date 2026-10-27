-- 工单管理增强 - 第二部分（建表+定时任务+AI参数）
-- SLA字段已在首次执行中成功添加

-- 工单操作日志表
CREATE TABLE IF NOT EXISTS dms_work_order_log (
    log_id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    order_id        BIGINT       NOT NULL                COMMENT '工单ID',
    order_no        VARCHAR(64)  DEFAULT NULL            COMMENT '工单号',
    from_status     VARCHAR(10)  DEFAULT NULL            COMMENT '变更前状态',
    to_status       VARCHAR(10)  DEFAULT NULL            COMMENT '变更后状态',
    action          VARCHAR(64)  DEFAULT NULL            COMMENT '操作类型',
    operator_id     BIGINT       DEFAULT NULL            COMMENT '操作人ID',
    operator_name   VARCHAR(64)  DEFAULT NULL            COMMENT '操作人名称',
    remark          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (log_id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单操作日志';

-- SLA超时检查定时任务
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('SLA超时检查', 'DEFAULT', 'dmsTask.slaTimeoutCheck', '0 */30 * * * ?', '3', '1', '0', 'admin', sysdate(), '每30分钟检查工单SLA超时并升级');

-- AI功能系统参数
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('AI功能总开关',     'dms.ai.enabled',         'false',         'Y', 'admin', sysdate(), 'AI功能总开关(true/false)'),
('AI大模型API地址',   'dms.ai.api_url',          '',              'Y', 'admin', sysdate(), '大模型API地址'),
('AI大模型API Key',   'dms.ai.api_key',          '',              'Y', 'admin', sysdate(), '大模型API密钥'),
('AI模型名称',        'dms.ai.model_name',       'deepseek-chat', 'Y', 'admin', sysdate(), '大模型名称'),
('AI智能报修开关',    'dms.ai.repair_assistant', 'false',         'Y', 'admin', sysdate(), '智能报修助手开关'),
('AI智能派工开关',    'dms.ai.dispatch_recommend','false',        'Y', 'admin', sysdate(), '智能派工推荐开关'),
('AI知识问答开关',    'dms.ai.knowledge_qa',     'false',         'Y', 'admin', sysdate(), '维修知识问答开关'),
('AI月度调用上限',    'dms.ai.monthly_limit',    '1000',          'Y', 'admin', sysdate(), 'AI功能月度调用次数上限');
