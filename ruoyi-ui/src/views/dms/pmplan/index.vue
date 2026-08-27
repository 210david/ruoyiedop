<template>
  <div class="app-container dms-pmplan-page">
    <!-- 视图切换 -->
    <div class="view-mode-bar">
      <el-radio-group v-model="viewMode">
        <el-radio-button value="list">列表视图</el-radio-button>
        <el-radio-button value="calendar">日历视图</el-radio-button>
        <el-radio-button value="timeline">时间线视图</el-radio-button>
      </el-radio-group>
      <button class="tip-pill" @click="showStatusHelp = true">
        <el-icon><WarningFilled /></el-icon>
        <span>业务操作说明</span>
      </button>
    </div>

    <!-- ===== 列表视图 ===== -->
    <template v-if="viewMode === 'list'">
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
          <label>触发类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.triggerType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_pm_trigger_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>关联设备</label>
          <div class="control">
            <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="启用" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>负责人</label>
          <div class="control">
            <el-input v-model="queryParams.assigneeName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>下次执行时间</label>
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:pmplan:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:pmplan:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:pmplan:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleAutoGenerate" v-hasPermi="['dms:pmplan:edit']">
            <el-icon><Refresh /></el-icon> 执行自动生成
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_pmplan_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="计划名称" prop="planName" key="planName" :width="colWidth('planName', 180)" resizable show-overflow-tooltip v-if="columns.planName.visible" />
          <el-table-column label="关联设备" prop="equipmentName" key="equipmentName" :width="colWidth('equipmentName', 160)" resizable show-overflow-tooltip v-if="columns.equipmentName.visible" />
          <el-table-column label="触发类型" prop="triggerType" key="triggerType" :width="colWidth('triggerType', 130)" resizable align="center" v-if="columns.triggerType.visible">
            <template #default="scope"><span class="badge violet">{{ triggerTypeLabel(scope.row.triggerType) }}</span></template>
          </el-table-column>
          <el-table-column label="周期" width="100" align="center" v-if="columns.cycle.visible">
            <template #default="scope">{{ scope.row.cycleValue }} {{ scope.row.cycleUnit }}</template>
          </el-table-column>
          <el-table-column label="预估工时" prop="estimatedHours" key="estimatedHours" :width="colWidth('estimatedHours', 90)" resizable align="center" v-if="columns.estimatedHours.visible" />
          <el-table-column label="提前天数" prop="advanceDays" key="advanceDays" :width="colWidth('advanceDays', 90)" resizable align="center" v-if="columns.advanceDays.visible" />
          <el-table-column label="自动派工" width="90" align="center" v-if="columns.autoAssign.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.autoAssign === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.autoAssign === '1' ? '是' : '否' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="上次执行" prop="lastExecuteTime" key="lastExecuteTime" :width="colWidth('lastExecuteTime', 160)" resizable align="center" v-if="columns.lastExecuteTime.visible" />
          <el-table-column label="下次执行" prop="nextExecuteTime" key="nextExecuteTime" :width="colWidth('nextExecuteTime', 160)" resizable align="center" v-if="columns.nextExecuteTime.visible">
            <template #default="scope">
              <span :style="isExpired(scope.row) ? 'color: #f56c6c; font-weight: bold' : ''">{{ scope.row.nextExecuteTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right" class-name="col-action">
        <template #default="scope">
          <div class="action-btn-row">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['dms:pmplan:query']">详情</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:pmplan:edit']">修改</el-button>
            <el-button link type="success" icon="Promotion" @click="handleGenerate(scope.row)" v-if="scope.row.status === '0'">生成</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:pmplan:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    </template>

    <!-- ===== 日历视图 ===== -->
    <template v-if="viewMode === 'calendar'">
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div class="cal-cell">
            <div class="cal-day" :class="{ 'is-selected': data.isSelected }">{{ data.day.split('-').slice(2).join('') }}</div>
            <div class="cal-plans">
              <div v-for="item in getCalendarItems(data.day).slice(0, 3)" :key="item.planId" class="cal-plan" :title="item.planName" @click.stop="handleCalendarClick(item)">
                {{ item.planName }}
              </div>
              <el-popover v-if="getCalendarItems(data.day).length > 3" placement="right" :width="320" trigger="click">
                <template #reference>
                  <div class="cal-more" @click.stop>还有 {{ getCalendarItems(data.day).length - 3 }} 项</div>
                </template>
                <div class="cal-pop-list">
                  <div v-for="item in getCalendarItems(data.day)" :key="item.planId" class="cal-pop-item" :title="item.planName" @click="handleCalendarClick(item)">
                    {{ item.planName }}
                  </div>
                </div>
              </el-popover>
            </div>
          </div>
        </template>
      </el-calendar>
    </template>

    <!-- ===== 时间线视图 ===== -->
    <template v-if="viewMode === 'timeline'">
      <el-timeline>
        <el-timeline-item v-for="item in calendarData" :key="item.planId" :timestamp="formatDate(item.nextExecuteTime)" placement="top" :type="isExpiredByDate(item.nextExecuteTime) ? 'danger' : 'primary'" :hollow="isExpiredByDate(item.nextExecuteTime)">
          <el-card shadow="hover" style="cursor: pointer" @click="handleUpdate(item)">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div>
                <strong>{{ item.planName }}</strong>
                <el-tag v-if="item.autoAssign === '1'" type="success" size="small" style="margin-left: 8px">自动派工</el-tag>
                <el-tag v-if="isExpiredByDate(item.nextExecuteTime)" type="danger" size="small" style="margin-left: 8px">已逾期</el-tag>
              </div>
              <div style="color: #909399; font-size: 13px">
                <span v-if="item.equipmentName">{{ item.equipmentName }} | </span>
                <span>{{ item.cycleValue }} {{ item.cycleUnit }}</span>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </template>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="pmplanRef" :model="form" :rules="rules" label-width="100px">
                <!-- 分组一：计划信息 -->
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row>
          <el-col :span="24"><el-form-item label="计划名称" prop="planName"><el-input v-model="form.planName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备编号" prop="equipmentId">
            <el-input v-model="form.equipmentCode" readonly placeholder="请选择设备" style="width: 100%" @click="openEquipmentPicker">
              <template #append><el-button icon="Search" @click="openEquipmentPicker" /></template>
              <template #suffix><el-icon v-if="form.equipmentCode" class="clear-icon" @click.stop="clearEquipment"><CircleClose /></el-icon></template>
            </el-input>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称"><el-input v-model="form.equipmentName" placeholder="选择设备后自动带出" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备类型"><el-input v-model="form.categoryName" placeholder="选择设备后自动带出" disabled /></el-form-item></el-col>
        </el-row>

        <!-- 分组二：触发规则 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg></span>触发规则</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="12"><el-form-item label="触发类型" prop="triggerType">
            <el-select v-model="form.triggerType" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_pm_trigger_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="周期" prop="cycleValue">
            <div class="cycle-composite">
              <el-input-number v-model="form.cycleValue" :min="1" controls-position="right" style="width: 120px" />
              <el-select v-model="form.cycleUnit" placeholder="单位" style="width: 90px">
                <el-option label="天" value="天" />
                <el-option label="周" value="周" />
                <el-option label="月" value="月" />
                <el-option label="小时" value="小时" />
              </el-select>
              <span class="cycle-suffix">每 {{ form.cycleValue || '?' }} {{ form.cycleUnit || '天' }} 执行一次</span>
            </div>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="提前天数" prop="advanceDays"><el-input-number v-model="form.advanceDays" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="自动派工" prop="autoAssign">
            <el-radio-group v-model="form.autoAssign"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <!-- 自动派工人员选择 -->
        <el-row v-if="form.autoAssign === '1'">
          <el-col :span="12"><el-form-item label="派工人员" prop="assigneeId">
            <el-input v-model="form.assigneeName" readonly placeholder="请选择派工人员" style="width: 100%" @click="openUserPicker">
              <template #append><el-button icon="Search" @click="openUserPicker" /></template>
              <template #suffix><el-icon v-if="form.assigneeName" class="clear-icon" @click.stop="form.assigneeId = undefined; form.assigneeName = undefined"><CircleClose /></el-icon></template>
            </el-input>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="人员名称"><el-input v-model="form.assigneeName" placeholder="选择后自动带出" disabled /></el-form-item></el-col>
        </el-row>

        <!-- 分组三：任务配置 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>任务配置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row>
          <el-col :span="12"><el-form-item label="预估工时" prop="estimatedHours"><el-input-number v-model="form.estimatedHours" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <!-- 任务清单编辑器 -->
        <el-form-item label="任务清单" prop="taskList">
          <div style="width: 100%">
            <el-alert type="info" :closable="false" show-icon style="margin-bottom: 8px">
              <template #title>维护时需要逐项执行的标准作业步骤，生成工单后维修人员按清单逐项打勾确认</template>
            </el-alert>
            <div v-for="(task, index) in taskItems" :key="index" style="display: flex; align-items: center; margin-bottom: 8px">
              <span style="width: 28px; text-align: center; color: #909399; font-weight: bold; flex-shrink: 0">{{ index + 1 }}</span>
              <el-input v-model="task.text" placeholder="例如：检查电机温度、清洁滤网、紧固螺栓" style="flex: 1; margin-right: 8px" />
              <el-button type="danger" plain icon="Delete" circle size="small" @click="taskItems.splice(index, 1)" />
            </div>
            <el-button type="primary" plain icon="Plus" size="small" @click="taskItems.push({ text: '' })">添加任务项</el-button>
          </div>
        </el-form-item>
        <!-- 备件清单编辑器 -->
        <el-form-item label="备件清单" prop="sparePartList">
          <div style="width: 100%">
            <el-alert type="info" :closable="false" show-icon style="margin-bottom: 8px">
              <template #title>预先配置本次维护需要准备的备件及数量，生成工单时自动带入</template>
            </el-alert>
            <el-table v-if="sparePartItems.length > 0" :data="sparePartItems" border size="small" style="margin-bottom: 8px" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" type="index" width="85" align="center" />
              <el-table-column label="备件名称" min-width="220">
                <template #default="{ row, $index }">
                  <el-input :model-value="row.partName" readonly placeholder="请选择备件" style="width: 100%" @click="openPartPicker($index)">
                    <template v-if="row.partId" #append>
                      <el-button icon="CircleClose" @click.stop="clearSparePart($index)" />
                    </template>
                    <template v-else #append>
                      <el-button icon="Search" @click.stop="openPartPicker($index)" />
                    </template>
                  </el-input>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="120" align="center">
                <template #default="{ row }">
                  <el-input-number v-model="row.qty" :min="1" :precision="0" size="small" controls-position="right" style="width: 100px" />
                </template>
              </el-table-column>
              <el-table-column label="单位" width="70" align="center">
                <template #default="{ row }">{{ dictUnitLabel(row.unit) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" icon="Delete" @click="sparePartItems.splice($index, 1)"></el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain icon="Plus" size="small" @click="openPartPicker(-1)">添加备件</el-button>
          </div>
        </el-form-item>

        <!-- 分组四：其他信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-row>
          <el-col :span="12"><el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <user-picker ref="userPickerRef" title="选择派工人员" @confirm="onUserPickerConfirm" />
    <equipment-picker ref="equipmentPickerRef" title="选择设备" @confirm="onEquipmentPickerConfirm" />
    <part-picker ref="partPickerRef" title="选择备件" @confirm="onPartPickerConfirm" />

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="1080px" append-to-body draggable class="rd-dialog pmplan-detail-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">PM计划详情</div>
            <div class="rd-detail-header-sub" v-if="viewForm.planName">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">{{ viewForm.planName }}</span>
            </div>
          </div>
          <div class="rd-detail-header-tags" v-if="viewForm.status != null">
            <el-tag :type="viewForm.status === '0' ? 'success' : 'info'" effect="dark" size="small">{{ viewForm.status === '0' ? '正常' : '停用' }}</el-tag>
          </div>
        </div>
      </template>
      <div v-loading="viewLoading" class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>计划名称</span><div class="rd-value">{{ viewForm.planName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>设备编号</span><div class="rd-value">{{ viewForm.equipmentCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>设备名称</span><div class="rd-value">{{ viewForm.equipmentName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>设备类型</span><div class="rd-value">{{ viewForm.categoryName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>状态</span><div class="rd-value"><span class="badge" :class="viewForm.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ viewForm.status === '0' ? '正常' : '停用' }}</span></div></div>
            </div>
          </div>
        </section>

        <!-- 触发规则 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg></span>触发规则</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>触发类型</span><div class="rd-value"><span class="badge violet">{{ triggerTypeLabel(viewForm.triggerType) }}</span></div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>周期</span><div class="rd-value">{{ viewForm.cycleValue || '-' }} {{ viewForm.cycleUnit || '' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>提前天数</span><div class="rd-value">{{ viewForm.advanceDays != null ? viewForm.advanceDays + ' 天' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>自动派工</span><div class="rd-value"><span class="badge" :class="viewForm.autoAssign === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ viewForm.autoAssign === '1' ? '是' : '否' }}</span></div></div>
              <div class="rd-item" v-if="viewForm.autoAssign === '1'"><span class="rd-label"><span class="rd-label-dot"></span>派工人员</span><div class="rd-value">{{ viewForm.assigneeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>预估工时</span><div class="rd-value">{{ viewForm.estimatedHours != null ? viewForm.estimatedHours + ' 小时' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>上次执行</span><div class="rd-value">{{ formatDate(viewForm.lastExecuteTime) || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>下次执行</span><div class="rd-value" :style="isExpiredByDate(viewForm.nextExecuteTime) ? 'color: #f56c6c; font-weight: bold' : ''">{{ formatDate(viewForm.nextExecuteTime) || '-' }}</div></div>
            </div>
          </div>
        </section>

        <!-- 任务清单 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>任务清单</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div v-if="viewForm._taskItems && viewForm._taskItems.length > 0" class="task-checklist">
              <div class="task-checklist-body">
                <div v-for="(task, index) in viewForm._taskItems" :key="index" class="task-item">
                  <el-icon class="task-icon-pending"><RemoveFilled /></el-icon>
                  <span>{{ task.text }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无任务清单" :image-size="60" />
          </div>
        </section>

        <!-- 备件清单 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>备件清单</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v4">
            <el-table v-if="viewForm._sparePartItems && viewForm._sparePartItems.length > 0" :data="viewForm._sparePartItems" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="备件名称" prop="partName" min-width="200" show-overflow-tooltip />
              <el-table-column label="数量" prop="qty" width="80" align="center" />
              <el-table-column label="单位" width="70" align="center">
                <template #default="{ row }">{{ dictUnitLabel(row.unit) }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无备件清单" :image-size="60" />
          </div>
        </section>

        <!-- 系统信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v5')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>系统信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v5">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>创建人</span><div class="rd-value">{{ viewForm.createBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>创建时间</span><div class="rd-value">{{ viewForm.createTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>更新人</span><div class="rd-value">{{ viewForm.updateBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>更新时间</span><div class="rd-value">{{ viewForm.updateTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label"><span class="rd-label-dot"></span>备注</span><div class="rd-value">{{ viewForm.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="PM计划业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">创建PM计划</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">配置触发规则</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">配置任务清单与备件</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">到期生成工单</el-tag>
          </div>
        </div>

        <h4>二、触发规则说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="手动触发">由人工点击「生成工单」按钮手动生成维护工单</el-descriptions-item>
          <el-descriptions-item label="周期触发">系统按设定的周期值和单位（天/月/小时）自动计算下次执行时间，到期后自动生成工单</el-descriptions-item>
          <el-descriptions-item label="提前天数">在到期日前N天提前生成工单，便于提前准备备件和安排人员</el-descriptions-item>
          <el-descriptions-item label="自动派工">开启后，生成的工单自动指派给配置的维修人；关闭则需手动派工</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">任务清单</div>
              <div class="highlight-card-body">配置标准作业步骤，生成工单后维修人员在完工时<strong>逐项打勾确认</strong>，确保维护质量可追溯</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">备件预配置</div>
              <div class="highlight-card-body">预先配置维护所需备件及数量，生成工单时<strong>自动带入工单</strong>，维修人员无需重复选择</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">多视图管理</div>
              <div class="highlight-card-body">支持<strong>列表、日历、时间线</strong>三种视图。日历视图可直观查看每日计划分布，时间线视图按时间排序展示</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">逾期预警</div>
              <div class="highlight-card-body">下次执行时间超过当前时间的计划以<strong>红色高亮</strong>显示，提醒及时执行或检查计划配置</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建PM计划：</strong>填写计划名称、选择关联设备（自动带出设备分类），设置触发类型和周期
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>配置任务与备件：</strong>编辑维护任务清单（标准作业步骤）和备件清单（所需备件及数量）
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>生成工单：</strong>手动点击「生成工单」或系统到期自动生成。工单自动带入任务清单和备件清单
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>批量执行：</strong>点击「执行自动生成」按钮，系统扫描所有到期计划并批量生成工单</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsPmPlan">
import { listPmplan, getPmplan, addPmplan, updatePmplan, delPmplan, generateWorkOrder, autoGeneratePm, getPmplanCalendar } from '@/api/dms/pmplan'
import { listEquipment } from '@/api/dms/equipment'
import { listUser } from '@/api/system/user'
import UserPicker from '@/components/UserPicker/index.vue'
import EquipmentPicker from '@/components/EquipmentPicker/index.vue'
import PartPicker from '@/components/PartPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, Edit, Delete, Refresh, ArrowDown, View, CircleClose, RemoveFilled, WarningFilled, ArrowRight } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0","v1","v2","v3","v4","v5"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_pmplan_index')
const { dms_pm_trigger_type, wms_unit } = proxy.useDict('dms_pm_trigger_type', 'wms_unit')

const list = ref([])
const equipmentOptions = ref([])
const userOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewMode = ref('list')
const calendarDate = ref(new Date())
const calendarData = ref([])
const taskItems = ref([])
const sparePartItems = ref([])
const showStatusHelp = ref(false)
/** 备件选择弹框目标行下标（-1 表示追加新行） */
const partPickerTargetIndex = ref(-1)
const viewOpen = ref(false)
const viewForm = ref({})
const viewLoading = ref(false)

const defaultColumns = {
  planName: { label: '计划名称', visible: true },
  equipmentName: { label: '关联设备', visible: true },
  triggerType: { label: '触发类型', visible: true },
  cycle: { label: '周期', visible: true },
  estimatedHours: { label: '预估工时', visible: true },
  advanceDays: { label: '提前天数', visible: true },
  autoAssign: { label: '自动派工', visible: true },
  lastExecuteTime: { label: '上次执行', visible: true },
  nextExecuteTime: { label: '下次执行', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_pmplan_columns')
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
const showAdvanced = ref(false)
const dateRange = ref([])
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.planName) count++
  if (queryParams.value.triggerType) count++
  if (queryParams.value.equipmentName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.assigneeName) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})
function triggerTypeLabel(val) { const item = dms_pm_trigger_type.value.find(d => d.value == val); return item ? item.label : '-' }

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, planName: undefined, triggerType: undefined, equipmentName: undefined, status: undefined, assigneeName: undefined, params: {} },
  rules: {
    planName: [{ required: true, message: '计划名称不能为空', trigger: 'blur' }],
    triggerType: [{ required: true, message: '触发类型不能为空', trigger: 'change' }],
    cycleValue: [{ required: true, message: '周期值不能为空', trigger: 'blur' }],
    cycleUnit: [{ required: true, message: '周期单位不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listPmplan(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}
function getEquipmentOptions() {
  listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows })
}
function getUserList() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}
/** 打开备件选择弹窗（index 为 -1 时表示追加新行） */
function openPartPicker(index) {
  partPickerTargetIndex.value = index
  const currentPartId = index >= 0 && sparePartItems.value[index] ? sparePartItems.value[index].partId : undefined
  const excludedIds = sparePartItems.value.filter((_, i) => i !== index).map(i => i.partId).filter(Boolean)
  proxy.$refs.partPickerRef.open(currentPartId, excludedIds)
}
/** 备件选择确认回调 — 自动带出名称和单位 */
function onPartPickerConfirm(part) {
  const idx = partPickerTargetIndex.value
  if (idx >= 0 && idx < sparePartItems.value.length) {
    const row = sparePartItems.value[idx]
    row.partId = part.partId
    row.partName = part.partName
    row.unit = part.unit || ''
  } else {
    sparePartItems.value.push({ partId: part.partId, partName: part.partName, qty: 1, unit: part.unit || '' })
  }
}
/** 清除行内备件 */
function clearSparePart(index) {
  const row = sparePartItems.value[index]
  if (row) {
    row.partId = undefined
    row.partName = ''
    row.unit = ''
  }
}
/** 字典值转单位标签 */
function dictUnitLabel(val) {
  if (!val && val !== '0') return ''
  const dictItem = wms_unit.value.find(d => d.value === val)
  return dictItem ? dictItem.label : val
}
/** 打开设备选择弹窗 */
function openEquipmentPicker() {
  proxy.$refs.equipmentPickerRef.open(form.value.equipmentId)
}
/** 设备选择确认回调 */
function onEquipmentPickerConfirm(eq) {
form.value.equipmentId = eq.equipmentId
form.value.equipmentCode = eq.equipmentCode
form.value.equipmentName = eq.equipmentName
form.value.categoryId = eq.categoryId
form.value.categoryName = eq.categoryName
}
/** 清除设备 */
function clearEquipment() {
form.value.equipmentId = undefined
form.value.equipmentCode = undefined
form.value.equipmentName = undefined
form.value.categoryId = undefined
form.value.categoryName = undefined
}
/** 打开人员选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.assigneeId)
}
/** 人员选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.assigneeId = user.userId
  form.assigneeName = user.nickName
}
/** 删除设备后清空派工人员 */
function onRemoveEquipment() {
  form.value.assigneeId = undefined
  form.value.assigneeName = undefined
}
function handleQuery() { showAdvanced.value = false; queryParams.value.params = proxy.addDateRange(queryParams.value.params, dateRange.value, 'NextExecuteTime'); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.planName = undefined; queryParams.value.triggerType = undefined; queryParams.value.equipmentName = undefined; queryParams.value.status = undefined; queryParams.value.assigneeName = undefined; dateRange.value = []; queryParams.value.params = {}; proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.planId); single.value = selection.length !== 1; multiple.value = !selection.length }

/** 任务清单JSON与数组互转 */
function parseTaskList(str) {
  if (!str) return []
  try {
    const arr = JSON.parse(str)
    if (Array.isArray(arr)) {
      // 兼容字符串数组和对象数组
      return arr.map(item => typeof item === 'string' ? { text: item } : { text: item.text || item.task || item.name || '' })
    }
  } catch (e) {
    // 非JSON，按换行分割
    return str.split('\n').filter(s => s.trim()).map(s => ({ text: s.trim() }))
  }
  return []
}
function taskListToString(items) {
  const valid = items.filter(i => i.text && i.text.trim())
  return valid.length > 0 ? JSON.stringify(valid.map(i => i.text.trim())) : ''
}
/** 备件清单JSON解析为数组 */
function parseSparePartList(str) {
  if (!str) return []
  try {
    const arr = JSON.parse(str)
    if (Array.isArray(arr)) {
      return arr.map(item => ({ partId: item.partId, partName: item.partName || '', qty: item.qty || 1, unit: item.unit || '' }))
    }
  } catch (e) { /* ignore */ }
  return []
}
/** 备件清单数组序列化为JSON */
function sparePartListToString(items) {
  const valid = items.filter(i => i.partId)
  return valid.length > 0 ? JSON.stringify(valid.map(i => ({ partId: i.partId, partName: i.partName, qty: i.qty || 1, unit: i.unit }))) : ''
}

function reset() {
form.value = { planName: undefined, equipmentId: undefined, equipmentCode: undefined, equipmentName: undefined, categoryId: undefined, categoryName: undefined, triggerType: '0', cycleValue: 30, cycleUnit: '天', taskList: undefined, sparePartList: undefined, estimatedHours: undefined, advanceDays: 3, autoAssign: '0', assigneeId: undefined, assigneeName: undefined, status: '0', remark: undefined }
  taskItems.value = []
  sparePartItems.value = []
  proxy.resetForm('pmplanRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增PM计划' }
function handleView(row) {
  viewLoading.value = true
  viewOpen.value = true
  getPmplan(row.planId).then(res => {
    viewForm.value = res.data
    viewForm.value._taskItems = parseTaskList(res.data.taskList)
    viewForm.value._sparePartItems = parseSparePartList(res.data.sparePartList)
    // 按开发规范：分组内字段全为空时自动收缩
    collapsedCards.v3 = !(viewForm.value._taskItems && viewForm.value._taskItems.length > 0)
    collapsedCards.v4 = !(viewForm.value._sparePartItems && viewForm.value._sparePartItems.length > 0)
    collapsedCards.v5 = !viewForm.value.createBy && !viewForm.value.createTime && !viewForm.value.updateBy && !viewForm.value.updateTime && !viewForm.value.remark
    viewLoading.value = false
  }).catch(() => { viewLoading.value = false })
}
function handleUpdate(row) {
  reset()
  getPmplan(row.planId || ids.value[0]).then(res => {
    form.value = res.data
    if (form.value.equipmentId) {
      const equipment = equipmentOptions.value.find(e => e.equipmentId === form.value.equipmentId)
      if (equipment) { form.value.categoryName = equipment.categoryName }
    }
    taskItems.value = parseTaskList(form.value.taskList)
    sparePartItems.value = parseSparePartList(form.value.sparePartList)
    open.value = true
    title.value = '修改PM计划'
  })
}
function submitForm() {
  proxy.$refs['pmplanRef'].validate(valid => {
    if (valid) {
      form.value.taskList = taskListToString(taskItems.value) || undefined
      form.value.sparePartList = sparePartListToString(sparePartItems.value) || undefined
      if (form.value.autoAssign !== '1') {
        form.value.assigneeId = undefined
        form.value.assigneeName = undefined
      }
      if (form.value.planId != undefined) { updatePmplan(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addPmplan(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const planIds = row.planId || ids.value; proxy.$modal.confirm('确认删除编号为"' + planIds + '"的数据？').then(() => delPmplan(planIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleGenerate(row) {
  proxy.$modal.confirm('确认根据计划「' + row.planName + '」生成工单？').then(() => {
    return generateWorkOrder(row.planId)
  }).then(() => {
    proxy.$modal.msgSuccess('工单已生成')
    getList()
  }).catch((err) => {
    if (err !== 'cancel' && err !== undefined && err !== null) {
      proxy.$modal.msgError(typeof err === 'string' ? err : (err.msg || err.message || '生成工单失败'))
    }
  })
}
function handleAutoGenerate() {
  proxy.$modal.confirm('确认立即执行PM计划自动生成？将扫描所有到期计划并自动生成工单。').then(() => {
    return autoGeneratePm()
  }).then((res) => {
    proxy.$modal.msgSuccess(res.msg || '执行完成')
    getList()
  }).catch(() => {})
}
function cancel() { open.value = false; reset() }

// ===== 日历视图 =====
function loadCalendarData() {
  getPmplanCalendar().then(res => { calendarData.value = res.data || [] })
}
/** 按天分组索引，避免每个日历格重复 filter */
const calendarItemsMap = computed(() => {
  const map = {}
  calendarData.value.forEach(item => {
    if (!item.nextExecuteTime) return
    const day = formatDate(item.nextExecuteTime).substring(0, 10)
    ;(map[day] = map[day] || []).push(item)
  })
  return map
})
function getCalendarItems(dayStr) {
  return calendarItemsMap.value[dayStr] || []
}
/** 日历上点击计划：打开详情弹窗（rd 卡片规范），而非修改表单 */
function handleCalendarClick(item) {
  handleView(item)
}
function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}
function isExpired(row) {
  if (!row.nextExecuteTime) return false
  return new Date(row.nextExecuteTime) < new Date()
}
function isExpiredByDate(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

// 监听视图切换
watch(viewMode, (val) => {
  if (val === 'calendar' || val === 'timeline') {
    loadCalendarData()
  }
})

getEquipmentOptions()
getUserList()
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.dms-pmplan-page {
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
.dms-pmplan-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-pmplan-page .filter-card { padding:14px 20px 16px; }
.dms-pmplan-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-pmplan-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-pmplan-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-pmplan-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-pmplan-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-pmplan-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-pmplan-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-pmplan-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-pmplan-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-pmplan-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-pmplan-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-pmplan-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-pmplan-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-pmplan-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-pmplan-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-pmplan-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.dms-pmplan-page .field .control :deep(.el-select) { width:100%; }
.dms-pmplan-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-pmplan-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-pmplan-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.dms-pmplan-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-pmplan-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-pmplan-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-pmplan-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-pmplan-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-pmplan-page .btn-soft .el-icon { font-size:14px; }
.dms-pmplan-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-pmplan-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-pmplan-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-pmplan-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-pmplan-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-pmplan-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-pmplan-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-pmplan-page .table-wrap { overflow-x:auto; }
.dms-pmplan-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-pmplan-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-pmplan-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-pmplan-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-pmplan-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-pmplan-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-pmplan-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-pmplan-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-pmplan-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-pmplan-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-pmplan-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-pmplan-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-pmplan-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-pmplan-page .badge.violet .dot { background:var(--brand-500); }
.dms-pmplan-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-pmplan-page .badge.green .dot { background:var(--green-500); }
.dms-pmplan-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-pmplan-page .badge.gray .dot { background:var(--ink-400); }
.dms-pmplan-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-pmplan-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-pmplan-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-pmplan-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-pmplan-page .pagination-container :deep(.el-pagination .btn-prev), .dms-pmplan-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-pmplan-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-pmplan-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-pmplan-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行 */
.dms-pmplan-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-pmplan-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-pmplan-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-pmplan-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-pmplan-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .dms-pmplan-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-pmplan-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-pmplan-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== 视图切换栏 + 业务操作说明入口 ===== */
.dms-pmplan-page .view-mode-bar { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; flex-wrap:wrap; gap:10px; }
.dms-pmplan-page .tip-pill { display: inline-flex; align-items: center; gap: 5px; height: 30px; padding: 0 10px; background: #fffaf0; border: 1px solid #fde68a; color: #92400e; border-radius: 999px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all .15s var(--ease-out); flex-shrink: 0; white-space: nowrap; }
.dms-pmplan-page .tip-pill:hover { background: var(--amber-50); border-color: var(--amber-500); color: #7c2d12; }
.dms-pmplan-page .tip-pill .el-icon { font-size: 14px; color: var(--amber-700); }

.is-selected {
  color: #1989fa;
  font-weight: bold;
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
.status-help-content .status-flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}
.status-help-content .flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-help-content .flow-arrow {
  color: #909399;
  font-size: 16px;
}
.highlight-card {
  border-radius: 8px;
  padding: 16px;
  border: 1px solid;
}
.highlight-success { background-color: #f0f9ff; border-color: #b3e19d; }
.highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.highlight-success .highlight-card-title { color: #67c23a; }
.highlight-danger .highlight-card-title { color: #f56c6c; }
.highlight-primary .highlight-card-title { color: #409eff; }
.highlight-warning .highlight-card-title { color: #e6a23c; }
.highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
</style>

<style>
/* 新增/修改弹窗 body 滚动 */
.rd-dialog:not(.pmplan-detail-dialog) .el-dialog__body {
  max-height: 72vh;
  overflow-y: auto;
  padding: 16px 20px;
}
/* 详情弹窗 body 滚动 */
.pmplan-detail-dialog .el-dialog__body {
  max-height: 68vh;
  overflow-y: auto;
  padding: 16px 20px;
}
.pmplan-detail-dialog .el-dialog__header {
  padding-bottom: 12px;
}
</style>

<style scoped>
/* ===== 日历视图单元格 ===== */
.cal-cell { height: 100%; display: flex; flex-direction: column; overflow: hidden; }
.cal-day { font-size: 14px; }
.cal-day.is-selected { color: var(--el-color-primary, #409eff); font-weight: 700; }
.cal-plans { display: flex; flex-direction: column; gap: 2px; margin-top: 2px; overflow: hidden; }
.cal-plan {
  font-size: 11px; line-height: 1.5; padding: 1px 4px; border-radius: 3px;
  background: #e8f4ff; color: #409eff;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  cursor: pointer; transition: background .15s ease;
}
.cal-plan:hover { background: #d4ecff; }
.cal-more { font-size: 11px; line-height: 1.5; padding: 1px 4px; color: #909399; cursor: pointer; }
.cal-more:hover { color: #409eff; }
/* 弹出列表（popover 渲染在 body 上，样式需全局） */
</style>

<style>
/* 日历"还有N项"弹出列表（popover 挂在 body，必须非 scoped） */
.cal-pop-list { display: flex; flex-direction: column; gap: 4px; max-height: 260px; overflow-y: auto; }
.cal-pop-item {
  font-size: 13px; padding: 5px 8px; border-radius: 4px;
  background: #f5f7fa; color: #303133;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  cursor: pointer; transition: all .15s ease;
}
.cal-pop-item:hover { background: #e8f4ff; color: #409eff; }
</style>

<style scoped>
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
/* 任务清单样式 */
.task-checklist {
  margin-top: 4px;
}
.task-checklist-body {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.task-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  font-size: 13px;
}
.task-icon-pending {
  color: #c0c4cc;
  font-size: 16px;
}
/* 周期组合控件 */
.cycle-composite {
  display: flex;
  align-items: center;
  gap: 0;
  width: 100%;
}
.cycle-composite :deep(.el-input-number) {
  border-radius: 6px 0 0 6px;
}
.cycle-composite :deep(.el-input-number .el-input__wrapper) {
  border-radius: 6px 0 0 6px;
  box-shadow: 0 0 0 1px var(--ink-200) inset !important;
}
.cycle-composite :deep(.el-input-number.is-focused .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--brand-500) inset !important;
}
.cycle-composite :deep(.el-select .el-select__wrapper) {
  border-radius: 0 6px 6px 0;
  box-shadow: 0 0 0 1px var(--ink-200) inset !important;
  border-left: 0;
  min-height: 32px;
}
.cycle-composite :deep(.el-select.is-focused .el-select__wrapper) {
  box-shadow: 0 0 0 1px var(--brand-500) inset !important;
}
.cycle-suffix {
  margin-left: 12px;
  font-size: 13px;
  color: var(--ink-500);
  font-weight: 500;
  white-space: nowrap;
}
.cycle-suffix::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--brand-500);
  margin-right: 6px;
  vertical-align: middle;
}
</style>
