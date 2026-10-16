<template>
  <div class="app-container wms-list-page">
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
          <label>控制点</label>
          <div class="control is-select">
            <el-select v-model="queryParams.ccpName" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="name in ccpNameOptions" :key="name" :label="name" :value="name" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>判定结果</label>
          <div class="control is-select">
            <el-select v-model="queryParams.judgeResult" placeholder="全部" clearable @change="onJudgeFilterChange">
              <el-option v-for="d in food_ccp_judge" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工序名称</label>
          <div class="control">
            <el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>垛号</label>
          <div class="control">
            <el-input v-model="queryParams.wipNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>监控项类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.monitorType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_ccp_monitor_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>记录时间</label>
          <div class="control is-date">
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon><span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['food:ccp:add']">录入</el-button>
          <el-button type="info" plain icon="Setting" @click="openTplDialog" v-hasPermi="['food:ccp:template']">模板设置</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['food:ccp:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="food_ccp_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="recordList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="记录编号" prop="recordNo" key="recordNo" :width="colWidth('recordNo', 140)" resizable v-if="columns.recordNo.visible">
            <template #default="scope"><span class="mono">{{ scope.row.recordNo }}</span></template>
          </el-table-column>
          <el-table-column label="记录时间" prop="recordTime" key="recordTime" :width="colWidth('recordTime', 150)" resizable align="center" v-if="columns.recordTime.visible">
            <template #default="scope">{{ parseTime(scope.row.recordTime) }}</template>
          </el-table-column>
          <el-table-column label="控制点" prop="ccpName" key="ccpName" :min-width="colWidth('ccpName', 130)" resizable show-overflow-tooltip v-if="columns.ccpName.visible" />
          <el-table-column label="工序" prop="processName" key="processName" :width="colWidth('processName', 120)" resizable show-overflow-tooltip v-if="columns.processName.visible" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable show-overflow-tooltip v-if="columns.workOrderNo.visible" />
          <el-table-column label="垛号" prop="wipNo" key="wipNo" :width="colWidth('wipNo', 130)" resizable show-overflow-tooltip v-if="columns.wipNo.visible" />
          <el-table-column label="批次号" prop="batchCode" key="batchCode" :width="colWidth('batchCode', 150)" resizable show-overflow-tooltip v-if="columns.batchCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :min-width="colWidth('materialName', 140)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="监控值" prop="monitorValue" key="monitorValue" :width="colWidth('monitorValue', 120)" resizable align="center" v-if="columns.monitorValue.visible">
            <template #default="scope">
              <span v-if="monitorText(scope.row) !== '' " class="mono">{{ monitorText(scope.row) }}</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="关键限值" prop="limitSnapshot" key="limitSnapshot" :width="colWidth('limitSnapshot', 110)" resizable align="center" v-if="columns.limitSnapshot.visible">
            <template #default="scope"><span class="mono">{{ scope.row.limitSnapshot }}</span></template>
          </el-table-column>
          <el-table-column label="判定结果" prop="judgeResult" key="judgeResult" :width="colWidth('judgeResult', 90)" resizable align="center" v-if="columns.judgeResult.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.judgeResult === '1' ? 'red' : 'green'"><span class="dot"></span>{{ dictLabel(food_ccp_judge, scope.row.judgeResult) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="纠正措施" prop="corrective" key="corrective" :min-width="colWidth('corrective', 160)" resizable show-overflow-tooltip v-if="columns.corrective.visible" />
          <el-table-column label="联动NCR" prop="ncrNo" key="ncrNo" :width="colWidth('ncrNo', 130)" resizable show-overflow-tooltip v-if="columns.ncrNo.visible" />
          <el-table-column label="记录人" prop="recorder" key="recorder" :width="colWidth('recorder', 90)" resizable align="center" v-if="columns.recorder.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 90)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="table-pager">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 录入弹框 ===== -->
    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog" @closed="cancel">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">CCP监控记录录入</span>
        </div>
      </template>
      <el-form ref="ccpRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>监控信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="控制点" prop="templateId">
                <el-select v-model="form.templateId" placeholder="请选择CCP控制点模板" style="width: 100%" @change="onTplChange">
                  <el-option v-for="t in templateList" :key="t.templateId" :label="t.processName + ' - ' + t.ccpName" :value="t.templateId" />
                </el-select>
                <div class="form-tip" v-if="selectedTpl">
                  关键限值：<b>{{ tplLimitText(selectedTpl) }}</b>
                  <template v-if="selectedTpl.monitorFreq">｜监控频率：{{ selectedTpl.monitorFreq }}</template>
                  <template v-if="localJudge === '1'">｜<span class="text-danger">监控值超出限值，须填写纠正措施并自动联动不合格品</span></template>
                </div>
              </el-form-item>
              <el-form-item label="监控值" prop="monitorInput">
                <el-input-number v-if="!selectedTpl || selectedTpl.monitorType !== '4'" v-model="form.monitorInput" :precision="2" :step="0.5" controls-position="right" style="width: 100%" :placeholder="monitorPlaceholder" />
                <el-select v-else v-model="form.moldStatus" placeholder="请选择上霉状态" style="width: 100%">
                  <el-option v-for="d in food_mold_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="记录时间" prop="recordTime">
                <el-date-picker v-model="form.recordTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="默认当前时间" style="width: 100%" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></span>关联信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="批次号" prop="batchId">
                <el-input :model-value="form.batchCode" readonly placeholder="点击选择食品批次（可选，超限将自动冻结该批次）" @click="openBatchPicker">
                  <template v-if="form.batchId" #append><el-button icon="CircleClose" @click.stop="clearBatch" /></template>
                  <template v-else #append><el-button icon="Search" @click="openBatchPicker" /></template>
                </el-input>
              </el-form-item>
              <el-form-item label="物料" prop="materialId">
                <el-input :model-value="[form.materialCode, form.materialName].filter(Boolean).join(' - ')" readonly placeholder="选择批次后自动带出；无批次时点击选择物料" @click="openMaterialPicker">
                  <template v-if="form.materialId" #append><el-button icon="CircleClose" @click.stop="clearMaterial" /></template>
                  <template v-else #append><el-button icon="Search" @click="openMaterialPicker" /></template>
                </el-input>
              </el-form-item>
              <el-form-item label="工单号" prop="workOrderNo">
                <el-input v-model="form.workOrderNo" placeholder="选填，如 WO202610140002" maxlength="50" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>偏差与纠正</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-form-item label="偏差说明" prop="overNote">
                <el-input v-model="form.overNote" type="textarea" :rows="2" placeholder="偏差/超限情况说明（超限时建议填写）" maxlength="500" />
              </el-form-item>
              <el-form-item label="纠正措施" prop="corrective">
                <el-input v-model="form.corrective" type="textarea" :rows="2" placeholder="超限时必填；未超限可不填" maxlength="500" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 详情弹框 ===== -->
    <el-dialog v-model="detailOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">CCP记录详情</span>
          <div class="rd-detail-header-sub" v-if="detail.recordNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ detail.recordNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page" v-loading="detailLoading">
        <!-- 记录信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>记录信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.d1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">记录时间</span><div class="rd-value">{{ parseTime(detail.recordTime) || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">记录人</span><div class="rd-value">{{ detail.recorder || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">控制点</span><div class="rd-value">{{ detail.ccpName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工序</span><div class="rd-value">{{ detail.processName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">监控项</span><div class="rd-value">{{ dictLabel(food_ccp_monitor_type, detail.monitorType) }}</div></div>
              <div class="rd-item"><span class="rd-label">监控值</span><div class="rd-value mono">{{ monitorText(detail) || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">关键限值</span><div class="rd-value mono">{{ detail.limitSnapshot || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">判定结果</span><div class="rd-value"><span v-if="detail.judgeResult" class="badge" :class="detail.judgeResult === '1' ? 'red' : 'green'"><span class="dot"></span>{{ dictLabel(food_ccp_judge, detail.judgeResult) }}</span><span v-else class="text-muted">—</span></div></div>
            </div>
          </div>
        </section>
        <!-- 关联信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></span>关联信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.d2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ detail.batchCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料</span><div class="rd-value">{{ [detail.materialCode, detail.materialName].filter(Boolean).join(' - ') || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ detail.workOrderNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">垛号</span><div class="rd-value">{{ detail.wipNo || '—' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 偏差与纠正 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>偏差与纠正</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.d3">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">偏差说明</span><div class="rd-value" :class="{ 'rd-value--muted': !detail.overNote }">{{ detail.overNote || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">纠正措施</span><div class="rd-value" :class="{ 'rd-value--muted': !detail.corrective }">{{ detail.corrective || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">联动NCR</span><div class="rd-value mono">{{ detail.ncrNo || '—' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" plain icon="Edit" @click="openEdit" v-hasPermi="['food:ccp:add']">补充偏差/纠正</el-button>
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 补充记录弹框 ===== -->
    <el-dialog v-model="editOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div><span class="rd-detail-header-title">补充偏差说明/纠正措施</span></div></template>
      <el-form label-width="90px">
        <el-form-item label="偏差说明"><el-input v-model="editForm.overNote" type="textarea" :rows="2" maxlength="500" /></el-form-item>
        <el-form-item label="纠正措施"><el-input v-model="editForm.corrective" type="textarea" :rows="2" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editOpen = false">取 消</el-button><el-button type="primary" @click="submitEdit">保 存</el-button></template>
    </el-dialog>

    <!-- ===== 模板设置弹框 ===== -->
    <el-dialog v-model="tplDialogOpen" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></div>
          <span class="rd-detail-header-title">CCP控制点模板设置</span>
        </div>
      </template>
      <div class="toolbar" style="padding: 0 0 10px 0">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleTplAdd">新增模板</el-button>
        </div>
      </div>
      <el-table v-loading="tplLoading" :data="templateList" border size="small" max-height="420">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="控制点" prop="ccpName" min-width="130" show-overflow-tooltip />
        <el-table-column label="工序" prop="processName" width="110" show-overflow-tooltip />
        <el-table-column label="工艺路线" prop="routeName" width="120" show-overflow-tooltip />
        <el-table-column label="监控项" prop="monitorType" width="90" align="center">
          <template #default="scope"><span class="badge blue"><span class="dot"></span>{{ dictLabel(food_ccp_monitor_type, scope.row.monitorType) }}</span></template>
        </el-table-column>
        <el-table-column label="关键限值" width="120" align="center">
          <template #default="scope"><span class="mono">{{ tplLimitText(scope.row) }}</span></template>
        </el-table-column>
        <el-table-column label="监控频率" prop="monitorFreq" width="100" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="scope"><span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleTplUpdate(scope.row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleTplDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer><el-button @click="tplDialogOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 模板编辑弹框 ===== -->
    <el-dialog v-model="tplFormOpen" width="680px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div><span class="rd-detail-header-title">{{ tplTitle }}</span></div></template>
      <el-form ref="tplRef" :model="tplForm" :rules="tplRules" label-width="100px">
        <div class="rd-page">
          <!-- 基础信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('t1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基础信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.t1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.t1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="控制点名称" prop="ccpName"><el-input v-model="tplForm.ccpName" placeholder="如：腌制间温度" maxlength="100" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工序名称" prop="processName"><el-input v-model="tplForm.processName" placeholder="如：三次上盐腌制" maxlength="100" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="模板名称" prop="templateName"><el-input v-model="tplForm.templateName" placeholder="如：干腌火腿CCP" maxlength="100" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工艺路线" prop="routeName"><el-input v-model="tplForm.routeName" placeholder="如：干腌火腿工艺路线" maxlength="100" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 监控与限值 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('t2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>监控与限值</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.t2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.t2">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="监控项类型" prop="monitorType">
                    <el-select v-model="tplForm.monitorType" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in food_ccp_monitor_type" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="限值类型" prop="limitType">
                    <el-select v-model="tplForm.limitType" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in food_ccp_limit_type" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col v-if="tplForm.limitType === '1' || tplForm.limitType === '2'" :span="12"><el-form-item label="限值下限" prop="limitMin"><el-input-number v-model="tplForm.limitMin" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col v-if="tplForm.limitType === '1' || tplForm.limitType === '3'" :span="12"><el-form-item label="限值上限" prop="limitMax"><el-input-number v-model="tplForm.limitMax" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col v-if="tplForm.limitType === '4'" :span="12">
                  <el-form-item label="枚举限值" prop="limitValue">
                    <el-select v-model="tplForm.limitValue" placeholder="限定枚举值" style="width: 100%">
                      <el-option v-for="d in food_mold_status" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12"><el-form-item label="单位" prop="unit"><el-input v-model="tplForm.unit" placeholder="如 ℃ / %" maxlength="20" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 监控要求 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('t3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>监控要求</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.t3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.t3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="监控频率" prop="monitorFreq"><el-input v-model="tplForm.monitorFreq" placeholder="如：每日2次" maxlength="50" /></el-form-item></el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="tplForm.status">
                      <el-radio value="0">正常</el-radio>
                      <el-radio value="1">停用</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="24"><el-form-item label="纠正措施" prop="correctiveAction"><el-input v-model="tplForm.correctiveAction" type="textarea" :rows="2" placeholder="超限时的默认纠正措施，录入时自动带出" maxlength="500" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="tplForm.remark" type="textarea" :rows="2" maxlength="500" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button @click="tplFormOpen = false">取 消</el-button><el-button type="primary" @click="submitTpl">确 定</el-button></template>
    </el-dialog>

    <!-- 批次选择器 / 物料选择器 -->
    <batch-picker ref="batchPickerRef" title="选择批次" @confirm="onBatchPickerConfirm" />
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="CCP电子台账业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、什么是CCP电子台账</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">CCP是什么？</div>
          <div class="highlight-card-body">CCP全称 Critical Control Point（关键控制点）。用大白话说：就是食品加工过程中<strong>最容易出食品安全问题、必须重点盯防的关键环节</strong>。比如腌火腿时盐要撒够、腌制间温度要达标、发酵间温湿度要合适、表面不能上霉——这些环节一旦没控制住，做出来的产品就可能不安全。所以每个环节都要定一条"合格标准"（即关键限值），工人班次班次地记录检查数据</div>
        </div>
        <div class="highlight-card highlight-success" style="margin-top: 12px;">
          <div class="highlight-card-title">CCP电子台账是什么？</div>
          <div class="highlight-card-body">就是把这些关键环节的日常检查记录<strong>从纸质台账搬到系统里</strong>：工人按时录入温度、湿度、盐量、感官检查结果，系统自动对照"合格标准"判断<strong>达标还是超标</strong>；一旦超标，系统自动要求填写纠正措施、自动开出问题处理单、自动锁住相关批次不许发货。记录改不了也删不掉，随时能按时间段导出——客户验厂、官方检查时直接拿出来，就是一份完整的食品安全监控证据。这套做法来自国际通用的食品安全管理方法HACCP，核心思路是<strong>提前防住问题，而不是事后检验才发现</strong></div>
        </div>

        <h4>二、判定结果说明</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="success">合格</el-tag>
            <span class="flow-note">监控值在关键限值范围内，记录归档</span>
          </div>
        </div>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="danger">超限</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="warning">必填纠正措施</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="danger">自动建NCR＋冻批</el-tag>
          </div>
          <span class="flow-note">NCR＝不合格品处理单；冻批＝相关批次立即锁定、禁止出库发货</span>
        </div>

        <h4>三、检查什么、标准怎么定</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">监控项类型＝检查什么内容</div>
              <div class="highlight-card-body"><strong>温度(℃)、湿度(%)、盐量比例(%)</strong>这三类直接填数字；<strong>感官检查</strong>类（比如上霉状态）不用填数，直接选选项（如：无上霉/轻微上霉/严重上霉）</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">限值类型＝合格标准怎么写</div>
              <div class="highlight-card-body">共4种写法：<strong>区间</strong>（最低~最高之间才算合格）、<strong>不低于</strong>（只卡下限）、<strong>不高于</strong>（只卡上限）、<strong>指定选项</strong>（感官类用）。录入时系统自动对照判断达标还是超标，不用人工心算</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、超限了系统会自动做什么</h4>
        <div class="highlight-card highlight-danger">
          <div class="highlight-card-title">超标自动联动，不用人工再跑流程</div>
          <div class="highlight-card-body">录入的监控值超出"合格标准"时：<strong>①必须填写纠正措施</strong>（比如调整温度、补加盐量），不填就存不了，防止漏处理；<strong>②系统自动开出一张"问题处理单"</strong>（系统里叫NCR），单号自动回填到这条记录上，质量部门接手处理；<strong>③相关批次自动锁定</strong>，在办完处理前不让出库发货，防止问题产品流出；<strong>④记录改不了也删不掉</strong>，每一步都有痕迹。问题处理单最后定为"报废"还是"退货"后，对应批次的状态也会跟着自动更新</div>
        </div>

        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>第一步 定标准：</strong>在「模板设置」里给每个关键环节定好规则——属于哪条工艺路线哪个工序、检查什么内容、合格标准是多少、多久检查一次、超标时默认怎么处理
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>第二步 日常记录：</strong>点「录入」，选好要检查的环节，如实填写当时的检查数据，系统自动判断达标还是超标；建议关联上批次/工单/垛号，以后出了问题方便追查
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>第三步 处理超标：</strong>超标的记录要写清楚哪里出了问题、怎么纠正的；当时没写完的，也可以在详情页点「补充偏差/纠正」补上
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>第四步 导出备查：</strong>客户验厂或官方检查时，选好时间段导出，就是一份完整的关键环节监控记录
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FoodCcp">
import { listCcpTemplate, addCcpTemplate, updateCcpTemplate, delCcpTemplate, listCcpRecord, getCcpRecord, addCcpRecord, updateCcpRecord, ccpStats } from "@/api/food/ccp"
import BatchPicker from '@/components/BatchPicker/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2', 'c3', 'd1', 'd2', 'd3', 't1', 't2', 't3'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('food_ccp_index')
const { food_ccp_judge, food_ccp_monitor_type, food_ccp_limit_type, food_mold_status } = proxy.useDict("food_ccp_judge", "food_ccp_monitor_type", "food_ccp_limit_type", "food_mold_status")

// 列显隐配置
const defaultColumns = {
  recordNo: { label: '记录编号', visible: true },
  recordTime: { label: '记录时间', visible: true },
  ccpName: { label: '控制点', visible: true },
  processName: { label: '工序', visible: true },
  workOrderNo: { label: '工单号', visible: true },
  wipNo: { label: '垛号', visible: true },
  batchCode: { label: '批次号', visible: true },
  materialName: { label: '物料名称', visible: true },
  monitorValue: { label: '监控值', visible: true },
  limitSnapshot: { label: '关键限值', visible: true },
  judgeResult: { label: '判定结果', visible: true },
  corrective: { label: '纠正措施', visible: true },
  ncrNo: { label: '联动NCR', visible: true },
  recorder: { label: '记录人', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('food_ccp_columns')
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

const recordList = ref([])
const templateList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref({})
const editOpen = ref(false)
const editForm = reactive({ recordId: null, overNote: undefined, corrective: undefined })
const tplDialogOpen = ref(false)
const tplLoading = ref(false)
const tplFormOpen = ref(false)
const tplTitle = ref("")
const batchPickerRef = ref(null)
const materialPickerRef = ref(null)

const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const showStatusHelp = ref(false)
const statusTabList = computed(() => food_ccp_judge.value)
function statusTabClass(value) { const map = { '0': 'tab-done', '1': 'tab-reject' }; return map[value] || '' }
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.judgeResult = status === 'all' ? undefined : status
  handleQuery()
}
function loadStatusCounts() {
  ccpStats({}).then(res => {
    const d = res.data || {}
    statusCounts.value = { all: d.total || 0, '0': d.pass || 0, '1': d.over || 0 }
  }).catch(() => {})
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, ccpName: undefined, batchCode: undefined, workOrderNo: undefined, judgeResult: undefined, processName: undefined, wipNo: undefined, materialName: undefined, monitorType: undefined },
  rules: {
    templateId: [{ required: true, message: "请选择CCP控制点模板", trigger: "change" }],
    recordTime: [{ required: true, message: "记录时间不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const tplData = reactive({
  tplForm: {},
  tplRules: {
    ccpName: [{ required: true, message: "控制点名称不能为空", trigger: "blur" }],
    monitorType: [{ required: true, message: "请选择监控项类型", trigger: "change" }],
    limitType: [{ required: true, message: "请选择限值类型", trigger: "change" }]
  }
})
const { tplForm, tplRules } = toRefs(tplData)

const ccpNameOptions = computed(() => [...new Set(templateList.value.map(t => t.ccpName).filter(Boolean))])
const selectedTpl = computed(() => templateList.value.find(t => t.templateId === form.value.templateId) || null)
const monitorPlaceholder = computed(() => {
  if (!selectedTpl.value) return '请先选择控制点'
  const type = selectedTpl.value.monitorType
  const unit = selectedTpl.value.unit || ''
  if (type === '1') return '请输入温度(℃)'
  if (type === '2') return '请输入湿度(%)'
  if (type === '3') return '请输入盐量比例(%)'
  return '请输入监控值' + (unit ? '(' + unit + ')' : '')
})
const localJudge = computed(() => {
  const tpl = selectedTpl.value
  if (!tpl) return null
  if (tpl.limitType === '4') {
    if (!form.value.moldStatus) return null
    return tpl.limitValue == form.value.moldStatus ? '0' : '1'
  }
  const v = form.value.monitorInput
  if (v == null) return null
  const n = Number(v)
  if (tpl.limitType === '2') return n < tpl.limitMin ? '1' : '0'
  if (tpl.limitType === '3') return n > tpl.limitMax ? '1' : '0'
  return (n < tpl.limitMin || n > tpl.limitMax) ? '1' : '0'
})

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.ccpName) count++
  if (queryParams.value.batchCode) count++
  if (queryParams.value.workOrderNo) count++
  if (queryParams.value.judgeResult) count++
  if (queryParams.value.processName) count++
  if (queryParams.value.wipNo) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.monitorType) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function loadTemplates(callback) {
  tplLoading.value = true
  listCcpTemplate({ pageNum: 1, pageSize: 200, status: '0' }).then(res => {
    templateList.value = res.rows || []
    tplLoading.value = false
    if (callback) callback()
  }).catch(() => { tplLoading.value = false })
}

function getList() {
  loading.value = true
  listCcpRecord(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    recordList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  showAdvanced.value = false
  getList()
}

function onJudgeFilterChange(v) {
  activeStatusTab.value = v || 'all'
  handleQuery()
}

function resetQuery() {
  queryParams.value.ccpName = undefined
  queryParams.value.batchCode = undefined
  queryParams.value.workOrderNo = undefined
  queryParams.value.judgeResult = undefined
  queryParams.value.processName = undefined
  queryParams.value.wipNo = undefined
  queryParams.value.materialName = undefined
  queryParams.value.monitorType = undefined
  dateRange.value = []
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.recordId)
}

/** 表单重置 */
function reset() {
  form.value = { recordId: null, templateId: null, recordTime: undefined, monitorInput: undefined, moldStatus: undefined, batchId: null, batchCode: undefined, materialId: null, materialCode: undefined, materialName: undefined, workOrderNo: undefined, overNote: undefined, corrective: undefined, remark: undefined }
  proxy.resetForm("ccpRef")
}

function handleAdd() {
  reset()
  open.value = true
}

function cancel() {
  open.value = false
  reset()
}

function onTplChange(templateId) {
  const tpl = templateList.value.find(t => t.templateId === templateId)
  if (tpl && !form.value.corrective && tpl.correctiveAction) {
    form.value.corrective = tpl.correctiveAction
  }
}

function submitForm() {
  proxy.$refs["ccpRef"].validate(valid => {
    if (valid) {
      if (selectedTpl.value && selectedTpl.value.monitorType !== '4' && form.value.monitorInput == null) {
        proxy.$modal.msgWarning("请填写监控值")
        return
      }
      if (selectedTpl.value && selectedTpl.value.monitorType === '4' && !form.value.moldStatus) {
        proxy.$modal.msgWarning("请选择上霉状态")
        return
      }
      if (localJudge.value === '1' && !form.value.corrective) {
        proxy.$modal.msgWarning("监控值超出关键限值，必须填写纠正措施")
        return
      }
      const payload = { ...form.value }
      if (selectedTpl.value && selectedTpl.value.monitorType !== '4') {
        // 按监控项类型落到对应字段
        const type = selectedTpl.value.monitorType
        if (type === '1') payload.tempValue = payload.monitorInput
        if (type === '2') payload.humidityValue = payload.monitorInput
        if (type === '3') payload.saltRate = payload.monitorInput
        if (type === '5') payload.monitorValue = payload.monitorInput
      }
      delete payload.monitorInput
      addCcpRecord(payload).then(() => {
        proxy.$modal.msgSuccess("录入成功")
        open.value = false
        getList()
        loadStatusCounts()
      })
    }
  })
}

function handleDetail(row) {
  detailOpen.value = true
  detailLoading.value = true
  getCcpRecord(row.recordId).then(response => {
    detail.value = response.data
    detailLoading.value = false
  }).catch(() => { detailLoading.value = false })
}

function openEdit() {
  Object.assign(editForm, { recordId: detail.value.recordId, overNote: detail.value.overNote, corrective: detail.value.corrective })
  editOpen.value = true
}

function submitEdit() {
  updateCcpRecord(editForm).then(() => {
    proxy.$modal.msgSuccess("补充成功")
    editOpen.value = false
    handleDetail({ recordId: detail.value.recordId })
  })
}

function handleExport() {
  proxy.download("food/ccp/export", { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `ccp_${new Date().getTime()}.xlsx`)
}

/** 批次/物料选择 */
function openBatchPicker() { batchPickerRef.value.open(form.value.batchId || undefined) }
function clearBatch() {
  form.value.batchId = null
  form.value.batchCode = undefined
  if (!form.value.materialId) return
}
function onBatchPickerConfirm(batch) {
  form.value.batchId = batch.batchId
  form.value.batchCode = batch.batchCode
  if (batch.materialId) {
    form.value.materialId = batch.materialId
    form.value.materialCode = batch.materialCode
    form.value.materialName = batch.materialName
  }
}
function openMaterialPicker() { materialPickerRef.value.open(form.value.materialId || undefined) }
function clearMaterial() { form.value.materialId = null; form.value.materialCode = undefined; form.value.materialName = undefined }
function onMaterialPickerConfirm(material) {
  form.value.materialId = material.materialId
  form.value.materialCode = material.materialCode
  form.value.materialName = material.materialName
}

/** 模板设置 */
function openTplDialog() {
  tplDialogOpen.value = true
  loadTemplates()
}
function resetTpl() {
  tplData.tplForm = { templateId: null, templateName: '干腌火腿CCP', routeId: null, routeName: undefined, processName: undefined, ccpName: undefined, monitorType: '1', limitType: '1', limitMin: undefined, limitMax: undefined, limitValue: undefined, unit: undefined, monitorFreq: undefined, correctiveAction: undefined, status: '0', remark: undefined }
  proxy.resetForm("tplRef")
}
function handleTplAdd() {
  resetTpl()
  tplFormOpen.value = true
  tplTitle.value = "新增CCP控制点模板"
}
function handleTplUpdate(row) {
  resetTpl()
  tplData.tplForm = { ...row }
  tplFormOpen.value = true
  tplTitle.value = "修改CCP控制点模板"
}
function handleTplDelete(row) {
  proxy.$modal.confirm('是否确认删除控制点[' + row.ccpName + ']模板？已有监控记录不受影响').then(() => delCcpTemplate(row.templateId)).then(() => {
    loadTemplates()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}
function submitTpl() {
  proxy.$refs["tplRef"].validate(valid => {
    if (valid) {
      if (tplForm.value.templateId != null) {
        updateCcpTemplate(tplForm.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          tplFormOpen.value = false
          loadTemplates()
        })
      } else {
        addCcpTemplate(tplForm.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          tplFormOpen.value = false
          loadTemplates()
        })
      }
    }
  })
}

/** 工具函数 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}
function monitorText(row) {
  if (!row || row.monitorType == null) return ''
  if (row.monitorType === '1') return row.tempValue != null ? row.tempValue + '℃' : ''
  if (row.monitorType === '2') return row.humidityValue != null ? row.humidityValue + '%' : ''
  if (row.monitorType === '3') return row.saltRate != null ? row.saltRate + '%' : ''
  if (row.monitorType === '4') {
    const label = dictLabel(food_mold_status.value, row.moldStatus)
    return label === '-' ? '' : label
  }
  if (row.monitorType === '5') return row.monitorValue != null ? String(row.monitorValue) : ''
  return ''
}
function tplLimitText(tpl) {
  if (!tpl) return ''
  if (tpl.limitType === '4') {
    const item = (food_mold_status.value || []).find(d => d.value == tpl.limitValue)
    return '枚举:' + (item ? item.label : tpl.limitValue)
  }
  if (tpl.limitType === '2') return '≥' + tpl.limitMin + (tpl.unit || '')
  if (tpl.limitType === '3') return '≤' + tpl.limitMax + (tpl.unit || '')
  const min = tpl.limitMin != null ? tpl.limitMin : '-∞'
  const max = tpl.limitMax != null ? tpl.limitMax : '+∞'
  return min + '~' + max + (tpl.unit || '')
}

onMounted(() => {
  getList()
  loadTemplates()
  loadStatusCounts()
})
</script>

<style scoped>
.form-tip { font-size: 12px; color: #909399; line-height: 1.6; }
.mono { font-family: "JetBrains Mono", Consolas, monospace; font-size: 12px; }
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.flow-item { display: flex; align-items: center; gap: 8px; }
.flow-arrow { font-size: 18px; color: #909399; }
.flow-note { font-size: 13px; color: #606266; }
.highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.highlight-success { background: #f0f9eb; border-color: #e1f3d8; }
.highlight-danger { background: #fef0f0; border-color: #fde2e2; }
.highlight-primary { background: #ecf5ff; border-color: #d9ecff; }
.highlight-warning { background: #fdf6ec; border-color: #faecd8; }
.highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.highlight-card-body { font-size: 13px; color: #606266; line-height: 1.7; }
.highlight-success .highlight-card-title { color: #67c23a; }
.highlight-danger .highlight-card-title { color: #f56c6c; }
.highlight-primary .highlight-card-title { color: #409eff; }
.highlight-warning .highlight-card-title { color: #e6a23c; }
</style>
