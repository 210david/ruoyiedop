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
          <label>客户编号</label>
          <div class="control">
            <el-input v-model="queryParams.customerNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>企业名称</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>客户等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.customerLevel" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_customer_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>客户状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.customerStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>所属行业</label>
          <div class="control is-select">
            <el-select v-model="queryParams.industry" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_industry" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>客户来源</label>
          <div class="control is-select">
            <el-select v-model="queryParams.customerSource" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_customer_source" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
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

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchClaim" v-hasPermi="['marketing:customer:claim']">
            <el-icon><Pointer /></el-icon> 批量领取
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:customer:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_customer_pool_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="客户编号" prop="customerNo" key="customerNo" :width="colWidth('customerNo', 150)" resizable v-if="columns.customerNo.visible" />
          <el-table-column label="企业名称" prop="customerName" key="customerName" show-overflow-tooltip v-if="columns.customerName.visible">
            <template #default="scope">
              <el-button link type="primary" @click="handleView(scope.row)">{{ scope.row.customerName }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="客户等级" prop="customerLevel" key="customerLevel" :width="colWidth('customerLevel', 100)" resizable align="center" v-if="columns.customerLevel.visible">
            <template #default="scope">
              <span class="badge" :class="levelBadgeClass(scope.row.customerLevel)">
                <span class="dot"></span>{{ levelLabel(scope.row.customerLevel) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="所属行业" prop="industry" key="industry" :width="colWidth('industry', 120)" resizable align="center" v-if="columns.industry.visible">
            <template #default="scope">
              <span class="badge" :class="industryBadgeClass(scope.row.industry)">
                <span class="dot"></span>{{ industryLabel(scope.row.industry) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="客户状态" prop="customerStatus" key="customerStatus" :width="colWidth('customerStatus', 100)" resizable align="center" v-if="columns.customerStatus.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.customerStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.customerStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="累计交易额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 120)" resizable align="right" v-if="columns.totalAmount.visible">
            <template #default="scope"><span class="rd-amount">￥{{ formatAmount(scope.row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="最后更新" prop="updateTime" key="updateTime" :width="colWidth('updateTime', 160)" resizable sortable="custom" v-if="columns.updateTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="success" icon="Pointer" @click="handleClaim(scope.row)" v-hasPermi="['marketing:customer:claim']">领取</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog" @open="loadRelations">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">客户详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.customerNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.customerNo }}</span>
            <dict-tag :options="marketing_customer_status" :value="viewForm.customerStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_basic')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>客户信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_basic">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">客户编号</span><div class="rd-value">{{ viewForm.customerNo }}</div></div>
                  <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
                  <div class="rd-item"><span class="rd-label">信用代码</span><div class="rd-value">{{ viewForm.creditCode }}</div></div>
                  <div class="rd-item"><span class="rd-label">客户等级</span><div class="rd-value"><dict-tag :options="marketing_customer_level" :value="viewForm.customerLevel" /></div></div>
                  <div class="rd-item"><span class="rd-label">客户来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="viewForm.customerSource" /></div></div>
                  <div class="rd-item"><span class="rd-label">客户状态</span><div class="rd-value"><dict-tag :options="marketing_customer_status" :value="viewForm.customerStatus" /></div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">客户标签</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.tagNames }">{{ viewForm.tagNames || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_company')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></span>企业信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_company }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_company">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="viewForm.industry" /></div></div>
                  <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="viewForm.companySize" /></div></div>
                  <div class="rd-item"><span class="rd-label">官网</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.website }">{{ viewForm.website || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.address }">{{ viewForm.address || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_business')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>业务信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_business }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_business">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">首次接触</span><div class="rd-value">{{ viewForm.firstContactDate }}</div></div>
                  <div class="rd-item"><span class="rd-label">合作开始</span><div class="rd-value">{{ viewForm.cooperationDate }}</div></div>
                  <div class="rd-item"><span class="rd-label">累计交易额</span><div class="rd-value rd-value--large rd-amount rd-amount--negative">￥{{ formatAmount(viewForm.totalAmount) }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_owner')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>负责信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_owner }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_owner">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewForm.userName || '未分配（公海）' }}</div></div>
                  <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.deptName }">{{ viewForm.deptName || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_other')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_other">
                <div class="rd-grid">
                  <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`联系人 (${contacts.length})`" name="contacts">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_contacts')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>联系人</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_contacts }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_contacts">
                <el-table :data="contacts" border size="small" v-if="contacts.length > 0">
                  <el-table-column label="姓名" prop="name" width="100" />
                  <el-table-column label="职位" prop="position" width="120" show-overflow-tooltip />
                  <el-table-column label="手机号" prop="phone" width="130" />
                  <el-table-column label="邮箱" prop="email" show-overflow-tooltip />
                  <el-table-column label="关键联系人" prop="isKey" width="100" align="center"><template #default="scope"><span class="badge" :class="scope.row.isKey === '1' ? 'red' : 'gray'"><span class="dot"></span>{{ scope.row.isKey === '1' ? '是' : '否' }}</span></template></el-table-column>
                  <el-table-column label="主要联系人" prop="isPrimary" width="100" align="center"><template #default="scope"><span class="badge" :class="scope.row.isPrimary === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.isPrimary === '1' ? '是' : '否' }}</span></template></el-table-column>
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg><p class="rd-empty-text">暂无联系人</p></div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`商机 (${opportunities.length})`" name="opportunities">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_opportunities')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></span>商机</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_opportunities }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_opportunities">
                <el-table :data="opportunities" border size="small" v-if="opportunities.length > 0">
                  <el-table-column label="商机编号" prop="opportunityNo" width="150" />
                  <el-table-column label="商机名称" prop="opportunityName" show-overflow-tooltip />
                  <el-table-column label="销售阶段" prop="stageName" width="100" align="center" />
                  <el-table-column label="预计金额" prop="expectedAmount" width="120" align="right"><template #default="scope">￥{{ formatAmount(scope.row.expectedAmount) }}</template></el-table-column>
                  <el-table-column label="商机状态" prop="opportunityStatus" width="100" align="center"><template #default="scope"><span class="badge" :class="oppBadgeClass(scope.row.opportunityStatus)"><span class="dot"></span>{{ oppStatusLabel(scope.row.opportunityStatus) }}</span></template></el-table-column>
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/></svg><p class="rd-empty-text">暂无商机</p></div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`合同 (${contracts.length})`" name="contracts">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_contracts')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>合同</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_contracts }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_contracts">
                <el-table :data="contracts" border size="small" v-if="contracts.length > 0">
                  <el-table-column label="合同编号" prop="contractNo" width="150" />
                  <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
                  <el-table-column label="合同金额" prop="contractAmount" width="120" align="right"><template #default="scope">￥{{ formatAmount(scope.row.contractAmount) }}</template></el-table-column>
                  <el-table-column label="签订日期" prop="signDate" width="120" />
                  <el-table-column label="合同状态" prop="contractStatus" width="100" align="center"><template #default="scope"><span class="badge" :class="contractBadgeClass(scope.row.contractStatus)"><span class="dot"></span>{{ contractStatusLabel(scope.row.contractStatus) }}</span></template></el-table-column>
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg><p class="rd-empty-text">暂无合同</p></div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`订单 (${orders.length})`" name="orders">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_orders')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg></span>订单</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_orders }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_orders">
                <el-table :data="orders" border size="small" v-if="orders.length > 0">
                  <el-table-column label="订单编号" prop="orderNo" width="150" />
                  <el-table-column label="订单名称" prop="orderName" show-overflow-tooltip />
                  <el-table-column label="订单金额" prop="orderAmount" width="120" align="right"><template #default="scope">￥{{ formatAmount(scope.row.orderAmount) }}</template></el-table-column>
                  <el-table-column label="下单日期" prop="orderDate" width="120" />
                  <el-table-column label="订单状态" prop="orderStatus" width="100" align="center"><template #default="scope"><span class="badge" :class="orderBadgeClass(scope.row.orderStatus)"><span class="dot"></span>{{ orderStatusLabel(scope.row.orderStatus) }}</span></template></el-table-column>
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg><p class="rd-empty-text">暂无订单</p></div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`互动记录 (${interactions.length})`" name="interactions">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_interactions')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></span>互动记录</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_interactions }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_interactions">
                <div class="rd-timeline" v-if="interactions.length > 0">
                  <div class="rd-timeline-item" v-for="item in interactions" :key="item.recordId">
                    <div class="rd-timeline-dot rd-timeline-dot--success"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title"><dict-tag :options="marketing_interaction_type" :value="item.interactType" /></span>
                        <span class="rd-timeline-time">{{ item.interactTime }}</span>
                      </div>
                      <div class="rd-timeline-comment">{{ item.content }}</div>
                      <div v-if="item.nextTime" style="margin-top: 6px; font-size: 12px; color: #e6a23c">下次跟进: {{ item.nextTime }} {{ item.nextContent ? '- ' + item.nextContent : '' }}</div>
                    </div>
                  </div>
                </div>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg><p class="rd-empty-text">暂无互动记录</p></div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`参与活动 (${activities.length})`" name="activities">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_activities')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>参与活动</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_activities }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_activities">
                <el-table :data="activities" border size="small" v-if="activities.length > 0">
                  <el-table-column label="活动名称" prop="activityName" show-overflow-tooltip />
                  <el-table-column label="联系人" prop="contactName" width="100" />
                  <el-table-column label="企业名称" prop="companyName" width="180" show-overflow-tooltip />
                  <el-table-column label="参与状态" prop="participateStatus" width="100" align="center"><template #default="scope"><span class="badge" :class="partBadgeClass(scope.row.participateStatus)"><span class="dot"></span>{{ partStatusLabel(scope.row.participateStatus) }}</span></template></el-table-column>
                  <el-table-column label="签到时间" prop="signTime" width="160" />
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/></svg><p class="rd-empty-text">暂无活动参与记录</p></div>
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="客户公海业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">负责人释放</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">公海池</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">销售领取</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">自动分配</el-tag>
          </div>
        </div>

        <h4>二、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>数据来源：</strong>客户管理中负责人主动释放客户到公海，或新增客户时未指定负责人</p>
          <p>• <strong>领取机制：</strong>有权限的销售人员均可领取公海客户，领取后客户自动分配给领取人</p>
          <p>• <strong>批量领取：</strong>支持批量选择多个公海客户一次性领取</p>
          <p>• <strong>详情查看：</strong>点击客户名称可查看完整客户信息及关联数据（联系人、商机、合同等）</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkCustomerPool">
import { listPublicPool, getCustomer, claimCustomer } from '@/api/mk/customer'
import { ArrowRight, ArrowDown, QuestionFilled } from '@element-plus/icons-vue'
import { listContact } from '@/api/mk/contact'
import { listOpportunity } from '@/api/mk/opportunity'
import { listContract } from '@/api/mk/contract'
import { listOrder } from '@/api/mk/order'
import { listInteraction } from '@/api/mk/interaction'
import { listParticipant } from '@/api/mk/participant'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(["v_basic", "v_company", "v_business", "v_owner", "v_other", "v_contacts", "v_opportunities", "v_contracts", "v_orders", "v_interactions", "v_activities"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_customer_pool')
const { marketing_customer_level, marketing_customer_source, marketing_industry, marketing_company_size, marketing_customer_status, marketing_opportunity_status, marketing_contract_status, marketing_order_status, marketing_interaction_type, marketing_participate_status } = proxy.useDict('marketing_customer_level', 'marketing_customer_source', 'marketing_industry', 'marketing_company_size', 'marketing_customer_status', 'marketing_opportunity_status', 'marketing_contract_status', 'marketing_order_status', 'marketing_interaction_type', 'marketing_participate_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
// 详情弹窗
const viewOpen = ref(false)
const viewForm = ref({})
const detailTab = ref('basic')
// 详情关联数据
const contacts = ref([])
const opportunities = ref([])
const contracts = ref([])
const orders = ref([])
const interactions = ref([])
const activities = ref([])

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, customerNo: undefined, customerName: undefined, customerLevel: undefined, customerStatus: undefined, industry: undefined, customerSource: undefined, params: {} }
})
const { queryParams } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  customerNo: { label: '客户编号', visible: true },
  customerName: { label: '企业名称', visible: true },
  customerLevel: { label: '客户等级', visible: true },
  industry: { label: '所属行业', visible: true },
  customerStatus: { label: '客户状态', visible: true },
  totalAmount: { label: '累计交易额', visible: true },
  updateTime: { label: '最后更新', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_customer_pool_columns')
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
if (queryParams.value.customerNo) count++
if (queryParams.value.customerName) count++
if (queryParams.value.customerLevel) count++
if (queryParams.value.customerStatus) count++
if (queryParams.value.industry) count++
if (queryParams.value.customerSource) count++
if (dateRange.value && dateRange.value.length > 0) count++
return count
})

const showAdvanced = ref(false)
const dateRange = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const statusTabList = computed(() => marketing_customer_status.value)
function loadStatusCounts() {
  // 基于当前筛选条件（剔除状态与分页）拉取全量数据统计，避免仅统计当前页
  const query = { ...queryParams.value, pageNum: 1, pageSize: 9999, customerStatus: undefined, params: { ...queryParams.value.params } }
  listPublicPool(query).then(res => {
    const counts = { all: 0 }
    marketing_customer_status.value.forEach(d => { counts[d.value] = 0 })
    const rows = res.rows || []
    rows.forEach(row => {
      const s = row.customerStatus
      if (counts[s] !== undefined) counts[s]++
    })
    counts.all = rows.length
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.customerStatus = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'green', '1': 'amber', '2': 'red', '3': 'blue', '4': 'gray' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_customer_status.value.find(d => d.value == status); return item ? item.label : '-' }
function levelBadgeClass(level) { const map = { '1': 'violet', '2': 'blue', '3': 'green', '4': 'amber', '5': 'gray' }; return map[level] || 'gray' }
function levelLabel(level) { const item = marketing_customer_level.value.find(d => d.value == level); return item ? item.label : '-' }
function industryBadgeClass(industry) { const map = { '1': 'blue', '2': 'green', '3': 'violet', '4': 'amber', '5': 'cyan', '6': 'red', '7': 'gray', '8': 'orange', '9': 'pink', '10': 'indigo' }; return map[industry] || 'gray' }
function industryLabel(industry) { const item = marketing_industry.value.find(d => d.value == industry); return item ? item.label : '-' }
function oppBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'violet', '4': 'gray', '5': 'red' }; return map[status] || 'gray' }
function oppStatusLabel(status) { const item = marketing_opportunity_status.value.find(d => d.value == status); return item ? item.label : '-' }
function contractBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'green', '4': 'gray', '5': 'red', '6': 'violet' }; return map[status] || 'gray' }
function contractStatusLabel(status) { const item = marketing_contract_status.value.find(d => d.value == status); return item ? item.label : '-' }
function orderBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'green', '4': 'gray', '5': 'red' }; return map[status] || 'gray' }
function orderStatusLabel(status) { const item = marketing_order_status.value.find(d => d.value == status); return item ? item.label : '-' }
function partBadgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'gray' }; return map[status] || 'gray' }
function partStatusLabel(status) { const item = marketing_participate_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-approved', '1': 'tab-draft', '2': 'tab-reject', '3': 'tab-audit', '4': 'tab-void' }; return map[value] || '' }

function getList() {
  loading.value = true
  listPublicPool(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime'); getList() }
function resetQuery() { queryParams.value.customerNo = undefined; queryParams.value.customerName = undefined; queryParams.value.customerLevel = undefined; queryParams.value.customerStatus = undefined; queryParams.value.industry = undefined; queryParams.value.customerSource = undefined; queryParams.value.params = {}; dateRange.value = []; activeStatusTab.value = 'all'; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.customerId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleExport() { proxy.download('mk/customer/export', { ...queryParams.value }, `public_pool_${new Date().getTime()}.xlsx`) }

function handleView(row) {
  getCustomer(row.customerId).then(res => {
    viewForm.value = res.data
    detailTab.value = 'basic'
    // 重置关联数据
    contacts.value = []; opportunities.value = []; contracts.value = []; orders.value = []; interactions.value = []; activities.value = []
    // 展开所有详情卡片
    Object.keys(collapsedCards).forEach(k => {
      if (k.startsWith('v_')) collapsedCards[k] = false
    })
    viewOpen.value = true
  })
}
/** 加载详情关联数据 */
function loadRelations() {
  if (!viewForm.value.customerId) return
  const cid = viewForm.value.customerId
  listContact({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { contacts.value = res.rows || [] })
  listOpportunity({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { opportunities.value = res.rows || [] })
  listContract({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { contracts.value = res.rows || [] })
  listOrder({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { orders.value = res.rows || [] })
  listInteraction({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { interactions.value = res.rows || [] })
  listParticipant({ customerId: cid, pageNum: 1, pageSize: 999 }).then(res => { activities.value = res.rows || [] })
}
/** 金额格式化 */
function formatAmount(val) { if (val == null || val === '') return '-'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

function handleClaim(row) {
  proxy.$modal.confirm('确认领取"' + row.customerName + '"？领取后您将成为该客户的负责人。').then(() => {
    claimCustomer(row.customerId).then(() => {
      proxy.$modal.msgSuccess('领取成功')
      getList()
    })
  }).catch(() => {})
}
function handleBatchClaim() {
  proxy.$modal.confirm('确认领取选中的 ' + ids.value.length + ' 个客户？').then(() => {
    const promises = ids.value.map(id => claimCustomer(id))
    Promise.all(promises).then(() => {
      proxy.$modal.msgSuccess('批量领取成功')
      getList()
    }).catch(() => {})
  }).catch(() => {})
}

const showStatusHelp = ref(false)

getList()
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }

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

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
