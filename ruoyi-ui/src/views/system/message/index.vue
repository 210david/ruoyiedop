<template>
  <div class="app-container sys-message-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>消息标题</label>
          <div class="control">
            <el-input v-model="queryParams.messageTitle" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>业务来源</label>
          <div class="control is-select">
            <el-select v-model="queryParams.bizSource" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_message_biz_source" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>消息类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.messageType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_message_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>消息级别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.messageLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_message_level" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button v-for="s in readStatusTabs" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span>
            <span>{{ s.label }}</span>
            <span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Check" :disabled="!selectedUnreadIds.length" @click="handleBatchRead">批量已读</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_message_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="messageList" border class="app-table"
          @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" :selectable="canSelect" />
          <el-table-column type="index" label="序号" width="85" align="center" resizable />
          <el-table-column label="消息ID" align="center" prop="messageId" :width="colWidth('messageId', 90)" resizable v-if="columns.messageId.visible" />
          <el-table-column label="消息标题" align="center" :show-overflow-tooltip="true" :width="colWidth('messageTitle', 200)" resizable v-if="columns.messageTitle.visible">
            <template #default="scope">
              <a class="link-type" style="cursor:pointer" @click="handleViewData(scope.row)">{{ scope.row.messageTitle }}</a>
            </template>
          </el-table-column>
          <el-table-column label="消息类型" align="center" prop="messageType" :width="colWidth('messageType', 100)" resizable v-if="columns.messageType.visible">
            <template #default="scope">
              <span class="badge violet">{{ typeLabel(scope.row.messageType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="消息级别" align="center" prop="messageLevel" :width="colWidth('messageLevel', 100)" resizable v-if="columns.messageLevel.visible">
            <template #default="scope">
              <span class="badge blue">{{ levelLabel(scope.row.messageLevel) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="业务来源" align="center" prop="bizSource" :width="colWidth('bizSource', 120)" resizable v-if="columns.bizSource.visible">
            <template #default="scope">
              <span class="badge amber">{{ bizSourceLabel(scope.row.bizSource) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="阅读状态" align="center" :width="colWidth('readStatus', 100)" resizable v-if="columns.readStatus.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isRead ? 'green' : 'gray'">
                <span class="dot"></span>{{ scope.row.isRead ? '已读' : '未读' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" :width="colWidth('createTime', 160)" resizable v-if="columns.createTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleViewData(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <!-- 消息详情弹窗 -->
    <message-detail-view ref="messageViewRef" @read="onMessageRead" />
  </div>
</template>

<script setup name="Message">
import MessageDetailView from "./DetailView"
import { listMessage, markMessageReadAll } from "@/api/system/message"
import { useMessageStore } from '@/store/modules/message'
import { useColumnResize } from '@/composables/useColumnResize'
import { Filter, ArrowDown } from '@element-plus/icons-vue'

const messageStore = useMessageStore()

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('sys_message_index')

const { proxy } = getCurrentInstance()
const { sys_message_type, sys_message_level, sys_message_biz_source } = useDict("sys_message_type", "sys_message_level", "sys_message_biz_source")

const messageList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const dateRange = ref([])
const activeStatusTab = ref('0')
const statusCounts = ref({})
const selectedRows = ref([])

// 阅读状态页签（只有未读和已读，没有全部）
const readStatusTabs = [
  { label: '未读', value: '0' },
  { label: '已读', value: '1' }
]

// 列显隐配置
const defaultColumns = {
  messageId: { label: '消息ID', visible: true },
  messageTitle: { label: '消息标题', visible: true },
  messageType: { label: '消息类型', visible: true },
  messageLevel: { label: '消息级别', visible: true },
  bizSource: { label: '业务来源', visible: true },
  readStatus: { label: '阅读状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('sys_message_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    messageTitle: undefined,
    messageType: undefined,
    messageLevel: undefined,
    bizSource: undefined,
    readStatus: '0'
  }
})

const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.messageTitle) count++
  if (queryParams.value.bizSource) count++
  if (queryParams.value.messageType) count++
  if (queryParams.value.messageLevel) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

// 选中的未读消息ID
const selectedUnreadIds = computed(() => {
  return selectedRows.value.filter(r => !r.isRead).map(r => r.messageId)
})

/** 状态页签样式映射 */
function statusTabClass(value) {
  const map = { '0': 'tab-unread', '1': 'tab-read' }
  return map[value] || ''
}

/** 字典label查询函数 */
function typeLabel(value) {
  const item = sys_message_type.value.find(d => d.value == value)
  return item ? item.label : '-'
}
function levelLabel(value) {
  const item = sys_message_level.value.find(d => d.value == value)
  return item ? item.label : '-'
}
function bizSourceLabel(value) {
  if (!value) return '-'
  const item = sys_message_biz_source.value.find(d => d.value == value)
  return item ? item.label : value
}

/** 只有未读消息可以勾选 */
function canSelect(row) {
  return !row.isRead
}

/** 查询消息列表 */
function getList() {
  loading.value = true
  listMessage(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    messageList.value = response.rows
    total.value = response.total
    loading.value = false
    nextTick(() => { applySavedWidths() })
    loadStatusCounts()
  }).catch(() => {
    loading.value = false
  })
}

/** 加载状态页签数量统计 */
function loadStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 }
  if (queryParams.value.messageTitle) baseQuery.messageTitle = queryParams.value.messageTitle
  if (queryParams.value.messageType) baseQuery.messageType = queryParams.value.messageType
  if (queryParams.value.messageLevel) baseQuery.messageLevel = queryParams.value.messageLevel
  if (queryParams.value.bizSource) baseQuery.bizSource = queryParams.value.bizSource
  // 查未读数量
  baseQuery.readStatus = '0'
  listMessage(proxy.addDateRange(baseQuery, dateRange.value)).then(res => {
    const counts = { '0': res.total }
    // 查已读数量
    baseQuery.readStatus = '1'
    listMessage(proxy.addDateRange(baseQuery, dateRange.value)).then(res2 => {
      counts['1'] = res2.total
      statusCounts.value = counts
    })
  }).catch(() => {})
}

/** 搜索按钮操作 */
function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  queryParams.value.messageTitle = undefined
  queryParams.value.bizSource = undefined
  queryParams.value.messageType = undefined
  queryParams.value.messageLevel = undefined
  queryParams.value.params = {}
  handleQuery()
}

/** 状态页签切换 */
function handleStatusTabClick(value) {
  activeStatusTab.value = value
  queryParams.value.readStatus = value
  queryParams.value.pageNum = 1
  getList()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  selectedRows.value = selection
}

/** 批量已读 */
function handleBatchRead() {
  const ids = selectedUnreadIds.value
  if (!ids.length) return
  messageStore.markReadAll(ids.join(',')).then(() => {
    proxy.$modal.msgSuccess('标记成功')
    getList()
  })
}

/** 查看消息详情（查看后自动标记为已读） */
function handleViewData(row) {
  proxy.$refs["messageViewRef"].open(row)
}

/** 详情弹窗标记已读后的回调 */
function onMessageRead(messageId) {
  // 通知全局 store 刷新铃铛未读数（乐观更新已在 store 中处理）
  messageStore.markRead(messageId)
  // 立即更新当前列表中的对应行
  const item = messageList.value.find(m => m.messageId === messageId)
  if (item) {
    item.isRead = true
  }
  // 刷新页签计数
  loadStatusCounts()
  // 如果当前在未读页签，延迟刷新列表让该消息消失
  if (activeStatusTab.value === '0') {
    setTimeout(() => getList(), 300)
  }
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-message-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff; --violet-500:#8b5cf6;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.sys-message-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-message-page .filter-card { padding:14px 20px 16px; }
.sys-message-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-message-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-message-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-message-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.sys-message-page .filter-card .adv-link:hover { color:var(--brand-600); }
.sys-message-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.sys-message-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.sys-message-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-message-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-message-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-message-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-message-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-message-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-message-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-message-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

.sys-message-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-message-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-message-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-message-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-message-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

.sys-message-page .field .control :deep(.el-select) { width:100%; }
.sys-message-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-message-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-message-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

.sys-message-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-message-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-message-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-message-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Status Tabs ===== */
.sys-message-page .status-tabs {
  display: flex; align-items: center; gap: 12px;
  padding: 6px 10px 6px 12px;
  border-bottom: 1px solid var(--ink-200);
  background: #fff;
}
.sys-message-page .tabs-track {
  display: flex; align-items: center; gap: 4px;
  flex: 1; min-width: 0; overflow-x: auto;
  scrollbar-width: none;
}
.sys-message-page .tabs-track::-webkit-scrollbar { display: none; }
.sys-message-page .status-tab {
  display: inline-flex; align-items: center; gap: 6px;
  height: 32px; padding: 0 12px; border-radius: var(--r-sm);
  font-size: 14px; color: var(--ink-500); cursor: pointer;
  user-select: none; transition: all .15s var(--ease-out);
  white-space: nowrap; border: 1px solid transparent; background: transparent;
}
.sys-message-page .status-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--ink-300); }
.sys-message-page .status-tab .count {
  font-size: 12px; font-weight: 600; padding: 1px 6px;
  border-radius: 999px; background: var(--ink-100); color: var(--ink-500);
  min-width: 18px; text-align: center; line-height: 1.4; font-feature-settings: "tnum" 1;
}
.sys-message-page .status-tab:hover { background: var(--ink-50); color: var(--ink-700); }
.sys-message-page .status-tab.is-active {
  background: var(--brand-50); color: var(--brand-700);
  font-weight: 600; border-color: var(--brand-200);
}
.sys-message-page .status-tab.is-active .count { background: var(--brand-600); color: #fff; }
.sys-message-page .status-tab.is-active .dot { background: var(--brand-500); }

/* 未读页签颜色 */
.sys-message-page .status-tab.tab-unread .dot { background: var(--amber-500); }
.sys-message-page .status-tab.tab-unread .count { background: var(--amber-50); color: var(--amber-700); }
.sys-message-page .status-tab.is-active.tab-unread .count { background: var(--amber-500); color: #fff; }

/* 已读页签颜色 */
.sys-message-page .status-tab.tab-read .dot { background: var(--green-500); }
.sys-message-page .status-tab.tab-read .count { background: var(--green-50); color: var(--green-700); }
.sys-message-page .status-tab.is-active.tab-read .count { background: var(--green-500); color: #fff; }

/* ===== Toolbar ===== */
.sys-message-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-message-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-message-page .toolbar .right { display:flex; gap:8px; align-items:center; }

/* ===== Table ===== */
.sys-message-page .table-wrap { overflow-x:auto; }
.sys-message-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-message-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-message-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-message-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-message-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-message-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-message-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-message-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-message-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-message-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Badge ===== */
.sys-message-page .badge {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 3px 9px; border-radius: 999px;
  font-size: 13px; font-weight: 600; line-height: 1;
  border: 1px solid transparent; white-space: nowrap;
}
.sys-message-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.sys-message-page .badge.violet { background: var(--violet-50); color: var(--brand-700); border-color: var(--brand-200); }
.sys-message-page .badge.blue { background: var(--blue-50); color: var(--blue-700); border-color: #bfdbfe; }
.sys-message-page .badge.amber { background: var(--amber-50); color: var(--amber-700); border-color: #fde68a; }
.sys-message-page .badge.green { background: var(--green-50); color: var(--green-700); border-color: #a7f3d0; }
.sys-message-page .badge.green .dot { background: var(--green-500); }
.sys-message-page .badge.gray { background: var(--ink-100); color: var(--ink-500); border-color: var(--ink-200); }
.sys-message-page .badge.gray .dot { background: var(--ink-400); }

/* ===== Pagination ===== */
.sys-message-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-message-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-message-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-message-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-message-page .pagination-container :deep(.el-pagination .btn-prev), .sys-message-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-message-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-message-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-message-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-message-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-message-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-message-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
