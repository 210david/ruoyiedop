<template>
  <div class="app-container bid-crawlLog-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>采集源</label><div class="control"><el-input v-model="queryParams.sourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>执行结果</label><div class="control"><el-select v-model="queryParams.runStatus" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_crawl_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>错误关键字</label><div class="control"><el-input v-model="queryParams.errorMsg" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>开始时间</label><div class="control"><el-date-picker v-model="startRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
        <div class="field"><label>采集源ID</label><div class="control"><el-input-number v-model="queryParams.sourceId" :min="1" controls-position="right" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 只读日志，用于排查"为什么没抓到"</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTab === 'all' }" @click="handleTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ tabCounts.all }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '0' }" @click="handleTabClick('0')"><span class="dot" style="background:var(--green-500)"></span><span>成功</span><span class="count">{{ tabCounts['0'] || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '1' }" @click="handleTabClick('1')"><span class="dot" style="background:var(--amber-500)"></span><span>部分失败</span><span class="count">{{ tabCounts['1'] || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '2' }" @click="handleTabClick('2')"><span class="dot" style="background:var(--red-500)"></span><span>失败</span><span class="count">{{ tabCounts['2'] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><QuestionFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="采集源" prop="sourceName" key="sourceName" :min-width="colWidth('sourceName', 180)" show-overflow-tooltip resizable />
          <el-table-column label="执行结果" prop="runStatus" key="runStatus" width="110" align="center">
            <template #default="scope"><span class="badge" :class="statusBadge(scope.row.runStatus)">{{ statusLabel(scope.row.runStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="抓取条数" prop="fetchedCount" key="fetchedCount" width="100" align="center" sortable="custom" :sort-orders="['descending','ascending']" />
          <el-table-column label="新增条数" prop="insertedCount" key="insertedCount" width="100" align="center" sortable="custom" :sort-orders="['descending','ascending']" />
          <el-table-column label="命中条数" prop="matchedCount" key="matchedCount" width="100" align="center" sortable="custom" :sort-orders="['descending','ascending']" />
          <el-table-column label="开始时间" prop="startTime" key="startTime" width="170" align="center" sortable="custom" :sort-orders="['descending','ascending']" />
          <el-table-column label="耗时" key="duration" width="100" align="center">
            <template #default="scope">{{ duration(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="错误信息" prop="errorMsg" key="errorMsg" :min-width="colWidth('errorMsg', 260)" show-overflow-tooltip resizable>
            <template #default="scope"><span class="error-text">{{ scope.row.errorMsg || '—' }}</span></template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <el-dialog v-model="showStatusHelp" title="抓取日志业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">抓取日志是什么？</div>
          <div class="highlight-card-body"><p>系统每次执行采集源抓取都会记录一条日志，包含抓取/新增/命中条数与错误信息，是排查"为什么没抓到"的唯一依据。</p></div>
        </div>
        <h4>二、状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="成功">抓取流程正常完成（抓到但全部重复也算成功）</el-descriptions-item>
          <el-descriptions-item label="部分失败">部分数据解析或入库异常，其余成功</el-descriptions-item>
          <el-descriptions-item label="失败">抓取整体失败，查看错误信息定位（网络/配置/站点改版）</el-descriptions-item>
        </el-descriptions>
        <h4>三、操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>排查：</strong>失败记录 → 复制错误信息 → 到采集源管理检查 parse_config 配置</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>验证：</strong>修正配置后到采集源管理点击「执行」重新验证</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="BidCrawlLog">
import { listCrawlLog } from '@/api/bid/crawlLog'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { bid_crawl_status } = proxy.useDict('bid_crawl_status')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bid_crawlLog_index')

const loading = ref(true)
const list = ref([])
const total = ref(0)
const showSearch = ref(true)
const startRange = ref([])
const activeTab = ref('all')
const showStatusHelp = ref(false)
const tabCounts = ref({ all: 0 })

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, sourceName: undefined, runStatus: undefined, errorMsg: undefined, sourceId: undefined, params: {} }
})
const { queryParams } = toRefs(data)

function statusBadge(v) { return { '0': 'green', '1': 'amber', '2': 'red' }[v] || 'gray' }
function statusLabel(v) { const d = bid_crawl_status.value.find(x => x.value == v); return d ? d.label : '—' }
function duration(row) {
  if (!row.startTime || !row.endTime) return '—'
  return Math.max(0, Math.round((new Date(row.endTime) - new Date(row.startTime)) / 1000)) + '秒'
}

function getList() {
  loading.value = true
  listCrawlLog(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    tabCounts.value = { all: res.total, ...(list.value && list.value.length ? {} : {}) }
    loading.value = false
    applySavedWidths()
  })
}
function handleTabClick(tab) { activeTab.value = tab; queryParams.value.runStatus = tab === 'all' ? undefined : tab; handleQuery() }
function handleQuery() { queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, startRange.value, 'StartTime'); getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; startRange.value = []; activeTab.value = 'all'; handleQuery() }
function handleSortChange(column) {
  if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' }
  else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }
  getList()
}
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.bid-crawlLog-page { padding-top:10px; --brand-50:#eef2ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.bid-crawlLog-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.bid-crawlLog-page .filter-head { display:flex; align-items:center; padding:12px 20px 0; }
.bid-crawlLog-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; }
.bid-crawlLog-page .filter-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-500); }
.bid-crawlLog-page .filter-bar { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:12px 16px; padding:12px 20px; }
.bid-crawlLog-page .field label { display:block; font-size:12px; color:var(--ink-500); margin-bottom:4px; }
.bid-crawlLog-page .filter-actions { display:flex; align-items:center; justify-content:space-between; padding:0 20px 14px; }
.bid-crawlLog-page .filter-info { display:flex; align-items:center; gap:6px; font-size:12px; color:var(--ink-400); }
.bid-crawlLog-page .filter-buttons { display:flex; gap:8px; }
/* 状态页签 */
.bid-crawlLog-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.bid-crawlLog-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.bid-crawlLog-page .tabs-track::-webkit-scrollbar { display:none; }
.bid-crawlLog-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s; white-space:nowrap; border:1px solid transparent; background:transparent; }
.bid-crawlLog-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.bid-crawlLog-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; }
.bid-crawlLog-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.bid-crawlLog-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.bid-crawlLog-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; white-space:nowrap; flex-shrink:0; transition:all .15s; }
.tip-pill:hover { background:#fffbeb; border-color:#f59e0b; color:#78350f; }
.bid-crawlLog-page .pagination-container { padding:10px 12px; }
.bid-crawlLog-page .badge { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.bid-crawlLog-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.bid-crawlLog-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.bid-crawlLog-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.bid-crawlLog-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.error-text { color: var(--red-700); }
/* 业务操作说明弹窗 */
.status-help-content { max-height:520px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .highlight-card { border-radius:8px; padding:16px; border:1px solid; }
.status-help-content .highlight-card-title { font-size:14px; font-weight:600; margin-bottom:8px; }
.status-help-content .highlight-card-body { font-size:13px; color:#606266; line-height:1.6; }
.status-help-content .highlight-primary { background-color:#ecf5ff; border-color:#a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color:#409eff; }
</style>
