-- =============================================
-- HR人力资源管理模块 - 表结构 Part 1 (组织~考勤)
-- =============================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 1. 组织架构 ==========
DROP TABLE IF EXISTS `biz_hr_organization`;
CREATE TABLE `biz_hr_organization` (
  `org_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `org_code` VARCHAR(50) NOT NULL COMMENT '组织编码',
  `org_name` VARCHAR(100) NOT NULL COMMENT '组织名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父组织ID',
  `order_num` INT DEFAULT 0 COMMENT '排序号',
  `org_level` TINYINT DEFAULT 1 COMMENT '层级(1公司2分厂3部门4班组)',
  `leader_id` BIGINT DEFAULT NULL COMMENT '负责人ID',
  `leader_name` VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名',
  `cost_center` TINYINT DEFAULT 0 COMMENT '成本中心(0否1是)',
  `attendance_org` TINYINT DEFAULT 0 COMMENT '考勤归属(0否1是)',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常1停用)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`org_id`),
  UNIQUE KEY `uk_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR组织架构表';

-- ========== 2. 岗位 ==========
DROP TABLE IF EXISTS `biz_hr_position`;
CREATE TABLE `biz_hr_position` (
  `position_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `position_code` VARCHAR(50) NOT NULL COMMENT '岗位编码',
  `position_name` VARCHAR(100) NOT NULL COMMENT '岗位名称',
  `position_seq` CHAR(1) DEFAULT NULL COMMENT '序列(M管理P技术W技能)',
  `position_level` VARCHAR(20) DEFAULT NULL COMMENT '岗级',
  `skill_level` VARCHAR(20) DEFAULT NULL COMMENT '技能等级',
  `org_id` BIGINT DEFAULT NULL COMMENT '所属组织ID',
  `headcount` INT DEFAULT 0 COMMENT '编制人数',
  `headcount_enabled` TINYINT DEFAULT 0 COMMENT '编制管理(0关1开)',
  `require_cert` TEXT DEFAULT NULL COMMENT '必须持证(JSON)',
  `order_num` INT DEFAULT 0 COMMENT '排序号',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常1停用)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`position_id`),
  UNIQUE KEY `uk_position_code` (`position_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR岗位表';

-- ========== 3. 员工档案 ==========
DROP TABLE IF EXISTS `biz_hr_employee`;
CREATE TABLE `biz_hr_employee` (
  `employee_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `employee_no` VARCHAR(30) NOT NULL COMMENT '工号',
  `employee_name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `gender` CHAR(1) DEFAULT '0' COMMENT '性别(0男1女2未知)',
  `id_card` VARCHAR(100) DEFAULT NULL COMMENT '身份证号(加密)',
  `birthday` DATE DEFAULT NULL COMMENT '出生日期',
  `nation` VARCHAR(30) DEFAULT NULL COMMENT '民族',
  `politics_status` VARCHAR(30) DEFAULT NULL COMMENT '政治面貌',
  `marital_status` CHAR(1) DEFAULT NULL COMMENT '婚姻(0未婚1已婚2离异3丧偶)',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
  `emergency_contact` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系电话',
  `native_place` VARCHAR(100) DEFAULT NULL COMMENT '籍贯',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '现住址',
  `edu_level` VARCHAR(20) DEFAULT NULL COMMENT '学历',
  `graduation_school` VARCHAR(100) DEFAULT NULL COMMENT '毕业院校',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `graduation_date` DATE DEFAULT NULL COMMENT '毕业日期',
  `social_work_years` INT DEFAULT 0 COMMENT '社会工龄',
  `company_work_years` INT DEFAULT 0 COMMENT '本公司工龄',
  `org_id` BIGINT DEFAULT NULL COMMENT '所属组织ID',
  `position_id` BIGINT DEFAULT NULL COMMENT '岗位ID',
  `position_name` VARCHAR(100) DEFAULT NULL COMMENT '岗位名称',
  `skill_level` VARCHAR(20) DEFAULT NULL COMMENT '技能等级',
  `work_type` CHAR(1) DEFAULT '0' COMMENT '工时制度(0标准1综合2不定时)',
  `employee_type` CHAR(1) DEFAULT '0' COMMENT '用工类型(0正式1派遣2临时3实习4返聘)',
  `entry_date` DATE DEFAULT NULL COMMENT '入职日期',
  `regular_date` DATE DEFAULT NULL COMMENT '转正日期',
  `leave_date` DATE DEFAULT NULL COMMENT '离职日期',
  `employee_status` CHAR(1) DEFAULT '0' COMMENT '状态(0在职1试用2离职3黑名单)',
  `bank_name` VARCHAR(50) DEFAULT NULL COMMENT '开户银行',
  `bank_account` VARCHAR(100) DEFAULT NULL COMMENT '银行卡号(加密)',
  `social_security_no` VARCHAR(50) DEFAULT NULL COMMENT '社保号',
  `social_security_status` CHAR(1) DEFAULT '0' COMMENT '参保状态(0在缴1停缴2未参保)',
  `house_fund_no` VARCHAR(50) DEFAULT NULL COMMENT '公积金号',
  `leave_reason` VARCHAR(200) DEFAULT NULL COMMENT '离职原因',
  `blacklist_flag` CHAR(1) DEFAULT '0' COMMENT '黑名单(0否1是)',
  `blacklist_reason` VARCHAR(200) DEFAULT NULL COMMENT '黑名单原因',
  `user_id` BIGINT DEFAULT NULL COMMENT '关联系统用户ID',
  `photo` VARCHAR(200) DEFAULT NULL COMMENT '照片URL',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常1停用)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  KEY `idx_name` (`employee_name`),
  KEY `idx_org` (`org_id`),
  KEY `idx_status` (`employee_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR员工档案表';

-- ========== 4. 入职 ==========
DROP TABLE IF EXISTS `biz_hr_entry`;
CREATE TABLE `biz_hr_entry` (
  `entry_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入职ID',
  `entry_no` VARCHAR(30) NOT NULL COMMENT '入职单号',
  `employee_name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `id_card` VARCHAR(100) DEFAULT NULL COMMENT '身份证号(加密)',
  `gender` CHAR(1) DEFAULT NULL COMMENT '性别',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `org_id` BIGINT DEFAULT NULL COMMENT '组织ID',
  `org_name` VARCHAR(100) DEFAULT NULL COMMENT '组织名称',
  `position_id` BIGINT DEFAULT NULL COMMENT '岗位ID',
  `position_name` VARCHAR(100) DEFAULT NULL COMMENT '岗位名称',
  `entry_date` DATE NOT NULL COMMENT '入职日期',
  `probation_months` INT DEFAULT NULL COMMENT '试用期月数',
  `probation_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '试用工资',
  `regular_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '转正工资',
  `contract_type` CHAR(1) DEFAULT '0' COMMENT '合同类型(0固定1无固定2以完成)',
  `contract_period` VARCHAR(30) DEFAULT NULL COMMENT '合同期限',
  `materials_status` TEXT DEFAULT NULL COMMENT '材料清单(JSON)',
  `health_check` CHAR(1) DEFAULT '0' COMMENT '岗前体检(0未做1合格2不合格)',
  `safety_education` CHAR(1) DEFAULT '0' COMMENT '三级安全教育(0未做1已完成)',
  `audit_status` CHAR(1) DEFAULT '0' COMMENT '审批状态(0待审1通过2驳回)',
  `audit_by` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `employee_id` BIGINT DEFAULT NULL COMMENT '生成的员工档案ID',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0待办1已建档2已取消)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`entry_id`),
  UNIQUE KEY `uk_entry_no` (`entry_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR入职管理表';

-- ========== 5. 转正 ==========
DROP TABLE IF EXISTS `biz_hr_regular`;
CREATE TABLE `biz_hr_regular` (
  `regular_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '转正ID',
  `regular_no` VARCHAR(30) NOT NULL COMMENT '转正单号',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `org_name` VARCHAR(100) DEFAULT NULL COMMENT '组织名称',
  `position_name` VARCHAR(100) DEFAULT NULL COMMENT '岗位名称',
  `entry_date` DATE DEFAULT NULL COMMENT '入职日期',
  `regular_date` DATE DEFAULT NULL COMMENT '拟转正日期',
  `work_performance` TEXT DEFAULT NULL COMMENT '工作表现',
  `skill_assessment` TEXT DEFAULT NULL COMMENT '技能达标',
  `attendance_record` TEXT DEFAULT NULL COMMENT '出勤情况',
  `safety_violation` CHAR(1) DEFAULT '0' COMMENT '安全违规(0否1是)',
  `evaluation_result` CHAR(1) DEFAULT '0' COMMENT '评估结果(0合格1不予录用2到期终止)',
  `audit_status` CHAR(1) DEFAULT '0' COMMENT '审批状态(0待审1通过2驳回)',
  `audit_by` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0待办1已生效2已取消)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`regular_id`),
  UNIQUE KEY `uk_regular_no` (`regular_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR转正管理表';

-- ========== 6. 调动 ==========
DROP TABLE IF EXISTS `biz_hr_transfer`;
CREATE TABLE `biz_hr_transfer` (
  `transfer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '调动ID',
  `transfer_no` VARCHAR(30) NOT NULL COMMENT '调动单号',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `transfer_type` CHAR(1) DEFAULT NULL COMMENT '调动类型(1部门2岗位3薪酬4班制5地点)',
  `from_org_id` BIGINT DEFAULT NULL COMMENT '原组织ID',
  `from_org_name` VARCHAR(100) DEFAULT NULL COMMENT '原组织名称',
  `to_org_id` BIGINT DEFAULT NULL COMMENT '目标组织ID',
  `to_org_name` VARCHAR(100) DEFAULT NULL COMMENT '目标组织名称',
  `from_position` VARCHAR(100) DEFAULT NULL COMMENT '原岗位',
  `to_position` VARCHAR(100) DEFAULT NULL COMMENT '目标岗位',
  `from_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '原薪资',
  `to_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '目标薪资',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `reason` VARCHAR(200) DEFAULT NULL COMMENT '调动原因',
  `audit_status` CHAR(1) DEFAULT '0' COMMENT '审批状态(0待审1通过2驳回)',
  `audit_by` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0待办1已生效2已取消)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`transfer_id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR调动管理表';

-- ========== 7. 离职 ==========
DROP TABLE IF EXISTS `biz_hr_leave`;
CREATE TABLE `biz_hr_leave` (
  `leave_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '离职ID',
  `leave_no` VARCHAR(30) NOT NULL COMMENT '离职单号',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `leave_type` CHAR(1) DEFAULT NULL COMMENT '离职类型(0主动辞职1协商解除2违纪辞退3合同到期4退休)',
  `leave_date` DATE NOT NULL COMMENT '离职日期',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '离职原因',
  `handover_status` CHAR(1) DEFAULT '0' COMMENT '交接状态(0未完成1已完成)',
  `handover_remark` TEXT DEFAULT NULL COMMENT '交接清单(JSON)',
  `health_check` CHAR(1) DEFAULT '0' COMMENT '离岗体检(0未做1合格2不合格3不需要)',
  `compensation_type` CHAR(1) DEFAULT NULL COMMENT '补偿类型(0N 1N+1 2 2N 3无)',
  `compensation_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '补偿金额',
  `annual_leave_balance` DECIMAL(5,1) DEFAULT NULL COMMENT '未休年假天数',
  `annual_leave_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '年假折算金额',
  `settlement_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '结算总金额',
  `certificate_no` VARCHAR(50) DEFAULT NULL COMMENT '离职证明编号',
  `audit_status` CHAR(1) DEFAULT '0' COMMENT '审批状态(0待审1通过2驳回)',
  `audit_by` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0待办1已办结2已取消)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`leave_id`),
  UNIQUE KEY `uk_leave_no` (`leave_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR离职管理表';

-- ========== 8. 劳动合同 ==========
DROP TABLE IF EXISTS `biz_hr_contract`;
CREATE TABLE `biz_hr_contract` (
  `contract_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `contract_no` VARCHAR(30) NOT NULL COMMENT '合同编号',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `contract_type` CHAR(1) DEFAULT '0' COMMENT '合同类型(0固定1无固定2以完成)',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '结束日期(无固定为空)',
  `sign_date` DATE DEFAULT NULL COMMENT '签订日期',
  `probation_months` INT DEFAULT 0 COMMENT '试用期月数',
  `probation_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '试用期工资',
  `regular_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '转正工资',
  `renew_count` INT DEFAULT 0 COMMENT '续签次数',
  `remind_status` CHAR(1) DEFAULT '0' COMMENT '预警状态(0正常1临期2过期)',
  `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0有效1终止2到期3解除)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`contract_id`),
  UNIQUE KEY `uk_contract_no` (`contract_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR劳动合同表';

-- ========== 9. 班次库 ==========
DROP TABLE IF EXISTS `biz_hr_shift`;
CREATE TABLE `biz_hr_shift` (
  `shift_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '班次ID',
  `shift_code` VARCHAR(30) NOT NULL COMMENT '班次编码',
  `shift_name` VARCHAR(50) NOT NULL COMMENT '班次名称',
  `start_time` VARCHAR(10) NOT NULL COMMENT '开始时间',
  `end_time` VARCHAR(10) NOT NULL COMMENT '结束时间',
  `cross_midnight` TINYINT DEFAULT 0 COMMENT '跨零点(0否1是)',
  `work_hours` DECIMAL(4,1) DEFAULT 8.0 COMMENT '工作时长',
  `rest_hours` DECIMAL(4,1) DEFAULT 1.0 COMMENT '休息时长',
  `meal_minutes` INT DEFAULT 60 COMMENT '用餐时间(分钟)',
  `night_shift` TINYINT DEFAULT 0 COMMENT '夜班(0否1是)',
  `night_allowance` DECIMAL(8,2) DEFAULT 0 COMMENT '夜班津贴',
  `applicable_to` VARCHAR(200) DEFAULT NULL COMMENT '适用场景',
  `order_num` INT DEFAULT 0 COMMENT '排序号',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常1停用)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`shift_id`),
  UNIQUE KEY `uk_shift_code` (`shift_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR班次库表';

-- ========== 10. 排班 ==========
DROP TABLE IF EXISTS `biz_hr_schedule`;
CREATE TABLE `biz_hr_schedule` (
  `schedule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `schedule_date` DATE NOT NULL COMMENT '排班日期',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `org_id` BIGINT DEFAULT NULL COMMENT '组织ID',
  `shift_id` BIGINT DEFAULT NULL COMMENT '班次ID',
  `shift_name` VARCHAR(50) DEFAULT NULL COMMENT '班次名称',
  `rest_day` TINYINT DEFAULT 0 COMMENT '休息日(0否1是)',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0排班1换班2取消)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`schedule_id`),
  KEY `idx_date_emp` (`schedule_date`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR排班管理表';

-- ========== 11. 考勤记录 ==========
DROP TABLE IF EXISTS `biz_hr_attendance_record`;
CREATE TABLE `biz_hr_attendance_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `record_date` DATE NOT NULL COMMENT '考勤日期',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `org_id` BIGINT DEFAULT NULL COMMENT '组织ID',
  `shift_id` BIGINT DEFAULT NULL COMMENT '班次ID',
  `shift_name` VARCHAR(50) DEFAULT NULL COMMENT '班次名称',
  `clock_in` DATETIME DEFAULT NULL COMMENT '上班打卡',
  `clock_out` DATETIME DEFAULT NULL COMMENT '下班打卡',
  `clock_in_source` CHAR(1) DEFAULT '0' COMMENT '上班来源(0考勤机1补卡2免卡)',
  `clock_out_source` CHAR(1) DEFAULT '0' COMMENT '下班来源(0考勤机1补卡2免卡)',
  `late_minutes` INT DEFAULT 0 COMMENT '迟到分钟',
  `early_minutes` INT DEFAULT 0 COMMENT '早退分钟',
  `attendance_status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常1迟到2早退3旷工4漏卡5请假6出差7加班)',
  `overtime_hours` DECIMAL(4,1) DEFAULT 0 COMMENT '加班小时',
  `night_shift` TINYINT DEFAULT 0 COMMENT '夜班(0否1是)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_date_emp` (`record_date`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR考勤记录表';

-- ========== 12. 请假 ==========
DROP TABLE IF EXISTS `biz_hr_leave_request`;
CREATE TABLE `biz_hr_leave_request` (
  `leave_req_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '请假ID',
  `leave_req_no` VARCHAR(30) NOT NULL COMMENT '请假单号',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `leave_type` VARCHAR(20) NOT NULL COMMENT '假别',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `leave_days` DECIMAL(5,1) NOT NULL COMMENT '请假天数',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '请假原因',
  `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
  `audit_status` CHAR(1) DEFAULT '0' COMMENT '审批状态(0待审1通过2驳回3已销假)',
  `audit_by` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0有效1取消2销假)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`leave_req_id`),
  UNIQUE KEY `uk_leave_req_no` (`leave_req_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR请假管理表';

-- ========== 13. 加班 ==========
DROP TABLE IF EXISTS `biz_hr_overtime`;
CREATE TABLE `biz_hr_overtime` (
  `overtime_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '加班ID',
  `overtime_no` VARCHAR(30) NOT NULL COMMENT '加班单号',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `overtime_date` DATE NOT NULL COMMENT '加班日期',
  `overtime_type` CHAR(1) DEFAULT '0' COMMENT '类型(0工作日1休息日2法定节假日)',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `overtime_hours` DECIMAL(4,1) NOT NULL COMMENT '加班小时',
  `multiplier` DECIMAL(3,1) DEFAULT 1.5 COMMENT '倍率',
  `compensation_type` CHAR(1) DEFAULT '0' COMMENT '补偿方式(0加班费1调休)',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '加班原因',
  `audit_status` CHAR(1) DEFAULT '0' COMMENT '审批状态(0待审1通过2驳回)',
  `audit_by` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0有效1取消)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`overtime_id`),
  UNIQUE KEY `uk_overtime_no` (`overtime_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR加班管理表';

-- ========== 14. 考勤月度汇总 ==========
DROP TABLE IF EXISTS `biz_hr_attendance_monthly`;
CREATE TABLE `biz_hr_attendance_monthly` (
  `monthly_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '月度ID',
  `year_month` VARCHAR(7) NOT NULL COMMENT '年月',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `org_id` BIGINT DEFAULT NULL COMMENT '组织ID',
  `attend_days` DECIMAL(5,1) DEFAULT 0 COMMENT '出勤天数',
  `late_count` INT DEFAULT 0 COMMENT '迟到次数',
  `early_count` INT DEFAULT 0 COMMENT '早退次数',
  `absent_days` DECIMAL(5,1) DEFAULT 0 COMMENT '旷工天数',
  `leave_days` DECIMAL(5,1) DEFAULT 0 COMMENT '请假天数',
  `overtime_hours` DECIMAL(6,1) DEFAULT 0 COMMENT '加班小时',
  `night_shift_count` INT DEFAULT 0 COMMENT '夜班次数',
  `high_temp_days` DECIMAL(5,1) DEFAULT 0 COMMENT '高温天数',
  `sealed_status` CHAR(1) DEFAULT '0' COMMENT '封账状态(0未封1已封)',
  `sealed_by` VARCHAR(64) DEFAULT NULL COMMENT '封账人',
  `sealed_time` DATETIME DEFAULT NULL COMMENT '封账时间',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`monthly_id`),
  KEY `idx_ym_emp` (`year_month`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR考勤月度汇总表';

-- ========== 15. 假期余额 ==========
DROP TABLE IF EXISTS `biz_hr_leave_balance`;
CREATE TABLE `biz_hr_leave_balance` (
  `balance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '余额ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `leave_type` VARCHAR(20) NOT NULL COMMENT '假别',
  `year` INT NOT NULL COMMENT '年度',
  `total_days` DECIMAL(5,1) DEFAULT 0 COMMENT '总额度',
  `used_days` DECIMAL(5,1) DEFAULT 0 COMMENT '已休天数',
  `remaining_days` DECIMAL(5,1) DEFAULT 0 COMMENT '剩余天数',
  `frozen_days` DECIMAL(5,1) DEFAULT 0 COMMENT '冻结天数',
  `expire_date` DATE DEFAULT NULL COMMENT '过期日期',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`balance_id`),
  KEY `idx_emp_type_year` (`employee_id`, `leave_type`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR假期余额表';
