<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户编号" prop="customerNo">
        <el-input v-model="queryParams.customerNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="企业名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户等级" prop="customerLevel">
        <el-select v-model="queryParams.customerLevel" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_customer_level" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="success" plain icon="Pointer" :disabled="multiple" @click="handleBatchClaim" v-hasPermi="['marketing:customer:claim']">批量领取</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:customer:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-alert title="公海池中的客户暂无负责人，有权限的销售人员均可领取。领取后客户将自动分配给您。" type="info" :closable="false" class="mb8" />

    <el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="客户编号" prop="customerNo" width="150" />
      <el-table-column label="企业名称" prop="customerName" show-overflow-tooltip>
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">{{ scope.row.customerName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="客户等级" prop="customerLevel" width="100" align="center">
        <template #default="scope"><dict-tag :options="marketing_customer_level" :value="scope.row.customerLevel" /></template>
      </el-table-column>
      <el-table-column label="所属行业" prop="industry" width="120" align="center">
        <template #default="scope"><dict-tag :options="marketing_industry" :value="scope.row.industry" /></template>
      </el-table-column>
      <el-table-column label="客户状态" prop="customerStatus" width="100" align="center">
        <template #default="scope"><dict-tag :options="marketing_customer_status" :value="scope.row.customerStatus" /></template>
      </el-table-column>
      <el-table-column label="累计交易额" prop="totalAmount" width="120" align="right" />
      <el-table-column label="最后更新" prop="updateTime" width="160" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="success" icon="Pointer" @click="handleClaim(scope.row)" v-hasPermi="['marketing:customer:claim']">领取</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="MkCustomerPool">
import { useRouter } from 'vue-router'
import { listPublicPool, claimCustomer } from '@/api/mk/customer'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { marketing_customer_level, marketing_industry, marketing_customer_status } = proxy.useDict('marketing_customer_level', 'marketing_industry', 'marketing_customer_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, customerNo: undefined, customerName: undefined, customerLevel: undefined }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listPublicPool(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.customerId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleView(row) { router.push('/mk/customer-detail/' + row.customerId) }
function handleExport() { proxy.download('mk/customer/export', { ...queryParams.value }, `public_pool_${new Date().getTime()}.xlsx`) }

function handleClaim(row) {
  proxy.$modal.confirm('确认领取"' + row.customerName + '"？领取后您将成为该客户的负责人。').then(() => {
    claimCustomer(row.customerId).then(() => {
      proxy.$modal.msgSuccess('领取成功')
      getList()
    })
  }).catch(() => {})
}

function handleBatchClaim() {
  proxy.$modal.confirm('确认领取选中的 ' + ids.value.length + ' 个客户？').then(() => {
    const promises = ids.value.map(id => claimCustomer(id))
    Promise.all(promises).then(() => {
      proxy.$modal.msgSuccess('批量领取成功')
      getList()
    }).catch(() => {})
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
</style>
