const fs = require('fs');
// Check menu SQL
try {
  const c = fs.readFileSync(__dirname + '/hr_menu.sql', 'utf8');
  console.log('hr_menu.sql length:', c.length);
  // Count INSERT statements
  const inserts = c.match(/INSERT INTO/g);
  console.log('INSERT count:', inserts ? inserts.length : 0);
  // Find menu_ids
  const ids = [...c.matchAll(/VALUES\((\d+),/g)];
  console.log('Menu IDs:', ids.map(m => m[1]).join(', '));
} catch(e) { console.log('hr_menu.sql:', e.message); }
// Check dict SQL
try {
  const c = fs.readFileSync(__dirname + '/hr_menu_dict.sql', 'utf8');
  console.log('hr_menu_dict.sql length:', c.length);
} catch(e) { console.log('hr_menu_dict.sql:', e.message); }
// Check gen_hr_menu.cjs
try {
  const c = fs.readFileSync(__dirname + '/gen_hr_menu.cjs', 'utf8');
  console.log('gen_hr_menu.cjs length:', c.length);
  console.log('First 200:', c.slice(0, 200));
} catch(e) { console.log('gen_hr_menu.cjs:', e.message); }
