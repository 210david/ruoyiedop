<template>
  <div class="app-container mk-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>经销商名称</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>经销层级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.dealerLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mk_dealer_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>档案状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mk_dealer_profile_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 经销层级由上级关系自动推导：无上级为一级，有上级为二级
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- View Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': viewMode === 'list' }" @click="viewMode = 'list'">
            <span class="dot"></span><span>档案列表</span><span class="count">{{ total }}</span>
          </button>
          <button class="status-tab" :class="{ 'is-active': viewMode === 'tree' }" @click="switchTree">
            <span class="dot"></span><span>层级树</span><span class="count">{{ treeCount }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:dealer:add']">新增档案</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:dealer:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:dealer:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:dealer:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <!-- List View -->
      <div class="table-wrap" v-show="viewMode === 'list'">
        <el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="客户编号" prop="customerNo" min-width="140" align="center" show-overflow-tooltip>
            <template #default="scope"><span class="col-mono">{{ scope.row.customerNo }}</span></template>
          </el-table-column>
          <el-table-column label="经销商名称" prop="customerName" min-width="200" show-overflow-tooltip />
          <el-table-column label="经销层级" prop="dealerLevel" width="110" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.dealerLevel === '1' ? 'warning' : ''" effect="light">{{ levelLabel(scope.row.dealerLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上级经销商" prop="parentCustomerName" min-width="160" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.parentCustomerName || '—（一级）' }}</template>
          </el-table-column>
          <el-table-column label="授权区域" prop="authRegions" min-width="150" show-overflow-tooltip />
          <el-table-column label="保证金" prop="depositAmount" width="130" align="center">
            <template #default="scope">{{ formatAmount(scope.row.depositAmount) }}</template>
          </el-table-column>
          <el-table-column label="信用状态" prop="creditStatus" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.creditStatus === '1' ? 'danger' : 'success'" effect="light">{{ scope.row.creditStatus === '1' ? '冻结' : '正常' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="本期采购额" prop="periodPurchaseAmount" width="130" align="center">
            <template #default="scope">{{ formatAmount(scope.row.periodPurchaseAmount) }}</template>
          </el-table-column>
          <el-table-column label="未结返利" prop="unsettledRebate" width="130" align="center">
            <template #default="scope">{{ formatAmount(scope.row.unsettledRebate) }}</template>
          </el-table-column>
          <el-table-column label="合作起止" width="200" align="center">
            <template #default="scope">
              <span v-if="scope.row.coopStartDate || scope.row.coopEndDate">{{ (scope.row.coopStartDate || '?') + ' ~ ' + (scope.row.coopEndDate || '?') }}</span>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" effect="light">{{ statusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="负责人" prop="userName" width="100" align="center" />
          <el-table-column label="操作" width="170" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['marketing:dealer:query']">详情</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:dealer:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:dealer:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>

      <!-- Tree View -->
      <div class="table-wrap" v-show="viewMode === 'tree'" v-loading="treeLoading">
        <div style="padding: 16px; max-width: 720px">
          <el-input v-model="treeFilterText" placeholder="输入名称过滤" clearable style="margin-bottom: 12px" />
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="{ label: 'customerName', children: 'children' }"
            node-key="customerId"
            default-expand-all
            :filter-node-method="filterTreeNode"
            highlight-current
            @node-click="onTreeNodeClick">
            <template #default="{ data }">
              <div class="tree-node">
                <el-tag size="small" :type="data.parentCustomerId ? '' : 'warning'" effect="light">{{ data.parentCustomerId ? '二级' : '一级' }}</el-tag>
                <span class="tree-node-name">{{ data.customerName }}</span>
                <span class="tree-node-no">{{ data.customerNo }}</span>
              </div>
            </template>
          </el-tree>
          <el-empty v-if="!treeLoading && treeData.length === 0" description="暂无经销商，请先在客户管理中创建经销商类型客户" />
        </div>
      </div>
    </div>

    <!-- 新增/修改档案对话框 -->
    <el-dialog v-model="open" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="dealerRef" :model="form" :rules="formRules" label-width="100px">
        <div class="rd-page">
          <!-- 卡片1：基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>
                基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="经销商" prop="customerId">
                  <el-select v-model="form.customerId" placeholder="请选择经销商类型客户" filterable :disabled="form.profileId != undefined" style="width: 100%" @change="onCustomerChange">
                    <el-option v-for="c in dealerCustomerOptions" :key="c.customerId" :label="c.customerName + '（' + (c.customerNo || '') + '）'" :value="c.customerId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="经销层级">
                  <el-tag :type="derivedLevel === '1' ? 'warning' : ''">{{ levelLabel(derivedLevel) }}</el-tag>
                  <span style="margin-left: 8px; color: var(--ink-500); font-size: 12px">由上级关系自动推导</span>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="档案状态" prop="status">
                  <el-select v-model="form.status" style="width: 100%">
                    <el-option v-for="d in mk_dealer_profile_status" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片2：授权与合作 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></span>
                授权与合作
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="授权区域" prop="authRegions"><el-input v-model="form.authRegions" placeholder="如：华东 / 江苏省" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="授权品类" prop="authCategories"><el-input v-model="form.authCategories" placeholder="授权品类说明" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="保证金" prop="depositAmount"><el-input-number v-model="form.depositAmount" :min="0" :precision="2" :controls="false" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="合作开始" prop="coopStartDate"><el-date-picker v-model="form.coopStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="合作结束" prop="coopEndDate"><el-date-picker v-model="form.coopEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片3：其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框（与编辑弹窗分组一致） -->
    <el-dialog v-model="viewOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">经销商档案详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.customerName">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ viewForm.customerName }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 卡片1：基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">经销商名称</span><div class="rd-value">{{ viewForm.customerName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">客户编号</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.customerNo }">{{ viewForm.customerNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">经销层级</span><div class="rd-value"><el-tag :type="viewForm.dealerLevel === '1' ? 'warning' : ''" effect="light">{{ levelLabel(viewForm.dealerLevel) }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">上级经销商</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.parentCustomerName }">{{ viewForm.parentCustomerName || '—（一级经销商）' }}</div></div>
              <div class="rd-item"><span class="rd-label">档案状态</span><div class="rd-value"><dict-tag :options="mk_dealer_profile_status" :value="viewForm.status" /></div></div>
              <div class="rd-item"><span class="rd-label">信用状态</span><div class="rd-value"><el-tag :type="viewForm.creditStatus === '1' ? 'danger' : 'success'" effect="light">{{ viewForm.creditStatus === '1' ? '冻结' : '正常' }}</el-tag></div></div>
            </div>
          </div>
        </section>
        <!-- 卡片2：授权与合作 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></span>
              授权与合作
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">授权区域</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.authRegions }">{{ viewForm.authRegions || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">授权品类</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.authCategories }">{{ viewForm.authCategories || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">保证金</span><div class="rd-value rd-amount">￥{{ formatAmount(viewForm.depositAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">合作开始</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.coopStartDate }">{{ viewForm.coopStartDate || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">合作结束</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.coopEndDate }">{{ viewForm.coopEndDate || '—' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 卡片3：其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>
              其他信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.userName }">{{ viewForm.userName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '—' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="handleUpdateFromView" v-hasPermi="['marketing:dealer:edit']">编 辑</el-button>
        <el-button type="primary" @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 业务说明 -->
    <el-dialog v-model="showHelp" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">经销商管理业务说明</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-body" style="display:block">
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">1</span>建档前提</div>
              <div class="help-item-desc">先在「客户管理」中创建客户类型为「经销商」的客户，再到本页建立经销商扩展档案（一户一档），同一经销商不允许重复建档。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">2</span>层级规则</div>
              <div class="help-item-desc">经销层级由客户的上级经销商关系自动推导——无上级为一级经销商，有上级为二级经销商，不支持手工维护；存在下级的经销商不允许删除档案。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">3</span>供货价</div>
              <div class="help-item-desc">经销商下单时按「经销商供货价」政策取价（按层级匹配），无供货价时回退等级价。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">4</span>流向登记</div>
              <div class="help-item-desc">经销商分销流向在「流向登记」页维护，可按发货单一键生成草稿，同一发货单同一物料防重复登记。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">5</span>档案状态</div>
              <div class="help-item-desc">合作中为正常履约状态；暂停合作期间不参与返利结算与供货价匹配；已终止为最终状态，档案保留仅作历史追溯。</div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkDealer">
import { listDealer, dealerTree, getDealer, addDealer, updateDealer, delDealer } from '@/api/mk/dealer'
import { listCustomer } from '@/api/mk/customer'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { mk_dealer_level, mk_dealer_profile_status } = proxy.useDict('mk_dealer_level', 'mk_dealer_profile_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const viewMode = ref('list')
const open = ref(false)
const title = ref('')
const dealerCustomerOptions = ref([])
const treeData = ref([])
const treeLoading = ref(false)
const treeCount = ref(0)
const treeFilterText = ref('')
const treeRef = ref()
const showHelp = ref(false)
const viewOpen = ref(false)
const viewForm = ref({})
// c0/c1/c2 = 编辑弹窗卡片，vc0/vc1/vc2 = 查看弹窗卡片
const { collapsedCards, toggleCard } = useDetailCard(["c0", "c1", "c2", "vc0", "vc1", "vc2"])

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, customerName: undefined, dealerLevel: undefined, status: undefined },
  form: {},
  rules: {
    customerId: [{ required: true, message: '请选择经销商客户', trigger: 'change' }],
    status: [{ required: true, message: '请选择档案状态', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 层级显示（表单内随所选客户推导） */
const derivedLevel = computed(() => {
  if (form.value.profileId != undefined) return form.value.dealerLevel
  const c = dealerCustomerOptions.value.find(o => o.customerId === form.value.customerId)
  return c ? (c.parentCustomerId ? '2' : '1') : '1'
})

const formRules = computed(() => rules.value)

function levelLabel(level) { const item = mk_dealer_level.value.find(d => d.value == level); return item ? item.label : '-' }
function statusLabel(status) { const item = mk_dealer_profile_status.value.find(d => d.value == status); return item ? item.label : '-' }
function formatAmount(val) { if (val == null || val === '') return '-'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

function getList() {
  loading.value = true
  listDealer(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  })
}

function switchTree() {
  viewMode.value = 'tree'
  if (treeData.value.length === 0) loadTree()
}

function loadTree() {
  treeLoading.value = true
  dealerTree({ customerName: queryParams.value.customerName }).then(res => {
    treeData.value = res.data || []
    treeCount.value = countNodes(treeData.value)
    treeLoading.value = false
  })
}

function countNodes(nodes) { return (nodes || []).reduce((sum, n) => sum + 1 + countNodes(n.children), 0) }

watch(treeFilterText, val => { treeRef.value && treeRef.value.filter(val) })
function filterTreeNode(value, data) { if (!value) return true; return (data.customerName || '').includes(value) }

/** 树节点点击 → 过滤列表定位该经销商 */
function onTreeNodeClick(data) {
  viewMode.value = 'list'
  queryParams.value.customerName = data.customerName
  getList()
}

function loadDealerCustomerOptions() {
  listCustomer({ pageNum: 1, pageSize: 9999, customerType: '2' }, { suppressError: true }).then(res => { dealerCustomerOptions.value = res.rows || [] })
}

function onCustomerChange() {}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value = { pageNum: 1, pageSize: 10, customerName: undefined, dealerLevel: undefined, status: undefined }; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.profileId); single.value = selection.length !== 1; multiple.value = !selection.length }

function reset() {
  form.value = { profileId: undefined, customerId: undefined, dealerLevel: undefined, authRegions: undefined, authCategories: undefined, depositAmount: undefined, coopStartDate: undefined, coopEndDate: undefined, status: '0', remark: undefined }
  proxy.resetForm('dealerRef')
}

function handleAdd() { reset(); loadDealerCustomerOptions(); open.value = true; title.value = '新增经销商档案' }
function handleUpdate(row) {
  reset(); loadDealerCustomerOptions()
  getDealer(row.profileId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改经销商档案' })
}
/** 查看档案详情（分组与编辑弹窗一致） */
function handleView(row) {
  getDealer(row.profileId).then(res => { viewForm.value = res.data || {}; viewOpen.value = true })
}
/** 从详情弹窗进入编辑 */
function handleUpdateFromView() {
  viewOpen.value = false
  handleUpdate({ profileId: viewForm.value.profileId })
}
function submitForm() {
  proxy.$refs['dealerRef'].validate(valid => {
    if (valid) {
      if (form.value.profileId != undefined) { updateDealer(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList(); loadTree() }) }
      else { addDealer(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList(); loadTree() }) }
    }
  })
}
function handleDelete(row) {
  const profileIds = row.profileId || ids.value
  proxy.$modal.confirm('确认删除所选经销商档案？').then(() => delDealer(profileIds)).then(() => { getList(); loadTree(); proxy.$modal.msgSuccess('删除成功') }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}
function handleExport() { proxy.download('mk/dealer/export', { ...queryParams.value }, `dealer_profile_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
</script>

<style scoped>
.tree-node { display: flex; align-items: center; gap: 8px; }
.tree-node-name { font-size: 14px; }
.tree-node-no { color: var(--ink-400); font-size: 12px; }

/* 业务说明条目 */
.help-item { padding: 10px 12px; border-radius: 8px; background: var(--bg-50, #f8fafc); border: 1px solid var(--line-200, #e2e8f0); }
.help-item + .help-item { margin-top: 10px; }
.help-item-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--ink-700, #334155); margin-bottom: 4px; }
.help-item-index { display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background: var(--brand-50, #eff6ff); color: var(--brand-500, #3b82f6); font-size: 12px; font-weight: 700; flex: none; }
.help-item-desc { font-size: 13px; line-height: 1.8; color: var(--ink-500, #64748b); padding-left: 28px; }
</style>
