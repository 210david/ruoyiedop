<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="调动单号" prop="transferNo">
        <el-input v-model="queryParams.transferNo" placeholder="请输入调动单号" clearable @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:transfer:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:transfer:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:transfer:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:transfer:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrTransferList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="调动单号" prop="transferNo" align="center"  />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="调动类型" prop="transferType" align="center"  />
      <el-table-column label="原组织ID" prop="fromOrgId" align="center"  />
      <el-table-column label="原组织名称" prop="fromOrgName" align="center"  />
      <el-table-column label="目标组织ID" prop="toOrgId" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:transfer:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:transfer:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="调动" append-to-body>
      <el-form ref="hrTransferRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="调动单号" prop="transferNo">
            <el-input v-model="form.transferNo" placeholder="请输入调动单号" />
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
          <el-form-item label="调动类型" prop="transferType">
            <el-input v-model="form.transferType" placeholder="请输入调动类型" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="原组织ID" prop="fromOrgId">
            <el-input-number v-model="form.fromOrgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="原组织名称" prop="fromOrgName">
            <el-input v-model="form.fromOrgName" placeholder="请输入原组织名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标组织ID" prop="toOrgId">
            <el-input-number v-model="form.toOrgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标组织名称" prop="toOrgName">
            <el-input v-model="form.toOrgName" placeholder="请输入目标组织名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="原岗位" prop="fromPosition">
            <el-input v-model="form.fromPosition" placeholder="请输入原岗位" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标岗位" prop="toPosition">
            <el-input v-model="form.toPosition" placeholder="请输入目标岗位" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="原薪资" prop="fromSalary">
            <el-input-number v-model="form.fromSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标薪资" prop="toSalary">
            <el-input-number v-model="form.toSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生效日期" prop="effectiveDate">
            <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="调动原因" prop="reason">
            <el-input v-model="form.reason" placeholder="请输入调动原因" />
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

<script setup name="HrTransfer">
import { listHrTransfer, getHrTransfer, addHrTransfer, updateHrTransfer, delHrTransfer } from '@/api/hr/hrTransfer'

const { proxy } = getCurrentInstance()

const hrTransferList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, transferNo: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined },
  rules: {
    transferNo: [{ required: true, message: '调动单号不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }],
    effectiveDate: [{ required: true, message: '生效日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrTransfer(queryParams.value).then(response => {
    hrTransferList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.transferNo = undefined
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.transferId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    transferNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    transferType: undefined,
    fromOrgId: undefined,
    fromOrgName: undefined,
    toOrgId: undefined,
    toOrgName: undefined,
    fromPosition: undefined,
    toPosition: undefined,
    fromSalary: undefined,
    toSalary: undefined,
    effectiveDate: undefined,
    reason: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('hrTransferRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const transferId = row.transferId || ids.value[0]
  getHrTransfer(transferId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrTransferRef'].validate(valid => {
    if (valid) {
      if (form.value.transferId != undefined) {
        updateHrTransfer(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrTransfer(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const transferIds = row.transferId || ids.value
  proxy.$modal.confirm('是否确认删除选中的调动？').then(function() {
    return delHrTransfer(transferIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/transfer/export', { ...queryParams.value }, `hrTransfer_${new Date().getTime()}.xlsx`)
}

getList()
</script>
