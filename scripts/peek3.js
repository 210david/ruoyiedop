const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  const d = await q(`SELECT dict_type,dict_label,dict_value FROM sys_dict_data WHERE dict_type REGEXP '^(mms|qms|wms|pms)_' ORDER BY dict_type,dict_sort`);
  let cur=''; let line=[];
  d.forEach(x=>{ if(x.dict_type!==cur){ if(cur) console.log(cur+': '+line.join(' | ')); cur=x.dict_type; line=[];} line.push(x.dict_label+'='+x.dict_value); });
  if(cur) console.log(cur+': '+line.join(' | '));
  console.log('\nteam/shift/resource 空?', await q(`SELECT (SELECT COUNT(*) FROM mms_team) team,(SELECT COUNT(*) FROM mms_shift) shift,(SELECT COUNT(*) FROM mms_resource) res,(SELECT COUNT(*) FROM mms_line) line`));
  await conn.end();
})();
