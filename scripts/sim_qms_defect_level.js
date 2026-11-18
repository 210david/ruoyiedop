/**
 * 缺陷等级补全
 * 1) 字典 qms_defect_level 增加「无缺陷」档（value=0），使合格项在等级列也有可读标签
 * 2) qms_insp_item：合格项（ND）补 defect_level='0'
 * 3) qms_insp_task：检验结果合格的报告，「最高缺陷等级」补 '0'
 * 4) qms_ncr：英文等级（critical/major/minor）纠正为字典值 1/2/3/4，
 *    原值无法被前端 qms_defect_level 字典命中，列表「缺陷等级」一直显示 '-'
 */
const mysql = require('mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true,
  });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  // ---------- 1) 字典：无缺陷 ----------
  const [d] = await q(`SELECT dict_code FROM sys_dict_data WHERE dict_type='qms_defect_level' AND dict_value='0'`);
  if (!d) {
    await exec(`INSERT INTO sys_dict_data(dict_sort,dict_label,dict_value,dict_type,css_class,list_class,
        is_default,status,create_by,create_time,remark)
      VALUES(0,'无缺陷','0','qms_defect_level','','success','N','0','admin',NOW(),'检验合格、未发现缺陷')`);
    console.log('字典新增：qms_defect_level / 0 / 无缺陷');
  } else {
    console.log('字典已存在：qms_defect_level / 0');
  }

  // ---------- 2) 明细：合格项补等级 ----------
  const r2 = await exec(`UPDATE qms_insp_item SET defect_level='0'
    WHERE item_result='1' AND (defect_level IS NULL OR defect_level='')`);
  console.log(`检验明细合格项补等级 ${r2.affectedRows} 行`);

  // ---------- 3) 任务：合格报告的最高缺陷等级 ----------
  const r3 = await exec(`UPDATE qms_insp_task SET defect_level='0'
    WHERE inspect_result='1' AND (defect_level IS NULL OR defect_level='')`);
  console.log(`检验任务最高缺陷等级 ${r3.affectedRows} 条`);

  // ---------- 4) NCR 英文等级 → 字典值 ----------
  const map = [
    ['NCR20261027001', '2', '真空漏气 2 袋，密封失效但未流入市场，判严重'],
    ['NCR20261027002', '3', '瓶盖密封垫偏移，抽检发现、复检合格，判一般'],
    ['NCR20261027003', '2', '酮体表面淤血面积超标，影响外观等级，判严重'],
    ['NCR20261027004', '4', '客户反馈口感偏咸（3 袋），未涉及安全指标，判轻微'],
    ['NCR20261027005', '1', '金属探测报警并检出金属屑，涉食品安全，判致命'],
  ];
  for (const [no, lv, why] of map) {
    const r = await exec(`UPDATE qms_ncr SET defect_level=?, defect_desc=IF(defect_desc IS NULL OR defect_desc='',?,defect_desc) WHERE ncr_no=?`, [lv, why, no]);
    if (r.affectedRows) console.log(`  ${no} → ${lv}`);
  }

  const [chk1] = await q(`SELECT IFNULL(defect_level,'NULL') lv,COUNT(*) c FROM qms_insp_item GROUP BY defect_level`);
  const [chk2] = await q(`SELECT IFNULL(defect_level,'NULL') lv,COUNT(*) c FROM qms_insp_task GROUP BY defect_level`);
  const [chk3] = await q(`SELECT defect_level,COUNT(*) c FROM qms_ncr GROUP BY defect_level ORDER BY defect_level`);
  console.log('明细等级分布', JSON.stringify(chk1));
  console.log('任务等级分布', JSON.stringify(chk2));
  console.log('NCR 等级分布', JSON.stringify(chk3));

  await conn.end();
})();
