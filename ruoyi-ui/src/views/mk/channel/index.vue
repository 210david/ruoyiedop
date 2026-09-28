<template>
  <div class="app-container mk-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>账号名称</label>
          <div class="control">
            <el-input v-model="queryParams.accountName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>渠道类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.channelType" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_channel_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>负责人</label>
          <div class="control is-select">
            <el-select v-model="queryParams.userId" placeholder="全部" clearable filterable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.channelStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
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
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:channel:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate()" v-hasPermi="['marketing:channel:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['marketing:channel:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:channel:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_channel_columns_v2" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
          <el-table-column label="账号名称" prop="accountName" key="accountName" :min-width="colWidth('accountName', 160)" resizable show-overflow-tooltip v-if="columns.accountName.visible" />
          <el-table-column label="渠道类型" prop="channelType" key="channelType" :width="colWidth('channelType', 110)" resizable align="center" v-if="columns.channelType.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.channelType)">
                <span class="dot"></span>{{ typeLabel(scope.row.channelType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="主页链接" prop="accountUrl" key="accountUrl" :min-width="colWidth('accountUrl', 200)" resizable v-if="columns.accountUrl.visible">
            <template #default="scope">
              <el-link v-if="scope.row.accountUrl" type="primary" :underline="false" :href="scope.row.accountUrl" target="_blank">{{ scope.row.accountUrl }}</el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="负责人" prop="userName" key="userName" :width="colWidth('userName', 100)" resizable v-if="columns.userName.visible">
            <template #default="scope">{{ scope.row.userName || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="channelStatus" key="channelStatus" :width="colWidth('channelStatus', 100)" resizable align="center" v-if="columns.channelStatus.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.channelStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.channelStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" prop="updateTime" key="updateTime" :width="colWidth('updateTime', 180)" resizable align="center" v-if="columns.updateTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:channel:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:channel:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="channelRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row>
                <el-col :span="12"><el-form-item label="渠道类型" prop="channelType">
                  <el-select v-model="form.channelType" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_channel_type" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="账号名称" prop="accountName"><el-input v-model="form.accountName" placeholder="请输入账号名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="主页链接" prop="accountUrl"><el-input v-model="form.accountUrl" placeholder="https://" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('operate')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>运营信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.operate }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.operate">
              <el-row>
                <el-col :span="12"><el-form-item label="负责人" prop="userId">
                  <el-input v-model="form.userName" readonly placeholder="请选择负责人" style="width: 100%" @click="openUserPicker">
                    <template #append>
                      <el-button icon="Search" @click="openUserPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.userName" class="clear-icon" @click.stop="clearUser"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="24"><el-form-item label="状态" prop="channelStatus">
                  <el-radio-group v-model="form.channelStatus">
                    <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('other')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 负责人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择负责人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="渠道账号业务操作说明" width="640px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="渠道账号">各推广平台（抖音/视频号/小红书/公众号/官网等）的官方账号档案，用于推广内容分发与数据归集</el-descriptions-item>
          <el-descriptions-item label="正常">账号可正常参与推广投放，内容包可分发至该渠道</el-descriptions-item>
          <el-descriptions-item label="停用">账号暂停使用，不再参与新推广，历史推广数据保留</el-descriptions-item>
          <el-descriptions-item label="负责人">账号的日常运营负责人，通过人员选择器选择</el-descriptions-item>
        </el-descriptions>
        <h4>二、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>主页链接：</strong>支持点击直接跳转平台主页，便于快速访问账号</p>
          <p>• <strong>删除规则：</strong>删除为逻辑删除，已删除账号不再展示，历史数据保留</p>
          <p>• <strong>状态切换：</strong>停用渠道不影响已发布内容的统计数据</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkChannel">
import { CircleClose, Search, Filter, Edit, Delete, Download, WarningFilled } from '@element-plus/icons-vue'
import { listChannel, getChannel, addChannel, updateChannel, delChannel } from '@/api/mk/channel'
import { listUser } from '@/api/system/user'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_channel_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'operate', 'other'])
const { marketing_channel_type, sys_normal_disable } = proxy.useDict('marketing_channel_type', 'sys_normal_disable')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0 })
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const showStatusHelp = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, accountName: undefined, channelType: undefined, userId: undefined, channelStatus: undefined, params: {} },
  rules: {
    channelType: [{ required: true, message: '渠道类型不能为空', trigger: 'change' }],
    accountName: [{ required: true, message: '账号名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  accountName: { label: '账号名称', visible: true },
  channelType: { label: '渠道类型', visible: true },
  accountUrl: { label: '主页链接', visible: true },
  userName: { label: '负责人', visible: true },
  channelStatus: { label: '状态', visible: true },
  updateTime: { label: '更新时间', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_channel_columns_v2')
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

const statusTabList = computed(() => sys_normal_disable.value)
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.accountName) count++
  if (queryParams.value.channelType) count++
  if (queryParams.value.userId) count++
  if (queryParams.value.channelStatus) count++
  return count
})

function getList() {
  loading.value = true
  listChannel(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false })
}

/** 打开负责人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.userId)
}
/** 负责人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.userId = user.userId
  form.value.userName = user.nickName
}
/** 清除负责人 */
function clearUser() {
  form.value.userId = undefined
  form.value.userName = undefined
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, accountName: undefined, channelType: undefined, userId: undefined, channelStatus: undefined, params: {} }
  activeStatusTab.value = 'all'
  handleQuery()
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.channelStatus = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() {
  // 基于当前筛选条件（剔除状态与分页）拉取全量数据统计，与列表筛选保持一致
  const query = { ...queryParams.value, pageNum: 1, pageSize: 9999, channelStatus: undefined, params: { ...queryParams.value.params } }
  listChannel(query, { suppressError: true }).then(res => {
    const counts = { all: 0, '0': 0, '1': 0 }
    const rows = res.rows || []
    rows.forEach(r => { if (counts[r.channelStatus] !== undefined) counts[r.channelStatus]++ })
    counts.all = rows.length
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) { const map = { '0': 'tab-done', '1': 'tab-void' }; return map[value] || '' }
function badgeClass(status) { const map = { '0': 'green', '1': 'gray' }; return map[status] || 'gray' }
function statusLabel(status) { const item = sys_normal_disable.value.find(d => d.value == status); return item ? item.label : '-' }
function typeBadgeClass(type) {
  const item = marketing_channel_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}
function typeLabel(type) { const item = marketing_channel_type.value.find(d => d.value == type); return item ? item.label : '-' }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.channelId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { channelId: undefined, channelType: undefined, accountName: undefined, accountUrl: undefined, userId: undefined, userName: undefined, channelStatus: '0', remark: undefined }
  proxy.resetForm('channelRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增渠道账号' }
function handleUpdate(row) {
  reset()
  getChannel(row.channelId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改渠道账号' })
}
function submitForm() {
  proxy.$refs['channelRef'].validate(valid => {
    if (valid) {
      if (form.value.channelId != undefined) { updateChannel(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addChannel(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const channelIds = row.channelId || ids.value
  proxy.$modal.confirm('确认删除选中的渠道账号？').then(() => delChannel(channelIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('mk/channel/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'channelId'), `channel_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
getList()
</script>

<style scoped>
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
:deep(.el-input.is-disabled .el-input__inner) {
  cursor: pointer;
}
.status-help-content {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}
.status-help-content h4 {
  margin: 20px 0 12px 0;
  color: #303133;
  font-weight: 600;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.status-help-content h4:first-child {
  margin-top: 0;
}
.status-help-content .highlight-card {
  background-color: #ecf5ff;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #409eff;
}
.status-help-content .highlight-card p {
  margin: 6px 0;
  line-height: 1.6;
  font-size: 13px;
  color: #606266;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
