<template>
  <div class="app-container qms-std-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>标准编码</label>
          <div class="control">
            <el-input v-model="queryParams.stdCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>检验项目</label>
          <div class="control">
            <el-input v-model="queryParams.stdName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>判定规则</label>
          <div class="control is-select">
            <el-select v-model="queryParams.judgeRule" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_judge_rule" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:std:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:std:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:std:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:std:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="标准编码" prop="stdCode" :width="colWidth('stdCode', 140)" resizable show-overflow-tooltip />
          <el-table-column label="检验项目" prop="stdName" :width="colWidth('stdName', 180)" resizable show-overflow-tooltip />
          <el-table-column label="检验方法" prop="inspectMethod" :width="colWidth('inspectMethod', 200)" resizable show-overflow-tooltip />
          <el-table-column label="计量单位" prop="unit" :width="colWidth('unit', 100)" resizable align="center">
            <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
          </el-table-column>
          <el-table-column label="标准下限" prop="valMin" :width="colWidth('valMin', 100)" resizable align="center" />
          <el-table-column label="标准上限" prop="valMax" :width="colWidth('valMax', 100)" resizable align="center" />
          <el-table-column label="目标值" prop="targetVal" :width="colWidth('targetVal', 100)" resizable align="center" />
          <el-table-column label="判定规则" prop="judgeRule" :width="colWidth('judgeRule', 100)" resizable align="center">
            <template #default="scope"><dict-tag :options="qms_judge_rule" :value="scope.row.judgeRule" /></template>
          </el-table-column>
          <el-table-column label="行业类型" prop="industryType" :width="colWidth('industryType', 120)" resizable align="center">
            <template #default="scope"><dict-tag :options="qms_industry_type" :value="scope.row.industryType" /></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
            <template #default="scope"><span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:std:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:std:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="stdRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="标准编码" prop="stdCode"><el-input v-model="form.stdCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="检验项目" prop="stdName"><el-input v-model="form.stdName" placeholder="请输入检验项目" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="检验方法" prop="inspectMethod"><el-input v-model="form.inspectMethod" placeholder="请输入检验方法" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计量单位" prop="unit"><el-select v-model="form.unit" clearable placeholder="请选择计量单位" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>标准值</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="标准下限" prop="valMin"><el-input-number v-model="form.valMin" :precision="4" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="标准上限" prop="valMax"><el-input-number v-model="form.valMax" :precision="4" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="目标值" prop="targetVal"><el-input-number v-model="form.targetVal" :precision="4" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>判定配置</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="判定规则" prop="judgeRule"><el-select v-model="form.judgeRule" style="width: 100%"><el-option v-for="d in qms_judge_rule" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="行业类型" prop="industryType"><el-select v-model="form.industryType" filterable clearable placeholder="请选择" style="width: 100%"><el-option v-for="d in qms_industry_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">检验标准详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">标准编码</span><div class="rd-value">{{ viewData.stdCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验项目</span><div class="rd-value">{{ viewData.stdName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验方法</span><div class="rd-value">{{ viewData.inspectMethod || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">计量单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
            <div class="rd-item"><span class="rd-label">判定规则</span><div class="rd-value"><dict-tag :options="qms_judge_rule" :value="viewData.judgeRule" /></div></div>
            <div class="rd-item"><span class="rd-label">行业类型</span><div class="rd-value"><dict-tag :options="qms_industry_type" :value="viewData.industryType" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>标准值</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">标准下限</span><div class="rd-value">{{ viewData.valMin != null ? viewData.valMin : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">标准上限</span><div class="rd-value">{{ viewData.valMax != null ? viewData.valMax : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">目标值</span><div class="rd-value">{{ viewData.targetVal != null ? viewData.targetVal : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ viewData.status === '0' ? '正常' : '停用' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="QmsStd">
import { listStd, getStd, addStd, updateStd, delStd } from '@/api/qms/std'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_std_index')
const { qms_judge_rule, wms_unit, qms_industry_type } = proxy.useDict('qms_judge_rule', 'wms_unit', 'qms_industry_type')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, stdCode: undefined, stdName: undefined, judgeRule: undefined, status: undefined, params: {} },
  rules: {
    stdName: [{ required: true, message: '检验项目不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.stdCode) count++
  if (queryParams.value.stdName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.judgeRule) count++
  return count
})

function getList() { loading.value = true; listStd(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.stdCode = undefined; queryParams.value.stdName = undefined; queryParams.value.judgeRule = undefined; queryParams.value.status = undefined; queryParams.value.params = {}; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.stdId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { stdId: undefined, stdCode: undefined, stdName: undefined, inspectMethod: undefined, unit: undefined, valMin: undefined, valMax: undefined, targetVal: undefined, judgeRule: '1', industryType: undefined, status: '0', remark: undefined }; proxy.resetForm('stdRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加检验标准' }
function handleUpdate(row) { reset(); getStd(row.stdId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改检验标准' }) }
function handleView(row) { getStd(row.stdId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { proxy.$refs['stdRef'].validate(valid => { if (valid) { if (form.value.stdId != undefined) { updateStd(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addStd(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const stdIds = row.stdId || ids.value; proxy.$modal.confirm('确认删除编号为"' + stdIds + '"的数据？').then(() => delStd(stdIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('qms/std/export', { ...queryParams.value }, `std_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
onActivated(() => { getList() })
</script>

<style scoped>
.qms-std-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-std-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-std-page .filter-card { padding:14px 20px 16px; }
.qms-std-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-std-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-std-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-std-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-std-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-std-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-std-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-std-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-std-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-std-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-std-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-std-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-std-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-std-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-std-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-std-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-std-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-std-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-std-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-std-page .field .control :deep(.el-select) { width:100%; }
.qms-std-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-std-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-std-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-std-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-std-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-std-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-std-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-std-page .btn-soft .el-icon { font-size:14px; }
.qms-std-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-std-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-std-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-std-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-std-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-std-page .table-wrap { overflow-x:auto; }
.qms-std-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-std-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-std-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-std-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-std-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-std-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-std-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-std-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-std-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-std-page .badge.green .dot { background:var(--green-500); }
.qms-std-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-std-page .badge.gray .dot { background:var(--ink-400); }
.qms-std-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-std-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-std-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-std-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
