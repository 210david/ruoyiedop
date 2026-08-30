<template>
  <div class="app-container pms-inquiry-page">
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
          <label>询价单号</label>
          <div class="control">
            <el-input v-model="queryParams.inquiryNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>询价标题</label>
          <div class="control">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in pms_inquiry_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>询价方式</label>
          <div class="control is-select">
            <el-select v-model="queryParams.inquiryType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in pms_inquiry_type" :key="dict.value" :label="dict.label" :value="dict.value" />
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
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span>
            <span>全部</span>
            <span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span>
            <span>{{ s.label }}</span>
            <span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:inquiry:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:inquiry:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:inquiry:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:inquiry:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_inquiry_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="inquiryList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="询价单号" align="center" prop="inquiryNo" key="inquiryNo" :width="colWidth('inquiryNo', 180)" resizable sortable="custom" v-if="columns.inquiryNo.visible" />
          <el-table-column label="询价标题" align="center" prop="title" key="title" :width="colWidth('title', 240)" resizable show-overflow-tooltip v-if="columns.title.visible" />
          <el-table-column label="物料名称" align="center" prop="materialName" key="materialName" :width="colWidth('materialName', 160)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="状态" align="center" prop="status" key="status" :width="colWidth('status', 120)" resizable sortable="custom" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="询价方式" align="center" prop="inquiryType" key="inquiryType" :width="colWidth('inquiryType', 120)" resizable v-if="columns.inquiryType.visible"><template #default="scope"><span class="badge violet">{{ inquiryTypeLabel(scope.row.inquiryType) }}</span></template></el-table-column>
          <el-table-column label="报价截止" align="center" prop="deadline" key="deadline" :width="colWidth('deadline', 180)" resizable v-if="columns.deadline.visible"><template #default="scope"><span>{{ parseTime(scope.row.deadline) }}</span></template></el-table-column>
          <el-table-column label="最高限价" prop="maxPrice" key="maxPrice" :width="colWidth('maxPrice', 130)" resizable align="right" v-if="columns.maxPrice.visible"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.maxPrice) }}</span></template></el-table-column>
          <el-table-column label="报价数" align="center" key="quotationCount" :width="colWidth('quotationCount', 90)" resizable v-if="columns.quotationCount.visible"><template #default="scope"><el-tag v-if="scope.row.quotationCount > 0" type="success" size="small">{{ scope.row.quotationCount }}</el-tag><span v-else>-</span></template></el-table-column>
          <el-table-column label="定标金额" prop="awardAmount" key="awardAmount" :width="colWidth('awardAmount', 130)" resizable align="right" v-if="columns.awardAmount.visible"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.awardAmount) }}</span></template></el-table-column>
          <el-table-column label="定标供应商" align="center" prop="awardSupplierName" key="awardSupplierName" :width="colWidth('awardSupplierName', 160)" resizable show-overflow-tooltip v-if="columns.awardSupplierName.visible" />
          <el-table-column label="创建时间" align="center" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['pms:inquiry:edit']">修改</el-button>
                <el-button link type="success" icon="Promotion" @click="handlePublish(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['pms:inquiry:edit']">发布</el-button>
                <el-button link type="warning" icon="CircleClose" @click="handleClose(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['pms:inquiry:edit']">截止</el-button>
                <el-button link type="info" icon="EditPen" @click="handleAddQuotation(scope.row)" v-if="scope.row.status === '1' || scope.row.status === '2' || scope.row.status === '3'" v-hasPermi="['pms:inquiry:edit']">录入报价</el-button>
                <el-button link type="primary" icon="Scale" @click="handleAward(scope.row)" v-if="scope.row.status === '1' || scope.row.status === '2' || scope.row.status === '3'" v-hasPermi="['pms:inquiry:edit']">{{ scope.row.status === '3' ? '继续定标' : '比价定标' }}</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></div><span class="rd-detail-header-title">{{ title }}</span><div class="rd-detail-header-sub" v-if="form.inquiryNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ form.inquiryNo }}</span></div></div></template>
      <el-form ref="inquiryRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="询价单号" prop="inquiryNo"><el-input v-model="form.inquiryNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="询价标题" prop="title"><el-input v-model="form.title" placeholder="请输入询价标题" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="询价方式" prop="inquiryType"><el-select v-model="form.inquiryType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in pms_inquiry_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="采购类型" prop="purchaseType"><el-select v-model="form.purchaseType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in pms_purchase_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="报价截止" prop="deadline"><el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止日期" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="预计金额"><el-input :model-value="formatMoney(computedEstimateAmount)" disabled placeholder="自动计算（数量×最高限价）" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="24"><el-form-item label="询价说明" prop="description"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>询价物料</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
                <template #title>
                  <span v-if="form.purchaseType === '1'">采购类型为「服务」，仅可选择服务类物料。若无可选项，请先在物料管理中新增「服务」类型的物料（如IT运维、保洁服务等）。</span>
                  <span v-else-if="form.purchaseType === '2'">采购类型为「工程」，仅可选择工程类物料。若无可选项，请先在物料管理中新增「工程」类型的物料（如装修工程、安装工程等）。</span>
                  <span v-else>采购类型为「物资」，可选择原材料、半成品、成品、辅料等物资类物料。如需采购服务或工程，请先切换采购类型。</span>
                </template>
              </el-alert>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="物料" prop="materialId" :rules="[{ required: true, message: '请选择物料', trigger: 'change' }]">
                    <el-select v-model="form.materialId" filterable placeholder="请选择物料" style="width: 100%" @change="onMaterialChange">
                      <el-option v-for="m in filteredMaterialOptions" :key="m.materialId" :label="m.materialCode + ' - ' + m.materialName" :value="m.materialId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="规格型号"><el-input v-model="form.specification" disabled placeholder="选择物料后自动带出" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="单位"><el-select v-model="form.unit" disabled placeholder="选择物料后自动带出" style="width: 100%"><el-option v-for="dict in wms_unit" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="采购数量" prop="quantity" :rules="[{ required: true, message: '请输入采购数量', trigger: 'blur' }]"><el-input-number v-model="form.quantity" :min="0" :precision="2" :controls="false" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="最高限价" prop="maxPrice"><el-input-number v-model="form.maxPrice" :min="0" :precision="2" :controls="false" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0"><el-row :gutter="20"><el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></el-col></el-row></div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></div><span class="rd-detail-header-title">询价单详情</span><div class="rd-detail-header-sub" v-if="viewData.inquiryNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.inquiryNo }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v1" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">询价单号</span><div class="rd-value">{{ viewData.inquiryNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">询价标题</span><div class="rd-value">{{ viewData.title || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_inquiry_status" :value="viewData.status" /></div></div>
            <div class="rd-item"><span class="rd-label">询价方式</span><div class="rd-value"><dict-tag :options="pms_inquiry_type" :value="viewData.inquiryType" /></div></div>
            <div class="rd-item"><span class="rd-label">采购类型</span><div class="rd-value"><dict-tag :options="pms_purchase_type" :value="viewData.purchaseType" /></div></div>
            <div class="rd-item"><span class="rd-label">报价截止</span><div class="rd-value">{{ parseTime(viewData.deadline) || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">预计金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.estimateAmount) }}</div></div>
            <div class="rd-item"><span class="rd-label">询价人</span><div class="rd-value">{{ viewData.inquirerName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">询价说明</span><div class="rd-value">{{ viewData.description || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>询价物料</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v2" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specification || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
            <div class="rd-item"><span class="rd-label">采购数量</span><div class="rd-value">{{ formatAmount(viewData.quantity) }}</div></div>
            <div class="rd-item"><span class="rd-label">最高限价</span><div class="rd-value rd-amount">{{ formatMoney(viewData.maxPrice) }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.status === '4'">
          <div class="rd-card-header" @click="toggleCard('v3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg></span>定标信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v3" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">定标供应商</span><div class="rd-value">{{ viewData.awardSupplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">定标金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.awardAmount) }}</div></div>
          </div></div>
        </section>
        <!-- 比价汇总（查看模式） -->
        <section class="rd-card" v-if="viewData.quotationList && viewData.quotationList.length > 0">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H1v12h8V11zM23 11h-8v12h8V11zM9 1H1v6h8V1zM23 1h-8v6h8V1z"/></svg></span>比价汇总</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="viewData.quotationList" border size="small" :row-class-name="viewQuotationRowClassName">
              <el-table-column label="供应商" prop="supplierName" min-width="160" align="center"><template #default="scope"><span>{{ scope.row.supplierName }}</span><el-tag v-if="isAwardedQuotation(scope.row)" type="success" size="small" effect="dark" style="margin-left:6px">已中标</el-tag></template></el-table-column>
              <el-table-column label="报价单价" width="120" align="right"><template #default="scope"><span class="rd-amount" :class="{ 'rd-price-best': isLowestPrice(scope.row, viewData.quotationList) }">{{ formatMoney(getQuotationPrice(scope.row)) }}</span><el-tag v-if="isLowestPrice(scope.row, viewData.quotationList)" type="success" size="small" style="margin-left:4px">最低</el-tag></template></el-table-column>
              <el-table-column label="报价总额" prop="totalAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template></el-table-column>
              <el-table-column label="税率(%)" prop="taxRate" width="80" align="center" />
              <el-table-column label="交货期(天)" prop="deliveryDays" width="90" align="center" />
              <el-table-column label="付款方式" prop="paymentTerms" width="110" align="center"><template #default="scope"><dict-tag :options="wms_payment_method" :value="scope.row.paymentTerms" /></template></el-table-column>
              <el-table-column label="有效期" prop="validDate" width="110" align="center" />
              <el-table-column label="联系人" prop="contactName" width="90" align="center" />
              <el-table-column label="备注" prop="remark" min-width="100" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v0" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '暂无备注' }}</div></div></div></div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 录入报价对话框 -->
    <el-dialog v-model="quotationOpen" width="800px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H1v12h8V11zM23 11h-8v12h8V11zM9 1H1v6h8V1zM23 1h-8v6h8V1z"/></svg></div><span class="rd-detail-header-title">录入供应商报价</span><div class="rd-detail-header-sub" v-if="quotationForm.inquiryNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ quotationForm.inquiryNo }}</span></div></div></template>
      <el-form ref="quotationRef" :model="quotationForm" :rules="quotationRules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>询价物料信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">询价单号</span><div class="rd-value">{{ quotationForm.inquiryNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ quotationForm.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ quotationForm.specification || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="quotationForm.unit" /></div></div>
              <div class="rd-item"><span class="rd-label">采购数量</span><div class="rd-value">{{ formatAmount(quotationForm.quantity) }}</div></div>
              <div class="rd-item"><span class="rd-label">最高限价</span><div class="rd-value rd-amount">{{ formatMoney(quotationForm.maxPrice) }}</div></div>
            </div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('q1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>供应商信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.q1 }" type="button" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.q1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-select v-model="quotationForm.supplierId" filterable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName + (quotedSupplierIds.includes(s.supplierId) ? '（已报价）' : '')" :value="s.supplierId" :disabled="quotedSupplierIds.includes(s.supplierId)" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="联系人"><el-input v-model="quotationForm.contactName" placeholder="请输入联系人" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="联系电话"><el-input v-model="quotationForm.contactPhone" placeholder="请输入联系电话" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('q2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>报价信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.q2 }" type="button" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.q2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="报价单价" prop="price" :rules="[{ required: true, message: '请输入报价单价', trigger: 'blur' }]"><el-input-number v-model="quotationForm.price" :min="0" :precision="2" :controls="false" style="width: 100%" @change="calcQuotationTotal" /></el-form-item></el-col><el-col :span="12"><el-form-item label="报价金额"><el-input-number v-model="quotationForm.totalAmount" :min="0" :precision="2" :controls="false" style="width: 100%" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="税率(%)" prop="taxRate" :rules="[{ required: true, message: '请输入税率', trigger: 'blur' }]"><el-input-number v-model="quotationForm.taxRate" :min="0" :precision="2" :controls="false" style="width: 100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('q3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H1v12h8V11zM23 11h-8v12h8V11zM9 1H1v6h8V1zM23 1h-8v6h8V1z"/></svg></span>商务条款</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.q3 }" type="button" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.q3">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="交货期(天)" prop="deliveryDays" :rules="[{ required: true, message: '请输入交货期', trigger: 'blur' }]"><el-input-number v-model="quotationForm.deliveryDays" :min="0" :controls="false" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="有效期" prop="validDate" :rules="[{ required: true, message: '请选择有效期', trigger: 'change' }]"><el-date-picker v-model="quotationForm.validDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="付款方式" prop="paymentTerms" :rules="[{ required: true, message: '请选择付款方式', trigger: 'change' }]"><el-select v-model="quotationForm.paymentTerms" placeholder="请选择付款方式" style="width: 100%"><el-option v-for="d in wms_payment_method" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('q4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.q4 }" type="button" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.q4"><el-row :gutter="20"><el-col :span="24"><el-form-item label="备注"><el-input v-model="quotationForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" /></el-form-item></el-col></el-row></div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitQuotation">确认录入</el-button><el-button @click="quotationOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="询比价业务操作说明" width="700px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">询价中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">已截止</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已比价</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已定标</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审核</el-tag>
            <el-tag size="small" type="info">提交审核后</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">询价中</el-tag>
            <el-tag size="small" type="info">审核通过后发布</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建询价单后的初始状态，可编辑、删除、发布或提交审核
          </el-descriptions-item>
          <el-descriptions-item label="待审核">
            提交审核后等待审核通过，审核通过后可发布进入询价中
          </el-descriptions-item>
          <el-descriptions-item label="询价中">
            询价单已发布，供应商可报价。可手动截止或等待报价截止到期
          </el-descriptions-item>
          <el-descriptions-item label="已截止">
            报价已截止，不再接受新报价。可进行比价定标
          </el-descriptions-item>
          <el-descriptions-item label="已比价">
            进入比价环节，可查看各供应商报价对比，选择中标供应商定标
          </el-descriptions-item>
          <el-descriptions-item label="已定标">
            已确定中标供应商和定标金额，询价流程结束，不可修改
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">
                最低价自动标记
              </div>
              <div class="highlight-card-body">
                比价汇总表中系统<strong>自动标记最低报价</strong>（绿色高亮），辅助采购人员快速识别最优报价，但不强制选择最低价
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">
                供应商唯一报价
              </div>
              <div class="highlight-card-body">
                同一询价单中，<strong>每个供应商只能报一次价</strong>，已报价的供应商在下拉列表中标记「已报价」并禁用选择
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">
                预计金额自动计算
              </div>
              <div class="highlight-card-body">
                预计金额 = <strong>采购数量 × 最高限价</strong>，选择物料和填写数量、限价后自动计算，无需手动输入
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">
                定标后不可修改
              </div>
              <div class="highlight-card-body">
                定标操作确认后询价单进入「已定标」状态，<strong>不可再修改或重新定标</strong>，请仔细确认中标供应商和定标金额
              </div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务触发说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建询价：</strong>填写询价标题、询价方式（公开/邀请）、采购类型（物资/服务/工程），选择物料并填写采购数量和最高限价后保存
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>发布询价：</strong>点击「发布」按钮，询价单进入「询价中」状态，等待供应商报价。报价截止时间到期后自动进入「已截止」
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>录入报价：</strong>在询价中/已截止/已比价状态下，点击「录入报价」按钮，填写供应商报价信息（单价、税率、交货期、付款方式等）。录入后系统自动将询价单转为「已比价」状态
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>比价定标：</strong>点击「比价定标」按钮，查看各供应商报价对比表，<strong>系统自动标记最低价</strong>。选择中标供应商并确认定标金额后提交，询价单进入「已定标」状态
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>定标完成：</strong>询价流程结束，定标信息不可修改。可在「查看」中查看定标供应商和定标金额
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 比价定标对话框 -->
    <el-dialog v-model="awardOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg></div><span class="rd-detail-header-title">比价定标</span><div class="rd-detail-header-sub" v-if="awardForm.inquiryNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ awardForm.inquiryNo }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>询价信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">询价单号</span><div class="rd-value">{{ awardForm.inquiryNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">询价标题</span><div class="rd-value">{{ awardForm.title || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ awardForm.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ awardForm.specification || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">采购数量</span><div class="rd-value">{{ formatAmount(awardForm.quantity) }}</div></div>
            <div class="rd-item"><span class="rd-label">最高限价</span><div class="rd-value rd-amount">{{ formatMoney(awardForm.maxPrice) }}</div></div>
            <div class="rd-item"><span class="rd-label">报价截止</span><div class="rd-value">{{ parseTime(awardForm.deadline) || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">报价数</span><div class="rd-value">{{ awardForm.quotationList?.length || 0 }} 家</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="!awardForm.quotationList || awardForm.quotationList.length === 0">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>暂无报价</div></div>
          <div class="rd-card-body" style="display:block"><el-alert type="warning" :closable="false" show-icon><template #title>当前询价单暂无供应商报价，请先通过"录入报价"功能添加供应商报价后再进行比价定标。</template></el-alert></div>
        </section>
        <template v-if="awardForm.quotationList && awardForm.quotationList.length > 0">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H1v12h8V11zM23 11h-8v12h8V11zM9 1H1v6h8V1zM23 1h-8v6h8V1z"/></svg></span>比价汇总</div></div>
            <div class="rd-card-body" style="display:block">
              <el-table :data="awardForm.quotationList" border size="small" @row-click="onAwardRowClick">
                <el-table-column label="" width="50" align="center"><template #default="scope"><el-radio v-model="awardForm.supplierId" :value="scope.row.supplierId" @change="onAwardSupplierChange(scope.row)"><span></span></el-radio></template></el-table-column>
                <el-table-column label="供应商" prop="supplierName" min-width="160" align="center" />
                <el-table-column label="报价单价" width="120" align="right"><template #default="scope"><span class="rd-amount" :class="{ 'rd-price-best': isLowestPrice(scope.row, awardForm.quotationList) }">{{ formatMoney(getQuotationPrice(scope.row)) }}</span><el-tag v-if="isLowestPrice(scope.row, awardForm.quotationList)" type="success" size="small" style="margin-left:4px">最低</el-tag></template></el-table-column>
                <el-table-column label="报价总额" prop="totalAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template></el-table-column>
                <el-table-column label="税率(%)" prop="taxRate" width="80" align="center" />
                <el-table-column label="交货期(天)" prop="deliveryDays" width="90" align="center" />
                <el-table-column label="付款方式" prop="paymentTerms" width="110" align="center"><template #default="scope"><dict-tag :options="wms_payment_method" :value="scope.row.paymentTerms" /></template></el-table-column>
                <el-table-column label="有效期" prop="validDate" width="110" align="center" />
                <el-table-column label="联系人" prop="contactName" width="90" align="center" />
              </el-table>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>定标信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-form ref="awardRef" :model="awardForm" :rules="awardRules" label-width="120px">
                <el-row :gutter="20"><el-col :span="12"><el-form-item label="中标供应商" prop="supplierId"><el-input :model-value="awardForm.supplierName || '请选择'" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="定标金额" prop="amount"><el-input-number v-model="awardForm.amount" :min="0" :precision="2" :controls="false" style="width: 100%" /></el-form-item></el-col></el-row>
              </el-form>
            </div>
          </section>
        </template>
      </div>
      <template #footer><el-button type="primary" @click="submitAward" :disabled="!awardForm.supplierId">确认定标</el-button><el-button @click="awardOpen = false">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsInquiry">
import { listInquiry, getInquiry, delInquiry, addInquiry, updateInquiry, publishInquiry, closeInquiry, compareInquiry, awardInquiry, addQuotation } from "@/api/pms/inquiry";
import { listMaterial } from '@/api/wms/material'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { pms_inquiry_status, pms_inquiry_type, pms_purchase_type, wms_unit, wms_payment_method } = proxy.useDict('pms_inquiry_status', 'pms_inquiry_type', 'pms_purchase_type', 'wms_unit', 'wms_payment_method');
const { collapsedCards, toggleCard } = useDetailCard(["c1","c2","c0","c3","q1","q2","q3","q4","v1","v2","v3","v0"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_inquiry_index')

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  inquiryNo: { label: '询价单号', visible: true },
  title: { label: '询价标题', visible: true },
  materialName: { label: '物料名称', visible: true },
  status: { label: '状态', visible: true },
  inquiryType: { label: '询价方式', visible: true },
  deadline: { label: '报价截止', visible: true },
  maxPrice: { label: '最高限价', visible: true },
  quotationCount: { label: '报价数', visible: true },
  awardAmount: { label: '定标金额', visible: true },
  awardSupplierName: { label: '定标供应商', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_inquiry_columns')
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
const activeStatusTab = ref('all')
const statusTabList = computed(() => pms_inquiry_status.value.map(d => ({ label: d.label, value: d.value })))
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listInquiry({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    pms_inquiry_status.value.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-approved', '4': 'tab-void' }
  return map[value] || ''
}
function badgeClass(status) {
  const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'green', '4': 'gray' }
  return map[status] || 'gray'
}
function statusLabel(status) {
  const item = pms_inquiry_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function inquiryTypeLabel(type) {
  const item = pms_inquiry_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}
function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  queryParams.value.status = tab === 'all' ? undefined : tab
  handleQuery()
}
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.inquiryNo) count++
  if (queryParams.value.title) count++
  if (queryParams.value.status) count++
  if (queryParams.value.inquiryType) count++
  return count
})

const inquiryList = ref([]);
const showStatusHelp = ref(false);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const awardOpen = ref(false);
const quotationOpen = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const materialOptions = ref([]);
const supplierOptions = ref([]);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: { inquiryId: null, inquiryNo: null, title: null, status: '0', inquiryType: '0', purchaseType: '0', deadline: null, estimateAmount: 0, description: null, inquirerId: null, inquirerName: null, deptId: null, deptName: null, materialId: null, materialCode: '', materialName: '', specification: '', unit: '', quantity: 1, maxPrice: 0, remark: null },
  queryParams: { pageNum: 1, pageSize: 10, inquiryNo: null, title: null, status: null, inquiryType: null, params: {} },
  rules: { title: [{ required: true, message: "询价标题不能为空", trigger: "blur" }], inquiryType: [{ required: true, message: "询价方式不能为空", trigger: "change" }], deadline: [{ required: true, message: "报价截止日期不能为空", trigger: "change" }] }
});

const awardForm = reactive({ inquiryId: null, inquiryNo: null, title: null, deadline: null, estimateAmount: 0, materialName: '', specification: '', quantity: 0, maxPrice: 0, quotationList: [], supplierId: null, supplierName: null, amount: 0 });
const awardRules = { supplierId: [{ required: true, message: "请选择中标供应商", trigger: "change" }], amount: [{ required: true, message: "请输入定标金额", trigger: "blur" }] };
const quotationForm = reactive({ inquiryId: null, inquiryNo: null, inquiryTitle: null, materialId: null, materialCode: '', materialName: '', specification: '', unit: '', quantity: 0, maxPrice: 0, supplierId: null, supplierName: null, contactName: null, contactPhone: null, price: null, totalAmount: 0, taxRate: 13, deliveryDays: null, paymentTerms: null, validDate: null, remark: null });
const quotationRules = { supplierId: [{ required: true, message: "请选择供应商", trigger: "change" }] };
const quotedSupplierIds = ref([]);

const { queryParams, form, rules } = toRefs(data);

/** 自动计算预计金额 = 采购数量 × 最高限价 */
const computedEstimateAmount = computed(() => {
  const qty = Number(form.value.quantity) || 0;
  const price = Number(form.value.maxPrice) || 0;
  return (qty * price).toFixed(2);
});

/** 根据采购类型过滤物料选项 */
const filteredMaterialOptions = computed(() => {
  const purchaseType = form.value.purchaseType;
  if (purchaseType === '1') {
    // 服务：只显示服务类物料（materialType='4'）
    return materialOptions.value.filter(m => m.materialType === '4');
  } else if (purchaseType === '2') {
    // 工程：只显示工程类物料（materialType='5'）
    return materialOptions.value.filter(m => m.materialType === '5');
  } else {
    // 物资：显示原材料/半成品/成品/辅料（0/1/2/3）
    return materialOptions.value.filter(m => m.materialType !== '4' && m.materialType !== '5');
  }
});

/** 从报价明细中提取单价 */
function getQuotationPrice(quotation) {
  if (quotation.detailList && quotation.detailList.length > 0) {
    return quotation.detailList[0].price || 0;
  }
  return 0;
}

/** 判断是否最低单价 */
function isLowestPrice(row, list) {
  if (!list || list.length === 0) return false;
  const prices = list.map(q => getQuotationPrice(q)).filter(p => p > 0);
  if (prices.length === 0) return false;
  const min = Math.min(...prices);
  return getQuotationPrice(row) === min && min > 0;
}

/** 判断报价行是否为中标供应商 */
function isAwardedQuotation(row) {
  return viewData.value.awardSupplierId != null && row.supplierId === viewData.value.awardSupplierId;
}

/** 查看详情-比价汇总表格行样式：中标行高亮 */
function viewQuotationRowClassName({ row }) {
  if (isAwardedQuotation(row)) {
    return 'rd-award-row';
  }
  return '';
}

function getList() {
  loading.value = true;
  listInquiry(queryParams.value).then(response => {
    inquiryList.value = response.rows.map(item => {
      // 从明细中提取物料信息到主表显示
      if (item.detailList && item.detailList.length > 0) {
        const d = item.detailList[0];
        item.materialName = d.materialName;
        item.materialCode = d.materialCode;
        item.maxPrice = d.maxPrice;
        item.quantity = d.quantity;
      }
      return item;
    });
    total.value = response.total;
    loading.value = false;
    loadStatusCounts();
    applySavedWidths();
  });
}

function cancel() { open.value = false; reset(); }

function reset() {
  form.value = { inquiryId: null, inquiryNo: null, title: null, status: '0', inquiryType: '0', purchaseType: '0', deadline: null, estimateAmount: 0, description: null, inquirerId: null, inquirerName: null, deptId: null, deptName: null, materialId: null, materialCode: '', materialName: '', specification: '', unit: '', quantity: 1, maxPrice: 0, remark: null };
  proxy.resetForm("inquiryRef");
}

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() {
  queryParams.value.inquiryNo = null;
  queryParams.value.title = null;
  queryParams.value.status = null;
  queryParams.value.inquiryType = null;
  queryParams.value.params = {};
  activeStatusTab.value = 'all';
  if (tableRef.value) tableRef.value.clearSort();
  handleQuery();
}
function handleSelectionChange(selection) { ids.value = selection.map(item => item.inquiryId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList(); }

function handleExport() { proxy.download('pms/inquiry/export', { ...queryParams.value }, `inquiry_${new Date().getTime()}.xlsx`); }

function handleAdd() { reset(); open.value = true; title.value = "新增询价单"; }

function handleUpdate(row) {
  reset();
  const inquiryId = row.inquiryId || ids.value[0];
  getInquiry(inquiryId).then(response => {
    const d = response.data;
    // 从明细中提取单物料信息到表单
    if (d.detailList && d.detailList.length > 0) {
      const detail = d.detailList[0];
      d.materialId = detail.materialId;
      d.materialCode = detail.materialCode;
      d.materialName = detail.materialName;
      d.specification = detail.specification;
      d.unit = detail.unit;
      d.quantity = detail.quantity;
      d.maxPrice = detail.maxPrice;
    }
    form.value = d;
    open.value = true; title.value = "修改询价单";
  });
}

function handleView(row) {
  getInquiry(row.inquiryId).then(response => {
    const d = response.data;
    if (d.detailList && d.detailList.length > 0) {
      const detail = d.detailList[0];
      d.materialId = detail.materialId;
      d.materialCode = detail.materialCode;
      d.materialName = detail.materialName;
      d.specification = detail.specification;
      d.unit = detail.unit;
      d.quantity = detail.quantity;
      d.maxPrice = detail.maxPrice;
    }
    if (!d.quotationList) d.quotationList = [];
    viewData.value = d;
    viewOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs["inquiryRef"].validate(valid => {
    if (valid) {
      // 将单物料信息组装为 detailList（后端兼容）
      form.value.detailList = [{
        materialId: form.value.materialId,
        materialCode: form.value.materialCode,
        materialName: form.value.materialName,
        specification: form.value.specification,
        unit: form.value.unit,
        quantity: form.value.quantity,
        maxPrice: form.value.maxPrice
      }];
      if (form.value.inquiryId != null) {
        updateInquiry(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addInquiry(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}

function handleDelete(row) {
  const inquiryIds = row.inquiryId ? [row.inquiryId] : ids.value;
  proxy.$modal.confirm('是否确认删除编号为"' + inquiryIds + '"的数据项？').then(() => delInquiry(inquiryIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

function handlePublish(row) {
  proxy.$modal.confirm('是否确认发布该询价单？').then(() => publishInquiry(row.inquiryId)).then(() => { getList(); proxy.$modal.msgSuccess("发布成功"); }).catch(() => {});
}

function handleClose(row) {
  proxy.$modal.confirm('是否确认截止该询价单？').then(() => closeInquiry(row.inquiryId)).then(() => { getList(); proxy.$modal.msgSuccess("截止成功"); }).catch(() => {});
}

async function handleAddQuotation(row) {
  const response = await getInquiry(row.inquiryId);
  const inquiry = response.data;
  quotationForm.inquiryId = row.inquiryId;
  quotationForm.inquiryNo = row.inquiryNo;
  quotationForm.inquiryTitle = row.title;
  quotationForm.supplierId = null;
  quotationForm.supplierName = null;
  quotationForm.contactName = null;
  quotationForm.contactPhone = null;
  quotationForm.price = null;
  quotationForm.totalAmount = 0;
  quotationForm.taxRate = 13;
  quotationForm.deliveryDays = null;
  quotationForm.paymentTerms = null;
  quotationForm.validDate = null;
  quotationForm.remark = null;
  // 记录已报价的供应商ID，用于前端重复校验
  quotedSupplierIds.value = (inquiry.quotationList || []).map(q => q.supplierId);
  // 从询价明细提取单物料信息
  if (inquiry.detailList && inquiry.detailList.length > 0) {
    const d = inquiry.detailList[0];
    quotationForm.materialId = d.materialId;
    quotationForm.materialCode = d.materialCode;
    quotationForm.materialName = d.materialName;
    quotationForm.specification = d.specification;
    quotationForm.unit = d.unit;
    quotationForm.quantity = d.quantity;
    quotationForm.maxPrice = d.maxPrice;
  }
  await loadSupplierOptions();
  quotationOpen.value = true;
}

function onSupplierChange(val) {
  if (val && quotedSupplierIds.value.includes(val)) {
    proxy.$modal.msgError('该供应商已对此询价单报过价，不能重复报价');
    quotationForm.supplierId = null;
    quotationForm.supplierName = null;
    return;
  }
  const s = supplierOptions.value.find(s => s.supplierId === val);
      if (s) {
        quotationForm.supplierName = s.supplierName;
        if (s.contactName) quotationForm.contactName = s.contactName;
      }
}

function calcQuotationTotal() {
  quotationForm.totalAmount = (Number(quotationForm.quantity) || 0) * (Number(quotationForm.price) || 0);
}

function submitQuotation() {
  if (quotedSupplierIds.value.includes(quotationForm.supplierId)) {
    proxy.$modal.msgError('该供应商已对此询价单报过价，不能重复报价');
    return;
  }
  proxy.$refs["quotationRef"].validate(valid => {
    if (valid) {
      calcQuotationTotal();
      const submitData = {
        inquiryId: quotationForm.inquiryId,
        supplierId: quotationForm.supplierId,
        supplierName: quotationForm.supplierName,
        contactName: quotationForm.contactName,
        contactPhone: quotationForm.contactPhone,
        totalAmount: quotationForm.totalAmount,
        taxRate: quotationForm.taxRate,
        deliveryDays: quotationForm.deliveryDays,
        paymentTerms: quotationForm.paymentTerms,
        validDate: quotationForm.validDate,
        remark: quotationForm.remark,
        detailList: [{
          materialId: quotationForm.materialId,
          materialCode: quotationForm.materialCode,
          materialName: quotationForm.materialName,
          specification: quotationForm.specification,
          unit: quotationForm.unit,
          quantity: quotationForm.quantity,
          price: quotationForm.price,
          amount: quotationForm.totalAmount
        }]
      };
      addQuotation(submitData).then(() => { proxy.$modal.msgSuccess("报价录入成功，询价单已自动转为「比价中」状态"); quotationOpen.value = false; getList(); }).catch(() => {});
    }
  });
}

async function handleAward(row) {
  const response = await getInquiry(row.inquiryId);
  const inquiry = response.data;
  // 状态为询价中(1)或已截止(2)时，先调比价接口变更为已比价(3)
  if (row.status === '1' || row.status === '2') {
    try {
      await compareInquiry(row.inquiryId);
    } catch (e) {
      return;
    }
  }
  awardForm.inquiryId = row.inquiryId;
  awardForm.inquiryNo = row.inquiryNo;
  awardForm.title = row.title;
  awardForm.deadline = row.deadline;
  awardForm.estimateAmount = row.estimateAmount;
  // 从明细提取物料信息
  if (inquiry.detailList && inquiry.detailList.length > 0) {
    const d = inquiry.detailList[0];
    awardForm.materialName = d.materialName;
    awardForm.specification = d.specification;
    awardForm.quantity = d.quantity;
    awardForm.maxPrice = d.maxPrice;
  }
  awardForm.quotationList = inquiry.quotationList || [];
  awardForm.supplierId = null;
  awardForm.supplierName = null;
  awardForm.amount = 0;
  awardOpen.value = true;
  // 比价后刷新列表
  if (row.status === '1' || row.status === '2') {
    getList();
  }
}

function onAwardRowClick(row) {
  awardForm.supplierId = row.supplierId;
  onAwardSupplierChange(row);
}

function onAwardSupplierChange(item) {
  awardForm.supplierName = item.supplierName;
  awardForm.amount = item.totalAmount;
}

function submitAward() {
  proxy.$refs["awardRef"].validate(valid => {
    if (valid) {
      proxy.$modal.confirm('是否确认对该询价单进行定标？定标后将不可修改。').then(() =>
        awardInquiry(awardForm.inquiryId, awardForm.supplierId, awardForm.supplierName, awardForm.amount)
      ).then(() => { awardOpen.value = false; getList(); proxy.$modal.msgSuccess("定标成功"); }).catch(() => {});
    }
  });
}

function onMaterialChange(val) {
  const matched = materialOptions.value.find(m => m.materialId === val);
  if (matched) {
    form.value.materialCode = matched.materialCode;
    form.value.materialName = matched.materialName;
    form.value.specification = matched.specModel;
    form.value.unit = matched.unit;
  }
}

/** 采购类型变化时清空已选物料 */
watch(() => form.value.purchaseType, () => {
  form.value.materialId = null;
  form.value.materialCode = '';
  form.value.materialName = '';
  form.value.specification = '';
  form.value.unit = '';
});

function loadMaterialOptions() {
  listMaterial({ pageNum: 1, pageSize: 999, status: '0' }).then(res => { materialOptions.value = res.rows || []; });
}

function loadSupplierOptions() {
  return listSupplier({ pageNum: 1, pageSize: 999, status: '0' }).then(res => { supplierOptions.value = res.rows || []; });
}

loadMaterialOptions();
getList();
onActivated(() => { getList(); })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-inquiry-page {
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
.pms-inquiry-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pms-inquiry-page .filter-card { padding:14px 20px 16px; }
.pms-inquiry-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-inquiry-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-inquiry-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-inquiry-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-inquiry-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-inquiry-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-inquiry-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-inquiry-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-inquiry-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-inquiry-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-inquiry-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pms-inquiry-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-inquiry-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-inquiry-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-inquiry-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-inquiry-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-inquiry-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-inquiry-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-inquiry-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-inquiry-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.pms-inquiry-page .field .control :deep(.el-select) { width:100%; }
.pms-inquiry-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-inquiry-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-inquiry-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-inquiry-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-inquiry-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-inquiry-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-inquiry-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-inquiry-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-inquiry-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-inquiry-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-inquiry-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-inquiry-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-inquiry-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-inquiry-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.pms-inquiry-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.pms-inquiry-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.pms-inquiry-page .status-tab.tab-audit .dot { background:var(--blue-500); }
.pms-inquiry-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); }
.pms-inquiry-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.pms-inquiry-page .status-tab.tab-approved .dot, .pms-inquiry-page .status-tab.tab-done .dot { background:var(--green-500); }
.pms-inquiry-page .status-tab.tab-approved .count, .pms-inquiry-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.pms-inquiry-page .status-tab.is-active.tab-approved .count, .pms-inquiry-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.pms-inquiry-page .status-tab.tab-void .dot { background:var(--ink-400); }
.pms-inquiry-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; border-radius:999px; border:1px solid var(--ink-200); background:#fff; font-size:13px; color:var(--ink-500); cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.pms-inquiry-page .tip-pill:hover { border-color:var(--brand-200); color:var(--brand-700); background:var(--brand-50); }
.pms-inquiry-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-inquiry-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-inquiry-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-inquiry-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pms-inquiry-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-inquiry-page .btn-soft .el-icon { font-size:14px; }
.pms-inquiry-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-inquiry-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-inquiry-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-inquiry-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-inquiry-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-inquiry-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.pms-inquiry-page .table-wrap { overflow-x:auto; }
.pms-inquiry-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-inquiry-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-inquiry-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-inquiry-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-inquiry-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-inquiry-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-inquiry-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-inquiry-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-inquiry-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-inquiry-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-inquiry-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
/* ===== Badges ===== */
.pms-inquiry-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-inquiry-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-inquiry-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-inquiry-page .badge.amber .dot { background:var(--amber-500); }
.pms-inquiry-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pms-inquiry-page .badge.blue .dot { background:var(--blue-500); }
.pms-inquiry-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-inquiry-page .badge.green .dot { background:var(--green-500); }
.pms-inquiry-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pms-inquiry-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-inquiry-page .badge.gray .dot { background:var(--ink-400); }
.pms-inquiry-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-inquiry-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-inquiry-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-inquiry-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-inquiry-page .pagination-container :deep(.el-pagination .btn-prev), .pms-inquiry-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-inquiry-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-inquiry-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-inquiry-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .pms-inquiry-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-inquiry-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-inquiry-page .toolbar { flex-wrap:wrap; gap:10px; } }
.rd-price-best {
  color: #67c23a;
  font-weight: bold;
}
:deep(.rd-award-row) {
  background-color: #f0f9eb;
}
:deep(.rd-award-row:hover > td) {
  background-color: #e8f5e0 !important;
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
.status-flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}
.flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.flow-arrow {
  color: #909399;
  font-size: 16px;
}
.highlight-card {
  border-radius: 8px;
  padding: 16px;
  border: 1px solid;
}
.highlight-success {
  background-color: #f0f9ff;
  border-color: #b3e19d;
}
.highlight-danger {
  background-color: #fef0f0;
  border-color: #fbc4c4;
}
.highlight-primary {
  background-color: #ecf5ff;
  border-color: #a0cfff;
}
.highlight-warning {
  background-color: #fdf6ec;
  border-color: #f5dab1;
}
.highlight-card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}
.highlight-success .highlight-card-title {
  color: #67c23a;
}
.highlight-danger .highlight-card-title {
  color: #f56c6c;
}
.highlight-primary .highlight-card-title {
  color: #409eff;
}
.highlight-warning .highlight-card-title {
  color: #e6a23c;
}
.highlight-card-body {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
:deep(.el-timeline-item__node) {
  background-color: transparent;
  border: 2px solid;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
