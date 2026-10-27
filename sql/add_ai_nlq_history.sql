-- =============================================
-- AI 数据查询历史表 ai_nlq_history（2026-09-19）
-- 用途：顶部 AI 助手「数据查询」模式的提问历史落库，与知识问答历史（ai_qa_history）同模式
-- 用户个人数据类：按 user_id 隔离，仅本人可查
-- =============================================
CREATE TABLE IF NOT EXISTS `ai_nlq_history` (
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`       BIGINT(20)   NOT NULL                COMMENT '提问用户ID',
  `user_name`     VARCHAR(64)  DEFAULT ''              COMMENT '提问用户名',
  `question`      VARCHAR(1000) NOT NULL               COMMENT '问题',
  `answer`        TEXT                                 COMMENT '回答（AI结论summary，无结论时为空）',
  `result_json`   TEXT                                 COMMENT '查询结果JSON（columns/rows/sql/emptyReason，供回放）',
  `status`        CHAR(1)      DEFAULT '0'             COMMENT '状态（0成功 1失败）',
  `create_time`   DATETIME     DEFAULT NULL            COMMENT '提问时间',
  PRIMARY KEY (`id`),
  KEY `idx_nlq_history_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI数据查询历史记录表';
