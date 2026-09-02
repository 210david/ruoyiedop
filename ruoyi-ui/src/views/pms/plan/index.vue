<template>
  <div class="app-container pms-plan-page">
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
          <label>计划单号</label>
          <div class="control">
            <el-input v-model="queryParams.planNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>计划标题</label>
          <div class="control">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in pms_plan_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>计划类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.planType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in pms_plan_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>编制人</label>
          <div class="control">
            <el-input v-model="queryParams.plannerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>编制部门</label>
          <div class="control">
            <el-input v-model="queryParams.deptName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:plan:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:plan:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:plan:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:plan:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_plan_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="planList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
<el-table-column label="计划单号" prop="planNo" key="planNo" :width="colWidth('planNo', 180)" resizable sortable="custom" v-if="columns.planNo.visible" />
          <el-table-column label="计划标题" prop="title" key="title" :width="colWidth('title', 240)" resizable show-overflow-tooltip v-if="columns.title.visible" />
          <el-table-column label="计划类型" prop="planType" key="planType" :width="colWidth('planType', 120)" resizable align="center" v-if="columns.planType.visible">
            <template #default="scope"><span class="badge violet">{{ planTypeLabel(scope.row.planType) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="预算金额" prop="budgetAmount" key="budgetAmount" :width="colWidth('budgetAmount', 130)" resizable align="right" sortable="custom" v-if="columns.budgetAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="实际金额" prop="actualAmount" key="actualAmount" :width="colWidth('actualAmount', 130)" resizable align="right" v-if="columns.actualAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.actualAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="编制人" prop="plannerName" key="plannerName" :width="colWidth('plannerName', 120)" resizable v-if="columns.plannerName.visible" />
          <el-table-column label="编制部门" prop="deptName" key="deptName" :width="colWidth('deptName', 140)" resizable show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="开始日期" prop="startDate" key="startDate" :width="colWidth('startDate', 130)" resizable align="center" v-if="columns.startDate.visible" />
          <el-table-column label="结束日期" prop="endDate" key="endDate" :width="colWidth('endDate', 130)" resizable align="center" v-if="columns.endDate.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['pms:plan:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:plan:edit']" v-if="scope.row.status === '0' || scope.row.status === '6'">修改</el-button>
                <el-button link type="primary" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['pms:plan:audit']" v-if="scope.row.status === '0' || scope.row.status === '1'">审批</el-button>
                <el-button link type="danger" icon="CircleClose" @click="handleClose(scope.row)" v-hasPermi="['pms:plan:edit']" v-if="scope.row.status === '2' || scope.row.status === '3'">关闭</el-button>
                <el-button link type="primary" icon="Promotion" @click="handleGenerateRequest(scope.row)" v-hasPermi="['pms:request:add']" v-if="scope.row.status === '2'">生成申请</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="1296px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H7v6h2v-6zm4 0h-2v6h2v-6zm4 0h-2v6h2v-6zm2-9h-2v2H7V2H5v2H4a2 2 0 0 0-2 2v1a3 3 0 0 1 0 6v1a2 2 0 0 0 2 2h1v2h2v-2h8v2h2v-2h1a2 2 0 0 0 2-2v-1a3 3 0 0 1 0-6V6a2 2 0 0 0-2-2z"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.planNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ form.planNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="planRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="计划单号" prop="planNo">
                    <el-input v-model="form.planNo" placeholder="系统自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="计划标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入计划标题" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="计划类型" prop="planType">
                    <el-select v-model="form.planType" placeholder="请选择计划类型" style="width: 100%">
                      <el-option v-for="dict in pms_plan_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态">
                    <el-tag :type="getStatusType(form.status)">{{ getStatusLabel(form.status) }}</el-tag>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="开始日期" prop="startDate">
                    <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束日期" prop="endDate">
                    <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="编制人">
                    <el-input v-model="form.plannerName" readonly placeholder="请选择编制人" style="width: 100%" @click="openUserPicker">
                      <template #append>
                        <el-button icon="Search" @click="openUserPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.plannerName" class="clear-icon" @click.stop="clearPlanner"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="编制部门">
                    <el-input v-model="form.deptName" placeholder="选择编制人后自动带出" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>计划明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddDetail">添加明细</el-button></el-col>
              </el-row>
              <el-table :data="form.detailList" border size="small">
                <el-table-column label="序号" type="index" width="85" align="center" />
                <el-table-column label="物料" prop="materialId" min-width="200"><template #default="scope"><el-input :model-value="scope.row.materialCode ? scope.row.materialCode + ' - ' + scope.row.materialName : ''" readonly size="small" placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker(scope.$index)"><template v-if="scope.row.materialCode" #append><el-button icon="CircleClose" size="small" @click.stop="clearMaterial(scope.$index)" /></template><template v-else #append><el-button icon="Search" size="small" @click="openMaterialPicker(scope.$index)" /></template></el-input></template></el-table-column>
                <el-table-column label="规格型号" prop="specification" min-width="120"><template #default="scope"><span>{{ scope.row.specification }}</span></template></el-table-column>
                <el-table-column label="单位" prop="unit" width="90"><template #default="scope"><el-select v-model="scope.row.unit" size="small" placeholder="单位" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></template></el-table-column>
                <el-table-column label="计划数量" prop="planQuantity" width="110"><template #default="scope"><el-input-number v-model="scope.row.planQuantity" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" @change="calculateAmount(scope.row)" /></template></el-table-column>
                <el-table-column label="预算单价" prop="budgetPrice" width="110"><template #default="scope"><el-input-number v-model="scope.row.budgetPrice" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" @change="calculateAmount(scope.row)" /></template></el-table-column>
                <el-table-column label="预算金额" prop="budgetAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetAmount) }}</span></template></el-table-column>
                <el-table-column label="需求日期" prop="requireDate" width="150"><template #default="scope"><el-date-picker v-model="scope.row.requireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" size="small" style="width: 100%" /></template></el-table-column>
                <el-table-column label="备注" prop="remark" min-width="120"><template #default="scope"><el-input v-model="scope.row.remark" placeholder="备注" size="small" /></template></el-table-column>
                <el-table-column label="操作" width="80" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="handleDeleteDetail(scope.$index)">删除</el-button></template></el-table-column>
              </el-table>
              <div class="detail-summary" v-if="form.detailList && form.detailList.length > 0">
                <span>合计：计划数量 {{ totalQuantity }} | 预算金额 <span class="rd-amount">{{ formatMoney(totalBudgetAmount) }}</span></span>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="计划说明" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入计划说明" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 审核记录（含驳回提示） -->
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4" style="display:block">
              <el-alert v-if="form.status === '6'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该计划已被驳回，请根据审核意见修改后重新提交</template>
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
    <el-dialog v-model="viewOpen" width="1296px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H7v6h2v-6zm4 0h-2v6h2v-6zm4 0h-2v6h2v-6zm2-9h-2v2H7V2H5v2H4a2 2 0 0 0-2 2v1a3 3 0 0 1 0 6v1a2 2 0 0 0 2 2h1v2h2v-2h8v2h2v-2h1a2 2 0 0 0 2-2v-1a3 3 0 0 1 0-6V6a2 2 0 0 0-2-2z"/></svg>
          </div>
          <span class="rd-detail-header-title">采购计划详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.planNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ viewData.planNo }}</span>
          </div>
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
              <div class="rd-item"><span class="rd-label">计划单号</span><div class="rd-value">{{ viewData.planNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划标题</span><div class="rd-value">{{ viewData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划类型</span><div class="rd-value"><dict-tag :options="pms_plan_type" :value="viewData.planType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_plan_status" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">开始日期</span><div class="rd-value">{{ viewData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束日期</span><div class="rd-value">{{ viewData.endDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预算金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.budgetAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">实际金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">编制人</span><div class="rd-value">{{ viewData.plannerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">编制部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>计划明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <el-table :data="viewData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="85" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specification" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="计划数量" prop="planQuantity" width="110" align="right"><template #default="scope">{{ formatAmount(scope.row.planQuantity) }}</template></el-table-column>
              <el-table-column label="预算单价" prop="budgetPrice" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetPrice) }}</span></template></el-table-column>
              <el-table-column label="预算金额" prop="budgetAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetAmount) }}</span></template></el-table-column>
              <el-table-column label="需求日期" prop="requireDate" width="120" align="center" />
              <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
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
              <div class="rd-item rd-item--full"><span class="rd-label">计划说明</span><div class="rd-value">{{ viewData.description || '-' }}</div></div>
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
    <el-dialog v-model="auditOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">采购计划审核</span>
          <div class="rd-detail-header-sub" v-if="auditData.planNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.planNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 信息摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>计划信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">计划单号</span><div class="rd-value">{{ auditData.planNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划标题</span><div class="rd-value">{{ auditData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划类型</span><div class="rd-value"><dict-tag :options="pms_plan_type" :value="auditData.planType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_plan_status" :value="auditData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">开始日期</span><div class="rd-value">{{ auditData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束日期</span><div class="rd-value">{{ auditData.endDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预算金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.budgetAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">实际金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">编制人</span><div class="rd-value">{{ auditData.plannerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">编制部门</span><div class="rd-value">{{ auditData.deptName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">计划说明</span><div class="rd-value">{{ auditData.description || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 计划明细 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>计划明细</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="auditData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="85" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specification" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="计划数量" prop="planQuantity" width="110" align="right"><template #default="scope">{{ formatAmount(scope.row.planQuantity) }}</template></el-table-column>
              <el-table-column label="预算单价" prop="budgetPrice" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetPrice) }}</span></template></el-table-column>
              <el-table-column label="预算金额" prop="budgetAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetAmount) }}</span></template></el-table-column>
              <el-table-column label="需求日期" prop="requireDate" width="120" align="center" />
              <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
        <!-- 历史审核记录 -->
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('a1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a1" style="display:block">
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
          <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
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

    <!-- 编制人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择编制人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="采购计划业务操作说明" width="700px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已关闭</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
            <el-tag size="small" type="info">任意状态可作废</el-tag>
          </div>
          <div class="flow-item">
            <el-tag type="success">已审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="primary">生成采购申请</el-tag>
            <el-tag size="small" type="info">自动带出计划明细</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建采购计划后的初始状态，可编辑、删除、提交审核
          </el-descriptions-item>
          <el-descriptions-item label="待审核">
            提交审核后等待审批，可审核通过或驳回（驳回回退为草稿）
          </el-descriptions-item>
          <el-descriptions-item label="已审批">
            审核通过，可生成采购申请。已审批的计划不可修改，可关闭
          </el-descriptions-item>
          <el-descriptions-item label="已关闭">
            已审批的计划可手动关闭，关闭后不再允许生成采购申请
          </el-descriptions-item>
          <el-descriptions-item label="已作废">
            采购计划被作废，不再有效
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">生成采购申请</div>
              <div class="highlight-card-body">
                已审批的计划可点击「生成采购申请」按钮，系统自动跳转到采购申请页面并<strong>自动带出计划明细</strong>，无需手动重新录入
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">预算金额自动计算</div>
              <div class="highlight-card-body">
                预算金额 = <strong>计划数量 × 预算单价</strong>，编辑明细行数量或单价后自动计算，总额自动汇总
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">审核后不可修改</div>
              <div class="highlight-card-body">
                采购计划审核通过后进入「已审批」状态，<strong>不可再修改计划内容</strong>，仅可关闭或生成采购申请
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
            <strong>新建计划：</strong>填写计划标题、计划类型（年度/季度/月度/临时），选择物料并填写计划数量和预算单价后保存
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交审核：</strong>点击「提交审核」按钮，采购计划进入「待审核」状态，等待审批
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审核通过：</strong>审核通过后进入「已审批」状态，可点击「生成采购申请」按钮跳转采购申请页面并自动带出明细
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审核驳回：</strong>审核驳回后回退为「草稿」状态，可修改后重新提交审核
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>关闭计划：</strong>已审批的计划可手动关闭，关闭后不再允许生成采购申请
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
    <!-- 物料选择弹框 -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
  </div>
</template>

<script setup name="PmsPlan">
import { listPlan, getPlan, delPlan, addPlan, updatePlan, auditPlan, closePlan } from "@/api/pms/plan";
import { getUserProfile } from "@/api/system/user";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import UserPicker from '@/components/UserPicker/index.vue'
import { CircleClose, QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const { proxy } = getCurrentInstance();
const { pms_plan_status, pms_plan_type, wms_unit } = proxy.useDict('pms_plan_status', 'pms_plan_type', 'wms_unit');
const { collapsedCards, toggleCard } = useDetailCard(["c1","c2","c0","c3","c4","v1","v2","v3","v4","a1"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_plan_index')

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  planNo: { label: '计划单号', visible: true },
  title: { label: '计划标题', visible: true },
  planType: { label: '计划类型', visible: true },
  status: { label: '状态', visible: true },
  budgetAmount: { label: '预算金额', visible: true },
  actualAmount: { label: '实际金额', visible: true },
  plannerName: { label: '编制人', visible: true },
  deptName: { label: '编制部门', visible: true },
  startDate: { label: '开始日期', visible: true },
  endDate: { label: '结束日期', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_plan_columns')
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

const showAdvanced = ref(false)
const dateRange = ref([])
const activeStatusTab = ref('all')
const statusTabList = computed(() => {
  return pms_plan_status.value.map(d => ({ label: d.label, value: d.value }))
})
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listPlan({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    pms_plan_status.value.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-done', '4': 'tab-done', '5': 'tab-void', '6': 'tab-reject' }
  return map[value] || ''
}
function badgeClass(status) {
  const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'green', '4': 'green', '5': 'gray', '6': 'red' }
  return map[status] || 'gray'
}
function statusLabel(status) {
  const item = pms_plan_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function planTypeLabel(type) {
  const item = pms_plan_type.value.find(d => d.value == type)
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
  if (queryParams.value.planNo) count++
  if (queryParams.value.title) count++
  if (queryParams.value.status) count++
  if (queryParams.value.planType) count++
  if (queryParams.value.plannerName) count++
  if (queryParams.value.deptName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

const planList = ref([]);
const showStatusHelp = ref(false);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const auditOpen = ref(false);
const auditData = ref({});
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const materialPickerRef = ref(null);
const currentDetailIndex = ref(-1);

const data = reactive({
  form: {
    planId: null,
    planNo: null,
    title: null,
    status: '0',
    planType: '2',
    startDate: null,
    endDate: null,
    budgetAmount: 0,
    actualAmount: 0,
    description: null,
    plannerId: null,
    plannerName: null,
    deptId: null,
    deptName: null,
    detailList: []
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planNo: null,
    title: null,
    status: null,
    planType: null,
    plannerName: null,
    deptName: null,
    params: {}
  },
  auditForm: {
    planId: null,
    auditOpinion: null
  },
  rules: {
    title: [{ required: true, message: "计划标题不能为空", trigger: "blur" }],
    planType: [{ required: true, message: "计划类型不能为空", trigger: "change" }],
    startDate: [{ required: true, message: "开始日期不能为空", trigger: "change" }],
    endDate: [{ required: true, message: "结束日期不能为空", trigger: "change" }]
  },
  auditRules: {
    auditOpinion: [{ required: true, message: "请输入审批意见", trigger: "blur" }]
  }
});

const { queryParams, form, auditForm, rules, auditRules } = toRefs(data);

// 计算属性
const totalQuantity = computed(() => {
  if (!form.value.detailList) return 0;
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.planQuantity) || 0), 0).toFixed(2);
});

const totalBudgetAmount = computed(() => {
  if (!form.value.detailList) return 0;
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.budgetAmount) || 0), 0);
});

/** 查询采购计划列表 */
function getList() {
  loading.value = true;
  proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime');
  listPlan(queryParams.value).then(response => {
    planList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    loadStatusCounts();
    applySavedWidths();
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    planId: null,
    planNo: null,
    title: null,
    status: '0',
    planType: '2',
    startDate: null,
    endDate: null,
    budgetAmount: 0,
    actualAmount: 0,
    description: null,
    plannerId: null,
    plannerName: null,
    deptId: null,
    deptName: null,
    detailList: []
  };
  proxy.resetForm("planRef");
}

/** 搜索按钮操作 */
function handleQuery() {
queryParams.value.pageNum = 1;
getList();
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.value.planNo = null;
  queryParams.value.title = null;
  queryParams.value.status = null;
  queryParams.value.planType = null;
  queryParams.value.plannerName = null;
  queryParams.value.deptName = null;
  queryParams.value.params = {};
  dateRange.value = [];
  activeStatusTab.value = 'all';
  if (tableRef.value) tableRef.value.clearSort();
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.planId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
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

/** 新增按钮操作 */
async function handleAdd() {
  reset();
  const userRes = await getUserProfile();
  form.value.plannerId = userRes.data.userId;
  form.value.plannerName = userRes.data.userName;
  form.value.deptId = userRes.data.deptId;
  form.value.deptName = userRes.data.deptName;
  open.value = true;
  title.value = "添加采购计划";
}

/** 修改按钮操作 */
async function handleUpdate(row) {
  reset();
  const planId = row.planId || ids.value[0];
  const response = await getPlan(planId);
  form.value = response.data;
  if (!form.value.detailList) {
    form.value.detailList = [];
  }
  open.value = true;
  title.value = "修改采购计划";
}

/** 查看按钮操作 */
async function handleView(row) {
  const response = await getPlan(row.planId);
  viewData.value = response.data;
  if (!viewData.value.detailList) {
    viewData.value.detailList = [];
  }
  viewOpen.value = true;
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["planRef"].validate(valid => {
    if (valid) {
      form.value.budgetAmount = totalBudgetAmount.value;
      if (form.value.planId != null) {
        updatePlan(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addPlan(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const planIds = row.planId ? [row.planId] : ids.value;
  proxy.$modal.confirm('是否确认删除采购计划编号为"' + planIds + '"的数据项？').then(function() {
    return delPlan(planIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 审核按钮操作 */
async function handleAudit(row) {
  const response = await getPlan(row.planId);
  auditData.value = response.data;
  auditForm.value = {
    planId: row.planId,
    auditOpinion: null
  };
  auditOpen.value = true;
}

/** 提交审核 */
function submitAudit(passed) {
  proxy.$refs["auditRef"].validate(valid => {
    if (valid) {
      const status = passed ? '2' : '6';
      const actionText = passed ? '通过' : '驳回';
      proxy.$modal.confirm(`确认${actionText}该采购计划？`).then(() => {
        return auditPlan(auditForm.value.planId, status, auditForm.value.auditOpinion);
      }).then(() => {
        proxy.$modal.msgSuccess("审批成功");
        auditOpen.value = false;
        getList();
      }).catch(() => {});
    }
  });
}

/** 关闭按钮操作 */
function handleClose(row) {
  proxy.$modal.confirm('是否确认关闭该采购计划？').then(function() {
    return closePlan(row.planId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("关闭成功");
  }).catch(() => {});
}

/** 生成采购申请 — 跳转到采购需求页面并传递planId */
function handleGenerateRequest(row) {
  router.push({ path: '/pms/demand/request', query: { planId: row.planId } });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("pms/plan/export", {
    ...proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime')
  }, `plan_${new Date().getTime()}.xlsx`);
}

/** 添加明细 */
function handleAddDetail() {
  if (!form.value.detailList) form.value.detailList = [];
  form.value.detailList.push({
    materialId: null,
    materialCode: '',
    materialName: '',
    specification: '',
    unit: '',
    planQuantity: 0,
    budgetPrice: 0,
    budgetAmount: 0,
    requireDate: null,
    remark: null
  });
}

/** 选择物料后自动带出物料信息 */
function openMaterialPicker(index) {
  currentDetailIndex.value = index;
  materialPickerRef.value.open(form.value.detailList[index].materialId);
}
function onMaterialPickerConfirm(material) {
  if (currentDetailIndex.value >= 0) {
    const d = form.value.detailList[currentDetailIndex.value];
    d.materialId = material.materialId;
    d.materialCode = material.materialCode;
    d.materialName = material.materialName;
    d.specification = material.specModel || '';
    d.unit = material.unit || '';
  }
}
function clearMaterial(index) {
  const d = form.value.detailList[index];
  d.materialId = null;
  d.materialCode = '';
  d.materialName = '';
  d.specification = '';
  d.unit = '';
}

/** 删除明细 */
function handleDeleteDetail(index) {
  form.value.detailList.splice(index, 1);
}

function calculateAmount(row) {
  const quantity = Number(row.planQuantity) || 0;
  const price = Number(row.budgetPrice) || 0;
  row.budgetAmount = quantity * price;
}

/** 获取状态类型 */
function getStatusType(status) {
  const typeMap = {
    '0': 'info',
    '1': 'warning',
    '2': 'success',
    '3': 'primary',
    '4': 'success',
    '5': 'info',
    '6': 'danger'
  };
  return typeMap[status] || 'info';
}

/** 获取状态标签 */
function getStatusLabel(status) {
  const labelMap = {
    '0': '草稿',
    '1': '待审批',
    '2': '审批通过',
    '3': '执行中',
    '4': '已完成',
    '5': '已关闭',
    '6': '已驳回'
  };
  return labelMap[status] || '未知';
}

/** 打开编制人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.plannerId)
}
/** 编制人选择确认回调 — 自动带出所在部门 */
function onUserPickerConfirm(user) {
  form.value.plannerId = user.userId
  form.value.plannerName = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}
/** 清除编制人 */
function clearPlanner() {
  form.value.plannerId = undefined
  form.value.plannerName = undefined
  form.value.deptId = undefined
  form.value.deptName = undefined
}

getList();
onActivated(() => { getList(); })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-plan-page {
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
.pms-plan-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pms-plan-page .filter-card { padding:14px 20px 16px; }
.pms-plan-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-plan-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-plan-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-plan-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-plan-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-plan-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-plan-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-plan-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-plan-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-plan-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-plan-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pms-plan-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-plan-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-plan-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-plan-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-plan-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-plan-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-plan-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-plan-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-plan-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.pms-plan-page .field .control :deep(.el-select) { width:100%; }
.pms-plan-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-plan-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-plan-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-plan-page .field .control :deep(.el-date-editor) { width:100%; }
.pms-plan-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pms-plan-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pms-plan-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.pms-plan-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-plan-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-plan-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-plan-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-plan-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-plan-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-plan-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-plan-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-plan-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-plan-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-plan-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.pms-plan-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.pms-plan-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.pms-plan-page .status-tab.tab-audit .dot { background:var(--blue-500); }
.pms-plan-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); }
.pms-plan-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.pms-plan-page .status-tab.tab-approved .dot, .pms-plan-page .status-tab.tab-done .dot { background:var(--green-500); }
.pms-plan-page .status-tab.tab-approved .count, .pms-plan-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.pms-plan-page .status-tab.is-active.tab-approved .count, .pms-plan-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.pms-plan-page .status-tab.tab-reject .dot { background:var(--red-500); }
.pms-plan-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.pms-plan-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.pms-plan-page .status-tab.tab-void .dot { background:var(--ink-400); }
.pms-plan-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; border-radius:999px; border:1px solid var(--ink-200); background:#fff; font-size:13px; color:var(--ink-500); cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.pms-plan-page .tip-pill:hover { border-color:var(--brand-200); color:var(--brand-700); background:var(--brand-50); }
.pms-plan-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-plan-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-plan-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-plan-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pms-plan-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-plan-page .btn-soft .el-icon { font-size:14px; }
.pms-plan-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-plan-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-plan-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-plan-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-plan-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-plan-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.pms-plan-page .table-wrap { overflow-x:auto; }
.pms-plan-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-plan-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-plan-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-plan-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-plan-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-plan-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-plan-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-plan-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-plan-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-plan-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-plan-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
/* ===== Badges ===== */
.pms-plan-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-plan-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-plan-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-plan-page .badge.amber .dot { background:var(--amber-500); }
.pms-plan-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pms-plan-page .badge.blue .dot { background:var(--blue-500); }
.pms-plan-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-plan-page .badge.green .dot { background:var(--green-500); }
.pms-plan-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.pms-plan-page .badge.red .dot { background:var(--red-500); }
.pms-plan-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pms-plan-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-plan-page .badge.gray .dot { background:var(--ink-400); }
.pms-plan-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-plan-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-plan-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-plan-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-plan-page .pagination-container :deep(.el-pagination .btn-prev), .pms-plan-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-plan-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-plan-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-plan-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .pms-plan-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-plan-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-plan-page .toolbar { flex-wrap:wrap; gap:10px; } }

.detail-summary {
  text-align: right;
  padding: 10px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-top: none;
  font-weight: bold;
}
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

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
