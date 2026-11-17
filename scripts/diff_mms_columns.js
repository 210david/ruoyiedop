/**
 * 对比 07_mms_data.sql 中每张表 INSERT 的列 vs 2026-10-13 全库备份中的建表列，
 * 找出 10-13 基线之后新增的列（服务器可能缺这些列）。
 * 用法: node scripts/diff_mms_columns.js
 */
const fs = require('fs');

const DATA = 'D:/EDOP/patch_20261117/sql/data/07_mms_data.sql';
const BACKUP = 'D:/EDOP_QY/EDOP_YLSP/sql/RUOYI_CFSP_full_backup_20261013.sql';

const dataSql = fs.readFileSync(DATA, 'utf8');
const bakSql = fs.readFileSync(BACKUP, 'utf8');

// 1. 数据文件里每个表的列清单
const insertRe = /INSERT\s+IGNORE INTO `(\w+)` \(([^)]+)\)/g;
const dataCols = {};
let m;
while ((m = insertRe.exec(dataSql))) {
  const t = m[1];
  const cols = m[2].split(',').map((s) => s.trim().replace(/`/g, ''));
  if (!dataCols[t]) dataCols[t] = new Set();
  cols.forEach((c) => dataCols[t].add(c));
}

// 2. 备份里每个表的建表列清单
const createRe = /CREATE TABLE `(\w+)` \(([\s\S]*?)\n\) ENGINE/g;
const bakCols = {};
while ((m = createRe.exec(bakSql))) {
  const t = m[1];
  const cols = new Set();
  // 逐行解析列定义（以 `col` 开头的行）
  for (const line of m[2].split('\n')) {
    const cm = line.match(/^\s*`(\w+)`/);
    if (cm) cols.add(cm[1]);
  }
  bakCols[t] = cols;
}

// 3. 差集
const report = [];
for (const [t, cols] of Object.entries(dataCols)) {
  if (!bakCols[t]) {
    report.push({ table: t, type: '表在10-13备份中不存在', cols: [...cols].join(',') });
    continue;
  }
  const missing = [...cols].filter((c) => !bakCols[t].has(c));
  if (missing.length) report.push({ table: t, type: '缺列', cols: missing.join(',') });
}

if (report.length === 0) {
  console.log('07 数据文件所有列在 10-13 备份结构中都存在，无新增列。');
} else {
  console.log('10-13 基线之后新增的列（服务器可能缺）：\n');
  for (const r of report) console.log(`${r.table.padEnd(28)} ${r.type}: ${r.cols}`);
}
