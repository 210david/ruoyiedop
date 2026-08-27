<template>
  <div class="app-container mk-list-page">
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
          <label>退货编号</label>
          <div class="control">
            <el-input v-model="queryParams.returnNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>客户名称</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>退货状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.returnStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_return_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>订单编号</label>
          <div class="control">
            <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>退货原因</label>
          <div class="control">
            <el-input v-model="queryParams.returnReason" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>退款日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRangeCreateTime" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
      <!-- Status Tabs + Tip Pill -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><QuestionFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:return:add']">新增</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:return:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:return:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_return_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="退货编号" prop="returnNo" key="returnNo" :width="colWidth('returnNo', 150)" resizable v-if="columns.returnNo.visible" />
          <el-table-column label="订单编号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 150)" resizable v-if="columns.orderNo.visible" />
          <el-table-column label="客户名称" prop="customerName" key="customerName" :width="colWidth('customerName', 180)" resizable show-overflow-tooltip v-if="columns.customerName.visible" />
          <el-table-column label="退货金额" prop="returnAmount" key="returnAmount" :width="colWidth('returnAmount', 120)" resizable align="right" v-if="columns.returnAmount.visible" />
          <el-table-column label="退货原因" prop="returnReason" key="returnReason" :width="colWidth('returnReason', 200)" resizable show-overflow-tooltip v-if="columns.returnReason.visible" />
          <el-table-column label="退货状态" prop="returnStatus" key="returnStatus" :width="colWidth('returnStatus', 100)" resizable align="center" v-if="columns.returnStatus.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.returnStatus)"><span class="dot"></span>{{ statusLabel(scope.row.returnStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="退款金额" prop="refundAmount" key="refundAmount" :width="colWidth('refundAmount', 120)" resizable align="right" v-if="columns.refundAmount.visible" />
          <el-table-column label="退款日期" prop="refundDate" key="refundDate" :width="colWidth('refundDate', 120)" resizable v-if="columns.refundDate.visible" />
          <el-table-column label="审批人" prop="approveBy" key="approveBy" :width="colWidth('approveBy', 100)" resizable v-if="columns.approveBy.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
        <template #default="scope">
          <div class="action-btn-row">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button v-if="scope.row.returnStatus === '4' || scope.row.returnStatus === '2'" link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['marketing:return:edit']">修改</el-button>
          <el-button v-if="scope.row.returnStatus === '4' || scope.row.returnStatus === '2'" link type="success" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['marketing:return:edit']">提交</el-button>
          <el-button v-if="scope.row.returnStatus === '0'" link type="success" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['marketing:return:approve']">审批</el-button>
          <el-button v-if="scope.row.returnStatus === '1'" link type="primary" icon="Money" @click="handleRefund(scope.row)" v-hasPermi="['marketing:return:refund']">退款</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="订单退货业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已通过</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已退款</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-tag size="small" type="info">修改后重置</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            退货单创建后的初始状态，可修改、提交审批。从发货管理页面发起退货或在本页面新增退货均创建为草稿状态
          </el-descriptions-item>
          <el-descriptions-item label="待审批">
            已提交审批，等待审批人审核。提交审批时系统自动将关联订单状态更新为“退货中”
          </el-descriptions-item>
          <el-descriptions-item label="已通过">
            审批通过，等待财务执行退款操作。仅营销总监有审批权限
          </el-descriptions-item>
          <el-descriptions-item label="已驳回">
            审批未通过，可根据审批意见修改后重新提交。驳回时系统自动恢复订单原状态
          </el-descriptions-item>
          <el-descriptions-item label="已退款">
            财务已执行退款，退货流程结束。退款时系统自动将关联订单状态更新为“已退货”
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、业务规则</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>退货创建：</strong>可从发货管理页面（已发货/已签收的发货单）直接发起退货，也可在本页面选择订单创建退货单。退货明细自动带出订单物料，退货数量不能超过订单数量
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>退货金额自动计算：</strong>退货金额根据各明细行退货数量×单价自动汇总，无需手动填写
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>草稿不影响订单：</strong>草稿状态的退货单不会更新订单状态，提交审批时才会将订单状态变为“退货中”
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审批流程：</strong>提交审批后由营销总监审核。审批通过可进入退款流程；审批驳回可修改后重新提交。每次审批操作均记录审批日志
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>退款操作：</strong>仅已通过状态的退货单可执行退款。退款需填写退款金额和退款日期，由财务专员操作。退款后订单状态变为“已退货”
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>驳回恢复：</strong>退货被驳回时，系统自动恢复关联订单的原状态（已签收或已完成）
          </el-timeline-item>
        </el-timeline>

        <h4>四、操作权限</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="新增退货">营销总监、销售经理可操作。选择关联订单后自动带出退货明细</el-descriptions-item>
          <el-descriptions-item label="修改退货">仅草稿或已驳回状态可修改，修改后退回到草稿状态</el-descriptions-item>
          <el-descriptions-item label="提交审批">仅草稿或已驳回状态可提交</el-descriptions-item>
          <el-descriptions-item label="审批退货">仅营销总监有审批权限，可通过或驳回</el-descriptions-item>
          <el-descriptions-item label="退款">仅财务专员可操作，需已通过状态</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 新增退货对话框 -->
    <el-dialog v-model="addOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="12" y1="18" x2="12" y2="12"/>
              <polyline points="9 15 12 12 15 15"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ addTitle }}</span>
          <div class="rd-detail-header-sub" v-if="addForm.returnNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ addForm.returnNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="addRef" :model="addForm" :rules="addRules" label-width="100px">
        <div class="rd-page">
          <!-- 订单信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addOrder')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                    <line x1="3" y1="6" x2="21" y2="6"/>
                    <path d="M16 10a4 4 0 0 1-8 0"/>
                  </svg>
                </span>
                订单信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addOrder }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addOrder">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="关联订单" prop="orderId">
                    <el-select v-model="addForm.orderId" filterable clearable placeholder="请选择订单" style="width: 100%" @change="onOrderChange">
                      <el-option v-for="o in orderOptions" :key="o.orderId" :label="o.orderNo + (o.customerName ? ' - ' + o.customerName : '')" :value="o.orderId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="订单编号">
                    <el-input v-model="addForm.orderNo" placeholder="选择订单后自动带出" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户名称">
                    <el-input v-model="addForm.customerName" placeholder="选择订单后自动带出" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <!-- 退货明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addItems')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="8" y1="6" x2="21" y2="6"/>
                    <line x1="8" y1="12" x2="21" y2="12"/>
                    <line x1="8" y1="18" x2="21" y2="18"/>
                    <line x1="3" y1="6" x2="3.01" y2="6"/>
                    <line x1="3" y1="12" x2="3.01" y2="12"/>
                    <line x1="3" y1="18" x2="3.01" y2="18"/>
                  </svg>
                </span>
                退货明细
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addItems }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addItems">
              <el-table :data="addForm.detailList" border size="small" empty-text="选择订单后，物料明细将自动带出">
                <el-table-column label="序号" type="index" width="85" align="center" />
                <el-table-column label="商品名称" prop="productName" min-width="150" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="productSpec" min-width="120" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="80" align="center" />
                <el-table-column label="订单数量" prop="orderQty" width="100" align="right" />
                <el-table-column label="发货数量" prop="shipQty" width="100" align="right" />
                <el-table-column label="退货数量" width="120" align="center">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.returnQty" :min="0" :max="scope.row.orderQty" :precision="2" :controls="false" size="small" style="width: 90px" placeholder="必填" @change="calcReturnAmount" />
                  </template>
                </el-table-column>
                <el-table-column label="单价" width="100" align="right">
                  <template #default="scope"><span class="rd-amount">{{ formatAmount(scope.row.unitPrice) }}</span></template>
                </el-table-column>
                <el-table-column label="退货金额" width="110" align="right">
                  <template #default="scope"><span class="rd-amount">{{ formatAmount(scope.row.returnAmount) }}</span></template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" size="small" @click="handleDeleteDetail(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="addForm.detailList && addForm.detailList.length" style="text-align: right; padding: 8px 0; font-weight: bold;">
                退货金额合计：￥{{ formatAmount(totalReturnAmount) }}
              </div>
            </div>
          </section>

          <!-- 退货信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addReturn')">
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
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addReturn }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addReturn">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="退货金额">
                    <el-input :model-value="formatAmount(totalReturnAmount)" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="退货状态">
                    <el-tag type="info" effect="light" round>草稿</el-tag>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="退货原因" prop="returnReason">
                    <el-input v-model="addForm.returnReason" type="textarea" :rows="3" placeholder="请输入退货原因" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="addForm.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <!-- 审核记录（仅修改时显示） -->
          <section class="rd-card" v-if="isEdit && addForm.approveLogList && addForm.approveLogList.length">
            <div class="rd-card-header" @click="toggleCard('editAudit')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 11l3 3L22 4"/>
                    <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                  </svg>
                </span>
                审核记录
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.editAudit }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.editAudit" style="display:block">
              <el-alert v-if="addForm.returnStatus === '2'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该退货单已被驳回，请根据审核意见修改后重新提交</template>
              </el-alert>
              <div class="rd-timeline">
                <div class="rd-timeline-item" v-for="log in addForm.approveLogList" :key="log.logId">
                  <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.actionType === '2', 'rd-timeline-dot--error': log.actionType === '3' }"></div>
                  <div class="rd-timeline-content">
                    <div class="rd-timeline-header">
                      <span class="rd-timeline-title">
                        <el-tag v-if="log.actionType === '1'" type="primary" size="small" effect="light" round>提交审批</el-tag>
                        <el-tag v-else-if="log.actionType === '2'" type="success" size="small" effect="light" round>审批通过</el-tag>
                        <el-tag v-else-if="log.actionType === '3'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                      </span>
                      <span class="rd-timeline-time">{{ log.approveTime }}</span>
                    </div>
                    <div class="rd-timeline-body">
                      <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ log.approveBy || '-' }}</div></div>
                    </div>
                    <div class="rd-timeline-comment" v-if="log.approveOpinion">
                      <strong>审批意见：</strong>{{ log.approveOpinion }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="addOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveOpen" width="1080px" append-to-body draggable class="rd-dialog">
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

          <!-- 退货明细 -->
          <section class="rd-card" v-if="approveForm.detailList && approveForm.detailList.length">
            <div class="rd-card-header" @click="toggleCard('approveItems')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="8" y1="6" x2="21" y2="6"/>
                    <line x1="8" y1="12" x2="21" y2="12"/>
                    <line x1="8" y1="18" x2="21" y2="18"/>
                    <line x1="3" y1="6" x2="3.01" y2="6"/>
                    <line x1="3" y1="12" x2="3.01" y2="12"/>
                    <line x1="3" y1="18" x2="3.01" y2="18"/>
                  </svg>
                </span>
                退货明细
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveItems }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.approveItems">
              <el-table :data="approveForm.detailList" border size="small">
                <el-table-column label="序号" type="index" width="85" align="center" />
                <el-table-column label="商品名称" prop="productName" min-width="150" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="productSpec" min-width="120" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="80" align="center" />
                <el-table-column label="订单数量" prop="orderQty" width="100" align="right" />
                <el-table-column label="发货数量" prop="shipQty" width="100" align="right" />
                <el-table-column label="退货数量" prop="returnQty" width="100" align="right" />
                <el-table-column label="单价" width="100" align="right"><template #default="scope"><span class="rd-amount">{{ formatAmount(scope.row.unitPrice) }}</span></template></el-table-column>
                <el-table-column label="退货金额" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatAmount(scope.row.returnAmount) }}</span></template></el-table-column>
              </el-table>
            </div>
          </section>

          <!-- 审核记录 -->
          <section class="rd-card" v-if="approveForm.approveLogList && approveForm.approveLogList.length">
            <div class="rd-card-header" @click="toggleCard('approveAudit')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 11l3 3L22 4"/>
                    <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                  </svg>
                </span>
                审核记录
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveAudit }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.approveAudit" style="display:block">
              <div class="rd-timeline">
                <div class="rd-timeline-item" v-for="log in approveForm.approveLogList" :key="log.logId">
                  <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.actionType === '2', 'rd-timeline-dot--error': log.actionType === '3' }"></div>
                  <div class="rd-timeline-content">
                    <div class="rd-timeline-header">
                      <span class="rd-timeline-title">
                        <el-tag v-if="log.actionType === '1'" type="primary" size="small" effect="light" round>提交审批</el-tag>
                        <el-tag v-else-if="log.actionType === '2'" type="success" size="small" effect="light" round>审批通过</el-tag>
                        <el-tag v-else-if="log.actionType === '3'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                      </span>
                      <span class="rd-timeline-time">{{ log.approveTime }}</span>
                    </div>
                    <div class="rd-timeline-body">
                      <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ log.approveBy || '-' }}</div></div>
                    </div>
                    <div class="rd-timeline-comment" v-if="log.approveOpinion">
                      <strong>审批意见：</strong>{{ log.approveOpinion }}
                    </div>
                  </div>
                </div>
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
    <el-dialog v-model="refundOpen" width="840px" append-to-body draggable class="rd-dialog">
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
    <el-dialog v-model="viewOpen" width="1080px" append-to-body draggable class="rd-dialog">
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

        <!-- 退货明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewItems')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="8" y1="6" x2="21" y2="6"/>
                  <line x1="8" y1="12" x2="21" y2="12"/>
                  <line x1="8" y1="18" x2="21" y2="18"/>
                  <line x1="3" y1="6" x2="3.01" y2="6"/>
                  <line x1="3" y1="12" x2="3.01" y2="12"/>
                  <line x1="3" y1="18" x2="3.01" y2="18"/>
                </svg>
              </span>
              退货明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewItems }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewItems">
            <el-table :data="viewForm.detailList" border size="small" v-if="viewForm.detailList && viewForm.detailList.length">
              <el-table-column label="序号" type="index" width="85" align="center" />
              <el-table-column label="商品名称" prop="productName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="productSpec" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center" />
              <el-table-column label="订单数量" prop="orderQty" width="100" align="right" />
              <el-table-column label="发货数量" prop="shipQty" width="100" align="right" />
              <el-table-column label="退货数量" prop="returnQty" width="100" align="right" />
              <el-table-column label="单价" width="100" align="right"><template #default="scope"><span class="rd-amount">{{ formatAmount(scope.row.unitPrice) }}</span></template></el-table-column>
              <el-table-column label="退货金额" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatAmount(scope.row.returnAmount) }}</span></template></el-table-column>
              <el-table-column label="退货原因" prop="returnReason" min-width="120" show-overflow-tooltip />
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <line x1="8" y1="6" x2="21" y2="6"/>
                <line x1="8" y1="12" x2="21" y2="12"/>
                <line x1="8" y1="18" x2="21" y2="18"/>
                <line x1="3" y1="6" x2="3.01" y2="6"/>
                <line x1="3" y1="12" x2="3.01" y2="12"/>
                <line x1="3" y1="18" x2="3.01" y2="18"/>
              </svg>
              <p class="rd-empty-text">暂无退货明细</p>
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

        <!-- 审核记录 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewAudit')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 11l3 3L22 4"/>
                  <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                </svg>
              </span>
              审核记录
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewAudit }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewAudit" style="display:block">
            <div class="rd-timeline" v-if="viewForm.approveLogList && viewForm.approveLogList.length">
              <div class="rd-timeline-item" v-for="log in viewForm.approveLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.actionType === '2', 'rd-timeline-dot--error': log.actionType === '3' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.actionType === '1'" type="primary" size="small" effect="light" round>提交审批</el-tag>
                      <el-tag v-else-if="log.actionType === '2'" type="success" size="small" effect="light" round>审批通过</el-tag>
                      <el-tag v-else-if="log.actionType === '3'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.approveTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ log.approveBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.approveOpinion">
                    <strong>审批意见：</strong>{{ log.approveOpinion }}
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
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MkReturn">
import { listReturn, getReturn, delReturn, addReturn, updateReturn, submitReturn, approveReturn, refundReturn } from '@/api/mk/returnOrder'
import { listOrder, getOrder } from '@/api/mk/order'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown } from '@element-plus/icons-vue'

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
const showStatusHelp = ref(false)
const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 })
const statusTabList = computed(() => marketing_return_status.value)
function loadStatusCounts() {
  const counts = { all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 }
  list.value.forEach(row => {
    const s = row.returnStatus
    if (counts[s] !== undefined) counts[s]++
  })
  counts.all = total.value
  statusCounts.value = counts
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.returnStatus = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'blue', '1': 'green', '2': 'amber', '3': 'gray', '4': 'red' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_return_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-audit', '1': 'tab-approved', '2': 'tab-draft', '3': 'tab-void', '4': 'tab-reject' }; return map[value] || '' }
const viewForm = ref({})
const refundOpen = ref(false)
const refundForm = ref({})
const approveOpen = ref(false)
const approveForm = ref({})
const addOpen = ref(false)
const addForm = ref({})
const addTitle = ref('新增退货')
const isEdit = ref(false)
const orderOptions = ref([])
const collapsedCards = reactive({ basic: false, reason: false, approve: false, refund: false, approveReturn: false, approveItems: false, approveReview: false, refundReturn: false, refundInfo: false, addOrder: false, addItems: false, addReturn: false, viewItems: false, editAudit: false, approveAudit: false, viewAudit: false })
function toggleCard(name) { collapsedCards[name] = !collapsedCards[name] }

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, returnNo: undefined, customerName: undefined, returnStatus: undefined, orderNo: undefined, returnReason: undefined, params: {} }
})
const { queryParams } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  returnNo: { label: '退货编号', visible: true },
  orderNo: { label: '订单编号', visible: true },
  customerName: { label: '客户名称', visible: true },
  returnAmount: { label: '退货金额', visible: true },
  returnReason: { label: '退货原因', visible: true },
  returnStatus: { label: '退货状态', visible: true },
  refundAmount: { label: '退款金额', visible: true },
  refundDate: { label: '退款日期', visible: true },
  approveBy: { label: '审批人', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_return_columns')
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

const dateRange = ref([])
const dateRangeCreateTime = ref([])
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.returnNo) count++
  if (queryParams.value.customerName) count++
  if (queryParams.value.returnStatus) count++
  if (queryParams.value.orderNo) count++
  if (queryParams.value.returnReason) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  if (dateRangeCreateTime.value && dateRangeCreateTime.value.length === 2) count++
  return count
})

function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }

const refundRules = {
  refundAmount: [{ required: true, message: '退款金额不能为空', trigger: 'blur' }],
  refundDate: [{ required: true, message: '请选择退款日期', trigger: 'change' }]
}

const addRules = {
  orderId: [{ required: true, message: '请选择关联订单', trigger: 'change' }],
  returnReason: [{ required: true, message: '请输入退货原因', trigger: 'blur' }]
}

/** 退货金额合计（自动计算） */
const totalReturnAmount = computed(() => {
  if (!addForm.value.detailList || !addForm.value.detailList.length) return 0
  return addForm.value.detailList.reduce((sum, item) => sum + (Number(item.returnAmount) || 0), 0)
})

function getList() { loading.value = true; listReturn(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false }) }
function handleQuery() { showAdvanced.value = false; queryParams.value.params = proxy.addDateRange(proxy.addDateRange(queryParams.value.params, dateRange.value, 'Refund'), dateRangeCreateTime.value, 'CreateTime'); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.returnNo = undefined; queryParams.value.customerName = undefined; queryParams.value.returnStatus = undefined; queryParams.value.orderNo = undefined; queryParams.value.returnReason = undefined; dateRange.value = []; dateRangeCreateTime.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.returnId); single.value = selection.length !== 1; multiple.value = !selection.length }
function formatAmount(val) { if (val == null || val === '') return '-'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }
function handleView(row) { getReturn(row.returnId).then(res => { viewForm.value = res.data; collapsedCards.refund = !(res.data.refundAmount != null && res.data.refundAmount !== ''); viewOpen.value = true }) }
function handleDelete(row) { const returnIds = row.returnId || ids.value; proxy.$modal.confirm('确认删除选中的退货记录？').then(() => delReturn(returnIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交编号为"' + row.returnNo + '"的退货单审批？').then(() => submitReturn(row.returnId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功，等待审批') }).catch(() => {}) }
function handleApprove(row) { getReturn(row.returnId).then(res => { approveForm.value = res.data; approveForm.value.approveOpinion = ''; approveOpen.value = true }) }
function submitApprove(approved) { proxy.$refs['approveRef'].validate(valid => { if (valid) { approveReturn(approveForm.value.returnId, approved, approveForm.value.approveOpinion || '').then(() => { proxy.$modal.msgSuccess(approved ? '审批通过' : '审批驳回'); approveOpen.value = false; getList() }) } }) }
function handleRefund(row) { refundForm.value = { returnId: row.returnId, returnNo: row.returnNo, returnAmount: row.returnAmount, refundAmount: row.returnAmount, refundDate: undefined }; refundOpen.value = true }
function submitRefund() { proxy.$refs['refundRef'].validate(valid => { if (valid) { refundReturn(refundForm.value.returnId, refundForm.value.refundAmount, refundForm.value.refundDate).then(() => { proxy.$modal.msgSuccess('退款成功'); refundOpen.value = false; getList() }) } }) }
function handleExport() { proxy.download('mk/return/export', { ...proxy.addDateRange(proxy.addDateRange(queryParams.value, dateRange.value, 'Refund'), dateRangeCreateTime.value, 'CreateTime') }, `return_${new Date().getTime()}.xlsx`) }

/** 新增退货 */
function handleAdd() {
  isEdit.value = false
  addTitle.value = '新增退货'
  addForm.value = { orderId: undefined, orderNo: undefined, customerId: undefined, customerName: undefined, returnReason: undefined, remark: undefined, detailList: [] }
  proxy.resetForm('addRef')
  addOpen.value = true
}

/** 修改退货（驳回后修改） */
function handleEdit(row) {
  getReturn(row.returnId).then(res => {
    const data = res.data
    isEdit.value = true
    addTitle.value = '修改退货'
    addForm.value = {
      returnId: data.returnId,
      returnNo: data.returnNo,
      returnStatus: data.returnStatus,
      approveLogList: data.approveLogList || [],
      orderId: data.orderId,
      orderNo: data.orderNo,
      customerId: data.customerId,
      customerName: data.customerName,
      returnReason: data.returnReason,
      remark: data.remark,
      detailList: (data.detailList || []).map(item => ({
        orderItemId: item.orderItemId,
        productName: item.productName,
        productSpec: item.productSpec,
        unit: item.unit,
        orderQty: item.orderQty,
        shipQty: item.shipQty,
        returnQty: item.returnQty,
        unitPrice: item.unitPrice,
        returnAmount: item.returnAmount,
        returnReason: item.returnReason || ''
      }))
    }
    proxy.resetForm('addRef')
    addOpen.value = true
  })
}

/** 加载订单选项 */
function loadOrderOptions() {
  listOrder({ pageNum: 1, pageSize: 9999 }).then(res => { orderOptions.value = res.rows || [] })
}

/** 选择订单后自动带出客户信息和物料明细 */
function onOrderChange(val) {
  if (!val) {
    addForm.value.orderId = undefined
    addForm.value.orderNo = undefined
    addForm.value.customerId = undefined
    addForm.value.customerName = undefined
    addForm.value.detailList = []
    return
  }
  getOrder(val).then(res => {
    const order = res.data
    addForm.value.orderId = order.orderId
    addForm.value.orderNo = order.orderNo
    addForm.value.customerId = order.customerId
    addForm.value.customerName = order.customerName
    // 从订单明细带出退货明细
    if (order.itemList && order.itemList.length > 0) {
      addForm.value.detailList = order.itemList.map(item => ({
        orderItemId: item.itemId,
        productName: item.productName,
        productSpec: item.productSpec,
        unit: item.unit,
        orderQty: item.quantity,
        shipQty: item.shippedQty,
        returnQty: undefined,
        unitPrice: item.unitPrice,
        returnAmount: 0,
        returnReason: ''
      }))
    } else {
      addForm.value.detailList = []
      proxy.$modal.msgWarning('该订单暂无物料明细')
    }
  })
}

/** 计算退货明细金额 */
function calcReturnAmount() {
  if (addForm.value.detailList) {
    addForm.value.detailList.forEach(item => {
      if (item.returnQty != null && item.unitPrice != null) {
        item.returnAmount = parseFloat((item.returnQty * item.unitPrice).toFixed(2))
      }
    })
  }
}

/** 删除退货明细行 */
function handleDeleteDetail(index) {
  addForm.value.detailList.splice(index, 1)
}

/** 提交新增/修改退货 */
function submitAdd() {
  proxy.$refs['addRef'].validate(valid => {
    if (valid) {
      // 校验退货明细：至少保留一条、退货数量不能为0且不能超过订单数量
      const allItems = addForm.value.detailList || []
      if (allItems.length === 0) {
        proxy.$modal.msgError('请至少保留一条退货明细')
        return
      }
      for (let i = 0; i < allItems.length; i++) {
        const item = allItems[i]
        if (item.returnQty == null || item.returnQty === 0) {
          proxy.$modal.msgError(`第${i + 1}行退货数量不能为0`)
          return
        }
        if (item.orderQty != null && item.returnQty > item.orderQty) {
          proxy.$modal.msgError(`第${i + 1}行退货数量不能大于订单数量（${item.orderQty}）`)
          return
        }
      }
      const validItems = allItems
      const payload = {
        orderId: addForm.value.orderId,
        orderNo: addForm.value.orderNo,
        customerId: addForm.value.customerId,
        customerName: addForm.value.customerName,
        returnAmount: totalReturnAmount.value,
        returnReason: addForm.value.returnReason,
        returnStatus: '4',
        remark: addForm.value.remark,
          detailList: validItems.map(d => ({
            orderItemId: d.orderItemId,
            productName: d.productName,
            productSpec: d.productSpec,
            unit: d.unit,
            orderQty: d.orderQty,
            shipQty: d.shipQty,
            returnQty: d.returnQty,
            unitPrice: d.unitPrice,
            returnAmount: d.returnAmount,
            returnReason: d.returnReason
          }))
      }
      if (isEdit.value) {
        payload.returnId = addForm.value.returnId
        proxy.$modal.confirm('确认修改退货单？修改后退货单将回到草稿状态。').then(() => {
          return updateReturn(payload)
        }).then(() => {
          proxy.$modal.msgSuccess('修改成功，退货单已回到草稿状态')
          addOpen.value = false
          getList()
        }).catch(() => {})
      } else {
        proxy.$modal.confirm('确认发起退货？退货单创建后将保存为草稿状态，可在退货管理页面提交审批。').then(() => {
          return addReturn(payload)
        }).then(() => {
          proxy.$modal.msgSuccess('退货单已创建（草稿状态），可提交审批')
          addOpen.value = false
          getList()
        }).catch(() => {})
      }
    }
  })
}

loadOrderOptions()
getList()
</script>

<style scoped>
/* 卡片式样式使用全局 detail-page.scss，此处仅保留页面特有样式 */
.reject-alert {
  margin-bottom: 16px;
}

.status-help-content {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}

.status-help-content h4 {
  margin: 20px 0 12px 0;
  color: #303133;
  font-weight: 600;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.status-help-content h4:first-child {
  margin-top: 0;
}

.status-flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flow-arrow {
  color: #909399;
  font-size: 16px;
}

:deep(.el-timeline-item__node) {
  background-color: transparent;
  border: 2px solid;
}

:deep(.el-timeline-item__node--primary) {
  border-color: #409eff;
}

:deep(.el-timeline-item__node--success) {
  border-color: #67c23a;
}

:deep(.el-timeline-item__node--warning) {
  border-color: #e6a23c;
}

:deep(.el-timeline-item__node--danger) {
  border-color: #f56c6c;
}

:deep(.el-timeline-item__node--info) {
  border-color: #909399;
}
</style>
