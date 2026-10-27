-- =====================================================
-- Fix: Create missing sys_alarm_log table
-- This table is required by the alert module but was
-- missing from the deployment SQL
-- =====================================================

CREATE TABLE IF NOT EXISTS `sys_alarm_log` (
  `id`            bigint(20)    NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
  `alarm_type`    varchar(50)   DEFAULT NULL              COMMENT 'Alarm type',
  `alarm_level`   varchar(10)   DEFAULT NULL              COMMENT 'Alarm level (INFO/WARN/ERROR)',
  `alarm_title`   varchar(200)  DEFAULT NULL              COMMENT 'Alarm title',
  `alarm_detail`  text          DEFAULT NULL              COMMENT 'Alarm detail',
  `status`        char(1)       DEFAULT '0'               COMMENT 'Status (0=unprocessed 1=processed)',
  `create_time`   datetime      DEFAULT NULL              COMMENT 'Create time',
  `handle_time`   datetime      DEFAULT NULL              COMMENT 'Handle time',
  `handle_by`     varchar(64)   DEFAULT NULL              COMMENT 'Handler',
  PRIMARY KEY (`id`),
  KEY `idx_alarm_type` (`alarm_type`),
  KEY `idx_alarm_level` (`alarm_level`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='System alarm log';
