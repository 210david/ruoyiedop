<template>
  <div class="app-container hr-leaveBalance-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
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
          <label>部门</label>
          <div class="control">
            <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>假别</label>
          <div class="control">
            <el-select v-model="queryParams.leaveType" placeholder="全部假别" clearable @change="handleQuery">
              <el-option v-for="d in leaveTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>年度</label>
          <div class="control">
            <el-select v-model="queryParams.year" placeholder="全部年度" clearable style="width: 100%">
              <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:leaveBalance:add']">新增</el-button>
          <el-button type="primary" plain icon="MagicStick" @click="handleBatchGenerate" v-hasPermi="['hr:leaveBalance:add']">批量生成</el-button>
          <el-button type="primary" plain icon="Upload" @click="handleImport" v-hasPermi="['hr:leaveBalance:add']">导入</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:leaveBalance:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:leaveBalance:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:leaveBalance:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_leaveBalance_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrLeaveBalanceList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible" />
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 120)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="部门" prop="deptName" key="deptName" align="center" :width="colWidth('deptName', 140)" resizable v-if="columns.deptName.visible" />
          <el-table-column label="假别" prop="leaveType" key="leaveType" align="center" :width="colWidth('leaveType', 140)" resizable v-if="columns.leaveType.visible">
            <template #default="scope">
              <span class="badge" :class="leaveTypeClass(scope.row.leaveType)"><span class="dot"></span>{{ leaveTypeLabel(scope.row.leaveType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="年度" prop="year" key="year" align="center" :width="colWidth('year', 100)" resizable v-if="columns.year.visible" />
          <el-table-column label="总额度" prop="totalDays" key="totalDays" align="center" :width="colWidth('totalDays', 110)" resizable v-if="columns.totalDays.visible" />
          <el-table-column label="已休天数" prop="usedDays" key="usedDays" align="center" :width="colWidth('usedDays', 110)" resizable v-if="columns.usedDays.visible" />
          <el-table-column label="冻结天数" prop="frozenDays" key="frozenDays" align="center" :width="colWidth('frozenDays', 110)" resizable v-if="columns.frozenDays.visible">
            <template #default="scope">
              <span v-if="scope.row.frozenDays && scope.row.frozenDays > 0" style="color: #e6a23c; font-weight: 600;">{{ scope.row.frozenDays }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="剩余天数" prop="remainingDays" key="remainingDays" align="center" :width="colWidth('remainingDays', 110)" resizable v-if="columns.remainingDays.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:leaveBalance:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:leaveBalance:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:leaveBalance:remove']">删除</el-button>
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
    <el-dialog v-model="open" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrLeaveBalanceRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
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
                  <el-form-item label="工号">
                    <el-input :model-value="form.employeeNo" readonly placeholder="选择人员后自动带出" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="部门">
                    <el-input :model-value="form.deptName" readonly placeholder="选择人员后自动带出" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="假别" prop="leaveType">
                    <el-select v-model="form.leaveType" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in leaveTypeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="年度" prop="year">
                    <el-select v-model="form.year" placeholder="请选择年度" style="width: 100%">
                      <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"/><path d="M22 12A10 10 0 0 0 12 2v10z"/></svg></span>余额信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="总额度" prop="totalDays">
                    <el-input-number v-model="form.totalDays" :precision="1" :min="0" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="已休天数" prop="usedDays">
                    <el-input-number v-model="form.usedDays" :precision="1" :min="0" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="剩余天数"><el-input :model-value="computedRemaining != null ? computedRemaining + ' 天' : ''" disabled placeholder="总额度-已休天数自动计算" /></el-form-item>
                </el-col>
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

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">假期余额详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.balanceId">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">员工：{{ viewData.employeeName || '-' }}</span>
              <span class="rd-detail-header-no">年度：{{ viewData.year ?? '-' }}</span>
            </div>
          </div>
        </div>
      </template>
      <el-tabs v-model="activeTab" class="balance-detail-tabs">
        <!-- ===== 基本信息+余额信息页签 ===== -->
        <el-tab-pane label="余额信息" name="info">
          <div class="rd-page">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('c0')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c0">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">年度</span><div class="rd-value">{{ viewData.year ?? '-' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('c1')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"/><path d="M22 12A10 10 0 0 0 12 2v10z"/></svg></span>余额信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c1">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">假别</span><div class="rd-value"><dict-tag :options="leaveTypeOptions" :value="viewData.leaveType"/></div></div>
                  <div class="rd-item"><span class="rd-label">总额度（天）</span><div class="rd-value">{{ viewData.totalDays ?? '-' }}</div></div>
<div class="rd-item"><span class="rd-label">已休天数（天）</span><div class="rd-value">{{ viewData.usedDays ?? '-' }}</div></div>
<div class="rd-item"><span class="rd-label">冻结天数（天）</span><div class="rd-value">{{ viewData.frozenDays ? (viewData.frozenDays + '（审批中冻结）') : '0' }}</div></div>
<div class="rd-item"><span class="rd-label">剩余天数（天）</span><div class="rd-value">{{ viewData.remainingDays ?? '-' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card" v-if="viewData.remark">
              <div class="rd-card-header" @click="toggleCard('c2')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c2">
                <div class="rd-grid">
                  <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
                </div>
              </div>
            </section>
          </div>
        </el-tab-pane>
        <!-- ===== 扣减明细页签 ===== -->
        <el-tab-pane label="扣减明细" name="logs">
          <div class="rd-page" style="max-height:50vh;">
            <el-table v-loading="logLoading" :data="balanceLogList" border class="app-table" size="small" style="width:100%">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="请假单号" prop="leaveReqNo" align="center" min-width="140" />
              <el-table-column label="变动天数" prop="changeDays" align="center" width="100">
                <template #default="scope">
                  <span :style="{ color: scope.row.changeDays > 0 ? '#f56c6c' : '#67c23a', fontWeight: 600 }">
                    {{ scope.row.changeDays > 0 ? '-' + scope.row.changeDays : '+' + Math.abs(scope.row.changeDays) }} 天
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="变动前已休" prop="beforeUsed" align="center" width="100" />
              <el-table-column label="变动后已休" prop="afterUsed" align="center" width="100" />
              <el-table-column label="变动前剩余" prop="beforeRemaining" align="center" width="100" />
              <el-table-column label="变动后剩余" prop="afterRemaining" align="center" width="100" />
              <el-table-column label="变动前冻结" prop="beforeFrozen" align="center" width="100">
                <template #default="scope">
                  <span v-if="scope.row.beforeFrozen != null">{{ scope.row.beforeFrozen }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="变动后冻结" prop="afterFrozen" align="center" width="100">
                <template #default="scope">
                  <span v-if="scope.row.afterFrozen != null">{{ scope.row.afterFrozen }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="类型" prop="changeType" align="center" width="90">
                <template #default="scope">
                  <span class="log-type-tag" :class="scope.row.changeType">{{ logTypeLabel(scope.row.changeType) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="原因" prop="changeReason" align="center" min-width="140" show-overflow-tooltip />
              <el-table-column label="操作人" prop="operator" align="center" width="90" />
              <el-table-column label="时间" prop="createTime" align="center" min-width="150" />
            </el-table>
            <div v-if="!logLoading && balanceLogList.length === 0" class="log-empty">
              <el-empty description="暂无扣减记录" :image-size="80" />
            </div>
            <div class="log-pagination" v-if="logTotal > 0">
              <pagination v-show="logTotal > 0" :total="logTotal" v-model:page="logQuery.pageNum" v-model:limit="logQuery.pageSize" :page-sizes="[10, 20, 50]" @pagination="fetchBalanceLogs(currentBalanceId)" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 批量生成弹窗 ===== -->
    <el-dialog v-model="batchOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon" style="background:#f0f9eb;color:#67c23a"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 3v12"/><circle cx="18" cy="6" r="3"/><circle cx="6" cy="18" r="3"/><path d="M18 9a9 9 0 0 1-9 9"/></svg></div>
          <span class="rd-detail-header-title">批量生成假期余额</span>
        </div>
      </template>
      <el-form ref="batchRef" :model="batchForm" :rules="batchRules" label-width="120px">
        <el-form-item label="年度" prop="year">
          <el-select v-model="batchForm.year" placeholder="请选择年度" style="width:100%">
            <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门范围">
          <el-input
            v-model="batchDeptNames"
            readonly
            placeholder="不选则处理全部部门"
            style="width: 100%"
            @click="openDeptPicker"
          >
            <template #append>
              <el-button icon="Search" @click="openDeptPicker" />
            </template>
            <template #suffix>
              <el-icon v-if="batchForm.deptIds.length > 0" class="clear-icon" @click.stop="clearDeptSelection"><CircleClose /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="假别及额度配置" prop="leaveTypeDaysList">
          <div style="width:100%">
            <div v-for="(item, idx) in batchForm.leaveTypeDaysList" :key="idx" class="batch-row">
              <el-select v-model="item.leaveType" placeholder="选择假别" style="width:160px">
                <el-option v-for="d in leaveTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
              <el-input-number v-model="item.totalDays" :precision="1" :min="0" :step="0.5" controls-position="right" style="width:140px" placeholder="额度" />
              <span class="batch-unit">天</span>
              <el-button link type="danger" icon="Delete" @click="batchForm.leaveTypeDaysList.splice(idx, 1)" v-if="batchForm.leaveTypeDaysList.length > 1" />
              <el-button link type="primary" icon="Plus" @click="batchForm.leaveTypeDaysList.push({ leaveType: '', totalDays: 0 })" v-if="idx === batchForm.leaveTypeDaysList.length - 1" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="覆盖已有记录">
          <el-switch v-model="batchForm.overwrite" active-text="是（更新总额度，保留已休天数）" inactive-text="否（跳过已有记录）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="batchLoading" @click="submitBatchGenerate">确认生成</el-button>
        <el-button @click="batchOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 导入弹窗 ===== -->
    <excel-import-dialog
      ref="importDialogRef"
      title="批量导入假期余额"
      action="/hr/leaveBalance/importData"
      template-action="/hr/leaveBalance/importTemplate"
      template-file-name="假期余额导入模板"
      update-support-label="覆盖已有记录"
      :tips="importTips"
      :column-labels="importColumnLabels"
      @success="handleImportSuccess"
    />

    <!-- 部门多选弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择部门范围" :multiple="true" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />

    <!-- 人员选择弹窗 -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
  </div>
</template>

<script setup name="HrLeaveBalance">
import { listHrLeaveBalance, getHrLeaveBalance, addHrLeaveBalance, updateHrLeaveBalance, delHrLeaveBalance, batchGenerateBalance } from '@/api/hr/hrLeaveBalance'
import { listHrLeaveBalanceLog } from '@/api/hr/hrLeaveBalanceLog'
import { Filter, Search, Edit, Delete, Download, Plus, MagicStick, Upload, CircleClose } from '@element-plus/icons-vue'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker';
import DeptPicker from '@/components/DeptPicker/index.vue';

const { proxy } = getCurrentInstance()
const { hr_leave_type: leaveTypeOptions } = proxy.useDict('hr_leave_type')

const hrLeaveBalanceList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

/** 年度选项：近2年 ~ 未来2年 */
const yearOptions = Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - 2 + i)

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_leaveBalance_index')

/** 列显隐配置 - 从 localStorage 恢复保存的设置 */
const defaultColumns = {
  employeeNo: { label: '工号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  deptName: { label: '部门', visible: true },
  leaveType: { label: '假别', visible: true },
  year: { label: '年度', visible: true },
  totalDays: { label: '总额度', visible: true },
  usedDays: { label: '已休天数', visible: true },
  frozenDays: { label: '冻结天数', visible: true },
  remainingDays: { label: '剩余天数', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_leaveBalance_columns')
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

/** 查看详情 */
const viewData = ref({})
const collapsedCards = reactive({ c0: false, c1: false, c2: false, e0: false, e1: false })
function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

/** 扣减明细 */
const activeTab = ref('info')
const balanceLogList = ref([])
const logLoading = ref(false)
const logTotal = ref(0)
const logQuery = reactive({ pageNum: 1, pageSize: 10 })

/** 变动类型标签 */
function logTypeLabel(v) {
  const map = { deduct: '扣减', refund: '退回', adjust: '手动调整', freeze: '冻结', unfreeze: '解冻' }
  return map[v] || v || '-'
}

/** 查询扣减明细 */
function fetchBalanceLogs(balanceId) {
  if (!balanceId) return
  logLoading.value = true
  listHrLeaveBalanceLog({ balanceId, ...logQuery }).then(response => {
    balanceLogList.value = response.rows || []
    logTotal.value = response.total || 0
    logLoading.value = false
  }).catch(() => {
    logLoading.value = false
    balanceLogList.value = []
    logTotal.value = 0
  })
}

/** 假别徽章映射（兼容字典编码与历史中文值） */
function leaveTypeClass(v) {
  const map = { '1': 'blue', '事假': 'blue', '2': 'amber', '病假': 'amber', '3': 'green', '年假': 'green', '4': 'violet', '婚假': 'violet', '5': 'violet', '产假': 'violet', '6': 'violet', '陪产假': 'violet', '7': 'gray', '丧假': 'gray', '8': 'red', '工伤假': 'red', '9': 'green', '调休': 'green' }
  return map[v] || 'gray'
}
function leaveTypeLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const map = { '1': '事假', '2': '病假', '3': '年假', '4': '婚假', '5': '产假', '6': '陪产假', '7': '丧假', '8': '工伤假', '9': '调休' }
  return map[v] || String(v)
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeNo: undefined, employeeName: undefined, deptName: undefined, leaveType: undefined, year: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    leaveType: [{ required: true, message: '假别不能为空', trigger: 'change' }],
    year: [{ required: true, message: '年度不能为空', trigger: 'change' }],
    totalDays: [{ required: true, message: '总额度不能为空', trigger: 'blur' }],
    usedDays: [{ required: true, message: '已休天数不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** ===== 人员弹框选择 ===== */
const employeePickerRef = ref(null)

function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
    f.employeeId = emp.employeeId;
    f.employeeNo = emp.employeeNo;
    f.employeeName = emp.employeeName;
    f.deptId = emp.orgId;
    f.deptName = emp.orgName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName };
  Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k]; });
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeNo = ''; form.value.employeeName = ''; form.value.deptId = null; form.value.deptName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.deptName) count++
  if (queryParams.value.leaveType) count++
  if (queryParams.value.year) count++
  return count
})

function getList() {
  loading.value = true
  listHrLeaveBalance(queryParams.value).then(response => {
    hrLeaveBalanceList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.employeeNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.deptName = undefined
  queryParams.value.leaveType = undefined
  queryParams.value.year = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.balanceId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeNo: undefined,
    employeeName: undefined,
    deptId: undefined,
    deptName: undefined,
    leaveType: undefined,
    year: undefined,
    totalDays: undefined,
    usedDays: undefined,
    remainingDays: undefined
  }
  proxy.resetForm('hrLeaveBalanceRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增假期余额'
}

function handleUpdate(row) {
  reset()
  const balanceId = row.balanceId || ids.value[0]
  getHrLeaveBalance(balanceId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改假期余额'
  })
}

/** 查看 —— 使用独立查看弹窗 */
const currentBalanceId = ref(null)
function handleView(row) {
  const balanceId = row.balanceId || ids.value[0]
  getHrLeaveBalance(balanceId).then(response => {
    viewData.value = response.data
    activeTab.value = 'info'
    balanceLogList.value = []
    logTotal.value = 0
    logQuery.pageNum = 1
    currentBalanceId.value = balanceId
    viewOpen.value = true
    // 自动加载扣减明细
    fetchBalanceLogs(balanceId)
  })
}

/** 剩余天数 = 总额度 - 已休天数（自动计算） */
const computedRemaining = computed(() => {
  const f = form.value
  if (f.totalDays == null || f.usedDays == null) return null
  return Math.round((Number(f.totalDays) - Number(f.usedDays)) * 10) / 10
})

function submitForm() {
  proxy.$refs['hrLeaveBalanceRef'].validate(valid => {
    if (valid) {
      if (computedRemaining.value != null) { form.value.remainingDays = computedRemaining.value }
      if (form.value.balanceId != undefined) {
        updateHrLeaveBalance(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrLeaveBalance(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const balanceIds = row.balanceId || ids.value
  proxy.$modal.confirm('是否确认删除选中的假期余额？').then(function() {
    return delHrLeaveBalance(balanceIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/leaveBalance/export', { ...queryParams.value }, `hrLeaveBalance_${new Date().getTime()}.xlsx`)
}

/** ===== 批量生成 ===== */
const batchOpen = ref(false)
const batchLoading = ref(false)
const batchRef = ref(null)
const deptPickerRef = ref(null)
const batchDeptNames = ref('')
const batchForm = reactive({
  year: new Date().getFullYear(),
  deptIds: [],
  deptNames: [],
  leaveTypeDaysList: [{ leaveType: '', totalDays: 0 }],
  overwrite: false
})
const batchRules = {
  year: [{ required: true, message: '请选择年度', trigger: 'change' }]
}

function handleBatchGenerate() {
  batchForm.year = new Date().getFullYear()
  batchForm.deptIds = []
  batchForm.deptNames = []
  batchDeptNames.value = ''
  batchForm.leaveTypeDaysList = [{ leaveType: '', totalDays: 0 }]
  batchForm.overwrite = false
  batchOpen.value = true
}

/** 打开部门多选弹窗 */
function openDeptPicker() {
  proxy.$refs.deptPickerRef.open(batchForm.deptIds.length > 0 ? batchForm.deptIds : null)
}

/** 部门多选确认回调 */
function onDeptPickerConfirm(deptList) {
  if (Array.isArray(deptList)) {
    batchForm.deptIds = deptList.map(d => d.deptId)
    batchForm.deptNames = deptList.map(d => d.deptName)
    batchDeptNames.value = deptList.map(d => d.deptName).join('、')
  }
}

/** 清除部门选择 */
function clearDeptSelection() {
  batchForm.deptIds = []
  batchForm.deptNames = []
  batchDeptNames.value = ''
}

function submitBatchGenerate() {
  // 校验假别配置
  const valid = batchForm.leaveTypeDaysList.filter(item => item.leaveType && item.totalDays > 0)
  if (valid.length === 0) {
    proxy.$modal.msgError('请至少配置一个假别及额度')
    return
  }
  const submitData = {
    year: batchForm.year,
    deptIds: batchForm.deptIds.length > 0 ? batchForm.deptIds : null,
    leaveTypeDaysList: valid,
    overwrite: batchForm.overwrite
  }
  batchLoading.value = true
  batchGenerateBalance(submitData).then(res => {
    proxy.$modal.msgSuccess(res.msg || '批量生成成功')
    batchOpen.value = false
    getList()
  }).catch(err => {
    proxy.$modal.msgError(err.msg || '批量生成失败')
  }).finally(() => {
    batchLoading.value = false
  })
}

/** ===== 批量导入 ===== */
const importDialogRef = ref(null)
const importTips = [
  '请先下载导入模板，按模板格式填写数据。',
  '工号、假别、年度、总额度为必填字段。',
  '导入时已休天数默认为0，剩余天数自动计算。',
  '勾选“覆盖已有记录”可更新已存在的假期余额数据。'
]
const importColumnLabels = {
  employeeNo: '工号',
  employeeName: '员工姓名',
  leaveType: '假别',
  year: '年度',
  totalDays: '总额度',
  usedDays: '已休天数',
  remainingDays: '剩余天数',
  result: '结果',
  errorReason: '失败原因'
}

function handleImport() {
  importDialogRef.value?.open()
}

function handleImportSuccess() {
  getList()
}

function cancel() {
  open.value = false
  reset()
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-leaveBalance-page {
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
.hr-leaveBalance-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-leaveBalance-page .filter-card { padding:14px 20px 16px; }
.hr-leaveBalance-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-leaveBalance-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-leaveBalance-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-leaveBalance-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-leaveBalance-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-leaveBalance-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-leaveBalance-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-leaveBalance-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-leaveBalance-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-leaveBalance-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-leaveBalance-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-leaveBalance-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-leaveBalance-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-leaveBalance-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-leaveBalance-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-leaveBalance-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-leaveBalance-page .field .control :deep(.el-select) { width:100%; }
.hr-leaveBalance-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-leaveBalance-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-leaveBalance-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-leaveBalance-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-leaveBalance-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-leaveBalance-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-leaveBalance-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-leaveBalance-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-leaveBalance-page .btn-soft .el-icon { font-size:14px; }
.hr-leaveBalance-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-leaveBalance-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-leaveBalance-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-leaveBalance-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-leaveBalance-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-leaveBalance-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }

/* ===== Table ===== */
.hr-leaveBalance-page .table-wrap { overflow-x:auto; }
.hr-leaveBalance-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-leaveBalance-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-leaveBalance-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-leaveBalance-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-leaveBalance-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-leaveBalance-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-leaveBalance-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-leaveBalance-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-leaveBalance-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-leaveBalance-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-leaveBalance-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-leaveBalance-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.hr-leaveBalance-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-leaveBalance-page .badge.amber .dot { background:var(--amber-500); }
.hr-leaveBalance-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-leaveBalance-page .badge.blue .dot { background:var(--blue-500); }
.hr-leaveBalance-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-leaveBalance-page .badge.green .dot { background:var(--green-500); }
.hr-leaveBalance-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-leaveBalance-page .badge.red .dot { background:var(--red-500); }
.hr-leaveBalance-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-leaveBalance-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-leaveBalance-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-leaveBalance-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-leaveBalance-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-leaveBalance-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-leaveBalance-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-leaveBalance-page .pagination-container :deep(.el-pagination .btn-prev), .hr-leaveBalance-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-leaveBalance-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-leaveBalance-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-leaveBalance-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式 ===== */
.hr-leaveBalance-page .rd-page { max-height:72vh; overflow-y:auto; }
.hr-leaveBalance-page .rd-card { border:1px solid #ebeef5; border-radius:8px; margin-bottom:16px; overflow:hidden; animation:rdFadeIn 0.4s ease-out forwards; }
.hr-leaveBalance-page .rd-card-header { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; background:#f5f7fa; cursor:pointer; user-select:none; }
.hr-leaveBalance-page .rd-card-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:#303133; }
.hr-leaveBalance-page .rd-card-icon { display:flex; align-items:center; color:#409eff; }
.hr-leaveBalance-page .rd-collapse-btn { border:none; background:transparent; cursor:pointer; padding:4px; display:flex; align-items:center; color:#909399; transition:transform 0.2s; }
.hr-leaveBalance-page .rd-collapse-btn.is-collapsed { transform:rotate(-90deg); }
.hr-leaveBalance-page .rd-card-body { padding:16px; }
.hr-leaveBalance-page .rd-grid { display:grid; grid-template-columns:repeat(2, 1fr); gap:14px 30px; }
.hr-leaveBalance-page .rd-item { display:flex; flex-direction:column; gap:4px; }
.hr-leaveBalance-page .rd-item--full { grid-column:1 / -1; }
.hr-leaveBalance-page .rd-label { font-size:12px; color:#909399; }
.hr-leaveBalance-page .rd-value { font-size:14px; color:#303133; font-weight:500; }
.hr-leaveBalance-page .rd-detail-header { display:flex; align-items:center; gap:12px; }
.hr-leaveBalance-page .rd-detail-header-icon { width:36px; height:36px; border-radius:8px; background:#ecf5ff; display:flex; align-items:center; justify-content:center; color:#409eff; }
.hr-leaveBalance-page .rd-detail-header-main { flex:1; }
.hr-leaveBalance-page .rd-detail-header-title { font-size:16px; font-weight:600; color:#303133; }
.hr-leaveBalance-page .rd-detail-header-sub { display:flex; align-items:center; gap:12px; margin-top:4px; }
.hr-leaveBalance-page .rd-detail-header-divider { width:1px; height:12px; background:#dcdfe6; }
.hr-leaveBalance-page .rd-detail-header-no { font-size:13px; color:#909399; }
@keyframes rdFadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-leaveBalance-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-leaveBalance-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-leaveBalance-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* 扣减明细页签 */
.balance-detail-tabs :deep(.el-tabs__header) { margin-bottom: 12px; }
.balance-detail-tabs :deep(.el-tabs__item) { font-size: 14px; font-weight: 600; }
.balance-detail-tabs .log-empty { padding: 20px 0; }
.balance-detail-tabs .log-pagination { margin-top: 8px; }
.balance-detail-tabs .log-pagination :deep(.el-pagination) { justify-content: center; }
.balance-detail-tabs .log-type-tag {
  display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 600;
}
.balance-detail-tabs .log-type-tag.deduct { color: #f56c6c; background: #fef0f0; border: 1px solid #fde2e2; }
.balance-detail-tabs .log-type-tag.refund { color: #67c23a; background: #f0f9eb; border: 1px solid #e1f3d8; }
.balance-detail-tabs .log-type-tag.adjust { color: #409eff; background: #ecf5ff; border: 1px solid #d9ecff; }
.balance-detail-tabs .log-type-tag.freeze { color: #e6a23c; background: #fdf6ec; border: 1px solid #faecd8; }
.balance-detail-tabs .log-type-tag.unfreeze { color: #909399; background: #f4f4f5; border: 1px solid #e9e9eb; }

/* ===== 批量生成弹窗样式 ===== */
.batch-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.batch-row .batch-unit { color: #909399; font-size: 13px; }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }

/* ===== 导入弹窗样式由 ExcelImportDialog 组件提供 ===== */
</style>
