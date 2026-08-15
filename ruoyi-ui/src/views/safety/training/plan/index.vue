<template>
  <div class="app-container safety-training-plan-page">
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
          <label>计划名称</label>
          <div class="control">
            <el-input v-model="queryParams.planName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>计划编号</label>
          <div class="control">
            <el-input v-model="queryParams.planCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>计划类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.planType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_training_plan_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>培训年份</label>
          <div class="control">
            <el-date-picker v-model="queryParams.planYear" type="year" placeholder="选择年份" value-format="YYYY" style="width: 100%" @change="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>计划状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.planStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option label="待执行" value="0" />
              <el-option label="执行中" value="1" />
              <el-option label="已完成" value="2" />
              <el-option label="已取消" value="3" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control is-select">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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

    <!-- ===== Status Tabs ===== -->
    <div class="surface status-tabs">
      <div class="tab" :class="{ active: queryParams.planStatus === undefined }" @click="handleStatusTab(undefined)">
        <span class="tab-label">全部</span>
        <span class="tab-count">{{ statusCounts.all }}</span>
      </div>
      <div class="tab" :class="{ active: queryParams.planStatus === '0' }" @click="handleStatusTab('0')">
        <span class="dot amber"></span>
        <span class="tab-label">待执行</span>
        <span class="tab-count">{{ statusCounts.pending }}</span>
      </div>
      <div class="tab" :class="{ active: queryParams.planStatus === '1' }" @click="handleStatusTab('1')">
        <span class="dot blue"></span>
        <span class="tab-label">执行中</span>
        <span class="tab-count">{{ statusCounts.executing }}</span>
      </div>
      <div class="tab" :class="{ active: queryParams.planStatus === '2' }" @click="handleStatusTab('2')">
        <span class="dot green"></span>
        <span class="tab-label">已完成</span>
        <span class="tab-count">{{ statusCounts.completed }}</span>
      </div>
      <div class="tab" :class="{ active: queryParams.planStatus === '3' }" @click="handleStatusTab('3')">
        <span class="dot" style="background: var(--ink-400);"></span>
        <span class="tab-label">已取消</span>
        <span class="tab-count">{{ statusCounts.cancelled }}</span>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:training:plan:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:training:plan:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:training:plan:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_training_plan_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="planList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="计划编号" prop="planCode" key="planCode" :width="colWidth('planCode', 156)" resizable v-if="columns.planCode.visible" />
          <el-table-column label="计划名称" prop="planName" key="planName" :width="colWidth('planName', 240)" resizable show-overflow-tooltip v-if="columns.planName.visible" />
          <el-table-column label="计划类型" prop="planType" key="planType" :width="colWidth('planType', 132)" resizable align="center" v-if="columns.planType.visible">
            <template #default="scope">
              <span class="badge violet">{{ planTypeLabel(scope.row.planType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="年度" prop="planYear" key="planYear" :width="colWidth('planYear', 96)" resizable align="center" v-if="columns.planYear.visible" />
          <el-table-column label="培训对象" prop="targetPersons" key="targetPersons" :width="colWidth('targetPersons', 180)" resizable v-if="columns.targetPersons.visible">
            <template #default="scope">
              <el-popover trigger="hover" placement="top" :width="260" :show-after="300" :disabled="!parseTargetPersonList(scope.row.targetPersons).length">
                <template #reference>
                  <span class="target-persons-cell">{{ formatTargetPersons(scope.row.targetPersons) }}</span>
                </template>
                <div class="persons-popover">
                  <div class="persons-popover-header">培训对象（{{ parseTargetPersonList(scope.row.targetPersons).length }} 人）</div>
                  <div class="persons-popover-list">
                    <div v-for="(person, idx) in parseTargetPersonList(scope.row.targetPersons)" :key="idx" class="persons-popover-item">
                      <span class="person-avatar">{{ (person.nickName || person.userName || '?').charAt(0) }}</span>
                      <div class="person-info">
                        <span class="person-name">{{ person.nickName || person.userName }}</span>
                        <span class="person-dept" v-if="person.deptName">{{ person.deptName }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column label="总课时" prop="totalHours" key="totalHours" :width="colWidth('totalHours', 96)" resizable align="center" v-if="columns.totalHours.visible" />
          <el-table-column label="培训进度" key="progress" :width="colWidth('progress', 216)" resizable align="center" v-if="columns.progress.visible">
            <template #default="scope">
              <div class="progress-cell">
                <span class="progress-stat" v-if="scope.row.recordCount > 0">
                  <el-button link type="primary" @click="handleViewRecords(scope.row)">{{ scope.row.recordCount }} 场</el-button>
                  <span class="progress-divider">/</span>
                  <span class="progress-people">{{ scope.row.attendeeCount || 0 }} 人</span>
                  <span class="progress-pass" v-if="scope.row.passRate != null">合格率 {{ scope.row.passRate }}%</span>
                </span>
                <span v-else class="progress-empty">未开始</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="计划状态" prop="planStatus" key="planStatus" :width="colWidth('planStatus', 120)" resizable align="center" v-if="columns.planStatus.visible">
            <template #default="scope">
              <span class="badge" :class="planStatusBadgeClass(scope.row.planStatus)">
                <span class="dot"></span>{{ planStatusLabel(scope.row.planStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="384" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:training:plan:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.planStatus === '0' || scope.row.planStatus === '3'" v-hasPermi="['safety:training:plan:edit']">修改</el-button>
              <el-button link type="primary" icon="Document" @click="handleViewRecords(scope.row)" v-if="scope.row.planStatus === '1' || scope.row.planStatus === '2'">培训记录</el-button>
              <el-button link type="success" @click="handleStart(scope.row)" v-if="scope.row.planStatus === '0'" v-hasPermi="['safety:training:plan:edit']">开始执行</el-button>
              <el-button link type="success" @click="handleComplete(scope.row)" v-if="scope.row.planStatus === '1'" v-hasPermi="['safety:training:plan:edit']">完成</el-button>
              <el-button link type="warning" @click="handleCancel(scope.row)" v-if="scope.row.planStatus === '0' || scope.row.planStatus === '1'" v-hasPermi="['safety:training:plan:edit']">取消</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.planStatus === '0' || scope.row.planStatus === '3'" v-hasPermi="['safety:training:plan:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="planRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="计划编号" prop="planCode"><el-input v-model="form.planCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计划名称" prop="planName"><el-input v-model="form.planName" placeholder="请输入计划名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="计划类型" prop="planType"><el-select v-model="form.planType" placeholder="请选择计划类型" clearable style="width: 100%"><el-option v-for="dict in safety_training_plan_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="年度" prop="planYear"><el-date-picker v-model="form.planYear" type="year" placeholder="请选择年度" value-format="YYYY" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>培训详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="培训对象" prop="targetPersons">
                <el-popover trigger="hover" placement="top" :width="260" :show-after="300" :disabled="!parseTargetPersonList(form.targetPersons).length">
                  <template #reference>
                    <el-input :model-value="targetPersonDisplay" readonly placeholder="请选择培训对象（可多选）" style="width: 100%" @click="openTargetUserPicker">
                      <template #append>
                        <el-button icon="Search" @click="openTargetUserPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.targetUserIds && form.targetUserIds.length" class="clear-icon" @click.stop="clearTargetPersons"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </template>
                  <div class="persons-popover">
                    <div class="persons-popover-header">培训对象（{{ parseTargetPersonList(form.targetPersons).length }} 人）</div>
                    <div class="persons-popover-list">
                      <div v-for="(person, idx) in parseTargetPersonList(form.targetPersons)" :key="idx" class="persons-popover-item">
                        <span class="person-avatar">{{ (person.nickName || person.userName || '?').charAt(0) }}</span>
                        <div class="person-info">
                          <span class="person-name">{{ person.nickName || person.userName }}</span>
                          <span class="person-dept" v-if="person.deptName">{{ person.deptName }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-popover>
              </el-form-item>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="总课时" prop="totalHours"><el-input-number v-model="form.totalHours" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计划状态" prop="planStatus"><el-select v-model="form.planStatus" disabled style="width: 100%"><el-option label="待执行" value="0" /><el-option label="执行中" value="1" /><el-option label="已完成" value="2" /><el-option label="已取消" value="3" /></el-select></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card" v-if="form.planId && (form.planStatus === '1' || form.planStatus === '2')">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="13" x2="15" y2="13"/><line x1="9" y1="17" x2="15" y2="17"/></svg></span>关联培训记录<span class="records-count-badge" v-if="planRecords.length > 0">{{ planRecords.length }} 场</span></div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <div v-loading="planRecordsLoading" class="plan-records-wrap">
                <div v-if="planRecords.length === 0" class="plan-records-empty">
                  <div class="empty-icon"><svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
                  <div class="empty-text">暂无关联培训记录</div>
                  <div class="empty-hint">请在「培训记录」页面新增记录并关联本计划</div>
                </div>
                <div v-else class="record-cards">
                  <div class="record-card-item" v-for="record in planRecords" :key="record.recordId">
                    <div class="record-card-left">
                      <div class="record-card-name">{{ record.courseName }}</div>
                      <div class="record-card-meta">
                        <span class="meta-chip"><el-icon><Calendar /></el-icon>{{ record.trainingDate || '—' }}</span>
                        <span class="meta-chip" v-if="record.hours"><el-icon><Clock /></el-icon>{{ record.hours }} 课时</span>
                        <span class="meta-chip" v-if="record.trainingLocation"><el-icon><Location /></el-icon>{{ record.trainingLocation }}</span>
                        <span class="meta-chip" v-if="record.trainer"><el-icon><User /></el-icon>{{ record.trainer }}</span>
                      </div>
                    </div>
                    <div class="record-card-right">
                      <div class="attendee-stat">
                        <span class="attendee-num">{{ record.attendeeCount || 0 }}</span>
                        <span class="attendee-label">人参训</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">培训计划详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.planCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.planCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">计划编号</span><div class="rd-value">{{ viewData.planCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划名称</span><div class="rd-value">{{ viewData.planName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划类型</span><div class="rd-value"><dict-tag :options="safety_training_plan_type" :value="viewData.planType" /></div></div>
              <div class="rd-item"><span class="rd-label">年度</span><div class="rd-value">{{ viewData.planYear || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划状态</span><div class="rd-value"><span class="badge" :class="planStatusBadgeClass(viewData.planStatus)"><span class="dot"></span>{{ planStatusLabel(viewData.planStatus) }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.targetPersons || viewData.totalHours != null || viewData.planStatus">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>培训详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">培训对象</span><div class="rd-value">{{ formatTargetPersons(viewData.targetPersons) }}</div></div>
              <div class="rd-item"><span class="rd-label">总课时</span><div class="rd-value">{{ viewData.totalHours != null ? viewData.totalHours + ' 学时' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">培训进度</span><div class="rd-value"><span v-if="viewData.recordCount > 0">{{ viewData.recordCount }} 场 / {{ viewData.attendeeCount || 0 }} 人<span v-if="viewData.passRate != null" style="margin-left:8px;color:var(--green-700)">合格率 {{ viewData.passRate }}%</span></span><span v-else style="color:var(--ink-400)">未开始</span></div></div>
            </div>
            <div class="rd-grid" style="margin-top:12px" v-if="parseTargetPersonList(viewData.targetPersons).length > 0">
              <div class="rd-item rd-item--full"><span class="rd-label">参训人员明细</span><div class="rd-value"><div class="view-persons-list"><div class="view-person-item" v-for="(person, idx) in parseTargetPersonList(viewData.targetPersons)" :key="idx"><span class="person-avatar">{{ (person.nickName || person.userName || '?').charAt(0) }}</span><div class="person-info"><span class="person-name">{{ person.nickName || person.userName }}</span><span class="person-dept" v-if="person.deptName">{{ person.deptName }}</span></div></div></div></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="planRecords.length > 0">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="13" x2="15" y2="13"/><line x1="9" y1="17" x2="15" y2="17"/></svg></span>关联培训记录<span class="records-count-badge">{{ planRecords.length }} 场</span></div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <div v-loading="planRecordsLoading" class="plan-records-wrap">
              <div class="record-cards">
                <div class="record-card-item" v-for="record in planRecords" :key="record.recordId">
                  <div class="record-card-left">
                    <div class="record-card-name">{{ record.courseName }}</div>
                    <div class="record-card-meta">
                      <span class="meta-chip"><el-icon><Calendar /></el-icon>{{ record.trainingDate || '—' }}</span>
                      <span class="meta-chip" v-if="record.hours"><el-icon><Clock /></el-icon>{{ record.hours }} 课时</span>
                      <span class="meta-chip" v-if="record.trainingLocation"><el-icon><Location /></el-icon>{{ record.trainingLocation }}</span>
                      <span class="meta-chip" v-if="record.trainer"><el-icon><User /></el-icon>{{ record.trainer }}</span>
                    </div>
                  </div>
                  <div class="record-card-right">
                    <div class="attendee-stat">
                      <span class="attendee-num">{{ record.attendeeCount || 0 }}</span>
                      <span class="attendee-label">人参训</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>


    <!-- 培训对象选择弹框 -->
    <user-picker ref="targetUserPickerRef" title="选择培训对象" multiple @confirm="onTargetUserPickerConfirm" />
  </div>
</template>

<script setup name="SafetyTrainingPlan">
import { listTrainingPlan, getTrainingPlan, addTrainingPlan, updateTrainingPlan, delTrainingPlan, startTrainingPlan, completeTrainingPlan, cancelTrainingPlan } from '@/api/safety/trainingPlan'
import { listTrainingRecord } from '@/api/safety/trainingRecord'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { useRouter } from 'vue-router'
import UserPicker from '@/components/UserPicker/index.vue'
import { Search, Filter, RefreshLeft, CircleClose, Calendar, Clock, Location, User, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()

const { proxy } = getCurrentInstance()
const { safety_training_plan_type } = proxy.useDict('safety_training_plan_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_training_plan_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3"])

const planList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref({})
const planRecords = ref([])
const planRecordsLoading = ref(false)
const targetUserPickerRef = ref()
const dateRange = ref([])

const default_columns = {
  planCode: { label: '计划编号', visible: true },
  planName: { label: '计划名称', visible: true },
  planType: { label: '计划类型', visible: true },
  planYear: { label: '年度', visible: true },
  targetPersons: { label: '培训对象', visible: true },
  totalHours: { label: '总课时', visible: true },
  progress: { label: '培训进度', visible: true },
  planStatus: { label: '计划状态', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_training_plan_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(default_columns).forEach(key => {
        result[key] = { label: default_columns[key].label, visible: parsed[key] !== undefined ? parsed[key] : default_columns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...default_columns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, planName: undefined, planCode: undefined, planType: undefined, planStatus: undefined, planYear: undefined, params: {} },
  rules: {
    planName: [{ required: true, message: '计划名称不能为空', trigger: 'blur' }],
    planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
    targetPersons: [{ required: true, message: '请选择培训对象', trigger: 'change' }],
    totalHours: [{ required: true, message: '请输入总课时', trigger: 'blur' }],
    planYear: [{ required: true, message: '请选择年度', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 培训对象显示文本 */
const targetPersonDisplay = computed(() => {
  const tp = form.value.targetPersons
  if (!tp) return ''
  try {
    const parsed = JSON.parse(tp)
    if (Array.isArray(parsed)) {
      const names = parsed.map(u => u.nickName || u.userName || '').filter(Boolean)
      if (names.length === 0) return ''
      return names.length > 3 ? names.slice(0, 3).join('、') + ` 等${names.length}人` : names.join('、')
    }
  } catch (e) {}
  return tp
})

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.planName) count++
  if (queryParams.value.planCode) count++
  if (queryParams.value.planType) count++
  if (queryParams.value.planYear) count++
  if (queryParams.value.planStatus) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

const statusCounts = computed(() => {
  const all = planList.value.length
  const pending = planList.value.filter(i => i.planStatus === '0').length
  const executing = planList.value.filter(i => i.planStatus === '1').length
  const completed = planList.value.filter(i => i.planStatus === '2').length
  const cancelled = planList.value.filter(i => i.planStatus === '3').length
  return { all, pending, executing, completed, cancelled }
})

function getList() {
  loading.value = true
  proxy.addDateRange(queryParams.value, dateRange.value)
  listTrainingPlan(queryParams.value).then(response => { planList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.planName = undefined; queryParams.value.planCode = undefined; queryParams.value.planType = undefined; queryParams.value.planStatus = undefined; queryParams.value.planYear = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleStatusTab(status) { queryParams.value.planStatus = status; queryParams.value.pageNum = 1; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.planId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); planRecords.value = []; collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; collapsedCards.c3 = false; open.value = true; title.value = '添加培训计划' }
function handleUpdate(row) { reset(); planRecords.value = []; getTrainingPlan(row.planId || ids.value[0]).then(response => { form.value = response.data; form.value.targetUserIds = parseTargetUserIds(form.value.targetPersons); collapsedCards.c1 = !form.value.targetUserIds?.length && !form.value.totalHours; collapsedCards.c2 = !form.value.remark; collapsedCards.c3 = false; open.value = true; title.value = '修改培训计划'; loadPlanRecords(row.planId) }) }
function handleView(row) { const id = row.planId || ids.value[0]; getTrainingPlan(id).then(response => { viewData.value = response.data; viewOpen.value = true; loadPlanRecords(id) }) }
function loadPlanRecords(planId) {
  planRecordsLoading.value = true
  listTrainingRecord({ planId: planId, pageSize: 100 }).then(response => {
    planRecords.value = response.rows || []
    planRecordsLoading.value = false
  }).catch(() => { planRecordsLoading.value = false })
}
function handleViewRecords(row) {
  router.push({ path: '/safety/training/record', query: { planId: row.planId, planName: row.planName } })
}
function submitForm() {
  proxy.$refs['planRef'].validate(valid => {
    if (valid) {
      if (form.value.planId != undefined) { updateTrainingPlan(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addTrainingPlan(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const planIds = row.planId || ids.value; proxy.$modal.confirm('是否确认删除培训计划？').then(function() { return delTrainingPlan(planIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/training/plan/export', { ...queryParams.value }, `training_plan_${new Date().getTime()}.xlsx`) }
function handleStart(row) { proxy.$modal.confirm('确认开始执行该培训计划吗？').then(function() { return startTrainingPlan(row.planId) }).then(() => { proxy.$modal.msgSuccess('已开始执行'); getList() }).catch(() => {}) }
function handleComplete(row) {
  const recordCount = row.recordCount || 0
  const attendeeCount = row.attendeeCount || 0
  let confirmMsg = '确认将该培训计划标记为已完成吗？'
  if (recordCount === 0) {
    confirmMsg = '该计划尚未关联任何培训记录，完成操作将失败。\n\n请先在「执行中」状态下新增培训记录并完成人员登记后再标记完成。\n\n是否继续？'
  } else if (attendeeCount === 0) {
    confirmMsg = '该计划的培训记录尚无参训人员，完成操作将失败。\n\n请先完成人员登记后再标记完成。\n\n是否继续？'
  } else {
    confirmMsg = `该计划已开展 ${recordCount} 场培训，累计 ${attendeeCount} 人参训` + (row.passRate != null ? `，合格率 ${row.passRate}%` : '') + '。\n\n确认标记为已完成吗？'
  }
  proxy.$modal.confirm(confirmMsg).then(function() { return completeTrainingPlan(row.planId) }).then(() => { proxy.$modal.msgSuccess('已完成'); getList() }).catch(() => {})
}
function handleCancel(row) { proxy.$modal.confirm('确认取消该培训计划吗？取消后将无法恢复。').then(function() { return cancelTrainingPlan(row.planId) }).then(() => { proxy.$modal.msgSuccess('已取消'); getList() }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { planId: undefined, planCode: undefined, planName: undefined, planType: undefined, planYear: undefined, targetPersons: undefined, targetUserIds: [], totalHours: undefined, planStatus: '0', remark: undefined }
  proxy.resetForm('planRef')
}

/** 解析 targetPersons JSON 字符串为用户ID数组 */
function parseTargetUserIds(targetPersons) {
  if (!targetPersons) return []
  try {
    const parsed = JSON.parse(targetPersons)
    if (Array.isArray(parsed)) return parsed.map(u => u.userId)
    return []
  } catch (e) {
    // 兼容旧格式（逗号分隔的文本）
    return []
  }
}

/** 解析 targetPersons JSON 字符串为人员列表 */
function parseTargetPersonList(targetPersons) {
  if (!targetPersons) return []
  try {
    const parsed = JSON.parse(targetPersons)
    if (Array.isArray(parsed)) return parsed
    return []
  } catch (e) {
    return []
  }
}

/** 打开培训对象选择弹窗 */
function openTargetUserPicker() {
  proxy.$refs.targetUserPickerRef.open(form.value.targetUserIds || [])
}

/** 培训对象选择确认回调 */
function onTargetUserPickerConfirm(users) {
  form.value.targetUserIds = users.map(u => u.userId)
  form.value.targetPersons = users.length > 0
    ? JSON.stringify(users.map(u => ({ userId: u.userId, nickName: u.nickName, deptName: u.deptName || '' })))
    : null
  proxy.$refs['planRef']?.validateField('targetPersons')
}

/** 清除培训对象 */
function clearTargetPersons() {
  form.value.targetUserIds = []
  form.value.targetPersons = null
  proxy.$refs['planRef']?.validateField('targetPersons')
}

/** 格式化表格列中的培训对象显示 */
function formatTargetPersons(targetPersons) {
  if (!targetPersons) return '-'
  try {
    const parsed = JSON.parse(targetPersons)
    if (Array.isArray(parsed)) {
      const names = parsed.map(u => u.nickName || u.userName || '')
      return names.length > 3 ? names.slice(0, 3).join('、') + ` 等${names.length}人` : names.join('、')
    }
  } catch (e) {}
  // 兼容旧格式（纯文本）
  return targetPersons
}

function planStatusLabel(status) { const map = { '0': '待执行', '1': '执行中', '2': '已完成', '3': '已取消' }; return map[status] || '-' }
function planStatusBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'gray' }; return map[status] || 'gray' }
function planTypeLabel(type) { const item = safety_training_plan_type.value.find(d => d.value == type); return item ? item.label : '-' }

getList()
</script>

<style scoped>
.safety-training-plan-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-training-plan-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-training-plan-page .filter-card { padding:14px 20px 16px; }
.safety-training-plan-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-training-plan-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-training-plan-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-training-plan-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-training-plan-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-training-plan-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-training-plan-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-training-plan-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-training-plan-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-training-plan-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-training-plan-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-training-plan-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-training-plan-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-training-plan-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-training-plan-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-training-plan-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-training-plan-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-training-plan-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-training-plan-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-training-plan-page .field .control.is-select { padding:0; }
.safety-training-plan-page .field .control.is-select :deep(.el-select) { width:100%; }
.safety-training-plan-page .field .control.is-select :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0 12px; min-height:34px; height:34px; }
.safety-training-plan-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-training-plan-page .status-tabs { display:flex; gap:0; padding:0 20px; align-items:center; }
.safety-training-plan-page .status-tabs .tab { display:flex; align-items:center; gap:6px; padding:14px 16px; cursor:pointer; border-bottom:2px solid transparent; transition:all .15s var(--ease-out); font-size:14px; color:var(--ink-500); }
.safety-training-plan-page .status-tabs .tab:hover { color:var(--ink-700); }
.safety-training-plan-page .status-tabs .tab.active { color:var(--brand-600); border-bottom-color:var(--brand-600); font-weight:600; }
.safety-training-plan-page .status-tabs .tab .dot { width:8px; height:8px; border-radius:50%; }
.safety-training-plan-page .status-tabs .tab .dot.amber { background:var(--amber-500); }
.safety-training-plan-page .status-tabs .tab .dot.blue { background:var(--blue-500); }
.safety-training-plan-page .status-tabs .tab .dot.green { background:var(--green-500); }
.safety-training-plan-page .status-tabs .tab .tab-count { font-size:12px; color:var(--ink-400); background:var(--ink-100); padding:2px 8px; border-radius:999px; font-weight:600; }
.safety-training-plan-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-training-plan-page .toolbar .left, .safety-training-plan-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-training-plan-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-training-plan-page .table-wrap { overflow-x:auto; }
.safety-training-plan-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-training-plan-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-training-plan-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-training-plan-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-training-plan-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-training-plan-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-training-plan-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-training-plan-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-training-plan-page .badge.amber .dot { background:var(--amber-500); }
.safety-training-plan-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-training-plan-page .badge.blue .dot { background:var(--blue-500); }
.safety-training-plan-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-training-plan-page .badge.green .dot { background:var(--green-500); }
.safety-training-plan-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-training-plan-page .badge.gray .dot { background:var(--ink-400); }
.safety-training-plan-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-training-plan-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-training-plan-page .filter-card .filter-bar { grid-template-columns:1fr; } .safety-training-plan-page .status-tabs { overflow-x:auto; } }
.safety-training-plan-page .progress-cell { display:flex; align-items:center; gap:4px; font-size:13px; }
.safety-training-plan-page .progress-cell .progress-divider { color:var(--ink-300); }
.safety-training-plan-page .progress-cell .progress-people { color:var(--ink-500); }
.safety-training-plan-page .progress-cell .progress-pass { color:var(--green-700); margin-left:6px; font-size:12px; }
.safety-training-plan-page .progress-cell .progress-empty { color:var(--ink-400); font-size:13px; }
/* 关联培训记录卡片样式 — 硬编码颜色，兼容 append-to-body dialog 场景 */
.records-count-badge { display:inline-flex; align-items:center; padding:1px 8px; margin-left:8px; border-radius:999px; background:#eef2ff; color:#4f46e5; font-size:12px; font-weight:600; line-height:1.4; }
.plan-records-wrap { min-height:80px; }
.plan-records-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:32px 0; }
.plan-records-empty .empty-icon { color:#cbd5e1; margin-bottom:8px; }
.plan-records-empty .empty-text { font-size:14px; color:#64748b; font-weight:500; }
.plan-records-empty .empty-hint { font-size:12px; color:#94a3b8; margin-top:4px; }
.record-cards { display:flex; flex-direction:column; gap:8px; }
.record-card-item { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; background:#fff; border:1px solid #e2e8f0; border-radius:8px; transition:all .15s ease; }
.record-card-item:hover { border-color:#c7d2fe; background:#fafbff; box-shadow:0 1px 3px rgba(99,102,241,.08); }
.record-card-left { flex:1; min-width:0; }
.record-card-name { font-size:14px; font-weight:600; color:#0f172a; margin-bottom:6px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.record-card-meta { display:flex; flex-wrap:wrap; gap:4px 14px; }
.meta-chip { display:inline-flex; align-items:center; gap:4px; font-size:12px; color:#64748b; }
.meta-chip .el-icon { font-size:13px; color:#94a3b8; }
.record-card-right { flex-shrink:0; margin-left:16px; }
.attendee-stat { display:flex; flex-direction:column; align-items:center; padding:6px 16px; background:#eff6ff; border-radius:8px; min-width:64px; }
.attendee-num { font-size:20px; font-weight:700; color:#1d4ed8; line-height:1; }
.attendee-label { font-size:11px; color:#94a3b8; margin-top:3px; }
.clear-icon { cursor:pointer; color:#c0c4cc; font-size:14px; }
.clear-icon:hover { color:#909399; }
/* 培训对象 popover 样式 */
.target-persons-cell { display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; cursor:default; }
.persons-popover { max-height:320px; overflow-y:auto; }
.persons-popover-header { font-size:13px; font-weight:600; color:#0f172a; padding-bottom:8px; margin-bottom:8px; border-bottom:1px solid #e2e8f0; }
.persons-popover-list { display:flex; flex-direction:column; gap:6px; }
.persons-popover-item { display:flex; align-items:center; gap:8px; padding:4px 0; }
.person-avatar { flex-shrink:0; width:28px; height:28px; border-radius:50%; background:#eef2ff; color:#4f46e5; font-size:12px; font-weight:600; display:inline-flex; align-items:center; justify-content:center; }
.person-info { display:flex; flex-direction:column; gap:1px; min-width:0; }
.person-name { font-size:13px; font-weight:500; color:#0f172a; }
.person-dept { font-size:12px; color:#94a3b8; }
/* 查看弹窗中参训人员列表 */
.view-persons-list { display:flex; flex-wrap:wrap; gap:8px; margin-top:4px; }
.view-person-item { display:flex; align-items:center; gap:8px; padding:6px 12px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; }
</style>
