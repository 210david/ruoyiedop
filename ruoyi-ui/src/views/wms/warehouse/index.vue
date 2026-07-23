<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="编码" prop="warehouseCode">
        <el-input v-model="queryParams.warehouseCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="名称" prop="warehouseName">
        <el-input v-model="queryParams.warehouseName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="节点类型" prop="nodeType">
        <el-select v-model="queryParams.nodeType" placeholder="请选择" clearable style="width: 160px">
          <el-option label="仓库" value="1" />
          <el-option label="仓区" value="2" />
          <el-option label="仓位" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 160px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['wms:warehouse:add']">新增仓库</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-if="refreshTable" v-loading="loading" :data="warehouseList" row-key="warehouseId" :default-expand-all="isExpandAll" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" @header-dragend="onHeaderDragEnd">
      <el-table-column prop="warehouseCode" label="编码" :width="colWidth('warehouseCode', 200)" resizable />
      <el-table-column prop="warehouseName" label="名称" :show-overflow-tooltip="true" />
      <el-table-column prop="nodeType" label="类型" :width="colWidth('nodeType', 90)" resizable align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.nodeType === '1'" type="primary" size="small">仓库</el-tag>
          <el-tag v-else-if="scope.row.nodeType === '2'" type="success" size="small">仓区</el-tag>
          <el-tag v-else-if="scope.row.nodeType === '3'" type="warning" size="small">仓位</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="areaType" label="库区类型" :width="colWidth('areaType', 100)" resizable align="center">
        <template #default="scope">
          <dict-tag v-if="scope.row.nodeType === '2'" :options="wms_area_type" :value="scope.row.areaType" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="locationType" label="库位类型" :width="colWidth('locationType', 100)" resizable align="center">
        <template #default="scope">
          <dict-tag v-if="scope.row.nodeType === '3'" :options="wms_location_type" :value="scope.row.locationType" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="capacity" label="容量" :width="colWidth('capacity', 90)" resizable align="right">
        <template #default="scope">
          <span v-if="scope.row.nodeType === '3'">{{ scope.row.capacity }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="locationStatus" label="库位状态" :width="colWidth('locationStatus', 90)" resizable align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.nodeType === '3'" :type="locationStatusType(scope.row.locationStatus)" size="small">{{ locationStatusText(scope.row.locationStatus) }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="deptName" label="管理部门" :width="colWidth('deptName', 110)" resizable align="center">
        <template #default="scope">
          <span v-if="scope.row.nodeType === '1'">{{ scope.row.deptName || '-' }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="managerName" label="负责人" :width="colWidth('managerName', 90)" resizable align="center">
        <template #default="scope">
          <span v-if="scope.row.nodeType === '1'">{{ scope.row.managerName || '-' }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.nodeType !== '3'" link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['wms:warehouse:add']">新增</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:warehouse:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:warehouse:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="warehouseRef" :model="form" :rules="rules" label-width="100px">
        <!-- 基本信息 -->
        <el-divider content-position="center">基本信息</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="上级节点">
              <el-input :value="form.parentName || '无（顶级仓库）'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="节点类型" prop="nodeType">
              <el-radio-group v-model="form.nodeType" disabled>
                <el-radio value="1">仓库</el-radio>
                <el-radio value="2">仓区</el-radio>
                <el-radio value="3">仓位</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="编码" prop="warehouseCode">
              <el-input v-model="form.warehouseCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" prop="warehouseName">
              <el-input v-model="form.warehouseName" :placeholder="form.nodeType === '1' ? '仓库名称' : form.nodeType === '2' ? '仓区名称' : '仓位名称'" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 仓库级字段 -->
        <template v-if="form.nodeType === '1'">
          <el-divider content-position="center">仓库信息</el-divider>
          <el-row>
            <el-col :span="12">
              <el-form-item label="管理部门" prop="deptId">
                <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="负责人" prop="managerId">
                <el-select v-model="form.managerId" filterable placeholder="请选择负责人" clearable style="width: 100%">
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="仓库地址" prop="address">
            <el-input v-model="form.address" type="textarea" placeholder="请输入仓库地址" />
          </el-form-item>
        </template>

        <!-- 仓区级字段 -->
        <template v-if="form.nodeType === '2'">
          <el-divider content-position="center">仓区信息</el-divider>
          <el-row>
            <el-col :span="12">
              <el-form-item label="库区类型" prop="areaType">
                <el-select v-model="form.areaType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in wms_area_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 仓位级字段 -->
        <template v-if="form.nodeType === '3'">
          <el-divider content-position="center">仓位信息</el-divider>
          <el-row>
            <el-col :span="12">
              <el-form-item label="库位类型" prop="locationType">
                <el-select v-model="form.locationType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in wms_location_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="库位容量" prop="capacity">
                <el-input-number v-model="form.capacity" :precision="2" :min="0" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 通用字段 -->
        <el-divider content-position="center">其他信息</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsWarehouse">
import { listWarehouseTree, getWarehouse, addWarehouse, updateWarehouse, delWarehouse } from '@/api/wms/warehouse'
import { deptTreeSelect, listUser } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_warehouse_index')
const { wms_area_type, wms_location_type } = proxy.useDict('wms_area_type', 'wms_location_type')

const warehouseList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref('')
const isExpandAll = ref(true)
const refreshTable = ref(true)
const deptOptions = ref([])
const userOptions = ref([])

/** 查询部门下拉树 */
function getDeptTree() {
  deptTreeSelect().then(response => {
    deptOptions.value = response.data
  })
}

/** 查询用户列表 */
function getUserList() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(response => {
    userOptions.value = response.rows
  })
}

const data = reactive({
  form: {},
  queryParams: {
    warehouseCode: undefined,
    warehouseName: undefined,
    nodeType: undefined,
    status: undefined
  },
  rules: {
    nodeType: [{ required: true, message: '请选择节点类型', trigger: 'change' }],
    warehouseName: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listWarehouseTree(queryParams.value).then(response => {
    warehouseList.value = proxy.handleTree(response.data, 'warehouseId', 'parentId')
    loading.value = false
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

function locationStatusType(status) {
  if (status === '0') return 'success'
  if (status === '1') return 'warning'
  if (status === '2') return 'danger'
  return 'info'
}

function locationStatusText(status) {
  if (status === '0') return '空闲'
  if (status === '1') return '占用'
  if (status === '2') return '锁定'
  return '-'
}

function reset() {
  form.value = {
    warehouseId: undefined,
    parentId: 0,
    parentName: undefined,
    warehouseCode: undefined,
    warehouseName: undefined,
    nodeType: '1',
    deptId: undefined,
    managerId: undefined,
    address: undefined,
    areaType: '0',
    locationType: '0',
    capacity: 0,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('warehouseRef')
}

function handleAdd(row) {
  reset()
  if (row && row.warehouseId) {
    form.value.parentId = row.warehouseId
    form.value.parentName = row.warehouseName
    // 根据父节点类型自动设置子节点类型
    if (row.nodeType === '1') {
      form.value.nodeType = '2'
    } else if (row.nodeType === '2') {
      form.value.nodeType = '3'
    }
  } else {
    form.value.parentId = 0
    form.value.parentName = '无（顶级仓库）'
    form.value.nodeType = '1'
  }
  open.value = true
  title.value = '新增' + (form.value.nodeType === '1' ? '仓库' : form.value.nodeType === '2' ? '仓区' : '仓位')
}

function handleUpdate(row) {
  reset()
  getWarehouse(row.warehouseId).then(response => {
    form.value = response.data
    // 查找父节点名称
    if (form.value.parentId && form.value.parentId !== 0) {
      findParentName(warehouseList.value, form.value.parentId)
    } else {
      form.value.parentName = '无（顶级仓库）'
    }
    open.value = true
    title.value = '修改' + (form.value.nodeType === '1' ? '仓库' : form.value.nodeType === '2' ? '仓区' : '仓位')
  })
}

function findParentName(list, parentId) {
  for (let item of list) {
    if (item.warehouseId === parentId) {
      form.value.parentName = item.warehouseName
      return true
    }
    if (item.children && item.children.length) {
      if (findParentName(item.children, parentId)) return true
    }
  }
  return false
}

function submitForm() {
  proxy.$refs['warehouseRef'].validate(valid => {
    if (valid) {
      if (form.value.warehouseId != undefined) {
        updateWarehouse(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addWarehouse(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除"' + row.warehouseName + '"？').then(function () {
    return delWarehouse(row.warehouseId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function cancel() {
  open.value = false
  reset()
}

getList()
getDeptTree()
getUserList()
</script>
