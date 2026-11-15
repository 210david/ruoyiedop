<template>
  <div class="app-container oa-list-page oa-vehicle-page">
    <!-- 筛选条件 -->
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
          <label>车牌号</label>
          <div class="control">
            <el-input v-model="queryParams.plateNo" placeholder="请输入车牌号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>车辆类型</label>
          <div class="control">
            <el-select v-model="queryParams.vehicleType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in vehicleTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>使用用途</label>
          <div class="control">
            <el-input v-model="queryParams.vehicleUse" placeholder="请输入使用用途" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>所属部门</label>
          <div class="control">
            <el-input v-model="queryParams.ownerDept" placeholder="请输入所属部门" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>车辆编号</label>
          <div class="control">
            <el-input v-model="queryParams.vehicleCode" placeholder="请输入车辆编号" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>品牌型号</label>
          <div class="control">
            <el-input v-model="queryParams.vehicleModel" placeholder="请输入品牌型号" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>默认驾驶员</label>
          <div class="control">
            <el-input v-model="queryParams.driverName" placeholder="请输入驾驶员姓名" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in vehicleStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>年检到期</label>
          <div class="control">
            <el-date-picker v-model="inspectRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>保险到期</label>
          <div class="control">
            <el-date-picker v-model="insuranceRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- 列表 -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['oa:vehicle:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['oa:vehicle:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['oa:vehicle:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['oa:vehicle:export']">导出</el-button>
          <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['oa:vehicle:import']">导入</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="oa_vehicle_index"></right-toolbar>
        </div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="vehicleList" @selection-change="handleSelectionChange" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" width="55" align="center" />
          <el-table-column label="车牌号" prop="plateNo" align="center" width="120" v-if="columns.plateNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.plateNo }}</span></template>
          </el-table-column>
          <el-table-column label="车辆编号" prop="vehicleCode" align="center" width="120" v-if="columns.vehicleCode.visible" />
          <el-table-column label="车辆类型" prop="vehicleType" align="center" width="110" v-if="columns.vehicleType.visible">
            <template #default="scope">
              <span v-if="scope.row.vehicleType" class="badge blue"><span class="dot"></span>{{ dictLabel(vehicleTypeOptions, scope.row.vehicleType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="品牌型号" prop="vehicleModel" align="center" min-width="130" show-overflow-tooltip v-if="columns.vehicleModel.visible" />
          <el-table-column label="使用用途" prop="vehicleUse" align="center" min-width="120" show-overflow-tooltip v-if="columns.vehicleUse.visible" />
          <el-table-column label="座位数" prop="seatCount" align="center" width="80" v-if="columns.seatCount.visible" />
          <el-table-column label="所属部门" prop="ownerDept" align="center" min-width="130" show-overflow-tooltip v-if="columns.ownerDept.visible" />
          <el-table-column label="默认驾驶员" prop="driverName" align="center" width="100" v-if="columns.driverName.visible" />
          <el-table-column label="驾驶员电话" prop="driverPhone" align="center" width="120" v-if="columns.driverPhone.visible" />
          <el-table-column label="年检到期" prop="inspectExpire" align="center" width="110" v-if="columns.inspectExpire.visible" />
          <el-table-column label="保险到期" prop="insuranceExpire" align="center" width="110" v-if="columns.insuranceExpire.visible" />
          <el-table-column label="状态" prop="status" align="center" width="100" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.status)"><span class="dot"></span>{{ dictLabel(vehicleStatusOptions, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['oa:vehicle:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['oa:vehicle:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['oa:vehicle:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/修改 -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 17h14l-1.5-5.5A2 2 0 0 0 15.6 10H8.4a2 2 0 0 0-1.9 1.5L5 17z"/><circle cx="7.5" cy="19.5" r="1.5"/><circle cx="16.5" cy="19.5" r="1.5"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub">{{ form.plateNo ? '车牌号：' + form.plateNo : '维护车辆基本信息、使用信息与证照信息' }}</div>
          </div>
        </div>
      </template>
      <el-form ref="vehicleRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 17h14l-1.5-5.5A2 2 0 0 0 15.6 10H8.4a2 2 0 0 0-1.9 1.5L5 17z"/><circle cx="7.5" cy="19.5" r="1.5"/><circle cx="16.5" cy="19.5" r="1.5"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="车牌号" prop="plateNo"><el-input v-model="form.plateNo" placeholder="请输入车牌号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="车辆编号" prop="vehicleCode"><el-input v-model="form.vehicleCode" placeholder="请输入车辆编号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="车辆类型" prop="vehicleType">
                    <el-select v-model="form.vehicleType" placeholder="请选择车辆类型" style="width: 100%">
                      <el-option v-for="d in vehicleTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                      <el-option v-for="d in vehicleStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="品牌型号" prop="vehicleModel"><el-input v-model="form.vehicleModel" placeholder="请输入品牌型号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="使用用途" prop="vehicleUse"><el-input v-model="form.vehicleUse" placeholder="请输入使用用途" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="座位数" prop="seatCount"><el-input-number v-model="form.seatCount" :min="1" :max="100" controls-position="right" style="width: 100%" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属部门" prop="ownerDept"><el-input v-model="form.ownerDept" placeholder="请输入所属部门" /></el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>驾驶员与证照</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="默认驾驶员" prop="driverName">
                    <el-input v-model="form.driverName" placeholder="选择人员或直接输入驾驶员姓名">
                      <template #append><el-button icon="Search" @click="openDriverPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="驾驶员电话" prop="driverPhone"><el-input v-model="form.driverPhone" placeholder="请输入驾驶员电话" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="行驶证号" prop="licenseNo"><el-input v-model="form.licenseNo" placeholder="请输入行驶证号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="购置日期" prop="purchaseDate">
                    <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择购置日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="年检到期日" prop="inspectExpire">
                    <el-date-picker v-model="form.inspectExpire" type="date" value-format="YYYY-MM-DD" placeholder="请选择年检到期日" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="保险到期日" prop="insuranceExpire">
                    <el-date-picker v-model="form.insuranceExpire" type="date" value-format="YYYY-MM-DD" placeholder="请选择保险到期日" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>附件与备注</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-form-item label="附件" prop="attachment">
                <file-upload v-model="form.attachment" :limit="3" :fileSize="50" />
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 17h14l-1.5-5.5A2 2 0 0 0 15.6 10H8.4a2 2 0 0 0-1.9 1.5L5 17z"/><circle cx="7.5" cy="19.5" r="1.5"/><circle cx="16.5" cy="19.5" r="1.5"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">车辆详情</div>
            <div class="rd-detail-header-sub">{{ viewData.plateNo || '' }}{{ viewData.vehicleCode ? ' · ' + viewData.vehicleCode : '' }}</div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 17h14l-1.5-5.5A2 2 0 0 0 15.6 10H8.4a2 2 0 0 0-1.9 1.5L5 17z"/><circle cx="7.5" cy="19.5" r="1.5"/><circle cx="16.5" cy="19.5" r="1.5"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">车牌号</span><div class="rd-value">{{ viewData.plateNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">车辆编号</span><div class="rd-value">{{ viewData.vehicleCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">车辆类型</span><div class="rd-value"><dict-tag :options="vehicleTypeOptions" :value="viewData.vehicleType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="vehicleStatusOptions" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">品牌型号</span><div class="rd-value">{{ viewData.vehicleModel || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">使用用途</span><div class="rd-value">{{ viewData.vehicleUse || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">座位数</span><div class="rd-value">{{ viewData.seatCount || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewData.ownerDept || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">默认驾驶员</span><div class="rd-value">{{ viewData.driverName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">驾驶员电话</span><div class="rd-value">{{ viewData.driverPhone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">行驶证号</span><div class="rd-value">{{ viewData.licenseNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">购置日期</span><div class="rd-value">{{ viewData.purchaseDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">年检到期日</span><div class="rd-value">{{ viewData.inspectExpire || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">保险到期日</span><div class="rd-value">{{ viewData.insuranceExpire || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联流程</span><div class="rd-value">{{ viewData.flowInstanceId || '未关联' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.attachment">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>附件</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v1">
            <div class="rd-file-links">
              <a class="rd-file-link" v-for="(url, idx) in String(viewData.attachment).split(',')" :key="idx" :href="url" target="_blank" rel="noopener">
                <span class="rd-file-name">{{ url }}</span>
              </a>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 人员选择 -->
    <user-picker ref="driverPickerRef" title="选择驾驶员" @confirm="onDriverPicked" />

    <!-- 导入 -->
    <ExcelImportDialog
      ref="importRef"
      title="车辆台账导入"
      action="/oa/vehicle/importData"
      template-action="/oa/vehicle/importTemplate"
      template-file-name="车辆台账导入模板.xlsx"
      update-support-label="如果已存在相同车牌号，则更新该车辆数据"
      :column-labels="{ vehicleCode: '车辆编号', plateNo: '车牌号', vehicleType: '车辆类型', vehicleModel: '品牌型号', vehicleUse: '使用用途', seatCount: '座位数', ownerDept: '所属部门', driverName: '默认驾驶员', driverPhone: '驾驶员电话', status: '状态', result: '导入结果', errorReason: '失败原因' }"
      :tips="['车牌号为必填项，且用于识别重复数据', '车辆类型、状态请填写字典值（如：车辆类型 1=轿车，状态 0=可用）', '勾选“更新已存在的数据”后，相同车牌号的记录将被覆盖更新']"
      @success="getList"
    />
  </div>
</template>

<script setup name="OaVehicle">
import { listOaVehicle, getOaVehicle, addOaVehicle, updateOaVehicle, delOaVehicle } from '@/api/oa/vehicle'
import { useDetailCard } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import UserPicker from '@/components/UserPicker/index.vue'

const { proxy } = getCurrentInstance()
const { collapsedCards, toggleCard } = useDetailCard(['e0', 'e1', 'e2', 'v0', 'v1'])
const { oa_vehicle_type: vehicleTypeOptions } = proxy.useDict('oa_vehicle_type')
const { oa_vehicle_status: vehicleStatusOptions } = proxy.useDict('oa_vehicle_status')

const importRef = ref()
const driverPickerRef = ref()
const vehicleList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const title = ref('')
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const inspectRange = ref([])
const insuranceRange = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 })

const columns = ref({
  plateNo: { label: '车牌号', visible: true },
  vehicleCode: { label: '车辆编号', visible: true },
  vehicleType: { label: '车辆类型', visible: true },
  vehicleModel: { label: '品牌型号', visible: true },
  vehicleUse: { label: '使用用途', visible: false },
  seatCount: { label: '座位数', visible: true },
  ownerDept: { label: '所属部门', visible: true },
  driverName: { label: '默认驾驶员', visible: true },
  driverPhone: { label: '驾驶员电话', visible: false },
  inspectExpire: { label: '年检到期', visible: true },
  insuranceExpire: { label: '保险到期', visible: true },
  status: { label: '状态', visible: true }
})

const statusTabList = computed(() => vehicleStatusOptions.value)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1, pageSize: 10,
    plateNo: undefined, vehicleCode: undefined, vehicleType: undefined, vehicleUse: undefined,
    ownerDept: undefined, driverName: undefined, vehicleModel: undefined,
    status: undefined, params: {}
  },
  rules: {
    plateNo: [{ required: true, message: '车牌号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.plateNo) count++
  if (queryParams.value.vehicleCode) count++
  if (queryParams.value.vehicleType) count++
  if (queryParams.value.vehicleUse) count++
  if (queryParams.value.ownerDept) count++
  if (queryParams.value.driverName) count++
  if (queryParams.value.vehicleModel) count++
  if (queryParams.value.status) count++
  if (inspectRange.value && inspectRange.value.length === 2) count++
  if (insuranceRange.value && insuranceRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listOaVehicle(queryParams.value).then(response => {
    vehicleList.value = response.rows
    total.value = response.total
    loading.value = false
    loadStatusCounts()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, inspectRange.value, 'InspectExpire')
  proxy.addDateRange(queryParams.value, insuranceRange.value, 'InsuranceExpire')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1, pageSize: queryParams.value.pageSize,
    plateNo: undefined, vehicleCode: undefined, vehicleType: undefined, vehicleUse: undefined,
    ownerDept: undefined, driverName: undefined, vehicleModel: undefined,
    status: undefined, params: {}
  }
  inspectRange.value = []
  insuranceRange.value = []
  activeStatusTab.value = 'all'
  getList()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.status = status === 'all' ? undefined : status
  handleQuery()
}

function loadStatusCounts() {
  const query = { ...queryParams.value, pageNum: 1, pageSize: 9999, status: undefined, params: { ...queryParams.value.params } }
  listOaVehicle(query).then(res => {
    const counts = { all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 }
    const rows = res.rows || []
    rows.forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    counts.all = rows.length
    statusCounts.value = counts
  }).catch(() => {})
}

function statusTabClass(value) { return { '0': 'tab-green', '1': 'tab-blue', '2': 'tab-amber', '4': 'tab-red' }[value] || '' }
function statusBadgeClass(value) { return { '0': 'green', '1': 'blue', '2': 'amber', '3': 'gray', '4': 'red' }[value] || 'gray' }
function dictLabel(options, v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (options || []).find(o => String(o.value) === String(v))
  return d ? d.label : String(v)
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.vehicleId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    vehicleId: undefined, vehicleCode: undefined, plateNo: undefined, vehicleType: undefined,
    vehicleModel: undefined, vehicleUse: undefined, seatCount: undefined, ownerDept: undefined,
    driverName: undefined, driverPhone: undefined, licenseNo: undefined, purchaseDate: undefined,
    inspectExpire: undefined, insuranceExpire: undefined, status: '0', attachment: undefined, remark: undefined
  }
  proxy.resetForm('vehicleRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增车辆'
}

function handleUpdate(row) {
  reset()
  const vehicleId = row.vehicleId || ids.value[0]
  getOaVehicle(vehicleId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改车辆'
  })
}

function handleView(row) {
  getOaVehicle(row.vehicleId).then(response => {
    viewData.value = response.data || {}
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['vehicleRef'].validate(valid => {
    if (!valid) return
    if (form.value.vehicleId) {
      updateOaVehicle(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addOaVehicle(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const vehicleIds = row.vehicleId ? [row.vehicleId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的车辆记录？').then(() => {
    return delOaVehicle(vehicleIds.join(','))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 打开驾驶员选择弹框 */
function openDriverPicker() {
  driverPickerRef.value.open()
}

/** 选择驾驶员后自动带出联系电话 */
function onDriverPicked(user) {
  form.value.driverName = user.nickName
  form.value.driverPhone = user.phonenumber
}

function handleImport() {
  importRef.value.open()
}

function handleExport() {
  const params = { ...queryParams.value }
  proxy.addDateRange(params, inspectRange.value, 'InspectExpire')
  proxy.addDateRange(params, insuranceRange.value, 'InsuranceExpire')
  proxy.download('oa/vehicle/export', params, `车辆台账_${new Date().getTime()}.xlsx`)
}

getList()
</script>
