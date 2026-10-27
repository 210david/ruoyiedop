const fs = require('fs');
const c = fs.readFileSync(__dirname + '/hr_entities.json', 'utf8');
console.log('Length:', c.length);
console.log('Last 500 chars:');
console.log(c.slice(-500));
// Try parsing
try {
  const j = JSON.parse(c);
  console.log('Parsed OK, entities:', j.entities.length);
  j.entities.forEach(e => console.log(' -', e.cls, e.table, 'fields:', e.fields.length));
} catch(ex) {
  console.log('Parse error:', ex.message);
}
