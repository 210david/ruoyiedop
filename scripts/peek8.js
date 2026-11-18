const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  const t = await q(`SELECT table_name tn FROM information_schema.tables WHERE table_schema='ruoyi_ylsp' AND table_name LIKE 'wms_%' ORDER BY table_name`);
  for(const x of t){ const r = await q(`SELECT COUNT(*) c FROM \`${x.tn}\``); console.log(x.tn, r[0].c); }
  console.log('warehouse:', await q(`SELECT warehouse_id,warehouse_code,warehouse_name FROM wms_warehouse`));
  console.log('location sample:', await q(`SELECT * FROM wms_location LIMIT 3`));
  console.log('outbound sample:', await q(`SELECT outbound_id,outbound_no,outbound_type,status,create_time FROM wms_outbound_order ORDER BY outbound_id DESC LIMIT 5`));
  await conn.end();
})();
