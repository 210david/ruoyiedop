<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="备件编号" prop="partCode">
        <el-input v-model="queryParams.partCode" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="备件名称" prop="partName">
        <el-input v-model="queryParams.partName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="备件类别" prop="partType">
        <el-select v-model="queryParams.partType" placeholder="请选择" clearable style="width: 160px">
          <el-option v-for="dict in dms_part_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:sparepart:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:sparepart:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:sparepart:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['dms:sparepart:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="备件编号" prop="partCode" :width="colWidth('partCode', 140)" resizable />
      <el-table-column label="备件名称" prop="partName" show-overflow-tooltip />
      <el-table-column label="备件类别" prop="partType" :width="colWidth('partType', 100)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="dms_part_type" :value="scope.row.partType" />
        </template>
      </el-table-column>
      <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 150)" resizable show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="wms_unit" :value="scope.row.unit" />
        </template>
      </el-table-column>
      <el-table-column label="供应商" prop="supplier" :width="colWidth('supplier', 140)" resizable show-overflow-tooltip />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['dms:sparepart:query']">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:sparepart:edit']">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="sparepartRef" :model="form" :rules="rules" label-width="100px" :disabled="formDisabled">
        <el-divider content-position="center">基本信息</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="备件编号" prop="partCode">
              <el-input v-model="form.partCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="备件名称" prop="partName"><el-input v-model="form.partName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="备件类别" prop="partType">
              <el-select v-model="form.partType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in dms_part_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="规格型号" prop="specModel"><el-input v-model="form.specModel" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in wms_unit" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplier">
              <el-select v-model="form.supplier" filterable clearable placeholder="请选择供应商" style="width: 100%">
                <el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierName" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="center">库存参数</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="库存下限" prop="stockMin"><el-input-number v-model="form.stockMin" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="库存上限" prop="stockMax"><el-input-number v-model="form.stockMax" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="center">其他信息</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm" v-if="!formDisabled">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsSparePart">
import { listSparepart, getSparepart, addSparepart, updateSparepart, delSparepart } from '@/api/dms/sparepart'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_sparepart_index')
const { wms_unit, dms_part_type } = proxy.useDict('wms_unit', 'dms_part_type')

const list = ref([])
const supplierOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const formDisabled = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, partCode: undefined, partName: undefined, partType: undefined },
  rules: {
    partCode: [{ required: false }],
    partName: [{ required: true, message: '备件名称不能为空', trigger: 'blur' }],
    partType: [{ required: true, message: '备件类别不能为空', trigger: 'change' }],
    unit: [{ required: true, message: '单位不能为空', trigger: 'change' }],
    stockMin: [{ validator: validateStockRange, trigger: 'change' }],
    stockMax: [{ validator: validateStockRange, trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 校验库存下限不能大于等于库存上限 */
function validateStockRange(rule, value, callback) {
  const min = form.value.stockMin
  const max = form.value.stockMax
  if (min != null && max != null && min > 0 && max > 0 && min >= max) {
    callback(new Error('库存下限不能大于等于库存上限'))
  } else {
    callback()
  }
}

function getList() {
  loading.value = true
  listSparepart(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
/** 获取供应商列表 */
function getSupplierList() {
  listSupplier({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { supplierOptions.value = res.rows })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.partId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = {
    partCode: undefined, partName: undefined, partType: undefined, specModel: undefined, unit: undefined,
    stockMin: 0, stockMax: 0, currentStock: 0,
    supplier: undefined, status: '0', remark: undefined
  }
  proxy.resetForm('sparepartRef')
}
function handleAdd() {
  reset()
  formDisabled.value = false
  open.value = true
  title.value = '添加备件'
}
function handleUpdate(row) {
  reset()
  formDisabled.value = false
  getSparepart(row.partId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改备件' })
}
function handleView(row) {
  reset()
  formDisabled.value = true
  getSparepart(row.partId).then(res => { form.value = res.data; open.value = true; title.value = '查看备件' })
}
function submitForm() {
  proxy.$refs['sparepartRef'].validate(valid => {
    if (valid) {
      if (form.value.partId != undefined) { updateSparepart(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addSparepart(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const partIds = row.partId || ids.value; proxy.$modal.confirm('确认删除编号为"' + partIds + '"的数据？').then(() => delSparepart(partIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('dms/sparepart/export', { ...queryParams.value }, `sparepart_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
getSupplierList()
</script>
