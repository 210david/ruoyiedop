-- ============================================================
-- MMS 生产管控模块 - 完整建表 + 菜单 + 字典 + 编号规则
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、主数据表
-- ============================================================

-- 1. BOM/物料清单主表
DROP TABLE IF EXISTS `mms_bom`;
CREATE TABLE `mms_bom` (
  `bom_id`          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT 'BOM ID',
  `bom_no`          VARCHAR(50)   NOT NULL                 COMMENT 'BOM编号',
  `bom_name`        VARCHAR(200)  NOT NULL                 COMMENT 'BOM名称',
  `product_id`      BIGINT(20)    NOT NULL                 COMMENT '成品/主产品物料ID',
  `product_code`    VARCHAR(50)   DEFAULT ''               COMMENT '产品编码',
  `product_name`    VARCHAR(100)  DEFAULT ''               COMMENT '产品名称',
  `bom_type`        VARCHAR(20)   DEFAULT '0'              COMMENT '清单类型(0标准BOM 1替代BOM 2配方 3临时BOM)',
  `version`         VARCHAR(10)   DEFAULT '1.0'            COMMENT '版本号',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0启用 1停用)',
  `effective_date`  DATE          DEFAULT NULL             COMMENT '生效日期',
  `expire_date`     DATE          DEFAULT NULL             COMMENT '失效日期',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志(0存在 2删除)',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`bom_id`),
  UNIQUE KEY `uk_bom_no` (`bom_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='BOM物料清单主表';

-- 2. BOM明细表
DROP TABLE IF EXISTS `mms_bom_detail`;
CREATE TABLE `mms_bom_detail` (
  `detail_id`       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `bom_id`          BIGINT(20)    NOT NULL                 COMMENT 'BOM ID',
  `material_id`     BIGINT(20)    NOT NULL                 COMMENT '子项物料ID',
  `material_code`   VARCHAR(50)   DEFAULT ''               COMMENT '物料编码',
  `material_name`   VARCHAR(100)  DEFAULT ''               COMMENT '物料名称',
  `spec_model`      VARCHAR(100)  DEFAULT ''               COMMENT '规格型号',
  `unit`            VARCHAR(20)   DEFAULT ''               COMMENT '单位',
  `usage_qty`       DECIMAL(18,6) NOT NULL DEFAULT 0       COMMENT '单件用量',
  `loss_rate`       DECIMAL(6,2)  DEFAULT 0.00             COMMENT '损耗率(%)',
  `is_key_material` CHAR(1)       DEFAULT '0'              COMMENT '是否关键料(0否 1是)',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_bom_id` (`bom_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='BOM物料清单明细表';

-- 3. 工艺路线主表
DROP TABLE IF EXISTS `mms_route`;
CREATE TABLE `mms_route` (
  `route_id`        BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '工艺路线ID',
  `route_no`        VARCHAR(50)   NOT NULL                 COMMENT '路线编号',
  `route_name`      VARCHAR(200)  NOT NULL                 COMMENT '路线名称',
  `product_id`      BIGINT(20)    NOT NULL                 COMMENT '产品ID',
  `product_code`    VARCHAR(50)   DEFAULT ''               COMMENT '产品编码',
  `product_name`    VARCHAR(100)  DEFAULT ''               COMMENT '产品名称',
  `version`         VARCHAR(10)   DEFAULT '1.0'            COMMENT '版本号',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0启用 1停用)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`route_id`),
  UNIQUE KEY `uk_route_no` (`route_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='工艺路线主表';

-- 4. 工序定义表
DROP TABLE IF EXISTS `mms_process`;
CREATE TABLE `mms_process` (
  `process_id`      BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '工序ID',
  `process_code`    VARCHAR(50)   NOT NULL                 COMMENT '工序编码',
  `process_name`    VARCHAR(100)  NOT NULL                 COMMENT '工序名称',
  `process_type`    VARCHAR(20)   DEFAULT '0'              COMMENT '工序类型(0加工 1装配 2检验 3包装)',
  `std_time`        DECIMAL(10,2) DEFAULT 0.00            COMMENT '标准工时(小时)',
  `prep_time`       DECIMAL(10,2) DEFAULT 0.00            COMMENT '准备时间(小时)',
  `is_key_process`  CHAR(1)       DEFAULT '0'              COMMENT '是否关键工序(0否 1是)',
  `is_outsource`    CHAR(1)       DEFAULT '0'              COMMENT '是否外协(0否 1是)',
  `workshop_name`   VARCHAR(100)  DEFAULT ''               COMMENT '所属车间',
  `line_name`       VARCHAR(100)  DEFAULT ''               COMMENT '所属产线',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0启用 1停用)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`process_id`),
  UNIQUE KEY `uk_process_code` (`process_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='工序定义表';

-- 5. 工艺路线工序关联表
DROP TABLE IF EXISTS `mms_route_process`;
CREATE TABLE `mms_route_process` (
  `id`              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `route_id`        BIGINT(20)    NOT NULL                 COMMENT '工艺路线ID',
  `process_id`      BIGINT(20)    NOT NULL                 COMMENT '工序ID',
  `process_code`    VARCHAR(50)   DEFAULT ''               COMMENT '工序编码',
  `process_name`    VARCHAR(100)  DEFAULT ''               COMMENT '工序名称',
  `step_seq`        INT(4)        NOT NULL                 COMMENT '工序顺序',
  `std_time`        DECIMAL(10,2) DEFAULT 0.00            COMMENT '标准工时',
  `is_key_process`  CHAR(1)       DEFAULT '0'              COMMENT '是否关键工序',
  `is_outsource`    CHAR(1)       DEFAULT '0'              COMMENT '是否外协',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_route_id` (`route_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='工艺路线工序关联表';

-- 6. 产能单元表
DROP TABLE IF EXISTS `mms_resource`;
CREATE TABLE `mms_resource` (
  `resource_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '产能单元ID',
  `resource_code`   VARCHAR(50)   NOT NULL                 COMMENT '产能单元编码',
  `resource_name`   VARCHAR(100)  NOT NULL                 COMMENT '产能单元名称',
  `resource_type`   VARCHAR(20)   DEFAULT '0'              COMMENT '类型(0单设备 1产线 2设备组)',
  `rate`            DECIMAL(18,3) DEFAULT 0.000            COMMENT '额定速率',
  `rate_unit`       VARCHAR(20)   DEFAULT '件/h'           COMMENT '速率单位',
  `batch_min`       DECIMAL(18,3) DEFAULT 0.000            COMMENT '最小批量',
  `batch_max`       DECIMAL(18,3) DEFAULT 0.000            COMMENT '最大批量',
  `efficiency`      DECIMAL(6,2)  DEFAULT 100.00           COMMENT '效率系数(%)',
  `workshop_name`   VARCHAR(100)  DEFAULT ''               COMMENT '所属车间',
  `line_name`       VARCHAR(100)  DEFAULT ''               COMMENT '所属产线',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0启用 1停用)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`resource_id`),
  UNIQUE KEY `uk_resource_code` (`resource_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='产能单元表';

-- 7. 班次定义表
DROP TABLE IF EXISTS `mms_shift`;
CREATE TABLE `mms_shift` (
  `shift_id`        BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '班次ID',
  `shift_name`      VARCHAR(50)   NOT NULL                 COMMENT '班次名称',
  `start_time`      VARCHAR(10)   NOT NULL                 COMMENT '开始时间(HH:mm)',
  `end_time`        VARCHAR(10)   NOT NULL                 COMMENT '结束时间(HH:mm)',
  `rest_minutes`    INT(4)        DEFAULT 0                COMMENT '休息时长(分钟)',
  `avail_hours`     DECIMAL(10,2) DEFAULT 8.00            COMMENT '可用工时',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0启用 1停用)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
PRIMARY KEY (`shift_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='班次定义表';

-- ============================================================
-- 二、业务单据表
-- ============================================================

-- 8. 生产需求表 (PS-01)
DROP TABLE IF EXISTS `mms_demand`;
CREATE TABLE `mms_demand` (
  `demand_id`       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '需求ID',
  `demand_no`       VARCHAR(50)   NOT NULL                 COMMENT '需求单号',
  `demand_type`     VARCHAR(20)   DEFAULT '0'              COMMENT '需求类型(0订单 1预测 2补库 3插单)',
  `source_order_no` VARCHAR(50)   DEFAULT ''               COMMENT '来源订单号',
  `product_id`      BIGINT(20)    NOT NULL                 COMMENT '产品ID',
  `product_code`    VARCHAR(50)   DEFAULT ''               COMMENT '产品编码',
  `product_name`    VARCHAR(100)  DEFAULT ''               COMMENT '产品名称',
  `spec_model`      VARCHAR(100)  DEFAULT ''               COMMENT '规格型号',
  `unit`            VARCHAR(20)   DEFAULT ''               COMMENT '单位',
  `demand_qty`      DECIMAL(18,3) NOT NULL                 COMMENT '需求数量',
  `require_date`    DATE          NOT NULL                 COMMENT '需求交期',
  `priority`       VARCHAR(10)   DEFAULT '1'              COMMENT '优先级(0高 1中 2低)',
  `customer_id`    BIGINT(20)    DEFAULT NULL             COMMENT '客户ID',
  `customer_no`    VARCHAR(50)   DEFAULT ''               COMMENT '客户编号',
  `customer_name`  VARCHAR(100)  DEFAULT ''               COMMENT '客户名称',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0草稿 1已确认 2已排产 3已取消)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`demand_id`),
  UNIQUE KEY `uk_demand_no` (`demand_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='生产需求表';

-- 9. 主生产计划表 (PS-02/PS-03)
DROP TABLE IF EXISTS `mms_mps`;
CREATE TABLE `mms_mps` (
  `mps_id`          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主计划ID',
  `mps_no`          VARCHAR(50)   NOT NULL                 COMMENT '主计划号',
  `demand_id`       BIGINT(20)    DEFAULT NULL             COMMENT '关联需求ID',
  `demand_no`       VARCHAR(50)   DEFAULT ''               COMMENT '关联需求号',
  `product_id`      BIGINT(20)    NOT NULL                 COMMENT '产品ID',
  `product_code`    VARCHAR(50)   DEFAULT ''               COMMENT '产品编码',
  `product_name`    VARCHAR(100)  DEFAULT ''               COMMENT '产品名称',
  `spec_model`      VARCHAR(100)  DEFAULT ''               COMMENT '规格型号',
  `unit`            VARCHAR(20)   DEFAULT ''               COMMENT '单位',
  `plan_qty`        DECIMAL(18,3) NOT NULL                 COMMENT '计划产量',
  `period_start`    DATE          NOT NULL                 COMMENT '计划期起始',
  `period_end`      DATE          NOT NULL                 COMMENT '计划期结束',
  `resource_id`     BIGINT(20)    DEFAULT NULL             COMMENT '产能单元ID',
  `resource_name`   VARCHAR(100)  DEFAULT ''               COMMENT '产能单元名称',
  `priority`       VARCHAR(10)   DEFAULT '1'              COMMENT '优先级',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0草稿 1已确认 2已发布 3已取消)',
  `audit_by`       VARCHAR(64)   DEFAULT ''               COMMENT '审批人',
  `audit_time`     DATETIME      DEFAULT NULL             COMMENT '审批时间',
  `audit_opinion`  VARCHAR(500)  DEFAULT NULL             COMMENT '审批意见',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`mps_id`),
  UNIQUE KEY `uk_mps_no` (`mps_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='主生产计划表';

-- 10. 物料齐套检查表 (PS-03/WO-04)
DROP TABLE IF EXISTS `mms_kit_check`;
CREATE TABLE `mms_kit_check` (
  `kit_id`          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '齐套检查ID',
  `kit_no`          VARCHAR(50)   NOT NULL                 COMMENT '齐套检查单号',
  `work_order_id`   BIGINT(20)    DEFAULT NULL             COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   DEFAULT ''               COMMENT '工单号',
  `mps_id`          BIGINT(20)    DEFAULT NULL             COMMENT '主计划ID',
  `mps_no`          VARCHAR(50)   DEFAULT ''               COMMENT '主计划号',
  `bom_id`          BIGINT(20)    DEFAULT NULL             COMMENT 'BOM ID',
  `check_time`      DATETIME      DEFAULT NULL             COMMENT '检查时间',
  `kit_rate`        DECIMAL(10,2) DEFAULT 0.00            COMMENT '齐套率(%)',
  `is_complete`     CHAR(1)       DEFAULT '0'              COMMENT '是否齐套(0否 1是)',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0待检查 1已通过 2缺料)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`kit_id`),
  UNIQUE KEY `uk_kit_no` (`kit_no`),
  KEY `idx_is_complete` (`is_complete`),
  KEY `idx_status` (`status`),
  KEY `idx_work_order_no` (`work_order_no`),
  KEY `idx_mps_no` (`mps_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='物料齐套检查表';

-- 11. 齐套检查明细表
DROP TABLE IF EXISTS `mms_kit_check_detail`;
CREATE TABLE `mms_kit_check_detail` (
  `detail_id`       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `kit_id`          BIGINT(20)    NOT NULL                 COMMENT '齐套检查ID',
  `material_id`     BIGINT(20)    NOT NULL                 COMMENT '物料ID',
  `material_code`   VARCHAR(50)   DEFAULT ''               COMMENT '物料编码',
  `material_name`   VARCHAR(100)  DEFAULT ''               COMMENT '物料名称',
  `spec_model`      VARCHAR(100)  DEFAULT ''               COMMENT '规格型号',
  `unit`            VARCHAR(20)   DEFAULT ''               COMMENT '单位',
  `required_qty`    DECIMAL(18,3) NOT NULL                 COMMENT '需求数量',
  `available_qty`   DECIMAL(18,3) DEFAULT 0.000            COMMENT '可用数量',
  `shortage_qty`    DECIMAL(18,3) DEFAULT 0.000            COMMENT '缺口量',
  `is_complete`     CHAR(1)       DEFAULT '0'              COMMENT '是否齐套',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_kit_id` (`kit_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='齐套检查明细表';

-- 12. 生产工单表 (WO-01)
DROP TABLE IF EXISTS `mms_work_order`;
CREATE TABLE `mms_work_order` (
  `work_order_id`   BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   NOT NULL                 COMMENT '工单号',
  `order_type`      VARCHAR(20)   DEFAULT '0'              COMMENT '工单类型(0生产 1返工 2外协)',
  `mps_id`          BIGINT(20)    DEFAULT NULL             COMMENT '关联主计划ID',
  `mps_no`          VARCHAR(50)   DEFAULT ''               COMMENT '关联主计划号',
  `demand_no`       VARCHAR(50)   DEFAULT ''               COMMENT '关联需求号',
  `product_id`      BIGINT(20)    NOT NULL                 COMMENT '产品ID',
  `product_code`    VARCHAR(50)   DEFAULT ''               COMMENT '产品编码',
  `product_name`    VARCHAR(100)  DEFAULT ''               COMMENT '产品名称',
  `spec_model`      VARCHAR(100)  DEFAULT ''               COMMENT '规格型号',
  `unit`            VARCHAR(20)   DEFAULT ''               COMMENT '单位',
  `plan_qty`        DECIMAL(18,3) NOT NULL                 COMMENT '计划数量',
  `bom_id`          BIGINT(20)    DEFAULT NULL             COMMENT 'BOM ID',
  `bom_no`          VARCHAR(50)   DEFAULT ''               COMMENT 'BOM编号',
  `route_id`        BIGINT(20)    DEFAULT NULL             COMMENT '工艺路线ID',
  `route_no`        VARCHAR(50)   DEFAULT ''               COMMENT '工艺路线编号',
  `resource_id`     BIGINT(20)    DEFAULT NULL             COMMENT '产能单元ID',
  `resource_name`   VARCHAR(100)  DEFAULT ''               COMMENT '产能单元名称',
  `plan_start`      DATETIME      DEFAULT NULL             COMMENT '计划开工',
  `plan_finish`     DATETIME      DEFAULT NULL             COMMENT '计划完工',
  `actual_start`    DATETIME      DEFAULT NULL             COMMENT '实际开工',
  `actual_finish`   DATETIME      DEFAULT NULL             COMMENT '实际完工',
  `finished_qty`    DECIMAL(18,3) DEFAULT 0.000            COMMENT '完工数量',
  `qualified_qty`   DECIMAL(18,3) DEFAULT 0.000            COMMENT '合格数量',
  `defect_qty`      DECIMAL(18,3) DEFAULT 0.000            COMMENT '不良数量',
  `priority`       VARCHAR(10)   DEFAULT '1'              COMMENT '优先级(0高 1中 2低)',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0新建 1已下达 2执行中 3已完工 4已关闭 5已暂停 6已作废)',
  `audit_by`       VARCHAR(64)   DEFAULT ''               COMMENT '审批人',
  `audit_time`     DATETIME      DEFAULT NULL             COMMENT '审批时间',
  `audit_opinion`  VARCHAR(500)  DEFAULT NULL             COMMENT '审批意见',
  `pause_reason`   VARCHAR(200)  DEFAULT ''               COMMENT '暂停原因',
  `close_remark`    VARCHAR(500)  DEFAULT ''               COMMENT '关闭说明',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`work_order_id`),
  UNIQUE KEY `uk_work_order_no` (`work_order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='生产工单表';

-- 13. 工单审核日志表
DROP TABLE IF EXISTS `mms_work_order_audit_log`;
CREATE TABLE `mms_work_order_audit_log` (
  `log_id`          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `work_order_id`   BIGINT(20)    NOT NULL                 COMMENT '工单ID',
  `audit_by`        VARCHAR(64)   DEFAULT ''               COMMENT '审核人',
  `audit_action`    VARCHAR(10)   DEFAULT ''               COMMENT '审核动作(1下达 2暂停 3恢复 4完工 5关闭 6作废)',
  `audit_remark`    VARCHAR(500)  DEFAULT NULL             COMMENT '审核意见',
  `audit_time`      DATETIME      DEFAULT NULL             COMMENT '审核时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='工单审核日志表';

-- 14. 领料单表 (EX-01)
DROP TABLE IF EXISTS `mms_issue`;
CREATE TABLE `mms_issue` (
  `issue_id`        BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '领料ID',
  `issue_no`        VARCHAR(50)   NOT NULL                 COMMENT '领料单号',
  `work_order_id`   BIGINT(20)    NOT NULL                 COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   DEFAULT ''               COMMENT '工单号',
  `material_id`     BIGINT(20)    NOT NULL                 COMMENT '物料ID',
  `material_code`   VARCHAR(50)   DEFAULT ''               COMMENT '物料编码',
  `material_name`   VARCHAR(100)  DEFAULT ''               COMMENT '物料名称',
  `spec_model`      VARCHAR(100)  DEFAULT ''               COMMENT '规格型号',
  `unit`            VARCHAR(20)   DEFAULT ''               COMMENT '单位',
  `issue_qty`       DECIMAL(18,3) NOT NULL                 COMMENT '领料数量',
  `batch_no`        VARCHAR(50)   DEFAULT ''               COMMENT '批次号',
  `issue_time`      DATETIME      DEFAULT NULL             COMMENT '领料时间',
  `issue_by`        VARCHAR(64)   DEFAULT ''               COMMENT '领料人',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0待领料 1已领料 2已退料)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`issue_id`),
  UNIQUE KEY `uk_issue_no` (`issue_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='领料单表';

-- 15. 退料单表 (EX-02)
DROP TABLE IF EXISTS `mms_return_material`;
CREATE TABLE `mms_return_material` (
  `return_id`       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '退料ID',
  `return_no`       VARCHAR(50)   NOT NULL                 COMMENT '退料单号',
  `work_order_id`   BIGINT(20)    NOT NULL                 COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   DEFAULT ''               COMMENT '工单号',
  `material_id`     BIGINT(20)    NOT NULL                 COMMENT '物料ID',
  `material_code`   VARCHAR(50)   DEFAULT ''               COMMENT '物料编码',
  `material_name`   VARCHAR(100)  DEFAULT ''               COMMENT '物料名称',
  `spec_model`      VARCHAR(100)  DEFAULT ''               COMMENT '规格型号',
  `unit`            VARCHAR(20)   DEFAULT ''               COMMENT '单位',
  `return_qty`      DECIMAL(18,3) NOT NULL                 COMMENT '退料数量',
  `return_reason`   VARCHAR(200)  DEFAULT ''               COMMENT '退料原因',
  `return_time`     DATETIME      DEFAULT NULL             COMMENT '退料时间',
  `return_by`       VARCHAR(64)   DEFAULT ''               COMMENT '退料人',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0待退料 1已退料)',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`return_id`),
  UNIQUE KEY `uk_return_no` (`return_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='退料单表';

-- 16. 报工记录表 (EX-03)
DROP TABLE IF EXISTS `mms_work_report`;
CREATE TABLE `mms_work_report` (
  `report_id`       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '报工ID',
  `report_no`       VARCHAR(50)   NOT NULL                 COMMENT '报工单号',
  `work_order_id`   BIGINT(20)    NOT NULL                 COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   DEFAULT ''               COMMENT '工单号',
  `process_id`      BIGINT(20)    DEFAULT NULL             COMMENT '工序ID',
  `process_name`    VARCHAR(100)  DEFAULT ''               COMMENT '工序名称',
  `resource_id`     BIGINT(20)    DEFAULT NULL             COMMENT '产能单元ID',
  `resource_name`   VARCHAR(100)  DEFAULT ''               COMMENT '产能单元名称',
  `good_qty`        DECIMAL(18,3) NOT NULL DEFAULT 0       COMMENT '合格数',
  `defect_qty`      DECIMAL(18,3) DEFAULT 0.000            COMMENT '不良数',
  `work_hours`      DECIMAL(10,2) DEFAULT 0.00            COMMENT '工时',
  `report_time`     DATETIME      DEFAULT NULL             COMMENT '报工时间',
  `report_by`       VARCHAR(64)   DEFAULT ''               COMMENT '报工人',
  `team_name`       VARCHAR(50)   DEFAULT ''               COMMENT '班组',
  `shift_name`      VARCHAR(50)   DEFAULT ''               COMMENT '班次',
  `is_first_piece`  CHAR(1)       DEFAULT '0'              COMMENT '是否首件(0否 1是)',
  `status`          VARCHAR(10)   DEFAULT '0'              COMMENT '状态(0待审核 1已审核 2已驳回)',
  `audit_by`        VARCHAR(64)   DEFAULT ''               COMMENT '审核人',
  `audit_time`      DATETIME      DEFAULT NULL             COMMENT '审核时间',
  `audit_remark`   VARCHAR(500)  DEFAULT ''               COMMENT '审核意见',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `uk_report_no` (`report_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='报工记录表';

-- 17. 过程质检记录表 (EX-04)
DROP TABLE IF EXISTS `mms_qc`;
CREATE TABLE `mms_qc` (
  `qc_id`           BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '质检ID',
  `qc_no`           VARCHAR(50)   NOT NULL                 COMMENT '质检单号',
  `work_order_id`   BIGINT(20)    NOT NULL                 COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   DEFAULT ''               COMMENT '工单号',
  `process_id`      BIGINT(20)    DEFAULT NULL             COMMENT '工序ID',
  `process_name`    VARCHAR(100)  DEFAULT ''               COMMENT '工序名称',
  `qc_type`         VARCHAR(20)   DEFAULT '0'              COMMENT '检验类型(0首件 1巡检 2末件)',
  `inspect_qty`     INT(4)        NOT NULL DEFAULT 1       COMMENT '检验数量',
  `defect_qty`      INT(4)        DEFAULT 0                COMMENT '不良数量',
  `scrap_qty`       INT(4)        DEFAULT 0                COMMENT '报废数量',
  `defect_type`     VARCHAR(100)  DEFAULT ''               COMMENT '缺陷类型',
  `qc_result`       VARCHAR(20)   DEFAULT '0'              COMMENT '检验结论(0合格 1让步 2拒收)',
  `qc_by`           VARCHAR(64)   DEFAULT ''               COMMENT '检验人',
  `qc_time`         DATETIME      DEFAULT NULL             COMMENT '检验时间',
  `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  `del_flag`        CHAR(1)       DEFAULT '0'              COMMENT '删除标志',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`qc_id`),
  UNIQUE KEY `uk_qc_no` (`qc_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='过程质检记录表';

-- 18. 异常上报表 (EX-06)
DROP TABLE IF EXISTS `mms_abnormal`;
CREATE TABLE `mms_abnormal` (
  `abnormal_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '异常ID',
  `abnormal_no`     VARCHAR(50)   NOT NULL                 COMMENT '异常单号',
  `work_order_id`   BIGINT(20)    DEFAULT NULL             COMMENT '工单ID',
  `work_order_no`   VARCHAR(50)   DEFAULT ''               COMMENT '工单号',
  `resource_id`     BIGINT(20)    DEFAULT NULL             COMMENT '产能单元ID',
  `resource_name`   VARCHAR(100)  DEFAULT ''               COMMENT '产能单元名称',
  `abnormal_type`   VARCHAR(30)   NOT NULL                 COMMENT '异常类型(0设备 1物料 2质量 3安全 4其他)',
  `severity`        VARCHAR(10)   DEFAULT '0'              COMMENT '严重等级(0一般 1严重 2紧急)',
  `description`    TEXT                                   COMMENT '异常描述',
  `report_by`       VARCHAR(64)   DEFAULT ''               COMMENT '上报人',
  `report_time`     DATETIME      DEFAULT NULL             COMMENT '上报时间',
  `response_by`     VARCHAR(64)   DEFAULT ''               COMMENT '响应人',
  `response_time`   DATETIME      DEFAULT NULL             COMMENT '响应时间',
  `