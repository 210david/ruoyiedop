const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  const rows = await q(`SELECT wo.product_code,wo.product_name,wo.spec_model,wo.unit,COUNT(*) c,SUM(wo.plan_qty) qty,SUM(wo.finished_qty) fin,
     MAX(CASE WHEN r.route_id IS NOT NULL THEN 1 ELSE 0 END) hr, MAX(CASE WHEN b.bom_id IS NOT NULL THEN 1 ELSE 0 END) hb
     FROM mms_work_order wo LEFT JOIN mms_route r ON r.product_code=wo.product_code LEFT JOIN mms_bom b ON b.product_code=wo.product_code
     GROUP BY wo.product_code,wo.product_name,wo.spec_model,wo.unit ORDER BY c DESC`);
  rows.forEach(r=>console.log([r.product_code,r.product_name,r.spec_model,r.unit,r.c,r.qty,r.fin,r.hr,r.hb].join(' | ')));
  console.log('总产品数', rows.length);
  await conn.end();
})();
