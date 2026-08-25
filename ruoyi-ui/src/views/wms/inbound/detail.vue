<template>
  <div class="app-container wms-list-page">
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
          <label>入库单号</label>
          <div class="control">
            <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>入库类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.orderType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_inbound_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon><span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left"></div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_inbound_detail_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" highlight-current-row @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="入库单号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 180)" resizable v-if="columns.orderNo.visible" />
          <el-table-column label="入库类型" prop="orderType" key="orderType" :width="colWidth('orderType', 100)" resizable align="center" v-if="columns.orderType.visible">
            <template #default="scope"><span class="badge violet">{{ orderTypeLabel(scope.row.orderType) }}</span></template>
          </el-table-column>
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 240)" resizable :show-overflow-tooltip="true" v-if="columns.supplierName.visible" />
          <el-table-column label="目标仓库" prop="warehouseName" key="warehouseName" :width="colWidth('warehouseName', 120)" resizable v-if="columns.warehouseName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="总数量" prop="totalQty" key="totalQty" :width="colWidth('totalQty', 110)" resizable align="center" class-name="col-num" v-if="columns.totalQty.visible" />
          <el-table-column label="入库日期" prop="inboundDate" key="inboundDate" :width="colWidth('inboundDate', 130)" resizable align="center" v-if="columns.inboundDate.visible" />
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
              <el-button plain type="success" icon="Download" size="small" @click="handleDetail(scope.row)" v-if="scope.row.status === '1'">收货</el-button>
              <el-button plain type="warning" icon="Upload" size="small" @click="handleDetail(scope.row)" v-if="scope.row.status === '2'">上架</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 入库作业详情面板 -->
    <el-dialog v-model="detailOpen" width="1200px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg></div>
          <span class="rd-detail-header-title">入库作业</span>
          <div class="rd-detail-header-sub" v-if="currentOrder.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ currentOrder.orderNo }}</span>
          </div>
        </div>
      </template>
      <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c6')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c6 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c6">
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">入库单号</span><div class="rd-value">{{ currentOrder.orderNo }}</div></div>
        <div class="rd-item"><span class="rd-label">入库类型</span><div class="rd-value"><dict-tag :options="wms_inbound_type" :value="currentOrder.orderType" /></div></div>
        <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_inbound_status" :value="currentOrder.status" /></div></div>
        <div class="rd-item"><span class="rd-label">总数量</span><div class="rd-value">{{ currentOrder.totalQty }}</div></div>
      </div>
                </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c5')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>入库信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c5">
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ currentOrder.supplierName }}</div></div>
        <div class="rd-item"><span class="rd-label">目标仓库</span><div class="rd-value">{{ currentOrder.warehouseName }}</div></div>
      </div>
                </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>入库明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c4">

      <el-table :data="currentOrder.detailList" border style="margin-top: 15px" @header-dragend="onHeaderDragEnd">
        <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
        <el-table-column label="物料名称" prop="materialName" show-overflow-tooltip />
        <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
        <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable />
        <el-table-column label="计划数量" prop="planQty" :width="colWidth('planQty', 100)" resizable align="center" />
        <el-table-column label="已收数量" prop="receivedQty" :width="colWidth('receivedQty', 100)" resizable align="center" />
        <el-table-column label="已上架数量" prop="putawayQty" :width="colWidth('putawayQty', 100)" resizable align="center" />
        <el-table-column label="上架库位" prop="locationName" :width="colWidth('locationName', 100)" resizable show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button plain type="success" icon="Download" size="small" @click="openReceive(scope.row)" v-if="currentOrder.status === '1' && scope.row.planQty > scope.row.receivedQty">收货</el-button>
            <el-button plain type="warning" icon="Upload" size="small" @click="openPutaway(scope.row)" v-if="currentOrder.status === '2' && getRemainingPutaway(scope.row) > 0">上架</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </section>
    </el-dialog>

    <!-- 收货对话框 -->
    <el-dialog v-model="receiveOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">收货确认</span>
          <div class="rd-detail-header-sub" v-if="currentOrder.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ currentOrder.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="receiveRef" :model="receiveForm" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ receiveForm.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="receiveForm.unit" /></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">物料名称</span><div class="rd-value">{{ receiveForm.materialName }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ receiveForm.batchNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>收货信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
            <div class="rd-grid" style="margin-bottom: 16px">
              <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ receiveForm.planQty }}</div></div>
              <div class="rd-item"><span class="rd-label">已收数量</span><div class="rd-value">{{ receiveForm.receivedQty }}</div></div>
              <div class="rd-item"><span class="rd-label">待收数量</span><div class="rd-value" style="color: #f56c6c; font-weight: 700">{{ (receiveForm.planQty - receiveForm.receivedQty).toFixed(2) }}</div></div>
            </div>
            <el-form-item label="本次收货" prop="qty">
              <el-input-number v-model="receiveForm.qty" :precision="2" :min="0" :max="receiveForm.planQty - receiveForm.receivedQty" style="width: 100%" />
              <div style="margin-top: 6px; color: #909399; font-size: 12px">最大可收数量：{{ (receiveForm.planQty - receiveForm.receivedQty).toFixed(2) }}</div>
            </el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer>
        <el-button type="success" @click="submitReceive">确认收货</el-button>
        <el-button @click="receiveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 上架对话框 -->
    <el-dialog v-model="putawayOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">上架确认</span>
          <div class="rd-detail-header-sub" v-if="currentOrder.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ currentOrder.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="putawayRef" :model="putawayForm" :rules="putawayRules" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ putawayForm.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="putawayForm.unit" /></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">物料名称</span><div class="rd-value">{{ putawayForm.materialName }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ putawayForm.batchNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></span>上架信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
            <div class="rd-grid" style="margin-bottom: 16px">
              <div class="rd-item"><span class="rd-label">已收数量</span><div class="rd-value">{{ putawayForm.receivedQty }}</div></div>
              <div class="rd-item"><span class="rd-label">已上架数量</span><div class="rd-value">{{ putawayForm.putawayQty }}</div></div>
              <div class="rd-item"><span class="rd-label">待上架数量</span><div class="rd-value" style="color: #f56c6c; font-weight: 700">{{ putawayForm.maxQty != null ? putawayForm.maxQty.toFixed(2) : '0.00' }}</div></div>
            </div>
            <el-form-item label="上架库位" prop="locationId">
              <el-select v-model="putawayForm.locationId" filterable placeholder="请选择库位" style="width: 100%">
                <el-option v-for="loc in locationOptions" :key="loc.warehouseId" :label="loc.warehouseCode + ' - ' + (loc.warehouseName || '')" :value="loc.warehouseId" />
              </el-select>
            </el-form-item>
            <el-form-item label="上架数量" prop="qty">
              <el-input-number v-model="putawayForm.qty" :precision="2" :min="0" :max="putawayForm.maxQty" style="width: 100%" />
              <div style="margin-top: 6px; color: #909399; font-size: 12px">最大可上数量：{{ putawayForm.maxQty != null ? putawayForm.maxQty.toFixed(2) : '0.00' }}</div>
            </el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer>
        <el-button type="warning" @click="submitPutaway">确认上架</el-button>
        <el-button @click="putawayOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="入库作业业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、作业流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">待收货</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待上架</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>

        <h4>二、页签说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待收货">入库单已提交，等待仓库人员进行收货确认。逐条明细进行收货，记录实际到货数量</el-descriptions-item>
          <el-descriptions-item label="待上架">所有明细收货完成后的状态，等待仓库人员进行上架操作，将货物绑定到具体库位</el-descriptions-item>
          <el-descriptions-item label="已完成">所有明细上架完成后的状态，入库流程结束，系统库存已自动更新</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">分批收货</div>
              <div class="highlight-card-body">每条明细可多次收货，本次收货数量不超过<strong>待收数量（计划 - 已收）</strong>。全部明细收货完成后，入库单自动流转到「待上架」</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">分批上架</div>
              <div class="highlight-card-body">每条明细可多次上架，本次上架数量不超过<strong>待上架数量（已收 - 已上架）</strong>。需选择目标库位，全部上架完成后入库单自动完成</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">库存自动更新</div>
              <div class="highlight-card-body">上架成功后系统自动增加对应库位的库存数量，无需手动维护库存</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">收货与上架区别</div>
              <div class="highlight-card-body">收货仅记录到货数量，不更新库存；<strong>上架才会绑定库位并更新库存</strong>。收货和上架是两个独立环节</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>收货操作：</strong>在「待收货」页签中选择入库单，点击「收货」按钮，输入本次收货数量并确认
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>上架操作：</strong>在「待上架」页签中选择入库单，点击「上架」按钮，选择目标库位并输入上架数量
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>自动完成：</strong>所有明细上架完成后，入库单自动流转到「已完成」，系统库存自动更新
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsInboundDetail">
import { listInbound, getInbound, receiveInbound, putawayInbound } from '@/api/wms/inbound'
import { listLocation } from '@/api/wms/warehouse'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { ArrowRight, ArrowDown, QuestionFilled, WarningFilled, Filter, Search } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(["c6","c5","c4","c3","c2","c1","c0"])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_inbound_detail')
const { wms_inbound_type, wms_inbound_status, wms_unit } = proxy.useDict('wms_inbound_type', 'wms_inbound_status', 'wms_unit')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const activeStatusTab = ref('1')
const statusCounts = ref({ all: 0, '1': 0, '2': 0, '3': 0 })
const statusTabList = ref([{ value: '1', label: '待收货' }, { value: '2', label: '待上架' }, { value: '3', label: '已完成' }])
const detailOpen = ref(false)
const currentOrder = ref({})
const receiveOpen = ref(false)
const putawayOpen = ref(false)
const receiveForm = ref({})
const putawayForm = ref({})
const locationOptions = ref([])
const showStatusHelp = ref(false)
const defaultColumns = { orderNo: { label: '入库单号', visible: true }, orderType: { label: '入库类型', visible: true }, supplierName: { label: '供应商', visible: true }, warehouseName: { label: '目标仓库', visible: true }, status: { label: '状态', visible: true }, totalQty: { label: '总数量', visible: true }, inboundDate: { label: '入库日期', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('wms_inbound_detail_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let count = 0; if (queryParams.value.orderNo) count++; if (queryParams.value.orderType) count++; if (queryParams.value.supplierName) count++; return count })
const putawayRules = {
  locationId: [{ required: true, message: '请选择库位', trigger: 'change' }],
  qty: [{ required: true, message: '请输入上架数量', trigger: 'blur' }]
}

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, orderType: undefined, supplierName: undefined, status: '1' }
})
const { queryParams } = toRefs(data)

/** 查询当前标签页的入库单列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (activeStatusTab.value === 'all') {
    // 「全部」标签排除草稿状态，草稿单据不能进行入库作业
    params.status = undefined
    params.params = { excludeStatus: '0' }
  } else {
    params.status = activeStatusTab.value
  }
  listInbound(params).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}

/** 加载各标签页的记录数（排除草稿） */
function loadStatusCounts() {
  listInbound({ pageNum: 1, pageSize: 999, params: { excludeStatus: '0' } }).then(res => {
    const counts = { all: res.total, '1': 0, '2': 0, '3': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

/** 切换标签页 */
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.pageNum = 1; getList() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'violet', '3': 'green' }; return map[status] || 'gray' }
function statusLabel(status) { const item = wms_inbound_status.value.find(d => d.value == status); return item ? item.label : '-' }
function orderTypeLabel(type) { const item = wms_inbound_type.value.find(d => d.value == type); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '1': 'tab-audit', '2': 'tab-partial', '3': 'tab-done' }; return map[value] || '' }

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.orderNo = undefined; queryParams.value.orderType = undefined; queryParams.value.supplierName = undefined; handleQuery() }

/** 打开入库单详情（列表操作按钮 + 行点击都走这里） */
function handleDetail(row) {
  getInbound(row.orderId).then(res => {
    currentOrder.value = res.data
    detailOpen.value = true
  })
}
function openReceive(row) {
  receiveForm.value = {
    orderId: currentOrder.value.orderId,
    detailId: row.detailId,
    materialCode: row.materialCode,
    materialName: row.materialName,
    unit: row.unit,
    batchNo: row.batchNo,
    planQty: row.planQty,
    receivedQty: row.receivedQty,
    qty: row.planQty - row.receivedQty
  }
  receiveOpen.value = true
}
function submitReceive() {
  receiveInbound(receiveForm.value.orderId, receiveForm.value.detailId, receiveForm.value.qty).then(() => {
    proxy.$modal.msgSuccess('收货成功')
    receiveOpen.value = false
    getInbound(currentOrder.value.orderId).then(res => { currentOrder.value = res.data })
    getList()
    loadStatusCounts()
  })
}
function getRemainingPutaway(row) {
  const details = currentOrder.value.detailList || []
  let totalPutaway = 0
  for (const d of details) {
    if (d.materialId === row.materialId && (d.batchNo || '') === (row.batchNo || '')) {
      totalPutaway += d.putawayQty || 0
    }
  }
  return (row.receivedQty || 0) - totalPutaway
}
function openPutaway(row) {
  const remaining = getRemainingPutaway(row)
  putawayForm.value = {
    orderId: currentOrder.value.orderId,
    detailId: row.detailId,
    materialCode: row.materialCode,
    materialName: row.materialName,
    unit: row.unit,
    batchNo: row.batchNo,
    receivedQty: row.receivedQty,
    putawayQty: row.putawayQty,
    locationId: row.locationId,
    qty: remaining,
    maxQty: remaining
  }
  // 加载该仓库的库位列表
  listLocation({ warehouseId: currentOrder.value.warehouseId, status: '0', pageSize: 999 }).then(res => {
    locationOptions.value = res.rows
  })
  putawayOpen.value = true
}
function submitPutaway() {
  proxy.$refs['putawayRef'].validate(valid => {
    if (valid) {
      putawayInbound(putawayForm.value.orderId, putawayForm.value.detailId, putawayForm.value.locationId, putawayForm.value.qty).then(() => {
        proxy.$modal.msgSuccess('上架成功')
        putawayOpen.value = false
        getInbound(currentOrder.value.orderId).then(res => { currentOrder.value = res.data })
        getList()
        loadStatusCounts()
      })
    }
  })
}

// 初始化
getList()
loadStatusCounts()

onActivated(() => {
  getList()
  loadStatusCounts()
})
</script>

<style scoped>
/* 页面特定样式 - 列表页面共享样式见 wms-list-page.scss */
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.flow-item { display: flex; align-items: center; gap: 8px; }
.flow-arrow { color: #909399; font-size: 16px; }
.highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.highlight-success { background-color: #f0f9ff; border-color: #b3e19d; }
.highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.highlight-success .highlight-card-title { color: #67c23a; }
.highlight-danger .highlight-card-title { color: #f56c6c; }
.highlight-primary .highlight-card-title { color: #409eff; }
.highlight-warning .highlight-card-title { color: #e6a23c; }
.highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
</style>
