<template>
  <div class="app-container mms-dashboard-page">
    <div class="page-header">
      <h2>生产运营看板</h2>
      <el-button icon="Refresh" @click="loadData">刷新数据</el-button>
    </div>

    <!-- ===== 顶部统计卡片 ===== -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-workorder">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div class="stat-body">
            <div class="stat-label">工单总数</div>
            <div class="stat-value">{{ overviewData.workOrder?.total || 0 }}</div>
            <div class="stat-detail">
              <span class="tag tag-primary">生产中 {{ overviewData.workOrder?.inProduction || 0 }}</span>
              <span class="tag tag-success">已完工 {{ overviewData.workOrder?.completed || 0 }}</span>
              <span class="tag tag-warning">异常 {{ overviewData.workOrder?.abnormal || 0 }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-mps">
          <div class="stat-icon"><el-icon><Calendar /></el-icon></div>
          <div class="stat-body">
            <div class="stat-label">计划总数</div>
            <div class="stat-value">{{ overviewData.mps?.total || 0 }}</div>
            <div class="stat-detail">
              <span class="tag tag-default">草稿 {{ overviewData.mps?.draft || 0 }}</span>
              <span class="tag tag-info">已发布 {{ overviewData.mps?.released || 0 }}</span>
              <span class="tag tag-success">已下达 {{ overviewData.mps?.executed || 0 }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-abnormal">
          <div class="stat-icon"><el-icon><Warning /></el-icon></div>
          <div class="stat-body">
            <div class="stat-label">异常总数</div>
            <div class="stat-value">{{ overviewData.abnormal?.total || 0 }}</div>
            <div class="stat-detail">
              <span class="tag tag-danger">待响应 {{ overviewData.abnormal?.pending || 0 }}</span>
              <span class="tag tag-warning">处理中 {{ overviewData.abnormal?.processing || 0 }}</span>
              <span class="tag tag-success">已关闭 {{ overviewData.abnormal?.closed || 0 }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-report">
          <div class="stat-icon"><el-icon><Tickets /></el-icon></div>
          <div class="stat-body">
            <div class="stat-label">报工总数</div>
            <div class="stat-value">{{ overviewData.report?.total || 0 }}</div>
            <div class="stat-detail">
              <span class="tag tag-warning">待审核 {{ overviewData.report?.pending || 0 }}</span>
              <span class="tag tag-success">已审核 {{ overviewData.report?.approved || 0 }}</span>
              <span class="tag tag-danger">已驳回 {{ overviewData.report?.rejected || 0 }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 中间区域 ===== -->
    <el-row :gutter="16">
      <!-- 工单状态分布 -->
      <el-col :xs="24" :sm="12" :lg="12">
        <div class="surface chart-card">
          <div class="chart-head">
            <span class="chart-title">工单状态分布</span>
          </div>
          <div class="chart-body">
            <div class="bar-chart">
              <div v-for="item in workOrderBars" :key="item.label" class="bar-item">
                <div class="bar-label">{{ item.label }}</div>
                <div class="bar-track">
                  <div class="bar-fill" :class="item.class" :style="{ width: item.percent + '%' }"></div>
                </div>
                <div class="bar-value">{{ item.count }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 异常分类统计 -->
      <el-col :xs="24" :sm="12" :lg="12">
        <div class="surface chart-card">
          <div class="chart-head">
            <span class="chart-title">异常处理趋势</span>
          </div>
          <div class="chart-body">
            <div class="donut-chart">
              <div class="donut-center">
                <div class="donut-total">{{ overviewData.abnormal?.total || 0 }}</div>
                <div class="donut-label">总异常</div>
              </div>
              <div class="donut-ring">
                <svg viewBox="0 0 120 120" class="donut-svg">
                  <circle cx="60" cy="60" r="50" fill="none" stroke="#f0f0f0" stroke-width="14" />
                  <circle v-if="abnormalDonut.pending > 0" cx="60" cy="60" r="50" fill="none" stroke="#f56c6c" stroke-width="14"
                    :stroke-dasharray="abnormalDonut.pendingArc" stroke-dashoffset="0" transform="rotate(-90 60 60)" />
                  <circle v-if="abnormalDonut.processing > 0" cx="60" cy="60" r="50" fill="none" stroke="#e6a23c" stroke-width="14"
                    :stroke-dasharray="abnormalDonut.processingArc" :stroke-dashoffset="abnormalDonut.processingOffset" transform="rotate(-90 60 60)" />
                  <circle v-if="abnormalDonut.closed > 0" cx="60" cy="60" r="50" fill="none" stroke="#67c23a" stroke-width="14"
                    :stroke-dasharray="abnormalDonut.closedArc" :stroke-dashoffset="abnormalDonut.closedOffset" transform="rotate(-90 60 60)" />
                </svg>
              </div>
            </div>
            <div class="donut-legend">
              <div class="legend-item"><span class="dot dot-danger"></span> 待响应 {{ overviewData.abnormal?.pending || 0 }}</div>
              <div class="legend-item"><span class="dot dot-warning"></span> 处理中 {{ overviewData.abnormal?.processing || 0 }}</div>
              <div class="legend-item"><span class="dot dot-success"></span> 已关闭 {{ overviewData.abnormal?.closed || 0 }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 底部列表 ===== -->
    <el-row :gutter="16">
      <el-col :span="24">
        <div class="surface list-card">
          <div class="chart-head">
            <span class="chart-title">最新工单</span>
          </div>
          <el-table :data="latestOrders" border stripe size="small">
            <el-table-column label="工单编号" align="center" prop="workOrderNo" width="140" />
            <el-table-column label="产品名称" align="center" prop="productName" min-width="120" show-overflow-tooltip />
            <el-table-column label="计划数量" align="center" prop="planQty" width="100" />
            <el-table-column label="完工数量" align="center" prop="finishedQty" width="100" />
            <el-table-column label="状态" align="center" prop="status" width="100">
              <template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
              <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="MmsDashboard">
import { getOverview } from "@/api/mms/dashboard";
import { listWorkOrder } from "@/api/mms/workorder";
import { WarningFilled, Timer } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_workorder_status } = proxy.useDict("mms_workorder_status");

const overviewData = ref({});
const latestOrders = ref([]);

const workOrderBars = computed(() => {
  const wo = overviewData.value.workOrder || {};
  const total = wo.total || 1;
  return [
    { label: "新建", count: 0, percent: 0, class: "bar-default" },
    { label: "已下达", count: 0, percent: 0, class: "bar-info" },
    { label: "执行中", count: wo.inProduction || 0, percent: Math.round((wo.inProduction || 0) / total * 100), class: "bar-primary" },
    { label: "已完工", count: wo.completed || 0, percent: Math.round((wo.completed || 0) / total * 100), class: "bar-success" },
    { label: "异常", count: wo.abnormal || 0, percent: Math.round((wo.abnormal || 0) / total * 100), class: "bar-warning" }
  ];
});

const abnormalDonut = computed(() => {
  const ab = overviewData.value.abnormal || {};
  const total = ab.total || 1;
  const circumference = 2 * Math.PI * 50; // ~314
  const pending = ab.pending || 0;
  const processing = ab.processing || 0;
  const closed = ab.closed || 0;

  const pendingArc = (pending / total) * circumference;
  const processingArc = (processing / total) * circumference;
  const closedArc = (closed / total) * circumference;

  return {
    pending, processing, closed,
    pendingArc: `${pendingArc} ${circumference - pendingArc}`,
    processingArc: `${processingArc} ${circumference - processingArc}`,
    processingOffset: -pendingArc,
    closedArc: `${closedArc} ${circumference - closedArc}`,
    closedOffset: -(pendingArc + processingArc)
  };
});

function loadData() {
  getOverview().then(response => {
    overviewData.value = response.data || {};
  });
  // 加载最新工单
  listWorkOrder({ pageNum: 1, pageSize: 5 }).then(response => {
    latestOrders.value = response.rows || [];
  });
}

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.mms-dashboard-page {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
    h2 { font-size: 20px; font-weight: 600; color: #1f2329; margin: 0; }
  }

  .stat-row { margin-bottom: 16px; }
  @keyframes pulse-alert { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }
  .stat-card { background: #fff; border-radius: 10px; padding: 20px; display: flex; gap: 16px; align-items: flex-start; box-shadow: 0 1px 4px rgba(0,0,0,0.04); transition: all 0.3s;
    &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
    .stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; flex-shrink: 0; }
    &.stat-workorder .stat-icon { background: linear-gradient(135deg, #3370ff, #00ceb8); }
    &.stat-mps .stat-icon { background: linear-gradient(135deg, #722ed1, #2f54eb); }
    &.stat-abnormal .stat-icon { background: linear-gradient(135deg, #fa5c7c, #ff8e53); }
    &.stat-abnormal-alert .stat-icon { background: linear-gradient(135deg, #f5385b, #ff6b6b); }
    &.stat-downtime .stat-icon { background: linear-gradient(135deg, #ff9f43, #ee5a24); }
    &.stat-report .stat-icon { background: linear-gradient(135deg, #52c41a, #95de64); }
    .stat-body { flex: 1; }
    .stat-label { font-size: 13px; color: #8f959e; margin-bottom: 4px; }
    .stat-value { font-size: 28px; font-weight: 700; color: #1f2329; margin-bottom: 8px; }
    .stat-value.value-alert { color: #f5385b; animation: pulse-alert 2s ease-in-out infinite; }
    .stat-unit { font-size: 13px; font-weight: 500; color: #8f959e; }
    .stat-detail { display: flex; flex-wrap: wrap; gap: 4px;
      .tag { font-size: 11px; padding: 2px 8px; border-radius: 10px; display: inline-block;
        &.tag-default { background: #f5f6f7; color: #8f959e; }
        &.tag-info { background: #f0f5ff; color: #2f54eb; }
        &.tag-primary { background: #e8f0ff; color: #3370ff; }
        &.tag-success { background: #f0f9ec; color: #52c41a; }
        &.tag-warning { background: #fdf6ec; color: #e6a23c; }
        &.tag-danger { background: #fef0f0; color: #f56c6c; }
      }
    }
  }

  .surface { background: #fff; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); margin-bottom: 16px; overflow: hidden; }
  .chart-card { padding: 20px;
    .chart-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
      .chart-title { font-size: 15px; font-weight: 600; color: #1f2329; }
    }
  }

  .bar-chart { display: flex; flex-direction: column; gap: 12px;
    .bar-item { display: flex; align-items: center; gap: 12px;
      .bar-label { font-size: 13px; color: #646a73; width: 60px; text-align: right; }
      .bar-track { flex: 1; height: 24px; background: #f5f6f7; border-radius: 12px; overflow: hidden; }
      .bar-fill { height: 100%; border-radius: 12px; transition: width 0.6s ease;
        &.bar-default { background: #c9cdd4; }
        &.bar-info { background: #909399; }
        &.bar-primary { background: #409eff; }
        &.bar-success { background: #67c23a; }
        &.bar-warning { background: #e6a23c; }
      }
      .bar-value { font-size: 13px; color: #1f2329; font-weight: 600; width: 30px; }
    }
  }

  .donut-chart { display: flex; align-items: center; justify-content: center; position: relative; margin-bottom: 12px;
    .donut-svg { width: 160px; height: 160px; }
    .donut-center { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); text-align: center;
      .donut-total { font-size: 24px; font-weight: 700; color: #1f2329; }
      .donut-label { font-size: 12px; color: #8f959e; }
    }
  }
  .donut-legend { display: flex; justify-content: center; gap: 16px;
    .legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #646a73;
      .dot { width: 8px; height: 8px; border-radius: 50%; }
      .dot-danger { background: #f56c6c; } .dot-warning { background: #e6a23c; } .dot-success { background: #67c23a; }
    }
  }

  .list-card { padding: 20px;
    .chart-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
      .chart-title { font-size: 15px; font-weight: 600; color: #1f2329; }
    }
  }
}
</style>
