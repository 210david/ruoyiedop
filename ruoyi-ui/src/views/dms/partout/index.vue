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

    <!-- 新增出库弹窗 -->
    <el-dialog v-model="open" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="partoutRef" :model="form" :rules="rules" label-width="100px">
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
            <el-form-item label="备件" prop="partId">
              <el-select v-model="form.partId" filterable placeholder="请选择备件（仅显示有库存的）" style="width: 100%" @change="onPartChange">
                <el-option v-for="item in spareOptions" :key="item.partId" :label="item.partCode + ' - ' + item.partName + '（库存：' + (item.currentStock || 0) + '）'" :value="item.partId" />
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
          <el-col :span="12">
            <el-form-item label="出库数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0.01" :max="form.currentStock || 9999" :precision="2" controls-position="right" style="width: 100%" />
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
            <el-form-item label="领用人" prop="receiver">
              <el-select v-model="form.receiver" filterable clearable placeholder="请选择领用人" style="width: 100%" @change="onReceiverChange">
                <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.nickName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="领用部门" prop="deptId">
              <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择领用部门" check-strictly clearable style="width: 100%" @change="onDeptChange" />
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
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsPartOut">
import { listPartOut, addPartOut, delPartOut } from '@/api/dms/partout'
import { listSparepart } from '@/api/dms/sparepart'
import { listUser, deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_partout_index')
const { dms_partout_type, wms_unit } = proxy.useDict('dms_partout_type', 'wms_unit')

const list = ref([])
const spareOptions = ref([])
const userOptions = ref([])
const deptOptions = ref([])
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
    partId: [{ required: true, message: '备件不能为空', trigger: 'change' }],
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
  title.value = '新增出库'
  getSpareOptions()
}
function reset() {
  form.value = {
    documentCode: undefined, targetType: undefined, partId: undefined, partCode: undefined, partName: undefined,
    unit: undefined, currentStock: undefined, quantity: 1, operateDate: today(),
    receiver: undefined, supplierOrDept: undefined, deptId: undefined, operatorName: undefined, remark: undefined
  }
  proxy.resetForm('partoutRef')
}
/** 领用人变更时，自动带出其所在部门 */
function onReceiverChange(val) {
  const user = userOptions.value.find(u => u.nickName === val)
  if (user && user.deptId) {
    form.value.deptId = user.deptId
    form.value.supplierOrDept = user.dept ? user.dept.deptName : findDeptName(deptOptions.value, user.deptId)
  } else {
    form.value.deptId = undefined
    form.value.supplierOrDept = undefined
  }
}
/** 部门树选择变更时，同步部门名称到 supplierOrDept */
function onDeptChange(val) {
  form.value.supplierOrDept = findDeptName(deptOptions.value, val)
}
/** 递归查找部门名称 */
function findDeptName(tree, targetId) {
  for (const node of tree) {
    if (node.id === targetId) return node.label
    if (node.children) {
      const found = findDeptName(node.children, targetId)
      if (found) return found
    }
  }
  return ''
}
/** 获取系统用户列表 */
function getUserOptions() {
  listUser({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { userOptions.value = res.rows })
}
/** 获取部门树 */
function getDeptTree() {
  deptTreeSelect().then(res => { deptOptions.value = res.data })
}
function onPartChange(val) {
  const part = spareOptions.value.find(i => i.partId === val)
  if (part) {
    form.value.partCode = part.partCode
    form.value.partName = part.partName
    form.value.unit = part.unit || ''
    form.value.currentStock = part.currentStock || 0
    // 重置出库数量，确保不超过库存
    if (form.value.quantity > form.value.currentStock) {
      form.value.quantity = form.value.currentStock
    }
  }
}
function submitForm() {
  proxy.$refs['partoutRef'].validate(valid => {
    if (valid) {
      addPartOut(form.value).then(() => { proxy.$modal.msgSuccess('出库成功'); open.value = false; getList() })
    }
  })
}
function cancel() { open.value = false; reset() }
function handleExport() { proxy.download('dms/sparepart/partout/export', { ...queryParams.value }, `partout_${new Date().getTime()}.xlsx`) }
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除选中的出库记录？').then(() => delPartOut(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

/** 获取备件列表，只取库存大于0的 */
function getSpareOptions() {
  listSparepart({ pageNum: 1, pageSize: 99999, status: '0' }).then(res => {
    spareOptions.value = (res.rows || []).filter(item => item.currentStock != null && item.currentStock > 0)
  })
}

onActivated(() => {
  getList()
})

getList()
getSpareOptions()
getUserOptions()
getDeptTree()
</script>
