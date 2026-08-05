<template>
  <div class="app-container dms-inspection-task-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>任务编号</label>
          <div class="control">
            <el-input v-model="queryParams.taskNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>任务状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.taskStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_inspection_status" :key="d.value" :label="d.label" :value="d.value" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs + Tip Pill -->
      <div class="status-tabs">
        <div class="tabs-track"></div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:inspection:task:add']">新增</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:inspection:task:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_inspection_task_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="任务编号" prop="taskNo" key="taskNo" :width="colWidth('taskNo', 180)" resizable v-if="columns.taskNo.visible" />
          <el-table-column label="路线名称" prop="routeName" key="routeName" :width="colWidth('routeName', 200)" resizable show-overflow-tooltip v-if="columns.routeName.visible" />
          <el-table-column label="计划日期" prop="planDate" key="planDate" :width="colWidth('planDate', 130)" resizable align="center" v-if="columns.planDate.visible" />
          <el-table-column label="点检人" prop="inspectorName" key="inspectorName" :width="colWidth('inspectorName', 100)" resizable v-if="columns.inspectorName.visible" />
          <el-table-column label="异常项" prop="abnormalCount" key="abnormalCount" :width="colWidth('abnormalCount', 80)" resizable align="center" v-if="columns.abnormalCount.visible">
            <template #default="scope"><span class="badge" :class="scope.row.abnormalCount > 0 ? 'red' : 'green'">{{ scope.row.abnormalCount || 0 }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="taskStatus" key="taskStatus" :width="colWidth('taskStatus', 100)" resizable align="center" v-if="columns.taskStatus.visible">
            <template #default="scope">
              <span class="badge" :class="taskStatusBadge(scope.row.taskStatus)"><span class="dot"></span>{{ taskStatusLabel(scope.row.taskStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="完成时间" prop="completeTime" key="completeTime" :width="colWidth('completeTime', 180)" resizable align="center" v-if="columns.completeTime.visible" />
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="scope">
              <el-button v-if="scope.row.taskStatus === '0' || scope.row.taskStatus === '1'" link type="primary" icon="VideoPlay" @click="handleExecute(scope.row)">执行点检</el-button>
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:inspection:task:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog inspection-add-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <div class="rd-page">
        <el-form ref="taskRef" :model="form" :rules="rules" label-width="100px">
          <!-- 任务信息卡片 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('add_basic')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
                任务信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.add_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.add_basic">
              <el-row>
                <el-col :span="12"><el-form-item label="任务编号" prop="taskNo"><el-input v-model="form.taskNo" placeholder="自动生成" disabled style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="巡检路线" prop="routeId">
                  <el-select v-model="form.routeId" filterable placeholder="请选择路线" style="width: 100%" @change="onRouteChange">
                    <el-option v-for="r in routeOptions" :key="r.routeId" :label="r.routeName" :value="r.routeId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计划日期" prop="planDate"><el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="点检人" prop="inspectorId">
                  <el-input v-model="form.inspectorName" readonly placeholder="请选择点检人" style="width: 100%" @click="openInspectorPicker">
                    <template #append>
                      <el-button icon="Search" @click="openInspectorPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.inspectorName" class="clear-icon" @click.stop="clearInspector"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 其他信息卡片 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('add_other')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.add_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.add_other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注内容" style="width: 100%" /></el-form-item>
            </div>
          </section>
        </el-form>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog inspection-detail-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">点检详情</span>
          <div class="rd-detail-header-tags" v-if="viewForm.taskStatus != null">
            <dict-tag :options="dms_inspection_status" :value="viewForm.taskStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('insp_basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.insp_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.insp_basic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">任务编号</span><div class="rd-value">{{ viewForm.taskNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">路线名称</span><div class="rd-value">{{ viewForm.routeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ viewForm.planDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">点检人</span><div class="rd-value">{{ viewForm.inspectorName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="dms_inspection_status" :value="viewForm.taskStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">异常项数</span><div class="rd-value">{{ viewForm.abnormalCount || 0 }}</div></div>
              <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewForm.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">完成时间</span><div class="rd-value">{{ viewForm.completeTime || '-' }}</div></div>
            </div>
          </div>
        </section>

        <!-- 通用检查项卡片 -->
        <section class="rd-card" v-if="viewGroups.common.length > 0">
          <div class="rd-card-header" @click="toggleCard('insp_common')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M9 12l2 2 4-4"/></svg></span>
              通用检查项
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.insp_common }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.insp_common">
            <el-table :data="viewGroups.common" border size="small" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" width="60" align="center" type="index" />
              <el-table-column label="检查项" prop="item" />
              <el-table-column label="结果" width="120" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.abnormal" type="danger" size="small">异常</el-tag>
                  <span v-else-if="scope.row.value !== undefined">{{ scope.row.value }}{{ scope.row.unit ? ' ' + scope.row.unit : '' }}</span>
                  <el-tag v-else type="success" size="small">正常</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="异常说明" prop="abnormalDesc" show-overflow-tooltip />
            </el-table>
          </div>
        </section>

        <!-- 设备检查项卡片 -->
        <section class="rd-card" v-for="(dg, di) in viewGroups.devices" :key="di">
          <div class="rd-card-header" @click="toggleCard('insp_device_' + di)">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>
              {{ dg.equipmentName }}
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards['insp_device_' + di] }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards['insp_device_' + di]">
            <el-table :data="dg.items" border size="small" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" width="60" align="center" type="index" />
              <el-table-column label="检查项" prop="item" />
              <el-table-column label="结果" width="120" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.abnormal" type="danger" size="small">异常</el-tag>
                  <span v-else-if="scope.row.value !== undefined">{{ scope.row.value }}{{ scope.row.unit ? ' ' + scope.row.unit : '' }}</span>
                  <el-tag v-else type="success" size="small">正常</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="异常说明" prop="abnormalDesc" show-overflow-tooltip />
            </el-table>
          </div>
        </section>

        <!-- 点检照片卡片 -->
        <section class="rd-card" v-if="viewForm.photoUrls">
          <div class="rd-card-header" @click="toggleCard('insp_photos')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></span>
              点检照片
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.insp_photos }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.insp_photos">
            <div v-for="(url, i) in parsePhotos(viewForm.photoUrls)" :key="i" style="display: inline-block; margin-right: 8px">
              <el-image :src="url" style="width: 120px; height: 120px" fit="cover" :preview-src-list="parsePhotos(viewForm.photoUrls)" />
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 执行点检弹窗 -->
    <el-dialog v-model="execOpen" width="936px" append-to-body draggable class="rd-dialog inspection-exec-dialog" :close-on-click-modal="false" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">执行点检</span>
        </div>
      </template>
      <div class="rd-page">
        <!-- 任务信息卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('exec_basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
              任务信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.exec_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.exec_basic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">任务编号</span><div class="rd-value">{{ execForm.taskNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">路线名称</span><div class="rd-value">{{ execForm.routeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">点检人</span><div class="rd-value">{{ execForm.inspectorName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ execForm.planDate || '-' }}</div></div>
            </div>
          </div>
        </section>

        <!-- 通用检查项卡片 -->
        <section class="rd-card" v-if="execGroups.common.length > 0">
          <div class="rd-card-header" @click="toggleCard('exec_common')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M9 12l2 2 4-4"/></svg></span>
              通用检查项
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.exec_common }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.exec_common">
            <el-table :data="execGroups.common" border style="width: 100%" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" width="60" align="center" type="index" />
              <el-table-column label="检查项" prop="item" min-width="180">
                <template #default="scope"><span style="font-weight: 500">{{ scope.row.item }}</span></template>
              </el-table-column>
              <el-table-column label="类型" width="90" align="center">
                <template #default="scope"><el-tag size="small" :type="scope.row.type === 'number' ? 'warning' : 'info'">{{ typeText(scope.row.type) }}</el-tag></template>
              </el-table-column>
              <el-table-column label="检查结果" min-width="260" align="center">
                <template #default="scope">
                  <div v-if="scope.row.type === 'check'" style="display: flex; align-items: center; justify-content: center; gap: 12px">
                    <el-radio-group v-model="scope.row.result">
                      <el-radio-button value="ok">正常</el-radio-button>
                      <el-radio-button value="abnormal">异常</el-radio-button>
                    </el-radio-group>
                  </div>
                  <div v-else-if="scope.row.type === 'number'" style="display: flex; align-items: center; justify-content: center; gap: 10px">
                    <el-input-number v-model="scope.row.value" :controls="false" style="width: 140px" placeholder="输入数值" />
                    <span style="color: #999; min-width: 30px">{{ scope.row.unit }}</span>
                    <el-checkbox v-model="scope.row.abnormal">异常</el-checkbox>
                  </div>
                  <div v-else style="display: flex; flex-direction: column; gap: 6px">
                    <el-input v-model="scope.row.value" placeholder="输入文本" />
                    <el-checkbox v-model="scope.row.abnormal">标记为异常</el-checkbox>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="异常说明" min-width="240">
                <template #default="scope">
                  <el-input v-model="scope.row.abnormalDesc" type="textarea" :rows="2"
                    :placeholder="(scope.row.abnormal || scope.row.result === 'abnormal') ? '必填：请描述异常情况' : '无异常时可不填'"
                    :required="scope.row.abnormal || scope.row.result === 'abnormal'" />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>

        <!-- 设备检查项卡片 -->
        <section class="rd-card" v-for="(dg, di) in execGroups.devices" :key="di">
          <div class="rd-card-header" @click="toggleCard('exec_device_' + di)">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>
              {{ dg.equipmentName }}
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards['exec_device_' + di] }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards['exec_device_' + di]">
            <el-table :data="dg.items" border style="width: 100%" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" width="60" align="center" type="index" />
              <el-table-column label="检查项" prop="item" min-width="180">
                <template #default="scope"><span style="font-weight: 500">{{ scope.row.item }}</span></template>
              </el-table-column>
              <el-table-column label="类型" width="90" align="center">
                <template #default="scope"><el-tag size="small" :type="scope.row.type === 'number' ? 'warning' : 'info'">{{ typeText(scope.row.type) }}</el-tag></template>
              </el-table-column>
              <el-table-column label="检查结果" min-width="260" align="center">
                <template #default="scope">
                  <div v-if="scope.row.type === 'check'" style="display: flex; align-items: center; justify-content: center; gap: 12px">
                    <el-radio-group v-model="scope.row.result">
                      <el-radio-button value="ok">正常</el-radio-button>
                      <el-radio-button value="abnormal">异常</el-radio-button>
                    </el-radio-group>
                  </div>
                  <div v-else-if="scope.row.type === 'number'" style="display: flex; align-items: center; justify-content: center; gap: 10px">
                    <el-input-number v-model="scope.row.value" :controls="false" style="width: 140px" placeholder="输入数值" />
                    <span style="color: #999; min-width: 30px">{{ scope.row.unit }}</span>
                    <el-checkbox v-model="scope.row.abnormal">异常</el-checkbox>
                  </div>
                  <div v-else style="display: flex; flex-direction: column; gap: 6px">
                    <el-input v-model="scope.row.value" placeholder="输入文本" />
                    <el-checkbox v-model="scope.row.abnormal">标记为异常</el-checkbox>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="异常说明" min-width="240">
                <template #default="scope">
                  <el-input v-model="scope.row.abnormalDesc" type="textarea" :rows="2"
                    :placeholder="(scope.row.abnormal || scope.row.result === 'abnormal') ? '必填：请描述异常情况' : '无异常时可不填'"
                    :required="scope.row.abnormal || scope.row.result === 'abnormal'" />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>

        <div v-if="execGroups.common.length === 0 && execGroups.devices.length === 0" class="rd-empty" style="margin-bottom: 16px">
          <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <p class="rd-empty-text">该路线未配置点检项，请先在巡检路线中配置</p>
        </div>

        <!-- 照片上传卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('exec_photos')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></span>
              现场照片
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.exec_photos }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.exec_photos">
            <el-upload v-model:file-list="execPhotoList" :action="uploadUrl" :headers="uploadHeaders" list-type="picture-card"
              :on-success="handlePhotoSuccess" :before-upload="beforePhotoUpload" accept="image/*" :limit="6">
              <el-icon><Plus /></el-icon>
            </el-upload>
            <p style="color: #999; font-size: 12px; margin-top: 8px">最多上传6张照片，单张不超过10MB，支持 JPG/PNG 格式</p>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitExecute" :loading="execSaving">提交点检结果</el-button>
        <el-button @click="execOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 点检人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择点检人" @confirm="onInspectorPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="点检任务业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">待执行</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">执行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待执行">任务已创建，等待点检人执行。可新增、删除</el-descriptions-item>
          <el-descriptions-item label="执行中">点检人已开始执行点检，系统记录开始时间。可继续填写检查结果</el-descriptions-item>
          <el-descriptions-item label="已完成">点检完成，提交检查结果后自动完成。若有异常项，系统自动生成整改工单</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">异常自动生成工单</div>
              <div class="highlight-card-body">点检结果中标记为异常的检查项，提交后系统<strong>自动生成整改工单</strong>，无需手动创建</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">点检项模板</div>
              <div class="highlight-card-body">点检项从关联的巡检路线<strong>自动带入</strong>，包括通用项和设备专属项，无需手动配置</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">异常说明必填</div>
              <div class="highlight-card-body">标记为异常的检查项<strong>必须填写异常说明</strong>，确保异常情况可追溯</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">现场照片</div>
              <div class="highlight-card-body">执行点检时可上传最多6张现场照片，便于记录设备状态和异常情况</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建任务：</strong>选择巡检路线、设置计划日期和点检人
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>执行点检：</strong>点击「执行点检」开始，系统记录开始时间并加载点检项模板</el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>填写结果：</strong>逐项填写检查结果（正常/异常/数值/文本），异常项填写说明，可上传现场照片</el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>提交完成：</strong>提交后任务完成。若有异常项，系统自动生成整改工单转入工单管理</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsInspectionTask">
import { Plus, CircleClose } from '@element-plus/icons-vue'
import { listTask, getTask, addTask, updateTask, delTask, completeTask, startTask } from '@/api/dms/inspection'
import { listRoute } from '@/api/dms/inspection'
import UserPicker from '@/components/UserPicker/index.vue'
import { getToken } from '@/utils/auth'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['add_basic', 'add_other', 'insp_basic', 'insp_common', 'insp_photos', 'exec_basic', 'exec_common', 'exec_photos'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_inspection_task_index')
const { dms_inspection_status } = proxy.useDict('dms_inspection_status')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const execOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const routeOptions = ref([])
const execSaving = ref(false)
const showStatusHelp = ref(false)

const defaultColumns = {
  taskNo: { label: '任务编号', visible: true },
  routeName: { label: '路线名称', visible: true },
  planDate: { label: '计划日期', visible: true },
  inspectorName: { label: '点检人', visible: true },
  abnormalCount: { label: '异常项', visible: true },
  taskStatus: { label: '状态', visible: true },
  completeTime: { label: '完成时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_inspection_task_columns')
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
  if (queryParams.value.taskNo) count++
  if (queryParams.value.taskStatus !== undefined && queryParams.value.taskStatus !== null && queryParams.value.taskStatus !== '') count++
  return count
})

function taskStatusLabel(val) {
  const item = dms_inspection_status.value.find(d => d.value == val)
  return item ? item.label : '-'
}
function taskStatusBadge(val) {
  const map = { '0': 'amber', '1': 'blue', '2': 'green' }
  return map[val] || 'gray'
}

const viewForm = ref({})
const viewGroups = ref({ common: [], devices: [] })
const execForm = ref({})
const execGroups = ref({ common: [], devices: [] })
const execPhotoList = ref([])
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/common/upload')
const uploadHeaders = ref({ Authorization: 'Bearer ' + getToken() })

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, taskNo: undefined, taskStatus: undefined },
  rules: {
    routeId: [{ required: true, message: '请选择巡检路线', trigger: 'change' }],
    inspectorId: [{ required: true, message: '请选择点检人', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listTask(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function getRouteOptions() { listRoute({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { routeOptions.value = res.rows }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.taskId); single.value = selection.length !== 1; multiple.value = !selection.length }

function reset() {
  form.value = { routeId: undefined, routeName: undefined, planDate: new Date().toISOString().slice(0, 10), inspectorId: undefined, inspectorName: undefined, taskStatus: '0', remark: undefined }
  proxy.resetForm('taskRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增点检任务' }
function onRouteChange(routeId) {
  const route = routeOptions.value.find(r => r.routeId === routeId)
  if (route) form.value.routeName = route.routeName
}
/** 打开点检人选择弹窗 */
function openInspectorPicker() {
  proxy.$refs.userPickerRef.open(form.value.inspectorId)
}
/** 点检人选择确认回调 */
function onInspectorPickerConfirm(user) {
  form.value.inspectorId = user.userId
  form.value.inspectorName = user.nickName
}
/** 清除点检人 */
function clearInspector() {
  form.value.inspectorId = undefined
  form.value.inspectorName = undefined
}

function submitForm() {
  proxy.$refs['taskRef'].validate(valid => {
    if (valid) { addTask(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
  })
}
function handleDelete(row) { const taskIds = row.taskId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delTask(taskIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }

// ===== 查看详情 =====
function handleView(row) {
  getTask(row.taskId).then(res => {
    viewForm.value = res.data
    viewGroups.value = { common: [], devices: [] }
    if (res.data.resultData) {
      try {
        const parsed = JSON.parse(res.data.resultData)
        // 兼容旧格式（扁平数组）
        if (Array.isArray(parsed)) {
          viewGroups.value = { common: parsed, devices: [] }
        } else {
          viewGroups.value = {
            common: parsed.common || [],
            devices: parsed.devices || []
          }
        }
      } catch (e) { viewGroups.value = { common: [], devices: [] } }
    }
    viewOpen.value = true
  })
}

// ===== 执行点检 =====
function handleExecute(row) {
  // 调用后端开始接口，记录开始时间并将状态改为执行中
  startTask(row.taskId).then(() => {
    getTask(row.taskId).then(res => {
      execForm.value = res.data
      execPhotoList.value = []
      execGroups.value = { common: [], devices: [] }

      // 从路线获取点检项模板
      const route = routeOptions.value.find(r => r.routeId === res.data.routeId)
      if (route && route.inspectionItems) {
        try {
          const parsed = JSON.parse(route.inspectionItems)
          // 兼容旧格式（扁平数组）
          if (Array.isArray(parsed)) {
            execGroups.value = { common: initItems(parsed), devices: [] }
          } else {
            execGroups.value = {
              common: initItems(parsed.common || []),
              devices: (parsed.devices || []).map(d => ({
                equipmentId: d.equipmentId,
                equipmentName: d.equipmentName,
                items: initItems(d.items || [])
              }))
            }
          }
        } catch (e) { execGroups.value = { common: [], devices: [] } }
      }
      // 如果没有模板，给一个默认通用项
      if (execGroups.value.common.length === 0 && execGroups.value.devices.length === 0) {
        execGroups.value.common = [{ item: '通用检查', type: 'check', unit: '', result: 'ok', value: undefined, abnormal: false, abnormalDesc: '' }]
      }

      // 恢复之前的结果数据
      if (res.data.resultData) {
        try {
          const prev = JSON.parse(res.data.resultData)
          if (Array.isArray(prev)) {
            prev.forEach((r, i) => { if (execGroups.value.common[i]) restoreItem(execGroups.value.common[i], r) })
          } else {
            (prev.common || []).forEach((r, i) => { if (execGroups.value.common[i]) restoreItem(execGroups.value.common[i], r) })
            ;(prev.devices || []).forEach((d, di) => {
              if (execGroups.value.devices[di]) {
                d.items.forEach((r, ii) => { if (execGroups.value.devices[di].items[ii]) restoreItem(execGroups.value.devices[di].items[ii], r) })
              }
            })
          }
        } catch (e) {}
      }

      execOpen.value = true
      // 刷新列表以显示状态变化
      getList()
    })
  }).catch(() => {})
}

function initItems(items) {
  return items.map(item => ({
    item: item.item || '',
    type: item.type || 'check',
    unit: item.unit || '',
    abnormalRequired: item.abnormalRequired || false,
    result: 'ok',
    value: undefined,
    abnormal: false,
    abnormalDesc: ''
  }))
}
function restoreItem(target, source) {
  target.result = source.result || (source.abnormal ? 'abnormal' : 'ok')
  target.value = source.value
  target.abnormal = source.abnormal || false
  target.abnormalDesc = source.abnormalDesc || ''
}

function handlePhotoSuccess(res) {}
function beforePhotoUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) { proxy.$modal.msgError('只能上传图片文件'); return false }
  if (!isLt10M) { proxy.$modal.msgError('图片大小不能超过10MB'); return false }
  return true
}

function submitExecute() {
  let abnormalCount = 0
  // 校验所有组的检查结果必填
  const allGroups = [...execGroups.value.common, ...execGroups.value.devices.flatMap(d => d.items)]
  for (const item of allGroups) {
    // 检查结果必填校验
    if (item.type === 'check') {
      if (!item.result) {
        proxy.$modal.msgError(`检查项「${item.item}」请选择检查结果（正常/异常）`)
        return
      }
    } else if (item.type === 'number') {
      if (item.value === undefined || item.value === null || item.value === '') {
        proxy.$modal.msgError(`检查项「${item.item}」请填写检查数值`)
        return
      }
    } else {
      if (item.value === undefined || item.value === null || item.value === '') {
        proxy.$modal.msgError(`检查项「${item.item}」请填写检查结果`)
        return
      }
    }
    // 异常项必须有异常说明
    const isAbnormal = item.abnormal || item.result === 'abnormal'
    if (isAbnormal) {
      abnormalCount++
      if (!item.abnormalDesc || item.abnormalDesc.trim() === '') {
        proxy.$modal.msgError(`检查项「${item.item}」标记为异常，请填写异常说明`)
        return
      }
    }
  }

  execSaving.value = true
  // 构建嵌套结构的结果数据
  const resultData = {
    common: execGroups.value.common.map(item => ({
      item: item.item, type: item.type, unit: item.unit,
      result: item.result, value: item.value,
      abnormal: item.abnormal || item.result === 'abnormal',
      abnormalDesc: item.abnormalDesc
    })),
    devices: execGroups.value.devices.map(dg => ({
      equipmentId: dg.equipmentId,
      equipmentName: dg.equipmentName,
      items: dg.items.map(item => ({
        item: item.item, type: item.type, unit: item.unit,
        result: item.result, value: item.value,
        abnormal: item.abnormal || item.result === 'abnormal',
        abnormalDesc: item.abnormalDesc
      }))
    }))
  }

  const photoUrls = execPhotoList.value.map(f => f.response?.fileName || f.response?.url || f.url).filter(u => u)

  const submitData = {
    taskId: execForm.value.taskId,
    taskNo: execForm.value.taskNo,
    taskStatus: '2',
    abnormalCount: abnormalCount,
    resultData: JSON.stringify(resultData),
    photoUrls: photoUrls.length > 0 ? JSON.stringify(photoUrls) : null,
    inspectorId: execForm.value.inspectorId,
    inspectorName: execForm.value.inspectorName
  }

  completeTask(submitData).then(() => {
    proxy.$modal.msgSuccess(abnormalCount > 0 ? `点检完成，发现${abnormalCount}个异常项，已自动生成整改工单` : '点检完成，一切正常')
    execOpen.value = false
    execSaving.value = false
    getList()
  }).catch(() => { execSaving.value = false })
}

function typeText(type) { return { check: '打勾', number: '数值', text: '文本' }[type] || type }
function parsePhotos(photoUrls) {
  if (!photoUrls) return []
  try {
    const urls = JSON.parse(photoUrls)
    const baseApi = import.meta.env.VITE_APP_BASE_API
    return urls.map(u => {
      if (u && !u.startsWith('http') && !u.startsWith('data:')) return baseApi + u
      return u
    })
  } catch (e) { return [] }
}

getRouteOptions()
getList()
</script>

<style scoped>
.dms-inspection-task-page {
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
.dms-inspection-task-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-inspection-task-page .filter-card { padding:14px 20px 16px; }
.dms-inspection-task-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-inspection-task-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-inspection-task-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-inspection-task-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-inspection-task-page .filter-card .field { display:flex; flex-direction:column; gap:4px; }
.dms-inspection-task-page .filter-card .field > label { font-size:13px; font-weight:500; color:var(--ink-500); }
.dms-inspection-task-page .filter-card .field .control { width:100%; }
.dms-inspection-task-page .filter-card .field .control .el-input, .dms-inspection-task-page .filter-card .field .control .el-select { width:100%; }
.dms-inspection-task-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-inspection-task-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-inspection-task-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-inspection-task-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.dms-inspection-task-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.dms-inspection-task-page .tabs-track::-webkit-scrollbar { display:none; }
.dms-inspection-task-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.dms-inspection-task-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.dms-inspection-task-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.dms-inspection-task-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 16px; border-bottom:1px solid var(--ink-200); }
.dms-inspection-task-page .toolbar .left { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.dms-inspection-task-page .toolbar .right { display:flex; align-items:center; gap:8px; }
.dms-inspection-task-page .toolbar-divider { width:1px; height:20px; background:var(--ink-200); margin:0 4px; }
.dms-inspection-task-page .btn-soft { display:inline-flex; align-items:center; gap:4px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; cursor:pointer; transition:all .15s var(--ease-out); border:1px solid transparent; background:transparent; color:var(--ink-700); }
.dms-inspection-task-page .btn-soft.is-outline { border-color:var(--ink-300); background:#fff; }
.dms-inspection-task-page .btn-soft.is-outline:hover { border-color:var(--brand-400); color:var(--brand-600); background:var(--brand-50); }
.dms-inspection-task-page .btn-soft.is-danger-outline { border-color:#fecaca; background:var(--red-50); color:var(--red-700); }
.dms-inspection-task-page .btn-soft.is-danger-outline:hover { background:var(--red-500); color:#fff; border-color:var(--red-500); }
.dms-inspection-task-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-inspection-task-page .table-wrap { overflow-x:auto; }
.dms-inspection-task-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-inspection-task-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-inspection-task-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-inspection-task-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-inspection-task-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-inspection-task-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-inspection-task-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-inspection-task-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-inspection-task-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-inspection-task-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-inspection-task-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-inspection-task-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-inspection-task-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-inspection-task-page .badge.amber .dot { background:var(--amber-500); }
.dms-inspection-task-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-inspection-task-page .badge.blue .dot { background:var(--blue-500); }
.dms-inspection-task-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-inspection-task-page .badge.green .dot { background:var(--green-500); }
.dms-inspection-task-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-inspection-task-page .badge.red .dot { background:var(--red-500); }
.dms-inspection-task-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-inspection-task-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-inspection-task-page .badge.gray .dot { background:var(--ink-400); }
@media (max-width:1100px) { .dms-inspection-task-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-inspection-task-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-inspection-task-page .toolbar { flex-wrap:wrap; gap:10px; } }
.dms-inspection-task-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-inspection-task-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-inspection-task-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-inspection-task-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-inspection-task-page .pagination-container :deep(.el-pagination .btn-prev), .dms-inspection-task-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-inspection-task-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-inspection-task-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-inspection-task-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
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
:deep(.el-input.is-disabled .el-input__inner) {
  cursor: pointer;
}
</style>
