<template>
  <div class="app-container qms-capa-page">
    <el-row :gutter="20">
      <el-col :span="showDetail ? 14 : 24">
        <!-- ===== Filter Card ===== -->
        <div class="surface filter-card" v-show="showSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>编号</label>
              <div class="control">
                <el-input v-model="queryParams.capaNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>来源</label>
              <div class="control is-select">
                <el-select v-model="queryParams.sourceType" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="d in sourceOptions" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control is-select">
                <el-select v-model="queryParams.capaStatus" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
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
              <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:capa:add']">新增</el-button>
              <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:capa:edit']">
                <el-icon><Edit /></el-icon> 修改
              </button>
              <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:capa:remove']">
                <el-icon><Delete /></el-icon> 删除
              </button>
              <div class="toolbar-divider"></div>
              <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:capa:export']">
                <el-icon><Download /></el-icon> 导出
              </button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @row-click="handleRowClick" @header-dragend="onHeaderDragEnd" highlight-current-row class="app-table">
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="CAPA编号" prop="capaNo" :width="colWidth('capaNo', 160)" resizable show-overflow-tooltip>
                <template #header><span>CAPA编号</span><el-tooltip content="CAPA（Corrective and Preventive Action）即纠正与预防措施编号，用于标识和追溯质量问题的闭环处理过程" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
              </el-table-column>
              <el-table-column label="标题" prop="title" :width="colWidth('title', 220)" resizable show-overflow-tooltip />
              <el-table-column label="来源" prop="sourceType" :width="colWidth('sourceType', 110)" resizable align="center"><template #default="scope"><dict-tag :options="sourceOptions" :value="scope.row.sourceType" /></template></el-table-column>
              <el-table-column label="来源编号" prop="sourceNo" :width="colWidth('sourceNo', 140)" resizable show-overflow-tooltip />
              <el-table-column label="状态" prop="capaStatus" :width="colWidth('capaStatus', 100)" resizable align="center"><template #default="scope"><dict-tag :options="statusOptions" :value="scope.row.capaStatus" /></template></el-table-column>
              <el-table-column label="责任人" prop="responsiblePerson" :width="colWidth('responsiblePerson', 100)" resizable show-overflow-tooltip />
              <el-table-column label="计划关闭" prop="planCloseTime" :width="colWidth('planCloseTime', 120)" resizable align="center"><template #default="scope"><span>{{ parseTime(scope.row.planCloseTime, '{y}-{m}-{d}') }}</span></template></el-table-column>
              <el-table-column label="操作" width="180" align="center" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:capa:edit']">修改</el-button>
                  <el-button v-if="scope.row.capaStatus !== '3'" link type="success" icon="CircleCheck" @click="handleClose(scope.row)" v-hasPermi="['qms:capa:close']">关闭</el-button>
                  <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:capa:remove']">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
        </div>
      </el-col>

      <!-- Right Detail Panel -->
      <el-col :span="10" v-show="showDetail">
        <div class="surface detail-panel">
          <div class="detail-head">
            <span class="detail-title">8D报告详情</span>
            <el-button link icon="Close" @click="showDetail = false" />
          </div>
          <div class="detail-body" v-if="detailData">
            <div class="rd-page">
              <section class="rd-card">
                <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
                <div class="rd-card-body" style="display:block"><div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">CAPA编号</span><div class="rd-value">{{ detailData.capaNo }}</div></div>
                  <div class="rd-item"><span class="rd-label">标题</span><div class="rd-value">{{ detailData.title }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">来源</span><div class="rd-value">{{ sourceOptions.find(d => d.value === detailData.sourceType)?.label }} {{ detailData.sourceNo }}</div></div>
                </div></div>
              </section>
              <section class="rd-card">
                <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>8D报告</div></div>
                <div class="rd-card-body" style="display:block"><div class="rd-grid">
                  <div class="rd-item rd-item--full"><span class="rd-label">D1 团队成员</span><div class="rd-value">{{ detailData.teamMembers }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">D2 问题描述</span><div class="rd-value">{{ detailData.problemDesc }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">D3 临时措施</span><div class="rd-value">{{ detailData.interimAction }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">D4 根因分析</span><div class="rd-value">{{ detailData.rootCause }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">D5 永久措施</span><div class="rd-value">{{ detailData.permanentAction }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">D6 验证结果</span><div class="rd-value">{{ detailData.verifyResult }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">D7 预防措施</span><div class="rd-value">{{ detailData.preventAction }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">D8 关闭备注</span><div class="rd-value">{{ detailData.closeRemark }}</div></div>
                </div></div>
              </section>
            </div>
          </div>
          <el-empty v-else description="点击行查看详情" />
        </div>
      </el-col>
    </el-row>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="capaRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="capaNo"><template #label><span>CAPA编号</span><el-tooltip content="CAPA（Corrective and Preventive Action）即纠正与预防措施编号，用于标识和追溯质量问题的闭环处理过程" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.capaNo" placeholder="留空自动生成" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="标题" prop="title"><el-input v-model="form.title" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="来源类型"><el-select v-model="form.sourceType" style="width: 100%"><el-option v-for="d in sourceOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="来源编号"><el-input v-model="form.sourceNo" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="责任人"><el-input v-model="form.responsiblePerson" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="计划关闭"><el-date-picker v-model="form.planCloseTime" type="date" value-format="YYYY-MM-DD" style="width: 200px" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>8D报告内容
                <el-tooltip content="8D（8 Disciplines）是质量管理中用于解决问题的结构化方法，包含8个步骤（D1-D8）：D1组队、D2问题描述、D3临时措施、D4根因分析、D5永久措施、D6验证、D7预防、D8关闭" placement="top"><el-icon class="rd-form-tip" style="margin-left:6px"><QuestionFilled /></el-icon></el-tooltip>
              </div>
            </div>
            <div class="rd-card-body">
              <el-form-item><template #label><span>D1 团队成员</span><el-tooltip content="D1：组建跨职能团队来解决问题，团队成员应包含具有相关知识和权限的人员" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.teamMembers" type="textarea" :rows="1" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D2 问题描述</span><el-tooltip content="D2：用可测量的术语清晰描述问题，明确谁、什么、何时、何地、为何" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.problemDesc" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D3 临时措施</span><el-tooltip content="D3：制定并实施临时遏制措施以隔离问题、防止问题扩大或产生不良影响" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.interimAction" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D4 根因分析</span><el-tooltip content="D4：识别并验证导致问题的根本原因，可使用鱼骨图、5Why等分析工具" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.rootCause" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D5 永久措施</span><el-tooltip content="D5：基于根因分析制定并实施永久性纠正措施，消除问题根因" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.permanentAction" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D6 验证结果</span><el-tooltip content="D6：验证永久纠正措施是否有效解决了问题，确保措施不会产生新的不良影响" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.verifyResult" type="textarea" :rows="1" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D7 预防措施</span><el-tooltip content="D7：制定预防措施，防止类似问题在其他产品或过程中再次发生" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.preventAction" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D8 关闭备注</span><el-tooltip content="D8：确认团队任务完成、认可团队贡献、记录经验教训并关闭CAPA" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.closeRemark" type="textarea" :rows="1" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsCapa">
import { listCapa, getCapa, addCapa, updateCapa, delCapa, closeCapa } from '@/api/qms/capa'
import { useColumnResize } from '@/composables/useColumnResize'
import { QuestionFilled } from '@element-plus/icons-vue'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_capa_index')
const { proxy } = getCurrentInstance()
const sourceOptions = [{ value: 'ncr', label: '不合格品NCR' }, { value: 'complaint', label: '客诉' }, { value: 'audit', label: '内审不符合项' }]
const { qms_capa_status: statusOptions } = proxy.useDict('qms_capa_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref('')
const showDetail = ref(false)
const detailData = ref(null)
const selectedId = ref(null)
const selectedIds = ref([])
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, capaNo: undefined, sourceType: undefined, capaStatus: undefined } })
const { queryParams, form } = toRefs(data)
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.capaNo) count++
  if (queryParams.value.sourceType) count++
  if (queryParams.value.capaStatus) count++
  return count
})

function getList() {
  loading.value = true
  listCapa(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增CAPA' }
function handleUpdate(row) {
  const id = row?.capaId || selectedId.value
  getCapa(id).then(res => {
    data.form = res.data
    open.value = true
    title.value = '修改CAPA'
  })
}
function handleRowClick(row) {
  selectedId.value = row.capaId
  detailData.value = row
  showDetail.value = true
}
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.capaId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['capaRef'].validate(valid => {
    if (valid) {
      if (form.value.capaId) { updateCapa(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addCapa(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const ids = row?.capaId ? [row.capaId] : selectedIds.value
  proxy.$modal.confirm('确认删除编号为"' + ids + '"的数据项？').then(() => delCapa(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleClose(row) {
  proxy.$modal.confirm('确认关闭CAPA "' + row.capaNo + '"？').then(() => closeCapa(row.capaId)).then(() => { getList(); proxy.$modal.msgSuccess('关闭成功') }).catch(() => {})
}
function handleExport() { proxy.download('qms/capa/export', { ...queryParams.value }, `capa_${new Date().getTime()}.xlsx`) }
function reset() {
  form.value = { capaId: null, capaNo: undefined, sourceType: 'ncr', sourceNo: undefined, title: undefined, teamMembers: undefined, problemDesc: undefined, interimAction: undefined, rootCause: undefined, permanentAction: undefined, verifyResult: undefined, preventAction: undefined, closeRemark: undefined, capaStatus: '0', responsiblePerson: undefined, planCloseTime: undefined }
  proxy.resetForm('capaRef')
}
function cancel() { open.value = false; reset() }
getList()
</script>

<style scoped>
.qms-capa-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-capa-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-capa-page .filter-card { padding:14px 20px 16px; }
.qms-capa-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-capa-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-capa-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-capa-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(3, minmax(0,1fr)); gap:12px 16px; }
.qms-capa-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-capa-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-capa-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-capa-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-capa-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-capa-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-capa-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-capa-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-capa-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-capa-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-capa-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-capa-page .field .control :deep(.el-select) { width:100%; }
.qms-capa-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-capa-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-capa-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-capa-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-capa-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-capa-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-capa-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-capa-page .btn-soft .el-icon { font-size:14px; }
.qms-capa-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-capa-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-capa-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-capa-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-capa-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-capa-page .table-wrap { overflow-x:auto; }
.qms-capa-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-capa-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-capa-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-capa-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-capa-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-capa-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-capa-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.qms-capa-page .detail-panel { height: fit-content; }
.qms-capa-page .detail-panel .detail-head { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-capa-page .detail-panel .detail-title { font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-capa-page .detail-panel .detail-body { padding:16px; max-height: 600px; overflow-y: auto; }
.qms-capa-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.qms-capa-page .rd-form-tip:hover { color: #909399; }
</style>
