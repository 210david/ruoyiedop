<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="员工ID" prop="employeeId">
        <el-input v-model="queryParams.employeeId" placeholder="请输入员工ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="员工姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="假别" prop="leaveType">
        <el-input v-model="queryParams.leaveType" placeholder="请输入假别" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="年度" prop="year">
        <el-input v-model="queryParams.year" placeholder="请输入年度" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:leaveBalance:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:leaveBalance:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:leaveBalance:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:leaveBalance:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrLeaveBalanceList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="假别" prop="leaveType" align="center"  />
      <el-table-column label="年度" prop="year" align="center"  />
      <el-table-column label="总额度" prop="totalDays" align="center"  />
      <el-table-column label="已休天数" prop="usedDays" align="center"  />
      <el-table-column label="剩余天数" prop="remainingDays" align="center"  />
      <el-table-column label="冻结天数" prop="frozenDays" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:leaveBalance:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:leaveBalance:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="假期余额" append-to-body>
      <el-form ref="hrLeaveBalanceRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="员工ID" prop="employeeId">
            <el-input-number v-model="form.employeeId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="员工姓名" prop="employeeName">
            <el-input v-model="form.employeeName" placeholder="请输入员工姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="假别" prop="leaveType">
            <el-input v-model="form.leaveType" placeholder="请输入假别" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年度" prop="year">
            <el-input-number v-model="form.year" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="总额度" prop="totalDays">
            <el-input-number v-model="form.totalDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="已休天数" prop="usedDays">
            <el-input-number v-model="form.usedDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="剩余天数" prop="remainingDays">
            <el-input-number v-model="form.remainingDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="冻结天数" prop="frozenDays">
            <el-input-number v-model="form.frozenDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="过期日期" prop="expireDate">
            <el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
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

<script setup name="HrLeaveBalance">
import { listHrLeaveBalance, getHrLeaveBalance, addHrLeaveBalance, updateHrLeaveBalance, delHrLeaveBalance } from '@/api/hr/hrLeaveBalance'

const { proxy } = getCurrentInstance()

const hrLeaveBalanceList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeId: undefined, employeeName: undefined, leaveType: undefined, year: undefined },
  rules: {
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }],
    leaveType: [{ required: true, message: '假别不能为空', trigger: 'blur' }],
    year: [{ required: true, message: '年度不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrLeaveBalance(queryParams.value).then(response => {
    hrLeaveBalanceList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.leaveType = undefined
  queryParams.value.year = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.balanceId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeName: undefined,
    leaveType: undefined,
    year: undefined,
    totalDays: undefined,
    usedDays: undefined,
    remainingDays: undefined,
    frozenDays: undefined,
    expireDate: undefined
  }
  proxy.resetForm('hrLeaveBalanceRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const balanceId = row.balanceId || ids.value[0]
  getHrLeaveBalance(balanceId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrLeaveBalanceRef'].validate(valid => {
    if (valid) {
      if (form.value.balanceId != undefined) {
        updateHrLeaveBalance(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrLeaveBalance(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const balanceIds = row.balanceId || ids.value
  proxy.$modal.confirm('是否确认删除选中的假期余额？').then(function() {
    return delHrLeaveBalance(balanceIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/leaveBalance/export', { ...queryParams.value }, `hrLeaveBalance_${new Date().getTime()}.xlsx`)
}

getList()
</script>
