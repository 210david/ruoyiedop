<template>
  <div class="app-container hr-taxRate-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>级数</label>
          <div class="control is-select">
            <el-select v-model="queryParams.level" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="y in levelOptions" :key="y" :label="'第' + y + '级'" :value="y" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" /><el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>生效日期</label>
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:taxRate:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:taxRate:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:taxRate:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:taxRate:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_taxRate_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrTaxRateList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="级数" prop="level" key="level" align="center" :width="colWidth('level', 90)" resizable v-if="columns.level.visible" />
          <el-table-column label="下限" prop="minAmount" key="minAmount" align="center" :width="colWidth('minAmount', 130)" resizable v-if="columns.minAmount.visible" />
          <el-table-column label="上限" prop="maxAmount" key="maxAmount" align="center" :width="colWidth('maxAmount', 130)" resizable v-if="columns.maxAmount.visible" />
          <el-table-column label="税率" prop="rate" key="rate" align="center" :width="colWidth('rate', 110)" resizable v-if="columns.rate.visible" />
          <el-table-column label="速算扣除数" prop="quickDeduction" key="quickDeduction" align="center" :width="colWidth('quickDeduction', 130)" resizable v-if="columns.quickDeduction.visible" />
          <el-table-column label="生效日期" prop="effectiveDate" key="effectiveDate" align="center" :width="colWidth('effectiveDate', 130)" resizable v-if="columns.effectiveDate.visible" />
          <el-table-column label="状态" prop="status" key="status" align="center" :width="colWidth('status', 100)" resizable v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="删除标志" prop="delFlag" key="delFlag" align="center" :width="colWidth('delFlag', 110)" resizable v-if="columns.delFlag.visible">
            <template #default="scope">
              <span class="badge" :class="delFlagBadgeClass(scope.row.delFlag)"><span class="dot"></span>{{ delFlagLabel(scope.row.delFlag) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:taxRate:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:taxRate:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:taxRate:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrTaxRateRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="级数" prop="level"><el-input-number v-model="form.level" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 税率信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></span>税率信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="下限" prop="minAmount"><el-input-number v-model="form.minAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="上限" prop="maxAmount"><el-input-number v-model="form.maxAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="税率" prop="rate"><el-input-number v-model="form.rate" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="速算扣除数" prop="quickDeduction"><el-input-number v-model="form.quickDeduction" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 生效信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>生效信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="生效日期" prop="effectiveDate"><el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
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

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">个税税率详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.taxRateId">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">级数：第{{ viewData.level }}级</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">级数</span><div class="rd-value">{{ viewData.level != null ? ('第' + viewData.level + '级') : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="statusOptions" :value="viewData.status" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></span>税率信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">下限</span><div class="rd-value">{{ viewData.minAmount ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">上限</span><div class="rd-value">{{ viewData.maxAmount ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">税率</span><div class="rd-value">{{ viewData.rate ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">速算扣除数</span><div class="rd-value">{{ viewData.quickDeduction ?? '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>生效信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ viewData.effectiveDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c3">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrTaxRate">
import { listHrTaxRate, getHrTaxRate, addHrTaxRate, updateHrTaxRate, delHrTaxRate } from '@/api/hr/hrTaxRate'
import { Filter, Search, Edit, Delete, Download } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()

const hrTaxRateList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const dateRange = ref([])

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_taxRate_index')

/** 列显隐配置 - 从 localStorage 恢复保存的设置 */
const defaultColumns = {
  level: { label: '级数', visible: true },
  minAmount: { label: '下限', visible: true },
  maxAmount: { label: '上限', visible: true },
  rate: { label: '税率', visible: true },
  quickDeduction: { label: '速算扣除数', visible: true },
  effectiveDate: { label: '生效日期', visible: true },
  status: { label: '状态', visible: true },
  delFlag: { label: '删除标志', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_taxRate_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

/** 查看详情 */
const viewData = ref({})
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, e0: false, e1: false, e2: false })
function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

const statusOptions = ref([
  { label: '正常', value: '0', elTagType: 'success' },
  { label: '停用', value: '1', elTagType: 'info' }
])
const levelOptions = [1, 2, 3, 4, 5, 6, 7]

function statusLabel(v) {
  const map = { '0': '正常', '1': '停用' }
  return map[String(v)] || '-'
}
function statusBadgeClass(v) {
  const map = { '0': 'green', '1': 'gray' }
  return map[String(v)] || 'gray'
}
function delFlagLabel(v) {
  const map = { '0': '正常', '2': '已删除' }
  return map[String(v)] || '-'
}
function delFlagBadgeClass(v) {
  const map = { '0': 'green', '2': 'red' }
  return map[String(v)] || 'gray'
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, level: undefined, status: undefined },
  rules: {
    level: [{ required: true, message: '级数不能为空', trigger: 'blur' }],
    minAmount: [{ required: true, message: '下限不能为空', trigger: 'blur' }],
    rate: [{ required: true, message: '税率不能为空', trigger: 'blur' }],
    quickDeduction: [{ required: true, message: '速算扣除数不能为空', trigger: 'blur' }],
    effectiveDate: [{ required: true, message: '生效日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.level != null) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listHrTaxRate(queryParams.value).then(response => {
    hrTaxRateList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  queryParams.value.params = {}
  proxy.addDateRange(queryParams.value, dateRange.value, 'EffectiveDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.level = undefined
  queryParams.value.status = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.taxRateId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    level: undefined,
    minAmount: undefined,
    maxAmount: undefined,
    rate: undefined,
    quickDeduction: undefined,
    effectiveDate: undefined
  }
  proxy.resetForm('hrTaxRateRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增个税税率'
}

function handleUpdate(row) {
  reset()
  const taxRateId = row.taxRateId || ids.value[0]
  getHrTaxRate(taxRateId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改个税税率'
  })
}

/** 查看 —— 使用独立查看弹窗 */
function handleView(row) {
  const taxRateId = row.taxRateId || ids.value[0]
  getHrTaxRate(taxRateId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrTaxRateRef'].validate(valid => {
    if (valid) {
      if (form.value.taxRateId != undefined) {
        updateHrTaxRate(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrTaxRate(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const taxRateIds = row.taxRateId || ids.value
  proxy.$modal.confirm('是否确认删除选中的个税税率？').then(function() {
    return delHrTaxRate(taxRateIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/taxRate/export', { ...queryParams.value }, `hrTaxRate_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-taxRate-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.hr-taxRate-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-taxRate-page .filter-card { padding:14px 20px 16px; }
.hr-taxRate-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-taxRate-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-taxRate-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-taxRate-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-taxRate-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-taxRate-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-taxRate-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-taxRate-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-taxRate-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-taxRate-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-taxRate-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-taxRate-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-taxRate-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-taxRate-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-taxRate-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-taxRate-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-taxRate-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-taxRate-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-taxRate-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-taxRate-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-taxRate-page .field .control :deep(.el-select) { width:100%; }
.hr-taxRate-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-taxRate-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-taxRate-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-taxRate-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-taxRate-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-taxRate-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-taxRate-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-taxRate-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-taxRate-page .btn-soft .el-icon { font-size:14px; }
.hr-taxRate-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-taxRate-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-taxRate-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-taxRate-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-taxRate-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-taxRate-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }

/* ===== Table ===== */
.hr-taxRate-page .table-wrap { overflow-x:auto; }
.hr-taxRate-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-taxRate-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-taxRate-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-taxRate-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-taxRate-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-taxRate-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-taxRate-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-taxRate-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-taxRate-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-taxRate-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-taxRate-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-taxRate-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.hr-taxRate-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-taxRate-page .badge.amber .dot { background:var(--amber-500); }
.hr-taxRate-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-taxRate-page .badge.blue .dot { background:var(--blue-500); }
.hr-taxRate-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-taxRate-page .badge.green .dot { background:var(--green-500); }
.hr-taxRate-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-taxRate-page .badge.red .dot { background:var(--red-500); }
.hr-taxRate-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-taxRate-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-taxRate-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-taxRate-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-taxRate-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-taxRate-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-taxRate-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-taxRate-page .pagination-container :deep(.el-pagination .btn-prev), .hr-taxRate-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-taxRate-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-taxRate-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-taxRate-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式 ===== */
.hr-taxRate-page .rd-page { max-height:60vh; overflow-y:auto; }
.hr-taxRate-page .rd-card { border:1px solid #ebeef5; border-radius:8px; margin-bottom:16px; overflow:hidden; animation:rdFadeIn 0.4s ease-out forwards; }
.hr-taxRate-page .rd-card-header { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; background:#f5f7fa; cursor:pointer; user-select:none; }
.hr-taxRate-page .rd-card-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:#303133; }
.hr-taxRate-page .rd-card-icon { display:flex; align-items:center; color:#409eff; }
.hr-taxRate-page .rd-collapse-btn { border:none; background:transparent; cursor:pointer; padding:4px; display:flex; align-items:center; color:#909399; transition:transform 0.2s; }
.hr-taxRate-page .rd-collapse-btn.is-collapsed { transform:rotate(-90deg); }
.hr-taxRate-page .rd-card-body { padding:16px; }
.hr-taxRate-page .rd-grid { display:grid; grid-template-columns:repeat(2, 1fr); gap:12px 24px; }
.hr-taxRate-page .rd-item { display:flex; flex-direction:column; gap:4px; }
.hr-taxRate-page .rd-item--full { grid-column:1 / -1; }
.hr-taxRate-page .rd-label { font-size:12px; color:#909399; }
.hr-taxRate-page .rd-value { font-size:14px; color:#303133; font-weight:500; }
.hr-taxRate-page .rd-detail-header { display:flex; align-items:center; gap:12px; }
.hr-taxRate-page .rd-detail-header-icon { width:36px; height:36px; border-radius:8px; background:#ecf5ff; display:flex; align-items:center; justify-content:center; color:#409eff; }
.hr-taxRate-page .rd-detail-header-main { flex:1; }
.hr-taxRate-page .rd-detail-header-title { font-size:16px; font-weight:600; color:#303133; }
.hr-taxRate-page .rd-detail-header-sub { display:flex; align-items:center; gap:12px; margin-top:4px; }
.hr-taxRate-page .rd-detail-header-divider { width:1px; height:12px; background:#dcdfe6; }
.hr-taxRate-page .rd-detail-header-no { font-size:13px; color:#909399; }
@keyframes rdFadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-taxRate-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-taxRate-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-taxRate-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
