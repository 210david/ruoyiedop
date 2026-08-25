<template>
  <div class="app-container mms-oee-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>产能单元</label><div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>日期范围</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
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
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:oee:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_oee_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="日期" prop="snapshotDate" key="snapshotDate" :width="colWidth('snapshotDate', 120)" resizable align="center" v-if="columns.snapshotDate.visible"><template #default="scope">{{ parseTime(scope.row.snapshotDate, '{y}-{m}-{d}') }}</template></el-table-column>
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 140)" resizable show-overflow-tooltip v-if="columns.resourceName.visible" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="工序" prop="processName" key="processName" :width="colWidth('processName', 120)" resizable show-overflow-tooltip v-if="columns.processName.visible" />
          <el-table-column label="计划工时" prop="plannedHours" key="plannedHours" :width="colWidth('plannedHours', 100)" resizable align="center" v-if="columns.plannedHours.visible" />
          <el-table-column label="运行工时" prop="runHours" key="runHours" :width="colWidth('runHours', 100)" resizable align="center" v-if="columns.runHours.visible" />
          <el-table-column label="停机工时" prop="downtimeHours" key="downtimeHours" :width="colWidth('downtimeHours', 100)" resizable align="center" v-if="columns.downtimeHours.visible" />
          <el-table-column label="可用率" prop="availability" key="availability" :width="colWidth('availability', 90)" resizable align="center" v-if="columns.availability.visible"><template #default="scope">{{ (scope.row.availability * 100).toFixed(2) }}%</template></el-table-column>
          <el-table-column label="表现率" prop="performance" key="performance" :width="colWidth('performance', 90)" resizable align="center" v-if="columns.performance.visible"><template #default="scope">{{ (scope.row.performance * 100).toFixed(2) }}%</template></el-table-column>
          <el-table-column label="质量率" prop="qualityRate" key="qualityRate" :width="colWidth('qualityRate', 90)" resizable align="center" v-if="columns.qualityRate.visible"><template #default="scope">{{ (scope.row.qualityRate * 100).toFixed(2) }}%</template></el-table-column>
          <el-table-column label="OEE" prop="oee" key="oee" :width="colWidth('oee', 90)" resizable align="center" v-if="columns.oee.visible"><template #default="scope"><span class="badge" :class="oeeBadgeClass(scope.row.oee)"><span class="dot"></span>{{ (scope.row.oee * 100).toFixed(2) }}%</span></template></el-table-column>
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="合格数量" prop="goodQty" key="goodQty" :width="colWidth('goodQty', 100)" resizable align="center" v-if="columns.goodQty.visible" />
          <el-table-column label="不良数量" prop="defectQty" key="defectQty" :width="colWidth('defectQty', 100)" resizable align="center" v-if="columns.defectQty.visible" />
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showHelp" title="OEE分析业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、报表释义 -->
        <h4>一、报表释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是OEE分析？</div>
          <div class="highlight-card-body">
            <p><strong>OEE（Overall Equipment Effectiveness，设备综合效率）</strong>是衡量产能单元综合运转效率的核心指标，由可用率、表现率和质量率三项乘积构成。</p>
            <p>OEE 源自<strong>TPM（全员生产维护）</strong>方法论，是国际制造业广泛采用的设备效率评价标准。通过 OEE 分析可以精确定位设备停机损失、性能瓶颈和质量损失。</p>
          </div>
        </div>

        <!-- 二、核心指标定义 -->
        <h4>二、核心指标定义</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">指标计算公式</div>
          <div class="highlight-card-body">
          <p><strong>日期</strong> = 该工序最近一次报工日期；无报工记录则取派工单实际开工日期 <span style="color: #f56c6c;">*系统自动提取</span></p>
          <p><strong>计划工时</strong> = 派工单计划完工时间 − 计划开工时间（小时） <span style="color: #f56c6c;">*系统自动计算</span></p>
          <p><strong>运行工时</strong> = 派工单实际完工 − 实际开工（小时），未完工则显示0（不计算） <span style="color: #f56c6c;">*系统自动计算</span></p>
          <p><strong>停机工时</strong> = 停机记录表（mms_downtime）中该派工单的停机分钟数之和 ÷ 60，无记录则为0 <span style="color: #f56c6c;">*需录入停机记录</span></p>
          <p><strong>可用率（Availability）</strong> = 已完工时 (运行工时 − 停机工时) ÷ 计划工时 × 100%，未完工时为0</p>
          <p><strong>表现率（Performance）</strong> = 合格数量 ÷ 计划数量 × 100%</p>
          <p><strong>质量率（Quality Rate）</strong> = 合格数量 ÷ (合格数量 + 不良数量) × 100%</p>
            <p><strong>OEE</strong> = 可用率 × 表现率 × 质量率 × 100%</p>
          <p><strong>合格数量</strong> = 派工单 good_qty（该工序合格数） <span style="color: #f56c6c;">*系统自动提取</span></p>
          <p><strong>不良数量</strong> = 派工单 defect_qty（该工序不良数） <span style="color: #f56c6c;">*系统自动提取</span></p>
          </div>
        </div>

        <!-- 三、数据来源 -->
        <h4>三、数据来源</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="派工单表（mms_dispatch）">提供工序、产能单元、计划工时、计划数量、合格数量、不良数量等基础数据，每个工序一条OEE记录</el-descriptions-item>
          <el-descriptions-item label="报工表（mms_work_report）">提供该工序最近一次报工日期，用于确定统计日期</el-descriptions-item>
          <el-descriptions-item label="停机记录表（mms_downtime）">提供设备停机时长数据，按派工单关联汇总停机分钟数</el-descriptions-item>
        </el-descriptions>

        <!-- 四、OEE等级判定 -->
        <h4>四、OEE等级判定</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">OEE等级参考标准</div>
          <div class="highlight-card-body">
            <p><strong>≥ 85%</strong>：优秀（绿色徽章）— 设备运转高效，停机和质量损失极小</p>
            <p><strong>60% ~ 85%</strong>：一般（琥珀色徽章）— 存在改善空间，需排查瓶颈环节</p>
            <p><strong>&lt; 60%</strong>：较差（红色徽章）— 设备效率偏低，需重点分析停机和质量原因</p>
          </div>
        </div>

      </div>
      <template #footer><el-button type="primary" @click="showHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup name="Oee">
import { listOee } from "@/api/mms/oee";
import { useColumnResize } from '@/composables/useColumnResize'
import { Filter, RefreshLeft, QuestionFilled } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_oee_index')
const dataList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0); const dateRange = ref([]);
const showHelp = ref(false);
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, resourceName: undefined, workOrderNo: undefined, params: {} } });
const { queryParams } = toRefs(data);
const columns = reactive({
  snapshotDate: { label: '日期', visible: true },
  resourceName: { label: '产能单元', visible: true },
  workOrderNo: { label: '工单编号', visible: true },
  processName: { label: '工序', visible: true },
  plannedHours: { label: '计划工时', visible: true },
  runHours: { label: '运行工时', visible: true },
  downtimeHours: { label: '停机工时', visible: true },
  availability: { label: '可用率', visible: true },
  performance: { label: '表现率', visible: true },
  qualityRate: { label: '质量率', visible: true },
  oee: { label: 'OEE', visible: true },
  planQty: { label: '计划数量', visible: true },
  goodQty: { label: '合格数量', visible: true },
  defectQty: { label: '不良数量', visible: true }
})
function getList() { loading.value = true; listOee(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths(); }).catch(() => { loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.resourceName = undefined; queryParams.value.workOrderNo = undefined; dateRange.value = []; handleQuery(); }
function handleExport() { proxy.download("mms/oee/export", { ...queryParams.value }, `oee_${new Date().getTime()}.xlsx`); }
function oeeBadgeClass(val) { if (val >= 0.85) return 'green'; if (val >= 0.6) return 'amber'; return 'red'; }
getList();
</script>
<style scoped>
.mms-oee-page { padding-top: 10px; }
.mms-oee-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-oee-page .filter-card { padding: 14px 20px 16px; }
.mms-oee-page .filter-head { margin-bottom: 12px; }
.mms-oee-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-oee-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-oee-page .filter-bar { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 12px 16px; }
.mms-oee-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-oee-page .filter-info { font-size: 13px; color: #64748b; }
.mms-oee-page .filter-buttons { display: flex; gap: 8px; }
.mms-oee-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-oee-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-oee-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-oee-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-oee-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-oee-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-oee-page .toolbar .left, .mms-oee-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-oee-page .table-wrap { overflow-x: auto; }
.mms-oee-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.mms-oee-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.mms-oee-page .badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; } .mms-oee-page .badge.green .dot { background: #10b981; }
.mms-oee-page .badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; } .mms-oee-page .badge.amber .dot { background: #f59e0b; }
.mms-oee-page .badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; } .mms-oee-page .badge.red .dot { background: #ef4444; }
/* ===== Status Tabs ===== */
.mms-oee-page .status-tabs { display: flex; align-items: center; gap: 12px; padding: 6px 10px 6px 12px; border-bottom: 1px solid #e2e8f0; background: #fff; }
.mms-oee-page .tabs-track { display: flex; align-items: center; gap: 4px; flex: 1; min-width: 0; overflow-x: auto; scrollbar-width: none; }
.mms-oee-page .tabs-track::-webkit-scrollbar { display: none; }
.mms-oee-page .status-tab { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: 6px; font-size: 14px; color: #64748b; cursor: pointer; user-select: none; transition: all .15s cubic-bezier(.16,.84,.44,1); white-space: nowrap; border: 1px solid transparent; background: transparent; }
.mms-oee-page .status-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: #cbd5e1; }
.mms-oee-page .status-tab .count { font-size: 12px; font-weight: 600; padding: 1px 6px; border-radius: 999px; background: #f1f5f9; color: #64748b; min-width: 18px; text-align: center; line-height: 1.4; font-feature-settings: "tnum" 1; }
.mms-oee-page .status-tab:hover { background: #f8fafc; color: #334155; }
.mms-oee-page .status-tab.is-active { background: #eef2ff; color: #4338ca; font-weight: 600; border-color: #c7d2fe; }
.mms-oee-page .status-tab.is-active .count { background: #4f46e5; color: #fff; }
.mms-oee-page .status-tab.is-active .dot { background: #6366f1; }

/* ===== Tip Pill ===== */
.mms-oee-page .tip-pill { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px; font-size: 13px; font-weight: 500; border-radius: 6px; border: 1px solid #fde68a; background: #fffaf0; color: #92400e; cursor: pointer; white-space: nowrap; flex-shrink: 0; transition: all 0.15s ease; }
.mms-oee-page .tip-pill:hover { background: #fffbeb; border-color: #f59e0b; color: #78350f; }
.mms-oee-page .tip-pill .el-icon { font-size: 14px; }

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
</style>
