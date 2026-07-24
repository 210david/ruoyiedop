<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="queryParams.categoryName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:category:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-if="refreshTable" v-loading="loading" :data="categoryList" row-key="categoryId" :default-expand-all="isExpandAll" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" @header-dragend="onHeaderDragEnd">
      <el-table-column label="分类名称" prop="categoryName" :width="colWidth('categoryName', 260)" resizable />
      <el-table-column label="分类编码" prop="categoryCode" :width="colWidth('categoryCode', 160)" resizable align="center" />
      <el-table-column label="层级" prop="categoryLevel" :width="colWidth('categoryLevel', 80)" resizable align="center" />
      <el-table-column label="排序" prop="orderNum" :width="colWidth('orderNum', 80)" resizable align="center" />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 180)" resizable align="center" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['dms:category:add']">新增</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:category:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:category:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="open" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24" v-if="form.parentId !== 0">
            <el-form-item label="上级分类" prop="parentId">
              <el-tree-select v-model="form.parentId" :data="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" value-key="categoryId" placeholder="选择上级分类" check-strictly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类编码" prop="categoryCode"><el-input v-model="form.categoryCode" placeholder="请输入" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类名称" prop="categoryName"><el-input v-model="form.categoryName" placeholder="请输入" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum"><el-input-number v-model="form.orderNum" :min="0" controls-position="right" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsCategory">
import { listCategory, getCategory, addCategory, updateCategory, delCategory } from '@/api/dms/category'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_category_index')

const categoryList = ref([])
const categoryOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const isExpandAll = ref(true)
const refreshTable = ref(true)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: { categoryName: undefined },
  rules: {
    categoryCode: [{ required: true, message: '分类编码不能为空', trigger: 'blur' }],
    categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCategory(queryParams.value).then(res => {
    categoryList.value = proxy.handleTree(res.data, 'categoryId')
    loading.value = false
  })
}
function handleQuery() { getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}
function reset() {
  form.value = { parentId: 0, categoryCode: undefined, categoryName: undefined, categoryLevel: 1, orderNum: 0, status: '0', remark: undefined }; proxy.resetForm('categoryRef') }
function handleAdd(row) {
  reset()
  listCategory().then(res => { categoryOptions.value = [{ categoryId: 0, categoryName: '顶级分类', children: proxy.handleTree(res.data, 'categoryId') }] })
  if (row != null && row.categoryId) { form.value.parentId = row.categoryId; form.value.categoryLevel = (row.categoryLevel || 1) + 1 } else { form.value.parentId = 0; form.value.categoryLevel = 1 }
  open.value = true; title.value = '添加设备分类'
}
function handleUpdate(row) {
  reset()
  listCategory().then(res => { categoryOptions.value = [{ categoryId: 0, categoryName: '顶级分类', children: proxy.handleTree(res.data, 'categoryId') }] })
  getCategory(row.categoryId).then(res => { form.value = res.data; open.value = true; title.value = '修改设备分类' })
}
function submitForm() {
  proxy.$refs['categoryRef'].validate(valid => {
    if (valid) {
      if (form.value.categoryId != undefined) { updateCategory(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addCategory(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { proxy.$modal.confirm('确认删除"' + row.categoryName + '"？').then(() => delCategory(row.categoryId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getList()
</script>
