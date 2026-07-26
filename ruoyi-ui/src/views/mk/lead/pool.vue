<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="企业名称" prop="companyName"><el-input v-model="queryParams.companyName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="线索等级" prop="leadGrade">
        <el-select v-model="queryParams.leadGrade" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_lead_grade" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审批状态" prop="receiveStatus">
        <el-select v-model="queryParams.receiveStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_lead_receive_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="info" plain icon="Guide" @click="showHelp = !showHelp">公海池说明</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-alert v-if="showHelp" type="info" :closable="false" style="margin-bottom: 12px">
      <template #title>
        <div style="line-height: 1.8">
          <strong>公海池业务说明：</strong><br/>
          1. <strong>数据来源：</strong>新增线索时未指定负责人（默认 isPublic=1），或管理员手动将线索退回公海；<br/>
          2. <strong>领取流程：</strong>点击「申请领取」提交审批申请 → 管理员审批通过后线索自动分配给申请人；<br/>
          3. <strong>审批状态：</strong>未申请（可申请）、待审批（等待管理员处理）、已批准（已分配）、已拒绝（退回公海可重新申请）。
        </div>
      </template>
    </el-alert>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @header-dragend="onHeaderDragEnd">
      <el-table-column label="线索编号" prop="leadNo" :width="colWidth('leadNo', 150)" resizable />
      <el-table-column label="企业名称" prop="companyName" :width="colWidth('companyName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="联系人" prop="contactName" :width="colWidth('contactName', 100)" resizable />
      <el-table-column label="手机号" prop="contactPhone" :width="colWidth('contactPhone', 130)" resizable />
      <el-table-column label="线索来源" prop="leadSource" :width="colWidth('leadSource', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_customer_source" :value="scope.row.leadSource" /></template>
      </el-table-column>
      <el-table-column label="等级" prop="leadGrade" :width="colWidth('leadGrade', 80)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_lead_grade" :value="scope.row.leadGrade" /></template>
      </el-table-column>
      <el-table-column label="评分" prop="leadScore" :width="colWidth('leadScore', 80)" resizable align="center" />
      <el-table-column label="审批状态" prop="receiveStatus" :width="colWidth('receiveStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_lead_receive_status" :value="scope.row.receiveStatus || '0'" /></template>
      </el-table-column>
      <el-table-column label="申请人" prop="receiveApplyUserName" :width="colWidth('receiveApplyUserName', 100)" resizable />
      <el-table-column label="申请时间" prop="receiveApplyTime" :width="colWidth('receiveApplyTime', 160)" resizable />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="success" icon="Pointer" @click="handleReceive(scope.row)" v-hasPermi="['marketing:lead:pool:receive']" v-if="!scope.row.receiveStatus || scope.row.receiveStatus === '0' || scope.row.receiveStatus === '3'">申请领取</el-button>
          <el-button link type="warning" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['marketing:lead:pool:approve']" v-if="scope.row.receiveStatus === '1'">审批</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

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
  </div>
</template>

<script setup name="MkLeadPool">
import { listLead, getLead, receiveLead, approveReceive, rejectReceive, getLeadLog } from '@/api/mk/lead'
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
const total = ref(0)
const viewForm = ref({})
const approveForm = ref({})
const detailTab = ref('basic')
const interactionList = ref([])
const timelineList = ref([])

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, companyName: undefined, leadGrade: undefined, receiveStatus: undefined, isPublic: '1' }
})
const { queryParams } = toRefs(data)

function getList() { loading.value = true; listLead(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.isPublic = '1'; handleQuery() }
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
</style>
