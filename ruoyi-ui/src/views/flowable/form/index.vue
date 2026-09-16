<template>
  <div class="app-container flowable-form-page">
    <!-- 筛选 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>表单名称</label>
          <div class="control">
            <el-input v-model="queryParams.formName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 支持回车快速搜索</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 列表 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:form:add']">新建表单</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="表单名称" prop="formName" min-width="150">
            <template #default="scope"><span class="col-strong">{{ scope.row.formName }}</span></template>
          </el-table-column>
          <el-table-column label="表单Key" prop="formKey" min-width="130">
            <template #default="scope"><span class="col-mono">{{ scope.row.formKey }}</span></template>
          </el-table-column>
          <el-table-column label="绑定流程" prop="processKey" min-width="130">
            <template #default="scope"><span class="col-mono">{{ scope.row.processKey || '未绑定' }}</span></template>
          </el-table-column>
          <el-table-column label="字段数" width="80" align="center">
            <template #default="scope">{{ fieldCount(scope.row.fieldsJson) }}</template>
          </el-table-column>
          <el-table-column label="数据表" prop="tableName" min-width="140">
            <template #default="scope">
              <span v-if="scope.row.tableName" class="badge green"><span class="dot"></span>{{ scope.row.tableName }}</span>
              <span v-else class="badge gray"><span class="dot"></span>未生成</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" align="center" />
          <el-table-column label="操作" width="280" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['flowable:form:edit']">设计</el-button>
                <el-button link type="warning" icon="Coin" @click="handleCreateTable(scope.row)" v-hasPermi="['flowable:form:table']">{{ scope.row.tableName ? '重建数据表' : '生成数据表' }}</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:form:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- 设计弹窗 -->
    <el-dialog v-model="open" width="1000px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          </div>
          <span class="rd-detail-header-title">表单设计：{{ form.formName || '新建表单' }}</span>
        </div>
      </template>
      <el-form :model="form" label-width="100px" size="small">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="表单名称" required><el-input v-model="form.formName" placeholder="如：资产入库申请" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="表单Key" required><el-input v-model="form.formKey" placeholder="小写字母开头，如 asset_in" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="绑定流程Key"><el-input v-model="form.processKey" placeholder="如 leave_process（发起时自动使用此表单）" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <el-divider content-position="left">字段设计（参照泛微：显示名 / 数据库字段 / 表现形式 / 顺序）</el-divider>
      <div style="margin-bottom:8px">
        <el-button size="small" type="primary" plain icon="Plus" @click="addField('text')">单行文本</el-button>
        <el-button size="small" plain icon="Plus" @click="addField('textarea')">多行文本</el-button>
        <el-button size="small" plain icon="Plus" @click="addField('number')">数字</el-button>
        <el-button size="small" plain icon="Plus" @click="addField('date')">日期</el-button>
        <el-button size="small" plain icon="Plus" @click="addField('select')">下拉单选</el-button>
        <span style="font-size:12px;color:#94a3b8;margin-left:12px">拖动行可调整显示顺序</span>
      </div>
      <el-table :data="form.fields" border size="small" row-key="rowKey">
        <el-table-column label="排序" width="60" align="center"><template #default><el-icon style="cursor:move;color:#94a3b8"><Rank /></el-icon></template></el-table-column>
        <el-table-column label="字段显示名" min-width="130"><template #default="scope"><el-input v-model="scope.row.label" placeholder="如：资产名称" size="small" /></template></el-table-column>
        <el-table-column label="数据库字段名" min-width="130"><template #default="scope"><el-input v-model="scope.row.field" placeholder="小写英文，如 zcmc" size="small" /></template></el-table-column>
        <el-table-column label="表现形式" width="130" align="center"><template #default="scope">{{ typeLabel(scope.row.type) }}</template></el-table-column>
        <el-table-column label="选项(下拉用)" min-width="150"><template #default="scope"><el-input v-if="scope.row.type==='select'" v-model="scope.row.options" placeholder="逗号分隔，如：事假,病假" size="small" /><span v-else style="color:#cbd5e1">—</span></template></el-table-column>
        <el-table-column label="必填" width="70" align="center"><template #default="scope"><el-switch v-model="scope.row.required" size="small" /></template></el-table-column>
        <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="delField(scope.$index)" /></template></el-table-column>
      </el-table>

      <el-divider content-position="left">表单预览</el-divider>
      <div class="form-preview">
        <el-form label-width="110px" size="small" disabled>
          <el-form-item v-for="f in form.fields" :key="f.field" :label="f.label || f.field" :required="f.required">
            <el-input v-if="f.type==='text'" placeholder="单行文本" />
            <el-input v-else-if="f.type==='textarea'" type="textarea" :rows="2" placeholder="多行文本" />
            <el-input-number v-else-if="f.type==='number'" :controls="false" style="width:100%" placeholder="数字" />
            <el-date-picker v-else-if="f.type==='date'" type="datetime" style="width:100%" placeholder="日期时间" />
            <el-select v-else-if="f.type==='select'" placeholder="下拉选择" style="width:100%">
              <el-option v-for="o in (f.options||'').split(',')" :key="o" :label="o" :value="o" />
            </el-select>
          </el-form-item>
          <div v-if="!form.fields || !form.fields.length" style="color:#94a3b8;font-size:13px;padding:12px">尚未添加字段，请点击上方按钮添加</div>
        </el-form>
      </div>

      <template #footer>
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowForm">
import { listForm, getForm, addForm, updateForm, delForm, createTable } from '@/api/flowable/form'
import { Search, Filter, Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'

const { proxy } = getCurrentInstance()
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, formName: undefined }
})
const { queryParams, form } = toRefs(data)

function fieldCount(json) {
  try { return JSON.parse(json || '[]').length } catch (e) { return 0 }
}
function typeLabel(t) { return { text: '单行文本框', textarea: '多行文本框', number: '数字（整数/浮点）', date: '日期时间', select: '下拉单选' }[t] || t }

function getList() {
  loading.value = true
  listForm(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.formName = undefined; handleQuery() }

let rowSeq = 1
function handleAdd() {
  form.value = { formId: undefined, formKey: undefined, formName: undefined, processKey: undefined, fields: [] }
  open.value = true
}
async function handleDesign(row) {
  const res = await getForm(row.formId)
  const d = res.data
  let fields = []
  try { fields = JSON.parse(d.fieldsJson || '[]') } catch (e) { fields = [] }
  fields.forEach(f => { if (!f.rowKey) f.rowKey = 'r' + (rowSeq++) })
  form.value = { ...d, fields }
  open.value = true
}
function addField(type) {
  form.value.fields.push({ rowKey: 'r' + (rowSeq++), field: '', label: '', type, required: false })
}
function delField(idx) { form.value.fields.splice(idx, 1) }

function submitForm() {
  if (!form.value.formName || !form.value.formKey) { proxy.$modal.msgWarning('请填写表单名称与表单Key'); return }
  if (!/^[a-z][a-z0-9_]{1,40}$/.test(form.value.formKey)) { proxy.$modal.msgWarning('表单Key仅允许小写字母开头的字母/数字/下划线'); return }
  for (const f of form.value.fields) {
    if (!f.label || !f.field) { proxy.$modal.msgWarning('存在未填写完整（显示名/字段名）的字段'); return }
    if (!/^[a-z][a-z0-9_]{0,39}$/.test(f.field)) { proxy.$modal.msgWarning('字段名[' + f.field + ']不合法：小写字母开头的字母/数字/下划线'); return }
  }
  const payload = {
    formId: form.value.formId, formKey: form.value.formKey, formName: form.value.formName,
    processKey: form.value.processKey || undefined,
    fieldsJson: JSON.stringify(form.value.fields)
  }
  if (form.value.formId) {
    updateForm(payload).then(() => { proxy.$modal.msgSuccess('保存成功'); open.value = false; getList() })
  } else {
    addForm(payload).then(() => { proxy.$modal.msgSuccess('新建成功'); open.value = false; getList() })
  }
}

function handleCreateTable(row) {
  proxy.$modal.confirm('将按当前字段定义生成数据表 flow_data_' + row.formKey + '（已存在则跳过创建）。确认执行？')
    .then(() => createTable(row.formId))
    .then(res => { proxy.$modal.msgSuccess('数据表已就绪：' + res.data); getList() })
    .catch(() => {})
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除表单"' + row.formName + '"？其数据表不会被删除').then(() => delForm(row.formId)).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList()
  }).catch(() => {})
}

getList()
onActivated(() => { getList() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.form-preview { background:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:16px; max-height:320px; overflow:auto; }
</style>
