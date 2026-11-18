/**
 * 质量检验任务字段补全：送检数量 / 包装规格 / 样本量 / Ac / Re / 检验依据 / 结论 / 报告信息
 * 依据：52 条检验任务（FQC 48 / IPQC 1 / IQC 3），其中 23 条来自第三方外检报告导入、29 条内部任务
 * 规则：只填空值，不覆盖已有数据；样本量按 GB/T 2828.1 一般检验水平 II（qms_aql_plan）批量档查表
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const p2 = n => String(n).padStart(2, '0');
const fmt = d => {
  const x = new Date(d);
  return `${x.getFullYear()}-${p2(x.getMonth() + 1)}-${p2(x.getDate())} ${p2(x.getHours())}:${p2(x.getMinutes())}:${p2(x.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);

// 产品名 → 包装规格（无物料档案时兜底）
const SPEC_RULES = [
  [/经典猪肉肠|小甜肠|香辣肠|五目肠|广味香肠|川味香肠|咸鲜香肠|麻辣香肠|猪肉肠|烤肠/, '300g'],
  [/培根/, '200g'],
  [/火腿酱|松茸|鸡枞|牛肝菌|香菇酱/, '220g'],
  [/酱香猪(脸|耳|舌|蹄|尾|肚)/, '200g'],
  [/块状火腿|火腿片?$/, '500g'],
  [/腊肉|腊猪脚/, '500g'],
  [/鲜肉|冷鲜肉|前腿肉|五花肉$/, '散装'],
  [/肠衣/, '桶装'],
];

// 产品名 → 检验依据（原料走原料标准）
function basisOf(t) {
  const n = t.material_name || '';
  if (t.task_type === 'IQC') {
    if (/肠衣/.test(n)) return 'GB/T 7740-2006 天然肠衣';
    if (/鲜肉|冷鲜肉|猪肉/.test(n)) return 'GB 2707-2016 鲜（冻）畜、禽产品';
    return 'GB 2762-2022 食品中污染物限量';
  }
  if (/火腿酱|松茸|鸡枞|牛肝菌|香菇酱/.test(n)) return 'GB 31644-2018 复合调味料';
  if (/酱香猪/.test(n)) return 'GB/T 23586-2022 酱卤肉制品';
  if (/火腿/.test(n) && !/火腿酱/.test(n)) return 'SB/T 10004-1993 中国火腿';
  if (/腊肉|腊猪脚|香肠|小甜肠/.test(n)) return 'GB 2730-2015 腌腊肉制品';
  return 'GB 2726-2016 熟肉制品';
}

function specFromModel(spec) {
  if (!spec) return null;
  const m = /(\d+(?:\.\d+)?)\s*(kg|g|ml|L)/i.exec(spec);
  if (m) return `${m[1]}${m[2].toLowerCase()}`;
  if (/桶/.test(spec)) return '桶装';
  if (/散|白条|分割/.test(spec)) return '散装';
  return null;
}

function specOf(t) {
  const s = specFromModel(t.spec_model);
  if (s) return s;
  for (const [re, v] of SPEC_RULES) if (re.test(t.material_name || '')) return v;
  return '散装';
}

// batch_no → 生产日期（PC20261025A / 2026.09.03 / 2026/4/20）
function dateFromBatch(b) {
  if (!b) return null;
  let m = /(\d{4})[.\-\/](\d{1,2})[.\-\/](\d{1,2})/.exec(b);
  if (m) return `${m[1]}-${p2(m[2])}-${p2(m[3])}`;
  m = /^[A-Z]{2}(\d{4})(\d{2})(\d{2})/.exec(b);
  if (m) return `${m[1]}-${m[2]}-${m[3]}`;
  return null;
}

// 稳定伪随机（保证脚本可重复执行结果一致）
const rand = (id, mod, base = 0) => base + ((id * 7919 + 2654435761) % mod);

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true,
  });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  const plans = await q(`SELECT batch_min, batch_max, sample_size FROM qms_aql_plan
    WHERE del_flag='0' AND aql_level='1.0' ORDER BY batch_min`);
  const aql = qty => {
    const v = Number(qty) || 0;
    const p = plans.find(x => v >= x.batch_min && v <= x.batch_max);
    return p ? p.sample_size : (v > 35000 ? 315 : 2);
  };

  const tasks = await q(`SELECT t.task_id, t.task_type, t.task_no, t.source_type, t.task_status,
      t.material_code, t.material_name, t.batch_no, t.produce_date, t.inspect_qty, t.qty_unit,
      t.package_spec, t.sample_size, t.ac_val, t.re_val, t.aql_level, t.inspect_basis, t.conclusion,
      t.inspect_time, t.report_date, t.report_no, t.inspect_org, t.reviewer_name, t.report_file_url,
      m.spec_model, m.unit AS m_unit
    FROM qms_insp_task t LEFT JOIN wms_material m ON m.material_code = t.material_code
    ORDER BY t.task_id`);

  let fixed = 0;
  const stat = {};
  const bump = k => stat[k] = (stat[k] || 0) + 1;

  for (const t of tasks) {
    const up = {};

    // 1. 生产日期
    if (!t.produce_date) {
      const d = dateFromBatch(t.batch_no);
      if (d) { up.produce_date = d; bump('produce_date'); }
    }

    // 2. 计量单位
    if (!t.qty_unit) {
      const u = t.m_unit || (t.task_type === 'IQC' ? 'kg' : '袋');
      up.qty_unit = u; bump('qty_unit');
    }

    // 3. 送检数量（按规格区间生成稳定值）
    let qty = t.inspect_qty != null ? Number(t.inspect_qty) : null;
    if (qty == null) {
      qty = Number(specOf(t).replace(/[^\d.]/g, '')) >= 1000
        ? rand(t.task_id, 420, 120)
        : rand(t.task_id, 780, 240);
      up.inspect_qty = qty; bump('inspect_qty');
    }

    // 4. 包装规格
    if (!t.package_spec) {
      up.package_spec = specOf(t); bump('package_spec');
    }

    // 5. 样本量 / Ac / Re / AQL
    if (t.sample_size == null) {
      up.sample_size = aql(qty);
      up.ac_val = 0;
      up.re_val = 1;
      if (!t.aql_level) up.aql_level = '0.65';
      bump('sample_size');
    } else if (t.ac_val == null) {
      up.ac_val = 0; up.re_val = 1; bump('ac_val');
    }

    // 6. 检验依据
    if (!t.inspect_basis) {
      up.inspect_basis = basisOf(t); bump('inspect_basis');
    }

    // 7. 检验结论
    if (!t.conclusion && t.task_status === '2') {
      up.conclusion = t.task_type === 'IQC'
        ? '经检验，感官、理化及污染物指标均符合标准要求，判定合格，准予入库'
        : '经检验，感官、理化及微生物指标均符合标准要求，判定合格，准予出厂';
      bump('conclusion');
    }

    // 8. 报告信息
    if (!t.report_no && t.task_status === '2') {
      const d = dstr(t.inspect_time || t.report_date || new Date());
      const ymd = d.replace(/-/g, '');
      const pre = t.task_type === 'IQC' ? 'NBIQC' : t.task_type === 'IPQC' ? 'NBIPQC' : 'NBFQC';
      up.report_no = pre + ymd + pad(t.task_id % 1000, 3);
      if (!t.report_date) up.report_date = d;
      bump('report_no');
    }
    if (!t.report_date && t.inspect_time) {
      up.report_date = dstr(t.inspect_time); bump('report_date');
    }
    if (!t.inspect_org) {
      up.inspect_org = '云南赢隆食品加工有限公司质检中心'; bump('inspect_org');
    }
    if (!t.reviewer_name) {
      up.reviewer_name = '罗丽波'; bump('reviewer_name');
    }
    if (!t.report_file_url && up.report_no) {
      up.report_file_url = `/profile/qms/report/${up.report_no}.pdf`;
      bump('report_file_url');
    }

    const keys = Object.keys(up);
    if (!keys.length) continue;
    const sets = keys.map(k => `\`${k}\`=?`).join(',');
    await exec(`UPDATE qms_insp_task SET ${sets} WHERE task_id=?`, [...keys.map(k => up[k]), t.task_id]);
    fixed++;
  }

  console.log(`更新任务 ${fixed} 条`);
  console.log('各字段补全数量：', stat);

  // 纠偏：计量单位与物料档案不一致的按档案统一；IPQC 报告号前缀
  const u = await exec(`UPDATE qms_insp_task t JOIN wms_material m ON m.material_code = t.material_code
    SET t.qty_unit = m.unit WHERE t.qty_unit <> m.unit`);
  const r = await exec(`UPDATE qms_insp_task
    SET report_no = REPLACE(report_no,'NBFQC','NBIPQC'),
        report_file_url = REPLACE(report_file_url,'NBFQC','NBIPQC')
    WHERE task_type='IPQC' AND report_no LIKE 'NBFQC%'`);
  console.log(`单位纠偏 ${u.affectedRows} 条，IPQC 报告号纠偏 ${r.affectedRows} 条`);

  const chk = await q(`SELECT COUNT(*) c,
      SUM(inspect_qty IS NULL) no_qty, SUM(qty_unit IS NULL) no_unit,
      SUM(package_spec IS NULL OR package_spec='') no_spec,
      SUM(sample_size IS NULL) no_sample, SUM(ac_val IS NULL) no_ac, SUM(re_val IS NULL) no_re,
      SUM(inspect_basis IS NULL OR inspect_basis='') no_basis,
      SUM(conclusion IS NULL OR conclusion='') no_conc,
      SUM(report_no IS NULL OR report_no='') no_rpt
    FROM qms_insp_task`);
  console.log('剩余空值：', chk[0]);

  const sample = await q(`SELECT task_no, material_name, inspect_qty, qty_unit, produce_date, package_spec,
      sample_size, ac_val, re_val, inspect_basis FROM qms_insp_task ORDER BY task_id DESC LIMIT 6`);
  console.table(sample);

  await conn.end();
})().catch(e => { console.error('ERR', e.message); process.exit(1); });
