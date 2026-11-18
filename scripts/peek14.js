const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  const d = await q(`SELECT dict_type,dict_label,dict_value FROM sys_dict_data WHERE dict_type LIKE 'marketing%' ORDER BY dict_type,dict_sort`);
  let cur=''; let line=[];
  d.forEach(x=>{ if(x.dict_type!==cur){ if(cur) console.log(cur+': '+line.join(' | ')); cur=x.dict_type; line=[];} line.push(x.dict_label+'='+x.dict_value); });
  if(cur) console.log(cur+': '+line.join(' | '));
  await conn.end();
})();
