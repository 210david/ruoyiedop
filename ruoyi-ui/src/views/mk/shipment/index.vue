<template>
  <div class="app-container mk-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>发货单号</label>
          <div class="control">
            <el-input v-model="queryParams.shipmentNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
        <div class="field">
          <label>客户名称</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_shipment_status" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-icon><QuestionFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:shipment:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:shipment:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:shipment:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:shipment:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_shipment_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="发货单号" prop="shipmentNo" key="shipmentNo" :width="colWidth('shipmentNo', 160)" resizable v-if="columns.shipmentNo.visible" />
          <el-table-column label="订单编号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 160)" resizable v-if="columns.orderNo.visible" />
          <el-table-column label="客户名称" prop="customerName" key="customerName" :width="colWidth('customerName', 200)" resizable show-overflow-tooltip v-if="columns.customerName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="发货日期" prop="shipmentDate" key="shipmentDate" :width="colWidth('shipmentDate', 120)" resizable align="center" v-if="columns.shipmentDate.visible" />
          <el-table-column label="发货数量" prop="totalQty" key="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" v-if="columns.totalQty.visible" />
          <el-table-column label="发货金额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 120)" resizable align="right" v-if="columns.totalAmount.visible"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template></el-table-column>
          <el-table-column label="物流公司" prop="logisticsCompany" key="logisticsCompany" :width="colWidth('logisticsCompany', 120)" resizable show-overflow-tooltip v-if="columns.logisticsCompany.visible" />
          <el-table-column label="物流单号" prop="trackingNo" key="trackingNo" :width="colWidth('trackingNo', 140)" resizable show-overflow-tooltip v-if="columns.trackingNo.visible" />
          <el-table-column label="出库单号" prop="outboundOrderNo" key="outboundOrderNo" :width="colWidth('outboundOrderNo', 140)" resizable show-overflow-tooltip v-if="columns.outboundOrderNo.visible" />
          <el-table-column label="发货人" prop="shipperName" key="shipperName" :width="colWidth('shipperName', 100)" resizable v-if="columns.shipperName.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="320" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:shipment:edit']" v-if="scope.row.status === '0'">修改</el-button>
              <el-button link type="success" icon="Promotion" @click="handleConfirm(scope.row)" v-hasPermi="['marketing:shipment:deliver']" v-if="scope.row.status === '0'">确认发货</el-button>
              <el-button link type="warning" icon="CircleCheck" @click="handleReceive(scope.row)" v-hasPermi="['marketing:shipment:receive']" v-if="scope.row.status === '1'">签收</el-button>
              <el-button link type="danger" icon="RefreshLeft" @click="handleReturn(scope.row)" v-hasPermi="['marketing:return:add']" v-if="scope.row.status === '1' || scope.row.status === '2'">退货</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:shipment:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

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
                <el-table-column label="单价" prop="unitPrice" width="100" align="right"><template #default="scope"><span>{{ scope.row.unitPrice != null ? Number(scope.row.unitPrice).toFixed(2) : '-' }}</span></template></el-table-column>
                <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
                <el-table-column label="操作" width="70" align="center" fixed="right">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" @click="handleDeleteDetail(scope.$index)" />
                  </template>
                </el-table-column>
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
              <div class="rd-item"><span class="rd-label">订单数量</span><div class="rd-value">{{ viewData.orderTotalQty != null ? viewData.orderTotalQty : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">已发数量</span><div class="rd-value">{{ viewData.orderShippedQty != null ? viewData.orderShippedQty : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">未发数量</span><div class="rd-value">{{ viewData.orderUnshippedQty != null ? viewData.orderUnshippedQty : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发货金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.totalAmount) }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>发货与物流信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发货日期</span><div class="rd-value">{{ viewData.shipmentDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发货人</span><div class="rd-value">{{ viewData.shipperName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">出库单号</span><div class="rd-value">{{ viewData.outboundOrderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物流公司</span><div class="rd-value">{{ viewData.logisticsCompany || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物流单号</span><div class="rd-value">{{ viewData.trackingNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>收货信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v4">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">收货人</span><div class="rd-value">{{ viewData.receiverName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">收货电话</span><div class="rd-value">{{ viewData.receiverPhone || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">收货地址</span><div class="rd-value">{{ viewData.receiverAddress || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v5')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v5">
            <div class="rd-grid">
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
              <el-table-column label="订单数量" prop="orderQty" width="100" align="right" />
              <el-table-column label="已发数量" prop="shippedQty" width="100" align="right" />
              <el-table-column label="未发数量" prop="unshippedQty" width="100" align="right" />
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
    <el-dialog v-model="confirmOpen" width="1000px" append-to-body draggable class="rd-dialog">
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
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>发货物料明细</div></div>
            <div class="rd-card-body">
              <el-table :data="confirmForm.detailList" border size="small" :header-cell-style="{ textAlign: 'center' }">
                <el-table-column label="序号" type="index" width="55" align="center" />
                <el-table-column label="商品名称" prop="productName" min-width="150" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="productSpec" min-width="120" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="80" align="center" />
                <el-table-column label="发货数量" prop="shipQty" width="100" align="right" />
                <el-table-column label="单价" prop="unitPrice" width="100" align="right" />
                <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              </el-table>
              <div class="detail-summary" v-if="confirmForm.detailList && confirmForm.detailList.length > 0">
                <span>合计：发货总数量 {{ confirmForm.totalQty || '0.00' }} | 发货总金额 {{ formatMoney(confirmForm.totalAmount) }}</span>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>出库与物流信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="出库单号" prop="outboundOrderNo"><el-input v-model="confirmForm.outboundOrderNo" placeholder="请输入仓库出库单号" style="width: 100%"><template #append><el-tooltip content="必须先在仓库管理完成出库，填写出库单号后才能发货" placement="top"><el-button icon="QuestionFilled" /></el-tooltip></template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发货日期" prop="shipmentDate"><el-date-picker v-model="confirmForm.shipmentDate" type="date" value-format="YYYY-MM-DD" placeholder="选择发货日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物流公司" prop="logisticsCompany"><el-input v-model="confirmForm.logisticsCompany" placeholder="请输入物流公司" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物流单号" prop="trackingNo"><el-input v-model="confirmForm.trackingNo" placeholder="请输入物流单号" style="width: 100%" /></el-form-item></el-col>
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

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="发货管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">待发货</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">已发货</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已签收</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待发货">
            发货单创建后的初始状态，可修改、删除。确认发货需填写出库单号、物流公司、物流单号和发货日期
          </el-descriptions-item>
          <el-descriptions-item label="已发货">
            已确认发货，系统自动更新订单明细的已发货数量。可进行签收操作；已发货或已签收状态可发起退货。删除已发货的发货单将自动回滚订单已发货数量和状态
          </el-descriptions-item>
          <el-descriptions-item label="已签收">
            客户已签收确认，发货流程完成。可从此状态发起退货
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、业务规则</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>支持多次发货：</strong>同一订单可创建多张发货单，分批次发货。每张发货单独立管理发货数量和物流信息
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>发货数量校验：</strong>发货明细由订单自动带出，每行发货数量必须大于0且不能超过该商品的未发货数量
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>出库前置要求：</strong>确认发货时必须填写出库单号，确保已在仓库管理模块完成出库操作后才能发货
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>自动更新订单状态：</strong>确认发货后系统自动更新订单明细的已发货数量。全部发货完成时订单状态变为已完成，部分发货时变为部分发货
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>发起退货：</strong>已发货或已签收的发货单可点击"退货"按钮直接创建退货单（草稿状态），退货明细从发货明细自动带出，退货数量不能超过发货数量
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>删除回滚：</strong>删除已发货的发货单将自动回滚关联订单的已发货数量，并重新计算订单状态（已审核/部分发货/已完成）
          </el-timeline-item>
        </el-timeline>

        <h4>四、操作权限</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="新增发货">选择已审核或部分发货状态的订单创建发货单</el-descriptions-item>
          <el-descriptions-item label="修改发货">仅待发货状态的发货单可修改</el-descriptions-item>
          <el-descriptions-item label="确认发货">填写出库单号、物流信息后确认发货，需出库权限</el-descriptions-item>
          <el-descriptions-item label="签收">仅已发货状态可签收</el-descriptions-item>
          <el-descriptions-item label="发起退货">已发货或已签收状态可发起退货</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 发起退货对话框 -->
    <el-dialog v-model="returnOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 14 31 14 26 9"/><path d="M19 16l4 4"/><path d="M5 8l-2 4 2 4"/><polyline points="15 10 12 7 9 10"/></svg></div><span class="rd-detail-header-title">发起退货</span><div class="rd-detail-header-sub" v-if="returnForm.shipmentNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">发货单号：{{ returnForm.shipmentNo }}</span></div></div></template>
      <el-form ref="returnRef" :model="returnForm" :rules="returnRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>发货信息</div></div>
            <div class="rd-card-body">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">发货单号</span><div class="rd-value">{{ returnForm.shipmentNo || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ returnForm.orderNo || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ returnForm.customerName || '-' }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>退货明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.r2 }" aria-label="折叠" @click.stop="toggleCard('r2')"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.r2">
              <el-table :data="returnForm.detailList" border size="small" :header-cell-style="{ textAlign: 'center' }">
                <el-table-column label="序号" type="index" width="55" align="center" />
                <el-table-column label="商品名称" prop="productName" min-width="180" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="productSpec" min-width="144" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="96" align="center" />
                <el-table-column label="订单数量" prop="orderQty" width="120" align="right" />
                <el-table-column label="发货数量" prop="shipQty" width="120" align="right" />
                <el-table-column label="退货数量" prop="returnQty" width="168">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.returnQty" :precision="2" :min="0" :max="scope.row.shipQty" :controls="false" size="small" style="width: 132px" placeholder="必填" @change="calcReturnAmount(scope.row)" />
                  </template>
                </el-table-column>
                <el-table-column label="单价" prop="unitPrice" width="120" align="right"><template #default="scope"><span>{{ scope.row.unitPrice != null ? Number(scope.row.unitPrice).toFixed(2) : '-' }}</span></template></el-table-column>
                <el-table-column label="退货金额" prop="returnAmount" width="144" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.returnAmount) }}</span></template></el-table-column>
                <el-table-column label="退货原因" prop="returnReason" min-width="180">
                  <template #default="scope">
                    <el-input v-model="scope.row.returnReason" size="small" placeholder="请输入退货原因" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="84" align="center" fixed="right">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" @click="handleDeleteReturnDetail(scope.$index)" />
                  </template>
                </el-table-column>
              </el-table>
              <div class="detail-summary" v-if="returnForm.detailList && returnForm.detailList.length > 0">
                <span>合计：退货总数量 {{ totalReturnQty }} | 退货总金额 {{ formatMoney(totalReturnAmount) }}</span>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/></svg></span>退货信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="退货金额"><el-input :model-value="formatMoney(totalReturnAmount)" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="退货日期"><el-date-picker v-model="returnForm.returnDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="退货原因" prop="returnReason"><el-input v-model="returnForm.returnReason" type="textarea" :rows="3" placeholder="请输入退货原因" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitReturn">确认退货</el-button>
        <el-button @click="returnOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkShipment">
import { listShipment, getShipment, addShipment, updateShipment, delShipment, confirmShipment, receiveShipment, getInProgressOrderIds } from '@/api/mk/shipment'
import { listOrder, getOrder } from '@/api/mk/order'
import { addReturn } from '@/api/mk/returnOrder'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { marketing_shipment_status } = proxy.useDict('marketing_shipment_status')

const { collapsedCards, toggleCard } = useDetailCard(["c1", "c2", "c3", "c0", "v1", "v2", "v3", "v4", "v5", "r2"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_shipment_index')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const confirmOpen = ref(false)
const confirmForm = ref({})
const returnOpen = ref(false)
const returnForm = ref({})
const showStatusHelp = ref(false)
const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0 })
const statusTabList = computed(() => marketing_shipment_status.value)
function loadStatusCounts() {
  const counts = { all: 0, '0': 0, '1': 0, '2': 0 }
  list.value.forEach(row => {
    const s = row.status
    if (counts[s] !== undefined) counts[s]++
  })
  counts.all = total.value
  statusCounts.value = counts
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_shipment_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done' }; return map[value] || '' }
const loading = ref(true)
const showSearch = ref(true)
const orderOptions = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const confirmRules = {
  outboundOrderNo: [{ required: true, message: '请填写出库单号', trigger: 'blur' }],
  logisticsCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  trackingNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
  shipmentDate: [{ required: true, message: '请选择发货日期', trigger: 'change' }]
}

const returnRules = {
  returnReason: [{ required: true, message: '请输入退货原因', trigger: 'blur' }]
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, shipmentNo: undefined, orderNo: undefined, customerName: undefined, status: undefined, params: {} },
  rules: {
    orderId: [{ required: true, message: '请选择关联订单', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  shipmentNo: { label: '发货单号', visible: true },
  orderNo: { label: '订单编号', visible: true },
  customerName: { label: '客户名称', visible: true },
  status: { label: '状态', visible: true },
  shipmentDate: { label: '发货日期', visible: true },
  totalQty: { label: '发货数量', visible: true },
  totalAmount: { label: '发货金额', visible: true },
  logisticsCompany: { label: '物流公司', visible: true },
  trackingNo: { label: '物流单号', visible: true },
  outboundOrderNo: { label: '出库单号', visible: true },
  shipperName: { label: '发货人', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_shipment_columns')
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

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.shipmentNo) count++
  if (queryParams.value.orderNo) count++
  if (queryParams.value.customerName) count++
  if (queryParams.value.status) count++
  return count
})

function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }

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
    loadStatusCounts()
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.shipmentNo = undefined
  queryParams.value.orderNo = undefined
  queryParams.value.customerName = undefined
  queryParams.value.status = undefined
  queryParams.value.params = {}
  activeStatusTab.value = 'all'
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

/** 删除明细行 */
function handleDeleteDetail(index) {
  form.value.detailList.splice(index, 1)
  calcDetail()
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
    if (!viewData.value.detailList) viewData.value.detailList = []
    // 加载订单信息以获取订单数量、已发数量、未发数量
    if (viewData.value.orderId) {
      getOrder(viewData.value.orderId).then(orderRes => {
        const order = orderRes.data
        if (order && order.itemList) {
          let orderTotal = 0, orderShipped = 0
          order.itemList.forEach(item => {
            orderTotal += (item.quantity || 0)
            orderShipped += (item.shippedQty || 0)
          })
          viewData.value.orderTotalQty = parseFloat(orderTotal.toFixed(2))
          viewData.value.orderShippedQty = parseFloat(orderShipped.toFixed(2))
          viewData.value.orderUnshippedQty = parseFloat((orderTotal - orderShipped).toFixed(2))
          // 为每行明细补充订单数量、已发数量、未发数量
          viewData.value.detailList.forEach(d => {
            if (d.orderItemId) {
              const oi = order.itemList.find(i => i.itemId === d.orderItemId)
              if (oi) {
                d.orderQty = parseFloat((oi.quantity || 0).toFixed(2))
                d.shippedQty = parseFloat((oi.shippedQty || 0).toFixed(2))
                d.unshippedQty = parseFloat((d.orderQty - d.shippedQty).toFixed(2))
              }
            }
          })
        }
        viewOpen.value = true
      })
    } else {
      viewOpen.value = true
    }
  })
}

function submitForm() {
  proxy.$refs['shipmentRef'].validate(valid => {
    if (valid) {
      // 校验：发货明细不能为空
      if (!form.value.detailList || form.value.detailList.length === 0) {
        proxy.$modal.msgError('发货明细不能为空')
        return
      }
      // 校验：每行发货数量必须大于0，且不能超过未发货数量
      for (let i = 0; i < form.value.detailList.length; i++) {
        const d = form.value.detailList[i]
        if (d.shipQty == null || parseFloat(d.shipQty) <= 0) {
          proxy.$modal.msgError(`第${i + 1}行商品「${d.productName || ''}」的发货数量必须大于0，如不发货请删除该行`)
          return
        }
        if (d.unshippedQty != null && parseFloat(d.shipQty) > parseFloat(d.unshippedQty)) {
          proxy.$modal.msgError(`第${i + 1}行商品「${d.productName || ''}」的发货数量(${d.shipQty})不能大于未发货数量(${d.unshippedQty})`)
          return
        }
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
  const msg = row.status && row.status !== '0' && row.status !== '3'
    ? '确认删除编号为"' + shipmentIds + '"的发货单？\n\n注意：删除已发货的发货单将自动回滚关联订单的已发货数量和状态。'
    : '确认删除编号为"' + shipmentIds + '"的发货单？'
  proxy.$modal.confirm(msg).then(() => delShipment(shipmentIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 确认发货 */
function handleConfirm(row) {
  getShipment(row.shipmentId).then(res => {
    const data = res.data
    confirmForm.value = {
      shipmentId: data.shipmentId,
      shipmentNo: data.shipmentNo,
      orderNo: data.orderNo,
      customerName: data.customerName,
      totalQty: data.totalQty,
      totalAmount: data.totalAmount,
      detailList: data.detailList || [],
      outboundOrderNo: undefined,
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
      proxy.$modal.confirm('确认发货？确认后将更新订单已发货数量。\n\n请确保已填写有效的出库单号，且仓库已完成出库。').then(() => {
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

/** 合计：退货总数量 */
const totalReturnQty = computed(() => {
  if (!returnForm.value.detailList) return '0.00'
  return returnForm.value.detailList.reduce((sum, item) => sum + (Number(item.returnQty) || 0), 0).toFixed(2)
})

/** 合计：退货总金额 */
const totalReturnAmount = computed(() => {
  if (!returnForm.value.detailList) return 0
  return returnForm.value.detailList.reduce((sum, item) => sum + (Number(item.returnAmount) || 0), 0)
})

/** 计算单行退货金额 */
function calcReturnAmount(row) {
  if (row && row.returnQty != null && row.unitPrice != null) {
    row.returnAmount = parseFloat((row.returnQty * row.unitPrice).toFixed(2))
  } else if (row) {
    row.returnAmount = 0
  }
}

/** 删除退货明细行 */
function handleDeleteReturnDetail(index) {
  returnForm.value.detailList.splice(index, 1)
}

/** 发起退货 — 从发货单直接创建退货单 */
function handleReturn(row) {
  getShipment(row.shipmentId).then(res => {
    const data = res.data
    returnForm.value = {
      shipmentId: data.shipmentId,
      shipmentNo: data.shipmentNo,
      orderId: data.orderId,
      orderNo: data.orderNo,
      customerId: data.customerId,
      customerName: data.customerName,
      totalQty: data.totalQty,
      totalAmount: data.totalAmount,
      detailList: (data.detailList || []).map(item => ({
        shipmentItemId: item.shipmentItemId,
        orderItemId: item.orderItemId,
        productName: item.productName,
        productSpec: item.productSpec,
        unit: item.unit,
        orderQty: item.orderQty,
        shipQty: item.shipQty,
        returnQty: undefined,
        unitPrice: item.unitPrice,
        returnAmount: 0,
        returnReason: undefined
      })),
      returnReason: undefined,
      returnDate: new Date().toISOString().slice(0, 10)
    }
    // 加载订单信息以获取订单数量
    if (data.orderId) {
      getOrder(data.orderId).then(orderRes => {
        const order = orderRes.data
        if (order && order.itemList && returnForm.value.detailList) {
          returnForm.value.detailList.forEach(d => {
            if (d.orderItemId) {
              const oi = order.itemList.find(i => i.itemId === d.orderItemId)
              if (oi) {
                d.orderQty = parseFloat((oi.quantity || 0).toFixed(2))
              }
            }
          })
        }
        returnOpen.value = true
      })
    } else {
      returnOpen.value = true
    }
  })
}

/** 提交退货 */
function submitReturn() {
  proxy.$refs['returnRef'].validate(valid => {
    if (valid) {
      // 校验：退货明细不能为空
      if (!returnForm.value.detailList || returnForm.value.detailList.length === 0) {
        proxy.$modal.msgError('退货明细不能为空')
        return
      }
      // 校验：每行退货数量必须大于0，且不能超过发货数量
      for (let i = 0; i < returnForm.value.detailList.length; i++) {
        const d = returnForm.value.detailList[i]
        if (d.returnQty == null || parseFloat(d.returnQty) <= 0) {
          proxy.$modal.msgError(`第${i + 1}行商品「${d.productName || ''}」的退货数量必须大于0，如不退货请删除该行`)
          return
        }
        if (d.shipQty != null && parseFloat(d.returnQty) > parseFloat(d.shipQty)) {
          proxy.$modal.msgError(`第${i + 1}行商品「${d.productName || ''}」的退货数量(${d.returnQty})不能大于发货数量(${d.shipQty})`)
          return
        }
      }
      proxy.$modal.confirm('确认发起退货？退货单创建后将保存为草稿状态，可在退货管理页面提交审批。').then(() => {
        return addReturn({
          orderId: returnForm.value.orderId,
          orderNo: returnForm.value.orderNo,
          customerId: returnForm.value.customerId,
          customerName: returnForm.value.customerName,
          returnAmount: parseFloat(totalReturnAmount.value.toFixed(2)),
          returnReason: returnForm.value.returnReason,
          returnStatus: '4',
          detailList: returnForm.value.detailList.map(d => ({
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
        })
      }).then(() => {
        proxy.$modal.msgSuccess('退货单已创建（草稿状态），可前往退货管理提交审批')
        returnOpen.value = false
        getList()
      }).catch(() => {})
    }
  })
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
