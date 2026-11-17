const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  console.log(await q(`SELECT warehouse_id, location_id, COUNT(*) c, SUM(qty) q FROM wms_inventory GROUP BY warehouse_id, location_id ORDER BY c DESC LIMIT 15`));
  console.log('location_id 是否在 wms_warehouse:', await q(`SELECT COUNT(DISTINCT location_id) loc FROM wms_inventory`), await q(`SELECT COUNT(*) c FROM wms_warehouse WHERE warehouse_id IN (SELECT DISTINCT location_id FROM wms_inventory)`));
  console.log(await q(`SELECT * FROM wms_warehouse WHERE warehouse_id>=6`));
  console.log('outbound type 分布:', await q(`SELECT outbound_type, status, COUNT(*) c FROM wms_outbound_order GROUP BY outbound_type,status`));
  await conn.end();
})();
