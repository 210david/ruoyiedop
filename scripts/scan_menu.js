const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const [r] = await conn.query(`SELECT menu_id,parent_id,menu_name,path,component,menu_type,visible,status FROM sys_menu WHERE menu_type='C' ORDER BY parent_id,order_num`);
  const tops = {};
  const [all] = await conn.query(`SELECT menu_id,menu_name,parent_id,path FROM sys_menu`);
  const map = new Map(all.map(m=>[m.menu_id,m]));
  function rootOf(id){ let m=map.get(id); while(m && m.parent_id && m.parent_id!==0){ m=map.get(m.parent_id);} return m?m.menu_name:'?'; }
  r.forEach(m=>{ const k=rootOf(m.menu_id); (tops[k]=tops[k]||[]).push(m.menu_name); });
  for(const k of Object.keys(tops)) console.log(k+' ('+tops[k].length+'): '+tops[k].join(', '));
  await conn.end();
})();
