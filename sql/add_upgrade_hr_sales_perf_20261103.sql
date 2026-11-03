-- =============================================
-- 需求三：人力资源管理加入营销管理内容（源禾农业 2026-11）
-- 1) biz_hr_sales_profile 营销岗档案扩展（1:1 员工）
-- 2) biz_hr_perf_manual 线下业绩手工补录（需审批）
-- 3) biz_hr_perf_month 月度业绩归集快照（定时任务生成）
-- 4) biz_hr_kpi_scheme / item / record 绩效考核（发起—自评—评分—确认—申诉）
-- 5) biz_hr_commission_plan / tier / bill 提成方案与核算表（MR-01 口径锁定）
-- 6) 字典、菜单（人力资源 905538 下）、月度归集定时任务
-- =============================================

CREATE TABLE IF NOT EXISTS `biz_hr_sales_profile` (
    `profile_id` bigint NOT NULL AUTO_INCREMENT COMMENT '档案ID',
    `employee_id` bigint NOT NULL COMMENT '员工ID（biz_hr_employee）',
    `employee_no` varchar(64) NULL COMMENT '员工编号',
    `employee_name` varchar(64) NULL COMMENT '员工姓名',
    `user_id` bigint NULL COMMENT '关联系统用户ID（营销业绩归属）',
    `sales_region` varchar(100) NULL COMMENT '销售区域',
    `channel_name` varchar(100) NULL COMMENT '销售渠道',
    `sales_level` char(1) DEFAULT '1' COMMENT '职级（1业务员 2主管 3经理）',
    `commission_plan_id` bigint NULL COMMENT '关联提成方案',
    `entry_target` decimal(18,2) DEFAULT 0 COMMENT '入职营销目标（年度）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`profile_id`),
    UNIQUE KEY `uk_hr_profile_emp` (`employee_id`),
    KEY `idx_hr_profile_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='营销岗员工档案扩展';

CREATE TABLE IF NOT EXISTS `biz_hr_perf_manual` (
    `manual_id` bigint NOT NULL AUTO_INCREMENT COMMENT '补录ID',
    `period` varchar(7) NOT NULL COMMENT '归集周期（yyyy-MM）',
    `employee_id` bigint NOT NULL COMMENT '员工ID',
    `employee_name` varchar(64) NULL COMMENT '员工姓名',
    `manual_amount` decimal(18,2) DEFAULT 0 COMMENT '补录业绩金额',
    `manual_reason` varchar(500) NULL COMMENT '补录原因',
    `perf_status` char(1) DEFAULT '0' COMMENT '状态（0待审批 1已通过 2已驳回）',
    `approver` varchar(64) NULL COMMENT '审批人',
    `approve_time` datetime NULL COMMENT '审批时间',
    `approve_opinion` varchar(500) NULL COMMENT '审批意见',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`manual_id`),
    KEY `idx_hr_manual_period` (`period`),
    KEY `idx_hr_manual_emp` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='线下业绩手工补录（需审批）';

CREATE TABLE IF NOT EXISTS `biz_hr_perf_month` (
    `perf_id` bigint NOT NULL AUTO_INCREMENT COMMENT '归集ID',
    `period` varchar(7) NOT NULL COMMENT '归集周期（yyyy-MM）',
    `employee_id` bigint NOT NULL COMMENT '员工ID',
    `employee_name` varchar(64) NULL COMMENT '员工姓名',
    `user_id` bigint NULL COMMENT '系统用户ID',
    `deal_amount` decimal(18,2) DEFAULT 0 COMMENT '成交金额（营销模块订单）',
    `payment_amount` decimal(18,2) DEFAULT 0 COMMENT '回款金额（回款记录）',
    `new_customer_count` int DEFAULT 0 COMMENT '新开客户数（周期内建档且完成首笔成交）',
    `manual_amount` decimal(18,2) DEFAULT 0 COMMENT '审批通过的补录业绩',
    `total_amount` decimal(18,2) DEFAULT 0 COMMENT '合计业绩（成交+补录）',
    `agg_time` datetime NULL COMMENT '归集时间',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`perf_id`),
    UNIQUE KEY `uk_hr_perf_period_emp` (`period`,`employee_id`),
    KEY `idx_hr_perf_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='营销业绩月度归集';

CREATE TABLE IF NOT EXISTS `biz_hr_kpi_scheme` (
    `scheme_id` bigint NOT NULL AUTO_INCREMENT COMMENT '方案ID',
    `scheme_name` varchar(200) NOT NULL COMMENT '方案名称',
    `period_type` char(1) DEFAULT '1' COMMENT '考核周期（1月 2季 3年）',
    `scheme_status` char(1) DEFAULT '0' COMMENT '状态（0草稿 1启用 2停用）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`scheme_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='KPI考核方案';

CREATE TABLE IF NOT EXISTS `biz_hr_kpi_item` (
    `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '指标ID',
    `scheme_id` bigint NOT NULL COMMENT '方案ID',
    `indicator_name` varchar(100) NOT NULL COMMENT '指标名称',
    `weight` decimal(5,2) DEFAULT 0 COMMENT '权重（%）',
    `target_value` varchar(100) NULL COMMENT '目标值',
    `score_rule` varchar(500) NULL COMMENT '评分规则说明',
    PRIMARY KEY (`item_id`),
    KEY `idx_hr_kpi_item_scheme` (`scheme_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='KPI考核指标';

CREATE TABLE IF NOT EXISTS `biz_hr_kpi_record` (
    `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '考核记录ID',
    `scheme_id` bigint NOT NULL COMMENT '方案ID',
    `period` varchar(7) NOT NULL COMMENT '考核周期（yyyy-MM）',
    `employee_id` bigint NOT NULL COMMENT '员工ID',
    `employee_name` varchar(64) NULL COMMENT '员工姓名',
    `self_score` decimal(5,2) NULL COMMENT '自评分',
    `self_comment` varchar(500) NULL COMMENT '自评说明',
    `superior_score` decimal(5,2) NULL COMMENT '上级评分',
    `superior_comment` varchar(500) NULL COMMENT '上级评语',
    `final_score` decimal(5,2) NULL COMMENT '最终得分',
    `kpi_status` char(1) DEFAULT '0' COMMENT '状态（0发起 1已自评 2已评分 3已确认 4申诉中 5已完成）',
    `appeal_reason` varchar(500) NULL COMMENT '申诉理由',
    `appeal_result` varchar(500) NULL COMMENT '申诉处理结果',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`record_id`),
    KEY `idx_hr_kpi_record_period` (`period`),
    KEY `idx_hr_kpi_record_emp` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='KPI考核记录';

CREATE TABLE IF NOT EXISTS `biz_hr_commission_plan` (
    `plan_id` bigint NOT NULL AUTO_INCREMENT COMMENT '方案ID',
    `plan_name` varchar(200) NOT NULL COMMENT '方案名称',
    `calc_base` char(1) DEFAULT '0' COMMENT '计算口径（0回款口径 1签单口径），MR-01 锁定后不可追溯修改',
    `plan_type` char(1) DEFAULT '0' COMMENT '类型（0固定比例 1阶梯 2超额奖励）',
    `base_ratio` decimal(5,2) DEFAULT 0 COMMENT '固定比例（%）',
    `bonus_threshold` decimal(18,2) NULL COMMENT '超额奖励起点',
    `bonus_ratio` decimal(5,2) DEFAULT 0 COMMENT '超额部分奖励比例（%）',
    `plan_status` char(1) DEFAULT '0' COMMENT '状态（0草稿 1启用 2停用）',
    `locked` char(1) DEFAULT '0' COMMENT '是否锁定（MR-01：已生成核算表后锁定，不可追溯修改）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`plan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='提成方案';

CREATE TABLE IF NOT EXISTS `biz_hr_commission_tier` (
    `tier_id` bigint NOT NULL AUTO_INCREMENT COMMENT '阶梯ID',
    `plan_id` bigint NOT NULL COMMENT '方案ID',
    `min_amount` decimal(18,2) DEFAULT 0 COMMENT '区间下限',
    `max_amount` decimal(18,2) NULL COMMENT '区间上限（空=不限）',
    `ratio` decimal(5,2) DEFAULT 0 COMMENT '提成比例（%）',
    PRIMARY KEY (`tier_id`),
    KEY `idx_hr_tier_plan` (`plan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='提成阶梯';

CREATE TABLE IF NOT EXISTS `biz_hr_commission_bill` (
    `bill_id` bigint NOT NULL AUTO_INCREMENT COMMENT '核算表ID',
    `period` varchar(7) NOT NULL COMMENT '核算周期（yyyy-MM）',
    `employee_id` bigint NOT NULL COMMENT '员工ID',
    `employee_name` varchar(64) NULL COMMENT '员工姓名',
    `plan_id` bigint NULL COMMENT '提成方案ID',
    `plan_name` varchar(200) NULL COMMENT '提成方案名称',
    `calc_base` char(1) NULL COMMENT '计算口径（快照）',
    `base_amount` decimal(18,2) DEFAULT 0 COMMENT '计算基数金额',
    `commission_amount` decimal(18,2) DEFAULT 0 COMMENT '提成金额',
    `bill_status` char(1) DEFAULT '0' COMMENT '状态（0待确认 1已确认 2已锁定）',
    `confirmer` varchar(64) NULL COMMENT '确认人',
    `confirm_time` datetime NULL COMMENT '确认时间',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`bill_id`),
    KEY `idx_hr_bill_period` (`period`),
    KEY `idx_hr_bill_emp` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='提成核算表';

-- ----------------------------
-- 字典
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('营销职级', 'hr_sales_level', '0', 'admin', NOW(), '营销岗员工职级'),
('补录审批状态', 'hr_manual_status', '0', 'admin', NOW(), '线下业绩补录审批状态'),
('考核周期类型', 'hr_kpi_period_type', '0', 'admin', NOW(), 'KPI考核周期类型'),
('考核记录状态', 'hr_kpi_status', '0', 'admin', NOW(), 'KPI考核记录状态'),
('提成计算口径', 'hr_commission_base', '0', 'admin', NOW(), '提成计算口径'),
('提成方案类型', 'hr_commission_type', '0', 'admin', NOW(), '提成方案类型'),
('核算表状态', 'hr_bill_status', '0', 'admin', NOW(), '提成核算表状态');

DELETE FROM sys_dict_data WHERE dict_type IN ('hr_sales_level','hr_manual_status','hr_kpi_period_type','hr_kpi_status','hr_commission_base','hr_commission_type','hr_bill_status');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark) VALUES
(NULL, 1, '业务员', '1', 'hr_sales_level', '', '0', 'admin', NOW(), ''),
(NULL, 2, '主管', '2', 'hr_sales_level', '', '0', 'admin', NOW(), ''),
(NULL, 3, '经理', '3', 'hr_sales_level', '', '0', 'admin', NOW(), ''),
(NULL, 1, '待审批', '0', 'hr_manual_status', 'warning', '0', 'admin', NOW(), ''),
(NULL, 2, '已通过', '1', 'hr_manual_status', 'success', '0', 'admin', NOW(), ''),
(NULL, 3, '已驳回', '2', 'hr_manual_status', 'danger', '0', 'admin', NOW(), ''),
(NULL, 1, '月', '1', 'hr_kpi_period_type', '', '0', 'admin', NOW(), ''),
(NULL, 2, '季', '2', 'hr_kpi_period_type', '', '0', 'admin', NOW(), ''),
(NULL, 3, '年', '3', 'hr_kpi_period_type', '', '0', 'admin', NOW(), ''),
(NULL, 1, '发起', '0', 'hr_kpi_status', 'info', '0', 'admin', NOW(), ''),
(NULL, 2, '已自评', '1', 'hr_kpi_status', '', '0', 'admin', NOW(), ''),
(NULL, 3, '已评分', '2', 'hr_kpi_status', '', '0', 'admin', NOW(), ''),
(NULL, 4, '已确认', '3', 'hr_kpi_status', 'success', '0', 'admin', NOW(), ''),
(NULL, 5, '申诉中', '4', 'hr_kpi_status', 'warning', '0', 'admin', NOW(), ''),
(NULL, 6, '已完成', '5', 'hr_kpi_status', 'success', '0', 'admin', NOW(), ''),
(NULL, 1, '回款口径', '0', 'hr_commission_base', '', '0', 'admin', NOW(), ''),
(NULL, 2, '签单口径', '1', 'hr_commission_base', '', '0', 'admin', NOW(), ''),
(NULL, 1, '固定比例', '0', 'hr_commission_type', '', '0', 'admin', NOW(), ''),
(NULL, 2, '阶梯比例', '1', 'hr_commission_type', '', '0', 'admin', NOW(), ''),
(NULL, 3, '超额奖励', '2', 'hr_commission_type', '', '0', 'admin', NOW(), ''),
(NULL, 1, '待确认', '0', 'hr_bill_status', 'warning', '0', 'admin', NOW(), ''),
(NULL, 2, '已确认', '1', 'hr_bill_status', 'success', '0', 'admin', NOW(), ''),
(NULL, 3, '已锁定', '2', 'hr_bill_status', 'info', '0', 'admin', NOW(), '');

-- ----------------------------
-- 菜单：人力资源管理（905538）下新增「营销考核」
-- ----------------------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('营销考核', 905538, 8, 'salesPerf', NULL, 1, 0, 'M', '0', '0', NULL, 'money', 'admin', NOW(), '营销岗档案扩展、业绩归集、绩效考核与提成核算');

SET @spMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('营销档案', @spMenuId, 1, 'profile', 'hr/salesProfile/index', 1, 0, 'C', '0', '0', 'hr:salesProfile:list', 'user', 'admin', NOW(), '营销岗员工档案扩展');
SET @spProfileId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('档案查询', @spProfileId, 1, '', '', 1, 0, 'F', '0', '0', 'hr:salesProfile:query', '#', 'admin', NOW(), ''),
('档案新增', @spProfileId, 2, '', '', 1, 0, 'F', '0', '0', 'hr:salesProfile:add', '#', 'admin', NOW(), ''),
('档案修改', @spProfileId, 3, '', '', 1, 0, 'F', '0', '0', 'hr:salesProfile:edit', '#', 'admin', NOW(), ''),
('档案删除', @spProfileId, 4, '', '', 1, 0, 'F', '0', '0', 'hr:salesProfile:remove', '#', 'admin', NOW(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('业绩归集', @spMenuId, 2, 'perf', 'hr/perf/index', 1, 0, 'C', '0', '0', 'hr:perf:list', 'chart', 'admin', NOW(), '月度业绩归集与线下补录审批');
SET @spPerfId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('归集查询', @spPerfId, 1, '', '', 1, 0, 'F', '0', '0', 'hr:perf:query', '#', 'admin', NOW(), ''),
('补录新增', @spPerfId, 2, '', '', 1, 0, 'F', '0', '0', 'hr:perf:manualAdd', '#', 'admin', NOW(), ''),
('补录审批', @spPerfId, 3, '', '', 1, 0, 'F', '0', '0', 'hr:perf:approve', '#', 'admin', NOW(), ''),
('手动归集', @spPerfId, 4, '', '', 1, 0, 'F', '0', '0', 'hr:perf:aggregate', '#', 'admin', NOW(), ''),
('归集导出', @spPerfId, 5, '', '', 1, 0, 'F', '0', '0', 'hr:perf:export', '#', 'admin', NOW(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('绩效考核', @spMenuId, 3, 'kpi', 'hr/kpi/index', 1, 0, 'C', '0', '0', 'hr:kpi:list', 'form', 'admin', NOW(), 'KPI方案配置与考核记录');
SET @spKpiId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('方案查询', @spKpiId, 1, '', '', 1, 0, 'F', '0', '0', 'hr:kpi:query', '#', 'admin', NOW(), ''),
('方案新增', @spKpiId, 2, '', '', 1, 0, 'F', '0', '0', 'hr:kpi:add', '#', 'admin', NOW(), ''),
('发起考核', @spKpiId, 3, '', '', 1, 0, 'F', '0', '0', 'hr:kpi:initiate', '#', 'admin', NOW(), ''),
('考核评分', @spKpiId, 4, '', '', 1, 0, 'F', '0', '0', 'hr:kpi:score', '#', 'admin', NOW(), ''),
('考核确认', @spKpiId, 5, '', '', 1, 0, 'F', '0', '0', 'hr:kpi:confirm', '#', 'admin', NOW(), ''),
('考核申诉', @spKpiId, 6, '', '', 1, 0, 'F', '0', '0', 'hr:kpi:appeal', '#', 'admin', NOW(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('提成核算', @spMenuId, 4, 'commission', 'hr/commission/index', 1, 0, 'C', '0', '0', 'hr:commission:list', 'calculator', 'admin', NOW(), '提成方案配置与每期核算表');
SET @spCommId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('方案查询', @spCommId, 1, '', '', 1, 0, 'F', '0', '0', 'hr:commission:query', '#', 'admin', NOW(), ''),
('方案新增', @spCommId, 2, '', '', 1, 0, 'F', '0', '0', 'hr:commission:add', '#', 'admin', NOW(), ''),
('方案修改', @spCommId, 3, '', '', 1, 0, 'F', '0', '0', 'hr:commission:edit', '#', 'admin', NOW(), ''),
('生成核算表', @spCommId, 4, '', '', 1, 0, 'F', '0', '0', 'hr:commission:generate', '#', 'admin', NOW(), ''),
('核算确认', @spCommId, 5, '', '', 1, 0, 'F', '0', '0', 'hr:commission:confirm', '#', 'admin', NOW(), ''),
('核算删除', @spCommId, 6, '', '', 1, 0, 'F', '0', '0', 'hr:commission:remove', '#', 'admin', NOW(), '');

-- ----------------------------
-- 定时任务：每月 1 日 2 点归集上月业绩
-- ----------------------------
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('营销业绩月度归集', 'DEFAULT', 'hrPerfAggTask.handle()', '0 0 2 1 * ?', '3', '1', '0', 'admin', NOW(), '每月1日凌晨归集上月营销业绩（成交/回款/新客/补录），并支持界面手动触发');

-- 验证
SELECT '--- 需求三新表 ---' AS info;
SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name LIKE 'biz_hr_%' AND (table_name LIKE '%perf%' OR table_name LIKE '%kpi%' OR table_name LIKE '%commission%' OR table_name LIKE '%sales_profile%');
SELECT '--- 营销考核菜单 ---' AS info;
SELECT menu_id, menu_name, parent_id, path, perms FROM sys_menu WHERE perms LIKE 'hr:%' OR menu_name = '营销考核';
