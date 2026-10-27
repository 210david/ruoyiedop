<template>
  <div class="app-container mk-list-page">
    <!-- ===== 筛选卡片 ===== -->
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
          <label>标题</label>
          <div class="control">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>内容类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.contentType" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_content_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>关联活动</label>
          <div class="control is-select">
            <el-select v-model="queryParams.activityId" placeholder="全部" clearable filterable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="a in activityOptions" :key="a.activityId" :label="a.activityName" :value="a.activityId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>内容编号</label>
          <div class="control">
            <el-input v-model="queryParams.contentNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建人</label>
          <div class="control is-select">
            <el-select v-model="queryParams.userId" placeholder="全部" clearable filterable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
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

    <!-- ===== 表格区 ===== -->
    <div class="surface">
      <!-- 审核 Tabs -->
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

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:content:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleEdit()" v-hasPermi="['marketing:content:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['marketing:content:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:content:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_content_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
          <el-table-column label="内容编号" prop="contentNo" key="contentNo" :width="colWidth('contentNo', 150)" resizable show-overflow-tooltip v-if="columns.contentNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.contentNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="标题" prop="title" key="title" :min-width="colWidth('title', 200)" resizable show-overflow-tooltip v-if="columns.title.visible">
            <template #default="scope">
              <el-link type="primary" :underline="false" @click="handleDetail(scope.row)">{{ scope.row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="内容类型" prop="contentType" key="contentType" :width="colWidth('contentType', 110)" resizable align="center" v-if="columns.contentType.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.contentType)">{{ typeLabel(scope.row.contentType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="关联活动" prop="activityName" key="activityName" :min-width="colWidth('activityName', 160)" resizable show-overflow-tooltip v-if="columns.activityName.visible">
            <template #default="scope">{{ scope.row.activityName || '-' }}</template>
          </el-table-column>
          <el-table-column label="计划发布日期" prop="planPublishDate" key="planPublishDate" :width="colWidth('planPublishDate', 130)" resizable align="center" v-if="columns.planPublishDate.visible">
            <template #default="scope">{{ formatDate(scope.row.planPublishDate) }}</template>
          </el-table-column>
          <el-table-column label="审核状态" prop="auditStatus" key="auditStatus" :width="colWidth('auditStatus', 110)" resizable align="center" v-if="columns.auditStatus.visible">
            <template #default="scope">
              <span class="badge" :class="auditBadgeClass(scope.row.auditStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.auditStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建人" prop="userName" key="userName" :width="colWidth('userName', 100)" resizable v-if="columns.userName.visible">
            <template #default="scope">{{ scope.row.userName || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button v-if="scope.row.auditStatus === '0' || scope.row.auditStatus === '3'" link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['marketing:content:edit']">编辑</el-button>
                <el-button v-if="scope.row.auditStatus === '0' || scope.row.auditStatus === '3'" link type="primary" icon="Promotion" @click="handleSubmitAudit(scope.row)" v-hasPermi="['marketing:content:edit']">提交审核</el-button>
                <el-button v-if="scope.row.auditStatus === '1'" link type="warning" icon="Stamp" @click="handleAudit(scope.row)" v-hasPermi="['marketing:content:audit']">审核</el-button>
                <el-button v-if="scope.row.auditStatus === '2'" link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['marketing:content:query']">详情</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:content:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 审核对话框（规范：信息摘要卡片 → 内容包 → 审核记录 → 审批意见；底部通过/驳回按钮） -->
    <el-dialog v-model="auditOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></div>
          <span class="rd-detail-header-title">内容审核</span>
          <div class="rd-detail-header-sub" v-if="auditForm.contentNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ auditForm.contentNo }}</span>
            <dict-tag :options="marketing_content_audit_status" :value="auditForm.auditStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page audit-dialog-body">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('auditBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.auditBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">内容编号</span><div class="rd-value col-mono">{{ auditForm.contentNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">内容标题</span><div class="rd-value">{{ auditForm.title || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">内容类型</span><div class="rd-value"><dict-tag :options="marketing_content_type" :value="auditForm.contentType" /></div></div>
              <div class="rd-item"><span class="rd-label">关联活动</span><div class="rd-value" :class="{ 'rd-value--muted': !auditForm.activityName }">{{ auditForm.activityName || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划发布日期</span><div class="rd-value">{{ auditForm.planPublishDate ? String(auditForm.planPublishDate).substring(0, 10) : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ auditForm.userName || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">创作主题</span><div class="rd-value" :class="{ 'rd-value--muted': !auditForm.theme }">{{ auditForm.theme || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !auditForm.remark }">{{ auditForm.remark || '暂无' }}</div></div>
            </div>
          </div>
        </section>

        <!-- 内容包 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('auditPkg')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>内容包（渠道文案）<span class="pkg-count" v-if="auditForm.packageList && auditForm.packageList.length">{{ auditForm.packageList.length }}</span></div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditPkg }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.auditPkg">
            <el-table border :data="auditForm.packageList || []" size="small">
              <el-table-column label="渠道" width="110" align="center">
                <template #default="scope"><span class="badge" :class="channelBadgeClass(scope.row.channelType)"><span class="dot"></span>{{ channelLabel(scope.row.channelType) }}</span></template>
              </el-table-column>
              <el-table-column label="包标题" prop="packageTitle" min-width="150" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.packageTitle || '—' }}</template>
              </el-table-column>
              <el-table-column label="正文" min-width="220" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.packageBody || '—' }}</template>
              </el-table-column>
              <el-table-column label="话题标签" prop="topicTags" min-width="130" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.topicTags || '—' }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template #default="scope"><span class="badge" :class="packageStatusBadgeClass(scope.row.packageStatus)">{{ packageStatusLabel(scope.row.packageStatus) }}</span></template>
              </el-table-column>
            </el-table>
            <div class="rd-empty" v-if="!auditForm.packageList || auditForm.packageList.length === 0">
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>
              <p class="rd-empty-text">暂无内容包</p>
            </div>
          </div>
        </section>

        <!-- 审核记录时间线 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('auditLog')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditLog }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.auditLog" style="display:block">
            <div class="rd-timeline" v-if="auditForm.auditLogList && auditForm.auditLogList.length">
              <div class="rd-timeline-item" v-for="log in auditForm.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '0'" size="small" effect="light" round>提交审核</el-tag>
                      <el-tag v-else-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">操作人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark && log.auditAction !== '0'">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
              <p class="rd-empty-text">暂无审核记录</p>
            </div>
          </div>
        </section>

        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('auditOpinion')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>审批意见</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditOpinion }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.auditOpinion">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
              <el-form-item label="审批意见" prop="auditRemark">
                <el-input v-model="auditForm.auditRemark" type="textarea" :rows="4" placeholder="请输入审批意见（驳回时必填）" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="submitAudit(true)">通 过</el-button>
        <el-button type="danger" @click="submitAudit(false)">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="内容发布业务操作说明" width="640px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="内容主档">推广内容的策划底稿（标题/类型/创作主题/计划发布日期），下挂各渠道内容包，形成「内容 → 渠道 → 引流」分发链路</el-descriptions-item>
          <el-descriptions-item label="草稿">内容编辑中，可修改、可提交审核，可继续完善各渠道内容包</el-descriptions-item>
          <el-descriptions-item label="待审核">已提交待审核人处理，此时内容锁定不可编辑</el-descriptions-item>
          <el-descriptions-item label="已通过">审核通过，进入发布阶段，可在详情页回填各渠道发布链接跟踪发布状态</el-descriptions-item>
          <el-descriptions-item label="已驳回">审核未通过，按审核意见修改后可重新提交审核</el-descriptions-item>
        </el-descriptions>
        <h4>二、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>流程：</strong>新建内容 → 配置渠道内容包（支持 AI 生成草稿）→ 提交审核 → 审核通过 → 各渠道发布并回填发布链接</p>
          <p>• <strong>内容包：</strong>同一内容可为不同渠道定制不同文案（标题/正文/话题标签/封面/引用素材），一渠道一包</p>
          <p>• <strong>AI 生成：</strong>基于创作主题为勾选渠道生成文案草稿，仅前端填充，需人工确认后保存</p>
          <p>• <strong>审核规则：</strong>驳回时必须填写审核意见；已通过内容不可再编辑</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 审核弹窗（规范 3.6：信息摘要 → 审核记录 → 审批意见） */
.audit-dialog-body {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 4px;
}
.audit-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}
.audit-summary-title {
  font-weight: 600;
  font-size: 15px;
  color: #1e293b;
  margin-bottom: 6px;
}
.audit-summary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}
.audit-log-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin: 4px 0 12px 0;
}
.audit-form {
  margin-top: 16px;
  border-top: 1px solid #f1f5f9;
  padding-top: 16px;
}
/* 内容包计数徽标（与详情页一致） */
.pkg-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  margin-left: 8px;
  border-radius: 10px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
}
</style>

<script setup name="MkContent">
import { Search, Filter, Edit, Delete, Download, WarningFilled, ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { listContent, getContent, delContent, submitContent, auditContent } from '@/api/mk/content'
import { listActivity } from '@/api/mk/activity'
import { listUser } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { collapsedCards, toggleCard } = useDetailCard(['auditBasic', 'auditPkg', 'auditLog', 'auditOpinion'])

const { proxy } = getCurrentInstance()
const router = useRouter()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_content_index')
const { marketing_content_type, marketing_content_audit_status, marketing_channel_type, marketing_package_status } = proxy.useDict('marketing_content_type', 'marketing_content_audit_status', 'marketing_channel_type', 'marketing_package_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const showStatusHelp = ref(false)
const activityOptions = ref([])
const userOptions = ref([])
const auditOpen = ref(false)
const auditForm = ref({ contentId: undefined, contentNo: undefined, title: undefined, contentType: undefined, activityName: undefined, planPublishDate: undefined, theme: undefined, remark: undefined, userName: undefined, auditStatus: undefined, auditRemark: undefined, packageList: [], auditLogList: [] })
const auditRules = {
  auditRemark: [{ validator: (rule, value, callback) => { if (pendingAuditPass.value === false && !value) { callback(new Error('驳回时必须填写审批意见')) } else { callback() } }, trigger: 'blur' }]
}
const pendingAuditPass = ref(true)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, contentType: undefined, activityId: undefined, contentNo: undefined, userId: undefined, auditStatus: undefined, params: {} }
})
const { queryParams } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  contentNo: { label: '内容编号', visible: true },
  title: { label: '标题', visible: true },
  contentType: { label: '内容类型', visible: true },
  activityName: { label: '关联活动', visible: true },
  planPublishDate: { label: '计划发布日期', visible: true },
  auditStatus: { label: '审核状态', visible: true },
  userName: { label: '创建人', visible: true },
  createTime: { label: '创建时间', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_content_columns')
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

const statusTabList = computed(() => marketing_content_audit_status.value)
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.title) count++
  if (queryParams.value.contentType) count++
  if (queryParams.value.activityId) count++
  if (queryParams.value.contentNo) count++
  if (queryParams.value.userId) count++
  return count
})

function getList() {
  loading.value = true
  listContent(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false })
}

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, title: undefined, contentType: undefined, activityId: undefined, contentNo: undefined, userId: undefined, auditStatus: undefined, params: {} }
  activeStatusTab.value = 'all'
  handleQuery()
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.auditStatus = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() {
  // 基于当前筛选条件（剔除审核状态与分页）拉取全量数据统计，与列表筛选保持一致
  const query = { ...queryParams.value, pageNum: 1, pageSize: 9999, auditStatus: undefined, params: { ...queryParams.value.params } }
  listContent(query, { suppressError: true }).then(res => {
    const rows = res.rows || []
    const counts = { all: rows.length }
    marketing_content_audit_status.value.forEach(d => { counts[d.value] = 0 })
    rows.forEach(r => { if (counts[r.auditStatus] !== undefined) counts[r.auditStatus]++ })
    statusCounts.value = counts
  }).catch(() => { statusCounts.value = { all: 0 } })
}
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-reject' }; return map[value] || '' }
function auditBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'red' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_content_audit_status.value.find(d => d.value == status); return item ? item.label : '-' }
/* 审核弹窗所需：渠道/内容包状态标签（与详情页保持一致） */
function channelLabel(type) { const item = marketing_channel_type.value.find(d => d.value == type); return item ? item.label : '-' }
function channelBadgeClass(type) { const item = marketing_channel_type.value.find(d => d.value == type); const cls = item ? item.listClass : ''; const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }; return map[cls] || 'gray' }
function packageStatusLabel(status) { const item = marketing_package_status.value.find(d => d.value == status); return item ? item.label : '待发布' }
function packageStatusBadgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'gray' }; return map[status] || 'amber' }
function typeBadgeClass(type) {
  const item = marketing_content_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}
function typeLabel(type) { const item = marketing_content_type.value.find(d => d.value == type); return item ? item.label : '-' }
/** 计划发布日期截取日期部分 */
function formatDate(val) {
  if (!val) return '-'
  return String(val).length > 10 ? String(val).substring(0, 10) : val
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.contentId); single.value = selection.length !== 1; multiple.value = !selection.length }

/** 新增 → 跳转详情编辑页 */
function handleAdd() { router.push('/mk/content-detail/0') }
/** 编辑 → 跳转详情编辑页 */
function handleEdit(row) { router.push('/mk/content-detail/' + (row.contentId || ids.value[0])) }
/** 详情 → 跳转详情页 */
function handleDetail(row) { router.push('/mk/content-detail/' + (row.contentId || ids.value[0])) }
/** 提交审核 */
function handleSubmitAudit(row) {
  proxy.$modal.confirm('确认提交审核？提交后内容将进入待审核状态，无法编辑').then(() => submitContent(row.contentId)).then(() => { getList(); proxy.$modal.msgSuccess('已提交审核') }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('提交审核失败：' + (e?.message || e)) })
}
/** 打开审核弹窗（加载完整信息：基本/内容包/审核记录） */
function handleAudit(row) {
  auditForm.value = { contentId: row.contentId, title: row.title, auditRemark: undefined, packageList: [], auditLogList: [] }
  pendingAuditPass.value = true
  getContent(row.contentId).then(res => {
    const data = res.data || {}
    auditForm.value = {
      contentId: data.contentId,
      contentNo: data.contentNo,
      title: data.title,
      contentType: data.contentType,
      activityName: data.activityName,
      planPublishDate: data.planPublishDate,
      theme: data.theme,
      remark: data.remark,
      userName: data.userName,
      auditStatus: data.auditStatus,
      auditRemark: undefined,
      packageList: data.packageList || [],
      auditLogList: data.auditLogList || []
    }
  }).catch(e => { proxy.$modal.msgWarning('审核详情加载失败：' + (e?.message || e)) })
  auditOpen.value = true
}
/** 提交审核结果（通过/驳回按钮） */
function submitAudit(pass) {
  pendingAuditPass.value = pass
  proxy.$refs['auditRef'].validate(valid => {
    if (!valid) return
    auditContent(auditForm.value.contentId, { pass, auditRemark: auditForm.value.auditRemark || undefined }).then(() => {
      proxy.$modal.msgSuccess(pass ? '审批通过' : '已驳回')
      auditOpen.value = false
      getList()
    })
  })
}
function handleDelete(row) {
  const contentIds = row.contentId || ids.value
  proxy.$modal.confirm('确认删除选中的内容？').then(() => delContent(contentIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}
function handleExport() { proxy.download('mk/content/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'contentId'), `content_${new Date().getTime()}.xlsx`) }

listActivity({ pageNum: 1, pageSize: 9999 }).then(res => { activityOptions.value = res.rows }).catch(e => { proxy.$modal.msgWarning('活动列表加载失败，活动下拉将为空：' + (e?.message || e)) })
listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }).catch(e => { proxy.$modal.msgWarning('用户列表加载失败，用户下拉将为空：' + (e?.message || e)) })
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
