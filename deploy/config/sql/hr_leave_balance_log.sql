-- ============================================================
-- 假期余额扣减明细表
-- 记录每次请假审批通过后对假期余额的扣减操作
-- ============================================================

DROP TABLE IF EXISTS `biz_hr_leave_balance_log`;
CREATE TABLE `biz_hr_leave_balance_log` (
  `log_id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `balance_id`      BIGINT       NOT NULL                COMMENT '假期余额ID（biz_hr_leave_balance.balance_id）',
  `leave_req_id`    BIGINT       NULL                    COMMENT '请假单ID（biz_hr_leave_request.leave_req_id）',
  `leave_req_no`    VARCHAR(64)  NULL                    COMMENT '请假单号',
  `employee_id`     BIGINT       NOT NULL                COMMENT '员工ID',
  `employee_name`   VARCHAR(64)  NULL                    COMMENT '员工姓名',
  `leave_type`     VARCHAR(20)  NOT NULL                COMMENT '假别',
  `year`            INT          NOT NULL                COMMENT '年度',
  `change_days`     DECIMAL(5,1) NOT NULL                COMMENT '变动天数（正数=扣减，负数=退回）',
  `before_used`     DECIMAL(5,1) NULL                    COMMENT '变动前已休天数',
  `after_used`      DECIMAL(5,1) NULL                    COMMENT '变动后已休天数',
  `before_remaining` DECIMAL(5,1) NULL                  COMMENT '变动前剩余天数',
  `after_remaining`  DECIMAL(5,1) NULL                    COMMENT '变动后剩余天数',
  `change_type`    VARCHAR(20)  NOT NULL DEFAULT 'deduct' COMMENT '变动类型：deduct=扣减 refund=退回 adjust=手动调整',
  `change_reason`  VARCHAR(200) NULL                    COMMENT '变动原因',
  `operator`       VARCHAR(64)  NULL                    COMMENT '操作人',
  `del_flag`       CHAR(1)      DEFAULT '0'             COMMENT '删除标志',
  `create_by`      VARCHAR(64)  NULL                    COMMENT '创建者',
  `create_time`    DATETIME     NULL                    COMMENT '创建时间',
  `update_by`      VARCHAR(64)  NULL                    COMMENT '更新者',
  `update_time`    DATETIME     NULL                    COMMENT '更新时间',
  `remark`         VARCHAR(500) NULL                    COMMENT '备注',
  PRIMARY KEY (`log_id`),
  KEY `idx_balance_id` (`balance_id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_leave_req_id` (`leave_req_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='假期余额扣减明细';
