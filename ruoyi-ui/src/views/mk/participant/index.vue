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
          <label>活动</label>
          <div class="control is-select">
            <el-select v-model="queryParams.activityId" filterable clearable placeholder="全部" @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="a in activityOptions" :key="a.activityId" :label="a.activityName" :value="a.activityId" />
            </el-select>
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
          <label>参与状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.participateStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_participate_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>联系人</label>
          <div class="control">
            <el-input v-model="queryParams.contactName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>手机号</label>
          <div class="control">
            <el-input v-model="queryParams.contactPhone" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>职位</label>
          <div class="control">
            <el-input v-model="queryParams.position" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>签到时间</label>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:participant:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:participant:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:participant:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:participant:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_participant_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="活动名称" prop="activityName" key="activityName" :width="colWidth('activityName', 200)" resizable show-overflow-tooltip v-if="!queryParams.activityId && columns.activityName.visible" />
          <el-table-column label="企业名称" prop="companyName" key="companyName" :width="colWidth('companyName', 180)" resizable show-overflow-tooltip v-if="columns.companyName.visible" />
          <el-table-column label="联系人" prop="contactName" key="contactName" :width="colWidth('contactName', 100)" resizable v-if="columns.contactName.visible" />
          <el-table-column label="手机号" prop="contactPhone" key="contactPhone" :width="colWidth('contactPhone', 130)" resizable v-if="columns.contactPhone.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.contactPhone }}</span></template>
          </el-table-column>
          <el-table-column label="邮箱" prop="contactEmail" key="contactEmail" :width="colWidth('contactEmail', 180)" resizable show-overflow-tooltip v-if="columns.contactEmail.visible" />
          <el-table-column label="职位" prop="position" key="position" :width="colWidth('position', 120)" resizable v-if="columns.position.visible" />
          <el-table-column label="参与状态" prop="participateStatus" key="participateStatus" :width="colWidth('participateStatus', 100)" resizable align="center" sortable="custom" v-if="columns.participateStatus.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.participateStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.participateStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="线索" :width="80" align="center" v-if="columns.leadId.visible">
            <template #default="scope"><el-tag v-if="scope.row.leadId" type="success" size="small">已转</el-tag><span v-else>-</span></template>
          </el-table-column>
          <el-table-column label="签到时间" prop="signTime" key="signTime" :width="colWidth('signTime', 160)" resizable sortable="custom" v-if="columns.signTime.visible" />
      <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
        <template #default="scope">
          <div class="action-btn-row">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:participant:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="signIn" icon="CircleCheck" v-if="scope.row.participateStatus !== '1'" v-hasPermi="['marketing:participant:edit']">签到</el-dropdown-item>
                <el-dropdown-item command="convertLead" icon="Connection" v-if="!scope.row.leadId" v-hasPermi="['marketing:participant:convert']">转线索</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:participant:remove']" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          </div>
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
      <el-form ref="participantRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_activity')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>活动关联</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_activity }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_activity">
              <el-row>
                <el-col :span="24"><el-form-item label="所属活动" prop="activityId">
                  <el-select v-model="form.activityId" filterable placeholder="请选择活动" style="width: 100%">
                    <el-option v-for="a in activityOptions" :key="a.activityId" :label="a.activityName" :value="a.activityId" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c_info')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>参与人信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c_info }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c_info">
              <el-row>
                <el-col :span="24"><el-form-item label="从联系人库选">
                  <el-select v-model="form.contactId" filterable clearable placeholder="选择后自动带出信息" style="width: 100%" @change="onContactSelect">
                    <el-option v-for="c in contactOptions" :key="c.contactId" :label="c.name + ' (' + (c.customerName||'') + ')'" :value="c.contactId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="企业名称" prop="companyName"><el-input v-model="form.companyName" placeholder="请输入企业名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="联系人" prop="contactName"><el-input v-model="form.contactName" placeholder="请输入联系人姓名" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="手机号" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入手机号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="邮箱" prop="contactEmail"><el-input v-model="form.contactEmail" placeholder="请输入邮箱" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="职位" prop="position"><el-input v-model="form.position" placeholder="请输入职位" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="参与状态" prop="participateStatus">
                  <el-select v-model="form.participateStatus" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_participate_status" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="签到时间" prop="signTime"><el-date-picker v-model="form.signTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择签到时间" style="width: 100%" /></el-form-item></el-col>
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
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <span class="rd-detail-header-title">参与人详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.contactName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ viewForm.contactName }}</span>
            <dict-tag :options="marketing_participate_status" :value="viewForm.participateStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_activity')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>活动关联</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_activity }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_activity">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">所属活动</span><div class="rd-value">{{ viewForm.activityName }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_info')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>参与人信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_info }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_info">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ viewForm.companyName }}</div></div>
              <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ viewForm.contactName }}</div></div>
              <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ viewForm.contactPhone }}</div></div>
              <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.contactEmail }">{{ viewForm.contactEmail || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.position }">{{ viewForm.position || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">参与状态</span><div class="rd-value"><dict-tag :options="marketing_participate_status" :value="viewForm.participateStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">签到时间</span><div class="rd-value">{{ viewForm.signTime }}</div></div>
              <div class="rd-item"><span class="rd-label">关联线索</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.leadId }">{{ viewForm.leadId ? '已转线索 (ID: ' + viewForm.leadId + ')' : '未转化' }}</div></div>
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
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="活动参与人业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">新增参与人</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">已报名</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已签到</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">转线索</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="已报名">参与人已登记，等待活动当天签到</el-descriptions-item>
          <el-descriptions-item label="已签到">参与人已到场签到，可转化为线索跟进</el-descriptions-item>
          <el-descriptions-item label="已转线索">参与人已转化为销售线索，可在线索模块继续跟进</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>新增参与人：</strong>可手动新增或从联系人库选择，自动带出联系人信息</p>
          <p>• <strong>签到操作：</strong>活动进行中时可对参与人进行签到，记录签到时间</p>
          <p>• <strong>转线索：</strong>签到后的参与人可转化为销售线索，便于后续跟进转化</p>
          <p>• <strong>活动筛选：</strong>可通过活动下拉框筛选特定活动的参与人</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkParticipant">
import { listParticipant, getParticipant, addParticipant, updateParticipant, delParticipant, signInParticipant, convertToLead } from '@/api/mk/participant'
import { ArrowRight, ArrowDown, QuestionFilled, WarningFilled, Search, Filter, Edit, Delete, Download } from '@element-plus/icons-vue'
import { listActivity } from '@/api/mk/activity'
import { listContact } from '@/api/mk/contact'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['c_activity', 'c_info', 'c_other', 'v_activity', 'v_info', 'v_other'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_participant_index')
const { marketing_participate_status } = proxy.useDict('marketing_participate_status')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const activityOptions = ref([])
const contactOptions = ref([])
const viewForm = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, activityId: undefined, companyName: undefined, participateStatus: undefined, contactName: undefined, contactPhone: undefined, position: undefined, params: {} },
  rules: {
    activityId: [{ required: true, message: '请选择所属活动', trigger: 'change' }],
    companyName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
    contactName: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
    contactPhone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  activityName: { label: '活动名称', visible: true },
  companyName: { label: '企业名称', visible: true },
  contactName: { label: '联系人', visible: true },
  contactPhone: { label: '手机号', visible: true },
  contactEmail: { label: '邮箱', visible: true },
  position: { label: '职位', visible: true },
  participateStatus: { label: '参与状态', visible: true },
  leadId: { label: '线索', visible: true },
  signTime: { label: '签到时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_participant_columns')
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

const statusTabList = computed(() => marketing_participate_status.value)
const activeFilterCount = computed(() => {
  let count = 0
if (queryParams.value.activityId) count++
if (queryParams.value.companyName) count++
if (queryParams.value.participateStatus) count++
if (queryParams.value.contactName) count++
if (queryParams.value.contactPhone) count++
if (queryParams.value.position) count++
if (dateRange.value && dateRange.value.length > 0) count++
return count
})

function getList() {
  loading.value = true
  listParticipant(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false })
}
function getActivityOptions() {
  listActivity({ pageNum: 1, pageSize: 9999 }).then(res => { activityOptions.value = res.rows })
}
function getContactOptions() {
  listContact({ pageNum: 1, pageSize: 9999 }).then(res => { contactOptions.value = res.rows })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, dateRange.value, 'SignTime'); getList() }
function resetQuery() {
queryParams.value.activityId = undefined; queryParams.value.companyName = undefined; queryParams.value.participateStatus = undefined; queryParams.value.contactName = undefined; queryParams.value.contactPhone = undefined; queryParams.value.position = undefined; queryParams.value.params = {}; dateRange.value = []; activeStatusTab.value = 'all'; handleQuery()
}
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.participateStatus = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() {
  listParticipant({ pageNum: 1, pageSize: 999 }, { suppressError: true }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.participateStatus] !== undefined) counts[r.participateStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-approved' }; return map[value] || '' }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'violet' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_participate_status.value.find(d => d.value == status); return item ? item.label : '-' }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.participantId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { activityId: undefined, leadId: undefined, contactId: undefined, companyName: undefined, contactName: undefined, contactPhone: undefined, contactEmail: undefined, position: undefined, participateStatus: '0', source: '0', signTime: undefined, remark: undefined }
  proxy.resetForm('participantRef')
}
function handleAdd() { reset(); if (contactOptions.value.length === 0) getContactOptions(); Object.keys(collapsedCards).forEach(k => { if (k.startsWith('c_')) collapsedCards[k] = false }); open.value = true; title.value = '新增活动参与人' }
function handleUpdate(row) { reset(); if (contactOptions.value.length === 0) getContactOptions(); getParticipant(row.participantId || ids.value[0]).then(res => { form.value = res.data; Object.keys(collapsedCards).forEach(k => { if (k.startsWith('c_')) collapsedCards[k] = false }); open.value = true; title.value = '修改活动参与人' }) }
function handleView(row) {
  getParticipant(row.participantId).then(res => {
    viewForm.value = res.data
    Object.keys(collapsedCards).forEach(k => { if (k.startsWith('v_')) collapsedCards[k] = false })
    viewOpen.value = true
  })
}
function onContactSelect(contactId) {
  if (contactId) {
    const c = contactOptions.value.find(i => i.contactId === contactId)
    if (c) {
      form.value.companyName = c.customerName
      form.value.contactName = c.name
      form.value.contactPhone = c.phone
      form.value.contactEmail = c.email
      form.value.position = c.position
    }
  }
}
function submitForm() {
  proxy.$refs['participantRef'].validate(valid => {
    if (valid) {
      if (form.value.participantId != undefined) { updateParticipant(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addParticipant(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const participantIds = row.participantId || ids.value; proxy.$modal.confirm('确认删除编号为"' + participantIds + '"的数据？').then(() => delParticipant(participantIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/participant/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'SignTime') }, `participant_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// P1-5: 签到
function handleSignIn(row) {
  proxy.$modal.confirm('确认签到？').then(() => signInParticipant(row.participantId)).then(() => { proxy.$modal.msgSuccess('签到成功'); getList() }).catch(() => {})
}
// P1-5: 转线索
function handleConvertLead(row) {
  proxy.$modal.confirm('确认将"' + row.contactName + '"转化为线索？').then(() => convertToLead(row.participantId)).then(() => { proxy.$modal.msgSuccess('转线索成功'); getList() }).catch(() => {})
}

// 操作下拉菜单统一分发
function handleCommand(cmd, row) {
  switch (cmd) {
    case 'signIn': handleSignIn(row); break
    case 'convertLead': handleConvertLead(row); break
    case 'delete': handleDelete(row); break
  }
}

const showStatusHelp = ref(false)

getActivityOptions()
getList()
</script>

<style scoped>
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
