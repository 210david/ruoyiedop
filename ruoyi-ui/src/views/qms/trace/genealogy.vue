<template>
  <div class="app-container qms-genealogy-page">
    <!-- ===== 完整度看板卡片 ===== -->
    <el-row :gutter="16" class="completeness-row" v-show="showSearch">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="completeness-card">
          <div class="completeness-card__label">已关联工单</div>
          <div class="completeness-card__value">{{ completeness.linkedOrders || 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="completeness-card">
          <div class="completeness-card__label">断点工单</div>
          <div class="completeness-card__value" style="color: var(--red-500)">{{ completeness.breakOrders || 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="completeness-card">
          <div class="completeness-card__label">完整率 <span class="completeness-card__target">目标 ≥ 98%</span></div>
          <div class="completeness-card__value" :style="{ color: (completeness.completenessRate || 0) >= 98 ? 'var(--green-500)' : 'var(--warning-500)' }">{{ completeness.completenessRate || 0 }}%</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="completeness-card">
          <div class="completeness-card__label">目标差距</div>
          <div class="completeness-card__value" :style="{ color: (completeness.gap || 0) <= 0 ? 'var(--green-500)' : 'var(--red-500)' }">{{ completeness.gap || 0 }}%</div>
        </div>
      </el-col>
    </el-row>

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
          <label>投入批次</label>
          <div class="control">
            <el-input v-model="queryParams.parentBatchNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产出批次</label>
          <div class="control">
            <el-input v-model="queryParams.childBatchNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>来源</label>
          <div class="control is-select">
            <el-select v-model="queryParams.sourceType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_genealogy_source" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>断点状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.breakFlag" placeholder="全部" clearable @change="handleQuery">
              <el-option label="仅断点" :value="1" />
              <el-option label="仅正常" :value="0" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>谱系时间</label>
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
      <!-- Status Tabs + Tip Pill -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="tab-btn" :class="{ active: queryParams.breakFlag === undefined || queryParams.breakFlag === null }" @click="setBreakFilter(undefined)">全部</button>
          <button class="tab-btn" :class="{ active: queryParams.breakFlag === 0 }" @click="setBreakFilter(0)">
            <span class="tab-dot dot-green"></span> 正常
          </button>
          <button class="tab-btn" :class="{ active: queryParams.breakFlag === 1 }" @click="setBreakFilter(1)">
            <span class="tab-dot dot-red"></span> 断点
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:genealogy:add']">新增谱系</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:genealogy:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:genealogy:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:genealogy:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleBreakList">
            <el-icon><Warning /></el-icon> 断点清单
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_genealogy_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="投入批次" prop="parentBatchNo" key="parentBatchNo" :width="colWidth('parentBatchNo', 140)" resizable show-overflow-tooltip v-if="columns.parentBatchNo.visible">
            <template #header><span>投入批次</span><el-tooltip content="投入原料或半成品的批次号，谱系链的上游批次" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="产出批次" prop="childBatchNo" key="childBatchNo" :width="colWidth('childBatchNo', 140)" resizable show-overflow-tooltip v-if="columns.childBatchNo.visible">
            <template #header><span>产出批次</span><el-tooltip content="产出半成品或成品的批次号，谱系链的下游批次。空值表示断点未登记" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope">
              <span v-if="scope.row.breakFlag === 1" class="badge red"><span class="dot"></span>{{ scope.row.childBatchNo || '断点' }}</span>
              <span v-else>{{ scope.row.childBatchNo || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 120)" resizable show-overflow-tooltip v-if="columns.workOrderNo.visible" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 120)" resizable show-overflow-tooltip v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 160)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="工序" prop="opName" key="opName" :width="colWidth('opName', 90)" resizable align="center" v-if="columns.opName.visible" />
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 130)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="投入量" prop="qtyIn" key="qtyIn" :width="colWidth('qtyIn', 90)" resizable align="center" v-if="columns.qtyIn.visible">
            <template #header><span>投入量</span><el-tooltip content="投入批次的数量，支持4位小数" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="产出量" prop="qtyOut" key="qtyOut" :width="colWidth('qtyOut', 90)" resizable align="center" v-if="columns.qtyOut.visible">
            <template #header><span>产出量</span><el-tooltip content="产出批次的数量，支持4位小数" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="来源" prop="sourceType" key="sourceType" :width="colWidth('sourceType', 90)" resizable align="center" v-if="columns.sourceType.visible">
            <template #header><span>来源</span><el-tooltip content="谱系数据来源：投料/报工/完工/调拨/返工/合并/拆分/补录" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><dict-tag :options="qms_genealogy_source" :value="scope.row.sourceType" /></template>
          </el-table-column>
          <el-table-column label="产线" prop="lineName" key="lineName" :width="colWidth('lineName', 90)" resizable show-overflow-tooltip v-if="columns.lineName.visible" />
          <el-table-column label="操作员" prop="operator" key="operator" :width="colWidth('operator', 80)" resizable align="center" v-if="columns.operator.visible" />
          <el-table-column label="谱系时间" prop="traceTime" key="traceTime" :width="colWidth('traceTime', 145)" resizable align="center" v-if="columns.traceTime.visible">
            <template #header><span>谱系时间</span><el-tooltip content="谱系关联建立的时间，即投入产出关系记录的时间点" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="断点" prop="breakFlag" key="breakFlag" :width="colWidth('breakFlag', 80)" resizable align="center" v-if="columns.breakFlag.visible">
            <template #header><span>断点</span><el-tooltip content="断点标记：表示该谱系记录的产出批次未登记，导致追溯链中断" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope">
              <span v-if="scope.row.breakFlag === 1" class="badge red"><span class="dot"></span>是</span>
              <span v-else class="badge green"><span class="dot"></span>否</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:genealogy:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:genealogy:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== View Dialog ===== -->
    <el-dialog v-model="viewOpen" width="1030px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">批次谱系详情</span>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_batch')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>批次关联信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_batch }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_batch" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">投入批次<el-tooltip content="投入原料或半成品的批次号，谱系链的上游批次" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.parentBatchNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">产出批次<el-tooltip content="产出半成品或成品的批次号，谱系链的下游批次。空值表示断点未登记" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.childBatchNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单号<el-tooltip content="关联的生产工单编号" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">工序<el-tooltip content="该谱系关联的生产工序名称" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.opName || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_material')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料与数量</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_material }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_material" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">物料编码<el-tooltip content="物料的唯一编码，复用仓库主数据" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称<el-tooltip content="物料的名称" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">投入数量<el-tooltip content="投入批次的数量，支持4位小数" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.qtyIn != null ? viewData.qtyIn : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">产出数量<el-tooltip content="产出批次的数量，支持4位小数" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.qtyOut != null ? viewData.qtyOut : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_prod')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 20h20"/><path d="M5 20V8l7-5 7 5v12"/><path d="M9 20v-6h6v6"/></svg></span>生产信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_prod }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_prod" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">产线<el-tooltip content="生产产线名称" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.lineName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">操作员<el-tooltip content="执行该操作的员工" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.operator || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商<el-tooltip content="原料批次关联的供应商名称" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_trace')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>来源与谱系时间</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_trace }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_trace" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">来源类型<el-tooltip content="谱系数据来源：投料(feeding)、报工(report)、完工(complete)、调拨(transfer)、返工(rework)、合并(merge)、拆分(split)、补录(manual)" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><dict-tag :options="qms_genealogy_source" :value="viewData.sourceType" /></div></div>
            <div class="rd-item"><span class="rd-label">谱系时间<el-tooltip content="谱系关联建立的时间" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.traceTime || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">断点标记<el-tooltip content="断点标记：0=正常，1=断点（产出批次未登记，追溯链中断）" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">
              <span v-if="viewData.breakFlag === 1" class="badge red"><span class="dot"></span>断点</span>
              <span v-else class="badge green"><span class="dot"></span>正常</span>
            </div></div>
            <div class="rd-item" v-if="viewData.breakFlag === 1"><span class="rd-label">断点原因</span><div class="rd-value">{{ viewData.breakReason || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('v_other')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_other" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== Add/Edit Dialog ===== -->
    <el-dialog v-model="open" width="1030px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 批次关联信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_batch')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>批次关联信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_batch }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_batch">
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item prop="parentBatchNo"><template #label><span>投入批次</span><el-tooltip content="投入原料或半成品的批次号，谱系链的上游批次" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.parentBatchNo" placeholder="原料/半成品批次号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="childBatchNo"><template #label><span>产出批次</span><el-tooltip content="产出半成品或成品的批次号，谱系链的下游批次。留空表示断点（未登记产出）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.childBatchNo" placeholder="半成品/成品批次号（空=断点）" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item prop="workOrderNo"><template #label><span>工单号</span><el-tooltip content="关联的生产工单编号" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.workOrderNo" placeholder="请输入工单号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="opName"><template #label><span>工序名称</span><el-tooltip content="该谱系关联的生产工序名称" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.opName" placeholder="请输入工序名称" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 物料与数量 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_material')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料与数量</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_material }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_material">
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item prop="materialCode"><template #label><span>物料编码</span><el-tooltip content="物料的唯一编码，复用仓库主数据" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.materialCode" placeholder="请输入物料编码" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="materialName"><template #label><span>物料名称</span><el-tooltip content="物料的名称" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.materialName" placeholder="请输入物料名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item prop="qtyIn"><template #label><span>投入数量</span><el-tooltip content="投入批次的数量，支持4位小数" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.qtyIn" :precision="4" style="width:100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="qtyOut"><template #label><span>产出数量</span><el-tooltip content="产出批次的数量，支持4位小数" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.qtyOut" :precision="4" style="width:100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 生产信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_prod')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 20h20"/><path d="M5 20V8l7-5 7 5v12"/><path d="M9 20v-6h6v6"/></svg></span>生产信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_prod }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_prod">
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item prop="lineName"><template #label><span>产线</span><el-tooltip content="生产产线名称" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.lineName" placeholder="请输入产线名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="operator"><template #label><span>操作员</span><el-tooltip content="执行该操作的员工" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.operator" placeholder="请输入操作员" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item prop="supplierName"><template #label><span>供应商</span><el-tooltip content="原料批次关联的供应商名称" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.supplierName" placeholder="请输入供应商名称" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 来源与谱系时间 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_trace')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>来源与谱系时间</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_trace }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_trace">
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item prop="sourceType"><template #label><span>来源类型</span><el-tooltip content="谱系数据来源：投料(feeding)、报工(report)、完工(complete)、调拨(transfer)、返工(rework)、合并(merge)、拆分(split)、补录(manual)" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.sourceType" style="width:100%"><el-option v-for="d in qms_genealogy_source" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="traceTime"><template #label><span>谱系时间</span><el-tooltip content="谱系关联建立的时间" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-date-picker v-model="form.traceTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_other')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 断点清单弹窗 ===== -->
    <el-dialog v-model="breakOpen" title="谱系断点清单" width="900px" append-to-body>
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>断点清单展示产出批次未登记的谱系记录，可能导致追溯链中断。请及时补录产出批次信息。</template>
      </el-alert>
      <el-table :data="breakList" border size="small">
        <el-table-column label="投入批次" prop="parentBatchNo" min-width="130" />
        <el-table-column label="工单号" prop="workOrderNo" min-width="110" />
        <el-table-column label="物料" min-width="150"><template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template></el-table-column>
        <el-table-column label="断点原因" prop="breakReason" min-width="200" />
        <el-table-column label="创建时间" prop="createTime" min-width="145" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="s"><el-button text size="small" @click="handleUpdate(s.row); breakOpen = false">补录</el-button></template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="批次谱系管理业务操作说明" width="780px" append-to-body>
      <div class="status-help-content">
        <h4>一、谱系数据说明</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">什么是批次谱系</div>
              <div class="highlight-card-body">批次谱系记录<strong>投入批次 ↔ 工单 ↔ 产出批次</strong>的关联关系链，是实现质量追溯（正反向）的核心数据源</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">正向追溯</div>
              <div class="highlight-card-body">由原料/半成品批次向下追查至<strong>成品批次 → 客户/订单</strong>，回答"这批原料用在了哪里"</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">反向追溯</div>
              <div class="highlight-card-body">由成品批次/客诉向上追查至<strong>原料批次 → 供应商 → 检验记录/NCR</strong>，回答"这个产品用了什么原料"</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title">谱系断点</div>
              <div class="highlight-card-body">投入或产出批次未建立关联时标记为<strong>断点</strong>，导致追溯链中断。需通过补录完善谱系数据</div>
            </div>
          </el-col>
        </el-row>

        <h4>二、来源类型说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="投料(feeding)"><strong>生产投料时</strong>自动生成，记录投入原料批次与工单的关联</el-descriptions-item>
          <el-descriptions-item label="报工(report)"><strong>工单报工时</strong>自动生成，记录工单与产出批次的关联</el-descriptions-item>
          <el-descriptions-item label="完工(complete)"><strong>工单完工入库时</strong>自动生成，记录完工产出批次</el-descriptions-item>
          <el-descriptions-item label="调拨(transfer)"><strong>批次调拨时</strong>自动生成，记录批次转移关系</el-descriptions-item>
          <el-descriptions-item label="返工(rework)"><strong>返工处理时</strong>自动生成，记录返工前后的批次关联</el-descriptions-item>
          <el-descriptions-item label="合并(merge)"><strong>多批合一</strong>时自动生成，记录多个投入批次合并为一个产出批次</el-descriptions-item>
          <el-descriptions-item label="拆分(split)"><strong>一批拆多</strong>时自动生成，记录一个投入批次拆分为多个产出批次</el-descriptions-item>
          <el-descriptions-item label="补录(manual)"><strong>人工补录</strong>，用于存量数据补全或异常数据修正</el-descriptions-item>
        </el-descriptions>

        <h4>三、完整度看板说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="已关联工单">谱系表中已建立完整投入↔产出关联的工单数量（断点标记=0）</el-descriptions-item>
          <el-descriptions-item label="断点工单">存在谱系断点（产出批次未登记）的工单数量（断点标记=1）</el-descriptions-item>
          <el-descriptions-item label="完整率">完整率 = 已关联工单 ÷ (已关联工单 + 断点工单) × 100%，目标值 ≥ 98%</el-descriptions-item>
          <el-descriptions-item label="目标差距">当前完整率与目标值(98%)的差距，负值表示已达标</el-descriptions-item>
        </el-descriptions>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新增谱系：</strong>手动补录谱系数据时，填写投入批次、产出批次、工单号、物料信息等
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看断点清单：</strong>点击「断点清单」按钮查看所有断点记录，识别需补录的谱系
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>补录断点：</strong>在断点清单中点击「补录」按钮，补充产出批次信息以修复断点
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>导出数据：</strong>支持按当前筛选条件导出谱系数据为Excel，用于审计或备份
          </el-timeline-item>
        </el-timeline>

        <h4>五、专业词汇说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="谱系(Genealogy)"><strong>物料批次间的投入-产出关联关系链</strong>，是实现质量追溯的基础数据结构</el-descriptions-item>
          <el-descriptions-item label="正向追溯">由原料/半成品批次向下追查至成品、订单、客户的过程</el-descriptions-item>
          <el-descriptions-item label="反向追溯">由成品批次/客诉向上追查至原料、供应商、工序、检验记录的过程</el-descriptions-item>
          <el-descriptions-item label="断点">投入或产出批次未建立关联，导致追溯链中断的节点</el-descriptions-item>
          <el-descriptions-item label="IQC">Incoming Quality Control（来料质量控制），对供应商交付的原材料进行入库前的质量检验</el-descriptions-item>
          <el-descriptions-item label="NCR">Non-Conformance Report（不合格品报告），记录质量异常并跟踪处理闭环</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsTraceGenealogy">
import { ref, reactive, computed, getCurrentInstance } from 'vue'
import { listGenealogy, getGenealogy, addGenealogy, updateGenealogy, delGenealogy, traceCompleteness, traceBreakList } from '@/api/qms/trace'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_genealogy_index')
const { collapsedCards, toggleCard } = useDetailCard(['v_batch', 'v_material', 'v_prod', 'v_trace', 'v_other', 'e_batch', 'e_material', 'e_prod', 'e_trace', 'e_other'])
const { proxy } = getCurrentInstance()
const { qms_genealogy_source } = proxy.useDict('qms_genealogy_source')

const loading = ref(true)
const list = ref([])
const total = ref(0)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const open = ref(false)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref(null)
const breakOpen = ref(false)
const breakList = ref([])
const completeness = ref({})
const showStatusHelp = ref(false)
const selectedId = ref(null)
const selectedIds = ref([])

const defaultColumns = {
  parentBatchNo: { label: '投入批次', visible: true },
  childBatchNo: { label: '产出批次', visible: true },
  workOrderNo: { label: '工单号', visible: true },
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  opName: { label: '工序', visible: true },
  supplierName: { label: '供应商', visible: true },
  qtyIn: { label: '投入量', visible: true },
  qtyOut: { label: '产出量', visible: true },
  sourceType: { label: '来源', visible: true },
  lineName: { label: '产线', visible: true },
  operator: { label: '操作员', visible: true },
  traceTime: { label: '谱系时间', visible: true },
  breakFlag: { label: '断点', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('qms_genealogy_columns')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, parentBatchNo: undefined, childBatchNo: undefined, workOrderNo: undefined, sourceType: undefined, breakFlag: undefined, materialCode: undefined, materialName: undefined, supplierName: undefined, params: {} }
})
const { queryParams, form } = toRefs(data)

const rules = {
  parentBatchNo: [{ required: true, message: '投入批次号不能为空', trigger: 'blur' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.parentBatchNo) count++
  if (queryParams.value.childBatchNo) count++
  if (queryParams.value.workOrderNo) count++
  if (queryParams.value.sourceType) count++
  if (queryParams.value.breakFlag !== undefined && queryParams.value.breakFlag !== null) count++
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.supplierName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

getList()
loadCompleteness()

function getList() {
  loading.value = true
  listGenealogy(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  })
}
function loadCompleteness() { traceCompleteness().then(res => { completeness.value = res.data }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.parentBatchNo = undefined
  queryParams.value.childBatchNo = undefined
  queryParams.value.workOrderNo = undefined
  queryParams.value.sourceType = undefined
  queryParams.value.breakFlag = undefined
  queryParams.value.materialCode = undefined
  queryParams.value.materialName = undefined
  queryParams.value.supplierName = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}
function setBreakFilter(val) {
  queryParams.value.breakFlag = val
  queryParams.value.pageNum = 1
  getList()
}
function handleSelectionChange(sel) {
  selectedIds.value = sel.map(i => i.id)
  selectedId.value = selectedIds.value[0] || null
}

function reset() {
  form.value = {
    id: undefined, parentBatchNo: undefined, childBatchNo: undefined, workOrderNo: undefined,
    opName: undefined, materialCode: undefined, materialName: undefined,
    qtyIn: undefined, qtyOut: undefined, lineName: undefined, operator: undefined,
    supplierName: undefined, sourceType: 'manual', traceTime: undefined, remark: undefined
  }
  proxy.resetForm('formRef')
}

function handleAdd() { reset(); open.value = true; title.value = '新增批次谱系' }
function handleUpdate(row) {
  reset()
  const id = row?.id || selectedId.value
  if (!id) return
  getGenealogy(id).then(res => { form.value = res.data; open.value = true; title.value = '修改批次谱系' })
}
function handleView(row) {
  getGenealogy(row.id).then(res => { viewData.value = res.data; viewOpen.value = true })
}
function submitForm() {
  proxy.$refs['formRef'].validate(valid => {
    if (!valid) return
    if (form.value.id) {
      updateGenealogy(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList(); loadCompleteness() })
    } else {
      addGenealogy(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList(); loadCompleteness() })
    }
  })
}
function cancel() { open.value = false; reset() }
function handleDelete(row) {
  const delIds = row?.id ? [row.id] : selectedIds.value
  if (!delIds.length) return
  proxy.$modal.confirm('确认删除选中的谱系记录？').then(() => delGenealogy(delIds.join(','))).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList(); loadCompleteness()
  }).catch(() => {})
}
function handleExport() {
  proxy.download('qms/trace/genealogy/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `谱系数据_${new Date().getTime()}.xlsx`)
}
function handleBreakList() { traceBreakList().then(res => { breakList.value = res.data; breakOpen.value = true }) }
</script>

<style scoped>
.qms-genealogy-page {
  padding-top: 10px;
  --brand-50: #eef2ff; --brand-100: #e0e7ff; --brand-200: #c7d2fe;
  --brand-500: #6366f1; --brand-600: #4f46e5; --brand-700: #4338ca;
  --ink-900: #0f172a; --ink-700: #334155; --ink-500: #64748b;
  --ink-400: #94a3b8; --ink-300: #cbd5e1; --ink-200: #e2e8f0;
  --ink-100: #f1f5f9; --ink-50: #f8fafc;
  --green-50: #ecfdf5; --green-500: #10b981; --green-700: #047857;
  --red-50: #fef2f2; --red-500: #ef4444; --red-700: #b91c1c;
  --warning-500: #f59e0b;
  --r-sm: 6px; --r-md: 10px; --r-lg: 14px;
  --shadow-card: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out: cubic-bezier(.16,.84,.44,1);
  color: var(--ink-900);
}
.qms-genealogy-page .surface {
  background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-lg);
  box-shadow: var(--shadow-card); overflow: hidden; margin-bottom: 8px;
}
/* ===== Completeness Overview Cards ===== */
.qms-genealogy-page .completeness-row { margin-bottom: 8px; }
.qms-genealogy-page .completeness-card {
  background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-lg);
  box-shadow: var(--shadow-card); padding: 16px 20px; height: 100%;
  box-sizing: border-box;
}
.qms-genealogy-page .completeness-card__label { font-size: 13px; color: var(--ink-500); margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.qms-genealogy-page .completeness-card__target { font-size: 12px; color: var(--ink-400); }
.qms-genealogy-page .completeness-card__value { font-size: 28px; font-weight: 700; color: var(--ink-900); line-height: 1.2; }
/* ===== Filter Card ===== */
.qms-genealogy-page .filter-card { padding: 14px 20px 16px; }
.qms-genealogy-page .filter-card .filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.qms-genealogy-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--ink-700); }
.qms-genealogy-page .filter-title .glyph { width: 4px; height: 14px; background: var(--brand-600); border-radius: 2px; }
.qms-genealogy-page .filter-card .adv-link { font-size: 14px; color: var(--ink-500); text-decoration: none; display: flex; align-items: center; gap: 4px; transition: color .15s; cursor: pointer; }
.qms-genealogy-page .filter-card .adv-link:hover { color: var(--brand-600); }
.qms-genealogy-page .filter-card .adv-link .chev { transition: transform .2s var(--ease-out); }
.qms-genealogy-page .filter-card .adv-link.is-open .chev { transform: rotate(180deg); }
.qms-genealogy-page .filter-card .filter-bar { display: grid; grid-template-columns: repeat(4, minmax(0,1fr)); gap: 12px 16px; }
.qms-genealogy-page .filter-card .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed var(--ink-200); }
.qms-genealogy-page .filter-card .filter-info { font-size: 13px; color: var(--ink-500); display: flex; align-items: center; gap: 6px; }
.qms-genealogy-page .filter-card .filter-buttons { display: flex; gap: 8px; }
.qms-genealogy-page .field { display: flex; flex-direction: column; gap: 6px; }
.qms-genealogy-page .field label { font-size: 14px; font-weight: 500; color: var(--ink-700); }
.qms-genealogy-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-sm); transition: border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-genealogy-page .field .control:focus-within { border-color: var(--brand-500); box-shadow: 0 0 0 3px rgba(99,102,241,.15); }
.qms-genealogy-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.qms-genealogy-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; color: var(--ink-900); height: 34px; line-height: 34px; }
.qms-genealogy-page .field .control :deep(.el-input__inner::placeholder) { color: var(--ink-400); }
.qms-genealogy-page .field .control :deep(.el-input__prefix) { color: var(--ink-400); margin-right: 4px; }
.qms-genealogy-page .field .control :deep(.el-input__prefix .el-icon) { font-size: 14px; }
.qms-genealogy-page .field .control :deep(.el-select) { width: 100%; }
.qms-genealogy-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; min-height: 34px; height: 34px; }
.qms-genealogy-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow: none !important; }
.qms-genealogy-page .field .control :deep(.el-date-editor) { width: 100%; }
.qms-genealogy-page .field .control :deep(.el-date-editor .el-range-input) { background: transparent; border: 0; font-size: 14px; color: var(--ink-900); }
.qms-genealogy-page .field .control :deep(.el-date-editor .el-range-separator) { color: var(--ink-400); }
.qms-genealogy-page .field .control :deep(.el-date-editor .el-range__icon) { color: var(--ink-400); }
.qms-genealogy-page .field .control :deep(.el-date-editor.el-input) { width: 100%; }
.qms-genealogy-page .field .control :deep(.el-date-editor.el-input .el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.qms-genealogy-page .field .control :deep(.el-date-editor.el-input .el-input__inner) { border: 0; background: transparent; font-size: 14px; color: var(--ink-900); height: 34px; line-height: 34px; }
/* ===== Status Tabs ===== */
.qms-genealogy-page .status-tabs { display: flex; align-items: center; justify-content: space-between; padding: 10px 20px; border-bottom: 1px solid var(--ink-200); background: var(--ink-50); }
.qms-genealogy-page .status-tabs .tabs-track { display: flex; align-items: center; gap: 8px; }
.qms-genealogy-page .status-tabs .tab-btn { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 14px; font-size: 14px; font-weight: 500; border-radius: var(--r-sm); border: 1px solid var(--ink-200); background: #fff; color: var(--ink-500); cursor: pointer; transition: all .15s var(--ease-out); }
.qms-genealogy-page .status-tabs .tab-btn:hover { border-color: var(--ink-300); color: var(--ink-700); }
.qms-genealogy-page .status-tabs .tab-btn.active { background: var(--brand-600); border-color: var(--brand-600); color: #fff; }
.qms-genealogy-page .status-tabs .tab-dot { width: 8px; height: 8px; border-radius: 50%; }
.qms-genealogy-page .status-tabs .dot-green { background: var(--green-500); }
.qms-genealogy-page .status-tabs .dot-red { background: var(--red-500); }
.qms-genealogy-page .status-tabs .tip-pill { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px; font-size: 14px; font-weight: 500; border-radius: var(--r-sm); border: 1px solid #fde68a; background: #fffbeb; color: #b45309; cursor: pointer; transition: all .15s var(--ease-out); white-space: nowrap; }
.qms-genealogy-page .status-tabs .tip-pill:hover { background: #fef3c7; border-color: #f59e0b; }
/* ===== Toolbar ===== */
.qms-genealogy-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid var(--ink-200); background: var(--ink-50); }
.qms-genealogy-page .toolbar .left { display: flex; gap: 8px; align-items: center; }
.qms-genealogy-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.qms-genealogy-page .toolbar-divider { width: 1px; height: 18px; background: var(--ink-200); margin: 0 4px; }
.qms-genealogy-page .btn-soft { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; font-size: 14px; font-weight: 500; border-radius: var(--r-sm); border: 1px solid transparent; cursor: pointer; user-select: none; transition: all .15s var(--ease-out); }
.qms-genealogy-page .btn-soft .el-icon { font-size: 14px; }
.qms-genealogy-page .btn-soft.is-outline { background: #fff; color: var(--ink-700); border-color: var(--ink-200); }
.qms-genealogy-page .btn-soft.is-outline:hover { background: var(--ink-50); border-color: var(--ink-300); color: var(--ink-900); }
.qms-genealogy-page .btn-soft.is-danger-outline { background: #fff; color: var(--red-700); border-color: #fecaca; }
.qms-genealogy-page .btn-soft.is-danger-outline:hover { background: var(--red-50); border-color: var(--red-500); }
.qms-genealogy-page .btn-soft:disabled { opacity: .5; cursor: not-allowed; }
/* ===== Table ===== */
.qms-genealogy-page .table-wrap { overflow-x: auto; }
.qms-genealogy-page .app-table { --el-table-bg-color: #fff; --el-table-header-bg-color: var(--ink-50); --el-table-row-hover-bg-color: #fafbff; --el-table-border-color: transparent; --el-table-text-color: var(--ink-700); --el-table-header-text-color: var(--ink-500); }
.qms-genealogy-page .app-table :deep(.el-table__body td) { border-right-color: transparent !important; }
.qms-genealogy-page .app-table :deep(.el-table__header th) { border-right-color: transparent !important; background: var(--ink-50) !important; color: var(--ink-500); font-weight: 600; font-size: 14px; padding: 12px 16px; border-bottom: 1px solid var(--ink-200); }
.qms-genealogy-page .app-table :deep(.el-table__body td) { padding: 14px 16px; border-bottom: 1px solid var(--ink-100); color: var(--ink-700); }
.qms-genealogy-page .app-table :deep(.el-table__row:hover > td) { background: #fafbff !important; }
.qms-genealogy-page .app-table :deep(.el-table__inner-wrapper::before) { display: none; }
.qms-genealogy-page .col-tip { margin-left: 4px; font-size: 14px; color: var(--ink-400); cursor: help; vertical-align: middle; }
.qms-genealogy-page .col-tip:hover { color: var(--brand-500); }
/* ===== Badge ===== */
.qms-genealogy-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.qms-genealogy-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.qms-genealogy-page .badge.green { background: var(--green-50); color: var(--green-700); border-color: #a7f3d0; }
.qms-genealogy-page .badge.green .dot { background: var(--green-500); }
.qms-genealogy-page .badge.red { background: var(--red-50); color: var(--red-700); border-color: #fecaca; }
.qms-genealogy-page .badge.red .dot { background: var(--red-500); }
/* ===== Dialog Detail Page Styles ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 14px; right: 14px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.rd-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width: 100% !important; }
.rd-detail-header { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -30px; right: -15px; width: 140px; height: 140px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; border-radius: 10px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 4px 12px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: -0.02em; line-height: 1.3; }
.rd-page { max-width: 100%; margin: 0 auto; }
.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-header .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; border-radius: 6px; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-card-header .rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-card-header .rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-card-header .rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }
.rd-card-body { padding: 14px 16px; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 80px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.rd-form-tip:hover { color: #909399; }
.rd-label-tip { margin-left: 4px; font-size: 13px; color: #c0c4cc; cursor: help; vertical-align: middle; }
@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }
.rd-card:nth-child(5) { animation-delay: 0.24s; }
.rd-card:nth-child(6) { animation-delay: 0.30s; }
@media (max-width: 768px) { .rd-grid { grid-template-columns: 1fr; } }
/* ===== Status Help Dialog ===== */
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-info { background-color: #f4f4f5; border-color: #d3d4d6; }
.status-help-content .highlight-info .highlight-card-title { color: #909399; }
/* ===== Responsive ===== */
@media (max-width: 1100px) { .qms-genealogy-page .filter-card .filter-bar { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 720px) { .qms-genealogy-page .filter-card .filter-bar { grid-template-columns: 1fr; } .qms-genealogy-page .toolbar { flex-wrap: wrap; gap: 10px; } }
</style>