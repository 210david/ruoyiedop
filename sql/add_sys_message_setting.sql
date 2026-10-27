-- =============================================
-- 消息通知设置表 sys_message_setting（2026-09-19）
-- 用途：消息中心「通知设置」按用户落库，控制顶部铃铛对各消息类型的提醒开关
-- 设计：每人一行，4 个类型开关（1系统通知/2业务预警/3审批消息/4待办事项），默认全开
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_message_setting` (
  `user_id`        BIGINT(20)   NOT NULL                COMMENT '用户ID（每人一行）',
  `notify_system`  CHAR(1)      DEFAULT '1'             COMMENT '系统通知提醒开关（1开 0关）',
  `notify_alert`   CHAR(1)      DEFAULT '1'             COMMENT '业务预警提醒开关（1开 0关）',
  `notify_audit`   CHAR(1)      DEFAULT '1'             COMMENT '审批消息提醒开关（1开 0关）',
  `notify_todo`    CHAR(1)      DEFAULT '1'             COMMENT '待办事项提醒开关（1开 0关）',
  `update_time`    DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知设置表（按用户隔离）';
