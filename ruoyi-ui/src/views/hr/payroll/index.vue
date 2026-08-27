<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="批次编号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入批次编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="年月" prop="yearMonth">
        <el-input v-model="queryParams.yearMonth" placeholder="请输入年月" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="批次名称" prop="batchName">
        <el-input v-model="queryParams.batchName" placeholder="请输入批次名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="发放日期" prop="payDate">
        <el-input v-model="queryParams.payDate" placeholder="请输入发放日期" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:payroll:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:payroll:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:payroll:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:payroll:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrPayrollBatchList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="批次编号" prop="batchNo" align="center"  />
      <el-table-column label="年月" prop="yearMonth" align="center"  />
      <el-table-column label="批次名称" prop="batchName" align="center"  />
      <el-table-column label="发放日期" prop="payDate" align="center" width="120" />
      <el-table-column label="发薪银行" prop="bankName" align="center"  />
      <el-table-column label="总人数" prop="totalCount" align="center"  />
      <el-table-column label="总金额" prop="totalAmount" align="center"  />
      <el-table-column label="审批状态" prop="auditStatus" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:payroll:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:payroll:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="薪酬发放" append-to-body>
      <el-form ref="hrPayrollBatchRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="批次编号" prop="batchNo">
            <el-input v-model="form.batchNo" placeholder="请输入批次编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年月" prop="yearMonth">
            <el-input v-model="form.yearMonth" placeholder="请输入年月" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="批次名称" prop="batchName">
            <el-input v-model="form.batchName" placeholder="请输入批次名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发放日期" prop="payDate">
            <el-date-picker v-model="form.payDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发薪银行" prop="bankName">
            <el-input v-model="form.bankName" placeholder="请输入发薪银行" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="总人数" prop="totalCount">
            <el-input-number v-model="form.totalCount" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="总金额" prop="totalAmount">
            <el-input-number v-model="form.totalAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批状态" prop="auditStatus">
            <el-input v-model="form.auditStatus" placeholder="请输入审批状态" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批人" prop="auditBy">
            <el-input v-model="form.auditBy" placeholder="请输入审批人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批时间" prop="auditTime">
            <el-date-picker v-model="form.auditTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批意见" prop="auditRemark">
            <el-input v-model="form.auditRemark" placeholder="请输入审批意见" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="银行回单号" prop="bankReceiptNo">
            <el-input v-model="form.bankReceiptNo" placeholder="请输入银行回单号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="封账状态" prop="sealedStatus">
            <el-input v-model="form.sealedStatus" placeholder="请输入封账状态" />
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

<script setup name="HrPayrollBatch">
import { listHrPayrollBatch, getHrPayrollBatch, addHrPayrollBatch, updateHrPayrollBatch, delHrPayrollBatch } from '@/api/hr/hrPayrollBatch'

const { proxy } = getCurrentInstance()

const hrPayrollBatchList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, batchNo: undefined, yearMonth: undefined, batchName: undefined, payDate: undefined },
  rules: {
    batchNo: [{ required: true, message: '批次编号不能为空', trigger: 'blur' }],
    yearMonth: [{ required: true, message: '年月不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrPayrollBatch(queryParams.value).then(response => {
    hrPayrollBatchList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.batchNo = undefined
  queryParams.value.yearMonth = undefined
  queryParams.value.batchName = undefined
  queryParams.value.payDate = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.batchId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    batchNo: undefined,
    yearMonth: undefined,
    batchName: undefined,
    payDate: undefined,
    bankName: undefined,
    totalCount: undefined,
    totalAmount: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined,
    bankReceiptNo: undefined,
    sealedStatus: undefined
  }
  proxy.resetForm('hrPayrollBatchRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const batchId = row.batchId || ids.value[0]
  getHrPayrollBatch(batchId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrPayrollBatchRef'].validate(valid => {
    if (valid) {
      if (form.value.batchId != undefined) {
        updateHrPayrollBatch(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrPayrollBatch(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const batchIds = row.batchId || ids.value
  proxy.$modal.confirm('是否确认删除选中的薪酬发放？').then(function() {
    return delHrPayrollBatch(batchIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/payroll/export', { ...queryParams.value }, `hrPayrollBatch_${new Date().getTime()}.xlsx`)
}

getList()
</script>
