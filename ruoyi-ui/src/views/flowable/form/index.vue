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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:form:add']">新增</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" border class="app-table">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="表单名称" prop="formName" key="formName" :min-width="colWidth('formName', 180)" align="center" show-overflow-tooltip resizable>
            <template #default="scope"><span class="col-strong">{{ scope.row.formName }}</span></template>
          </el-table-column>
          <el-table-column label="数据库表名" prop="tableName" key="tableName" :min-width="colWidth('tableName', 180)" align="center" show-overflow-tooltip resizable>
            <template #default="scope">
              <span v-if="scope.row.tableName" class="badge green"><span class="dot"></span>{{ scope.row.tableName }}</span>
              <span v-else class="badge gray"><span class="dot"></span>未生成</span>
            </template>
          </el-table-column>
          <el-table-column label="表单描述" prop="formDesc" key="formDesc" :min-width="colWidth('formDesc', 200)" align="center" show-overflow-tooltip resizable>
            <template #default="scope">{{ scope.row.formDesc || '—' }}</template>
          </el-table-column>
          <el-table-column label="字段数" prop="fieldCount" key="fieldCount" width="90" align="center">
            <template #default="scope">{{ fieldCount(scope.row.fieldsJson) }}</template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" width="170" align="center" />
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

    <!-- ===== 新建自定义表单弹窗（参照泛微e9：名称/复制/描述） ===== -->
    <el-dialog v-model="addOpen" width="520px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          </div>
          <span class="rd-detail-header-title">新建自定义表单</span>
        </div>
      </template>
      <el-form ref="addRef" :model="addFormData" :rules="addRules" label-width="120px" size="small">
        <el-form-item label="表单名称" prop="formName"><el-input v-model="addFormData.formName" placeholder="如：销售合同" maxlength="50" /></el-form-item>
        <el-form-item label="从已有表单复制">
          <div class="copy-field">
            <el-input :model-value="copySourceName" readonly placeholder="未选择（字段将为空，可稍后在编辑字段中设计）">
              <template #suffix>
                <el-icon class="copy-clear" v-if="addFormData.copyFrom" @click="clearCopySource"><CircleClose /></el-icon>
              </template>
            </el-input>
            <el-button icon="Search" @click="openCopyPicker">浏览</el-button>
          </div>
          <div class="form-tip">选择后将复制该表单的全部字段设计；数据库表名保存后自动生成为 flow_data_+唯一数字，无需填写</div>
        </el-form-item>
        <el-form-item label="表单描述"><el-input v-model="addFormData.formDesc" type="textarea" :rows="3" maxlength="200" placeholder="表单用途说明" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd(true)">保存并进入详细设置</el-button>
        <el-button @click="submitAdd(false)">保 存</el-button>
        <el-button @click="addOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 从已有表单复制：弹框选择器（按弹框选择器开发规范） ===== -->
    <el-dialog v-model="copyPickerOpen" width="640px" append-to-body draggable class="rd-dialog" title="从已有表单复制" :close-on-click-modal="false">
      <div class="copy-picker-bar">
        <el-input v-model="copyKeyword" placeholder="搜索表单名称" clearable @keyup.enter="loadCopyList">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button icon="Search" @click="loadCopyList">搜索</el-button>
      </div>
      <el-table :data="copyList" border size="small" max-height="360" highlight-current-row
        @current-change="onCopyRowSelect" @row-dblclick="confirmCopyPicker" class="copy-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="表单名称" prop="formName" min-width="160" align="center" show-overflow-tooltip>
          <template #default="scope"><span class="col-strong">{{ scope.row.formName }}</span></template>
        </el-table-column>
        <el-table-column label="表单描述" prop="formDesc" min-width="180" align="center" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.formDesc || '—' }}</template>
        </el-table-column>
        <el-table-column label="字段数" width="80" align="center">
          <template #default="scope">{{ fieldCount(scope.row.fieldsJson) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
      </el-table>
      <template #footer>
        <el-button type="primary" :disabled="!copySelected" @click="confirmCopyPicker">确 定</el-button>
        <el-button @click="copyPickerOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 表单设计弹窗（基本信息 / 编辑字段，参照泛微e9） ===== -->
    <el-dialog v-model="open" width="1100px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          </div>
          <span class="rd-detail-header-title">编辑表单：{{ form.formName || '新建表单' }}</span>
        </div>
      </template>
      <el-tabs v-model="activeTab" class="form-design-tabs">
        <!-- ===== 基本信息 ===== -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="form" label-width="110px" size="small" class="basic-form">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="表单名称" required><el-input v-model="form.formName" placeholder="如：销售合同" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="表单Key"><el-input :model-value="form.formKey" disabled /><div class="form-tip">系统自动生成的唯一数字标识，用于数据表命名</div></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="数据库表名">
                <div class="tbl-name-row">
                  <span v-if="form.tableName" class="badge green"><span class="dot"></span>{{ form.tableName }}</span>
                  <span v-else class="badge gray"><span class="dot"></span>{{ 'flow_data_' + (form.formKey || '{表单Key}') }}</span>
                </div>
                <div class="form-tip" v-if="!form.tableName">尚未生成数据表，点击列表"生成数据表"后按此名称创建（明细表自动追加 _dt1/_dt2…）</div>
              </el-form-item></el-col>
            </el-row>
            <el-form-item label="表单描述"><el-input v-model="form.formDesc" type="textarea" :rows="2" maxlength="200" placeholder="表单用途说明" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ===== 编辑字段 ===== -->
        <el-tab-pane label="编辑字段" name="fields">
          <div class="field-tab-bar">
            <el-tabs v-model="activeTableTab" type="card" class="table-tabs"
              @tab-remove="removeDetailTable">
              <el-tab-pane name="main" :closable="false">
                <template #label><span>主表 <span class="tab-tbl">( {{ mainTableName }} )</span></span></template>
              </el-tab-pane>
              <el-tab-pane v-for="dt in detailTables" :key="dt" :name="dt" :closable="true">
                <template #label><span>明细表{{ dt.slice(2) }} <span class="tab-tbl">( {{ mainTableName + '_' + dt }} )</span></span></template>
              </el-tab-pane>
              <el-tab-pane name="__add" :closable="false">
                <template #label><span class="tab-add">＋</span></template>
              </el-tab-pane>
            </el-tabs>
          </div>

          <div class="field-toolbar">
            <el-button size="small" type="primary" plain icon="Plus" @click="openAddField">添加字段</el-button>
            <el-button size="small" type="primary" plain icon="DocumentAdd" @click="openBatchAdd">批量添加</el-button>
            <el-button size="small" plain icon="Delete" :disabled="!checkedFields.length" @click="removeCheckedFields">删除选中</el-button>
            <span class="field-tip">当前表：{{ activeTableTab === 'main' ? mainTableName : mainTableName + '_' + activeTableTab }}<template v-if="activeTableTab !== 'main'">（明细表通过 mainid 字段关联主表主键，生成数据表时自动创建）</template></span>
          </div>

          <el-table :data="activeFields" border size="small" row-key="rowKey" @selection-change="onFieldSelChange" class="field-table" max-height="380">
            <el-table-column type="selection" width="45" align="center" />
            <el-table-column label="字段显示名" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.label" placeholder="如：合同名称" size="small" /></template></el-table-column>
            <el-table-column label="数据库字段名称" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.field" placeholder="小写英文，如 htmc" size="small" /></template></el-table-column>
            <el-table-column label="表现形式" width="130" align="center">
              <template #default="scope">
                <el-select v-model="scope.row.form" size="small" @change="onFormChange(scope.row)">
                  <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="110" align="center">
              <template #default="scope">
                <el-select v-model="scope.row.dtype" size="small" :disabled="scope.row.form === 'textarea'">
                  <el-option label="文本" value="text" /><el-option label="日期" value="date" />
                  <el-option label="浮点数" value="float" /><el-option label="整数" value="int" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="文本长度" width="100" align="center">
              <template #default="scope"><el-input-number v-if="scope.row.dtype === 'text'" v-model="scope.row.length" :min="1" :max="2000" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">—</span></template>
            </el-table-column>
            <el-table-column label="小数位数" width="95" align="center">
              <template #default="scope"><el-input-number v-if="scope.row.dtype === 'float'" v-model="scope.row.scale" :min="1" :max="6" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">—</span></template>
            </el-table-column>
            <el-table-column label="选项(选择框)" min-width="130" align="center">
              <template #default="scope"><el-input v-if="scope.row.form === 'select'" v-model="scope.row.options" placeholder="逗号分隔" size="small" /><span v-else style="color:#cbd5e1">—</span></template>
            </el-table-column>
            <el-table-column label="必填" width="70" align="center"><template #default="scope"><el-switch v-model="scope.row.required" size="small" /></template></el-table-column>
            <el-table-column label="显示顺序" width="100" align="center"><template #default="scope"><el-input-number v-model="scope.row.order" :min="1" :max="999" :controls="false" size="small" style="width:100%" /></template></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 添加字段弹窗（参照泛微e9） ===== -->
    <el-dialog v-model="addFieldOpen" width="520px" append-to-body draggable class="rd-dialog" title="添加字段" :close-on-click-modal="false">
      <el-form ref="addFieldRef" :model="newField" :rules="fieldRules" label-width="120px" size="small">
        <el-form-item label="字段显示名" prop="label"><el-input v-model="newField.label" placeholder="如：合同名称" /></el-form-item>
        <el-form-item label="数据库字段名称" prop="field">
          <el-input v-model="newField.field" placeholder="小写英文，如 htmc" />
          <div class="form-tip">仅允许小写字母开头的字母/数字/下划线</div>
        </el-form-item>
        <el-form-item label="字段位置">
          <el-select v-model="newField.table" style="width:100%">
            <el-option label="主表" value="main" />
            <el-option v-for="dt in detailTables" :key="dt" :label="'明细表' + dt.slice(2) + '（' + mainTableName + '_' + dt + '）'" :value="dt" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示顺序"><el-input-number v-model="newField.order" :min="1" :max="999" :controls="false" style="width:160px" /></el-form-item>
        <el-form-item label="表现形式">
          <el-select v-model="newField.form" style="width:100%" @change="newField.dtype = newField.form === 'textarea' ? 'text' : newField.dtype">
            <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="newField.dtype" style="width:100%" :disabled="newField.form === 'textarea'">
            <el-option label="文本" value="text" /><el-option label="日期" value="date" />
            <el-option label="浮点数" value="float" /><el-option label="整数" value="int" />
          </el-select>
        </el-form-item>
        <el-form-item label="文本长度" v-if="newField.dtype === 'text'"><el-input-number v-model="newField.length" :min="1" :max="2000" :controls="false" style="width:160px" /></el-form-item>
        <el-form-item label="小数位数" v-if="newField.dtype === 'float'"><el-input-number v-model="newField.scale" :min="1" :max="6" :controls="false" style="width:160px" /></el-form-item>
        <el-form-item label="选项(选择框)" v-if="newField.form === 'select'"><el-input v-model="newField.options" placeholder="逗号分隔，如：解除,终止,暂停" /></el-form-item>
        <el-form-item label="必填"><el-switch v-model="newField.required" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAddField">确 定</el-button>
        <el-button @click="addFieldOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 批量添加字段弹窗（参照泛微e9：多行直接填写） ===== -->
    <el-dialog v-model="batchOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          </div>
          <span class="rd-detail-header-title">批量添加字段：{{ activeTableTab === 'main' ? '主表（' + mainTableName + '）' : '明细表' + activeTableTab.slice(2) + '（' + mainTableName + '_' + activeTableTab + '）' }}</span>
        </div>
      </template>
      <div class="batch-toolbar">
        <el-button size="small" type="primary" plain icon="Plus" @click="addBatchRow">再加一行</el-button>
        <span class="field-tip">直接在表格中逐行填写，确定后一次性追加到当前表；字段显示名与数据库字段名称为必填</span>
      </div>
      <el-table :data="batchRows" border size="small" row-key="batchKey" max-height="360" class="field-table">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="字段显示名" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.label" placeholder="如：合同名称" size="small" /></template></el-table-column>
        <el-table-column label="数据库字段名称" min-width="130" align="center"><template #default="scope"><el-input v-model="scope.row.field" placeholder="小写英文，如 htmc" size="small" /></template></el-table-column>
        <el-table-column label="表现形式" width="125" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.form" size="small" @change="scope.row.dtype = scope.row.form === 'textarea' ? 'text' : scope.row.dtype">
              <el-option v-for="(v, k) in FORM_OPTIONS" :key="k" :label="v" :value="k" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="105" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.dtype" size="small" :disabled="scope.row.form === 'textarea'">
              <el-option label="文本" value="text" /><el-option label="日期" value="date" />
              <el-option label="浮点数" value="float" /><el-option label="整数" value="int" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="文本长度" width="90" align="center">
          <template #default="scope"><el-input-number v-if="scope.row.dtype === 'text'" v-model="scope.row.length" :min="1" :max="2000" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">—</span></template>
        </el-table-column>
        <el-table-column label="小数位数" width="90" align="center">
          <template #default="scope"><el-input-number v-if="scope.row.dtype === 'float'" v-model="scope.row.scale" :min="1" :max="6" :controls="false" size="small" style="width:100%" /><span v-else style="color:#cbd5e1">—</span></template>
        </el-table-column>
        <el-table-column label="选项(选择框)" min-width="120" align="center">
          <template #default="scope"><el-input v-if="scope.row.form === 'select'" v-model="scope.row.options" placeholder="逗号分隔" size="small" /><span v-else style="color:#cbd5e1">—</span></template>
        </el-table-column>
        <el-table-column label="必填" width="65" align="center"><template #default="scope"><el-switch v-model="scope.row.required" size="small" /></template></el-table-column>
        <el-table-column label="操作" width="60" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeBatchRow(scope.$index)" /></template></el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="confirmBatchAdd">确 定</el-button>
        <el-button @click="batchOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowForm">
import { listForm, getForm, addForm, updateForm, delForm, createTable } from '@/api/flowable/form'
import { Search, Filter, CircleClose } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_form_index')
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const activeTab = ref('basic')
const activeTableTab = ref('main')
const addFieldOpen = ref(false)
const addFieldRef = ref(null)
const checkedFields = ref([])

// ===== 新建自定义表单弹窗状态 =====
const addOpen = ref(false)
const addRef = ref(null)
const addFormData = ref({})
const addRules = {
  formName: [{ required: true, message: '表单名称不能为空', trigger: 'blur' }]
}

// ===== 从已有表单复制：弹框选择器状态 =====
const copyPickerOpen = ref(false)
const copyKeyword = ref('')
const copyList = ref([])
const copySelected = ref(null)
const copySourceName = computed(() => copySelected.value ? copySelected.value.formName : '')

function openCopyPicker() {
  copyKeyword.value = ''
  copySelected.value = null
  loadCopyList()
  copyPickerOpen.value = true
}

function loadCopyList() {
  listForm({ pageNum: 1, pageSize: 1000, formName: copyKeyword.value || undefined }).then(res => {
    copyList.value = (res.rows || []).filter(f => f.formId !== form.value.formId)
  })
}

function onCopyRowSelect(row) { copySelected.value = row }

/** 确认选择：立即拉取源表单字段暂存，保存时直接使用 */
async function confirmCopyPicker() {
  if (!copySelected.value) return
  const src = await getForm(copySelected.value.formId)
  addFormData.value.copyFrom = copySelected.value.formId
  addFormData.value.copyFieldsJson = src.data.fieldsJson || '[]'
  copyPickerOpen.value = false
}

function clearCopySource() {
  addFormData.value.copyFrom = undefined
  addFormData.value.copyFieldsJson = undefined
  copySelected.value = null
}

// 表现形式（参照泛微e9）
const FORM_OPTIONS = { text: '单行文本框', textarea: '多行文本框', browse: '浏览按钮', check: 'Check框', select: '选择框', attach: '附件上传' }

const data = reactive({
  form: {},
  fields: [],
  detailTables: [],
  newField: {},
  queryParams: { pageNum: 1, pageSize: 10, formName: undefined }
})
const { queryParams, form, fields, detailTables, newField } = toRefs(data)

const fieldRules = {
  label: [{ required: true, message: '字段显示名不能为空', trigger: 'blur' }],
  field: [
    { required: true, message: '数据库字段名称不能为空', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]{0,39}$/, message: '仅允许小写字母开头的字母/数字/下划线', trigger: 'blur' }
  ]
}

const mainTableName = computed(() => form.value.formKey ? 'flow_data_' + form.value.formKey : 'flow_data_{表单Key}')

const activeFields = computed(() => fields.value.filter(f => (f.table || 'main') === activeTableTab.value))

function fieldCount(json) {
  try { return normalizeFields(JSON.parse(json || '[]')).length } catch (e) { return 0 }
}

/** 兼容旧结构 {type:text/textarea/number/date/select} → {form,dtype,length,scale,table} */
function normalizeFields(arr) {
  return (arr || []).map(f => {
    const n = { ...f, rowKey: f.rowKey || 'r' + Math.random().toString(36).slice(2, 8) }
    if (!n.form) {
      if (n.type === 'textarea') n.form = 'textarea'
      else if (n.type === 'select') n.form = 'select'
      else n.form = 'text'
      if (n.type === 'number') n.dtype = 'float'
      else if (n.type === 'date') n.dtype = 'date'
      else n.dtype = n.dtype || 'text'
    }
    n.dtype = n.dtype || 'text'
    n.length = n.length || 500
    n.scale = n.scale || 2
    n.table = n.table || 'main'
    n.order = n.order || 1
    return n
  })
}

function getList() {
  loading.value = true
  listForm(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.formName = undefined; handleQuery() }

function handleAdd() {
  addFormData.value = { formName: undefined, copyFrom: undefined, copyFieldsJson: undefined, formDesc: undefined }
  copySelected.value = null
  addOpen.value = true
  nextTick(() => addRef.value && addRef.value.clearValidate())
}

/** 新建：保存(enterDesign=false 仅保存) / 保存并进入详细设置(enterDesign=true) */
function submitAdd(enterDesign) {
  addRef.value.validate(async valid => {
    if (!valid) return
    const fieldsJson = addFormData.value.copyFieldsJson || '[]'
    const payload = { formName: addFormData.value.formName, formDesc: addFormData.value.formDesc || undefined, fieldsJson }
    const res = await addForm(payload)
    const newId = res.data
    const copied = fieldsJson !== '[]'
    addOpen.value = false
    proxy.$modal.msgSuccess(copied ? '新建成功，已复制所选表单的全部字段' : '新建成功，表单Key已自动生成')
    getList()
    if (enterDesign) {
      const detail = await getForm(newId)
      form.value = detail.data
      fields.value = normalizeFields(safeParse(detail.data.fieldsJson))
      rebuildDetailTables()
      activeTab.value = 'fields'
      activeTableTab.value = 'main'
      open.value = true
    }
  })
}

async function handleDesign(row) {
  const res = await getForm(row.formId)
  form.value = res.data
  fields.value = normalizeFields(safeParse(res.data.fieldsJson))
  rebuildDetailTables()
  activeTab.value = 'basic'
  activeTableTab.value = 'main'
  open.value = true
}

function safeParse(json) {
  try { return JSON.parse(json || '[]') } catch (e) { return [] }
}

function rebuildDetailTables() {
  const set = new Set(fields.value.map(f => f.table).filter(t => t && t !== 'main'))
  detailTables.value = [...set].sort()
}

function onFormChange(row) {
  if (row.form === 'textarea') row.dtype = 'text'
}

function openAddField() {
  newField.value = { label: undefined, field: undefined, table: activeTableTab.value === '__add' ? 'main' : activeTableTab.value, order: fields.value.length + 1, form: 'text', dtype: 'text', length: 500, scale: 2, options: undefined, required: false }
  addFieldOpen.value = true
  nextTick(() => addFieldRef.value && addFieldRef.value.clearValidate())
}

function confirmAddField() {
  addFieldRef.value.validate(valid => {
    if (!valid) return
    const dup = fields.value.some(f => f.field === newField.value.field)
    if (dup) { proxy.$modal.msgWarning('数据库字段名称已存在：' + newField.value.field); return }
    fields.value.push({ rowKey: 'r' + Math.random().toString(36).slice(2, 8), ...newField.value })
    if (newField.value.table !== 'main' && !detailTables.value.includes(newField.value.table)) {
      detailTables.value.push(newField.value.table)
      detailTables.value.sort()
    }
    activeTableTab.value = newField.value.table
    addFieldOpen.value = false
  })
}

function onFieldSelChange(rows) { checkedFields.value = rows }

// ===== 批量添加字段 =====
const batchOpen = ref(false)
const batchRows = ref([])
function blankBatchRow() {
  return { batchKey: 'b' + Math.random().toString(36).slice(2, 8), label: undefined, field: undefined, form: 'text', dtype: 'text', length: 500, scale: 2, options: undefined, required: false }
}
function openBatchAdd() {
  batchRows.value = [blankBatchRow(), blankBatchRow(), blankBatchRow(), blankBatchRow(), blankBatchRow()]
  batchOpen.value = true
}
function addBatchRow() { batchRows.value.push(blankBatchRow()) }
function removeBatchRow(idx) { batchRows.value.splice(idx, 1) }
function confirmBatchAdd() {
  const rows = batchRows.value.filter(r => (r.label || '').trim() || (r.field || '').trim())
  if (!rows.length) { proxy.$modal.msgWarning('请至少填写一行字段'); return }
  let order = fields.value.length
  for (const r of rows) {
    if (!r.label || !r.field) { proxy.$modal.msgWarning('存在未填写完整的行（字段显示名/数据库字段名称）'); return }
    if (!/^[a-z][a-z0-9_]{0,39}$/.test(r.field)) { proxy.$modal.msgWarning('字段名[' + r.field + ']不合法：小写字母开头的字母/数字/下划线'); return }
    if (fields.value.some(f => f.field === r.field)) { proxy.$modal.msgWarning('数据库字段名称已存在：' + r.field); return }
  }
  const table = activeTableTab.value === '__add' ? 'main' : activeTableTab.value
  rows.forEach(r => {
    order++
    fields.value.push({ rowKey: 'r' + Math.random().toString(36).slice(2, 8), table, order, ...r })
  })
  if (table !== 'main' && !detailTables.value.includes(table)) {
    detailTables.value.push(table)
    detailTables.value.sort()
  }
  proxy.$modal.msgSuccess('已批量添加 ' + rows.length + ' 个字段')
  batchOpen.value = false
}

function removeCheckedFields() {
  const keys = checkedFields.value.map(r => r.rowKey)
  fields.value = fields.value.filter(f => !keys.includes(f.rowKey))
  rebuildDetailTables()
  if (activeTableTab.value !== 'main' && !detailTables.value.includes(activeTableTab.value)) {
    activeTableTab.value = 'main'
  }
}

function removeDetailTable(name) {
  fields.value = fields.value.filter(f => f.table !== name)
  detailTables.value = detailTables.value.filter(t => t !== name)
  activeTableTab.value = 'main'
}

// "+" 添加明细表页签：新增后直接弹出添加字段
watch(activeTableTab, v => {
  if (v === '__add') {
    const used = new Set(['main', ...detailTables.value])
    let i = 1
    while (used.has('dt' + i)) i++
    const dt = 'dt' + i
    detailTables.value.push(dt)
    detailTables.value.sort()
    nextTick(() => { activeTableTab.value = dt; openAddField() })
  }
})

function submitForm() {
  if (!form.value.formName || !form.value.formKey) { proxy.$modal.msgWarning('请填写表单名称与表单Key'); return }
  if (!/^[a-z0-9][a-z0-9_]{0,39}$/.test(form.value.formKey)) { proxy.$modal.msgWarning('表单Key仅允许小写字母/数字开头的字母、数字、下划线'); return }
  for (const f of fields.value) {
    if (!f.label || !f.field) { proxy.$modal.msgWarning('存在未填写完整（显示名/字段名）的字段'); return }
    if (!/^[a-z][a-z0-9_]{0,39}$/.test(f.field)) { proxy.$modal.msgWarning('字段名[' + f.field + ']不合法：小写字母开头的字母/数字/下划线'); return }
    if (f.dtype === 'text' && (!f.length || f.length < 1)) { proxy.$modal.msgWarning('字段[' + f.label + ']请填写文本长度'); return }
  }
  const payload = {
    formId: form.value.formId, formKey: form.value.formKey, formName: form.value.formName,
    formDesc: form.value.formDesc || undefined,
    processKey: form.value.processKey || undefined,
    fieldsJson: JSON.stringify(fields.value.map(({ rowKey, ...rest }) => rest))
  }
  if (form.value.formId) {
    updateForm(payload).then(() => { proxy.$modal.msgSuccess('保存成功'); open.value = false; getList() })
  } else {
    addForm(payload).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
  }
}

function handleCreateTable(row) {
  proxy.$modal.confirm('将按当前字段定义生成数据表 ' + 'flow_data_' + row.formKey + '（明细表自动追加 _dt1/_dt2…，已存在的表仅补齐缺失字段列）。确认执行？')
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

.basic-form { padding: 8px 12px 0; }
.field-tab-bar { margin: 0 12px; }
.field-tab-bar .tab-tbl { font-size: 12px; color: var(--ink-500, #64748b); }
.field-tab-bar .tab-add { font-weight: 700; color: var(--brand-600, #4f46e5); }
.field-toolbar { display: flex; align-items: center; gap: 8px; margin: 10px 12px; }
.field-tip { font-size: 12px; color: var(--ink-500, #94a3b8); margin-left: 8px; }
.field-table { margin: 0 12px 8px; }
.form-tip { font-size: 12px; color: var(--ink-500, #94a3b8); line-height: 1.4; margin-top: 4px; }
.copy-field { display: flex; gap: 8px; width: 100%; }
.copy-field .el-input { flex: 1; }
.copy-clear { cursor: pointer; color: var(--ink-400, #94a3b8); }
.copy-clear:hover { color: var(--red-500, #ef4444); }
.copy-picker-bar { display: flex; gap: 8px; margin-bottom: 10px; }
.copy-table { width: 100%; }
.batch-toolbar { display: flex; align-items: center; gap: 8px; margin: 0 12px 10px; }
.tbl-name-row { line-height: 28px; }
</style>
