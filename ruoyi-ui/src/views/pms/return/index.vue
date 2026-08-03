<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="退货单号" prop="returnNo"><el-input v-model="queryParams.returnNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="供应商" prop="supplierName"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px"><el-option v-for="d in pms_return_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="退货类型" prop="returnType"><el-select v-model="queryParams.returnType" placeholder="全部" clearable style="width: 140px"><el-option v-for="d in pms_return_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button><el-button type="info" plain icon="More" @click="showAdvanced = !showAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button></el-form-item>
      <template v-show="showAdvanced">
        <el-form-item label="经办人" prop="handlerName"><el-input v-model="queryParams.handlerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
        <el-form-item label="退货日期"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" /></el-form-item>
      </template>
</el-form>

    <!-- 业务状态流转说明 -->
    <el-alert type="info" :closable="false" show-icon class="mb8">
      <template #title>
        <div style="display: flex; align-items: center; gap: 8px;">
          <span>业务状态流转：</span>
          <el-tag size="small" type="info">草稿</el-tag>
          <el-icon><ArrowRight /></el-icon>
          <el-tag size="small" type="warning">待审批</el-tag>
          <el-icon><ArrowRight /></el-icon>
          <el-tag size="small" type="primary">已审批</el-tag>
          <el-icon><ArrowRight /></el-icon>
          <el-tag size="small" type="success">已退货</el-tag>
          <el-tag size="small" type="danger" style="margin-left: 8px;">已作废</el-tag>
          <el-tag size="small" type="danger" style="margin-left: 8px;">已驳回</el-tag>
          <el-button link type="primary" size="small" @click="showStatusHelp = true" style="margin-left: 8px;">
            <el-icon><QuestionFilled /></el-icon> 查看详情
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:return:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:return:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:return:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:return:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="退货单号" prop="returnNo" :width="colWidth('returnNo', 160)" resizable sortable="custom" />
      <el-table-column label="收货单号" prop="receiveNo" :width="colWidth('receiveNo', 160)" resizable />
      <el-table-column label="采购单号" prop="orderNo" :width="colWidth('orderNo', 160)" resizable />
      <el-table-column label="供应商" prop="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom"><template #default="scope"><dict-tag :options="pms_return_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="退货类型" prop="returnType" :width="colWidth('returnType', 100)" resizable align="center"><template #default="scope"><dict-tag :options="pms_return_type" :value="scope.row.returnType" /></template></el-table-column>
      <el-table-column label="退货日期" prop="returnDate" :width="colWidth('returnDate', 120)" resizable align="center" sortable="custom" />
      <el-table-column label="退货总数量" prop="totalQty" :width="colWidth('totalQty', 110)" resizable align="right" />
      <el-table-column label="退货总金额" prop="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="right" sortable="custom"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template></el-table-column>
      <el-table-column label="经办人" prop="handlerName" :width="colWidth('handlerName', 100)" resizable />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
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
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

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
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
            <div class="rd-card-body" style="display:block">
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
        <!-- 单据信息 -->
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
        <!-- 退货信息 -->
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
        <!-- 退货明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>退货明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <el-table :data="viewData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center" />
              <el-table-column label="退货数量" prop="qty" width="100" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="110" align="right" />
              <el-table-column label="退货金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              <el-table-column label="退货原因" prop="returnReason" min-width="150" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
        <!-- 退货原因 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>退货原因</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v4">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">退货原因</span><div class="rd-value">{{ viewData.reason || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 审核记录 -->
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
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
        <!-- 退货信息摘要 -->
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
        <!-- 退货明细 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>退货明细</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="auditData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center" />
              <el-table-column label="退货数量" prop="qty" width="100" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="110" align="right" />
              <el-table-column label="退货金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              <el-table-column label="退货原因" prop="returnReason" min-width="150" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
        <!-- 历史审核记录 -->
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
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
        <!-- 审批意见 -->
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
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已退货</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
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
            <el-tag type="warning">待审批</el-tag>
            <span style="font-size: 12px; color: #909399; margin-left: 4px;">（修改后重新提交）</span>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建退货单后的初始状态，可编辑、删除、提交审批。可由收货验收页面"发起退货"自动创建。手动新增时选择采购订单后，系统自动带出已收货物料明细（未收货的物料不显示），填写退货数量后保存
          </el-descriptions-item>
          <el-descriptions-item label="待审批">
            已提交审批，等待审批人审核。审批人可【通过】或【驳回】
          </el-descriptions-item>
          <el-descriptions-item label="已审批">
            审批通过，系统自动生成退货出库单（WMS）并提示出库单号。退货出库前需确保关联入库单已完成收货上架，否则出库提交时将提示库存不足并显示关联入库单号
          </el-descriptions-item>
          <el-descriptions-item label="已退货">
            退货出库已完成（库存已扣减），退货流程结束
          </el-descriptions-item>
          <el-descriptions-item label="已作废">
            草稿状态的退货单被删除（软删除）
          </el-descriptions-item>
          <el-descriptions-item label="已驳回">
            审批人驳回退货申请，可修改后重新提交审批
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、业务触发说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建退货：</strong>手动新增退货单（选择采购订单后自动带出已收物料明细，填写退货数量和原因），或由收货验收页面"发起退货"自动创建（草稿状态）
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交审批：</strong>草稿或已驳回状态点击"提交"按钮，变为待审批
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审批通过：</strong>审批人点击"审批"并选择"通过"，变为已审批状态，系统<strong style="color: #67c23a;">自动生成退货出库单</strong>（WMS出库单类型为退货出库）并提示出库单号
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审批驳回：</strong>审批人点击"审批"并选择"驳回"，变为已驳回状态，可修改后重新提交
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>出库提交校验：</strong>退货出库单提交时，系统会校验库存是否充足。如果关联的入库单尚未完成收货上架，将提示"物料【xxx】库存不足，关联入库单号：IN2026xxxx，请先在WMS入库管理中完成该入库单的上架操作后重试"，方便用户快速定位需要处理的入库单
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>出库完成：</strong>仓库完成退货出库后（库存已扣减），退货单变为已退货状态，流程结束
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>作废：</strong>草稿状态点击"删除"，变为已作废状态
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsReturn">
import { listReturn, getReturn, addReturn, updateReturn, delReturn, submitReturn, auditReturn } from '@/api/pms/return'
import { listOrder, getOrder } from '@/api/pms/order'
import { ArrowRight, QuestionFilled } from '@element-plus/icons-vue'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { pms_return_status, pms_return_type, wms_unit } = proxy.useDict('pms_return_status', 'pms_return_type', 'wms_unit')

const { collapsedCards, toggleCard } = useDetailCard(["c1","c4","c2","c0","v1","v3","v2","v4","a4"])
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_return_index')

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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, returnNo: undefined, supplierName: undefined, status: undefined, returnType: undefined, params: {} },
  auditForm: { returnId: null, auditOpinion: null },
  rules: { orderId: [{ required: true, message: '请选择采购订单', trigger: 'change' }] },
  auditRules: { auditOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }] }
})
const { queryParams, form, auditForm, rules, auditRules } = toRefs(data)

function getList() { loading.value = true; listReturn(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.returnId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { returnId: undefined, returnNo: undefined, receiveId: undefined, receiveNo: undefined, orderId: undefined, orderNo: undefined, supplierId: undefined, supplierName: undefined, status: '0', returnType: '0', returnDate: undefined, totalQty: 0, totalAmount: 0, reason: undefined, handlerName: undefined, remark: undefined, detailList: [] }; proxy.resetForm('returnRef') }
function onSupplierChange(val) { const matched = supplierOptions.value.find(s => s.supplierId === val); form.value.supplierName = matched ? matched.supplierName : undefined }
function loadSupplierOptions() { listSupplier({ pageNum: 1, pageSize: 999 }).then(res => { supplierOptions.value = res.rows || [] }) }
function loadOrderOptions() { listOrder({ pageNum: 1, pageSize: 999 }).then(res => { orderOptions.value = (res.rows || []).filter(o => ['2','3','4','5'].includes(o.status)) }) }
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
function handleAdd() { reset(); open.value = true; title.value = '添加退货单' }
function handleUpdate(row) { reset(); getReturn(row.returnId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改退货单' }) }
function handleView(row) { getReturn(row.returnId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function calcDetail(row) { if (row.qty != null && row.unitPrice != null) row.amount = parseFloat((row.qty * row.unitPrice).toFixed(2)); let tq = 0, ta = 0; form.value.detailList.forEach(d => { if (d.qty) tq += d.qty; if (d.amount) ta += d.amount }); form.value.totalQty = parseFloat(tq.toFixed(2)); form.value.totalAmount = parseFloat(ta.toFixed(2)) }
function handleDeleteDetail(index) { form.value.detailList.splice(index, 1); calcDetail({}) }
function submitForm() { proxy.$refs['returnRef'].validate(valid => { if (valid) { if (form.value.returnId != undefined) { updateReturn(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addReturn(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const returnIds = row.returnId || ids.value; proxy.$modal.confirm('确认删除编号为"' + returnIds + '"的数据？').then(() => delReturn(returnIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('pms/return/export', { ...queryParams.value }, `return_${new Date().getTime()}.xlsx`) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交编号为"' + row.returnNo + '"的退货单审批？').then(() => submitReturn(row.returnId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {}) }
function handleAudit(row) { getReturn(row.returnId).then(res => { auditData.value = res.data; auditForm.value = { returnId: row.returnId, auditOpinion: null }; auditOpen.value = true }) }
function submitAudit(passed) { proxy.$refs['auditRef'].validate(valid => { if (valid) { const status = passed ? '2' : '5'; const actionText = passed ? '通过' : '驳回'; proxy.$modal.confirm(`确认${actionText}该退货单？`).then(() => { return auditReturn(auditForm.value.returnId, status, auditForm.value.auditOpinion) }).then((res) => { if (passed) { const outboundOrderNo = res.outboundOrderNo; const outboundOrderError = res.outboundOrderError; if (outboundOrderError) { proxy.$modal.msgWarning(`审批通过，但出库单生成失败：${outboundOrderError}`); } else if (outboundOrderNo) { proxy.$modal.msgSuccess(`审批通过，已自动生成退货出库单：${outboundOrderNo}`); } else { proxy.$modal.msgSuccess('审批通过'); } } else { proxy.$modal.msgSuccess('已驳回'); } auditOpen.value = false; getList() }).catch(() => {}) } }) }
function cancel() { open.value = false; reset() }

getList()
loadSupplierOptions()
loadOrderOptions()
onActivated(() => { getList() })
</script>

<style scoped>
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

.reject-alert {
  margin-bottom: 16px;
}
</style>
