<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="班次编码" prop="shiftCode">
        <el-input v-model="queryParams.shiftCode" placeholder="请输入班次编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="班次名称" prop="shiftName">
        <el-input v-model="queryParams.shiftName" placeholder="请输入班次名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-input v-model="queryParams.startTime" placeholder="请输入开始时间" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-input v-model="queryParams.endTime" placeholder="请输入结束时间" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:shift:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:shift:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:shift:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:shift:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrShiftList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="班次编码" prop="shiftCode" align="center"  />
      <el-table-column label="班次名称" prop="shiftName" align="center"  />
      <el-table-column label="开始时间" prop="startTime" align="center"  />
      <el-table-column label="结束时间" prop="endTime" align="center"  />
      <el-table-column label="跨零点" prop="crossMidnight" align="center"  />
      <el-table-column label="工作时长" prop="workHours" align="center"  />
      <el-table-column label="休息时长" prop="restHours" align="center"  />
      <el-table-column label="用餐时间" prop="mealMinutes" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:shift:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:shift:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="班次" append-to-body>
      <el-form ref="hrShiftRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="班次编码" prop="shiftCode">
            <el-input v-model="form.shiftCode" placeholder="请输入班次编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班次名称" prop="shiftName">
            <el-input v-model="form.shiftName" placeholder="请输入班次名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-input v-model="form.startTime" placeholder="请输入开始时间" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-input v-model="form.endTime" placeholder="请输入结束时间" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="跨零点" prop="crossMidnight">
            <el-input-number v-model="form.crossMidnight" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工作时长" prop="workHours">
            <el-input-number v-model="form.workHours" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="休息时长" prop="restHours">
            <el-input-number v-model="form.restHours" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用餐时间" prop="mealMinutes">
            <el-input-number v-model="form.mealMinutes" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="夜班" prop="nightShift">
            <el-input-number v-model="form.nightShift" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="夜班津贴" prop="nightAllowance">
            <el-input-number v-model="form.nightAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="适用场景" prop="applicableTo">
            <el-input v-model="form.applicableTo" placeholder="请输入适用场景" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序号" prop="orderNum">
            <el-input-number v-model="form.orderNum" :precision="0" :min="0" controls-position="right" style="width: 100%" />
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

<script setup name="HrShift">
import { listHrShift, getHrShift, addHrShift, updateHrShift, delHrShift } from '@/api/hr/hrShift'

const { proxy } = getCurrentInstance()

const hrShiftList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, shiftCode: undefined, shiftName: undefined, startTime: undefined, endTime: undefined },
  rules: {
    shiftCode: [{ required: true, message: '班次编码不能为空', trigger: 'blur' }],
    shiftName: [{ required: true, message: '班次名称不能为空', trigger: 'blur' }],
    startTime: [{ required: true, message: '开始时间不能为空', trigger: 'blur' }],
    endTime: [{ required: true, message: '结束时间不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrShift(queryParams.value).then(response => {
    hrShiftList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.shiftCode = undefined
  queryParams.value.shiftName = undefined
  queryParams.value.startTime = undefined
  queryParams.value.endTime = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.shiftId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    shiftCode: undefined,
    shiftName: undefined,
    startTime: undefined,
    endTime: undefined,
    crossMidnight: undefined,
    workHours: undefined,
    restHours: undefined,
    mealMinutes: undefined,
    nightShift: undefined,
    nightAllowance: undefined,
    applicableTo: undefined,
    orderNum: undefined
  }
  proxy.resetForm('hrShiftRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const shiftId = row.shiftId || ids.value[0]
  getHrShift(shiftId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrShiftRef'].validate(valid => {
    if (valid) {
      if (form.value.shiftId != undefined) {
        updateHrShift(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrShift(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const shiftIds = row.shiftId || ids.value
  proxy.$modal.confirm('是否确认删除选中的班次？').then(function() {
    return delHrShift(shiftIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/shift/export', { ...queryParams.value }, `hrShift_${new Date().getTime()}.xlsx`)
}

getList()
</script>
