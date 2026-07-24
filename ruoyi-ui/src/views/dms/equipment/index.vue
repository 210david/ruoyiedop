<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="设备编号" prop="equipmentCode">
        <el-input v-model="queryParams.equipmentCode" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="设备名称" prop="equipmentName">
        <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="设备状态" prop="equipmentStatus">
        <el-select v-model="queryParams.equipmentStatus" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in dms_equipment_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:equipment:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:equipment:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:equipment:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['dms:equipment:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="设备编号" prop="equipmentCode" :width="colWidth('equipmentCode', 130)" resizable />
      <el-table-column label="设备名称" prop="equipmentName" show-overflow-tooltip />
      <el-table-column label="分类" prop="categoryName" :width="colWidth('categoryName', 120)" resizable />
      <el-table-column label="型号" prop="model" :width="colWidth('model', 120)" resizable />
      <el-table-column label="使用部门" prop="deptName" :width="colWidth('deptName', 120)" resizable />
      <el-table-column label="安装位置" prop="installLocation" :width="colWidth('installLocation', 120)" resizable show-overflow-tooltip />
      <el-table-column label="责任人" prop="responsibleName" :width="colWidth('responsibleName', 80)" resizable />
      <el-table-column label="设备状态" prop="equipmentStatus" :width="colWidth('equipmentStatus', 90)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="dms_equipment_status" :value="scope.row.equipmentStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['dms:equipment:query']">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:equipment:edit']">修改</el-button>
          <el-button link type="info" @click="handleHistory(scope.row)">履历</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:equipment:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="equipmentRef" :model="form" :rules="rules" label-width="100px">
                <!-- 分组一：基本信息 -->
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c4">
        <el-row>
          <el-col :span="12"><el-form-item label="设备编号" prop="equipmentCode"><el-input v-model="form.equipmentCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称" prop="equipmentName"><el-input v-model="form.equipmentName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备分类" prop="categoryId"><el-tree-select v-model="form.categoryId" :data="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" value-key="categoryId" placeholder="请选择" check-strictly style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备状态" prop="equipmentStatus">
            <el-select v-model="form.equipmentStatus" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_equipment_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="型号" prop="model"><el-input v-model="form.model" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="序列号" prop="serialNumber"><el-input v-model="form.serialNumber" placeholder="请输入" /></el-form-item></el-col>
        </el-row>

        <!-- 分组二：资产信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>资产信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row>
          <el-col :span="12"><el-form-item label="制造商" prop="manufacturer"><el-input v-model="form.manufacturer" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="供应商" prop="supplier"><el-input v-model="form.supplier" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="购置日期" prop="purchaseDate"><el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="原值" prop="originalValue"><el-input-number v-model="form.originalValue" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>

        <!-- 分组三：使用信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg></span>使用信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="责任人" prop="responsibleId">
              <el-select v-model="form.responsibleId" filterable clearable placeholder="请选择责任人" style="width: 100%" @change="onResponsibleChange">
                <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用部门" prop="deptId">
              <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="安装位置" prop="installLocation">
              <el-input v-model="form.installLocation" placeholder="请输入安装位置" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组四：图片与附件 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></span>图片与附件</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="设备图片" prop="equipmentImage">
              <image-upload v-model="form.equipmentImage" :limit="5" :fileSize="10" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="附件资料" prop="attachmentUrl">
              <file-upload v-model="form.attachmentUrl" :fileSize="50" :fileType="['bmp', 'gif', 'jpg', 'jpeg', 'png', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf', 'rar', 'zip', 'gz', 'bz2', 'mp4', 'avi', 'rmvb']" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组五：其他信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 设备详情抽屉 -->
    <dms-equipment-view-drawer ref="equipmentViewRef" />

    <!-- 设备履历弹窗 -->
    <el-dialog v-model="historyOpen" width="850px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><polyline points="12 7 12 12 15 14"/></svg></div>
          <span class="rd-detail-header-title">设备履历</span>
        </div>
      </template>
      <el-tabs v-model="historyTab">
        <el-tab-pane label="工单记录" name="orders">
          <el-table :data="historyOrders" border size="small" @header-dragend="onHeaderDragEnd">
            <el-table-column label="工单号" prop="orderNo" :width="colWidth('orderNo', 150)" resizable />
            <el-table-column label="工单类型" prop="orderType" :width="colWidth('orderType', 100)" resizable align="center">
              <template #default="scope"><dict-tag :options="dms_order_type" :value="scope.row.orderType" /></template>
            </el-table-column>
            <el-table-column label="故障描述" prop="faultDescription" show-overflow-tooltip />
            <el-table-column label="状态" prop="orderStatus" :width="colWidth('orderStatus', 90)" resizable align="center">
              <template #default="scope"><dict-tag :options="dms_order_status" :value="scope.row.orderStatus" /></template>
            </el-table-column>
            <el-table-column label="报修时间" prop="reportTime" :width="colWidth('reportTime', 150)" resizable align="center" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="变更日志" name="logs">
          <el-table :data="historyLogs" border size="small" @header-dragend="onHeaderDragEnd">
            <el-table-column label="变更类型" prop="changeType" :width="colWidth('changeType', 100)" resizable align="center">
              <template #default="scope">
                <el-tag>{{ {0:'状态变更',1:'位置变更',2:'部门变更',3:'责任人变更',4:'调拨'}[scope.row.changeType] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="原值" prop="oldValue" :width="colWidth('oldValue', 80)" resizable />
            <el-table-column label="新值" prop="newValue" :width="colWidth('newValue', 80)" resizable />
            <el-table-column label="变更原因" prop="changeReason" show-overflow-tooltip />
            <el-table-column label="操作人" prop="operatorName" :width="colWidth('operatorName', 80)" resizable />
            <el-table-column label="时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup name="DmsEquipment">
import { listEquipment, getEquipment, addEquipment, updateEquipment, delEquipment, getEquipmentHistory, listEquipmentLog } from '@/api/dms/equipment'
import { listCategory } from '@/api/dms/category'
import { deptTreeSelect, listUser } from '@/api/system/user'
import DmsEquipmentViewDrawer from './view.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c4","c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_equipment_index')
const { dms_equipment_status, dms_order_type, dms_order_status } = proxy.useDict('dms_equipment_status', 'dms_order_type', 'dms_order_status')

const list = ref([])
const categoryOptions = ref([])
const deptOptions = ref([])
const userOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const historyOpen = ref(false)
const historyTab = ref('orders')
const historyOrders = ref([])
const historyLogs = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, equipmentCode: undefined, equipmentName: undefined, equipmentStatus: undefined },
  rules: {
    equipmentName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listEquipment(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function getCategoryTree() {
  listCategory().then(res => { categoryOptions.value = proxy.handleTree(res.data, 'categoryId') })
}
function getDeptTree() {
  deptTreeSelect().then(res => { deptOptions.value = res.data })
}
function getUserList() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows })
}
/** 选择责任人后自动带出其所在部门 */
function onResponsibleChange(userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user && user.deptId) {
      form.value.deptId = user.deptId
    }
  }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.equipmentId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = {
    equipmentCode: undefined, equipmentName: undefined, categoryId: undefined, model: undefined, serialNumber: undefined,
    manufacturer: undefined, supplier: undefined, purchaseDate: undefined, originalValue: undefined,
    deptId: undefined, installLocation: undefined, equipmentStatus: '0', responsibleId: undefined,
    equipmentImage: undefined, attachmentUrl: undefined, status: '0', remark: undefined
  }
  proxy.resetForm('equipmentRef')
}
function handleAdd() { reset(); open.value = true; title.value = '添加设备' }
function handleUpdate(row) {
  reset()
  getEquipment(row.equipmentId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改设备' })
}
function submitForm() {
  proxy.$refs['equipmentRef'].validate(valid => {
    if (valid) {
      if (form.value.equipmentId != undefined) { updateEquipment(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addEquipment(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const equipmentIds = row.equipmentId || ids.value; proxy.$modal.confirm('确认删除编号为"' + equipmentIds + '"的数据？').then(() => delEquipment(equipmentIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('dms/equipment/export', { ...queryParams.value }, `equipment_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function handleHistory(row) {
  historyTab.value = 'orders'
  historyOpen.value = true
  getEquipmentHistory(row.equipmentId).then(res => { historyOrders.value = res.data || [] })
  listEquipmentLog({ equipmentId: row.equipmentId, pageNum: 1, pageSize: 100 }).then(res => { historyLogs.value = res.rows || [] })
}
/** 查看设备详情 */
function handleView(row) {
  proxy.$refs.equipmentViewRef.open(row.equipmentId)
}
getCategoryTree()
getDeptTree()
getUserList()
getList()
</script>
