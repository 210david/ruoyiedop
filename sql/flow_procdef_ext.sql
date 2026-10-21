-- ----------------------------
-- 流程定义扩展表（版本备注 / 活动版本标志）
-- 配合流程版本管理：用户可选择启用哪个版本（设为活动版本）、每个版本可填写备注说明
-- Flowable 原生表不存备注/活动版本，此表按 procdef_id 一对一扩展
-- ----------------------------
DROP TABLE IF EXISTS flow_procdef_ext;
CREATE TABLE flow_procdef_ext (
  procdef_id   varchar(64)  NOT NULL              COMMENT '流程定义ID（ACT_RE_PROCDEF.ID_）',
  proc_key     varchar(64)  DEFAULT ''            COMMENT '流程Key（ACT_RE_PROCDEF.KEY_）',
  version      int          DEFAULT NULL          COMMENT '版本号（ACT_RE_PROCDEF.VERSION_）',
  remark       varchar(500) DEFAULT ''            COMMENT '版本备注说明',
  active_flag  char(1)      DEFAULT '0'           COMMENT '是否活动版本（1当前活动版本 0非活动）',
  create_time  datetime     DEFAULT NULL          COMMENT '创建时间',
  update_time  datetime     DEFAULT NULL          COMMENT '更新时间',
  PRIMARY KEY (procdef_id),
  KEY idx_proc_key (proc_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义扩展（版本备注/活动版本）';
