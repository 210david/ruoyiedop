<template>
  <div class="app-container hr-leaverequest-page">
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
          <label>请假单号</label>
          <div class="control">
            <el-input v-model="queryParams.leaveReqNo" placeholder="请输入请假单号" clearable @keyup.enter="handleQuery">
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
          <label>部门</label>
          <div class="control">
            <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>假别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.leaveType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in leaveTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>开始时间</label>
          <div class="control">
            <el-date-picker v-model="startTimeRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>结束时间</label>
          <div class="control">
            <el-date-picker v-model="endTimeRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:leaveRequest:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:leaveRequest:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:leaveRequest:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:leaveRequest:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_leaveRequest_index"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrLeaveRequestList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="请假单号" prop="leaveReqNo" key="leaveReqNo" align="center" :width="colWidth('leaveReqNo', 160)" resizable v-if="columns.leaveReqNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.leaveReqNo }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :min-width="colWidth('employeeName', 110)" show-overflow-tooltip resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 110)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="部门" prop="deptName" key="deptName" align="center" :width="colWidth('deptName', 120)" resizable v-if="columns.deptName.visible" />
          <el-table-column label="假别" prop="leaveType" key="leaveType" align="center" :width="colWidth('leaveType', 110)" resizable v-if="columns.leaveType.visible">
            <template #default="scope">
              <span class="badge" :class="leaveTypeBadgeClass(scope.row.leaveType)" v-if="scope.row.leaveType !== undefined && scope.row.leaveType !== null && scope.row.leaveType !== ''">
                <span class="dot"></span>{{ leaveTypeLabel(scope.row.leaveType) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="开始时间" prop="startTime" key="startTime" align="center" :width="colWidth('startTime', 120)" resizable v-if="columns.startTime.visible" />
          <el-table-column label="结束时间" prop="endTime" key="endTime" align="center" :width="colWidth('endTime', 120)" resizable v-if="columns.endTime.visible" />
          <el-table-column label="请假天数" prop="leaveDays" key="leaveDays" align="center" :width="colWidth('leaveDays', 110)" resizable v-if="columns.leaveDays.visible" />
          <el-table-column label="可用余额" prop="remainingDays" key="remainingDays" align="center" :width="colWidth('remainingDays', 110)" resizable v-if="columns.remainingDays.visible">
            <template #default="scope">
              <span v-if="scope.row.remainingDays != null" :class="{ 'text-warning': (scope.row.remainingDays - (scope.row.frozenDays || 0)) < 1 }">{{ (scope.row.remainingDays - (scope.row.frozenDays || 0)).toFixed(1) }} 天</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="审批状态" prop="auditStatus" key="auditStatus" align="center" :width="colWidth('auditStatus', 100)" resizable v-if="columns.auditStatus?.visible !== false">
            <template #default="scope">
              <span class="audit-tag" :class="auditStatusClass(scope.row.auditStatus)">{{ auditStatusLabel(scope.row.auditStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:leaveRequest:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:leaveRequest:edit']" v-if="scope.row.auditStatus !== '1'">修改</el-button>
                <el-button link type="warning" icon="Finished" @click="handleAudit(scope.row)" v-if="scope.row.auditStatus === '0' || !scope.row.auditStatus" v-hasPermi="['hr:leaveRequest:audit']">审批</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:leaveRequest:remove']" v-if="scope.row.auditStatus !== '1'">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrLeaveRequestRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="请假单号" prop="leaveReqNo"><el-input v-model="form.leaveReqNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="选择人员后自动带出" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="部门" prop="deptName"><el-input v-model="form.deptName" placeholder="选择人员后自动带出" readonly /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>请假信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="假别" prop="leaveType"><el-select v-model="form.leaveType" placeholder="请选择" clearable style="width: 100%" @change="onLeaveTypeChange"><el-option v-for="d in leaveTypeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="假期余额">
                  <div class="leave-balance-info" v-if="leaveBalanceInfo">
                    <span class="balance-tag" :class="{ 'is-warning': computedAvailableBalance < 1 }">可用 {{ computedAvailableBalance }} 天</span>
                    <span class="balance-detail">总额 {{ leaveBalanceInfo.totalDays }} 天 / 已休 {{ leaveBalanceInfo.usedDays }} 天 / 冻结 {{ leaveBalanceInfo.frozenDays || 0 }} 天</span>
                  </div>
                  <span v-else-if="leaveBalanceWarning" class="balance-warning">{{ leaveBalanceWarning }}</span>
                  <span v-else class="balance-placeholder">请先选择人员和假别</span>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" :default-time="defaultStartTime" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" :default-time="defaultEndTime" :disabled-date="disabledEndDate" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="请假天数" prop="leaveDays"><el-input :model-value="computedLeaveDaysDisplay" disabled placeholder="自动计算（考虑排班休息时间，≥4小时=1天，<4小时=0.5天）" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>事由说明</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e3">
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="请假原因" prop="reason"><el-input v-model="form.reason" placeholder="请输入请假原因" /></el-form-item></el-col>
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

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">请假详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.leaveReqNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">请假单号：{{ viewData.leaveReqNo }}</span>
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
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">请假单号</span><div class="rd-value">{{ viewData.leaveReqNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>请假信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">假别</span><div class="rd-value"><dict-tag :options="leaveTypeOptions" :value="viewData.leaveType"/></div></div>
              <div class="rd-item" v-if="viewData.remainingDays != null"><span class="rd-label">假期余额</span><div class="rd-value">可用 {{ (viewData.remainingDays - (viewData.frozenDays || 0)).toFixed(1) }} 天（总额 {{ viewData.totalDays }} 天 / 已休 {{ viewData.usedDays }} 天 / 冻结 {{ viewData.frozenDays || 0 }} 天）</div></div>
              <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewData.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ viewData.endTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">请假天数</span><div class="rd-value">{{ viewData.leaveDays != null ? viewData.leaveDays + ' 天' : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>事由说明</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c3">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">请假原因</span><div class="rd-value">{{ viewData.reason || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c2">
            <el-alert v-if="viewData.auditStatus === '2'" type="warning" :closable="false" show-icon class="reject-alert">
              <template #title>该记录已被驳回，请根据审核意见修改后重新提交</template>
            </el-alert>
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
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <file-preview ref="filePreviewRef" />

    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="auditOpen" width="912px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">请假审批</div>
            <div class="rd-detail-header-sub" v-if="auditData.leaveReqNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">请假单号：{{ auditData.leaveReqNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">请假单号</span><div class="rd-value">{{ auditData.leaveReqNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ auditData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ auditData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ auditData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>请假信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">假别</span><div class="rd-value"><dict-tag :options="leaveTypeOptions" :value="auditData.leaveType"/></div></div>
              <div class="rd-item"><span class="rd-label">假期余额</span><div class="rd-value" v-if="auditData.remainingDays != null">可用 {{ (auditData.remainingDays - (auditData.frozenDays || 0)).toFixed(1) }} 天（总额 {{ auditData.totalDays }} 天 / 已休 {{ auditData.usedDays }} 天 / 冻结 {{ auditData.frozenDays || 0 }} 天）</div><div class="rd-value" v-else>-</div></div>
              <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ auditData.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ auditData.endTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">请假天数</span><div class="rd-value">{{ auditData.leaveDays != null ? auditData.leaveDays + ' 天' : '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">请假原因</span><div class="rd-value">{{ auditData.reason || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
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
            <el-input v-model="auditData.auditRemark" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入审批意见，驳回时建议填写驳回原因" />
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" :loading="auditSubmitting" @click="submitAudit('1')">通 过</el-button>
        <el-button type="danger" :loading="auditSubmitting" @click="submitAudit('2')">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 人员选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
  </div>
</template>

<script setup name="HrLeaveRequest">
import { listHrLeaveRequest, getHrLeaveRequest, addHrLeaveRequest, updateHrLeaveRequest, delHrLeaveRequest, auditHrLeaveRequest } from '@/api/hr/hrLeaveRequest'
import { listHrLeaveBalance } from '@/api/hr/hrLeaveBalance'
import { useColumnResize } from '@/composables/useColumnResize'
import FilePreview from '@/components/FilePreview/index.vue'
import { downloadFile } from '@/utils/downloadFile'
import EmployeePicker from '@/components/EmployeePicker'

const { proxy } = getCurrentInstance()
const { hr_leave_type: leaveTypeOptions } = proxy.useDict('hr_leave_type')
const { hr_audit_status: auditStatusOptions } = proxy.useDict('hr_audit_status')

const hrLeaveRequestList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const auditOpen = ref(false)
const auditSubmitting = ref(false)
const auditData = ref({})
const title = ref('')
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const showAdvanced = ref(false)
const startTimeRange = ref([])
const endTimeRange = ref([])
const filePreviewRef = ref(null)
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, e0: false, e1: false, e2: false, e3: false })
const employeePickerRef = ref(null)
const leaveBalanceInfo = ref(null)
const leaveBalanceWarning = ref('')

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_leaveRequest_index')

const defaultColumns = {
  leaveReqNo: { label: '请假单号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  deptName: { label: '部门', visible: true },
  leaveType: { label: '假别', visible: true },
  startTime: { label: '开始时间', visible: true },
  endTime: { label: '结束时间', visible: true },
  leaveDays: { label: '请假天数', visible: true },
  remainingDays: { label: '假期余额', visible: true },
  auditStatus: { label: '审批状态', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_leaveRequest_index')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, leaveReqNo: undefined, employeeName: undefined, employeeNo: undefined, deptName: undefined, leaveType: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    leaveType: [{ required: true, message: '假别不能为空', trigger: 'change' }],
    startTime: [{ required: true, message: '开始时间不能为空', trigger: 'change' }],
    endTime: [
      { required: true, message: '结束时间不能为空', trigger: 'change' },
      { validator: validateEndTime, trigger: 'change' }
    ],
    leaveDays: [{ validator: validateLeaveDays, trigger: 'change' }],
    reason: [{ required: true, message: '请假原因不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.leaveReqNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.deptName) count++
  if (queryParams.value.leaveType) count++
  if (startTimeRange.value && startTimeRange.value.length === 2) count++
  if (endTimeRange.value && endTimeRange.value.length === 2) count++
  return count
})

/** 开始时间默认08:30，结束时间默认17:30 */
const defaultStartTime = new Date(2000, 0, 1, 8, 30, 0)
const defaultEndTime = new Date(2000, 0, 1, 17, 30, 0)

/** 结束时间不能早于开始时间 */
function validateEndTime(rule, value, callback) {
  if (value && form.value.startTime) {
    const s = new Date(String(form.value.startTime).replace(/-/g, '/'))
    const e = new Date(String(value).replace(/-/g, '/'))
    if (e < s) {
      callback(new Error('结束时间不能早于开始时间'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

/** 可用余额 = 剩余天数 - 冻结天数 */
const computedAvailableBalance = computed(() => {
  if (!leaveBalanceInfo.value) return 0
  const remaining = parseFloat(leaveBalanceInfo.value.remainingDays) || 0
  const frozen = parseFloat(leaveBalanceInfo.value.frozenDays) || 0
  return Math.round((remaining - frozen) * 10) / 10
})

/** 请假天数不能超过可用余额（剩余 - 冻结） */
function validateLeaveDays(rule, value, callback) {
  if (computedLeaveDays.value != null && leaveBalanceInfo.value) {
    if (computedLeaveDays.value > computedAvailableBalance.value) {
      callback(new Error('请假天数(' + computedLeaveDays.value + '天)不能超过可用余额(' + computedAvailableBalance.value + '天，剩余' + leaveBalanceInfo.value.remainingDays + '天 - 冻结' + (leaveBalanceInfo.value.frozenDays || 0) + '天)'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

/** 禁用结束时间早于开始时间的日期 */
function disabledEndDate(date) {
  if (!form.value.startTime) return false
  const s = new Date(String(form.value.startTime).replace(/-/g, '/'))
  return date < new Date(s.getFullYear(), s.getMonth(), s.getDate())
}

/** 根据开始/结束时间自动计算请假天数（大于4小时算1天，不足4小时算0.5天，跨天累加） */
const computedLeaveDays = computed(() => {
  const f = form.value
  if (!f.startTime || !f.endTime) return null
  const s = new Date(String(f.startTime).replace(/-/g, '/'))
  const e = new Date(String(f.endTime).replace(/-/g, '/'))
  if (isNaN(s.getTime()) || isNaN(e.getTime()) || e < s) return null
  // 计算跨天数（完整天数 * 1天 + 剩余小时按规则折算）
  const diffMs = e - s
  const fullDays = Math.floor(diffMs / 86400000)
  const remainHours = (diffMs - fullDays * 86400000) / 3600000
  // 剩余不足4小时算0.5天，大于等于4小时算1天
  let extraDays = 0
  if (remainHours > 0) {
    extraDays = remainHours >= 4 ? 1 : 0.5
  }
  const days = fullDays + extraDays
  return days < 0.5 ? 0.5 : days
})

/** 请假天数显示（含余额超限提示，可用=剩余-冻结） */
const computedLeaveDaysDisplay = computed(() => {
  if (computedLeaveDays.value == null) return ''
  let text = computedLeaveDays.value + ' 天'
  if (leaveBalanceInfo.value) {
    if (computedLeaveDays.value > computedAvailableBalance.value) {
      text += ' （超出可用余额' + (computedLeaveDays.value - computedAvailableBalance.value).toFixed(1) + '天）'
    }
  }
  return text
})

/** 监听开始/结束时间变化，自动填充请假天数 */
watch(() => [form.value.startTime, form.value.endTime], () => {
  if (computedLeaveDays.value != null) { form.value.leaveDays = computedLeaveDays.value }
  // 触发请假天数余额校验
  if (form.value.leaveDays != null) {
    proxy.$refs['hrLeaveRequestRef']?.validateField('leaveDays')
  }
})

/** 假别变更后查询假期余额 */
function onLeaveTypeChange() {
  leaveBalanceInfo.value = null
  leaveBalanceWarning.value = ''
  if (!form.value.leaveType || !form.value.employeeId) return
  fetchLeaveBalance()
}

/** 查询当前员工对应假别的假期余额 */
function fetchLeaveBalance() {
  if (!form.value.employeeId || !form.value.leaveType) return
  const year = new Date().getFullYear()
  listHrLeaveBalance({
    employeeId: form.value.employeeId,
    leaveType: form.value.leaveType,
    year: year,
    pageNum: 1,
    pageSize: 10
  }).then(response => {
    if (response.rows && response.rows.length > 0) {
      leaveBalanceInfo.value = response.rows[0]
      // 触发请假天数余额校验
      if (form.value.leaveDays != null) {
        proxy.$refs['hrLeaveRequestRef']?.validateField('leaveDays')
      }
    } else {
      leaveBalanceInfo.value = null
      leaveBalanceWarning.value = '未找到该假别的假期余额记录'
    }
  })
}

function getList() {
  loading.value = true
  listHrLeaveRequest(queryParams.value).then(response => {
    hrLeaveRequestList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, startTimeRange.value, 'StartTime')
  proxy.addDateRange(queryParams.value, endTimeRange.value, 'EndTime')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.leaveReqNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.deptName = undefined
  queryParams.value.leaveType = undefined
  startTimeRange.value = []
  endTimeRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function handleFilePreview(url) {
  const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
  proxy.$refs.filePreviewRef.open(url, name)
}

function handleFileDownload(url) {
  downloadFile(url)
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.leaveReqId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    leaveReqNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    deptId: undefined,
    deptName: undefined,
    leaveType: undefined,
    startTime: undefined,
    endTime: undefined,
  leaveDays: undefined,
    reason: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('hrLeaveRequestRef')
  leaveBalanceInfo.value = null
  leaveBalanceWarning.value = ''
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增请假'
}

function handleUpdate(row) {
  reset()
  const leaveReqId = row.leaveReqId || ids.value[0]
  getHrLeaveRequest(leaveReqId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改请假'
    // 如果已有假别和人员，自动查询假期余额
    if (form.value.leaveType && form.value.employeeId) { fetchLeaveBalance() }
  })
}

function handleView(row) {
  const leaveReqId = row.leaveReqId
  getHrLeaveRequest(leaveReqId).then(response => {
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

function submitForm() {
  proxy.$refs['hrLeaveRequestRef'].validate(valid => {
    if (valid) {
      if (computedLeaveDays.value != null) { form.value.leaveDays = computedLeaveDays.value }
      if (form.value.leaveReqId != undefined) {
        updateHrLeaveRequest(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrLeaveRequest(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const leaveReqIds = row.leaveReqId || ids.value
  proxy.$modal.confirm('是否确认删除选中的请假？').then(function() {
    return delHrLeaveRequest(leaveReqIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 审批状态映射 */
function auditStatusLabel(v) {
  if (!v) return '待审批'
  const map = { '0': '待审批', '1': '已通过', '2': '已驳回' }
  return map[v] || '待审批'
}
function auditStatusClass(v) {
  const map = { '0': 'pending', '1': 'pass', '2': 'reject' }
  return map[v] || 'pending'
}

/** 请假审批 */
function handleAudit(row) {
  getHrLeaveRequest(row.leaveReqId).then(response => {
    const d = response.data || {}
auditData.value = {
  leaveReqId: d.leaveReqId,
  leaveReqNo: d.leaveReqNo,
  employeeName: d.employeeName,
  employeeNo: d.employeeNo,
  deptName: d.deptName,
  leaveType: d.leaveType,
  remainingDays: d.remainingDays,
  totalDays: d.totalDays,
  usedDays: d.usedDays,
  startTime: d.startTime,
  endTime: d.endTime,
  leaveDays: d.leaveDays,
  reason: d.reason,
  auditStatus: d.auditStatus,
  auditLogList: d.auditLogList || [],
  auditRemark: ''
}
    auditOpen.value = true
  })
}
function submitAudit(action) {
  if (action === '2' && !auditData.value.auditRemark) {
    proxy.$modal.msgWarning('驳回时请填写审批意见')
    return
  }
  auditSubmitting.value = true
  auditHrLeaveRequest(auditData.value.leaveReqId, action, auditData.value.auditRemark).then(() => {
    proxy.$modal.msgSuccess(action === '1' ? '审批通过' : '已驳回')
    auditOpen.value = false
    getList()
  }).finally(() => {
    auditSubmitting.value = false
  })
}

function handleExport() {
  proxy.download('hr/leaveRequest/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'leaveReqId'), `hrLeaveRequest_${new Date().getTime()}.xlsx`)
}

/** 假别文字映射 */
function leaveTypeLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (leaveTypeOptions.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}
function leaveTypeBadgeClass(v) {
  const map = { '1': 'blue', '2': 'amber', '3': 'green', '4': 'violet', '5': 'red', '6': 'blue', '7': 'gray', '8': 'amber', '9': 'green' }
  return map[v] || 'gray'
}

/** ===== 人员弹框选择 ===== */
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName, employeeNo: emp.employeeNo, deptId: emp.orgId, deptName: emp.orgName };
  Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k]; });
  // 人员变更后，如果已选假别则自动查询假期余额
  if (f.leaveType) { fetchLeaveBalance() }
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
.hr-leaverequest-page {
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
.hr-leaverequest-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-leaverequest-page .filter-card { padding:14px 20px 16px; }
.hr-leaverequest-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-leaverequest-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-leaverequest-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-leaverequest-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-leaverequest-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-leaverequest-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-leaverequest-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-leaverequest-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-leaverequest-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-leaverequest-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-leaverequest-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-leaverequest-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-leaverequest-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-leaverequest-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-leaverequest-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-leaverequest-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-leaverequest-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-leaverequest-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-leaverequest-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-leaverequest-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-leaverequest-page .field .control :deep(.el-select) { width:100%; }
.hr-leaverequest-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-leaverequest-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-leaverequest-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-leaverequest-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-leaverequest-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-leaverequest-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-leaverequest-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-leaverequest-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-leaverequest-page .btn-soft .el-icon { font-size:14px; }
.hr-leaverequest-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-leaverequest-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-leaverequest-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-leaverequest-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-leaverequest-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-leaverequest-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-leaverequest-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-leaverequest-page .table-wrap { overflow-x:auto; }
.hr-leaverequest-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-leaverequest-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-leaverequest-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-leaverequest-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-leaverequest-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-leaverequest-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-leaverequest-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-leaverequest-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-leaverequest-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-leaverequest-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-leaverequest-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-leaverequest-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-leaverequest-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-leaverequest-page .badge.amber .dot { background:var(--amber-500); }
.hr-leaverequest-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-leaverequest-page .badge.blue .dot { background:var(--blue-500); }
.hr-leaverequest-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-leaverequest-page .badge.green .dot { background:var(--green-500); }
.hr-leaverequest-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-leaverequest-page .badge.red .dot { background:var(--red-500); }
.hr-leaverequest-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-leaverequest-page .badge.violet .dot { background:var(--brand-500); }
.hr-leaverequest-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-leaverequest-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-leaverequest-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-leaverequest-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-leaverequest-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-leaverequest-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-leaverequest-page .pagination-container :deep(.el-pagination .btn-prev), .hr-leaverequest-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-leaverequest-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-leaverequest-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-leaverequest-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 查看详情弹窗（rd-*) ===== */
.hr-leaverequest-page .rd-page {
  max-height: 60vh;
  overflow-y: auto;
}
.hr-leaverequest-page .rd-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  animation: rdFadeIn 0.4s ease-out forwards;
}
.hr-leaverequest-page .rd-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  cursor: pointer;
  user-select: none;
}
.hr-leaverequest-page .rd-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.hr-leaverequest-page .rd-card-icon {
  display: flex;
  align-items: center;
  color: #409eff;
}
.hr-leaverequest-page .rd-collapse-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #909399;
  transition: transform 0.2s;
}
.hr-leaverequest-page .rd-collapse-btn.is-collapsed {
  transform: rotate(-90deg);
}
.hr-leaverequest-page .rd-card-body {
  padding: 16px;
}
.hr-leaverequest-page .rd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}
.hr-leaverequest-page .rd-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hr-leaverequest-page .rd-item--full {
  grid-column: 1 / -1;
}
.hr-leaverequest-page .rd-label {
  font-size: 12px;
  color: #909399;
}
.hr-leaverequest-page .rd-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.hr-leaverequest-page .rd-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: #c0c4cc;
}
.hr-leaverequest-page .rd-empty-icon {
  margin-bottom: 8px;
}
.hr-leaverequest-page .rd-empty-text {
  font-size: 13px;
  margin: 0;
}
.hr-leaverequest-page .rd-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hr-leaverequest-page .rd-detail-header-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}
.hr-leaverequest-page .rd-detail-header-main {
  flex: 1;
}
.hr-leaverequest-page .rd-detail-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.hr-leaverequest-page .rd-detail-header-sub {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}
.hr-leaverequest-page .rd-detail-header-divider {
  width: 1px;
  height: 12px;
  background: #dcdfe6;
}
.hr-leaverequest-page .rd-detail-header-no {
  font-size: 13px;
  color: #909399;
}

/* ===== rd-dialog header 覆盖（全局 detail-page.scss 兜底） ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 16px; right: 16px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }

@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* 假期余额显示 */
.text-warning { color: #e6a23c; font-weight: 600; }
.leave-balance-info {
  display: flex;
  align-items: center;
  gap: 10px;
  line-height: 32px;
}
.leave-balance-info .balance-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #67c23a;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
}
.leave-balance-info .balance-tag.is-warning {
  color: #e6a23c;
  background: #fdf6ec;
  border-color: #faecd8;
}
.leave-balance-info .balance-detail {
  font-size: 12px;
  color: #909399;
}
.balance-warning {
  color: #f56c6c;
  font-size: 13px;
  line-height: 32px;
}
.balance-placeholder {
  color: #c0c4cc;
  font-size: 13px;
  line-height: 32px;
}

/* 审批状态标签 */
.audit-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}
.audit-tag.pending { color: #e6a23c; background: #fdf6ec; border: 1px solid #faecd8; }
.audit-tag.pass { color: #67c23a; background: #f0f9eb; border: 1px solid #e1f3d8; }
.audit-tag.reject { color: #f56c6c; background: #fef0f0; border: 1px solid #fde2e2; }

/* ===== 驳回提示 ===== */
.reject-alert { margin-bottom: 16px; }

/* ===== 审核记录时间轴 ===== */
.hr-leaverequest-page .rd-timeline { position: relative; padding-left: 24px; }
.hr-leaverequest-page .rd-timeline::before { content: ''; position: absolute; left: 10px; top: 8px; bottom: 8px; width: 2px; background: linear-gradient(to bottom, #d1d5db, #e5e7eb); border-radius: 1px; }
.hr-leaverequest-page .rd-timeline-item { position: relative; padding-bottom: 10px; }
.hr-leaverequest-page .rd-timeline-item:last-child { padding-bottom: 0; }
.hr-leaverequest-page .rd-timeline-dot { position: absolute; left: -24px; top: 4px; width: 20px; height: 20px; border-radius: 50%; background: #fff; border: 3px solid #9ca3af; display: flex; align-items: center; justify-content: center; z-index: 1; }
.hr-leaverequest-page .rd-timeline-dot--success { border-color: #10b981; box-shadow: 0 0 0 4px #ecfdf5; }
.hr-leaverequest-page .rd-timeline-dot--error { border-color: #ef4444; box-shadow: 0 0 0 4px #fef2f2; }
.hr-leaverequest-page .rd-timeline-content { background: #f9fafb; border-radius: 10px; padding: 10px; border: 1px solid #f3f4f6; }
.hr-leaverequest-page .rd-timeline-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.hr-leaverequest-page .rd-timeline-title { display: flex; align-items: center; gap: 6px; }
.hr-leaverequest-page .rd-timeline-time { font-size: 12px; color: #9ca3af; }
.hr-leaverequest-page .rd-timeline-body { display: flex; gap: 12px; }
.hr-leaverequest-page .rd-timeline-body .rd-item { flex: 1; }
.hr-leaverequest-page .rd-timeline-comment { font-size: 13px; color: #6b7280; padding-top: 4px; border-top: 1px dashed #e5e7eb; margin-top: 6px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-leaverequest-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-leaverequest-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-leaverequest-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
