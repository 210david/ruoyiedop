<template>
  <div class="app-container safety-hazard-page">
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
          <label>隐患编号</label>
          <div class="control">
            <el-input v-model="queryParams.hazardCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>隐患描述</label>
          <div class="control">
            <el-input v-model="queryParams.hazardDesc" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>隐患类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.hazardType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_hazard_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>隐患等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.hazardLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_hazard_level" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>隐患状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.hazardStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_hazard_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>发现时间</label>
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in safety_hazard_status" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:hazard:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:hazard:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Upload" @click="handleExport" v-hasPermi="['safety:hazard:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_hazard_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hazardList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="隐患编号" prop="hazardCode" key="hazardCode" :width="colWidth('hazardCode', 160)" resizable show-overflow-tooltip v-if="columns.hazardCode.visible" />
          <el-table-column label="隐患描述" prop="hazardDesc" key="hazardDesc" :width="colWidth('hazardDesc', 220)" resizable show-overflow-tooltip v-if="columns.hazardDesc.visible" />
          <el-table-column label="隐患类型" prop="hazardType" key="hazardType" :width="colWidth('hazardType', 120)" resizable align="center" v-if="columns.hazardType.visible">
            <template #default="scope"><span class="badge violet">{{ hazardTypeLabel(scope.row.hazardType) }}</span></template>
          </el-table-column>
          <el-table-column label="隐患等级" prop="hazardLevel" key="hazardLevel" :width="colWidth('hazardLevel', 100)" resizable align="center" v-if="columns.hazardLevel.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.hazardLevel === '2' ? 'red' : 'amber'">
                <span class="dot"></span>{{ hazardLevelLabel(scope.row.hazardLevel) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="隐患状态" prop="hazardStatus" key="hazardStatus" :width="colWidth('hazardStatus', 120)" resizable align="center" sortable="custom" v-if="columns.hazardStatus.visible">
            <template #default="scope">
              <span class="badge" :class="hazardBadgeClass(scope.row.hazardStatus)">
                <span class="dot"></span>{{ hazardStatusLabel(scope.row.hazardStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="负责人" prop="personName" key="personName" :width="colWidth('personName', 100)" resizable show-overflow-tooltip v-if="columns.personName.visible" />
          <el-table-column label="整改期限" prop="rectifyDeadline" key="rectifyDeadline" :width="colWidth('rectifyDeadline', 120)" resizable align="center" v-if="columns.rectifyDeadline.visible" />
          <el-table-column label="整改日期" prop="rectifyTime" key="rectifyTime" :width="colWidth('rectifyTime', 160)" resizable align="center" v-if="columns.rectifyTime.visible" />
          <el-table-column label="发现人" prop="discoverPerson" key="discoverPerson" :width="colWidth('discoverPerson', 100)" resizable align="center" v-if="columns.discoverPerson.visible" />
          <el-table-column label="发现时间" prop="discoverTime" key="discoverTime" :width="colWidth('discoverTime', 160)" resizable align="center" v-if="columns.discoverTime.visible" />
          <el-table-column label="操作" width="250" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:hazard:query']">查看</el-button>
              <el-button v-if="scope.row.hazardStatus === '0' || scope.row.hazardStatus === '2'" link type="primary" icon="Check" @click="handleSubmit(scope.row)" v-hasPermi="['safety:hazard:submit']">提交</el-button>
              <el-button v-if="scope.row.hazardStatus === '1'" link type="primary" icon="DocumentChecked" @click="handleApprove(scope.row)" v-hasPermi="['safety:hazard:approve']">审批</el-button>
              <el-button v-if="scope.row.hazardStatus === '3'" link type="primary" icon="Edit" @click="handleStartRectify(scope.row)" v-hasPermi="['safety:hazard:rectify']">整改</el-button>
              <el-button v-if="scope.row.hazardStatus === '4'" link type="primary" icon="Upload" @click="handleStartRectify(scope.row)" v-hasPermi="['safety:hazard:rectify']">提交整改</el-button>
              <el-button v-if="scope.row.hazardStatus === '5'" link type="primary" icon="CircleCheck" @click="handleVerify(scope.row)" v-hasPermi="['safety:hazard:verify']">验收</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:hazard:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:hazard:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hazardRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>隐患信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="隐患编号" prop="hazardCode"><el-input v-model="form.hazardCode" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="隐患类型" prop="hazardType"><el-select v-model="form.hazardType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_hazard_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="隐患等级" prop="hazardLevel"><el-radio-group v-model="form.hazardLevel"><el-radio value="1">一般</el-radio><el-radio value="2">重大</el-radio></el-radio-group></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="隐患来源" prop="sourceType"><el-select v-model="form.sourceType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_hazard_source" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-form-item label="隐患描述" prop="hazardDesc"><el-input v-model="form.hazardDesc" type="textarea" :rows="3" placeholder="请输入隐患描述" /></el-form-item>
              <el-form-item label="隐患图片" prop="photos"><image-upload v-model="form.photos" :limit="5" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>责任分配</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="负责人" prop="personId">
                  <el-input v-model="form.personName" readonly placeholder="请选择负责人" style="width: 100%" @click="openUserPicker">
                    <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                    <template #suffix><el-icon v-if="form.personName" class="clear-icon" @click.stop="clearPerson"><CircleClose /></el-icon></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="责任部门" prop="deptId">
                  <el-input v-model="form.deptName" readonly placeholder="选择负责人后自动带出" style="width: 100%" disabled>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
              <el-form-item label="整改要求" prop="rectifyReq"><el-input v-model="form.rectifyReq" type="textarea" :rows="2" placeholder="请输入整改要求" /></el-form-item>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="整改期限" prop="rectifyDeadline"><el-date-picker v-model="form.rectifyDeadline" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">隐患详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.hazardCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.hazardCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>隐患信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">隐患编号</span><div class="rd-value">{{ viewData.hazardCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">隐患类型</span><div class="rd-value"><dict-tag :options="safety_hazard_type" :value="viewData.hazardType" /></div></div>
              <div class="rd-item"><span class="rd-label">隐患等级</span><div class="rd-value"><span class="badge" :class="viewData.hazardLevel === '2' ? 'red' : 'amber'"><span class="dot"></span>{{ hazardLevelLabel(viewData.hazardLevel) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">隐患来源</span><div class="rd-value"><dict-tag :options="safety_hazard_source" :value="viewData.sourceType" /></div></div>
              <div class="rd-item"><span class="rd-label">隐患状态</span><div class="rd-value"><span class="badge" :class="hazardBadgeClass(viewData.hazardStatus)"><span class="dot"></span>{{ hazardStatusLabel(viewData.hazardStatus) }}</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">隐患描述</span><div class="rd-value">{{ viewData.hazardDesc || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.photos"><span class="rd-label">隐患图片</span><div class="rd-value"><image-preview :src="viewData.photos" :width="100" :height="100" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.personName || viewData.deptName || viewData.rectifyReq || viewData.rectifyDeadline">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>责任分配</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewData.personName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">责任部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">整改要求</span><div class="rd-value">{{ viewData.rectifyReq || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">整改期限</span><div class="rd-value">{{ viewData.rectifyDeadline || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.rectifyDesc || viewData.verifyResult">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>整改验收信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">整改说明</span><div class="rd-value">{{ viewData.rectifyDesc || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">整改时间</span><div class="rd-value">{{ viewData.rectifyTime || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.rectifyPhotos"><span class="rd-label">整改后图片</span><div class="rd-value"><image-preview :src="viewData.rectifyPhotos" :width="100" :height="100" /></div></div>
            </div>
            <div class="rd-grid" v-if="viewData.verifyResult" style="margin-top:12px">
              <div class="rd-item"><span class="rd-label">验收结果</span><div class="rd-value"><el-tag v-if="viewData.verifyResult === '1'" type="success" size="small" effect="light" round>验收通过</el-tag><el-tag v-else-if="viewData.verifyResult === '2'" type="danger" size="small" effect="light" round>验收不通过</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">验收人</span><div class="rd-value">{{ viewData.verifyPerson || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">验收时间</span><div class="rd-value">{{ viewData.verifyTime || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.verifyComment"><span class="rd-label">验收意见</span><div class="rd-value">{{ viewData.verifyComment }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 8v4l3 3"/><circle cx="12" cy="12" r="9"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in viewData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1' || log.auditAction === '6', 'rd-timeline-dot--error': log.auditAction === '2' || log.auditAction === '7' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审批通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                      <el-tag v-else-if="log.auditAction === '3'" type="info" size="small" effect="light" round>提交审核</el-tag>
                      <el-tag v-else-if="log.auditAction === '4'" type="warning" size="small" effect="light" round>开始整改</el-tag>
                      <el-tag v-else-if="log.auditAction === '5'" type="warning" size="small" effect="light" round>整改提交</el-tag>
                      <el-tag v-else-if="log.auditAction === '6'" type="success" size="small" effect="light" round>验收通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '7'" type="danger" size="small" effect="light" round>验收不通过</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">操作人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 整改对话框 -->
    <el-dialog v-model="rectifyOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">{{ rectifyTitle }}</span>
        </div>
      </template>
      <el-form ref="rectifyRef" :model="rectifyForm" :rules="rectifyRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card" v-if="rectifyRejectInfo">
            <div class="rd-card-header" @click="toggleCard('c_reject')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>上次验收驳回</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_reject }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c_reject">
              <div class="rd-timeline-comment" style="margin:0 0 8px"><strong>驳回原因：</strong>{{ rectifyRejectInfo.comment }}</div>
              <div style="font-size:13px;color:var(--ink-500)">验收人：{{ rectifyRejectInfo.person }} ｜ 验收时间：{{ rectifyRejectInfo.time }}</div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>整改信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="负责人" prop="personName"><el-input v-model="rectifyForm.personName" readonly placeholder="--" style="width: 100%" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="整改日期" prop="rectifyTime"><el-date-picker v-model="rectifyForm.rectifyTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="整改说明" prop="rectifyDesc"><el-input v-model="rectifyForm.rectifyDesc" type="textarea" :rows="3" placeholder="请输入整改说明" /></el-form-item>
              <el-form-item label="整改后图片" prop="rectifyPhotos"><image-upload v-model="rectifyForm.rectifyPhotos" :limit="5" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitRectify">提交整改</el-button><el-button @click="rectifyOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 验收对话框 -->
    <el-dialog v-model="verifyOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">隐患验收</span>
        </div>
      </template>
      <el-form ref="verifyRef" :model="verifyForm" :rules="verifyRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>验收信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="验收日期" prop="verifyTime"><el-date-picker v-model="verifyForm.verifyTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="验收结果" prop="verifyResult"><el-radio-group v-model="verifyForm.verifyResult"><el-radio value="1">通过</el-radio><el-radio value="2">不通过</el-radio></el-radio-group></el-form-item>
              <el-form-item label="验收意见" prop="verifyComment"><el-input v-model="verifyForm.verifyComment" type="textarea" :rows="3" placeholder="请输入验收意见" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitVerify">确认验收</el-button><el-button @click="verifyOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveOpen" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><path d="M21 12c0 4.97-4.03 9-9 9s-9-4.03-9-9 4.03-9 9-9 9 4.03 9 9z"/></svg></div>
          <span class="rd-detail-header-title">隐患审批</span>
          <div class="rd-detail-header-sub" v-if="approveHazardData.hazardCode">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ approveHazardData.hazardCode }}</span>
            <dict-tag :options="safety_hazard_status" :value="approveHazardData.hazardStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a_basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>隐患信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_basic">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">隐患描述</span><div class="rd-value" :class="{ 'rd-value--muted': !approveHazardData.hazardDesc }">{{ approveHazardData.hazardDesc || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">隐患类型</span><div class="rd-value"><dict-tag :options="safety_hazard_type" :value="approveHazardData.hazardType" /></div></div>
              <div class="rd-item"><span class="rd-label">隐患等级</span><div class="rd-value"><dict-tag :options="safety_hazard_level" :value="approveHazardData.hazardLevel" /></div></div>
              <div class="rd-item"><span class="rd-label">隐患来源</span><div class="rd-value"><dict-tag :options="safety_hazard_source" :value="approveHazardData.sourceType" /></div></div>
              <div class="rd-item"><span class="rd-label">发现人</span><div class="rd-value" :class="{ 'rd-value--muted': !approveHazardData.discoverPerson }">{{ approveHazardData.discoverPerson || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">发现时间</span><div class="rd-value" :class="{ 'rd-value--muted': !approveHazardData.discoverTime }">{{ approveHazardData.discoverTime || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a_assign')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>责任分配</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_assign }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_assign">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value" :class="{ 'rd-value--muted': !approveHazardData.personName }">{{ approveHazardData.personName || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">责任部门</span><div class="rd-value" :class="{ 'rd-value--muted': !approveHazardData.deptName }">{{ approveHazardData.deptName || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">整改期限</span><div class="rd-value" :class="{ 'rd-value--muted': !approveHazardData.rectifyDeadline }">{{ approveHazardData.rectifyDeadline || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">整改要求</span><div class="rd-value" :class="{ 'rd-value--muted': !approveHazardData.rectifyReq }">{{ approveHazardData.rectifyReq || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="approveHazardData.auditLogList && approveHazardData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('a_audit')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 8v4l3 3"/><circle cx="12" cy="12" r="9"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_audit }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_audit" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in approveHazardData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1' || log.auditAction === '6', 'rd-timeline-dot--error': log.auditAction === '2' || log.auditAction === '7' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审批通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                      <el-tag v-else-if="log.auditAction === '3'" type="info" size="small" effect="light" round>提交审核</el-tag>
                      <el-tag v-else-if="log.auditAction === '4'" type="warning" size="small" effect="light" round>开始整改</el-tag>
                      <el-tag v-else-if="log.auditAction === '5'" type="warning" size="small" effect="light" round>整改提交</el-tag>
                      <el-tag v-else-if="log.auditAction === '6'" type="success" size="small" effect="light" round>验收通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '7'" type="danger" size="small" effect="light" round>验收不通过</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">操作人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a_remark')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>审批意见</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_remark }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_remark">
            <el-form ref="approveRef" :model="approveForm" label-width="0">
              <el-form-item prop="approveComment">
                <el-input v-model="approveForm.approveComment" type="textarea" :rows="3" placeholder="请输入审批意见（不通过时必填）" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="confirmApprove('1')">通 过</el-button>
        <el-button type="danger" @click="confirmApprove('2')">不通过</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 责任人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择责任人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="隐患管理业务操作说明" width="760px" append-to-body>
      <div class="status-help-content">
        <h4>一、隐患闭环业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待整改</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">整改中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待验收</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已闭环</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top:8px">
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">审批驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">草稿（修改后重新提交）</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top:8px">
          <div class="flow-item">
            <el-tag type="danger">超期未整改</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">继续整改</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">隐患登记后的初始状态，可修改、提交审批或删除</el-descriptions-item>
          <el-descriptions-item label="待审批">隐患已提交审批，等待审批人审批</el-descriptions-item>
          <el-descriptions-item label="审批驳回">审批不通过，退回草稿状态，修改后可重新提交</el-descriptions-item>
          <el-descriptions-item label="待整改">审批通过，等待责任人开始整改</el-descriptions-item>
          <el-descriptions-item label="整改中">责任人正在进行整改，整改完成后提交整改说明</el-descriptions-item>
          <el-descriptions-item label="待验收">整改已完成提交，等待验收人验收</el-descriptions-item>
          <el-descriptions-item label="已闭环">验收通过，隐患完成闭环</el-descriptions-item>
          <el-descriptions-item label="超期未整改">整改期限已过但未完成整改，需优先处理</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>隐患来源：</strong>包括随手拍、计划排查、专项排查、举报、上级交办等多种来源</p>
          <p>• <strong>隐患等级：</strong>分为一般隐患和重大隐患，重大隐患需重点跟踪</p>
          <p>• <strong>闭环机制：</strong>隐患从登记、审批、整改到验收形成完整闭环，全程留痕</p>
          <p>• <strong>超期预警：</strong>整改期限到期后未完成整改的隐患自动标记为超期未整改</p>
          <p>• <strong>验收不通过：</strong>验收不通过时需重新整改，整改后重新提交验收</p>
          <p>• <strong>审核记录：</strong>隐患详情页可查看完整的审核记录时间线</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyHazard">
import { listHazard, getHazard, addHazard, updateHazard, delHazard, submitHazard, approveHazard as approveHazardApi, startRectify, submitRectify as submitRectifyApi, verifyHazard as verifyHazardApi } from '@/api/safety/hazard'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { ArrowDown, Search, Filter, RefreshLeft, CircleClose, WarningFilled, ArrowRight } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_hazard_type, safety_hazard_level, safety_hazard_source, safety_hazard_status } = proxy.useDict('safety_hazard_type', 'safety_hazard_level', 'safety_hazard_source', 'safety_hazard_status')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_hazard_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","c4","c5","c_audit","a_basic","a_assign","a_audit","a_remark","c_reject","c_rectify","vc0","vc1","vc2","vc3","vc4"])

const hazardList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const rectifyOpen = ref(false)
const rectifyTitle = ref('隐患整改')
const rectifyRejectInfo = ref(null)
const verifyOpen = ref(false)
const approveOpen = ref(false)
const approveHazardData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })

const defaultColumns = {
  hazardCode: { label: '隐患编号', visible: true },
  hazardDesc: { label: '隐患描述', visible: true },
  hazardType: { label: '隐患类型', visible: true },
  hazardLevel: { label: '隐患等级', visible: true },
  hazardStatus: { label: '隐患状态', visible: true },
  personName: { label: '负责人', visible: true },
  rectifyDeadline: { label: '整改期限', visible: true },
  rectifyTime: { label: '整改日期', visible: true },
  discoverPerson: { label: '发现人', visible: true },
  discoverTime: { label: '发现时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_hazard_columns')
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
  rectifyForm: {},
  verifyForm: {},
  approveForm: {},
  queryParams: { pageNum: 1, pageSize: 10, hazardCode: undefined, hazardDesc: undefined, hazardType: undefined, hazardLevel: undefined, hazardStatus: undefined, params: {} },
  rules: {
    hazardDesc: [{ required: true, message: '隐患描述不能为空', trigger: 'blur' }],
    hazardType: [{ required: true, message: '隐患类型不能为空', trigger: 'change' }],
    hazardLevel: [{ required: true, message: '隐患等级不能为空', trigger: 'change' }],
    deptId: [{ required: true, message: '责任部门不能为空', trigger: 'change' }],
    personId: [{ required: true, message: '责任人不能为空', trigger: 'change' }],
    rectifyReq: [{ required: true, message: '整改要求不能为空', trigger: 'blur' }],
    rectifyDeadline: [{ required: true, message: '整改期限不能为空', trigger: 'change' }]
  },
  rectifyRules: { rectifyDesc: [{ required: true, message: '整改说明不能为空', trigger: 'blur' }] },
  verifyRules: { verifyResult: [{ required: true, message: '验收结果不能为空', trigger: 'change' }], verifyComment: [{ required: true, message: '验收意见不能为空', trigger: 'blur' }] },
  approveRules: {}
})

const { queryParams, form, rectifyForm, verifyForm, approveForm, rules, rectifyRules, verifyRules, approveRules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.hazardCode) count++
  if (queryParams.value.hazardDesc) count++
  if (queryParams.value.hazardType) count++
  if (queryParams.value.hazardLevel) count++
  if (queryParams.value.hazardStatus) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  listHazard(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    hazardList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
    loadStatusCounts()
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; queryParams.value.hazardCode = undefined; queryParams.value.hazardDesc = undefined; queryParams.value.hazardType = undefined; queryParams.value.hazardLevel = undefined; queryParams.value.hazardStatus = undefined; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.hazardId); single.value = selection.length !== 1; multiple.value = !selection.length }

function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.hazardStatus = status === 'all' ? undefined : status; handleQuery() }

function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; collapsedCards.c3 = false; collapsedCards.c4 = false; collapsedCards.c5 = false; open.value = true; title.value = '添加隐患' }
function handleUpdate(row) { reset(); const hazardId = row.hazardId || ids.value[0]; getHazard(hazardId).then(response => { form.value = response.data; collapsedCards.c2 = !response.data.remark; open.value = true; title.value = '修改隐患' }) }
function handleView(row) { const hazardId = row.hazardId || ids.value[0]; getHazard(hazardId).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交该隐患审批吗？').then(function() { return submitHazard(row.hazardId) }).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {}) }
function handleApprove(row) {
  approveForm.value = { hazardId: row.hazardId, approveComment: '' }
  approveHazardData.value = {}
  collapsedCards.a_basic = false
  collapsedCards.a_assign = false
  collapsedCards.a_audit = false
  collapsedCards.a_remark = false
  getHazard(row.hazardId).then(res => {
    approveHazardData.value = res.data
    approveOpen.value = true
  })
}
function confirmApprove(result) {
  if (result === '2' && !approveForm.value.approveComment) {
    proxy.$modal.msgWarning('审批不通过时必须填写审批意见')
    return
  }
  approveForm.value.approveResult = result
  approveHazardApi(approveForm.value).then(() => {
    proxy.$modal.msgSuccess(result === '1' ? '审批通过' : '已驳回')
    approveOpen.value = false
    getList()
  })
}
function handleStartRectify(row) { rectifyForm.value.hazardId = row.hazardId; rectifyForm.value.personName = row.personName; rectifyForm.value.rectifyTime = parseToday(); rectifyForm.value.rectifyDesc = row.rectifyDesc || ''; rectifyForm.value.rectifyPhotos = row.rectifyPhotos || ''; rectifyRejectInfo.value = (row.hazardStatus === '4' && row.verifyResult === '2') ? { comment: row.verifyComment, person: row.verifyPerson, time: row.verifyTime } : null; rectifyTitle.value = rectifyRejectInfo.value ? '重新整改' : '隐患整改'; rectifyOpen.value = true }
function submitRectify() { proxy.$refs['rectifyRef'].validate(valid => { if (valid) { submitRectifyApi(rectifyForm.value).then(() => { proxy.$modal.msgSuccess('整改提交成功'); rectifyOpen.value = false; getList() }) } }) }
function handleVerify(row) { verifyForm.value.hazardId = row.hazardId; verifyForm.value.verifyTime = parseToday(); verifyForm.value.verifyResult = ''; verifyForm.value.verifyComment = ''; verifyOpen.value = true }
function submitVerify() { proxy.$refs['verifyRef'].validate(valid => { if (valid) { verifyHazardApi(verifyForm.value).then(() => { proxy.$modal.msgSuccess('验收完成'); verifyOpen.value = false; getList() }) } }) }

function submitForm() {
  proxy.$refs['hazardRef'].validate(valid => {
    if (valid) {
      if (form.value.hazardId != undefined) { updateHazard(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addHazard(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const hazardIds = row.hazardId || ids.value; proxy.$modal.confirm('是否确认删除隐患编号为"' + hazardIds + '"的数据项？').then(function() { return delHazard(hazardIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/hazard/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `hazard_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { hazardId: undefined, hazardCode: undefined, hazardDesc: undefined, hazardType: undefined, hazardLevel: '1', sourceType: undefined, deptId: undefined, deptName: undefined, personId: undefined, personName: undefined, rectifyReq: undefined, rectifyDeadline: undefined, hazardStatus: '0', remark: undefined }
  proxy.resetForm('hazardRef')
}

/** 打开责任人选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.personId) }
/** 责任人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.personId = user.userId
  form.value.personName = user.nickName
  form.value.deptId = user.deptId
  form.value.deptName = user.deptName || ''
}
/** 清除责任人 */
function clearPerson() {
  form.value.personId = undefined
  form.value.personName = undefined
  form.value.deptId = undefined
  form.value.deptName = undefined
}

/** 获取今日日期字符串 YYYY-MM-DD */
function parseToday() { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` }

function loadStatusCounts() {
  listHazard({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    ;(res.rows || []).forEach(r => { counts[r.hazardStatus] = (counts[r.hazardStatus] || 0) + 1 })
    statusCounts.value = counts
  }).catch(() => {})
}

function hazardTypeLabel(type) { const item = safety_hazard_type.value.find(d => d.value == type); return item ? item.label : '-' }
function hazardLevelLabel(level) { const item = safety_hazard_level.value.find(d => d.value == level); return item ? item.label : '-' }
function hazardStatusLabel(status) { const item = safety_hazard_status.value.find(d => d.value == status); return item ? item.label : '-' }
function hazardBadgeClass(status) { const map = { '0': 'gray', '1': 'amber', '2': 'red', '3': 'blue', '4': 'blue', '5': 'blue', '6': 'green', '7': 'red' }; return map[status] || 'gray' }
function statusTabClass(value) { const map = { '0': 'tab-void', '1': 'tab-draft', '2': 'tab-reject', '3': 'tab-audit', '4': 'tab-audit', '5': 'tab-audit', '6': 'tab-done', '7': 'tab-reject' }; return map[value] || '' }

getList()
</script>

<style scoped>
.safety-hazard-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-hazard-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-hazard-page .filter-card { padding:14px 20px 16px; }
.safety-hazard-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-hazard-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-hazard-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-hazard-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-hazard-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-hazard-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-hazard-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-hazard-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-hazard-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-hazard-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-hazard-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-hazard-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-hazard-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-hazard-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-hazard-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-hazard-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-hazard-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-hazard-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-hazard-page .field .control :deep(.el-select) { width:100%; }
.safety-hazard-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-hazard-page .field .control :deep(.el-date-editor) { width:100%; }
.safety-hazard-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.safety-hazard-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.safety-hazard-page .tabs-track::-webkit-scrollbar { display:none; }
.safety-hazard-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.safety-hazard-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.safety-hazard-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.safety-hazard-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.safety-hazard-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.safety-hazard-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.safety-hazard-page .status-tab.is-active .dot { background:var(--brand-500); }
.safety-hazard-page .status-tab.tab-draft .dot { background:var(--amber-500); } .safety-hazard-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .safety-hazard-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.safety-hazard-page .status-tab.tab-audit .dot { background:var(--blue-500); } .safety-hazard-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .safety-hazard-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.safety-hazard-page .status-tab.tab-done .dot { background:var(--green-500); } .safety-hazard-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .safety-hazard-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.safety-hazard-page .status-tab.tab-void .dot { background:var(--ink-400); }
.safety-hazard-page .status-tab.tab-reject .dot { background:var(--red-500); } .safety-hazard-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); } .safety-hazard-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.safety-hazard-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-hazard-page .toolbar .left, .safety-hazard-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-hazard-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-hazard-page .table-wrap { overflow-x:auto; }
.safety-hazard-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-hazard-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-hazard-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-hazard-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-hazard-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-hazard-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-hazard-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-hazard-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-hazard-page .badge.amber .dot { background:var(--amber-500); }
.safety-hazard-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-hazard-page .badge.blue .dot { background:var(--blue-500); }
.safety-hazard-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-hazard-page .badge.green .dot { background:var(--green-500); }
.safety-hazard-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-hazard-page .badge.red .dot { background:var(--red-500); }
.safety-hazard-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-hazard-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-hazard-page .badge.gray .dot { background:var(--ink-400); }
.safety-hazard-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-hazard-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-hazard-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
/* 审核记录时间线样式 */
.rd-timeline { position: relative; padding-left: 24px; }
.rd-timeline-item { position: relative; padding-bottom: 20px; }
.rd-timeline-item:last-child { padding-bottom: 0; }
.rd-timeline-item::before { content: ''; position: absolute; left: -16px; top: 8px; bottom: 0; width: 2px; background: var(--ink-200); }
.rd-timeline-item:last-child::before { display: none; }
.rd-timeline-dot { position: absolute; left: -22px; top: 4px; width: 10px; height: 10px; border-radius: 50%; background: var(--ink-400); border: 2px solid #fff; box-shadow: 0 0 0 1px var(--ink-300); }
.rd-timeline-dot--success { background: var(--green-500); box-shadow: 0 0 0 1px var(--green-500); }
.rd-timeline-dot--error { background: var(--red-500); box-shadow: 0 0 0 1px var(--red-500); }
.rd-timeline-content { }
.rd-timeline-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.rd-timeline-title { }
.rd-timeline-time { font-size: 13px; color: var(--ink-500); }
.rd-timeline-body { margin-bottom: 4px; }
.rd-timeline-body .rd-item { display: flex; gap: 6px; align-items: baseline; font-size: 14px; }
.rd-timeline-body .rd-label { color: var(--ink-500); min-width: 60px; }
.rd-timeline-body .rd-value { color: var(--ink-900); font-weight: 500; }
.rd-timeline-comment { font-size: 14px; color: var(--ink-700); background: var(--ink-50); border-radius: 6px; padding: 8px 12px; margin-top: 6px; }
.rd-timeline-comment strong { color: var(--ink-500); font-weight: 600; }
.safety-hazard-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.safety-hazard-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.safety-hazard-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.status-help-content { max-height:500px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.status-help-content .flow-item { display:flex; align-items:center; gap:8px; }
.status-help-content .flow-arrow { color:#909399; font-size:16px; }
.status-help-content .highlight-card { background-color:#ecf5ff; border-radius:8px; padding:16px; border-left:4px solid #409eff; }
.status-help-content .highlight-card p { margin:6px 0; line-height:1.6; font-size:13px; color:#606266; }
</style>
