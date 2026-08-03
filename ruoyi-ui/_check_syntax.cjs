const fs = require('fs');
const content = fs.readFileSync('src/views/pms/invoice/index.vue', 'utf8');
const scriptMatch = content.match(/<script setup[^>]*>([\s\S]*?)<\/script>/);
if (scriptMatch) {
  let script = scriptMatch[1]
    .replace(/^.*import .*$/gm, '')
    .replace(/import\.meta\.env\.VITE_APP_BASE_API/g, "''")
    .replace(/onActivated\(/g, 'null(');
  fs.writeFileSync('_temp_check.mjs', script);
  console.log('Temp file written. Script lines:', script.split('\n').length);
} else {
  console.log('No script block found');
}
