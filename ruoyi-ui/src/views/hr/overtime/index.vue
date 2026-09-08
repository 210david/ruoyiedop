<template>
  <div class="app-container hr-overtime-page">
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
          <label>加班单号</label>
          <div class="control">
            <el-input v-model="queryParams.overtimeNo" placeholder="请输入加班单号" clearable @keyup.enter="handleQuery">
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
          <label>审批状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in auditStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>加班类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.overtimeType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in overtimeTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>加班日期</label>
          <div class="control">
            <el-date-picker v-model="overtimeDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:overtime:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:overtime:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:overtime:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:overtime:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_overtime_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrOvertimeList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="加班单号" prop="overtimeNo" key="overtimeNo" align="center" :width="colWidth('overtimeNo', 180)" resizable v-if="columns.overtimeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.overtimeNo }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 110)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="部门" prop="deptName" key="deptName" align="center" :width="colWidth('deptName', 120)" resizable v-if="columns.deptName.visible" />
          <el-table-column label="加班日期" prop="overtimeDate" key="overtimeDate" align="center" :width="colWidth('overtimeDate', 120)" resizable v-if="columns.overtimeDate.visible" />
          <el-table-column label="加班类型" prop="overtimeType" key="overtimeType" align="center" :width="colWidth('overtimeType', 120)" resizable v-if="columns.overtimeType.visible">
            <template #default="scope">
              <span class="badge" :class="overtimeTypeClass(scope.row.overtimeType)"><span class="dot"></span>{{ overtimeTypeLabel(scope.row.overtimeType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="开始时间" prop="startTime" key="startTime" align="center" :width="colWidth('startTime', 130)" resizable v-if="columns.startTime.visible" />
          <el-table-column label="结束时间" prop="endTime" key="endTime" align="center" :width="colWidth('endTime', 130)" resizable v-if="columns.endTime.visible" />
          <el-table-column label="审批状态" prop="auditStatus" key="auditStatus" align="center" :width="colWidth('auditStatus', 110)" resizable v-if="columns.auditStatus.visible">
            <template #default="scope">
              <span class="badge" :class="auditStatusClass(scope.row.auditStatus)"><span class="dot"></span>{{ auditStatusLabel(scope.row.auditStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:overtime:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:overtime:edit']">修改</el-button>
                <el-button link type="warning" icon="Finished" @click="handleAudit(scope.row)" v-if="scope.row.auditStatus === '0'" v-hasPermi="['hr:overtime:audit']">审批</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:overtime:remove']">删除</el-button>
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
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrOvertimeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
          <el-form-item label="加班单号" prop="overtimeNo">
            <el-input v-model="form.overtimeNo" placeholder="自动生成" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名" prop="employeeId">
            <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
              <template #append>
                <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工号" prop="employeeNo">
            <el-input v-model="form.employeeNo" placeholder="请输入工号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="部门" prop="deptName">
            <el-input v-model="form.deptName" placeholder="选择人员后自动带出" readonly />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>加班信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
          <el-form-item label="加班类型" prop="overtimeType">
            <el-select v-model="form.overtimeType" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in overtimeTypeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="加班日期" prop="overtimeDate">
            <el-date-picker v-model="form.overtimeDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-time-picker v-model="form.startTime" value-format="HH:mm:ss" placeholder="选择时间" format="HH:mm" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-time-picker v-model="form.endTime" value-format="HH:mm:ss" placeholder="选择时间" format="HH:mm" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="加班小时" prop="overtimeHours">
            <el-input :model-value="computedOvertimeHours != null ? computedOvertimeHours + ' 小时' : ''" disabled placeholder="根据起止时间自动计算" />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e3">
              <el-row :gutter="20">
                <el-col :span="24">
          <el-form-item label="加班原因" prop="reason">
            <el-input v-model="form.reason" placeholder="请输入加班原因" />
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
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">加班详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.overtimeNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">单号：{{ viewData.overtimeNo }}</span>
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
              <div class="rd-item"><span class="rd-label">加班单号</span><div class="rd-value">{{ viewData.overtimeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>加班信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">加班类型</span><div class="rd-value"><dict-tag :options="overtimeTypeOptions" :value="viewData.overtimeType"/></div></div>
              <div class="rd-item"><span class="rd-label">加班日期</span><div class="rd-value">{{ viewData.overtimeDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewData.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ viewData.endTime || '-' }}</div></div>
<div class="rd-item"><span class="rd-label">加班小时</span><div class="rd-value">{{ viewData.overtimeHours != null ? viewData.overtimeHours : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">加班原因</span><div class="rd-value">{{ viewData.reason || '-' }}</div></div>
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

    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="auditOpen" width="912px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">加班审批</div>
            <div class="rd-detail-header-sub" v-if="auditData.overtimeNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">加班单号：{{ auditData.overtimeNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">加班单号</span><div class="rd-value">{{ auditData.overtimeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ auditData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ auditData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ auditData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>加班信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">加班类型</span><div class="rd-value"><dict-tag :options="overtimeTypeOptions" :value="auditData.overtimeType"/></div></div>
              <div class="rd-item"><span class="rd-label">加班日期</span><div class="rd-value">{{ auditData.overtimeDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ auditData.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ auditData.endTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">加班小时</span><div class="rd-value">{{ auditData.overtimeHours != null ? auditData.overtimeHours : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">加班原因</span><div class="rd-value">{{ auditData.reason || '-' }}</div></div>
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

    <!-- 人员选择弹窗 -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
  </div>
</template>

<script setup name="HrOvertime">
import { listHrOvertime, getHrOvertime, addHrOvertime, updateHrOvertime, delHrOvertime, auditHrOvertime } from '@/api/hr/hrOvertime'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker';

const { proxy } = getCurrentInstance()

/** 字典：加班类型、审批状态（加班小时按起止时间自动计算，无需补偿方式） */
const { hr_overtime_type: overtimeTypeOptions, hr_audit_status: auditStatusOptions } = proxy.useDict('hr_overtime_type', 'hr_audit_status')

const hrOvertimeList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const auditOpen = ref(false)
const auditSubmitting = ref(false)
const auditData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showAdvanced = ref(false)
const overtimeDateRange = ref([])
const startTimeRange = ref([])
const endTimeRange = ref([])
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, e0: false, e1: false, e2: false, e3: false })

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_overtime_index')

/** 列显隐配置 */
const defaultColumns = {
  overtimeNo: { label: '加班单号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  deptName: { label: '部门', visible: true },
  overtimeDate: { label: '加班日期', visible: true },
  overtimeType: { label: '加班类型', visible: true },
  startTime: { label: '开始时间', visible: true },
  endTime: { label: '结束时间', visible: true },
  auditStatus: { label: '审批状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_overtime_columns')
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

/** 审批状态徽章映射（兼容字典编码与历史中文值） */
function auditStatusClass(v) {
  const map = { '0': 'amber', '待审批': 'amber', '待审': 'amber', '1': 'green', '审批中': 'blue', '通过': 'green', '已通过': 'green', '2': 'red', '驳回': 'red', '已驳回': 'red', '3': 'red', '已作废': 'gray', '4': 'gray' }
  return map[v] || 'gray'
}
function auditStatusLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const map = { '0': '待审', '1': '通过', '2': '驳回', '待审批': '待审批', '审批中': '审批中', '已通过': '已通过', '已驳回': '已驳回', '已作废': '已作废' }
  return map[v] || String(v)
}

/** 加班类型徽章映射（兼容字典编码与历史中文值） */
function overtimeTypeClass(v) {
  const map = { '0': 'blue', '工作日': 'blue', '1': 'amber', '休息日': 'amber', '2': 'violet', '法定节假日': 'violet' }
  return map[v] || 'gray'
}
function overtimeTypeLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const map = { '0': '工作日', '1': '休息日', '2': '法定节假日' }
  return map[v] || String(v)
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.overtimeNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.deptName) count++
  if (queryParams.value.auditStatus) count++
  if (queryParams.value.overtimeType) count++
  if (overtimeDateRange.value && overtimeDateRange.value.length === 2) count++
  if (startTimeRange.value && startTimeRange.value.length === 2) count++
  if (endTimeRange.value && endTimeRange.value.length === 2) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, overtimeNo: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined, deptName: undefined, overtimeType: undefined, auditStatus: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    overtimeDate: [{ required: true, message: '加班日期不能为空', trigger: 'blur' }],
    startTime: [{ required: true, message: '开始时间不能为空', trigger: 'blur' }],
    endTime: [
      { required: true, message: '结束时间不能为空', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value && form.value.startTime) {
            const s = new Date('2000-01-01 ' + form.value.startTime)
            const e = new Date('2000-01-01 ' + value)
            if (!isNaN(s.getTime()) && !isNaN(e.getTime()) && e <= s) {
              callback(new Error('结束时间不能早于开始时间'))
            } else {
              callback()
            }
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    overtimeHours: [],
    reason: [{ required: true, message: '加班原因不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 根据开始/结束时间自动计算加班小时（实时显示，纯时间+加班日期） */
const computedOvertimeHours = computed(() => {
  const f = form.value
  if (!f.startTime || !f.endTime) return null
  // startTime/endTime 为 HH:mm:ss 格式，需结合 overtimeDate 拼成完整 datetime
  const dateStr = f.overtimeDate || '1970-01-01'
  const s = new Date((dateStr + ' ' + f.startTime).replace(/-/g, '/')).getTime()
  const e = new Date((dateStr + ' ' + f.endTime).replace(/-/g, '/')).getTime()
  if (isNaN(s) || isNaN(e)) return null
  // 支持跨天：结束时间小于开始时间时，结束时间加一天
  let diff = e - s
  if (diff <= 0) diff += 24 * 3600000
  // 最小单位0.5小时，向上取整
  return Math.ceil(diff / 3600000 * 2) / 2
})

/** ===== 人员弹框选择 ===== */
const employeePickerRef = ref(null)

function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：工号、部门等字段
  f.employeeNo = emp.employeeNo;
  f.deptId = emp.orgId;
  f.deptName = emp.orgName;
  // 其他档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, positionId: emp.positionId, positionName: emp.positionName };
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

function getList() {
  loading.value = true
  listHrOvertime(queryParams.value).then(response => {
    hrOvertimeList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  queryParams.value.params = {}
  proxy.addDateRange(queryParams.value, overtimeDateRange.value, 'OvertimeDate')
  proxy.addDateRange(queryParams.value, startTimeRange.value, 'StartTime')
  proxy.addDateRange(queryParams.value, endTimeRange.value, 'EndTime')
  getList()
}

function resetQuery() {
  queryParams.value.overtimeNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.deptName = undefined
  queryParams.value.auditStatus = undefined
  queryParams.value.overtimeType = undefined
  overtimeDateRange.value = []
  startTimeRange.value = []
  endTimeRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.overtimeId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function reset() {
  form.value = {
    overtimeNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    deptId: undefined,
    deptName: undefined,
    overtimeDate: undefined,
    overtimeType: undefined,
    startTime: undefined,
    endTime: undefined,
    overtimeHours: undefined,
    reason: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('hrOvertimeRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增加班'
}

function handleUpdate(row) {
  reset()
  const overtimeId = row.overtimeId || ids.value[0]
  getHrOvertime(overtimeId).then(response => {
    const d = response.data
    // 从完整 datetime 中提取时间部分用于编辑表单
    if (d.startTime && d.startTime.length > 8) d.startTime = d.startTime.substring(11)
    if (d.endTime && d.endTime.length > 8) d.endTime = d.endTime.substring(11)
    form.value = d
    open.value = true
    title.value = '修改加班'
  })
}

function handleView(row) {
  const overtimeId = row.overtimeId
  getHrOvertime(overtimeId).then(response => {
    viewData.value = response.data || {}
    viewOpen.value = true
  }).catch(() => {
    proxy.$modal.msgError('获取详情失败')
  })
}

/** 详情弹窗：获取审核日志列表（安全访问，防止 viewData 为空时报错） */
function viewAuditLogs() {
  return viewData.value && viewData.value.auditLogList ? viewData.value.auditLogList : []
}

function submitForm() {
  proxy.$refs['hrOvertimeRef'].validate(valid => {
    if (valid) {
      computeOvertimeHours(form.value)
      if (form.value.overtimeId != undefined) {
        updateHrOvertime(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrOvertime(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 根据开始/结束时间自动计算加班小时（纯时间+加班日期，跨天支持） */
function computeOvertimeHours(f) {
  if (!f.startTime || !f.endTime) return
  const dateStr = f.overtimeDate || '1970-01-01'
  const s = new Date((dateStr + ' ' + f.startTime).replace(/-/g, '/')).getTime()
  const e = new Date((dateStr + ' ' + f.endTime).replace(/-/g, '/')).getTime()
  if (isNaN(s) || isNaN(e)) return
let diff = e - s
if (diff <= 0) diff += 24 * 3600000
// 最小单位0.5小时，向上取整
f.overtimeHours = Math.ceil(diff / 3600000 * 2) / 2
  // 提交给后端时拼接完整 datetime
  if (f.overtimeDate) {
    f.startTime = f.overtimeDate + ' ' + f.startTime
    f.endTime = f.overtimeDate + ' ' + f.endTime
  }
}

function handleDelete(row) {
  const overtimeIds = row.overtimeId || ids.value
  proxy.$modal.confirm('是否确认删除选中的加班？').then(function() {
    return delHrOvertime(overtimeIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/overtime/export', { ...queryParams.value }, `hrOvertime_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** 审批 */
function handleAudit(row) {
  getHrOvertime(row.overtimeId).then(response => {
    const d = response.data || {}
    auditData.value = {
      overtimeId: d.overtimeId,
      overtimeNo: d.overtimeNo,
      employeeName: d.employeeName,
      employeeNo: d.employeeNo,
      deptName: d.deptName,
      overtimeType: d.overtimeType,
      overtimeDate: d.overtimeDate,
      startTime: d.startTime,
      endTime: d.endTime,
      overtimeHours: d.overtimeHours,
      reason: d.reason,
      auditStatus: d.auditStatus,
      auditRemark: '',
  auditLogList: d.auditLogList || []
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
  auditHrOvertime(auditData.value.overtimeId, action, auditData.value.auditRemark).then(() => {
    proxy.$modal.msgSuccess(action === '1' ? '审批通过' : '已驳回')
    auditOpen.value = false
    getList()
  }).finally(() => {
    auditSubmitting.value = false
  })
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-overtime-page {
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
.hr-overtime-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-overtime-page .filter-card { padding:14px 20px 16px; }
.hr-overtime-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-overtime-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-overtime-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-overtime-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-overtime-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-overtime-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-overtime-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-overtime-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-overtime-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-overtime-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-overtime-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-overtime-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-overtime-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-overtime-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-overtime-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-overtime-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-overtime-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-overtime-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-overtime-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-overtime-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-overtime-page .field .control :deep(.el-select) { width:100%; }
.hr-overtime-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-overtime-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-overtime-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-overtime-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-overtime-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-overtime-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-overtime-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-overtime-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-overtime-page .btn-soft .el-icon { font-size:14px; }
.hr-overtime-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-overtime-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-overtime-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-overtime-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-overtime-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-overtime-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-overtime-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-overtime-page .table-wrap { overflow-x:auto; }
.hr-overtime-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-overtime-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-overtime-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-overtime-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-overtime-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-overtime-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-overtime-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-overtime-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-overtime-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-overtime-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-overtime-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-overtime-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-overtime-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-overtime-page .badge.amber .dot { background:var(--amber-500); }
.hr-overtime-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-overtime-page .badge.blue .dot { background:var(--blue-500); }
.hr-overtime-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-overtime-page .badge.green .dot { background:var(--green-500); }
.hr-overtime-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-overtime-page .badge.red .dot { background:var(--red-500); }
.hr-overtime-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-overtime-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-overtime-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-overtime-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-overtime-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-overtime-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-overtime-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-overtime-page .pagination-container :deep(.el-pagination .btn-prev), .hr-overtime-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-overtime-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-overtime-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-overtime-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式（遵循 detail-page-style-guide 规范） ===== */
.hr-overtime-page .rd-page { max-height: 62vh; overflow-y: auto; }
.hr-overtime-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-overtime-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-overtime-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-overtime-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-overtime-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-overtime-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-overtime-page .rd-card-body { padding: 16px; }
.hr-overtime-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-overtime-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-overtime-page .rd-item--full { grid-column: 1 / -1; }
.hr-overtime-page .rd-label { font-size: 12px; color: #909399; }
.hr-overtime-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-overtime-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-overtime-page .rd-empty-icon { margin-bottom: 8px; }
.hr-overtime-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-overtime-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-overtime-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-overtime-page .rd-detail-header-main { flex: 1; }
.hr-overtime-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-overtime-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-overtime-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-overtime-page .rd-detail-header-no { font-size: 13px; color: #909399; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.hr-overtime-page .rd-card { animation: rdFadeIn 0.4s ease-out forwards; }
.hr-overtime-page .rd-card:nth-child(2) { animation-delay: 0.06s; }
.hr-overtime-page .rd-card:nth-child(3) { animation-delay: 0.12s; }
.hr-overtime-page .rd-card:nth-child(4) { animation-delay: 0.18s; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-overtime-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-overtime-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-overtime-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== 驳回提示 ===== */
.reject-alert { margin-bottom: 16px; }

/* ===== 审核记录时间轴 ===== */
.hr-overtime-page .rd-timeline { position: relative; padding-left: 24px; }
.hr-overtime-page .rd-timeline::before { content: ''; position: absolute; left: 10px; top: 8px; bottom: 8px; width: 2px; background: linear-gradient(to bottom, #d1d5db, #e5e7eb); border-radius: 1px; }
.hr-overtime-page .rd-timeline-item { position: relative; padding-bottom: 10px; }
.hr-overtime-page .rd-timeline-item:last-child { padding-bottom: 0; }
.hr-overtime-page .rd-timeline-dot { position: absolute; left: -24px; top: 4px; width: 20px; height: 20px; border-radius: 50%; background: #fff; border: 3px solid #9ca3af; display: flex; align-items: center; justify-content: center; z-index: 1; }
.hr-overtime-page .rd-timeline-dot--success { border-color: #10b981; box-shadow: 0 0 0 4px #ecfdf5; }
.hr-overtime-page .rd-timeline-dot--error { border-color: #ef4444; box-shadow: 0 0 0 4px #fef2f2; }
.hr-overtime-page .rd-timeline-content { background: #f9fafb; border-radius: 10px; padding: 10px; border: 1px solid #f3f4f6; }
.hr-overtime-page .rd-timeline-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.hr-overtime-page .rd-timeline-title { display: flex; align-items: center; gap: 6px; }
.hr-overtime-page .rd-timeline-time { font-size: 12px; color: #9ca3af; }
.hr-overtime-page .rd-timeline-body { display: flex; gap: 12px; }
.hr-overtime-page .rd-timeline-body .rd-item { flex: 1; }
.hr-overtime-page .rd-timeline-comment { font-size: 13px; color: #6b7280; padding-top: 4px; border-top: 1px dashed #e5e7eb; margin-top: 6px; }

</style>
