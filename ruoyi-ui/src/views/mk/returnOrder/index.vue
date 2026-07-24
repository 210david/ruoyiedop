<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="退货编号" prop="returnNo">
        <el-input v-model="queryParams.returnNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="退货状态" prop="returnStatus">
        <el-select v-model="queryParams.returnStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_return_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:return:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:return:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="退货编号" prop="returnNo" :width="colWidth('returnNo', 150)" resizable />
      <el-table-column label="订单编号" prop="orderNo" :width="colWidth('orderNo', 150)" resizable />
      <el-table-column label="客户名称" prop="customerName" :width="colWidth('customerName', 180)" resizable show-overflow-tooltip />
      <el-table-column label="退货金额" prop="returnAmount" :width="colWidth('returnAmount', 120)" resizable align="right" />
      <el-table-column label="退货原因" prop="returnReason" :width="colWidth('returnReason', 200)" resizable show-overflow-tooltip />
      <el-table-column label="退货状态" prop="returnStatus" :width="colWidth('returnStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_return_status" :value="scope.row.returnStatus" /></template>
      </el-table-column>
      <el-table-column label="退款金额" prop="refundAmount" :width="colWidth('refundAmount', 120)" resizable align="right" />
      <el-table-column label="退款日期" prop="refundDate" :width="colWidth('refundDate', 120)" resizable />
      <el-table-column label="审批人" prop="approveBy" :width="colWidth('approveBy', 100)" resizable />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button v-if="scope.row.returnStatus === '0'" link type="success" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['marketing:return:approve']">审批</el-button>
          <el-button v-if="scope.row.returnStatus === '1'" link type="primary" icon="Money" @click="handleRefund(scope.row)" v-hasPermi="['marketing:return:refund']">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 审批对话框 -->
    <el-dialog v-model="approveOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <path d="M9 15l2 2 4-4"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">审批退货</span>
          <div class="rd-detail-header-sub" v-if="approveForm.returnNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ approveForm.returnNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="approveRef" :model="approveForm" label-width="100px">
        <div class="rd-page">
          <!-- 退货信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('approveReturn')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                退货信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveReturn }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.approveReturn">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">退货编号</span><div class="rd-value">{{ approveForm.returnNo }}</div></div>
                <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ approveForm.orderNo }}</div></div>
                <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ approveForm.customerName }}</div></div>
                <div class="rd-item"><span class="rd-label">退货金额</span><div class="rd-value rd-value--large rd-amount rd-amount--negative">￥{{ formatAmount(approveForm.returnAmount) }}</div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">退货原因</span><div class="rd-value">{{ approveForm.returnReason }}</div></div>
              </div>
            </div>
          </section>

          <!-- 审批信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('approveReview')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 11l3 3L22 4"/>
                    <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                  </svg>
                </span>
                审批信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveReview }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.approveReview">
              <el-form-item label="审批意见" prop="approveOpinion" :rules="[{ required: true, message: '请输入审批意见', trigger: 'blur' }]">
                <el-input v-model="approveForm.approveOpinion" type="textarea" :rows="3" placeholder="请输入审批意见" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="success" @click="submitApprove(true)">通 过</el-button>
        <el-button type="danger" @click="submitApprove(false)">驳 回</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 退款对话框 -->
    <el-dialog v-model="refundOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="6" width="20" height="12" rx="2"/>
              <circle cx="12" cy="12" r="2"/>
              <path d="M6 12h.01M18 12h.01"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">退款</span>
          <div class="rd-detail-header-sub" v-if="refundForm.returnNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ refundForm.returnNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="refundRef" :model="refundForm" :rules="refundRules" label-width="100px">
        <div class="rd-page">
          <!-- 退货信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('refundReturn')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                退货信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.refundReturn }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.refundReturn">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">退货编号</span><div class="rd-value">{{ refundForm.returnNo }}</div></div>
                <div class="rd-item"><span class="rd-label">退货金额</span><div class="rd-value rd-value--large rd-amount rd-amount--negative">￥{{ formatAmount(refundForm.returnAmount) }}</div></div>
              </div>
            </div>
          </section>

          <!-- 退款信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('refundInfo')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="2" y="6" width="20" height="12" rx="2"/>
                    <circle cx="12" cy="12" r="2"/>
                    <path d="M6 12h.01M18 12h.01"/>
                  </svg>
                </span>
                退款信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.refundInfo }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.refundInfo">
              <el-row>
                <el-col :span="12"><el-form-item label="退款金额" prop="refundAmount"><el-input-number v-model="refundForm.refundAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="退款日期" prop="refundDate"><el-date-picker v-model="refundForm.refundDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitRefund">确认退款</el-button>
        <el-button @click="refundOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
              <path d="M3 3v5h5"/>
              <path d="M9 12h6"/>
              <path d="M12 9v6"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">退货详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.returnNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.returnNo }}</span>
            <dict-tag :options="marketing_return_status" :value="viewForm.returnStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>退货编号</span>
                <div class="rd-value">{{ viewForm.returnNo }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>订单编号</span>
                <div class="rd-value">{{ viewForm.orderNo }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>客户名称</span>
                <div class="rd-value">{{ viewForm.customerName }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>退货金额</span>
                <div class="rd-value rd-value--large rd-amount rd-amount--negative">￥{{ formatAmount(viewForm.returnAmount) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>退货状态</span>
                <div class="rd-value"><dict-tag :options="marketing_return_status" :value="viewForm.returnStatus" /></div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>创建时间</span>
                <div class="rd-value">{{ viewForm.createTime }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 退货原因 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('reason')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="16" x2="12" y2="12"/>
                  <line x1="12" y1="8" x2="12.01" y2="8"/>
                </svg>
              </span>
              退货原因
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.reason }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.reason">
            <div class="rd-grid">
              <div class="rd-item rd-item--full">
                <span class="rd-label"><span class="rd-label-dot"></span>退货原因</span>
                <div class="rd-value">{{ viewForm.returnReason }}</div>
              </div>
              <div class="rd-item rd-item--full">
                <span class="rd-label"><span class="rd-label-dot"></span>备注</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无备注' }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 审批信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('approve')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <path d="M9 15l2 2 4-4"/>
                </svg>
              </span>
              审批信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approve }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.approve">
            <div class="rd-timeline" v-if="viewForm.returnStatus !== '0' && viewForm.approveTime">
              <div class="rd-timeline-item">
                <div class="rd-timeline-dot" :class="viewForm.returnStatus === '2' ? 'rd-timeline-dot--error' : 'rd-timeline-dot--success'"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="viewForm.returnStatus === '2'" type="danger" size="small" effect="light" round>已驳回</el-tag>
                      <el-tag v-else type="success" size="small" effect="light" round>已通过</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ viewForm.approveTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item">
                      <span class="rd-label"><span class="rd-label-dot"></span>审批人</span>
                      <div class="rd-value">{{ viewForm.approveBy }}</div>
                    </div>
                    <div class="rd-item">
                      <span class="rd-label"><span class="rd-label-dot"></span>审批时间</span>
                      <div class="rd-value">{{ viewForm.approveTime }}</div>
                    </div>
                  </div>
                  <div class="rd-timeline-comment" v-if="viewForm.approveOpinion">
                    <strong>审批意见：</strong>{{ viewForm.approveOpinion }}
                  </div>
                </div>
              </div>
            </div>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <p class="rd-empty-text">暂无审批记录</p>
            </div>
          </div>
        </section>

        <!-- 退款信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('refund')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="2" y="6" width="20" height="12" rx="2"/>
                  <circle cx="12" cy="12" r="2"/>
                  <path d="M6 12h.01M18 12h.01"/>
                </svg>
              </span>
              退款信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.refund }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.refund">
            <div class="rd-grid" v-if="viewForm.refundAmount != null && viewForm.refundAmount !== ''">
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>退款金额</span>
                <div class="rd-value rd-value--large rd-amount rd-amount--negative">￥{{ formatAmount(viewForm.refundAmount) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>退款日期</span>
                <div class="rd-value">{{ viewForm.refundDate }}</div>
              </div>
            </div>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="6" width="20" height="12" rx="2"/>
                <circle cx="12" cy="12" r="2"/>
                <path d="M6 12h.01M18 12h.01"/>
              </svg>
              <p class="rd-empty-text">暂无退款记录</p>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MkReturn">
import { listReturn, getReturn, delReturn, approveReturn, refundReturn } from '@/api/mk/returnOrder'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_return_index')
const { marketing_return_status } = proxy.useDict('marketing_return_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const viewOpen = ref(false)
const viewForm = ref({})
const refundOpen = ref(false)
const refundForm = ref({})
const approveOpen = ref(false)
const approveForm = ref({})
const collapsedCards = reactive({ basic: false, reason: false, approve: false, refund: false, approveReturn: false, approveReview: false, refundReturn: false, refundInfo: false })
function toggleCard(name) { collapsedCards[name] = !collapsedCards[name] }

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, returnNo: undefined, customerName: undefined, returnStatus: undefined }
})
const { queryParams } = toRefs(data)

const refundRules = {
  refundAmount: [{ required: true, message: '退款金额不能为空', trigger: 'blur' }],
  refundDate: [{ required: true, message: '请选择退款日期', trigger: 'change' }]
}

function getList() { loading.value = true; listReturn(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.returnId); single.value = selection.length !== 1; multiple.value = !selection.length }
function formatAmount(val) { if (val == null || val === '') return '-'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }
function handleView(row) { getReturn(row.returnId).then(res => { viewForm.value = res.data; collapsedCards.approve = !(res.data.returnStatus !== '0' && res.data.approveTime); collapsedCards.refund = !(res.data.refundAmount != null && res.data.refundAmount !== ''); viewOpen.value = true }) }
function handleDelete(row) { const returnIds = row.returnId || ids.value; proxy.$modal.confirm('确认删除选中的退货记录？').then(() => delReturn(returnIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleApprove(row) { getReturn(row.returnId).then(res => { approveForm.value = res.data; approveForm.value.approveOpinion = ''; approveOpen.value = true }) }
function submitApprove(approved) { proxy.$refs['approveRef'].validate(valid => { if (valid) { approveReturn(approveForm.value.returnId, approved, approveForm.value.approveOpinion || '').then(() => { proxy.$modal.msgSuccess(approved ? '审批通过' : '审批驳回'); approveOpen.value = false; getList() }) } }) }
function handleRefund(row) { refundForm.value = { returnId: row.returnId, returnNo: row.returnNo, returnAmount: row.returnAmount, refundAmount: row.returnAmount, refundDate: undefined }; refundOpen.value = true }
function submitRefund() { proxy.$refs['refundRef'].validate(valid => { if (valid) { refundReturn(refundForm.value.returnId, refundForm.value.refundAmount, refundForm.value.refundDate).then(() => { proxy.$modal.msgSuccess('退款成功'); refundOpen.value = false; getList() }) } }) }
function handleExport() { proxy.download('mk/return/export', { ...queryParams.value }, `return_${new Date().getTime()}.xlsx`) }
getList()
</script>

<style scoped>
/* 卡片式样式使用全局 detail-page.scss，此处仅保留页面特有样式 */
</style>
