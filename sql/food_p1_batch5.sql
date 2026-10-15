-- =============================================================================
-- P1-批次五：HACCP CCP电子台账 + 检验索证索票放行前置 + 不合格品批次联动
-- 环境：仅传丰CFSP（RUOYI_CFSP），永不同步标准版
-- 内容：
--   1) 新增行业表：food_ccp_template / food_ccp_record / food_insp_cert
--   2) 配置数据：字典4项、系统参数2项（索证前置开关+必传证件类型）、
--      干腌火腿CCP模板种子（取自 mms_route_process RT-HAM-01 实际关键限值）、菜单
-- 设计要点：CCP记录不可物理删除（无删除接口）；超限自动联动不合格品控制（NCR）+批次冻结
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、行业表（utf8mb4_0900_ai_ci）
-- -----------------------------------------------------------------------------
-- CCP控制点模板（配置驱动：关键限值/监控频率/纠正措施，按工艺路线+工序可配）
CREATE TABLE IF NOT EXISTS `food_ccp_template` (
  `template_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name`     varchar(100) DEFAULT NULL COMMENT '模板名称',
  `route_id`          bigint       DEFAULT NULL COMMENT '工艺路线ID(mms_route)',
  `route_name`        varchar(100) DEFAULT NULL COMMENT '工艺路线名称',
  `process_name`      varchar(100) DEFAULT NULL COMMENT '工序名称',
  `ccp_name`          varchar(100) NOT NULL COMMENT '控制点名称',
  `monitor_type`      char(1)      NOT NULL DEFAULT '1' COMMENT '监控项类型(字典food_ccp_monitor_type)',
  `limit_type`        char(1)      NOT NULL DEFAULT '1' COMMENT '限值类型(字典food_ccp_limit_type)',
  `limit_min`         decimal(10,2) DEFAULT NULL COMMENT '限值下限',
  `limit_max`         decimal(10,2) DEFAULT NULL COMMENT '限值上限',
  `limit_value`       varchar(50)  DEFAULT NULL COMMENT '枚举限值(如上霉状态字典值)',
  `unit`              varchar(20)  DEFAULT NULL COMMENT '单位',
  `monitor_freq`      varchar(50)  DEFAULT NULL COMMENT '监控频率',
  `corrective_action` varchar(500) DEFAULT NULL COMMENT '纠正措施',
  `status`            char(1)      DEFAULT '0' COMMENT '状态(0正常1停用)',
  `del_flag`          char(1)      DEFAULT '0' COMMENT '删除标志',
  `create_by`         varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`       datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`         varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time`       datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`            varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`template_id`),
  KEY `idx_food_ccp_tpl_route` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='HACCP CCP控制点模板';

-- CCP监控记录（不可物理删除：仅逻辑删除，超限自动联动NCR+批次冻结）
CREATE TABLE IF NOT EXISTS `food_ccp_record` (
  `record_id`       bigint        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `record_no`       varchar(50)   NOT NULL COMMENT '记录编号(CCP+yyyyMMdd+4位)',
  `template_id`     bigint        NOT NULL COMMENT '模板ID',
  `ccp_name`        varchar(100)  DEFAULT NULL COMMENT '控制点名称(快照)',
  `monitor_type`    char(1)       DEFAULT NULL COMMENT '监控项类型(快照)',
  `route_id`        bigint        DEFAULT NULL COMMENT '工艺路线ID',
  `process_name`    varchar(100)  DEFAULT NULL COMMENT '工序名称',
  `work_order_no`   varchar(50)   DEFAULT NULL COMMENT '工单号',
  `wip_id`          bigint        DEFAULT NULL COMMENT '垛ID(food_wip)',
  `wip_no`          varchar(64)   DEFAULT NULL COMMENT '垛号',
  `batch_id`        bigint        DEFAULT NULL COMMENT '批次ID(food_batch)',
  `batch_code`      varchar(64)   DEFAULT NULL COMMENT '批次号',
  `material_id`     bigint        DEFAULT NULL COMMENT '物料ID',
  `material_code`   varchar(64)   DEFAULT NULL COMMENT '物料编码',
  `material_name`   varchar(255)  DEFAULT NULL COMMENT '物料名称',
  `record_time`     datetime      NOT NULL COMMENT '记录时间',
  `temp_value`      decimal(6,2)  DEFAULT NULL COMMENT '温度(℃)',
  `humidity_value`  decimal(6,2)  DEFAULT NULL COMMENT '湿度(%)',
  `salt_rate`       decimal(6,2)  DEFAULT NULL COMMENT '盐量比例(%)',
  `mold_status`     char(1)       DEFAULT NULL COMMENT '上霉状态(字典food_mold_status)',
  `monitor_value`   decimal(10,2) DEFAULT NULL COMMENT '其他监控值',
  `limit_snapshot`  varchar(100)  DEFAULT NULL COMMENT '关键限值快照',
  `judge_result`    char(1)       DEFAULT '0' COMMENT '判定结果(字典food_ccp_judge: 0合格1超限)',
  `over_note`       varchar(500)  DEFAULT NULL COMMENT '偏差/超限说明',
  `corrective`      varchar(500)  DEFAULT NULL COMMENT '纠正措施',
  `ncr_id`          bigint        DEFAULT NULL COMMENT '联动NCR ID(qms_ncr)',
  `ncr_no`          varchar(50)   DEFAULT NULL COMMENT '联动NCR编号',
  `recorder`        varchar(64)   DEFAULT NULL COMMENT '记录人',
  `del_flag`        char(1)       DEFAULT '0' COMMENT '删除标志',
  `create_by`       varchar(64)   DEFAULT '' COMMENT '创建者',
  `create_time`     datetime      DEFAULT NULL COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
  `remark`          varchar(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_food_ccp_record_no` (`record_no`),
  KEY `idx_food_ccp_rec_tpl` (`template_id`),
  KEY `idx_food_ccp_rec_batch` (`batch_id`),
  KEY `idx_food_ccp_rec_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='HACCP CCP监控记录';

-- 检验任务索证索票附件（放行前置：动物检疫合格证明/检验报告必传）
CREATE TABLE IF NOT EXISTS `food_insp_cert` (
  `cert_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `task_id`       bigint       NOT NULL COMMENT '检验任务ID(qms_insp_task)',
  `task_no`       varchar(50)  DEFAULT NULL COMMENT '检验任务编号',
  `batch_no`      varchar(64)  DEFAULT NULL COMMENT '批次号',
  `cert_type`     char(1)      NOT NULL COMMENT '证件类型(字典food_cert_type)',
  `cert_no`       varchar(100) DEFAULT NULL COMMENT '证件编号',
  `file_name`     varchar(255) NOT NULL COMMENT '文件名称',
  `file_url`      varchar(500) NOT NULL COMMENT '文件地址',
  `upload_by`     varchar(64)  DEFAULT NULL COMMENT '上传人',
  `upload_time`   datetime     DEFAULT NULL COMMENT '上传时间',
  `del_flag`      char(1)      DEFAULT '0' COMMENT '删除标志',
  `create_by`     varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`cert_id`),
  KEY `idx_food_cert_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验索证索票附件';

-- -----------------------------------------------------------------------------
-- 二、字典（幂等：先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `sys_dict_data` WHERE dict_type IN ('food_ccp_monitor_type','food_ccp_limit_type','food_ccp_judge','food_cert_type');
DELETE FROM `sys_dict_type` WHERE dict_type IN ('food_ccp_monitor_type','food_ccp_limit_type','food_ccp_judge','food_cert_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('CCP监控项类型', 'food_ccp_monitor_type', '0', 'admin', sysdate(), 'HACCP关键控制点监控项类型'),
('CCP限值类型',   'food_ccp_limit_type',   '0', 'admin', sysdate(), '关键限值校验方式'),
('CCP判定结果',   'food_ccp_judge',        '0', 'admin', sysdate(), 'CCP记录判定结果'),
('检验索证类型',  'food_cert_type',        '0', 'admin', sysdate(), '索证索票附件证件类型');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '温度',     '1', 'food_ccp_monitor_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(2, '湿度',     '2', 'food_ccp_monitor_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '盐量比例', '3', 'food_ccp_monitor_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(4, '上霉状态', '4', 'food_ccp_monitor_type', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(5, '其他',     '5', 'food_ccp_monitor_type', '', 'info',    'N', '0', 'admin', sysdate(), ''),
(1, '区间',     '1', 'food_ccp_limit_type',   '', 'primary', 'N', '0', 'admin', sysdate(), '下限~上限'),
(2, '不小于',   '2', 'food_ccp_limit_type',   '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '不大于',   '3', 'food_ccp_limit_type',   '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(4, '枚举',     '4', 'food_ccp_limit_type',   '', 'info',    'N', '0', 'admin', sysdate(), '限定字典值'),
(1, '合格',     '0', 'food_ccp_judge',        '', 'success', 'N', '0', 'admin', sysdate(), ''),
(2, '超限',     '1', 'food_ccp_judge',        '', 'danger',  'N', '0', 'admin', sysdate(), ''),
(1, '动物检疫合格证明', '1', 'food_cert_type', '', 'warning', 'N', '0', 'admin', sysdate(), '采购鲜腿等原料索证'),
(2, '检验报告',         '2', 'food_cert_type', '', 'warning', 'N', '0', 'admin', sysdate(), '供应商出厂检验报告'),
(3, '其他',             '3', 'food_cert_type', '', 'info',    'N', '0', 'admin', sysdate(), '');

-- -----------------------------------------------------------------------------
-- 三、系统参数（索证放行前置开关，配置驱动；幂等：先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `sys_config` WHERE config_key IN ('food.insp.cert.required','food.insp.cert.required.types');

INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`) VALUES
('检验索证索票放行前置开关', 'food.insp.cert.required',        'true',  'Y', 'admin', sysdate(), '开启后来料检验(IQC)判定放行前必须上传必传证件附件'),
('检验必传证件类型',         'food.insp.cert.required.types',  '1,2',   'Y', 'admin', sysdate(), '必传证件类型，逗号分隔，取字典food_cert_type');

-- -----------------------------------------------------------------------------
-- 四、干腌火腿CCP模板种子（RT-HAM-01，限值取自 mms_route_process 实际配置；幂等：先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `food_ccp_template` WHERE template_id BETWEEN 1 AND 99;

INSERT INTO `food_ccp_template` (`template_id`,`template_name`,`route_id`,`route_name`,`process_name`,`ccp_name`,`monitor_type`,`limit_type`,`limit_min`,`limit_max`,`limit_value`,`unit`,`monitor_freq`,`corrective_action`,`status`,`del_flag`,`create_by`,`create_time`,`remark`) VALUES
(1, '干腌火腿CCP', 1, '干腌火腿工艺路线', '原料腿验收',   '原料腿验收温度', '1', '1', 0.00,  4.00,  NULL, '℃', '每批到货',   '超温原料腿拒收退回，通知采购与供应商', '0', '0', 'admin', sysdate(), '限值取自工艺路线配置'),
(2, '干腌火腿CCP', 1, '干腌火腿工艺路线', '三次上盐腌制', '腌制间温度',     '1', '1', 0.00,  4.00,  NULL, '℃', '每日2次',    '立即隔离评估，记录偏差时间与持续时长，按SOP调整温控并复测', '0', '0', 'admin', sysdate(), ''),
(3, '干腌火腿CCP', 1, '干腌火腿工艺路线', '三次上盐腌制', '腌制间湿度',     '2', '1', 75.00, 90.00, NULL, '%',  '每日1次',    '检查加湿除湿设备，调整后复测并记录', '0', '0', 'admin', sysdate(), ''),
(4, '干腌火腿CCP', 1, '干腌火腿工艺路线', '三次上盐腌制', '盐量比例',       '3', '1', 2.50,  3.50,  NULL, '%',  '每垛每次上盐', '按SOP补盐或评估盐分渗透情况，必要时延长腌制时间', '0', '0', 'admin', sysdate(), ''),
(5, '干腌火腿CCP', 1, '干腌火腿工艺路线', '三次上盐腌制', '上霉状态',       '4', '4', NULL,  NULL,  '1',  '',   '每周2次',    '异常垛位立即隔离，通知技术员评估处理', '0', '0', 'admin', sysdate(), '枚举限值=上霉正常'),
(6, '干腌火腿CCP', 1, '干腌火腿工艺路线', '上挂低温发酵', '发酵间温度',     '1', '1', 8.00,  12.00, NULL, '℃', '每日2次',    '调整温控设备，评估失重与品质影响并记录', '0', '0', 'admin', sysdate(), ''),
(7, '干腌火腿CCP', 1, '干腌火腿工艺路线', '上挂低温发酵', '发酵间湿度',     '2', '1', 70.00, 85.00, NULL, '%',  '每日1次',    '调整加湿设备，记录偏差与处置', '0', '0', 'admin', sysdate(), ''),
(8, '干腌火腿CCP', 1, '干腌火腿工艺路线', '检验分级',     '分级检验区温度', '1', '1', 0.00,  8.00,  NULL, '℃', '每批',       '调整环境温度后复测，异常批次隔离评估', '0', '0', 'admin', sysdate(), ''),
(9, '干腌火腿CCP', 1, '干腌火腿工艺路线', '分割包装',     '分割车间温度',   '1', '1', 0.00,  12.00, NULL, '℃', '每班1次',    '停止作业调整温控，评估产品温度后恢复', '0', '0', 'admin', sysdate(), '');

-- -----------------------------------------------------------------------------
-- 五、菜单：CCP电子台账（质量管理 4727 下；幂等：先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `sys_menu` WHERE menu_id BETWEEN 9062190 AND 9062199;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (9062190, 'CCP电子台账', 4727, 4, 'ccp', 'food/ccp/index', '', '', 1, 0, 'C', '0', '0', 'food:ccp:list', 'form', 'admin', sysdate(), 'HACCP关键控制点电子记录：关键限值校验、超限联动不合格品控制、按时段导出应对验厂');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(9062191, '台账查询',     9062190, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:ccp:query',    '#', 'admin', sysdate(), ''),
(9062192, '台账录入',     9062190, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:ccp:add',      '#', 'admin', sysdate(), ''),
(9062193, '台账导出',     9062190, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:ccp:export',   '#', 'admin', sysdate(), '按时段导出CCP记录应对验厂'),
(9062194, '模板配置',     9062190, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:ccp:template', '#', 'admin', sysdate(), '维护CCP控制点模板（关键限值/监控频率/纠正措施）');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.menu_id FROM `sys_menu` m WHERE m.menu_id BETWEEN 9062190 AND 9062199;
