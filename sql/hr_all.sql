-- =============================================
-- HR人力资源管理模块 完整SQL(表结构+菜单+字典+预置数据)
-- =============================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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


-- =============================================
-- HR人力资源管理模块 - 表结构 Part 2 (薪酬~劳务)
-- =============================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 16. 薪资项字典 ==========
DROP TABLE IF EXISTS `biz_hr_salary_item`;
CREATE TABLE `biz_hr_salary_item` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '薪资项ID',
  `item_code` VARCHAR(30) NOT NULL COMMENT '编码',
  `item_name` VARCHAR(50) NOT NULL COMMENT '名称',
  `item_category` CHAR(1) DEFAULT '0' COMMENT '类别(0固定1考勤联动2计件3手工)',
  `calc_rule` VARCHAR(500) DEFAULT NULL COMMENT '计算规则',
  `is_add` TINYINT DEFAULT 1 COMMENT '加项(0扣减1增加)',
  `industry_tag` VARCHAR(100) DEFAULT NULL COMMENT '行业标签',
  `order_num` INT DEFAULT 0 COMMENT '排序号',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常1停用)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `uk_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR薪资项字典表';

-- ========== 17. 薪酬方案(定薪) ==========
DROP TABLE IF EXISTS `biz_hr_salary_plan`;
CREATE TABLE `biz_hr_salary_plan` (
  `plan_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '方案ID',
  `plan_no` VARCHAR(30) NOT NULL COMMENT '定薪单号',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `plan_type` CHAR(1) DEFAULT '0' COMMENT '类型(0入职定薪1转正调薪2岗变调薪3年度普调4个案调薪)',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `basic_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '基本工资',
  `position_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '岗位工资',
  `skill_allowance` DECIMAL(10,2) DEFAULT 0 COMMENT '技能津贴',
  `seniority_pay` DECIMAL(10,2) DEFAULT 0 COMMENT '工龄工资',
  `night_allowance` DECIMAL(8,2) DEFAULT 0 COMMENT '夜班津贴标准',
  `high_temp_allowance` DECIMAL(8,2) DEFAULT 0 COMMENT '高温津贴标准',
  `hazard_allowance` DECIMAL(8,2) DEFAULT 0 COMMENT '有毒有害津贴',
  `meal_allowance` DECIMAL(8,2) DEFAULT 0 COMMENT '餐补标准',
  `transport_allowance` DECIMAL(8,2) DEFAULT 0 COMMENT '交通补贴',
  `full_attendance_bonus` DECIMAL(8,2) DEFAULT 0 COMMENT '全勤奖',
  `performance_bonus` DECIMAL(10,2) DEFAULT 0 COMMENT '绩效奖金',
  `other_allowance` DECIMAL(10,2) DEFAULT 0 COMMENT '其他补贴',
  `total_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '薪资合计',
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
  PRIMARY KEY (`plan_id`),
  UNIQUE KEY `uk_plan_no` (`plan_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR薪酬方案表';

-- ========== 18. 工资条明细 ==========
DROP TABLE IF EXISTS `biz_hr_payslip`;
CREATE TABLE `biz_hr_payslip` (
  `payslip_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工资条ID',
  `year_month` VARCHAR(7) NOT NULL COMMENT '年月',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `org_id` BIGINT DEFAULT NULL COMMENT '组织ID',
  `basic_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '基本工资',
  `position_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '岗位工资',
  `skill_allowance` DECIMAL(10,2) DEFAULT 0 COMMENT '技能津贴',
  `seniority_pay` DECIMAL(10,2) DEFAULT 0 COMMENT '工龄工资',
  `night_allowance` DECIMAL(10,2) DEFAULT 0 COMMENT '夜班津贴',
  `high_temp_allowance` DECIMAL(10,2) DEFAULT 0 COMMENT '高温津贴',
  `hazard_allowance` DECIMAL(10,2) DEFAULT 0 COMMENT '有毒有害津贴',
  `meal_allowance` DECIMAL(10,2) DEFAULT 0 COMMENT '餐补',
  `transport_allowance` DECIMAL(10,2) DEFAULT 0 COMMENT '交通补贴',
  `full_attendance_bonus` DECIMAL(10,2) DEFAULT 0 COMMENT '全勤奖',
  `piecework_pay` DECIMAL(10,2) DEFAULT 0 COMMENT '计件工资',
  `overtime_pay` DECIMAL(10,2) DEFAULT 0 COMMENT '加班费',
  `performance_bonus` DECIMAL(10,2) DEFAULT 0 COMMENT '绩效奖金',
  `absence_deduction` DECIMAL(10,2) DEFAULT 0 COMMENT '缺勤扣款',
  `other_add` DECIMAL(10,2) DEFAULT 0 COMMENT '其他加项',
  `other_deduct` DECIMAL(10,2) DEFAULT 0 COMMENT '其他扣项',
  `gross_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '应发合计',
  `social_insurance` DECIMAL(10,2) DEFAULT 0 COMMENT '社保个人',
  `house_fund` DECIMAL(10,2) DEFAULT 0 COMMENT '公积金个人',
  `income_tax` DECIMAL(10,2) DEFAULT 0 COMMENT '个人所得税',
  `other_deduction` DECIMAL(10,2) DEFAULT 0 COMMENT '其他扣款',
  `net_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '实发工资',
  `calc_status` CHAR(1) DEFAULT '0' COMMENT '状态(0草稿1已锁定2已发放)',
  `sealed_by` VARCHAR(64) DEFAULT NULL COMMENT '锁定人',
  `sealed_time` DATETIME DEFAULT NULL COMMENT '锁定时间',
  `pay_date` DATE DEFAULT NULL COMMENT '发放日期',
  `bank_receipt_no` VARCHAR(50) DEFAULT NULL COMMENT '银行回单号',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`payslip_id`),
  KEY `idx_ym_emp` (`year_month`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR工资条明细表';

-- ========== 19. 计件方案 ==========
DROP TABLE IF EXISTS `biz_hr_piecework_plan`;
CREATE TABLE `biz_hr_piecework_plan` (
  `pw_plan_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '计件方案ID',
  `pw_code` VARCHAR(30) NOT NULL COMMENT '计件编码',
  `pw_name` VARCHAR(100) NOT NULL COMMENT '计件名称',
  `pw_type` CHAR(1) DEFAULT '0' COMMENT '计件类型(0个人1班组集体)',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
  `unit_price` DECIMAL(10,2) NOT NULL COMMENT '计件单价',
  `product_id` BIGINT DEFAULT NULL COMMENT '产品ID',
  `process_id` BIGINT DEFAULT NULL COMMENT '工序ID',
  `org_id` BIGINT DEFAULT NULL COMMENT '适用组织ID',
  `base_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '保底工资',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `expiry_date` DATE DEFAULT NULL COMMENT '失效日期',
  `audit_status` CHAR(1) DEFAULT '0' COMMENT '审批状态(0待审1通过2驳回)',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0有效1停用)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`pw_plan_id`),
  UNIQUE KEY `uk_pw_code` (`pw_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR计件方案表';

-- ========== 20. 计件报工 ==========
DROP TABLE IF EXISTS `biz_hr_piecework_record`;
CREATE TABLE `biz_hr_piecework_record` (
  `pw_record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报工ID',
  `pw_record_no` VARCHAR(30) NOT NULL COMMENT '报工单号',
  `record_date` DATE NOT NULL COMMENT '报工日期',
  `employee_id` BIGINT DEFAULT NULL COMMENT '员工ID(个人计件)',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `org_id` BIGINT DEFAULT NULL COMMENT '班组组织ID(集体计件)',
  `pw_plan_id` BIGINT NOT NULL COMMENT '计件方案ID',
  `pw_name` VARCHAR(100) DEFAULT NULL COMMENT '计件名称',
  `qualified_qty` DECIMAL(10,2) NOT NULL COMMENT '合格数量',
  `unqualified_qty` DECIMAL(10,2) DEFAULT 0 COMMENT '不合格数量',
  `unit_price` DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
  `total_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '计件总额',
  `confirm_status` CHAR(1) DEFAULT '0' COMMENT '确认状态(0待确认1已确认2已驳回)',
  `confirm_by` VARCHAR(64) DEFAULT NULL COMMENT '确认人',
  `confirm_time` DATETIME DEFAULT NULL COMMENT '确认时间',
  `source` CHAR(1) DEFAULT '0' COMMENT '来源(0手工录入1质检同步)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`pw_record_id`),
  UNIQUE KEY `uk_pw_record_no` (`pw_record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR计件报工表';

-- ========== 21. 社保公积金 ==========
DROP TABLE IF EXISTS `biz_hr_social_security`;
CREATE TABLE `biz_hr_social_security` (
  `ss_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '社保ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `year` INT NOT NULL COMMENT '年度',
  `base_amount` DECIMAL(10,2) NOT NULL COMMENT '缴费基数',
  `pension_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '养老单位比例',
  `pension_employee` DECIMAL(8,4) DEFAULT 0 COMMENT '养老个人比例',
  `medical_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '医疗单位比例',
  `medical_employee` DECIMAL(8,4) DEFAULT 0 COMMENT '医疗个人比例',
  `unemployment_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '失业单位比例',
  `unemployment_employee` DECIMAL(8,4) DEFAULT 0 COMMENT '失业个人比例',
  `injury_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '工伤单位比例',
  `maternity_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '生育单位比例',
  `fund_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '公积金单位比例',
  `fund_employee` DECIMAL(8,4) DEFAULT 0 COMMENT '公积金个人比例',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`ss_id`),
  KEY `idx_emp_year` (`employee_id`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR社保公积金表';

-- ========== 22. 个税参数表 ==========
DROP TABLE IF EXISTS `biz_hr_tax_rate`;
CREATE TABLE `biz_hr_tax_rate` (
  `tax_rate_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '税率ID',
  `level` INT NOT NULL COMMENT '级数',
  `min_amount` DECIMAL(12,2) NOT NULL COMMENT '下限',
  `max_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '上限',
  `rate` DECIMAL(5,4) NOT NULL COMMENT '税率',
  `quick_deduction` DECIMAL(10,2) NOT NULL COMMENT '速算扣除数',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常1停用)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`tax_rate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR个税税率表';

-- ========== 23. 专项附加扣除 ==========
DROP TABLE IF EXISTS `biz_hr_special_deduction`;
CREATE TABLE `biz_hr_special_deduction` (
  `sd_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '扣除ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `deduction_type` VARCHAR(30) NOT NULL COMMENT '扣除项目',
  `deduction_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '月扣除金额',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `expiry_date` DATE DEFAULT NULL COMMENT '失效日期',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`sd_id`),
  KEY `idx_emp` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR专项附加扣除表';

-- ========== 24. 薪酬发放批次 ==========
DROP TABLE IF EXISTS `biz_hr_payroll_batch`;
CREATE TABLE `biz_hr_payroll_batch` (
  `batch_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `batch_no` VARCHAR(30) NOT NULL COMMENT '批次编号',
  `year_month` VARCHAR(7) NOT NULL COMMENT '年月',
  `batch_name` VARCHAR(100) DEFAULT NULL COMMENT '批次名称',
  `pay_date` DATE DEFAULT NULL COMMENT '发放日期',
  `bank_name` VARCHAR(50) DEFAULT NULL COMMENT '发薪银行',
  `total_count` INT DEFAULT 0 COMMENT '总人数',
  `total_amount` DECIMAL(14,2) DEFAULT 0 COMMENT '总金额',
  `audit_status` CHAR(1) DEFAULT '0' COMMENT '审批状态(0待审1HR复核通过2总经理批准3已发放4已驳回)',
  `audit_by` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `bank_receipt_no` VARCHAR(50) DEFAULT NULL COMMENT '银行回单号',
  `sealed_status` CHAR(1) DEFAULT '0' COMMENT '封账状态(0未封1已封)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`batch_id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR薪酬发放批次表';

-- ========== 25. 证书台账 ==========
DROP TABLE IF EXISTS `biz_hr_certificate`;
CREATE TABLE `biz_hr_certificate` (
  `cert_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '证书ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `cert_type` VARCHAR(50) NOT NULL COMMENT '证书类别',
  `cert_name` VARCHAR(100) NOT NULL COMMENT '证书名称',
  `cert_no` VARCHAR(50) DEFAULT NULL COMMENT '证书编号',
  `issue_date` DATE DEFAULT NULL COMMENT '发证日期',
  `valid_from` DATE DEFAULT NULL COMMENT '有效起始日',
  `valid_to` DATE NOT NULL COMMENT '有效期至',
  `review_date` DATE DEFAULT NULL COMMENT '复审日期',
  `issue_org` VARCHAR(100) DEFAULT NULL COMMENT '发证机构',
  `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
  `remind_status` CHAR(1) DEFAULT '0' COMMENT '预警状态(0正常1临期2过期)',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0有效1失效2已注销)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`cert_id`),
  KEY `idx_emp` (`employee_id`),
  KEY `idx_valid_to` (`valid_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR证书台账表';

-- ========== 26. 职业健康体检 ==========
DROP TABLE IF EXISTS `biz_hr_health_check`;
CREATE TABLE `biz_hr_health_check` (
  `health_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '体检ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `employee_name` VARCHAR(50) DEFAULT NULL COMMENT '员工姓名',
  `employee_no` VARCHAR(30) DEFAULT NULL COMMENT '工号',
  `check_type` CHAR(1) NOT NULL COMMENT '体检类型(0岗前1在岗2离岗)',
  `check_date` DATE NOT NULL COMMENT '体检日期',
  `check_org` VARCHAR(100) DEFAULT NULL COMMENT '体检机构',
  `hazard_type` VARCHAR(50) DEFAULT NULL COMMENT '职业危害因素',
  `check_result` CHAR(1) DEFAULT '0' COMMENT '结论(0合格1不合格2疑似职业病3其他)',
  `conclusion` TEXT DEFAULT NULL COMMENT '体检结论',
  `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
  `next_check_date` DATE DEFAULT NULL COMMENT '下次体检日期',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`health_id`),
  KEY `idx_emp` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR职业健康体检表';

-- ========== 27. 劳务用工 ==========
DROP TABLE IF EXISTS `biz_hr_labor_worker`;
CREATE TABLE `biz_hr_labor_worker` (
  `labor_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '劳务工ID',
  `worker_name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `id_card` VARCHAR(100) DEFAULT NULL COMMENT '身份证号(加密)',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `worker_type` CHAR(1) DEFAULT '0' COMMENT '用工形态(0劳务派遣1季节临时工2实习生3退休返聘)',
  `dispatch_company` VARCHAR(100) DEFAULT NULL COMMENT '派遣公司',
  `bank_name` VARCHAR(50) DEFAULT NULL COMMENT '开户银行',
  `bank_account` VARCHAR(100) DEFAULT NULL COMMENT '银行卡号(加密)',
  `org_id` BIGINT DEFAULT NULL COMMENT '分配组织ID',
  `position_name` VARCHAR(100) DEFAULT NULL COMMENT '岗位',
  `start_date` DATE NOT NULL COMMENT '用工开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '用工结束日期',
  `pay_type` CHAR(1) DEFAULT '0' COMMENT '结算方式(0日薪1计件2月薪)',
  `daily_wage` DECIMAL(8,2) DEFAULT 0 COMMENT '日薪标准',
  `agreement_no` VARCHAR(30) DEFAULT NULL COMMENT '协议编号',
  `to_regular` CHAR(1) DEFAULT '0' COMMENT '是否转正(0否1是)',
  `employee_id` BIGINT DEFAULT NULL COMMENT '转正后员工ID',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0在用工1已结束2已转正)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`labor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR劳务用工表';

-- ========== 28. 临时工日结 ==========
DROP TABLE IF EXISTS `biz_hr_temp_daily_settlement`;
CREATE TABLE `biz_hr_temp_daily_settlement` (
  `settlement_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '结算ID',
  `settlement_no` VARCHAR(30) NOT NULL COMMENT '结算单号',
  `settlement_date` DATE NOT NULL COMMENT '结算日期',
  `labor_id` BIGINT NOT NULL COMMENT '劳务工ID',
  `worker_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `attend_days` DECIMAL(4,1) DEFAULT 0 COMMENT '出勤天数',
  `daily_wage` DECIMAL(8,2) DEFAULT 0 COMMENT '日薪',
  `piecework_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '计件金额',
  `total_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '结算总额',
  `pay_status` CHAR(1) DEFAULT '0' COMMENT '支付状态(0未付1已付)',
  `pay_date` DATE DEFAULT NULL COMMENT '支付日期',
  `pay_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`settlement_id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR临时工日结表';


-- HR模块菜单(自动生成)
SET NAMES utf8mb4;

-- 删除旧菜单(先删子菜单再删父菜单)
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'hr:%');
DELETE FROM sys_menu WHERE perms LIKE 'hr:%';
DELETE FROM sys_menu WHERE menu_name = '人力资源管理' AND parent_id = 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('人力资源管理', 0, 15, 'hr', NULL, 1, 0, 'M', '0', '0', '', 'people', 'admin', sysdate(), '人力资源管理目录');
SET @hrMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构', @hrMenuId, 1, 'organization', 'hr/organization/index', 1, 0, 'C', '0', '0', 'hr:organization:list', 'tree', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:remove', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('组织架构导出', @p, 5, '#', '', 1, 0, 'F', '0', '0', 'hr:organization:export', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理', @hrMenuId, 2, 'position', 'hr/position/index', 1, 0, 'C', '0', '0', 'hr:position:list', 'post', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:position:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:position:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:position:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('岗位管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:position:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案', @hrMenuId, 3, 'employee', 'hr/employee/index', 1, 0, 'C', '0', '0', 'hr:employee:list', 'user', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:remove', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('员工档案导出', @p, 5, '#', '', 1, 0, 'F', '0', '0', 'hr:employee:export', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理', @hrMenuId, 4, 'entry', 'hr/entry/index', 1, 0, 'C', '0', '0', 'hr:entry:list', 'guide', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('入职管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:entry:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理', @hrMenuId, 5, 'regular', 'hr/regular/index', 1, 0, 'C', '0', '0', 'hr:regular:list', 'checkbox', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('转正管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:regular:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理', @hrMenuId, 6, 'transfer', 'hr/transfer/index', 1, 0, 'C', '0', '0', 'hr:transfer:list', 'swap', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('调动管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:transfer:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理', @hrMenuId, 7, 'leave', 'hr/leave/index', 1, 0, 'C', '0', '0', 'hr:leave:list', 'exit-full', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('离职管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:leave:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同', @hrMenuId, 8, 'contract', 'hr/contract/index', 1, 0, 'C', '0', '0', 'hr:contract:list', 'documentation', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳动合同删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:contract:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理', @hrMenuId, 9, 'shift', 'hr/shift/index', 1, 0, 'C', '0', '0', 'hr:shift:list', 'time', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('班次管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:shift:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理', @hrMenuId, 10, 'schedule', 'hr/schedule/index', 1, 0, 'C', '0', '0', 'hr:schedule:list', 'date', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('排班管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:schedule:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录', @hrMenuId, 11, 'attendance', 'hr/attendance/index', 1, 0, 'C', '0', '0', 'hr:attendance:list', 'monitor', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤记录删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:attendance:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理', @hrMenuId, 12, 'leaveRequest', 'hr/leaveRequest/index', 1, 0, 'C', '0', '0', 'hr:leaveRequest:list', 'edit', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('请假管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveRequest:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理', @hrMenuId, 13, 'overtime', 'hr/overtime/index', 1, 0, 'C', '0', '0', 'hr:overtime:list', 'time-range', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('加班管理删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:overtime:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报', @hrMenuId, 14, 'monthly', 'hr/monthly/index', 1, 0, 'C', '0', '0', 'hr:monthly:list', 'chart', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('考勤月报删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:monthly:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额', @hrMenuId, 15, 'leaveBalance', 'hr/leaveBalance/index', 1, 0, 'C', '0', '0', 'hr:leaveBalance:list', 'edit', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveBalance:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveBalance:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveBalance:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('假期余额删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:leaveBalance:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典', @hrMenuId, 16, 'salaryItem', 'hr/salaryItem/index', 1, 0, 'C', '0', '0', 'hr:salaryItem:list', 'list', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪资项字典删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryItem:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪', @hrMenuId, 17, 'salaryPlan', 'hr/salaryPlan/index', 1, 0, 'C', '0', '0', 'hr:salaryPlan:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('定薪调薪删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:salaryPlan:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条', @hrMenuId, 18, 'payslip', 'hr/payslip/index', 1, 0, 'C', '0', '0', 'hr:payslip:list', 'documentation', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('工资条删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:payslip:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案', @hrMenuId, 19, 'pieceworkPlan', 'hr/pieceworkPlan/index', 1, 0, 'C', '0', '0', 'hr:piecework:list', 'component', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件方案删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工', @hrMenuId, 20, 'pieceworkRecord', 'hr/pieceworkRecord/index', 1, 0, 'C', '0', '0', 'hr:piecework:list', 'component', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('计件报工删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:piecework:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金', @hrMenuId, 21, 'socialSecurity', 'hr/socialSecurity/index', 1, 0, 'C', '0', '0', 'hr:socialSecurity:list', 'security', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('社保公积金删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数', @hrMenuId, 22, 'taxRate', 'hr/taxRate/index', 1, 0, 'C', '0', '0', 'hr:taxRate:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:taxRate:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:taxRate:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:taxRate:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('个税参数删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:taxRate:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除', @hrMenuId, 23, 'specialDeduction', 'hr/specialDeduction/index', 1, 0, 'C', '0', '0', 'hr:specialDeduction:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:specialDeduction:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:specialDeduction:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:specialDeduction:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('专项附加扣除删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:specialDeduction:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放', @hrMenuId, 24, 'payroll', 'hr/payroll/index', 1, 0, 'C', '0', '0', 'hr:payroll:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('薪酬发放删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:payroll:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账', @hrMenuId, 25, 'certificate', 'hr/certificate/index', 1, 0, 'C', '0', '0', 'hr:certificate:list', 'skill', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:certificate:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:certificate:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:certificate:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('证书台账删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:certificate:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康', @hrMenuId, 26, 'healthCheck', 'hr/healthCheck/index', 1, 0, 'C', '0', '0', 'hr:healthCheck:list', 'chart', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:healthCheck:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:healthCheck:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:healthCheck:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('职业健康删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:healthCheck:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工', @hrMenuId, 27, 'laborWorker', 'hr/laborWorker/index', 1, 0, 'C', '0', '0', 'hr:laborWorker:list', 'people', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:laborWorker:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:laborWorker:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:laborWorker:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('劳务用工删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:laborWorker:remove', '#', 'admin', sysdate(), '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结', @hrMenuId, 28, 'tempSettlement', 'hr/tempSettlement/index', 1, 0, 'C', '0', '0', 'hr:tempSettlement:list', 'money', 'admin', sysdate(), '');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:tempSettlement:query', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:tempSettlement:add', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:tempSettlement:edit', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('临时工日结删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:tempSettlement:remove', '#', 'admin', sysdate(), '');


-- ========== HR字典数据 ==========

-- 性别
DELETE FROM sys_dict_type WHERE dict_type = 'hr_gender';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('性别', 'hr_gender', '0', 'admin', sysdate(), '性别');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '男', '0', 'hr_gender', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '女', '1', 'hr_gender', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '未知', '2', 'hr_gender', '0', 'admin', sysdate(), '');

-- 婚姻状况
DELETE FROM sys_dict_type WHERE dict_type = 'hr_marital_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('婚姻状况', 'hr_marital_status', '0', 'admin', sysdate(), '婚姻状况');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '未婚', '0', 'hr_marital_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '已婚', '1', 'hr_marital_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '离异', '2', 'hr_marital_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '丧偶', '3', 'hr_marital_status', '0', 'admin', sysdate(), '');

-- 员工状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_employee_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('员工状态', 'hr_employee_status', '0', 'admin', sysdate(), '员工状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '在职', '0', 'hr_employee_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '试用期', '1', 'hr_employee_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '离职', '2', 'hr_employee_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '黑名单', '3', 'hr_employee_status', '0', 'admin', sysdate(), '');

-- 用工类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_employee_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('用工类型', 'hr_employee_type', '0', 'admin', sysdate(), '用工类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '正式', '0', 'hr_employee_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '劳务派遣', '1', 'hr_employee_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '临时工', '2', 'hr_employee_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '实习生', '3', 'hr_employee_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '退休返聘', '4', 'hr_employee_type', '0', 'admin', sysdate(), '');

-- 工时制度
DELETE FROM sys_dict_type WHERE dict_type = 'hr_work_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('工时制度', 'hr_work_type', '0', 'admin', sysdate(), '工时制度');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '标准工时', '0', 'hr_work_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '综合工时', '1', 'hr_work_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '不定时', '2', 'hr_work_type', '0', 'admin', sysdate(), '');

-- 组织层级
DELETE FROM sys_dict_type WHERE dict_type = 'hr_org_level';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('组织层级', 'hr_org_level', '0', 'admin', sysdate(), '组织层级');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '公司', '1', 'hr_org_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '分厂', '2', 'hr_org_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '部门', '3', 'hr_org_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '班组', '4', 'hr_org_level', '0', 'admin', sysdate(), '');

-- 岗位序列
DELETE FROM sys_dict_type WHERE dict_type = 'hr_position_seq';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('岗位序列', 'hr_position_seq', '0', 'admin', sysdate(), '岗位序列');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '管理', 'M', 'hr_position_seq', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '技术', 'P', 'hr_position_seq', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '技能', 'W', 'hr_position_seq', '0', 'admin', sysdate(), '');

-- 学历
DELETE FROM sys_dict_type WHERE dict_type = 'hr_edu_level';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('学历', 'hr_edu_level', '0', 'admin', sysdate(), '学历');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '博士', '1', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '硕士', '2', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '本科', '3', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '大专', '4', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '高中', '5', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (6, '中专', '6', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (7, '初中', '7', 'hr_edu_level', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (8, '其他', '8', 'hr_edu_level', '0', 'admin', sysdate(), '');

-- 合同类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_contract_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('合同类型', 'hr_contract_type', '0', 'admin', sysdate(), '合同类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '固定期限', '0', 'hr_contract_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '无固定期限', '1', 'hr_contract_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '以完成一定工作', '2', 'hr_contract_type', '0', 'admin', sysdate(), '');

-- 审批状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_audit_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('审批状态', 'hr_audit_status', '0', 'admin', sysdate(), '审批状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '待审', '0', 'hr_audit_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '通过', '1', 'hr_audit_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '驳回', '2', 'hr_audit_status', '0', 'admin', sysdate(), '');

-- 考勤状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_attendance_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('考勤状态', 'hr_attendance_status', '0', 'admin', sysdate(), '考勤状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '正常', '0', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '迟到', '1', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '早退', '2', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '旷工', '3', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '漏卡', '4', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (6, '请假', '5', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (7, '出差', '6', 'hr_attendance_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (8, '加班', '7', 'hr_attendance_status', '0', 'admin', sysdate(), '');

-- 假别
DELETE FROM sys_dict_type WHERE dict_type = 'hr_leave_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('假别', 'hr_leave_type', '0', 'admin', sysdate(), '假别');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '事假', '1', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '病假', '2', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '年假', '3', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '婚假', '4', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '产假', '5', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (6, '陪产假', '6', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (7, '丧假', '7', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (8, '工伤假', '8', 'hr_leave_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (9, '调休', '9', 'hr_leave_type', '0', 'admin', sysdate(), '');

-- 加班类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_overtime_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('加班类型', 'hr_overtime_type', '0', 'admin', sysdate(), '加班类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '工作日', '0', 'hr_overtime_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '休息日', '1', 'hr_overtime_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '法定节假日', '2', 'hr_overtime_type', '0', 'admin', sysdate(), '');

-- 薪资项类别
DELETE FROM sys_dict_type WHERE dict_type = 'hr_salary_item_category';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('薪资项类别', 'hr_salary_item_category', '0', 'admin', sysdate(), '薪资项类别');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '固定项', '0', 'hr_salary_item_category', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '考勤联动', '1', 'hr_salary_item_category', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '计件项', '2', 'hr_salary_item_category', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '手工项', '3', 'hr_salary_item_category', '0', 'admin', sysdate(), '');

-- 支付状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_pay_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('支付状态', 'hr_pay_status', '0', 'admin', sysdate(), '支付状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '未付', '0', 'hr_pay_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '已付', '1', 'hr_pay_status', '0', 'admin', sysdate(), '');

-- 证书类别
DELETE FROM sys_dict_type WHERE dict_type = 'hr_cert_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('证书类别', 'hr_cert_type', '0', 'admin', sysdate(), '证书类别');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '特种作业证', '0', 'hr_cert_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '健康证', '1', 'hr_cert_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '职业资格证', '2', 'hr_cert_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '特种设备证', '3', 'hr_cert_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '其他', '4', 'hr_cert_type', '0', 'admin', sysdate(), '');

-- 预警状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_remind_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('预警状态', 'hr_remind_status', '0', 'admin', sysdate(), '预警状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '正常', '0', 'hr_remind_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '临期', '1', 'hr_remind_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '过期', '2', 'hr_remind_status', '0', 'admin', sysdate(), '');

-- 体检类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_health_check_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('体检类型', 'hr_health_check_type', '0', 'admin', sysdate(), '体检类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '岗前', '0', 'hr_health_check_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '在岗', '1', 'hr_health_check_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '离岗', '2', 'hr_health_check_type', '0', 'admin', sysdate(), '');

-- 体检结论
DELETE FROM sys_dict_type WHERE dict_type = 'hr_health_check_result';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('体检结论', 'hr_health_check_result', '0', 'admin', sysdate(), '体检结论');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '合格', '0', 'hr_health_check_result', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '不合格', '1', 'hr_health_check_result', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '疑似职业病', '2', 'hr_health_check_result', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '其他', '3', 'hr_health_check_result', '0', 'admin', sysdate(), '');

-- 用工形态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_labor_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('用工形态', 'hr_labor_type', '0', 'admin', sysdate(), '用工形态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '劳务派遣', '0', 'hr_labor_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '季节临时工', '1', 'hr_labor_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '实习生', '2', 'hr_labor_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '退休返聘', '3', 'hr_labor_type', '0', 'admin', sysdate(), '');

-- 封账状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_sealed_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('封账状态', 'hr_sealed_status', '0', 'admin', sysdate(), '封账状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '未封', '0', 'hr_sealed_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '已封', '1', 'hr_sealed_status', '0', 'admin', sysdate(), '');

-- 经济补偿
DELETE FROM sys_dict_type WHERE dict_type = 'hr_compensation_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('经济补偿', 'hr_compensation_type', '0', 'admin', sysdate(), '经济补偿');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, 'N', '0', 'hr_compensation_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, 'N+1', '1', 'hr_compensation_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '2N', '2', 'hr_compensation_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '无', '3', 'hr_compensation_type', '0', 'admin', sysdate(), '');

-- 调动类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_transfer_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('调动类型', 'hr_transfer_type', '0', 'admin', sysdate(), '调动类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '部门', '1', 'hr_transfer_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '岗位', '2', 'hr_transfer_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '薪酬', '3', 'hr_transfer_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '班制', '4', 'hr_transfer_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '地点', '5', 'hr_transfer_type', '0', 'admin', sysdate(), '');

-- 离职类型
DELETE FROM sys_dict_type WHERE dict_type = 'hr_leave_emp_type';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('离职类型', 'hr_leave_emp_type', '0', 'admin', sysdate(), '离职类型');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '主动辞职', '0', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '协商解除', '1', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '违纪辞退', '2', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (4, '合同到期', '3', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (5, '退休', '4', 'hr_leave_emp_type', '0', 'admin', sysdate(), '');

-- 参保状态
DELETE FROM sys_dict_type WHERE dict_type = 'hr_social_security_status';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('参保状态', 'hr_social_security_status', '0', 'admin', sysdate(), '参保状态');
SET @dictId = LAST_INSERT_ID();
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (1, '在缴', '0', 'hr_social_security_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (2, '停缴', '1', 'hr_social_security_status', '0', 'admin', sysdate(), '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (3, '未参保', '2', 'hr_social_security_status', '0', 'admin', sysdate(), '');

-- ========== 预置班次数据 ==========
INSERT INTO biz_hr_shift (shift_code, shift_name, start_time, end_time, cross_midnight, work_hours, rest_hours, meal_minutes, night_shift, night_allowance, applicable_to, order_num, status, create_by, create_time) VALUES
('S001','长白班','08:00','17:00',0,8.0,1.0,60,0,0,'职能/常白班产线',1,'0','admin',sysdate()),
('S002','两班倒白','08:00','20:00',0,12.0,1.0,60,0,0,'食品加工旺季',2,'0','admin',sysdate()),
('S003','两班倒夜','20:00','08:00',1,12.0,1.0,60,1,20,'食品加工旺季',3,'0','admin',sysdate()),
('S004','三班早','08:00','16:00',0,8.0,0.5,30,0,0,'半连续产线',4,'0','admin',sysdate()),
('S005','三班中','16:00','24:00',0,8.0,0.5,30,0,0,'半连续产线',5,'0','admin',sysdate()),
('S006','三班夜','00:00','08:00',0,8.0,0.5,30,1,20,'半连续产线',6,'0','admin',sysdate());

-- ========== 预置薪资项 ==========
INSERT INTO biz_hr_salary_item (item_code, item_name, item_category, calc_rule, is_add, order_num, status, create_by, create_time) VALUES
('basic_salary','基本工资','0','定薪单确定',1,1,'0','admin',sysdate()),
('position_salary','岗位工资','0','按岗位-岗级标准表自动带出',1,2,'0','admin',sysdate()),
('skill_allowance','技能等级津贴','0','按员工技能等级查表',1,3,'0','admin',sysdate()),
('seniority_pay','工龄工资','0','按司龄阶梯计算',1,4,'0','admin',sysdate()),
('night_allowance','夜班津贴','1','当月夜班次数×每次标准',1,5,'0','admin',sysdate()),
('high_temp_allowance','高温津贴','1','高温月份高温岗位出勤天×日标准',1,6,'0','admin',sysdate()),
('hazard_allowance','有毒有害津贴','1','岗位标记职业危害等级对应档位',1,7,'0','admin',sysdate()),
('full_attendance','全勤奖','1','当月无迟到早退请假旷工',1,8,'0','admin',sysdate()),
('meal_allowance','餐补','1','出勤天×日标准',1,9,'0','admin',sysdate()),
('piecework_pay','计件工资','2','合格品数量×计件单价',1,10,'0','admin',sysdate()),
('overtime_pay','加班费','1','见计算规则',1,11,'0','admin',sysdate()),
('performance_bonus','绩效奖金','3','本期手工录入',1,12,'0','admin',sysdate()),
('absence_deduction','缺勤扣款','1','事假/病假按日工资扣减',0,13,'0','admin',sysdate()),
('social_insurance','社保公积金个人','1','按基数×个人比例',0,14,'0','admin',sysdate()),
('income_tax','个人所得税','1','累计预扣预缴',0,15,'0','admin',sysdate()),
('other_add','其他加项','3','逐月手工录入',1,16,'0','admin',sysdate()),
('other_deduct','其他扣项','3','逐月手工录入',0,17,'0','admin',sysdate());

-- ========== 预置个税税率表 ==========
INSERT INTO biz_hr_tax_rate (level, min_amount, max_amount, rate, quick_deduction, effective_date, status, create_by, create_time) VALUES
(1, 0, 36000, 0.03, 0, '2026-01-01', '0', 'admin', sysdate()),
(2, 36000, 144000, 0.10, 2520, '2026-01-01', '0', 'admin', sysdate()),
(3, 144000, 300000, 0.20, 16920, '2026-01-01', '0', 'admin', sysdate()),
(4, 300000, 420000, 0.25, 31920, '2026-01-01', '0', 'admin', sysdate()),
(5, 420000, 660000, 0.30, 52920, '2026-01-01', '0', 'admin', sysdate()),
(6, 660000, 960000, 0.35, 85920, '2026-01-01', '0', 'admin', sysdate()),
(7, 960000, NULL, 0.45, 181920, '2026-01-01', '0', 'admin', sysdate());

-- ========== 预置组织架构 ==========
INSERT INTO biz_hr_organization (org_code, org_name, parent_id, order_num, org_level, cost_center, attendance_org, status, create_by, create_time) VALUES
('ORG001','总公司',0,1,1,1,1,'0','admin',sysdate()),
('ORG002','一分厂',1,1,2,1,1,'0','admin',sysdate()),
('ORG003','二分厂',1,2,2,1,1,'0','admin',sysdate()),
('ORG004','综合管理部',1,3,3,1,0,'0','admin',sysdate()),
('ORG005','生产一车间',2,1,3,1,1,'0','admin',sysdate()),
('ORG006','生产二车间',3,1,3,1,1,'0','admin',sysdate());

-- ========== 给admin角色分配HR菜单权限 ==========
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'hr:%';

