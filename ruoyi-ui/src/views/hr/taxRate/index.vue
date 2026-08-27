<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="级数" prop="level">
        <el-input v-model="queryParams.level" placeholder="请输入级数" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="下限" prop="minAmount">
        <el-input v-model="queryParams.minAmount" placeholder="请输入下限" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="上限" prop="maxAmount">
        <el-input v-model="queryParams.maxAmount" placeholder="请输入上限" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="税率" prop="rate">
        <el-input v-model="queryParams.rate" placeholder="请输入税率" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:taxRate:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:taxRate:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:taxRate:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:taxRate:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrTaxRateList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="级数" prop="level" align="center"  />
      <el-table-column label="下限" prop="minAmount" align="center"  />
      <el-table-column label="上限" prop="maxAmount" align="center"  />
      <el-table-column label="税率" prop="rate" align="center"  />
      <el-table-column label="速算扣除数" prop="quickDeduction" align="center"  />
      <el-table-column label="生效日期" prop="effectiveDate" align="center" width="120" />
      <el-table-column label="状态" prop="status" align="center"  />
      <el-table-column label="删除标志" prop="delFlag" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:taxRate:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:taxRate:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="个税税率" append-to-body>
      <el-form ref="hrTaxRateRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="级数" prop="level">
            <el-input-number v-model="form.level" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="下限" prop="minAmount">
            <el-input-number v-model="form.minAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="上限" prop="maxAmount">
            <el-input-number v-model="form.maxAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="税率" prop="rate">
            <el-input-number v-model="form.rate" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="速算扣除数" prop="quickDeduction">
            <el-input-number v-model="form.quickDeduction" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生效日期" prop="effectiveDate">
            <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
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

<script setup name="HrTaxRate">
import { listHrTaxRate, getHrTaxRate, addHrTaxRate, updateHrTaxRate, delHrTaxRate } from '@/api/hr/hrTaxRate'

const { proxy } = getCurrentInstance()

const hrTaxRateList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, level: undefined, minAmount: undefined, maxAmount: undefined, rate: undefined },
  rules: {
    level: [{ required: true, message: '级数不能为空', trigger: 'blur' }],
    minAmount: [{ required: true, message: '下限不能为空', trigger: 'blur' }],
    rate: [{ required: true, message: '税率不能为空', trigger: 'blur' }],
    quickDeduction: [{ required: true, message: '速算扣除数不能为空', trigger: 'blur' }],
    effectiveDate: [{ required: true, message: '生效日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrTaxRate(queryParams.value).then(response => {
    hrTaxRateList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.level = undefined
  queryParams.value.minAmount = undefined
  queryParams.value.maxAmount = undefined
  queryParams.value.rate = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.taxRateId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    level: undefined,
    minAmount: undefined,
    maxAmount: undefined,
    rate: undefined,
    quickDeduction: undefined,
    effectiveDate: undefined
  }
  proxy.resetForm('hrTaxRateRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const taxRateId = row.taxRateId || ids.value[0]
  getHrTaxRate(taxRateId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrTaxRateRef'].validate(valid => {
    if (valid) {
      if (form.value.taxRateId != undefined) {
        updateHrTaxRate(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrTaxRate(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const taxRateIds = row.taxRateId || ids.value
  proxy.$modal.confirm('是否确认删除选中的个税税率？').then(function() {
    return delHrTaxRate(taxRateIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/taxRate/export', { ...queryParams.value }, `hrTaxRate_${new Date().getTime()}.xlsx`)
}

getList()
</script>
