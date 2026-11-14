-- ============================================================
-- 流程请求登记表：为每个流程实例分配短数字「流程编号」
-- 对标泛微 workflow_requestbase.requestid（流程实例主键，纯数字）
-- 引擎内部仍使用 Flowable 的 UUID 实例ID，由本表映射为对外流程编号
-- ============================================================

CREATE TABLE IF NOT EXISTS `flow_request` (
  `request_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流程编号（流程请求主键，自增数字）',
  `instance_id` VARCHAR(64) NOT NULL COMMENT 'Flowable 流程实例ID（内部UUID）',
  `process_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义Key',
  `business_key` VARCHAR(64) DEFAULT NULL COMMENT '业务主键',
  `create_time` DATETIME DEFAULT NULL COMMENT '登记时间（历史数据取流程发起时间）',
  PRIMARY KEY (`request_id`),
  UNIQUE KEY `uk_instance_id` (`instance_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100001 DEFAULT CHARSET=utf8mb4 COMMENT='流程请求登记表（流程实例对外短数字编号）';

-- 回填历史流程实例：按发起时间顺序发号；可重复执行（已登记的实例自动跳过）
INSERT IGNORE INTO `flow_request` (`instance_id`, `process_definition_key`, `business_key`, `create_time`)
SELECT `ID_`, SUBSTRING_INDEX(`PROC_DEF_ID_`, ':', 1), `BUSINESS_KEY_`, `START_TIME_`
FROM `ACT_HI_PROCINST`
ORDER BY `START_TIME_`;
