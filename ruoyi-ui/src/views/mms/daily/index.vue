<template>
  <div class="app-container mms-daily-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>日期范围</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 支持日期范围筛选</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
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
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:daily:export']">导出</el-button>
          <el-button type="success" plain icon="MagicStick" @click="handleBatchDailyReport" v-hasPermi="['mms:ai:dailyReport']">AI生成日报</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_daily_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="onSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="50" align="center" fixed="left" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
          <el-table-column label="生产日期" prop="statDate" key="statDate" :width="colWidth('statDate', 120)" resizable align="center" v-if="columns.statDate.visible"><template #default="scope">{{ parseTime(scope.row.statDate, '{y}-{m}-{d}') }}</template></el-table-column>
          <el-table-column label="工单数" prop="workOrderCount" key="workOrderCount" :width="colWidth('workOrderCount', 100)" resizable align="center" v-if="columns.workOrderCount.visible" />
          <el-table-column label="已完工数" prop="completedCount" key="completedCount" :width="colWidth('completedCount', 100)" resizable align="center" v-if="columns.completedCount.visible" />
          <el-table-column label="在制数" prop="inProgressCount" key="inProgressCount" :width="colWidth('inProgressCount', 100)" resizable align="center" v-if="columns.inProgressCount.visible" />
          <el-table-column label="异常数" prop="abnormalCount" key="abnormalCount" :width="colWidth('abnormalCount', 100)" resizable align="center" v-if="columns.abnormalCount.visible">
            <template #default="scope"><span v-if="scope.row.abnormalCount > 0" class="badge red"><span class="dot"></span>{{ scope.row.abnormalCount }}</span><span v-else>{{ scope.row.abnormalCount }}</span></template>
          </el-table-column>
          <el-table-column label="计划数量" prop="totalPlanQty" key="totalPlanQty" :width="colWidth('totalPlanQty', 120)" resizable align="center" v-if="columns.totalPlanQty.visible" />
          <el-table-column label="合格数量" prop="totalGoodQty" key="totalGoodQty" :width="colWidth('totalGoodQty', 120)" resizable align="center" v-if="columns.totalGoodQty.visible" />
          <el-table-column label="不良数量" prop="totalDefectQty" key="totalDefectQty" :width="colWidth('totalDefectQty', 120)" resizable align="center" v-if="columns.totalDefectQty.visible" />
          <el-table-column label="报工次数" prop="reportCount" key="reportCount" :width="colWidth('reportCount', 100)" resizable align="center" v-if="columns.reportCount.visible" />
          <el-table-column label="AI日报" width="110" align="center" fixed="right">
            <template #default="scope">
              <AiAssistButton v-hasPermi="['mms:ai:dailyReport']" button-text="生成" size="small" button-type="primary" :api="aiDailyReport" :params="[scope.row.statDate ? parseTime(scope.row.statDate, '{y}-{m}-{d}') : null]" @result="d => onDailyReport(d, scope.row)" />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <AiResultPanel :visible="dailyReportVisible" :content="dailyReportText" :latency-ms="dailyReportLatency" @close="dailyReportVisible = false" />
      <!-- 批量生成进度面板 -->
      <div v-if="batchItems.length" class="ai-batch-panel">
        <div class="ai-batch-head">
          <span class="ai-batch-title">AI 批量生成日报（{{ batchItems.filter(i => i.status !== 'loading').length }}/{{ batchItems.length }}）</span>
          <span v-if="batchElapsed" class="ai-batch-elapsed">已等待 {{ batchElapsed }} 秒</span>
          <el-button link type="primary" size="small" @click="batchItems = []; batchTimer && clearInterval(batchTimer); batchTimer = null">关闭</el-button>
        </div>
        <div v-for="(item, i) in batchItems" :key="i" class="ai-batch-item" :class="'is-' + item.status">
          <div class="ai-batch-item-head">
            <span class="ai-batch-date">{{ item.date }} 生产日报</span>
            <el-tag v-if="item.status === 'loading'" size="small" type="info">生成中…</el-tag>
            <el-tag v-else-if="item.status === 'done'" size="small" type="success">完成</el-tag>
            <template v-else>
              <el-tag size="small" type="danger">失败</el-tag>
              <el-button link type="primary" size="small" icon="RefreshRight" @click="retryBatchItem(item)">重试</el-button>
            </template>
          </div>
          <pre v-if="item.content" class="ai-batch-content">{{ item.content }}</pre>
        </div>
        <div class="ai-batch-foot">AI 结果仅供参考，请确认后使用</div>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showHelp" title="生产日报业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、报表释义 -->
        <h4>一、报表释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是生产日报？</div>
          <div class="highlight-card-body">
            <p><strong>生产日报</strong>是按日汇总生产工单开工、完工、异常及产出情况的生产管控报表。</p>
            <p>通过每日汇总工单开工数量、完工进度、异常情况和产出数据，帮助生产管理人员掌握每日生产节奏，及时发现生产异常并调整生产计划。</p>
          </div>
        </div>

        <!-- 二、核心指标定义 -->
        <h4>二、核心指标定义</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">指标计算公式</div>
          <div class="highlight-card-body">
            <p><strong>生产日期</strong> = 工单实际开工日期（actual_start 的日期部分） <span style="color: #f56c6c;">*系统自动提取</span></p>
            <p><strong>工单数</strong> = 当日开工的工单总数 <span style="color: #f56c6c;">*系统自动统计</span></p>
            <p><strong>已完工数</strong> = 当日 actual_finish 不为空的工单数 <span style="color: #f56c6c;">*系统自动统计</span></p>
            <p><strong>在制数</strong> = 当日已开工但未完工的工单数 = 工单数 - 已完工数 <span style="color: #f56c6c;">*系统自动计算</span></p>
            <p><strong>异常数</strong> = 当日工单中 defect_qty（不良数）大于 0 的工单数 <span style="color: #f56c6c;">*系统自动统计</span></p>
            <p><strong>计划数量</strong> = 当日所有工单的计划数量合计 <span style="color: #f56c6c;">*系统自动汇总</span></p>
            <p><strong>合格数量</strong> = 当日工单合格数量合计（qualified_qty） <span style="color: #f56c6c;">*系统自动汇总</span></p>
            <p><strong>不良数量</strong> = 当日工单不良数量合计 <span style="color: #f56c6c;">*系统自动汇总</span></p>
            <p><strong>报工次数</strong> = 当日关联工单的报工记录总数 <span style="color: #f56c6c;">*系统自动统计</span></p>
          </div>
        </div>

        <!-- 三、数据来源 -->
        <h4>三、数据来源</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单表（mms_work_order）">提供工单开工日期、完工日期、计划数量、合格数量、不良数量等基础数据</el-descriptions-item>
          <el-descriptions-item label="报工表（mms_work_report）">提供实际报工记录，用于合格数量和报工次数统计</el-descriptions-item>
        </el-descriptions>

      </div>
      <template #footer><el-button type="primary" @click="showHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup name="Daily">
import { listDaily } from "@/api/mms/daily";
import { aiDailyReport } from '@/api/ai/assist'
import { parseTime } from '@/utils/ruoyi'
import AiAssistButton from '@/components/AiAssist/AiAssistButton.vue'
import AiResultPanel from '@/components/AiAssist/AiResultPanel.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { Filter, RefreshLeft, QuestionFilled } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_daily_index')
const dataList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0); const dateRange = ref([]);
const showHelp = ref(false);
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, params: {} } });
const { queryParams } = toRefs(data);
const columns = reactive({
  statDate: { label: '生产日期', visible: true },
  workOrderCount: { label: '工单数', visible: true },
  completedCount: { label: '已完工数', visible: true },
  inProgressCount: { label: '在制数', visible: true },
  abnormalCount: { label: '异常数', visible: true },
  totalPlanQty: { label: '计划数量', visible: true },
  totalGoodQty: { label: '合格数量', visible: true },
  totalDefectQty: { label: '不良数量', visible: true },
  reportCount: { label: '报工次数', visible: true }
})
function getList() { loading.value = true; listDaily(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths(); }).catch(() => { loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { dateRange.value = []; handleQuery(); }
function handleExport() { proxy.download("mms/daily/export", { ...queryParams.value }, `daily_${new Date().getTime()}.xlsx`); }

// ===== AI生成日报（外挂式增量） =====
const dailyReportVisible = ref(false)
const dailyReportText = ref('')
const dailyReportLatency = ref(0)
const selectedRows = ref([])

function onSelectionChange(rows) { selectedRows.value = rows }

function rowDate(row) { return row.statDate ? parseTime(row.statDate, '{y}-{m}-{d}') : null }

// 批量生成：为勾选的每一天并行调用AI，结果按日期分段汇总到一个面板（实时进度+超时保护）
const batchItems = ref([])
const batchElapsed = ref(0)
let batchTimer = null

function handleBatchDailyReport() {
  const rows = selectedRows.value
  if (!rows.length) { proxy.$modal.msgWarning('请先勾选要生成日报的日期行（可全选）'); return }
  if (batchTimer) { proxy.$modal.msgWarning('正在生成中，请等待完成或关闭面板'); return }
  batchItems.value = rows.map(r => ({ date: rowDate(r) || ('第' + (r.statDate || '?') + '天'), status: 'loading', content: '' }))
  const items = batchItems.value
  dailyReportVisible.value = true
  dailyReportLatency.value = 0
  const startAt = Date.now()
  batchTimer = setInterval(() => { batchElapsed.value = Math.round((Date.now() - startAt) / 1000) }, 1000)
  let done = 0, failed = 0
  items.forEach((item, idx) => {
    const d = rowDate(rows[idx])
    if (!d) { item.status = 'failed'; item.content = '日期缺失，无法生成'; done++; failed++; checkDone(); return }
    // 单日超过75秒视为超时失败，避免无限挂起
    const timeout = setTimeout(() => {
      if (item.status === 'loading') { item.status = 'failed'; item.content = '生成超时（75秒无响应），请点击重试'; done++; failed++; checkDone() }
    }, 75000)
    aiDailyReport(d).then(res => {
      if (item.status !== 'loading') return
      const d0 = res.data || res
      item.content = d0.content || d0.error || 'AI未返回内容'
      item.status = (d0.success === false) ? 'failed' : 'done'
      if (d0.latencyMs) dailyReportLatency.value += d0.latencyMs
      if (item.status === 'failed') failed++
      done++
    }).catch(() => {
      if (item.status !== 'loading') return
      item.status = 'failed'
      item.content = '生成失败（网络/服务异常），请重试'
      failed++; done++
    }).finally(() => { clearTimeout(timeout); checkDone() })
  })
  function checkDone() {
    if (done < items.length) return
    clearInterval(batchTimer); batchTimer = null
    if (failed) proxy.$modal.msgWarning(failed + ' 天日报生成失败，可在面板中查看原因后重试')
    else proxy.$modal.msgSuccess('已生成 ' + items.length + ' 天生产日报，耗时 ' + batchElapsed.value + ' 秒')
  }
}

function retryBatchItem(item) {
  if (batchTimer) { proxy.$modal.msgWarning('正在生成中，请稍候'); return }
  if (!item.date || item.date.startsWith('第')) { proxy.$modal.msgError('该行日期缺失，无法重试'); return }
  item.status = 'loading'; item.content = ''
  dailyReportVisible.value = true
  const startAt = Date.now()
  batchTimer = setInterval(() => { batchElapsed.value = Math.round((Date.now() - startAt) / 1000) }, 1000)
  const timeout = setTimeout(() => {
    if (item.status === 'loading') { item.status = 'failed'; item.content = '生成超时（75秒无响应）'; clearInterval(batchTimer); batchTimer = null }
  }, 75000)
  aiDailyReport(item.date).then(res => {
    const d0 = res.data || res
    item.content = d0.content || d0.error || 'AI未返回内容'
    item.status = (d0.success === false) ? 'failed' : 'done'
  }).catch(() => { item.status = 'failed'; item.content = '生成失败（网络/服务异常）' }).finally(() => {
    clearTimeout(timeout); clearInterval(batchTimer); batchTimer = null
    item.status === 'done' ? proxy.$modal.msgSuccess(item.date + ' 日报生成成功') : proxy.$modal.msgError(item.date + ' 日报生成失败')
  })
}

function onDailyReport(d, row) {
  dailyReportText.value = (row ? '【' + rowDate(row) + ' 生产日报】\n\n' : '') + (d.content || d.error || 'AI未返回内容')
  dailyReportLatency.value = d.latencyMs || 0
  dailyReportVisible.value = true
}
getList();
</script>
<style scoped>
.mms-daily-page { padding-top: 10px; }
.mms-daily-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-daily-page .filter-card { padding: 14px 20px 16px; }
.mms-daily-page .filter-head { margin-bottom: 12px; }
.mms-daily-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-daily-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-daily-page .filter-bar { display: grid; grid-template-columns: 1fr; gap: 12px 16px; max-width: 400px; }
.mms-daily-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-daily-page .filter-info { font-size: 13px; color: #64748b; }
.mms-daily-page .filter-buttons { display: flex; gap: 8px; }
.mms-daily-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-daily-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-daily-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-daily-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-daily-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-daily-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-daily-page .toolbar .left, .mms-daily-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-daily-page .table-wrap { overflow-x: auto; }
.mms-daily-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.mms-daily-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.mms-daily-page .badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; } .mms-daily-page .badge.red .dot { background: #ef4444; }
/* ===== Status Tabs ===== */
.mms-daily-page .status-tabs { display: flex; align-items: center; gap: 12px; padding: 6px 10px 6px 12px; border-bottom: 1px solid #e2e8f0; background: #fff; }
.mms-daily-page .tabs-track { display: flex; align-items: center; gap: 4px; flex: 1; min-width: 0; overflow-x: auto; scrollbar-width: none; }
.mms-daily-page .tabs-track::-webkit-scrollbar { display: none; }
.mms-daily-page .status-tab { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: 6px; font-size: 14px; color: #64748b; cursor: pointer; user-select: none; transition: all .15s cubic-bezier(.16,.84,.44,1); white-space: nowrap; border: 1px solid transparent; background: transparent; }
.mms-daily-page .status-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: #cbd5e1; }
.mms-daily-page .status-tab .count { font-size: 12px; font-weight: 600; padding: 1px 6px; border-radius: 999px; background: #f1f5f9; color: #64748b; min-width: 18px; text-align: center; line-height: 1.4; font-feature-settings: "tnum" 1; }
.mms-daily-page .status-tab:hover { background: #f8fafc; color: #334155; }
.mms-daily-page .status-tab.is-active { background: #eef2ff; color: #4338ca; font-weight: 600; border-color: #c7d2fe; }
.mms-daily-page .status-tab.is-active .count { background: #4f46e5; color: #fff; }
.mms-daily-page .status-tab.is-active .dot { background: #6366f1; }

/* ===== Tip Pill ===== */
.mms-daily-page .tip-pill { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px; font-size: 13px; font-weight: 500; border-radius: 6px; border: 1px solid #fde68a; background: #fffaf0; color: #92400e; cursor: pointer; white-space: nowrap; flex-shrink: 0; transition: all 0.15s ease; }
.mms-daily-page .tip-pill:hover { background: #fffbeb; border-color: #f59e0b; color: #78350f; }
.mms-daily-page .tip-pill .el-icon { font-size: 14px; }

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

/* 批量AI日报面板 */
.ai-batch-panel { margin: 0 12px 12px; border: 1px solid #d9ecff; background: #f4f9ff; border-radius: 10px; padding: 10px 14px; }
.ai-batch-head { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.ai-batch-title { font-weight: 600; color: #1d39c4; font-size: 14px; }
.ai-batch-elapsed { color: #909399; font-size: 12px; }
.ai-batch-item { background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 8px 12px; margin-bottom: 8px; }
.ai-batch-item.is-failed { border-color: #fbc4c4; background: #fffaf9; }
.ai-batch-item-head { display: flex; align-items: center; gap: 8px; }
.ai-batch-date { font-weight: 600; font-size: 13px; color: #303133; }
.ai-batch-content { margin: 6px 0 0; white-space: pre-wrap; word-break: break-word; font-family: inherit; font-size: 13px; line-height: 1.7; color: #303133; max-height: 320px; overflow: auto; }
.ai-batch-foot { color: #909399; font-size: 12px; margin-top: 4px; }
</style>
