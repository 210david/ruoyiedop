-- =============================================================
-- BI 决策支持模块 · F7 阈值预警推送（PRD v1.2）
-- 依赖：bi_all.sql（bi_metric_dict / bi_metric_snapshot 已建）
-- 内容：
--   1. bi_metric_alert 预警记录表
--   2. 注册 Quartz 任务：biMetricSnapshotTask.alertMetrics（01:10，快照后执行）
-- 幂等说明：alertMetrics 按 uk_alert 唯一键 upsert，可安全手动重跑；
--           抑制规则（24h 同级不重复推）由代码保证，重复执行不会刷屏。
-- 备注：bi_metric_alert 属统计/追溯类，豁免归属字段填充（规范 3.5.4），
--       保留 dept_id 维度列（NOT NULL DEFAULT 0=全部门，教训同快照表：
--       NULL 会使唯一索引不去重）。
-- =============================================================

-- ----------------------------
-- 1. BI指标预警记录表
-- ----------------------------
DROP TABLE IF EXISTS bi_metric_alert;
CREATE TABLE bi_metric_alert (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  metric_code     VARCHAR(64)  NOT NULL                COMMENT '指标编码',
  metric_date     DATE         NOT NULL                COMMENT '预警数据日期（快照日期）',
  dept_id         BIGINT       NOT NULL DEFAULT 0      COMMENT '部门ID（0=全部门汇总）',
  alert_level     CHAR(1)      NOT NULL                COMMENT '预警级别（1黄线 2红线）',
  metric_value    DECIMAL(20,4) DEFAULT NULL           COMMENT '触发时指标值',
  threshold_value DECIMAL(20,4) DEFAULT NULL           COMMENT '触发时阈值',
  alert_message   VARCHAR(500) DEFAULT NULL            COMMENT '预警内容',
  push_status     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '推送状态（0未推送 1已推送 2抑制未推）',
  push_time       DATETIME     DEFAULT NULL            COMMENT '最近推送时间',
  push_count      INT          NOT NULL DEFAULT 0      COMMENT '累计推送次数',
  push_remark     VARCHAR(200) DEFAULT NULL            COMMENT '最近一次未推送原因（抑制说明）',
  recover_time    DATETIME     DEFAULT NULL            COMMENT '恢复时间（值回到阈值内）',
  status          CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '预警状态（0未恢复 1已恢复）',
  create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time     DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time     DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_alert (metric_code, metric_date, dept_id, alert_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='BI指标预警记录表';

-- ----------------------------
-- 2. 注册 Quartz 预警任务（每日 01:10，快照后 10 分钟执行）
-- ----------------------------
INSERT INTO sys_job (job_name, job_group, module_key, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('BI指标阈值预警', 'DEFAULT', 'bi', 'biMetricSnapshotTask.alertMetrics', '0 10 1 * * ?', '3', '1', '0', 'admin', NOW(), '每日快照后匹配阈值并按抑制规则推送站内预警（PRD F7）');
