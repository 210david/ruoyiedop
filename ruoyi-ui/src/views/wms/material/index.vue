<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--查询条件-->
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="物料编码" prop="materialCode">
            <el-input v-model="queryParams.materialCode" placeholder="请输入物料编码" clearable style="width: 200px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="物料名称" prop="materialName">
            <el-input v-model="queryParams.materialName" placeholder="请输入物料名称" clearable style="width: 600px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="物料类型" prop="materialType">
            <el-select v-model="queryParams.materialType" placeholder="请选择" clearable style="width: 200px">
              <el-option v-for="dict in wms_material_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px">
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
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:material:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['wms:material:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:material:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:material:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table border v-loading="loading" :data="materialList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <!-- 基本信息 -->
          <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
          <el-table-column label="物料名称" prop="materialName" :width="colWidth('materialName', 224)" resizable show-overflow-tooltip />
          <el-table-column label="物料类型" prop="materialType" :width="colWidth('materialType', 100)" resizable align="center">
            <template #default="scope">
              <dict-tag :options="wms_material_type" :value="scope.row.materialType" />
            </template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 225)" resizable show-overflow-tooltip />
          <el-table-column label="单位" prop="unit" :width="colWidth('unit', 80)" resizable align="center">
            <template #default="scope">
              <dict-tag :options="wms_unit" :value="scope.row.unit" />
            </template>
          </el-table-column>
          <el-table-column label="批次管理" prop="isBatchManage" :width="colWidth('isBatchManage', 90)" resizable align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isBatchManage === '1' ? 'success' : 'info'">{{ scope.row.isBatchManage === '1' ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <!-- 效期管理 -->
          <el-table-column label="效期管理" prop="isExpiryManage" :width="colWidth('isExpiryManage', 90)" resizable align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isExpiryManage === '1' ? 'success' : 'info'">{{ scope.row.isExpiryManage === '1' ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="保质期(天)" prop="shelfLifeDays" :width="colWidth('shelfLifeDays', 100)" resizable align="center">
            <template #default="scope">
              {{ scope.row.shelfLifeDays != null ? scope.row.shelfLifeDays : '-' }}
            </template>
          </el-table-column>
          <!-- 库存控制 -->
          <el-table-column label="安全库存下限" prop="safetyStockMin" :width="colWidth('safetyStockMin', 120)" resizable align="right" />
          <el-table-column label="安全库存上限" prop="safetyStockMax" :width="colWidth('safetyStockMax', 120)" resizable align="right" />
          <!-- 其他 -->
          <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:material:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:material:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="materialRef" :model="form" :rules="rules" label-width="100px">
        <!-- 分组一：基本信息 -->
        <el-divider content-position="center">基本信息</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input v-model="form.materialCode" placeholder="请输入物料编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="materialName">
              <el-input v-model="form.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="物料类型" prop="materialType">
              <el-select v-model="form.materialType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in wms_material_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in wms_unit" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="规格型号" prop="specModel">
              <el-input v-model="form.specModel" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组二：效期管理 -->
        <el-divider content-position="center">效期管理</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="效期管理" prop="isExpiryManage">
              <el-radio-group v-model="form.isExpiryManage">
                <el-radio value="0">否</el-radio>
                <el-radio value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次管理" prop="isBatchManage">
              <el-radio-group v-model="form.isBatchManage">
                <el-radio value="0">否</el-radio>
                <el-radio value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="保质期天数" prop="shelfLifeDays">
              <el-input-number v-model="form.shelfLifeDays" :min="0" :precision="0" controls-position="right" placeholder="天" style="width: 50%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="临期预警" prop="expiryAlertDays">
              <el-input-number v-model="form.expiryAlertDays" :min="0" :precision="0" controls-position="right" placeholder="天" style="width: 50%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组三：库存控制 -->
        <el-divider content-position="center">库存控制</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="安全库存下限" prop="safetyStockMin">
              <el-input-number v-model="form.safetyStockMin" :precision="2" :min="0" style="width: 50%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全库存上限" prop="safetyStockMax">
              <el-input-number v-model="form.safetyStockMax" :precision="2" :min="0" style="width: 50%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 分组四：其他信息 -->
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

<script setup name="WmsMaterial">
import { listMaterial, getMaterial, addMaterial, updateMaterial, delMaterial } from '@/api/wms/material'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('wms_material_index')
const { wms_material_type, wms_unit } = proxy.useDict('wms_material_type', 'wms_unit')

const materialList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    materialCode: undefined,
    materialName: undefined,
    materialType: undefined,
    status: undefined
  },
  rules: {
    materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'blur' }],
    materialName: [{ required: true, message: '物料名称不能为空', trigger: 'blur' }],
    unit: [{ required: true, message: '计量单位不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listMaterial(queryParams.value).then(response => {
    materialList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.materialId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    materialId: undefined,
    materialCode: undefined,
    materialName: undefined,
    materialType: '0',
    specModel: undefined,
    unit: undefined,
    shelfLifeDays: undefined,
    expiryAlertDays: 30,
    isExpiryManage: '0',
    safetyStockMin: 0,
    safetyStockMax: 0,
    isBatchManage: '0',
    status: '0',
    remark: undefined
  }
  proxy.resetForm('materialRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加物料'
}

function handleUpdate(row) {
  reset()
  const materialId = row.materialId || ids.value[0]
  getMaterial(materialId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改物料'
  })
}

function submitForm() {
  proxy.$refs['materialRef'].validate(valid => {
    if (valid) {
      if (form.value.materialId != undefined) {
        updateMaterial(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addMaterial(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const materialIds = row.materialId || ids.value
  proxy.$modal.confirm('是否确认删除物料编号为"' + materialIds + '"的数据项？').then(function () {
    return delMaterial(materialIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('wms/material/export', { ...queryParams.value }, `material_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

getList()
</script>
