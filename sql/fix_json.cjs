// Fix hr_entities.json: truncate at the broken HrPayrollBatch entity, then add complete HrPayrollBatch + 4 missing entities
const fs = require('fs');
const file = __dirname + '/hr_entities.json';
const raw = fs.readFileSync(file, 'utf8');

// Find the last valid complete entity block boundary
// The JSON is truncated at "auditBy":"audit  -- mid-way through HrPayrollBatch fields
// We need to:
// 1. Remove everything from HrPayrollBatch onward
// 2. Add complete HrPayrollBatch + HrCertificate + HrHealthCheck + HrLaborWorker + HrTempDailySettlement
// 3. Close the array and object

// Find the position where HrPayrollBatch starts
const idx = raw.indexOf('"cls": "HrPayrollBatch"');
if (idx === -1) { console.error('HrPayrollBatch not found'); process.exit(1); }

// Go back to find the opening brace of this entity
let entityStart = raw.lastIndexOf('{', idx);
// Remove the trailing comma before it
let prefix = raw.substring(0, entityStart).trimEnd();
if (prefix.endsWith(',')) prefix = prefix.slice(0, -1);
prefix += ',\n    ';

// Build the fixed entities
const newEntities = [
  {
    "cls": "HrPayrollBatch", "table": "biz_hr_payroll_batch", "path": "payroll", "perms": "hr:payroll", "title": "薪酬发放",
    "pk": "batchId", "pkCol": "batch_id", "pkType": "Long",
    "fields": [
      ["batchNo","batch_no","String","批次编号",true],
      ["yearMonth","year_month","String","年月",true],
      ["batchName","batch_name","String","批次名称",false],
      ["payDate","pay_date","Date","发放日期",false],
      ["bankName","bank_name","String","发薪银行",false],
      ["totalCount","total_count","Integer","总人数",false],
      ["totalAmount","total_amount","BigDecimal","总金额",false],
      ["auditStatus","audit_status","String","审批状态",false],
      ["auditBy","audit_by","String","审批人",false],
      ["auditTime","audit_time","Date","审批时间",false],
      ["auditRemark","audit_remark","String","审批意见",false],
      ["bankReceiptNo","bank_receipt_no","String","银行回单号",false],
      ["sealedStatus","sealed_status","String","封账状态",false],
      ["delFlag","del_flag","String","删除标志",false]
    ]
  },
  {
    "cls": "HrCertificate", "table": "biz_hr_certificate", "path": "certificate", "perms": "hr:certificate", "title": "证书台账",
    "pk": "certId", "pkCol": "cert_id", "pkType": "Long",
    "fields": [
      ["employeeId","employee_id","Long","员工ID",true],
      ["employeeName","employee_name","String","员工姓名",false],
      ["employeeNo","employee_no","String","工号",false],
      ["certType","cert_type","String","证书类别",true],
      ["certName","cert_name","String","证书名称",true],
      ["certNo","cert_no","String","证书编号",false],
      ["issueDate","issue_date","Date","发证日期",false],
      ["validFrom","valid_from","Date","有效起始日",false],
      ["validTo","valid_to","Date","有效期至",true],
      ["reviewDate","review_date","Date","复审日期",false],
      ["issueOrg","issue_org","String","发证机构",false],
      ["attachmentUrl","attachment_url","String","附件URL",false],
      ["remindStatus","remind_status","String","预警状态",false],
      ["status","status","String","状态",false],
      ["delFlag","del_flag","String","删除标志",false]
    ]
  },
  {
    "cls": "HrHealthCheck", "table": "biz_hr_health_check", "path": "healthCheck", "perms": "hr:healthCheck", "title": "职业健康体检",
    "pk": "healthId", "pkCol": "health_id", "pkType": "Long",
    "fields": [
      ["employeeId","employee_id","Long","员工ID",true],
      ["employeeName","employee_name","String","员工姓名",false],
      ["employeeNo","employee_no","String","工号",false],
      ["checkType","check_type","String","体检类型",true],
      ["checkDate","check_date","Date","体检日期",true],
      ["checkOrg","check_org","String","体检机构",false],
      ["hazardType","hazard_type","String","职业危害因素",false],
      ["checkResult","check_result","String","结论",false],
      ["conclusion","conclusion","String","体检结论",false],
      ["attachmentUrl","attachment_url","String","附件URL",false],
      ["nextCheckDate","next_check_date","Date","下次体检日期",false],
      ["delFlag","del_flag","String","删除标志",false]
    ]
  },
  {
    "cls": "HrLaborWorker", "table": "biz_hr_labor_worker", "path": "laborWorker", "perms": "hr:laborWorker", "title": "劳务用工",
    "pk": "laborId", "pkCol": "labor_id", "pkType": "Long",
    "fields": [
      ["workerName","worker_name","String","姓名",true],
      ["idCard","id_card","String","身份证号",false],
      ["phone","phone","String","联系电话",false],
      ["workerType","worker_type","String","用工形态",false],
      ["dispatchCompany","dispatch_company","String","派遣公司",false],
      ["bankName","bank_name","String","开户银行",false],
      ["bankAccount","bank_account","String","银行卡号",false],
      ["orgId","org_id","Long","分配组织ID",false],
      ["positionName","position_name","String","岗位",false],
      ["startDate","start_date","Date","用工开始日期",true],
      ["endDate","end_date","Date","用工结束日期",false],
      ["payType","pay_type","String","结算方式",false],
      ["dailyWage","daily_wage","BigDecimal","日薪标准",false],
      ["agreementNo","agreement_no","String","协议编号",false],
      ["toRegular","to_regular","String","是否转正",false],
      ["employeeId","employee_id","Long","转正后员工ID",false],
      ["status","status","String","状态",false],
      ["delFlag","del_flag","String","删除标志",false]
    ]
  },
  {
    "cls": "HrTempDailySettlement", "table": "biz_hr_temp_daily_settlement", "path": "tempSettlement", "perms": "hr:tempSettlement", "title": "临时工日结",
    "pk": "settlementId", "pkCol": "settlement_id", "pkType": "Long",
    "fields": [
      ["settlementNo","settlement_no","String","结算单号",true],
      ["settlementDate","settlement_date","Date","结算日期",true],
      ["laborId","labor_id","Long","劳务工ID",true],
      ["workerName","worker_name","String","姓名",false],
      ["attendDays","attend_days","BigDecimal","出勤天数",false],
      ["dailyWage","daily_wage","BigDecimal","日薪",false],
      ["pieceworkAmount","piecework_amount","BigDecimal","计件金额",false],
      ["totalAmount","total_amount","BigDecimal","结算总额",false],
      ["payStatus","pay_status","String","支付状态",false],
      ["payDate","pay_date","Date","支付日期",false],
      ["payMethod","pay_method","String","支付方式",false],
      ["delFlag","del_flag","String","删除标志",false]
    ]
  }
];

// Build JSON string manually
let jsonStr = prefix;
for (let i = 0; i < newEntities.length; i++) {
  jsonStr += JSON.stringify(newEntities[i], null, 4);
  if (i < newEntities.length - 1) jsonStr += ',';
  jsonStr += '\n    ';
}
jsonStr += '\n  ]\n}';

fs.writeFileSync(file, jsonStr, 'utf8');
console.log('Fixed! Total entities now:', JSON.parse(jsonStr).entities.length);
JSON.parse(jsonStr).entities.forEach(e => console.log(' -', e.cls, '| fields:', e.fields.length));
