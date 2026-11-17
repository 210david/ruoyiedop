const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  console.log('bom:', await q(`SELECT bom_id,bom_no,product_code,product_name,version,status FROM mms_bom LIMIT 25`));
  console.log('\nbom_detail 列:', (await q(`SELECT column_name FROM information_schema.columns WHERE table_schema='ruoyi_ylsp' AND table_name='mms_bom_detail' ORDER BY ordinal_position`)).map(x=>x.column_name).join(','));
  console.log(await q(`SELECT * FROM mms_bom_detail LIMIT 5`));
  console.log('\n工单按产品统计(rue matched):');
  console.log(await q(`SELECT wo.product_code, wo.product_name, COUNT(*) wo_cnt,
     MAX(CASE WHEN r.route_id IS NOT NULL THEN 1 ELSE 0 END) has_route,
     MAX(CASE WHEN b.bom_id IS NOT NULL THEN 1 ELSE 0 END) has_bom
     FROM mms_work_order wo
     LEFT JOIN mms_route r ON r.product_code=wo.product_code
     LEFT JOIN mms_bom b ON b.product_code=wo.product_code
     GROUP BY wo.product_code, wo.product_name ORDER BY wo_cnt DESC LIMIT 40`));
  await conn.end();
})();
