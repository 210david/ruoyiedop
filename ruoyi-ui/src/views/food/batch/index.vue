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
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>批次状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.batchStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_batch_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>效期状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.expireStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_expire_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>到期日期</label>
          <div class="control">
            <el-date-picker v-model="expireDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['food:batch:add']">新增批次</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['food:batch:remove']">删除</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['food:batch:export']">导出</el-button>
          <el-button type="success" plain icon="MagicStick" @click="handleExpiryAdvice" v-hasPermi="['food:batch:list']">AI出库建议</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="food_batch_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="batchList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="批次号" prop="batchCode" key="batchCode" :min-width="colWidth('batchCode', 170)" resizable show-overflow-tooltip v-if="columns.batchCode.visible">
            <template #default="scope">
              <el-link type="primary" @click="handleDetail(scope.row)">{{ scope.row.batchCode }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="物料" prop="batchMaterial" key="batchMaterial" :min-width="colWidth('batchMaterial', 200)" resizable show-overflow-tooltip v-if="columns.batchMaterial.visible">
            <template #default="scope">{{ scope.row.materialCode }} / {{ scope.row.materialName }}</template>
          </el-table-column>
          <el-table-column label="数量" prop="qty" key="qty" :width="colWidth('qty', 100)" resizable align="right" class-name="col-num" v-if="columns.qty.visible">
            <template #default="scope">{{ formatQty(scope.row.qty) }} <span class="badge blue">{{ unitLabel(scope.row.qtyUnit) }}</span></template>
          </el-table-column>
          <el-table-column label="生产日期" prop="produceDate" key="produceDate" :width="colWidth('produceDate', 110)" resizable align="center" v-if="columns.produceDate.visible" />
          <el-table-column label="到期日期" prop="expireDate" key="expireDate" :width="colWidth('expireDate', 110)" resizable align="center" sortable v-if="columns.expireDate.visible" />
          <el-table-column label="剩余天数" prop="daysRemain" key="daysRemain" :width="colWidth('daysRemain', 100)" resizable align="center" v-if="columns.daysRemain.visible">
            <template #default="scope">
              <span :class="daysClass(scope.row)">{{ daysToExpire(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="批次状态" prop="batchStatus" key="batchStatus" :width="colWidth('batchStatus', 90)" resizable align="center" v-if="columns.batchStatus.visible">
            <template #default="scope">
              <span class="badge" :class="batchStatusClass(scope.row.batchStatus)"><span class="dot"></span>{{ dictLabel(food_batch_status, scope.row.batchStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="效期状态" prop="expireStatus" key="expireStatus" :width="colWidth('expireStatus', 90)" resizable align="center" v-if="columns.expireStatus.visible">
            <template #default="scope">
              <span class="badge" :class="expireStatusClass(scope.row.expireStatus)"><span class="dot"></span>{{ dictLabel(food_expire_status, scope.row.expireStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="来源" prop="sourceType" key="sourceType" :width="colWidth('sourceType', 90)" resizable align="center" v-if="columns.sourceType.visible">
            <template #default="scope">
              <span class="badge violet">{{ dictLabel(food_source_type, scope.row.sourceType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 250)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button v-if="scope.row.batchStatus === '0'" link type="success" icon="Check" @click="handleQualify(scope.row)" v-hasPermi="['food:batch:qualify']">合格</el-button>
                <el-button v-if="['0','1'].includes(scope.row.batchStatus)" link type="warning" icon="Lock" @click="handleFreeze(scope.row)" v-hasPermi="['food:batch:freeze']">冻结</el-button>
                <el-button v-if="scope.row.batchStatus === '2'" link type="primary" icon="Unlock" @click="handleUnfreeze(scope.row)" v-hasPermi="['food:batch:freeze']">解冻</el-button>
                <el-button v-if="scope.row.batchStatus !== '3'" link type="danger" icon="CircleClose" @click="handleScrap(scope.row)" v-hasPermi="['food:batch:scrap']">报废</el-button>
                <el-button link type="info" icon="View" @click="handleDetail(scope.row)">详情</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/编辑批次 -->
    <el-dialog v-model="open" width="620px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="batchRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 物料信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('b1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.b1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.b1">
              <el-form-item label="物料" prop="materialId">
                <el-input :model-value="[form.materialCode, form.materialName].filter(Boolean).join(' - ')" readonly :disabled="form.batchId != null" placeholder="点击选择物料" @click="openMaterialPicker">
                  <template v-if="form.materialCode" #append><el-button icon="CircleClose" @click.stop="clearMaterial" /></template>
                  <template v-else #append><el-button icon="Search" @click="openMaterialPicker" /></template>
                </el-input>
              </el-form-item>
              <el-form-item label="外部批次号" prop="extBatchCode">
                <el-input v-model="form.extBatchCode" placeholder="供应商批次号等外部批次" />
              </el-form-item>
            </div>
          </section>
          <!-- 效期信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('b2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>效期信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.b2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.b2">
              <el-form-item label="生产日期" prop="produceDate">
                <el-date-picker v-model="form.produceDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生产日期" style="width: 100%" />
              </el-form-item>
              <el-form-item label="保质期天数" prop="shelfLifeDays">
                <el-input-number v-model="form.shelfLifeDays" :min="0" controls-position="right" style="width: 100%" />
              </el-form-item>
              <el-form-item label="到期日期" prop="expireDate">
                <el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="留空自动按生产日期+保质期推算" style="width: 100%" />
              </el-form-item>
            </div>
          </section>
          <!-- 数量与状态 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('b3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="16 18 23 18 23 11"/></svg></span>数量与状态</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.b3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.b3">
              <el-form-item label="批次数量" prop="qty">
                <el-input-number v-model="form.qty" :min="0" :precision="2" controls-position="right" style="width: 100%" />
              </el-form-item>
              <el-form-item label="单位" prop="qtyUnit">
                <el-select v-model="form.qtyUnit" placeholder="选择物料后自动带出，可修改" style="width: 100%" clearable>
                  <el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="批次状态" prop="batchStatus">
                <el-radio-group v-model="form.batchStatus">
                  <el-radio v-for="d in food_batch_status" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
          </section>
          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('b4')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.b4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.b4">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

    <!-- 批次详情 -->
    <el-dialog v-model="detailOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">批次详情</span>
          <div class="rd-detail-header-sub" v-if="detail.batch">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ detail.batch.batchCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- ===== 基本信息 ===== -->
          <el-tab-pane label="基本信息" name="info">
            <!-- 批次信息 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('d1')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>批次信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.d1">
                <div class="rd-grid" v-if="detail.batch">
                  <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ detail.batch.batchCode }}</div></div>
                  <div class="rd-item"><span class="rd-label">外部批次</span><div class="rd-value">{{ detail.batch.extBatchCode || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">来源类型</span><div class="rd-value"><dict-tag :options="food_source_type" :value="detail.batch.sourceType" /></div></div>
                  <div class="rd-item"><span class="rd-label">批次状态</span><div class="rd-value"><dict-tag :options="food_batch_status" :value="detail.batch.batchStatus" /></div></div>
                  <div class="rd-item"><span class="rd-label">效期状态</span><div class="rd-value"><dict-tag :options="food_expire_status" :value="detail.batch.expireStatus" /></div></div>
                </div>
              </div>
            </section>
            <!-- 物料与数量 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('d2')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="16 18 23 18 23 11"/></svg></span>物料与数量</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.d2">
                <div class="rd-grid" v-if="detail.batch">
                  <div class="rd-item rd-item--full"><span class="rd-label">物料</span><div class="rd-value">{{ detail.batch.materialCode }} / {{ detail.batch.materialName }}</div></div>
                  <div class="rd-item"><span class="rd-label">批次数量</span><div class="rd-value">{{ formatQty(detail.batch.qty) }} {{ unitLabel(detail.batch.qtyUnit) }}</div></div>
                  <div class="rd-item"><span class="rd-label">保质期</span><div class="rd-value">{{ detail.batch.shelfLifeDays ? detail.batch.shelfLifeDays + ' 天' : '-' }}</div></div>
                </div>
              </div>
            </section>
            <!-- 效期信息 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('d3')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>效期信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.d3">
                <div class="rd-grid" v-if="detail.batch">
                  <div class="rd-item"><span class="rd-label">生产日期</span><div class="rd-value">{{ detail.batch.produceDate || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ detail.batch.expireDate || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">剩余天数</span><div class="rd-value">{{ detail.batch.daysRemain != null ? detail.batch.daysRemain + ' 天' : '-' }}</div></div>
                </div>
              </div>
            </section>
            <!-- 其他信息 -->
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('d4')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>其他信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.d4">
                <div class="rd-grid" v-if="detail.batch">
                  <div class="rd-item rd-item--full"><span class="rd-label">冻结原因</span><div class="rd-value" :class="{ 'rd-value--muted': !detail.batch.freezeReason }">{{ detail.batch.freezeReason || '-' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !detail.batch.remark }">{{ detail.batch.remark || '-' }}</div></div>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <!-- ===== 谱系追溯 ===== -->
          <el-tab-pane label="谱系追溯" name="genealogy">
            <h4 class="tab-title">向上追溯（来源批次）</h4>
            <el-table :data="genealogyUp" size="small" border>
              <el-table-column label="父批次号" prop="parentBatchCode" min-width="150" />
              <el-table-column label="关系" width="90" align="center">
                <template #default="scope">
                  <dict-tag :options="food_relation_type" :value="scope.row.relationType" />
                </template>
              </el-table-column>
              <el-table-column label="数量" prop="qty" width="90" align="right" />
              <el-table-column label="关联单据" prop="refBillCode" min-width="130">
                <template #default="scope">{{ scope.row.refBillCode || '-' }}</template>
              </el-table-column>
            </el-table>
            <h4 class="tab-title">向下追踪（派生批次）</h4>
            <el-table :data="genealogyDown" size="small" border>
              <el-table-column label="子批次号" prop="childBatchCode" min-width="150" />
              <el-table-column label="关系" width="90" align="center">
                <template #default="scope">
                  <dict-tag :options="food_relation_type" :value="scope.row.relationType" />
                </template>
              </el-table-column>
              <el-table-column label="数量" prop="qty" width="90" align="right" />
              <el-table-column label="关联单据" prop="refBillCode" min-width="130">
                <template #default="scope">{{ scope.row.refBillCode || '-' }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <!-- ===== 批次库存 ===== -->
          <el-tab-pane label="批次库存" name="stock">
            <el-table :data="stockList" size="small" border>
              <el-table-column label="仓库" prop="warehouseName" min-width="120" />
              <el-table-column label="库位" prop="locationName" min-width="120" />
              <el-table-column label="库存量" prop="qty" width="100" align="right" />
              <el-table-column label="锁定/预留" prop="qtyLocked" width="100" align="right" />
              <el-table-column label="可用量" width="100" align="right">
                <template #default="scope">{{ formatQty((Number(scope.row.qty) || 0) - (Number(scope.row.qtyLocked) || 0)) }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <!-- ===== 状态日志 ===== -->
          <el-tab-pane label="状态日志" name="log">
            <el-table :data="statusLogs" size="small" border>
              <el-table-column label="操作类型" prop="operateType" width="130" />
              <el-table-column label="迁移" min-width="120">
                <template #default="scope">
                  {{ statusLabel(scope.row.fromStatus) }} → {{ statusLabel(scope.row.toStatus) }}
                </template>
              </el-table-column>
              <el-table-column label="原因" prop="reason" min-width="160" show-overflow-tooltip />
              <el-table-column label="操作人" prop="operateBy" width="100" />
              <el-table-column label="操作时间" prop="operateTime" width="160" />
            </el-table>
          </el-tab-pane>
          <!-- ===== 销售预留 ===== -->
          <el-tab-pane label="销售预留" name="reserve">
            <div style="margin-bottom: 8px">
              <el-button type="primary" size="small" icon="Plus" @click="handleAddReserve" v-hasPermi="['food:batch:edit']">新增预留</el-button>
            </div>
            <el-table :data="reserveList" size="small" border>
              <el-table-column label="客户" prop="customerName" min-width="120" show-overflow-tooltip />
              <el-table-column label="关联单据" prop="orderBillCode" min-width="120">
                <template #default="scope">{{ scope.row.orderBillCode || '-' }}</template>
              </el-table-column>
              <el-table-column label="预留量" prop="qty" width="90" align="right" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="scope">
                  <dict-tag :options="food_reserve_status" :value="scope.row.status" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="130" align="center" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button v-if="scope.row.status === '0'" link type="primary" size="small" @click="handleReleaseReserve(scope.row)" v-hasPermi="['food:batch:edit']">释放</el-button>
                    <el-button link type="danger" size="small" @click="handleDeleteReserve(scope.row)" v-hasPermi="['food:batch:remove']">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 新增预留 -->
    <el-dialog v-model="reserveOpen" width="480px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">新增批次预留</span>
        </div>
      </template>
      <el-form ref="reserveRef" :model="reserveForm" :rules="reserveRules" label-width="90px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rs1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>预留信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rs1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.rs1">
              <el-form-item label="预留数量" prop="qty">
                <el-input-number v-model="reserveForm.qty" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
              </el-form-item>
              <el-form-item label="客户名称" prop="customerName">
                <el-input v-model="reserveForm.customerName" placeholder="请输入客户名称" />
              </el-form-item>
              <el-form-item label="关联单据" prop="orderBillCode">
                <el-input v-model="reserveForm.orderBillCode" placeholder="销售订单号等（选填）" />
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="reserveForm.remark" type="textarea" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitReserve">确 定</el-button>
          <el-button @click="reserveOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 物料选择器 -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />

    <!-- AI出库建议 -->
    <el-dialog title="AI临期批次出库建议（FEFO）" v-model="adviceOpen" width="860px" append-to-body>
      <el-alert v-if="adviceData.summary" type="success" :closable="false" show-icon style="margin-bottom: 12px; white-space: pre-wrap;">
        <template #title>
          <span style="white-space: pre-wrap;">{{ adviceData.summary }}</span>
        </template>
      </el-alert>
      <el-alert v-else-if="adviceLoaded && adviceData.total > 0" type="info" :closable="false" show-icon style="margin-bottom: 12px;">
        <template #title>
          <span>共 {{ adviceData.total }} 个批次在30天内到期，已按 FEFO 由近及远排序；AI 智能建议暂不可用（大模型额度或开关未就绪），数据仅供参考</span>
        </template>
      </el-alert>
      <el-empty v-if="adviceLoaded && adviceData.total === 0" description="近30天无临期/过期批次" :image-size="80" />
      <el-table v-else v-loading="!adviceLoaded" element-loading-text="AI 分析中..." :data="adviceData.rows || []" size="small" max-height="380">
        <el-table-column label="批次号" prop="批次号" width="180" />
        <el-table-column label="物料" prop="物料" min-width="140" show-overflow-tooltip />
        <el-table-column label="批次数量" prop="批次数量" width="90" align="right" />
        <el-table-column label="单位" prop="单位" width="70" align="center">
          <template #default="scope">{{ wms_unit[scope.row['单位']]?.label || scope.row['单位'] }}</template>
        </el-table-column>
        <el-table-column label="到期日期" prop="到期日期" width="110" align="center" />
        <el-table-column label="剩余天数" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row['剩余天数'] < 0" type="danger">过期{{ Math.abs(scope.row['剩余天数']) }}天</el-tag>
            <el-tag v-else-if="scope.row['剩余天数'] <= 7" type="warning">{{ scope.row['剩余天数'] }}天</el-tag>
            <span v-else>{{ scope.row['剩余天数'] }}天</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="状态" width="80" align="center" />
      </el-table>
      <template #footer>
        <el-button @click="adviceOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="批次管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、批次状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待检</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">合格</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">冻结</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">报废</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待检">新批次初始状态。效期管理物料收货后自动生成待检批次，处于检验隔离期；<strong>待检批次不可出库发货</strong>，须待检验完成或质量放行</el-descriptions-item>
          <el-descriptions-item label="合格">检验合格或质量员手动放行后的正常状态，批次可参与出库分配（按效期先进先出 FEFO）、可投入生产使用</el-descriptions-item>
          <el-descriptions-item label="冻结">质量异常控制状态：检验不合格、召回、CCP 监控超限或人工发现异常时触发；<strong>冻结批次禁止出库、禁止移动</strong>，查明原因后可解冻恢复或转报废</el-descriptions-item>
          <el-descriptions-item label="报废">批次终态（不可逆）：确认不可用后执行报废，须通过报废出库将货物移出库存并全程留痕</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">批次自动生成</div>
              <div class="highlight-card-body">启用效期管理的物料<strong>收货后自动生成待检批次</strong>，无需手工建档；手工填写的供应商批次仅作为外部参考号记录。生产完工自动建成品批次并建立投入产出谱系</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">状态与库存联动</div>
              <div class="highlight-card-body">出库分配<strong>只认合格批次</strong>，待检/冻结/报废批次自动排除在可发列表之外；批次库存与仓储库存双写同步，状态变更实时生效</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">质量与效期双维度</div>
              <div class="highlight-card-body">「批次状态」管质量（待检/合格/冻结/报废），「效期状态」管时间（正常/临期/过期），两条线独立预警；临期批次触发 FEFO 优先出库建议</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">全程留痕可追溯</div>
              <div class="highlight-card-body">每次状态变更（谁在何时合格/冻结/报废）自动写入<strong>状态变更日志</strong>；批次详情可查看谱系、库存分布与占用记录，满足召回追溯要求</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>批次产生：</strong>采购/生产/退货入库时系统自动生成待检批次；调拨入库沿用原批次；也可在本页「新增批次」手工建档
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>检验放行：</strong>QMS 检验合格后自动放行批次；紧急情况下质量员也可在本页对批次点「合格」手动放行
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>异常处置：</strong>发现质量异常点「冻结」（同时支持 QMS 不合格、召回单、CCP 超限自动触发）；调查后「解冻」恢复或「报废」处理
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>效期监控：</strong>关注列表中「剩余天数/效期状态」，临期批次参考「AI出库建议」优先安排出库，避免过期报废损失
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FoodBatch">
import { listBatch, getBatch, addBatch, updateBatch, delBatch, qualifyBatch, freezeBatch, unfreezeBatch, scrapBatch, genealogy, batchStock, statusLog, listReserve, addReserve, releaseReserve, deleteReserve, expiryAdvice } from "@/api/food/batch"
import { listMaterial } from "@/api/wms/material"
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['b1', 'b2', 'b3', 'b4', 'd1', 'd2', 'd3', 'd4', 'd5', 'rs1'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('food_batch_index')
const { food_batch_status, food_expire_status, food_source_type, food_relation_type, food_reserve_status, wms_unit } = proxy.useDict("food_batch_status", "food_expire_status", "food_source_type", "food_relation_type", "food_reserve_status", "wms_unit")

// 列显隐配置
const defaultColumns = {
  batchCode: { label: '批次号', visible: true },
  batchMaterial: { label: '物料', visible: true },
  qty: { label: '数量', visible: true },
  produceDate: { label: '生产日期', visible: true },
  expireDate: { label: '到期日期', visible: true },
  daysRemain: { label: '剩余天数', visible: true },
  batchStatus: { label: '批次状态', visible: true },
  expireStatus: { label: '效期状态', visible: true },
  sourceType: { label: '来源', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('food_batch_columns')
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

const batchList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const expireDateRange = ref([])
const materialPickerRef = ref(null)

const detailOpen = ref(false)
const detail = ref({})
const activeTab = ref("info")
const genealogyUp = ref([])
const genealogyDown = ref([])
const stockList = ref([])
const statusLogs = ref([])
const reserveList = ref([])

const reserveOpen = ref(false)
const reserveForm = ref({})
const reserveRules = { qty: [{ required: true, message: "预留数量不能为空", trigger: "blur" }] }

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1, pageSize: 10,
    batchCode: undefined, materialName: undefined, batchStatus: undefined, expireStatus: undefined,
    params: {}
  },
  rules: {
    materialId: [{ required: true, message: "物料不能为空", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.batchCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.batchStatus) count++
  if (queryParams.value.expireStatus) count++
  if (expireDateRange.value && expireDateRange.value.length > 0) count++
  return count
})

watch(expireDateRange, val => {
  queryParams.value.params = {
    beginExpireDate: val && val.length ? val[0] : undefined,
    endExpireDate: val && val.length ? val[1] : undefined
  }
})

function formatQty(v) {
  return v == null ? "0" : Number(v).toLocaleString(undefined, { maximumFractionDigits: 2 })
}

/** 单位字典值转标签（存量文本数据回退原值显示） */
function unitLabel(v) {
  if (!v) return ""
  const item = wms_unit.value.find(d => d.value == v)
  return item ? item.label : v
}

/** 字典值转标签 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}

function batchStatusClass(v) {
  return { "0": "amber", "1": "green", "2": "blue", "3": "red" }[v] || "gray"
}

function expireStatusClass(v) {
  return { "0": "green", "1": "amber", "2": "red", "3": "gray" }[v] || "gray"
}

function daysToExpire(row) {
  if (!row.expireDate) return "-"
  const diff = Math.floor((new Date(row.expireDate) - new Date(new Date().toDateString())) / 86400000)
  if (diff < 0) return "已过期"
  return diff + " 天"
}

function daysClass(row) {
  if (!row.expireDate) return ""
  const diff = Math.floor((new Date(row.expireDate) - new Date(new Date().toDateString())) / 86400000)
  if (diff < 0) return "days-danger"
  if (diff <= 30) return "days-warn"
  return ""
}

function statusLabel(v) {
  const map = { "0": "待检", "1": "合格", "2": "冻结", "3": "报废" }
  return map[v] || "-"
}

// ---------------- 状态卡片统计 ----------------

const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const statusTabList = computed(() => food_batch_status.value)
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-done', '2': 'tab-audit', '3': 'tab-partial' }; return map[value] || '' }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.batchStatus = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() { listBatch({ pageNum: 1, pageSize: 999 }).then(res => { const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0 }; (res.rows || []).forEach(r => { if (counts[r.batchStatus] !== undefined) counts[r.batchStatus]++ }); statusCounts.value = counts }).catch(() => {}) }

function getList() {
  loading.value = true
  listBatch(queryParams.value).then(response => {
    batchList.value = response.rows
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
  queryParams.value.batchCode = undefined
  queryParams.value.materialName = undefined
  queryParams.value.batchStatus = undefined
  queryParams.value.expireStatus = undefined
  expireDateRange.value = []
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.batchId)
  multiple.value = !selection.length
}

function reset() {
  form.value = { batchId: undefined, materialId: undefined, materialCode: undefined, materialName: undefined, extBatchCode: undefined, produceDate: undefined, shelfLifeDays: undefined, expireDate: undefined, qty: 0, qtyUnit: "", batchStatus: "0", remark: undefined }
  proxy.resetForm("batchRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增批次（初始化来源，批次号自动生成）"
}

function openMaterialPicker() {
  materialPickerRef.value.open(form.value.materialId)
}

function clearMaterial() {
  form.value.materialId = undefined
  form.value.materialCode = undefined
  form.value.materialName = undefined
  form.value.qtyUnit = ""
}

function onMaterialPickerConfirm(material) {
  form.value.materialId = material.materialId
  form.value.materialCode = material.materialCode
  form.value.materialName = material.materialName
  form.value.qtyUnit = material.unit || ""
  listMaterial({ pageNum: 1, pageSize: 1, materialId: material.materialId }).then(res => {
    const m = (res.rows || [])[0]
    if (m && m.shelfLifeDays) form.value.shelfLifeDays = m.shelfLifeDays
  })
}

/** 取消按钮 */
function cancel()
{
  open.value = false
  reset()
}

function submitForm() {
  proxy.$refs["batchRef"].validate(valid => {
    if (valid) {
      if (form.value.batchId != null) {
        updateBatch(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
          loadStatusCounts()
        })
      } else {
        addBatch(form.value).then(res => {
          proxy.$modal.msgSuccess("批次创建成功：" + (res.data || ""))
          open.value = false
          getList()
          loadStatusCounts()
        })
      }
    }
  })
}

function handleDelete(row) {
  const deleteIds = row.batchId || ids.value
  proxy.$modal.confirm("是否确认删除所选批次？").then(() => delBatch(deleteIds)).then(() => {
    getList()
    loadStatusCounts()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("food/batch/export", { ...queryParams.value }, `batch_${new Date().getTime()}.xlsx`)
}

// ---------------- AI出库建议 ----------------

const adviceOpen = ref(false)
const showStatusHelp = ref(false)
const adviceLoaded = ref(false)
const adviceData = ref({})

function handleExpiryAdvice() {
  adviceOpen.value = true
  adviceLoaded.value = false
  adviceData.value = {}
  expiryAdvice(30).then(res => {
    adviceData.value = res.data || {}
    adviceLoaded.value = true
  })
}

// ---------------- 状态操作 ----------------

function handleQualify(row) {
  proxy.$modal.prompt("请输入合格判定备注（选填）").then(({ value }) => {
    return qualifyBatch(row.batchId, value)
  }).then(() => {
    proxy.$modal.msgSuccess("已判定合格")
    getList()
    loadStatusCounts()
  }).catch(() => {})
}

function handleFreeze(row) {
  proxy.$modal.prompt("请输入冻结原因").then(({ value }) => {
    return freezeBatch(row.batchId, value)
  }).then(() => {
    proxy.$modal.msgSuccess("已冻结")
    getList()
    loadStatusCounts()
  }).catch(() => {})
}

function handleUnfreeze(row) {
  proxy.$modal.confirm("确认解冻批次【" + row.batchCode + "】吗？解冻后状态将变为合格。").then(() => unfreezeBatch(row.batchId)).then(() => {
    proxy.$modal.msgSuccess("已解冻")
    getList()
    loadStatusCounts()
  }).catch(() => {})
}

function handleScrap(row) {
  proxy.$modal.prompt("请输入报废原因（必填）").then(({ value }) => {
    if (!value) {
      proxy.$modal.msgError("报废原因不能为空")
      return Promise.reject()
    }
    return scrapBatch(row.batchId, value)
  }).then(() => {
    proxy.$modal.msgSuccess("已报废")
    getList()
    loadStatusCounts()
  }).catch(() => {})
}

// ---------------- 详情抽屉 ----------------

function handleDetail(row) {
  getBatch(row.batchId).then(res => {
    detail.value = { batch: res.data }
    detailOpen.value = true
    activeTab.value = "info"
    loadDetailData(row.batchId)
  })
}

function loadDetailData(batchId) {
  genealogy(batchId, "up").then(res => { genealogyUp.value = res.data || [] })
  genealogy(batchId, "down").then(res => { genealogyDown.value = res.data || [] })
  batchStock(batchId).then(res => { stockList.value = res.data || [] })
  statusLog(batchId).then(res => { statusLogs.value = res.data || [] })
  listReserve({ pageNum: 1, pageSize: 50, batchId: batchId }).then(res => { reserveList.value = res.rows || [] })
}

// ---------------- 预留 ----------------

function handleAddReserve() {
  reserveForm.value = { batchId: detail.value.batch.batchId, qty: undefined, customerName: undefined, orderBillCode: undefined, remark: undefined }
  reserveOpen.value = true
}

function submitReserve() {
  proxy.$refs["reserveRef"].validate(valid => {
    if (valid) {
      addReserve(reserveForm.value).then(() => {
        proxy.$modal.msgSuccess("预留成功")
        reserveOpen.value = false
        loadDetailData(detail.value.batch.batchId)
      })
    }
  })
}

function handleReleaseReserve(row) {
  proxy.$modal.confirm("确认释放该预留吗？").then(() => releaseReserve(row.reserveId)).then(() => {
    proxy.$modal.msgSuccess("已释放")
    loadDetailData(detail.value.batch.batchId)
  }).catch(() => {})
}

function handleDeleteReserve(row) {
  proxy.$modal.confirm("确认删除该预留记录吗？预留中的记录删除后将自动解锁库存。").then(() => deleteReserve(row.reserveId)).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    loadDetailData(detail.value.batch.batchId)
  }).catch(() => {})
}

onMounted(() => {
  getList()
  loadStatusCounts()
})
</script>

<style scoped>
.days-warn { color: #e6a23c; font-weight: 600; }
.days-danger { color: #f56c6c; font-weight: 600; }
.tab-title { margin: 8px 0 6px; font-size: 13px; color: #606266; }
.detail-tabs :deep(.el-tabs__header) { margin-bottom: 12px; }
.detail-tabs :deep(.el-tab-pane) { min-height: 180px; }
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.flow-item { display: flex; align-items: center; gap: 8px; }
.flow-arrow { color: #909399; font-size: 16px; }
.highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
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
