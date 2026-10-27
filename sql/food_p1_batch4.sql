-- =============================================================================
-- P1-批次四：工艺路线行业参数 + 在制品WIP台账 + 工序转移 + 物料行业参数
-- 环境：仅传丰CFSP（RUOYI_CFSP），永不同步标准版
-- 内容：
--   1) mms_route_process 增加行业工序参数（标准时长天/温湿度范围/CCP/损耗）——通用表扩展
--   2) mms_work_report 增加批次维度（报工挂批次与工序节点）
--   3) 新增行业表：food_wip / food_wip_env / food_wip_transfer / food_material_param
--   4) 配置数据：字典5项、编码规则2项、火腿物料2项、干腌火腿工艺路线模板（9道工序，已审核）、
--      物料行业参数（火腿产出率76%/批次计划模式）、菜单（在制品台账/工序转移）
-- 注意：ALTER 语句仅可执行一次
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、通用表扩展（改造）
-- -----------------------------------------------------------------------------
ALTER TABLE `mms_route_process`
  ADD COLUMN `std_days`      decimal(6,1) DEFAULT 0.0 COMMENT '标准时长(天)',
  ADD COLUMN `min_days`      decimal(6,1) DEFAULT NULL COMMENT '最短时长(天)',
  ADD COLUMN `max_days`      decimal(6,1) DEFAULT NULL COMMENT '最长时长(天)',
  ADD COLUMN `temp_min`      decimal(6,2) DEFAULT NULL COMMENT '温度下限(℃)',
  ADD COLUMN `temp_max`      decimal(6,2) DEFAULT NULL COMMENT '温度上限(℃)',
  ADD COLUMN `humidity_min`  decimal(6,2) DEFAULT NULL COMMENT '湿度下限(%)',
  ADD COLUMN `humidity_max`  decimal(6,2) DEFAULT NULL COMMENT '湿度上限(%)',
  ADD COLUMN `ccp_flag`      char(1)      DEFAULT '0'  COMMENT 'CCP检验点(0否1是)',
  ADD COLUMN `loss_type`     varchar(20)  DEFAULT NULL COMMENT '主要损耗类型(字典food_loss_type)',
  ADD COLUMN `loss_rate_max` decimal(6,2) DEFAULT NULL COMMENT '该工序最大损耗率(%)';

ALTER TABLE `mms_work_report`
  ADD COLUMN `batch_id`   bigint      DEFAULT NULL COMMENT '批次ID(food_batch)',
  ADD COLUMN `batch_code` varchar(64) DEFAULT NULL COMMENT '批次号';

-- -----------------------------------------------------------------------------
-- 二、行业表（utf8mb4_0900_ai_ci）
-- -----------------------------------------------------------------------------
-- 在制品台账（垛/批维度，长周期WIP跟踪）
CREATE TABLE IF NOT EXISTS `food_wip` (
  `wip_id`               bigint        NOT NULL AUTO_INCREMENT COMMENT '垛ID',
  `wip_no`               varchar(64)   NOT NULL COMMENT '垛号',
  `work_order_id`        bigint        DEFAULT NULL COMMENT '工单ID',
  `work_order_no`        varchar(50)   DEFAULT NULL COMMENT '工单号',
  `route_id`             bigint        DEFAULT NULL COMMENT '工艺路线ID',
  `batch_id`             bigint        DEFAULT NULL COMMENT '关联批次ID(food_batch)',
  `batch_code`           varchar(64)   DEFAULT NULL COMMENT '关联批次号',
  `material_id`          bigint        NOT NULL COMMENT '物料ID',
  `material_code`        varchar(64)   DEFAULT NULL COMMENT '物料编码',
  `material_name`        varchar(255)  DEFAULT NULL COMMENT '物料名称',
  `qty`                  decimal(18,2) DEFAULT 0.00 COMMENT '在制数量',
  `qty_unit`             varchar(20)   DEFAULT NULL COMMENT '单位(字典wms_unit)',
  `wip_location`         varchar(100)  DEFAULT NULL COMMENT '柜位/垛位',
  `current_step_seq`     int           DEFAULT 0 COMMENT '当前工序序号',
  `current_process_name` varchar(100)  DEFAULT NULL COMMENT '当前工序名称',
  `step_start_time`      datetime      DEFAULT NULL COMMENT '入柜/进入当前工序时间',
  `plan_step_end`        datetime      DEFAULT NULL COMMENT '预计出柜时间',
  `wip_status`           char(1)       DEFAULT '0' COMMENT '状态(0在制1已完成9已关闭)',
  `finish_time`          datetime      DEFAULT NULL COMMENT '完成时间',
  `del_flag`             char(1)       DEFAULT '0' COMMENT '删除标志',
  `create_by`            varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`          datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`            varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`          datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`               varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`wip_id`),
  UNIQUE KEY `uk_food_wip_no` (`wip_no`),
  KEY `idx_food_wip_wo` (`work_order_id`),
  KEY `idx_food_wip_batch` (`batch_id`),
  KEY `idx_food_wip_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品在制品台账(垛/批维度)';

-- 温湿度/上霉状态记录（台账明细，软删除）
CREATE TABLE IF NOT EXISTS `food_wip_env` (
  `env_id`       bigint      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `wip_id`       bigint      NOT NULL COMMENT '垛ID',
  `wip_no`       varchar(64) DEFAULT NULL COMMENT '垛号',
  `step_seq`     int         DEFAULT 0 COMMENT '工序序号',
  `process_name` varchar(100) DEFAULT NULL COMMENT '工序名称',
  `record_time`  datetime    NOT NULL COMMENT '记录时间',
  `temp`         decimal(6,2) DEFAULT NULL COMMENT '温度(℃)',
  `humidity`     decimal(6,2) DEFAULT NULL COMMENT '湿度(%)',
  `mold_status`  char(1)     DEFAULT NULL COMMENT '上霉状态(字典food_mold_status)',
  `recorder`     varchar(64) DEFAULT NULL COMMENT '记录人',
  `del_flag`     char(1)     DEFAULT '0' COMMENT '删除标志',
  `create_by`    varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time`  datetime    DEFAULT NULL COMMENT '创建时间',
  `remark`       varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`env_id`),
  KEY `idx_food_wip_env_wip` (`wip_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='在制品温湿度上霉记录';

-- 工序转移单（工序间流转，记录损耗，批次随转）
CREATE TABLE IF NOT EXISTS `food_wip_transfer` (
  `transfer_id`       bigint        NOT NULL AUTO_INCREMENT COMMENT '转移ID',
  `transfer_no`       varchar(50)   NOT NULL COMMENT '转移单号',
  `work_order_id`     bigint        DEFAULT NULL COMMENT '工单ID',
  `work_order_no`     varchar(50)   DEFAULT NULL COMMENT '工单号',
  `wip_id`            bigint        NOT NULL COMMENT '垛ID',
  `wip_no`            varchar(64)   DEFAULT NULL COMMENT '垛号',
  `batch_id`          bigint        DEFAULT NULL COMMENT '批次ID',
  `batch_code`        varchar(64)   DEFAULT NULL COMMENT '批次号',
  `material_id`       bigint        DEFAULT NULL COMMENT '物料ID',
  `material_code`     varchar(64)   DEFAULT NULL COMMENT '物料编码',
  `material_name`     varchar(255)  DEFAULT NULL COMMENT '物料名称',
  `from_step_seq`     int           DEFAULT 0 COMMENT '转出工序序号',
  `from_process_name` varchar(100)  DEFAULT NULL COMMENT '转出工序名称',
  `to_step_seq`       int           DEFAULT 0 COMMENT '转入工序序号',
  `to_process_name`   varchar(100)  DEFAULT NULL COMMENT '转入工序名称',
  `transfer_qty`      decimal(18,2) NOT NULL COMMENT '转移数量',
  `loss_qty`          decimal(18,2) DEFAULT 0.00 COMMENT '本次损耗数量',
  `loss_rate`         decimal(6,2)  DEFAULT NULL COMMENT '损耗率(%)',
  `loss_type`         varchar(20)   DEFAULT NULL COMMENT '损耗类型(字典food_loss_type)',
  `std_days`          decimal(6,1)  DEFAULT NULL COMMENT '转入工序标准时长(天)快照',
  `plan_step_end`     datetime      DEFAULT NULL COMMENT '预计出柜时间(按标准时长推算)',
  `transfer_time`     datetime      DEFAULT NULL COMMENT '转移时间',
  `transfer_by`       varchar(64)   DEFAULT NULL COMMENT '转移人',
  `move_no`           varchar(64)   DEFAULT NULL COMMENT '联动移库单号(wms_move_order)',
  `status`            char(1)       DEFAULT '1' COMMENT '状态(1已转移)',
  `del_flag`          char(1)       DEFAULT '0' COMMENT '删除标志',
  `create_by`         varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`       datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`         varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`       datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`            varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`transfer_id`),
  UNIQUE KEY `uk_food_wip_transfer_no` (`transfer_no`),
  KEY `idx_food_wip_tr_wip` (`wip_id`),
  KEY `idx_food_wip_tr_wo` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品工序转移单';

-- 物料行业参数（配方双模型/产出率/排产模式，配置驱动）
CREATE TABLE IF NOT EXISTS `food_material_param` (
  `param_id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  `material_id`      bigint       NOT NULL COMMENT '物料ID',
  `material_code`    varchar(64)  DEFAULT NULL COMMENT '物料编码',
  `material_name`    varchar(255) DEFAULT NULL COMMENT '物料名称',
  `yield_model`      char(1)      DEFAULT '2' COMMENT '配方模型(1产出率模型2配比配方)',
  `std_yield_rate`   decimal(6,2) DEFAULT NULL COMMENT '基准产出率(%)',
  `yield_alarm_rate` decimal(6,2) DEFAULT NULL COMMENT '产出率告警下限(%)',
  `schedule_mode`    char(1)      DEFAULT '2' COMMENT '排产模式(1批次计划2离散日排产)',
  `del_flag`         char(1)      DEFAULT '0' COMMENT '删除标志',
  `create_by`        varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`      datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`        varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time`      datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`           varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`param_id`),
  UNIQUE KEY `uk_food_mat_param` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料食品行业参数';

-- -----------------------------------------------------------------------------
-- 三、字典（幂等：先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `sys_dict_data` WHERE dict_type IN ('food_wip_status','food_mold_status','food_loss_type','food_schedule_mode','food_yield_model');
DELETE FROM `sys_dict_type` WHERE dict_type IN ('food_wip_status','food_mold_status','food_loss_type','food_schedule_mode','food_yield_model');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('在制状态',   'food_wip_status',    '0', 'admin', sysdate(), '在制品台账状态'),
('上霉状态',   'food_mold_status',   '0', 'admin', sysdate(), '干腌火腿上霉状态'),
('损耗类型',   'food_loss_type',     '0', 'admin', sysdate(), '食品生产损耗类型'),
('排产模式',   'food_schedule_mode', '0', 'admin', sysdate(), '按物料/产线配置的排产模式'),
('配方模型',   'food_yield_model',   '0', 'admin', sysdate(), '产出率模型/配比配方双模型');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(1, '在制',   '0', 'food_wip_status', '', 'warning', 'Y', '0', 'admin', sysdate()),
(2, '已完成', '1', 'food_wip_status', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '已关闭', '9', 'food_wip_status', '', 'info',    'N', '0', 'admin', sysdate()),
(1, '未上霉',   '0', 'food_mold_status', '', 'info',    'N', '0', 'admin', sysdate()),
(2, '上霉初期', '1', 'food_mold_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '上霉良好', '2', 'food_mold_status', '', 'success', 'N', '0', 'admin', sysdate()),
(4, '上霉不足', '3', 'food_mold_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(5, '上霉过重', '4', 'food_mold_status', '', 'danger',  'N', '0', 'admin', sysdate()),
(1, '修割损耗', '1', 'food_loss_type', '', 'info',    'N', '0', 'admin', sysdate()),
(2, '盐渍损耗', '2', 'food_loss_type', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '发酵失重', '3', 'food_loss_type', '', 'warning', 'N', '0', 'admin', sysdate()),
(4, '其他损耗', '4', 'food_loss_type', '', 'info',    'N', '0', 'admin', sysdate()),
(1, '批次计划模式', '1', 'food_schedule_mode', '', 'warning', 'N', '0', 'admin', sysdate()),
(2, '离散日排产',   '2', 'food_schedule_mode', '', 'info',    'Y', '0', 'admin', sysdate()),
(1, '产出率模型', '1', 'food_yield_model', '', 'warning', 'N', '0', 'admin', sysdate()),
(2, '配比配方',   '2', 'food_yield_model', '', 'info',    'N', '0', 'admin', sysdate());

-- -----------------------------------------------------------------------------
-- 四、火腿物料（幂等：编码唯一）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `wms_material` (`material_code`, `material_name`, `material_type`, `spec_model`, `unit`, `is_batch_manage`, `is_expiry_manage`, `shelf_life_days`, `expiry_alert_days`, `status`, `del_flag`, `create_by`, `create_time`, `remark`) VALUES
('MAT010', '鲜猪腿（原料腿）',   '0', '带骨鲜腿', '3', '1', '1', 180, 30, '0', '0', 'admin', sysdate(), '干腌火腿原料'),
('MAT011', '干腌火腿（整腿）',   '2', '整只',     '3', '1', '1', 365, 60, '0', '0', 'admin', sysdate(), 'P1产出率模型演示成品');

-- -----------------------------------------------------------------------------
-- 五、干腌火腿工艺路线模板（9道工序，直接置为已审核；幂等：按编号先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `mms_route_version_log` WHERE route_no = 'RT-HAM-01';
DELETE FROM `mms_route_audit_log`   WHERE route_id IN (SELECT route_id FROM (SELECT route_id FROM `mms_route` WHERE route_no = 'RT-HAM-01') t);
DELETE FROM `mms_route_process`     WHERE route_id IN (SELECT route_id FROM (SELECT route_id FROM `mms_route` WHERE route_no = 'RT-HAM-01') t);
DELETE FROM `mms_route`             WHERE route_no = 'RT-HAM-01';

DELETE FROM `mms_process` WHERE process_code LIKE 'HX%';
INSERT INTO `mms_process` (`process_code`, `process_name`, `process_type`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `workshop_name`, `status`, `del_flag`, `create_by`, `create_time`, `remark`) VALUES
('HX01', '原料腿验收',   '0', 24.00,   0.00, '1', '0', '火腿车间', '0', '0', 'admin', sysdate(), 'CCP1：动物检疫合格证明索证'),
('HX02', '修割整形',     '0', 24.00,   0.00, '0', '0', '火腿车间', '0', '0', 'admin', sysdate(), '修割损耗主要环节'),
('HX03', '三次上盐腌制', '0', 384.00,  0.00, '1', '0', '腌制间',   '0', '0', 'admin', sysdate(), 'CCP2：盐量比例控制，12-20天'),
('HX04', '洗晒整形',     '0', 48.00,   0.00, '0', '0', '晾晒场',   '0', '0', 'admin', sysdate(), ''),
('HX05', '上挂低温发酵', '0', 1440.00, 0.00, '0', '0', '发酵间',   '0', '0', 'admin', sysdate(), '45-75天低温发酵'),
('HX06', '熟化分级',     '0', 2160.00, 0.00, '0', '0', '熟化房',   '0', '0', 'admin', sysdate(), '75-120天熟化'),
('HX07', '检验分级',     '0', 48.00,   0.00, '1', '0', '化验室',   '0', '0', 'admin', sysdate(), 'CCP3：出厂检验'),
('HX08', '分割包装',     '0', 24.00,   0.00, '1', '0', '包装间',   '0', '0', 'admin', sysdate(), 'CCP4：金属探测'),
('HX09', '成品入库',     '0', 24.00,   0.00, '0', '0', '成品仓',   '0', '0', 'admin', sysdate(), '');

INSERT INTO `mms_route` (`route_no`, `route_name`, `product_id`, `product_code`, `product_name`, `version`, `effective_date`, `status`, `audit_by`, `audit_time`, `audit_remark`, `total_std_time`, `process_count`, `del_flag`, `create_by`, `create_time`, `remark`)
VALUES ('RT-HAM-01', '干腌火腿标准工艺路线', (SELECT material_id FROM `wms_material` WHERE material_code = 'MAT011'), 'MAT011', '干腌火腿（整腿）', '1.0', CURDATE(), '2', 'admin', sysdate(), 'P1预置模板直接启用', 4176.00, 9, '0', 'admin', sysdate(), '干腌火腿工序链：验收→修割→腌制→洗晒→发酵→熟化→检验→包装→入库');

SET @route_id = (SELECT route_id FROM `mms_route` WHERE route_no = 'RT-HAM-01' LIMIT 1);

-- 9道工序参数（标准时长/温湿度范围/CCP/损耗阈值均为配置数据，传丰参数仅为数据）
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 10, 24.00, 0.00, '1', '0', 1.0, 1.0, 1.0, 0.00, 4.00, NULL, NULL, '1', NULL, NULL, '0', 'admin', sysdate(), 'CCP1：动物检疫合格证明索证' FROM `mms_process` p WHERE p.process_code = 'HX01';
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 20, 24.00, 0.00, '0', '0', 1.0, 1.0, 2.0, 8.00, 12.00, NULL, NULL, '0', '1', 8.00, '0', 'admin', sysdate(), '修割损耗主要环节' FROM `mms_process` p WHERE p.process_code = 'HX02';
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 30, 384.00, 0.00, '1', '0', 16.0, 12.0, 20.0, 0.00, 4.00, 75.00, 90.00, '1', '2', 5.00, '0', 'admin', sysdate(), 'CCP2：盐量比例控制' FROM `mms_process` p WHERE p.process_code = 'HX03';
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 40, 48.00, 0.00, '0', '0', 2.0, 2.0, 3.0, 15.00, 25.00, 50.00, 70.00, '0', '4', 1.00, '0', 'admin', sysdate(), '' FROM `mms_process` p WHERE p.process_code = 'HX04';
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 50, 1440.00, 0.00, '0', '0', 60.0, 45.0, 75.0, 8.00, 12.00, 70.00, 85.00, '0', '3', 12.00, '0', 'admin', sysdate(), '低温发酵' FROM `mms_process` p WHERE p.process_code = 'HX05';
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 60, 2160.00, 0.00, '0', '0', 90.0, 75.0, 120.0, 12.00, 18.00, 60.00, 70.00, '0', '3', 5.00, '0', 'admin', sysdate(), '熟化' FROM `mms_process` p WHERE p.process_code = 'HX06';
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 70, 48.00, 0.00, '1', '0', 2.0, 2.0, 3.0, 0.00, 8.00, NULL, NULL, '1', NULL, NULL, '0', 'admin', sysdate(), 'CCP3：出厂检验' FROM `mms_process` p WHERE p.process_code = 'HX07';
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 80, 24.00, 0.00, '1', '0', 1.0, 1.0, 1.0, 0.00, 12.00, NULL, NULL, '1', '4', 1.00, '0', 'admin', sysdate(), 'CCP4：金属探测' FROM `mms_process` p WHERE p.process_code = 'HX08';
INSERT INTO `mms_route_process` (`route_id`, `process_id`, `process_code`, `process_name`, `process_type`, `step_seq`, `std_time`, `prep_time`, `is_key_process`, `is_outsource`, `std_days`, `min_days`, `max_days`, `temp_min`, `temp_max`, `humidity_min`, `humidity_max`, `ccp_flag`, `loss_type`, `loss_rate_max`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT @route_id, p.process_id, p.process_code, p.process_name, '0', 90, 24.00, 0.00, '0', '0', 1.0, 1.0, 1.0, NULL, NULL, NULL, NULL, '0', NULL, NULL, '0', 'admin', sysdate(), '' FROM `mms_process` p WHERE p.process_code = 'HX09';

-- -----------------------------------------------------------------------------
-- 六、物料行业参数（火腿=产出率模型76%/批次计划；火腿肠=配比配方/日排产；幂等）
-- -----------------------------------------------------------------------------
DELETE FROM `food_material_param` WHERE material_id IN (
  SELECT material_id FROM (SELECT material_id FROM `wms_material` WHERE material_code IN ('MAT002','MAT011')) t);
INSERT INTO `food_material_param` (`material_id`, `material_code`, `material_name`, `yield_model`, `std_yield_rate`, `yield_alarm_rate`, `schedule_mode`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT m.material_id, m.material_code, m.material_name, '1', 76.00, 70.00, '1', '0', 'admin', sysdate(), '干腌火腿：投入鲜腿→产出成品，基准产出率76%'
FROM `wms_material` m WHERE m.material_code = 'MAT011';
INSERT INTO `food_material_param` (`material_id`, `material_code`, `material_name`, `yield_model`, `std_yield_rate`, `yield_alarm_rate`, `schedule_mode`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT m.material_id, m.material_code, m.material_name, '2', NULL, NULL, '2', '0', 'admin', sysdate(), '火腿肠：BOM配比配方，离散日排产'
FROM `wms_material` m WHERE m.material_code = 'MAT002';

-- -----------------------------------------------------------------------------
-- 七、菜单：在制品台账 / 工序转移（生产执行 5233 下；幂等：先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `sys_menu` WHERE menu_id BETWEEN 9062160 AND 9062179;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (9062160, '在制品台账', 5233, 6, 'wip', 'food/wip/index', '', '', 1, 0, 'C', '0', '0', 'food:wip:list', 'monitor', 'admin', sysdate(), '长周期WIP按垛/批跟踪：工序位置、入柜出柜、温湿度、上霉状态');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(9062161, '台账查询',     9062160, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wip:query',   '#', 'admin', sysdate(), ''),
(9062162, '台账新增建垛', 9062160, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wip:add',     '#', 'admin', sysdate(), ''),
(9062163, '台账修改',     9062160, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wip:edit',    '#', 'admin', sysdate(), ''),
(9062164, '台账删除',     9062160, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wip:remove',  '#', 'admin', sysdate(), ''),
(9062165, '温湿度记录',   9062160, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wip:env',     '#', 'admin', sysdate(), ''),
(9062166, '台账导出',     9062160, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wip:export',  '#', 'admin', sysdate(), '');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (9062170, '工序转移', 5233, 7, 'wipTransfer', 'food/wipTransfer/index', '', '', 1, 0, 'C', '0', '0', 'food:wipTransfer:list', 'swap', 'admin', sysdate(), '工序间流转单据：转移留痕、损耗记录、批次随转');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(9062171, '转移查询', 9062170, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wipTransfer:query',   '#', 'admin', sysdate(), ''),
(9062172, '转移新增', 9062170, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wipTransfer:add',     '#', 'admin', sysdate(), ''),
(9062174, '转移导出', 9062170, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:wipTransfer:export',  '#', 'admin', sysdate(), '');

-- 八、补充按钮：产出率核算（完工分析 5345 下）、物料行业参数（物料管理 2010 下）；幂等：先删后插
DELETE FROM `sys_menu` WHERE menu_id BETWEEN 9062180 AND 9062189;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(9062180, '产出率核算查询', 5345, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:yield:list',   '#', 'admin', sysdate(), '干腌火腿产出率核算（在完工分析页内）'),
(9062181, '产出率核算导出', 5345, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:yield:export', '#', 'admin', sysdate(), ''),
(9062185, '行业参数查询',   2010, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:param:query',  '#', 'admin', sysdate(), '查看物料食品行业参数（产出率模型/排产模式）'),
(9062186, '行业参数维护',   2010, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:param:edit',   '#', 'admin', sysdate(), '维护物料食品行业参数（在物料管理页内）'),
(9062187, '行业参数列表',   2010, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:param:list',   '#', 'admin', sysdate(), '列表查询行业参数（排产工作台双模式标识使用）');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.menu_id FROM `sys_menu` m WHERE m.menu_id BETWEEN 9062160 AND 9062189;
