<template>
  <div class="app-container mms-kit-page">
    <!-- ===== 统计概览卡片 ===== -->
    <div class="kit-summary-cards" v-if="summaryData.total > 0">
      <div class="summary-card summary-total">
        <div class="summary-icon"><el-icon><Box /></el-icon></div>
        <div class="summary-body">
          <div class="summary-label">齐套检查总数</div>
          <div class="summary-value">{{ summaryData.total }}</div>
        </div>
      </div>
      <div class="summary-card summary-pass">
        <div class="summary-icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="summary-body">
          <div class="summary-label">齐套通过</div>
          <div class="summary-value">{{ summaryData.passCount }}</div>
        </div>
      </div>
      <div class="summary-card summary-short">
        <div class="summary-icon"><el-icon><WarningFilled /></el-icon></div>
        <div class="summary-body">
          <div class="summary-label">缺料异常</div>
          <div class="summary-value">{{ summaryData.shortCount }}</div>
        </div>
      </div>
      <div class="summary-card summary-rate">
        <div class="summary-icon"><el-icon><DataAnalysis /></el-icon></div>
        <div class="summary-body">
          <div class="summary-label">平均齐套率</div>
          <div class="summary-value" :class="{ 'rate-good': summaryData.avgRate >= 100, 'rate-warn': summaryData.avgRate >= 80 && summaryData.avgRate < 100, 'rate-bad': summaryData.avgRate < 80 }">{{ summaryData.avgRate }}%</div>
        </div>
      </div>
    </div>

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
          <label>齐套单号</label>
          <div class="control"><el-input v-model="queryParams.kitNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>主计划号</label>
          <div class="control"><el-input v-model="queryParams.mpsNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>是否齐套</label>
          <div class="control is-select"><el-select v-model="queryParams.isComplete" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_yes_no" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_kit_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>BOM编号</label>
          <div class="control"><el-input v-model="queryParams.bomNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>检查日期</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button>
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="MagicStick" @click="handleAutoCheck" v-hasPermi="['mms:kit:add']">一键齐套检查</el-button>
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:kit:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:kit:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:kit:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:kit:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_kit_columns" /></div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="齐套单号" prop="kitNo" key="kitNo" :width="colWidth('kitNo', 140)" resizable v-if="columns.kitNo.visible" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="主计划号" prop="mpsNo" key="mpsNo" :width="colWidth('mpsNo', 140)" resizable v-if="columns.mpsNo.visible" />
          <el-table-column label="检查时间" prop="checkTime" key="checkTime" :width="colWidth('checkTime', 160)" resizable align="center" v-if="columns.checkTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.checkTime) }}</span></template>
          </el-table-column>
          <el-table-column label="齐套率(%)" prop="kitRate" key="kitRate" :width="colWidth('kitRate', 120)" resizable align="center" v-if="columns.kitRate.visible">
            <template #default="scope">
              <div class="rate-bar-wrap">
                <div class="rate-bar" :style="{ width: Math.min(scope.row.kitRate, 100) + '%', background: scope.row.kitRate >= 100 ? '#10b981' : scope.row.kitRate >= 80 ? '#f59e0b' : '#ef4444' }"></div>
                <span class="rate-text" :style="{ color: scope.row.kitRate >= 100 ? '#10b981' : scope.row.kitRate >= 80 ? '#f59e0b' : '#ef4444', fontWeight: 600 }">{{ scope.row.kitRate }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="是否齐套" prop="isComplete" key="isComplete" :width="colWidth('isComplete', 90)" resizable align="center" v-if="columns.isComplete.visible">
            <template #default="scope"><span v-if="scope.row.isComplete" class="badge" :class="scope.row.isComplete === '1' ? 'green' : 'red'"><span class="dot"></span>{{ dictLabel(mms_yes_no, scope.row.isComplete) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:kit:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="success" icon="Aim" @click="handleExecute(scope.row)" v-hasPermi="['mms:kit:execute']">执行检查</el-button>
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['mms:kit:query']">明细</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="齐套单号" prop="kitNo"><el-input v-model="form.kitNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请输入" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="主计划号" prop="mpsNo"><el-input v-model="form.mpsNo" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="BOM编号" prop="bomNo"><el-input v-model="form.bomNo" placeholder="请输入" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">齐套检查详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.kitNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.kitNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">齐套单号</span><div class="rd-value">{{ viewData.kitNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">主计划号</span><div class="rd-value">{{ viewData.mpsNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">BOM编号</span><div class="rd-value">{{ viewData.bomNo || '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">齐套率</span><div class="rd-value" :style="{ color: viewData.kitRate >= 100 ? '#10b981' : viewData.kitRate >= 80 ? '#f59e0b' : '#ef4444', fontWeight: 600 }">{{ viewData.kitRate != null ? viewData.kitRate + '%' : '-' }}</div></div><div class="rd-item"><span class="rd-label">是否齐套</span><div class="rd-value"><dict-tag :options="mms_yes_no" :value="viewData.isComplete" /></div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_kit_status" :value="viewData.status" /></div></div><div class="rd-item"><span class="rd-label">检查时间</span><div class="rd-value">{{ viewData.checkTime ? parseTime(viewData.checkTime) : '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 明细Dialog ===== -->
    <el-dialog title="齐套检查明细" v-model="detailOpen" width="960px" append-to-body>
      <div class="detail-summary" v-if="detailList.length > 0">
        <div class="detail-summary-item"><span class="label">物料总项数</span><span class="value">{{ detailList.length }}</span></div>
        <div class="detail-summary-item"><span class="label">齐套项数</span><span class="value" style="color:#10b981;font-weight:600">{{ detailList.filter(d => d.isComplete === '1').length }}</span></div>
        <div class="detail-summary-item"><span class="label">缺料项数</span><span class="value" style="color:#ef4444;font-weight:600">{{ detailList.filter(d => d.isComplete === '0').length }}</span></div>
      </div>
      <el-table :data="detailList" border :max-height="500">
        <el-table-column label="物料编码" align="center" prop="materialCode" width="120" />
        <el-table-column label="物料名称" align="center" prop="materialName" min-width="150" show-overflow-tooltip />
        <el-table-column label="规格型号" align="center" prop="specModel" width="120" />
        <el-table-column label="单位" align="center" prop="unit" width="60"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
        <el-table-column label="需求数量" align="center" prop="requiredQty" width="100" />
        <el-table-column label="可用数量" align="center" prop="availableQty" width="100" />
        <el-table-column label="缺口量" align="center" prop="shortageQty" width="100">
          <template #default="scope"><span :style="{ color: scope.row.shortageQty > 0 ? '#ef4444' : '#10b981', fontWeight: 600 }">{{ scope.row.shortageQty }}</span></template>
        </el-table-column>
        <el-table-column label="是否齐套" align="center" prop="isComplete" width="80">
          <template #default="scope"><span v-if="scope.row.isComplete" class="badge" :class="scope.row.isComplete === '1' ? 'green' : 'red'"><span class="dot"></span>{{ dictLabel(mms_yes_no, scope.row.isComplete) }}</span><span v-else class="text-muted">—</span></template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 一键齐套检查-工单选择弹窗 ===== -->
    <el-dialog v-model="autoCheckOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg></div>
          <span class="rd-detail-header-title">一键齐套检查</span>
        </div>
      </template>
      <div class="auto-check-content">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
          <template #title>选择需要进行齐套检查的工单，系统将自动根据工单BOM展开物料需求并对比库存，生成齐套检查结果。</template>
        </el-alert>
        <div class="auto-check-toolbar">
          <el-input v-model="workOrderQuery.workOrderNo" placeholder="工单号" clearable @keyup.enter="loadWorkOrders" style="width: 160px" />
          <el-input v-model="workOrderQuery.productName" placeholder="产品名称" clearable @keyup.enter="loadWorkOrders" style="width: 160px" />
          <el-button type="primary" icon="Search" @click="loadWorkOrders">搜索</el-button>
          <el-button icon="RefreshLeft" @click="resetWorkOrderQuery">重置</el-button>
        </div>
        <el-table ref="workOrderTableRef" v-loading="workOrderLoading" :data="workOrderList" border @selection-change="handleWorkOrderSelectionChange" :max-height="400" style="margin-top: 12px">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="工单号" prop="workOrderNo" width="130" align="center" />
          <el-table-column label="产品编码" prop="productCode" width="120" align="center" show-overflow-tooltip />
          <el-table-column label="产品名称" prop="productName" min-width="150" show-overflow-tooltip />
          <el-table-column label="计划数量" prop="planQty" width="90" align="center" />
          <el-table-column label="BOM编号" prop="bomNo" width="120" align="center" show-overflow-tooltip />
          <el-table-column label="状态" prop="status" width="80" align="center">
            <template #default="scope"><span class="badge" :class="workOrderStatusBadgeClass(scope.row.status)"><span class="dot"></span>{{ workOrderStatusLabel(scope.row.status) }}</span></template>
          </el-table-column>
        </el-table>
        <pagination v-show="workOrderTotal > 0" :total="workOrderTotal" v-model:page="workOrderQuery.pageNum" v-model:limit="workOrderQuery.pageSize" @pagination="loadWorkOrders" :small="true" />
      </div>
      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 13px; color: #64748b;">已选 {{ selectedWorkOrderIds.length }} 个工单</span>
          <div>
            <el-button @click="autoCheckOpen = false">取 消</el-button>
            <el-button type="primary" :disabled="selectedWorkOrderIds.length === 0" @click="submitAutoCheck">开始齐套检查</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="齐套检查业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、齐套检查释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是齐套检查？</div>
          <div class="highlight-card-body">
            <strong>齐套检查（Kit Check）</strong>是生产管控中在工单开工前对物料齐套情况进行校验的单据。系统自动比对BOM物料需求与当前库存，计算齐套率，标识缺口物料，确保工单开工前物料充足，避免因缺料导致生产中断。<br/><br/>
            齐套检查遵循 <strong>MES 物料齐套管理规范</strong>，通过齐套率（≥100%齐套/80-99%部分齐套/&lt;80%不齐套）直观反映物料准备状态，支持缺料预警和物料跟进。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="warning">待检查</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">点击「执行检查」或「一键齐套检查」</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已通过</el-tag></div>
          <div class="flow-item" style="margin-left: 8px;"><el-tag type="danger">缺料</el-tag></div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待检查">齐套检查新建后的初始状态，等待执行检查</el-descriptions-item>
          <el-descriptions-item label="已通过">齐套检查已执行完成，所有物料满足需求（齐套率100%）</el-descriptions-item>
          <el-descriptions-item label="缺料">齐套检查已执行完成，存在物料缺口（齐套率&lt;100%）</el-descriptions-item>
        </el-descriptions>
        <h4>四、重点业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心规则</div>
          <div class="highlight-card-body">
            <p>• <strong>一键齐套检查：</strong>选择工单后自动创建齐套检查单并执行检查，一步到位</p>
            <p>• <strong>批量检查：</strong>支持同时选择多个工单进行批量齐套检查</p>
            <p>• <strong>自动BOM展开：</strong>系统根据工单关联BOM自动展开物料需求（含损耗率计算）</p>
            <p>• <strong>库存对比：</strong>自动查询WMS库存可用量（可用 = 库存 - 锁定），计算缺口</p>
            <p>• <strong>齐套率：</strong>齐套率≥100%为齐套（绿色），80-99%为部分齐套（黄色），&lt;80%为不齐套（红色）</p>
            <p>• <strong>明细查看：</strong>已检查的齐套单可查看各物料的齐套明细，包括缺口量</p>
            <p>• <strong>缺口量监控：</strong>缺口量大于0时自动标红显示，便于物料跟进</p>
          </div>
        </div>
        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>选择工单：</strong>点击「一键齐套检查」选择需要进行检查的工单（可批量）</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>自动检查：</strong>系统自动展开BOM物料需求，对比库存可用量，计算齐套率和缺口量</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>查看结果：</strong>检查完成后查看齐套率和各物料明细，跟进缺料</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="KitCheck">
import { listKit, getKitCheck, addKitCheck, updateKitCheck, delKitCheck, executeKitCheck, getKitCheckDetail, batchAutoKitCheck } from "@/api/mms/kit";
import { listWorkOrder } from "@/api/mms/workorder";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, MagicStick, Box, CircleCheck, DataAnalysis } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_kit_status, mms_yes_no, wms_unit } = proxy.useDict("mms_kit_status", "mms_yes_no", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_kit_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","vc0","vc1"])

const dataList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusCounts = ref({});
const showStatusHelp = ref(false);
const detailOpen = ref(false);
const detailList = ref([]);

// ===== 一键齐套检查相关 =====
const autoCheckOpen = ref(false);
const workOrderLoading = ref(false);
const workOrderList = ref([]);
const workOrderTotal = ref(0);
const selectedWorkOrderIds = ref([]);
const workOrderQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined, status: undefined });

// ===== 统计概览 =====
const summaryData = ref({ total: 0, passCount: 0, shortCount: 0, avgRate: 0 });

const statusTabList = computed(() => mms_kit_status.value ? mms_kit_status.value.map(d => ({ label: d.label, value: d.value })) : []);

const defaultColumns = {
  kitNo: { label: '齐套单号', visible: true }, workOrderNo: { label: '工单号', visible: true },
  mpsNo: { label: '主计划号', visible: true }, checkTime: { label: '检查时间', visible: true },
  kitRate: { label: '齐套率', visible: true }, isComplete: { label: '是否齐套', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_kit_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.kitNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.mpsNo) c++; if (queryParams.value.isComplete) c++; if (queryParams.value.status) c++; if (queryParams.value.bomNo) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, kitNo: undefined, workOrderNo: undefined, mpsNo: undefined, bomNo: undefined, isComplete: undefined, status: undefined, params: {} },
  rules: { workOrderNo: [{ required: true, message: "请输入工单号", trigger: "blur" }] }
});
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listKit(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); loadSummary(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.kitNo) baseQuery.kitNo = queryParams.value.kitNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.mpsNo) baseQuery.mpsNo = queryParams.value.mpsNo; if (queryParams.value.bomNo) baseQuery.bomNo = queryParams.value.bomNo; if (queryParams.value.isComplete) baseQuery.isComplete = queryParams.value.isComplete; listKit(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_kit_status.value) { mms_kit_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function loadSummary() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.kitNo) baseQuery.kitNo = queryParams.value.kitNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.mpsNo) baseQuery.mpsNo = queryParams.value.mpsNo; listKit(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const rows = res.rows || []; let passCount = 0, shortCount = 0, totalRate = 0, checkedCount = 0; rows.forEach(r => { if (r.status === '1') { passCount++; } if (r.status === '2') { shortCount++; } if (r.kitRate != null && (r.status === '1' || r.status === '2')) { totalRate += parseFloat(r.kitRate); checkedCount++; } }); summaryData.value = { total: res.total, passCount, shortCount, avgRate: checkedCount > 0 ? (totalRate / checkedCount).toFixed(2) : 0 }; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.kitNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.mpsNo = undefined; queryParams.value.bomNo = undefined; queryParams.value.isComplete = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.kitId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { kitNo: undefined, workOrderNo: undefined, mpsNo: undefined, bomNo: undefined, remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增齐套检查"; }
function handleUpdate(row) { reset(); const id = row.kitId || ids.value[0]; getKitCheck(id).then(response => { form.value = response.data; open.value = true; title.value = "修改齐套检查"; }); }
function handleView(row) { const id = row.kitId || ids.value[0]; getKitCheck(id).then(response => { viewData.value = response.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.kitId != null) { updateKitCheck(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addKitCheck(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.kitId || ids.value; proxy.$modal.confirm('是否确认删除选中的齐套检查记录？').then(() => delKitCheck(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/kit/export", { ...queryParams.value }, `kit_${new Date().getTime()}.xlsx`); }
function handleExecute(row) { proxy.$modal.confirm('是否执行齐套检查？').then(() => executeKitCheck(row.kitId)).then(() => { getList(); proxy.$modal.msgSuccess("执行成功"); }).catch(() => {}); }
function handleDetail(row) { getKitCheckDetail(row.kitId).then(response => { detailList.value = response.data; detailOpen.value = true; }); }

// ===== 一键齐套检查 =====
function handleAutoCheck() { autoCheckOpen.value = true; workOrderQuery.pageNum = 1; workOrderQuery.workOrderNo = undefined; workOrderQuery.productName = undefined; selectedWorkOrderIds.value = []; loadWorkOrders(); }
function loadWorkOrders() { workOrderLoading.value = true; listWorkOrder({ ...workOrderQuery }).then(response => { workOrderList.value = response.rows; workOrderTotal.value = response.total; workOrderLoading.value = false; }).catch(() => { workOrderLoading.value = false; }); }
function resetWorkOrderQuery() { workOrderQuery.workOrderNo = undefined; workOrderQuery.productName = undefined; workOrderQuery.pageNum = 1; loadWorkOrders(); }
function handleWorkOrderSelectionChange(selection) { selectedWorkOrderIds.value = selection.map(item => item.workOrderId); }
function submitAutoCheck() { if (selectedWorkOrderIds.value.length === 0) return; const loadingInstance = proxy.$loading({ text: '正在执行齐套检查...' }); batchAutoKitCheck(selectedWorkOrderIds.value).then(response => { loadingInstance.close(); autoCheckOpen.value = false; proxy.$modal.msgSuccess(response.msg); getList(); }).catch(() => { loadingInstance.close(); }); }

function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_kit_status, status); }
function badgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'red' }; return map[status] || 'gray'; }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-done', '2': 'tab-short' }; return map[value] || ''; }
function workOrderStatusLabel(status) { const map = { '0': '新建', '1': '已下达', '2': '执行中', '3': '报工中', '4': '待质检', '5': '完工', '6': '已关闭', '7': '已暂停', '8': '已作废' }; return map[status] || '未知'; }
function workOrderStatusBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'blue', '3': 'blue', '4': 'amber', '5': 'green', '6': 'gray', '7': 'red', '8': 'gray' }; return map[status] || 'gray'; }

getList();
</script>

<style scoped>
.mms-kit-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-kit-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-kit-page .filter-card{padding:14px 20px 16px}
.mms-kit-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-kit-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-kit-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-kit-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-kit-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-kit-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-kit-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-kit-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-kit-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-kit-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-kit-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-kit-page .field{display:flex;flex-direction:column;gap:6px}
.mms-kit-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-kit-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-kit-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-kit-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-kit-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-kit-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-kit-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-kit-page .field .control :deep(.el-select){width:100%}
.mms-kit-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-kit-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-kit-page .toolbar .left,.mms-kit-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-kit-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-kit-page .table-wrap{overflow-x:auto}
.mms-kit-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-kit-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-kit-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-kit-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-kit-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-kit-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-kit-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-kit-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-kit-page .badge.amber .dot{background:var(--amber-500)}
.mms-kit-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-kit-page .badge.blue .dot{background:var(--blue-500)}
.mms-kit-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-kit-page .badge.green .dot{background:var(--green-500)}
.mms-kit-page .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.mms-kit-page .badge.red .dot{background:var(--red-500)}
.mms-kit-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-kit-page .badge.gray .dot{background:var(--ink-400)}
.mms-kit-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-kit-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-kit-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-kit-page .tabs-track::-webkit-scrollbar{display:none}
.mms-kit-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-kit-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-kit-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-kit-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-kit-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-kit-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-kit-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-kit-page .status-tab.tab-draft .dot{background:var(--amber-500)}.mms-kit-page .status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)}.mms-kit-page .status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
.mms-kit-page .status-tab.tab-done .dot{background:var(--green-500)}.mms-kit-page .status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)}.mms-kit-page .status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
.mms-kit-page .status-tab.tab-short .dot{background:var(--red-500)}.mms-kit-page .status-tab.tab-short .count{background:var(--red-50);color:var(--red-700)}.mms-kit-page .status-tab.is-active.tab-short .count{background:var(--red-500);color:#fff}
.mms-kit-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-kit-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-kit-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
/* 统计概览卡片 */
.kit-summary-cards{display:flex;gap:12px;margin-bottom:8px}
.kit-summary-cards .summary-card{flex:1;display:flex;align-items:center;gap:12px;padding:16px 20px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card)}
.kit-summary-cards .summary-card .summary-icon{width:42px;height:42px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:20px;flex-shrink:0}
.kit-summary-cards .summary-card .summary-icon .el-icon{font-size:20px}
.kit-summary-cards .summary-card .summary-label{font-size:13px;color:var(--ink-500);margin-bottom:2px}
.kit-summary-cards .summary-card .summary-value{font-size:24px;font-weight:700;color:var(--ink-900);line-height:1}
.kit-summary-cards .summary-total .summary-icon{background:var(--brand-50);color:var(--brand-600)}
.kit-summary-cards .summary-pass .summary-icon{background:var(--green-50);color:var(--green-500)}
.kit-summary-cards .summary-short .summary-icon{background:var(--red-50);color:var(--red-500)}
.kit-summary-cards .summary-rate .summary-icon{background:var(--blue-50);color:var(--blue-500)}
.kit-summary-cards .summary-rate .rate-good{color:var(--green-500)}
.kit-summary-cards .summary-rate .rate-warn{color:var(--amber-500)}
.kit-summary-cards .summary-rate .rate-bad{color:var(--red-500)}
/* 齐套率进度条 */
.mms-kit-page .rate-bar-wrap{position:relative;width:100%;min-width:80px;height:24px;background:var(--ink-100);border-radius:4px;overflow:hidden;display:flex;align-items:center;justify-content:center}
.mms-kit-page .rate-bar{position:absolute;left:0;top:0;height:100%;border-radius:4px;transition:width .3s var(--ease-out)}
.mms-kit-page .rate-text{position:relative;z-index:1;font-size:13px}
/* 明细摘要 */
.detail-summary{display:flex;gap:24px;padding:12px 20px;background:var(--ink-50);border-radius:8px;margin-bottom:12px}
.detail-summary-item{display:flex;align-items:center;gap:8px}
.detail-summary-item .label{font-size:13px;color:var(--ink-500)}
.detail-summary-item .value{font-size:18px;font-weight:700;color:var(--ink-900)}
/* 一键齐套检查工具栏 */
.auto-check-toolbar{display:flex;gap:8px;align-items:center;flex-wrap:wrap}
/* 业务说明 */
.status-help-content{max-height:500px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{background-color:#ecf5ff;border-radius:8px;padding:16px;border-left:4px solid #409eff}
.status-help-content .highlight-card p{margin:6px 0;line-height:1.6;font-size:13px;color:#606266}
.status-help-content .highlight-card.highlight-warning{background-color:#fdf6ec;border-left-color:#e6a23c}
@media(max-width:1100px){.mms-kit-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}.kit-summary-cards{flex-wrap:wrap}.kit-summary-cards .summary-card{flex:1 1 45%}}
@media(max-width:720px){.mms-kit-page .filter-card .filter-bar{grid-template-columns:1fr}.kit-summary-cards{flex-direction:column}}
</style>