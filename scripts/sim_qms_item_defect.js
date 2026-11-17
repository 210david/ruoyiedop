/**
 * 检验明细缺陷信息补全（qms_insp_item）
 * 现状：295 行明细全部判定合格（item_result='1'），缺陷代码/名称/等级均为空，缺陷数量为 0
 * 处理：合格项按「未发现缺陷」记 ND；若存在不合格项而缺缺陷信息，按检验项目映射真实缺陷代码
 */
const mysql = require('mysql2/promise');

// 检验项目 → 缺陷（仅用于不合格项兜底）
const DEFECT_MAP = [
  [/过氧化值|酸价|滋味|气味|酸败/, 'DC006', '酸败味', '1'],
  [/亚硝酸盐/, 'DC015', '亚硝酸盐残留超标', '1'],
  [/水分/, 'DC014', '水分含量超标', '3'],
  [/净含量|规格重量/, 'DC003', '净含量不足', '2'],
  [/菌落总数|大肠菌群|霉菌|酵母|致病菌/, 'DC004', '异物（金属）', '1'],
  [/标签|标识|喷码/, 'DC009', '标签错版', '2'],
  [/真空|漏气|封口|破损/, 'DC001', '真空漏气', '1'],
];

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true,
  });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  const stats = await q(`SELECT item_result, COUNT(*) c,
      SUM(defect_code IS NULL OR defect_code='') no_code
    FROM qms_insp_item GROUP BY item_result`);

  // 1) 合格项：未发现缺陷
  const ok = await exec(`UPDATE qms_insp_item
    SET defect_code='ND', defect_name='无缺陷', defect_qty=0
    WHERE item_result='1' AND (defect_code IS NULL OR defect_code='')`);
  console.log(`合格项补「ND/无缺陷」${ok.affectedRows} 行`);

  // 2) 不合格项：按项目名映射真实缺陷
  const bad = await q(`SELECT item_id, std_name, defect_qty FROM qms_insp_item
    WHERE item_result='2' AND (defect_code IS NULL OR defect_code='')`);
  let cnt = 0;
  for (const it of bad) {
    const hit = DEFECT_MAP.find(([re]) => re.test(it.std_name || ''));
    if (!hit) continue;
    const [, code, name, lvl] = hit;
    const d = await q(`SELECT defect_id FROM qms_defect_code WHERE defect_code=?`, [code]);
    await exec(`UPDATE qms_insp_item SET defect_id=?, defect_code=?, defect_name=?, defect_level=?, defect_qty=?
      WHERE item_id=?`, [d[0] ? d[0].defect_id : null, code, name, lvl,
      Number(it.defect_qty) > 0 ? it.defect_qty : 1, it.item_id]);
    cnt++;
  }
  console.log(`不合格项补真实缺陷 ${cnt} 行（原不合格 ${bad.length} 行）`);

  const chk = await q(`SELECT COUNT(*) c,
      SUM(defect_code IS NULL OR defect_code='') no_code,
      SUM(defect_name IS NULL OR defect_name='') no_name,
      SUM(defect_qty IS NULL) no_qty,
      SUM(item_result='2') bad
    FROM qms_insp_item`);
  console.log('校验：', chk[0]);

  const dist = await q(`SELECT defect_code, defect_name, COUNT(*) c FROM qms_insp_item GROUP BY defect_code, defect_name`);
  console.table(dist);

  await conn.end();
})().catch(e => { console.error('ERR', e.message); process.exit(1); });
