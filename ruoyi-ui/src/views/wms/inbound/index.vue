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
          <label>目标仓库</label>
          <div class="control is-select">
            <el-select v-model="queryParams.warehouseId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleStatusChange">
              <el-option v-for="d in wms_inbound_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>入库日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:inbound:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:inbound:remove']">删除</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:inbound:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_inbound_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
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
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:inbound:edit']" v-if="scope.row.status === '0'">修改</el-button>
              <el-button link type="primary" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['wms:inbound:edit']" v-if="scope.row.status === '0'">提交</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:inbound:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ form.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="inboundRef" :model="form" :rules="rules" label-width="100px">
          <!-- 单据信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
                单据信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row :gutter="16">
                <el-col :span="8"><el-form-item label="入库单号" prop="orderNo"><el-input v-model="form.orderNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="入库类型" prop="orderType"><el-select v-model="form.orderType" style="width:100%"><el-option v-for="d in wms_inbound_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="状态"><dict-tag :options="wms_inbound_status" :value="form.status" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 入库信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('info')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>
                入库信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.info }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.info">
              <el-row :gutter="16">
                <el-col :span="8"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" filterable clearable placeholder="请选择供应商" style="width:100%"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="目标仓库" prop="warehouseId"><el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%"><el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="入库日期" prop="inboundDate"><el-date-picker v-model="form.inboundDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 入库明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('detail')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>
                入库明细
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.detail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.detail">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddDetail">添加明细</el-button></el-col>
              </el-row>
              <el-table :data="form.detailList" border @header-dragend="onHeaderDragEnd">
                <el-table-column label="物料" min-width="200"><template #default="scope"><el-select v-model="scope.row.materialId" filterable clearable placeholder="请选择物料" style="width:100%" @change="handleMaterialChange(scope.$index, scope.row)"><el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialCode + ' - ' + m.materialName" :value="m.materialId" /></el-select></template></el-table-column>
                <el-table-column label="计划数量" width="120"><template #default="scope"><el-input-number v-model="scope.row.planQty" :precision="2" :min="0" controls-position="right" style="width:100%" /></template></el-table-column>
                <el-table-column label="批次号" width="130"><template #default="scope"><el-input v-model="scope.row.batchNo" :placeholder="scope.row.isBatchManage === '1' ? '批次号（必填）' : '批次号'" /></template></el-table-column>
                <el-table-column label="生产日期" width="150"><template #default="scope"><el-date-picker v-model="scope.row.productionDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :placeholder="scope.row.isExpiryManage === '1' ? '生产日期（必填）' : '生产日期'" /></template></el-table-column>
                <el-table-column label="单位" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
                <el-table-column label="单价" width="120"><template #default="scope"><el-input-number v-model="scope.row.unitPrice" :precision="2" :min="0" controls-position="right" style="width:100%" /></template></el-table-column>
                <el-table-column label="操作" width="80" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" @click="handleDeleteDetail(scope.$index)">删除</el-button></template></el-table-column>
              </el-table>
              <el-form-item label="备注" prop="remark" style="margin-top: 15px"><el-input v-model="form.remark" type="textarea" /></el-form-item>
            </div>
          </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg>
          </div>
          <span class="rd-detail-header-title">入库单详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ detailData.orderNo }}</span>
            <dict-tag :options="wms_inbound_status" :value="detailData.status" />
          </div>
        </div>
      </template>
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dBasic')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
              单据信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">入库单号</span><div class="rd-value">{{ detailData.orderNo }}</div></div>
              <div class="rd-item"><span class="rd-label">入库类型</span><div class="rd-value"><dict-tag :options="wms_inbound_type" :value="detailData.orderType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_inbound_status" :value="detailData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">总数量</span><div class="rd-value">{{ detailData.totalQty }}</div></div>
              <div class="rd-item"><span class="rd-label">完成时间</span><div class="rd-value">{{ detailData.completeDate || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 入库信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dInfo')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>
              入库信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dInfo }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dInfo">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ detailData.supplierName }}</div></div>
              <div class="rd-item"><span class="rd-label">目标仓库</span><div class="rd-value">{{ detailData.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">入库日期</span><div class="rd-value">{{ detailData.inboundDate }}</div></div>
            </div>
          </div>
        </section>
        <!-- 入库明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dDetail')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>
              入库明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dDetail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dDetail">
            <el-table :data="detailData.detailList" border @header-dragend="onHeaderDragEnd">
              <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable align="center" />
              <el-table-column label="物料名称" prop="materialName" show-overflow-tooltip align="center" />
              <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="计划数量" prop="planQty" :width="colWidth('planQty', 100)" resizable align="center" />
              <el-table-column label="已收数量" prop="receivedQty" :width="colWidth('receivedQty', 100)" resizable align="center" />
              <el-table-column label="已上架数量" prop="putawayQty" :width="colWidth('putawayQty', 100)" resizable align="center" />
              <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable align="center" />
              <el-table-column label="上架库位" prop="locationName" :width="colWidth('locationName', 100)" resizable show-overflow-tooltip align="center" />
            </el-table>
          </div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dOther')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>
              其他信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dOther }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dOther">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !detailData.remark }">{{ detailData.remark || '暂无备注' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ detailData.createBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ detailData.createTime || '-' }}</div></div>
            </div>
          </div>
        </section>
    </el-dialog>
    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="入库单管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
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

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">新建入库单后的初始状态，可编辑、删除、提交。提交后单据进入「待收货」状态，无法再修改</el-descriptions-item>
          <el-descriptions-item label="待收货">已提交的入库单，仓库人员可在「入库作业」中进行收货操作</el-descriptions-item>
          <el-descriptions-item label="待上架">所有明细收货完成后进入此状态，仓库人员可在「入库作业」中进行上架操作，将货物放到指定库位</el-descriptions-item>
          <el-descriptions-item label="已完成">所有明细上架完成后自动完成，入库流程结束，库存已更新</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">明细物料自动带出</div>
              <div class="highlight-card-body">选择物料后自动带出<strong>单位、批次管理标识、效期管理标识</strong>，批次管理物料需填写批次号，效期管理物料需填写生产日期</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">提交后不可修改</div>
              <div class="highlight-card-body">入库单提交后进入「待收货」状态，<strong>无法再修改单据内容</strong>。如需调整，需删除后重新创建</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">收货与上架分离</div>
              <div class="highlight-card-body">收货仅记录到货数量，上架才将货物绑定到具体库位并更新库存。支持<strong>分批收货和分批上架</strong></div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">明细校验规则</div>
              <div class="highlight-card-body">至少添加一条明细；计划数量必须大于0；启用批次管理的物料<strong>批次号必填</strong>；启用效期管理的物料<strong>生产日期必填</strong></div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建入库单：</strong>选择入库类型、供应商、目标仓库、入库日期，添加入库明细（物料、计划数量、批次号、单价等）
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交入库单：</strong>确认无误后点击「提交」按钮，单据进入「待收货」状态，提交后无法修改
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>收货作业：</strong>在「入库作业」页面对待收货单据进行收货，记录实际到货数量
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>上架作业：</strong>收货完成后在「入库作业」页面进行上架，选择库位并将货物上架，系统自动更新库存
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsInbound">
import { listInbound, getInbound, addInbound, updateInbound, delInbound, submitInbound } from '@/api/wms/inbound'
import { listWarehouse } from '@/api/wms/warehouse'
import { listSupplier } from '@/api/wms/supplier'
import { listMaterial } from '@/api/wms/material'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { ArrowRight, ArrowDown, QuestionFilled, WarningFilled, Filter, Search } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'info', 'detail', 'dBasic', 'dInfo', 'dDetail', 'dOther'])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_inbound_index')
const { wms_inbound_type, wms_inbound_status, wms_unit } = proxy.useDict('wms_inbound_type', 'wms_inbound_status', 'wms_unit')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref(''); const detailOpen = ref(false); const detailData = ref({}); const showStatusHelp = ref(false); const activeStatusTab = ref('all'); const statusCounts = ref({ all: 0 }); const dateRange = ref([]); const dateRangeCreateTime = ref([])
const warehouseOptions = ref([]); const supplierOptions = ref([]); const materialOptions = ref([])
const defaultColumns = { orderNo: { label: '入库单号', visible: true }, orderType: { label: '入库类型', visible: true }, supplierName: { label: '供应商', visible: true }, warehouseName: { label: '目标仓库', visible: true }, status: { label: '状态', visible: true }, totalQty: { label: '总数量', visible: true }, inboundDate: { label: '入库日期', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('wms_inbound_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let count = 0; if (queryParams.value.orderNo) count++; if (queryParams.value.orderType) count++; if (queryParams.value.warehouseId) count++; if (queryParams.value.status) count++; if (dateRange.value && dateRange.value.length > 0) count++; if (dateRangeCreateTime.value && dateRangeCreateTime.value.length > 0) count++; return count })
const statusTabList = computed(() => wms_inbound_status.value)
function today() { const d = new Date(); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0') }
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, orderType: undefined, warehouseId: undefined, status: undefined }, rules: { orderType: [{ required: true, message: '入库类型不能为空', trigger: 'change' }], warehouseId: [{ required: true, message: '目标仓库不能为空', trigger: 'change' }], inboundDate: [{ required: true, message: '预计入库日期不能为空', trigger: 'change' }] } })
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; const params = proxy.addDateRange(proxy.addDateRange(queryParams.value, dateRange.value, 'InboundDate'), dateRangeCreateTime.value, 'CreateTime'); listInbound(params).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.orderNo = undefined; queryParams.value.orderType = undefined; queryParams.value.warehouseId = undefined; queryParams.value.status = undefined; queryParams.value.params = {}; dateRange.value = []; dateRangeCreateTime.value = []; activeStatusTab.value = 'all'; handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.orderId); multiple.value = !sel.length }
function reset() { form.value = { orderNo: undefined, orderType: '0', supplierId: undefined, warehouseId: undefined, inboundDate: today(), status: '0', remark: undefined, detailList: [] }; proxy.resetForm('inboundRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加入库单' }
function handleUpdate(row) { reset(); getInbound(row.orderId).then(res => { form.value = res.data; open.value = true; title.value = '修改入库单' }) }
function handleDetail(row) { getInbound(row.orderId).then(res => { detailData.value = res.data; detailOpen.value = true }) }
function handleAddDetail() { if (!form.value.detailList) form.value.detailList = []; form.value.detailList.push({ materialId: undefined, planQty: 0, batchNo: undefined, productionDate: undefined, unitPrice: 0, isBatchManage: '0', isExpiryManage: '0' }) }
function handleMaterialChange(index, row) { const material = materialOptions.value.find(m => m.materialId === row.materialId); if (material) { row.isBatchManage = material.isBatchManage; row.isExpiryManage = material.isExpiryManage; row.unit = material.unit } }
function handleDeleteDetail(index) { form.value.detailList.splice(index, 1) }
function submitForm() {
  if (!validateDetailList()) return
  proxy.$refs['inboundRef'].validate(v => {
    if (v) {
      if (form.value.orderId != null) {
        updateInbound(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addInbound(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function validateDetailList() {
  if (!form.value.detailList || form.value.detailList.length === 0) { proxy.$modal.msgError('请至少添加一条入库明细'); return false }
  for (let i = 0; i < form.value.detailList.length; i++) {
    const detail = form.value.detailList[i]
    if (!detail.materialId) { proxy.$modal.msgError(`第${i + 1}行明细：请选择物料`); return false }
    if (!detail.planQty || detail.planQty <= 0) { proxy.$modal.msgError(`第${i + 1}行明细：计划数量必须大于0`); return false }
    if (detail.isBatchManage === '1' && (!detail.batchNo || detail.batchNo.trim() === '')) { proxy.$modal.msgError(`第${i + 1}行明细：该物料启用批次管理，批次号必填`); return false }
    if (detail.isExpiryManage === '1' && !detail.productionDate) { proxy.$modal.msgError(`第${i + 1}行明细：该物料启用效期管理，生产日期必填`); return false }
  }
  return true
}
function handleDelete(row) { const orderIds = row.orderId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delInbound(orderIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交入库单「' + row.orderNo + '」？提交后将无法修改。').then(() => submitInbound(row.orderId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {}) }
function handleExport() { proxy.download('wms/inbound/export', { ...proxy.addDateRange(proxy.addDateRange(queryParams.value, dateRange.value, 'InboundDate'), dateRangeCreateTime.value, 'CreateTime') }, `inbound_${new Date().getTime()}.xlsx`) }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'violet', '3': 'green' }; return map[status] || 'gray' }
function statusLabel(status) { const item = wms_inbound_status.value.find(d => d.value == status); return item ? item.label : '-' }
function orderTypeLabel(type) { const item = wms_inbound_type.value.find(d => d.value == type); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-partial', '3': 'tab-done' }; return map[value] || '' }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function handleStatusChange(val) { activeStatusTab.value = val ? val : 'all'; handleQuery() }
function loadStatusCounts() { listInbound({ pageNum: 1, pageSize: 999 }).then(res => { const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 }; (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ }); statusCounts.value = counts }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function loadOptions() {
  listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
  listSupplier({ status: '0', pageSize: 999 }).then(res => { supplierOptions.value = res.rows })
  listMaterial({ status: '0', pageSize: 999 }).then(res => { materialOptions.value = res.rows })
}
loadOptions()
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
