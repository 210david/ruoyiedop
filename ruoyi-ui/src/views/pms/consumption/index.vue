<template>
  <div class="app-container pms-consumption-page">
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
          <label>耗用单号</label>
          <div class="control">
            <el-input v-model="queryParams.consumptionNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>采购订单号</label>
          <div class="control">
            <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="待登记" value="0" />
              <el-option label="已登记" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>使用部门</label>
          <div class="control">
            <el-input v-model="queryParams.deptName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>领用人</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button class="status-tab tab-warn" :class="{ 'is-active': activeStatusTab === '0' }" @click="handleStatusTabClick('0')">
            <span class="dot"></span><span>待登记</span><span class="count">{{ statusCounts['0'] || 0 }}</span>
          </button>
          <button class="status-tab tab-done" :class="{ 'is-active': activeStatusTab === '1' }" @click="handleStatusTabClick('1')">
            <span class="dot"></span><span>已登记</span><span class="count">{{ statusCounts['1'] || 0 }}</span>
          </button>
        </div>
      </div>

      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['pms:consumption:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <span class="toolbar-tip">零散采购收货验收通过后自动生成耗用登记，不入库，直接交付使用</span>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_consumption_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="consumptionList" border class="app-table"
                  @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="耗用单号" prop="consumptionNo" key="consumptionNo" :width="colWidth('consumptionNo', 170)" resizable v-if="columns.consumptionNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.consumptionNo }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '1' ? 'green' : 'amber'">
                <span class="dot"></span>{{ scope.row.status === '1' ? '已登记' : '待登记' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="采购订单号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 160)" resizable v-if="columns.orderNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.orderNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="收货单号" prop="receiveNo" key="receiveNo" :width="colWidth('receiveNo', 160)" resizable v-if="columns.receiveNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.receiveNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :min-width="colWidth('supplierName', 160)" resizable show-overflow-tooltip v-if="columns.supplierName.visible">
            <template #default="scope">{{ scope.row.supplierName || '-' }}</template>
          </el-table-column>
          <el-table-column label="耗用数量" prop="totalQty" key="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" class-name="col-num" v-if="columns.totalQty.visible" />
          <el-table-column label="耗用金额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="right" v-if="columns.totalAmount.visible">
            <template #default="scope"><span class="col-amt">{{ formatMoney(scope.row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="使用部门" prop="deptName" key="deptName" :width="colWidth('deptName', 140)" resizable show-overflow-tooltip v-if="columns.deptName.visible">
            <template #default="scope">{{ scope.row.deptName || '-' }}</template>
          </el-table-column>
          <el-table-column label="领用人" prop="userName" key="userName" :width="colWidth('userName', 100)" resizable align="center" v-if="columns.userName.visible">
            <template #default="scope">{{ scope.row.userName || '-' }}</template>
          </el-table-column>
          <el-table-column label="使用日期" prop="useDate" key="useDate" :width="colWidth('useDate', 120)" resizable align="center" v-if="columns.useDate.visible">
            <template #default="scope">{{ scope.row.useDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleRegister(scope.row)" v-hasPermi="['pms:consumption:edit']">
                  {{ scope.row.status === '1' ? '修改' : '登记' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 登记弹框 ===== -->
    <el-dialog v-model="registerOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Box /></el-icon></div>
          <span class="rd-detail-header-title">{{ registerTitle }}</span>
        </div>
      </template>
      <el-form ref="registerRef" :model="registerForm" :rules="registerRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>耗用信息</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="耗用单号">
                    <el-input v-model="registerForm.consumptionNo" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="使用日期" prop="useDate">
                    <el-date-picker v-model="registerForm.useDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="领用人" prop="userName">
                    <el-input v-model="registerForm.userName" readonly placeholder="请从组织架构选择" @click="openUserPicker">
                      <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                      <template #suffix>
                        <el-icon v-if="registerForm.userName" style="cursor:pointer" @click.stop="clearUser"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="使用部门" prop="deptName">
                    <el-input v-model="registerForm.deptName" readonly placeholder="选择领用人后自动带出" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="用途说明" prop="usagePurpose">
                    <el-input v-model="registerForm.usagePurpose" type="textarea" :rows="2" placeholder="请输入耗用用途，如：三楼办公区维修更换配件" />
                  </el-form-item>
                </el-col>
              </el-row>
              <!-- 耗用明细 -->
              <el-table :data="registerForm.detailList || []" border size="small" class="detail-table">
                <el-table-column type="index" label="#" width="50" align="center" />
                <el-table-column label="物料编码" prop="materialCode" width="120" align="center" />
                <el-table-column label="物料名称" prop="materialName" min-width="140" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="specModel" min-width="110" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="70" align="center" />
                <el-table-column label="数量" prop="qty" width="90" align="right" />
                <el-table-column label="单价" prop="unitPrice" width="100" align="right" />
                <el-table-column label="金额" prop="amount" width="110" align="right" />
              </el-table>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="registerOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitRegister">确 定</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看弹框 ===== -->
    <el-dialog v-model="viewOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Box /></el-icon></div>
          <span class="rd-detail-header-title">耗用登记详情</span>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>基本信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">耗用单号</span><div class="rd-value">{{ viewData.consumptionNo }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><el-tag :type="viewData.status === '1' ? 'success' : 'warning'" size="small">{{ viewData.status === '1' ? '已登记' : '待登记' }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">采购订单号</span><div class="rd-value">{{ viewData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">收货单号</span><div class="rd-value">{{ viewData.receiveNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">使用日期</span><div class="rd-value">{{ viewData.useDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">使用部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">领用人</span><div class="rd-value">{{ viewData.userName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">耗用数量</span><div class="rd-value">{{ viewData.totalQty }}</div></div>
              <div class="rd-item"><span class="rd-label">耗用金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.totalAmount) }}</div></div>
              <div class="rd-item" style="grid-column: span 2"><span class="rd-label">用途说明</span><div class="rd-value">{{ viewData.usagePurpose || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><List /></el-icon></span>耗用明细</div>
          </div>
          <div class="rd-card-body">
            <el-table :data="viewData.detailList || []" border size="small">
              <el-table-column type="index" label="#" width="50" align="center" />
              <el-table-column label="物料编码" prop="materialCode" width="120" align="center" />
              <el-table-column label="物料名称" prop="materialName" min-width="140" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="110" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="70" align="center" />
              <el-table-column label="数量" prop="qty" width="90" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="100" align="right" />
              <el-table-column label="金额" prop="amount" width="110" align="right" />
            </el-table>
          </div>
        </section>
      </div>
    </el-dialog>

    <!-- 领用人选择（组织架构） -->
    <UserPicker ref="userPickerRef" title="选择领用人" @confirm="onUserConfirm" />
  </div>
</template>

<script setup name="PmsConsumption">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose } from '@element-plus/icons-vue'
import UserPicker from '@/components/UserPicker/index.vue'
import { listConsumption, getConsumption, updateConsumption, delConsumption } from '@/api/pms/consumption'
import { useColumnResize } from '@/composables/useColumnResize'

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_consumption_index')

const loading = ref(false)
const consumptionList = ref([])
const total = ref(0)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const multiple = ref(true)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0 })

const registerOpen = ref(false)
const registerTitle = ref('耗用登记')
const registerRef = ref()
const registerForm = ref({})
const viewOpen = ref(false)
const viewData = ref(null)
const userPickerRef = ref()

const columns = ref({
  consumptionNo: { label: '耗用单号', visible: true },
  status: { label: '状态', visible: true },
  orderNo: { label: '采购订单号', visible: true },
  receiveNo: { label: '收货单号', visible: true },
  supplierName: { label: '供应商', visible: true },
  totalQty: { label: '耗用数量', visible: true },
  totalAmount: { label: '耗用金额', visible: true },
  deptName: { label: '使用部门', visible: true },
  userName: { label: '领用人', visible: true },
  useDate: { label: '使用日期', visible: true }
})

const queryParams = reactive({
  pageNum: 1, pageSize: 10,
  consumptionNo: undefined, orderNo: undefined, status: undefined,
  deptName: undefined, userName: undefined
})

const registerRules = {
  userName: [{ required: true, message: '请选择领用人', trigger: 'change' }],
  deptName: [{ required: true, message: '使用部门不能为空', trigger: 'change' }],
  useDate: [{ required: true, message: '请选择使用日期', trigger: 'change' }],
  usagePurpose: [{ required: true, message: '请输入用途说明', trigger: 'blur' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.consumptionNo) count++
  if (queryParams.orderNo) count++
  if (queryParams.status) count++
  if (queryParams.deptName) count++
  if (queryParams.userName) count++
  return count
})

function formatMoney(v) {
  if (v == null) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getList() {
  loading.value = true
  listConsumption(queryParams).then(res => {
    consumptionList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
    loadStatusCounts()
  }).catch(() => { loading.value = false })
}

function loadStatusCounts() {
  listConsumption({ pageNum: 1, pageSize: 1, status: '0' }).then(res => {
    statusCounts.value['0'] = res.total || 0
    statusCounts.value.all = statusCounts.value['0'] + statusCounts.value['1']
  }).catch(() => {})
  listConsumption({ pageNum: 1, pageSize: 1, status: '1' }).then(res => {
    statusCounts.value['1'] = res.total || 0
    statusCounts.value.all = statusCounts.value['0'] + statusCounts.value['1']
  }).catch(() => {})
}

function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  queryParams.status = tab === 'all' ? undefined : tab
  queryParams.pageNum = 1
  getList()
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.consumptionNo = undefined
  queryParams.orderNo = undefined
  queryParams.status = undefined
  queryParams.deptName = undefined
  queryParams.userName = undefined
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.consumptionId)
  multiple.value = !selection.length
}

function handleView(row) {
  getConsumption(row.consumptionId).then(res => {
    viewData.value = res.data
    viewOpen.value = true
  })
}

function handleRegister(row) {
  getConsumption(row.consumptionId).then(res => {
    registerForm.value = res.data
    registerTitle.value = res.data.status === '1' ? '修改耗用登记' : '耗用登记'
    registerOpen.value = true
  })
}

/** 领用人选择（组织架构） */
function openUserPicker() {
  userPickerRef.value.open(registerForm.value.userId)
}
function onUserConfirm(u) {
  registerForm.value.userId = u.userId
  registerForm.value.userName = u.nickName
  registerForm.value.deptId = u.deptId
  registerForm.value.deptName = u.deptName
}
function clearUser() {
  registerForm.value.userId = null
  registerForm.value.userName = null
  registerForm.value.deptId = null
  registerForm.value.deptName = null
}

function submitRegister() {
  registerRef.value.validate(valid => {
    if (!valid) return
    const data = { ...registerForm.value, status: '1' }
    updateConsumption(data).then(() => {
      ElMessage.success('登记成功')
      registerOpen.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const consumptionIds = row?.consumptionId ? [row.consumptionId] : ids.value
  ElMessageBox.confirm('确认删除选中的耗用登记吗？', '提示', { type: 'warning' }).then(() => {
    delConsumption(consumptionIds.join(',')).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-consumption-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.pms-consumption-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.pms-consumption-page .filter-card { padding:14px 20px 16px; }
.pms-consumption-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-consumption-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-consumption-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-consumption-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-consumption-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-consumption-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-consumption-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-consumption-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-consumption-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-consumption-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-consumption-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.pms-consumption-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-consumption-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-consumption-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-consumption-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.pms-consumption-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-consumption-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-consumption-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }

/* el-select transparent inside .control */
.pms-consumption-page .field .control :deep(.el-select) { width:100%; }
.pms-consumption-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-consumption-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-consumption-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Status Tabs ===== */
.pms-consumption-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-consumption-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-consumption-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-consumption-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-consumption-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-consumption-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-consumption-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-consumption-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-consumption-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-consumption-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-consumption-page .status-tab.tab-warn .dot { background:var(--amber-500); }
.pms-consumption-page .status-tab.tab-warn .count { background:var(--amber-50); color:var(--amber-700); }
.pms-consumption-page .status-tab.is-active.tab-warn .count { background:var(--amber-500); color:#fff; }
.pms-consumption-page .status-tab.tab-done .dot { background:var(--green-500); }
.pms-consumption-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.pms-consumption-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }

/* ===== Toolbar ===== */
.pms-consumption-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-consumption-page .toolbar .left { display:flex; align-items:center; gap:8px; }
.pms-consumption-page .toolbar .right { display:flex; align-items:center; gap:8px; }
.pms-consumption-page .toolbar-tip { font-size:12px; color:var(--ink-500); }

/* Soft Button */
.pms-consumption-page .btn-soft { display:flex; align-items:center; gap:6px; padding:0 14px; height:32px; border-radius:var(--r-sm); font-size:13px; font-weight:500; cursor:pointer; transition:all .15s; border:1px solid transparent; }
.pms-consumption-page .btn-soft.is-danger-outline { background:#fff; border-color:#fecaca; color:var(--red-600); }
.pms-consumption-page .btn-soft.is-danger-outline:hover:not(:disabled) { border-color:var(--red-500); background:var(--red-50); }
.pms-consumption-page .btn-soft:disabled { opacity:0.5; cursor:not-allowed; }

/* ===== Table ===== */
.pms-consumption-page .table-wrap { overflow-x:auto; }
.pms-consumption-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-consumption-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-consumption-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-consumption-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-consumption-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-consumption-page .app-table :deep(.el-table__row td) { padding:12px 16px; border-bottom:1px solid var(--ink-100); white-space:nowrap; }
.pms-consumption-page .app-table :deep(.el-table__row:hover td) { background:#fafbff !important; }

/* Badge */
.pms-consumption-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-consumption-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-consumption-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-consumption-page .badge.amber .dot { background:var(--amber-500); }
.pms-consumption-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-consumption-page .badge.green .dot { background:var(--green-500); }

/* Mono / Amount / Num columns */
.pms-consumption-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:13px; color:var(--ink-700); letter-spacing:-.01em; }
.pms-consumption-page .app-table .col-amt { font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
.pms-consumption-page .app-table .col-num { font-variant-numeric:tabular-nums; font-weight:600; }

/* ===== Pagination ===== */
.pms-consumption-page .pagination-container { display:flex; justify-content:flex-end; padding:12px 16px; border-top:1px solid var(--ink-200); }
.pms-consumption-page .pagination-container :deep(.el-pagination) { font-weight:500; }
.pms-consumption-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; font-weight:500; }
.pms-consumption-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); color:#fff; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .pms-consumption-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-consumption-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-consumption-page .toolbar { flex-wrap:wrap; gap:10px; } .pms-consumption-page .status-tabs { padding:6px 8px; } }

/* 弹窗明细表格 */
.pms-consumption-page .detail-table { margin-top: 8px; }

/* 操作列按钮对齐 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* 弹窗底部 */
:deep(.rd-dialog .el-dialog__footer) { background: #fff; border-top: 1px solid #e5e7eb; padding: 16px 24px; }
</style>
