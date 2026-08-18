-- =====================================================
-- MMS 生产管控模块 - 扩展表（补全方案中缺失的表）
-- 执行时间: 2026-08-16
-- 说明: 补充工厂建模、编码规则、快照、流转卡、派工、
--       批次谱系、停机、外协、排产、OEE、日报等表
-- =====================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- =====================
-- 一、工厂建模（4级层级）
-- =====================

-- 1. 工厂
DROP TABLE IF EXISTS mms_factory;
CREATE TABLE mms_factory (
  factory_id    BIGINT(20)  NOT NULL AUTO_INCREMENT,
  factory_code  VARCHAR(50) NOT NULL,
  factory_name  VARCHAR(100) NOT NULL,
  address       VARCHAR(300) DEFAULT '',
  status        CHAR(1)      DEFAULT '0',
  tenant_id     BIGINT(20)   DEFAULT NULL,
  del_flag      CHAR(1)      DEFAULT '0',
  create_by     VARCHAR(64)  DEFAULT '',
  create_time   DATETIME     DEFAULT NULL,
  update_by     VARCHAR(64)  DEFAULT '',
  update_time   DATETIME     DEFAULT NULL,
  remark        VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (factory_id),
  UNIQUE KEY uk_factory_code (factory_code)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 2. 车间
DROP TABLE IF EXISTS mms_workshop;
CREATE TABLE mms_workshop (
  workshop_id   BIGINT(20)  NOT NULL AUTO_INCREMENT,
  workshop_code VARCHAR(50) NOT NULL,
  workshop_name VARCHAR(100) NOT NULL,
  factory_id    BIGINT(20)  NOT NULL,
  leader_user_id BIGINT(20) DEFAULT NULL,
  status        CHAR(1)     DEFAULT '0',
  del_flag      CHAR(1)     DEFAULT '0',
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  remark        VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (workshop_id),
  UNIQUE KEY uk_workshop_code (workshop_code),
  KEY idx_factory_id (factory_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 3. 产线
DROP TABLE IF EXISTS mms_line;
CREATE TABLE mms_line (
  line_id       BIGINT(20)  NOT NULL AUTO_INCREMENT,
  line_code     VARCHAR(50) NOT NULL,
  line_name     VARCHAR(100) NOT NULL,
  workshop_id   BIGINT(20)  NOT NULL,
  line_type     VARCHAR(20) DEFAULT '0',
  cycle_time    INT(6)      DEFAULT 0,
  status        CHAR(1)     DEFAULT '0',
  del_flag      CHAR(1)     DEFAULT '0',
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  remark        VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (line_id),
  UNIQUE KEY uk_line_code (line_code),
  KEY idx_workshop_id (workshop_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 4. 工位
DROP TABLE IF EXISTS mms_station;
CREATE TABLE mms_station (
  station_id    BIGINT(20)  NOT NULL AUTO_INCREMENT,
  station_code  VARCHAR(50) NOT NULL,
  station_name  VARCHAR(100) NOT NULL,
  line_id       BIGINT(20)  NOT NULL,
  station_type  VARCHAR(20) DEFAULT '0',
  status        CHAR(1)     DEFAULT '0',
  del_flag      CHAR(1)     DEFAULT '0',
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  remark        VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (station_id),
  UNIQUE KEY uk_station_code (station_code),
  KEY idx_line_id (line_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 二、编码规则引擎
-- =====================

DROP TABLE IF EXISTS mms_code_rule;
CREATE TABLE mms_code_rule (
  rule_id      BIGINT(20)  NOT NULL AUTO_INCREMENT,
  rule_key     VARCHAR(30) NOT NULL,
  rule_name    VARCHAR(100) NOT NULL,
  prefix       VARCHAR(10) NOT NULL,
  date_fmt     VARCHAR(20) DEFAULT 'yyyyMMdd',
  seq_len      INT(2)      DEFAULT 4,
  reset_cycle  VARCHAR(10) DEFAULT 'day',
  `current_seq`  BIGINT(20)  DEFAULT 0,
  `current_date` VARCHAR(10) DEFAULT '',
  status       CHAR(1)     DEFAULT '0',
  del_flag     CHAR(1)     DEFAULT '0',
  create_by    VARCHAR(64) DEFAULT '',
  create_time  DATETIME    DEFAULT NULL,
  update_by    VARCHAR(64) DEFAULT '',
  update_time  DATETIME    DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (rule_id),
  UNIQUE KEY uk_rule_key (rule_key)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 初始化编码规则
INSERT INTO mms_code_rule (rule_key, rule_name, prefix, date_fmt, seq_len, reset_cycle, status, del_flag, create_by, create_time, remark) VALUES
('WO',  '工单号',     'WO',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '生产工单编号'),
('PK',  '领料单号',   'PK',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '领料单编号'),
('DI',  '派工单号',   'DI',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '派工单编号'),
('RP',  '报工单号',   'RP',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '报工记录编号'),
('EX',  '异常单号',   'EX',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '异常记录编号'),
('DT',  '停机单号',   'DT',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '停机记录编号'),
('IN',  '入库单号',   'IN',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '完工入库编号'),
('OUT', '外协单号',   'OUT', 'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '外协单编号'),
('SCH', '排产单号',   'SCH', 'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '排产单编号'),
('DM',  '需求单号',   'DM',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '需求池编号'),
('FC',  '流转卡号',   'FC',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '流转卡编号'),
('KC',  '齐套检查号', 'KC',  'yyyyMMdd', 4, 'day',  '0', '0', 'admin', NOW(), '齐套检查编号')
ON DUPLICATE KEY UPDATE rule_name = VALUES(rule_name);

-- =====================
-- 三、工厂日历
-- =====================

DROP TABLE IF EXISTS mms_factory_calendar;
CREATE TABLE mms_factory_calendar (
  calendar_id  BIGINT(20)  NOT NULL AUTO_INCREMENT,
  work_date    DATE        NOT NULL,
  day_type     CHAR(1)     DEFAULT '0',
  shift_json   TEXT,
  avail_hours DECIMAL(10,2) DEFAULT 8.00,
  status       CHAR(1)     DEFAULT '0',
  del_flag     CHAR(1)     DEFAULT '0',
  create_by    VARCHAR(64) DEFAULT '',
  create_time  DATETIME    DEFAULT NULL,
  update_by    VARCHAR(64) DEFAULT '',
  update_time  DATETIME    DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (calendar_id),
  UNIQUE KEY uk_work_date (work_date)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 四、BOM/工艺快照表
-- =====================

DROP TABLE IF EXISTS mms_work_order_bom;
CREATE TABLE mms_work_order_bom (
  snapshot_id    BIGINT(20) NOT NULL AUTO_INCREMENT,
  work_order_id  BIGINT(20) NOT NULL,
  bom_id         BIGINT(20) NOT NULL,
  bom_no         VARCHAR(50) DEFAULT '',
  material_id    BIGINT(20) NOT NULL,
  material_code  VARCHAR(50) DEFAULT '',
  material_name  VARCHAR(100) DEFAULT '',
  spec_model     VARCHAR(100) DEFAULT '',
  unit           VARCHAR(20) DEFAULT '',
  usage_qty      DECIMAL(18,6) NOT NULL DEFAULT 0,
  loss_rate      DECIMAL(8,4) DEFAULT 0.0000,
  supply_type    CHAR(1)     DEFAULT '1',
  is_key_material CHAR(1)    DEFAULT '0',
  pick_store_id  BIGINT(20)  DEFAULT NULL,
  snapshot_version VARCHAR(10) DEFAULT '1.0',
  create_time    DATETIME    DEFAULT NULL,
  PRIMARY KEY (snapshot_id),
  KEY idx_wo_id (work_order_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS mms_work_order_routing;
CREATE TABLE mms_work_order_routing (
  snapshot_id    BIGINT(20) NOT NULL AUTO_INCREMENT,
  work_order_id  BIGINT(20) NOT NULL,
  route_id       BIGINT(20) DEFAULT NULL,
  op_seq         INT(4)     NOT NULL,
  process_id     BIGINT(20) NOT NULL,
  process_code   VARCHAR(50) DEFAULT '',
  process_name   VARCHAR(100) DEFAULT '',
  resource_id    BIGINT(20) DEFAULT NULL,
  resource_name  VARCHAR(100) DEFAULT '',
  std_time       DECIMAL(10,2) DEFAULT 0.00,
  prep_time      DECIMAL(10,2) DEFAULT 0.00,
  is_key_process CHAR(1)    DEFAULT '0',
  is_outsource   CHAR(1)    DEFAULT '0',
  report_mode    CHAR(1)    DEFAULT '1',
  is_danger      CHAR(1)    DEFAULT '0',
  qc_flag        CHAR(1)    DEFAULT '0',
  snapshot_version VARCHAR(10) DEFAULT '1.0',
  create_time    DATETIME   DEFAULT NULL,
  PRIMARY KEY (snapshot_id),
  KEY idx_wo_id (work_order_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 五、流转卡
-- =====================

DROP TABLE IF EXISTS mms_flow_card;
CREATE TABLE mms_flow_card (
  card_id      BIGINT(20)  NOT NULL AUTO_INCREMENT,
  card_code    VARCHAR(50) NOT NULL,
  work_order_id BIGINT(20) NOT NULL,
  work_order_no VARCHAR(50) DEFAULT '',
  batch_no     VARCHAR(50) DEFAULT '',
  op_seq       INT(4)     DEFAULT 0,
  process_name VARCHAR(100) DEFAULT '',
  status       CHAR(1)    DEFAULT '0',
  qr_content   TEXT,
  print_count  INT(4)     DEFAULT 0,
  del_flag     CHAR(1)    DEFAULT '0',
  create_by    VARCHAR(64) DEFAULT '',
  create_time  DATETIME   DEFAULT NULL,
  update_by    VARCHAR(64) DEFAULT '',
  update_time  DATETIME   DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (card_id),
  UNIQUE KEY uk_card_code (card_code),
  KEY idx_wo_id (work_order_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 六、派工单
-- =====================

DROP TABLE IF EXISTS mms_dispatch;
CREATE TABLE mms_dispatch (
  dispatch_id   BIGINT(20) NOT NULL AUTO_INCREMENT,
  dispatch_no   VARCHAR(50) NOT NULL,
  work_order_id BIGINT(20) NOT NULL,
  work_order_no VARCHAR(50) DEFAULT '',
  op_seq        INT(4)     NOT NULL,
  process_id    BIGINT(20) DEFAULT NULL,
  process_name  VARCHAR(100) DEFAULT '',
  resource_id   BIGINT(20) DEFAULT NULL,
  resource_name VARCHAR(100) DEFAULT '',
  team_id       BIGINT(20) DEFAULT NULL,
  team_name     VARCHAR(50) DEFAULT '',
  user_ids      VARCHAR(500) DEFAULT '',
  plan_qty      DECIMAL(18,3) NOT NULL DEFAULT 0,
  good_qty      DECIMAL(18,3) DEFAULT 0.000,
  defect_qty    DECIMAL(18,3) DEFAULT 0.000,
  plan_start    DATETIME   DEFAULT NULL,
  plan_end      DATETIME   DEFAULT NULL,
  actual_start  DATETIME   DEFAULT NULL,
  actual_end    DATETIME   DEFAULT NULL,
  status        CHAR(1)    DEFAULT '0',
  del_flag      CHAR(1)    DEFAULT '0',
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME   DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME   DEFAULT NULL,
  remark        VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (dispatch_id),
  UNIQUE KEY uk_dispatch_no (dispatch_no),
  KEY idx_wo_id (work_order_id),
  KEY idx_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 七、批次谱系
-- =====================

DROP TABLE IF EXISTS mms_batch_trace;
CREATE TABLE mms_batch_trace (
  trace_id    BIGINT(20) NOT NULL AUTO_INCREMENT,
  work_order_id BIGINT(20) NOT NULL,
  op_seq      INT(4)     NOT NULL,
  batch_out   VARCHAR(64) NOT NULL,
  batch_in    VARCHAR(64) NOT NULL,
  material_in BIGINT(20) DEFAULT NULL,
  material_code VARCHAR(50) DEFAULT '',
  qty_used     DECIMAL(18,6) NOT NULL DEFAULT 0,
  create_time  DATETIME   DEFAULT NULL,
  create_by    VARCHAR(64) DEFAULT '',
  PRIMARY KEY (trace_id),
  KEY idx_batch_out (batch_out),
  KEY idx_batch_in (batch_in),
  KEY idx_wo_id (work_order_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 八、停机记录
-- =====================

DROP TABLE IF EXISTS mms_downtime;
CREATE TABLE mms_downtime (
  downtime_id  BIGINT(20) NOT NULL AUTO_INCREMENT,
  downtime_no  VARCHAR(50) NOT NULL,
  dispatch_id  BIGINT(20) DEFAULT NULL,
  work_order_id BIGINT(20) DEFAULT NULL,
  resource_id  BIGINT(20) DEFAULT NULL,
  resource_name VARCHAR(100) DEFAULT '',
  start_time   DATETIME   NOT NULL,
  end_time     DATETIME   DEFAULT NULL,
  dt_type      VARCHAR(20) DEFAULT '0',
  reason       VARCHAR(500) DEFAULT '',
  minutes      INT(6)     DEFAULT 0,
  status       CHAR(1)    DEFAULT '0',
  del_flag     CHAR(1)    DEFAULT '0',
  create_by    VARCHAR(64) DEFAULT '',
  create_time  DATETIME   DEFAULT NULL,
  update_by    VARCHAR(64) DEFAULT '',
  update_time  DATETIME   DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (downtime_id),
  UNIQUE KEY uk_downtime_no (downtime_no)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 九、外协单
-- =====================

DROP TABLE IF EXISTS mms_outsource;
CREATE TABLE mms_outsource (
  outsource_id   BIGINT(20) NOT NULL AUTO_INCREMENT,
  outsource_no   VARCHAR(50) NOT NULL,
  work_order_id  BIGINT(20) NOT NULL,
  work_order_no  VARCHAR(50) DEFAULT '',
  op_seq         INT(4)     DEFAULT NULL,
  process_name   VARCHAR(100) DEFAULT '',
  supplier_id    BIGINT(20) DEFAULT NULL,
  supplier_name  VARCHAR(100) DEFAULT '',
  out_qty        DECIMAL(18,3) NOT NULL DEFAULT 0,
  unit_price     DECIMAL(18,4) DEFAULT 0.0000,
  total_amount   DECIMAL(18,4) DEFAULT 0.0000,
  out_date       DATE      DEFAULT NULL,
  plan_back_date DATE      DEFAULT NULL,
  actual_back_date DATE    DEFAULT NULL,
  back_qty       DECIMAL(18,3) DEFAULT 0.000,
  loss_qty       DECIMAL(18,3) DEFAULT 0.000,
  status         CHAR(1)   DEFAULT '0',
  del_flag       CHAR(1)   DEFAULT '0',
  create_by      VARCHAR(64) DEFAULT '',
  create_time    DATETIME  DEFAULT NULL,
  update_by      VARCHAR(64) DEFAULT '',
  update_time    DATETIME  DEFAULT NULL,
  remark         VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (outsource_id),
  UNIQUE KEY uk_outsource_no (outsource_no),
  KEY idx_wo_id (work_order_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 十、排产相关表
-- =====================

-- 排产单头
DROP TABLE IF EXISTS mms_schedule;
CREATE TABLE mms_schedule (
  schedule_id  BIGINT(20) NOT NULL AUTO_INCREMENT,
  schedule_no VARCHAR(50) NOT NULL,
  plan_id     BIGINT(20) DEFAULT NULL,
  strategy    CHAR(1)    DEFAULT '1',
  status      CHAR(1)    DEFAULT '0',
  del_flag    CHAR(1)    DEFAULT '0',
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME  DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME  DEFAULT NULL,
  remark      VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (schedule_id),
  UNIQUE KEY uk_schedule_no (schedule_no)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 排产任务行（甘特图条目）
DROP TABLE IF EXISTS mms_schedule_task;
CREATE TABLE mms_schedule_task (
  task_id      BIGINT(20) NOT NULL AUTO_INCREMENT,
  schedule_id  BIGINT(20) NOT NULL,
  work_order_id BIGINT(20) NOT NULL,
  work_order_no VARCHAR(50) DEFAULT '',
  resource_id   BIGINT(20) NOT NULL,
  resource_name VARCHAR(100) DEFAULT '',
  op_seq        INT(4)    DEFAULT 0,
  plan_start    DATETIME  NOT NULL,
  plan_end      DATETIME  NOT NULL,
  setup_minutes INT(6)   DEFAULT 0,
  locked        CHAR(1)   DEFAULT '0',
  status        CHAR(1)   DEFAULT '0',
  del_flag      CHAR(1)   DEFAULT '0',
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME  DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME  DEFAULT NULL,
  remark        VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (task_id),
  KEY idx_schedule_id (schedule_id),
  KEY idx_wo_id (work_order_id),
  KEY idx_resource_id (resource_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- 产能负荷快照
DROP TABLE IF EXISTS mms_capacity_load;
CREATE TABLE mms_capacity_load (
  load_id         BIGINT(20) NOT NULL AUTO_INCREMENT,
  snapshot_date   DATE      NOT NULL,
  resource_id     BIGINT(20) NOT NULL,
  resource_name   VARCHAR(100) DEFAULT '',
  calendar_hours  DECIMAL(10,2) DEFAULT 0.00,
  load_hours      DECIMAL(10,2) DEFAULT 0.00,
  load_rate       DECIMAL(8,4) DEFAULT 0.0000,
  create_time     DATETIME  DEFAULT NULL,
  PRIMARY KEY (load_id),
  KEY idx_date_resource (snapshot_date, resource_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- =====================
-- 十一、OEE快照 + 日报
-- =====================

DROP TABLE IF EXISTS mms_oee_snapshot;
CREATE TABLE mms_oee_snapshot (
  snapshot_id    BIGINT(20) NOT NULL AUTO_INCREMENT,
  snapshot_date  DATE      NOT NULL,
  resource_id    BIGINT(20) NOT NULL,
  resource_name  VARCHAR(100) DEFAULT '',
  work_order_id  BIGINT(20) DEFAULT NULL,
  work_order_no  VARCHAR(50) DEFAULT '',
  planned_hours  DECIMAL(10,2) DEFAULT 0.00,
  run_hours      DECIMAL(10,2) DEFAULT 0.00,
  downtime_hours DECIMAL(10,2) DEFAULT 0.00,
  availability   DECIMAL(8,4) DEFAULT 0.0000,
  performance    DECIMAL(8,4) DEFAULT 0.0000,
  quality_rate   DECIMAL(8,4) DEFAULT 0.0000,
  oee            DECIMAL(8,4) DEFAULT 0.0000,
  good_qty       DECIMAL(18,3) DEFAULT 0.000,
  defect_qty     DECIMAL(18,3) DEFAULT 0.000,
  create_time    DATETIME  DEFAULT NULL,
  PRIMARY KEY (snapshot_id),
  KEY idx_date_resource (snapshot_date, resource_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS mms_stat_daily;
CREATE TABLE mms_stat_daily (
  stat_id        BIGINT(20) NOT NULL AUTO_INCREMENT,
  stat_date      DATE      NOT NULL,
  work_order_count INT(6)  DEFAULT 0,
  completed_count  INT(6)  DEFAULT 0,
  in_progress_count INT(6) DEFAULT 0,
  abnormal_count   INT(6)  DEFAULT 0,
  total_plan_qty   DECIMAL(18,3) DEFAULT 0.000,
  total_good_qty   DECIMAL(18,3) DEFAULT 0.000,
  total_defect_qty DECIMAL(18,3) DEFAULT 0.000,
  avg_oee           DECIMAL(8,4) DEFAULT 0.0000,
  report_count      INT(6) DEFAULT 0,
  create_time       DATETIME DEFAULT NULL,
  PRIMARY KEY (stat_id),
  UNIQUE KEY uk_stat_date (stat_date)
) ENGINE=InnoDB AUTO_INCREMENT=1;
