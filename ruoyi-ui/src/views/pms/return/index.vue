<template>
  <div class="app-container pms-return-page">
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
          <label>退货单号</label>
          <div class="control">
            <el-input v-model="queryParams.returnNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_return_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>退货类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.returnType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_return_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>经办人</label>
          <div class="control">
            <el-input v-model="queryParams.handlerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>退货日期</label>
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
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:return:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:return:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:return:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:return:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_return_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="退货单号" prop="returnNo" key="returnNo" :width="colWidth('returnNo', 160)" resizable sortable="custom" v-if="columns.returnNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.returnNo }}</span></template>
          </el-table-column>
          <el-table-column label="收货单号" prop="receiveNo" key="receiveNo" :width="colWidth('receiveNo', 160)" resizable v-if="columns.receiveNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.receiveNo }}</span></template>
          </el-table-column>
          <el-table-column label="采购单号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 160)" resizable v-if="columns.orderNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.orderNo }}</span></template>
          </el-table-column>
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="退货类型" prop="returnType" key="returnType" :width="colWidth('returnType', 100)" resizable align="center" v-if="columns.returnType.visible">
            <template #default="scope">
              <span class="badge violet">{{ returnTypeLabel(scope.row.returnType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="退货日期" prop="returnDate" key="returnDate" :width="colWidth('returnDate', 120)" resizable align="center" sortable="custom" v-if="columns.returnDate.visible" />
          <el-table-column label="退货总数量" prop="totalQty" key="totalQty" :width="colWidth('totalQty', 110)" resizable align="right" class-name="col-num" v-if="columns.totalQty.visible" />
          <el-table-column label="退货总金额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="right" sortable="custom" class-name="col-num" v-if="columns.totalAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="经办人" prop="handlerName" key="handlerName" :width="colWidth('handlerName', 100)" resizable v-if="columns.handlerName.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
<el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
<el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:return:edit']" v-if="scope.row.status === '0' || scope.row.status === '5'">修改</el-button>
<el-button link type="success" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['pms:return:edit']" v-if="scope.row.status === '0' || scope.row.status === '5'">提交</el-button>
<el-button link type="warning" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['pms:return:audit']" v-if="scope.row.status === '1'">审批</el-button>
<el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:return:remove']" v-if="scope.row.status === '0'">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改弹窗 ===== -->
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="returnRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <!-- 单据信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>单据信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="退货单号" prop="returnNo">
                    <el-input v-model="form.returnNo" placeholder="保存后自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="收货单号" prop="receiveNo">
                    <el-input v-model="form.receiveNo" placeholder="关联收货单号" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="采购单号" prop="orderId">
                    <el-select v-model="form.orderId" filterable placeholder="请选择采购订单" style="width: 100%" @change="onOrderChange" :disabled="!!form.receiveId">
                      <el-option v-for="o in orderOptions" :key="o.orderId" :label="o.orderNo" :value="o.orderId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 退货信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>退货信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="退货日期" prop="returnDate">
                    <el-date-picker v-model="form.returnDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="供应商" prop="supplierId">
                    <el-select v-model="form.supplierId" filterable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange">
                      <el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="退货类型" prop="returnType">
                    <el-select v-model="form.returnType" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in pms_return_type" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="经办人" prop="handlerName">
                    <el-input v-model="form.handlerName" placeholder="请输入经办人" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 退货明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>退货明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-table :data="form.detailList" border size="small" empty-text="选择采购订单后，已收货的物料明细将自动带出">
                <el-table-column label="序号" type="index" width="55" align="center" />
                <el-table-column label="物料编码" prop="materialCode" min-width="120" show-overflow-tooltip />
                <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
                <el-table-column label="单位" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
                <el-table-column label="已收数量" prop="receivedQty" width="100" align="right" />
                <el-table-column label="退货数量" width="100"><template #default="scope"><el-input-number v-model="scope.row.qty" :precision="2" :min="0" :max="scope.row.receivedQty" :controls="false" size="small" style="width: 90px" @change="calcDetail(scope.row)" /></template></el-table-column>
                <el-table-column label="单价" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.unitPrice) }}</span></template></el-table-column>
                <el-table-column label="退货金额" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
                <el-table-column label="退货原因" min-width="150"><template #default="scope"><el-input v-model="scope.row.returnReason" placeholder="原因" size="small" /></template></el-table-column>
                <el-table-column label="操作" width="80" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="handleDeleteDetail(scope.$index)">删除</el-button></template></el-table-column>
              </el-table>
            </div>
          </section>
          <!-- 退货原因 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>退货原因</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="退货原因" prop="reason">
                    <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="请输入退货原因" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 审核记录 -->
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header" @click="toggleCard('c5')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c5" style="display:block">
              <el-alert v-if="form.status === '5'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该退货单已被驳回，请根据审核意见修改后重新提交</template>
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
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 查看弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg></div><span class="rd-detail-header-title">退货详情</span><div class="rd-detail-header-sub" v-if="viewData.returnNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.returnNo }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>单据信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">退货单号</span><div class="rd-value">{{ viewData.returnNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">收货单号</span><div class="rd-value">{{ viewData.receiveNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ viewData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_return_status" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">出库单号</span><div class="rd-value">{{ viewData.outboundOrderNo || (viewData.outboundOrderId ? 'OUT' + viewData.outboundOrderId : '-') || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>退货信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">退货类型</span><div class="rd-value"><dict-tag :options="pms_return_type" :value="viewData.returnType" /></div></div>
              <div class="rd-item"><span class="rd-label">退货日期</span><div class="rd-value">{{ viewData.returnDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">经办人</span><div class="rd-value">{{ viewData.handlerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">退货总数量</span><div class="rd-value">{{ viewData.totalQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">退货总金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.totalAmount) }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>退货明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <el-table :data="viewData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="退货数量" prop="qty" width="100" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="110" align="right" />
              <el-table-column label="退货金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              <el-table-column label="退货原因" prop="returnReason" min-width="150" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>退货原因</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v4">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">退货原因</span><div class="rd-value">{{ viewData.reason || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('v5')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v5" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in viewData.auditLogList" :key="log.logId">
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
            <div class="rd-empty" v-if="!viewData.auditLogList || viewData.auditLogList.length === 0">
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
              <p class="rd-empty-text">暂无审核记录</p>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="auditOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">退货审批</span>
          <div class="rd-detail-header-sub" v-if="auditData.returnNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.returnNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>退货信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">退货单号</span><div class="rd-value">{{ auditData.returnNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">收货单号</span><div class="rd-value">{{ auditData.receiveNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ auditData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ auditData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">退货类型</span><div class="rd-value"><dict-tag :options="pms_return_type" :value="auditData.returnType" /></div></div>
              <div class="rd-item"><span class="rd-label">退货日期</span><div class="rd-value">{{ auditData.returnDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">退货总数量</span><div class="rd-value">{{ auditData.totalQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">退货总金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.totalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">经办人</span><div class="rd-value">{{ auditData.handlerName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">退货原因</span><div class="rd-value">{{ auditData.reason || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>退货明细</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="auditData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="退货数量" prop="qty" width="100" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="110" align="right" />
              <el-table-column label="退货金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              <el-table-column label="退货原因" prop="returnReason" min-width="150" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('a5')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a5" style="display:block">
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
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a4 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a4">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
              <el-form-item label="审批意见" prop="auditOpinion">
                <el-input v-model="auditForm.auditOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" show-word-limit maxlength="500" />
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

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="退货管理业务状态流转说明" width="700px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="info">草稿</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">待审批</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="primary">已审批</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已退货</el-tag></div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item"><el-tag type="info">草稿</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="danger">已作废</el-tag></div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item"><el-tag type="warning">待审批</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="danger">已驳回</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">待审批</el-tag><span style="font-size: 12px; color: #909399; margin-left: 4px;">（修改后重新提交）</span></div>
        </div>
        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">新建退货单后的初始状态，可编辑、删除、提交审批。可由收货验收页面"发起退货"自动创建。手动新增时选择采购订单后，系统自动带出已收货物料明细（未收货的物料不显示），填写退货数量后保存</el-descriptions-item>
          <el-descriptions-item label="待审批">已提交审批，等待审批人审核。审批人可【通过】或【驳回】</el-descriptions-item>
          <el-descriptions-item label="已审批">审批通过，系统自动生成退货出库单（WMS）并提示出库单号。退货出库前需确保关联入库单已完成收货上架，否则出库提交时将提示库存不足并显示关联入库单号</el-descriptions-item>
          <el-descriptions-item label="已退货">退货出库已完成（库存已扣减），退货流程结束</el-descriptions-item>
          <el-descriptions-item label="已作废">草稿状态的退货单被删除（软删除）</el-descriptions-item>
          <el-descriptions-item label="已驳回">审批人驳回退货申请，可修改后重新提交审批</el-descriptions-item>
        </el-descriptions>
        <h4>三、业务触发说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>新建退货：</strong>手动新增退货单（选择采购订单后自动带出已收物料明细，填写退货数量和原因），或由收货验收页面"发起退货"自动创建（草稿状态）。同一采购订单存在草稿/待审批/已审批/已驳回退货单时不可发起新退货</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>提交审批：</strong>草稿或已驳回状态点击"提交"按钮，变为待审批</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>审批通过：</strong>审批人点击"审批"并选择"通过"，变为已审批状态，系统<strong style="color: #67c23a;">自动生成退货出库单</strong>（WMS出库单类型为退货出库）并提示出库单号</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>审批驳回：</strong>审批人点击"审批"并选择"驳回"，变为已驳回状态，可修改后重新提交</el-timeline-item>
          <el-timeline-item type="info" :hollow="true"><strong>出库提交校验：</strong>退货出库单提交时，系统会校验库存是否充足。如果关联的入库单尚未完成收货上架，将提示"物料【xxx】库存不足，关联入库单号：IN2026xxxx，请先在WMS入库管理中完成该入库单的上架操作后重试"，方便用户快速定位需要处理的入库单</el-timeline-item>
          <el-timeline-item type="info" :hollow="true"><strong>出库完成：</strong>仓库完成退货出库后（库存已扣减），退货单变为已退货状态，流程结束</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>作废：</strong>草稿状态点击"删除"，变为已作废状态</el-timeline-item>
        </el-timeline>
        <h4>四、退货限制规则</h4>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="24">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title"><el-icon style="margin-right: 4px;"><CircleClose /></el-icon>退货单互斥规则</div>
              <div class="highlight-card-body">同一采购订单<strong>不可同时存在多个进行中的退货单</strong>。进行中状态包括：<el-tag size="small" type="info">草稿</el-tag>、<el-tag size="small" type="warning">待审批</el-tag>、<el-tag size="small" type="primary">已审批</el-tag>、<el-tag size="small" type="danger">已驳回</el-tag>。必须等待当前退货单完成（<el-tag size="small" type="success">已退货</el-tag>）或作废（<el-tag size="small" type="danger">已作废</el-tag>）后，才能发起新的退货</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 16px;">
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title"><el-icon style="margin-right: 4px;"><QuestionFilled /></el-icon>前端过滤</div>
              <div class="highlight-card-body">新增退货时，采购订单下拉列表会<strong>自动隐藏</strong>存在进行中退货单的订单，用户无法选择</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title"><el-icon style="margin-right: 4px;"><CircleClose /></el-icon>后端校验</div>
              <div class="highlight-card-body">若通过其他途径（如验收收货页面"发起退货"）提交，后端会拦截并提示："该采购订单已有进行中的退货单，请先完成或作废该退货单后再发起新的退货"</div>
            </div>
          </el-col>
        </el-row>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsReturn">
import { listReturn, getReturn, addReturn, updateReturn, delReturn, submitReturn, auditReturn, getInProgressReturnOrderIds } from '@/api/pms/return'
import { listOrder, getOrder } from '@/api/pms/order'
import { ArrowRight, ArrowDown, QuestionFilled, CircleClose, Plus, Edit, Delete, Download, RefreshLeft, WarningFilled, User, Calendar, OfficeBuilding, Search, Filter } from '@element-plus/icons-vue'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { pms_return_status, pms_return_type, wms_unit } = proxy.useDict('pms_return_status', 'pms_return_type', 'wms_unit')

const { collapsedCards, toggleCard } = useDetailCard(["c1","c4","c2","c0","c5","v1","v3","v2","v4","v5","a4","a5"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_return_index')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const auditOpen = ref(false)
const auditData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const supplierOptions = ref([])
const orderOptions = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0, '5': 0 })

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  returnNo: { label: '退货单号', visible: true },
  receiveNo: { label: '收货单号', visible: true },
  orderNo: { label: '采购单号', visible: true },
  supplierName: { label: '供应商', visible: true },
  status: { label: '状态', visible: true },
  returnType: { label: '退货类型', visible: true },
  returnDate: { label: '退货日期', visible: true },
  totalQty: { label: '退货总数量', visible: true },
  totalAmount: { label: '退货总金额', visible: true },
  handlerName: { label: '经办人', visible: true },
  createTime: { label: '创建时间', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_return_columns')
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
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, returnNo: undefined, supplierName: undefined, status: undefined, returnType: undefined, handlerName: undefined, params: {} },
  auditForm: { returnId: null, auditOpinion: null },
  rules: { orderId: [{ required: true, message: '请选择采购订单', trigger: 'change' }] },
  auditRules: { auditOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }] }
})
const { queryParams, form, auditForm, rules, auditRules } = toRefs(data)

const statusTabList = computed(() => pms_return_status.value)
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.returnNo) count++
  if (queryParams.value.supplierName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.returnType) count++
  if (queryParams.value.handlerName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() { loading.value = true; listReturn(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.returnNo = undefined; queryParams.value.supplierName = undefined; queryParams.value.status = undefined; queryParams.value.returnType = undefined; queryParams.value.handlerName = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery()
}
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.returnId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { returnId: undefined, returnNo: undefined, receiveId: undefined, receiveNo: undefined, orderId: undefined, orderNo: undefined, supplierId: undefined, supplierName: undefined, status: '0', returnType: '0', returnDate: undefined, totalQty: 0, totalAmount: 0, reason: undefined, handlerName: undefined, remark: undefined, detailList: [] }; proxy.resetForm('returnRef') }
function onSupplierChange(val) { const matched = supplierOptions.value.find(s => s.supplierId === val); form.value.supplierName = matched ? matched.supplierName : undefined }
function loadSupplierOptions() { listSupplier({ pageNum: 1, pageSize: 999 }).then(res => { supplierOptions.value = res.rows || [] }) }
function loadOrderOptions() {
  Promise.all([
    listOrder({ pageNum: 1, pageSize: 999 }),
    getInProgressReturnOrderIds().catch(() => ({ data: [] }))
  ]).then(([orderRes, inProgressRes]) => {
    const inProgressIds = (inProgressRes && inProgressRes.data) || []
    orderOptions.value = (orderRes.rows || []).filter(o =>
      ['2','3','4','5'].includes(o.status) && !inProgressIds.includes(o.orderId)
    )
  })
}
function onOrderChange(orderId) {
  if (!orderId) { form.value.detailList = []; return }
  getOrder(orderId).then(res => {
    const order = res.data
    form.value.orderNo = order.orderNo
    form.value.supplierId = order.supplierId
    form.value.supplierName = order.supplierName
    form.value.detailList = (order.detailList || []).filter(d => d.receivedQty > 0).map(d => ({
      materialId: d.materialId, materialCode: d.materialCode, materialName: d.materialName,
      specModel: d.specModel, unit: d.unit, qty: 0, unitPrice: d.unitPrice, amount: 0,
      returnReason: '', receivedQty: d.receivedQty
    }))
    if (form.value.detailList.length === 0) {
      proxy.$modal.msgWarning('该采购订单尚未收货验收，无可退物料')
    }
  })
}
function handleAdd() { reset(); loadOrderOptions(); open.value = true; title.value = '添加退货单' }
function handleUpdate(row) { 
  let returnId = row && row.returnId ? row.returnId : (ids.value && ids.value.length > 0 ? ids.value[0] : null);
  if (!returnId || typeof returnId !== 'number') {
    proxy.$modal.msgError('无效的退货单ID');
    return;
  }
  reset(); getReturn(returnId).then(res => { form.value = res.data; open.value = true; title.value = '修改退货单' }).catch(() => {
    proxy.$modal.msgError('获取退货单详情失败');
  }) 
}
function handleView(row) { 
  if (!row || !row.returnId || typeof row.returnId !== 'number') {
    proxy.$modal.msgError('无效的退货单ID');
    return;
  }
  getReturn(row.returnId).then(res => { 
    viewData.value = res.data; 
    viewOpen.value = true 
  }).catch(() => {
    proxy.$modal.msgError('获取退货单详情失败');
  }) 
}
function calcDetail(row) { if (row.qty != null && row.unitPrice != null) row.amount = parseFloat((row.qty * row.unitPrice).toFixed(2)); let tq = 0, ta = 0; form.value.detailList.forEach(d => { if (d.qty) tq += d.qty; if (d.amount) ta += d.amount }); form.value.totalQty = parseFloat(tq.toFixed(2)); form.value.totalAmount = parseFloat(ta.toFixed(2)) }
function handleDeleteDetail(index) { form.value.detailList.splice(index, 1); calcDetail({}) }
function submitForm() { proxy.$refs['returnRef'].validate(valid => { if (valid) { if (form.value.returnId != undefined) { updateReturn(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addReturn(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { 
  let returnIds = row && row.returnId ? row.returnId : ids.value;
  if (!returnIds) {
    proxy.$modal.msgError('无效的退货单ID');
    return;
  }
  if (Array.isArray(returnIds) && returnIds.some(id => !id || typeof id !== 'number')) {
    proxy.$modal.msgError('存在无效的退货单ID');
    return;
  }
  if (!Array.isArray(returnIds) && (!returnIds || typeof returnIds !== 'number')) {
    proxy.$modal.msgError('无效的退货单ID');
    return;
  }
  proxy.$modal.confirm('确认删除编号为"' + returnIds + '"的数据？').then(() => delReturn(returnIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) 
}
function handleExport() { proxy.download('pms/return/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `return_${new Date().getTime()}.xlsx`) }
function handleSubmit(row) { 
  if (!row || !row.returnId || typeof row.returnId !== 'number') {
    proxy.$modal.msgError('无效的退货单ID');
    return;
  }
  proxy.$modal.confirm('确认提交编号为"' + row.returnNo + '"的退货单审批？').then(() => submitReturn(row.returnId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {}) 
}
function handleAudit(row) { 
  if (!row || !row.returnId || typeof row.returnId !== 'number') {
    proxy.$modal.msgError('无效的退货单ID');
    return;
  }
  getReturn(row.returnId).then(res => { 
    auditData.value = res.data; 
    auditForm.value = { returnId: row.returnId, auditOpinion: null }; 
    auditOpen.value = true 
  }).catch(() => {
    proxy.$modal.msgError('获取退货单详情失败');
  })
}
function submitAudit(passed) { proxy.$refs['auditRef'].validate(valid => { if (valid) { 
    if (!auditForm.value.returnId || typeof auditForm.value.returnId !== 'number') {
      proxy.$modal.msgError('退货单ID无效或丢失，请重新选择退货单');
      auditOpen.value = false;
      return;
    }
    const status = passed ? '2' : '5'; const actionText = passed ? '通过' : '驳回'; proxy.$modal.confirm(`确认${actionText}该退货单？`).then(() => { return auditReturn(auditForm.value.returnId, status, auditForm.value.auditOpinion) }).then((res) => { if (passed) { const outboundOrderNo = res.outboundOrderNo; const outboundOrderError = res.outboundOrderError; if (outboundOrderError) { proxy.$modal.msgWarning(`审批通过，但出库单生成失败：${outboundOrderError}`); } else if (outboundOrderNo) { proxy.$modal.msgSuccess(`审批通过，已自动生成退货出库单：${outboundOrderNo}`); } else { proxy.$modal.msgSuccess('审批通过'); } } else { proxy.$modal.msgSuccess('已驳回'); } auditOpen.value = false; getList() }).catch((error) => { proxy.$modal.msgError('审批失败: ' + (error.message || '服务器错误')); }) } }) }
function cancel() { open.value = false; reset() }
function loadStatusCounts() {
  listReturn({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0, '5': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'green', '4': 'gray', '5': 'red' }; return map[status] || 'gray' }
function statusLabel(status) { const item = pms_return_status.value.find(d => d.value == status); return item ? item.label : '-' }
function returnTypeLabel(type) { const item = pms_return_type.value.find(d => d.value == type); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-done', '4': 'tab-void', '5': 'tab-reject' }; return map[value] || '' }
function supplierInitial(name) { return name ? name.charAt(0) : '?' }
function avatarStyle(name) {
  const gradients = ['linear-gradient(135deg,#6366f1,#8b5cf6)','linear-gradient(135deg,#10b981,#06b6d4)','linear-gradient(135deg,#f59e0b,#ef4444)','linear-gradient(135deg,#3b82f6,#6366f1)','linear-gradient(135deg,#ec4899,#8b5cf6)']
  const index = name ? name.charCodeAt(0) % gradients.length : 0
  return { background: gradients[index] }
}

getList()
loadStatusCounts()
loadSupplierOptions()
loadOrderOptions()
onActivated(() => { getList(); loadOrderOptions() })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-return-page {
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

/* ===== Page Head ===== */
.pms-return-page .page-head { display:flex; align-items:flex-end; justify-content:space-between; gap:24px; margin-bottom:20px; }
.pms-return-page .page-head h1 { font-size:26px; font-weight:700; letter-spacing:-.02em; margin:0 0 6px; color:var(--ink-900); }
.pms-return-page .page-head .subtitle { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:14px; }
.pms-return-page .page-head .subtitle .dot { width:3px; height:3px; background:var(--ink-300); border-radius:50%; }
.pms-return-page .page-head .subtitle strong { color:var(--ink-700); font-weight:600; }

/* ===== Surface Card ===== */
.pms-return-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.pms-return-page .filter-card { padding:14px 20px 16px; }
.pms-return-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-return-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-return-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-return-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-return-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-return-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-return-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-return-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-return-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-return-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-return-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.pms-return-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-return-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-return-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-return-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.pms-return-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-return-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-return-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-return-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-return-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.pms-return-page .field .control :deep(.el-select) { width:100%; }
.pms-return-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-return-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-return-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.pms-return-page .field .control :deep(.el-date-editor) { width:100%; }
.pms-return-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pms-return-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pms-return-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Status Tabs ===== */
.pms-return-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-return-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-return-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-return-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-return-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-return-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-return-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-return-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-return-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-return-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-return-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.pms-return-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.pms-return-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.pms-return-page .status-tab.tab-audit .dot { background:var(--blue-500); }
.pms-return-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); }
.pms-return-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.pms-return-page .status-tab.tab-approved .dot, .pms-return-page .status-tab.tab-done .dot { background:var(--green-500); }
.pms-return-page .status-tab.tab-approved .count, .pms-return-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.pms-return-page .status-tab.is-active.tab-approved .count, .pms-return-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.pms-return-page .status-tab.tab-void .dot { background:var(--ink-400); }
.pms-return-page .status-tab.tab-reject .dot { background:var(--red-500); }
.pms-return-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.pms-return-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }

/* ===== Tip Pill ===== */
.pms-return-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.pms-return-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.pms-return-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }

/* ===== Toolbar ===== */
.pms-return-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-return-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-return-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-return-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.pms-return-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-return-page .btn-soft .el-icon { font-size:14px; }
.pms-return-page .btn-soft.is-primary { background:var(--brand-600); color:#fff; box-shadow:0 1px 0 rgba(255,255,255,.15) inset, 0 4px 12px -4px rgba(79,70,229,.5); }
.pms-return-page .btn-soft.is-primary:hover { background:var(--brand-700); transform:translateY(-1px); box-shadow:0 1px 0 rgba(255,255,255,.15) inset, 0 6px 16px -4px rgba(79,70,229,.55); }
.pms-return-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-return-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-return-page .btn-soft.is-ghost { background:transparent; color:var(--ink-700); }
.pms-return-page .btn-soft.is-ghost:hover { background:var(--ink-100); }
.pms-return-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-return-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-return-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-return-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.pms-return-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.pms-return-page .table-wrap { overflow-x:auto; }
.pms-return-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-return-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-return-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-return-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-return-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-return-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-return-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-return-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-return-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-return-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-return-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.pms-return-page .app-table :deep(.col-num) { text-align:right; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.pms-return-page .app-table .supplier { display:flex; align-items:center; gap:10px; min-width:0; }
.pms-return-page .app-table .supplier .avatar-mini { width:28px; height:28px; border-radius:8px; display:grid; place-items:center; font-size:12px; font-weight:700; color:#fff; flex-shrink:0; }
.pms-return-page .app-table .supplier .supplier-info { min-width:0; flex:1; }
.pms-return-page .app-table .supplier .name { color:var(--ink-900); font-weight:500; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.pms-return-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }

/* ===== Badges ===== */
.pms-return-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-return-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-return-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-return-page .badge.amber .dot { background:var(--amber-500); }
.pms-return-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pms-return-page .badge.blue .dot { background:var(--blue-500); }
.pms-return-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-return-page .badge.green .dot { background:var(--green-500); }
.pms-return-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.pms-return-page .badge.red .dot { background:var(--red-500); }
.pms-return-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pms-return-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-return-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.pms-return-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-return-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-return-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-return-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-return-page .pagination-container :deep(.el-pagination .btn-prev), .pms-return-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-return-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-return-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-return-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .pms-return-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-return-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-return-page .toolbar { flex-wrap:wrap; gap:10px; } .pms-return-page .status-tabs { padding:6px 8px; } }

/* ===== Status Help Dialog ===== */
.status-help-content { max-height:500px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.flow-item { display:flex; align-items:center; gap:8px; }
.flow-arrow { color:#909399; font-size:16px; }
:deep(.el-timeline-item__node) { background-color:transparent; border:2px solid; }
.reject-alert { margin-bottom:16px; }
.highlight-card { border-radius:8px; padding:16px; border:1px solid; }
.highlight-success { background-color:#f0f9ff; border-color:#b3e19d; }
.highlight-danger { background-color:#fef0f0; border-color:#fbc4c4; }
.highlight-info { background-color:#f4f4f5; border-color:#d3d4d6; }
.highlight-warning { background-color:#fdf6ec; border-color:#f5dab1; }
.highlight-card-title { font-size:14px; font-weight:600; margin-bottom:8px; display:flex; align-items:center; }
.highlight-success .highlight-card-title { color:#67c23a; }
.highlight-danger .highlight-card-title { color:#f56c6c; }
.highlight-info .highlight-card-title { color:#909399; }
.highlight-warning .highlight-card-title { color:#e6a23c; }
.highlight-card-body { font-size:13px; color:#606266; line-height:1.6; }
</style>