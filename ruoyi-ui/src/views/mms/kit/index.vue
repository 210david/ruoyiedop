<template>
  <div class="app-container mms-kit-page">
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
          <label>齐套单号</label>
          <div class="control"><el-input v-model="queryParams.kitNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>主计划号</label>
          <div class="control"><el-input v-model="queryParams.mpsNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>是否齐套</label>
          <div class="control is-select"><el-select v-model="queryParams.isComplete" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_yes_no" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_kit_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>BOM编号</label>
          <div class="control"><el-input v-model="queryParams.bomNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>检查日期</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button>
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:kit:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:kit:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:kit:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:kit:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_kit_columns" /></div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="齐套单号" prop="kitNo" key="kitNo" :width="colWidth('kitNo', 140)" resizable v-if="columns.kitNo.visible" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="主计划号" prop="mpsNo" key="mpsNo" :width="colWidth('mpsNo', 140)" resizable v-if="columns.mpsNo.visible" />
          <el-table-column label="检查时间" prop="checkTime" key="checkTime" :width="colWidth('checkTime', 160)" resizable align="center" v-if="columns.checkTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.checkTime) }}</span></template>
          </el-table-column>
          <el-table-column label="齐套率(%)" prop="kitRate" key="kitRate" :width="colWidth('kitRate', 100)" resizable align="center" v-if="columns.kitRate.visible">
            <template #default="scope"><span :style="{ color: scope.row.kitRate >= 100 ? '#10b981' : scope.row.kitRate >= 80 ? '#f59e0b' : '#ef4444', fontWeight: 600 }">{{ scope.row.kitRate }}%</span></template>
          </el-table-column>
          <el-table-column label="是否齐套" prop="isComplete" key="isComplete" :width="colWidth('isComplete', 90)" resizable align="center" v-if="columns.isComplete.visible">
            <template #default="scope"><span class="badge" :class="scope.row.isComplete === '1' ? 'green' : 'red'"><span class="dot"></span>{{ dictLabel(mms_yes_no, scope.row.isComplete) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:kit:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="success" icon="Aim" @click="handleExecute(scope.row)" v-hasPermi="['mms:kit:execute']">执行检查</el-button>
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['mms:kit:query']">明细</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改 Dialog ===== -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="齐套单号" prop="kitNo"><el-input v-model="form.kitNo" placeholder="自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工单号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="主计划号" prop="mpsNo"><el-input v-model="form.mpsNo" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="BOM编号" prop="bomNo"><el-input v-model="form.bomNo" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 明细Dialog ===== -->
    <el-dialog title="齐套检查明细" v-model="detailOpen" width="900px" append-to-body>
      <el-table :data="detailList" border>
        <el-table-column label="物料编码" align="center" prop="materialCode" width="120" />
        <el-table-column label="物料名称" align="center" prop="materialName" min-width="150" show-overflow-tooltip />
        <el-table-column label="规格型号" align="center" prop="specModel" width="120" />
        <el-table-column label="单位" align="center" prop="unit" width="60" />
        <el-table-column label="需求数量" align="center" prop="requiredQty" width="100" />
        <el-table-column label="可用数量" align="center" prop="availableQty" width="100" />
        <el-table-column label="缺口量" align="center" prop="shortageQty" width="100">
          <template #default="scope"><span :style="{ color: scope.row.shortageQty > 0 ? '#ef4444' : '#10b981', fontWeight: 600 }">{{ scope.row.shortageQty }}</span></template>
        </el-table-column>
        <el-table-column label="是否齐套" align="center" prop="isComplete" width="80">
          <template #default="scope"><span class="badge" :class="scope.row.isComplete === '1' ? 'green' : 'red'"><span class="dot"></span>{{ dictLabel(mms_yes_no, scope.row.isComplete) }}</span></template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="齐套检查业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、齐套检查释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是齐套检查？</div>
          <div class="highlight-card-body">
            <strong>齐套检查（Kit Check）</strong>是生产管控中在工单开工前对物料齐套情况进行校验的单据。系统自动比对BOM物料需求与当前库存，计算齐套率，标识缺口物料，确保工单开工前物料充足，避免因缺料导致生产中断。<br/><br/>
            齐套检查遵循 <strong>MES 物料齐套管理规范</strong>，通过齐套率（≥100%齐套/80-99%部分齐套/&lt;80%不齐套）直观反映物料准备状态，支持缺料预警和物料跟进。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="warning">待检查</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">点击「执行检查」</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已检查</el-tag></div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待检查">齐套检查新建后的初始状态，等待执行检查</el-descriptions-item>
          <el-descriptions-item label="已检查">齐套检查已执行完成，可查看明细</el-descriptions-item>
        </el-descriptions>
        <h4>四、重点业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心规则</div>
          <div class="highlight-card-body">
            <p>• <strong>执行检查：</strong>待检查状态的齐套单可执行检查操作，系统自动比对库存与需求</p>
            <p>• <strong>齐套率：</strong>齐套率≥100%为齐套（绿色），80-99%为部分齐套（黄色），&lt;80%为不齐套（红色）</p>
            <p>• <strong>明细查看：</strong>已检查的齐套单可查看各物料的齐套明细，包括缺口量</p>
            <p>• <strong>缺口量监控：</strong>缺口量大于0时自动标红显示，便于物料跟进</p>
          </div>
        </div>
        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>创建齐套单：</strong>点击「新增」创建齐套检查单，关联工单</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>执行检查：</strong>点击「执行检查」系统自动比对BOM需求与库存，计算齐套率</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>查看明细：</strong>检查完成后查看各物料的齐套明细和缺口量，跟进缺料</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="KitCheck">
import { listKit, getKitCheck, addKitCheck, updateKitCheck, delKitCheck, executeKitCheck, getKitCheckDetail } from "@/api/mms/kit";
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_kit_status, mms_yes_no } = proxy.useDict("mms_kit_status", "mms_yes_no");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_kit_index')

const dataList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusCounts = ref({});
const showStatusHelp = ref(false);
const detailOpen = ref(false);
const detailList = ref([]);

const statusTabList = computed(() => mms_kit_status.value ? mms_kit_status.value.map(d => ({ label: d.label, value: d.value })) : []);

const defaultColumns = {
  kitNo: { label: '齐套单号', visible: true }, workOrderNo: { label: '工单号', visible: true },
  mpsNo: { label: '主计划号', visible: true }, checkTime: { label: '检查时间', visible: true },
  kitRate: { label: '齐套率', visible: true }, isComplete: { label: '是否齐套', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_kit_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.kitNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.mpsNo) c++; if (queryParams.value.isComplete) c++; if (queryParams.value.status) c++; if (queryParams.value.bomNo) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, kitNo: undefined, workOrderNo: undefined, mpsNo: undefined, bomNo: undefined, isComplete: undefined, status: undefined, params: {} },
  rules: { workOrderNo: [{ required: true, message: "请输入工单号", trigger: "blur" }] }
});
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listKit(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); updateStatusCounts(response.rows); }); }
function updateStatusCounts(rows) { const counts = { all: total.value }; if (mms_kit_status.value) { mms_kit_status.value.forEach(d => { counts[d.value] = rows.filter(r => r.status === d.value).length; }); } statusCounts.value = counts; }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.kitNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.mpsNo = undefined; queryParams.value.bomNo = undefined; queryParams.value.isComplete = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.kitId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { kitNo: undefined, workOrderNo: undefined, mpsNo: undefined, bomNo: undefined, remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增齐套检查"; }
function handleUpdate(row) { reset(); const id = row.kitId || ids.value[0]; getKitCheck(id).then(response => { form.value = response.data; open.value = true; title.value = "修改齐套检查"; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.kitId != null) { updateKitCheck(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addKitCheck(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.kitId || ids.value; proxy.$modal.confirm('是否确认删除选中的齐套检查记录？').then(() => delKitCheck(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/kit/export", { ...queryParams.value }, `kit_${new Date().getTime()}.xlsx`); }
function handleExecute(row) { proxy.$modal.confirm('是否执行齐套检查？').then(() => executeKitCheck(row.kitId)).then(() => { getList(); proxy.$modal.msgSuccess("执行成功"); }).catch(() => {}); }
function handleDetail(row) { getKitCheckDetail(row.kitId).then(response => { detailList.value = response.data; detailOpen.value = true; }); }

function dictLabel(dictRef, value) { if (!dictRef || !dictRef.value) return '-'; const item = dictRef.value.find(d => d.value == value); return item ? item.label : '-'; }
function statusLabel(status) { return dictLabel(mms_kit_status, status); }
function badgeClass(status) { const map = { '0': 'amber', '1': 'green' }; return map[status] || 'gray'; }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-done' }; return map[value] || ''; }

getList();
</script>

<style scoped>
.mms-kit-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-kit-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-kit-page .filter-card{padding:14px 20px 16px}
.mms-kit-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-kit-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-kit-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-kit-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-kit-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-kit-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-kit-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-kit-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-kit-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-kit-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-kit-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-kit-page .field{display:flex;flex-direction:column;gap:6px}
.mms-kit-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-kit-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-kit-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-kit-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-kit-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-kit-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-kit-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-kit-page .field .control :deep(.el-select){width:100%}
.mms-kit-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-kit-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-kit-page .toolbar .left,.mms-kit-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-kit-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-kit-page .table-wrap{overflow-x:auto}
.mms-kit-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-kit-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-kit-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-kit-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-kit-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-kit-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-kit-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-kit-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-kit-page .badge.amber .dot{background:var(--amber-500)}
.mms-kit-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-kit-page .badge.blue .dot{background:var(--blue-500)}
.mms-kit-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-kit-page .badge.green .dot{background:var(--green-500)}
.mms-kit-page .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.mms-kit-page .badge.red .dot{background:var(--red-500)}
.mms-kit-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-kit-page .badge.gray .dot{background:var(--ink-400)}
.mms-kit-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-kit-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-kit-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-kit-page .tabs-track::-webkit-scrollbar{display:none}
.mms-kit-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-kit-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-kit-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-kit-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-kit-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-kit-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-kit-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-kit-page .status-tab.tab-draft .dot{background:var(--amber-500)}.mms-kit-page .status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)}.mms-kit-page .status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
.mms-kit-page .status-tab.tab-done .dot{background:var(--green-500)}.mms-kit-page .status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)}.mms-kit-page .status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
.mms-kit-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-kit-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-kit-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.status-help-content{max-height:500px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{background-color:#ecf5ff;border-radius:8px;padding:16px;border-left:4px solid #409eff}
.status-help-content .highlight-card p{margin:6px 0;line-height:1.6;font-size:13px;color:#606266}
@media(max-width:1100px){.mms-kit-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.mms-kit-page .filter-card .filter-bar{grid-template-columns:1fr}}
</style>