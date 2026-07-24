<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="单据号" prop="documentCode">
        <el-input v-model="queryParams.documentCode" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="入库类型" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="dict in dms_partin_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="供应商" prop="supplierOrDept">
        <el-input v-model="queryParams.supplierOrDept" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:partin:add']">新增入库</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:partstock:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['dms:partin:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="单据号" prop="documentCode" :width="colWidth('documentCode', 140)" resizable />
      <el-table-column label="备件编号" prop="partCode" :width="colWidth('partCode', 120)" resizable />
      <el-table-column label="备件名称" prop="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
      <el-table-column label="入库类型" prop="sourceType" :width="colWidth('sourceType', 100)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="dms_partin_type" :value="scope.row.sourceType" />
        </template>
      </el-table-column>
      <el-table-column label="供应商" prop="supplierOrDept" :width="colWidth('supplierOrDept', 150)" resizable show-overflow-tooltip />
      <el-table-column label="存放位置" prop="storageLocation" :width="colWidth('storageLocation', 120)" resizable show-overflow-tooltip />
      <el-table-column label="数量" prop="quantity" :width="colWidth('quantity', 80)" resizable align="center" />
      <el-table-column label="入库日期" prop="operateDate" :width="colWidth('operateDate', 110)" resizable align="center" />
      <el-table-column label="变更前" prop="beforeStock" :width="colWidth('beforeStock', 80)" resizable align="center" />
      <el-table-column label="变更后" prop="afterStock" :width="colWidth('afterStock', 80)" resizable align="center" />
      <el-table-column label="操作人" prop="operatorName" :width="colWidth('operatorName', 80)" resizable />
      <el-table-column label="操作时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
      <el-table-column label="操作" width="80" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:partstock:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增入库弹窗 -->
    <el-dialog v-model="open" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="partinRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row>
          <el-col :span="12">
            <el-form-item label="单据号" prop="documentCode">
              <el-input v-model="form.documentCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库类型" prop="sourceType">
              <el-select v-model="form.sourceType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in dms_partin_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备件信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="24">
            <el-form-item label="备件" prop="partId">
              <el-select v-model="form.partId" filterable placeholder="请选择备件" style="width: 100%" @change="onPartChange">
                <el-option v-for="item in spareOptions" :key="item.partId" :label="item.partCode + ' - ' + item.partName" :value="item.partId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierOrDept">
              <el-input v-model="form.supplierOrDept" placeholder="选择备件后自动带出" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <dict-tag :options="wms_unit" :value="form.unit" />
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>入库明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row>
          <el-col :span="12">
            <el-form-item label="存放位置" prop="storageLocation">
              <el-input v-model="form.storageLocation" placeholder="请输入存放位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库日期" prop="operateDate">
              <el-date-picker v-model="form.operateDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsPartIn">
import { listPartIn, addPartIn, delPartIn } from '@/api/dms/partin'
import { listSparepart } from '@/api/dms/sparepart'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_partin_index')
const { dms_partin_type, wms_unit } = proxy.useDict('dms_partin_type', 'wms_unit')

const list = ref([])
const spareOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')

/** 获取当天日期 YYYY-MM-DD */
function today() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, documentCode: undefined, sourceType: undefined, supplierOrDept: undefined },
  rules: {
    sourceType: [{ required: true, message: '入库类型不能为空', trigger: 'change' }],
    partId: [{ required: true, message: '备件不能为空', trigger: 'change' }],
    quantity: [{ required: true, message: '数量不能为空', trigger: 'blur' }],
    storageLocation: [{ required: true, message: '存放位置不能为空', trigger: 'blur' }],
    operateDate: [{ required: true, message: '入库日期不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listPartIn(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); multiple.value = !selection.length }
function handleAdd() {
  reset(); open.value = true
  title.value = '新增入库'
  getSpareOptions()
}
function reset() {
  form.value = {
    documentCode: undefined, sourceType: undefined, partId: undefined, partCode: undefined, partName: undefined,
    unit: undefined, supplierOrDept: undefined, storageLocation: undefined, operateDate: today(),
    quantity: 1, operatorName: undefined, remark: undefined
  }
  proxy.resetForm('partinRef')
}
function onPartChange(val) {
  const part = spareOptions.value.find(i => i.partId === val)
  if (part) {
    form.value.partCode = part.partCode
    form.value.partName = part.partName
    form.value.unit = part.unit || ''
    form.value.supplierOrDept = part.supplier || ''
    // 如果备件已有存放位置，自动带出
    if (part.storageLocation) {
      form.value.storageLocation = part.storageLocation
    }
  }
}
function submitForm() {
  proxy.$refs['partinRef'].validate(valid => {
    if (valid) {
      addPartIn(form.value).then(() => { proxy.$modal.msgSuccess('入库成功'); open.value = false; getList() })
    }
  })
}
function cancel() { open.value = false; reset() }
function handleExport() { proxy.download('dms/sparepart/partin/export', { ...queryParams.value }, `partin_${new Date().getTime()}.xlsx`) }
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除选中的入库记录？').then(() => delPartIn(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

function getSpareOptions() {
  listSparepart({ pageNum: 1, pageSize: 99999, status: '0' }).then(res => { spareOptions.value = res.rows })
}

onActivated(() => {
  getList()
})

getList()
getSpareOptions()
</script>
