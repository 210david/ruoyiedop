/**
 * 修复脚本：确保 rd-page 包装器正确添加
 * 检查所有有 rd-dialog 的文件，确保 el-form 内有 rd-page 包装器
 */
const fs = require('fs');
const path = require('path');

const BASE = path.join(__dirname, 'src', 'views');
const DIRS = ['wms', 'dms', 'mk'];

function fixFile(filePath, relPath) {
  let content = fs.readFileSync(filePath, 'utf8');
  
  if (!content.includes('class="rd-dialog"')) return false;
  
  let fixed = false;
  
  // Fix 1: If there's rd-card inside el-form but no rd-page wrapper, add it
  // Pattern: <el-form ...>\n ... <section class="rd-card"> without <div class="rd-page"> before it
  if (content.includes('class="rd-card"') && !content.includes('class="rd-page"')) {
    // Add rd-page after el-form opening tag
    content = content.replace(
      /(<el-form[^>]*>)\s*\n(\s*<!--[^>]*-->)?\s*\n(\s*)(<section class="rd-card">)/,
      (match, formTag, comment, indent, section) => {
        fixed = true;
        const commentStr = comment ? comment + '\n' : '';
        return `${formTag}\n        ${commentStr}${indent}<div class="rd-page">\n${indent}${section}`;
      }
    );
    
    // If the above didn't match, try a simpler pattern
    if (!fixed) {
      content = content.replace(
        /(<el-form[^>]*>)\s*\n(\s*)(<section class="rd-card">)/,
        (match, formTag, indent, section) => {
          fixed = true;
          return `${formTag}\n        <div class="rd-page">\n${indent}${section}`;
        }
      );
    }
  }
  
  // Fix 2: If rd-page opening exists but closing doesn't, or vice versa
  const rdPageOpen = (content.match(/<div class="rd-page">/g) || []).length;
  const rdPageClose = (content.match(/<\/div>\s*<\/section>\s*<\/div>\s*<\/el-form>/g) || []).length;
  
  // Fix 3: Remove orphaned closing </div> that was added by the batch script
  // but rd-page opening wasn't added
  if (rdPageOpen === 0 && content.includes('rd-card')) {
    // The batch script added </div></section></div> before </el-form>
    // but didn't add <div class="rd-page">. So we need to either:
    // a) Add the opening tag, or
    // b) Remove the extra closing tag
    
    // Option a: Add rd-page opening after el-form
    const formMatch = content.match(/<el-form[^>]*>/);
    if (formMatch) {
      const insertPos = content.indexOf(formMatch[0]) + formMatch[0].length;
      content = content.substring(0, insertPos) + '\n        <div class="rd-page">' + content.substring(insertPos);
      fixed = true;
    }
  }
  
  // Fix 4: For detail dialogs (no el-form, just rd-page), ensure rd-page is present
  // If dialog has rd-card but no rd-page and no el-form, wrap cards in rd-page
  if (content.includes('class="rd-card"') && !content.includes('class="rd-page"') && !content.includes('<el-form')) {
    // Find the first rd-card after the header template
    const headerEndIdx = content.indexOf('</template>');
    if (headerEndIdx !== -1) {
      const afterHeader = content.substring(headerEndIdx);
      const firstCardIdx = afterHeader.indexOf('<section class="rd-card">');
      if (firstCardIdx !== -1) {
        const insertPos = headerEndIdx + firstCardIdx;
        content = content.substring(0, insertPos) + '<div class="rd-page">\n        ' + content.substring(insertPos);
        fixed = true;
      }
    }
  }
  
  // Fix 5: Remove leftover HTML comments like <!-- 分组一：基本信息 --> that are now redundant
  // (These are harmless but messy)
  
  if (fixed) {
    fs.writeFileSync(filePath, content, 'utf8');
    console.log('[FIXED] ' + relPath);
    return true;
  }
  return false;
}

function walkDir(d) {
  const items = fs.readdirSync(d);
  for (const item of items) {
    const fullPath = path.join(d, item);
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory()) {
      walkDir(fullPath);
    } else if (item.endsWith('.vue')) {
      const relPath = path.relative(BASE, fullPath).replace(/\\/g, '/');
      try {
        fixFile(fullPath, relPath);
      } catch (e) {
        console.error('[ERROR] ' + relPath + ': ' + e.message);
      }
    }
  }
}

let totalFixed = 0;
for (const dir of DIRS) {
  const dirPath = path.join(BASE, dir);
  if (fs.existsSync(dirPath)) {
    walkDir(dirPath);
  }
}

console.log('\nDone!');
