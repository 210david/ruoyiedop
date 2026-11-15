<template>
  <div class="app-container flowable-urgency-page">
    <!-- ===== 筛选条件卡片 ===== -->
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
          <label>档位值</label>
          <div class="control">
            <el-input v-model="queryParams.levelValue" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><CollectionTag /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>默认名称</label>
          <div class="control">
            <el-input v-model="queryParams.defaultName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>自定义名称</label>
          <div class="control">
            <el-input v-model="queryParams.customName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><EditPen /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>是否封存</label>
          <div class="control is-select">
            <el-select v-model="queryParams.isSealed" placeholder="全部" clearable @change="handleQuery">
              <el-option label="未封存" value="0" />
              <el-option label="已封存" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>提醒分级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.isRemind" placeholder="全部" clearable @change="handleQuery">
              <el-option label="触发" value="1" />
              <el-option label="不触发" value="0" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>显示顺序</label>
          <div class="control">
            <el-input v-model="queryParams.showOrder" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Sort /></el-icon></template>
            </el-input>
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

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button class="status-tab tab-approved" :class="{ 'is-active': activeStatusTab === '0' }" @click="handleStatusTabClick('0')">
            <span class="dot"></span><span>正常</span><span class="count">{{ statusCounts['0'] }}</span>
          </button>
          <button class="status-tab tab-void" :class="{ 'is-active': activeStatusTab === '1' }" @click="handleStatusTabClick('1')">
            <span class="dot"></span><span>停用</span><span class="count">{{ statusCounts['1'] }}</span>
          </button>
        </div>
      </div>
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:urgency:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete" v-hasPermi="['flowable:urgency:remove']">批量删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_urgency_index" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table"
          @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="档位值" prop="levelValue" key="levelValue" :width="colWidth('levelValue', 100)" align="center" resizable v-if="columns.levelValue.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.levelValue }}</span></template>
          </el-table-column>
          <el-table-column label="默认名称" prop="defaultName" key="defaultName" :min-width="colWidth('defaultName', 160)" align="center" show-overflow-tooltip resizable v-if="columns.defaultName.visible">
            <template #default="scope"><span class="col-strong">{{ scope.row.defaultName }}</span></template>
          </el-table-column>
          <el-table-column label="自定义名称" prop="customName" key="customName" :min-width="colWidth('customName', 180)" align="center" show-overflow-tooltip resizable v-if="columns.customName.visible">
            <template #default="scope">{{ scope.row.customName || '—' }}</template>
          </el-table-column>
          <el-table-column label="显示顺序" prop="showOrder" key="showOrder" :width="colWidth('showOrder', 110)" align="center" resizable v-if="columns.showOrder.visible" />
          <el-table-column label="是否封存" prop="isSealed" key="isSealed" :width="colWidth('isSealed', 120)" align="center" resizable v-if="columns.isSealed.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isSealed === '1' ? 'gray' : 'green'">
                <span class="dot"></span>{{ scope.row.isSealed === '1' ? '已封存' : '未封存' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="提醒分级" prop="isRemind" key="isRemind" :width="colWidth('isRemind', 120)" align="center" resizable v-if="columns.isRemind.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isRemind === '1' ? 'amber' : 'gray'">
                <span class="dot"></span>{{ scope.row.isRemind === '1' ? '触发' : '不触发' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" align="center" resizable v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['flowable:urgency:edit']">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:urgency:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- ===== 新增/编辑弹窗 ===== -->
    <el-dialog v-model="open" width="560px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ form.levelId ? '编辑紧急程度' : '新增紧急程度' }}</span>
        </div>
      </template>
      <el-form ref="urgencyRef" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="档位值" prop="levelValue">
          <el-input v-model="form.levelValue" placeholder="如：1（存业务表 urgency 列，创建后不可修改）" maxlength="20" :disabled="!!form.levelId" />
          <div class="form-tip">业务表 urgency 列实际存储的值；已创建的档位值不可修改，避免历史数据错位</div>
        </el-form-item>
        <el-form-item label="默认名称" prop="defaultName">
          <el-input v-model="form.defaultName" placeholder="如：普通 / 紧急 / 特急" maxlength="20" />
        </el-form-item>
        <el-form-item label="自定义名称" prop="customName">
          <el-input v-model="form.customName" placeholder="留空则使用默认名称（企业可自定义显示名）" maxlength="100" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="showOrder">
          <el-input-number v-model="form.showOrder" :min="0" :max="999" controls-position="right" style="width:160px" />
          <div class="form-tip">数字越小排序越靠前，决定发起页下拉的档位顺序</div>
        </el-form-item>
        <el-form-item label="是否封存" prop="isSealed">
          <el-switch v-model="form.isSealed" active-value="1" inactive-value="0" />
          <span class="form-tip" style="margin-left:12px">封存后新发起的流程不再显示该档位（历史数据不受影响）</span>
        </el-form-item>
        <el-form-item label="提醒分级" prop="isRemind">
          <el-switch v-model="form.isRemind" active-value="1" inactive-value="0" />
          <span class="form-tip" style="margin-left:12px">该档位是否触发分级提醒（预留能力，需配合提醒分发逻辑）</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowUrgency">
import { listUrgencyLevel, getUrgencyLevel, addUrgencyLevel, updateUrgencyLevel, delUrgencyLevel } from '@/api/flowable/urgency'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, CollectionTag, EditPen, Sort, ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_urgency_index')
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const open = ref(false)
const urgencyRef = ref(null)
const selectedIds = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0 })

/** 状态下拉选项（列表与筛选共用口径） */
const statusOptions = [
  { label: '正常', value: '0' },
  { label: '停用', value: '1' }
]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1, pageSize: 10,
    levelValue: undefined, defaultName: undefined, customName: undefined,
    showOrder: undefined, isSealed: undefined, isRemind: undefined,
    status: undefined, params: {}
  }
})
const { queryParams, form } = toRefs(data)

// ===== 列显隐（RightToolbar 持久化） =====
const defaultColumns = {
  levelValue: { label: '档位值', visible: true },
  defaultName: { label: '默认名称', visible: true },
  customName: { label: '自定义名称', visible: true },
  showOrder: { label: '显示顺序', visible: true },
  isSealed: { label: '是否封存', visible: true },
  isRemind: { label: '提醒分级', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_urgency_columns')
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
watch(columns, v => localStorage.setItem('flowable_urgency_columns', JSON.stringify(v)), { deep: true })

const rules = {
  levelValue: [
    { required: true, message: '档位值不能为空', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{1,20}$/, message: '仅允许字母/数字/下划线（1-20位）', trigger: 'blur' }
  ],
  defaultName: [{ required: true, message: '默认名称不能为空', trigger: 'blur' }],
  showOrder: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
}

/** 状态文案（列表徽章与筛选下拉共用口径） */
function statusLabel(v) {
  const item = statusOptions.find(d => d.value == v)
  return item ? item.label : (v === undefined || v === null || v === '' ? '-' : String(v))
}

const activeFilterCount = computed(() => {
  const q = queryParams.value
  return [q.levelValue, q.defaultName, q.customName, q.showOrder, q.isSealed, q.isRemind, q.status]
    .filter(v => v !== undefined && v !== null && v !== '').length
})

function getList() {
  loading.value = true
  listUrgencyLevel(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

/** 状态标签栏数量统计（不分页全量统计） */
function loadStatusCounts() {
  listUrgencyLevel({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total || 0, '0': 0, '1': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => { statusCounts.value = { all: 0, '0': 0, '1': 0 } })
}

/** 状态标签点击：与状态筛选条件联动 */
function handleStatusTabClick(s) {
  activeStatusTab.value = s
  queryParams.value.status = s === 'all' ? undefined : s
  handleQuery()
}

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }

/** 重置：显式重建 queryParams 整体对象（规范 2.2.4），并还原状态标签 */
function resetQuery() {
  queryParams.value = {
    pageNum: 1, pageSize: queryParams.value.pageSize,
    levelValue: undefined, defaultName: undefined, customName: undefined,
    showOrder: undefined, isSealed: undefined, isRemind: undefined,
    status: undefined, params: {}
  }
  activeStatusTab.value = 'all'
  showAdvanced.value = false
  getList()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.levelId)
}

function nextSort() {
  const max = list.value.reduce((m, r) => Math.max(m, r.showOrder || 0), 0)
  return max + 1
}

function handleAdd() {
  form.value = {
    levelId: undefined, levelValue: undefined, defaultName: undefined, customName: undefined,
    showOrder: nextSort(), isSealed: '0', isRemind: '0', status: '0'
  }
  open.value = true
  nextTick(() => urgencyRef.value && urgencyRef.value.resetFields())
}

async function handleEdit(row) {
  const res = await getUrgencyLevel(row.levelId)
  const d = res.data || {}
  form.value = {
    ...d,
    isSealed: d.isSealed || '0',
    isRemind: d.isRemind || '0',
    status: d.status || '0'
  }
  open.value = true
  nextTick(() => urgencyRef.value && urgencyRef.value.clearValidate())
}

function submitForm() {
  urgencyRef.value.validate(valid => {
    if (!valid) return
    const payload = {
      levelId: form.value.levelId,
      levelValue: form.value.levelValue,
      defaultName: form.value.defaultName,
      customName: form.value.customName,
      showOrder: form.value.showOrder,
      isSealed: form.value.isSealed,
      isRemind: form.value.isRemind,
      status: form.value.status
    }
    if (form.value.levelId) {
      updateUrgencyLevel(payload).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
        .catch(e => { proxy.$modal.msgError('修改失败：' + (e?.message || e || '请稍后重试')) })
    } else {
      addUrgencyLevel(payload).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
        .catch(e => { proxy.$modal.msgError('新增失败：' + (e?.message || e || '请稍后重试')) })
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除紧急程度"' + (row.customName || row.defaultName) + '"？已在用的档位删除后历史数据仍保留该值。').then(() => delUrgencyLevel(row.levelId)).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList(); loadStatusCounts()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}

function handleBatchDelete() {
  proxy.$modal.confirm('确认删除选中的 ' + selectedIds.value.length + ' 个档位？').then(() => delUrgencyLevel(selectedIds.value.join(','))).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList(); loadStatusCounts()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('批量删除失败：' + (e?.message || e)) })
}

getList()
loadStatusCounts()
onActivated(() => { getList(); loadStatusCounts() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

/* 页面级样式补充（Token/卡片/筛选/表格/状态标签栏/徽章由 flow-model.scss 统一提供） */
.flowable-urgency-page {
  /* 筛选区紧凑化（对齐其他流程列表页口径） */
  .filter-card {
    padding: 12px 16px 14px;
    margin-bottom: 6px;
    .filter-head { margin-bottom: 10px; }
    .filter-bar { gap: 10px 14px; }
    .filter-actions { margin-top: 10px; padding-top: 10px; }
    .field .control { height: 32px; padding: 0 10px; }
    .field .control :deep(.el-input__wrapper) { height: 30px; }
    .field .control :deep(.el-input__inner) { height: 30px; line-height: 30px; }
  }

  /* 工具栏：无新增批量按钮时保持与列表区一致的内边距 */
  .toolbar { padding: 10px 16px; }

  /* 表格与分页 */
  .table-wrap { padding-top: 6px; }
  .table-wrap + .pagination-container { padding-top: 0; }

  /* 弹窗表单内的说明文字 */
  .form-tip {
    font-size: 12px;
    color: var(--ink-400, #94a3b8);
    line-height: 1.5;
    margin-top: 4px;
  }
}
</style>
