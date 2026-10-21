/**
 * 批量将列表页弹性文本列的 :width="colWidth(...)" 改为 :min-width="colWidth(...)"
 * 用法: node convert-colwidth.mjs [pilot|all]
 *   pilot: 仅处理试点3页
 *   all:   全量处理
 */
import fs from 'fs';
import path from 'path';

const VIEWS = path.resolve(process.cwd(), 'src/views');

// 弹性文本列关键词（按中文 label 判定）
const ELASTIC = ['名称','姓名','备注','描述','说明','地址','内容','标题','意见','原因','规格','型号','供应商','客户','公司','邮箱','企业','摘要','问题','关键词','概述','范围','标准要求'];

const PILOT = ['hr/specialDeduction/index.vue', 'hr/employee/index.vue', 'dms/partledger/index.vue'];
const mode = process.argv[2] || 'pilot';

const files = [];
(function walk(d) {
  for (const f of fs.readdirSync(d)) {
    const p = path.join(d, f);
    if (fs.statSync(p).isDirectory()) walk(p);
    else if (f === 'index.vue' && fs.readFileSync(p, 'utf8').includes('useColumnResize')) files.push(p);
  }
})(VIEWS);

let changedFiles = 0, changedCols = 0;
const report = [];
for (const f of files) {
  const rel = path.relative(VIEWS, f).split(path.sep).join('/');
  if (mode === 'pilot' && !PILOT.includes(rel)) continue;

  let t = fs.readFileSync(f, 'utf8');
  let n = 0;
  t = t.replace(/<el-table-column([^>]*)>/g, (m, attrs) => {
    if (!attrs.includes(':width="colWidth(')) return m;
    const lm = attrs.match(/label="([^"]+)"/);
    const label = lm ? lm[1] : '';
    if (!ELASTIC.some(k => label.includes(k))) return m;
    n++;
    return '<el-table-column' + attrs.replace(/:width="(colWidth\([^)]*\))"/, ':min-width="$1"') + '>';
  });
  if (n) {
    fs.writeFileSync(f, t);
    changedFiles++; changedCols += n;
    report.push(`${rel}: ${n}列`);
  }
}
console.log(report.join('\n'));
console.log('--- mode:', mode, '| files changed:', changedFiles, '| columns converted:', changedCols);
