<template>
  <div class="app-container mk-payment-page">
    <!-- 回款统计卡片 -->
    <el-row :gutter="12" class="mb8" v-if="stats">
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">总计划回款</div>
          <div class="stat-value">{{ formatMoney(stats.totalPlanAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card stat-success">
          <div class="stat-label">已回款</div>
          <div class="stat-value">{{ formatMoney(stats.totalActualAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card stat-warning">
          <div class="stat-label">待回款</div>
          <div class="stat-value">{{ formatMoney(stats.totalPendingAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card stat-danger">
          <div class="stat-label">逾期金额</div>
          <div class="stat-value">{{ formatMoney(stats.totalOverdueAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">回款率</div>
          <div class="stat-value">{{ stats.paymentRate }}%</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">逾期笔数</div>
          <div class="stat-value">{{ stats.overdueCount }}</div>
        </div>
      </el-col>
    </el-row>

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
          <label>合同编号</label>
          <div class="control">
            <el-input v-model="queryParams.contractNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <label>回款状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.paymentStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_payment_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>逾期</label>
          <div class="control is-select">
            <el-select v-model="queryParams.overdueFlag" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option label="未逾期" value="0" />
              <el-option label="已逾期" value="1" />
            </el-select>
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
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:payment:add']">新增回款计划</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:payment:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:payment:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_payment_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="合同编号" prop="contractNo" key="contractNo" :width="colWidth('contractNo', 150)" resizable v-if="columns.contractNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.contractNo }}</span></template>
          </el-table-column>
          <el-table-column label="客户名称" prop="customerName" key="customerName" :width="colWidth('customerName', 200)" resizable show-overflow-tooltip v-if="columns.customerName.visible" />
          <el-table-column label="期次" prop="periodNo" key="periodNo" :width="colWidth('periodNo', 80)" resizable align="center" v-if="columns.periodNo.visible" />
          <el-table-column label="计划回款金额" prop="planAmount" key="planAmount" :width="colWidth('planAmount', 140)" resizable align="right" sortable="custom" class-name="col-num" v-if="columns.planAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.planAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="计划回款日期" prop="planDate" key="planDate" :width="colWidth('planDate', 130)" resizable align="center" sortable="custom" v-if="columns.planDate.visible" />
          <el-table-column label="实际回款金额" prop="actualAmount" key="actualAmount" :width="colWidth('actualAmount', 140)" resizable align="right" sortable="custom" class-name="col-num" v-if="columns.actualAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.actualAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="实际回款日期" prop="actualDate" key="actualDate" :width="colWidth('actualDate', 130)" resizable align="center" v-if="columns.actualDate.visible" />
          <el-table-column label="回款方式" prop="paymentMethod" key="paymentMethod" :width="colWidth('paymentMethod', 100)" resizable align="center" v-if="columns.paymentMethod.visible">
            <template #default="scope">
              <span class="badge violet">{{ methodLabel(scope.row.paymentMethod) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="paymentStatus" key="paymentStatus" :width="colWidth('paymentStatus', 100)" resizable align="center" sortable="custom" v-if="columns.paymentStatus.visible">
            <template #default="scope">
              <span class="badge" :class="payStatusBadgeClass(scope.row.paymentStatus)">
                <span class="dot"></span>{{ payStatusLabel(scope.row.paymentStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="逾期" prop="overdueFlag" key="overdueFlag" :width="colWidth('overdueFlag', 80)" resizable align="center" v-if="columns.overdueFlag.visible">
            <template #default="scope">
              <span class="badge red" v-if="scope.row.overdueFlag === '1'">逾期</span>
              <span v-else style="color: var(--ink-400)">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
              <el-button link type="success" icon="Money" @click="handleRecord(scope.row)" v-if="scope.row.paymentStatus !== '1' && !hasPendingRecord(scope.row)" v-hasPermi="['marketing:payment:edit']">回款登记</el-button>
              <el-button link type="warning" icon="Check" @click="handleConfirm(scope.row)" v-if="hasPendingRecord(scope.row)" v-hasPermi="['marketing:payment:confirm']">回款确认</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="回款管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、回款计划状态流转</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">待回款</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">部分回款</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已回款</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">待回款</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已逾期</el-tag>
            <el-tag size="small" type="danger">超过计划回款日期</el-tag>
          </div>
        </div>

        <h4>二、回款记录确认流程</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待确认</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已确认</el-tag>
            <el-tag size="small" type="success">更新计划金额</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">待确认</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-tag size="small" type="info">可重新登记</el-tag>
          </div>
        </div>

        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待回款">
            回款计划已创建，尚未有确认的回款记录
          </el-descriptions-item>
          <el-descriptions-item label="部分回款">
            已有部分回款记录被确认，但累计确认金额未达到计划回款金额
          </el-descriptions-item>
          <el-descriptions-item label="已回款">
            累计确认回款金额已达到或超过计划回款金额，回款计划完成
          </el-descriptions-item>
          <el-descriptions-item label="已逾期">
            超过计划回款日期仍未完成回款，系统自动标记逾期状态
          </el-descriptions-item>
        </el-descriptions>

        <h4>四、业务规则</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>回款计划创建：</strong>选择关联客户和合同后创建回款计划，填写期次、计划回款金额、计划回款日期、回款方式和收款银行账户
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>多次回款登记：</strong>同一回款计划支持多次回款登记。每笔回款登记后状态为"待确认"，需等待财务确认后才能登记下一笔
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>回款确认：</strong>财务专员确认回款记录后，系统自动更新回款计划的累计实际回款金额、实际回款日期和状态。确认金额达计划金额时状态变为"已回款"
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>回款驳回：</strong>财务专员可驳回待确认的回款记录，驳回后该笔记录无效，可重新登记新的回款记录
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>逾期预警：</strong>超过计划回款日期且未完成回款的计划会标记为逾期，页面顶部统计卡片实时展示逾期金额和笔数
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>回款统计：</strong>页面顶部统计卡片展示总计划回款、已回款、待回款、逾期金额、回款率和逾期笔数，支持实时刷新
          </el-timeline-item>
        </el-timeline>

        <h4>五、操作权限</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="新增回款计划">营销总监、销售经理、销售代表可操作</el-descriptions-item>
          <el-descriptions-item label="回款登记">营销总监、销售经理可操作。待回款或部分回款状态可登记，已回款状态不可登记</el-descriptions-item>
          <el-descriptions-item label="回款确认">仅营销总监、财务专员可确认或驳回</el-descriptions-item>
          <el-descriptions-item label="删除回款计划">营销总监、销售经理可操作。已确认的回款记录不允许删除</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 新增回款计划对话框 -->
    <el-dialog v-model="addOpen" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="1" x2="12" y2="23"/>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">新增回款计划</span>
        </div>
      </template>
      <el-form ref="addRef" :model="addForm" :rules="addRules" label-width="120px">
        <div class="rd-page">
          <!-- 关联信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addRelation')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                    <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                  </svg>
                </span>
                关联信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addRelation }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addRelation">
              <el-row>
                <el-col :span="12"><el-form-item label="关联客户" prop="customerId">
                  <el-select v-model="addForm.customerId" filterable placeholder="请选择客户" style="width: 100%" @change="onCustomerChange">
                    <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="关联合同" prop="contractId">
                  <el-select v-model="addForm.contractId" filterable clearable placeholder="可选" style="width: 100%" @change="onContractChange">
                    <el-option v-for="c in contractOptions" :key="c.contractId" :label="c.contractNo + ' - ' + c.contractName" :value="c.contractId" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 计划信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addPlan')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                </span>
                计划信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addPlan }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addPlan">
              <el-row>
                <el-col :span="8"><el-form-item label="期次" prop="periodNo"><el-input-number v-model="addForm.periodNo" :min="1" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="计划回款金额" prop="planAmount"><el-input-number v-model="addForm.planAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="计划回款日期" prop="planDate"><el-date-picker v-model="addForm.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="回款方式" prop="paymentMethod">
                  <el-select v-model="addForm.paymentMethod" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="收款银行账户" prop="bankAccount"><el-input v-model="addForm.bankAccount" placeholder="请输入银行账户" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addOther')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="16" x2="12" y2="12"/>
                    <line x1="12" y1="8" x2="12.01" y2="8"/>
                  </svg>
                </span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addOther }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addOther">
              <el-form-item label="备注" prop="remark"><el-input v-model="addForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="addOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 回款登记对话框（多次回款） -->
    <el-dialog v-model="recordOpen" width="1000px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="5" width="20" height="14" rx="2"/>
              <line x1="2" y1="10" x2="22" y2="10"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">回款登记</span>
          <div class="rd-detail-header-sub" v-if="recordForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">合同：{{ recordForm.contractNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 计划信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('recordPlan')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </span>
              计划信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.recordPlan }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.recordPlan">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ recordForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ recordForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">期次</span><div class="rd-value">{{ recordForm.periodNo }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回款金额</span><div class="rd-value rd-value--large rd-amount">{{ formatMoney(recordForm.planAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">已回款金额</span><div class="rd-value rd-amount">{{ formatMoney(recordForm.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">待回款金额</span><div class="rd-value rd-value--large rd-amount">{{ formatMoney(recordForm.planAmount - recordForm.actualAmount) }}</div></div>
            </div>
          </div>
        </section>

        <!-- 回款登记 -->
        <section class="rd-card" v-if="recordForm.paymentStatus !== '1'">
          <div class="rd-card-header" @click="toggleCard('recordForm')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="18" x2="12" y2="12"/>
                  <line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </span>
              回款登记
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.recordForm }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.recordForm">
            <el-form ref="recordFormRef" :model="recordFormData" :rules="recordRules" label-width="100px">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="回款金额" prop="thisAmount">
                    <el-input-number v-model="recordFormData.thisAmount" :min="0" :precision="2" :controls="true" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回款日期" prop="paymentDate">
                    <el-date-picker v-model="recordFormData.paymentDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回款方式" prop="paymentMethod">
                    <el-select v-model="recordFormData.paymentMethod" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in marketing_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="银行账户" prop="bankAccount">
                    <el-input v-model="recordFormData.bankAccount" placeholder="请输入银行账户" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="收款凭证" prop="receiptAttachment">
                    <file-upload v-model="recordFormData.receiptAttachment" :fileSize="20" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="recordFormData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </section>

        <!-- 回款完成提示 -->
        <section class="rd-card" v-if="recordForm.paymentStatus === '1'">
          <div class="rd-card-body">
            <el-result icon="success" title="回款已完成" sub-title="该回款计划已全部回款，无需继续登记">
              <template #extra>
                <el-button @click="recordOpen = false">关 闭</el-button>
              </template>
            </el-result>
          </div>
        </section>

        <!-- 已登记的回款记录 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('recordHistory')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M3 3v5h5"/>
                  <path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/>
                  <path d="M12 7v5l4 2"/>
                </svg>
              </span>
              已登记的回款记录
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.recordHistory }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.recordHistory">
            <el-table border :data="existingRecords" size="small" v-if="existingRecords.length > 0">
              <el-table-column label="回款金额" prop="thisAmount" min-width="120" align="center" />
              <el-table-column label="回款日期" prop="paymentDate" min-width="120" align="center" />
              <el-table-column label="回款方式" min-width="100" align="center">
                <template #default="scope"><dict-tag :options="marketing_payment_method" :value="scope.row.paymentMethod" /></template>
              </el-table-column>
              <el-table-column label="确认状态" min-width="100" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.confirmStatus === '0'" type="warning" size="small">待确认</el-tag>
                  <el-tag v-else-if="scope.row.confirmStatus === '1'" type="success" size="small">已确认</el-tag>
                  <el-tag v-else type="danger" size="small">已驳回</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="确认人" prop="confirmBy" min-width="100" align="center" />
              <el-table-column label="凭证" min-width="120" align="center">
                <template #default="scope">
                  <div v-if="scope.row.receiptAttachment" style="display:flex;gap:4px;justify-content:center;align-items:center">
                    <el-link :href="baseUrl + scope.row.receiptAttachment" target="_blank" type="primary"><el-icon><View /></el-icon></el-link>
                    <el-link type="success" :underline="false" @click="handlePreview(scope.row.receiptAttachment)">预览</el-link>
                  </div>
                  <span v-else style="color: #c0c4cc">-</span>
                </template>
              </el-table-column>
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <p class="rd-empty-text">暂无回款记录</p>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitRecord" v-if="recordForm.paymentStatus !== '1'">提交回款</el-button>
        <el-button @click="recordOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="1" x2="12" y2="23"/>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">回款计划详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">合同：{{ viewForm.contractNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 关联合同 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewContract')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
              </span>
              关联合同
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewContract }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewContract">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
            </div>
          </div>
        </section>

        <!-- 回款计划信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewPlan')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </span>
              回款计划信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewPlan }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewPlan">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">期次</span><div class="rd-value">{{ viewForm.periodNo }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回款金额</span><div class="rd-value rd-value--large rd-amount">{{ formatMoney(viewForm.planAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回款日期</span><div class="rd-value">{{ viewForm.planDate }}</div></div>
              <div class="rd-item"><span class="rd-label">回款方式</span><div class="rd-value"><dict-tag :options="marketing_payment_method" :value="viewForm.paymentMethod" /></div></div>
              <div class="rd-item"><span class="rd-label">收款银行账户</span><div class="rd-value">{{ viewForm.bankAccount }}</div></div>
              <div class="rd-item"><span class="rd-label">逾期标志</span><div class="rd-value"><el-tag v-if="viewForm.overdueFlag === '1'" type="danger" size="small">已逾期</el-tag><span v-else>未逾期</span></div></div>
            </div>
          </div>
        </section>

        <!-- 实际回款信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewActual')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                  <polyline points="22 4 12 14.01 9 11.01"/>
                </svg>
              </span>
              实际回款信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewActual }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewActual">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">实际回款金额</span><div class="rd-value rd-value--large rd-amount">{{ formatMoney(viewForm.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">实际回款日期</span><div class="rd-value">{{ viewForm.actualDate }}</div></div>
              <div class="rd-item"><span class="rd-label">回款状态</span><div class="rd-value"><dict-tag :options="marketing_payment_status" :value="viewForm.paymentStatus" /></div></div>
            </div>
          </div>
        </section>

        <!-- 回款记录明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewRecords')">
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
              回款记录明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewRecords }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewRecords">
            <el-table border :data="viewForm.recordList" size="small" v-if="viewForm.recordList && viewForm.recordList.length > 0">
              <el-table-column label="回款金额" prop="thisAmount" width="120" align="center" />
              <el-table-column label="回款日期" prop="paymentDate" width="120" align="center" />
              <el-table-column label="回款方式" width="100" align="center">
                <template #default="scope"><dict-tag :options="marketing_payment_method" :value="scope.row.paymentMethod" /></template>
              </el-table-column>
              <el-table-column label="确认状态" width="100" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.confirmStatus === '0'" type="warning" size="small">待确认</el-tag>
                  <el-tag v-else-if="scope.row.confirmStatus === '1'" type="success" size="small">已确认</el-tag>
                  <el-tag v-else type="danger" size="small">已驳回</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="确认人" prop="confirmBy" width="100" align="center" />
              <el-table-column label="凭证" width="120" align="center">
                <template #default="scope">
                  <div v-if="scope.row.receiptAttachment" style="display:flex;gap:4px;justify-content:center;align-items:center">
                    <el-link :href="baseUrl + scope.row.receiptAttachment" target="_blank" type="primary"><el-icon><View /></el-icon></el-link>
                    <el-link type="success" :underline="false" @click="handlePreview(scope.row.receiptAttachment)">预览</el-link>
                  </div>
                  <span v-else style="color: #c0c4cc">-</span>
                </template>
              </el-table-column>
              <el-table-column label="备注" prop="remark" show-overflow-tooltip />
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <p class="rd-empty-text">暂无回款记录</p>
            </div>
          </div>
        </section>

        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewOther')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="16" x2="12" y2="12"/>
                  <line x1="12" y1="8" x2="12.01" y2="8"/>
                </svg>
              </span>
              其他信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOther }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewOther">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无备注' }}</div></div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>

    <!-- 回款确认对话框 -->
    <el-dialog v-model="confirmOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11l3 3L22 4"/>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">回款确认</span>
          <div class="rd-detail-header-sub" v-if="confirmPlanForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">合同：{{ confirmPlanForm.contractNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 计划信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('confirmPlan')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </span>
              计划信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.confirmPlan }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.confirmPlan">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ confirmPlanForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ confirmPlanForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">期次</span><div class="rd-value">{{ confirmPlanForm.periodNo }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回款金额</span><div class="rd-value rd-value--large rd-amount">{{ formatMoney(confirmPlanForm.planAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">已确认金额</span><div class="rd-value rd-amount">{{ formatMoney(confirmPlanForm.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">待确认金额</span><div class="rd-value rd-value--large rd-amount">{{ formatMoney((confirmPlanForm.planAmount || 0) - (confirmPlanForm.actualAmount || 0)) }}</div></div>
            </div>
          </div>
        </section>

        <!-- 待确认回款信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('confirmRecord')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="18" x2="12" y2="12"/>
                  <line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </span>
              待确认回款信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.confirmRecord }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.confirmRecord">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">本次回款金额</span><div class="rd-value rd-value--large rd-amount">{{ formatMoney(confirmRecordForm.thisAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">回款日期</span><div class="rd-value">{{ confirmRecordForm.paymentDate }}</div></div>
              <div class="rd-item"><span class="rd-label">回款方式</span><div class="rd-value"><dict-tag :options="marketing_payment_method" :value="confirmRecordForm.paymentMethod" /></div></div>
              <div class="rd-item"><span class="rd-label">银行账户</span><div class="rd-value">{{ confirmRecordForm.bankAccount }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">收款凭证</span><div class="rd-value"><template v-if="confirmRecordForm.receiptAttachment"><el-link :href="baseUrl + confirmRecordForm.receiptAttachment" target="_blank" type="primary">查看凭证</el-link><el-button link type="success" icon="View" size="small" style="margin-left: 12px" @click="handlePreview(confirmRecordForm.receiptAttachment)">预览</el-button></template><span v-else style="color: #c0c4cc">-</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !confirmRecordForm.remark }">{{ confirmRecordForm.remark || '暂无' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="submitConfirm('1')" v-hasPermi="['marketing:payment:confirm']">确认回款</el-button>
        <el-button type="danger" @click="submitConfirm('2')" v-hasPermi="['marketing:payment:confirm']">驳 回</el-button>
        <el-button @click="confirmOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
    <!-- 文件预览 -->
    <file-preview ref="filePreviewRef" />
  </div>
</template>

<script setup name="MkPayment">
import { listPayment, getPayment, addPayment, delPayment, paymentStatistics, listPaymentRecord, addPaymentRecord, delPaymentRecord, confirmPaymentRecord } from '@/api/mk/payment'
import { listContract } from '@/api/mk/contract'
import { listCustomer } from '@/api/mk/customer'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatMoney } from '@/composables/useDetailCard'
import { ArrowRight, ArrowDown, Search, Filter, WarningFilled, Delete, Download, RefreshLeft } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { marketing_payment_status, marketing_payment_method } = proxy.useDict('marketing_payment_status', 'marketing_payment_method')

const { collapsedCards, toggleCard } = useDetailCard(["addRelation","addPlan","addOther","recordPlan","recordForm","recordHistory","viewContract","viewPlan","viewActual","viewRecords","viewOther","confirmPlan","confirmRecord"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_payment_index')

const list = ref([])
const addOpen = ref(false)
const recordOpen = ref(false)
const viewOpen = ref(false)
const showStatusHelp = ref(false)
const confirmOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const viewForm = ref({})
const stats = ref(null)
const customerOptions = ref([])
const contractOptions = ref([])
const existingRecords = ref([])
const recordForm = ref({})
const recordFormData = ref({})
const confirmPlanForm = ref({})
const confirmRecordForm = ref({})
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0 })
const baseUrl = import.meta.env.VITE_APP_BASE_API
function getFileName(name) { if (name && name.lastIndexOf('/') > -1) { return name.slice(name.lastIndexOf('/') + 1) } return name || '' }
function handlePreview(fileUrl) { proxy.$refs.filePreviewRef.open(fileUrl, getFileName(fileUrl)) }

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  contractNo: { label: '合同编号', visible: true },
  customerName: { label: '客户名称', visible: true },
  periodNo: { label: '期次', visible: true },
  planAmount: { label: '计划回款金额', visible: true },
  planDate: { label: '计划回款日期', visible: true },
  actualAmount: { label: '实际回款金额', visible: true },
  actualDate: { label: '实际回款日期', visible: true },
  paymentMethod: { label: '回款方式', visible: true },
  paymentStatus: { label: '状态', visible: true },
  overdueFlag: { label: '逾期', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_payment_columns')
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

const data = reactive({
  addForm: {},
  queryParams: { pageNum: 1, pageSize: 10, contractNo: undefined, customerName: undefined, paymentStatus: undefined, overdueFlag: undefined, params: {} },
  addRules: {
    customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
    planAmount: [{ required: true, message: '计划回款金额不能为空', trigger: 'blur' }],
    planDate: [{ required: true, message: '请选择计划回款日期', trigger: 'change' }]
  },
  recordRules: {
    thisAmount: [{ required: true, message: '回款金额不能为空', trigger: 'blur' }],
    paymentDate: [{ required: true, message: '请选择回款日期', trigger: 'change' }],
    paymentMethod: [{ required: true, message: '请选择回款方式', trigger: 'change' }]
  }
})
const { queryParams, addForm, addRules, recordRules } = toRefs(data)

const statusTabList = computed(() => marketing_payment_status.value)
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.contractNo) count++
  if (queryParams.value.customerName) count++
  if (queryParams.value.paymentStatus) count++
  if (queryParams.value.overdueFlag) count++
  return count
})

// badge样式方法
function methodLabel(method) {
  const item = marketing_payment_method.value.find(d => d.value == method)
  return item ? item.label : '-'
}
function payStatusBadgeClass(status) {
  const map = { '0': 'amber', '1': 'green', '2': 'violet' }
  return map[status] || 'gray'
}
function payStatusLabel(status) {
  const item = marketing_payment_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-done', '2': 'tab-partial' }
  return map[value] || ''
}

function getList() {
  loading.value = true
  listPayment(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function loadStatusCounts() {
  listPayment({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.paymentStatus] !== undefined) counts[r.paymentStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function loadStats() {
  paymentStatistics().then(res => { stats.value = res.data }).catch(() => {})
}

function getCustomerOptions() {
  listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows })
}

function getContractOptions() {
  listContract({ pageNum: 1, pageSize: 9999 }).then(res => { contractOptions.value = res.rows })
}

function onCustomerChange(customerId) {
  if (customerId) {
    const customer = customerOptions.value.find(c => c.customerId === customerId)
    if (customer) addForm.value.customerName = customer.customerName
  }
}

function onContractChange(contractId) {
  if (contractId) {
    const contract = contractOptions.value.find(c => c.contractId === contractId)
    if (contract) {
      addForm.value.contractNo = contract.contractNo
      addForm.value.customerId = contract.customerId
      addForm.value.customerName = contract.customerName
    }
  }
}

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.contractNo = undefined; queryParams.value.customerName = undefined
  queryParams.value.paymentStatus = undefined; queryParams.value.overdueFlag = undefined
  queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery()
}
function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.paymentStatus = status === 'all' ? undefined : status
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.planId); single.value = selection.length !== 1; multiple.value = !selection.length }

function handleAdd() {
  addForm.value = { customerId: undefined, contractId: undefined, periodNo: 1, planAmount: 0, planDate: undefined, paymentMethod: '0', bankAccount: undefined, remark: undefined }
  proxy.resetForm('addRef')
  addOpen.value = true
}

function submitAdd() {
  proxy.$refs['addRef'].validate(valid => {
    if (valid) {
      addPayment(addForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        addOpen.value = false
        getList()
        loadStats()
      })
    }
  })
}

function handleDelete(row) {
  const planIds = row.planId || ids.value
  proxy.$modal.confirm('确认删除选中的回款计划？').then(() => delPayment(planIds)).then(() => { getList(); loadStats(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

function handleView(row) {
  getPayment(row.planId).then(res => { viewForm.value = res.data; viewOpen.value = true })
}

function handleRecord(row) {
  getPayment(row.planId).then(res => {
    recordForm.value = res.data
    existingRecords.value = res.data.recordList || []
    // 初始化回款登记表单
    const remainingAmount = (res.data.planAmount || 0) - (res.data.actualAmount || 0)
    recordFormData.value = {
      planId: res.data.planId,
      contractId: res.data.contractId,
      customerId: res.data.customerId,
      thisAmount: remainingAmount > 0 ? remainingAmount : 0,
      paymentDate: undefined,
      paymentMethod: res.data.paymentMethod || '0',
      bankAccount: res.data.bankAccount,
      receiptAttachment: undefined,
      remark: undefined,
      confirmStatus: '0'
    }
    proxy.resetForm('recordFormRef')
    recordOpen.value = true
  })
}

function submitRecord() {
  proxy.$refs['recordFormRef'].validate(valid => {
    if (!valid) return
    const data = { ...recordFormData.value }
    if (!data.thisAmount || data.thisAmount <= 0) {
      proxy.$modal.msgWarning('回款金额必须大于0')
      return
    }
    addPaymentRecord(data).then(() => {
      proxy.$modal.msgSuccess('回款登记成功，请等待确认后再登记下一笔')
      getList()
      loadStats()
      // 登记后直接关闭对话框，需等确认后才能继续登记
      recordOpen.value = false
    })
  })
}

/** 检查是否有待确认的回款记录 */
function hasPendingRecord(row) {
  return row.recordList && row.recordList.some(r => r.confirmStatus === '0')
}

/** 打开回款确认对话框 */
function handleConfirm(row) {
  getPayment(row.planId).then(res => {
    confirmPlanForm.value = res.data
    // 找到待确认的那笔记录
    const pending = (res.data.recordList || []).find(r => r.confirmStatus === '0')
    confirmRecordForm.value = pending || {}
    confirmOpen.value = true
  })
}

/** 提交确认/驳回 */
function submitConfirm(confirmStatus) {
  const msg = confirmStatus === '1' ? '确认' : '驳回'
  proxy.$modal.confirm('确认' + msg + '该笔回款记录？').then(() => {
    return confirmPaymentRecord(confirmRecordForm.value.recordId, confirmStatus, '')
  }).then(() => {
    proxy.$modal.msgSuccess(msg + '成功')
    confirmOpen.value = false
    getList()
    loadStats()
  }).catch(() => {})
}

function handleExport() { proxy.download('mk/payment/export', { ...queryParams.value }, `payment_${new Date().getTime()}.xlsx`) }

getCustomerOptions()
getContractOptions()
getList()
loadStats()
onActivated(() => { getList() })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.mk-payment-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Stat Cards ===== */
.mk-payment-page .stat-card {
  background:#fff;
  border:1px solid var(--ink-200);
  border-radius:var(--r-lg);
  box-shadow:var(--shadow-card);
  padding:16px 18px;
  text-align:center;
  transition:all .15s var(--ease-out);
}
.mk-payment-page .stat-card:hover {
  border-color:var(--brand-200);
  box-shadow:0 4px 12px -2px rgba(99,102,241,.12);
}
.mk-payment-page .stat-card .stat-label { font-size:13px; color:var(--ink-500); margin-bottom:8px; }
.mk-payment-page .stat-card .stat-value { font-size:22px; font-weight:700; color:var(--ink-900); font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.mk-payment-page .stat-card.stat-success .stat-value { color:var(--green-500); }
.mk-payment-page .stat-card.stat-warning .stat-value { color:var(--amber-500); }
.mk-payment-page .stat-card.stat-danger .stat-value { color:var(--red-500); }

/* ===== Surface Card ===== */
.mk-payment-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.mk-payment-page .filter-card { padding:14px 20px 16px; }
.mk-payment-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mk-payment-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mk-payment-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mk-payment-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mk-payment-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mk-payment-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mk-payment-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mk-payment-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mk-payment-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mk-payment-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mk-payment-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.mk-payment-page .field { display:flex; flex-direction:column; gap:6px; }
.mk-payment-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.mk-payment-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.mk-payment-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.mk-payment-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mk-payment-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mk-payment-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mk-payment-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mk-payment-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.mk-payment-page .field .control :deep(.el-select) { width:100%; }
.mk-payment-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mk-payment-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.mk-payment-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Status Tabs ===== */
.mk-payment-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mk-payment-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mk-payment-page .tabs-track::-webkit-scrollbar { display:none; }
.mk-payment-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mk-payment-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mk-payment-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.mk-payment-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mk-payment-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mk-payment-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mk-payment-page .status-tab.is-active .dot { background:var(--brand-500); }
.mk-payment-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.mk-payment-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.mk-payment-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.mk-payment-page .status-tab.tab-done .dot { background:var(--green-500); }
.mk-payment-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.mk-payment-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mk-payment-page .status-tab.tab-partial .dot { background:var(--brand-500); }
.mk-payment-page .status-tab.tab-partial .count { background:var(--violet-50); color:var(--brand-700); }
.mk-payment-page .status-tab.is-active.tab-partial .count { background:var(--brand-500); color:#fff; }

/* ===== Tip Pill ===== */
.mk-payment-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.mk-payment-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.mk-payment-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }

/* ===== Toolbar ===== */
.mk-payment-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mk-payment-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.mk-payment-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mk-payment-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.mk-payment-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.mk-payment-page .btn-soft .el-icon { font-size:14px; }
.mk-payment-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.mk-payment-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.mk-payment-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.mk-payment-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.mk-payment-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.mk-payment-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.mk-payment-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.mk-payment-page .table-wrap { overflow-x:auto; }
.mk-payment-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mk-payment-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mk-payment-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.mk-payment-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.mk-payment-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mk-payment-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.mk-payment-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mk-payment-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.mk-payment-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mk-payment-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.mk-payment-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.mk-payment-page .app-table :deep(.col-num) { text-align:right; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.mk-payment-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }

/* ===== Badges ===== */
.mk-payment-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mk-payment-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mk-payment-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.mk-payment-page .badge.amber .dot { background:var(--amber-500); }
.mk-payment-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.mk-payment-page .badge.blue .dot { background:var(--blue-500); }
.mk-payment-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.mk-payment-page .badge.green .dot { background:var(--green-500); }
.mk-payment-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.mk-payment-page .badge.red .dot { background:var(--red-500); }
.mk-payment-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.mk-payment-page .badge.violet .dot { background:var(--brand-500); }
.mk-payment-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.mk-payment-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.mk-payment-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.mk-payment-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.mk-payment-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.mk-payment-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.mk-payment-page .pagination-container :deep(.el-pagination .btn-prev), .mk-payment-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.mk-payment-page .pagination-container :deep(.el-pagination .btn-prev:hover), .mk-payment-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.mk-payment-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .mk-payment-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mk-payment-page .filter-card .filter-bar { grid-template-columns:1fr; } .mk-payment-page .toolbar { flex-wrap:wrap; gap:10px; } .mk-payment-page .status-tabs { padding:6px 8px; } }

/* ===== Status Help Dialog ===== */
.status-help-content { max-height:500px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.flow-item { display:flex; align-items:center; gap:8px; }
.flow-arrow { color:#909399; font-size:16px; }
:deep(.el-timeline-item__node) { background-color:transparent; border:2px solid; }
:deep(.el-timeline-item__node--primary) { border-color:#409eff; }
:deep(.el-timeline-item__node--success) { border-color:#67c23a; }
:deep(.el-timeline-item__node--warning) { border-color:#e6a23c; }
:deep(.el-timeline-item__node--danger) { border-color:#f56c6c; }
:deep(.el-timeline-item__node--info) { border-color:#909399; }
/* 卡片式样式使用全局 detail-page.scss */
</style>
