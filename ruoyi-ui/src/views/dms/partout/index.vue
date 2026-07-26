<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="单据号" prop="documentCode">
        <el-input v-model="queryParams.documentCode" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="出库类型" prop="targetType">
        <el-select v-model="queryParams.targetType" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="dict in dms_partout_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="领用部门" prop="supplierOrDept">
        <el-input v-model="queryParams.supplierOrDept" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:partout:add']">新增出库</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:partstock:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['dms:partout:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="单据号" prop="documentCode" :width="colWidth('documentCode', 140)" resizable />
      <el-table-column label="备件编号" prop="partCode" :width="colWidth('partCode', 120)" resizable />
      <el-table-column label="备件名称" prop="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
      <el-table-column label="出库类型" prop="targetType" :width="colWidth('targetType', 100)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="dms_partout_type" :value="scope.row.targetType" />
        </template>
      </el-table-column>
      <el-table-column label="领用部门" prop="supplierOrDept" :width="colWidth('supplierOrDept', 150)" resizable show-overflow-tooltip />
      <el-table-column label="领用人" prop="receiver" :width="colWidth('receiver', 80)" resizable />
      <el-table-column label="出库数量" prop="quantity" :width="colWidth('quantity', 90)" resizable align="center" />
      <el-table-column label="出库日期" prop="operateDate" :width="colWidth('operateDate', 110)" resizable align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['dms:partout:query']">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:partout:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:partstock:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增出库弹窗 -->
    <el-dialog v-model="open" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="partoutRef" :model="form" :rules="rules" label-width="100px" :disabled="formDisabled">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
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
            <el-form-item label="出库类型" prop="targetType">
              <el-select v-model="form.targetType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in dms_partout_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>备件信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="24">
            <el-form-item label="备件" prop="stockId">
              <el-select v-model="form.stockId" filterable placeholder="请选择备件（仅显示有库存的）" style="width: 100%" @change="onPartChange" :disabled="!!form.recordId">
                <el-option v-for="item in spareOptions" :key="item.stockId" :label="item.partCode + ' - ' + item.partName + '（' + (item.warehouseName || '默认') + '：' + (item.currentStock || 0) + '）'" :value="item.stockId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数量" prop="currentStock">
              <el-input v-model="form.currentStock" disabled />
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
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>领用信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row>
          <el-col :span="12">
            <el-form-item label="出库日期" prop="operateDate">
              <el-date-picker v-model="form.operateDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出库数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0.01" :max="form.currentStock || 9999" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="领用人" prop="receiver">
              <el-input v-model="form.receiver" readonly placeholder="请选择领用人" style="width: 100%" @click="openReceiverPicker">
                <template #append>
                  <el-button icon="Search" @click="openReceiverPicker" />
                </template>
                <template #suffix>
                  <el-icon v-if="form.receiver" class="clear-icon" @click.stop="clearReceiver"><CircleClose /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="领用部门" prop="deptId">
              <el-input v-model="form.supplierOrDept" readonly placeholder="请选择领用部门" style="width: 100%" @click="openDeptPicker">
                <template #append>
                  <el-button icon="Search" @click="openDeptPicker" />
                </template>
                <template #suffix>
                  <el-icon v-if="form.supplierOrDept" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm" v-if="!formDisabled">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 领用人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择领用人" @confirm="onReceiverPickerConfirm" />

    <!-- 领用部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择领用部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />
  </div>
</template>

<script setup name="DmsPartOut">
import { CircleClose } from '@element-plus/icons-vue'
import { listPartOut, addPartOut, delPartOut, getPartOut, updatePartOut } from '@/api/dms/partout'
import { listPartLedger } from '@/api/dms/partledger'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_partout_index')
const { dms_partout_type, wms_unit } = proxy.useDict('dms_partout_type', 'wms_unit')

const list = ref([])
const spareOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const formDisabled = ref(false)

/** 获取当天日期 YYYY-MM-DD */
function today() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 校验出库数量不能大于库存数量 */
function validateQuantity(rule, value, callback) {
  if (value == null || value <= 0) {
    callback(new Error('出库数量必须大于0'))
  } else if (form.value.currentStock != null && value > form.value.currentStock) {
    callback(new Error('出库数量不能大于库存数量（' + form.value.currentStock + '）'))
  } else {
    callback()
  }
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, documentCode: undefined, targetType: undefined, supplierOrDept: undefined },
  rules: {
    targetType: [{ required: true, message: '出库类型不能为空', trigger: 'change' }],
    stockId: [{ required: true, message: '备件不能为空', trigger: 'change' }],
    quantity: [{ required: true, validator: validateQuantity, trigger: 'blur' }],
    operateDate: [{ required: true, message: '出库日期不能为空', trigger: 'change' }],
    receiver: [{ required: true, message: '领用人不能为空', trigger: 'change' }],
    deptId: [{ required: true, message: '领用部门不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listPartOut(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); multiple.value = !selection.length }
function handleAdd() {
  reset(); open.value = true
  formDisabled.value = false
  title.value = '新增出库'
  getSpareOptions()
}
function handleView(row) {
  reset(); formDisabled.value = true
  getPartOut(row.recordId).then(res => { form.value = res.data; open.value = true; title.value = '查看出库记录' })
}
function handleUpdate(row) {
  reset(); formDisabled.value = false
  getPartOut(row.recordId).then(res => { form.value = res.data; open.value = true; title.value = '修改出库记录'; getSpareOptions() })
}
function reset() {
  form.value = {
    documentCode: undefined, targetType: undefined, partId: undefined, stockId: undefined, partCode: undefined, partName: undefined,
    unit: undefined, currentStock: undefined, warehouseName: undefined, quantity: 1, operateDate: today(),
    receiver: undefined, supplierOrDept: undefined, deptId: undefined, operatorName: undefined, remark: undefined
  }
  proxy.resetForm('partoutRef')
}
/** 打开领用人选择弹窗 */
function openReceiverPicker() {
  proxy.$refs.userPickerRef.open(undefined)
}
/** 领用人选择确认回调 */
function onReceiverPickerConfirm(user) {
  form.value.receiver = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.supplierOrDept = user.deptName
  }
}
/** 清除领用人 */
function clearReceiver() {
  form.value.receiver = undefined
}
/** 打开部门选择弹窗 */
function openDeptPicker() {
  proxy.$refs.deptPickerRef.open(form.value.deptId)
}
/** 部门选择确认回调 */
function onDeptPickerConfirm(dept) {
  form.value.deptId = dept.deptId
  form.value.supplierOrDept = dept.deptName
}
/** 清除部门 */
function clearDept() {
  form.value.deptId = undefined
  form.value.supplierOrDept = undefined
}
function onPartChange(val) {
  const item = spareOptions.value.find(i => i.stockId === val)
  if (item) {
    form.value.partId = item.partId
    form.value.stockId = item.stockId
    form.value.partCode = item.partCode
    form.value.partName = item.partName
    form.value.unit = item.unit || ''
    form.value.currentStock = item.currentStock || 0
    form.value.warehouseName = item.warehouseName || '备件库'
    // 重置出库数量，确保不超过库存
    if (form.value.quantity > form.value.currentStock) {
      form.value.quantity = form.value.currentStock
    }
  }
}
function submitForm() {
  proxy.$refs['partoutRef'].validate(valid => {
    if (valid) {
      if (form.value.recordId != undefined) { updatePartOut(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addPartOut(form.value).then(() => { proxy.$modal.msgSuccess('出库成功'); open.value = false; getList() }) }
    }
  })
}
function cancel() { open.value = false; reset() }
function handleExport() { proxy.download('dms/sparepart/partout/export', { ...queryParams.value }, `partout_${new Date().getTime()}.xlsx`) }
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除选中的出库记录？').then(() => delPartOut(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

/** 获取备件列表，只取库存大于0的 */
function getSpareOptions() {
  listPartLedger({ pageNum: 1, pageSize: 99999 }).then(res => {
    spareOptions.value = (res.rows || []).filter(item => item.currentStock != null && Number(item.currentStock) > 0)
  })
}

onActivated(() => {
  getList()
})

getList()
getSpareOptions()
</script>

<style scoped>
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
:deep(.el-input.is-disabled .el-input__inner) {
  cursor: pointer;
}
</style>
