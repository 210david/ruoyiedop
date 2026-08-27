<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="年月" prop="yearMonth">
        <el-input v-model="queryParams.yearMonth" placeholder="请输入年月" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="员工ID" prop="employeeId">
        <el-input v-model="queryParams.employeeId" placeholder="请输入员工ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="员工姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工号" prop="employeeNo">
        <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:monthly:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:monthly:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:monthly:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:monthly:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrAttendanceMonthlyList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="年月" prop="yearMonth" align="center"  />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="组织ID" prop="orgId" align="center"  />
      <el-table-column label="出勤天数" prop="attendDays" align="center"  />
      <el-table-column label="迟到次数" prop="lateCount" align="center"  />
      <el-table-column label="早退次数" prop="earlyCount" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:monthly:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:monthly:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="考勤月报" append-to-body>
      <el-form ref="hrAttendanceMonthlyRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年月" prop="yearMonth">
            <el-input v-model="form.yearMonth" placeholder="请输入年月" />
          </el-form-item>
        </el-col>
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
          <el-form-item label="工号" prop="employeeNo">
            <el-input v-model="form.employeeNo" placeholder="请输入工号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组织ID" prop="orgId">
            <el-input-number v-model="form.orgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出勤天数" prop="attendDays">
            <el-input-number v-model="form.attendDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="迟到次数" prop="lateCount">
            <el-input-number v-model="form.lateCount" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="早退次数" prop="earlyCount">
            <el-input-number v-model="form.earlyCount" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="旷工天数" prop="absentDays">
            <el-input-number v-model="form.absentDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="请假天数" prop="leaveDays">
            <el-input-number v-model="form.leaveDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="加班小时" prop="overtimeHours">
            <el-input-number v-model="form.overtimeHours" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="夜班次数" prop="nightShiftCount">
            <el-input-number v-model="form.nightShiftCount" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="高温天数" prop="highTempDays">
            <el-input-number v-model="form.highTempDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="封账状态" prop="sealedStatus">
            <el-input v-model="form.sealedStatus" placeholder="请输入封账状态" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="封账人" prop="sealedBy">
            <el-input v-model="form.sealedBy" placeholder="请输入封账人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="封账时间" prop="sealedTime">
            <el-date-picker v-model="form.sealedTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
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

<script setup name="HrAttendanceMonthly">
import { listHrAttendanceMonthly, getHrAttendanceMonthly, addHrAttendanceMonthly, updateHrAttendanceMonthly, delHrAttendanceMonthly } from '@/api/hr/hrAttendanceMonthly'

const { proxy } = getCurrentInstance()

const hrAttendanceMonthlyList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, yearMonth: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined },
  rules: {
    yearMonth: [{ required: true, message: '年月不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrAttendanceMonthly(queryParams.value).then(response => {
    hrAttendanceMonthlyList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.yearMonth = undefined
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.monthlyId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    yearMonth: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    orgId: undefined,
    attendDays: undefined,
    lateCount: undefined,
    earlyCount: undefined,
    absentDays: undefined,
    leaveDays: undefined,
    overtimeHours: undefined,
    nightShiftCount: undefined,
    highTempDays: undefined,
    sealedStatus: undefined,
    sealedBy: undefined,
    sealedTime: undefined
  }
  proxy.resetForm('hrAttendanceMonthlyRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const monthlyId = row.monthlyId || ids.value[0]
  getHrAttendanceMonthly(monthlyId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrAttendanceMonthlyRef'].validate(valid => {
    if (valid) {
      if (form.value.monthlyId != undefined) {
        updateHrAttendanceMonthly(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrAttendanceMonthly(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const monthlyIds = row.monthlyId || ids.value
  proxy.$modal.confirm('是否确认删除选中的考勤月报？').then(function() {
    return delHrAttendanceMonthly(monthlyIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/monthly/export', { ...queryParams.value }, `hrAttendanceMonthly_${new Date().getTime()}.xlsx`)
}

getList()
</script>
