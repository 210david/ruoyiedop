<template>
  <div class="app-container mms-route-page">
    <!-- Filter Card -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>路线编号</label><div class="control"><el-input v-model="queryParams.routeNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>路线名称</label><div class="control"><el-input v-model="queryParams.routeName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产品编码</label><div class="control"><el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产品名称</label><div class="control"><el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_route_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>版本</label><div class="control"><el-input v-model="queryParams.version" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <!-- Table Section -->
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:route:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:route:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:route:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="info" plain icon="CopyDocument" :disabled="single" @click="handleCopy" v-hasPermi="['mms:route:copy']">复制</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:route:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_route_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table" @row-click="handleRowClick">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="路线编号" prop="routeNo" key="routeNo" :width="colWidth('routeNo', 140)" resizable v-if="columns.routeNo.visible" />
          <el-table-column label="路线名称" prop="routeName" key="routeName" :width="colWidth('routeName', 200)" resizable show-overflow-tooltip v-if="columns.routeName.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="版本" prop="version" key="version" :width="colWidth('version', 80)" resizable align="center" v-if="columns.version.visible" />
          <el-table-column label="工序数" key="processCount" :width="colWidth('processCount', 80)" resizable align="center" v-if="columns.processCount.visible"><template #default="scope"><el-tag size="small" type="info" round>{{ scope.row.processCount || 0 }}</el-tag></template></el-table-column>
          <el-table-column label="总工时(h)" prop="totalStdTime" key="totalStdTime" :width="colWidth('totalStdTime', 100)" resizable align="center" v-if="columns.totalStdTime.visible"><template #default="scope"><span>{{ scope.row.totalStdTime != null ? scope.row.totalStdTime : '-' }}</span></template></el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click.stop="handleView(scope.row)">详情</el-button>
              <el-button link type="primary" icon="Edit" @click.stop="handleUpdate(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1'" v-hasPermi="['mms:route:edit']">修改</el-button>
              <el-button link type="success" icon="CircleCheck" @click.stop="handleEnable(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '3'" v-hasPermi="['mms:route:status']">启用</el-button>
              <el-button link type="warning" icon="Check" @click.stop="handleAudit(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['mms:route:status']">审核</el-button>
              <el-button link type="danger" icon="CircleClose" @click.stop="handleDisable(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1' || scope.row.status === '2'" v-hasPermi="['mms:route:status']">停用</el-button>
              <el-button link type="info" icon="CopyDocument" @click.stop="handleCopy(scope.row)" v-hasPermi="['mms:route:copy']">复制</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></div><span class="rd-detail-header-title">{{ title }}</span><div class="rd-detail-header-sub" v-if="form.routeNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ form.routeNo }}</span></div></div></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></span>路线标识</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="路线编号" prop="routeNo"><el-input v-model="form.routeNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="路线名称" prop="routeName"><el-input v-model="form.routeName" placeholder="请输入路线名称" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="版本" prop="version"><el-input v-model="form.version" placeholder="如 1.0" /></el-form-item></el-col><el-col :span="8"><el-form-item label="状态"><el-tag :type="statusTagType(form.status)" effect="light">{{ statusLabel(form.status) }}</el-tag></el-form-item></el-col><el-col :span="8"><el-form-item label="总工时(h)"><el-input :model-value="form.totalStdTime || 0" disabled /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 3a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2z"/><path d="M3 7a2 2 0 0 1 2-2h5a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" readonly placeholder="请选择产品" @click="openProductPicker"><template #append><el-button icon="Search" @click="openProductPicker" /></template></el-input></el-form-item></el-col><el-col :span="16"><el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" placeholder="请选择产品" readonly /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="默认车间"><el-input v-model="form.defaultWorkshop" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="默认产线"><el-input v-model="form.defaultLine" placeholder="请输入" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="生效日期"><el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="失效日期"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/></svg></span>工序明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <div class="detail-toolbar"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddProcess">添加工序行</el-button><el-button type="success" plain icon="Sort" size="small" @click="handleSortProcess" :disabled="!form.processList || form.processList.length < 2">按序号排序</el-button><span class="detail-tip">点击工序编码选择已有工序</span></div>
              <el-table :data="form.processList" border size="small" class="detail-table" max-height="400">
                <el-table-column label="序号" width="70" align="center"><template #default="scope"><el-input-number v-model="scope.row.stepSeq" :min="1" :step="1" size="small" controls-position="right" style="width: 100%" /></template></el-table-column>
                <el-table-column label="工序编码" width="130"><template #default="scope"><el-input v-model="scope.row.processCode" readonly size="small" placeholder="选择工序" @click="openProcessPicker(scope.$index)"><template #append><el-button icon="Search" size="small" @click="openProcessPicker(scope.$index)" /></template></el-input></template></el-table-column>
                <el-table-column label="工序名称" min-width="120"><template #default="scope"><el-input v-model="scope.row.processName" size="small" /></template></el-table-column>
                <el-table-column label="标准工时(h)" width="110"><template #default="scope"><el-input-number v-model="scope.row.stdTime" :min="0" :precision="2" :step="0.5" size="small" controls-position="right" style="width: 100%" @change="recalcTotalStdTime" /></template></el-table-column>
                <el-table-column label="准备时间(h)" width="110"><template #default="scope"><el-input-number v-model="scope.row.prepTime" :min="0" :precision="2" :step="0.5" size="small" controls-position="right" style="width: 100%" /></template></el-table-column>
                <el-table-column label="关键" width="55" align="center"><template #default="scope"><el-checkbox v-model="scope.row.isKeyProcess" true-value="1" false-value="0" /></template></el-table-column>
                <el-table-column label="外协" width="55" align="center"><template #default="scope"><el-checkbox v-model="scope.row.isOutsource" true-value="1" false-value="0" /></template></el-table-column>
                <el-table-column label="产能单元" width="130"><template #default="scope"><el-input v-model="scope.row.resourceName" readonly size="small" placeholder="绑定资源" @click="openResourcePicker(scope.$index)"><template #append><el-button icon="Search" size="small" @click="openResourcePicker(scope.$index)" /></template></el-input></template></el-table-column>
                <el-table-column label="设备" width="120"><template #default="scope"><el-input v-model="scope.row.equipName" size="small" /></template></el-table-column>
                <el-table-column label="SOP编号" width="120"><template #default="scope"><el-input v-model="scope.row.sopNo" size="small" /></template></el-table-column>
                <el-table-column label="SOP名称" width="150"><template #default="scope"><el-input v-model="scope.row.sopName" size="small" /></template></el-table-column>
                <el-table-column label="参数" width="80" align="center"><template #default="scope"><el-button link type="primary" icon="Setting" size="small" @click="openParamEditor(scope.$index)">编辑</el-button></template></el-table-column>
                <el-table-column label="操作" width="60" align="center" fixed="right"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="handleDeleteProcess(scope.$index)" /></template></el-table-column>
              </el-table>
              <div v-if="!form.processList || form.processList.length === 0" class="empty-detail"><el-empty description="暂无工序" :image-size="60" /></div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>附加信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0"><el-row :gutter="20"><el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" /></el-form-item></el-col></el-row></div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></div><span class="rd-detail-header-title">工艺路线详情</span><div class="rd-detail-header-sub" v-if="viewData.routeNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.routeNo }}</span></div></div></template>
      <div class="rd-page" v-loading="viewLoading">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></span>路线标识</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">路线编号</span><div class="rd-value">{{ viewData.routeNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">路线名称</span><div class="rd-value">{{ viewData.routeName || '-' }}</div></div><div class="rd-item"><span class="rd-label">版本</span><div class="rd-value">{{ viewData.version || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span></div></div><div class="rd-item"><span class="rd-label">总标准工时</span><div class="rd-value">{{ viewData.totalStdTime != null ? viewData.totalStdTime + ' h' : '-' }}</div></div><div class="rd-item"><span class="rd-label">工序数</span><div class="rd-value">{{ viewData.processCount || 0 }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 3a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2z"/><path d="M3 7a2 2 0 0 1 2-2h5a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">默认车间</span><div class="rd-value">{{ viewData.defaultWorkshop || '-' }}</div></div><div class="rd-item"><span class="rd-label">默认产线</span><div class="rd-value">{{ viewData.defaultLine || '-' }}</div></div><div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ viewData.effectiveDate || '-' }}</div></div><div class="rd-item"><span class="rd-label">失效日期</span><div class="rd-value">{{ viewData.expireDate || '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/></svg></span>工序列表</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3">
            <el-table :data="viewData.processList" border size="small" class="detail-table">
              <el-table-column label="序号" prop="stepSeq" width="60" align="center" />
              <el-table-column label="工序编码" prop="processCode" width="120" />
              <el-table-column label="工序名称" prop="processName" min-width="120" show-overflow-tooltip />
              <el-table-column label="标准工时(h)" prop="stdTime" width="100" align="center" />
              <el-table-column label="准备时间(h)" prop="prepTime" width="100" align="center" />
              <el-table-column label="关键" width="55" align="center"><template #default="scope"><el-tag v-if="scope.row.isKeyProcess === '1'" size="small" type="danger">是</el-tag><span v-else>否</span></template></el-table-column>
              <el-table-column label="外协" width="55" align="center"><template #default="scope"><el-tag v-if="scope.row.isOutsource === '1'" size="small" type="warning">是</el-tag><span v-else>否</span></template></el-table-column>
              <el-table-column label="产能单元" prop="resourceName" width="120" show-overflow-tooltip />
              <el-table-column label="设备" prop="equipName" width="120" show-overflow-tooltip />
              <el-table-column label="SOP编号" prop="sopNo" width="100" show-overflow-tooltip />
              <el-table-column label="SOP名称" prop="sopName" min-width="120" show-overflow-tooltip />
            </el-table>
            <div v-if="!viewData.processList || viewData.processList.length === 0" class="empty-detail"><el-empty description="暂无工序" :image-size="60" /></div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><path d="M12 7v5l4 2"/></svg></span>版本变更记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4">
            <el-timeline><el-timeline-item v-for="log in versionLogs" :key="log.logId" :timestamp="log.changeTime" :type="logTimelineType(log.changeType)"><span style="font-weight:600">{{ changeTypeLabel(log.changeType) }}</span><span style="margin-left:8px;color:#909399">{{ log.changeDesc }}</span><span style="margin-left:8px;color:#c0c4cc">— {{ log.changeBy }}</span></el-timeline-item></el-timeline>
            <el-empty v-if="!versionLogs || versionLogs.length === 0" description="暂无变更记录" :image-size="60" />
          </div>
        </section>
      </div>
    </el-dialog>
    <!-- Process Picker -->
    <el-dialog v-model="processPickerOpen" width="800px" append-to-body draggable title="选择工序">
      <div style="margin-bottom:12px;display:flex;gap:8px;align-items:center"><el-input v-model="processPickerQuery.processCode" placeholder="工序编码" clearable size="small" style="width:160px" @keyup.enter="loadProcessPicker" /><el-input v-model="processPickerQuery.processName" placeholder="工序名称" clearable size="small" style="width:160px" @keyup.enter="loadProcessPicker" /><el-button type="primary" plain icon="Search" size="small" @click="loadProcessPicker">查询</el-button></div>
      <el-table v-loading="processPickerLoading" :data="processPickerList" border size="small" highlight-current-row @row-click="onProcessPickerRowClick" height="360"><el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="selectedProcessId" :value="row.processId" @click.stop><span /></el-radio></template></el-table-column><el-table-column label="工序编码" prop="processCode" width="140" /><el-table-column label="工序名称" prop="processName" min-width="180" /><el-table-column label="类型" prop="processType" width="80" /><el-table-column label="标准工时" prop="stdTime" width="100" align="center" /></el-table>
      <pagination v-show="processPickerTotal > 0" :total="processPickerTotal" v-model:page="processPickerQuery.pageNum" v-model:limit="processPickerQuery.pageSize" @pagination="loadProcessPicker" />
      <template #footer><el-button @click="processPickerOpen = false">取 消</el-button><el-button type="primary" @click="confirmProcessPicker" :disabled="!selectedProcessId">确 定</el-button></template>
    </el-dialog>
    <!-- Resource Picker -->
    <el-dialog v-model="resourcePickerOpen" width="800px" append-to-body draggable title="选择产能单元">
      <div style="margin-bottom:12px;display:flex;gap:8px;align-items:center"><el-input v-model="resourcePickerQuery.resourceCode" placeholder="编码" clearable size="small" style="width:160px" @keyup.enter="loadResourcePicker" /><el-input v-model="resourcePickerQuery.resourceName" placeholder="名称" clearable size="small" style="width:160px" @keyup.enter="loadResourcePicker" /><el-button type="primary" plain icon="Search" size="small" @click="loadResourcePicker">查询</el-button></div>
      <el-table v-loading="resourcePickerLoading" :data="resourcePickerList" border size="small" highlight-current-row @row-click="onResourcePickerRowClick" height="360"><el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="selectedResourceId" :value="row.resourceId" @click.stop><span /></el-radio></template></el-table-column><el-table-column label="编码" prop="resourceCode" width="140" /><el-table-column label="名称" prop="resourceName" min-width="180" /><el-table-column label="类型" prop="resourceType" width="100" /></el-table>
      <pagination v-show="resourcePickerTotal > 0" :total="resourcePickerTotal" v-model:page="resourcePickerQuery.pageNum" v-model:limit="resourcePickerQuery.pageSize" @pagination="loadResourcePicker" />
      <template #footer><el-button @click="resourcePickerOpen = false">取 消</el-button><el-button type="primary" @click="confirmResourcePicker" :disabled="!selectedResourceId">确 定</el-button></template>
    </el-dialog>
    <!-- Param Editor -->
    <el-dialog v-model="paramEditorOpen" width="700px" append-to-body draggable title="工艺参数模板编辑">
      <div style="margin-bottom:12px"><el-button type="primary" plain icon="Plus" size="small" @click="paramList.push({paramCode:'',paramName:'',stdValue:'',upperLimit:'',lowerLimit:'',unit:'',collectMode:'手动'})">添加参数</el-button></div>
      <el-table :data="paramList" border size="small" max-height="360">
        <el-table-column label="参数编码" width="120"><template #default="scope"><el-input v-model="scope.row.paramCode" size="small" /></template></el-table-column>
        <el-table-column label="参数名称" min-width="120"><template #default="scope"><el-input v-model="scope.row.paramName" size="small" /></template></el-table-column>
        <el-table-column label="标准值" width="100"><template #default="scope"><el-input v-model="scope.row.stdValue" size="small" /></template></el-table-column>
        <el-table-column label="上限" width="100"><template #default="scope"><el-input v-model="scope.row.upperLimit" size="small" /></template></el-table-column>
        <el-table-column label="下限" width="100"><template #default="scope"><el-input v-model="scope.row.lowerLimit" size="small" /></template></el-table-column>
        <el-table-column label="单位" width="70"><template #default="scope"><el-input v-model="scope.row.unit" size="small" /></template></el-table-column>
        <el-table-column label="采集方式" width="100"><template #default="scope"><el-select v-model="scope.row.collectMode" size="small" style="width:100%"><el-option label="手动" value="手动" /><el-option label="自动" value="自动" /></el-select></template></el-table-column>
        <el-table-column label="操作" width="60" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="paramList.splice(scope.$index, 1)" /></template></el-table-column>
      </el-table>
      <template #footer><el-button @click="paramEditorOpen = false">取 消</el-button><el-button type="primary" @click="saveParamTemplate">确 定</el-button></template>
    </el-dialog>
    <!-- Product Picker -->
    <material-picker ref="productPickerRef" title="选择产品" @confirm="onProductPickerConfirm" />
    <!-- Business Help -->
    <el-dialog v-model="showStatusHelp" title="工艺路线管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、工艺路线释义</h4>
        <div class="highlight-card highlight-primary"><div class="highlight-card-title">什么是工艺路线？</div><div class="highlight-card-body"><strong>工艺路线（Routing）</strong>是生产管控中定义产品生产加工路径的核心主数据。工艺路线规定了各工序的先后顺序、对应设备和标准工时，是工单排产和工序流转的基础依据。<br/><br/>工艺路线遵循 <strong>MES 标准化生产模型</strong>，通过版本管理支持不同工艺方案的并存与切换，已审核状态的路线方可被工单引用。</div></div>
        <h4>二、状态流转图</h4>
        <div class="status-flow"><div class="flow-item"><el-tag type="info">草稿</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">启用</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div><div class="flow-item"><el-tag type="primary">已启用</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">审核</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div><div class="flow-item"><el-tag type="success">已审核</el-tag></div></div>
        <div class="status-flow" style="margin-top:8px"><div class="flow-item"><el-tag type="info">草稿/已启用</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">停用</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div><div class="flow-item"><el-tag type="danger">已停用</el-tag></div></div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border><el-descriptions-item label="草稿">路线新建后的初始状态，可修改工序和属性</el-descriptions-item><el-descriptions-item label="已启用">路线已启用，可提交审核</el-descriptions-item><el-descriptions-item label="已审核">路线已审核通过，可被工单引用进行生产</el-descriptions-item><el-descriptions-item label="已停用">路线已停用，不可被新工单引用，可重新启用</el-descriptions-item></el-descriptions>
        <h4>四、业务操作流程</h4>
        <el-timeline><el-timeline-item type="primary" :hollow="true"><strong>创建路线：</strong>点击「新增」创建工艺路线，选择产品、填写版本号和工序信息</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>启用路线：</strong>草稿状态下点击「启用」使路线进入已启用状态</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>审核路线：</strong>已启用状态下点击「审核」，审核通过后路线可被工单引用</el-timeline-item><el-timeline-item type="danger" :hollow="true"><strong>停用路线：</strong>不再使用的路线可停用</el-timeline-item></el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup name="Route">
import { listRoute, getRoute, addRoute, updateRoute, delRoute, enableRoute, auditRoute, disableRoute, copyRoute, getRouteVersionLog } from "@/api/mms/route";
import { listProcess } from "@/api/mms/process";
import { listResource } from "@/api/mms/resource";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, CopyDocument, CircleCheck, CircleClose, Check, Sort, Setting } from '@element-plus/icons-vue'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2', 'c3', 'c0', 'vc1', 'vc2', 'vc3', 'vc4'])
const { proxy } = getCurrentInstance();
const { mms_route_status, mms_route_change_type } = proxy.useDict("mms_route_status", "mms_route_change_type");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_route_index')

const dataList = ref([]);
const open = ref(false);
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
const viewOpen = ref(false);
const viewLoading = ref(false);
const viewData = ref({});
const versionLogs = ref([]);

// Picker state
const processPickerOpen = ref(false);
const processPickerLoading = ref(false);
const processPickerList = ref([]);
const processPickerTotal = ref(0);
const processPickerQuery = reactive({ pageNum: 1, pageSize: 10, processCode: undefined, processName: undefined, status: '0' });
const selectedProcessId = ref(null);
const selectedProcessRow = ref(null);
const currentProcessIndex = ref(-1);

const resourcePickerOpen = ref(false);
const resourcePickerLoading = ref(false);
const resourcePickerList = ref([]);
const resourcePickerTotal = ref(0);
const resourcePickerQuery = reactive({ pageNum: 1, pageSize: 10, resourceCode: undefined, resourceName: undefined });
const selectedResourceId = ref(null);
const selectedResourceRow = ref(null);
const currentResourceIndex = ref(-1);

const paramEditorOpen = ref(false);
const paramList = ref([]);
const currentParamIndex = ref(-1);

const statusTabList = computed(() => mms_route_status.value ? mms_route_status.value.map(d => ({ label: d.label, value: d.value })) : []);

const defaultColumns = {
  routeNo: { label: '路线编号', visible: true },
  routeName: { label: '路线名称', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  version: { label: '版本', visible: true },
  processCount: { label: '工序数', visible: true },
  totalStdTime: { label: '总工时(h)', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_route_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.value.routeNo) count++;
  if (queryParams.value.routeName) count++;
  if (queryParams.value.productCode) count++;
  if (queryParams.value.productName) count++;
  if (queryParams.value.status) count++;
  if (queryParams.value.version) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, routeNo: undefined, routeName: undefined, productCode: undefined, productName: undefined, version: undefined, status: undefined, params: {} },
  rules: {
    routeName: [{ required: true, message: "请输入路线名称", trigger: "blur" }],
    productCode: [{ required: true, message: "请选择产品", trigger: "change" }],
    version: [{ required: true, message: "请输入版本号", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listRoute(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    updateStatusCounts(response.rows);
  });
}

function updateStatusCounts(rows) {
  const counts = { all: total.value };
  if (mms_route_status.value) {
    mms_route_status.value.forEach(d => {
      counts[d.value] = rows.filter(r => r.status === d.value).length;
    });
  }
  statusCounts.value = counts;
}

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.routeNo = undefined; queryParams.value.routeName = undefined; queryParams.value.productCode = undefined; queryParams.value.productName = undefined; queryParams.value.version = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.routeId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function handleRowClick(row) { /* 可扩展为行点击查看详情 */ }

function reset() {
  form.value = { routeId: null, routeNo: undefined, routeName: undefined, productId: undefined, productCode: undefined, productName: undefined, version: '1.0', effectiveDate: undefined, expireDate: undefined, status: '0', defaultWorkshop: undefined, defaultLine: undefined, totalStdTime: 0, remark: undefined, processList: [] };
  proxy.resetForm("formRef");
}

function handleAdd() { reset(); open.value = true; title.value = "新增工艺路线"; }

function handleUpdate(row) {
  reset();
  const id = row.routeId || ids.value[0];
  getRoute(id).then(response => {
    form.value = response.data;
    if (!form.value.processList) form.value.processList = [];
    open.value = true;
    title.value = "修改工艺路线";
  });
}

function handleView(row) {
  viewOpen.value = true; viewLoading.value = true; viewData.value = {};
  getRoute(row.routeId).then(response => {
    viewData.value = response.data;
    if (!viewData.value.processList) viewData.value.processList = [];
    viewLoading.value = false;
    getRouteVersionLog(row.routeId).then(res => { versionLogs.value = res.data || []; });
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.routeId != null) {
        updateRoute(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addRoute(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}

function cancel() { open.value = false; reset(); }

function handleDelete(row) {
  const delIds = row.routeId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的工艺路线？').then(() => delRoute(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

function handleExport() { proxy.download("mms/route/export", { ...queryParams.value }, `route_${new Date().getTime()}.xlsx`); }

function handleEnable(row) { proxy.$modal.confirm('是否确认启用工艺路线[' + row.routeNo + ']？').then(() => enableRoute(row.routeId)).then(() => { proxy.$modal.msgSuccess("启用成功"); getList(); }).catch(() => {}); }
function handleAudit(row) { proxy.$modal.confirm('是否确认审核工艺路线[' + row.routeNo + ']？审核通过后可被工单引用。').then(() => auditRoute(row.routeId)).then(() => { proxy.$modal.msgSuccess("审核成功"); getList(); }).catch(() => {}); }
function handleDisable(row) { proxy.$modal.confirm('是否确认停用工艺路线[' + row.routeNo + ']？停用后不可被新工单引用。').then(() => disableRoute(row.routeId)).then(() => { proxy.$modal.msgSuccess("停用成功"); getList(); }).catch(() => {}); }
function handleCopy(row) { proxy.$modal.confirm('是否确认复制工艺路线[' + row.routeNo + ']为新版本？').then(() => copyRoute(row.routeId)).then(() => { proxy.$modal.msgSuccess("复制成功"); getList(); }).catch(() => {}); }

// ===== Process Detail Operations =====
function handleAddProcess() {
  const seq = (form.value.processList.length + 1) * 10;
  form.value.processList.push({ id: null, processId: undefined, processCode: '', processName: '', stepSeq: seq, stdTime: 0, prepTime: 0, isKeyProcess: '0', isOutsource: '0', resourceId: undefined, resourceName: '', equipId: undefined, equipName: '', sopId: undefined, sopNo: '', sopName: '', paramTemplate: null, remark: undefined });
}
function handleDeleteProcess(index) { form.value.processList.splice(index, 1); recalcTotalStdTime(); }
function handleSortProcess() { form.value.processList.sort((a, b) => (a.stepSeq || 0) - (b.stepSeq || 0)); }
function recalcTotalStdTime() {
  let total = 0;
  form.value.processList.forEach(p => { if (p.stdTime) total += p.stdTime; });
  form.value.totalStdTime = Math.round(total * 100) / 100;
}

// ===== Process Picker =====
function openProcessPicker(index) {
  currentProcessIndex.value = index;
  processPickerOpen.value = true;
  processPickerQuery.pageNum = 1;
  processPickerQuery.processCode = undefined;
  processPickerQuery.processName = undefined;
  selectedProcessId.value = form.value.processList[index].processId || null;
  selectedProcessRow.value = null;
  loadProcessPicker();
}
function loadProcessPicker() {
  processPickerLoading.value = true;
  listProcess(processPickerQuery).then(res => {
    processPickerList.value = res.rows;
    processPickerTotal.value = res.total;
    processPickerLoading.value = false;
  });
}
function onProcessPickerRowClick(row) { selectedProcessId.value = row.processId; selectedProcessRow.value = row; }
function confirmProcessPicker() {
  if (!selectedProcessRow.value) return;
  const row = selectedProcessRow.value;
  const idx = currentProcessIndex.value;
  const p = form.value.processList[idx];
  p.processId = row.processId;
  p.processCode = row.processCode;
  p.processName = row.processName;
  p.stdTime = row.stdTime || 0;
  p.prepTime = row.prepTime || 0;
  p.isKeyProcess = row.isKeyProcess || '0';
  p.isOutsource = row.isOutsource || '0';
  recalcTotalStdTime();
  processPickerOpen.value = false;
}

// ===== Resource Picker =====
function openResourcePicker(index) {
  currentResourceIndex.value = index;
  resourcePickerOpen.value = true;
  resourcePickerQuery.pageNum = 1;
  resourcePickerQuery.resourceCode = undefined;
  resourcePickerQuery.resourceName = undefined;
  selectedResourceId.value = form.value.processList[index].resourceId || null;
  selectedResourceRow.value = null;
  loadResourcePicker();
}
function loadResourcePicker() {
  resourcePickerLoading.value = true;
  listResource(resourcePickerQuery).then(res => {
    resourcePickerList.value = res.rows;
    resourcePickerTotal.value = res.total;
    resourcePickerLoading.value = false;
  });
}
function onResourcePickerRowClick(row) { selectedResourceId.value = row.resourceId; selectedResourceRow.value = row; }
function confirmResourcePicker() {
  if (!selectedResourceRow.value) return;
  const row = selectedResourceRow.value;
  const p = form.value.processList[currentResourceIndex.value];
  p.resourceId = row.resourceId;
  p.resourceName = row.resourceName;
  resourcePickerOpen.value = false;
}

// ===== Param Editor =====
function openParamEditor(index) {
  currentParamIndex.value = index;
  const p = form.value.processList[index];
  try {
    paramList.value = p.paramTemplate ? JSON.parse(p.paramTemplate) : [];
  } catch (e) {
    paramList.value = [];
  }
  paramEditorOpen.value = true;
}
function saveParamTemplate() {
  const p = form.value.processList[currentParamIndex.value];
  p.paramTemplate = paramList.value.length > 0 ? JSON.stringify(paramList.value) : null;
  paramEditorOpen.value = false;
}

// ===== Product Picker =====
function openProductPicker() { proxy.$refs.productPickerRef.open(form.value.productId); }
function onProductPickerConfirm(material) {
  form.value.productId = material.materialId;
  form.value.productCode = material.materialCode;
  form.value.productName = material.materialName;
}

// ===== Dict helpers =====
function statusLabel(status) { if (!mms_route_status.value) return '-'; const item = mms_route_status.value.find(d => d.value == status); return item ? item.label : '-'; }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'gray' }; return map[status] || 'gray'; }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-void' }; return map[value] || ''; }
function statusTagType(status) { const map = { '0': 'info', '1': '', '2': 'success', '3': 'danger' }; return map[status] || 'info'; }
function logTimelineType(type) { const map = { create: 'primary', update: 'warning', enable: 'success', audit: 'success', disable: 'danger', copy: 'info' }; return map[type] || 'info'; }
function changeTypeLabel(type) { if (!mms_route_change_type.value) return type; const item = mms_route_change_type.value.find(d => d.value === type); return item ? item.label : type; }

getList();
</script>

<style scoped>
.mms-route-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-route-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-route-page .filter-card{padding:14px 20px 16px}
.mms-route-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-route-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-route-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-route-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-route-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-route-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-route-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-route-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-route-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-route-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-route-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-route-page .field{display:flex;flex-direction:column;gap:6px}
.mms-route-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-route-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-route-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-route-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-route-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-route-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-route-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-route-page .field .control :deep(.el-select){width:100%}
.mms-route-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-route-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-route-page .toolbar .left,.mms-route-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-route-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-route-page .table-wrap{overflow-x:auto}
.mms-route-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-route-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-route-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-route-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-route-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-route-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-route-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-route-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-route-page .badge.amber .dot{background:var(--amber-500)}
.mms-route-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-route-page .badge.blue .dot{background:var(--blue-500)}
.mms-route-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-route-page .badge.green .dot{background:var(--green-500)}
.mms-route-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-route-page .badge.gray .dot{background:var(--ink-400)}
.mms-route-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-route-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-route-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-route-page .tabs-track::-webkit-scrollbar{display:none}
.mms-route-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-route-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-route-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-route-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-route-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-route-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-route-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-route-page .status-tab.tab-draft .dot{background:var(--amber-500)}.mms-route-page .status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)}.mms-route-page .status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
.mms-route-page .status-tab.tab-audit .dot{background:var(--blue-500)}.mms-route-page .status-tab.tab-audit .count{background:var(--blue-50);color:var(--blue-700)}.mms-route-page .status-tab.is-active.tab-audit .count{background:var(--blue-500);color:#fff}
.mms-route-page .status-tab.tab-done .dot{background:var(--green-500)}.mms-route-page .status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)}.mms-route-page .status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
.mms-route-page .status-tab.tab-void .dot{background:var(--ink-400)}.mms-route-page .status-tab.tab-void .count{background:var(--ink-100);color:var(--ink-500)}.mms-route-page .status-tab.is-active.tab-void .count{background:var(--ink-400);color:#fff}
.mms-route-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-route-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-route-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.mms-route-page .detail-toolbar{display:flex;align-items:center;gap:8px;margin-bottom:12px}
.mms-route-page .detail-toolbar .detail-tip{font-size:13px;color:var(--ink-500)}
.mms-route-page .empty-detail{padding:20px 0}
.status-help-content{max-height:500px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{border-radius:8px;padding:16px;border:1px solid}
.status-help-content .highlight-card-title{font-size:14px;font-weight:600;margin-bottom:8px;display:flex;align-items:center}
.status-help-content .highlight-card-body{font-size:13px;color:#606266;line-height:1.6}
.status-help-content .highlight-primary{background-color:#ecf5ff;border-color:#a0cfff}.status-help-content .highlight-primary .highlight-card-title{color:#409eff}
.status-help-content .highlight-warning{background-color:#fdf6ec;border-color:#f5dab1}.status-help-content .highlight-warning .highlight-card-title{color:#e6a23c}
@media(max-width:1100px){.mms-route-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.mms-route-page .filter-card .filter-bar{grid-template-columns:1fr}}
</style>
