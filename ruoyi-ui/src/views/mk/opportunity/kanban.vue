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
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog" @open="loadRelations">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">商机详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.opportunityNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.opportunityNo }}</span>
            <dict-tag :options="marketing_opportunity_status" :value="viewForm.opportunityStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <!-- 基本信息 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('basic')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
                  </span>
                  基本信息
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.basic">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">商机编号</span><div class="rd-value">{{ viewForm.opportunityNo }}</div></div>
                  <div class="rd-item"><span class="rd-label">商机名称</span><div class="rd-value">{{ viewForm.opportunityName }}</div></div>
                  <div class="rd-item"><span class="rd-label">所属客户</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
                  <div class="rd-item"><span class="rd-label">主要联系人</span><div class="rd-value">{{ viewForm.contactName }}</div></div>
                  <div class="rd-item"><span class="rd-label">商机来源</span><div class="rd-value"><dict-tag :options="marketing_opportunity_source" :value="viewForm.opportunitySource" /></div></div>
                </div>
              </div>
            </section>
            <!-- 金额与阶段 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('amount')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                  </span>
                  金额与阶段
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.amount }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.amount">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">销售阶段</span><div class="rd-value">{{ viewForm.stageName }}</div></div>
                  <div class="rd-item"><span class="rd-label">预计金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatAmount(viewForm.expectedAmount) }}</div></div>
                  <div class="rd-item"><span class="rd-label">预计成交</span><div class="rd-value">{{ viewForm.expectedDate }}</div></div>
                  <div class="rd-item"><span class="rd-label">赢率</span><div class="rd-value">{{ viewForm.winRate }}%</div></div>
                  <div class="rd-item"><span class="rd-label">加权金额</span><div class="rd-value rd-amount">￥{{ formatAmount(viewForm.weightedAmount) }}</div></div>
                  <div class="rd-item"><span class="rd-label">商机状态</span><div class="rd-value"><dict-tag :options="marketing_opportunity_status" :value="viewForm.opportunityStatus" /></div></div>
                </div>
              </div>
            </section>
            <!-- 竞争与方案 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('solution')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
                  </span>
                  竞争与方案
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.solution }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.solution">
                <div class="rd-grid">
                  <div class="rd-item rd-item--full"><span class="rd-label">竞争对手</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.competitor }">{{ viewForm.competitor || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">客户痛点</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.painPoint }">{{ viewForm.painPoint || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">解决方案</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.solution }">{{ viewForm.solution || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <!-- 负责与跟进 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('owner')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  </span>
                  负责与跟进
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.owner }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.owner">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewForm.userName }}</div></div>
                  <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewForm.deptName }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">下一步行动</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.nextAction }">{{ viewForm.nextAction || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">下次行动时间</span><div class="rd-value">{{ viewForm.nextTime }}</div></div>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`跟进记录 (${interactionList.length})`" name="interactions">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('interactions')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                  </span>
                  跟进记录
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.interactions }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.interactions">
                <div class="rd-timeline" v-if="interactionList.length > 0">
                  <div class="rd-timeline-item" v-for="item in interactionList" :key="item.recordId">
                    <div class="rd-timeline-dot rd-timeline-dot--success"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title">{{ item.userName }}</span>
                        <span class="rd-timeline-time">{{ item.interactTime }}</span>
                      </div>
                      <div class="rd-timeline-comment">{{ item.content }}</div>
                    </div>
                  </div>
                </div>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  <p class="rd-empty-text">暂无跟进记录</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`阶段历史 (${stageLogList.length})`" name="stageLog">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('stageLog')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><path d="M12 7v5l4 2"/></svg>
                  </span>
                  阶段历史
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.stageLog }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.stageLog">
                <div class="rd-timeline" v-if="stageLogList.length > 0">
                  <div class="rd-timeline-item" v-for="log in stageLogList" :key="log.logId">
                    <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': getLogTagType(log.actionType) === 'success', 'rd-timeline-dot--error': getLogTagType(log.actionType) === 'danger' }"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title">
                          <el-tag size="small" :type="getLogTagType(log.actionType)">{{ getActionTypeLabel(log.actionType) }}</el-tag>
                          <span style="margin-left: 8px">
                            <span v-if="log.fromStageName">{{ log.fromStageName }}</span>
                            <span v-if="log.toStageName"> → {{ log.toStageName }}</span>
                          </span>
                        </span>
                        <span class="rd-timeline-time">{{ log.operatorName }} · {{ log.operateTime }}</span>
                      </div>
                      <div class="rd-timeline-comment" v-if="log.remark">{{ log.remark }}</div>
                    </div>
                  </div>
                </div>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  <p class="rd-empty-text">暂无阶段变更记录</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`关联合同 (${contractList.length})`" name="contracts">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('contracts')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M16 13H8"/><path d="M16 17H8"/><path d="M10 9H8"/></svg>
                  </span>
                  关联合同
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.contracts }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.contracts">
                <el-table :data="contractList" border size="small" v-if="contractList.length > 0">
                  <el-table-column label="合同编号" prop="contractNo" width="150" />
                  <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
                  <el-table-column label="合同金额" prop="contractAmount" width="120" align="right" />
                  <el-table-column label="签约日期" prop="signDate" width="120" />
                  <el-table-column label="状态" prop="contractStatus" width="80" align="center">
                    <template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template>
                  </el-table-column>
                </el-table>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                  <p class="rd-empty-text">暂无关联合同</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`关联订单 (${orderList.length})`" name="orders">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('orders')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
                  </span>
                  关联订单
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.orders }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.orders">
                <el-table :data="orderList" border size="small" v-if="orderList.length > 0">
                  <el-table-column label="订单编号" prop="orderNo" width="150" />
                  <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
                  <el-table-column label="订单金额" prop="orderAmount" width="120" align="right" />
                  <el-table-column label="状态" prop="orderStatus" width="80" align="center">
                    <template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template>
                  </el-table-column>
                </el-table>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>
                  <p class="rd-empty-text">暂无关联订单</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MkOpportunityKanban">
import { listOpportunity, getOpportunity, advanceOpportunity, retreatOpportunity, getOpportunityRelations } from '@/api/mk/opportunity'
import { listStage } from '@/api/mk/stage'
import { listUser } from '@/api/system/user'
import { WarningFilled, Plus } from '@element-plus/icons-vue'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { marketing_opportunity_status, marketing_opportunity_source, marketing_contract_status, marketing_order_status } = proxy.useDict('marketing_opportunity_status', 'marketing_opportunity_source', 'marketing_contract_status', 'marketing_order_status')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'amount', 'solution', 'owner', 'interactions', 'stageLog', 'contracts', 'orders'])

const loading = ref(false)
const stages = ref([])
const userOptions = ref([])
const filterUserId = ref(undefined)
const viewOpen = ref(false)
const viewForm = ref({})
const detailTab = ref('basic')
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
  getOpportunity(row.opportunityId).then(res => {
    viewForm.value = res.data
    detailTab.value = 'basic'
    Object.keys(collapsedCards).forEach(k => { collapsedCards[k] = false })
    viewOpen.value = true
  })
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
