<template>
  <div class="app-container bid-notice-page">
    <!-- 筛选条件卡片 -->
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
          <label>标题关键词</label>
          <div class="control">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>信息类型</label>
          <div class="control">
            <el-select v-model="queryParams.noticeType" placeholder="全部" clearable style="width: 100%">
              <el-option v-for="d in bid_notice_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>地区</label>
          <div class="control">
            <el-input v-model="queryParams.regionCode" placeholder="如：云南" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>发布时间</label>
          <div class="control">
            <el-date-picker v-model="publishRange" type="daterange" range-separator="-"
              start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>招标人</label>
          <div class="control">
            <el-input v-model="queryParams.tenderee" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>代理机构</label>
          <div class="control">
            <el-input v-model="queryParams.agent" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>行业分类</label>
          <div class="control">
            <el-select v-model="queryParams.industry" placeholder="全部" clearable style="width: 100%">
              <el-option v-for="d in bid_industry" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>截止时间</label>
          <div class="control">
            <el-date-picker v-model="deadlineRange" type="daterange" range-separator="-"
              start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bid:notice:add']">手动登记</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['bid:notice:export']">导出</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['bid:notice:remove']">批量删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="bid_notice_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table"
          @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="公告标题" prop="title" key="title"
            :min-width="colWidth('title', 260)" show-overflow-tooltip resizable v-if="columns.title.visible" />
          <el-table-column label="信息类型" prop="noticeType" key="noticeType" :width="colWidth('noticeType', 120)" resizable align="center" v-if="columns.noticeType.visible">
            <template #default="scope">
              <span class="badge violet">{{ typeLabel(scope.row.noticeType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="招标人" prop="tenderee" key="tenderee"
            :min-width="colWidth('tenderee', 180)" show-overflow-tooltip resizable v-if="columns.tenderee.visible" />
          <el-table-column label="代理机构" prop="agent" key="agent"
            :min-width="colWidth('agent', 160)" show-overflow-tooltip resizable v-if="columns.agent.visible" />
          <el-table-column label="地区" prop="regionCode" key="regionCode" :width="colWidth('regionCode', 100)" resizable align="center" v-if="columns.regionCode.visible">
            <template #default="scope">{{ scope.row.regionCode || '—' }}</template>
          </el-table-column>
          <el-table-column label="行业分类" prop="industry" key="industry" :width="colWidth('industry', 110)" resizable align="center" v-if="columns.industry.visible">
            <template #default="scope">{{ industryLabel(scope.row.industry) }}</template>
          </el-table-column>
          <el-table-column label="预算金额（万元）" prop="budgetAmount" key="budgetAmount" :width="colWidth('budgetAmount', 170)" resizable align="center"
            sortable="custom" :sort-orders="['descending','ascending']" v-if="columns.budgetAmount.visible">
            <template #default="scope">{{ scope.row.budgetAmount != null ? formatWan(scope.row.budgetAmount) : '—' }}</template>
          </el-table-column>
          <el-table-column label="发布时间" prop="publishTime" key="publishTime" :width="colWidth('publishTime', 170)" resizable align="center"
            sortable="custom" :sort-orders="['descending','ascending']" v-if="columns.publishTime.visible" />
          <el-table-column label="截止时间" prop="deadlineTime" key="deadlineTime" :width="colWidth('deadlineTime', 180)" resizable align="center"
            sortable="custom" :sort-orders="['descending','ascending']" v-if="columns.deadlineTime.visible">
            <template #default="scope">
              <span v-if="scope.row.deadlineTime" :class="deadlineClass(scope.row.deadlineTime)">{{ deadlineText(scope.row.deadlineTime) }}</span>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="来源" prop="sourceName" key="sourceName"
            :min-width="colWidth('sourceName', 140)" show-overflow-tooltip resizable v-if="columns.sourceName.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['bid:notice:edit']">编辑</el-button>
                <el-button link type="primary" icon="Promotion" @click="handleToProject(scope.row)"
                  v-hasPermi="['bid:notice:edit']">转投标</el-button>
                <el-button link type="success" icon="MagicStick" @click="handleAiExtract(scope.row)"
                  v-if="scope.row.aiExtracted !== '1' && scope.row.content" v-hasPermi="['bid:notice:edit']">AI抽取</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['bid:notice:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total"
          v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 手动登记/修改弹窗 -->
    <el-dialog v-model="open" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.noticeId">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">ID：{{ form.noticeId }}</span>
          </div>
        </div>
      </template>
      <el-form ref="noticeRef" :model="form" :rules="rules" label-width="132px">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg></span>基本信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="公告标题" prop="title">
                  <el-input v-model="form.title" placeholder="请输入公告标题" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="信息类型" prop="noticeType">
                  <el-select v-model="form.noticeType" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in bid_notice_type" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="项目关联键" prop="projectKey">
                  <el-input v-model="form.projectKey" placeholder="招标编号，同项目公告用同一编号串联" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7h-4V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v3H4a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"/><path d="M10 5h4v2h-4z"/></svg></span>招标主体</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="招标人" prop="tenderee">
                  <el-input v-model="form.tenderee" placeholder="请输入招标人/采购人" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="代理机构" prop="agent">
                  <el-input v-model="form.agent" placeholder="请输入代理机构" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="地区" prop="regionCode">
                  <el-input v-model="form.regionCode" placeholder="如：云南 / 昆明" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="行业分类" prop="industry">
                  <el-select v-model="form.industry" placeholder="全部" clearable style="width: 100%">
                    <el-option v-for="d in bid_industry" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>时间节点</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="发布时间" prop="publishTime">
                  <el-date-picker v-model="form.publishTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="截止时间" prop="deadlineTime">
                  <el-date-picker v-model="form.deadlineTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开标时间" prop="openTime">
                  <el-date-picker v-model="form.openTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg></span>金额与联系</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="预算金额（万元）" prop="budgetAmount">
                  <el-input-number v-model="budgetWan" :precision="4" :min="0" placeholder="万元" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="招标附件" prop="attachmentUrl">
                  <file-upload v-model="form.attachmentUrl" :fileSize="20" :fileType="['pdf','doc','docx','xls','xlsx','zip','rar','png','jpg','jpeg']" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系人" prop="contactPerson">
                  <el-input v-model="form.contactPerson" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input v-model="form.contactPhone" placeholder="请输入" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></span>来源与正文</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="原文链接" prop="sourceUrl">
                  <el-input v-model="form.sourceUrl" placeholder="请输入公告原文链接" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="公告正文" prop="content">
                  <el-input v-model="form.content" type="textarea" :rows="4" placeholder="粘贴公告正文（用于AI抽取与正文匹配）" show-word-limit maxlength="8000" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" width="1166px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
          <span class="rd-detail-header-title">公告详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.projectKey">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ detailData.projectKey }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page" v-loading="detailLoading">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg></span>
              公告基本信息
              <el-button size="small" type="primary" link style="margin-left:auto" @click="handleAiExtract(detailData)" v-if="detailData.content" v-hasPermi="['bid:notice:edit']">AI 智能抽取</el-button>
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">公告标题</span><div class="rd-value">{{ detailData.title }}</div></div>
              <div class="rd-item"><span class="rd-label">信息类型</span><div class="rd-value">{{ typeLabel(detailData.noticeType) }}</div></div>
              <div class="rd-item"><span class="rd-label">项目关联键</span><div class="rd-value">{{ detailData.projectKey || '—' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7h-4V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v3H4a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"/><path d="M10 5h4v2h-4z"/></svg></span>招标主体</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">招标人</span><div class="rd-value">{{ detailData.tenderee || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">代理机构</span><div class="rd-value">{{ detailData.agent || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">地区</span><div class="rd-value">{{ detailData.regionCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">行业分类</span><div class="rd-value">{{ industryLabel(detailData.industry) }}</div></div>
              <div class="rd-item"><span class="rd-label">预算金额（万元）</span><div class="rd-value rd-amount">{{ detailData.budgetAmount != null ? formatWan(detailData.budgetAmount) : '—' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>时间节点</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发布时间</span><div class="rd-value">{{ detailData.publishTime || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">截止时间</span><div class="rd-value">{{ detailData.deadlineTime || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">开标时间</span><div class="rd-value">{{ detailData.openTime || '—' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg></span>联系与来源</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ detailData.contactPerson || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ detailData.contactPhone || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">命中关键词</span><div class="rd-value">{{ detailData.matchedKeywords || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">原文链接</span><div class="rd-value"><el-link v-if="detailData.sourceUrl" :href="detailData.sourceUrl" target="_blank" type="primary">{{ detailData.sourceUrl }}</el-link><span v-else>—</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">招标附件</span><div class="rd-value rd-value--block">
                <file-upload v-if="detailData.attachmentUrl" v-model="detailData.attachmentUrl" disabled />
                <span v-else>—</span>
              </div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="archiveList.length">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg></span>项目档案（同项目全部公告）</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="item in archiveList" :key="item.noticeId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': item.noticeId === detailData.noticeId }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag size="small" effect="light" round :type="item.noticeId === detailData.noticeId ? 'primary' : 'info'">{{ typeLabel(item.noticeType) }}</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ item.publishTime || '' }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item rd-item--full"><span class="rd-label">标题</span><div class="rd-value">{{ item.title }}</div></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="detailData.content">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>公告正文</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <pre class="notice-content notice-content--fmt">{{ detailData.content }}</pre>
          </div>
        </section>
      </div>
    </el-dialog>

    <!-- AI抽取预览确认弹窗 -->
    <el-dialog v-model="aiPreviewOpen" width="640px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 4V2m0 2a2 2 0 0 1 2 2m-2-2a2 2 0 0 0-2 2m8 0V4m0 2a2 2 0 0 1-2 2M3 11l18-5-2 8-6 2-3 4-2-6z"/></svg>
          </div>
          <span class="rd-detail-header-title">AI 抽取结果预览</span>
        </div>
      </template>
      <div v-loading="aiPreviewLoading">
        <el-alert type="warning" :closable="false" show-icon style="margin-bottom:12px"
          title="AI 结果仅供参考，请确认后再使用" description="仅空字段会被回填；已有值的字段不会被覆盖。" />
        <el-table :data="aiPreviewRows" border size="small">
          <el-table-column label="字段" prop="label" width="140" />
          <el-table-column label="AI 抽取值" prop="aiValue">
            <template #default="scope"><span v-if="scope.row.aiValue">{{ scope.row.aiValue }}</span><span v-else class="ai-empty">未识别</span></template>
          </el-table-column>
          <el-table-column label="当前值" prop="curValue" width="180">
            <template #default="scope"><span v-if="scope.row.curValue">{{ scope.row.curValue }}</span><span v-else class="ai-empty">（空，将回填）</span></template>
          </el-table-column>
          <el-table-column label="回填" width="70" align="center">
            <template #default="scope">
              <el-checkbox v-model="scope.row.checked" :disabled="!scope.row.aiValue || !!scope.row.curValue" />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" :loading="aiApplyLoading" @click="confirmAiApply">确认回填</el-button>
        <el-button @click="aiPreviewOpen = false">取 消</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="BidNotice">
import { listNotice, getNotice, addNotice, updateNotice, delNotice, getArchive, aiExtractNotice, applyAiExtract, toProject } from '@/api/bid/notice'
import { useColumnResize } from '@/composables/useColumnResize'
import { formatWan, yuanToWan, wanToYuan } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { bid_notice_type, bid_industry } = proxy.useDict('bid_notice_type', 'bid_industry')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bid_notice_index')

const showAdvanced = ref(false)
const showSearch = ref(true)
const publishRange = ref([])
const deadlineRange = ref([])
const loading = ref(true)
const list = ref([])
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const open = ref(false)
const title = ref('')
const detailOpen = ref(false)
const detailLoading = ref(false)
const detailData = ref({})
const archiveList = ref([])
const aiPreviewOpen = ref(false)
const aiPreviewLoading = ref(false)
const aiApplyLoading = ref(false)
const aiPreviewRows = ref([])
const aiPreviewNoticeId = ref(null)

const AI_FIELD_LABELS = {
  deadlineTime: '截止时间',
  openTime: '开标时间',
  budgetAmount: '预算金额（元）',
  tenderee: '招标人',
  agent: '代理机构',
  contactPerson: '联系人',
  contactPhone: '联系电话',
  region: '项目所在地'
}

const defaultColumns = {
  title: { label: '公告标题', visible: true },
  noticeType: { label: '信息类型', visible: true },
  tenderee: { label: '招标人', visible: true },
  agent: { label: '代理机构', visible: true },
  regionCode: { label: '地区', visible: true },
  industry: { label: '行业分类', visible: true },
  budgetAmount: { label: '预算金额（万元）', visible: true },
  publishTime: { label: '发布时间', visible: true },
  deadlineTime: { label: '截止时间', visible: true },
  sourceName: { label: '来源', visible: true }
}
function loadColumnVisibility() { try { const saved = localStorage.getItem('bid_notice_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, noticeType: undefined, regionCode: undefined,
    tenderee: undefined, agent: undefined, industry: undefined, params: {} },
  rules: {
    title: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
    noticeType: [{ required: true, message: '信息类型不能为空', trigger: 'change' }],
    tenderee: [{ required: true, message: '招标人不能为空', trigger: 'blur' }],
    publishTime: [{ required: true, message: '发布时间不能为空', trigger: 'change' }],
    deadlineTime: [{ required: true, message: '截止时间不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 预算金额：存储单位为元，录入/展示以万元换算
const budgetWan = computed({
  get: () => yuanToWan(form.value.budgetAmount),
  set: v => { form.value.budgetAmount = wanToYuan(v) }
})

const activeFilterCount = computed(() => {
  const q = queryParams.value
  return ['title','noticeType','regionCode','tenderee','agent','industry'].filter(k => q[k]).length
    + (publishRange.value.length ? 1 : 0) + (deadlineRange.value.length ? 1 : 0)
})

function typeLabel(v) {
  const item = bid_notice_type.value.find(d => d.value == v)
  return item ? item.label : '—'
}
function industryLabel(v) {
  if (v == null || v === '') return '—'
  const item = bid_industry.value.find(d => d.value == v)
  return item ? item.label : v
}
function deadlineClass(t) {
  if (!t) return ''
  const days = (new Date(t).getTime() - Date.now()) / 86400000
  if (days <= 3) return 'deadline-red'
  if (days <= 7) return 'deadline-amber'
  return ''
}
function deadlineText(t) {
  const days = (new Date(t).getTime() - Date.now()) / 86400000
  const base = String(t).replace('T', ' ').slice(0, 16)
  return days <= 7 ? `${base}（剩${Math.max(0, Math.ceil(days))}天）` : base
}

function getList() {
  loading.value = true
  listNotice(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  })
}
function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, publishRange.value, 'PublishTime')
  proxy.addDateRange(queryParams.value, deadlineRange.value, 'DeadlineTime')
  queryParams.value.pageNum = 1
  getList()
}
function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, title: undefined, noticeType: undefined,
    regionCode: undefined, tenderee: undefined, agent: undefined, industry: undefined, params: {} }
  publishRange.value = []
  deadlineRange.value = []
  getList()
}
function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}

function reset() {
  form.value = { noticeId: undefined, title: undefined, noticeType: '2', projectKey: undefined, tenderee: undefined,
    agent: undefined, regionCode: undefined, industry: undefined, publishTime: undefined, deadlineTime: undefined, openTime: undefined,
    budgetAmount: undefined, contactPerson: undefined, contactPhone: undefined, sourceUrl: undefined, content: undefined, attachmentUrl: undefined }
  proxy.resetForm('noticeRef')
}
function handleAdd() { reset(); open.value = true; title.value = '手动登记招标公告' }
function handleUpdate(row) {
  reset()
  getNotice(row.noticeId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '编辑招标公告'
  })
}
function handleDetail(row) {
  detailData.value = {}
  archiveList.value = []
  detailOpen.value = true
  detailLoading.value = true
  getNotice(row.noticeId).then(res => {
    detailData.value = res.data
    if (res.data && res.data.projectKey) {
      getArchive(res.data.projectKey).then(r => { archiveList.value = r.data || [] })
    }
  }).finally(() => { detailLoading.value = false })
}

function submitForm() {
  proxy.$refs['noticeRef'].validate(v => {
    if (v) {
      if (form.value.noticeId != null) {
        updateNotice(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addNotice(form.value).then(() => { proxy.$modal.msgSuccess('登记成功'); open.value = false; getList() })
      }
    }
  })
}
function cancel() { open.value = false; reset() }

function handleDelete(row) {
  const noticeIds = row && row.noticeId ? [row.noticeId] : ids.value
  if (!noticeIds.length) return
  const label = row && row.noticeId ? '公告「' + (row.title || '').slice(0, 30) + '」' : '选中的 ' + noticeIds.length + ' 条公告'
  proxy.$modal.confirm('确认删除' + label + '？').then(() => delNotice(noticeIds)).then(() => {
    getList(); proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.noticeId)
  multiple.value = !selection.length
}

function handleToProject(row) {
  proxy.$modal.confirm('确认为公告「' + (row.title || '').slice(0, 30) + '」登记投标项目？').then(() => toProject(row.noticeId)).then(res => {
    getList(); proxy.$modal.msgSuccess(res.msg)
  }).catch(() => {})
}

function handleAiExtract(row) {
  aiPreviewNoticeId.value = row.noticeId
  aiPreviewRows.value = []
  aiPreviewOpen.value = true
  aiPreviewLoading.value = true
  aiExtractNotice(row.noticeId).then(res => {
    const fields = (res.data && res.data.fields) || {}
    aiPreviewRows.value = Object.keys(AI_FIELD_LABELS).map(key => {
      const cur = row[key]
      const curValue = cur != null && cur !== '' ? String(cur) : ''
      const aiValue = fields[key] != null ? String(fields[key]) : ''
      return { key, label: AI_FIELD_LABELS[key], aiValue, curValue, checked: !!aiValue && !curValue }
    })
  }).finally(() => { aiPreviewLoading.value = false })
}

function confirmAiApply() {
  const fields = {}
  aiPreviewRows.value.forEach(r => { if (r.checked && r.aiValue) { fields[r.key] = r.aiValue } })
  if (!Object.keys(fields).length) {
    proxy.$modal.msgWarning('请至少勾选一个要回填的字段')
    return
  }
  aiApplyLoading.value = true
  applyAiExtract(aiPreviewNoticeId.value, fields).then(() => {
    proxy.$modal.msgSuccess('回填成功')
    aiPreviewOpen.value = false
    if (detailOpen.value) { handleDetail({ noticeId: aiPreviewNoticeId.value }) } else { getList() }
  }).finally(() => { aiApplyLoading.value = false })
}

function handleExport() {
  proxy.download('bid/notice/export', { ...proxy.addDateRange(queryParams.value, publishRange.value, 'PublishTime') }, `notice_${new Date().getTime()}.xlsx`)
}

getList()
onActivated(() => { getList() })
</script>

<style scoped>
.bid-notice-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.bid-notice-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.bid-notice-page .filter-head { display:flex; align-items:center; justify-content:space-between; padding:12px 20px 0; }
.bid-notice-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; }
.bid-notice-page .filter-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-500); }
.bid-notice-page .adv-link { display:inline-flex; align-items:center; gap:4px; color:var(--ink-500); cursor:pointer; font-size:13px; }
.bid-notice-page .adv-link:hover { color:var(--brand-600); }
.bid-notice-page .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; padding:12px 20px; }
.bid-notice-page .field label { display:block; font-size:12px; color:var(--ink-500); margin-bottom:4px; }
.bid-notice-page .filter-actions { display:flex; align-items:center; justify-content:space-between; padding:0 20px 14px; }
.bid-notice-page .filter-info { display:flex; align-items:center; gap:6px; font-size:12px; color:var(--ink-400); }
.bid-notice-page .filter-buttons { display:flex; gap:8px; }
.bid-notice-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 12px; }
.bid-notice-page .toolbar .left { display:flex; gap:8px; }
.bid-notice-page .pagination-container { padding:10px 12px; }
/* 状态/类型徽章 */
.bid-notice-page .badge { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.bid-notice-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.bid-notice-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.bid-notice-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.bid-notice-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.bid-notice-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.bid-notice-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
/* 截止时间预警色 */
.bid-notice-page .deadline-red { color:var(--red-500); font-weight:600; }
.bid-notice-page .deadline-amber { color:var(--amber-700); font-weight:600; }
/* 操作列对齐 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
/* 公告正文预览 */
.notice-content { white-space: pre-wrap; word-break: break-all; max-height: 420px; overflow-y: auto; font-size: 13px; line-height: 1.9; color: var(--ink-700); margin: 0; }
.notice-content--fmt { background: var(--el-fill-color-lighter, #f7f8fa); border-radius: 8px; padding: 14px 16px; }
/* AI抽取预览 */
.ai-empty { color: var(--ink-400); font-size: 12px; }
/* 详情附件列表：块级布局，覆盖 rd-value 的省略显示 */
.rd-value--block { display: block; white-space: normal; overflow: visible; text-overflow: clip; }
.rd-value--block .upload-file-list { margin: 0; }
</style>
