<template>
  <div class="app-container mms-flowcard-page">
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
          <label>流转卡号</label>
          <div class="control">
            <el-input v-model="queryParams.cardCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单编号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>当前工序</label>
          <div class="control">
            <el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_flowcard_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:flowcard:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:flowcard:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:flowcard:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:flowcard:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_flowcard_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="流转卡号" prop="cardCode" key="cardCode" :width="colWidth('cardCode', 150)" resizable v-if="columns.cardCode.visible" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 130)" resizable v-if="columns.batchNo.visible" />
          <el-table-column label="当前工序序号" prop="opSeq" key="opSeq" :width="colWidth('opSeq', 110)" resizable align="center" v-if="columns.opSeq.visible" />
          <el-table-column label="当前工序" prop="processName" key="processName" :width="colWidth('processName', 140)" resizable show-overflow-tooltip v-if="columns.processName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="打印次数" prop="printCount" key="printCount" :width="colWidth('printCount', 90)" resizable align="center" v-if="columns.printCount.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:flowcard:query']">详情</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:flowcard:edit']">修改</el-button>
              <el-button link type="success" icon="Printer" @click="handlePrint(scope.row)" v-hasPermi="['mms:flowcard:print']">打印</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改 Dialog ===== -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="流转卡号" prop="cardCode">
              <el-input v-model="form.cardCode" placeholder="自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单编号" prop="workOrderNo">
              <el-input v-model="form.workOrderNo" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前工序序号" prop="opSeq">
              <el-input-number v-model="form.opSeq" :min="0" placeholder="请输入" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前工序" prop="processName">
              <el-input v-model="form.processName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择">
                <el-option v-for="d in mms_flowcard_status" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="800px" append-to-body draggable>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="6" width="20" height="12" rx="2"/><path d="M6 10h.01M10 10h.01M14 10h4"/></svg>
          </div>
          <span class="rd-detail-header-title">流转卡详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.cardCode">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewData.cardCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">流转卡号</span><div class="rd-value">{{ viewData.cardCode || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">当前工序序号</span><div class="rd-value">{{ viewData.opSeq != null ? viewData.opSeq : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">当前工序</span><div class="rd-value">{{ viewData.processName || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span></div></div>
          <div class="rd-item"><span class="rd-label">打印次数</span><div class="rd-value">{{ viewData.printCount != null ? viewData.printCount : 0 }}</div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div>
          <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div>
        </div>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="流转卡管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、流转卡释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是流转卡？</div>
          <div class="highlight-card-body">
            <strong>流转卡（Flow Card）</strong>是生产管控中跟踪在制品（WIP）在工序间流转的跟踪单据。流转卡记录当前工序、批次号、状态和打印次数，支持工序级进度追踪和批次追溯，是实现工序级生产管控的核心载体。<br/><br/>
            流转卡遵循 <strong>MES 在制品管理规范</strong>，通过工序流转记录实现在制品的精确追踪，支持批次管理和打印管理，确保生产过程可追溯。
          </div>
        </div>
        <h4>二、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>创建流转卡：</strong>工单下达后系统自动生成流转卡，关联批次号和工序信息</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>工序流转：</strong>流转卡随在制品在工序间流转，记录当前工序状态</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>打印管理：</strong>流转卡可打印实物卡，支持多次打印并记录打印次数</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowCard">
import { listFlowCard, getFlowCard, addFlowCard, updateFlowCard, delFlowCard, printFlowCard } from "@/api/mms/flowcard";
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_flowcard_status } = proxy.useDict("mms_flowcard_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_flowcard_index')

const dataList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const showStatusHelp = ref(false);
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

const statusTabList = computed(() => {
  if (!mms_flowcard_status.value) return [];
  return mms_flowcard_status.value.map(d => ({ label: d.label, value: d.value }));
});

const defaultColumns = {
  cardCode: { label: '流转卡号', visible: true },
  workOrderNo: { label: '工单编号', visible: true },
  batchNo: { label: '批次号', visible: true },
  opSeq: { label: '当前工序序号', visible: true },
  processName: { label: '当前工序', visible: true },
  status: { label: '状态', visible: true },
  printCount: { label: '打印次数', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_flowcard_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.value.cardCode) count++;
  if (queryParams.value.workOrderNo) count++;
  if (queryParams.value.batchNo) count++;
  if (queryParams.value.processName) count++;
  if (queryParams.value.status) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    cardCode: undefined,
    workOrderNo: undefined,
    batchNo: undefined,
    processName: undefined,
    status: undefined,
    params: {}
  },
  rules: {
    workOrderNo: [{ required: true, message: "请输入工单编号", trigger: "blur" }],
    batchNo: [{ required: true, message: "请输入批次号", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listFlowCard(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    updateStatusCounts(response.rows);
  });
}

function updateStatusCounts(rows) {
  const counts = { all: total.value };
  if (mms_flowcard_status.value) {
    mms_flowcard_status.value.forEach(d => {
      counts[d.value] = rows.filter(r => r.status === d.value).length;
    });
  }
  statusCounts.value = counts;
}

function handleQuery() {
  showAdvanced.value = false;
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value.cardCode = undefined;
  queryParams.value.workOrderNo = undefined;
  queryParams.value.batchNo = undefined;
  queryParams.value.processName = undefined;
  queryParams.value.status = undefined;
  dateRange.value = [];
  queryParams.value.params = {};
  activeStatusTab.value = 'all';
  handleQuery();
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status;
  queryParams.value.status = status === "all" ? undefined : status;
  handleQuery();
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop;
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc';
  } else {
    queryParams.value.params.orderByColumn = undefined;
    queryParams.value.params.isAsc = undefined;
  }
  getList();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.cardId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    cardId: undefined,
    cardCode: undefined,
    workOrderId: undefined,
    workOrderNo: undefined,
    batchNo: undefined,
    opSeq: undefined,
    processName: undefined,
    status: undefined,
    qrContent: undefined,
    printCount: undefined,
    remark: undefined,
  };
  proxy.resetForm("formRef");
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增流转卡";
}

function handleUpdate(row) {
  reset();
  const id = row.cardId || ids.value[0];
  getFlowCard(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改流转卡";
  });
}

function handleView(row) {
  getFlowCard(row.cardId).then(response => {
    viewData.value = response.data;
    viewOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.cardId != null) {
        updateFlowCard(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addFlowCard(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function cancel() {
  open.value = false;
  reset();
}

function handleDelete(row) {
  const delIds = row.cardId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的流转卡？').then(function() {
    return delFlowCard(delIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleExport() {
  proxy.download("mms/flowcard/export", { ...queryParams.value }, `flowcard_${new Date().getTime()}.xlsx`);
}

function handlePrint(row) {
  proxy.$modal.confirm('是否确认打印流转卡"' + row.cardCode + '"？').then(function() {
    return printFlowCard(row.cardId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("打印成功");
  }).catch(() => {});
}

function statusLabel(status) {
  const item = mms_flowcard_status.value.find(d => d.value == status);
  return item ? item.label : '-';
}

function badgeClass(status) {
  const map = { '0': 'blue', '1': 'green', '2': 'red' };
  return map[status] || 'gray';
}

function statusTabClass(value) {
  const map = { '0': 'tab-audit', '1': 'tab-done', '2': 'tab-reject' };
  return map[value] || '';
}

getList();
</script>

<style scoped>
.mms-flowcard-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.mms-flowcard-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-flowcard-page .filter-card { padding:14px 20px 16px; }
.mms-flowcard-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-flowcard-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-flowcard-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-flowcard-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-flowcard-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-flowcard-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-flowcard-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-flowcard-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-flowcard-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-flowcard-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-flowcard-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-flowcard-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-flowcard-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-flowcard-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-flowcard-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-flowcard-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-flowcard-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-flowcard-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-flowcard-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-flowcard-page .field .control :deep(.el-select) { width:100%; }
.mms-flowcard-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-flowcard-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-flowcard-page .toolbar .left, .mms-flowcard-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-flowcard-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-flowcard-page .table-wrap { overflow-x:auto; }
.mms-flowcard-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-flowcard-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-flowcard-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-flowcard-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-flowcard-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-flowcard-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-flowcard-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-flowcard-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-flowcard-page .badge.amber .dot { background:var(--amber-500); }
.mms-flowcard-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-flowcard-page .badge.blue .dot { background:var(--blue-500); }
.mms-flowcard-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-flowcard-page .badge.green .dot { background:var(--green-500); }
.mms-flowcard-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .mms-flowcard-page .badge.red .dot { background:var(--red-500); }
.mms-flowcard-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-flowcard-page .badge.gray .dot { background:var(--ink-400); }
.mms-flowcard-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-flowcard-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-flowcard-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-flowcard-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-flowcard-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-flowcard-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-flowcard-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-flowcard-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-flowcard-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-flowcard-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-flowcard-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-flowcard-page .status-tab.tab-audit .dot { background:var(--blue-500); } .mms-flowcard-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .mms-flowcard-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.mms-flowcard-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-flowcard-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-flowcard-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-flowcard-page .status-tab.tab-reject .dot { background:var(--red-500); } .mms-flowcard-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); } .mms-flowcard-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }
.rd-page { max-width: 760px; margin: 0 auto; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
@media (max-width:1100px) { .mms-flowcard-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mms-flowcard-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
