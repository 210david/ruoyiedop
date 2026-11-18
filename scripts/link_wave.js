const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => (await conn.query(s,p))[0];
  const waves = await q(`SELECT wave_id, wave_no, warehouse_id, create_time FROM wms_wave ORDER BY wave_id`);
  const outs = await q(`SELECT order_id, order_type, warehouse_id, create_time FROM wms_outbound_order WHERE del_flag='0'`);
  let n = 0;
  for (const w of waves) {
    const d = String(w.create_time).slice(0,10);
    const list = outs.filter(o => String(o.create_time).slice(0,10) === d && Number(o.warehouse_id) === Number(w.warehouse_id));
    for (const o of list) {
      await conn.query(`UPDATE wms_outbound_order SET wave_id=? WHERE order_id=?`, [w.wave_id, o.order_id]);
      n++;
    }
  }
  console.log('关联波次出库单', n);
  await conn.end();
})();
