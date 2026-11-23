-- =============================================================
-- 补齐请假流程业务表：flow_data_leave_form
--
-- 背景（同步遗漏）：
--   upgrade_20261118_flow_config_data.sql 同步了请假申请表单的定义
--   （flow_form form_id=2，form_key=leave_form，table_name=flow_data_leave_form），
--   但同一批同步里只给报销/公车/出差/外出/补卡写了 CREATE TABLE，漏掉了请假流程
--   的建表语句。结果 flow_form.table_name 指向一张不存在的表。
--
--   流程台账配置页「字段」候选项的业务字段（source=main）来自：
--     FlowLedgerServiceImpl.selectLedgerFields(formId)
--   → 读取 information_schema.COLUMNS WHERE TABLE_NAME = form.tableName
--   → 表不存在时返回 0 列，页面只剩 8 个系统字段，业务表单字段全部缺失。
--
-- 影响：配置请假流程台账时选不到「请假类型/开始日期/结束日期/请假天数/请假事由」等业务字段。
--
-- 修复方式：
--   1. 建表（本脚本，幂等 CREATE TABLE IF NOT EXISTS）；
--   2. 若目标环境已有该表但缺列，走「流程管理 → 表单管理 → 数据表生成」
--      （POST /flowable/form/createTable/{formId}），后端 createOrPatchTable 会自动补列。
--
-- 列结构依据 flow_form.form_id=2 的 fields_json（15 个业务/系统字段）+ 流程运行所需的
-- 内部列（process_instance_id / create_by / create_time），与其余 flow_data_* 表保持一致。
-- =============================================================

CREATE TABLE IF NOT EXISTS `flow_data_leave_form` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `processNo` varchar(64) DEFAULT NULL COMMENT '流程编号',
  `urgency` varchar(20) DEFAULT NULL COMMENT '紧急程度',
  `signOpinion` varchar(4000) DEFAULT NULL COMMENT '签字意见',
  `printCount` int DEFAULT NULL COMMENT '打印次数',
  `procStatus` varchar(20) DEFAULT NULL COMMENT '流程状态',
  `sqr` varchar(1000) DEFAULT NULL COMMENT '申请人',
  `sqbm` varchar(1000) DEFAULT NULL COMMENT '申请部门',
  `sqrq` date DEFAULT NULL COMMENT '申请日期',
  `leave_type` varchar(40) DEFAULT NULL COMMENT '请假类型',
  `start_date` datetime DEFAULT NULL COMMENT '开始日期',
  `end_date` datetime DEFAULT NULL COMMENT '结束日期',
  `days` decimal(18,1) DEFAULT NULL COMMENT '请假天数',
  `reason` varchar(500) DEFAULT NULL COMMENT '请假事由',
  `xgfj` varchar(500) DEFAULT NULL COMMENT '相关附件',
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT '流程实例ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_title` (`title`(191)),
  KEY `idx_urgency` (`urgency`),
  KEY `idx_process_no` (`processNo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='请假申请流程数据';

-- 校验：应返回 19 列（含 id），且存在 leave_type / start_date / end_date / days / reason / xgfj
-- SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flow_data_leave_form'
--  ORDER BY ORDINAL_POSITION;
