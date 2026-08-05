<template>
  <div class="app-container mk-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>活动编号</label>
          <div class="control">
            <el-input v-model="queryParams.activityNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>活动名称</label>
          <div class="control">
            <el-input v-model="queryParams.activityName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>活动类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.activityType" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_activity_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>活动状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.activityStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_activity_status" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:activity:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:activity:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:activity:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchPublish" v-hasPermi="['marketing:activity:status']">
            <el-icon><Promotion /></el-icon> 批量发布
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchArchive" v-hasPermi="['marketing:activity:status']">
            <el-icon><FolderOpened /></el-icon> 批量归档
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="multiple" @click="handleBatchOwner" v-hasPermi="['marketing:activity:edit']">
            <el-icon><User /></el-icon> 批量改负责人
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:activity:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_activity_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="活动编号" prop="activityNo" key="activityNo" :width="colWidth('activityNo', 150)" resizable sortable="custom" v-if="columns.activityNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.activityNo }}</span></template>
          </el-table-column>
          <el-table-column label="活动名称" prop="activityName" key="activityName" :width="colWidth('activityName', 200)" resizable show-overflow-tooltip v-if="columns.activityName.visible">
            <template #default="scope">
              <el-button link type="primary" @click="handleView(scope.row)">{{ scope.row.activityName }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="类型" prop="activityType" key="activityType" :width="colWidth('activityType', 100)" resizable align="center" v-if="columns.activityType.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.activityType)">
                <span class="dot"></span>{{ typeLabel(scope.row.activityType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="形式" prop="activityForm" key="activityForm" :width="colWidth('activityForm', 80)" resizable align="center" v-if="columns.activityForm.visible">
            <template #default="scope">
              <span class="badge" :class="formBadgeClass(scope.row.activityForm)">
                <span class="dot"></span>{{ formLabel(scope.row.activityForm) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="activityStatus" key="activityStatus" :width="colWidth('activityStatus', 100)" resizable align="center" sortable="custom" v-if="columns.activityStatus.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.activityStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.activityStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="开始时间" prop="startTime" key="startTime" :width="colWidth('startTime', 160)" resizable sortable="custom" v-if="columns.startTime.visible" />
          <el-table-column label="结束时间" prop="endTime" key="endTime" :width="colWidth('endTime', 160)" resizable v-if="columns.endTime.visible" />
          <el-table-column label="预算" prop="budget" key="budget" :width="colWidth('budget', 100)" resizable align="right" class-name="col-num" v-if="columns.budget.visible" />
          <el-table-column label="报名" prop="signupCount" key="signupCount" :width="colWidth('signupCount', 70)" resizable align="center" v-if="columns.signupCount.visible" />
          <el-table-column label="签到" prop="signedCount" key="signedCount" :width="colWidth('signedCount', 70)" resizable align="center" v-if="columns.signedCount.visible" />
          <el-table-column label="线索" prop="convertedLeadCount" key="convertedLeadCount" :width="colWidth('convertedLeadCount', 70)" resizable align="center" v-if="columns.convertedLeadCount.visible" />
          <el-table-column label="成交额" prop="dealAmount" key="dealAmount" :width="colWidth('dealAmount', 100)" resizable align="right" sortable="custom" class-name="col-num" v-if="columns.dealAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatAmount(scope.row.dealAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="负责人" prop="userName" key="userName" :width="colWidth('userName', 100)" resizable v-if="columns.userName.visible" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:activity:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <!-- 状态流转 -->
                <el-dropdown-item command="publish" icon="Promotion" v-if="scope.row.activityStatus === '0'" v-hasPermi="['marketing:activity:status']">发布</el-dropdown-item>
                <el-dropdown-item command="start" icon="VideoPlay" v-if="scope.row.activityStatus === '1'" v-hasPermi="['marketing:activity:status']">开始</el-dropdown-item>
                <el-dropdown-item command="finish" icon="CircleClose" v-if="scope.row.activityStatus === '2'" v-hasPermi="['marketing:activity:status']">结束</el-dropdown-item>
                <el-dropdown-item command="settle" icon="DataAnalysis" v-if="['3','4'].includes(scope.row.activityStatus)" v-hasPermi="['marketing:activity:edit']">活动结算</el-dropdown-item>
                <el-dropdown-item command="archive" icon="FolderOpened" v-if="scope.row.activityStatus === '3'" v-hasPermi="['marketing:activity:status']">归档</el-dropdown-item>
                <el-dropdown-item command="cancel" icon="CircleClose" v-if="['1','2'].includes(scope.row.activityStatus)" v-hasPermi="['marketing:activity:status']" divided>取消活动</el-dropdown-item>
                <!-- 业务操作 -->
                <el-dropdown-item command="copy" icon="CopyDocument" v-hasPermi="['marketing:activity:copy']" divided>复制活动</el-dropdown-item>
                <el-dropdown-item command="promote" icon="Link" v-if="scope.row.activityForm === '0'">报名推广</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:activity:remove']" divided>删除</el-dropdown-item>
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
    <el-dialog v-model="open" width="850px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="activityRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row>
              <el-col :span="12"><el-form-item label="活动编号" prop="activityNo"><el-input v-model="form.activityNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动名称" prop="activityName"><el-input v-model="form.activityName" placeholder="请输入活动名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动类型" prop="activityType">
                <el-select v-model="form.activityType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_activity_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动形式" prop="activityForm">
                <el-select v-model="form.activityForm" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_activity_form" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动状态" prop="activityStatus">
                <el-select v-model="form.activityStatus" placeholder="请选择" style="width: 100%" :disabled="form.activityId != null">
                  <el-option v-for="d in marketing_activity_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动地点/链接" prop="location"><el-input v-model="form.location" placeholder="线下地点或线上链接" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('schedule')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>时间与预算</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.schedule }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.schedule">
            <el-row>
              <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="活动预算" prop="budget"><el-input-number v-model="form.budget" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="实际花费" prop="cost"><el-input-number v-model="form.cost" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="目标人数" prop="targetCount"><el-input-number v-model="form.targetCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="目标线索数" prop="targetLeadCount"><el-input-number v-model="form.targetLeadCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('owner')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>负责信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.owner }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.owner">
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
              <el-col :span="12"><el-form-item label="联系方式" prop="ownerPhone"><el-input v-model="form.ownerPhone" placeholder="手机号/微信号" /></el-form-item></el-col>
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
          <div class="rd-card-header" @click="toggleCard('detail')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>活动详情</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.detail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.detail">
            <el-form-item label="活动简介" prop="summary"><el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入活动简介" /></el-form-item>
            <el-form-item label="活动详情" prop="content"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入活动详情" /></el-form-item>
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('attachment')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件资料</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.attachment }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.attachment">
            <el-form-item label="附件上传">
              <el-upload
                multiple
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="handleBeforeAttachmentUpload"
                :on-success="handleAttachmentSuccess"
                :on-error="handleAttachmentError"
                :limit="10"
                :on-exceed="() => proxy.$modal.msgError('最多上传10个文件')"
              >
                <el-button type="primary" icon="Upload">上传文件</el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 doc/xls/ppt/pdf/txt/图片/压缩包(zip/rar/7z) 等格式，单文件不超过 20MB</div>
                </template>
              </el-upload>
              <!-- 自定义文件列表 -->
              <div class="attachment-list" v-if="attachmentFileList.length > 0">
                <div v-for="(file, index) in attachmentFileList" :key="index" class="attachment-list-item">
                  <el-link :underline="false" @click="handleAttachmentPreview(file)" type="primary">
                    <el-icon style="margin-right: 4px"><Document /></el-icon>
                    <span class="attachment-name">{{ file.name }}</span>
                  </el-link>
                  <el-link :underline="false" @click="handleAttachmentDelete(index)" type="danger">删除</el-link>
                </div>
              </div>
            </el-form-item>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('result')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>活动效果（结算）<el-tag v-if="!isSettlementPhase" type="info" size="small" style="margin-left: 8px">活动结束后填写</el-tag></div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.result }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.result">
            <el-alert v-if="!isSettlementPhase" title="活动效果数据需在活动结束后进行结算填写" type="info" :closable="false" show-icon style="margin-bottom: 16px" />
            <el-row>
              <el-col :span="8"><el-form-item label="实际参与" prop="actualCount"><el-input-number v-model="form.actualCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="获取线索" prop="leadCount"><el-input-number v-model="form.leadCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item prop="mqlCount">
                <template #label><span>MQL阶段</span><el-tooltip content="MQL（Marketing Qualified Lead）营销合格线索：经过营销团队初步筛选，符合目标客户画像，有进一步培育价值的线索" placement="top"><el-icon style="margin-left: 2px; vertical-align: middle; color: #909399"><QuestionFilled /></el-icon></el-tooltip></template>
                <el-input-number v-model="form.mqlCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item prop="sqlCount">
                <template #label><span>SQL阶段</span><el-tooltip content="SQL（Sales Qualified Lead）销售合格线索：经过销售团队确认，有明确购买意向和预算，可进入销售跟进阶段的线索" placement="top"><el-icon style="margin-left: 2px; vertical-align: middle; color: #909399"><QuestionFilled /></el-icon></el-tooltip></template>
                <el-input-number v-model="form.sqlCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item label="转化商机" prop="opportunityCount"><el-input-number v-model="form.opportunityCount" :min="0" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="成交金额" prop="dealAmount"><el-input-number v-model="form.dealAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="!isSettlementPhase" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 活动结算弹窗 -->
    <el-dialog v-model="settleOpen" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div>
          <span class="rd-detail-header-title">活动结算</span>
        </div>
      </template>
      <el-alert title="请填写活动实际效果数据，完成结算后可归档活动" type="info" :closable="false" show-icon style="margin-bottom: 20px" />
      <div class="rd-grid" style="margin-bottom: 20px">
        <div class="rd-item"><span class="rd-label">活动名称</span><div class="rd-value">{{ settleForm.activityName }}</div></div>
        <div class="rd-item"><span class="rd-label">活动时间</span><div class="rd-value">{{ settleForm.startTime }} ~ {{ settleForm.endTime }}</div></div>
        <div class="rd-item"><span class="rd-label">活动预算</span><div class="rd-value rd-amount rd-amount--negative">￥{{ formatAmount(settleForm.budget) }}</div></div>
        <div class="rd-item"><span class="rd-label">目标人数</span><div class="rd-value">{{ settleForm.targetCount }} 人</div></div>
      </div>
      <el-form ref="settleRef" :model="settleForm" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('settleCost')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>费用与参与</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.settleCost }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.settleCost">
            <el-row>
              <el-col :span="12"><el-form-item label="实际花费" prop="cost">
                <el-input-number v-model="settleForm.cost" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                <div class="settle-field-tip">活动实际支出的总费用</div>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="实际参与" prop="actualCount">
                <el-input-number v-model="settleForm.actualCount" :min="0" controls-position="right" style="width: 100%" />
                <div class="settle-field-tip">实际到场参与人数</div>
              </el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('settleLead')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>线索转化</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.settleLead }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.settleLead">
            <el-row>
              <el-col :span="12"><el-form-item label="获取线索" prop="leadCount">
                <el-input-number v-model="settleForm.leadCount" :min="0" controls-position="right" style="width: 100%" />
                <div class="settle-field-tip">活动收集到的线索总数</div>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item prop="mqlCount">
                <template #label><span>MQL阶段</span><el-tooltip content="MQL（Marketing Qualified Lead）营销合格线索：经过营销团队初步筛选，符合目标客户画像，有进一步培育价值的线索" placement="top"><el-icon style="margin-left: 2px; vertical-align: middle; color: #909399"><QuestionFilled /></el-icon></el-tooltip></template>
                <el-input-number v-model="settleForm.mqlCount" :min="0" controls-position="right" style="width: 100%" />
                <div class="settle-field-tip">营销合格线索数量</div>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item prop="sqlCount">
                <template #label><span>SQL阶段</span><el-tooltip content="SQL（Sales Qualified Lead）销售合格线索：经过销售团队确认，有明确购买意向和预算，可进入销售跟进阶段的线索" placement="top"><el-icon style="margin-left: 2px; vertical-align: middle; color: #909399"><QuestionFilled /></el-icon></el-tooltip></template>
                <el-input-number v-model="settleForm.sqlCount" :min="0" controls-position="right" style="width: 100%" />
                <div class="settle-field-tip">销售合格线索数量</div>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="转化商机" prop="opportunityCount">
                <el-input-number v-model="settleForm.opportunityCount" :min="0" controls-position="right" style="width: 100%" />
                <div class="settle-field-tip">转化为商机的数量</div>
              </el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('settleDeal')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>成交效果</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.settleDeal }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.settleDeal">
            <el-row>
              <el-col :span="12"><el-form-item label="成交金额" prop="dealAmount">
                <el-input-number v-model="settleForm.dealAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                <div class="settle-field-tip">活动带来的成交总金额</div>
              </el-form-item></el-col>
            </el-row>
          </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSettle">确认结算</el-button>
        <el-button @click="settleOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 文件在线预览 -->
    <file-preview ref="filePreviewRef" />

    <!-- 取消活动弹窗 -->
    <el-dialog v-model="cancelOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">取消活动</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="取消原因"><el-input v-model="cancelRemark" type="textarea" :rows="3" placeholder="请输入取消原因" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmCancel">确认取消</el-button>
        <el-button @click="cancelOpen = false">返 回</el-button>
      </template>
    </el-dialog>

    <!-- 批量改负责人弹窗 -->
    <el-dialog v-model="ownerOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">批量修改负责人</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="batchOwnerUserId" filterable placeholder="请选择负责人" style="width: 100%" @change="onBatchUserChange">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchOwner">确 定</el-button>
        <el-button @click="ownerOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- P3-15: 报名推广中心 -->
    <el-dialog v-model="promoteOpen" width="600px" append-to-body draggable class="rd-dialog" v-loading="promoteLoading">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="2" y1="12" x2="22" y2="12"/>
              <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">报名推广中心</span>
          <div class="rd-detail-header-sub" v-if="promoteData.activityName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ promoteData.activityName }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <div v-if="promoteData.url" class="promote-center">
        <!-- 活动名称 -->
        <div class="promote-activity-name">{{ promoteData.activityName }}</div>

        <!-- 二维码区域 -->
        <div class="promote-qr-section">
          <canvas ref="qrCanvasRef" class="promote-qr-canvas"></canvas>
          <div class="promote-qr-tip">扫描二维码进入报名页面</div>
        </div>

        <!-- 链接区域 -->
        <div class="promote-url-section">
          <div class="promote-url-label">报名链接</div>
          <el-input :model-value="promoteData.url" readonly>
            <template #append>
              <el-button @click="copyPromoteUrl">复制链接</el-button>
            </template>
          </el-input>
        </div>

        <!-- 操作按钮 -->
        <div class="promote-actions">
          <el-button type="primary" icon="Download" @click="downloadQrCode">下载二维码</el-button>
          <el-button type="success" icon="Promotion" @click="openPromotePage">预览报名页</el-button>
        </div>

        <!-- 业务说明 -->
        <el-divider content-position="left">推广说明</el-divider>
        <div class="promote-guide">
          <p><b>1. 线上推广</b>：复制链接，通过微信、邮件、短信发送给客户</p>
          <p><b>2. 线下推广</b>：下载二维码，印制在宣传海报、易拉宝、桌牌上</p>
          <p><b>3. 现场签到</b>：客户报名成功后获得签到码，活动当天扫码或输入签到码完成签到</p>
          <p><b>4. 线索转化</b>：签到后的参与人可一键转化为营销线索，进入销售漏斗</p>
        </div>
        </div>
      </div>
    </el-dialog>

    <!-- 详情弹窗 - Tab页 -->
    <el-dialog v-model="viewOpen" width="1080px" append-to-body draggable class="rd-dialog" @open="loadViewRelations">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">活动详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.activityNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.activityNo }}</span>
            <dict-tag :options="marketing_activity_status" :value="viewForm.activityStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page rd-page--wide">
        <el-tabs v-model="viewDetailTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewInfo')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>活动信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewInfo }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewInfo">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">活动编号</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.activityNo }">{{ viewForm.activityNo || '—' }}</div></div>
                  <div class="rd-item"><span class="rd-label">活动名称</span><div class="rd-value">{{ viewForm.activityName || '—' }}</div></div>
                  <div class="rd-item"><span class="rd-label">活动类型</span><div class="rd-value"><dict-tag :options="marketing_activity_type" :value="viewForm.activityType" /></div></div>
                  <div class="rd-item"><span class="rd-label">活动形式</span><div class="rd-value"><dict-tag :options="marketing_activity_form" :value="viewForm.activityForm" /></div></div>
                  <div class="rd-item"><span class="rd-label">活动状态</span><div class="rd-value"><dict-tag :options="marketing_activity_status" :value="viewForm.activityStatus" /></div></div>
                  <div class="rd-item"><span class="rd-label">活动地点</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.location }">{{ viewForm.location || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewSchedule')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>时间与预算</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewSchedule }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewSchedule">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewForm.startTime || '—' }}</div></div>
                  <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ viewForm.endTime || '—' }}</div></div>
                  <div class="rd-item"><span class="rd-label">活动预算</span><div class="rd-value rd-amount rd-amount--negative">￥{{ formatAmount(viewForm.budget) }}</div></div>
                  <div class="rd-item"><span class="rd-label">实际花费</span><div class="rd-value rd-amount rd-amount--negative">￥{{ formatAmount(viewForm.cost) }}</div></div>
                  <div class="rd-item"><span class="rd-label">目标人数</span><div class="rd-value">{{ viewForm.targetCount || 0 }} 人</div></div>
                  <div class="rd-item"><span class="rd-label">目标线索数</span><div class="rd-value">{{ viewForm.targetLeadCount || 0 }} 个</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewOwner')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>负责信息</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOwner }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewOwner">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.userName }">{{ viewForm.userName || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">联系方式</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.ownerPhone }">{{ viewForm.ownerPhone || '暂无' }}</div></div>
                  <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.deptName }">{{ viewForm.deptName || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewDesc')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg></span>活动描述</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewDesc }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewDesc">
                <div class="rd-grid">
                  <div class="rd-item rd-item--full"><span class="rd-label">活动简介</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.summary }">{{ viewForm.summary || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">活动详情</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.content }">{{ viewForm.content || '暂无' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewAttach')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件资料</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewAttach }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewAttach">
                <div v-if="viewAttachmentList.length > 0">
                  <div v-for="(file, index) in viewAttachmentList" :key="index" class="rd-item rd-item--full" style="margin-bottom: 8px;">
                    <span class="rd-label">附件{{ index + 1 }}</span>
                    <div class="rd-value"><el-link :underline="false" @click="handleAttachmentPreview(file)" type="primary"><el-icon style="margin-right: 4px"><Document /></el-icon><span>{{ file.name }}</span></el-link></div>
                  </div>
                </div>
                <div v-else class="rd-empty"><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg><p class="rd-empty-text">暂无附件资料</p></div>
              </div>
            </section>
          </el-tab-pane>
          <!-- 参与人 -->
          <el-tab-pane name="participants">
            <template #label>参与人<el-badge :value="viewParticipants.length" :hidden="viewParticipants.length === 0" class="ml5" /></template>
            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddParticipant" v-hasPermi="['marketing:participant:add']">新增参与人</el-button></el-col>
            </el-row>
            <el-table :data="viewParticipants" border size="small">
              <el-table-column label="客户名称" prop="companyName" show-overflow-tooltip />
              <el-table-column label="联系人" prop="contactName" width="100" align="center" />
              <el-table-column label="手机号" prop="contactPhone" width="130" align="center" />
              <el-table-column label="职位" prop="position" width="100" align="center" />
              <el-table-column label="状态" prop="participateStatus" width="90" align="center"><template #default="scope"><span class="badge" :class="partBadgeClass(scope.row.participateStatus)"><span class="dot"></span>{{ partStatusLabel(scope.row.participateStatus) }}</span></template></el-table-column>
              <el-table-column label="操作" width="200" align="center"><template #default="scope"><el-button link type="primary" size="small" @click="handleSignIn(scope.row)" v-if="scope.row.participateStatus !== '1'" v-hasPermi="['marketing:participant:edit']">签到</el-button><el-button link type="primary" size="small" @click="handleConvertLead(scope.row)" v-if="!scope.row.leadId" v-hasPermi="['marketing:participant:convert']">转线索</el-button><el-tag v-if="scope.row.leadId" type="success" size="small">已转线索</el-tag></template></el-table-column>
            </el-table>
          </el-tab-pane>
          <!-- 活动效果 -->
          <el-tab-pane label="活动效果" name="result">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewResult')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></span>活动效果数据</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewResult }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewResult">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">实际参与</span><div class="rd-value">{{ viewForm.actualCount || 0 }} 人</div></div>
                  <div class="rd-item"><span class="rd-label">获取线索</span><div class="rd-value">{{ viewForm.leadCount || 0 }} 个</div></div>
                  <div class="rd-item"><span class="rd-label">MQL数量</span><div class="rd-value">{{ viewForm.mqlCount || 0 }} 个</div></div>
                  <div class="rd-item"><span class="rd-label">SQL数量</span><div class="rd-value">{{ viewForm.sqlCount || 0 }} 个</div></div>
                  <div class="rd-item"><span class="rd-label">转化商机</span><div class="rd-value">{{ viewForm.opportunityCount || 0 }} 个</div></div>
                  <div class="rd-item"><span class="rd-label">成交金额</span><div class="rd-value rd-amount rd-amount--negative">￥{{ formatAmount(viewForm.dealAmount) }}</div></div>
                  <div class="rd-item"><span class="rd-label">实际ROI</span><div class="rd-value">{{ viewForm.actualRoi || 0 }}%</div></div>
                </div>
              </div>
            </section>
            <div style="padding: 8px 0;"><el-button type="warning" icon="DataAnalysis" @click="handleReview" v-hasPermi="['marketing:activity:review']" v-if="viewForm.activityStatus === '3'">一键复盘</el-button></div>
          </el-tab-pane>
          <!-- 报名推广 -->
          <el-tab-pane label="报名推广" name="promote" v-loading="viewPromoteLoading">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewPromote')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></span>报名推广</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewPromote }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewPromote">
                <el-row :gutter="20">
                  <el-col :span="10"><div style="text-align: center;"><canvas ref="viewQrRef" style="border-radius: 8px; border: 1px solid #ebeef5;"></canvas><div style="margin-top: 12px;"><el-button type="primary" size="small" icon="Download" @click="downloadViewQr">下载二维码</el-button><el-button type="success" size="small" icon="Promotion" @click="openViewPromotePage">预览报名页</el-button></div></div></el-col>
                  <el-col :span="14">
                    <div class="rd-grid">
                      <div class="rd-item rd-item--full"><span class="rd-label">报名链接</span><div class="rd-value"><el-input :model-value="viewPromoteUrl" readonly><template #append><el-button @click="copyViewPromoteUrl">复制链接</el-button></template></el-input></div></div>
                      <div class="rd-item"><span class="rd-label">报名人数</span><div class="rd-value">{{ viewForm.signupCount || 0 }} 人</div></div>
                      <div class="rd-item"><span class="rd-label">签到人数</span><div class="rd-value">{{ viewForm.signedCount || 0 }} 人</div></div>
                      <div class="rd-item"><span class="rd-label">目标人数</span><div class="rd-value">{{ viewForm.targetCount || 0 }} 人</div></div>
                      <div class="rd-item"><span class="rd-label">转化线索</span><div class="rd-value">{{ viewForm.convertedLeadCount || 0 }} 个</div></div>
                    </div>
                    <el-divider content-position="left">使用说明</el-divider>
                    <div style="font-size: 13px; color: #6b7280; line-height: 2;"><p><b>1. 线上推广</b>：复制链接通过微信/邮件/短信发给客户</p><p><b>2. 线下推广</b>：下载二维码印制在海报/易拉宝/桌牌上</p><p><b>3. 现场签到</b>：客户报名后获得签到码，活动当天扫码签到</p><p><b>4. 线索转化</b>：签到后在"参与人"Tab一键转线索</p></div>
                  </el-col>
                </el-row>
              </div>
            </section>
          </el-tab-pane>
          <!-- 操作日志 -->
          <el-tab-pane label="操作日志" name="logs">
            <section class="rd-card">
              <div class="rd-card-header" @click="toggleCard('viewLogs')">
                <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>操作日志</div>
                <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewLogs }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
              </div>
              <div class="rd-card-body" v-show="!collapsedCards.viewLogs">
                <div v-if="viewStatusLogs.length > 0" class="rd-timeline">
                  <div v-for="log in viewStatusLogs" :key="log.logId" class="rd-timeline-item">
                    <div class="rd-timeline-dot rd-timeline-dot--success"></div>
                    <div class="rd-timeline-content">
                      <div class="rd-timeline-header">
                        <span class="rd-timeline-title"><el-tag size="small" :type="statusTagType(log.fromStatus)">{{ statusLabel(log.fromStatus) }}</el-tag> → <el-tag size="small" :type="statusTagType(log.toStatus)">{{ statusLabel(log.toStatus) }}</el-tag></span>
                        <span class="rd-timeline-time">{{ log.createTime }}</span>
                      </div>
                      <div class="rd-timeline-body"><div class="rd-item"><span class="rd-label">操作人</span><div class="rd-value">{{ log.operatorName }}</div></div></div>
                      <div class="rd-timeline-comment" v-if="log.remark">{{ log.remark }}</div>
                    </div>
                  </div>
                </div>
                <div v-else class="rd-empty"><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg><p class="rd-empty-text">暂无操作日志</p></div>
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 复盘弹窗 -->
    <el-dialog v-model="reviewOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
          <span class="rd-detail-header-title">活动复盘</span>
        </div>
      </template>
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">实际参与（签到）</span><div class="rd-value">{{ reviewData.signedCount }}</div></div>
        <div class="rd-item"><span class="rd-label">获取线索</span><div class="rd-value">{{ reviewData.leadCount }}</div></div>
        <div class="rd-item"><span class="rd-label">MQL数量</span><div class="rd-value">{{ reviewData.mqlCount }}</div></div>
        <div class="rd-item"><span class="rd-label">SQL数量</span><div class="rd-value">{{ reviewData.sqlCount }}</div></div>
        <div class="rd-item"><span class="rd-label">转化商机</span><div class="rd-value">{{ reviewData.opportunityCount }}</div></div>
        <div class="rd-item"><span class="rd-label">成交金额</span><div class="rd-value rd-amount rd-amount--negative">￥{{ formatAmount(reviewData.dealAmount) }}</div></div>
        <div class="rd-item"><span class="rd-label">活动预算</span><div class="rd-value rd-amount rd-amount--negative">￥{{ formatAmount(reviewData.budget) }}</div></div>
        <div class="rd-item"><span class="rd-label">实际ROI</span><div class="rd-value">{{ reviewData.actualRoi }}%</div></div>
      </div>
      <div style="margin-top: 16px;"><el-alert title="系统已根据参与人签到和线索转化数据自动统计，您可修改后保存" type="info" :closable="false" /></div>
      <template #footer><el-button type="primary" @click="saveReviewData">保存复盘</el-button><el-button @click="reviewOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- 新增参与人弹窗 -->
    <el-dialog v-model="participantOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg></div>
          <span class="rd-detail-header-title">新增参与人</span>
        </div>
      </template>
      <el-form ref="participantRef" :model="participantForm" :rules="participantRules" label-width="120px">
        <el-form-item label="联系人库"><el-select v-model="participantForm.contactId" filterable clearable placeholder="选择后自动带出信息" style="width: 100%" @change="onContactSelect"><el-option v-for="c in viewContactOptions" :key="c.contactId" :label="c.name + ' (' + (c.customerName||'') + ')'" :value="c.contactId" /></el-select></el-form-item>
        <el-form-item label="客户名称" prop="companyName"><el-input v-model="participantForm.companyName" /></el-form-item>
        <el-form-item label="联系人" prop="contactName"><el-input v-model="participantForm.contactName" /></el-form-item>
        <el-form-item label="手机号" prop="contactPhone"><el-input v-model="participantForm.contactPhone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="participantForm.contactEmail" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="participantForm.position" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitParticipant">确 定</el-button><el-button @click="participantOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- 负责人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择负责人" @confirm="onUserPickerConfirm" />

    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择所属部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="活动管理业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已发布</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">进行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已结束</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已归档</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="primary">已发布</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">进行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已取消</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">新建活动后的初始状态，可编辑、删除、发布</el-descriptions-item>
          <el-descriptions-item label="已发布">活动已对外发布，可开始活动或取消活动。线上活动可通过"报名推广"获取报名链接</el-descriptions-item>
          <el-descriptions-item label="进行中">活动正在进行中，可结束活动或取消活动。参与人可签到</el-descriptions-item>
          <el-descriptions-item label="已结束">活动已结束，可进行活动结算（录入实际成交额等）或归档</el-descriptions-item>
          <el-descriptions-item label="已归档">活动已归档归档，不可再修改。可查看活动数据和参与人记录</el-descriptions-item>
          <el-descriptions-item label="已取消">已发布或进行中的活动被取消，不再对外展示</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>状态流转：</strong>草稿 → 已发布 → 进行中 → 已结束 → 已归档，每个状态需通过对应操作按钮触发</p>
          <p>• <strong>参与人管理：</strong>在活动详情中可新增参与人、签到、转化为线索</p>
          <p>• <strong>活动结算：</strong>已结束或已归档的活动可进行结算，录入实际成交额和成交客户</p>
          <p>• <strong>复制活动：</strong>可复制现有活动创建新活动，便于快速创建类似活动</p>
          <p>• <strong>批量操作：</strong>支持批量发布、批量归档、批量改负责人</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkActivity">
import QRCode from 'qrcode'
import { CircleClose, ArrowRight, ArrowDown, QuestionFilled, WarningFilled, Search, Filter, Edit, Delete, Download, Promotion, FolderOpened, User } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { listActivity, getActivity, addActivity, updateActivity, delActivity, changeActivityStatus, copyActivity, batchChangeStatus, batchUpdateOwner, getRegisterUrl, getActivityStatusLogs, reviewActivity, saveReview } from '@/api/mk/activity'
import { getParticipantsByActivity, addParticipant, signInParticipant, convertToLead } from '@/api/mk/participant'
import { listContact } from '@/api/mk/contact'
import { listUser } from '@/api/system/user'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'schedule', 'owner', 'detail', 'attachment', 'result', 'viewInfo', 'viewSchedule', 'viewOwner', 'viewDesc', 'viewAttach', 'viewResult', 'viewPromote', 'viewLogs', 'settleCost', 'settleLead', 'settleDeal'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_activity_index')
const { marketing_activity_type, marketing_activity_form, marketing_activity_status, marketing_participate_status } = proxy.useDict('marketing_activity_type', 'marketing_activity_form', 'marketing_activity_status', 'marketing_participate_status')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0, '5': 0 })
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const cancelOpen = ref(false)
const cancelRemark = ref('')
const cancelRow = ref({})
// 活动结算
const settleOpen = ref(false)
const settleForm = ref({})
const ownerOpen = ref(false)
const batchOwnerUserId = ref(null)
const batchOwnerName = ref('')
const batchOwnerDeptId = ref(null)
const batchOwnerPhone = ref('')
// P3-15: 报名推广中心
const promoteOpen = ref(false)
const promoteLoading = ref(false)
const promoteData = ref({ url: '', token: '', activityName: '' })
const qrCanvasRef = ref(null)
// 详情弹窗
const viewOpen = ref(false)
const viewForm = ref({})
const viewDetailTab = ref('basic')
const viewParticipants = ref([])
const viewStatusLogs = ref([])
const viewContactOptions = ref([])
const viewPromoteLoading = ref(false)
const viewPromoteUrl = ref('')
const viewQrRef = ref(null)
const viewAttachmentList = computed(() => {
  if (!viewForm.value.attachments) return []
  return viewForm.value.attachments.split(',').filter(url => url).map(url => ({
    name: url.substring(url.lastIndexOf('/') + 1),
    url: url
  }))
})
// 复盘
const reviewOpen = ref(false)
const reviewData = ref({})
// 参与人
const participantOpen = ref(false)
const participantForm = ref({})
const participantRules = {
  companyName: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }],
  contactName: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
}

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = { Authorization: 'Bearer ' + getToken() }
const filePreviewRef = ref(null)

// 活动效果字段仅在活动结束后可填写
const isSettlementPhase = computed(() => form.value.activityId != null && ['3', '4'].includes(form.value.activityStatus))

// 附件文件列表（从 form.attachments 逗号分隔字符串解析）
const attachmentFileList = computed(() => {
  if (!form.value.attachments) return []
  return form.value.attachments.split(',').filter(url => url).map(url => ({
    name: url.substring(url.lastIndexOf('/') + 1),
    url: url
  }))
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, activityNo: undefined, activityName: undefined, activityType: undefined, activityStatus: undefined, params: {} },
  rules: {
    activityName: [{ required: true, message: '活动名称不能为空', trigger: 'blur' }],
    activityType: [{ required: true, message: '活动类型不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  activityNo: { label: '活动编号', visible: true },
  activityName: { label: '活动名称', visible: true },
  activityType: { label: '类型', visible: true },
  activityForm: { label: '形式', visible: true },
  activityStatus: { label: '状态', visible: true },
  startTime: { label: '开始时间', visible: true },
  endTime: { label: '结束时间', visible: true },
  budget: { label: '预算', visible: true },
  signupCount: { label: '报名', visible: true },
  signedCount: { label: '签到', visible: true },
  convertedLeadCount: { label: '线索', visible: true },
  dealAmount: { label: '成交额', visible: true },
  userName: { label: '负责人', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_activity_columns')
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

const statusTabList = computed(() => marketing_activity_status.value)
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.activityNo) count++
  if (queryParams.value.activityName) count++
  if (queryParams.value.activityType) count++
  if (queryParams.value.activityStatus) count++
  return count
})

function getList() {
  loading.value = true
  listActivity(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }).catch(() => { loading.value = false })
}

/** 打开负责人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.userId)
}
/** 负责人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.userId = user.userId
  form.value.userName = user.nickName
  if (user.phonenumber) form.value.ownerPhone = user.phonenumber
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

function onBatchUserChange(userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user) {
      batchOwnerName.value = user.nickName
      batchOwnerDeptId.value = user.deptId
      batchOwnerPhone.value = user.phonenumber || ''
    }
  }
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.activityNo = undefined; queryParams.value.activityName = undefined; queryParams.value.activityType = undefined; queryParams.value.activityStatus = undefined; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery()
}
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.activityStatus = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() {
  listActivity({ pageNum: 1, pageSize: 999 }, { suppressError: true }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0, '5': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.activityStatus] !== undefined) counts[r.activityStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-done', '4': 'tab-void', '5': 'tab-reject' }; return map[value] || '' }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'green', '4': 'gray', '5': 'red' }; return map[status] || 'gray' }
function statusLabel(status) { const item = marketing_activity_status.value.find(d => d.value == status); return item ? item.label : '-' }
function typeBadgeClass(type) { const map = { '1': 'blue', '2': 'green', '3': 'violet', '4': 'amber' }; return map[type] || 'gray' }
function typeLabel(type) { const item = marketing_activity_type.value.find(d => d.value == type); return item ? item.label : '-' }
function formBadgeClass(form) { const map = { '1': 'blue', '2': 'green' }; return map[form] || 'gray' }
function formLabel(form) { const item = marketing_activity_form.value.find(d => d.value == form); return item ? item.label : '-' }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.activityId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { activityNo: undefined, activityName: undefined, activityType: undefined, activityForm: undefined, activityStatus: '0', startTime: undefined, endTime: undefined, location: undefined, budget: 0, cost: 0, targetCount: 0, targetLeadCount: 0, actualCount: 0, leadCount: 0, mqlCount: 0, sqlCount: 0, opportunityCount: 0, dealAmount: 0, userId: undefined, userName: undefined, ownerPhone: undefined, deptId: undefined, deptName: undefined, summary: undefined, content: undefined, attachments: undefined, remark: undefined }
  proxy.resetForm('activityRef')
}
function handleAdd() { reset(); collapsedCards.result = true; open.value = true; title.value = '新增营销活动' }
function handleUpdate(row) { reset(); getActivity(row.activityId || ids.value[0]).then(res => { form.value = res.data; collapsedCards.result = !['3', '4'].includes(res.data.activityStatus); open.value = true; title.value = '修改营销活动' }) }
function handleView(row) {
  getActivity(row.activityId).then(res => {
    viewForm.value = res.data
    viewDetailTab.value = 'basic'
    Object.keys(collapsedCards).forEach(k => { if (k.startsWith('view')) collapsedCards[k] = false })
    viewOpen.value = true
  })
}
function loadViewRelations() {
  if (!viewForm.value.activityId) return
  getParticipantsByActivity(viewForm.value.activityId).then(res => { viewParticipants.value = res.data || [] })
  getActivityStatusLogs(viewForm.value.activityId).then(res => { viewStatusLogs.value = res.data || [] })
  loadViewPromoteUrl()
}
function statusTagType(val) {
  const map = { '0': 'info', '1': 'primary', '2': 'success', '3': 'warning', '4': 'info', '5': 'danger' }
  return map[val] || 'info'
}
// 复盘
function handleReview() {
  reviewActivity(viewForm.value.activityId).then(res => {
    reviewData.value = res.data
    reviewOpen.value = true
  })
}
function saveReviewData() {
  const data = {
    activityId: viewForm.value.activityId,
    actualCount: reviewData.value.signedCount,
    leadCount: reviewData.value.leadCount,
    mqlCount: reviewData.value.mqlCount,
    sqlCount: reviewData.value.sqlCount,
    opportunityCount: reviewData.value.opportunityCount,
    dealAmount: reviewData.value.dealAmount,
    actualRoi: reviewData.value.actualRoi,
    cost: reviewData.value.budget
  }
  saveReview(data).then(() => {
    proxy.$modal.msgSuccess('复盘保存成功')
    reviewOpen.value = false
    getActivity(viewForm.value.activityId).then(res => { viewForm.value = res.data })
  })
}
// 参与人操作
function handleAddParticipant() {
  participantForm.value = { activityId: viewForm.value.activityId, source: '0' }
  if (viewContactOptions.value.length === 0) listContact({ pageNum: 1, pageSize: 9999 }).then(res => { viewContactOptions.value = res.rows })
  participantOpen.value = true
}
function onContactSelect(contactId) {
  if (contactId) {
    const c = viewContactOptions.value.find(i => i.contactId === contactId)
    if (c) {
      participantForm.value.companyName = c.customerName
      participantForm.value.contactName = c.name
      participantForm.value.contactPhone = c.phone
      participantForm.value.contactEmail = c.email
      participantForm.value.position = c.position
    }
  }
}
function submitParticipant() {
  proxy.$refs['participantRef'].validate(valid => {
    if (valid) {
      addParticipant(participantForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        participantOpen.value = false
        getParticipantsByActivity(viewForm.value.activityId).then(res => { viewParticipants.value = res.data || [] })
      })
    }
  })
}
function handleSignIn(row) {
  proxy.$modal.confirm('确认签到？').then(() => signInParticipant(row.participantId)).then(() => {
    proxy.$modal.msgSuccess('签到成功')
    getParticipantsByActivity(viewForm.value.activityId).then(res => { viewParticipants.value = res.data || [] })
  }).catch(() => {})
}
function handleConvertLead(row) {
  proxy.$modal.confirm('确认将该参与人转化为线索？').then(() => convertToLead(row.participantId)).then(() => {
    proxy.$modal.msgSuccess('转线索成功')
    getParticipantsByActivity(viewForm.value.activityId).then(res => { viewParticipants.value = res.data || [] })
  }).catch(() => {})
}
// 详情页报名推广
async function loadViewPromoteUrl() {
  viewPromoteLoading.value = true
  try {
    const res = await getRegisterUrl(viewForm.value.activityId)
    viewPromoteUrl.value = window.location.origin + res.url
    await nextTick()
    if (viewQrRef.value) {
      await QRCode.toCanvas(viewQrRef.value, viewPromoteUrl.value, { width: 200, margin: 2, color: { dark: '#1a1a2e', light: '#ffffff' } })
    }
  } finally { viewPromoteLoading.value = false }
}
function copyViewPromoteUrl() {
  navigator.clipboard.writeText(viewPromoteUrl.value).then(() => proxy.$modal.msgSuccess('链接已复制到剪贴板')).catch(() => {
    const input = document.createElement('input'); input.value = viewPromoteUrl.value; document.body.appendChild(input); input.select(); document.execCommand('copy'); document.body.removeChild(input); proxy.$modal.msgSuccess('链接已复制到剪贴板')
  })
}
function downloadViewQr() {
  if (!viewQrRef.value) return
  const link = document.createElement('a'); link.download = '活动报名二维码_' + (viewForm.value.activityName || '') + '.png'; link.href = viewQrRef.value.toDataURL('image/png'); link.click()
}
function openViewPromotePage() { window.open(viewPromoteUrl.value, '_blank') }
function submitForm() {
  proxy.$refs['activityRef'].validate(valid => {
    if (valid) {
      if (form.value.activityId != undefined) { updateActivity(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addActivity(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const activityIds = row.activityId || ids.value; proxy.$modal.confirm('确认删除编号为"' + activityIds + '"的数据？').then(() => delActivity(activityIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/activity/export', { ...queryParams.value }, `activity_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// P0-1: 状态流转
function handleChangeStatus(row, toStatus) {
  proxy.$modal.confirm('确认将活动"' + row.activityName + '"状态变更为[' + statusLabel(toStatus) + ']？').then(() => {
    changeActivityStatus(row.activityId, { toStatus, remark: '' }).then(() => {
      proxy.$modal.msgSuccess('状态变更成功')
      getList()
      // 结束活动后提示是否立即结算
      if (toStatus === '3') {
        setTimeout(() => {
          proxy.$modal.confirm('活动已结束，是否立即填写活动效果进行结算？').then(() => {
            handleSettle(row)
          }).catch(() => {})
        }, 500)
      }
    })
  }).catch(() => {})
}

// 活动结算
function handleSettle(row) {
  getActivity(row.activityId).then(res => {
    settleForm.value = res.data
    settleOpen.value = true
  })
}
function confirmSettle() {
  updateActivity(settleForm.value).then(() => {
    proxy.$modal.msgSuccess('结算成功')
    settleOpen.value = false
    getList()
  })
}
function handleCancel(row) {
  cancelRow.value = row
  cancelRemark.value = ''
  cancelOpen.value = true
}
function confirmCancel() {
  if (!cancelRemark.value) { proxy.$modal.msgError('请填写取消原因'); return }
  changeActivityStatus(cancelRow.value.activityId, { toStatus: '5', remark: cancelRemark.value }).then(() => {
    proxy.$modal.msgSuccess('已取消')
    cancelOpen.value = false
    getList()
  })
}

// P2-11: 活动复制
function handleCopy(row) {
  proxy.$modal.confirm('确认复制活动"' + row.activityName + '"？').then(() => copyActivity(row.activityId)).then(() => {
    proxy.$modal.msgSuccess('复制成功')
    getList()
  }).catch(() => {})
}

// 操作下拉菜单统一分发
function handleCommand(cmd, row) {
  switch (cmd) {
    case 'publish': handleChangeStatus(row, '1'); break
    case 'start': handleChangeStatus(row, '2'); break
    case 'finish': handleChangeStatus(row, '3'); break
    case 'archive': handleChangeStatus(row, '4'); break
    case 'settle': handleSettle(row); break
    case 'cancel': handleCancel(row); break
    case 'copy': handleCopy(row); break
    case 'promote': handleRegisterUrl(row); break
    case 'delete': handleDelete(row); break
  }
}

// P3-12: 批量操作
function handleBatchPublish() {
  proxy.$modal.confirm('确认批量发布选中的' + ids.value.length + '个活动？').then(() => {
    batchChangeStatus({ activityIds: ids.value, status: '1' }).then(() => {
      proxy.$modal.msgSuccess('批量发布成功')
      getList()
    })
  }).catch(() => {})
}
function handleBatchArchive() {
  proxy.$modal.confirm('确认批量归档选中的' + ids.value.length + '个活动？').then(() => {
    batchChangeStatus({ activityIds: ids.value, status: '4' }).then(() => {
      proxy.$modal.msgSuccess('批量归档成功')
      getList()
    })
  }).catch(() => {})
}
function handleBatchOwner() {
  batchOwnerUserId.value = null
  batchOwnerName.value = ''
  ownerOpen.value = true
}
function confirmBatchOwner() {
  if (!batchOwnerUserId.value) { proxy.$modal.msgError('请选择负责人'); return }
  batchUpdateOwner({ activityIds: ids.value, userId: batchOwnerUserId.value, userName: batchOwnerName.value, deptId: batchOwnerDeptId.value, ownerPhone: batchOwnerPhone.value }).then(() => {
    proxy.$modal.msgSuccess('批量修改成功')
    ownerOpen.value = false
    getList()
  })
}

// P3-15: 报名推广中心
async function handleRegisterUrl(row) {
  promoteOpen.value = true
  promoteLoading.value = true
  promoteData.value = { url: '', token: '', activityName: row.activityName }
  try {
    const res = await getRegisterUrl(row.activityId)
    const fullUrl = window.location.origin + res.url
    promoteData.value = { url: fullUrl, token: res.token, activityName: row.activityName }
    // 生成二维码
    await nextTick()
    if (qrCanvasRef.value) {
      await QRCode.toCanvas(qrCanvasRef.value, fullUrl, {
        width: 240,
        margin: 2,
        color: { dark: '#1a1a2e', light: '#ffffff' }
      })
    }
  } finally {
    promoteLoading.value = false
  }
}
function copyPromoteUrl() {
  navigator.clipboard.writeText(promoteData.value.url).then(() => {
    proxy.$modal.msgSuccess('链接已复制到剪贴板')
  }).catch(() => {
    // fallback
    const input = document.createElement('input')
    input.value = promoteData.value.url
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    proxy.$modal.msgSuccess('链接已复制到剪贴板')
  })
}
function downloadQrCode() {
  if (!qrCanvasRef.value) return
  const link = document.createElement('a')
  link.download = '活动报名二维码_' + promoteData.value.activityName + '.png'
  link.href = qrCanvasRef.value.toDataURL('image/png')
  link.click()
}
function openPromotePage() {
  window.open(promoteData.value.url, '_blank')
}

// 附件上传相关
function handleBeforeAttachmentUpload(file) {
  const allowedTypes = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt', 'png', 'jpg', 'jpeg', 'gif', 'bmp', 'zip', 'rar', '7z', 'gz', 'tar']
  const ext = file.name.split('.').pop().toLowerCase()
  if (!allowedTypes.includes(ext)) {
    proxy.$modal.msgError('不支持的文件格式：' + ext)
    return false
  }
  if (file.size / 1024 / 1024 > 20) {
    proxy.$modal.msgError('文件大小不能超过 20MB')
    return false
  }
  proxy.$modal.loading('正在上传文件，请稍候...')
  return true
}
function handleAttachmentSuccess(res) {
  proxy.$modal.closeLoading()
  if (res.code === 200) {
    const url = res.fileName
    const current = form.value.attachments ? form.value.attachments.split(',').filter(u => u) : []
    current.push(url)
    form.value.attachments = current.join(',')
    proxy.$modal.msgSuccess('上传成功')
  } else {
    proxy.$modal.msgError(res.msg || '上传失败')
  }
}
function handleAttachmentError() {
  proxy.$modal.closeLoading()
  proxy.$modal.msgError('上传失败')
}
function handleAttachmentDelete(index) {
  const current = form.value.attachments ? form.value.attachments.split(',').filter(u => u) : []
  current.splice(index, 1)
  form.value.attachments = current.length > 0 ? current.join(',') : undefined
}
function handleAttachmentPreview(file) {
  filePreviewRef.value?.open(file.url, file.name)
}

const showStatusHelp = ref(false)

listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
getList()
</script>

<style scoped>
.promote-center {
  text-align: center;
}
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
.promote-activity-name {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
}
.promote-qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}
.promote-qr-canvas {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.promote-qr-tip {
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
}
.promote-url-section {
  margin-bottom: 16px;
}
.promote-url-label {
  text-align: left;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.promote-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}
.promote-guide {
  text-align: left;
  font-size: 13px;
  color: #606266;
  line-height: 2;
}
.promote-guide p {
  margin: 0;
}
.attachment-list {
  margin-top: 12px;
  width: 100%;
}
.attachment-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  transition: background 0.2s;
}
.attachment-list-item:hover {
  background: #f5f7fa;
}
.attachment-name {
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
  vertical-align: middle;
}
.settle-field-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 2px;
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

<!-- 非scoped样式：修复 el-dialog append-to-body 下 el-upload 在 rd-card(overflow:hidden) 内无法点击的问题 -->
<style>
.rd-dialog .rd-card .rd-card-body {
  overflow: visible;
}
.rd-dialog .rd-card .el-upload {
  position: relative;
  z-index: 1;
}
/* 详情弹窗加宽：取消 rd-page 的 max-width 限制 */
.rd-dialog .rd-page--wide {
  max-width: none;
}
</style>
