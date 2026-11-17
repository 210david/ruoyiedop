const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  console.log('workshop:'); console.table(await q(`SELECT * FROM mms_workshop`));
  console.log('process:'); console.table(await q(`SELECT process_id,process_code,process_name,process_type FROM mms_process`));
  console.log('wo route/bom 覆盖:');
  console.log(await q(`SELECT COUNT(*) total, SUM(route_id IS NOT NULL) has_route, SUM(bom_id IS NOT NULL) has_bom FROM mms_work_order`));
  console.log('产品->路线 覆盖:');
  console.log(await q(`SELECT COUNT(DISTINCT wo.product_code) prods, COUNT(DISTINCT CASE WHEN r.route_id IS NOT NULL THEN wo.product_code END) matched FROM mms_work_order wo LEFT JOIN mms_route r ON r.product_code=wo.product_code`));
  console.log('wo 日期范围:'); console.log(await q(`SELECT MIN(plan_start),MAX(plan_finish),MIN(create_time),MAX(create_time) FROM mms_work_order`));
  console.log('finish_receipt 样例:'); console.table(await q(`SELECT * FROM mms_finish_receipt LIMIT 3`));
  console.log('issue 样例:'); console.table(await q(`SELECT * FROM mms_issue LIMIT 2`));
  await conn.end();
})();
