<template>
  <div class="app-container sys-message-page">
    <!-- Page Head -->
    <div class="page-head">
      <div class="page-head-left">
        <h1>消息中心</h1>
        <div class="subtitle">
          <span>集中处理审批提醒、业务通知、系统消息</span>
          <span class="sep">·</span>
          <span>由系统智能排序，最近消息优先</span>
        </div>
      </div>
      <div class="page-head-right" v-if="unreadTotal > 0">
        <span class="unread-pill">
          <span class="dot"></span>
          <span>{{ unreadTotal }} 条未读</span>
        </span>
      </div>
    </div>

    <!-- Inbox Layout -->
    <div class="inbox">
      <!-- ─── 左侧：分类导航 ─── -->
      <aside class="surface side">
        <div class="side-head">
          <div class="side-label">分类</div>
          <div class="side-search">
            <el-icon class="search-icon"><Search /></el-icon>
            <input v-model="queryParams.messageTitle" placeholder="搜索消息标题"
                   @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="cat-list">
          <div v-for="cat in categories" :key="cat.key"
            class="cat" :class="[{ 'is-active': activeCategory === cat.key, 'has-hot': cat.hot }]"
            @click="handleCategoryClick(cat.key)">
            <span class="glyph" :style="cat.glyphStyle"><el-icon><component :is="cat.icon" /></el-icon></span>
            <span class="name">{{ cat.label }}</span>
            <span class="count" v-if="cat.count !== undefined">{{ cat.count }}</span>
          </div>
          <div class="cat-divider"></div>
          <div class="cat" :class="{ 'is-active': activeCategory === 'read' }"
               @click="handleCategoryClick('read')">
            <span class="glyph glyph-gray"><el-icon><DocumentChecked /></el-icon></span>
            <span class="name">已读消息</span>
          </div>
        </div>
        <div class="side-foot">
          <button class="side-action-btn" @click="showSettings = true">
            <el-icon><Setting /></el-icon>
            <span>通知设置</span>
          </button>
        </div>
      </aside>

      <!-- ─── 右侧：消息列表 ─── -->
      <section class="surface main">
        <!-- Toolbar -->
        <div class="main-toolbar">
          <div class="seg">
            <button v-for="s in readStatusTabs" :key="s.value"
              :class="{ 'is-active': activeStatusTab === s.value }"
              @click="handleStatusTabClick(s.value)">
              {{ s.label }}
              <span class="seg-count" :class="s.value === '0' ? 'seg-unread' : ''">{{ statusCounts[s.value] || 0 }}</span>
            </button>
          </div>
          <div class="actions">
            <button class="btn-soft is-outline" :disabled="!hasUnread || batchReading" @click="handleBatchReadAll">
              <el-icon><Check /></el-icon> 标记已读
            </button>
            <button class="btn-soft is-outline" @click="showSearch = !showSearch">
              <el-icon><Filter /></el-icon> 筛选
            </button>
          </div>
        </div>

        <!-- Filter Panel (collapsible) -->
        <transition name="filter-slide">
          <div class="filter-panel" v-show="showSearch">
            <div class="filter-row">
              <div class="field">
                <label>业务来源</label>
                <el-select v-model="queryParams.bizSource" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="dict in sys_message_biz_source" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </div>
              <div class="field">
                <label>消息类型</label>
                <el-select v-model="queryParams.messageType" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="dict in sys_message_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </div>
              <div class="field">
                <label>消息级别</label>
                <el-select v-model="queryParams.messageLevel" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="dict in sys_message_level" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </div>
              <div class="field">
                <label>通知时间</label>
                <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange"
                  range-separator="-" start-placeholder="开始" end-placeholder="结束" style="width: 100%" />
              </div>
            </div>
            <div class="filter-footer">
              <span class="filter-info">
                <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件
              </span>
              <div class="filter-buttons">
                <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              </div>
            </div>
          </div>
        </transition>

        <!-- Message Cards -->
        <div class="msg-list" v-loading="loading">
          <div v-if="messageList.length === 0 && !loading" class="empty">
            <div class="empty-pic"><el-icon><MessageBox /></el-icon></div>
            <div class="empty-title">暂无消息</div>
            <div class="empty-hint">当前分类下没有消息记录</div>
          </div>

          <article v-for="msg in messageList" :key="msg.messageId"
            class="msg" :class="[msgKindClass(msg), { 'is-unread': !msg.isRead && msg.bizHandled != '1' && msg.bizHandled !== true }]"
            @click="handleViewData(msg)">
            <span class="accent"></span>
            <div class="msg-inner">
              <div class="ic"><el-icon><component :is="msgIcon(msg)" /></el-icon></div>
              <div class="body">
                <div class="meta">
                  <span class="tag">{{ typeLabel(msg.messageType) }}</span>
                  <span v-if="msg.bizSource" class="biz-source">{{ bizSourceLabel(msg.bizSource) }}</span>
                  <span class="dot-sep" v-if="msg.bizSource"></span>
                  <span v-if="msg.bizId" class="biz-id">单据 #{{ msg.bizId }}</span>
                  <span class="dot-sep" v-if="msg.bizEntryName"></span>
                  <span v-if="msg.bizEntryName" class="biz-entry">{{ msg.bizEntryName }}</span>
                </div>
                <div class="title" :class="{ 'is-handled': msg.bizHandled == '1' || msg.bizHandled === true }">
                  {{ msg.messageTitle }}
                </div>
                <div class="desc">
                  <span v-if="msg.bizHandled == '1' || msg.bizHandled === true" class="handled-tag">已处理</span>
                  <span v-if="msg.bizHandled == '1' || msg.bizHandled === true" class="desc-sep">·</span>
                  <span v-if="msg.messageContent" class="desc-text" v-html="highlightContent(msg.messageContent)"></span>
                  <span class="level-badge" :class="levelClass(msg.messageLevel)">{{ levelLabel(msg.messageLevel) }}</span>
                </div>
              </div>
              <div class="side-actions">
                <span class="time">{{ formatTime(msg.createTime) }}</span>
                <div class="row-actions">
                  <button v-if="msg.redirectUrl" class="is-primary" @click.stop="handleViewData(msg)">
                    <el-icon><View /></el-icon> 查看
                  </button>
                  <button v-else @click.stop="handleViewData(msg)">
                    <el-icon><View /></el-icon> 详情
                  </button>
                  <button v-if="!msg.isRead && msg.bizHandled != '1' && msg.bizHandled !== true"
                    class="is-ghost" title="标为已读" @click.stop="handleMarkRead(msg)">
                    <el-icon><Check /></el-icon>
                  </button>
                </div>
              </div>
            </div>
          </article>
        </div>

        <!-- Pagination -->
        <div class="pager" v-show="total > 0">
          <pagination
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
          />
        </div>
      </section>
    </div>

    <!-- 消息详情弹窗 -->
    <message-detail-view ref="messageViewRef" @read="onMessageRead" />

    <!-- 通知设置弹窗：卡片式设计，读写走后端（按用户隔离），控制顶部铃铛提醒类型 -->
    <el-dialog v-model="showSettings" width="540px" append-to-body class="settings-dialog">
      <template #header>
        <div class="set-head">
          <span class="set-head-icon"><el-icon><Setting /></el-icon></span>
          <span class="set-head-title">通知设置</span>
          <span class="set-head-sub">关闭后，该类消息不再出现在顶部铃铛提醒与消息中心列表中（历史记录不删除）</span>
        </div>
      </template>
      <div class="settings-body" v-loading="settingsLoading">
        <div class="setting-row" v-for="item in settingItems" :key="item.key">
          <span class="setting-glyph" :style="item.glyphStyle"><el-icon><component :is="item.icon" /></el-icon></span>
          <div class="setting-info">
            <div class="setting-name">{{ item.name }}<span v-if="item.count > 0" class="setting-count">{{ item.count }} 条未读</span></div>
            <div class="setting-desc">{{ item.desc }}</div>
          </div>
          <el-switch v-model="item.enabled" :disabled="settingsLoading" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showSettings = false">取 消</el-button>
        <el-button type="primary" :loading="settingsSaving" @click="handleSaveSettings">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Message">
import MessageDetailView from "./DetailView"
import { listMessage, getNotifySetting, saveNotifySetting } from "@/api/system/message"
import { useMessageStore } from '@/store/modules/message'
import { Filter, Search, Check, View, DocumentChecked, MessageBox,
  WarningFilled, Bell, Tickets, Files, Memo, Setting } from '@element-plus/icons-vue'
import { parseTime } from '@/utils/ruoyi'

const messageStore = useMessageStore()

const { proxy } = getCurrentInstance()
const { sys_message_type, sys_message_level, sys_message_biz_source } = useDict("sys_message_type", "sys_message_level", "sys_message_biz_source")

// 通知设置（按用户落库）：关闭的消息类型在铃铛与消息中心列表同步隐藏
const notifyTypes = ref(['1', '2', '3', '4']) // 默认全放行，onMounted 后按设置刷新

/** 加载通知设置 → 刷新放行类型（铃铛与列表共用） */
function loadNotifySetting() {
  getNotifySetting().then(res => {
    const d = res.data || {}
    const types = []
    if ((d.notifySystem ?? '1') !== '0') types.push('1')
    if ((d.notifyAlert ?? '1') !== '0') types.push('2')
    if ((d.notifyAudit ?? '1') !== '0') types.push('3')
    if ((d.notifyTodo ?? '1') !== '0') types.push('4')
    notifyTypes.value = types
    // 若当前激活的分类被关闭，回到"全部"
    if (activeCategory.value !== 'all' && activeCategory.value !== 'read' && !types.includes(activeCategory.value)) {
      activeCategory.value = 'all'
    }
    getList()
  }).catch(() => {})
}

const messageList = ref([])
const loading = ref(true)
const showSearch = ref(false)
const showSettings = ref(false)
const total = ref(0)
const dateRange = ref([])
const activeStatusTab = ref('0')
const statusCounts = ref({})
const activeCategory = ref('all')
const unreadTotal = ref(0)
const batchReading = ref(false)

// 通知设置项：与左侧分类同图标同配色；开关落库（sys_message_setting），控制顶部铃铛提醒
const settingsLoading = ref(false)
const settingsSaving = ref(false)
const settingItems = ref([
  { key: 'notifySystem', type: '1', name: '系统通知', desc: '系统升级、维护等重要通知', enabled: true, icon: Bell, glyphStyle: 'background:#ef4444; color:#fff;' },
  { key: 'notifyAlert', type: '2', name: '业务预警', desc: '库存不足、订单异常等预警消息', enabled: true, icon: WarningFilled, glyphStyle: 'background:#f59e0b; color:#fff;' },
  { key: 'notifyAudit', type: '3', name: '审批消息', desc: '待审批、审批结果等消息提醒', enabled: true, icon: Memo, glyphStyle: 'background:#10b981; color:#fff;' },
  { key: 'notifyTodo', type: '4', name: '待办事项', desc: '任务分配、到期提醒等待办消息', enabled: true, icon: Tickets, glyphStyle: 'background:#3b82f6; color:#fff;' },
])
// 注意：无"邮件通知"项 —— 平台无邮件推送能力，虚假开关已于 2026-09-19 移除

/** 打开设置弹窗：加载本人设置（落库）+ 各类型未读数 */
watch(showSettings, (v) => {
  if (!v) return
  settingsLoading.value = true
  Promise.all([
    getNotifySetting(),
    listMessage({ pageNum: 1, pageSize: 1, readStatus: '0', messageType: '1' }),
    listMessage({ pageNum: 1, pageSize: 1, readStatus: '0', messageType: '2' }),
    listMessage({ pageNum: 1, pageSize: 1, readStatus: '0', messageType: '3' }),
    listMessage({ pageNum: 1, pageSize: 1, readStatus: '0', messageType: '4' })
  ]).then(([setting, t1, t2, t3, t4]) => {
    const d = setting.data || {}
    const counts = { '1': t1.total, '2': t2.total, '3': t3.total, '4': t4.total }
    settingItems.value.forEach(item => {
      item.enabled = (d[item.key] ?? '1') !== '0'
      item.count = counts[item.type] || 0
    })
  }).finally(() => { settingsLoading.value = false })
})

/** 保存通知设置：落库后刷新顶部铃铛与消息列表 */
function handleSaveSettings() {
  settingsSaving.value = true
  const body = {}
  settingItems.value.forEach(item => { body[item.key] = item.enabled ? '1' : '0' })
  saveNotifySetting(body).then(() => {
    showSettings.value = false
    proxy.$modal.msgSuccess('通知设置已保存，铃铛与消息列表即时生效')
    loadNotifySetting()
    messageStore.loadNoticeTop()
  }).finally(() => { settingsSaving.value = false })
}

// 阅读状态页签
const readStatusTabs = [
  { label: '未读', value: '0' },
  { label: '已读', value: '1' }
]

// 分类导航 — 从后端独立查询各类型未读数，不依赖当前页数据；按通知设置隐藏已关闭类型
const catCounts = ref({})
const categories = computed(() => [
  { key: 'all', label: '全部消息', icon: Files, glyphStyle: 'background:#64748b; color:#fff;', count: catCounts.value.total || 0 },
  { key: '1', label: '系统通知', icon: Bell, glyphStyle: 'background:#ef4444; color:#fff;', count: catCounts.value['1'] || 0, hot: (catCounts.value['1'] || 0) > 0 },
  { key: '2', label: '业务预警', icon: WarningFilled, glyphStyle: 'background:#f59e0b; color:#fff;', count: catCounts.value['2'] || 0, hot: (catCounts.value['2'] || 0) > 0 },
  { key: '3', label: '审批消息', icon: Memo, glyphStyle: 'background:#10b981; color:#fff;', count: catCounts.value['3'] || 0, hot: (catCounts.value['3'] || 0) > 0 },
  { key: '4', label: '待办事项', icon: Tickets, glyphStyle: 'background:#3b82f6; color:#fff;', count: catCounts.value['4'] || 0, hot: (catCounts.value['4'] || 0) > 0 },
].filter(cat => cat.key === 'all' || notifyTypes.value.includes(cat.key)))

const hasUnread = computed(() => (catCounts.value.total || 0) > 0)

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

/** 消息类型对应的图标 */
function msgIcon(msg) {
  const map = { '1': Bell, '2': WarningFilled, '3': Memo, '4': Tickets }
  return map[msg.messageType] || MessageBox
}

/** 消息类型对应的数据属性 (用于配色) */
function msgKindClass(msg) {
  const map = { '1': 'kind-system', '2': 'kind-alert', '3': 'kind-audit', '4': 'kind-todo' }
  return map[msg.messageType] || 'kind-system'
}

/** 消息内容关键字段高亮：单号/编号→hl-mono；金额/日期/数量→hl-em（先转义保安全） */
function highlightContent(text) {
  if (!text) return ''
  const esc = String(text).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  return esc
    // 单号/编号（字母开头编号，含多段连字符）：EQ202609160003、WO-202609010047、CAPA-2026090105
    .replace(/\b([A-Z]{2,}[-]?[A-Z0-9][A-Z0-9-]{3,})\b/g, '<span class="hl-mono">$1</span>')
    // 日期时间：2026-09-19、2026/09/19 14:30
    .replace(/\d{4}[-/]\d{1,2}[-/]\d{1,2}(\s+\d{1,2}:\d{2}(:\d{2})?)?/g, '<span class="hl-em">$&</span>')
    // 金额：¥123.45 / 1,234.56元 / 3万元
    .replace(/(¥|￥)?\d[\d,]*(\.\d{1,2})?(元|万元)/g, '<span class="hl-em">$&</span>')
    // 关键对象名：「」引号内容（计划名/客户名/设备名）
    .replace(/「([^」]{2,30})」/g, '「<span class="hl-em">$1</span>」')
    // 冒号枚举值：设备：数控加工中心 / 故障类型：PM维护 / 优先级：低
    .replace(/(设备|故障类型|优先级|客户|订单号|工单号|批次号|规格|型号|数量|状态|原因|类型|等级)[:：]\s*([^，。、；\s]{2,20})/g,
      '$1: <span class="hl-em">$2</span>')
}
function typeLabel(value) {
  const item = sys_message_type.value.find(d => d.value == value)
  return item ? item.label : '消息'
}
function levelLabel(value) {
  const item = sys_message_level.value.find(d => d.value == value)
  return item ? item.label : '普通'
}
function levelClass(value) {
  const map = { '1': 'level-normal', '2': 'level-important', '3': 'level-urgent' }
  return map[value] || 'level-normal'
}
function bizSourceLabel(value) {
  if (!value) return '-'
  const item = sys_message_biz_source.value.find(d => d.value == value)
  return item ? item.label : value
}

/** 格式化时间 */
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 7) return `${days} 天前`
  return parseTime(time, '{y}-{m}-{d}')
}

/** 分类点击 */
function handleCategoryClick(key) {
  activeCategory.value = key
  if (key === 'all') {
    queryParams.value.messageType = undefined
    queryParams.value.readStatus = '0'
    activeStatusTab.value = '0'
  } else if (key === 'read') {
    queryParams.value.messageType = undefined
    queryParams.value.readStatus = '1'
    activeStatusTab.value = '1'
  } else {
    queryParams.value.messageType = key
    queryParams.value.readStatus = '0'
    activeStatusTab.value = '0'
  }
  queryParams.value.pageNum = 1
  getList()
}

/** 查询消息列表 */
function getList() {
  loading.value = true
  const params = { ...proxy.addDateRange(queryParams.value, dateRange.value) }
  // 通知设置放行类型过滤（铃铛与列表同步生效）
  if (notifyTypes.value.length < 4) {
    params.messageTypes = notifyTypes.value.join(',')
  }
  listMessage(params).then(response => {
    messageList.value = response.rows
    total.value = response.total
    loading.value = false
    loadStatusCounts()
  }).catch(() => {
    loading.value = false
  })
}

/** 加载状态页签数量统计 + 各分类未读数 */
function loadStatusCounts() {
  // 全局基类查询（不含 messageType 和 readStatus）
  const globalBase = { pageNum: 1, pageSize: 1 }
  if (queryParams.value.messageTitle) globalBase.messageTitle = queryParams.value.messageTitle
  if (queryParams.value.messageLevel) globalBase.messageLevel = queryParams.value.messageLevel
  if (queryParams.value.bizSource) globalBase.bizSource = queryParams.value.bizSource

  // 当前分类下的已读/未读统计（带 messageType 筛选）
  const currentBase = { ...globalBase }
  if (activeCategory.value !== 'all' && activeCategory.value !== 'read' && activeCategory.value) {
    currentBase.messageType = activeCategory.value
  }
  const currentUnreadQuery = { ...currentBase, readStatus: '0' }
  const currentReadQuery = { ...currentBase, readStatus: '1' }

  // 全局各类型未读数（不受当前 messageType 筛选影响）
  const typeQueries = ['1', '2', '3', '4'].map(t =>
    listMessage(proxy.addDateRange({ ...globalBase, messageType: t, readStatus: '0' }, dateRange.value))
  )

  Promise.all([
    listMessage(proxy.addDateRange(currentUnreadQuery, dateRange.value)),
    listMessage(proxy.addDateRange(currentReadQuery, dateRange.value)),
    ...typeQueries
  ]).then(([res1, res2, t1, t2, t3, t4]) => {
    // 顶部已读/未读统计 — 反映当前分类筛选
    statusCounts.value = { '0': res1.total, '1': res2.total }
    // 左侧各分类未读数 — 全局独立统计
    catCounts.value = {
      total: (t1.total || 0) + (t2.total || 0) + (t3.total || 0) + (t4.total || 0),
      '1': t1.total,
      '2': t2.total,
      '3': t3.total,
      '4': t4.total
    }
    unreadTotal.value = catCounts.value.total
    // "全部"计数按放行类型求和（隐藏类型不参与），避免与列表 total 不一致
    catCounts.value.total = ['1', '2', '3', '4']
      .filter(t => notifyTypes.value.includes(t))
      .reduce((sum, t) => sum + (catCounts.value[t] || 0), 0)
  }).catch(() => {})
}

/** 搜索 */
function handleQuery() {
  showSearch.value = false
  queryParams.value.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  dateRange.value = []
  queryParams.value.messageTitle = undefined
  queryParams.value.bizSource = undefined
  queryParams.value.messageType = undefined
  queryParams.value.messageLevel = undefined
  queryParams.value.params = {}
  activeCategory.value = 'all'
  handleQuery()
}

/** 状态页签切换 */
function handleStatusTabClick(value) {
  activeStatusTab.value = value
  queryParams.value.readStatus = value
  queryParams.value.pageNum = 1
  getList()
}

/** 批量标记已读 — 仅标记当前筛选结果中的未读消息 */
function handleBatchReadAll() {
  const ids = messageList.value.filter(m => !m.isRead).map(m => m.messageId)
  if (!ids.length) {
    proxy.$modal.msgWarning('当前列表没有未读消息')
    return
  }
  batchReading.value = true
  messageStore.markReadAll(ids.join(',')).then(() => {
    proxy.$modal.msgSuccess(`已标记 ${ids.length} 条消息为已读`)
    messageStore.loadNoticeTop()
    getList()
  }).finally(() => {
    batchReading.value = false
  })
}

/** 单条标记已读 */
function handleMarkRead(row) {
  if (!row.messageId) return
  messageStore.markRead(row.messageId).then(() => {
    row.isRead = true
    loadStatusCounts()
    messageStore.loadNoticeTop()
  })
}

/** 查看消息详情 */
function handleViewData(row) {
  proxy.$refs["messageViewRef"].open(row)
}

/** 详情弹窗标记已读后的回调 */
function onMessageRead(messageId) {
  const item = messageList.value.find(m => m.messageId === messageId)
  if (item) {
    item.isRead = true
  }
  loadStatusCounts()
  messageStore.loadNoticeTop()
  if (activeStatusTab.value === '0') {
    setTimeout(() => // 初始化：先加载通知设置（决定放行类型），再查列表
loadNotifySetting(), 300)
  }
}

// 初始化：先加载通知设置（决定放行类型），再查列表
loadNotifySetting()
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
  --shadow-pop:0 12px 32px -8px rgba(15,23,42,.18), 0 2px 6px rgba(15,23,42,.06);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Page Head ===== */
.sys-message-page .page-head {
  display: flex; align-items: flex-end; justify-content: space-between;
  gap: 24px; margin-bottom: 18px;
}
.sys-message-page .page-head h1 {
  font-size: 26px; font-weight: 700; letter-spacing: -.02em;
  margin: 0 0 6px; color: var(--ink-900);
}
.sys-message-page .page-head .subtitle {
  font-size: 13px; color: var(--ink-500);
  display: flex; align-items: center; gap: 10px;
}
.sys-message-page .page-head .sep { color: var(--ink-300); }
.sys-message-page .unread-pill {
  display: inline-flex; align-items: center; gap: 6px;
  height: 32px; padding: 0 14px;
  background: var(--red-50); color: var(--red-700);
  border: 1px solid #fecaca; border-radius: 999px;
  font-size: 13px; font-weight: 600;
}
.sys-message-page .unread-pill .dot {
  width: 7px; height: 7px; border-radius: 50%; background: var(--red-500);
  box-shadow: 0 0 0 4px rgba(239,68,68,.15);
  animation: dot-pulse 2s var(--ease-out) infinite;
}
@keyframes dot-pulse {
  0%,100% { box-shadow: 0 0 0 4px rgba(239,68,68,.15); }
  50%     { box-shadow: 0 0 0 8px rgba(239,68,68,0); }
}

/* ===== Surface ===== */
.sys-message-page .surface {
  background: #fff; border: 1px solid var(--ink-200);
  border-radius: var(--r-lg); box-shadow: var(--shadow-card);
  overflow: hidden;
}

/* ===== Inbox Layout ===== */
.sys-message-page .inbox {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  align-items: flex-start;
}

/* ===== 左侧分类导航 ===== */
.sys-message-page .side {
  position: sticky;
  top: 80px;
  align-self: flex-start;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  overscroll-behavior: contain;
  /* 覆盖全局 aside 样式（index.scss 对 <aside> 的 padding/背景），分类背景才能贴近边框 */
  padding: 0;
  background: #fff;
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  line-height: normal;
}
.sys-message-page .side-head {
  padding: 14px 16px 10px;
  border-bottom: 1px solid var(--ink-200);
  background: var(--ink-50);
}
.sys-message-page .side-label {
  font-size: 11px; font-weight: 600; color: var(--ink-500);
  letter-spacing: .06em; text-transform: uppercase;
}
.sys-message-page .side-search {
  display: flex; align-items: center;
  height: 34px; padding: 0 12px; margin-top: 8px;
  background: #fff; border: 1px solid var(--ink-200);
  border-radius: var(--r-sm); transition: all .15s var(--ease-out);
}
.sys-message-page .side-search:focus-within {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 3px rgba(99,102,241,.15);
}
.sys-message-page .side-search .search-icon { color: var(--ink-400); font-size: 14px; margin-right: 6px; }
.sys-message-page .side-search input {
  border: 0; outline: 0; background: transparent;
  flex: 1; font-size: 13px; color: var(--ink-900);
}
.sys-message-page .side-search input::placeholder { color: var(--ink-400); }

.sys-message-page .cat-list { padding: 0; }
.sys-message-page .cat {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 10px; border-radius: 8px;
  margin: 0 4px; /* 左右各留4px 小间距，背景色接近贴边但不压死 */
  cursor: pointer; transition: all .12s var(--ease-out);
  position: relative; color: var(--ink-700);
}
.sys-message-page .cat:hover { background: var(--ink-50); }
.sys-message-page .cat .glyph {
  width: 30px; height: 30px; border-radius: 50%;
  display: grid; place-items: center;
  font-size: 14px; flex-shrink: 0;
  background: #e2e8f0; color: #475569;
}
.sys-message-page .cat.has-hot .glyph { box-shadow: 0 0 0 3px rgba(239,68,68,.08); }
.sys-message-page .cat .glyph.glyph-gray { background: #94a3b8; color: #fff; }
.sys-message-page .cat .name { flex: 1; font-size: 13.5px; font-weight: 500; }
.sys-message-page .cat .count {
  min-width: 22px; height: 20px; padding: 0 6px;
  border-radius: 999px;
  background: var(--ink-100); color: var(--ink-500);
  font-size: 11.5px; font-weight: 600;
  display: grid; place-items: center; line-height: 1;
}
.sys-message-page .cat.is-active {
  background: var(--brand-50); color: var(--brand-700);
}
.sys-message-page .cat.is-active .name { font-weight: 600; }
.sys-message-page .cat.is-active .count { background: var(--brand-600); color: #fff; }
.sys-message-page .cat.is-active::before {
  content: ''; position: absolute; left: -4px; top: 8px; bottom: 8px;
  width: 3px; border-radius: 2px; background: var(--brand-600);
}
.sys-message-page .cat.has-hot .count { background: var(--red-500); color: #fff; }
.sys-message-page .cat-divider { height: 1px; background: var(--ink-200); margin: 6px 0; }

/* ===== Side Footer ===== */
.sys-message-page .side-foot {
  padding: 10px 12px; border-top: 1px solid var(--ink-200);
  background: var(--ink-50);
}
.sys-message-page .side-action-btn {
  width: 100%;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  height: 34px; padding: 0 12px;
  background: #fff; border: 1px solid var(--ink-200);
  border-radius: var(--r-sm);
  font-size: 13px; font-weight: 500; color: var(--ink-700);
  cursor: pointer; user-select: none;
  transition: all .15s var(--ease-out);
}
.sys-message-page .side-action-btn:hover {
  border-color: var(--brand-500); color: var(--brand-700);
  background: var(--brand-50);
}
.sys-message-page .side-action-btn:disabled {
  opacity: .5; cursor: not-allowed;
}
.sys-message-page .side-action-btn .el-icon { font-size: 14px; }

/* ===== Notification Settings Dialog（卡片式，对齐消息中心设计语言） ===== */
:deep(.settings-dialog) { border-radius: 14px; overflow: hidden; }
:deep(.settings-dialog .el-dialog__header) { padding: 0; margin: 0; }
:deep(.settings-dialog .el-dialog__headerbtn) { top: 14px; right: 14px; }
:deep(.settings-dialog .el-dialog__body) { padding: 8px 20px 4px; }
.sys-message-page .set-head {
  display: flex; flex-wrap: wrap; align-items: center; gap: 6px 10px;
  padding: 16px 20px; background: linear-gradient(120deg, var(--brand-50), var(--ink-50));
  border-bottom: 1px solid var(--ink-100);
}
.sys-message-page .set-head-icon {
  width: 32px; height: 32px; border-radius: 9px; flex: none;
  background: var(--brand-500); color: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(99,102,241,.35);
}
.sys-message-page .set-head-title { font-size: 16px; font-weight: 700; color: var(--ink-900); }
.sys-message-page .set-head-sub { flex-basis: 100%; font-size: 12px; color: var(--ink-500); }
.sys-message-page .settings-body {
  display: flex; flex-direction: column; gap: 2px;
}
.sys-message-page .setting-row {
  display: flex; align-items: center; gap: 12px;
  padding: 13px 12px; border-radius: var(--r-md);
  transition: background .15s;
  flex-wrap: nowrap;
}
.sys-message-page .setting-row:hover { background: var(--ink-50); }
.sys-message-page .setting-glyph {
  flex: none; width: 34px; height: 34px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; font-size: 16px;
}
.sys-message-page .setting-info { flex: 1; min-width: 0; }
.sys-message-page .setting-name {
  font-size: 14px; font-weight: 600; color: var(--ink-900);
  display: flex; align-items: center; gap: 8px; margin-bottom: 3px;
  flex-wrap: nowrap;
}
.sys-message-page .setting-count {
  flex: none; font-size: 11px; font-weight: 500; color: var(--brand-600);
  background: var(--brand-50); border-radius: 9px; padding: 1px 8px;
  font-variant-numeric: tabular-nums; white-space: nowrap;
}
.sys-message-page .setting-desc { font-size: 12px; color: var(--ink-500); }

/* ===== 右侧主区域 ===== */
.sys-message-page .main { min-width: 0; }

/* ===== Toolbar ===== */
.sys-message-page .main-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; border-bottom: 1px solid var(--ink-200);
  background: #fff; flex-wrap: wrap; gap: 10px;
}
.sys-message-page .seg {
  display: inline-flex; background: var(--ink-100);
  padding: 3px; border-radius: var(--r-sm);
}
.sys-message-page .seg button {
  border: 0; background: transparent;
  height: 28px; padding: 0 14px;
  font-size: 13px; font-weight: 500; color: var(--ink-500);
  border-radius: 4px; cursor: pointer;
  transition: all .12s var(--ease-out);
  display: inline-flex; align-items: center; gap: 4px;
}
.sys-message-page .seg button:hover { color: var(--ink-700); }
.sys-message-page .seg button.is-active {
  background: #fff; color: var(--ink-900);
  box-shadow: 0 1px 2px rgba(15,23,42,.08);
  font-weight: 600;
}
.sys-message-page .seg .seg-count { font-size: 11px; font-weight: 600; }
.sys-message-page .seg .seg-unread { color: var(--red-500); }
.sys-message-page .seg button.is-active .seg-unread { color: var(--red-500); }

.sys-message-page .actions { display: flex; gap: 6px; align-items: center; }
.sys-message-page .btn-soft {
  display: inline-flex; align-items: center; gap: 6px;
  height: 30px; padding: 0 12px;
  font-size: 13px; font-weight: 500;
  border-radius: var(--r-sm);
  border: 1px solid transparent;
  cursor: pointer; user-select: none;
  transition: all .15s var(--ease-out);
  background: #fff; color: var(--ink-700);
}
.sys-message-page .btn-soft .el-icon { font-size: 14px; }
.sys-message-page .btn-soft:hover { background: var(--ink-50); }
.sys-message-page .btn-soft.is-outline { border-color: var(--ink-200); }
.sys-message-page .btn-soft.is-outline:hover { border-color: var(--ink-300); color: var(--ink-900); }
.sys-message-page .btn-soft:disabled { opacity: .5; cursor: not-allowed; }

/* ===== Filter Panel ===== */
.sys-message-page .filter-panel {
  padding: 14px 16px;
  border-bottom: 1px solid var(--ink-200);
  background: var(--ink-50);
}
.sys-message-page .filter-row {
  display: grid; grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 16px;
}
.sys-message-page .field { display: flex; flex-direction: column; gap: 6px; }
.sys-message-page .field label {
  font-size: 13px; font-weight: 500; color: var(--ink-700);
}
.sys-message-page .field :deep(.el-select) { width: 100%; }
.sys-message-page .field :deep(.el-select .el-select__wrapper) {
  box-shadow: 0 0 0 1px var(--ink-200) inset !important;
  border-radius: var(--r-sm);
}
.sys-message-page .field :deep(.el-select .el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--brand-500) inset !important;
}
.sys-message-page .field :deep(.el-date-editor) { width: 100%; }
.sys-message-page .field :deep(.el-date-editor .el-range-input) { font-size: 13px; }
.sys-message-page .filter-footer {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 12px; padding-top: 12px; border-top: 1px dashed var(--ink-200);
}
.sys-message-page .filter-info {
  font-size: 12px; color: var(--ink-500);
  display: flex; align-items: center; gap: 6px;
}
.sys-message-page .filter-buttons { display: flex; gap: 8px; }

.filter-slide-enter-active, .filter-slide-leave-active {
  transition: all .2s var(--ease-out);
  overflow: hidden;
}
.filter-slide-enter-from, .filter-slide-leave-to {
  opacity: 0; max-height: 0; padding-top: 0; padding-bottom: 0;
}
.filter-slide-enter-to, .filter-slide-leave-from {
  opacity: 1; max-height: 300px;
}

/* ===== 消息卡片列表 ===== */
.sys-message-page .msg-list {
  padding: 12px; display: flex; flex-direction: column; gap: 8px;
  min-height: 300px;
}

.sys-message-page .msg {
  position: relative; background: #fff;
  border: 1px solid var(--ink-200);
  border-radius: var(--r-md);
  transition: all .15s var(--ease-out);
  overflow: hidden; cursor: pointer;
}
.sys-message-page .msg:hover {
  border-color: var(--ink-300);
  box-shadow: 0 6px 16px -8px rgba(15,23,42,.18);
  transform: translateY(-1px);
}
.sys-message-page .msg .accent {
  position: absolute; left: 0; top: 0; bottom: 0; width: 4px;
  background: var(--ink-200); transition: background .15s;
}
.sys-message-page .msg-inner {
  padding: 14px 16px 14px 18px;
  display: grid;
  grid-template-columns: 36px 1fr auto;
  gap: 14px; align-items: flex-start;
}
.sys-message-page .msg .ic {
  width: 36px; height: 36px; border-radius: 9px;
  display: grid; place-items: center;
  font-size: 16px; flex-shrink: 0;
}
.sys-message-page .msg .body { min-width: 0; }
.sys-message-page .msg .meta {
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
  font-size: 12px; color: var(--ink-500); margin-bottom: 4px;
}
.sys-message-page .msg .meta .tag {
  padding: 1px 8px; border-radius: 999px;
  font-size: 11.5px; font-weight: 600;
  border: 1px solid; line-height: 1.4;
}
.sys-message-page .msg .meta .dot-sep {
  width: 3px; height: 3px; background: var(--ink-300); border-radius: 50%;
}
.sys-message-page .msg .meta .biz-source { color: var(--ink-700); font-weight: 500; }
.sys-message-page .msg .meta .biz-id { font-family: ui-monospace, "JetBrains Mono", "SF Mono", Menlo, monospace; font-size: 11.5px; color: var(--ink-500); }
.sys-message-page .msg .meta .biz-entry { color: var(--brand-600); font-weight: 500; }
.sys-message-page .msg .title {
  font-size: 14px; font-weight: 500; color: var(--ink-700);
  line-height: 1.5; word-break: break-word;
}
.sys-message-page .msg.is-unread .title { color: var(--ink-900); font-weight: 600; }
.sys-message-page .msg .title.is-handled { color: var(--ink-400); text-decoration: line-through; }
.sys-message-page .msg .desc {
  font-size: 12.5px; color: var(--ink-500); line-height: 1.6;
  margin-top: 6px; display: flex; align-items: center; gap: 6px; flex-wrap: wrap;
}
.sys-message-page .msg .desc .handled-tag {
  padding: 1px 7px; border-radius: 4px;
  background: var(--ink-100); color: var(--ink-500);
  font-size: 11px; font-weight: 600;
}
.sys-message-page .msg .desc .desc-sep { color: var(--ink-300); }
.sys-message-page .msg .desc .desc-text {
  flex: 1; min-width: 0;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; word-break: break-word;
}
/* 关键字段高亮：单号/编号（mono 等宽）与金额/日期/数量（em 强调） */
.sys-message-page .msg .desc .desc-text :deep(.hl-mono) {
  font-family: ui-monospace, "JetBrains Mono", "SF Mono", Menlo, monospace;
  font-size: 12px; color: var(--brand-700); font-weight: 600;
  background: var(--brand-50); padding: 0 4px; border-radius: 4px;
}
.sys-message-page .msg .desc .desc-text :deep(.hl-em) {
  color: var(--ink-900); font-weight: 600;
}
.sys-message-page .msg .desc .level-badge {
  padding: 1px 7px; border-radius: 4px;
  font-size: 11px; font-weight: 600; flex-shrink: 0;
}
.sys-message-page .msg .desc .level-normal { background: var(--ink-100); color: var(--ink-500); }
.sys-message-page .msg .desc .level-important { background: var(--amber-50); color: var(--amber-700); }
.sys-message-page .msg .desc .level-urgent { background: var(--red-50); color: var(--red-700); }

.sys-message-page .msg .side-actions {
  display: flex; flex-direction: column; gap: 6px; align-items: flex-end;
  min-width: 110px;
}
.sys-message-page .msg .time { font-size: 12px; color: var(--ink-400); white-space: nowrap; }
.sys-message-page .msg .row-actions {
  display: flex; gap: 4px; flex-wrap: wrap; justify-content: flex-end;
}
.sys-message-page .msg .row-actions button {
  display: inline-flex; align-items: center; gap: 3px;
  height: 26px; padding: 0 9px;
  background: #fff; border: 1px solid var(--ink-200);
  border-radius: 5px; font-size: 12px; color: var(--ink-700);
  cursor: pointer; transition: all .12s;
}
.sys-message-page .msg .row-actions button:hover {
  background: var(--ink-50); color: var(--ink-900); border-color: var(--ink-300);
}
.sys-message-page .msg .row-actions .is-primary {
  background: var(--brand-600); color: #fff; border-color: var(--brand-600);
}
.sys-message-page .msg .row-actions .is-primary:hover {
  background: var(--brand-700); border-color: var(--brand-700);
}
.sys-message-page .msg .row-actions .is-ghost {
  border-color: transparent; background: transparent;
  padding: 0 6px; color: var(--ink-500);
}
.sys-message-page .msg .row-actions .is-ghost:hover { color: var(--ink-900); background: var(--ink-50); }

/* ===== 状态配色 ===== */
/* 系统通知 (type=1) - green */
.sys-message-page .msg.kind-system .accent,
.sys-message-page .msg.kind-system .ic { background: var(--green-50); color: var(--green-700); }
.sys-message-page .msg.kind-system .meta .tag { background: var(--green-50); color: var(--green-700); border-color: #a7f3d0; }

/* 业务预警 (type=2) - red */
.sys-message-page .msg.kind-alert .accent,
.sys-message-page .msg.kind-alert .ic { background: var(--red-50); color: var(--red-700); }
.sys-message-page .msg.kind-alert .meta .tag { background: var(--red-50); color: var(--red-700); border-color: #fecaca; }

/* 审批消息 (type=3) - amber */
.sys-message-page .msg.kind-audit .accent,
.sys-message-page .msg.kind-audit .ic { background: var(--amber-50); color: var(--amber-700); }
.sys-message-page .msg.kind-audit .meta .tag { background: var(--amber-50); color: var(--amber-700); border-color: #fde68a; }

/* 待办事项 (type=4) - blue */
.sys-message-page .msg.kind-todo .accent,
.sys-message-page .msg.kind-todo .ic { background: var(--blue-50); color: var(--blue-700); }
.sys-message-page .msg.kind-todo .meta .tag { background: var(--blue-50); color: var(--blue-700); border-color: #bfdbfe; }

/* 未读态：渐变背景 */
.sys-message-page .msg.kind-system.is-unread { background: linear-gradient(to right, var(--green-50) 0%, #fff 35%); }
.sys-message-page .msg.kind-alert.is-unread { background: linear-gradient(to right, var(--red-50) 0%, #fff 35%); }
.sys-message-page .msg.kind-audit.is-unread { background: linear-gradient(to right, var(--amber-50) 0%, #fff 35%); }
.sys-message-page .msg.kind-todo.is-unread { background: linear-gradient(to right, var(--blue-50) 0%, #fff 35%); }
.sys-message-page .msg.is-unread .accent { background: currentColor; }

/* 已读态 */
.sys-message-page .msg:not(.is-unread) { opacity: .88; }
.sys-message-page .msg:not(.is-unread) .accent { background: var(--ink-200); }

/* ===== Empty State ===== */
.sys-message-page .empty {
  padding: 80px 24px;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: var(--ink-500); text-align: center;
}
.sys-message-page .empty .empty-pic {
  width: 80px; height: 80px; border-radius: 50%;
  background: var(--ink-100);
  display: grid; place-items: center;
  color: var(--ink-400); font-size: 32px;
  margin-bottom: 14px;
}
.sys-message-page .empty .empty-title {
  font-size: 15px; font-weight: 600; color: var(--ink-700); margin-bottom: 4px;
}
.sys-message-page .empty .empty-hint { font-size: 13px; color: var(--ink-500); }

/* ===== Pagination ===== */
.sys-message-page .pager { padding: 0 16px 14px; }
.sys-message-page .pager :deep(.pagination-container) {
  display: flex; align-items: center; justify-content: flex-end;
  padding: 0; font-size: 13px; color: var(--ink-500); background: transparent;
}
.sys-message-page .pager :deep(.el-pagination) { justify-content: flex-end; }
.sys-message-page .pager :deep(.el-pagination .el-pager li) {
  border-radius: 6px; border: 1px solid var(--ink-200);
  background: #fff; min-width: 32px; height: 32px; line-height: 32px;
  font-size: 13px; color: var(--ink-700); margin: 0 2px;
}
.sys-message-page .pager :deep(.el-pagination .el-pager li.is-active) {
  background: var(--brand-600); border-color: var(--brand-600);
  color: #fff; font-weight: 600;
  box-shadow: 0 4px 10px -2px rgba(79,70,229,.4);
}
.sys-message-page .pager :deep(.el-pagination .btn-prev),
.sys-message-page .pager :deep(.el-pagination .btn-next) {
  border-radius: 6px; border: 1px solid var(--ink-200);
  background: #fff; min-width: 32px; height: 32px;
}
.sys-message-page .pager :deep(.el-pagination .btn-prev:hover),
.sys-message-page .pager :deep(.el-pagination .btn-next:hover) {
  border-color: var(--brand-200); color: var(--brand-700);
}

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .sys-message-page .inbox { grid-template-columns: 1fr; }
  .sys-message-page .side { position: static; }
  .sys-message-page .msg .side-actions { min-width: auto; }
  .sys-message-page .filter-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .sys-message-page .filter-row { grid-template-columns: 1fr; }
  .sys-message-page .msg-inner { grid-template-columns: 36px 1fr; }
  .sys-message-page .msg .side-actions {
    grid-column: 1 / -1;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

/* ===== a11y focus ===== */
.sys-message-page .btn-soft:focus-visible,
.sys-message-page .cat:focus-visible,
.sys-message-page .msg .row-actions button:focus-visible,
.sys-message-page .seg button:focus-visible {
  outline: 2px solid var(--brand-500); outline-offset: 2px;
}
</style>
