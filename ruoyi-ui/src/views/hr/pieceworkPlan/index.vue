<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="计件编码" prop="pwCode">
        <el-input v-model="queryParams.pwCode" placeholder="请输入计件编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="计件名称" prop="pwName">
        <el-input v-model="queryParams.pwName" placeholder="请输入计件名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="计件类型" prop="pwType">
        <el-input v-model="queryParams.pwType" placeholder="请输入计件类型" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="单位" prop="unit">
        <el-input v-model="queryParams.unit" placeholder="请输入单位" clearable @keyup.enter="handleQuery" />
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
    <el-table v-loading="loading" :data="hrPieceworkPlanList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="计件编码" prop="pwCode" align="center"  />
      <el-table-column label="计件名称" prop="pwName" align="center"  />
      <el-table-column label="计件类型" prop="pwType" align="center"  />
      <el-table-column label="单位" prop="unit" align="center"  />
      <el-table-column label="计件单价" prop="unitPrice" align="center"  />
      <el-table-column label="产品ID" prop="productId" align="center"  />
      <el-table-column label="工序ID" prop="processId" align="center"  />
      <el-table-column label="适用组织ID" prop="orgId" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:piecework:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:piecework:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="计件方案" append-to-body>
      <el-form ref="hrPieceworkPlanRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="计件编码" prop="pwCode">
            <el-input v-model="form.pwCode" placeholder="请输入计件编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计件名称" prop="pwName">
            <el-input v-model="form.pwName" placeholder="请输入计件名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计件类型" prop="pwType">
            <el-input v-model="form.pwType" placeholder="请输入计件类型" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单位" prop="unit">
            <el-input v-model="form.unit" placeholder="请输入单位" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计件单价" prop="unitPrice">
            <el-input-number v-model="form.unitPrice" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="产品ID" prop="productId">
            <el-input-number v-model="form.productId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工序ID" prop="processId">
            <el-input-number v-model="form.processId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="适用组织ID" prop="orgId">
            <el-input-number v-model="form.orgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="保底工资" prop="baseSalary">
            <el-input-number v-model="form.baseSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生效日期" prop="effectiveDate">
            <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="失效日期" prop="expiryDate">
            <el-date-picker v-model="form.expiryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批状态" prop="auditStatus">
            <el-input v-model="form.auditStatus" placeholder="请输入审批状态" />
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

<script setup name="HrPieceworkPlan">
import { listHrPieceworkPlan, getHrPieceworkPlan, addHrPieceworkPlan, updateHrPieceworkPlan, delHrPieceworkPlan } from '@/api/hr/hrPieceworkPlan'

const { proxy } = getCurrentInstance()

const hrPieceworkPlanList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, pwCode: undefined, pwName: undefined, pwType: undefined, unit: undefined },
  rules: {
    pwCode: [{ required: true, message: '计件编码不能为空', trigger: 'blur' }],
    pwName: [{ required: true, message: '计件名称不能为空', trigger: 'blur' }],
    unitPrice: [{ required: true, message: '计件单价不能为空', trigger: 'blur' }],
    effectiveDate: [{ required: true, message: '生效日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrPieceworkPlan(queryParams.value).then(response => {
    hrPieceworkPlanList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.pwCode = undefined
  queryParams.value.pwName = undefined
  queryParams.value.pwType = undefined
  queryParams.value.unit = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.pwPlanId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    pwCode: undefined,
    pwName: undefined,
    pwType: undefined,
    unit: undefined,
    unitPrice: undefined,
    productId: undefined,
    processId: undefined,
    orgId: undefined,
    baseSalary: undefined,
    effectiveDate: undefined,
    expiryDate: undefined,
    auditStatus: undefined
  }
  proxy.resetForm('hrPieceworkPlanRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const pwPlanId = row.pwPlanId || ids.value[0]
  getHrPieceworkPlan(pwPlanId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrPieceworkPlanRef'].validate(valid => {
    if (valid) {
      if (form.value.pwPlanId != undefined) {
        updateHrPieceworkPlan(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrPieceworkPlan(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const pwPlanIds = row.pwPlanId || ids.value
  proxy.$modal.confirm('是否确认删除选中的计件方案？').then(function() {
    return delHrPieceworkPlan(pwPlanIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/pieceworkPlan/export', { ...queryParams.value }, `hrPieceworkPlan_${new Date().getTime()}.xlsx`)
}

getList()
</script>
