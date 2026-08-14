<template>
  <div class="app-container qms-trace-dashboard-page">
    <!-- ===== 完整度看板卡片 ===== -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card">
          <div class="stat-card__label">已关联工单</div>
          <div class="stat-card__value">{{ data.linkedOrders || 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card">
          <div class="stat-card__label">断点工单</div>
          <div class="stat-card__value stat-card__value--red">{{ data.breakOrders || 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card">
          <div class="stat-card__label">谱系完整率 <span class="stat-card__target">目标 ≥ 98%</span></div>
          <div class="stat-card__value" :class="(data.completenessRate || 0) >= 98 ? 'stat-card__value--green' : 'stat-card__value--orange'">{{ data.completenessRate || 0 }}%</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card">
          <div class="stat-card__label">目标差距</div>
          <div class="stat-card__value" :class="(data.gap || 0) <= 0 ? 'stat-card__value--green' : 'stat-card__value--red'">{{ data.gap || 0 }}%</div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 断点清单 ===== -->
    <div class="surface">
      <div class="section-head">
        <div class="section-title"><span class="glyph"></span> 断点清单</div>
        <button type="button" class="tip-pill" @click="showHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务说明</span>
        </button>
      </div>
      <div class="table-wrap">
        <el-table :data="breakList" border size="small">
          <el-table-column label="投入批次" prop="parentBatchNo" min-width="130" />
          <el-table-column label="工单号" prop="workOrderNo" min-width="110" />
          <el-table-column label="物料" min-width="150"><template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template></el-table-column>
          <el-table-column label="断点原因" prop="breakReason" min-width="200" />
          <el-table-column label="创建时间" prop="createTime" min-width="145" />
        </el-table>
        <el-empty v-if="!breakList.length" description="无断点数据" />
      </div>
    </div>

    <!-- ===== 业务说明弹窗 ===== -->
    <el-dialog v-model="showHelp" title="谱系完整度看板业务说明" width="780px" append-to-body>
      <div class="help-content">
        <h4>一、谱系完整度概述</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">什么是谱系完整度</div>
              <div class="highlight-card-body">谱系完整度衡量<strong>批次谱系数据</strong>的完善程度，即投入批次↔工单↔产出批次的关联是否完整建立。完整度越高，质量追溯能力越强</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">为什么重要</div>
              <div class="highlight-card-body">谱系完整度是质量追溯的基础。完整率 ≥ 98% 才能保证<strong>正向/反向追溯</strong>的可靠性，满足食品 HACCP、汽车 IATF16949 等监管合规要求</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">谱系断点</div>
              <div class="highlight-card-body">投入或产出批次未建立关联时标记为<strong>断点</strong>，导致追溯链中断。断点工单数直接影响完整率</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title">数据治理目标</div>
              <div class="highlight-card-body">谱系完整率纳入质量看板，目标值 <strong>≥ 98%</strong>。未达标时需通过补录断点工单的产出批次信息来提升完整率</div>
            </div>
          </el-col>
        </el-row>

        <h4>二、看板指标说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="已关联工单">谱系表中已建立完整<strong>投入↔产出关联</strong>的工单数量（断点标记=0）。该数值越高，说明谱系数据越完善</el-descriptions-item>
          <el-descriptions-item label="断点工单">存在谱系断点（产出批次未登记）的工单数量（断点标记=1）。该数值应尽可能低，理想状态为 0</el-descriptions-item>
          <el-descriptions-item label="谱系完整率">完整率 = 已关联工单 ÷ (已关联工单 + 断点工单) × 100%。目标值 <strong>≥ 98%</strong>，未达标时以橙色标示，达标时以绿色标示</el-descriptions-item>
          <el-descriptions-item label="目标差距">当前完整率与目标值(98%)的差距。负值表示已达标（绿色），正值表示未达标（红色），差距越大说明需要治理的断点越多</el-descriptions-item>
        </el-descriptions>

        <h4>三、断点清单说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="投入批次">谱系记录中的上游批次号（原料/半成品），是追溯链的起点</el-descriptions-item>
          <el-descriptions-item label="工单号">关联的生产工单编号，用于定位具体生产任务</el-descriptions-item>
          <el-descriptions-item label="物料">断点涉及的产品编码与名称，帮助快速识别物料类型</el-descriptions-item>
          <el-descriptions-item label="断点原因">产出批次未登记的具体原因说明，便于针对性修复</el-descriptions-item>
          <el-descriptions-item label="创建时间">谱系记录的创建时间，用于判断断点存在时长，优先处理长期未修复的断点</el-descriptions-item>
        </el-descriptions>

        <h4>四、数据治理流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>查看看板：</strong>通过完整率、断点工单数等指标，评估当前谱系数据健康度
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>识别断点：</strong>在断点清单中查看所有断点记录，按创建时间或物料优先级排序，确定需优先处理的断点
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>补录修复：</strong>前往「谱系管理」页面，对断点工单补充产出批次信息，修复后完整率将自动提升
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>持续监控：</strong>定期查看完整度看板，确保完整率持续 ≥ 98%，满足监管审计要求
          </el-timeline-item>
        </el-timeline>

        <h4>五、专业词汇说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="谱系(Genealogy)"><strong>物料批次间的投入-产出关联关系链</strong>，是实现质量追溯的基础数据结构</el-descriptions-item>
          <el-descriptions-item label="正向追溯">由原料/半成品批次向下追查至成品、订单、客户的过程</el-descriptions-item>
          <el-descriptions-item label="反向追溯">由成品批次/客诉向上追查至原料、供应商、工序、检验记录的过程</el-descriptions-item>
          <el-descriptions-item label="断点">投入或产出批次未建立关联，导致追溯链中断的节点</el-descriptions-item>
          <el-descriptions-item label="HACCP">Hazard Analysis and Critical Control Points（危害分析与关键控制点），食品安全管理体系</el-descriptions-item>
          <el-descriptions-item label="IATF16949">汽车行业质量管理体系标准，要求完整的产品追溯能力</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsTraceDashboard">
import { ref, getCurrentInstance } from 'vue'
import { traceCompleteness, traceBreakList } from '@/api/qms/trace'

const { proxy } = getCurrentInstance()
const data = ref({})
const breakList = ref([])
const showHelp = ref(false)

traceCompleteness().then(res => { data.value = res.data })
traceBreakList({ pageNum: 1, pageSize: 10 }).then(res => { breakList.value = res.rows })
</script>

<style scoped>
.qms-trace-dashboard-page {
  padding-top: 10px;
  --ink-900: #0f172a; --ink-700: #334155; --ink-500: #64748b;
  --ink-400: #94a3b8; --ink-300: #cbd5e1; --ink-200: #e2e8f0;
  --ink-100: #f1f5f9; --ink-50: #f8fafc;
  --green-500: #10b981; --green-700: #047857;
  --red-500: #ef4444; --red-700: #b91c1c;
  --warning-500: #f59e0b;
  --brand-600: #4f46e5;
  --r-sm: 6px; --r-md: 10px; --r-lg: 14px;
  --shadow-card: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out: cubic-bezier(.16,.84,.44,1);
  color: var(--ink-900);
}

/* ===== Stat Cards ===== */
.qms-trace-dashboard-page .stat-row { margin-bottom: 8px; }
.qms-trace-dashboard-page .stat-card {
  background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-lg);
  box-shadow: var(--shadow-card); padding: 16px 20px; height: 100%;
  box-sizing: border-box; display: flex; flex-direction: column; justify-content: center;
}
.qms-trace-dashboard-page .stat-card__label {
  font-size: 13px; color: var(--ink-500); margin-bottom: 8px;
  display: flex; align-items: center; gap: 8px;
}
.qms-trace-dashboard-page .stat-card__target { font-size: 12px; color: var(--ink-400); }
.qms-trace-dashboard-page .stat-card__value {
  font-size: 28px; font-weight: 700; color: var(--ink-900); line-height: 1.2;
}
.qms-trace-dashboard-page .stat-card__value--green { color: var(--green-500); }
.qms-trace-dashboard-page .stat-card__value--red { color: var(--red-500); }
.qms-trace-dashboard-page .stat-card__value--orange { color: var(--warning-500); }

/* ===== Surface Card ===== */
.qms-trace-dashboard-page .surface {
  background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-lg);
  box-shadow: var(--shadow-card); overflow: hidden; margin-bottom: 8px;
}
.qms-trace-dashboard-page .section-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 20px; border-bottom: 1px solid var(--ink-200); background: var(--ink-50);
}
.qms-trace-dashboard-page .section-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; font-weight: 600; color: var(--ink-700);
}
.qms-trace-dashboard-page .section-title .glyph {
  width: 4px; height: 14px; background: var(--brand-600); border-radius: 2px;
}
.qms-trace-dashboard-page .tip-pill {
  display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px;
  font-size: 14px; font-weight: 500; border-radius: var(--r-sm);
  border: 1px solid #fde68a; background: #fffbeb; color: #b45309;
  cursor: pointer; transition: all .15s var(--ease-out); white-space: nowrap;
}
.qms-trace-dashboard-page .tip-pill:hover { background: #fef3c7; border-color: #f59e0b; }
.qms-trace-dashboard-page .table-wrap { padding: 12px 20px; }

/* ===== Help Dialog ===== */
.help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.help-content h4:first-child { margin-top: 0; }
.help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.help-content .highlight-primary .highlight-card-title { color: #409eff; }
.help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.help-content .highlight-success .highlight-card-title { color: #67c23a; }
.help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.help-content .highlight-info { background-color: #f4f4f5; border-color: #d3d4d6; }
.help-content .highlight-info .highlight-card-title { color: #909399; }
</style>
