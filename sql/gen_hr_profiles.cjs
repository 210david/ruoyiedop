// 为 sys_user 全部用户生成完整员工档案（biz_hr_employee）
// 用法: node gen_hr_profiles.cjs  -> 输出 hr_employee_profiles.sql
const fs = require('fs');

// ---------- 基础数据 ----------
const users = [
  { uid: 1,   name: '系统管理员', dept: 100, sex: '1', phone: '15888888888', email: 'ry@163.com',      pos: '总经理',     entry: '2022-01-10', edu: '2', school: '清华大学',       major: '计算机科学与技术', grad: '2008-06-30', politics: '中共党员', marital: '1', native: '北京市', city: '北京市海淀区' },
  { uid: 2,   name: '李华',      dept: 105, sex: '0', phone: '15666666666', email: 'ry@qq.com',       pos: '综合管理部经理', entry: '2022-03-01', edu: '3', school: '山东大学',       major: '行政管理',       grad: '2012-06-30', politics: '中共党员', marital: '1', native: '山东济南', city: '山东济南历下区' },
  { uid: 100, name: '张涛',      dept: 103, sex: '0', phone: '',            email: '',                pos: '研发部经理',   entry: '2022-06-15', edu: '2', school: '浙江大学',       major: '软件工程',       grad: '2013-06-30', politics: '群众',     marital: '1', native: '浙江杭州', city: '浙江杭州西湖区' },
  { uid: 110, name: '王伟',      dept: 103, sex: '0', phone: '13800001001', email: 'wangwei@dt.com',  pos: '软件工程师',   entry: '2023-02-20', edu: '3', school: '华中科技大学',   major: '计算机科学与技术', grad: '2016-06-30', politics: '群众',     marital: '1', native: '湖北武汉', city: '湖北武汉洪山区' },
  { uid: 111, name: '刘洋',      dept: 103, sex: '0', phone: '13800001002', email: 'liuyang@dt.com',  pos: '软件工程师',   entry: '2023-05-08', edu: '3', school: '西安电子科技大学', major: '软件工程',       grad: '2017-06-30', politics: '群众',     marital: '0', native: '陕西西安', city: '陕西西安雁塔区' },
  { uid: 112, name: '陈杰',      dept: 103, sex: '1', phone: '13800001003', email: 'chenjie@dt.com',  pos: '测试工程师',   entry: '2023-09-01', edu: '3', school: '成都理工大学',   major: '信息管理与信息系统', grad: '2018-06-30', politics: '群众',   marital: '0', native: '四川成都', city: '四川成都武侯区' },
  { uid: 113, name: '张敏',      dept: 104, sex: '1', phone: '13800002001', email: 'zhangmin@dt.com', pos: '市场部经理',   entry: '2022-08-15', edu: '3', school: '上海财经大学',   major: '市场营销',       grad: '2013-06-30', politics: '群众',     marital: '1', native: '上海市',   city: '上海市浦东新区' },
  { uid: 114, name: '赵磊',      dept: 104, sex: '0', phone: '13800002002', email: 'zhaolei@dt.com',  pos: '市场专员',     entry: '2024-03-18', edu: '4', school: '烟台大学',       major: '电子商务',       grad: '2019-06-30', politics: '群众',     marital: '0', native: '山东烟台', city: '山东烟台芝罘区' },
  { uid: 115, name: '孙丽',      dept: 104, sex: '1', phone: '13800002003', email: 'sunli@dt.com',    pos: '市场专员',     entry: '2024-06-01', edu: '3', school: '南京审计大学',   major: '市场营销',       grad: '2020-06-30', politics: '群众',     marital: '1', native: '江苏南京', city: '江苏南京玄武区' },
  { uid: 116, name: '周伟',      dept: 104, sex: '0', phone: '13800002004', email: 'zhouwei@dt.com',  pos: '市场专员',     entry: '2025-02-10', edu: '4', school: '深圳大学',       major: '广告学',         grad: '2021-06-30', politics: '群众',     marital: '0', native: '广东深圳', city: '广东深圳南山区' },
  { uid: 120, name: '吴霞',      dept: 105, sex: '1', phone: '13800003001', email: 'wuxia@dt.com',    pos: '人事专员',     entry: '2023-04-10', edu: '2', school: '中国人民大学',   major: '人力资源管理',   grad: '2015-06-30', politics: '中共党员', marital: '1', native: '北京市',   city: '北京市朝阳区' },
  { uid: 121, name: '郑宇',      dept: 105, sex: '0', phone: '13800003002', email: 'zhengyu@dt.com',  pos: '行政专员',     entry: '2023-07-24', edu: '3', school: '浙江大学',       major: '行政管理',       grad: '2018-06-30', politics: '群众',     marital: '0', native: '浙江宁波', city: '浙江杭州拱墅区' },
  { uid: 122, name: '冯玲',      dept: 105, sex: '1', phone: '13800003003', email: 'fengling@dt.com', pos: '行政前台',     entry: '2024-08-05', edu: '4', school: '杭州职业技术学院', major: '文秘',         grad: '2021-06-30', politics: '群众',     marital: '0', native: '浙江绍兴', city: '浙江杭州滨江区' },
  { uid: 123, name: '钱伟',      dept: 106, sex: '0', phone: '13800004001', email: 'qianwei@dt.com',  pos: '财务部经理',   entry: '2022-05-20', edu: '3', school: '中南财经政法大学', major: '会计学',       grad: '2012-06-30', politics: '群众',     marital: '1', native: '湖南长沙', city: '湖南长沙岳麓区' },
  { uid: 124, name: '孙静',      dept: 106, sex: '1', phone: '13800004002', email: 'sunjing@dt.com',  pos: '会计',         entry: '2023-11-06', edu: '3', school: '南京财经大学',   major: '财务管理',       grad: '2017-06-30', politics: '群众',     marital: '1', native: '江苏苏州', city: '江苏南京鼓楼区' },
  { uid: 125, name: '吴昊',      dept: 106, sex: '0', phone: '13800004003', email: 'wuhao@dt.com',    pos: '出纳',         entry: '2024-09-12', edu: '4', school: '成都学院',       major: '会计电算化',     grad: '2022-06-30', politics: '群众',     marital: '0', native: '四川绵阳', city: '四川成都成华区' },
  { uid: 126, name: '林鹏',      dept: 107, sex: '0', phone: '13800005001', email: 'linpeng@dt.com',  pos: '人事部经理',   entry: '2022-04-11', edu: '3', school: '武汉大学',       major: '人力资源管理',   grad: '2013-06-30', politics: '中共党员', marital: '1', native: '湖北武汉', city: '湖北武汉武昌区' },
  { uid: 127, name: '高洁',      dept: 107, sex: '1', phone: '13800005002', email: 'gaojie@dt.com',   pos: '招聘专员',     entry: '2023-06-19', edu: '3', school: '山东师范大学',   major: '应用心理学',     grad: '2018-06-30', politics: '群众',     marital: '1', native: '山东青岛', city: '山东青岛市南区' },
  { uid: 128, name: '徐磊',      dept: 107, sex: '0', phone: '13800005003', email: 'xulei@dt.com',    pos: '培训专员',     entry: '2024-04-22', edu: '3', school: '烟台大学',       major: '人力资源管理',   grad: '2019-06-30', politics: '群众',     marital: '0', native: '山东潍坊', city: '山东烟台福山区' },
  { uid: 129, name: '胡颖',      dept: 107, sex: '1', phone: '13800005004', email: 'huying@dt.com',   pos: '薪酬专员',     entry: '2025-03-03', edu: '3', school: '青岛大学',       major: '劳动与社会保障', grad: '2021-06-30', politics: '群众',     marital: '0', native: '山东临沂', city: '山东青岛市北区' },
  { uid: 133, name: '李玲',      dept: 200, sex: '1', phone: '13800006001', email: 'liling@dt.com',   pos: '生产部经理',   entry: '2022-07-01', edu: '3', school: '合肥工业大学',   major: '机械设计制造及其自动化', grad: '2013-06-30', politics: '群众', marital: '1', native: '安徽合肥', city: '安徽合肥蜀山区' },
  { uid: 134, name: '马杰',      dept: 200, sex: '0', phone: '13800006002', email: 'majie@dt.com',    pos: '车间主任',     entry: '2023-03-13', edu: '4', school: '烟台职业学院',   major: '机电一体化',     grad: '2015-06-30', politics: '群众',     marital: '1', native: '山东淄博', city: '山东烟台开发区' },
  { uid: 135, name: '唐宇',      dept: 200, sex: '0', phone: '13800006003', email: 'tangyu@dt.com',   pos: '生产计划员',   entry: '2024-05-27', edu: '3', school: '山东交通学院',   major: '工业工程',       grad: '2020-06-30', politics: '群众',     marital: '0', native: '山东枣庄', city: '山东烟台莱山区' },
  { uid: 136, name: '许智',      dept: 200, sex: '1', phone: '13800006004', email: 'xuzhi@dt.com',    pos: '设备技术员',   entry: '2025-06-16', edu: '4', school: '烟台工程职业技术学院', major: '数控技术', grad: '2022-06-30', politics: '群众',     marital: '0', native: '山东济宁', city: '山东烟台牟平区' },
  { uid: 140, name: '曹燕',      dept: 101, sex: '1', phone: '13800007001', email: 'caoyan@dt.com',   pos: '质量部经理',   entry: '2022-02-14', edu: '3', school: '江南大学',       major: '食品质量与安全', grad: '2013-06-30', politics: '群众',     marital: '1', native: '江苏无锡', city: '江苏无锡滨湖区' },
  { uid: 141, name: '丁辉',      dept: 101, sex: '0', phone: '13800007002', email: 'dinghui@dt.com',  pos: '质量工程师',   entry: '2023-08-21', edu: '3', school: '齐鲁工业大学',   major: '食品科学',       grad: '2016-06-30', politics: '群众',     marital: '1', native: '山东德州', city: '山东烟台福山区' },
  { uid: 142, name: '谢斌',      dept: 101, sex: '0', phone: '13800007003', email: 'xiebin@dt.com',   pos: '质检员',       entry: '2024-10-09', edu: '4', school: '烟台职业学院',   major: '食品检测技术',   grad: '2021-06-30', politics: '群众',     marital: '0', native: '山东聊城', city: '山东烟台开发区' },
  { uid: 143, name: '杨华',      dept: 105, sex: '0', phone: '13800003004', email: 'yanghua@dt.com',  pos: '行政专员',     entry: '2024-11-18', edu: '3', school: '鲁东大学',       major: '汉语言文学',     grad: '2019-06-30', politics: '群众',     marital: '0', native: '山东泰安', city: '山东烟台芝罘区' },
  { uid: 144, name: '何静',      dept: 105, sex: '1', phone: '13800003005', email: 'hejing@dt.com',   pos: '人事专员',     entry: '2025-04-07', edu: '3', school: '鲁东大学',       major: '人力资源管理',   grad: '2021-06-30', politics: '群众',     marital: '0', native: '山东威海', city: '山东烟台莱山区' },
  { uid: 145, name: '罗强',      dept: 105, sex: '0', phone: '13800003006', email: 'luoqiang@dt.com', pos: '后勤专员',     entry: '2025-07-21', edu: '5', school: '烟台工贸学校',   major: '汽车维修',       grad: '2014-06-30', politics: '群众',     marital: '1', native: '山东菏泽', city: '山东烟台开发区' },
  { uid: 146, name: 'WMS经理测试员', dept: 100, sex: '0', phone: '', email: '',           pos: '仓储经理',     entry: '2023-01-30', edu: '3', school: '山东大学',       major: '物流管理',       grad: '2015-06-30', politics: '群众',     marital: '1', native: '山东济南', city: '山东烟台莱山区' },
  { uid: 147, name: '入库测试员',    dept: 100, sex: '0', phone: '', email: '',           pos: '仓储专员',     entry: '2023-10-16', edu: '4', school: '烟台职业学院',   major: '物流管理',       grad: '2020-06-30', politics: '群众',     marital: '0', native: '山东东营', city: '山东烟台福山区' },
  { uid: 148, name: '出库测试员',    dept: 100, sex: '0', phone: '', email: '',           pos: '仓储专员',     entry: '2023-10-16', edu: '4', school: '烟台职业学院',   major: '物流管理',       grad: '2020-06-30', politics: '群众',     marital: '0', native: '山东滨州', city: '山东烟台开发区' },
  { uid: 149, name: '盘点测试员',    dept: 100, sex: '0', phone: '', email: '',           pos: '仓储专员',     entry: '2024-02-26', edu: '4', school: '烟台工程职业技术学院', major: '会计电算化', grad: '2021-06-30', politics: '群众', marital: '0', native: '山东日照', city: '山东烟台牟平区' },
  { uid: 150, name: '查询测试员',    dept: 100, sex: '0', phone: '', email: '',           pos: '仓储专员',     entry: '2024-02-26', edu: '4', school: '烟台职业学院',   major: '计算机应用技术', grad: '2021-06-30', politics: '群众',     marital: '0', native: '山东临沂', city: '山东烟台芝罘区' },
  { uid: 151, name: '设备管理员测试', dept: 100, sex: '0', phone: '', email: '',          pos: '设备管理员',   entry: '2023-04-03', edu: '3', school: '青岛科技大学',   major: '机械工程',       grad: '2016-06-30', politics: '群众',     marital: '1', native: '山东青岛', city: '山东烟台莱山区' },
  { uid: 152, name: '维修工程师测试', dept: 100, sex: '0', phone: '', email: '',          pos: '维修工程师',   entry: '2023-04-03', edu: '4', school: '烟台职业学院',   major: '机电一体化',     grad: '2018-06-30', politics: '群众',     marital: '1', native: '山东潍坊', city: '山东烟台开发区' },
  { uid: 153, name: '操作员测试',    dept: 100, sex: '0', phone: '', email: '',           pos: '设备操作员',   entry: '2024-07-15', edu: '6', school: '烟台工贸学校',   major: '数控技术',       grad: '2019-06-30', politics: '群众',     marital: '0', native: '山东聊城', city: '山东烟台福山区' },
  { uid: 154, name: '车间主管测试',  dept: 100, sex: '0', phone: '', email: '',           pos: '车间主管',     entry: '2022-11-21', edu: '3', school: '山东交通学院',   major: '机械设计制造及其自动化', grad: '2014-06-30', politics: '群众', marital: '1', native: '山东泰安', city: '山东烟台牟平区' },
  { uid: 155, name: '张测试',        dept: 100, sex: '0', phone: '', email: '',           pos: '测试专员',     entry: '2025-09-01', edu: '3', school: '鲁东大学',       major: '计算机科学与技术', grad: '2023-06-30', politics: '群众',   marital: '0', native: '山东烟台', city: '山东烟台莱山区' },
  { uid: 156, name: '营销总监测试',  dept: 104, sex: '0', phone: '13800000000', email: 't@test.com',     pos: '营销总监',   entry: '2022-06-06', edu: '2', school: '复旦大学',       major: '工商管理',       grad: '2010-06-30', politics: '中共党员', marital: '1', native: '上海市',   city: '山东烟台莱山区' },
  { uid: 157, name: '销售经理测试',  dept: 104, sex: '0', phone: '13800000001', email: 'mk_mgr01@test.com', pos: '销售经理', entry: '2023-02-27', edu: '3', school: '山东财经大学',   major: '市场营销',       grad: '2014-06-30', politics: '群众',   marital: '1', native: '山东淄博', city: '山东烟台福山区' },
  { uid: 158, name: '销售代表测试1', dept: 104, sex: '0', phone: '13800000002', email: 'mk_sales01@test.com', pos: '销售代表', entry: '2024-06-17', edu: '3', school: '鲁东大学',   major: '国际经济与贸易', grad: '2020-06-30', politics: '群众',   marital: '0', native: '山东烟台', city: '山东烟台芝罘区' },
  { uid: 159, name: '销售代表测试2', dept: 104, sex: '0', phone: '13800000003', email: 'mk_sales02@test.com', pos: '销售代表', entry: '2024-06-17', edu: '3', school: '临沂大学',   major: '市场营销',       grad: '2020-06-30', politics: '群众',   marital: '0', native: '山东临沂', city: '山东烟台开发区' },
  { uid: 160, name: '财务专员测试',  dept: 104, sex: '0', phone: '13800000004', email: 'mk_fin01@test.com', pos: '财务专员', entry: '2025-01-13', edu: '3', school: '山东工商学院',   major: '会计学',       grad: '2019-06-30', politics: '群众',   marital: '1', native: '山东潍坊', city: '山东烟台牟平区' },
  { uid: 161, name: '市场专员测试',  dept: 104, sex: '0', phone: '13800000005', email: 'mkt01@test.com', pos: '市场专员',   entry: '2025-08-04', edu: '4', school: '烟台理工学院',   major: '广告学',       grad: '2022-06-30', politics: '群众',   marital: '0', native: '山东威海', city: '山东烟台莱山区' },
];

// ---------- 工具函数 ----------
const regions = ['370602', '370202', '320102', '440304', '510104', '330102', '420106', '610102', '110101', '310101'];
const banks = ['中国工商银行', '中国建设银行', '中国农业银行', '中国银行', '招商银行'];
const emergencySurnames = ['王', '李', '张', '刘', '陈', '杨', '赵', '周'];
const weight = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
const checkMap = '10X98765432';

function idCard(uid, u, i) {
  const region = regions[i % regions.length];
  const y = 1975 + (uid % 26);           // 1975~2000
  const m = String(1 + (uid % 12)).padStart(2, '0');
  const d = String(1 + (uid * 3) % 28).padStart(2, '0');
  let seq = String(100 + i * 2 + (u.sex === '0' ? 1 : 0)).padStart(3, '0'); // 17位: 男奇女偶
  const body = region + y + m + d + seq;
  let sum = 0;
  for (let k = 0; k < 17; k++) sum += Number(body[k]) * weight[k];
  return body + checkMap[sum % 11];
}

function yearsSince(dateStr) { return Math.max(1, Math.floor((Date.now() - new Date(dateStr)) / (365.25 * 86400000))); }
function addMonths(dateStr, n) {
  const d = new Date(dateStr); d.setMonth(d.getMonth() + n);
  return d.toISOString().slice(0, 10);
}
function q(s) { return s == null || s === '' ? 'NULL' : `'${String(s).replace(/'/g, "''")}'`; }
function bankAcc(seed) {
  let s = '6222';
  let x = seed * 2654435761 % 100000000;
  for (let k = 0; k < 4; k++) { s += String(x % 100000000).padStart(8, '0'); x = (x * 1103515245 + 12345) % 2147483647; }
  return s.slice(0, 19);
}
function phoneGen(seed) { let s = '138'; let x = seed * 7919; for (let k = 0; k < 8; k++) { s += x % 10; x = Math.floor(x / 10) + seed * (k + 3); } return s.slice(0, 11); }

// ---------- 生成 SQL ----------
const rows = [];
users.forEach((u, i) => {
  const idc = idCard(u.uid, u, i);
  const birth = `${1975 + (u.uid % 26)}-${String(1 + (u.uid % 12)).padStart(2, '0')}-${String(1 + (u.uid * 3) % 28).padStart(2, '0')}`;
  const empNo = 'EMP' + String(u.uid).padStart(4, '0');
  const email = u.email || ('user' + u.uid + '@dt.com');
  const phone = u.phone || phoneGen(u.uid);
  const emName = emergencySurnames[i % emergencySurnames.length] + ['建国', '秀英', '志强', '丽娟', '永强', '桂花', '建军', '玉梅'][i % 8];
  const emPhone = '139' + String(10000000 + ((u.uid * 1234567) % 89999999));
  const socialYears = yearsSince(u.entry) + (u.uid % 3);
  const companyYears = yearsSince(u.entry);
  const regular = addMonths(u.entry, 3);
  const bank = banks[i % banks.length];
  const values = [
    q(empNo), q(u.name), q(u.sex), q(idc), q(birth), q('汉族'), q(u.politics), q(u.marital),
    q(phone), q(email), q(emName), q(emPhone), q(u.native), q(u.city + '鼎泰路88号'),
    q(u.edu), q(u.school), q(u.major), q(u.grad),
    String(socialYears), String(companyYears),
    String(u.dept), 'NULL', q(u.pos),
    (u.pos.includes('经理') || u.pos.includes('主管') || u.pos.includes('总监') || u.pos.includes('主任')) ? "'M'" : (u.pos.includes('工程师') || u.pos.includes('技术')) ? "'P'" : "'W'",
    "'0'", "'0'", q(u.entry), q(regular), 'NULL', "'0'",
    q(bank), q(bankAcc(u.uid)), q(idc), "'0'", q('HF' + String(20260000000 + u.uid * 137)), 'NULL', "'0'", 'NULL',
    String(u.uid), 'NULL', "'0'", "'0'",
    q('admin'), 'NOW()', 'NULL', 'NULL',
    q('关联系统用户 user_id=' + u.uid)
  ];
  rows.push(values.join(', '));
});

const cols = 'employee_no, employee_name, gender, id_card, birthday, nation, politics_status, marital_status, phone, email, emergency_contact, emergency_phone, native_place, address, edu_level, graduation_school, major, graduation_date, social_work_years, company_work_years, org_id, position_id, position_name, skill_level, work_type, employee_type, entry_date, regular_date, leave_date, employee_status, bank_name, bank_account, social_security_no, social_security_status, house_fund_no, leave_reason, blacklist_flag, blacklist_reason, user_id, photo, status, del_flag, create_by, create_time, update_by, update_time, remark';

const sql = `-- 人力资源模块：按系统用户生成完整员工档案（46人）
-- 生成时间: ${new Date().toLocaleString('zh-CN')}
-- 说明: org_id 关联 sys_dept.dept_id, user_id 关联 sys_user.user_id
-- 注意: 请仅在无有效档案数据时执行一次（重复执行会产生重复档案）
INSERT INTO biz_hr_employee (${cols})
${rows.map(r => 'SELECT ' + r).join(' UNION ALL\n')};
`;

fs.writeFileSync(__dirname + '/hr_employee_profiles.sql', sql, 'utf8');
console.log('已生成 ' + users.length + ' 条档案 SQL -> hr_employee_profiles.sql');
