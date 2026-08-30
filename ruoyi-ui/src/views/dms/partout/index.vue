<template>
  <div class="app-container dms-partout-page">
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
          <label>单据号</label>
          <div class="control">
            <el-input v-model="queryParams.documentCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>出库类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.targetType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in dms_partout_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>领用部门</label>
          <div class="control">
            <el-input v-model="queryParams.supplierOrDept" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>备件编号</label>
          <div class="control">
            <el-input v-model="queryParams.partCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>备件名称</label>
          <div class="control">
            <el-input v-model="queryParams.partName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>领用人</label>
          <div class="control">
            <el-input v-model="queryParams.receiver" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>出库日期</label>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:partout:add']">新增出库</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:partstock:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['dms:partout:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_partout_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="单据号" prop="documentCode" key="documentCode" :width="colWidth('documentCode', 140)" resizable v-if="columns.documentCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.documentCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件编号" prop="partCode" key="partCode" :width="colWidth('partCode', 120)" resizable v-if="columns.partCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.partCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件名称" prop="partName" key="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip v-if="columns.partName.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span class="badge amber">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="出库类型" prop="targetType" key="targetType" :width="colWidth('targetType', 100)" resizable align="center" v-if="columns.targetType.visible">
            <template #default="scope"><span class="badge violet">{{ targetTypeLabel(scope.row.targetType) }}</span></template>
          </el-table-column>
          <el-table-column label="领用部门" prop="supplierOrDept" key="supplierOrDept" :width="colWidth('supplierOrDept', 150)" resizable show-overflow-tooltip v-if="columns.supplierOrDept.visible" />
          <el-table-column label="领用人" prop="receiver" key="receiver" :width="colWidth('receiver', 100)" resizable v-if="columns.receiver.visible" />
          <el-table-column label="出库数量" prop="quantity" key="quantity" :width="colWidth('quantity', 90)" resizable align="center" v-if="columns.quantity.visible" />
          <el-table-column label="出库日期" prop="operateDate" key="operateDate" :width="colWidth('operateDate', 120)" resizable align="center" v-if="columns.operateDate.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
        <template #default="scope">
          <div class="action-btn-row">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['dms:partout:query']">查看</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:partout:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:partstock:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">出库记录详情</div>
            <div class="rd-detail-header-sub" v-if="viewForm.documentCode">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">单据号：{{ viewForm.documentCode }}</span>
            </div>
          </div>
        </div>
      </template>
      <div v-loading="viewLoading" class="rd-page">
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">单据号</span><div class="rd-value">{{ viewForm.documentCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">出库类型</span><div class="rd-value"><dict-tag :options="dms_partout_type" :value="viewForm.targetType" /></div></div>
              <div class="rd-item"><span class="rd-label">出库日期</span><div class="rd-value">{{ viewForm.operateDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">经办人</span><div class="rd-value">{{ viewForm.operatorName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 备件信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>备件信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">备件编号</span><div class="rd-value">{{ viewForm.partCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">备件名称</span><div class="rd-value">{{ viewForm.partName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ viewForm.warehouseName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewForm.unit" /></div></div>
            </div>
          </div>
        </section>
        <!-- 领用信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>领用信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">出库数量</span><div class="rd-value rd-amount">{{ viewForm.quantity != null ? viewForm.quantity : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">领用人</span><div class="rd-value">{{ viewForm.receiver || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">领用部门</span><div class="rd-value">{{ viewForm.supplierOrDept || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewForm.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增出库弹窗 -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="partoutRef" :model="form" :rules="rules" label-width="100px" :disabled="formDisabled">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row>
          <el-col :span="12">
            <el-form-item label="单据号" prop="documentCode">
              <el-input v-model="form.documentCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出库类型" prop="targetType">
              <el-select v-model="form.targetType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in dms_partout_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>备件信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="12">
            <el-form-item label="备件编号" prop="stockId">
              <el-input v-model="form.partCode" readonly placeholder="请选择备件（仅显示有库存的）" style="width: 100%" @click="openSparePartPicker" :disabled="!!form.recordId">
                <template v-if="form.partCode && !form.recordId" #append>
                  <el-button icon="CircleClose" @click.stop="clearSparePart" />
                </template>
                <template v-else #append>
                  <el-button icon="Search" @click="openSparePartPicker" :disabled="!!form.recordId" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备件名称" prop="partName">
              <el-input v-model="form.partName" placeholder="选择备件后自动带出" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数量" prop="currentStock">
              <el-input v-model="form.currentStock" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <dict-tag :options="wms_unit" :value="form.unit" />
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>领用信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row>
          <el-col :span="12">
            <el-form-item label="出库日期" prop="operateDate">
              <el-date-picker v-model="form.operateDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出库数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0.01" :max="form.currentStock || 9999" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="领用人" prop="receiver">
              <el-input v-model="form.receiver" readonly placeholder="请选择领用人" style="width: 100%" @click="openReceiverPicker">
                <template #append>
                  <el-button icon="Search" @click="openReceiverPicker" />
                </template>
                <template #suffix>
                  <el-icon v-if="form.receiver" class="clear-icon" @click.stop="clearReceiver"><CircleClose /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="领用部门" prop="deptId">
              <el-input v-model="form.supplierOrDept" readonly placeholder="请选择领用部门" style="width: 100%" @click="openDeptPicker">
                <template #append>
                  <el-button icon="Search" @click="openDeptPicker" />
                </template>
                <template #suffix>
                  <el-icon v-if="form.supplierOrDept" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm" v-if="!formDisabled">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 领用人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择领用人" @confirm="onReceiverPickerConfirm" />

    <!-- 领用部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择领用部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />

    <!-- 备件选择弹窗（备件库存台账，仅显示有库存的） -->
    <spare-part-picker ref="sparePartPickerRef" source="stock" title="选择备件" @confirm="onSparePartPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="备件出库业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">选择出库类型</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">选择备件</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">填写领用信息</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">库存自动扣减</el-tag>
          </div>
        </div>

        <h4>二、出库类型说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="维修领用">工单维修时领用备件，工单验收后系统自动生成出库单</el-descriptions-item>
          <el-descriptions-item label="报废出库">备件损坏或过期，直接报废出库</el-descriptions-item>
          <el-descriptions-item label="调拨出库">调拨至其他仓库或部门</el-descriptions-item>
          <el-descriptions-item label="其他出库">盘亏出库等其他场景</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">库存校验</div>
              <div class="highlight-card-body">出库数量<strong>不能超过当前库存</strong>，系统自动校验并限制最大可出数量</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">仅显示有库存备件</div>
              <div class="highlight-card-body">备件选择列表<strong>仅显示库存大于0的备件</strong>，避免无库存出库</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">工单自动出库</div>
              <div class="highlight-card-body">工单验收通过后，若使用了备件，系统<strong>自动创建出库单</strong>并扣减库存，无需手动操作</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">领用信息必填</div>
              <div class="highlight-card-body">出库必须填写<strong>领用人和领用部门</strong>，确保备件去向可追溯</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>选择出库类型：</strong>根据实际场景选择对应的出库类型
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>选择备件：</strong>从有库存的备件中选择，系统显示当前库存数量
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>填写领用信息：</strong>输入出库数量（不超过库存）、选择领用人和领用部门</el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>保存出库：</strong>保存后库存自动扣减，可在备件台账中查看最新库存</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsPartOut">
import { CircleClose, Search, Filter, RefreshLeft, Delete, Download, ArrowDown } from '@element-plus/icons-vue'
import { listPartOut, addPartOut, delPartOut, getPartOut, updatePartOut } from '@/api/dms/partout'
import { fetchAllPages, downloadCsv } from '@/utils/csvExport'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import SparePartPicker from '@/components/SparepartPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0","v3","v2","v1","v0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_partout_index')
const { dms_partout_type, wms_unit } = proxy.useDict('dms_partout_type', 'wms_unit')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const formDisabled = ref(false)
const viewOpen = ref(false)
const viewLoading = ref(false)
const viewForm = ref({})
const showStatusHelp = ref(false)

const defaultColumns = {
  documentCode: { label: '单据号', visible: true },
  partCode: { label: '备件编号', visible: true },
  partName: { label: '备件名称', visible: true },
  unit: { label: '单位', visible: true },
  targetType: { label: '出库类型', visible: true },
  supplierOrDept: { label: '领用部门', visible: true },
  receiver: { label: '领用人', visible: true },
  quantity: { label: '出库数量', visible: true },
  operateDate: { label: '出库日期', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_partout_columns')
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
  if (queryParams.value.documentCode) count++
  if (queryParams.value.targetType) count++
  if (queryParams.value.supplierOrDept) count++
  if (queryParams.value.partCode) count++
  if (queryParams.value.partName) count++
  if (queryParams.value.receiver) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})
function unitLabel(val) { const item = wms_unit.value.find(d => d.value == val); return item ? item.label : '-' }
function targetTypeLabel(val) { const item = dms_partout_type.value.find(d => d.value == val); return item ? item.label : '-' }

/** 获取当天日期 YYYY-MM-DD */
function today() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 校验出库数量不能大于库存数量 */
function validateQuantity(rule, value, callback) {
  if (value == null || value <= 0) {
    callback(new Error('出库数量必须大于0'))
  } else if (form.value.currentStock != null && value > form.value.currentStock) {
    callback(new Error('出库数量不能大于库存数量（' + form.value.currentStock + '）'))
  } else {
    callback()
  }
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, documentCode: undefined, targetType: undefined, supplierOrDept: undefined, partCode: undefined, partName: undefined, receiver: undefined, params: {} },
  rules: {
    targetType: [{ required: true, message: '出库类型不能为空', trigger: 'change' }],
    stockId: [{ required: true, message: '备件不能为空', trigger: 'change' }],
    quantity: [{ required: true, validator: validateQuantity, trigger: 'blur' }],
    operateDate: [{ required: true, message: '出库日期不能为空', trigger: 'change' }],
    receiver: [{ required: true, message: '领用人不能为空', trigger: 'change' }],
    deptId: [{ required: true, message: '领用部门不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listPartOut(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.params = proxy.addDateRange(queryParams.value.params, dateRange.value, 'OperateDate'); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.documentCode = undefined; queryParams.value.targetType = undefined; queryParams.value.supplierOrDept = undefined; queryParams.value.partCode = undefined; queryParams.value.partName = undefined; queryParams.value.receiver = undefined; dateRange.value = []; queryParams.value.params = {}; proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); multiple.value = !selection.length }
function handleAdd() {
  reset(); open.value = true
  formDisabled.value = false
  title.value = '新增出库'
}
function handleView(row) {
  viewLoading.value = true
  viewOpen.value = true
  getPartOut(row.recordId).then(res => {
    viewForm.value = res.data || {}
    // 按开发规范：分组内字段全为空时自动收缩
    collapsedCards.v0 = !viewForm.value.remark
    viewLoading.value = false
  }).catch(() => { viewLoading.value = false })
}
function handleUpdate(row) {
  reset(); formDisabled.value = false
  getPartOut(row.recordId).then(res => { form.value = res.data; open.value = true; title.value = '修改出库记录' })
}
function reset() {
  form.value = {
    documentCode: undefined, targetType: undefined, partId: undefined, stockId: undefined, partCode: undefined, partName: undefined,
    unit: undefined, currentStock: undefined, warehouseName: undefined, quantity: 1, operateDate: today(),
    receiver: undefined, supplierOrDept: undefined, deptId: undefined, operatorName: undefined, remark: undefined
  }
  proxy.resetForm('partoutRef')
}
/** 打开领用人选择弹窗 */
function openReceiverPicker() {
  proxy.$refs.userPickerRef.open(undefined)
}
/** 领用人选择确认回调 */
function onReceiverPickerConfirm(user) {
  form.value.receiver = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.supplierOrDept = user.deptName
  }
}
/** 清除领用人 */
function clearReceiver() {
  form.value.receiver = undefined
}
/** 打开部门选择弹窗 */
function openDeptPicker() {
  proxy.$refs.deptPickerRef.open(form.value.deptId)
}
/** 部门选择确认回调 */
function onDeptPickerConfirm(dept) {
  form.value.deptId = dept.deptId
  form.value.supplierOrDept = dept.deptName
}
/** 清除部门 */
function clearDept() {
  form.value.deptId = undefined
  form.value.supplierOrDept = undefined
}
/** 打开备件选择弹窗 */
function openSparePartPicker() {
  if (form.value.recordId) return
  proxy.$refs.sparePartPickerRef.open(form.value.stockId)
}
/** 备件选择确认回调 */
function onSparePartPickerConfirm(part) {
  form.value.stockId = part.stockId
  form.value.partId = part.partId
  form.value.partCode = part.partCode
  form.value.partName = part.partName
  form.value.unit = part.unit || ''
  form.value.currentStock = part.currentStock || 0
  form.value.warehouseName = part.warehouseName || '备件库'
  // 重置出库数量，确保不超过库存
  if (form.value.quantity > form.value.currentStock) {
    form.value.quantity = form.value.currentStock
  }
}
/** 清除备件选择 */
function clearSparePart() {
  form.value.stockId = undefined
  form.value.partId = undefined
  form.value.partCode = undefined
  form.value.partName = undefined
  form.value.unit = undefined
  form.value.currentStock = undefined
  form.value.warehouseName = undefined
}
function submitForm() {
  proxy.$refs['partoutRef'].validate(valid => {
    if (valid) {
      if (form.value.recordId != undefined) { updatePartOut(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addPartOut(form.value).then(() => { proxy.$modal.msgSuccess('出库成功'); open.value = false; getList() }) }
    }
  })
}
function cancel() { open.value = false; reset() }
/** 导出出库记录（与列表口径一致：含筛选条件、日期范围，导出全部页数据） */
async function handleExport() {
  const rows = await fetchAllPages(listPartOut, queryParams.value)
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['单据号', '备件编号', '备件名称', '单位', '出库类型', '领用部门', '领用人', '出库数量', '出库日期']
  downloadCsv(`partout_${new Date().getTime()}`, headers, rows.map(i => [
    i.documentCode, i.partCode, i.partName, unitLabel(i.unit), targetTypeLabel(i.targetType),
    i.supplierOrDept || '', i.receiver || '', i.quantity != null ? i.quantity : '', i.operateDate || ''
  ]))
}
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除选中的出库记录？').then(() => delPartOut(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

onActivated(() => {
  getList()
})

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.dms-partout-page {
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
.dms-partout-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-partout-page .filter-card { padding:14px 20px 16px; }
.dms-partout-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-partout-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-partout-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-partout-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-partout-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-partout-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-partout-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-partout-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-partout-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-partout-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-partout-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-partout-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-partout-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-partout-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-partout-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-partout-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.dms-partout-page .field .control :deep(.el-select) { width:100%; }
.dms-partout-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-partout-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-partout-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.dms-partout-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-partout-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-partout-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-partout-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-partout-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-partout-page .btn-soft .el-icon { font-size:14px; }
.dms-partout-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-partout-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-partout-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-partout-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-partout-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-partout-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-partout-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-partout-page .table-wrap { overflow-x:auto; }
.dms-partout-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-partout-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-partout-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-partout-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-partout-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-partout-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-partout-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-partout-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-partout-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-partout-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-partout-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.dms-partout-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-partout-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-partout-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-partout-page .badge.amber .dot { background:var(--amber-500); }
.dms-partout-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-partout-page .badge.violet .dot { background:var(--brand-500); }
.dms-partout-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-partout-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-partout-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-partout-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-partout-page .pagination-container :deep(.el-pagination .btn-prev), .dms-partout-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-partout-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-partout-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-partout-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
.dms-partout-page .clear-icon { cursor:pointer; color:#c0c4cc; font-size:14px; }
.dms-partout-page .clear-icon:hover { color:#909399; }
.dms-partout-page :deep(.el-input.is-disabled .el-input__inner) { cursor:pointer; }
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行 */
.dms-partout-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-partout-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-partout-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-partout-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-partout-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .dms-partout-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-partout-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-partout-page .toolbar { flex-wrap:wrap; gap:10px; } }

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
