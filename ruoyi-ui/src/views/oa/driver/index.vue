<template>
  <div class="app-container oa-list-page oa-driver-page">
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
          <label>司机姓名</label>
          <div class="control">
            <el-input v-model="queryParams.driverName" placeholder="请输入司机姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>手机号</label>
          <div class="control">
            <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in driverStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>准驾车型</label>
          <div class="control">
            <el-input v-model="queryParams.licenseType" placeholder="请输入准驾车型" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>驾驶证号</label>
          <div class="control">
            <el-input v-model="queryParams.licenseNo" placeholder="请输入驾驶证号" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>所属部门</label>
          <div class="control">
            <el-input v-model="queryParams.ownerDept" placeholder="请输入所属部门" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>驾驶证到期</label>
          <div class="control">
            <el-date-picker v-model="licenseRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>入职日期</label>
          <div class="control">
            <el-date-picker v-model="entryRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><QuestionFilled /></el-icon><span>业务操作说明</span>
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['oa:driver:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['oa:driver:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['oa:driver:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['oa:driver:export']">导出</el-button>
          <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['oa:driver:import']">导入</el-button>
          <el-button type="primary" plain icon="Calendar" @click="calendarRef.open()">出车日历</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="oa_driver_index"></right-toolbar>
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="driverList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" width="55" align="center" />
          <el-table-column label="司机姓名" prop="driverName" key="driverName" align="center" :width="colWidth('driverName', 110)" resizable v-if="columns.driverName.visible" />
          <el-table-column label="手机号" prop="phone" key="phone" align="center" :width="colWidth('phone', 120)" resizable v-if="columns.phone.visible" />
          <el-table-column label="准驾车型" prop="licenseType" key="licenseType" align="center" :width="colWidth('licenseType', 100)" resizable v-if="columns.licenseType.visible" />
          <el-table-column label="驾驶证号" prop="licenseNo" key="licenseNo" align="center" :min-width="colWidth('licenseNo', 160)" show-overflow-tooltip resizable v-if="columns.licenseNo.visible" />
          <el-table-column label="驾驶证有效期至" prop="licenseExpire" key="licenseExpire" align="center" :width="colWidth('licenseExpire', 130)" resizable v-if="columns.licenseExpire.visible" />
          <el-table-column label="入职日期" prop="entryDate" key="entryDate" align="center" :width="colWidth('entryDate', 110)" resizable v-if="columns.entryDate.visible" />
          <el-table-column label="所属部门" prop="ownerDept" key="ownerDept" align="center" :min-width="colWidth('ownerDept', 130)" show-overflow-tooltip resizable v-if="columns.ownerDept.visible" />
          <el-table-column label="驾龄(年)" prop="drivingYears" key="drivingYears" align="center" :width="colWidth('drivingYears', 90)" resizable v-if="columns.drivingYears.visible" />
          <el-table-column label="状态" prop="status" key="status" align="center" :width="colWidth('status', 100)" resizable v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.status)"><span class="dot"></span>{{ dictLabel(driverStatusOptions, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="关联流程" prop="flowLink" key="flowLink" align="center" :width="colWidth('flowLink', 200)" resizable v-if="columns.flowLink.visible">
            <template #default="scope">
              <template v-if="scope.row.flowInstanceId">
                <el-link type="primary" :underline="false" @click="openFlowDetail(scope.row)">{{ scope.row.flowTitle || '查看流程' }}</el-link>
                <el-tag size="small" :type="scope.row.flowRunning === '1' ? 'warning' : 'info'" style="margin-left: 4px">{{ scope.row.flowRunning === '1' ? '进行中' : '已结束' }}</el-tag>
              </template>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 190)" align="center" fixed="right" class-name="col-action" resizable>
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['oa:driver:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['oa:driver:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['oa:driver:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 出车日历 -->
    <oa-resource-calendar ref="calendarRef" type="driver" title="司机出车日历" />

    <!-- 新增/修改 -->
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub">{{ form.driverName ? '司机：' + form.driverName : '维护司机基本信息、驾驶资质与联系方式' }}</div>
          </div>
        </div>
      </template>
      <el-form ref="driverRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="司机姓名" prop="driverName">
                    <el-input v-model="form.driverName" :readonly="!!form.userId" placeholder="选择人员或直接输入司机姓名" @input="onDriverNameInput">
                      <template #append><el-button icon="Search" @click.stop="openDriverPicker" /></template>
                      <template #suffix>
                        <el-icon v-if="form.driverName" class="vehicle-clear-icon" @click.stop="clearDriver"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证号" prop="idCard"><el-input v-model="form.idCard" placeholder="请输入身份证号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属部门" prop="ownerDept">
                    <el-input v-model="form.ownerDept" :disabled="!!form.userId" :placeholder="form.userId ? '选择人员后自动回填' : '请输入所属部门'" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                      <el-option v-for="d in driverStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="9" cy="11" r="2"/><path d="M15 9h3M15 13h3M7 17h10"/></svg></span>驾驶资质</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="驾驶证号" prop="licenseNo"><el-input v-model="form.licenseNo" placeholder="请输入驾驶证号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="准驾车型" prop="licenseType"><el-input v-model="form.licenseType" placeholder="如 A1、B2、C1" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="驾驶证有效期至" prop="licenseExpire">
                    <el-date-picker v-model="form.licenseExpire" type="date" value-format="YYYY-MM-DD" placeholder="请选择有效期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="入职日期" prop="entryDate">
                    <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择入职日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="驾龄(年)" prop="drivingYears"><el-input-number v-model="form.drivingYears" :min="0" :max="60" controls-position="right" style="width: 100%" /></el-form-item>
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
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">司机详情</div>
            <div class="rd-detail-header-sub">{{ viewData.driverName || '' }}</div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">司机姓名</span><div class="rd-value">{{ viewData.driverName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ viewData.phone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">身份证号</span><div class="rd-value">{{ viewData.idCard || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewData.ownerDept || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="driverStatusOptions" :value="viewData.status" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="9" cy="11" r="2"/><path d="M15 9h3M15 13h3M7 17h10"/></svg></span>驾驶资质</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">驾驶证号</span><div class="rd-value">{{ viewData.licenseNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">准驾车型</span><div class="rd-value">{{ viewData.licenseType || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">驾驶证有效期至</span><div class="rd-value">{{ viewData.licenseExpire || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">入职日期</span><div class="rd-value">{{ viewData.entryDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">驾龄(年)</span><div class="rd-value">{{ viewData.drivingYears || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>附件与备注</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">关联流程</span><div class="rd-value">{{ viewData.flowTitle || viewData.flowInstanceId || '未关联' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
            <div class="rd-file-links" v-if="viewData.attachment">
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

    <!-- 业务操作说明 -->
    <el-dialog v-model="showStatusHelp" title="司机台账业务操作说明" width="780px" append-to-body>
      <div class="oa-status-help-content">
        <h4>一、司机台账说明</h4>
        <div class="help-card help-card-primary">
          <div class="help-card-title">司机资源统一管理</div>
          <div class="help-card-body">
            司机台账统一维护驾驶员基本信息、驾照资质与出车状态。公车使用申请会按“指派驾驶员”自动与台账联动，无需手工修改出车状态。
          </div>
        </div>

        <h4>二、状态说明</h4>
        <div class="help-status-list">
          <div class="help-status-item">
            <span class="badge green"><span class="dot"></span>在岗</span>
            <p>司机正常在岗，可被公车使用申请选择。</p>
          </div>
          <div class="help-status-item">
            <span class="badge blue"><span class="dot"></span>出车中</span>
            <p>公车使用申请提交后自动置为出车中；流程办结或终止后自动恢复为在岗。</p>
          </div>
          <div class="help-status-item">
            <span class="badge amber"><span class="dot"></span>休假</span>
            <p>司机休假期间不可被用车申请选择，由管理员手工维护。</p>
          </div>
          <div class="help-status-item">
            <span class="badge red"><span class="dot"></span>停用</span>
            <p>司机已停用，不再参与出车调度。</p>
          </div>
        </div>

        <h4>三、出车流程与状态联动</h4>
        <div class="help-flow">
          <span class="help-flow-node">提交用车申请</span>
          <span class="help-flow-arrow">→</span>
          <span class="help-flow-node">部门负责人审批</span>
          <span class="help-flow-arrow">→</span>
          <span class="help-flow-node">综合管理部调度派车</span>
          <span class="help-flow-arrow">→</span>
          <span class="help-flow-node">出车登记 / 回车登记</span>
          <span class="help-flow-arrow">→</span>
          <span class="help-flow-node is-end">流程办结<small>自动释放</small></span>
        </div>
        <ul class="help-tips" style="margin-top:12px">
          <li>申请提交并指派司机后，司机自动变为“出车中”。</li>
          <li>流程办结或终止后，司机自动恢复为“在岗”。</li>
        </ul>

        <h4>四、常见操作</h4>
        <ul class="help-tips">
          <li><strong>新增 / 修改：</strong>维护司机姓名、手机号、驾驶证号、准驾车型、驾照有效期等信息。</li>
          <li><strong>出车日历：</strong>按司机查看出车时段，点击司机姓名可筛选该司机的出车明细。</li>
          <li><strong>详情：</strong>查看完整台账信息及当前关联流程。</li>
          <li><strong>导入 / 导出：</strong>批量维护司机台账。</li>
          <li><strong>删除：</strong>仅删除台账数据，不会删除历史流程记录。</li>
        </ul>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 人员选择 -->
    <user-picker ref="driverPickerRef" title="选择司机" @confirm="onDriverPicked" />

    <!-- 导入 -->
    <ExcelImportDialog
      ref="importRef"
      title="司机台账导入"
      action="/oa/driver/importData"
      template-action="/oa/driver/importTemplate"
      template-file-name="司机台账导入模板.xlsx"
      update-support-label="如果已存在相同手机号，则更新该司机数据"
      :column-labels="{ driverName: '司机姓名', phone: '手机号', licenseType: '准驾车型', licenseNo: '驾驶证号', licenseExpire: '驾驶证有效期至', entryDate: '入职日期', ownerDept: '所属部门', drivingYears: '驾龄', status: '状态', result: '导入结果', errorReason: '失败原因' }"
      :tips="['司机姓名为必填项，手机号用于识别重复数据', '状态请填写字典值（如：状态 0=在岗）', '勾选“更新已存在的数据”后，相同手机号的记录将被覆盖更新']"
      @success="getList"
    />
  </div>
</template>

<script setup name="OaDriver">
import { listOaDriver, getOaDriver, addOaDriver, updateOaDriver, delOaDriver } from '@/api/oa/driver'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import UserPicker from '@/components/UserPicker/index.vue'
import OaResourceCalendar from '@/components/OaResourceCalendar/index.vue'
import { listHrEmployee } from '@/api/hr/hrEmployee'
import { QuestionFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()
const { collapsedCards, toggleCard } = useDetailCard(['e0', 'e1', 'e2', 'v0', 'v1', 'v2'])
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('oa_driver_index')
const { oa_driver_status: driverStatusOptions } = proxy.useDict('oa_driver_status')

const importRef = ref()
const calendarRef = ref()
const driverPickerRef = ref()
const driverList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const title = ref('')
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const licenseRange = ref([])
const entryRange = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })

const columns = ref({
  driverName: { label: '司机姓名', visible: true },
  phone: { label: '手机号', visible: true },
  licenseType: { label: '准驾车型', visible: true },
  licenseNo: { label: '驾驶证号', visible: false },
  licenseExpire: { label: '驾驶证有效期至', visible: true },
  entryDate: { label: '入职日期', visible: false },
  ownerDept: { label: '所属部门', visible: true },
  drivingYears: { label: '驾龄', visible: true },
  status: { label: '状态', visible: true },
  flowLink: { label: '关联流程', visible: true }
})

const statusTabList = computed(() => driverStatusOptions.value)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1, pageSize: 10,
    driverName: undefined, phone: undefined,
    licenseType: undefined, licenseNo: undefined, ownerDept: undefined, status: undefined, params: {}
  },
  rules: {
    driverName: [{ required: true, message: '司机姓名不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.driverName) count++
  if (queryParams.value.phone) count++
  if (queryParams.value.licenseType) count++
  if (queryParams.value.licenseNo) count++
  if (queryParams.value.ownerDept) count++
  if (queryParams.value.status) count++
  if (licenseRange.value && licenseRange.value.length === 2) count++
  if (entryRange.value && entryRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listOaDriver(queryParams.value).then(response => {
    driverList.value = response.rows
    total.value = response.total
    loading.value = false
    loadStatusCounts()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, licenseRange.value, 'LicenseExpire')
  proxy.addDateRange(queryParams.value, entryRange.value, 'EntryDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1, pageSize: queryParams.value.pageSize,
    driverName: undefined, phone: undefined,
    licenseType: undefined, licenseNo: undefined, ownerDept: undefined, status: undefined, params: {}
  }
  licenseRange.value = []
  entryRange.value = []
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
  listOaDriver(query).then(res => {
    const counts = { all: 0, '0': 0, '1': 0, '2': 0, '3': 0 }
    const rows = res.rows || []
    rows.forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    counts.all = rows.length
    statusCounts.value = counts
  }).catch(() => {})
}

function statusTabClass(value) { return { '0': 'tab-green', '1': 'tab-amber', '2': 'tab-red', '3': 'tab-blue' }[value] || '' }
function statusBadgeClass(value) { return { '0': 'green', '1': 'amber', '2': 'red', '3': 'blue' }[value] || 'gray' }
function dictLabel(options, v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (options || []).find(o => String(o.value) === String(v))
  return d ? d.label : String(v)
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.driverId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    driverId: undefined, userId: undefined, driverName: undefined, phone: undefined,
    idCard: undefined, licenseNo: undefined, licenseType: undefined, licenseExpire: undefined,
    entryDate: undefined, ownerDept: undefined, drivingYears: undefined,
    status: '0', attachment: undefined, remark: undefined
  }
  proxy.resetForm('driverRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增司机'
}

function handleUpdate(row) {
  reset()
  const driverId = row.driverId || ids.value[0]
  getOaDriver(driverId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改司机'
  })
}

function handleView(row) {
  getOaDriver(row.driverId).then(response => {
    viewData.value = response.data || {}
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['driverRef'].validate(valid => {
    if (!valid) return
    if (form.value.driverId) {
      updateOaDriver(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addOaDriver(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const driverIds = row.driverId ? [row.driverId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的司机记录？').then(() => {
    return delOaDriver(driverIds.join(','))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 打开司机选择弹框 */
function openDriverPicker() {
  driverPickerRef.value.open(form.value.userId)
}

/** 手动输入姓名（外部人员）：解除组织用户关联，并即时清除校验残留 */
function onDriverNameInput(val) {
  if (form.value.userId) {
    form.value.userId = undefined
    form.value.ownerDept = undefined
  }
  if (val) proxy.$refs['driverRef']?.clearValidate('driverName')
}

/** 选择人员后自动带出部门与电话，并回填身份证号 */
function onDriverPicked(user) {
  form.value.userId = user.userId
  form.value.driverName = user.nickName || user.nickname
  form.value.ownerDept = user.deptName
  form.value.phone = user.phonenumber
  // 选择后立即清除“司机姓名不能为空”的校验残留，避免提交时误报
  nextTick(() => {
    proxy.$refs['driverRef']?.validateField('driverName', () => {})
  })
  listHrEmployee({ userId: user.userId, pageNum: 1, pageSize: 1 }).then(res => {
    const emp = (res.rows || [])[0]
    if (emp && emp.idCard) form.value.idCard = emp.idCard
  }).catch(() => {})
}

/**
 * 清除司机姓名：
 * - 人员选择带入时，一并清除人员关联与自动带出的部门、电话、身份证；
 * - 手工输入时仅清空姓名，保留已手工填写的联系方式。
 */
function clearDriver() {
  const fromPicker = !!form.value.userId
  form.value.userId = undefined
  form.value.driverName = undefined
  if (fromPicker) {
    form.value.ownerDept = undefined
    form.value.phone = undefined
    form.value.idCard = undefined
  }
  proxy.$refs['driverRef']?.clearValidate('driverName')
}

function handleImport() {
  importRef.value.open()
}

function handleExport() {
  const params = { ...queryParams.value }
  proxy.addDateRange(params, licenseRange.value, 'LicenseExpire')
  proxy.addDateRange(params, entryRange.value, 'EntryDate')
  proxy.download('oa/driver/export', params, `司机台账_${new Date().getTime()}.xlsx`)
}

/** 查看关联流程：新标签页打开流程详情页（与流程任务列表同一跳转方式） */
function openFlowDetail(row) {
  const href = router.resolve({
    path: '/flow/detail/create',
    query: { instanceId: row.flowInstanceId || '', defName: row.flowTitle || '' }
  }).href
  window.open(href, '_blank')
}

getList()
</script>
