<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="12 2 2 7 12 12 22 7 12 2"/>
            <polyline points="2 17 12 22 22 17"/>
            <polyline points="2 12 12 17 22 12"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="mk-pkg-picker">
      <!-- 步骤条 -->
      <div class="mk-pkg-picker-steps">
        <span class="step" :class="{ 'is-active': step === 1 }">1 选择所属内容</span>
        <el-icon><ArrowRight /></el-icon>
        <span class="step" :class="{ 'is-active': step === 2, 'is-disabled': !selectedContentId }">2 选择内容包</span>
      </div>

      <!-- 步骤1：内容列表 -->
      <template v-if="step === 1">
        <div class="mk-pkg-picker-search">
          <el-input v-model="contentQuery.title" placeholder="内容标题" clearable size="small" style="width: 200px" @keyup.enter="loadContents" />
          <el-select v-model="contentQuery.auditStatus" placeholder="审核状态" clearable size="small" style="width: 130px; margin-left: 8px" @change="loadContents">
            <el-option v-for="d in marketing_content_audit_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="loadContents">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetContentQuery">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="contentList" row-key="contentId" highlight-current-row size="small" height="340" @row-click="onContentClick" @row-dblclick="onContentDblClick">
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedContentId" :value="row.contentId" @click.stop="onContentClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="内容编号" prop="contentNo" width="150" show-overflow-tooltip>
            <template #default="scope"><span class="col-mono">{{ scope.row.contentNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="内容标题" prop="title" min-width="220" show-overflow-tooltip />
          <el-table-column label="内容类型" prop="contentType" width="110" align="center">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.contentType)">{{ typeLabel(scope.row.contentType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="审核状态" prop="auditStatus" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="auditBadgeClass(scope.row.auditStatus)"><span class="dot"></span>{{ statusLabel(scope.row.auditStatus) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="mk-pkg-picker-pager">
          <el-pagination v-model:current-page="contentQuery.pageNum" v-model:page-size="contentQuery.pageSize" :total="contentTotal" layout="total, prev, pager, next" small @current-change="loadContents" />
        </div>
      </template>

      <!-- 步骤2：内容包列表 -->
      <template v-else>
        <div class="mk-pkg-picker-search">
          <el-button icon="ArrowLeft" size="small" @click="backToContent">返回重选内容</el-button>
          <span class="pkg-context">当前内容：<strong>{{ selectedContentTitle }}</strong></span>
        </div>
        <el-table v-loading="pkgLoading" :data="pkgList" row-key="packageId" highlight-current-row size="small" height="340" @row-click="onPkgClick" @row-dblclick="onPkgDblClick">
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedPkgId" :value="row.packageId" @click.stop="onPkgClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="包标题" prop="packageTitle" min-width="220" show-overflow-tooltip />
          <el-table-column label="渠道账号" prop="channelName" min-width="140" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.channelName || '-' }}</template>
          </el-table-column>
          <el-table-column label="包状态" prop="packageStatus" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="pkgStatusBadgeClass(scope.row.packageStatus)">{{ pkgStatusLabel(scope.row.packageStatus) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="mk-pkg-picker-empty" v-if="!pkgLoading && pkgList.length === 0">该内容下暂无内容包</div>
      </template>
    </div>
    <template #footer>
      <el-button v-if="step === 2" @click="backToContent">上一步</el-button>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="selectedPkgId == null">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listContent, getContent } from '@/api/mk/content'

const { proxy } = getCurrentInstance()
const { marketing_content_type, marketing_content_audit_status, marketing_package_status } = proxy.useDict('marketing_content_type', 'marketing_content_audit_status', 'marketing_package_status')

const props = defineProps({
  title: { type: String, default: '选择所属内容与内容包' }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const pkgLoading = ref(false)
const step = ref(1)
const contentList = ref([])
const contentTotal = ref(0)
const pkgList = ref([])
const selectedContentId = ref(null)
const selectedContentTitle = ref('')
const selectedContentNo = ref('')
const selectedPkgId = ref(null)
const selectedPkgRow = ref(null)

const contentQuery = reactive({ pageNum: 1, pageSize: 10, title: undefined, auditStatus: undefined })

/** 弹窗打开时初始化 */
function onOpen() {
  step.value = 1
  selectedContentId.value = null
  selectedContentTitle.value = ''
  selectedContentNo.value = ''
  selectedPkgId.value = null
  selectedPkgRow.value = null
  contentQuery.title = undefined
  contentQuery.auditStatus = undefined
  contentQuery.pageNum = 1
  loadContents()
}

function loadContents() {
  loading.value = true
  listContent({ pageNum: contentQuery.pageNum, pageSize: contentQuery.pageSize, title: contentQuery.title, auditStatus: contentQuery.auditStatus }).then(res => {
    contentList.value = res.rows || []
    contentTotal.value = res.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}
function resetContentQuery() {
  contentQuery.title = undefined
  contentQuery.auditStatus = undefined
  contentQuery.pageNum = 1
  loadContents()
}

function onContentClick(row) { selectedContentId.value = row.contentId }
function onContentDblClick(row) { selectContent(row) }
function selectContent(row) {
  selectedContentId.value = row.contentId
  selectedContentTitle.value = row.title
  selectedContentNo.value = row.contentNo || ''
  selectedPkgId.value = null
  selectedPkgRow.value = null
  step.value = 2
  loadPackages()
}
function loadPackages() {
  pkgLoading.value = true
  getContent(selectedContentId.value).then(res => {
    pkgList.value = res.data?.packageList || []
    pkgLoading.value = false
  }).catch(() => { pkgLoading.value = false })
}
function backToContent() {
  step.value = 1
  selectedPkgId.value = null
  selectedPkgRow.value = null
  loadContents()
}

function onPkgClick(row) { selectedPkgId.value = row.packageId }
function onPkgDblClick(row) { selectPkg(row) }
function selectPkg(row) { selectedPkgId.value = row.packageId; selectedPkgRow.value = row }

function handleConfirm() {
  if (selectedPkgId.value == null) return
  const pkg = selectedPkgRow.value || pkgList.value.find(p => p.packageId === selectedPkgId.value)
  emit('confirm', {
    contentId: selectedContentId.value,
    contentTitle: selectedContentTitle.value,
    contentNo: selectedContentNo.value,
    packageId: pkg.packageId,
    packageTitle: pkg.packageTitle,
    channelId: pkg.channelId,
    channelName: pkg.channelName,
    channelType: pkg.channelType
  })
  visible.value = false
}

defineExpose({ open: (pkgId) => { visible.value = true; if (pkgId) selectedPkgId.value = pkgId } })

/* 徽标样式（与列表页一致） */
function typeBadgeClass(type) {
  const item = marketing_content_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}
function typeLabel(type) { const item = marketing_content_type.value.find(d => d.value == type); return item ? item.label : '-' }
function auditBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'red' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_content_audit_status.value.find(d => d.value == status); return item ? item.label : '-' }
function pkgStatusBadgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'gray' }; return map[status] || 'amber' }
function pkgStatusLabel(status) { const item = marketing_package_status.value.find(d => d.value == status); return item ? item.label : '待发布' }
</script>

<style scoped>
.mk-pkg-picker-steps { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; font-size: 13px; color: #94a3b8; }
.mk-pkg-picker-steps .step { padding: 3px 12px; border-radius: 999px; background: #f1f5f9; }
.mk-pkg-picker-steps .step.is-active { background: #eff6ff; color: #1d4ed8; font-weight: 600; }
.mk-pkg-picker-steps .step.is-disabled { opacity: 0.5; }
.mk-pkg-picker-search { display: flex; align-items: center; margin-bottom: 10px; }
.pkg-context { margin-left: 12px; font-size: 13px; color: #334155; }
.mk-pkg-picker-pager { display: flex; justify-content: flex-end; margin-top: 10px; }
.mk-pkg-picker-empty { text-align: center; padding: 40px 0; color: #94a3b8; font-size: 13px; }
.badge { display: inline-flex; align-items: center; padding: 3px 9px; border-radius: 999px; font-size: 12px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.badge.blue { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
.badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; }
.badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.badge.gray { background: #f1f5f9; color: #64748b; border-color: #e2e8f0; }
.badge .dot { width: 6px; height: 6px; border-radius: 50%; margin-right: 5px; background: currentColor; }
.col-mono { font-variant-numeric: tabular-nums; }
</style>
