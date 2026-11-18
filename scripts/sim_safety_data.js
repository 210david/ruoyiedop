/**
 * 安全生产管理（biz_safety_*）数据补全 —— 幂等
 *   区域树 / 应急预案 / 应急物资 / 演练 / 事故 / 重大危险源 / 安全设备
 *   检查任务 / 隐患 + 整改 + 审核日志 / 合规检查 / 危险物品 + 出入库 + 供应商
 *   培训计划 / 课程 / 记录 / 签到 / 证书 / 特种作业人员 / 在线考试体系 / 到期提醒
 *
 * 同时把历史数据的英文/中文枚举纠正为字典值
 * 用法：node scripts/sim_safety_data.js
 */
const mysql = require('mysql2/promise')

const DB = { host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true }
const NOW = '2026-11-17 09:00:00'
const ENT = 1
const DEPT = { 101: '综合管理部', 104: '生产部', 105: '质量管理部', 106: '仓储物流部', 108: '安全生产办公室', 109: '屠宰分割车间', 110: '加工车间' }
const UID = { 丰孟: 2, 罗丽波: 3, 李晨: 5, 周强: 6, 吴芳: 7, 张伟: 8, 刘洋: 9, 陈静: 10, 赵雷: 11, 孙丽: 12, 杨帆: 13, 胡明: 14, 徐婷: 15, 高鹏: 16, 邓楠: 20, 黄健: 21 }

let seed = 20261118
const rnd = () => { seed = (seed * 9301 + 49297) % 233280; return seed / 233280 }
const pick = a => a[Math.floor(rnd() * a.length)]
const ri = (a, b) => a + Math.floor(rnd() * (b - a + 1))
const d2 = n => Number(n).toFixed(2)
const p2 = n => String(n).padStart(2, '0')
const fmt = t => `${t.getFullYear()}-${p2(t.getMonth() + 1)}-${p2(t.getDate())} ${p2(t.getHours())}:${p2(t.getMinutes())}:00`
const dstr = t => `${t.getFullYear()}-${p2(t.getMonth() + 1)}-${p2(t.getDate())}`
const daysAgo = n => { const d = new Date(2026, 10, 17, 9, 0, 0); d.setDate(d.getDate() - n); return d }

;(async () => {
  const c = await mysql.createConnection(DB)

  const exists = async (sql, p) => { const [r] = await c.query(sql, p); return r.length > 0 }
  async function ins(table, obj, uniq) {
    if (uniq) {
      const cols = Array.isArray(uniq) ? uniq : [uniq]
      const where = cols.map(k => `${k}=?`).join(' AND ')
      if (await exists(`SELECT 1 FROM ${table} WHERE ${where}`, cols.map(k => obj[k]))) return 0
    }
    const cols = Object.keys(obj)
    await c.query(`INSERT INTO ${table} (${cols.join(',')}) VALUES (${cols.map(() => '?').join(',')})`, cols.map(k => obj[k]))
    return 1
  }
  const base = (dept = 108) => ({ enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', dept_id: dept })

  /* ---------- 0. 纠正历史枚举 ---------- */
  const lvl = { 重大风险: '1', 较大风险: '2', 一般风险: '3', 低风险: '4' }
  const [rps] = await c.query('SELECT risk_point_id, risk_level FROM biz_safety_risk_point')
  let fix1 = 0
  for (const r of rps) if (lvl[r.risk_level]) { await c.query('UPDATE biz_safety_risk_point SET risk_level=? WHERE risk_point_id=?', [lvl[r.risk_level], r.risk_point_id]); fix1++ }
  const hl = { 重大隐患: '2', 较大隐患: '2', 一般隐患: '1' }
  const hs = { pending: '3', rectifying: '4', rectified: '5', verified: '6', draft: '0', approving: '1', rejected: '2', overdue: '7' }
  const [hzs] = await c.query('SELECT hazard_id, hazard_level, hazard_status FROM biz_safety_hazard')
  let fix2 = 0
  for (const h of hzs) {
    const l = hl[h.hazard_level] ?? h.hazard_level
    const s = hs[h.hazard_status] ?? h.hazard_status
    if (l !== h.hazard_level || s !== h.hazard_status) {
      await c.query('UPDATE biz_safety_hazard SET hazard_level=?, hazard_status=? WHERE hazard_id=?', [l, s, h.hazard_id]); fix2++
    }
  }
  console.log(`[0] 纠正风险等级 ${fix1} 条 / 隐患等级状态 ${fix2} 条`)

  /* ---------- 1. 区域树 ---------- */
  const areas = [
    ['QY01', '云南赢隆食品加工有限公司厂区', '0', null],
    ['QY0101', '屠宰分割车间', 'QY01', '生产区'],
    ['QY0102', '加工车间（灌装/包装/熟制）', 'QY01', '生产区'],
    ['QY0103', '制冷机房（液氨）', 'QY01', '重点部位'],
    ['QY0104', '原料冷冻库', 'QY01', '仓储区'],
    ['QY0105', '成品冷藏库', 'QY01', '仓储区'],
    ['QY0106', '锅炉房', 'QY01', '重点部位'],
    ['QY0107', '配电室', 'QY01', '重点部位'],
    ['QY0108', '化验室', 'QY01', '辅助区'],
    ['QY0109', '危险化学品仓库', 'QY01', '重点部位'],
    ['QY0110', '污水处理站', 'QY01', '辅助区'],
    ['QY0111', '厂区办公区', 'QY01', '办公区']
  ]
  const areaId = {}
  let n1 = 0
  for (const [code, name, pcode, type] of areas) {
    if (await exists('SELECT 1 FROM biz_safety_area WHERE area_code=?', [code])) {
      const [r] = await c.query('SELECT area_id FROM biz_safety_area WHERE area_code=?', [code]); areaId[code] = r[0].area_id; continue
    }
    const [r] = await c.query(
      `INSERT INTO biz_safety_area (parent_id,ancestors,area_code,area_name,node_type,enterprise_id,area_type,safety_person_id,safety_person_name,status,del_flag,create_by,create_time,update_by,remark)
       VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
      [pcode ? (areaId[pcode] || 0) : 0, '0', code, name, pcode ? '1' : '0', ENT, type,
        UID['邓楠'], '邓楠', '0', '0', 'admin', NOW, '', null]
    )
    areaId[code] = r.insertId; n1++
  }
  console.log(`[1] 区域 ${n1} 个`)

  /* ---------- 2. 危险物品 ---------- */
  const materials = [
    ['WHP001', '液氨', '2', '7664-41-7', 12, 't', 'QY0103', '制冷机房储罐区', '1', 8.6, 3, 12, 5],
    ['WHP002', '氢氧化钠', '14', '1310-73-2', 2, 't', 'QY0109', '危化品仓库A区', '0', 0.8, 0.2, 1.5, 5],
    ['WHP003', '次氯酸钠', '14', '7681-52-9', 1, 't', 'QY0109', '危化品仓库A区', '0', 0.35, 0.1, 0.8, 5],
    ['WHP004', '75% 食用酒精', '5', '64-17-5', 1, 't', 'QY0109', '危化品仓库B区', '0', 0.42, 0.1, 0.6, 5],
    ['WHP005', '乙炔（气瓶）', '2', '74-86-2', 40, '瓶', 'QY0109', '危化品仓库气瓶间', '2', 12, 5, 20, 9],
    ['WHP006', '氧气（气瓶）', '3', '7782-44-7', 40, '瓶', 'QY0109', '危化品仓库气瓶间', '0', 15, 5, 20, 9]
  ]
  const matId = {}
  let n2 = 0
  for (const [code, name, cls, cas, maxQ, unit, area, areaName, toxic, cur, smin, smax, sup] of materials) {
    const row = {
      material_code: code, material_name: name, hazard_class: cls, cas_no: cas, max_storage: d2(maxQ), storage_unit: unit,
      storage_area_id: areaId[area] || null, storage_area_name: areaName, toxic_flag: toxic, current_stock: d2(cur),
      safety_stock_min: d2(smin), safety_stock_max: d2(smax), default_supplier_id: sup, enterprise_id: ENT,
      physical_properties: name === '液氨' ? '无色有刺激性气味气体，易液化，沸点 -33.3℃，易溶于水' : '按 SDS 执行',
      hazard_summary: name === '液氨' ? '有毒、可燃，与空气形成爆炸性混合物，泄漏可致中毒窒息' : '腐蚀性/易燃，按 GB 15603 储存',
      first_aid_measures: '迅速脱离现场至空气新鲜处，保持呼吸道通畅，必要时输氧或人工呼吸，立即就医',
      fire_fighting: '雾状水稀释、切断气源；消防人员须穿戴全身防护服',
      leak_handling: '疏散人员至上风向，切断泄漏源，喷雾状水稀释，禁止带压紧固',
      storage_precaution: '阴凉通风库房，远离火种热源，与氧化剂、酸类分开存放',
      exposure_control: '严加密闭，提供喷淋洗眼器，作业时佩戴防毒面具',
      stability_reactivity: '稳定，避免与卤素、酸类接触',
      toxicological_info: 'LC50 1390mg/m³（大鼠吸入，2h）',
      ecological_info: '对水生生物有毒',
      disposal_info: '交由有资质单位处置，禁止直排',
      transport_info: '按危险货物运输规定，专用槽车运输',
      flash_point: name === '液氨' ? '—' : '13℃', explosion_limit: name === '液氨' ? '15.7%~27.4%' : '3.3%~19%',
      status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }
    n2 += await ins('biz_safety_material', row, 'material_code')
    const [r] = await c.query('SELECT material_id FROM biz_safety_material WHERE material_code=?', [code])
    matId[code] = r[0].material_id
  }
  console.log(`[2] 危险物品 ${n2} 种`)

  /* ---------- 3. 危险物品供应商 ---------- */
  const sups = [
    ['WHS001', '曲靖市麒麟气体有限公司', '1', '91530302MA6K8X2N3B', '李工', '0874-3125566', '曲靖市麒麟区南宁东路 88 号', '液氨、乙炔、氧气充装与销售', 0],
    ['WHS002', '云南云天化股份有限公司', '1', '91530000216522182C', '王经理', '0871-68319000', '昆明市西山区滇池路 1418 号', '液碱、双氧水等化工原料', 0],
    ['WHS003', '富源县顺发化工经营部', '2', '92530325MA6P2Q4L1X', '张顺发', '13888456712', '富源县中安街道化工市场 12 号', '次氯酸钠、酒精分装与零售', 0],
    ['WHS004', '曲靖恒安劳保用品有限公司', '2', '91530300MA6N7K9T2F', '陈恒', '0874-3317788', '曲靖市沾益区龙华大道 66 号', '劳动防护用品、应急物资', 0]
  ]
  let n3 = 0
  const supId = {}
  for (const [code, name, type, credit, contact, phone, addr, scope] of sups) {
    n3 += await ins('biz_safety_material_supplier', {
      supplier_code: code, supplier_name: name, supplier_type: type, credit_code: credit, contact_person: contact,
      contact_phone: phone, address: addr, business_scope: scope, enterprise_id: ENT, status: '0', del_flag: '0',
      create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'supplier_code')
    const [r] = await c.query('SELECT supplier_id FROM biz_safety_material_supplier WHERE supplier_code=?', [code])
    supId[code] = r[0].supplier_id
  }
  console.log(`[3] 危险物品供应商 ${n3} 家`)

  /* ---------- 4. 危险物品出入库 ---------- */
  let n4 = 0
  const moveSpec = [['WHP001', 30], ['WHP002', 6], ['WHP003', 6], ['WHP004', 6], ['WHP005', 10], ['WHP006', 10]]
  for (const [code, cnt] of moveSpec) {
    const m = materials.find(x => x[0] === code)
    let stock = Number(m[9])
    let seqm = 1
    for (let i = 0; i < cnt; i++) {
      const ago = ri(1, 150)
      const d = daysAgo(ago)
      const rtype = rnd() < 0.55 ? '1' : '2'
      const q = code === 'WHP001' ? Number((ri(20, 90) / 100).toFixed(2)) : ri(1, 4)
      const recordCode = `WHRK${dstr(d).replace(/-/g, '')}${p2(seqm++)}`
      const before = stock
      const after = rtype === '1' ? before + q : Math.max(before - q, 0)
      n4 += await ins('biz_safety_material_record', {
        record_code: recordCode, material_id: matId[code], material_code: code, material_name: m[1], record_type: rtype,
        quantity: d2(q), unit: m[5], before_stock: d2(before), after_stock: d2(after),
        supplier_id: rtype === '1' ? (supId['WHS001'] || null) : null,
        supplier_name: rtype === '1' ? '曲靖市麒麟气体有限公司' : null,
        recipient: rtype === '2' ? pick(['黄健', '胡明', '张伟']) : null,
        purpose: rtype === '2' ? pick(['制冷机组补充', '车间清洗消毒', '设备维修焊接用气', '化验室配制']) : '采购入库',
        batch_no: 'B' + dstr(d).replace(/-/g, ''), operator: pick(['徐婷', '杨帆']),
        record_time: fmt(d), enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
      }, 'record_code')
      stock = after
    }
  }
  console.log(`[4] 危险物品出入库 ${n4} 条`)

  /* ---------- 5. 应急预案 ---------- */
  const plans = [
    ['YA2026001', '生产安全事故综合应急预案', '1', '3', 'V3.0', 2025, '2025-01-10', '2028-01-09'],
    ['YA2026002', '液氨泄漏事故专项应急预案', '2', '2', 'V2.0', 2025, '2025-01-10', '2028-01-09'],
    ['YA2026003', '火灾爆炸事故专项应急预案', '2', '2', 'V2.0', 2025, '2025-01-10', '2028-01-09'],
    ['YA2026004', '有限空间作业事故专项应急预案', '2', '3', 'V1.0', 2025, '2025-03-15', '2028-03-14'],
    ['YA2026005', '食品安全事故应急预案', '2', '2', 'V2.0', 2025, '2025-01-10', '2028-01-09'],
    ['YA2026006', '特种设备事故专项应急预案', '2', '3', 'V1.0', 2025, '2025-05-20', '2028-05-19'],
    ['YA2026007', '制冷机房现场处置方案', '3', '4', 'V2.0', 2025, '2025-01-10', '2028-01-09'],
    ['YA2026008', '配电室触电现场处置方案', '3', '4', 'V1.0', 2025, '2025-06-01', '2028-05-31']
  ]
  const planId = {}
  let n5 = 0
  for (const [code, name, type, level, ver, regYear, eff, exp] of plans) {
    n5 += await ins('biz_safety_emergency_plan', {
      plan_code: code, plan_name: name, plan_type: type, plan_level: level, version: ver,
      reg_no: `富应急备〔${regYear}〕${code.slice(-3)}号`, reg_date: eff, effective_date: eff, expire_date: exp,
      remind_status: '0', content: `${name}：明确组织机构与职责、预警与信息报告、应急响应分级、处置措施、应急保障与后期处置。`, attachment: null,
      enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null, dept_id: 108
    }, 'plan_code')
    const [r] = await c.query('SELECT plan_id FROM biz_safety_emergency_plan WHERE plan_code=?', [code])
    planId[code] = r[0].plan_id
  }
  console.log(`[5] 应急预案 ${n5} 份`)

  /* ---------- 6. 应急物资 ---------- */
  const ems = [
    ['YJWZ001', '正压式空气呼吸器', '2', '6.8L 碳纤维瓶', 8, '套', '应急器材室', '邓楠', '2027-06-30'],
    ['YJWZ002', '过滤式防毒面具', '1', '3M 6800 配氨滤盒', 20, '套', '应急器材室', '邓楠', '2027-03-31'],
    ['YJWZ003', '化学防护服', '1', '全封闭式 L/XL', 10, '套', '应急器材室', '邓楠', '2027-09-30'],
    ['YJWZ004', '干粉灭火器', '4', 'MFZ/ABC 8kg', 24, '具', '各区域消防点', '邓楠', '2027-05-31'],
    ['YJWZ005', '二氧化碳灭火器', '4', 'MT/5 5kg', 8, '具', '配电室/化验室', '邓楠', '2027-05-31'],
    ['YJWZ006', '消防水带', '4', 'DN65×25m', 12, '盘', '消防箱', '邓楠', '2029-12-31'],
    ['YJWZ007', '急救箱', '3', '综合型', 6, '个', '车间/办公区', '孙丽', '2027-01-31'],
    ['YJWZ008', '担架', '3', '折叠式', 3, '副', '应急器材室', '邓楠', '2029-12-31'],
    ['YJWZ009', '洗眼器', '3', '复合式', 4, '台', '化验室/危化品库', '邓楠', '2029-12-31'],
    ['YJWZ010', '应急照明灯', '5', '充电式', 15, '个', '各疏散通道', '邓楠', '2028-12-31'],
    ['YJWZ011', '对讲机', '5', '防爆型', 10, '台', '应急器材室', '邓楠', '2028-06-30'],
    ['YJWZ012', '警戒带/警示牌', '9', '一次性 500m', 10, '卷', '应急器材室', '邓楠', '2029-12-31'],
    ['YJWZ013', '吸油毡/吸附棉', '9', '40×50cm', 200, '片', '应急器材室', '邓楠', '2029-12-31'],
    ['YJWZ014', '应急饮用水', '6', '550ml×24', 20, '箱', '应急物资库', '徐婷', '2026-12-31'],
    ['YJWZ015', '应急食品（压缩饼干）', '6', '250g×20', 10, '箱', '应急物资库', '徐婷', '2027-02-28']
  ]
  let n6 = 0
  for (const [code, name, type, spec, qty, unit, loc, who, exp] of ems) {
    n6 += await ins('biz_safety_emergency_material', {
      material_code: code, material_name: name, material_type: type, spec_model: spec, quantity: d2(qty), unit,
      storage_location: loc, person_id: UID[who] || 20, person_name: who, effective_date: exp, remind_status: '0',
      enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null, dept_id: 108
    }, 'material_code')
  }
  console.log(`[6] 应急物资 ${n6} 项`)

  /* ---------- 7. 应急演练 ---------- */
  const drills = [
    ['YL2026001', '液氨泄漏事故应急演练', '2', '2026-04-18', '制冷机房及厂区东侧空地', '邓楠', 46],
    ['YL2026002', '火灾疏散逃生演练', '1', '2026-06-12', '加工车间、厂区集合点', '邓楠', 88],
    ['YL2026003', '有限空间（污水站）中毒窒息救援演练', '2', '2026-08-21', '污水处理站', '邓楠', 22],
    ['YL2026004', '食品安全事故（异物混入）应急演练', '2', '2026-09-16', '加工车间、化验室', '陈静', 34],
    ['YL2026005', '特种设备（锅炉）超压处置演练', '2', '2026-10-23', '锅炉房', '黄健', 18]
  ]
  let n7 = 0
  for (const [code, name, type, date, loc, cmd, cnt] of drills) {
    n7 += await ins('biz_safety_drill', {
      drill_code: code, plan_id: planId['YA2026002'] || null, drill_type: type, drill_name: name, drill_date: date,
      drill_location: loc, drill_commander: cmd, drill_objective: '检验应急预案的可操作性，锻炼应急队伍协同处置能力',
      participants: '安全管理、生产、仓储、质量等部门相关人员', participant_count: cnt,
      drill_desc: `${name}按预案流程开展：预警报告 → 启动响应 → 现场处置 → 人员疏散 → 环境监测 → 响应终止。`,
      eval_result: '演练组织有序，各小组响应及时，处置流程基本符合预案要求。',
      problems: '个别人员防护装备穿戴不规范；应急物资取用不熟练；信息上报环节存在延迟。',
      improvements: '加强防护装备实操训练；每季度开展一次应急物资取用演练；明确信息上报时限与责任人。',
      attachments: '[]', enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null, dept_id: 108
    }, 'drill_code')
  }
  console.log(`[7] 应急演练 ${n7} 次`)

  /* ---------- 8. 事故记录 ---------- */
  const incidents = [
    ['SG2026001', '4', '2026-03-09 10:20:00', '屠宰分割车间', '员工在清理分割台时被刀片划伤左手拇指，伤口约 1.5cm，送医缝合 3 针，未构成伤残。', 0, 860],
    ['SG2026002', '4', '2026-07-14 15:40:00', '加工车间包装工位', '包装机运行时员工违规伸手调整膜架，被夹伤右手食指，休工 5 天。', 0, 2350],
    ['SG2026003', '4', '2026-10-08 09:05:00', '成品冷藏库门口', '员工在冷库门口因地面结霜湿滑摔倒，右膝软组织挫伤，未住院。', 0, 420]
  ]
  let n8 = 0
  for (const [code, level, time, loc, desc, cas, loss] of incidents) {
    n8 += await ins('biz_safety_incident', {
      incident_code: code, incident_level: level, incident_date: time, incident_location: loc, incident_desc: desc,
      casualties: cas, economic_loss: d2(loss),
      cause_analysis: '直接原因：违反安全操作规程/现场防滑措施不到位。间接原因：安全培训针对性不足，现场监督检查不到位。',
      four_not_let_go: '事故原因未查清不放过、责任人未处理不放过、整改措施未落实不放过、有关人员未受到教育不放过（已全部落实）。',
      rectify_measures: '修订岗位安全操作规程并重新培训；增设防滑垫与警示标识；将该工位纳入每日安全巡查重点。',
      report_person_id: UID['邓楠'], report_person: '邓楠', report_time: time, enterprise_id: ENT, del_flag: '0',
      create_by: 'admin', create_time: NOW, update_by: '', remark: null, dept_id: 108
    }, 'incident_code')
  }
  console.log(`[8] 事故记录 ${n8} 条`)

  /* ---------- 9. 重大危险源 ---------- */
  const mh = [
    ['ZDWY001', '液氨储罐区（2×5m³）', '制冷机房液氨储罐', '储存液氨用于制冷系统，构成危险化学品重大危险源', 8.6, 10, '2', 'GB 18218-2018《危险化学品重大危险源辨识》', '2026-05-12', '2029-05-11', '胡明', '13888766554'],
    ['ZDWY002', '蒸汽锅炉（2t/h）', '锅炉房', '额定蒸汽压力 1.25MPa，属于特种设备重点监控对象', null, null, '3', 'TSG 11-2020《锅炉安全技术规程》', '2026-06-18', '2027-06-17', '黄健', '13988234511']
  ]
  let n9 = 0
  for (const [code, name, unit, desc, actual, critical, level, basis, idDate, nextDate, who, phone] of mh) {
    n9 += await ins('biz_safety_major_hazard', {
      hazard_code: code, hazard_name: name, unit_name: unit, unit_desc: desc,
      r_value: actual ? d2(actual) : null, critical_quantity: critical, actual_quantity: actual,
      ratio: actual && critical ? d2(actual / critical) : null, hazard_level: level,
      identification_basis: basis, identification_date: idDate, next_identification_date: nextDate,
      responsible_person: who, responsible_phone: phone,
      area_id: areaId['QY0103'] || null, area_name: name.includes('液氨') ? '制冷机房' : '锅炉房',
      enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'hazard_code')
  }
  console.log(`[9] 重大危险源 ${n9} 个`)

  /* ---------- 10. 安全设备（特种设备 + 安全附件） ---------- */
  const eqs = [
    ['AQSB001', '蒸汽锅炉', 'WNS2-1.25', 'QY0106', '锅炉房', '1', '1', '锅滇字第 2023-0357 号', '2027-06-17', '黄健'],
    ['AQSB002', '液氨储罐 1#', '5m³', 'QY0103', '制冷机房', '1', '2', '容滇字 2022-1188', '2027-03-20', '胡明'],
    ['AQSB003', '液氨储罐 2#', '5m³', 'QY0103', '制冷机房', '1', '2', '容滇字 2022-1189', '2027-03-20', '胡明'],
    ['AQSB004', '氨制冷压力管道', 'DN100 全长 186m', 'QY0103', '制冷机房', '1', '3', '管滇字 2022-0451', '2027-08-12', '胡明'],
    ['AQSB005', '叉车 1#', 'CPCD30 3t', 'QY0104', '原料库装卸区', '1', '5', '厂内滇 A-0231', '2027-01-15', '高鹏'],
    ['AQSB006', '叉车 2#', 'CPD20 2t 电动', 'QY0105', '成品库装卸区', '1', '5', '厂内滇 A-0232', '2027-01-15', '高鹏'],
    ['AQSB007', '载货电梯', '3t 2 层', 'QY0102', '加工车间货梯', '1', '6', '梯滇字 2021-0662', '2027-02-28', '杨帆'],
    ['AQSB008', '安全阀（锅炉）', 'A48Y-16C DN40', 'QY0106', '锅炉房', '3', '1', null, '2026-12-15', '黄健'],
    ['AQSB009', '氨气泄漏报警装置', 'NH3-A4 四点位', 'QY0103', '制冷机房', '3', '1', null, '2027-04-30', '胡明'],
    ['AQSB010', '可燃气体报警装置', 'GT-903', 'QY0109', '危化品仓库', '3', '1', null, '2027-04-30', '邓楠'],
    ['AQSB011', '防雷接地装置', '厂区全覆盖', 'QY01', '厂区', '3', '1', null, '2027-07-31', '邓楠'],
    ['AQSB012', '消防水泵', 'XBD 5.5kW', 'QY01', '消防泵房', '3', '1', null, '2027-05-31', '邓楠']
  ]
  let n10 = 0
  for (const [code, name, spec, area, areaName, cat, sub, cert, exp, who] of eqs) {
    n10 += await ins('biz_safety_equipment', {
      equipment_code: code, equipment_name: name, spec_model: spec, area_id: areaId[area] || null, area_name: areaName,
      equipment_category: cat, special_sub_type: sub, reg_cert_no: cert, inspect_expire: exp, remind_status: '0',
      person_id: UID[who] || 20, person_name: who, enterprise_id: ENT, status: '0', del_flag: '0',
      create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'equipment_code')
  }
  console.log(`[10] 安全设备 ${n10} 台`)

  /* ---------- 11. 特种作业人员 ---------- */
  const workers = [
    ['张伟', 'TQ001', 109, '屠宰分割车间', '分割组长', '13988765431', '1', '5', '滇 A0520230012', '2023-04-12', '2029-04-11', '2026-02-10'],
    ['黄健', 'TQ002', 104, '生产部', '设备主管', '13988234511', '1', '1', '滇 D0120220887', '2022-06-20', '2028-06-19', '2026-05-15'],
    ['刘洋', 'TQ003', 110, '加工车间', '包装班长', '13988765432', '1', '1', '滇 D0120230451', '2023-08-09', '2029-08-08', '2026-07-20'],
    ['胡明', 'TQ004', 106, '仓储物流部', '制冷工', '13888766554', '1', '4', '滇 ZL20210088', '2021-09-15', '2027-09-14', '2026-03-01'],
    ['杨帆', 'TQ005', 106, '仓储物流部', '制冷工', '13888766555', '1', '4', '滇 ZL20210089', '2021-09-15', '2027-09-14', '2026-03-01'],
    ['高鹏', 'TQ006', 106, '仓储物流部', '叉车司机', '13988765433', '1', '5', '滇 CC20230067', '2023-03-22', '2029-03-21', '2026-01-25'],
    ['徐婷', 'TQ007', 106, '仓储物流部', '叉车司机', '13988765434', '1', '5', '滇 CC20230068', '2023-03-22', '2029-03-21', '2026-01-25'],
    ['周强', 'TQ008', 103, '采购供应部', '维修焊工', '13988765435', '1', '2', '滇 H0120220771', '2022-11-08', '2028-11-07', '2026-05-08'],
    ['赵雷', 'TQ009', 105, '质量管理部', '化验员', '13988765436', '0', null, null, null, null, null],
    ['孙丽', 'TQ010', 105, '质量管理部', '化验员', '13988765437', '0', null, null, null, null, null],
    ['邓楠', 'TQ011', 108, '安全生产办公室', '安全管理员', '13988765438', '0', null, null, null, null, null],
    ['陈静', 'TQ012', 105, '质量管理部', '质量主管', '13988765439', '0', null, null, null, null, null]
  ]
  let n11 = 0
  for (const [name, no, dept, deptName, pos, phone, special, job, cert, issue, validTo, review] of workers) {
    n11 += await ins('biz_safety_worker', {
      worker_name: name, worker_no: no, dept_id: dept, dept_name: deptName, position: pos, phone,
      is_special: special, job_type: job, cert_no: cert, cert_issue_date: issue, valid_to: validTo, review_date: review,
      remind_status: '0', enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'worker_no')
  }
  console.log(`[11] 特种作业人员 ${n11} 人`)

  /* ---------- 12. 安全检查任务 ---------- */
  const [rps2] = await c.query('SELECT risk_point_id, risk_name FROM biz_safety_risk_point ORDER BY risk_point_id')
  const executors = ['邓楠', '张伟', '黄健', '胡明', '刘洋', '陈静']
  let n12 = 0
  let tseq = 1
  for (let i = 0; i < 70; i++) {
    const d = daysAgo(ri(0, 100))
    const code = `AQC${dstr(d).replace(/-/g, '')}${p2(tseq++)}`
    const rp = rps2.length ? pick(rps2) : { risk_point_id: null, risk_name: '厂区' }
    const who = pick(executors)
    const status = ri(0, 100) < 88 ? '2' : (ri(0, 1) ? '1' : '0')
    const hz = status === '2' && rnd() < 0.35 ? ri(1, 3) : 0
    n12 += await ins('biz_safety_inspection_task', {
      task_code: code, task_type: pick(['1', '1', '1', '2', '3', '4', '5']), task_name: `${rp.risk_name}安全排查`,
      risk_point_id: rp.risk_point_id, plan_date: dstr(d), executor_id: UID[who] || 20, executor_name: who,
      task_status: status, inspect_result: status === '2' ? (hz ? '2' : '1') : null, hazard_count: hz,
      inspect_time: status === '2' ? fmt(d) : null, enterprise_id: ENT, del_flag: '0', create_by: 'admin',
      create_time: NOW, update_by: '', remark: null
    }, 'task_code')
  }
  console.log(`[12] 安全检查任务 ${n12} 条`)

  /* ---------- 13. 隐患（补齐到 30 条）+ 整改 + 审核日志 ---------- */
  const hzDesc = [
    ['配电柜未设置绝缘垫，柜门未上锁', '2', '电气'], ['制冷机房应急照明失效', '2', '电气'],
    ['氨气报警仪未按周期检定', '2', '设备'], ['分割车间地面局部破损积水', '4', '环境'],
    ['员工未按规定佩戴防割手套', '1', '行为'], ['危化品仓库标识不规范', '3', '管理'],
    ['消防通道被周转筐临时占用', '3', '管理'], ['锅炉房压力表超期未检', '2', '设备'],
    ['包装机安全门联锁开关失效', '2', '设备'], ['冷库门口防滑垫缺失', '4', '环境'],
    ['员工在车间吸烟', '1', '行为'], ['应急物资台账与实际不符', '3', '管理'],
    ['污水站有限空间未设置警戒', '3', '管理'], ['叉车行驶速度过快', '1', '行为'],
    ['蒸煮锅压力表模糊不清', '2', '设备'], ['化验室试剂未分类存放', '3', '管理'],
    ['更衣室通道堵塞', '4', '环境'], ['切割设备防护罩缺失', '2', '设备'],
    ['应急预案未及时修订', '3', '管理'], ['新员工未完成三级教育上岗', '1', '行为'],
    ['冷库内货物堆放超高', '2', '设备'], ['手持电动工具绝缘破损', '2', '设备']
  ]
  const [maxHz] = await c.query('SELECT MAX(H.h) m FROM (SELECT CAST(SUBSTRING(hazard_code, 11) AS UNSIGNED) h FROM biz_safety_hazard) H')
  let hseq = Number(maxHz[0].m || 8) + 1
  const newHazards = []
  let n13 = 0
  for (let i = 0; i < hzDesc.length; i++) {
    const [desc, type, cat] = hzDesc[i]
    const d = daysAgo(ri(3, 120))
    const day = dstr(d).replace(/-/g, '')
    const code = `YH${day}${p2(hseq++)}`
    const level = rnd() < 0.18 ? '2' : '1'
    const who = pick(executors)
    const stRoll = rnd()
    let status, rectTime = null, verifyTime = null
    if (stRoll < 0.55) status = '6'
    else if (stRoll < 0.7) status = '5'
    else if (stRoll < 0.85) status = '4'
    else if (stRoll < 0.95) status = '3'
    else status = '1'
    if (['5', '6'].includes(status)) rectTime = fmt(daysAgo(ri(0, 3)))
    if (status === '6') verifyTime = fmt(daysAgo(ri(0, 2)))
    const deadline = dstr(new Date(d.getTime() + ri(3, 15) * 86400000))
    const deptEntry = pick(Object.entries(DEPT).filter(([k]) => k !== '101'))
    n13 += await ins('biz_safety_hazard', {
      hazard_code: code, risk_point_id: rps2.length ? pick(rps2).risk_point_id : null, hazard_desc: desc,
      hazard_type: type, hazard_level: level, photos: '[]', discover_person_id: UID[who] || 20, discover_person: who,
      discover_time: fmt(d), source_type: pick(['1', '2', '2', '3', '4']), task_id: null,
      dept_id: Number(deptEntry[0]), dept_name: deptEntry[1], person_id: UID[who] || 20, person_name: who,
      rectify_req: `按规范要求完成整改：${desc}`,
      rectify_deadline: deadline, hazard_status: status,
      rectify_desc: ['4', '5', '6'].includes(status) ? `已按要求整改：${desc}问题已消除，现场已恢复` : null,
      rectify_photos: ['4', '5', '6'].includes(status) ? '[]' : null, rectify_time: rectTime,
      verify_person_id: status === '6' ? UID['邓楠'] : null, verify_person: status === '6' ? '邓楠' : null,
      verify_time: verifyTime, verify_result: status === '6' ? '1' : null,
      verify_comment: status === '6' ? '现场复核，隐患已闭环' : null,
      approve_person_id: ['3', '4', '5', '6'].includes(status) ? UID['丰孟'] : null,
      approve_person: ['3', '4', '5', '6'].includes(status) ? '丰孟' : null,
      approve_time: ['3', '4', '5', '6'].includes(status) ? fmt(d) : null,
      approve_result: ['3', '4', '5', '6'].includes(status) ? '1' : null,
      approve_comment: ['3', '4', '5', '6'].includes(status) ? '同意整改' : null,
      enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: fmt(d), update_by: '', remark: cat
    }, 'hazard_desc')
    const [r] = await c.query('SELECT hazard_id FROM biz_safety_hazard WHERE hazard_desc=? ORDER BY hazard_id LIMIT 1', [desc])
    if (r.length) newHazards.push({ id: r[0].hazard_id, status, who, d, desc })
  }
  console.log(`[13] 新增隐患 ${n13} 条`)

  // 整改单 + 审核日志（针对已有 + 新增隐患）
  const [allHz] = await c.query('SELECT hazard_id, hazard_code, hazard_status, hazard_desc, discover_person, discover_time, verify_person FROM biz_safety_hazard ORDER BY hazard_id')
  let n14 = 0, n15 = 0
  for (const h of allHz) {
    n15 += await ins('biz_safety_hazard_audit_log', {
      hazard_id: h.hazard_id, audit_action: '0', audit_by: h.discover_person || '邓楠', audit_time: h.discover_time, audit_remark: '隐患已上报，待审批'
    }, ['hazard_id', 'audit_action'])
    if (['3', '4', '5', '6'].includes(h.hazard_status)) {
      n15 += await ins('biz_safety_hazard_audit_log', {
        hazard_id: h.hazard_id, audit_action: '1', audit_by: '丰孟', audit_time: h.discover_time, audit_remark: '同意整改，明确责任人与时限'
      }, ['hazard_id', 'audit_action'])
    }
    if (['4', '5', '6'].includes(h.hazard_status)) {
      n14 += await ins('biz_safety_rectify', {
        hazard_id: h.hazard_id, rectify_type: '1',
        rectify_plan: '按隐患描述逐项整改，完成后拍照留存', solution_desc: `已整改：${h.hazard_desc}`,
        before_photos: '[]', after_photos: '[]',
        rectify_person_id: UID[h.discover_person] || 20, rectify_person: h.discover_person,
        rectify_time: h.discover_time,
        verify_person_id: h.hazard_status === '6' ? UID['邓楠'] : null, verify_person: h.hazard_status === '6' ? '邓楠' : null,
        verify_time: h.hazard_status === '6' ? h.discover_time : null, verify_result: h.hazard_status === '6' ? '1' : null,
        verify_comment: h.hazard_status === '6' ? '整改到位，同意闭环' : null,
        del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null, dept_id: 108
      }, ['hazard_id'])
      n15 += await ins('biz_safety_hazard_audit_log', {
        hazard_id: h.hazard_id, audit_action: '3', audit_by: h.discover_person, audit_time: h.discover_time, audit_remark: '整改完成，申请验收'
      }, ['hazard_id', 'audit_action'])
    }
    if (h.hazard_status === '6') {
      n15 += await ins('biz_safety_hazard_audit_log', {
        hazard_id: h.hazard_id, audit_action: '4', audit_by: '邓楠', audit_time: h.discover_time, audit_remark: '现场复核通过，隐患闭环'
      }, ['hazard_id', 'audit_action'])
    }
  }
  console.log(`[14] 整改单 ${n14} 张 / 审核日志 ${n15} 条`)

  /* ---------- 15. 合规检查 ---------- */
  let n16 = 0
  const checks = [
    ['HG2026001', '危险化学品储存合规检查', 'WHP001', '液氨', '3', '2026-03-18', '邓楠', '1'],
    ['HG2026002', '危险化学品储存合规检查', 'WHP002', '氢氧化钠', '3', '2026-03-18', '邓楠', '1'],
    ['HG2026003', '重大危险源专项检查', null, null, '2', '2026-05-20', '邓楠', '2'],
    ['HG2026004', '特种设备定期检验合规检查', null, null, '1', '2026-06-25', '黄健', '1'],
    ['HG2026005', '消防设施完好性检查', null, null, '1', '2026-07-16', '邓楠', '1'],
    ['HG2026006', '有限空间作业合规检查', null, null, '3', '2026-08-28', '邓楠', '1'],
    ['HG2026007', '职业健康防护合规检查', null, null, '2', '2026-09-22', '孙丽', '1'],
    ['HG2026008', '重大危险源专项检查', null, null, '2', '2026-10-30', '邓楠', '1']
  ]
  for (const [code, name, mcode, mname, type, date, who, result] of checks) {
    n16 += await ins('biz_safety_compliance_check', {
      check_code: code, check_name: name, material_id: mcode ? matId[mcode] : null, material_name: mname,
      area_id: areaId['QY0109'] || null, area_name: '危险化学品仓库', check_type: type,
      check_items: JSON.stringify(['证照齐全有效', '储存条件符合要求', '安全设施完好', '台账记录完整', '人员培训到位']),
      check_date: date, checker: who, check_result: result,
      problems: result === '2' ? '液氨储罐区围堰有轻微破损；应急器材室呼吸器压力不足 2 具。' : null,
      rectification: result === '2' ? '已修复围堰，更换呼吸器气瓶，并于 5 个工作日内复查。' : null,
      rectify_deadline: result === '2' ? '2026-06-05' : null, rectify_status: result === '2' ? '2' : '0',
      attachment: null, enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'check_code')
  }
  console.log(`[15] 合规检查 ${n16} 条`)

  /* ---------- 16. 培训 ---------- */
  const courses = [
    ['KC001', '安全生产法律法规与主体责任', '2', '线上', 4, '邓楠'],
    ['KC002', '液氨制冷安全操作与应急处置', '2', '线下', 6, '胡明'],
    ['KC003', '有限空间作业安全', '2', '线下', 4, '邓楠'],
    ['KC004', '粉尘防爆与食品安全', '2', '线下', 3, '陈静'],
    ['KC005', '消防器材使用与疏散逃生', '2', '线下', 3, '邓楠'],
    ['KC006', '特种设备安全操作规范', '2', '线下', 4, '黄健'],
    ['KC007', '岗前三级安全教育', '2', '线下', 8, '邓楠'],
    ['KC008', '危险化学品安全管理', '2', '线上', 4, '邓楠']
  ]
  const courseId = {}
  let n17 = 0
  for (const [code, name, type, form, hours, instr] of courses) {
    n17 += await ins('biz_safety_training_course', {
      course_code: code, course_name: name, course_type: type, course_form: form, hours: d2(hours), instructor: instr,
      description: `${name}：依据《安全生产法》《食品安全法》及企业安全管理制度开展，含案例分析与实操演练。`,
      material_url: null, enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'course_code')
    const [r] = await c.query('SELECT course_id FROM biz_safety_training_course WHERE course_code=?', [code])
    courseId[code] = r[0].course_id
  }

  const plansTr = [
    ['PX2026001', '2026 年度安全生产教育培训计划', '1', '2026', 32],
    ['PX2026002', '特种作业人员复审培训计划', '2', '2026', 16],
    ['PX2026003', '有限空间作业专项培训计划', '4', '2026', 8],
    ['PX2026004', '危险化学品安全管理培训计划', '5', '2026', 12],
    ['PX2026005', '粉尘防爆专项培训计划', '3', '2026', 6],
    ['PX2026006', '应急处置与逃生演练培训计划', '6', '2026', 10]
  ]
  const planTrId = {}
  let n18 = 0
  for (const [code, name, type, year, hours] of plansTr) {
    n18 += await ins('biz_safety_training_plan', {
      plan_code: code, plan_name: name, plan_type: type, plan_year: year,
      target_persons: '全体员工及特种作业人员', total_hours: d2(hours), plan_status: '2',
      enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null, dept_id: 108
    }, 'plan_code')
    const [r] = await c.query('SELECT plan_id FROM biz_safety_training_plan WHERE plan_code=?', [code])
    planTrId[code] = r[0].plan_id
  }

  const recSpec = [
    ['KC007', 'PX2026001', 120, '会议室', '邓楠'],
    ['KC001', 'PX2026001', 90, '会议室', '邓楠'],
    ['KC005', 'PX2026006', 60, '厂区空地', '邓楠'],
    ['KC002', 'PX2026002', 45, '制冷机房', '胡明'],
    ['KC003', 'PX2026003', 30, '污水站', '邓楠'],
    ['KC008', 'PX2026004', 40, '危化品仓库', '邓楠'],
    ['KC004', 'PX2026005', 60, '加工车间', '陈静'],
    ['KC006', 'PX2026002', 20, '锅炉房', '黄健'],
    ['KC001', 'PX2026001', 70, '会议室', '邓楠'],
    ['KC005', 'PX2026006', 55, '厂区空地', '邓楠'],
    ['KC003', 'PX2026003', 25, '污水站', '邓楠'],
    ['KC007', 'PX2026001', 35, '会议室', '邓楠']
  ]
  let n19 = 0, n20 = 0, n21 = 0
  const attendeesPool = ['张伟', '刘洋', '黄健', '胡明', '杨帆', '徐婷', '高鹏', '周强', '陈静', '孙丽', '赵雷', '吴芳']
  for (let i = 0; i < recSpec.length; i++) {
    const [cc, pc, cnt, loc, trainer] = recSpec[i]
    const d = daysAgo(150 - i * 11)
    const row = {
      plan_id: planTrId[pc] || null, course_id: courseId[cc] || null, trainer_id: UID[trainer] || 20,
      course_name: courses.find(x => x[0] === cc)[1], course_type: cc, training_date: dstr(d),
      training_location: loc, hours: d2(courses.find(x => x[0] === cc)[4]), trainer, attachment: null,
      enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null, dept_id: 108
    }
    const added = await ins('biz_safety_training_record', row, ['course_name', 'training_date'])
    n19 += added
    const [rr] = await c.query('SELECT record_id FROM biz_safety_training_record WHERE course_name=? AND training_date=? LIMIT 1', [row.course_name, row.training_date])
    const rid = rr.length ? rr[0].record_id : null
    if (!rid) continue
    const picked = [...attendeesPool].sort(() => rnd() - 0.5).slice(0, Math.min(6, attendeesPool.length))
    for (const p of picked) {
      n20 += await ins('biz_safety_training_attendee', {
        record_id: rid, user_id: UID[p] || null, user_name: p, dept_id: 108, dept_name: '安全生产办公室',
        sign_in_status: '1', sign_in_time: fmt(d), sign_in_method: pick(['1', '2', '3']),
        exam_score: d2(ri(70, 100)), is_pass: '1', exam_time: fmt(d),
        enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
      }, ['record_id', 'user_name'])
    }
  }
  console.log(`[16] 培训：课程 ${n17} / 计划 ${n18} / 记录 ${n19} / 签到 ${n20}`)

  /* ---------- 17. 培训证书 ---------- */
  const certs = [
    ['PXZS001', '安全生产管理人员培训合格证', '1', '邓楠', '2025-11-20', '2028-11-19', '曲靖市应急管理局'],
    ['PXZS002', '制冷与空调作业操作证', '2', '胡明', '2021-09-15', '2027-09-14', '云南省应急管理厅'],
    ['PXZS003', '制冷与空调作业操作证', '2', '杨帆', '2021-09-15', '2027-09-14', '云南省应急管理厅'],
    ['PXZS004', '电工作业操作证', '2', '黄健', '2022-06-20', '2028-06-19', '云南省应急管理厅'],
    ['PXZS005', '焊接与热切割作业操作证', '2', '周强', '2022-11-08', '2028-11-07', '云南省应急管理厅'],
    ['PXZS006', '叉车司机特种作业证', '2', '高鹏', '2023-03-22', '2029-03-21', '曲靖市市场监督管理局'],
    ['PXZS007', '叉车司机特种作业证', '2', '徐婷', '2023-03-22', '2029-03-21', '曲靖市市场监督管理局'],
    ['PXZS008', '食品安全管理员培训证', '1', '陈静', '2025-06-10', '2028-06-09', '曲靖市市场监督管理局'],
    ['PXZS009', '有限空间作业培训合格证', '1', '张伟', '2026-08-21', '2029-08-20', '曲靖市应急管理局'],
    ['PXZS010', '消防设施操作员证', '1', '刘洋', '2024-05-16', '2029-05-15', '云南省消防救援总队']
  ]
  let n22 = 0
  for (const [no, name, type, who, issue, exp, org] of certs) {
    n22 += await ins('biz_safety_training_cert', {
      cert_no: no, cert_name: name, cert_type: type, user_id: UID[who] || null, user_name: who,
      dept_id: 108, dept_name: '安全生产办公室', record_id: null, course_id: null,
      issue_date: issue, expire_date: exp, issue_org: org, attachment: null, remind_status: '0',
      enterprise_id: ENT, status: '0', del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'cert_no')
  }
  console.log(`[17] 培训证书 ${n22} 本`)

  /* ---------- 18. 在线考试 ---------- */
  const examCats = ['安全生产法律法规', '危险化学品安全', '特种设备安全', '消防安全', '应急处置']
  const catIdMap = {}
  let n23 = 0
  for (let i = 0; i < examCats.length; i++) {
    n23 += await ins('biz_safety_exam_category', {
      parent_id: 0, category_name: examCats[i], order_num: i + 1, enterprise_id: ENT, status: '0', del_flag: '0',
      create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'category_name')
    const [r] = await c.query('SELECT category_id FROM biz_safety_exam_category WHERE category_name=?', [examCats[i]])
    catIdMap[examCats[i]] = r[0].category_id
  }

  const qs = [
    ['1', '《安全生产法》规定，生产经营单位的主要负责人对本单位安全生产工作负什么责任？', ['全面负责', '部分负责', '监督责任', '连带责任'], 'A', '安全生产法第五条：主要负责人对本单位安全生产工作全面负责。', '安全生产法律法规'],
    ['1', '生产经营单位应当对从业人员进行安全生产教育和培训，保证从业人员具备必要的安全生产知识。', ['正确', '错误'], 'A', '这是从业人员的法定权利与单位的法定义务。', '安全生产法律法规'],
    ['1', '发现事故隐患应当如何处理？', ['立即消除或采取防范措施', '等上级安排', '记录下来即可', '交接班时说明'], 'A', '安全生产法要求立即消除或者采取防范措施。', '安全生产法律法规'],
    ['1', '液氨泄漏时应向哪个方向撤离？', ['上风向', '下风向', '任意方向', '低洼处'], 'A', '氨气比空气轻且有毒，应向上风向、高处撤离。', '危险化学品安全'],
    ['2', '液氨的主要危险特性包括？', ['有毒', '可燃', '腐蚀性', '放射性'], 'ABC', '液氨有毒、可燃、具腐蚀性，不具放射性。', '危险化学品安全'],
    ['1', '氨气泄漏报警仪的报警设定值一般不超过多少 ppm？', ['25', '50', '100', '200'], 'A', '按 GB 50493，氨气 TLV 为 25ppm，一级报警取 25ppm。', '危险化学品安全'],
    ['1', '危险化学品仓库应实行的管理制度是？', ['双人收发、双人保管', '单人管理', '无需登记', '随意领用'], 'A', '剧毒及易制爆化学品须双人收发、双人保管。', '危险化学品安全'],
    ['1', '锅炉安全阀的作用是？', ['超压时自动泄压', '控制水位', '调节燃烧', '除尘'], 'A', '安全阀是锅炉最重要的安全附件之一。', '特种设备安全'],
    ['1', '压力容器定期检验周期一般不超过几年？', ['3', '5', '8', '10'], 'A', '按 TSG 21，一般 3~6 年，通常取 3 年首次、后续按等级。', '特种设备安全'],
    ['1', '叉车厂内行驶速度一般不得超过多少 km/h？', ['5', '10', '15', '20'], 'A', '厂内道路一般不超过 10km/h，车间内不超过 5km/h。', '特种设备安全'],
    ['2', '特种设备包括以下哪些？', ['锅炉', '压力容器', '起重机械', '厂内机动车辆'], 'ABCD', '特种设备目录中均属特种设备。', '特种设备安全'],
    ['1', '干粉灭火器适用于扑救哪类火灾？', ['固体、液体、气体及电气火灾', '仅固体', '仅气体', '仅金属'], 'A', 'ABC 干粉适用范围广。', '消防安全'],
    ['1', '发生火灾时正确的逃生方式是？', ['低姿匍匐沿疏散指示撤离', '乘坐电梯', '躲进库房', '返回取物'], 'A', '烟气上升，低姿撤离并禁止乘电梯。', '消防安全'],
    ['1', '消防通道的要求是？', ['保持畅通不得占用', '可临时堆放货物', '可停放车辆', '可锁闭'], 'A', '消防通道必须保持畅通。', '消防安全'],
    ['1', '灭火器压力表指针在哪个区域表示正常？', ['绿区', '红区', '黄区', '任意'], 'A', '绿区为正常，红区需充装。', '消防安全'],
    ['1', '有限空间作业必须先做什么？', ['通风、检测、审批', '直接进入', '戴手套即可', '先动火'], 'A', '先通风、再检测、后作业，并办理审批。', '应急处置'],
    ['2', '有限空间作业的安全措施包括？', ['气体检测', '强制通风', '专人监护', '穿戴防护装备'], 'ABCD', '四项均为必备措施。', '应急处置'],
    ['1', '发现同事触电应首先？', ['切断电源或用绝缘物挑开', '直接用手拉拽', '泼水降温', '等待救援'], 'A', '严禁徒手接触触电者。', '应急处置'],
    ['1', '人员氨中毒后应？', ['迅速撤离至新鲜空气处并就医', '继续作业', '大量饮水', '自行服药'], 'A', '脱离接触、保持呼吸道通畅并送医。', '应急处置'],
    ['3', '食品加工企业同样需要落实安全生产主体责任。', ['正确', '错误'], 'A', '食品安全与生产安全同为法定责任。', '安全生产法律法规']
  ]
  const qIds = []
  let n24 = 0
  for (const [type, content, options, answer, analysis, cat] of qs) {
    n24 += await ins('biz_safety_exam_question', {
      question_type: type, content, options: JSON.stringify(options), answer, analysis,
      category_id: catIdMap[cat] || null, category_name: cat, difficulty: pick(['1', '2', '2', '3']),
      job_types: null, status: '0', enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'content')
    const [r] = await c.query('SELECT question_id FROM biz_safety_exam_question WHERE content=?', [content])
    qIds.push({ id: r[0].question_id, q: { type, content, options, answer, analysis } })
  }

  const papers = [['2026 年度全员安全生产考试卷（A 卷）', 100, 80, 60], ['危险化学品与特种设备专项考试卷', 100, 80, 45]]
  const paperIds = []
  let n25 = 0, n26 = 0
  for (const [name, total, pass, dur] of papers) {
    n25 += await ins('biz_safety_exam_paper', {
      paper_name: name, course_id: null, course_name: null, total_score: d2(total), pass_score: d2(pass), duration: dur,
      shuffle_question: '1', shuffle_option: '1', switch_limit: 3, show_analysis: '1', status: '1', publish_by: 'admin',
      publish_time: NOW, enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'paper_name')
    const [r] = await c.query('SELECT paper_id FROM biz_safety_exam_paper WHERE paper_name=?', [name])
    const pid = r[0].paper_id
    paperIds.push(pid)
    let sort = 1
    for (const q of qIds.slice(0, 10)) {
      n26 += await ins('biz_safety_exam_paper_question', {
        paper_id: pid, question_id: q.id, question_type: q.q.type, content: q.q.content,
        options: JSON.stringify(q.q.options), answer: q.q.answer, analysis: q.q.analysis,
        score: d2(10), sort_no: sort++, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
      }, ['paper_id', 'question_id'])
    }
  }

  /* ---------- 19. 考试安排与成绩 ---------- */
  let n27 = 0, n28 = 0, n29 = 0, n30 = 0
  const examSpec = [[0, '2026 年度全员安全生产考试', '2026-09-18 09:00:00', '2026-09-18 18:00:00'], [1, '危险化学品与特种设备专项考试', '2026-10-24 09:00:00', '2026-10-24 18:00:00']]
  for (const [pi, name, st, et] of examSpec) {
    const pid = paperIds[pi]
    n27 += await ins('biz_safety_exam', {
      exam_name: name, paper_id: pid, paper_name: papers[pi][0], record_id: null, attendee_source: '1',
      retake_from_id: null, retake_count: 0, score_strategy: '1', start_time: st, end_time: et, exam_status: '3',
      publish_by: 'admin', publish_time: st, dept_id: 108, enterprise_id: ENT, del_flag: '0',
      create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, 'exam_name')
    const [er] = await c.query('SELECT exam_id FROM biz_safety_exam WHERE exam_name=?', [name])
    const eid = er[0].exam_id
    const group = ['张伟', '刘洋', '黄健', '胡明', '杨帆', '高鹏', '周强', '邓楠']
    for (const p of group) {
      n28 += await ins('biz_safety_exam_user', {
        exam_id: eid, user_id: UID[p] || null, user_name: p, dept_id: 108, enterprise_id: ENT,
        del_flag: '0', create_by: 'admin', create_time: NOW
      }, ['exam_id', 'user_name'])
      const score = ri(72, 100)
      const passed = score >= 80 ? '1' : '0'
      const row = {
        exam_id: eid, paper_id: pid, user_id: UID[p] || 0, user_name: p, worker_id: null, attendee_id: null,
        start_time: st, submit_time: st, used_seconds: ri(600, 2400), submit_type: pick(['1', '1', '2']),
        total_score: d2(score), is_pass: passed, judge_status: '1', switch_count: 0, exam_result: '4',
        dept_id: 108, enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
      }
      const added = await ins('biz_safety_exam_record', row, ['exam_id', 'user_name'])
      n29 += added
      if (added) {
        const [rr] = await c.query('SELECT record_id FROM biz_safety_exam_record WHERE exam_id=? AND user_name=?', [eid, p])
        const rid = rr[0].record_id
        const [pqs] = await c.query('SELECT id AS paper_question_id, question_id, answer FROM biz_safety_exam_paper_question WHERE paper_id=?', [pid])
        for (const pq of pqs) {
          const right = rnd() < 0.85
          n30 += await ins('biz_safety_exam_answer', {
            record_id: rid, paper_question_id: pq.paper_question_id, user_answer: right ? pq.answer : 'B',
            is_correct: right ? '1' : '0', score: d2(right ? 10 : 0), del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
          }, ['record_id', 'paper_question_id'])
        }
      }
    }
  }
  console.log(`[18-19] 考试：分类 ${n23} / 题库 ${n24} / 试卷 ${n25} / 组卷 ${n26} / 考试 ${n27} / 考生 ${n28} / 成绩 ${n29} / 答题 ${n30}`)

  /* ---------- 20. 到期提醒 ---------- */
  let n31 = 0
  const [expCerts] = await c.query("SELECT cert_no, cert_name, user_name, expire_date FROM biz_safety_training_cert WHERE expire_date IS NOT NULL ORDER BY expire_date")
  for (const x of expCerts.slice(0, 5)) {
    const days = Math.ceil((new Date(x.expire_date) - new Date('2026-11-17')) / 86400000)
    n31 += await ins('biz_safety_remind', {
      remind_type: '2', related_id: null, related_name: `${x.user_name} - ${x.cert_name}`,
      remind_content: `${x.user_name} 的《${x.cert_name}》将于 ${x.expire_date} 到期，请及时安排复审`,
      expire_date: x.expire_date, remind_days: 90, remind_status: days < 0 ? '0' : '1',
      notify_level: days < 30 ? '1' : '2', to_person_id: UID['邓楠'], to_person_name: '邓楠',
      read_time: null, handle_time: null, handle_by: null, handle_result: null,
      enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, ['remind_type', 'related_name'])
  }
  const [expEq] = await c.query("SELECT equipment_name, inspect_expire FROM biz_safety_equipment WHERE inspect_expire IS NOT NULL ORDER BY inspect_expire LIMIT 4")
  for (const x of expEq) {
    n31 += await ins('biz_safety_remind', {
      remind_type: '3', related_id: null, related_name: x.equipment_name,
      remind_content: `${x.equipment_name} 的定期检验将于 ${x.inspect_expire} 到期，请提前申报检验`,
      expire_date: x.inspect_expire, remind_days: 60, remind_status: '0', notify_level: '1',
      to_person_id: UID['邓楠'], to_person_name: '邓楠', read_time: null, handle_time: null, handle_by: null, handle_result: null,
      enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, ['remind_type', 'related_name'])
  }
  const [expPlan] = await c.query("SELECT plan_name, expire_date FROM biz_safety_emergency_plan ORDER BY expire_date LIMIT 2")
  for (const x of expPlan) {
    n31 += await ins('biz_safety_remind', {
      remind_type: '6', related_id: null, related_name: x.plan_name,
      remind_content: `《${x.plan_name}》将于 ${x.expire_date} 到期，请组织修订并重新备案`,
      expire_date: x.expire_date, remind_days: 90, remind_status: '0', notify_level: '2',
      to_person_id: UID['邓楠'], to_person_name: '邓楠', read_time: null, handle_time: null, handle_by: null, handle_result: null,
      enterprise_id: ENT, del_flag: '0', create_by: 'admin', create_time: NOW, update_by: '', remark: null
    }, ['remind_type', 'related_name'])
  }
  console.log(`[20] 到期提醒 ${n31} 条`)

  /* ---------- 汇总 ---------- */
  const [[s]] = await c.query(`SELECT
    (SELECT COUNT(*) FROM biz_safety_area) area,(SELECT COUNT(*) FROM biz_safety_emergency_plan) plan,
    (SELECT COUNT(*) FROM biz_safety_emergency_material) em,(SELECT COUNT(*) FROM biz_safety_drill) drill,
    (SELECT COUNT(*) FROM biz_safety_incident) inc,(SELECT COUNT(*) FROM biz_safety_major_hazard) mh,
    (SELECT COUNT(*) FROM biz_safety_equipment) eq,(SELECT COUNT(*) FROM biz_safety_inspection_task) it,
    (SELECT COUNT(*) FROM biz_safety_hazard) hz,(SELECT COUNT(*) FROM biz_safety_rectify) rect,
    (SELECT COUNT(*) FROM biz_safety_compliance_check) cc,(SELECT COUNT(*) FROM biz_safety_material) mat,
    (SELECT COUNT(*) FROM biz_safety_training_plan) tp,(SELECT COUNT(*) FROM biz_safety_training_record) tr,
    (SELECT COUNT(*) FROM biz_safety_worker) wk,(SELECT COUNT(*) FROM biz_safety_exam_question) q,
    (SELECT COUNT(*) FROM biz_safety_exam_record) er,(SELECT COUNT(*) FROM biz_safety_remind) rm`)
  console.log('\n[汇总]', s)
  await c.end()
})()
