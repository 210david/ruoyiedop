<template>
  <div class="app-container">
    <!-- 完整度看板 -->
    <el-row :gutter="16" class="mb10">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">已关联工单</div>
          <div class="stat-value">{{ completeness.linkedOrders || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">断点工单</div>
          <div class="stat-value" style="color:#f56c6c">{{ completeness.breakOrders || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">完整率</div>
          <div class="stat-value" :style="{color: (completeness.completenessRate||0) >= 98 ? '#67c23a' : '#e6a23c'}">{{ completeness.completenessRate || 0 }}%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">目标差距</div>
          <div class="stat-value" :style="{color: (completeness.gap||0) <= 0 ? '#67c23a' : '#f56c6c'}">{{ completeness.gap || 0 }}%</div>
          <div class="stat-sub">目标 ≥98%</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询区 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="投入批次" prop="parentBatchNo">
        <el-input v-model="queryParams.parentBatchNo" placeholder="投入批次号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="产出批次" prop="childBatchNo">
        <el-input v-model="queryParams.childBatchNo" placeholder="产出批次号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工单号" prop="workOrderNo">
        <el-input v-model="queryParams.workOrderNo" placeholder="工单号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="来源" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in qms_genealogy_source" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="断点" prop="breakFlag">
        <el-select v-model="queryParams.breakFlag" placeholder="全部" clearable style="width: 100px">
          <el-option label="仅断点" :value="1" /><el-option label="仅正常" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:genealogy:add']">新增谱系</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:genealogy:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:genealogy:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['qms:genealogy:export']">导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Warning" @click="handleBreakList">断点清单</el-button></el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="投入批次" prop="parentBatchNo" min-width="130" />
      <el-table-column label="产出批次" prop="childBatchNo" min-width="130">
        <template #default="scope">
          <el-tag v-if="scope.row.breakFlag === 1" type="danger" size="small">{{ scope.row.childBatchNo || '断点' }}</el-tag>
          <span v-else>{{ scope.row.childBatchNo || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="工单号" prop="workOrderNo" min-width="110" />
      <el-table-column label="物料" min-width="160">
        <template #default="scope">{{ scope.row.materialCode }} / {{ scope.row.materialName }}</template>
      </el-table-column>
      <el-table-column label="工序" prop="opName" min-width="80" />
      <el-table-column label="供应商" prop="supplierName" min-width="120" />
      <el-table-column label="投入量" prop="qtyIn" min-width="80" align="center" />
      <el-table-column label="产出量" prop="qtyOut" min-width="80" align="center" />
      <el-table-column label="来源" min-width="80">
        <template #default="scope"><dict-tag :options="qms_genealogy_source" :value="scope.row.sourceType" /></template>
      </el-table-column>
      <el-table-column label="操作员" prop="operator" min-width="70" />
      <el-table-column label="谱系时间" prop="traceTime" min-width="145" />
      <el-table-column label="断点" min-width="60" align="center">
        <template #default="scope"><el-tag v-if="scope.row.breakFlag === 1" type="danger" size="small">是</el-tag><span v-else>-</span></template>
      </el-table-column>
      <el-table-column label="操作" width="130" align="center" fixed="right">
        <template #default="scope">
          <el-button text size="small" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:genealogy:edit']">修改</el-button>
          <el-button text size="small" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:genealogy:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="open" :title="title" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="投入批次" prop="parentBatchNo">
              <el-input v-model="form.parentBatchNo" placeholder="原料/半成品批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产出批次" prop="childBatchNo">
              <el-input v-model="form.childBatchNo" placeholder="半成品/成品批次号（空=断点）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="工单号" prop="workOrderNo"><el-input v-model="form.workOrderNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工序名称" prop="opName"><el-input v-model="form.opName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="投入数量" prop="qtyIn"><el-input-number v-model="form.qtyIn" :precision="4" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产出数量" prop="qtyOut"><el-input-number v-model="form.qtyOut" :precision="4" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="产线" prop="lineName"><el-input v-model="form.lineName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作员" prop="operator"><el-input v-model="form.operator" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="供应商" prop="supplierName"><el-input v-model="form.supplierName" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="来源类型" prop="sourceType">
              <el-select v-model="form.sourceType" style="width:100%">
                <el-option v-for="d in qms_genealogy_source" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="谱系时间" prop="traceTime"><el-date-picker v-model="form.traceTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 断点清单弹窗 -->
    <el-dialog v-model="breakOpen" title="谱系断点清单" width="900px" append-to-body>
      <el-table :data="breakList" border size="small">
        <el-table-column label="投入批次" prop="parentBatchNo" min-width="130" />
        <el-table-column label="工单号" prop="workOrderNo" min-width="110" />
        <el-table-column label="物料" min-width="150"><template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template></el-table-column>
        <el-table-column label="断点原因" prop="breakReason" min-width="200" />
        <el-table-column label="创建时间" prop="createTime" min-width="145" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="s"><el-button text size="small" @click="handleUpdate(s.row); breakOpen = false">补录</el-button></template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="QmsTraceGenealogy">
import { ref, reactive, getCurrentInstance } from 'vue'
import { listGenealogy, getGenealogy, addGenealogy, updateGenealogy, delGenealogy, traceCompleteness, traceBreakList } from '@/api/qms/trace'

const { proxy } = getCurrentInstance()
const { qms_genealogy_source } = proxy.useDict('qms_genealogy_source')

const loading = ref(true)
const list = ref([])
const total = ref(0)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const open = ref(false)
const title = ref('')
const breakOpen = ref(false)
const breakList = ref([])
const completeness = ref({})
const queryParams = reactive({ pageNum: 1, pageSize: 10, parentBatchNo: undefined, childBatchNo: undefined, workOrderNo: undefined, sourceType: undefined, breakFlag: undefined })
const form = ref({})

const rules = {
  parentBatchNo: [{ required: true, message: '投入批次号不能为空', trigger: 'blur' }]
}

getList()
loadCompleteness()

function getList() { loading.value = true; listGenealogy(queryParams).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function loadCompleteness() { traceCompleteness().then(res => { completeness.value = res.data }) }
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.id); single.value = sel.length !== 1; multiple.value = !sel.length }

function reset() {
  form.value = { id: undefined, parentBatchNo: undefined, childBatchNo: undefined, workOrderNo: undefined, opName: undefined, materialCode: undefined, materialName: undefined, qtyIn: undefined, qtyOut: undefined, lineName: undefined, operator: undefined, supplierName: undefined, sourceType: 'manual', traceTime: undefined, remark: undefined }
  proxy.resetForm('formRef')
}

function handleAdd() { reset(); open.value = true; title.value = '新增批次谱系' }
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getGenealogy(id).then(res => { form.value = res.data; open.value = true; title.value = '修改批次谱系' })
}
function submitForm() {
  proxy.$refs['formRef'].validate(valid => {
    if (!valid) return
    if (form.value.id) { updateGenealogy(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList(); loadCompleteness() }) }
    else { addGenealogy(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList(); loadCompleteness() }) }
  })
}
function handleDelete(row) {
  const delIds = row.id ? [row.id] : ids.value
  proxy.$modal.confirm('确认删除选中的谱系记录？').then(() => delGenealogy(delIds)).then(() => { proxy.$modal.msgSuccess('删除成功'); getList(); loadCompleteness() }).catch(() => {})
}
function handleExport() { proxy.download('qms/trace/genealogy/export', { ...queryParams }, `谱系数据_${new Date().getTime()}.xlsx`) }
function handleBreakList() { traceBreakList().then(res => { breakList.value = res.data; breakOpen.value = true }) }
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-sub { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
</style>
