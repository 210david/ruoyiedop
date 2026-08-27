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
