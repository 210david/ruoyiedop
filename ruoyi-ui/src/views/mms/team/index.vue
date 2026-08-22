<template>
  <div class="app-container mms-team-page">
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
          <label>班组编号</label>
          <div class="control">
            <el-input v-model="queryParams.teamCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>班组名称</label>
          <div class="control">
            <el-input v-model="queryParams.teamName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>班组长</label>
          <div class="control">
            <el-input v-model="queryParams.leader" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_team_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>产能单元</label>
          <div class="control">
            <el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建日期</label>
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:team:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:team:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:team:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:team:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_team_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="班组编号" prop="teamCode" key="teamCode" :width="colWidth('teamCode', 140)" resizable v-if="columns.teamCode.visible" />
          <el-table-column label="班组名称" prop="teamName" key="teamName" :width="colWidth('teamName', 140)" resizable v-if="columns.teamName.visible" />
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 140)" resizable show-overflow-tooltip v-if="columns.resourceName.visible" />
          <el-table-column label="班组长" prop="leader" key="leader" :width="colWidth('leader', 100)" resizable v-if="columns.leader.visible" />
          <el-table-column label="联系电话" prop="phone" key="phone" :width="colWidth('phone', 130)" resizable v-if="columns.phone.visible" />
          <el-table-column label="班组人数" prop="memberCount" key="memberCount" :width="colWidth('memberCount', 100)" resizable align="center" v-if="columns.memberCount.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:team:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:team:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="班组编号" prop="teamCode">
                    <el-input v-model="form.teamCode" placeholder="保存时自动生成" readonly />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="班组名称" prop="teamName">
                    <el-input v-model="form.teamName" placeholder="请输入" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="产能单元" prop="resourceId" required>
                    <el-input v-model="form.resourceName" readonly placeholder="请选择产能单元" style="width: 100%" @click="openResourcePicker">
                      <template #append>
                        <el-button icon="Search" @click="openResourcePicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.resourceName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearResource"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status" required>
                    <el-select v-model="form.status" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in mms_team_status" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>人员信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="班组长" prop="leader" required>
                    <el-input v-model="form.leader" readonly placeholder="请选择班组长" style="width: 100%" @click="openLeaderPicker">
                      <template #append>
                        <el-button icon="Search" @click="openLeaderPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.leader" class="rd-form-tip" style="cursor:pointer" @click.stop="clearLeader"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="phone" required>
                    <el-input v-model="form.phone" placeholder="请输入" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="班组人数" prop="memberCount" required>
                    <el-input-number v-model="form.memberCount" :min="0" :step="1" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">班组详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.teamCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ viewData.teamCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">班组编号</span><div class="rd-value">{{ viewData.teamCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">班组名称</span><div class="rd-value">{{ viewData.teamName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_team_status" :value="viewData.status" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>人员信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">班组长</span><div class="rd-value">{{ viewData.leader || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.phone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">班组人数</span><div class="rd-value">{{ viewData.memberCount != null ? viewData.memberCount + ' 人' : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 人员选择器 ===== -->
    <user-picker ref="leaderPickerRef" title="选择班组长" @confirm="onLeaderPickerConfirm" />

    <!-- ===== 产能单元选择器弹窗 ===== -->
    <el-dialog v-model="resourcePickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">选择产能单元</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="resourcePickerQuery.resourceName" placeholder="产能单元名称" clearable size="small" style="width:200px" @keyup.enter="handleResourcePickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain icon="Search" size="small" @click="handleResourcePickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetResourcePickerQuery">重置</el-button>
      </div>
      <el-table ref="resourcePickerTableRef" v-loading="resourcePickerLoading" :data="resourcePickerList" highlight-current-row @row-click="onResourceRowClick" @row-dblclick="onResourceRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="resourcePickerSelectedId" :value="row.resourceId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="资源编码" prop="resourceCode" width="130" show-overflow-tooltip />
        <el-table-column label="产能单元" prop="resourceName" min-width="150" show-overflow-tooltip />
        <el-table-column label="产线" prop="lineName" width="100" show-overflow-tooltip />
        <el-table-column label="车间" prop="workshopName" width="100" show-overflow-tooltip />
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="resourcePickerQuery.pageNum" v-model:page-size="resourcePickerQuery.pageSize" :total="resourcePickerTotal" layout="total, prev, pager, next" small @current-change="getResourcePickerList" />
      </div>
      <div v-if="resourcePickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出产能单元</div>
      <template #footer><el-button @click="resourcePickerOpen = false">取 消</el-button><el-button type="primary" @click="handleResourcePickerConfirm" :disabled="!resourcePickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="班组管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、班组管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是班组管理？</div>
          <div class="highlight-card-body">
            <strong>班组管理（Team Management）</strong>是生产管控中定义工厂生产班组的主数据。班组是执行生产任务的基本组织单元，关联产能单元，记录班组长、人数等信息，是派工管理和报工管理中班组维度数据的核心来源。<br/><br/>
            班组管理遵循 <strong>MES 生产组织管理规范</strong>，通过班组与产能单元的绑定，实现生产任务到班组的精确分配，确保派工单中的班组字段可追溯、可管控。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="success">已启用</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">停用</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="danger">已停用</el-tag></div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="已启用">班组新建后默认为已启用，可被派工单引用。可停用</el-descriptions-item>
          <el-descriptions-item label="已停用">班组已停用，不可被新派工单引用。可重新启用</el-descriptions-item>
        </el-descriptions>
        <h4>四、重点业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心规则</div>
          <div class="highlight-card-body">
            <p>• <strong>班组编号：</strong>保存时自动生成，无需手动输入</p>
            <p>• <strong>班组名称：</strong>自定义命名，如"装配甲班""数控一组"等，不可重复</p>
            <p>• <strong>产能单元：</strong>班组可关联一个产能单元（设备/产线），实现班组与产能的绑定</p>
            <p>• <strong>班组长：</strong>从人员选择弹窗中选择班组长，选择后自动带出联系电话</p>
            <p>• <strong>状态控制：</strong>已启用状态的班组方可被派工单引用；已停用班组不可被新派工单引用</p>
          </div>
        </div>
        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>创建班组：</strong>点击「新增」创建班组，填写班组名称、关联产能单元、班组长等信息，保存后默认为已启用</el-timeline-item>
          <el-timeline-item type="info" :hollow="true"><strong>修改班组：</strong>可修改班组的名称、产能单元、班组长等信息</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>停用班组：</strong>不再使用的班组可停用，停用后不可被新派工单引用</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>派工引用：</strong>派工管理页面中，班组字段从已启用班组列表中选择</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Team">
import { listTeam, getTeam, addTeam, updateTeam, delTeam } from "@/api/mms/team";
import { listResource } from "@/api/mms/resource";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import UserPicker from '@/components/UserPicker/index.vue'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_team_status } = proxy.useDict("mms_team_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_team_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])

const dataList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusCounts = ref({});
const showStatusHelp = ref(false);

// ===== 班组长人员选择 =====
const leaderPickerRef = ref()
function openLeaderPicker() { leaderPickerRef.value.open() }
function clearLeader() { form.value.leader = undefined; form.value.phone = undefined; }
function onLeaderPickerConfirm(user) {
  form.value.leader = user.nickName
  form.value.phone = user.phonenumber || ''
}

// 状态标签列表
const statusTabList = computed(() => {
  if (!mms_team_status.value) return [];
  return mms_team_status.value.map(d => ({ label: d.label, value: d.value }));
});

// 列显隐配置
const defaultColumns = {
  teamCode: { label: '班组编号', visible: true },
  teamName: { label: '班组名称', visible: true },
  resourceName: { label: '产能单元', visible: true },
  leader: { label: '班组长', visible: true },
  phone: { label: '联系电话', visible: true },
  memberCount: { label: '班组人数', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_team_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

// 当前已选筛选条件数
const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.value.teamCode) count++;
  if (queryParams.value.teamName) count++;
  if (queryParams.value.leader) count++;
  if (queryParams.value.status) count++;
  if (queryParams.value.resourceName) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, teamCode: undefined, teamName: undefined, leader: undefined, resourceName: undefined, status: undefined, params: {} },
  rules: {
    teamName: [
      { required: true, message: "请输入班组名称", trigger: "blur" },
      { min: 2, max: 50, message: "班组名称长度必须在2到50个字符", trigger: "blur" }
    ],
    resourceId: [{ required: true, message: "请选择产能单元", trigger: "change" }],
    leader: [{ required: true, message: "请选择班组长", trigger: "change" }],
    status: [{ required: true, message: "请选择状态", trigger: "change" }],
    phone: [
      { required: true, message: "请输入联系电话", trigger: "blur" },
      { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }
    ],
    memberCount: [{ required: true, message: "请输入班组人数", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

// ===== 产能单元选择器 =====
const resourcePickerOpen = ref(false)
const resourcePickerLoading = ref(false)
const resourcePickerList = ref([])
const resourcePickerTotal = ref(0)
const resourcePickerSelectedId = ref(null)
const resourcePickerSelectedRow = ref(null)
const resourcePickerTableRef = ref()
const resourcePickerQuery = reactive({ pageNum: 1, pageSize: 10, resourceName: undefined, status: '0' })

function openResourcePicker() {
  resourcePickerOpen.value = true
  resourcePickerSelectedId.value = null
  resourcePickerSelectedRow.value = null
  resourcePickerQuery.pageNum = 1
  resourcePickerQuery.resourceName = undefined
  getResourcePickerList()
}
function getResourcePickerList() {
  resourcePickerLoading.value = true
  listResource(resourcePickerQuery).then(res => {
    resourcePickerList.value = res.rows
    resourcePickerTotal.value = res.total
    resourcePickerLoading.value = false
  }).catch(() => { resourcePickerLoading.value = false })
}
function handleResourcePickerQuery() { resourcePickerQuery.pageNum = 1; getResourcePickerList() }
function resetResourcePickerQuery() { resourcePickerQuery.resourceName = undefined; handleResourcePickerQuery() }
function onResourceRowClick(row) { resourcePickerSelectedId.value = row.resourceId; resourcePickerSelectedRow.value = row }
function onResourceRowDblClick(row) { onResourceRowClick(row); handleResourcePickerConfirm() }
function handleResourcePickerConfirm() {
  if (!resourcePickerSelectedId.value) { proxy.$modal.msgWarning('请先选择产能单元'); return }
  form.value.resourceId = resourcePickerSelectedRow.value.resourceId
  form.value.resourceName = resourcePickerSelectedRow.value.resourceName
  resourcePickerOpen.value = false
}
function clearResource() {
  form.value.resourceId = undefined
  form.value.resourceName = undefined
}

function getList() {
  loading.value = true;
  listTeam(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    loadStatusCounts();
  });
}

function loadStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 };
  if (queryParams.value.teamCode) baseQuery.teamCode = queryParams.value.teamCode;
  if (queryParams.value.teamName) baseQuery.teamName = queryParams.value.teamName;
  if (queryParams.value.leader) baseQuery.leader = queryParams.value.leader;
  if (queryParams.value.resourceName) baseQuery.resourceName = queryParams.value.resourceName;
  listTeam(proxy.addDateRange(baseQuery, dateRange.value)).then(res => {
    const counts = { all: res.total };
    if (mms_team_status.value) {
      mms_team_status.value.forEach(d => { counts[d.value] = 0; });
      (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; });
    }
    statusCounts.value = counts;
  }).catch(() => {});
}

function handleQuery() {
  showAdvanced.value = false;
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value.teamCode = undefined;
  queryParams.value.teamName = undefined;
  queryParams.value.leader = undefined;
  queryParams.value.resourceName = undefined;
  queryParams.value.status = undefined;
  dateRange.value = [];
  queryParams.value.params = {};
  activeStatusTab.value = 'all';
  handleQuery();
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status;
  queryParams.value.status = status === "all" ? undefined : status;
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.teamId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}


function reset() {
  form.value = {
    teamCode: undefined,
    teamName: undefined,
    resourceId: undefined,
    resourceName: undefined,
    leader: undefined,
    phone: undefined,
    memberCount: undefined,
    status: '0',
    remark: undefined,
  };
  proxy.resetForm("formRef");
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增班组";
}

function handleUpdate(row) {
  reset();
  const id = row.teamId || ids.value[0];
  getTeam(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改班组";
  });
}
function handleView(row) {
  const id = row.teamId || ids.value[0];
  getTeam(id).then(response => {
    viewData.value = response.data;
    viewOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.teamId != null) {
        updateTeam(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addTeam(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function cancel() {
  open.value = false;
  reset();
}

function handleDelete(row) {
  const delIds = row.teamId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的班组？').then(function() {
    return delTeam(delIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleExport() {
  proxy.download("mms/team/export", { ...queryParams.value }, `team_${new Date().getTime()}.xlsx`);
}

// ===== 字典辅助函数 =====
function statusLabel(status) {
  if (status === null || status === undefined || status === '') return '—';
  if (!mms_team_status.value) return '—';
  const item = mms_team_status.value.find(d => d.value == status);
  return item ? item.label : '—';
}

function badgeClass(status) {
  const map = {
    '0': 'green',    // 已启用
    '1': 'gray'      // 已停用
  };
  return map[status] || 'gray';
}

function statusTabClass(value) {
  const map = {
    '0': 'tab-done',
    '1': 'tab-void'
  };
  return map[value] || '';
}

getList();
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-team-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
</style>