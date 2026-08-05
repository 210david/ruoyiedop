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
          <label>移库单号</label>
          <div class="control">
            <el-input v-model="queryParams.moveNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleStatusChange">
              <el-option v-for="d in wms_move_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:move:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:move:remove']">删除</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:move:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_move_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="移库单号" prop="moveNo" key="moveNo" :width="colWidth('moveNo', 160)" resizable v-if="columns.moveNo.visible" />
          <el-table-column label="仓库" prop="warehouseName" key="warehouseName" :width="colWidth('warehouseName', 120)" resizable v-if="columns.warehouseName.visible" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 120)" resizable v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 200)" resizable :show-overflow-tooltip="true" v-if="columns.materialName.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 70)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
          <el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 100)" resizable v-if="columns.batchNo.visible" />
          <el-table-column label="源库位" prop="fromLocationName" key="fromLocationName" :width="colWidth('fromLocationName', 180)" resizable :show-overflow-tooltip="true" v-if="columns.fromLocationName.visible" />
          <el-table-column label="目标库位" prop="toLocationName" key="toLocationName" :width="colWidth('toLocationName', 180)" resizable :show-overflow-tooltip="true" v-if="columns.toLocationName.visible" />
          <el-table-column label="移库数量" prop="moveQty" key="moveQty" :width="colWidth('moveQty', 100)" resizable align="right" class-name="col-num" v-if="columns.moveQty.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="300" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['wms:move:query']">详情</el-button>
              <el-button link type="primary" icon="Check" @click="handleApprove(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['wms:move:edit']">审批</el-button>
              <el-button link type="primary" icon="Sort" @click="handleExecute(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['wms:move:edit']">执行</el-button>
              <el-button link type="warning" icon="CircleClose" @click="handleVoid(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1'" v-hasPermi="['wms:move:edit']">作废</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '3' || scope.row.status === '4'" v-hasPermi="['wms:move:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 9 2 12 5 15"/><polyline points="9 5 12 2 15 5"/><polyline points="15 19 12 22 9 19"/><polyline points="19 9 22 12 19 15"/><line x1="2" y1="12" x2="22" y2="12"/><line x1="12" y1="2" x2="12" y2="22"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="moveRef" :model="form" :rules="rules" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>仓库与物料</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%" @change="onWarehouseChange">
            <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存物料" prop="inventoryId">
          <el-select v-model="form.inventoryId" filterable clearable placeholder="请先选择仓库，再选择库存物料" style="width:100%" :disabled="!form.warehouseId" @change="onInventoryChange">
            <el-option v-for="inv in inventoryOptions" :key="inv.inventoryId" :label="inv.materialCode + ' - ' + inv.materialName + '（批次: ' + (inv.batchNo || '无') + '，库位: ' + (inv.locationName || '') + '，可用: ' + inv.qty + '）'" :value="inv.inventoryId" />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchNo" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单位"><dict-tag :options="wms_unit" :value="form.unit" /></el-form-item></el-col>
        </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('move')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 9 2 12 5 15"/><polyline points="9 5 12 2 15 5"/><polyline points="15 19 12 22 9 19"/><polyline points="19 9 22 12 19 15"/><line x1="2" y1="12" x2="22" y2="12"/><line x1="12" y1="2" x2="12" y2="22"/></svg></span>移库信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.move }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.move">
        <el-form-item label="源库位">
          <el-input :model-value="form.fromLocationName || ''" disabled />
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="可用数量"><el-input v-model="form.availableQty" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="移库数量" prop="moveQty"><el-input-number v-model="form.moveQty" :precision="2" :min="0" :max="form.availableQty || 0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="目标库位" prop="toLocationId">
          <el-select v-model="form.toLocationId" filterable clearable placeholder="请选择目标库位" style="width:100%" @change="onTargetLocationChange">
            <el-option v-for="l in targetLocationOptions" :key="l.warehouseId" :label="(l.warehouseName || '')" :value="l.warehouseId" />
          </el-select>
        </el-form-item>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.other">
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg>
          </div>
          <span class="rd-detail-header-title">移库单详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.moveNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ detailData.moveNo }}</span>
            <dict-tag :options="wms_move_status" :value="detailData.status" />
          </div>
        </div>
      </template>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">移库单号</span><div class="rd-value">{{ detailData.moveNo }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_move_status" :value="detailData.status" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dMaterial')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></span>物料信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dMaterial }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dMaterial">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ detailData.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ detailData.batchNo || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ detailData.materialCode }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ detailData.materialName }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="detailData.unit" /></div></div>
              <div class="rd-item"><span class="rd-label">移库数量</span><div class="rd-value">{{ detailData.moveQty }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dLocation')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg></span>库位信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dLocation }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dLocation">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">源库位</span><div class="rd-value">{{ detailData.fromLocationName || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">目标库位</span><div class="rd-value">{{ detailData.toLocationName || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dApprove')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <path d="M9 15l2 2 4-4"/>
                </svg>
              </span>
              审批信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dApprove }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dApprove">
            <div class="rd-timeline" v-if="detailData.approveBy">
              <div class="rd-timeline-item">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': detailData.status === '1' || detailData.status === '2', 'rd-timeline-dot--error': detailData.status === '4' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="detailData.status === '1' || detailData.status === '2'" type="success" size="small" effect="light" round>审批通过</el-tag>
                      <el-tag v-else-if="detailData.status === '4'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ detailData.approveTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ detailData.approveBy }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="detailData.approveOpinion">
                    <strong>审批意见：</strong>{{ detailData.approveOpinion }}
                  </div>
                </div>
              </div>
            </div>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
              <p class="rd-empty-text">暂无审批记录</p>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dOther')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dOther }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dOther">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !detailData.remark }">{{ detailData.remark || '暂无备注' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ detailData.createBy }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ detailData.createTime }}</div></div>
              <div class="rd-item" v-if="detailData.completeTime"><span class="rd-label">完成时间</span><div class="rd-value">{{ detailData.completeTime }}</div></div>
            </div>
          </div>
        </section>
      <template #footer><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 移库审批对话框 -->
    <el-dialog v-model="approveOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <path d="M9 15l2 2 4-4"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">移库审批</span>
          <div class="rd-detail-header-sub" v-if="approveForm.moveNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ approveForm.moveNo }}</span>
            <dict-tag :options="wms_move_status" :value="approveForm.status" />
          </div>
        </div>
      </template>
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aBasic')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </span>
              单据信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">移库单号</span><div class="rd-value">{{ approveForm.moveNo }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_move_status" :value="approveForm.status" /></div></div>
              <div class="rd-item"><span class="rd-label">移库数量</span><div class="rd-value rd-value--large rd-amount">{{ approveForm.moveQty }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ approveForm.createBy }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ approveForm.createTime }}</div></div>
            </div>
          </div>
        </section>

        <!-- 物料信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aMaterial')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                </svg>
              </span>
              物料信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aMaterial }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aMaterial">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ approveForm.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ approveForm.batchNo || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ approveForm.materialCode }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ approveForm.materialName }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="approveForm.unit" /></div></div>
            </div>
          </div>
        </section>

        <!-- 库位信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aLocation')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
              </span>
              库位信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aLocation }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aLocation">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">源库位</span><div class="rd-value">{{ approveForm.fromLocationName || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">目标库位</span><div class="rd-value">{{ approveForm.toLocationName || '暂无' }}</div></div>
            </div>
          </div>
        </section>

        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aOpinion')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <path d="M9 15l2 2 4-4"/>
                </svg>
              </span>
              审批意见
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aOpinion }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aOpinion">
            <el-form label-width="100px">
              <el-form-item label="审批意见" required>
                <el-input v-model="approveForm.approveOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      <template #footer>
        <el-button type="success" @click="confirmApprove(true)">通过</el-button>
        <el-button type="danger" @click="confirmApprove(false)">驳回</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="移库管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已批准</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="danger">已驳回</el-tag>
            <el-tag size="small" type="info">退回重新提交</el-tag>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
            <el-tag size="small" type="info">待审批/已批准可作废</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待审批">新建移库单后的初始状态，可编辑、删除、审批、作废。审批通过后进入「已批准」状态</el-descriptions-item>
          <el-descriptions-item label="已批准">审批通过的移库单，可执行移库操作或作废</el-descriptions-item>
          <el-descriptions-item label="已完成">执行移库后完成，系统自动更新源库位和目标库位的库存</el-descriptions-item>
          <el-descriptions-item label="已驳回">审批驳回后回退为待审批状态，可修改后重新提交</el-descriptions-item>
          <el-descriptions-item label="已作废">移库单已作废，不再有效</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">库存物料选择</div>
              <div class="highlight-card-body">选择仓库后自动加载该仓库可用库存，选择库存物料后自动带出<strong>物料编码、名称、批次号、源库位、可用数量</strong>等信息</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">目标库位限制</div>
              <div class="highlight-card-body">目标库位<strong>不能与源库位相同</strong>，移库数量不能超过可用库存数量。系统自动排除源库位</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">审批流程</div>
              <div class="highlight-card-body">移库单需经过<strong>审批通过</strong>后才能执行。审批时可填写审批意见，支持通过和驳回</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">库存自动更新</div>
              <div class="highlight-card-body">执行移库后系统自动<strong>减少源库位库存、增加目标库位库存</strong>，无需手动维护库存</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建移库单：</strong>选择仓库和库存物料，系统自动带出源库位和可用数量，填写移库数量和目标库位
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>审批移库单：</strong>在列表中点击「审批」按钮，查看移库详情并填写审批意见，通过或驳回
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>执行移库：</strong>审批通过后点击「执行」按钮，系统自动完成库位间库存转移
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>作废移库单：</strong>待审批或已批准状态的移库单可作废，作废后不可恢复
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsMove">
import { listMove, getMove, addMove, delMove, approveMove, rejectMove, executeMove, voidMove } from '@/api/wms/move'
import { listWarehouse, listLocation } from '@/api/wms/warehouse'
import { listInventory } from '@/api/wms/inventory'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { ArrowRight, ArrowDown, QuestionFilled, WarningFilled, Filter, Search } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'move', 'other', 'dBasic', 'dMaterial', 'dLocation', 'dApprove', 'dOther', 'aBasic', 'aMaterial', 'aLocation', 'aOpinion'])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_move_index')
const { wms_move_status, wms_unit } = proxy.useDict('wms_move_status', 'wms_unit')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref(''); const activeStatusTab = ref('all'); const statusCounts = ref({ all: 0 }); const dateRange = ref([])
const warehouseOptions = ref([]); const inventoryOptions = ref([]); const allLocationOptions = ref([]); const detailOpen = ref(false); const detailData = ref({}); const approveOpen = ref(false); const approveForm = ref({}); const showStatusHelp = ref(false)
const defaultColumns = { moveNo: { label: '移库单号', visible: true }, warehouseName: { label: '仓库', visible: true }, materialCode: { label: '物料编码', visible: true }, materialName: { label: '物料名称', visible: true }, unit: { label: '单位', visible: true }, batchNo: { label: '批次号', visible: true }, fromLocationName: { label: '源库位', visible: true }, toLocationName: { label: '目标库位', visible: true }, moveQty: { label: '移库数量', visible: true }, status: { label: '状态', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('wms_move_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let count = 0; if (queryParams.value.moveNo) count++; if (queryParams.value.warehouseId) count++; if (queryParams.value.materialCode) count++; if (queryParams.value.materialName) count++; if (queryParams.value.status) count++; if (dateRange.value && dateRange.value.length > 0) count++; return count })
const statusTabList = computed(() => wms_move_status.value)
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, moveNo: undefined, warehouseId: undefined, materialCode: undefined, materialName: undefined, status: undefined },
  rules: {
    warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'change' }],
    inventoryId: [{ required: true, message: '请选择库存物料', trigger: 'change' }],
    toLocationId: [{ required: true, message: '目标库位不能为空', trigger: 'change' }],
    moveQty: [{ required: true, message: '移库数量不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 目标库位选项：排除源库位 */
const targetLocationOptions = computed(() => {
  if (!form.value.fromLocationId) return allLocationOptions.value
  return allLocationOptions.value.filter(l => l.warehouseId !== form.value.fromLocationId)
})

function getList() { loading.value = true; listMove(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.moveNo = undefined; queryParams.value.warehouseId = undefined; queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.status = undefined; queryParams.value.params = {}; dateRange.value = []; activeStatusTab.value = 'all'; handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.moveId); multiple.value = !sel.length }
function reset() {
  form.value = { warehouseId: undefined, inventoryId: undefined, materialId: undefined, unit: undefined, batchNo: undefined, fromLocationId: undefined, fromLocationCode: undefined, fromLocationName: undefined, toLocationId: undefined, toLocationCode: undefined, toLocationName: undefined, moveQty: 0, availableQty: 0, remark: undefined }
  inventoryOptions.value = []; allLocationOptions.value = []
  proxy.resetForm('moveRef')
}
function handleAdd() { reset(); open.value = true; title.value = '添加移库单' }
function submitForm() {
  proxy.$refs['moveRef'].validate(v => {
    if (v) {
      if (form.value.moveQty <= 0) { proxy.$modal.msgError('移库数量必须大于0'); return }
      if (form.value.toLocationId === form.value.fromLocationId) { proxy.$modal.msgError('目标库位不能与源库位相同'); return }
      addMove(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
    }
  })
}
function handleApprove(row) {
  getMove(row.moveId).then(res => {
    approveForm.value = { ...res.data, approveOpinion: '' }
    approveOpen.value = true
  })
}
function confirmApprove(passed) {
  if (!approveForm.value.approveOpinion) {
    proxy.$modal.msgWarning('请输入审批意见')
    return
  }
  if (passed) {
    approveMove(approveForm.value.moveId, approveForm.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('审批通过')
      approveOpen.value = false
      getList()
    })
  } else {
    rejectMove(approveForm.value.moveId, approveForm.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('已驳回')
      approveOpen.value = false
      getList()
    })
  }
}
function handleExecute(row) { proxy.$modal.confirm('确认执行移库？').then(() => executeMove(row.moveId)).then(() => { getList(); proxy.$modal.msgSuccess('执行完成') }).catch(() => {}) }
function handleVoid(row) { proxy.$modal.confirm('确认作废该移库单？作废后不可恢复').then(() => voidMove(row.moveId)).then(() => { getList(); proxy.$modal.msgSuccess('已作废') }).catch(() => {}) }
function handleExport() { proxy.download('wms/move/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `move_${new Date().getTime()}.xlsx`) }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'red', '4': 'gray' }; return map[status] || 'gray' }
function statusLabel(status) { const item = wms_move_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-reject', '4': 'tab-void' }; return map[value] || '' }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function handleStatusChange(val) { activeStatusTab.value = val ? val : 'all'; handleQuery() }
function loadStatusCounts() { listMove({ pageNum: 1, pageSize: 999 }).then(res => { const counts = { all: res.total }; (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ }); statusCounts.value = counts }).catch(() => {}) }
function handleDelete(row) { const moveIds = row.moveId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delMove(moveIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function handleDetail(row) { getMove(row.moveId).then(res => { detailData.value = res.data; detailOpen.value = true }) }

/** 选择目标库位后：记录编码和名称 */
function onTargetLocationChange(locationId) {
  const loc = allLocationOptions.value.find(l => l.warehouseId === locationId)
  if (loc) {
    form.value.toLocationCode = loc.warehouseCode
    form.value.toLocationName = loc.warehouseName
  } else {
    form.value.toLocationCode = undefined
    form.value.toLocationName = undefined
  }
}

/** 选择仓库后：加载该仓库的库存列表和库位列表 */
function onWarehouseChange(warehouseId) {
  form.value.inventoryId = undefined
  form.value.unit = undefined
  form.value.batchNo = undefined
  form.value.fromLocationId = undefined
  form.value.fromLocationCode = undefined
  form.value.availableQty = 0
  form.value.moveQty = 0
  form.value.toLocationId = undefined
  inventoryOptions.value = []
  allLocationOptions.value = []
  if (warehouseId) {
    listInventory({ warehouseId: warehouseId, pageSize: 999 }).then(res => {
      inventoryOptions.value = res.rows.filter(r => r.qty > 0)
    })
    listLocation({ warehouseId: warehouseId, status: '0', pageSize: 999 }).then(res => {
      allLocationOptions.value = res.rows
    })
  }
}

/** 选择库存物料后：自动带出物料信息、批次号、源库位、可用数量 */
function onInventoryChange(inventoryId) {
  const inv = inventoryOptions.value.find(i => i.inventoryId === inventoryId)
  if (inv) {
    form.value.materialId = inv.materialId
    form.value.materialCode = inv.materialCode
    form.value.materialName = inv.materialName
    form.value.unit = inv.unit
    form.value.batchNo = inv.batchNo
    form.value.fromLocationId = inv.locationId
    form.value.fromLocationCode = inv.locationCode
    form.value.fromLocationName = inv.locationName
    form.value.availableQty = inv.qty
    form.value.moveQty = inv.qty
    form.value.toLocationId = undefined
  }
}

listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
getList()
loadStatusCounts()
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
