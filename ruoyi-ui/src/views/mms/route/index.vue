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
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><QuestionFilled /></el-icon><span>业务操作说明</span></button>
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
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="路线编号" prop="routeNo" key="routeNo" :width="colWidth('routeNo', 140)" resizable v-if="columns.routeNo.visible" />
          <el-table-column label="路线名称" prop="routeName" key="routeName" :width="colWidth('routeName', 200)" resizable show-overflow-tooltip v-if="columns.routeName.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="版本" prop="version" key="version" :width="colWidth('version', 80)" resizable align="center" v-if="columns.version.visible" />
          <el-table-column label="工序数" key="processCount" :width="colWidth('processCount', 80)" resizable align="center" v-if="columns.processCount.visible"><template #default="scope"><el-tag size="small" type="info" round>{{ scope.row.processCount || 0 }}</el-tag></template></el-table-column>
          <el-table-column label="总工时(h)" prop="totalStdTime" key="totalStdTime" :width="colWidth('totalStdTime', 100)" resizable align="center" v-if="columns.totalStdTime.visible"><template #default="scope"><span>{{ scope.row.totalStdTime != null ? scope.row.totalStdTime : '—' }}</span></template></el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click.stop="handleView(scope.row)">详情</el-button>
              <el-button link type="primary" icon="Edit" @click.stop="handleUpdate(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1' || scope.row.status === '4'" v-hasPermi="['mms:route:edit']">修改</el-button>
              <el-button link type="success" icon="CircleCheck" @click.stop="handleEnable(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '3' || scope.row.status === '4'" v-hasPermi="['mms:route:status']">提交审核</el-button>
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
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/></svg></span>工序明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <div class="detail-toolbar"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddProcess">添加工序</el-button><span class="detail-tip">拖拽行调整顺序 · 点击「并行」将工序与上方合并为并行执行</span></div>
              <div class="flow-canvas" v-if="form.processList && form.processList.length > 0">
                <template v-for="(group, gi) in processGroups" :key="gi">
                  <div class="flow-connector" v-if="gi > 0">
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
                        <div class="flow-card" v-for="(p, pi) in group" :key="p._uid || p.processCode + gi + pi">
                          <div class="flow-card-icon" :class="p.isKeyProcess === '1' ? 'key' : 'normal'"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
                          <div class="flow-card-text">
                            <div class="flow-card-name">{{ p.processName || '未命名' }}</div>
                            <div class="flow-card-code">{{ p.processCode || '—' }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
              <div class="flow-empty" v-else><el-empty description="暂无工序，请点击「添加工序」" :image-size="60" /></div>
              <el-table :data="form.processList" border size="small" class="detail-table process-edit-table" max-height="350" row-key="_uid">
                <el-table-column label="" width="36" align="center"><template #default><el-icon class="drag-handle" style="cursor:grab"><Rank /></el-icon></template></el-table-column>
                <el-table-column label="步骤" width="80" align="center"><template #default="scope"><div class="step-cell"><span class="step-num">{{ scope.row._stepGroupIdx + 1 }}</span><el-button v-if="scope.$index > 0 && canMergeParallel(scope.$index)" link type="warning" size="small" @click.stop="toggleParallel(scope.$index)">并行</el-button><el-button v-if="scope.row._isParallel" link type="primary" size="small" @click.stop="toggleParallel(scope.$index)">拆分</el-button></div></template></el-table-column>
                <el-table-column label="工序编码" width="130"><template #default="scope"><el-input v-model="scope.row.processCode" readonly size="small" placeholder="选择" @click="openProcessPicker(scope.$index)"><template v-if="scope.row.processCode" #append><el-button icon="CircleClose" size="small" @click.stop="clearProcess(scope.$index)" /></template><template v-else #append><el-button icon="Search" size="small" @click="openProcessPicker(scope.$index)" /></template></el-input></template></el-table-column>
                <el-table-column label="工序名称" min-width="120"><template #default="scope"><el-input v-model="scope.row.processName" size="small" /></template></el-table-column>
                <el-table-column label="产能单元" width="160"><template #default="scope"><el-input v-model="scope.row.resourceName" readonly size="small" placeholder="选择" @click="openResourcePicker(scope.$index)"><template v-if="scope.row.resourceName" #append><el-button icon="CircleClose" size="small" @click.stop="clearResource(scope.$index)" /></template><template v-else #append><el-button icon="Search" size="small" @click="openResourcePicker(scope.$index)" /></template></el-input></template></el-table-column>
                <el-table-column label="工时(h)" width="100"><template #default="scope"><el-input-number v-model="scope.row.stdTime" :min="0" :precision="2" :step="0.5" size="small" controls-position="right" style="width: 100%" @change="recalcTotalStdTime" /></template></el-table-column>
                <el-table-column label="准备(h)" width="100"><template #default="scope"><el-input-number v-model="scope.row.prepTime" :min="0" :precision="2" :step="0.5" size="small" controls-position="right" style="width: 100%" /></template></el-table-column>
                <el-table-column label="关键" width="50" align="center"><template #default="scope"><el-checkbox v-model="scope.row.isKeyProcess" true-value="1" false-value="0" /></template></el-table-column>
                <el-table-column label="外协" width="50" align="center"><template #default="scope"><el-checkbox v-model="scope.row.isOutsource" true-value="1" false-value="0" /></template></el-table-column>
                <el-table-column label="操作" width="50" align="center" fixed="right"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click.stop="handleDeleteProcess(scope.$index)" /></template></el-table-column>
              </el-table>
              <div v-if="!form.processList || form.processList.length === 0" class="empty-detail"><el-empty description="暂无工序" :image-size="60" /></div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>附加信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0"><el-row :gutter="20"><el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" /></el-form-item></el-col></el-row></div>
          </section>
          <section class="rd-card" v-if="form.status === '4'">
            <div class="rd-card-body" style="display:block">
              <el-alert type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该路线已被驳回，请修改后重新提交</template>
              </el-alert>
            </div>
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
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">路线编号</span><div class="rd-value">{{ viewData.routeNo || '—' }}</div></div><div class="rd-item"><span class="rd-label">路线名称</span><div class="rd-value">{{ viewData.routeName || '—' }}</div></div><div class="rd-item"><span class="rd-label">版本</span><div class="rd-value">{{ viewData.version || '—' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status" class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span><span v-else class="text-muted">—</span></div></div><div class="rd-item"><span class="rd-label">总标准工时</span><div class="rd-value">{{ viewData.totalStdTime != null ? viewData.totalStdTime + ' h' : '—' }}</div></div><div class="rd-item"><span class="rd-label">工序数</span><div class="rd-value">{{ viewData.processCount || 0 }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 3a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2z"/><path d="M3 7a2 2 0 0 1 2-2h5a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '—' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '—' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/></svg></span>工序列表</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3">
            <div class="flow-canvas" v-if="viewData.processList && viewData.processList.length > 0">
              <template v-for="(group, gi) in viewProcessGroups" :key="gi">
                <div class="flow-connector" v-if="gi > 0">
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
                      <div class="flow-card" v-for="(p, pi) in group" :key="pi">
                        <div class="flow-card-icon" :class="p.isKeyProcess === '1' ? 'key' : 'normal'"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
                        <div class="flow-card-text">
                          <div class="flow-card-name">{{ p.processName || '未命名' }}</div>
                          <div class="flow-card-code">{{ p.processCode || '—' }}</div>
                          <div class="flow-card-meta" v-if="p.resourceName">{{ p.resourceName }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
            <el-table :data="viewData.processList" border size="small" class="detail-table" style="margin-top:12px">
              <el-table-column label="序号" prop="stepSeq" width="60" align="center" />
              <el-table-column label="工序编码" prop="processCode" width="120" />
              <el-table-column label="工序名称" prop="processName" min-width="120" show-overflow-tooltip />
              <el-table-column label="产能单元" prop="resourceName" min-width="120" show-overflow-tooltip />
              <el-table-column label="标准工时(h)" prop="stdTime" width="100" align="center" />
              <el-table-column label="准备时间(h)" prop="prepTime" width="100" align="center" />
              <el-table-column label="关键" width="55" align="center"><template #default="scope"><el-tag v-if="scope.row.isKeyProcess === '1'" size="small" type="danger">是</el-tag><span v-else>否</span></template></el-table-column>
              <el-table-column label="外协" width="55" align="center"><template #default="scope"><el-tag v-if="scope.row.isOutsource === '1'" size="small" type="warning">是</el-tag><span v-else>否</span></template></el-table-column>
            </el-table>
            <div v-if="!viewData.processList || viewData.processList.length === 0" class="empty-detail"><el-empty description="暂无工序" :image-size="60" /></div>
          </div>
        </section>
        <!-- 版本变更记录（含审核意见） -->
        <section class="rd-card">
<div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><path d="M12 7v5l4 2"/></svg></span>版本变更记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
<div class="rd-card-body" v-show="!collapsedCards.vc4">
<el-alert v-if="viewData.status === '4'" type="warning" :closable="false" show-icon style="margin-bottom:12px">该路线已被驳回，请修改后重新提交审核</el-alert>
<el-timeline><el-timeline-item v-for="log in versionLogs" :key="log.logId" :timestamp="log.changeTime" :type="logTimelineType(log.changeType)"><div style="display:flex;align-items:center;flex-wrap:wrap;gap:4px"><span style="font-weight:600">{{ log.changeDesc }}</span><el-tag v-if="log.changeType === 'audit' && log.newStatus === '2'" type="success" size="small" effect="light" round>通过</el-tag><el-tag v-else-if="log.changeType === 'audit' && log.newStatus === '4'" type="danger" size="small" effect="light" round>驳回</el-tag><span style="margin-left:4px;color:#c0c4cc">— {{ log.changeBy }}</span></div><div v-if="log.auditRemark" style="margin-top:4px;color:#606266;font-size:13px;line-height:1.5;background:var(--ink-50);border-radius:4px;padding:6px 10px">审核意见：{{ log.auditRemark }}</div></el-timeline-item></el-timeline>
<el-empty v-if="!versionLogs || versionLogs.length === 0" description="暂无变更记录" :image-size="60" />
</div>
</section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="auditOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">工艺路线审批</span>
          <div class="rd-detail-header-sub" v-if="auditData.routeNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.routeNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page" v-loading="auditLoading">
        <!-- 信息摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></span>路线信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">路线编号</span><div class="rd-value">{{ auditData.routeNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">路线名称</span><div class="rd-value">{{ auditData.routeName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ auditData.productCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ auditData.productName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">版本</span><div class="rd-value">{{ auditData.version || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">总工时(h)</span><div class="rd-value">{{ auditData.totalStdTime != null ? auditData.totalStdTime : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工序数</span><div class="rd-value">{{ auditData.processCount || 0 }}</div></div>
            </div>
          </div>
        </section>
        <!-- 工序列表摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/></svg></span>工序列表</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="flow-canvas" v-if="auditData.processList && auditData.processList.length > 0">
              <template v-for="(group, gi) in auditProcessGroups" :key="gi">
                <div class="flow-connector" v-if="gi > 0">
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
                      <div class="flow-card" v-for="(p, pi) in group" :key="pi">
                        <div class="flow-card-icon" :class="p.isKeyProcess === '1' ? 'key' : 'normal'"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
                        <div class="flow-card-text">
                          <div class="flow-card-name">{{ p.processName || '未命名' }}</div>
                          <div class="flow-card-code">{{ p.processCode || '—' }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
            <el-table :data="auditData.processList" border size="small" style="margin-top:12px">
              <el-table-column label="序号" prop="stepSeq" width="60" align="center" />
              <el-table-column label="工序编码" prop="processCode" width="120" />
              <el-table-column label="工序名称" prop="processName" min-width="120" show-overflow-tooltip />
              <el-table-column label="产能单元" prop="resourceName" min-width="120" show-overflow-tooltip />
              <el-table-column label="标准工时(h)" prop="stdTime" width="100" align="center" />
              <el-table-column label="关键" width="55" align="center"><template #default="scope"><el-tag v-if="scope.row.isKeyProcess === '1'" size="small" type="danger">是</el-tag><span v-else>否</span></template></el-table-column>
              <el-table-column label="外协" width="55" align="center"><template #default="scope"><el-tag v-if="scope.row.isOutsource === '1'" size="small" type="warning">是</el-tag><span v-else>否</span></template></el-table-column>
            </el-table>
          </div>
        </section>
        <!-- 历史审核记录 -->
        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aOpinion')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aOpinion }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.aOpinion">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
              <el-form-item label="审批意见" prop="auditRemark">
                <el-input v-model="auditForm.auditRemark" type="textarea" :rows="4" placeholder="请输入审批意见" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="submitAudit(true)">通 过</el-button>
        <el-button type="danger" @click="submitAudit(false)">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>
    <!-- Process Picker -->
    <el-dialog v-model="processPickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 20h20M4 20V8l8-5 8 5v12M9 20v-6h6v6"/></svg></div>
          <span class="rd-detail-header-title">选择工序</span>
        </div>
      </template>
      <div class="rd-page" style="padding:0">
        <div style="margin-bottom:12px;display:flex;gap:8px;align-items:center">
          <el-input v-model="processPickerQuery.processCode" placeholder="工序编码" clearable size="small" style="width:160px" @keyup.enter="loadProcessPicker">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-input v-model="processPickerQuery.processName" placeholder="工序名称" clearable size="small" style="width:160px" @keyup.enter="loadProcessPicker" />
          <el-select v-model="processPickerQuery.processType" placeholder="类型" clearable size="small" style="width:140px" @change="loadProcessPicker">
            <el-option v-for="d in mms_process_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
          <el-button type="primary" plain icon="Search" size="small" @click="loadProcessPicker">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetProcessPickerQuery">重置</el-button>
        </div>
        <el-table v-loading="processPickerLoading" :data="processPickerList" border size="small" highlight-current-row :row-class-name="processPickerRowClass" @row-click="onProcessPickerRowClick" height="360">
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio v-model="selectedProcessId" :value="row.processId" @change="onProcessPickerRadioChange(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="工序编码" prop="processCode" width="140" />
          <el-table-column label="工序名称" prop="processName" min-width="160" />
          <el-table-column label="类型" prop="processType" width="100" align="center">
            <template #default="scope">
              <span v-if="scope.row.processType" class="badge blue"><span class="dot"></span>{{ dictLabel(mms_process_type, scope.row.processType) }}</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="标准工时" prop="stdTime" width="100" align="center">
            <template #default="scope">
              <span>{{ scope.row.stdTime != null ? scope.row.stdTime + ' h' : '—' }}</span>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="processPickerTotal > 0" :total="processPickerTotal" v-model:page="processPickerQuery.pageNum" v-model:limit="processPickerQuery.pageSize" @pagination="loadProcessPicker" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="processPickerOpen = false">取 消</el-button>
          <el-button type="primary" @click="confirmProcessPicker" :disabled="!selectedProcessId">确 定</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- Resource Picker -->
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
        <el-input v-model="resourcePickerQuery.lineName" placeholder="产线" clearable size="small" style="width:160px" @keyup.enter="handleResourcePickerQuery" />
        <el-button type="primary" plain icon="Search" size="small" @click="handleResourcePickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetResourcePickerQuery">重置</el-button>
      </div>
      <el-table v-loading="resourcePickerLoading" :data="resourcePickerList" highlight-current-row @row-click="onResourceRowClick" @row-dblclick="onResourceRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="resourcePickerSelectedId" :value="row.resourceId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="资源编码" prop="resourceCode" width="130" show-overflow-tooltip />
        <el-table-column label="产能单元" prop="resourceName" min-width="150" show-overflow-tooltip />
        <el-table-column label="产线" prop="lineName" width="100" show-overflow-tooltip />
        <el-table-column label="车间" prop="workshopName" width="100" show-overflow-tooltip />
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="resourcePickerQuery.pageNum" v-model:page-size="resourcePickerQuery.pageSize" :total="resourcePickerTotal" layout="total, prev, pager, next" small @current-change="getResourcePickerList" />
      </div>
      <template #footer><el-button @click="resourcePickerOpen = false">取 消</el-button><el-button type="primary" @click="confirmResourcePicker" :disabled="!resourcePickerSelectedId">确 定</el-button></template>
    </el-dialog>
    <!-- Product Picker -->
    <material-picker ref="productPickerRef" title="选择产品" @confirm="onProductPickerConfirm" />
    <!-- 业务操作说明 -->
    <el-dialog v-model="showStatusHelp" title="工艺路线管理业务操作说明" width="820px" append-to-body draggable class="rd-dialog status-help-dialog">
      <div class="status-help-content">
        <!-- 一、工艺路线释义 -->
        <h4>一、工艺路线释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是工艺路线？</div>
          <div class="highlight-card-body">
            <strong>工艺路线（Routing）</strong>是生产管控中定义产品生产加工路径的核心主数据。工艺路线规定了各工序的先后顺序、对应产能单元、标准工时和准备时间，是工单排产、工序流转和产能计算的基础依据。<br/><br/>
            工艺路线遵循<strong>MES标准化生产模型</strong>，通过版本管理支持不同工艺方案的并存与切换。已审核状态的路线方可被工单引用，确保生产过程使用经过验证的标准工艺。工艺路线与BOM共同构成生产管控的两大核心主数据：BOM定义"用什么料"，工艺路线定义"怎么造"。
          </div>
        </div>
        <div class="highlight-card highlight-success" style="margin-top: 12px;">
          <div class="highlight-card-title">工艺路线的核心组成</div>
          <div class="highlight-card-body">
            <p>• <strong>工序序列：</strong>按生产先后顺序排列的加工步骤，支持串行和并行排列，每个工序包含编码、名称、标准工时等</p>
            <p>• <strong>产能单元：</strong>每个工序可绑定具体的产能单元（设备/产线），用于排产时的资源分配</p>
            <p>• <strong>标准工时：</strong>每个工序的标准加工时间，用于计算总工时和排产计划</p>
            <p>• <strong>关键/外协标记：</strong>标记关键工序和外协工序，便于排产和产能管理</p>
            <p>• <strong>流程图可视化：</strong>系统自动生成横向流程图，直观展示串行/并行工序关系</p>
          </div>
        </div>

        <!-- 二、工艺路线状态流转图 -->
        <h4>二、工艺路线状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「提交审核」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「审核」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已审核</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="primary">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="danger">审核驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">修改后重新提交</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待审核</el-tag>
            <span style="font-size: 12px; color: #909399; margin-left: 4px;">（可重新审核）</span>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-tag size="small" type="primary">或</el-tag>
            <el-tag type="primary">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「停用」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已停用</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="danger">已停用</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「提交审核」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待审核</el-tag>
            <el-tag size="small" type="info">可重新审核</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">路线新建后的初始状态。可修改路线信息、维护工序列表、提交审核或删除。点击「提交审核」后进入待审核状态，可继续修改</el-descriptions-item>
          <el-descriptions-item label="待审核">路线已提交审核，等待审核人审批。此状态下仍可修改工序和属性，方便调整完善。点击「审核」后进入已审核状态</el-descriptions-item>
          <el-descriptions-item label="已审核">路线已审核通过，可被生产工单引用进行排产和工序流转。已审核状态不可直接修改，如需调整请先「复制」创建新版本</el-descriptions-item>
          <el-descriptions-item label="已停用">路线已停用，不可被新工单引用，但历史关联工单仍保留。可重新「提交审核」恢复为待审核状态</el-descriptions-item>
          <el-descriptions-item label="已驳回">审核人驳回路线。可修改内容后点击「提交审核」重新提交，或直接「提交审核」再次送审。修改后状态自动重置为待审核，审核日志保留</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">路线标识区</div>
          <div class="highlight-card-body">
            <p>• <strong>路线编号：</strong>路线的唯一标识编号，保存后由系统自动生成，无需手工输入</p>
            <p>• <strong>路线名称：</strong>路线的描述性名称，建议包含产品名+工艺特征，如"产品A-标准工艺路线"<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>版本：</strong>路线的版本号，用于版本管理。可通过「复制」创建新版本<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>状态：</strong>当前路线状态（草稿/待审核/已审核/已停用/已驳回），以标签形式只读展示</p>
            <p>• <strong>总工时(h)：</strong>所有工序标准工时之和，系统自动计算，不可手动修改</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">产品信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>产品编码：</strong>从物料库中选择该路线对应的产品，点击输入框右侧搜索按钮弹出物料选择器，选择后自动带出产品名称<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>产品名称：</strong>选择产品后自动带出，不可手动编辑</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">工序明细区</div>
          <div class="highlight-card-body">
            <p>• <strong>添加工序：</strong>点击「添加工序」按钮新增一行工序，新增后点击工序编码右侧搜索按钮从工序库中选择已有工序</p>
            <p>• <strong>拖拽排序：</strong>按住工序行左侧拖拽手柄图标（⋮），上下拖动可调整工序排列顺序，步骤序号自动重排</p>
            <p>• <strong>串行/并行：</strong>点击工序行「并行」按钮，可将该工序与上方工序合并为并行执行组（同一步骤序号）；再次点击「拆分」可恢复为串行</p>
            <p>• <strong>流程图可视化：</strong>工序明细区上方自动生成横向流程图，蓝色圆形节点表示串行步骤，橙色节点表示并行组，直观展示工序间逻辑关系</p>
            <p>• <strong>工序编码：</strong>从工序库中选择已有工序，选择后自动带出工序名称、标准工时、准备时间等<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>工序名称：</strong>选择工序后自动带出，可手动修改为更具体的名称</p>
            <p>• <strong>产能单元：</strong>点击输入框右侧搜索按钮从产能单元库中选择该工序对应的设备/产线资源。可选择资源编码、产能单元名称、所属产线和车间。未选择产能单元的工序在排产时将使用工单头上的产能单元</p>
            <p>• <strong>标准工时(h)：</strong>该工序的标准加工时间，用于计算总工时和排产计划。修改后自动重算总工时<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>准备时间(h)：</strong>该工序的换线/准备时间，排产时计入工序总耗时</p>
            <p>• <strong>关键：</strong>勾选表示该工序为关键工序（瓶颈工序），排产时优先保障产能</p>
            <p>• <strong>外协：</strong>勾选表示该工序为外协工序，需发送给外部供应商加工</p>
            <p>• <strong>删除工序：</strong>点击行尾删除按钮移除该工序，系统保留至少一个工序行，不允许全部删除</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">附加信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>备注：</strong>填写路线的补充说明信息，如特殊工艺要求、注意事项等</p>
          </div>
        </div>
        <div class="highlight-card highlight-danger" style="margin-top: 12px;">
          <div class="highlight-card-title">驳回提醒</div>
          <div class="highlight-card-body">
            <p>• 当路线状态为「已驳回」时，编辑表单顶部会显示橙色警告提示：<strong>"该路线已被驳回，请修改后重新提交"</strong>，提醒用户修改内容后重新提交审核</p>
          </div>
        </div>

        <!-- 五、核心业务规则 -->
        <h4>五、核心业务规则</h4>
        <div class="highlight-card highlight-danger">
          <div class="highlight-card-title">关键规则</div>
          <div class="highlight-card-body">
            <p>• <strong>已审核不可修改：</strong>路线状态为已审核后，所有字段均不可修改。如需调整，请先「复制」创建新版本，修改后重新提交审核</p>
            <p>• <strong>工单引用校验：</strong>只有已审核状态的路线才能被生产工单引用。草稿、待审核、已停用、已驳回状态的路线不可被工单引用</p>
            <p>• <strong>总工时自动计算：</strong>总工时 = 所有工序标准工时之和，系统在添加、删除或修改工序工时后自动重算，不可手动修改</p>
            <p>• <strong>版本管理：</strong>通过「复制」功能创建新版本路线，新版本独立于原版本，可独立修改和审核。同一产品可有多个版本的路线</p>
            <p>• <strong>产能单元校验：</strong>提交时若存在未选择产能单元的工序，系统将弹窗提示，未选择产能单元的工序在排产时将使用工单头上的产能单元</p>
            <p>• <strong>至少一道工序：</strong>提交时工序明细至少需要保留一个工序，系统不允许删除最后一个工序行</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">工序规则</div>
          <div class="highlight-card-body">
            <p>• <strong>工序来源：</strong>工序列表中的工序从工序库中选择，确保工序编码和标准的统一性</p>
            <p>• <strong>并行工序：</strong>同一步骤序号的工序为并行执行，表示这些工序可同时加工，排产时并行分配资源</p>
            <p>• <strong>关键工序：</strong>关键工序在排产时优先保障产能，系统在产能负荷分析时重点关注关键工序</p>
            <p>• <strong>外协工序：</strong>外协工序在工单下发时生成外协采购需求，不计入内部产能</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>合理设置标准工时和准备时间是准确排产的基础，建议定期根据实际生产数据校准标准工时</p>
          </div>
        </div>

        <!-- 六、详情查看与版本变更记录 -->
        <h4>六、详情查看与版本变更记录</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">详情弹窗</div>
          <div class="highlight-card-body">
            <p>• 点击列表行操作列「详情」按钮，打开路线详情弹窗</p>
            <p>• 详情弹窗包含：<strong>路线标识</strong>（编号、名称、版本、状态、总工时、工序数）、<strong>产品信息</strong>（编码、名称）、<strong>工序列表</strong>（含流程图可视化展示和工序明细表格）、<strong>版本变更记录</strong>（时间轴展示）</p>
            <p>• 所有卡片支持折叠/展开，点击卡片标题栏即可切换</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">版本变更记录</div>
          <div class="highlight-card-body">
            <p>• 版本变更记录以<strong>时间轴</strong>形式展示路线的全部操作历史</p>
            <p>• 记录类型包括：创建、修改、提交审核、审核通过、审核驳回、停用、复制等</p>
            <p>• 审核记录会显示审核意见内容，驳回记录以红色标签标识，通过记录以绿色标签标识</p>
            <p>• 若路线状态为「已驳回」，变更记录区顶部会显示橙色提醒：该路线已被驳回，请修改后重新提交审核</p>
          </div>
        </div>

        <!-- 七、审核操作说明 -->
        <h4>七、审核操作说明</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">审核流程</div>
          <div class="highlight-card-body">
            <p>• 当路线状态为「待审核」时，行操作列会出现「审核」按钮，点击后进入<strong>审批弹窗</strong></p>
            <p>• 审批弹窗展示：<strong>路线信息摘要</strong>（编号、名称、产品、版本、总工时、工序数）、<strong>工序列表</strong>（含流程图可视化展示和工序明细表格，含关键/外协标记）</p>
            <p>• 在「审批意见」区填写审批意见（必填，最多500字），点击底部按钮：</p>
            <p style="padding-left: 16px;">○ 点击「通过」：路线状态变为<strong>已审核</strong>，可被生产工单引用</p>
            <p style="padding-left: 16px;">○ 点击「驳回」：路线状态变为<strong>已驳回</strong>，可修改后重新提交审核</p>
            <p>• 审核操作均会记录在版本变更记录中，保留审核意见</p>
          </div>
        </div>

        <!-- 八、业务操作流程 -->
        <h4>八、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建路线：</strong>点击「新增」创建工艺路线，填写路线名称、版本号，点击产品编码搜索按钮从物料库中选择产品，路线编号保存后自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>维护工序：</strong>在工序明细区点击「添加工序」新增工序行，点击工序编码搜索按钮从工序库选择工序，点击产能单元搜索按钮选择对应资源。通过拖拽手柄调整工序顺序，点击「并行」按钮可设置并行工序组。修改标准工时后总工时自动重算，设置关键/外协标记
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交审核：</strong>确认工序信息无误后点击「提交审核」，路线进入待审核状态，仍可继续调整完善
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审核路线：</strong>待审核状态下点击「审核」，在审批弹窗中查看路线信息摘要和工序列表（含流程图），填写审批意见后点击「通过」或「驳回」。审核通过后路线状态变为已审核，可被生产工单引用进行排产；审核驳回后状态变为已驳回，可修改后重新提交
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>复制新版本：</strong>如需修改已审核的路线，点击「复制」创建新版本，修改后重新提交审核。新版本独立于原版本，可独立修改和审核
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>停用路线：</strong>不再使用的路线可点击「停用」，停用后不可被新工单引用，可随时重新提交审核恢复
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>查看详情：</strong>点击「详情」可查看路线完整信息，包括路线标识、产品信息、工序列表（含流程图可视化展示）和版本变更记录时间轴
          </el-timeline-item>
        </el-timeline>

        <!-- 九、筛选与列表操作 -->
        <h4>九、筛选与列表操作</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">筛选与搜索</div>
          <div class="highlight-card-body">
            <p>• <strong>基础筛选：</strong>支持按路线编号、路线名称、产品编码、产品名称快速搜索，输入后回车或点击「搜索」按钮查询</p>
            <p>• <strong>高级筛选：</strong>点击「高级筛选」展开更多条件，包括状态（下拉选择）、版本号、创建日期范围（日期区间选择器）</p>
            <p>• <strong>重置：</strong>点击「重置」按钮清空所有筛选条件并重新查询</p>
            <p>• <strong>状态标签页：</strong>列表顶部提供状态标签快捷切换（全部/草稿/待审核/已审核/已停用/已驳回），各标签显示对应状态的记录数</p>
          </div>
        </div>
        <div class="highlight-card highlight-success" style="margin-top: 12px;">
          <div class="highlight-card-title">列表操作</div>
          <div class="highlight-card-body">
            <p>• <strong>列设置：</strong>点击右上角工具栏的列设置图标，可自定义显示/隐藏表格列（路线编号、路线名称、产品编码、产品名称、版本、工序数、总工时、状态、创建时间）</p>
            <p>• <strong>列宽调整：</strong>表格列头支持拖拽调整列宽，设置自动保存到本地</p>
            <p>• <strong>导出：</strong>点击「导出」按钮可将当前筛选结果导出为Excel文件</p>
            <p>• <strong>批量删除：</strong>勾选多行后点击「删除」按钮可批量删除选中路线</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
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
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, QuestionFilled, CopyDocument, CircleCheck, CircleClose, Check, Sort, Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2', 'c3', 'c0', 'cAudit', 'vc1', 'vc2', 'vc3', 'vcAudit', 'vc4', 'vc5', 'aAudit', 'aOpinion'])
const { proxy } = getCurrentInstance();
const { mms_route_status, mms_route_change_type, mms_process_type } = proxy.useDict("mms_route_status", "mms_route_change_type", "mms_process_type");
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

// Audit dialog state
const auditOpen = ref(false);
const auditLoading = ref(false);
const auditData = ref({});
const auditForm = ref({ routeId: null, auditRemark: undefined });
const auditRules = {
  auditRemark: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
};

// Picker state
const processPickerOpen = ref(false);
const processPickerLoading = ref(false);
const processPickerList = ref([]);
const processPickerTotal = ref(0);
const processPickerQuery = reactive({ pageNum: 1, pageSize: 10, processCode: undefined, processName: undefined, processType: undefined, status: '0' });
const selectedProcessId = ref(null);
const selectedProcessRow = ref(null);
const currentProcessIndex = ref(-1);

// Resource Picker state
const resourcePickerOpen = ref(false);
const resourcePickerLoading = ref(false);
const resourcePickerList = ref([]);
const resourcePickerTotal = ref(0);
const resourcePickerQuery = reactive({ pageNum: 1, pageSize: 10, resourceName: undefined, lineName: undefined, status: '0' });
const resourcePickerSelectedId = ref(null);
const resourcePickerSelectedRow = ref(null);

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
    loadStatusCounts();
  });
}

function loadStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 };
  if (queryParams.value.routeNo) baseQuery.routeNo = queryParams.value.routeNo;
  if (queryParams.value.routeName) baseQuery.routeName = queryParams.value.routeName;
  if (queryParams.value.productCode) baseQuery.productCode = queryParams.value.productCode;
  if (queryParams.value.productName) baseQuery.productName = queryParams.value.productName;
  if (queryParams.value.version) baseQuery.version = queryParams.value.version;
  listRoute(proxy.addDateRange(baseQuery, dateRange.value)).then(res => {
    const counts = { all: res.total };
    if (mms_route_status.value) {
      mms_route_status.value.forEach(d => { counts[d.value] = 0; });
      (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; });
    }
    statusCounts.value = counts;
  }).catch(() => {});
}

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.routeNo = undefined; queryParams.value.routeName = undefined; queryParams.value.productCode = undefined; queryParams.value.productName = undefined; queryParams.value.version = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.routeId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function handleRowClick(row) { /* 可扩展为行点击查看详情 */ }

function reset() {
  form.value = { routeId: null, routeNo: undefined, routeName: undefined, productId: undefined, productCode: undefined, productName: undefined, version: '1.0', status: '0', totalStdTime: 0, remark: undefined, processList: [] };
  proxy.resetForm("formRef");
}

function handleAdd() { reset(); handleAddProcess(); open.value = true; title.value = "新增工艺路线"; initSortable(); }

function handleUpdate(row) {
  reset();
  const id = row.routeId || ids.value[0];
  getRoute(id).then(response => {
    form.value = response.data;
    if (!form.value.processList) form.value.processList = [];
    if (form.value.processList.length === 0) { handleAddProcess(); }
    form.value.processList.sort((a, b) => (a.stepSeq || 0) - (b.stepSeq || 0));
    form.value.processList.forEach((p, i) => {
      p._uid = genUid();
      p._isParallel = false;
      p._stepGroupIdx = i;
    });
    for (let i = 1; i < form.value.processList.length; i++) {
      if (form.value.processList[i].stepSeq === form.value.processList[i - 1].stepSeq) {
        form.value.processList[i]._isParallel = true;
      }
    }
    syncStepGroups();
    open.value = true;
    title.value = "修改工艺路线";
    initSortable();
  });
}

function handleView(row) {
  viewOpen.value = true; viewLoading.value = true; viewData.value = {};
  getRoute(row.routeId).then(response => {
    viewData.value = response.data;
    if (!viewData.value.processList) viewData.value.processList = [];
    // 无数据时默认收缩卡片
    collapsedCards.vc3 = viewData.value.processList.length === 0;
    viewLoading.value = false;
    getRouteVersionLog(row.routeId).then(res => {
      versionLogs.value = res.data || [];
      collapsedCards.vc4 = versionLogs.value.length === 0;
    });
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (!form.value.processList || form.value.processList.length === 0) {
        proxy.$modal.msgWarning('工序明细至少需要添加一个工序');
        return;
      }
      const missing = form.value.processList.filter(p => !p.resourceId);
      if (missing.length > 0) {
        proxy.$modal.confirm(`有 ${missing.length} 道工序未选择产能单元，未选择产能单元的工序在排产时将使用工单头上的产能单元。是否继续提交？`).then(() => {
          doSubmit();
        }).catch(() => {});
        return;
      }
      doSubmit();
    }
  });
}
function doSubmit() {
  if (form.value.routeId != null) {
    updateRoute(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
  } else {
    addRoute(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
  }
}

function cancel() { open.value = false; reset(); }

function handleDelete(row) {
  const delIds = row.routeId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的工艺路线？').then(() => delRoute(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

function handleExport() { proxy.download("mms/route/export", { ...queryParams.value }, `route_${new Date().getTime()}.xlsx`); }

function handleEnable(row) { proxy.$modal.confirm('是否确认提交工艺路线[' + row.routeNo + ']进行审核？').then(() => enableRoute(row.routeId)).then(() => { proxy.$modal.msgSuccess("已提交审核"); getList(); }).catch(() => {}); }
function handleAudit(row) {
  auditForm.value = { routeId: row.routeId, auditRemark: undefined };
  auditData.value = {};
  auditOpen.value = true;
  auditLoading.value = true;
  getRoute(row.routeId).then(res => {
    auditData.value = res.data;
    auditLoading.value = false;
  }).catch(() => {
    auditLoading.value = false;
  });
}
function submitAudit(passed) {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      const auditAction = passed ? '1' : '2';
      const actionText = passed ? '通过' : '驳回';
      proxy.$modal.confirm(`确认${actionText}该工艺路线？`).then(() => {
        return auditRoute(auditForm.value.routeId, auditAction, auditForm.value.auditRemark);
      }).then(() => {
        proxy.$modal.msgSuccess(`审批${actionText}`);
        auditOpen.value = false;
        getList();
      }).catch(() => {});
    }
  });
}
function handleDisable(row) { proxy.$modal.confirm('是否确认停用工艺路线[' + row.routeNo + ']？停用后不可被新工单引用。').then(() => disableRoute(row.routeId)).then(() => { proxy.$modal.msgSuccess("停用成功"); getList(); }).catch(() => {}); }
function handleCopy(row) { proxy.$modal.confirm('是否确认复制工艺路线[' + row.routeNo + ']为新版本？').then(() => copyRoute(row.routeId)).then(() => { proxy.$modal.msgSuccess("复制成功"); getList(); }).catch(() => {}); }

// ===== Process Detail Operations =====
let _uidCounter = 0;
function genUid() { return 'p_' + (++_uidCounter) + '_' + Date.now(); }
function handleAddProcess() {
  const list = form.value.processList || (form.value.processList = []);
  list.push({ _uid: genUid(), id: null, processId: undefined, processCode: '', processName: '', resourceId: undefined, resourceName: '', stepSeq: (list.length + 1) * 10, stdTime: 0, prepTime: 0, isKeyProcess: '0', isOutsource: '0', remark: undefined, _isParallel: false, _stepGroupIdx: list.length });
  syncStepGroups();
}
function handleDeleteProcess(index) {
  if (form.value.processList.length <= 1) {
    proxy.$modal.msgWarning('工序明细至少需要保留一个工序，不能删除');
    return;
  }
  form.value.processList.splice(index, 1);
  recalcTotalStdTime();
  syncStepGroups();
}
function recalcTotalStdTime() {
  let total = 0;
  form.value.processList.forEach(p => { if (p.stdTime) total += p.stdTime; });
  form.value.totalStdTime = Math.round(total * 100) / 100;
}

// ===== 可视化流程图：并行分组计算 =====
const processGroups = computed(() => {
  const list = form.value.processList || [];
  const groups = [];
  let currentGroup = [];
  list.forEach((p, i) => {
    if (i === 0) {
      currentGroup.push(p);
    } else {
      if (p._isParallel) {
        currentGroup.push(p);
      } else {
        groups.push(currentGroup);
        currentGroup = [p];
      }
    }
  });
  if (currentGroup.length > 0) groups.push(currentGroup);
  return groups;
});

const viewProcessGroups = computed(() => {
  return groupByStepSeq(viewData.value.processList);
});
const auditProcessGroups = computed(() => {
  return groupByStepSeq(auditData.value.processList);
});
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

function syncStepGroups() {
  const groups = processGroups.value;
  groups.forEach((group, gi) => {
    group.forEach(p => {
      p._stepGroupIdx = gi;
      p.stepSeq = (gi + 1) * 10;
    });
  });
}

function canMergeParallel(index) {
  if (index <= 0) return false;
  const prev = form.value.processList[index - 1];
  return prev && (prev.processCode || prev.processName);
}

function toggleParallel(index) {
  const p = form.value.processList[index];
  if (p._isParallel) {
    p._isParallel = false;
  } else {
    p._isParallel = true;
  }
  syncStepGroups();
}

function prepareProcessListForSubmit() {
  syncStepGroups();
  form.value.processList.forEach(p => {
    delete p._uid;
    delete p._isParallel;
    delete p._stepGroupIdx;
  });
}

// ===== Process Picker =====
function openProcessPicker(index) {
  currentProcessIndex.value = index;
  processPickerOpen.value = true;
  processPickerQuery.pageNum = 1;
  processPickerQuery.processCode = undefined;
  processPickerQuery.processName = undefined;
  processPickerQuery.processType = undefined;
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
function onProcessPickerRadioChange(row) { selectedProcessId.value = row.processId; selectedProcessRow.value = row; }
function processPickerRowClass({ row }) { return selectedProcessId.value === row.processId ? 'current-row' : ''; }
function resetProcessPickerQuery() { processPickerQuery.pageNum = 1; processPickerQuery.processCode = undefined; processPickerQuery.processName = undefined; processPickerQuery.processType = undefined; loadProcessPicker(); }
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

function clearProcess(index) { const p = form.value.processList[index]; p.processId = undefined; p.processCode = ''; p.processName = ''; p.stdTime = 0; p.prepTime = 0; p.isKeyProcess = '0'; p.isOutsource = '0'; recalcTotalStdTime(); }

// ===== Resource Picker =====
function openResourcePicker(index) {
  currentProcessIndex.value = index;
  resourcePickerOpen.value = true;
  resourcePickerQuery.pageNum = 1;
  resourcePickerQuery.resourceName = undefined;
  resourcePickerQuery.lineName = undefined;
  resourcePickerSelectedId.value = form.value.processList[index].resourceId || null;
  resourcePickerSelectedRow.value = null;
  getResourcePickerList();
}
function getResourcePickerList() {
  resourcePickerLoading.value = true;
  listResource(resourcePickerQuery).then(res => {
    resourcePickerList.value = res.rows;
    resourcePickerTotal.value = res.total;
    resourcePickerLoading.value = false;
  }).catch(() => { resourcePickerLoading.value = false; });
}
function handleResourcePickerQuery() { resourcePickerQuery.pageNum = 1; getResourcePickerList(); }
function resetResourcePickerQuery() { resourcePickerQuery.pageNum = 1; resourcePickerQuery.resourceName = undefined; resourcePickerQuery.lineName = undefined; getResourcePickerList(); }
function onResourceRowClick(row) { resourcePickerSelectedId.value = row.resourceId; resourcePickerSelectedRow.value = row; }
function onResourceRowDblClick(row) { onResourceRowClick(row); confirmResourcePicker(); }
function confirmResourcePicker() {
  if (!resourcePickerSelectedRow.value) return;
  const row = resourcePickerSelectedRow.value;
  const idx = currentProcessIndex.value;
  const p = form.value.processList[idx];
  p.resourceId = row.resourceId;
  p.resourceName = row.resourceName;
  resourcePickerOpen.value = false;
}
function clearResource(index) { const p = form.value.processList[index]; p.resourceId = undefined; p.resourceName = ''; }

// ===== Product Picker =====
function openProductPicker() { proxy.$refs.productPickerRef.open(form.value.productId); }
function clearProduct() { form.value.productId = undefined; form.value.productCode = undefined; form.value.productName = undefined; }
function onProductPickerConfirm(material) {
  form.value.productId = material.materialId;
  form.value.productCode = material.materialCode;
  form.value.productName = material.materialName;
}

// ===== Dict helpers =====
function statusLabel(status) { if (status === null || status === undefined || status === '') return '—'; if (!mms_route_status.value) return '—'; const item = mms_route_status.value.find(d => d.value == status); return item ? item.label : '—'; }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'gray', '4': 'red' }; return map[status] || 'gray'; }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-void', '4': 'tab-reject' }; return map[value] || ''; }
function statusTagType(status) { const map = { '0': 'info', '1': '', '2': 'success', '3': 'danger', '4': 'danger' }; return map[status] || 'info'; }
function logTimelineType(type) { const map = { create: 'primary', update: 'warning', enable: 'success', audit: 'success', disable: 'danger', copy: 'info' }; return map[type] || 'info'; }
function changeTypeLabel(type) { if (!mms_route_change_type.value) return type; const item = mms_route_change_type.value.find(d => d.value === type); return item ? item.label : type; }
function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }

// ===== 拖拽排序 =====
let sortableInstance = null;
function initSortable() {
  nextTick(() => {
    const el = document.querySelector('.process-edit-table .el-table__body > tbody');
    if (!el) return;
    if (sortableInstance) { sortableInstance.destroy(); sortableInstance = null; }
    sortableInstance = Sortable.create(el, {
      handle: '.drag-handle',
      animation: 200,
      ghostClass: 'sortable-ghost',
      onEnd: (evt) => {
        if (evt.oldIndex === evt.newIndex) return;
        const moved = form.value.processList.splice(evt.oldIndex, 1)[0];
        form.value.processList.splice(evt.newIndex, 0, moved);
        // 重新判断并行标记：移除首行的并行标记
        form.value.processList.forEach((p, i) => {
          if (i === 0) p._isParallel = false;
        });
        syncStepGroups();
      }
    });
  });
}

getList();
</script>

<style>
:root{--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1)}
</style>
<style scoped>
.mms-route-page{padding-top:10px;font-feature-settings:"tnum" 1;color:var(--ink-900)}
.surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.filter-card{padding:14px 20px 16px}
.filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.filter-card .adv-link:hover{color:var(--brand-600)}
.filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.filter-card .filter-buttons{display:flex;gap:8px}
.field{display:flex;flex-direction:column;gap:6px}
.field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.field .control :deep(.el-select){width:100%}
.field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.toolbar .left,.toolbar .right{display:flex;gap:8px;align-items:center}
.toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.table-wrap{overflow-x:auto}
.app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.app-table :deep(.el-table__body td){border-right-color:transparent!important}
.app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.app-table :deep(.el-table__inner-wrapper::before){display:none}
.badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.badge .dot{width:6px;height:6px;border-radius:50%}
.badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.badge.amber .dot{background:var(--amber-500)}
.badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.badge.blue .dot{background:var(--blue-500)}
.badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.badge.green .dot{background:var(--green-500)}
.badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.badge.gray .dot{background:var(--ink-400)}
.pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.tabs-track::-webkit-scrollbar{display:none}
.status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.status-tab.is-active .count{background:var(--brand-600);color:#fff}
.status-tab.is-active .dot{background:var(--brand-500)}
.status-tab.tab-draft .dot{background:var(--amber-500)}.status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)}.status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
.status-tab.tab-audit .dot{background:var(--blue-500)}.status-tab.tab-audit .count{background:var(--blue-50);color:var(--blue-700)}.status-tab.is-active.tab-audit .count{background:var(--blue-500);color:#fff}
.status-tab.tab-done .dot{background:var(--green-500)}.status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)}.status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
.status-tab.tab-void .dot{background:var(--ink-400)}.status-tab.tab-void .count{background:var(--ink-100);color:var(--ink-500)}.status-tab.is-active.tab-void .count{background:var(--ink-400);color:#fff}
.status-tab.tab-reject .dot{background:var(--red-500)}.status-tab.tab-reject .count{background:var(--red-50);color:var(--red-700)}.status-tab.is-active.tab-reject .count{background:var(--red-500);color:#fff}
.badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.badge.red .dot{background:var(--red-500)}
.reject-alert{margin-bottom:16px}
.tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.detail-toolbar{display:flex;align-items:center;gap:8px;margin-bottom:12px}
.detail-toolbar .detail-tip{font-size:13px;color:var(--ink-500)}
.empty-detail{padding:20px 0}
.status-help-content{max-height:520px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{border-radius:8px;padding:16px;border:1px solid}
.status-help-content .highlight-card-title{font-size:14px;font-weight:600;margin-bottom:8px;display:flex;align-items:center}
.status-help-content .highlight-card-body{font-size:13px;color:#606266;line-height:1.6}
.status-help-content .highlight-card-body p{margin:4px 0}
.status-help-content .highlight-primary{background-color:#ecf5ff;border-color:#a0cfff}.status-help-content .highlight-primary .highlight-card-title{color:#409eff}
.status-help-content .highlight-success{background-color:#f0f9eb;border-color:#b3e19d}.status-help-content .highlight-success .highlight-card-title{color:#67c23a}
.status-help-content .highlight-warning{background-color:#fdf6ec;border-color:#f5dab1}.status-help-content .highlight-warning .highlight-card-title{color:#e6a23c}
.status-help-content .highlight-danger{background-color:#fef0f0;border-color:#fbc4c4}.status-help-content .highlight-danger .highlight-card-title{color:#f56c6c}
/* ===== 横向流程图样式 ===== */
.flow-canvas{display:flex;align-items:flex-start;overflow-x:auto;overflow-y:hidden;padding:18px 16px;background:linear-gradient(180deg,#f8fafc 0%,#f1f5f9 100%);border:1px solid var(--ink-200);border-radius:var(--r-md);margin-bottom:12px;gap:0;scrollbar-width:thin}
.flow-canvas::-webkit-scrollbar{height:6px}
.flow-canvas::-webkit-scrollbar-track{background:transparent}
.flow-canvas::-webkit-scrollbar-thumb{background:var(--ink-300);border-radius:3px}
.flow-connector{display:flex;align-items:center;height:72px;flex-shrink:0;padding:0 2px}
.flow-connector-dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300);flex-shrink:0}
.flow-connector-line{width:24px;height:2px;background:linear-gradient(90deg,var(--ink-300),var(--ink-400));flex-shrink:0}
.flow-connector-arrow{width:0;height:0;border-left:7px solid var(--ink-400);border-top:5px solid transparent;border-bottom:5px solid transparent;flex-shrink:0;margin-left:-1px}
.flow-step{display:flex;align-items:flex-start;flex-shrink:0}
.flow-step-badge{display:flex;align-items:center;justify-content:center;width:28px;height:28px;border-radius:50%;font-size:13px;font-weight:700;flex-shrink:0;margin-right:8px;transition:all .2s var(--ease-out)}
.flow-step-badge.serial{background:linear-gradient(135deg,#6366f1,#4f46e5);color:#fff;box-shadow:0 2px 6px rgba(79,70,229,.35)}
.flow-step-badge.parallel{background:linear-gradient(135deg,#f59e0b,#d97706);color:#fff;box-shadow:0 2px 6px rgba(217,119,6,.35)}
.flow-step-body{display:flex;flex-direction:column;gap:4px}
.flow-step-label{font-size:11px;font-weight:600;letter-spacing:.3px;padding:1px 7px;border-radius:999px;display:inline-flex;align-items:center;gap:3px;width:fit-content}
.flow-step.is-parallel .flow-step-label{background:#fffbeb;color:#b45309;border:1px solid #fde68a}
.flow-step:not(.is-parallel) .flow-step-label{background:var(--brand-50);color:var(--brand-700);border:1px solid var(--brand-200)}
.flow-step-nodes{display:flex;flex-direction:column;gap:4px}
.flow-card{display:flex;align-items:center;gap:8px;padding:8px 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm);min-width:140px;transition:all .15s var(--ease-out);box-shadow:0 1px 2px rgba(15,23,42,.04)}
.flow-card:hover{border-color:var(--brand-400);box-shadow:0 2px 8px rgba(99,102,241,.15);transform:translateY(-1px)}
.flow-card-icon{display:flex;align-items:center;justify-content:center;width:28px;height:28px;border-radius:var(--r-sm);flex-shrink:0}
.flow-card-icon.normal{background:var(--ink-100);color:var(--ink-500)}
.flow-card-icon.key{background:#fef2f2;color:#ef4444}
.flow-card-text{display:flex;flex-direction:column;gap:1px;min-width:0}
.flow-card-name{font-size:13px;font-weight:600;color:var(--ink-900);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:120px}
.flow-card-code{font-size:11px;color:var(--ink-400);font-family:'SF Mono',Monaco,monospace}
.flow-card-meta{font-size:10px;color:var(--ink-500);margin-top:1px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:120px}
.flow-empty{padding:20px 0}
.step-cell{display:flex;flex-direction:column;align-items:center;gap:2px}
.step-num{display:inline-flex;align-items:center;justify-content:center;width:22px;height:22px;border-radius:50%;background:var(--brand-50);color:var(--brand-700);font-size:12px;font-weight:700}
.drag-handle{color:var(--ink-400);font-size:16px}
.drag-handle:hover{color:var(--brand-500)}
.process-edit-table :deep(.el-table__body tr){cursor:default}
.process-edit-table :deep(.el-table__body tr:hover .drag-handle){color:var(--brand-500)}
.process-edit-table :deep(.sortable-ghost){opacity:.4;background:var(--brand-50)}
.process-edit-table :deep(.sortable-chosen){background:var(--brand-50)}

@media(max-width:1100px){.filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.filter-card .filter-bar{grid-template-columns:1fr}}
</style>
