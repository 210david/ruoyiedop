﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="app-container mk-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>企业名称</label>
          <div class="control">
            <el-input v-model="queryParams.companyName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>线索等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.leadGrade" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_lead_grade" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>审批状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.receiveStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_lead_receive_status" :key="d.value" :label="d.label" :value="d.value" />
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
      <!-- Status Tabs + Tip Pill -->
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
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_lead_pool_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column label="线索编号" prop="leadNo" key="leadNo" :width="colWidth('leadNo', 150)" resizable v-if="columns.leadNo.visible" />
          <el-table-column label="企业名称" prop="companyName" key="companyName" :width="colWidth('companyName', 200)" resizable show-overflow-tooltip v-if="columns.companyName.visible" />
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
          <el-table-column label="审批状态" prop="receiveStatus" key="receiveStatus" :width="colWidth('receiveStatus', 100)" resizable align="center" v-if="columns.receiveStatus.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.receiveStatus || '0')"><span class="dot"></span>{{ statusLabel(scope.row.receiveStatus || '0') }}</span></template>
          </el-table-column>
          <el-table-column label="申请人" prop="receiveApplyUserName" key="receiveApplyUserName" :width="colWidth('receiveApplyUserName', 100)" resizable v-if="columns.receiveApplyUserName.visible" />
          <el-table-column label="申请时间" prop="receiveApplyTime" key="receiveApplyTime" :width="colWidth('receiveApplyTime', 160)" resizable sortable="custom" v-if="columns.receiveApplyTime.visible" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
              <el-button link type="success" icon="Pointer" @click="handleReceive(scope.row)" v-hasPermi="['marketing:lead:pool:receive']" v-if="!scope.row.receiveStatus || scope.row.receiveStatus === '0' || scope.row.receiveStatus === '3'">申请领取</el-button>
              <el-button link type="warning" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['marketing:lead:pool:approve']" v-if="scope.row.receiveStatus === '1'">审批</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

        <!-- 线索详情弹窗 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog" @open="loadViewRelations">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></div>
          <span class="rd-detail-header-title">线索详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.leadNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.leadNo }}</span>
            <dict-tag :options="marketing_lead_status" :value="viewForm.leadStatus" />
            <el-tag type="warning" size="small" v-if="viewForm.isPublic === '1'">公海</el-tag>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_lead')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>线索信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_lead }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_lead">
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
              <div class="rd-card-header" @click="toggleCard('v_company')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></span>企业信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_company }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_company">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="viewForm.industry" /></div></div>
                  <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="viewForm.companySize" /></div></div>
                  <div class="rd-item"><span class="rd-label">线索来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="viewForm.leadSource" /></div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.address }">{{ viewForm.address || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_requirement')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>需求与评分</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_requirement }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_requirement">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">线索评分</span><div class="rd-value">{{ viewForm.leadScore }}</div></div>
                  <div class="rd-item"><span class="rd-label">线索等级</span><div class="rd-value"><dict-tag :options="marketing_lead_grade" :value="viewForm.leadGrade" /></div></div>
                  <div class="rd-item"><span class="rd-label">线索状态</span><div class="rd-value"><dict-tag :options="marketing_lead_status" :value="viewForm.leadStatus" /></div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">需求描述</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.requirementDesc }">{{ viewForm.requirementDesc || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_receive')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></span>领取审批信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_receive }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_receive">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">审批状态</span><div class="rd-value"><dict-tag :options="marketing_lead_receive_status" :value="viewForm.receiveStatus || '0'" /></div></div>
                  <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.receiveApplyUserName }">{{ viewForm.receiveApplyUserName || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">申请时间</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.receiveApplyTime }">{{ viewForm.receiveApplyTime || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.receiveApproveUserName }">{{ viewForm.receiveApproveUserName || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">审批时间</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.receiveApproveTime }">{{ viewForm.receiveApproveTime || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">审批备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.receiveRemark }">{{ viewForm.receiveRemark || '暂无' }}</div></div>
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
          <el-tab-pane :label="`跟进记录 (${interactionList.length})`" name="interactions">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_interactions')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></span>跟进记录</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_interactions }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_interactions">
                <div class="rd-timeline" v-if="interactionList.length > 0">
                  <div class="rd-timeline-item" v-for="item in interactionList" :key="item.recordId">
                    <div class="rd-timeline-dot rd-timeline-dot--success"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title"><el-tag size="small" type="info">{{ getInteractTypeLabel(item.interactType) }}</el-tag> {{ item.userName }}</span>
                        <span class="rd-timeline-time">{{ item.interactTime }}</span>
                      </div>
                      <div class="rd-timeline-comment">{{ item.content }}</div>
                      <div v-if="item.nextTime" style="margin-top: 6px; font-size: 12px; color: #e6a23c">下次跟进: {{ item.nextTime }} {{ item.nextContent ? '- ' + item.nextContent : '' }}</div>
                    </div>
                  </div>
                </div>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg><p class="rd-empty-text">暂无跟进记录</p></div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane label="时间线" name="timeline">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('v_timeline')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><path d="M12 7v5l4 2"/></svg></span>时间线</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_timeline }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.v_timeline">
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
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg><p class="rd-empty-text">暂无时间线</p></div>
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button type="success" icon="Pointer" @click="handleViewReceive" v-if="!viewForm.receiveStatus || viewForm.receiveStatus === '0' || viewForm.receiveStatus === '3'" v-hasPermi="['marketing:lead:pool:receive']">申请领取</el-button>
        <el-button type="warning" icon="Check" @click="handleViewApprove" v-if="viewForm.receiveStatus === '1'" v-hasPermi="['marketing:lead:pool:approve']">审批</el-button>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveOpen" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">领取审批</span>
          <div class="rd-detail-header-sub" v-if="approveForm.leadNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ approveForm.leadNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a_basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>线索基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_basic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">线索编号</span><div class="rd-value">{{ approveForm.leadNo }}</div></div>
              <div class="rd-item"><span class="rd-label">线索来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="approveForm.leadSource" /></div></div>
              <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ approveForm.companyName }}</div></div>
              <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ approveForm.contactName }}</div></div>
              <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ approveForm.contactPhone }}</div></div>
              <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value" :class="{ 'rd-value--muted': !approveForm.contactEmail }">{{ approveForm.contactEmail || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value" :class="{ 'rd-value--muted': !approveForm.position }">{{ approveForm.position || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value" :class="{ 'rd-value--muted': !approveForm.address }">{{ approveForm.address || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a_company')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></span>企业画像</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_company }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_company">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="approveForm.industry" /></div></div>
              <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="approveForm.companySize" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a_requirement')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>需求与评分</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_requirement }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_requirement">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">线索等级</span><div class="rd-value"><dict-tag :options="marketing_lead_grade" :value="approveForm.leadGrade" /></div></div>
              <div class="rd-item"><span class="rd-label">线索评分</span><div class="rd-value">{{ approveForm.leadScore }}</div></div>
              <div class="rd-item"><span class="rd-label">线索状态</span><div class="rd-value"><dict-tag :options="marketing_lead_status" :value="approveForm.leadStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">最后跟进</span><div class="rd-value" :class="{ 'rd-value--muted': !approveForm.lastFollowTime }">{{ approveForm.lastFollowTime || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">需求描述</span><div class="rd-value" :class="{ 'rd-value--muted': !approveForm.requirementDesc }">{{ approveForm.requirementDesc || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a_apply')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></span>申请信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_apply }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_apply">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">审批状态</span><div class="rd-value"><dict-tag :options="marketing_lead_receive_status" :value="approveForm.receiveStatus || '1'" /></div></div>
              <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ approveForm.receiveApplyUserName }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">申请时间</span><div class="rd-value">{{ approveForm.receiveApplyTime }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a_remark')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>审批备注</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a_remark }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.a_remark">
            <el-form ref="approveRef" :model="approveForm" label-width="0">
              <el-form-item prop="receiveRemark">
                <el-input v-model="approveForm.receiveRemark" type="textarea" :rows="3" placeholder="请输入审批备注（可选）" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="confirmApprove">通过</el-button>
        <el-button type="danger" @click="confirmReject">拒绝</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="线索公海业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">退回公海</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">申请领取</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已批准</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="primary">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已拒绝</el-tag>
            <el-tag size="small" type="info">退回公海可重新申请</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="未申请">线索在公海池中，未被任何人申请领取</el-descriptions-item>
          <el-descriptions-item label="待审批">已有销售人员申请领取，等待管理员审批</el-descriptions-item>
          <el-descriptions-item label="已批准">审批通过，线索已分配给申请人</el-descriptions-item>
          <el-descriptions-item label="已拒绝">审批被拒绝，线索退回公海，可重新申请</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>申请领取：</strong>点击"申请领取"提交审批申请，等待管理员审批</p>
          <p>• <strong>审批流程：</strong>管理员可查看线索详情后决定通过或拒绝</p>
          <p>• <strong>自动分配：</strong>审批通过后线索自动分配给申请人，成为跟进中状态</p>
          <p>• <strong>重新申请：</strong>被拒绝的线索退回公海，可重新申请领取</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkLeadPool">
import { listLead, getLead, receiveLead, approveReceive, rejectReceive, getLeadLog } from '@/api/mk/lead'
import { ArrowRight, QuestionFilled } from '@element-plus/icons-vue'
import { listInteraction } from '@/api/mk/interaction'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['v_lead', 'v_company', 'v_requirement', 'v_receive', 'v_other', 'v_interactions', 'v_timeline', 'a_basic', 'a_company', 'a_requirement', 'a_apply', 'a_remark'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_lead_pool')
const { marketing_lead_status, marketing_lead_grade, marketing_customer_source, marketing_industry, marketing_company_size, marketing_lead_receive_status, marketing_interaction_type } = proxy.useDict('marketing_lead_status', 'marketing_lead_grade', 'marketing_customer_source', 'marketing_industry', 'marketing_company_size', 'marketing_lead_receive_status', 'marketing_interaction_type')

const list = ref([])
const viewOpen = ref(false)
const approveOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showHelp = ref(false)
const showStatusHelp = ref(false)
const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })
const statusTabList = computed(() => marketing_lead_receive_status.value)
function loadStatusCounts() {
  const counts = { all: 0, '0': 0, '1': 0, '2': 0, '3': 0 }
  list.value.forEach(row => {
    const s = row.receiveStatus || '0'
    if (counts[s] !== undefined) counts[s]++
  })
  counts.all = total.value
  statusCounts.value = counts
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.receiveStatus = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'red' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_lead_receive_status.value.find(d => d.value == status); return item ? item.label : '-' }
function sourceBadgeClass(source) { const map = { '1': 'blue', '2': 'green', '3': 'violet', '4': 'amber', '5': 'cyan', '6': 'red', '7': 'gray' }; return map[source] || 'gray' }
function sourceLabel(source) { const item = marketing_customer_source.value.find(d => d.value == source); return item ? item.label : '-' }
function gradeBadgeClass(grade) { const map = { '1': 'red', '2': 'amber', '3': 'blue', '4': 'green' }; return map[grade] || 'gray' }
function gradeLabel(grade) { const item = marketing_lead_grade.value.find(d => d.value == grade); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-reject' }; return map[value] || '' }
const total = ref(0)
const viewForm = ref({})
const approveForm = ref({})
const detailTab = ref('basic')
const interactionList = ref([])
const timelineList = ref([])

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, companyName: undefined, leadGrade: undefined, receiveStatus: undefined, isPublic: '1', params: {} }
})
const { queryParams } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  leadNo: { label: '线索编号', visible: true },
  companyName: { label: '企业名称', visible: true },
  contactName: { label: '联系人', visible: true },
  contactPhone: { label: '手机号', visible: true },
  leadSource: { label: '线索来源', visible: true },
  leadGrade: { label: '等级', visible: true },
  leadScore: { label: '评分', visible: true },
  receiveStatus: { label: '审批状态', visible: true },
  receiveApplyUserName: { label: '申请人', visible: true },
  receiveApplyTime: { label: '申请时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_lead_pool_columns')
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
  if (queryParams.value.companyName) count++
  if (queryParams.value.leadGrade) count++
  if (queryParams.value.receiveStatus) count++
  return count
})

function getList() { loading.value = true; listLead(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false }) }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.companyName = undefined; queryParams.value.leadGrade = undefined; queryParams.value.receiveStatus = undefined; queryParams.value.isPublic = '1'; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleView(row) {
  getLead(row.leadId).then(res => {
    viewForm.value = res.data
    detailTab.value = 'basic'
    interactionList.value = []
    timelineList.value = []
    Object.keys(collapsedCards).forEach(k => {
      if (k.startsWith('v_')) collapsedCards[k] = false
    })
    viewOpen.value = true
  })
}
/** 加载详情关联数据 */
function loadViewRelations() {
  if (!viewForm.value.leadId) return
  listInteraction({ leadId: viewForm.value.leadId, pageNum: 1, pageSize: 999 }).then(res => {
    interactionList.value = res.rows || []
    buildTimeline()
  })
}
/** 构建时间线 */
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
function getInteractTypeLabel(type) {
  const item = marketing_interaction_type.value?.find(d => d.value === type)
  return item ? item.label : type
}
/** 详情弹窗内申请领取 */
function handleViewReceive() {
  proxy.$modal.confirm('确认申请领取线索"' + viewForm.value.companyName + '"？提交后需等待管理员审批。').then(() => {
    receiveLead(viewForm.value.leadId, {}).then(() => {
      getList()
      proxy.$modal.msgSuccess('申请已提交，等待审批')
      getLead(viewForm.value.leadId).then(res => { viewForm.value = res.data })
    })
  }).catch(() => {})
}
/** 详情弹窗内审批 */
function handleViewApprove() {
  approveForm.value = {
    leadId: viewForm.value.leadId,
    leadNo: viewForm.value.leadNo,
    companyName: viewForm.value.companyName,
    contactName: viewForm.value.contactName,
    contactPhone: viewForm.value.contactPhone,
    contactEmail: viewForm.value.contactEmail,
    position: viewForm.value.position,
    address: viewForm.value.address,
    industry: viewForm.value.industry,
    companySize: viewForm.value.companySize,
    leadSource: viewForm.value.leadSource,
    leadGrade: viewForm.value.leadGrade,
    leadScore: viewForm.value.leadScore,
    leadStatus: viewForm.value.leadStatus,
    requirementDesc: viewForm.value.requirementDesc,
    lastFollowTime: viewForm.value.lastFollowTime,
    receiveStatus: viewForm.value.receiveStatus,
    receiveApplyUserName: viewForm.value.receiveApplyUserName,
    receiveApplyTime: viewForm.value.receiveApplyTime,
    receiveRemark: undefined
  }
  Object.keys(collapsedCards).forEach(k => {
    if (k.startsWith('a_')) collapsedCards[k] = false
  })
  approveOpen.value = true
}
function handleReceive(row) {
  proxy.$modal.confirm('确认申请领取线索"' + row.companyName + '"？提交后需等待管理员审批。').then(() => receiveLead(row.leadId, {})).then(() => { getList(); proxy.$modal.msgSuccess('申请已提交，等待审批') }).catch(() => {})
}
function handleApprove(row) {
  approveForm.value = {
    leadId: row.leadId,
    leadNo: row.leadNo,
    companyName: row.companyName,
    contactName: row.contactName,
    contactPhone: row.contactPhone,
    contactEmail: row.contactEmail,
    position: row.position,
    address: row.address,
    industry: row.industry,
    companySize: row.companySize,
    leadSource: row.leadSource,
    leadGrade: row.leadGrade,
    leadScore: row.leadScore,
    leadStatus: row.leadStatus,
    requirementDesc: row.requirementDesc,
    lastFollowTime: row.lastFollowTime,
    receiveStatus: row.receiveStatus,
    receiveApplyUserName: row.receiveApplyUserName,
    receiveApplyTime: row.receiveApplyTime,
    receiveRemark: undefined
  }
  Object.keys(collapsedCards).forEach(k => {
    if (k.startsWith('a_')) collapsedCards[k] = false
  })
  approveOpen.value = true
}
function confirmApprove() {
  approveReceive(approveForm.value.leadId, { receiveRemark: approveForm.value.receiveRemark }).then(() => {
    proxy.$modal.msgSuccess('审批通过，线索已分配')
    approveOpen.value = false
    getList()
  })
}
function confirmReject() {
  rejectReceive(approveForm.value.leadId, { receiveRemark: approveForm.value.receiveRemark }).then(() => {
    proxy.$modal.msgSuccess('已拒绝领取申请')
    approveOpen.value = false
    getList()
  })
}
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
</style>
