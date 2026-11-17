/**
 * 产品设计(PDM)：产品分类 / 产品档案 / 设计BOM / 设计方案与配方 / 图纸文件 / 版本 / 设计变更
 * 依据已有 21 个 BOM 产品与 21 条工艺路线
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);
const ri = (a, b) => Math.floor(Math.random() * (b - a + 1)) + a;

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  for (const t of ['pdm_product_category', 'pdm_product', 'pdm_design_bom', 'pdm_design_bom_item', 'pdm_scheme',
    'pdm_scheme_formula', 'pdm_design_file', 'pdm_doc_version', 'pdm_change_order', 'pdm_change_order_item', 'pdm_change_audit_log']) {
    await exec(`DELETE FROM \`${t}\``);
  }

  // ---------- 1. 产品分类 ----------
  const roots = [
    ['PC01', '腌腊制品'], ['PC02', '香肠制品'], ['PC03', '分割鲜肉'], ['PC04', '酱卤制品'], ['PC05', '调味酱料']
  ];
  const catMap = new Map();
  for (const [i, c] of roots.entries()) {
    const ins = await exec(`INSERT INTO pdm_product_category(parent_id,ancestors,category_code,category_name,order_num,status,del_flag,create_by,create_time,remark)
      VALUES(0,'0',?,?,?, '0','0','system',NOW(),'一级分类')`, [c[0], c[1], i + 1]);
    catMap.set(c[1], ins.insertId);
  }
  const subs = [['PC0201', '低温香肠', '香肠制品'], ['PC0202', '腊肠', '香肠制品'], ['PC0101', '腊肉', '腌腊制品'], ['PC0102', '火腿', '腌腊制品'], ['PC0301', '冷鲜分割', '分割鲜肉']];
  for (const [i, s] of subs.entries()) {
    const parentId = catMap.get(s[2]);
    const p = await q(`SELECT category_code FROM pdm_product_category WHERE category_id=?`, [parentId]);
    const ins = await exec(`INSERT INTO pdm_product_category(parent_id,ancestors,category_code,category_name,order_num,status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?, '0','0','system',NOW(),'二级分类')`, [parentId, '0,' + parentId, s[0], s[1], i + 1]);
    catMap.set(s[1], ins.insertId);
  }
  console.log(`产品分类 ${catMap.size} 个`);

  // ---------- 2. 产品档案 ----------
  const boms = await q(`SELECT b.*, m.material_id, m.spec_model, m.unit FROM mms_bom b LEFT JOIN wms_material m ON m.material_code=b.product_code ORDER BY b.bom_id`);
  const catOf = name => /肠/.test(name) ? '低温香肠' : (/火腿/.test(name) ? '火腿' : (/腊|培根/.test(name) ? '腊肉' : (/酱/.test(name) ? '调味酱料' : '冷鲜分割')));
  let pCnt = 0;
  const prodMap = new Map();
  for (const [i, b] of boms.entries()) {
    const catName = catOf(b.product_name);
    const t = new Date(2026, 2, 1 + i, 9, 0, 0);
    const ins = await exec(`INSERT INTO pdm_product(product_code,material_id,material_name,spec_model,unit,category_id,category_name,product_status,designer_id,designer_name,final_date,del_flag,dept_id,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?, '1',11,'罗丽波',?, '0',104,'system',?,?)`,
      [b.product_code, b.material_id, b.product_name, b.spec_model || '', b.unit || '袋',
        catMap.get(catName) || null, catName, dstr(new Date(t.getTime() + 20 * 86400000)), fmt(t), '由 BOM 反推建立产品档案']);
    prodMap.set(b.product_code, ins.insertId);
    pCnt++;
  }
  // 工单中出现但无 BOM 的成品，也建立产品档案
  const extra = await q(`SELECT DISTINCT wo.product_code, wo.product_name, wo.spec_model, wo.unit, wo.product_id
    FROM mms_work_order wo WHERE wo.del_flag='0'`);
  for (const [i, e] of extra.entries()) {
    if (prodMap.has(e.product_code)) continue;
    const catName = catOf(e.product_name);
    const t = new Date(2026, 4, 1 + (i % 28), 9, 0, 0);
    const ins = await exec(`INSERT INTO pdm_product(product_code,material_id,material_name,spec_model,unit,category_id,category_name,product_status,designer_id,designer_name,final_date,del_flag,dept_id,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?, '1',11,'罗丽波',?, '0',104,'system',?,'工单产品补建档案')`,
      [e.product_code, e.product_id, e.product_name, e.spec_model || '', e.unit || '袋',
        catMap.get(catName) || null, catName, dstr(new Date(t.getTime() + 10 * 86400000)), fmt(t)]);
    prodMap.set(e.product_code, ins.insertId);
    pCnt++;
  }
  console.log(`产品档案 ${pCnt} 个`);

  // ---------- 3. 设计 BOM ----------
  const details = await q(`SELECT * FROM mms_bom_detail ORDER BY bom_id, seq`);
  const detByBom = new Map();
  details.forEach(d => { if (!detByBom.has(d.bom_id)) detByBom.set(d.bom_id, []); detByBom.get(d.bom_id).push(d); });
  let dbCnt = 0, dbiCnt = 0;
  for (const b of boms) {
    const pid = prodMap.get(b.product_code);
    const t = new Date(2026, 2, 2, 9, 0, 0);
    const ins = await exec(`INSERT INTO pdm_design_bom(bom_no,bom_name,product_id,product_code,product_name,base_qty,base_unit,bom_status,del_flag,dept_id,create_by,create_time,remark)
      VALUES(?,?,?,?,?,100,?, '1','0',104,'system',?,'基准100kg成品配方')`,
      [b.bom_no.replace('BOM', 'DBOM'), b.product_name + '设计BOM', pid, b.product_code, b.product_name, b.unit || 'kg', fmt(t)]);
    dbCnt++;
    for (const [k, d] of (detByBom.get(b.bom_id) || []).entries()) {
      await exec(`INSERT INTO pdm_design_bom_item(bom_id,parent_id,item_level,material_id,material_code,material_name,spec_model,unit,usage_qty,loss_rate,order_num,remark)
        VALUES(?,0,1,?,?,?,?,?,?,?,?,?)`,
        [ins.insertId, d.material_id, d.material_code, d.material_name, d.spec_model, d.unit, d.usage_qty, d.loss_rate, k + 1, d.remark]);
      dbiCnt++;
    }
  }
  console.log(`设计BOM ${dbCnt} 个 / 明细 ${dbiCnt} 行`);

  // ---------- 4. 设计方案 + 配方 ----------
  const schemes = [
    ['低温香肠降盐配方优化方案', 'WL20260003', '在保持风味前提下将食盐添加量由 2.8% 降至 2.3%，提升健康属性', '食盐 2.3%、亚硝酸盐 ≤30mg/kg、水分 ≤60%'],
    ['黑松露小甜肠风味提升方案', 'WL20260276', '调整黑松露添加比例，优化斩拌工艺温度', '黑松露 1.2%、斩拌温度 ≤8℃'],
    ['五花培根低脂改良方案', 'WL20260205', '降低肥肉比例，调整滚揉时间改善口感', '肥瘦比 3:7、滚揉 8h'],
    ['鸡枞火腿酱增香方案', 'WL20260007', '增加鸡枞菌预处理工艺，提升鲜味', '鸡枞 15%、熬制 90min'],
    ['腊猪脚减盐降亚硝方案', 'WL20260240', '缩短腌制时间并优化亚硝酸盐残留', '腌制 72h、亚硝 ≤20mg/kg']
  ];
  let sCnt = 0, fCnt = 0;
  for (const [i, s] of schemes.entries()) {
    const code = (await q(`SELECT product_code FROM pdm_product WHERE product_code=?`, [s[1]]))[0];
    if (!code) continue;
    const pid = prodMap.get(s[1]);
    const t = new Date(2026, 3, 10 + i * 4, 10, 0, 0);
    const ins = await exec(`INSERT INTO pdm_scheme(scheme_no,scheme_name,product_id,product_code,product_name,design_goal,main_params,scheme_status,final_time,final_by,dept_id,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,104, '0','system',?,?)`,
      ['FA' + dstr(t).replace(/-/g, '') + pad(i + 1), s[0], pid, s[1], code.material_name || '', s[2], s[3],
        i < 3 ? '2' : '1', i < 3 ? fmt(new Date(t.getTime() + 10 * 86400000)) : null, i < 3 ? '罗丽波' : null, fmt(t), '研发试制方案']);
    sCnt++;
    const lines = (detByBom.get(boms.find(b => b.product_code === s[1])?.bom_id) || []).slice(0, 4);
    for (const [k, l] of lines.entries()) {
      await exec(`INSERT INTO pdm_scheme_formula(scheme_id,material_id,item_name,ratio,unit,tolerance,order_num,remark)
        VALUES(?,?,?,?,?,?,?,?)`,
        [ins.insertId, l.material_id, l.material_name, (Number(l.usage_qty) / 100).toFixed(4), l.unit, '±2%', k + 1, '']);
      fCnt++;
    }
    // 版本
    await exec(`INSERT INTO pdm_doc_version(biz_type,biz_id,version_no,version_desc,effective_flag,effective_time,effect_basis,is_final_set,create_by,create_time)
      VALUES('scheme',?,'V1.0','方案初版发布',?,?, 'audit','1','system',?)`,
      [ins.insertId, i < 3 ? '1' : '0', i < 3 ? fmt(new Date(t.getTime() + 10 * 86400000)) : null, fmt(t)]);
  }
  console.log(`设计方案 ${sCnt} 个 / 配方 ${fCnt} 行`);

  // ---------- 5. 图纸/技术文件 ----------
  const files = [
    ['真空包装袋版面图', '1', 'WL20260003'], ['香肠产品标签稿', '1', 'WL20260003'],
    ['灌装机工艺参数表', '2', 'WL20260264'], ['培根切片规格图', '1', 'WL20260205'],
    ['礼盒包装结构图', '1', 'WL20260006'], ['火腿酱玻璃瓶标签', '1', 'WL20260007'],
    ['产品企业标准 Q/YLSP 001-2026', '3', 'WL20260003'], ['HACCP 计划书（2026 版）', '3', 'WL20260003']
  ];
  let f2Cnt = 0;
  for (const [i, f] of files.entries()) {
    const t = new Date(2026, 3, 5 + i * 3, 14, 0, 0);
    if (!prodMap.get(f[2])) continue;
    await exec(`INSERT INTO pdm_design_file(file_no,file_name,file_category,product_id,product_code,product_name,file_url,file_status,del_flag,dept_id,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?, '1','0',104,'system',?,'受控技术文件')`,
      ['TF' + pad(i + 1), f[0], f[1], prodMap.get(f[2]) || null, f[2], (boms.find(b => b.product_code === f[2]) || {}).product_name || '',
        '/profile/pdm/' + encodeURIComponent(f[0]) + '.pdf', fmt(t)]);
    f2Cnt++;
  }
  console.log(`技术文件 ${f2Cnt} 份`);

  // ---------- 6. 设计变更 ----------
  const changes = [
    ['香肠包装规格由 500g 调整为 400g', 'WL20260003', '2', '响应客户对单包装规格需求', '涉及包装材料、标签、BOM 与工艺卡'],
    ['培根切片厚度由 2.5mm 调整为 2.0mm', 'WL20260205', '3', '提升产品口感一致性', '涉及切片设备参数与作业指导书'],
    ['鸡枞火腿酱瓶型更换为 200g 玻璃瓶', 'WL20260007', '2', '降低包装成本', '涉及包材 BOM、标签与灌装参数'],
    ['腊猪脚腌制时间由 96h 缩短至 72h', 'WL20260240', '1', '提升周转效率', '涉及工艺路线与 CCP 参数']
  ];
  let cCnt = 0, ciCnt = 0;
  for (const [i, c] of changes.entries()) {
    const t = new Date(2026, 5, 8 + i * 7, 9, 30, 0);
    const pid = prodMap.get(c[1]);
    const ins = await exec(`INSERT INTO pdm_change_order(change_no,change_name,product_id,product_code,product_name,change_level,urgent_level,change_content,change_reason,effect_analysis,change_status,submit_time,audit_by,audit_time,audit_opinion,close_time,close_by,dept_id,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,104, '0','system',?,?)`,
      ['ECR' + dstr(t).replace(/-/g, '') + pad(i + 1), c[0], pid, c[1], (boms.find(b => b.product_code === c[1]) || {}).product_name || '',
        c[2], '1', c[3], c[4], '经评估对成本影响 ' + ri(2, 8) + '%，需同步更新 BOM 与工艺文件',
        i < 3 ? '3' : '1', fmt(t), i < 3 ? '技术总监' : null, i < 3 ? fmt(new Date(t.getTime() + 3 * 86400000)) : null,
        i < 3 ? '同意变更，按计划执行' : null, i < 3 ? fmt(new Date(t.getTime() + 15 * 86400000)) : null,
        i < 3 ? '技术总监' : null, fmt(t), '设计变更申请']);
    cCnt++;
    for (const doc of [['1', '设计BOM'], ['2', '工艺路线'], ['3', '作业指导书']]) {
      await exec(`INSERT INTO pdm_change_order_item(change_id,doc_type,biz_id,biz_no,biz_name,current_version,new_version,item_status,effect_time)
        VALUES(?,?,?,?,?,?,?,?,?)`,
        [ins.insertId, doc[0], 1, 'DOC-' + pad(i + 1), doc[1], 'V1.0', 'V1.1', i < 3 ? '2' : '0', fmt(new Date(t.getTime() + 15 * 86400000))]);
      ciCnt++;
    }
    await exec(`INSERT INTO pdm_change_audit_log(change_id,audit_action,audit_by,audit_time,audit_remark)
      VALUES(?,'1','罗丽波',?,'提交设计变更申请')`, [ins.insertId, fmt(t)]);
    if (i < 3) {
      await exec(`INSERT INTO pdm_change_audit_log(change_id,audit_action,audit_by,audit_time,audit_remark)
        VALUES(?,'3','丰孟',?,'技术评审通过，同意变更')`, [ins.insertId, fmt(new Date(t.getTime() + 3 * 86400000))]);
    }
  }
  console.log(`设计变更 ${cCnt} 个 / 变更对象 ${ciCnt} 行`);

  // ---------- 7. MBOM 发布 ----------
  const rs = await q(`SELECT route_id, route_no, product_code, product_name, version FROM mms_route`);
  for (const [i, r] of rs.entries()) {
    const srcBom = boms.find(b => b.product_code === r.product_code) || {};
    const ebom = await q(`SELECT bom_id FROM pdm_design_bom WHERE product_code=? LIMIT 1`, [r.product_code]);
    await exec(`INSERT INTO pdm_mbom_release(bom_id,bom_no,product_id,product_code,product_name,ebom_version,mms_bom_id,mms_bom_no,mms_version,item_count,release_status,release_by,release_time,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?, '1',?,?, 'system',NOW(),'设计BOM转制造BOM发布')`,
      [ebom[0] ? ebom[0].bom_id : 1, 'MBOM' + pad(i + 1), prodMap.get(r.product_code) || 1, r.product_code, r.product_name,
        'V1.0', srcBom.bom_id || null, srcBom.bom_no || '', r.version || 'V1.0',
        (detByBom.get(srcBom.bom_id) || []).length, '罗丽波', fmt(new Date(2026, 6, 1 + i, 10, 0, 0))]);
  }
  console.log(`MBOM 发布 ${rs.length} 条`);

  console.log(await q(`SELECT (SELECT COUNT(*) FROM pdm_product_category) 分类,(SELECT COUNT(*) FROM pdm_product) 产品,
    (SELECT COUNT(*) FROM pdm_design_bom) 设计BOM,(SELECT COUNT(*) FROM pdm_scheme) 方案,
    (SELECT COUNT(*) FROM pdm_design_file) 文件,(SELECT COUNT(*) FROM pdm_change_order) 变更,
    (SELECT COUNT(*) FROM pdm_mbom_release) MBOM`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
