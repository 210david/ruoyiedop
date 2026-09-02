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
          <label>订单编号</label>
          <div class="control">
            <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <label>订单状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.orderStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_order_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>合同编号</label>
          <div class="control">
            <el-input v-model="queryParams.contractNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>负责人</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:order:add']">新增</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:order:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:order:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_order_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="订单编号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 150)" resizable v-if="columns.orderNo.visible" />
          <el-table-column label="合同编号" prop="contractNo" key="contractNo" :width="colWidth('contractNo', 150)" resizable v-if="columns.contractNo.visible" />
          <el-table-column label="客户名称" prop="customerName" key="customerName" show-overflow-tooltip v-if="columns.customerName.visible" />
          <el-table-column label="订单金额" prop="orderAmount" key="orderAmount" :width="colWidth('orderAmount', 120)" resizable align="right" v-if="columns.orderAmount.visible">
            <template #default="scope"><span class="rd-amount">￥{{ scope.row.orderAmount }}</span></template>
          </el-table-column>
          <el-table-column label="订单状态" prop="orderStatus" key="orderStatus" :width="colWidth('orderStatus', 100)" resizable align="center" v-if="columns.orderStatus.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.orderStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.orderStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="负责人" prop="userName" key="userName" :width="colWidth('userName', 100)" resizable v-if="columns.userName.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
        <template #default="scope">
          <div class="action-btn-row">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <!-- 草稿/已驳回：提交 -->
          <el-button v-if="scope.row.orderStatus === '0' || scope.row.orderStatus === '5'" link type="success" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['marketing:order:edit']">提交</el-button>
          <!-- 待审核：审核 -->
          <el-button v-if="scope.row.orderStatus === '1'" link type="primary" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['marketing:order:approve']">审核</el-button>
          <!-- 草稿/已驳回：修改 -->
          <el-button v-if="scope.row.orderStatus === '0' || scope.row.orderStatus === '5'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:order:edit']">修改</el-button>
          <!-- 草稿/待审核/已审核/部分发货：作废 -->
          <el-button v-if="scope.row.orderStatus === '0' || scope.row.orderStatus === '1' || scope.row.orderStatus === '2' || scope.row.orderStatus === '3'" link type="danger" icon="CircleClose" @click="handleVoid(scope.row)" v-hasPermi="['marketing:order:cancel']">作废</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="1200px" append-to-body draggable class="rd-dialog order-form-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/>
              <path d="M9 3V1h4v2"/>
              <path d="M9 3v6h4V3"/>
              <path d="M3 15h6v3H3z"/>
              <path d="M15 15h6v3h-6z"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ form.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 订单基本信息 -->
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
                订单基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row>
                <el-col :span="8"><el-form-item label="订单编号" prop="orderNo"><el-input v-model="form.orderNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="8"><el-form-item prop="contractId">
                  <template #label>
                    <span>关联合同</span>
                    <el-tooltip content="仅可选择审批通过且已生效的合同" placement="top">
                      <el-icon class="rd-form-tip"><question-filled /></el-icon>
                    </el-tooltip>
                  </template>
                  <el-input v-model="form.contractNo" readonly placeholder="请选择合同" style="width: 100%" @click="openContractPicker">
                    <template #append><el-button icon="Search" @click="openContractPicker" /></template>
                    <template #suffix>
                      <el-icon v-if="form.contractNo" class="clear-icon" @click.stop="clearContract"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="关联客户" prop="customerId">
                  <el-input v-model="form.customerName" readonly placeholder="请选择客户" style="width: 100%" @click="openCustomerPicker">
                    <template v-if="form.customerName" #append>
                      <el-button icon="CircleClose" @click.stop="clearCustomer" />
                    </template>
                    <template v-else #append>
                      <el-button icon="Search" @click="openCustomerPicker" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item prop="orderAmount">
                  <template #label>
                    <span>订单金额</span>
                    <el-tooltip content="根据订单明细自动计算" placement="top">
                      <el-icon class="rd-form-tip"><question-filled /></el-icon>
                    </el-tooltip>
                  </template>
                  <el-input-number v-model="form.orderAmount" :min="0" :precision="2" controls-position="right" disabled style="width: 100%" />
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="订单状态" prop="orderStatus">
                  <el-select v-model="form.orderStatus" disabled style="width: 100%">
                    <el-option v-for="d in marketing_order_status" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 订单明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('items')">
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
                订单明细
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.items }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.items">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddItem">添加明细</el-button></el-col>
              </el-row>
              <el-table border :data="form.itemList" size="small">
                <el-table-column label="行号" prop="lineNo" width="70" align="center" />
                <el-table-column label="物料编码" prop="materialCode" width="140" align="center">
                  <template #default="scope"><span>{{ scope.row.materialCode || '—' }}</span></template>
                </el-table-column>
                <el-table-column label="商品名称" min-width="180" align="center">
                  <template #default="scope">
                    <el-input :model-value="scope.row.materialCode ? scope.row.materialCode + ' - ' + scope.row.productName : ''" readonly size="small" placeholder="选择商品" @click="openMaterialPicker(scope.$index)">
                      <template v-if="scope.row.materialCode" #append><el-button icon="CircleClose" size="small" @click.stop="clearMaterial(scope.$index)" /></template>
                      <template v-else #append><el-button icon="Search" size="small" @click="openMaterialPicker(scope.$index)" /></template>
                    </el-input>
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" width="140" align="center">
                  <template #default="scope"><span>{{ scope.row.productSpec }}</span></template>
                </el-table-column>
                <el-table-column label="单位" width="90" align="center">
                  <template #default="scope">
                    <el-select v-model="scope.row.unit" size="small" placeholder="单位" style="width: 100%">
                      <el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="100" align="center">
                  <template #default="scope">
                    <el-form-item :prop="'itemList.' + scope.$index + '.quantity'" :rules="[{ required: true, message: '请输入数量', trigger: 'blur' }]" style="margin-bottom: 0" label-width="0">
                      <el-input-number v-model="scope.row.quantity" :min="1" :controls="false" size="small" placeholder="请输入" style="width: 100%" @change="calcSubtotal(scope.$index)" />
                    </el-form-item>
                  </template>
                </el-table-column>
                <el-table-column label="单价" width="120" align="center">
                  <template #default="scope">
                    <el-form-item :prop="'itemList.' + scope.$index + '.unitPrice'" :rules="[{ required: true, message: '请输入单价', trigger: 'blur' }]" style="margin-bottom: 0" label-width="0">
                      <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" :controls="false" size="small" placeholder="请输入" style="width: 100%" @change="calcSubtotal(scope.$index)" />
                    </el-form-item>
                  </template>
                </el-table-column>
                <el-table-column label="小计" width="120" align="center">
                  <template #default="scope">{{ formatAmount(scope.row.subtotal) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" size="small" @click="handleDeleteItem(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>

          <!-- 负责信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('owner')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                </span>
                负责信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.owner }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.owner">
              <el-row>
                <el-col :span="12"><el-form-item label="负责人" prop="userId">
                  <el-input v-model="form.userName" readonly placeholder="请选择负责人" style="width: 100%" @click="openUserPicker">
                    <template #append>
                      <el-button icon="Search" @click="openUserPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.userName" class="clear-icon" @click.stop="clearUser"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                  <el-input v-model="form.deptName" readonly placeholder="请选择部门" style="width: 100%" @click="openDeptPicker">
                    <template #append>
                      <el-button icon="Search" @click="openDeptPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.deptName" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('other')">
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
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>

          <!-- 审核记录（含驳回提示） -->
<section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
<div class="rd-card-header" @click="toggleCard('formAudit')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.formAudit }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
<div class="rd-card-body" v-show="!collapsedCards.formAudit" style="display:block">
              <el-alert v-if="form.orderStatus === '5'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该订单已被驳回，请根据审核意见修改后重新提交</template>
              </el-alert>
              <div class="rd-timeline">
                <div class="rd-timeline-item" v-for="log in form.auditLogList" :key="log.logId">
                  <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                  <div class="rd-timeline-content">
                    <div class="rd-timeline-header">
                      <span class="rd-timeline-title">
                        <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                        <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                      </span>
                      <span class="rd-timeline-time">{{ log.auditTime }}</span>
                    </div>
                    <div class="rd-timeline-body">
                      <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                    </div>
                    <div class="rd-timeline-comment" v-if="log.auditRemark">
                      <strong>审核意见：</strong>{{ log.auditRemark }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewOpen" width="1200px" append-to-body draggable class="rd-dialog order-detail-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/>
              <path d="M9 3V1h4v2"/>
              <path d="M9 3v6h4V3"/>
              <path d="M3 15h6v3H3z"/>
              <path d="M15 15h6v3h-6z"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">订单详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.orderNo }}</span>
            <dict-tag :options="marketing_order_status" :value="viewForm.orderStatus" />
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
              <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ viewForm.orderNo }}</div></div>
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">订单金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatAmount(viewForm.orderAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">订单状态</span><div class="rd-value"><dict-tag :options="marketing_order_status" :value="viewForm.orderStatus" /></div></div>
              <div class="rd-item" v-if="viewForm.finishTime"><span class="rd-label">完成时间</span><div class="rd-value">{{ viewForm.finishTime }}</div></div>
            </div>
          </div>
        </section>

        <!-- 订单明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('items')">
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
              订单明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.items }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.items">
            <el-table border :data="viewForm.itemList" size="small" v-if="viewForm.itemList && viewForm.itemList.length > 0">
              <el-table-column label="行号" prop="lineNo" width="70" align="center" />
              <el-table-column label="物料编码" prop="materialCode" width="140" align="center">
                <template #default="scope"><span>{{ scope.row.materialCode || '—' }}</span></template>
              </el-table-column>
              <el-table-column label="商品名称" prop="productName" show-overflow-tooltip align="center" />
              <el-table-column label="规格型号" prop="productSpec" width="140" align="center" />
              <el-table-column label="单位" width="80" align="center">
                <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
              </el-table-column>
              <el-table-column label="数量" prop="quantity" width="80" align="center" />
              <el-table-column label="已发货" width="80" align="center">
                <template #default="scope"><span>{{ scope.row.shippedQty || 0 }}</span></template>
              </el-table-column>
              <el-table-column label="单价" width="100" align="center">
                <template #default="scope">{{ formatAmount(scope.row.unitPrice) }}</template>
              </el-table-column>
              <el-table-column label="小计" width="120" align="center">
                <template #default="scope">{{ formatAmount(scope.row.subtotal) }}</template>
              </el-table-column>
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
              <p class="rd-empty-text">暂无订单明细</p>
            </div>
          </div>
        </section>

        <!-- 负责信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('owner')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </span>
              负责信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.owner }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.owner">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewForm.userName }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewForm.deptName }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
            </div>
          </div>
        </section>

        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('other')">
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
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.other">
            <div class="rd-grid">
              <div class="rd-item rd-item--full">
                <span class="rd-label">备注</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无备注' }}</div>
              </div>
              <div class="rd-item rd-item--full" v-if="viewForm.cancelReason">
                <span class="rd-label">作废原因</span>
                <div class="rd-value">{{ viewForm.cancelReason }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 审核记录 -->
<section class="rd-card" v-if="viewForm.auditLogList && viewForm.auditLogList.length">
<div class="rd-card-header" @click="toggleCard('viewAudit')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewAudit }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
<div class="rd-card-body" v-show="!collapsedCards.viewAudit" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in viewForm.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

<!-- 作废订单对话框 -->
    <el-dialog v-model="voidOpen" width="720px" append-to-body draggable class="rd-dialog order-cancel-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">订单作废</span>
          <div class="rd-detail-header-sub" v-if="voidForm.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ voidForm.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form label-width="100px">
        <div class="rd-page">
          <!-- 订单信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cancelOrder')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                订单信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cancelOrder }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cancelOrder">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ voidForm.orderNo }}</div></div>
              </div>
            </div>
          </section>

          <!-- 作废信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('cancelInfo')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="15" y1="9" x2="9" y2="15"/>
                    <line x1="9" y1="9" x2="15" y2="15"/>
                  </svg>
                </span>
                作废信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cancelInfo }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.cancelInfo">
              <el-form-item label="作废原因">
                <el-input v-model="voidForm.voidReason" type="textarea" :rows="3" placeholder="请输入作废原因" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitVoid">确认作废</el-button>
        <el-button @click="voidOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditOpen" width="1152px" append-to-body draggable class="rd-dialog order-audit-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          </div>
          <span class="rd-detail-header-title">订单审核</span>
          <div class="rd-detail-header-sub" v-if="auditData.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ auditData.orderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 订单信息摘要 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('auditOrder')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
              </span>
              订单信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditOrder }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.auditOrder">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ auditData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ auditData.contractNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ auditData.customerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">订单金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatAmount(auditData.orderAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">订单状态</span><div class="rd-value"><dict-tag :options="marketing_order_status" :value="auditData.orderStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ auditData.userName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ auditData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 订单明细 -->
        <section class="rd-card" v-if="auditData.itemList && auditData.itemList.length > 0">
          <div class="rd-card-header" @click="toggleCard('auditItems')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
              </span>
              订单明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditItems }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.auditItems">
            <el-table border :data="auditData.itemList" size="small">
              <el-table-column label="行号" prop="lineNo" width="70" align="center" />
              <el-table-column label="物料编码" prop="materialCode" width="140" align="center">
                <template #default="scope"><span>{{ scope.row.materialCode || '—' }}</span></template>
              </el-table-column>
              <el-table-column label="商品名称" prop="productName" show-overflow-tooltip align="center" />
              <el-table-column label="规格型号" prop="productSpec" width="140" align="center" />
              <el-table-column label="单位" width="80" align="center"><template #default="scope"><span class="badge gray"><span class="dot"></span>{{ scope.row.unit }}</span></template></el-table-column>
              <el-table-column label="数量" prop="quantity" width="80" align="center" />
              <el-table-column label="单价" width="100" align="center"><template #default="scope">{{ formatAmount(scope.row.unitPrice) }}</template></el-table-column>
              <el-table-column label="小计" width="120" align="center"><template #default="scope">{{ formatAmount(scope.row.subtotal) }}</template></el-table-column>
            </el-table>
          </div>
        </section>
        <!-- 历史审核记录 -->
<section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
<div class="rd-card-header" @click="toggleCard('auditLog')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditLog }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
<div class="rd-card-body" v-show="!collapsedCards.auditLog" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in auditData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 审核意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('auditOpinion')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审核意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditOpinion }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.auditOpinion">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
              <el-form-item label="审核意见" prop="auditOpinion">
                <el-input v-model="auditForm.auditOpinion" type="textarea" :rows="4" placeholder="请输入审核意见" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="submitAudit(true)">通 过</el-button>
        <el-button type="danger" @click="submitAudit(false)">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 客户选择弹窗 -->
    <customer-picker ref="customerPickerRef" title="选择客户" @confirm="onCustomerPickerConfirm" />

    <!-- 负责人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择负责人" @confirm="onUserPickerConfirm" />

    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择所属部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />

    <!-- 合同选择弹窗 -->
    <contract-picker ref="contractPickerRef" title="选择合同" :contract-statuses="['2']" @confirm="onContractPickerConfirm" />

    <!-- 商品选择弹窗 -->
    <material-picker ref="materialPickerRef" title="选择商品" @confirm="onMaterialPickerConfirm" />

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="订单管理业务操作说明" width="864px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">部分发货</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-tag size="small" type="info">修改后重提</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
            <el-tag size="small" type="info">需填写作废原因</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">新建订单后的初始状态，可编辑、删除、提交审核、作废</el-descriptions-item>
          <el-descriptions-item label="待审核">已提交审核，等待审核人处理。可通过或驳回</el-descriptions-item>
          <el-descriptions-item label="已驳回">审核未通过，可修改后重新提交</el-descriptions-item>
          <el-descriptions-item label="已审核">审核通过，订单正式生效，可在发货管理中进行发货、可作废</el-descriptions-item>
          <el-descriptions-item label="部分发货">已部分发货，可在发货管理中继续发货直至全部发货完毕，可作废</el-descriptions-item>
          <el-descriptions-item label="已完成">订单全部发货完毕</el-descriptions-item>
          <el-descriptions-item label="已作废">订单已作废，不再执行</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>关联合同：</strong>可选择关联合同，自动带入客户和合同编号信息</p>
          <p>• <strong>多次发货：</strong>已审核订单支持多次发货，请在【发货管理】菜单中操作，每次填写本次发货数量，全部发货完毕后自动转为已完成</p>
          <p>• <strong>订单审核：</strong>提交后需审核人审批，驳回后可修改内容并重新提交</p>
          <p>• <strong>订单作废：</strong>草稿/待审核/已审核/部分发货状态可作废，需填写作废原因</p>
                    <p>• <strong>订单明细：</strong>添加商品明细行，自动计算小计金额，合计自动汇总为订单金额</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkOrder">
import { CircleClose, ArrowRight, ArrowDown, QuestionFilled } from '@element-plus/icons-vue'
import { listOrder, getOrder, addOrder, updateOrder, delOrder, submitOrder, voidOrder, auditOrder } from '@/api/mk/order'
import { listContract } from '@/api/mk/contract'
import { getCustomer } from '@/api/mk/customer'
import { listMaterial } from '@/api/wms/material'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import CustomerPicker from '@/components/CustomerPicker/index.vue'
import ContractPicker from '@/components/ContractPicker/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_order_index')
const { marketing_order_status, wms_unit } = proxy.useDict('marketing_order_status', 'wms_unit')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const voidOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const materialPickerIndex = ref(null)
const viewForm = ref({})
const voidForm = ref({})
const auditOpen = ref(false)
const auditData = ref({})
const auditForm = ref({ orderId: null, auditOpinion: null })
const collapsedCards = reactive({ basic: false, owner: false, items: false, other: false, cancelOrder: false, cancelInfo: false, formAudit: false, viewAudit: false, auditLog: false })
const showStatusHelp = ref(false)
const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0, '5': 0, '6': 0 })
const statusTabList = computed(() => marketing_order_status.value)
function loadStatusCounts() {
  // 基于当前筛选条件（剔除状态与分页）拉取全量数据统计，避免仅统计当前页
  const query = { ...queryParams.value, pageNum: 1, pageSize: 9999, orderStatus: undefined, params: { ...queryParams.value.params } }
  listOrder(query).then(res => {
    const counts = { all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0, '5': 0, '6': 0 }
    const rows = res.rows || []
    rows.forEach(row => {
      const s = row.orderStatus
      if (counts[s] !== undefined) counts[s]++
    })
    counts.all = rows.length
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.orderStatus = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'violet', '4': 'green', '5': 'red', '6': 'gray' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_order_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-partial', '4': 'tab-done', '5': 'tab-reject', '6': 'tab-void' }; return map[value] || '' }
function toggleCard(name) { collapsedCards[name] = !collapsedCards[name] }
function formatAmount(val) { if (val == null || val === '') return '-'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, customerName: undefined, orderStatus: undefined, contractNo: undefined, userName: undefined, params: {} },
  rules: {
    customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
    userId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
  },
  auditRules: {
    auditOpinion: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
  }
})
const { queryParams, form, rules, auditRules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  orderNo: { label: '订单编号', visible: true },
  contractNo: { label: '合同编号', visible: true },
  customerName: { label: '客户名称', visible: true },
  orderAmount: { label: '订单金额', visible: true },
  orderStatus: { label: '订单状态', visible: true },
  userName: { label: '负责人', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_order_columns')
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
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.orderNo) count++
  if (queryParams.value.customerName) count++
  if (queryParams.value.orderStatus) count++
  if (queryParams.value.contractNo) count++
  if (queryParams.value.userName) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }

function getList() {
  loading.value = true
  listOrder(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false })
}

/** 打开负责人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.userId)
}
/** 负责人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.userId = user.userId
  form.value.userName = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}
/** 清除负责人 */
function clearUser() {
  form.value.userId = undefined
  form.value.userName = undefined
}
/** 打开部门选择弹窗 */
function openDeptPicker() {
  proxy.$refs.deptPickerRef.open(form.value.deptId)
}
/** 部门选择确认回调 */
function onDeptPickerConfirm(dept) {
  form.value.deptId = dept.deptId
  form.value.deptName = dept.deptName
}
/** 清除部门 */
function clearDept() {
  form.value.deptId = undefined
  form.value.deptName = undefined
}

/** 打开客户选择弹窗 */
function openCustomerPicker() {
  proxy.$refs.customerPickerRef.open(form.value.customerId)
}
/** 客户选择确认回调 — 自动带出客户的负责人作为订单负责人 */
function onCustomerPickerConfirm(customer) {
  form.value.customerId = customer.customerId
  form.value.customerName = customer.customerName
  if (customer.userId) {
    form.value.userId = customer.userId
    form.value.userName = customer.userName
    if (customer.deptId) { form.value.deptId = customer.deptId; form.value.deptName = customer.deptName }
  }
}
/** 清除客户 */
function clearCustomer() {
  form.value.customerId = undefined
  form.value.customerName = undefined
}
/** 打开合同选择弹窗 */
function openContractPicker() {
  proxy.$refs.contractPickerRef.open(form.value.contractId)
}
/** 合同选择确认回调 — 自动带出客户及负责人信息 */
function onContractPickerConfirm(contract) {
  form.value.contractId = contract.contractId
  form.value.contractNo = contract.contractNo
  if (contract.customerId) {
    form.value.customerId = contract.customerId
    form.value.customerName = contract.customerName
    getCustomer(contract.customerId).then(res => {
      const customer = res.data
      if (customer && customer.userId && !form.value.userId) {
        form.value.userId = customer.userId
        form.value.userName = customer.userName
        if (customer.deptId) { form.value.deptId = customer.deptId; form.value.deptName = customer.deptName }
      }
    })
  }
}
/** 清除合同 */
function clearContract() {
  form.value.contractId = undefined
  form.value.contractNo = undefined
}
function handleAddItem() { if (!form.value.itemList) { form.value.itemList = [] }; const lineNo = (form.value.itemList.length + 1) * 10; form.value.itemList.push({ lineNo, materialId: undefined, materialCode: undefined, productName: undefined, productSpec: undefined, unit: undefined, quantity: undefined, unitPrice: undefined, subtotal: undefined }) }
function handleDeleteItem(index) { form.value.itemList.splice(index, 1); form.value.itemList.forEach((item, idx) => { item.lineNo = (idx + 1) * 10 }) }
/** 打开商品选择弹窗 */
function openMaterialPicker(index) {
  materialPickerIndex.value = index
  proxy.$refs.materialPickerRef.open(form.value.itemList[index]?.materialId)
}
/** 商品选择确认回调 */
function onMaterialPickerConfirm(material) {
  const index = materialPickerIndex.value
  if (index == null) return
  const item = form.value.itemList[index]
  if (item) {
    item.materialId = material.materialId
    item.materialCode = material.materialCode
    item.productName = material.materialName
    item.productSpec = material.specModel
    item.unit = material.unit
  }
}
/** 清除商品 */
function clearMaterial(index) {
  const item = form.value.itemList[index]
  if (item) {
    item.materialId = undefined
    item.materialCode = undefined
    item.productName = undefined
    item.productSpec = undefined
    item.unit = undefined
  }
}
function calcSubtotal(index) { const item = form.value.itemList[index]; if (item && item.quantity != null && item.unitPrice != null) { item.subtotal = (item.quantity * item.unitPrice).toFixed(2) } else { item.subtotal = undefined }; const total = form.value.itemList.reduce((sum, i) => sum + (parseFloat(i.subtotal) || 0), 0); form.value.orderAmount = total.toFixed(2) }
function handleQuery() { showAdvanced.value = false; queryParams.value.params = proxy.addDateRange(queryParams.value.params, dateRange.value, 'CreateTime'); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.orderNo = undefined; queryParams.value.customerName = undefined; queryParams.value.orderStatus = undefined; queryParams.value.contractNo = undefined; queryParams.value.userName = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.orderId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { orderNo: undefined, contractId: undefined, contractNo: undefined, customerId: undefined, customerName: undefined, orderAmount: 0, orderStatus: '0', userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, itemList: [], remark: undefined }; collapsedCards.basic = false; collapsedCards.owner = false; collapsedCards.items = false; collapsedCards.other = false; proxy.resetForm('orderRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增订单' }
function handleUpdate(row) { reset(); getOrder(row.orderId || ids.value[0]).then(res => { form.value = res.data; if (!form.value.itemList) { form.value.itemList = [] }; open.value = true; title.value = '修改订单' }) }
function handleView(row) { getOrder(row.orderId).then(res => { viewForm.value = res.data; collapsedCards.basic = false; collapsedCards.owner = false; collapsedCards.items = !res.data.itemList || res.data.itemList.length === 0; collapsedCards.other = !res.data.remark && !res.data.cancelReason; viewOpen.value = true }) }
function submitForm() { proxy.$refs['orderRef'].validate(valid => { if (valid) { if (form.value.orderId != undefined) { updateOrder(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addOrder(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const orderIds = row.orderId || ids.value; proxy.$modal.confirm('确认删除编号为"' + orderIds + '"的数据？').then(() => delOrder(orderIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

function handleSubmit(row) { proxy.$modal.confirm('确认提交订单【' + row.orderNo + '】？').then(() => submitOrder(row.orderId)).then(() => { getList(); proxy.$modal.msgSuccess('订单提交成功') }).catch(() => {}) }

/** 审核按钮操作 */
async function handleAudit(row) { const response = await getOrder(row.orderId); auditData.value = response.data; auditForm.value = { orderId: row.orderId, auditOpinion: null }; auditOpen.value = true }

/** 提交审核 */
function submitAudit(passed) { proxy.$refs['auditRef'].validate(valid => { if (valid) { const status = passed ? '2' : '1'; const actionText = passed ? '通过' : '驳回'; proxy.$modal.confirm('确认' + actionText + '该订单？').then(() => auditOrder(auditForm.value.orderId, status, auditForm.value.auditOpinion)).then(() => { proxy.$modal.msgSuccess('审核成功'); auditOpen.value = false; getList() }).catch(() => {}) } }) }

function handleVoid(row) { voidForm.value = { orderId: row.orderId, orderNo: row.orderNo, voidReason: '' }; voidOpen.value = true }
function submitVoid() { if (!voidForm.value.voidReason) { proxy.$modal.msgWarning('请输入作废原因'); return }; voidOrder(voidForm.value.orderId, voidForm.value.voidReason).then(() => { proxy.$modal.msgSuccess('订单已作废'); voidOpen.value = false; getList() }) }

function handleExport() { proxy.download('mk/order/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime') }, `order_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
getList()
</script>

<style scoped>
/* ===== 订单页面卡片式样式（遵循 detail-page-style-guide 规范） ===== */
:deep(.order-cancel-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-cancel-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-cancel-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-cancel-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-cancel-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.order-receive-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-receive-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-receive-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-receive-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-receive-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.order-form-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-form-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-form-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-form-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-form-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.order-detail-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-detail-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-detail-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-detail-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-detail-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }

.rd-page { max-width: 1128px; margin: 0 auto; }

.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-body { padding: 14px 16px; }

.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-label-dot { display: none; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--large { font-size: 18px; font-weight: 700; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
.rd-amount { font-variant-numeric: tabular-nums; font-weight: 700; color: #111827; }

.rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 6px; border: none; background: transparent; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }

.rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px; text-align: center; color: #9ca3af; background: #f9fafb; border-radius: 12px; border: 1px dashed #e5e7eb; }
.rd-empty-icon { margin-bottom: 4px; color: #d1d5db; }
.rd-empty-text { font-size: 14px; font-weight: 500; margin: 0; }

@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }

@media (max-width: 768px) { .rd-grid { grid-template-columns: 1fr; } .rd-card-header { padding: 8px 12px; } .rd-card-body { padding: 12px; } }
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
.rd-form-tip {
  margin-left: 4px;
  color: #c0c4cc;
  font-size: 14px;
  vertical-align: middle;
  cursor: help;
}
.rd-form-tip:hover {
  color: #909399;
}
.rd-select-empty {
  padding: 10px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
:deep(.el-input.is-disabled .el-input__inner) {
  cursor: pointer;
}

:deep(.order-audit-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.order-audit-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.order-audit-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.order-audit-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.order-audit-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

.reject-alert {
  margin-bottom: 16px;
}

.rd-timeline {
  position: relative;
  padding-left: 20px;
}
.rd-timeline::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: #e5e7eb;
}
.rd-timeline-item {
  position: relative;
  padding-bottom: 20px;
}
.rd-timeline-item:last-child {
  padding-bottom: 0;
}
.rd-timeline-dot {
  position: absolute;
  left: -18px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d1d5db;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #e5e7eb;
}
.rd-timeline-dot--success {
  background: #67c23a;
  box-shadow: 0 0 0 1px #b3e19d;
}
.rd-timeline-dot--error {
  background: #f56c6c;
  box-shadow: 0 0 0 1px #fab6b6;
}
.rd-timeline-content {
  padding-left: 8px;
}
.rd-timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.rd-timeline-title {
  display: flex;
  align-items: center;
}
.rd-timeline-time {
  font-size: 12px;
  color: #9ca3af;
  font-variant-numeric: tabular-nums;
}
.rd-timeline-body {
  margin-bottom: 4px;
}
.rd-timeline-comment {
  font-size: 13px;
  color: #6b7280;
  background: #f9fafb;
  border-radius: 6px;
  padding: 6px 10px;
  margin-top: 4px;
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
.status-help-content .status-flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}
.status-help-content .flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-help-content .flow-arrow {
  color: #909399;
  font-size: 16px;
}
.status-help-content .highlight-card {
  background-color: #ecf5ff;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #409eff;
}
.status-help-content .highlight-card p {
  margin: 6px 0;
  line-height: 1.6;
  font-size: 13px;
  color: #606266;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
