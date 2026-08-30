<template>
  <div class="app-container wms-material-page">
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
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <label>物料类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.materialType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_material_type" :key="d.value" :label="d.label" :value="d.value" />
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
          <label>批次管理</label>
          <div class="control is-select">
            <el-select v-model="queryParams.isBatchManage" placeholder="全部" clearable @change="handleQuery">
              <el-option label="是" value="1" />
              <el-option label="否" value="0" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>效期管理</label>
          <div class="control is-select">
            <el-select v-model="queryParams.isExpiryManage" placeholder="全部" clearable @change="handleQuery">
              <el-option label="是" value="1" />
              <el-option label="否" value="0" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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
      <!-- Type Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTypeTab === 'all' }" @click="handleTypeTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ typeCounts.all || 0 }}</span>
          </button>
          <button v-for="t in wms_material_type" :key="t.value" class="status-tab" :class="[typeTabClass(t.value), { 'is-active': activeTypeTab === t.value }]" @click="handleTypeTabClick(t.value)">
            <span class="dot"></span><span>{{ t.label }}</span><span class="count">{{ typeCounts[t.value] || 0 }}</span>
          </button>
        </div>
      </div>
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:material:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['wms:material:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:material:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['wms:material:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['wms:material:import']">
            <el-icon><Upload /></el-icon> 导入
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_material_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="materialList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 180)" resizable sortable="custom" v-if="columns.materialCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.materialCode }}</span></template>
          </el-table-column>
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 240)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="物料类型" prop="materialType" key="materialType" :width="colWidth('materialType', 120)" resizable align="center" v-if="columns.materialType.visible">
            <template #default="scope">
              <span class="badge violet">{{ materialTypeLabel(scope.row.materialType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 240)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope">
              <span class="badge blue">{{ unitLabel(scope.row.unit) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="批次管理" prop="isBatchManage" key="isBatchManage" :width="colWidth('isBatchManage', 90)" resizable align="center" v-if="columns.isBatchManage.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isBatchManage === '1' ? 'green' : 'gray'">{{ scope.row.isBatchManage === '1' ? '是' : '否' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="效期管理" prop="isExpiryManage" key="isExpiryManage" :width="colWidth('isExpiryManage', 90)" resizable align="center" v-if="columns.isExpiryManage.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isExpiryManage === '1' ? 'green' : 'gray'">{{ scope.row.isExpiryManage === '1' ? '是' : '否' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="保质期(天)" prop="shelfLifeDays" key="shelfLifeDays" :width="colWidth('shelfLifeDays', 110)" resizable align="center" v-if="columns.shelfLifeDays.visible">
            <template #default="scope">{{ scope.row.shelfLifeDays != null ? scope.row.shelfLifeDays : '-' }}</template>
          </el-table-column>
          <el-table-column label="安全库存下限" prop="safetyStockMin" key="safetyStockMin" :width="colWidth('safetyStockMin', 130)" resizable align="center" class-name="col-num" v-if="columns.safetyStockMin.visible" />
          <el-table-column label="安全库存上限" prop="safetyStockMax" key="safetyStockMax" :width="colWidth('safetyStockMax', 130)" resizable align="center" class-name="col-num" v-if="columns.safetyStockMax.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:material:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:material:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="物料导入"
      action="/wms/material/importData"
      template-action="/wms/material/importTemplate"
      template-file-name="material_template"
      update-support-label="是否更新已经存在的物料数据"
      :tips="importTips"
      :update-key-options="updateKeyOptions"
      default-update-key="materialName"
      @success="getList"
    />

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="materialRef" :model="form" :rules="rules" label-width="100px" class="material-form">
                <!-- 分组一：基本信息 -->
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-alert v-if="isServiceOrEngineering" type="info" :closable="false" show-icon style="margin-bottom: 16px">
          <template #title>
            <span v-if="form.materialType === '4'">当前为「服务」类型物料，适用于IT运维、保洁服务、咨询服务等非物资类采购。「效期管理」和「库存控制」不适用于此类物料。</span>
            <span v-else-if="form.materialType === '5'">当前为「工程」类型物料，适用于装修工程、安装工程、施工项目等。「效期管理」和「库存控制」不适用于此类物料。</span>
          </template>
        </el-alert>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input v-model="form.materialCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="materialName">
              <el-input v-model="form.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料类型" prop="materialType">
              <el-select v-model="form.materialType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in wms_material_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in wms_unit" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="isServiceOrEngineering ? '规格描述' : '规格型号'" prop="specModel">
              <el-input v-model="form.specModel" :placeholder="isServiceOrEngineering ? '请输入规格描述' : '请输入规格型号'" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组二：效期管理 -->
                  </div>
        </section>
        <section class="rd-card" v-show="!isServiceOrEngineering">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>效期管理</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="效期管理" prop="isExpiryManage">
              <el-radio-group v-model="form.isExpiryManage">
                <el-radio value="0">否</el-radio>
                <el-radio value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次管理" prop="isBatchManage">
              <el-radio-group v-model="form.isBatchManage">
                <el-radio value="0">否</el-radio>
                <el-radio value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="保质期天数" prop="shelfLifeDays">
              <el-input-number v-model="form.shelfLifeDays" :min="0" :precision="0" controls-position="right" placeholder="天" style="width: 50%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="临期预警" prop="expiryAlertDays">
              <el-input-number v-model="form.expiryAlertDays" :min="0" :precision="0" controls-position="right" placeholder="天" style="width: 50%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组三：库存控制 -->
                  </div>
        </section>
        <section class="rd-card" v-show="!isServiceOrEngineering">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="16 18 23 18 23 11"/></svg></span>库存控制</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="安全库存下限" prop="safetyStockMin">
              <el-input-number v-model="form.safetyStockMin" :precision="2" :min="0" style="width: 50%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全库存上限" prop="safetyStockMax">
              <el-input-number v-model="form.safetyStockMax" :precision="2" :min="0" style="width: 50%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 分组四：其他信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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

    <!-- 查看对话框 -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">物料详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.materialCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编码：{{ viewData.materialCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料类型</span><div class="rd-value"><dict-tag :options="wms_material_type" :value="viewData.materialType" /></div></div>
              <div class="rd-item"><span class="rd-label">计量单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 效期管理 -->
        <section class="rd-card" v-show="!isViewServiceOrEngineering">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>效期管理</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">效期管理</span><div class="rd-value"><el-tag :type="viewData.isExpiryManage === '1' ? 'success' : 'info'">{{ viewData.isExpiryManage === '1' ? '是' : '否' }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">批次管理</span><div class="rd-value"><el-tag :type="viewData.isBatchManage === '1' ? 'success' : 'info'">{{ viewData.isBatchManage === '1' ? '是' : '否' }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">保质期天数</span><div class="rd-value">{{ viewData.shelfLifeDays != null ? viewData.shelfLifeDays + ' 天' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">临期预警</span><div class="rd-value">{{ viewData.expiryAlertDays != null ? viewData.expiryAlertDays + ' 天' : '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 库存控制 -->
        <section class="rd-card" v-show="!isViewServiceOrEngineering">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="16 18 23 18 23 11"/></svg></span>库存控制</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">安全库存下限</span><div class="rd-value">{{ viewData.safetyStockMin != null ? viewData.safetyStockMin : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">安全库存上限</span><div class="rd-value">{{ viewData.safetyStockMax != null ? viewData.safetyStockMax : '-' }}</div></div>
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
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><el-tag :type="viewData.status === '0' ? 'success' : 'danger'">{{ viewData.status === '0' ? '正常' : '停用' }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsMaterial">
import { listMaterial, getMaterial, addMaterial, updateMaterial, delMaterial } from '@/api/wms/material'
import { fetchAllPages, downloadCsv } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import { ArrowDown, Plus, Edit, Delete, Download, Upload, RefreshLeft, Search, Filter } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0","v3","v2","v1","v0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_material_index')
const { wms_material_type, wms_unit } = proxy.useDict('wms_material_type', 'wms_unit')

const materialList = ref([])
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
const activeTypeTab = ref('all')
const typeCounts = ref({ all: 0 })

// 列显隐配置
const defaultColumns = {
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  materialType: { label: '物料类型', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  isBatchManage: { label: '批次管理', visible: true },
  isExpiryManage: { label: '效期管理', visible: true },
  shelfLifeDays: { label: '保质期(天)', visible: true },
  safetyStockMin: { label: '安全库存下限', visible: true },
  safetyStockMax: { label: '安全库存上限', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('wms_material_columns')
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

// ===== 导入功能配置 =====
const importTips = [
  '物料名称为必填字段，不能为空',
  '物料编码由系统自动生成，无需填写',
  '物料类型填：原材料/半成品/成品/辅料（或留空）',
  '计量单位填：个/件/箱/kg/吨/米（或留空）',
  '如勾选「更新已存在数据」，需选择匹配字段：按名称或编码匹配',
  '导入结果将显示每条数据的处理情况，失败数据可修正后重新导入'
]
const updateKeyOptions = [
  { value: 'materialName', label: '物料名称' },
  { value: 'materialCode', label: '物料编码' }
]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    materialCode: undefined,
    materialName: undefined,
    materialType: undefined,
    isBatchManage: undefined,
    isExpiryManage: undefined,
    status: undefined,
    params: {}
  },
rules: {
materialName: [{ required: true, message: '物料名称不能为空', trigger: 'blur' }],
    unit: [{ required: true, message: '计量单位不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.materialType) count++
  if (queryParams.value.isBatchManage) count++
  if (queryParams.value.isExpiryManage) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

/** 是否为服务或工程类型 */
const isServiceOrEngineering = computed(() => {
  return form.value.materialType === '4' || form.value.materialType === '5'
})

/** 查看详情-是否为服务或工程类型 */
const isViewServiceOrEngineering = computed(() => {
  return viewData.value.materialType === '4' || viewData.value.materialType === '5'
})

function getList() {
  loading.value = true
  listMaterial(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    materialList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params = queryParams.value.params || {}; queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params = queryParams.value.params || {}; queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function materialTypeLabel(type) { const item = wms_material_type.value.find(d => d.value == type); return item ? item.label : '-' }
function unitLabel(unit) { const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : '-' }

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  loadTypeCounts()
}

function resetQuery() {
queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.materialType = undefined; queryParams.value.isBatchManage = undefined; queryParams.value.isExpiryManage = undefined; queryParams.value.status = undefined; queryParams.value.params = {}; dateRange.value = []; activeTypeTab.value = 'all'; handleQuery()
}
function handleTypeTabClick(type) { activeTypeTab.value = type; queryParams.value.materialType = type === 'all' ? undefined : type; handleQuery() }
function typeTabClass(value) { const map = { '0': 'tab-raw', '1': 'tab-semi', '2': 'tab-product', '3': 'tab-aux', '4': 'tab-service', '5': 'tab-engineering' }; return map[value] || '' }
function loadTypeCounts() {
  listMaterial({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total };
    (wms_material_type.value || []).forEach(d => { counts[d.value] = 0 });
    (res.rows || []).forEach(r => { if (counts[r.materialType] !== undefined) counts[r.materialType]++ });
    typeCounts.value = counts
  }).catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.materialId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    materialId: undefined,
    materialCode: undefined,
    materialName: undefined,
    materialType: '0',
    specModel: undefined,
    unit: undefined,
    shelfLifeDays: undefined,
    expiryAlertDays: 30,
    isExpiryManage: '0',
    safetyStockMin: 0,
    safetyStockMax: 0,
    isBatchManage: '0',
    status: '0',
    remark: undefined
  }
  proxy.resetForm('materialRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加物料'
}

function handleUpdate(row) {
  reset()
  const materialId = row.materialId || ids.value[0]
  getMaterial(materialId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改物料'
  })
}

function handleView(row) {
  getMaterial(row.materialId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['materialRef'].validate(valid => {
    if (valid) {
      if (form.value.materialId != undefined) {
        updateMaterial(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addMaterial(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const materialIds = row.materialId || ids.value
  proxy.$modal.confirm('是否确认删除物料编号为"' + materialIds + '"的数据项？').then(function () {
    return delMaterial(materialIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 导出物料（与列表口径一致：含筛选条件，导出全部页数据） */
async function handleExport() {
  const rows = await fetchAllPages(listMaterial, proxy.addDateRange(queryParams.value, dateRange.value))
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['物料编码', '物料名称', '物料类型', '规格型号', '单位', '批次管理', '效期管理', '保质期(天)', '安全库存下限', '安全库存上限', '状态', '创建时间']
  downloadCsv(`material_${new Date().getTime()}`, headers, rows.map(i => [
    i.materialCode || '', i.materialName || '', materialTypeLabel(i.materialType), i.specModel || '', unitLabel(i.unit),
    i.isBatchManage === '1' ? '是' : '否', i.isExpiryManage === '1' ? '是' : '否',
    i.shelfLifeDays != null ? i.shelfLifeDays : '',
    i.safetyStockMin != null ? i.safetyStockMin : '', i.safetyStockMax != null ? i.safetyStockMax : '',
    i.status === '0' ? '正常' : '停用', i.createTime || ''
  ]))
}

function handleImport() {
  proxy.$refs['importRef'].open()
}

function cancel() {
  open.value = false
  reset()
}

getList()
loadTypeCounts()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.wms-material-page {
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

/* ===== Surface Card ===== */
.wms-material-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.wms-material-page .filter-card { padding:14px 20px 16px; }
.wms-material-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.wms-material-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.wms-material-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.wms-material-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.wms-material-page .filter-card .adv-link:hover { color:var(--brand-600); }
.wms-material-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.wms-material-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.wms-material-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.wms-material-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.wms-material-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.wms-material-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.wms-material-page .field { display:flex; flex-direction:column; gap:6px; }
.wms-material-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.wms-material-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.wms-material-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.wms-material-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.wms-material-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.wms-material-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.wms-material-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.wms-material-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.wms-material-page .field .control :deep(.el-select) { width:100%; }
.wms-material-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.wms-material-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.wms-material-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.wms-material-page .field .control :deep(.el-date-editor) { width:100%; }
.wms-material-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.wms-material-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.wms-material-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Type Tabs ===== */
.wms-material-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.wms-material-page .tabs-track { display:flex; align-items:center; gap:4px; flex-wrap:wrap; }
.wms-material-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.wms-material-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.wms-material-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.wms-material-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.wms-material-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.wms-material-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.wms-material-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 物料类型颜色 */
.wms-material-page .status-tab.tab-raw .dot { background:var(--amber-500); }
.wms-material-page .status-tab.tab-raw .count { background:var(--amber-50); color:var(--amber-700); }
.wms-material-page .status-tab.is-active.tab-raw .count { background:var(--amber-500); color:#fff; }
.wms-material-page .status-tab.tab-semi .dot { background:var(--blue-500); }
.wms-material-page .status-tab.tab-semi .count { background:var(--blue-50); color:var(--blue-700); }
.wms-material-page .status-tab.is-active.tab-semi .count { background:var(--blue-500); color:#fff; }
.wms-material-page .status-tab.tab-product .dot { background:var(--green-500); }
.wms-material-page .status-tab.tab-product .count { background:var(--green-50); color:var(--green-700); }
.wms-material-page .status-tab.is-active.tab-product .count { background:var(--green-500); color:#fff; }
.wms-material-page .status-tab.tab-aux .dot { background:#8b5cf6; }
.wms-material-page .status-tab.tab-aux .count { background:var(--violet-50); color:#7c3aed; }
.wms-material-page .status-tab.is-active.tab-aux .count { background:#8b5cf6; color:#fff; }
.wms-material-page .status-tab.tab-service .dot { background:#06b6d4; }
.wms-material-page .status-tab.tab-service .count { background:#ecfeff; color:#0e7490; }
.wms-material-page .status-tab.is-active.tab-service .count { background:#06b6d4; color:#fff; }
.wms-material-page .status-tab.tab-engineering .dot { background:var(--ink-400); }
.wms-material-page .status-tab.tab-engineering .count { background:var(--ink-100); color:var(--ink-500); }
.wms-material-page .status-tab.is-active.tab-engineering .count { background:var(--ink-400); color:#fff; }

/* ===== Toolbar ===== */
.wms-material-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.wms-material-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.wms-material-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.wms-material-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.wms-material-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.wms-material-page .btn-soft .el-icon { font-size:14px; }
.wms-material-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.wms-material-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.wms-material-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.wms-material-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.wms-material-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.wms-material-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }

/* ===== Table ===== */
.wms-material-page .table-wrap { overflow-x:auto; }
.wms-material-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.wms-material-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.wms-material-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.wms-material-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.wms-material-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.wms-material-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.wms-material-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.wms-material-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.wms-material-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.wms-material-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.wms-material-page .app-table :deep(.col-num) { text-align:center; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }

/* ===== Badges ===== */
.wms-material-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.wms-material-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.wms-material-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.wms-material-page .badge.amber .dot { background:var(--amber-500); }
.wms-material-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.wms-material-page .badge.blue .dot { background:var(--blue-500); }
.wms-material-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.wms-material-page .badge.green .dot { background:var(--green-500); }
.wms-material-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.wms-material-page .badge.red .dot { background:var(--red-500); }
.wms-material-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.wms-material-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.wms-material-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.wms-material-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.wms-material-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.wms-material-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.wms-material-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; }
.wms-material-page .pagination-container :deep(.el-pagination .btn-prev), .wms-material-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.wms-material-page .pagination-container :deep(.el-pagination .btn-prev:hover), .wms-material-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.wms-material-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
.wms-material-page :deep(.col-action) { padding: 6px 4px !important; }
.wms-material-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.wms-material-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.wms-material-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.wms-material-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .wms-material-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .wms-material-page .filter-card .filter-bar { grid-template-columns:1fr; } .wms-material-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
