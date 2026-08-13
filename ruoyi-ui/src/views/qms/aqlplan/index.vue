<template>
  <div class="app-container">
    <!-- Filter -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>AQL等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.aqlLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="v in aqlLevels" :key="v" :label="v" :value="v" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>字码</label>
          <div class="control">
            <el-input v-model="queryParams.codeLetter" placeholder="如G/H/J" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>检验严格度</label>
          <div class="control is-select">
            <el-select v-model="queryParams.inspectLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="1" />
              <el-option label="加严" value="2" />
              <el-option label="放宽" value="3" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="surface">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:aqlplan:add']">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:aqlplan:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:aqlplan:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['qms:aqlplan:export']">导出</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-loading="loading" :data="planList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="方案ID" prop="planId" width="80" align="center" />
        <el-table-column label="AQL等级" prop="aqlLevel" width="100" align="center" />
        <el-table-column label="字码" prop="codeLetter" width="80" align="center" />
        <el-table-column label="样本量(n)" prop="sampleSize" width="100" align="center" />
        <el-table-column label="接收数(Ac)" prop="acVal" width="100" align="center" />
        <el-table-column label="拒收数(Re)" prop="reVal" width="100" align="center" />
        <el-table-column label="检验严格度" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.inspectLevel === '1' ? '' : scope.row.inspectLevel === '2' ? 'danger' : 'success'" size="small">
              {{ scope.row.inspectLevel === '1' ? '正常' : scope.row.inspectLevel === '2' ? '加严' : '放宽' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="批量范围" width="160" align="center">
          <template #default="scope">
            {{ scope.row.batchMin || 0 }} ~ {{ scope.row.batchMax || '∞' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
              {{ scope.row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:aqlplan:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:aqlplan:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="planRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="AQL等级" prop="aqlLevel">
              <el-select v-model="form.aqlLevel" placeholder="请选择" style="width: 100%">
                <el-option v-for="v in aqlLevels" :key="v" :label="v" :value="v" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字码" prop="codeLetter">
              <el-input v-model="form.codeLetter" placeholder="如G/H/J/K" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="样本量" prop="sampleSize">
              <el-input-number v-model="form.sampleSize" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验严格度" prop="inspectLevel">
              <el-select v-model="form.inspectLevel" style="width: 100%">
                <el-option label="正常" value="1" />
                <el-option label="加严" value="2" />
                <el-option label="放宽" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="接收数Ac" prop="acVal">
              <el-input-number v-model="form.acVal" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拒收数Re" prop="reVal">
              <el-input-number v-model="form.reVal" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批量下限" prop="batchMin">
              <el-input-number v-model="form.batchMin" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批量上限" prop="batchMax">
              <el-input-number v-model="form.batchMax" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listAqlPlan, getAqlPlan, addAqlPlan, updateAqlPlan, delAqlPlan } from '@/api/qms/aqlplan'

const { proxy } = getCurrentInstance()
const planList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const aqlLevels = ['0.25', '0.40', '0.65', '1.0', '1.5', '2.5', '4.0', '6.5', '10.0']

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, aqlLevel: undefined, codeLetter: undefined, inspectLevel: undefined, status: undefined },
  rules: {
    aqlLevel: [{ required: true, message: 'AQL等级不能为空', trigger: 'change' }],
    codeLetter: [{ required: true, message: '字码不能为空', trigger: 'blur' }],
    sampleSize: [{ required: true, message: '样本量不能为空', trigger: 'blur' }],
    acVal: [{ required: true, message: '接收数不能为空', trigger: 'blur' }],
    reVal: [{ required: true, message: '拒收数不能为空', trigger: 'blur' }],
    inspectLevel: [{ required: true, message: '检验严格度不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listAqlPlan(queryParams.value).then(res => {
    planList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.planId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = { aqlLevel: undefined, codeLetter: undefined, sampleSize: undefined, acVal: 0, reVal: 1, inspectLevel: '1', batchMin: undefined, batchMax: undefined, status: '0', remark: undefined }
  proxy.resetForm('planRef')
}

function handleAdd() { reset(); open.value = true; title.value = '新增抽样方案' }

function handleUpdate(row) {
  reset()
  const planId = row.planId || ids.value[0]
  getAqlPlan(planId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改抽样方案'
  })
}

function submitForm() {
  proxy.$refs['planRef'].validate(valid => {
    if (valid) {
      if (form.value.planId != null) {
        updateAqlPlan(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addAqlPlan(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}

function handleDelete(row) {
  const planIds = row.planId ? [row.planId] : ids.value
  proxy.$modal.confirm('确认删除选中的方案?').then(() => {
    return delAqlPlan(planIds)
  }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

function handleExport() {
  proxy.download('qms/aqlplan/export', { ...queryParams.value }, `aqlplan_${new Date().getTime()}.xlsx`)
}

function cancel() { open.value = false; reset() }

getList()
</script>
