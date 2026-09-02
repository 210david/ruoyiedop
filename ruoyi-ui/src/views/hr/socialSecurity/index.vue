<template>
  <div class="app-container hr-socialsecurity-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>年度</label>
          <div class="control">
            <el-input v-model="queryParams.year" placeholder="请输入年度" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:socialSecurity:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:socialSecurity:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:socialSecurity:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:socialSecurity:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_socialsecurity_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrSocialSecurityList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 110)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="年度" prop="year" key="year" align="center" :width="colWidth('year', 90)" resizable v-if="columns.year.visible" />
          <el-table-column label="缴费基数" prop="baseAmount" key="baseAmount" align="center" :width="colWidth('baseAmount', 130)" resizable v-if="columns.baseAmount.visible" />
          <el-table-column label="养老单位比例" prop="pensionEmployer" key="pensionEmployer" align="center" :width="colWidth('pensionEmployer', 130)" resizable v-if="columns.pensionEmployer.visible" />
          <el-table-column label="养老个人比例" prop="pensionEmployee" key="pensionEmployee" align="center" :width="colWidth('pensionEmployee', 130)" resizable v-if="columns.pensionEmployee.visible" />
          <el-table-column label="医疗单位比例" prop="medicalEmployer" key="medicalEmployer" align="center" :width="colWidth('medicalEmployer', 130)" resizable v-if="columns.medicalEmployer.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:socialSecurity:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:socialSecurity:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:socialSecurity:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrSocialSecurityRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 卡片：基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入工号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="年度" prop="year"><el-input-number v-model="form.year" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：缴纳基数 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>缴纳基数</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="缴费基数" prop="baseAmount"><el-input-number v-model="form.baseAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：缴纳比例 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></span>缴纳比例</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="养老单位比例" prop="pensionEmployer"><el-input-number v-model="form.pensionEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="养老个人比例" prop="pensionEmployee"><el-input-number v-model="form.pensionEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="医疗单位比例" prop="medicalEmployer"><el-input-number v-model="form.medicalEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="医疗个人比例" prop="medicalEmployee"><el-input-number v-model="form.medicalEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="失业单位比例" prop="unemploymentEmployer"><el-input-number v-model="form.unemploymentEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="失业个人比例" prop="unemploymentEmployee"><el-input-number v-model="form.unemploymentEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工伤单位比例" prop="injuryEmployer"><el-input-number v-model="form.injuryEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="生育单位比例" prop="maternityEmployer"><el-input-number v-model="form.maternityEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="公积金单位比例" prop="fundEmployer"><el-input-number v-model="form.fundEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="公积金个人比例" prop="fundEmployee"><el-input-number v-model="form.fundEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
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

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">社保公积金详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.employeeNo || viewData.year">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">工号：{{ viewData.employeeNo || '-' }}</span>
              <span class="rd-detail-header-no">年度：{{ viewData.year || '-' }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">年度</span><div class="rd-value">{{ viewData.year != null ? viewData.year : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>缴纳基数</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">缴费基数</span><div class="rd-value">{{ viewData.baseAmount != null ? viewData.baseAmount : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></span>缴纳比例</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">养老单位比例</span><div class="rd-value">{{ viewData.pensionEmployer != null ? viewData.pensionEmployer : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">养老个人比例</span><div class="rd-value">{{ viewData.pensionEmployee != null ? viewData.pensionEmployee : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">医疗单位比例</span><div class="rd-value">{{ viewData.medicalEmployer != null ? viewData.medicalEmployer : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">医疗个人比例</span><div class="rd-value">{{ viewData.medicalEmployee != null ? viewData.medicalEmployee : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">失业单位比例</span><div class="rd-value">{{ viewData.unemploymentEmployer != null ? viewData.unemploymentEmployer : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">失业个人比例</span><div class="rd-value">{{ viewData.unemploymentEmployee != null ? viewData.unemploymentEmployee : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工伤单位比例</span><div class="rd-value">{{ viewData.injuryEmployer != null ? viewData.injuryEmployer : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">生育单位比例</span><div class="rd-value">{{ viewData.maternityEmployer != null ? viewData.maternityEmployer : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">公积金单位比例</span><div class="rd-value">{{ viewData.fundEmployer != null ? viewData.fundEmployer : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">公积金个人比例</span><div class="rd-value">{{ viewData.fundEmployee != null ? viewData.fundEmployee : '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 人员选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
  </div>
</template>

<script setup name="HrSocialSecurity">
import { listHrSocialSecurity, getHrSocialSecurity, addHrSocialSecurity, updateHrSocialSecurity, delHrSocialSecurity } from '@/api/hr/hrSocialSecurity'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'

const { proxy } = getCurrentInstance()

const hrSocialSecurityList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const collapsedCards = reactive({ c0: false, c1: false, c2: false })

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_socialsecurity_index')

/** 列显隐配置 */
const defaultColumns = {
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  year: { label: '年度', visible: true },
  baseAmount: { label: '缴费基数', visible: true },
  pensionEmployer: { label: '养老单位比例', visible: true },
  pensionEmployee: { label: '养老个人比例', visible: true },
  medicalEmployer: { label: '医疗单位比例', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_socialsecurity_columns')
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

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.year) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeId: undefined, employeeName: undefined, employeeNo: undefined, year: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    year: [{ required: true, message: '年度不能为空', trigger: 'blur' }],
    baseAmount: [{ required: true, message: '缴费基数不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrSocialSecurity(queryParams.value).then(response => {
    hrSocialSecurityList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.year = undefined
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.ssId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    year: undefined,
    baseAmount: undefined,
    pensionEmployer: undefined,
    pensionEmployee: undefined,
    medicalEmployer: undefined,
    medicalEmployee: undefined,
    unemploymentEmployer: undefined,
    unemploymentEmployee: undefined,
    injuryEmployer: undefined,
    maternityEmployer: undefined,
    fundEmployer: undefined,
    fundEmployee: undefined
  }
  proxy.resetForm('hrSocialSecurityRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增社保公积金'
}

function handleUpdate(row) {
  reset()
  const ssId = row.ssId || ids.value[0]
  getHrSocialSecurity(ssId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改社保公积金'
  })
}

function handleView(row) {
  const ssId = row.ssId
  getHrSocialSecurity(ssId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrSocialSecurityRef'].validate(valid => {
    if (valid) {
      if (form.value.ssId != undefined) {
        updateHrSocialSecurity(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrSocialSecurity(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const ssIds = row.ssId || ids.value
  proxy.$modal.confirm('是否确认删除选中的社保公积金？').then(function() {
    return delHrSocialSecurity(ssIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/socialSecurity/export', { ...queryParams.value }, `hrSocialSecurity_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** ===== 人员弹框选择 ===== */
const employeePickerRef = ref(null)
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName };
  Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k]; });
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-socialsecurity-page {
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
.hr-socialsecurity-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-socialsecurity-page .filter-card { padding:14px 20px 16px; }
.hr-socialsecurity-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-socialsecurity-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-socialsecurity-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-socialsecurity-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-socialsecurity-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-socialsecurity-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-socialsecurity-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-socialsecurity-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-socialsecurity-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-socialsecurity-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-socialsecurity-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-socialsecurity-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-socialsecurity-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-socialsecurity-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-socialsecurity-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-socialsecurity-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-socialsecurity-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-socialsecurity-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-socialsecurity-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-socialsecurity-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-socialsecurity-page .field .control :deep(.el-select) { width:100%; }
.hr-socialsecurity-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-socialsecurity-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-socialsecurity-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-socialsecurity-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-socialsecurity-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-socialsecurity-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-socialsecurity-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-socialsecurity-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-socialsecurity-page .btn-soft .el-icon { font-size:14px; }
.hr-socialsecurity-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-socialsecurity-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-socialsecurity-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-socialsecurity-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-socialsecurity-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-socialsecurity-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-socialsecurity-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-socialsecurity-page .table-wrap { overflow-x:auto; }
.hr-socialsecurity-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-socialsecurity-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-socialsecurity-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-socialsecurity-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-socialsecurity-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-socialsecurity-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-socialsecurity-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-socialsecurity-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-socialsecurity-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-socialsecurity-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-socialsecurity-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-socialsecurity-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-socialsecurity-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-socialsecurity-page .badge.amber .dot { background:var(--amber-500); }
.hr-socialsecurity-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-socialsecurity-page .badge.blue .dot { background:var(--blue-500); }
.hr-socialsecurity-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-socialsecurity-page .badge.green .dot { background:var(--green-500); }
.hr-socialsecurity-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-socialsecurity-page .badge.red .dot { background:var(--red-500); }
.hr-socialsecurity-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-socialsecurity-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-socialsecurity-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-socialsecurity-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .btn-prev), .hr-socialsecurity-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-socialsecurity-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式（遵循 detail-page-style-guide 规范） ===== */
.hr-socialsecurity-page .rd-page { max-height: 62vh; overflow-y: auto; }
.hr-socialsecurity-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-socialsecurity-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-socialsecurity-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-socialsecurity-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-socialsecurity-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-socialsecurity-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-socialsecurity-page .rd-card-body { padding: 16px; }
.hr-socialsecurity-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-socialsecurity-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-socialsecurity-page .rd-item--full { grid-column: 1 / -1; }
.hr-socialsecurity-page .rd-label { font-size: 12px; color: #909399; }
.hr-socialsecurity-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-socialsecurity-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-socialsecurity-page .rd-empty-icon { margin-bottom: 8px; }
.hr-socialsecurity-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-socialsecurity-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-socialsecurity-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-socialsecurity-page .rd-detail-header-main { flex: 1; }
.hr-socialsecurity-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-socialsecurity-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-socialsecurity-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-socialsecurity-page .rd-detail-header-no { font-size: 13px; color: #909399; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.hr-socialsecurity-page .rd-card { animation: rdFadeIn 0.4s ease-out forwards; }
.hr-socialsecurity-page .rd-card:nth-child(2) { animation-delay: 0.06s; }
.hr-socialsecurity-page .rd-card:nth-child(3) { animation-delay: 0.12s; }
.hr-socialsecurity-page .rd-card:nth-child(4) { animation-delay: 0.18s; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-socialsecurity-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-socialsecurity-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-socialsecurity-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
