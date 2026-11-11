<template>
  <div class="app-container safety-exam-paper-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>试卷名称</label>
          <div class="control">
            <el-input v-model="queryParams.paperName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>关联课程</label>
          <div class="control is-select">
            <el-select v-model="queryParams.courseId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="c in courseOptions" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="草稿" value="0" />
              <el-option label="已发布" value="1" />
              <el-option label="停用" value="2" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>创建时间</label>
          <div class="control is-select">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:exam:paper:add']">新增试卷</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:exam:paper:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_exam_paper_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="paperList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
          <el-table-column label="试卷名称" prop="paperName" key="paperName" :min-width="colWidth('paperName', 220)" resizable show-overflow-tooltip v-if="columns.paperName.visible" />
          <el-table-column label="关联课程" prop="courseName" key="courseName" :width="colWidth('courseName', 160)" resizable show-overflow-tooltip v-if="columns.courseName.visible">
            <template #default="scope">{{ scope.row.courseName || '-' }}</template>
          </el-table-column>
          <el-table-column label="总分" prop="totalScore" key="totalScore" :width="colWidth('totalScore', 90)" resizable align="center" v-if="columns.totalScore.visible">
            <template #default="scope">{{ scope.row.totalScore != null ? scope.row.totalScore + ' 分' : '-' }}</template>
          </el-table-column>
          <el-table-column label="及格分" prop="passScore" key="passScore" :width="colWidth('passScore', 90)" resizable align="center" v-if="columns.passScore.visible">
            <template #default="scope">{{ scope.row.passScore != null ? scope.row.passScore + ' 分' : '-' }}</template>
          </el-table-column>
          <el-table-column label="时长" prop="duration" key="duration" :width="colWidth('duration', 90)" resizable align="center" v-if="columns.duration.visible">
            <template #default="scope">{{ scope.row.duration }} 分钟</template>
          </el-table-column>
          <el-table-column label="防作弊" key="antiCheat" :width="colWidth('antiCheat', 150)" resizable align="center" v-if="columns.antiCheat.visible">
            <template #default="scope">
              <span class="cheat-tags">
                <span class="badge blue" v-if="scope.row.shuffleQuestion === 'Y'">题乱序</span>
                <span class="badge blue" v-if="scope.row.shuffleOption === 'Y'">项乱序</span>
                <span class="badge violet" v-if="scope.row.switchLimit > 0">切屏≤{{ scope.row.switchLimit }}</span>
                <span class="text-muted" v-if="scope.row.shuffleQuestion !== 'Y' && scope.row.shuffleOption !== 'Y' && !(scope.row.switchLimit > 0)">-</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 96)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="发布时间" prop="publishTime" key="publishTime" :width="colWidth('publishTime', 160)" resizable align="center" v-if="columns.publishTime.visible">
            <template #default="scope">{{ scope.row.publishTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 220)" align="center" fixed="right" class-name="col-action" resizable>
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:exam:paper:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.status !== '1'" v-hasPermi="['safety:exam:paper:edit']">修改</el-button>
                <el-button link type="primary" icon="List" @click="handleCompose(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['safety:exam:paper:edit']">组卷</el-button>
                <el-button link type="success" icon="Promotion" @click="handlePublish(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['safety:exam:paper:publish']">发布</el-button>
                <el-button link type="warning" icon="RefreshLeft" @click="handleUnpublish(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['safety:exam:paper:publish']">撤回</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status !== '1'" v-hasPermi="['safety:exam:paper:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/编辑基础信息弹窗 ===== -->
    <el-dialog v-model="open" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="paperRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="试卷名称" prop="paperName"><el-input v-model="form.paperName" placeholder="请输入试卷名称" maxlength="100" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="关联课程" prop="courseId">
                  <el-select v-model="form.courseId" placeholder="可空，关联后可带入培训" style="width: 100%" clearable filterable @change="onFormCourseChange">
                    <el-option v-for="c in courseOptions" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>考试与评析</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="考试时长" prop="duration">
                  <div style="display: flex; align-items: center; width: 100%;"><el-input-number v-model="form.duration" :min="1" :max="600" style="flex: 1" /><span style="margin-left: 8px; font-size: 14px; color: #94a3b8; white-space: nowrap;">分钟</span></div>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="及格分" prop="passScore">
                  <el-input-number v-model="form.passScore" :precision="1" :min="0.5" :max="1000" style="width: 100%" />
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="交卷后解析">
                  <el-switch v-model="form.showAnalysis" active-value="Y" inactive-value="N" />
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></span>防作弊设置</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="题目乱序">
                  <el-switch v-model="form.shuffleQuestion" active-value="Y" inactive-value="N" />
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="选项乱序">
                  <el-switch v-model="form.shuffleOption" active-value="Y" inactive-value="N" />
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="切屏限制">
                  <div style="display: flex; align-items: center; width: 100%;"><el-input-number v-model="form.switchLimit" :min="0" :max="20" style="flex: 1" /><span style="margin-left: 8px; font-size: 14px; color: #94a3b8; white-space: nowrap;">次（0 不限）</span></div>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
              <el-alert type="info" :closable="false" show-icon title="保存基础信息后，点击列表「组卷」选题；保存题目时按题目分值自动计算总分。" />
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 组卷弹窗 ===== -->
    <el-dialog v-model="composeOpen" width="1100px" append-to-body draggable class="rd-dialog compose-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/></svg></div>
          <span class="rd-detail-header-title">组卷 - {{ composePaper.paperName }}</span>
          <div class="rd-detail-header-sub"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">已选 {{ selectedQuestions.length }} 题，合计 {{ composeTotalScore }} 分</span></div>
        </div>
      </template>

      <div class="compose-body">
        <!-- 上部：题库筛选 + 随机抽题 -->
        <div class="compose-filter">
          <el-select v-model="pickQuery.categoryId" placeholder="题目分类" clearable filterable style="width: 170px">
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
          </el-select>
          <el-select v-model="pickQuery.questionType" placeholder="题型" clearable style="width: 120px">
            <el-option v-for="dict in safety_exam_question_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
          <el-select v-model="pickQuery.difficulty" placeholder="难度" clearable style="width: 110px">
            <el-option v-for="dict in safety_exam_difficulty" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
          <el-input v-model="pickQuery.content" placeholder="题干关键字" clearable style="width: 180px" @keyup.enter="loadPickList" />
          <el-button type="primary" plain icon="Search" @click="loadPickList">查询题库</el-button>
          <div class="compose-filter-right">
            <span class="random-label">随机抽题</span>
            <el-input-number v-model="randomCount" :min="1" :max="100" size="small" style="width: 100px" />
            <el-button type="success" plain icon="MagicStick" @click="handleRandomPick">随机抽入</el-button>
          </div>
        </div>

        <!-- 中部：题库候选表 -->
        <el-table :data="pickList" border size="small" max-height="260" @selection-change="pickSelectionChange" v-loading="pickLoading" class="pick-table">
          <el-table-column type="selection" width="45" align="center" />
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column label="题型" width="90" align="center">
            <template #default="scope"><span class="badge" :class="typeBadgeClass(scope.row.questionType)">{{ typeLabel(scope.row.questionType) }}</span></template>
          </el-table-column>
          <el-table-column label="题干" min-width="280" show-overflow-tooltip prop="content" />
          <el-table-column label="难度" width="80" align="center">
            <template #default="scope"><span class="badge" :class="diffBadgeClass(scope.row.difficulty)">{{ diffLabel(scope.row.difficulty) }}</span></template>
          </el-table-column>
        </el-table>
        <div class="pick-actions">
          <pagination v-show="pickTotal > 0" :total="pickTotal" v-model:page="pickQuery.pageNum" v-model:limit="pickQuery.pageSize" :pager-count="5" layout="prev, pager, next" @pagination="loadPickList" />
          <el-button type="primary" icon="Plus" :disabled="!pickSelection.length" @click="addPickedQuestions">添加所选（{{ pickSelection.length }}）</el-button>
        </div>

        <!-- 下部：已选题目 -->
        <div class="selected-head">
          <span class="selected-title">已选题目</span>
          <span class="selected-tip">默认分值可修改；总分将按分值合计自动计算</span>
          <el-button link type="danger" icon="Delete" @click="clearSelected" :disabled="!selectedQuestions.length">清空</el-button>
        </div>
        <!-- 批量设分工具条 -->
        <div class="batch-score-bar" v-if="selectedQuestions.length">
          <div class="bs-head">
            <div class="bs-label"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>批量设分</div>
            <div class="bs-target">
              <span class="bs-target-label">目标总分</span>
              <el-input-number v-model="targetScore" :precision="1" :min="1" :max="1000" size="small" style="width: 110px" />
              <el-button size="small" type="success" plain icon="MagicStick" @click="autoDistribute">一键均分凑满</el-button>
              <span class="bs-diff" :class="scoreDiff === 0 ? 'ok' : 'lack'" v-if="diffText">{{ diffText }}</span>
            </div>
          </div>
          <div class="bs-types">
            <div class="bs-type" v-for="g in typeGroups" :key="g.type">
              <span class="badge" :class="typeBadgeClass(g.type)">{{ g.label }}</span>
              <span class="bs-count">{{ g.count }} 题</span>
              <span class="bs-sep">×</span>
              <span class="bs-unit">每题</span>
              <el-input-number v-model="typeScoreInputs[g.type]" :precision="1" :min="0.5" :max="100" size="small" style="width: 84px" placeholder="分值" controls-position="right" />
              <el-button size="small" type="primary" plain @click="applyTypeScore(g.type)">应用</el-button>
            </div>
          </div>
        </div>
        <el-table :data="selectedQuestions" border size="small" max-height="300" class="selected-table">
          <el-table-column type="index" label="序" width="55" align="center" />
          <el-table-column label="题型" width="90" align="center">
            <template #default="scope"><span class="badge" :class="typeBadgeClass(scope.row.questionType)">{{ typeLabel(scope.row.questionType) }}</span></template>
          </el-table-column>
          <el-table-column label="题干" min-width="280" show-overflow-tooltip prop="content" />
          <el-table-column label="分值" width="130" align="center">
            <template #default="scope">
              <el-input-number v-model="scope.row.score" :precision="1" :min="0.5" :max="100" size="small" style="width: 100px" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="scope">
              <el-button link type="danger" icon="Delete" @click="removeSelected(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer><div class="dialog-footer"><el-button type="primary" :loading="composeSaving" @click="submitCompose">保存组卷</el-button><el-button @click="composeOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">试卷详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.paperName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ viewData.paperName }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">试卷名称</span><div class="rd-value">{{ viewData.paperName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联课程</span><div class="rd-value">{{ viewData.courseName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="statusBadgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">发布时间</span><div class="rd-value">{{ viewData.publishTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>考试与评析</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">总分 / 及格分</span><div class="rd-value">{{ viewData.totalScore != null ? viewData.totalScore + ' 分' : '-' }} / {{ viewData.passScore != null ? viewData.passScore + ' 分' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">考试时长</span><div class="rd-value">{{ viewData.duration }} 分钟</div></div>
              <div class="rd-item"><span class="rd-label">交卷后解析</span><div class="rd-value">{{ viewData.showAnalysis === 'Y' ? '展示' : '不展示' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></span>防作弊设置</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">题目乱序</span><div class="rd-value">{{ viewData.shuffleQuestion === 'Y' ? '开启' : '关闭' }}</div></div>
              <div class="rd-item"><span class="rd-label">选项乱序</span><div class="rd-value">{{ viewData.shuffleOption === 'Y' ? '开启' : '关闭' }}</div></div>
              <div class="rd-item"><span class="rd-label">切屏限制</span><div class="rd-value">{{ viewData.switchLimit > 0 ? viewData.switchLimit + ' 次' : '不限' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/></svg></span>题目明细（{{ viewQuestions.length }} 题）</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block">
            <div class="view-question-item" v-for="(q, idx) in viewQuestions" :key="q.id">
              <div class="vq-head">
                <span class="vq-no">{{ idx + 1 }}</span>
                <span class="badge" :class="typeBadgeClass(q.questionType)">{{ typeLabel(q.questionType) }}</span>
                <span class="vq-content">{{ q.content }}</span>
                <span class="vq-score">{{ q.score }} 分</span>
              </div>
            </div>
            <div class="tree-empty" v-if="!viewQuestions.length">试卷暂无题目</div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

  </div>
</template>

<script setup name="SafetyExamPaper">
import { listExamPaper, getExamPaper, addExamPaper, updateExamPaper, savePaperQuestions, randomPickExamQuestion, publishExamPaper, unpublishExamPaper, delExamPaper } from '@/api/safety/examPaper'
import { listExamQuestion } from '@/api/safety/examQuestion'
import { examCategoryOptions } from '@/api/safety/examCategory'
import { listTrainingCourse } from '@/api/safety/trainingCourse'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_exam_question_type, safety_exam_difficulty } = proxy.useDict('safety_exam_question_type', 'safety_exam_difficulty')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_exam_paper_index')
// c0~c3 = 编辑弹窗卡片，vc0~vc4 = 查看弹窗卡片
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3","vc4"])

const paperList = ref([])
const courseOptions = ref([])
const categoryOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref({})
const dateRange = ref([])

/* ===== 组卷状态 ===== */
const composeOpen = ref(false)
const composePaper = ref({})
const composeSaving = ref(false)
const pickQuery = reactive({ pageNum: 1, pageSize: 10, categoryId: undefined, questionType: undefined, difficulty: undefined, content: undefined })
const pickList = ref([])
const pickTotal = ref(0)
const pickLoading = ref(false)
const pickSelection = ref([])
const selectedQuestions = ref([])
const randomCount = ref(10)

const default_columns = {
  paperName: { label: '试卷名称', visible: true },
  courseName: { label: '关联课程', visible: true },
  totalScore: { label: '总分', visible: true },
  passScore: { label: '及格分', visible: true },
  duration: { label: '时长', visible: true },
  antiCheat: { label: '防作弊', visible: true },
  status: { label: '状态', visible: true },
  publishTime: { label: '发布时间', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_exam_paper_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, paperName: undefined, courseId: undefined, status: undefined, params: {} },
  rules: {
    paperName: [{ required: true, message: '试卷名称不能为空', trigger: 'blur' }],
    duration: [{ required: true, message: '考试时长不能为空', trigger: 'blur' }],
    passScore: [{ required: true, message: '及格分不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.paperName) count++
  if (queryParams.value.courseId) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length) count++
  return count
})

const composeTotalScore = computed(() => {
  return selectedQuestions.value.reduce((sum, q) => sum + (Number(q.score) || 0), 0).toFixed(1).replace(/\.0$/, '')
})

/* ===== 批量设分 ===== */
const targetScore = ref(100)
const typeScoreInputs = reactive({})
const typeGroups = computed(() => {
  const map = new Map()
  selectedQuestions.value.forEach(q => {
    if (!map.has(q.questionType)) map.set(q.questionType, { type: q.questionType, label: typeLabel(q.questionType), count: 0 })
    map.get(q.questionType).count++
  })
  return [...map.values()]
})
const scoreDiff = computed(() => Math.round(((Number(targetScore.value) || 0) - Number(composeTotalScore.value)) * 10) / 10)
const diffText = computed(() => {
  if (!selectedQuestions.value.length) return ''
  if (scoreDiff.value === 0) return '已凑满目标分'
  return scoreDiff.value > 0 ? `还差 ${scoreDiff.value} 分` : `超出 ${-scoreDiff.value} 分`
})
function applyTypeScore(type) {
  const v = Number(typeScoreInputs[type])
  if (!v || v <= 0) { proxy.$modal.msgWarning('请先输入该题型的每题分值'); return }
  let count = 0
  selectedQuestions.value.forEach(q => { if (q.questionType === type) { q.score = v; count++ } })
  proxy.$modal.msgSuccess(`已将 ${count} 道${typeLabel(type)}的每题分值设为 ${v} 分`)
}
function autoDistribute() {
  const n = selectedQuestions.value.length
  const target = Number(targetScore.value) || 0
  if (!n) { proxy.$modal.msgWarning('请先添加题目'); return }
  if (n * 0.5 > target) { proxy.$modal.msgWarning(`共 ${n} 题，每题至少 0.5 分，目标总分需不低于 ${n * 0.5} 分`); return }
  // 以 0.1 分为最小单位均摊，余数逐题 +0.1 分摊
  const totalTenths = Math.round(target * 10)
  const baseTenths = Math.floor(totalTenths / n)
  let rem = totalTenths - baseTenths * n
  selectedQuestions.value.forEach(q => {
    q.score = (baseTenths + (rem-- > 0 ? 1 : 0)) / 10
  })
  proxy.$modal.msgSuccess(`已按 ${target} 分均摊到 ${n} 题（余数分摊到前几题）`)
}
const viewQuestions = computed(() => viewData.value.questions || [])

function getList() {
  loading.value = true
  const params = proxy.addDateRange({ ...queryParams.value }, dateRange.value)
  listExamPaper(params).then(response => { paperList.value = response.rows; total.value = response.total; applySavedWidths() }).catch(error => { console.error(error) }).finally(() => { loading.value = false })
}
function loadCourseOptions() { listTrainingCourse({ pageNum: 1, pageSize: 500 }).then(res => { courseOptions.value = res.rows || [] }) }
function loadCategoryOptions() { examCategoryOptions().then(res => { categoryOptions.value = res.data || [] }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.paperName = undefined; queryParams.value.courseId = undefined; queryParams.value.status = undefined
  dateRange.value = []
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(item => item.paperId); multiple.value = !selection.length }

function reset() {
  form.value = { paperId: undefined, paperName: undefined, courseId: undefined, courseName: undefined, totalScore: undefined, passScore: 60, duration: 60, shuffleQuestion: 'N', shuffleOption: 'N', switchLimit: 0, showAnalysis: 'Y', status: '0', remark: undefined }
  proxy.resetForm('paperRef')
}
function onFormCourseChange(courseId) {
  const item = courseOptions.value.find(c => c.courseId === courseId)
  form.value.courseName = item ? item.courseName : undefined
}
function handleAdd() { reset(); open.value = true; title.value = '新增试卷' }
function handleUpdate(row) {
  reset()
  getExamPaper(row.paperId).then(response => {
    form.value = response.data
    open.value = true; title.value = '修改试卷'
  })
}
function submitForm() {
  proxy.$refs['paperRef'].validate(valid => {
    if (!valid) return
    if (form.value.paperId != undefined) {
      updateExamPaper(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
    } else {
      addExamPaper(form.value).then(() => { proxy.$modal.msgSuccess('新增成功，请继续点击「组卷」选题'); open.value = false; getList() })
    }
  })
}
function handleDelete(row) { const paperIds = row.paperId || ids.value; proxy.$modal.confirm('是否确认删除所选试卷？').then(function() { return delExamPaper(paperIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

/* ===== 发布/撤回 ===== */
function handlePublish(row) {
  proxy.$modal.confirm(`发布后试卷题目集合将冻结，确认发布「${row.paperName}」？`).then(() => {
    return publishExamPaper(row.paperId)
  }).then(() => { getList(); proxy.$modal.msgSuccess('发布成功') }).catch(() => {})
}
function handleUnpublish(row) {
  proxy.$modal.confirm('撤回后试卷退回草稿状态，确认撤回？').then(() => {
    return unpublishExamPaper(row.paperId)
  }).then(() => { getList(); proxy.$modal.msgSuccess('已撤回') }).catch(e => { if (e && e.message) proxy.$modal.msgError(e.message) })
}

/* ===== 查看 ===== */
function handleView(row) { getExamPaper(row.paperId).then(response => { viewData.value = response.data; viewOpen.value = true }) }

/* ===== 组卷 ===== */
function handleCompose(row) {
  getExamPaper(row.paperId).then(response => {
    composePaper.value = response.data
    selectedQuestions.value = (response.data.questions || []).map(q => ({ ...q, score: Number(q.score) }))
    pickQuery.pageNum = 1
    loadPickList()
    composeOpen.value = true
  })
}
function loadPickList() {
  pickLoading.value = true
  listExamQuestion({ ...pickQuery, status: '0' }).then(res => {
    pickList.value = res.rows || []
    pickTotal.value = res.total || 0
  }).finally(() => { pickLoading.value = false })
}
function pickSelectionChange(selection) { pickSelection.value = selection }
function addPickedQuestions() {
  const existIds = new Set(selectedQuestions.value.map(q => q.questionId))
  let added = 0
  pickSelection.value.forEach(q => {
    if (!existIds.has(q.questionId)) {
      selectedQuestions.value.push({ questionId: q.questionId, questionType: q.questionType, content: q.content, score: 2 })
      added++
    }
  })
  if (added === 0) proxy.$modal.msgWarning('所选题目均已在试卷中')
}
function removeSelected(idx) { selectedQuestions.value.splice(idx, 1) }
function clearSelected() { selectedQuestions.value = [] }
function handleRandomPick() {
  randomPickExamQuestion({ categoryId: pickQuery.categoryId, questionType: pickQuery.questionType, difficulty: pickQuery.difficulty, count: randomCount.value }).then(res => {
    const existIds = new Set(selectedQuestions.value.map(q => q.questionId))
    let added = 0
    ;(res.data || []).forEach(q => {
      if (!existIds.has(q.questionId)) {
        selectedQuestions.value.push({ questionId: q.questionId, questionType: q.questionType, content: q.content, score: 2 })
        added++
      }
    })
    proxy.$modal.msgSuccess(`随机抽入 ${added} 题${added < randomCount.value ? `（题库符合条件仅 ${added} 题）` : ''}`)
  })
}
function submitCompose() {
  if (!selectedQuestions.value.length) { proxy.$modal.msgWarning('请先添加题目'); return }
  const totalScore = selectedQuestions.value.reduce((sum, q) => sum + (Number(q.score) || 0), 0)
  if (composePaper.value.passScore != null && Number(composePaper.value.passScore) > totalScore) {
    proxy.$modal.msgWarning(`及格分（${composePaper.value.passScore}）大于当前总分（${totalScore}），请调整分值或及格分`)
    return
  }
  composeSaving.value = true
  savePaperQuestions({ paperId: composePaper.value.paperId, questions: selectedQuestions.value }).then(() => {
    proxy.$modal.msgSuccess(`组卷已保存，共 ${selectedQuestions.value.length} 题，总分 ${totalScore} 分`)
    composeOpen.value = false
    getList()
  }).finally(() => { composeSaving.value = false })
}
function cancel() { open.value = false; reset() }

/* ===== 展示辅助 ===== */
function typeLabel(type) { const item = safety_exam_question_type.value.find(d => d.value == type); return item ? item.label : '-' }
function typeBadgeClass(type) { return { '1': 'blue', '2': 'violet', '3': 'green' }[type] || 'gray' }
function diffLabel(diff) { const item = safety_exam_difficulty.value.find(d => d.value == diff); return item ? item.label : '-' }
function diffBadgeClass(diff) { return { '1': 'green', '2': 'blue', '3': 'red' }[diff] || 'gray' }
function statusLabel(status) { return { '0': '草稿', '1': '已发布', '2': '停用' }[status] || '-' }
function statusBadgeClass(status) { return { '0': 'gray', '1': 'green', '2': 'red' }[status] || 'gray' }

loadCourseOptions()
loadCategoryOptions()
getList()
</script>

<style scoped>
.safety-exam-paper-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-exam-paper-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-exam-paper-page .filter-card { padding:14px 20px 16px; }
.safety-exam-paper-page .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-exam-paper-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-exam-paper-page .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-exam-paper-page .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-exam-paper-page .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-exam-paper-page .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-exam-paper-page .filter-buttons { display:flex; gap:8px; }
.safety-exam-paper-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-exam-paper-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-exam-paper-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-exam-paper-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-exam-paper-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-exam-paper-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-exam-paper-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-exam-paper-page .field .control :deep(.el-select) { width:100%; }
.safety-exam-paper-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-exam-paper-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-exam-paper-page .toolbar .left, .safety-exam-paper-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-exam-paper-page .table-wrap { overflow-x:auto; }
.safety-exam-paper-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-exam-paper-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-exam-paper-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-exam-paper-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-exam-paper-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-exam-paper-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-exam-paper-page .batch-score-bar { padding:10px 14px 12px; background:var(--ink-50); border:1px dashed var(--ink-200); border-radius:var(--r-sm); margin-bottom:10px; }
.safety-exam-paper-page .bs-head { display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:10px; padding-bottom:8px; border-bottom:1px dashed var(--ink-200); }
.safety-exam-paper-page .bs-label { display:flex; align-items:center; gap:6px; font-size:13px; font-weight:600; color:var(--brand-700); white-space:nowrap; }
.safety-exam-paper-page .bs-target { display:flex; align-items:center; gap:8px; font-size:13px; color:var(--ink-700); white-space:nowrap; }
.safety-exam-paper-page .bs-target-label { font-weight:500; }
.safety-exam-paper-page .bs-types { display:flex; flex-wrap:wrap; align-items:stretch; gap:8px; }
.safety-exam-paper-page .bs-type { display:flex; align-items:center; gap:7px; padding:6px 10px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); box-shadow:var(--shadow-card); }
.safety-exam-paper-page .bs-count { font-size:13px; color:var(--ink-700); font-weight:600; white-space:nowrap; }
.safety-exam-paper-page .bs-sep { color:var(--ink-400); font-size:12px; }
.safety-exam-paper-page .bs-unit { font-size:13px; color:var(--ink-500); white-space:nowrap; }
.safety-exam-paper-page .bs-diff { font-weight:600; padding:2px 8px; border-radius:999px; background:#fff; }
.safety-exam-paper-page .bs-diff.ok { color:var(--green-700); border:1px solid var(--green-500); }
.safety-exam-paper-page .bs-diff.lack { color:var(--red-700); border:1px solid var(--red-500); }
.safety-exam-paper-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-exam-paper-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-exam-paper-page .badge.green .dot { background:var(--green-500); }
.safety-exam-paper-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-exam-paper-page .badge.red .dot { background:var(--red-500); }
.safety-exam-paper-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-exam-paper-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-exam-paper-page .badge.blue .dot { background:var(--blue-500); }
.safety-exam-paper-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-exam-paper-page .badge.gray .dot { background:var(--ink-400); }
.safety-exam-paper-page .text-muted { color:var(--ink-400); }
.safety-exam-paper-page .cheat-tags { display:inline-flex; gap:4px; flex-wrap:wrap; justify-content:center; }

/* ===== 组卷弹窗 ===== */
.compose-body { display:flex; flex-direction:column; gap:10px; }
.compose-filter { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.compose-filter-right { margin-left:auto; display:flex; gap:8px; align-items:center; }
.random-label { font-size:13px; color:var(--ink-500); }
.pick-table { width:100%; }
.pick-actions { display:flex; align-items:center; justify-content:space-between; }
.selected-head { display:flex; align-items:center; gap:10px; margin-top:2px; }
.selected-title { font-size:14px; font-weight:600; color:var(--ink-700); }
.selected-tip { font-size:12px; color:var(--ink-400); flex:1; }

/* ===== 详情题目 ===== */
.view-question-item { padding:8px 0; border-bottom:1px dashed var(--ink-200, #e2e8f0); }
.view-question-item:last-child { border-bottom:none; }
.vq-head { display:flex; align-items:center; gap:10px; }
.vq-no { width:22px; height:22px; border-radius:50%; background:var(--ink-100); color:var(--ink-500); display:inline-flex; align-items:center; justify-content:center; font-size:12px; font-weight:600; flex-shrink:0; }
.vq-content { flex:1; font-size:14px; color:var(--ink-700); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.vq-score { font-size:13px; font-weight:600; color:var(--brand-600); flex-shrink:0; }

/* 操作列按钮对齐 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .safety-exam-paper-page .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-exam-paper-page .filter-bar { grid-template-columns:1fr; } }
</style>
