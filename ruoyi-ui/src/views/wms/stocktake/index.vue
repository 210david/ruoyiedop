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
          <label>盘点单号</label>
          <div class="control">
            <el-input v-model="queryParams.takeNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>仓库</label>
          <div class="control is-select">
            <el-select v-model="queryParams.warehouseId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>盘点类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.takeType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_take_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleStatusChange">
              <el-option v-for="d in wms_take_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>计划日期</label>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:stocktake:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:stocktake:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_stocktake_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="盘点单号" prop="takeNo" key="takeNo" :width="colWidth('takeNo', 160)" resizable v-if="columns.takeNo.visible" />
          <el-table-column label="仓库" prop="warehouseName" key="warehouseName" :width="colWidth('warehouseName', 120)" resizable v-if="columns.warehouseName.visible" />
          <el-table-column label="库区" prop="areaName" key="areaName" :width="colWidth('areaName', 120)" resizable v-if="columns.areaName.visible" />
          <el-table-column label="盘点类型" prop="takeType" key="takeType" :width="colWidth('takeType', 100)" resizable align="center" v-if="columns.takeType.visible">
            <template #default="scope"><span class="badge violet">{{ takeTypeLabel(scope.row.takeType) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="计划日期" prop="planDate" key="planDate" :width="colWidth('planDate', 120)" resizable align="center" v-if="columns.planDate.visible" />
          <el-table-column label="备注" prop="remark" key="remark" :width="colWidth('remark', 200)" resizable :show-overflow-tooltip="true" v-if="columns.remark.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="300" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
              <el-button link type="primary" icon="VideoPlay" @click="handleStart(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['wms:stocktake:start']">开始</el-button>
              <el-button link type="primary" icon="Check" @click="handleApprove(scope.row)" v-if="scope.row.status === '2'" v-hasPermi="['wms:stocktake:approve']">审批</el-button>
              <el-button link type="danger" icon="CircleClose" @click="handleVoid(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1'" v-hasPermi="['wms:stocktake:void']">作废</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:stocktake:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
        <el-form ref="takeRef" :model="form" :rules="rules" label-width="100px">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>盘点范围</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%" @change="onWarehouseChange"><el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="库区" prop="areaId"><el-select v-model="form.areaId" filterable clearable placeholder="请选择库区（可选）" style="width:100%"><el-option v-for="a in areaOptions" :key="a.warehouseId" :label="a.warehouseName" :value="a.warehouseId" /></el-select></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('setting')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>盘点设置</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.setting }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.setting">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="盘点类型" prop="takeType"><el-select v-model="form.takeType" style="width:100%" @change="onTakeTypeChange"><el-option v-for="d in wms_take_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12" v-if="form.takeType === '1'"><el-form-item label="抽样比例" prop="sampleRatio"><el-slider v-model="form.sampleRatio" :min="10" :max="100" :step="10" show-input style="width:100%" /></el-form-item></el-col>
                <el-col :span="12" v-if="form.takeType === '2'"><el-form-item label="循环批次" prop="cycleNo"><el-select v-model="form.cycleNo" style="width:100%"><el-option :value="1" label="第1轮（共4轮）" /><el-option :value="2" label="第2轮（共4轮）" /><el-option :value="3" label="第3轮（共4轮）" /><el-option :value="4" label="第4轮（共4轮）" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计划日期" prop="planDate"><el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('other')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
            </div>
          </section>
        </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailOpen" width="1296px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">盘点详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.takeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ detailData.takeNo }}</span>
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
              <div class="rd-item"><span class="rd-label">盘点单号</span><div class="rd-value">{{ detailData.takeNo }}</div></div>
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ detailData.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">库区</span><div class="rd-value">{{ detailData.areaName || '全仓库' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_take_status" :value="detailData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">盘点类型</span><div class="rd-value"><dict-tag :options="wms_take_type" :value="detailData.takeType" /></div></div>
              <div class="rd-item" v-if="detailData.takeType === '1'"><span class="rd-label">抽样比例</span><div class="rd-value">{{ detailData.sampleRatio }}%</div></div>
              <div class="rd-item" v-if="detailData.takeType === '2'"><span class="rd-label">循环批次</span><div class="rd-value">第{{ detailData.cycleNo }}轮（共4轮）</div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ detailData.planDate }}</div></div>
              <div class="rd-item" v-if="detailData.startTime"><span class="rd-label">开始时间</span><div class="rd-value">{{ detailData.startTime }}</div></div>
              <div class="rd-item" v-if="detailData.endTime"><span class="rd-label">结束时间</span><div class="rd-value">{{ detailData.endTime }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !detailData.remark }">{{ detailData.remark || '暂无备注' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dDetail')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>盘点明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dDetail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dDetail">
            <el-table :data="detailPageList" border @header-dragend="onHeaderDragEnd">
              <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
              <el-table-column label="物料名称" prop="materialName" min-width="200" show-overflow-tooltip />
              <el-table-column label="库区/库位" width="180">
                <template #default="scope">{{ [scope.row.areaName, scope.row.locationName].filter(Boolean).join(' / ') || '-' }}</template>
              </el-table-column>
              <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 100)" resizable />
              <el-table-column label="账面数量" prop="bookQty" :width="colWidth('bookQty', 100)" resizable align="right" />
              <el-table-column label="实盘数量" prop="actualQty" :width="colWidth('actualQty', 100)" resizable align="right" />
              <el-table-column label="差异" prop="diffQty" :width="colWidth('diffQty', 100)" resizable align="right">
                <template #default="scope"><span :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.diffQty }}</span></template>
              </el-table-column>
              <el-table-column label="差异原因" prop="diffReason" show-overflow-tooltip />
            </el-table>
            <pagination v-show="(detailData.detailList || []).length > 0" :total="(detailData.detailList || []).length" v-model:page="detailPage.pageNum" v-model:limit="detailPage.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, prev, pager, next, jumper" />
          </div>
        </section>
        <!-- 审批信息 -->
        <section class="rd-card" v-if="detailData.approveLogList && detailData.approveLogList.length">
          <div class="rd-card-header" @click="toggleCard('dApprove')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dApprove }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dApprove">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="item in detailData.approveLogList" :key="item.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': item.approveAction === 'pass', 'rd-timeline-dot--error': item.approveAction === 'reject' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="item.approveAction === 'pass'" type="success" size="small" effect="light" round>审批通过</el-tag>
                      <el-tag v-else-if="item.approveAction === 'reject'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ item.approveTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ item.approveBy }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="item.approveOpinion">
                    <strong>审批意见：</strong>{{ item.approveOpinion }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      <template #footer><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveOpen" width="1152px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></div>
          <span class="rd-detail-header-title">盘点审批</span>
          <div class="rd-detail-header-sub" v-if="approveForm.takeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ approveForm.takeNo }}</span>
            <dict-tag :options="wms_take_status" :value="approveForm.status" />
          </div>
        </div>
      </template>
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">盘点单号</span><div class="rd-value">{{ approveForm.takeNo }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_take_status" :value="approveForm.status" /></div></div>
              <div class="rd-item"><span class="rd-label">盘点类型</span><div class="rd-value"><dict-tag :options="wms_take_type" :value="approveForm.takeType" /></div></div>
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ approveForm.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">库区</span><div class="rd-value">{{ approveForm.areaName || '全仓库' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ approveForm.planDate }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ approveForm.createBy }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ approveForm.createTime }}</div></div>
            </div>
          </div>
        </section>
        <!-- 盘点明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aDetail')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>盘点明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aDetail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aDetail">
            <el-table :data="approvePageList" border size="small">
              <el-table-column label="物料编码" prop="materialCode" width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="180" show-overflow-tooltip />
              <el-table-column label="批次号" prop="batchNo" width="100" />
              <el-table-column label="账面数量" prop="bookQty" width="100" align="right" />
              <el-table-column label="实盘数量" prop="actualQty" width="100" align="right" />
              <el-table-column label="差异" prop="diffQty" width="100" align="right">
                <template #default="scope"><span :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.diffQty }}</span></template>
              </el-table-column>
              <el-table-column label="差异原因" prop="diffReason" min-width="150" show-overflow-tooltip />
            </el-table>
            <pagination v-show="(approveForm.detailList || []).length > 0" :total="(approveForm.detailList || []).length" v-model:page="approvePage.pageNum" v-model:limit="approvePage.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, prev, pager, next, jumper" />
          </div>
        </section>
        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aOpinion')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></span>审批意见</div>
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

    <!-- 录入实盘数量 -->
    <el-dialog v-model="submitOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div>
          <span class="rd-detail-header-title">录入实盘数量</span>
          <div class="rd-detail-header-sub" v-if="detailData.takeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ detailData.takeNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="submitRef" :model="inputForm" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('sMaterial')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sMaterial }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.sMaterial">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ inputForm.materialCode || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">物料名称</span><div class="rd-value">{{ inputForm.materialName }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ inputForm.batchNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">库区/库位</span><div class="rd-value">{{ [inputForm.areaName, inputForm.locationName].filter(Boolean).join(' / ') || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('sBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>盘点信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.sBasic">
            <div class="rd-grid" style="margin-bottom: 16px">
              <div class="rd-item"><span class="rd-label">账面数量</span><div class="rd-value">{{ inputForm.bookQty }}</div></div>
              <div class="rd-item"><span class="rd-label">当前差异</span><div class="rd-value" :style="{ color: (inputForm.actualQty - inputForm.bookQty) < 0 ? '#f56c6c' : (inputForm.actualQty - inputForm.bookQty) > 0 ? '#67c23a' : '' }">{{ (inputForm.actualQty - inputForm.bookQty).toFixed(2) }}</div></div>
            </div>
            <el-form-item label="实盘数量" prop="actualQty"><el-input-number v-model="inputForm.actualQty" :precision="2" :min="0" style="width: 100%" /></el-form-item>
            <el-form-item label="差异原因"><el-input v-model="inputForm.diffReason" type="textarea" /></el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitDetailForm">确 定</el-button><el-button @click="submitOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="盘点任务业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">待开始</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">盘点中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
            <el-tag size="small" type="info">待开始/盘点中可作废</el-tag>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="danger">驳回</el-tag>
            <el-tag size="small" type="info">退回重新盘点</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待开始">新建盘点单后的初始状态，可编辑、删除、开始盘点、作废。开始后进入「盘点中」状态</el-descriptions-item>
          <el-descriptions-item label="盘点中">盘点进行中，仓库人员在「盘点作业」页面录入实盘数量，可作废</el-descriptions-item>
          <el-descriptions-item label="待审批">盘点完成后提交审批，等待审批人审核。可审批通过或驳回（驳回回退为盘点中）</el-descriptions-item>
          <el-descriptions-item label="已完成">审批通过后完成，系统自动调整库存差异</el-descriptions-item>
          <el-descriptions-item label="已作废">盘点单已作废，不再有效</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">盘点类型说明</div>
              <div class="highlight-card-body">支持<strong>全盘、抽样盘点、循环盘点</strong>三种类型。抽样盘点需设置抽样比例，循环盘点需选择循环批次</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">盘点范围</div>
              <div class="highlight-card-body">可选择<strong>指定仓库或指定库区</strong>进行盘点。选择仓库后自动加载该仓库的库区列表</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">库存自动调整</div>
              <div class="highlight-card-body">审批通过后系统根据<strong>实盘数量与账面数量的差异</strong>自动调整库存，差异为正增加库存，差异为负减少库存</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">审批驳回退回</div>
              <div class="highlight-card-body">审批驳回后盘点单退回到「盘点中」状态，盘点人员需重新核实并录入实盘数量后再次提交</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建盘点单：</strong>选择仓库和库区（可选），设置盘点类型和计划日期，保存后进入「待开始」状态
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>开始盘点：</strong>点击「开始」按钮，系统自动生成盘点明细（基于当前库存快照），进入「盘点中」状态
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>录入与提交：</strong>在「盘点作业」页面录入实盘数量后提交审批，进入「待审批」状态
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>审批完成：</strong>审批通过后系统自动调整库存差异，盘点单进入「已完成」状态
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsStockTake">
import { listStockTake, getStockTake, addStockTake, delStockTake, startStockTake, submitStockTakeDetail, voidStockTake, approveStockTake, rejectStockTake, submitForApproval } from '@/api/wms/stocktake'
import { listWarehouse, listArea } from '@/api/wms/warehouse'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { ArrowRight, ArrowDown, QuestionFilled, WarningFilled, Filter, Search } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'setting', 'other', 'dBasic', 'dDetail', 'dApprove', 'aBasic', 'aDetail', 'aOpinion', 'sMaterial', 'sBasic'])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_stocktake_index')
const { wms_take_type, wms_take_status } = proxy.useDict('wms_take_type', 'wms_take_status')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref(''); const detailOpen = ref(false); const detailData = ref({}); const submitOpen = ref(false); const approveOpen = ref(false); const activeStatusTab = ref('all'); const statusCounts = ref({ all: 0 }); const dateRange = ref([])
const inputForm = ref({})
const approveForm = ref({})
// 盘点明细前端分页
const detailPage = reactive({ pageNum: 1, pageSize: 10 })
const detailPageList = computed(() => {
  const list = detailData.value.detailList || []
  const start = (detailPage.pageNum - 1) * detailPage.pageSize
  return list.slice(start, start + detailPage.pageSize)
})
const approvePage = reactive({ pageNum: 1, pageSize: 10 })
const approvePageList = computed(() => {
  const list = approveForm.value.detailList || []
  const start = (approvePage.pageNum - 1) * approvePage.pageSize
  return list.slice(start, start + approvePage.pageSize)
})
const warehouseOptions = ref([]); const areaOptions = ref([]); const showStatusHelp = ref(false)
const defaultColumns = { takeNo: { label: '盘点单号', visible: true }, warehouseName: { label: '仓库', visible: true }, areaName: { label: '库区', visible: true }, takeType: { label: '盘点类型', visible: true }, status: { label: '状态', visible: true }, planDate: { label: '计划日期', visible: true }, remark: { label: '备注', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('wms_stocktake_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let count = 0; if (queryParams.value.takeNo) count++; if (queryParams.value.warehouseId) count++; if (queryParams.value.takeType) count++; if (queryParams.value.status) count++; if (dateRange.value && dateRange.value.length > 0) count++; return count })
const statusTabList = computed(() => wms_take_status.value)
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, takeNo: undefined, warehouseId: undefined, takeType: undefined, status: undefined }, rules: { warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'change' }], takeType: [{ required: true, message: '盘点类型不能为空', trigger: 'change' }], planDate: [{ required: true, message: '计划日期不能为空', trigger: 'change' }] } })
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listStockTake(proxy.addDateRange(queryParams.value, dateRange.value, 'PlanDate')).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.takeNo = undefined; queryParams.value.warehouseId = undefined; queryParams.value.takeType = undefined; queryParams.value.status = undefined; queryParams.value.params = {}; dateRange.value = []; activeStatusTab.value = 'all'; handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.takeId); multiple.value = !sel.length }
function reset() { form.value = { warehouseId: undefined, areaId: undefined, takeType: '0', sampleRatio: 30, cycleNo: 1, planDate: undefined, remark: undefined }; areaOptions.value = []; proxy.resetForm('takeRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加盘点单' }
function submitForm() { proxy.$refs['takeRef'].validate(v => { if (v) { addStockTake(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } }) }
function handleDetail(row) { getStockTake(row.takeId).then(res => { detailData.value = res.data; detailPage.pageNum = 1; detailOpen.value = true }) }
function handleApprove(row) { getStockTake(row.takeId).then(res => { approveForm.value = { ...res.data, approveOpinion: '' }; approvePage.pageNum = 1; approveOpen.value = true }) }
function confirmApprove(passed) {
  if (!approveForm.value.approveOpinion) { proxy.$modal.msgWarning('请输入审批意见'); return }
  if (passed) {
    approveStockTake(approveForm.value.takeId, approveForm.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('审批通过'); approveOpen.value = false; getList()
    })
  } else {
    rejectStockTake(approveForm.value.takeId, approveForm.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('已驳回，盘点单已退回盘点人员重新核实'); approveOpen.value = false; getList()
    })
  }
}
function handleStart(row) { proxy.$modal.confirm('确认开始盘点？').then(() => startStockTake(row.takeId)).then(() => { getList(); proxy.$modal.msgSuccess('已开始') }).catch(() => {}) }
function handleVoid(row) { proxy.$modal.confirm('确认作废该盘点单？作废后将无法继续盘点。').then(() => voidStockTake(row.takeId)).then(() => { getList(); proxy.$modal.msgSuccess('已作废') }).catch(() => {}) }
function handleSubmitDetail(row) { inputForm.value = { takeId: detailData.value.takeId, detailId: row.detailId, materialCode: row.materialCode, materialName: row.materialName, batchNo: row.batchNo, areaName: row.areaName, locationName: row.locationName, bookQty: row.bookQty, actualQty: row.actualQty || row.bookQty, diffReason: undefined }; submitOpen.value = true }
function submitDetailForm() { submitStockTakeDetail(inputForm.value.takeId, inputForm.value.detailId, inputForm.value.actualQty, inputForm.value.diffReason).then(() => { proxy.$modal.msgSuccess('录入成功'); submitOpen.value = false; getStockTake(detailData.value.takeId).then(res => { detailData.value = res.data }) }) }
function handleSubmitForApproval() { proxy.$modal.confirm('确认提交审批？提交后将等待审批人审批。').then(() => submitForApproval(detailData.value.takeId)).then(() => { proxy.$modal.msgSuccess('已提交审批'); getStockTake(detailData.value.takeId).then(res => { detailData.value = res.data }); getList() }).catch(() => {}) }
function handleDelete(row) { const takeIds = row.takeId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delStockTake(takeIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'violet', '3': 'green', '4': 'gray' }; return map[status] || 'gray' }
function statusLabel(status) { const item = wms_take_status.value.find(d => d.value == status); return item ? item.label : '-' }
function takeTypeLabel(type) { const item = wms_take_type.value.find(d => d.value == type); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-partial', '3': 'tab-done', '4': 'tab-void' }; return map[value] || '' }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function handleStatusChange(val) { activeStatusTab.value = val ? val : 'all'; handleQuery() }
function loadStatusCounts() { listStockTake({ pageNum: 1, pageSize: 999 }).then(res => { const counts = { all: res.total }; (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ }); statusCounts.value = counts }).catch(() => {}) }
function onTakeTypeChange() {
  if (form.value.takeType === '1' && !form.value.sampleRatio) { form.value.sampleRatio = 30 }
  if (form.value.takeType === '2' && !form.value.cycleNo) { form.value.cycleNo = 1 }
}
function onWarehouseChange(warehouseId) {
  form.value.areaId = undefined
  if (warehouseId) {
    listArea({ warehouseId: warehouseId, status: '0', pageSize: 999 }).then(res => { areaOptions.value = res.rows })
  } else {
    areaOptions.value = []
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
