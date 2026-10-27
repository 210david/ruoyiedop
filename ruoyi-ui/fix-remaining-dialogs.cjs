/**
 * 修复剩余未转换的弹窗
 * 处理使用 :title 或 title 属性的 el-dialog
 */
const fs = require('fs');
const path = require('path');

const BASE = path.join(__dirname, 'src', 'views');

const ICON_DOC = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>';

const FILES = [
  'dms/workorder/index.vue',
  'dms/inspection/task/index.vue',
  'wms/stocktake/detail.vue',
  'wms/outbound/wave.vue',
  'wms/outbound/detail.vue',
  'wms/outbound/check.vue',
  'wms/inbound/detail.vue',
];

function processFile(relPath) {
  const filePath = path.join(BASE, relPath);
  let content = fs.readFileSync(filePath, 'utf8');
  let modified = false;

  // Pattern 1: <el-dialog :title="expr" v-model="var" width="W" append-to-body>
  content = content.replace(
    /<el-dialog\s+:title="([^"]+)"\s+v-model="(\w+)"\s+width="([^"]+)"\s+append-to-body\s*>/g,
    (match, titleExpr, modelVar, width) => {
      // Skip if already has rd-dialog
      if (match.includes('rd-dialog')) return match;
      modified = true;
      return `<el-dialog v-model="${modelVar}" width="${width}" append-to-body draggable class="rd-dialog">\n      <template #header>\n        <div class="rd-detail-header">\n          <div class="rd-detail-header-icon">${ICON_DOC}</div>\n          <span class="rd-detail-header-title">{{ ${titleExpr} }}</span>\n        </div>\n      </template>`;
    }
  );

  // Pattern 2: <el-dialog title="XX" v-model="var" width="W" append-to-body>
  content = content.replace(
    /<el-dialog\s+title="([^"]+)"\s+v-model="(\w+)"\s+width="([^"]+)"\s+append-to-body\s*>/g,
    (match, title, modelVar, width) => {
      if (match.includes('rd-dialog')) return match;
      modified = true;
      return `<el-dialog v-model="${modelVar}" width="${width}" append-to-body draggable class="rd-dialog">\n      <template #header>\n        <div class="rd-detail-header">\n          <div class="rd-detail-header-icon">${ICON_DOC}</div>\n          <span class="rd-detail-header-title">${title}</span>\n        </div>\n      </template>`;
    }
  );

  // Pattern 3: Handle dialogs with extra attributes like :close-on-click-modal="false"
  content = content.replace(
    /<el-dialog\s+title="([^"]+)"\s+v-model="(\w+)"\s+width="([^"]+)"\s+append-to-body\s+([^>]+)>/g,
    (match, title, modelVar, width, extra) => {
      if (match.includes('rd-dialog')) return match;
      modified = true;
      return `<el-dialog v-model="${modelVar}" width="${width}" append-to-body draggable class="rd-dialog" ${extra}>\n      <template #header>\n        <div class="rd-detail-header">\n          <div class="rd-detail-header-icon">${ICON_DOC}</div>\n          <span class="rd-detail-header-title">${title}</span>\n        </div>\n      </template>`;
    }
  );

  // Pattern 4: Handle workorder detail dialog with custom class
  // <el-dialog v-model="viewOpen" width="1120px" append-to-body class="workorder-detail-dialog" top="5vh">
  // This one already has a custom header - just need to add rd-dialog class
  content = content.replace(
    /class="workorder-detail-dialog"/g,
    'class="rd-dialog workorder-detail-dialog"'
  );

  if (modified) {
    fs.writeFileSync(filePath, content, 'utf8');
    console.log('[DONE] ' + relPath);
  } else {
    console.log('[SKIP] No changes needed: ' + relPath);
  }
}

for (const f of FILES) {
  try {
    processFile(f);
  } catch (e) {
    console.error('[ERROR] ' + f + ': ' + e.message);
  }
}
console.log('\nDone!');
