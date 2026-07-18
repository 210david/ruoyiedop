<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="问题" prop="question">
        <el-input v-model="queryParams.question" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="关键词" prop="keywords">
        <el-input v-model="queryParams.keywords" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:ai:faq:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:ai:faq:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:ai:faq:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="问题" prop="question" show-overflow-tooltip />
      <el-table-column label="分类" prop="categoryName" :width="colWidth('categoryName', 120)" resizable />
      <el-table-column label="关键词" prop="keywords" :width="colWidth('keywords', 160)" resizable show-overflow-tooltip />
      <el-table-column label="查看次数" prop="viewCount" :width="colWidth('viewCount', 80)" resizable align="center" />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:ai:faq:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:ai:faq:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="faqRef" :model="form" :rules="rules" label-width="100px">
        <el-divider content-position="center">FAQ信息</el-divider>
        <el-form-item label="问题" prop="question"><el-input v-model="form.question" placeholder="请输入问题" /></el-form-item>
        <el-form-item label="标准答案" prop="answer"><el-input v-model="form.answer" type="textarea" :rows="5" placeholder="请输入标准答案" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="分类" prop="categoryName"><el-input v-model="form.categoryName" placeholder="如：数控车床" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="关键词" prop="keywords"><el-input v-model="form.keywords" placeholder="多个关键词用逗号分隔，如：主轴,异响,温度" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsRepairFaq">
import { listFaq, getFaq, addFaq, updateFaq, delFaq } from '@/api/dms/ai'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('dms_ai_faq_index')

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
  queryParams: { pageNum: 1, pageSize: 10, question: undefined, keywords: undefined },
  rules: {
    question: [{ required: true, message: '问题不能为空', trigger: 'blur' }],
    answer: [{ required: true, message: '标准答案不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listFaq(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.faqId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { question: undefined, answer: undefined, categoryName: undefined, keywords: undefined, status: '0', remark: undefined }; proxy.resetForm('faqRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增FAQ' }
function handleUpdate(row) { reset(); getFaq(row.faqId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改FAQ' }) }
function submitForm() {
  proxy.$refs['faqRef'].validate(valid => {
    if (valid) {
      if (form.value.faqId != undefined) { updateFaq(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addFaq(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const faqIds = row.faqId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delFaq(faqIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getList()
</script>
