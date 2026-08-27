<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="结算单号" prop="settlementNo">
        <el-input v-model="queryParams.settlementNo" placeholder="请输入结算单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="结算日期" prop="settlementDate">
        <el-input v-model="queryParams.settlementDate" placeholder="请输入结算日期" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="劳务工ID" prop="laborId">
        <el-input v-model="queryParams.laborId" placeholder="请输入劳务工ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="workerName">
        <el-input v-model="queryParams.workerName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:tempSettlement:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:tempSettlement:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:tempSettlement:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:tempSettlement:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrTempDailySettlementList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="结算单号" prop="settlementNo" align="center"  />
      <el-table-column label="结算日期" prop="settlementDate" align="center" width="120" />
      <el-table-column label="劳务工ID" prop="laborId" align="center"  />
      <el-table-column label="姓名" prop="workerName" align="center"  />
      <el-table-column label="出勤天数" prop="attendDays" align="center"  />
      <el-table-column label="日薪" prop="dailyWage" align="center"  />
      <el-table-column label="计件金额" prop="pieceworkAmount" align="center"  />
      <el-table-column label="结算总额" prop="totalAmount" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:tempSettlement:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:tempSettlement:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="临时工日结" append-to-body>
      <el-form ref="hrTempDailySettlementRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="结算单号" prop="settlementNo">
            <el-input v-model="form.settlementNo" placeholder="请输入结算单号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结算日期" prop="settlementDate">
            <el-date-picker v-model="form.settlementDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="劳务工ID" prop="laborId">
            <el-input-number v-model="form.laborId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名" prop="workerName">
            <el-input v-model="form.workerName" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出勤天数" prop="attendDays">
            <el-input-number v-model="form.attendDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="日薪" prop="dailyWage">
            <el-input-number v-model="form.dailyWage" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计件金额" prop="pieceworkAmount">
            <el-input-number v-model="form.pieceworkAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结算总额" prop="totalAmount">
            <el-input-number v-model="form.totalAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="支付状态" prop="payStatus">
            <el-input v-model="form.payStatus" placeholder="请输入支付状态" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="支付日期" prop="payDate">
            <el-date-picker v-model="form.payDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="支付方式" prop="payMethod">
            <el-input v-model="form.payMethod" placeholder="请输入支付方式" />
          </el-form-item>
        </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrTempDailySettlement">
import { listHrTempDailySettlement, getHrTempDailySettlement, addHrTempDailySettlement, updateHrTempDailySettlement, delHrTempDailySettlement } from '@/api/hr/hrTempDailySettlement'

const { proxy } = getCurrentInstance()

const hrTempDailySettlementList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, settlementNo: undefined, settlementDate: undefined, laborId: undefined, workerName: undefined },
  rules: {
    settlementNo: [{ required: true, message: '结算单号不能为空', trigger: 'blur' }],
    settlementDate: [{ required: true, message: '结算日期不能为空', trigger: 'blur' }],
    laborId: [{ required: true, message: '劳务工ID不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrTempDailySettlement(queryParams.value).then(response => {
    hrTempDailySettlementList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.settlementNo = undefined
  queryParams.value.settlementDate = undefined
  queryParams.value.laborId = undefined
  queryParams.value.workerName = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.settlementId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    settlementNo: undefined,
    settlementDate: undefined,
    laborId: undefined,
    workerName: undefined,
    attendDays: undefined,
    dailyWage: undefined,
    pieceworkAmount: undefined,
    totalAmount: undefined,
    payStatus: undefined,
    payDate: undefined,
    payMethod: undefined
  }
  proxy.resetForm('hrTempDailySettlementRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const settlementId = row.settlementId || ids.value[0]
  getHrTempDailySettlement(settlementId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrTempDailySettlementRef'].validate(valid => {
    if (valid) {
      if (form.value.settlementId != undefined) {
        updateHrTempDailySettlement(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrTempDailySettlement(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const settlementIds = row.settlementId || ids.value
  proxy.$modal.confirm('是否确认删除选中的临时工日结？').then(function() {
    return delHrTempDailySettlement(settlementIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/tempSettlement/export', { ...queryParams.value }, `hrTempDailySettlement_${new Date().getTime()}.xlsx`)
}

getList()
</script>
