<template>
  <div class="app-container mk-list-page">
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
          <label>线索编号</label>
          <div class="control">
            <el-input v-model="queryParams.leadNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>企业名称</label>
          <div class="control">
            <el-input v-model="queryParams.companyName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>线索状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.leadStatus" placeholder="请选择" clearable>
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_lead_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>线索等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.leadGrade" placeholder="请选择" clearable>
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_lead_grade" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>线索来源</label>
          <div class="control is-select">
            <el-select v-model="queryParams.leadSource" placeholder="请选择" clearable>
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_customer_source" :key="d.value" :label="d.label" :value="d.value" />
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
      <!-- Status Tabs -->
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
          <el-icon><QuestionFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:lead:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['marketing:lead:add']">
            <el-icon><Upload /></el-icon> 导入
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:lead:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:lead:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchAssign" v-hasPermi="['marketing:lead:assign']">
            <el-icon><User /></el-icon> 批量分配
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchStatus" v-hasPermi="['marketing:lead:edit']">
            <el-icon><RefreshRight /></el-icon> 批量改状态
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:lead:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_lead_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="线索编号" prop="leadNo" key="leadNo" :width="colWidth('leadNo', 150)" resizable v-if="columns.leadNo.visible" />
          <el-table-column label="企业名称" prop="companyName" key="companyName" :width="colWidth('companyName', 200)" resizable show-overflow-tooltip v-if="columns.companyName.visible">
            <template #default="scope">
              <el-button link type="primary" @click="handleView(scope.row)">{{ scope.row.companyName }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="联系人" prop="contactName" key="contactName" :width="colWidth('contactName', 100)" resizable v-if="columns.contactName.visible" />
          <el-table-column label="手机号" prop="contactPhone" key="contactPhone" :width="colWidth('contactPhone', 130)" resizable v-if="columns.contactPhone.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.contactPhone }}</span></template>
          </el-table-column>
          <el-table-column label="线索来源" prop="leadSource" key="leadSource" :width="colWidth('leadSource', 100)" resizable align="center" v-if="columns.leadSource.visible">
            <template #default="scope">
              <span class="badge" :class="sourceBadgeClass(scope.row.leadSource)">
                <span class="dot"></span>{{ sourceLabel(scope.row.leadSource) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="等级" prop="leadGrade" key="leadGrade" :width="colWidth('leadGrade', 80)" resizable align="center" v-if="columns.leadGrade.visible">
            <template #default="scope">
              <span class="badge" :class="gradeBadgeClass(scope.row.leadGrade)">
                <span class="dot"></span>{{ gradeLabel(scope.row.leadGrade) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="评分" prop="leadScore" key="leadScore" :width="colWidth('leadScore', 80)" resizable align="center" v-if="columns.leadScore.visible" />
          <el-table-column label="状态" prop="leadStatus" key="leadStatus" :width="colWidth('leadStatus', 100)" resizable align="center" v-if="columns.leadStatus.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.leadStatus)"><span class="dot"></span>{{ statusLabel(scope.row.leadStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="负责人" prop="userName" key="userName" :width="colWidth('userName', 100)" resizable v-if="columns.userName.visible">
            <template #default="scope">
              <span v-if="scope.row.userName">{{ scope.row.userName }}</span>
              <el-tag v-else type="warning" size="small">公海</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最后跟进" prop="lastFollowTime" key="lastFollowTime" :width="colWidth('lastFollowTime', 160)" resizable sortable="custom" v-if="columns.lastFollowTime.visible" />
          <el-table-column label="操作" width="320" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
              <el-button link type="primary" icon="ChatDotRound" @click="handleFollowUp(scope.row)" v-if="scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'" v-hasPermi="['marketing:lead:edit']">跟进</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:lead:edit']">修改</el-button>
              <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click" v-hasPermi="['marketing:lead:edit']">
                <el-button link type="primary" icon="ArrowDown">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="convert" icon="Promotion" v-if="scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'" v-hasPermi="['marketing:lead:edit']">转化为客户</el-dropdown-item>
                    <el-dropdown-item command="assign" icon="User" v-if="scope.row.userId && scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'" v-hasPermi="['marketing:lead:assign']">转移分配</el-dropdown-item>
                    <el-dropdown-item command="release" icon="Coin" v-if="scope.row.userId && scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'">退回公海</el-dropdown-item>
                    <el-dropdown-item command="invalidate" icon="CircleClose" v-if="scope.row.leadStatus !== '4' && scope.row.leadStatus !== '5'" divided>标记无效</el-dropdown-item>
                    <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:lead:remove']" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="leadRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_lead')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></span>线索信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_lead }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_lead">
              <el-row>
                <el-col :span="12"><el-form-item label="线索编号" prop="leadNo"><el-input v-model="form.leadNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="企业名称" prop="companyName"><el-input v-model="form.companyName" placeholder="请输入企业名称" @blur="onCompanyBlur" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="联系人" prop="contactName"><el-input v-model="form.contactName" placeholder="请输入联系人姓名" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="手机号" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入手机号" @blur="onPhoneBlur" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="邮箱" prop="contactEmail"><el-input v-model="form.contactEmail" placeholder="请输入邮箱" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="职位" prop="position"><el-input v-model="form.position" placeholder="请输入职位" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_company')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></span>企业信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_company }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_company">
              <el-row>
                <el-col :span="12"><el-form-item label="所属行业" prop="industry">
                  <el-select v-model="form.industry" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_industry" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="企业规模" prop="companySize">
                  <el-select v-model="form.companySize" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_company_size" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
                <el-col :span="24"><el-form-item label="详细地址" prop="address"><el-input v-model="form.address" placeholder="请输入详细地址" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_requirement')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>需求与评分</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_requirement }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_requirement">
              <el-row>
                <el-col :span="12"><el-form-item label="线索来源" prop="leadSource">
                  <el-select v-model="form.leadSource" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_customer_source" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="线索评分" prop="leadScore"><el-input-number v-model="form.leadScore" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="线索等级" prop="leadGrade">
                  <el-select v-model="form.leadGrade" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_lead_grade" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="线索状态" prop="leadStatus">
                  <el-select v-model="form.leadStatus" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_lead_status" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item></el-col>
                <el-col :span="24"><el-form-item label="需求描述" prop="requirementDesc"><el-input v-model="form.requirementDesc" type="textarea" :rows="3" placeholder="请输入需求描述" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_owner')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>负责信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_owner }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_owner">
              <el-row>
                <el-col :span="12"><el-form-item label="负责人" prop="userId">
                  <el-input v-model="form.userName" readonly placeholder="请选择负责人" style="width: 100%" @click="openUserPicker">
                    <template #append>
                      <el-button icon="Search" @click="openUserPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.userName" class="clear-icon" @click.stop="clearUser"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                  <el-input v-model="form.deptName" readonly placeholder="请选择部门" style="width: 100%" @click="openDeptPicker">
                    <template #append>
                      <el-button icon="Search" @click="openDeptPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.deptName" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="是否公海" prop="isPublic">
                  <el-radio-group v-model="form.isPublic"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_other')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情弹窗 - Tab页 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog" @open="loadLeadRelations">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg>
          </div>
          <span class="rd-detail-header-title">线索详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.leadNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.leadNo }}</span>
            <dict-tag :options="marketing_lead_status" :value="viewForm.leadStatus" />
            <el-tag v-if="viewForm.isPublic === '1'" type="warning" size="small">公海</el-tag>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewBasic')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>线索信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewBasic">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">线索编号</span><div class="rd-value">{{ viewForm.leadNo }}</div></div>
                  <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ viewForm.companyName }}</div></div>
                  <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ viewForm.contactName }}</div></div>
                  <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ viewForm.contactPhone }}</div></div>
                  <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.contactEmail }">{{ viewForm.contactEmail || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.position }">{{ viewForm.position || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewCompany')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></span>企业信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewCompany }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewCompany">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="viewForm.industry" /></div></div>
                  <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="viewForm.companySize" /></div></div>
                  <div class="rd-item"><span class="rd-label">线索来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="viewForm.leadSource" /></div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.address }">{{ viewForm.address || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewRequirement')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>需求与评分</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewRequirement }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewRequirement">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">线索评分</span><div class="rd-value">{{ viewForm.leadScore }}</div></div>
                  <div class="rd-item"><span class="rd-label">线索等级</span><div class="rd-value"><dict-tag :options="marketing_lead_grade" :value="viewForm.leadGrade" /></div></div>
                  <div class="rd-item"><span class="rd-label">线索状态</span><div class="rd-value"><dict-tag :options="marketing_lead_status" :value="viewForm.leadStatus" /></div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">需求描述</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.requirementDesc }">{{ viewForm.requirementDesc || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewOwner')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>负责信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOwner }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewOwner">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewForm.userName || '未分配（公海）' }}</div></div>
                  <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.deptName }">{{ viewForm.deptName || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">领取时间</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.receiveTime }">{{ viewForm.receiveTime || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.receiveApproveUserName }">{{ viewForm.receiveApproveUserName || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">审批时间</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.receiveApproveTime }">{{ viewForm.receiveApproveTime || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">最后跟进</span><div class="rd-value">{{ viewForm.lastFollowTime || '未跟进' }}</div></div>
                  <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card" v-if="viewForm.convertCustomerId">
              <div class="rd-card-header" @click="toggleCard('viewConvert')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg></span>转化信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewConvert }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewConvert">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">转化状态</span><div class="rd-value"><el-tag type="success" size="small">已转化</el-tag></div></div>
                  <div class="rd-item"><span class="rd-label">转化时间</span><div class="rd-value">{{ viewForm.convertTime }}</div></div>
                  <div class="rd-item"><span class="rd-label">转化客户</span><div class="rd-value"><el-button link type="primary" @click="goCustomerDetail(viewForm.convertCustomerId)">查看客户</el-button></div></div>
                </div>
              </div>
            </section>
            <section class="rd-card" v-if="viewForm.leadStatus === '5'">
              <div class="rd-card-header" @click="toggleCard('viewInvalid')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></span>无效信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewInvalid }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewInvalid">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">无效原因</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.ineffectiveReason }">{{ viewForm.ineffectiveReason || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">无效说明</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.ineffectiveRemark }">{{ viewForm.ineffectiveRemark || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewOther')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOther }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewOther">
                <div class="rd-grid">
                  <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`跟进记录 (${interactionList.length})`" name="interactions">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewInteractions')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></span>跟进记录</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewInteractions }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewInteractions">
                <div class="rd-timeline" v-if="interactionList.length > 0">
                  <div class="rd-timeline-item" v-for="item in interactionList" :key="item.recordId">
                    <div class="rd-timeline-dot rd-timeline-dot--success"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title"><el-tag size="small" type="info">{{ getInteractTypeLabel(item.interactType) }}</el-tag> {{ item.userName }}</span>
                        <span class="rd-timeline-time">{{ item.interactTime }}</span>
                      </div>
                      <div class="rd-timeline-comment">{{ item.content }}</div>
                      <div v-if="item.nextTime" style="margin-top: 6px; font-size: 12px; color: #e6a23c">
                        下次跟进: {{ item.nextTime }} {{ item.nextContent ? '- ' + item.nextContent : '' }}
                      </div>
                    </div>
                  </div>
                </div>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  <p class="rd-empty-text">暂无跟进记录</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane label="时间线" name="timeline">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewTimeline')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><path d="M12 7v5l4 2"/></svg></span>时间线</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewTimeline }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewTimeline">
                <div class="rd-timeline" v-if="timelineList.length > 0">
                  <div class="rd-timeline-item" v-for="(item, idx) in timelineList" :key="idx">
                    <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': item.type === 'success', 'rd-timeline-dot--error': item.type === 'danger' }"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title">{{ item.title }}</span>
                        <span class="rd-timeline-time">{{ item.time }}</span>
                      </div>
                      <div class="rd-timeline-comment" v-if="item.desc">{{ item.desc }}</div>
                    </div>
                  </div>
                </div>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  <p class="rd-empty-text">暂无时间线</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleEditFromView" v-hasPermi="['marketing:lead:edit']">编辑</el-button>
        <el-button type="success" @click="handleConvertFromView" v-if="viewForm.leadStatus !== '4' && viewForm.leadStatus !== '5'" v-hasPermi="['marketing:lead:edit']">转化为客户</el-button>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 负责人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择负责人" @confirm="onUserPickerConfirm" />
    <user-picker ref="assignUserPickerRef" title="选择负责人" @confirm="onAssignUserPickerConfirm" />
    <user-picker ref="batchAssignUserPickerRef" title="选择负责人" @confirm="onBatchAssignUserPickerConfirm" />

    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择所属部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">分配线索</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-input v-model="assignUserName" readonly placeholder="请选择（留空释放到公海）" style="width: 100%" @click="openAssignUserPicker">
            <template #append>
              <el-button icon="Search" @click="openAssignUserPicker" />
            </template>
            <template #suffix>
              <el-icon v-if="assignUserName" class="clear-icon" @click.stop="clearAssignUser"><CircleClose /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAssign">确 定</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量分配弹窗 -->
    <el-dialog v-model="batchAssignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">批量分配线索</span>
        </div>
      </template>
      <el-alert :title="'已选中 ' + ids.length + ' 条线索'" type="info" :closable="false" class="mb8" />
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-input v-model="batchAssignUserName" readonly placeholder="请选择负责人" style="width: 100%" @click="openBatchAssignUserPicker">
            <template #append>
              <el-button icon="Search" @click="openBatchAssignUserPicker" />
            </template>
            <template #suffix>
              <el-icon v-if="batchAssignUserName" class="clear-icon" @click.stop="clearBatchAssignUser"><CircleClose /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchAssign">确 定</el-button>
        <el-button @click="batchAssignOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量变更状态弹窗 -->
    <el-dialog v-model="batchStatusOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg></div>
          <span class="rd-detail-header-title">批量变更线索状态</span>
        </div>
      </template>
      <el-alert :title="'已选中 ' + ids.length + ' 条线索'" type="info" :closable="false" class="mb8" />
      <el-form label-width="80px">
        <el-form-item label="线索状态">
          <el-select v-model="batchStatusValue" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_lead_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchStatus">确 定</el-button>
        <el-button @click="batchStatusOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 转化确认弹窗 -->
    <el-dialog v-model="convertOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></div>
          <span class="rd-detail-header-title">线索转化</span>
        </div>
      </template>
      <el-alert type="info" :closable="false" class="mb8">
        <template #title>
          确认将线索"<b>{{ convertForm.companyName }}</b>"转化为客户？<br/>
          系统将自动创建客户档案，并根据线索联系人信息自动创建客户联系人。
        </template>
      </el-alert>
      <el-descriptions :column="2" border size="small" class="mb8">
        <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ convertForm.companyName }}</div></div>
        <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ convertForm.contactName }}</div></div>
        <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ convertForm.contactPhone }}</div></div>
        <div class="rd-item"><span class="rd-label">线索来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="convertForm.leadSource" /></div></div>
      </el-descriptions>
      <template #footer>
        <el-button type="success" @click="confirmConvert">确认转化</el-button>
        <el-button @click="convertOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 无效标记弹窗 -->
    <el-dialog v-model="invalidateOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">标记线索无效</span>
        </div>
      </template>
      <el-form ref="invalidateRef" :model="invalidateForm" label-width="80px">
        <el-form-item label="无效原因" prop="ineffectiveReason">
          <el-select v-model="invalidateForm.ineffectiveReason" placeholder="请选择无效原因" style="width: 100%">
            <el-option label="电话空号/停机" value="phone_invalid" />
            <el-option label="客户明确拒绝" value="rejected" />
            <el-option label="需求不匹配" value="no_match" />
            <el-option label="重复线索" value="duplicate" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="无效说明" prop="ineffectiveRemark">
          <el-input v-model="invalidateForm.ineffectiveRemark" type="textarea" :rows="3" placeholder="请输入无效说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmInvalidate">确认标记无效</el-button>
        <el-button @click="invalidateOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 快速跟进弹窗 -->
    <el-dialog v-model="followOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div>
          <span class="rd-detail-header-title">快速跟进</span>
        </div>
      </template>
      <el-form ref="followRef" :model="followForm" :rules="followRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('followInfo')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>线索信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.followInfo }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.followInfo">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ followForm.companyName }}</div></div>
                <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ followForm.contactName }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('followForm')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>跟进信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.followForm }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.followForm">
              <el-form-item label="互动类型" prop="interactType">
                <el-select v-model="followForm.interactType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_interaction_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="互动时间" prop="interactTime">
                <el-date-picker v-model="followForm.interactTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
              </el-form-item>
              <el-form-item label="跟进内容" prop="content">
                <el-input v-model="followForm.content" type="textarea" :rows="3" placeholder="请输入跟进内容" />
              </el-form-item>
              <el-form-item label="下次跟进">
                <el-date-picker v-model="followForm.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间（可选）" style="width: 100%" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmFollowUp">确 定</el-button>
        <el-button @click="followOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="importOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <span class="rd-detail-header-title">线索导入</span>
        </div>
      </template>
      <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :auto-upload="false" :action="importUrl" :headers="headers" :data="{ updateSupport: importUpdateSupport }" :on-success="handleImportSuccess" :on-error="handleImportError" drag>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-button type="primary" link @click="handleDownloadTemplate">下载模板</el-button>
          </div>
        </template>
      </el-upload>
      <div class="mt16">
        <el-checkbox v-model="importUpdateSupport">如果已存在相同企业名称，则更新数据</el-checkbox>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitImport">确 定</el-button>
        <el-button @click="importOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 重复线索弹窗 -->
    <el-dialog v-model="dupOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg></div>
          <span class="rd-detail-header-title">疑似重复线索</span>
        </div>
      </template>
      <el-alert v-if="dupList.length > 0" title="系统检测到以下线索可能与当前输入重复，请确认是否继续" type="warning" :closable="false" class="mb8" />
      <el-table :data="dupList" border size="small">
        <el-table-column label="线索编号" prop="leadNo" width="150" />
        <el-table-column label="企业名称" prop="companyName" show-overflow-tooltip />
        <el-table-column label="联系人" prop="contactName" width="100" />
        <el-table-column label="手机号" prop="contactPhone" width="130" />
        <el-table-column label="状态" prop="leadStatus" width="80" align="center">
          <template #default="scope"><span class="badge" :class="badgeClass(scope.row.leadStatus)"><span class="dot"></span>{{ statusLabel(scope.row.leadStatus) }}</span></template>
        </el-table-column>
      </el-table>
      <el-empty v-if="dupList.length === 0" description="未检测到重复线索" />
      <template #footer>
        <el-button @click="dupOpen = false">关 闭</el-button>
      </template>
</el-dialog>

    <!-- 客户详情弹窗 -->
    <CustomerDetailDialog v-model="customerDetailVisible" :customer-id="customerDetailId" />

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="线索管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">新线索</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">跟进中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已转化</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="primary">跟进中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">无效</el-tag>
            <el-tag size="small" type="info">无法再跟进</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="新线索">线索创建后的初始状态，等待分配或跟进</el-descriptions-item>
          <el-descriptions-item label="跟进中">线索已分配负责人，正在进行跟进沟通</el-descriptions-item>
          <el-descriptions-item label="已转化">线索已成功转化为客户，自动创建客户档案和联系人</el-descriptions-item>
          <el-descriptions-item label="无效">线索被标记为无效（电话空号、客户拒绝等），不可再跟进</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>快速跟进：</strong>点击"跟进"按钮可快速录入跟进记录，系统自动更新最后跟进时间</p>
          <p>• <strong>转化为客户：</strong>线索转化后自动创建客户档案和联系人，不可逆操作</p>
          <p>• <strong>公海机制：</strong>可退回公海，其他销售人员可申请领取</p>
          <p>• <strong>查重功能：</strong>新增时自动检测重复企业名称和手机号</p>
          <p>• <strong>批量操作：</strong>支持批量分配负责人、批量变更状态</p>
          <p>• <strong>线索导入：</strong>支持通过Excel批量导入线索数据</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
</div>
</template>

<script setup name="MkLead">
import { useRouter } from 'vue-router'
import { UploadFilled, CircleClose, ArrowRight, QuestionFilled } from '@element-plus/icons-vue'
import { listLead, getLead, addLead, updateLead, delLead, convertLead, assignLead, releaseLeadToPool, batchAssignLead, batchUpdateLeadStatus, invalidateLead, updateFollowTime, checkLeadDuplicate, getLeadLog } from '@/api/mk/lead'
import { listInteraction, addInteraction } from '@/api/mk/interaction'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import CustomerDetailDialog from '@/components/CustomerDetailDialog/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['c_lead', 'c_company', 'c_requirement', 'c_owner', 'c_other', 'viewBasic', 'viewCompany', 'viewRequirement', 'viewOwner', 'viewOther', 'viewConvert', 'viewInvalid', 'viewInteractions', 'viewTimeline', 'followInfo', 'followForm'])
import { getToken } from '@/utils/auth'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_lead_index')
const { marketing_lead_status, marketing_lead_grade, marketing_customer_source, marketing_industry, marketing_company_size, marketing_interaction_type } = proxy.useDict('marketing_lead_status', 'marketing_lead_grade', 'marketing_customer_source', 'marketing_industry', 'marketing_company_size', 'marketing_interaction_type')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const statusTabList = computed(() => marketing_lead_status.value)
function loadStatusCounts() {
  const counts = { all: 0 }
  marketing_lead_status.value.forEach(d => { counts[d.value] = 0 })
  list.value.forEach(row => {
    const s = row.leadStatus
    if (counts[s] !== undefined) counts[s]++
  })
  counts.all = total.value
  statusCounts.value = counts
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.leadStatus = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'violet', '4': 'gray', '5': 'red' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_lead_status.value.find(d => d.value == status); return item ? item.label : '-' }
function sourceBadgeClass(source) { const map = { '1': 'blue', '2': 'green', '3': 'violet', '4': 'amber', '5': 'cyan', '6': 'red', '7': 'gray' }; return map[source] || 'gray' }
function sourceLabel(source) { const item = marketing_customer_source.value.find(d => d.value == source); return item ? item.label : '-' }
function gradeBadgeClass(grade) { const map = { '1': 'red', '2': 'amber', '3': 'blue', '4': 'green' }; return map[grade] || 'gray' }
function gradeLabel(grade) { const item = marketing_lead_grade.value.find(d => d.value == grade); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-partial', '4': 'tab-void', '5': 'tab-reject' }; return map[value] || '' }
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewForm = ref({})
const detailTab = ref('basic')
const interactionList = ref([])
const timelineList = ref([])

// 分配相关
const assignOpen = ref(false)
const assignUserId = ref(null)
const assignUserName = ref(null)
const assignUserDeptId = ref(null)
const assignLeadId = ref(null)

// 批量分配相关
const batchAssignOpen = ref(false)
const batchAssignUserId = ref(null)
const batchAssignUserName = ref(null)
const batchAssignUserDeptId = ref(null)

// 批量变更状态相关
const batchStatusOpen = ref(false)
const batchStatusValue = ref(null)

// 转化相关
const convertOpen = ref(false)
const convertForm = ref({})

// 无效标记
const invalidateOpen = ref(false)
const invalidateForm = ref({})
const invalidateLeadId = ref(null)

// 快速跟进
const followOpen = ref(false)
const followForm = ref({})
const followRules = {
  interactType: [{ required: true, message: '请选择互动类型', trigger: 'change' }],
  interactTime: [{ required: true, message: '请选择互动时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入跟进内容', trigger: 'blur' }]
}

// 导入相关
const importOpen = ref(false)
const importUpdateSupport = ref(false)
const importUrl = ref(import.meta.env.VITE_APP_BASE_API + '/mk/lead/importData')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

// 查重
const dupOpen = ref(false)
const dupList = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, leadNo: undefined, companyName: undefined, leadStatus: undefined, leadGrade: undefined, leadSource: undefined, isPublic: '0', params: {} },
  rules: {
    companyName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
    contactName: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
    contactPhone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  leadNo: { label: '线索编号', visible: true },
  companyName: { label: '企业名称', visible: true },
  contactName: { label: '联系人', visible: true },
  contactPhone: { label: '手机号', visible: true },
  leadSource: { label: '线索来源', visible: true },
  leadGrade: { label: '等级', visible: true },
  leadScore: { label: '评分', visible: true },
  leadStatus: { label: '状态', visible: true },
  userName: { label: '负责人', visible: true },
  lastFollowTime: { label: '最后跟进', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_lead_columns')
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

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.leadNo) count++
  if (queryParams.value.companyName) count++
  if (queryParams.value.leadStatus) count++
  if (queryParams.value.leadGrade) count++
  if (queryParams.value.leadSource) count++
  return count
})

function getList() { loading.value = true; listLead(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false }) }

/** 打开负责人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.userId)
}
/** 负责人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.userId = user.userId
  form.value.userName = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}
/** 清除负责人 */
function clearUser() {
  form.value.userId = undefined
  form.value.userName = undefined
}
/** 打开部门选择弹窗 */
function openDeptPicker() {
  proxy.$refs.deptPickerRef.open(form.value.deptId)
}
/** 部门选择确认回调 */
function onDeptPickerConfirm(dept) {
  form.value.deptId = dept.deptId
  form.value.deptName = dept.deptName
}
/** 清除部门 */
function clearDept() {
  form.value.deptId = undefined
  form.value.deptName = undefined
}

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.leadNo = undefined; queryParams.value.companyName = undefined; queryParams.value.leadStatus = undefined; queryParams.value.leadGrade = undefined; queryParams.value.leadSource = undefined; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.leadId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { leadNo: undefined, companyName: undefined, contactName: undefined, contactPhone: undefined, contactEmail: undefined, position: undefined, industry: undefined, companySize: undefined, address: undefined, leadSource: undefined, activityId: undefined, requirementDesc: undefined, leadScore: 0, leadGrade: 'D', leadStatus: '0', isPublic: '1', userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, remark: undefined }
  proxy.resetForm('leadRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增线索' }
function handleUpdate(row) { reset(); getLead(row.leadId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改线索' }) }
function handleView(row) {
  getLead(row.leadId).then(res => {
    viewForm.value = res.data
    detailTab.value = 'basic'
    interactionList.value = []
    timelineList.value = []
    Object.keys(collapsedCards).forEach(k => { collapsedCards[k] = false })
    viewOpen.value = true
  })
}
function loadLeadRelations() {
  if (!viewForm.value.leadId) return
  listInteraction({ leadId: viewForm.value.leadId, pageNum: 1, pageSize: 999 }).then(res => {
    interactionList.value = res.rows || []
  })
}
function buildTimeline() {
  getLeadLog(viewForm.value.leadId).then(res => {
    const logList = res.data || []
    const typeMap = {
      create: 'primary',
      apply: 'warning',
      approve: 'success',
      reject: 'danger',
      assign: 'success',
      release: 'info',
      convert: 'success',
      invalidate: 'danger',
      status_change: 'primary'
    }
    const titleMap = {
      create: '线索创建',
      apply: '申请领取',
      approve: '审批通过',
      reject: '审批退回',
      assign: '线索分配',
      release: '退回公海',
      convert: '线索转化',
      invalidate: '标记无效',
      status_change: '信息更新'
    }
    const items = []
    // 日志记录（持久化，不受退回公海影响）
    logList.forEach(log => {
      items.push({
        time: log.operateTime,
        title: titleMap[log.actionType] || log.actionType,
        desc: (log.operatorName ? '操作人：' + log.operatorName + '　' : '') + (log.actionDesc || ''),
        type: typeMap[log.actionType] || 'primary'
      })
    })
    // 跟进记录仍然保留
    interactionList.value.forEach(i => {
      items.push({ time: i.interactTime, title: '跟进记录', desc: i.content, type: 'primary' })
      if (i.nextTime) {
        items.push({ time: i.nextTime, title: '计划跟进', desc: i.nextContent || '', type: 'warning' })
      }
    })
    items.sort((a, b) => (b.time || '').localeCompare(a.time || ''))
    timelineList.value = items
  })
}
function handleEditFromView() {
  viewOpen.value = false
  handleUpdate(viewForm.value)
}
function handleConvertFromView() {
  convertForm.value = { ...viewForm.value }
  convertOpen.value = true
}
const customerDetailVisible = ref(false)
const customerDetailId = ref(null)
function openCustomerDetail(customerId) { customerDetailId.value = customerId; customerDetailVisible.value = true }
function goCustomerDetail(customerId) { openCustomerDetail(customerId) }
function getInteractTypeLabel(type) {
  const item = marketing_interaction_type.value?.find(d => d.value === type)
  return item ? item.label : type
}
function submitForm() {
  proxy.$refs['leadRef'].validate(valid => {
    if (valid) {
      if (form.value.leadId != undefined) { updateLead(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addLead(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const leadIds = row.leadId || ids.value; proxy.$modal.confirm('确认删除编号为"' + leadIds + '"的数据？').then(() => delLead(leadIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/lead/export', { ...queryParams.value }, `lead_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ====== 更多操作下拉菜单 ======
function handleCommand(cmd, row) {
  if (cmd === 'convert') handleConvert(row)
  else if (cmd === 'assign') handleAssignSingle(row)
  else if (cmd === 'release') handleRelease(row)
  else if (cmd === 'invalidate') handleInvalidate(row)
  else if (cmd === 'delete') handleDelete(row)
}

// 转化
function handleConvert(row) {
  convertForm.value = { ...row }
  convertOpen.value = true
}
function confirmConvert() {
  convertLead(convertForm.value.leadId, {}).then(res => {
    proxy.$modal.msgSuccess('转化成功，已自动创建客户和联系人')
    convertOpen.value = false
    getList()
// 弹窗打开新创建的客户详情
if (res.convertCustomerId) {
openCustomerDetail(res.convertCustomerId)
}
  })
}

// 分配
function handleAssignSingle(row) {
  assignLeadId.value = row.leadId
  assignUserId.value = row.userId || null
  assignUserName.value = row.userName || null
  assignUserDeptId.value = row.deptId || null
  assignOpen.value = true
}
/** 打开分配负责人选择弹窗 */
function openAssignUserPicker() {
  proxy.$refs.assignUserPickerRef.open(assignUserId.value)
}
/** 分配负责人选择确认回调 */
function onAssignUserPickerConfirm(user) {
  assignUserId.value = user.userId
  assignUserName.value = user.nickName
  assignUserDeptId.value = user.deptId
}
/** 清除分配负责人 */
function clearAssignUser() {
  assignUserId.value = null
  assignUserName.value = null
  assignUserDeptId.value = null
}
function confirmAssign() {
  const data = { userId: assignUserId.value, deptId: assignUserDeptId.value, userName: assignUserName.value }
  assignLead(assignLeadId.value, data).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getList()
  })
}

// 退回公海
function handleRelease(row) {
  proxy.$modal.confirm('确认将"' + row.companyName + '"退回公海？').then(() => {
    releaseLeadToPool(row.leadId).then(() => {
      proxy.$modal.msgSuccess('已退回公海')
      getList()
    })
  }).catch(() => {})
}

// 标记无效
function handleInvalidate(row) {
  invalidateLeadId.value = row.leadId
  invalidateForm.value = { ineffectiveReason: undefined, ineffectiveRemark: undefined }
  invalidateOpen.value = true
}
function confirmInvalidate() {
  if (!invalidateForm.value.ineffectiveReason) {
    proxy.$modal.msgWarning('请选择无效原因')
    return
  }
  invalidateLead(invalidateLeadId.value, invalidateForm.value).then(() => {
    proxy.$modal.msgSuccess('已标记为无效')
    invalidateOpen.value = false
    getList()
  })
}

// 快速跟进
function handleFollowUp(row) {
  followForm.value = {
    leadId: row.leadId,
    companyName: row.companyName,
    contactName: row.contactName,
    interactType: undefined,
    interactTime: undefined,
    content: undefined,
    userId: row.userId,
    userName: row.userName,
    nextTime: undefined
  }
  followOpen.value = true
}
function confirmFollowUp() {
  proxy.$refs['followRef'].validate(valid => {
    if (valid) {
      addInteraction(followForm.value).then(() => {
        proxy.$modal.msgSuccess('跟进记录添加成功')
        // 更新线索最后跟进时间
        updateFollowTime(followForm.value.leadId).then(() => {})
        followOpen.value = false
        getList()
      })
    }
  })
}

// 批量分配
function handleBatchAssign() {
  batchAssignUserId.value = null
  batchAssignUserName.value = null
  batchAssignUserDeptId.value = null
  batchAssignOpen.value = true
}
/** 打开批量分配负责人选择弹窗 */
function openBatchAssignUserPicker() {
  proxy.$refs.batchAssignUserPickerRef.open(batchAssignUserId.value)
}
/** 批量分配负责人选择确认回调 */
function onBatchAssignUserPickerConfirm(user) {
  batchAssignUserId.value = user.userId
  batchAssignUserName.value = user.nickName
  batchAssignUserDeptId.value = user.deptId
}
/** 清除批量分配负责人 */
function clearBatchAssignUser() {
  batchAssignUserId.value = null
  batchAssignUserName.value = null
  batchAssignUserDeptId.value = null
}
function confirmBatchAssign() {
  if (!batchAssignUserId.value) {
    proxy.$modal.msgWarning('请选择负责人')
    return
  }
  const data = {
    leadIds: ids.value,
    userId: batchAssignUserId.value,
    deptId: batchAssignUserDeptId.value,
    userName: batchAssignUserName.value
  }
  batchAssignLead(data).then(() => {
    proxy.$modal.msgSuccess('批量分配成功')
    batchAssignOpen.value = false
    getList()
  })
}

// 批量变更状态
function handleBatchStatus() {
  batchStatusValue.value = null
  batchStatusOpen.value = true
}
function confirmBatchStatus() {
  if (!batchStatusValue.value) {
    proxy.$modal.msgWarning('请选择线索状态')
    return
  }
  batchUpdateLeadStatus({ leadIds: ids.value, leadStatus: batchStatusValue.value }).then(() => {
    proxy.$modal.msgSuccess('批量变更状态成功')
    batchStatusOpen.value = false
    getList()
  })
}

// 导入
function handleImport() { importOpen.value = true }
function handleDownloadTemplate() { proxy.download('mk/lead/importTemplate', {}, `lead_template_${new Date().getTime()}.xlsx`) }
function submitImport() { proxy.$refs['uploadRef'].submit() }
function handleImportSuccess(res) {
  if (res.code === 200) {
    proxy.$modal.msgSuccess(res.msg)
    importOpen.value = false
    getList()
  } else {
    proxy.$modal.msgError(res.msg)
  }
}
function handleImportError() { proxy.$modal.msgError('导入失败') }

// 查重
function onCompanyBlur() { if (form.value.companyName && !form.value.leadId) doCheckDup() }
function onPhoneBlur() { if (form.value.contactPhone && !form.value.leadId) doCheckDup() }
function doCheckDup() {
  checkLeadDuplicate(form.value.companyName, form.value.contactPhone).then(res => {
    if (res.data && res.data.length > 0) {
      dupList.value = res.data
      dupOpen.value = true
    }
  })
}

// 监听详情tab切换
watch(detailTab, (val) => {
  if (val === 'timeline') {
    buildTimeline()
  }
})

handleQuery()
getList()

const showStatusHelp = ref(false)
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.ml5 { margin-left: 5px; }
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
:deep(.el-input.is-disabled .el-input__inner) {
  cursor: pointer;
}
.mt16 { margin-top: 16px; }
.text-center { text-align: center; }

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
.status-help-content .highlight-card {
  background-color: #ecf5ff;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #409eff;
}
.status-help-content .highlight-card p {
  margin: 6px 0;
  line-height: 1.6;
  font-size: 13px;
  color: #606266;
}
</style>
