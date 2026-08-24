<template>
  <div class="app-container mms-trace-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>产出批次</label><div class="control"><el-input v-model="queryParams.batchOut" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>投入批次</label><div class="control"><el-input v-model="queryParams.batchIn" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>物料编码</label><div class="control"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
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
          <el-table-column type="index" label="序号" width="55" align="center" />
          <el-table-column label="产出批次" prop="batchOut" key="batchOut" :width="colWidth('batchOut', 160)" resizable v-if="columns.batchOut.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 180)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="投入批次" prop="batchIn" key="batchIn" :width="colWidth('batchIn', 160)" resizable v-if="columns.batchIn.visible" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 140)" resizable v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 160)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 160)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><span v-if="scope.row.unit" class="badge violet">{{ unitLabel(scope.row.unit) }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="使用数量" prop="qtyUsed" key="qtyUsed" :width="colWidth('qtyUsed', 120)" resizable align="center" v-if="columns.qtyUsed.visible" />
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
import { useColumnResize } from '@/composables/useColumnResize'
import { Filter, RefreshLeft, QuestionFilled } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const { wms_unit } = proxy.useDict("wms_unit")
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_trace_index')
const dataList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0);
const showHelp = ref(false);
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, batchOut: undefined, batchIn: undefined, materialCode: undefined, params: {} } });
const { queryParams } = toRefs(data);
const columns = reactive({
  batchOut: { label: '产出批次', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  batchIn: { label: '投入批次', visible: true },
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  qtyUsed: { label: '使用数量', visible: true },
})
function getList() { loading.value = true; listTrace(queryParams.value).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths(); }).catch(() => { loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.batchOut = undefined; queryParams.value.batchIn = undefined; queryParams.value.materialCode = undefined; handleQuery(); }
function handleExport() { proxy.download("mms/trace/export", { ...queryParams.value }, `trace_${new Date().getTime()}.xlsx`); }
function unitLabel(unit) { if (!unit) return '—'; const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : '—'; }
getList();
</script>
<style scoped>
.mms-trace-page { padding-top: 10px; }
.mms-trace-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-trace-page .filter-card { padding: 14px 20px 16px; }
.mms-trace-page .filter-head { margin-bottom: 12px; }
.mms-trace-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-trace-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-trace-page .filter-bar { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 12px 16px; }
.mms-trace-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-trace-page .filter-info { font-size: 13px; color: #64748b; }
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
