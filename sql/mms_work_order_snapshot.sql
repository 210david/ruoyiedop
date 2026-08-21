-- ============================================================
-- MMS 工单BOM快照 + 工艺快照表
-- 执行时间: 2026-08-20
-- 说明: 工单下达时冻结BOM/工艺副本，保证在制工单不受主数据变更影响
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. 工单BOM快照表 mms_wo_bom_snapshot
--    工单下达时从 mms_bom_detail 复制，此后只读
-- ============================================================
DROP TABLE IF EXISTS `mms_wo_bom_snapshot`;
CREATE TABLE `mms_wo_bom_snapshot` (
    `snapshot_id`      BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '快照明细ID',
    `work_order_id`    BIGINT(20)    NOT NULL                 COMMENT '工单ID',
    `bom_id`           BIGINT(20)    NOT NULL                 COMMENT '源BOM ID',
    `bom_no`           VARCHAR(50)   DEFAULT NULL             COMMENT 'BOM编号',
    `bom_version`      VARCHAR(20)   DEFAULT NULL             COMMENT 'BOM版本号',
    `seq`              INT(4)        DEFAULT 0                COMMENT '行序号',
    `material_id`      BIGINT(20)    NOT NULL                 COMMENT '子项物料ID',
    `material_code`    VARCHAR(50)   DEFAULT NULL             COMMENT '物料编码',
    `material_name`    VARCHAR(200)  DEFAULT NULL             COMMENT '物料名称',
    `spec_model`       VARCHAR(500)  DEFAULT NULL             COMMENT '规格型号',
    `unit`             VARCHAR(16)   DEFAULT NULL             COMMENT '单位',
    `usage_qty`        DECIMAL(18,6) DEFAULT 0                COMMENT '单件用量',
    `loss_rate`        DECIMAL(6,2)  DEFAULT 0                COMMENT '损耗率(%)',
    `is_key_material`  CHAR(1)       DEFAULT '0'             COMMENT '是否关键料(0否 1是)',
    `supply_type`      CHAR(1)       DEFAULT '1'             COMMENT '供应方式(1直接领料 2倒冲 3车间库存)',
    `pick_store_id`    BIGINT(20)    DEFAULT NULL             COMMENT '默认发料仓库ID',
    `pick_store_name`  VARCHAR(200)  DEFAULT NULL             COMMENT '默认发料仓库名称',
    `is_phantom`       CHAR(1)       DEFAULT '0'             COMMENT '是否虚拟件(0否 1是)',
    `del_flag`         CHAR(1)       DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
    `create_by`        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
    PRIMARY KEY (`snapshot_id`),
    KEY `idx_wo_bom_wo_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单BOM快照（下达时冻结）';


-- ============================================================
-- 2. 工单工艺快照表 mms_wo_route_snapshot
--    工单下达时从 mms_route_process 复制，此后只读
-- ============================================================
DROP TABLE IF EXISTS `mms_wo_route_snapshot`;
CREATE TABLE `mms_wo_route_snapshot` (
    `snapshot_id`      BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '快照明细ID',
    `work_order_id`    BIGINT(20)    NOT NULL                 COMMENT '工单ID',
    `route_id`         BIGINT(20)    NOT NULL                 COMMENT '源工艺路线ID',
    `route_no`         VARCHAR(50)   DEFAULT NULL             COMMENT '工艺路线编号',
    `route_version`    VARCHAR(20)   DEFAULT NULL             COMMENT '工艺路线版本号',
    `step_seq`         INT(4)        DEFAULT 0                COMMENT '工序顺序',
    `process_id`       BIGINT(20)    NOT NULL                 COMMENT '工序ID',
    `process_code`     VARCHAR(50)   DEFAULT NULL             COMMENT '工序编码',
    `process_name`     VARCHAR(200)  DEFAULT NULL             COMMENT '工序名称',
    `std_time`         DECIMAL(18,6) DEFAULT 0                COMMENT '标准工时(小时)',
    `prep_time`        DECIMAL(18,6) DEFAULT 0                COMMENT '准备时间(小时)',
    `is_key_process`   CHAR(1)       DEFAULT '0'             COMMENT '是否关键工序(0否 1是)',
    `is_outsource`     CHAR(1)       DEFAULT '0'             COMMENT '是否外协(0否 1是)',
    `del_flag`         CHAR(1)       DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
    `create_by`        VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
    PRIMARY KEY (`snapshot_id`),
    KEY `idx_wo_route_wo_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单工艺快照（下达时冻结）';
