// 用Node.js生成HR模块菜单SQL（避免文件被截断）
const fs = require('fs');
const path = require('path');

const menus = [
  // [name, path, component, icon, perms, hasExport]
  ['组织架构', 'organization', 'hr/organization/index', 'tree', 'hr:organization', true],
  ['岗位管理', 'position', 'hr/position/index', 'post', 'hr:position', false],
  ['员工档案', 'employee', 'hr/employee/index', 'user', 'hr:employee', true],
  ['入职管理', 'entry', 'hr/entry/index', 'guide', 'hr:entry', false],
  ['转正管理', 'regular', 'hr/regular/index', 'checkbox', 'hr:regular', false],
  ['调动管理', 'transfer', 'hr/transfer/index', 'swap', 'hr:transfer', false],
  ['离职管理', 'leave', 'hr/leave/index', 'exit-full', 'hr:leave', false],
  ['劳动合同', 'contract', 'hr/contract/index', 'documentation', 'hr:contract', false],
  ['班次管理', 'shift', 'hr/shift/index', 'time', 'hr:shift', false],
  ['排班管理', 'schedule', 'hr/schedule/index', 'date', 'hr:schedule', false],
  ['考勤记录', 'attendance', 'hr/attendance/index', 'monitor', 'hr:attendance', false],
  ['请假管理', 'leaveRequest', 'hr/leaveRequest/index', 'edit', 'hr:leaveRequest', false],
  ['加班管理', 'overtime', 'hr/overtime/index', 'time-range', 'hr:overtime', false],
  ['考勤月报', 'monthly', 'hr/monthly/index', 'chart', 'hr:monthly', false],
  ['假期余额', 'leaveBalance', 'hr/leaveBalance/index', 'edit', 'hr:leaveBalance', false],
  ['薪资项字典', 'salaryItem', 'hr/salaryItem/index', 'list', 'hr:salaryItem', false],
  ['定薪调薪', 'salaryPlan', 'hr/salaryPlan/index', 'money', 'hr:salaryPlan', false],
  ['工资条', 'payslip', 'hr/payslip/index', 'documentation', 'hr:payslip', false],
  ['计件方案', 'pieceworkPlan', 'hr/pieceworkPlan/index', 'component', 'hr:piecework', false],
  ['计件报工', 'pieceworkRecord', 'hr/pieceworkRecord/index', 'component', 'hr:piecework', false],
  ['社保公积金', 'socialSecurity', 'hr/socialSecurity/index', 'security', 'hr:socialSecurity', false],
  ['个税参数', 'taxRate', 'hr/taxRate/index', 'money', 'hr:taxRate', false],
  ['专项附加扣除', 'specialDeduction', 'hr/specialDeduction/index', 'money', 'hr:specialDeduction', false],
  ['薪酬发放', 'payroll', 'hr/payroll/index', 'money', 'hr:payroll', false],
  ['证书台账', 'certificate', 'hr/certificate/index', 'skill', 'hr:certificate', false],
  ['职业健康', 'healthCheck', 'hr/healthCheck/index', 'chart', 'hr:healthCheck', false],
  ['劳务用工', 'laborWorker', 'hr/laborWorker/index', 'people', 'hr:laborWorker', false],
  ['临时工日结', 'tempSettlement', 'hr/tempSettlement/index', 'money', 'hr:tempSettlement', false],
];

let sql = `-- HR模块菜单(自动生成)\nSET NAMES utf8mb4;\n\n-- 删除旧菜单(先删子菜单再删父菜单)\nDELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'hr:%');\nDELETE FROM sys_menu WHERE perms LIKE 'hr:%';\nDELETE FROM sys_menu WHERE menu_name = '人力资源管理' AND parent_id = 0;\n\n`;

// 一级菜单
sql += `INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('人力资源管理', 0, 15, 'hr', NULL, 1, 0, 'M', '0', '0', '', 'people', 'admin', sysdate(), '人力资源管理目录');\nSET @hrMenuId = LAST_INSERT_ID();\n\n`;

menus.forEach((m, i) => {
  const [name, mp, comp, icon, perms, hasExport] = m;
  const orderNum = i + 1;
  // C菜单
  sql += `INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('${name}', @hrMenuId, ${orderNum}, '${mp}', '${comp}', 1, 0, 'C', '0', '0', '${perms}:list', '${icon}', 'admin', sysdate(), '');\n`;
  sql += `SET @p = LAST_INSERT_ID();\n`;
  // F按钮
  sql += `INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('${name}查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', '${perms}:query', '#', 'admin', sysdate(), '');\n`;
  sql += `INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('${name}新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', '${perms}:add', '#', 'admin', sysdate(), '');\n`;
  sql += `INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('${name}修改', @p, 3, '#', '', 1, 0, 'F', '0', '0', '${perms}:edit', '#', 'admin', sysdate(), '');\n`;
  sql += `INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('${name}删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', '${perms}:remove', '#', 'admin', sysdate(), '');\n`;
  if (hasExport) {
    sql += `INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES ('${name}导出', @p, 5, '#', '', 1, 0, 'F', '0', '0', '${perms}:export', '#', 'admin', sysdate(), '');\n`;
  }
  sql += `\n`;
});

// ========== 字典数据 ==========
sql += `\n-- ========== HR字典数据 ==========\n`;

const dicts = [
  ['hr_gender', '性别', [['0','男'],['1','女'],['2','未知']]],
  ['hr_marital_status', '婚姻状况', [['0','未婚'],['1','已婚'],['2','离异'],['3','丧偶']]],
  ['hr_employee_status', '员工状态', [['0','在职'],['1','试用期'],['2','离职'],['3','黑名单']]],
  ['hr_employee_type', '用工类型', [['0','正式'],['1','劳务派遣'],['2','临时工'],['3','实习生'],['4','退休返聘']]],
  ['hr_work_type', '工时制度', [['0','标准工时'],['1','综合工时'],['2','不定时']]],
  ['hr_org_level', '组织层级', [['1','公司'],['2','分厂'],['3','部门'],['4','班组']]],
  ['hr_position_seq', '岗位序列', [['M','管理'],['P','技术'],['W','技能']]],
  ['hr_edu_level', '学历', [['1','博士'],['2','硕士'],['3','本科'],['4','大专'],['5','高中'],['6','中专'],['7','初中'],['8','其他']]],
  ['hr_contract_type', '合同类型', [['0','固定期限'],['1','无固定期限'],['2','以完成一定工作']]],
  ['hr_audit_status', '审批状态', [['0','待审'],['1','通过'],['2','驳回']]],
  ['hr_attendance_status', '考勤状态', [['0','正常'],['1','迟到'],['2','早退'],['3','旷工'],['4','漏卡'],['5','请假'],['6','出差'],['7','加班']]],
  ['hr_leave_type', '假别', [['1','事假'],['2','病假'],['3','年假'],['4','婚假'],['5','产假'],['6','陪产假'],['7','丧假'],['8','工伤假'],['9','调休']]],
  ['hr_overtime_type', '加班类型', [['0','工作日'],['1','休息日'],['2','法定节假日']]],
  ['hr_salary_item_category', '薪资项类别', [['0','固定项'],['1','考勤联动'],['2','计件项'],['3','手工项']]],
  ['hr_pay_status', '支付状态', [['0','未付'],['1','已付']]],
  ['hr_cert_type', '证书类别', [['0','特种作业证'],['1','健康证'],['2','职业资格证'],['3','特种设备证'],['4','其他']]],
  ['hr_remind_status', '预警状态', [['0','正常'],['1','临期'],['2','过期']]],
  ['hr_health_check_type', '体检类型', [['0','岗前'],['1','在岗'],['2','离岗']]],
  ['hr_health_check_result', '体检结论', [['0','合格'],['1','不合格'],['2','疑似职业病'],['3','其他']]],
  ['hr_labor_type', '用工形态', [['0','劳务派遣'],['1','季节临时工'],['2','实习生'],['3','退休返聘']]],
  ['hr_sealed_status', '封账状态', [['0','未封'],['1','已封']]],
  ['hr_compensation_type', '经济补偿', [['0','N'],['1','N+1'],['2','2N'],['3','无']]],
  ['hr_transfer_type', '调动类型', [['1','部门'],['2','岗位'],['3','薪酬'],['4','班制'],['5','地点']]],
  ['hr_leave_emp_type', '离职类型', [['0','主动辞职'],['1','协商解除'],['2','违纪辞退'],['3','合同到期'],['4','退休']]],
  ['hr_social_security_status', '参保状态', [['0','在缴'],['1','停缴'],['2','未参保']]],
];

dicts.forEach(d => {
  const [dtype, dname, items] = d;
  sql += `\n-- ${dname}\nDELETE FROM sys_dict_type WHERE dict_type = '${dtype}';\n`;
  sql += `INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES ('${dname}', '${dtype}', '0', 'admin', sysdate(), '${dname}');\n`;
  sql += `SET @dictId = LAST_INSERT_ID();\n`;
  items.forEach((item, idx) => {
    sql += `INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time, remark) VALUES (${idx+1}, '${item[1]}', '${item[0]}', '${dtype}', '0', 'admin', sysdate(), '');\n`;
  });
});

// ========== 预置数据 ==========
sql += `\n-- ========== 预置班次数据 ==========\n`;
sql += `INSERT INTO biz_hr_shift (shift_code, shift_name, start_time, end_time, cross_midnight, work_hours, rest_hours, meal_minutes, night_shift, night_allowance, applicable_to, order_num, status, create_by, create_time) VALUES\n`;
sql += `('S001','长白班','08:00','17:00',0,8.0,1.0,60,0,0,'职能/常白班产线',1,'0','admin',sysdate()),\n`;
sql += `('S002','两班倒白','08:00','20:00',0,12.0,1.0,60,0,0,'食品加工旺季',2,'0','admin',sysdate()),\n`;
sql += `('S003','两班倒夜','20:00','08:00',1,12.0,1.0,60,1,20,'食品加工旺季',3,'0','admin',sysdate()),\n`;
sql += `('S004','三班早','08:00','16:00',0,8.0,0.5,30,0,0,'半连续产线',4,'0','admin',sysdate()),\n`;
sql += `('S005','三班中','16:00','24:00',0,8.0,0.5,30,0,0,'半连续产线',5,'0','admin',sysdate()),\n`;
sql += `('S006','三班夜','00:00','08:00',0,8.0,0.5,30,1,20,'半连续产线',6,'0','admin',sysdate());\n`;

sql += `\n-- ========== 预置薪资项 ==========\n`;
sql += `INSERT INTO biz_hr_salary_item (item_code, item_name, item_category, calc_rule, is_add, order_num, status, create_by, create_time) VALUES\n`;
sql += `('basic_salary','基本工资','0','定薪单确定',1,1,'0','admin',sysdate()),\n`;
sql += `('position_salary','岗位工资','0','按岗位-岗级标准表自动带出',1,2,'0','admin',sysdate()),\n`;
sql += `('skill_allowance','技能等级津贴','0','按员工技能等级查表',1,3,'0','admin',sysdate()),\n`;
sql += `('seniority_pay','工龄工资','0','按司龄阶梯计算',1,4,'0','admin',sysdate()),\n`;
sql += `('night_allowance','夜班津贴','1','当月夜班次数×每次标准',1,5,'0','admin',sysdate()),\n`;
sql += `('high_temp_allowance','高温津贴','1','高温月份高温岗位出勤天×日标准',1,6,'0','admin',sysdate()),\n`;
sql += `('hazard_allowance','有毒有害津贴','1','岗位标记职业危害等级对应档位',1,7,'0','admin',sysdate()),\n`;
sql += `('full_attendance','全勤奖','1','当月无迟到早退请假旷工',1,8,'0','admin',sysdate()),\n`;
sql += `('meal_allowance','餐补','1','出勤天×日标准',1,9,'0','admin',sysdate()),\n`;
sql += `('piecework_pay','计件工资','2','合格品数量×计件单价',1,10,'0','admin',sysdate()),\n`;
sql += `('overtime_pay','加班费','1','见计算规则',1,11,'0','admin',sysdate()),\n`;
sql += `('performance_bonus','绩效奖金','3','本期手工录入',1,12,'0','admin',sysdate()),\n`;
sql += `('absence_deduction','缺勤扣款','1','事假/病假按日工资扣减',0,13,'0','admin',sysdate()),\n`;
sql += `('social_insurance','社保公积金个人','1','按基数×个人比例',0,14,'0','admin',sysdate()),\n`;
sql += `('income_tax','个人所得税','1','累计预扣预缴',0,15,'0','admin',sysdate()),\n`;
sql += `('other_add','其他加项','3','逐月手工录入',1,16,'0','admin',sysdate()),\n`;
sql += `('other_deduct','其他扣项','3','逐月手工录入',0,17,'0','admin',sysdate());\n`;

sql += `\n-- ========== 预置个税税率表 ==========\n`;
sql += `INSERT INTO biz_hr_tax_rate (level, min_amount, max_amount, rate, quick_deduction, effective_date, status, create_by, create_time) VALUES\n`;
sql += `(1, 0, 36000, 0.03, 0, '2026-01-01', '0', 'admin', sysdate()),\n`;
sql += `(2, 36000, 144000, 0.10, 2520, '2026-01-01', '0', 'admin', sysdate()),\n`;
sql += `(3, 144000, 300000, 0.20, 16920, '2026-01-01', '0', 'admin', sysdate()),\n`;
sql += `(4, 300000, 420000, 0.25, 31920, '2026-01-01', '0', 'admin', sysdate()),\n`;
sql += `(5, 420000, 660000, 0.30, 52920, '2026-01-01', '0', 'admin', sysdate()),\n`;
sql += `(6, 660000, 960000, 0.35, 85920, '2026-01-01', '0', 'admin', sysdate()),\n`;
sql += `(7, 960000, NULL, 0.45, 181920, '2026-01-01', '0', 'admin', sysdate());\n`;

sql += `\n-- ========== 预置组织架构 ==========\n`;
sql += `INSERT INTO biz_hr_organization (org_code, org_name, parent_id, order_num, org_level, cost_center, attendance_org, status, create_by, create_time) VALUES\n`;
sql += `('ORG001','总公司',0,1,1,1,1,'0','admin',sysdate()),\n`;
sql += `('ORG002','一分厂',1,1,2,1,1,'0','admin',sysdate()),\n`;
sql += `('ORG003','二分厂',1,2,2,1,1,'0','admin',sysdate()),\n`;
sql += `('ORG004','综合管理部',1,3,3,1,0,'0','admin',sysdate()),\n`;
sql += `('ORG005','生产一车间',2,1,3,1,1,'0','admin',sysdate()),\n`;
sql += `('ORG006','生产二车间',3,1,3,1,1,'0','admin',sysdate());\n`;

sql += `\n-- ========== 给admin角色分配HR菜单权限 ==========\n`;
sql += `INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'hr:%';\n`;

fs.writeFileSync(path.join(__dirname, 'hr_menu_dict.sql'), sql, 'utf8');
console.log('SQL generated: hr_menu_dict.sql, length:', sql.length);
