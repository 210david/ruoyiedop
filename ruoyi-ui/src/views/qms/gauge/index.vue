<template>
  <div class="app-container qms-gauge-page">
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
          <label>量检具编号</label>
          <div class="control">
            <el-input v-model="queryParams.gaugeNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>量检具名称</label>
          <div class="control">
            <el-input v-model="queryParams.gaugeName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>量检具状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.gaugeStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_gauge_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>备注</label>
          <div class="control"><el-input v-model="queryParams.remark" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:gauge:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:gauge:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:gauge:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:gauge:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_gauge_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="量检具编号" prop="gaugeNo" key="gaugeNo" :width="colWidth('gaugeNo', 140)" resizable show-overflow-tooltip v-if="columns.gaugeNo.visible" />
          <el-table-column label="量检具名称" prop="gaugeName" key="gaugeName" :width="colWidth('gaugeName', 180)" resizable show-overflow-tooltip v-if="columns.gaugeName.visible" />
          <el-table-column prop="gaugeType" key="gaugeType" :width="colWidth('gaugeType', 120)" resizable align="center" v-if="columns.gaugeType.visible">
            <template #header><span>类型</span><el-tooltip content="量检具分类大类：称量类（天平/水分仪等）、分析类（光谱仪/色谱仪等）、量具类（卡尺/千分尺/量块等）、监测类（温度记录仪等）" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><span class="badge violet"><span class="dot"></span>{{ gaugeTypeLabel(scope.row.gaugeType) }}</span></template>
          </el-table-column>
          <el-table-column label="精度" prop="precisionVal" key="precisionVal" :width="colWidth('precisionVal', 100)" resizable align="center" v-if="columns.precisionVal.visible" />
          <el-table-column label="测量范围" prop="measureRange" key="measureRange" :width="colWidth('measureRange', 140)" resizable show-overflow-tooltip v-if="columns.measureRange.visible" />
          <el-table-column prop="calibCycle" key="calibCycle" :width="colWidth('calibCycle', 110)" resizable align="center" v-if="columns.calibCycle.visible">
            <template #header><span>校准周期(月)</span><el-tooltip content="量检具两次校准之间的间隔月数，到期前应及时送检校准" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="上次校准日期" prop="lastCalibDate" key="lastCalibDate" :width="colWidth('lastCalibDate', 120)" resizable align="center" v-if="columns.lastCalibDate.visible" />
          <el-table-column label="下次校准日期" prop="nextCalibDate" key="nextCalibDate" :width="colWidth('nextCalibDate', 120)" resizable align="center" v-if="columns.nextCalibDate.visible" />
          <el-table-column prop="gaugeStatus" key="gaugeStatus" :width="colWidth('gaugeStatus', 100)" resizable align="center" v-if="columns.gaugeStatus.visible">
            <template #header><span>量检具状态</span><el-tooltip content="在用：正常使用中；停用：暂停使用；待校准：校准到期需送检" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope">
              <span class="badge" :class="gaugeBadgeClass(scope.row.gaugeStatus)"><span class="dot"></span>{{ gaugeStatusLabel(scope.row.gaugeStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 80)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:gauge:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:gauge:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 4l6 6"/><path d="M3 12l9 9 9-9-9-9z"/><path d="M3 21h18"/><circle cx="7.5" cy="16.5" r="1.5"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="gaugeRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="量检具编号" prop="gaugeNo"><el-input v-model="form.gaugeNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="量检具名称" prop="gaugeName"><el-input v-model="form.gaugeName" placeholder="请输入量检具名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="gaugeType">
                  <template #label><span>类型</span><el-tooltip content="量检具分类大类：称量类（天平/水分仪等）、分析类（光谱仪/色谱仪等）、量具类（卡尺/千分尺/量块等）、监测类（温度记录仪等）" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></template>
                  <el-select v-model="form.gaugeType" placeholder="请选择类型" style="width: 100%">
                    <el-option v-for="d in qms_gauge_type" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="precisionVal">
                  <template #label>
                    <span>精度</span>
                    <el-tooltip content="请输入量检具的精度值，如 0.02mm、0.01mm、±0.005mm 等" placement="top">
                      <el-icon class="form-tip-icon"><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </template>
                  <el-input v-model="form.precisionVal" placeholder="如 0.02mm" />
                </el-form-item></el-col>
              </el-row>
              <el-form-item prop="measureRange">
                <template #label>
                  <span>测量范围</span>
                  <el-tooltip content="请输入量检具可测量的最小至最大值，如 0~150mm、0~25mm、-50~50℃ 等" placement="top">
                    <el-icon class="form-tip-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                </template>
                <el-input v-model="form.measureRange" placeholder="如 0~150mm" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>校准信息</div><el-tooltip content="记录量检具的校准周期及历次校准日期，下次校准日期 = 上次校准日期 + 校准周期，到期前应及时送检" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="lastCalibDate"><template #label><span>上次校准日期</span><el-tooltip content="最近一次校准的执行日期，校准合格后方可继续使用" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></template><el-date-picker v-model="form.lastCalibDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="nextCalibDate"><template #label><span>下次校准日期</span><el-tooltip content="预计下次需要校准的日期，通常为上次校准日期加校准周期" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></template><el-date-picker v-model="form.nextCalibDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="calibCycle"><template #label><span>校准周期(月)</span><el-tooltip content="量检具两次校准之间的间隔月数，到期前应及时送检校准" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.calibCycle" :min="1" :max="120" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>状态配置</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="gaugeStatus"><template #label><span>量检具状态</span><el-tooltip content="在用：正常使用中；停用：暂停使用；待校准：校准到期需送检" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.gaugeStatus" style="width: 100%"><el-option v-for="d in qms_gauge_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">量检具详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">量检具编号</span><div class="rd-value">{{ viewData.gaugeNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">量检具名称</span><div class="rd-value">{{ viewData.gaugeName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">类型<el-tooltip content="量检具分类大类：称量类（天平/水分仪等）、分析类（光谱仪/色谱仪等）、量具类（卡尺/千分尺/量块等）、监测类（温度记录仪等）" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><dict-tag :options="qms_gauge_type" :value="viewData.gaugeType" /></div></div>
            <div class="rd-item"><span class="rd-label">精度</span><div class="rd-value">{{ viewData.precisionVal || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">测量范围</span><div class="rd-value">{{ viewData.measureRange || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">量检具状态<el-tooltip content="在用：正常使用中；停用：暂停使用；待校准：校准到期需送检" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ gaugeStatusLabel(viewData.gaugeStatus) }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>校准信息</div><el-tooltip content="记录量检具的校准周期及历次校准日期，下次校准日期 = 上次校准日期 + 校准周期，到期前应及时送检" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">校准周期(月)<el-tooltip content="量检具两次校准之间的间隔月数，到期前应及时送检校准" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.calibCycle != null ? viewData.calibCycle : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">上次校准日期<el-tooltip content="最近一次校准的执行日期，校准合格后方可继续使用" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.lastCalibDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">下次校准日期<el-tooltip content="预计下次需要校准的日期，通常为上次校准日期加校准周期" placement="top"><el-icon class="form-tip-icon"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.nextCalibDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ viewData.status === '0' ? '正常' : '停用' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsGauge">
import { listGauge, getGauge, addGauge, updateGauge, delGauge } from '@/api/qms/gauge'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_gauge_index')
const { qms_gauge_status, qms_gauge_type } = proxy.useDict('qms_gauge_status', 'qms_gauge_type')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const defaultColumns = { gaugeNo: { label: '量检具编号', visible: true }, gaugeName: { label: '量检具名称', visible: true }, gaugeType: { label: '类型', visible: true }, precisionVal: { label: '精度', visible: true }, measureRange: { label: '测量范围', visible: true }, calibCycle: { label: '校准周期(月)', visible: true }, lastCalibDate: { label: '上次校准日期', visible: true }, nextCalibDate: { label: '下次校准日期', visible: true }, gaugeStatus: { label: '量检具状态', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_gauge_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, gaugeNo: undefined, gaugeName: undefined, gaugeStatus: undefined, status: undefined, remark: undefined, params: {} },
  rules: {
    gaugeName: [{ required: true, message: '量检具名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.gaugeNo) count++
  if (queryParams.value.gaugeName) count++
  if (queryParams.value.gaugeStatus) count++
  if (queryParams.value.status) count++
  if (queryParams.value.remark) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function gaugeStatusLabel(val) { const item = qms_gauge_status.value.find(d => d.value == val); return item ? item.label : '-' }
function gaugeBadgeClass(val) { if (val == '0') return 'green'; if (val == '1') return 'red'; if (val == '2') return 'orange'; return 'gray' }
function gaugeTypeLabel(val) { const item = qms_gauge_type.value.find(d => d.value == val); return item ? item.label : '-' }

function getList() { loading.value = true; listGauge(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.gaugeNo = undefined; queryParams.value.gaugeName = undefined; queryParams.value.gaugeStatus = undefined; queryParams.value.status = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.gaugeId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { gaugeId: undefined, gaugeNo: undefined, gaugeName: undefined, gaugeType: undefined, precisionVal: undefined, measureRange: undefined, calibCycle: 12, lastCalibDate: undefined, nextCalibDate: undefined, gaugeStatus: '0', status: '0', remark: undefined }; proxy.resetForm('gaugeRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加量检具' }
function handleUpdate(row) { reset(); getGauge(row.gaugeId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改量检具' }) }
function handleView(row) { getGauge(row.gaugeId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { proxy.$refs['gaugeRef'].validate(valid => { if (valid) { if (form.value.gaugeId != undefined) { updateGauge(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addGauge(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const gaugeIds = row.gaugeId || ids.value; proxy.$modal.confirm('确认删除编号为"' + gaugeIds + '"的数据？').then(() => delGauge(gaugeIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('qms/gauge/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `gauge_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
onActivated(() => { getList() })
</script>

<style scoped>
.qms-gauge-page { padding-top: 10px; --violet-50:#f5f3ff; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-gauge-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-gauge-page .filter-card { padding:14px 20px 16px; }
.qms-gauge-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-gauge-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-gauge-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-gauge-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-gauge-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-gauge-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-gauge-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-gauge-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-gauge-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-gauge-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-gauge-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-gauge-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-gauge-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-gauge-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-gauge-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-gauge-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-gauge-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-gauge-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-gauge-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-gauge-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-gauge-page .field .control :deep(.el-select) { width:100%; }
.qms-gauge-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-gauge-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-gauge-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-gauge-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-gauge-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-gauge-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-gauge-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-gauge-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-gauge-page .btn-soft .el-icon { font-size:14px; }
.qms-gauge-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-gauge-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-gauge-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-gauge-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-gauge-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-gauge-page .table-wrap { overflow-x:auto; }
.qms-gauge-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-gauge-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-gauge-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-gauge-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-gauge-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-gauge-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-gauge-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-gauge-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-gauge-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-gauge-page .badge.green .dot { background:var(--green-500); }
.qms-gauge-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-gauge-page .badge.gray .dot { background:var(--ink-400); }
.qms-gauge-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-gauge-page .badge.red .dot { background:var(--red-500); }
.qms-gauge-page .badge.orange { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-gauge-page .badge.orange .dot { background:#f59e0b; }
.qms-gauge-page .badge.violet { background:var(--violet-50); color:#7c3aed; border-color:#ddd6fe; }
.qms-gauge-page .badge.violet .dot { background:#8b5cf6; }
.qms-gauge-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.qms-gauge-page .form-tip-icon { color:var(--ink-400); margin-left:4px; cursor:help; vertical-align:middle; font-size:14px; transition:color .15s; }
.qms-gauge-page .form-tip-icon:hover { color:var(--brand-500); }
.qms-gauge-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-gauge-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-gauge-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-gauge-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
@media (max-width:1100px) { .qms-gauge-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-gauge-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-gauge-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
