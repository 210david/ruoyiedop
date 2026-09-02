-- 人力资源模块编号规则（接入 mk_number_rule 统一编码管理）
-- 单据类: reset_type='1' 按天重置, date_format=yyyyMMdd, seq_length=4, connector='-'
-- 基础资料类: reset_type='0' 不重置, 无日期, seq_length=4, connector='-'
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, connector, status, create_by, create_time, remark)
SELECT * FROM (
  SELECT 'hr_entry' rule_code, 'hr' module, '入职单号' rule_name, 'RZ' prefix, 'yyyyMMdd' date_format, '1' reset_type, 4 seq_length, 1 seq_start, 1 step, 0 current_seq, '-' connector, '0' status, 'admin' create_by, NOW() create_time, '人力资源-入职管理' remark
  UNION ALL SELECT 'hr_regular', 'hr', '转正单号', 'ZZ', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-转正管理'
  UNION ALL SELECT 'hr_transfer', 'hr', '调岗单号', 'DG', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-调岗管理'
  UNION ALL SELECT 'hr_leave', 'hr', '离职单号', 'LZ', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-离职管理'
  UNION ALL SELECT 'hr_leave_request', 'hr', '请假单号', 'QJ', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-请假申请'
  UNION ALL SELECT 'hr_overtime', 'hr', '加班单号', 'JB', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-加班管理'
  UNION ALL SELECT 'hr_contract', 'hr', '合同编号', 'HT', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-合同管理'
  UNION ALL SELECT 'hr_certificate', 'hr', '证照档案编号', 'ZD', '', '0', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-证照管理'
  UNION ALL SELECT 'hr_labor_agreement', 'hr', '劳务协议编号', 'LW', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-劳务人员'
  UNION ALL SELECT 'hr_payroll_batch', 'hr', '工资批次号', 'PC', 'yyyyMM', '3', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-工资核算'
  UNION ALL SELECT 'hr_piecework_plan', 'hr', '计件方案编码', 'PWFA', '', '0', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-计件方案'
  UNION ALL SELECT 'hr_piecework_record', 'hr', '计件记录单号', 'PWJL', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-计件记录'
  UNION ALL SELECT 'hr_temp_settlement', 'hr', '临时工结算单号', 'JS', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-临时工结算'
  UNION ALL SELECT 'hr_salary_plan', 'hr', '定薪单号', 'DX', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-薪资方案'
  UNION ALL SELECT 'hr_salary_item', 'hr', '薪资项目编码', 'XM', '', '0', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-薪资项目'
  UNION ALL SELECT 'hr_shift', 'hr', '班次编码', 'BC', '', '0', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-班次管理'
  UNION ALL SELECT 'hr_position', 'hr', '岗位编码', 'GW', '', '0', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-岗位管理'
  UNION ALL SELECT 'hr_organization', 'hr', '组织编码', 'ORGC', '', '0', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '人力资源-组织管理'
  UNION ALL SELECT 'hr_employee', 'hr', '员工工号', 'EMP', '', '0', 4, 1, 1, 46, '-', '0', 'admin', NOW(), '人力资源-员工管理（当前序号接续已有 EMP0001~EMP0046）'
) t
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule r WHERE r.rule_code = t.rule_code);
