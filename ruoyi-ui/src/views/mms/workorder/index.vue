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
          <label>来源类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.sourceType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_work_order_source_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
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
          <div class="toolbar-divider"></div>
          <el-button type="primary" plain icon="MagicStick" :disabled="multiple" @click="handleBatchKitCheck" v-hasPermi="['mms:kit:add']">批量齐套检查</el-button>
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
          <el-table-column label="来源类型" prop="sourceType" key="sourceType" :width="colWidth('sourceType', 100)" resizable align="center" v-if="columns.sourceType.visible">
            <template #default="scope"><dict-tag :options="mms_work_order_source_type" :value="scope.row.sourceType" /></template>
          </el-table-column>
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span v-if="scope.row.unit" class="badge gray">{{ unitLabel(scope.row.unit) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="当前产出" prop="finishedQty" key="finishedQty" :width="colWidth('finishedQty', 130)" resizable align="center" v-if="columns.finishedQty.visible">
            <template #default="scope">
              <div class="qty-output-cell">
                <span class="qty-output-val" :class="{ 'is-final': isWorkOrderFinished(scope.row.status) }">{{ scope.row.finishedQty != null ? scope.row.finishedQty : '—' }}</span>
                <span class="qty-output-tag" v-if="scope.row.finishedQty != null && scope.row.planQty > 0 && !isWorkOrderFinished(scope.row.status)">{{ Math.round(scope.row.finishedQty / scope.row.planQty * 100) }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="合格数量" prop="qualifiedQty" key="qualifiedQty" :width="colWidth('qualifiedQty', 110)" resizable align="center" v-if="columns.qualifiedQty.visible">
            <template #default="scope">
              <span :class="{ 'qty-in-progress': !isWorkOrderFinished(scope.row.status) && scope.row.qualifiedQty != null && scope.row.qualifiedQty > 0 }">{{ scope.row.qualifiedQty != null ? scope.row.qualifiedQty : '—' }}</span>
            </template>
          </el-table-column>
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
          <el-table-column label="操作" width="380" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:workorder:query']">详情</el-button>
              <el-button v-if="scope.row.status === '0'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:workorder:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="success" icon="Promotion" @click="handleRelease(scope.row)" v-hasPermi="['mms:workorder:release']">下达</el-button>
              <el-button v-if="scope.row.status === '0' || scope.row.status === '1'" link type="primary" icon="MagicStick" @click="handleKitCheck(scope.row)" v-hasPermi="['mms:kit:add']">齐套检查</el-button>
              <el-button v-if="scope.row.status === '1' || scope.row.status === '2'" link type="warning" icon="VideoPause" @click="handlePause(scope.row)" v-hasPermi="['mms:workorder:pause']">暂停</el-button>
              <el-button v-if="scope.row.status === '5'" link type="success" icon="VideoPlay" @click="handleResume(scope.row)" v-hasPermi="['mms:workorder:resume']">恢复</el-button>
              <el-button v-if="scope.row.status === '2'" link type="primary" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['mms:workorder:finish']">完工</el-button>
              <el-button v-if="scope.row.status === '2'" link type="warning" icon="CircleClose" @click="handleForceClose(scope.row)" v-hasPermi="['mms:workorder:close']">强制关闭</el-button>
              <el-button v-if="scope.row.status === '3'" link type="success" icon="Lock" @click="handleClose(scope.row)" v-hasPermi="['mms:workorder:close']">关闭</el-button>
              <el-button v-if="scope.row.status === '3' || scope.row.status === '4'" link type="warning" icon="RefreshLeft" @click="handleRework(scope.row)" v-hasPermi="['mms:workorder:edit']">返工</el-button>
              <el-button v-if="scope.row.status !== '4' && scope.row.status !== '6'" link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['mms:workorder:cancel']">作废</el-button>
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="来源类型" prop="sourceType"><el-select v-model="form.sourceType" placeholder="请选择" @change="handleSourceTypeChange" style="width: 100%"><el-option v-for="dict in mms_work_order_source_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="工单类型" prop="orderType"><el-select v-model="form.orderType" placeholder="请选择"><el-option v-for="dict in mms_order_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col></el-row>
              <el-row :gutter="20" v-if="form.sourceType === '1'"><el-col :span="12"><el-form-item label="关联计划号" prop="mpsNo" :rules="[{ required: true, message: '请选择关联计划', trigger: 'change' }]"><el-input v-model="form.mpsNo" readonly placeholder="请选择关联计划，选择后自动带出工单信息" style="width: 100%" @click="openMpsPicker"><template #append><el-button icon="Search" @click="openMpsPicker" /></template><template #suffix><el-icon v-if="form.mpsNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearMps"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20" v-if="form.sourceType === '2'"><el-col :span="12"><el-form-item label="关联订单号" prop="sourceOrderNo" :rules="[{ required: true, message: '请选择关联销售订单', trigger: 'change' }]"><el-input v-model="form.sourceOrderNo" readonly placeholder="请选择关联销售订单，选择后自动带出信息" style="width: 100%" @click="openOrderPicker"><template #append><el-button icon="Search" @click="openOrderPicker" /></template><template #suffix><el-icon v-if="form.sourceOrderNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearSourceOrder"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" readonly placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker"><template #append><el-button icon="Search" @click="openMaterialPicker" /></template><template #suffix><el-icon v-if="form.productCode" class="rd-form-tip" style="cursor:pointer" @click.stop="clearMaterial"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col><el-col :span="12"><el-form-item label="规格型号" prop="specModel"><el-input v-model="form.specModel" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="单位" prop="unit"><el-select v-model="form.unit" placeholder="请选择" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M2 12h20"/></svg></span>生产信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划数量" prop="planQty"><el-input-number v-model="form.planQty" :min="0" :precision="2" placeholder="请输入" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="BOM编号" prop="bomNo"><el-input v-model="form.bomNo" readonly placeholder="请选择BOM" style="width: 100%" @click="openBomPicker"><template #append><el-button icon="Search" @click="openBomPicker" /></template><template #suffix><el-icon v-if="form.bomNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearBom"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="24"><el-form-item label="工艺路线" prop="routeNo"><el-input v-model="form.routeNo" readonly placeholder="请选择工艺路线" style="width: 100%" @click="openRoutePicker"><template #append><el-button icon="Search" @click="openRoutePicker" /></template><template #suffix><el-icon v-if="form.routeNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearRoute"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
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
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rc_kit')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg></span>齐套检查</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc_kit }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.rc_kit">
              <div v-if="!releaseKitCheckDone" style="text-align:center;padding:20px 0">
                <el-button type="primary" plain icon="MagicStick" @click="handleReleaseKitCheck" v-hasPermi="['mms:kit:add']">执行齐套检查</el-button>
                <div style="margin-top:8px;color:var(--el-text-color-secondary);font-size:12px;">
                  <el-icon><InfoFilled /></el-icon>
                  下达前建议执行齐套检查，确认物料是否满足生产需求
                </div>
              </div>
              <div v-else-if="releaseKitCheckData" class="kit-release-result">
                <div class="kit-release-summary">
                  <div class="kit-release-item">
                    <span class="kit-release-label">齐套单号</span>
                    <span class="kit-release-value">{{ releaseKitCheckData.kitNo || '—' }}</span>
                  </div>
                  <div class="kit-release-item">
                    <span class="kit-release-label">齐套率</span>
                    <span class="kit-release-value" :style="{ color: parseFloat(releaseKitCheckData.kitRate) >= 100 ? '#10b981' : parseFloat(releaseKitCheckData.kitRate) >= 80 ? '#f59e0b' : '#ef4444', fontWeight: 600 }">{{ releaseKitCheckData.kitRate }}%</span>
                  </div>
                  <div class="kit-release-item">
                    <span class="kit-release-label">是否齐套</span>
                    <span class="kit-release-value"><span v-if="releaseKitCheckData.isComplete" class="badge" :class="releaseKitCheckData.isComplete === '1' ? 'green' : 'red'"><span class="dot"></span>{{ releaseKitCheckData.isComplete === '1' ? '是' : '否' }}</span><span v-else>—</span></span>
                  </div>
                  <div class="kit-release-item">
                    <span class="kit-release-label">检查时间</span>
                    <span class="kit-release-value">{{ releaseKitCheckData.checkTime ? parseTime(releaseKitCheckData.checkTime) : '—' }}</span>
                  </div>
                </div>
                <el-alert v-if="releaseKitCheckData.isComplete === '0'" type="error" :closable="false" show-icon style="margin-top:8px">
                  <template #title>物料不齐套，存在缺料风险，建议跟进缺料后再下达</template>
                </el-alert>
                <el-alert v-else-if="releaseKitCheckData.isComplete === '1'" type="success" :closable="false" show-icon style="margin-top:8px">
                  <template #title>物料齐套检查通过，可以放心下达</template>
                </el-alert>
              </div>
            </div>
          </section>
        </template>
      </div>
      <template #footer>
        <el-button @click="releaseOpen = false">取 消</el-button>
        <el-button type="primary" :loading="releaseSubmitting" :disabled="releaseData.canRelease === false" @click="submitRelease">确认下达</el-button>
      </template>
    </el-dialog>

    <!-- ===== 暂停原因弹窗 ===== -->
    <el-dialog v-model="pauseOpen" width="984px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg></div>
          <span class="rd-detail-header-title">工单暂停</span>
          <div class="rd-detail-header-sub" v-if="pauseForm.workOrderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ pauseForm.workOrderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('pc_wo')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>工单信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.pc_wo }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.pc_wo" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ pauseForm.workOrderNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="mms_order_type" :value="pauseForm.orderType" /></div></div>
            <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ pauseForm.productCode || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ pauseForm.productName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ pauseForm.planQty != null ? pauseForm.planQty : '—' }} {{ unitLabel(pauseForm.unit) }}</div></div>
            <div class="rd-item"><span class="rd-label">当前产出</span><div class="rd-value" :class="{ 'rd-value--in-progress': pauseForm.finishedQty != null && pauseForm.finishedQty > 0 }">{{ pauseForm.finishedQty != null ? pauseForm.finishedQty : '—' }}<span class="rd-value-hint" v-if="pauseForm.finishedQty != null && pauseForm.finishedQty > 0">（进行中）</span></div></div>
<div class="rd-item"><span class="rd-label">工单状态</span><div class="rd-value"><span v-if="pauseForm.status" class="badge" :class="badgeClass(pauseForm.status)"><span class="dot"></span>{{ statusLabel(pauseForm.status) }}</span><span v-else class="text-muted">—</span></div></div>
</div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('pc_reason')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>暂停原因</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.pc_reason }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.pc_reason">
            <el-form ref="pauseFormRef" :model="pauseForm" label-width="100px">
              <el-form-item label="暂停原因" prop="pauseReason" :rules="[{ required: true, message: '请输入暂停原因', trigger: 'blur' }]">
                <el-input v-model="pauseForm.pauseReason" type="textarea" :rows="4" placeholder="请输入暂停原因，例如：等待物料齐套、设备故障、品质异常等" />
              </el-form-item>
            </el-form>
            <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px;">
              <el-icon><InfoFilled /></el-icon>
              暂停后工单将进入"已暂停"状态，恢复后可继续生产
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="pauseOpen = false">取 消</el-button>
        <el-button type="warning" @click="submitPause">确认暂停</el-button>
      </template>
    </el-dialog>

    <!-- ===== 关闭/作废弹窗 ===== -->
    <el-dialog v-model="closeOpen" width="984px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">{{ closeTitle }}</span>
          <div class="rd-detail-header-sub" v-if="closeForm.workOrderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ closeForm.workOrderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cc_wo')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>工单信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cc_wo }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.cc_wo" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ closeForm.workOrderNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="mms_order_type" :value="closeForm.orderType" /></div></div>
            <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ closeForm.productCode || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ closeForm.productName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ closeForm.planQty != null ? closeForm.planQty : '—' }} {{ unitLabel(closeForm.unit) }}</div></div>
            <div class="rd-item"><span class="rd-label">完工数量</span><div class="rd-value">{{ closeForm.finishedQty != null ? closeForm.finishedQty : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ closeForm.qualifiedQty != null ? closeForm.qualifiedQty : '—' }}</div></div>
            <div class="rd-item" v-if="closeForm.type === 'forceClose'"><span class="rd-label">不良数量</span><div class="rd-value" style="color: var(--el-color-danger);">{{ closeForm.defectQty != null ? closeForm.defectQty : '—' }}</div></div>
            <div class="rd-item" v-if="closeForm.type === 'forceClose'"><span class="rd-label">短产数量</span><div class="rd-value" style="color: var(--el-color-danger); font-weight: 600;">{{ closeForm.shortQty != null ? closeForm.shortQty : '—' }} {{ unitLabel(closeForm.unit) }}</div></div>
            <div class="rd-item"><span class="rd-label">工单状态</span><div class="rd-value"><span v-if="closeForm.status" class="badge" :class="badgeClass(closeForm.status)"><span class="dot"></span>{{ statusLabel(closeForm.status) }}</span><span v-else class="text-muted">—</span></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cc_reason')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>{{ closeLabel }}</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cc_reason }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.cc_reason">
            <el-form ref="closeFormRef" :model="closeForm" label-width="100px">
              <el-form-item :label="closeLabel" prop="remark" :rules="[{ required: true, message: '请输入' + closeLabel, trigger: 'blur' }]">
                <el-input v-model="closeForm.remark" type="textarea" :rows="4" :placeholder="'请输入' + closeLabel" />
              </el-form-item>
            </el-form>
            <div v-if="closeForm.type === 'cancel'" style="margin-top: 8px; color: var(--el-color-danger); font-size: 12px;">
              <el-icon><WarningFilled /></el-icon>
              作废后工单将无法恢复，请确认后再操作
            </div>
            <div v-else-if="closeForm.type === 'forceClose'" style="margin-top: 8px; color: var(--el-color-danger); font-size: 12px;">
              <el-icon><WarningFilled /></el-icon>
              强制关闭将终止未完成的工序，取消未完成派工单，工单进入“已关闭”状态。短产数量：{{ closeForm.shortQty }} {{ unitLabel(closeForm.unit) }}
            </div>
            <div v-else style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px;">
              <el-icon><InfoFilled /></el-icon>
              关闭后工单将进入“已关闭”状态，不再允许任何操作
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="closeOpen = false">取 消</el-button>
        <el-button :type="closeForm.type === 'cancel' ? 'danger' : (closeForm.type === 'forceClose' ? 'warning' : 'success')" @click="submitClose">{{ closeForm.type === 'cancel' ? '确认作废' : (closeForm.type === 'forceClose' ? '确认强制关闭' : '确认关闭') }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== 返工弹窗 ===== -->
    <el-dialog v-model="reworkOpen" width="984px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg></div>
          <span class="rd-detail-header-title">创建返工工单</span>
          <div class="rd-detail-header-sub" v-if="reworkForm.workOrderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ reworkForm.workOrderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rw_wo')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>源工单信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rw_wo }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.rw_wo" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ reworkForm.workOrderNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ reworkForm.productCode || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ reworkForm.productName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ reworkForm.planQty != null ? reworkForm.planQty : '—' }} {{ unitLabel(reworkForm.unit) }}</div></div>
            <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ reworkForm.qualifiedQty != null ? reworkForm.qualifiedQty : '—' }}</div></div>
            <div class="rd-item rd-item-highlight"><span class="rd-label">不良数量</span><div class="rd-value" style="color: var(--el-color-danger); font-weight: 600;">{{ reworkForm.defectQty != null ? reworkForm.defectQty : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">完工数量</span><div class="rd-value" style="color: var(--el-color-success); font-weight: 600;">{{ reworkForm.finishedQty != null ? reworkForm.finishedQty : '—' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rw_form')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>返工信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rw_form }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.rw_form">
            <el-form ref="reworkFormRef" :model="reworkForm" label-width="100px">
              <el-form-item label="返工数量" prop="reworkQty" :rules="[{ required: true, message: '请输入返工数量', trigger: 'blur' }]">
                <el-input-number v-model="reworkForm.reworkQty" :min="1" :max="reworkForm.planQty || 1" :precision="0" style="width: 200px" />
                <span style="margin-left: 8px; color: var(--el-text-color-secondary); font-size: 12px;">最大可返工数量：{{ reworkForm.planQty || 0 }}</span>
              </el-form-item>
              <el-form-item label="返工原因" prop="reworkReason">
                <el-input v-model="reworkForm.reworkReason" type="textarea" :rows="4" placeholder="请输入返工原因（可选）" />
              </el-form-item>
            </el-form>
            <div style="margin-top: 8px; color: var(--el-color-warning); font-size: 12px;">
              <el-icon><InfoFilled /></el-icon>
              返工工单将继承源工单的BOM和工艺路线，需重新下达后才能开始生产
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="reworkOpen = false">取 消</el-button>
        <el-button type="warning" @click="submitRework">确认创建返工工单</el-button>
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
      <el-tabs v-model="viewActiveTab" class="wo-detail-tabs">
        <!-- 页签一：工单详情 -->
        <el-tab-pane label="工单详情" name="detail">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="mms_order_type" :value="viewData.orderType" /></div></div>
            <div class="rd-item"><span class="rd-label">来源类型</span><div class="rd-value"><dict-tag :options="mms_work_order_source_type" :value="viewData.sourceType" /></div></div>
            <div class="rd-item" v-if="viewData.sourceType === '1'"><span class="rd-label">关联计划号</span><div class="rd-value">{{ viewData.mpsNo || '—' }}</div></div>
            <div class="rd-item" v-if="viewData.sourceType === '2'"><span class="rd-label">关联订单号</span><div class="rd-value">{{ viewData.sourceOrderNo || '—' }}</div></div>
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
            <div class="rd-item"><span class="rd-label">{{ isWorkOrderFinished(viewData.status) ? '完工数量' : '当前产出' }}</span><div class="rd-value" :class="{ 'rd-value--in-progress': !isWorkOrderFinished(viewData.status) && viewData.finishedQty != null && viewData.finishedQty > 0 }">{{ viewData.finishedQty != null ? viewData.finishedQty : '—' }}<span class="rd-value-hint" v-if="!isWorkOrderFinished(viewData.status) && viewData.finishedQty != null && viewData.finishedQty > 0">（进行中）</span></div></div>
            <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value" :class="{ 'rd-value--in-progress': !isWorkOrderFinished(viewData.status) && viewData.qualifiedQty != null && viewData.qualifiedQty > 0 }">{{ viewData.qualifiedQty != null ? viewData.qualifiedQty : '—' }}<span class="rd-value-hint" v-if="!isWorkOrderFinished(viewData.status) && viewData.qualifiedQty != null && viewData.qualifiedQty > 0">（半成品）</span></div></div>
            <div class="rd-item"><span class="rd-label">BOM编号</span><div class="rd-value">{{ viewData.bomNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工艺路线</span><div class="rd-value">{{ viewData.routeNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划开工</span><div class="rd-value">{{ viewData.planStart ? parseTime(viewData.planStart) : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划完工</span><div class="rd-value">{{ viewData.planFinish ? parseTime(viewData.planFinish) : '—' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.bomSnapshotList && viewData.bomSnapshotList.length > 0">
          <div class="rd-card-header" @click="toggleCard('vc_bom')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>BOM明细（下达冻结）</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc_bom }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc_bom">
            <el-table :data="viewData.bomSnapshotList" border size="small" style="width: 100%" max-height="300">
              <el-table-column label="序号" prop="seq" width="60" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="140" />
              <el-table-column label="规格型号" prop="specModel" min-width="120" />
              <el-table-column label="单位" width="60" align="center">
                <template #default="scope">{{ unitLabel(scope.row.unit) }}</template>
              </el-table-column>
              <el-table-column label="单件用量" prop="usageQty" width="100" align="center" />
              <el-table-column label="损耗率(%)" prop="lossRate" width="90" align="center" />
              <el-table-column label="关键料" width="70" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.isKeyMaterial === '1'" type="danger" size="small">是</el-tag>
                  <span v-else>—</span>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px;">
              <el-icon><InfoFilled /></el-icon>
              BOM明细在工单下达时冻结为快照，后续BOM变更不影响本工单
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.routeSnapshotList && viewData.routeSnapshotList.length > 0">
          <div class="rd-card-header" @click="toggleCard('vc_route')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>工艺工序（下达冻结）</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc_route }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc_route">
            <div class="flow-canvas" v-if="viewData.routeSnapshotList && viewData.routeSnapshotList.length > 0">
              <template v-for="(group, gi) in viewRouteProcessGroups" :key="gi">
                <div class="flow-connector" :class="'flow-connector--' + flowConnectorStatus(viewRouteProcessGroups[gi - 1], group)" v-if="gi > 0">
                  <div class="flow-connector-dot"></div>
                  <div class="flow-connector-line"></div>
                  <div class="flow-connector-arrow"></div>
                </div>
                <div class="flow-step" :class="{ 'is-parallel': group.length > 1 }">
                  <div class="flow-step-badge" :class="group.length > 1 ? 'parallel' : 'serial'">{{ gi + 1 }}</div>
                  <div class="flow-step-body">
                    <div class="flow-step-label" v-if="group.length > 1">并行执行</div>
                    <div class="flow-step-label" v-else>串行</div>
                    <div class="flow-step-nodes">
                      <div class="flow-card" :class="'flow-card--' + p.execStatus" v-for="(p, pi) in group" :key="pi">
                        <div class="flow-card-icon" :class="p.isKeyProcess === '1' ? 'key' : 'normal'"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
                        <div class="flow-card-text">
                          <div class="flow-card-name">{{ p.processName || '未命名' }}</div>
                          <div class="flow-card-code">{{ p.processCode || '—' }}</div>
                          <div class="flow-card-meta" v-if="p.resourceName">{{ p.resourceName }}</div>
                          <div class="flow-card-status">
                            <span class="flow-status-dot" :class="'dot--' + p.execStatus"></span>
                            <span class="flow-status-text" :class="'text--' + p.execStatus">{{ flowStatusText(p.execStatus) }}</span>
                            <span class="flow-status-progress" v-if="p.progress > 0">{{ p.progress }}%</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
            <!-- 图例 -->
            <div class="flow-legend">
              <div class="flow-legend-item"><span class="flow-legend-dot dot--completed"></span>已完工</div>
              <div class="flow-legend-item"><span class="flow-legend-dot dot--in-progress"></span>进行中</div>
              <div class="flow-legend-item"><span class="flow-legend-dot dot--pending"></span>待开工</div>
              <div class="flow-legend-item"><span class="flow-legend-dot dot--not-dispatched"></span>未派工</div>
              <div class="flow-legend-item"><span class="flow-legend-dot dot--cancelled"></span>已取消</div>
            </div>
            <el-table :data="viewData.routeSnapshotList" border size="small" style="width: 100%; margin-top: 12px;" max-height="300">
              <el-table-column label="顺序" prop="stepSeq" width="70" align="center" />
              <el-table-column label="工序编码" prop="processCode" min-width="120" />
              <el-table-column label="工序名称" prop="processName" min-width="140" />
              <el-table-column label="产能单元" prop="resourceName" min-width="120" show-overflow-tooltip />
              <el-table-column label="标准工时(h)" prop="stdTime" width="110" align="center" />
              <el-table-column label="准备时间(h)" prop="prepTime" width="110" align="center" />
              <el-table-column label="关键工序" width="80" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.isKeyProcess === '1'" type="danger" size="small">是</el-tag>
                  <span v-else>—</span>
                </template>
              </el-table-column>
              <el-table-column label="外协" width="70" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.isOutsource === '1'" type="warning" size="small">是</el-tag>
                  <span v-else>—</span>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px;">
              <el-icon><InfoFilled /></el-icon>
              工艺工序在工单下达时冻结为快照，后续工艺变更不影响本工单
            </div>
          </div>
        </section>
        <!-- 排产信息 -->
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
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid">
            <div class="rd-item" v-if="viewData.sourceType !== '1' && viewData.sourceType !== '2'"><span class="rd-label">来源说明</span><div class="rd-value text-muted">手工创建，无关联单据</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div>
          </div></div>
        </section>
        </el-tab-pane>

        <!-- 页签二：工序完成情况 -->
        <el-tab-pane label="工序完成情况" name="progress" v-if="viewData.routeSnapshotList && viewData.routeSnapshotList.length > 0">
          <div v-if="viewProgressSummary">
            <!-- 总体进度概览 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('pc_overview')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>总体进度</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.pc_overview }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
              <div class="rd-card-body" v-show="!collapsedCards.pc_overview" style="display:block">
                <div class="proc-overview">
                  <div class="proc-overview-main">
                    <div class="proc-overview-ring">
                      <svg viewBox="0 0 120 120" class="proc-ring-svg">
                        <circle cx="60" cy="60" r="50" fill="none" stroke="#e5e7eb" stroke-width="8" />
                        <circle cx="60" cy="60" r="50" fill="none" stroke="url(#ringGrad)" stroke-width="8" stroke-linecap="round" :stroke-dasharray="314" :stroke-dashoffset="314 - (314 * viewProgressSummary.overallProgress / 100)" transform="rotate(-90 60 60)" class="proc-ring-fill" />
                        <defs>
                          <linearGradient id="ringGrad" x1="0" y1="0" x2="1" y2="1">
                            <stop offset="0%" stop-color="#6366f1" />
                            <stop offset="100%" stop-color="#10b981" />
                          </linearGradient>
                        </defs>
                      </svg>
                      <div class="proc-ring-text">
                        <div class="proc-ring-num">{{ viewProgressSummary.overallProgress }}<span class="proc-ring-unit">%</span></div>
                        <div class="proc-ring-label">总体进度</div>
                      </div>
                    </div>
                    <div class="proc-overview-stats">
                      <div class="proc-stat-item">
                        <div class="proc-stat-num">{{ viewProgressSummary.total }}</div>
                        <div class="proc-stat-label">工序总数</div>
                      </div>
                      <div class="proc-stat-item proc-stat--green">
                        <div class="proc-stat-num">{{ viewProgressSummary.completed }}</div>
                        <div class="proc-stat-label">已完工</div>
                      </div>
                      <div class="proc-stat-item proc-stat--blue">
                        <div class="proc-stat-num">{{ viewProgressSummary.inProgress }}</div>
                        <div class="proc-stat-label">进行中</div>
                      </div>
                      <div class="proc-stat-item proc-stat--gray">
                        <div class="proc-stat-num">{{ viewProgressSummary.pending }}</div>
                        <div class="proc-stat-label">待开工</div>
                      </div>
                    </div>
                  </div>
                  <div class="proc-overview-divider"></div>
                  <div class="proc-overview-qty">
                    <div class="proc-qty-row">
                      <span class="proc-qty-label">计划总量</span>
                      <span class="proc-qty-val">{{ viewProgressSummary.totalPlanQty }}</span>
                    </div>
                    <div class="proc-qty-row">
                      <span class="proc-qty-label">{{ isWorkOrderFinished(viewData.status) ? '合格总量' : '当前合格' }}</span>
                      <span class="proc-qty-val" :class="isWorkOrderFinished(viewData.status) ? 'proc-qty--green' : 'proc-qty--blue'">{{ viewProgressSummary.totalGoodQty }}</span>
                    </div>
                    <div class="proc-qty-row">
                      <span class="proc-qty-label">{{ isWorkOrderFinished(viewData.status) ? '不良总量' : '当前损耗' }}</span>
                      <span class="proc-qty-val proc-qty--red">{{ viewProgressSummary.totalDefectQty }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </section>
            <!-- 工序进度详情时间线 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('pc_timeline')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>工序进度详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.pc_timeline }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
              <div class="rd-card-body" v-show="!collapsedCards.pc_timeline">
                <div class="proc-timeline">
                  <template v-for="(p, idx) in viewProcessProgress" :key="idx">
                    <div class="proc-timeline-connector" v-if="idx > 0">
                      <div class="proc-timeline-line"></div>
                    </div>
                    <div class="proc-step-card" :class="'proc-step--' + (p.status || 'none')">
                      <div class="proc-step-left">
                        <div class="proc-step-index">{{ idx + 1 }}</div>
                        <div class="proc-step-status-dot" :style="{ background: dispatchProgressColor(p.status) }"></div>
                      </div>
                      <div class="proc-step-right">
                        <div class="proc-step-head">
                          <div class="proc-step-title">
                            <span class="proc-step-name">{{ p.processName || '未命名' }}</span>
                            <span class="proc-step-code">{{ p.processCode || '—' }}</span>
                            <el-tag v-if="p.isKeyProcess === '1'" type="danger" size="small" effect="light" style="margin-left:4px">关键</el-tag>
                            <el-tag v-if="p.isOutsource === '1'" type="warning" size="small" effect="light" style="margin-left:2px">外协</el-tag>
                          </div>
                          <span v-if="p.status" class="badge" :class="dispatchStatusBadgeClass(p.status)"><span class="dot"></span>{{ p.statusLabel }}</span>
                          <span v-else class="badge gray"><span class="dot"></span>未派工</span>
                        </div>
                        <div class="proc-step-meta">
                          <span class="proc-meta-item" v-if="p.resourceName"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.91 8.84L8.56 2.23a2 2 0 0 0-1.12 0L2.43 5.32a1 1 0 0 0 0 1.78l11.14 5.32a1 1 0 0 0 .84 0l7.68-3.65a1 1 0 0 0 0-1.78z"/><path d="M3.27 6.96l-.42 6.4c-.01.35.19.67.52.83l9.46 4.51a1 1 0 0 0 .84 0l9.46-4.51c.33-.16.53-.48.52-.83l-.42-6.4"/></svg>{{ p.resourceName }}</span>
                          <span class="proc-meta-item" v-if="p.teamName"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>{{ p.teamName }}</span>
                          <span class="proc-meta-item" v-if="p.dispatchNo"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>{{ p.dispatchNo }}</span>
                        </div>
                        <!-- 进度条 -->
                        <div class="proc-step-bar-wrap" v-if="p.status !== null">
                          <div class="proc-step-bar-track">
                            <div class="proc-step-bar-fill" :style="{ width: p.progress + '%', background: dispatchProgressColor(p.status) }"></div>
                          </div>
                          <span class="proc-step-bar-pct">{{ p.progress }}%</span>
                        </div>
                        <!-- 数量信息 -->
                        <div class="proc-step-qty-grid" v-if="p.status !== null">
                          <div class="proc-qty-cell">
                            <span class="proc-qty-cell-label">计划</span>
                            <span class="proc-qty-cell-val">{{ p.planQty ?? '—' }}</span>
                          </div>
                          <div class="proc-qty-cell proc-qty-cell--green">
                            <span class="proc-qty-cell-label">合格</span>
                            <span class="proc-qty-cell-val">{{ p.goodQty ?? '—' }}</span>
                          </div>
                          <div class="proc-qty-cell proc-qty-cell--red" v-if="parseFloat(p.defectQty) > 0">
                            <span class="proc-qty-cell-label">不良</span>
                            <span class="proc-qty-cell-val">{{ p.defectQty }}</span>
                          </div>
                        </div>
                        <!-- 时间信息 -->
                        <div class="proc-step-time" v-if="p.actualStart || p.actualEnd">
                          <span class="proc-time-item" v-if="p.actualStart">
                            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 4 12 14.01 9 11.01"/></svg>
                            开工 {{ parseTime(p.actualStart, '{m}-{d} {h}:{i}') }}
                          </span>
                          <span class="proc-time-item" v-if="p.actualEnd">
                            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                            完工 {{ parseTime(p.actualEnd, '{m}-{d} {h}:{i}') }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </template>
                </div>
              </div>
            </section>
          </div>
          <div v-else class="rd-empty">
            <div class="rd-empty-icon"><svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></div>
            <p class="rd-empty-text">暂无工序完成情况数据</p>
          </div>
        </el-tab-pane>
      </el-tabs>
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

    <!-- ===== 齐套检查结果弹窗 ===== -->
    <el-dialog v-model="kitCheckResultOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg></div>
          <span class="rd-detail-header-title">齐套检查结果</span>
          <div class="rd-detail-header-sub" v-if="kitCheckResultData.kitNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ kitCheckResultData.kitNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('kc_info')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.kc_info }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.kc_info" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">齐套单号</span><div class="rd-value">{{ kitCheckResultData.kitNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ kitCheckResultData.workOrderNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">主计划号</span><div class="rd-value">{{ kitCheckResultData.mpsNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">BOM编号</span><div class="rd-value">{{ kitCheckResultData.bomNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">齐套率</span><div class="rd-value" :style="{ color: parseFloat(kitCheckResultData.kitRate) >= 100 ? '#10b981' : parseFloat(kitCheckResultData.kitRate) >= 80 ? '#f59e0b' : '#ef4444', fontWeight: 600 }">{{ kitCheckResultData.kitRate != null ? kitCheckResultData.kitRate + '%' : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">是否齐套</span><div class="rd-value"><span v-if="kitCheckResultData.isComplete" class="badge" :class="kitCheckResultData.isComplete === '1' ? 'green' : 'red'"><span class="dot"></span>{{ kitCheckResultData.isComplete === '1' ? '是' : '否' }}</span><span v-else class="text-muted">—</span></div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="kitCheckResultData.status" class="badge" :class="kitStatusBadgeClass(kitCheckResultData.status)"><span class="dot"></span>{{ kitStatusLabel(kitCheckResultData.status) }}</span><span v-else class="text-muted">—</span></div></div>
            <div class="rd-item"><span class="rd-label">检查时间</span><div class="rd-value">{{ kitCheckResultData.checkTime ? parseTime(kitCheckResultData.checkTime) : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="kitCheckDetailList.length > 0">
          <div class="rd-card-header" @click="toggleCard('kc_summary')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>统计概览</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.kc_summary }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.kc_summary" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">物料总项数</span><div class="rd-value" style="font-size:18px;font-weight:700">{{ kitCheckDetailList.length }}</div></div>
            <div class="rd-item"><span class="rd-label">齐套项数</span><div class="rd-value" style="font-size:18px;font-weight:700;color:#10b981">{{ kitCheckDetailList.filter(d => d.isComplete === '1').length }}</div></div>
            <div class="rd-item"><span class="rd-label">缺料项数</span><div class="rd-value" style="font-size:18px;font-weight:700;color:#ef4444">{{ kitCheckDetailList.filter(d => d.isComplete === '0').length }}</div></div>
            <div class="rd-item"><span class="rd-label">齐套率</span><div class="rd-value" style="font-size:18px;font-weight:700" :style="{ color: parseFloat(kitCheckResultData.kitRate) >= 100 ? '#10b981' : parseFloat(kitCheckResultData.kitRate) >= 80 ? '#f59e0b' : '#ef4444' }">{{ kitCheckResultData.kitRate != null ? kitCheckResultData.kitRate + '%' : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="kitCheckDetailList.length > 0">
          <div class="rd-card-header" @click="toggleCard('kc_detail')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>物料明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.kc_detail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.kc_detail" style="padding:0">
            <el-table :data="kitCheckDetailList" border :max-height="400" size="small">
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
                <template #default="scope"><span v-if="scope.row.isComplete" class="badge" :class="scope.row.isComplete === '1' ? 'green' : 'red'"><span class="dot"></span>{{ scope.row.isComplete === '1' ? '是' : '否' }}</span><span v-else class="text-muted">—</span></template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="kitCheckResultOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 物料选择器（仅半成品和成品） ===== -->
    <material-picker ref="materialPickerRef" title="选择产品物料" :material-types="['1','2']" @confirm="onMaterialPickerConfirm" />

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

    <!-- ===== 关联销售订单选择器弹窗（订单+明细展平到单表） ===== -->
    <el-dialog v-model="orderPickerOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg></div>
          <span class="rd-detail-header-title">选择关联销售订单</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="orderPickerQuery.orderNo" placeholder="订单编号" clearable size="small" style="width: 180px" @keyup.enter="handleOrderPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleOrderPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetOrderPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table order-picker-table">
          <el-table v-loading="orderPickerLoading" :data="orderPickerFlatList" highlight-current-row @row-click="onOrderRowClick" @row-dblclick="onOrderRowDblClick" height="380" size="small" :span-method="onOrderSpanMethod" :row-class-name="orderRowClassName" empty-text="暂无已确认的订单明细">
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="orderPickerSelectedId" :value="row._rowKey" @click.stop="onOrderRadioClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="订单编号" prop="orderNo" width="140" show-overflow-tooltip />
            <el-table-column label="客户名称" prop="customerName" min-width="120" show-overflow-tooltip />
            <el-table-column label="订单金额" prop="orderAmount" width="100" align="center" />
            <el-table-column label="行号" prop="lineNo" width="60" align="center" />
            <el-table-column label="产品名称" prop="productName" min-width="140" show-overflow-tooltip />
            <el-table-column label="规格型号" prop="productSpec" width="120" show-overflow-tooltip />
            <el-table-column label="单位" prop="unit" width="70" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
            <el-table-column label="订单数量" prop="quantity" width="90" align="center" />
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="orderPickerQuery.pageNum" v-model:page-size="orderPickerQuery.pageSize" :total="orderPickerTotal" layout="total, prev, pager, next" small @current-change="getOrderPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="orderPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleOrderPickerConfirm" :disabled="!orderPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="工单管理业务操作说明" width="984px" append-to-body>
      <div class="status-help-content">
        <!-- 一、工单释义 -->
        <h4>一、工单释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是工单？</div>
          <div class="highlight-card-body">
            <strong>工单（Work Order）</strong>是生产管控中用于安排、执行和跟踪生产任务的核心单据。工单关联产品、BOM和工艺路线，定义计划数量、计划开工/完工时间，通过下达→生产→完工的全生命周期管理，实现生产过程的数字化管控和进度追踪。<br/><br/>
            工单是<strong>MES（制造执行系统）</strong>的核心数据载体，向上对接主生产计划（MPS）和销售需求，向下驱动物料领料、工序流转、质量检验和完工入库，满足精益生产和离散制造对生产过程可追溯、可量化、可管控的要求。
          </div>
        </div>

        <!-- 二、工单状态流转图 -->
        <h4>二、工单状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">新建</el-tag>
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
            <el-tag type="primary">执行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「完工」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完工</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「关闭」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已关闭</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">新建</el-tag>
            <el-tag size="small" type="danger">或</el-tag>
            <el-tag type="primary">已下达/执行中</el-tag>
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
            <el-tag type="primary">已下达/执行中</el-tag>
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
            <el-tag type="primary">执行中</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="新建">工单新建后的初始状态（0）。可修改工单信息、下达至生产线或作废。点击「下达」将工单下发至车间，状态变为已下达</el-descriptions-item>
          <el-descriptions-item label="已下达">工单已下达至生产线（1），等待开始生产。可暂停或开始生产。进入生产后状态自动变为执行中</el-descriptions-item>
          <el-descriptions-item label="执行中">工单正在生产执行中（2）。可暂停（需填写暂停原因）或完工。暂停后可恢复继续生产</el-descriptions-item>
          <el-descriptions-item label="已完工">工单所有工序完工后自动流转到此状态（3）。系统会<strong>自动生成一条完工质检单</strong>（在质检管理页面查看），质检作为独立业务流转，不卡住工单状态。可关闭工单或发起返工</el-descriptions-item>
          <el-descriptions-item label="已关闭">工单已关闭（4），不可再操作。关闭表示工单生命周期结束，数据归档</el-descriptions-item>
          <el-descriptions-item label="已暂停">工单已暂停生产（5），可恢复继续生产。暂停时需填写暂停原因</el-descriptions-item>
          <el-descriptions-item label="已作废">工单因异常情况被作废（6），不再执行。除已作废和已关闭外的工单均可作废</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>工单编号：</strong>工单的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>工单类型：</strong>包括生产工单、返工工单、试产工单等<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>来源类型：</strong>工单来源分为三种<span style="color: #f56c6c;">*必填</span></p>
            <p style="padding-left: 16px;">○ <strong>计划生成：</strong>从主生产计划（MPS）下达而来，需选择关联计划号</p>
            <p style="padding-left: 16px;">○ <strong>订单直转：</strong>从销售订单直接转为工单，需选择关联订单号</p>
            <p style="padding-left: 16px;">○ <strong>手工创建：</strong>手工新建工单，无关联单据</p>
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
            <p>• <strong>工艺路线：</strong>工单关联的工艺路线，各工序已绑定对应产能单元</p>
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
            <p>1. <strong>暂停处理：</strong>执行中如遇设备故障、物料短缺等异常，可暂停工单并填写暂停原因，排除故障后恢复生产</p>
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
            <strong>下达工单：</strong>新建状态下点击「下达」将工单下发至生产线，状态变为已下达，车间可开始排产
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>暂停/恢复：</strong>执行中如需暂停，点击「暂停」并填写原因。暂停后点击「恢复」继续生产
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

        <!-- 七、数量计算逻辑说明 -->
        <h4>七、数量计算逻辑说明</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">核心原则：串行工序数量流转，不可简单累加</div>
          <div class="highlight-card-body">
            在串行工序中，前道工序的不良品已被剔除，不会流转到后道工序。后道工序的计划数量等于前道工序的合格数量（即<strong>合格品向下流转，不良品就地剔除</strong>）。因此，工单层面的数量汇总不能简单将各工序的数量相加，否则会导致重复计算。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">三个核心数量字段</div>
          <div class="highlight-card-body">
            <p>• <strong>合格数量（qualifiedQty）：</strong>取最终已完工工序的合格数。如果有并行工序（相同工序顺序号），取并行工序中最小的合格数。如果最终工序尚未完工，取当前最远已完工工序的合格数</p>
            <p>• <strong>不良数量（defectQty）：</strong>工单计划数量 − 最终合格数量。表示从投入到最终产出过程中的总损耗。如果还没有任何工序完工，不良数量为0</p>
            <p>• <strong>当前产出（finishedQty）：</strong>最终已完工工序的合格数 + 该工序的不良数。表示最后一道工序实际处理了多少产品</p>
          </div>
        </div>
        <div class="highlight-card highlight-success" style="margin-top: 12px;">
          <div class="highlight-card-title">计算示例</div>
          <div class="highlight-card-body">
            <p>假设工单计划数量100，经过4道串行工序（含1组并行）：</p>
            <p style="padding-left: 16px;">○ op10 下料切割：计划100，合格95，不良5 → <strong>5个不良品剔除，95个向下流转</strong></p>
            <p style="padding-left: 16px;">○ op20 车削加工：计划95，合格92，不良3</p>
            <p style="padding-left: 16px;">○ op20 铣削加工：计划95，合格88，不良4 → <strong>并行工序取最小合格数88</strong></p>
            <p style="padding-left: 16px;">○ op30 部件装配：计划88，合格85，不良3 → <strong>最终工序</strong></p>
            <p style="margin-top: 8px; padding-left: 16px;">计算结果：</p>
            <p style="padding-left: 32px;">• 合格数量 = 85（最终工序合格数）</p>
            <p style="padding-left: 32px;">• 不良数量 = 100 − 85 = 15（投入 − 最终合格）</p>
            <p style="padding-left: 32px;">• 当前产出 = 85 + 3 = 88（最终工序处理总量）</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">列表显示说明</div>
          <div class="highlight-card-body">
            <p>• <strong>工单未完工时（执行中）：</strong>列表中显示为"当前产出"（蓝紫色），并附带完成百分比标签，表示这是过程中的半成品产出数量，非最终完工数据</p>
            <p>• <strong>工单已完工时（已完工/已关闭）：</strong>列表中显示为"完工数量"（绿色），表示这是最终确认的完工数据</p>
            <p>• <strong>详情弹窗工序完成情况：</strong>未完工时标签为"当前合格"和"当前损耗"，已完工时标签为"合格总量"和"不良总量"</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">为什么不能简单累加各工序不良？</div>
          <div class="highlight-card-body">
            <p>以上述示例为例，各工序不良数之和 = 5+3+4+3 = 15，碰巧等于"投入−最终合格"的结果。但这只是因为后道计划恰好等于前道合格。实际上：</p>
            <p>• 如果 op20 车削加工不良为10（而非3），则合格85，op20 铣削加工计划仍为95（与车削并行，不依赖车削结果），合格88</p>
            <p>• 并行取最小合格数 = 85，op30 计划变为85</p>
            <p>• 简单累加不良 = 5+10+4+... 会超过实际总损耗</p>
            <p>• 正确做法：总损耗 = 投入100 − 最终合格数，这始终是准确的</p>
          </div>
        </div>
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
         closeWorkOrder, cancelWorkOrder, getWorkOrderAuditLog, createReworkOrder } from "@/api/mms/workorder";
import { listBom } from "@/api/mms/bom";
import { listRoute } from "@/api/mms/route";
import { listMps } from "@/api/mms/mps";
import { listOrder, getOrder } from "@/api/mk/order";
import { autoKitCheckByWorkOrderId, batchAutoKitCheck, getKitCheck, getKitCheckDetail } from "@/api/mms/kit";
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, QuestionFilled, CircleClose, MagicStick, Box, CircleCheck, DataAnalysis } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_order_type, mms_work_order_source_type, mms_priority, mms_workorder_status, wms_unit, mms_schedule_status, mms_kit_status, mms_yes_no, marketing_order_status } = proxy.useDict("mms_order_type", "mms_work_order_source_type", "mms_priority", "mms_workorder_status", "wms_unit", "mms_schedule_status", "mms_kit_status", "mms_yes_no", "marketing_order_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_workorder_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2","vc_sched","vc_bom","vc_route","vc_progress","rc_bom","rc_route","rc_warn","rc_kit","pc_wo","pc_reason","cc_wo","cc_reason","kc_info","kc_summary","kc_detail","pc_overview","pc_timeline"])

const dataList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const viewActiveTab = ref('detail');
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
const reworkOpen = ref(false);
const reworkForm = ref({});
const reworkFormRef = ref(null);
const logOpen = ref(false);
const auditLogList = ref([]);

// 下达弹窗
const releaseOpen = ref(false);
const releaseLoading = ref(false);
const releaseData = ref({});
const releaseSubmitting = ref(false);

// 齐套检查结果弹窗
const kitCheckResultOpen = ref(false);
const kitCheckResultData = ref({});
const kitCheckDetailList = ref([]);
const kitCheckLoading = ref(false);

// 下达弹窗内的齐套检查
const releaseKitCheckDone = ref(false);
const releaseKitCheckData = ref(null);

// 状态标签列表
const statusTabList = computed(() => {
  if (!mms_workorder_status.value) return [];
  return mms_workorder_status.value.map(d => ({ label: d.label, value: d.value }));
});

// 列显隐配置
const defaultColumns = {
  workOrderNo: { label: '工单编号', visible: true },
  orderType: { label: '工单类型', visible: true },
  sourceType: { label: '来源类型', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  planQty: { label: '计划数量', visible: true },
  finishedQty: { label: '当前产出', visible: true },
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
    sourceType: undefined,
    priority: undefined,
    status: undefined,
    bomNo: undefined,
    routeNo: undefined,
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
  if (queryParams.value.sourceType) baseQuery.sourceType = queryParams.value.sourceType;
  if (queryParams.value.priority) baseQuery.priority = queryParams.value.priority;
  if (queryParams.value.bomNo) baseQuery.bomNo = queryParams.value.bomNo;
  if (queryParams.value.routeNo) baseQuery.routeNo = queryParams.value.routeNo;
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
  queryParams.value.sourceType = undefined;
  queryParams.value.priority = undefined;
  queryParams.value.status = undefined;
  queryParams.value.bomNo = undefined;
  queryParams.value.routeNo = undefined;
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
    sourceType: '3',
    sourceOrderId: undefined,
    sourceOrderNo: undefined,
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

// ===== 工艺工序流程图分组计算（含派工状态） =====
const viewRouteProcessGroups = computed(() => {
  const snapshots = viewData.value.routeSnapshotList;
  const dispatches = viewData.value.dispatchList;
  if (!snapshots || snapshots.length === 0) return [];
  // 先把每个 snapshot 合并 dispatch 状态
  const merged = snapshots.map(s => {
    const dispatch = dispatches ? dispatches.find(d => d.processId === s.processId) : null;
    let execStatus = 'not-dispatched';
    if (dispatch) {
      if (dispatch.status === '2') execStatus = 'completed';
      else if (dispatch.status === '1') execStatus = 'in-progress';
      else if (dispatch.status === '0') execStatus = 'pending';
      else if (dispatch.status === '3') execStatus = 'cancelled';
    }
    const planQty = dispatch ? (parseFloat(dispatch.planQty) || 0) : 0;
    const goodQty = dispatch ? (parseFloat(dispatch.goodQty) || 0) : 0;
    const progress = planQty > 0 ? Math.min(100, Math.round((goodQty / planQty) * 100)) : 0;
    return { ...s, execStatus, dispatch: dispatch || null, progress };
  });
  return groupByStepSeq(merged);
});

/** 流程图节点状态文字标签 */
function flowStatusText(status) {
  const map = { 'completed': '已完工', 'in-progress': '进行中', 'pending': '待开工', 'not-dispatched': '未派工', 'cancelled': '已取消' };
  return map[status] || '未派工';
}

/** 计算流程图连线状态：前一个组全完成则连线为绿色 */
function flowConnectorStatus(prevGroup, currGroup) {
  if (!prevGroup || !currGroup) return 'default';
  // 前一组所有工序都已完成 → 绿色线
  const prevAllCompleted = prevGroup.every(p => p.execStatus === 'completed');
  if (prevAllCompleted) return 'completed';
  // 前一组有进行中 → 橙色线
  const prevHasInProgress = prevGroup.some(p => p.execStatus === 'in-progress');
  if (prevHasInProgress) return 'in-progress';
  return 'default';
}

// ===== 工序完成情况 computed =====
/** 按工序快照stepSeq合并派工单数据 */
const viewProcessProgress = computed(() => {
  const snapshots = viewData.value.routeSnapshotList;
  const dispatches = viewData.value.dispatchList;
  if (!snapshots || snapshots.length === 0) return [];
  if (!dispatches || dispatches.length === 0) {
    // 工单未下达，无派工单
    return snapshots.map(s => ({
      snapshotId: s.snapshotId,
      stepSeq: s.stepSeq,
      processCode: s.processCode,
      processName: s.processName,
      resourceName: s.resourceName,
      isKeyProcess: s.isKeyProcess,
      isOutsource: s.isOutsource,
      stdTime: s.stdTime,
      prepTime: s.prepTime,
      dispatchNo: null,
      status: null,
      statusLabel: '未派工',
      planQty: null,
      goodQty: null,
      defectQty: null,
      actualStart: null,
      actualEnd: null,
      teamName: null,
      progress: 0
    }));
  }
  // 按 processId 匹配派工单
  return snapshots.map(s => {
    const dispatch = dispatches.find(d => d.processId === s.processId);
    if (!dispatch) {
      return {
        snapshotId: s.snapshotId,
        stepSeq: s.stepSeq,
        processCode: s.processCode,
        processName: s.processName,
        resourceName: s.resourceName,
        isKeyProcess: s.isKeyProcess,
        isOutsource: s.isOutsource,
        stdTime: s.stdTime,
        prepTime: s.prepTime,
        dispatchNo: null,
        status: null,
        statusLabel: '未派工',
        planQty: null,
        goodQty: null,
        defectQty: null,
        actualStart: null,
        actualEnd: null,
        teamName: null,
        progress: 0
      };
    }
    const planQty = parseFloat(dispatch.planQty) || 0;
    const goodQty = parseFloat(dispatch.goodQty) || 0;
    const defectQty = parseFloat(dispatch.defectQty) || 0;
    const progress = planQty > 0 ? Math.min(100, Math.round((goodQty / planQty) * 100)) : 0;
    return {
      snapshotId: s.snapshotId,
      stepSeq: s.stepSeq,
      processCode: s.processCode,
      processName: s.processName,
      resourceName: s.resourceName,
      isKeyProcess: s.isKeyProcess,
      isOutsource: s.isOutsource,
      stdTime: s.stdTime,
      prepTime: s.prepTime,
      dispatchNo: dispatch.dispatchNo,
      status: dispatch.status,
      statusLabel: dispatchStatusLabel(dispatch.status),
      planQty: dispatch.planQty,
      goodQty: dispatch.goodQty,
      defectQty: dispatch.defectQty,
      actualStart: dispatch.actualStart,
      actualEnd: dispatch.actualEnd,
      teamName: dispatch.teamName,
      progress: progress
    };
  });
});

/** 工序完成情况汇总数据 */
const viewProgressSummary = computed(() => {
  const list = viewProcessProgress.value;
  if (!list || list.length === 0) return null;
  const total = list.length;
  const completed = list.filter(p => p.status === '2').length;
  const inProgress = list.filter(p => p.status === '1').length;
  const pending = list.filter(p => p.status === '0' || p.status === null).length;
  const cancelled = list.filter(p => p.status === '3').length;

  // 计划总量：取首工序计划数量（代表工单总投入量，并行取最小）
  let firstPlanQty = 0;
  let firstStepSeq = null;
  for (const p of list) {
    const sq = p.stepSeq ?? 0;
    if (firstStepSeq === null || sq < firstStepSeq) {
      firstStepSeq = sq;
      firstPlanQty = parseFloat(p.planQty) || 0;
    } else if (sq === firstStepSeq) {
      // 并行工序取最小值
      firstPlanQty = Math.min(firstPlanQty, parseFloat(p.planQty) || 0);
    }
  }

  // 合格总量：取最终已完工工序（stepSeq最大且status=2）的合格数，并行取最小
  let finalGoodQty = 0;
  let finalStepSeq = null;
  let hasCompleted = false;
  for (const p of list) {
    if (p.status !== '2') continue;
    const sq = p.stepSeq ?? 0;
    if (finalStepSeq === null || sq > finalStepSeq) {
      finalStepSeq = sq;
      finalGoodQty = parseFloat(p.goodQty) || 0;
      hasCompleted = true;
    } else if (sq === finalStepSeq) {
      // 同一 stepSeq 的并行工序取最小合格数
      finalGoodQty = Math.min(finalGoodQty, parseFloat(p.goodQty) || 0);
    }
  }

  // 不良总量：投入量 - 最终合格数（串行工序不良是递进淘汰的，不能简单累加）
  let totalDefectQty = 0;
  if (hasCompleted) {
    totalDefectQty = Math.max(0, firstPlanQty - finalGoodQty);
  }

  // 总体进度：基于最终合格数 / 计划总量
  const overallProgress = firstPlanQty > 0 && hasCompleted
    ? Math.min(100, Math.round((finalGoodQty / firstPlanQty) * 100))
    : 0;

  return {
    total, completed, inProgress, pending, cancelled,
    totalPlanQty: Number(firstPlanQty.toFixed(2)),
    totalGoodQty: Number(finalGoodQty.toFixed(2)),
    totalDefectQty: Number(totalDefectQty.toFixed(2)),
    overallProgress
  };
});

function dispatchStatusLabel(status) {
  const map = { '0': '待开工', '1': '进行中', '2': '已完成', '3': '已取消' };
  return map[status] || '未派工';
}
function dispatchStatusBadgeClass(status) {
  const map = { '0': 'gray', '1': 'blue', '2': 'green', '3': 'red' };
  return map[status] || 'gray';
}
function dispatchProgressColor(status) {
  const map = { '0': '#94a3b8', '1': '#3b82f6', '2': '#10b981', '3': '#ef4444' };
  return map[status] || '#cbd5e1';
}
function groupByStepSeq(list) {
  if (!list || list.length === 0) return [];
  const sorted = [...list].sort((a, b) => (a.stepSeq || 0) - (b.stepSeq || 0));
  const groups = [];
  let currentGroup = [sorted[0]];
  for (let i = 1; i < sorted.length; i++) {
    if (sorted[i].stepSeq === sorted[i - 1].stepSeq) {
      currentGroup.push(sorted[i]);
    } else {
      groups.push(currentGroup);
      currentGroup = [sorted[i]];
    }
  }
  groups.push(currentGroup);
  return groups;
}

function handleView(row) {
getWorkOrder(row.workOrderId).then(response => {
viewData.value = response.data;
viewActiveTab.value = 'detail';
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
  releaseKitCheckDone.value = false;
  releaseKitCheckData.value = null;
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
  pauseForm.value = {
    workOrderId: row.workOrderId,
    workOrderNo: row.workOrderNo,
    orderType: row.orderType,
    productCode: row.productCode,
    productName: row.productName,
    planQty: row.planQty,
    unit: row.unit,
    finishedQty: row.finishedQty,
    status: row.status,
    pauseReason: ""
  };
  pauseOpen.value = true;
}

function submitPause() {
  proxy.$refs["pauseFormRef"].validate(valid => {
    if (valid) {
      pauseWorkOrder(pauseForm.value.workOrderId, pauseForm.value.pauseReason).then(() => {
        pauseOpen.value = false;
        getList();
        proxy.$modal.msgSuccess("暂停成功");
      });
    }
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
  closeForm.value = {
    workOrderId: row.workOrderId,
    workOrderNo: row.workOrderNo,
    orderType: row.orderType,
    productCode: row.productCode,
    productName: row.productName,
    planQty: row.planQty,
    unit: row.unit,
    finishedQty: row.finishedQty,
    qualifiedQty: row.qualifiedQty,
    status: row.status,
    remark: "",
    type: "close"
  };
  closeOpen.value = true;
}

function handleCancel(row) {
  closeTitle.value = "工单作废";
  closeLabel.value = "作废原因";
  closeForm.value = {
    workOrderId: row.workOrderId,
    workOrderNo: row.workOrderNo,
    orderType: row.orderType,
    productCode: row.productCode,
    productName: row.productName,
    planQty: row.planQty,
    unit: row.unit,
    finishedQty: row.finishedQty,
    qualifiedQty: row.qualifiedQty,
    status: row.status,
    remark: "",
    type: "cancel"
  };
  closeOpen.value = true;
}

function submitClose() {
  proxy.$refs["closeFormRef"].validate(valid => {
    if (valid) {
      if (closeForm.value.type === "close" || closeForm.value.type === "forceClose") {
        closeWorkOrder(closeForm.value.workOrderId, closeForm.value.remark).then(() => {
          closeOpen.value = false;
          getList();
          proxy.$modal.msgSuccess(closeForm.value.type === "forceClose" ? "强制关闭成功" : "关闭成功");
        });
      } else {
        cancelWorkOrder(closeForm.value.workOrderId, closeForm.value.remark).then(() => {
          closeOpen.value = false;
          getList();
          proxy.$modal.msgSuccess("作废成功");
        });
      }
    }
  });
}

// ===== 强制关闭（短产关闭） =====
function handleForceClose(row) {
  const totalOutput = (row.qualifiedQty || 0) + (row.defectQty || 0);
  const shortQty = row.planQty - totalOutput;
  closeTitle.value = "强制关闭（短产）";
  closeLabel.value = "短产原因";
  closeForm.value = {
    workOrderId: row.workOrderId,
    workOrderNo: row.workOrderNo,
    orderType: row.orderType,
    productCode: row.productCode,
    productName: row.productName,
    planQty: row.planQty,
    unit: row.unit,
    finishedQty: row.finishedQty,
    qualifiedQty: row.qualifiedQty,
    defectQty: row.defectQty,
    status: row.status,
    remark: "",
    type: "forceClose",
    shortQty: shortQty
  };
  closeOpen.value = true;
}

// ===== 创建返工工单 =====
function handleRework(row) {
  reworkForm.value = {
    workOrderId: row.workOrderId,
    workOrderNo: row.workOrderNo,
    productCode: row.productCode,
    productName: row.productName,
    planQty: row.planQty,
    qualifiedQty: row.qualifiedQty,
    defectQty: row.defectQty,
    finishedQty: row.finishedQty,
    unit: row.unit,
    reworkQty: 1,
    reworkReason: ""
  };
  reworkOpen.value = true;
}

function submitRework() {
  proxy.$refs["reworkFormRef"].validate(valid => {
    if (valid) {
      createReworkOrder(reworkForm.value.workOrderId, reworkForm.value.reworkQty, reworkForm.value.reworkReason).then(() => {
        reworkOpen.value = false;
        getList();
        proxy.$modal.msgSuccess("返工工单创建成功，请到工单列表下达");
      });
    }
  });
}

function handleAuditLog(row) {
  getWorkOrderAuditLog(row.workOrderId).then(response => {
    auditLogList.value = response.data;
    logOpen.value = true;
  });
}

// ===== 齐套检查 =====
// 单工单齐套检查（行内按钮）
function handleKitCheck(row) {
  const loadingInstance = proxy.$loading({ text: '正在执行齐套检查...' });
  autoKitCheckByWorkOrderId(row.workOrderId).then(response => {
    loadingInstance.close();
    const kitId = response.kitId;
    // 获取齐套检查结果详情
    Promise.all([getKitCheck(kitId), getKitCheckDetail(kitId)]).then(([infoRes, detailRes]) => {
      kitCheckResultData.value = infoRes.data;
      kitCheckDetailList.value = detailRes.data || [];
      kitCheckResultOpen.value = true;
    });
    getList();
  }).catch(() => {
    loadingInstance.close();
  });
}

// 批量齐套检查（工具栏按钮）
function handleBatchKitCheck() {
  if (ids.value.length === 0) {
    proxy.$modal.msgWarning('请先选择需要检查的工单');
    return;
  }
  proxy.$modal.confirm('是否确认对选中的 ' + ids.value.length + ' 个工单进行齐套检查？').then(() => {
    const loadingInstance = proxy.$loading({ text: '正在执行批量齐套检查...' });
    batchAutoKitCheck(ids.value).then(response => {
      loadingInstance.close();
      proxy.$modal.msgSuccess(response.msg);
      getList();
    }).catch(() => {
      loadingInstance.close();
    });
  }).catch(() => {});
}

// 齐套检查结果辅助函数
function kitStatusBadgeClass(status) {
  const map = { '0': 'amber', '1': 'green', '2': 'red' };
  return map[status] || 'gray';
}
function kitStatusLabel(status) {
  const item = mms_kit_status.value ? mms_kit_status.value.find(d => d.value == status) : null;
  return item ? item.label : '—';
}
function kitRateClass(rate) {
  const r = parseFloat(rate);
  if (isNaN(r)) return 'rate-bad';
  if (r >= 100) return 'rate-good';
  if (r >= 80) return 'rate-warn';
  return 'rate-bad';
}
function kitRateStatusText(rate) {
  const r = parseFloat(rate);
  if (isNaN(r)) return '未知';
  if (r >= 100) return '齐套';
  if (r >= 80) return '部分齐套';
  return '不齐套';
}

// 下达弹窗内的齐套检查
function handleReleaseKitCheck() {
  const wo = releaseData.value.workOrder;
  if (!wo) return;
  const loadingInstance = proxy.$loading({ text: '正在执行齐套检查...' });
  autoKitCheckByWorkOrderId(wo.workOrderId).then(response => {
    loadingInstance.close();
    const kitId = response.kitId;
    Promise.all([getKitCheck(kitId), getKitCheckDetail(kitId)]).then(([infoRes, detailRes]) => {
      releaseKitCheckData.value = infoRes.data;
      releaseKitCheckDone.value = true;
    });
  }).catch(() => {
    loadingInstance.close();
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

/** 判断工单是否已完工（状态3=已完工, 4=已关闭） */
function isWorkOrderFinished(status) {
  return ['3', '4'].includes(String(status));
}

function statusLabel(status) {
  const item = mms_workorder_status.value.find(d => d.value == status);
  return item ? item.label : '—';
}

function badgeClass(status) {
  const map = {
    '0': 'amber',    // 新建
    '1': 'blue',     // 已下达
    '2': 'blue',     // 执行中
    '3': 'green',    // 已完工
    '4': 'gray',     // 已关闭
    '5': 'amber',    // 已暂停
    '6': 'red'       // 已作废
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
    '3': 'tab-done',
    '4': 'tab-void',
    '5': 'tab-draft',
    '6': 'tab-void'
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

// ===== 来源类型切换 =====
function handleSourceTypeChange() {
  // 切换来源类型时，清空所有关联字段和自动带出的数据
  form.value.mpsId = undefined
  form.value.mpsNo = undefined
  form.value.sourceOrderId = undefined
  form.value.sourceOrderNo = undefined
  form.value.demandNo = undefined
  // 清空选择计划/订单时自动带出的产品、BOM、工艺路线等数据
  form.value.productId = undefined
  form.value.productCode = undefined
  form.value.productName = undefined
  form.value.specModel = undefined
  form.value.unit = undefined
  form.value.planQty = undefined
  form.value.bomId = undefined
  form.value.bomNo = undefined
  form.value.routeId = undefined
  form.value.routeNo = undefined
  form.value.resourceId = undefined
  form.value.resourceName = undefined
  form.value.planStart = undefined
  form.value.planFinish = undefined
  form.value.priority = undefined
}

// ===== 关联销售订单选择器（订单+明细展平到单表） =====
const orderPickerOpen = ref(false)
const orderPickerLoading = ref(false)
const orderPickerFlatList = ref([])
const orderPickerTotal = ref(0)
const orderPickerSelectedId = ref(null)
const orderPickerSelectedRow = ref(null)
const orderPickerQuery = reactive({ pageNum: 1, pageSize: 10, orderNo: undefined, orderStatus: '2' })
const _orderGroupIndexMap = ref({})

function openOrderPicker() {
  orderPickerOpen.value = true
  orderPickerSelectedId.value = null
  orderPickerSelectedRow.value = null
  orderPickerFlatList.value = []
  _orderGroupIndexMap.value = {}
  orderPickerQuery.pageNum = 1
  orderPickerQuery.orderNo = undefined
  getOrderPickerList()
}

/** 查询订单列表 → 逐个加载明细 → 展平为行 */
function getOrderPickerList() {
  orderPickerLoading.value = true
  listOrder(orderPickerQuery).then(async res => {
    const orders = res.rows || []
    orderPickerTotal.value = res.total
    const flatRows = []
    for (const order of orders) {
      try {
        const detail = await getOrder(order.orderId)
        const items = (detail.data && detail.data.itemList) ? detail.data.itemList : []
        if (items.length === 0) {
          flatRows.push({
            _rowKey: order.orderId + '_0',
            orderId: order.orderId,
            orderNo: order.orderNo,
            customerName: order.customerName,
            orderAmount: order.orderAmount,
            customerId: order.customerId,
            lineNo: null, productName: null, productSpec: null, unit: null, quantity: null,
            _orderRowspan: 1
          })
        } else {
          items.forEach((item, idx) => {
            flatRows.push({
              _rowKey: order.orderId + '_' + item.itemId,
              orderId: order.orderId,
              orderNo: order.orderNo,
              customerName: order.customerName,
              orderAmount: order.orderAmount,
              customerId: order.customerId,
              itemId: item.itemId,
              lineNo: item.lineNo,
              materialId: item.materialId,
              materialCode: item.materialCode,
              productName: item.productName,
              productSpec: item.productSpec,
              unit: item.unit,
              quantity: item.quantity,
              _orderRowspan: idx === 0 ? items.length : 0
            })
          })
        }
      } catch (e) {
        // 单个订单加载失败，跳过
      }
    }
    orderPickerFlatList.value = flatRows
    orderPickerLoading.value = false
  }).catch(() => {
    orderPickerLoading.value = false
    orderPickerFlatList.value = []
  })
}

function handleOrderPickerQuery() { orderPickerQuery.pageNum = 1; getOrderPickerList() }
function resetOrderPickerQuery() { orderPickerQuery.orderNo = undefined; handleOrderPickerQuery() }

/** radio 点击 → 选中行 */
function onOrderRadioClick(row) { onOrderRowClick(row) }

/** 行样式：不同订单交替背景色 */
function orderRowClassName({ row }) {
  if (!row.orderId) return ''
  if (_orderGroupIndexMap.value[row.orderId] === undefined) {
    const usedIds = Object.keys(_orderGroupIndexMap.value)
    _orderGroupIndexMap.value[row.orderId] = usedIds.length % 2
  }
  return _orderGroupIndexMap.value[row.orderId] === 0 ? 'order-group-a' : 'order-group-b'
}

/** 行点击 → 选中（必须有明细行才算有效选择） */
function onOrderRowClick(row) {
  if (row.itemId == null) return
  orderPickerSelectedId.value = row._rowKey
  orderPickerSelectedRow.value = row
}
function onOrderRowDblClick(row) {
  if (row.itemId == null) return
  onOrderRowClick(row)
  handleOrderPickerConfirm()
}

/** 合并行：同一订单的 orderNo/customerName/orderAmount 合并显示 */
function onOrderSpanMethod({ row, columnIndex }) {
  if (columnIndex >= 1 && columnIndex <= 3) {
    if (row._orderRowspan > 0) return { rowspan: row._orderRowspan, colspan: 1 }
    return { rowspan: 0, colspan: 0 }
  }
}

/** 确认选择：一步带出订单号 + 产品 + 数量等信息 */
function handleOrderPickerConfirm() {
  if (!orderPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择订单明细行'); return }
  const row = orderPickerSelectedRow.value
  // 来源订单号
  form.value.sourceOrderId = row.orderId
  form.value.sourceOrderNo = row.orderNo
  // 产品信息（从订单明细行带出）
  if (row.materialId) form.value.productId = row.materialId
  if (row.materialCode) form.value.productCode = row.materialCode
  if (row.productName) form.value.productName = row.productName
  if (row.productSpec) form.value.specModel = row.productSpec
  if (row.unit) form.value.unit = row.unit
  if (row.quantity != null) form.value.planQty = Number(row.quantity)
  orderPickerOpen.value = false
  // 自动查询已发布的BOM和已审核的工艺路线
  if (row.materialId) {
    autoFillBomAndRoute(row.materialId, false)
  } else {
    proxy.$modal.msgSuccess('已带出订单关联信息，请确认并补充BOM、工艺路线等')
  }
}

function clearSourceOrder() {
  form.value.sourceOrderId = undefined
  form.value.sourceOrderNo = undefined
  // 清空由订单带出的数据
  form.value.productId = undefined
  form.value.productCode = undefined
  form.value.productName = undefined
  form.value.specModel = undefined
  form.value.unit = undefined
  form.value.planQty = undefined
  form.value.bomId = undefined
  form.value.bomNo = undefined
  form.value.routeId = undefined
  form.value.routeNo = undefined
  form.value.resourceId = undefined
  form.value.resourceName = undefined
  form.value.planStart = undefined
  form.value.planFinish = undefined
  form.value.priority = undefined
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
/* 齐套检查-下达弹窗内嵌结果样式 */
.kit-release-result { padding: 4px 0; }
.kit-release-summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px 24px; }
.kit-release-item { display: flex; align-items: center; gap: 8px; }
.kit-release-label { font-size: 13px; font-weight: 500; color: #6b7280; white-space: nowrap; min-width: 60px; }
.kit-release-value { font-size: 14px; font-weight: 500; color: #111827; }

/* ===== 订单选择弹窗：不同订单交替背景色 ===== */
.mms-workorder-page :deep(.order-group-a > td) { background-color: #fafbff !important; }
.mms-workorder-page :deep(.order-group-b > td) { background-color: #fff8f0 !important; }
.mms-workorder-page :deep(.el-table__body tr.order-group-a:hover > td) { background-color: #eef2ff !important; }
.mms-workorder-page :deep(.el-table__body tr.order-group-b:hover > td) { background-color: #fdf2e9 !important; }
.mms-workorder-page :deep(.el-table__body tr.current-row > td) { background-color: #e0e7ff !important; }

/* ===== 工单详情弹窗 Tabs ===== */
.wo-detail-tabs :deep(.el-tabs__header) { margin-bottom: 14px; }
.wo-detail-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #e2e8f0; }
.wo-detail-tabs :deep(.el-tabs__item) { font-size: 14px; font-weight: 600; color: #64748b; padding: 0 20px; }
.wo-detail-tabs :deep(.el-tabs__item.is-active) { color: #4f46e5; }
.wo-detail-tabs :deep(.el-tabs__active-bar) { background-color: #4f46e5; height: 2px; border-radius: 2px; }
.wo-detail-tabs :deep(.el-tab-pane) { min-height: 200px; }

/* ===== 进行中数量标注样式 ===== */
.qty-output-cell { display: inline-flex; align-items: baseline; gap: 4px; }
.qty-output-val { font-weight: 600; color: #475569; }
.qty-output-val.is-final { color: #16a34a; }
.qty-output-tag { font-size: 11px; color: #6366f1; background: #eef2ff; border-radius: 4px; padding: 1px 5px; font-weight: 600; }
.qty-in-progress { color: #6366f1; font-weight: 500; }
.rd-value--in-progress { color: #6366f1 !important; font-weight: 600; }
.rd-value-hint { font-size: 12px; color: #94a3b8; font-weight: 400; margin-left: 2px; }
/* 工序完成情况-数量颜色 */
.proc-qty--green { color: var(--green-700) !important; }
.proc-qty--red { color: var(--red-700) !important; }
.proc-qty--blue { color: var(--brand-600) !important; }
</style>