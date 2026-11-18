/**
 * 设备管理（dms_*）数据补全 —— 幂等
 *   dms_equipment_category 二级分类 / dms_equipment 台账 / dms_pm_plan 维保计划
 *   dms_inspection_route + dms_inspection_task 巡检 / dms_spare_part(+stock/record) 备件
 *   dms_work_order(+log) 维修工单 / dms_repair_faq 知识库 / dms_equipment_log 变更日志
 *   dms_data_collection_config + dms_data_record 运行采集
 *
 * 同时把历史数据的英文枚举（running/running/A/B）纠正为字典值（0/0/0/1）
 * 用法：node scripts/sim_dms_data.js
 */
const mysql = require('mysql2/promise')

const DB = { host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true }
const NOW = '2026-11-17 09:00:00'

// 车间/部门
const DEPT = {
  104: '生产部', 105: '质量管理部', 106: '仓储物流部', 109: '屠宰分割车间', 110: '加工车间'
}
// 人员
const U = { 张伟: 8, 刘洋: 9, 陈静: 10, 赵雷: 11, 孙丽: 12, 杨帆: 13, 胡明: 14, 徐婷: 15, 高鹏: 16, 黄健: 21, 吴芳: 7 }

let seed = 20261117
const rnd = () => { seed = (seed * 9301 + 49297) % 233280; return seed / 233280 }
const pick = a => a[Math.floor(rnd() * a.length)]
const ri = (a, b) => a + Math.floor(rnd() * (b - a + 1))
const d2 = n => Number(n).toFixed(2)
const pad = (n, w = 3) => String(n).padStart(w, '0')
const dt = (y, m, d, h = 9, mi = 0) => `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mi).padStart(2, '0')}:00`
const dateOnly = s => s.slice(0, 10)

;(async () => {
  const c = await mysql.createConnection(DB)
  const exists = async (sql, p) => { const [r] = await c.query(sql, p); return r.length > 0 }

  /* ---------------- A. 纠正历史英文枚举 ---------------- */
  const statusMap = { running: '0', idle: '1', repairing: '2', stopped: '3', fault: '4', scrapped: '5' }
  const levelMap = { A: '0', B: '1', C: '2', D: '3' }
  const [eqs] = await c.query('SELECT equipment_id, equipment_status, equipment_level FROM dms_equipment')
  let fixed = 0
  for (const e of eqs) {
    const s = statusMap[e.equipment_status] ?? e.equipment_status
    const l = levelMap[e.equipment_level] ?? e.equipment_level
    if (s !== e.equipment_status || l !== e.equipment_level) {
      await c.query('UPDATE dms_equipment SET equipment_status=?, equipment_level=? WHERE equipment_id=?', [s, l, e.equipment_id])
      fixed++
    }
  }
  console.log(`[A] 纠正设备枚举 ${fixed} 台`)

  /* ---------------- B. 二级设备分类 ---------------- */
  const cats = [
    [1, 'LB01', '屠宰设备', 'LB0101'], [1, 'LB01', '分割设备', 'LB0102'],
    [2, 'LB02', '灌装设备', 'LB0201'], [2, 'LB02', '包装设备', 'LB0202'],
    [3, 'LB03', '冷库机组', 'LB0301'], [3, 'LB03', '速冻设备', 'LB0302'],
    [4, 'LB04', '熟制设备', 'LB0401'], [4, 'LB04', '动力设备', 'LB0402'],
    [5, 'LB05', '计量器具', 'LB0501'], [5, 'LB05', '检测仪器', 'LB0502']
  ]
  let catAdd = 0
  for (const [pid, pcode, name, code] of cats) {
    if (await exists('SELECT 1 FROM dms_equipment_category WHERE category_code=?', [code])) continue
    await c.query(
      'INSERT INTO dms_equipment_category (parent_id,ancestors,category_code,category_name,category_level,order_num,status,del_flag,create_by,create_time,update_by,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)',
      [pid, '0', code, name, 2, catAdd + 1, '0', '0', 'admin', NOW, '', null]
    )
    catAdd++
  }
  console.log(`[B] 新增二级分类 ${catAdd} 条`)

  const [catRows] = await c.query('SELECT category_id, category_code FROM dms_equipment_category')
  const catByCode = Object.fromEntries(catRows.map(r => [r.category_code, r.category_id]))

  /* ---------------- C. 设备台账 ---------------- */
  const equip = [
    ['SB20260009', '真空滚揉机', 'GR-1000', '诸城兴盛机械', 'LB0401', 104, '腌制发酵车间', '0', '1', '刘洋', '2022-09-10', 128000, '2024-09-09'],
    ['SB20260010', '斩拌机', 'ZB-125', '诸城兴盛机械', 'LB0401', 104, '灌装车间', '0', '1', '刘洋', '2022-09-10', 76000, '2024-09-09'],
    ['SB20260011', '烟熏炉', 'YX-500', '诸城兴盛机械', 'LB0401', 104, '烟熏车间', '0', '0', '黄健', '2022-09-20', 265000, '2024-09-19'],
    ['SB20260012', '可倾式蒸煮锅', 'ZG-800', '温州华通机械', 'LB0401', 104, '熟制车间', '0', '2', '刘洋', '2022-10-08', 58000, '2024-10-07'],
    ['SB20260013', '全自动拉伸膜包装机', 'DL-320', '青岛艾讯包装', 'LB0202', 104, '包装车间', '0', '0', '刘洋', '2023-01-12', 460000, '2025-01-11'],
    ['SB20260014', '喷码贴标一体机', 'PM-200', '广州易科标识', 'LB0202', 104, '包装车间', '0', '2', '刘洋', '2023-01-12', 68000, '2025-01-11'],
    ['SB20260015', '气调保鲜包装机', 'QT-4', '青岛艾讯包装', 'LB0202', 104, '包装车间', '4', '1', '刘洋', '2023-02-20', 198000, '2025-02-19'],
    ['SB20260016', '螺旋速冻机', 'SD-1000', '烟台冰轮股份', 'LB0302', 104, '速冻车间', '0', '0', '黄健', '2022-07-18', 520000, '2024-07-17'],
    ['SB20260017', '成品冷藏库（0~4℃）', 'LK-80', '大连冰山集团', 'LB0301', 106, '成品冷库', '0', '0', '胡明', '2022-05-20', 780000, '2025-05-19'],
    ['SB20260018', '原料冷冻库（-18℃）', 'LD-120', '大连冰山集团', 'LB0301', 106, '原料冷库', '0', '0', '胡明', '2022-05-20', 860000, '2025-05-19'],
    ['SB20260019', '氨气泄漏报警装置', 'NH3-A4', '大连冰山集团', 'LB0301', 106, '制冷机房', '0', '0', '胡明', '2022-06-01', 46000, '2025-05-31'],
    ['SB20260020', '反渗透水处理设备', 'RO-2T', '昆明水捷环保', 'LB0402', 104, '动力间', '0', '1', '黄健', '2022-08-05', 92000, '2024-08-04'],
    ['SB20260021', '蒸汽锅炉', 'WNS2-1.25', '曲靖锅炉制造', 'LB0402', 104, '锅炉房', '2', '0', '黄健', '2020-11-15', 398000, '2023-11-14'],
    ['SB20260022', '螺杆空气压缩机', 'KA-75', '阿特拉斯·科普柯', 'LB0402', 104, '动力间', '0', '1', '黄健', '2022-08-05', 156000, '2024-08-04'],
    ['SB20260023', '电子台秤', 'TCS-300', '上海耀华称重', 'LB0501', 104, '包装车间', '0', '2', '陈静', '2023-03-06', 4800, '2025-03-05'],
    ['SB20260024', '数显温度计', 'WSS-411', '上海自动化仪表', 'LB0501', 105, '各生产车间', '0', '3', '陈静', '2023-03-06', 1200, '2025-03-05'],
    ['SB20260025', '电子分析天平', 'FA2004', '上海精密科学', 'LB0501', 105, '化验室', '0', '2', '孙丽', '2022-12-10', 8600, '2024-12-09'],
    ['SB20260026', '立式压力蒸汽灭菌锅', 'LX-B50', '上海博迅医疗', 'LB0502', 105, '化验室', '3', '1', '孙丽', '2022-12-10', 16800, '2024-12-09'],
    ['SB20260027', '臭氧发生器', 'OZ-20', '广州创环臭氧', 'LB0202', 104, '包装车间', '1', '3', '刘洋', '2023-04-18', 26000, '2025-04-17'],
    ['SB20260028', '螺杆真空泵', 'VP-60', '上海真空泵厂', 'LB0402', 104, '动力间', '5', '3', '黄健', '2019-06-20', 32000, null]
  ]
  let eqAdd = 0
  for (const [code, name, model, mfr, catCode, deptId, loc, st, lv, who, buy, val, war] of equip) {
    if (await exists('SELECT 1 FROM dms_equipment WHERE equipment_code=?', [code])) continue
    await c.query(
      `INSERT INTO dms_equipment (equipment_code,equipment_name,asset_code,category_id,model,serial_number,manufacturer,supplier,
        purchase_date,install_date,original_value,dept_id,dept_name,install_location,equipment_status,equipment_level,warranty_date,
        responsible_id,responsible_name,status,del_flag,create_by,create_time,update_by,remark)
       VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
      [code, name, 'ZC' + code.slice(2), catByCode[catCode] || null, model, model + '-' + pad(ri(100, 999)), mfr, mfr,
        buy, buy, d2(val), deptId, DEPT[deptId] || '生产部', loc, st, lv, war,
        U[who] || 9, who, '0', '0', 'admin', NOW, '', '设备管理模块基础台账']
    )
    eqAdd++
  }
  console.log(`[C] 新增设备 ${eqAdd} 台（累计 ${eqs.length + eqAdd} 台）`)

  const [allEq] = await c.query('SELECT equipment_id, equipment_code, equipment_name, category_id, dept_id, dept_name, install_location, responsible_id, responsible_name, equipment_level, purchase_date, original_value FROM dms_equipment ORDER BY equipment_id')

  /* ---------------- D. 维保计划 ---------------- */
  const pmTpl = {
    LB0101: ['刀具刃口检查与更换', '传动链条润滑', '8', '月'], LB0102: ['锯条张紧度校验', '导轨清洁润滑', '8', '月'],
    LB0201: ['灌装嘴密封件检查', '料斗清洗消毒验证', '6', '月'], LB0202: ['热封刀温度校准', '真空泵油位检查', '6', '月'],
    LB0301: ['冷凝器翅片清洗', '制冷剂压力检测', '12', '月'], LB0302: ['蒸发器除霜', '网带张紧与润滑', '10', '月'],
    LB0401: ['夹层锅压力表校验', '搅拌轴密封更换', '8', '月'], LB0402: ['空滤/油滤更换', '安全阀起跳试验', '10', '月'],
    LB0501: ['计量器具周期检定', '砝码比对校准', '4', '月'], LB0502: ['仪器期间核查', '标准物质更换', '4', '月'],
    '1': ['整机点检与紧固', '安全防护装置检查', '8', '月'],
    '2': ['整机点检与紧固', '安全防护装置检查', '8', '月'],
    '3': ['整机点检与紧固', '安全防护装置检查', '8', '月'],
    '4': ['整机点检与紧固', '安全防护装置检查', '8', '月'],
    '5': ['整机点检与紧固', '安全防护装置检查', '8', '月']
  }
  const catCodeById = Object.fromEntries(catRows.map(r => [r.category_id, r.category_code]))
  let pmAdd = 0
  for (const e of allEq) {
    if (e.equipment_code === 'SB20260028') continue
    const cc = catCodeById[e.category_id] || '1'
    const tpl = pmTpl[cc] || pmTpl['1']
    const planName = `${e.equipment_name}月度维保`
    if (await exists('SELECT 1 FROM dms_pm_plan WHERE plan_name=?', [planName])) continue
    const cycle = cc === 'LB0301' ? 3 : (cc === 'LB0501' || cc === 'LB0502' ? 6 : 1)
    const next = new Date(2026, 10, 20 + ri(0, 25))
    await c.query(
      `INSERT INTO dms_pm_plan (plan_name,equipment_id,category_id,trigger_type,cycle_value,cycle_unit,task_list,spare_part_list,estimated_hours,advance_days,auto_assign,assignee_id,assignee_name,last_execute_time,next_execute_time,status,del_flag,create_by,create_time,update_by,remark)
       VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
      [planName, e.equipment_id, e.category_id, '0', cycle, '月',
        JSON.stringify(tpl.slice(0, 2).map((t, i) => ({ seq: i + 1, item: t }))),
        JSON.stringify([]), d2(tpl[2]), 3, '1', e.responsible_id, e.responsible_name,
        dt(2026, 10, 20 + ri(0, 9)), next.toISOString().slice(0, 10) + ' 08:00:00',
        '0', '0', 'admin', NOW, '', null]
    )
    pmAdd++
  }
  console.log(`[D] 新增维保计划 ${pmAdd} 条`)

  /* ---------------- E. 巡检路线与任务 ---------------- */
  const routes = [
    ['XJ001', '屠宰分割线日巡检', 109, '屠宰分割车间', '0', 1, [1, 9, 10]],
    ['XJ002', '包装车间日巡检', 104, '生产部', '0', 1, [2, 3, 13, 14, 15]],
    ['XJ003', '制冷机房周巡检', 106, '仓储物流部', '1', 1, [4, 17, 18, 19]],
    ['XJ004', '动力设备周巡检', 104, '生产部', '1', 1, [20, 21, 22]]
  ]
  const routeIdByCode = {}
  let routeAdd = 0
  for (const [code, name, deptId, deptName, cycleType, cycleValue, eqIds] of routes) {
    const [ex] = await c.query('SELECT route_id FROM dms_inspection_route WHERE route_code=?', [code])
    if (ex.length) { routeIdByCode[code] = ex[0].route_id; continue }
    const items = ['设备外观与清洁', '运行声音与振动', '温度/压力读数', '安全防护装置', '跑冒滴漏']
      .map((t, i) => ({ seq: i + 1, item: t, standard: '正常', method: '目视/听诊' }))
    const [r] = await c.query(
      `INSERT INTO dms_inspection_route (route_name,route_code,dept_id,dept_name,cycle_type,cycle_value,equipment_ids,inspection_items,status,del_flag,create_by,create_time,update_by,remark)
       VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
      [name, code, deptId, deptName, cycleType, cycleValue, JSON.stringify(eqIds), JSON.stringify(items), '0', '0', 'admin', NOW, '', null]
    )
    routeIdByCode[code] = r.insertId
    routeAdd++
  }
  console.log(`[E1] 新增巡检路线 ${routeAdd} 条`)

  // 近 90 天巡检任务
  const inspectors = ['张伟', '刘洋', '黄健', '胡明', '杨帆']
  let taskAdd = 0
  let seq = 1
  const [[{ n: itCnt }]] = await c.query('SELECT COUNT(*) n FROM dms_inspection_task')
  const skipTask = Number(itCnt) > 0
  const start = new Date(2026, 7, 20)
  for (let day = 0; day < 90 && !skipTask; day++) {
    const cur = new Date(start.getTime() + day * 86400000)
    const y = cur.getFullYear(), m = cur.getMonth() + 1, dd = cur.getDate()
    for (const [code] of routes) {
      const isDaily = code === 'XJ001' || code === 'XJ002'
      if (!isDaily && cur.getDay() !== 1) continue
      if (cur.getTime() > new Date(2026, 10, 16).getTime()) continue
      const who = pick(inspectors)
      const taskNo = `XJ${y}${String(m).padStart(2, '0')}${String(dd).padStart(2, '0')}${pad(seq++, 3)}`
      if (await exists('SELECT 1 FROM dms_inspection_task WHERE task_no=?', [taskNo])) continue
      const isPast = day < 87
      const status = isPast ? (rnd() < 0.96 ? '2' : '1') : '0'
      const abnormal = status === '2' ? (rnd() < 0.18 ? ri(1, 3) : 0) : 0
      const st = dt(y, m, dd, 8, ri(0, 50))
      const [r] = await c.query(
        `INSERT INTO dms_inspection_task (task_no,route_id,route_name,plan_date,inspector_id,inspector_name,task_status,overdue_flag,start_time,complete_time,abnormal_count,result_data,photo_urls,del_flag,create_by,create_time,update_by,remark)
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
        [taskNo, routeIdByCode[code], routes.find(x => x[0] === code)[1], dateOnly(st), U[who] || 9, who,
          status, '0', st, status === '2' ? dt(y, m, dd, 9, ri(0, 59)) : null, abnormal,
          JSON.stringify({ total: 5, pass: 5 - abnormal, abnormal }), JSON.stringify([]),
          '0', 'admin', NOW, '', null]
      )
      taskAdd++
    }
  }
  console.log(`[E2] 新增巡检任务 ${taskAdd} 条`)

  /* ---------------- F. 备件 ---------------- */
  const parts = [
    ['BJ0001', '不锈钢圆刀片', '7', 'φ120×1.5mm', '片', '屠宰分割设备', 40, 200, 60, 128, 'A区-01-02', 46],
    ['BJ0002', '分割锯条', '7', '1650×16×0.6mm', '条', '屠宰分割设备', 20, 120, 30, 74, 'A区-01-03', 68],
    ['BJ0003', '深沟球轴承', '1', '6205-2RS', '套', '通用', 10, 60, 20, 36, 'A区-02-01', 18],
    ['BJ0004', '带座外球面轴承', '1', 'UCP208', '套', '包装设备', 6, 40, 12, 22, 'A区-02-02', 42],
    ['BJ0005', 'O型密封圈', '3', 'φ45×3.5 食品级', '个', '灌装设备', 50, 300, 100, 186, 'B区-01-01', 2],
    ['BJ0006', '骨架油封', '3', 'TC 35×62×10', '个', '通用', 20, 120, 40, 66, 'B区-01-02', 5],
    ['BJ0007', '三角皮带', '4', 'B-1650', '条', '动力设备', 8, 40, 12, 18, 'B区-02-01', 12],
    ['BJ0008', '链条', '4', '08B-1 米', '米', '包装设备', 10, 60, 20, 34, 'B区-02-02', 9],
    ['BJ0009', '不锈钢网带', '10', '宽600 节距25.4', '米', '速冻设备', 5, 30, 10, 14, 'C区-01-01', 260],
    ['BJ0010', '初效空气滤芯', '5', '592×592×46', '个', '动力设备', 8, 40, 12, 21, 'C区-01-02', 35],
    ['BJ0011', '罗茨风机皮带轮', '4', 'φ180', '个', '动力设备', 2, 10, 4, 6, 'C区-02-01', 128],
    ['BJ0012', '制冷剂 R717 氨', '9', '99.8% 50kg/瓶', '瓶', '制冷设备', 2, 12, 4, 5, '危化品柜-01', 850],
    ['BJ0013', '冷冻机油', '8', '46# 18L/桶', '桶', '制冷设备', 2, 10, 3, 6, '危化品柜-02', 420],
    ['BJ0014', '食品级润滑油', '8', 'NLGI 2 400g', '支', '通用', 10, 50, 20, 28, 'C区-03-01', 36],
    ['BJ0015', '交流接触器', '5', 'CJX2-2510 220V', '个', '电气', 6, 30, 10, 17, 'D区-01-01', 68],
    ['BJ0016', '热继电器', '5', 'NR2-25 12-18A', '个', '电气', 4, 20, 8, 11, 'D区-01-02', 55],
    ['BJ0017', '铠装热电偶', '5', 'WRN-230 K型', '支', '熟制设备', 6, 30, 10, 19, 'D区-02-01', 96],
    ['BJ0018', '温度传感器', '5', 'PT100 A级', '支', '制冷设备', 8, 40, 12, 25, 'D区-02-02', 78],
    ['BJ0019', '气缸', '6', 'SC63×100', '个', '包装设备', 4, 20, 8, 12, 'E区-01-01', 145],
    ['BJ0020', '电磁阀', '6', '2W-025-08 220V', '个', '通用', 6, 30, 10, 16, 'E区-01-02', 88],
    ['BJ0021', '真空泵油', '8', 'VM-100 1L', '瓶', '包装设备', 6, 30, 10, 22, 'E区-02-01', 110],
    ['BJ0022', '热封刀加热片', '5', 'AC220V 300W', '片', '包装设备', 8, 40, 12, 26, 'E区-02-02', 132],
    ['BJ0023', '氨用压力表', '11', 'YA-100 0~2.5MPa', '块', '制冷设备', 4, 20, 6, 9, 'F区-01-01', 168],
    ['BJ0024', '冷库门封条', '10', '磁性 三元乙丙', '米', '冷库', 20, 100, 30, 58, 'F区-01-02', 26]
  ]
  const partIds = {}
  let partAdd = 0, stockAdd = 0, recAdd = 0
  for (const [code, name, type, spec, unit, appCat, min, max, safe, cur, loc, price] of parts) {
    let pid
    const [ex] = await c.query('SELECT part_id FROM dms_spare_part WHERE part_code=?', [code])
    if (ex.length) pid = ex[0].part_id
    else {
      const [r] = await c.query(
        `INSERT INTO dms_spare_part (part_code,part_name,part_type,spec_model,unit,applicable_category,supplier_id,stock_min,stock_max,safety_stock,current_stock,storage_location,supplier,status,del_flag,create_by,create_time,update_by,remark)
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
        [code, name, type, spec, unit, appCat, null, d2(min), d2(max), d2(safe), d2(cur), loc, '曲靖机电物资有限公司', '0', '0', 'admin', NOW, '', null]
      )
      pid = r.insertId
      partAdd++
    }
    partIds[code] = { id: pid, name, loc, cur, price }

    if (!(await exists('SELECT 1 FROM dms_spare_part_stock WHERE part_id=? AND warehouse_name=?', [pid, '备件库']))) {
      await c.query(
        `INSERT INTO dms_spare_part_stock (part_id,warehouse_name,storage_location,current_stock,lock_qty,stock_min,stock_max,safety_stock,version,status,del_flag,create_by,create_time,update_by,remark)
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
        [pid, '备件库', loc, d2(cur), '0.00', d2(min), d2(max), d2(safe), 0, '0', '0', 'admin', NOW, '', null]
      )
      stockAdd++
    }

    // 备件流水：入库 2 次 + 领用若干次，最后余额 = cur（已有流水则跳过）
    if (await exists('SELECT 1 FROM dms_spare_part_record WHERE part_id=?', [pid])) continue
    const moves = []
    const in1 = Math.max(Math.ceil(cur * 0.6) + ri(2, 8), 5)
    const out1 = Math.max(Math.ceil(cur * 0.25), 1)
    const in2 = Math.max(Math.ceil(cur * 0.35), 3)
    moves.push({ t: '0', q: in1, date: '2026-06-18', doc: 'RK20260618-' + code, sup: '曲靖机电物资有限公司' })
    moves.push({ t: '1', q: out1, date: '2026-07-22', doc: 'LY20260722-' + code, sup: '生产部' })
    moves.push({ t: '0', q: in2, date: '2026-09-05', doc: 'RK20260905-' + code, sup: '曲靖机电物资有限公司' })
    if (cur > 0 && rnd() < 0.7) moves.push({ t: '1', q: Math.max(Math.floor(cur * 0.15), 1), date: '2026-10-18', doc: 'LY20261018-' + code, sup: '生产部' })
    let stock = cur
    for (const m of moves.reverse()) stock += (m.t === '1' ? m.q : -m.q)   // 反推初始
    if (stock < 0) { stock = cur; }
    for (const m of moves.reverse()) {
      if (await exists('SELECT 1 FROM dms_spare_part_record WHERE document_code=?', [m.doc])) continue
      const before = stock
      const after = m.t === '0' ? before + m.q : before - m.q
      if (after < 0) continue
      await c.query(
        `INSERT INTO dms_spare_part_record (part_id,stock_id,document_code,source_type,target_type,supplier_or_dept,receiver,part_code,part_name,move_type,quantity,before_stock,after_stock,ref_type,ref_id,ref_no,operator_id,operator_name,operate_date,storage_location,create_time,remark)
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
        [pid, null, m.doc, m.t === '0' ? '0' : '1', m.t === '0' ? '1' : '0', m.sup, m.t === '1' ? '刘洋' : '杨帆',
          code, name, m.t, d2(m.q), d2(before), d2(after), null, null, null,
          m.t === '1' ? 9 : 13, m.t === '1' ? '刘洋' : '杨帆', m.date, loc, m.date + ' 10:00:00', null]
      )
      stock = after
      recAdd++
    }
  }
  console.log(`[F] 备件 ${partAdd} 种 / 库存 ${stockAdd} 条 / 流水 ${recAdd} 条`)

  /* ---------------- G. 维修工单 ---------------- */
  const faults = [
    ['真空度不足，包装袋封口不牢', '真空泵油乳化，更换真空泵油并清洗泵腔', '真空泵油×1'],
    ['热封刀温度波动，封口出现虚封', '热封刀加热片局部损坏，更换加热片并重新标定温度', '热封刀加热片×1'],
    ['设备运行异响，振动偏大', '传动轴承磨损，更换轴承并补充润滑脂', '深沟球轴承×2'],
    ['输送带跑偏', '张紧装置松动，调整张紧并校正托辊', '无'],
    ['制冷效果下降，库温偏高', '冷凝器翅片积尘，清洗冷凝器并检查制冷剂压力', '无'],
    ['气动执行机构动作迟缓', '气缸密封老化，更换气缸', '气缸×1'],
    ['灌装量不稳定，误差超标', '灌装嘴密封圈磨损，更换密封圈', 'O型密封圈×4'],
    ['控制柜报警，接触器烧蚀', '接触器触点烧蚀，更换交流接触器', '交流接触器×1'],
    ['蒸汽压力不足，升温慢', '安全阀起跳后未复位，重新整定安全阀', '无'],
    ['喷码模糊，字符残缺', '喷头堵塞，清洗喷头并更换墨水', '无'],
    ['温度显示异常，读数跳变', '热电偶接线松动，紧固接线并更换热电偶', '铠装热电偶×1'],
    ['设备漏油', '骨架油封老化失效，更换油封', '骨架油封×2']
  ]
  const repairers = ['黄健', '刘洋', '张伟', '胡明']
  let woAdd = 0, wlogAdd = 0
  let woSeq = 1
  const [[{ n: woCnt }]] = await c.query('SELECT COUNT(*) n FROM dms_work_order')
  const skipWO = Number(woCnt) > 0
  for (let i = 0; i < 34 && !skipWO; i++) {
    const e = allEq[ri(0, allEq.length - 1)]
    if (e.equipment_code === 'SB20260028') continue
    const dayOffset = ri(0, 150)
    const base = new Date(2026, 10, 16)
    const d = new Date(base.getTime() - dayOffset * 86400000)
    const y = d.getFullYear(), m = d.getMonth() + 1, dd = d.getDate()
    const orderNo = `WX${y}${String(m).padStart(2, '0')}${String(dd).padStart(2, '0')}${pad(woSeq++, 3)}`
    if (await exists('SELECT 1 FROM dms_work_order WHERE order_no=?', [orderNo])) continue

    const otype = rnd() < 0.6 ? '0' : (rnd() < 0.55 ? '1' : (rnd() < 0.6 ? '2' : '3'))
    const f = otype === '1' ? ['按维保计划执行月度保养', '清洁、紧固、润滑、参数校验', '食品级润滑油×1'] : pick(faults)
    const reporter = otype === '2' ? pick(['张伟', '刘洋', '胡明']) : pick(['刘洋', '张伟', '胡明', '黄健', '陈静'])
    const assignee = otype === '1' ? '黄健' : pick(repairers)
    const prio = otype === '0' ? (rnd() < 0.3 ? '0' : (rnd() < 0.6 ? '1' : '2')) : '2'
    // 状态：越久越可能已验收
    let st
    if (dayOffset > 30) st = rnd() < 0.85 ? '5' : '4'
    else if (dayOffset > 12) st = pick(['5', '5', '4', '3'])
    else if (dayOffset > 4) st = pick(['3', '2', '1'])
    else st = pick(['0', '1', '2'])
    // 时间必须严格递增：报修 -> 派工 -> 到场 -> 完工
    const p2 = n => String(n).padStart(2, '0')
    const fmt = t => `${t.getFullYear()}-${p2(t.getMonth() + 1)}-${p2(t.getDate())} ${p2(t.getHours())}:${p2(t.getMinutes())}:00`
    const tReport = new Date(y, m - 1, dd, ri(7, 15), ri(0, 59))
    const respHours = prio === '0' ? 2 : (prio === '1' ? 4 : 8)
    const procHours = prio === '0' ? 8 : (prio === '1' ? 24 : 72)
    const tAssign = new Date(tReport.getTime() + ri(15, 80) * 60000)
    const tArrive = new Date(tAssign.getTime() + ri(10, 45) * 60000)
    const tComplete = new Date(tArrive.getTime() + ri(60, 330) * 60000)
    const reportTime = fmt(tReport)
    const assignTime = ['1', '2', '3', '4', '5'].includes(st) ? fmt(tAssign) : null
    const arriveTime = ['2', '3', '4', '5'].includes(st) ? fmt(tArrive) : null
    const completeTime = ['4', '5'].includes(st) ? fmt(tComplete) : null
    const slaResp = fmt(new Date(tReport.getTime() + respHours * 3600000))
    const slaProc = fmt(new Date(tReport.getTime() + procHours * 3600000))
    const downtime = completeTime ? d2(ri(30, 240) / 60) : null
    const cost = ['4', '5'].includes(st) ? d2(ri(50, 2600)) : null
    const [r] = await c.query(
      `INSERT INTO dms_work_order (order_no,equipment_id,equipment_code,equipment_name,order_type,fault_description,reporter_id,reporter_name,report_time,priority,order_status,assignee_id,assignee_name,assign_time,arrive_time,sla_response_deadline,sla_process_deadline,sla_timeout_status,sla_escalated,fault_cause,repair_measure,spare_parts_used,task_checklist,repair_cost,downtime_duration,complete_time,complete_remark,deviation_reason,assigner_id,assigner_name,verifier_id,verifier_name,verify_time,verify_opinion,rating,del_flag,create_by,create_time,update_by,update_time,remark)
       VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
      [orderNo, e.equipment_id, e.equipment_code, e.equipment_name, otype, f[0],
        U[reporter] || 9, reporter, reportTime, prio, st,
        ['1', '2', '3', '4', '5'].includes(st) ? (U[assignee] || 21) : null,
        ['1', '2', '3', '4', '5'].includes(st) ? assignee : null,
        assignTime, arriveTime,
        slaResp, slaProc,
        '0', '0',
        ['4', '5'].includes(st) ? f[1] : null,
        ['4', '5'].includes(st) ? f[1] : null,
        ['4', '5'].includes(st) ? f[2] : null,
        null, cost, downtime, completeTime,
        ['4', '5'].includes(st) ? '维修完成，试运行正常，现场清理完毕' : null,
        null, 7, '吴芳',
        st === '5' ? 9 : null, st === '5' ? '刘洋' : null,
        st === '5' ? completeTime : null, st === '5' ? '验收合格' : null,
        st === '5' ? ri(4, 5) : null,
        '0', 'admin', reportTime, '', null, otype === '1' ? 'PM 计划触发' : null]
    )
    woAdd++
    // 日志
    const flow = [['0', '1', '派工'], ['1', '2', '接单'], ['2', '3', '开始处理'], ['3', '4', '完工'], ['4', '5', '验收']]
    const upto = { '0': 0, '1': 1, '2': 2, '3': 3, '4': 4, '5': 5 }[st]
    for (let k = 0; k < upto; k++) {
      const [from, to, action] = flow[k]
      await c.query(
        `INSERT INTO dms_work_order_log (order_id,order_no,from_status,to_status,action,operator_id,operator_name,remark,create_time) VALUES (?,?,?,?,?,?,?,?,?)`,
        [r.insertId, orderNo, from, to, action, k === 0 ? 7 : (U[assignee] || 21), k === 0 ? '吴芳' : assignee, null, reportTime]
      )
      wlogAdd++
    }
  }
  console.log(`[G] 维修工单 ${woAdd} 张 / 流转日志 ${wlogAdd} 条`)

  /* ---------------- H. 维修知识库 ---------------- */
  const faqs = [
    ['真空包装机抽真空慢、真空度上不去怎么办？', '1) 检查真空泵油位与油质，油乳化或低于下限需更换；2) 检查真空管路与接头是否漏气；3) 检查真空室密封条是否老化变形；4) 检查排气阀是否卡滞。日常建议每 500 小时更换一次真空泵油。', '包装设备'],
    ['热封刀温度不稳定，封口出现虚封、拉丝', '1) 用测温仪实测刀面温度与显示值比对；2) 加热片局部损坏需更换；3) 温控器 PID 参数漂移需重新自整定；4) 热电偶接触不良会读数跳变，紧固或更换。', '包装设备'],
    ['制冷机组高压报警频繁', '1) 冷凝器翅片积尘，用压缩空气或清水清洗；2) 冷却水塔水量不足或水温高；3) 系统内有不凝性气体需排放；4) 高压保护设定值是否被误调。', '制冷设备'],
    ['冷库温度降不下来，库温偏高', '1) 检查库门密封条与门封是否漏气；2) 蒸发器结霜过厚，需化霜；3) 制冷剂不足，查漏并补充；4) 库内货物堆放过密影响风循环。', '制冷设备'],
    ['灌装机灌装量不稳定，误差超标', '1) 灌装嘴密封圈磨损漏液，更换密封圈；2) 料缸液位波动大，保持稳定液位；3) 灌装时间参数与实际速度不匹配，重新标定；4) 单向阀有异物卡住需清洗。', '灌装设备'],
    ['分割锯条易断、切口不平整', '1) 锯条张紧度不足或过紧，按说明书调整；2) 导向轮磨损导致跑偏；3) 进料速度过快；4) 锯条超过使用寿命应定期更换。', '屠宰分割设备'],
    ['设备运行异响、振动大如何排查', '1) 用听诊器定位异响部位；2) 检查轴承是否磨损、缺油；3) 检查地脚螺栓与联轴器是否松动；4) 检查传动皮带/链条张紧度；5) 转动部件是否不平衡。', '通用'],
    ['蒸汽锅炉安全阀起跳后不回座', '1) 安全阀阀芯结垢卡滞，需拆检清洗；2) 弹簧疲劳失效需更换；3) 起跳压力设定偏低，重新整定并铅封；4) 处理后须做一次手动排放试验确认。', '动力设备'],
    ['空气压缩机排气温度高报警', '1) 检查油位与油质，缺油或油品劣化需更换；2) 冷却器堵塞需清洗；3) 环境温度过高、通风不良；4) 温控阀故障导致油不经冷却器。', '动力设备'],
    ['氨制冷机房氨气报警仪报警处置流程', '1) 立即撤离无关人员，启动强制通风；2) 佩戴正压式呼吸器两人以上进入查漏；3) 用肥皂水或检漏仪定位漏点；4) 关闭相关阀门、抽空漏氨段；5) 上报安全管理员并记录，严禁带压紧固。', '安全应急'],
    ['金属探测器误报频繁', '1) 检查周围是否有电磁干扰源；2) 产品效应（含水分/盐分高）导致误报，调整相位；3) 传送带是否清洁、有无金属碎屑残留；4) 按标准试块做灵敏度验证并记录。', '检测设备'],
    ['电子秤称量偏差超差', '1) 检查秤台是否水平、有无异物卡住；2) 用标准砝码做偏载与重复性测试；3) 超差需重新校准并粘贴校准标识；4) 属强检器具的按周期送检。', '计量器具'],
    ['设备点检发现跑冒滴漏如何处理', '1) 立即挂牌并通知当班班组长；2) 评估是否影响食品安全，必要时停机；3) 轻微渗漏可紧固或更换密封件；4) 处理后在点检表记录并跟踪 3 天。', '通用'],
    ['包装车间臭氧发生器使用注意', '1) 臭氧对人体有害，必须在无人环境下运行；2) 运行结束后通风 30 分钟以上再进人；3) 定期检查发生器放电管与气源干燥度；4) 浓度按工艺要求控制在 2~5ppm。', '包装设备'],
    ['PM 维保计划到期未执行如何处理', '1) 系统按提前 3 天提醒，逾期会标记超期；2) 生产冲突时可申请延期并说明原因；3) 补做后在系统回填实际执行时间与耗材；4) 连续两次逾期纳入设备管理考核。', '管理制度']
  ]
  let faqAdd = 0
  for (const [q, a, cat] of faqs) {
    if (await exists('SELECT 1 FROM dms_repair_faq WHERE question=?', [q])) continue
    await c.query(
      `INSERT INTO dms_repair_faq (question,answer,category_id,category_name,keywords,view_count,status,del_flag,create_by,create_time,update_by,remark)
       VALUES (?,?,?,?,?,?,?,?,?,?,?,?)`,
      [q, a, null, cat, q.replace(/[，。？、]/g, ' ').split(' ').slice(0, 4).join(','), ri(3, 68), '0', '0', 'admin', NOW, '', null]
    )
    faqAdd++
  }
  console.log(`[H] 维修知识库 ${faqAdd} 条`)

  /* ---------------- I. 设备变更日志 ---------------- */
  let logAdd = 0
  for (const e of allEq) {
    if (await exists('SELECT 1 FROM dms_equipment_log WHERE equipment_id=?', [e.equipment_id])) continue
    const rows = [
      ['0', '设备状态', '1', '0', '设备完成安装调试，投入使用', 'admin'],
      ['5', '设备型号', '', e.equipment_code.slice(2), '台账信息补录', 'admin'],
      ['3', '责任人', '', e.responsible_name, '按车间责任划分调整责任人', 'admin']
    ]
    for (const [ct, label, ov, nv, reason, op] of rows) {
      await c.query(
        `INSERT INTO dms_equipment_log (equipment_id,equipment_code,equipment_name,change_type,field_label,old_value,new_value,change_reason,operator_id,operator_name,create_time)
         VALUES (?,?,?,?,?,?,?,?,?,?,?)`,
        [e.equipment_id, e.equipment_code, e.equipment_name, ct, label, ov, nv, reason, 1, op, dt(2026, 9, ri(1, 28), ri(9, 17), 0)]
      )
      logAdd++
    }
  }
  console.log(`[I] 设备变更日志 ${logAdd} 条`)

  /* ---------------- J. 运行数据采集 ---------------- */
  const collectEq = ['SB20260004', 'SB20260005', 'SB20260016', 'SB20260017']
  let cfgAdd = 0, recAdd2 = 0
  for (const code of collectEq) {
    const e = allEq.find(x => x.equipment_code === code)
    if (!e) continue
    if (!(await exists('SELECT 1 FROM dms_data_collection_config WHERE equipment_id=?', [e.equipment_id]))) {
      const isCool = code === 'SB20260004' || code === 'SB20260017'
      await c.query(
        `INSERT INTO dms_data_collection_config (equipment_id,equipment_name,collect_mode,collect_status,collect_hours,collect_count,param1_name,param1_unit,param1_alarm_min,param1_alarm_max,param2_name,param2_unit,param2_alarm_min,param2_alarm_max,param3_name,param3_unit,param3_alarm_min,param3_alarm_max,status,access_key,del_flag,create_by,create_time,update_by,remark)
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
        [e.equipment_id, e.equipment_name, '1', '1', '1', '0',
          isCool ? '库温' : '运行温度', '℃', isCool ? -18 : 0, isCool ? 4 : 12,
          isCool ? '压力' : '电流', isCool ? 'MPa' : 'A', isCool ? 0.2 : 0, isCool ? 1.6 : 60,
          isCool ? '湿度' : '运行时长', isCool ? '%' : 'h', 40, 90,
          '0', 'EDOP-' + code, '0', 'admin', NOW, '', null]
      )
      cfgAdd++
    }
    // 近 7 天，每 4 小时一条
    for (let k = 0; k < 7 * 6; k++) {
      const t = new Date(2026, 10, 16, 20, 0)
      t.setHours(t.getHours() - k * 4)
      const ts = `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')} ${String(t.getHours()).padStart(2, '0')}:00:00`
      if (await exists('SELECT 1 FROM dms_data_record WHERE equipment_id=? AND collect_time=?', [e.equipment_id, ts])) continue
      const isCool = code === 'SB20260004' || code === 'SB20260017'
      await c.query(
        `INSERT INTO dms_data_record (equipment_id,equipment_code,equipment_name,run_status,run_hours,product_count,param1_value,param2_value,param3_value,collect_time,collect_mode,create_by,create_time,update_by,remark)
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
        [e.equipment_id, code, e.equipment_name, rnd() < 0.05 ? '2' : '0', d2(ri(1200, 4200)), d2(ri(0, 800)),
          isCool ? d2(-2 + rnd() * 5) : d2(4 + rnd() * 5), isCool ? d2(0.4 + rnd() * 0.7) : d2(18 + rnd() * 20), d2(55 + rnd() * 30),
          ts, '1', 'admin', ts, '', null]
      )
      recAdd2++
    }
  }
  console.log(`[J] 采集配置 ${cfgAdd} 条 / 采集记录 ${recAdd2} 条`)

  /* ---------------- SLA 超时标记（按实际时间回算） ---------------- */
  const [sla] = await c.query(`UPDATE dms_work_order SET sla_timeout_status = CASE
      WHEN (assign_time IS NOT NULL AND assign_time > sla_response_deadline) THEN '1'
      WHEN (complete_time IS NOT NULL AND complete_time > sla_process_deadline) THEN '1'
      WHEN (assign_time IS NULL AND sla_response_deadline < '2026-11-17 09:00:00') THEN '1'
      ELSE '0' END`)
  console.log(`[K] SLA 超时标记 ${sla.affectedRows} 张`)

  // 制造 3 张响应超时样本（紧急件响应时限 20 分钟），便于看板演示超时率
  const [breach] = await c.query("SELECT order_id FROM dms_work_order WHERE order_type='0' AND order_status='5' ORDER BY order_id LIMIT 3")
  for (const b of breach) {
    await c.query(
      "UPDATE dms_work_order SET sla_response_deadline=DATE_ADD(report_time, INTERVAL 20 MINUTE), sla_timeout_status='1' WHERE order_id=? AND assign_time IS NOT NULL AND assign_time > DATE_ADD(report_time, INTERVAL 20 MINUTE)",
      [b.order_id]
    )
  }

  /* ---------------- 汇总 ---------------- */
  const [[sum]] = await c.query(`SELECT
    (SELECT COUNT(*) FROM dms_equipment_category) cat,
    (SELECT COUNT(*) FROM dms_equipment) eq,
    (SELECT COUNT(*) FROM dms_pm_plan) pm,
    (SELECT COUNT(*) FROM dms_inspection_route) rt,
    (SELECT COUNT(*) FROM dms_inspection_task) it,
    (SELECT COUNT(*) FROM dms_spare_part) pt,
    (SELECT COUNT(*) FROM dms_spare_part_stock) pst,
    (SELECT COUNT(*) FROM dms_spare_part_record) pr,
    (SELECT COUNT(*) FROM dms_work_order) wo,
    (SELECT COUNT(*) FROM dms_work_order_log) wl,
    (SELECT COUNT(*) FROM dms_repair_faq) fq,
    (SELECT COUNT(*) FROM dms_equipment_log) el,
    (SELECT COUNT(*) FROM dms_data_collection_config) cc,
    (SELECT COUNT(*) FROM dms_data_record) dr`)
  console.log('\n[汇总]', sum)
  await c.end()
})()
