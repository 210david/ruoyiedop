<template>
  <div class="app-container hr-ot-page">
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
          <label>外出单号</label>
          <div class="control">
            <el-input v-model="queryParams.outingNo" placeholder="请输入外出单号" clearable @keyup.enter="handleQuery">
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
          <label>外出日期</label>
          <div class="control">
            <el-date-picker v-model="outingDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>外出地点</label>
          <div class="control">
            <el-input v-model="queryParams.destination" placeholder="请输入外出地点" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>审批人</label>
          <div class="control">
            <el-input v-model="queryParams.auditBy" placeholder="请输入审批人" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:outing:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:outing:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:outing:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:outing:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_outing_index" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="外出单号" prop="outingNo" key="outingNo" align="center" :width="colWidth('outingNo', 160)" resizable v-if="columns.outingNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.outingNo }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :min-width="colWidth('employeeName', 110)" show-overflow-tooltip resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 110)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="部门" prop="deptName" key="deptName" align="center" :width="colWidth('deptName', 120)" resizable v-if="columns.deptName.visible" />
          <el-table-column label="外出日期" prop="outingDate" key="outingDate" align="center" :width="colWidth('outingDate', 120)" resizable v-if="columns.outingDate.visible" />
          <el-table-column label="外出地点" prop="destination" key="destination" align="center" :width="colWidth('destination', 150)" resizable v-if="columns.destination.visible" />
          <el-table-column label="开始时间" prop="startTime" key="startTime" align="center" :width="colWidth('startTime', 100)" resizable v-if="columns.startTime.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.startTime || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="结束时间" prop="endTime" key="endTime" align="center" :width="colWidth('endTime', 100)" resizable v-if="columns.endTime.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.endTime || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="审批状态" prop="auditStatus" key="auditStatus" align="center" :width="colWidth('auditStatus', 110)" resizable v-if="columns.auditStatus.visible">
            <template #default="scope">
              <span class="badge" :class="auditStatusClass(scope.row.auditStatus)" v-if="scope.row.auditStatus !== undefined && scope.row.auditStatus !== null && scope.row.auditStatus !== ''">
                <span class="dot"></span>{{ auditStatusLabel(scope.row.auditStatus) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:outing:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:outing:edit']">修改</el-button>
                <el-button link type="warning" icon="Finished" @click="handleAudit(scope.row)" v-if="scope.row.auditStatus === '0'" v-hasPermi="['hr:outing:audit']">审批</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:outing:remove']">删除</el-button>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="外出单号" prop="outingNo"><el-input v-model="form.outingNo" placeholder="自动生成" disabled /></el-form-item></el-col>
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
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>外出信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="外出日期" prop="outingDate"><el-date-picker v-model="form.outingDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="外出地点" prop="destination"><el-input v-model="form.destination" placeholder="请输入外出地点" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-time-picker v-model="form.startTime" format="HH:mm" value-format="HH:mm" placeholder="选择时间" style="width: 100%" :disabled-hours="() => []" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-time-picker v-model="form.endTime" format="HH:mm" value-format="HH:mm" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
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
                <el-col :span="24"><el-form-item label="外出事由" prop="reason"><el-input v-model="form.reason" placeholder="请输入外出事由" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">外出详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.outingNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">单号：{{ viewData.outingNo }}</span>
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
              <div class="rd-item"><span class="rd-label">外出单号</span><div class="rd-value">{{ viewData.outingNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>外出信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">外出日期</span><div class="rd-value">{{ viewData.outingDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">外出地点</span><div class="rd-value">{{ viewData.destination || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewData.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ viewData.endTime || '-' }}</div></div>
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
              <div class="rd-item rd-item--full"><span class="rd-label">外出事由</span><div class="rd-value">{{ viewData.reason || '-' }}</div></div>
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
            <div class="rd-detail-header-title">外出审批</div>
            <div class="rd-detail-header-sub" v-if="auditData.outingNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">外出单号：{{ auditData.outingNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">外出单号</span><div class="rd-value">{{ auditData.outingNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ auditData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ auditData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ auditData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>外出信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">外出日期</span><div class="rd-value">{{ auditData.outingDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ auditData.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ auditData.endTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预计返回时间</span><div class="rd-value">{{ auditData.expectedReturnTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">外出地点</span><div class="rd-value">{{ auditData.destination || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">外出事由</span><div class="rd-value">{{ auditData.reason || '-' }}</div></div>
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

<script setup name="HrOuting">
import { listHrOuting, getHrOuting, addHrOuting, updateHrOuting, delHrOuting, auditHrOuting } from '@/api/hr/hrOuting'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'

const { proxy } = getCurrentInstance()
const { hr_audit_status: auditStatusOptions } = proxy.useDict('hr_audit_status')

const dataList = ref([])
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
const outingDateRange = ref([])
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, e0: false, e1: false, e3: false, e2: false })
const employeePickerRef = ref(null)

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_outing_index')

const defaultColumns = {
  outingNo: { label: '外出单号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  deptName: { label: '部门', visible: true },
  outingDate: { label: '外出日期', visible: true },
  destination: { label: '外出地点', visible: true },
  startTime: { label: '开始时间', visible: true },
  endTime: { label: '结束时间', visible: true },
  auditStatus: { label: '审批状态', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_outing_index')
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
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    outingNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    deptName: undefined,
    destination: undefined,
    auditStatus: undefined,
    auditBy: undefined,
  },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    outingDate: [{ required: true, message: '外出日期不能为空', trigger: 'blur' }],
    startTime: [{ required: true, message: '开始时间不能为空', trigger: 'blur' }],
    endTime: [
      { required: true, message: '结束时间不能为空', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value && form.value.startTime) {
            if (value <= form.value.startTime) {
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
    destination: [{ required: true, message: '外出地点不能为空', trigger: 'blur' }],
    reason: [{ required: true, message: '外出事由不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.outingNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.auditStatus) count++
  if (queryParams.value.destination) count++
  if (queryParams.value.auditBy) count++
  if (outingDateRange.value && outingDateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listHrOuting(queryParams.value).then(response => {
    dataList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, outingDateRange.value, 'OutingDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.outingNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.deptName = undefined
  queryParams.value.destination = undefined
  queryParams.value.auditStatus = undefined
  queryParams.value.auditBy = undefined
  outingDateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.outingId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    outingNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    deptId: undefined,
    deptName: undefined,
    outingDate: undefined,
    startTime: '08:30',
    endTime: '17:30',
    destination: undefined,
    reason: undefined,
    transportMode: undefined,
    expectedReturnTime: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('formRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增外出'
}

function handleUpdate(row) {
  reset()
  const id = row.outingId || ids.value[0]
  getHrOuting(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改外出'
  })
}

function handleView(row) {
  getHrOuting(row.outingId).then(response => {
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
  proxy.$refs['formRef'].validate(valid => {
    if (valid) {
      if (form.value.outingId != undefined) {
        updateHrOuting(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrOuting(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const id = row.outingId || ids.value
  proxy.$modal.confirm('是否确认删除选中的外出？').then(function() {
    return delHrOuting(id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/outing/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'outingId'), `hrOuting_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** 审批状态徽章 */
function auditStatusClass(v) {
  const map = { '0': 'amber', '1': 'green', '2': 'red', '3': 'gray' }
  return map[v] || 'gray'
}
function auditStatusLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (auditStatusOptions.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}

/** ===== 人员弹框选择 ===== */
function openEmployeePicker() {
  employeePickerRef.value.open(form.value.employeeId)
}
function onEmployeePickerConfirm(emp) {
  const f = form.value
  f.employeeId = emp.employeeId
  f.employeeName = emp.employeeName
  const auto = { employeeNo: emp.employeeNo, deptId: emp.orgId, deptName: emp.orgName };
  Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k] })
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) {
      form.value.employeeId = null
      form.value.employeeName = ''
    } else {
      openEmployeePicker()
    }
    return
  }
  openEmployeePicker()
}

/** 审批 */
function handleAudit(row) {
  getHrOuting(row.outingId).then(response => {
    const d = response.data || {}
    auditData.value = {
      outingId: d.outingId,
      outingNo: d.outingNo,
      employeeName: d.employeeName,
      employeeNo: d.employeeNo,
      deptName: d.deptName,
      outingDate: d.outingDate,
      startTime: d.startTime,
      endTime: d.endTime,
      expectedReturnTime: d.expectedReturnTime,
      destination: d.destination,
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
  auditHrOuting(auditData.value.outingId, action, auditData.value.auditRemark).then(() => {
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
.hr-ot-page {
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
.hr-ot-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-ot-page .filter-card { padding:14px 20px 16px; }
.hr-ot-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-ot-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-ot-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-ot-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-ot-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-ot-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-ot-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-ot-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-ot-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-ot-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-ot-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-ot-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-ot-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-ot-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-ot-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-ot-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-ot-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-ot-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-ot-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-ot-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-ot-page .field .control :deep(.el-select) { width:100%; }
.hr-ot-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-ot-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-ot-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-ot-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-ot-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-ot-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-ot-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-ot-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-ot-page .btn-soft .el-icon { font-size:14px; }
.hr-ot-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-ot-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-ot-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-ot-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-ot-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-ot-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-ot-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-ot-page .table-wrap { overflow-x:auto; }
.hr-ot-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-ot-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-ot-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-ot-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-ot-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-ot-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-ot-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-ot-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-ot-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-ot-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-ot-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-ot-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-ot-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-ot-page .badge.amber .dot { background:var(--amber-500); }
.hr-ot-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-ot-page .badge.blue .dot { background:var(--blue-500); }
.hr-ot-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-ot-page .badge.green .dot { background:var(--green-500); }
.hr-ot-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-ot-page .badge.red .dot { background:var(--red-500); }
.hr-ot-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-ot-page .badge.violet .dot { background:var(--brand-500); }
.hr-ot-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-ot-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-ot-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-ot-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-ot-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-ot-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-ot-page .pagination-container :deep(.el-pagination .btn-prev), .hr-ot-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-ot-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-ot-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-ot-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 查看详情弹窗（rd-*) ===== */
.hr-ot-page .rd-page { max-height: 60vh; overflow-y: auto; }
.hr-ot-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; animation: rdFadeIn 0.4s ease-out forwards; }
.hr-ot-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-ot-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-ot-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-ot-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-ot-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-ot-page .rd-card-body { padding: 16px; }
.hr-ot-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-ot-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-ot-page .rd-item--full { grid-column: 1 / -1; }
.hr-ot-page .rd-label { font-size: 12px; color: #909399; }
.hr-ot-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-ot-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-ot-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-ot-page .rd-detail-header-main { flex: 1; }
.hr-ot-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-ot-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-ot-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-ot-page .rd-detail-header-no { font-size: 13px; color: #909399; }

/* ===== rd-dialog header 覆盖 ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 16px; right: 16px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }

@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 操作列按钮对齐 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-ot-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-ot-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-ot-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== 驳回提示 ===== */
.reject-alert { margin-bottom: 16px; }

/* ===== 审核记录时间轴 ===== */
.hr-outing-page .rd-timeline { position: relative; padding-left: 24px; }
.hr-outing-page .rd-timeline::before { content: ''; position: absolute; left: 10px; top: 8px; bottom: 8px; width: 2px; background: linear-gradient(to bottom, #d1d5db, #e5e7eb); border-radius: 1px; }
.hr-outing-page .rd-timeline-item { position: relative; padding-bottom: 10px; }
.hr-outing-page .rd-timeline-item:last-child { padding-bottom: 0; }
.hr-outing-page .rd-timeline-dot { position: absolute; left: -24px; top: 4px; width: 20px; height: 20px; border-radius: 50%; background: #fff; border: 3px solid #9ca3af; display: flex; align-items: center; justify-content: center; z-index: 1; }
.hr-outing-page .rd-timeline-dot--success { border-color: #10b981; box-shadow: 0 0 0 4px #ecfdf5; }
.hr-outing-page .rd-timeline-dot--error { border-color: #ef4444; box-shadow: 0 0 0 4px #fef2f2; }
.hr-outing-page .rd-timeline-content { background: #f9fafb; border-radius: 10px; padding: 10px; border: 1px solid #f3f4f6; }
.hr-outing-page .rd-timeline-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.hr-outing-page .rd-timeline-title { display: flex; align-items: center; gap: 6px; }
.hr-outing-page .rd-timeline-time { font-size: 12px; color: #9ca3af; }
.hr-outing-page .rd-timeline-body { display: flex; gap: 12px; }
.hr-outing-page .rd-timeline-body .rd-item { flex: 1; }
.hr-outing-page .rd-timeline-comment { font-size: 13px; color: #6b7280; padding-top: 4px; border-top: 1px dashed #e5e7eb; margin-top: 6px; }

</style>