const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  console.table(await q(`SELECT r.route_id,r.route_no,r.product_code,r.product_name,r.version,
    COUNT(p.id) steps, GROUP_CONCAT(CONCAT(p.step_seq,':',p.process_name) ORDER BY p.step_seq) chain
    FROM mms_route r LEFT JOIN mms_route_process p ON p.route_id=r.route_id GROUP BY r.route_id ORDER BY r.route_id`));
  await conn.end();
})();
