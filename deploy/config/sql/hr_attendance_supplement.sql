-- =====================================================
-- 考勤补全：补卡、外出、出差 SQL
-- 1. 新建三张表：补卡、外出、出差
-- 2. 新增菜单及按钮权限
-- 3. 新增字典：补卡时段、出差交通方式
-- 4. 新增编号规则
-- =====================================================

-- =====================================================
-- 1. 创建补卡表 biz_hr_clock_repair
-- =====================================================
CREATE TABLE IF NOT EXISTS `biz_hr_clock_repair` (
  `clock_repair_id`     BIGINT NOT NULL AUTO_INCREMENT COMMENT '补卡ID',
  `clock_repair_no`    VARCHAR(64) DEFAULT NULL COMMENT '补卡单号',
  `employee_id`         BIGINT NOT NULL COMMENT '员工ID',
  `employee_name`       VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no`         VARCHAR(50) DEFAULT NULL COMMENT '工号',
  `repair_date`         DATE NOT NULL COMMENT '补卡日期',
  `repair_type`         CHAR(1) NOT NULL COMMENT '补卡时段(0=上班打卡 1=下班打卡)',
  `expected_clock_time` DATETIME DEFAULT NULL COMMENT '应打卡时间',
  `actual_clock_time`   DATETIME DEFAULT NULL COMMENT '实际补卡时间',
  `reason`              VARCHAR(500) NOT NULL COMMENT '补卡原因',
  `attachment_url`      VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
  `audit_status`        CHAR(1) DEFAULT '0' COMMENT '审批状态(0=待审 1=通过 2=驳回 3=已作废)',
  `audit_by`            VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time`          DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark`        VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `status`              CHAR(1) DEFAULT '0' COMMENT '状态(0=启用 1=停用)',
  `del_flag`            CHAR(1) DEFAULT '0' COMMENT '删除标志(0=正常 2=删除)',
  `create_by`           VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time`         DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by`           VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time`         DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark`              VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`clock_repair_id`),
  KEY `idx_clock_repair_no` (`clock_repair_no`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_repair_date` (`repair_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR补卡申请表';

-- =====================================================
-- 2. 创建外出表 biz_hr_outing
-- =====================================================
CREATE TABLE IF NOT EXISTS `biz_hr_outing` (
  `outing_id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '外出ID',
  `outing_no`           VARCHAR(64) DEFAULT NULL COMMENT '外出单号',
  `employee_id`         BIGINT NOT NULL COMMENT '员工ID',
  `employee_name`       VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no`         VARCHAR(50) DEFAULT NULL COMMENT '工号',
  `outing_date`         DATE NOT NULL COMMENT '外出日期',
  `start_time`          DATETIME NOT NULL COMMENT '开始时间',
  `end_time`            DATETIME NOT NULL COMMENT '结束时间',
  `destination`         VARCHAR(200) NOT NULL COMMENT '外出地点',
  `reason`              VARCHAR(500) NOT NULL COMMENT '外出事由',
  `transport_mode`      VARCHAR(50) DEFAULT NULL COMMENT '交通工具',
  `expected_return_time` DATETIME DEFAULT NULL COMMENT '预计返回时间',
  `attachment_url`      VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
  `audit_status`        CHAR(1) DEFAULT '0' COMMENT '审批状态(0=待审 1=通过 2=驳回 3=已作废)',
  `audit_by`            VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time`          DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark`        VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `status`              CHAR(1) DEFAULT '0' COMMENT '状态(0=启用 1=停用)',
  `del_flag`            CHAR(1) DEFAULT '0' COMMENT '删除标志(0=正常 2=删除)',
  `create_by`           VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time`         DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by`           VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time`         DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark`              VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`outing_id`),
  KEY `idx_outing_no` (`outing_no`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_outing_date` (`outing_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR外出申请表';

-- =====================================================
-- 3. 创建出差表 biz_hr_business_trip
-- =====================================================
CREATE TABLE IF NOT EXISTS `biz_hr_business_trip` (
  `trip_id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '出差ID',
  `trip_no`             VARCHAR(64) DEFAULT NULL COMMENT '出差单号',
  `employee_id`         BIGINT NOT NULL COMMENT '员工ID',
  `employee_name`       VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no`         VARCHAR(50) DEFAULT NULL COMMENT '工号',
  `destination`         VARCHAR(200) NOT NULL COMMENT '出差目的地',
  `reason`              VARCHAR(500) NOT NULL COMMENT '出差事由',
  `departure_date`      DATE NOT NULL COMMENT '出发日期',
  `expected_return_date` DATE NOT NULL COMMENT '预计返回日期',
  `actual_return_date`  DATE DEFAULT NULL COMMENT '实际返回日期',
  `transport_mode`      VARCHAR(50) DEFAULT NULL COMMENT '交通方式',
  `trip_days`            DECIMAL(5,1) DEFAULT NULL COMMENT '出差天数',
  `budget_amount`       DECIMAL(10,2) DEFAULT NULL COMMENT '费用预算',
  `attachment_url`      VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
  `audit_status`        CHAR(1) DEFAULT '0' COMMENT '审批状态(0=待审 1=通过 2=驳回 3=已作废)',
  `audit_by`            VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time`          DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark`        VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `status`              CHAR(1) DEFAULT '0' COMMENT '状态(0=启用 1=停用)',
  `del_flag`            CHAR(1) DEFAULT '0' COMMENT '删除标志(0=正常 2=删除)',
  `create_by`           VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time`         DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by`           VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time`         DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark`              VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`trip_id`),
  KEY `idx_trip_no` (`trip_no`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_departure_date` (`departure_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR出差申请表';

-- =====================================================
-- 4. 新增菜单：补卡申请、外出申请、出差申请
-- =====================================================

-- 找到考勤排班或人力资源管理的父菜单
SET @parentId = (SELECT menu_id FROM sys_menu WHERE menu_name = '考勤排班' AND parent_id != 0 LIMIT 1);
SET @hrId = (SELECT menu_id FROM sys_menu WHERE menu_name = '人力资源管理' LIMIT 1);
SET @parentId = IFNULL(@parentId, @hrId);

-- 补卡申请菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('补卡申请', @parentId, 10, 'clockRepair', 'hr/clockRepair/index', NULL, 1, 0, 'C', '0', '0', 'hr:clockRepair:list', 'time', 'admin', NOW(), '补卡申请管理');

SET @menuId = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('补卡查询', @menuId, 1, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:clockRepair:query', '#', 'admin', NOW(), ''),
('补卡新增', @menuId, 2, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:clockRepair:add', '#', 'admin', NOW(), ''),
('补卡修改', @menuId, 3, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:clockRepair:edit', '#', 'admin', NOW(), ''),
('补卡删除', @menuId, 4, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:clockRepair:remove', '#', 'admin', NOW(), ''),
('补卡导出', @menuId, 5, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:clockRepair:export', '#', 'admin', NOW(), '');

-- 外出申请菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('外出申请', @parentId, 11, 'outing', 'hr/outing/index', NULL, 1, 0, 'C', '0', '0', 'hr:outing:list', 'position', 'admin', NOW(), '外出申请管理');

SET @menuId = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('外出查询', @menuId, 1, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:outing:query', '#', 'admin', NOW(), ''),
('外出新增', @menuId, 2, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:outing:add', '#', 'admin', NOW(), ''),
('外出修改', @menuId, 3, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:outing:edit', '#', 'admin', NOW(), ''),
('外出删除', @menuId, 4, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:outing:remove', '#', 'admin', NOW(), ''),
('外出导出', @menuId, 5, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:outing:export', '#', 'admin', NOW(), '');

-- 出差申请菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('出差申请', @parentId, 12, 'businessTrip', 'hr/businessTrip/index', NULL, 1, 0, 'C', '0', '0', 'hr:businessTrip:list', 'guide', 'admin', NOW(), '出差申请管理');

SET @menuId = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('出差查询', @menuId, 1, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:businessTrip:query', '#', 'admin', NOW(), ''),
('出差新增', @menuId, 2, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:businessTrip:add', '#', 'admin', NOW(), ''),
('出差修改', @menuId, 3, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:businessTrip:edit', '#', 'admin', NOW(), ''),
('出差删除', @menuId, 4, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:businessTrip:remove', '#', 'admin', NOW(), ''),
('出差导出', @menuId, 5, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:businessTrip:export', '#', 'admin', NOW(), '');

-- =====================================================
-- 5. 新增字典类型和字典数据
-- =====================================================

-- 补卡时段字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('补卡时段', 'hr_clock_repair_type', '0', 'admin', NOW(), '补卡申请时段（上班/下班）');

-- 补卡时段字典数据
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '上班打卡', '0', 'hr_clock_repair_type', 'Y', '0', 'admin', NOW(), '上班补卡'),
(2, '下班打卡', '1', 'hr_clock_repair_type', 'N', '0', 'admin', NOW(), '下班补卡');

-- 出差交通方式字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('出差交通方式', 'hr_transport_mode', '0', 'admin', NOW(), '出差/外出的交通方式');

-- 出差交通方式字典数据
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '飞机', '1', 'hr_transport_mode', 'N', '0', 'admin', NOW(), '飞机'),
(2, '高铁', '2', 'hr_transport_mode', 'N', '0', 'admin', NOW(), '高铁/动车'),
(3, '火车', '3', 'hr_transport_mode', 'N', '0', 'admin', NOW(), '普通火车'),
(4, '长途汽车', '4', 'hr_transport_mode', 'N', '0', 'admin', NOW(), '长途汽车'),
(5, '自驾', '5', 'hr_transport_mode', 'N', '0', 'admin', NOW(), '自驾'),
(6, '公司车辆', '6', 'hr_transport_mode', 'N', '0', 'admin', NOW(), '公司派车'),
(7, '公共交通', '7', 'hr_transport_mode', 'N', '0', 'admin', NOW(), '公交/地铁'),
(8, '其他', '9', 'hr_transport_mode', 'N', '0', 'admin', NOW(), '其他交通方式');

-- =====================================================
-- 6. 新增编号规则
-- =====================================================
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `seq_length`, `reset_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('hr_clock_repair', '补卡单号', 'BK', 'yyyyMMdd', 4, '1', '0', 'admin', NOW(), '补卡申请自动生成单号'),
('hr_outing', '外出单号', 'WC', 'yyyyMMdd', 4, '1', '0', 'admin', NOW(), '外出申请自动生成单号'),
('hr_business_trip', '出差单号', 'CC', 'yyyyMMdd', 4, '1', '0', 'admin', NOW(), '出差申请自动生成单号');
