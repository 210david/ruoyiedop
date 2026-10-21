/**
 * 批量样式转换脚本
 * 将 el-dialog + el-divider + el-descriptions 转换为卡片式详情页样式
 * 遵循 detail-page-style-guide.md 规范
 */
const fs = require('fs');
const path = require('path');

const BASE = path.join(__dirname, 'src', 'views');
const DIRS = ['wms', 'dms', 'mk'];
const SKIP_FILES = [
  'mk/returnOrder/index.vue',
  'mk/payment/index.vue',
  'mk/order/index.vue',
  'mk/invoice/index.vue',
  'mk/contract/index.vue',
];

// SVG icons
const ICON_DOC = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>';
const ICON_VIEW = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg>';
const ICON_LIST = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>';
const ICON_INFO = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>';
const ICON_CHECK = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>';
const ICON_CLOCK = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>';
const ICON_BOX = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>';
const COLLAPSE_BTN = '<button class="rd-collapse-btn" :class="{ \'is-collapsed\': collapsedCards.__KEY__ }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>';

function getIconForTitle(title) {
  const t = title.toLowerCase();
  if (t.includes('审批') || t.includes('审核') || t.includes('approv')) return ICON_CHECK;
  if (t.includes('流程') || t.includes('时间') || t.includes('日志')) return ICON_CLOCK;
  if (t.includes('明细') || t.includes('列表') || t.includes('记录')) return ICON_LIST;
  if (t.includes('详情') || t.includes('查看')) return ICON_VIEW;
  if (t.includes('物料') || t.includes('产品') || t.includes('物料')) return ICON_BOX;
  return ICON_DOC;
}

function slugify(title) {
  // Generate a key from title
  const map = {
    '基本信息': 'basic', '单据信息': 'orderInfo', '物料信息': 'material', '入库信息': 'inboundInfo',
    '出库信息': 'outboundInfo', '出库明细': 'detail', '入库明细': 'detail', '库存信息': 'stock',
    '效期管理': 'expiry', '库存控制': 'stockCtrl', '其他信息': 'other', '其他': 'other',
    '资产信息': 'asset', '使用信息': 'usage', '图片与附件': 'attachment', '设备信息': 'equipment',
    '工单信息': 'orderInfo', '维修信息': 'repair', '备件信息': 'sparepart', '计划信息': 'plan',
    '出库信息': 'outbound', '入库信息': 'inbound', '库位信息': 'location', '流程信息': 'flow',
    '联系人信息': 'contact', '客户信息': 'customer', '活动信息': 'activity', '线索信息': 'lead',
    '商机信息': 'opportunity', '交互信息': 'interaction', '参与人信息': 'participant',
    '标签信息': 'tag', '编号规则': 'numberRule', '订单信息': 'orderInfo', '付款信息': 'payment',
    '开票信息': 'invoice', '退货信息': 'return', '退款信息': 'refund', '合同信息': 'contract',
    '业务信息': 'business', '补充信息': 'extra', '状态管理': 'status', '负责信息': 'owner',
    '企业信息': 'enterprise', '仓库与物料': 'warehouse', '移库信息': 'move',
  };
  if (map[title]) return map[title];
  // For detail page cards, prefix with 'd'
  return 'card_' + title.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '').substring(0, 8);
}

function processFile(filePath, relPath) {
  let content = fs.readFileSync(filePath, 'utf8');
  
  // Skip if already has rd-dialog class
  if (content.includes('class="rd-dialog"')) {
    console.log('[SKIP] Already optimized: ' + relPath);
    return false;
  }
  
  // Skip if no el-dialog
  if (!content.includes('<el-dialog')) {
    console.log('[SKIP] No dialogs: ' + relPath);
    return false;
  }
  
  let modified = false;
  let allKeys = [];
  
  // === Step 1: Transform el-dialog opening tags ===
  
  // Pattern A: <el-dialog :title="title" v-model="open" width="XXXpx" append-to-body>
  content = content.replace(
    /<el-dialog\s+:title="title"\s+v-model="(\w+)"\s+width="([^"]+)"\s+append-to-body\s*>/g,
    (match, modelVar, width) => {
      modified = true;
      return `<el-dialog v-model="${modelVar}" width="${width}" append-to-body draggable class="rd-dialog">\n      <template #header>\n        <div class="rd-detail-header">\n          <div class="rd-detail-header-icon">${ICON_DOC}</div>\n          <span class="rd-detail-header-title">{{ title }}</span>\n        </div>\n      </template>`;
    }
  );
  
  // Pattern B: <el-dialog title="XX" v-model="yy" width="XXXpx" append-to-body>
  content = content.replace(
    /<el-dialog\s+title="([^"]+)"\s+v-model="(\w+)"\s+width="([^"]+)"\s+append-to-body\s*>/g,
    (match, title, modelVar, width) => {
      modified = true;
      const icon = getIconForTitle(title);
      // Check if there's a matching data field for subtitle
      return `<el-dialog v-model="${modelVar}" width="${width}" append-to-body draggable class="rd-dialog">\n      <template #header>\n        <div class="rd-detail-header">\n          <div class="rd-detail-header-icon">${icon}</div>\n          <span class="rd-detail-header-title">${title}</span>\n        </div>\n      </template>`;
    }
  );
  
  // Pattern C: <el-dialog v-model="yy" title="XX" width="XXXpx" append-to-body> (reversed order)
  content = content.replace(
    /<el-dialog\s+v-model="(\w+)"\s+title="([^"]+)"\s+width="([^"]+)"\s+append-to-body\s*>/g,
    (match, modelVar, title, width) => {
      modified = true;
      const icon = getIconForTitle(title);
      return `<el-dialog v-model="${modelVar}" width="${width}" append-to-body draggable class="rd-dialog">\n      <template #header>\n        <div class="rd-detail-header">\n          <div class="rd-detail-header-icon">${icon}</div>\n          <span class="rd-detail-header-title">${title}</span>\n        </div>\n      </template>`;
    }
  );
  
  if (!modified) {
    console.log('[SKIP] No matching dialog patterns: ' + relPath);
    return false;
  }
  
  // === Step 2: Replace el-divider with rd-card sections ===
  // This is a stateful transformation within <el-form> or dialog body
  
  // Find all el-divider content-position="center" and replace with card sections
  // We need to track open/close state
  
  const dividerRegex = /<el-divider\s+content-position="center">([^<]+)<\/el-divider>/g;
  let dividerMatch;
  let dividerPositions = [];
  while ((dividerMatch = dividerRegex.exec(content)) !== null) {
    dividerPositions.push({
      index: dividerMatch.index,
      fullMatch: dividerMatch[0],
      title: dividerMatch[1].trim(),
      end: dividerMatch.index + dividerMatch[0].length
    });
  }
  
  if (dividerPositions.length > 0) {
    // Process dividers in reverse order to not mess up indices
    let cardKeyCounter = 0;
    const cardKeys = [];
    
    for (let i = dividerPositions.length - 1; i >= 0; i--) {
      const dp = dividerPositions[i];
      const cardKey = 'c' + (cardKeyCounter++);
      cardKeys.unshift(cardKey);
      
      const icon = getIconForTitle(dp.title);
      const collapseBtn = COLLAPSE_BTN.replace(/__KEY__/g, cardKey);
      
      const replacement = `<section class="rd-card">\n          <div class="rd-card-header" @click="toggleCard('${cardKey}')">\n            <div class="rd-card-title"><span class="rd-card-icon">${icon}</span>${dp.title}</div>\n            ${collapseBtn}\n          </div>\n          <div class="rd-card-body" v-show="!collapsedCards.${cardKey}">`;
      
      // For first divider, also add rd-page wrapper before it
      if (i === 0) {
        // Find the <el-form ...> tag that contains this divider
        const beforeDivider = content.substring(0, dp.index);
        const formTagMatch = beforeDivider.match(/<el-form[^>]*>\s*$/);
        if (formTagMatch) {
          // Insert rd-page wrapper after el-form opening tag
          const formEnd = beforeDivider.lastIndexOf('>') + 1;
          content = content.substring(0, formEnd) + '\n        <div class="rd-page">' + content.substring(formEnd);
          // Adjust dp index
          dp.index += '\n        <div class="rd-page">'.length;
          dp.end += '\n        <div class="rd-page">'.length;
        }
      }
      
      // Close previous card body if not the first divider
      if (i < dividerPositions.length - 0) {
        // Check if there's a previous card body to close
        // Actually, let's handle this differently
      }
      
      content = content.substring(0, dp.index) + replacement + content.substring(dp.end);
    }
    
    // Now we need to close each card body before the next card starts, and close the last card before </el-form>
    // Re-find all card-body openings
    const cardBodyRegex = /<div class="rd-card-body"[^>]*>/g;
    let cardBodyMatches = [];
    let cbMatch;
    while ((cbMatch = cardBodyRegex.exec(content)) !== null) {
      cardBodyMatches.push(cbMatch.index + cbMatch[0].length);
    }
    
    // For each card body opening except the last, find the next <section class="rd-card"> and insert closing tags
    for (let i = 0; i < cardBodyMatches.length - 1; i++) {
      const nextCardRegex = /<section class="rd-card">/g;
      nextCardRegex.lastIndex = cardBodyMatches[i];
      const nextCardMatch = nextCardRegex.exec(content);
      if (nextCardMatch) {
        const insertPos = nextCardMatch.index;
        content = content.substring(0, insertPos) + '          </div>\n        </section>\n        ' + content.substring(insertPos);
      }
    }
    
    // Close the last card body before </el-form>
    if (cardBodyMatches.length > 0) {
      const lastCardBody = cardBodyMatches[cardBodyMatches.length - 1];
      const formCloseIdx = content.indexOf('</el-form>', lastCardBody);
      if (formCloseIdx !== -1) {
        content = content.substring(0, formCloseIdx) + '          </div>\n        </section>\n        </div>\n      ' + content.substring(formCloseIdx);
      }
    }
    
    allKeys.push(...cardKeys);
  }
  
  // === Step 3: Replace el-descriptions with rd-grid (for detail dialogs) ===
  // Pattern: <el-descriptions :column="N" border>...</el-descriptions>
  
  // Replace el-descriptions opening
  content = content.replace(/<el-descriptions\s+:column="\d+"\s+border\s*>/g, '<div class="rd-grid">');
  content = content.replace(/<el-descriptions\s+:column="\d+"\s+border\s+title="([^"]+)"\s+class="info-group"\s*>/g, '<div class="rd-grid">');
  content = content.replace(/<\/el-descriptions>/g, '</div>');
  
  // Replace el-descriptions-item with rd-item
  // Pattern: <el-descriptions-item label="XX">CONTENT</el-descriptions-item>
  content = content.replace(
    /<el-descriptions-item\s+label="([^"]+)"(?:\s+:span="\d+")?>([\s\S]*?)<\/el-descriptions-item>/g,
    (match, label, val) => {
      const trimmedVal = val.trim();
      const isFullSpan = match.includes(':span="3"') || match.includes(':span="2"');
      const fullClass = isFullSpan ? ' rd-item--full' : '';
      const isMuted = trimmedVal.includes("|| '-'") || trimmedVal.includes("|| '-'");
      // Replace || '-' with || '暂无'
      const processedVal = trimmedVal.replace(/\|\| '-'/g, "|| '暂无'");
      const mutedClass = processedVal.includes("|| '暂无'") ? " :class=\"{ 'rd-value--muted': true }\"" : '';
      return `<div class="rd-item${fullClass}"><span class="rd-label">${label}</span><div class="rd-value"${mutedClass}>${processedVal}</div></div>`;
    }
  );
  
  // === Step 4: Add import for useDetailCard ===
  if (!content.includes('useDetailCard')) {
    const keysStr = allKeys.length > 0 ? JSON.stringify(allKeys) : '[]';
    // Try to add after useColumnResize import
    if (content.includes("import { useColumnResize } from '@/composables/useColumnResize'")) {
      content = content.replace(
        /import { useColumnResize } from '@\/composables\/useColumnResize'/,
        `import { useColumnResize } from '@/composables/useColumnResize'\nimport { useDetailCard } from '@/composables/useDetailCard'\nconst { collapsedCards, toggleCard } = useDetailCard(${keysStr})`
      );
    } else if (content.includes("const { proxy } = getCurrentInstance()")) {
      content = content.replace(
        /const \{ proxy \} = getCurrentInstance\(\)/,
        `import { useDetailCard } from '@/composables/useDetailCard'\nconst { collapsedCards, toggleCard } = useDetailCard(${keysStr})\nconst { proxy } = getCurrentInstance()`
      );
    }
  }
  
  // === Step 5: Make el-table columns align="center" in detail dialogs ===
  // This is handled per-file manually for complex cases
  
  fs.writeFileSync(filePath, content, 'utf8');
  console.log('[DONE] ' + relPath);
  return true;
}

// Main
let totalProcessed = 0;
let totalSkipped = 0;

for (const dir of DIRS) {
  const dirPath = path.join(BASE, dir);
  if (!fs.existsSync(dirPath)) continue;
  
  function walkDir(d) {
    const items = fs.readdirSync(d);
    for (const item of items) {
      const fullPath = path.join(d, item);
      const stat = fs.statSync(fullPath);
      if (stat.isDirectory()) {
        walkDir(fullPath);
      } else if (item.endsWith('.vue')) {
        const relPath = path.relative(BASE, fullPath).replace(/\\/g, '/');
        if (SKIP_FILES.includes(relPath)) {
          console.log('[SKIP] Already optimized (in skip list): ' + relPath);
          totalSkipped++;
          continue;
        }
        try {
          if (processFile(fullPath, relPath)) {
            totalProcessed++;
          } else {
            totalSkipped++;
          }
        } catch (e) {
          console.error('[ERROR] ' + relPath + ': ' + e.message);
          totalSkipped++;
        }
      }
    }
  }
  
  walkDir(dirPath);
}

console.log(`\n=== Summary ===`);
console.log(`Processed: ${totalProcessed}`);
console.log(`Skipped: ${totalSkipped}`);
