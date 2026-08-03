<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="收货单号" prop="receiveNo"><el-input v-model="queryParams.receiveNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="采购单号" prop="orderNo"><el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="供应商" prop="supplierName"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px"><el-option v-for="d in pms_receive_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button><el-button type="info" plain icon="More" @click="showAdvanced = !showAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button></el-form-item>
      <template v-if="showAdvanced">
        <el-form-item label="验收人" prop="inspectorName"><el-input v-model="queryParams.inspectorName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
        <el-form-item label="收货日期"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" /></el-form-item>
      </template>
    </el-form>

    <!-- 重点业务提示 -->
    <el-alert type="warning" :closable="false" show-icon class="mb8">
      <template #title>
        <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
          <span style="font-weight: bold;">验收规则提示：</span>
          <el-tag size="small" type="success" effect="dark">合格品 → 自动入库</el-tag>
          <el-tag size="small" type="danger" effect="dark">不合格品 → 入库后发起退货</el-tag>
          <el-tag size="small" type="warning" effect="dark">同一订单不可同时存在多个进行中收货单</el-tag>
          <span style="color: #909399; font-size: 12px;">收货明细由采购订单自动带出，应收数量不能超过未收数量且必须大于0；同一采购订单存在待验收/待审核/已驳回收货单时不可发起新收货</span>
          <el-button link type="primary" size="small" @click="showStatusHelp = true">
            <el-icon><QuestionFilled /></el-icon> 查看详情
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:receive:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:receive:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:receive:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:receive:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="收货单号" prop="receiveNo" :width="colWidth('receiveNo', 160)" resizable sortable="custom" />
      <el-table-column label="采购单号" prop="orderNo" :width="colWidth('orderNo', 160)" resizable />
      <el-table-column label="供应商" prop="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom"><template #default="scope"><dict-tag :options="pms_receive_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="收货日期" prop="receiveDate" :width="colWidth('receiveDate', 120)" resizable align="center" sortable="custom" />
      <el-table-column label="应收数量" prop="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" />
      <el-table-column label="合格数量" prop="qualifiedQty" :width="colWidth('qualifiedQty', 100)" resizable align="right" />
      <el-table-column label="不合格数量" prop="unqualifiedQty" :width="colWidth('unqualifiedQty', 110)" resizable align="right" />
      <el-table-column label="验收人" prop="inspectorName" :width="colWidth('inspectorName', 100)" resizable />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
          <el-table-column label="操作" width="290" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:receive:edit']" v-if="scope.row.status === '0' || scope.row.status === '6'">修改</el-button>
          <el-button link type="primary" icon="Check" @click="handleInspect(scope.row)" v-hasPermi="['pms:receive:edit']" v-if="scope.row.status === '0'">验收</el-button>
          <el-button link type="success" icon="Checked" @click="handleAudit(scope.row)" v-hasPermi="['pms:receive:audit']" v-if="scope.row.status === '5'">审核</el-button>
          <el-button link type="warning" icon="RefreshLeft" @click="handleReturn(scope.row)" v-hasPermi="['pms:return:add']" v-if="(scope.row.status === '1' || scope.row.status === '2') && scope.row.unqualifiedQty > 0">发起退货</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:receive:remove']" v-if="scope.row.status === '0'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

    <el-dialog v-model="open" width="1166px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div><span class="rd-detail-header-title">{{ title }}</span><div class="rd-detail-header-sub" v-if="form.receiveNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ form.receiveNo }}</span></div></div></template>
      <el-form ref="receiveRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="收货单号" prop="receiveNo">
                    <el-input v-model="form.receiveNo" placeholder="保存后自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="采购单号" prop="orderId">
                    <el-select v-model="form.orderId" filterable clearable placeholder="请选择采购订单" style="width: 100%" @change="onOrderChange">
                      <el-option v-for="o in orderOptions" :key="o.orderId" :label="o.orderNo" :value="o.orderId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="收货日期" prop="receiveDate">
                    <el-date-picker v-model="form.receiveDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="供应商" prop="supplierName">
                    <el-input v-model="form.supplierName" placeholder="选择采购订单后自动带出" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="入库仓库" prop="warehouseId">
                    <el-select v-model="form.warehouseId" filterable placeholder="请选择入库仓库" style="width: 100%">
                      <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="验收人" prop="inspectorName">
                    <el-input v-model="form.inspectorName" readonly placeholder="请选择验收人" style="width: 100%" @click="openUserPicker">
                      <template #append>
                        <el-button icon="Search" @click="openUserPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.inspectorName" class="clear-icon" @click.stop="clearInspector"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="应收数量" prop="orderTotalQty">
                    <el-input-number v-model="form.orderTotalQty" :precision="2" :min="0" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="已收数量" prop="orderReceivedQty">
                    <el-input-number v-model="form.orderReceivedQty" :precision="2" :min="0" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="未收数量" prop="orderUnreceivedQty">
                    <el-input-number v-model="form.orderUnreceivedQty" :precision="2" :min="0" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 收货明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>收货明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-table :data="form.detailList" border size="small" :header-cell-style="{ textAlign: 'center' }">
                <el-table-column label="序号" type="index" width="55" align="center" />
                <el-table-column label="物料" prop="materialId" min-width="200">
                  <template #default="scope">
                    <el-select v-model="scope.row.materialId" filterable clearable size="small" placeholder="请选择物料" style="width: 100%" @change="(val) => onMaterialChange(val, scope.$index)">
                      <el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialCode + ' - ' + m.materialName" :value="m.materialId" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" prop="specModel" min-width="120">
                  <template #default="scope"><span>{{ scope.row.specModel }}</span></template>
                </el-table-column>
                <el-table-column label="单位" prop="unit" width="90">
                  <template #default="scope">
                    <el-select v-model="scope.row.unit" size="small" placeholder="单位" style="width: 100%">
                      <el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="应收数量" prop="receiveQty" width="120"><template #default="scope"><el-input-number v-model="scope.row.receiveQty" :precision="2" :min="0" :max="scope.row.unreceivedQty" :controls="false" size="small" style="width: 100px" @change="calcDetail(scope.row)" /><div v-if="scope.row.unreceivedQty != null" style="font-size: 11px; color: #909399; line-height: 1.4; margin-top: 2px;">未收: {{ scope.row.unreceivedQty }}</div></template></el-table-column>
                <el-table-column label="合格数量" prop="qualifiedQty" width="100">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.qualifiedQty" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" :disabled="!isInspectMode" @change="calcDetail(scope.row)" />
                  </template>
                </el-table-column>
                <el-table-column label="不合格数量" prop="unqualifiedQty" width="120" align="right">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.unqualifiedQty" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" :disabled="!isInspectMode" @change="calcDetailAmount(scope.row)" />
                  </template>
                </el-table-column>
                <el-table-column label="单价" prop="unitPrice" width="100"><template #default="scope"><el-input-number v-model="scope.row.unitPrice" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" /></template></el-table-column>
                <el-table-column label="金额" prop="amount" width="110" align="right">
                  <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template>
                </el-table-column>
                <el-table-column label="批次号" prop="batchNo" width="100"><template #default="scope"><el-input v-model="scope.row.batchNo" placeholder="批次" size="small" /></template></el-table-column>
              </el-table>
              <div class="detail-summary" v-if="form.detailList && form.detailList.length > 0">
                <span>合计：应收总数量 {{ totalReceiveQty }} | 合格数量 {{ totalQualifiedQty }} | 不合格数量 {{ totalUnqualifiedQty }}</span>
              </div>
            </div>
          </section>
          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0"><el-row :gutter="20"><el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></el-col></el-row></div>
          </section>
          <!-- 审核记录（驳回时显示） -->
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3" style="display:block">
              <el-alert v-if="form.status === '6'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该记录已被驳回，请根据审核意见修改后重新提交</template>
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

    <!-- 查看对话框 -->
    <el-dialog v-model="viewOpen" width="1166px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div><span class="rd-detail-header-title">收货验收详情</span><div class="rd-detail-header-sub" v-if="viewData.receiveNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.receiveNo }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">收货单号</span><div class="rd-value">{{ viewData.receiveNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ viewData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">入库仓库</span><div class="rd-value">{{ viewData.warehouseName || (warehouseOptions.find(w => w.warehouseId === viewData.warehouseId)?.warehouseName) || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_receive_status" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">收货日期</span><div class="rd-value">{{ viewData.receiveDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">应收总数量</span><div class="rd-value">{{ viewData.totalQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ viewData.qualifiedQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">不合格数量</span><div class="rd-value">{{ viewData.unqualifiedQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">验收人</span><div class="rd-value">{{ viewData.inspectorName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">入库单号</span><div class="rd-value">{{ viewData.inboundOrderNo || (viewData.inboundOrderId ? 'IN' + viewData.inboundOrderId : '-') || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>收货明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <el-table :data="viewData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="100" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="应收数量" prop="receiveQty" width="100" align="right" />
              <el-table-column label="合格数量" prop="qualifiedQty" width="100" align="right" />
              <el-table-column label="不合格数量" prop="unqualifiedQty" width="110" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="100" align="right" />
              <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              <el-table-column label="批次号" prop="batchNo" width="100" />
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('v3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v3" style="display:block">
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
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 验收对话框 -->
    <el-dialog v-model="inspectOpen" width="1166px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div><span class="rd-detail-header-title">收货验收</span><div class="rd-detail-header-sub" v-if="inspectData.receiveNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ inspectData.receiveNo }}</span></div></div></template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">收货单号</span><div class="rd-value">{{ inspectData.receiveNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ inspectData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ inspectData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">收货日期</span><div class="rd-value">{{ inspectData.receiveDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">应收总数量</span><div class="rd-value">{{ inspectData.totalQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">验收人</span><div class="rd-value">{{ inspectData.inspectorName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 验收明细 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>验收明细</div></div>
          <div class="rd-card-body" style="display:block">
            <el-alert type="warning" :closable="false" show-icon class="mb8">
              <template #title>请填写合格数量和不合格数量。默认不合格=应收-合格，可手动调低。如本次仅部分收货且全部合格，请将不合格设为0</template>
            </el-alert>
            <el-table :data="inspectData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="100" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="应收数量" prop="receiveQty" width="100" align="right" />
              <el-table-column label="合格数量" prop="qualifiedQty" width="120">
                <template #default="scope">
                  <el-input-number v-model="scope.row.qualifiedQty" :precision="2" :min="0" :max="scope.row.receiveQty" :controls="false" size="small" style="width: 100px" @change="calcInspectDetail(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column label="不合格数量" prop="unqualifiedQty" width="130" align="right">
                <template #default="scope">
                  <el-input-number v-model="scope.row.unqualifiedQty" :precision="2" :min="0" :max="scope.row.receiveQty ? (scope.row.receiveQty - (scope.row.qualifiedQty || 0) + (scope.row.unqualifiedQty || 0)) : 0" :controls="false" size="small" style="width: 90px" @change="calcInspectAmount(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column label="单价" prop="unitPrice" width="100" align="right" />
              <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              <el-table-column label="批次号" prop="batchNo" width="100">
                <template #default="scope"><el-input v-model="scope.row.batchNo" placeholder="批次" size="small" /></template>
              </el-table-column>
            </el-table>
            <div class="detail-summary" v-if="inspectData.detailList && inspectData.detailList.length > 0">
              <span>合计：合格数量 <span class="inspect-total-qty">{{ inspectTotalQualifiedQty }}</span> | 不合格数量 <span class="inspect-total-qty">{{ inspectTotalUnqualifiedQty }}</span></span>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitInspect">确认验收</el-button>
        <el-button @click="inspectOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditOpen" width="1166px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">收货验收审核</div>
            <div class="rd-detail-header-sub" v-if="auditData.receiveNo">
              <span class="rd-detail-header-no">编号：{{ auditData.receiveNo }}</span>
              <dict-tag :options="pms_receive_status" :value="auditData.status" />
            </div>
          </div>
        </div>
      </template>
      
      <div class="rd-page">
        <!-- 基本信息卡片 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>
              基本信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>收货单号</span>
                <div class="rd-value">{{ auditData.receiveNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>采购单号</span>
                <div class="rd-value">{{ auditData.orderNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>供应商</span>
                <div class="rd-value">{{ auditData.supplierName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>入库仓库</span>
                <div class="rd-value">{{ auditData.warehouseName || (warehouseOptions.find(w => w.warehouseId === auditData.warehouseId)?.warehouseName) || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>收货日期</span>
                <div class="rd-value">{{ auditData.receiveDate || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>验收人</span>
                <div class="rd-value">{{ auditData.inspectorName || '-' }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 验收结果卡片 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>
              验收结果
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid rd-grid--3col">
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>应收总数量</span>
                <div class="rd-value rd-value--large">{{ auditData.totalQty || '0' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot" style="background: #10b981;"></span>合格数量</span>
                <div class="rd-value rd-value--large" style="color: #10b981;">{{ auditData.qualifiedQty || '0' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot" style="background: #ef4444;"></span>不合格数量</span>
                <div class="rd-value rd-value--large" :style="{ color: (auditData.unqualifiedQty > 0) ? '#ef4444' : '#111827' }">{{ auditData.unqualifiedQty || '0' }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 验收明细卡片 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>
              验收明细
            </div>
          </div>
          <div class="rd-card-body">
            <el-table :data="auditData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="应收数量" prop="receiveQty" width="100" align="right" />
              <el-table-column label="合格数量" width="100" align="right">
                <template #default="scope">
                  <span style="color: #10b981; font-weight: bold;">{{ scope.row.qualifiedQty || 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column label="不合格数量" width="100" align="right">
                <template #default="scope">
                  <span :style="{ color: (scope.row.unqualifiedQty > 0) ? '#ef4444' : '#111827', fontWeight: (scope.row.unqualifiedQty > 0) ? 'bold' : 'normal' }">{{ scope.row.unqualifiedQty || 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="110" align="right">
                <template #default="scope">
                  <span class="rd-amount">{{ formatMoney(scope.row.unitPrice) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="金额" width="120" align="right">
                <template #default="scope">
                  <span class="rd-amount rd-value--large">{{ formatMoney(scope.row.amount) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>

        <!-- 历史审核记录卡片 -->
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>
              历史审核记录
            </div>
          </div>
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

        <!-- 审批意见卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a1">
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

    <!-- 验收人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择验收人" @confirm="onUserPickerConfirm" />

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="收货验收业务状态流转说明" width="700px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">待验收</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已验收</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已入库</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">部分验收</el-tag>
            <el-tag size="small" type="info">审核通过后·存在不合格品</el-tag>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-tag size="small" type="info">审核驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="info">待验收</el-tag>
            <el-tag size="small" type="info">修改后重置</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待验收">
            新建收货单后的初始状态，可编辑、删除、验收
          </el-descriptions-item>
          <el-descriptions-item label="待审核">
            验收提交后等待审核，可审核通过或驳回
          </el-descriptions-item>
          <el-descriptions-item label="部分验收">
            审核通过且存在不合格物料，可发起退货
          </el-descriptions-item>
          <el-descriptions-item label="已验收">
            审核通过且全部合格，合格品已自动入库
          </el-descriptions-item>
          <el-descriptions-item label="已驳回">
            审核未通过，可修改后重新验收
          </el-descriptions-item>
          <el-descriptions-item label="已入库">
            审核通过且合格物料已入库完成，收货流程结束
          </el-descriptions-item>
          <el-descriptions-item label="已作废">
            收货单被作废，不再有效
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">
                <el-icon style="margin-right: 4px;"><CircleCheck /></el-icon>合格品处理
              </div>
              <div class="highlight-card-body">
                验收审核通过后，系统<strong>自动生成采购入库单</strong>（WMS入库单类型为采购入库），合格品和不合格品均入库，不合格品标记为"不合格品待退"便于仓库区分存放
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">
                <el-icon style="margin-right: 4px;"><CircleClose /></el-icon>不合格品处理
              </div>
              <div class="highlight-card-body">
                不合格品已随合格品一并入库（标记待退）。存在不合格数量的部分验收或已验收收货单，需点击"发起退货"按钮，系统<strong>自动创建退货单</strong>，审批通过后生成退货出库单退回供应商
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 16px;">
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title">
                <el-icon style="margin-right: 4px;"><QuestionFilled /></el-icon>收货数量限制
              </div>
              <div class="highlight-card-body">
                收货明细由采购订单自动带出，<strong>不可增删行</strong>。每行应收数量必须大于0，且不能超过该物料的未收数量（订单数量 - 已收数量）。系统在输入框下方显示当前未收数量供参考
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">
                <el-icon style="margin-right: 4px;"><CircleClose /></el-icon>收货单互斥规则
              </div>
              <div class="highlight-card-body">
                同一采购订单<strong>不可同时存在多个进行中的收货单</strong>（待验收、待审核、已驳回均为进行中）。必须等待当前收货单完成审核（已验收/部分验收/已入库）或作废后，才能发起新的收货
              </div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务触发说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建收货：</strong>选择采购订单，自动带出供应商和物料明细（不可增删行），应收数量默认为未收数量，可修改但不能超过未收数量且必须大于0。同一订单存在进行中收货单时不可新建
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>验收操作：</strong>点击"验收"按钮，填写每行物料的合格数量，不合格数量自动计算（应收数量 - 合格数量）。提交后进入待审核状态
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审核操作：</strong>审核通过后，系统<strong style="color: #67c23a;">自动生成采购入库单</strong>（合格品和不合格品均入库，不合格品标记待退）。全部合格→已验收；存在不合格→部分验收
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审核驳回：</strong>审核驳回后可修改验收结果，重新提交验收
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>发起退货：</strong>存在不合格数量的部分验收或已验收收货单，需点击"发起退货"按钮，<strong style="color: #f56c6c;">自动创建退货单</strong>。退货审批通过后自动生成退货出库单，将不合格品退回供应商
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsReceive">
import { listReceive, getReceive, addReceive, updateReceive, delReceive, inspectReceive, auditReceive, getInProgressOrderIds } from '@/api/pms/receive'
import { addReturn } from '@/api/pms/return'
import { listOrder, getOrder } from '@/api/pms/order'
import { listMaterial } from '@/api/wms/material'
import { listWarehouse } from '@/api/wms/warehouse'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'
import UserPicker from '@/components/UserPicker/index.vue'
import { CircleClose, ArrowRight, QuestionFilled, CircleCheck } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { pms_receive_status, wms_unit } = proxy.useDict('pms_receive_status', 'wms_unit')

const { collapsedCards, toggleCard } = useDetailCard(["c1", "c2", "c0", "c3", "v1", "v2", "v3", "a1"])
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_receive_index')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const inspectOpen = ref(false)
const inspectData = ref({})
const auditOpen = ref(false)
const auditData = ref({})
const auditForm = reactive({ receiveId: undefined, auditOpinion: '' })
const auditRules = {
  auditOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}
const auditRef = ref()
const showStatusHelp = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const orderOptions = ref([])
const materialOptions = ref([])
const warehouseOptions = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, receiveNo: undefined, orderNo: undefined, supplierName: undefined, status: undefined, params: {} },
  rules: {
    orderId: [{ required: true, message: '请选择采购订单', trigger: 'change' }],
    receiveDate: [{ required: true, message: '收货日期不能为空', trigger: 'change' }],
    warehouseId: [{ required: true, message: '请选择入库仓库', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 是否处于验收模式（已存在记录且状态为待验收/部分验收时，可填写合格/不合格数量） */
const isInspectMode = computed(() => {
  return form.value.receiveId != undefined && (form.value.status === '0' || form.value.status === '1')
})

/** 合计：应收总数量 */
const totalReceiveQty = computed(() => {
  if (!form.value.detailList) return '0.00'
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.receiveQty) || 0), 0).toFixed(2)
})

/** 合计：合格数量 */
const totalQualifiedQty = computed(() => {
  if (!form.value.detailList) return '0.00'
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.qualifiedQty) || 0), 0).toFixed(2)
})

/** 合计：不合格数量 */
const totalUnqualifiedQty = computed(() => {
  if (!form.value.detailList) return '0.00'
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.unqualifiedQty) || 0), 0).toFixed(2)
})

/** 验收弹窗合计：合格数量 */
const inspectTotalQualifiedQty = computed(() => {
  if (!inspectData.value.detailList) return '0.00'
  return inspectData.value.detailList.reduce((sum, item) => sum + (Number(item.qualifiedQty) || 0), 0).toFixed(2)
})

/** 验收弹窗合计：不合格数量 */
const inspectTotalUnqualifiedQty = computed(() => {
  if (!inspectData.value.detailList) return '0.00'
  return inspectData.value.detailList.reduce((sum, item) => sum + (Number(item.unqualifiedQty) || 0), 0).toFixed(2)
})

function getList() {
  loading.value = true
  listReceive(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, dateRange.value)
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.params = {}
  handleQuery()
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

function handleSelectionChange(selection) {
  ids.value = selection.map(i => i.receiveId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    receiveId: undefined,
    receiveNo: undefined,
    orderId: undefined,
    orderNo: undefined,
    supplierId: undefined,
    supplierName: undefined,
    status: '0',
    receiveDate: undefined,
    totalQty: 0,
    orderTotalQty: 0,
    orderReceivedQty: 0,
    orderUnreceivedQty: 0,
    qualifiedQty: 0,
    unqualifiedQty: 0,
    inspectorId: undefined,
    inspectorName: undefined,
    warehouseId: undefined,
    remark: undefined,
    detailList: []
  }
  proxy.resetForm('receiveRef')
}

/** 选择采购订单后自动带出供应商、采购员（作为验收人）和物料明细 */
function onOrderChange(val) {
  if (!val) {
    form.value.orderId = undefined
    form.value.orderNo = undefined
    form.value.supplierId = undefined
    form.value.supplierName = undefined
    form.value.inspectorId = undefined
    form.value.inspectorName = undefined
    form.value.detailList = []
    return
  }
  getOrder(val).then(res => {
    const order = res.data
    form.value.orderId = order.orderId
    form.value.orderNo = order.orderNo
    form.value.supplierId = order.supplierId
    form.value.supplierName = order.supplierName
    // 自动带出采购员作为验收人
    form.value.inspectorId = order.buyerId
    form.value.inspectorName = order.buyerName
    // 计算主表应收/已收/未收
    let orderTotal = 0, orderReceived = 0
    if (order.detailList) {
      order.detailList.forEach(d => {
        orderTotal += (d.qty || 0)
        orderReceived += (d.receivedQty || 0)
      })
    }
    form.value.orderTotalQty = parseFloat(orderTotal.toFixed(2))
    form.value.orderReceivedQty = parseFloat(orderReceived.toFixed(2))
    form.value.orderUnreceivedQty = parseFloat((orderTotal - orderReceived).toFixed(2))
    // 从采购订单明细带出收货明细，应收数量 = 订单数量 - 已收数量（未收数量）
    if (order.detailList && order.detailList.length > 0) {
      form.value.detailList = order.detailList.map(d => ({
        orderDetailId: d.detailId,
        materialId: d.materialId,
        materialCode: d.materialCode,
        materialName: d.materialName,
        specModel: d.specModel,
        unit: d.unit,
        receiveQty: parseFloat((d.qty - (d.receivedQty || 0)).toFixed(2)),
        unreceivedQty: parseFloat((d.qty - (d.receivedQty || 0)).toFixed(2)),
        qualifiedQty: 0,
        unqualifiedQty: 0,
        unitPrice: d.unitPrice,
        amount: d.amount,
        batchNo: ''
      }))
    } else {
      form.value.detailList = []
    }
    calcDetail({})
  })
}

/** 选择物料后自动带出物料信息 */
function onMaterialChange(val, index) {
  const matched = materialOptions.value.find(m => m.materialId === val)
  if (matched) {
    form.value.detailList[index].materialCode = matched.materialCode
    form.value.detailList[index].materialName = matched.materialName
    form.value.detailList[index].specModel = matched.specModel || matched.specification
    form.value.detailList[index].unit = matched.unit
  }
}

/** 加载采购订单选项（已审批/已下单/部分到货的订单可收货，排除有进行中收货单的订单） */
function loadOrderOptions() {
  Promise.all([
    listOrder({ pageNum: 1, pageSize: 999 }),
    getInProgressOrderIds().catch(() => ({ data: [] }))
  ]).then(([orderRes, inProgressRes]) => {
    // 仅显示已审批(2)、已下单(3)、部分到货(4)状态的订单
    const inProgressIds = (inProgressRes && inProgressRes.data) || []
    orderOptions.value = (orderRes.rows || []).filter(o =>
      (o.status === '2' || o.status === '3' || o.status === '4') &&
      !inProgressIds.includes(o.orderId)
    )
  })
}

/** 加载物料主数据选项 */
function loadMaterialOptions() {
  listMaterial({ pageNum: 1, pageSize: 999, status: '0' }).then(res => {
    materialOptions.value = res.rows || []
  })
}

/** 加载仓库选项 */
function loadWarehouseOptions() {
  listWarehouse({ pageNum: 1, pageSize: 999 }).then(res => {
    warehouseOptions.value = res.rows || []
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加收货验收'
}

function handleUpdate(row) {
  reset()
  getReceive(row.receiveId || ids.value[0]).then(res => {
    form.value = res.data
    if (!form.value.detailList) form.value.detailList = []
    // 计算每行明细的未收数量，用于限制应收数量上限
    if (form.value.orderId) {
      getOrder(form.value.orderId).then(orderRes => {
        const order = orderRes.data
        if (order && order.detailList && form.value.detailList) {
          form.value.detailList.forEach(d => {
            if (d.orderDetailId) {
              const od = order.detailList.find(o => o.detailId === d.orderDetailId)
              if (od) {
                d.unreceivedQty = parseFloat(((od.qty || 0) - (od.receivedQty || 0)).toFixed(2))
              }
            }
          })
        }
        open.value = true
        title.value = '修改收货验收'
      })
    } else {
      open.value = true
      title.value = '修改收货验收'
    }
  })
}

function handleView(row) {
  getReceive(row.receiveId).then(res => {
    viewData.value = res.data
    viewOpen.value = true
  })
}

/** 合格数量变化时，自动设置不合格数量默认值 = 应收数量 - 合格数量 */
function calcDetail(row) {
  if (row && row.receiveQty != null && row.qualifiedQty != null) {
    let diff = parseFloat(row.receiveQty) - parseFloat(row.qualifiedQty || 0)
    row.unqualifiedQty = parseFloat(diff.toFixed(2))
    if (row.unqualifiedQty < 0) row.unqualifiedQty = 0
  }
  calcDetailAmount(row)
}

/** 不合格数量或单价变化时，只重算金额和合计 */
function calcDetailAmount(row) {
  // 计算明细行金额（按实际收货数量 = 合格 + 不合格）
  if (row && row.qualifiedQty != null && row.unqualifiedQty != null && row.unitPrice != null) {
    let actualReceived = parseFloat(row.qualifiedQty || 0) + parseFloat(row.unqualifiedQty || 0)
    row.amount = parseFloat((actualReceived * row.unitPrice).toFixed(2))
  }
  let tq = 0, q = 0, uq = 0
  form.value.detailList.forEach(d => {
    if (d.receiveQty) tq += d.receiveQty
    if (d.qualifiedQty) q += d.qualifiedQty
    if (d.unqualifiedQty) uq += d.unqualifiedQty
  })
  form.value.totalQty = parseFloat(tq.toFixed(2))
  form.value.qualifiedQty = parseFloat(q.toFixed(2))
  form.value.unqualifiedQty = parseFloat(uq.toFixed(2))
}

function handleAddDetail() {
  if (!form.value.detailList) form.value.detailList = []
  form.value.detailList.push({
    materialId: null,
    materialCode: '',
    materialName: '',
    specModel: '',
    unit: '',
    receiveQty: 0,
    unreceivedQty: null,
    qualifiedQty: 0,
    unqualifiedQty: 0,
    unitPrice: 0,
    amount: 0,
    batchNo: ''
  })
}

function handleDeleteDetail(index) {
  form.value.detailList.splice(index, 1)
  calcDetail({})
}

function submitForm() {
  proxy.$refs['receiveRef'].validate(valid => {
    if (valid) {
      // 校验：每行明细的应收数量必须大于0
      if (form.value.detailList && form.value.detailList.length > 0) {
        for (let i = 0; i < form.value.detailList.length; i++) {
          const d = form.value.detailList[i]
          if (d.receiveQty == null || parseFloat(d.receiveQty) <= 0) {
            proxy.$modal.msgError(`第${i + 1}行物料的应收数量必须大于0`)
            return
          }
          if (d.orderDetailId != null && d.unreceivedQty != null && d.receiveQty != null) {
            if (parseFloat(d.receiveQty) > parseFloat(d.unreceivedQty)) {
              proxy.$modal.msgError(`第${i + 1}行物料的应收数量(${d.receiveQty})不能大于未收数量(${d.unreceivedQty})`)
              return
            }
          }
        }
      }
      if (form.value.receiveId != undefined) {
        updateReceive(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addReceive(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const receiveIds = row.receiveId || ids.value
  proxy.$modal.confirm('确认删除编号为"' + receiveIds + '"的数据？').then(() => delReceive(receiveIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleInspect(row) {
  getReceive(row.receiveId).then(res => {
    inspectData.value = res.data
    if (!inspectData.value.detailList) inspectData.value.detailList = []
    inspectOpen.value = true
  })
}

/** 合格数量变化时，自动设置不合格数量默认值 = 应收数量 - 合格数量 */
function calcInspectDetail(row) {
  if (row && row.receiveQty != null && row.qualifiedQty != null) {
    let diff = parseFloat(row.receiveQty) - parseFloat(row.qualifiedQty || 0)
    row.unqualifiedQty = parseFloat(diff.toFixed(2))
    if (row.unqualifiedQty < 0) row.unqualifiedQty = 0
  }
  calcInspectAmount(row)
}

/** 不合格数量或单价变化时，只重算金额和合计 */
function calcInspectAmount(row) {
  // 计算明细行金额（按实际收货数量 = 合格 + 不合格）
  if (row.qualifiedQty != null && row.unqualifiedQty != null && row.unitPrice != null) {
    let actualReceived = parseFloat(row.qualifiedQty || 0) + parseFloat(row.unqualifiedQty || 0)
    row.amount = parseFloat((actualReceived * row.unitPrice).toFixed(2))
  }
  // 更新主表合计
  let q = 0, uq = 0
  inspectData.value.detailList.forEach(d => {
    if (d.qualifiedQty) q += d.qualifiedQty
    if (d.unqualifiedQty) uq += d.unqualifiedQty
  })
  inspectData.value.qualifiedQty = parseFloat(q.toFixed(2))
  inspectData.value.unqualifiedQty = parseFloat(uq.toFixed(2))
}

/** 提交验收 */
function submitInspect() {
  // 校验：每行必须有合格数量
  const hasEmpty = inspectData.value.detailList.some(d => d.qualifiedQty == null || d.qualifiedQty === '')
  if (hasEmpty) {
    proxy.$modal.msgWarning('请填写所有物料的合格数量')
    return
  }
  // 过滤掉合格数量和不合格数量都为0的明细行（说明本次未实际收货）
  const validDetails = inspectData.value.detailList.filter(d => {
    const q = parseFloat(d.qualifiedQty) || 0
    const uq = parseFloat(d.unqualifiedQty) || 0
    return q > 0 || uq > 0
  })
  if (validDetails.length === 0) {
    proxy.$modal.msgWarning('合格数量和不合格数量不能全为0，请填写实际收货数量')
    return
  }
  const removedCount = inspectData.value.detailList.length - validDetails.length
  const confirmMsg = removedCount > 0
    ? `有${removedCount}条物料明细合格数量和不合格数量均为0，将自动删除。确认提交验收结果？`
    : '确认提交验收结果？提交后将进入待审核状态。'
  proxy.$modal.confirm(confirmMsg).then(() => {
    return inspectReceive({
      receiveId: inspectData.value.receiveId,
      detailList: validDetails.map(d => ({
        detailId: d.detailId,
        receiveQty: d.receiveQty,
        qualifiedQty: d.qualifiedQty,
        unqualifiedQty: d.unqualifiedQty,
        unitPrice: d.unitPrice,
        amount: d.amount,
        batchNo: d.batchNo
      }))
    })
  }).then(() => {
    proxy.$modal.msgSuccess('验收已提交，待审核')
    inspectOpen.value = false
    getList()
  }).catch(() => {})
}

/** 发起退货 — 根据不合格物料自动创建退货单 */
function handleReturn(row) {
  getReceive(row.receiveId).then(res => {
    const receive = res.data
    // 筛选不合格数量 > 0 的明细
    const unqualifiedDetails = (receive.detailList || []).filter(d => d.unqualifiedQty != null && d.unqualifiedQty > 0)
    if (unqualifiedDetails.length === 0) {
      proxy.$modal.msgWarning('该收货单没有不合格物料，无需退货')
      return
    }
    proxy.$modal.confirm(`确认基于收货单 ${receive.receiveNo} 的 ${unqualifiedDetails.length} 条不合格物料发起退货？`).then(() => {
      return addReturn({
        orderId: receive.orderId,
        orderNo: receive.orderNo,
        receiveId: receive.receiveId,
        receiveNo: receive.receiveNo,
        supplierId: receive.supplierId,
        supplierName: receive.supplierName,
        returnType: '0', // 默认质量问题
        returnDate: new Date().toISOString().slice(0, 10),
        reason: '收货验收不合格物料退货',
        handlerName: receive.inspectorName,
        status: '0', // 草稿
        detailList: unqualifiedDetails.map(d => ({
          materialId: d.materialId,
          materialCode: d.materialCode,
          materialName: d.materialName,
          specModel: d.specModel,
          unit: d.unit,
          qty: d.unqualifiedQty,
          unitPrice: d.unitPrice,
          amount: parseFloat((d.unqualifiedQty * (d.unitPrice || 0)).toFixed(2)),
          returnReason: '验收不合格'
        }))
      })
    }).then(res => {
      proxy.$modal.msgSuccess('退货单已创建，请前往退货管理提交审批')
    }).catch(() => {})
  })
}

/** 打开审核弹窗 */
function handleAudit(row) {
  getReceive(row.receiveId).then(res => {
    auditData.value = res.data
    auditForm.receiveId = row.receiveId
    auditForm.auditOpinion = ''
    auditOpen.value = true
  })
}

/** 提交审核 */
function submitAudit(passed) {
  proxy.$refs['auditRef'].validate(valid => {
    if (!valid) return
    const status = passed ? '2' : '6'
    const actionText = passed ? '通过' : '驳回'
    proxy.$modal.confirm(`确认${actionText}该记录？`).then(() => {
      return auditReceive(auditForm.receiveId, status, auditForm.auditOpinion)
    }).then((res) => {
      if (passed) {
        const inboundOrderNo = res.inboundOrderNo
        const inboundOrderError = res.inboundOrderError
        if (inboundOrderError) {
          proxy.$modal.msgWarning(`审核通过，但入库单生成失败：${inboundOrderError}`)
        } else if (inboundOrderNo) {
          proxy.$modal.msgSuccess(`审核通过，已自动生成入库单：${inboundOrderNo}`)
        } else {
          proxy.$modal.msgWarning('审核通过，但未生成入库单（未检测到合格物料或未设置入库仓库）')
        }
      } else {
        proxy.$modal.msgSuccess('已驳回')
      }
      auditOpen.value = false
      getList()
    }).catch(() => {})
  })
}

function handleExport() {
  proxy.download('pms/receive/export', { ...queryParams.value }, `receive_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** 打开验收人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.inspectorId)
}

/** 验收人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.inspectorId = user.userId
  form.value.inspectorName = user.nickName
}

/** 清除验收人 */
function clearInspector() {
  form.value.inspectorId = undefined
  form.value.inspectorName = undefined
}

loadOrderOptions()
loadMaterialOptions()
loadWarehouseOptions()
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.detail-summary {
  text-align: right;
  padding: 10px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-top: none;
  font-weight: bold;
}
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
.reject-alert {
  margin-bottom: 16px;
}
.inspect-total-qty {
  color: #409eff;
  font-weight: bold;
}
.auto-calc-qty {
  font-variant-numeric: tabular-nums;
  color: #f56c6c;
  font-weight: 600;
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

.highlight-card {
  border-radius: 8px;
  padding: 16px;
  border: 1px solid;
}

.highlight-success {
  background-color: #f0f9ff;
  border-color: #b3e19d;
}

.highlight-danger {
  background-color: #fef0f0;
  border-color: #fbc4c4;
}

.highlight-info {
  background-color: #f4f4f5;
  border-color: #d3d4d6;
}

.highlight-warning {
  background-color: #fdf6ec;
  border-color: #f5dab1;
}

.highlight-card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.highlight-success .highlight-card-title {
  color: #67c23a;
}

.highlight-danger .highlight-card-title {
  color: #f56c6c;
}

.highlight-info .highlight-card-title {
  color: #909399;
}

.highlight-warning .highlight-card-title {
  color: #e6a23c;
}

.highlight-card-body {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

:deep(.el-timeline-item__node) {
  background-color: transparent;
  border: 2px solid;
}

/* 3列网格 */
.rd-grid--3col {
  grid-template-columns: repeat(3, 1fr);
}

/* 高亮卡片 - 用于审核操作区 */
.rd-card--highlight {
  border: 2px solid #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.rd-card--highlight .rd-card-header {
  background: linear-gradient(to right, #eff6ff, #fff);
}

/* 审核对话框底部样式 */
:deep(.rd-dialog .el-dialog__footer) {
  background: #fff;
  border-top: 1px solid #e5e7eb;
  padding: 16px 24px;
}
</style>
