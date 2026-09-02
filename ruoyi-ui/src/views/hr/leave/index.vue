<template>
  <div class="app-container hr-leave-page">
    <!-- 筛选条件卡片 -->
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
          <label>离职单号</label>
          <div class="control">
            <el-input v-model="queryParams.leaveNo" placeholder="请输入离职单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>审批状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in auditStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>离职类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.leaveType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in leaveEmpTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>交接状态</label>
          <div class="control">
            <el-input v-model="queryParams.handoverStatus" placeholder="请输入交接状态" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>离职日期</label>
          <div class="control">
            <el-date-picker v-model="leaveDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
    <!-- 状态统计页签 -->
    <div class="status-tabs">
      <div class="tabs-track">
        <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
          <span class="dot"></span>
          <span>全部</span>
          <span class="count">{{ statusCounts.all }}</span>
        </button>
        <button v-for="s in statusTabList" :key="s.value" class="status-tab"
          :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
          @click="handleStatusTabClick(s.value)">
          <span class="dot"></span>
          <span>{{ s.label }}</span>
          <span class="count">{{ statusCounts[s.value] || 0 }}</span>
        </button>
      </div>
      <button class="tip-pill" @click="showStatusHelp = true">
        <el-icon><QuestionFilled /></el-icon>
        <span>业务操作说明</span>
      </button>
    </div>
    <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:leave:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:leave:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:leave:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:leave:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_leave_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrLeaveList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="离职单号" prop="leaveNo" key="leaveNo" align="center" :width="colWidth('leaveNo', 180)" resizable v-if="columns.leaveNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.leaveNo }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 110)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="离职类型" prop="leaveType" key="leaveType" align="center" :width="colWidth('leaveType', 120)" resizable v-if="columns.leaveType.visible">
            <template #default="scope">
              <span class="badge" :class="leaveTypeBadgeClass(scope.row.leaveType)" v-if="scope.row.leaveType !== undefined && scope.row.leaveType !== null && scope.row.leaveType !== ''">
                <span class="dot"></span>{{ leaveTypeLabel(scope.row.leaveType) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="离职日期" prop="leaveDate" key="leaveDate" align="center" :width="colWidth('leaveDate', 120)" resizable v-if="columns.leaveDate.visible" />
          <el-table-column label="离职原因" prop="reason" key="reason" align="center" :width="colWidth('reason', 160)" resizable show-overflow-tooltip v-if="columns.reason.visible" />
          <el-table-column label="交接状态" prop="handoverStatus" key="handoverStatus" align="center" :width="colWidth('handoverStatus', 110)" resizable v-if="columns.handoverStatus.visible">
            <template #default="scope">
              <span class="badge" :class="handoverStatusClass(scope.row.handoverStatus)"><span class="dot"></span>{{ scope.row.handoverStatus || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="审批状态" prop="auditStatus" key="auditStatus" align="center" :width="colWidth('auditStatus', 110)" resizable v-if="columns.auditStatus.visible">
            <template #default="scope">
              <span class="badge" :class="auditStatusClass(scope.row.auditStatus)"><span class="dot"></span>{{ auditStatusLabel(scope.row.auditStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:leave:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:leave:edit']">修改</el-button>
                <el-button link type="warning" icon="Finished" @click="handleAudit(scope.row)" v-if="scope.row.auditStatus === '0'" v-hasPermi="['hr:leave:audit']">审批</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:leave:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrLeaveRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
          <el-form-item label="离职单号" prop="leaveNo">
            <el-input v-model="form.leaveNo" placeholder="自动生成" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
          <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
            <template #append>
              <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
              <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
            </template>
          </el-input>
        </el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="工号" prop="employeeNo">
            <el-input v-model="form.employeeNo" placeholder="选择人员后自动带出" />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>离职信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
          <el-form-item label="离职类型" prop="leaveType">
            <el-select v-model="form.leaveType" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in leaveEmpTypeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="离职日期" prop="leaveDate">
            <el-date-picker v-model="form.leaveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交接状态" prop="handoverStatus">
            <el-input v-model="form.handoverStatus" placeholder="请输入交接状态" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="离岗体检" prop="healthCheck">
            <el-input v-model="form.healthCheck" placeholder="请输入离岗体检" />
          </el-form-item>
        </el-col>
<el-col :span="12">
<el-form-item label="离岗体检" prop="healthCheck">
<el-input v-model="form.healthCheck" placeholder="请输入离岗体检" />
</el-form-item>
</el-col>
<el-col :span="24">
          <el-form-item label="交接清单" prop="handoverRemark">
            <el-input v-model="form.handoverRemark" placeholder="请输入交接清单" />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>原因说明</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e3">
              <el-row :gutter="20">
                <el-col :span="24">
          <el-form-item label="离职原因" prop="reason">
            <el-input v-model="form.reason" placeholder="请输入离职原因" />
          </el-form-item>
        </el-col>
                <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入备注" />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2" style="display:block">
              <el-alert v-if="form.auditStatus === '2'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该记录已被驳回，请根据审核意见修改后重新提交</template>
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
                    <div class="rd-timeline-comment" v-if="log.auditRemark"><strong>审核意见：</strong>{{ log.auditRemark }}</div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">离职详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.leaveNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">单号：{{ viewData.leaveNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">离职单号</span><div class="rd-value">{{ viewData.leaveNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>离职信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">离职类型</span><div class="rd-value"><dict-tag :options="leaveEmpTypeOptions" :value="viewData.leaveType"/></div></div>
              <div class="rd-item"><span class="rd-label">离职日期</span><div class="rd-value">{{ viewData.leaveDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">交接状态</span><div class="rd-value">{{ handoverStatusLabel(viewData.handoverStatus) }}</div></div>
<div class="rd-item"><span class="rd-label">离岗体检</span><div class="rd-value">{{ viewData.healthCheck || '-' }}</div></div>
<div class="rd-item rd-item--full"><span class="rd-label">交接清单</span><div class="rd-value">{{ viewData.handoverRemark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2" style="display:block">
            <div class="rd-timeline" v-if="viewAuditLogs().length">
              <div class="rd-timeline-item" v-for="(log, idx) in viewAuditLogs()" :key="idx">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime || '-' }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark"><strong>审核意见：</strong>{{ log.auditRemark }}</div>
                </div>
              </div>
            </div>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
              <p class="rd-empty-text">暂无审核记录</p>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>原因说明</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">离职原因</span><div class="rd-value">{{ viewData.reason || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="auditOpen" width="912px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">离职审批</div>
            <div class="rd-detail-header-sub" v-if="auditData.leaveNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">离职单号：{{ auditData.leaveNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">离职单号</span><div class="rd-value">{{ auditData.leaveNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ auditData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ auditData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">当前审批状态</span><div class="rd-value">
                <span class="badge" :class="auditStatusClass(auditData.auditStatus)" v-if="auditData.auditStatus !== undefined && auditData.auditStatus !== null && auditData.auditStatus !== ''"><span class="dot"></span>{{ auditStatusLabel(auditData.auditStatus) }}</span>
                <span v-else>-</span>
              </div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>离职信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">离职类型</span><div class="rd-value"><dict-tag v-if="auditData.leaveType !== undefined && auditData.leaveType !== null && auditData.leaveType !== ''" :options="leaveEmpTypeOptions" :value="auditData.leaveType" /><span v-else>-</span></div></div>
              <div class="rd-item"><span class="rd-label">离职日期</span><div class="rd-value">{{ auditData.leaveDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">交接状态</span><div class="rd-value">{{ handoverStatusLabel(auditData.handoverStatus) }}</div></div>
<div class="rd-item"><span class="rd-label">离岗体检</span><div class="rd-value">{{ auditData.healthCheck || '-' }}</div></div>
<div class="rd-item rd-item--full"><span class="rd-label">交接清单</span><div class="rd-value">{{ auditData.handoverRemark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">离职原因</span><div class="rd-value">{{ auditData.reason || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ auditData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('a1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
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
                  <div class="rd-timeline-comment" v-if="log.auditRemark"><strong>审核意见：</strong>{{ log.auditRemark }}</div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>审批意见</div></div>
          <div class="rd-card-body" style="display:block">
            <el-input v-model="auditData.auditRemark" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入审批意见，驳回时必填" />
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" :loading="auditSubmitting" @click="submitAudit('1')">通 过</el-button>
        <el-button type="danger" :loading="auditSubmitting" @click="submitAudit('2')">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 业务操作说明弹窗 ===== -->
    <el-dialog v-model="showStatusHelp" title="离职管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是离职管理？</div>
          <div class="highlight-card-body">
            <p><strong>离职管理</strong>是人力资源模块中办理员工离职手续的核心单据，记录员工从提出离职申请到审批完成正式离岗的全过程信息。</p>
            <p>离职单涵盖离职类型、工作交接、离岗体检等内容，依据《劳动合同法》解除或终止用工关系，审批通过后离职手续办理完毕。</p>
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「审批」通过</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="success">已审核</el-tag>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「审批」驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="danger">已驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">修改后重新提交</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="warning">待审批</el-tag>
          </div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待审批">离职单新增后的初始状态。可修改、删除单据；具有审批权限的人员可执行审批操作</el-descriptions-item>
          <el-descriptions-item label="已审核">审批人审核通过，离职手续完成。单据不可再修改、删除，可查看详情及审核记录</el-descriptions-item>
          <el-descriptions-item label="已驳回">审批人驳回并给出审核意见。需点击「修改」查看驳回原因，修改后保存即重新提交回到待审批状态</el-descriptions-item>
        </el-descriptions>
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息（卡片区一）</div>
          <div class="highlight-card-body">
            <p><strong>姓名</strong>：点击输入框从员工档案弹框选择，工号自动带出 <span style="color: #f56c6c;">*必填</span></p>
            <p><strong>离职单号</strong>：保存后系统自动生成，无需填写</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px">
          <div class="highlight-card-title">离职信息（卡片区二）</div>
          <div class="highlight-card-body">
<p><strong>离职类型</strong>：字典下拉选择（主动离职、辞退等） <span style="color: #f56c6c;">*必填</span></p>
<p><strong>离职日期</strong>：日期选择器选择 <span style="color: #f56c6c;">*必填</span></p>
<p><strong>交接状态 / 交接清单</strong>：填写工作交接进展与清单明细；交接状态必填，交接清单选填</p>
<p><strong>离岗体检</strong>：离岗体检情况说明</p>
</div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px">
          <div class="highlight-card-title">原因说明（卡片区三）</div>
          <div class="highlight-card-body">
            <p><strong>离职原因</strong>：填写员工离职的具体原因</p>
            <p><strong>备注</strong>：补充说明信息，选填</p>
          </div>
        </div>
        <h4>五、核心业务规则</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">审批规则</div>
          <div class="highlight-card-body">
            <p>1. 离职单保存后<strong>单号自动生成</strong>，审批状态默认为<strong>待审批</strong></p>
            <p>2. 仅<strong>待审批</strong>状态可执行审批；审批时需填写审批意见（驳回时必填）</p>
            <p>3. <strong>已驳回</strong>的单据不能直接再次审批，必须修改后重新提交</p>
            <p>4. 每次审批（通过/驳回）均写入审核记录，形成完整审批轨迹</p>
<p>5. <strong>审批通过后自动同步员工档案</strong>：员工状态置为离职，并更新离职日期、离职原因</p>
          </div>
        </div>
        <div class="highlight-card highlight-danger" style="margin-top: 12px">
          <div class="highlight-card-title">注意事项</div>
          <div class="highlight-card-body">
            <p style="color: #f56c6c;">已审核的离职单不允许修改和删除，如信息有误请联系管理员处理。</p>
          </div>
        </div>
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建离职单：</strong>点击「新增」，选择员工档案自动带出信息，填写离职类型与交接信息，保存后单号自动生成，状态为待审批
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>离职审批：</strong>在待审批状态点击「审批」，核对交接信息后选择通过或驳回并填写审批意见
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>驳回整改：</strong>被驳回的单据修改后保存即重新提交，回到待审批状态重新接受审批
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>离职完成：</strong>审批通过后状态变为已审核，用工关系解除，离职手续办理完毕
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- ===== 人员选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
  </div>
</template>

<script setup name="HrLeave">
import { listHrLeave, getHrLeave, addHrLeave, updateHrLeave, delHrLeave, auditHrLeave } from '@/api/hr/hrLeave'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'

const { proxy } = getCurrentInstance()
const { hr_leave_emp_type: leaveEmpTypeOptions, hr_audit_status: auditStatusOptions } = proxy.useDict('hr_leave_emp_type', 'hr_audit_status')

const hrLeaveList = ref([])
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusTabList = computed(() => {
  return auditStatusOptions.value.map(d => ({ label: d.label, value: d.value }))
})
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listHrLeave({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    auditStatusOptions.value.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.auditStatus] !== undefined) counts[r.auditStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-approved', '2': 'tab-reject' }
  return map[value] || ''
}
function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  if (tab === 'all') {
    queryParams.value.auditStatus = undefined
  } else {
    queryParams.value.auditStatus = tab
  }
  queryParams.value.pageNum = 1
  getList()
}
const auditOpen = ref(false)
const auditSubmitting = ref(false)
const auditData = ref({})
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
const showAdvanced = ref(false)
const leaveDateRange = ref([])
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, e0: false, e1: false, e2: false, e3: false, a1: false })
const employeePickerRef = ref(null)

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_leave_index')

/** 列显隐配置 */
const defaultColumns = {
  leaveNo: { label: '离职单号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  leaveType: { label: '离职类型', visible: true },
  leaveDate: { label: '离职日期', visible: true },
  reason: { label: '离职原因', visible: true },
  handoverStatus: { label: '交接状态', visible: true },
  auditStatus: { label: '审批状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_leave_columns')
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

/** 审批状态徽章映射（与 hr_audit_status 字典一致：0待审批 1已审核 2已驳回） */
function auditStatusClass(v) {
  const map = { '0': 'amber', '1': 'green', '2': 'red' }
  return map[v] || 'gray'
}
function auditStatusLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (auditStatusOptions.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}

/** 交接状态徽章映射 */
function handoverStatusClass(v) {
  const map = { '已完成': 'green', '已交接': 'green', '待交接': 'amber', '交接中': 'blue', '未交接': 'red' }
  return map[v] || 'gray'
}

/** 离职类型徽章映射 */
function leaveTypeLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (leaveEmpTypeOptions.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}
function leaveTypeBadgeClass(v) {
  const map = { '0': 'blue', '1': 'amber', '2': 'red' }
  return map[v] || 'gray'
}

/** 交接状态文字映射（库内 char(1) 编码：0未完成 1已完成，无对应字典） */
function handoverStatusLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const map = { '0': '未完成', '1': '已完成', '未完成': '未完成', '已完成': '已完成' }
  return map[v] || String(v)
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.leaveNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.auditStatus) count++
  if (queryParams.value.leaveType) count++
  if (queryParams.value.handoverStatus) count++
  if (leaveDateRange.value && leaveDateRange.value.length === 2) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, leaveNo: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined, leaveType: undefined, handoverStatus: undefined, auditStatus: undefined },
rules: {
employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
leaveType: [{ required: true, message: '请选择离职类型', trigger: 'change' }],
leaveDate: [{ required: true, message: '离职日期不能为空', trigger: 'blur' }],
handoverStatus: [{ required: true, message: '请输入交接状态', trigger: 'blur' }],
reason: [{ required: true, message: '离职原因不能为空', trigger: 'blur' }]
}
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrLeave(queryParams.value).then(response => {
    hrLeaveList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
    loadStatusCounts()
  })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  queryParams.value.params = {}
  proxy.addDateRange(queryParams.value, leaveDateRange.value, 'LeaveDate')
  getList()
}

function resetQuery() {
  queryParams.value.leaveNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.auditStatus = undefined
  queryParams.value.leaveType = undefined
  queryParams.value.handoverStatus = undefined
  leaveDateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.leaveId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function reset() {
  form.value = {
    leaveNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    leaveType: undefined,
    leaveDate: undefined,
    reason: undefined,
    handoverStatus: undefined,
    handoverRemark: undefined,
    healthCheck: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined,
    remark: undefined
  }
  proxy.resetForm('hrLeaveRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增离职'
}

function handleUpdate(row) {
  reset()
  const leaveId = row.leaveId || ids.value[0]
  getHrLeave(leaveId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改离职'
  })
}

function handleView(row) {
  const leaveId = row.leaveId
  getHrLeave(leaveId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

/** 查看弹窗审核记录：优先使用审核日志，兼容历史数据合成单节点 */
function viewAuditLogs() {
  const d = viewData.value || {}
  if (d.auditLogList && d.auditLogList.length) return d.auditLogList
  if (d.auditStatus === undefined || d.auditStatus === null || d.auditStatus === '') return []
  return [{ auditAction: d.auditStatus, auditBy: d.auditBy, auditTime: d.auditTime, auditRemark: d.auditRemark }]
}

/** 审批 —— 打开审批弹窗 */
function handleAudit(row) {
  getHrLeave(row.leaveId).then(response => {
    const d = response.data || {}
    auditData.value = {
      leaveId: d.leaveId,
      leaveNo: d.leaveNo,
      employeeName: d.employeeName,
      employeeNo: d.employeeNo,
      leaveType: d.leaveType,
      leaveDate: d.leaveDate,
      handoverStatus: d.handoverStatus,
      handoverRemark: d.handoverRemark,
      healthCheck: d.healthCheck,
      reason: d.reason,
      remark: d.remark,
      auditStatus: d.auditStatus,
      auditLogList: d.auditLogList || [],
      auditRemark: ''
    }
    auditOpen.value = true
  })
}

/** 提交审批（action：1通过 2驳回） */
function submitAudit(action) {
  if (action === '2' && !auditData.value.auditRemark) {
    proxy.$modal.msgWarning('驳回时请填写审批意见')
    return
  }
  auditSubmitting.value = true
  auditHrLeave(auditData.value.leaveId, action, auditData.value.auditRemark).then(() => {
    proxy.$modal.msgSuccess(action === '1' ? '审批通过' : '已驳回')
    auditOpen.value = false
    getList()
  }).finally(() => {
    auditSubmitting.value = false
  })
}

function submitForm() {
  proxy.$refs['hrLeaveRef'].validate(valid => {
    if (valid) {
      if (form.value.leaveId != undefined) {
        updateHrLeave(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrLeave(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const leaveIds = row.leaveId || ids.value
  proxy.$modal.confirm('是否确认删除选中的离职？').then(function() {
    return delHrLeave(leaveIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/leave/export', { ...queryParams.value }, `hrLeave_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** ===== 人员弹框选择 ===== */
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName, employeeNo: emp.employeeNo };
  Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k]; });
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-leave-page {
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

/* ===== Surface Card ===== */
.hr-leave-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-leave-page .filter-card { padding:14px 20px 16px; }
.hr-leave-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-leave-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-leave-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-leave-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-leave-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-leave-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-leave-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-leave-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-leave-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-leave-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-leave-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-leave-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-leave-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-leave-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-leave-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-leave-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-leave-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-leave-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-leave-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-leave-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-leave-page .field .control :deep(.el-select) { width:100%; }
.hr-leave-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-leave-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-leave-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-leave-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-leave-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-leave-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-leave-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-leave-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-leave-page .btn-soft .el-icon { font-size:14px; }
.hr-leave-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-leave-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-leave-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-leave-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-leave-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-leave-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-leave-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-leave-page .table-wrap { overflow-x:auto; }
.hr-leave-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-leave-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-leave-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-leave-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-leave-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-leave-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-leave-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-leave-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-leave-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-leave-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-leave-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-leave-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-leave-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-leave-page .badge.amber .dot { background:var(--amber-500); }
.hr-leave-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-leave-page .badge.blue .dot { background:var(--blue-500); }
.hr-leave-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-leave-page .badge.green .dot { background:var(--green-500); }
.hr-leave-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-leave-page .badge.red .dot { background:var(--red-500); }
.hr-leave-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-leave-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-leave-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-leave-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-leave-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-leave-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-leave-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-leave-page .pagination-container :deep(.el-pagination .btn-prev), .hr-leave-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-leave-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-leave-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-leave-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式（遵循 detail-page-style-guide 规范） ===== */
.hr-leave-page .rd-page { max-height: 62vh; overflow-y: auto; }
.hr-leave-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-leave-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-leave-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-leave-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-leave-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-leave-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-leave-page .rd-card-body { padding: 16px; }
.hr-leave-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-leave-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-leave-page .rd-item--full { grid-column: 1 / -1; }
.hr-leave-page .rd-label { font-size: 12px; color: #909399; }
.hr-leave-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-leave-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-leave-page .rd-empty-icon { margin-bottom: 8px; }
.hr-leave-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-leave-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-leave-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-leave-page .rd-detail-header-main { flex: 1; }
.hr-leave-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-leave-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-leave-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-leave-page .rd-detail-header-no { font-size: 13px; color: #909399; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.hr-leave-page .rd-card { animation: rdFadeIn 0.4s ease-out forwards; }
.hr-leave-page .rd-card:nth-child(2) { animation-delay: 0.06s; }
.hr-leave-page .rd-card:nth-child(3) { animation-delay: 0.12s; }
.hr-leave-page .rd-card:nth-child(4) { animation-delay: 0.18s; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-leave-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-leave-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-leave-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== 状态统计页签 ===== */
.hr-leave-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.hr-leave-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.hr-leave-page .tabs-track::-webkit-scrollbar { display:none; }
.hr-leave-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:6px; font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s ease; white-space:nowrap; border:1px solid transparent; background:transparent; }
.hr-leave-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); flex-shrink:0; }
.hr-leave-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.hr-leave-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.hr-leave-page .status-tab.is-active { background:var(--blue-50); color:var(--blue-700); font-weight:600; border-color:#bfdbfe; }
.hr-leave-page .status-tab.is-active .count { background:var(--blue-500); color:#fff; }
.hr-leave-page .status-tab.is-active .dot { background:var(--blue-500); }
.hr-leave-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.hr-leave-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.hr-leave-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.hr-leave-page .status-tab.tab-approved .dot { background:var(--green-500); }
.hr-leave-page .status-tab.tab-approved .count { background:var(--green-50); color:var(--green-700); }
.hr-leave-page .status-tab.is-active.tab-approved .count { background:var(--green-500); color:#fff; }
.hr-leave-page .status-tab.tab-reject .dot { background:var(--red-500); }
.hr-leave-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.hr-leave-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.hr-leave-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; white-space:nowrap; flex-shrink:0; transition:all .15s ease; }
.hr-leave-page .tip-pill:hover { background:#fffbeb; border-color:#f59e0b; color:#78350f; }
.hr-leave-page .tip-pill .el-icon { font-size:14px; }

/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height:520px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.status-help-content .flow-item { display:flex; align-items:center; gap:8px; }
.status-help-content .flow-arrow { color:#909399; font-size:16px; }
.status-help-content .highlight-card { border-radius:8px; padding:16px; border:1px solid; }
.status-help-content .highlight-card-title { font-size:14px; font-weight:600; margin-bottom:8px; display:flex; align-items:center; }
.status-help-content .highlight-card-body { font-size:13px; color:#606266; line-height:1.6; }
.status-help-content .highlight-card-body p { margin:4px 0; }
.status-help-content .highlight-primary { background-color:#ecf5ff; border-color:#a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color:#409eff; }
.status-help-content .highlight-warning { background-color:#fdf6ec; border-color:#f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color:#e6a23c; }
.status-help-content .highlight-danger { background-color:#fef0f0; border-color:#fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color:#f56c6c; }
.reject-alert { margin-bottom: 16px; }

</style>
