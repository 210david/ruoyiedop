<template>
  <div class="app-container wms-list-page">
    <el-tabs v-model="activeTab" class="rebate-tabs">
      <!-- ==================== 页签一：返利政策 ==================== -->
      <el-tab-pane label="返利政策" name="policy">
        <!-- ===== Filter Card ===== -->
        <div class="surface filter-card" v-show="policyShowSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
            <a class="adv-link" :class="{ 'is-open': showPolicyAdvanced }" @click.prevent="showPolicyAdvanced = !showPolicyAdvanced">
              <span>{{ showPolicyAdvanced ? '收起' : '高级筛选' }}</span>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </a>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>政策编号</label>
              <div class="control">
                <el-input v-model="policyQuery.policyNo" placeholder="请输入" clearable @keyup.enter="handlePolicyQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>政策名称</label>
              <div class="control">
                <el-input v-model="policyQuery.policyName" placeholder="请输入" clearable @keyup.enter="handlePolicyQuery" />
              </div>
            </div>
            <div class="field">
              <label>客户名称</label>
              <div class="control">
                <el-input v-model="policyQuery.customerName" placeholder="请输入" clearable @keyup.enter="handlePolicyQuery" />
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control is-select">
                <el-select v-model="policyQuery.status" placeholder="全部" clearable @change="handlePolicyQuery">
                  <el-option label="启用" value="0" />
                  <el-option label="停用" value="1" />
                </el-select>
              </div>
            </div>
            <div class="field" v-show="showPolicyAdvanced">
              <label>考核口径</label>
              <div class="control is-select">
                <el-select v-model="policyQuery.basis" placeholder="全部" clearable @change="handlePolicyQuery">
                  <el-option v-for="d in mk_rebate_basis" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </div>
            </div>
            <div class="field" v-show="showPolicyAdvanced">
              <label>周期类型</label>
              <div class="control is-select">
                <el-select v-model="policyQuery.periodType" placeholder="全部" clearable @change="handlePolicyQuery">
                  <el-option v-for="d in mk_rebate_period" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info">
              <el-icon><Filter /></el-icon> 已选 {{ policyFilterCount }} 个条件，支持回车快速搜索
            </div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetPolicyQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handlePolicyQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- ===== Table Section ===== -->
        <div class="surface">
          <!-- Status Tabs -->
          <div class="status-tabs">
            <div class="tabs-track">
              <button class="status-tab" :class="{ 'is-active': activePolicyStatusTab === 'all' }" @click="handlePolicyStatusTabClick('all')">
                <span class="dot"></span><span>全部</span><span class="count">{{ policyStatusCounts.all || 0 }}</span>
              </button>
              <button class="status-tab tab-green" :class="{ 'is-active': activePolicyStatusTab === '0' }" @click="handlePolicyStatusTabClick('0')">
                <span class="dot"></span><span>启用</span><span class="count">{{ policyStatusCounts['0'] || 0 }}</span>
              </button>
              <button class="status-tab tab-gray" :class="{ 'is-active': activePolicyStatusTab === '1' }" @click="handlePolicyStatusTabClick('1')">
                <span class="dot"></span><span>停用</span><span class="count">{{ policyStatusCounts['1'] || 0 }}</span>
              </button>
            </div>
            <button class="tip-pill" @click="showPolicyHelp = true">
              <el-icon><WarningFilled /></el-icon><span>业务操作说明</span>
            </button>
          </div>

          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:rebate:add']">新增</el-button>
              <el-button type="success" plain icon="Edit" @click="handleUpdate()" v-hasPermi="['marketing:rebate:edit']">修改</el-button>
              <el-button type="danger" plain icon="Delete" @click="handleDelete()" v-hasPermi="['marketing:rebate:remove']">删除</el-button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="policyShowSearch" @queryTable="getPolicyList" :columns="policyColumns" storageKey="rebate_policy_visible" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="tableRef" v-loading="policyLoading" :data="policyList" border class="app-table" @selection-change="handlePolicySelectionChange" @header-dragend="onHeaderDragEnd">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
              <el-table-column label="政策编号" prop="policyNo" key="policyNo" :width="colWidth('policyNo', 140)" resizable v-if="policyColumns.policyNo.visible">
                <template #default="scope"><span class="mono">{{ scope.row.policyNo }}</span></template>
              </el-table-column>
              <el-table-column label="政策名称" prop="policyName" key="policyName" :min-width="colWidth('policyName', 150)" resizable show-overflow-tooltip v-if="policyColumns.policyName.visible" />
              <el-table-column label="客户名称" prop="customerName" key="customerName" :width="colWidth('customerName', 160)" resizable show-overflow-tooltip v-if="policyColumns.customerName.visible">
                <template #default="scope">
                  <span v-if="scope.row.customerName">{{ scope.row.customerName }}</span>
                  <span v-else class="text-muted">全部客户</span>
                </template>
              </el-table-column>
              <el-table-column label="考核口径" prop="basis" key="basis" :width="colWidth('basis', 100)" resizable align="center" v-if="policyColumns.basis.visible">
                <template #default="scope">
                  <span class="badge" :class="scope.row.basis === '2' ? 'green' : 'blue'"><span class="dot"></span>{{ dictLabel(mk_rebate_basis, scope.row.basis) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="周期类型" prop="periodType" key="periodType" :width="colWidth('periodType', 100)" resizable align="center" v-if="policyColumns.periodType.visible">
                <template #default="scope">
                  <span class="badge" :class="periodBadgeClass(scope.row.periodType)"><span class="dot"></span>{{ dictLabel(mk_rebate_period, scope.row.periodType) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="阶梯数" prop="tierCount" key="tierCount" :width="colWidth('tierCount', 80)" resizable align="center" v-if="policyColumns.tierCount.visible">
                <template #default="scope"><span class="mono">{{ (scope.row.tiers || []).length }}档</span></template>
              </el-table-column>
              <el-table-column label="生效日期" prop="startDate" key="startDate" :width="colWidth('startDate', 110)" resizable align="center" v-if="policyColumns.startDate.visible">
                <template #default="scope">{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</template>
              </el-table-column>
              <el-table-column label="失效日期" prop="endDate" key="endDate" :width="colWidth('endDate', 110)" resizable align="center" v-if="policyColumns.endDate.visible">
                <template #default="scope">{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</template>
              </el-table-column>
              <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="policyColumns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" :width="colWidth('操作', 160)" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button link type="primary" icon="View" @click="handlePolicyView(scope.row)">详情</el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:rebate:edit']">修改</el-button>
                    <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:rebate:remove']">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="table-pager">
            <pagination v-show="policyTotal > 0" :total="policyTotal" v-model:page="policyQuery.pageNum" v-model:limit="policyQuery.pageSize" @pagination="getPolicyList" />
          </div>
        </div>
      </el-tab-pane>

      <!-- ==================== 页签二：返利结算 ==================== -->
      <el-tab-pane label="返利结算" name="settle">
        <!-- ===== Filter Card ===== -->
        <div class="surface filter-card" v-show="settleShowSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
            <a class="adv-link" :class="{ 'is-open': showSettleAdvanced }" @click.prevent="showSettleAdvanced = !showSettleAdvanced">
              <span>{{ showSettleAdvanced ? '收起' : '高级筛选' }}</span>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </a>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>结算单号</label>
              <div class="control">
                <el-input v-model="settleQuery.settleNo" placeholder="请输入" clearable @keyup.enter="handleSettleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>客户名称</label>
              <div class="control">
                <el-input v-model="settleQuery.customerName" placeholder="请输入" clearable @keyup.enter="handleSettleQuery" />
              </div>
            </div>
            <div class="field">
              <label>期间</label>
              <div class="control is-date">
                <el-date-picker v-model="settleQuery.period" type="month" value-format="YYYYMM" placeholder="选择期间" style="width: 100%" clearable @change="handleSettleQuery" />
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control is-select">
                <el-select v-model="settleQuery.status" placeholder="全部" clearable @change="handleSettleQuery">
                  <el-option v-for="d in mk_rebate_settle_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </div>
            </div>
            <div class="field" v-show="showSettleAdvanced">
              <label>政策编号</label>
              <div class="control">
                <el-input v-model="settleQuery.policyNo" placeholder="支持模糊查询" clearable @keyup.enter="handleSettleQuery" />
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info">
              <el-icon><Filter /></el-icon> 已选 {{ settleFilterCount }} 个条件
            </div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetSettleQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleSettleQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- ===== Table Section ===== -->
        <div class="surface">
          <!-- Status Tabs -->
          <div class="status-tabs">
            <div class="tabs-track">
              <button class="status-tab" :class="{ 'is-active': activeSettleStatusTab === 'all' }" @click="handleSettleStatusTabClick('all')">
                <span class="dot"></span><span>全部</span><span class="count">{{ settleStatusCounts.all || 0 }}</span>
              </button>
              <button v-for="d in mk_rebate_settle_status" :key="d.value" class="status-tab" :class="[settleTabClass(d.value), { 'is-active': activeSettleStatusTab === d.value }]" @click="handleSettleStatusTabClick(d.value)">
                <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ settleStatusCounts[d.value] || 0 }}</span>
              </button>
            </div>
            <button class="tip-pill" @click="showSettleHelp = true">
              <el-icon><WarningFilled /></el-icon><span>业务操作说明</span>
            </button>
          </div>

          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Money" @click="handleSettle" v-hasPermi="['marketing:rebate:settle']">核算返利</el-button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="settleShowSearch" @queryTable="getSettleList" :columns="settleColumns" storageKey="rebate_settle_visible" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table v-loading="settleLoading" :data="settleList" border class="app-table" @header-dragend="onHeaderDragEnd">
              <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
              <el-table-column label="结算单号" prop="settleNo" key="settleNo" :width="colWidth('settleNo', 160)" resizable v-if="settleColumns.settleNo.visible">
                <template #default="scope"><span class="mono">{{ scope.row.settleNo }}</span></template>
              </el-table-column>
              <el-table-column label="客户名称" prop="customerName" key="customerName" :min-width="colWidth('customerName', 150)" resizable show-overflow-tooltip v-if="settleColumns.customerName.visible" />
              <el-table-column label="期间" prop="period" key="period" :width="colWidth('period', 90)" resizable align="center" v-if="settleColumns.period.visible">
                <template #default="scope"><span class="mono">{{ scope.row.period }}</span></template>
              </el-table-column>
              <el-table-column label="政策编号" prop="policyNo" key="policyNo" :width="colWidth('policyNo', 140)" resizable v-if="settleColumns.policyNo.visible">
                <template #default="scope"><span class="mono">{{ scope.row.policyNo }}</span></template>
              </el-table-column>
              <el-table-column label="政策名称" prop="policyName" key="policyName" :min-width="colWidth('policyName', 140)" resizable show-overflow-tooltip v-if="settleColumns.policyName.visible" />
              <el-table-column label="达成额" prop="achieveAmount" key="achieveAmount" :width="colWidth('achieveAmount', 120)" resizable align="right" v-if="settleColumns.achieveAmount.visible">
                <template #default="scope"><span class="mono">{{ scope.row.achieveAmount }}</span></template>
              </el-table-column>
              <el-table-column label="比例" prop="rebateRate" key="rebateRate" :width="colWidth('rebateRate', 80)" resizable align="center" v-if="settleColumns.rebateRate.visible">
                <template #default="scope"><span class="mono">{{ scope.row.rebateRate }}%</span></template>
              </el-table-column>
              <el-table-column label="返利额" prop="rebateAmount" key="rebateAmount" :width="colWidth('rebateAmount', 110)" resizable align="right" v-if="settleColumns.rebateAmount.visible">
                <template #default="scope"><span class="mono">{{ scope.row.rebateAmount }}</span></template>
              </el-table-column>
              <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="settleColumns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === '1' ? 'green' : 'amber'"><span class="dot"></span>{{ dictLabel(mk_rebate_settle_status, scope.row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="兑现方式" prop="redeemType" key="redeemType" :width="colWidth('redeemType', 100)" resizable align="center" v-if="settleColumns.redeemType.visible">
                <template #default="scope">
                  <span v-if="scope.row.redeemType">{{ scope.row.redeemType }}</span>
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column label="兑现时间" prop="redeemDate" key="redeemDate" :width="colWidth('redeemDate', 150)" resizable align="center" v-if="settleColumns.redeemDate.visible">
                <template #default="scope">{{ parseTime(scope.row.redeemDate) || '—' }}</template>
              </el-table-column>
              <el-table-column label="操作" :width="colWidth('操作', 140)" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button link type="primary" icon="View" @click="handleSettleView(scope.row)">详情</el-button>
                    <el-button v-if="scope.row.status === '0'" link type="primary" icon="Money" @click="handleRedeem(scope.row)" v-hasPermi="['marketing:rebate:redeem']">兑现</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="table-pager">
            <pagination v-show="settleTotal > 0" :total="settleTotal" v-model:page="settleQuery.pageNum" v-model:limit="settleQuery.pageSize" @pagination="getSettleList" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ===== 政策弹框（新增/修改） ===== -->
    <el-dialog v-model="policyOpen" width="780px" append-to-body draggable class="rd-dialog" @closed="cancelPolicy">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ policyTitle }}</span>
        </div>
      </template>
      <el-form ref="policyRef" :model="policyForm" :rules="policyRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.83z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="政策名称" prop="policyName">
                    <el-input v-model="policyForm.policyName" placeholder="请输入政策名称" maxlength="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户" prop="customerId">
                    <el-input :model-value="policyForm.customerName" readonly placeholder="点击选择客户，留空=全部客户" @click="openPolicyCustomerPicker">
                      <template v-if="policyForm.customerId" #append><el-button icon="CircleClose" @click.stop="clearPolicyCustomer" /></template>
                      <template v-else #append><el-button icon="Search" @click="openPolicyCustomerPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>考核规则</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="考核口径" prop="basis">
                    <el-radio-group v-model="policyForm.basis">
                      <el-radio value="1">按金额</el-radio>
                      <el-radio value="2">按数量</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="周期类型" prop="periodType">
                    <el-radio-group v-model="policyForm.periodType">
                      <el-radio value="1">月</el-radio>
                      <el-radio value="2">季</el-radio>
                      <el-radio value="3">年</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>有效期与状态</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="生效日期" prop="startDate">
                    <el-date-picker v-model="policyForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="失效日期" prop="endDate">
                    <el-date-picker v-model="policyForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择失效日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="policyForm.status">
                      <el-radio value="0">启用</el-radio>
                      <el-radio value="1">停用</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>阶梯明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <div class="tier-bar">
                <el-button type="primary" plain icon="Plus" size="small" @click="addTier">添加阶梯</el-button>
                <span class="form-tip">阶梯上限留空表示不封顶；各档区间不可重叠</span>
              </div>
              <el-table :data="policyForm.tiers" border size="small">
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column label="阶梯下限（含）" min-width="150">
                  <template #default="{ row }">
                    <el-input-number v-model="row.tierMin" :min="0" :precision="2" controls-position="right" style="width: 100%" placeholder="必填" />
                  </template>
                </el-table-column>
                <el-table-column label="阶梯上限（含）" min-width="150">
                  <template #default="{ row }">
                    <el-input-number v-model="row.tierMax" :min="0" :precision="2" controls-position="right" style="width: 100%" placeholder="不封顶" />
                  </template>
                </el-table-column>
                <el-table-column label="返利比例%" min-width="130">
                  <template #default="{ row }">
                    <el-input-number v-model="row.rebateRate" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" placeholder="返利比例" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70" align="center">
                  <template #default="{ $index }">
                    <el-button link type="danger" icon="Delete" @click="removeTier($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitPolicy">确 定</el-button>
          <el-button @click="cancelPolicy">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 核算返利弹框 ===== -->
    <el-dialog v-model="settleOpen" width="560px" append-to-body draggable class="rd-dialog" @closed="cancelSettle">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v12"/><path d="M15 9.5c0-.83-1.34-1.5-3-1.5s-3 .67-3 1.5 1.34 1.5 3 1.5 3 .67 3 1.5-1.34 1.5-3 1.5-3-.67-3-1.5"/></svg></div>
          <span class="rd-detail-header-title">核算返利</span>
        </div>
      </template>
      <el-form ref="settleRef" :model="settleForm" :rules="settleRules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-input :model-value="settleForm.customerName" readonly placeholder="点击选择客户" @click="openSettleCustomerPicker">
            <template v-if="settleForm.customerId" #append><el-button icon="CircleClose" @click.stop="clearSettleCustomer" /></template>
            <template v-else #append><el-button icon="Search" @click="openSettleCustomerPicker" /></template>
          </el-input>
        </el-form-item>
        <el-form-item label="核算期间" prop="period">
          <el-date-picker v-model="settleForm.period" type="month" value-format="YYYYMM" placeholder="选择核算期间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitSettle">开始核算</el-button>
          <el-button @click="cancelSettle">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 核算结果弹框 ===== -->
    <el-dialog v-model="resultOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><polyline points="9 15 11 17 15 13"/></svg></div>
          <span class="rd-detail-header-title">核算结果</span>
        </div>
      </template>
      <div class="form-tip" style="margin-bottom: 10px">本次核算生成结算单 <b>{{ settleResultList.length }}</b> 张</div>
      <el-table :data="settleResultList" border size="small" max-height="380">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="结算单号" prop="settleNo" width="160">
          <template #default="scope"><span class="mono">{{ scope.row.settleNo }}</span></template>
        </el-table-column>
        <el-table-column label="政策名称" prop="policyName" min-width="140" show-overflow-tooltip />
        <el-table-column label="达成额" prop="achieveAmount" width="120" align="right">
          <template #default="scope"><span class="mono">{{ scope.row.achieveAmount }}</span></template>
        </el-table-column>
        <el-table-column label="比例" prop="rebateRate" width="80" align="center">
          <template #default="scope"><span class="mono">{{ scope.row.rebateRate }}%</span></template>
        </el-table-column>
        <el-table-column label="返利额" prop="rebateAmount" width="110" align="right">
          <template #default="scope"><span class="mono">{{ scope.row.rebateAmount }}</span></template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resultOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 政策详情弹窗 ===== -->
    <el-dialog v-model="policyViewOpen" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">返利政策详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-title" style="padding: 12px 16px 0">基本信息</div>
          <el-descriptions :column="2" border class="rd-desc" style="margin: 10px 16px 16px">
            <el-descriptions-item label="政策编号"><span class="mono">{{ policyView.policyNo }}</span></el-descriptions-item>
            <el-descriptions-item label="政策名称">{{ policyView.policyName }}</el-descriptions-item>
            <el-descriptions-item label="客户名称">{{ policyView.customerName || '全部客户' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <span class="badge" :class="policyView.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ policyView.status === '0' ? '启用' : '停用' }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </section>
        <section class="rd-card">
          <div class="rd-card-title" style="padding: 12px 16px 0">考核规则</div>
          <el-descriptions :column="2" border class="rd-desc" style="margin: 10px 16px 16px">
            <el-descriptions-item label="考核口径"><dict-tag :options="mk_rebate_basis" :value="policyView.basis" /></el-descriptions-item>
            <el-descriptions-item label="周期类型"><dict-tag :options="mk_rebate_period" :value="policyView.periodType" /></el-descriptions-item>
          </el-descriptions>
        </section>
        <section class="rd-card">
          <div class="rd-card-title" style="padding: 12px 16px 0">返利阶梯</div>
          <el-table :data="policyView.tiers || []" border size="small" style="margin: 10px 16px 16px; width: calc(100% - 32px)">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="阶梯下限（含）" prop="tierMin" align="right"><template #default="s"><span class="mono">{{ s.row.tierMin }}</span></template></el-table-column>
            <el-table-column label="阶梯上限（含）" align="right"><template #default="s"><span class="mono">{{ s.row.tierMax == null ? '不封顶' : s.row.tierMax }}</span></template></el-table-column>
            <el-table-column label="返利比例%" prop="rebateRate" align="right"><template #default="s"><span class="mono">{{ s.row.rebateRate }}%</span></template></el-table-column>
          </el-table>
        </section>
        <section class="rd-card">
          <div class="rd-card-title" style="padding: 12px 16px 0">有效期与状态</div>
          <el-descriptions :column="2" border class="rd-desc" style="margin: 10px 16px 16px">
            <el-descriptions-item label="生效日期">{{ parseTime(policyView.startDate, '{y}-{m}-{d}') || '—' }}</el-descriptions-item>
            <el-descriptions-item label="失效日期">{{ parseTime(policyView.endDate, '{y}-{m}-{d}') || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ policyView.createBy || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ parseTime(policyView.createTime) || '—' }}</el-descriptions-item>
          </el-descriptions>
        </section>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" icon="Edit" @click="policyViewOpen = false; handleUpdate(policyView)" v-hasPermi="['marketing:rebate:edit']">编 辑</el-button>
          <el-button @click="policyViewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 结算详情弹窗 ===== -->
    <el-dialog v-model="settleViewOpen" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">返利结算详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-title" style="padding: 12px 16px 0">结算信息</div>
          <el-descriptions :column="2" border class="rd-desc" style="margin: 10px 16px 16px">
            <el-descriptions-item label="结算单号"><span class="mono">{{ settleView.settleNo }}</span></el-descriptions-item>
            <el-descriptions-item label="核算期间"><span class="mono">{{ settleView.period }}</span></el-descriptions-item>
            <el-descriptions-item label="状态">
              <span class="badge" :class="settleView.status === '1' ? 'green' : 'amber'"><span class="dot"></span>{{ dictLabel(mk_rebate_settle_status, settleView.status) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ parseTime(settleView.createTime) || '—' }}</el-descriptions-item>
          </el-descriptions>
        </section>
        <section class="rd-card">
          <div class="rd-card-title" style="padding: 12px 16px 0">客户与政策</div>
          <el-descriptions :column="2" border class="rd-desc" style="margin: 10px 16px 16px">
            <el-descriptions-item label="客户名称">{{ settleView.customerName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="政策编号"><span class="mono">{{ settleView.policyNo || '—' }}</span></el-descriptions-item>
            <el-descriptions-item label="政策名称" :span="2">{{ settleView.policyName || '—' }}</el-descriptions-item>
          </el-descriptions>
        </section>
        <section class="rd-card">
          <div class="rd-card-title" style="padding: 12px 16px 0">返利计算</div>
          <el-descriptions :column="3" border class="rd-desc" style="margin: 10px 16px 16px">
            <el-descriptions-item label="达成额"><span class="mono">{{ settleView.achieveAmount }}</span></el-descriptions-item>
            <el-descriptions-item label="返利比例"><span class="mono">{{ settleView.rebateRate }}%</span></el-descriptions-item>
            <el-descriptions-item label="返利额"><span class="mono">{{ settleView.rebateAmount }}</span></el-descriptions-item>
          </el-descriptions>
        </section>
        <section class="rd-card">
          <div class="rd-card-title" style="padding: 12px 16px 0">兑现信息</div>
          <el-descriptions :column="2" border class="rd-desc" style="margin: 10px 16px 16px">
            <el-descriptions-item label="兑现方式">{{ settleView.redeemType || '未兑现' }}</el-descriptions-item>
            <el-descriptions-item label="兑现时间">{{ parseTime(settleView.redeemDate) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="兑现操作人">{{ settleView.redeemBy || '—' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ settleView.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </section>
      </div>
      <template #footer>
        <div class="dialog-footer"><el-button @click="settleViewOpen = false">关 闭</el-button></div>
      </template>
    </el-dialog>

    <!-- ===== 政策业务操作说明 ===== -->
    <el-dialog v-model="showPolicyHelp" title="返利政策 - 业务操作说明" width="760px" append-to-body class="rd-dialog">
      <div class="help-body">
        <h4>功能定位</h4>
        <p>返利政策定义"达成即返"的销售激励规则：按周期考核客户的进货金额或数量，达到阶梯档位即按比例计提返利。是快消/食品行业渠道压货、大客户锁量的核心手段。</p>
        <h4>政策要素</h4>
        <ul>
          <li><b>考核口径</b>：按金额（元）或按数量（件/箱）</li>
          <li><b>周期类型</b>：月 / 季 / 年，决定达成额的统计周期</li>
          <li><b>返利阶梯</b>：多档区间递进，如月进货 10~20 万返 1%、20 万以上返 2%；上限留空=不封顶</li>
          <li><b>适用客户</b>：可指定单一客户，或留空适用全部客户</li>
        </ul>
        <h4>状态说明</h4>
        <ul>
          <li><b>启用</b>：参与返利核算（需在有效期内）</li>
          <li><b>停用</b>：暂停使用，不再参与核算</li>
        </ul>
        <h4>重点业务规则</h4>
        <ul>
          <li>同一客户同一期间命中多条政策时按各政策分别核算</li>
          <li>阶梯区间不可重叠；下限含本值，上限含本值</li>
          <li>政策整组管理：修改/删除均作用于整组阶梯</li>
        </ul>
        <h4>操作流程</h4>
        <p>新增政策（基本信息 → 考核规则 → 有效期 → 阶梯明细）→ 启用 → 到"返利结算"页签按客户+期间核算 → 生成结算单 → 兑现</p>
      </div>
      <template #footer>
        <div class="dialog-footer"><el-button @click="showPolicyHelp = false">关 闭</el-button></div>
      </template>
    </el-dialog>

    <!-- ===== 结算业务操作说明 ===== -->
    <el-dialog v-model="showSettleHelp" title="返利结算 - 业务操作说明" width="760px" append-to-body class="rd-dialog">
      <div class="help-body">
        <h4>功能定位</h4>
        <p>按"客户 + 核算期间"自动计算返利：系统统计该客户期间内已完成订单的金额/数量，匹配启用的返利政策阶梯，生成返利结算单。</p>
        <h4>状态流转</h4>
        <ul>
          <li><b>待兑现</b>：核算生成，返利额已确认，尚未支付给客户</li>
          <li><b>已兑现</b>：已按兑现方式（抵货款/现金等）支付，终态留痕</li>
        </ul>
        <h4>重点业务规则</h4>
        <ul>
          <li>达成额统计口径：已完成/已发货/已回款状态订单之和（与政策考核口径对应金额或数量）</li>
          <li>同一客户+同一期间+同一政策只生成一张结算单，重复核算自动跳过</li>
          <li>兑现时必须填写兑现方式，操作人+时间自动留痕</li>
        </ul>
        <h4>操作流程</h4>
        <p>核算返利（选客户 → 选期间 → 开始核算）→ 查看核算结果 → 确认结算单 → 对待兑现单执行兑现（填兑现方式）→ 完成</p>
      </div>
      <template #footer>
        <div class="dialog-footer"><el-button @click="showSettleHelp = false">关 闭</el-button></div>
      </template>
    </el-dialog>

    <!-- 客户选择器 -->
    <customer-picker ref="customerPickerRef" title="选择客户" @confirm="onCustomerPickerConfirm" />
  </div>
</template>

<script setup name="MkRebate">
import { listPolicyGroups, addPolicyGroup, updatePolicyGroup, delPolicyGroup, listSettles, settleRebate, redeemSettle } from "@/api/mk/rebate"
import CustomerPicker from '@/components/CustomerPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2', 'c3', 'c4'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('rebate_columns')
const { mk_rebate_basis, mk_rebate_period, mk_rebate_settle_status } = proxy.useDict("mk_rebate_basis", "mk_rebate_period", "mk_rebate_settle_status")

const activeTab = ref('policy')
const customerPickerRef = ref(null)
const pickerTarget = ref('policy')

// ===== 返利政策 =====
const policyList = ref([])
const policyLoading = ref(true)
const policyShowSearch = ref(true)
const policyIds = ref([])
const policyTotal = ref(0)
const policyOpen = ref(false)
const policyTitle = ref("")
// 列显隐配置（RightToolbar 按 storageKey 自动恢复/持久化）
const policyColumns = ref({
  policyNo: { label: '政策编号', visible: true },
  policyName: { label: '政策名称', visible: true },
  customerName: { label: '客户名称', visible: true },
  basis: { label: '考核口径', visible: true },
  periodType: { label: '周期类型', visible: true },
  tierCount: { label: '阶梯数', visible: true },
  startDate: { label: '生效日期', visible: true },
  endDate: { label: '失效日期', visible: true },
  status: { label: '状态', visible: true }
})

// ===== 返利结算 =====
const settleList = ref([])
const settleLoading = ref(true)
const settleShowSearch = ref(true)
const settleTotal = ref(0)
const settleOpen = ref(false)
const resultOpen = ref(false)
const settleResultList = ref([])
const settleColumns = ref({
  settleNo: { label: '结算单号', visible: true },
  customerName: { label: '客户名称', visible: true },
  period: { label: '期间', visible: true },
  policyNo: { label: '政策编号', visible: true },
  policyName: { label: '政策名称', visible: true },
  achieveAmount: { label: '达成额', visible: true },
  rebateRate: { label: '比例', visible: true },
  rebateAmount: { label: '返利额', visible: true },
  status: { label: '状态', visible: true },
  redeemType: { label: '兑现方式', visible: true },
  redeemDate: { label: '兑现时间', visible: true }
})

// ===== 高级筛选 / 状态卡片 / 业务说明 / 详情弹窗 =====
const showPolicyAdvanced = ref(false)
const showSettleAdvanced = ref(false)
const activePolicyStatusTab = ref('all')
const activeSettleStatusTab = ref('all')
const policyStatusCounts = ref({ all: 0, '0': 0, '1': 0 })
const settleStatusCounts = ref({ all: 0 })
const showPolicyHelp = ref(false)
const showSettleHelp = ref(false)
const policyViewOpen = ref(false)
const policyView = ref({})
const settleViewOpen = ref(false)
const settleView = ref({})

const data = reactive({
  policyQuery: { pageNum: 1, pageSize: 10, policyNo: undefined, policyName: undefined, customerName: undefined, basis: undefined, periodType: undefined, status: undefined },
  settleQuery: { pageNum: 1, pageSize: 10, settleNo: undefined, customerName: undefined, period: undefined, policyNo: undefined, status: undefined },
  policyForm: {},
  settleForm: {}
})
const { policyQuery, settleQuery, policyForm, settleForm } = toRefs(data)

const policyRules = {
  policyName: [{ required: true, message: "政策名称不能为空", trigger: "blur" }],
  basis: [{ required: true, message: "请选择考核口径", trigger: "change" }],
  periodType: [{ required: true, message: "请选择周期类型", trigger: "change" }],
  startDate: [{ required: true, message: "生效日期不能为空", trigger: "change" }],
  endDate: [{ required: true, message: "失效日期不能为空", trigger: "change" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }]
}
const settleRules = {
  customerId: [{ required: true, message: "请选择客户", trigger: "change" }],
  period: [{ required: true, message: "核算期间不能为空", trigger: "change" }]
}

const policyFilterCount = computed(() => [policyQuery.value.policyNo, policyQuery.value.policyName, policyQuery.value.customerName, policyQuery.value.basis, policyQuery.value.periodType, policyQuery.value.status].filter(v => v != null && v !== '').length)
const settleFilterCount = computed(() => [settleQuery.value.settleNo, settleQuery.value.customerName, settleQuery.value.period, settleQuery.value.policyNo, settleQuery.value.status].filter(v => v != null && v !== '').length)

// ==================== 状态卡片 / 说明 / 详情 ====================
function handlePolicyStatusTabClick(status) {
  activePolicyStatusTab.value = status
  policyQuery.value.status = status === 'all' ? undefined : status
  handlePolicyQuery()
}
function handleSettleStatusTabClick(status) {
  activeSettleStatusTab.value = status
  settleQuery.value.status = status === 'all' ? undefined : status
  handleSettleQuery()
}
function settleTabClass(v) { return v === '1' ? 'tab-green' : 'tab-draft' }

function loadPolicyStatusCounts() {
  listPolicyGroups({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    policyStatusCounts.value = counts
  }).catch(() => {})
}
function loadSettleStatusCounts() {
  listSettles({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    ;(res.rows || []).forEach(r => { counts[r.status] = (counts[r.status] || 0) + 1 })
    settleStatusCounts.value = counts
  }).catch(() => {})
}

/** 政策详情（列表行已带整组 tiers） */
function handlePolicyView(row) {
  policyView.value = { ...row, tiers: row.tiers || [] }
  policyViewOpen.value = true
}
/** 结算详情 */
function handleSettleView(row) {
  settleView.value = { ...row }
  settleViewOpen.value = true
}

// ==================== 返利政策 ====================
function getPolicyList() {
  policyLoading.value = true
  listPolicyGroups(policyQuery.value).then(response => {
    policyList.value = response.rows
    policyTotal.value = response.total
    policyLoading.value = false
    applySavedWidths()
    loadPolicyStatusCounts()
  })
}

function handlePolicyQuery() {
  policyQuery.value.pageNum = 1
  getPolicyList()
}

function resetPolicyQuery() {
  policyQuery.value.policyNo = undefined
  policyQuery.value.policyName = undefined
  policyQuery.value.customerName = undefined
  policyQuery.value.basis = undefined
  policyQuery.value.periodType = undefined
  policyQuery.value.status = undefined
  handlePolicyQuery()
}

function handlePolicySelectionChange(selection) {
  policyIds.value = selection.map(item => item.policyNo)
}

/** 表单重置 */
function resetPolicy() {
  data.policyForm = {
    policyNo: null, policyName: undefined, customerId: null, customerName: undefined,
    basis: '1', periodType: '1', startDate: undefined, endDate: undefined, status: '0',
    tiers: [{ tierMin: undefined, tierMax: undefined, rebateRate: undefined }]
  }
  proxy.resetForm("policyRef")
}

function handleAdd() {
  resetPolicy()
  policyTitle.value = "新增返利政策"
  policyOpen.value = true
}

function handleUpdate(row) {
  let target = row
  if (!target) {
    if (policyIds.value.length === 0) {
      proxy.$modal.msgWarning("请选择一条政策记录")
      return
    }
    if (policyIds.value.length > 1) {
      proxy.$modal.msgWarning("只能选择一条政策记录进行修改")
      return
    }
    target = policyList.value.find(g => g.policyNo === policyIds.value[0])
  }
  if (!target) return
  resetPolicy()
  data.policyForm = {
    policyNo: target.policyNo,
    policyName: target.policyName,
    customerId: target.customerId,
    customerName: target.customerName,
    basis: target.basis || '1',
    periodType: target.periodType || '1',
    startDate: target.startDate,
    endDate: target.endDate,
    status: target.status || '0',
    tiers: (target.tiers || []).map(t => ({ tierMin: t.tierMin, tierMax: t.tierMax, rebateRate: t.rebateRate }))
  }
  policyTitle.value = "修改返利政策"
  policyOpen.value = true
}

function cancelPolicy() {
  policyOpen.value = false
  resetPolicy()
}

function handleDelete(row) {
  const nos = row ? [row.policyNo] : policyIds.value
  if (!nos || nos.length === 0) {
    proxy.$modal.msgWarning("请选择要删除的政策")
    return
  }
  proxy.$modal.confirm('是否确认删除政策编号为"' + nos.join('、') + '"的返利政策（整组）？').then(() => {
    return Promise.all(nos.map(no => delPolicyGroup(no)))
  }).then(() => {
    getPolicyList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 阶梯行操作 */
function addTier() {
  policyForm.value.tiers.push({ tierMin: undefined, tierMax: undefined, rebateRate: undefined })
}
function removeTier(index) {
  policyForm.value.tiers.splice(index, 1)
}

/** 阶梯校验：min<=max、区间不重叠 */
function validateTiers() {
  const tiers = policyForm.value.tiers || []
  if (tiers.length === 0) {
    proxy.$modal.msgWarning("请至少添加一条返利阶梯")
    return false
  }
  for (let i = 0; i < tiers.length; i++) {
    const t = tiers[i]
    if (t.tierMin == null) {
      proxy.$modal.msgWarning("第" + (i + 1) + "行阶梯下限不能为空")
      return false
    }
    if (t.rebateRate == null) {
      proxy.$modal.msgWarning("第" + (i + 1) + "行返利比例不能为空")
      return false
    }
    if (t.tierMax != null && Number(t.tierMin) > Number(t.tierMax)) {
      proxy.$modal.msgWarning("第" + (i + 1) + "行阶梯下限不能大于上限")
      return false
    }
  }
  const sorted = [...tiers].sort((a, b) => Number(a.tierMin) - Number(b.tierMin))
  for (let i = 1; i < sorted.length; i++) {
    const prev = sorted[i - 1]
    const curr = sorted[i]
    if (prev.tierMax == null || Number(curr.tierMin) <= Number(prev.tierMax)) {
      proxy.$modal.msgWarning("阶梯区间存在重叠，请调整各档上下限")
      return false
    }
  }
  return true
}

function submitPolicy() {
  proxy.$refs["policyRef"].validate(valid => {
    if (!valid) return
    if (!validateTiers()) return
    const head = policyForm.value
    const payload = head.tiers.map(t => ({
      policyNo: head.policyNo || undefined,
      policyName: head.policyName,
      customerId: head.customerId,
      customerName: head.customerName,
      basis: head.basis,
      periodType: head.periodType,
      tierMin: t.tierMin,
      tierMax: t.tierMax == null ? null : t.tierMax,
      rebateRate: t.rebateRate,
      startDate: head.startDate,
      endDate: head.endDate,
      status: head.status
    }))
    const request = head.policyNo != null ? updatePolicyGroup(payload) : addPolicyGroup(payload)
    request.then(() => {
      proxy.$modal.msgSuccess(head.policyNo != null ? "修改成功" : "新增成功")
      policyOpen.value = false
      getPolicyList()
    })
  })
}

// ==================== 返利结算 ====================
function getSettleList() {
  settleLoading.value = true
  listSettles(settleQuery.value).then(response => {
    settleList.value = response.rows
    settleTotal.value = response.total
    settleLoading.value = false
    applySavedWidths()
    loadSettleStatusCounts()
  })
}

function handleSettleQuery() {
  settleQuery.value.pageNum = 1
  getSettleList()
}

function resetSettleQuery() {
  settleQuery.value.settleNo = undefined
  settleQuery.value.customerName = undefined
  settleQuery.value.period = undefined
  settleQuery.value.policyNo = undefined
  settleQuery.value.status = undefined
  handleSettleQuery()
}

/** 核算返利 */
function handleSettle() {
  data.settleForm = { customerId: null, customerName: undefined, period: undefined }
  proxy.resetForm("settleRef")
  settleOpen.value = true
}

function cancelSettle() {
  settleOpen.value = false
  data.settleForm = { customerId: null, customerName: undefined, period: undefined }
  proxy.resetForm("settleRef")
}

function submitSettle() {
  proxy.$refs["settleRef"].validate(valid => {
    if (!valid) return
    settleRebate({ customerId: settleForm.value.customerId, period: settleForm.value.period }).then(res => {
      settleOpen.value = false
      proxy.$modal.msgSuccess(res.msg || "核算完成")
      settleResultList.value = res.data || []
      resultOpen.value = true
      getSettleList()
    })
  })
}

/** 兑现（仅待兑现） */
function handleRedeem(row) {
  proxy.$prompt('请输入兑现方式（如：抵货款/现金）', '返利兑现', {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "兑现方式（必填）",
    inputPattern: /\S+/,
    inputErrorMessage: "兑现方式不能为空"
  }).then(({ value }) => {
    return redeemSettle(row.settleId, value.trim())
  }).then(() => {
    proxy.$modal.msgSuccess("兑现成功")
    getSettleList()
  }).catch(() => {})
}

// ==================== 客户选择器 ====================
function openPolicyCustomerPicker() {
  pickerTarget.value = 'policy'
  customerPickerRef.value.open(policyForm.value.customerId || undefined)
}
function openSettleCustomerPicker() {
  pickerTarget.value = 'settle'
  customerPickerRef.value.open(settleForm.value.customerId || undefined)
}
function clearPolicyCustomer() {
  policyForm.value.customerId = null
  policyForm.value.customerName = undefined
}
function clearSettleCustomer() {
  settleForm.value.customerId = null
  settleForm.value.customerName = undefined
}
function onCustomerPickerConfirm(customer) {
  if (pickerTarget.value === 'settle') {
    settleForm.value.customerId = customer.customerId
    settleForm.value.customerName = customer.customerName
  } else {
    policyForm.value.customerId = customer.customerId
    policyForm.value.customerName = customer.customerName
  }
}

/** 工具函数 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}
function periodBadgeClass(v) {
  if (v === '2') return 'green'
  if (v === '3') return 'amber'
  return 'blue'
}

onMounted(() => {
  getPolicyList()
  getSettleList()
})
</script>

<style scoped>
.rebate-tabs :deep(.el-tabs__header) { margin-bottom: 12px; }
.tier-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.form-tip { font-size: 12px; color: #909399; line-height: 1.6; }
.mono { font-family: "JetBrains Mono", Consolas, monospace; font-size: 12px; }

/* 状态卡片颜色 */
.wms-list-page .status-tab.tab-green .dot { background: var(--green-500); }
.wms-list-page .status-tab.tab-green .count { background: var(--green-50); color: var(--green-700); }
.wms-list-page .status-tab.is-active.tab-green .count { background: var(--green-500); color: #fff; }
.wms-list-page .status-tab.tab-draft .dot { background: var(--amber-500); }
.wms-list-page .status-tab.tab-draft .count { background: var(--amber-50); color: var(--amber-700); }
.wms-list-page .status-tab.tab-gray .dot { background: var(--ink-400); }
.wms-list-page .status-tab.tab-gray .count { background: var(--ink-100); color: var(--ink-500); }
.wms-list-page .status-tab.is-active.tab-gray .count { background: var(--ink-400); color: #fff; }

/* 业务操作说明 */
.help-body h4 { margin: 14px 0 6px; color: #303133; font-size: 14px; }
.help-body h4:first-child { margin-top: 0; }
.help-body p, .help-body li { font-size: 13px; color: #606266; line-height: 1.8; }
.help-body ul { margin: 0; padding-left: 20px; }
</style>
