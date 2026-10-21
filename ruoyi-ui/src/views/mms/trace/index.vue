<template>
  <div class="app-container mms-trace-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>投入批次</label><div class="control"><el-input v-model="queryParams.batchIn" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产出批次</label><div class="control"><el-input v-model="queryParams.batchOut" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>工单号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>追溯类型</label><div class="control is-select"><el-select v-model="queryParams.sourceType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="t in sourceTypes" :key="t.value" :label="t.label" :value="t.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>物料编码</label><div class="control"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>物料名称</label><div class="control"><el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>工序</label><div class="control"><el-input v-model="queryParams.opName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>产线</label><div class="control"><el-input v-model="queryParams.lineName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>供应商</label><div class="control"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>操作员</label><div class="control"><el-input v-model="queryParams.operator" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>断点状态</label><div class="control is-select"><el-select v-model="queryParams.breakFlag" placeholder="全部" clearable @change="handleQuery"><el-option label="正常" :value="0" /><el-option label="断点" :value="1" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>追溯时间</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab is-active">
            <span class="dot"></span>
            <span>全部</span>
            <span class="count">{{ total }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showHelp = true">
          <el-icon><QuestionFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:trace:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_trace_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
          <el-table-column label="追溯类型" prop="sourceType" key="sourceType" :width="colWidth('sourceType', 100)" align="center" resizable v-if="columns.sourceType.visible">
            <template #default="scope"><span class="badge" :class="sourceClass(scope.row.sourceType)">{{ sourceLabel(scope.row.sourceType) }}</span></template>
          </el-table-column>
          <el-table-column label="来源单据" prop="sourceNo" key="sourceNo" :width="colWidth('sourceNo', 150)" resizable v-if="columns.sourceNo.visible" />
          <el-table-column label="投入批次" prop="batchIn" key="batchIn" :width="colWidth('batchIn', 160)" resizable v-if="columns.batchIn.visible">
            <template #default="scope">
              <el-tooltip v-if="scope.row.batchIn && !String(scope.row.batchIn).startsWith('领料-')" content="点击查看正向追溯：该原料批次流向了哪些成品" placement="top">
                <el-link type="primary" :underline="false" @click="jumpTrace(scope.row.batchIn, 'forward')">{{ scope.row.batchIn }}</el-link>
              </el-tooltip>
              <span v-else-if="scope.row.batchIn" class="text-muted">{{ scope.row.batchIn }}</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="产出批次" prop="batchOut" key="batchOut" :width="colWidth('batchOut', 160)" resizable v-if="columns.batchOut.visible">
            <template #default="scope">
              <el-tooltip v-if="scope.row.batchOut" content="点击查看反向追溯：该成品批次用了哪些原料、经过哪些工序" placement="top">
                <el-link type="primary" :underline="false" @click="jumpTrace(scope.row.batchOut, 'backward')">{{ scope.row.batchOut }}</el-link>
              </el-tooltip>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 150)" resizable v-if="columns.workOrderNo.visible">
            <template #default="scope">
              <el-tooltip v-if="scope.row.workOrderNo" content="点击跳转工单管理，查看该工单详情" placement="top">
                <el-link type="primary" :underline="false" @click="jumpWorkOrder(scope.row.workOrderNo)">{{ scope.row.workOrderNo }}</el-link>
              </el-tooltip>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible"><template #default="scope"><span v-if="scope.row.productCode">{{ scope.row.productCode }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="产品名称" prop="productName" key="productName" :min-width="colWidth('productName', 160)" resizable show-overflow-tooltip v-if="columns.productName.visible"><template #default="scope"><span v-if="scope.row.productName">{{ scope.row.productName }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="工序" prop="opName" key="opName" :width="colWidth('opName', 130)" resizable v-if="columns.opName.visible"><template #default="scope"><span v-if="scope.row.opName">{{ scope.row.opName }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="产线" prop="lineName" key="lineName" :width="colWidth('lineName', 120)" resizable v-if="columns.lineName.visible"><template #default="scope"><span v-if="scope.row.lineName">{{ scope.row.lineName }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 140)" resizable v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :min-width="colWidth('materialName', 150)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="投入数量" prop="qtyUsed" key="qtyUsed" :width="colWidth('qtyUsed', 100)" resizable align="center" v-if="columns.qtyUsed.visible"><template #default="scope"><span v-if="scope.row.qtyUsed != null">{{ scope.row.qtyUsed }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="产出数量" prop="qtyOut" key="qtyOut" :width="colWidth('qtyOut', 100)" resizable align="center" v-if="columns.qtyOut.visible"><template #default="scope"><span v-if="scope.row.qtyOut != null">{{ scope.row.qtyOut }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 140)" resizable show-overflow-tooltip v-if="columns.supplierName.visible">
            <template #default="scope">
              <el-tooltip v-if="scope.row.supplierName" content="点击跳转供应商管理，查看该供应商档案" placement="top">
                <el-link type="warning" :underline="false" @click="jumpSupplier(scope.row.supplierName)">{{ scope.row.supplierName }}</el-link>
              </el-tooltip>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作员" prop="operator" key="operator" :width="colWidth('operator', 100)" resizable align="center" v-if="columns.operator.visible"><template #default="scope"><span v-if="scope.row.operator">{{ scope.row.operator }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="追溯时间" prop="traceTime" key="traceTime" :width="colWidth('traceTime', 160)" resizable align="center" v-if="columns.traceTime.visible" />
          <el-table-column label="断点" prop="breakFlag" key="breakFlag" :width="colWidth('breakFlag', 130)" resizable align="center" v-if="columns.breakFlag.visible">
            <template #default="scope">
              <span v-if="scope.row.breakFlag === 1" class="badge red" :title="scope.row.breakReason">{{ scope.row.breakReason || '断点' }}</span>
              <span v-else class="badge green">正常</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" key="操作" :width="colWidth('操作', 150)" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Share" @click="handleTrace(scope.row, 'backward')" v-hasPermi="['mms:trace:query']">反向追溯</el-button>
              <el-button link type="warning" icon="Share" @click="handleTrace(scope.row, 'forward')" v-hasPermi="['mms:trace:query']">正向追溯</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showHelp" title="批次追溯业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、报表释义 -->
        <h4>一、报表释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是批次追溯？</div>
          <div class="highlight-card-body">
            <p><strong>批次追溯</strong>是记录和追溯工单产出批次与投入批次（物料）对应关系的生产报表。</p>
            <p>通过追溯工单使用物料的批次信息，实现正反向追溯：正向可查询某批次物料被用于哪些工单，反向可查询某工单使用了哪些批次的物料。满足<strong>ISO 9001 质量管理体系</strong>对产品追溯性的要求。</p>
          </div>
        </div>

        <!-- 二、核心指标定义 -->
        <h4>二、核心指标定义</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">字段说明</div>
          <div class="highlight-card-body">
            <p><strong>产出批次</strong> = 工单编号（产出的成品/半成品批次） <span style="color: #f56c6c;">*系统自动提取</span></p>
            <p><strong>投入批次</strong> = 退料单号；无退料记录则显示“领料-物料编码” <span style="color: #f56c6c;">*系统自动提取</span></p>
            <p><strong>产品编码/产品名称</strong> = 工单生产的产品信息 <span style="color: #f56c6c;">*系统自动提取</span></p>
            <p><strong>物料编码/名称/规格/单位</strong> = 来自齐套检查明细（mms_kit_check_detail） <span style="color: #f56c6c;">*系统自动提取</span></p>
            <p><strong>使用数量</strong> = 齐套检查明细中的需求量（required_qty），即工单所需物料的数量 <span style="color: #f56c6c;">*系统自动提取</span></p>
          </div>
        </div>

        <!-- 三、数据来源 -->
        <h4>三、数据来源</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="齐套检查明细（mms_kit_check_detail）">提供投入批次、物料编码、需求量等追溯基础数据</el-descriptions-item>
          <el-descriptions-item label="工单表（mms_work_order）">提供产出批次（工单号）、产品编码、产品名称等关联数据</el-descriptions-item>
          <el-descriptions-item label="退料表（mms_return_material）">提供退料单号，用于投入批次的退料追溯</el-descriptions-item>
        </el-descriptions>

        <!-- 四、追溯场景 -->
        <h4>四、追溯场景</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">典型追溯场景</div>
          <div class="highlight-card-body">
            <p><strong>正向追溯（物料→产品）：</strong>当某批次物料发现质量问题时，通过投入批次查询该物料被用于哪些工单，定位受影响的产品批次</p>
            <p><strong>反向追溯（产品→物料）：</strong>当某批次产品发现质量问题时，通过产出批次查询该工单使用了哪些批次的物料，定位问题物料来源</p>
          </div>
        </div>

      </div>
      <template #footer><el-button type="primary" @click="showHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup name="Trace">
import { listTrace } from "@/api/mms/trace";
import { useRouter } from 'vue-router'
import { useColumnResize } from '@/composables/useColumnResize'
import { Filter, RefreshLeft, QuestionFilled, ArrowDown } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const router = useRouter();
const { wms_unit } = proxy.useDict("wms_unit")
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_trace_index')
const dataList = ref([]); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false); const total = ref(0);
const showHelp = ref(false);
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, batchOut: undefined, batchIn: undefined, workOrderNo: undefined, materialCode: undefined, materialName: undefined, sourceType: undefined, opName: undefined, lineName: undefined, supplierName: undefined, operator: undefined, breakFlag: undefined, params: {} } });
const { queryParams } = toRefs(data);
const dateRange = ref([]);
const activeFilterCount = computed(() => {
  let c = 0;
  const q = queryParams.value;
  if (q.batchIn) c++; if (q.batchOut) c++; if (q.workOrderNo) c++;
  if (q.sourceType) c++; if (q.materialCode) c++; if (q.materialName) c++;
  if (q.opName) c++; if (q.lineName) c++; if (q.supplierName) c++;
  if (q.operator) c++; if (q.breakFlag !== undefined && q.breakFlag !== null && q.breakFlag !== '') c++;
  if (dateRange.value && dateRange.value.length === 2) c++;
  return c;
});
// 追溯类型（谱系来源）
const sourceTypes = [
  { value: 'feeding', label: '领料投料' },
  { value: 'report', label: '报工' },
  { value: 'complete', label: '完工入库' },
  { value: 'rework', label: '返工' },
  { value: 'manual', label: '手工补录' }
];
const columns = reactive({
  sourceType: { label: '追溯类型', visible: true },
  sourceNo: { label: '来源单据', visible: true },
  batchIn: { label: '投入批次', visible: true },
  batchOut: { label: '产出批次', visible: true },
  workOrderNo: { label: '工单号', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  opName: { label: '工序', visible: true },
  lineName: { label: '产线', visible: true },
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  qtyUsed: { label: '投入数量', visible: true },
  qtyOut: { label: '产出数量', visible: true },
  supplierName: { label: '供应商', visible: true },
  operator: { label: '操作员', visible: true },
  traceTime: { label: '追溯时间', visible: true },
  breakFlag: { label: '断点', visible: true },
})
function getList() {
  loading.value = true;
  listTrace(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths(); }).catch(() => { loading.value = false; });
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() {
  queryParams.value.batchOut = undefined; queryParams.value.batchIn = undefined;
  queryParams.value.workOrderNo = undefined; queryParams.value.materialCode = undefined;
  queryParams.value.materialName = undefined; queryParams.value.sourceType = undefined;
  queryParams.value.opName = undefined; queryParams.value.lineName = undefined;
  queryParams.value.supplierName = undefined; queryParams.value.operator = undefined;
  queryParams.value.breakFlag = undefined; dateRange.value = [];
  queryParams.value.params = {};
  handleQuery();
}
function handleExport() { proxy.download("mms/trace/export", { ...queryParams.value }, `trace_${new Date().getTime()}.xlsx`); }
function unitLabel(unit) { if (!unit) return '—'; const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : '—'; }
/** 追溯类型标签与配色 */
function sourceLabel(type) {
  const m = { feeding: '领料投料', report: '报工', complete: '完工入库', rework: '返工', manual: '手工补录' };
  return m[type] || type || '—';
}
function sourceClass(type) {
  const m = { feeding: 'violet', report: 'blue', complete: 'green', rework: 'amber', manual: 'gray' };
  return m[type] || 'gray';
}

// ===== 追溯跳转（复用 QMS 追溯查询页更完整的视图：树+检验记录+NCR+降级追溯） =====
/** 通用跳转：按批次号+方向打开 QMS 追溯查询页 */
function jumpTrace(batchNo, direction) {
  if (!batchNo || String(batchNo).startsWith('领料-')) {
    proxy.$modal.msgWarning('该批次号非真实批次（发料未录批次），无法追溯');
    return;
  }
  router.push({ path: '/qms/trace/search', query: { batchNo: String(batchNo), direction } });
}
/** 跳转工单管理页（按工单号定位） */
function jumpWorkOrder(workOrderNo) {
  if (!workOrderNo) return;
  router.push({ path: '/mms/workorder/list', query: { workOrderNo: String(workOrderNo) } });
}
/** 跳转供应商管理页（按供应商名称定位） */
function jumpSupplier(supplierName) {
  if (!supplierName) return;
  router.push({ path: '/wms/supplier', query: { supplierName: String(supplierName) } });
}
function handleTrace(row, direction) {
  // 反向：从产出批次向上追；正向：从投入批次向下追
  const batchNo = direction === 'forward' ? row.batchIn : row.batchOut;
  if (!batchNo || String(batchNo).startsWith('领料-')) {
    proxy.$modal.msgWarning(direction === 'forward' ? '该行投入批次无真实批次号（发料未录批次），无法正向追溯' : '产出批次为空，无法反向追溯');
    return;
  }
  // 跳转质量追溯查询页，带批次号与方向参数，由目标页自动执行追溯
  router.push({ path: '/qms/trace/search', query: { batchNo: String(batchNo), direction } });
}
getList();
</script>
<style scoped>
.mms-trace-page { padding-top: 10px; }
.mms-trace-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-trace-page .filter-card { padding: 14px 20px 16px; }
.mms-trace-page .filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.mms-trace-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-trace-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-trace-page .filter-card .adv-link { font-size: 14px; color: #64748b; text-decoration: none; display: flex; align-items: center; gap: 4px; transition: color .15s; cursor: pointer; }
.mms-trace-page .filter-card .adv-link:hover { color: #4f46e5; }
.mms-trace-page .filter-card .adv-link .chev { transition: transform .2s cubic-bezier(.16,.84,.44,1); }
.mms-trace-page .filter-card .adv-link.is-open .chev { transform: rotate(180deg); }
.mms-trace-page .filter-bar { display: grid; grid-template-columns: repeat(4, minmax(0,1fr)); gap: 12px 16px; }
.mms-trace-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-trace-page .filter-info { font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 6px; }
.mms-trace-page .field .control.is-select :deep(.el-select) { width: 100%; }
.mms-trace-page .field .control.is-select :deep(.el-select__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; min-height: 34px; }
.mms-trace-page .filter-buttons { display: flex; gap: 8px; }
.mms-trace-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-trace-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-trace-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-trace-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-trace-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-trace-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-trace-page .toolbar .left, .mms-trace-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-trace-page .table-wrap { overflow-x: auto; }
/* ===== Status Tabs ===== */
.mms-trace-page .status-tabs { display: flex; align-items: center; gap: 12px; padding: 6px 10px 6px 12px; border-bottom: 1px solid #e2e8f0; background: #fff; }
.mms-trace-page .tabs-track { display: flex; align-items: center; gap: 4px; flex: 1; min-width: 0; overflow-x: auto; scrollbar-width: none; }
.mms-trace-page .tabs-track::-webkit-scrollbar { display: none; }
.mms-trace-page .status-tab { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: 6px; font-size: 14px; color: #64748b; cursor: pointer; user-select: none; transition: all .15s cubic-bezier(.16,.84,.44,1); white-space: nowrap; border: 1px solid transparent; background: transparent; }
.mms-trace-page .status-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: #cbd5e1; }
.mms-trace-page .status-tab .count { font-size: 12px; font-weight: 600; padding: 1px 6px; border-radius: 999px; background: #f1f5f9; color: #64748b; min-width: 18px; text-align: center; line-height: 1.4; font-feature-settings: "tnum" 1; }
.mms-trace-page .status-tab:hover { background: #f8fafc; color: #334155; }
.mms-trace-page .status-tab.is-active { background: #eef2ff; color: #4338ca; font-weight: 600; border-color: #c7d2fe; }
.mms-trace-page .status-tab.is-active .count { background: #4f46e5; color: #fff; }
.mms-trace-page .status-tab.is-active .dot { background: #6366f1; }

/* ===== Tip Pill ===== */
.mms-trace-page .tip-pill { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px; font-size: 13px; font-weight: 500; border-radius: 6px; border: 1px solid #fde68a; background: #fffaf0; color: #92400e; cursor: pointer; white-space: nowrap; flex-shrink: 0; transition: all 0.15s ease; }
.mms-trace-page .tip-pill:hover { background: #fffbeb; border-color: #f59e0b; color: #78350f; }
.mms-trace-page .tip-pill .el-icon { font-size: 14px; }

/* ===== Badge 徽章样式 ===== */
.mms-trace-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.mms-trace-page .badge.violet { background: #eef2ff; color: #4338ca; border-color: #c7d2fe; }
.mms-trace-page .badge.blue { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
.mms-trace-page .badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.mms-trace-page .badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; }
.mms-trace-page .badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.mms-trace-page .badge.gray { background: #f8fafc; color: #475569; border-color: #e2e8f0; }
.mms-trace-page .text-muted { color: #94a3b8; font-size: 13px; }

/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height: 520px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-card-body p { margin: 4px 0; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }
@media (max-width: 900px) { .mms-trace-page .filter-bar { grid-template-columns: 1fr; } }

</style>
