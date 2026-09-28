<template>
  <div class="app-container mk-list-page mk-channelLink-page">
    <!-- ===== Activity Selector Card ===== -->
    <div class="surface selector-card" v-show="showSearch">
      <div class="selector-bar">
        <div class="field">
          <label>推广活动</label>
          <div class="control is-select">
            <el-select v-model="activityId" placeholder="请选择报名中/进行中的活动" filterable @change="handleActivityChange">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="a in activityOptions" :key="a.activityId" :label="a.activityName" :value="a.activityId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>渠道账号</label>
          <div class="control is-select">
            <el-select v-model="queryParams.channelId" placeholder="全部渠道" clearable filterable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="c in channelOptions" :key="c.channelId" :label="c.accountName" :value="c.channelId" />
            </el-select>
          </div>
        </div>
        <div class="selector-tip">
          <el-icon><WarningFilled /></el-icon>
          <span>归因链接 = 活动报名链接 + 渠道归因参数，用于统计各渠道的点击/报名/线索数据</span>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 选择活动后展示其归因链接，支持按渠道账号筛选
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Link" @click="handleGenerate" :disabled="!activityId" v-hasPermi="['marketing:channelLink:generate']">生成归因链接</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['marketing:channelLink:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_channelLink_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" :empty-text="activityId ? '暂无归因链接' : '请先在上方选择推广活动'" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
          <el-table-column label="活动名称" prop="activityName" key="activityName" :min-width="colWidth('activityName', 180)" resizable show-overflow-tooltip v-if="columns.activityName.visible" />
          <el-table-column label="渠道账号名" prop="channelName" key="channelName" :min-width="colWidth('channelName', 140)" resizable show-overflow-tooltip v-if="columns.channelName.visible" />
          <el-table-column label="渠道类型" prop="channelTypeQuery" key="channelTypeQuery" :width="colWidth('channelTypeQuery', 110)" resizable align="center" v-if="columns.channelTypeQuery.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.channelTypeQuery)">
                <span class="dot"></span>{{ typeLabel(scope.row.channelTypeQuery) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="归因码" prop="channelCode" key="channelCode" :width="colWidth('channelCode', 130)" resizable align="center" v-if="columns.channelCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.channelCode || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="报名链接" key="registerUrl" :min-width="colWidth('registerUrl', 200)" resizable align="center" v-if="columns.registerUrl.visible">
            <template #default="scope">
              <div class="link-cell">
                <span class="col-mono">{{ scope.row.channelCode }}</span>
                <el-button link type="primary" icon="CopyDocument" @click="handleCopyLink(scope.row)" v-hasPermi="['marketing:channelLink:list']">复制</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="点击数" prop="clickCount" key="clickCount" :width="colWidth('clickCount', 100)" resizable align="center" v-if="columns.clickCount.visible">
            <template #default="scope"><span class="rd-amount">{{ scope.row.clickCount || 0 }}</span></template>
          </el-table-column>
          <el-table-column label="报名数" prop="signupCount" key="signupCount" :width="colWidth('signupCount', 100)" resizable align="center" v-if="columns.signupCount.visible">
            <template #default="scope"><span class="rd-amount">{{ scope.row.signupCount || 0 }}</span></template>
          </el-table-column>
          <el-table-column label="线索数" prop="leadCount" key="leadCount" :width="colWidth('leadCount', 100)" resizable align="center" v-if="columns.leadCount.visible">
            <template #default="scope"><span class="rd-amount">{{ scope.row.leadCount || 0 }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="CopyDocument" @click="handleCopyLink(scope.row)" v-hasPermi="['marketing:channelLink:list']">复制链接</el-button>
                <el-button link type="primary" icon="Grid" @click="handleQrCode(scope.row)" v-hasPermi="['marketing:channelLink:list']">二维码</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:channelLink:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 生成归因链接对话框 -->
    <el-dialog v-model="generateOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></div>
          <span class="rd-detail-header-title">生成归因链接</span>
          <div class="rd-detail-header-sub" v-if="currentActivityName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ currentActivityName }}</span>
          </div>
        </div>
      </template>
      <el-form label-width="90px">
        <el-form-item label="推广活动">
          <el-input :model-value="currentActivityName" readonly />
        </el-form-item>
        <el-form-item label="选择渠道">
          <el-select v-model="generateChannelIds" multiple filterable collapse-tags collapse-tags-tooltip placeholder="请选择渠道账号（可多选）" style="width: 100%">
            <el-option v-for="c in channelOptions" :key="c.channelId" :label="c.accountName" :value="c.channelId" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="generate-tip">
        为选中渠道批量生成归因链接，已生成过的渠道自动跳过；客户通过归因链接报名后，点击/报名/线索数据将自动统计到对应渠道
      </div>
      <template #footer>
        <el-button type="primary" :loading="generateLoading" @click="submitGenerate">生 成</el-button>
        <el-button @click="generateOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 归因二维码对话框 -->
    <el-dialog v-model="qrOpen" width="480px" append-to-body draggable class="rd-dialog" v-loading="qrLoading">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><path d="M14 14h3v3h-3z"/><path d="M21 14v.01"/><path d="M14 21v.01"/><path d="M18 18h3"/><path d="M18 21h3"/></svg></div>
          <span class="rd-detail-header-title">归因二维码</span>
          <div class="rd-detail-header-sub" v-if="qrRow.channelName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ qrRow.channelName }}</span>
          </div>
        </div>
      </template>
      <div class="qr-center">
        <canvas ref="qrCanvasRef" class="qr-canvas"></canvas>
        <div class="qr-tip">扫码进入该渠道专属报名页</div>
        <div class="qr-url">
          <el-input :model-value="qrUrl" readonly>
            <template #append><el-button @click="copyQrUrl">复制链接</el-button></template>
          </el-input>
        </div>
        <div class="qr-actions">
          <el-button type="primary" icon="Download" @click="downloadQrCode">下载二维码</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MkChannelLink">
import QRCode from 'qrcode'
import { Filter, Delete, WarningFilled } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { listChannelLink, generateLinks, delChannelLink } from '@/api/mk/channelLink'
import { listActivity, getRegisterUrl } from '@/api/mk/activity'
import { listAllChannel } from '@/api/mk/channel'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const route = useRoute()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_channelLink_index')
const { marketing_channel_type } = proxy.useDict('marketing_channel_type')

const list = ref([])
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const activityId = ref(null)
const activityOptions = ref([])
const channelOptions = ref([])
const generateOpen = ref(false)
const generateLoading = ref(false)
const generateChannelIds = ref([])
const qrOpen = ref(false)
const qrLoading = ref(false)
const qrUrl = ref('')
const qrRow = ref({})
const qrCanvasRef = ref(null)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, channelId: undefined, params: {} }
})
const { queryParams } = toRefs(data)

// 当前选中活动名称
const currentActivityName = computed(() => {
  const item = activityOptions.value.find(a => Number(a.activityId) === Number(activityId.value))
  return item ? item.activityName : ''
})

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  activityName: { label: '活动名称', visible: true },
  channelName: { label: '渠道账号名', visible: true },
  channelTypeQuery: { label: '渠道类型', visible: true },
  channelCode: { label: '归因码', visible: true },
  registerUrl: { label: '报名链接', visible: true },
  clickCount: { label: '点击数', visible: true },
  signupCount: { label: '报名数', visible: true },
  leadCount: { label: '线索数', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_channelLink_columns')
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

// 加载活动选项（仅报名中/进行中），URL 带 activityId 时自动选中
function loadActivities() {
  listActivity({ pageNum: 1, pageSize: 9999 }).then(res => {
    const rows = res.rows || []
    const targetId = route.query.activityId ? Number(route.query.activityId) : null
    activityOptions.value = rows.filter(a => ['1', '2'].includes(String(a.activityStatus)))
    if (targetId) {
      const target = rows.find(a => Number(a.activityId) === targetId)
      // URL 指定的活动即使已结束也加入选项，便于回看归因数据
      if (target && !activityOptions.value.some(a => Number(a.activityId) === targetId)) {
        activityOptions.value.unshift(target)
      }
      if (target) { activityId.value = targetId; getList() }
    }
  })
}

function getList() {
  if (!activityId.value) { list.value = []; total.value = 0; return }
  loading.value = true
  listChannelLink({ ...queryParams.value, activityId: activityId.value }).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function handleActivityChange() { queryParams.value.pageNum = 1; getList() }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
/** 重置：清空渠道筛选，保留活动选择（活动为页面主维度，重置仅还原筛选条件） */
function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 10, channelId: undefined, params: {} }
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.linkId); multiple.value = !selection.length }

/** 渠道类型徽章样式（复用字典 listClass 配色） */
function typeBadgeClass(type) {
  const item = marketing_channel_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}
function typeLabel(type) { const item = marketing_channel_type.value.find(d => d.value == type); return item ? item.label : '-' }

/** 拼接完整归因报名链接（token 实时获取 + 归因参数 ch） */
async function buildRegisterUrl(row) {
  const res = await getRegisterUrl(row.activityId)
  return window.location.origin + '/h5/register/' + row.activityId + '?token=' + res.token + '&ch=' + row.channelCode
}

/** 复制文本到剪贴板（含降级方案） */
function copyText(text) {
  navigator.clipboard.writeText(text).then(() => proxy.$modal.msgSuccess('链接已复制到剪贴板')).catch(() => {
    const input = document.createElement('input'); input.value = text; document.body.appendChild(input); input.select(); document.execCommand('copy'); document.body.removeChild(input); proxy.$modal.msgSuccess('链接已复制到剪贴板')
  })
}

/** 复制归因链接 */
async function handleCopyLink(row) {
  try {
    const url = await buildRegisterUrl(row)
    copyText(url)
  } catch (e) {}
}

/** 生成归因链接 */
function handleGenerate() {
  if (!activityId.value) { proxy.$modal.msgWarning('请先选择推广活动'); return }
  generateChannelIds.value = []
  generateOpen.value = true
}

function submitGenerate() {
  if (!generateChannelIds.value.length) { proxy.$modal.msgWarning('请选择渠道账号'); return }
  generateLoading.value = true
  generateLinks({ activityId: activityId.value, channelIds: generateChannelIds.value }).then(res => {
    const rows = res.data || []
    rows.length ? proxy.$modal.msgSuccess(`成功生成 ${rows.length} 条归因链接`) : proxy.$modal.msgWarning('所选渠道均已生成归因链接')
    generateOpen.value = false
    getList()
  }).finally(() => { generateLoading.value = false })
}

/** 归因二维码 */
async function handleQrCode(row) {
  qrOpen.value = true
  qrLoading.value = true
  qrRow.value = row
  qrUrl.value = ''
  try {
    qrUrl.value = await buildRegisterUrl(row)
    await nextTick()
    if (qrCanvasRef.value) {
      await QRCode.toCanvas(qrCanvasRef.value, qrUrl.value, { width: 240, margin: 2, color: { dark: '#1a1a2e', light: '#ffffff' } })
    }
  } finally { qrLoading.value = false }
}

function copyQrUrl() { if (qrUrl.value) copyText(qrUrl.value) }

function downloadQrCode() {
  if (!qrCanvasRef.value) return
  const link = document.createElement('a')
  link.download = '归因二维码_' + (qrRow.value.channelName || '') + '_' + (qrRow.value.channelCode || '') + '.png'
  link.href = qrCanvasRef.value.toDataURL('image/png')
  link.click()
}

/** 删除归因链接 */
function handleDelete(row) {
  const linkIds = (row && row.linkId) || ids.value
  proxy.$modal.confirm('确认删除选中的归因链接？').then(() => delChannelLink(linkIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

loadActivities()
listAllChannel().then(res => { channelOptions.value = res.data || [] })
</script>

<style scoped>
/* ===== 活动选择卡片 ===== */
.selector-card { padding: 14px 20px 16px; }
.selector-bar { display: flex; align-items: flex-end; gap: 24px; }
.selector-bar .field { width: 320px; }
.selector-tip { margin-left: auto; display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--ink-500); padding-bottom: 8px; }

/* ===== 报名链接列 ===== */
.link-cell { display: inline-flex; align-items: center; gap: 6px; justify-content: center; }

/* ===== 生成弹窗提示 ===== */
.generate-tip { margin-top: 4px; padding: 10px 12px; background: #ecf5ff; border-radius: 6px; font-size: 13px; color: #606266; line-height: 1.8; }

/* ===== 二维码弹窗 ===== */
.qr-center { text-align: center; padding: 8px 12px; }
.qr-canvas { border-radius: 8px; border: 1px solid #ebeef5; }
.qr-tip { margin: 10px 0 16px; font-size: 13px; color: #909399; }
.qr-url { margin-bottom: 16px; text-align: left; }
.qr-actions { display: flex; justify-content: center; }

/* 操作列按钮对齐：flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
