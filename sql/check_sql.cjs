const fs = require('fs');
try {
  const c = fs.readFileSync(__dirname + '/hr_all.sql', 'utf8');
  console.log('hr_all.sql length:', c.length);
  console.log('Last 300:', c.slice(-300));
} catch(e) { console.log('hr_all.sql:', e.message); }
try {
  const c = fs.readFileSync(__dirname + '/hr_tables.sql', 'utf8');
  console.log('hr_tables.sql length:', c.length);
} catch(e) { console.log('hr_tables.sql:', e.message); }
try {
  const c = fs.readFileSync(__dirname + '/hr_tables2.sql', 'utf8');
  console.log('hr_tables2.sql length:', c.length);
  // find CREATE TABLE statements
  const tables = c.match(/CREATE TABLE\s+\S+/gi);
  if (tables) console.log('Tables in hr_tables2.sql:', tables);
} catch(e) { console.log('hr_tables2.sql:', e.message); }
// also check hr_all.sql tables
try {
  const c = fs.readFileSync(__dirname + '/hr_all.sql', 'utf8');
  const tables = c.match(/CREATE TABLE\s+\S+/gi);
  if (tables) console.log('Tables in hr_all.sql:', tables);
} catch(e) {}
