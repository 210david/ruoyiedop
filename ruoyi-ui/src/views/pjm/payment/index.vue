<template>
  <div class="app-container pjm-list-page pjm-payment-page">
    <!-- ===== 筛选卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field" style="grid-column: span 2">
          <label>选择项目</label>
          <div class="control">
            <el-input :model-value="selectedProjectLabel" placeholder="点击选择项目" readonly style="cursor:pointer" @click="openProjectPicker">
              <template #append><el-button icon="Search" @click.stop="openProjectPicker" /></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>收款单号</label>
          <div class="control">
            <el-input v-model="queryParams.paymentNo" placeholder="模糊搜索单号" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 回款汇总 ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track"><span class="tree-label">项目回款汇总</span></div>
        <span class="badge green" v-if="currentSummary">
          回款比例 {{ payRatio(currentSummary) }}
        </span>
      </div>

      <div class="table-wrap" v-if="!queryParams.projectId">
        <el-table v-loading="summaryLoading" :data="summaryList" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="项目编号" prop="projectNo" width="150" align="center">
            <template #default="scope"><span class="col-mono">{{ scope.row.projectNo }}</span></template>
          </el-table-column>
          <el-table-column label="项目名称" prop="projectName" min-width="220" show-overflow-tooltip />
          <el-table-column label="合同总额(元)" width="150" align="right" class-name="col-num">
            <template #default="scope"><span class="col-amt">{{ money(scope.row.contractAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="已回款(元)" width="150" align="right" class-name="col-num">
            <template #default="scope"><span class="col-amt">{{ money(scope.row.receivedAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="回款比例" width="170">
            <template #default="scope">
              <div class="pbar-wrap">
                <div class="pbar">
                  <div class="pbar-fill" :class="{ 'is-done': payRatioNum(scope.row) >= 100 }" :style="{ width: payRatio(scope.row) }"></div>
                </div>
                <span class="pbar-txt">{{ payRatio(scope.row) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="待回款(元)" width="150" align="right" class-name="col-num">
            <template #default="scope"><span class="col-amt">{{ money(pendingAmount(scope.row)) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="viewProject(scope.row)">明细</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="summaryTotal > 0" :total="summaryTotal" v-model:page="summaryQuery.pageNum" v-model:limit="summaryQuery.pageSize" @pagination="loadSummary" />
      </div>

      <!-- 单项目汇总卡片 -->
      <div class="stat-grid" v-else-if="currentSummary">
        <div class="stat-card is-brand">
          <div class="stat-label">合同总额</div>
          <div class="stat-value">¥{{ money(currentSummary.contractAmount) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已回款</div>
          <div class="stat-value">¥{{ money(currentSummary.receivedAmount) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">待回款</div>
          <div class="stat-value">¥{{ money(pendingAmount(currentSummary)) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">回款比例</div>
          <div class="stat-value">{{ payRatio(currentSummary) }}</div>
        </div>
      </div>
      <el-empty v-else description="该项目暂无回款数据" class="tree-empty" />
    </div>

    <!-- ===== 收款明细 ===== -->
    <div class="surface" v-if="queryParams.projectId">
      <div class="status-tabs">
        <div class="tabs-track"><span class="tree-label">收款明细</span></div>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pjm:payment:add']">登记收款</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleEdit()" v-hasPermi="['pjm:payment:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['pjm:payment:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_payment_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="paymentList" border class="app-table"
                  @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="收款单号" prop="paymentNo" key="paymentNo" :width="colWidth('paymentNo', 170)" resizable v-if="columns.paymentNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.paymentNo }}</span></template>
          </el-table-column>
          <el-table-column label="期次" prop="periodNo" key="periodNo" :width="colWidth('periodNo', 80)" resizable align="center" v-if="columns.periodNo.visible">
            <template #default="scope">{{ scope.row.periodNo ? '第' + scope.row.periodNo + '期' : '-' }}</template>
          </el-table-column>
          <el-table-column label="收款金额(元)" prop="amount" key="amount" :width="colWidth('amount', 140)" resizable align="right" class-name="col-num" v-if="columns.amount.visible">
            <template #default="scope"><span class="col-amt">{{ money(scope.row.amount) }}</span></template>
          </el-table-column>
          <el-table-column label="收款日期" prop="paymentDate" key="paymentDate" :width="colWidth('paymentDate', 120)" resizable align="center" v-if="columns.paymentDate.visible">
            <template #default="scope">{{ scope.row.paymentDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="付款方" prop="payer" key="payer" :min-width="colWidth('payer', 160)" resizable show-overflow-tooltip v-if="columns.payer.visible">
            <template #default="scope">{{ scope.row.payer || '-' }}</template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" key="remark" :min-width="colWidth('remark', 160)" resizable show-overflow-tooltip v-if="columns.remark.visible">
            <template #default="scope">{{ scope.row.remark || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['pjm:payment:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pjm:payment:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改弹框 ===== -->
    <el-dialog v-model="open" width="640px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Money /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="paymentRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>收款信息
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="所属项目" prop="projectId">
                    <el-input :model-value="formProjectLabel" placeholder="点击选择项目" readonly style="width:100%;cursor:pointer" @click="openFormProjectPicker">
                      <template #append><el-button icon="Search" @click.stop="openFormProjectPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="期次">
                    <el-input-number v-model="form.periodNo" :min="1" :max="99" controls-position="right" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="收款金额" prop="amount">
                    <el-input-number v-model="form.amount" :min="0.01" :precision="2" controls-position="right" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="收款日期" prop="paymentDate">
                    <el-date-picker v-model="form.paymentDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="付款方">
                    <el-input v-model="form.payer" placeholder="付款单位名称（默认为业主单位）" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注">
                    <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="凭证号、付款说明等" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
    <ProjectPicker ref="formProjectPickerRef" @confirm="onFormProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmPayment">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPayment, summaryPayment, getPayment, addPayment, updatePayment, delPayment } from '@/api/pjm/payment'
import { listProject } from '@/api/pjm/project'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import ProjectPicker from '@/components/ProjectPicker/index.vue'

const { collapsedCards, toggleCard } = useDetailCard(['c1'])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_payment_index')

const loading = ref(false)
const summaryLoading = ref(false)
const paymentList = ref([])
const summaryList = ref([])
const total = ref(0)
const summaryTotal = ref(0)
const projectOptions = ref([])
const open = ref(false)
const title = ref('')
const paymentRef = ref()
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const currentSummary = ref(null)

const columns = ref({
  paymentNo: { label: '收款单号', visible: true },
  periodNo: { label: '期次', visible: true },
  amount: { label: '收款金额(元)', visible: true },
  paymentDate: { label: '收款日期', visible: true },
  payer: { label: '付款方', visible: true },
  remark: { label: '备注', visible: true }
})

const queryParams = reactive({ pageNum: 1, pageSize: 10, projectId: null, paymentNo: null })
const projectPickerRef = ref()
const selectedProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(queryParams.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openProjectPicker() { projectPickerRef.value?.open(queryParams.projectId) }
function onProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  queryParams.projectId = p.projectId
  onProjectChange()
}
const summaryQuery = reactive({ pageNum: 1, pageSize: 10 })
const form = ref({})
// 表单内项目浏览框
const formProjectPickerRef = ref()
const formProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(form.value.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openFormProjectPicker() { formProjectPickerRef.value?.open(form.value.projectId) }
function onFormProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  form.value.projectId = p.projectId
}
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  paymentDate: [{ required: true, message: '请选择收款日期', trigger: 'change' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.projectId) count++
  if (queryParams.paymentNo) count++
  return count
})

function money(v) {
  if (v == null) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function payRatioNum(row) {
  const c = Number(row?.contractAmount || 0)
  if (!c) return 0
  return Math.min(Number(row?.receivedAmount || 0) / c * 100, 100)
}
function payRatio(row) { return payRatioNum(row).toFixed(1) + '%' }
function pendingAmount(row) {
  const c = Number(row?.contractAmount || 0)
  return Math.max(c - Number(row?.receivedAmount || 0), 0)
}

function loadProjects() {
  listProject({ pageNum: 1, pageSize: 200 }).then(res => { projectOptions.value = res.rows || [] })
}

/** 汇总：无项目时列表视图；有项目时取单项目汇总 */
function loadSummary() {
  summaryLoading.value = true
  const q = queryParams.projectId
    ? { projectId: queryParams.projectId, pageNum: 1, pageSize: 1 }
    : summaryQuery
  summaryPayment(q).then(res => {
    if (queryParams.projectId) {
      currentSummary.value = (res.rows || [])[0] || null
    } else {
      summaryList.value = res.rows || []
      summaryTotal.value = res.total || 0
    }
    summaryLoading.value = false
  }).catch(() => { summaryLoading.value = false })
}

function getList() {
  if (!queryParams.projectId) { paymentList.value = []; total.value = 0; return }
  loading.value = true
  listPayment(queryParams).then(res => {
    paymentList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function onProjectChange() {
  queryParams.pageNum = 1
  currentSummary.value = null
  loadSummary()
  getList()
}

function viewProject(row) {
  queryParams.projectId = row.projectId
  queryParams.pageNum = 1
  loadSummary()
  getList()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.projectId = null
  queryParams.paymentNo = null
  currentSummary.value = null
  paymentList.value = []
  total.value = 0
  loadSummary()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.paymentId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  const proj = projectOptions.value.find(p => p.projectId === queryParams.projectId)
  form.value = {
    paymentId: null, projectId: queryParams.projectId, periodNo: null,
    amount: null, paymentDate: new Date().toISOString().slice(0, 10),
    payer: proj?.ownerUnit || proj?.customer || null, remark: null
  }
}

function handleAdd() {
  reset()
  title.value = '登记收款'
  open.value = true
}

function handleEdit(row) {
  const paymentId = row?.paymentId || ids.value[0]
  getPayment(paymentId).then(res => {
    form.value = res.data
    title.value = '修改收款记录'
    open.value = true
  })
}

function submitForm() {
  paymentRef.value.validate(valid => {
    if (!valid) return
    const action = form.value.paymentId ? updatePayment(form.value) : addPayment(form.value)
    action.then(() => {
      ElMessage.success(form.value.paymentId ? '修改成功' : '登记成功')
      open.value = false
      getList()
      loadSummary()
    })
  })
}

function handleDelete(row) {
  const paymentIds = row?.paymentId ? [row.paymentId] : ids.value
  ElMessageBox.confirm('确认删除该收款记录吗？', '提示', { type: 'warning' }).then(() => {
    delPayment(paymentIds.join(',')).then(() => {
      ElMessage.success('删除成功')
      getList()
      loadSummary()
    })
  }).catch(() => {})
}

onMounted(() => {
  loadProjects()
  loadSummary()
})
</script>

<style scoped>
.pjm-payment-page .tree-empty { padding: 24px 0; }
</style>
