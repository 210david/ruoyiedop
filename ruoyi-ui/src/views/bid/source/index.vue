<template>
  <div class="app-container bid-source-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>采集源名称</label><div class="control"><el-input v-model="queryParams.sourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>类型</label><div class="control"><el-select v-model="queryParams.sourceType" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_source_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>启用状态</label><div class="control"><el-select v-model="queryParams.enabled" placeholder="全部" clearable style="width:100%"><el-option label="启用" value="0" /><el-option label="停用" value="1" /></el-select></div></div>
        <div class="field"><label>最近执行结果</label><div class="control"><el-select v-model="queryParams.lastRunStatus" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_crawl_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>最近执行时间</label><div class="control"><el-date-picker v-model="lastRunRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <div class="surface">
      <div class="toolbar">
        <div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bid:source:add']">新增采集源</el-button></div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="采集源名称" prop="sourceName" key="sourceName" :min-width="colWidth('sourceName', 200)" show-overflow-tooltip resizable />
          <el-table-column label="类型" prop="sourceType" key="sourceType" width="110" align="center">
            <template #default="scope"><span class="badge violet">{{ typeLabel(scope.row.sourceType) }}</span></template>
          </el-table-column>
          <el-table-column label="采集地址" prop="baseUrl" key="baseUrl" :min-width="colWidth('baseUrl', 260)" show-overflow-tooltip resizable />
          <el-table-column label="抓取间隔" prop="crawlInterval" key="crawlInterval" width="100" align="center">
            <template #default="scope">{{ scope.row.crawlInterval }}分钟</template>
          </el-table-column>
          <el-table-column label="启用" prop="enabled" key="enabled" width="90" align="center">
            <template #default="scope"><span class="badge" :class="scope.row.enabled === '0' ? 'green' : 'gray'">{{ scope.row.enabled === '0' ? '启用' : '停用' }}</span></template>
          </el-table-column>
          <el-table-column label="最近执行时间" prop="lastRunTime" key="lastRunTime" width="170" align="center" />
          <el-table-column label="执行结果" prop="lastRunStatus" key="lastRunStatus" width="100" align="center">
            <template #default="scope"><span class="badge" :class="statusBadge(scope.row.lastRunStatus)">{{ statusLabel(scope.row.lastRunStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="VideoPlay" @click="handleRun(scope.row)" v-hasPermi="['bid:source:run']">执行</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['bid:source:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.enabled !== '0'" v-hasPermi="['bid:source:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="sourceRef" :model="form" :rules="rules" label-width="110px">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="名称" prop="sourceName"><el-input v-model="form.sourceName" placeholder="如：曲靖市公共资源交易中心" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="类型" prop="sourceType">
                <el-radio-group v-model="form.sourceType">
                  <el-radio v-for="d in bid_source_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                </el-radio-group>
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="采集地址" prop="baseUrl"><el-input v-model="form.baseUrl" placeholder="列表页或接口地址" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="解析器" prop="parserCode">
                <el-select v-model="form.parserCode" placeholder="请选择" style="width:100%" @change="onParserChange">
                  <el-option label="common-html（HTML页面）" value="common-html" />
                  <el-option label="common-json（数据接口）" value="common-json" />
                  <el-option label="rss（RSS订阅）" value="rss" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="抓取间隔(分)" prop="crawlInterval">
                <el-input-number v-model="form.crawlInterval" :min="30" :step="30" style="width:100%" />
                <div class="form-tip">最小 30 分钟，请控制请求频率（合规要求）</div>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="启用" prop="enabled">
                <el-radio-group v-model="form.enabled"><el-radio value="0">启用</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
              </el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg></span>解析配置（parse_config JSON）</div></div>
          <div class="rd-card-body" style="display:block">
            <el-form-item label-width="0" prop="parseConfig">
              <el-input v-model="form.parseConfig" type="textarea" :rows="8" class="config-editor" :placeholder="configPlaceholder" @blur="validateConfig" />
              <div class="form-tip" :class="{ 'is-error': configError }">{{ configError || '站点改版只需修改此配置，无需改代码' }}</div>
            </el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="BidSource">
import { listSource, getSource, addSource, updateSource, delSource, runSource } from '@/api/bid/source'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { bid_source_type, bid_crawl_status } = proxy.useDict('bid_source_type', 'bid_crawl_status')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bid_source_index')

const loading = ref(true)
const list = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const showSearch = ref(true)
const lastRunRange = ref([])
const configError = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, sourceName: undefined, sourceType: undefined, enabled: undefined, lastRunStatus: undefined, params: {} },
  rules: {
    sourceName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    baseUrl: [{ required: true, message: '采集地址不能为空', trigger: 'blur' }],
    parserCode: [{ required: true, message: '解析器不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  const q = queryParams.value
  return ['sourceName','sourceType','enabled','lastRunStatus'].filter(k => q[k]).length + (lastRunRange.value.length ? 1 : 0)
})

const configPlaceholder = computed(() => {
  if (form.value.parserCode === 'common-json') {
    return '{\n  "dataPath": "data.rows",\n  "fieldMap": { "title": "name", "url": "detailUrl", "date": "publishDate" },\n  "pageParam": "pageNo", "pageSize": 20, "maxPage": 1\n}'
  }
  if (form.value.parserCode === 'common-html') {
    return '{\n  "listSelector": "ul.news-list li",\n  "fields": { "title": "a.title", "url": "a.title@href", "date": "span.date", "budget": "span.money" },\n  "pageParam": "page", "pageStart": 1, "maxPage": 1, "absoluteUrl": true\n}'
  }
  return 'RSS 订阅无需解析配置，可留空'
})

function typeLabel(v) { const d = bid_source_type.value.find(x => x.value == v); return d ? d.label : '—' }
function statusBadge(v) { return { '0': 'green', '1': 'amber', '2': 'red' }[v] || 'gray' }
function statusLabel(v) { const d = bid_crawl_status.value.find(x => x.value == v); return d ? d.label : '—' }

function getList() {
  loading.value = true
  listSource(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, lastRunRange.value, 'LastRunTime'); getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; lastRunRange.value = []; handleQuery() }

function reset() {
  form.value = { sourceId: undefined, sourceName: undefined, sourceType: '1', baseUrl: undefined, parserCode: 'common-html', parseConfig: undefined, crawlInterval: 120, enabled: '0' }
  proxy.resetForm('sourceRef')
  configError.value = ''
}
function handleAdd() { reset(); open.value = true; title.value = '新增采集源' }
function handleUpdate(row) {
  reset()
  getSource(row.sourceId).then(res => { form.value = res.data; open.value = true; title.value = '修改采集源' })
}
function onParserChange() { configError.value = '' }
function validateConfig() {
  configError.value = ''
  if (form.value.parserCode !== 'rss' && form.value.parseConfig) {
    try { JSON.parse(form.value.parseConfig) } catch (e) { configError.value = '解析配置不是合法的 JSON：' + e.message }
  }
}
function submitForm() {
  validateConfig()
  if (configError.value) { proxy.$modal.msgError(configError.value); return }
  proxy.$refs['sourceRef'].validate(v => {
    if (v) {
      if (form.value.sourceId != null) {
        updateSource(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addSource(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function cancel() { open.value = false; reset() }
function handleDelete(row) {
  proxy.$modal.confirm('确认删除采集源「' + row.sourceName + '」？').then(() => delSource(row.sourceId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleRun(row) {
  proxy.$modal.confirm('立即执行采集源「' + row.sourceName + '」一次？').then(() => runSource(row.sourceId)).then(() => {
    proxy.$modal.msgSuccess('执行完成'); getList()
  }).catch(() => {})
}
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.bid-source-page { padding-top:10px; --brand-50:#eef2ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.bid-source-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.bid-source-page .filter-head { display:flex; align-items:center; padding:12px 20px 0; }
.bid-source-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; }
.bid-source-page .filter-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-500); }
.bid-source-page .filter-bar { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:12px 16px; padding:12px 20px; }
.bid-source-page .field label { display:block; font-size:12px; color:var(--ink-500); margin-bottom:4px; }
.bid-source-page .filter-actions { display:flex; align-items:center; justify-content:space-between; padding:0 20px 14px; }
.bid-source-page .filter-info { display:flex; align-items:center; gap:6px; font-size:12px; color:var(--ink-400); }
.bid-source-page .filter-buttons { display:flex; gap:8px; }
.bid-source-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 12px; }
.bid-source-page .toolbar .left { display:flex; gap:8px; }
.bid-source-page .pagination-container { padding:10px 12px; }
.bid-source-page .badge { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.bid-source-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.bid-source-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.bid-source-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.bid-source-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.bid-source-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
:deep(.col-action) { padding:6px 4px !important; }
:deep(.col-action .cell) { display:flex; justify-content:center; padding:0; }
.action-btn-row { display:inline-flex; flex-wrap:wrap; justify-content:center; gap:0; }
:deep(.col-action .el-button) { padding:2px 4px; margin:0 2px; white-space:nowrap; justify-content:center; }
:deep(.col-action .el-button + .el-button) { margin-left:2px; }
.config-editor :deep(textarea) { font-family: Consolas, Monaco, monospace; font-size: 13px; }
.form-tip { font-size: 12px; color: var(--ink-400); margin-top: 4px; }
.form-tip.is-error { color: var(--red-500); }
</style>
