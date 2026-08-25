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
          <label>负责人</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:customer:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['marketing:customer:import']">
            <el-icon><Upload /></el-icon> 导入
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:customer:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:customer:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchAssign" v-hasPermi="['marketing:customer:assign']">
            <el-icon><User /></el-icon> 批量分配
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchStatus" v-hasPermi="['marketing:customer:edit']">
            <el-icon><RefreshRight /></el-icon> 批量改状态
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:customer:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_customer_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="客户编号" prop="customerNo" key="customerNo" :width="colWidth('customerNo', 150)" resizable sortable="custom" v-if="columns.customerNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.customerNo }}</span></template>
          </el-table-column>
          <el-table-column label="企业名称" prop="customerName" key="customerName" :width="colWidth('customerName', 200)" resizable show-overflow-tooltip v-if="columns.customerName.visible">
            <template #default="scope">
              <el-button link type="primary" @click="handleView(scope.row)">{{ scope.row.customerName }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="客户标签" prop="tagNames" key="tagNames" :width="colWidth('tagNames', 150)" resizable show-overflow-tooltip v-if="columns.tagNames.visible">
            <template #default="scope">
              <span v-if="scope.row.tagNames">{{ scope.row.tagNames }}</span>
              <span v-else>-</span>
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
          <el-table-column label="客户状态" prop="customerStatus" key="customerStatus" :width="colWidth('customerStatus', 100)" resizable align="center" sortable="custom" v-if="columns.customerStatus.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.customerStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.customerStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="累计交易额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 120)" resizable align="right" sortable="custom" class-name="col-num" v-if="columns.totalAmount.visible"><template #default="scope"><span class="rd-amount">￥{{ formatAmount(scope.row.totalAmount) }}</span></template></el-table-column>
          <el-table-column label="负责人" prop="userName" key="userName" :width="colWidth('userName', 100)" resizable v-if="columns.userName.visible">
            <template #default="scope">
              <span v-if="scope.row.userName">{{ scope.row.userName }}</span>
              <el-tag v-else type="warning" size="small">公海</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="部门" prop="deptName" key="deptName" :width="colWidth('deptName', 120)" resizable v-if="columns.deptName.visible" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:customer:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click" v-hasPermi="['marketing:customer:edit']">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="assign" icon="User" v-if="scope.row.userId" v-hasPermi="['marketing:customer:assign']">转移分配</el-dropdown-item>
                <el-dropdown-item command="release" icon="Coin" v-if="scope.row.userId && scope.row.customerStatus !== '0'">释放到公海</el-dropdown-item>
                <el-dropdown-item command="claim" icon="Pointer" v-if="!scope.row.userId" v-hasPermi="['marketing:customer:claim']">领取客户</el-dropdown-item>
                <el-dropdown-item command="status" icon="RefreshRight" divided>变更状态</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:customer:remove']" divided>删除</el-dropdown-item>
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
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="customerRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c_basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c_basic">
            <el-row>
              <el-col :span="12"><el-form-item label="客户编号" prop="customerNo"><el-input v-model="form.customerNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="企业名称" prop="customerName"><el-input v-model="form.customerName" placeholder="请输入企业名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="信用代码" prop="creditCode"><el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="客户等级" prop="customerLevel">
                <el-select v-model="form.customerLevel" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_customer_level" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="客户来源" prop="customerSource">
                <el-select v-model="form.customerSource" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_customer_source" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="客户状态" prop="customerStatus">
                <el-select v-model="form.customerStatus" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="客户标签" prop="tagIds">
                <el-select v-model="form.tagIds" multiple filterable clearable placeholder="请选择客户标签" style="width: 100%">
                  <el-option v-for="t in tagOptions" :key="t.tagId" :label="t.tagName" :value="t.tagId" />
                </el-select>
              </el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c_company')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/><line x1="9" y1="9" x2="9" y2="9.01"/><line x1="9" y1="12" x2="9" y2="12.01"/><line x1="9" y1="15" x2="9" y2="15.01"/><line x1="9" y1="18" x2="9" y2="18.01"/></svg></span>企业信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_company }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c_company">
            <el-row>
              <el-col :span="12"><el-form-item label="所属行业" prop="industry">
                <el-select v-model="form.industry" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_industry" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="企业规模" prop="companySize">
                <el-select v-model="form.companySize" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_company_size" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="官网" prop="website"><el-input v-model="form.website" placeholder="请输入官网地址" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c_address')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg></span>地址信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_address }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c_address">
            <el-row>
              <el-col :span="24"><el-form-item label="详细地址" prop="address"><el-input v-model="form.address" placeholder="请输入详细地址" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c_business')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>业务信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_business }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c_business">
            <el-row>
              <el-col :span="12"><el-form-item label="首次接触" prop="firstContactDate"><el-date-picker v-model="form.firstContactDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="合作开始" prop="cooperationDate"><el-date-picker v-model="form.cooperationDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="累计交易额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c_owner')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>负责信息</div>
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
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">分配客户</span>
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

    <!-- 负责人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择负责人" @confirm="onUserPickerConfirm" />
    <user-picker ref="assignUserPickerRef" title="选择负责人" @confirm="onAssignUserPickerConfirm" />
    <user-picker ref="batchAssignUserPickerRef" title="选择负责人" @confirm="onBatchAssignUserPickerConfirm" />

    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择所属部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />

    <!-- 批量分配弹窗 -->
    <el-dialog v-model="batchAssignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">批量分配客户</span>
        </div>
      </template>
      <el-alert :title="'已选中 ' + ids.length + ' 个客户'" type="info" :closable="false" class="mb8" />
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
          <span class="rd-detail-header-title">批量变更客户状态</span>
        </div>
      </template>
      <el-alert :title="'已选中 ' + ids.length + ' 个客户'" type="info" :closable="false" class="mb8" />
      <el-form label-width="80px">
        <el-form-item label="客户状态">
          <el-select v-model="batchStatusValue" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchStatus">确 定</el-button>
        <el-button @click="batchStatusOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 变更状态弹窗（单条） -->
    <el-dialog v-model="statusOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg></div>
          <span class="rd-detail-header-title">变更客户状态</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="客户状态">
          <el-select v-model="statusValue" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmStatusChange">确 定</el-button>
        <el-button @click="statusOpen = false">取 消</el-button>
      </template>
    </el-dialog>

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
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_action')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></span>状态与分配</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_action }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_action">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">当前状态</span><div class="rd-value"><dict-tag :options="marketing_customer_status" :value="viewForm.customerStatus" /></div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">状态操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleViewStatusChange('1')" v-if="viewForm.customerStatus === '0'" v-hasPermi="['marketing:customer:edit']">转为签约</el-button>
                    <el-button size="small" type="success" plain @click="handleViewStatusChange('2')" v-if="viewForm.customerStatus === '1'" v-hasPermi="['marketing:customer:edit']">转为合作中</el-button>
                    <el-button size="small" type="warning" plain @click="handleViewStatusChange('3')" v-if="viewForm.customerStatus === '2'" v-hasPermi="['marketing:customer:edit']">暂停合作</el-button>
                    <el-button size="small" type="success" plain @click="handleViewStatusChange('2')" v-if="viewForm.customerStatus === '3'" v-hasPermi="['marketing:customer:edit']">恢复合作</el-button>
                    <el-button size="small" type="danger" plain @click="handleViewStatusChange('4')" v-if="['0','1','2','3'].includes(viewForm.customerStatus)" v-hasPermi="['marketing:customer:edit']">标记流失</el-button>
                    <el-button size="small" type="primary" plain @click="handleViewStatusChange('0')" v-if="viewForm.customerStatus === '4'" v-hasPermi="['marketing:customer:edit']">重新激活</el-button></div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">分配操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleViewAssign" v-if="viewForm.userId" v-hasPermi="['marketing:customer:assign']">转移分配</el-button>
                    <el-button size="small" type="warning" plain @click="handleViewRelease" v-if="viewForm.userId" v-hasPermi="['marketing:customer:edit']">释放到公海</el-button>
                    <el-button size="small" type="success" plain @click="handleViewClaim" v-if="!viewForm.userId" v-hasPermi="['marketing:customer:claim']">领取客户</el-button></div></div>
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
    </el-dialog>

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="客户导入"
      action="/mk/customer/importData"
      template-action="/mk/customer/importTemplate"
      template-file-name="customer_template"
      update-support-label="是否更新已经存在的客户数据"
      :tips="importTips"
      :update-key-options="updateKeyOptions"
      default-update-key="customerName"
      @success="getList"
    />

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="客户管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">意向客户</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">签约客户</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">合作中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">暂停合作</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">流失客户</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="意向客户">新增客户的初始状态，表示有合作意向但尚未签约</el-descriptions-item>
          <el-descriptions-item label="签约客户">已签订合同，正式建立合作关系</el-descriptions-item>
          <el-descriptions-item label="合作中">合作进行中，有持续的订单和业务往来</el-descriptions-item>
          <el-descriptions-item label="暂停合作">暂时停止合作，可恢复为合作中</el-descriptions-item>
          <el-descriptions-item label="流失客户">客户已流失，可重新激活为意向客户</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>公海机制：</strong>释放到公海的客户无负责人，其他销售人员可领取</p>
          <p>• <strong>批量操作：</strong>支持批量分配负责人、批量变更客户状态</p>
          <p>• <strong>客户导入：</strong>支持通过Excel批量导入客户数据</p>
          <p>• <strong>客户查重：</strong>新增时系统自动检测重复企业名称</p>
          <p>• <strong>详情关联：</strong>客户详情页展示联系人、商机、合同、订单、互动记录等关联数据</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkCustomer">
import { UploadFilled, CircleClose, ArrowRight, ArrowDown, QuestionFilled, WarningFilled, Search, Filter, Edit, Delete, Download, User, Upload, RefreshRight } from '@element-plus/icons-vue'
import { listCustomer, getCustomer, addCustomer, updateCustomer, delCustomer, changeCustomerStatus, assignCustomer, releaseToPool, claimCustomer, batchAssignCustomer, batchUpdateStatus } from '@/api/mk/customer'
import { listTag } from '@/api/mk/tag'
import { listContact } from '@/api/mk/contact'
import { listOpportunity } from '@/api/mk/opportunity'
import { listContract } from '@/api/mk/contract'
import { listOrder } from '@/api/mk/order'
import { listInteraction } from '@/api/mk/interaction'
import { listParticipant } from '@/api/mk/participant'
import { listUser } from '@/api/system/user'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c_basic", "c_company", "c_address", "c_business", "c_owner", "c_other", "v_basic", "v_company", "v_business", "v_owner", "v_other", "v_action", "v_contacts", "v_opportunities", "v_contracts", "v_orders", "v_interactions", "v_activities"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_customer_index')
const { marketing_customer_level, marketing_customer_source, marketing_industry, marketing_company_size, marketing_customer_status, marketing_opportunity_status, marketing_contract_status, marketing_order_status, marketing_interaction_type, marketing_participate_status } = proxy.useDict('marketing_customer_level', 'marketing_customer_source', 'marketing_industry', 'marketing_company_size', 'marketing_customer_status', 'marketing_opportunity_status', 'marketing_contract_status', 'marketing_order_status', 'marketing_interaction_type', 'marketing_participate_status')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewForm = ref({})
const detailTab = ref('basic')
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 })
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const tagOptions = ref([])
// 详情关联数据
const contacts = ref([])
const opportunities = ref([])
const contracts = ref([])
const orders = ref([])
const interactions = ref([])
const activities = ref([])
// 分配相关
const assignOpen = ref(false)
const assignUserId = ref(null)
const assignUserName = ref(null)
const assignUserDeptId = ref(null)
const assignCustomerId = ref(null)

// 批量分配相关
const batchAssignOpen = ref(false)
const batchAssignUserId = ref(null)
const batchAssignUserName = ref(null)
const batchAssignUserDeptId = ref(null)

// 批量变更状态相关
const batchStatusOpen = ref(false)
const batchStatusValue = ref(null)

// 单条变更状态
const statusOpen = ref(false)
const statusValue = ref(null)
const statusCustomerId = ref(null)

// ===== 导入功能配置 =====
const importTips = [
  '企业名称为必填字段，不能为空',
  '客户编号由系统自动生成，无需填写',
  '客户等级、客户状态等字典字段请参考系统中已有数据填写',
  '如勾选「更新已存在数据」，需选择匹配字段：按企业名称、统一信用代码或客户编号匹配',
  '导入结果将显示每条数据的处理情况，失败数据可修正后重新导入'
]
const updateKeyOptions = [
  { value: 'customerName', label: '企业名称' },
  { value: 'creditCode', label: '统一社会信用代码' },
  { value: 'customerNo', label: '客户编号' }
]

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, customerNo: undefined, customerName: undefined, customerLevel: undefined, customerStatus: undefined, industry: undefined, customerSource: undefined, userName: undefined, params: {} },
  rules: {
    customerName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  customerNo: { label: '客户编号', visible: true },
  customerName: { label: '企业名称', visible: true },
  tagNames: { label: '客户标签', visible: true },
  customerLevel: { label: '客户等级', visible: true },
  industry: { label: '所属行业', visible: true },
  customerStatus: { label: '客户状态', visible: true },
  totalAmount: { label: '累计交易额', visible: true },
  userName: { label: '负责人', visible: true },
  deptName: { label: '部门', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_customer_columns')
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

const statusTabList = computed(() => marketing_customer_status.value)
const activeFilterCount = computed(() => {
  let count = 0
if (queryParams.value.customerNo) count++
if (queryParams.value.customerName) count++
if (queryParams.value.customerLevel) count++
if (queryParams.value.customerStatus) count++
if (queryParams.value.industry) count++
if (queryParams.value.customerSource) count++
if (queryParams.value.userName) count++
if (dateRange.value && dateRange.value.length > 0) count++
return count
})

function getList() {
  loading.value = true
  listCustomer(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false })
}
function getTagOptions() {
  listTag({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { tagOptions.value = res.rows })
}

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

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime'); getList() }
function resetQuery() {
queryParams.value.customerNo = undefined; queryParams.value.customerName = undefined; queryParams.value.customerLevel = undefined; queryParams.value.customerStatus = undefined; queryParams.value.industry = undefined; queryParams.value.customerSource = undefined; queryParams.value.userName = undefined; queryParams.value.params = {}; dateRange.value = []; activeStatusTab.value = 'all'; handleQuery()
}
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.customerStatus = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() {
  listCustomer({ pageNum: 1, pageSize: 999 }, { suppressError: true }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.customerStatus] !== undefined) counts[r.customerStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) { const map = { '0': 'tab-audit', '1': 'tab-approved', '2': 'tab-done', '3': 'tab-draft', '4': 'tab-void' }; return map[value] || '' }
function badgeClass(status) { const map = { '0': 'blue', '1': 'green', '2': 'green', '3': 'amber', '4': 'gray' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_customer_status.value.find(d => d.value == status); return item ? item.label : '-' }
function levelBadgeClass(level) { const map = { '1': 'violet', '2': 'blue', '3': 'green', '4': 'amber', '5': 'gray' }; return map[level] || 'gray' }
function levelLabel(level) { const item = marketing_customer_level.value.find(d => d.value == level); return item ? item.label : '-' }
function industryBadgeClass(industry) { const map = { '1': 'blue', '2': 'green', '3': 'violet', '4': 'amber', '5': 'cyan', '6': 'red', '7': 'gray', '8': 'orange', '9': 'pink', '10': 'indigo' }; return map[industry] || 'gray' }
function industryLabel(industry) { const item = marketing_industry.value.find(d => d.value == industry); return item ? item.label : '-' }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.customerId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { customerNo: undefined, customerName: undefined, creditCode: undefined, customerLevel: '3', customerSource: undefined, industry: undefined, companySize: undefined, address: undefined, website: undefined, customerStatus: '0', firstContactDate: undefined, cooperationDate: undefined, totalAmount: 0, userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, tagIds: [], remark: undefined }
  proxy.resetForm('customerRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增企业客户' }
function handleUpdate(row) { reset(); getCustomer(row.customerId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改企业客户' }) }
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
/** 详情页状态流转 */
function handleViewStatusChange(status) {
  proxy.$modal.confirm('确认将客户状态变更为"' + statusLabel(status) + '"？').then(() => {
    changeCustomerStatus(viewForm.value.customerId, status).then(() => {
      proxy.$modal.msgSuccess('状态变更成功')
      getCustomer(viewForm.value.customerId).then(res => { viewForm.value = res.data })
      getList()
    })
  }).catch(() => {})
}
/** 详情页分配 */
function handleViewAssign() {
  assignCustomerId.value = viewForm.value.customerId
  assignUserId.value = viewForm.value.userId || null
  assignUserName.value = viewForm.value.userName || null
  assignUserDeptId.value = viewForm.value.deptId || null
  assignOpen.value = true
}
/** 详情页释放到公海 */
function handleViewRelease() {
  proxy.$modal.confirm('确认将该客户释放到公海？释放后其他销售人员可以领取。').then(() => {
    releaseToPool(viewForm.value.customerId).then(() => {
      proxy.$modal.msgSuccess('已释放到公海')
      getCustomer(viewForm.value.customerId).then(res => { viewForm.value = res.data })
      getList()
    })
  }).catch(() => {})
}
/** 详情页领取客户 */
function handleViewClaim() {
  proxy.$modal.confirm('确认领取该客户？领取后您将成为该客户的负责人。').then(() => {
    claimCustomer(viewForm.value.customerId).then(() => {
      proxy.$modal.msgSuccess('领取成功')
      getCustomer(viewForm.value.customerId).then(res => { viewForm.value = res.data })
      getList()
    })
  }).catch(() => {})
}
function levelTagType(val) { return { '1': 'danger', '2': 'warning', '3': '', '4': 'info' }[val] || '' }
function statusTagType(val) { return { '0': 'primary', '1': 'success', '2': 'success', '3': 'warning', '4': 'info' }[val] || '' }
function submitForm() {
  proxy.$refs['customerRef'].validate(valid => {
    if (valid) {
      if (form.value.customerId != undefined) { updateCustomer(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addCustomer(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const customerIds = row.customerId || ids.value; proxy.$modal.confirm('确认删除编号为"' + customerIds + '"的数据？').then(() => delCustomer(customerIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/customer/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime') }, `customer_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ====== 操作下拉菜单 ======
function handleCommand(cmd, row) {
  if (cmd === 'assign') handleAssignSingle(row)
  else if (cmd === 'release') handleRelease(row)
  else if (cmd === 'claim') handleClaim(row)
  else if (cmd === 'status') handleStatusSingle(row)
  else if (cmd === 'delete') handleDelete(row)
}

// 分配/转移
function handleAssignSingle(row) {
  assignCustomerId.value = row.customerId
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
  assignCustomer(assignCustomerId.value, data).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getList()
  })
}

// 释放到公海
function handleRelease(row) {
  proxy.$modal.confirm('确认将"' + row.customerName + '"释放到公海？').then(() => {
    releaseToPool(row.customerId).then(() => {
      proxy.$modal.msgSuccess('已释放到公海')
      getList()
    })
  }).catch(() => {})
}

// 领取客户
function handleClaim(row) {
  proxy.$modal.confirm('确认领取"' + row.customerName + '"？').then(() => {
    claimCustomer(row.customerId).then(() => {
      proxy.$modal.msgSuccess('领取成功')
      getList()
    })
  }).catch(() => {})
}

// 变更状态（单条）
function handleStatusSingle(row) {
  statusCustomerId.value = row.customerId
  statusValue.value = row.customerStatus
  statusOpen.value = true
}
function confirmStatusChange() {
  changeCustomerStatus(statusCustomerId.value, statusValue.value).then(() => {
    proxy.$modal.msgSuccess('状态变更成功')
    statusOpen.value = false
    getList()
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
    customerIds: ids.value,
    userId: batchAssignUserId.value,
    deptId: batchAssignUserDeptId.value,
    userName: batchAssignUserName.value
  }
  batchAssignCustomer(data).then(() => {
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
    proxy.$modal.msgWarning('请选择客户状态')
    return
  }
  batchUpdateStatus({ customerIds: ids.value, customerStatus: batchStatusValue.value }).then(() => {
    proxy.$modal.msgSuccess('批量变更状态成功')
    batchStatusOpen.value = false
    getList()
  })
}

// 导入
function handleImport() { proxy.$refs['importRef'].open() }

listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
getTagOptions()
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
