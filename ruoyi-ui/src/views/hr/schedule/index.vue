<template>
  <div class="app-container hr-schedule-page">
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
          <label>排班月份</label>
          <div class="control">
            <el-date-picker v-model="filterMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width: 100%" @change="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>部门</label>
          <div class="control is-select">
            <el-select v-model="filterOrgId" placeholder="全部部门" clearable filterable @change="handleQuery">
              <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="filterEmployeeName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>班次</label>
          <div class="control is-select">
            <el-select v-model="filterShiftId" placeholder="全部班次" clearable filterable @change="handleQuery">
              <el-option v-for="s in shiftOptions" :key="s.shiftId" :label="s.shiftName" :value="s.shiftId" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>排班模板</label>
          <div class="control is-select">
            <el-select v-model="filterTemplateId" placeholder="全部模板" clearable filterable @change="handleQuery">
              <el-option v-for="t in templateOptions" :key="t.templateId" :label="t.templateName" :value="t.templateId" />
            </el-select>
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

    <!-- ===== Calendar Section ===== -->
    <div class="surface">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:schedule:add']">单条新增</el-button>
          <el-button type="success" plain icon="Finished" @click="handleBatch" v-hasPermi="['hr:schedule:batch']">批量排班</el-button>
          <button type="button" class="btn-soft is-outline" @click="handleCopyLast" v-hasPermi="['hr:schedule:batch']"><el-icon><CopyDocument /></el-icon> 复制上月</button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-danger-outline" @click="handleClear" v-hasPermi="['hr:schedule:clear']"><el-icon><Delete /></el-icon> 清除排班</button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:schedule:export']"><el-icon><Download /></el-icon> 导出</button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="showStatusHelp = true">
            <el-icon><WarningFilled /></el-icon> 业务操作说明
          </button>
        </div>
        <div class="right">
          <!-- 视图切换 -->
          <el-radio-group v-model="viewMode" size="small" @change="onViewModeChange">
            <el-radio-button value="calendar"><el-icon><Calendar /></el-icon> 日历视图</el-radio-button>
            <el-radio-button value="list"><el-icon><List /></el-icon> 列表视图</el-radio-button>
          </el-radio-group>
          <template v-if="viewMode === 'calendar'">
            <el-tooltip content="上一月"><el-button icon="ArrowLeft" @click="changeMonth(-1)" circle size="small" /></el-tooltip>
            <span class="month-label">{{ displayMonth }}</span>
            <el-tooltip content="下一月"><el-button icon="ArrowRight" @click="changeMonth(1)" circle size="small" /></el-tooltip>
            <el-tooltip content="返回今天"><el-button icon="HomeFilled" @click="goToday" circle size="small" /></el-tooltip>
          </template>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery" storageKey="hr_schedule_columns"></right-toolbar>
        </div>
      </div>

      <!-- ===== 日历视图 ===== -->
      <div class="calendar-wrap" v-loading="loading" v-if="viewMode === 'calendar'">
        <table class="calendar-table" v-if="calendarDays.length > 0">
          <thead>
            <tr>
              <th class="th-emp">员工</th>
              <th v-for="d in calendarDays" :key="d.date" class="th-day" :class="{ 'is-weekend': d.isWeekend, 'is-today': d.isToday }">
                <div class="th-day-num">{{ d.day }}</div>
                <div class="th-day-week">{{ d.weekLabel }}</div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="emp in calendarEmployees" :key="emp.employeeId">
              <td class="td-emp">
                <div class="emp-name">{{ emp.employeeName }}</div>
                <div class="emp-no">{{ emp.employeeNo }}</div>
              </td>
              <td v-for="d in calendarDays" :key="d.date" class="td-cell" :class="{ 'is-weekend': d.isWeekend, 'is-today': d.isToday, 'has-shift': getShift(emp.employeeId, d.date) }" @click="onCellClick(emp, d)">
                <div class="shift-badge" v-if="getShift(emp.employeeId, d.date)" :style="getShiftStyle(getShift(emp.employeeId, d.date))">
                  {{ getShift(emp.employeeId, d.date).shiftName }}
                </div>
                <div class="shift-empty" v-else>—</div>
              </td>
            </tr>
            <tr v-if="calendarEmployees.length === 0">
              <td :colspan="calendarDays.length + 1" class="empty-row">暂无排班数据，请使用「批量排班」生成</td>
            </tr>
          </tbody>
        </table>
      </div>

    </div>

      <!-- ===== 列表视图 ===== -->
    <div class="surface" v-if="viewMode === 'list'" v-loading="listLoading">
      <div class="list-toolbar">
        <div class="left">
          <el-button type="danger" plain icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete" v-hasPermi="['hr:schedule:remove']">批量删除<span v-if="selectedIds.length > 0" class="sel-count">({{ selectedIds.length }})</span></el-button>
        </div>
        <div class="right">
          <span class="list-total">共 {{ listTotal }} 条</span>
        </div>
      </div>
      <el-table ref="scheduleTableRef" :data="scheduleList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border stripe style="width: 100%">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" :width="colWidth('index', 70)" align="center" />
        <el-table-column label="排班日期" prop="scheduleDate" :width="colWidth('scheduleDate', 120)" align="center" />
        <el-table-column label="员工姓名" prop="employeeName" :min-width="colWidth('employeeName', 120)" show-overflow-tooltip />
        <el-table-column label="工号" prop="employeeNo" :width="colWidth('employeeNo', 100)" align="center" />
        <el-table-column label="部门" prop="orgName" :width="colWidth('orgName', 140)" show-overflow-tooltip />
        <el-table-column label="班次" prop="shiftName" :width="colWidth('shiftName', 100)" align="center">
          <template #default="scope">
            <el-tag size="small" v-if="scope.row.shiftName">{{ scope.row.shiftName }}</el-tag>
            <span v-else style="color:#94a3b8">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" align="center" />
        <el-table-column label="操作" :width="colWidth('action', 150)" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:schedule:edit']">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:schedule:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="listTotal > 0" :total="listTotal" v-model:page="listQuery.pageNum" v-model:limit="listQuery.pageSize" @pagination="getScheduleList" />
    </div>

    <!-- ===== 单条编辑弹窗 ===== -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrScheduleRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 卡片1：人员信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>
                人员信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名" prop="employeeId">
                    <el-input :model-value="form.employeeName" readonly placeholder="请选择人员" @click="openEmployeePicker">
                      <template #append>
                        <el-button v-if="form.employeeId" icon="CircleClose" @click.stop="clearEmployee" />
                        <el-button v-else icon="Search" @click="openEmployeePicker" />
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工号" prop="employeeNo">
                    <el-input :model-value="form.employeeNo" readonly placeholder="选择人员后自动带出" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="部门" prop="orgId">
                    <el-input :model-value="form.orgName" readonly placeholder="选择人员后自动带出" disabled>
                      <template #suffix><el-icon v-if="form.orgName" style="color:#c0c4cc"><OfficeBuilding /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片2：排班信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>
                排班信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="排班日期" prop="scheduleDate">
                    <el-date-picker v-model="form.scheduleDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="班次" prop="shiftId">
                    <el-input :model-value="form.shiftName" readonly placeholder="请选择班次" @click="openShiftPicker">
                      <template #append>
                        <el-button v-if="form.shiftId" icon="CircleClose" @click.stop="clearShift" />
                        <el-button v-else icon="Search" @click="openShiftPicker" />
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片3：其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="请输入备注" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 批量排班弹窗 ===== -->
    <el-dialog v-model="batchOpen" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 3h5v5"/><path d="M8 21H3v-5"/><path d="M21 3l-7 7"/><path d="M3 21l7-7"/></svg></div>
          <span class="rd-detail-header-title">批量排班</span>
        </div>
      </template>
      <el-form ref="batchRef" :model="batchForm" :rules="batchRules" label-width="120px">
        <div class="rd-page">
          <!-- 卡片1：排班方式 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('bc0')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 3h5v5"/><path d="M8 21H3v-5"/><path d="M21 3l-7 7"/><path d="M3 21l7-7"/></svg></span>
                排班方式
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.bc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.bc0">
              <el-form-item label="排班方式" prop="scheduleMode">
                <el-radio-group v-model="batchForm.scheduleMode" @change="onScheduleModeChange">
                  <el-radio value="template">按轮转模板排班</el-radio>
                  <el-radio value="fixed">按固定班次排班</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="日期范围" prop="dateRange">
                <el-date-picker v-model="batchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
              <el-form-item v-if="batchForm.scheduleMode === 'template'" label="排班模板" prop="templateId">
                <el-select v-model="batchForm.templateId" placeholder="选择排班模板" filterable style="width: 100%" @change="onTemplateChange">
                  <el-option v-for="t in templateOptions" :key="t.templateId" :label="t.templateName + '（' + t.cycleDays + '天循环）'" :value="t.templateId" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="batchForm.scheduleMode === 'template'" label="模板预览">
                <div class="template-preview" v-if="batchForm.templatePreview && batchForm.templatePreview.length > 0">
                  <span v-for="(item, idx) in batchForm.templatePreview" :key="idx" class="shift-chip" :class="item.shiftId > 0 ? 'shift-chip--work' : 'shift-chip--rest'">
                    {{ item.shiftName }}
                  </span>
                </div>
                <span v-else style="color:#94a3b8">请先选择模板</span>
              </el-form-item>
              <el-form-item v-if="batchForm.scheduleMode === 'fixed'" label="选择班次" prop="shiftId">
                <el-input :model-value="batchForm.shiftName" readonly placeholder="请选择班次" @click="openBatchShiftPicker">
                  <template #append>
                    <el-button v-if="batchForm.shiftId" icon="CircleClose" @click.stop="clearBatchShift" />
                    <el-button v-else icon="Search" @click="openBatchShiftPicker" />
                  </template>
                </el-input>
              </el-form-item>
            </div>
          </section>
          <!-- 卡片2：选择范围 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('bc1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>
                选择范围
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.bc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.bc1">
              <el-form-item label="选择人员方式" prop="targetMode">
                <el-radio-group v-model="batchForm.targetMode">
                  <el-radio value="employees">指定员工</el-radio>
                  <el-radio value="dept">按部门</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="batchForm.targetMode === 'employees'" label="选择员工" prop="employeeIds">
                <div class="batch-employee-picker">
                  <div class="selected-employee-tags" v-if="batchSelectedEmployees.length > 0">
                    <el-tag v-for="emp in batchSelectedEmployees" :key="emp.employeeId" closable size="small" @close="removeBatchEmployee(emp)" style="margin: 2px 4px 2px 0">
                      {{ emp.employeeName }}（{{ emp.employeeNo }}）
                    </el-tag>
                  </div>
                  <el-button type="primary" plain icon="Search" size="small" @click="openBatchEmployeePicker">
                    {{ batchSelectedEmployees.length > 0 ? '继续添加' : '选择员工' }}
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item v-if="batchForm.targetMode === 'dept'" label="选择部门" prop="orgId">
                <el-input :model-value="batchForm.orgName" readonly placeholder="请选择部门" @click="openBatchDeptPicker">
                  <template #append>
                    <el-button v-if="batchForm.orgId" icon="CircleClose" @click.stop="clearBatchDept" />
                    <el-button v-else icon="Search" @click="openBatchDeptPicker" />
                  </template>
                </el-input>
              </el-form-item>
            </div>
          </section>
          <!-- 卡片3：排班选项 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('bc2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>
                排班选项
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.bc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.bc2">
              <el-form-item label="排班选项">
                <div class="batch-options">
                  <el-checkbox v-model="batchForm.overwrite">覆盖已存在排班</el-checkbox>
                  <el-checkbox v-model="batchForm.excludeWeekend">排除周末</el-checkbox>
                </div>
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitBatch">生成排班</el-button>
        <el-button @click="batchOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="排班管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、排班管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是排班管理？</div>
          <div class="highlight-card-body">
            <strong>排班管理（Schedule Management）</strong>是人力资源管理的核心功能之一，用于按月份为员工安排每日的工作班次。排班数据以月度日历视图展示，支持单条新增、批量排班、复制上月和清除排班等操作。<br/><br/>
            排班管理以 <strong>班次管理</strong> 和 <strong>排班模板</strong> 为基础数据，通过批量排班功能可快速生成整月排班，避免逐日手动操作，极大提升排班效率。
          </div>
        </div>
        <h4>二、核心概念</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="日历视图">以月份为维度，横轴为日期、纵轴为员工，每格显示当天班次名称，点击空白格可新增排班，点击已有班次可编辑</el-descriptions-item>
          <el-descriptions-item label="排班方式">支持「按轮转模板排班」和「按固定班次排班」两种模式。模板排班按预设的循环序列轮转，固定班次排班为每天指定同一班次</el-descriptions-item>
          <el-descriptions-item label="选择范围">批量排班时可选择「指定员工」或「按部门」。指定员工支持弹框多选，按部门则对部门下所有员工生成排班</el-descriptions-item>
          <el-descriptions-item label="覆盖策略">勾选「覆盖已存在排班」会清除目标日期范围内已有的排班数据后重新生成；不勾选则仅对空白日期排班</el-descriptions-item>
          <el-descriptions-item label="周末排除">勾选「排除周末」后，周六和周日不生成排班记录</el-descriptions-item>
        </el-descriptions>
        <h4>三、典型应用场景</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">场景示例</div>
          <div class="highlight-card-body">
            <p>1. <strong>批量排班（模板模式）：</strong>选择「做五休二」模板，指定3名员工，日期范围为整月，系统自动按循环序列为每人生成排班</p>
            <p>2. <strong>批量排班（固定模式）：</strong>选择「早班」班次，按部门选择生产车间，日期范围为一周，所有车间员工排早班</p>
            <p>3. <strong>复制上月：</strong>一键将上月排班数据复制到当月，适用于排班模式相对固定的场景</p>
            <p>4. <strong>单条新增：</strong>在日历上点击空白格，为特定员工在特定日期安排临时班次</p>
          </div>
        </div>
        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>单条新增：</strong>点击工具栏「单条新增」或在日历上点击空白格，选择人员、日期和班次后保存</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>批量排班：</strong>点击「批量排班」按钮，选择排班方式（模板/固定班次），指定人员范围和日期范围，生成排班</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>复制上月：</strong>点击「复制上月」按钮，系统自动将上月排班数据复制到当月，覆盖已有数据</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>清除排班：</strong>点击「清除排班」按钮，清除当月全部排班数据（不可撤销，请谨慎操作）</el-timeline-item>
        </el-timeline>
        <h4>五、注意事项</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">使用提示</div>
          <div class="highlight-card-body">
            <p>• 批量排班前请确保已创建好班次和排班模板，否则无法选择</p>
            <p>• 复制上月排班会覆盖当月已有排班数据，请确认后再操作</p>
            <p>• 清除排班操作不可撤销，建议在操作前导出备份</p>
            <p>• 日历视图中点击已有班次可快速编辑，点击空白格可快速新增</p>
            <p>• 排班数据与考勤管理关联，排班班次是考勤计算的基准</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 人员选择弹窗（单选） -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
    <!-- 人员选择弹窗（多选） -->
    <employee-picker ref="batchEmployeePickerRef" title="批量选择人员" :multiple="true" @confirm="onBatchEmployeePickerConfirm" />
    <!-- 班次选择弹窗 -->
    <shift-picker ref="shiftPickerRef" title="选择班次" @confirm="onShiftPickerConfirm" />
    <!-- 批量排班班次选择弹窗 -->
    <shift-picker ref="batchShiftPickerRef" title="选择班次" @confirm="onBatchShiftPickerConfirm" />
    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />
  </div>
</template>

<script setup name="HrSchedule">
import { listHrSchedule, getHrSchedule, addHrSchedule, updateHrSchedule, delHrSchedule, getScheduleCalendar, batchSchedule, clearSchedule } from '@/api/hr/hrSchedule'
import { Calendar, List } from '@element-plus/icons-vue'
import { listHrShift } from '@/api/hr/hrShift'
import { listHrScheduleTemplate } from '@/api/hr/hrScheduleTemplate'
import { listHrEmployee } from '@/api/hr/hrEmployee'
import { deptTreeSelect } from '@/api/system/user'
import { Filter, Search, Delete, Download, CopyDocument, HomeFilled, WarningFilled, ArrowDown } from '@element-plus/icons-vue'
import EmployeePicker from '@/components/EmployeePicker'
import ShiftPicker from '@/components/ShiftPicker'
import DeptPicker from '@/components/DeptPicker'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
import { OfficeBuilding } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","bc0","bc1","bc2"])
const { colWidth, onHeaderDragEnd, tableRef: scheduleTableRef, applySavedWidths } = useColumnResize('hr_schedule_list')

const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const viewMode = ref('calendar')

/** 列表视图数据 */
const listLoading = ref(false)
const scheduleList = ref([])
const listTotal = ref(0)
const selectedIds = ref([])
const listQuery = ref({ pageNum: 1, pageSize: 10 })
const title = ref('')
const open = ref(false)
const batchOpen = ref(false)
const showStatusHelp = ref(false)

const filterMonth = ref('')
const filterOrgId = ref(undefined)
const filterEmployeeName = ref('')
const filterShiftId = ref(undefined)
const filterTemplateId = ref(undefined)

const deptOptions = ref([])
const shiftOptions = ref([])
const templateOptions = ref([])

const calendarData = ref([])
const calendarDays = ref([])
const calendarEmployees = ref([])

const queryParams = ref({ pageNum: 1, pageSize: 50 })

/** 已选筛选条件数量 */
const activeFilterCount = computed(() => {
  let count = 0
  if (filterMonth.value) count++
  if (filterOrgId.value) count++
  if (filterEmployeeName.value) count++
  if (filterShiftId.value) count++
  if (filterTemplateId.value) count++
  return count
})

const weekLabels = ['日', '一', '二', '三', '四', '五', '六']

/** 班次颜色映射 */
const shiftColors = {}
function getShiftStyle(shift) {
  if (!shift) return {}
  // 休息日特殊样式
  if (!shift.shiftId || shift.shiftId === 0) {
    return { bg: '#f1f5f9', color: '#94a3b8' }
  }
  if (!shiftColors[shift.shiftId]) {
    const palette = [
      { bg: '#e0e7ff', color: '#4338ca' },
      { bg: '#dcfce7', color: '#15803d' },
      { bg: '#fef3c7', color: '#b45309' },
      { bg: '#fce7f3', color: '#be185d' },
      { bg: '#e0f2fe', color: '#0369a1' },
      { bg: '#f3e8ff', color: '#7c3aed' },
    ]
    shiftColors[shift.shiftId] = palette[Object.keys(shiftColors).length % palette.length]
  }
  return shiftColors[shift.shiftId]
}

function getShift(employeeId, dateStr) {
  return calendarData.value.find(d => d.employeeId === employeeId && d.scheduleDate === dateStr)
}

const data = reactive({
  form: {},
  rules: {
    scheduleDate: [{ required: true, message: '排班日期不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    shiftId: [{ required: true, message: '请选择班次', trigger: 'change' }]
  }
})
const { form, rules } = toRefs(data)

const batchForm = reactive({
  scheduleMode: 'template',
  targetMode: 'employees',
  employeeIds: [],
  orgId: undefined,
  orgName: '',
  dateRange: [],
  templateId: undefined,
  templatePreview: [],
  shiftId: undefined,
  shiftName: '',
  overwrite: true,
  excludeWeekend: false
})

/** 批量排班选中的员工对象列表 */
const batchSelectedEmployees = ref([])

const batchRules = {
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  templateId: [{ required: false, message: '请选择排班模板', trigger: 'change' }],
  shiftId: [{ required: false, message: '请选择班次', trigger: 'change' }]
}

const displayMonth = computed(() => {
  if (!filterMonth.value) return ''
  const [y, m] = filterMonth.value.split('-')
  return `${y}年${parseInt(m)}月`
})

/** 生成日历天 */
function buildCalendarDays() {
  const year = parseInt(filterMonth.value.split('-')[0])
  const month = parseInt(filterMonth.value.split('-')[1]) - 1
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const days = []
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(year, month, d)
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const dayOfWeek = date.getDay()
    days.push({
      date: dateStr,
      day: d,
      weekLabel: weekLabels[dayOfWeek],
      isWeekend: dayOfWeek === 0 || dayOfWeek === 6,
      isToday: dateStr === todayStr
    })
  }
  calendarDays.value = days
}

function getList() {
  if (!filterMonth.value) return
  loading.value = true

  const [year, month] = filterMonth.value.split('-')
  const startDate = `${year}-${month}-01`
  const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate()
  const endDate = `${year}-${month}-${String(daysInMonth).padStart(2, '0')}`

  buildCalendarDays()

  const params = { startDate, endDate }
  if (filterOrgId.value) params.orgId = filterOrgId.value

  getScheduleCalendar(params).then(res => {
    calendarData.value = res.data || []

    // 提取员工列表
    const empMap = new Map()
    calendarData.value.forEach(d => {
      if (!empMap.has(d.employeeId)) {
        empMap.set(d.employeeId, { employeeId: d.employeeId, employeeName: d.employeeName, employeeNo: d.employeeNo })
      }
    })

    let emps = Array.from(empMap.values())
    if (filterEmployeeName.value) {
      emps = emps.filter(e => e.employeeName.includes(filterEmployeeName.value))
    }

    // 如果没有排班数据但有部门选择，仍然加载部门员工
    if (emps.length === 0 && filterOrgId.value) {
      listHrEmployee({ orgId: filterOrgId.value, pageNum: 1, pageSize: 200 }).then(res2 => {
        calendarEmployees.value = (res2.rows || []).map(e => ({ employeeId: e.employeeId, employeeName: e.employeeName, employeeNo: e.employeeNo }))
        loading.value = false
      }).catch(() => { loading.value = false })
    } else {
      calendarEmployees.value = emps
      loading.value = false
    }
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  if (!filterMonth.value) {
    const now = new Date()
    filterMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  }
  if (viewMode.value === 'calendar') {
    getList()
  } else {
    getScheduleList()
  }
}

/** 视图切换 */
function onViewModeChange() {
  if (viewMode.value === 'list') {
    listQuery.value.pageNum = 1
    getScheduleList()
  } else {
    getList()
  }
}

/** 查询排班列表（分页） */
function getScheduleList() {
  if (!filterMonth.value) return
  listLoading.value = true
  const [year, month] = filterMonth.value.split('-')
  const startDate = `${year}-${month}-01`
  const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate()
  const endDate = `${year}-${month}-${String(daysInMonth).padStart(2, '0')}`

  const params = {
    ...listQuery.value,
    beginScheduleDate: startDate,
    endScheduleDate: endDate
  }
  if (filterOrgId.value) params.orgId = filterOrgId.value
  if (filterEmployeeName.value) params.employeeName = filterEmployeeName.value
  if (filterShiftId.value) params.shiftId = filterShiftId.value

  listHrSchedule(params).then(res => {
    scheduleList.value = res.rows || []
    listTotal.value = res.total || 0
    listLoading.value = false
    applySavedWidths()
  }).catch(() => { listLoading.value = false })
}

/** 多选 */
function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.scheduleId)
}

/** 批量删除 */
function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  proxy.$modal.confirm(`确认删除选中的 ${selectedIds.value.length} 条排班记录？`).then(() => {
    delHrSchedule(selectedIds.value.join(',')).then(() => {
      proxy.$modal.msgSuccess('删除成功')
      selectedIds.value = []
      getScheduleList()
    })
  }).catch(() => {})
}

/** 单条删除 */
function handleDelete(row) {
  proxy.$modal.confirm(`确认删除「${row.employeeName}」在「${row.scheduleDate}」的排班记录？`).then(() => {
    delHrSchedule(row.scheduleId).then(() => {
      proxy.$modal.msgSuccess('删除成功')
      getScheduleList()
    })
  }).catch(() => {})
}

function resetQuery() {
  filterOrgId.value = undefined
  filterEmployeeName.value = undefined
  filterShiftId.value = undefined
  filterTemplateId.value = undefined
  showAdvanced.value = false
  handleQuery()
}

function changeMonth(delta) {
  const [y, m] = filterMonth.value.split('-').map(Number)
  let newMonth = m - 1 + delta
  let newYear = y
  if (newMonth < 0) { newMonth = 11; newYear-- }
  if (newMonth > 11) { newMonth = 0; newYear++ }
  filterMonth.value = `${newYear}-${String(newMonth + 1).padStart(2, '0')}`
  getList()
}

function goToday() {
  const now = new Date()
  filterMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  getList()
}

/** 单元格点击 */
function onCellClick(emp, day) {
  const existing = getShift(emp.employeeId, day.date)
  if (existing) {
    handleUpdate({ scheduleId: existing.scheduleId })
  } else {
    reset()
    form.value.employeeId = emp.employeeId
    form.value.employeeName = emp.employeeName
    form.value.employeeNo = emp.employeeNo
    form.value.scheduleDate = day.date
    open.value = true
    title.value = '新增排班'
  }
}

/** ===== 单条新增/编辑 ===== */
function reset() {
  form.value = {
    scheduleDate: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined,
    orgId: undefined, orgName: undefined, shiftId: undefined, shiftName: undefined, remark: undefined
  }
  proxy.resetForm('hrScheduleRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增排班'
}

function handleUpdate(row) {
  reset()
  getHrSchedule(row.scheduleId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改排班'
  })
}

function submitForm() {
  proxy.$refs['hrScheduleRef'].validate(valid => {
    if (valid) {
      if (form.value.scheduleId != undefined) {
        updateHrSchedule(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrSchedule(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function cancel() { open.value = false; reset() }

function handleExport() {
  const [year, month] = filterMonth.value.split('-')
  const startDate = `${year}-${month}-01`
  const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate()
  const endDate = `${year}-${month}-${String(daysInMonth).padStart(2, '0')}`
  proxy.download('hr/schedule/export', { beginScheduleDate: startDate, endScheduleDate: endDate }, `hrSchedule_${filterMonth.value}.xlsx`)
}

/** ===== 批量排班 ===== */
function handleBatch() {
  batchForm.scheduleMode = 'template'
  batchForm.targetMode = 'employees'
  batchForm.employeeIds = []
  batchForm.orgId = undefined
  batchForm.orgName = ''
  batchForm.dateRange = []
  batchForm.templateId = undefined
  batchForm.templatePreview = []
  batchForm.shiftId = undefined
  batchForm.shiftName = ''
  batchForm.overwrite = true
  batchForm.excludeWeekend = false
  batchSelectedEmployees.value = []

  // 默认日期范围：当前月
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const firstDay = `${year}-${String(month + 1).padStart(2, '0')}-01`
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const lastDay = `${year}-${String(month + 1).padStart(2, '0')}-${String(daysInMonth).padStart(2, '0')}`
  batchForm.dateRange = [firstDay, lastDay]

  batchOpen.value = true
}

function onScheduleModeChange() {
  batchRules.templateId[0].required = batchForm.scheduleMode === 'template'
  batchRules.shiftId[0].required = batchForm.scheduleMode === 'fixed'
}

function onTemplateChange(templateId) {
  const t = templateOptions.value.find(t => t.templateId === templateId)
  if (t) {
    try {
      batchForm.templatePreview = JSON.parse(t.shiftSequence)
    } catch (e) {
      batchForm.templatePreview = []
    }
  }
}

function submitBatch() {
  proxy.$refs['batchRef'].validate(valid => {
    if (!valid) return

    if (batchForm.scheduleMode === 'template' && !batchForm.templateId) {
      proxy.$modal.msgError('请选择排班模板')
      return
    }
    if (batchForm.scheduleMode === 'fixed' && !batchForm.shiftId) {
      proxy.$modal.msgError('请选择班次')
      return
    }
    if (batchForm.targetMode === 'employees' && batchSelectedEmployees.value.length === 0) {
      proxy.$modal.msgError('请选择员工')
      return
    }
    if (batchForm.targetMode === 'dept' && !batchForm.orgId) {
      proxy.$modal.msgError('请选择部门')
      return
    }
    if (!batchForm.dateRange || batchForm.dateRange.length !== 2) {
      proxy.$modal.msgError('请选择日期范围')
      return
    }

    const dto = {
      employeeIds: batchForm.targetMode === 'employees' ? batchSelectedEmployees.value.map(e => e.employeeId) : null,
      orgId: batchForm.targetMode === 'dept' ? batchForm.orgId : null,
      startDate: batchForm.dateRange[0],
      endDate: batchForm.dateRange[1],
      templateId: batchForm.scheduleMode === 'template' ? batchForm.templateId : null,
      shiftId: batchForm.scheduleMode === 'fixed' ? batchForm.shiftId : null,
      shiftName: batchForm.scheduleMode === 'fixed' ? batchForm.shiftName : null,
      overwrite: batchForm.overwrite,
      excludeWeekend: batchForm.excludeWeekend
    }

    batchSchedule(dto).then(res => {
      proxy.$modal.msgSuccess(res.msg || '批量排班成功')
      batchOpen.value = false
      getList()
    }).catch(() => {})
  })
}

/** 复制上月排班 */
function handleCopyLast() {
  const [y, m] = filterMonth.value.split('-').map(Number)
  let prevMonth = m - 2
  let prevYear = y
  if (prevMonth < 0) { prevMonth = 11; prevYear-- }
  const prevStart = `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-01`
  const prevDays = new Date(prevYear, prevMonth + 1, 0).getDate()
  const prevEnd = `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-${String(prevDays).padStart(2, '0')}`

  const curStart = `${y}-${String(m).padStart(2, '0')}-01`
  const curDays = new Date(y, m, 0).getDate()
  const curEnd = `${y}-${String(m).padStart(2, '0')}-${String(curDays).padStart(2, '0')}`

  proxy.$modal.confirm(`将 ${prevStart} 至 ${prevEnd} 的排班复制到 ${curStart} 至 ${curEnd}，覆盖已存在排班？`).then(() => {
    getScheduleCalendar({ startDate: prevStart, endDate: prevEnd }).then(res => {
      const prevData = res.data || []
      if (prevData.length === 0) {
        proxy.$modal.msgWarning('上月无排班数据可复制')
        return
      }

      const toInsert = []
      prevData.forEach(d => {
        const prevDate = new Date(d.scheduleDate)
        const curDate = `${y}-${String(m).padStart(2, '0')}-${String(prevDate.getDate()).padStart(2, '0')}`
        if (prevDate.getDate() <= curDays) {
          toInsert.push({ ...d, scheduleDate: curDate, scheduleId: undefined })
        }
      })

      if (toInsert.length === 0) {
        proxy.$modal.msgWarning('无可复制的数据')
        return
      }

      clearSchedule({ startDate: curStart, endDate: curEnd, orgId: filterOrgId.value }).then(() => {
        const promises = toInsert.map(s => addHrSchedule(s))
        Promise.all(promises).then(() => {
          proxy.$modal.msgSuccess(`成功复制 ${toInsert.length} 条排班记录`)
          getList()
        }).catch(() => {
          proxy.$modal.msgError('部分记录插入失败')
          getList()
        })
      })
    })
  }).catch(() => {})
}

/** 清除排班 */
function handleClear() {
  if (!filterMonth.value) {
    proxy.$modal.msgWarning('请先选择月份')
    return
  }
  const [y, m] = filterMonth.value.split('-')
  const startDate = `${y}-${m}-01`
  const daysInMonth = new Date(parseInt(y), parseInt(m), 0).getDate()
  const endDate = `${y}-${m}-${String(daysInMonth).padStart(2, '0')}`

  proxy.$modal.confirm(`确认清除 ${displayMonth.value} 的全部排班数据？此操作不可撤销！`).then(() => {
    clearSchedule({ startDate, endDate, orgId: filterOrgId.value }).then(res => {
      proxy.$modal.msgSuccess(res.msg || '清除成功')
      getList()
    })
  }).catch(() => {})
}

/** ===== 单条弹窗：人员/班次弹框选择 ===== */
const employeePickerRef = ref(null)
const shiftPickerRef = ref(null)

function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId) }
function onEmployeePickerConfirm(emp) {
  form.value.employeeId = emp.employeeId
  form.value.employeeName = emp.employeeName
  form.value.employeeNo = emp.employeeNo || ''
  form.value.orgId = emp.orgId || null
  form.value.orgName = emp.orgName || ''
}
function clearEmployee() {
  form.value.employeeId = null; form.value.employeeName = ''; form.value.employeeNo = ''; form.value.orgId = null; form.value.orgName = ''
}
function openShiftPicker() { shiftPickerRef.value.open(form.value.shiftId) }
function onShiftPickerConfirm(shift) {
  form.value.shiftId = shift.shiftId; form.value.shiftName = shift.shiftName
}
function clearShift() { form.value.shiftId = null; form.value.shiftName = '' }

/** ===== 批量排班弹窗：人员多选/班次/部门弹框选择 ===== */
const batchEmployeePickerRef = ref(null)
const batchShiftPickerRef = ref(null)
const deptPickerRef = ref(null)

function openBatchEmployeePicker() {
  const currentIds = batchSelectedEmployees.value.map(e => e.employeeId)
  batchEmployeePickerRef.value.open(currentIds)
}
function onBatchEmployeePickerConfirm(employees) {
  // 合并：保留不在新选择中的旧选择 + 新选择
  const newIds = new Set(employees.map(e => e.employeeId))
  const kept = batchSelectedEmployees.value.filter(e => !newIds.has(e.employeeId))
  batchSelectedEmployees.value = [...kept, ...employees]
  batchForm.employeeIds = batchSelectedEmployees.value.map(e => e.employeeId)
}
function removeBatchEmployee(emp) {
  batchSelectedEmployees.value = batchSelectedEmployees.value.filter(e => e.employeeId !== emp.employeeId)
  batchForm.employeeIds = batchSelectedEmployees.value.map(e => e.employeeId)
}

function openBatchShiftPicker() { batchShiftPickerRef.value.open(batchForm.shiftId) }
function onBatchShiftPickerConfirm(shift) {
  batchForm.shiftId = shift.shiftId; batchForm.shiftName = shift.shiftName
}
function clearBatchShift() { batchForm.shiftId = null; batchForm.shiftName = '' }

function openBatchDeptPicker() { deptPickerRef.value.open(batchForm.orgId) }
function onDeptPickerConfirm(dept) {
  batchForm.orgId = dept.deptId; batchForm.orgName = dept.deptName
}
function clearBatchDept() { batchForm.orgId = null; batchForm.orgName = '' }

/** ===== 初始化加载 ===== */
function loadDeptOptions() {
  deptTreeSelect().then(res => {
    function flatten(nodes, result) {
      nodes.forEach(n => {
        result.push({ deptId: n.id, deptName: n.label })
        if (n.children) flatten(n.children, result)
      })
      return result
    }
    deptOptions.value = flatten(res.data || [], [])
  })
}

function loadShiftOptions() {
  listHrShift({ pageNum: 1, pageSize: 100 }).then(res => { shiftOptions.value = res.rows || [] })
}

function loadTemplateOptions() {
  listHrScheduleTemplate({ pageNum: 1, pageSize: 100, status: '0' }).then(res => { templateOptions.value = res.rows || [] })
}

// 初始化
const now = new Date()
filterMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

loadDeptOptions()
loadShiftOptions()
loadTemplateOptions()
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-schedule-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.hr-schedule-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-schedule-page .filter-card { padding:14px 20px 16px; }
.hr-schedule-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-schedule-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-schedule-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-schedule-page .filter-card .adv-link { display:inline-flex; align-items:center; gap:4px; font-size:13px; color:var(--ink-500); cursor:pointer; user-select:none; transition:color .15s; }
.hr-schedule-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-schedule-page .filter-card .adv-link .chev { font-size:12px; transition:transform .2s; }
.hr-schedule-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-schedule-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(3, minmax(0,1fr)); gap:12px 16px; }
.hr-schedule-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-schedule-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-schedule-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-schedule-page .field { display:flex; flex-direction:column; gap:6px; }
.hr-schedule-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-schedule-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-schedule-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-schedule-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-schedule-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-schedule-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-schedule-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-schedule-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-schedule-page .field .control :deep(.el-select) { width:100%; }
.hr-schedule-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-schedule-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-schedule-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-schedule-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-schedule-page .toolbar .left { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.hr-schedule-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-schedule-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-schedule-page .month-label { font-size:15px; font-weight:600; color:var(--ink-700); min-width:100px; text-align:center; }
.hr-schedule-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-schedule-page .btn-soft .el-icon { font-size:14px; }
.hr-schedule-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-schedule-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-schedule-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-schedule-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-schedule-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-schedule-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-schedule-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Calendar Table ===== */
.hr-schedule-page .calendar-wrap { overflow-x:auto; max-height:70vh; overflow-y:auto; }
.hr-schedule-page .calendar-table { width:100%; border-collapse:collapse; font-size:12px; }
.hr-schedule-page .calendar-table th, .hr-schedule-page .calendar-table td { border:1px solid var(--ink-200); text-align:center; }
.hr-schedule-page .calendar-table .th-emp { position:sticky; left:0; z-index:3; background:var(--ink-50); min-width:100px; max-width:140px; padding:8px; font-weight:600; color:var(--ink-700); }
.hr-schedule-page .calendar-table .th-day { min-width:42px; padding:6px 2px; background:var(--ink-50); }
.hr-schedule-page .calendar-table .th-day.is-weekend { background:#fef2f2; }
.hr-schedule-page .calendar-table .th-day.is-today { background:#e0e7ff; }
.hr-schedule-page .calendar-table .th-day-num { font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-schedule-page .calendar-table .th-day-week { font-size:11px; color:var(--ink-400); }
.hr-schedule-page .calendar-table .th-day.is-weekend .th-day-num { color:#b91c1c; }
.hr-schedule-page .calendar-table .td-emp { position:sticky; left:0; z-index:2; background:#fff; min-width:100px; max-width:140px; padding:6px 8px; text-align:left; }
.hr-schedule-page .calendar-table .td-emp .emp-name { font-size:13px; font-weight:600; color:var(--ink-700); }
.hr-schedule-page .calendar-table .td-emp .emp-no { font-size:11px; color:var(--ink-400); }
.hr-schedule-page .calendar-table .td-cell { min-width:42px; height:40px; cursor:pointer; transition:background .1s; }
.hr-schedule-page .calendar-table .td-cell:hover { background:#f0f7ff; }
.hr-schedule-page .calendar-table .td-cell.is-weekend { background:#fffbeb; }
.hr-schedule-page .calendar-table .td-cell.is-today { background:#eef2ff; }
.hr-schedule-page .calendar-table .td-cell.has-shift { background:#f8fafc; }
.hr-schedule-page .calendar-table .shift-badge { display:inline-block; padding:2px 6px; border-radius:4px; font-size:11px; font-weight:600; line-height:1.4; white-space:nowrap; }
.hr-schedule-page .calendar-table .shift-empty { color:var(--ink-300); font-size:11px; }
.hr-schedule-page .calendar-table .empty-row { text-align:center; padding:40px; color:var(--ink-400); font-size:14px; }

/* ===== Pagination ===== */
.hr-schedule-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-schedule-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-schedule-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-schedule-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-schedule-page .pagination-container :deep(.el-pagination .btn-prev), .hr-schedule-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-schedule-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-schedule-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-schedule-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情页卡片式样式（遵循 detail-page-style-guide 规范） ===== */

/* 覆盖 el-dialog 默认 header */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 14px; right: 14px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.rd-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.rd-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width: none; }

/* ---- 标题横幅 ---- */
.hr-schedule-page .rd-detail-header { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.hr-schedule-page .rd-detail-header::before { content: ''; position: absolute; top: -30px; right: -15px; width: 140px; height: 140px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%); pointer-events: none; }
.hr-schedule-page .rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; border-radius: 10px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 4px 12px rgb(0 0 0 / 0.1); }
.hr-schedule-page .rd-detail-header-title { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: -0.02em; line-height: 1.3; }

/* ---- 卡片 ---- */
.hr-schedule-page .rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4,0,0.2,1); animation: rdFadeIn 0.4s ease-out forwards; }
.hr-schedule-page .rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.hr-schedule-page .rd-card:last-child { margin-bottom: 0; }
.hr-schedule-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; }
.hr-schedule-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.hr-schedule-page .rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.hr-schedule-page .rd-card-body { padding: 14px 16px; }
.hr-schedule-page .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 28px; height: 28px; border: none; background: transparent; cursor: pointer; color: #6b7280; border-radius: 6px; transition: all 0.15s; }
.hr-schedule-page .rd-collapse-btn:hover { background: #f3f4f6; color: #374151; }
.hr-schedule-page .rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); transition: transform 0.2s; }

/* ---- 批量排班表单 ---- */
.hr-schedule-page .batch-employee-picker { display: flex; flex-direction: column; gap: 8px; }
.hr-schedule-page .selected-employee-tags { display: flex; flex-wrap: wrap; max-height: 120px; overflow-y: auto; padding: 4px 0; }
.hr-schedule-page .batch-options { display: flex; flex-direction: column; gap: 8px; }
.hr-schedule-page .template-preview { display: flex; flex-wrap: wrap; gap: 4px; }
.hr-schedule-page .shift-chip { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }
.hr-schedule-page .shift-chip--work { background: #e0e7ff; color: #4338ca; }
.hr-schedule-page .shift-chip--rest { background: #f1f5f9; color: #64748b; }

/* ---- 进入动画 ---- */
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.hr-schedule-page .rd-card:nth-child(2) { animation-delay: 0.06s; }
.hr-schedule-page .rd-card:nth-child(3) { animation-delay: 0.12s; }
.hr-schedule-page .rd-card:nth-child(4) { animation-delay: 0.18s; }

/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 16px 0 8px; font-size: 16px; font-weight: 700; color: #111827; padding-left: 10px; border-left: 4px solid #4f46e5; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .highlight-card { padding: 12px 16px; border-radius: 8px; border: 1px solid #e5e7eb; margin-bottom: 12px; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 700; margin-bottom: 6px; }
.status-help-content .highlight-card-body { font-size: 13px; color: #374151; line-height: 1.8; }
.status-help-content .highlight-card-body p { margin: 4px 0; }
.status-help-content .highlight-primary { background: #eff6ff; border-color: #bfdbfe; }
.status-help-content .highlight-primary .highlight-card-title { color: #1d4ed8; }
.status-help-content .highlight-success { background: #ecfdf5; border-color: #a7f3d0; }
.status-help-content .highlight-success .highlight-card-title { color: #047857; }
.status-help-content .highlight-warning { background: #fffbeb; border-color: #fde68a; }
.status-help-content .highlight-warning .highlight-card-title { color: #b45309; }

/* ===== 列表视图 ===== */
.hr-schedule-page .list-toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 20px; border-bottom:1px solid var(--ink-100); background:var(--ink-50); }
.hr-schedule-page .list-toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-schedule-page .list-toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-schedule-page .sel-count { margin-left:4px; font-size:12px; font-weight:600; }
.hr-schedule-page .list-total { font-size:13px; color:var(--ink-500); }
.hr-schedule-page .surface .el-table { border:0; }
.hr-schedule-page .surface .el-table th.el-table__cell { background:var(--ink-50); color:var(--ink-700); font-weight:600; font-size:13px; }
.hr-schedule-page .surface .el-table td.el-table__cell { font-size:13px; color:var(--ink-700); }
.hr-schedule-page .surface .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-schedule-page .surface .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-schedule-page .surface .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-schedule-page .surface .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-schedule-page .surface .pagination-container :deep(.el-pagination .btn-prev), .hr-schedule-page .surface .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-schedule-page .surface .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-schedule-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-schedule-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-schedule-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
