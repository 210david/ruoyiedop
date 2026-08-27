<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="请假单号" prop="leaveReqNo">
        <el-input v-model="queryParams.leaveReqNo" placeholder="请输入请假单号" clearable @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:leaveRequest:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:leaveRequest:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:leaveRequest:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:leaveRequest:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrLeaveRequestList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="请假单号" prop="leaveReqNo" align="center"  />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="假别" prop="leaveType" align="center"  />
      <el-table-column label="开始时间" prop="startTime" align="center" width="120" />
      <el-table-column label="结束时间" prop="endTime" align="center" width="120" />
      <el-table-column label="请假天数" prop="leaveDays" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:leaveRequest:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:leaveRequest:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="请假" append-to-body>
      <el-form ref="hrLeaveRequestRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="请假单号" prop="leaveReqNo">
            <el-input v-model="form.leaveReqNo" placeholder="请输入请假单号" />
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
          <el-form-item label="假别" prop="leaveType">
            <el-input v-model="form.leaveType" placeholder="请输入假别" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker v-model="form.endTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="请假天数" prop="leaveDays">
            <el-input-number v-model="form.leaveDays" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="请假原因" prop="reason">
            <el-input v-model="form.reason" placeholder="请输入请假原因" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="附件URL" prop="attachmentUrl">
            <el-input v-model="form.attachmentUrl" placeholder="请输入附件URL" />
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
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrLeaveRequest">
import { listHrLeaveRequest, getHrLeaveRequest, addHrLeaveRequest, updateHrLeaveRequest, delHrLeaveRequest } from '@/api/hr/hrLeaveRequest'

const { proxy } = getCurrentInstance()

const hrLeaveRequestList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, leaveReqNo: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined },
  rules: {
    leaveReqNo: [{ required: true, message: '请假单号不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }],
    leaveType: [{ required: true, message: '假别不能为空', trigger: 'blur' }],
    startTime: [{ required: true, message: '开始时间不能为空', trigger: 'blur' }],
    endTime: [{ required: true, message: '结束时间不能为空', trigger: 'blur' }],
    leaveDays: [{ required: true, message: '请假天数不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrLeaveRequest(queryParams.value).then(response => {
    hrLeaveRequestList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.leaveReqNo = undefined
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.leaveReqId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    leaveReqNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    leaveType: undefined,
    startTime: undefined,
    endTime: undefined,
    leaveDays: undefined,
    reason: undefined,
    attachmentUrl: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('hrLeaveRequestRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const leaveReqId = row.leaveReqId || ids.value[0]
  getHrLeaveRequest(leaveReqId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrLeaveRequestRef'].validate(valid => {
    if (valid) {
      if (form.value.leaveReqId != undefined) {
        updateHrLeaveRequest(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrLeaveRequest(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const leaveReqIds = row.leaveReqId || ids.value
  proxy.$modal.confirm('是否确认删除选中的请假？').then(function() {
    return delHrLeaveRequest(leaveReqIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/leaveRequest/export', { ...queryParams.value }, `hrLeaveRequest_${new Date().getTime()}.xlsx`)
}

getList()
</script>
