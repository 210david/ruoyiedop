<template>
  <div class="app-container wms-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>垛号</label>
          <div class="control">
            <el-input v-model="queryParams.wipNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.wipStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_wip_status" :key="d.value" :label="d.label" :value="d.value" />
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['food:wip:add']">建垛</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['food:wip:edit']">修改</el-button>
          <el-button type="warning" plain icon="CircleCheck" :disabled="multiple" @click="handleFinish" v-hasPermi="['food:wip:edit']">完工</el-button>
          <el-button type="info" plain icon="SwitchButton" :disabled="multiple" @click="handleClose" v-hasPermi="['food:wip:edit']">关闭</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['food:wip:remove']">删除</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['food:wip:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="food_wip_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="wipList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="垛号" prop="wipNo" key="wipNo" :width="colWidth('wipNo', 130)" resizable v-if="columns.wipNo.visible">
            <template #default="scope"><span class="mono-text">{{ scope.row.wipNo }}</span></template>
          </el-table-column>
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable show-overflow-tooltip v-if="columns.workOrderNo.visible" />
          <el-table-column label="批次号" prop="batchCode" key="batchCode" :width="colWidth('batchCode', 140)" resizable show-overflow-tooltip v-if="columns.batchCode.visible">
            <template #default="scope"><span v-if="scope.row.batchCode">{{ scope.row.batchCode }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 110)" resizable show-overflow-tooltip v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :min-width="colWidth('materialName', 150)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="在制数量" prop="qty" key="qty" :width="colWidth('qty', 110)" resizable align="right" v-if="columns.qty.visible">
            <template #default="scope"><span class="num-text">{{ scope.row.qty }}</span> <span class="text-muted" v-if="scope.row.qtyUnit">{{ unitLabel(scope.row.qtyUnit) }}</span></template>
          </el-table-column>
          <el-table-column label="柜位/垛位" prop="wipLocation" key="wipLocation" :min-width="colWidth('wipLocation', 120)" resizable show-overflow-tooltip v-if="columns.wipLocation.visible">
            <template #default="scope"><span v-if="scope.row.wipLocation">{{ scope.row.wipLocation }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="当前工序" prop="currentProcessName" key="currentProcessName" :min-width="colWidth('currentProcessName', 140)" resizable show-overflow-tooltip v-if="columns.currentProcessName.visible">
            <template #default="scope">
              <span v-if="scope.row.currentStepSeq" class="badge blue"><span class="dot"></span>{{ scope.row.currentStepSeq }} {{ scope.row.currentProcessName }}</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="入柜时间" prop="stepStartTime" key="stepStartTime" :width="colWidth('stepStartTime', 150)" resizable align="center" v-if="columns.stepStartTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.stepStartTime, '{y}-{m}-{d} {h}:{i}') }}</span></template>
          </el-table-column>
          <el-table-column label="预计出柜" prop="planStepEnd" key="planStepEnd" :width="colWidth('planStepEnd', 160)" resizable align="center" v-if="columns.planStepEnd.visible">
            <template #default="scope">
              <span v-if="!scope.row.planStepEnd" class="text-muted">—</span>
              <span v-else-if="isOverdue(scope.row)" class="badge red"><span class="dot"></span>{{ parseTime(scope.row.planStepEnd, '{y}-{m}-{d} {h}:{i}') }}</span>
              <span v-else>{{ parseTime(scope.row.planStepEnd, '{y}-{m}-{d} {h}:{i}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="在制天数" prop="wipDays" key="wipDays" :width="colWidth('wipDays', 90)" resizable align="center" v-if="columns.wipDays.visible">
            <template #default="scope"><span v-if="scope.row.wipDays != null" class="num-text">{{ scope.row.wipDays }}天</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="wipStatus" key="wipStatus" :width="colWidth('wipStatus', 90)" resizable align="center" v-if="columns.wipStatus.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadge(scope.row.wipStatus)"><span class="dot"></span>{{ dictLabel(food_wip_status, scope.row.wipStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 250)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
                <el-button v-if="scope.row.wipStatus === '0'" link type="warning" icon="CircleCheck" @click="handleFinishRow(scope.row)" v-hasPermi="['food:wip:edit']">完工</el-button>
                <el-button v-if="scope.row.wipStatus === '0'" link type="info" icon="SwitchButton" @click="handleCloseRow(scope.row)" v-hasPermi="['food:wip:edit']">关闭</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['food:wip:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['food:wip:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 建垛/编辑对话框 -->
    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="wipRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('w1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>生产信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.w1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.w1">
              <el-form-item label="生产工单" prop="workOrderId">
                <el-input :model-value="form.workOrderNo" readonly placeholder="点击选择工单（垛跟工单产品走）" @click="openWoPicker">
                  <template v-if="form.workOrderId" #append><el-button icon="CircleClose" @click.stop="clearWorkOrder" /></template>
                  <template v-else #append><el-button icon="Search" @click="openWoPicker" /></template>
                </el-input>
              </el-form-item>
              <el-form-item label="产品" prop="materialId">
                <el-input :model-value="[form.materialCode, form.materialName].filter(Boolean).join(' - ')" readonly placeholder="选择工单后自动带出；无工单时点击选择物料" @click="openMaterialPicker">
                  <template v-if="form.materialId" #append><el-button icon="CircleClose" @click.stop="clearMaterial" /></template>
                  <template v-else #append><el-button icon="Search" @click="openMaterialPicker" /></template>
                </el-input>
              </el-form-item>
              <el-form-item label="批次号" prop="batchId">
                <el-input :model-value="form.batchCode" readonly placeholder="点击选择批次（可选，干腌火腿建垛时可暂不指定）" @click="openBatchPicker">
                  <template v-if="form.batchId" #append><el-button icon="CircleClose" @click.stop="clearBatch" /></template>
                  <template v-else #append><el-button icon="Search" @click="openBatchPicker" /></template>
                </el-input>
              </el-form-item>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="在制数量" prop="qty">
                    <el-input-number v-model="form.qty" :min="0.001" :precision="2" :step="1" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="单位" prop="qtyUnit">
                    <el-select v-model="form.qtyUnit" placeholder="请选择" :disabled="!!form.workOrderId" style="width: 100%">
                      <el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="柜位/垛位" prop="wipLocation">
                <el-input v-model="form.wipLocation" placeholder="如：晾房A区-1号柜" maxlength="100" />
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入内容" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card" v-if="form.wipId == null">
            <div class="rd-card-header" @click="toggleCard('w2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>工序进度</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.w2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.w2">
              <el-form-item label="当前工序" prop="currentStepSeq">
                <el-select v-if="routeSteps.length > 0" v-model="form.currentStepSeq" placeholder="请选择起始工序" style="width: 100%">
                  <el-option v-for="s in routeSteps" :key="s.stepSeq" :label="s.stepSeq + ' - ' + s.processName + (s.stdDays ? `（标准${s.stdDays}天）` : '')" :value="s.stepSeq" />
                </el-select>
                <el-input-number v-else v-model="form.currentStepSeq" :min="1" :step="10" controls-position="right" placeholder="无工艺路线时直接填写工序序号" style="width: 100%" />
                <div class="form-tip" v-if="routeSteps.length === 0">工单未挂工艺路线时需手工填写工序序号；预计出柜时间按工序标准时长自动推算</div>
              </el-form-item>
              <el-form-item label="入柜时间" prop="stepStartTime">
                <el-date-picker v-model="form.stepStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="默认当前时间" style="width: 100%" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card" v-else>
            <div class="rd-card-body" style="display:block">
              <el-alert type="info" :closable="false" show-icon title="工序位置（当前工序/入柜时间）仅可通过【工序转移】单据推进，此处不可直接修改" />
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

    <!-- 详情对话框（含温湿度记录） -->
    <el-dialog v-model="detailOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></div>
          <span class="rd-detail-header-title">在制品详情</span>
          <div class="rd-detail-header-sub" v-if="detail.wipNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ detail.wipNo }}</span></div>
        </div>
      </template>
      <div class="rd-page" v-loading="detailLoading">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>垛标识</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d1 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.d1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">垛号</span><div class="rd-value mono-text">{{ detail.wipNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="detail.wipStatus" class="badge" :class="statusBadge(detail.wipStatus)"><span class="dot"></span>{{ dictLabel(food_wip_status, detail.wipStatus) }}</span><span v-else class="text-muted">—</span></div></div>
              <div class="rd-item"><span class="rd-label">柜位/垛位</span><div class="rd-value">{{ detail.wipLocation || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">在制天数</span><div class="rd-value">{{ detail.wipDays != null ? detail.wipDays + ' 天' : '—' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/></svg></span>生产信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d2 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.d2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ detail.workOrderNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ detail.batchCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料</span><div class="rd-value">{{ [detail.materialCode, detail.materialName].filter(Boolean).join(' - ') || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">在制数量</span><div class="rd-value">{{ detail.qty != null ? detail.qty + (detail.qtyUnit ? ' ' + unitLabel(detail.qtyUnit) : '') : '—' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>工序进度</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d3 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.d3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">当前工序</span><div class="rd-value"><span v-if="detail.currentStepSeq" class="badge blue"><span class="dot"></span>{{ detail.currentStepSeq }} {{ detail.currentProcessName }}</span><span v-else class="text-muted">—</span></div></div>
              <div class="rd-item"><span class="rd-label">入柜时间</span><div class="rd-value">{{ parseTime(detail.stepStartTime, '{y}-{m}-{d} {h}:{i}') || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">预计出柜</span><div class="rd-value"><span :class="{ 'text-danger': isOverdue(detail) }">{{ parseTime(detail.planStepEnd, '{y}-{m}-{d} {h}:{i}') || '—' }}</span><span v-if="isOverdue(detail)" class="badge red" style="margin-left:6px"><span class="dot"></span>已超期</span></div></div>
              <div class="rd-item"><span class="rd-label">完工时间</span><div class="rd-value">{{ parseTime(detail.finishTime, '{y}-{m}-{d} {h}:{i}') || '—' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></svg></span>温湿度/上霉记录</div>
            <div style="display:flex;align-items:center;gap:8px">
              <el-button v-if="detail.wipStatus === '0'" type="primary" plain icon="Plus" size="small" @click="envFormOpen = !envFormOpen" v-hasPermi="['food:wip:env']">记录</el-button>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d4 }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.d4" style="display:block">
            <el-form v-show="envFormOpen" :model="envForm" label-width="90px" style="margin-bottom:12px" inline>
              <el-form-item label="温度(℃)"><el-input-number v-model="envForm.temp" :precision="1" :step="0.5" controls-position="right" style="width: 120px" /></el-form-item>
              <el-form-item label="湿度(%)"><el-input-number v-model="envForm.humidity" :precision="1" :step="1" :min="0" :max="100" controls-position="right" style="width: 120px" /></el-form-item>
              <el-form-item label="上霉状态">
                <el-select v-model="envForm.moldStatus" placeholder="请选择" clearable style="width: 130px">
                  <el-option v-for="d in food_mold_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="记录时间"><el-date-picker v-model="envForm.recordTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="默认当前时间" style="width: 170px" /></el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitEnv" v-hasPermi="['food:wip:env']">保 存</el-button>
              </el-form-item>
            </el-form>
            <el-table :data="detail.envList || []" border size="small" class="detail-table">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column label="记录时间" prop="recordTime" width="150" align="center"><template #default="scope">{{ parseTime(scope.row.recordTime, '{y}-{m}-{d} {h}:{i}') }}</template></el-table-column>
              <el-table-column label="工序" min-width="110" show-overflow-tooltip><template #default="scope"><span v-if="scope.row.stepSeq">{{ scope.row.stepSeq }} {{ scope.row.processName }}</span><span v-else class="text-muted">—</span></template></el-table-column>
              <el-table-column label="温度(℃)" prop="temp" width="90" align="center"><template #default="scope"><span v-if="scope.row.temp != null">{{ scope.row.temp }}</span><span v-else class="text-muted">—</span></template></el-table-column>
              <el-table-column label="湿度(%)" prop="humidity" width="90" align="center"><template #default="scope"><span v-if="scope.row.humidity != null">{{ scope.row.humidity }}</span><span v-else class="text-muted">—</span></template></el-table-column>
              <el-table-column label="上霉状态" prop="moldStatus" width="110" align="center"><template #default="scope"><span v-if="scope.row.moldStatus != null && scope.row.moldStatus !== ''" class="badge" :class="moldBadge(scope.row.moldStatus)"><span class="dot"></span>{{ dictLabel(food_mold_status, scope.row.moldStatus) }}</span><span v-else class="text-muted">—</span></template></el-table-column>
              <el-table-column label="记录人" prop="recorder" width="100" align="center" />
              <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" @click="handleDeleteEnv(scope.row)" v-hasPermi="['food:wip:env']" /></template></el-table-column>
            </el-table>
            <div v-if="!(detail.envList && detail.envList.length)" style="text-align:center;padding:16px;color:#909399;font-size:13px">暂无温湿度记录</div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 工单选择器 -->
    <el-dialog v-model="woPickerOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">选择工单</span></div></template>
      <div class="material-picker"><div class="material-picker-search"><el-input v-model="woPickerQuery.workOrderNo" placeholder="工单编号" clearable size="small" style="width: 180px" @keyup.enter="handleWoPickerQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input><el-input v-model="woPickerQuery.productName" placeholder="产品名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleWoPickerQuery" /><el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleWoPickerQuery">查询</el-button><el-button icon="RefreshLeft" size="small" @click="resetWoPickerQuery">重置</el-button></div><div class="material-picker-table"><el-table v-loading="woPickerLoading" :data="woPickerList" highlight-current-row @row-click="onWoRowClick" @row-dblclick="onWoRowDblClick" height="360" size="small" border><el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="woPickerSelectedId" :value="row.workOrderId" @click.stop="onWoRowClick(row)"><span /></el-radio></template></el-table-column><el-table-column label="工单编号" prop="workOrderNo" width="150" show-overflow-tooltip /><el-table-column label="产品编码" prop="productCode" width="120" show-overflow-tooltip /><el-table-column label="产品名称" prop="productName" min-width="160" show-overflow-tooltip /><el-table-column label="计划数量" prop="planQty" width="90" align="center" /><el-table-column label="状态" prop="status" width="90" align="center"><template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template></el-table-column></el-table></div><div class="material-picker-pager"><el-pagination v-model:current-page="woPickerQuery.pageNum" v-model:page-size="woPickerQuery.pageSize" :total="woPickerTotal" layout="total, prev, pager, next" small @current-change="getWoPickerList" /></div></div>
      <template #footer><el-button @click="woPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleWoPickerConfirm" :disabled="!woPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- 批次选择器 / 物料选择器 -->
    <batch-picker ref="batchPickerRef" title="选择批次" @confirm="onBatchPickerConfirm" />
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
  </div>
</template>

<script setup name="FoodWip">
import { listWip, getWip, addWip, updateWip, delWip, finishWip, closeWip, addEnv, delEnv } from "@/api/food/wip"
import { listWorkOrder } from "@/api/mms/workorder"
import { getRoute } from "@/api/mms/route"
import BatchPicker from '@/components/BatchPicker/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['w1', 'w2', 'd1', 'd2', 'd3', 'd4'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('food_wip_index')
const { food_wip_status, food_mold_status, wms_unit, mms_workorder_status } = proxy.useDict("food_wip_status", "food_mold_status", "wms_unit", "mms_workorder_status")

// 列显隐配置
const defaultColumns = {
  wipNo: { label: '垛号', visible: true },
  workOrderNo: { label: '工单号', visible: true },
  batchCode: { label: '批次号', visible: true },
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  qty: { label: '在制数量', visible: true },
  wipLocation: { label: '柜位/垛位', visible: true },
  currentProcessName: { label: '当前工序', visible: true },
  stepStartTime: { label: '入柜时间', visible: true },
  planStepEnd: { label: '预计出柜', visible: true },
  wipDays: { label: '在制天数', visible: true },
  wipStatus: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('food_wip_columns')
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

const wipList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const routeSteps = ref([])
const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref({})
const envFormOpen = ref(false)
const envForm = reactive({ temp: undefined, humidity: undefined, moldStatus: undefined, recordTime: undefined })
const batchPickerRef = ref(null)
const materialPickerRef = ref(null)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, wipNo: undefined, workOrderNo: undefined, batchCode: undefined, materialName: undefined, wipStatus: undefined },
  rules: {
    qty: [{ required: true, message: "在制数量不能为空", trigger: "blur" }],
    currentStepSeq: [{ required: true, message: "当前工序不能为空", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 工单选择器
const woPickerOpen = ref(false); const woPickerLoading = ref(false); const woPickerList = ref([]); const woPickerTotal = ref(0); const woPickerSelectedId = ref(null); const woPickerSelectedRow = ref(null);
const woPickerQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined, params: { statusList: ['1', '2'] } });

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.wipNo) count++
  if (queryParams.value.workOrderNo) count++
  if (queryParams.value.batchCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.wipStatus) count++
  return count
})

function getList() {
  loading.value = true
  listWip(queryParams.value).then(response => {
    wipList.value = response.rows
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
  queryParams.value.wipNo = undefined
  queryParams.value.workOrderNo = undefined
  queryParams.value.batchCode = undefined
  queryParams.value.materialName = undefined
  queryParams.value.wipStatus = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.wipId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 表单重置 */
function reset() {
  form.value = { wipId: null, workOrderId: null, workOrderNo: undefined, routeId: null, batchId: null, batchCode: undefined, materialId: null, materialCode: undefined, materialName: undefined, qty: undefined, qtyUnit: undefined, wipLocation: undefined, currentStepSeq: undefined, stepStartTime: undefined, remark: undefined }
  routeSteps.value = []
  proxy.resetForm("wipRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新建在制垛"
}

function handleUpdate(row) {
  reset()
  const wipId = row.wipId || ids.value[0]
  getWip(wipId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改在制品"
  })
}

function handleDetail(row) {
  detailOpen.value = true
  detailLoading.value = true
  envFormOpen.value = false
  Object.assign(envForm, { temp: undefined, humidity: undefined, moldStatus: undefined, recordTime: undefined })
  getWip(row.wipId).then(response => {
    detail.value = response.data
    detailLoading.value = false
  }).catch(() => { detailLoading.value = false })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

function submitForm() {
  proxy.$refs["wipRef"].validate(valid => {
    if (valid) {
      if (!form.value.workOrderId && !form.value.materialId) {
        proxy.$modal.msgWarning("工单与物料至少选择一项")
        return
      }
      if (form.value.wipId != null) {
        updateWip(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addWip(form.value).then(() => {
          proxy.$modal.msgSuccess("建垛成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const deleteIds = row.wipId || ids.value
  proxy.$modal.confirm('是否确认删除所选在制品垛？存在转移记录的垛不可删除').then(() => delWip(deleteIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function doFinish(wipId) {
  finishWip(wipId).then(() => {
    getList()
    proxy.$modal.msgSuccess("已完工")
  })
}

function handleFinish() {
  const row = wipList.value.find(i => i.wipId === ids.value[0])
  if (row && row.wipStatus !== '0') {
    proxy.$modal.msgWarning("仅「在制」状态的垛可完工")
    return
  }
  proxy.$modal.confirm('是否确认完工垛[' + (row ? row.wipNo : '') + ']？').then(() => doFinish(ids.value[0])).catch(() => {})
}

function handleFinishRow(row) {
  proxy.$modal.confirm('是否确认完工垛[' + row.wipNo + ']？').then(() => doFinish(row.wipId)).catch(() => {})
}

function doClose(wipId) {
  closeWip(wipId).then(() => {
    getList()
    proxy.$modal.msgSuccess("已关闭")
  })
}

function handleClose() {
  const row = wipList.value.find(i => i.wipId === ids.value[0])
  if (row && row.wipStatus !== '0') {
    proxy.$modal.msgWarning("仅「在制」状态的垛可关闭")
    return
  }
  proxy.$modal.confirm('是否确认关闭垛[' + (row ? row.wipNo : '') + ']？关闭后不再参与工序流转').then(() => doClose(ids.value[0])).catch(() => {})
}

function handleCloseRow(row) {
  proxy.$modal.confirm('是否确认关闭垛[' + row.wipNo + ']？关闭后不再参与工序流转').then(() => doClose(row.wipId)).catch(() => {})
}

function handleExport() {
  proxy.download("food/wip/export", { ...queryParams.value }, `wip_${new Date().getTime()}.xlsx`)
}

/** 温湿度记录 */
function submitEnv() {
  if (envForm.temp == null && envForm.humidity == null && (envForm.moldStatus == null || envForm.moldStatus === '')) {
    proxy.$modal.msgWarning("温度、湿度、上霉状态至少填写一项")
    return
  }
  addEnv({ ...envForm, wipId: detail.value.wipId }).then(() => {
    proxy.$modal.msgSuccess("记录成功")
    Object.assign(envForm, { temp: undefined, humidity: undefined, moldStatus: undefined, recordTime: undefined })
    envFormOpen.value = false
    handleDetail(detail.value)
  })
}

function handleDeleteEnv(row) {
  proxy.$modal.confirm('是否确认删除该条记录？').then(() => delEnv(row.envId)).then(() => {
    handleDetail(detail.value)
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 工单选择 */
function openWoPicker() {
  woPickerOpen.value = true; woPickerSelectedId.value = null; woPickerSelectedRow.value = null; woPickerQuery.pageNum = 1; woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; getWoPickerList();
}
function getWoPickerList() { woPickerLoading.value = true; listWorkOrder(woPickerQuery).then(res => { woPickerList.value = res.rows; woPickerTotal.value = res.total; woPickerLoading.value = false; }).catch(() => { woPickerLoading.value = false; }); }
function handleWoPickerQuery() { woPickerQuery.pageNum = 1; getWoPickerList(); }
function resetWoPickerQuery() { woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; handleWoPickerQuery(); }
function onWoRowClick(row) { woPickerSelectedId.value = row.workOrderId; woPickerSelectedRow.value = row; }
function onWoRowDblClick(row) { onWoRowClick(row); handleWoPickerConfirm(); }
function handleWoPickerConfirm() {
  if (!woPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工单'); return; }
  const row = woPickerSelectedRow.value;
  form.value.workOrderId = row.workOrderId;
  form.value.workOrderNo = row.workOrderNo;
  form.value.materialId = row.productId;
  form.value.materialCode = row.productCode;
  form.value.materialName = row.productName;
  form.value.qtyUnit = row.unit;
  form.value.routeId = row.routeId;
  loadRouteSteps(row.routeId);
  woPickerOpen.value = false;
}
function clearWorkOrder() {
  form.value.workOrderId = null;
  form.value.workOrderNo = undefined;
  form.value.materialId = null;
  form.value.materialCode = undefined;
  form.value.materialName = undefined;
  form.value.qtyUnit = undefined;
  form.value.routeId = null;
  routeSteps.value = [];
}

/** 加载工艺路线工序列表（供选择当前工序） */
function loadRouteSteps(routeId) {
  routeSteps.value = []
  if (!routeId) return
  getRoute(routeId).then(res => {
    const list = (res.data && res.data.processList) || []
    routeSteps.value = list.map(p => ({ stepSeq: p.stepSeq, processName: p.processName, stdDays: p.stdDays }))
    if (routeSteps.value.length > 0 && !form.value.currentStepSeq) {
      form.value.currentStepSeq = routeSteps.value[0].stepSeq
    }
  })
}

/** 批次选择 */
function openBatchPicker() { batchPickerRef.value.open(form.value.batchId || undefined) }
function clearBatch() { form.value.batchId = null; form.value.batchCode = undefined }
function onBatchPickerConfirm(batch) { form.value.batchId = batch.batchId; form.value.batchCode = batch.batchCode }

/** 物料选择（无工单时手工指定产品） */
function openMaterialPicker() {
  if (form.value.workOrderId) return
  materialPickerRef.value.open(form.value.materialId || undefined)
}
function clearMaterial() {
  if (form.value.workOrderId) return
  form.value.materialId = null; form.value.materialCode = undefined; form.value.materialName = undefined
}
function onMaterialPickerConfirm(material) {
  if (form.value.workOrderId) return
  form.value.materialId = material.materialId
  form.value.materialCode = material.materialCode
  form.value.materialName = material.materialName
}

/** 工具函数 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}
function unitLabel(v) {
  if (v == null || v === '') return ''
  const item = (wms_unit.value || []).find(d => d.value == v)
  return item ? item.label : v
}
function statusBadge(s) {
  if (s === '0') return 'blue'
  if (s === '1') return 'green'
  return 'gray'
}
function moldBadge(s) {
  const n = Number(s)
  if (n >= 3) return 'red'
  if (n === 2) return 'amber'
  if (n === 1) return 'violet'
  return 'green'
}
function isOverdue(row) {
  return row.wipStatus === '0' && row.planStepEnd && new Date(row.planStepEnd).getTime() < Date.now()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.form-tip { font-size: 12px; color: #909399; line-height: 1.6; }
</style>
