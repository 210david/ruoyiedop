<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="发货单号" prop="shipmentNo"><el-input v-model="queryParams.shipmentNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
          <el-form-item label="订单编号" prop="orderNo"><el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
          <el-form-item label="客户名称" prop="customerName"><el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
          <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px"><el-option v-for="d in marketing_shipment_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
          <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
        </el-form>

        <!-- 业务提示 -->
        <el-alert type="warning" :closable="false" show-icon class="mb8">
          <template #title>
            <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
              <span style="font-weight: bold;">发货规则提示：</span>
              <el-tag size="small" type="success" effect="dark">同一订单支持多次发货</el-tag>
              <el-tag size="small" type="primary" effect="dark">发货数量不能超过未发货数量</el-tag>
              <el-tag size="small" type="warning" effect="dark">确认发货后自动更新订单状态</el-tag>
              <span style="color: #909399; font-size: 12px;">发货明细由订单自动带出，发货数量必须大于0且不能超过订单未发货数量；确认发货后系统自动更新订单已发货数量</span>
            </div>
          </template>
        </el-alert>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:shipment:add']">新增</el-button></el-col>
          <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:shipment:edit']">修改</el-button></el-col>
          <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:shipment:remove']">删除</el-button></el-col>
          <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:shipment:export']">导出</el-button></el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="发货单号" prop="shipmentNo" :width="colWidth('shipmentNo', 160)" resizable />
          <el-table-column label="订单编号" prop="orderNo" :width="colWidth('orderNo', 160)" resizable />
          <el-table-column label="客户名称" prop="customerName" :width="colWidth('customerName', 200)" resizable show-overflow-tooltip />
          <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center"><template #default="scope"><dict-tag :options="marketing_shipment_status" :value="scope.row.status" /></template></el-table-column>
          <el-table-column label="发货日期" prop="shipmentDate" :width="colWidth('shipmentDate', 120)" resizable align="center" />
          <el-table-column label="发货数量" prop="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" />
          <el-table-column label="发货金额" prop="totalAmount" :width="colWidth('totalAmount', 120)" resizable align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template></el-table-column>
          <el-table-column label="物流公司" prop="logisticsCompany" :width="colWidth('logisticsCompany', 120)" resizable show-overflow-tooltip />
          <el-table-column label="物流单号" prop="trackingNo" :width="colWidth('trackingNo', 140)" resizable show-overflow-tooltip />
          <el-table-column label="发货人" prop="shipperName" :width="colWidth('shipperName', 100)" resizable />
          <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
          <el-table-column label="操作" width="260" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:shipment:edit']" v-if="scope.row.status === '0'">修改</el-button>
              <el-button link type="success" icon="Promotion" @click="handleConfirm(scope.row)" v-hasPermi="['marketing:shipment:deliver']" v-if="scope.row.status === '0'">确认发货</el-button>
              <el-button link type="warning" icon="CircleCheck" @click="handleReceive(scope.row)" v-hasPermi="['marketing:shipment:receive']" v-if="scope.row.status === '1'">签收</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:shipment:remove']" v-if="scope.row.status === '0'">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></div><span class="rd-detail-header-title">{{ title }}</span><div class="rd-detail-header-sub" v-if="form.shipmentNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ form.shipmentNo }}</span></div></div></template>
      <el-form ref="shipmentRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="发货单号" prop="shipmentNo">
                    <el-input v-model="form.shipmentNo" placeholder="保存后自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="关联订单" prop="orderId">
                    <el-select v-model="form.orderId" filterable clearable placeholder="请选择订单" style="width: 100%" @change="onOrderChange" :disabled="form.shipmentId != undefined">
                      <el-option v-for="o in orderOptions" :key="o.orderId" :label="o.orderNo + (o.customerName ? ' - ' + o.customerName : '')" :value="o.orderId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="客户名称" prop="customerName">
                    <el-input v-model="form.customerName" placeholder="选择订单后自动带出" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="合同编号" prop="contractNo">
                    <el-input v-model="form.contractNo" placeholder="选择订单后自动带出" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="订单数量" prop="orderTotalQty"><el-input-number v-model="form.orderTotalQty" :precision="2" :min="0" disabled style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="已发数量" prop="orderShippedQty"><el-input-number v-model="form.orderShippedQty" :precision="2" :min="0" disabled style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="未发数量" prop="orderUnshippedQty"><el-input-number v-model="form.orderUnshippedQty" :precision="2" :min="0" disabled style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 发货明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>发货明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-table :data="form.detailList" border size="small" :header-cell-style="{ textAlign: 'center' }">
                <el-table-column label="序号" type="index" width="55" align="center" />
                <el-table-column label="商品名称" prop="productName" min-width="150" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="productSpec" min-width="120" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="80" align="center" />
                <el-table-column label="订单数量" prop="orderQty" width="100" align="right" />
                <el-table-column label="已发数量" prop="shippedQty" width="100" align="right" />
                <el-table-column label="未发数量" prop="unshippedQty" width="100" align="right" />
                <el-table-column label="发货数量" prop="shipQty" width="140">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.shipQty" :precision="2" :min="0" :max="scope.row.unshippedQty" :controls="false" size="small" style="width: 110px" @change="calcDetail(scope.row)" />
                  </template>
                </el-table-column>
                <el-table-column label="单价" prop="unitPrice" width="100"><template #default="scope"><el-input-number v-model="scope.row.unitPrice" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" @change="calcDetail(scope.row)" /></template></el-table-column>
                <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              </el-table>
              <div class="detail-summary" v-if="form.detailList && form.detailList.length > 0">
                <span>合计：发货总数量 {{ totalShipQty }} | 发货总金额 {{ formatMoney(totalShipAmount) }}</span>
              </div>
            </div>
          </section>
          <!-- 收货信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>收货信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="收货人" prop="receiverName"><el-input v-model="form.receiverName" placeholder="请输入收货人" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="收货电话" prop="receiverPhone"><el-input v-model="form.receiverPhone" placeholder="请输入收货电话" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="收货地址" prop="receiverAddress"><el-input v-model="form.receiverAddress" placeholder="请输入收货地址" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0"><el-row :gutter="20"><el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></el-col></el-row></div>
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
    <el-dialog v-model="viewOpen" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></div><span class="rd-detail-header-title">发货详情</span><div class="rd-detail-header-sub" v-if="viewData.shipmentNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.shipmentNo }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发货单号</span><div class="rd-value">{{ viewData.shipmentNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ viewData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewData.contractNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewData.customerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="marketing_shipment_status" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">发货日期</span><div class="rd-value">{{ viewData.shipmentDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发货数量</span><div class="rd-value">{{ viewData.totalQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发货金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.totalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">物流公司</span><div class="rd-value">{{ viewData.logisticsCompany || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物流单号</span><div class="rd-value">{{ viewData.trackingNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发货人</span><div class="rd-value">{{ viewData.shipperName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">收货人</span><div class="rd-value">{{ viewData.receiverName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">收货电话</span><div class="rd-value">{{ viewData.receiverPhone || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">收货地址</span><div class="rd-value">{{ viewData.receiverAddress || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>发货明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <el-table :data="viewData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="商品名称" prop="productName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="productSpec" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center" />
              <el-table-column label="发货数量" prop="shipQty" width="100" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="100" align="right" />
              <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 确认发货对话框 -->
    <el-dialog v-model="confirmOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></div><span class="rd-detail-header-title">确认发货</span><div class="rd-detail-header-sub" v-if="confirmForm.shipmentNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ confirmForm.shipmentNo }}</span></div></div></template>
      <el-form ref="confirmRef" :model="confirmForm" :rules="confirmRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>发货信息</div></div>
            <div class="rd-card-body">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">发货单号</span><div class="rd-value">{{ confirmForm.shipmentNo || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ confirmForm.orderNo || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ confirmForm.customerName || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">发货数量</span><div class="rd-value">{{ confirmForm.totalQty || '-' }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>物流信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物流公司" prop="logisticsCompany"><el-input v-model="confirmForm.logisticsCompany" placeholder="请输入物流公司" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物流单号" prop="trackingNo"><el-input v-model="confirmForm.trackingNo" placeholder="请输入物流单号" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发货日期" prop="shipmentDate"><el-date-picker v-model="confirmForm.shipmentDate" type="date" value-format="YYYY-MM-DD" placeholder="选择发货日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitConfirm">确认发货</el-button>
        <el-button @click="confirmOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkShipment">
import { listShipment, getShipment, addShipment, updateShipment, delShipment, confirmShipment, receiveShipment, getInProgressOrderIds } from '@/api/mk/shipment'
import { listOrder, getOrder } from '@/api/mk/order'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { marketing_shipment_status } = proxy.useDict('marketing_shipment_status')

const { collapsedCards, toggleCard } = useDetailCard(["c1", "c2", "c3", "c0", "v1", "v2"])
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('mk_shipment_index')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const confirmOpen = ref(false)
const confirmForm = ref({})
const loading = ref(true)
const showSearch = ref(true)
const orderOptions = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const confirmRules = {
  logisticsCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  trackingNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
  shipmentDate: [{ required: true, message: '请选择发货日期', trigger: 'change' }]
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, shipmentNo: undefined, orderNo: undefined, customerName: undefined, status: undefined, params: {} },
  rules: {
    orderId: [{ required: true, message: '请选择关联订单', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 合计：发货总数量 */
const totalShipQty = computed(() => {
  if (!form.value.detailList) return '0.00'
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.shipQty) || 0), 0).toFixed(2)
})

/** 合计：发货总金额 */
const totalShipAmount = computed(() => {
  if (!form.value.detailList) return '0.00'
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.amount) || 0), 0).toFixed(2)
})

function getList() {
  loading.value = true
  listShipment(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(i => i.shipmentId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    shipmentId: undefined,
    shipmentNo: undefined,
    orderId: undefined,
    orderNo: undefined,
    contractId: undefined,
    contractNo: undefined,
    customerId: undefined,
    customerName: undefined,
    status: '0',
    shipmentDate: undefined,
    logisticsCompany: undefined,
    trackingNo: undefined,
    totalQty: 0,
    totalAmount: 0,
    receiverName: undefined,
    receiverPhone: undefined,
    receiverAddress: undefined,
    orderTotalQty: 0,
    orderShippedQty: 0,
    orderUnshippedQty: 0,
    remark: undefined,
    detailList: []
  }
  proxy.resetForm('shipmentRef')
}

/** 选择订单后自动带出客户、合同和明细 */
function onOrderChange(val) {
  if (!val) {
    form.value.orderId = undefined
    form.value.orderNo = undefined
    form.value.customerName = undefined
    form.value.contractNo = undefined
    form.value.detailList = []
    form.value.orderTotalQty = 0
    form.value.orderShippedQty = 0
    form.value.orderUnshippedQty = 0
    return
  }
  getOrder(val).then(res => {
    const order = res.data
    form.value.orderId = order.orderId
    form.value.orderNo = order.orderNo
    form.value.customerId = order.customerId
    form.value.customerName = order.customerName
    form.value.contractId = order.contractId
    form.value.contractNo = order.contractNo
    // 计算订单总数量、已发数量、未发数量
    let orderTotal = 0, orderShipped = 0
    if (order.itemList) {
      order.itemList.forEach(item => {
        orderTotal += (item.quantity || 0)
        orderShipped += (item.shippedQty || 0)
      })
    }
    form.value.orderTotalQty = parseFloat(orderTotal.toFixed(2))
    form.value.orderShippedQty = parseFloat(orderShipped.toFixed(2))
    form.value.orderUnshippedQty = parseFloat((orderTotal - orderShipped).toFixed(2))
    // 从订单明细带出发货明细
    if (order.itemList && order.itemList.length > 0) {
      form.value.detailList = order.itemList.map(item => {
        const orderQty = parseFloat((item.quantity || 0).toFixed(2))
        const shippedQty = parseFloat((item.shippedQty || 0).toFixed(2))
        const unshippedQty = parseFloat((orderQty - shippedQty).toFixed(2))
        return {
          orderItemId: item.itemId,
          productName: item.productName,
          productSpec: item.productSpec,
          unit: item.unit,
          orderQty: orderQty,
          shippedQty: shippedQty,
          unshippedQty: unshippedQty,
          shipQty: unshippedQty > 0 ? unshippedQty : 0,
          unitPrice: item.unitPrice,
          amount: unshippedQty > 0 ? parseFloat((unshippedQty * (item.unitPrice || 0)).toFixed(2)) : 0
        }
      })
    } else {
      form.value.detailList = []
    }
  })
}

/** 加载订单选项（已确认8、已发货2的订单可发货） */
function loadOrderOptions() {
  Promise.all([
    listOrder({ pageNum: 1, pageSize: 999 }),
    getInProgressOrderIds().catch(() => ({ data: [] }))
  ]).then(([orderRes, inProgressRes]) => {
    // 仅显示已审核(2)、部分发货(3)状态的订单
    orderOptions.value = (orderRes.rows || []).filter(o =>
      o.orderStatus === '2' || o.orderStatus === '3'
    )
  })
}

/** 计算明细金额 */
function calcDetail(row) {
  if (row && row.shipQty != null && row.unitPrice != null) {
    row.amount = parseFloat((row.shipQty * row.unitPrice).toFixed(2))
  }
  // 计算主表合计
  let tq = 0, ta = 0
  form.value.detailList.forEach(d => {
    if (d.shipQty) tq += d.shipQty
    if (d.amount) ta += d.amount
  })
  form.value.totalQty = parseFloat(tq.toFixed(2))
  form.value.totalAmount = parseFloat(ta.toFixed(2))
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加发货单'
}

function handleUpdate(row) {
  reset()
  getShipment(row.shipmentId || ids.value[0]).then(res => {
    form.value = res.data
    if (!form.value.detailList) form.value.detailList = []
    // 加载订单信息以获取未发货数量
    if (form.value.orderId) {
      getOrder(form.value.orderId).then(orderRes => {
        const order = orderRes.data
        if (order && order.itemList && form.value.detailList) {
          let orderTotal = 0, orderShipped = 0
          order.itemList.forEach(item => {
            orderTotal += (item.quantity || 0)
            orderShipped += (item.shippedQty || 0)
          })
          form.value.orderTotalQty = parseFloat(orderTotal.toFixed(2))
          form.value.orderShippedQty = parseFloat(orderShipped.toFixed(2))
          form.value.orderUnshippedQty = parseFloat((orderTotal - orderShipped).toFixed(2))
          // 更新每行明细的未发货数量
          form.value.detailList.forEach(d => {
            if (d.orderItemId) {
              const oi = order.itemList.find(i => i.itemId === d.orderItemId)
              if (oi) {
                const orderQty = parseFloat((oi.quantity || 0).toFixed(2))
                const shippedQty = parseFloat((oi.shippedQty || 0).toFixed(2))
                // 修改时需要把当前发货单的发货数量加回未发货数量
                const currentShipQty = parseFloat((d.shipQty || 0).toFixed(2))
                d.orderQty = orderQty
                d.shippedQty = shippedQty
                d.unshippedQty = parseFloat((orderQty - shippedQty + currentShipQty).toFixed(2))
              }
            }
          })
        }
        open.value = true
        title.value = '修改发货单'
      })
    } else {
      open.value = true
      title.value = '修改发货单'
    }
  })
}

function handleView(row) {
  getShipment(row.shipmentId).then(res => {
    viewData.value = res.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['shipmentRef'].validate(valid => {
    if (valid) {
      // 校验：至少一行发货数量大于0
      if (form.value.detailList && form.value.detailList.length > 0) {
        let hasShip = false
        for (let i = 0; i < form.value.detailList.length; i++) {
          const d = form.value.detailList[i]
          if (d.shipQty != null && parseFloat(d.shipQty) > 0) {
            hasShip = true
            if (d.unshippedQty != null && parseFloat(d.shipQty) > parseFloat(d.unshippedQty)) {
              proxy.$modal.msgError(`第${i + 1}行商品的发货数量(${d.shipQty})不能大于未发货数量(${d.unshippedQty})`)
              return
            }
          }
        }
        if (!hasShip) {
          proxy.$modal.msgError('至少需要一行商品的发货数量大于0')
          return
        }
      } else {
        proxy.$modal.msgError('发货明细不能为空')
        return
      }
      if (form.value.shipmentId != undefined) {
        updateShipment(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addShipment(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const shipmentIds = row.shipmentId || ids.value
  proxy.$modal.confirm('确认删除编号为"' + shipmentIds + '"的发货单？').then(() => delShipment(shipmentIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 确认发货 */
function handleConfirm(row) {
  getShipment(row.shipmentId).then(res => {
    confirmForm.value = {
      shipmentId: res.data.shipmentId,
      shipmentNo: res.data.shipmentNo,
      orderNo: res.data.orderNo,
      customerName: res.data.customerName,
      totalQty: res.data.totalQty,
      logisticsCompany: undefined,
      trackingNo: undefined,
      shipmentDate: new Date().toISOString().slice(0, 10)
    }
    confirmOpen.value = true
  })
}

function submitConfirm() {
  proxy.$refs['confirmRef'].validate(valid => {
    if (valid) {
      proxy.$modal.confirm('确认发货？确认后将更新订单已发货数量。').then(() => {
        return confirmShipment(confirmForm.value)
      }).then(() => {
        proxy.$modal.msgSuccess('发货成功')
        confirmOpen.value = false
        getList()
      }).catch(() => {})
    }
  })
}

/** 签收 */
function handleReceive(row) {
  proxy.$modal.confirm('确认签收发货单「' + row.shipmentNo + '」？').then(() => {
    return receiveShipment(row.shipmentId)
  }).then(() => {
    proxy.$modal.msgSuccess('签收成功')
    getList()
  }).catch(() => {})
}

function handleExport() {
  proxy.download('mk/shipment/export', { ...queryParams.value }, `shipment_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

loadOrderOptions()
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
</style>
