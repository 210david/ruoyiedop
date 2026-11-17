const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => (await conn.query(s,p))[0];
  console.log('凭证合计:', await q(`SELECT SUM(total_amount) t, COUNT(*) c FROM fin_voucher WHERE status IN ('AUDITED','POSTED')`));
  console.log('分录借贷:', await q(`SELECT SUM(CASE WHEN direction='DEBIT' THEN amount ELSE 0 END) d, SUM(CASE WHEN direction='CREDIT' THEN amount ELSE 0 END) c FROM fin_voucher_entry e JOIN fin_voucher v ON v.voucher_id=e.voucher_id WHERE v.status IN ('AUDITED','POSTED')`));
  console.log('总账 202610:', await q(`SELECT SUM(closing_debit) cd, SUM(closing_credit) cc FROM fin_ledger WHERE period='202610'`));
  console.log('按科目:', await q(`SELECT account_code,MAX(period) p,SUM(closing_debit) cd,SUM(closing_credit) cc FROM fin_ledger GROUP BY account_code`));
  await conn.end();
})();
