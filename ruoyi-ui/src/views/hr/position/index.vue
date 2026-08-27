<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="岗位编码" prop="positionCode">
        <el-input v-model="queryParams.positionCode" placeholder="请输入岗位编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="岗位名称" prop="positionName">
        <el-input v-model="queryParams.positionName" placeholder="请输入岗位名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="岗位序列" prop="positionSeq">
        <el-input v-model="queryParams.positionSeq" placeholder="请输入岗位序列" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="岗级" prop="positionLevel">
        <el-input v-model="queryParams.positionLevel" placeholder="请输入岗级" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:position:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:position:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:position:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:position:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrPositionList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="岗位编码" prop="positionCode" align="center"  />
      <el-table-column label="岗位名称" prop="positionName" align="center"  />
      <el-table-column label="岗位序列" prop="positionSeq" align="center"  />
      <el-table-column label="岗级" prop="positionLevel" align="center"  />
      <el-table-column label="技能等级" prop="skillLevel" align="center"  />
      <el-table-column label="所属组织ID" prop="orgId" align="center"  />
      <el-table-column label="编制人数" prop="headcount" align="center"  />
      <el-table-column label="编制管理" prop="headcountEnabled" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:position:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:position:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="岗位" append-to-body>
      <el-form ref="hrPositionRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="岗位编码" prop="positionCode">
            <el-input v-model="form.positionCode" placeholder="请输入岗位编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="岗位名称" prop="positionName">
            <el-input v-model="form.positionName" placeholder="请输入岗位名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="岗位序列" prop="positionSeq">
            <el-input v-model="form.positionSeq" placeholder="请输入岗位序列" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="岗级" prop="positionLevel">
            <el-input v-model="form.positionLevel" placeholder="请输入岗级" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="技能等级" prop="skillLevel">
            <el-input v-model="form.skillLevel" placeholder="请输入技能等级" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属组织ID" prop="orgId">
            <el-input-number v-model="form.orgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="编制人数" prop="headcount">
            <el-input-number v-model="form.headcount" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="编制管理" prop="headcountEnabled">
            <el-input-number v-model="form.headcountEnabled" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="必须持证" prop="requireCert">
            <el-input v-model="form.requireCert" placeholder="请输入必须持证" />
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

<script setup name="HrPosition">
import { listHrPosition, getHrPosition, addHrPosition, updateHrPosition, delHrPosition } from '@/api/hr/hrPosition'

const { proxy } = getCurrentInstance()

const hrPositionList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, positionCode: undefined, positionName: undefined, positionSeq: undefined, positionLevel: undefined },
  rules: {
    positionCode: [{ required: true, message: '岗位编码不能为空', trigger: 'blur' }],
    positionName: [{ required: true, message: '岗位名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrPosition(queryParams.value).then(response => {
    hrPositionList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.positionCode = undefined
  queryParams.value.positionName = undefined
  queryParams.value.positionSeq = undefined
  queryParams.value.positionLevel = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.positionId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    positionCode: undefined,
    positionName: undefined,
    positionSeq: undefined,
    positionLevel: undefined,
    skillLevel: undefined,
    orgId: undefined,
    headcount: undefined,
    headcountEnabled: undefined,
    requireCert: undefined,
    orderNum: undefined
  }
  proxy.resetForm('hrPositionRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const positionId = row.positionId || ids.value[0]
  getHrPosition(positionId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrPositionRef'].validate(valid => {
    if (valid) {
      if (form.value.positionId != undefined) {
        updateHrPosition(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrPosition(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const positionIds = row.positionId || ids.value
  proxy.$modal.confirm('是否确认删除选中的岗位？').then(function() {
    return delHrPosition(positionIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/position/export', { ...queryParams.value }, `hrPosition_${new Date().getTime()}.xlsx`)
}

getList()
</script>
