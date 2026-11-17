const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  console.log('finish batch 空:', await q(`SELECT COUNT(*) c, SUM(batch_no='' OR batch_no IS NULL) empty_cnt FROM mms_finish_receipt`));
  console.log('issue batch 空:', await q(`SELECT COUNT(*) c, SUM(batch_no='' OR batch_no IS NULL) empty_cnt FROM mms_issue`));
  console.log('issue_detail 样例:', await q(`SELECT * FROM mms_issue_detail LIMIT 3`));
  console.log('users:', await q(`SELECT user_id,user_name,nick_name,dept_id FROM sys_user WHERE del_flag='0' LIMIT 20`));
  console.log('bom_detail(肠类):', await q(`SELECT * FROM mms_bom_detail WHERE bom_id=1`));
  console.log('inv 样例:', await q(`SELECT inventory_id,material_code,material_name,warehouse_name,available_qty,unit FROM wms_inventory LIMIT 3`));
  await conn.end();
})();
