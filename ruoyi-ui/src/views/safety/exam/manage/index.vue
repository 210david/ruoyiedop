<template>
  <div class="app-container safety-exam-manage-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>场次名称</label>
          <div class="control">
            <el-input v-model="queryParams.examName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>关联试卷</label>
          <div class="control is-select">
            <el-select v-model="queryParams.paperId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="p in paperOptions" :key="p.paperId" :label="p.paperName" :value="p.paperId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.examStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_exam_exam_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:exam:add']">新增场次</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:exam:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_exam_manage_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="examList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
          <el-table-column label="场次名称" prop="examName" key="examName" :min-width="colWidth('examName', 200)" resizable show-overflow-tooltip v-if="columns.examName.visible" />
          <el-table-column label="关联试卷" prop="paperName" key="paperName" :width="colWidth('paperName', 170)" resizable show-overflow-tooltip v-if="columns.paperName.visible" />
          <el-table-column label="考试窗口" key="window" :min-width="colWidth('window', 280)" resizable v-if="columns.window.visible">
            <template #default="scope">
              <span class="window-text">{{ fmtTime(scope.row.startTime) }} ~ {{ fmtTime(scope.row.endTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="examStatus" key="examStatus" :width="colWidth('examStatus', 100)" resizable align="center" v-if="columns.examStatus.visible">
            <template #default="scope"><span class="badge" :class="statusBadgeClass(scope.row.examStatus)"><span class="dot"></span>{{ statusLabel(scope.row.examStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="应考" prop="totalCount" key="totalCount" :width="colWidth('totalCount', 80)" resizable align="center" v-if="columns.totalCount.visible">
            <template #default="scope">{{ scope.row.totalCount ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="已交卷" prop="submittedCount" key="submittedCount" :width="colWidth('submittedCount', 90)" resizable align="center" v-if="columns.submittedCount.visible">
            <template #default="scope">
              <span v-if="scope.row.totalCount > 0" class="badge blue"><span class="dot"></span>{{ scope.row.submittedCount ?? 0 }}/{{ scope.row.totalCount }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="缺考" prop="absentCount" key="absentCount" :width="colWidth('absentCount', 80)" resizable align="center" v-if="columns.absentCount.visible">
            <template #default="scope">{{ scope.row.totalCount > 0 ? (scope.row.absentCount ?? 0) : '-' }}</template>
          </el-table-column>
          <el-table-column label="及格率" prop="passRate" key="passRate" :width="colWidth('passRate', 90)" resizable align="center" v-if="columns.passRate.visible">
            <template #default="scope">{{ scope.row.totalCount > 0 && scope.row.passRate != null ? scope.row.passRate + '%' : '-' }}</template>
          </el-table-column>
          <el-table-column label="平均分" prop="avgScore" key="avgScore" :width="colWidth('avgScore', 90)" resizable align="center" v-if="columns.avgScore.visible">
            <template #default="scope">{{ scope.row.totalCount > 0 && scope.row.avgScore != null ? scope.row.avgScore : '-' }}</template>
          </el-table-column>
          <el-table-column label="发布人" prop="publishBy" key="publishBy" :width="colWidth('publishBy', 110)" resizable align="center" v-if="columns.publishBy.visible">
            <template #default="scope">{{ scope.row.publishBy || '-' }}</template>
          </el-table-column>
          <el-table-column label="发布时间" prop="publishTime" key="publishTime" :width="colWidth('publishTime', 160)" resizable align="center" v-if="columns.publishTime.visible">
            <template #default="scope">{{ scope.row.publishTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 300)" align="center" fixed="right" class-name="col-action" resizable>
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Tickets" @click="handleDetail(scope.row)" v-hasPermi="['safety:exam:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.examStatus === '0'" v-hasPermi="['safety:exam:edit']">修改</el-button>
                <el-button link type="success" icon="Promotion" @click="handlePublish(scope.row)" v-if="scope.row.examStatus === '0'" v-hasPermi="['safety:exam:publish']">发布</el-button>
                <el-button link type="warning" icon="RefreshLeft" @click="handleCancel(scope.row)" v-if="scope.row.examStatus === '1'" v-hasPermi="['safety:exam:publish']">撤回</el-button>
                <el-button link type="danger" icon="CircleClose" @click="handleForceEnd(scope.row)" v-if="scope.row.examStatus === '1' || scope.row.examStatus === '2'" v-hasPermi="['safety:exam:forceEnd']">强制结束</el-button>
                <el-button link type="primary" icon="User" @click="handleAddUsers(scope.row)" v-if="scope.row.examStatus === '1' || scope.row.examStatus === '2'" v-hasPermi="['safety:exam:publish']">增补考生</el-button>
                <el-button link type="primary" icon="DataAnalysis" @click="handleRecords(scope.row)" v-if="scope.row.examStatus && scope.row.examStatus !== '0'" v-hasPermi="['safety:exam:stats']">考试情况</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.examStatus === '0' || scope.row.examStatus === '4'" v-hasPermi="['safety:exam:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/编辑弹窗 ===== -->
    <el-dialog v-model="open" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="examRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="场次名称" prop="examName"><el-input v-model="form.examName" placeholder="如：2026年三季度安全知识考试" maxlength="100" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="关联试卷" prop="paperId">
                  <el-input :model-value="selectedPaperText" readonly placeholder="点击选择已发布试卷" style="width: 100%" @click="openPaperPicker">
                    <template #append><el-button icon="Search" @click="openPaperPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="考生来源">
                  <el-radio-group v-model="form.attendeeSource" @change="onSourceChange">
                    <el-radio value="1">培训参训人</el-radio>
                    <el-radio value="2">手动添加</el-radio>
                  </el-radio-group>
                </el-form-item></el-col>
                <el-col :span="12" v-if="form.attendeeSource === '1'"><el-form-item label="关联培训" prop="recordId">
                  <el-select v-model="form.recordId" placeholder="考试名单取自参训人" style="width: 100%" filterable>
                    <el-option v-for="r in recordOptions" :key="r.recordId" :label="r.courseName ? `${r.courseName}（${(r.trainingDate || '').substring(0, 10)}）` : ('培训记录 #' + r.recordId)" :value="r.recordId" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
              <el-form-item v-if="form.attendeeSource === '2'" label="考试人员" prop="userIds">
                <el-input :model-value="manualUserNamesText" readonly placeholder="点击选择考试人员" style="width: 100%" @click="openExamUserPicker">
                  <template #append><el-button icon="Search" @click="openExamUserPicker" /></template>
                </el-input>
                <div class="exam-user-tags" v-if="manualSelectedUsers.length">
                  <el-tag v-for="u in manualSelectedUsers" :key="u.userId" closable @close="removeExamUser(u.userId)">{{ u.nickName || u.userName }}</el-tag>
                </div>
                <div style="font-size: 12px; color: var(--el-text-color-secondary); line-height: 1.6; margin-top: 2px;">已选 {{ (form.userIds || []).length }} 人；考生需有系统账号方可登录答题</div>
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>考试窗口</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="考试窗口" prop="timeRange">
                <el-date-picker v-model="form.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
              </el-form-item>
              <el-alert type="info" :closable="false" show-icon title="发布后按考生来源生成名单：来自培训取参训人（有系统账号者），手动添加取勾选人员；发布前可随时修改，发布后仅可撤回或强制结束。" />
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 考试情况（名单/成绩）弹窗 ===== -->
    <el-dialog v-model="recordsOpen" width="920px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">考试情况</span>
          <div class="rd-detail-header-sub" v-if="recordsExam.examName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ recordsExam.examName }}</span></div>
        </div>
      </template>
      <template v-if="recordsExam.examStatus === '0'">
        <el-alert v-if="recordsExam.attendeeSource === '1'" type="info" :closable="false" show-icon style="margin-bottom: 4px;" title="场次尚未发布，发布后将按关联培训的参训人（有系统账号者）生成考生名单。" />
        <el-alert v-else type="info" :closable="false" show-icon style="margin-bottom: 8px;" title="场次尚未发布，以下为手动添加的考试人员预览；发布后正式生成考生名单并开放答题。" />
        <el-table v-if="recordsExam.attendeeSource === '2'" v-loading="draftPreviewLoading" :data="pagedDraftUsers" border size="small">
          <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (manualPage - 1) * manualPageSize + i + 1" />
          <el-table-column label="考生" prop="userName" min-width="110" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.userName || ('用户 #' + scope.row.userId) }}</template>
          </el-table-column>
          <el-table-column label="岗位" prop="postName" min-width="130" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.postName || '-' }}</template>
          </el-table-column>
          <el-table-column label="部门" prop="deptName" min-width="140" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.deptName || '-' }}</template>
          </el-table-column>
        </el-table>
        <pagination v-show="draftPreviewUsers.length > 0" :total="draftPreviewUsers.length" v-model:page="manualPage" v-model:limit="manualPageSize" :pager-count="5" layout="total, prev, pager, next" @pagination="() => {}" style="margin-top: 8px;" />
      </template>
      <template v-else>
      <div class="records-summary">
        <span class="badge gray"><span class="dot"></span>应考 {{ stats.total }}</span>
        <span class="badge green"><span class="dot"></span>已交卷 {{ stats.submitted }}</span>
        <span class="badge red"><span class="dot"></span>缺考 {{ stats.absent }}</span>
        <span class="badge violet"><span class="dot"></span>及格率 {{ stats.passRate }}%</span>
        <span class="badge blue"><span class="dot"></span>平均分 {{ stats.avgScore }}</span>
        <span class="badge gray" v-if="stats.maxScore != null"><span class="dot"></span>最高 {{ stats.maxScore }} / 最低 {{ stats.minScore }}</span>
        <el-button v-if="stats.submitted > 0" type="primary" plain size="small" icon="DataAnalysis" style="margin-left: auto" @click="handleAnalysis" v-hasPermi="['safety:exam:stats']">试题分析</el-button>
        <el-button v-if="stats.submitted > 0" type="info" plain size="small" icon="Document" @click="handleArchiveExport" v-hasPermi="['safety:exam:export']">答卷存档</el-button>
        <el-button v-if="recordsExam.examStatus === '3'" type="danger" plain size="small" icon="RefreshRight" @click="handleRetake" v-hasPermi="['safety:exam:publish']">发起补考</el-button>
        <el-button v-if="stats.submitted > 0" type="warning" plain size="small" icon="Download" @click="handleExportRecords()" v-hasPermi="['safety:exam:export']">导出成绩</el-button>
      </div>
      <el-table v-loading="recordsLoading" :data="recordList" border size="small" max-height="420">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="考生" prop="userName" min-width="110" show-overflow-tooltip />
        <el-table-column label="状态" prop="examResult" width="100" align="center">
          <template #default="scope"><span class="badge" :class="resultBadgeClass(scope.row.examResult)"><span class="dot"></span>{{ resultLabel(scope.row.examResult) }}</span></template>
        </el-table-column>
        <el-table-column label="成绩" prop="totalScore" width="100" align="center">
          <template #default="scope">{{ scope.row.totalScore != null ? scope.row.totalScore + ' 分' : '-' }}</template>
        </el-table-column>
        <el-table-column label="是否及格" prop="isPass" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.isPass" class="badge" :class="scope.row.isPass === '1' ? 'green' : 'red'"><span class="dot"></span>{{ scope.row.isPass === '1' ? '及格' : '不及格' }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="切屏次数" prop="switchCount" width="90" align="center">
          <template #default="scope">{{ scope.row.switchCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="交卷时间" prop="submitTime" width="160" align="center">
          <template #default="scope">{{ scope.row.submitTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="用时" prop="usedSeconds" width="110" align="center">
          <template #default="scope">{{ fmtUsed(scope.row) }}</template>
        </el-table-column>
      </el-table>
      <pagination v-show="recordTotal > 0" :total="recordTotal" v-model:page="recordQuery.pageNum" v-model:limit="recordQuery.pageSize" :pager-count="5" layout="prev, pager, next" @pagination="loadRecords" />
      </template>
      <template #footer><el-button @click="recordsOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 试题分析弹窗（FR-7 错题率 + 复训卷生成） ===== -->
    <el-dialog v-model="analysisOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></div>
          <span class="rd-detail-header-title">试题分析</span>
          <div class="rd-detail-header-sub" v-if="recordsExam.examName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ recordsExam.examName }}</span></div>
        </div>
      </template>
      <div class="records-summary">
        <span class="badge gray"><span class="dot"></span>共 {{ analysisList.length }} 题</span>
        <span class="badge red" v-if="highErrorCount > 0"><span class="dot"></span>错误率 ≥ {{ analysisThreshold * 100 }}% 的题目 {{ highErrorCount }} 道</span>
        <div style="margin-left: auto; display: flex; align-items: center; gap: 8px">
          <span style="font-size: 12px; color: var(--el-text-color-secondary)">错误率阈值</span>
          <el-input-number v-model="analysisThreshold" :min="0.1" :max="1" :step="0.1" size="small" style="width: 110px" />
          <el-button type="primary" plain size="small" icon="MagicStick" :disabled="highErrorCount === 0" @click="handleCreateRemedial" v-hasPermi="['safety:exam:stats']">生成复训卷草稿</el-button>
        </div>
      </div>
      <el-table v-loading="analysisLoading" :data="filteredAnalysisList" border size="small" max-height="420">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="题干" prop="content" min-width="240" show-overflow-tooltip />
        <el-table-column label="题型" prop="questionType" width="90" align="center">
          <template #default="scope">{{ questionTypeLabel(scope.row.questionType) }}</template>
        </el-table-column>
        <el-table-column label="知识点分类" prop="categoryName" width="130" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.categoryName || '-' }}</template>
        </el-table-column>
        <el-table-column label="答题人数" prop="answerCount" width="90" align="center" />
        <el-table-column label="答对人数" prop="correctCount" width="90" align="center" />
        <el-table-column label="错误率" prop="errorRate" width="120" align="center" sortable>
          <template #default="scope">
            <span class="badge" :class="errorRateBadgeClass(scope.row.errorRate)"><span class="dot"></span>{{ (scope.row.errorRate * 100).toFixed(1) }}%</span>
          </template>
        </el-table-column>
      </el-table>
      <el-alert v-if="analysisList.length > 0" type="info" :closable="false" show-icon style="margin-top: 10px"
        title="复训卷以本试卷快照为蓝本复制达到阈值的错题，生成后保持草稿态，可在「试卷管理」中确认并发布；建议按高错误率知识点安排下一轮复训。" />
      <template #footer><el-button @click="analysisOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 发起补考弹窗（FR-8） ===== -->
    <el-dialog v-model="retakeOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9"/><path d="M3 3v6h6"/></svg></div>
          <span class="rd-detail-header-title">发起补考</span>
          <div class="rd-detail-header-sub" v-if="recordsExam.examName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ recordsExam.examName }}</span></div>
        </div>
      </template>
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 14px"
        title="补考名单自动取本场次「不合格 + 缺考」且有系统账号的考生；补考沿用本试卷，成绩按下方策略回写参训人。" />
      <el-form :model="retakeForm" label-width="100px">
        <el-form-item label="补考开始" required>
          <el-date-picker v-model="retakeForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="补考结束" required>
          <el-date-picker v-model="retakeForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成绩覆盖策略">
          <el-radio-group v-model="retakeForm.scoreStrategy">
            <el-radio value="2">取最新（默认）</el-radio>
            <el-radio value="1">取最高</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="retakeOpen = false">取 消</el-button>
        <el-button type="primary" :loading="retakeSubmitting" @click="submitRetake">确认发起补考</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">考试场次详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.examName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ viewData.examName }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">场次名称</span><div class="rd-value">{{ viewData.examName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联试卷</span><div class="rd-value">{{ viewData.paperName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">考生来源</span><div class="rd-value">{{ viewData.attendeeSource === '2' ? '手动添加' : '培训参训人' }}</div></div>
              <div class="rd-item" v-if="viewData.attendeeSource === '1'"><span class="rd-label">关联培训</span><div class="rd-value">{{ viewData.courseName || (viewData.recordId ? '培训记录 #' + viewData.recordId : '-') }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.attendeeSource === '2'">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/></svg></span>考试人员（{{ (viewData.manualUsers || []).length }} 人）</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <el-table v-if="(viewData.manualUsers || []).length" :data="pagedDetailUsers" size="small" border>
              <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (manualPage - 1) * manualPageSize + i + 1" />
              <el-table-column label="考生" prop="userName" min-width="110" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.userName || ('用户 #' + scope.row.userId) }}</template>
              </el-table-column>
              <el-table-column label="岗位" prop="postName" min-width="130" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.postName || '-' }}</template>
              </el-table-column>
              <el-table-column label="部门" prop="deptName" min-width="140" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.deptName || '-' }}</template>
              </el-table-column>
            </el-table>
            <pagination v-show="(viewData.manualUsers || []).length > 0" :total="(viewData.manualUsers || []).length" v-model:page="manualPage" v-model:limit="manualPageSize" :pager-count="5" layout="total, prev, pager, next" @pagination="() => {}" style="margin-top: 8px;" />
            <div class="tree-empty" v-if="!(viewData.manualUsers || []).length">暂无手动名单</div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>考试窗口与状态</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewData.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ viewData.endTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="statusBadgeClass(viewData.examStatus)"><span class="dot"></span>{{ statusLabel(viewData.examStatus) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">发布人</span><div class="rd-value">{{ viewData.publishBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发布时间</span><div class="rd-value">{{ viewData.publishTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 考试人员多选弹框（开发规范：picker-component-style-guide） -->
    <!-- 选择试卷弹框（关联试卷弹框选择，仅已发布试卷） -->
    <el-dialog v-model="paperPickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <span class="rd-detail-header-title">选择试卷</span>
          <div class="rd-detail-header-sub"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">仅已发布试卷可选</span></div>
        </div>
      </template>
      <div style="margin-bottom: 10px; display: flex; align-items: center;">
        <el-input v-model="paperQuery.paperName" placeholder="试卷名称" clearable style="width: 240px" @keyup.enter="handlePaperQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handlePaperQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetPaperQuery">重置</el-button>
      </div>
      <el-table v-loading="paperLoading" :data="paperList" border size="small" max-height="380" @row-click="onPaperRowClick" :row-class-name="paperRowClass" style="cursor: pointer;">
        <el-table-column width="55" align="center">
          <template #default="scope">
            <el-radio :model-value="pendingPaper ? pendingPaper.paperId : null" :value="scope.row.paperId" @click.stop="onPaperRowClick(scope.row)"><span></span></el-radio>
          </template>
        </el-table-column>
        <el-table-column label="试卷名称" prop="paperName" min-width="220" show-overflow-tooltip />
        <el-table-column label="关联课程" prop="courseName" min-width="140" show-overflow-tooltip />
        <el-table-column label="总分" prop="totalScore" width="90" align="center" />
        <el-table-column label="时长(分钟)" prop="duration" width="100" align="center" />
        <el-table-column label="发布人" prop="publishBy" width="110" align="center" />
        <el-table-column label="发布时间" prop="publishTime" width="160" align="center" />
      </el-table>
      <pagination v-show="paperTotal > 0" :total="paperTotal" v-model:page="paperQuery.pageNum" v-model:limit="paperQuery.pageSize" :pager-count="5" layout="total, prev, pager, next" @pagination="loadPaperList" style="margin-top: 8px;" />
      <template #footer>
        <el-button @click="paperPickerOpen = false">取 消</el-button>
        <el-button type="primary" @click="confirmPaper" :disabled="!pendingPaper">确 定</el-button>
      </template>
    </el-dialog>
    <user-picker ref="userPickerRef" title="选择考试人员" :multiple="true" @confirm="onExamUserConfirm" />

  </div>
</template>

<script setup name="SafetyExamManage">
import { listExam, getExam, listExamRecords, getExamStats, addExam, updateExam, publishExam, cancelExam, forceEndExam, delExam, getQuestionStats, createRemedialPaper, createRetakeExam, addExamUsers } from '@/api/safety/examManage'
import { listExamPaper } from '@/api/safety/examPaper'
import { listTrainingRecord } from '@/api/safety/trainingRecord'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import UserPicker from '@/components/UserPicker/index.vue'
import { Search, Filter, RefreshLeft } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_exam_exam_status, safety_exam_result } = proxy.useDict('safety_exam_exam_status', 'safety_exam_result')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_exam_manage_index')
// c0/c1 = 编辑弹窗卡片，vc0~vc3 = 详情弹窗卡片
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","vc0","vc1","vc2","vc3"])

const viewOpen = ref(false)
const viewData = ref({})

/* ===== 考生来源（手动添加选人，UserPicker 弹框多选） ===== */
const userPickerRef = ref(null)
const manualSelectedUsers = ref([])
const manualUserNamesText = computed(() => manualSelectedUsers.value.map(u => u.nickName || u.userName).join('、'))
function openExamUserPicker() {
  userPickerRef.value.open((form.value.userIds || []).slice())
}
function onExamUserConfirm(users) {
  // 增补考生模式：选人结果走增补接口，不回写编辑弹窗
  if (addUsersExam.value) {
    const row = addUsersExam.value
    addUsersExam.value = null
    const userIds = (users || []).map(u => u.userId)
    if (!userIds.length) { proxy.$modal.msgWarning('请勾选要增补的考试人员'); return }
    doAddUsers(row, { userIds: userIds })
    return
  }
  manualSelectedUsers.value = users || []
  form.value.userIds = (users || []).map(u => u.userId)
}
function removeExamUser(userId) {
  manualSelectedUsers.value = manualSelectedUsers.value.filter(u => u.userId !== userId)
  form.value.userIds = manualSelectedUsers.value.map(u => u.userId)
}

const examList = ref([])
const paperOptions = ref([])
const recordOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const recordsOpen = ref(false)
const recordsExam = ref({})
const recordList = ref([])
const recordTotal = ref(0)
const recordsLoading = ref(false)
const recordQuery = reactive({ pageNum: 1, pageSize: 10 })
const stats = reactive({ total: 0, submitted: 0, absent: 0, passRate: 0, avgScore: 0, maxScore: null, minScore: null })

const default_columns = {
  examName: { label: '场次名称', visible: true },
  paperName: { label: '关联试卷', visible: true },
  window: { label: '考试窗口', visible: true },
  totalCount: { label: '应考', visible: true },
  submittedCount: { label: '已交卷', visible: true },
  absentCount: { label: '缺考', visible: true },
  passRate: { label: '及格率', visible: true },
  avgScore: { label: '平均分', visible: true },
  examStatus: { label: '状态', visible: true },
  publishBy: { label: '发布人', visible: true },
  publishTime: { label: '发布时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_exam_manage_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(default_columns).forEach(key => {
        result[key] = { label: default_columns[key].label, visible: parsed[key] !== undefined ? parsed[key] : default_columns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...default_columns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, examName: undefined, paperId: undefined, examStatus: undefined, params: {} },
  rules: {
    examName: [{ required: true, message: '场次名称不能为空', trigger: 'blur' }],
    paperId: [{ required: true, message: '请选择关联试卷', trigger: 'change' }],
    timeRange: [{ required: true, message: '请选择考试窗口', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.examName) count++
  if (queryParams.value.paperId) count++
  if (queryParams.value.examStatus) count++
  return count
})

function getList() { loading.value = true; listExam(queryParams.value).then(response => { examList.value = response.rows; total.value = response.total; applySavedWidths() }).catch(error => { console.error(error) }).finally(() => { loading.value = false }) }
function loadPaperOptions() { listExamPaper({ pageNum: 1, pageSize: 500 }).then(res => { paperOptions.value = res.rows || [] }) }
/* ===== 关联试卷弹框选择（仅已发布试卷） ===== */
const paperPickerOpen = ref(false)
const paperLoading = ref(false)
const paperList = ref([])
const paperTotal = ref(0)
const paperQuery = reactive({ pageNum: 1, pageSize: 10, paperName: undefined })
const pendingPaper = ref(null)
const selectedPaperText = computed(() => {
  if (!form.value.paperId) return ''
  return form.value.totalScore != null ? `${form.value.paperName}（${form.value.totalScore}分）` : (form.value.paperName || `试卷 #${form.value.paperId}`)
})
function openPaperPicker() {
  pendingPaper.value = null
  paperQuery.pageNum = 1
  paperQuery.paperName = undefined
  loadPaperList()
  paperPickerOpen.value = true
}
function loadPaperList() {
  paperLoading.value = true
  listExamPaper({ pageNum: paperQuery.pageNum, pageSize: paperQuery.pageSize, status: '1', paperName: paperQuery.paperName || undefined }).then(res => {
    paperList.value = res.rows || []
    paperTotal.value = res.total || 0
  }).finally(() => { paperLoading.value = false })
}
function handlePaperQuery() { paperQuery.pageNum = 1; loadPaperList() }
function resetPaperQuery() { paperQuery.paperName = undefined; handlePaperQuery() }
function onPaperRowClick(row) { pendingPaper.value = row }
function paperRowClass({ row }) { return pendingPaper.value && pendingPaper.value.paperId === row.paperId ? 'current-row' : '' }
function confirmPaper() {
  if (!pendingPaper.value) return
  form.value.paperId = pendingPaper.value.paperId
  form.value.paperName = pendingPaper.value.paperName
  form.value.totalScore = pendingPaper.value.totalScore
  paperPickerOpen.value = false
  proxy.$refs['examRef'].validateField('paperId')
}
function loadRecordOptions() { listTrainingRecord({ pageNum: 1, pageSize: 500 }).then(res => { recordOptions.value = res.rows || [] }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.examName = undefined; queryParams.value.paperId = undefined; queryParams.value.examStatus = undefined; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.examId); multiple.value = !selection.length }

function reset() {
  form.value = { examId: undefined, examName: undefined, paperId: undefined, paperName: undefined, totalScore: undefined, recordId: undefined, attendeeSource: '1', userIds: [], timeRange: [], startTime: undefined, endTime: undefined, remark: undefined }
  manualSelectedUsers.value = []
  proxy.resetForm('examRef')
}
function onSourceChange(source) {
  if (source === '1') { form.value.userIds = []; manualSelectedUsers.value = [] } else { form.value.recordId = undefined }
}
function handleAdd() { reset(); loadRecordOptions(); open.value = true; title.value = '新增考试场次' }
function handleUpdate(row) {
  reset(); loadRecordOptions()
  getExam(row.examId).then(response => {
    const detail = response.data || {}
    manualSelectedUsers.value = (detail.manualUsers || []).map(u => ({ userId: u.userId, nickName: u.userName, userName: u.userName }))
    form.value = { ...detail, timeRange: detail.startTime && detail.endTime ? [detail.startTime, detail.endTime] : [], userIds: manualSelectedUsers.value.map(u => u.userId) }
    open.value = true; title.value = '修改考试场次'
  })
}
function submitForm() {
  proxy.$refs['examRef'].validate(valid => {
    if (!valid) return
    if (!form.value.timeRange || form.value.timeRange.length !== 2) { proxy.$modal.msgWarning('请选择考试窗口'); return }
    // 考生来源条件必填
    if (form.value.attendeeSource === '1' && !form.value.recordId) { proxy.$modal.msgWarning('考生来源为培训参训人时，请选择关联培训'); return }
    if (form.value.attendeeSource === '2' && (!form.value.userIds || !form.value.userIds.length)) { proxy.$modal.msgWarning('考生来源为手动添加时，请勾选考试人员'); return }
    form.value.startTime = form.value.timeRange[0]
    form.value.endTime = form.value.timeRange[1]
    if (form.value.examId != undefined) {
      updateExam(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
    } else {
      addExam(form.value).then(() => { proxy.$modal.msgSuccess('新增成功，确认无误后请点击「发布」'); open.value = false; getList() })
    }
  })
}
function handlePublish(row) {
  proxy.$modal.confirm(`发布后将按关联培训参训人生成考生名单，确认发布「${row.examName}」？`).then(() => {
    return publishExam(row.examId)
  }).then(() => { getList(); proxy.$modal.msgSuccess('发布成功') }).catch(e => { if (e && e.message) proxy.$modal.msgError(e.message) })
}
function handleCancel(row) {
  proxy.$modal.confirm('撤回后考生名单作废，确认撤回？').then(() => {
    return cancelExam(row.examId)
  }).then(() => { getList(); proxy.$modal.msgSuccess('已撤回') }).catch(e => { if (e && e.message) proxy.$modal.msgError(e.message) })
}
function handleForceEnd(row) {
  proxy.$modal.confirm('强制结束后未交卷考生将标记缺考，确认执行？').then(() => {
    return forceEndExam(row.examId)
  }).then(() => { getList(); proxy.$modal.msgSuccess('已强制结束') }).catch(e => { if (e && e.message) proxy.$modal.msgError(e.message) })
}
/* ===== 发布后增补考生（只增不减：手动来源选人追加，培训来源按参训人重新同步追加） ===== */
const addUsersExam = ref(null)
function handleAddUsers(row) {
  if (row.attendeeSource === '2') {
    // 手动来源：复用选人弹框，确认结果走增补接口（见 onExamUserConfirm）
    addUsersExam.value = row
    userPickerRef.value.open([])
  } else {
    proxy.$modal.confirm('将按关联培训参训人重新同步，仅追加未在名单中的考生，确认增补？').then(() => {
      return doAddUsers(row, {})
    }).catch(() => {})
  }
}
function doAddUsers(row, data) {
  return addExamUsers(row.examId, data).then(res => {
    const added = res.data || 0
    proxy.$modal.msgSuccess(added > 0 ? `已增补 ${added} 名考生` : '所选人员均已在名单中，无需增补')
    getList()
    return added
  }).catch(e => { if (e && e.message) proxy.$modal.msgError(e.message) })
}
function handleDelete(row) { const examIds = row.examId || ids.value; proxy.$modal.confirm('是否确认删除所选场次？').then(function() { return delExam(examIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }

/* ===== 详情 ===== */
function handleDetail(row) {
  manualPage.value = 1
  getExam(row.examId).then(response => {
    viewData.value = { ...row, ...response.data }
    viewOpen.value = true
  })
}

/* ===== 考试情况 ===== */
const draftPreviewUsers = ref([])
const draftPreviewLoading = ref(false)
/* 手动名单客户端分页（详情弹窗与名单草稿预览共用，两弹窗不同时打开） */
const manualPage = ref(1)
const manualPageSize = ref(10)
const pagedDetailUsers = computed(() => {
  const list = viewData.value.manualUsers || []
  const start = (manualPage.value - 1) * manualPageSize.value
  return list.slice(start, start + manualPageSize.value)
})
const pagedDraftUsers = computed(() => {
  const start = (manualPage.value - 1) * manualPageSize.value
  return draftPreviewUsers.value.slice(start, start + manualPageSize.value)
})
function handleRecords(row) {
  recordsExam.value = row
  recordQuery.pageNum = 1
  manualPage.value = 1
  draftPreviewUsers.value = []
  recordsOpen.value = true
  if (row.examStatus === '0') {
    // 草稿态：正式名单未生成，手动来源显示名单预览
    if (row.attendeeSource === '2') {
      draftPreviewLoading.value = true
      getExam(row.examId).then(res => { draftPreviewUsers.value = (res.data && res.data.manualUsers) || [] }).finally(() => { draftPreviewLoading.value = false })
    }
    return
  }
  loadRecords()
}
function loadRecords() {
  recordsLoading.value = true
  listExamRecords(recordsExam.value.examId, recordQuery).then(res => {
    recordList.value = res.rows || []
    recordTotal.value = res.total || 0
  }).finally(() => { recordsLoading.value = false })
  getExamStats(recordsExam.value.examId).then(res => {
    Object.assign(stats, res.data || {})
  })
}
function handleExportRecords(row) {
  // 列表行调用传 row；弹窗内调用不传参，取当前场次；防御 el-button 传入事件对象
  const exam = (row && row.examId) ? row : recordsExam.value
  proxy.download(`safety/exam/${exam.examId}/records/export`, {}, `考试成绩_${exam.examName || exam.examId}_${new Date().getTime()}.xlsx`)
}

/* ===== FR-12 答卷存档导出 ===== */
function handleArchiveExport() {
  proxy.download(`safety/exam/${recordsExam.value.examId}/archiveExport`, {}, `答卷存档_${recordsExam.value.examName || recordsExam.value.examId}_${new Date().getTime()}.xlsx`)
}

/* ===== FR-7 试题分析 + 复训卷 ===== */
const analysisOpen = ref(false)
const analysisLoading = ref(false)
const analysisList = ref([])
const analysisThreshold = ref(0.5)
const highErrorCount = computed(() => analysisList.value.filter(q => q.answerCount > 0 && q.errorRate != null && q.errorRate >= analysisThreshold.value).length)
const filteredAnalysisList = computed(() => analysisList.value)

function handleAnalysis() {
  analysisOpen.value = true
  analysisLoading.value = true
  analysisList.value = []
  getQuestionStats(recordsExam.value.examId).then(res => {
    analysisList.value = res.data || []
  }).finally(() => { analysisLoading.value = false })
}

function handleCreateRemedial() {
  proxy.$modal.confirm(`将按错误率 ≥ ${analysisThreshold.value * 100}% 的 ${highErrorCount.value} 道错题生成复训试卷草稿，确认生成？`).then(() => {
    createRemedialPaper(recordsExam.value.examId, analysisThreshold.value).then(res => {
      proxy.$modal.msgSuccess('复训试卷草稿已生成，可在「试卷管理」中确认并发布')
      analysisOpen.value = false
    })
  }).catch(() => {})
}

/* ===== FR-8 发起补考 ===== */
const retakeOpen = ref(false)
const retakeSubmitting = ref(false)
const retakeForm = reactive({ startTime: '', endTime: '', scoreStrategy: '2' })

function handleRetake() {
  retakeForm.startTime = ''
  retakeForm.endTime = ''
  retakeForm.scoreStrategy = '2'
  retakeOpen.value = true
}

function submitRetake() {
  if (!retakeForm.startTime || !retakeForm.endTime) {
    proxy.$modal.msgWarning('请设置补考起止时间')
    return
  }
  if (retakeForm.endTime <= retakeForm.startTime) {
    proxy.$modal.msgWarning('补考结束时间必须晚于开始时间')
    return
  }
  retakeSubmitting.value = true
  createRetakeExam(recordsExam.value.examId, retakeForm).then(res => {
    proxy.$modal.msgSuccess('补考场次已创建并发布，可在列表中查看')
    retakeOpen.value = false
    recordsOpen.value = false
    getList()
  }).finally(() => { retakeSubmitting.value = false })
}

function questionTypeLabel(t) { return { '1': '单选题', '2': '多选题', '3': '判断题' }[t] || '-' }
function errorRateBadgeClass(rate) {
  if (rate == null) return 'gray'
  if (rate >= 0.5) return 'red'
  if (rate >= 0.3) return 'violet'
  return 'green'
}

/* ===== 展示辅助 ===== */
function fmtTime(t) { return t ? String(t).substring(5, 16) : '-' }
function statusLabel(s) { const d = safety_exam_exam_status.value.find(x => x.value == s); return d ? d.label : '-' }
function statusBadgeClass(s) { return { '0': 'gray', '1': 'blue', '2': 'violet', '3': 'green', '4': 'red' }[s] || 'gray' }
function resultLabel(r) { const d = safety_exam_result.value.find(x => x.value == r); return d ? d.label : '-' }
function resultBadgeClass(r) { return { '0': 'gray', '1': 'blue', '2': 'green', '3': 'red', '4': 'green' }[r] || 'gray' }
function fmtDuration(seconds) {
  const m = Math.floor(seconds / 60), s = seconds % 60
  return m > 0 ? `${m}分${s}秒` : `${s}秒`
}
/** 用时显示：已交卷显示实际用时；进行中显示已进行时长（按开始答题时间起算） */
function fmtUsed(row) {
  if (row.usedSeconds != null) return fmtDuration(row.usedSeconds)
  if (row.examResult === '1' && row.startTime) {
    const start = new Date(String(row.startTime).replace(' ', 'T')).getTime()
    if (!isNaN(start)) {
      const mins = Math.max(0, Math.floor((Date.now() - start) / 60000))
      return `已进行 ${mins} 分钟`
    }
  }
  return '-'
}

loadPaperOptions()
loadRecordOptions()
getList()
</script>

<style scoped>
.safety-exam-manage-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-exam-manage-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-exam-manage-page .filter-card { padding:14px 20px 16px; }
.safety-exam-manage-page .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-exam-manage-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-exam-manage-page .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-exam-manage-page .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-exam-manage-page .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-exam-manage-page .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-exam-manage-page .filter-buttons { display:flex; gap:8px; }
.safety-exam-manage-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-exam-manage-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-exam-manage-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-exam-manage-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-exam-manage-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-exam-manage-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-exam-manage-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-exam-manage-page .field .control :deep(.el-select) { width:100%; }
.safety-exam-manage-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-exam-manage-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-exam-manage-page .toolbar .left, .safety-exam-manage-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-exam-manage-page .table-wrap { overflow-x:auto; }
.safety-exam-manage-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-exam-manage-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-exam-manage-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-exam-manage-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-exam-manage-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-exam-manage-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-exam-manage-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-exam-manage-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-exam-manage-page .badge.green .dot { background:var(--green-500); }
.safety-exam-manage-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-exam-manage-page .badge.red .dot { background:var(--red-500); }
.safety-exam-manage-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); } .safety-exam-manage-page .badge.violet .dot { background:var(--brand-500); }
.safety-exam-manage-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-exam-manage-page .badge.blue .dot { background:var(--blue-500); }
.safety-exam-manage-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-exam-manage-page .badge.gray .dot { background:var(--ink-400); }
.safety-exam-manage-page .window-text { font-size:13px; color:var(--ink-500); }
.safety-exam-manage-page .records-summary { display:flex; gap:8px; margin-bottom:10px; }

/* 操作列按钮对齐 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* 考试人员已选标签 */
.exam-user-tags { display:flex; flex-wrap:wrap; gap:6px; margin-top:6px; width:100%; }
.exam-user-tags .el-tag { max-width:160px; overflow:hidden; text-overflow:ellipsis; }

@media (max-width:1100px) { .safety-exam-manage-page .filter-bar { grid-template-columns:repeat(2,1fr); } }
</style>
