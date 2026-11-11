-- ----------------------------
-- 流程节点「已查看人员」记录
-- 参考泛微 E9 流程图：点击当前审批节点，除了「未审批操作人」「已操作人员」，
-- 还会列出「已查看人员」——即已经打开过该节点任务、但还没办理的人。
--
-- 数据来源：办理人（含候选人）打开办理页时，前端调用
--   POST /flowable/process/nodeViewed/{taskId}
-- 由后端按「实例 + 节点 + 用户」去重写入，同一人重复打开只更新时间、保留首次查看时间。
--
-- 说明：这张表只做展示用途，流程流转完全不依赖它；表不存在时相关接口自动降级（不报错）。
-- ----------------------------
CREATE TABLE IF NOT EXISTS flow_node_viewer (
  viewer_id    bigint       NOT NULL AUTO_INCREMENT           COMMENT '主键',
  instance_id  varchar(64)  NOT NULL                          COMMENT '流程实例ID',
  node_id      varchar(128) NOT NULL                          COMMENT '节点ID（BPMN activityId，用户任务即 taskDefinitionKey）',
  task_id      varchar(64)  DEFAULT NULL                      COMMENT '最近一次查看的任务ID（同一节点多次办理时区分）',
  user_id      bigint       NOT NULL                          COMMENT '查看人用户ID',
  user_name    varchar(64)  DEFAULT ''                        COMMENT '查看人姓名（冗余存储，列表展示免联查）',
  view_time    datetime     DEFAULT NULL                      COMMENT '首次查看时间',
  PRIMARY KEY (viewer_id),
  UNIQUE KEY uk_inst_node_user (instance_id, node_id, user_id),
  KEY idx_inst (instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点查看记录（流程图「已查看人员」）';

-- ----------------------------
-- 回退语句（如需还原）
-- ----------------------------
-- DROP TABLE IF EXISTS flow_node_viewer;
