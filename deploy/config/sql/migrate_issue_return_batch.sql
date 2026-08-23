-- ============================================================
-- 批量领料/退料改造 SQL迁移脚本
-- 1. 创建领料明细表 mms_issue_detail
-- 2. 创建退料明细表 mms_return_detail
-- 3. 将现有领料数据迁移到明细表
-- 4. 将现有退料数据迁移到明细表
-- 数据库: ry-vue
-- ============================================================

-- ========== 1. 领料明细表 ==========
DROP TABLE IF EXISTS `mms_issue_detail`;
CREATE TABLE `mms_issue_detail` (
  `detail_id`      BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `issue_id`       BIGINT(20)   NOT NULL COMMENT '领料单ID',
  `seq`            INT(11)      DEFAULT 1 COMMENT '序号',
  `material_id`    BIGINT(20)   DEFAULT NULL COMMENT '物料ID',
  `material_code`  VARCHAR(64)  DEFAULT NULL COMMENT '物料编码',
  `material_name`  VARCHAR(200) DEFAULT NULL COMMENT '物料名称',
  `spec_model`     VARCHAR(200) DEFAULT NULL COMMENT '规格型号',
  `unit`           VARCHAR(20)  DEFAULT NULL COMMENT '单位',
  `issue_qty`      DECIMAL(18,4) DEFAULT 0 COMMENT '领料数量',
  `batch_no`       VARCHAR(64)  DEFAULT NULL COMMENT '批次号',
  `del_flag`       CHAR(1)      DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  `create_by`      VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_by`      VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
  `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_issue_id` (`issue_id`),
  KEY `idx_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领料明细表';

-- ========== 2. 退料明细表 ==========
DROP TABLE IF EXISTS `mms_return_detail`;
CREATE TABLE `mms_return_detail` (
  `detail_id`      BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `return_id`      BIGINT(20)   NOT NULL COMMENT '退料单ID',
  `seq`            INT(11)      DEFAULT 1 COMMENT '序号',
  `material_id`    BIGINT(20)   DEFAULT NULL COMMENT '物料ID',
  `material_code`  VARCHAR(64)  DEFAULT NULL COMMENT '物料编码',
  `material_name`  VARCHAR(200) DEFAULT NULL COMMENT '物料名称',
  `spec_model`     VARCHAR(200) DEFAULT NULL COMMENT '规格型号',
  `unit`           VARCHAR(20)  DEFAULT NULL COMMENT '单位',
  `return_qty`     DECIMAL(18,4) DEFAULT 0 COMMENT '退料数量',
  `return_reason`  VARCHAR(500) DEFAULT NULL COMMENT '退料原因',
  `del_flag`       CHAR(1)      DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  `create_by`      VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
  `create_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
  `update_by`      VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
  `update_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
  `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_return_id` (`return_id`),
  KEY `idx_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退料明细表';

-- ========== 3. 迁移现有领料数据到明细表 ==========
INSERT INTO `mms_issue_detail` (`issue_id`, `seq`, `material_id`, `material_code`, `material_name`, `spec_model`, `unit`, `issue_qty`, `batch_no`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT `issue_id`, 1, `material_id`, `material_code`, `material_name`, `spec_model`, `unit`, `issue_qty`, `batch_no`, `del_flag`, `create_by`, `create_time`, `remark`
FROM `mms_issue`
WHERE `del_flag` = '0';

-- ========== 4. 迁移现有退料数据到明细表 ==========
INSERT INTO `mms_return_detail` (`return_id`, `seq`, `material_id`, `material_code`, `material_name`, `spec_model`, `unit`, `return_qty`, `return_reason`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT `return_id`, 1, `material_id`, `material_code`, `material_name`, `spec_model`, `unit`, `return_qty`, `return_reason`, `del_flag`, `create_by`, `create_time`, `remark`
FROM `mms_return_material`
WHERE `del_flag` = '0';

-- ========== 5. 领料列表页查询视图（关联明细汇总，方便列表展示） ==========
-- 为了列表页能展示物料信息，添加汇总字段到主表查询
-- 注意：主表保留物料相关列以兼容旧数据，新数据通过明细表管理

-- ========== 6. 更新字典说明 ==========
-- 领料状态 mms_issue_status: 0=待领料, 1=已领料
-- 退料状态 mms_return_status: 0=待退料, 1=已退料
