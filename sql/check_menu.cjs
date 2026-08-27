const fs = require('fs');
const c = fs.readFileSync(__dirname + '/hr_menu.sql', 'utf8');
// Check if certificate, healthCheck, laborWorker, tempSettlement are present
const missing = [];
['certificate', 'healthCheck', 'laborWorker', 'tempSettlement'].forEach(p => {
  if (!c.includes(p)) missing.push(p);
});
if (missing.length > 0) {
  console.log('Missing menus:', missing.join(', '));
} else {
  console.log('All menus present!');
}
// Show INSERT lines
const lines = c.split('\n').filter(l => l.includes('INSERT INTO'));
console.log('Total INSERT lines:', lines.length);
// Show component paths
const comps = [...c.matchAll(/hr\/(\w+)/g)];
const uniqueComps = [...new Set(comps.map(m => m[1]))];
console.log('Component paths found:', uniqueComps.join(', '));
