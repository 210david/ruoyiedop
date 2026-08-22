<template>
  <div class="app-container mms-schedule-gantt-page">
    <!-- ===== 顶部工具栏 ===== -->
    <div class="surface gantt-toolbar">
      <div class="toolbar-left">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 280px" @change="handleDateChange" />
        <el-button type="primary" plain icon="Search" @click="loadGanttData">刷新</el-button>
        <div class="toolbar-divider"></div>
        <el-radio-group v-model="viewMode" size="small" @change="handleViewModeChange">
          <el-radio-button label="day">日视图</el-radio-button>
          <el-radio-button label="week">周视图</el-radio-button>
        </el-radio-group>
        <div class="toolbar-divider"></div>
        <el-checkbox v-model="showCancelled" size="small" @change="renderGantt">显示已取消</el-checkbox>
      </div>
      <div class="toolbar-right">
        <el-button icon="ArrowLeft" @click="navigateDate(-1)">{{ viewMode === 'day' ? '前一天' : '前一周' }}</el-button>
        <el-button icon="ArrowRight" @click="navigateDate(1)">{{ viewMode === 'day' ? '后一天' : '后一周' }}</el-button>
        <el-button icon="RefreshLeft" @click="resetDateRange">{{ viewMode === 'day' ? '今天' : '本周' }}</el-button>
        <div class="toolbar-divider"></div>
        <el-button :icon="isFullscreen ? 'Aim' : 'FullScreen'" @click="toggleFullscreen">{{ isFullscreen ? '退出全屏' : '全屏' }}</el-button>
        <el-button icon="QuestionFilled" @click="helpVisible = true">操作说明</el-button>
      </div>
    </div>

    <div class="gantt-body">
      <!-- ===== 待排产工单池（左侧栏，可折叠） ===== -->
      <div class="surface gantt-sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
        <!-- 折叠态：窄边条 -->
        <template v-if="sidebarCollapsed">
          <div class="sidebar-collapsed-bar" @click="toggleSidebar">
            <el-icon><Tickets /></el-icon>
            <el-badge :value="unscheduledOrders.length" type="primary" :max="99" />
            <span class="sidebar-collapsed-text">展开</span>
          </div>
        </template>
        <!-- 展开态：完整工单池 -->
        <template v-else>
          <div class="sidebar-header">
            <el-icon><Tickets /></el-icon>
            <span>待排产工单</span>
            <el-badge :value="unscheduledOrders.length" type="primary" />
            <el-button class="sidebar-collapse-btn" icon="Fold" text size="small" @click="toggleSidebar" />
          </div>
          <div class="sidebar-search">
            <el-input v-model="woSearch" placeholder="搜索工单号/产品" clearable size="small" prefix-icon="Search" @input="filterUnscheduled" />
          </div>
          <div class="sidebar-list" v-loading="sidebarLoading">
            <div v-for="wo in pagedUnscheduled" :key="wo.workOrderId"
                 class="wo-card"
                 :class="{ 'wo-card-priority-high': wo.priority == '0' }"
                 draggable="true"
                 @dragstart="onDragStart($event, wo)"
                 @dragend="onDragEnd">
              <div class="wo-card-header">
                <span class="wo-no">{{ wo.workOrderNo }}</span>
                <el-tag size="small" :type="wo.priority == '0' ? 'danger' : wo.priority == '1' ? 'warning' : 'info'" effect="dark">{{ wo.priority == '0' ? '高' : wo.priority == '1' ? '中' : '低' }}</el-tag>
              </div>
              <div class="wo-card-body">
                <div class="wo-product">{{ wo.productName }}</div>
                <div class="wo-meta">
                  <span>{{ wo.productCode }}</span>
                  <span>{{ wo.planQty }} {{ unitLabel(wo.unit) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-if="filteredUnscheduled.length === 0" description="无待排产工单" :image-size="60" />
          </div>
          <div class="sidebar-pagination" v-if="filteredUnscheduled.length > 0">
            <el-pagination
              v-model:current-page="woCurrentPage"
              v-model:page-size="woPageSize"
              :page-sizes="[10, 20, 50]"
              :total="filteredUnscheduled.length"
              layout="total, prev, next"
              size="small"
              background
              @current-change="handleWoPageChange"
            />
          </div>
        </template>
      </div>

      <!-- ===== 甘特图主区域 ===== -->
      <div class="surface gantt-main">
        <!-- 拖拽时间指示线（HTML 浮层，放在 ganttChartRef 外部避免被 ECharts 初始化清空） -->
        <div v-show="dragIndicator.visible" class="drag-indicator" :style="{ left: dragIndicator.x + 'px' }">
          <div class="drag-indicator-label" :style="{ top: dragIndicator.labelY + 'px' }">
            <span class="drag-indicator-time" :class="{ 'is-invalid': !dragIndicator.isValidRow }">{{ dragIndicator.timeLabel }}</span>
            <span v-if="dragIndicator.resourceName" class="drag-indicator-resource">{{ dragIndicator.resourceName }}</span>
          </div>
          <div class="drag-indicator-line" :class="{ 'is-invalid': !dragIndicator.isValidRow }"></div>
        </div>
        <!-- 任务条悬浮提示（HTML 浮层） -->
        <div v-show="taskTooltip.visible" class="task-tooltip" :style="{ left: taskTooltip.x + 'px', top: taskTooltip.y + 'px' }">
          <div class="task-tooltip-title">{{ taskTooltip.workOrderNo }}</div>
          <div class="task-tooltip-row">产品：{{ taskTooltip.productName }}</div>
          <div class="task-tooltip-row">产能单元：{{ taskTooltip.resourceName }}</div>
          <div class="task-tooltip-row">开始：{{ taskTooltip.planStart }}</div>
          <div class="task-tooltip-row">结束：{{ taskTooltip.planEnd }}</div>
          <div class="task-tooltip-row">状态：{{ taskTooltip.statusLabel }}</div>
        </div>
        <div ref="ganttChartRef" class="gantt-chart" v-loading="ganttLoading"></div>
        <div class="gantt-legend">
          <span class="legend-item"><span class="dot dot-released"></span>已下达</span>
          <span v-if="showCancelled" class="legend-item"><span class="dot dot-cancelled"></span>已取消</span>
          <span class="legend-item"><span class="dot dot-conflict"></span>冲突</span>
          <span class="legend-tip"><el-icon><InfoFilled /></el-icon> 拖拽工单到甘特图可创建排产；点击任务条可查看详情</span>
        </div>
      </div>
    </div>

    <!-- ===== 拖拽排产编辑弹窗 ===== -->
    <el-dialog v-model="scheduleDialogVisible" width="720px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">创建排产任务</div>
            <div class="rd-detail-header-sub" v-if="scheduleForm">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">{{ scheduleForm.workOrderNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="scheduleForm">
        <!-- 卡片1：工单信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('sc_wo')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              </span>
              工单信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc_wo }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.sc_wo">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>工单编号</span>
                <div class="rd-value">{{ scheduleForm.workOrderNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>工单类型</span>
                <div class="rd-value">{{ woTypeLabel(scheduleForm.orderType) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>产品编码</span>
                <div class="rd-value">{{ scheduleForm.productCode || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>产品名称</span>
                <div class="rd-value">{{ scheduleForm.productName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>规格型号</span>
                <div class="rd-value">{{ scheduleForm.specModel || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>计划数量</span>
                <div class="rd-value">{{ scheduleForm.planQty }} {{ unitLabel(scheduleForm.unit) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>BOM编号</span>
                <div class="rd-value">{{ scheduleForm.bomNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>工艺路线</span>
                <div class="rd-value">{{ scheduleForm.routeNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>优先级</span>
                <div class="rd-value">
                  <el-tag :type="scheduleForm.priority == '0' ? 'danger' : scheduleForm.priority == '1' ? 'warning' : 'info'" size="small">
                    {{ priorityLabel(scheduleForm.priority) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 卡片2：排产信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('sc_plan')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
              </span>
              排产信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc_plan }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.sc_plan">
            <el-form :model="scheduleForm" label-width="90px" style="margin-top: 4px;">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="产能单元" prop="resourceId">
                    <el-select v-model="scheduleForm.resourceId" placeholder="请选择" style="width: 100%">
                      <el-option v-for="r in resourceList" :key="r.resourceId" :label="r.resourceName" :value="r.resourceId" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="排产策略" prop="strategy">
                    <el-radio-group v-model="scheduleForm.strategy">
                      <el-radio label="1">正排</el-radio>
                      <el-radio label="2">倒排</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="计划开始" prop="planStart">
                    <el-date-picker v-model="scheduleForm.planStart" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="计划结束" prop="planEnd">
                    <el-date-picker v-model="scheduleForm.planEnd" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleScheduleSubmit">确认排产</el-button>
      </template>
    </el-dialog>

    <!-- ===== 任务详情弹窗 ===== -->
    <el-dialog v-model="taskDialogVisible" width="720px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H5a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2h-4"/><polyline points="9 11 12 14 15 11"/><line x1="12" y1="3" x2="12" y2="14"/></svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">排产任务详情</div>
            <div class="rd-detail-header-sub" v-if="currentTask">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">{{ currentTask.scheduleNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="currentTask">
        <!-- 卡片1：基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('td_basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              </span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.td_basic }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.td_basic">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>排产编号</span>
                <div class="rd-value">{{ currentTask.scheduleNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>状态</span>
                <div class="rd-value">
                  <el-tag :type="statusTagType(currentTask.status)" size="small">{{ statusLabel(currentTask.status) }}</el-tag>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>工单编号</span>
                <div class="rd-value">{{ currentTask.workOrderNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>工单类型</span>
                <div class="rd-value">
                  <el-tag :type="currentTask.orderType == '0' ? '' : currentTask.orderType == '1' ? 'warning' : 'info'" size="small">{{ woTypeLabel(currentTask.orderType) }}</el-tag>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>产能单元</span>
                <div class="rd-value">{{ currentTask.resourceName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>工序</span>
                <div class="rd-value">工序{{ currentTask.opSeq }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>优先级</span>
                <div class="rd-value">
                  <el-tag :type="currentTask.priority == '0' ? 'danger' : currentTask.priority == '1' ? 'warning' : 'info'" size="small" effect="dark">
                    {{ priorityLabel(currentTask.priority) }}
                  </el-tag>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>排产策略</span>
                <div class="rd-value">{{ currentTask.strategy == '1' ? '正排' : '倒排' }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 卡片2：产品与工单信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('td_wo')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 12l8 4 8-4"/><path d="M4 17l8 4 8-4"/></svg>
              </span>
              产品与工单信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.td_wo }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.td_wo">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>产品编码</span>
                <div class="rd-value">{{ currentTask.productCode || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>产品名称</span>
                <div class="rd-value">{{ currentTask.productName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>规格型号</span>
                <div class="rd-value">{{ currentTask.specModel || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>计划数量</span>
                <div class="rd-value">{{ currentTask.planQty }} {{ unitLabel(currentTask.unit) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>BOM编号</span>
                <div class="rd-value">{{ currentTask.bomNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>工艺路线</span>
                <div class="rd-value">{{ currentTask.routeNo || '-' }}</div>
              </div>
              <div class="rd-item" v-if="currentTask.mpsNo">
                <span class="rd-label"><span class="rd-label-dot"></span>关联主计划</span>
                <div class="rd-value">{{ currentTask.mpsNo }}</div>
              </div>
              <div class="rd-item" v-if="currentTask.demandNo">
                <span class="rd-label"><span class="rd-label-dot"></span>关联需求号</span>
                <div class="rd-value">{{ currentTask.demandNo }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 卡片3：排产信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('td_plan')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </span>
              排产信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.td_plan }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.td_plan">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>计划开始</span>
                <div class="rd-value">{{ formatTime(currentTask.planStart) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>计划结束</span>
                <div class="rd-value">{{ formatTime(currentTask.planEnd) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>准备时间</span>
                <div class="rd-value">{{ currentTask.setupMinutes }} 分钟</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>下达人</span>
                <div class="rd-value">{{ currentTask.releaseBy || '-' }}</div>
              </div>
              <div class="rd-item" v-if="currentTask.releaseTime">
                <span class="rd-label"><span class="rd-label-dot"></span>下达时间</span>
                <div class="rd-value">{{ formatTime(currentTask.releaseTime) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>创建时间</span>
                <div class="rd-value">{{ formatTime(currentTask.createTime) }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="taskDialogVisible = false">关闭</el-button>
        <el-button v-if="currentTask && currentTask.status === '0'" type="danger" @click="handleCancelTask(currentTask)">取消排产</el-button>
      </template>
    </el-dialog>

    <!-- ===== 操作说明弹窗 ===== -->
    <el-dialog v-model="helpVisible" width="720px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">排产工作台操作说明</div>
            <div class="rd-detail-header-sub">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">帮助文档</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 卡片1：页面布局 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('help_layout')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>
              </span>
              一、页面布局
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.help_layout }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.help_layout">
            <div class="help-list">
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text"><b>左侧栏</b>：待排产工单列表，显示状态为"新建"且尚未排产的工单。列表按优先级排序：高 → 中 → 低。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet" style="color: #ef4444;">●</span>
                <span class="help-list-text"><b>高优先级标注</b>（红色边框）：优先级为"高"的工单，排产后需<b>重点验收</b>，请优先安排排产。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet" style="color: #94a3b8;">●</span>
                <span class="help-list-text"><b>中/低优先级</b>（默认样式）：无特殊标注，按正常流程排产。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text"><b>右侧甘特图</b>：以产能单元（设备/产线）为纵轴、时间为横轴的可视化排产看板。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text"><b>顶部工具栏</b>：日期范围切换、视图模式、排产策略、批量操作。其中「显示已取消」复选框用于控制甘特图上是否展示已取消的排产任务，默认隐藏以保持看板简洁，勾选后已取消的任务以灰色任务条显示。</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 卡片2：排产操作流程 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('help_flow')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
              </span>
              二、排产操作流程
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.help_flow }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.help_flow">
            <el-steps :active="3" direction="vertical" :space="70" class="help-steps">
              <el-step title="步骤1：选择日期范围" description="在顶部日期选择器中调整甘特图显示的日期区间，默认显示当天。可切换「日视图/周视图」，日视图按小时展示，周视图按天展示。使用「前一天/后一天」按钮快速翻页。" />
              <el-step title="步骤2：拖拽工单到甘特图" description="从左侧待排产工单列表中，将工单卡片拖拽到甘特图上对应的产能单元行和起始时间位置。拖拽时甘特图上会显示动态指示线，实时提示当前时间点和产能单元。拖拽到的产能单元行即为该工单首工序的排产设备。" />
              <el-step title="步骤3：编辑并确认排产" description="松开鼠标后会弹出排产编辑弹窗，显示工单信息和排产时间，用户可手动调整排产时间和产能单元。排产对应的是工单的首道工序，产能单元取自拖拽位置。点击「确认排产」后系统自动下达，排产任务状态变为「已下达」，同时更新工单的计划时间。" />
            </el-steps>
          </div>
        </section>

        <!-- 卡片3：产能单元与工序关系 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('help_resource')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
              </span>
              三、产能单元与工序关系
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.help_resource }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.help_resource">
            <div class="help-list">
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text"><b>产能单元绑定在工艺路线的每道工序上。</b>在工艺路线管理中，每道工序可指定对应的产能单元（设备/产线），例如：工序10注塑→注塑机A，工序20组装→组装线1号，工序30检验→检验台C。一个工单可跨多个产能单元完成生产。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text"><b>排产对应首工序。</b>工单排产时，排产任务对应的是工单的第一道工序。拖拽工单到甘特图某产能单元行上，即为该首工序指定排产设备。排产弹窗中的产能单元下拉可手动调整。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text"><b>工单下达时自动生成首工序派工单。</b>工单下达时，系统根据工艺路线中首工序绑定的产能单元自动生成派工单。若工序已绑定产能单元则取工序的，未绑定则取排产指定的。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text"><b>工序流转时自动带入各工序的产能单元。</b>首工序派工单完工后，系统自动创建下一道工序的派工单，产能单元取自工艺快照中该工序绑定的产能单元。例如：注塑工序完工后，系统自动创建组装工序的派工单，产能单元自动带出"组装线1号"，无需人工干预。</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 卡片4：任务条颜色说明 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('help_color')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="13.5" cy="6.5" r="2.5"/><circle cx="19" cy="12" r="2.5"/><circle cx="6" cy="12" r="2.5"/><circle cx="10" cy="18" r="2.5"/></svg>
              </span>
              四、任务条颜色说明
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.help_color }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.help_color">
            <div class="help-color-grid">
              <div class="help-color-card">
                <span class="help-color-swatch dot-released"></span>
                <div class="help-color-text">
                  <span class="help-color-name">绿色 - 已下达</span>
                  <span class="help-color-desc">排产已确认并下达到生产执行，工单计划时间已同步更新</span>
                </div>
              </div>
              <div class="help-color-card">
                <span class="help-color-swatch dot-cancelled"></span>
                <div class="help-color-text">
                  <span class="help-color-name">灰色 - 已取消</span>
                  <span class="help-color-desc">排产已取消，工单可重新排产</span>
                </div>
              </div>
              <div class="help-color-card">
                <span class="help-color-swatch help-swatch-conflict"></span>
                <div class="help-color-text">
                  <span class="help-color-name">红色边框 - 冲突</span>
                  <span class="help-color-desc">同一产能单元同一时段有多个任务重叠</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 卡片4：单任务操作 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('help_task')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H5a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2h-4"/><polyline points="9 11 12 14 15 11"/><line x1="12" y1="3" x2="12" y2="14"/></svg>
              </span>
              五、单任务操作
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.help_task }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.help_task">
            <div class="help-list">
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text">点击甘特图上的任务条，可打开任务详情弹窗。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text">在详情弹窗中可执行：取消排产操作。</span>
              </div>
              <div class="help-list-item">
                <span class="help-list-bullet">●</span>
                <span class="help-list-text">取消排产后，工单会回到左侧待排产列表中，可重新排产。</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 卡片5：排产策略 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('help_strategy')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20V10"/><path d="M18 20V4"/><path d="M6 20v-6"/></svg>
              </span>
              六、排产策略
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.help_strategy }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.help_strategy">
            <div class="help-strategy-grid">
              <div class="help-strategy-card">
                <div class="help-strategy-tag">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/><line x1="3" y1="12" x2="15" y2="12"/></svg>
                  正排
                </div>
                <span class="help-strategy-desc">从开始时间向后排产，先做前面的工序。</span>
              </div>
              <div class="help-strategy-card">
                <div class="help-strategy-tag">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                  倒排
                </div>
                <span class="help-strategy-desc">从结束时间倒推排产，以满足交期为目标。</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 卡片6：常见问题 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('help_faq')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              </span>
              七、常见问题
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.help_faq }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.help_faq">
            <el-collapse class="help-faq-collapse">
              <el-collapse-item title="甘特图上看不到任务条？" name="1">
                <p>可能原因：当前日期范围内没有排产任务。请检查日期范围是否正确，或点击「刷新」按钮重新加载。</p>
              </el-collapse-item>
              <el-collapse-item title="拖拽后提示「系统繁忙」？" name="2">
                <p>排产任务的时间格式需要精确到秒，系统会自动处理。如果反复出现，请检查后端服务是否正常运行。</p>
              </el-collapse-item>
              <el-collapse-item title="待排产列表中没有工单？" name="3">
                <p>待排产列表只显示状态为「新建」且未排产的工单。如果工单已排产或状态已变更（如已下达），则不会显示。请到「工单管理」页面查看所有工单。</p>
              </el-collapse-item>
            </el-collapse>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="helpVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ScheduleGantt">
import * as echarts from 'echarts'
import { Tickets, InfoFilled, QuestionFilled, Fold, FullScreen, Aim } from '@element-plus/icons-vue'
import { getGanttData, dragSchedule, batchSchedule, cancelSchedule, getScheduledWoIds } from '@/api/mms/schedule'
import { listWorkOrder } from '@/api/mms/workorder'
import { listResource } from '@/api/mms/resource'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { mms_schedule_status, mms_schedule_strategy } = proxy.useDict('mms_schedule_status', 'mms_schedule_strategy')
const { mms_order_type, mms_priority } = proxy.useDict('mms_order_type', 'mms_priority')
const { wms_unit } = proxy.useDict('wms_unit')
const { collapsedCards, toggleCard } = useDetailCard(['sc_wo', 'sc_plan', 'td_basic', 'td_wo', 'td_plan', 'help_layout', 'help_flow', 'help_resource', 'help_color', 'help_task', 'help_strategy', 'help_faq'])

// ===== 状态 =====
const ganttChartRef = ref(null)
let ganttChart = null
let resizeObserver = null
let resizeTimer = null
const ganttLoading = ref(false)
const sidebarLoading = ref(false)
const sidebarCollapsed = ref(false)
const isFullscreen = ref(false)
const dateRange = ref([])
const viewMode = ref('day')
const taskDialogVisible = ref(false)
const helpVisible = ref(false)
const currentTask = ref(null)
const scheduleDialogVisible = ref(false)
const scheduleForm = ref(null)

// 数据
const scheduleData = ref([])
const resourceList = ref([])
const unscheduledOrders = ref([])
const filteredUnscheduled = ref([])
const woSearch = ref('')
const woCurrentPage = ref(1)
const woPageSize = ref(10)
const showCancelled = ref(false) // 是否显示已取消的排产任务，默认隐藏

// 任务条悬浮提示状态
const taskTooltip = ref({
  visible: false,
  x: 0,
  y: 0,
  workOrderNo: '',
  productName: '',
  resourceName: '',
  planStart: '',
  planEnd: '',
  statusLabel: ''
})

// 拖拽中的工单
const draggingWorkOrder = ref(null)

// 拖拽指示线状态
const dragIndicator = ref({
  visible: false,
  x: 0,
  labelY: 0,
  timeLabel: '',
  resourceName: '',
  isValidRow: true
})

// 计算属性
const pagedUnscheduled = computed(() => {
  const start = (woCurrentPage.value - 1) * woPageSize.value
  return filteredUnscheduled.value.slice(start, start + woPageSize.value)
})

// ===== 初始化 =====
onMounted(() => {
  initDateRange()
  loadResources().then(() => {
    loadGanttData().then(() => {
      loadUnscheduledOrders()
    })
  })
  window.addEventListener('resize', handleResize)

  // 注册拖拽事件和 resize 监听
    nextTick(() => {
      if (ganttChartRef.value) {
        // 使用 capture 模式注册拖拽事件，确保在 ECharts canvas 拦截前捕获
        ganttChartRef.value.addEventListener('dragover', onGanttDragOver, { capture: true, passive: false })
        ganttChartRef.value.addEventListener('drop', onGanttDrop, { capture: true, passive: false })
        ganttChartRef.value.addEventListener('dragleave', onGanttDragLeave, { capture: true })

        // 使用 ResizeObserver 监听容器尺寸变化（左侧菜单收缩/展开时也能自动 resize）
        resizeObserver = new ResizeObserver(() => {
          handleResize()
        })
        resizeObserver.observe(ganttChartRef.value)
      }
    })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (resizeTimer) {
    clearTimeout(resizeTimer)
    resizeTimer = null
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  // 清理拖拽事件监听
  if (ganttChartRef.value) {
    ganttChartRef.value.removeEventListener('dragover', onGanttDragOver, { capture: true })
    ganttChartRef.value.removeEventListener('drop', onGanttDrop, { capture: true })
    ganttChartRef.value.removeEventListener('dragleave', onGanttDragLeave, { capture: true })
  }
  if (ganttChart) ganttChart.dispose()
  // 退出全屏时恢复框架样式
  if (isFullscreen.value) {
    document.body.classList.remove('gantt-fullscreen-active')
  }
})

// ===== 侧栏折叠 / 全屏 =====
function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  // 折叠/展开后甘特图宽度变化，需要延迟 resize
  nextTick(() => {
    setTimeout(() => handleResize(), 300)
  })
}

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  if (isFullscreen.value) {
    document.body.classList.add('gantt-fullscreen-active')
  } else {
    document.body.classList.remove('gantt-fullscreen-active')
  }
  // 全屏切换后甘特图尺寸变化，延迟 resize
  nextTick(() => {
    setTimeout(() => handleResize(), 300)
  })
}

function initDateRange() {
  if (viewMode.value === 'day') {
    // 日视图：选中当天
    const today = new Date()
    dateRange.value = [formatDate(today), formatDate(today)]
  } else {
    // 周视图：选中本周（周一到周日）
    const now = new Date()
    const monday = new Date(now)
    const day = now.getDay() || 7
    monday.setDate(now.getDate() - day + 1)
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 6)
    dateRange.value = [formatDate(monday), formatDate(sunday)]
  }
}

function formatDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

function resetDateRange() {
  initDateRange()
  loadGanttData()
}

function navigateDate(offset) {
  const start = new Date(dateRange.value[0])
  const end = new Date(dateRange.value[1])
  const days = viewMode.value === 'day' ? 1 : 7
  start.setDate(start.getDate() + offset * days)
  end.setDate(end.getDate() + offset * days)
  dateRange.value = [formatDate(start), formatDate(end)]
  loadGanttData()
}

// 切换视图模式时调整日期范围
function handleViewModeChange() {
  if (viewMode.value === 'day') {
    // 切到日视图：取当前日期范围的开始日期作为选中日
    const focusDate = new Date(dateRange.value[0])
    dateRange.value = [formatDate(focusDate), formatDate(focusDate)]
  } else {
    // 切到周视图：取当前日期范围的开始日期所在的那一周
    const focusDate = new Date(dateRange.value[0])
    const day = focusDate.getDay() || 7
    const monday = new Date(focusDate)
    monday.setDate(focusDate.getDate() - day + 1)
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 6)
    dateRange.value = [formatDate(monday), formatDate(sunday)]
  }
  loadGanttData()
}

// ===== 数据加载 =====
function loadGanttData() {
  if (!dateRange.value || dateRange.value.length !== 2) return Promise.resolve()
  ganttLoading.value = true
  return getGanttData(dateRange.value[0], dateRange.value[1]).then(res => {
    scheduleData.value = res.data || []
    renderGantt()
  }).catch(() => {}).finally(() => {
    ganttLoading.value = false
  })
}

function loadUnscheduledOrders() {
  sidebarLoading.value = true
  // 并行查询：所有工单 + 所有有已下达排产记录的工单ID（不限日期范围）
  Promise.all([
    listWorkOrder({ pageSize: 500 }),
    getScheduledWoIds()
  ]).then(([woRes, idsRes]) => {
    // 全量已下达排产记录的工单ID集合（不受甘特图日期范围限制）
    const scheduledWoIds = new Set(idsRes.data || [])
    unscheduledOrders.value = (woRes.rows || [])
      .filter(wo => wo.status === '0' || wo.status === '1') // 新建或已下达均可排产
      .filter(wo => !scheduledWoIds.has(wo.workOrderId))    // 排除已有已下达排产记录的
      .sort(sortByPriority)                                  // 按优先级排序：高→中→低
    filteredUnscheduled.value = [...unscheduledOrders.value]
    woCurrentPage.value = 1
  }).catch(() => {}).finally(() => {
    sidebarLoading.value = false
  })
}

function loadResources() {
  return listResource({ status: '0', pageSize: 200 }).then(res => {
    resourceList.value = res.rows || []
  }).catch(() => {})
}

function filterUnscheduled() {
  if (!woSearch.value) {
    filteredUnscheduled.value = [...unscheduledOrders.value]
  } else {
    const kw = woSearch.value.toLowerCase()
    filteredUnscheduled.value = unscheduledOrders.value.filter(wo =>
      (wo.workOrderNo || '').toLowerCase().includes(kw) ||
      (wo.productName || '').toLowerCase().includes(kw) ||
      (wo.productCode || '').toLowerCase().includes(kw)
    )
  }
  // 搜索结果也按优先级排序
  filteredUnscheduled.value.sort(sortByPriority)
  // 搜索后重置到第一页
  woCurrentPage.value = 1
}

// 按优先级排序：高(0) → 中(1) → 低(2)
function sortByPriority(a, b) {
  const order = { '0': 0, '1': 1, '2': 2 }
  const pa = order[a.priority] !== undefined ? order[a.priority] : 99
  const pb = order[b.priority] !== undefined ? order[b.priority] : 99
  return pa - pb
}

function handleWoPageChange(page) {
  woCurrentPage.value = page
}

function handleDateChange() {
  loadGanttData().then(() => {
    loadUnscheduledOrders()
  })
}

// ===== 甘特图渲染 =====
function renderGantt() {
  if (!ganttChartRef.value) return
  if (!ganttChart) {
    ganttChart = echarts.init(ganttChartRef.value)
  }

  const resources = resourceList.value
  if (resources.length === 0) return

  const yAxisData = resources.map(r => r.resourceName)
  const resourceIdMap = {}
  resources.forEach((r, i) => { resourceIdMap[r.resourceId] = i })

  const startDate = dateRange.value[0] + ' 00:00:00'
  // xMax 设为次日 00:00:00，确保完整24小时时间轴都能显示
  const endDay = new Date(dateRange.value[1] + ' 00:00:00')
  endDay.setDate(endDay.getDate() + 1)
  const xMin = new Date(startDate).getTime()
  const xMax = endDay.getTime()

  // 根据开关过滤已取消的排产任务
  const tasks = showCancelled.value
    ? scheduleData.value
    : scheduleData.value.filter(t => t.status !== '1')

  // 计算冲突（已取消任务不参与冲突检测，既不标红他人也不被标红）
  const conflictMap = {}
  tasks.forEach(task => {
    // 已取消的任务不会被标记为冲突，保持灰色显示
    if (task.status === '1') {
      conflictMap[task.scheduleId] = false
      return
    }
    const start = new Date(task.planStart).getTime()
    const end = new Date(task.planEnd).getTime()
    const hasConflict = tasks.some(t =>
      t.scheduleId !== task.scheduleId &&
      t.resourceId === task.resourceId &&
      t.status !== '1' &&
      new Date(t.planStart).getTime() < end &&
      new Date(t.planEnd).getTime() > start
    )
    conflictMap[task.scheduleId] = hasConflict
  })

  const option = {
    tooltip: {
      formatter: function (params) {
        const data = params.data ? params.data.taskData : null
        if (!data) return ''
        return `<div style="font-weight:600;margin-bottom:4px">${data.workOrderNo}</div>
          <div>产品：${data.productName || '-'}</div>
          <div>产能单元：${data.resourceName || '-'}</div>
          <div>开始：${formatTime(data.planStart)}</div>
          <div>结束：${formatTime(data.planEnd)}</div>
          <div>状态：${statusLabel(data.status)}</div>`
      }
    },
    grid: {
      left: 140,
      right: 30,
      top: 50,
      bottom: 40
    },
    xAxis: {
      type: 'time',
      min: xMin,
      max: xMax,
      // 日视图按24等分（每小时一个刻度），周视图按7等分（每天一个刻度）
      splitNumber: viewMode.value === 'day' ? 24 : 7,
      axisLabel: {
        formatter: function (val) {
          const d = new Date(val)
          if (viewMode.value === 'day') {
            return `${String(d.getHours()).padStart(2, '0')}:00`
          }
          return `${d.getMonth() + 1}/${d.getDate()}`
        },
        // 日视图每1小时一个标签，周视图每12小时一个标签
        interval: viewMode.value === 'day' ? 1 * 60 * 60 * 1000 : 12 * 60 * 60 * 1000,
        color: '#64748b',
        hideOverlap: false,
        showMinLabel: true,
        showMaxLabel: true
      },
      splitLine: {
        show: true,
        lineStyle: { color: '#e2e8f0', type: 'dashed' }
      }
    },
    yAxis: {
      type: 'category',
      data: yAxisData,
      axisLabel: { color: '#334155', fontWeight: 600 },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      splitLine: { show: true, lineStyle: { color: '#f1f5f9' } }
    },
    // 使用一个透明的 scatter series 作为占位，确保 ECharts 正确初始化坐标系
    // 否则 series:[] 时 convertToPixel 无法正常工作
    series: [{
      type: 'scatter',
      data: [],
      symbolSize: 0,
      silent: true,
      tooltip: { show: false }
    }]
  }

  ganttChart.setOption(option, true)

  // 注册点击事件（点击 series 数据点时触发）
  ganttChart.off('click')
  ganttChart.on('click', function (params) {
    if (params.data && params.data.taskData) {
      currentTask.value = params.data.taskData
      taskDialogVisible.value = true
    }
  })

  // 延迟绘制以确保 ECharts 布局完成（配合占位 series 确保坐标系已初始化）
  setTimeout(() => {
    drawTaskBars(tasks, resourceIdMap, conflictMap)
  }, 50)
}

function drawTaskBars(tasks, resourceIdMap, conflictMap) {
  if (!ganttChart || !ganttChartRef.value) return

  const graphicElements = []

  // 甘特图时间轴的像素边界（用于裁剪跨天任务条）
  const startDate = dateRange.value[0] + ' 00:00:00'
  const endDay = new Date(dateRange.value[1] + ' 00:00:00')
  endDay.setDate(endDay.getDate() + 1)
  const xMinTs = new Date(startDate).getTime()
  const xMaxTs = endDay.getTime()
  const xMinPx = ganttChart.convertToPixel({ xAxisIndex: 0 }, xMinTs)
  const xMaxPx = ganttChart.convertToPixel({ xAxisIndex: 0 }, xMaxTs)

  // 计算日视图每天的像素分界点（用于跨天任务每天显示工单号）
  const dayBoundaries = []
  if (viewMode.value === 'day') {
    // 日视图：日期范围可能只有1天，边界就是 xMinPx / xMaxPx
    const start = new Date(dateRange.value[0] + ' 00:00:00')
    const end = new Date(dateRange.value[1] + ' 00:00:00')
    end.setDate(end.getDate() + 1)
    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      dayBoundaries.push(ganttChart.convertToPixel({ xAxisIndex: 0 }, d.getTime()))
    }
  } else {
    // 周视图：按天分界
    const start = new Date(dateRange.value[0] + ' 00:00:00')
    const end = new Date(dateRange.value[1] + ' 00:00:00')
    end.setDate(end.getDate() + 1)
    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      dayBoundaries.push(ganttChart.convertToPixel({ xAxisIndex: 0 }, d.getTime()))
    }
  }

  // ===== 计算重叠任务的垂直分层（lane）=====
  // 同一产能单元下时间重叠的任务分配到不同 lane，垂直错开显示
  const taskLaneMap = {} // scheduleId -> lane 索引（0, 1, 2...）
  const maxLaneByResource = {} // resourceId -> 最大 lane 数

  // 按产能单元分组
  const tasksByResource = {}
  tasks.forEach(task => {
    if (!tasksByResource[task.resourceId]) {
      tasksByResource[task.resourceId] = []
    }
    tasksByResource[task.resourceId].push(task)
  })

  // 对每个产能单元的任务，按开始时间排序后分配 lane
  Object.keys(tasksByResource).forEach(resId => {
    const groupTasks = tasksByResource[resId]
      .slice()
      .sort((a, b) => new Date(a.planStart).getTime() - new Date(b.planStart).getTime())

    // lanes[i] 存储该 lane 上最后一个任务的结束时间
    const laneEndTimes = []

    groupTasks.forEach(task => {
      const startTs = new Date(task.planStart).getTime()
      const endTs = new Date(task.planEnd).getTime()

      // 找一个不冲突的 lane
      let assignedLane = -1
      for (let i = 0; i < laneEndTimes.length; i++) {
        if (laneEndTimes[i] <= startTs) {
          assignedLane = i
          break
        }
      }
      if (assignedLane === -1) {
        assignedLane = laneEndTimes.length
        laneEndTimes.push(endTs)
      } else {
        laneEndTimes[assignedLane] = endTs
      }
      taskLaneMap[task.scheduleId] = assignedLane
    })

    maxLaneByResource[resId] = laneEndTimes.length
  })

  tasks.forEach(task => {
    const resIndex = resourceIdMap[task.resourceId]
    if (resIndex === undefined) return

    const startTs = new Date(task.planStart).getTime()
    const endTs = new Date(task.planEnd).getTime()
    const hasConflict = conflictMap[task.scheduleId] || false

    // 获取当前任务在所属产能单元的 lane 信息
    const lane = taskLaneMap[task.scheduleId] || 0
    const totalLanes = maxLaneByResource[task.resourceId] || 1

    // 使用 ECharts 的 convertToPixel 来获取像素坐标
    let x1 = ganttChart.convertToPixel({ xAxisIndex: 0 }, startTs)
    let x2 = ganttChart.convertToPixel({ xAxisIndex: 0 }, endTs)
    // category 轴：convertToPixel(yAxisIndex, resIndex) 返回该类别行的中心 Y 坐标
    const yCenter = ganttChart.convertToPixel({ yAxisIndex: 0 }, resIndex)

    if (isNaN(x1) || isNaN(x2) || isNaN(yCenter)) return

    // 裁剪到甘特图可视区域（解决跨天任务条超出边界的问题）
    const origX1 = x1
    x1 = Math.max(x1, xMinPx)
    x2 = Math.min(x2, xMaxPx)
    if (x2 <= x1) return

    // 根据重叠层数动态计算任务条高度和位置
    // 单层时 barH=24，多层时缩小到 18，留出间距
    const baseBarH = totalLanes > 1 ? 18 : 24
    const totalHeight = 30 // 行内可用高度
    const laneSpacing = Math.min(baseBarH, (totalHeight - 4) / totalLanes)
    const barH = Math.max(laneSpacing, 12)
    const laneGap = 2

    const rectX = x1
    // 多 lane 时从上到下排列
    const laneStartY = yCenter - (totalLanes * (barH + laneGap) - laneGap) / 2
    const rectY = laneStartY + lane * (barH + laneGap)
    const rectW = Math.max(x2 - x1, 3)

    const color = getTaskColor(task.status, hasConflict)

    graphicElements.push({
      type: 'rect',
      id: 'task-' + task.scheduleId,
      shape: { x: rectX, y: rectY, width: rectW, height: barH, r: [4, 4, 4, 4] },
      style: {
        fill: color,
        stroke: hasConflict ? '#ef4444' : '#fff',
        lineWidth: hasConflict ? 2 : 1
      },
      z: 100,
      onclick: (function (t) {
        return function () {
          currentTask.value = t
          taskDialogVisible.value = true
        }
      })(task),
      // 鼠标悬浮显示工单信息 tooltip
      onmousemove: (function (t, px, py) {
        return function () {
          ganttChart.dispatchAction({ type: 'showTip', seriesIndex: 0, dataIndex: 0, x: px, y: py })
          // 用自定义 HTML 浮层显示
          showTaskTooltip(t, px, py)
        }
      })(task, rectX + rectW / 2, rectY),
      onmouseout: function () {
        hideTaskTooltip()
      }
    })

    // 工单编号文字显示策略：
    // - 日视图：跨天时每天显示一次工单编号（因为每天是独立的甘特图视图）
    // - 周视图：整个任务条只显示一次工单编号（避免一周内重复显示）
    if (viewMode.value === 'day') {
      // 日视图：按天分段显示
      for (let i = 0; i < dayBoundaries.length - 1; i++) {
        const dayStart = Math.max(x1, dayBoundaries[i])
        const dayEnd = Math.min(x2, dayBoundaries[i + 1])
        const segWidth = dayEnd - dayStart
        if (segWidth < 50) continue // 太窄不显示文字，鼠标悬浮查看即可

        const textId = 'task-text-' + task.scheduleId + '-' + i
        const segStart = Math.max(origX1, dayBoundaries[i])
        const segTextX = segStart + 6

        graphicElements.push({
          type: 'text',
          id: textId,
          style: {
            text: task.workOrderNo,
            x: segTextX,
            y: rectY + barH / 2,
            fill: '#fff',
            fontSize: totalLanes > 1 ? 10 : 11,
            fontWeight: 'bold',
            textVerticalAlign: 'middle',
            overflow: 'truncate',
            width: dayEnd - segTextX - 4
          },
          z: 101,
          silent: true
        })
      }
    } else {
      // 周视图：整个任务条只显示一次工单编号
      const textX = x1 + 6
      const textWidth = x2 - textX - 4
      if (textWidth >= 50) {
        graphicElements.push({
          type: 'text',
          id: 'task-text-' + task.scheduleId,
          style: {
            text: task.workOrderNo,
            x: textX,
            y: rectY + barH / 2,
            fill: '#fff',
            fontSize: totalLanes > 1 ? 10 : 11,
            fontWeight: 'bold',
            textVerticalAlign: 'middle',
            overflow: 'truncate',
            width: textWidth
          },
          z: 101,
          silent: true
        })
      }
    }
  })

  ganttChart.setOption({ graphic: graphicElements })
}

// ===== 任务条悬浮提示 =====
function showTaskTooltip(task, px, py) {
  // py 是任务条顶部位置，tooltip 显示在任务条上方
  const tooltipY = py - 10 // 稍微上移，让浮层在任务条上方
  taskTooltip.value = {
    visible: true,
    x: px,
    y: tooltipY,
    workOrderNo: task.workOrderNo || '-',
    productName: task.productName || '-',
    resourceName: task.resourceName || '-',
    planStart: formatTime(task.planStart),
    planEnd: formatTime(task.planEnd),
    statusLabel: statusLabel(task.status)
  }
}

function hideTaskTooltip() {
  taskTooltip.value.visible = false
}

function getTaskColor(status, hasConflict) {
if (hasConflict) return '#ef4444'
switch (status) {
case '0': return '#10b981' // 已下达-绿色
case '1': return '#94a3b8' // 已取消-灰色
default: return '#10b981'
}
}

function statusLabel(status) {
const item = mms_schedule_status.value ? mms_schedule_status.value.find(d => d.value == status) : null
return item ? item.label : '—'
}

function unitLabel(val) {
  if (!val && val !== '0') return ''
  const item = wms_unit.value ? wms_unit.value.find(d => d.value === val) : null
  return item ? item.label : val
}

function woTypeLabel(type) {
  const item = mms_order_type.value ? mms_order_type.value.find(d => d.value == type) : null
  return item ? item.label : '—'
}

function priorityLabel(priority) {
  const item = mms_priority.value ? mms_priority.value.find(d => d.value == priority) : null
  return item ? item.label : '—'
}

function statusTagType(status) {
  switch (status) {
    case '0': return 'success' // 已下达
    case '1': return 'danger'   // 已取消
    default: return 'info'
  }
}

function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

function handleResize() {
  if (!ganttChart) return
  // 防抖：避免收缩菜单过渡过程中频繁触发
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    if (!ganttChart) return
    ganttChart.resize()
    // resize 后坐标系像素变了，需重新绘制任务条
    const resources = resourceList.value
    if (resources.length === 0 || scheduleData.value.length === 0) return
    const resourceIdMap = {}
    resources.forEach((r, i) => { resourceIdMap[r.resourceId] = i })
    // 与 renderGantt 保持一致的过滤逻辑
    const tasks = showCancelled.value
      ? scheduleData.value
      : scheduleData.value.filter(t => t.status !== '1')
    const conflictMap = {}
    tasks.forEach(task => {
      // 已取消的任务不会被标记为冲突，保持灰色显示
      if (task.status === '1') {
        conflictMap[task.scheduleId] = false
        return
      }
      const start = new Date(task.planStart).getTime()
      const end = new Date(task.planEnd).getTime()
      conflictMap[task.scheduleId] = tasks.some(t =>
        t.scheduleId !== task.scheduleId &&
        t.resourceId === task.resourceId &&
        t.status !== '1' &&
        new Date(t.planStart).getTime() < end &&
        new Date(t.planEnd).getTime() > start
      )
    })
    drawTaskBars(tasks, resourceIdMap, conflictMap)
  }, 150)
}

// ===== 拖拽：从侧栏拖工单到甘特图 =====
function onDragStart(event, wo) {
  draggingWorkOrder.value = wo
  event.dataTransfer.effectAllowed = 'copy'
  event.dataTransfer.setData('text/plain', wo.workOrderId)
}

function onDragEnd() {
  draggingWorkOrder.value = null
  dragIndicator.value.visible = false
}

// 拖拽离开甘特图区域时隐藏指示线
function onGanttDragLeave() {
  dragIndicator.value.visible = false
}

// 拖拽过程中在甘特图上显示动态时间指示线（HTML 浮层方式）
function onGanttDragOver(e) {
  e.preventDefault()
  e.dataTransfer.dropEffect = 'copy'
  if (!ganttChart || !draggingWorkOrder.value) return

  const rect = ganttChartRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  // 将像素坐标转换为时间值
  const timePoint = ganttChart.convertFromPixel({ xAxisIndex: 0 }, x)
  const yIndex = Math.round(ganttChart.convertFromPixel({ yAxisIndex: 0 }, y))

  // 获取图表边界
  const startDate = dateRange.value[0] + ' 00:00:00'
  const endDay = new Date(dateRange.value[1] + ' 00:00:00')
  endDay.setDate(endDay.getDate() + 1)
  const xMin = new Date(startDate).getTime()
  const xMax = endDay.getTime()
  const leftPx = ganttChart.convertToPixel({ xAxisIndex: 0 }, xMin)
  const rightPx = ganttChart.convertToPixel({ xAxisIndex: 0 }, xMax)
  const topPx = ganttChart.convertToPixel({ yAxisIndex: 0 }, 0)
  const bottomPx = ganttChart.convertToPixel({ yAxisIndex: 0 }, Math.max(resourceList.value.length - 1, 0))

  const gridLeft = leftPx
  const gridRight = rightPx
  const gridTop = Math.min(topPx, bottomPx) - 20

  // 超出图表区域不显示
  if (x < gridLeft - 10 || x > gridRight + 10) {
    dragIndicator.value.visible = false
    return
  }

  // 限制指示线在图表区域内
  const lineX = Math.max(gridLeft, Math.min(x, gridRight))

  // 格式化时间标签
  const d = new Date(timePoint)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const dateStr = `${d.getMonth() + 1}/${d.getDate()}`
  const timeLabel = viewMode.value === 'day' ? `${hh}:${mm}` : `${dateStr} ${hh}:${mm}`

  // 判断是否在有效产能单元行上
  const isValidRow = yIndex >= 0 && yIndex < resourceList.value.length
  const resourceName = isValidRow ? resourceList.value[yIndex].resourceName : ''

  // 更新指示线状态
  dragIndicator.value = {
    visible: true,
    x: lineX,
    labelY: Math.max(gridTop - 30, 2),
    timeLabel,
    resourceName,
    isValidRow
  }
}

function onGanttDrop(e) {
  e.preventDefault()
  if (!draggingWorkOrder.value) return
  const wo = draggingWorkOrder.value

  if (!ganttChart) return
  const rect = ganttChartRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  const timePoint = ganttChart.convertFromPixel({ xAxisIndex: 0 }, x)
  const yIndex = Math.round(ganttChart.convertFromPixel({ yAxisIndex: 0 }, y))

  // 隐藏指示线
  dragIndicator.value.visible = false

  if (yIndex < 0 || yIndex >= resourceList.value.length) {
    proxy.$modal.msgWarning('请拖放到产能单元行上')
    return
  }

  const resource = resourceList.value[yIndex]
  const startTime = new Date(timePoint)
  // 对齐到整点
  startTime.setMinutes(0, 0, 0)
  const endTime = new Date(startTime.getTime() + 1 * 60 * 60 * 1000)

  // 弹出编辑弹窗，用户可手动调整排期时间
  scheduleForm.value = {
    workOrderId: wo.workOrderId,
    workOrderNo: wo.workOrderNo,
    orderType: wo.orderType,
    productId: wo.productId,
    productCode: wo.productCode,
    productName: wo.productName,
    specModel: wo.specModel,
    planQty: wo.planQty,
    unit: wo.unit,
    bomNo: wo.bomNo,
    routeNo: wo.routeNo,
    priority: wo.priority,
    resourceId: resource.resourceId,
    resourceName: resource.resourceName,
    opSeq: 1,
    planStart: formatTime(startTime),
    planEnd: formatTime(endTime),
    setupMinutes: 0,
      strategy: '1',
      status: '0' // 已下达
    }
  scheduleDialogVisible.value = true
}

// 确认排产提交
function handleScheduleSubmit() {
  if (!scheduleForm.value) return
  const form = scheduleForm.value

  // 校验
  if (!form.resourceId) {
    proxy.$modal.msgWarning('请选择产能单元')
    return
  }
  if (!form.planStart || !form.planEnd) {
    proxy.$modal.msgWarning('请选择排产时间')
    return
  }
  const startTs = new Date(form.planStart).getTime()
  const endTs = new Date(form.planEnd).getTime()
  if (endTs <= startTs) {
    proxy.$modal.msgWarning('结束时间必须大于开始时间')
    return
  }

  // 补充产能单元名称
  const res = resourceList.value.find(r => r.resourceId === form.resourceId)
  if (res) form.resourceName = res.resourceName

  batchSchedule([form]).then(res => {
    scheduleDialogVisible.value = false
    // 刷新甘特图和待排产列表
    loadGanttData().then(() => {
      loadUnscheduledOrders()
    })
    // 根据是否有冲突显示不同提示
    const data = res.data || {}
    if (data.hasConflict) {
      const msgs = (data.conflictMsgs || []).join('；')
      proxy.$modal.msgWarning('排产已保存，但存在冲突：' + msgs)
    } else {
      proxy.$modal.msgSuccess('排产下达成功')
    }
  })
}

// ===== 单任务操作 =====
function handleCancelTask(task) {
  proxy.$modal.confirm('确定取消该排产？').then(() => {
    cancelSchedule(task.scheduleId, '甘特图取消').then(() => {
      proxy.$modal.msgSuccess('取消成功')
      taskDialogVisible.value = false
      loadGanttData().then(() => {
        loadUnscheduledOrders()
      })
    })
  }).catch(() => {})
}
</script>

<style scoped>
.mms-schedule-gantt-page {
  --brand-50: #eef2ff; --brand-100: #e0e7ff; --brand-500: #6366f1; --brand-600: #4f46e5;
  --ink-900: #0f172a; --ink-700: #334155; --ink-500: #64748b; --ink-400: #94a3b8;
  --ink-300: #cbd5e1; --ink-200: #e2e8f0; --ink-100: #f1f5f9; --ink-50: #f8fafc;
  --r-sm: 6px; --r-md: 10px; --r-lg: 14px;
  --shadow-card: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  padding-top: 10px;
  color: var(--ink-900);
}
.mms-schedule-gantt-page .surface {
  background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-lg);
  box-shadow: var(--shadow-card); overflow: hidden;
}
.gantt-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 16px; margin-bottom: 8px; flex-wrap: wrap; gap: 8px;
}
.toolbar-left, .toolbar-right { display: flex; align-items: center; gap: 8px; }
.toolbar-divider { width: 1px; height: 20px; background: var(--ink-200); margin: 0 4px; }

/* 方案3：优化高度计算，从 200px 减到 130px，多出约 70px 垂直空间 */
.gantt-body {
  display: flex; gap: 8px; height: calc(100vh - 130px); min-height: 400px;
}

/* 侧栏 */
.gantt-sidebar {
  width: 280px; flex-shrink: 0; display: flex; flex-direction: column;
  transition: width 0.25s ease;
}
/* 方案1：折叠态 */
.gantt-sidebar.sidebar-collapsed {
  width: 44px;
}
.gantt-sidebar.sidebar-collapsed .sidebar-collapsed-bar {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 12px 4px; cursor: pointer; color: var(--ink-500);
  transition: color 0.15s;
}
.gantt-sidebar.sidebar-collapsed .sidebar-collapsed-bar:hover {
  color: var(--brand-500);
}
.gantt-sidebar.sidebar-collapsed .sidebar-collapsed-text {
  font-size: 10px; writing-mode: vertical-rl; letter-spacing: 2px;
}
.sidebar-collapse-btn { margin-left: auto; }
.sidebar-header {
  display: flex; align-items: center; gap: 6px; padding: 12px 16px;
  font-weight: 600; font-size: 14px; color: var(--ink-700);
  border-bottom: 1px solid var(--ink-200);
}
.sidebar-search { padding: 8px 12px; }
.sidebar-list {
  flex: 1; overflow-y: auto; padding: 4px 8px;
}
.sidebar-pagination {
  padding: 6px 8px; border-top: 1px solid var(--ink-200);
  display: flex; justify-content: center; flex-shrink: 0;
}
.wo-card {
  position: relative;
  background: var(--ink-50); border: 1px solid var(--ink-200);
  border-radius: var(--r-sm); padding: 10px 12px; margin-bottom: 6px;
  cursor: grab; transition: all .15s;
}
.wo-card:hover {
  border-color: var(--brand-500); background: var(--brand-50);
  box-shadow: 0 2px 4px rgba(99,102,241,.1);
}
.wo-card:active { cursor: grabbing; opacity: 0.7; }

/* 高优先级工单卡片样式 */
.wo-card-priority-high {
  border-color: #f87171;
  border-left: 3px solid #ef4444;
  background: linear-gradient(135deg, #fef2f2 0%, #fff 60%);
}
.wo-card-priority-high:hover {
  border-color: #ef4444;
  background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 60%);
  box-shadow: 0 2px 8px rgba(239,68,68,.15);
}

.wo-card-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;
}
.wo-no { font-size: 13px; font-weight: 600; color: var(--ink-700); }
.wo-card-body { font-size: 12px; color: var(--ink-500); }
.wo-product { font-size: 13px; color: var(--ink-700); margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.wo-meta { display: flex; justify-content: space-between; }

/* 主区域 */
.gantt-main {
flex: 1; display: flex; flex-direction: column; position: relative;
}
.gantt-chart {
  flex: 1; min-height: 400px;
  position: relative; /* 为拖拽指示线提供定位上下文 */
}

/* 拖拽时间指示线 */
.drag-indicator {
  position: absolute;
  top: 0; bottom: 0;
  width: 0;
  z-index: 200;
  pointer-events: none;
}
.drag-indicator-line {
  position: absolute;
  top: 30px; bottom: 10px;
  left: -1px;
  width: 2px;
  background: #6366f1;
  border-radius: 1px;
}
.drag-indicator-line.is-invalid {
  background: #ef4444;
}
.drag-indicator-label {
  position: absolute;
  left: 0;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}
.drag-indicator-time {
  background: #6366f1;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(99,102,241,.3);
}
.drag-indicator-time.is-invalid {
  background: #ef4444;
  box-shadow: 0 2px 6px rgba(239,68,68,.3);
}
.drag-indicator-resource {
  font-size: 11px;
  color: #6366f1;
  font-weight: 600;
  text-shadow: 0 0 4px #fff, 0 0 4px #fff;
}

/* 任务条悬浮提示 */
.task-tooltip {
  position: absolute;
  z-index: 300;
  pointer-events: none;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.92);
  color: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 12px;
  line-height: 1.6;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(4px);
}
.task-tooltip-title {
  font-weight: 700;
  font-size: 13px;
  margin-bottom: 4px;
  padding-bottom: 4px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}
.task-tooltip-row {
  color: rgba(255, 255, 255, 0.85);
}
.gantt-legend {
  display: flex; align-items: center; gap: 16px; padding: 8px 16px;
  border-top: 1px solid var(--ink-200); background: var(--ink-50);
  font-size: 12px; color: var(--ink-500);
}
.legend-item { display: flex; align-items: center; gap: 4px; }
.legend-item .dot { width: 10px; height: 10px; border-radius: 3px; }
.dot-released { background: #10b981; }
.dot-cancelled { background: #94a3b8; }
.dot-conflict { background: #ef4444; }
.legend-tip { margin-left: auto; display: flex; align-items: center; gap: 4px; color: var(--ink-400); }

/* 操作说明弹窗 - 卡片内容样式 */
.help-list { display: flex; flex-direction: column; gap: 10px; }
.help-list-item { display: flex; align-items: flex-start; gap: 8px; }
.help-list-bullet { color: var(--brand-500, #6366f1); font-size: 10px; line-height: 22px; flex-shrink: 0; }
.help-list-text { font-size: 14px; line-height: 1.7; color: #374151; }
.help-steps { padding-left: 4px; }
.help-color-grid { display: flex; flex-direction: column; gap: 10px; }
.help-color-card {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 14px; border-radius: 8px;
  background: #f8fafc; border: 1px solid #e2e8f0;
  transition: all .15s;
}
.help-color-card:hover { border-color: #c7d2fe; background: #eef2ff; }
.help-color-swatch { width: 24px; height: 24px; border-radius: 6px; flex-shrink: 0; }
.help-swatch-conflict { background: #fff; border: 2px solid #ef4444; }
.help-color-text { display: flex; flex-direction: column; gap: 2px; }
.help-color-name { font-size: 14px; font-weight: 600; color: #111827; }
.help-color-desc { font-size: 13px; color: #6b7280; }
.help-strategy-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.help-strategy-card {
  display: flex; flex-direction: column; gap: 8px;
  padding: 14px; border-radius: 10px;
  background: #f8fafc; border: 1px solid #e2e8f0;
  transition: all .15s;
}
.help-strategy-card:hover { border-color: #c7d2fe; background: #eef2ff; }
.help-strategy-tag {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 14px; font-weight: 700; color: #4f46e5;
}
.help-strategy-desc { font-size: 13px; color: #6b7280; line-height: 1.6; }
.help-faq-collapse { border: none; }
.help-faq-collapse :deep(.el-collapse-item__header) { font-size: 14px; font-weight: 500; color: #374151; }
.help-faq-collapse :deep(.el-collapse-item__content) { font-size: 13px; color: #6b7280; padding-bottom: 8px; }

/* 拖拽排产编辑弹窗 */
.schedule-wo-info { font-size: 14px; font-weight: 600; color: var(--ink-700); }
</style>

<!-- 全屏模式样式（非 scoped，需穿透到 body 和框架元素） -->
<style>
/* 隐藏左侧菜单 */
body.gantt-fullscreen-active .sidebar-container {
  display: none !important;
}
/* 隐藏顶部导航 + 页签栏（fixed-header 包含 navbar 和 tags-view） */
body.gantt-fullscreen-active .fixed-header {
  display: none !important;
}
/* 取消主容器的左边距 */
body.gantt-fullscreen-active .main-container {
  margin-left: 0 !important;
}
/* 消除 app-main 的 margin-top 和 padding */
body.gantt-fullscreen-active .app-main {
  margin-top: 0 !important;
  padding: 0 !important;
  height: 100vh !important;
}
body.gantt-fullscreen-active .mms-schedule-gantt-page {
  padding-top: 4px !important;
}
/* 全屏时甘特图高度：100vh - 工具栏(~50px) - 图例(~36px) - 间距 */
body.gantt-fullscreen-active .gantt-body {
  height: calc(100vh - 100px) !important;
}

</style>
