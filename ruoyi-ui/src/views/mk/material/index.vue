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
          <label>素材类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.materialType" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="d in marketing_material_type" :key="d.value" :label="d.label" :value="d.value" />
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
          <label>素材编号</label>
          <div class="control">
            <el-input v-model="queryParams.materialNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>标签</label>
          <div class="control">
            <el-input v-model="queryParams.tags" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
      <!-- 类型 Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTypeTab === 'all' }" @click="handleTypeTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ typeCounts.all }}</span>
          </button>
          <button v-for="s in typeTabList" :key="s.value"
            class="status-tab"
            :class="[typeTabClass(s.value), { 'is-active': activeTypeTab === s.value }]"
            @click="handleTypeTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ typeCounts[s.value] || 0 }}</span>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:material:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate()" v-hasPermi="['marketing:material:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['marketing:material:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:material:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_material_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
          <el-table-column label="素材编号" prop="materialNo" key="materialNo" :width="colWidth('materialNo', 150)" resizable show-overflow-tooltip v-if="columns.materialNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.materialNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="标题" prop="title" key="title" :min-width="colWidth('title', 200)" resizable show-overflow-tooltip v-if="columns.title.visible">
            <template #default="scope">
              <div v-if="scope.row.materialType === '1' && scope.row.fileUrl" class="title-cell">
                <el-image class="mat-thumb" :src="previewSrc(scope.row)" fit="cover" :preview-src-list="[previewSrc(scope.row)]" preview-teleported />
                <span>{{ scope.row.title }}</span>
              </div>
              <span v-else>{{ scope.row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column label="类型" prop="materialType" key="materialType" :width="colWidth('materialType', 100)" resizable align="center" v-if="columns.materialType.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.materialType)">
                <span class="dot"></span>{{ typeLabel(scope.row.materialType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="标签" prop="tags" key="tags" :min-width="colWidth('tags', 180)" resizable v-if="columns.tags.visible">
            <template #default="scope">
              <template v-if="splitTags(scope.row.tags).length">
                <el-tag v-for="t in splitTags(scope.row.tags)" :key="t" size="small" effect="plain" class="tag-item">{{ t }}</el-tag>
              </template>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="引用次数" prop="useCount" key="useCount" :width="colWidth('useCount', 100)" resizable align="center" v-if="columns.useCount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatCount(scope.row.useCount) }}</span></template>
          </el-table-column>
          <el-table-column label="创建人" prop="userName" key="userName" :width="colWidth('userName', 100)" resizable v-if="columns.userName.visible">
            <template #default="scope">{{ scope.row.userName || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['marketing:material:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:material:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:material:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="materialRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row>
                <el-col :span="24"><el-form-item label="素材标题" prop="title"><el-input v-model="form.title" placeholder="请输入素材标题" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="素材类型" prop="materialType">
                  <el-radio-group v-model="form.materialType" @change="handleTypeChange">
                    <el-radio v-for="d in marketing_material_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="标签" prop="tags"><el-input v-model="form.tags" placeholder="多个标签用英文逗号分隔" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('relation')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></span>关联信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.relation }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.relation">
              <el-row>
                <el-col :span="24"><el-form-item label="关联活动" prop="activityId">
                  <el-input v-model="form.activityName" readonly placeholder="点击选择关联活动（选填）" style="width: 100%" @click="openActivityPicker">
                    <template #append>
                      <el-button icon="Search" @click="openActivityPicker" />
                    </template>
                    <template #suffix>
                      <el-icon v-if="form.activityId" class="clear-icon" @click.stop="clearActivity"><CircleClose /></el-icon>
                    </template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('content')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>素材内容</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.content }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.content">
              <el-row>
                <el-col v-if="form.materialType === '0'" :span="24">
                  <el-form-item label="文案内容" prop="content">
                    <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入文案内容" />
                  </el-form-item>
                </el-col>
                <el-col v-else :span="24">
                  <el-form-item label="素材文件">
                    <el-upload
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :accept="acceptAttr"
                      :before-upload="handleBeforeUpload"
                      :on-success="handleUploadSuccess"
                      :on-error="handleUploadError"
                    >
                      <el-button type="primary" icon="Upload">上传文件</el-button>
                      <template #tip>
                        <div class="el-upload__tip">{{ uploadTip }}</div>
                      </template>
                    </el-upload>
                    <div class="material-file" v-if="form.fileUrl || form.fileName">
                      <el-icon class="material-file-icon"><Document /></el-icon>
                      <span class="material-file-name">{{ displayFileName }}</span>
                      <span v-if="form.fileSize" class="material-file-size">{{ formatFileSize(form.fileSize) }}</span>
                      <el-link :underline="false" type="danger" @click="clearFile">删除</el-link>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('other')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.other">
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

    <!-- 素材详情弹窗 -->
    <el-dialog v-model="viewOpen" width="760px" append-to-body draggable class="rd-dialog detail-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">素材详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>素材信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">素材编号</span><div class="rd-value">{{ viewForm.materialNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">素材标题</span><div class="rd-value">{{ viewForm.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">素材类型</span><div class="rd-value"><dict-tag :options="marketing_material_type" :value="viewForm.materialType" /></div></div>
              <div class="rd-item"><span class="rd-label">引用次数</span><div class="rd-value">{{ formatCount(viewForm.useCount) }}</div></div>
              <div class="rd-item"><span class="rd-label">标签</span>
                <div class="rd-value">
                  <template v-if="splitTags(viewForm.tags).length">
                    <el-tag v-for="t in splitTags(viewForm.tags)" :key="t" size="small" effect="plain" class="tag-item">{{ t }}</el-tag>
                  </template>
                  <span v-else>暂无</span>
                </div>
              </div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewForm.userName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联活动</span><div class="rd-value">{{ viewForm.activityName || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewContent')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>素材内容</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewContent }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewContent">
            <div class="rd-grid">
            <div class="rd-item rd-item--full" v-if="viewForm.materialType === '0'">
              <span class="rd-label">文案内容</span>
              <div class="rd-value view-content-text" :class="{ 'rd-value--muted': !viewForm.content }">{{ viewForm.content || '暂无' }}</div>
            </div>
            <div class="rd-item rd-item--full" v-else>
              <span class="rd-label">素材文件</span>
              <div class="rd-value">
                <template v-if="viewForm.fileUrl">
                  <el-image v-if="viewForm.materialType === '1'" class="view-image" :src="previewSrc(viewForm)" fit="cover" :preview-src-list="[previewSrc(viewForm)]" preview-teleported />
                  <div class="material-file">
                    <el-icon class="material-file-icon"><Document /></el-icon>
                    <span class="material-file-name">{{ displayFileNameOf(viewForm) }}</span>
                    <span v-if="viewForm.fileSize" class="material-file-size">{{ formatFileSize(viewForm.fileSize) }}</span>
                    <el-link :underline="false" type="primary" :href="previewSrc(viewForm)" target="_blank">查看</el-link>
                  </div>
                </template>
                <span v-else>暂无</span>
              </div>
            </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewOther')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOther }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewOther">
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
        <el-button type="primary" icon="Edit" @click="viewOpen = false; handleUpdate(viewForm)" v-hasPermi="['marketing:material:edit']">编 辑</el-button>
      </template>
    </el-dialog>

    <!-- 关联活动选择器 -->
    <activity-picker ref="activityPickerRef" title="选择关联活动" @confirm="onActivityPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="素材库业务操作说明" width="640px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="素材库">存放推广所需的文案、图片、视频等素材，供内容创作与内容包组装时引用</el-descriptions-item>
          <el-descriptions-item label="文案">文本类素材，直接填写文案内容，无需上传文件</el-descriptions-item>
          <el-descriptions-item label="图片">上传图片文件，列表中可点击缩略图放大预览</el-descriptions-item>
          <el-descriptions-item label="视频">上传视频文件，作为短视频/直播等推广形式的素材</el-descriptions-item>
          <el-descriptions-item label="其他">其他辅助素材文件（文档、压缩包等）</el-descriptions-item>
        </el-descriptions>
        <h4>二、重点业务规则</h4>
        <div class="highlight-card">
          <p>• <strong>类型切换：</strong>文案型与文件型内容互斥，切换类型会清空另一类已填内容</p>
          <p>• <strong>标签：</strong>多个标签用英文逗号分隔，便于素材检索与归类</p>
          <p>• <strong>关联活动：</strong>可选，标记素材归属的营销活动，支持模糊搜索</p>
          <p>• <strong>引用次数：</strong>素材被内容包引用后累加，反映素材使用热度</p>
          <p>• <strong>删除规则：</strong>删除为逻辑删除，已被引用的素材删除后不影响历史内容</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkMaterial">
import { Search, Filter, Edit, Delete, Download, WarningFilled, Upload, Document, ArrowDown, View, CircleClose } from '@element-plus/icons-vue'
import { listMaterial, getMaterial, addMaterial, updateMaterial, delMaterial } from '@/api/mk/material'
import { listActivity } from '@/api/mk/activity'
import { listUser } from '@/api/system/user'
import ActivityPicker from '@/components/ActivityPicker/index.vue'
import { getToken } from '@/utils/auth'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_material_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'relation', 'content', 'other', 'viewBasic', 'viewContent', 'viewOther'])
const { marketing_material_type } = proxy.useDict('marketing_material_type')

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = { Authorization: 'Bearer ' + getToken() }

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewForm = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const activeTypeTab = ref('all')
const typeCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showStatusHelp = ref(false)
const activityPickerRef = ref()
const activityOptions = ref([])
const userOptions = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, materialType: undefined, activityId: undefined, materialNo: undefined, tags: undefined, userId: undefined, params: {} },
  rules: {
    materialType: [{ required: true, message: '素材类型不能为空', trigger: 'change' }],
    title: [{ required: true, message: '素材标题不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  materialNo: { label: '素材编号', visible: true },
  title: { label: '标题', visible: true },
  materialType: { label: '类型', visible: true },
  tags: { label: '标签', visible: true },
  useCount: { label: '引用次数', visible: true },
  userName: { label: '创建人', visible: true },
  createTime: { label: '创建时间', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mk_material_columns')
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

const typeTabList = computed(() => marketing_material_type.value)
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.title) count++
  if (queryParams.value.materialType) count++
  if (queryParams.value.activityId) count++
  if (queryParams.value.materialNo) count++
  if (queryParams.value.tags) count++
  if (queryParams.value.userId) count++
  return count
})

// 上传格式提示与限制
const uploadTip = computed(() => {
  if (form.value.materialType === '1') return '支持 jpg/png/gif/webp 等图片格式，单文件不超过 20MB'
  if (form.value.materialType === '2') return '支持 mp4/mov/avi 等视频格式，单文件不超过 100MB'
  return '支持常见文档/压缩包等格式，单文件不超过 20MB'
})
const acceptAttr = computed(() => {
  if (form.value.materialType === '1') return 'image/*'
  if (form.value.materialType === '2') return 'video/*'
  return undefined
})
/** 已上传文件展示名（fileName 为服务端路径时取末段文件名） */
function displayFileNameOf(m) {
  const name = m.fileName || m.fileUrl
  if (!name) return ''
  return name.substring(name.lastIndexOf('/') + 1)
}
// 已上传文件展示名（当前编辑表单）
const displayFileName = computed(() => displayFileNameOf(form.value))

function getList() {
  loading.value = true
  listMaterial(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadTypeCounts(); applySavedWidths() }).catch(() => { loading.value = false })
}

function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, title: undefined, materialType: undefined, activityId: undefined, materialNo: undefined, tags: undefined, userId: undefined, params: {} }
  activeTypeTab.value = 'all'
  handleQuery()
}
function handleTypeTabClick(type) { activeTypeTab.value = type; queryParams.value.materialType = type === 'all' ? undefined : type; handleQuery() }
function loadTypeCounts() {
  // 基于当前筛选条件（剔除类型与分页）拉取全量数据统计，与列表筛选保持一致
  const query = { ...queryParams.value, pageNum: 1, pageSize: 9999, materialType: undefined, params: { ...queryParams.value.params } }
  listMaterial(query, { suppressError: true }).then(res => {
    const rows = res.rows || []
    const counts = { all: rows.length }
    marketing_material_type.value.forEach(d => { counts[d.value] = 0 })
    rows.forEach(r => { if (counts[r.materialType] !== undefined) counts[r.materialType]++ })
    typeCounts.value = counts
  }).catch(() => {})
}
function typeTabClass(value) { const map = { '0': 'tab-audit', '1': 'tab-done', '2': 'tab-draft', '3': 'tab-void' }; return map[value] || '' }
function typeBadgeClass(type) {
  const item = marketing_material_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}
function typeLabel(type) { const item = marketing_material_type.value.find(d => d.value == type); return item ? item.label : '-' }
/** 标签逗号拆分 */
function splitTags(tags) {
  if (!tags) return []
  return tags.split(',').map(t => t.trim()).filter(Boolean)
}
/** 引用次数千分位显示 */
function formatCount(count) {
  if (count == null) return '-'
  return Number(count).toLocaleString('zh-CN')
}
/** 图片预览地址：/profile/ 资源需经 /common/file/access 携带 token 访问（img 无法带 Authorization 头） */
function previewSrc(row) {
  const url = row.fileUrl
  if (!url) return ''
  // 兼容历史数据中的绝对地址：截取 /profile/ 起始的相对资源路径
  const idx = url.indexOf('/profile/')
  const resource = idx >= 0 ? url.substring(idx) : url
  if (!resource.startsWith('/profile/')) {
    return url.startsWith('http') ? url : import.meta.env.VITE_APP_BASE_API + url
  }
  return import.meta.env.VITE_APP_BASE_API + '/common/file/access?resource=' + encodeURIComponent(resource) + '&token=' + getToken()
}
/** 文件大小格式化 */
function formatFileSize(size) {
  if (size == null) return ''
  if (size < 1024) return size + 'B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
  return (size / 1024 / 1024).toFixed(2) + 'MB'
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.materialId); single.value = selection.length !== 1; multiple.value = !selection.length }

/** 类型切换：清理另一类内容字段 */
function handleTypeChange(val) {
  if (val === '0') {
    form.value.fileUrl = undefined
    form.value.fileName = undefined
    form.value.fileSize = undefined
  } else {
    form.value.content = undefined
  }
}

/** 上传前校验文件大小 */
function handleBeforeUpload(file) {
  const isVideo = form.value.materialType === '2'
  const limit = isVideo ? 100 * 1024 * 1024 : 20 * 1024 * 1024
  if (file.size > limit) {
    proxy.$modal.msgError(isVideo ? '视频文件不能超过 100MB' : '文件大小不能超过 20MB')
    return false
  }
  return true
}
/** 上传成功：取 res.url / res.fileName 存库（统一截取 /profile/ 起始相对路径，避免域名端口变更后失效） */
function handleUploadSuccess(res, file) {
  if (res.code !== 200) {
    proxy.$modal.msgError(res.msg || '上传失败')
    return
  }
  let url = res.url || ''
  const idx = url.indexOf('/profile/')
  if (idx >= 0) url = url.substring(idx)
  form.value.fileUrl = url
  form.value.fileName = res.fileName
  form.value.fileSize = file.size
  proxy.$modal.msgSuccess('上传成功')
}
function handleUploadError() { proxy.$modal.msgError('上传失败') }
/** 删除已上传文件 */
function clearFile() {
  form.value.fileUrl = undefined
  form.value.fileName = undefined
  form.value.fileSize = undefined
}

function reset() {
  form.value = { materialId: undefined, materialType: '0', title: undefined, content: undefined, fileUrl: undefined, fileName: undefined, fileSize: undefined, tags: undefined, activityId: undefined, activityName: undefined, materialStatus: '0', remark: undefined }
  proxy.resetForm('materialRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增素材' }
function handleUpdate(row) {
  reset()
  getMaterial(row.materialId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改素材' })
}
/** 查看详情 */
function handleView(row) {
  getMaterial(row.materialId).then(res => {
    viewForm.value = res.data || {}
    Object.keys(collapsedCards).forEach(k => { collapsedCards[k] = false })
    viewOpen.value = true
  })
}
function submitForm() {
  proxy.$refs['materialRef'].validate(valid => {
    if (valid) {
      if (form.value.materialId != undefined) { updateMaterial(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addMaterial(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const materialIds = row.materialId || ids.value
  proxy.$modal.confirm('确认删除选中的素材？').then(() => delMaterial(materialIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

/** 关联活动选择器 */
function openActivityPicker() { activityPickerRef.value.open(form.value.activityId) }
function onActivityPickerConfirm(activity) {
  form.value.activityId = activity.activityId
  form.value.activityName = activity.activityName
}
function clearActivity() {
  form.value.activityId = undefined
  form.value.activityName = undefined
}
function handleExport() { proxy.download('mk/material/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'materialId'), `material_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

listActivity({ pageNum: 1, pageSize: 9999 }).then(res => { activityOptions.value = res.rows }).catch(() => {})
listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }).catch(() => {})
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
/* 标题列：图片缩略图 + 标题 */
.title-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
}
.mat-thumb {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  flex-shrink: 0;
  cursor: pointer;
}
/* 标签列 */
.tag-item {
  margin: 1px 4px 1px 0;
}
/* 已上传文件展示 */
.material-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 6px 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  width: fit-content;
  max-width: 100%;
}
.material-file-icon {
  color: #409eff;
  flex-shrink: 0;
}
.material-file-name {
  color: #334155;
  word-break: break-all;
}
.material-file-size {
  color: #94a3b8;
  flex-shrink: 0;
}

/* 详情：文案内容与图片预览 */
.view-content-text {
  white-space: pre-wrap;
  word-break: break-all;
}
.view-image {
  width: 160px;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
}
/* 关联活动输入框清除图标 */
.clear-icon {
  color: #c0c4cc;
  cursor: pointer;
}
.clear-icon:hover {
  color: #909399;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
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
</style>
