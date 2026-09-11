<template>
  <div class="app-container hr-monthly-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>年月</label>
          <div class="control">
            <el-date-picker v-model="queryParams.yearMonth" type="month" value-format="YYYY-MM" placeholder="选择年月" :clearable="false" @change="handleQuery" style="width: 100%" />
          </div>
        </div>
        <div class="field">
          <label>部门</label>
          <div class="control">
            <el-input :model-value="queryParams.deptName" readonly placeholder="请选择部门" @click="deptFilterRef && deptFilterRef.open()">
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
              <template #append>
                <el-button v-if="queryParams.deptId" icon="CircleClose" @click="clearDeptFilter" />
                <el-button v-else icon="Search" @click="deptFilterRef && deptFilterRef.open()" />
              </template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup="e => { if (e.key === 'Enter') handleQuery() }" @change="handleQuery" @clear="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup="e => { if (e.key === 'Enter') handleQuery() }" @change="handleQuery" @clear="handleQuery">
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
          <el-button type="primary" @click="handleGenerate" v-hasPermi="['hr:monthly:generate']">
            <el-icon><MagicStick /></el-icon> 生成{{ currentMonthLabel }}月报
          </el-button>
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:monthly:add']">新增</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:monthly:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:monthly:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_monthly_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrAttendanceMonthlyList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="年月" prop="yearMonth" key="yearMonth" align="center" :width="colWidth('yearMonth', 120)" resizable v-if="columns.yearMonth.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.yearMonth }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :min-width="colWidth('employeeName', 110)" show-overflow-tooltip resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="部门" prop="deptName" key="deptName" align="center" :width="colWidth('deptName', 140)" resizable show-overflow-tooltip v-if="columns.deptName.visible">
            <template #default="scope">{{ scope.row.deptName || scope.row.orgName || '-' }}</template>
          </el-table-column>
                    <el-table-column label="应出勤天数" prop="requiredDays" key="requiredDays" align="center" :width="colWidth('requiredDays', 120)" resizable v-if="columns.requiredDays.visible">
            <template #default="scope"><span class="col-mono" style="font-weight:600">{{ scope.row.requiredDays ?? '-' }}</span></template>
          </el-table-column>
          <el-table-column label="实际出勤天数" prop="attendDays" key="attendDays" align="center" :width="colWidth('attendDays', 110)" resizable v-if="columns.attendDays.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="showDetail(scope.row, 'attend')">{{ scope.row.attendDays ?? 0 }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="旷工天数" prop="absentDays" key="absentDays" align="center" :width="colWidth('absentDays', 110)" resizable v-if="columns.absentDays.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="showDetail(scope.row, 'absent')">{{ scope.row.absentDays ?? 0 }}</el-link>
            </template>
          </el-table-column>

          <el-table-column label="请假天数" prop="leaveDays" key="leaveDays" align="center" :width="colWidth('leaveDays', 110)" resizable v-if="columns.leaveDays.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="showDetail(scope.row, 'leave')">{{ scope.row.leaveDays ?? 0 }}</el-link>
            </template>
          </el-table-column>

         <el-table-column label="迟到次数" prop="lateCount" key="lateCount" align="center" :width="colWidth('lateCount', 110)" resizable v-if="columns.lateCount.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="showDetail(scope.row, 'late')">{{ scope.row.lateCount ?? 0 }}</el-link>
            </template>
          </el-table-column>

         <el-table-column label="早退次数" prop="earlyCount" key="earlyCount" align="center" :width="colWidth('earlyCount', 110)" resizable v-if="columns.earlyCount.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="showDetail(scope.row, 'early')">{{ scope.row.earlyCount ?? 0 }}</el-link>
            </template>
          </el-table-column>

          <el-table-column label="外出天数" prop="outingDays" key="outingDays" align="center" :width="colWidth('outingDays', 110)" resizable v-if="columns.outingDays.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="showDetail(scope.row, 'outing')">{{ scope.row.outingDays ?? 0 }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="出差天数" prop="tripDays" key="tripDays" align="center" :width="colWidth('tripDays', 110)" resizable v-if="columns.tripDays.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="showDetail(scope.row, 'trip')">{{ scope.row.tripDays ?? 0 }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="加班小时" prop="overtimeHours" key="overtimeHours" align="center" :width="colWidth('overtimeHours', 110)" resizable v-if="columns.overtimeHours.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="showDetail(scope.row, 'overtime')">{{ scope.row.overtimeHours ?? 0 }}</el-link>
            </template>
          </el-table-column>
            <el-table-column label="操作" width="150" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:monthly:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:monthly:edit']">修正</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:monthly:remove']">删除</el-button>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrAttendanceMonthlyRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="年月" prop="yearMonth"><el-date-picker v-model="form.yearMonth" type="month" value-format="YYYY-MM" placeholder="选择年月" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入工号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="部门" prop="orgId">
                  <el-input :model-value="form.orgName" readonly placeholder="请选择部门">
                    <template #append>
                      <el-button v-if="form.orgId" icon="CircleClose" @click="onOrgFieldClick" />
                      <el-button v-else icon="Search" @click="onOrgFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>出勤统计</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="应出勤天数" prop="requiredDays"><el-input-number v-model="form.requiredDays" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="实际出勤" prop="attendDays"><el-input-number v-model="form.attendDays" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="迟到次数" prop="lateCount"><el-input-number v-model="form.lateCount" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="早退次数" prop="earlyCount"><el-input-number v-model="form.earlyCount" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="请假天数" prop="leaveDays"><el-input-number v-model="form.leaveDays" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></span>加班与缺勤</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="加班小时" prop="overtimeHours"><el-input-number v-model="form.overtimeHours" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="旷工天数" prop="absentDays"><el-input-number v-model="form.absentDays" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="外出天数" prop="outingDays"><el-input-number v-model="form.outingDays" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出差天数" prop="tripDays"><el-input-number v-model="form.tripDays" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">考勤月报详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.employeeNo || viewData.yearMonth">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">年月：{{ viewData.yearMonth || '-' }}</span>
              <span class="rd-detail-header-no">工号：{{ viewData.employeeNo || '-' }}</span>
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
              <div class="rd-item"><span class="rd-label">年月</span><div class="rd-value">{{ viewData.yearMonth || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>出勤统计</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">应出勤天数</span><div class="rd-value">{{ viewData.requiredDays != null ? viewData.requiredDays : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">实际出勤天数</span><div class="rd-value">{{ viewData.attendDays != null ? viewData.attendDays : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">迟到次数</span><div class="rd-value">{{ viewData.lateCount != null ? viewData.lateCount : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">早退次数</span><div class="rd-value">{{ viewData.earlyCount != null ? viewData.earlyCount : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">请假天数</span><div class="rd-value">{{ viewData.leaveDays != null ? viewData.leaveDays : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></span>加班与缺勤</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">加班小时</span><div class="rd-value">{{ viewData.overtimeHours != null ? viewData.overtimeHours : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">旷工天数</span><div class="rd-value">{{ viewData.absentDays != null ? viewData.absentDays : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">外出天数</span><div class="rd-value">{{ viewData.outingDays != null ? viewData.outingDays : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">出差天数</span><div class="rd-value">{{ viewData.tripDays != null ? viewData.tripDays : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('c4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c4" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 人员选择弹窗 -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
    <!-- 部门选择弹窗 -->
    <dept-picker ref="orgPickerRef" title="选择部门" @confirm="onOrgPickerConfirm" />
    <dept-picker ref="deptFilterRef" title="选择筛选部门" @confirm="onDeptFilterConfirm" />
  </div>

    <!-- ===== 考勤下钻明细弹框（按字段只显示对应数据） ===== -->
    <el-dialog v-model="detailOpen" :title="detailTitle" width="1150px" append-to-body draggable class="rd-dialog">
      <div class="rd-page" style="padding-top:0">
        <section class="rd-card" style="margin-bottom:0">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>
              {{ detailRow.employeeName }} · {{ detailRow.yearMonth }} · {{ detailTypeLabel }}
            </div>
          </div>
          <div class="rd-card-body" style="padding:0">
            <!-- 实际出勤 / 旷工 / 迟到 / 早退：日打卡明细（按类型过滤） -->
            <el-table v-if="['attend','absent','late','early'].includes(detailType)" :data="pagedRecords" border stripe size="default" style="width:100%" @sort-change="onDetailSortChange" :header-cell-style="{background:'#f8fafc',color:'#334155',fontWeight:600}">
              <el-table-column label="序号" type="index" :index="(i) => (detailPage.pageNum - 1) * detailPage.pageSize + i + 1" width="60" align="center" />
              <el-table-column label="日期" prop="recordDate" width="120" align="center" sortable :default-sort="{ prop: 'recordDate', order: 'ascending' }" />
              <el-table-column label="班次" prop="shiftName" min-width="120" align="center" />
              <el-table-column label="上班打卡" width="140" align="center">
                <template #default="scope">
                  {{ formatClockTime(scope.row.clockIn) }}
                  <el-tag v-if="String(scope.row.clockInSource) === '1'" type="warning" size="small" effect="plain" style="margin-left:2px">补</el-tag>
                  <el-tag v-else-if="String(scope.row.clockInSource) === '3'" type="success" size="small" effect="plain" style="margin-left:2px">外出</el-tag>
                  <el-tag v-else-if="String(scope.row.clockInSource) === '4'" type="primary" size="small" effect="plain" style="margin-left:2px">出差</el-tag>
                  <el-tag v-else-if="String(scope.row.clockInSource) === '2'" type="info" size="small" effect="plain" style="margin-left:2px">手动</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="下班打卡" width="140" align="center">
                <template #default="scope">
                  {{ formatClockTime(scope.row.clockOut) }}
                  <el-tag v-if="String(scope.row.clockOutSource) === '1'" type="warning" size="small" effect="plain" style="margin-left:2px">补</el-tag>
                  <el-tag v-else-if="String(scope.row.clockOutSource) === '3'" type="success" size="small" effect="plain" style="margin-left:2px">外出</el-tag>
                  <el-tag v-else-if="String(scope.row.clockOutSource) === '4'" type="primary" size="small" effect="plain" style="margin-left:2px">出差</el-tag>
                  <el-tag v-else-if="String(scope.row.clockOutSource) === '2'" type="info" size="small" effect="plain" style="margin-left:2px">手动</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" prop="attendanceStatus" width="90" align="center">
                <template #default="scope"><el-tag :type="attendanceTagType(scope.row.attendanceStatus)" size="small">{{ attendanceStatusText(scope.row.attendanceStatus) }}</el-tag></template>
              </el-table-column>
              <el-table-column v-if="detailType === 'absent'" label="旷工说明" prop="remark" min-width="220" show-overflow-tooltip />
              <el-table-column label="迟到(分)" prop="lateMinutes" width="90" align="center" />
              <el-table-column label="早退(分)" prop="earlyMinutes" width="90" align="center" />
              <el-table-column label="加班(h)" prop="overtimeHours" width="90" align="center" />
              <template #empty><el-empty description="当月无此类记录" :image-size="80" /></template>
            </el-table>
            <!-- 请假：请假单列表 -->
            <el-table v-else-if="detailType==='leave'" :data="pagedLeaveList" border stripe size="default" style="width:100%" @sort-change="onDetailSortChange" :header-cell-style="{background:'#f8fafc',color:'#334155',fontWeight:600}">
              <el-table-column label="序号" type="index" :index="(i) => (detailPage.pageNum - 1) * detailPage.pageSize + i + 1" width="60" align="center" />
              <el-table-column label="请假单号" prop="leaveReqNo" min-width="150" align="center" />
              <el-table-column label="假别" prop="leaveType" width="100" align="center">
                <template #default="scope"><dict-tag :options="hrLeaveTypeOptions" :value="scope.row.leaveType" /></template>
              </el-table-column>
              <el-table-column label="开始时间" prop="startTime" width="160" align="center" sortable />
              <el-table-column label="结束时间" prop="endTime" width="160" align="center" />
              <el-table-column label="请假天数" prop="leaveDays" width="100" align="center" />
              <el-table-column label="请假原因" prop="reason" min-width="180" show-overflow-tooltip />
              <template #empty><el-empty description="当月无请假记录" :image-size="80" /></template>
            </el-table>
            <!-- 外出：外出单列表 -->
            <el-table v-else-if="detailType==='outing'" :data="detailData.outings || []" border stripe size="default" style="width:100%" :header-cell-style="{background:'#f8fafc',color:'#334155',fontWeight:600}">
              <el-table-column label="序号" type="index" width="60" align="center" />
              <el-table-column label="外出单号" prop="outingNo" min-width="150" align="center" />
              <el-table-column label="外出日期" prop="outingDate" width="120" align="center" />
              <el-table-column label="开始时间" prop="startTime" width="150" align="center" />
              <el-table-column label="结束时间" prop="endTime" width="150" align="center" />
              <el-table-column label="外出地点" prop="destination" min-width="140" show-overflow-tooltip />
              <el-table-column label="外出事由" prop="reason" min-width="160" show-overflow-tooltip />
              <template #empty><el-empty description="当月无外出记录" :image-size="80" /></template>
            </el-table>
            <!-- 出差：出差单列表 -->
            <el-table v-else-if="detailType==='trip'" :data="detailData.trips || []" border stripe size="default" style="width:100%" :header-cell-style="{background:'#f8fafc',color:'#334155',fontWeight:600}">
              <el-table-column label="序号" type="index" width="60" align="center" />
              <el-table-column label="出差单号" prop="tripNo" min-width="150" align="center" />
              <el-table-column label="出发日期" prop="departureDate" width="120" align="center" />
              <el-table-column label="返回日期" prop="actualReturnDate" width="120" align="center">
                <template #default="scope">{{ scope.row.actualReturnDate || scope.row.expectedReturnDate || '-' }}</template>
              </el-table-column>
              <el-table-column label="出差天数" prop="tripDays" width="90" align="center" />
              <el-table-column label="出差事由" prop="reason" min-width="160" show-overflow-tooltip />
              <template #empty><el-empty description="当月无出差记录" :image-size="80" /></template>
            </el-table>
            <!-- 加班：优先展示加班审批单，也展示打卡记录中的加班 -->
            <div v-else-if="detailType==='overtime'">
              <!-- 加班审批单列表 -->
              <div v-if="(detailData.overtimes || []).length > 0" style="margin-bottom:4px">
                <div style="font-size:13px;font-weight:600;color:#334155;padding:8px 12px;background:#f8fafc;border-radius:6px 6px 0 0;border:1px solid #e2e8f0;border-bottom:0">加班审批单</div>
                <el-table :data="detailData.overtimes || []" border stripe size="default" style="width:100%" :header-cell-style="{background:'#f8fafc',color:'#334155',fontWeight:600}">
                  <el-table-column label="序号" type="index" width="60" align="center" />
                  <el-table-column label="加班单号" prop="overtimeNo" min-width="150" align="center" />
                  <el-table-column label="加班日期" prop="overtimeDate" width="120" align="center" />
                  <el-table-column label="开始时间" prop="startTime" width="160" align="center" />
                  <el-table-column label="结束时间" prop="endTime" width="160" align="center" />
                  <el-table-column label="加班(h)" prop="overtimeHours" width="100" align="center">
                    <template #default="scope"><span style="font-weight:600">{{ scope.row.overtimeHours ?? 0 }}</span></template>
                  </el-table-column>
                  <el-table-column label="加班类型" prop="overtimeType" width="100" align="center" />
                  <el-table-column label="补偿方式" prop="compensationType" width="100" align="center" />
                  <el-table-column label="加班原因" prop="reason" min-width="160" show-overflow-tooltip />
                </el-table>
              </div>
              <!-- 打卡加班记录 -->
              <div v-if="pagedRecords.length > 0" style="margin-top:8px">
                <div style="font-size:13px;font-weight:600;color:#334155;padding:8px 12px;background:#f8fafc;border-radius:6px 6px 0 0;border:1px solid #e2e8f0;border-bottom:0">打卡加班记录</div>
                <el-table :data="pagedRecords" border stripe size="default" style="width:100%" @sort-change="onDetailSortChange" :header-cell-style="{background:'#f8fafc',color:'#334155',fontWeight:600}">
                  <el-table-column label="序号" type="index" :index="(i) => (detailPage.pageNum - 1) * detailPage.pageSize + i + 1" width="60" align="center" />
                  <el-table-column label="日期" prop="recordDate" width="120" align="center" sortable :default-sort="{ prop: 'recordDate', order: 'ascending' }" />
                  <el-table-column label="班次" prop="shiftName" min-width="120" align="center" />
                  <el-table-column label="上班打卡" width="140" align="center">
                    <template #default="scope">
                      {{ formatClockTime(scope.row.clockIn) }}
                      <el-tag v-if="String(scope.row.clockInSource) === '1'" type="warning" size="small" effect="plain" style="margin-left:2px">补</el-tag>
                      <el-tag v-else-if="String(scope.row.clockInSource) === '3'" type="success" size="small" effect="plain" style="margin-left:2px">外出</el-tag>
                      <el-tag v-else-if="String(scope.row.clockInSource) === '4'" type="primary" size="small" effect="plain" style="margin-left:2px">出差</el-tag>
                      <el-tag v-else-if="String(scope.row.clockInSource) === '2'" type="info" size="small" effect="plain" style="margin-left:2px">手动</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="下班打卡" width="140" align="center">
                    <template #default="scope">
                      {{ formatClockTime(scope.row.clockOut) }}
                      <el-tag v-if="String(scope.row.clockOutSource) === '1'" type="warning" size="small" effect="plain" style="margin-left:2px">补</el-tag>
                      <el-tag v-else-if="String(scope.row.clockOutSource) === '3'" type="success" size="small" effect="plain" style="margin-left:2px">外出</el-tag>
                      <el-tag v-else-if="String(scope.row.clockOutSource) === '4'" type="primary" size="small" effect="plain" style="margin-left:2px">出差</el-tag>
                      <el-tag v-else-if="String(scope.row.clockOutSource) === '2'" type="info" size="small" effect="plain" style="margin-left:2px">手动</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="加班(h)" prop="overtimeHours" width="100" align="center">
                    <template #default="scope"><span style="font-weight:600">{{ scope.row.overtimeHours ?? 0 }}</span></template>
                  </el-table-column>
                </el-table>
              </div>
              <el-empty v-if="(detailData.overtimes || []).length === 0 && pagedRecords.length === 0" description="当月无加班记录" :image-size="80" />
            </div>
            <div class="detail-pagination" style="display:flex;justify-content:flex-end;padding:12px 16px 4px">
              <el-pagination
                v-model:current-page="detailPage.pageNum"
                v-model:page-size="detailPage.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="detailTotal"
                layout="total, sizes, prev, pager, next, jumper"
                background
                small
              />
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <div style="display:flex;justify-content:space-between;align-items:center;width:100%">
          <span style="font-size:13px;color:#94a3b8">共 {{ detailTotal }} 条记录</span>
          <el-button type="primary" icon="Close" @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

</template>

<script setup name="HrAttendanceMonthly">
import { listHrAttendanceMonthly, getHrAttendanceMonthly, addHrAttendanceMonthly, updateHrAttendanceMonthly, delHrAttendanceMonthly, generateMonthly } from '@/api/hr/hrAttendanceMonthly'
import { MagicStick, OfficeBuilding } from '@element-plus/icons-vue'
import { getMonthlyDetail } from '@/api/hr/hrAttendanceMonthly'
import { useDict } from '@/utils/dict'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker';
import DeptPicker from '@/components/DeptPicker';

const { proxy } = getCurrentInstance()
const { hr_leave_type: hrLeaveTypeOptions } = proxy.useDict('hr_leave_type')

const hrAttendanceMonthlyList = ref([])
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
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, c4: false, e0: false, e1: false, e2: false, e3: false })

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_monthly_index')

/** 列显隐配置 */
const defaultColumns = {
  yearMonth: { label: '年月', visible: true },
  deptName: { label: '部门', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  requiredDays: { label: '应出勤天数', visible: true },
  attendDays: { label: '实际出勤天数', visible: true },
  absentDays: { label: '旷工天数', visible: true },
  leaveDays: { label: '请假天数', visible: true },
  lateCount: { label: '迟到次数', visible: true },
  earlyCount: { label: '早退次数', visible: true },
  outingDays: { label: '外出天数', visible: false },
  tripDays: { label: '出差天数', visible: false },
  overtimeHours: { label: '加班小时', visible: false }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_monthly_columns')
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
  let count = 0
  if (queryParams.value.yearMonth) count++
  if (queryParams.value.deptId) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, yearMonth: undefined, deptId: undefined, deptName: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined },
  rules: {
    yearMonth: [{ required: true, message: '年月不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** ===== 人员/部门/岗位弹框选择 ===== */
const employeePickerRef = ref(null)
const orgPickerRef = ref(null)

function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName };
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
function openOrgPicker() { orgPickerRef.value.open(form.value.orgId); }
function onOrgPickerConfirm(dept) { form.value.orgId = dept.deptId; form.value.orgName = dept.deptName; }
function clearOrg() { form.value.orgId = null; form.value.orgName = ''; }
function onOrgFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.orgId) { clearOrg(); } else { openOrgPicker(); }
    return;
  }
  openOrgPicker();
}

/** 搜索按钮：重置页码后查询 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置筛选条件 */
function resetQuery() {
  queryParams.value.pageNum = 1
  queryParams.value.deptId = undefined
  queryParams.value.deptName = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  handleQuery()
}

function getList() {
  loading.value = true
  listHrAttendanceMonthly(queryParams.value).then(response => {
    hrAttendanceMonthlyList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

/** 部门筛选 */
const deptFilterRef = ref(null)
function onDeptFilterConfirm(dept) {
  queryParams.value.deptId = dept.deptId
  queryParams.value.deptName = dept.deptName
  handleQuery()
}
function clearDeptFilter() {
  queryParams.value.deptId = undefined
  queryParams.value.deptName = undefined
  handleQuery()
}

/** ===== 考勤下钻明细（按字段类型只显示对应数据） ===== */
const detailOpen = ref(false)
const detailType = ref('attend')
const detailRow = ref({})
const detailData = ref({})
const attendanceStatusMap = { '0': '正常', '1': '迟到', '2': '早退', '3': '旷工', '4': '漏卡', '5': '请假', '6': '出差', '7': '加班' }
function attendanceStatusText(v) { return attendanceStatusMap[String(v)] || String(v ?? '-') }
/** 打卡列只显示时间部分（日期已在明细表的"日期"列展示） */
function formatClockTime(v) {
  if (!v) return '-'
  const str = String(v).replace('T', ' ')
  const m = str.match(/\d{2}:\d{2}(:\d{2})?$/)
  return m ? m[0] : str
}
function attendanceTagType(v) {
  return { '0': 'success', '1': 'warning', '2': 'warning', '3': 'danger', '4': 'danger', '5': 'info', '6': 'info', '7': 'primary' }[String(v)] || 'info'
}
/** 各下钻类型对应的标题与日打卡状态过滤 */
const detailTypeMeta = {
  attend: { label: '实际出勤明细', status: ['0', '1', '2', '4'] },
  absent: { label: '旷工明细', status: ['3', '4'] },
  late: { label: '迟到明细', status: ['1'] },
  early: { label: '早退明细', status: ['2'] },
  leave: { label: '请假明细', status: null },
  outing: { label: '外出明细', status: null },
  trip: { label: '出差明细', status: null },
  overtime: { label: '加班明细', status: ['0', '1', '2'] }
}
const detailTypeLabel = computed(() => (detailTypeMeta[detailType.value] || {}).label || '')
/** 弹框分页（前端分页） */
const detailPage = reactive({ pageNum: 1, pageSize: 10 })
const pagedRecords = computed(() => {
  const list = detailRecords.value
  const start = (detailPage.pageNum - 1) * detailPage.pageSize
  return list.slice(start, start + detailPage.pageSize)
})
const pagedLeaveList = computed(() => {
  const list = [...(detailData.value.leaves || [])].sort((a, b) => String(a.startTime).localeCompare(String(b.startTime)))
  if (detailSortOrder.value === 'descending') list.reverse()
  const start = (detailPage.pageNum - 1) * detailPage.pageSize
  return list.slice(start, start + detailPage.pageSize)
})
const detailTotal = computed(() => {
  if (detailType.value === 'leave') return (detailData.value.leaves || []).length
  if (detailType.value === 'outing') return (detailData.value.outings || []).length
  if (detailType.value === 'trip') return (detailData.value.trips || []).length
  if (detailType.value === 'overtime') return (detailData.value.overtimes || []).length + detailRecords.value.length
  return detailRecords.value.length
})
const detailTitle = computed(() => '考勤明细 - ' + (detailRow.value.employeeName || '') + '（' + (detailRow.value.yearMonth || '') + '）')
/** 日打卡明细按状态过滤：出勤/旷工/迟到/早退只看对应记录 */
/** 明细排序方向：null=默认(升序)，'ascending'/'descending' */
const detailSortOrder = ref('ascending')
const detailRecords = computed(() => {
  // 旷工：后端直接生成旷工日虚拟明细（含全天无打卡/漏卡缺半天备注）
  if (detailType.value === 'absent') {
    const list = detailData.value.absentRecords || []
    const sorted = [...list].sort((a, b) => String(a.recordDate).localeCompare(String(b.recordDate)))
    return detailSortOrder.value === 'descending' ? sorted.reverse() : sorted
  }
  // 加班：打卡明细中加班小时>0 的记录
  if (detailType.value === 'overtime') {
    const list = (detailData.value.records || []).filter(r => r.overtimeHours != null && Number(r.overtimeHours) > 0)
    const sorted = [...list].sort((a, b) => String(a.recordDate).localeCompare(String(b.recordDate)))
    return detailSortOrder.value === 'descending' ? sorted.reverse() : sorted
  }
  const list = detailData.value.records || []
  const meta = detailTypeMeta[detailType.value]
  const filtered = (!meta || !meta.status) ? list : list.filter(r => meta.status.includes(String(r.attendanceStatus)))
  // 日期全集排序（保证跨页顺序稳定），默认升序
  const sorted = [...filtered].sort((a, b) => String(a.recordDate).localeCompare(String(b.recordDate)))
  return detailSortOrder.value === 'descending' ? sorted.reverse() : sorted
})
function onDetailSortChange({ order }) {
  detailSortOrder.value = order || 'ascending'
  detailPage.pageNum = 1
}
function showDetail(row, field) {
  detailRow.value = row
  detailType.value = field
  detailPage.pageNum = 1
  getMonthlyDetail(row.employeeId, row.yearMonth).then(res => {
    detailData.value = res.data || {}
    detailOpen.value = true
  })
}

/** ===== 月报自动生成 ===== */

// 当前操作的月份：筛选条件里的年月；未选则默认上个月（月底出上月月报的业务惯例）
const currentMonth = computed(() => {
  const ym = queryParams.value.yearMonth
  if (ym && /^\d{4}-\d{2}$/.test(ym)) return ym
  const d = new Date()
  d.setDate(1)
  d.setMonth(d.getMonth() - 1)
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0')
})
const currentMonthLabel = computed(() => currentMonth.value ? `（${currentMonth.value}）` : '')

// 生成月报：一键按当前月份（筛选年月，默认上个月）自动汇总
function handleGenerate() {
  doGenerate(currentMonth.value)
}

function doGenerate(ym) {
  proxy.$modal.confirm('将按「日考勤打卡 + 已审批请假/加班/外出/出差单」自动汇总生成月报，是否继续？').then(() => {
    generateMonthly(ym).then(res => {
      const d = res.data || {}
      proxy.$modal.msgSuccess('生成完成：' + (d.total ?? 0) + ' 人（考勤记录 ' + (d.recordCount ?? 0) + ' 条，请假 ' + (d.leaveCount ?? 0) + ' 张，加班 ' + (d.overtimeCount ?? 0) + ' 张，外出 ' + (d.outingCount ?? 0) + ' 张，出差 ' + (d.tripCount ?? 0) + ' 张）')
      getList()
    })
  }).catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.monthlyId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function reset() {
  form.value = {
    yearMonth: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    orgId: undefined,
    requiredDays: undefined,
    attendDays: undefined,
    lateCount: undefined,
    earlyCount: undefined,
    absentDays: undefined,
    leaveDays: undefined,
    outingDays: undefined,
    tripDays: undefined,
    overtimeHours: undefined,
    sealedStatus: undefined,
    sealedBy: undefined,
    sealedTime: undefined
  }
  proxy.resetForm('hrAttendanceMonthlyRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增考勤月报'
}

function handleUpdate(row) {
  reset()
  const monthlyId = row.monthlyId || ids.value[0]
  getHrAttendanceMonthly(monthlyId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改考勤月报'
  })
}

function handleView(row) {
  const monthlyId = row.monthlyId
  getHrAttendanceMonthly(monthlyId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrAttendanceMonthlyRef'].validate(valid => {
    if (valid) {
      if (form.value.monthlyId != undefined) {
        updateHrAttendanceMonthly(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrAttendanceMonthly(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const monthlyIds = row.monthlyId || ids.value
  proxy.$modal.confirm('是否确认删除选中的考勤月报？').then(function() {
    return delHrAttendanceMonthly(monthlyIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/monthly/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'monthlyId'), `hrAttendanceMonthly_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-monthly-page {
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
.hr-monthly-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-monthly-page .filter-card { padding:14px 20px 16px; }
.hr-monthly-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-monthly-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-monthly-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-monthly-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-monthly-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-monthly-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-monthly-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-monthly-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-monthly-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-monthly-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-monthly-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-monthly-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-monthly-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-monthly-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-monthly-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-monthly-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-monthly-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-monthly-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-monthly-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-monthly-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-monthly-page .field .control :deep(.el-select) { width:100%; }
.hr-monthly-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-monthly-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-monthly-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-monthly-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-monthly-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-monthly-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-monthly-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-monthly-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-monthly-page .btn-soft .el-icon { font-size:14px; }
.hr-monthly-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-monthly-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-monthly-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-monthly-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-monthly-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-monthly-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-monthly-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-monthly-page .table-wrap { overflow-x:auto; }
.hr-monthly-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-monthly-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-monthly-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-monthly-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-monthly-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-monthly-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-monthly-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-monthly-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-monthly-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-monthly-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-monthly-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-monthly-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-monthly-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-monthly-page .badge.amber .dot { background:var(--amber-500); }
.hr-monthly-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-monthly-page .badge.blue .dot { background:var(--blue-500); }
.hr-monthly-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-monthly-page .badge.green .dot { background:var(--green-500); }
.hr-monthly-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-monthly-page .badge.red .dot { background:var(--red-500); }
.hr-monthly-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-monthly-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-monthly-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-monthly-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-monthly-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-monthly-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-monthly-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-monthly-page .pagination-container :deep(.el-pagination .btn-prev), .hr-monthly-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-monthly-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-monthly-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-monthly-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式（遵循 detail-page-style-guide 规范） ===== */
.hr-monthly-page .rd-page { max-height: 62vh; overflow-y: auto; }
.hr-monthly-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-monthly-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-monthly-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-monthly-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-monthly-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-monthly-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-monthly-page .rd-card-body { padding: 16px; }
.hr-monthly-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-monthly-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-monthly-page .rd-item--full { grid-column: 1 / -1; }
.hr-monthly-page .rd-label { font-size: 12px; color: #909399; }
.hr-monthly-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-monthly-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-monthly-page .rd-empty-icon { margin-bottom: 8px; }
.hr-monthly-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-monthly-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-monthly-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-monthly-page .rd-detail-header-main { flex: 1; }
.hr-monthly-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-monthly-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-monthly-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-monthly-page .rd-detail-header-no { font-size: 13px; color: #909399; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.hr-monthly-page .rd-card { animation: rdFadeIn 0.4s ease-out forwards; }
.hr-monthly-page .rd-card:nth-child(2) { animation-delay: 0.06s; }
.hr-monthly-page .rd-card:nth-child(3) { animation-delay: 0.12s; }
.hr-monthly-page .rd-card:nth-child(4) { animation-delay: 0.18s; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-monthly-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-monthly-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-monthly-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== 考勤下钻明细弹框增强 ===== */
/* 右上角关闭按钮放大提亮（append-to-body 弹窗须用全局选择器） */
.el-dialog.rd-dialog .el-dialog__headerbtn {
  width: 44px;
  height: 44px;
  top: 10px;
  right: 12px;
}
.el-dialog.rd-dialog .el-dialog__headerbtn .el-dialog__close {
  font-size: 22px;
  color: #ffffff;
  font-weight: 700;
  transition: transform 0.15s ease, opacity 0.15s ease;
  opacity: 0.95;
}
.el-dialog.rd-dialog .el-dialog__headerbtn:hover .el-dialog__close {
  transform: scale(1.2);
  opacity: 1;
}
/* 弹框内卡片铺满 */
.el-dialog.rd-dialog .rd-page > .rd-card {
  width: 100%;
}
/* 分页器右对齐区域 */
.detail-pagination {
  border-top: 1px solid #f1f5f9;
}
</style>
