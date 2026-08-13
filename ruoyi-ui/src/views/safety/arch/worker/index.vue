<template>
  <div class="app-container safety-worker-page">
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
          <label>姓名</label>
          <div class="control">
            <el-input v-model="queryParams.workerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>岗位</label>
          <div class="control">
            <el-input v-model="queryParams.position" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>作业类别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.jobType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_job_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.workerNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:worker:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:worker:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:worker:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_worker_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="workerList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="姓名" prop="workerName" key="workerName" :width="colWidth('workerName', 120)" resizable v-if="columns.workerName.visible" />
          <el-table-column label="工号" prop="workerNo" key="workerNo" :width="colWidth('workerNo', 120)" resizable v-if="columns.workerNo.visible" />
          <el-table-column label="所属部门" prop="deptName" key="deptName" :width="colWidth('deptName', 140)" resizable show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="岗位" prop="position" key="position" :width="colWidth('position', 130)" resizable show-overflow-tooltip v-if="columns.position.visible" />
          <el-table-column label="联系电话" prop="phone" key="phone" :width="colWidth('phone', 130)" resizable v-if="columns.phone.visible" />
          <el-table-column label="作业类别" prop="jobType" key="jobType" :width="colWidth('jobType', 140)" resizable show-overflow-tooltip v-if="columns.jobType.visible">
            <template #default="scope"><span class="badge blue">{{ jobTypeLabel(scope.row.jobType) }}</span></template>
          </el-table-column>
          <el-table-column label="作业证号" prop="certNo" key="certNo" :width="colWidth('certNo', 130)" resizable v-if="columns.certNo.visible" />
          <el-table-column label="证书有效期至" prop="validTo" key="validTo" :width="colWidth('validTo', 140)" resizable align="center" sortable="custom" v-if="columns.validTo.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.remindStatus === '1' ? 'red' : 'green'">
                <span class="dot"></span>{{ scope.row.validTo }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:worker:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:worker:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:worker:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="workerRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>特种人员信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="姓名" prop="workerName"><el-input v-model="form.workerName" placeholder="请输入姓名" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="workerNo"><el-input v-model="form.workerNo" placeholder="请输入工号" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                  <el-input v-model="form.deptName" readonly placeholder="请选择部门" style="width: 100%" @click="openDeptPicker">
                    <template #append><el-button icon="Search" @click="openDeptPicker" /></template>
                    <template #suffix><el-icon v-if="form.deptName" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="岗位" prop="position"><el-input v-model="form.position" placeholder="请输入岗位" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" placeholder="请输入联系电话" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><rect x="3" y="4" width="18" height="16" rx="2"/></svg></span>证件信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="作业类别" prop="jobType"><el-select v-model="form.jobType" placeholder="请选择作业类别" clearable style="width: 100%"><el-option v-for="dict in safety_job_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="作业证号" prop="certNo"><el-input v-model="form.certNo" placeholder="请输入作业证号" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="发证日期" prop="certIssueDate"><el-date-picker v-model="form.certIssueDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="有效期至" prop="validTo"><el-date-picker v-model="form.validTo" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <span class="rd-detail-header-title">特种人员详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.workerNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">工号：{{ viewData.workerNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>特种人员信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ viewData.workerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.workerNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">岗位</span><div class="rd-value">{{ viewData.position || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.phone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="viewData.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ viewData.status === '0' ? '正常' : '停用' }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.jobType || viewData.certNo || viewData.certIssueDate || viewData.validTo">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><rect x="3" y="4" width="18" height="16" rx="2"/></svg></span>证件信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">作业类别</span><div class="rd-value"><dict-tag :options="safety_job_type" :value="viewData.jobType" /></div></div>
              <div class="rd-item"><span class="rd-label">作业证号</span><div class="rd-value">{{ viewData.certNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发证日期</span><div class="rd-value">{{ viewData.certIssueDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">有效期至</span><div class="rd-value"><span class="badge" :class="viewData.remindStatus === '1' ? 'red' : 'green'"><span class="dot"></span>{{ viewData.validTo || '-' }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />
  </div>
</template>

<script setup name="SafetyWorker">
import { listWorker, getWorker, addWorker, updateWorker, delWorker } from '@/api/safety/worker'
import DeptPicker from '@/components/DeptPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_job_type } = proxy.useDict('safety_job_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_worker_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])

const workerList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  workerName: { label: '姓名', visible: true },
  workerNo: { label: '工号', visible: true },
  deptName: { label: '所属部门', visible: true },
  position: { label: '岗位', visible: true },
  phone: { label: '联系电话', visible: true },
  jobType: { label: '作业类别', visible: true },
  certNo: { label: '作业证号', visible: true },
  validTo: { label: '证书有效期至', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_worker_columns')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, workerName: undefined, position: undefined, jobType: undefined, workerNo: undefined, status: undefined, params: {} },
  rules: {
    workerName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    deptId: [{ required: true, message: '所属部门不能为空', trigger: 'change' }],
    jobType: [{ required: true, message: '作业类别不能为空', trigger: 'change' }],
    certNo: [{ required: true, message: '作业证号不能为空', trigger: 'blur' }],
    certIssueDate: [{ required: true, message: '发证日期不能为空', trigger: 'change' }],
    validTo: [{ required: true, message: '有效期不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.workerName) count++
  if (queryParams.value.position) count++
  if (queryParams.value.jobType) count++
  if (queryParams.value.workerNo) count++
  if (queryParams.value.status) count++
  return count
})

function getList() { loading.value = true; listWorker(queryParams.value).then(response => { workerList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.workerName = undefined; queryParams.value.position = undefined; queryParams.value.jobType = undefined; queryParams.value.workerNo = undefined; queryParams.value.status = undefined; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.workerId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = '添加特种人员' }
function handleView(row) { const workerId = row.workerId || ids.value[0]; getWorker(workerId).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { reset(); getWorker(row.workerId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.jobType && !response.data.certNo && !response.data.certIssueDate && !response.data.validTo; collapsedCards.c2 = !response.data.remark; open.value = true; title.value = '修改特种人员' }) }
function submitForm() {
  proxy.$refs['workerRef'].validate(valid => {
    if (valid) {
      if (form.value.workerId != undefined) { updateWorker(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addWorker(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const workerIds = row.workerId || ids.value; proxy.$modal.confirm('是否确认删除选中的特种人员？').then(function() { return delWorker(workerIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/worker/export', { ...queryParams.value }, `worker_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { workerId: undefined, workerName: undefined, workerNo: undefined, deptId: undefined, deptName: undefined, position: undefined, phone: undefined, isSpecial: '1', jobType: undefined, certNo: undefined, certIssueDate: undefined, validTo: undefined, status: '0', remark: undefined }
  proxy.resetForm('workerRef')
}
/** 打开部门选择弹窗 */
function openDeptPicker() { proxy.$refs.deptPickerRef.open(form.value.deptId) }
/** 部门选择确认回调 */
function onDeptPickerConfirm(dept) { form.value.deptId = dept.deptId; form.value.deptName = dept.deptName }
/** 清除部门 */
function clearDept() { form.value.deptId = undefined; form.value.deptName = undefined }
/** 作业类别字典翻译 */
function jobTypeLabel(type) { const item = safety_job_type.value.find(d => d.value == type); return item ? item.label : '-' }

getList()
</script>

<style scoped>
.safety-worker-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-worker-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-worker-page .filter-card { padding:14px 20px 16px; }
.safety-worker-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-worker-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-worker-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-worker-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-worker-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-worker-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-worker-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-worker-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-worker-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-worker-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-worker-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-worker-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-worker-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-worker-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-worker-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-worker-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-worker-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-worker-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-worker-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-worker-page .field .control :deep(.el-select) { width:100%; }
.safety-worker-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-worker-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-worker-page .toolbar .left, .safety-worker-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-worker-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-worker-page .table-wrap { overflow-x:auto; }
.safety-worker-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-worker-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-worker-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-worker-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-worker-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-worker-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-worker-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-worker-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-worker-page .badge.amber .dot { background:var(--amber-500); }
.safety-worker-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-worker-page .badge.blue .dot { background:var(--blue-500); }
.safety-worker-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-worker-page .badge.green .dot { background:var(--green-500); }
.safety-worker-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-worker-page .badge.red .dot { background:var(--red-500); }
.safety-worker-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-worker-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-worker-page .badge.gray .dot { background:var(--ink-400); }
.safety-worker-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-worker-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-worker-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
</style>
