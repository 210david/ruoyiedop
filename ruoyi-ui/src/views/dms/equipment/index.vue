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

    <el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
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

    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="equipmentRef" :model="form" :rules="rules" label-width="100px">
        <!-- 分组一：基本信息 -->
        <el-divider content-position="center">基本信息</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="设备编号" prop="equipmentCode"><el-input v-model="form.equipmentCode" placeholder="请输入" /></el-form-item></el-col>
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
        <el-divider content-position="center">资产信息</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="制造商" prop="manufacturer"><el-input v-model="form.manufacturer" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="供应商" prop="supplier"><el-input v-model="form.supplier" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="购置日期" prop="purchaseDate"><el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="原值" prop="originalValue"><el-input-number v-model="form.originalValue" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>

        <!-- 分组三：使用信息 -->
        <el-divider content-position="center">使用信息</el-divider>
        <el-row>
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
        <el-divider content-position="center">图片与附件</el-divider>
        <el-row>
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
        <el-divider content-position="center">其他信息</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 设备详情抽屉 -->
    <dms-equipment-view-drawer ref="equipmentViewRef" />

    <!-- 设备履历弹窗 -->
    <el-dialog title="设备履历" v-model="historyOpen" width="850px" append-to-body>
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

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('dms_equipment_index')
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
    equipmentCode: [{ required: true, message: '设备编号不能为空', trigger: 'blur' }],
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
