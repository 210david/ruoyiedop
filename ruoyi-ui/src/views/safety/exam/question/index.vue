<template>
  <div class="app-container safety-exam-question-page">
    <div class="task-layout">
      <!-- ===== 左侧：题目分类树 ===== -->
      <div class="surface tree-card" :class="{ 'is-collapsed': !treeExpanded }">
        <div class="tree-head">
          <template v-if="treeExpanded">
            <div class="filter-title"><span class="glyph"></span> 题目分类</div>
          </template>
          <el-tooltip :content="treeExpanded ? '收起侧栏' : '展开侧栏'" placement="right">
            <el-icon class="tree-toggle" @click="toggleTree"><Fold v-if="treeExpanded" /><Expand v-else /></el-icon>
          </el-tooltip>
        </div>
        <template v-if="treeExpanded">
          <div class="tree-search">
            <el-input v-model="treeKeyword" placeholder="搜索分类" clearable>
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <div class="tree-body">
            <el-tree ref="treeRef" :key="treeRenderKey" :data="treeData" node-key="id"
              :props="{ label: 'label', children: 'children' }"
              :filter-node-method="filterNode" highlight-current
              :expand-on-click-node="false" default-expand-all @node-click="handleNodeClick">
              <template #default="{ data }">
                <span class="tree-node" :class="{ 'is-leaf': !data.children }">
                  <el-icon v-if="data.children" class="tree-icon"><Folder /></el-icon>
                  <el-icon v-else class="tree-icon leaf"><Collection /></el-icon>
                  <span class="tree-label">{{ data.label }}</span>
                </span>
              </template>
            </el-tree>
            <div class="tree-empty" v-if="!treeLoading && !treeData.length">暂无题目分类</div>
          </div>
          <div class="tree-footer">
            <el-button link type="primary" icon="Setting" @click="openCategoryMgr" v-hasPermi="['safety:exam:question:add']">管理分类</el-button>
          </div>
        </template>
      </div>

      <!-- ===== 右侧：筛选 + 列表 ===== -->
      <div class="task-main">
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
              <label>题干</label>
              <div class="control">
                <el-input v-model="queryParams.content" placeholder="请输入关键字" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>题型</label>
              <div class="control is-select">
                <el-select v-model="queryParams.questionType" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="dict in safety_exam_question_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>难度</label>
              <div class="control is-select">
                <el-select v-model="queryParams.difficulty" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="dict in safety_exam_difficulty" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>适用工种</label>
              <div class="control is-select">
                <el-select v-model="queryParams.jobTypes" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="dict in safety_job_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </div>
            </div>
            <div class="field" v-show="showAdvanced">
              <label>状态</label>
              <div class="control is-select">
                <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
                  <el-option label="启用" value="0" />
                  <el-option label="停用" value="1" />
                </el-select>
              </div>
            </div>
            <div class="field" v-show="showAdvanced">
              <label>题目分类</label>
              <div class="control is-select">
                <el-select v-model="queryParams.categoryId" placeholder="全部" clearable filterable @change="handleQuery">
                  <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
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

        <div class="surface">
          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:exam:question:add']">新增</el-button>
              <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:exam:question:remove']">删除</el-button>
              <div class="toolbar-divider"></div>
              <el-button type="success" plain icon="Upload" @click="handleImport" v-hasPermi="['safety:exam:question:import']">导入</el-button>
              <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:exam:question:export']">导出</el-button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_exam_question_columns" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="tableRef" v-loading="loading" :data="questionList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
              <el-table-column label="题干" prop="content" key="content" :min-width="colWidth('content', 300)" resizable show-overflow-tooltip v-if="columns.content.visible" />
              <el-table-column label="题型" prop="questionType" key="questionType" :width="colWidth('questionType', 100)" resizable align="center" v-if="columns.questionType.visible">
                <template #default="scope"><span class="badge" :class="typeBadgeClass(scope.row.questionType)">{{ typeLabel(scope.row.questionType) }}</span></template>
              </el-table-column>
              <el-table-column label="难度" prop="difficulty" key="difficulty" :width="colWidth('difficulty', 90)" resizable align="center" v-if="columns.difficulty.visible">
                <template #default="scope"><span class="badge" :class="diffBadgeClass(scope.row.difficulty)">{{ diffLabel(scope.row.difficulty) }}</span></template>
              </el-table-column>
              <el-table-column label="正确答案" prop="answer" key="answer" :width="colWidth('answer', 110)" resizable align="center" v-if="columns.answer.visible">
                <template #default="scope"><span class="answer-text">{{ scope.row.answer }}</span></template>
              </el-table-column>
              <el-table-column label="题目分类" prop="categoryName" key="categoryName" :width="colWidth('categoryName', 130)" resizable align="center" show-overflow-tooltip v-if="columns.categoryName.visible" />
              <el-table-column label="适用工种" prop="jobTypes" key="jobTypes" :min-width="colWidth('jobTypes', 150)" resizable align="center" v-if="columns.jobTypes.visible">
                <template #default="scope"><span class="job-types">{{ jobTypesText(scope.row.jobTypes) }}</span></template>
              </el-table-column>
              <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
                <template #default="scope"><span>{{ scope.row.createTime || '-' }}</span></template>
              </el-table-column>
              <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 96)" resizable align="center" v-if="columns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === '0' ? 'green' : 'red'" style="cursor: pointer" @click="handleToggleStatus(scope.row)" v-hasPermi="['safety:exam:question:edit']">
                    <span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}
                  </span>
                  <span v-if="!checkPermi(['safety:exam:question:edit'])" class="badge" :class="scope.row.status === '0' ? 'green' : 'red'">
                    <span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" :width="colWidth('操作', 150)" align="center" fixed="right" class-name="col-action" resizable>
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:exam:question:query']">查看</el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:exam:question:edit']">修改</el-button>
                    <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:exam:question:remove']">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
        </div>
      </div>
    </div>

    <!-- ===== 新增/编辑弹窗 ===== -->
    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="questionRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="题型" prop="questionType">
                  <el-radio-group v-model="form.questionType" @change="handleTypeChange">
                    <el-radio-button v-for="dict in safety_exam_question_type" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio-button>
                  </el-radio-group>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="题目分类" prop="categoryId">
                  <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%" clearable @change="onFormCategoryChange">
                    <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="难度" prop="difficulty">
                  <el-select v-model="form.difficulty" placeholder="请选择" style="width: 100%">
                    <el-option v-for="dict in safety_exam_difficulty" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="适用工种" prop="jobTypeList">
                  <el-select v-model="form.jobTypeList" placeholder="可多选，留空为通用" style="width: 100%" multiple collapse-tags collapse-tags-tooltip clearable>
                    <el-option v-for="dict in safety_job_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
              <el-form-item label="题干" prop="content">
                <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题干" maxlength="500" show-word-limit />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/></svg></span>选项与答案</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <template v-if="form.questionType !== '3'">
                <el-form-item v-for="(opt, idx) in optionRows" :key="idx" :label="optionLetters[idx]" :prop="'optionRows.' + idx">
                  <div class="option-row">
                    <el-input v-model="optionRows[idx]" placeholder="请输入选项内容" maxlength="200" />
                    <el-button v-if="optionRows.length > 2" link type="danger" icon="Delete" @click="removeOption(idx)" />
                  </div>
                </el-form-item>
                <el-form-item label=" " v-if="optionRows.length < 8">
                  <el-button link type="primary" icon="Plus" @click="addOption">添加选项</el-button>
                </el-form-item>
                <el-form-item :label="form.questionType === '2' ? '正确答案(多选)' : '正确答案'" prop="answer">
                  <el-checkbox-group v-if="form.questionType === '2'" v-model="answerList">
                    <el-checkbox v-for="(letter, idx) in optionLetters.slice(0, optionRows.length)" :key="letter" :value="letter">{{ letter }}</el-checkbox>
                  </el-checkbox-group>
                  <el-radio-group v-else v-model="form.answer">
                    <el-radio v-for="letter in optionLetters.slice(0, optionRows.length)" :key="letter" :value="letter">{{ letter }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </template>
              <template v-else>
                <el-form-item label="选项">
                  <div class="judge-options"><span class="badge gray">正确</span><span class="badge gray">错误</span><span class="judge-tip">判断题选项固定，无需编辑</span></div>
                </el-form-item>
                <el-form-item label="正确答案" prop="answer">
                  <el-radio-group v-model="form.answer">
                    <el-radio value="对">对</el-radio>
                    <el-radio value="错">错</el-radio>
                  </el-radio-group>
                </el-form-item>
              </template>
              <el-form-item label="答案解析" prop="analysis">
                <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="选填，答题后展示给考生" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>状态与备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio value="0">启用</el-radio>
                    <el-radio value="1">停用</el-radio>
                  </el-radio-group>
                </el-form-item></el-col>
              </el-row>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">题目详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.categoryName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">分类：{{ viewData.categoryName }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg></span>题干</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">题型 / 难度</span><div class="rd-value">
                <span class="badge" :class="typeBadgeClass(viewData.questionType)">{{ typeLabel(viewData.questionType) }}</span>
                <span class="badge" :class="diffBadgeClass(viewData.difficulty)" style="margin-left: 8px">{{ diffLabel(viewData.difficulty) }}</span>
              </div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">题干</span><div class="rd-value">{{ viewData.content || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/></svg></span>选项与答案</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="view-options">
              <div class="view-option-item" v-for="(opt, idx) in viewOptionList" :key="idx" :class="{ 'is-correct': isCorrectOption(idx) }">
                <span class="view-option-letter">{{ optionLetters[idx] }}</span>
                <span class="view-option-text">{{ opt }}</span>
                <el-icon v-if="isCorrectOption(idx)" class="view-option-check"><Check /></el-icon>
              </div>
            </div>
            <div class="rd-grid" style="margin-top: 12px">
              <div class="rd-item"><span class="rd-label">正确答案</span><div class="rd-value"><span class="answer-text strong">{{ viewData.answer }}</span></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="viewData.status === '0' ? 'green' : 'red'"><span class="dot"></span>{{ viewData.status === '0' ? '启用' : '停用' }}</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">答案解析</span><div class="rd-value">{{ viewData.analysis || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">题目分类</span><div class="rd-value">{{ viewData.categoryName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">适用工种</span><div class="rd-value">{{ jobTypesText(viewData.jobTypes) }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 分类管理弹窗 ===== -->
    <el-dialog v-model="categoryOpen" title="题目分类管理" width="620px" append-to-body>
      <div class="category-mgr">
        <div class="category-add-row">
          <el-input v-model="categoryForm.categoryName" placeholder="新分类名称" maxlength="50" style="flex: 1" @keyup.enter="submitCategoryAdd" />
          <el-input-number v-model="categoryForm.orderNum" :min="0" :max="999" controls-position="right" style="width: 110px" />
          <el-button type="primary" icon="Plus" @click="submitCategoryAdd" v-hasPermi="['safety:exam:question:add']">添加</el-button>
        </div>
        <el-table :data="categoryOptions" border size="small" max-height="360" class="category-table">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="分类名称" prop="categoryName" min-width="180" show-overflow-tooltip />
          <el-table-column label="排序" prop="orderNum" width="80" align="center" />
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleCategoryEdit(scope.row)" v-hasPermi="['safety:exam:question:edit']">改名</el-button>
              <el-button link type="danger" icon="Delete" @click="handleCategoryDelete(scope.row)" v-hasPermi="['safety:exam:question:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer><el-button @click="categoryOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 分类改名弹窗 ===== -->
    <el-dialog v-model="categoryEditOpen" title="修改分类名称" width="420px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="分类名称"><el-input v-model="categoryEditName" maxlength="50" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitCategoryEdit">确 定</el-button><el-button @click="categoryEditOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="题目导入"
      action="/safety/exam/question/importData"
      template-action="/safety/exam/question/importTemplate"
      template-file-name="exam_question_template"
      update-support-label="是否更新已存在的题目数据"
      :tips="importTips"
      :column-labels="importColumnLabels"
      @success="onImportSuccess"
    />

  </div>
</template>

<script setup name="SafetyExamQuestion">
import { listExamQuestion, getExamQuestion, addExamQuestion, updateExamQuestion, delExamQuestion, changeQuestionStatus } from '@/api/safety/examQuestion'
import { examCategoryOptions, addExamCategory, updateExamCategory, delExamCategory } from '@/api/safety/examCategory'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, Fold, Expand, Folder, Collection, Check } from '@element-plus/icons-vue'
import ExcelImportDialog from '@/components/ExcelImportDialog/index.vue'
import { checkPermi } from '@/utils/permission'

const { proxy } = getCurrentInstance()
const { safety_exam_question_type, safety_exam_difficulty, safety_job_type } = proxy.useDict('safety_exam_question_type', 'safety_exam_difficulty', 'safety_job_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_exam_question_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])

const optionLetters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

const questionList = ref([])
const categoryOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref({})
const optionRows = ref(['', '', '', ''])
const answerList = ref([])

/* ===== 左树 ===== */
const treeRef = ref(null)
const treeExpanded = ref(true)
const treeRenderKey = ref(0)
const treeKeyword = ref('')
const treeLoading = ref(false)
const treeData = ref([])

const importRef = ref(null)
const importTips = [
  '请先下载导入模板，按模板列顺序填写题目数据',
  '题型列请填写：单选题、多选题、判断题',
  '单选/多选题答案请填写字母（如 A 或 ABD），判断题答案只能填：对 或 错',
  '难度列请填写：简单、中等、困难（留空默认中等）',
  '题目分类需与系统中已启用分类名称一致，不存在时该行导入失败',
  '任一行数据不合法将整体回滚，请修正后重新导入'
]
const importColumnLabels = {
  questionType: '题型',
  content: '题干',
  answer: '正确答案',
  analysis: '答案解析',
  categoryName: '题目分类',
  difficulty: '难度',
  jobTypes: '适用工种',
  status: '状态'
}

const default_columns = {
  content: { label: '题干', visible: true },
  questionType: { label: '题型', visible: true },
  difficulty: { label: '难度', visible: true },
  answer: { label: '正确答案', visible: true },
  categoryName: { label: '题目分类', visible: true },
  jobTypes: { label: '适用工种', visible: true },
  createTime: { label: '创建时间', visible: true },
  status: { label: '状态', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_exam_question_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, content: undefined, questionType: undefined, difficulty: undefined, jobTypes: undefined, status: undefined, categoryId: undefined, params: {} },
  rules: {
    questionType: [{ required: true, message: '题型不能为空', trigger: 'change' }],
    content: [{ required: true, message: '题干不能为空', trigger: 'blur' }],
    answer: [{ required: true, message: '正确答案不能为空', trigger: 'change' }],
    difficulty: [{ required: true, message: '难度不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.content) count++
  if (queryParams.value.questionType) count++
  if (queryParams.value.difficulty) count++
  if (queryParams.value.jobTypes) count++
  if (queryParams.value.status) count++
  if (queryParams.value.categoryId) count++
  return count
})

const viewOptionList = computed(() => {
  if (!viewData.value.options) return []
  try { return JSON.parse(viewData.value.options) } catch (e) { return [] }
})

/* ===== 分类树 ===== */
function buildTree(categories) {
  const roots = []
  const map = {}
  categories.forEach(c => { map[c.categoryId] = { id: c.categoryId, label: c.categoryName, raw: c, children: [] } })
  categories.forEach(c => {
    const node = map[c.categoryId]
    if (c.parentId && map[c.parentId]) { map[c.parentId].children.push(node) } else { roots.push(node) }
  })
  roots.forEach(r => { if (!r.children.length) delete r.children })
  return roots
}
function loadTree() {
  treeLoading.value = true
  examCategoryOptions().then(res => {
    categoryOptions.value = res.data || []
    treeData.value = buildTree(res.data || [])
  }).finally(() => { treeLoading.value = false })
}
watch(treeKeyword, val => { treeRef.value && treeRef.value.filter(val) })
function filterNode(value, data) { if (!value) return true; return data.label.indexOf(value) !== -1 }
function toggleTree() { treeExpanded.value = !treeExpanded.value; treeRenderKey.value++ }
function handleNodeClick(data) {
  queryParams.value.categoryId = data.id
  queryParams.value.pageNum = 1
  getList()
}

/* ===== 分类管理 ===== */
const categoryOpen = ref(false)
const categoryEditOpen = ref(false)
const categoryEditId = ref(null)
const categoryEditName = ref('')
const categoryForm = reactive({ categoryName: '', orderNum: 0 })
function openCategoryMgr() { categoryForm.categoryName = ''; categoryForm.orderNum = 0; categoryOpen.value = true }
function submitCategoryAdd() {
  if (!categoryForm.categoryName || !categoryForm.categoryName.trim()) { proxy.$modal.msgWarning('请输入分类名称'); return }
  addExamCategory({ categoryName: categoryForm.categoryName.trim(), orderNum: categoryForm.orderNum, parentId: 0, status: '0' }).then(() => {
    proxy.$modal.msgSuccess('分类已添加'); categoryForm.categoryName = ''; loadTree()
  })
}
function handleCategoryEdit(row) { categoryEditId.value = row.categoryId; categoryEditName.value = row.categoryName; categoryEditOpen.value = true }
function submitCategoryEdit() {
  if (!categoryEditName.value.trim()) { proxy.$modal.msgWarning('请输入分类名称'); return }
  updateExamCategory({ categoryId: categoryEditId.value, categoryName: categoryEditName.value.trim() }).then(() => {
    proxy.$modal.msgSuccess('修改成功'); categoryEditOpen.value = false; loadTree()
  })
}
function handleCategoryDelete(row) {
  proxy.$modal.confirm(`是否确认删除分类「${row.categoryName}」？分类下存在题目时将无法删除`).then(() => {
    return delExamCategory(row.categoryId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功'); loadTree()
    if (queryParams.value.categoryId === row.categoryId) { queryParams.value.categoryId = undefined; getList() }
  }).catch(() => {})
}

/* ===== 列表 ===== */
function getList() { loading.value = true; listExamQuestion(queryParams.value).then(response => { questionList.value = response.rows; total.value = response.total; applySavedWidths() }).catch(error => { console.error(error) }).finally(() => { loading.value = false }) }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.content = undefined; queryParams.value.questionType = undefined; queryParams.value.difficulty = undefined
  queryParams.value.jobTypes = undefined; queryParams.value.status = undefined; queryParams.value.categoryId = undefined
  treeRef.value && treeRef.value.setCurrentKey(null)
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(item => item.questionId); multiple.value = !selection.length }

/* ===== 题目表单 ===== */
function reset() {
  form.value = { questionId: undefined, questionType: '1', content: undefined, answer: undefined, analysis: undefined, categoryId: queryParams.value.categoryId || undefined, difficulty: '2', jobTypeList: [], status: '0', remark: undefined }
  optionRows.value = ['', '', '', '']
  answerList.value = []
  proxy.resetForm('questionRef')
}
function handleTypeChange(type) {
  form.value.answer = undefined; answerList.value = []
  if (type === '3') { optionRows.value = [] } else if (!optionRows.value.length) { optionRows.value = ['', '', '', ''] }
}
function onFormCategoryChange(categoryId) {
  const item = categoryOptions.value.find(c => c.categoryId === categoryId)
  form.value.categoryName = item ? item.categoryName : undefined
}
function addOption() { if (optionRows.value.length < 8) optionRows.value.push('') }
function removeOption(idx) {
  optionRows.value.splice(idx, 1)
  const removedLetter = optionLetters[idx]
  if (form.value.questionType === '2') {
    answerList.value = answerList.value.filter(l => l !== removedLetter)
  } else if (form.value.answer === removedLetter) {
    form.value.answer = undefined
  }
}
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = '新增题目' }
function handleUpdate(row) {
  reset()
  getExamQuestion(row.questionId || ids.value[0]).then(response => {
    form.value = response.data
    form.value.jobTypeList = form.value.jobTypes ? String(form.value.jobTypes).split(',') : []
    if (form.value.questionType !== '3') {
      try { optionRows.value = JSON.parse(form.value.options || '[]') } catch (e) { optionRows.value = [] }
      if (form.value.questionType === '2') { answerList.value = form.value.answer ? String(form.value.answer).split('') : [] }
    } else {
      optionRows.value = []
    }
    open.value = true; title.value = '修改题目'
  })
}
function handleView(row) { getExamQuestion(row.questionId).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['questionRef'].validate(valid => {
    if (!valid) return
    if (form.value.questionType === '3') {
      form.value.options = '["正确","错误"]'
    } else {
      const emptyIdx = optionRows.value.findIndex(o => !o || !o.trim())
      if (emptyIdx >= 0) { proxy.$modal.msgWarning(`选项 ${optionLetters[emptyIdx]} 内容不能为空`); return }
      form.value.options = JSON.stringify(optionRows.value.map(o => o.trim()))
      if (form.value.questionType === '2') {
        if (answerList.value.length < 2) { proxy.$modal.msgWarning('多选题答案至少选择 2 项'); return }
        form.value.answer = [...answerList.value].sort().join('')
      }
    }
    form.value.jobTypes = form.value.jobTypeList && form.value.jobTypeList.length ? form.value.jobTypeList.join(',') : undefined
    if (form.value.questionId != undefined) {
      updateExamQuestion(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
    } else {
      addExamQuestion(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
    }
  })
}
function handleDelete(row) { const questionIds = row.questionId || ids.value; proxy.$modal.confirm('是否确认删除所选题目？').then(function() { return delExamQuestion(questionIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleToggleStatus(row) {
  const next = row.status === '0' ? '1' : '0'
  const tip = next === '1' ? `停用后该题目不会被随机组卷选中，确认停用「${row.content.substring(0, 20)}…」？` : '确认启用该题目？'
  proxy.$modal.confirm(tip).then(() => {
    return changeQuestionStatus(row.questionId, next)
  }).then(() => { row.status = next; proxy.$modal.msgSuccess(next === '1' ? '已停用' : '已启用') }).catch(() => {})
}
function handleExport() { proxy.download('safety/exam/question/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'questionId'), `exam_question_${new Date().getTime()}.xlsx`) }
function handleImport() { proxy.$refs.importRef.open() }
function onImportSuccess() { loadTree(); getList() }
function cancel() { open.value = false; reset() }

/* ===== 展示辅助 ===== */
function typeLabel(type) { const item = safety_exam_question_type.value.find(d => d.value == type); return item ? item.label : '-' }
function typeBadgeClass(type) { return { '1': 'blue', '2': 'violet', '3': 'green' }[type] || 'gray' }
function diffLabel(diff) { const item = safety_exam_difficulty.value.find(d => d.value == diff); return item ? item.label : '-' }
function diffBadgeClass(diff) { return { '1': 'green', '2': 'blue', '3': 'red' }[diff] || 'gray' }
function jobTypesText(jobTypes) {
  if (!jobTypes) return '通用'
  return String(jobTypes).split(',').map(v => { const d = safety_job_type.value.find(x => x.value == v); return d ? d.label : v }).join('、')
}
function isCorrectOption(idx) {
  if (!viewData.value.answer) return false
  if (viewData.value.questionType === '3') return false
  return String(viewData.value.answer).indexOf(optionLetters[idx]) !== -1
}

loadTree()
getList()
</script>

<style scoped>
.safety-exam-question-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-exam-question-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-exam-question-page .filter-card { padding:14px 20px 16px; }
.safety-exam-question-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-exam-question-page .filter-head .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-exam-question-page .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-exam-question-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-exam-question-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-exam-question-page .filter-card .adv-link .chev { transition:transform .2s; }
.safety-exam-question-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-exam-question-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-exam-question-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-exam-question-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-exam-question-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-exam-question-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-exam-question-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-exam-question-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-exam-question-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-exam-question-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-exam-question-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-exam-question-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-exam-question-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-exam-question-page .field .control :deep(.el-select) { width:100%; }
.safety-exam-question-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-exam-question-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-exam-question-page .toolbar .left, .safety-exam-question-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-exam-question-page .toolbar-divider { width:1px; height:16px; background:var(--ink-200); margin:0 4px; }
.safety-exam-question-page .table-wrap { overflow-x:auto; }
.safety-exam-question-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-exam-question-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-exam-question-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-exam-question-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-exam-question-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-exam-question-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-exam-question-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-exam-question-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-exam-question-page .badge.green .dot { background:var(--green-500); }
.safety-exam-question-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-exam-question-page .badge.red .dot { background:var(--red-500); }
.safety-exam-question-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-exam-question-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-exam-question-page .badge.blue .dot { background:var(--blue-500); }
.safety-exam-question-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-exam-question-page .badge.gray .dot { background:var(--ink-400); }
.safety-exam-question-page .answer-text { font-weight:600; color:var(--brand-600); letter-spacing:.5px; }
.safety-exam-question-page .answer-text.strong { font-size:15px; }
.safety-exam-question-page .job-types { font-size:13px; color:var(--ink-500); }

/* ===== 左树右列表布局 ===== */
.safety-exam-question-page .task-layout { display:flex; gap:6px; align-items:stretch; }
.safety-exam-question-page .task-main { flex:1; min-width:0; display:flex; flex-direction:column; }
.safety-exam-question-page .tree-card { width:220px; flex-shrink:0; align-self:flex-start; margin-bottom:8px; padding:12px 10px 10px; transition:width .2s; }
.safety-exam-question-page .tree-card.is-collapsed { width:44px; padding:14px 6px 10px; }
.safety-exam-question-page .tree-card.is-collapsed .tree-head { justify-content:center; border-bottom:none; margin-bottom:0; padding-bottom:0; }
.safety-exam-question-page .tree-head { display:flex; align-items:center; justify-content:space-between; padding:0 6px 10px; border-bottom:1px dashed var(--ink-200); margin-bottom:8px; }
.safety-exam-question-page .tree-toggle { cursor:pointer; color:var(--ink-500); }
.safety-exam-question-page .tree-toggle:hover { color:var(--brand-600); }
.safety-exam-question-page .tree-search { padding:0 2px 8px; }
.safety-exam-question-page .tree-search :deep(.el-input__wrapper) { box-shadow:0 0 0 1px var(--ink-200) inset; border-radius:6px; }
.safety-exam-question-page .tree-body { max-height:calc(100vh - 330px); overflow:auto; }
.safety-exam-question-page .tree-node { display:flex; align-items:center; gap:6px; font-size:13px; overflow:hidden; }
.safety-exam-question-page .tree-node .tree-icon { color:var(--brand-600); flex-shrink:0; }
.safety-exam-question-page .tree-node .tree-icon.leaf { color:var(--green-500); }
.safety-exam-question-page .tree-node .tree-label { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.safety-exam-question-page .tree-empty { text-align:center; color:var(--ink-400); font-size:13px; padding:24px 0; }
.safety-exam-question-page .tree-footer { border-top:1px dashed var(--ink-200); margin-top:8px; padding:8px 6px 0; text-align:center; }
.safety-exam-question-page .tree-card :deep(.el-tree-node__content) { height:32px; border-radius:8px; }

/* ===== 弹窗内辅助 ===== */
.option-row { display:flex; align-items:center; gap:8px; width:100%; }
.judge-options { display:flex; align-items:center; gap:8px; }
.judge-tip { font-size:13px; color:var(--ink-400); }
.view-options { display:flex; flex-direction:column; gap:8px; }
.view-option-item { display:flex; align-items:center; gap:10px; padding:9px 14px; border:1px solid var(--ink-200); border-radius:8px; font-size:14px; color:var(--ink-700); }
.view-option-item.is-correct { border-color:#a7f3d0; background:var(--green-50); color:var(--green-700); font-weight:600; }
.view-option-letter { width:24px; height:24px; border-radius:50%; background:var(--ink-100); color:var(--ink-500); display:inline-flex; align-items:center; justify-content:center; font-size:13px; font-weight:600; flex-shrink:0; }
.view-option-item.is-correct .view-option-letter { background:var(--green-500); color:#fff; }
.view-option-text { flex:1; }
.view-option-check { color:var(--green-500); flex-shrink:0; }

/* ===== 分类管理 ===== */
.category-mgr { display:flex; flex-direction:column; gap:12px; }
.category-add-row { display:flex; gap:8px; align-items:center; }
.category-table :deep(.el-table__header th) { background:var(--ink-50, #f8fafc); color:#64748b; font-weight:600; }

/* 操作列按钮对齐 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .safety-exam-question-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-exam-question-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
