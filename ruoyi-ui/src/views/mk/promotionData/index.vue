<template>
  <div class="app-container mk-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>所属内容</label>
          <div class="control is-select">
            <el-select v-model="queryParams.contentId" placeholder="全部" clearable filterable @change="handleFilterContentChange">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="c in contentOptions" :key="c.contentId" :label="c.title" :value="c.contentId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>内容包</label>
          <div class="control is-select">
            <el-select v-model="queryParams.packageId" placeholder="请先选择内容" clearable filterable :disabled="!queryParams.contentId" @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="p in filterPackageOptions" :key="p.packageId" :label="p.packageTitle" :value="p.packageId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>渠道账号</label>
          <div class="control is-select">
            <el-select v-model="queryParams.channelId" placeholder="全部" clearable filterable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="c in channelOptions" :key="c.channelId" :label="c.accountName" :value="c.channelId" />
            </el-select>
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
          <label>录入人</label>
          <div class="control">
            <el-input v-model="queryParams.createBy" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>数据日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:promotionData:add']">录入数据</el-button>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['marketing:promotionData:import']">
            <el-icon><Upload /></el-icon> 批量导入
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['marketing:promotionData:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_promotionData_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
          <el-table-column label="内容包标题" prop="packageTitle" key="packageTitle" :min-width="colWidth('packageTitle', 200)" resizable show-overflow-tooltip v-if="columns.packageTitle.visible">
            <template #default="scope">{{ scope.row.packageTitle || '-' }}</template>
          </el-table-column>
          <el-table-column label="渠道类型" prop="channelType" key="channelType" :width="colWidth('channelType', 110)" resizable align="center" v-if="columns.channelType.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.channelType)">
                <span class="dot"></span>{{ typeLabel(scope.row.channelType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="数据日期" prop="dataDate" key="dataDate" :width="colWidth('dataDate', 130)" resizable align="center" v-if="columns.dataDate.visible" />
          <el-table-column label="曝光量" prop="exposureCount" key="exposureCount" :width="colWidth('exposureCount', 100)" resizable align="center" v-if="columns.exposureCount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatNum(scope.row.exposureCount) }}</span></template>
          </el-table-column>
          <el-table-column label="阅读量" prop="readCount" key="readCount" :width="colWidth('readCount', 100)" resizable align="center" v-if="columns.readCount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatNum(scope.row.readCount) }}</span></template>
          </el-table-column>
          <el-table-column label="点赞量" prop="likeCount" key="likeCount" :width="colWidth('likeCount', 90)" resizable align="center" v-if="columns.likeCount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatNum(scope.row.likeCount) }}</span></template>
          </el-table-column>
          <el-table-column label="评论量" prop="commentCount" key="commentCount" :width="colWidth('commentCount', 90)" resizable align="center" v-if="columns.commentCount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatNum(scope.row.commentCount) }}</span></template>
          </el-table-column>
          <el-table-column label="线索量" prop="leadCount" key="leadCount" :width="colWidth('leadCount', 90)" resizable align="center" v-if="columns.leadCount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatNum(scope.row.leadCount) }}</span></template>
          </el-table-column>
          <el-table-column label="录入人" prop="createBy" key="createBy" :width="colWidth('createBy', 100)" resizable v-if="columns.createBy.visible">
            <template #default="scope">{{ scope.row.createBy || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:promotionData:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:promotionData:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 录入/修改对话框 -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="promotionDataRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row>
                <el-col :span="12"><el-form-item label="所属内容" prop="contentId">
                  <el-input :model-value="form.contentTitle" readonly placeholder="点击选择所属内容" style="width: 100%" @click="openContentPkgPicker">
                    <template #append>
                      <el-button icon="Search" @click="openContentPkgPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.contentId" class="clear-icon" @click.stop="clearContentSelection"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="内容包" prop="packageId">
                  <el-input :model-value="form.packageTitle" readonly placeholder="点击选择内容包" style="width: 100%" @click="openContentPkgPicker">
                    <template #append>
                      <el-button icon="Search" @click="openContentPkgPicker" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="数据日期" prop="dataDate">
                  <el-date-picker v-model="form.dataDate" type="date" value-format="YYYY-MM-DD" placeholder="选择数据日期" style="width: 100%" />
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('metrics')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>效果数据</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.metrics }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.metrics">
              <el-row>
                <el-col :span="8"><el-form-item label="曝光量" prop="exposureCount"><el-input-number v-model="form.exposureCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="阅读量" prop="readCount"><el-input-number v-model="form.readCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="点赞量" prop="likeCount"><el-input-number v-model="form.likeCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="评论量" prop="commentCount"><el-input-number v-model="form.commentCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="线索量" prop="leadCount"><el-input-number v-model="form.leadCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入（规范 4.3.10：统一 ExcelImportDialog 组件） -->
    <excel-import-dialog
      ref="importRef"
      title="推广数据导入"
      action="/mk/promotionData/importData"
      template-action="/mk/promotionData/importTemplate"
      template-file-name="mk_promotion_data_template"
      update-support-label="是否更新已存在的数据（同内容包+数据日期）"
      :tips="importTips"
      :column-labels="importColumnLabels"
      @success="getList"
    />

    <!-- 所属内容/内容包 弹框选择器 -->
    <mk-content-package-picker ref="contentPkgPickerRef" title="选择所属内容与内容包" @confirm="onContentPkgConfirm" />
  </div>
</template>

<script setup name="MkPromotionData">
import { Filter, Upload, Delete } from '@element-plus/icons-vue'
import { CircleClose } from '@element-plus/icons-vue'
import { listPromotionData, getPromotionData, addPromotionData, updatePromotionData, delPromotionData } from '@/api/mk/promotionData'
import { listContent } from '@/api/mk/content'
import { listAllChannel } from '@/api/mk/channel'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog/index.vue'
import MkContentPackagePicker from '@/components/MkContentPackagePicker/index.vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_promotionData_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'metrics'])
const { marketing_channel_type } = proxy.useDict('marketing_channel_type')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const dateRange = ref([])
const contentOptions = ref([])
const channelOptions = ref([])
const filterPackageOptions = ref([])
const importRef = ref(null)
const contentPkgPickerRef = ref(null)

// 批量导入（ExcelImportDialog 规范）
const importTips = [
  '文件格式：内容包ID（或内容包标题）、数据日期（yyyy-MM-dd）、曝光量、阅读量、点赞量、评论量、带来线索量',
  '内容包ID与内容包标题至少填写一项，两者都填时以内容包ID为准',
  '内容包标题存在多条同名记录时导入失败，请改用内容包ID',
  '同「内容包 + 数据日期」已存在数据时，勾选上方更新选项将覆盖更新，否则该行按失败处理',
  '数值列留空按 0 处理，数据日期格式必须为 yyyy-MM-dd'
]
const importColumnLabels = {
  packageId: '内容包ID',
  importPackageTitle: '内容包标题',
  dataDate: '数据日期',
  exposureCount: '曝光量',
  readCount: '阅读量',
  likeCount: '点赞量',
  commentCount: '评论量',
  leadCount: '带来线索量',
  result: '处理结果',
  errorReason: '失败原因'
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, contentId: undefined, packageId: undefined, channelId: undefined, channelType: undefined, createBy: undefined, params: {} },
  rules: {
    packageId: [{ required: true, message: '内容包不能为空', trigger: 'change' }],
    dataDate: [{ required: true, message: '数据日期不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  packageTitle: { label: '内容包标题', visible: true },
  channelType: { label: '渠道类型', visible: true },
  dataDate: { label: '数据日期', visible: true },
  exposureCount: { label: '曝光量', visible: true },
  readCount: { label: '阅读量', visible: true },
  likeCount: { label: '点赞量', visible: true },
  commentCount: { label: '评论量', visible: true },
  leadCount: { label: '线索量', visible: true },
  createBy: { label: '录入人', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_promotionData_columns')
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

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.contentId) count++
  if (queryParams.value.packageId) count++
  if (queryParams.value.channelId) count++
  if (queryParams.value.channelType) count++
  if (queryParams.value.createBy) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listPromotionData(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }).catch(() => { loading.value = false })
}

/** 加载内容包选项（联动） */
function loadPackages(contentId, target) {
  return getContent(contentId).then(res => {
    if (target === 'filter') filterPackageOptions.value = res.data?.packageList || []
    else formPackageOptions.value = res.data?.packageList || []
  }).catch(() => {
    if (target === 'filter') filterPackageOptions.value = []
    else formPackageOptions.value = []
  })
}

/** 筛选-内容联动 */
function handleFilterContentChange(val) {
  queryParams.value.packageId = undefined
  filterPackageOptions.value = []
  if (val) loadPackages(val, 'filter')
  handleQuery()
}

/** 表单：打开所属内容/内容包弹框选择器 */
function openContentPkgPicker() { contentPkgPickerRef.value.open() }
/** 选择器确认：回填内容 + 内容包 */
function onContentPkgConfirm(sel) {
  form.value.contentId = sel.contentId
  form.value.contentTitle = sel.contentTitle
  form.value.packageId = sel.packageId
  form.value.packageTitle = sel.packageTitle
}
function clearContentSelection() {
  form.value.contentId = undefined
  form.value.contentTitle = undefined
  form.value.packageId = undefined
  form.value.packageTitle = undefined
}

function handleQuery() {
  queryParams.value.pageNum = 1
  proxy.addDateRange(queryParams.value, dateRange.value, 'DataTime')
  getList()
}
function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, contentId: undefined, packageId: undefined, channelId: undefined, channelType: undefined, createBy: undefined, params: {} }
  dateRange.value = []
  filterPackageOptions.value = []
  handleQuery()
}

function typeBadgeClass(type) {
  const item = marketing_channel_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}
function typeLabel(type) { const item = marketing_channel_type.value.find(d => d.value == type); return item ? item.label : '-' }
/** 数值千分位显示 */
function formatNum(val) {
  if (val == null) return '-'
  return Number(val).toLocaleString('zh-CN')
}

function handleSelectionChange(selection) { ids.value = selection.map(i => i.dataId); multiple.value = !selection.length }

function reset() {
  form.value = { dataId: undefined, contentId: undefined, contentTitle: undefined, packageId: undefined, packageTitle: undefined, dataDate: undefined, exposureCount: 0, readCount: 0, likeCount: 0, commentCount: 0, leadCount: 0 }
  proxy.resetForm('promotionDataRef')
}
function handleAdd() { reset(); open.value = true; title.value = '录入推广数据' }
function handleUpdate(row) {
  reset()
  getPromotionData(row.dataId).then(res => {
    const d = res.data || {}
    form.value = { dataId: d.dataId, contentId: d.contentId, contentTitle: d.contentTitle, packageId: d.packageId, packageTitle: d.packageTitle, dataDate: d.dataDate, exposureCount: d.exposureCount || 0, readCount: d.readCount || 0, likeCount: d.likeCount || 0, commentCount: d.commentCount || 0, leadCount: d.leadCount || 0 }
    open.value = true
    title.value = '修改推广数据'
  })
}
function submitForm() {
  proxy.$refs['promotionDataRef'].validate(valid => {
    if (valid) {
      if (form.value.dataId != undefined) { updatePromotionData(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addPromotionData(form.value).then(() => { proxy.$modal.msgSuccess('录入成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const dataIds = row.dataId || ids.value
  proxy.$modal.confirm('确认删除选中的推广数据？').then(() => delPromotionData(dataIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}
function cancel() { open.value = false; reset() }

/** 批量导入：打开统一导入弹窗（规范 4.3.10） */
function handleImport() { importRef.value.open() }

// 初始化下拉数据源
listContent({ pageNum: 1, pageSize: 9999 }).then(res => { contentOptions.value = res.rows || [] }).catch(e => { proxy.$modal.msgWarning('内容列表加载失败，内容下拉将为空：' + (e?.message || e)) })
listAllChannel().then(res => { channelOptions.value = res.data || [] }).catch(e => { proxy.$modal.msgWarning('渠道列表加载失败，渠道下拉将为空：' + (e?.message || e)) })
getList()
</script>

<style scoped>
/* 选择器输入框清除图标 */
.clear-icon { color: #c0c4cc; cursor: pointer; }
.clear-icon:hover { color: #909399; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
