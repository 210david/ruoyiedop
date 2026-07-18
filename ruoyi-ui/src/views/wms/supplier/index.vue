<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="供应商编码" prop="supplierCode">
        <el-input v-model="queryParams.supplierCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:supplier:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['wms:supplier:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:supplier:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:supplier:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="供应商编码" prop="supplierCode" :width="colWidth('supplierCode', 130)" resizable />
      <el-table-column label="供应商名称" prop="supplierName" show-overflow-tooltip />
      <el-table-column label="联系人" prop="contactPerson" :width="colWidth('contactPerson', 100)" resizable />
      <el-table-column label="联系电话" prop="contactPhone" :width="colWidth('contactPhone', 130)" resizable />
      <el-table-column label="地址" prop="address" show-overflow-tooltip />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:supplier:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:supplier:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="supplierRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="供应商编码" prop="supplierCode"><el-input v-model="form.supplierCode" placeholder="请输入" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入" /></el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson"><el-input v-model="form.contactPerson" placeholder="请输入" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" prop="address"><el-input v-model="form.address" type="textarea" placeholder="请输入" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsSupplier">
import { listSupplier, getSupplier, addSupplier, updateSupplier, delSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('wms_supplier_index')

const list = ref([])
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
  queryParams: { pageNum: 1, pageSize: 10, supplierCode: undefined, supplierName: undefined },
  rules: {
    supplierCode: [{ required: true, message: '供应商编码不能为空', trigger: 'blur' }],
    supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listSupplier(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.supplierId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { supplierCode: undefined, supplierName: undefined, contactPerson: undefined, contactPhone: undefined, address: undefined, status: '0', remark: undefined }; proxy.resetForm('supplierRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加供应商' }
function handleUpdate(row) { reset(); getSupplier(row.supplierId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改供应商' }) }
function submitForm() {
  proxy.$refs['supplierRef'].validate(valid => {
    if (valid) {
      if (form.value.supplierId != undefined) { updateSupplier(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addSupplier(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const supplierIds = row.supplierId || ids.value; proxy.$modal.confirm('确认删除编号为"' + supplierIds + '"的数据？').then(() => delSupplier(supplierIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('wms/supplier/export', { ...queryParams.value }, `supplier_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
getList()
</script>
