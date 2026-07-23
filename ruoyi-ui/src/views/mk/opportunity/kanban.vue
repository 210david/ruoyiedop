<template>
  <div class="app-container">
    <div class="kanban-toolbar">
      <div>
        <el-button type="primary" icon="Refresh" @click="loadData" :loading="loading">刷新</el-button>
      </div>
      <div>
        <span style="font-size: 13px; color: #909399; margin-right: 8px">负责人筛选:</span>
        <el-select v-model="filterUserId" placeholder="全部" clearable filterable style="width: 200px" @change="loadData">
          <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
        </el-select>
      </div>
    </div>

    <div class="kanban-container" v-loading="loading">
      <div class="kanban-column" v-for="stage in stages" :key="stage.stageCode">
        <div class="kanban-header">
          <span>{{ stage.stageName }}</span>
          <div class="kanban-header__right">
            <el-tag size="small" type="info">{{ stage.opportunities.length }}</el-tag>
            <span class="kanban-header__amount">¥{{ formatAmount(stage.totalAmount) }}</span>
          </div>
        </div>
        <div class="kanban-body" :data-stage="stage.stageCode"
             @dragover.prevent="onDragOver"
             @dragleave="onDragLeave"
             @drop="onDrop($event, stage)">
          <div class="kanban-card" v-for="opp in stage.opportunities" :key="opp.opportunityId"
               draggable="true"
               @dragstart="onDragStart($event, opp)"
               @dragend="onDragEnd"
               @click="handleView(opp)"
               :class="{ 'kanban-card--stagnant': isStagnant(opp, stage), 'kanban-card--won': opp.opportunityStatus === '1' }">
            <div class="card-title">{{ opp.opportunityName }}
              <el-tag v-if="opp.opportunityStatus === '1'" size="small" type="success" style="margin-left: 6px">赢单</el-tag>
            </div>
            <div class="card-info">
              <span>{{ opp.customerName }}</span>
              <span v-if="opp.userName" style="margin-left: 8px; font-size: 12px; color: #c0c4cc">{{ opp.userName }}</span>
            </div>
            <div class="card-footer">
              <span class="amount">¥{{ formatAmount(opp.expectedAmount) }}</span>
              <el-tag size="small" :type="opp.winRate >= 50 ? 'success' : 'warning'">{{ opp.winRate }}%</el-tag>
            </div>
            <div v-if="isStagnant(opp, stage)" class="card-stagnant-tag">
              <el-icon><WarningFilled /></el-icon> 停滞
            </div>
          </div>
          <div v-if="stage.opportunities.length === 0" class="empty-text">
            <el-icon style="font-size: 24px; color: #dcdfe6"><Plus /></el-icon>
            <div>拖拽商机到此处</div>
          </div>
        </div>
        <div class="kanban-summary">
          <div>加权金额: <span class="summary-amount">¥{{ formatAmount(stage.totalWeightedAmount) }}</span></div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog title="商机详情" v-model="viewOpen" width="900px" append-to-body @open="loadRelations">
      <el-tabs v-model="detailTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-collapse v-model="viewActiveNames">
            <el-collapse-item title="基本信息" name="basic">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="商机编号">{{ viewForm.opportunityNo }}</el-descriptions-item>
                <el-descriptions-item label="商机名称">{{ viewForm.opportunityName }}</el-descriptions-item>
                <el-descriptions-item label="所属客户">{{ viewForm.customerName }}</el-descriptions-item>
                <el-descriptions-item label="主要联系人">{{ viewForm.contactName }}</el-descriptions-item>
                <el-descriptions-item label="商机来源"><dict-tag :options="marketing_opportunity_source" :value="viewForm.opportunitySource" /></el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
            <el-collapse-item title="金额与阶段" name="amount">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="销售阶段">{{ viewForm.stageName }}</el-descriptions-item>
                <el-descriptions-item label="预计金额">¥{{ viewForm.expectedAmount }}</el-descriptions-item>
                <el-descriptions-item label="预计成交">{{ viewForm.expectedDate }}</el-descriptions-item>
                <el-descriptions-item label="赢率">{{ viewForm.winRate }}%</el-descriptions-item>
                <el-descriptions-item label="加权金额">¥{{ viewForm.weightedAmount }}</el-descriptions-item>
                <el-descriptions-item label="商机状态"><dict-tag :options="marketing_opportunity_status" :value="viewForm.opportunityStatus" /></el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
            <el-collapse-item title="竞争与方案" name="solution">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="竞争对手">{{ viewForm.competitor }}</el-descriptions-item>
                <el-descriptions-item label="客户痛点" :span="2">{{ viewForm.painPoint }}</el-descriptions-item>
                <el-descriptions-item label="解决方案" :span="2">{{ viewForm.solution }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
            <el-collapse-item title="负责与跟进" name="owner">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="负责人">{{ viewForm.userName }}</el-descriptions-item>
                <el-descriptions-item label="所属部门">{{ viewForm.deptName }}</el-descriptions-item>
                <el-descriptions-item label="下一步行动">{{ viewForm.nextAction }}</el-descriptions-item>
                <el-descriptions-item label="下次行动时间">{{ viewForm.nextTime }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
          </el-collapse>
        </el-tab-pane>
        <el-tab-pane :label="`跟进记录 (${interactionList.length})`" name="interactions">
          <el-timeline v-if="interactionList.length > 0">
            <el-timeline-item v-for="item in interactionList" :key="item.recordId" :timestamp="item.interactTime" placement="top">
              <el-card>
                <p style="margin: 0">{{ item.content }}</p>
                <div style="margin-top: 4px; font-size: 12px; color: #909399">{{ item.userName }}</div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无跟进记录" />
        </el-tab-pane>
        <el-tab-pane :label="`阶段历史 (${stageLogList.length})`" name="stageLog">
          <el-timeline v-if="stageLogList.length > 0">
            <el-timeline-item v-for="log in stageLogList" :key="log.logId" :timestamp="log.operateTime" placement="top" :type="getLogTagType(log.actionType)">
              <el-card>
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <div>
                    <el-tag size="small" :type="getLogTagType(log.actionType)">{{ getActionTypeLabel(log.actionType) }}</el-tag>
                    <span style="margin-left: 8px">
                      <span v-if="log.fromStageName">{{ log.fromStageName }}</span>
                      <span v-if="log.toStageName"> → {{ log.toStageName }}</span>
                    </span>
                  </div>
                  <span style="color: #909399; font-size: 12px">{{ log.operatorName }}</span>
                </div>
                <p v-if="log.remark" style="margin: 8px 0 0; color: #606266; font-size: 13px">{{ log.remark }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无阶段变更记录" />
        </el-tab-pane>
        <el-tab-pane :label="`关联合同 (${contractList.length})`" name="contracts">
          <el-table :data="contractList" border size="small" v-if="contractList.length > 0">
            <el-table-column label="合同编号" prop="contractNo" width="150" />
            <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
            <el-table-column label="合同金额" prop="contractAmount" width="120" align="right" />
            <el-table-column label="签约日期" prop="signDate" width="120" />
            <el-table-column label="状态" prop="contractStatus" width="80" align="center">
              <template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无关联合同" />
        </el-tab-pane>
        <el-tab-pane :label="`关联订单 (${orderList.length})`" name="orders">
          <el-table :data="orderList" border size="small" v-if="orderList.length > 0">
            <el-table-column label="订单编号" prop="orderNo" width="150" />
            <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
            <el-table-column label="订单金额" prop="orderAmount" width="120" align="right" />
            <el-table-column label="状态" prop="orderStatus" width="80" align="center">
              <template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无关联订单" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup name="MkOpportunityKanban">
import { listOpportunity, getOpportunity, advanceOpportunity, retreatOpportunity, getOpportunityRelations } from '@/api/mk/opportunity'
import { listStage } from '@/api/mk/stage'
import { listUser } from '@/api/system/user'
import { WarningFilled, Plus } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { marketing_opportunity_status, marketing_opportunity_source, marketing_contract_status, marketing_order_status } = proxy.useDict('marketing_opportunity_status', 'marketing_opportunity_source', 'marketing_contract_status', 'marketing_order_status')

const loading = ref(false)
const stages = ref([])
const userOptions = ref([])
const filterUserId = ref(undefined)
const viewOpen = ref(false)
const viewForm = ref({})
const detailTab = ref('basic')
const viewActiveNames = ref(['basic', 'amount', 'solution', 'owner'])
const interactionList = ref([])
const stageLogList = ref([])
const contractList = ref([])
const orderList = ref([])

// 拖拽状态
const draggingOpp = ref(null)
const draggingEl = ref(null)

function loadData() {
  loading.value = true
  Promise.all([
    listStage({ pageNum: 1, pageSize: 9999, status: '0' }),
    listOpportunity({ pageNum: 1, pageSize: 9999, userId: filterUserId.value })
  ]).then(([stageRes, oppRes]) => {
    // 排除输单商机，看板只展示进行中 + 赢单
    const activeOpps = oppRes.rows.filter(o => o.opportunityStatus !== '2')
    stages.value = stageRes.rows.map(s => {
      const opps = activeOpps.filter(o => o.stageCode === s.stageCode)
      return {
        ...s,
        opportunities: opps,
        count: opps.length,
        totalAmount: opps.reduce((sum, o) => sum + Number(o.expectedAmount || 0), 0),
        totalWeightedAmount: opps.reduce((sum, o) => sum + Number(o.weightedAmount || 0), 0)
      }
    })
    loading.value = false
  }).catch(() => { loading.value = false })
}

function getUserOptions() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}

function formatAmount(val) {
  if (!val) return '0'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

function isStagnant(opp, stage) {
  if (!opp.stageEnterTime) return false
  const days = Math.floor((Date.now() - new Date(opp.stageEnterTime).getTime()) / (1000 * 60 * 60 * 24))
  return days > (stage.maxDays || 30)
}

// ===== 拖拽逻辑 =====
function onDragStart(e, opp) {
  draggingOpp.value = opp
  draggingEl.value = e.target
  e.target.style.opacity = '0.5'
  e.dataTransfer.effectAllowed = 'move'
}

function onDragEnd(e) {
  e.target.style.opacity = '1'
  // 清除所有 drag-over 样式
  document.querySelectorAll('.kanban-body.drag-over').forEach(el => el.classList.remove('drag-over'))
}

function onDragOver(e) {
  e.preventDefault()
  e.currentTarget.classList.add('drag-over')
}

function onDragLeave(e) {
  e.currentTarget.classList.remove('drag-over')
}

function onDrop(e, targetStage) {
  e.preventDefault()
  e.currentTarget.classList.remove('drag-over')
  const opp = draggingOpp.value
  if (!opp) return

  if (opp.stageCode === targetStage.stageCode) {
    return // 同一阶段不处理
  }

  // 判断是推进还是退回
  const currentStage = stages.value.find(s => s.stageCode === opp.stageCode)
  if (!currentStage) return

  if (targetStage.sort > currentStage.sort) {
    // 推进
    proxy.$modal.confirm(`确认将商机"${opp.opportunityName}"从"${currentStage.stageName}"推进到"${targetStage.stageName}"？`).then(() => {
      advanceOpportunity(opp.opportunityId, { stageCode: targetStage.stageCode, remark: '看板拖拽推进' }).then(() => {
        proxy.$modal.msgSuccess('阶段推进成功')
        loadData()
      }).catch(() => {})
    }).catch(() => {})
  } else {
    // 退回
    proxy.$modal.confirm(`确认将商机"${opp.opportunityName}"从"${currentStage.stageName}"退回到"${targetStage.stageName}"？`).then(() => {
      retreatOpportunity(opp.opportunityId, { stageCode: targetStage.stageCode, remark: '看板拖拽退回' }).then(() => {
        proxy.$modal.msgSuccess('阶段退回成功')
        loadData()
      }).catch(() => {})
    }).catch(() => {})
  }
}

// ===== 详情 =====
function handleView(row) {
  getOpportunity(row.opportunityId).then(res => { viewForm.value = res.data; detailTab.value = 'basic'; viewOpen.value = true })
}

function loadRelations() {
  if (!viewForm.value.opportunityId) return
  getOpportunityRelations(viewForm.value.opportunityId).then(res => {
    const data = res.data || {}
    interactionList.value = data.interactions || []
    stageLogList.value = data.stageLogs || []
    contractList.value = data.contracts || []
    orderList.value = data.orders || []
  })
}

function getActionTypeLabel(type) {
  const map = { advance: '推进', retreat: '退回', win: '赢单', lose: '输单', reopen: '重开' }
  return map[type] || type
}

function getLogTagType(type) {
  const map = { advance: 'success', retreat: 'warning', win: 'success', lose: 'danger', reopen: 'info' }
  return map[type] || 'primary'
}

getUserOptions()
loadData()
</script>

<style scoped>
.kanban-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.kanban-container { display: flex; gap: 15px; overflow-x: auto; padding-bottom: 10px; }
.kanban-column { min-width: 280px; background: #f5f7fa; border-radius: 8px; display: flex; flex-direction: column; }
.kanban-header { padding: 12px 15px; font-weight: bold; display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #e4e7ed; }
.kanban-header__right { display: flex; align-items: center; gap: 8px; }
.kanban-header__amount { font-size: 13px; color: #e6a23c; font-weight: 600; }
.kanban-body { padding: 10px; flex: 1; min-height: 100px; max-height: 500px; overflow-y: auto; transition: background 0.2s; }
.kanban-body.drag-over { background: #e6f4ff; border: 2px dashed #409eff; border-radius: 6px; padding: 8px; }
.kanban-card { background: #fff; border-radius: 6px; padding: 12px; margin-bottom: 10px; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.1); transition: box-shadow 0.2s, transform 0.15s; position: relative; }
.kanban-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.15); transform: translateY(-1px); }
.kanban-card--stagnant { border-left: 3px solid #f56c6c; }
.kanban-card--won { border-left: 3px solid #67c23a; background: #f0f9eb; }
.card-title { font-weight: 600; margin-bottom: 6px; color: #303133; }
.card-info { font-size: 13px; color: #909399; margin-bottom: 8px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-footer .amount { color: #e6a23c; font-weight: 600; }
.card-stagnant-tag { position: absolute; top: 8px; right: 8px; font-size: 11px; color: #f56c6c; display: flex; align-items: center; gap: 2px; }
.kanban-summary { padding: 8px 15px; border-top: 1px solid #e4e7ed; font-size: 12px; color: #909399; }
.summary-amount { color: #409eff; font-weight: 600; }
.empty-text { text-align: center; color: #c0c4cc; padding: 30px 20px; font-size: 13px; }
.empty-text .el-icon { margin-bottom: 4px; }

html.dark .kanban-column { background: var(--el-bg-color-overlay); }
html.dark .kanban-card { background: var(--el-bg-color); }
html.dark .card-title { color: var(--el-text-color-primary); }
html.dark .kanban-body.drag-over { background: rgba(64, 158, 255, 0.1); }
</style>
