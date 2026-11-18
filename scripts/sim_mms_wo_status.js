/**
 * 工单状态 / 优先级口径修正（mms_work_order）
 * 现状：status 存 'finished'/'processing'/'pending'/'released'，priority 存 'high'/'normal'/'low'/'5'
 *      与字典 mms_workorder_status(0~6)、mms_priority(0高/1中/2低) 不匹配
 *      → 列表「状态」「优先级」列显示 '—'，顶部状态页签除「全部」外全为 0
 * 映射：finished→3 已完工；processing→2 执行中；released→1 已下达；pending→0 草稿
 *      high→0 高；normal→1 中；low→2 低；'5'(导入异常值)→1 中
 */
const mysql = require('mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true,
  });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  console.log('== 处理前：', JSON.stringify(await q(`SELECT status,COUNT(*) c FROM mms_work_order GROUP BY status`)));

  const stMap = [
    ['finished', '3', '已完工'],
    ['processing', '2', '执行中'],
    ['released', '1', '已下达'],
    ['pending', '0', '草稿'],
  ];
  for (const [from, to, label] of stMap) {
    const r = await exec(`UPDATE mms_work_order SET status=? WHERE status=?`, [to, from]);
    if (r.affectedRows) console.log(`  状态 ${from} → ${to} ${label}：${r.affectedRows} 单`);
  }

  const prMap = [
    ['high', '0', '高'],
    ['normal', '1', '中'],
    ['low', '2', '低'],
    ['5', '1', '中'],
  ];
  for (const [from, to, label] of prMap) {
    const r = await exec(`UPDATE mms_work_order SET priority=? WHERE priority=?`, [to, from]);
    if (r.affectedRows) console.log(`  优先级 ${from} → ${to} ${label}：${r.affectedRows} 单`);
  }

  console.log('== 处理后状态：', JSON.stringify(await q(`SELECT status,COUNT(*) c FROM mms_work_order GROUP BY status ORDER BY status`)));
  console.log('== 处理后优先级：', JSON.stringify(await q(`SELECT priority,COUNT(*) c FROM mms_work_order GROUP BY priority ORDER BY priority`)));
  const bad = await q(`SELECT COUNT(*) c FROM mms_work_order WHERE status NOT IN ('0','1','2','3','4','5','6') OR priority NOT IN ('0','1','2')`);
  console.log('== 仍不合规：', JSON.stringify(bad));

  await conn.end();
})();
