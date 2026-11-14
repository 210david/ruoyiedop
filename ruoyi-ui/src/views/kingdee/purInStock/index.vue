<template>
  <div class="app-container kd-pi-page">
    <!-- 筛选条件 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>单据日期</label>
          <div class="control">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="handleQuery"
            />
          </div>
        </div>
        <div class="field">
          <label>单据编号</label>
          <div class="control">
            <el-input v-model="queryParams.billNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>商品名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Refresh" :loading="syncing" @click="handleSync" v-hasPermi="['kingdee:purinstock:sync']">
            同步金蝶
          </el-button>
          <el-button icon="RefreshRight" @click="getList">刷新</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="piList" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="单据日期" prop="billDate" width="115" align="center" show-overflow-tooltip />
          <el-table-column label="单据编号" prop="billNo" min-width="150" align="center" show-overflow-tooltip />
          <el-table-column label="供应商" prop="supplierName" min-width="200" align="center" show-overflow-tooltip />
          <el-table-column label="采购员" prop="purchaserName" width="110" align="center" show-overflow-tooltip />
          <el-table-column label="部门" prop="deptName" min-width="130" align="center" show-overflow-tooltip />
          <el-table-column label="付款状态" prop="payStatus" width="110" align="center" show-overflow-tooltip />
          <el-table-column label="未付款金额" prop="unpaidAmount" width="120" align="right">
            <template #default="scope">{{ money(scope.row.unpaidAmount) }}</template>
          </el-table-column>
          <el-table-column label="已付款金额" prop="paidAmount" width="120" align="right">
            <template #default="scope">{{ money(scope.row.paidAmount) }}</template>
          </el-table-column>
          <el-table-column label="商品名称" prop="materialName" min-width="200" align="center" show-overflow-tooltip />
          <el-table-column label="仓库" prop="warehouseName" min-width="120" align="center" show-overflow-tooltip />
          <el-table-column label="单位" prop="unitName" width="80" align="center" />
          <el-table-column label="数量" prop="qty" width="100" align="right">
            <template #default="scope">{{ money(scope.row.qty, 4) }}</template>
          </el-table-column>
          <el-table-column label="单价" prop="price" width="110" align="right">
            <template #default="scope">{{ money(scope.row.price, 4) }}</template>
          </el-table-column>
          <el-table-column label="税额" prop="taxAmount" width="110" align="right">
            <template #default="scope">{{ money(scope.row.taxAmount) }}</template>
          </el-table-column>
          <el-table-column label="价税合计" prop="totalAmount" width="120" align="right">
            <template #default="scope">{{ money(scope.row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column label="退货数量" prop="returnQty" width="100" align="right">
            <template #default="scope">{{ money(scope.row.returnQty, 4) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script setup name="KdPurInStock">
import { listPurInStock, syncPurInStock } from '@/api/kingdee/purInStock'
import { Filter } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const piList = ref([])
const loading = ref(false)
const syncing = ref(false)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  billNo: undefined,
  supplierName: undefined,
  materialName: undefined,
  params: {}
})

/** 查询列表 */
function getList() {
  loading.value = true
  queryParams.value.params = {
    beginBillDate: dateRange.value && dateRange.value[0] ? dateRange.value[0] : undefined,
    endBillDate: dateRange.value && dateRange.value[1] ? dateRange.value[1] : undefined
  }
  listPurInStock(queryParams.value).then(response => {
    piList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 搜索 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  dateRange.value = []
  queryParams.value.billNo = undefined
  queryParams.value.supplierName = undefined
  queryParams.value.materialName = undefined
  handleQuery()
}

/** 从金蝶同步 */
function handleSync() {
  proxy.$modal.confirm('确认从金蝶云星辰同步采购入库单？数据按单据编号幂等写入。').then(() => {
    syncing.value = true
    syncPurInStock().then(res => {
      const data = res.data || {}
      const stat = data.statistics || {}
      syncing.value = false
      proxy.$modal.msgSuccess(`同步完成：共 ${stat.total || 0} 行，写入 ${stat.inserted || 0} 行，跳过 ${stat.skipped || 0} 行`)
      const reasons = data.skipReasons || []
      if (reasons.length) {
        proxy.$modal.alert(reasons.join('\n'), '同步提示')
      }
      getList()
    }).catch(() => {
      syncing.value = false
    })
  }).catch(() => {})
}

/** 金额/数量格式化 */
function money(val, digits = 2) {
  if (val === null || val === undefined || val === '') {
    return ''
  }
  const num = Number(val)
  return isNaN(num) ? val : num.toFixed(digits)
}

getList()
</script>

<style scoped>
.kd-pi-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --r-sm:6px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

.kd-pi-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* 筛选卡片 */
.kd-pi-page .filter-card { padding:14px 20px 16px; }
.kd-pi-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.kd-pi-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.kd-pi-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.kd-pi-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.kd-pi-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.kd-pi-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.kd-pi-page .filter-card .filter-buttons { display:flex; gap:8px; }

.kd-pi-page .field { display:flex; flex-direction:column; gap:6px; }
.kd-pi-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.kd-pi-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s, box-shadow .15s; }
.kd-pi-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.kd-pi-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.kd-pi-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.kd-pi-page .field .control :deep(.el-date-editor) { width:100%; }
.kd-pi-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.kd-pi-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }

/* 工具栏与表格 */
.kd-pi-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.kd-pi-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.kd-pi-page .table-wrap { overflow-x:auto; }
.kd-pi-page .app-table { --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.kd-pi-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 12px; border-bottom:1px solid var(--ink-200); white-space:nowrap; }
.kd-pi-page .app-table :deep(.el-table__header th .cell) { white-space:nowrap; overflow:visible; text-overflow:clip; }
.kd-pi-page .app-table :deep(.el-table__body td) { padding:12px 12px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); white-space:nowrap; }
.kd-pi-page .app-table :deep(.el-table__body td .cell) { white-space:nowrap; }
.kd-pi-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }

@media (max-width:1100px) { .kd-pi-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .kd-pi-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
