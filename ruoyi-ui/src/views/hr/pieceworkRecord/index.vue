<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="报工单号" prop="pwRecordNo">
        <el-input v-model="queryParams.pwRecordNo" placeholder="请输入报工单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="报工日期" prop="recordDate">
        <el-input v-model="queryParams.recordDate" placeholder="请输入报工日期" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="员工ID" prop="employeeId">
        <el-input v-model="queryParams.employeeId" placeholder="请输入员工ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="员工姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:piecework:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:piecework:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:piecework:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:piecework:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrPieceworkRecordList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="报工单号" prop="pwRecordNo" align="center"  />
      <el-table-column label="报工日期" prop="recordDate" align="center" width="120" />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="班组组织ID" prop="orgId" align="center"  />
      <el-table-column label="计件方案ID" prop="pwPlanId" align="center"  />
      <el-table-column label="计件名称" prop="pwName" align="center"  />
      <el-table-column label="合格数量" prop="qualifiedQty" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:piecework:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:piecework:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="计件报工" append-to-body>
      <el-form ref="hrPieceworkRecordRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="报工单号" prop="pwRecordNo">
            <el-input v-model="form.pwRecordNo" placeholder="请输入报工单号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报工日期" prop="recordDate">
            <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
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
          <el-form-item label="班组组织ID" prop="orgId">
            <el-input-number v-model="form.orgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计件方案ID" prop="pwPlanId">
            <el-input-number v-model="form.pwPlanId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计件名称" prop="pwName">
            <el-input v-model="form.pwName" placeholder="请输入计件名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合格数量" prop="qualifiedQty">
            <el-input-number v-model="form.qualifiedQty" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="不合格数量" prop="unqualifiedQty">
            <el-input-number v-model="form.unqualifiedQty" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单价" prop="unitPrice">
            <el-input-number v-model="form.unitPrice" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计件总额" prop="totalAmount">
            <el-input-number v-model="form.totalAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="确认状态" prop="confirmStatus">
            <el-input v-model="form.confirmStatus" placeholder="请输入确认状态" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="确认人" prop="confirmBy">
            <el-input v-model="form.confirmBy" placeholder="请输入确认人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="确认时间" prop="confirmTime">
            <el-date-picker v-model="form.confirmTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="来源" prop="source">
            <el-input v-model="form.source" placeholder="请输入来源" />
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

<script setup name="HrPieceworkRecord">
import { listHrPieceworkRecord, getHrPieceworkRecord, addHrPieceworkRecord, updateHrPieceworkRecord, delHrPieceworkRecord } from '@/api/hr/hrPieceworkRecord'

const { proxy } = getCurrentInstance()

const hrPieceworkRecordList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, pwRecordNo: undefined, recordDate: undefined, employeeId: undefined, employeeName: undefined },
  rules: {
    pwRecordNo: [{ required: true, message: '报工单号不能为空', trigger: 'blur' }],
    recordDate: [{ required: true, message: '报工日期不能为空', trigger: 'blur' }],
    pwPlanId: [{ required: true, message: '计件方案ID不能为空', trigger: 'blur' }],
    qualifiedQty: [{ required: true, message: '合格数量不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrPieceworkRecord(queryParams.value).then(response => {
    hrPieceworkRecordList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.pwRecordNo = undefined
  queryParams.value.recordDate = undefined
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.pwRecordId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    pwRecordNo: undefined,
    recordDate: undefined,
    employeeId: undefined,
    employeeName: undefined,
    orgId: undefined,
    pwPlanId: undefined,
    pwName: undefined,
    qualifiedQty: undefined,
    unqualifiedQty: undefined,
    unitPrice: undefined,
    totalAmount: undefined,
    confirmStatus: undefined,
    confirmBy: undefined,
    confirmTime: undefined,
    source: undefined
  }
  proxy.resetForm('hrPieceworkRecordRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const pwRecordId = row.pwRecordId || ids.value[0]
  getHrPieceworkRecord(pwRecordId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrPieceworkRecordRef'].validate(valid => {
    if (valid) {
      if (form.value.pwRecordId != undefined) {
        updateHrPieceworkRecord(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrPieceworkRecord(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const pwRecordIds = row.pwRecordId || ids.value
  proxy.$modal.confirm('是否确认删除选中的计件报工？').then(function() {
    return delHrPieceworkRecord(pwRecordIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/pieceworkRecord/export', { ...queryParams.value }, `hrPieceworkRecord_${new Date().getTime()}.xlsx`)
}

getList()
</script>
