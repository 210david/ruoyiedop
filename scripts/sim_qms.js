/**
 * 质量管理模块数据模拟：CAPA(含5Why/效果验证) / 质量文档 / 缺陷代码 / 客诉 / 供应商质量评价
 * / 抽样方案 / 物料质量属性 / 车间产线 / 批次谱系 / SN / 内审 / 管理评审
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const rnd = (a, b) => a + Math.random() * (b - a);
const ri = (a, b) => Math.floor(rnd(a, b + 1));

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  const tables = ['qms_capa', 'qms_capa_root_cause', 'qms_capa_effectiveness', 'qms_doc', 'qms_defect_code',
    'qms_complaint', 'qms_supplier_eval', 'qms_aql_plan', 'qms_material_attr', 'qms_workshop',
    'qms_lot_genealogy', 'qms_trace_sn', 'qms_audit_plan', 'qms_audit_item', 'qms_audit_check_item', 'qms_mr'];
  for (const t of tables) await exec(`DELETE FROM \`${t}\``);

  // ---------- 1. CAPA（源自 5 张 NCR） ----------
  const ncrs = await q(`SELECT * FROM qms_ncr WHERE del_flag='0' ORDER BY ncr_id`);
  const capaDetail = {
    1: {
      title: '乌猪培根真空包装漏气问题整改',
      problem: 'NCR20261027001 批次 BG20261027A 检出 2 袋真空包装漏气，存在二次污染风险。',
      interim: '对同批次 500 袋全检，隔离漏气品 2 袋作报废处理，其余重新抽真空。',
      why: ['包装袋封口存在虚封', '热封温度设定偏低（145℃）', '温控参数被操作工手动下调', '换型后未执行参数复核', '首件确认未覆盖热封温度项'],
      root: '换型后未按《真空包装作业指导书》复核热封温度，首件确认表单缺少温度记录项。',
      perm: '修订《真空包装作业指导书》QMS-SOP-018，热封温度固化为 155±5℃并加密码锁定；首件确认表增加温度、真空度记录项。',
      prevent: '将热封温度、真空度纳入每班首件确认强制项，班长签字确认后放行。'
    },
    2: {
      title: '鸡枞火腿酱净含量不足整改',
      problem: 'NCR20261027002 批次 HJ20261026A 抽检 5 瓶净含量低于标示值 220g。',
      interim: '库存 3000 瓶按 2% 抽检复称，超差 42 瓶返工补装。',
      why: ['灌装量波动', '灌装机计量泵磨损', '计量泵未纳入预防性维护', '设备点检表无计量项', '点检标准未覆盖灌装设备'],
      root: '灌装机计量泵密封圈磨损导致灌装量衰减，设备点检标准未包含计量精度校验。',
      perm: '更换计量泵密封圈，增加每班首件称重（3 瓶）与每周计量校验，纳入设备点检表。',
      prevent: '建立灌装设备计量精度周校验制度，超差自动停线。'
    },
    3: {
      title: '冷鲜肉到货中心温度超标整改（供应商）',
      problem: 'NCR20261027003 供应商富源县胜境生猪养殖场到货冷鲜肉中心温度偏高，30kg 让步接收。',
      interim: '该批 30kg 单独存放并 4 小时内投产，暂停该供应商后续到货验收放行。',
      why: ['运输途中制冷不足', '冷链车预冷时间不足', '装车前未做车厢预冷', '司机未按规程执行', '供应商冷链作业规程缺失预冷要求'],
      root: '供应商冷链运输规程未规定装车前车厢预冷至 0~4℃，且缺少在途温度记录。',
      perm: '要求供应商修订冷链运输规程并加装温度记录仪，到货随车提供温度曲线。',
      prevent: '冷鲜肉到货实施"温度曲线+中心温度"双验证，不合格直接拒收。'
    },
    4: {
      title: '广味香肠客诉异味问题处理',
      problem: '客户反馈批次 XC20261020A 广味香肠存在轻微酸败味，涉及 3 袋。',
      interim: '客诉批次先行召回，同批次留样复检感官与挥发性盐基氮。',
      why: ['产品出现酸败味', '腌制时间超出工艺上限', '腌制间温度偏高', '腌制间制冷能力不足', '夏季产能提升未同步评估制冷负荷'],
      root: '腌制间制冷能力不足，夏季高温时段腌制间温度超过 4~8℃ 控制上限，导致微生物繁殖。',
      perm: '腌制间增设 2 台冷风机并加装温度自动记录与超限报警，腌制时间纳入 MES 强制卡控。',
      prevent: '关键控制点（CCP）温度超限自动报警并锁定工序流转。'
    },
    5: {
      title: '金属探测工序漏剔风险整改',
      problem: 'NCR20261027005 金属探测工序试块测试异常，1 袋产品存在漏剔风险。',
      interim: '停线校验金检机，回溯当班 1800 袋重新过检，未再发现金属异物。',
      why: ['金检机剔除装置动作迟缓', '气缸气压不足', '空压机供气压力波动', '气路未设稳压装置', '设备安装时未按规范配置气源处理件'],
      root: '金检机气路缺少稳压与过滤装置，气压波动导致剔除气缸动作不到位。',
      perm: '加装气源稳压阀与油水分离器，剔除装置增加动作确认传感器，每班开机做 Fe/SUS 试块验证。',
      prevent: '将金检机纳入 CCP 点检，试块验证记录随批归档。'
    }
  };
  let capaNo = 1, rcCnt = 0, effCnt = 0;
  for (const n of ncrs) {
    const d = capaDetail[capaNo] || capaDetail[1];
    const base = new Date(n.discover_time);
    const cno = 'CAPA' + fmt(base).slice(0, 10).replace(/-/g, '') + pad(capaNo);
    const closed = n.ncr_status === '5';
    const ins = await exec(`INSERT INTO qms_capa(capa_no,source_type,source_no,title,problem_desc,team_members,interim_action,root_cause,permanent_action,verify_result,prevent_action,close_remark,capa_status,responsible_person,responsible_person_id,plan_close_time,close_time,del_flag,status,create_by,create_time,remark,dept_id)
      VALUES(?,'ncr',?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','0','system',?,?,104)`,
      [cno, n.ncr_no, d.title, d.problem, '质量部、生产部、设备部、采购部',
        d.interim, d.root, d.perm, closed ? '整改后连续 3 批抽检合格，问题未再发生' : '整改实施中，待效果验证',
        d.prevent, closed ? '效果验证通过，予以关闭' : '', closed ? '3' : '1', '陈静', 10,
        fmt(new Date(base.getTime() + 30 * 86400000)), closed ? fmt(new Date(base.getTime() + 25 * 86400000)) : null,
        fmt(base), '来源:' + n.ncr_no]);
    await exec(`INSERT INTO qms_capa_root_cause(capa_id,capa_no,analysis_type,why1,why2,why3,why4,why5,root_cause,create_by,create_time)
      VALUES(?,?, '5why',?,?,?,?,?,?, 'system',?)`,
      [ins.insertId, cno, d.why[0], d.why[1], d.why[2], d.why[3], d.why[4], d.root, fmt(base)]);
    rcCnt++;
    if (closed) {
      await exec(`INSERT INTO qms_capa_effectiveness(capa_id,capa_no,check_period,check_date,checker,check_method,check_result,evidence,recurrence_flag,recurrence_desc,action_taken,esig_user,esig_time,status,del_flag,create_by,create_time)
        VALUES(?,?,?,?,?,?,?,?, '0','','整改措施已固化到作业指导书与点检表',?,?, '0','0','system',?)`,
        [ins.insertId, cno, '2026-11', fmt(new Date(base.getTime() + 26 * 86400000)), '陈静',
          '连续 3 批抽检 + 现场执行记录核查', '1', '抽检记录 3 份、点检表 5 份，均符合要求', '陈静',
          fmt(new Date(base.getTime() + 26 * 86400000)), fmt(new Date(base.getTime() + 26 * 86400000))]);
      effCnt++;
    }
    capaNo++;
  }
  console.log(`CAPA ${ncrs.length} 条，5Why ${rcCnt} 条，效果验证 ${effCnt} 条`);

  // ---------- 2. 质量文档 ----------
  const docs = [
    ['QMS-MAN-001', '质量手册', 'manual', '2.0', '2026-01-05'],
    ['QMS-PRO-002', '不合格品控制程序', 'procedure', '3.0', '2026-01-05'],
    ['QMS-PRO-003', '纠正预防措施控制程序', 'procedure', '2.1', '2026-02-10'],
    ['QMS-PRO-004', '供应商质量管理程序', 'procedure', '2.0', '2026-02-10'],
    ['QMS-PRO-005', '产品追溯与召回程序', 'procedure', '1.2', '2026-03-01'],
    ['QMS-SOP-018', '真空包装作业指导书', 'sop', '1.3', '2026-03-15'],
    ['QMS-SOP-021', '金属探测关键控制点作业指导书', 'sop', '1.1', '2026-03-15'],
    ['QMS-SOP-026', '腌制工序温度控制作业指导书', 'sop', '1.2', '2026-04-01'],
    ['QMS-FRM-031', '首件确认记录表', 'form', '1.0', '2026-04-01'],
    ['QMS-FRM-036', '成品出厂检验报告', 'form', '1.1', '2026-04-20'],
    ['QMS-FRM-042', '纠正预防措施验证记录', 'form', '1.0', '2026-05-06'],
    ['QMS-PRO-007', '管理评审控制程序', 'procedure', '1.0', '2026-05-20']
  ];
  for (const d of docs) {
    await exec(`INSERT INTO qms_doc(doc_no,doc_title,doc_category,version_no,doc_status,dept_id,dept_name,industry_type,publish_date,del_flag,status,create_by,create_time,remark)
      VALUES(?,?,?,?, '2',104,'质量管理部','food',?, '0','0','system',NOW(),'受控文件')`, d);
  }
  console.log(`质量文档 ${docs.length} 份`);

  // ---------- 3. 缺陷代码 ----------
  const defects = [
    ['DC001', '真空漏气', '1', '包装缺陷'], ['DC002', '封口虚封', '2', '包装缺陷'], ['DC003', '净含量不足', '2', '包装缺陷'],
    ['DC004', '异物（金属）', '1', '材料缺陷'], ['DC005', '异物（毛发）', '1', '材料缺陷'], ['DC006', '酸败味', '1', '性能缺陷'],
    ['DC007', '色泽不均', '3', '外观缺陷'], ['DC008', '表面发白（烟熏）', '3', '工艺缺陷'], ['DC009', '标签错版', '2', '包装缺陷'],
    ['DC010', '日期喷码不清', '3', '包装缺陷'], ['DC011', '肠体破裂', '2', '工艺缺陷'], ['DC012', '切面粗糙', '4', '外观缺陷'],
    ['DC013', '中心温度不达标', '1', '工艺缺陷'], ['DC014', '水分含量超标', '3', '性能缺陷'], ['DC015', '亚硝酸盐残留超标', '1', '材料缺陷'],
    ['DC016', '包装袋破损', '2', '包装缺陷'], ['DC017', '规格重量偏差', '3', '外观缺陷'], ['DC018', '腌制不入味', '4', '工艺缺陷']
  ];
  for (const d of defects) {
    await exec(`INSERT INTO qms_defect_code(defect_code,defect_name,defect_level,defect_category,status,del_flag,create_by,create_time)
      VALUES(?,?,?,?, '0','0','system',NOW())`, d);
  }
  console.log(`缺陷代码 ${defects.length} 项`);

  // ---------- 4. 客诉 ----------
  const custs = await q(`SELECT customer_id,customer_name FROM mk_customer LIMIT 3`);
  const receipts = await q(`SELECT batch_no, product_name, product_code FROM mms_finish_receipt WHERE batch_no<>'' ORDER BY finish_id DESC LIMIT 3`);
  const complaints = [
    { c: custs[0], r: receipts[0], type: 'quality', desc: '客户反馈香肠开袋后有轻微酸味，怀疑运输途中温度失控。', sev: '2', qty: 3, amt: 126, claim: 300, res: '4', st: '3' },
    { c: custs[1], r: receipts[1], type: 'quality', desc: '外包装箱受潮变形，内部产品真空袋完好但纸箱标识模糊。', sev: '3', qty: 10, amt: 380, claim: 0, res: '2', st: '3' },
    { c: custs[2], r: receipts[2], type: 'delivery', desc: '订单交付延迟 2 天，客户要求说明原因。', sev: '3', qty: 0, amt: 0, claim: 0, res: '5', st: '1' }
  ];
  let cpNo = 1;
  for (const c of complaints) {
    const d = new Date(2026, 9, 20 + cpNo);
    await exec(`INSERT INTO qms_complaint(complaint_no,customer_name,customer_code,material_name,material_code,batch_no,complaint_date,complaint_type,defect_desc,severity,return_qty,return_amt,claim_amt,handle_desc,handle_result,complaint_status,close_time,del_flag,status,create_by,create_time,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','0','system',?,104)`,
      ['TS2026' + pad(cpNo, 4), c.c ? c.c.customer_name : '云南本地经销商', c.c ? 'KH' + pad(c.c.customer_id) : '',
        c.r ? c.r.product_name : '', c.r ? c.r.product_code : '', c.r ? c.r.batch_no : '', fmt(d).slice(0, 10),
        c.type, c.desc, c.sev, c.qty, c.amt, c.claim,
        c.res === '4' ? '现场核查为运输环节温度失控，已对承运商开出整改通知并加强保温包装' : (c.res === '2' ? '已补发同规格产品并更换外箱' : '已协调生产与物流调整交期，向客户致歉'),
        c.res, c.st, c.st === '3' ? fmt(new Date(d.getTime() + 5 * 86400000)) : null, fmt(d)]);
    cpNo++;
  }
  console.log(`客诉 ${complaints.length} 条`);

  // ---------- 5. 供应商质量评价 ----------
  const sups = await q(`SELECT supplier_id,supplier_name FROM wms_supplier WHERE del_flag='0' ORDER BY supplier_id LIMIT 12`);
  let sCnt = 0;
  for (const [i, s] of sups.entries()) {
    const total = Number((78 + rnd(0, 18)).toFixed(2));
    const grade = total >= 90 ? 'A' : total >= 80 ? 'B' : total >= 70 ? 'C' : 'D';
    const incoming = ri(4, 20), pass = Math.max(1, Math.round(incoming * (0.9 + Math.random() * 0.1)));
    await exec(`INSERT INTO qms_supplier_eval(supplier_id,supplier_name,eval_period,eval_type,incoming_total,incoming_pass,ppm,pass_rate,quality_score,delivery_score,service_score,total_score,grade,eval_status,del_flag,status,create_by,create_time,remark)
      VALUES(?,?,?, 'monthly',?,?,?,?,?,?,?,?,?, '1','0','0','system',NOW(),?)`,
      [s.supplier_id, s.supplier_name, '2026-10', incoming, pass, Math.round((1 - pass / incoming) * 1000000),
        (pass / incoming * 100).toFixed(2), (total * 0.5).toFixed(2), (total * 0.3).toFixed(2), (total * 0.2).toFixed(2), total, grade,
        grade === 'D' ? '到货质量波动，已启动约谈' : '评价符合要求']);
    sCnt++;
  }
  console.log(`供应商质量评价 ${sCnt} 家`);

  // ---------- 6. 抽样方案（GB/T 2828.1 一般检验水平 II，AQL 1.0） ----------
  const aql = [
    ['A', 2, 0, 1, 2, 8], ['B', 3, 0, 1, 9, 15], ['C', 5, 0, 1, 16, 25], ['D', 8, 0, 1, 26, 50],
    ['E', 13, 0, 1, 51, 90], ['F', 20, 0, 1, 91, 150], ['G', 32, 0, 1, 151, 280], ['H', 50, 1, 2, 281, 500],
    ['J', 80, 2, 3, 501, 1200], ['K', 125, 3, 4, 1201, 3200], ['L', 200, 5, 6, 3201, 10000], ['M', 315, 7, 8, 10001, 35000]
  ];
  for (const a of aql) {
    await exec(`INSERT INTO qms_aql_plan(aql_level,code_letter,sample_size,ac_val,re_val,inspect_level,batch_min,batch_max,status,del_flag,create_by,create_time,remark)
      VALUES('1.0',?,?,?,?, '2',?,?, '0','0','system',NOW(),'GB/T 2828.1 一般检验水平II')`, a);
  }
  console.log(`抽样方案 ${aql.length} 行`);

  // ---------- 7. 物料质量属性 ----------
  const mats = await q(`SELECT material_id,material_code,material_name,material_type FROM wms_material WHERE del_flag='0' AND material_type IN ('0','3','6') ORDER BY material_id LIMIT 40`);
  let maCnt = 0;
  for (const m of mats) {
    const types = m.material_type === '6' ? '外观,规格' : (m.material_type === '3' ? '感官,水分,微生物' : '感官,理化,微生物');
    await exec(`INSERT INTO qms_material_attr(material_id,material_code,material_name,inspect_types,industry_type,is_exempt,status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?, 'food', '0','0','0','system',NOW(),?)`,
      [m.material_id, m.material_code, m.material_name, types, '按物料类别配置检验项']);
    maCnt++;
  }
  console.log(`物料质量属性 ${maCnt} 项`);

  // ---------- 8. 质量车间/产线/工位树 ----------
  const ws = await q(`SELECT * FROM mms_workshop ORDER BY workshop_id`);
  const lines = await q(`SELECT * FROM mms_line ORDER BY line_id`);
  const stations = await q(`SELECT * FROM mms_station ORDER BY station_id`);
  for (const w of ws) {
    await exec(`INSERT INTO qms_workshop(workshop_id,parent_id,workshop_code,workshop_name,node_type,order_num,status,del_flag,create_by,create_time,remark)
      VALUES(?,0,?,?, '1',?, '0','0','system',NOW(),'同步自生产车间')`, [w.workshop_id, w.workshop_code, w.workshop_name, w.workshop_id]);
  }
  for (const l of lines) {
    await exec(`INSERT INTO qms_workshop(workshop_id,parent_id,workshop_code,workshop_name,node_type,line_type,cycle_time,order_num,status,del_flag,create_by,create_time)
      VALUES(?,?,?,?, '2',?,?,?, '0','0','system',NOW())`,
      [100 + l.line_id, l.workshop_id, l.line_code, l.line_name, l.line_type, l.cycle_time, l.line_id]);
  }
  for (const s of stations) {
    await exec(`INSERT INTO qms_workshop(workshop_id,parent_id,workshop_code,workshop_name,node_type,station_type,order_num,status,del_flag,create_by,create_time)
      VALUES(?,?,?,?, '3',?,?, '0','0','system',NOW())`,
      [200 + s.station_id, 100 + s.line_id, s.station_code, s.station_name, s.station_type, s.station_id]);
  }
  console.log(`车间树 ${ws.length + lines.length + stations.length} 节点`);

  // ---------- 9. 批次谱系 + SN 追溯 ----------
  const traces = await q(`SELECT t.*, wo.work_order_no, wo.product_name FROM mms_batch_trace t
    LEFT JOIN mms_work_order wo ON wo.work_order_id=t.work_order_id ORDER BY t.trace_id`);
  const disp = await q(`SELECT work_order_id, process_name, resource_name, team_name FROM mms_dispatch ORDER BY dispatch_id`);
  const dispMap = new Map();
  disp.forEach(d => { if (!dispMap.has(d.work_order_id)) dispMap.set(d.work_order_id, d); });
  const fin = await q(`SELECT work_order_id, finish_qty, finish_time FROM mms_finish_receipt WHERE batch_no<>''`);
  const finMap = new Map(fin.map(f => [f.work_order_id, f]));
  let gCnt = 0;
  for (const t of traces) {
    const d = dispMap.get(t.work_order_id) || {};
    const f = finMap.get(t.work_order_id) || {};
    await exec(`INSERT INTO qms_lot_genealogy(parent_batch_no,child_batch_no,work_order_no,op_id,op_name,material_id,material_code,line_name,operator,source_type,source_no,trace_time,break_flag,del_flag,status,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?, 'mms',?,?,0, '0','0','system',?,?)`,
      [t.batch_in, t.batch_out, t.work_order_no, String(t.op_seq), d.process_name || '分割分拣', t.material_in, t.material_code,
        d.resource_name || '', d.team_name || '', t.work_order_no, fmt(new Date(t.create_time)),
        fmt(new Date(t.create_time)), '领料批次→成品批次']);
    gCnt++;
  }
  const batches = await q(`SELECT DISTINCT batch_no, work_order_id FROM mms_finish_receipt WHERE batch_no<>'' ORDER BY finish_id DESC`);
  let snCnt = 0;
  for (const b of batches) {
    for (let i = 1; i <= 2; i++) {
      await exec(`INSERT INTO qms_trace_sn(sn_code,batch_no,work_order_no,material_id,material_code,status,del_flag,create_by,create_time)
        VALUES(?,?,?,?,?, '1','0','system',NOW())`,
        ['SN' + b.batch_no + pad(i, 2), b.batch_no, (await q(`SELECT work_order_no FROM mms_work_order WHERE work_order_id=?`, [b.work_order_id]))[0]?.work_order_no || '',
          null, '']);
      snCnt++;
    }
  }
  console.log(`批次谱系 ${gCnt} 条，SN ${snCnt} 条`);

  // ---------- 10. 内审 ----------
  const plans = [
    ['IA2026-01', '2026年度质量管理体系内部审核（上半年）', 2026, 'internal', '屠宰分割、灌装、包装、仓储全过程', '陈静', '2026-05-12', '2026-05-14', '2', '共开出一般不符合 3 项，无严重不符合，体系运行基本有效'],
    ['IA2026-02', '2026年度质量管理体系内部审核（下半年）', 2026, 'internal', '全部门及 HACCP 关键控制点', '陈静', '2026-10-20', '2026-10-22', '1', null]
  ];
  const clauses = [
    ['8.5.1', '生产和服务提供的控制', '灌装工序参数记录不完整', '2', '灌装车间', '补充参数记录并纳入班后检查', '1'],
    ['8.7', '不合格输出的控制', '不合格品隔离区标识不清', '2', '质量管理部', '重新划分隔离区并挂牌', '2'],
    ['7.1.5', '监视和测量资源', '压力表校准标识缺失', '2', '设备部', '补做校准并建立台账', '2'],
    ['8.4', '外部提供过程控制', '供应商年度评价未按期完成', '3', '采购部', '10 月底前完成全部供应商评价', '1'],
    ['9.2', '内部审核', '内审员培训记录缺失', '3', '质量管理部', '补充培训并留存签到表', '2']
  ];
  let apCnt = 0;
  for (const p of plans) {
    const ins = await exec(`INSERT INTO qms_audit_plan(audit_plan_no,audit_title,audit_year,audit_type,audit_scope,lead_auditor,audit_date_from,audit_date_to,audit_conclusion,plan_status,del_flag,status,create_by,create_time,remark,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?, '0','0','system',NOW(),'',104)`,
      [p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[9], p[8]]);
    for (const c of clauses.slice(0, p[8] === '2' ? 3 : 5)) {
      await exec(`INSERT INTO qms_audit_item(audit_plan_id,audit_plan_no,clause_ref,nc_desc,nc_type,dept_name,corrective_action,item_status,del_flag,status,create_by,create_time)
        VALUES(?,?,?,?,?,?,?,?, '0','0','system',NOW())`,
        [ins.insertId, p[0], c[0] + ' ' + c[1], c[2], c[3], c[4], c[5], c[6]]);
      await exec(`INSERT INTO qms_audit_check_item(audit_plan_id,clause_ref,check_content,check_result,evidence,sort_no,del_flag,create_by,create_time)
        VALUES(?,?,?,?,?,?, '0','system',NOW())`,
        [ins.insertId, c[0], '检查' + c[1] + '相关记录是否完整、可追溯', c[3] === '2' ? '2' : '1', '抽查记录 ' + ri(3, 10) + ' 份', apCnt + 1]);
      apCnt++;
    }
  }
  console.log(`内审计划 ${plans.length} 次，不符合项 ${apCnt} 条`);

  // ---------- 11. 管理评审 ----------
  await exec(`INSERT INTO qms_mr(mr_no,mr_title,mr_year,mr_date,chairperson,participants,input_summary,resolution,action_items,mr_status,del_flag,status,create_by,create_time,dept_id)
    VALUES('MR2026-01','2026年度管理评审会议',2026,'2026-11-05','总经理','总经理、质量负责人、生产部、采购部、销售部、仓储部',
    '回顾 2026 年 1-10 月质量目标达成情况：成品一次交检合格率 98.6%，客诉 3 起，供应商来料合格率 96.8%，完成内审 2 次、CAPA 5 项。',
    '体系运行有效，资源配置基本满足；需重点改善腌制工序温度控制与冷链运输管理。',
    '1. 腌制间增设制冷设备（设备部，11 月底前）；2. 供应商冷链温度记录全覆盖（采购部，12 月底前）；3. 修订 3 份作业指导书（质量部，11 月中）；4. 2027 年质量目标制定（质量部，12 月）。',
    '2','0','0','system',NOW(),104)`);
  console.log('管理评审 1 次');

  console.log(await q(`SELECT (SELECT COUNT(*) FROM qms_capa) capa,(SELECT COUNT(*) FROM qms_doc) doc,(SELECT COUNT(*) FROM qms_defect_code) defect,
    (SELECT COUNT(*) FROM qms_complaint) complaint,(SELECT COUNT(*) FROM qms_supplier_eval) supEval,(SELECT COUNT(*) FROM qms_aql_plan) aql,
    (SELECT COUNT(*) FROM qms_material_attr) matAttr,(SELECT COUNT(*) FROM qms_workshop) ws,(SELECT COUNT(*) FROM qms_lot_genealogy) gene,
    (SELECT COUNT(*) FROM qms_trace_sn) sn,(SELECT COUNT(*) FROM qms_audit_plan) audit,(SELECT COUNT(*) FROM qms_mr) mr`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
