<template>
  <div class="app-container mms-bom-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div><a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a></div>
      <div class="filter-bar">
        <div class="field"><label>BOM编号</label><div class="control"><el-input v-model="queryParams.bomNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>BOM名称</label><div class="control"><el-input v-model="queryParams.bomName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产品编码</label><div class="control"><el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产品名称</label><div class="control"><el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>BOM类型</label><div class="control is-select"><el-select v-model="queryParams.bomType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_bom_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_bom_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[{ 'is-active': activeStatusTab === s.value }, statusTabClass(s.value)]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><QuestionFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:bom:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:bom:edit']">修改</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:bom:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['mms:bom:import']">导入</el-button><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:bom:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_bom_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="bomList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column label="BOM编号" prop="bomNo" key="bomNo" :width="colWidth('bomNo', 140)" resizable v-if="columns.bomNo.visible" /><el-table-column label="BOM名称" prop="bomName" key="bomName" :width="colWidth('bomName', 200)" resizable show-overflow-tooltip v-if="columns.bomName.visible" /><el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" /><el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" /><el-table-column label="BOM类型" prop="bomType" key="bomType" :width="colWidth('bomType', 100)" resizable align="center" v-if="columns.bomType.visible"><template #default="scope"><span class="badge blue"><span class="dot"></span>{{ bomTypeLabel(scope.row.bomType) }}</span></template></el-table-column><el-table-column label="版本" prop="version" key="version" :width="colWidth('version', 80)" resizable align="center" v-if="columns.version.visible" /><el-table-column label="基准数量" prop="baseQty" key="baseQty" :width="colWidth('baseQty', 100)" resizable align="center" v-if="columns.baseQty.visible"><template #default="scope"><span>{{ scope.row.baseQty }} {{ unitLabel(scope.row.baseUnit) }}</span></template></el-table-column><el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column><el-table-column label="操作" width="240" align="center" fixed="right"><template #default="scope"><el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button><el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.status !== '1'" v-hasPermi="['mms:bom:edit']">修改</el-button><el-button link type="success" icon="Promotion" @click="handlePublish(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['mms:bom:edit']">发布</el-button><el-button link type="warning" icon="CircleClose" @click="handleDisable(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['mms:bom:edit']">停用</el-button><el-button link type="success" icon="CircleCheck" @click="handleEnable(scope.row)" v-if="scope.row.status === '2'" v-hasPermi="['mms:bom:edit']">启用</el-button><el-button link type="warning" icon="CopyDocument" @click="handleCopy(scope.row)" v-hasPermi="['mms:bom:add']">复制</el-button><el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status !== '1'" v-hasPermi="['mms:bom:remove']">删除</el-button></template></el-table-column></el-table></div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.bomNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ form.bomNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="bomRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- BOM标识 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>BOM标识</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="BOM编号" prop="bomNo"><el-input v-model="form.bomNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="16"><el-form-item label="BOM名称" prop="bomName"><el-input v-model="form.bomName" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="BOM类型" prop="bomType"><el-select v-model="form.bomType" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_bom_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="基准数量" prop="baseQty"><el-input-number v-model="form.baseQty" :min="0.01" :precision="2" :step="1" placeholder="请输入" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="版本" prop="version"><el-input v-model="form.version" placeholder="请输入版本号，如1.0" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 产品信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 3a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2z"/><path d="M3 7a2 2 0 0 1 2-2h5a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg></span>产品信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="产品编号" prop="productCode"><el-input v-model="form.productCode" readonly placeholder="请选择产品" style="width: 100%" @click="openProductPicker"><template v-if="form.productCode" #append><el-button icon="CircleClose" @click.stop="clearProduct" /></template><template v-else #append><el-button icon="Search" @click="openProductPicker" /></template></el-input></el-form-item></el-col>
                <el-col :span="10"><el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" placeholder="请选择产品" readonly /></el-form-item></el-col>
                <el-col :span="6"><el-form-item label="基准单位" prop="baseUnit"><el-input v-model="form.baseUnitText" readonly placeholder="选择产品后带出" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- BOM明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>BOM明细（子件物料）</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <div class="detail-toolbar"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddDetail">添加物料行</el-button><span class="detail-tip">选择物料后，若该物料为半成品且有已发布BOM，系统自动关联引用BOM，支持多层级展开</span></div>
              <el-table :data="form.detailList" border size="small" class="detail-table" max-height="400">
                <el-table-column label="序号" width="55" align="center"><template #default="scope">{{ scope.$index + 1 }}</template></el-table-column>
                <el-table-column label="物料编码" width="140"><template #default="scope"><el-input v-model="scope.row.materialCode" readonly size="small" placeholder="选择物料" @click="openMaterialPicker(scope.$index)"><template v-if="scope.row.materialCode" #append><el-button icon="CircleClose" size="small" @click.stop="clearMaterial(scope.$index)" /></template><template v-else #append><el-button icon="Search" size="small" @click="openMaterialPicker(scope.$index)" /></template></el-input></template></el-table-column>
                <el-table-column label="物料名称" width="150" show-overflow-tooltip><template #default="scope"><span>{{ scope.row.materialName }}</span></template></el-table-column>
                <el-table-column label="规格型号" width="110" show-overflow-tooltip><template #default="scope"><span>{{ scope.row.specModel }}</span></template></el-table-column>
                <el-table-column label="单位" width="60" align="center"><template #default="scope"><span>{{ unitLabel(scope.row.unit) }}</span></template></el-table-column>
                <el-table-column label="单件用量" width="110"><template #default="scope"><el-input-number v-model="scope.row.usageQty" :min="0" :precision="2" :step="0.1" size="small" controls-position="right" style="width: 100%" /></template></el-table-column>
                <el-table-column label="损耗率(%)" width="90"><template #default="scope"><el-input-number v-model="scope.row.lossRate" :min="0" :max="100" :precision="2" :step="1" size="small" controls-position="right" style="width: 100%" /></template></el-table-column>
                <el-table-column label="关键料" width="60" align="center"><template #default="scope"><el-checkbox v-model="scope.row.isKeyMaterial" true-value="1" false-value="0" /></template></el-table-column>
                <el-table-column label="供应方式" width="110"><template #default="scope"><el-select v-model="scope.row.supplyType" size="small" style="width: 100%"><el-option v-for="d in mms_supply_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></template></el-table-column>
                <el-table-column label="虚拟件" width="60" align="center"><template #default="scope"><el-checkbox v-model="scope.row.isPhantom" true-value="1" false-value="0" /></template></el-table-column>
                <el-table-column label="引用BOM" width="130"><template #default="scope"><span class="text-muted">{{ scope.row.bomRefNo || '—' }}</span></template></el-table-column>
                <el-table-column label="操作" width="60" align="center" fixed="right"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="handleDeleteDetail(scope.$index)" /></template></el-table-column>
              </el-table>
              <div v-if="!form.detailList || form.detailList.length === 0" class="empty-detail"><el-empty description="暂无明细，请点击「添加物料行」" :image-size="60" /></div>
            </div>
          </section>
          <!-- 状态与生效 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>状态与生效</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" placeholder="请选择" style="width: 100%" disabled><el-option v-for="d in mms_bom_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="生效日期" prop="effectiveDate"><el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 附加信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>附加信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" /></el-form-item></el-col>
              </el-row>
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

    <!-- BOM详情查看对话框 -->
    <el-dialog v-model="viewOpen" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">BOM详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.bomNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ viewData.bomNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page" v-loading="viewLoading">
        <!-- BOM标识 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>BOM标识</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">BOM编号</span><div class="rd-value">{{ viewData.bomNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">BOM名称</span><div class="rd-value">{{ viewData.bomName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">BOM类型</span><div class="rd-value"><span class="badge blue"><span class="dot"></span>{{ bomTypeLabel(viewData.bomType) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">版本</span><div class="rd-value">{{ viewData.version || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">基准数量</span><div class="rd-value">{{ viewData.baseQty != null ? viewData.baseQty : '—' }} {{ unitLabel(viewData.baseUnit) }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span></div></div>
            </div>
          </div>
        </section>
        <!-- 产品信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 3a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2z"/><path d="M3 7a2 2 0 0 1 2-2h5a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg></span>产品信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">基准单位</span><div class="rd-value">{{ unitLabel(viewData.baseUnit) }}</div></div>
            </div>
          </div>
        </section>
        <!-- BOM明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>BOM明细（子件物料）</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block">
            <el-table :data="viewData.detailList" border size="small" class="app-table" max-height="400">
              <el-table-column label="序号" width="55" align="center"><template #default="scope">{{ scope.$index + 1 }}</template></el-table-column>
              <el-table-column label="物料编码" prop="materialCode" width="140" show-overflow-tooltip />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="70" align="center"><template #default="scope">{{ unitLabel(scope.row.unit) }}</template></el-table-column>
              <el-table-column label="单件用量" prop="usageQty" width="110" align="center" />
              <el-table-column label="损耗率(%)" prop="lossRate" width="100" align="center" />
              <el-table-column label="关键料" width="70" align="center"><template #default="scope"><el-tag v-if="scope.row.isKeyMaterial === '1'" type="danger" size="small">是</el-tag><span v-else>否</span></template></el-table-column>
              <el-table-column label="供应方式" width="100" align="center"><template #default="scope">{{ supplyTypeLabel(scope.row.supplyType) }}</template></el-table-column>
              <el-table-column label="虚拟件" width="70" align="center"><template #default="scope"><el-tag v-if="scope.row.isPhantom === '1'" type="warning" size="small">是</el-tag><span v-else>否</span></template></el-table-column>
              <el-table-column label="引用BOM" prop="bomRefNo" width="120" show-overflow-tooltip />
              <template #empty><el-empty description="暂无明细数据" :image-size="60" /></template>
            </el-table>
          </div>
        </section>
        <!-- 状态与生效 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>状态与生效</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ viewData.effectiveDate || '—' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 附加信息 -->
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>附加信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '—' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" icon="Connection" @click="handleViewTreeFromDetail" v-if="viewData.bomId">多层级展开</el-button>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- BOM多层级展开对话框 -->
    <el-dialog v-model="treeOpen" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/><path d="M6 15l6 6 6-6"/></svg></div>
          <span class="rd-detail-header-title">BOM多层级展开 - {{ treeBomNo }}</span>
        </div>
      </template>
      <div v-if="!treeLoading && treeData.length === 0" style="text-align:center;padding:60px 20px;color:var(--ink-400)">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
        <p style="margin:0 0 8px;font-size:15px;font-weight:600;color:var(--ink-500)">暂无多层级展开数据</p>
        <p style="margin:0;font-size:13px;line-height:1.6;color:var(--ink-400)">BOM明细中未设置"引用BOM"的子件，无法展开多层级结构。<br/>在BOM明细中为半成品子件设置引用BOM后，可查看完整的树形结构。</p>
      </div>
      <el-table v-loading="treeLoading" v-else :data="treeData" border size="small" class="app-table" row-key="_rowKey" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all max-height="500">
        <el-table-column label="物料编码" prop="materialCode" width="150" show-overflow-tooltip />
        <el-table-column label="物料名称" prop="materialName" min-width="180" show-overflow-tooltip />
        <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
        <el-table-column label="单位" prop="unit" width="70" align="center"><template #default="scope">{{ unitLabel(scope.row.unit) }}</template></el-table-column>
        <el-table-column label="单件用量" prop="usageQty" width="100" align="center" />
        <el-table-column label="损耗率(%)" prop="lossRate" width="90" align="center" />
        <el-table-column label="关键料" width="70" align="center"><template #default="scope"><el-tag v-if="scope.row.isKeyMaterial === '1'" type="danger" size="small">是</el-tag><span v-else>否</span></template></el-table-column>
        <el-table-column label="供应方式" width="100" align="center"><template #default="scope">{{ supplyTypeLabel(scope.row.supplyType) }}</template></el-table-column>
        <el-table-column label="引用BOM" prop="bomRefNo" width="130" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="treeOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="BOM导入"
      action="/mms/bom/importData"
      template-action="/mms/bom/importTemplate"
      template-file-name="bom_template"
      update-support-label="是否更新已存在的BOM数据"
      :tips="importTips"
      @success="getList"
    />

    <!-- 物料选择器 -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
    <!-- 产品选择器（仅半成品和成品） -->
    <material-picker ref="productPickerRef" title="选择产品" :material-types="['1','2']" @confirm="onProductPickerConfirm" />

    <!-- 业务操作说明 -->
    <el-dialog v-model="showStatusHelp" title="BOM管理业务操作说明" width="820px" append-to-body draggable class="rd-dialog">
      <div class="status-help-content">
        <!-- 一、BOM释义 -->
        <h4>一、BOM释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是BOM？</div>
          <div class="highlight-card-body">
            <strong>BOM（Bill of Materials，物料清单）</strong>是生产管控中定义产品组成结构和用量关系的核心主数据。BOM列出了生产一个产品所需的所有原材料、半成品、零件及其数量关系，是物料需求计划（MRP）和工单发料的基础依据。<br/><br/>
            支持多层级BOM结构（半成品嵌套）、基准数量（批量BOM）、供应方式（直接领料/倒冲/车间库存）、版本发布管理，确保生产过程中使用正确的物料清单，支持成本核算和物料追溯。BOM的准确性直接影响MRP运算结果和工单发料的正确性，是生产管控系统中最关键的基础主数据之一。
          </div>
        </div>
        <div class="highlight-card highlight-success" style="margin-top: 12px;">
          <div class="highlight-card-title">BOM的分类</div>
          <div class="highlight-card-body">
            <p>• <strong>标准BOM：</strong>用于标准产品的生产管理，是最常用的BOM类型，定义产品的标准组成结构</p>
            <p>• <strong>定制BOM：</strong>用于定制化产品的生产管理，根据客户需求调整组成结构和用量</p>
            <p>• <strong>维修BOM：</strong>用于设备维修和售后服务，定义维修所需备品备件清单</p>
          </div>
        </div>

        <!-- 二、BOM状态流转图 -->
        <h4>二、BOM状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「发布」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已发布</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="success">已发布</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「停用」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已停用</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">草稿</el-tag>
            <el-tag size="small" type="danger">点击「删除」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已删除</el-tag>
            <el-tag size="small" type="info">异常终止</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">BOM创建后的初始状态。可修改BOM信息、维护明细行、发布或删除。点击「发布」后BOM状态变为已发布，不可再修改</el-descriptions-item>
          <el-descriptions-item label="已发布">BOM已正式发布，可被生产工单和MRP运算引用。已发布状态不可修改，如需调整可通过「复制」创建新版本后修改</el-descriptions-item>
          <el-descriptions-item label="已停用">BOM已停用，不再被新工单引用，但历史关联工单仍保留。停用后可重新启用恢复为已发布状态</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">BOM标识区</div>
          <div class="highlight-card-body">
            <p>• <strong>BOM编号：</strong>BOM的唯一标识编号，保存后由系统自动生成，无需手工输入</p>
            <p>• <strong>BOM名称：</strong>BOM的描述性名称，建议包含产品名+版本/规格，如"产品A标准BOM V1.0"<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>BOM类型：</strong>选择BOM类型（标准BOM/定制BOM/维修BOM），不同类型用于不同业务场景<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>版本：</strong>BOM的版本号，用于版本管理。可通过「复制」创建新版本号<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>基准数量：</strong>BOM的基准生产数量。当用量按批量定义时，基准数量表示该用量对应的产量。例如基准数量100、单件用量2，表示生产100个产品需200个物料<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>基准单位：</strong>基准数量的计量单位，选择产品后自动带出，无需手动填写</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">产品信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>产品编码：</strong>从物料库中选择该BOM对应的产品，点击搜索按钮弹出选择器，选择后自动带出产品名称和基准单位<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>产品名称：</strong>选择产品后自动带出，不可手动编辑</p>
            <p>• <strong>基准单位：</strong>选择产品后自动带出（取自物料主数据的计量单位），不可手动编辑</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">BOM明细区（子件物料）</div>
          <div class="highlight-card-body">
            <p>• <strong>物料编码：</strong>从物料库中选择子件物料，选择后自动带出物料名称、规格型号和单位<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>物料名称：</strong>选择物料后自动带出，不可手动编辑</p>
            <p>• <strong>规格型号：</strong>选择物料后自动带出，不可手动编辑</p>
            <p>• <strong>单位：</strong>选择物料后自动带出，不可手动编辑</p>
            <p>• <strong>单件用量：</strong>生产一个基准数量产品所需的该物料数量，支持小数。例如单件用量2，基准数量100，则总用量=2×100=200<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>损耗率(%)：</strong>生产过程中的损耗比例，系统按"单件用量×(1+损耗率)"计算实际发料量。如单件用量10、损耗率5%，则实际发料=10×1.05=10.5</p>
            <p>• <strong>关键料：</strong>勾选表示该物料为关键物料（影响交期或成本的关键件），MRP运算时优先计算</p>
            <p>• <strong>供应方式：</strong>选择物料的供应方式：直接领料（从仓库领料）、倒冲（生产完工后按消耗量扣减）、车间库存（从车间库存中领用）<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>虚拟件：</strong>勾选表示该子件为虚拟件（Phantom），虚拟件不实际入库，其BOM明细直接展开到当前层级</p>
            <p>• <strong>引用BOM：</strong>选择物料后系统自动查询该物料的已发布BOM并关联，支持多层级BOM展开。仅半成品物料会有引用BOM</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">状态与生效区</div>
          <div class="highlight-card-body">
            <p>• <strong>状态：</strong>BOM的当前状态（草稿/已发布/已停用）。新增时默认为草稿，已发布后不可修改此字段</p>
            <p>• <strong>生效日期：</strong>BOM的生效日期，生效后可被工单引用。可根据业务需要设置未来生效日期</p>
          </div>
        </div>

        <!-- 五、核心业务规则 -->
        <h4>五、核心业务规则</h4>
        <div class="highlight-card highlight-danger">
          <div class="highlight-card-title">关键规则</div>
          <div class="highlight-card-body">
            <p>• <strong>唯一发布版：</strong>同一产品同时仅允许一个已发布版本的BOM。发布前系统自动校验，若已有已发布版本将阻止发布</p>
            <p>• <strong>已发布不可修改：</strong>BOM状态为已发布后，所有字段均不可修改。如需调整，请先「复制」创建新版本BOM，修改后发布新版本，旧版本自动停用</p>
            <p>• <strong>不可删除已发布BOM：</strong>已发布状态的BOM不可删除，仅草稿或已停用状态可删除</p>
            <p>• <strong>基准数量与用量关系：</strong>子件总用量 = 单件用量 × 基准数量 × (1 + 损耗率)。基准数量为1时，单件用量即为每个产品的用量</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">多层级BOM规则</div>
          <div class="highlight-card-body">
            <p>• <strong>引用BOM：</strong>BOM明细中的子件如果是半成品（本身也有BOM），可在"引用BOM"列关联其BOM编号</p>
            <p>• <strong>递归展开：</strong>系统支持递归展开多层级BOM结构，最深支持3层。在BOM详情中点击「多层级展开」可查看完整物料树</p>
            <p>• <strong>虚拟件展开：</strong>虚拟件的BOM明细会在MRP运算时自动展开到上层，虚拟件本身不产生库存出入库</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>设置引用BOM后，MRP运算时会自动展开半成品的子件需求，实现完整物料需求计算</p>
          </div>
        </div>
        <div class="highlight-card highlight-success" style="margin-top: 12px;">
          <div class="highlight-card-title">供应方式说明</div>
          <div class="highlight-card-body">
            <p>• <strong>直接领料：</strong>从仓库按需领料，适用于大部分常规物料。工单下发后凭领料单到仓库领料</p>
            <p>• <strong>倒冲：</strong>生产完工入库时按BOM用量自动扣减库存，适用于不易按单件计量的辅助材料（如胶水、油漆等）</p>
            <p>• <strong>车间库存：</strong>从车间库存中领用，适用于车间常备的低值易耗品，定期补充车间库存</p>
          </div>
        </div>

        <!-- 六、业务操作流程 -->
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建BOM：</strong>点击「新增」创建BOM，填写BOM名称、类型、版本号和基准数量，BOM编号保存后自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>选择产品：</strong>在产品信息区点击搜索按钮，从物料库中选择该BOM对应的产品，选择后自动带出产品名称
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>维护明细：</strong>在BOM明细区点击「添加物料行」，逐行添加组成物料，设置用量、损耗率、供应方式等。半成品子件自动关联引用BOM
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>发布BOM：</strong>确认无误后点击「发布」，BOM状态变为已发布，可被生产工单和MRP运算引用
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>多层级展开：</strong>在BOM详情中点击「多层级展开」，查看BOM的完整多层级物料树结构，支持最深3层展开
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>复制新版本：</strong>如需修改已发布的BOM，点击「复制」创建新版本BOM，修改后发布新版本，旧版本自动停用
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Bom">
import { listBom, getBom, addBom, updateBom, delBom, publishBom, disableBom, enableBom, copyBom, getBomTree } from "@/api/mms/bom";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import { Search, Filter, RefreshLeft, ArrowDown, QuestionFilled, ArrowRight, Connection, CircleClose, CircleCheck, Upload } from '@element-plus/icons-vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2', 'c3', 'c4', 'c0', 'vc0', 'vc1', 'vc2', 'vc3', 'vc4'])

const { proxy } = getCurrentInstance();
const { mms_bom_type, mms_bom_status, mms_supply_type, wms_unit } = proxy.useDict("mms_bom_type", "mms_bom_status", "mms_supply_type", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_bom_index')

const bomList = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);
const treeOpen = ref(false); const treeLoading = ref(false); const treeData = ref([]); const treeBomNo = ref("");
const viewOpen = ref(false); const viewLoading = ref(false); const viewData = ref({});
const currentDetailIndex = ref(-1); const materialPickerRef = ref(null); const productPickerRef = ref(null);

const statusTabList = computed(() => mms_bom_status.value ? mms_bom_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { bomNo: { label: 'BOM编号', visible: true }, bomName: { label: 'BOM名称', visible: true }, productCode: { label: '产品编码', visible: true }, productName: { label: '产品名称', visible: true }, bomType: { label: 'BOM类型', visible: true }, version: { label: '版本', visible: true }, baseQty: { label: '基准数量', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_bom_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.bomNo) c++; if (queryParams.value.bomName) c++; if (queryParams.value.productCode) c++; if (queryParams.value.productName) c++; if (queryParams.value.bomType) c++; if (queryParams.value.status) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: { detailList: [] }, queryParams: { pageNum: 1, pageSize: 10, bomNo: undefined, bomName: undefined, productCode: undefined, productName: undefined, bomType: undefined, status: undefined, params: {} }, rules: { bomName: [{ required: true, message: "BOM名称不能为空", trigger: "blur" }], productCode: [{ required: true, message: "产品编号不能为空", trigger: "change" }], bomType: [{ required: true, message: "BOM类型不能为空", trigger: "change" }], baseQty: [{ required: true, message: "基准数量不能为空", trigger: "blur" }], version: [{ required: true, message: "版本不能为空", trigger: "blur" }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listBom(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { bomList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.bomNo) baseQuery.bomNo = queryParams.value.bomNo; if (queryParams.value.bomName) baseQuery.bomName = queryParams.value.bomName; if (queryParams.value.productCode) baseQuery.productCode = queryParams.value.productCode; if (queryParams.value.productName) baseQuery.productName = queryParams.value.productName; if (queryParams.value.bomType) baseQuery.bomType = queryParams.value.bomType; listBom(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_bom_status.value) { mms_bom_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.bomNo = undefined; queryParams.value.bomName = undefined; queryParams.value.productCode = undefined; queryParams.value.productName = undefined; queryParams.value.bomType = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function statusTabClass(status) { const map = { '0': 'tab-draft', '1': 'tab-done', '2': 'tab-void' }; return map[status] || ''; }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.bomId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { bomId: null, bomNo: undefined, bomName: undefined, productId: undefined, productCode: undefined, productName: undefined, bomType: "0", version: "1.0", baseQty: 1, baseUnit: undefined, baseUnitText: '', status: "0", effectiveDate: undefined, remark: undefined, detailList: [] }; proxy.resetForm("bomRef"); }
function handleAdd() { reset(); handleAddDetail(); open.value = true; title.value = "新增BOM"; }
function handleUpdate(row) { reset(); const bomId = row.bomId || ids.value[0]; getBom(bomId).then(response => { form.value = response.data; if (!form.value.detailList || form.value.detailList.length === 0) { form.value.detailList = []; handleAddDetail(); } form.value.baseUnitText = unitLabel(form.value.baseUnit); open.value = true; title.value = "修改BOM"; }); }
function submitForm() { proxy.$refs["bomRef"].validate(valid => { if (!valid) return; if (!form.value.detailList || form.value.detailList.length === 0) { proxy.$modal.msgError("BOM明细不能为空，请至少添加一行物料明细"); return; } const hasInvalid = form.value.detailList.some(d => !d.materialId); if (hasInvalid) { proxy.$modal.msgError("BOM明细中存在未选择物料的行，请选择物料或删除该行"); return; } if (form.value.bomId != null) { updateBom(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addBom(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const bomIds = row.bomId || ids.value; proxy.$modal.confirm('是否确认删除选中的BOM？').then(() => delBom(bomIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/bom/export", { ...queryParams.value }, `bom_${new Date().getTime()}.xlsx`); }

/* ===== BOM导入 ===== */
const importTips = [
  'BOM名称、产品编码、版本号为必填字段，不能为空',
  'BOM编号由系统自动生成，无需填写',
  '同一BOM的多行明细通过"产品编码+版本号"分组，每行填写一行物料明细',
  'BOM类型填：标准BOM/替代BOM/配方/临时BOM（或留空，默认标准BOM）',
  '供应方式填：直接领料/倒冲/车间库存（或留空，默认直接领料）',
  '是否关键料、是否虚拟件填：是/否（或留空，默认否）',
  '物料编码必须在物料管理中已存在，产品编码对应的物料类型应为半成品或成品',
  '如勾选「更新已存在数据」，相同产品编码+版本号的BOM将被覆盖更新',
];
function handleImport() { proxy.$refs['importRef'].open(); }
function bomTypeLabel(type) { if (type === null || type === undefined || type === '') return '—'; const item = mms_bom_type.value.find(d => d.value == type); return item ? item.label : '—' }
function statusLabel(status) { if (status === null || status === undefined || status === '') return '—'; const item = mms_bom_status.value.find(d => d.value == status); return item ? item.label : '—' }
function supplyTypeLabel(type) { if (type === null || type === undefined || type === '') return '—'; const item = mms_supply_type.value.find(d => d.value == type); return item ? item.label : '—' }
function unitLabel(unit) { if (unit === null || unit === undefined || unit === '') return '—'; const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : (unit || '—') }
function badgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'gray' }; return map[status] || 'gray'; }

/* ===== BOM详情查看 ===== */
function handleView(row) { const bomId = row.bomId || ids.value[0]; viewLoading.value = true; viewOpen.value = true; getBom(bomId).then(response => { viewData.value = response.data; if (!viewData.value.detailList) { viewData.value.detailList = []; } viewLoading.value = false; }); }

/* ===== BOM发布 ===== */
function handlePublish(row) { proxy.$modal.confirm('是否确认发布BOM[' + row.bomNo + ']？发布后不可修改。').then(() => publishBom(row.bomId)).then(() => { proxy.$modal.msgSuccess("发布成功"); getList(); }).catch(() => {}); }
function handleDisable(row) { proxy.$modal.confirm('是否确认停用BOM[' + row.bomNo + ']？停用后不再被新工单引用。').then(() => disableBom(row.bomId)).then(() => { proxy.$modal.msgSuccess("停用成功"); getList(); }).catch(() => {}); }
function handleEnable(row) { proxy.$modal.confirm('是否确认启用BOM[' + row.bomNo + ']？启用后将恢复为已发布状态。').then(() => enableBom(row.bomId)).then(() => { proxy.$modal.msgSuccess("启用成功"); getList(); }).catch(() => {}); }

/* ===== BOM复制 ===== */
function handleCopy(row) { proxy.$modal.confirm('是否确认复制BOM[' + row.bomNo + ']为新版本？').then(() => copyBom(row.bomId)).then(() => { proxy.$modal.msgSuccess("复制成功"); getList(); }).catch(() => {}); }

/* ===== BOM多层级展开 ===== */
function handleViewTree(row) { treeOpen.value = true; treeBomNo.value = row.bomNo; treeLoading.value = true; getBomTree(row.bomId).then(response => { treeData.value = addTreeKeys(response.data || [], 0); treeLoading.value = false; }); }
function handleViewTreeFromDetail() { if (viewData.value.bomId) { handleViewTree(viewData.value); } }
function addTreeKeys(nodes, prefix) { return nodes.map((n, i) => { n._rowKey = prefix + '-' + i; if (n.children && n.children.length > 0) { n.children = addTreeKeys(n.children, prefix + '-' + i); } else { delete n.children; } return n; }); }

/* ===== BOM明细行操作 ===== */
function handleAddDetail() { form.value.detailList.push({ detailId: null, seq: (form.value.detailList.length + 1) * 10, materialId: undefined, materialCode: '', materialName: '', materialType: '', specModel: '', unit: '', usageQty: 1, lossRate: 0, isKeyMaterial: '0', supplyType: '1', pickStoreId: undefined, isPhantom: '0', bomRefId: undefined, bomRefNo: '' }); }
function handleDeleteDetail(index) { if (form.value.detailList.length <= 1) { proxy.$modal.msgError("BOM明细至少需要保留一行，不允许删除"); return; } form.value.detailList.splice(index, 1); }

/* ===== 物料选择器 ===== */
function openMaterialPicker(index) { currentDetailIndex.value = index; materialPickerRef.value.open(form.value.detailList[index].materialId); }
function clearMaterial(index) { const d = form.value.detailList[index]; d.materialId = undefined; d.materialCode = ''; d.materialName = ''; d.materialType = ''; d.specModel = ''; d.unit = ''; d.bomRefId = undefined; d.bomRefNo = ''; }
function onMaterialPickerConfirm(material) { if (currentDetailIndex.value >= 0) { const d = form.value.detailList[currentDetailIndex.value]; d.materialId = material.materialId; d.materialCode = material.materialCode; d.materialName = material.materialName; d.specModel = material.specModel || ''; d.unit = material.unit || ''; d.materialType = material.materialType; d.bomRefId = undefined; d.bomRefNo = ''; autoFillBomRef(material.materialId, currentDetailIndex.value); } }

/* 自动查询该物料的已发布BOM，有则自动带出 */
function autoFillBomRef(materialId, index) { if (!materialId) return; listBom({ pageNum: 1, pageSize: 99, status: '1', productId: materialId }).then(res => { const rows = res.rows || []; if (rows.length > 0) { const d = form.value.detailList[index]; d.bomRefId = rows[0].bomId; d.bomRefNo = rows[0].bomNo; } }).catch(() => {}); }

/* ===== 产品选择器 ===== */
function openProductPicker() { productPickerRef.value.open(form.value.productId); }
function clearProduct() { form.value.productId = undefined; form.value.productCode = undefined; form.value.productName = undefined; form.value.baseUnit = undefined; form.value.baseUnitText = ''; }
function onProductPickerConfirm(material) { form.value.productId = material.materialId; form.value.productCode = material.materialCode; form.value.productName = material.materialName; form.value.baseUnit = material.unit || ''; form.value.baseUnitText = unitLabel(material.unit); }

getList();
</script>

<style scoped>
.mms-bom-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-bom-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-bom-page .filter-card { padding:14px 20px 16px; }
.mms-bom-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-bom-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-bom-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-bom-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-bom-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-bom-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-bom-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-bom-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-bom-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-bom-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-bom-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-bom-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-bom-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-bom-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-bom-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-bom-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-bom-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-bom-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-bom-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-bom-page .field .control :deep(.el-select) { width:100%; }
.mms-bom-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-bom-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-bom-page .toolbar .left, .mms-bom-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-bom-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-bom-page .table-wrap { overflow-x:auto; }
.mms-bom-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-bom-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-bom-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-bom-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-bom-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-bom-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-bom-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-bom-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-bom-page .badge.amber .dot { background:var(--amber-500); }
.mms-bom-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-bom-page .badge.blue .dot { background:var(--blue-500); }
.mms-bom-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-bom-page .badge.green .dot { background:var(--green-500); }
.mms-bom-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .mms-bom-page .badge.red .dot { background:var(--red-500); }
.mms-bom-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-bom-page .badge.gray .dot { background:var(--ink-400); }
.mms-bom-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-bom-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-bom-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-bom-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-bom-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-bom-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-bom-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-bom-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-bom-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-bom-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-bom-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-bom-page .status-tab.tab-draft .dot { background:var(--amber-500); } .mms-bom-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .mms-bom-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.mms-bom-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-bom-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-bom-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-bom-page .status-tab.tab-void .dot { background:var(--ink-400); } .mms-bom-page .status-tab.tab-void .count { background:var(--ink-100); color:var(--ink-500); } .mms-bom-page .status-tab.is-active.tab-void .count { background:var(--ink-400); color:#fff; }
.mms-bom-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.mms-bom-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.mms-bom-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.mms-bom-page .detail-toolbar { display:flex; align-items:center; gap:12px; margin-bottom:10px; }
.mms-bom-page .detail-toolbar .detail-tip { font-size:12px; color:var(--ink-400); }
.mms-bom-page .detail-table { --el-table-header-bg-color:var(--ink-50); }
.mms-bom-page .form-tip-text { font-size: 12px; color: var(--ink-400); margin-left: 8px; }
.mms-bom-page .empty-detail { padding:20px 0; }
.mms-bom-page .level-badge { display:inline-flex; align-items:center; justify-content:center; width:22px; height:22px; border-radius:50%; font-size:12px; font-weight:600; }
.mms-bom-page .level-badge.level-0 { background:var(--brand-100); color:var(--brand-700); }
.mms-bom-page .level-badge.level-1 { background:var(--blue-50); color:var(--blue-700); }
.mms-bom-page .level-badge.level-2 { background:var(--amber-50); color:var(--amber-700); }
.mms-bom-page .level-badge.level-3 { background:var(--green-50); color:var(--green-700); }
.status-help-content { max-height:520px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.status-help-content .flow-item { display:flex; align-items:center; gap:8px; }
.status-help-content .flow-arrow { color:#909399; font-size:16px; }
.status-help-content .highlight-card { border-radius:8px; padding:16px; border:1px solid; }
.status-help-content .highlight-card-title { font-size:14px; font-weight:600; margin-bottom:8px; display:flex; align-items:center; }
.status-help-content .highlight-card-body { font-size:13px; color:#606266; line-height:1.6; }
.status-help-content .highlight-card-body p { margin:4px 0; }
.status-help-content .highlight-primary { background-color:#ecf5ff; border-color:#a0cfff; } .status-help-content .highlight-primary .highlight-card-title { color:#409eff; }
.status-help-content .highlight-success { background-color:#f0f9eb; border-color:#b3e19d; } .status-help-content .highlight-success .highlight-card-title { color:#67c23a; }
.status-help-content .highlight-warning { background-color:#fdf6ec; border-color:#f5dab1; } .status-help-content .highlight-warning .highlight-card-title { color:#e6a23c; }
.status-help-content .highlight-danger { background-color:#fef0f0; border-color:#fbc4c4; } .status-help-content .highlight-danger .highlight-card-title { color:#f56c6c; }
@media (max-width:1100px) { .mms-bom-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mms-bom-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>