<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="供应商" prop="supplierName">
        <el-input v-model="queryParams.supplierName" placeholder="供应商名称/编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="物料" prop="materialName">
        <el-input v-model="queryParams.materialName" placeholder="物料名称/编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="询价单号" prop="inquiryNo">
        <el-input v-model="queryParams.inquiryNo" placeholder="请输入询价单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:priceCompare:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table border v-loading="loading" :data="priceCompareList" :row-class-name="rowClassName" @sort-change="handleSortChange">
      <el-table-column label="询价单号" align="center" prop="inquiryNo" width="160" resizable sortable="custom" />
      <el-table-column label="供应商编号" align="center" prop="supplierCode" width="120" resizable show-overflow-tooltip />
      <el-table-column label="供应商名称" align="center" prop="supplierName" width="160" resizable show-overflow-tooltip />
      <el-table-column label="物料编号" align="center" prop="materialCode" width="130" resizable />
      <el-table-column label="物料名称" align="center" prop="materialName" width="150" resizable show-overflow-tooltip />
      <el-table-column label="规格型号" align="center" prop="specification" width="130" resizable show-overflow-tooltip />
      <el-table-column label="单位" align="center" prop="unit" width="70" resizable>
        <template #default="scope">
          <dict-tag :options="wms_unit" :value="scope.row.unit" />
        </template>
      </el-table-column>
      <el-table-column label="数量" align="right" prop="quantity" width="90" resizable />
      <el-table-column label="单价" align="right" prop="price" width="110" resizable sortable="custom">
        <template #default="scope">
          <span :class="{ 'rd-price-best': isLowestPrice(scope.row) }">{{ formatMoney(scope.row.price) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="报价日期" align="center" prop="quotationDate" width="110" resizable>
        <template #default="scope"><span>{{ parseTime(scope.row.quotationDate, '{y}-{m}-{d}') }}</span></template>
      </el-table-column>
      <el-table-column label="有效期" align="center" prop="validDate" width="110" resizable>
        <template #default="scope"><span>{{ parseTime(scope.row.validDate, '{y}-{m}-{d}') }}</span></template>
      </el-table-column>
      <el-table-column label="税率(%)" align="right" prop="taxRate" width="80" resizable />
      <el-table-column label="交货期(天)" align="center" prop="deliveryDays" width="90" resizable />
      <el-table-column label="付款方式" align="center" prop="paymentTerms" width="110" resizable show-overflow-tooltip>
        <template #default="scope">
          <dict-tag :options="wms_payment_method" :value="scope.row.paymentTerms" />
        </template>
      </el-table-column>
      <el-table-column label="中标" align="center" prop="isAward" width="70" fixed="right">
        <template #default="scope">
          <el-tag v-if="scope.row.isAward === '1'" type="success" size="small" effect="dark">中标</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="PriceCompare">
import { listPriceCompare, exportPriceCompare } from '@/api/pms/priceCompare'
import { formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { wms_unit, wms_payment_method } = proxy.useDict('wms_unit', 'wms_payment_method')

const priceCompareList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const lowestPriceMap = ref({})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  supplierName: undefined,
  materialName: undefined,
  inquiryNo: undefined,
  orderByColumn: undefined,
  isAsc: undefined
})

function getList() {
  loading.value = true
  listPriceCompare(queryParams.value).then(res => {
    priceCompareList.value = res.rows
    total.value = res.total
    computeLowestPrices()
    loading.value = false
  })
}

/** 计算每个物料+询价单组合的最低价 */
function computeLowestPrices() {
  const map = {}
  priceCompareList.value.forEach(row => {
    const key = row.inquiryNo + '_' + row.materialCode
    if (!map[key] || row.price < map[key]) {
      map[key] = row.price
    }
  })
  lowestPriceMap.value = map
}

/** 判断是否为最低价 */
function isLowestPrice(row) {
  const key = row.inquiryNo + '_' + row.materialCode
  return lowestPriceMap.value[key] !== undefined && row.price === lowestPriceMap.value[key]
}

/** 行样式：中标行高亮 */
function rowClassName({ row }) {
  if (row.isAward === '1') {
    return 'rd-award-row'
  }
  return ''
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSortChange(column) {
  queryParams.value.orderByColumn = column.prop
  queryParams.value.isAsc = column.order
  getList()
}

function handleExport() {
  proxy.download('pms/priceCompare/export', { ...queryParams.value }, `price_compare_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
:deep(.rd-award-row) {
  background-color: #f0f9eb;
}
:deep(.rd-award-row:hover > td) {
  background-color: #e8f5e0 !important;
}
.rd-price-best {
  color: #f56c6c;
  font-weight: bold;
}
</style>
