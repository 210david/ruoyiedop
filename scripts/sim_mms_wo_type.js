/**
 * 工单类型补全（mms_work_order.order_type）
 * 现状：order_type 存的是英文 'normal'/'trial'，而字典 mms_order_type 是 0生产/1返工/2外协
 *      → 前端 orderTypeLabel 匹配不到，列表「工单类型」整列显示 '—'
 * 规则：
 *   1) normal → 0 生产（绝大多数）
 *   2) 有 NCR 返工处置（disposition='3'）的工单 → 1 返工
 *   3) 存在委外工序记录（mms_outsource）的工单 → 2 外协
 *   4) trial 试制单 → 3 试制（字典需补 3=试制）
 * 同时修正 source_type 的英文旧值：order→2 订单直转、plan→1 计划生成、manual→3 手工创建
 */
const mysql = require('mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true,
  });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  console.log('== 处理前 order_type：', JSON.stringify(await q(`SELECT order_type t,COUNT(*) c FROM mms_work_order GROUP BY order_type`)));
  console.log('== 处理前 source_type：', JSON.stringify(await q(`SELECT source_type t,COUNT(*) c FROM mms_work_order GROUP BY source_type`)));

  // 1) 复位为生产
  const r1 = await exec(`UPDATE mms_work_order SET order_type='0'
    WHERE order_type IN ('normal','') OR order_type IS NULL`);
  console.log(`normal → 0 生产：${r1.affectedRows} 单`);

  // 2) 返工：NCR 处置为「返工/返修」，且能对应到工单号
  const rework = await q(`SELECT n.defect_level, n.material_name, w.work_order_id, w.work_order_no
    FROM qms_ncr n JOIN mms_work_order w ON w.work_order_no = n.source_no
    WHERE n.disposition='3'`);
  for (const r of rework) {
    await exec(`UPDATE mms_work_order SET order_type='1' WHERE work_order_id=?`, [r.work_order_id]);
    console.log(`  返工：${r.work_order_no}（${r.material_name}，源自 NCR 返工处置）`);
  }

  // 3) 外协：存在委外工序记录
  const outs = await q(`SELECT DISTINCT w.work_order_id, w.work_order_no, GROUP_CONCAT(DISTINCT o.process_name) ops
    FROM mms_outsource o JOIN mms_work_order w ON w.work_order_id=o.work_order_id
    WHERE w.order_type='0' GROUP BY w.work_order_id, w.work_order_no`);
  for (const r of outs) {
    await exec(`UPDATE mms_work_order SET order_type='2' WHERE work_order_id=?`, [r.work_order_id]);
    console.log(`  外协：${r.work_order_no}（委外工序：${r.ops}）`);
  }

  // 4) 试制：trial 单
  const trial = await q(`SELECT work_order_id, work_order_no, product_name, plan_qty, unit FROM mms_work_order WHERE order_type='trial'`);
  for (const r of trial) {
    await exec(`UPDATE mms_work_order SET order_type='3' WHERE work_order_id=?`, [r.work_order_id]);
    console.log(`  试制：${r.work_order_no}（${r.product_name} ${Number(r.plan_qty)}${r.unit}，新产品中试）`);
  }

  // 5) source_type 英文旧值 → 字典值
  const map = { plan: '1', order: '2', manual: '3' };
  for (const [k, v] of Object.entries(map)) {
    const r = await exec(`UPDATE mms_work_order SET source_type=? WHERE source_type=?`, [v, k]);
    if (r.affectedRows) console.log(`  source_type ${k} → ${v}：${r.affectedRows} 单`);
  }

  // 6) 试制单的备注补一句，避免「试制」标签无解释
  await exec(`UPDATE mms_work_order SET remark=CONCAT(IFNULL(remark,''), IF(IFNULL(remark,'')='','','；'), '新产品中试试制，工艺验证通过后转量产')
    WHERE order_type='3' AND remark NOT LIKE '%中试%'`);

  console.log('== 处理后 order_type：', JSON.stringify(await q(`SELECT order_type t,COUNT(*) c FROM mms_work_order GROUP BY order_type ORDER BY order_type`)));
  console.log('== 处理后 source_type：', JSON.stringify(await q(`SELECT source_type t,COUNT(*) c FROM mms_work_order GROUP BY source_type ORDER BY source_type`)));
  console.log('== 仍为空的工单类型：', JSON.stringify(await q(`SELECT COUNT(*) c FROM mms_work_order WHERE order_type IS NULL OR order_type=''`)));

  await conn.end();
})();
