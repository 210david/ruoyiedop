<template>
  <div class="app-container mms-workorder-page">
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
          <label>工单编号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品编码</label>
          <div class="control">
            <el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>产品名称</label>
          <div class="control">
            <el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>工单类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.orderType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_order_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工单状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_workorder_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>优先级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.priority" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_priority" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>BOM编号</label>
          <div class="control">
            <el-input v-model="queryParams.bomNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工艺路线</label>
          <div class="control">
            <el-input v-model="queryParams.routeNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>产能单元</label>
          <div class="control">
            <el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建日期</label>
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:workorder:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:workorder:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:workorder:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:workorder:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_workorder_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 150)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="工单类型" prop="orderType" key="orderType" :width="colWidth('orderType', 100)" resizable align="center" v-if="columns.orderType.visible">
            <template #default="scope"><span v-if="scope.row.orderType" class="badge violet">{{ orderTypeLabel(scope.row.orderType) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span v-if="scope.row.unit" class="badge gray">{{ unitLabel(scope.row.unit) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="完工数量" prop="finishedQty" key="finishedQty" :width="colWidth('finishedQty', 100)" resizable align="center" v-if="columns.finishedQty.visible" />
          <el-table-column label="合格数量" prop="qualifiedQty" key="qualifiedQty" :width="colWidth('qualifiedQty', 100)" resizable align="center" v-if="columns.qualifiedQty.visible" />
          <el-table-column label="优先级" prop="priority" key="priority" :width="colWidth('priority', 90)" resizable align="center" v-if="columns.priority.visible">
            <template #default="scope"><span v-if="scope.row.priority" class="badge" :class="priorityBadgeClass(scope.row.priority)"><span class="dot"></span>{{ priorityLabel(scope.row.priority) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="工单状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="计划开工" prop="planStart" key="planStart" :width="colWidth('planStart', 160)" resizable align="center" v-if="columns.planStart.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.planStart) }}</span></template>
          </el-table-column>
          <el-table-column label="计划完工" prop="planFinish" key="planFinish" :width="colWidth('planFinish', 160)" resizable align="center" v-if="columns.planFinish.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.planFinish) }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="320" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:workorder:query']">详情</el-button>
              <el-button v-if="scope.row.status === '0'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:workorder:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="success" icon="Promotion" @click="handleRelease(scope.row)" v-hasPermi="['mms:workorder:release']">下达</el-button>
              <el-button v-if="scope.row.status === '1' || scope.row.status === '2'" link type="warning" icon="VideoPause" @click="handlePause(scope.row)" v-hasPermi="['mms:workorder:pause']">暂停</el-button>
              <el-button v-if="scope.row.status === '7'" link type="success" icon="VideoPlay" @click="handleResume(scope.row)" v-hasPermi="['mms:workorder:resume']">恢复</el-button>
              <el-button v-if="scope.row.status === '2' || scope.row.status === '3'" link type="primary" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['mms:workorder:finish']">完工</el-button>
              <el-button v-if="scope.row.status === '4' || scope.row.status === '5'" link type="success" icon="Lock" @click="handleClose(scope.row)" v-hasPermi="['mms:workorder:close']">关闭</el-button>
              <el-button v-if="scope.row.status !== '6' && scope.row.status !== '8'" link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['mms:workorder:cancel']">作废</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改 Dialog ===== -->
    <el-dialog v-model="open" width="984px" append-to-body draggable class="rd-dialog">
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="关联计划号" prop="mpsNo"><el-input v-model="form.mpsNo" readonly placeholder="请选择关联计划，选择后自动带出工单信息" style="width: 100%" @click="openMpsPicker"><template #append><el-button icon="Search" @click="openMpsPicker" /></template><template #suffix><el-icon v-if="form.mpsNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearMps"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="工单类型" prop="orderType"><el-select v-model="form.orderType" placeholder="请选择"><el-option v-for="dict in mms_order_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" readonly placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker"><template #append><el-button icon="Search" @click="openMaterialPicker" /></template><template #suffix><el-icon v-if="form.productCode" class="rd-form-tip" style="cursor:pointer" @click.stop="clearMaterial"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col><el-col :span="12"><el-form-item label="规格型号" prop="specModel"><el-input v-model="form.specModel" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="单位" prop="unit"><el-select v-model="form.unit" placeholder="请选择" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M2 12h20"/></svg></span>生产信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划数量" prop="planQty"><el-input-number v-model="form.planQty" :min="0" :precision="2" placeholder="请输入" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="BOM编号" prop="bomNo"><el-input v-model="form.bomNo" readonly placeholder="请选择BOM" style="width: 100%" @click="openBomPicker"><template #append><el-button icon="Search" @click="openBomPicker" /></template><template #suffix><el-icon v-if="form.bomNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearBom"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工艺路线" prop="routeNo"><el-input v-model="form.routeNo" readonly placeholder="请选择工艺路线" style="width: 100%" @click="openRoutePicker"><template #append><el-button icon="Search" @click="openRoutePicker" /></template><template #suffix><el-icon v-if="form.routeNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearRoute"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="产能单元" prop="resourceName"><el-input v-model="form.resourceName" readonly placeholder="请选择产能单元" style="width: 100%" @click="openResourcePicker"><template #append><el-button icon="Search" @click="openResourcePicker" /></template><template #suffix><el-icon v-if="form.resourceName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearResource"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划开工" prop="planStart"><el-date-picker v-model="form.planStart" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="计划完工" prop="planFinish"><el-date-picker v-model="form.planFinish" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="优先级" prop="priority"><el-select v-model="form.priority" placeholder="请选择"><el-option v-for="dict in mms_priority" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col></el-row>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 工单下达弹窗 ===== -->
    <el-dialog v-model="releaseOpen" width="984px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/></svg></div>
          <span class="rd-detail-header-title">工单下达</span>
          <div class="rd-detail-header-sub" v-if="releaseData.workOrder">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ releaseData.workOrder.workOrderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page" v-loading="releaseLoading">
        <template v-if="releaseData.workOrder">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rc_wo')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>工单信息</div>
            </div>
            <div class="rd-card-body" style="display:block">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ releaseData.workOrder.workOrderNo || '—' }}</div></div>
                <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ releaseData.workOrder.productCode || '—' }}</div></div>
                <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ releaseData.workOrder.productName || '—' }}</div></div>
                <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ releaseData.workOrder.planQty }} {{ unitLabel(releaseData.workOrder.unit) }}</div></div>
                <div class="rd-item"><span class="rd-label">BOM编号</span><div class="rd-value">{{ releaseData.workOrder.bomNo || '—' }}</div></div>
                <div class="rd-item"><span class="rd-label">工艺路线</span><div class="rd-value">{{ releaseData.workOrder.routeNo || '—' }}</div></div>
                <div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ releaseData.workOrder.resourceName || '—' }}</div></div>
                <div class="rd-item"><span class="rd-label">计划开工</span><div class="rd-value">{{ releaseData.workOrder.planStart ? parseTime(releaseData.workOrder.planStart) : '—' }}</div></div>
              </div>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rc_bom')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>BOM明细（下达冻结）</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc_bom }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.rc_bom">
              <el-table :data="releaseData.bomDetails" border size="small" style="width: 100%" max-height="300">
                <el-table-column label="序号" prop="seq" width="60" align="center" />
                <el-table-column label="物料编码" prop="materialCode" min-width="120" />
                <el-table-column label="物料名称" prop="materialName" min-width="140" />
                <el-table-column label="规格型号" prop="specModel" min-width="120" />
                <el-table-column label="单位" width="60" align="center">
                  <template #default="scope">{{ unitLabel(scope.row.unit) }}</template>
                </el-table-column>
                <el-table-column label="单件用量" prop="usageQty" width="100" align="center" />
                <el-table-column label="损耗率(%)" prop="lossRate" width="90" align="center" />
                <el-table-column label="需求数量" prop="demandQty" width="110" align="center">
                  <template #default="scope">
                    <span style="font-weight: 600; color: var(--el-color-primary)">{{ scope.row.demandQty }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="关键料" width="70" align="center">
                  <template #default="scope">
                    <el-tag v-if="scope.row.isKeyMaterial" type="danger" size="small">是</el-tag>
                    <span v-else>—</span>
                  </template>
                </el-table-column>
              </el-table>
              <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px;">
                <el-icon><InfoFilled /></el-icon>
                下达时将复制以上BOM明细为工单快照，后续BOM变更不影响本工单。需求数量 = 计划数量 × 单件用量 × (1 + 损耗率)
              </div>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rc_route')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>工艺工序（下达冻结）</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc_route }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.rc_route">
              <el-table :data="releaseData.routeProcesses" border size="small" style="width: 100%" max-height="300">
                <el-table-column label="顺序" prop="stepSeq" width="70" align="center" />
                <el-table-column label="工序编码" prop="processCode" min-width="120" />
                <el-table-column label="工序名称" prop="processName" min-width="140" />
                <el-table-column label="标准工时(h)" prop="stdTime" width="110" align="center" />
                <el-table-column label="准备时间(h)" prop="prepTime" width="110" align="center" />
                <el-table-column label="关键工序" width="80" align="center">
                  <template #default="scope">
                    <el-tag v-if="scope.row.isKeyProcess" type="danger" size="small">是</el-tag>
                    <span v-else>—</span>
                  </template>
                </el-table-column>
                <el-table-column label="外协" width="70" align="center">
                  <template #default="scope">
                    <el-tag v-if="scope.row.isOutsource" type="warning" size="small">是</el-tag>
                    <span v-else>—</span>
                  </template>
                </el-table-column>
              </el-table>
              <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px;">
                <el-icon><InfoFilled /></el-icon>
                下达时将复制以上工艺工序为工单快照，并自动生成首工序派工单
              </div>
            </div>
          </section>

          <section class="rd-card" v-if="releaseData.warnings && releaseData.warnings.length > 0">
            <div class="rd-card-header" @click="toggleCard('rc_warn')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>校验提示</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc_warn }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.rc_warn">
              <el-alert v-for="(w, i) in releaseData.warnings" :key="i" :title="w" type="warning" :closable="false" show-icon style="margin-bottom: 4px;" />
            </div>
          </section>
        </template>
      </div>
      <template #footer>
        <el-button @click="releaseOpen = false">取 消</el-button>
        <el-button type="primary" :loading="releaseSubmitting" :disabled="releaseData.canRelease === false" @click="submitRelease">确认下达</el-button>
      </template>
    </el-dialog>

    <!-- ===== 暂停原因 Dialog ===== -->
    <el-dialog title="工单暂停" v-model="pauseOpen" width="600px" append-to-body>
      <el-form ref="pauseFormRef" :model="pauseForm" label-width="100px">
        <el-form-item label="工单编号">
          <span>{{ pauseForm.workOrderNo }}</span>
        </el-form-item>
        <el-form-item label="暂停原因" prop="pauseReason">
          <el-input v-model="pauseForm.pauseReason" type="textarea" placeholder="请输入暂停原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitPause">确 定</el-button>
          <el-button @click="pauseOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 关闭/作废 Dialog ===== -->
    <el-dialog :title="closeTitle" v-model="closeOpen" width="600px" append-to-body>
      <el-form ref="closeFormRef" :model="closeForm" label-width="100px">
        <el-form-item label="工单编号">
          <span>{{ closeForm.workOrderNo }}</span>
        </el-form-item>
        <el-form-item :label="closeLabel" prop="remark">
          <el-input v-model="closeForm.remark" type="textarea" :placeholder="'请输入' + closeLabel" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitClose">确 定</el-button>
          <el-button @click="closeOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 工单详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="984px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/><path d="M9 3V1h4v2"/><path d="M9 3v6h4V3"/></svg>
          </div>
          <span class="rd-detail-header-title">工单详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.workOrderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewData.workOrderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="mms_order_type" :value="viewData.orderType" /></div></div>
            <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
            <div class="rd-item"><span class="rd-label">工单状态</span><div class="rd-value"><span v-if="viewData.status" class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span><span v-else class="text-muted">—</span></div></div>
            <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="mms_priority" :value="viewData.priority" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M2 12h20"/></svg></span>生产信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">完工数量</span><div class="rd-value">{{ viewData.finishedQty != null ? viewData.finishedQty : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ viewData.qualifiedQty != null ? viewData.qualifiedQty : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">BOM编号</span><div class="rd-value">{{ viewData.bomNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工艺路线</span><div class="rd-value">{{ viewData.routeNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划开工</span><div class="rd-value">{{ viewData.planStart ? parseTime(viewData.planStart) : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划完工</span><div class="rd-value">{{ viewData.planFinish ? parseTime(viewData.planFinish) : '—' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc_sched')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>排产信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc_sched }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc_sched" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">排产状态</span><div class="rd-value">
              <span v-if="viewData.scheduleStatus" class="badge" :class="scheduleBadgeClass(viewData.scheduleStatus)"><span class="dot"></span>{{ scheduleStatusLabel(viewData.scheduleStatus) }}</span>
              <span v-else class="text-muted">未排产</span>
            </div></div>
            <div class="rd-item"><span class="rd-label">排产单号</span><div class="rd-value">{{ viewData.scheduleNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">排产产能单元</span><div class="rd-value">{{ viewData.scheduleResourceName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">排产计划开始</span><div class="rd-value">{{ viewData.schedulePlanStart ? parseTime(viewData.schedulePlanStart) : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">排产计划结束</span><div class="rd-value">{{ viewData.schedulePlanEnd ? parseTime(viewData.schedulePlanEnd) : '—' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">关联计划号</span><div class="rd-value">{{ viewData.mpsNo || '—' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div>
          </div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 审核日志 Dialog ===== -->
    <el-dialog title="工单审核日志" v-model="logOpen" width="840px" append-to-body>
      <el-table :data="auditLogList" border>
        <el-table-column label="操作" align="center" prop="auditAction" width="100" />
        <el-table-column label="操作人" align="center" prop="auditBy" width="100" />
        <el-table-column label="操作时间" align="center" prop="auditTime" width="160">
          <template #default="scope"><span>{{ parseTime(scope.row.auditTime) }}</span></template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="auditRemark" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <!-- ===== 物料选择器 ===== -->
    <material-picker ref="materialPickerRef" title="选择产品物料" @confirm="onMaterialPickerConfirm" />

    <!-- ===== BOM选择器弹窗 ===== -->
    <el-dialog v-model="bomPickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">选择BOM</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="bomPickerQuery.bomNo" placeholder="BOM编号" clearable size="small" style="width:180px" @keyup.enter="handleBomPickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="bomPickerQuery.bomName" placeholder="BOM名称" clearable size="small" style="width:180px" @keyup.enter="handleBomPickerQuery" />
        <el-button type="primary" plain icon="Search" size="small" @click="handleBomPickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetBomPickerQuery">重置</el-button>
      </div>
      <el-table ref="bomPickerTableRef" v-loading="bomPickerLoading" :data="bomPickerList" highlight-current-row @row-click="onBomRowClick" @row-dblclick="onBomRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="bomPickerSelectedId" :value="row.bomId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="BOM编号" prop="bomNo" width="140" show-overflow-tooltip />
        <el-table-column label="BOM名称" prop="bomName" min-width="180" show-overflow-tooltip />
        <el-table-column label="产品编码" prop="productCode" width="130" show-overflow-tooltip />
        <el-table-column label="产品名称" prop="productName" min-width="150" show-overflow-tooltip />
        <el-table-column label="版本" prop="version" width="70" align="center" />
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="bomPickerQuery.pageNum" v-model:page-size="bomPickerQuery.pageSize" :total="bomPickerTotal" layout="total, prev, pager, next" small @current-change="getBomPickerList" />
      </div>
      <div v-if="bomPickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出BOM编号</div>
      <template #footer><el-button @click="bomPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleBomPickerConfirm" :disabled="!bomPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 工艺路线选择器弹窗 ===== -->
    <el-dialog v-model="routePickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></div>
          <span class="rd-detail-header-title">选择工艺路线</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="routePickerQuery.routeNo" placeholder="路线编号" clearable size="small" style="width:180px" @keyup.enter="handleRoutePickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="routePickerQuery.routeName" placeholder="路线名称" clearable size="small" style="width:180px" @keyup.enter="handleRoutePickerQuery" />
        <el-button type="primary" plain icon="Search" size="small" @click="handleRoutePickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetRoutePickerQuery">重置</el-button>
      </div>
      <el-table ref="routePickerTableRef" v-loading="routePickerLoading" :data="routePickerList" highlight-current-row @row-click="onRouteRowClick" @row-dblclick="onRouteRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="routePickerSelectedId" :value="row.routeId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="路线编号" prop="routeNo" width="140" show-overflow-tooltip />
        <el-table-column label="路线名称" prop="routeName" min-width="180" show-overflow-tooltip />
        <el-table-column label="产品编码" prop="productCode" width="130" show-overflow-tooltip />
        <el-table-column label="产品名称" prop="productName" min-width="150" show-overflow-tooltip />
        <el-table-column label="版本" prop="version" width="70" align="center" />
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="routePickerQuery.pageNum" v-model:page-size="routePickerQuery.pageSize" :total="routePickerTotal" layout="total, prev, pager, next" small @current-change="getRoutePickerList" />
      </div>
      <div v-if="routePickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出工艺路线</div>
      <template #footer><el-button @click="routePickerOpen = false">取 消</el-button><el-button type="primary" @click="handleRoutePickerConfirm" :disabled="!routePickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 产能单元选择器弹窗 ===== -->
    <el-dialog v-model="resourcePickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">选择产能单元</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="resourcePickerQuery.resourceName" placeholder="产能单元名称" clearable size="small" style="width:200px" @keyup.enter="handleResourcePickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain icon="Search" size="small" @click="handleResourcePickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetResourcePickerQuery">重置</el-button>
      </div>
      <el-table ref="resourcePickerTableRef" v-loading="resourcePickerLoading" :data="resourcePickerList" highlight-current-row @row-click="onResourceRowClick" @row-dblclick="onResourceRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="resourcePickerSelectedId" :value="row.resourceId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="资源编码" prop="resourceCode" width="130" show-overflow-tooltip />
        <el-table-column label="产能单元" prop="resourceName" min-width="150" show-overflow-tooltip />
        <el-table-column label="产线" prop="lineName" width="100" show-overflow-tooltip />
        <el-table-column label="车间" prop="workshopName" width="100" show-overflow-tooltip />
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="resourcePickerQuery.pageNum" v-model:page-size="resourcePickerQuery.pageSize" :total="resourcePickerTotal" layout="total, prev, pager, next" small @current-change="getResourcePickerList" />
      </div>
      <div v-if="resourcePickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出产能单元</div>
      <template #footer><el-button @click="resourcePickerOpen = false">取 消</el-button><el-button type="primary" @click="handleResourcePickerConfirm" :disabled="!resourcePickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 关联计划选择器弹窗 ===== -->
    <el-dialog v-model="mpsPickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/><path d="M9 3V1h4v2"/><path d="M9 3v6h4V3"/></svg></div>
          <span class="rd-detail-header-title">选择关联计划</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="mpsPickerQuery.mpsNo" placeholder="计划编号" clearable size="small" style="width:180px" @keyup.enter="handleMpsPickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="mpsPickerQuery.productCode" placeholder="产品编码" clearable size="small" style="width:180px" @keyup.enter="handleMpsPickerQuery" />
        <el-button type="primary" plain icon="Search" size="small" @click="handleMpsPickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetMpsPickerQuery">重置</el-button>
      </div>
      <el-table ref="mpsPickerTableRef" v-loading="mpsPickerLoading" :data="mpsPickerList" highlight-current-row @row-click="onMpsRowClick" @row-dblclick="onMpsRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="mpsPickerSelectedId" :value="row.mpsId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="计划编号" prop="mpsNo" width="140" show-overflow-tooltip />
        <el-table-column label="产品编码" prop="productCode" width="130" show-overflow-tooltip />
        <el-table-column label="产品名称" prop="productName" min-width="150" show-overflow-tooltip />
        <el-table-column label="计划数量" prop="planQty" width="100" align="center" />
        <el-table-column label="产能单元" prop="resourceName" min-width="120" show-overflow-tooltip />
        <el-table-column label="计划开始" prop="periodStart" width="110" align="center"><template #default="scope"><span>{{ scope.row.periodStart ? parseTime(scope.row.periodStart, '{y}-{m}-{d}') : '—' }}</span></template></el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="mpsPickerQuery.pageNum" v-model:page-size="mpsPickerQuery.pageSize" :total="mpsPickerTotal" layout="total, prev, pager, next" small @current-change="getMpsPickerList" />
      </div>
      <div v-if="mpsPickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出关联计划号</div>
      <template #footer><el-button @click="mpsPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleMpsPickerConfirm" :disabled="!mpsPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="工单管理业务操作说明" width="984px" append-to-body>
      <div class="status-help-content">
        <!-- 一、工单释义 -->
        <h4>一、工单释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是工单？</div>
          <div class="highlight-card-body">
            <strong>工单（Work Order）</strong>是生产管控中用于安排、执行和跟踪生产任务的核心单据。工单关联产品、BOM、工艺路线和产能单元，定义计划数量、计划开工/完工时间，通过下达→生产→完工的全生命周期管理，实现生产过程的数字化管控和进度追踪。<br/><br/>
            工单是<strong>MES（制造执行系统）</strong>的核心数据载体，向上对接主生产计划（MPS）和销售需求，向下驱动物料领料、工序流转、质量检验和完工入库，满足精益生产和离散制造对生产过程可追溯、可量化、可管控的要求。
          </div>
        </div>

        <!-- 二、工单状态流转图 -->
        <h4>二、工单状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「下达」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已下达</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">开始生产</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">生产中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「完工」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待完工</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">确认完工</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完工</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-tag size="small" type="danger">或</el-tag>
            <el-tag type="primary">已下达/生产中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「作废」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
            <el-tag size="small" type="info">异常终止</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="primary">已下达/生产中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「暂停」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">已暂停</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「恢复」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">生产中</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">工单新建后的初始状态。可修改工单信息、下达至生产线或作废。点击「下达」将工单下发至车间，状态变为已下达</el-descriptions-item>
          <el-descriptions-item label="已下达">工单已下达至生产线，等待开始生产。可暂停或开始生产。进入生产后状态自动变为生产中</el-descriptions-item>
          <el-descriptions-item label="生产中">工单正在生产执行中。可暂停（需填写暂停原因）或完工。暂停后可恢复继续生产</el-descriptions-item>
          <el-descriptions-item label="待完工">生产基本完成，等待最终完工确认。可执行完工操作，系统自动记录完工数量和合格数量</el-descriptions-item>
          <el-descriptions-item label="已完工">工单已完工，完工数量已确认。可关闭工单或查看详情。已完工工单可关闭</el-descriptions-item>
          <el-descriptions-item label="已关闭">工单已关闭，不可再操作。关闭表示工单生命周期结束</el-descriptions-item>
          <el-descriptions-item label="已作废">工单因异常情况被作废，不再执行。除已作废和已关闭外的工单均可作废</el-descriptions-item>
          <el-descriptions-item label="已暂停">工单已暂停生产，可恢复继续生产。暂停时需填写暂停原因</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>工单编号：</strong>工单的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>工单类型：</strong>包括生产工单、返工工单、试产工单等<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>产品编码：</strong>待生产产品的编码<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>产品名称：</strong>待生产产品的名称<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>规格型号：</strong>产品的规格型号信息</p>
            <p>• <strong>单位：</strong>产品的计量单位，如个、kg、m等</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">生产信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>计划数量：</strong>本工单计划生产的数量<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>BOM编号：</strong>关联的物料清单编号，用于确定物料需求</p>
            <p>• <strong>工艺路线：</strong>关联的工艺路线编号，用于确定加工工序路径</p>
            <p>• <strong>产能单元：</strong>指派生产的产能单元（设备/产线）</p>
            <p>• <strong>计划开工：</strong>计划开始生产的时间</p>
            <p>• <strong>计划完工：</strong>计划完成生产的时间</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">其他信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>优先级：</strong>工单的生产优先级，分为高、中、低，影响排产顺序</p>
            <p>• <strong>关联计划号：</strong>关联的主生产计划（MPS）编号</p>
            <p>• <strong>备注：</strong>工单的补充说明信息</p>
          </div>
        </div>

        <!-- 五、工单生命周期管控 -->
        <h4>五、工单生命周期管控</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是工单生命周期管控？</div>
          <div class="highlight-card-body">
            <strong>工单生命周期管控</strong>是生产管控的核心机制，通过状态流转实现工单从创建到关闭的全过程管理。每个状态对应特定的可执行操作，确保生产过程有序可控、可追溯。工单的暂停/恢复机制支持应对设备故障、物料短缺等异常情况，完工确认机制确保生产数量和质量数据的准确归集。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">异常处理规则</div>
          <div class="highlight-card-body">
            <p>1. <strong>暂停处理：</strong>生产中如遇设备故障、物料短缺等异常，可暂停工单并填写暂停原因，排除故障后恢复生产</p>
            <p>2. <strong>作废处理：</strong>因订单取消、工艺变更等原因无法继续生产的工单，可作废终止，作废后不可恢复</p>
            <p>3. <strong>关闭处理：</strong>已完工的工单确认无误后可关闭，关闭表示工单生命周期结束，数据归档</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>暂停/作废/关闭操作均需填写原因，系统自动记录操作日志，确保全流程可追溯</p>
          </div>
        </div>

        <!-- 六、业务操作流程 -->
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建工单：</strong>点击「新增」创建工单，填写工单类型、产品信息、计划数量、关联BOM和工艺路线，保存后工单编号自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>下达工单：</strong>草稿状态下点击「下达」将工单下发至生产线，状态变为已下达，车间可开始排产
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>暂停/恢复：</strong>生产中如需暂停，点击「暂停」并填写原因。暂停后点击「恢复」继续生产
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>完工确认：</strong>生产完成后点击「完工」，系统自动记录完工数量和合格数量，状态变为已完工
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>已完工的工单可点击「详情」查看完整信息，包括生产数据、审核日志等
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>作废/关闭：</strong>异常工单可「作废」终止；已完工工单确认无误后可「关闭」归档
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WorkOrder">
import { listWorkOrder, getWorkOrder, addWorkOrder, updateWorkOrder, delWorkOrder,
         getReleasePreview, releaseWorkOrder, pauseWorkOrder, resumeWorkOrder, finishWorkOrder,
         closeWorkOrder, cancelWorkOrder, getWorkOrderAuditLog } from "@/api/mms/workorder";
import { listBom } from "@/api/mms/bom";
import { listRoute } from "@/api/mms/route";
import { listResource } from "@/api/mms/resource";
import { listMps } from "@/api/mms/mps";
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, QuestionFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_order_type, mms_priority, mms_workorder_status, wms_unit, mms_schedule_status } = proxy.useDict("mms_order_type", "mms_priority", "mms_workorder_status", "wms_unit", "mms_schedule_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_workorder_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2","vc_sched","rc_bom","rc_route","rc_warn"])

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
const pauseOpen = ref(false);
const closeOpen = ref(false);
const closeTitle = ref("");
const closeLabel = ref("");
const logOpen = ref(false);
const auditLogList = ref([]);

// 下达弹窗
const releaseOpen = ref(false);
const releaseLoading = ref(false);
const releaseData = ref({});
const releaseSubmitting = ref(false);

// 状态标签列表
const statusTabList = computed(() => {
  if (!mms_workorder_status.value) return [];
  return mms_workorder_status.value.map(d => ({ label: d.label, value: d.value }));
});

// 列显隐配置
const defaultColumns = {
  workOrderNo: { label: '工单编号', visible: true },
  orderType: { label: '工单类型', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  planQty: { label: '计划数量', visible: true },
  finishedQty: { label: '完工数量', visible: true },
  qualifiedQty: { label: '合格数量', visible: true },
  priority: { label: '优先级', visible: true },
  status: { label: '工单状态', visible: true },
  planStart: { label: '计划开工', visible: true },
  planFinish: { label: '计划完工', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_workorder_columns')
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

// 当前已选筛选条件数
const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.value.workOrderNo) count++;
  if (queryParams.value.productCode) count++;
  if (queryParams.value.productName) count++;
  if (queryParams.value.orderType) count++;
  if (queryParams.value.status) count++;
  if (queryParams.value.priority) count++;
  if (queryParams.value.bomNo) count++;
  if (queryParams.value.routeNo) count++;
  if (queryParams.value.resourceName) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    workOrderNo: undefined,
    productCode: undefined,
    productName: undefined,
    orderType: undefined,
    priority: undefined,
    status: undefined,
    bomNo: undefined,
    routeNo: undefined,
    resourceName: undefined,
    params: {}
  },
  rules: {
    orderType: [{ required: true, message: "请选择工单类型", trigger: "change" }],
    productCode: [{ required: true, message: "请选择产品编码", trigger: "change" }],
    productName: [{ required: true, message: "请选择产品", trigger: "change" }],
    planQty: [{ required: true, message: "请输入计划数量", trigger: "blur" }],
    bomNo: [{ required: true, message: "请选择BOM", trigger: "change" }],
    routeNo: [{ required: true, message: "请选择工艺路线", trigger: "change" }]
  },
  pauseForm: {},
  closeForm: {}
});

const { queryParams, form, rules, pauseForm, closeForm } = toRefs(data);

function getList() {
  loading.value = true;
  listWorkOrder(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    loadStatusCounts();
  });
}

function loadStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 };
  if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo;
  if (queryParams.value.productCode) baseQuery.productCode = queryParams.value.productCode;
  if (queryParams.value.productName) baseQuery.productName = queryParams.value.productName;
  if (queryParams.value.orderType) baseQuery.orderType = queryParams.value.orderType;
  if (queryParams.value.priority) baseQuery.priority = queryParams.value.priority;
  if (queryParams.value.bomNo) baseQuery.bomNo = queryParams.value.bomNo;
  if (queryParams.value.routeNo) baseQuery.routeNo = queryParams.value.routeNo;
  if (queryParams.value.resourceName) baseQuery.resourceName = queryParams.value.resourceName;
  listWorkOrder(proxy.addDateRange(baseQuery, dateRange.value)).then(res => {
    const counts = { all: res.total };
    if (mms_workorder_status.value) {
      mms_workorder_status.value.forEach(d => { counts[d.value] = 0; });
      (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; });
    }
    statusCounts.value = counts;
  }).catch(() => {});
}

function handleQuery() {
  showAdvanced.value = false;
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value.workOrderNo = undefined;
  queryParams.value.productCode = undefined;
  queryParams.value.productName = undefined;
  queryParams.value.orderType = undefined;
  queryParams.value.priority = undefined;
  queryParams.value.status = undefined;
  queryParams.value.bomNo = undefined;
  queryParams.value.routeNo = undefined;
  queryParams.value.resourceName = undefined;
  dateRange.value = [];
  queryParams.value.params = {};
  activeStatusTab.value = 'all';
  handleQuery();
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status;
  queryParams.value.status = status === "all" ? undefined : status;
  handleQuery();
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop;
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc';
  } else {
    queryParams.value.params.orderByColumn = undefined;
    queryParams.value.params.isAsc = undefined;
  }
  getList();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.workOrderId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    workOrderNo: undefined,
    orderType: undefined,
    mpsId: undefined,
    mpsNo: undefined,
    demandNo: undefined,
    productId: undefined,
    productCode: undefined,
    productName: undefined,
    specModel: undefined,
    unit: undefined,
    planQty: undefined,
    bomId: undefined,
    bomNo: undefined,
    routeId: undefined,
    routeNo: undefined,
    resourceId: undefined,
    resourceName: undefined,
    planStart: undefined,
    planFinish: undefined,
    priority: undefined,
    status: undefined,
    remark: undefined,
  };
  proxy.resetForm("formRef");
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增工单";
}

function handleUpdate(row) {
  reset();
  const id = row.workOrderId || ids.value[0];
  getWorkOrder(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改工单";
  });
}

function handleView(row) {
  getWorkOrder(row.workOrderId).then(response => {
    viewData.value = response.data;
    viewOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.workOrderId != null) {
        updateWorkOrder(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addWorkOrder(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function cancel() {
  open.value = false;
  reset();
}

function handleDelete(row) {
  const delIds = row.workOrderId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的工单？').then(function() {
    return delWorkOrder(delIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleExport() {
  proxy.download("mms/workorder/export", { ...queryParams.value }, `workorder_${new Date().getTime()}.xlsx`);
}

// ===== 业务操作 =====

function handleRelease(row) {
  releaseLoading.value = true;
  releaseOpen.value = true;
  releaseData.value = {};
  getReleasePreview(row.workOrderId).then(res => {
    releaseData.value = res.data;
    releaseLoading.value = false;
  }).catch(() => {
    releaseLoading.value = false;
  });
}

function submitRelease() {
  const wo = releaseData.value.workOrder;
  if (!wo) return;
  releaseSubmitting.value = true;
  releaseWorkOrder(wo.workOrderId).then(() => {
    releaseOpen.value = false;
    getList();
    proxy.$modal.msgSuccess("下达成功");
  }).finally(() => {
    releaseSubmitting.value = false;
  });
}

function handlePause(row) {
  pauseForm.value = { workOrderId: row.workOrderId, workOrderNo: row.workOrderNo, pauseReason: "" };
  pauseOpen.value = true;
}

function submitPause() {
  pauseWorkOrder(pauseForm.value.workOrderId, pauseForm.value.pauseReason).then(() => {
    pauseOpen.value = false;
    getList();
    proxy.$modal.msgSuccess("暂停成功");
  });
}

function handleResume(row) {
  proxy.$modal.confirm('是否确认恢复工单"' + row.workOrderNo + '"？').then(function() {
    return resumeWorkOrder(row.workOrderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("恢复成功");
  }).catch(() => {});
}

function handleFinish(row) {
  proxy.$modal.confirm('是否确认完工工单"' + row.workOrderNo + '"？').then(function() {
    return finishWorkOrder(row.workOrderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("完工成功");
  }).catch(() => {});
}

function handleClose(row) {
  closeTitle.value = "工单关闭";
  closeLabel.value = "关闭备注";
  closeForm.value = { workOrderId: row.workOrderId, workOrderNo: row.workOrderNo, remark: "", type: "close" };
  closeOpen.value = true;
}

function handleCancel(row) {
  closeTitle.value = "工单作废";
  closeLabel.value = "作废原因";
  closeForm.value = { workOrderId: row.workOrderId, workOrderNo: row.workOrderNo, remark: "", type: "cancel" };
  closeOpen.value = true;
}

function submitClose() {
  if (closeForm.value.type === "close") {
    closeWorkOrder(closeForm.value.workOrderId, closeForm.value.remark).then(() => {
      closeOpen.value = false;
      getList();
      proxy.$modal.msgSuccess("关闭成功");
    });
  } else {
    cancelWorkOrder(closeForm.value.workOrderId, closeForm.value.remark).then(() => {
      closeOpen.value = false;
      getList();
      proxy.$modal.msgSuccess("作废成功");
    });
  }
}

function handleAuditLog(row) {
  getWorkOrderAuditLog(row.workOrderId).then(response => {
    auditLogList.value = response.data;
    logOpen.value = true;
  });
}

// ===== 字典辅助函数 =====
function orderTypeLabel(type) {
  const item = mms_order_type.value.find(d => d.value == type);
  return item ? item.label : '—';
}

function unitLabel(unit) {
  const item = wms_unit.value.find(d => d.value == unit);
  return item ? item.label : '—';
}

function statusLabel(status) {
  const item = mms_workorder_status.value.find(d => d.value == status);
  return item ? item.label : '—';
}

function badgeClass(status) {
  const map = {
    '0': 'amber',    // 草稿
    '1': 'blue',     // 已下达
    '2': 'blue',     // 生产中
    '3': 'amber',    // 待完工
    '4': 'green',    // 已完工
    '5': 'gray',     // 已关闭
    '6': 'gray',     // 已作废
    '7': 'amber',    // 已暂停
    '8': 'red'       // 异常
  };
  return map[status] || 'gray';
}

function scheduleStatusLabel(status) {
  const item = mms_schedule_status.value.find(d => d.value == status);
  return item ? item.label : '—';
}

function scheduleBadgeClass(status) {
  const item = mms_schedule_status.value.find(d => d.value == status);
  const listClass = item ? item.listClass : '';
  const map = { 'success': 'green', 'danger': 'red', 'info': 'gray', 'warning': 'amber', 'primary': 'blue' };
  return map[listClass] || 'gray';
}

function statusTabClass(value) {
  const map = {
    '0': 'tab-draft',
    '1': 'tab-audit',
    '2': 'tab-audit',
    '3': 'tab-draft',
    '4': 'tab-done',
    '5': 'tab-void',
    '6': 'tab-void',
    '7': 'tab-draft',
    '8': 'tab-reject'
  };
  return map[value] || '';
}

function priorityLabel(priority) {
  const item = mms_priority.value.find(d => d.value == priority);
  return item ? item.label : '—';
}

function priorityBadgeClass(priority) {
  const map = { '0': 'red', '1': 'amber', '2': 'blue' };
  return map[priority] || 'gray';
}

// ===== 物料选择器 =====
const materialPickerRef = ref()
function openMaterialPicker() { materialPickerRef.value.open(form.value.productId) }
function onMaterialPickerConfirm(material) {
  // 如果切换了产品，联动清空已选的BOM和工艺路线（因为原关联已失效）
  const productChanged = form.value.productId && form.value.productId !== material.materialId
  if (productChanged) {
    form.value.bomId = undefined
    form.value.bomNo = undefined
    form.value.routeId = undefined
    form.value.routeNo = undefined
  }
  form.value.productId = material.materialId
  form.value.productCode = material.materialCode
  form.value.productName = material.materialName
  form.value.specModel = material.specModel
  if (material.unit) form.value.unit = material.unit
  // 产品确定后，自动查询已发布的BOM和已审核的工艺路线，如果各只有一个则自动带出
  autoFillBomAndRoute(material.materialId, productChanged)
}
function clearMaterial() {
  form.value.productId = undefined
  form.value.productCode = undefined
  form.value.productName = undefined
  form.value.specModel = undefined
  // 联动清空BOM和工艺路线
  form.value.bomId = undefined
  form.value.bomNo = undefined
  form.value.routeId = undefined
  form.value.routeNo = undefined
}

// ===== BOM选择器 =====
const bomPickerOpen = ref(false)
const bomPickerLoading = ref(false)
const bomPickerList = ref([])
const bomPickerTotal = ref(0)
const bomPickerSelectedId = ref(null)
const bomPickerSelectedRow = ref(null)
const bomPickerTableRef = ref()
const bomPickerQuery = reactive({ pageNum: 1, pageSize: 10, bomNo: undefined, bomName: undefined, status: '1', productId: undefined })

function openBomPicker() {
  if (!form.value.productId) {
    proxy.$modal.msgWarning('请先选择产品，再选择BOM')
    return
  }
  bomPickerOpen.value = true
  bomPickerSelectedId.value = null
  bomPickerSelectedRow.value = null
  bomPickerQuery.pageNum = 1
  bomPickerQuery.bomNo = undefined
  bomPickerQuery.bomName = undefined
  bomPickerQuery.productId = form.value.productId
  getBomPickerList()
}
function getBomPickerList() {
  bomPickerLoading.value = true
  listBom(bomPickerQuery).then(res => {
    bomPickerList.value = res.rows
    bomPickerTotal.value = res.total
    bomPickerLoading.value = false
  }).catch(() => { bomPickerLoading.value = false })
}
function handleBomPickerQuery() { bomPickerQuery.pageNum = 1; getBomPickerList() }
function resetBomPickerQuery() { bomPickerQuery.bomNo = undefined; bomPickerQuery.bomName = undefined; handleBomPickerQuery() }
// 注意：resetBomPickerQuery 不清除 productId，因为它来自工单产品，是固定过滤条件
function onBomRowClick(row) { bomPickerSelectedId.value = row.bomId; bomPickerSelectedRow.value = row }
function onBomRowDblClick(row) { onBomRowClick(row); handleBomPickerConfirm() }
function handleBomPickerConfirm() {
  if (!bomPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择BOM'); return }
  form.value.bomId = bomPickerSelectedRow.value.bomId
  form.value.bomNo = bomPickerSelectedRow.value.bomNo
  bomPickerOpen.value = false
}
function clearBom() { form.value.bomId = undefined; form.value.bomNo = undefined }

// ===== 自动带出BOM和工艺路线 =====
// 当产品确定后，自动查询该产品下已发布的BOM和已审核的工艺路线
// 如果各只有一个，则自动带出；如果有多个，提示用户手动选择
function autoFillBomAndRoute(productId, productChanged) {
  if (!productId) return
  // 仅在产品变更或BOM/工艺路线未选择时才自动查询
  const needBom = !form.value.bomId
  const needRoute = !form.value.routeId
  if (!needBom && !needRoute) return

  // 查询已发布的BOM（status=1 已发布）
  const bomPromise = needBom
    ? listBom({ pageNum: 1, pageSize: 99, status: '1', productId: productId })
    : Promise.resolve(null)
  // 查询已审核的工艺路线（status=2 已审核）
  const routePromise = needRoute
    ? listRoute({ pageNum: 1, pageSize: 99, status: '2', productId: productId })
    : Promise.resolve(null)

  Promise.all([bomPromise, routePromise]).then(([bomRes, routeRes]) => {
    const messages = []

    // 处理BOM自动带出
    if (bomRes) {
      const bomList = bomRes.rows || []
      if (bomList.length === 1) {
        form.value.bomId = bomList[0].bomId
        form.value.bomNo = bomList[0].bomNo
        messages.push('BOM已自动带出')
      } else if (bomList.length > 1) {
        messages.push(`该产品有${bomList.length}条已发布BOM，请手动选择`)
      } else {
        messages.push('该产品暂无已发布的BOM')
      }
    }

    // 处理工艺路线自动带出
    if (routeRes) {
      const routeList = routeRes.rows || []
      if (routeList.length === 1) {
        form.value.routeId = routeList[0].routeId
        form.value.routeNo = routeList[0].routeNo
        messages.push('工艺路线已自动带出')
      } else if (routeList.length > 1) {
        messages.push(`该产品有${routeList.length}条已审核工艺路线，请手动选择`)
      } else {
        messages.push('该产品暂无已审核的工艺路线')
      }
    }

    // 提示用户自动带出结果
    if (messages.length > 0) {
      const hasAutoFill = messages.some(m => m.includes('已自动带出'))
      const hasManualNeed = messages.some(m => m.includes('请手动选择'))
      const hasMissing = messages.some(m => m.includes('暂无'))
      if (hasManualNeed || hasMissing) {
        proxy.$modal.msgWarning(messages.join('；'))
      } else if (hasAutoFill) {
        proxy.$modal.msgSuccess(messages.join('；'))
      }
    }
  }).catch(() => {
    // 查询失败时静默处理，不影响用户操作
  })
}

// ===== 工艺路线选择器 =====
const routePickerOpen = ref(false)
const routePickerLoading = ref(false)
const routePickerList = ref([])
const routePickerTotal = ref(0)
const routePickerSelectedId = ref(null)
const routePickerSelectedRow = ref(null)
const routePickerTableRef = ref()
const routePickerQuery = reactive({ pageNum: 1, pageSize: 10, routeNo: undefined, routeName: undefined, status: '2', productId: undefined })

function openRoutePicker() {
  if (!form.value.productId) {
    proxy.$modal.msgWarning('请先选择产品，再选择工艺路线')
    return
  }
  routePickerOpen.value = true
  routePickerSelectedId.value = null
  routePickerSelectedRow.value = null
  routePickerQuery.pageNum = 1
  routePickerQuery.routeNo = undefined
  routePickerQuery.routeName = undefined
  routePickerQuery.productId = form.value.productId
  getRoutePickerList()
}
function getRoutePickerList() {
  routePickerLoading.value = true
  listRoute(routePickerQuery).then(res => {
    routePickerList.value = res.rows
    routePickerTotal.value = res.total
    routePickerLoading.value = false
  }).catch(() => { routePickerLoading.value = false })
}
function handleRoutePickerQuery() { routePickerQuery.pageNum = 1; getRoutePickerList() }
function resetRoutePickerQuery() { routePickerQuery.routeNo = undefined; routePickerQuery.routeName = undefined; handleRoutePickerQuery() }
// 注意：resetRoutePickerQuery 不清除 productId，因为它来自工单产品，是固定过滤条件
function onRouteRowClick(row) { routePickerSelectedId.value = row.routeId; routePickerSelectedRow.value = row }
function onRouteRowDblClick(row) { onRouteRowClick(row); handleRoutePickerConfirm() }
function handleRoutePickerConfirm() {
  if (!routePickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工艺路线'); return }
  form.value.routeId = routePickerSelectedRow.value.routeId
  form.value.routeNo = routePickerSelectedRow.value.routeNo
  routePickerOpen.value = false
}
function clearRoute() { form.value.routeId = undefined; form.value.routeNo = undefined }

// ===== 产能单元选择器 =====
const resourcePickerOpen = ref(false)
const resourcePickerLoading = ref(false)
const resourcePickerList = ref([])
const resourcePickerTotal = ref(0)
const resourcePickerSelectedId = ref(null)
const resourcePickerSelectedRow = ref(null)
const resourcePickerTableRef = ref()
const resourcePickerQuery = reactive({ pageNum: 1, pageSize: 10, resourceName: undefined, status: '0' })

function openResourcePicker() {
  resourcePickerOpen.value = true
  resourcePickerSelectedId.value = null
  resourcePickerSelectedRow.value = null
  resourcePickerQuery.pageNum = 1
  resourcePickerQuery.resourceName = undefined
  getResourcePickerList()
}
function getResourcePickerList() {
  resourcePickerLoading.value = true
  listResource(resourcePickerQuery).then(res => {
    resourcePickerList.value = res.rows
    resourcePickerTotal.value = res.total
    resourcePickerLoading.value = false
  }).catch(() => { resourcePickerLoading.value = false })
}
function handleResourcePickerQuery() { resourcePickerQuery.pageNum = 1; getResourcePickerList() }
function resetResourcePickerQuery() { resourcePickerQuery.resourceName = undefined; handleResourcePickerQuery() }
function onResourceRowClick(row) { resourcePickerSelectedId.value = row.resourceId; resourcePickerSelectedRow.value = row }
function onResourceRowDblClick(row) { onResourceRowClick(row); handleResourcePickerConfirm() }
function handleResourcePickerConfirm() {
  if (!resourcePickerSelectedId.value) { proxy.$modal.msgWarning('请先选择产能单元'); return }
  form.value.resourceId = resourcePickerSelectedRow.value.resourceId
  form.value.resourceName = resourcePickerSelectedRow.value.resourceName
  resourcePickerOpen.value = false
}
function clearResource() {
  form.value.resourceId = undefined
  form.value.resourceName = undefined
}

// ===== 关联计划选择器 =====
const mpsPickerOpen = ref(false)
const mpsPickerLoading = ref(false)
const mpsPickerList = ref([])
const mpsPickerTotal = ref(0)
const mpsPickerSelectedId = ref(null)
const mpsPickerSelectedRow = ref(null)
const mpsPickerTableRef = ref()
const mpsPickerQuery = reactive({ pageNum: 1, pageSize: 10, mpsNo: undefined, productCode: undefined, status: '3' })

function openMpsPicker() {
  mpsPickerOpen.value = true
  mpsPickerSelectedId.value = null
  mpsPickerSelectedRow.value = null
  mpsPickerQuery.pageNum = 1
  mpsPickerQuery.mpsNo = undefined
  mpsPickerQuery.productCode = undefined
  getMpsPickerList()
}
function getMpsPickerList() {
  mpsPickerLoading.value = true
  listMps(mpsPickerQuery).then(res => {
    mpsPickerList.value = res.rows
    mpsPickerTotal.value = res.total
    mpsPickerLoading.value = false
  }).catch(() => { mpsPickerLoading.value = false })
}
function handleMpsPickerQuery() { mpsPickerQuery.pageNum = 1; getMpsPickerList() }
function resetMpsPickerQuery() { mpsPickerQuery.mpsNo = undefined; mpsPickerQuery.productCode = undefined; handleMpsPickerQuery() }
function onMpsRowClick(row) { mpsPickerSelectedId.value = row.mpsId; mpsPickerSelectedRow.value = row }
function onMpsRowDblClick(row) { onMpsRowClick(row); handleMpsPickerConfirm() }
function handleMpsPickerConfirm() {
  if (!mpsPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择关联计划'); return }
  const row = mpsPickerSelectedRow.value
  form.value.mpsId = row.mpsId
  form.value.mpsNo = row.mpsNo
  // 选择计划后自动带出工单信息
  // 如果切换了产品，联动清空已选的BOM和工艺路线（因为原关联已失效）
  const productChanged = form.value.productId && row.productId && form.value.productId !== row.productId
  if (productChanged) {
    form.value.bomId = undefined
    form.value.bomNo = undefined
    form.value.routeId = undefined
    form.value.routeNo = undefined
  }
  if (row.productId) form.value.productId = row.productId
  if (row.productCode) form.value.productCode = row.productCode
  if (row.productName) form.value.productName = row.productName
  if (row.specModel) form.value.specModel = row.specModel
  if (row.unit) form.value.unit = row.unit
  if (row.planQty != null) form.value.planQty = row.planQty
  if (row.resourceId) form.value.resourceId = row.resourceId
  if (row.resourceName) form.value.resourceName = row.resourceName
  if (row.priority) form.value.priority = row.priority
  if (row.demandNo) form.value.demandNo = row.demandNo
  if (row.periodStart) form.value.planStart = row.periodStart + ' 00:00:00'
  if (row.periodEnd) form.value.planFinish = row.periodEnd + ' 23:59:59'
  mpsPickerOpen.value = false
  // 产品确定后，自动查询已发布的BOM和已审核的工艺路线，如果各只有一个则自动带出
  if (row.productId) {
    autoFillBomAndRoute(row.productId, productChanged)
  } else {
    proxy.$modal.msgSuccess('已带出计划关联信息，请确认并补充BOM、工艺路线等')
  }
}
function clearMps() {
  form.value.mpsId = undefined
  form.value.mpsNo = undefined
}

getList();
</script>

<style scoped>
.mms-workorder-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.mms-workorder-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-workorder-page .filter-card { padding:14px 20px 16px; }
.mms-workorder-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-workorder-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-workorder-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-workorder-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-workorder-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-workorder-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-workorder-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-workorder-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-workorder-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-workorder-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-workorder-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-workorder-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-workorder-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-workorder-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-workorder-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-workorder-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-workorder-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-workorder-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-workorder-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-workorder-page .field .control :deep(.el-select) { width:100%; }
.mms-workorder-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-workorder-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-workorder-page .toolbar .left, .mms-workorder-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-workorder-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-workorder-page .table-wrap { overflow-x:auto; }
.mms-workorder-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-workorder-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-workorder-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-workorder-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-workorder-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-workorder-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-workorder-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-workorder-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-workorder-page .badge.amber .dot { background:var(--amber-500); }
.mms-workorder-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-workorder-page .badge.blue .dot { background:var(--blue-500); }
.mms-workorder-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-workorder-page .badge.green .dot { background:var(--green-500); }
.mms-workorder-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .mms-workorder-page .badge.red .dot { background:var(--red-500); }
.mms-workorder-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.mms-workorder-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-workorder-page .badge.gray .dot { background:var(--ink-400); }
.mms-workorder-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-workorder-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-workorder-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-workorder-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-workorder-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-workorder-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-workorder-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-workorder-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-workorder-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-workorder-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-workorder-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-workorder-page .status-tab.tab-draft .dot { background:var(--amber-500); } .mms-workorder-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .mms-workorder-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.mms-workorder-page .status-tab.tab-audit .dot { background:var(--blue-500); } .mms-workorder-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .mms-workorder-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.mms-workorder-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-workorder-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-workorder-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-workorder-page .status-tab.tab-reject .dot { background:var(--red-500); } .mms-workorder-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); } .mms-workorder-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.mms-workorder-page .status-tab.tab-void .dot { background:var(--ink-400); } .mms-workorder-page .status-tab.tab-void .count { background:var(--ink-100); color:var(--ink-500); } .mms-workorder-page .status-tab.is-active.tab-void .count { background:var(--ink-400); color:#fff; }
.mms-workorder-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.mms-workorder-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.mms-workorder-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.status-help-content { max-height: 520px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.status-help-content .flow-item { display: flex; align-items: center; gap: 8px; }
.status-help-content .flow-arrow { color: #909399; font-size: 16px; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-card-body p { margin: 4px 0; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }
.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }
.rd-page { max-width: 912px; margin: 0 auto; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
.mms-workorder-page .rd-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 12px; overflow: hidden; }
.mms-workorder-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; cursor: pointer; background: #f8fafc; border-bottom: 1px solid #e5e7eb; }
.mms-workorder-page .rd-card-header:hover { background: #f1f5f9; }
.mms-workorder-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-workorder-page .rd-card-icon { display: flex; align-items: center; color: #6366f1; }
.mms-workorder-page .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; cursor: pointer; color: #94a3b8; transition: transform .2s; }
.mms-workorder-page .rd-collapse-btn.is-collapsed { transform: rotate(180deg); }
.mms-workorder-page .rd-card-body { padding: 16px; }
.mms-workorder-page .text-muted { color: #94a3b8; }
@media (max-width:1100px) { .mms-workorder-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mms-workorder-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>