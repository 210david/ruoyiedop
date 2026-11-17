const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  console.table(await q(`SELECT customer_id,customer_name,customer_type,credit_status FROM mk_customer`));
  console.log(await q(`SELECT lead_source,lead_status FROM sys_dict_data WHERE dict_type IN ('mk_lead_source','mk_lead_status','mk_opportunity_status','mk_activity_type')`).catch(e=>e.message));
  await conn.end();
})();
