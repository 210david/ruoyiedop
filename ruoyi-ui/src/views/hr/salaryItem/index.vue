<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="编码" prop="itemCode">
        <el-input v-model="queryParams.itemCode" placeholder="请输入编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="名称" prop="itemName">
        <el-input v-model="queryParams.itemName" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类别" prop="itemCategory">
        <el-input v-model="queryParams.itemCategory" placeholder="请输入类别" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="计算规则" prop="calcRule">
        <el-input v-model="queryParams.calcRule" placeholder="请输入计算规则" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:salaryItem:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:salaryItem:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:salaryItem:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:salaryItem:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrSalaryItemList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="编码" prop="itemCode" align="center"  />
      <el-table-column label="名称" prop="itemName" align="center"  />
      <el-table-column label="类别" prop="itemCategory" align="center"  />
      <el-table-column label="计算规则" prop="calcRule" align="center"  />
      <el-table-column label="加项" prop="isAdd" align="center"  />
      <el-table-column label="行业标签" prop="industryTag" align="center"  />
      <el-table-column label="排序号" prop="orderNum" align="center"  />
      <el-table-column label="状态" prop="status" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:salaryItem:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:salaryItem:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="薪资项字典" append-to-body>
      <el-form ref="hrSalaryItemRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="编码" prop="itemCode">
            <el-input v-model="form.itemCode" placeholder="请输入编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="名称" prop="itemName">
            <el-input v-model="form.itemName" placeholder="请输入名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类别" prop="itemCategory">
            <el-input v-model="form.itemCategory" placeholder="请输入类别" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计算规则" prop="calcRule">
            <el-input v-model="form.calcRule" placeholder="请输入计算规则" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="加项" prop="isAdd">
            <el-input-number v-model="form.isAdd" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="行业标签" prop="industryTag">
            <el-input v-model="form.industryTag" placeholder="请输入行业标签" />
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

<script setup name="HrSalaryItem">
import { listHrSalaryItem, getHrSalaryItem, addHrSalaryItem, updateHrSalaryItem, delHrSalaryItem } from '@/api/hr/hrSalaryItem'

const { proxy } = getCurrentInstance()

const hrSalaryItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, itemCode: undefined, itemName: undefined, itemCategory: undefined, calcRule: undefined },
  rules: {
    itemCode: [{ required: true, message: '编码不能为空', trigger: 'blur' }],
    itemName: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrSalaryItem(queryParams.value).then(response => {
    hrSalaryItemList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.itemCode = undefined
  queryParams.value.itemName = undefined
  queryParams.value.itemCategory = undefined
  queryParams.value.calcRule = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.itemId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    itemCode: undefined,
    itemName: undefined,
    itemCategory: undefined,
    calcRule: undefined,
    isAdd: undefined,
    industryTag: undefined,
    orderNum: undefined
  }
  proxy.resetForm('hrSalaryItemRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const itemId = row.itemId || ids.value[0]
  getHrSalaryItem(itemId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrSalaryItemRef'].validate(valid => {
    if (valid) {
      if (form.value.itemId != undefined) {
        updateHrSalaryItem(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrSalaryItem(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const itemIds = row.itemId || ids.value
  proxy.$modal.confirm('是否确认删除选中的薪资项字典？').then(function() {
    return delHrSalaryItem(itemIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/salaryItem/export', { ...queryParams.value }, `hrSalaryItem_${new Date().getTime()}.xlsx`)
}

getList()
</script>
