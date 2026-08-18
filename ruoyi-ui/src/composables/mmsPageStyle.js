/**
 * MMS 模块列表页面通用样式 Mixin
 * 
 * 使用方式：在各页面的 <style scoped> 中通过 @import 引入
 * 注意：由于 scoped 样式的限制，CSS 变量需要在每个页面的根元素上设置
 * 
 * 更推荐的方式：在各页面 <style> 标签内直接内联此样式
 * 通过 mixin 函数生成样式字符串
 */

/**
 * 生成页面级样式
 * @param {string} prefix - 页面 class 前缀，如 'mms-issue-page'
 * @returns {string} CSS 样式字符串
 */
export function generateMmsPageStyle(prefix) {
  return `
${prefix}{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
${prefix} .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
${prefix} .filter-card{padding:14px 20px 16px}
${prefix} .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
${prefix} .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
${prefix} .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
${prefix} .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
${prefix} .filter-card .adv-link:hover{color:var(--brand-600)}
${prefix} .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
${prefix} .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
${prefix} .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
${prefix} .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
${prefix} .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
${prefix} .filter-card .filter-buttons{display:flex;gap:8px}
${prefix} .field{display:flex;flex-direction:column;gap:6px}
${prefix} .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
${prefix} .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
${prefix} .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
${prefix} .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
${prefix} .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
${prefix} .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
${prefix} .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
${prefix} .field .control :deep(.el-select){width:100%}
${prefix} .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
${prefix} .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
${prefix} .toolbar .left,${prefix} .toolbar .right{display:flex;gap:8px;align-items:center}
${prefix} .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
${prefix} .table-wrap{overflow-x:auto}
${prefix} .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
${prefix} .app-table :deep(.el-table__body td){border-right-color:transparent!important}
${prefix} .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
${prefix} .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
${prefix} .app-table :deep(.el-table__inner-wrapper::before){display:none}
${prefix} .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
${prefix} .badge .dot{width:6px;height:6px;border-radius:50%}
${prefix} .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a} ${prefix} .badge.amber .dot{background:var(--amber-500)}
${prefix} .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe} ${prefix} .badge.blue .dot{background:var(--blue-500)}
${prefix} .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0} ${prefix} .badge.green .dot{background:var(--green-500)}
${prefix} .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca} ${prefix} .badge.red .dot{background:var(--red-500)}
${prefix} .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)} ${prefix} .badge.gray .dot{background:var(--ink-400)}
${prefix} .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
${prefix} .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
${prefix} .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
${prefix} .tabs-track::-webkit-scrollbar{display:none}
${prefix} .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
${prefix} .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
${prefix} .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
${prefix} .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
${prefix} .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
${prefix} .status-tab.is-active .count{background:var(--brand-600);color:#fff}
${prefix} .status-tab.is-active .dot{background:var(--brand-500)}
${prefix} .status-tab.tab-draft .dot{background:var(--amber-500)} ${prefix} .status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)} ${prefix} .status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
${prefix} .status-tab.tab-audit .dot{background:var(--blue-500)} ${prefix} .status-tab.tab-audit .count{background:var(--blue-50);color:var(--blue-700)} ${prefix} .status-tab.is-active.tab-audit .count{background:var(--blue-500);color:#fff}
${prefix} .status-tab.tab-done .dot{background:var(--green-500)} ${prefix} .status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)} ${prefix} .status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
${prefix} .status-tab.tab-reject .dot{background:var(--red-500)} ${prefix} .status-tab.tab-reject .count{background:var(--red-50);color:var(--red-700)} ${prefix} .status-tab.is-active.tab-reject .count{background:var(--red-500);color:#fff}
${prefix} .status-tab.tab-void .dot{background:var(--ink-400)} ${prefix} .status-tab.tab-void .count{background:var(--ink-100);color:var(--ink-500)} ${prefix} .status-tab.is-active.tab-void .count{background:var(--ink-400);color:#fff}
${prefix} .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
${prefix} .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
${prefix} .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.status-help-content{max-height:500px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{background-color:#ecf5ff;border-radius:8px;padding:16px;border-left:4px solid #409eff}
.status-help-content .highlight-card p{margin:6px 0;line-height:1.6;font-size:13px;color:#606266}
@media(max-width:1100px){${prefix} .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){${prefix} .filter-card .filter-bar{grid-template-columns:1fr}}
`
}
