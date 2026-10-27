const fs = require('fs');
// Merge all SQL files into one
let merged = '-- =============================================\n-- HR人力资源管理模块 完整SQL(表结构+菜单+字典+预置数据)\n-- =============================================\nSET NAMES utf8mb4;\nSET FOREIGN_KEY_CHECKS = 0;\n\n';

// Add table SQL (both parts)
merged += fs.readFileSync(__dirname + '/hr_tables.sql', 'utf8') + '\n\n';
merged += fs.readFileSync(__dirname + '/hr_tables2.sql', 'utf8') + '\n\n';

// Add menu/dict/preset data SQL
merged += fs.readFileSync(__dirname + '/hr_menu_dict.sql', 'utf8') + '\n';

fs.writeFileSync(__dirname + '/hr_all.sql', merged, 'utf8');
console.log('hr_all.sql merged, length:', merged.length);
