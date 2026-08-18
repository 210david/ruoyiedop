<template>
  <div class="app-container mms-issue-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>领料编号</label><div class="control"><el-input v-model="queryParams.issueNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>物料编码</label><div class="control"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>物料名称</label><div class="control"><el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_issue_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>批次号</label><div class="control"><el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>领料人</label><div class="control"><el-input v-model="queryParams.issueBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="{ 'is-active': activeStatusTab === s.value }" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:issue:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:issue:edit']">修改</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:issue:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:issue:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_issue_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column label="领料编号" prop="issueNo" key="issueNo" :width="colWidth('issueNo', 140)" resizable v-if="columns.issueNo.visible" /><el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" /><el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 130)" resizable v-if="columns.materialCode.visible" /><el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 200)" resizable show-overflow-tooltip v-if="columns.materialName.visible" /><el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip v-if="columns.specModel.visible" /><el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible" /><el-table-column label="领料数量" prop="issueQty" key="issueQty" :width="colWidth('issueQty', 100)" resizable align="center" v-if="columns.issueQty.visible" /><el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 120)" resizable v-if="columns.batchNo.visible" /><el-table-column label="领料人" prop="issueBy" key="issueBy" :width="colWidth('issueBy', 90)" resizable align="center" v-if="columns.issueBy.visible" /><el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column><el-table-column label="操作" width="180" align="center" fixed="right"><template #default="scope"><el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:issue:edit']">修改</el-button><el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:issue:remove']">删除</el-button></template></el-table-column></el-table></div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <el-dialog :title="title" v-model="open" width="700px" append-to-body><el-form ref="formRef" :model="form" :rules="rules" label-width="120px"><el-row :gutter="20"><el-col :span="12"><el-form-item label="领料编号" prop="issueNo"><el-input v-model="form.issueNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请输入" /></el-form-item></el-col></el-row><el-row :gutter="20"><el-col :span="12"><el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" placeholder="请输入" /></el-form-item></el-col></el-row><el-row :gutter="20"><el-col :span="8"><el-form-item label="规格型号" prop="specModel"><el-input v-model="form.specModel" placeholder="请输入" /></el-form-item></el-col><el-col :span="8"><el-form-item label="单位" prop="unit"><el-input v-model="form.unit" placeholder="请输入" /></el-form-item></el-col><el-col :span="8"><el-form-item label="领料数量" prop="issueQty"><el-input-number v-model="form.issueQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col></el-row><el-row :gutter="20"><el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="领料人" prop="issueBy"><el-input v-model="form.issueBy" placeholder="请输入" /></el-form-item></el-col></el-row><el-row :gutter="20"><el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" placeholder="请选择"><el-option v-for="d in mms_issue_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></el-form><template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template></el-dialog>
    <el-dialog v-model="showStatusHelp" title="领料管理业务操作说明" width="820px" append-to-body><div class="status-help-content"><h4>一、领料管理释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">什么是领料管理？</div><div class="highlight-card-body"><strong>领料管理（Material Issue）</strong>是生产管控中记录生产过程中从仓库领取物料进入生产线的关键单据。领料需关联生产工单和物料，记录领料数量和领料人，确保物料库存准确和生产成本正确归集。<br/><br/>领料管理遵循 <strong>MES 物料管理规范</strong>，领料数据与仓库管理系统（WMS）联动，确保库存数据实时一致，支持批次号管理便于物料追溯。</div></div><h4>二、重点业务规则</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">核心规则</div><div class="highlight-card-body"><p>• <strong>领料编号：</strong>系统自动生成，唯一标识每笔领料记录</p><p>• <strong>工单关联：</strong>每笔领料需关联生产工单，便于成本核算</p><p>• <strong>批次管理：</strong>支持批次号管理，便于物料追溯</p><p>• <strong>库存同步：</strong>领料确认后同步扣减仓库库存</p></div></div><h4>三、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>创建领料记录：</strong>点击「新增」创建领料记录，填写工单编号和物料信息</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>记录领料信息：</strong>填写领料数量、批次号和领料人</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>确认领料：</strong>确认领料信息无误后保存，系统同步扣减库存</el-timeline-item></el-timeline></div><template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template></el-dialog>
  </div>
</template>

<script setup name="Issue">
import { listIssue, getIssue, addIssue, updateIssue, delIssue } from "@/api/mms/issue";
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, ArrowDown, WarningFilled, ArrowRight, QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_issue_status } = proxy.useDict("mms_issue_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_issue_index')

const dataList = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);

const statusTabList = computed(() => mms_issue_status.value ? mms_issue_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { issueNo: { label: '领料编号', visible: true }, workOrderNo: { label: '工单编号', visible: true }, materialCode: { label: '物料编码', visible: true }, materialName: { label: '物料名称', visible: true }, specModel: { label: '规格型号', visible: true }, unit: { label: '单位', visible: true }, issueQty: { label: '领料数量', visible: true }, batchNo: { label: '批次号', visible: true }, issueBy: { label: '领料人', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_issue_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.issueNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.materialCode) c++; if (queryParams.value.materialName) c++; if (queryParams.value.status) c++; if (queryParams.value.batchNo) c++; if (queryParams.value.issueBy) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, issueNo: undefined, workOrderNo: undefined, materialCode: undefined, materialName: undefined, status: undefined, batchNo: undefined, issueBy: undefined, params: {} }, rules: { issueNo: [{ required: true, message: "请输入领料编号", trigger: "blur" }], workOrderNo: [{ required: true, message: "请输入工单编号", trigger: "blur" }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listIssue(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); updateStatusCounts(response.rows); }); }
function updateStatusCounts(rows) { const counts = { all: total.value }; if (mms_issue_status.value) { mms_issue_status.value.forEach(d => { counts[d.value] = rows.filter(r => r.status === d.value).length; }); } statusCounts.value = counts; }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.issueNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.status = undefined; queryParams.value.batchNo = undefined; queryParams.value.issueBy = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.issueId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { issueNo: undefined, workOrderNo: undefined, materialCode: undefined, materialName: undefined, specModel: undefined, unit: undefined, issueQty: undefined, batchNo: undefined, issueBy: undefined, status: undefined, remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增领料"; }
function handleUpdate(row) { reset(); const id = row.issueId || ids.value[0]; getIssue(id).then(response => { form.value = response.data; open.value = true; title.value = "修改领料"; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.issueId != null) { updateIssue(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addIssue(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.issueId || ids.value; proxy.$modal.confirm('是否确认删除选中的领料记录？').then(() => delIssue(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/issue/export", { ...queryParams.value }, `issue_${new Date().getTime()}.xlsx`); }
function dictLabel(dictRef, value) { if (!dictRef || !dictRef.value) return '-'; const item = dictRef.value.find(d => d.value == value); return item ? item.label : '-'; }
function statusLabel(status) { return dictLabel(mms_issue_status, status); }
function badgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'red' }; return map[status] || 'gray'; }

getList();
</script>

<style scoped>
.mms-issue-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-issue-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-issue-page .filter-card{padding:14px 20px 16px}
.mms-issue-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-issue-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-issue-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-issue-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-issue-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-issue-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-issue-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-issue-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-issue-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-issue-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-issue-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-issue-page .field{display:flex;flex-direction:column;gap:6px}
.mms-issue-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-issue-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-issue-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-issue-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-issue-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-issue-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-issue-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-issue-page .field .control :deep(.el-select){width:100%}
.mms-issue-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-issue-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-issue-page .toolbar .left,.mms-issue-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-issue-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-issue-page .table-wrap{overflow-x:auto}
.mms-issue-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-issue-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-issue-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-issue-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-issue-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-issue-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-issue-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-issue-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-issue-page .badge.amber .dot{background:var(--amber-500)}
.mms-issue-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-issue-page .badge.blue .dot{background:var(--blue-500)}
.mms-issue-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-issue-page .badge.green .dot{background:var(--green-500)}
.mms-issue-page .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.mms-issue-page .badge.red .dot{background:var(--red-500)}
.mms-issue-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-issue-page .badge.gray .dot{background:var(--ink-400)}
.mms-issue-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-issue-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-issue-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-issue-page .tabs-track::-webkit-scrollbar{display:none}
.mms-issue-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-issue-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-issue-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-issue-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-issue-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-issue-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-issue-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-issue-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-issue-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-issue-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.status-help-content{max-height:500px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{background-color:#ecf5ff;border-radius:8px;padding:16px;border-left:4px solid #409eff}
.status-help-content .highlight-card p{margin:6px 0;line-height:1.6;font-size:13px;color:#606266}
@media(max-width:1100px){.mms-issue-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.mms-issue-page .filter-card .filter-bar{grid-template-columns:1fr}}
</style>