<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="商机编号" prop="opportunityNo"><el-input v-model="queryParams.opportunityNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="商机名称" prop="opportunityName"><el-input v-model="queryParams.opportunityName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="销售阶段" prop="stageCode">
        <el-select v-model="queryParams.stageCode" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="s in stageOptions" :key="s.stageCode" :label="s.stageName" :value="s.stageCode" />
        </el-select>
      </el-form-item>
      <el-form-item label="商机状态" prop="opportunityStatus">
        <el-select v-model="queryParams.opportunityStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_opportunity_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:opportunity:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:opportunity:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:opportunity:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:opportunity:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="商机编号" prop="opportunityNo" :width="colWidth('opportunityNo', 150)" resizable />
      <el-table-column label="商机名称" prop="opportunityName" show-overflow-tooltip />
      <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
      <el-table-column label="销售阶段" prop="stageName" :width="colWidth('stageName', 100)" resizable align="center" />
      <el-table-column label="预计金额" prop="expectedAmount" :width="colWidth('expectedAmount', 120)" resizable align="right" />
      <el-table-column label="赢率" prop="winRate" :width="colWidth('winRate', 80)" resizable align="center">
        <template #default="scope">{{ scope.row.winRate }}%</template>
      </el-table-column>
      <el-table-column label="加权金额" prop="weightedAmount" :width="colWidth('weightedAmount', 120)" resizable align="right" />
      <el-table-column label="商机状态" prop="opportunityStatus" :width="colWidth('opportunityStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_opportunity_status" :value="scope.row.opportunityStatus" /></template>
      </el-table-column>
      <el-table-column label="负责人" prop="userName" :width="colWidth('userName', 100)" resizable />
      <el-table-column label="操作" width="320" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:opportunity:edit']">修改</el-button>
          <el-dropdown v-if="scope.row.opportunityStatus === '0'" @command="(cmd) => handleAction(cmd, scope.row)" style="margin-left: 8px">
            <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="advance">推进阶段</el-dropdown-item>
                <el-dropdown-item command="retreat">退回阶段</el-dropdown-item>
                <el-dropdown-item command="win" divided style="color: #67c23a">赢单关单</el-dropdown-item>
                <el-dropdown-item command="lose" style="color: #f56c6c">输单关单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button v-if="scope.row.opportunityStatus !== '0'" link type="warning" @click="handleReopen(scope.row)">重开</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="open" width="850px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="opportunityRef" :model="form" :rules="rules" label-width="110px">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="基本信息" name="basic">
            <el-row>
              <el-col :span="12"><el-form-item label="商机编号" prop="opportunityNo"><el-input v-model="form.opportunityNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="商机名称" prop="opportunityName"><el-input v-model="form.opportunityName" placeholder="请输入商机名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="所属客户" prop="customerId">
                <el-select v-model="form.customerId" filterable clearable placeholder="请选择客户" style="width: 100%" @change="onCustomerChange">
                  <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="主要联系人" prop="contactId">
                <el-select v-model="form.contactId" filterable clearable placeholder="请选择联系人" style="width: 100%">
                  <el-option v-for="c in contactOptions" :key="c.contactId" :label="c.name" :value="c.contactId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="商机来源" prop="opportunitySource">
                <el-select v-model="form.opportunitySource" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_opportunity_source" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="金额与阶段" name="amount">
            <el-row>
              <el-col :span="12"><el-form-item label="预计金额" prop="expectedAmount"><el-input-number v-model="form.expectedAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="预计成交日期" prop="expectedDate"><el-date-picker v-model="form.expectedDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="销售阶段" prop="stageCode">
                <el-select v-model="form.stageCode" placeholder="请选择" style="width: 100%" @change="onStageChange">
                  <el-option v-for="s in stageOptions" :key="s.stageCode" :label="s.stageName" :value="s.stageCode" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="赢率" prop="winRate"><el-input-number v-model="form.winRate" :min="0" :max="100" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="商机状态" prop="opportunityStatus">
                <el-select v-model="form.opportunityStatus" placeholder="请选择" style="width: 100%"><el-option v-for="d in marketing_opportunity_status" :key="d.value" :label="d.label" :value="d.value" /></el-select>
              </el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="竞争与方案" name="solution">
            <el-row>
              <el-col :span="12"><el-form-item label="竞争对手" prop="competitor"><el-input v-model="form.competitor" placeholder="请输入竞争对手" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="客户痛点" prop="painPoint"><el-input v-model="form.painPoint" type="textarea" :rows="2" placeholder="请输入客户痛点" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="解决方案" prop="solution"><el-input v-model="form.solution" type="textarea" :rows="2" placeholder="请输入解决方案" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="负责与跟进" name="owner">
            <el-row>
              <el-col :span="12"><el-form-item label="负责人" prop="userId">
                <el-select v-model="form.userId" filterable clearable placeholder="请选择负责人" style="width: 100%" @change="onUserChange">
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="下一步行动" prop="nextAction"><el-input v-model="form.nextAction" placeholder="请输入下一步行动" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="下次行动时间" prop="nextTime"><el-date-picker v-model="form.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="其他信息" name="other">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情弹窗 - Tab页 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog" @open="loadRelations">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">商机详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.opportunityNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.opportunityNo }}</span>
            <dict-tag :options="marketing_opportunity_status" :value="viewForm.opportunityStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewBasic')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
                  </span>
                  基本信息
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewBasic }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewBasic">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">商机编号</span><div class="rd-value">{{ viewForm.opportunityNo }}</div></div>
                  <div class="rd-item"><span class="rd-label">商机名称</span><div class="rd-value">{{ viewForm.opportunityName }}</div></div>
                  <div class="rd-item"><span class="rd-label">所属客户</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
                  <div class="rd-item"><span class="rd-label">主要联系人</span><div class="rd-value">{{ viewForm.contactName }}</div></div>
                  <div class="rd-item"><span class="rd-label">商机来源</span><div class="rd-value"><dict-tag :options="marketing_opportunity_source" :value="viewForm.opportunitySource" /></div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewAmount')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                  </span>
                  金额与阶段
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewAmount }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewAmount">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">销售阶段</span><div class="rd-value">{{ viewForm.stageName }}</div></div>
                  <div class="rd-item"><span class="rd-label">预计金额</span><div class="rd-value rd-value--large rd-amount">￥{{ viewForm.expectedAmount }}</div></div>
                  <div class="rd-item"><span class="rd-label">预计成交</span><div class="rd-value">{{ viewForm.expectedDate }}</div></div>
                  <div class="rd-item"><span class="rd-label">赢率</span><div class="rd-value">{{ viewForm.winRate }}%</div></div>
                  <div class="rd-item"><span class="rd-label">加权金额</span><div class="rd-value rd-amount">￥{{ viewForm.weightedAmount }}</div></div>
                  <div class="rd-item"><span class="rd-label">商机状态</span><div class="rd-value"><dict-tag :options="marketing_opportunity_status" :value="viewForm.opportunityStatus" /></div></div>
                  <div class="rd-item" v-if="viewForm.actualAmount"><span class="rd-label">实际成交金额</span><div class="rd-value rd-amount">￥{{ viewForm.actualAmount }}</div></div>
                  <div class="rd-item" v-if="viewForm.actualDate"><span class="rd-label">实际成交日期</span><div class="rd-value">{{ viewForm.actualDate }}</div></div>
                  <div class="rd-item rd-item--full" v-if="viewForm.lostReason"><span class="rd-label">输单原因</span><div class="rd-value">{{ viewForm.lostReason }}</div></div>
                  <div class="rd-item rd-item--full" v-if="viewForm.lostRemark"><span class="rd-label">输单说明</span><div class="rd-value">{{ viewForm.lostRemark }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewSolution')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
                  </span>
                  竞争与方案
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewSolution }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewSolution">
                <div class="rd-grid">
                  <div class="rd-item rd-item--full"><span class="rd-label">竞争对手</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.competitor }">{{ viewForm.competitor || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">客户痛点</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.painPoint }">{{ viewForm.painPoint || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">解决方案</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.solution }">{{ viewForm.solution || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewOwner')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  </span>
                  负责与跟进
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOwner }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewOwner">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewForm.userName }}</div></div>
                  <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewForm.deptName }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">下一步行动</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.nextAction }">{{ viewForm.nextAction || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">下次行动时间</span><div class="rd-value">{{ viewForm.nextTime }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewOther')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
                  </span>
                  其他信息
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOther }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewOther">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`跟进记录 (${interactionList.length})`" name="interactions">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewInteractions')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                  </span>
                  跟进记录
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewInteractions }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
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
          <el-tab-pane :label="`阶段历史 (${stageLogList.length})`" name="stageLog">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewStageLog')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><path d="M12 7v5l4 2"/></svg>
                  </span>
                  阶段历史
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewStageLog }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewStageLog">
                <div class="rd-timeline" v-if="stageLogList.length > 0">
                  <div class="rd-timeline-item" v-for="log in stageLogList" :key="log.logId">
                    <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': getLogTagType(log.actionType) === 'success', 'rd-timeline-dot--error': getLogTagType(log.actionType) === 'danger' }"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title">
                          <el-tag size="small" :type="getLogTagType(log.actionType)">{{ getActionTypeLabel(log.actionType) }}</el-tag>
                          <span style="margin-left: 8px">
                            <span v-if="log.fromStageName">{{ log.fromStageName }}</span>
                            <span v-if="log.toStageName"> → {{ log.toStageName }}</span>
                          </span>
                        </span>
                        <span class="rd-timeline-time">{{ log.operatorName }} · {{ log.operateTime }}</span>
                      </div>
                      <div class="rd-timeline-comment" v-if="log.remark">{{ log.remark }}</div>
                    </div>
                  </div>
                </div>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  <p class="rd-empty-text">暂无阶段变更记录</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`关联合同 (${contractList.length})`" name="contracts">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewContracts')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M16 13H8"/><path d="M16 17H8"/><path d="M10 9H8"/></svg>
                  </span>
                  关联合同
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewContracts }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewContracts">
                <el-table :data="contractList" border size="small" v-if="contractList.length > 0">
                  <el-table-column label="合同编号" prop="contractNo" width="150" />
                  <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
                  <el-table-column label="合同金额" prop="contractAmount" width="120" align="right" />
                  <el-table-column label="签约日期" prop="signDate" width="120" />
                  <el-table-column label="状态" prop="contractStatus" width="80" align="center">
                    <template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template>
                  </el-table-column>
                </el-table>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                  <p class="rd-empty-text">暂无关联合同</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
          <el-tab-pane :label="`关联订单 (${orderList.length})`" name="orders">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewOrders')">
                <div class="rd-card-title">
                  <span class="rd-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
                  </span>
                  关联订单
                </div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOrders }" aria-label="折叠">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewOrders">
                <el-table :data="orderList" border size="small" v-if="orderList.length > 0">
                  <el-table-column label="订单编号" prop="orderNo" width="150" />
                  <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
                  <el-table-column label="订单金额" prop="orderAmount" width="120" align="right" />
                  <el-table-column label="状态" prop="orderStatus" width="80" align="center">
                    <template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template>
                  </el-table-column>
                </el-table>
                <div class="rd-empty" v-else>
                  <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>
                  <p class="rd-empty-text">暂无关联订单</p>
                </div>
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 阶段推进/赢单/输单弹窗 -->
    <el-dialog v-model="actionOpen" width="620px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <path d="M9 15l2 2 4-4"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ actionTitle }}</span>
          <div class="rd-detail-header-sub" v-if="actionForm.opportunityName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ actionForm.opportunityName }}</span>
          </div>
        </div>
      </template>
      <el-form :model="actionForm" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('actionInfo')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
                </span>
                商机信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.actionInfo }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.actionInfo">
              <div class="rd-grid">
                <div class="rd-item rd-item--full"><span class="rd-label">商机名称</span><div class="rd-value">{{ actionForm.opportunityName }}</div></div>
                <div class="rd-item"><span class="rd-label">当前阶段</span><div class="rd-value"><el-tag type="info">{{ actionForm.currentStageName }}</el-tag></div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('actionForm')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                </span>
                操作信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.actionForm }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.actionForm">
              <el-form-item label="目标阶段" v-if="actionType === 'advance' || actionType === 'retreat'">
                <el-select v-model="actionForm.toStageCode" placeholder="请选择目标阶段" style="width: 100%">
                  <el-option v-for="s in targetStageOptions" :key="s.stageCode" :label="s.stageName + ' (' + s.winRate + '%)'" :value="s.stageCode" />
                </el-select>
              </el-form-item>
              <template v-if="actionType === 'win'">
                <el-form-item label="实际成交金额" required>
                  <el-input-number v-model="actionForm.actualAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                </el-form-item>
                <el-form-item label="实际成交日期" required>
                  <el-date-picker v-model="actionForm.actualDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                </el-form-item>
              </template>
              <template v-if="actionType === 'lose'">
                <el-form-item label="输单原因" required>
                  <el-select v-model="actionForm.lostReason" placeholder="请选择输单原因" style="width: 100%">
                    <el-option label="价格过高" value="价格过高" />
                    <el-option label="竞品胜出" value="竞品胜出" />
                    <el-option label="需求不匹配" value="需求不匹配" />
                    <el-option label="决策人变化" value="决策人变化" />
                    <el-option label="项目取消" value="项目取消" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
                <el-form-item label="输单说明">
                  <el-input v-model="actionForm.lostRemark" type="textarea" :rows="3" placeholder="请输入输单说明" />
                </el-form-item>
              </template>
              <el-form-item label="备注" v-if="actionType === 'advance' || actionType === 'retreat'">
                <el-input v-model="actionForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAction">确 定</el-button>
        <el-button @click="actionOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkOpportunity">
import { listOpportunity, getOpportunity, addOpportunity, updateOpportunity, delOpportunity, advanceOpportunity, retreatOpportunity, winOpportunity, loseOpportunity, reopenOpportunity, getOpportunityRelations } from '@/api/mk/opportunity'
import { listStage } from '@/api/mk/stage'
import { listCustomer } from '@/api/mk/customer'
import { listContact } from '@/api/mk/contact'
import { listUser, deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['viewBasic', 'viewAmount', 'viewSolution', 'viewOwner', 'viewOther', 'viewInteractions', 'viewStageLog', 'viewContracts', 'viewOrders', 'actionInfo', 'actionForm'])
import { ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_opportunity_index')
const { marketing_opportunity_status, marketing_opportunity_source, marketing_contract_status, marketing_order_status, marketing_interaction_type } = proxy.useDict('marketing_opportunity_status', 'marketing_opportunity_source', 'marketing_contract_status', 'marketing_order_status', 'marketing_interaction_type')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const customerOptions = ref([])
const contactOptions = ref([])
const userOptions = ref([])
const deptOptions = ref([])
const stageOptions = ref([])
const viewForm = ref({})
const activeNames = ref(['basic', 'amount', 'solution', 'owner', 'other'])
const detailTab = ref('basic')
const interactionList = ref([])
const stageLogList = ref([])
const contractList = ref([])
const orderList = ref([])
const actionOpen = ref(false)
const actionTitle = ref('')
const actionType = ref('')
const actionForm = ref({})
const targetStageOptions = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, opportunityNo: undefined, opportunityName: undefined, stageCode: undefined, opportunityStatus: undefined },
  rules: {
    opportunityName: [{ required: true, message: '商机名称不能为空', trigger: 'blur' }],
    customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
    expectedAmount: [{ required: true, message: '预计金额不能为空', trigger: 'blur' }],
    expectedDate: [{ required: true, message: '请选择预计成交日期', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listOpportunity(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getCustomerOptions() { listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows }) }
function getUserOptions() { listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }) }
function getDeptTree() { deptTreeSelect().then(res => { deptOptions.value = res.data }) }
function getStageOptions() { listStage({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { stageOptions.value = res.rows }) }
function onCustomerChange(customerId) {
  form.value.contactId = undefined
  if (customerId) { listContact({ customerId: customerId, pageNum: 1, pageSize: 9999 }).then(res => { contactOptions.value = res.rows }) }
  else { contactOptions.value = [] }
}
function onStageChange(stageCode) {
  const stage = stageOptions.value.find(s => s.stageCode === stageCode)
  if (stage) { form.value.stageName = stage.stageName; form.value.winRate = stage.winRate }
}
function onUserChange(userId) {
  if (userId) { const user = userOptions.value.find(u => u.userId === userId); if (user) { form.value.userName = user.nickName; if (user.deptId) { form.value.deptId = user.deptId; form.value.deptName = user.dept ? user.dept.deptName : undefined } } }
  else { form.value.userName = undefined }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.opportunityId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { opportunityNo: undefined, opportunityName: undefined, customerId: undefined, contactId: undefined, leadId: undefined, opportunitySource: undefined, expectedAmount: 0, expectedDate: undefined, stageCode: 'stage1', stageName: '初步接触', winRate: 10, weightedAmount: 0, opportunityStatus: '0', competitor: undefined, painPoint: undefined, solution: undefined, userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, nextAction: undefined, nextTime: undefined, remark: undefined }
  contactOptions.value = []
  proxy.resetForm('opportunityRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增商机' }
function handleUpdate(row) {
  reset()
  getOpportunity(row.opportunityId || ids.value[0]).then(res => {
    form.value = res.data
    if (form.value.customerId) { listContact({ customerId: form.value.customerId, pageNum: 1, pageSize: 9999 }).then(r => { contactOptions.value = r.rows }) }
    open.value = true; title.value = '修改商机'
  })
}
function handleView(row) { getOpportunity(row.opportunityId).then(res => { viewForm.value = res.data; detailTab.value = 'basic'; Object.keys(collapsedCards).forEach(k => { collapsedCards[k] = false }); viewOpen.value = true }) }
function loadRelations() {
  if (!viewForm.value.opportunityId) return
  getOpportunityRelations(viewForm.value.opportunityId).then(res => {
    const data = res.data || {}
    interactionList.value = data.interactions || []
    stageLogList.value = data.stageLogs || []
    contractList.value = data.contracts || []
    orderList.value = data.orders || []
  })
}
function submitForm() {
  proxy.$refs['opportunityRef'].validate(valid => {
    if (valid) {
      if (form.value.opportunityId != undefined) { updateOpportunity(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addOpportunity(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const opportunityIds = row.opportunityId || ids.value; proxy.$modal.confirm('确认删除编号为"' + opportunityIds + '"的数据？').then(() => delOpportunity(opportunityIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/opportunity/export', { ...queryParams.value }, `opportunity_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ===== 阶段推进/赢单/输单 =====
function handleAction(cmd, row) {
  actionType.value = cmd
  actionForm.value = { opportunityId: row.opportunityId, opportunityName: row.opportunityName, currentStageCode: row.stageCode, currentStageName: row.stageName, toStageCode: undefined, actualAmount: undefined, actualDate: undefined, lostReason: undefined, lostRemark: undefined, remark: undefined }
  if (cmd === 'advance') {
    actionTitle.value = '推进阶段'
    // 只能推进到排序更大的阶段
    const currentSort = stageOptions.value.find(s => s.stageCode === row.stageCode)?.sort || 0
    targetStageOptions.value = stageOptions.value.filter(s => s.sort > currentSort)
    if (targetStageOptions.value.length === 0) { proxy.$modal.msgWarning('已是最后阶段，无法继续推进'); return }
    actionOpen.value = true
  } else if (cmd === 'retreat') {
    actionTitle.value = '退回阶段'
    // 只能退回到排序更小的阶段
    const currentSort = stageOptions.value.find(s => s.stageCode === row.stageCode)?.sort || 0
    targetStageOptions.value = stageOptions.value.filter(s => s.sort < currentSort)
    if (targetStageOptions.value.length === 0) { proxy.$modal.msgWarning('已是第一阶段，无法继续退回'); return }
    actionOpen.value = true
  } else if (cmd === 'win') {
    actionTitle.value = '赢单关单'
    actionForm.value.actualAmount = row.expectedAmount
    actionForm.value.actualDate = new Date().toISOString().slice(0, 10)
    actionOpen.value = true
  } else if (cmd === 'lose') {
    actionTitle.value = '输单关单'
    actionOpen.value = true
  }
}
function submitAction() {
  const id = actionForm.value.opportunityId
  if (actionType.value === 'advance') {
    if (!actionForm.value.toStageCode) { proxy.$modal.msgError('请选择目标阶段'); return }
    advanceOpportunity(id, { stageCode: actionForm.value.toStageCode, remark: actionForm.value.remark }).then(() => { proxy.$modal.msgSuccess('阶段推进成功'); actionOpen.value = false; getList() })
  } else if (actionType.value === 'retreat') {
    if (!actionForm.value.toStageCode) { proxy.$modal.msgError('请选择目标阶段'); return }
    retreatOpportunity(id, { stageCode: actionForm.value.toStageCode, remark: actionForm.value.remark }).then(() => { proxy.$modal.msgSuccess('阶段退回成功'); actionOpen.value = false; getList() })
  } else if (actionType.value === 'win') {
    if (!actionForm.value.actualAmount) { proxy.$modal.msgError('请填写实际成交金额'); return }
    if (!actionForm.value.actualDate) { proxy.$modal.msgError('请选择实际成交日期'); return }
    winOpportunity(id, { actualAmount: actionForm.value.actualAmount, actualDate: actionForm.value.actualDate, remark: actionForm.value.remark }).then(() => { proxy.$modal.msgSuccess('赢单关单成功'); actionOpen.value = false; getList() })
  } else if (actionType.value === 'lose') {
    if (!actionForm.value.lostReason) { proxy.$modal.msgError('请选择输单原因'); return }
    loseOpportunity(id, { lostReason: actionForm.value.lostReason, lostRemark: actionForm.value.lostRemark }).then(() => { proxy.$modal.msgSuccess('输单关单成功'); actionOpen.value = false; getList() })
  }
}
function handleReopen(row) {
  proxy.$modal.confirm('确认重新打开商机"' + row.opportunityName + '"？').then(() => reopenOpportunity(row.opportunityId)).then(() => { proxy.$modal.msgSuccess('重新打开成功'); getList() }).catch(() => {})
}

function getInteractTypeLabel(type) {
  const item = marketing_interaction_type.value?.find(d => d.value === type)
  return item ? item.label : type
}
function getActionTypeLabel(type) {
  const map = { advance: '推进', retreat: '退回', win: '赢单', lose: '输单', reopen: '重开' }
  return map[type] || type
}
function getLogTagType(type) {
  const map = { advance: 'success', retreat: 'warning', win: 'success', lose: 'danger', reopen: 'info' }
  return map[type] || 'primary'
}

getCustomerOptions()
getUserOptions()
getDeptTree()
getStageOptions()
getList()
</script>
