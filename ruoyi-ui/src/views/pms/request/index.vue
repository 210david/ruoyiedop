<template>
  <div class="app-container pms-request-page">
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
          <label>申请单号</label>
          <div class="control">
            <el-input v-model="queryParams.requestNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>标题</label>
          <div class="control">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_request_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>优先级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.priority" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_priority" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>申请人</label>
          <div class="control">
            <el-input v-model="queryParams.requesterName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>审批人</label>
          <div class="control">
            <el-input v-model="queryParams.auditBy" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>采购用途</label>
          <div class="control">
            <el-input v-model="queryParams.purpose" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
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
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span>
            <span>全部</span>
            <span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span>
            <span>{{ s.label }}</span>
            <span class="count">{{ statusCounts[s.value] || 0 }}</span>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:request:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:request:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:request:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:request:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_request_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
<el-table-column label="申请单号" prop="requestNo" key="requestNo" :width="colWidth('requestNo', 180)" resizable sortable="custom" v-if="columns.requestNo.visible" />
          <el-table-column label="标题" prop="title" key="title" :width="colWidth('title', 240)" resizable show-overflow-tooltip v-if="columns.title.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="优先级" prop="priority" key="priority" :width="colWidth('priority', 100)" resizable align="center" sortable="custom" v-if="columns.priority.visible">
            <template #default="scope"><span class="badge amber">{{ priorityLabel(scope.row.priority) }}</span></template>
          </el-table-column>
          <el-table-column label="期望交货日期" prop="expectDate" key="expectDate" :width="colWidth('expectDate', 130)" resizable align="center" sortable="custom" v-if="columns.expectDate.visible" />
          <el-table-column label="预估总金额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="right" sortable="custom" v-if="columns.totalAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="申请人" prop="requesterName" key="requesterName" :width="colWidth('requesterName', 120)" resizable v-if="columns.requesterName.visible" />
          <el-table-column label="申请部门" prop="deptName" key="deptName" :width="colWidth('deptName', 140)" resizable show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="审批人" prop="auditBy" key="auditBy" :width="colWidth('auditBy', 120)" resizable v-if="columns.auditBy.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:request:edit']" v-if="scope.row.status === '0' || scope.row.status === '3'">修改</el-button>
                <el-button link type="primary" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['pms:request:approve']" v-if="scope.row.status === '0' || scope.row.status === '1'">审批</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:request:remove']" v-if="scope.row.status === '0'">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

<!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="1190px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="requestRef" :model="form" :rules="rules" label-width="130px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="申请单号" prop="requestNo">
                    <el-input v-model="form.requestNo" placeholder="保存后自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="申请标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入申请标题" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="优先级" prop="priority">
                    <el-select v-model="form.priority" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in pms_priority" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="期望交货日期" prop="expectDate">
                    <el-date-picker v-model="form.expectDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="申请人" prop="requesterName">
                    <el-input v-model="form.requesterName" readonly placeholder="请选择申请人" style="width: 100%" @click="openUserPicker">
                      <template #append>
                        <el-button icon="Search" @click="openUserPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.requesterName" class="clear-icon" @click.stop="clearRequester"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="申请部门" prop="deptName">
                    <el-input v-model="form.deptName" placeholder="选择申请人后自动带出" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="预估总金额" prop="totalAmount">
                    <el-input-number v-model="form.totalAmount" :precision="2" :min="0" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="planId">
                    <template #label>
                      <span>关联采购计划</span>
                      <el-tooltip content="仅可选择审批通过的采购计划；选择后将自动带出计划明细" placement="top">
                        <el-icon class="rd-form-tip"><question-filled /></el-icon>
                      </el-tooltip>
                    </template>
                    <el-input :model-value="form.planNo ? form.planNo + ' - ' + (form.planTitle || '') : ''" readonly placeholder="可选，选择后自动带出明细" style="width: 100%" @click="openPlanPicker">
                      <template v-if="form.planNo" #append><el-button icon="CircleClose" @click.stop="clearPlan" /></template>
                      <template v-else #append><el-button icon="Search" @click="openPlanPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="采购用途" prop="purpose">
                    <el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="请输入采购用途" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>采购明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddDetail">添加明细</el-button></el-col>
              </el-row>
              <el-table :data="form.detailList" border size="small">
                <el-table-column label="序号" type="index" width="85" align="center" />
                <el-table-column label="物料" prop="materialId" min-width="200"><template #default="scope"><el-input :model-value="scope.row.materialCode ? scope.row.materialCode + ' - ' + scope.row.materialName : ''" readonly size="small" placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker(scope.$index)"><template v-if="scope.row.materialCode" #append><el-button icon="CircleClose" size="small" @click.stop="clearMaterial(scope.$index)" /></template><template v-else #append><el-button icon="Search" size="small" @click="openMaterialPicker(scope.$index)" /></template></el-input></template></el-table-column>
                <el-table-column label="规格型号" prop="specModel" min-width="120"><template #default="scope"><span>{{ scope.row.specModel }}</span></template></el-table-column>
                <el-table-column label="单位" prop="unit" width="90"><template #default="scope"><el-select v-model="scope.row.unit" size="small" placeholder="单位" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></template></el-table-column>
                <el-table-column label="数量" prop="qty" width="100"><template #default="scope"><el-input-number v-model="scope.row.qty" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" @change="calcDetailAmount(scope.row)" /></template></el-table-column>
                <el-table-column label="预估单价" prop="estimatedPrice" width="110"><template #default="scope"><el-input-number v-model="scope.row.estimatedPrice" :precision="2" :min="0" :controls="false" size="small" style="width: 100px" @change="calcDetailAmount(scope.row)" /></template></el-table-column>
                <el-table-column label="预估金额" prop="estimatedAmount" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.estimatedAmount) }}</span></template></el-table-column>
                <el-table-column label="操作" width="80" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="handleDeleteDetail(scope.$index)">删除</el-button></template></el-table-column>
              </el-table>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <!-- 审核记录（含驳回提示） -->
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3" style="display:block">
              <el-alert v-if="form.status === '3'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该申请已被驳回，请根据审核意见修改后重新提交</template>
              </el-alert>
              <div class="rd-timeline">
                <div class="rd-timeline-item" v-for="log in form.auditLogList" :key="log.logId">
                  <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                  <div class="rd-timeline-content">
                    <div class="rd-timeline-header">
                      <span class="rd-timeline-title">
                        <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                        <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                      </span>
                      <span class="rd-timeline-time">{{ log.auditTime }}</span>
                    </div>
                    <div class="rd-timeline-body">
                      <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                    </div>
                    <div class="rd-timeline-comment" v-if="log.auditRemark">
                      <strong>审核意见：</strong>{{ log.auditRemark }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
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
    <el-dialog v-model="viewOpen" width="1190px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">采购申请详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.requestNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.requestNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">申请单号</span><div class="rd-value">{{ viewData.requestNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">标题</span><div class="rd-value">{{ viewData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_request_status" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="pms_priority" :value="viewData.priority" /></div></div>
              <div class="rd-item"><span class="rd-label">期望交货日期</span><div class="rd-value">{{ viewData.expectDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预估总金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.totalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ viewData.requesterName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
              <div class="rd-item" v-if="viewData.planNo"><span class="rd-label">关联计划</span><div class="rd-value">{{ viewData.planNo }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">采购用途</span><div class="rd-value">{{ viewData.purpose || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>采购明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <el-table :data="viewData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="85" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="数量" prop="qty" width="100" align="right" />
              <el-table-column label="预估单价" prop="estimatedPrice" width="110" align="right" />
              <el-table-column label="预估金额" prop="estimatedAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.estimatedAmount) }}</span></template></el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 审核记录 -->
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('v4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v4" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in viewData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditOpen" width="1060px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">采购申请审核</span>
          <div class="rd-detail-header-sub" v-if="auditData.requestNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.requestNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 信息摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>申请信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">申请单号</span><div class="rd-value">{{ auditData.requestNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请标题</span><div class="rd-value">{{ auditData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_request_status" :value="auditData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="pms_priority" :value="auditData.priority" /></div></div>
              <div class="rd-item"><span class="rd-label">期望交货日期</span><div class="rd-value">{{ auditData.expectDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预估总额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.totalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ auditData.requesterName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请部门</span><div class="rd-value">{{ auditData.deptName || '-' }}</div></div>
              <div class="rd-item" v-if="auditData.planNo"><span class="rd-label">关联计划</span><div class="rd-value">{{ auditData.planNo }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">采购用途</span><div class="rd-value">{{ auditData.purpose || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 采购明细 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>采购明细</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="auditData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="85" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="数量" prop="qty" width="100" align="right"><template #default="scope">{{ formatAmount(scope.row.qty) }}</template></el-table-column>
              <el-table-column label="预估单价" prop="estimatedPrice" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.estimatedPrice) }}</span></template></el-table-column>
              <el-table-column label="预估金额" prop="estimatedAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.estimatedAmount) }}</span></template></el-table-column>
            </el-table>
          </div>
        </section>
        <!-- 历史审核记录 -->
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('a2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a2" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in auditData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a1">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="110px">
              <el-form-item label="审批意见" prop="auditOpinion">
                <el-input v-model="auditForm.auditOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" show-word-limit maxlength="500" />
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

    <!-- 申请人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择申请人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="采购申请业务操作说明" width="700px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已转采购</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-tag size="small" type="info">审批驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="info">草稿</el-tag>
            <el-tag size="small" type="info">修改后重新提交</el-tag>
          </div>
          <div class="flow-item">
            <el-tag type="info">已作废</el-tag>
            <el-tag size="small" type="info">任意状态可作废</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建采购申请后的初始状态，可编辑、删除、提交审批
          </el-descriptions-item>
          <el-descriptions-item label="待审批">
            提交审批后等待审核，可审核通过或驳回
          </el-descriptions-item>
          <el-descriptions-item label="已审批">
            审核通过，可转为采购订单。已审批的申请不可修改
          </el-descriptions-item>
          <el-descriptions-item label="已驳回">
            审批未通过，回退为草稿状态可修改后重新提交
          </el-descriptions-item>
          <el-descriptions-item label="已转采购">
            已转为采购订单，采购申请流程结束
          </el-descriptions-item>
          <el-descriptions-item label="已作废">
            采购申请被作废，不再有效
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">关联采购计划</div>
              <div class="highlight-card-body">
                新建申请时可选择已审批通过的采购计划，系统<strong>自动带出计划明细</strong>（物料、数量、预算单价），无需手动重新录入
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">预估金额自动计算</div>
              <div class="highlight-card-body">
                预估金额 = <strong>数量 × 预估单价</strong>，编辑明细行数量或单价后自动计算，预估总金额自动汇总
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">审批后不可修改</div>
              <div class="highlight-card-body">
                采购申请审批通过后进入「已审批」状态，<strong>不可再修改申请内容</strong>，仅可转为采购订单
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">物料选择自动带出</div>
              <div class="highlight-card-body">
                选择物料后自动带出<strong>物料编码、名称、规格型号、单位</strong>等信息，无需手动填写
              </div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务触发说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建申请：</strong>填写申请标题、优先级（普通/紧急/特急）、期望交货日期，选择物料并填写数量和预估单价后保存
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>关联计划（可选）：</strong>可选择已审批通过的采购计划，自动带出计划明细，也可手动添加物料明细
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交审批：</strong>点击「提交审批」按钮，采购申请进入「待审批」状态，等待审核
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审核通过：</strong>审核通过后进入「已审批」状态，可转为采购订单进行后续采购流程
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审核驳回：</strong>审核驳回后回退为「草稿」状态，可修改后重新提交审批
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
    <!-- 物料选择弹框 -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
    <!-- 采购计划选择弹框 -->
    <plan-picker ref="planPickerRef" title="选择采购计划" @confirm="onPlanPickerConfirm" />
  </div>
</template>

<script setup name="PmsRequest">
import { listRequest, getRequest, addRequest, updateRequest, delRequest, auditRequest } from '@/api/pms/request'
import { getPlan } from '@/api/pms/plan'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import PlanPicker from '@/components/PlanPicker/index.vue'
import UserPicker from '@/components/UserPicker/index.vue'
import { CircleClose, QuestionFilled } from '@element-plus/icons-vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const { proxy } = getCurrentInstance()
const { pms_request_status, pms_priority, wms_unit } = proxy.useDict('pms_request_status', 'pms_priority', 'wms_unit')

const { collapsedCards, toggleCard } = useDetailCard(["c1","c2","c0","c3","v1","v2","v3","v4","a1","a2"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_request_index')

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  requestNo: { label: '申请单号', visible: true },
  title: { label: '标题', visible: true },
  status: { label: '状态', visible: true },
  priority: { label: '优先级', visible: true },
  expectDate: { label: '期望交货日期', visible: true },
  totalAmount: { label: '预估总金额', visible: true },
  requesterName: { label: '申请人', visible: true },
  deptName: { label: '申请部门', visible: true },
  auditBy: { label: '审批人', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_request_columns')
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

const activeStatusTab = ref('all')
const statusTabList = computed(() => {
  return pms_request_status.value.map(d => ({ label: d.label, value: d.value }))
})
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listRequest({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    pms_request_status.value.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-reject', '4': 'tab-done', '5': 'tab-void' }
  return map[value] || ''
}
function badgeClass(status) {
  const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'red', '4': 'green', '5': 'gray' }
  return map[status] || 'gray'
}
function statusLabel(status) {
  const item = pms_request_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function priorityLabel(priority) {
  const item = pms_priority.value.find(d => d.value == priority)
  return item ? item.label : '-'
}
function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  if (tab === 'all') {
    queryParams.value.status = undefined
  } else {
    queryParams.value.status = tab
  }
  handleQuery()
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.requestNo) count++
  if (queryParams.value.title) count++
  if (queryParams.value.status) count++
  if (queryParams.value.priority) count++
  if (queryParams.value.requesterName) count++
  if (queryParams.value.auditBy) count++
  if (queryParams.value.purpose) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

const list = ref([])
const showStatusHelp = ref(false)
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const materialPickerRef = ref(null)
const currentDetailIndex = ref(-1)
const planPickerRef = ref(null)
const auditOpen = ref(false)
const auditData = ref({})
const auditForm = ref({ requestId: undefined, auditOpinion: null })
const auditRules = { auditOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }] }

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    requestNo: undefined,
    title: undefined,
    status: undefined,
    priority: undefined,
    requesterName: undefined,
    auditBy: undefined,
    purpose: undefined,
    params: {}
  },
  rules: {
    title: [{ required: true, message: '申请标题不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const showAdvanced = ref(false)
const dateRange = ref([])

function getList() {
  loading.value = true
  listRequest(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  })
}

function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime'); queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.requestNo = undefined
  queryParams.value.title = undefined
  queryParams.value.status = undefined
  queryParams.value.priority = undefined
  queryParams.value.requesterName = undefined
  queryParams.value.auditBy = undefined
  queryParams.value.purpose = undefined
  queryParams.value.params = {}
  dateRange.value = []
  activeStatusTab.value = 'all'
  if (tableRef.value) tableRef.value.clearSort()
  handleQuery()
}
function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.requestId); single.value = selection.length !== 1; multiple.value = !selection.length }

function reset() {
  form.value = { requestId: undefined, requestNo: undefined, title: undefined, status: '0', priority: '0', expectDate: undefined, purpose: undefined, totalAmount: 0, requesterId: undefined, requesterName: undefined, deptId: undefined, deptName: undefined, planId: undefined, planNo: undefined, planTitle: undefined, remark: undefined, detailList: [] }
  proxy.resetForm('requestRef')
}

function handleAdd() { reset(); open.value = true; title.value = '添加采购申请' }
function handleUpdate(row) {
  reset()
  getRequest(row.requestId || ids.value[0]).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改采购申请'
    // 后端未返回计划标题时，补充查询以完整展示"计划编号 - 计划标题"
    if (form.value.planNo && !form.value.planTitle) {
      getPlan(form.value.planId).then(r => { if (r.data) form.value.planTitle = r.data.title })
    }
  })
}
function handleView(row) { getRequest(row.requestId).then(res => { viewData.value = res.data; viewOpen.value = true }) }

function calcDetailAmount(row) {
  if (row.qty != null && row.estimatedPrice != null) {
    row.estimatedAmount = parseFloat((row.qty * row.estimatedPrice).toFixed(2))
  }
  let total = 0
  form.value.detailList.forEach(d => { if (d.estimatedAmount != null) total += d.estimatedAmount })
  form.value.totalAmount = parseFloat(total.toFixed(2))
}

function handleAddDetail() {
  if (!form.value.detailList) form.value.detailList = []
  form.value.detailList.push({ materialId: null, materialCode: '', materialName: '', specModel: '', unit: '', qty: 0, estimatedPrice: 0, estimatedAmount: 0 })
}

/** 选择物料后自动带出物料信息 */
function openMaterialPicker(index) {
  currentDetailIndex.value = index
  materialPickerRef.value.open(form.value.detailList[index].materialId)
}
function onMaterialPickerConfirm(material) {
  if (currentDetailIndex.value >= 0) {
    const d = form.value.detailList[currentDetailIndex.value]
    d.materialId = material.materialId
    d.materialCode = material.materialCode
    d.materialName = material.materialName
    d.specModel = material.specModel || ''
    d.unit = material.unit || ''
  }
}
function clearMaterial(index) {
  const d = form.value.detailList[index]
  d.materialId = null
  d.materialCode = ''
  d.materialName = ''
  d.specModel = ''
  d.unit = ''
}

/** 打开采购计划选择弹框 */
function openPlanPicker() {
  planPickerRef.value.open(form.value.planId)
}

/** 弹框确认选择采购计划后自动带出明细 */
function onPlanPickerConfirm(plan) {
  applyPlanDetail(plan)
}

/** 清空已选采购计划 */
function clearPlan() {
  form.value.planId = undefined
  form.value.planNo = undefined
  form.value.planTitle = undefined
}

/** 选择采购计划后自动带出明细（planId 用于路由预选场景） */
function onPlanChange(val) {
  if (!val) {
    form.value.planId = undefined
    form.value.planNo = undefined
    form.value.planTitle = undefined
    return
  }
  getPlan(val).then(res => {
    applyPlanDetail(res.data)
  })
}

/** 应用采购计划数据并带出明细 */
function applyPlanDetail(plan) {
  if (!plan) return
  form.value.planId = plan.planId
  form.value.planNo = plan.planNo
  form.value.planTitle = plan.title
  // 自动带出计划明细为采购申请明细
  if (plan.detailList && plan.detailList.length > 0) {
    form.value.detailList = plan.detailList.map(d => ({
      materialId: d.materialId,
      materialCode: d.materialCode,
      materialName: d.materialName,
      specModel: d.specification,
      unit: d.unit,
      qty: d.planQuantity,
      estimatedPrice: d.budgetPrice,
      estimatedAmount: d.budgetAmount
    }))
    calcDetailAmount({})
  }
}

function handleDeleteDetail(index) { form.value.detailList.splice(index, 1); calcDetailAmount({}) }

function submitForm() {
  proxy.$refs['requestRef'].validate(valid => {
    if (valid) {
      if (form.value.requestId != undefined) {
        updateRequest(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addRequest(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}

function handleDelete(row) {
  const requestIds = row.requestId || ids.value
  proxy.$modal.confirm('确认删除编号为"' + requestIds + '"的数据？').then(() => delRequest(requestIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

/** 审核按钮操作 */
async function handleAudit(row) {
  const res = await getRequest(row.requestId)
  auditData.value = res.data
  auditForm.value = { requestId: row.requestId, auditOpinion: null }
  auditOpen.value = true
}

/** 提交审核 */
function submitAudit(passed) {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      const status = passed ? '2' : '3'
      const actionText = passed ? '通过' : '驳回'
      proxy.$modal.confirm(`确认${actionText}该采购申请？`).then(() => {
        return auditRequest(auditForm.value.requestId, status, auditForm.value.auditOpinion)
      }).then(() => {
        proxy.$modal.msgSuccess('审批成功')
        auditOpen.value = false
        getList()
      }).catch(() => {})
    }
  })
}

function handleExport() { proxy.download('pms/request/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime') }, `purchase_request_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

/** 打开申请人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.requesterId)
}
/** 申请人选择确认回调 — 自动带出所在部门 */
function onUserPickerConfirm(user) {
  form.value.requesterId = user.userId
  form.value.requesterName = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}
/** 清除申请人 */
function clearRequester() {
  form.value.requesterId = undefined
  form.value.requesterName = undefined
  form.value.deptId = undefined
  form.value.deptName = undefined
}

getList()

/** 从采购计划页面跳转过来时，自动打开新增弹窗并预选计划 */
const route = useRoute()
onActivated(() => {
  getList()
  if (route.query.planId) {
    handleAdd()
    nextTick(() => {
      form.value.planId = Number(route.query.planId)
      onPlanChange(form.value.planId)
    })
  }
})
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-request-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.pms-request-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pms-request-page .filter-card { padding:14px 20px 16px; }
.pms-request-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-request-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-request-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-request-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-request-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-request-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-request-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-request-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-request-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-request-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-request-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pms-request-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-request-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-request-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-request-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-request-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-request-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-request-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-request-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-request-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.pms-request-page .field .control :deep(.el-select) { width:100%; }
.pms-request-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-request-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-request-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-request-page .field .control :deep(.el-date-editor) { width:100%; }
.pms-request-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pms-request-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pms-request-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.pms-request-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-request-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-request-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-request-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-request-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-request-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-request-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-request-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-request-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-request-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-request-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.pms-request-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.pms-request-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.pms-request-page .status-tab.tab-audit .dot { background:var(--blue-500); }
.pms-request-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); }
.pms-request-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.pms-request-page .status-tab.tab-approved .dot, .pms-request-page .status-tab.tab-done .dot { background:var(--green-500); }
.pms-request-page .status-tab.tab-approved .count, .pms-request-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.pms-request-page .status-tab.is-active.tab-approved .count, .pms-request-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.pms-request-page .status-tab.tab-reject .dot { background:var(--red-500); }
.pms-request-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.pms-request-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.pms-request-page .status-tab.tab-void .dot { background:var(--ink-400); }
.pms-request-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; border-radius:999px; border:1px solid var(--ink-200); background:#fff; font-size:13px; color:var(--ink-500); cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.pms-request-page .tip-pill:hover { border-color:var(--brand-200); color:var(--brand-700); background:var(--brand-50); }
.pms-request-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-request-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-request-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-request-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pms-request-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-request-page .btn-soft .el-icon { font-size:14px; }
.pms-request-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-request-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-request-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-request-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-request-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-request-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.pms-request-page .table-wrap { overflow-x:auto; }
.pms-request-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-request-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-request-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-request-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-request-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-request-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-request-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-request-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-request-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-request-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-request-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
/* ===== Badges ===== */
.pms-request-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-request-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-request-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-request-page .badge.amber .dot { background:var(--amber-500); }
.pms-request-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pms-request-page .badge.blue .dot { background:var(--blue-500); }
.pms-request-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-request-page .badge.green .dot { background:var(--green-500); }
.pms-request-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.pms-request-page .badge.red .dot { background:var(--red-500); }
.pms-request-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-request-page .badge.gray .dot { background:var(--ink-400); }
.pms-request-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-request-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-request-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-request-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-request-page .pagination-container :deep(.el-pagination .btn-prev), .pms-request-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-request-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-request-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-request-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .pms-request-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-request-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-request-page .toolbar { flex-wrap:wrap; gap:10px; } }

.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
.reject-alert {
  margin-bottom: 16px;
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
.highlight-card {
  border-radius: 8px;
  padding: 16px;
  border: 1px solid;
}
.highlight-success {
  background-color: #f0f9ff;
  border-color: #b3e19d;
}
.highlight-danger {
  background-color: #fef0f0;
  border-color: #fbc4c4;
}
.highlight-primary {
  background-color: #ecf5ff;
  border-color: #a0cfff;
}
.highlight-warning {
  background-color: #fdf6ec;
  border-color: #f5dab1;
}
.highlight-card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}
.highlight-success .highlight-card-title {
  color: #67c23a;
}
.highlight-danger .highlight-card-title {
  color: #f56c6c;
}
.highlight-primary .highlight-card-title {
  color: #409eff;
}
.highlight-warning .highlight-card-title {
  color: #e6a23c;
}
.highlight-card-body {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
:deep(.el-timeline-item__node) {
  background-color: transparent;
  border: 2px solid;
}
.rd-form-tip {
  margin-left: 4px;
  color: #c0c4cc;
  font-size: 14px;
  vertical-align: middle;
  cursor: help;
}
.rd-form-tip:hover {
  color: #909399;
}
.rd-select-empty {
  padding: 10px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
