const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  console.table(await q(`SELECT ncr_id,ncr_no,source_type,source_no,material_code,material_name,supplier_name,batch_no,defect_qty,defect_level,disposition,ncr_status,discover_time FROM qms_ncr`));
  console.log(await q(`SELECT supplier_id,supplier_name,supplier_code FROM wms_supplier LIMIT 6`));
  await conn.end();
})();
