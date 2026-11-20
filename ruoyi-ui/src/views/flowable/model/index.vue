<template>
  <div class="app-container flowable-model-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>模型名称</label>
          <div class="control">
            <el-input v-model="queryParams.modelName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix
                ><el-icon><Search /></el-icon
              ></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>模型Key</label>
          <div class="control">
            <el-input v-model="queryParams.modelKey" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix
                ><el-icon><Document /></el-icon
              ></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程分类</label>
          <div class="control is-select">
            <el-select v-model="queryParams.category" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>部署状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.deployed" placeholder="全部" clearable @change="handleQuery">
              <el-option label="已部署" value="1" />
              <el-option label="未部署" value="0" />
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

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:model:add']">新增</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_model_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
          <template v-for="colKey in sortedColumnKeys" :key="colKey">
            <el-table-column v-if="colKey === '序号'" type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
            <el-table-column v-else-if="colKey === 'modelName'" label="模型名称" prop="modelName" key="modelName" :min-width="colWidth('modelName', 160)" resizable show-overflow-tooltip>
              <template #default="scope"
                ><span class="col-strong">{{ scope.row.modelName }}</span></template
              >
            </el-table-column>
            <el-table-column v-else-if="colKey === 'modelKey'" label="模型Key" prop="modelKey" key="modelKey" :min-width="colWidth('modelKey', 140)" resizable show-overflow-tooltip>
              <template #default="scope"
                ><span class="col-mono">{{ scope.row.modelKey }}</span></template
              >
            </el-table-column>
            <el-table-column v-else-if="colKey === 'category'" label="流程分类" prop="category" key="category" :width="colWidth('category', 110)" resizable align="center">
              <template #default="scope">
                <span class="badge violet">{{ categoryLabel(scope.row.category) }}</span>
              </template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'formName'" label="关联表单" prop="formName" key="formName" :width="colWidth('formName', 150)" resizable align="center" show-overflow-tooltip>
              <template #default="scope">
                <span v-if="scope.row.formName" class="badge blue"><span class="dot"></span>{{ scope.row.formName }}</span>
                <span v-else class="badge gray"><span class="dot"></span>未关联</span>
              </template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'version'" label="版本" prop="version" key="version" :width="colWidth('version', 110)" resizable align="center">
              <template #default="scope">
                <template v-if="scope.row.version != null">
                  <span class="version-no">V{{ scope.row.version }}</span>
                  <span v-if="scope.row.activeFlag === '1'" class="version-active-dot" title="当前活动版本">●</span>
                  <span v-if="scope.row.suspended" class="version-suspended" title="该版本已挂起，不可发起新流程">已挂起</span>
                </template>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'deployed'" label="部署状态" prop="deployed" key="deployed" :width="colWidth('deployed', 120)" resizable align="center">
              <template #default="scope">
                <div class="deploy-cell">
                  <span class="badge" :class="scope.row.deployed === '1' ? 'green' : 'gray'"> <span class="dot"></span>{{ scope.row.deployed === '1' ? '已部署' : '未部署' }} </span>
                  <!-- 已产生的流程实例数（含已办结）：有数据时禁止删除模型，避免单据与审批记录被级联删除 -->
                  <span v-if="Number(scope.row.instanceCount) > 0" class="used-tag" :title="'已产生 ' + scope.row.instanceCount + ' 个流程实例（含已办结），该模型不允许删除'">
                    已发起 {{ scope.row.instanceCount }} 单
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'createTime'" label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 170)" resizable align="center" />
          </template>
          <el-table-column label="操作" key="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['flowable:model:edit']">设计</el-button>
                <el-button link type="success" icon="Promotion" @click="handleDeploy(scope.row)" v-hasPermi="['flowable:model:deploy']">部署</el-button>
                <el-button
                  link
                  type="danger"
                  icon="Delete"
                  @click="handleDelete(scope.row)"
                  v-hasPermi="['flowable:model:remove']"
                  :disabled="scope.row.systemFlow"
                  :title="scope.row.systemFlow ? '系统流程已与业务模块关联，不允许删除，只能修改或调整模型' : (Number(scope.row.instanceCount) > 0 ? '该流程已产生 ' + scope.row.instanceCount + ' 个流程实例，不允许删除' : '')"
                  >删除</el-button
                >
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- ===== 新增模型弹窗 ===== -->
    <el-dialog v-model="addOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 19l7-7 3 3-7 7-3-3z" />
              <path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z" />
            </svg>
          </div>
          <span class="rd-detail-header-title">新增流程模型</span>
        </div>
      </template>
      <el-form ref="modelRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="如：请假审批流程" maxlength="50" @blur="onModelNameBlur" />
        </el-form-item>
        <el-form-item label="模型Key" prop="modelKey">
          <el-input v-model="form.modelKey" placeholder="留空自动生成，可手工修改（英文，流程唯一标识）" maxlength="60" />
          <div class="form-tip">用于流程发起时的唯一标识，留空将按模型名称自动生成；部署后不可再修改</div>
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入流程用途描述" maxlength="200" />
        </el-form-item>
        <el-form-item label="关联表单" prop="formId">
          <el-input :model-value="selectedFormName" readonly placeholder="请选择流程表单" style="width: 100%" @click="openFormPicker">
            <template #suffix
              ><el-icon class="picker-suffix-icon" @click="openFormPicker"><Search /></el-icon
            ></template>
          </el-input>
          <div class="form-tip">发起流程时按此表单渲染填写；一个表单可关联多个流程模型</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="addOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 关联表单弹框选择器（开发规范：业务实体选择须用弹框，禁止 el-select） -->
    <form-picker ref="formPickerRef" title="选择流程表单" @confirm="onFormPickerConfirm" />

    <!-- ===== 部署方式弹窗（存为新版 / 覆盖当前版本） ===== -->
    <el-dialog v-model="deployOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Document /></el-icon>
          </div>
          <span class="rd-detail-header-title">部署流程模型</span>
        </div>
      </template>
      <el-form label-width="100px">
        <el-form-item label="模型名称">
          <span class="badge blue"><span class="dot"></span>{{ deployRow?.modelName || '—' }}</span>
        </el-form-item>
        <el-form-item label="结构对比" v-if="deployCheck">
          <div class="deploy-check">
            <el-alert v-if="!deployCheck.deployed" type="info" :closable="false" show-icon title="首次部署：将创建该流程的第 1 个版本" />
            <el-alert
              v-else-if="!deployCheck.structureChanged && !deployCheck.formChanged"
              type="success"
              :closable="false"
              show-icon
              title="流程结构未变更（仅表单字段/必填/只读/名称调整）"
              description="节点表单已实时生效，运行中流程无需重新部署；如需刷新流程名称等可继续部署"
            />
            <el-alert v-else type="warning" :closable="false" show-icon title="流程结构已变更，建议存为新版本（运行中流程继续按原版本走完）">
              <div v-for="(c, i) in deployCheck.changes" :key="i" class="deploy-change-item">· {{ c }}</div>
            </el-alert>
          </div>
        </el-form-item>
        <el-form-item label="部署方式">
          <el-radio-group v-model="deployMode">
            <el-radio value="new">存为新版本（多版本并存）</el-radio>
            <el-radio value="replace">覆盖当前版本（停用旧版本）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="版本备注">
          <el-input v-model="deployRemark" type="textarea" :rows="2" maxlength="200" placeholder="如：调整审批人为部门经理（可留空）" />
        </el-form-item>
        <el-form-item label="设为活动版本">
          <el-switch v-model="deploySetActive" :disabled="deployMode === 'replace'" />
          <span class="form-tip" style="margin-left: 12px">{{ deployMode === 'replace' ? '覆盖模式自动设为活动版本' : '开启后新发起流程优先使用该版本' }}</span>
        </el-form-item>
        <el-form-item label="说明">
          <div class="form-tip" v-if="deployMode === 'new'">每次部署生成一个新版本；新发起流程默认使用最新版本，历史版本可单独启停</div>
          <div class="form-tip" v-else>部署后自动停用所有旧版本；运行中的流程继续按原版本走完，新发起使用新版本</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitDeploy">确认部署</el-button>
        <el-button @click="deployOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 版本清单弹框：版本多了标签行放不下时，从清单里挑 ===== -->
    <el-dialog v-model="versionListOpen" width="780px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 6h16M4 12h16M4 18h10" />
            </svg>
          </div>
          <span class="rd-detail-header-title">版本清单</span>
          <div class="rd-detail-header-sub">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">共 {{ designVersions.length }} 个版本</span>
          </div>
        </div>
      </template>
      <el-table :data="versionRowsDesc" border max-height="420" class="app-table" @row-dblclick="openVersionEditorFromList">
        <el-table-column label="版本" width="110" align="center">
          <template #default="scope">
            <span class="col-strong">V{{ scope.row.version }}</span>
            <span class="vt-badge" style="margin-left: 6px" v-if="scope.row.activeFlag === '1'">当前</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <span class="badge" :class="scope.row.suspended ? 'gray' : 'green'"><span class="dot"></span>{{ scope.row.suspended ? '已挂起' : '已激活' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="版本备注" prop="remark" min-width="160" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column label="部署时间" prop="deploymentTime" width="170" align="center" />
        <el-table-column label="操作" width="230" align="center" class-name="col-action">
          <template #default="scope">
            <el-button link type="primary" icon="EditPen" @click="openVersionEditorFromList(scope.row)" v-hasPermi="['flowable:model:edit']">编辑</el-button>
            <el-button link type="primary" icon="View" @click="openVersionViewFromList(scope.row)" v-hasPermi="['flowable:definition:query']">查看</el-button>
            <el-button link type="primary" icon="RefreshLeft" @click="loadFromList(scope.row)" v-hasPermi="['flowable:model:edit']">载入</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="form-tip" style="float: left; line-height: 32px">双击某行也可直接编辑该版本</span>
        <el-button @click="versionListOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 版本管理编辑弹框（版本号/版本备注/设为活动版本） ===== -->
    <el-dialog v-model="versionOpen" width="480px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
          </div>
          <span class="rd-detail-header-title">版本 V{{ versionEdit.version }}</span>
        </div>
      </template>
      <el-form label-width="100px">
        <el-form-item label="版本号">
          <el-input :model-value="'V' + versionEdit.version" disabled />
        </el-form-item>
        <el-form-item label="版本备注">
          <el-input v-model="versionEdit.remark" type="textarea" :rows="3" maxlength="200" placeholder="如：调整审批人为部门经理（可留空）" />
        </el-form-item>
        <el-form-item label="设为活动版本">
          <el-switch v-model="versionEdit.setActive" :disabled="versionEdit.versionActive || versionEdit.suspended" />
          <span class="form-tip" style="margin-left: 12px">{{
            versionEdit.versionActive ? '已是当前活动版本' : versionEdit.suspended ? '该版本已挂起，请先激活' : '开启后新发起流程优先使用该版本'
          }}</span>
        </el-form-item>
        <el-form-item label="版本状态">
          <span class="badge" :class="versionEdit.suspended ? 'gray' : 'green'"> <span class="dot"></span>{{ versionEdit.suspended ? '已挂起' : '已激活' }} </span>
          <span class="form-tip" style="margin-left: 12px">
            {{ versionEdit.suspended ? '挂起中：该版本不可发起新流程' : '正常：该版本可发起新流程' }}
          </span>
        </el-form-item>
        <el-form-item label="操作">
          <el-button v-if="!versionEdit.suspended" type="warning" plain icon="VideoPause" :loading="versionStateLoading" v-hasPermi="['flowable:model:edit']" @click="handleVersionSuspend(true)"
            >挂起该版本</el-button
          >
          <el-button v-else type="success" plain icon="VideoPlay" :loading="versionStateLoading" v-hasPermi="['flowable:model:edit']" @click="handleVersionSuspend(false)">激活该版本</el-button>
          <div class="form-tip" style="margin-top: 6px">挂起后该版本不可发起新流程，运行中实例不受影响</div>
        </el-form-item>
        <el-form-item label="版本内容">
          <el-button plain icon="View" @click="openVersionView" v-hasPermi="['flowable:definition:query']">查看节点/XML</el-button>
          <el-button plain icon="RefreshLeft" @click="loadVersionIntoDesigner" v-hasPermi="['flowable:model:edit']">载入到设计器</el-button>
          <div class="form-tip" style="margin-top: 6px">载入会把当前画布替换成该版本内容（不自动保存），确认后点右上「存为新版」即生成新版本（会先自动保存草稿）</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitVersionEdit">确 定</el-button>
        <el-button @click="versionOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 历史版本内容查看（只读：节点清单 + BPMN XML） ===== -->
    <el-dialog v-model="versionViewOpen" width="880px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <span class="rd-detail-header-title">版本 V{{ versionView.version }} 内容（只读）</span>
        </div>
      </template>
      <el-tabs v-model="versionViewTab">
        <el-tab-pane label="节点清单" name="nodes">
          <el-table v-loading="versionViewLoading" :data="versionView.nodes" size="small" border max-height="420">
            <el-table-column type="index" label="#" width="56" align="center" />
            <el-table-column prop="name" label="节点名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="id" label="节点ID" min-width="170" show-overflow-tooltip>
              <template #default="scope"><span class="col-mono">{{ scope.row.id }}</span></template>
            </el-table-column>
            <el-table-column prop="config" label="配置摘要" min-width="240" show-overflow-tooltip />
          </el-table>
          <div class="form-tip" v-if="!versionViewLoading && !versionView.nodes.length">该版本没有用户任务节点，或 XML 解析失败</div>
        </el-tab-pane>
        <el-tab-pane label="BPMN XML" name="xml">
          <pre class="version-xml">{{ versionView.xml }}</pre>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="versionViewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 流程设计器弹窗（bpmn-js + 自研属性面板） ===== -->
    <el-dialog v-model="designOpen" fullscreen append-to-body class="designer-dialog" :close-on-click-modal="false" :show-close="false" destroy-on-close @opened="onDesignerOpened">
      <template #header>
        <div class="designer-header">
          <div class="designer-title">
            <span class="glyph"></span>
            流程设计器
            <span class="designer-model-name" :title="designModel.modelName">{{ designModel.modelName }}</span>
            <span class="badge blue"><span class="dot"></span>Key: {{ designModel.modelKey }}</span>
          </div>
          <div class="designer-actions">
            <!-- 流转设置页签的保存由子页签内的「编辑」页承载，顶部保存对该页无意义；
 关闭按钮保留，保证任何页签都有明确退出入口 -->
            <el-button v-if="designTab !== 'flow'" type="primary" icon="Check" @click="saveDesigner()" v-hasPermi="['flowable:model:edit']">保存</el-button>
            <!-- 存为新版：先保存当前设计再部署，生成该流程的新版本（历史版本并存，运行中实例继续按原版本走完） -->
            <el-button type="success" icon="Promotion" @click="deployFromDesigner" v-hasPermi="['flowable:model:deploy']">存为新版</el-button>
            <el-button @click="designOpen = false">关 闭</el-button>
          </div>
        </div>
      </template>
      <!-- 版本标签行：只展示最近若干个版本，更早的收进「版本清单」弹框，避免版本多了撑爆一行 -->
      <div class="version-tab-row" v-if="designVersions.length">
        <span class="version-row-label"
          ><el-icon><Clock /></el-icon> 版本管理</span
        >
        <span
          class="version-tab"
          v-for="v in visibleVersionTabs"
          :key="v.modelId"
          :class="{ 'is-active': designVersionSelected && designVersionSelected.modelId === v.modelId }"
          @click="openVersionEditor(v)"
        >
          V{{ v.version }}
          <span class="vt-badge" v-if="v.activeFlag === '1'">当前</span>
        </span>
        <span v-if="hiddenVersionCount > 0" class="version-more" @click="versionListOpen = true">+{{ hiddenVersionCount }} 个早期版本</span>
        <el-button v-if="designVersions.length" link type="primary" size="small" class="version-all-btn" @click="versionListOpen = true">版本清单</el-button>
        <span class="version-row-tip">点击版本号可编辑备注、设为活动版本</span>
      </div>
      <el-tabs v-model="designTab" class="designer-tabs" :class="{ 'flow-active': designTab === 'flow' }" @tab-change="onDesignTabChange">
        <!-- ===== 基本信息（流程基本信息） ===== -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form label-width="110px" class="design-basic-form">
            <el-form-item label="模型名称" required><el-input v-model="designModel.modelName" placeholder="如：请假审批流程" maxlength="50" /></el-form-item>
            <el-form-item label="模型Key"
              ><el-input :model-value="designModel.modelKey" disabled />
              <div class="form-tip">流程唯一标识，创建后不可修改</div></el-form-item
            >
            <el-form-item label="流程分类"
              ><el-select v-model="designModel.category" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" /> </el-select
            ></el-form-item>
            <el-form-item label="流程描述"><el-input v-model="designModel.description" type="textarea" :rows="3" maxlength="200" placeholder="流程用途说明" /></el-form-item>
            <el-form-item label="关联表单"
              ><el-select v-model="designModel.formId" filterable clearable placeholder="选择流程表单（发起时渲染填写）" style="width: 100%">
                <el-option v-for="f in formOptions" :key="f.formId" :label="f.formName" :value="f.formId" />
              </el-select>
              <div class="form-tip">也可在"表单管理"页签查看表单详情；修改后点击右上"保存"生效</div></el-form-item
            >
            <el-form-item label="跳过相同审批人">
              <el-switch v-model="designModel.skipSameApprover" active-text="开启" inactive-text="关闭" />
              <div class="form-tip">开启后，相邻两个节点办理人为同一人时，后一节点自动跳过；关闭则同一人仍需再次审批。保存后立即生效，进行中的流程从下一节点开始按新设置判断</div>
            </el-form-item>
            <el-form-item label="当前状态">
              <span class="badge" :class="designModel.deployed === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ designModel.deployed === '1' ? '已部署' : '未部署' }}</span>
              <span class="badge blue" style="margin-left: 8px" v-if="designActiveVersion"><span class="dot"></span>当前版本 V{{ designActiveVersion }}</span>
              <span class="badge gray" style="margin-left: 8px" v-else><span class="dot"></span>未设置活动版本</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- ===== 表单管理（基本信息/编辑字段二级页签，表单设计引用组件） ===== -->
        <el-tab-pane label="表单管理" name="form">
          <el-tabs v-model="formTab" type="card" class="form-sub-tabs">
            <el-tab-pane label="基本信息" name="basic">
              <el-form label-width="110px" class="design-basic-form" v-if="designModel.formId">
                <el-form-item label="表单名称"><el-input :model-value="boundFormInfo.formName || '—'" disabled /></el-form-item>
                <el-form-item label="数据库表名"
                  ><el-input :model-value="boundFormInfo.tableName || '未生成'" disabled />
                  <div class="form-tip">表单保存设计后自动生成，此处只读展示</div></el-form-item
                >
                <el-form-item label="表单描述"><el-input :model-value="boundFormInfo.formDesc || '—'" disabled /></el-form-item>
                <el-form-item label="字段数"><el-input :model-value="boundFormInfo.fieldCount != null ? boundFormInfo.fieldCount + ' 个' : '—'" disabled /></el-form-item>
                <el-form-item label="更换表单"
                  ><el-select v-model="designModel.formId" filterable clearable placeholder="选择流程表单" style="width: 100%">
                    <el-option v-for="f in formOptions" :key="f.formId" :label="f.formName" :value="f.formId" />
                  </el-select>
                  <div class="form-tip">更换后点击右上"保存"生效；发起流程时按此表单渲染填写</div></el-form-item
                >
              </el-form>
              <el-form label-width="110px" class="design-basic-form" v-else>
                <el-form-item label="关联表单"
                  ><el-select v-model="designModel.formId" filterable clearable placeholder="当前未关联表单，请选择" style="width: 100%">
                    <el-option v-for="f in formOptions" :key="f.formId" :label="f.formName" :value="f.formId" />
                  </el-select>
                  <div class="form-tip">关联后发起流程时按此表单渲染填写；保存后可在此编辑字段、配置字段权限</div></el-form-item
                >
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="编辑字段" name="fields">
              <FormFieldsEditor v-if="designModel.formId" ref="formFieldsRef" :form-id="designModel.formId" @saved="onFormFieldsSaved" />
              <div v-else class="form-tip" style="padding: 16px 12px">请先在"基本信息"中关联表单并点击右上"保存"后，再编辑字段</div>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
        <!-- ===== 流转设置：仅流程图（打开默认显示流程图，点「编辑流程图」进入流程图编辑页） ===== -->
        <el-tab-pane label="流转设置" name="flow">
          <div class="flow-tabbar">
            <!-- 顶部工具条：左侧标题 + 画布信息，右侧画布动作与主操作。
 旧版是「编 辑」按钮绝对定位浮在画布右上角，页签横线到按钮之间留出一条没有任何内容的空白带，
 看上去像页面没渲染完；改成整条工具条后按钮有了归属，画布也有了明确的上边界。 -->
            <div class="flow-toolbar">
              <div class="ft-left">
                <span class="glyph"></span>
                <span class="ft-title">流程图</span>
                <span class="ft-sub" v-if="flowEmpty">尚未绘制，点右侧按钮开始</span>
                <template v-else>
                  <span class="ft-count">共 {{ flowNodeCount }} 个节点</span>
                  <span class="ft-sub">滚轮缩放 · 拖拽平移</span>
                </template>
              </div>
              <div class="ft-right">
                <el-button v-if="!flowEmpty" size="small" icon="Aim" @click="fitFlowCanvas">适应画布</el-button>
                <el-button size="small" type="primary" icon="EditPen" @click="openGraphicEditor('graph')">{{ flowEmpty ? '开始绘制' : '编辑流程图' }}</el-button>
              </div>
            </div>
            <div class="designer-body">
              <div class="designer-canvas" ref="canvasRef" :class="{ 'canvas-hidden': flowEmpty }"></div>
              <!-- 空状态：模型尚无流程图时，画布中央给出引导与主操作，替代一片空网格 -->
              <div v-if="flowEmpty" class="flow-empty">
                <div class="fe-art">
                  <span class="fe-node fe-start"></span>
                  <span class="fe-line"></span>
                  <span class="fe-node fe-task"></span>
                  <span class="fe-line"></span>
                  <span class="fe-node fe-end"></span>
                </div>
                <div class="fe-title">还没有流程图</div>
                <div class="fe-desc">该流程还没有绘制流转图。从「开始」到「结束」拖拽出审批节点，即可让流程真正跑起来。</div>
                <el-button type="primary" icon="EditPen" @click="openGraphicEditor('graph')">开始绘制流程图</el-button>
                <div class="fe-steps">
                  <span><b>1</b> 拖入节点</span>
                  <span><b>2</b> 连线成流</span>
                  <span><b>3</b> 设置办理人</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <!-- ===== 高级设置（高级功能设置） ===== -->
        <el-tab-pane label="高级设置" name="advanced">
          <el-form label-width="110px" class="design-basic-form">
            <el-form-item label="默认部署方式">
              <el-radio-group v-model="designModel.deployMode">
                <el-radio value="new">存为新版本（多版本并存）</el-radio>
                <el-radio value="replace">覆盖当前版本（停用旧版本）</el-radio>
              </el-radio-group>
              <div class="form-tip">点击列表"部署"时的预选方式；每次部署仍可临时更改</div>
            </el-form-item>
            <el-form-item label="流程锁定">
              <el-switch v-model="designModel.locked" />
              <span class="form-tip" style="margin-left: 12px">锁定后前台不可发起新流程；保存设计时需二次确认，防止误改已上线流程</span>
            </el-form-item>
            <el-form-item label="帮助文档"><el-input v-model="designModel.helpDoc" placeholder="帮助文档链接或说明（可选）" maxlength="200" /></el-form-item>
            <el-form-item label="显示顺序"
              ><el-input-number v-model="designModel.orderNum" :min="0" :max="999" :controls="false" style="width: 160px" />
              <div class="form-tip">数字越小排序越靠前</div></el-form-item
            >
            <!-- 流程编号规则（参照泛微）：段式编号的编辑器较长，收到「流程编号规则」弹框里，
 本页只留一行状态摘要 + 配置入口，避免高级设置页被编号配置撑成一整屏 -->
            <el-form-item label="流程编号">
              <div class="no-rule-row">
                <div class="nr-main">
                  <div class="nr-head">
                    <span class="badge" :class="processNoRuleOn ? 'green' : 'gray'"><span class="dot"></span>{{ processNoRuleOn ? '已启用' : '未启用' }}</span>
                    <span class="nr-segs">{{ processNoRuleOn ? noRuleSegmentsText : '发起流程时不生成流程编号' }}</span>
                  </div>
                  <div class="nr-sub" v-if="processNoRuleOn">
                    {{ noRuleResetLabel }} · {{ designModel.processNoRule.allowEdit ? '允许手工修改' : '不允许手工修改' }}
                  </div>
                </div>
                <el-button size="small" type="primary" plain icon="Setting" @click="openNoRuleDialog">配 置</el-button>
              </div>
              <div class="form-tip">编号由多个段拼装而成，发起流程时自动生成并写入表单「流程编号」字段</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- ===== 流程编号规则弹框：段式编号编辑器（组成/重置周期/手工修改/预览）集中在此设置 =====
      取消即丢弃草稿，确定只写回设计器内存，仍需点右上「保存」落库 -->
      <el-dialog
        v-model="noRuleOpen"
        title="流程编号规则"
        width="780px"
        style="max-width: calc(100vw - 40px)"
        append-to-body
        :close-on-click-modal="false"
        destroy-on-close
        class="no-rule-dialog"
      >
        <el-form label-width="110px" class="design-basic-form nr-dialog-form">
          <el-form-item label="启用流程编号">
            <el-switch v-model="noRuleDraft.enabled" />
            <span class="form-tip" style="margin-left: 12px">开启后发起流程自动生成单号，表单里放「流程编号」字段即可显示（字段本身只读）</span>
          </el-form-item>
          <template v-if="noRuleDraft.enabled">
            <el-form-item label="编号组成">
              <div class="no-seg-editor">
                <div v-for="(s, i) in noRuleDraft.segments" :key="i" class="no-seg-row">
                  <el-select v-model="s.type" style="width: 108px" @change="onSegTypeChange(s)">
                    <el-option label="固定文本" value="text" />
                    <el-option label="日期" value="date" />
                    <el-option label="流水号" value="serial" />
                    <el-option label="表单字段" value="field" />
                  </el-select>
                  <el-input v-if="s.type === 'text'" v-model="s.value" placeholder="如 HT" maxlength="12" style="width: 120px" />
                  <el-select v-else-if="s.type === 'date'" v-model="s.format" style="width: 168px">
                    <el-option label="年（yyyy）" value="yyyy" />
                    <el-option label="年月（yyyyMM）" value="yyyyMM" />
                    <el-option label="年月日（yyyyMMdd）" value="yyyyMMdd" />
                    <el-option label="年月日时分秒" value="yyyyMMddHHmmss" />
                    <el-option label="月日（MMdd）" value="MMdd" />
                  </el-select>
                  <template v-else-if="s.type === 'serial'">
                    <span class="seg-lab">位数</span>
                    <el-input-number v-model="s.len" :min="1" :max="12" :controls="false" style="width: 62px" />
                    <span class="seg-lab">起始</span>
                    <el-input-number v-model="s.start" :min="1" :max="999999999" :controls="false" style="width: 92px" />
                    <span class="seg-lab">步长</span>
                    <el-input-number v-model="s.step" :min="1" :max="1000" :controls="false" style="width: 62px" />
                  </template>
                  <el-select
                    v-else
                    v-model="s.field"
                    filterable
                    clearable
                    placeholder="选择框字段"
                    style="width: 168px"
                    @focus="loadNoRuleFields(designModel.formId)"
                    @change="onSegFieldChange(s)"
                  >
                    <el-option v-for="f in noRuleFieldOptions" :key="f.field" :label="f.label" :value="f.field" />
                  </el-select>
                  <!-- 选择框字段：每个选项值配一个代字，编号里用代字，没配的用原值 -->
                  <div v-if="s.type === 'field' && s.field" class="no-code-box">
                    <span class="seg-lab">代字</span>
                    <div v-if="fieldOptions(s.field).length" class="no-code-list">
                      <span v-for="o in fieldOptions(s.field)" :key="o.value" class="no-code-item">
                        <span class="no-code-label" :title="o.value">{{ o.label }}</span>
                        <el-input v-model="s.codes[o.value]" size="small" maxlength="12" placeholder="留空用原值" style="width: 104px" />
                      </span>
                    </div>
                    <span v-else class="seg-lab">该选择框字段还没配选项</span>
                  </div>
                  <span class="seg-ops">
                    <el-button link icon="Top" :disabled="i === 0" title="上移" @click="moveSeg(i, -1)" />
                    <el-button link icon="Bottom" :disabled="i === noRuleDraft.segments.length - 1" title="下移" @click="moveSeg(i, 1)" />
                    <el-button
                      link
                      type="danger"
                      icon="Delete"
                      :disabled="noRuleDraft.segments.length <= 1"
                      title="删除该段"
                      @click="noRuleDraft.segments.splice(i, 1)"
                    />
                  </span>
                </div>
                <el-button size="small" plain icon="Plus" @click="addSeg">添加编号段</el-button>
                <div class="form-tip">
                  段按先后顺序直接拼接，要分隔符就加一个内容为「-」的固定文本段；流水号段只认第一个；表单字段只列下拉框类字段
                </div>
                <!-- 实时预览：就在段编辑下面，改任意一段立刻能看到最终单号长什么样 -->
                <div class="no-preview-card">
                  <span class="np-lab">编号预览</span>
                  <span class="np-value">{{ noPreviewText || '—' }}</span>
                  <span class="np-tip">{{ noPreviewTip }}</span>
                  <el-button size="small" link icon="Refresh" @click="loadNoPreview(true)">刷新</el-button>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="重置周期">
              <el-select v-model="noRuleDraft.reset" style="width: 220px">
                <el-option label="不重置（按流程一直累计）" value="none" />
                <el-option label="每年重置" value="year" />
                <el-option label="每季度重置" value="quarter" />
                <el-option label="每月重置" value="month" />
                <el-option label="每日重置" value="day" />
              </el-select>
              <div class="form-tip">流水号进入新周期后从「起始编号」重新开始</div>
            </el-form-item>
            <el-form-item label="允许手工修改">
              <el-switch v-model="noRuleDraft.allowEdit" />
              <span class="form-tip" style="margin-left: 12px">开启后发起人可在发起页改编号；与已有单号重复时自动改回规则生成的号</span>
            </el-form-item>
          </template>
        </el-form>
        <template #footer>
          <div class="nr-dialog-footer">
            <span class="nr-footer-tip">确定后仍需点击设计器右上「保存」，规则才会写入流程配置</span>
            <div class="nr-footer-btns">
              <el-button @click="noRuleOpen = false">取 消</el-button>
              <el-button type="primary" @click="confirmNoRule">确 定</el-button>
            </div>
          </div>
        </template>
      </el-dialog>
      <input ref="xmlFileRef" type="file" accept=".xml,.bpmn" style="display: none" @change="onXmlFileChange" />
    </el-dialog>

    <!-- ===== 图形化编辑独立全屏弹窗（点"编辑"进入独立编辑页，关闭返回列表） =====
     :show-close="false"：页头/画布工具条右侧已有「关闭」，右上角再挂一个 el-dialog 自带的 ×
     就是两个含义相同的关闭入口，且 × 与深蓝页头贴在一起显得拥挤，故隐藏。 -->
    <el-dialog v-model="graphicOpen" fullscreen append-to-body class="designer-dialog" :show-close="false" :close-on-click-modal="false" destroy-on-close @opened="onGraphicOpened">
      <template #header>
        <div class="designer-header">
          <div class="designer-title">
            <span class="glyph"></span>
            流程图编辑
            <span class="designer-model-name">{{ designModel.modelName }}</span>
            <span class="mode-badge">{{ graphicModeLabel }}</span>
            <span class="badge blue"><span class="dot"></span>Key: {{ designModel.modelKey }}</span>
          </div>
          <!-- 图形模式的工具栏已移到画布上方的浅色工具条（参考流程设计器）；节点/出口模式仍用头部按钮 -->
          <div class="designer-actions" v-if="graphicMode !== 'graph'">
            <el-button type="primary" icon="Check" @click="graphicSave" v-hasPermi="['flowable:model:edit']">保存</el-button>
            <el-button @click="graphicClose">关 闭</el-button>
          </div>
        </div>
      </template>
      <div class="designer-body graphic-body">
        <!-- 图形编辑工具栏：浅色扁平按钮条，参考流程设计器（简洁、按组分区） -->
        <div v-if="graphicMode === 'graph'" class="graphic-toolbar">
          <div class="gt-group">
            <el-button link icon="RefreshLeft" @click="graphicCmd('undo')">撤销</el-button>
            <el-button link icon="RefreshRight" @click="graphicCmd('redo')">重做</el-button>
            <el-button link icon="Delete" :disabled="!graphicHasSelection" @click="graphicDelete">删除</el-button>
          </div>
          <span class="gt-divider"></span>
          <div class="gt-group">
            <el-button link icon="ZoomIn" @click="onGraphicZoomCmd('in')">放大</el-button>
            <el-button link icon="ZoomOut" @click="onGraphicZoomCmd('out')">缩小</el-button>
            <el-button link icon="FullScreen" @click="onGraphicZoomCmd('100')">还原</el-button>
          </div>
          <span class="gt-divider"></span>
          <div class="gt-group">
            <el-button link icon="Upload" @click="onGraphicMoreCmd('import')">导入</el-button>
            <el-button link icon="Download" @click="onGraphicMoreCmd('export')">导出</el-button>
            <el-button link icon="QuestionFilled" @click="onGraphicMoreCmd('help')">说明</el-button>
          </div>
          <div class="gt-right">
            <span class="gt-zoom">{{ graphicZoomPct }}%</span>
            <el-button type="primary" size="small" icon="Check" @click="graphicSave" v-hasPermi="['flowable:model:edit']">保存</el-button>
            <el-button size="small" @click="graphicClose">关闭</el-button>
          </div>
        </div>
        <!-- 单一模式编辑：一次只编辑一类（图形 / 节点 / 出口）。
 画布容器仅在图形模式渲染；节点/出口模式不建实例、不做视口适配，
 从根本上避免零尺寸容器上 fit-viewport 算出非有限值（SVGMatrix scale 报错）。 -->
        <div v-if="graphicMode === 'graph'" class="graphic-pane">
          <div class="palette-host" ref="paletteHostRef" v-show="!paletteCollapsed"></div>
          <div class="graphic-main">
            <!-- 收起/展开左侧节点面板：悬浮在画布左边缘，收起后画布铺满便于查看完整流程图 -->
            <button type="button" class="palette-toggle" :class="{ 'is-collapsed': paletteCollapsed }" @click="togglePalette"
              :title="paletteCollapsed ? '展开节点面板' : '收起节点面板，查看完整流程图'">
              <el-icon><ArrowLeft v-if="!paletteCollapsed" /><ArrowRight v-else /></el-icon>
              <span>{{ paletteCollapsed ? '展开' : '收起' }}</span>
            </button>
            <div class="designer-canvas" ref="graphicCanvasRef"></div>
            <div class="props-handle" v-if="selectedElement" @click="propsCollapsed = !propsCollapsed" :title="propsCollapsed ? '展开节点信息' : '收起节点信息'">
              <span>{{ propsCollapsed ? '‹' : '›' }}</span>
            </div>
            <div class="designer-props" v-if="selectedElement && !propsCollapsed">
              <template v-if="selectedElement">
                <div class="props-title">
                  <span class="glyph"></span> 节点信息
                  <el-tag size="small" effect="plain" style="margin-left: 8px">{{ selectedElement.type.replace('bpmn:', '') }}</el-tag>
                </div>
                <el-form label-width="86px" size="small" class="props-form">
                  <el-form-item :label="selectedElement.isSequenceFlow ? '连线名称' : '节点名称'">
                    <el-input
                      v-model="selectedElement.name"
                      :placeholder="selectedElement.isSequenceFlow ? '如：3天以上' : '如：部门经理审批'"
                      @change="updateElementProp('name', selectedElement.name)"
                    />
                  </el-form-item>

                  <!-- 可设置节点（申请节点 / 用户任务 / 结束节点）：节点信息行（操作人员 / 表单内容 / 操作菜单） -->
                  <template v-if="isConfigurableNode(selectedElement)">
                    <el-form-item label="操作人员">
                      <div class="props-link-row">
                        <el-button link type="primary" class="props-link" :title="participantSummaryText" @click="openParticipantPicker">{{ participantSummaryText }}</el-button>
                        <el-button link type="primary" icon="Setting" class="props-link-gear" title="设置操作人员" @click="openParticipantPicker" />
                      </div>
                      <div class="props-tip">
                        {{ selectedElement.isStart ? '申请节点默认由流程发起人办理，可在此改为指定人员/组织角色/表单字段' : '支持流程发起人、上一活动办理人、组织/用户/岗位、表单字段' }}
                      </div>
                    </el-form-item>
                    <el-form-item label="表单内容">
                      <div class="props-link-row">
                        <el-button link type="primary" class="props-link" :title="nodeFormText(selectedElement)" @click="openNodeForm(selectedElement)">{{ nodeFormText(selectedElement) }}</el-button>
                        <el-button link type="primary" icon="Setting" class="props-link-gear" title="配置节点表单" @click="openNodeForm(selectedElement)" />
                      </div>
                      <div class="props-tip">每个节点可设置独立表单、表单样式与字段必填</div>
                    </el-form-item>
                    <el-form-item v-if="!selectedElement.isEnd" label="打印模板">
                      <div class="props-link-row">
                        <el-button link type="primary" class="props-link" :title="printTemplateText(selectedElement)" @click="openPrintTemplate(selectedElement)">{{
                          printTemplateText(selectedElement)
                        }}</el-button>
                        <el-button link type="primary" icon="Setting" class="props-link-gear" title="配置打印模板" @click="openPrintTemplate(selectedElement)" />
                      </div>
                      <div class="props-tip">办理页「打印」使用的模板；未配置时自动用该节点的显示模板</div>
                    </el-form-item>
                    <el-form-item v-if="selectedElement.isUserTask" label="签字意见">
                      <div class="props-link-row">
                        <el-switch v-model="selectedElement.signOpinion" inline-prompt active-text="启用" inactive-text="关闭" @change="setSignOpinion" />
                        <el-button v-if="!selectedElement.isStart" link type="primary" class="props-link-gear" title="签字意见详细设置（必填 / 默认意见）" @click="openNodeOpMenu(selectedElement)">
                          <el-icon><Setting /></el-icon>
                        </el-button>
                      </div>
                      <div class="props-tip">默认启用；必填、默认意见等详细设置在「操作菜单」弹窗底部。表单里已放「签字意见」字段时不会重复显示</div>
                    </el-form-item>
                    <el-form-item v-if="!selectedElement.isEnd" label="操作菜单">
                      <div class="props-link-row">
                        <el-button link type="primary" class="props-link" :title="nodeOpMenuText(selectedElement)" @click="openNodeOpMenu(selectedElement)">{{
                          nodeOpMenuText(selectedElement)
                        }}</el-button>
                        <el-button link type="primary" icon="Setting" class="props-link-gear" title="配置操作菜单" @click="openNodeOpMenu(selectedElement)" />
                      </div>
                      <div class="props-tip">
                        {{
                          selectedElement.isStart
                            ? '申请节点只保留发起场景按钮（提交 / 打印 / 抄送）'
                            : '可启用/停用按钮、自定义按钮名称、配置默认审批意见与签字意见（必填 / 默认意见）'
                        }}
                      </div>
                    </el-form-item>
                    <el-form-item v-if="!selectedElement.isEnd" label="强制收回">
                      <div class="props-link-row">
                        <el-switch v-model="selectedElement.allowRecall" inline-prompt active-text="允许" inactive-text="关闭" @change="setAllowRecall" />
                      </div>
                      <div class="props-tip">默认允许；下一节点办理前，本节点办理人可在「待办流程-已办」收回流程，回到本节点重新办理</div>
                    </el-form-item>
                    <el-form-item v-if="selectedElement.isUserTask" label="抄送人员">
                      <div class="props-link-row">
                        <el-button link type="primary" class="props-link" :title="ccSummaryText" @click="openParticipantPicker('cc')">{{ ccSummaryText }}</el-button>
                        <el-button link type="primary" icon="Setting" class="props-link-gear" title="设置抄送人员" @click="openParticipantPicker('cc')" />
                      </div>
                      <div class="props-tip">节点到达 / 办理完成后自动抄送，抄送人在「待办流程-待阅」查看</div>
                    </el-form-item>
                  </template>

                  <!-- 分支条件（网关出口）：选「字段 + 运算符 + 值」自动拼表达式，不再手写 JUEL -->
                  <div v-if="selectedElement.isSequenceFlow" class="cond-section">
                    <div class="cond-head">
                      <span class="cond-head-title">分支条件</span>
                      <el-switch v-model="condAdvanced" size="small" inline-prompt active-text="表达式" inactive-text="配置" @change="onCondModeChange" />
                    </div>

                    <!-- 默认出口：兜底分支，不参与条件判断（仅排他/包容网关有效，同一网关只能有一条） -->
                    <div v-if="selectedElement.defaultFlowSupported" class="cond-default">
                      <div class="cond-default-row">
                        <span class="cond-default-label">默认出口</span>
                        <el-switch
                          v-model="selectedElement.isDefaultFlow"
                          size="small"
                          inline-prompt
                          active-text="默认"
                          inactive-text="非默认"
                          @change="setDefaultFlow"
                        />
                      </div>
                      <div class="props-tip">默认出口不参与条件判断：所有条件都不成立时走这里；同一网关只能有一条，设为默认会清空该出口的条件。</div>
                    </div>

                    <template v-if="!condAdvanced">
                      <div v-if="!condFieldOptions.length" class="props-tip">该流程未绑定表单字段，可切到「表达式」手写条件</div>
                      <template v-else>
                        <div v-for="(row, idx) in condRows" :key="idx" class="cond-row">
                          <div class="cond-line">
                            <el-select v-if="idx > 0" v-model="condJoin" size="small" class="cond-join" @change="syncCondition">
                              <el-option label="并且" value="AND" />
                              <el-option label="或者" value="OR" />
                            </el-select>
                            <span v-else class="cond-when">当</span>
                            <el-select v-model="row.field" size="small" class="cond-field" filterable placeholder="选择字段" @change="onCondFieldChange(row)">
                              <el-option v-for="f in condFieldOptions" :key="f.field" :label="f.label" :value="f.field">
                                <span class="cond-opt-label">{{ f.label }}</span>
                                <span class="cond-opt-field">{{ f.field }}</span>
                              </el-option>
                            </el-select>
                            <el-button link type="danger" size="small" class="cond-del" :disabled="condRows.length <= 1" title="删除这条条件" @click="removeCondRow(idx)">
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </div>
                          <div class="cond-line">
                            <el-select v-model="row.op" size="small" class="cond-op" @change="onCondOpChange(row)">
                              <el-option v-for="o in condOpsFor(row)" :key="o.value" :label="o.label" :value="o.value" />
                            </el-select>
                            <span v-if="!condNeedsValue(row)" class="cond-novalue">无需填写比较值</span>
                            <el-select
                              v-else-if="condValueOptions(row).length"
                              v-model="row.value"
                              size="small"
                              class="cond-value"
                              filterable
                              allow-create
                              default-first-option
                              :multiple="isMultiValueOp(row.op)"
                              collapse-tags
                              placeholder="选择值"
                              @change="syncCondition"
                            >
                              <el-option v-for="o in condValueOptions(row)" :key="o.value" :label="o.label" :value="o.value" />
                            </el-select>
                            <el-date-picker
                              v-else-if="condValueKind(row) === 'date'"
                              v-model="row.value"
                              size="small"
                              class="cond-value"
                              type="date"
                              value-format="YYYY-MM-DD"
                              placeholder="选择日期"
                              @change="syncCondition"
                            />
                            <el-time-picker
                              v-else-if="condValueKind(row) === 'time'"
                              v-model="row.value"
                              size="small"
                              class="cond-value"
                              value-format="HH:mm:ss"
                              format="HH:mm"
                              placeholder="选择时间"
                              @change="syncCondition"
                            />
                            <el-input-number
                              v-else-if="condValueKind(row) === 'number'"
                              v-model="row.value"
                              size="small"
                              class="cond-value"
                              controls-position="right"
                              placeholder="数值"
                              @change="syncCondition"
                            />
                            <el-input
                              v-else
                              v-model="row.value"
                              size="small"
                              class="cond-value"
                              :placeholder="isMultiValueOp(row.op) ? '多个值用逗号分隔' : '填写比较值'"
                              @change="syncCondition"
                            />
                          </div>
                        </div>
                        <div class="cond-actions">
                          <el-button link type="primary" size="small" @click="addCondRow">
                            <el-icon><Plus /></el-icon> 添加条件
                          </el-button>
                          <el-button link size="small" :disabled="!condRows.some((r) => r.field)" @click="clearCondition">清空条件</el-button>
                        </div>
                      </template>
                      <div class="cond-preview">
                        <span class="cond-preview-label">表达式</span>
                        <code>{{ condExpr || '未设置（这条出口无条件，按顺序兜底）' }}</code>
                      </div>
                    </template>

                    <template v-else>
                      <el-input v-model="selectedElement.condition" type="textarea" :rows="3" placeholder="如：${days > 3}" @change="updateCondition" />
                    </template>

                    <div class="props-tip">
                      {{ selectedElement.sourceIsGateway ? '当前连线位于网关出口，按出口顺序逐个判断，条件成立即走该分支。' : '提示：仅排他/包容网关出口的连线条件生效。' }}
                    </div>
                  </div>
                </el-form>
              </template>
              <div class="props-help">
                <div class="props-help-title">使用提示</div>
                <ul>
                  <li>本页只编辑图形：从左侧节点面板拖拽节点、连线，选中后右侧配置属性</li>
                  <li>节点清单、出口信息请回流转设置，各自点"编 辑"单独进入</li>
                  <li>点顶部"保存"同步回设计器画布并保存到系统；"关闭"不保存未提交的改动</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <!-- 设置操作人员：处理方式 + 办理方式 + 参与者模式 + 关系对象 -->
        <el-dialog v-model="participantOpen" width="1032px" append-to-body draggable class="rd-dialog" destroy-on-close :close-on-click-modal="false">
          <template #header>
            <div class="rd-detail-header">
              <div class="rd-detail-header-icon">
                <el-icon><User /></el-icon>
              </div>
              <span class="rd-detail-header-title">{{ participantDraft.scope === 'cc' ? '设置抄送人员' : '设置操作人员' }}</span>
              <div class="rd-detail-header-sub" v-if="selectedElement">
                <div class="rd-detail-header-divider"></div>
                <span class="rd-detail-header-no">{{ selectedElement.name || '当前节点' }}</span>
              </div>
            </div>
          </template>
          <el-form v-if="selectedElement" label-width="96px" size="small" class="pc-form">
            <div class="pc-section">
              <div class="pc-section-head">
                <span class="pc-section-title">{{ participantDraft.scope === 'cc' ? '抄送规则' : '办理规则' }}</span>
                <span class="pc-section-desc">{{ participantDraft.scope === 'cc' ? '设置抄送时机与接收人' : '先决定任务怎么派，再选择具体操作人' }}</span>
              </div>
              <el-form-item v-if="participantDraft.scope === 'cc'" label="抄送时机">
                <el-radio-group v-model="participantDraft.timing" @change="onCcTimingChange">
                  <el-radio value="enter">节点到达时</el-radio>
                  <el-radio value="complete">办理完成后</el-radio>
                  <el-radio value="both">两者都抄送</el-radio>
                </el-radio-group>
                <div class="props-tip">抄送人在「待办流程-待阅」中查看流程详情并标记已读</div>
              </el-form-item>
              <template v-else>
                <el-form-item label="处理方式">
                  <el-radio-group v-model="selectedElement.handleMode" size="small" @change="onHandleModeChange">
                    <el-radio-button value="assign">指定</el-radio-button>
                    <el-radio-button value="runtime" :disabled="selectedElement.performMode !== 'claim'">提交时指定</el-radio-button>
                  </el-radio-group>
                  <div class="pc-mode-tip">
                    <span class="pc-mode-badge">{{ currentHandleModeLabel }}</span>
                    <span>{{ handleModeTip }}</span>
                  </div>
                  <div v-if="selectedElement.performMode === 'claim' && selectedElement.handleMode === 'runtime'" class="pc-mode-compare">
                    <span><b>提交时指定</b>：上一环节提交时从范围内指定 1 人办理</span>
                  </div>
                </el-form-item>
                <el-form-item v-if="selectedElement.isUserTask" label="办理方式">
                  <el-radio-group v-model="selectedElement.performMode" size="small" @change="onPerformModeChange">
                    <el-radio-button value="and">会签</el-radio-button>
                    <el-radio-button value="seq">串签</el-radio-button>
                    <el-radio-button value="claim">抢占</el-radio-button>
                  </el-radio-group>
                  <div class="pc-mode-tip">
                    <span class="pc-mode-badge">{{ currentPerformModeLabel }}</span>
                    <span>{{ performModeTip }}</span>
                  </div>
                </el-form-item>
              </template>
            </div>
            <div class="pc-section">
              <div class="pc-section-head">
                <span class="pc-section-title">{{ participantDraft.scope === 'cc' ? '抄送范围' : '操作人范围' }}</span>
                <span class="pc-section-desc">先选关系对象，再选择具体对象或直接添加</span>
              </div>
              <el-form-item :label="participantDraft.scope === 'cc' ? '抄送对象' : '关系对象'">
                <div class="pc-rel-groups">
                  <el-radio-group v-model="participantDraft.relCategory" class="pc-rel-tabs" @change="onRelCategoryChange">
                    <el-radio-button v-for="g in RELATION_GROUPS" :key="g.key" :value="g.key">{{ g.label }}</el-radio-button>
                  </el-radio-group>
                  <div class="pc-rel-panel">
                    <span class="pc-rel-group-hint">{{ pcRelationHint }}</span>
                    <el-radio-group v-model="participantDraft.relType" class="pc-rel-radios" @change="onRelTypeChange">
                      <el-radio v-for="r in pcRelationOptions" :key="r.type" :value="r.type" border size="small" class="pc-rel-chip">{{ r.label }}</el-radio>
                    </el-radio-group>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="选择对象">
              <div class="pc-pick-row">
                <template v-if="pcPickerKind === 'user'">
                  <el-button size="small" icon="User" @click="openPcUserPicker">选择人员</el-button>
                </template>
                <template v-else-if="pcPickerKind === 'dept'">
                  <el-button size="small" icon="OfficeBuilding" @click="openPcDeptPicker">选择部门</el-button>
                </template>
                <template v-else-if="pcPickerKind === 'role'">
                  <el-button size="small" icon="Avatar" @click="openPcRolePicker">选择角色</el-button>
                </template>
                <template v-else-if="pcPickerKind === 'post'">
                  <el-button size="small" icon="Postcard" @click="openPcPostPicker">选择岗位</el-button>
                </template>
                <template v-else-if="pcPickerKind === 'node'">
                  <el-select v-model="participantDraft.nodeId" filterable clearable placeholder="选择节点" style="width: 200px">
                    <el-option v-for="n in participantNodeOptions" :key="n.id" :label="n.optionLabel || n.name || n.id" :value="n.id" />
                  </el-select>
                  <el-radio-group v-model="participantDraft.nodeRelation">
                    <el-radio value="self">{{ pcNodeSelfLabel }}</el-radio>
                    <el-radio value="leader">{{ pcNodeLeaderLabel }}</el-radio>
                    <el-radio value="chargeLeader">{{ pcNodeChargeLeaderLabel }}</el-radio>
                  </el-radio-group>
                </template>
                <template v-else-if="pcPickerKind === 'field'">
                  <el-select v-model="participantDraft.fieldKey" filterable clearable :placeholder="'选择' + (pcFieldWantDept ? '部门' : '人员') + '字段'" style="width: 260px">
                    <el-option v-for="f in pcFieldOptions" :key="f.field" :label="f.label + '（' + f.field + '）'" :value="f.field" />
                  </el-select>
                </template>
                <span v-else class="pc-all-users">全部启用人员，无需选择对象</span>
                <span v-if="pcPickerKind !== 'node' && pcPickerKind !== 'field' && pcPickerKind !== 'none'" class="pc-picked">{{ pcPickedText }}</span>
                <el-button size="small" type="primary" plain @click="addParticipantFromDraft">添加到已选</el-button>
              </div>
              <div v-if="pcPickerKind === 'field'" class="props-tip">
                {{ pcFieldOptions.length ? pcFieldValueHint : '当前表单没有' + (pcFieldWantDept ? '部门' : '人员') + '类型的浏览字段，请先在表单中配置' }}
              </div>
              <div v-else-if="pcPickerKind === 'node'" class="props-tip">
                <template v-if="participantDraft.nodeId === PREV_NODE_ID">运行期取最近一个已完成任务的办理人（首个审批环节回退为发起人），跟着流程实际走到哪一步，不用指定具体节点</template>
                <template v-else>取该节点最近一次办理人；选择「申请节点」时取流程发起人（用于按发起人及其领导派单）；流程未流转到该节点时无法解析</template>
              </div>
              <div v-else-if="pcPickerKind === 'none'" class="props-tip">「所有人」覆盖全部启用人员，后续新增人员也会自动纳入范围</div>
              </el-form-item>
            </div>

            <div class="pc-section">
              <div class="pc-section-head">
                <span class="pc-section-title">{{ participantDraft.scope === 'cc' ? '已选抄送人' : '已选操作人' }}</span>
                <span v-if="participantDraft.items.length" class="pc-section-count">{{ participantDraft.items.length }} 条</span>
                <span class="pc-section-desc" style="margin-left: auto">
                  {{ participantDraft.scope === 'cc'
                    ? '多条取并集，合并去重后全部抄送'
                    : '多条取并集，不是二选一：指定+会签/串签每人一个任务，指定+抢占多人进候选池，提交时指定由上一环节从中选 1 人' }}
                </span>
              </div>
              <div class="pc-selected-body">
              <el-table v-if="participantDraft.items.length" :data="participantDraft.items" size="small" border class="pc-items-table">
                <el-table-column label="关系" min-width="160">
                  <template #default="s">{{ relationLabel(s.row) }}</template>
                </el-table-column>
                <el-table-column label="对象" min-width="200" show-overflow-tooltip>
                  <template #default="s">{{ participantNameText(s.row) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="70" align="center">
                  <template #default="s">
                    <el-button link type="danger" size="small" @click="participantDraft.items.splice(s.$index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
                <span v-else class="pc-empty">尚未添加，先在上方选择关系对象并「添加到已选」</span>
              </div>
            </div>

            <div v-if="participantDraft.scope !== 'cc'" class="pc-section">
              <div class="pc-section-head">
                <span class="pc-section-title">兜底设置</span>
                <span class="pc-section-desc">解析不到人时转交，避免节点无人可办、会签被跳过</span>
              </div>
              <el-form-item label="兜底办理人">
              <div class="pc-pick-row">
                <el-button size="small" icon="User" @click="openPcFallbackPicker">选择兜底办理人</el-button>
                <span class="pc-picked">{{ participantDraft.fallbackNames.length ? participantDraft.fallbackNames.join('、') : '未设置（解析不到人时转流程管理员）' }}</span>
                <el-button v-if="participantDraft.fallbackIds.length" link type="danger" size="small" @click="clearPcFallback">清除</el-button>
              </div>
              </el-form-item>
            </div>
          </el-form>
          <template #footer>
            <el-button size="small" @click="participantOpen = false">取消</el-button>
            <el-button size="small" type="primary" :loading="participantSaving" @click="saveParticipantConfig">保存</el-button>
          </template>
        </el-dialog>

        <!-- 操作人员选择器：人员 / 部门 / 角色 / 岗位（统一弹窗组件） -->
        <user-picker ref="pcUserPickerRef" :multiple="true" title="选择人员" @confirm="onPcUsersPicked" />
        <user-picker ref="pcFallbackPickerRef" title="选择兜底办理人" @confirm="onPcFallbackPicked" />
        <dept-picker ref="pcDeptPickerRef" :multiple="pcDeptMultiple" title="选择部门" @confirm="onPcDeptsPicked" />
        <role-picker ref="pcRolePickerRef" title="选择角色" @confirm="onPcRolesPicked" />
        <position-picker ref="pcPositionPickerRef" :multiple="true" title="选择岗位" @confirm="onPcPostsPicked" />

        <!-- 节点信息：节点清单维护（独立模式下只出现本区块） -->
        <div v-if="graphicMode === 'nodes'" class="graphic-single-pane">
          <div class="batch-toolbar">
            <el-button-group>
              <el-button size="small" icon="Plus" @click="openAddNode" title="新增节点" />
              <el-button
                size="small"
                icon="Minus"
                :disabled="!graphicCheckedNodes.length"
                @click="removeGraphicNodes"
                :title="graphicCheckedNodes.length ? '删除勾选节点（已选 ' + graphicCheckedNodes.length + ' 个）' : '请先勾选要删除的节点'"
              />
            </el-button-group>
            <span class="field-tip">勾选行后点「−」批量删除；新增节点在选中节点后追加；修改实时写入流程图，点"保存"生效</span>
          </div>
          <el-table
            ref="nodeTableRef"
            :data="graphicNodes"
            row-key="id"
            border
            size="small"
            max-height="420"
            class="field-table"
            highlight-current-row
            @current-change="onGraphicNodeSelect"
            @selection-change="onNodeSelectionChange"
          >
            <el-table-column type="selection" width="46" align="center" reserve-selection :selectable="isNodeSelectable" />
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column label="节点名称" min-width="150" align="center">
              <template #default="scope"><el-input v-model="scope.row.name" placeholder="如：部门审批" size="small" @change="updateNodeProp(scope.row, 'name', scope.row.name)" /></template>
            </el-table-column>
            <el-table-column label="节点类型" width="150" align="center">
              <template #default="scope">
                <el-select v-if="isTypeChangeable(scope.row)" v-model="scope.row.type" size="small" style="width: 100%" @change="changeNodeType(scope.row)">
                  <el-option v-for="t in ADD_NODE_TYPES" :key="t.value" :label="t.label" :value="t.value" />
                </el-select>
                <span v-else class="badge blue"><span class="dot"></span>{{ scope.row.typeLabel }}</span>
              </template>
            </el-table-column>
            <el-table-column label="分支语义" width="120" align="center">
              <template #default="scope">
                <span v-if="scope.row.isUserTask || isGatewayType(scope.row.type)">{{ branchRoleLabel(scope.row) }}</span>
                <span v-else style="color: #cbd5e1">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作者（办理人）" min-width="180" align="center">
              <template #default="scope">
                <el-select
                  v-if="isConfigurableNode(scope.row)"
                  v-model="scope.row.assignee"
                  placeholder="未设置"
                  clearable
                  filterable
                  allow-create
                  size="small"
                  style="width: 100%"
                  @change="updateNodeAssignee(scope.row)"
                >
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="String(u.userId)" />
                </el-select>
                <span v-else style="color: #cbd5e1">—</span>
              </template>
            </el-table-column>
            <el-table-column label="节点表单" min-width="150" align="center">
              <template #default="scope">
                <el-select
                  v-if="scope.row.isUserTask"
                  v-model="scope.row.formKey"
                  placeholder="Html模式"
                  clearable
                  filterable
                  allow-create
                  size="small"
                  style="width: 100%"
                  @change="updateNodeFormKey(scope.row)"
                >
                  <el-option v-for="f in formOptions" :key="f.formId" :label="f.formName" :value="String(f.formId)" />
                </el-select>
                <span v-else-if="isConfigurableNode(scope.row)" style="color: #64748b">{{ nodeFormText(scope.row) }}</span>
                <span v-else style="color: #cbd5e1">—</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <!-- 出口信息：连线与出口条件维护（独立模式下只出现本区块） -->
        <div v-if="graphicMode === 'exits'" class="graphic-single-pane">
          <div class="batch-toolbar">
            <el-button-group>
              <el-button size="small" icon="Plus" @click="openAddLine" title="新增出口" />
              <el-button size="small" icon="Minus" :disabled="!graphicSelectedLine" @click="removeGraphicLine" title="删除选中出口" />
            </el-button-group>
            <el-select v-model="exitFilterNodeId" placeholder="******请选择当前节点******" clearable filterable size="small" style="width: 220px; margin-left: 12px" @change="onExitFilterChange">
              <el-option v-for="n in graphicNodes" :key="n.id" :label="n.name || n.id" :value="n.id" />
            </el-select>
            <span class="field-tip">选择当前节点后仅显示该节点的出口；↑↓调整同源出口顺序即网关判断优先级；点"保存"生效</span>
          </div>
          <el-table :data="filteredGraphicExits" border size="small" max-height="420" class="field-table" highlight-current-row row-key="id" ref="exitTableRef" @current-change="onGraphicLineSelect">
            <el-table-column label="" width="100" align="center">
              <template #default="scope">
                <span class="drag-handle" title="拖动调整同源出口顺序（即网关判断优先级）">⋮⋮</span>
                <el-button link type="primary" size="small" :disabled="!canMoveExit(scope.row, -1)" @click="moveExitOrder(scope.row, -1)" title="上移（提高流转优先级）">↑</el-button>
                <el-button link type="primary" size="small" :disabled="!canMoveExit(scope.row, 1)" @click="moveExitOrder(scope.row, 1)" title="下移（降低流转优先级）">↓</el-button>
              </template>
            </el-table-column>
            <el-table-column label="源节点" min-width="110" align="center"
              ><template #default="scope">{{ scope.row.sourceName || scope.row.sourceId }}</template></el-table-column
            >
            <el-table-column label="目标节点" min-width="110" align="center"
              ><template #default="scope">{{ scope.row.targetName || scope.row.targetId }}</template></el-table-column
            >
            <el-table-column label="出口名称" min-width="120" align="center">
              <template #default="scope"><el-input v-model="scope.row.name" placeholder="如：3天以上" size="small" @change="updateNodeProp(scope.row, 'name', scope.row.name)" /></template>
            </el-table-column>
            <el-table-column label="是否退回" width="80" align="center">
              <template #default="scope">
                <el-checkbox :model-value="!!scope.row.isReturn" @change="(val) => updateExitMeta(scope.row, 'isReturn', val)" />
              </template>
            </el-table-column>
            <el-table-column label="出口条件" width="120" align="center">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="openExitRules(scope.row)">
                  <el-icon><Setting /></el-icon>
                  <el-icon v-if="scope.row.condition" style="color: #22c55e; margin-left: 2px"><Check /></el-icon>
                </el-button>
              </template>
            </el-table-column>
            <el-table-column label="附加规则" width="90" align="center">
              <template #default="scope">
                <el-input v-model="scope.row.extraRule" placeholder="规则编号" size="small" @change="(val) => updateExitMeta(scope.row, 'extraRule', val)" />
              </template>
            </el-table-column>
            <el-table-column label="生成编号" width="80" align="center">
              <template #default="scope">
                <el-checkbox :model-value="!!scope.row.genNo" @change="(val) => updateExitMeta(scope.row, 'genNo', val)" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <input ref="graphicFileRef" type="file" accept=".xml,.bpmn" style="display: none" @change="onGraphicFileChange" />
    </el-dialog>

    <!-- 新增节点弹框（编辑页面：在选中节点后追加） -->
    <el-dialog v-model="addNodeOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Pointer /></el-icon>
          </div>
          <span class="rd-detail-header-title">新增节点</span>
        </div>
      </template>
      <el-form label-width="90px" size="small">
        <el-form-item label="节点类型">
          <el-select v-model="addNodeForm.type" style="width: 100%">
            <el-option v-for="t in ADD_NODE_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分支方式" v-if="addNodeForm.type === 'bpmn:ExclusiveGateway'">
          <el-select v-model="addNodeForm.branchMode" style="width: 100%">
            <el-option v-for="b in ADD_BRANCH_MODES" :key="b.value" :label="b.label" :value="b.value" />
          </el-select>
          <div class="form-tip">分支方式仅决定落成的网关类型，出口条件仍可回车在画布/出口信息中调整</div>
        </el-form-item>
        <el-form-item label="节点名称"><el-input v-model="addNodeForm.name" placeholder="如：总经理审批（可留空）" maxlength="30" /></el-form-item>
        <el-form-item label="来源节点"><el-input :model-value="graphicSelectedRow ? graphicSelectedRow.name || graphicSelectedRow.id : '—'" disabled /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAddNode">确 定</el-button>
        <el-button @click="addNodeOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 新增出口弹框（编辑页面：来源→目标连线） -->
    <el-dialog v-model="addLineOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><ArrowDown /></el-icon>
          </div>
          <span class="rd-detail-header-title">新增出口</span>
        </div>
      </template>
      <el-form label-width="90px" size="small">
        <el-form-item label="来源节点">
          <el-select v-model="addLineForm.sourceId" filterable placeholder="选择来源节点" style="width: 100%">
            <el-option v-for="n in graphicNodes" :key="n.id" :label="(n.name || n.id) + '（' + n.typeLabel + '）'" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标节点">
          <el-select v-model="addLineForm.targetId" filterable placeholder="选择目标节点" style="width: 100%">
            <el-option v-for="n in graphicNodes" :key="n.id" :label="(n.name || n.id) + '（' + n.typeLabel + '）'" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出口名称"><el-input v-model="addLineForm.name" placeholder="如：3天以上（可留空）" maxlength="30" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAddLine">确 定</el-button>
        <el-button @click="addLineOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 节点表单配置弹窗（每个节点可单独指定/设计表单） ===== -->
    <el-dialog v-model="nodeFormOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Document /></el-icon>
          </div>
          <span class="rd-detail-header-title">节点表单：{{ nodeFormDraft.nodeName }}</span>
        </div>
      </template>
      <el-form label-width="96px" size="small">
        <el-form-item label="节点表单">
          <div class="node-form-row">
            <el-input
              :model-value="nodeFormDraft.formId ? nodeFormName : '流程表单：' + (flowFormName || '未关联')"
              readonly
              class="node-form-pick"
              placeholder="点击选择表单"
              title="点击选择表单"
              @click="openNodeFormPicker"
            >
              <template #suffix>
                <el-icon v-if="nodeFormDraft.formId" class="node-form-rename" title="修改模板名称" @click.stop="renameNodeForm"><EditPen /></el-icon>
              </template>
            </el-input>
            <el-button :icon="Plus" @click="createNodeForm">新增</el-button>
          </div>
          <div class="props-tip">
            <template v-if="nodeFormDraft.isNodeForm">当前为节点独立表单，仅作用于本节点；可继续在「表单设计」调整样式与必填</template>
            <template v-else>未设置时使用数据表默认表单；点击名称可引用默认表单或其他节点已设置的表单，或点「新增」创建本节点表单</template>
          </div>
          <!-- 引用模板后的命名说明：名称未修改时保存会自动改成「节点名称+表单」，避免与其他节点模板重名 -->
          <div v-if="nodeFormDraft.isNodeForm && nodeFormDraft.refFormName" class="node-form-ref-tip">
            已引用模板「{{ nodeFormDraft.refFormName }}」<template v-if="nodeFormDraft.fromNodeName">（来自节点「{{ nodeFormDraft.fromNodeName }}」）</template>；
            <template v-if="nodeFormDraft.nameDirty">已自定义名称为「{{ nodeFormDraft.nodeFormName }}」，保存后按该名称生效</template>
            <template v-else>名称未修改，保存时将自动命名为「{{ autoNodeFormName }}」，避免与其他节点模板重名</template>
          </div>
          <el-button v-if="nodeFormDraft.formId" link type="primary" class="node-form-reset" @click="useFlowForm">恢复使用流程表单</el-button>
        </el-form-item>
        <el-form-item label="表单设计">
          <el-button size="small" type="primary" plain :icon="EditPen" :disabled="!nodeDesignFormId && !nodeFormDraft.isNodeForm" @click="openNodeFormDesigner">打开表单设计</el-button>
          <div class="props-tip">在「页面设计」调整表单样式，在「编辑字段」设置字段必填/非必填</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveNodeForm">确 定</el-button>
        <el-button @click="nodeFormOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 选择节点表单：本流程流程表单 + 其他节点已设置的表单 -->
    <el-dialog v-model="nodeFormPickOpen" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Document /></el-icon>
          </div>
          <span class="rd-detail-header-title">选择节点表单</span>
        </div>
      </template>
      <div class="node-form-pick">
        <div class="node-form-pick-search">
          <el-input v-model="nodeFormPickKeyword" placeholder="表单名称" clearable size="small" style="width: 220px">
            <template #prefix
              ><el-icon><Search /></el-icon
            ></template>
          </el-input>
        </div>
        <el-empty v-if="!filteredNodeFormOptions.length" :description="nodeFormPickKeyword ? '未找到匹配的表单' : '暂无可引用的表单'" :image-size="72" />
        <el-table
          v-else
          :data="filteredNodeFormOptions"
          height="300"
          size="small"
          highlight-current-row
          @row-click="(row) => (nodeFormPickId = row.key)"
          @row-dblclick="
            (row) => {
              nodeFormPickId = row.key;
              confirmNodeFormPick();
            }
          "
        >
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="nodeFormPickId" :value="row.key" @click.stop="nodeFormPickId = row.key"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="表单名称" prop="formName" min-width="220" show-overflow-tooltip />
          <el-table-column label="来源" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ row.fromNodeNames.length ? '节点：' + row.fromNodeNames.join('、') : '数据表默认表单' }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="nodeFormPickOpen = false">取 消</el-button>
        <el-button type="primary" :disabled="!nodeFormPickId" @click="confirmNodeFormPick">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 节点表单设计（整页只有页面设计，字段维护通过「表单编辑」弹窗；保存/关闭在顶部） -->
    <el-dialog
      v-model="nodeFormDesignOpen"
      width="1180px"
      :fullscreen="nodeDesignFullscreen"
      append-to-body
      draggable
      class="rd-dialog node-design-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><EditPen /></el-icon>
          </div>
          <span class="rd-detail-header-title">页面设计：{{ nodeDesignForm.formName || '未选择表单' }}</span>
          <!-- 保存/关闭放在顶部，底部完整留给设计器 -->
          <div class="node-design-head-btns">
            <el-button @click="nodeFormDesignOpen = false">关 闭</el-button>
            <el-button type="primary" :loading="nodeDesignNameSaving" @click="saveNodeDesignAll">保 存</el-button>
          </div>
        </div>
      </template>
      <div v-loading="nodeDesignLoading" class="node-form-design-wrap" :class="{ 'is-fullscreen': nodeDesignFullscreen }">
        <el-empty v-if="!nodeDesignFormId" description="请先选择节点表单" :image-size="72" />
        <template v-else>
          <!-- 字段定义只有一份（表单管理里的表单），节点只维护自己的显示布局；
               模板名称与「表单编辑」入口放在右侧字段面板顶部，画布尽量宽 -->
          <ExcelGridDesigner
            v-if="nodeDesignForm.grid"
            :model-value="nodeDesignForm.grid"
            :fields="nodeDesignForm.fields"
            :form="{ formName: nodeDesignForm.formName, formKey: nodeDesignForm.formKey }"
            :active="nodeFormDesignOpen"
            :fullscreen="nodeDesignFullscreen"
            hide-save
            head-slot
            @update:model-value="(v) => (nodeDesignForm.grid = v)"
            @save="saveNodeDesignAll"
            @toggle-fullscreen="nodeDesignFullscreen = !nodeDesignFullscreen"
          >
            <template #panel-head>
              <div class="ndh-wrap">
                <div class="ndh-title">节点模板名称</div>
                <el-input v-model="nodeDesignForm.formName" size="small" maxlength="50" placeholder="请输入模板名称" />
                <div class="ndh-actions">
                  <el-button size="small" type="primary" plain icon="EditPen" :disabled="!nodeDesignForm.formId" @click="openNodeFieldEditor">表单编辑</el-button>
                  <el-button v-if="nodeUnplacedFields.length" size="small" plain icon="Plus" @click="fillUnplacedNodeFields">补齐字段（{{ nodeUnplacedFields.length }}）</el-button>
                </div>
                <div v-if="nodeFormDraft.isNodeForm && nodeFormDraft.refFormName && !nodeFormDraft.nameDirty" class="ndh-tip">未改名称将自动命名为「{{ autoNodeFormName }}」</div>
              </div>
            </template>
          </ExcelGridDesigner>
        </template>
      </div>
    </el-dialog>

    <!-- 节点表单「表单编辑」：临时增删字段/调整字段（与表单管理共用同一份字段定义） -->
    <el-dialog v-model="nodeFieldEditOpen" width="1080px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false" destroy-on-close :before-close="handleNodeFieldEditorClose">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Document /></el-icon>
          </div>
          <span class="rd-detail-header-title">表单编辑：{{ nodeDesignForm.formName || '未选择表单' }}</span>
        </div>
      </template>
      <div v-loading="nodeDesignLoading" class="node-field-edit-wrap">
        <FormFieldsEditor v-if="nodeFieldEditOpen && nodeDesignForm.formId" ref="nodeFieldsRef" :form-id="nodeDesignForm.formId" @saved="onNodeFieldsSaved" />
        <el-empty v-else description="请先在节点表单里选择表单" :image-size="72" />
      </div>
      <template #footer>
        <div class="node-field-edit-footer">
          <span class="field-tip">字段定义与「表单管理」共用一份，保存后所有引用该表单的节点实时同步</span>
          <el-button @click="handleNodeFieldEditorClose()">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 节点操作菜单配置弹窗（对标泛微：启用 / 自定义名称 / 默认审批意见 + 签字意见设置） ===== -->
    <el-dialog v-model="opMenuOpen" width="700px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Setting /></el-icon>
          </div>
          <span class="rd-detail-header-title">操作菜单：{{ opMenuDraft.nodeName }}</span>
        </div>
      </template>
      <div class="props-tip" style="margin-bottom: 10px">
        {{ opMenuDraft.isStart ? '申请节点只提供发起场景按钮（提交 / 打印 / 抄送）' : '设置该节点办理页显示的操作按钮：可启用/停用、自定义按钮名称、配置点击按钮时预填的默认审批意见' }}
      </div>
      <el-table :data="opMenuDraft.rows" size="small" class="op-menu-table" border>
        <el-table-column label="启用" width="60" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.enabled" />
          </template>
        </el-table-column>
        <el-table-column label="默认名称" width="100" align="center">
          <template #default="{ row }">{{ row.label }}</template>
        </el-table-column>
        <el-table-column label="自定义名称" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.name" size="small" maxlength="10" :placeholder="row.label" :disabled="!row.enabled" @input="row.name = row.name.replace(/\s/g, '')" />
          </template>
        </el-table-column>
        <el-table-column label="默认审批意见" min-width="170">
          <template #default="{ row }">
            <el-input
              v-if="row.hasOpinion"
              v-model="row.defaultOpinion"
              size="small"
              maxlength="100"
              placeholder="点击该按钮时预填的意见"
              :disabled="!row.enabled"
            />
            <span v-else class="op-menu-na">—</span>
          </template>
        </el-table-column>
      </el-table>
      <!-- 签字意见设置（申请节点无独立意见框设置，仅审批节点提供） -->
      <div v-if="!opMenuDraft.isStart" class="op-menu-sign">
        <div class="op-menu-sign-title">
          <span class="op-menu-sign-name">签字意见设置</span>
          <span class="op-menu-sign-tip">控制该节点办理页签字意见框的显示与填写要求</span>
        </div>
        <div class="op-menu-sign-row">
          <el-checkbox v-model="opMenuDraft.sign.enabled">启用签字意见</el-checkbox>
          <el-checkbox v-model="opMenuDraft.sign.required" :disabled="!opMenuDraft.sign.enabled">意见必填</el-checkbox>
        </div>
        <el-input
          v-model="opMenuDraft.sign.defaultValue"
          size="small"
          maxlength="100"
          placeholder="默认签字意见：进入办理页时自动预填（可留空）"
          :disabled="!opMenuDraft.sign.enabled"
        />
      </div>
      <template #footer>
        <el-button size="small" @click="resetOpMenuDefault">恢复默认</el-button>
        <el-button size="small" @click="opMenuOpen = false">取 消</el-button>
        <el-button size="small" type="primary" @click="saveNodeOpMenu">确 定</el-button>
      </template>
    </el-dialog>

    <!-- ===== 出口条件规则配置弹窗（参照出口条件设置：规则列表 + AND/OR + 规则引用） ===== -->
    <el-dialog v-model="exitRulesOpen" width="760px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="rd-detail-header-title">出口条件设置（{{ exitRulesRow ? exitRulesRow.name || exitRulesRow.id : '' }}）</div>
        </div>
      </template>
      <div class="batch-toolbar" style="margin-bottom: 10px">
        <span class="rules-label">规则间关系：</span>
        <el-radio-group v-model="exitRulesForm.join" size="small">
          <el-radio-button value="AND">AND</el-radio-button>
          <el-radio-button value="OR">OR</el-radio-button>
        </el-radio-group>
        <el-button size="small" type="primary" icon="Plus" style="margin-left: 12px" @click="addRuleRow">添加规则</el-button>
        <el-button size="small" icon="Minus" :disabled="exitRulesForm.rows.length <= 1" @click="removeRuleRow">删除规则</el-button>
        <span class="field-tip" style="margin-left: 12px">多条规则按 AND/OR 组合为一个 JUEL 表达式写入出口条件</span>
      </div>
      <el-table :data="exitRulesForm.rows" border size="small" max-height="300" class="field-table">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="字段（表单字段/流程变量）" min-width="170" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.field" placeholder="选择字段" size="small" filterable allow-create default-first-option style="width: 100%" @change="onRuleFieldChange(scope.row)">
              <el-option v-for="f in condFieldOptions" :key="f.field" :label="f.label + '（' + f.field + '）'" :value="f.field" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="运算符" width="120" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.op" size="small" style="width: 100%" @change="onRuleOpChange(scope.row)">
              <el-option v-for="o in condOpsFor(scope.row)" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="值" min-width="190" align="center">
          <template #default="scope">
            <span v-if="!condNeedsValue(scope.row)" class="cond-novalue">无需填写</span>
            <el-select
              v-else-if="condValueOptions(scope.row).length"
              v-model="scope.row.value"
              size="small"
              style="width: 100%"
              filterable
              allow-create
              default-first-option
              :multiple="isMultiValueOp(scope.row.op)"
              collapse-tags
              placeholder="选择值"
            >
              <el-option v-for="o in condValueOptions(scope.row)" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-date-picker
              v-else-if="condValueKind(scope.row) === 'date'"
              v-model="scope.row.value"
              size="small"
              style="width: 100%"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
            />
            <el-time-picker
              v-else-if="condValueKind(scope.row) === 'time'"
              v-model="scope.row.value"
              size="small"
              style="width: 100%"
              value-format="HH:mm:ss"
              format="HH:mm"
              placeholder="选择时间"
            />
            <el-input-number v-else-if="condValueKind(scope.row) === 'number'" v-model="scope.row.value" size="small" controls-position="right" placeholder="数值" />
            <el-input v-else v-model="scope.row.value" :placeholder="isMultiValueOp(scope.row.op) ? '多个值用逗号分隔' : '填写比较值'" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="条件预览" min-width="180" align="center">
          <template #default="scope"
            ><span style="font-family: monospace">{{ ruleToExpr(scope.row) || '—' }}</span></template
          >
        </el-table-column>
      </el-table>
      <div class="batch-toolbar" style="margin-top: 10px">
        <span class="rules-label">从其他出口引用规则：</span>
        <el-select v-model="exitRulesCopyFrom" placeholder="选择已配置条件的出口" clearable filterable size="small" style="width: 240px">
          <el-option v-for="e in otherRuleExits" :key="e.id" :label="(e.name || e.id) + '（' + (e.condition || '') + '）'" :value="e.id" />
        </el-select>
        <el-button size="small" style="margin-left: 8px" @click="applyCopyExitRules" :disabled="!exitRulesCopyFrom">复 制</el-button>
      </div>
      <div class="batch-toolbar" style="margin-top: 6px">
        <span class="rules-label">最终表达式：</span>
        <span style="font-family: monospace; color: #2563eb">{{ exitRulesExpr || '（空）' }}</span>
      </div>
      <template #footer>
        <el-button type="primary" @click="saveExitRules">保 存</el-button>
        <el-button @click="clearExitRules">清除条件</el-button>
        <el-button @click="exitRulesOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 节点编辑操作组弹窗（参照「编辑操作组」：8 种操作者类型 + 多组操作者 + 条件/批次/会签） ===== -->
    <el-dialog v-model="opGroupsOpen" width="820px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="rd-detail-header-title">编辑操作组（{{ opGroupsNodeName }}）</div>
        </div>
      </template>
      <div class="batch-toolbar" style="margin-bottom: 10px">
        <el-button size="small" type="primary" icon="Plus" @click="addOperatorGroup">添加操作者设置</el-button>
        <span class="field-tip">一个节点可配置多组操作者；同一批次操作者并行办理，跨批次按顺序串行；点"保存"生效</span>
      </div>
      <el-collapse v-model="opGroupsActiveNames">
        <el-collapse-item v-for="(g, gi) in opGroupsForm" :key="gi" :name="String(gi)">
          <template #title>
            <span style="font-weight: 600">操作组 {{ gi + 1 }}</span>
            <span style="margin-left: 8px; font-size: 12px; color: #64748b">{{ opGroupTitle(g) }}</span>
            <el-button link type="danger" size="small" style="margin-left: 12px" @click.stop="removeOperatorGroup(gi)">删除操作组</el-button>
          </template>
          <el-form label-width="86px" size="small" class="props-form">
            <el-form-item label="操作组名称">
              <el-input v-model="g.name" placeholder="如：财务确认" maxlength="30" style="width: 220px" />
            </el-form-item>
            <el-form-item label="操作者类型">
              <el-radio-group v-model="g.type">
                <el-radio v-for="t in OPERATOR_TYPES" :key="t.value" :value="t.value" style="margin-right: 8px">{{ t.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="g.type === 'user'" label="指定人">
              <el-select
                v-model="g.userId"
                placeholder="选择用户"
                filterable
                clearable
                style="width: 100%"
                @change="
                  (val) => {
                    g.refName = opUserLabel(val);
                  }
                "
              >
                <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="String(u.userId)" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="g.type === 'role'" label="指定角色">
              <el-select
                v-model="g.roleId"
                placeholder="选择角色"
                filterable
                clearable
                style="width: 100%"
                @change="
                  (val) => {
                    g.refName = opRoleLabel(val);
                  }
                "
              >
                <el-option v-for="r in roleOptions" :key="r.roleId" :label="r.roleName + '（' + r.roleKey + '）'" :value="String(r.roleId)" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="g.type === 'dept' || g.type === 'subDept'" :label="opTypeLabel(g.type)">
              <el-tree-select
                v-model="g.refId"
                :data="deptTreeOptions"
                :props="{ label: 'label', children: 'children' }"
                node-key="id"
                value-key="id"
                check-strictly
                filterable
                placeholder="选择部门"
                clearable
                style="width: 100%"
                @node-click="
                  () => {
                    g.refName = opDeptLabel(g.refId);
                  }
                "
                @change="
                  (val) => {
                    g.refName = opDeptLabel(val);
                  }
                "
              />
            </el-form-item>
            <el-form-item v-if="g.type === 'post'" label="指定岗位">
              <el-select
                v-model="g.refId"
                placeholder="选择岗位"
                filterable
                clearable
                style="width: 100%"
                @change="
                  (val) => {
                    g.refName = opPostLabel(val);
                  }
                "
              >
                <el-option v-for="p in postOptions" :key="p.postId" :label="p.postName + '（' + p.postCode + '）'" :value="String(p.postId)" />
              </el-select>
            </el-form-item>
            <el-form-item label="办理方式">
              <el-select
                v-model="g.counterSign"
                style="width: 180px"
                @change="
                  (val) => {
                    g.counterSign = val;
                  }
                "
              >
                <el-option label="单人办理" value="single" />
                <el-option label="或签（一人通过即通过）" value="or" />
                <el-option label="会签（全部通过才通过）" value="and" />
              </el-select>
            </el-form-item>
            <el-form-item label="条件">
              <el-input v-model="g.condition" type="textarea" :rows="2" placeholder="JUEL 表达式，如：${days > 3}（可留空表示无条件）" />
              <div class="props-tip">满足条件时本组操作者参与办理；留空表示始终参与</div>
            </el-form-item>
            <el-form-item label="批次">
              <el-input-number v-model="g.batch" :min="0" :max="99" :controls="false" style="width: 120px" />
              <div class="props-tip">同批次并行、跨批次按批次号升序串行</div>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
      <div v-if="!opGroupsForm.length" class="props-empty">
        <p>暂无操作组，点击上方「添加操作者设置」新增</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="saveOperatorGroups">保 存</el-button>
        <el-button @click="opGroupsOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 节点字段权限配置弹窗 ===== -->
    <el-dialog v-model="fieldPermsOpen" width="560px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          </div>
          <span class="rd-detail-header-title">字段权限：{{ fieldPermsNodeName }}</span>
        </div>
      </template>
      <div class="field-perms-tip">控制该审批节点办理人在任务审批页看到的表单字段：可编辑 / 只读 / 隐藏。未配置的字段默认只读展示。</div>
      <div class="field-perms-copybar" v-if="otherPermNodes.length">
        <span class="fpc-label">复制其他节点：</span>
        <el-select v-model="copyPermFrom" size="small" placeholder="选择已配置权限的节点" style="width: 220px">
          <el-option v-for="n in otherPermNodes" :key="n.id" :label="n.name + '（' + n.summary + '）'" :value="n.id" />
        </el-select>
        <el-button size="small" type="primary" plain :disabled="!copyPermFrom" @click="applyCopyPerms">复制</el-button>
      </div>
      <el-table :data="fieldPermsRows" border size="small" max-height="360" class="field-perms-table" v-loading="fieldPermsLoading">
        <el-table-column type="index" label="序号" key="序号" width="55" align="center" />
        <el-table-column label="字段" min-width="150">
          <template #default="s"
            ><span class="col-strong">{{ s.row.label }}</span
            ><em class="fp-field">{{ s.row.field }}</em></template
          >
        </el-table-column>
        <el-table-column label="权限" width="260" align="center">
          <template #default="s">
            <el-radio-group v-model="fieldPermsRows[s.$index].perm" size="small">
              <el-radio-button value="edit">可编辑</el-radio-button>
              <el-radio-button value="readonly">只读</el-radio-button>
              <el-radio-button value="hidden">隐藏</el-radio-button>
            </el-radio-group>
          </template>
        </el-table-column>
      </el-table>
      <div class="field-perms-empty" v-if="!fieldPermsLoading && !fieldPermsRows.length">当前流程模型未绑定表单，请先在"基本信息"中关联表单并保存后再配置字段权限</div>
      <template #footer>
        <el-button type="primary" @click="saveFieldPerms">确 定</el-button>
        <el-button @click="fieldPermsOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 节点打印模板（打印模板可按节点配置） ===== -->
    <el-dialog v-model="printTplOpen" width="560px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <el-icon><Printer /></el-icon>
          </div>
          <span class="rd-detail-header-title">打印模板：{{ printTplDraft.nodeName || '当前节点' }}</span>
        </div>
      </template>
      <el-form label-width="96px" size="small">
        <el-form-item label="打印方式">
          <el-radio-group v-model="printTplDraft.mode">
            <el-radio value="display">跟随当前节点显示模板</el-radio>
            <el-radio value="form">使用指定表单模板</el-radio>
          </el-radio-group>
          <div class="props-tip">选「跟随」时，办理页打印效果与屏幕上看到的表单一致</div>
        </el-form-item>
        <el-form-item label="打印模板" v-if="printTplDraft.mode === 'form'">
          <el-select v-model="printTplDraft.formId" filterable placeholder="选择表单库中的表单" style="width: 100%">
            <el-option v-for="f in formOptions" :key="f.formId" :label="f.formName" :value="String(f.formId)" />
          </el-select>
          <div class="props-tip">保存时会快照该表单的字段与布局，之后表单库改版不影响本节点打印</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="printTplSaving" @click="savePrintTemplate">保 存</el-button>
        <el-button @click="printTplOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowModel">
import { listModel, getModel, addModel, updateModel, delModel, deployModel, listDefinition, getDefinitionXml, updateDefinitionRemark, setActiveDefinition, changeDefinitionState } from '@/api/flowable/flow';
import { allForms, getForm } from '@/api/flowable/form';
import { listNodePrints, saveNodePrint, delNodePrint } from '@/api/flowable/nodePrint';
import FormPicker from '@/components/FormPicker/index.vue';
import UserPicker from '@/components/UserPicker/index.vue';
import DeptPicker from '@/components/DeptPicker/index.vue';
import RolePicker from '@/components/RolePicker/index.vue';
import PositionPicker from '@/components/PositionPicker/index.vue';
import request from '@/utils/request';
import { isPrimaryButton, hasSecondaryModifier } from 'diagram-js/lib/util/Mouse';
import { normalizeDiagramSizes, DEFAULT_EVENT_SIZE } from '@/utils/bpmnSizes';
import { ElMessageBox } from 'element-plus';
import { h, nextTick, ref, computed, watch, reactive, toRefs, onMounted, onBeforeUnmount } from 'vue';
import {
  Search,
  Filter,
  Document,
  Pointer,
  RefreshLeft,
  RefreshRight,
  ZoomIn,
  ZoomOut,
  FullScreen,
  Setting,
  Check,
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  MoreFilled,
  QuestionFilled,
  Clock,
  User,
  EditPen,
  Plus,
  Delete,
} from '@element-plus/icons-vue';
import Sortable from 'sortablejs';
import { useColumnResize } from '@/composables/useColumnResize';
import { useColumnDrag } from '@/composables/useColumnDrag';
import FormFieldsEditor from '../form/components/FormFieldsEditor.vue';
import ExcelGridDesigner from '../form/designer/ExcelGridDesigner.vue';
import { parseLayout, fieldsToGrid, serializeLayout, isFlowSysField } from '../form/designer/converters.js';
import { listNodeForms, saveNodeForm as saveNodeFormApi, delNodeForm } from '@/api/flowable/nodeForm';
import { rtypeOf, optionListOf } from '@/components/FormGridControl';
import { deployCheckModel } from '@/api/flowable/flow';
import { useDetailCard } from '@/composables/useDetailCard';

const { proxy } = getCurrentInstance();
const { flowable_category } = proxy.useDict('flowable_category');
const { collapsedCards, toggleCard } = useDetailCard(['v1']);
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_model_index');

const list = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const addOpen = ref(false);
const designOpen = ref(false);
const designModel = ref({});

/**
 * 流程编号规则默认值（参照泛微）：编号 = 若干段直接拼接，不再单独配「分隔符」，
 * 需要分隔符就加一个内容为「-」的固定文本段（位置、个数都能自己控）
 */
function defaultNoRule() {
  return {
    enabled: true,
    reset: 'year',
    allowEdit: false,
    segments: [
      { type: 'text', value: '' },
      { type: 'date', format: 'yyyyMMdd' },
      { type: 'text', value: '-' },
      { type: 'serial', len: 4, start: 1, step: 1 },
    ],
  };
}
/** 旧版规则带 sep（段间分隔符）：折算成段与段之间的固定文本段，保证历史编号格式不变 */
function withSepSegments(list, sep) {
  if (!sep || !list || list.length < 2) return list || [];
  const out = [];
  list.forEach((s, i) => {
    if (i) out.push({ type: 'text', value: String(sep) });
    out.push(s);
  });
  return out;
}
const NO_SEG_TYPES = ['text', 'date', 'serial', 'field'];
/** 单段兜底：字段缺失/类型非法时补默认值，避免模板里出现 undefined */
function sanitizeSeg(s) {
  const t = s && NO_SEG_TYPES.includes(s.type) ? s.type : 'text';
  if (t === 'text') return { type: 'text', value: s && s.value != null ? String(s.value) : '' };
  if (t === 'date') return { type: 'date', format: (s && s.format) || 'yyyyMMdd' };
  if (t === 'serial')
    return {
      type: 'serial',
      len: Math.min(12, Math.max(1, Number(s && s.len) || 4)),
      start: Math.max(1, Number(s && s.start) || 1),
      step: Math.max(1, Number(s && s.step) || 1),
    };
  return {
    type: 'field',
    field: (s && s.field) || '',
    // 选项值 → 代字，如 { "研发部": "YF" }；没配的选项用原值
    codes: s && s.codes && typeof s.codes === 'object' ? { ...s.codes } : {},
  };
}
/** 解析后端返回的规则 JSON：新版 segments 直接归一化，旧版 prefix/dateFmt/seqLen 自动折算 */
function parseNoRule(raw) {
  const base = defaultNoRule();
  if (!raw) return base;
  let o;
  try {
    o = typeof raw === 'string' ? JSON.parse(raw) : raw;
  } catch (e) {
    return base;
  }
  if (!o || typeof o !== 'object') return base;
  const rule = {
    enabled: o.enabled !== false,
    reset: o.reset || base.reset,
    allowEdit: !!o.allowEdit,
    segments: [],
  };
  if (Array.isArray(o.segments) && o.segments.length) {
    rule.segments = o.segments.filter((s) => s && s.type).map(sanitizeSeg);
  } else {
    // 旧版三段式：老模型打开就是新界面，不丢配置
    if (o.prefix && String(o.prefix).trim()) rule.segments.push({ type: 'text', value: String(o.prefix).trim() });
    if (o.dateFmt && o.dateFmt !== 'none') rule.segments.push({ type: 'date', format: o.dateFmt });
    rule.segments.push({ type: 'serial', len: Math.min(12, Math.max(1, Number(o.seqLen) || 4)), start: 1, step: 1 });
  }
  if (!rule.segments.length) rule.segments = base.segments;
  // 老规则里的 sep 折算成固定文本段（迁移一次，之后保存就是新结构）
  if (o.sep) rule.segments = withSepSegments(rule.segments, o.sep);
  return rule;
}
/**
 * 流程编号规则弹框：编辑器较长（编号段/重置周期/手工修改/预览），不再平铺在高级设置页。
 * 弹框内编辑的是草稿 noRuleDraft，取消丢弃；确定才写回 designModel（仍需点右上「保存」落库）
 */
const NO_RESET_LABELS = { none: '不重置', year: '每年重置', quarter: '每季度重置', month: '每月重置', day: '每日重置' };
const noRuleOpen = ref(false);
const noRuleDraft = ref(defaultNoRule());
const processNoRuleOn = computed(() => !!(designModel.value.processNoRule || {}).enabled);
const noRuleResetLabel = computed(() => NO_RESET_LABELS[(designModel.value.processNoRule || {}).reset] || '不重置');
/** 高级设置页的编号规则摘要：一眼看出「由哪些段拼成」 */
const noRuleSegmentsText = computed(() => {
  const segs = (designModel.value.processNoRule || {}).segments || [];
  const parts = segs.map((s) => {
    if (s.type === 'text') return s.value ? '「' + s.value + '」' : '空文本';
    if (s.type === 'date') return '日期(' + (s.format || 'yyyyMMdd') + ')';
    if (s.type === 'serial') return '流水号(' + (Number(s.len) || 4) + '位)';
    return '字段(' + (s.field || '未选') + ')';
  });
  return parts.length ? parts.join(' + ') : '未配置编号段';
});
function openNoRuleDialog() {
  // 深拷贝：弹框里改的只是草稿，取消不污染已保存的规则
  noRuleDraft.value = JSON.parse(JSON.stringify(designModel.value.processNoRule || defaultNoRule()));
  if (!Array.isArray(noRuleDraft.value.segments) || !noRuleDraft.value.segments.length)
    noRuleDraft.value.segments = JSON.parse(JSON.stringify(defaultNoRule().segments));
  // 预览状态重置：重新打开时算的是「当前草稿」的号，不复用上一次的结果
  noPreviewText.value = '';
  noPreviewTip.value = '';
  noPreviewSeq.value = 0;
  lastNoPreviewKey = '';
  noRuleOpen.value = true;
  loadNoRuleFields(designModel.value.formId);
  if (noRuleDraft.value.enabled) loadNoPreview(true);
}
function confirmNoRule() {
  designModel.value.processNoRule = JSON.parse(JSON.stringify(noRuleDraft.value));
  noRuleOpen.value = false;
  proxy.$modal.msgSuccess('编号规则已更新，点击右上「保存」后生效');
}
/** 表单字段段的可选字段（主表字段），打开下拉时按当前关联表单加载 */
const noRuleFieldOptions = ref([]);
function noRuleFieldLabel(field) {
  const f = noRuleFieldOptions.value.find((x) => x.field === field);
  return f ? f.label : field;
}
async function loadNoRuleFields(formId) {
  if (!formId || noRuleFieldOptions.value.length) return;
  try {
    const res = await getForm(formId);
    const fields = JSON.parse((res.data && res.data.fieldsJson) || '[]');
    // 只有「下拉框类选择框」字段才能进编号：值的取值范围确定，才能逐项配代字
    noRuleFieldOptions.value = (fields || []).filter(
      (f) => f && f.field && f.form === 'select' && (!f.table || f.table === 'main'),
    );
    // 选项到位后补一次预览：算样例值时还没选项，字段段会先显示成 [字段名]
    if (noRuleOpen.value && noPreviewText.value.includes('[')) {
      lastNoPreviewKey = '';
      loadNoPreview(true);
    }
  } catch (e) {
    noRuleFieldOptions.value = [];
  }
}
/** 选择框字段的选项列表：[{label, value}]（兼容旧版字符串数组） */
function fieldOptions(field) {
  const f = noRuleFieldOptions.value.find((x) => x.field === field);
  const list = (f && f.selectOptions) || [];
  return list.map((o) =>
    o && typeof o === 'object'
      ? { label: o.label != null ? String(o.label) : String(o.value), value: o.value != null ? String(o.value) : String(o.label) }
      : { label: String(o), value: String(o) },
  );
}
/** 切换字段段所选字段：代字是跟选项走的，换字段就清空重配 */
function onSegFieldChange(s) {
  s.codes = {};
  loadNoRuleFields(designModel.value.formId);
}
/** 编号预览：优先用后端「真实下一号」（不消耗流水号），接口不可用时按规则本地估算 */
const noPreviewText = ref('');
const noPreviewTip = ref('');
const noPreviewSeq = ref(0);
let noPreviewTimer = null;
// 上一次真正发过请求的规则快照：打开弹窗时「初始化预览」与「规则 watch」会各触发一次，
// 两次相同请求间隔在 1 秒内会被前端防重提交拦截（表现为回退成「估算」），这里做去重
let lastNoPreviewKey = '';
// 预览请求是否在途（防止同一请求打两次被防重提交挡掉）
let noPreviewLoading = false;
// 在途期间又改了规则：等这一发回来立刻再算一次，否则预览会停在旧值上
let noPreviewPending = false;
function scheduleNoPreview() {
  clearTimeout(noPreviewTimer);
  // 200ms：改完一段（输个字符、点下起始号）差不多就是松手的时间，立刻能看到结果
  noPreviewTimer = setTimeout(() => loadNoPreview(), 200);
}
function buildNoByRule(r, seq) {
  const d = new Date();
  const p2 = (n) => String(n).padStart(2, '0');
  const dateOf = (f) => {
    const y = d.getFullYear();
    const M = p2(d.getMonth() + 1);
    const D = p2(d.getDate());
    if (f === 'yyyy') return String(y);
    if (f === 'yyyyMM') return String(y) + M;
    if (f === 'yyyyMMdd') return String(y) + M + D;
    if (f === 'yyyyMMddHHmmss') return String(y) + M + D + p2(d.getHours()) + p2(d.getMinutes()) + p2(d.getSeconds());
    if (f === 'MMdd') return M + D;
    return '';
  };
  const parts = [];
  // 流水号段只认第一个（与后端口径一致），多添的流水号段不再重复拼接
  let serialUsed = false;
  for (const s of r.segments || []) {
    let v = '';
    if (s.type === 'serial') {
      if (serialUsed) continue;
      serialUsed = true;
    }
    if (s.type === 'text') v = String(s.value || '').trim();
    else if (s.type === 'date') v = dateOf(s.format);
    else if (s.type === 'serial') v = String(seq).padStart(Number(s.len) || 4, '0');
    else if (s.type === 'field') v = demoFieldValue(s);
    if (v) parts.push(v);
  }
  // 段与段直接拼接，不再单独插分隔符（需要的话就是一个「固定文本」段）
  return parts.join('');
}
/** 预览用的字段值：取第一个选项的值，再按「代字」映射；没配代字的用原值 */
function demoFieldValue(s) {
  if (!s.field) return '';
  const opts = fieldOptions(s.field);
  if (!opts.length) return '[' + noRuleFieldLabel(s.field) + ']';
  const raw = opts[0].value;
  const code = (s.codes || {})[raw];
  return code && String(code).trim() ? String(code).trim() : raw;
}
/** 本地估算用的序号：后端已返回真实下一号就用它，否则用流水号段的起始编号 */
function guessNextSeq(r) {
  if (noPreviewSeq.value > 0) return noPreviewSeq.value;
  const s = (r.segments || []).find((x) => x.type === 'serial');
  return s ? Math.max(1, Number(s.start) || 1) : 1;
}
const processNoDemo = computed(() => {
  const r = noRuleDraft.value || defaultNoRule();
  return buildNoByRule(r, guessNextSeq(r));
});
async function loadNoPreview(force) {
  clearTimeout(noPreviewTimer);
  const r = noRuleDraft.value;
  if (!r || !r.enabled) {
    lastNoPreviewKey = '';
    noPreviewText.value = '';
    noPreviewTip.value = '';
    return;
  }
  const key = JSON.stringify(r);
  if (!force && key === lastNoPreviewKey && noPreviewText.value) return;
  // 上一发还没回来就不再发：同一请求 1 秒内第二次会被前端防重提交拦掉，白丢一次预览
  if (noPreviewLoading) {
    noPreviewPending = true;
    return;
  }
  lastNoPreviewKey = key;
  const sample = {};
  (r.segments || []).forEach((s) => {
    if (s.type !== 'field' || !s.field) return;
    // 用第一个选项的真实值去算，预览才能看出来「代字」到底映射成什么
    const opts = fieldOptions(s.field);
    sample[s.field] = opts.length ? opts[0].value : '[' + noRuleFieldLabel(s.field) + ']';
  });
  noPreviewLoading = true;
  try {
    const res = await request({
      url: '/flowable/processNo/preview',
      method: 'post',
      data: { processKey: designModel.value.modelKey || '', rule: r, sample },
    });
    const d = res.data || {};
    noPreviewSeq.value = Number(d.nextSeq) || 0;
    noPreviewText.value = d.preview || processNoDemo.value;
    noPreviewTip.value = d.started
      ? '下一单的真实编号（当前流水号 ' + (Number(d.current) || 0) + '）'
      : '尚未发过单，按起始编号预览';
  } catch (e) {
    noPreviewSeq.value = 0;
    noPreviewText.value = processNoDemo.value;
    noPreviewTip.value = '按规则与今天日期估算';
  } finally {
    noPreviewLoading = false;
    // 在途期间又改过规则：补算一次，保证看到的永远是最后设置的效果
    // （规则没变就不重发，同一请求 1 秒内重复会被防重提交挡掉）
    if (noPreviewPending) {
      noPreviewPending = false;
      if (JSON.stringify(noRuleDraft.value) !== key) loadNoPreview(true);
    }
  }
}
function addSeg() {
  const r = noRuleDraft.value;
  if (!r) return;
  if (!Array.isArray(r.segments)) r.segments = [];
  r.segments.push({ type: 'serial', len: 4, start: 1, step: 1 });
}
function moveSeg(i, delta) {
  const a = (noRuleDraft.value || {}).segments;
  if (!a) return;
  const j = i + delta;
  if (j < 0 || j >= a.length) return;
  const [x] = a.splice(i, 1);
  a.splice(j, 0, x);
}
/** 切换段类型时补齐该类型的必填属性，避免切换后渲染出空控件 */
function onSegTypeChange(s) {
  if (!s) return;
  if (s.type === 'text' && s.value == null) s.value = '';
  else if (s.type === 'date' && !s.format) s.format = 'yyyyMMdd';
  else if (s.type === 'serial')
    Object.assign(s, { len: s.len || 4, start: s.start || 1, step: s.step || 1 });
  else if (s.type === 'field') {
    s.field = s.field || '';
    if (!s.codes || typeof s.codes !== 'object') s.codes = {};
    loadNoRuleFields(designModel.value.formId);
  }
}
// 草稿一改就重新预览（防抖 200ms），让「起始编号/位数/段顺序」立刻可见；弹框关着不请求
watch(
  () => (noRuleDraft.value ? JSON.stringify(noRuleDraft.value) : ''),
  () => {
    if (noRuleOpen.value && noRuleDraft.value && noRuleDraft.value.enabled) scheduleNoPreview();
  },
);
const canvasRef = ref(null);
const xmlFileRef = ref(null);
const selectedElement = ref(null);
let bpmnModeler = null;
let modeling = null;
let selectedEl = null; // 当前选中的 bpmn 元素（非响应式）
let selectedElSource = 'main'; // 选中元素来源：main=设计器只读视图已弃用编辑，graphic=独立编辑页
/** 按选中元素所属实例取 modeling（只读视图无 modeling，编辑统一在独立编辑页） */
function currentModeling() {
  return selectedElSource === 'graphic' && graphicModeler ? graphicModeler.get('modeling') : modeling;
}
const userOptions = ref([]);
const roleOptions = ref([]);

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, modelName: undefined, modelKey: undefined, category: undefined, deployed: undefined },
  rules: {
    modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
    modelKey: [{ pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '仅允许字母开头，含字母/数字/下划线', trigger: 'blur' }],
    category: [{ required: true, message: '请选择流程分类', trigger: 'change' }],
    formId: [{ required: true, message: '请选择关联表单', trigger: 'change' }],
  },
});
const { queryParams, form, rules } = toRefs(data);

const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.value.modelName) count++;
  if (queryParams.value.modelKey) count++;
  if (queryParams.value.category) count++;
  if (queryParams.value.deployed) count++;
  return count;
});

const defaultColumns = {
  modelName: { label: '模型名称', visible: true },
  modelKey: { label: '模型Key', visible: true },
  category: { label: '流程分类', visible: true },
  version: { label: '版本', visible: true },
  deployed: { label: '部署状态', visible: true },
  createTime: { label: '创建时间', visible: true },
};
const columns = ref(loadColumnVisibility());
// 列顺序拖拽持久化（序号列也可拖动，与列宽拖拽共存）
const { sortedColumnKeys, initSortable } = useColumnDrag('flowable_model_index', ['序号', 'modelName', 'modelKey', 'category', 'formName', 'version', 'deployed', 'createTime'], tableRef, colWidth);
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_model_columns');
    if (saved) {
      const parsed = JSON.parse(saved);
      const result = {};
      Object.keys(defaultColumns).forEach((key) => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible };
      });
      return result;
    }
  } catch (e) {}
  return { ...defaultColumns };
}

function categoryLabel(value) {
  const item = flowable_category.value.find((d) => d.value == value);
  return item ? item.label : value || '-';
}

function getList() {
  loading.value = true;
  listModel(queryParams.value)
    .then((res) => {
      let rows = res.rows || [];
      if (queryParams.value.deployed) {
        rows = rows.filter((r) => r.deployed === queryParams.value.deployed);
      }
      list.value = rows;
      total.value = res.total;
      loading.value = false;
      applySavedWidths();
      initSortable();
    })
    .catch(() => {
      loading.value = false;
    });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}
function resetQuery() {
  queryParams.value.modelName = undefined;
  queryParams.value.modelKey = undefined;
  queryParams.value.category = undefined;
  queryParams.value.deployed = undefined;
  handleQuery();
}

function handleAdd() {
  form.value = { modelName: undefined, modelKey: undefined, category: 'oa', description: undefined, formId: undefined };
  loadFormOptions();
  addOpen.value = true;
}

/**
 * 自动生成模型Key：统一 wf_ 前缀 + 名称转写
 * - 英文名：wf_ + 小写下划线（如 Test Auto Flow → wf_test_auto_flow）
 * - 中文名：wf_ + 时间戳 + 随机数兜底
 * 仅作为「用户未填写」时的默认值，用户可手工覆盖
 */
function buildAutoModelKey() {
  const name = (form.value.modelName || '').trim();
  const ascii = name
    .replace(/[^a-zA-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toLowerCase();
  if (ascii && /^[a-z]/.test(ascii)) return ('wf_' + ascii).slice(0, 60);
  // 纯中文/数字开头：用 wf_ + 时间戳（秒级）+ 3位随机，保证可通过 key 正则且不易撞
  const ts = Math.floor(Date.now() / 1000)
    .toString()
    .slice(-8);
  const rnd = Math.floor(Math.random() * 900 + 100);
  return 'wf_' + ts + rnd;
}

/** 模型名称失焦：Key 为空时自动填充 */
function onModelNameBlur() {
  if (!form.value.modelKey && form.value.modelName) {
    form.value.modelKey = buildAutoModelKey();
  }
}

// ===== 关联表单设置（模型侧绑定表单，一个表单可关联多个模型；列表"表单"入口已移除，关联在设计弹窗"基本信息"完成） =====
const formOptions = ref([]);
function loadFormOptions() {
  return allForms().then((res) => {
    formOptions.value = res.data || [];
  });
}

// ===== 关联表单弹框选择器（开发规范：业务实体选择须用弹框选择器，禁止 el-select） =====
const formPickerRef = ref(null);
/** 已选表单名称（弹框选择器为只读输入 + 选择按钮的展示口径） */
const selectedFormName = computed(() => {
  if (!form.value.formId) return '';
  const hit = formOptions.value.find((f) => String(f.formId) === String(form.value.formId));
  return hit ? hit.formName : form.value.formName || '已选择表单 #' + form.value.formId;
});
function openFormPicker() {
  formPickerRef.value?.open(form.value.formId);
}
function onFormPickerConfirm(sel) {
  form.value.formId = sel.formId;
  form.value.formName = sel.formName;
}

function submitAdd() {
  proxy.$refs['modelRef'].validate((valid) => {
    if (valid) {
      // Key 留空时兜底自动生成（正常由名称失焦时填充）
      if (!form.value.modelKey) {
        form.value.modelKey = buildAutoModelKey();
      }
      addModel(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功，请点击"设计"绘制流程图');
        addOpen.value = false;
        getList();
      });
    }
  });
}

// ===== bpmn-js 设计器 =====
/** 面板/右键菜单中文翻译模块（按 bpmn-js 17 实际字符串全量覆盖） */
const bpmnTranslations = {
  // ===== 左侧面板工具 =====
  'Activate hand tool': '激活抓手工具',
  'Activate lasso tool': '激活套索选择工具',
  'Activate create/remove space tool': '激活空间调整工具',
  'Activate global connect tool': '激活全局连线工具',
  'Create start event': '创建开始事件',
  'Create intermediate/boundary event': '创建中间/边界事件',
  'Create end event': '创建结束事件',
  'Create gateway': '创建网关',
  'Create task': '创建任务',
  'Create data object reference': '创建数据对象',
  'Create data store reference': '创建数据存储',
  'Create pool/participant': '创建池/参与者',
  'Create group': '创建分组注释',
  'Create expanded sub-process': '创建展开的子流程',
  // ===== 选中节点快捷菜单（追加/连接） =====
  'Append end event': '追加结束事件',
  'Append task': '追加任务',
  'Append gateway': '追加网关',
  'Append intermediate/boundary event': '追加中间/边界事件',
  'Append receive task': '追加接收任务',
  'Append text annotation': '追加文本注释',
  'Append compensation activity': '追加补偿活动',
  'Append conditional intermediate catch event': '追加条件中间捕获事件',
  'Append message intermediate catch event': '追加消息中间捕获事件',
  'Append signal intermediate catch event': '追加信号中间捕获事件',
  'Append timer intermediate catch event': '追加定时中间捕获事件',
  'Connect using association': '创建注释关联',
  'Connect using data input association': '创建数据输入关联',
  'Connect to other element': '连接到其他元素',
  'Change element': '修改类型',
  Delete: '删除',
  'Add text annotation': '添加文本注释',
  // ===== 泳道 =====
  'Add lane above': '在上方添加泳道',
  'Add lane below': '在下方添加泳道',
  'Divide into two lanes': '拆分为两条泳道',
  'Divide into three lanes': '拆分为三条泳道',
  // ===== 对齐/分布 =====
  'Align elements': '元素对齐',
  'Distribute elements horizontally': '水平分布元素',
  'Distribute elements vertically': '垂直分布元素',
  // ===== 多实例/循环等标记 =====
  Loop: '循环',
  'Parallel multi-instance': '并行多实例（会签）',
  'Sequential multi-instance': '顺序多实例（依次审批）',
  'Participant multiplicity': '参与者多实例',
  'Ad-hoc': '临时（Ad-hoc）子流程',
  'Toggle non-interrupting': '切换为非中断事件',
  Collection: '集合',
  // ===== 修改类型菜单（ReplaceMenu） =====
  Task: '任务',
  'User task': '用户任务（人工审批）',
  'Send task': '发送任务',
  'Receive task': '接收任务',
  'Manual task': '手工任务',
  'Business rule task': '业务规则任务',
  'Script task': '脚本任务',
  'Call activity': '调用活动（子流程引用）',
  'Service task': '服务任务（系统自动执行）',
  'Sub-process': '子流程',
  'Sub-process (collapsed)': '子流程（折叠）',
  'Sub-process (expanded)': '子流程（展开）',
  Transaction: '事务子流程',
  'Event sub-process': '事件子流程',
  'Event-based gateway': '事件网关',
  'Exclusive gateway': '排他网关（二选一）',
  'Inclusive gateway': '包容网关（多选）',
  'Complex gateway': '复杂网关',
  'Parallel gateway': '并行网关（全走）',
  'Parallel Event based instantiating Gateway': '并行事件网关（实例化）',
  'Event based instantiating Gateway': '事件网关（实例化）',
  'Sequence flow': '顺序流',
  'Conditional flow': '条件流',
  'Default flow': '默认流',
  'Start event': '开始事件',
  'End event': '结束事件',
  'Intermediate throw event': '中间抛出事件',
  'Message start event': '消息开始事件',
  'Message intermediate catch event': '消息中间捕获事件',
  'Message intermediate throw event': '消息中间抛出事件',
  'Message end event': '消息结束事件',
  'Message boundary event': '消息边界事件',
  'Message boundary event (non-interrupting)': '消息边界事件（非中断）',
  'Timer start event': '定时开始事件',
  'Timer intermediate catch event': '定时中间捕获事件',
  'Timer boundary event': '定时边界事件',
  'Timer boundary event (non-interrupting)': '定时边界事件（非中断）',
  'Conditional start event': '条件开始事件',
  'Conditional intermediate catch event': '条件中间捕获事件',
  'Conditional boundary event': '条件边界事件',
  'Conditional boundary event (non-interrupting)': '条件边界事件（非中断）',
  'Signal start event': '信号开始事件',
  'Signal start event (non-interrupting)': '信号开始事件（非中断）',
  'Signal intermediate catch event': '信号中间捕获事件',
  'Signal intermediate throw event': '信号中间抛出事件',
  'Signal boundary event': '信号边界事件',
  'Signal boundary event (non-interrupting)': '信号边界事件（非中断）',
  'Signal end event': '信号结束事件',
  'Escalation start event': '升级开始事件',
  'Escalation start event (non-interrupting)': '升级开始事件（非中断）',
  'Escalation intermediate throw event': '升级中间抛出事件',
  'Escalation boundary event': '升级边界事件',
  'Escalation boundary event (non-interrupting)': '升级边界事件（非中断）',
  'Escalation end event': '升级结束事件',
  'Error start event': '错误开始事件',
  'Error boundary event': '错误边界事件',
  'Error end event': '错误结束事件',
  'Compensation start event': '补偿开始事件',
  'Compensation intermediate throw event': '补偿中间抛出事件',
  'Compensation boundary event': '补偿边界事件',
  'Compensation end event': '补偿结束事件',
  'Cancel end event': '终止结束事件',
  'Cancel boundary event': '取消边界事件',
  'Terminate end event': '终止结束事件',
  'Message start event (non-interrupting)': '消息开始事件（非中断）',
  'Timer start event (non-interrupting)': '定时开始事件（非中断）',
  'Conditional start event (non-interrupting)': '条件开始事件（非中断）',
  'Link intermediate catch event': '链接中间捕获事件',
  'Link intermediate throw event': '链接中间抛出事件',
  'Data object reference': '数据对象',
  'Data store reference': '数据存储',
  'Expanded pool/participant': '展开的池/参与者',
};
function bpmnTranslate(template, replacements) {
  const text = bpmnTranslations[template] || template;
  return text.replace(/{([^}]+)}/g, (_, key) => (replacements && replacements[key]) || '{' + key + '}');
}
const bpmnChineseTranslate = { translate: ['value', bpmnTranslate] };

/**
 * 精简版节点面板：只保留业务需要的节点类型（参考节点面板）
 * 事件：开始 / 结束
 * 节点：互斥网关 / 并行网关 / 相容网关 / 用户任务
 * 覆盖 bpmn-js 默认 PaletteProvider，移除连线工具、中间事件、数据对象、泳道等冗余入口；
 * 底层仍生成标准 BPMN 元素，不影响引擎解析与部署。
 */
/** 节点图标（内联 SVG，配色与画布节点一致：开始绿、结束红、网关橙、用户任务蓝） */
const PALETTE_ICONS = {
  start: '<svg viewBox="0 0 24 24" width="16" height="16"><polygon points="8,5 19,12 8,19" fill="#fff"/></svg>',
  end: '<svg viewBox="0 0 24 24" width="16" height="16"><rect x="9" y="9" width="12" height="12" rx="1.5" fill="#fff"/></svg>',
  exclusive: '<svg viewBox="0 0 24 24" width="16" height="16"><path d="M7.5 7.5L16.5 16.5M16.5 7.5L7.5 16.5" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/></svg>',
  parallel: '<svg viewBox="0 0 24 24" width="16" height="16"><path d="M12 7V17M7 12H17" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/></svg>',
  inclusive: '<svg viewBox="0 0 24 24" width="16" height="16"><circle cx="12" cy="12" r="4" fill="none" stroke="#fff" stroke-width="2"/></svg>',
  task: '<svg viewBox="0 0 24 24" width="16" height="16"><circle cx="12" cy="7.6" r="3.6" fill="#fff"/><path d="M5.2 19.2c0-3.5 3-5.6 6.8-5.6s6.8 2.1 6.8 5.6z" fill="#fff"/></svg>',
};
function WmPaletteProvider(palette, create, elementFactory, translate) {
  this._palette = palette;
  this._create = create;
  this._elementFactory = elementFactory;
  this._translate = translate;
  palette.registerProvider(this);
}
/**
 * 开始/结束事件的默认名称：圆圈里直接写「开始 / 结束」，与用户任务「名称写在节点里」同一观感，
 * 拖入即有名字，不用再到属性面板里补。
 */
const WM_EVENT_DEFAULT_NAME = { 'bpmn:StartEvent': '开始', 'bpmn:EndEvent': '结束' };
WmPaletteProvider.$inject = ['palette', 'create', 'elementFactory', 'translate'];
WmPaletteProvider.prototype.getPaletteEntries = function () {
  const create = this._create;
  const elementFactory = this._elementFactory;
  const translate = this._translate;
  function action(type, group, className, title, icon) {
    const defaultName = WM_EVENT_DEFAULT_NAME[type];
    function createListener(event) {
      const shape = elementFactory.createShape({ type });
      // 开始/结束事件带上默认名称，新建时就显示在圆圈里。
      // 注意：elementFactory.createShape 只认 type 等结构属性，attrs 里的 name 会被丢掉，
      // 必须在拖拽模板对象上直接写 businessObject.name，落下时才会连标签一起创建。
      if (defaultName) shape.businessObject.name = translate(defaultName);
      create.start(event, shape);
    }
    // 参考节点面板：彩色图标在上、文字在下，便于一眼识别节点类型
    const html = '<div class="entry" draggable="true"><i class="wm-node-icon wm-node-icon-' + icon + '">' + PALETTE_ICONS[icon] + '</i><span class="wm-pal-label">' + title + '</span></div>';
    return { group, className, title, html, action: { dragstart: createListener, click: createListener } };
  }
  return {
    'create.start-event': action('bpmn:StartEvent', 'event', 'wm-pal-start', translate('开始'), 'start'),
    'create.end-event': action('bpmn:EndEvent', 'event', 'wm-pal-end', translate('结束'), 'end'),
    'create.exclusive-gateway': action('bpmn:ExclusiveGateway', 'task', 'wm-pal-gw', translate('互斥网关'), 'exclusive'),
    'create.parallel-gateway': action('bpmn:ParallelGateway', 'task', 'wm-pal-gw', translate('并行网关'), 'parallel'),
    'create.inclusive-gateway': action('bpmn:InclusiveGateway', 'task', 'wm-pal-gw', translate('相容网关'), 'inclusive'),
    'create.user-task': action('bpmn:UserTask', 'task', 'wm-pal-task', translate('用户任务'), 'task'),
  };
};
/** 精简工具条模块（覆盖默认 palette） */
const wmPaletteModule = { paletteProvider: ['type', WmPaletteProvider] };

/**
 * 节点快捷菜单（ContextPad）裁剪：
 * bpmn-js 默认把全部 BPMN 入口都放出来（中间事件、文本注释、全量改类型），
 * 与平台「只认开始/结束/审批/分支 4 类节点」的约定冲突，且默认「追加任务」建的是
 * bpmn:Task（无办理人，引擎会直接走过），这里统一白名单化。
 */
function WmContextPadProvider(contextPad, create, elementFactory, autoPlace) {
  this._create = create;
  this._elementFactory = elementFactory;
  this._autoPlace = autoPlace;
  // 优先级取负，保证在 bpmn-js 默认 provider 之后执行，才能删改它给出的入口
  contextPad.registerProvider(-1000, this);
}
WmContextPadProvider.$inject = ['contextPad', 'create', 'elementFactory', 'autoPlace'];
WmContextPadProvider.prototype.getContextPadEntries = function (element) {
  const create = this._create;
  const elementFactory = this._elementFactory;
  const autoPlace = this._autoPlace;
  /** 追加审批节点（默认「追加任务」建的是 bpmn:Task，改成与左侧面板一致的 bpmn:UserTask） */
  function appendUserTaskAction() {
    function appendStart(event, source) {
      const shape = elementFactory.createShape({ type: 'bpmn:UserTask' });
      create.start(event, shape, { source });
    }
    return {
      group: 'model',
      className: 'bpmn-icon-task',
      title: '追加审批节点',
      action: {
        dragstart: appendStart,
        click: function (_, source) {
          autoPlace.append(source, elementFactory.createShape({ type: 'bpmn:UserTask' }));
        },
      },
    };
  }
  return function (entries) {
    // 业务上不存在、且属性面板无法配置的入口：中间/边界事件、文本注释
    delete entries['append.intermediate-event'];
    delete entries['append.text-annotation'];
    // 改类型统一走节点表格（带配置迁移与连线重建），避免这里打开全量 BPMN 类型菜单
    delete entries['replace'];
    if (entries['append.append-task']) {
      entries['append.append-task'] = appendUserTaskAction();
    }
    return entries;
  };
};
/** 节点快捷菜单模块 */
const wmContextPadModule = { __init__: ['wmContextPad'], wmContextPad: ['type', WmContextPadProvider] };

/**
 * 只限制「节点名称」文字（label）的单独拖动，其余一切照常：
 *   - 节点本身、连线本身（没有 labelTarget）：放行，由 diagram-js 的 Move 正常处理，
 *     单点拖动与框选后整体拖动都依赖这条路径；
 *   - 连线（出口线）名称：放行，位置需要人工微调（避开压线与分支名重叠）；
 *   - 节点名称 / 事件名称（labelTarget 指向节点、事件）：拒绝单独拖动。
 *
 * 背景：画布上的节点名/事件名是画在图形内部的白色文字（markEventLabel / styleTaskName 写的内联样式），
 * 而 diagram-js 把 label 当成普通 shape，允许单独拖走（Move 会把 label 塞进 shapes 里真正移动）；
 * 白字一旦被拖到白色画布上就“看不见了”，用户会以为节点名丢了。
 * 这里在 shape.move.start 阶段（优先级 2000，高于 Move 的 1500/1000 与 LabelSupport 的 1400）返回 false 拒绝，
 * 只取消这一颗文字的拖动；节点名仍可双击改文字，也会随所属节点一起移动。
 */
function WmLabelLock(eventBus) {
  eventBus.on('shape.move.start', 2000, (e) => {
    const shape = e && e.shape;
    if (!shape) return;
    const host = shape.labelTarget;
    // 不是标签（节点本身、连线本身）：交给 diagram-js 正常拖动，框选后整体拖动也走这里
    if (!host) return undefined;
    const isConnection = !!(host.waypoints || host.type === 'bpmn:SequenceFlow' || /Connection$/.test(host.type || ''));
    // 连线名称放行，其余（节点/事件名称）一律拒绝拖动
    return isConnection ? undefined : false;
  });
}
WmLabelLock.$inject = ['eventBus'];
/** 节点名称锁定模块 */
const wmLabelLockModule = { __init__: ['wmLabelLock'], wmLabelLock: ['type', WmLabelLock] };

/**
 * 框选多选：画布空白处按住左键拖动 → 出现选框，框内节点/连线一起选中；
 * 之后拖动任一选中节点即可整体移动（Move 模块本身支持多选拖拽）。
 *
 * bpmn-js 17 的 Modeler 没有挂载 lasso 工具，空白处左键拖动只会平移画布，
 * 这里把 diagram-js 的 LassoTool 接到「空白处左键拖动」上（Shift+拖动保留 lassoTool 自带逻辑）。
 */
function WmBoxSelect(eventBus, canvas, lassoTool, selection) {
  eventBus.on('element.mousedown', 1400, (event) => {
    if (event.element !== canvas.getRootElement()) return;
    if (!isPrimaryButton(event) || hasSecondaryModifier(event)) return;
    lassoTool.activateLasso(event.originalEvent);
    // 事件已处理，避免继续冒泡到 MoveCanvas 变成平移画布
    return true;
  });
  // 框选结果去掉根元素与文字标签，避免出现「选中一段文字」的观感。
  // lassoTool 自己在 lasso.end 上的优先级是 0，这里取负优先级保证在它写入选区之后再过滤
  eventBus.on('lasso.end', -100, () => {
    const all = selection.get();
    const filtered = all.filter((el) => el !== canvas.getRootElement() && el.type !== 'label' && !el.labelTarget);
    if (filtered.length !== all.length) selection.select(filtered);
  });
}
WmBoxSelect.$inject = ['eventBus', 'canvas', 'lassoTool', 'selection'];
/** 框选模块（依赖 diagram-js 的 lasso-tool） */
const wmBoxSelectModule = { __init__: ['wmBoxSelect'], wmBoxSelect: ['type', WmBoxSelect] };

/**
 * 等「流转设置」页签的画布挂载完成（弹窗打开动画 / 页签首帧渲染时 canvasRef 可能还是空）。
 * 最多等 1 秒，仍拿不到就交给调用方提示用户重开关联。
 */
async function waitForDesignerCanvas() {
  for (let i = 0; i < 20; i++) {
    if (canvasRef.value) return true;
    await new Promise((resolve) => setTimeout(resolve, 50));
  }
  return !!canvasRef.value;
}

async function onDesignerOpened() {
  // 弹窗刚打开时页签内容可能还没挂载，「流转设置」画布此刻可能为空：
  // 把 null 当 container 传给 bpmn-js，整块画布会永久空白（之后切页签也补不回来），这里先等容器就绪
  if (!(await waitForDesignerCanvas())) {
    proxy.$modal.msgWarning('流程图画布未就绪，请关闭设计器后重新打开');
    return;
  }
  // 设计器必需样式：工具面板/连线菜单/字体图标
  await import('bpmn-js/dist/assets/diagram-js.css');
  await import('bpmn-js/dist/assets/bpmn-js.css');
  await import('bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css');
  // 只读视图：NavigatedViewer（可平移缩放、无编辑面板/快捷面板）——编辑统一进独立编辑页面
  const { default: NavigatedViewer } = await import('bpmn-js/lib/NavigatedViewer.js');
  bpmnModeler = new NavigatedViewer({
    container: canvasRef.value,
    additionalModules: [bpmnChineseTranslate],
  });
  modeling = null;
  // 加载用户/角色选项（供独立编辑页面办理人/候选组选择器）
  request({ url: '/system/user/list', method: 'get', params: { pageNum: 1, pageSize: 200 } })
    .then((res) => {
      userOptions.value = res.rows || [];
    })
    .catch((e) => {
      proxy.$modal.msgWarning('用户列表加载失败，办理人下拉将为空：' + (e?.message || e));
    });
  request({ url: '/system/role/list', method: 'get', params: { pageNum: 1, pageSize: 100 } })
    .then((res) => {
      roleOptions.value = res.rows || [];
    })
    .catch((e) => {
      proxy.$modal.msgWarning('角色列表加载失败，候选组下拉将为空：' + (e?.message || e));
    });
  const xml = designModel.value.bpmnXml || '';
  // 无 XML 或缺少 bpmndi 图形段（DI）时，bpmn-js importXML 会抛 “no diagram to display”，
  // 此处统一兜底为空画布，保证新建/历史空模型也能正常打开设计器
  const hasDiagram = /<bpmndi:BPMNDiagram[\s>]/.test(xml);
  if (!xml || !hasDiagram) {
    try {
      await newBlankDiagram();
      bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
      applyWmCanvasMarkers(bpmnModeler);
    } catch (e) {
      proxy.$modal.msgWarning('该模型暂无流程图，点"编 辑"开始绘制');
    }
    return;
  }
  try {
    // 历史模型里存在 100x60 这类被压扁的处理节点、36x36 的默认事件：
    // 导入时统一撑到设计器口径（事件 80x80、处理节点不低于 100x80），与「编 辑」画布、流程图查看器一致
    await bpmnModeler.importXML(normalizeDiagramSizes(xml));
    bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
    // 布局稳定后二次适配：内容垂直水平居中，避免贴顶、下方大片空白
    setTimeout(() => {
      try {
        bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
      } catch (e) {}
    }, 150);
    applyWmCanvasMarkers(bpmnModeler);
  } catch (e) {
    proxy.$modal.msgError('流程图导入失败：' + (e.message || e));
  }
}

/** 退回线自动重布局：退回/驳回出口绕行（源上方 → 横向跨越 → 目标顶部），其余连线走默认布局 */
let wmReturnExitChecker = null;
function applyWmReturnRoutes(modeler) {
  if (!modeler || !wmReturnExitChecker) return;
  try {
    const layouter = modeler.get('layouter');
    const modeling = modeler.get('modeling');
    if (!layouter || !modeling) return;
    const registry = modeler.get('elementRegistry');
    const flows = registry.filter((el) => el.type === 'bpmn:SequenceFlow');
    flows.forEach((el) => {
      const hit = wmReturnExitChecker(el);
      if (!hit) return;
      const waypoints = layouter.layoutConnection(el, { connectionStart: undefined, connectionEnd: undefined });
      if (waypoints && waypoints.length) {
        modeling.updateWaypoints(el, waypoints);
      }
    });
    // updateWaypoints 触发连线重渲染会丢失 marker，重布局后重新应用视觉标记（红虚线）
    applyWmCanvasMarkers(modeler);
  } catch (e) {}
}

/**
 * 是否为部署时注入的「发起人（重新提交）」节点。
 * flowable:initiatorNode 不在 bpmn-moddle 描述符里，bo.get() 取不到，只能读 $attrs。
 */
function isInitiatorNode(el) {
  const bo = el && el.businessObject;
  if (!bo) return false;
  const v = (bo.get && bo.get('flowable:initiatorNode')) || (bo.$attrs && bo.$attrs['flowable:initiatorNode']);
  return v === true || v === 'true';
}

/** 部署时注入的发起人技术节点 ID（与后端 FlowReturnConstants.INITIATOR_NODE_ID 保持一致） */
const INITIATOR_NODE_ID = 'applyNode';

/**
 * 剔除部署时注入的「发起人（重新提交）」技术节点：节点本身 + 相关连线 + 对应图形（DI）。
 *
 * 版本 XML 取自部署产物，天然带这个技术节点。若直接载入设计器：画布会多出一个虚线框、
 * 节点数变多，而且点「保存 / 存为新版」会把技术节点写回模型草稿，模型从此长期被污染。
 * 因此展示与编辑侧统一先剥离；部署时后端 injectInitiatorNode 会按需重新注入，能力不受影响。
 */
function stripInitiatorArtifacts(xml) {
  if (!xml || (xml.indexOf(INITIATOR_NODE_ID) < 0 && xml.indexOf('initiatorNode') < 0)) return xml;
  try {
    const doc = new DOMParser().parseFromString(xml, 'application/xml');
    if (!doc || doc.getElementsByTagName('parsererror').length) return xml;
    const all = Array.from(doc.getElementsByTagName('*'));
    const node = all.find(
      (el) => el.getAttribute && (el.getAttribute('flowable:initiatorNode') === 'true' || el.getAttribute('initiatorNode') === 'true' || el.getAttribute('id') === INITIATOR_NODE_ID),
    );
    if (!node) return xml;
    const nodeId = node.getAttribute('id');
    const removed = new Set([nodeId]);
    // 技术节点只有出线没有入线：两条都按 id 一并摘掉，避免残留孤立连线
    all
      .filter((el) => el.localName === 'sequenceFlow' && (el.getAttribute('sourceRef') === nodeId || el.getAttribute('targetRef') === nodeId))
      .forEach((el) => removed.add(el.getAttribute('id')));
    all.forEach((el) => {
      const ref = el.getAttribute('bpmnElement');
      const isIncomingOutgoingRef = (el.localName === 'incoming' || el.localName === 'outgoing') && removed.has((el.textContent || '').trim());
      if (removed.has(el.getAttribute('id')) || (ref && removed.has(ref)) || isIncomingOutgoingRef) {
        if (el.parentNode) el.parentNode.removeChild(el);
      }
    });
    return new XMLSerializer().serializeToString(doc);
  } catch (e) {
    return xml;
  }
}

/** 单体节点视觉标记：开始=绿圆、结束=红色终止、发起人=灰虚线卡、用户任务=蓝色卡片、网关=橙色菱形 */
function markWmShape(el, modeler) {
  if (!el || !modeler) return;
  // 先着色再打标记：即使 canvas.addMarker 这类接口异常，也不影响节点视觉
  decorateWmShape(el, modeler);
  try {
    const canvas = modeler.get('canvas');
    if (el.type === 'bpmn:StartEvent') {
      canvas.addMarker(el.id, 'wm-start');
      markEventLabel(el, modeler);
    } else if (el.type === 'bpmn:EndEvent') {
      canvas.addMarker(el.id, 'wm-end');
      markEventLabel(el, modeler);
    } else if (isInitiatorNode(el)) canvas.addMarker(el.id, 'wm-initiator');
    else if (el.type === 'bpmn:UserTask' || el.type === 'bpmn:Task' || el.type === 'bpmn:ServiceTask') canvas.addMarker(el.id, 'wm-task');
    else if (isGatewayType(el.type)) canvas.addMarker(el.id, 'wm-gateway');
  } catch (e) {}
}

/**
 * 开始/结束事件的名称已经由 utils/bpmnSizes#normalizeEventLabelPositions 搬进圆内，
 * 这里把标签切成与用户任务同款的白字粗体（默认是圆下方的深灰字），并打上标记供样式表命中。
 */
function markEventLabel(el, modeler) {
  try {
    const canvas = modeler.get('canvas');
    const registry = modeler.get('elementRegistry');
    registry.filter((lb) => lb.labelTarget === el).forEach((lb) => {
      canvas.addMarker(lb.id, 'wm-event-label');
      const graphics = registry.getGraphics(lb);
      const visual = graphics && graphics.querySelector ? graphics.querySelector('.djs-visual') : null;
      if (visual) styleTaskName(visual);
    });
  } catch (e) {}
}

/**
 * 把开始/结束事件的名称挂到圆圈里（文字行垂直居中于圆心）。
 * bpmn-js 默认把事件标签放在圆下方，新建、改名、换类型后都会重新落回下方；
 * 导入的模型由 normalizeEventLabelPositions 在 XML 层处理，这里补齐「画布上新增/改动」的同一口径。
 */
function placeEventLabelInside(el, modeler) {
  if (!modeler || !el) return;
  if (el.type !== 'bpmn:StartEvent' && el.type !== 'bpmn:EndEvent') return;
  try {
    const registry = modeler.get('elementRegistry');
    const modeling = modeler.get('modeling');
    if (!modeling) return;
    registry
      .filter((lb) => lb.labelTarget === el)
      .forEach((label) => {
        if (!label.width || !label.height) return;
        const dx = Math.round(el.x + (el.width - label.width) / 2 - label.x);
        const dy = Math.round(el.y + el.height / 2 - label.height / 2 - label.y);
        // 已在圆内（位置足够接近）就不动，避免 element.changed → 挪标签 → element.changed 死循环
        if (Math.abs(dx) < 1 && Math.abs(dy) < 1) return;
        modeling.moveElements([label], { x: dx, y: dy });
      });
  } catch (e) {}
}

/** 用户任务名称由 bpmn-js 直接绘制在节点内部，这里改为白色粗体（避免再画一份重复文字） */
function styleTaskName(visual) {
  try {
    visual.querySelectorAll('text').forEach((t) => {
      if (t.getAttribute('fill') !== '#fff') t.setAttribute('fill', '#fff');
      if (t.style.fill !== '#fff') t.style.fill = '#fff';
      if (t.getAttribute('font-weight') !== '700') t.setAttribute('font-weight', '700');
      if (t.style.fontWeight !== '700') t.style.fontWeight = '700';
    });
  } catch (e) {}
}

/**
 * 在节点 SVG 内补画装饰图元（CSS 伪元素对 SVG 元素无效，只能真实插入节点），
 * 让画布节点与左侧节点面板图标保持同一套视觉：开始绿圆白三角、结束红圆白方块、用户任务蓝卡白人像。
 */
function decorateWmShape(el, modeler) {
  try {
    const graphics = modeler.get('elementRegistry').getGraphics(el);
    const visual = graphics && graphics.querySelector('.djs-visual');
    if (!visual) return;
    const NS = 'http://www.w3.org/2000/svg';
    const w = el.width || 36;
    const h = el.height || 36;
    const add = (tag, attrs) => {
      const node = document.createElementNS(NS, tag);
      Object.keys(attrs).forEach((k) => node.setAttribute(k, attrs[k]));
      node.setAttribute('class', 'wm-deco');
      node.setAttribute('pointer-events', 'none');
      visual.appendChild(node);
    };
    // bpmn-js 用 SVG 属性绘制节点，样式表选择器命中率不稳定，这里直接改 SVG 属性，保证拖入即生效
    const paint = (node, attrs) => {
      if (!node) return;
      Object.keys(attrs).forEach((k) => {
        const v = String(attrs[k]);
        if (node.getAttribute(k) !== v) node.setAttribute(k, v);
        // SVG 呈现属性优先级低于样式表，这里同步写内联样式，避免被 bpmn-js 默认样式覆盖
        if (k === 'fill' || k === 'stroke' || k === 'stroke-width' || k === 'display') {
          if (node.style[k] !== v) node.style[k] = v;
        }
      });
    };
    if (el.type === 'bpmn:StartEvent') {
      paint(visual.querySelector('circle') || visual.querySelector('ellipse'), { fill: '#4caf50', stroke: '#43a047', 'stroke-width': '2' });
      // 符号缩小放圆圈左上角（对齐用户任务左上角的人像图标），名称居中写在圆里（与任务节点同一观感）
      if (!visual.querySelector('.wm-deco')) {
        add('path', { d: 'M' + w * 0.195 + ' ' + h * 0.175 + ' L' + w * 0.385 + ' ' + h * 0.275 + ' L' + w * 0.195 + ' ' + h * 0.375 + ' Z', fill: '#fff', stroke: 'none' });
      }
    } else if (el.type === 'bpmn:EndEvent') {
      paint(visual.querySelector('circle') || visual.querySelector('ellipse'), { fill: '#e74c3c', stroke: '#1f2937', 'stroke-width': '4' });
      // 终止结束事件默认会多画一个黑色实心圆，隐去只保留白方块
      visual.querySelectorAll('circle').forEach((c, i) => {
        if (i > 0) c.setAttribute('display', 'none');
      });
      if (!visual.querySelector('.wm-deco')) {
        add('rect', { x: w * 0.21, y: h * 0.21, width: w * 0.13, height: h * 0.13, fill: '#fff', stroke: 'none' });
      }
    } else if (isInitiatorNode(el)) {
      // 发起人（重新提交）节点：灰白虚线卡片。正常流转不经过它，只在驳回退回发起人时使用，
      // 画成和审批节点一样的蓝色卡片会被误认为“多了一个申请节点”
      paint(visual.querySelector('rect'), { fill: '#f8fafc', stroke: '#94a3b8', 'stroke-width': '2', 'stroke-dasharray': '6 4' });
      // 没有办理人、也不是真审批环节，隐去默认人像图标，只留节点名
      visual.querySelectorAll('path:not(.wm-deco)').forEach((p) => {
        p.setAttribute('display', 'none');
        p.style.display = 'none';
      });
      // 浅底上用深灰字（styleTaskName 的白色在灰底上看不清，所以不走 task 分支）
      visual.querySelectorAll('text').forEach((t) => {
        if (t.getAttribute('fill') !== '#475569') t.setAttribute('fill', '#475569');
        if (t.style.fill !== '#475569') t.style.fill = '#475569';
      });
    } else if (el.type === 'bpmn:UserTask' || el.type === 'bpmn:Task' || el.type === 'bpmn:ServiceTask') {
      paint(visual.querySelector('rect'), { fill: '#4a90e2', stroke: '#3a7fbf', 'stroke-width': '2' });
      // 隐去 bpmn-js 默认的黑色人像标识，改用面板同款白色人像（保留自己插入的装饰图元）
      visual.querySelectorAll('path:not(.wm-deco)').forEach((p) => {
        p.setAttribute('display', 'none');
        p.style.display = 'none';
      });
      styleTaskName(visual);
      if (!visual.querySelector('.wm-deco')) {
        // 左上角白色人像
        add('circle', { cx: 16, cy: 14, r: 4.6, fill: '#fff', stroke: 'none' });
        add('path', { d: 'M8 27c0-4.2 3.6-6.6 8-6.6s8 2.4 8 6.6z', fill: '#fff', stroke: 'none' });
      }
    } else if (isGatewayType(el.type)) {
      paint(visual.querySelector('polygon'), { fill: '#f0a63c', stroke: '#d98b1f', 'stroke-width': '2' });
      visual.querySelectorAll('path').forEach((p) => {
        p.setAttribute('fill', '#fff');
        p.setAttribute('stroke', '#fff');
      });
      visual.querySelectorAll('circle').forEach((c) => {
        c.setAttribute('fill', 'none');
        c.setAttribute('stroke', '#fff');
        c.setAttribute('stroke-width', '2.5');
      });
    }
  } catch (e) {}
}

/** 变更后延迟补一次全量视觉（bpmn-js 可能先渲染后重绘，直接属性着色需要兜底重刷） */
let wmDecoSweepTimer = null;
function scheduleWmDecoSweep(modeler) {
  if (wmDecoSweepTimer) clearTimeout(wmDecoSweepTimer);
  wmDecoSweepTimer = setTimeout(() => {
    try {
      applyWmCanvasMarkers(modeler);
    } catch (e) {}
  }, 120);
}

/**
 * 画布 DOM 兜底监听：不依赖 bpmn-js 事件时序，节点 SVG 一渲染出来就统一上色。
 * 只监听结构/形状相关属性，自身写入的 fill/stroke 不会触发，避免循环。
 */
let wmCanvasObserver = null;
let wmObserverTimer = null;
function setupWmCanvasObserver() {
  try {
    if (wmCanvasObserver) {
      wmCanvasObserver.disconnect();
      wmCanvasObserver = null;
    }
    const host = graphicCanvasRef.value;
    if (!host) return;
    wmCanvasObserver = new MutationObserver(() => {
      if (wmObserverTimer) return;
      wmObserverTimer = setTimeout(() => {
        wmObserverTimer = null;
        if (graphicModeler) applyWmCanvasMarkers(graphicModeler);
      }, 60);
    });
    wmCanvasObserver.observe(host, { childList: true, subtree: true, attributes: true, attributeFilter: ['transform', 'class', 'd'] });
  } catch (e) {}
}

/** 画布视觉标记：节点样式 + 退回出口红色虚线 */
function applyWmCanvasMarkers(modeler) {
  if (!modeler) return;
  try {
    const canvas = modeler.get('canvas');
    const registry = modeler.get('elementRegistry');
    registry.forEach((el) => {
      if (el.type === 'bpmn:SequenceFlow') {
        const bo = el.businessObject;
        let meta = {};
        try {
          meta = JSON.parse((bo.get && bo.get('flowable:exitMeta')) || (bo.$attrs && bo.$attrs['flowable:exitMeta']) || '{}') || {};
        } catch (e) {}
        const isReturn = !!meta.isReturn || /退回|驳回/.test(bo.name || '');
        if (isReturn) canvas.addMarker(el.id, 'wm-return');
        else canvas.removeMarker(el.id, 'wm-return');
      } else {
        markWmShape(el, modeler);
      }
    });
  } catch (e) {}
}

/**
 * 把 XML 重新灌进设计器画布，并补回同款配色。
 * bpmn-js 的 importXML 会重建整棵 SVG：之前 addMarker 打的 wm-* 标记、decorateWmShape 插的
 * 白三角/白方块全部丢失，画布就退回 bpmn-js 默认的黑白样式（编辑完流程图保存后回来看见的就是它）。
 * 因此每次重导入后都必须重跑一次 applyWmCanvasMarkers。
 */
async function syncToDesignerCanvas(xml) {
  if (!bpmnModeler || !xml) return;
  await bpmnModeler.importXML(xml);
  const refit = () => {
    try {
      bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
    } catch (e) {}
    // marker / 装饰图元是幂等的，重复上色不会叠加
    try {
      applyWmCanvasMarkers(bpmnModeler);
    } catch (e) {}
  };
  refit();
  // 画布可能刚从隐藏状态恢复，等尺寸稳定后再补一次
  window.setTimeout(refit, 120);
}

/** 创建新画布：根流程 ID 对齐模型 Key，画布保持纯白（节点由用户从 palette 自行拖拽） */
async function newBlankDiagram(target) {
  // createDiagram 只存在于 Modeler，NavigatedViewer（只读）没有该方法：走默认时先兜底到 graphicModeler
  const md = target || (bpmnModeler && typeof bpmnModeler.createDiagram === 'function' ? bpmnModeler : graphicModeler);
  if (!md || typeof md.createDiagram !== 'function') throw new Error('当前画布实例不支持创建新图');
  await md.createDiagram();
  const canvas = md.get('canvas');
  const root = canvas.getRootElement();
  const modeling = md.get('modeling');
  modeling.updateProperties(root, {
    id: designModel.value.modelKey,
    name: designModel.value.modelName || designModel.value.modelKey,
  });
  // 纯白画布：不预置 开始/结束 节点，用户从上方 palette 自行拖拽绘制
}

/** 可配置节点：申请（开始）/用户任务/结束节点都可设置操作人员与表单 */
function isConfigurableNode(node) {
  return !!node && (node.isUserTask || node.isStart || node.isEnd);
}

function loadSelectedElement(el) {
  selectedEl = el;
  const bo = el.businessObject;
  const isUserTask = el.type === 'bpmn:UserTask';
  const isStart = el.type === 'bpmn:StartEvent';
  const isEnd = el.type === 'bpmn:EndEvent';
  const isSequenceFlow = el.type === 'bpmn:SequenceFlow';
  const isBranchNode = isGatewayType(el.type);
  let condition = '';
  if (isSequenceFlow && bo.conditionExpression) {
    condition = bo.conditionExpression.body || '';
  }
  // 右面板分支条件：把已有表达式反解成「字段 + 运算符 + 值」的规则行
  if (isSequenceFlow) initConditionBuilder(condition);
  selectedElement.value = {
    id: bo.id,
    name: bo.name || '',
    type: el.type,
    isUserTask,
    isStart,
    isEnd,
    isSequenceFlow,
    isBranchNode,
    sourceIsGateway: !!(el.source && el.source.type && el.source.type.includes('Gateway')),
    // 默认出口只对排他/包容网关有意义（并行网关按全部出口同时激活，不存在兜底）
    defaultFlowSupported:
      isSequenceFlow && !!el.source && (el.source.type === 'bpmn:ExclusiveGateway' || el.source.type === 'bpmn:InclusiveGateway'),
    isDefaultFlow: !!(
      isSequenceFlow &&
      el.source &&
      el.source.businessObject &&
      el.source.businessObject.default &&
      el.source.businessObject.default.id === bo.id
    ),
    condition,
    assignee: (bo.get && bo.get('flowable:assignee')) || bo.assignee || '',
    candidateGroups: (bo.get && bo.get('flowable:candidateGroups')) || bo.candidateGroups || '',
    formKey: nodeFormMap.value[bo.id] && nodeFormMap.value[bo.id].formKey ? nodeFormMap.value[bo.id].formKey : (bo.get && bo.get('flowable:formKey')) || bo.formKey || '',
    nodeFormName: nodeFormMap.value[bo.id] ? nodeFormMap.value[bo.id].formName || '' : (bo.get && bo.get('flowable:nodeFormName')) || (bo.$attrs && bo.$attrs['flowable:nodeFormName']) || '',
    nodeFormSnapshot: nodeFormMap.value[bo.id] ? { fields: nodeFormMap.value[bo.id].fields || [], grid: nodeFormMap.value[bo.id].grid || null } : readNodeFormSnapshot(bo),
    opMenus: parseOpMenus(bo),
    opMenuDetails: parseOpMenuDetails(bo),
    signOpinion: parseSignOpinion(bo),
    signOpinionJson: parseSignOpinionJson(bo),
    allowRecall: parseAllowRecall(bo),
    fieldPerms: parseFieldPerms(bo),
    multiInstance: !!bo.loopCharacteristics,
    ...(isUserTask || isStart || isEnd ? parseParticipantConfig(bo) : {}),
    ...(isUserTask ? parseCcConfig(bo) : { ccParticipants: [], ccTiming: 'enter' }),
    // 分支语义为界面派生值，不持久化到 XML
    branchRole: deriveBranchRole(el.type, bo.id),
  };
}

function updateElementProp(key, value) {
  if (!selectedEl) return;
  currentModeling().updateProperties(selectedEl, { [key]: value });
  // 节点改名后按新名字重新适配宽度，避免长名字挤在 100 宽的小框里
  if (key === 'name' && selectedElSource === 'graphic') normalizeNodeSize(selectedEl);
}

function updateFormKey() {
  if (!selectedEl) return;
  const bo = selectedEl.businessObject;
  currentModeling().updateModdleProperties(selectedEl, bo, { 'flowable:formKey': selectedElement.value.formKey || undefined });
}

// ===== 分支语义（界面派生状态，不写回 XML，引擎零影响） =====
const BRANCH_ROLES = [
  { value: 'none', label: '一般' },
  { value: 'fork', label: '分支起点' },
  { value: 'join', label: '分支合并点' },
];
/** 取节点出/入口数量（用于派生分支语义与出口提示） */
function countNodeLinks(nodeId) {
  let out = 0,
    inn = 0;
  graphicExits.value.forEach((e) => {
    if (e.sourceId === nodeId) out++;
    if (e.targetId === nodeId) inn++;
  });
  return { out, inn };
}
/** 派生分支语义：网关类或出口≥2 → 分支起点；入口≥2 → 合并点；否则一般 */
function deriveBranchRole(type, nodeId) {
  const { out, inn } = countNodeLinks(nodeId);
  if (isGatewayType(type) || out >= 2) return 'fork';
  if (inn >= 2) return 'join';
  return 'none';
}
const branchRoleTip = computed(() => {
  const se = selectedElement.value;
  if (!se) return '';
  const { out, inn } = countNodeLinks(se.id);
  const real = deriveBranchRole(se.type, se.id);
  const roleText = { none: '一般（单进单出）', fork: '分支起点', join: '分支合并点' }[real] || '一般';
  const gap = se.branchRole && se.branchRole !== real ? '<br/>当前形状实际为「' + roleText + '」，与所选不一致：请在出口信息中增删连线使其一致' : '';
  return '出口 ' + out + ' 条 / 入口 ' + inn + ' 条，实际形状：' + roleText + gap;
});
function updateBranchRole() {
  // 语义标签仅前端派生展示，不修改任何 BPMN 属性（引擎零影响）
  const se = selectedElement.value;
  if (!se) return;
  const real = deriveBranchRole(se.type, se.id);
  if (se.branchRole !== real) {
    proxy.$modal.msgWarning('分支语义由图形形状派生：请在「出口信息」中增减连线后再确认');
  }
}
/** 节点信息表格用：按节点行派生分支语义文案 */
function branchRoleLabel(row) {
  const role = deriveBranchRole(row.type, row.id);
  return { none: '一般', fork: '分支起点', join: '分支合并点' }[role] || '一般';
}

// ===== 节点字段权限（存节点扩展属性 flowable:fieldPerms，随 BPMN XML 保存部署） =====
const fieldPermsOpen = ref(false);
const fieldPermsLoading = ref(false);
const fieldPermsNodeName = ref('');
const fieldPermsRows = ref([]);

function parseFieldPerms(bo) {
  const raw = (bo.get && bo.get('flowable:fieldPerms')) || bo.fieldPerms || '';
  if (!raw) return {};
  try {
    return JSON.parse(raw) || {};
  } catch (e) {
    return {};
  }
}
const fieldPermsSummary = computed(() => {
  if (!selectedElement.value || !selectedElement.value.isUserTask) return '';
  const perms = selectedElement.value.fieldPerms || {};
  const keys = Object.keys(perms);
  if (!keys.length) return '未配置：默认全部只读展示';
  const edit = keys.filter((k) => perms[k] === 'edit').length;
  const hidden = keys.filter((k) => perms[k] === 'hidden').length;
  return `可编辑 ${edit} / 只读 ${keys.length - edit - hidden} / 隐藏 ${hidden}`;
});

async function openFieldPerms() {
  if (!selectedEl) return;
  fieldPermsNodeName.value = selectedElement.value.name || selectedElement.value.id;
  const perms = parseFieldPerms(selectedEl.businessObject);
  copyPermFrom.value = '';
  fieldPermsOpen.value = true;
  fieldPermsLoading.value = true;
  try {
    // 模型绑定的表单字段（模型需先关联表单）
    const formId = designModel.value.formId;
    if (!formId) {
      fieldPermsRows.value = [];
      return;
    }
    const res = await request({ url: '/flowable/form/' + formId, method: 'get' });
    const f = res.data || {};
    let fields = [];
    try {
      fields = JSON.parse(f.fieldsJson || '[]');
    } catch (e) {}
    fieldPermsRows.value = fields.map((fd) => ({ label: fd.label, field: fd.field, perm: perms[fd.field] || 'readonly' }));
  } catch (e) {
    fieldPermsRows.value = [];
  } finally {
    fieldPermsLoading.value = false;
  }
}

// ===== 复制其他节点权限 =====
const copyPermFrom = ref('');

/** 画布上其他已配置字段权限的用户任务节点 */
const otherPermNodes = computed(() => {
  if (!bpmnModeler) return [];
  try {
    const canvas = bpmnModeler.get('canvas');
    const root = canvas.getRootElement();
    const nodes = [];
    for (const el of root.children || []) {
      if (el.type !== 'bpmn:UserTask' || el.id === selectedElement.value?.id) continue;
      const perms = parseFieldPerms(el.businessObject);
      const keys = Object.keys(perms);
      if (!keys.length) continue;
      const edit = keys.filter((k) => perms[k] === 'edit').length;
      const hidden = keys.filter((k) => perms[k] === 'hidden').length;
      nodes.push({ id: el.id, name: el.businessObject.name || el.id, perms, summary: `可编辑${edit}/只读${keys.length - edit - hidden}/隐藏${hidden}` });
    }
    return nodes;
  } catch (e) {
    return [];
  }
});

function applyCopyPerms() {
  const src = otherPermNodes.value.find((n) => n.id === copyPermFrom.value);
  if (!src || !fieldPermsRows.value.length) return;
  for (const r of fieldPermsRows.value) {
    r.perm = src.perms[r.field] || 'readonly';
  }
  proxy.$modal.msgSuccess('已复制「' + src.name + '」的权限配置，可继续调整后确定');
}

function saveFieldPerms() {
  if (!selectedEl) {
    fieldPermsOpen.value = false;
    return;
  }
  const perms = {};
  for (const r of fieldPermsRows.value) {
    if (r.perm !== 'readonly') perms[r.field] = r.perm; // 只读为默认态，仅存差异项，保持 XML 精简
  }
  const json = Object.keys(perms).length ? JSON.stringify(perms) : undefined;
  modeling.updateModdleProperties(selectedEl, selectedEl.businessObject, { 'flowable:fieldPerms': json });
  // bpmn-js moddle 未知属性可能不序列化，兜底写入 extensionAttributes
  try {
    const bo = selectedEl.businessObject;
    bo.$attrs['flowable:fieldPerms'] = json;
  } catch (e) {}
  selectedElement.value.fieldPerms = perms;
  fieldPermsOpen.value = false;
  proxy.$modal.msgSuccess('字段权限已保存到节点，流程保存后生效');
}

// ===== 节点参与者配置（通用 7 项 + 表单字段 4 项，统一扁平「关系对象」） =====
// picker 决定下方选择器：user=人员 / dept=部门 / role=角色 / post=岗位 / node=节点（含申请节点、上一节点办理人）/ field=表单字段
// 原「发起人」「上一节点办理人」两个无需选对象的独立选项已并入「流程节点操作人」的节点下拉：
// 申请节点 → 发起人；上一节点办理人（自动）→ 运行期动态锚点。存量配置仍按 LEGACY_RELATION_LABELS 正常回显与解析。
const RELATION_GROUPS = [
  {
    key: 'general',
    label: '通用',
    options: [
      { type: 'user', label: '指定人员', picker: 'user' },
      { type: 'dept', label: '指定部门', picker: 'dept' },
      { type: 'deptLeader', label: '指定部门领导', picker: 'dept' },
      { type: 'deptChargeLeader', label: '指定部门分管领导', picker: 'dept' },
      { type: 'role', label: '指定角色', picker: 'role' },
      { type: 'post', label: '指定岗位', picker: 'post' },
      { type: 'nodeUser', label: '流程节点操作人', picker: 'node' },
      { type: 'allUsers', label: '所有人', picker: 'none' },
    ],
  },
  {
    key: 'field',
    label: '流程表单',
    options: [
      { type: 'fieldUser', label: '所选人员', picker: 'field' },
      { type: 'fieldUserLeader', label: '所选人员部门领导', picker: 'field' },
      { type: 'fieldUserChargeLeader', label: '所选人员分管领导', picker: 'field' },
      { type: 'fieldDeptUsers', label: '所选组织部门人员', picker: 'field' },
      { type: 'fieldDeptLeader', label: '所选组织部门领导', picker: 'field' },
      { type: 'fieldDeptChargeLeader', label: '所选组织分管领导', picker: 'field' },
    ],
  },
];
const RELATION_LABELS = { nodeUserLeader: '流程节点操作人的部门领导', nodeUserChargeLeader: '流程节点操作人的分管领导' };
RELATION_GROUPS.forEach((g) =>
  g.options.forEach((o) => {
    RELATION_LABELS[o.type] = o.label;
  }),
);
/** 旧版本已保存的关系类型，仅用于列表回显（后端仍按原语义解析） */
const LEGACY_RELATION_LABELS = {
  // 已并入「流程节点操作人」节点下拉的两个独立关系（存量节点的配置照常显示、照常解析）
  initiator: '发起人（申请节点）',
  activityUser: '上一节点办理人',
  deptParentLeader: '指定部门领导',
  deptOrgLeader: '指定部门领导',
  initiatorDeptAndBelow: '流程发起人所属组织',
  activityUserLeader: '上一节点办理人的部门领导',
  activityUserOrgLeader: '上一节点办理人的部门领导',
  activityUserChargeLeader: '上一节点办理人的分管领导',
  prevAssigneeLeader: '上一节点办理人的部门领导',
  currentUserLeader: '当前办理人的部门领导',
  userLeader: '所选人员的部门领导',
  userOrgLeader: '所选人员的部门领导',
  userChargeLeader: '所选人员的分管领导',
  fieldDeptParentLeader: '所选组织部门领导',
  field: '表单字段',
};
const participantOpen = ref(false);
const participantSaving = ref(false);
const formFieldOptions = ref([]);
const pcUserPickerRef = ref();
const pcDeptPickerRef = ref();
const pcRolePickerRef = ref();
const pcPositionPickerRef = ref();
const pcFallbackPickerRef = ref();
const participantDraft = reactive({
  scope: 'handler',
  timing: 'enter',
  relCategory: 'general', // 关系对象一级分类：通用 / 流程表单
  relType: 'user', // 关系对象 type
  nodeRelation: 'self', // 节点操作人子关系：本人 / 部门领导 / 分管领导
  pickIds: [], // 已选对象ID（人员/部门/角色/岗位）
  pickNames: [], // 已选对象名称
  nodeId: '',
  fieldKey: '',
  fallbackIds: [], // 兜底办理人（节点级）
  fallbackNames: [],
  items: [],
});
/** 当前一级分类下的二级关系选项 */
const pcRelationOptions = computed(() => (RELATION_GROUPS.find((g) => g.key === participantDraft.relCategory) || RELATION_GROUPS[0]).options);
const pcRelationHint = computed(() =>
  participantDraft.relCategory === 'field'
    ? '取表单浏览按钮字段的值：人员字段取所选人员、其部门领导或分管领导；组织字段取所选部门的人员、部门领导或分管领导'
    : '按人员、部门、角色、岗位等范围取人',
);
const pcRelationMeta = computed(() => pcRelationOptions.value.find((o) => o.type === participantDraft.relType) || pcRelationOptions.value[0]);
const pcPickerKind = computed(() => pcRelationMeta.value.picker);
/** 流程表单：组织类选项只允许部门字段，人员类选项只允许人员字段 */
const pcFieldWantDept = computed(() => ['fieldDeptUsers', 'fieldDeptLeader', 'fieldDeptChargeLeader'].includes(participantDraft.relType));
const pcFieldOptions = computed(() => formFieldOptions.value.filter((f) => f.fieldType === (pcFieldWantDept.value ? 'dept' : 'user')));
/** 流程表单取值说明：按当前关系对象说明运行期取到的是谁 */
const pcFieldValueHint = computed(() => {
  const map = {
    fieldUser: '运行时取该人员字段的值，即所选人员本人',
    fieldUserLeader: '运行时取该人员字段所选人员所属部门的领导',
    fieldUserChargeLeader: '运行时取该人员字段所选人员所属部门的分管领导',
    fieldDeptUsers: '运行时取该部门字段所选部门下的人员（本部门）',
    fieldDeptLeader: '运行时取该部门字段所选部门的领导',
    fieldDeptChargeLeader: '运行时取该部门字段所选部门的分管领导',
  };
  return map[participantDraft.relType] || '运行时取该字段的值推导出办理人';
});
/** 部门多选（仅「指定部门」支持多选；领导类只允许单选部门） */
const pcDeptMultiple = computed(() => participantDraft.relType === 'dept');
const pcPickedText = computed(() => (participantDraft.pickNames.length ? participantDraft.pickNames.join('、') : '未选择'));
/** 「流程节点操作人」下拉里的虚拟节点：上一节点办理人（运行期动态取值，不绑定具体节点） */
const PREV_NODE_ID = '__prev_assignee__';
/** 可选节点：申请（开始）节点 + 流程中的用户任务 + 上一节点办理人（动态）
 * 申请节点没有任务记录，后端按「流程发起人」解析（可衍生成「申请节点的部门领导」）；
 * 上一节点办理人由后端取最近一个已完成任务的办理人（首个审批环节回退为发起人） */
const participantNodeOptions = computed(() => {
  const list = (graphicNodes.value || [])
    .filter((n) => n.isUserTask || n.type === 'bpmn:UserTask' || n.isStart)
    .map((n) => ({
      ...n,
      optionLabel: n.isStart ? (n.name || '申请') + '（申请节点）' : n.name || n.id,
    }));
  list.push({ id: PREV_NODE_ID, name: '上一节点办理人', optionLabel: '上一节点办理人（自动）', virtual: true });
  return list;
});
/** 节点本人 / 部门领导 单选文案：虚拟节点换成「上一节点办理人」的说法 */
const pcNodeSelfLabel = computed(() => (participantDraft.nodeId === PREV_NODE_ID ? '上一节点办理人' : '节点本人'));
const pcNodeLeaderLabel = computed(() => (participantDraft.nodeId === PREV_NODE_ID ? '上一节点办理人的部门领导' : '节点本人的部门领导'));
const pcNodeChargeLeaderLabel = computed(() => (participantDraft.nodeId === PREV_NODE_ID ? '上一节点办理人的分管领导' : '节点本人的分管领导'));

/** 处理方式 / 办理方式：当前选中项的名称与说明（弹窗分组提示联动） */
const HANDLE_MODE_META = {
  assign: { label: '指定', tip: '按配置直接算出办理人：会签/串签每人一个任务；抢占解析出多人时进入候选池' },
  runtime: { label: '提交时指定', tip: '上一环节提交时从算出的范围内指定 1 人办理，未指定则退回候选池' },
};
const PERFORM_MODE_META = {
  and: { label: '会签', tip: '范围内多人并行办理，全部办完节点才通过' },
  seq: { label: '串签', tip: '按配置顺序依次办理，所有人办完节点才通过' },
  claim: { label: '抢占', tip: '任务只有一份，范围内谁先办理谁完成；可配合「提交时指定」' },
};
const currentHandleModeLabel = computed(() => (HANDLE_MODE_META[(selectedElement.value || {}).handleMode] || HANDLE_MODE_META.assign).label);
const handleModeTip = computed(() => (HANDLE_MODE_META[(selectedElement.value || {}).handleMode] || HANDLE_MODE_META.assign).tip);
const currentPerformModeLabel = computed(() => (PERFORM_MODE_META[(selectedElement.value || {}).performMode] || PERFORM_MODE_META.claim).label);
const performModeTip = computed(() => (PERFORM_MODE_META[(selectedElement.value || {}).performMode] || PERFORM_MODE_META.claim).tip);

/** 读取节点参与者配置，兼容旧的 assignee / candidateGroups 写法 */
function parseParticipantConfig(bo) {
  let cfg = {};
  const raw = (bo.get && bo.get('flowable:participantConfig')) || (bo.$attrs && bo.$attrs['flowable:participantConfig']) || '';
  if (raw) {
    try {
      cfg = JSON.parse(raw) || {};
    } catch (e) {}
  }
  let participants = Array.isArray(cfg.participants) ? cfg.participants : [];
  if (!participants.length) {
    const a = (bo.get && bo.get('flowable:assignee')) || '';
    const g = (bo.get && bo.get('flowable:candidateGroups')) || '';
    if (a) {
      const u = userOptions.value.find((x) => String(x.userId) === String(a));
      participants = [{ mode: 'org', type: 'user', kind: 'user', id: String(a), name: u ? u.nickName : a.indexOf('${') > -1 ? '指定办理人 ' + a : '用户 ' + a }];
    } else if (g) {
      participants = String(g)
        .split(',')
        .filter(Boolean)
        .map((id) => {
          const r = roleOptions.value.find((x) => String(x.roleId) === String(id));
          return { mode: 'org', type: 'role', kind: 'role', id: String(id), name: r ? r.roleName : '角色 ' + id };
        });
    }
  }
  // 历史配置的「候选」与「提交时指定」同属候选池语义，统一归并到「提交时指定」
  const rawHandleMode = cfg.handleMode || (bo.get && bo.get('flowable:candidateGroups') ? 'candidate' : 'assign');
  const handleMode = rawHandleMode === 'candidate' ? 'runtime' : rawHandleMode;
  const loop = bo.loopCharacteristics;
  const performMode = cfg.performMode || (loop ? (loop.isSequential ? 'seq' : 'and') : 'claim');
  const participantMode = cfg.participantMode || (participants.length > 1 ? 'multi' : 'single');
  return {
    handleMode,
    performMode,
    participantMode,
    participants,
    fallbackUserIds: Array.isArray(cfg.fallbackUserIds) ? cfg.fallbackUserIds : [],
    fallbackNames: Array.isArray(cfg.fallbackNames) ? cfg.fallbackNames : [],
  };
}

/** 当前编辑实例的 moddle（画布编辑用 graphicModeler，只读预览用 bpmnModeler） */
function currentModdle() {
  const m = selectedElSource === 'graphic' && graphicModeler ? graphicModeler : bpmnModeler;
  return m ? m.get('moddle') : null;
}

/** 关系对象的中文名称（兼容旧配置） */
function relationLabel(item) {
  if (!item) return '';
  return RELATION_LABELS[item.type] || LEGACY_RELATION_LABELS[item.type] || item.type || '';
}

/** 操作人/抄送人显示名称：存量配置的 name 里可能还是旧文案，统一显示为新关系名 */
const LEGACY_NAME_TEXT = [
  ['所选字段中人员的部门领导', '所选人员部门领导'],
  ['所选字段中组织的分管领导', '所选组织分管领导'],
  ['所选字段中组织的领导', '所选组织部门领导'],
  ['所选字段中的人员', '所选人员'],
];
function participantNameText(p) {
  let text = String((p && p.name) || '').replace(/上级领导/g, '部门领导');
  LEGACY_NAME_TEXT.forEach(([from, to]) => {
    text = text.split(from).join(to);
  });
  return text;
}

function openParticipantPicker(scope) {
  const se = selectedElement.value;
  if (!isConfigurableNode(se)) return;
  const target = scope === 'cc' ? 'cc' : 'handler';
  const source = target === 'cc' ? se.ccParticipants || [] : se.participants || [];
  participantDraft.scope = target;
  participantDraft.timing = se.ccTiming || 'enter';
  participantDraft.relCategory = 'general';
  participantDraft.relType = 'user';
  participantDraft.nodeRelation = 'self';
  participantDraft.pickIds = [];
  participantDraft.pickNames = [];
  participantDraft.nodeId = '';
  participantDraft.fieldKey = '';
  participantDraft.fallbackIds = (se.fallbackUserIds || []).slice();
  participantDraft.fallbackNames = (se.fallbackNames || []).slice();
  participantDraft.items = source.map((p) => Object.assign({}, p));
  participantOpen.value = true;
  loadFormFieldOptions();
}

/** 抄送时机切换：立即写回节点配置，避免只改弹窗内值不生效 */
function onCcTimingChange() {
  const se = selectedElement.value;
  if (!se) return;
  se.ccTiming = participantDraft.timing;
  persistCcConfig();
}

/** 切换关系对象一级分类：默认选中该分类下第一个二级选项，并清空已选 */
function onRelCategoryChange(key) {
  const group = RELATION_GROUPS.find((g) => g.key === key) || RELATION_GROUPS[0];
  participantDraft.relType = group.options[0].type;
  onRelTypeChange();
}

/** 切换关系对象时清空已选，避免不同类型之间串值 */
function onRelTypeChange() {
  participantDraft.pickIds = [];
  participantDraft.pickNames = [];
  participantDraft.nodeId = '';
  participantDraft.fieldKey = '';
}

/** 回显给选择器组件时把数字ID还原为 number（组件内部按数值比较） */
function toPickerIds(ids) {
  return (ids || []).map((v) => (/^\d+$/.test(String(v)) ? Number(v) : v));
}

function openPcUserPicker() {
  if (pcUserPickerRef.value) pcUserPickerRef.value.open(toPickerIds(participantDraft.pickIds));
}
function onPcUsersPicked(users) {
  const list = Array.isArray(users) ? users : [users];
  participantDraft.pickIds = list.map((u) => String(u.userId));
  participantDraft.pickNames = list.map((u) => u.nickName || u.userName);
}

function openPcDeptPicker() {
  const ids = toPickerIds(participantDraft.pickIds);
  const cur = pcDeptMultiple.value ? ids : ids[0] || null;
  if (pcDeptPickerRef.value) pcDeptPickerRef.value.open(cur);
}
function onPcDeptsPicked(depts) {
  const list = (Array.isArray(depts) ? depts : [depts]).filter((d) => d && d.deptId);
  participantDraft.pickIds = list.map((d) => String(d.deptId));
  participantDraft.pickNames = list.map((d) => d.deptName);
}

function openPcRolePicker() {
  if (pcRolePickerRef.value) pcRolePickerRef.value.open(toPickerIds(participantDraft.pickIds));
}
function onPcRolesPicked(roles) {
  const list = Array.isArray(roles) ? roles : [roles];
  participantDraft.pickIds = list.map((r) => String(r.roleId));
  participantDraft.pickNames = list.map((r) => r.roleName);
}

function openPcPostPicker() {
  if (pcPositionPickerRef.value) pcPositionPickerRef.value.open(toPickerIds(participantDraft.pickIds));
}
function onPcPostsPicked(posts) {
  const list = Array.isArray(posts) ? posts : [posts];
  participantDraft.pickIds = list.map((p) => String(p.postId || p.positionId));
  participantDraft.pickNames = list.map((p) => p.postName || p.positionName);
}

/** 兜底办理人：解析不到人时转交（节点级，即时写回 selectedElement） */
function openPcFallbackPicker() {
  const ids = toPickerIds(participantDraft.fallbackIds);
  // 单选弹窗：回显必须是单个ID，传数组会被当成"已选中该项"
  if (pcFallbackPickerRef.value) pcFallbackPickerRef.value.open(ids[0] || null);
}
function onPcFallbackPicked(users) {
  const list = (Array.isArray(users) ? users : [users]).filter((u) => u && u.userId);
  if (!list.length) return;
  participantDraft.fallbackIds = list.map((u) => String(u.userId));
  participantDraft.fallbackNames = list.map((u) => u.nickName || u.userName);
  const se = selectedElement.value;
  if (se) {
    se.fallbackUserIds = participantDraft.fallbackIds.slice();
    se.fallbackNames = participantDraft.fallbackNames.slice();
  }
}
function clearPcFallback() {
  participantDraft.fallbackIds = [];
  participantDraft.fallbackNames = [];
  const se = selectedElement.value;
  if (se) {
    se.fallbackUserIds = [];
    se.fallbackNames = [];
  }
}

function loadFormFieldOptions() {
  const formId = designModel.value.formId;
  formFieldOptions.value = [];
  if (!formId) return;
  request({ url: '/flowable/form/' + formId, method: 'get' })
    .then((res) => {
      let fields = [];
      try {
        fields = JSON.parse((res.data || {}).fieldsJson || '[]');
      } catch (e) {}
      formFieldOptions.value = fields.map((f) => ({
        // 保留字段原始定义（form/dtype/selectOptions 等）：分支条件的值控件要按字段类型渲染
        ...f,
        label: f.label || f.field,
        field: f.field,
        // 浏览按钮字段带值类型（人员/部门），供运行时推导领导关系
        fieldType: f.form === 'browse' ? f.browseType || 'user' : 'auto',
      }));
    })
    .catch(() => {
      formFieldOptions.value = [];
    });
}

/** 同一关系 + 同一对象重复添加时给出提示 */
function isDuplicateParticipant(item) {
  return participantDraft.items.some(
    (p) =>
      p.type === item.type &&
      String(p.nodeId || '') === String(item.nodeId || '') &&
      String(p.field || '') === String(item.field || '') &&
      String((p.ids || [p.id]).join(',')) === String((item.ids || [item.id]).join(',')),
  );
}

/** 按当前关系对象 + 选择结果生成一条操作人配置 */
function addParticipantFromDraft() {
  const d = participantDraft;
  const meta = pcRelationMeta.value;
  let item;
  if (meta.picker === 'node') {
    if (!d.nodeId) {
      proxy.$modal.msgWarning('请先选择节点');
      return;
    }
    const relation = d.nodeRelation || 'self';
    const leader = relation === 'leader';
    const chargeLeader = relation === 'chargeLeader';
    const relationText = chargeLeader ? '分管领导' : leader ? '部门领导' : '本人';
    if (d.nodeId === PREV_NODE_ID) {
      // 虚拟节点「上一节点办理人（自动）」：由运行期上下文决定，不绑定具体节点
      item = {
        type: chargeLeader ? 'activityUserChargeLeader' : leader ? 'activityUserLeader' : 'activityUser',
        mode: 'activity',
        relation,
        name: '上一节点办理人 · ' + relationText,
      };
    } else {
      const n = participantNodeOptions.value.find((x) => x.id === d.nodeId);
      item = {
        type: chargeLeader ? 'nodeUserChargeLeader' : leader ? 'nodeUserLeader' : 'nodeUser',
        mode: 'node',
        nodeId: d.nodeId,
        relation,
        name: (n ? n.name || n.id : d.nodeId) + ' · ' + relationText,
      };
    }
  } else if (meta.picker === 'none') {
    // 「所有人」：不需要选对象，运行时解析为全部启用人员
    item = { type: 'allUsers', mode: 'all', name: '所有人' };
  } else if (meta.picker === 'field') {
    if (!d.fieldKey) {
      proxy.$modal.msgWarning('请先选择表单字段');
      return;
    }
    const f = formFieldOptions.value.find((x) => x.field === d.fieldKey);
    item = {
      type: d.relType,
      mode: 'field',
      field: d.fieldKey,
      fieldType: f ? f.fieldType : 'auto',
      name: (f ? f.label : d.fieldKey) + ' · ' + meta.label,
    };
  } else {
    if (!d.pickIds.length) {
      proxy.$modal.msgWarning('请先在弹窗中选择对象');
      return;
    }
    const names = d.pickNames.length ? d.pickNames : d.pickIds;
    let name = names.join('、');
    if (d.relType === 'deptLeader') {
      name = name + ' · 部门领导';
    } else if (d.relType === 'deptChargeLeader') {
      name = name + ' · 分管领导';
    }
    // 指定部门 = 该部门本身的人员，不含下级部门
    item = { type: d.relType, mode: 'org', ids: d.pickIds.slice(), id: d.pickIds[0], name };
  }
  if (isDuplicateParticipant(item)) {
    proxy.$modal.msgWarning('该操作人已添加');
    return;
  }
  d.items.push(item);
  d.pickIds = [];
  d.pickNames = [];
  d.fieldKey = '';
}

/** 处理方式切换：抢占+指定只能落 1 人，多人需用会签/串签或提交时指定 */
function onHandleModeChange(mode) {
  const se = selectedElement.value;
  if (!se) return;
  const ps = se.participants || [];
  const claim = (se.performMode || 'claim') === 'claim';
  if (mode === 'assign' && claim && ps.length > 1) {
    se.handleMode = 'runtime';
    proxy.$modal.msgWarning('抢占节点「指定」只能有 1 条操作人配置；多人请改办理方式为会签/串签，或改用「提交时指定」');
    return;
  }
  persistParticipantConfig();
}

/** 办理方式切换：会签/串签按人落任务，「提交时指定」（认领池）不适用，自动改回指定 */
function onPerformModeChange(mode) {
  const se = selectedElement.value;
  if (!se) return;
  if (mode !== 'claim' && (se.handleMode || 'assign') !== 'assign') {
    se.handleMode = 'assign';
    proxy.$modal.msgSuccess('会签/串签按人逐个生成任务，「提交时指定」仅抢占可用，已改为「指定」');
  }
  persistParticipantConfig();
}

function saveParticipantConfig() {
  if (!selectedEl) {
    participantOpen.value = false;
    return;
  }
  const items = participantDraft.items || [];
  participantSaving.value = true;
  try {
    if (participantDraft.scope === 'cc') {
      selectedElement.value.ccParticipants = items.map((p) => Object.assign({}, p));
      selectedElement.value.ccTiming = participantDraft.timing || 'enter';
      persistCcConfig();
      participantOpen.value = false;
      proxy.$modal.msgSuccess('抄送人已保存到节点，流程保存后生效');
      return;
    }
    const claim = (selectedElement.value.performMode || 'claim') === 'claim';
    if (claim && (selectedElement.value.handleMode || 'assign') === 'assign' && items.length > 1) {
      proxy.$modal.msgWarning('抢占节点「指定」只能有 1 条操作人配置；多人请把办理方式改为会签/串签，或把处理方式改为「提交时指定」');
      return;
    }
    selectedElement.value.participants = items.map((p) => Object.assign({}, p));
    persistParticipantConfig();
    participantOpen.value = false;
    proxy.$modal.msgSuccess('操作人已保存到节点，流程保存后生效');
  } finally {
    participantSaving.value = false;
  }
}

/** 读取节点抄送配置（flowable:ccConfig） */
function parseCcConfig(bo) {
  let cfg = {};
  const raw = (bo.get && bo.get('flowable:ccConfig')) || (bo.$attrs && bo.$attrs['flowable:ccConfig']) || '';
  if (raw) {
    try {
      cfg = JSON.parse(raw) || {};
    } catch (e) {}
  }
  return {
    ccParticipants: Array.isArray(cfg.participants) ? cfg.participants : [],
    ccTiming: cfg.timing || 'enter',
  };
}

/** 把节点抄送配置写回 XML（无抄送人时清除属性） */
function persistCcConfig() {
  const se = selectedElement.value;
  if (!se || !se.isUserTask || !selectedEl) return;
  const m = currentModeling();
  if (!m) return;
  const bo = selectedEl.businessObject;
  const participants = se.ccParticipants || [];
  if (!participants.length) {
    m.updateModdleProperties(selectedEl, bo, { 'flowable:ccConfig': undefined });
    try {
      delete bo.$attrs['flowable:ccConfig'];
    } catch (e) {}
    return;
  }
  const json = JSON.stringify({ timing: se.ccTiming || 'enter', participants });
  m.updateModdleProperties(selectedEl, bo, { 'flowable:ccConfig': json });
  try {
    bo.$attrs['flowable:ccConfig'] = json;
  } catch (e) {}
}

/** 把节点参与者配置写回 XML，并派生 assignee / 多实例 */
function persistParticipantConfig() {
  const se = selectedElement.value;
  if (!isConfigurableNode(se) || !selectedEl) return;
  const participants = se.participants || [];
  const cfg = {
    handleMode: se.handleMode || 'assign',
    performMode: se.performMode || 'claim',
    participantMode: participants.length > 1 ? 'multi' : 'single',
    participants,
  };
  // 兜底办理人：解析不到人时转交，避免节点无人可办 / 会签被跳过
  if ((se.fallbackUserIds || []).length) {
    cfg.fallbackUserIds = se.fallbackUserIds.slice();
    cfg.fallbackNames = (se.fallbackNames || []).slice();
  }
  const json = JSON.stringify(cfg);
  const m = currentModeling();
  if (!m) return;
  const bo = selectedEl.businessObject;
  m.updateModdleProperties(selectedEl, bo, { 'flowable:participantConfig': json });
  try {
    bo.$attrs['flowable:participantConfig'] = json;
  } catch (e) {}

  const moddle = currentModdle();
  const isMulti = se.isUserTask && cfg.performMode !== 'claim';
  // 会签/串签：集合由后端表达式现算（引擎需要时回调 participantCollection.collectionFor），
  // 只有进入节点前必须备好变量才会有时序风险；实例办理人由 ${participantAssignee} 赋值
  const collectionVar = "${participantCollection.collectionFor('" + bo.id + "', execution)}";

  // 只有「指定 + 单一固定人员」才直接写 assignee；其余一律由 ParticipantResolveListener
  // 在任务创建时按 participantConfig 解析派发（部门/角色/岗位/节点/字段/领导类都走解析器）
  const fixedUsers = participants.filter((p) => p.type === 'user' && ((p.ids && p.ids.length) || p.id));
  let assignee = '';
  if (cfg.handleMode === 'assign' && participants.length === 1) {
    if (fixedUsers.length === 1) assignee = fixedUsers[0].ids ? fixedUsers[0].ids[0] : fixedUsers[0].id;
    else if (participants[0].type === 'initiator') assignee = '${initiator}';
  }
  // 多实例节点的办理人必须来自集合变量，不能被上面的单人 assignee 覆盖
  if (isMulti) assignee = '${participantAssignee}';
  m.updateModdleProperties(selectedEl, bo, {
    'flowable:assignee': assignee || undefined,
    'flowable:candidateGroups': undefined,
  });
  try {
    delete bo.$attrs['flowable:candidateUsers'];
  } catch (e) {}

  // 多实例仅适用于用户任务；开始/结束事件不写 loopCharacteristics，避免生成非法 BPMN
  if (!isMulti) {
    m.updateModdleProperties(selectedEl, bo, { loopCharacteristics: undefined });
  } else if (moddle) {
    const loop = moddle.create('bpmn:MultiInstanceLoopCharacteristics', {
      isSequential: cfg.performMode === 'seq',
      // completionCondition 必须是 bpmn:FormalExpression 元素，传字符串会导致 XML 序列化报 isGeneric
      completionCondition: moddle.create('bpmn:FormalExpression', { body: '${nrOfCompletedInstances >= nrOfInstances}' }),
    });
    // flowable 扩展属性不在 bpmn 标准描述符里，直接写 $attrs（只读对象，不能整体赋值）才能序列化到 XML
    loop.$attrs['flowable:collection'] = collectionVar;
    loop.$attrs['flowable:elementVariable'] = 'participantAssignee';
    m.updateModdleProperties(selectedEl, bo, { loopCharacteristics: loop });
  }

  se.assignee = assignee || '';
  se.candidateGroups = '';
  se.multiInstance = isMulti;
}

function updateCondition() {
  if (!selectedEl) return;
  const bo = selectedEl.businessObject;
  const expr = (selectedElement.value.condition || '').trim();
  const m = currentModeling();
  if (expr) {
    const moddle = currentModdle();
    const cond = moddle.create('bpmn:FormalExpression', { body: expr });
    m.updateProperties(selectedEl, { conditionExpression: cond });
  } else {
    m.updateProperties(selectedEl, { conditionExpression: undefined });
  }
}

function clearCondition() {
  // 智能模式与表达式模式一并清空，规则行也要复位，否则面板上还留着旧条件
  condRows.value = [{ field: '', op: 'eq', value: '' }];
  condJoin.value = 'AND';
  selectedElement.value.condition = '';
  updateCondition();
}

/** 设为/取消该网关的默认出口（写入 BPMN 网关的 default 属性，同一网关只允许一条） */
function setDefaultFlow(val) {
  if (!selectedEl || selectedEl.type !== 'bpmn:SequenceFlow' || !selectedEl.source) return;
  const gateway = selectedEl.source;
  // bpmn-js 的 UpdatePropertiesHandler 会自动把连线元素解包成 businessObject
  currentModeling().updateProperties(gateway, { default: val ? selectedEl : undefined });
  if (val) {
    // 默认出口不参与条件判断，清掉该出口的条件（含智能规则行），避免“配了条件却不生效”的误解
    clearCondition();
  }
  selectedElement.value.isDefaultFlow = !!val;
}

// ===== 工具栏命令 =====
function cmdUndo() {
  bpmnModeler.get('commandStack').undo();
}
function cmdRedo() {
  bpmnModeler.get('commandStack').redo();
}
function zoom(delta) {
  bpmnModeler.get('canvas').zoom(bpmnModeler.get('canvas').zoom() + delta);
}
function zoomReset() {
  bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
}
function deleteSelected() {
  const sel = bpmnModeler.get('selection').get();
  if (!sel.length) {
    proxy.$modal.msgWarning('请先在画布中选中要删除的节点/连线');
    return;
  }
  modeling.removeElements(sel);
}

/** 流程图允许出现的元素类型：左侧面板 6 类节点 + 连线 + 文本注释 */
const SUPPORTED_DIAGRAM_TYPES = [
  'bpmn:StartEvent',
  'bpmn:EndEvent',
  'bpmn:UserTask',
  'bpmn:ExclusiveGateway',
  'bpmn:ParallelGateway',
  'bpmn:InclusiveGateway',
  'bpmn:SequenceFlow',
  'bpmn:TextAnnotation',
];

/** 保存前校验：用户任务办理人配置、结束节点存在性、不支持的节点类型 */
function validateDiagram(modeler) {
  const root = (modeler || bpmnModeler).get('canvas').getRootElement();
  const issues = [];
  let hasEnd = false;
  (root.children || []).forEach((c) => {
    // 跳过名称文字元素：bpmn-js 会把事件/网关/连线的名称渲染成独立的 label 元素，并非流程节点
    if (c.type === 'label' || c.labelTarget) return;
    if (c.type === 'bpmn:EndEvent') hasEnd = true;
    // 通用任务/服务任务/中间事件/子流程等：属性面板无法配置，引擎里也不会生成人工待办
    if (!SUPPORTED_DIAGRAM_TYPES.includes(c.type)) {
      const typeName = NODE_TYPE_LABELS[c.type] || c.type.replace('bpmn:', '');
      issues.push('「' + ((c.businessObject && c.businessObject.name) || c.id) + '」为不支持的节点类型（' + typeName + '），部署后可能被引擎直接跳过或执行报错');
      return;
    }
    if (c.type === 'bpmn:UserTask') {
      const bo = c.businessObject;
      const a = (bo.get && bo.get('flowable:assignee')) || '';
      const g = (bo.get && bo.get('flowable:candidateGroups')) || '';
      const pcfg = (bo.get && bo.get('flowable:participantConfig')) || (bo.$attrs && bo.$attrs['flowable:participantConfig']) || '';
      let hasParticipants = false;
      if (pcfg) {
        try {
          hasParticipants = !!(JSON.parse(pcfg).participants || []).length;
        } catch (e) {}
      }
      if (!a && !g && !hasParticipants && !bo.loopCharacteristics) {
        issues.push('「' + (bo.name || bo.id) + '」未配置办理人或候选组');
      }
    }
    // 多出口网关：既没有条件也没有默认出口时，引擎会按连线顺序固定取第一条，必须提前拦住
    if (c.type === 'bpmn:ExclusiveGateway' || c.type === 'bpmn:InclusiveGateway') {
      const gbo = c.businessObject;
      const outgoing = (c.outgoing || []).map((f) => f.businessObject || f);
      if (outgoing.length > 1) {
        const defaultId = gbo.default && gbo.default.id;
        const conditionOf = (f) => {
          const expr = f.get ? f.get('conditionExpression') : f.conditionExpression;
          return expr && expr.body ? String(expr.body).trim() : '';
        };
        const conditionalCount = outgoing.filter((f) => conditionOf(f)).length;
        if (!conditionalCount && !defaultId) {
          issues.push('网关「' + (gbo.name || gbo.id) + '」有 ' + outgoing.length + ' 条出口，但都没有条件和默认出口，引擎会固定走第一条出口（按连线顺序）');
        } else if (!defaultId && conditionalCount < outgoing.length) {
          issues.push('网关「' + (gbo.name || gbo.id) + '」存在未设置条件的出口且未设置默认出口，将按连线顺序命中第一条无条件的出口，建议把兜底出口设为默认出口');
        }
        if (defaultId) {
          const defFlow = outgoing.find((f) => f.id === defaultId);
          if (defFlow && conditionOf(defFlow)) {
            issues.push('网关「' + (gbo.name || gbo.id) + '」的默认出口「' + (defFlow.name || defFlow.id) + '」仍配置了条件，默认出口不参与条件判断，该条件不会生效');
          }
        }
      }
    }
  });
  if (!hasEnd) issues.push('流程缺少结束节点');
  return issues;
}

/**
 * 保存前校验提醒：有问题时弹窗列出，用户可「仍要保存」或「返回修改」。
 * 返回 true 表示继续保存，false 表示用户选择返回修改。
 */
async function confirmDiagramIssues(issues) {
  if (!issues.length) return true;
  try {
    await ElMessageBox.confirm(
      () =>
        h('div', null, [
          h('p', { style: 'margin-bottom:8px;font-weight:600' }, '存在 ' + issues.length + ' 个问题：'),
          h(
            'ul',
            { style: 'padding-left:18px;margin:0;color:#e6a23c' },
            issues.map((t) => h('li', null, t)),
          ),
          h('p', { style: 'margin-top:10px;color:#909399;font-size:13px' }, '仍可保存，但该流程部署后可能无法正常发起。'),
        ]),
      '保存前校验提醒',
      { confirmButtonText: '仍要保存', cancelButtonText: '返回修改', type: 'warning', draggable: true },
    );
    return true;
  } catch (e) {
    return false; // 用户选择返回修改
  }
}

async function handleDesign(row) {
  const res = await getModel(row.modelId);
  designModel.value = {
    ...res.data,
    // 历史草稿可能已被部署注入的技术节点污染，进入设计器前统一剥离
    bpmnXml: stripInitiatorArtifacts(res.data.bpmnXml || ''),
    deployed: res.data.deployed || row.deployed,
    version: res.data.version || row.version,
    deployMode: res.data.deployMode || 'new',
    locked: !!res.data.locked,
    helpDoc: res.data.helpDoc || '',
    orderNum: res.data.orderNum != null ? res.data.orderNum : 0,
    processNoRule: parseNoRule(res.data.processNoRule),
    skipSameApprover: !!res.data.skipSameApprover,
  };
  loadFormOptions();
  loadDesignVersions();
  loadNodeFormTemplates();
  loadNodePrintTemplates();
  selectedElement.value = null;
  designTab.value = 'flow';
  designOpen.value = true;
  // 编号规则：字段段候选项要随表单预加载（弹框里选「表单字段」段才有下拉）；
  // 「下一号」预览只在弹框打开时算，设计器打开时不再发无意义的预览请求
  loadNoRuleFields(designModel.value.formId);
}

// ===== 版本管理（设计器内版本标签行 + 版本编辑弹框） =====
const designVersions = ref([]);
const designVersionSelected = ref(null);
/** 版本清单弹框（版本多时从清单里选） */
const versionListOpen = ref(false);
/** 标签行最多直接展示的版本数，更早的折叠进清单 */
const VERSION_TAB_LIMIT = 6;
/** 版本倒序（新的在前），清单表与折叠计算共用 */
const versionRowsDesc = computed(() => [...designVersions.value].sort((a, b) => (b.version || 0) - (a.version || 0)));
/**
 * 标签行展示哪些版本：最近 N 个 + 当前活动版本 + 当前选中的版本
 * （后两个可能落在早期，需要单独补回来，否则「当前」标记和选中态会看不见）
 */
const visibleVersionTabs = computed(() => {
  const all = versionRowsDesc.value;
  if (all.length <= VERSION_TAB_LIMIT) return [...all].sort((a, b) => (a.version || 0) - (b.version || 0));
  const picked = all.slice(0, VERSION_TAB_LIMIT);
  const keep = (v) => v && !picked.some((p) => p.modelId === v.modelId) && picked.push(v);
  keep(all.find((v) => v.activeFlag === '1'));
  const selected = designVersionSelected.value;
  keep(selected ? all.find((v) => v.modelId === selected.modelId) : null);
  return picked.sort((a, b) => (a.version || 0) - (b.version || 0));
});
const hiddenVersionCount = computed(() => Math.max(0, designVersions.value.length - visibleVersionTabs.value.length));
/** 清单里选中某版本（后续「查看/载入/编辑」都按 designVersionSelected 取数） */
function pickVersion(row) {
  designVersionSelected.value = row;
  return row;
}
function openVersionEditorFromList(row) {
  versionListOpen.value = false;
  openVersionEditor(pickVersion(row));
}
function openVersionViewFromList(row) {
  pickVersion(row);
  versionListOpen.value = false;
  openVersionView();
}
function loadFromList(row) {
  pickVersion(row);
  versionListOpen.value = false;
  loadVersionIntoDesigner();
}
/** 表单管理二级页签：基本信息/编辑字段 */
const formTab = ref('basic');
/** 表单管理→编辑字段 的编辑器实例：顶部「保存」时顺带提交字段改动，避免字段编辑白改 */
const formFieldsRef = ref(null);
/** 组件内保存字段设计后刷新表单选项（字段数等回显同步） */
function onFormFieldsSaved() {
  loadFormOptions();
  loadDesignVersions();
}

// ===== 流转设置：仅流程图（已移除节点信息/出口信息子页签，节点与连线统一在流程图编辑页维护） =====
const NODE_TYPE_LABELS = {
  'bpmn:StartEvent': '开始',
  'bpmn:EndEvent': '结束',
  'bpmn:UserTask': '审批节点',
  'bpmn:ServiceTask': '服务节点',
  'bpmn:ExclusiveGateway': '排他网关',
  'bpmn:ParallelGateway': '并行网关',
  'bpmn:InclusiveGateway': '包容网关',
  'bpmn:Task': '任务节点',
};

// ===== 业务语义层（简化）：界面只暴露 4 类节点，底层仍写标准 BPMN 元素 =====
/** 界面节点类型（仅用于展示与新建引导，不影响 XML 结构） */
const SEMANTIC_NODE_LABELS = {
  'bpmn:UserTask': '审批节点',
  'bpmn:ExclusiveGateway': '分支节点',
  'bpmn:ParallelGateway': '分支节点',
  'bpmn:StartEvent': '发起节点',
  'bpmn:Task': '发起节点',
  'bpmn:EndEvent': '结束节点',
};
/** 节点语义标签：审批/发起/结束/分支；无法识别的类型（如历史脏数据 label）兜底为「未识别」 */
function semanticNodeLabel(type) {
  if (!type) return '未识别';
  if (SEMANTIC_NODE_LABELS[type]) return SEMANTIC_NODE_LABELS[type];
  if (type.includes('Gateway')) return '分支节点';
  const t = type.replace('bpmn:', '');
  // 已知 BPMN 元素但不在业务语义内（服务节点/通用任务等）→ 归入未识别，不再作为业务节点引导
  return NODE_TYPE_LABELS[type] ? t : '未识别';
}
/** 是否网关类元素（分支/汇聚） */
function isGatewayType(type) {
  return !!type && type.includes('Gateway');
}

/** 是否允许在该行直接改类型：仅网关/审批/发起/结束等可换型元素，连线与流程根不参与 */
function isTypeChangeable(row) {
  if (!row || !row.type) return false;
  if (row.type === 'bpmn:SequenceFlow') return false;
  // 用字面量判定，避免依赖后续才声明的 ADD_NODE_TYPES（const 暂时性死区会导致首渲染抛错）
  return ['bpmn:UserTask', 'bpmn:ExclusiveGateway', 'bpmn:StartEvent', 'bpmn:EndEvent'].includes(row.type);
}

/**
 * 表格内直接切换节点类型（bpmn-js 不支持原地改型，采用「同位置重建 + 配置迁移 + 连线重建」）：
 * 1) 读取原节点的全部扩展属性（办理人/候选组/表单Key/字段权限/操作组/多实例/出口元数据）与所有入出连线；
 * 2) 记录坐标后在原位置删除旧元素、新建目标类型元素；
 * 3) 把扩展属性与节点名迁移到新元素；
 * 4) 按原方向重连所有入出连线，并恢复连线名称与分支条件；
 * 5) 重新应用画布标记与退回路由。
 * 注意：节点 ID 会变化（bpmn-js 重建的必然结果），保存后以新 ID 落库。
 */
async function changeNodeType(row) {
  if (!graphicModeler || !row) return;
  const registry = graphicModeler.get('elementRegistry');
  const oldEl = registry.get(row.id);
  if (!oldEl) {
    proxy.$modal.msgWarning('节点已不存在，请刷新后重试');
    return;
  }
  const targetType = row.type;
  if (oldEl.type === targetType) return;

  const modeling = graphicModeler.get('modeling');
  const elementFactory = graphicModeler.get('elementFactory');
  const canvas = graphicModeler.get('canvas');
  const root = canvas.getRootElement();
  const oldBo = oldEl.businessObject;

  // 1) 采集扩展属性（原样迁移，避免配置丢失）
  const attrs = {};
  ['flowable:assignee', 'flowable:candidateGroups', 'flowable:candidateUsers', 'flowable:formKey', 'flowable:fieldPerms', 'flowable:operatorGroups', 'flowable:participantConfig'].forEach((k) => {
    const v = (oldBo.get && oldBo.get(k)) || (oldBo.$attrs && oldBo.$attrs[k]);
    if (v !== undefined && v !== null && v !== '') attrs[k] = v;
  });
  const hadMultiInstance = !!oldBo.loopCharacteristics;
  const savedName = oldBo.name || '';

  // 2) 采集连线（含名称与分支条件），按方向分入/出
  const incoming = registry.filter((el) => el.type === 'bpmn:SequenceFlow' && el.businessObject.targetRef && el.businessObject.targetRef.id === oldEl.id);
  const outgoing = registry.filter((el) => el.type === 'bpmn:SequenceFlow' && el.businessObject.sourceRef && el.businessObject.sourceRef.id === oldEl.id);
  const packFlow = (fl) => ({
    name: fl.businessObject.name || '',
    condition: fl.businessObject.conditionExpression ? fl.businessObject.conditionExpression.body || '' : '',
    extraAttrs: Object.assign({}, fl.businessObject.$attrs || {}),
  });
  const inPacks = incoming.map((fl) => ({ el: fl, pack: packFlow(fl) }));
  const outPacks = outgoing.map((fl) => ({ el: fl, pack: packFlow(fl) }));

  const x = oldEl.x,
    y = oldEl.y,
    w = oldEl.width,
    h = oldEl.height;

  try {
    // 3) 先建新元素（同位置），再删旧元素，保证不出现流程断链的中间态被持久化
    const newShape = modeling.createShape(elementFactory.createShape({ type: targetType }), { x: x + w / 2, y: y + h / 2 }, root);
    // 4) 迁移名称与扩展属性
    if (savedName) modeling.updateProperties(newShape, { name: savedName });
    const newBo = newShape.businessObject;
    Object.keys(attrs).forEach((k) => {
      try {
        newBo.$attrs[k] = attrs[k];
      } catch (e) {}
    });
    if (hadMultiInstance && targetType === 'bpmn:UserTask') {
      const moddle = graphicModeler.get('moddle');
      const loop = moddle.create('bpmn:MultiInstanceLoopCharacteristics', {
        isSequential: false,
        completionCondition: moddle.create('bpmn:FormalExpression', { body: '${nrOfCompletedInstances >= nrOfInstances}' }),
      });
      const props = { loopCharacteristics: loop };
      // 新元素换了 id，集合表达式里的节点ID要跟着换，否则会去算旧节点的参与者
      const pcfg = attrs['flowable:participantConfig'] || '';
      let isHandledByParticipant = false;
      if (pcfg) {
        try {
          isHandledByParticipant = !!(JSON.parse(pcfg).participants || []).length;
        } catch (e) {}
      }
      if (isHandledByParticipant) {
        loop.$attrs['flowable:collection'] = "${participantCollection.collectionFor('" + newBo.id + "', execution)}";
        loop.$attrs['flowable:elementVariable'] = 'participantAssignee';
        props['flowable:assignee'] = '${participantAssignee}';
      }
      modeling.updateModdleProperties(newShape, newBo, props);
    }

    // 5) 重建入线（源 → 新元素）
    inPacks.forEach(({ el, pack }) => {
      const src = el.source;
      if (!src) return;
      const conn = modeling.connect(src, newShape, { type: 'bpmn:SequenceFlow' });
      if (pack.name) modeling.updateProperties(conn, { name: pack.name });
      if (pack.condition) {
        const moddle = graphicModeler.get('moddle');
        modeling.updateProperties(conn, { conditionExpression: moddle.create('bpmn:FormalExpression', { body: pack.condition }) });
      }
      Object.keys(pack.extraAttrs || {}).forEach((k) => {
        try {
          conn.businessObject.$attrs[k] = pack.extraAttrs[k];
        } catch (e) {}
      });
    });
    // 6) 重建出线（新元素 → 目标）
    outPacks.forEach(({ el, pack }) => {
      const dst = el.target;
      if (!dst) return;
      const conn = modeling.connect(newShape, dst, { type: 'bpmn:SequenceFlow' });
      if (pack.name) modeling.updateProperties(conn, { name: pack.name });
      if (pack.condition) {
        const moddle = graphicModeler.get('moddle');
        modeling.updateProperties(conn, { conditionExpression: moddle.create('bpmn:FormalExpression', { body: pack.condition }) });
      }
      Object.keys(pack.extraAttrs || {}).forEach((k) => {
        try {
          conn.businessObject.$attrs[k] = pack.extraAttrs[k];
        } catch (e) {}
      });
    });

    // 7) 删除旧元素及其残留连线
    modeling.removeElements([oldEl]);

    // 8) 同步选中态与视觉标记
    refreshGraphicData();
    const created = graphicNodes.value.find((n) => n.type === targetType && n.name === savedName);
    graphicSelectedRow.value = created || null;
    applyWmCanvasMarkers(graphicModeler);
    applyWmReturnRoutes(graphicModeler);
    proxy.$modal.msgSuccess('节点类型已切换为「' + (SEMANTIC_NODE_LABELS[targetType] || targetType.replace('bpmn:', '')) + '」，原有配置与连线已保留，点"保存"生效');
  } catch (e) {
    proxy.$modal.msgError('节点类型切换失败：' + (e.message || e));
    refreshGraphicData();
  }
}

/** 表格编辑：节点属性（名称等）实时写入 BPMN（编辑弹窗的独立实例） */
function updateNodeProp(row, key, value) {
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(row.id);
  if (el) {
    graphicModeler.get('modeling').updateProperties(el, { [key]: value });
    // 表格里改节点名同样要重算宽度
    if (key === 'name') normalizeNodeSize(el);
  }
  refreshGraphicData();
}

/** 表格编辑：节点办理人（编辑弹窗的独立实例） */
function updateNodeAssignee(row) {
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(row.id);
  if (el) graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, { 'flowable:assignee': row.assignee || undefined });
  refreshGraphicData();
}

/** 表格编辑：节点候选组（编辑弹窗的独立实例） */
function updateNodeCandidate(row) {
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(row.id);
  if (el) graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, { 'flowable:candidateGroups': row.candidateGroups || undefined });
  refreshGraphicData();
}

/** 表格编辑：节点表单（写回 flowable:formKey，留空即 Html 模式） */
function updateNodeFormKey(row) {
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(row.id);
  if (!el) return;
  const val = row.formKey === 'Html模式' || row.formKey === '' ? undefined : row.formKey;
  graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, { 'flowable:formKey': val || undefined });
  refreshGraphicData();
}

// ===== 节点右侧设置：操作人员 / 表单内容 / 操作菜单（参照节点信息面板） =====
const nodeFormOpen = ref(false);
const nodeFormDesignOpen = ref(false);
const nodeFormPickOpen = ref(false);
const nodeFormPickId = ref('');
const nodeFormPickKeyword = ref('');
const nodeFormDraft = reactive({
  nodeId: '',
  nodeName: '',
  formId: '',
  isNodeForm: false,
  nodeFormName: '',
  // 引用来源节点与所用模板名称（用于「未修改名称时自动按节点名称+表单重命名」）
  fromNodeId: '',
  fromNodeName: '',
  refFormName: '',
  // 用户是否主动改过名称；未改过则保存时自动重命名，避免与其他节点的模板重名
  nameDirty: false,
  fields: null,
  grid: null,
});
const opMenuOpen = ref(false);
const opMenuDraft = reactive({ nodeId: '', nodeName: '', isStart: false, rows: [], sign: { enabled: true, required: false, defaultValue: '' } });
/** 办理节点可配置的操作按钮（签字意见有独立开关、附件走「相关资源」，不再放进操作菜单） */
const OP_MENU_OPTIONS = [
  { value: 'submit', label: '提交' },
  { value: 'reject', label: '退回' },
  { value: 'transfer', label: '转办' },
  { value: 'addSign', label: '加签' },
  { value: 'terminate', label: '终止' },
  { value: 'print', label: '打印' },
  { value: 'cc', label: '抄送' },
];
/** 支持配置「默认审批意见」的按钮：提交 / 退回（办理动作类按钮，点击时可预填意见） */
const OP_MENU_OPINION_KEYS = ['submit', 'reject'];
/** 历史配置可能仍带「签字意见/附件上传」，仅用于摘要文案展示 */
const OP_MENU_LEGACY_LABELS = { comment: '签字意见', attach: '附件上传' };
/** 审批/办理节点默认菜单 */
const DEFAULT_OP_MENUS = ['submit', 'reject', 'print', 'cc'];
/** 申请（开始）节点默认菜单：发起场景不需要退回/转办/加签/终止 */
const START_OP_MENUS = ['submit', 'print', 'cc'];

/** 右侧「操作人员」行摘要 */
const participantSummaryText = computed(() => {
  const se = selectedElement.value;
  const ps = (se && se.participants) || [];
  if (!ps.length) return se && se.isStart ? '申请人（流程发起人）' : '未设置';
  return ps.length > 2
    ? ps
        .slice(0, 2)
        .map((p) => participantNameText(p))
        .join('、') +
        ' 等' +
        ps.length +
        '人'
    : ps.map((p) => participantNameText(p)).join('、');
});

/** 右侧「抄送人员」行摘要 */
const ccSummaryText = computed(() => {
  const se = selectedElement.value;
  const ps = (se && se.ccParticipants) || [];
  if (!ps.length) return '未设置抄送';
  return ps.length > 2
    ? ps
        .slice(0, 2)
        .map((p) => participantNameText(p))
        .join('、') +
        ' 等' +
        ps.length +
        '人'
    : ps.map((p) => participantNameText(p)).join('、');
});

const flowFormName = computed(() => {
  const hit = formOptions.value.find((f) => String(f.formId) === String(designModel.value.formId || ''));
  return hit ? hit.formName : '';
});
const nodeFormName = computed(() => {
  if (nodeFormDraft.isNodeForm) return nodeFormDraft.nodeFormName || defaultNodeFormName(nodeFormDraft.nodeName);
  const hit = formOptions.value.find((f) => String(f.formId) === String(nodeFormDraft.formId || ''));
  return hit ? hit.formName : nodeFormDraft.formId ? '表单 #' + nodeFormDraft.formId : '';
});

/** 节点模板默认名称：节点名称 + 表单 */
function defaultNodeFormName(nodeName) {
  return (nodeName || '节点') + '表单';
}

/** 本流程其他节点已占用的模板名称（用于去重） */
function otherNodeFormNames(selfNodeId) {
  const used = new Set();
  Object.keys(nodeFormMap.value || {}).forEach((id) => {
    if (id === selfNodeId) return;
    const name = (nodeFormMap.value[id] && nodeFormMap.value[id].formName) || '';
    if (name) used.add(String(name));
  });
  return used;
}

/** 与其他节点模板重名时追加序号，例如「部门审批表单(2)」 */
function uniqueNodeFormName(base, selfNodeId) {
  const used = otherNodeFormNames(selfNodeId);
  if (!used.has(base)) return base;
  let i = 2;
  while (used.has(base + '(' + i + ')')) i++;
  return base + '(' + i + ')';
}

/** 引用模板后、名称未修改时自动使用的名称（即节点名称+表单） */
const autoNodeFormName = computed(() => uniqueNodeFormName(defaultNodeFormName(nodeFormDraft.nodeName), nodeFormDraft.nodeId));

/**
 * 解析保存时实际使用的模板名称：
 * 1. 引用某模板且名称未修改 → 自动取「节点名称+表单」，避免与其他节点模板重名；
 * 2. 用户主动改过名称 → 保留自定义名称，仅在与其他节点模板重名时追加序号；
 * 3. 未填名称 → 回退为「节点名称+表单」。
 */
function resolveNodeFormName() {
  const cur = (nodeFormDraft.nodeFormName || '').trim();
  const refName = (nodeFormDraft.refFormName || '').trim();
  const untouched = !nodeFormDraft.nameDirty && (!cur || (!!refName && cur === refName));
  if (!untouched) {
    if (!otherNodeFormNames(nodeFormDraft.nodeId).has(cur)) return cur;
  }
  return uniqueNodeFormName(defaultNodeFormName(nodeFormDraft.nodeName), nodeFormDraft.nodeId);
}

/** 可选节点表单：数据表默认表单 + 本流程其他节点已设置好的节点表单 */
const flowNodeFormOptions = computed(() => {
  const arr = [];
  const flowFid = designModel.value.formId ? String(designModel.value.formId) : '';
  const flowHit = formOptions.value.find((f) => String(f.formId) === flowFid);
  if (flowFid) {
    arr.push({
      key: 'default',
      formId: flowFid,
      formName: (flowHit?.formName || flowFormName.value || '默认表单') + '（数据表默认表单）',
      fromNodeNames: [],
      snapshot: null,
    });
  }
  graphicNodes.value.forEach((n) => {
    if (!isConfigurableNode(n) || !n.nodeFormSnapshot || n.id === nodeFormDraft.nodeId) return;
    arr.push({
      key: 'node:' + n.id,
      formId: String(n.formKey || flowFid),
      formName: n.nodeFormName || (n.name || n.id) + '表单',
      fromNodeNames: [n.name || n.id],
      snapshot: n.nodeFormSnapshot,
    });
  });
  return arr;
});

const filteredNodeFormOptions = computed(() => {
  const kw = nodeFormPickKeyword.value.trim();
  if (!kw) return flowNodeFormOptions.value;
  return flowNodeFormOptions.value.filter((f) => f.formName.includes(kw) || f.fromNodeNames.join('、').includes(kw));
});

/**
 * 解析节点扩展属性 flowable:operationMenus
 * 兼容两种格式：旧格式字符串数组 ["submit","reject"]；新格式对象数组 [{key,name,defaultOpinion}]
 */
function parseOpMenus(bo) {
  const raw = (bo && bo.get && bo.get('flowable:operationMenus')) || (bo && bo.$attrs && bo.$attrs['flowable:operationMenus']) || '';
  if (!raw) return [];
  try {
    const arr = JSON.parse(raw);
    if (!Array.isArray(arr)) return [];
    return arr.map((v) => (typeof v === 'string' ? v : v && v.key)).filter(Boolean);
  } catch (e) {
    return [];
  }
}

/**
 * 解析节点扩展属性 flowable:operationMenus 中的按钮明细（自定义名称 / 默认审批意见）
 * 返回 [{key, name, defaultOpinion}]；旧格式（字符串数组）只有 key
 */
function parseOpMenuDetails(bo) {
  const raw = (bo && bo.get && bo.get('flowable:operationMenus')) || (bo && bo.$attrs && bo.$attrs['flowable:operationMenus']) || '';
  if (!raw) return [];
  try {
    const arr = JSON.parse(raw);
    if (!Array.isArray(arr)) return [];
    return arr
      .map((v) => {
        if (typeof v === 'string') return { key: v, name: '', defaultOpinion: '' };
        return v && v.key ? { key: v.key, name: v.name || '', defaultOpinion: v.defaultOpinion || '' } : null;
      })
      .filter(Boolean);
  } catch (e) {
    return [];
  }
}

/**
 * 解析节点扩展属性 flowable:signOpinionJson（签字意见完整设置：启用/必填/默认意见）
 * 未配置时返回 null（调用方回退 flowable:signOpinion 开关或默认值）
 */
function parseSignOpinionJson(bo) {
  const raw = (bo && bo.get && bo.get('flowable:signOpinionJson')) || (bo && bo.$attrs && bo.$attrs['flowable:signOpinionJson']) || '';
  if (!raw) return null;
  try {
    const obj = JSON.parse(raw);
    return obj && typeof obj === 'object' ? obj : null;
  } catch (e) {
    return null;
  }
}

/**
 * 解析节点扩展属性 flowable:signOpinion（签字意见开关）
 * 签字意见已从操作菜单中独立出来：未显式配置时默认启用，由本开关单独控制
 */
function parseSignOpinion(bo) {
  const raw = (bo && bo.get && bo.get('flowable:signOpinion')) || (bo && bo.$attrs && bo.$attrs['flowable:signOpinion']) || '';
  if (raw) {
    const v = String(raw).trim().toLowerCase();
    return !(v === 'false' || v === '0' || v === '否');
  }
  return true;
}

/** 解析节点扩展属性 flowable:allowRecall（强制收回开关）：未配置时默认允许 */
function parseAllowRecall(bo) {
  const raw = (bo && bo.get && bo.get('flowable:allowRecall')) || (bo && bo.$attrs && bo.$attrs['flowable:allowRecall']) || '';
  if (!raw) return true;
  const v = String(raw).trim().toLowerCase();
  return !(v === 'false' || v === '0' || v === '否');
}

/** 解析节点表单快照（flowable:nodeFormJson） */
function readNodeFormSnapshot(bo) {
  const raw = (bo && bo.get && bo.get('flowable:nodeFormJson')) || (bo && bo.$attrs && bo.$attrs['flowable:nodeFormJson']) || '';
  if (!raw) return null;
  try {
    const obj = JSON.parse(raw);
    return obj && Array.isArray(obj.fields) ? obj : null;
  } catch (e) {
    return null;
  }
}

function cloneJson(v) {
  return v ? JSON.parse(JSON.stringify(v)) : v;
}

/** 安全解析 JSON 数组（表单 fieldsJson 这类字符串） */
function parseJsonArray(text) {
  try {
    const v = JSON.parse(text || '[]');
    return Array.isArray(v) ? v : [];
  } catch (e) {
    return [];
  }
}

// ===== 节点表单模板（以数据库 flow_node_form 为准：字段/必填/只读修改无需重新部署流程） =====
/** nodeId -> { formName, formKey, nodeType, fields, grid } */
const nodeFormMap = ref({});

/** 加载当前模型的节点模板到内存映射，并刷新图形节点数据 */
async function loadNodeFormTemplates() {
  const modelId = designModel.value && designModel.value.modelId;
  if (!modelId) {
    nodeFormMap.value = {};
    return;
  }
  try {
    const res = await listNodeForms(modelId);
    const map = {};
    (res.data || []).forEach((t) => {
      let fields = [];
      try {
        fields = JSON.parse(t.fieldsJson || '[]') || [];
      } catch (e) {
        fields = [];
      }
      const layout = parseLayout(t.layoutJson);
      map[t.nodeId] = {
        formName: t.formName || '',
        formKey: t.formKey ? String(t.formKey) : '',
        nodeType: t.nodeType || '',
        fields,
        grid: layout.grid || null,
      };
    });
    nodeFormMap.value = map;
  } catch (e) {
    nodeFormMap.value = {};
  }
  refreshGraphicData();
  if (selectedElement.value && graphicModeler) {
    const el = graphicModeler.get('elementRegistry').get(selectedElement.value.id);
    if (el) loadSelectedElement(el);
  }
}

/* ===== 节点打印模板：办理页打印优先用节点配置，未配置回退节点显示模板 ===== */
/** nodeId → { nodeId, templateFormId, templateName } */
const nodePrintMap = ref({});
const printTplOpen = ref(false);
const printTplSaving = ref(false);
const printTplDraft = ref({ nodeId: '', nodeName: '', mode: 'display', formId: '' });

async function loadNodePrintTemplates() {
  const modelId = designModel.value && designModel.value.modelId;
  if (!modelId) {
    nodePrintMap.value = {};
    return;
  }
  try {
    const res = await listNodePrints(modelId);
    const map = {};
    (res.data || []).forEach((p) => {
      map[p.nodeId] = p;
    });
    nodePrintMap.value = map;
  } catch (e) {
    nodePrintMap.value = {};
  }
}

/** 节点信息面板上的取值文案 */
function printTemplateText(node) {
  if (!node) return '跟随节点显示模板';
  const hit = nodePrintMap.value[node.id];
  return hit && hit.templateName ? '打印模板：' + hit.templateName : '跟随节点显示模板';
}

function openPrintTemplate(node) {
  if (!node) return;
  const hit = nodePrintMap.value[node.id];
  printTplDraft.value = {
    nodeId: node.id,
    nodeName: node.name || '',
    mode: hit ? 'form' : 'display',
    formId: hit && hit.templateFormId ? String(hit.templateFormId) : '',
  };
  printTplOpen.value = true;
}

async function savePrintTemplate() {
  const d = printTplDraft.value;
  const modelId = designModel.value && designModel.value.modelId;
  if (!modelId || !d.nodeId) return;
  if (d.mode === 'form' && !d.formId) {
    proxy.$modal.msgWarning('请选择打印模板');
    return;
  }
  printTplSaving.value = true;
  try {
    if (d.mode === 'display') {
      await delNodePrint(modelId, d.nodeId);
      const map = { ...nodePrintMap.value };
      delete map[d.nodeId];
      nodePrintMap.value = map;
      proxy.$modal.msgSuccess('已改为跟随节点显示模板');
    } else {
      const form = formOptions.value.find((f) => String(f.formId) === String(d.formId));
      const payload = {
        modelId: String(modelId),
        modelKey: designModel.value.modelKey || '',
        nodeId: d.nodeId,
        nodeName: d.nodeName,
        templateFormId: String(d.formId),
        templateName: form ? form.formName : '',
      };
      await saveNodePrint(payload);
      nodePrintMap.value = { ...nodePrintMap.value, [d.nodeId]: payload };
      proxy.$modal.msgSuccess('打印模板已保存');
    }
    printTplOpen.value = false;
  } catch (e) {
    proxy.$modal.msgError('保存失败：' + (e && e.message ? e.message : '请稍后重试'));
  } finally {
    printTplSaving.value = false;
  }
}

/** 节点类型（start / userTask / end） */
function nodeTypeOfNode(nodeId) {
  const hit = graphicNodes.value.find((n) => n.id === nodeId);
  if (!hit) return 'userTask';
  return hit.isStart ? 'start' : hit.isEnd ? 'end' : 'userTask';
}

/** 节点布局 → layoutJson 字符串 */
function serializeNodeGrid(grid) {
  if (!grid) return '';
  try {
    return serializeLayout({ mode: 'grid', grid });
  } catch (e) {
    return '';
  }
}

/** 字段增删后同步网格：移除已删除字段的格子，把新增字段追加到末尾 */
function syncGridWithFields(grid, fields) {
  if (!grid) return grid;
  const mainFields = (fields || []).filter((f) => f && f.field && (f.table || 'main') === 'main');
  // 保留判定用全量字段（含紧急程度/打印次数等系统字段）：用户手工放到画布上的必须留住
  const fieldSet = new Set(mainFields.map((f) => f.field));
  grid.cells = (grid.cells || []).filter((c) => !(c.type === 'field' && c.field && !fieldSet.has(c.field)));
  const used = new Set(grid.cells.filter((c) => c.type === 'field' && c.field).map((c) => c.field));
  // 自动铺排只补业务字段；系统字段（紧急程度/签字意见/流程状态/打印次数）由用户在画布上自行选择放置
  const missing = mainFields.filter((f) => !used.has(f.field) && !isFlowSysField(f.field));
  if (!missing.length) return grid;
  let row = grid.cells.filter((c) => c.type === 'field').reduce((m, c) => Math.max(m, c.r + (c.rs || 1)), 1);
  let col = 0;
  const span = 2;
  for (const f of missing) {
    if (col + span > grid.cols) {
      row++;
      col = 0;
    }
    while (row >= grid.rows) {
      grid.rows++;
      grid.rowHeights.push(30);
    }
    grid.cells.push({ r: row, c: col, rs: 1, cs: span, type: 'field', field: f.field });
    col += span;
  }
  return grid;
}

/** 节点模板保存到数据库（发起页/办理页实时读取，无需重新部署） */
async function saveNodeFormToDb() {
  const modelId = designModel.value && designModel.value.modelId;
  if (!modelId || !nodeFormDraft.nodeId) return;
  // 恢复默认表单：删除该节点模板
  if (!nodeFormDraft.isNodeForm) {
    await delNodeForm(modelId, nodeFormDraft.nodeId);
    delete nodeFormMap.value[nodeFormDraft.nodeId];
    return;
  }
  const nodeType = nodeTypeOfNode(nodeFormDraft.nodeId);
  // 名称解析：引用了其他模板且名称未修改时，自动改为「节点名称+表单」，避免节点模板重名
  const formName = resolveNodeFormName();
  const payload = {
    modelId: String(modelId),
    modelKey: designModel.value.modelKey || '',
    nodeId: nodeFormDraft.nodeId,
    nodeType,
    nodeName: nodeFormDraft.nodeName || '',
    formName,
    formKey: nodeFormDraft.formId || '',
    fieldsJson: JSON.stringify(nodeFormDraft.fields || []),
    layoutJson: serializeNodeGrid(nodeFormDraft.grid),
  };
  await saveNodeFormApi(payload);
  nodeFormMap.value[nodeFormDraft.nodeId] = {
    formName: payload.formName,
    formKey: payload.formKey,
    nodeType,
    fields: cloneJson(nodeFormDraft.fields || []),
    grid: cloneJson(nodeFormDraft.grid),
  };
  // 回写草稿：界面显示最终名称，并清空「未修改」的引用比对（后续保存不再重复改名）
  nodeFormDraft.nodeFormName = formName;
  nodeFormDraft.nameDirty = false;
  nodeFormDraft.refFormName = '';
  if (nodeDesignForm.formId === nodeFormDraft.formId) nodeDesignForm.formName = formName;
}

/** 兼容迁移：把历史 BPMN 里的 flowable:nodeFormJson 模板一次性落库 */
async function migrateLegacyNodeForms() {
  const modelId = designModel.value && designModel.value.modelId;
  if (!modelId) return;
  const candidates = graphicNodes.value.filter((n) => isConfigurableNode(n) && !nodeFormMap.value[n.id] && n.nodeFormSnapshot && n.nodeFormSnapshot.fields && n.nodeFormSnapshot.fields.length);
  if (!candidates.length) return;
  for (const n of candidates) {
    const payload = {
      modelId: String(modelId),
      modelKey: designModel.value.modelKey || '',
      nodeId: n.id,
      nodeType: n.isStart ? 'start' : n.isEnd ? 'end' : 'userTask',
      nodeName: n.name || '',
      formName: n.nodeFormName || '',
      formKey: n.formKey || '',
      fieldsJson: JSON.stringify(n.nodeFormSnapshot.fields || []),
      layoutJson: serializeNodeGrid(n.nodeFormSnapshot.grid),
    };
    try {
      await saveNodeFormApi(payload);
    } catch (e) {}
  }
  await loadNodeFormTemplates();
}

function nodeFormText(node) {
  if (!isConfigurableNode(node)) return '未绑定';
  if (node.nodeFormSnapshot) return (node.nodeFormName || '节点表单') + '（节点表单）';
  if (!node.formKey) {
    return flowFormName.value ? '流程表单：' + flowFormName.value : '流程表单（未关联）';
  }
  const hit = formOptions.value.find((f) => String(f.formId) === String(node.formKey));
  return hit ? hit.formName : '表单 #' + node.formKey;
}

function nodeOpMenuText(node) {
  // 新格式：按钮明细里有自定义名称时一并展示，例如「提交(批准)、退回、打印」
  const details = (node && node.opMenuDetails) || [];
  if (details.length) {
    return details
      .map((d) => {
        const hit = OP_MENU_OPTIONS.find((m) => m.value === d.key);
        const label = hit ? hit.label : OP_MENU_LEGACY_LABELS[d.key] || d.key;
        return d.name ? label + '（' + d.name + '）' : label;
      })
      .join('、');
  }
  const items = (node && node.opMenus) || [];
  if (!items.length) {
    // 未配置时展示按节点类型给出的默认菜单
    const defaults = node && node.isStart ? START_OP_MENUS : DEFAULT_OP_MENUS;
    return (
      '默认菜单（' +
      defaults
        .map((v) => {
          const hit = OP_MENU_OPTIONS.find((m) => m.value === v);
          return hit ? hit.label : v;
        })
        .join('、') +
      '）'
    );
  }
  return items
    .map((v) => {
      const hit = OP_MENU_OPTIONS.find((m) => m.value === v);
      return hit ? hit.label : OP_MENU_LEGACY_LABELS[v] || v;
    })
    .join('、');
}

/** 打开节点表单配置（右侧属性面板入口，申请节点、用户任务与结束节点均可设置） */
function openNodeForm(node) {
  if (!isConfigurableNode(node)) return;
  nodeFormDraft.nodeId = node.id;
  nodeFormDraft.nodeName = node.name || node.id;
  nodeFormDraft.formId = String(node.formKey || '') === 'html' ? '' : String(node.formKey || '');
  nodeFormDraft.isNodeForm = !!node.nodeFormSnapshot;
  nodeFormDraft.nodeFormName = node.nodeFormSnapshot ? node.nodeFormName || '' : '';
  nodeFormDraft.fromNodeId = node.nodeFormSnapshot ? node.nodeFormSnapshot.fromNodeId || '' : '';
  nodeFormDraft.fromNodeName = '';
  // 已落库模板不再记得引用来源；若名称与其他节点模板重复，由保存时的名称解析自动去重
  nodeFormDraft.refFormName = '';
  nodeFormDraft.nameDirty = false;
  nodeFormDraft.fields = node.nodeFormSnapshot ? cloneJson(node.nodeFormSnapshot.fields) : null;
  nodeFormDraft.grid = node.nodeFormSnapshot ? cloneJson(node.nodeFormSnapshot.grid) : null;
  nodeFormOpen.value = true;
}

/** 恢复使用数据表默认表单 */
function useFlowForm() {
  nodeFormDraft.formId = '';
  nodeFormDraft.isNodeForm = false;
  nodeFormDraft.nodeFormName = '';
  nodeFormDraft.fromNodeId = '';
  nodeFormDraft.fromNodeName = '';
  nodeFormDraft.refFormName = '';
  nodeFormDraft.nameDirty = false;
  nodeFormDraft.fields = null;
  nodeFormDraft.grid = null;
}

function openNodeFormPicker() {
  nodeFormPickKeyword.value = '';
  nodeFormPickId.value = nodeFormDraft.isNodeForm ? (nodeFormDraft.fromNodeId ? 'node:' + nodeFormDraft.fromNodeId : '') : 'default';
  nodeFormPickOpen.value = true;
}

function confirmNodeFormPick() {
  const opt = flowNodeFormOptions.value.find((o) => o.key === nodeFormPickId.value);
  if (!opt) return;
  if (opt.key === 'default') {
    nodeFormDraft.isNodeForm = false;
    nodeFormDraft.formId = opt.formId;
    nodeFormDraft.nodeFormName = '';
    nodeFormDraft.fromNodeId = '';
    nodeFormDraft.fromNodeName = '';
    nodeFormDraft.refFormName = '';
    nodeFormDraft.nameDirty = false;
    nodeFormDraft.fields = null;
    nodeFormDraft.grid = null;
  } else {
    nodeFormDraft.isNodeForm = true;
    nodeFormDraft.formId = opt.formId;
    // 先显示所引用模板的名称；若用户不再修改名称，保存时自动改为「节点名称+表单」
    nodeFormDraft.nodeFormName = opt.formName;
    nodeFormDraft.fromNodeId = opt.key.slice(5);
    nodeFormDraft.fromNodeName = (opt.fromNodeNames && opt.fromNodeNames[0]) || '';
    nodeFormDraft.refFormName = opt.formName;
    nodeFormDraft.nameDirty = false;
    nodeFormDraft.fields = cloneJson(opt.snapshot.fields);
    nodeFormDraft.grid = cloneJson(opt.snapshot.grid);
  }
  nodeFormPickOpen.value = false;
}

/** 节点表单设计已落库；BPMN 仅保留 formKey 引用，旧快照属性清空（避免与数据库双份来源） */
function persistNodeFormAttrs(el) {
  if (!graphicModeler || !el) return;
  const bo = el.businessObject;
  const formKey = nodeFormDraft.formId || (designModel.value.formId ? String(designModel.value.formId) : undefined);
  graphicModeler.get('modeling').updateModdleProperties(el, bo, {
    'flowable:formKey': formKey,
    'flowable:nodeFormName': undefined,
    'flowable:nodeFormJson': undefined,
  });
  try {
    const set = (k, v) => {
      if (v) bo.$attrs[k] = v;
      else delete bo.$attrs[k];
    };
    set('flowable:formKey', formKey);
    delete bo.$attrs['flowable:nodeFormName'];
    delete bo.$attrs['flowable:nodeFormJson'];
  } catch (e) {}
}

/** 新增节点表单：复制数据表默认表单的字段与布局，形成该节点自己的表单 */
async function createNodeForm() {
  const fid = designModel.value.formId ? String(designModel.value.formId) : nodeFormDraft.formId;
  if (!fid) {
    proxy.$modal.msgWarning('流程未绑定数据表表单，请先在「基本信息」关联表单');
    return;
  }
  try {
    const d = (await getForm(fid)).data || {};
    let fields = [];
    try {
      fields = JSON.parse(d.fieldsJson || '[]');
    } catch (e) {
      fields = [];
    }
    const layout = parseLayout(d.layoutJson);
    nodeFormDraft.isNodeForm = true;
    nodeFormDraft.formId = String(d.formId || fid);
    nodeFormDraft.fromNodeId = '';
    nodeFormDraft.fromNodeName = '';
    nodeFormDraft.refFormName = '';
    nodeFormDraft.nameDirty = false;
    nodeFormDraft.nodeFormName = uniqueNodeFormName(defaultNodeFormName(nodeFormDraft.nodeName), nodeFormDraft.nodeId);
    nodeFormDraft.fields = cloneJson(fields);
    nodeFormDraft.grid = cloneJson(layout.grid || fieldsToGrid(fields.filter((f) => (f.table || 'main') === 'main')));
    proxy.$modal.msgSuccess('已创建节点表单，请继续设计');
    openNodeFormDesigner();
  } catch (e) {
    proxy.$modal.msgError('创建节点表单失败：' + (e.message || e));
  }
}

/** 修改节点模板名称（仅节点模板，独立于表单管理） */
async function renameNodeForm() {
  if (!nodeFormDraft.isNodeForm && !nodeFormDraft.formId) {
    proxy.$modal.msgWarning('请先选择或新增节点表单');
    return;
  }
  let name = '';
  try {
    const { value } = await ElMessageBox.prompt('请输入节点模板名称', '修改节点模板名称', {
      inputValue: nodeFormName.value,
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValidator: (v) => (v && v.trim() ? true : '节点模板名称不能为空'),
    });
    name = (value || '').trim();
  } catch (e) {
    return;
  }
  nodeFormDraft.nodeFormName = name;
  nodeFormDraft.nameDirty = true;
  if (nodeFormDraft.isNodeForm) {
    try {
      await saveNodeFormToDb();
    } catch (e) {
      proxy.$modal.msgError('节点模板名称保存失败：' + (e.message || e));
      return;
    }
    const el = graphicModeler && graphicModeler.get('elementRegistry').get(nodeFormDraft.nodeId);
    if (el) persistNodeFormAttrs(el);
    refreshGraphicData();
  }
  const finalName = nodeFormDraft.nodeFormName;
  if (nodeDesignForm.formId === nodeFormDraft.formId) nodeDesignForm.formName = finalName;
  proxy.$modal.msgSuccess(finalName === name ? '节点模板名称已更新' : `名称「${name}」已被其他节点模板占用，已自动更名为「${finalName}」`);
}

/** 保存节点表单（节点模板落库 + BPMN 仅保留 formKey；实时生效，无需重新部署） */
async function saveNodeForm() {
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(nodeFormDraft.nodeId);
  if (!el) {
    proxy.$modal.msgWarning('节点已不存在，请重新打开');
    return;
  }
  const beforeName = (nodeFormDraft.nodeFormName || '').trim();
  try {
    await saveNodeFormToDb();
  } catch (e) {
    proxy.$modal.msgError('节点表单保存失败：' + (e.message || e));
    return;
  }
  persistNodeFormAttrs(el);
  refreshGraphicData();
  if (selectedElement.value && selectedElement.value.id === nodeFormDraft.nodeId) {
    const el2 = graphicModeler.get('elementRegistry').get(nodeFormDraft.nodeId);
    if (el2) loadSelectedElement(el2);
  }
  nodeFormOpen.value = false;
  proxy.$modal.msgSuccess(
    nodeFormDraft.isNodeForm && nodeFormDraft.nodeFormName !== beforeName && beforeName
      ? `节点表单已保存，模板名称已自动改为「${nodeFormDraft.nodeFormName}」（避免与引用模板重名），实时生效`
      : '节点表单已保存，实时生效（无需重新部署）',
  );
}

// ===== 节点表单设计：整页只有页面设计（字段维护走独立的「表单编辑」弹窗） =====
/** 「表单编辑」弹窗：临时增删字段/调整字段 */
const nodeFieldEditOpen = ref(false);
const nodeDesignLoading = ref(false);
const nodeDesignNameSaving = ref(false);
const nodeDesignFullscreen = ref(false);
/** 「表单编辑」弹窗里的字段编辑器实例：关闭弹窗/保存模板前先取回最新字段，避免字段编辑白改 */
const nodeFieldsRef = ref(null);
const nodeDesignForm = reactive({ formId: '', formName: '', formKey: '', isNodeForm: false, fields: [], grid: null });
/** 设计目标：节点表单优先，未指定则设计数据表默认表单 */
const nodeDesignFormId = computed(() => nodeFormDraft.formId || designModel.value.formId || '');

/** 页面设计里尚未放置到网格的字段（表单新增字段后，节点布局不会自动变，这里给出补齐入口） */
const nodeUnplacedFields = computed(() => {
  const grid = nodeDesignForm.grid;
  if (!grid) return [];
  const used = new Set((grid.cells || []).filter((c) => c && c.type === 'field' && c.field).map((c) => c.field));
  return (nodeDesignForm.fields || []).filter((f) => f && f.field && (f.table || 'main') === 'main' && !used.has(f.field) && !isFlowSysField(f.field));
});

/** 重新从表单读一次字段定义（字段只有一份来源＝表单，节点只拿布局） */
async function refreshNodeFieldsFromForm() {
  const fid = nodeDesignForm.formId;
  if (!fid) return;
  try {
    const d = (await getForm(fid)).data || {};
    nodeDesignForm.fields = parseJsonArray(d.fieldsJson);
  } catch (e) {
    /* 读取失败保持原字段，不打断设计 */
  }
}

/** 离开表单编辑弹窗时把未保存的字段改动写回表单：
 * 弹窗与编辑器都是 v-if + destroy-on-close，不回收就等于静默丢弃改动 */
watch(nodeFieldEditOpen, async (val, old) => {
  if (old !== true || val !== false) return;
  if (!nodeFieldsRef.value || !nodeFieldsRef.value.isDirty) return;
  await nodeFieldsRef.value.flush();
});

function openNodeFormDesigner() {
  if (!nodeDesignFormId.value && !nodeFormDraft.isNodeForm) {
    proxy.$modal.msgWarning('流程未关联表单，请先在基本信息中关联');
    return;
  }
  nodeDesignFullscreen.value = true;
  nodeFormDesignOpen.value = true;
  loadNodeDesignData();
}

/** 打开「表单编辑」弹窗（临时增删字段/调整字段） */
function openNodeFieldEditor() {
  if (!nodeDesignForm.formId) {
    proxy.$modal.msgWarning('请先为节点选择表单');
    return;
  }
  nodeFieldEditOpen.value = true;
}

/** 关闭「表单编辑」：有未保存字段先落库，校验不通过则不关闭，把问题留在弹窗里改 */
async function handleNodeFieldEditorClose(done) {
  const ed = nodeFieldsRef.value;
  if (ed && ed.isDirty) {
    const r = await ed.flush();
    if (!r || !r.ok) return;
    await refreshNodeFieldsFromForm();
  }
  if (typeof done === 'function') done();
  else nodeFieldEditOpen.value = false;
}

/** 保存节点模板名称与设计：只作用于当前节点，不修改表单管理中的数据表表单 */
async function saveNodeDesignAll() {
  if (!nodeDesignForm.formId && !nodeFormDraft.isNodeForm) return;
  const name = (nodeDesignForm.formName || '').trim();
  if (!name) {
    proxy.$modal.msgWarning('请输入节点模板名称');
    return;
  }
  nodeDesignNameSaving.value = true;
  try {
    // 「表单编辑」里改了字段却没点「保存字段设计」：先写回表单，再把最新字段读回来
    if (nodeFieldsRef.value && nodeFieldsRef.value.isDirty) {
      const flushed = await nodeFieldsRef.value.flush();
      if (!flushed.ok) return;
      if (flushed.fields) nodeDesignForm.fields = cloneJson(flushed.fields);
      else await refreshNodeFieldsFromForm();
    }
    // 在「表单设计」里改了名称才算用户自定义；否则保持「未修改」，保存时自动按「节点名称+表单」命名
    if (name !== (nodeFormDraft.nodeFormName || '').trim()) nodeFormDraft.nameDirty = true;
    nodeFormDraft.isNodeForm = true;
    nodeFormDraft.formId = nodeFormDraft.formId || String(nodeDesignForm.formId || designModel.value.formId || '');
    nodeFormDraft.nodeFormName = name;
    nodeFormDraft.fromNodeId = '';
    nodeFormDraft.fields = cloneJson(nodeDesignForm.fields);
    nodeFormDraft.grid = cloneJson(nodeDesignForm.grid);
    await saveNodeFormToDb();
    nodeDesignForm.formName = nodeFormDraft.nodeFormName;
    const el = graphicModeler && graphicModeler.get('elementRegistry').get(nodeFormDraft.nodeId);
    if (el) persistNodeFormAttrs(el);
    refreshGraphicData();
    proxy.$modal.msgSuccess(
      nodeFormDraft.nodeFormName !== name ? `已保存到当前节点，模板名称已自动改为「${nodeFormDraft.nodeFormName}」（避免与其他节点模板重名）` : '已保存到当前节点，实时生效（不影响表单管理）',
    );
  } catch (e) {
    proxy.$modal.msgError('保存失败：' + (e.message || e));
  } finally {
    nodeDesignNameSaving.value = false;
  }
}

async function loadNodeDesignData() {
  // 字段定义只有一份（表单管理里的表单），节点只持有自己的「显示布局」
  nodeDesignForm.isNodeForm = true;
  const fid = nodeDesignFormId.value;
  if (!fid) {
    nodeDesignForm.fields = [];
    nodeDesignForm.grid = null;
    return;
  }
  nodeDesignLoading.value = true;
  try {
    const res = await getForm(fid);
    const d = res.data || {};
    const fields = parseJsonArray(d.fieldsJson);
    nodeDesignForm.formId = d.formId;
    nodeDesignForm.formKey = d.formKey || '';
    nodeDesignForm.formName = nodeFormDraft.isNodeForm ? nodeFormName.value : (nodeFormDraft.nodeName || '节点') + '表单';
    nodeDesignForm.fields = fields;
    // 布局：节点已存布局优先（节点靠布局裁剪显示哪些字段），否则用表单布局
    if (nodeFormDraft.isNodeForm && nodeFormDraft.grid) {
      nodeDesignForm.grid = cloneJson(nodeFormDraft.grid);
    } else {
      const layout = parseLayout(d.layoutJson);
      nodeDesignForm.grid = layout.grid || fieldsToGrid(fields.filter((f) => (f.table || 'main') === 'main'));
    }
  } catch (e) {
    proxy.$modal.msgError('加载表单设计失败：' + (e.message || e));
  } finally {
    nodeDesignLoading.value = false;
  }
}

/** 把页面设计里还没放置的字段补齐到末尾（只追加，不动已有格子） */
function fillUnplacedNodeFields() {
  if (!nodeDesignForm.grid || !nodeUnplacedFields.value.length) return;
  const count = nodeUnplacedFields.value.length;
  nodeDesignForm.grid = syncGridWithFields(nodeDesignForm.grid, nodeDesignForm.fields);
  proxy.$modal.msgSuccess('已补齐 ' + count + ' 个字段，点右上「保存」写入本节点');
}

/** 字段设计保存回调：字段定义就是表单本身，保存后重新读一遍表单（布局不动） */
async function onNodeFieldsSaved() {
  // 只刷新字段定义，不重载布局：重载会把页面上还没保存的页面设计改动盖掉
  await refreshNodeFieldsFromForm();
}

/** 打开节点操作菜单配置（含自定义名称 / 默认审批意见 / 签字意见设置） */
function openNodeOpMenu(node) {
  if (!node || (!node.isUserTask && !node.isStart)) return;
  opMenuDraft.nodeId = node.id;
  opMenuDraft.nodeName = node.name || node.id;
  opMenuDraft.isStart = !!node.isStart;
  // 申请节点只保留发起场景按钮；审批节点全量可配
  const allowed = opMenuDraft.isStart ? START_OP_MENUS : OP_MENU_OPTIONS.map((m) => m.value);
  // 已配置的按钮明细（自定义名称 / 默认审批意见），旧格式只有 key
  const detailMap = {};
  (node.opMenuDetails || []).forEach((d) => {
    if (d && d.key) detailMap[d.key] = d;
  });
  // 实际生效的按钮：已配置取配置值，未配置取默认菜单
  const keys = (node.opMenus || []).filter((v) => allowed.includes(v));
  const defaults = opMenuDraft.isStart ? START_OP_MENUS : DEFAULT_OP_MENUS;
  const effective = keys.length ? keys : defaults;
  opMenuDraft.rows = OP_MENU_OPTIONS.filter((m) => allowed.includes(m.value)).map((m) => {
    const d = detailMap[m.value] || {};
    return {
      key: m.value,
      label: m.label,
      enabled: effective.includes(m.value),
      name: d.name || '',
      defaultOpinion: d.defaultOpinion || '',
      hasOpinion: !opMenuDraft.isStart && OP_MENU_OPINION_KEYS.includes(m.value),
    };
  });
  // 签字意见设置：完整配置优先，回退节点开关（默认启用 / 非必填 / 无默认意见）
  const sj = (node.signOpinionJson && typeof node.signOpinionJson === 'object') ? node.signOpinionJson : {};
  opMenuDraft.sign = {
    enabled: sj.enabled !== undefined ? !!sj.enabled : node.signOpinion !== false,
    required: !!sj.required,
    defaultValue: sj.defaultValue || '',
  };
  opMenuOpen.value = true;
}

function resetOpMenuDefault() {
  // 申请节点只保留发起场景按钮；审批节点默认提交、退回、打印、抄送；明细清空
  const defaults = opMenuDraft.isStart ? START_OP_MENUS : DEFAULT_OP_MENUS;
  opMenuDraft.rows.forEach((r) => {
    r.enabled = defaults.includes(r.key);
    r.name = '';
    r.defaultOpinion = '';
  });
  opMenuDraft.sign = { enabled: true, required: false, defaultValue: '' };
}

/**
 * 保存节点「签字意见」开关到 BPMN 扩展属性 flowable:signOpinion
 * 与操作菜单同一存法（随流程定义部署），办理页据此决定是否显示意见框
 */
function setSignOpinion(v) {
  const se = selectedElement.value;
  if (!graphicModeler || !se) return;
  const el = graphicModeler.get('elementRegistry').get(se.id);
  if (!el) {
    proxy.$modal.msgWarning('节点已不存在，请重新选中');
    return;
  }
  const val = v ? 'true' : 'false';
  const props = { 'flowable:signOpinion': val };
  // 已保存过完整签字意见配置（操作菜单弹窗）时，同步 enabled，避免两处开关打架
  const rawJson =
    (el.businessObject.get && el.businessObject.get('flowable:signOpinionJson')) ||
    (el.businessObject.$attrs && el.businessObject.$attrs['flowable:signOpinionJson']) ||
    '';
  if (rawJson) {
    try {
      const obj = JSON.parse(rawJson) || {};
      obj.enabled = !!v;
      props['flowable:signOpinionJson'] = JSON.stringify(obj);
    } catch (e) {}
  }
  graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, props);
  try {
    el.businessObject.$attrs['flowable:signOpinion'] = val;
    if (props['flowable:signOpinionJson']) el.businessObject.$attrs['flowable:signOpinionJson'] = props['flowable:signOpinionJson'];
  } catch (e) {}
  se.signOpinion = !!v;
  refreshGraphicData();
  proxy.$modal.msgSuccess((v ? '已启用' : '已关闭') + '该节点的签字意见，点右上"保存"后生效');
}

/**
 * 保存节点「强制收回」开关到 BPMN 扩展属性 flowable:allowRecall
 * 开启后该节点办理人在下一节点办理前可在「已办」收回流程
 */
function setAllowRecall(v) {
  const se = selectedElement.value;
  if (!graphicModeler || !se) return;
  const el = graphicModeler.get('elementRegistry').get(se.id);
  if (!el) {
    proxy.$modal.msgWarning('节点已不存在，请重新选中');
    return;
  }
  const val = v ? 'true' : 'false';
  graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, { 'flowable:allowRecall': val });
  try {
    el.businessObject.$attrs['flowable:allowRecall'] = val;
  } catch (e) {}
  se.allowRecall = !!v;
  refreshGraphicData();
  proxy.$modal.msgSuccess((v ? '已允许' : '已关闭') + '该节点的强制收回，点右上"保存"后生效');
}

function saveNodeOpMenu() {
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(opMenuDraft.nodeId);
  if (!el) {
    proxy.$modal.msgWarning('节点已不存在，请重新打开');
    return;
  }
  // 申请节点只保存发起场景按钮：历史配置里的退回/转办/加签/终止一并丢弃
  const allowed = opMenuDraft.isStart ? START_OP_MENUS : OP_MENU_OPTIONS.map((m) => m.value);
  const rows = opMenuDraft.rows.filter((r) => r.enabled && allowed.includes(r.key));
  // 新格式：对象数组 [{key, name, defaultOpinion}]，与默认一致的自定义名称/空意见不存，保持 XML 精简
  const json = rows.length
    ? JSON.stringify(
        rows.map((r) => {
          const o = { key: r.key };
          const name = (r.name || '').trim();
          if (name && name !== r.label) o.name = name;
          const op = (r.defaultOpinion || '').trim();
          if (r.hasOpinion && op) o.defaultOpinion = op;
          return o;
        })
      )
    : undefined;
  graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, { 'flowable:operationMenus': json });
  try {
    if (json) el.businessObject.$attrs['flowable:operationMenus'] = json;
    else delete el.businessObject.$attrs['flowable:operationMenus'];
  } catch (e) {}
  // 签字意见设置（申请节点无独立意见框，不写）：完整 JSON + 旧开关属性保持同步
  if (!opMenuDraft.isStart) {
    const sign = opMenuDraft.sign;
    const signJson = JSON.stringify({ enabled: !!sign.enabled, required: !!sign.required, defaultValue: (sign.defaultValue || '').trim() });
    graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, {
      'flowable:signOpinionJson': signJson,
      'flowable:signOpinion': sign.enabled ? 'true' : 'false',
    });
    try {
      el.businessObject.$attrs['flowable:signOpinionJson'] = signJson;
      el.businessObject.$attrs['flowable:signOpinion'] = sign.enabled ? 'true' : 'false';
    } catch (e) {}
  }
  if (selectedElement.value && selectedElement.value.id === opMenuDraft.nodeId) {
    const details = rows.map((r) => ({
      key: r.key,
      name: (r.name || '').trim() === r.label ? '' : (r.name || '').trim(),
      defaultOpinion: r.hasOpinion ? (r.defaultOpinion || '').trim() : '',
    }));
    selectedElement.value.opMenus = details.map((d) => d.key);
    selectedElement.value.opMenuDetails = details;
    // 签字意见开关随弹窗保存（申请节点仍取节点属性）
    selectedElement.value.signOpinion = opMenuDraft.isStart ? parseSignOpinion(el.businessObject) : !!opMenuDraft.sign.enabled;
    selectedElement.value.signOpinionJson = opMenuDraft.isStart
      ? parseSignOpinionJson(el.businessObject)
      : { enabled: !!opMenuDraft.sign.enabled, required: !!opMenuDraft.sign.required, defaultValue: (opMenuDraft.sign.defaultValue || '').trim() };
  }
  refreshGraphicData();
  opMenuOpen.value = false;
  proxy.$modal.msgSuccess('操作菜单已保存，点右上"保存"生效');
}

/** 表格编辑：连线分支条件（编辑弹窗的独立实例，与画布右侧属性面板同一写法） */
function updateExitCondition(row) {
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(row.id);
  if (!el) return;
  const expr = (row.condition || '').trim();
  const m = graphicModeler.get('modeling');
  if (expr) {
    const moddle = graphicModeler.get('moddle');
    const cond = moddle.create('bpmn:FormalExpression', { body: expr });
    m.updateProperties(el, { conditionExpression: cond });
  } else {
    m.updateProperties(el, { conditionExpression: undefined });
  }
  refreshGraphicData();
}

/** 编辑弹窗节点/出口清单（从独立实例解析） */
/** 开始/结束事件统一为 80x80（与 100x80 的处理节点同高），避免比处理节点明显偏小 */
const WM_EVENT_SIZE = DEFAULT_EVENT_SIZE;
function normalizeEventSize(el) {
  if (!graphicModeler || !el || (el.type !== 'bpmn:StartEvent' && el.type !== 'bpmn:EndEvent')) return;
  if (el.width >= WM_EVENT_SIZE) return;
  try {
    graphicModeler.get('modeling').resizeShape(el, {
      x: el.x + (el.width - WM_EVENT_SIZE) / 2,
      y: el.y + (el.height - WM_EVENT_SIZE) / 2,
      width: WM_EVENT_SIZE,
      height: WM_EVENT_SIZE,
    });
  } catch (e) {
    /* 规则不允许时忽略 */
  }
}
function normalizeAllEventSizes() {
  if (!graphicModeler) return;
  const registry = graphicModeler.get('elementRegistry');
  registry.filter((el) => el.type === 'bpmn:StartEvent' || el.type === 'bpmn:EndEvent').forEach(normalizeEventSize);
}

/**
 * 处理节点尺寸规范化：不低于 100x80，节点名过长时按文字宽度加宽（不压到相邻节点）。
 * 历史模型里存在 100x60 这种被压扁的节点，画在流程图里比相邻节点矮一截、节点名贴着上下边框，
 * 看起来就是「节点太窄」。这里统一撑到与标准节点一致：
 * - 只放大不缩小，尊重手工拉大过的节点；
 * - 以节点中心为基准缩放，连线端点仍落在边线中点；
 * - 加宽前先算左右相邻节点的空隙，空隙不够就不加宽（避免两个节点叠在一起），交给 bpmn-js 自动折行。
 */
const WM_TASK_MIN_WIDTH = 100;
const WM_TASK_MIN_HEIGHT = 80;
const WM_TASK_PAD_X = 24;
const WM_TASK_MAX_WIDTH = 280;
const WM_TASK_GAP = 16;
const WM_TASK_TYPES = ['bpmn:UserTask', 'bpmn:Task', 'bpmn:ServiceTask', 'bpmn:ScriptTask', 'bpmn:ManualTask', 'bpmn:BusinessRuleTask', 'bpmn:SendTask', 'bpmn:ReceiveTask', 'bpmn:CallActivity'];
let wmLabelMeasureCtx = null;
/** 节点名自然宽度（bpmn-js 标签默认字体 12px Arial） */
function measureNodeNameWidth(text) {
  if (!text) return 0;
  if (!wmLabelMeasureCtx) {
    const cv = document.createElement('canvas');
    wmLabelMeasureCtx = cv.getContext('2d');
    wmLabelMeasureCtx.font = '12px Arial, sans-serif';
  }
  return wmLabelMeasureCtx.measureText(String(text)).width;
}
/** 节点左右两侧到相邻节点之间的空隙（该侧没有相邻节点时为 Infinity） */
function nodeSideRoom(el) {
  let left = Infinity;
  let right = Infinity;
  try {
    graphicModeler
      .get('elementRegistry')
      .filter((e) => e.type === 'bpmn:SequenceFlow')
      .forEach((flow) => {
        const other = flow.source === el ? flow.target : flow.target === el ? flow.source : null;
        if (!other || other === el || !other.width) return;
        if (other.x >= el.x + el.width) right = Math.min(right, other.x - (el.x + el.width));
        else if (other.x + other.width <= el.x) left = Math.min(left, el.x - (other.x + other.width));
      });
  } catch (e) {
    /* 画布未就绪时忽略 */
  }
  return { left, right };
}
function normalizeNodeSize(el) {
  if (!graphicModeler || !el || WM_TASK_TYPES.indexOf(el.type) < 0) return;
  if (!el.width || !el.height) return;
  const name = (el.businessObject && el.businessObject.name) || '';
  let wantW = Math.min(WM_TASK_MAX_WIDTH, Math.max(WM_TASK_MIN_WIDTH, Math.ceil(measureNodeNameWidth(name)) + WM_TASK_PAD_X * 2));
  if (wantW > el.width) {
    const { left, right } = nodeSideRoom(el);
    const room = Math.min(left, right);
    if (isFinite(room)) wantW = Math.min(wantW, el.width + Math.max(0, (room - WM_TASK_GAP) * 2));
  }
  const needW = Math.max(el.width, wantW);
  const needH = Math.max(WM_TASK_MIN_HEIGHT, el.height);
  if (needW === el.width && needH === el.height) return;
  try {
    graphicModeler.get('modeling').resizeShape(el, {
      x: Math.round(el.x - (needW - el.width) / 2),
      y: Math.round(el.y - (needH - el.height) / 2),
      width: needW,
      height: needH,
    });
  } catch (e) {
    /* 规则不允许时忽略 */
  }
}
function normalizeAllNodeSizes() {
  if (!graphicModeler) return;
  try {
    graphicModeler
      .get('elementRegistry')
      .filter((el) => WM_TASK_TYPES.indexOf(el.type) >= 0)
      .forEach(normalizeNodeSize);
  } catch (e) {
    /* 画布未就绪时忽略 */
  }
}

function refreshGraphicData() {
  if (!graphicModeler) {
    graphicNodes.value = [];
    graphicExits.value = [];
    return;
  }
  try {
    const registry = graphicModeler.get('elementRegistry');
    graphicNodes.value = registry
      .filter((el) => el.businessObject && el.businessObject.$instanceOf && el.businessObject.$instanceOf('bpmn:FlowNode'))
      .map((el) => {
        const bo = el.businessObject;
        return {
          id: bo.id,
          name: bo.name || '',
          type: el.type,
          typeLabel: semanticNodeLabel(el.type),
          isUserTask: el.type === 'bpmn:UserTask',
          isStart: el.type === 'bpmn:StartEvent',
          isEnd: el.type === 'bpmn:EndEvent',
          assignee: (bo.get && bo.get('flowable:assignee')) || bo.assignee || '',
          candidateGroups: (bo.get && bo.get('flowable:candidateGroups')) || bo.candidateGroups || '',
          formKey: nodeFormMap.value[bo.id] && nodeFormMap.value[bo.id].formKey ? nodeFormMap.value[bo.id].formKey : (bo.get && bo.get('flowable:formKey')) || bo.formKey || '',
          nodeFormName: nodeFormMap.value[bo.id] ? nodeFormMap.value[bo.id].formName || '' : (bo.get && bo.get('flowable:nodeFormName')) || (bo.$attrs && bo.$attrs['flowable:nodeFormName']) || '',
          nodeFormSnapshot: nodeFormMap.value[bo.id] ? { fields: nodeFormMap.value[bo.id].fields || [], grid: nodeFormMap.value[bo.id].grid || null } : readNodeFormSnapshot(bo),
          opMenus: parseOpMenus(bo),
          opMenuDetails: parseOpMenuDetails(bo),
          signOpinion: parseSignOpinion(bo),
          signOpinionJson: parseSignOpinionJson(bo),
          allowRecall: parseAllowRecall(bo),
          participants: parseParticipantConfig(bo).participants || [],
          operatorGroups: parseOperatorGroups(bo),
          multiInstance: !!bo.loopCharacteristics,
        };
      });
    graphicExits.value = registry
      .filter((el) => el.type === 'bpmn:SequenceFlow')
      .map((el) => {
        const bo = el.businessObject;
        let meta = {};
        try {
          meta = JSON.parse((bo.get && bo.get('flowable:exitMeta')) || (bo.$attrs && bo.$attrs['flowable:exitMeta']) || '{}') || {};
        } catch (e) {}
        return {
          id: bo.id,
          name: bo.name || '',
          sourceId: bo.sourceRef ? bo.sourceRef.id : '',
          sourceName: (bo.sourceRef && bo.sourceRef.name) || '',
          targetId: bo.targetRef ? bo.targetRef.id : '',
          targetName: (bo.targetRef && bo.targetRef.name) || '',
          condition: bo.conditionExpression ? bo.conditionExpression.body || '' : '',
          isReturn: !!meta.isReturn,
          extraRule: meta.extraRule || '',
          genNo: !!meta.genNo,
        };
      });
  } catch (e) {
    graphicNodes.value = [];
    graphicExits.value = [];
  }
}

/** 出口信息：按当前节点过滤（「请选择当前节点」） */
const exitFilterNodeId = ref('');
const filteredGraphicExits = computed(() => {
  if (!exitFilterNodeId.value) return graphicExits.value;
  return graphicExits.value.filter((e) => e.sourceId === exitFilterNodeId.value);
});
function onExitFilterChange() {
  graphicSelectedLine.value = null;
}

/** 出口元数据（是否退回/附加规则/生成编号）写回 BPMN 扩展属性 flowable:exitMeta（同 fieldPerms 模式） */
function updateExitMeta(row, key, val) {
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(row.id);
  if (!el) return;
  let meta = {};
  try {
    meta = JSON.parse((el.businessObject.get && el.businessObject.get('flowable:exitMeta')) || '{}') || {};
  } catch (e) {}
  meta[key] = val;
  const json = meta.isReturn || meta.extraRule || meta.genNo ? JSON.stringify(meta) : undefined;
  try {
    el.businessObject.$attrs['flowable:exitMeta'] = json;
  } catch (e) {}
  row[key] = key === 'isReturn' || key === 'genNo' ? !!val : val || '';
  applyWmCanvasMarkers(graphicModeler);
  applyWmReturnRoutes(graphicModeler);
  proxy.$modal.msgSuccess(key === 'isReturn' ? '已标记为退回出口，点"保存"生效' : '出口设置已更新，点"保存"生效');
}

/** 同源出口顺序调整（上移/下移）：顺序即排他/包容网关判断优先级（写回 BPMN flowElements 顺序，保存后生效） */
function canMoveExit(row, dir) {
  if (!row || !row.sourceId) return false;
  const siblings = graphicExits.value.filter((e) => e.sourceId === row.sourceId);
  if (siblings.length < 2) return false;
  const idx = siblings.findIndex((e) => e.id === row.id);
  return dir === -1 ? idx > 0 : idx < siblings.length - 1 && idx !== -1;
}
function moveExitOrder(row, dir) {
  if (!graphicModeler || !canMoveExit(row, dir)) return;
  const el = graphicModeler.get('elementRegistry').get(row.id);
  const root = graphicModeler.get('canvas').getRootElement();
  const flowElements = root.businessObject.flowElements || [];
  const siblings = flowElements.filter((f) => f.$type === 'bpmn:SequenceFlow' && f.sourceRef && f.sourceRef.id === row.sourceId);
  const idx = siblings.findIndex((f) => f.id === row.id);
  if (idx === -1) return;
  const swapWith = dir === -1 ? siblings[idx - 1] : siblings[idx + 1];
  if (!swapWith) return;
  const i1 = flowElements.indexOf(el.businessObject);
  const i2 = flowElements.indexOf(swapWith);
  if (i1 === -1 || i2 === -1) return;
  flowElements[i1] = swapWith;
  flowElements[i2] = el.businessObject;
  // 直接改写 flowElements 顺序（不进撤销栈）：仅影响 XML 输出顺序与网关判断优先级
  refreshGraphicData();
  proxy.$modal.msgSuccess('出口顺序已调整，点"保存"生效');
}

// ===== 出口拖动排序（拖动手柄，sortablejs + 同源出口顺序写回 BPMN） =====
const exitTableRef = ref(null);
let exitDragSortable = null;

/** 拖拽结束：oldIndex/newIndex 映射过滤后行数据，重排同源出口在 BPMN flowElements 中的顺序 */
function onExitDragEnd(evt) {
  const fromRow = filteredGraphicExits.value[evt.oldIndex];
  const toRow = filteredGraphicExits.value[evt.newIndex];
  if (!fromRow || !toRow || fromRow.id === toRow.id) return;
  if (fromRow.sourceId !== toRow.sourceId) {
    proxy.$modal.msgWarning('仅同源节点的出口之间可拖动排序，已还原');
    refreshGraphicData();
    return;
  }
  if (!graphicModeler) return;
  const el = graphicModeler.get('elementRegistry').get(fromRow.id);
  const target = graphicModeler.get('elementRegistry').get(toRow.id);
  const root = graphicModeler.get('canvas').getRootElement();
  const flowElements = root.businessObject.flowElements || [];
  const siblings = flowElements.filter((f) => f.$type === 'bpmn:SequenceFlow' && f.sourceRef && f.sourceRef.id === fromRow.sourceId);
  const i1 = siblings.findIndex((f) => f.id === fromRow.id);
  const i2 = siblings.findIndex((f) => f.id === toRow.id);
  if (i1 === -1 || i2 === -1) return;
  // 重排：将 from 移除后按目标位置重新插入
  siblings.splice(i1, 1);
  const targetIdx = siblings.findIndex((f) => f.id === toRow.id);
  siblings.splice(targetIdx, 0, el.businessObject);
  // 写回 flowElements：按新顺序替换同源兄弟在数组中的位置
  let cursor = 0;
  for (let i = 0; i < flowElements.length; i++) {
    const f = flowElements[i];
    if (f.$type === 'bpmn:SequenceFlow' && f.sourceRef && f.sourceRef.id === fromRow.sourceId) {
      flowElements[i] = siblings[cursor++];
    }
  }
  // 直接改写 flowElements 顺序（不进撤销栈）：仅影响 XML 输出顺序与网关判断优先级
  refreshGraphicData();
  proxy.$modal.msgSuccess('出口顺序已调整，点"保存"生效');
}

/** exits 页签激活时初始化表格行拖拽（仅 exits 页签的表格 tbody）；watch 注册在 graphicTab 定义之后（见文件尾部） */
function initExitDragSort() {
  if (exitDragSortable) {
    exitDragSortable.destroy();
    exitDragSortable = null;
  }
  if (!exitTableRef.value) return;
  const tbody = exitTableRef.value.$el.querySelector('.el-table__body-wrapper tbody');
  if (!tbody) return;
  exitDragSortable = Sortable.create(tbody, {
    handle: '.drag-handle',
    animation: 150,
    onEnd: onExitDragEnd,
  });
}

// ===== 出口条件规则弹窗（参照出口条件设置：多规则 + AND/OR 组合为 JUEL 表达式） =====
const exitRulesOpen = ref(false);
const exitRulesRow = ref(null);
const exitRulesCopyFrom = ref('');
const exitRulesForm = ref({ join: 'AND', rows: [] });
// ===== 分支条件：选字段 + 运算符 + 值 自动拼 JUEL，不再让人手写表达式 =====
/** 运算符表（sym 为 JUEL 符号；空值判断不需要比较值） */
const COND_OPS = [
  { value: 'eq', label: '等于', sym: '==' },
  { value: 'neq', label: '不等于', sym: '!=' },
  { value: 'gt', label: '大于', sym: '>' },
  { value: 'gte', label: '大于等于', sym: '>=' },
  { value: 'lt', label: '小于', sym: '<' },
  { value: 'lte', label: '小于等于', sym: '<=' },
  { value: 'in', label: '属于（多值）', sym: '==' },
  { value: 'notin', label: '不属于（多值）', sym: '!=' },
  { value: 'empty', label: '为空', sym: '' },
  { value: 'notempty', label: '不为空', sym: '' },
];
/** 旧名字保留：出口条件弹窗仍按下拉引用 */
const RULE_OPS = COND_OPS;

/** 可作为条件字段的清单：主表单为主，叠加各节点表单里出现过的字段（去重） */
const condFieldOptions = computed(() => {
  const out = [];
  const seen = new Set();
  const push = (f) => {
    if (!f || !f.field || seen.has(f.field)) return;
    seen.add(f.field);
    const rtype = f.rtype || rtypeOf(f);
    out.push({ field: f.field, label: f.label || f.field, rtype, options: f.options && f.options.length ? f.options : optionListOf(f) });
  };
  (formFieldOptions.value || []).forEach(push);
  Object.keys(nodeFormMap.value || {}).forEach((id) => {
    const nf = nodeFormMap.value[id];
    ((nf && nf.fields) || []).forEach(push);
  });
  return out;
});

function fieldMetaOf(field) {
  if (!field) return null;
  return condFieldOptions.value.find((f) => f.field === field) || null;
}

/** 字段的值类型：options（有选项）/ date / time / number / text */
function condValueKind(row) {
  const meta = fieldMetaOf(row && row.field);
  if (!meta) return 'text';
  if (meta.options && meta.options.length) return 'options';
  const rt = meta.rtype || '';
  if (rt === 'date' || rt === 'datetime') return 'date';
  if (rt === 'time') return 'time';
  if (rt === 'number') return 'number';
  return 'text';
}
function isMultiValueOp(op) {
  return op === 'in' || op === 'notin';
}
/** 是否要填比较值（为空/不为空不用填） */
function condNeedsValue(row) {
  return !!row && row.op !== 'empty' && row.op !== 'notempty';
}
/** 可选值（下拉/单选框字段才能选具体值） */
function condValueOptions(row) {
  const meta = fieldMetaOf(row && row.field);
  return meta && meta.options ? meta.options : [];
}
/** 当前字段能用哪些运算符：文本类只给等值/集合/空值，数字与日期再加大小比较 */
function condOpsFor(row) {
  const kind = condValueKind(row);
  return COND_OPS.filter((o) => (['gt', 'gte', 'lt', 'lte'].includes(o.value) ? kind === 'number' || kind === 'date' : true));
}

/** 比较值 → JUEL 字面量：数字字段不加引号，其余（含选项值）统一按字符串加引号 */
function condLiteral(field, raw) {
  const text = String(raw == null ? '' : raw).trim();
  if (!text) return '';
  const meta = fieldMetaOf(field);
  const numeric = meta ? meta.rtype === 'number' : /^-?\d+(\.\d+)?$/.test(text);
  if (numeric) return text;
  return "'" + text.replace(/'/g, "''") + "'";
}

/** 单条规则 → JUEL 片段（裸片段，不含 ${}；JUEL 只允许整体一个 ${...}） */
function ruleToExpr(r) {
  if (!r || !r.field) return '';
  const field = r.field;
  const op = r.op || 'eq';
  if (op === 'empty') return 'empty ' + field;
  if (op === 'notempty') return 'not empty ' + field;
  const raw = Array.isArray(r.value) ? r.value.join(',') : r.value;
  const text = String(raw == null ? '' : raw).trim();
  if (!text) return '';
  const def = COND_OPS.find((o) => o.value === op);
  if (!def || !def.sym) return '';
  if (isMultiValueOp(op)) {
    const vals = text
      .split(/[,，]/)
      .map((s) => s.trim())
      .filter(Boolean);
    if (!vals.length) return '';
    return '(' + vals.map((v) => field + ' ' + def.sym + ' ' + condLiteral(field, v)).join(op === 'in' ? ' || ' : ' && ') + ')';
  }
  return field + ' ' + def.sym + ' ' + condLiteral(field, text);
}

/** 多条规则 → 完整表达式：${a > 1 && b == 'x'} */
function rulesToExpr(rows, join) {
  const parts = (rows || []).map(ruleToExpr).filter(Boolean);
  if (!parts.length) return '';
  let body = parts[0];
  if (parts.length > 1) {
    const joiner = join === 'OR' ? ' || ' : ' && ';
    body = parts.map((p) => (/^\(.*\)$/.test(p) ? p : '(' + p + ')')).join(joiner);
  }
  return '${' + body + '}';
}

/** 把 "a == 1 || a == 2" 拆出来的多行合并回「属于（多值）」，避免来回切换后规则行变多 */
function mergeInRows(rows, join) {
  const out = [];
  (rows || []).forEach((r) => {
    const prev = out[out.length - 1];
    const toArr = (v) => (Array.isArray(v) ? v.slice() : String(v == null ? '' : v).split(/[,，]/).filter(Boolean));
    const canMerge =
      prev &&
      r.value !== '' &&
      prev.field === r.field &&
      ((join === 'OR' && (prev.op === 'eq' || prev.op === 'in') && r.op === 'eq') || (join === 'AND' && (prev.op === 'neq' || prev.op === 'notin') && r.op === 'neq'));
    if (canMerge) {
      prev.op = join === 'OR' ? 'in' : 'notin';
      const merged = toArr(prev.value).concat(toArr(r.value));
      prev.value = merged.filter((v, i) => merged.indexOf(v) === i);
      return;
    }
    out.push({ ...r });
  });
  return out;
}

/** 表达式 → 规则行（兼容 ${a > 3}、旧写法 ${a} > 3、裸写法 a > 3） */
function parseConditionRules(cond) {
  const raw = String(cond || '').trim();
  if (!raw) return { join: 'AND', rows: [] };
  const body = raw.replace(/\$\{([^}]*)\}/g, '$1').trim();
  const rows = [];
  const opMap = { '==': 'eq', '!=': 'neq', '>': 'gt', '>=': 'gte', '<': 'lt', '<=': 'lte' };
  const segRe = /(not\s+empty\s+\w+)|(empty\s+\w+)|(\w+\s*(?:==|!=|>=|<=|>|<)\s*(?:'[^']*'|"[^"]*"|-?\d+(?:\.\d+)?))/g;
  let m;
  while ((m = segRe.exec(body)) !== null) {
    const seg = String(m[0] || '').trim();
    const empty = /^(not\s+empty|empty)\s+(\w+)$/.exec(seg);
    if (empty) {
      rows.push({ field: empty[2], op: /^not/i.test(empty[1]) ? 'notempty' : 'empty', value: '' });
      continue;
    }
    const cmp = /^(\w+)\s*(==|!=|>=|<=|>|<)\s*(.+)$/.exec(seg);
    if (!cmp) continue;
    rows.push({ field: cmp[1], op: opMap[cmp[2]] || 'eq', value: cmp[3].replace(/^['"]|['"]$/g, '') });
  }
  const join = body.includes('||') ? 'OR' : 'AND';
  return { join, rows: mergeInRows(rows, join) };
}

/** 换字段/换运算符后校正：运算符不适用就回退等值，值类型跟着变 */
function normalizeCondRow(row) {
  if (!row) return;
  if (!condOpsFor(row).some((o) => o.value === row.op)) row.op = 'eq';
  if (!condNeedsValue(row)) return;
  if (isMultiValueOp(row.op)) {
    if (!Array.isArray(row.value)) {
      row.value = String(row.value == null ? '' : row.value)
        .split(/[,，]/)
        .map((s) => s.trim())
        .filter(Boolean);
    }
  } else {
    if (Array.isArray(row.value)) row.value = row.value[0] || '';
    const opts = condValueOptions(row);
    if (opts.length && !String(row.value == null ? '' : row.value)) row.value = opts[0].value;
    if (condValueKind(row) === 'number' && row.value !== '' && row.value !== null && row.value !== undefined) row.value = Number(row.value);
  }
}

/** 出口条件弹窗的多规则 → 最终表达式 */
const exitRulesExpr = computed(() => rulesToExpr(exitRulesForm.value.rows, exitRulesForm.value.join));

/** 出口条件弹窗：字段/运算符切换后的校正 */
function onRuleFieldChange(row) {
  normalizeCondRow(row);
}
function onRuleOpChange(row) {
  normalizeCondRow(row);
}

// ---- 画布右侧属性面板的分支条件（与出口条件弹窗同一套规则模型） ----
const condAdvanced = ref(false);
const condJoin = ref('AND');
const condRows = ref([{ field: '', op: 'eq', value: '' }]);
const condExpr = computed(() => rulesToExpr(condRows.value, condJoin.value));

/** 选中连线时把已有表达式反解为规则行；解析不了则进「表达式」模式，避免智能模式改坏已有条件 */
function initConditionBuilder(cond) {
  const text = String(cond || '').trim();
  const parsed = parseConditionRules(text);
  condJoin.value = parsed.join;
  condRows.value = parsed.rows.length ? parsed.rows : [{ field: '', op: 'eq', value: '' }];
  condAdvanced.value = !parsed.rows.length && !!text;
}
function addCondRow() {
  condRows.value.push({ field: '', op: 'eq', value: '' });
}
function removeCondRow(idx) {
  if (condRows.value.length <= 1) return;
  condRows.value.splice(idx, 1);
  syncCondition();
}
function onCondFieldChange(row) {
  normalizeCondRow(row);
  syncCondition();
}
function onCondOpChange(row) {
  normalizeCondRow(row);
  syncCondition();
}

/** 智能模式改动 → 立即写回连线的 conditionExpression（落库仍由右上「保存」完成） */
function syncCondition() {
  if (!selectedElement.value || condAdvanced.value) return;
  const expr = condExpr.value;
  selectedElement.value.condition = expr;
  updateCondition();
}
/** 智能配置 ↔ 表达式 切换 */
function onCondModeChange(val) {
  if (!selectedElement.value) return;
  if (val) {
    // 进表达式模式：把刚配的规则带过去，避免用户重写
    selectedElement.value.condition = condExpr.value;
    updateCondition();
    return;
  }
  const cond = String(selectedElement.value.condition || '').trim();
  const parsed = parseConditionRules(cond);
  if (!parsed.rows.length && cond) {
    proxy.$modal.msgWarning('该条件较复杂，无法自动拆成规则，请继续用「表达式」编辑');
    condAdvanced.value = true;
    return;
  }
  condJoin.value = parsed.join;
  condRows.value = parsed.rows.length ? parsed.rows : [{ field: '', op: 'eq', value: '' }];
}

/** 打开条件规则弹窗：从现有 conditionExpression 反解（简单表达式拆分为单规则） */
function openExitRules(row) {
  exitRulesRow.value = row;
  exitRulesCopyFrom.value = '';
  loadFormFieldOptions();
  const parsed = parseConditionRules(row && row.condition);
  exitRulesForm.value = parsed.rows.length ? parsed : { join: 'AND', rows: [{ field: '', op: 'eq', value: '' }] };
  exitRulesOpen.value = true;
}

function addRuleRow() {
  exitRulesForm.value.rows.push({ field: '', op: 'eq', value: '' });
}
function removeRuleRow() {
  if (exitRulesForm.value.rows.length > 1) exitRulesForm.value.rows.pop();
}

/** 从其他出口引用规则（规则库复用：该模型内已配置条件的出口） */
const otherRuleExits = computed(() => {
  return graphicExits.value.filter((e) => e.condition && (!exitRulesRow.value || e.id !== exitRulesRow.value.id));
});
function applyCopyExitRules() {
  const src = otherRuleExits.value.find((e) => e.id === exitRulesCopyFrom.value);
  if (!src) return;
  openExitRulesInto(src.condition);
  proxy.$modal.msgSuccess('已复制「' + (src.name || src.id) + '」的规则，可继续调整后保存');
}
function openExitRulesInto(cond) {
  const parsed = parseConditionRules(cond);
  exitRulesForm.value = parsed.rows.length ? parsed : { join: 'AND', rows: [{ field: '', op: 'eq', value: '' }] };
}

/** 保存：最终表达式写回出口条件（conditionExpression），同时刷新表格 */
function saveExitRules() {
  if (!exitRulesRow.value) return;
  const expr = exitRulesExpr.value;
  const row = graphicExits.value.find((e) => e.id === exitRulesRow.value.id);
  if (row) {
    row.condition = expr;
    updateExitCondition(row);
  }
  exitRulesOpen.value = false;
  proxy.$modal.msgSuccess(expr ? '出口条件已保存，点"保存"生效' : '出口条件已清除，点"保存"生效');
}
function clearExitRules() {
  if (!exitRulesRow.value) return;
  const row = graphicExits.value.find((e) => e.id === exitRulesRow.value.id);
  if (row) {
    row.condition = '';
    updateExitCondition(row);
  }
  exitRulesOpen.value = false;
  proxy.$modal.msgSuccess('出口条件已清除，点"保存"生效');
}

// ===== 节点编辑操作组弹窗（参照「编辑操作组」：8 种操作者类型 + 多组 + 条件/批次/会签） =====
const OPERATOR_TYPES = [
  { value: 'user', label: '指定人' },
  { value: 'dept', label: '指定部门' },
  { value: 'subDept', label: '指定分部' },
  { value: 'role', label: '指定角色' },
  { value: 'post', label: '指定岗位' },
  { value: 'all', label: '所有人' },
  { value: 'creator', label: '流程创建人' },
  { value: 'nodeOp', label: '节点操作者' },
];
const opGroupsOpen = ref(false);
const opGroupsNodeName = ref('');
const opGroupsNodeId = ref('');
const opGroupsActiveNames = ref(['0']);
let opGroupsTargetRow = null;

/** 解析节点操作组（BPMN 扩展属性 flowable:operatorGroups，同 exitMeta 模式） */
function parseOperatorGroups(bo) {
  let raw = '';
  try {
    raw = (bo.get && bo.get('flowable:operatorGroups')) || (bo.$attrs && bo.$attrs['flowable:operatorGroups']) || '';
  } catch (e) {}
  if (!raw) return [];
  try {
    return JSON.parse(raw) || [];
  } catch (e) {
    return [];
  }
}

/** 节点信息表格操作组摘要（如「2组·财务确认」） */
function operatorGroupSummary(row) {
  if (!row || !row.isUserTask) return '—';
  const groups = row.operatorGroups || [];
  if (!groups.length) return '未设置';
  const names = groups.map((g) => g.name || g.refName || OPERATOR_TYPES.find((t) => t.value === g.type)?.label || '').filter(Boolean);
  return groups.length + '组' + (names.length ? '·' + names.slice(0, 2).join('/') : '');
}
function opGroupTitle(g) {
  const t = OPERATOR_TYPES.find((x) => x.value === g.type);
  const ref = g.refName ? '：' + g.refName : '';
  const counter = { single: '单人', or: '或签', and: '会签' }[g.counterSign] || '';
  return ((t && t.label) || '') + ref + (counter ? ' · ' + counter : '');
}
function opTypeLabel(t) {
  const x = OPERATOR_TYPES.find((v) => v.value === t);
  return (x && x.label) || '';
}
function opUserLabel(id) {
  const u = userOptions.value.find((x) => String(x.userId) === String(id));
  return u ? u.nickName + '（' + u.userName + '）' : '';
}
function opRoleLabel(id) {
  const r = roleOptions.value.find((x) => String(x.roleId) === String(id));
  return r ? r.roleName + '（' + r.roleKey + '）' : '';
}
function opDeptLabel(id) {
  const find = (list) => {
    for (const n of list || []) {
      if (String(n.id) === String(id)) return n.label;
      const r = find(n.children);
      if (r) return r;
    }
    return null;
  };
  return find(deptTreeOptions.value) || '';
}
function opPostLabel(id) {
  const p = postOptions.value.find((x) => String(x.postId) === String(id));
  return p ? p.postName + '（' + p.postCode + '）' : '';
}

/** 部门树/岗位选项（操作组指定部门/分部/岗位选择器） */
const deptTreeOptions = ref([]);
const postOptions = ref([]);
function loadOperatorRefOptions() {
  request({ url: '/system/user/deptTree', method: 'get' })
    .then((res) => {
      deptTreeOptions.value = res.data || [];
    })
    .catch((e) => {
      proxy.$modal.msgWarning('部门树加载失败，部门选择器将为空：' + (e?.message || e));
    });
  request({ url: '/system/post/list', method: 'get', params: { pageNum: 1, pageSize: 200 } })
    .then((res) => {
      postOptions.value = res.rows || [];
    })
    .catch((e) => {
      proxy.$modal.msgWarning('岗位列表加载失败，岗位选择器将为空：' + (e?.message || e));
    });
}

/** 打开操作组弹窗：从 BPMN 扩展属性回填；无数据时按现有 assignee/candidateGroups 初始化一组 */
function openOperatorGroups(row) {
  if (!row || !row.isUserTask) return;
  opGroupsTargetRow = row;
  opGroupsNodeId.value = row.id;
  opGroupsNodeName.value = row.name || row.id;
  let groups = row.operatorGroups || [];
  if (!groups.length) {
    if (row.assignee) {
      groups = [{ name: undefined, type: 'user', userId: row.assignee, refName: opUserLabel(row.assignee), counterSign: 'single', condition: '', batch: 0 }];
    } else if (row.candidateGroups) {
      groups = [{ name: undefined, type: 'role', roleId: row.candidateGroups, refName: opRoleLabel(row.candidateGroups), counterSign: 'single', condition: '', batch: 0 }];
    } else {
      groups = [{ name: undefined, type: 'user', userId: undefined, refName: '', counterSign: 'single', condition: '', batch: 0 }];
    }
  }
  opGroupsForm.value = groups;
  opGroupsActiveNames.value = ['0'];
  loadOperatorRefOptions();
  opGroupsOpen.value = true;
}
function addOperatorGroup() {
  opGroupsForm.value.push({ name: undefined, type: 'user', userId: undefined, roleId: undefined, refName: '', counterSign: 'single', condition: '', batch: 0 });
  opGroupsActiveNames.value = [String(opGroupsForm.value.length - 1)];
}
function removeOperatorGroup(gi) {
  if (!opGroupsForm.value.length) return;
  opGroupsForm.value.splice(gi, 1);
}

/** 保存操作组：写回 BPMN 扩展属性 + 同步首个指定人/角色到节点办理人 + 会签联动多实例 */
function saveOperatorGroups() {
  if (!graphicModeler || !opGroupsNodeId.value) return;
  const el = graphicModeler.get('elementRegistry').get(opGroupsNodeId.value);
  if (!el) {
    opGroupsOpen.value = false;
    return;
  }
  const groups = (opGroupsForm.value || []).filter((g) =>
    g.type === 'user' ? g.userId : g.type === 'role' ? g.roleId : g.type === 'dept' || g.type === 'subDept' || g.type === 'post' ? g.refId : true,
  );
  const json = groups.length ? JSON.stringify(groups) : undefined;
  const bo = el.businessObject;
  try {
    bo.$attrs['flowable:operatorGroups'] = json;
  } catch (e) {}
  // 同步节点基础办理人/候选组（兼容运行态既有链路）；部门/分部/岗位运行态由 OperatorGroupTaskListener 解析候选用户
  const first = groups[0];
  const m = graphicModeler.get('modeling');
  m.updateModdleProperties(el, bo, {
    'flowable:assignee': first && first.type === 'user' ? first.userId || undefined : undefined,
    'flowable:candidateGroups': first && first.type === 'role' ? first.roleId || undefined : undefined,
  });
  // 会签/或签联动多实例（会签=并行、或签=串行、单人=无）
  const counter = first && first.counterSign;
  if (counter === 'and' || counter === 'or') {
    const moddle = graphicModeler.get('moddle');
    const loop = moddle.create('bpmn:MultiInstanceLoopCharacteristics', {
      isSequential: counter === 'or',
      completionCondition: counter === 'and' ? moddle.create('bpmn:FormalExpression', { body: '${nrOfCompletedInstances >= nrOfInstances}' }) : undefined,
    });
    m.updateModdleProperties(el, bo, { loopCharacteristics: loop });
  } else {
    m.updateModdleProperties(el, bo, { loopCharacteristics: undefined });
  }
  // 刷新表格行数据
  if (opGroupsTargetRow) {
    opGroupsTargetRow.operatorGroups = groups;
    opGroupsTargetRow.assignee = first && first.type === 'user' ? first.userId || '' : '';
    opGroupsTargetRow.candidateGroups = first && first.type === 'role' ? first.roleId || '' : '';
    opGroupsTargetRow.multiInstance = !!(counter === 'and' || counter === 'or');
  }
  opGroupsOpen.value = false;
  proxy.$modal.msgSuccess('操作组已保存到节点，点"保存"生效');
}

function onGraphicNodeSelect(row) {
  graphicSelectedRow.value = row;
  if (row && graphicModeler) {
    const el = graphicModeler.get('elementRegistry').get(row.id);
    if (el) graphicModeler.get('selection').select(el);
  }
}
function onGraphicLineSelect(row) {
  graphicSelectedLine.value = row;
  if (row && graphicModeler) {
    const el = graphicModeler.get('elementRegistry').get(row.id);
    if (el) graphicModeler.get('selection').select(el);
  }
}

/** 勾选变化：记录当前勾选的节点行（供批量删除按钮的启用状态与删除范围使用） */
function onNodeSelectionChange(rows) {
  graphicCheckedNodes.value = rows || [];
}
/** 是否可被勾选删除：仅限 4 类语义节点（连线与无法识别的脏元素不可删） */
function isNodeSelectable(row) {
  return !!row && !!row.id && row.type !== 'bpmn:SequenceFlow';
}
/** 批量删除勾选的节点（含其关联连线一并移除，删除后清空勾选） */
function removeGraphicNodes() {
  if (!graphicModeler) return;
  const rows = graphicCheckedNodes.value;
  if (!rows || !rows.length) {
    proxy.$modal.msgWarning('请先勾选要删除的节点');
    return;
  }
  const registry = graphicModeler.get('elementRegistry');
  const targets = rows.map((r) => registry.get(r.id)).filter(Boolean);
  if (!targets.length) {
    proxy.$modal.msgWarning('勾选节点已不存在，请刷新后重试');
    return;
  }
  const names = rows.map((r) => r.name || r.id).join('、');
  proxy.$modal
    .confirm('确认删除勾选的 ' + targets.length + ' 个节点（' + names + '）？其关联连线将一并移除。')
    .then(() => {
      graphicModeler.get('modeling').removeElements(targets);
      graphicSelectedRow.value = null;
      graphicCheckedNodes.value = [];
      if (nodeTableRef.value) nodeTableRef.value.clearSelection();
      refreshGraphicData();
      applyWmCanvasMarkers(graphicModeler);
      applyWmReturnRoutes(graphicModeler);
      proxy.$modal.msgSuccess('已删除 ' + targets.length + ' 个节点，点"保存"生效');
    })
    .catch(() => {});
}

// 新增节点（在选中节点后追加）：界面仅 4 类业务语义，底层仍写标准 BPMN 元素
const ADD_NODE_TYPES = [
  { value: 'bpmn:UserTask', label: '审批节点' },
  { value: 'bpmn:ExclusiveGateway', label: '分支节点' },
  { value: 'bpmn:StartEvent', label: '发起节点' },
  { value: 'bpmn:EndEvent', label: '结束节点' },
];
// 分支方式：仅当节点类型为「分支节点」时需要选择（默认排他网关）
const ADD_BRANCH_MODES = [
  { value: 'bpmn:ExclusiveGateway', label: '排他分支（按出口条件选一条）' },
  { value: 'bpmn:ParallelGateway', label: '并行分支（全部出口同时激活）' },
];
function openAddNode() {
  if (!graphicSelectedRow.value) {
    proxy.$modal.msgWarning('请先在表格中选中一个节点（作为新节点的来源）');
    return;
  }
  addNodeForm.value = { type: 'bpmn:UserTask', branchMode: 'bpmn:ExclusiveGateway', name: undefined };
  addNodeOpen.value = true;
}
function confirmAddNode() {
  if (!graphicModeler || !graphicSelectedRow.value) return;
  const source = graphicModeler.get('elementRegistry').get(graphicSelectedRow.value.id);
  if (!source) {
    proxy.$modal.msgWarning('选中节点已不存在');
    return;
  }
  // 「分支节点」按所选分支方式落成标准网关元素；其余类型直接用语义类型
  const realType = addNodeForm.value.type === 'bpmn:ExclusiveGateway' ? addNodeForm.value.branchMode || 'bpmn:ExclusiveGateway' : addNodeForm.value.type;
  if (!realType || !String(realType).startsWith('bpmn:')) {
    proxy.$modal.msgError('节点类型无效，请重新选择');
    return;
  }
  try {
    graphicModeler.get('modeling').appendShape(source, { type: realType, name: addNodeForm.value.name || undefined });
  } catch (e) {
    proxy.$modal.msgError('新增节点失败：' + (e.message || e));
    addNodeOpen.value = false;
    return;
  }
  addNodeOpen.value = false;
  refreshGraphicData();
}
function removeGraphicNode() {
  if (!graphicModeler || !graphicSelectedRow.value) return;
  const el = graphicModeler.get('elementRegistry').get(graphicSelectedRow.value.id);
  if (el) graphicModeler.get('modeling').removeElements([el]);
  graphicSelectedRow.value = null;
  refreshGraphicData();
}

// 新增出口（来源→目标连线）
function openAddLine() {
  addLineForm.value = { sourceId: undefined, targetId: undefined, name: undefined };
  addLineOpen.value = true;
}
function confirmAddLine() {
  if (!graphicModeler || !addLineForm.value.sourceId || !addLineForm.value.targetId) {
    proxy.$modal.msgWarning('请选择来源/目标节点');
    return;
  }
  if (addLineForm.value.sourceId === addLineForm.value.targetId) {
    proxy.$modal.msgWarning('来源/目标节点不能相同');
    return;
  }
  const src = graphicModeler.get('elementRegistry').get(addLineForm.value.sourceId);
  const dst = graphicModeler.get('elementRegistry').get(addLineForm.value.targetId);
  if (!src || !dst) {
    proxy.$modal.msgWarning('来源/目标节点不存在');
    return;
  }
  graphicModeler.get('modeling').connect(src, dst, { type: 'bpmn:SequenceFlow', name: addLineForm.value.name || undefined });
  addLineOpen.value = false;
  refreshGraphicData();
}
function removeGraphicLine() {
  if (!graphicModeler || !graphicSelectedLine.value) return;
  const el = graphicModeler.get('elementRegistry').get(graphicSelectedLine.value.id);
  if (el) graphicModeler.get('modeling').removeElements([el]);
  graphicSelectedLine.value = null;
  refreshGraphicData();
}

// ===== 图形化编辑独立全屏弹窗（点击"编辑"进入独立编辑页面，关闭返回列表） =====
const graphicOpen = ref(false);
const graphicCanvasRef = ref(null);
const paletteHostRef = ref(null);
// 节点信息面板：右侧弹出，可收缩（点节点自动展开，手柄 ‹/› 切换）
const propsCollapsed = ref(false);
// 左侧节点面板（palette）：可收起，收起后画布铺满便于查看完整流程图
const paletteCollapsed = ref(false);
/** 收起/展开左侧节点面板：容器宽度变化后通知 bpmn-js 重算画布尺寸并适配视口 */
function togglePalette() {
  paletteCollapsed.value = !paletteCollapsed.value;
  nextTick(() => {
    if (!graphicModeler) return;
    try {
      graphicModeler.get('canvas').resized();
    } catch (e) {
      /* 画布不可用时忽略 */
    }
    safeFitViewport(graphicModeler);
  });
}
const graphicFileRef = ref(null);
let graphicModeler = null;
// 编辑弹窗：三个二级页签与节点/出口清单
const graphicTab = ref('graph');
const graphicNodes = ref([]);
const graphicExits = ref([]);
const graphicSelectedRow = ref(null);
const graphicSelectedLine = ref(null);
/** 节点表勾选集合（多选批量删除用）与表格实例引用 */
const graphicCheckedNodes = ref([]);
const nodeTableRef = ref(null);
const addNodeOpen = ref(false);
const addNodeForm = ref({ type: 'bpmn:UserTask', branchMode: 'bpmn:ExclusiveGateway', name: undefined });
const addLineOpen = ref(false);
const addLineForm = ref({ sourceId: undefined, targetId: undefined, name: undefined });

/** 打开独立编辑页面：按入口决定编辑模式（graph 只编辑图形 / nodes 只编辑节点 / exits 只编辑出口），一次只编辑一类 */
function openGraphicEditor(mode) {
  if (!designModel.value.modelKey) return;
  // 流转设置子页签名（gnodes/gexits）与编辑模式名（nodes/exits）不同名，此处做归一映射
  const MODE_ALIAS = { gnodes: 'nodes', gexits: 'exits', graph: 'graph', nodes: 'nodes', exits: 'exits' };
  const resolved = MODE_ALIAS[mode] || 'graph';
  graphicMode.value = resolved;
  graphicTab.value = resolved;
  // 每次打开编辑页，左侧节点面板保持展开
  paletteCollapsed.value = false;
  // 分支条件要能选表单字段（画布右侧属性面板 / 出口条件弹窗都靠它）
  loadFormFieldOptions();
  graphicOpen.value = true;
}
const graphicMode = ref('graph');
/** 编辑模式的中文名（顶部标题与页签标签共用） */
const GRAPHIC_MODE_LABEL = { graph: '图形编辑', nodes: '节点信息', exits: '出口信息' };
const graphicModeLabel = computed(() => GRAPHIC_MODE_LABEL[graphicMode.value] || '图形编辑');

/**
 * 安全适配视口：容器尺寸为 0（隐藏中/尚未布局）时直接跳过。
 * bpmn-js 的 fit-viewport 会按容器宽高算缩放，容器 0 尺寸时得到非有限值，
 * 内部 SVGMatrix.scale 抛 "The provided float value is non-finite"。
 */
function safeFitViewport(modeler) {
  if (!modeler) return;
  try {
    const el = modeler.get('canvas').getContainer ? modeler.get('canvas').getContainer() : null;
    const w = el ? el.clientWidth : 0;
    const h = el ? el.clientHeight : 0;
    if (!w || !h) return;
    modeler.get('canvas').zoom('fit-viewport', 'auto');
  } catch (e) {
    /* 画布不可用时忽略，等下次布局完成再适配 */
  }
}

/** 独立编辑页初始化：独立 bpmn-js 实例 + 导入当前模型 */
async function onGraphicOpened() {
  await import('bpmn-js/dist/assets/diagram-js.css');
  await import('bpmn-js/dist/assets/bpmn-js.css');
  await import('bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css');
  const { default: BpmnModeler } = await import('bpmn-js/lib/Modeler.js');
  const { default: LassoToolModule } = await import('diagram-js/lib/features/lasso-tool');
  const { default: WmReturnLayouter, isReturnExit } = await import('./WmReturnLayouter.js');
  wmReturnExitChecker = isReturnExit;
  graphicModeler = new BpmnModeler({
    container: graphicCanvasRef.value,
    additionalModules: [bpmnChineseTranslate, wmPaletteModule, wmContextPadModule, wmLabelLockModule, LassoToolModule, wmBoxSelectModule, { layouter: ['type', WmReturnLayouter] }],
  });
  // 选中事件 → 属性面板互通（独立实例的选中元素共享同一面板状态）
  const gEventBus = graphicModeler.get('eventBus');
  gEventBus.on('selection.changed', (e) => {
    selectedElSource = 'graphic';
    const el = e.newSelection[0];
    if (el) {
      loadSelectedElement(el);
      propsCollapsed.value = false;
    } else {
      selectedEl = null;
      selectedElement.value = null;
    }
  });
  // 从左侧节点面板拖入/新增/修改节点后立即套用同款节点视觉，保证画布样式与面板按钮一致
  gEventBus.on('shape.added', (e) => {
    markWmShape(e.element, graphicModeler);
    scheduleWmDecoSweep(graphicModeler);
    setTimeout(() => {
      normalizeEventSize(e.element);
      normalizeNodeSize(e.element);
      placeEventLabelInside(e.element, graphicModeler);
      // 标签元素（xxx_label）可能比图形晚一帧才建出来，再补一次
      setTimeout(() => placeEventLabelInside(e.element, graphicModeler), 60);
    }, 0);
  });
  gEventBus.on('element.changed', (e) => {
    markWmShape(e.element, graphicModeler);
    scheduleWmDecoSweep(graphicModeler);
  });
  // 撤销/重做：节点表、出口表、退回出口样式都要跟着回退，
  // 否则画布上已经恢复的节点在右侧表格里还是「消失」状态（工具栏按钮与 Ctrl+Z 同一路径）
  gEventBus.on('commandStack.changed', () => {
    refreshGraphicData();
    applyWmReturnRoutes(graphicModeler);
    applyWmCanvasMarkers(graphicModeler);
  });
  // palette 迁移优先执行：无论后续 XML 加载成败，图元面板都必须落在左侧独立节点面板里，
  // 不能因为加载异常而残留在画布内
  await nextTick();
  mountPaletteToTop();
  setTimeout(mountPaletteToTop, 150);
  setupWmCanvasObserver();
  try {
    const xml = await resolveGraphicXml();
    await graphicModeler.importXML(normalizeDiagramSizes(xml));
    // 开始/结束事件放大到与处理节点协调的比例；被压扁的处理节点统一撑到 100x80；
    // 然后清空导入产生的缩放历史，避免打开就算「已修改」
    normalizeAllEventSizes();
    normalizeAllNodeSizes();
    try {
      graphicModeler.get('commandStack').clear();
    } catch (e) {}
    safeFitViewport(graphicModeler);
    // 布局稳定后二次适配：避免弹窗打开瞬间容器尺寸未定导致内容贴顶、下方大片空白
    setTimeout(() => safeFitViewport(graphicModeler), 150);
    applyWmCanvasMarkers(graphicModeler);
    applyWmReturnRoutes(graphicModeler);
    mountPaletteToTop();
    refreshGraphicData();
    loadNodeFormTemplates().then(() => migrateLegacyNodeForms());
    loadNodePrintTemplates();
  } catch (e) {
    proxy.$modal.msgError('加载流程图失败：' + (e.message || e));
  }
}

/** 把 bpmn-js 默认挂在画布左上角的 palette 移到独立节点面板（palette-host，画布左侧） */
function mountPaletteToTop() {
  try {
    const host = paletteHostRef.value;
    if (!host || !graphicCanvasRef.value) return;
    const paletteEl = graphicCanvasRef.value.querySelector('.djs-palette');
    if (paletteEl && paletteEl.parentElement !== host) host.appendChild(paletteEl);
  } catch (e) {
    /* 画布未就绪时忽略，等下次调用 */
  }
}

/**
 * 取流程图 XML：
 * 1) 优先用主设计器 viewer 的当前内容；
 * 2) viewer 尚未导入任何定义时 saveXML 会抛 "no definitions loaded"，此时回退模型自身 bpmnXml；
 * 3) 都没有图形段则生成空白图，保证画布可编辑而不是整屏报错。
 */
async function resolveGraphicXml() {
  let xml = '';
  try {
    if (bpmnModeler) {
      const res = await bpmnModeler.saveXML({ format: true });
      xml = (res && res.xml) || '';
    }
  } catch (e) {
    xml = '';
  }
  if (!xml) xml = designModel.value.bpmnXml || '';
  if (!xml || !/<bpmndi:BPMNDiagram[\s>]/.test(xml)) {
    // 关键：空白图必须由 graphicModeler（Modeler）生成，主设计器是 NavigatedViewer 没有 createDiagram
    await newBlankDiagram(graphicModeler);
    const res = await graphicModeler.saveXML({ format: true });
    xml = (res && res.xml) || '';
  }
  return xml;
}

function graphicCmd(cmd) {
  if (!graphicModeler) return;
  const stack = graphicModeler.get('commandStack');
  try {
    if (cmd === 'undo') {
      // 空栈时 undo() 会抛错，先看 canUndo，撤销到底就静默忽略
      if (typeof stack.canUndo === 'function' && !stack.canUndo()) return;
      stack.undo();
    } else {
      if (typeof stack.canRedo === 'function' && !stack.canRedo()) return;
      stack.redo();
    }
  } catch (e) {
    return;
  }
  syncGraphicZoomPct();
}
/** 当前缩放百分比（工具栏下拉展示用，随缩放/适配实时刷新） */
const graphicZoomPct = ref(100);
function syncGraphicZoomPct() {
  if (!graphicModeler) return;
  try {
    const z = graphicModeler.get('canvas').zoom();
    graphicZoomPct.value = Math.round(z * 100);
  } catch (e) {
    /* 画布未就绪时忽略 */
  }
}
function graphicZoom(delta) {
  if (!graphicModeler) return;
  const canvas = graphicModeler.get('canvas');
  canvas.zoom(Math.min(2, Math.max(0.2, Math.round((canvas.zoom() + delta) * 100) / 100)));
  syncGraphicZoomPct();
}
function graphicZoomReset() {
  if (!graphicModeler) return;
  graphicModeler.get('canvas').zoom('fit-viewport', 'auto');
  syncGraphicZoomPct();
}
/** 视图下拉命令：放大/缩小/适应画布/100% */
function onGraphicZoomCmd(cmd) {
  if (!graphicModeler) return;
  if (cmd === 'in') graphicZoom(0.1);
  else if (cmd === 'out') graphicZoom(-0.1);
  else if (cmd === 'reset') graphicZoomReset();
  else if (cmd === '100') {
    graphicModeler.get('canvas').zoom(1);
    syncGraphicZoomPct();
  }
}
/** 更多菜单：导入/导出/说明（原平铺按钮收纳，能力不减） */
function onGraphicMoreCmd(cmd) {
  if (cmd === 'import') graphicImportXml();
  else if (cmd === 'export') graphicExportXml();
  else if (cmd === 'help') openGraphicHelp();
}
function openGraphicHelp() {
  ElMessageBox.alert(
    '<div style="line-height:2;font-size:13px">' +
      '<b>画布操作</b>：从左侧节点面板拖拽节点到画布；空白处按住左键拖动可框选多个节点/连线，随后拖动任一选中节点即可整体移动；选中节点/连线后右侧显示属性。<br/>' +
      '<b>快捷键</b>：Ctrl+Z 撤销、Ctrl+Y（或 Ctrl+Shift+Z）重做、Del 删除选中、Ctrl+滚轮 缩放画布。<br/>' +
      '<b>保存</b>：点顶部「保存」同步回流程设计器并持久化；「关闭」不保存未提交的改动。<br/>' +
      '<b>导入/导出</b>：顶部工具条按钮，用于备份或迁移 BPMN XML。</div>',
    '流程图编辑 · 使用说明',
    { dangerouslyUseHTMLString: true, confirmButtonText: '知道了', customStyle: { width: '560px' } },
  );
}
/** 画布是否有选中元素（控制删除按钮可用状态） */
const graphicHasSelection = computed(() => !!(selectedElement.value && selectedElement.value.id));
function graphicDelete() {
  if (!graphicModeler) return;
  const sel = graphicModeler.get('selection').get();
  if (sel && sel.length) graphicModeler.get('modeling').removeElements(sel);
}
/**
 * 键盘快捷键：Del 删除选中元素、Ctrl+Z 撤销、Ctrl+Y / Ctrl+Shift+Z 重做（输入框聚焦时不触发）。
 *
 * 撤销为什么不靠 bpmn-js 自带的 keyboard 模块：diagram-js 的 Keyboard 只在传入
 * `config.keyboard.bindTo` 时才 bind DOM（见 diagram-js/features/keyboard/Keyboard.js#attach），
 * 本项目没传，所以内置的 Ctrl+Z 绑定是「死」的（用说明里写了 Ctrl+Z，实测按了没反应）。
 * 这里在自己的 keydown 里直接走 commandStack，与工具栏「撤销/重做」同一动作。
 */
function onGraphicKeydown(e) {
  if (!graphicOpen.value || !graphicModeler) return;
  const tag = ((e.target && e.target.tagName) || '').toUpperCase();
  if (tag === 'INPUT' || tag === 'TEXTAREA' || (e.target && e.target.isContentEditable)) return;
  const key = (e.key || '').toLowerCase();
  const cmdKey = (e.ctrlKey || e.metaKey) && !e.altKey;
  // Ctrl+Z 撤销 / Ctrl+Shift+Z 重做
  if (cmdKey && key === 'z') {
    e.preventDefault();
    graphicCmd(e.shiftKey ? 'redo' : 'undo');
    return;
  }
  // Ctrl+Y 重做
  if (cmdKey && key === 'y') {
    e.preventDefault();
    graphicCmd('redo');
    return;
  }
  if (e.key === 'Delete') {
    const sel = graphicModeler.get('selection').get();
    if (sel && sel.length) {
      e.preventDefault();
      graphicDelete();
    }
  }
}
onMounted(() => {
  window.addEventListener('keydown', onGraphicKeydown);
});
onBeforeUnmount(() => {
  window.removeEventListener('keydown', onGraphicKeydown);
});

/** 独立页面保存：同步回主画布 + 持久化到后端（与 saveDesigner 同一校验/锁定逻辑） */
async function graphicSave() {
  if (!graphicModeler || !designModel.value.modelId) return;
  try {
    if (designModel.value.locked) {
      try {
        await ElMessageBox.confirm('该流程已锁定，确认仍要保存修改？', '流程锁定提醒', { confirmButtonText: '仍要保存', cancelButtonText: '返回修改', type: 'warning', draggable: true });
      } catch (e) {
        return;
      }
    }
    // 保存前再兜一次节点尺寸（手工拖动可能又拉成扁的），保证存进模型的永远是标准尺寸
    normalizeAllNodeSizes();
    applyWmReturnRoutes(graphicModeler);
    const { xml } = await graphicModeler.saveXML({ format: true });
    // 保存前校验（独立编辑页同样拦截不支持的节点类型 / 未配办理人 / 缺结束节点）
    if (!(await confirmDiagramIssues(validateDiagram(graphicModeler)))) return;
    // 同步回设计器画布（主 modeler 重新导入同一份模型；导入后必须重贴同款配色，否则会退回黑白样式）
    await syncToDesignerCanvas(xml);
    await updateModel({
      modelId: designModel.value.modelId,
      modelName: designModel.value.modelName,
      category: designModel.value.category,
      description: designModel.value.description,
      formId: designModel.value.formId || null,
      unbind: !designModel.value.formId,
      deployMode: designModel.value.deployMode || null,
      locked: !!designModel.value.locked,
      helpDoc: designModel.value.helpDoc || null,
      orderNum: designModel.value.orderNum != null ? designModel.value.orderNum : null,
      processNoRule: JSON.stringify(designModel.value.processNoRule || defaultNoRule()),
      skipSameApprover: !!designModel.value.skipSameApprover,
      bpmnXml: xml,
    });
    proxy.$modal.msgSuccess('流程图已保存，已同步回设计器');
    getList();
  } catch (e) {
    proxy.$modal.msgError('保存失败：' + (e.message || e));
  }
}

/** 关闭返回列表（流转设置-图形编辑），未保存的改动不生效 */
function graphicClose() {
  graphicOpen.value = false;
}

/** 独立页面导入/导出 XML */
function graphicImportXml() {
  graphicFileRef.value && graphicFileRef.value.click();
}
async function onGraphicFileChange(e) {
  const file = e.target.files[0];
  if (!file || !graphicModeler) return;
  try {
    const text = await file.text();
    await graphicModeler.importXML(normalizeDiagramSizes(text));
    normalizeAllEventSizes();
    normalizeAllNodeSizes();
    graphicModeler.get('canvas').zoom('fit-viewport', 'auto');
    proxy.$modal.msgSuccess('XML 已导入，点"保存"生效');
  } catch (e) {
    proxy.$modal.msgError('XML 导入失败：' + (e.message || e));
  }
  e.target.value = '';
}
async function graphicExportXml() {
  if (!graphicModeler) return;
  try {
    const { xml } = await graphicModeler.saveXML({ format: true });
    const blob = new Blob([xml], { type: 'application/xml' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = (designModel.value.modelKey || 'process') + '.bpmn20.xml';
    link.click();
    URL.revokeObjectURL(link.href);
  } catch (e) {
    proxy.$modal.msgError('导出失败');
  }
}
/** 当前活动版本号（基本信息页签与版本标签行共用同一数据口径） */
const designActiveVersion = computed(() => {
  const act = designVersions.value.find((v) => v.activeFlag === '1');
  return act ? act.version : null;
});
const versionOpen = ref(false);
const versionEdit = ref({ version: null, remark: '', setActive: false, versionActive: false, suspended: false });
/** 历史版本内容查看弹框（只读：节点清单 + BPMN XML） */
const versionViewOpen = ref(false);
const versionViewLoading = ref(false);
const versionViewTab = ref('nodes');
const versionView = ref({ version: null, xml: '', nodes: [] });

/** 加载该模型全部已部署版本（供版本标签行展示） */
function loadDesignVersions() {
  const key = designModel.value.modelKey;
  if (!key) {
    designVersions.value = [];
    return;
  }
  // onlyActive=false：拉全该 Key 的历史版本（后端默认按 Key 聚合只留 1 条，会导致版本标签只剩一个）
  listDefinition({ modelKey: key, pageNum: 1, pageSize: 100, onlyActive: false })
    .then((res) => {
      designVersions.value = res.rows || [];
    })
    .catch(() => {
      designVersions.value = [];
    });
}

/** 点击版本标签 → 打开版本编辑弹框 */
function openVersionEditor(v) {
  designVersionSelected.value = v;
  versionEdit.value = {
    version: v.version,
    remark: v.remark || '',
    setActive: v.activeFlag === '1',
    versionActive: v.activeFlag === '1',
    suspended: !!v.suspended,
    modelId: v.modelId,
  };
  versionOpen.value = true;
}

/** 保存版本编辑：先存备注，勾选时再设为活动版本 */
function submitVersionEdit() {
  const edit = versionEdit.value;
  const modelId = edit.modelId;
  updateDefinitionRemark(modelId, edit.remark || '')
    .then(() => {
      if (edit.setActive) {
        return setActiveDefinition(modelId);
      }
      return null;
    })
    .then(() => {
      proxy.$modal.msgSuccess('版本信息已更新');
      versionOpen.value = false;
      loadDesignVersions();
    })
    .catch((e) => {
      proxy.$modal.msgError('版本信息更新失败：' + (e?.message || e || '请稍后重试'));
    });
}

/** 从 BPMN XML 解析用户任务节点（名称/ID/扩展配置摘要），供历史版本只读查看 */
function parseVersionNodes(xml) {
  if (!xml) return [];
  let doc;
  try {
    doc = new DOMParser().parseFromString(xml, 'application/xml');
  } catch (e) {
    return [];
  }
  // localName 忽略命名空间前缀：同一份 BPMN 可能写成 bpmn:userTask / bpmn2:userTask / 默认命名空间的 userTask
  return Array.from(doc.getElementsByTagName('*'))
    .filter((el) => el.localName === 'userTask')
    .map((el) => {
      const ext = Array.from(el.children).find((n) => n.localName === 'extensionElements');
      let config = '';
      if (ext) {
        config = Array.from(ext.getElementsByTagName('*'))
          .filter((n) => n.children.length === 0 && (n.textContent || '').trim())
          .map((n) => (n.getAttribute('name') ? n.getAttribute('name') + '=' : '') + (n.textContent || '').trim())
          .join('; ');
      }
      return {
        name: el.getAttribute('name') || el.getAttribute('id') || '未命名节点',
        id: el.getAttribute('id') || '',
        config: config || '—',
      };
    });
}

/** 查看历史版本内容（节点清单 + XML） */
function openVersionView() {
  const v = designVersionSelected.value;
  if (!v) return;
  versionViewLoading.value = true;
  versionView.value = { version: v.version, xml: '', nodes: [] };
  versionViewTab.value = 'nodes';
  versionViewOpen.value = true;
  getDefinitionXml(v.modelId)
    .then((res) => {
      // 部署产物里带「重新提交」技术节点（部署时自动注入），只读查看时剥离，避免当成业务节点
      const xml = stripInitiatorArtifacts(res.data || '');
      versionView.value = { version: v.version, xml, nodes: parseVersionNodes(xml) };
    })
    .catch((e) => proxy.$modal.msgError('读取版本内容失败：' + (e?.message || e)))
    .finally(() => {
      versionViewLoading.value = false;
    });
}

/** 把历史版本 XML 载入当前画布（不自动保存，用户确认后手动「保存」） */
function loadVersionIntoDesigner() {
  const v = designVersionSelected.value;
  if (!v) return;
  proxy.$modal
    .confirm('将把 V' + v.version + ' 的流程图载入当前设计器，当前画布内容会被替换且不会自动保存，确认继续？')
    .then(() => getDefinitionXml(v.modelId))
    .then(async (res) => {
      // 版本 XML 是部署产物，含部署时注入的「重新提交」技术节点：载入前剥离，
      // 否则画布多出虚线框，点「保存 / 存为新版」还会把技术节点写回模型草稿
      const xml = stripInitiatorArtifacts((res.data || '').trim());
      if (!xml) throw new Error('该版本没有可用的 BPMN XML');
      if (!/<bpmndi:BPMNDiagram[\s>]/.test(xml)) throw new Error('该版本 XML 缺少图形段（DI），无法载入画布');
      versionOpen.value = false;
      designTab.value = 'flow';
      await nextTick();
      // 首次打开设计器时画布实例可能还没创建（页签懒渲染），这里补一次初始化，
      // 否则 importXML 无处可导，用户会看到一个「提示成功但画布没变」的假成功
      if (!bpmnModeler) {
        await onDesignerOpened();
      }
      if (!bpmnModeler) throw new Error('设计器画布未就绪，请关闭设计器后重新打开再试');
      await syncToDesignerCanvas(normalizeDiagramSizes(xml));
      // 画布即权威：回读一次让节点数/空状态等派生信息跟手刷新
      try {
        const { xml: saved } = await bpmnModeler.saveXML({ format: true });
        if (saved) designModel.value.bpmnXml = saved;
      } catch (e) {}
      proxy.$modal.msgSuccess('已载入 V' + v.version + ' 到设计器，确认无误后点右上「保存」');
    })
    .catch((e) => {
      if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('载入失败：' + (e?.message || e));
    });
}

/** 版本挂起/激活（由流程定义页收敛而来，入口统一收在版本编辑弹框） */
const versionStateLoading = ref(false);
function handleVersionSuspend(suspend) {
  const edit = versionEdit.value;
  const label = suspend ? '挂起' : '激活';
  let tip = `确认${label}版本 V${edit.version}？`;
  if (suspend) {
    tip = `确认挂起版本 V${edit.version}？挂起后该版本不可发起新流程，运行中实例不受影响`;
    if (edit.versionActive) {
      tip = `版本 V${edit.version} 是当前活动版本，挂起后新发起将无法使用该版本（需先激活其他版本或本版本）。确认挂起？`;
    }
  }
  proxy.$modal
    .confirm(tip)
    .then(() => {
      versionStateLoading.value = true;
      return changeDefinitionState(edit.modelId, suspend);
    })
    .then(() => {
      versionStateLoading.value = false;
      proxy.$modal.msgSuccess(`已${label}版本 V${edit.version}`);
      edit.suspended = suspend;
      loadDesignVersions();
      getList();
    })
    .catch(() => {
      versionStateLoading.value = false;
    });
}

// ===== 四页签：基本信息 / 表单管理 / 流转设置 / 高级设置 =====
const designTab = ref('flow');
/** 流转设置是否为空（模型尚无流程图）：为空时画布中央显示引导空状态，点按钮直接进入绘制；
 已有流程图时恢复为右上角「编 辑」按钮，保持原有位置。
 注意：仅有一张空的 BPMNDiagram（没有任何流程节点）也视为“还没有流程图”，
 否则会出现一片空画布，既没有引导提示、又容易被误认为已设计。 */
const flowEmpty = computed(() => {
  const xml = designModel.value && designModel.value.bpmnXml ? designModel.value.bpmnXml : '';
  if (!/<bpmndi:BPMNDiagram[\s>]/.test(xml)) return true;
  // 兼容带前缀（bpmn:/bpmn2:）与默认命名空间（不带前缀）两种 XML 写法
  const nodeRe =
    /<(?:bpmn2?:)?(startEvent|endEvent|userTask|task|serviceTask|sendTask|receiveTask|manualTask|scriptTask|businessRuleTask|callActivity|subProcess|exclusiveGateway|parallelGateway|inclusiveGateway|eventBasedGateway|complexGateway|intermediateCatchEvent|intermediateThrowEvent|boundaryEvent)[\s>/]/i;
  return !nodeRe.test(xml);
});
/** 流转设置工具条：流程节点数（与 flowEmpty 同一套节点正则，只把 test 换成全局统计） */
const flowNodeCount = computed(() => {
  const xml = designModel.value && designModel.value.bpmnXml ? designModel.value.bpmnXml : '';
  const nodeRe =
    /<(?:bpmn2?:)?(?:startEvent|endEvent|userTask|task|serviceTask|sendTask|receiveTask|manualTask|scriptTask|businessRuleTask|callActivity|subProcess|exclusiveGateway|parallelGateway|inclusiveGateway|eventBasedGateway|complexGateway|intermediateCatchEvent|intermediateThrowEvent|boundaryEvent)[\s>/]/gi;
  return (xml.match(nodeRe) || []).length;
});
/** 流转设置画布：滚轮缩放/拖拽平移之后一键回到「适应画布」 */
function fitFlowCanvas() {
  if (!bpmnModeler) return;
  try {
    bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
    applyWmCanvasMarkers(bpmnModeler);
  } catch (e) {}
}
/** 表单管理页签：当前关联表单详情（来自表单选项，兜底用模型详情字段） */
const boundFormInfo = computed(() => {
  const fid = designModel.value.formId;
  if (!fid) return {};
  const f = formOptions.value.find((x) => String(x.formId) === String(fid));
  if (f) {
    let fieldCount = null;
    try {
      const arr = JSON.parse(f.fieldsJson || 'null');
      fieldCount = Array.isArray(arr) ? arr.length : null;
    } catch (e) {}
    return { formName: f.formName, tableName: f.tableName, formDesc: f.formDesc, fieldCount };
  }
  return { formName: designModel.value.formName, tableName: designModel.value.formTableName, formDesc: null, fieldCount: designModel.value.formFieldCount };
});
/** 切回流转设置页签时重设画布视口（隐藏期间渲染尺寸会失效），并重贴同款配色 */
function onDesignTabChange(tab) {
  if (tab === 'flow' && bpmnModeler) {
    proxy.$nextTick(() => {
      try {
        bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
      } catch (e) {}
      try {
        applyWmCanvasMarkers(bpmnModeler);
      } catch (e) {}
    });
  }
}

/** 保存设计：返回是否保存成功（silent=true 时静默保存，供「存为新版」前自动落盘用） */
async function saveDesigner(silent = false) {
  try {
    // 流程锁定：锁定后保存需二次确认，防止误改已上线流程
    if (designModel.value.locked) {
      try {
        await ElMessageBox.confirm('该流程已锁定，确认仍要保存修改？', '流程锁定提醒', { confirmButtonText: '仍要保存', cancelButtonText: '返回修改', type: 'warning', draggable: true });
      } catch (e) {
        return false;
      }
    }
    // 「表单管理 → 编辑字段」里改了字段却没点「保存字段设计」：顶部保存顺带提交，避免字段改动丢失
    if (formFieldsRef.value && formFieldsRef.value.isDirty) {
      const flushed = await formFieldsRef.value.flush();
      if (!flushed.ok) return false;
    }
    const { xml } = await bpmnModeler.saveXML({ format: true });
    // 保存前校验
    if (!(await confirmDiagramIssues(validateDiagram()))) return false;
    await updateModel({
      modelId: designModel.value.modelId,
      modelName: designModel.value.modelName,
      category: designModel.value.category,
      description: designModel.value.description,
      formId: designModel.value.formId || null,
      unbind: !designModel.value.formId,
      deployMode: designModel.value.deployMode || null,
      locked: !!designModel.value.locked,
      helpDoc: designModel.value.helpDoc || null,
      orderNum: designModel.value.orderNum != null ? designModel.value.orderNum : null,
      processNoRule: JSON.stringify(designModel.value.processNoRule || defaultNoRule()),
      skipSameApprover: !!designModel.value.skipSameApprover,
      bpmnXml: xml,
    });
    if (!silent) proxy.$modal.msgSuccess('流程设计已保存，可点击"部署"发布流程');
    getList();
    return true;
  } catch (e) {
    proxy.$modal.msgError('保存失败：' + (e.message || e));
    return false;
  }
}

/** 设计器内「存为新版」：先保存设计（部署读的是已落库的 XML），再打开部署弹窗并固定在「存为新版本」 */
async function deployFromDesigner() {
  if (!(await saveDesigner(true))) return;
  await handleDeploy({ modelId: designModel.value.modelId, modelName: designModel.value.modelName, deployMode: 'new' });
  // 结构对比可能把预选改成「覆盖当前版本」，本入口明确是存为新版，这里纠正回来
  deployMode.value = 'new';
}

async function exportXml() {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true });
    const blob = new Blob([xml], { type: 'application/xml' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = (designModel.value.modelKey || 'process') + '.bpmn20.xml';
    link.click();
    URL.revokeObjectURL(link.href);
  } catch (e) {
    proxy.$modal.msgError('导出失败');
  }
}

function importLocalXml() {
  xmlFileRef.value.click();
}

async function onXmlFileChange(e) {
  const file = e.target.files[0];
  if (!file) return;
  const text = await file.text();
  try {
    // 导入后重贴同款配色（importXML 会清掉 wm-* 标记，否则画布变黑白）
    await syncToDesignerCanvas(normalizeTaskShapeHeights(text));
    proxy.$modal.msgSuccess('XML导入成功，记得点击"保存"');
  } catch (err) {
    proxy.$modal.msgError('XML解析失败：' + err.message);
  }
  e.target.value = '';
}

// ===== 查看流程图入口已移除（与设计弹窗图形编辑只读视图重复），只读查看走 设计→流转设置→图形编辑 =====

// ===== 部署方式弹窗（存为新版 / 覆盖当前版本） =====
const deployOpen = ref(false);
const deployRow = ref(null);
const deployMode = ref('new');
const deployRemark = ref('');
const deploySetActive = ref(true);
/** 部署前结构对比结果（结构未变则无需存为新版本） */
const deployCheck = ref(null);

async function handleDeploy(row) {
  deployRow.value = row;
  // 预选高级设置中的默认部署方式
  deployMode.value = row.deployMode === 'replace' ? 'replace' : 'new';
  deployRemark.value = '';
  deploySetActive.value = true;
  deployCheck.value = null;
  deployOpen.value = true;
  try {
    const res = await deployCheckModel(row.modelId);
    deployCheck.value = res.data || null;
    const changed = deployCheck.value && (deployCheck.value.structureChanged || deployCheck.value.formChanged);
    if (deployCheck.value && deployCheck.value.deployed) {
      // 结构未变：表单类修改已实时生效，默认覆盖当前版本；结构变更：存为新版本
      deployMode.value = changed ? 'new' : 'replace';
    } else {
      deployMode.value = 'new';
    }
  } catch (e) {
    deployCheck.value = null;
  }
}

function submitDeploy() {
  const row = deployRow.value;
  if (!row) return;
  const tip = deployMode.value === 'replace' ? '覆盖当前版本：部署后自动停用旧版本，运行中的流程继续走完，新发起使用新版本' : '存为新版本：新旧版本并存，新发起优先使用活动版本';
  deployModel(row.modelId, deployMode.value, deployRemark.value, deploySetActive.value)
    .then(() => {
      proxy.$modal.msgSuccess('部署成功：' + tip);
      deployOpen.value = false;
      getList();
      // 从设计器内发起部署：同步"当前状态/版本标签行"，无需关掉设计器再重进
      if (designOpen.value) {
        designModel.value.deployed = '1';
        loadDesignVersions();
      }
    })
    .catch((e) => {
      proxy.$modal.msgError('部署失败：' + (e?.message || e || '请检查流程图是否已保存'));
    });
}

function handleDelete(row) {
  if (row.systemFlow) {
    const safeName = String(row.modelName ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
    ElMessageBox.alert(
      `<div style="line-height:1.9;font-size:13px">` +
        `流程「<b>${safeName}</b>」属于<b>系统流程</b>，已与考勤、车辆、印章等业务模块联动，相关流程模型与表单<b>不允许删除</b>。<br>` +
        `如需调整，请修改表单字段（支持新增、修改字段）或调整流程模型；不再启用时可挂起对应流程版本。</div>`,
      '系统流程不可删除',
      { dangerouslyUseHTMLString: true, type: 'warning', confirmButtonText: '我知道了' },
    ).catch(() => {});
    return;
  }
  const used = Number(row.instanceCount || 0);
  if (used > 0) {
    // 前端前置拦截：后端也会校验，这里先给出可读的原因与替代方案，避免用户白点一次
    const safeName = String(row.modelName ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
    ElMessageBox.alert(
      `<div style="line-height:1.9;font-size:13px">` +
        `流程「<b>${safeName}</b>」已产生 <b style="color:#dc2626">${used}</b> 个流程实例（含已办结），相关单据与审批记录仍在使用中，因此<b>不允许删除</b>。<br>` +
        `如果只是不再启用该流程，请在该模型的<b>版本管理</b>中把对应版本<b>挂起</b>：挂起后无法再发起新流程，已产生的数据全部保留。</div>`,
      '该流程已有业务数据，禁止删除',
      { dangerouslyUseHTMLString: true, type: 'warning', confirmButtonText: '我知道了' },
    ).catch(() => {});
    return;
  }
  proxy.$modal
    .confirm('确认删除模型"' + row.modelName + '"？')
    .then(() => delModel(row.modelId))
    .then(() => {
      proxy.$modal.msgSuccess('删除成功');
      getList();
    })
    .catch((e) => {
      // 后端业务拦截（如已产生实例）已由请求拦截器统一提示，这里只留日志，避免重复弹窗
      if (e !== 'cancel' && e !== 'close' && e?.message) console.warn('删除模型失败：', e.message);
    });
}

getList();
onActivated(() => {
  getList();
});
// 出口编辑模式下初始化表格行拖拽（graphicTab 定义之后注册，避免 TDZ）
watch([graphicTab, graphicMode, filteredGraphicExits], () => {
  if (graphicMode.value === 'exits') nextTick(initExitDragSort);
});
</script>

<style lang="scss" scoped>
@use './flow-model.scss';

.field-perms-tip {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 10px;
  line-height: 1.5;
}
.deploy-check {
  width: 100%;
}
/* 部署状态列：部署标识 + 「已发起 N 单」（有数据则禁止删除模型） */
.deploy-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  line-height: 1;
}
.used-tag {
  display: inline-block;
  padding: 0 6px;
  font-size: 11px;
  line-height: 16px;
  color: #b45309;
  background: #fef3c7;
  border: 1px solid #fde68a;
  border-radius: 8px;
  white-space: nowrap;
  cursor: help;
}
.deploy-change-item {
  font-size: 12px;
  line-height: 1.7;
  color: #92400e;
}
.field-perms-copybar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.field-perms-copybar .fpc-label {
  font-size: 12px;
  color: #475569;
  flex: none;
}
.field-perms-table {
  width: 100%;
}
.field-perms-table .fp-field {
  font-style: normal;
  color: #94a3b8;
  font-size: 11px;
  margin-left: 6px;
}
.field-perms-empty {
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  padding: 20px 0;
}

/* ===== 节点参与者（一期：扁平「关系对象」+ 统一选择器 + 已选列表） ===== */
.pc-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  width: 100%;
  min-height: 24px;
}
.pc-empty {
  font-size: 12px;
  color: #94a3b8;
}
.pc-add {
  height: 24px;
  padding: 0 8px;
}
.pc-pick-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  flex-wrap: wrap;
}
.pc-picked {
  font-size: 12px;
  color: #334155;
}
.pc-form {
  padding-top: 2px;
}
.pc-section {
  border: 1px solid #e8eef7;
  border-radius: 10px;
  background: #fbfcfe;
  padding: 12px 14px 2px;
  margin-bottom: 12px;
}
.pc-section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.pc-section-title {
  position: relative;
  padding-left: 9px;
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  line-height: 18px;
}
.pc-section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 2px;
  bottom: 2px;
  width: 3px;
  border-radius: 2px;
  background: #2563eb;
}
.pc-section-desc {
  font-size: 12px;
  color: #94a3b8;
}
.pc-section-count {
  padding: 0 8px;
  border-radius: 10px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  line-height: 18px;
}
.pc-section .el-form-item {
  margin-bottom: 12px;
}
.pc-selected-body {
  width: 100%;
  padding-bottom: 12px;
}
.pc-mode-tip {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  width: 100%;
  margin-top: 6px;
  font-size: 12px;
  line-height: 18px;
  color: #64748b;
}
.pc-mode-badge {
  flex: none;
  padding: 0 6px;
  border-radius: 4px;
  background: #eff6ff;
  color: #2563eb;
  line-height: 18px;
}
.pc-mode-compare {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  width: 100%;
  margin-top: 4px;
  padding-top: 6px;
  border-top: 1px dashed #e2e8f0;
  font-size: 12px;
  color: #94a3b8;
}
.pc-mode-compare b {
  color: #64748b;
  font-weight: 600;
}
.pc-rel-groups {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}
.pc-rel-tabs {
  align-self: flex-start;
}
.pc-rel-tabs :deep(.el-radio-button__inner) {
  padding: 6px 18px;
  font-size: 13px;
}
.pc-rel-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  background: #fff;
  box-sizing: border-box;
}
.pc-rel-group-hint {
  font-size: 12px;
  color: #94a3b8;
  line-height: 18px;
}
.pc-rel-radios {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.pc-rel-radios :deep(.el-radio) {
  margin-right: 0;
  height: 26px;
}
.pc-rel-radios :deep(.el-radio.is-bordered) {
  padding: 0 10px;
  height: 26px;
  border-radius: 13px;
}
.pc-rel-radios :deep(.el-radio.is-bordered .el-radio__label) {
  padding-left: 5px;
  font-size: 12px;
}
.pc-rel-radios :deep(.el-radio.is-bordered.is-checked) {
  background: #eff6ff;
  border-color: #2563eb;
}
.pc-all-users {
  padding: 1px 10px;
  border-radius: 10px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
}
.pc-items-table {
  width: 100%;
}

/* ===== 右侧节点信息行（蓝色链接值 + 设置齿轮） ===== */
.props-link-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  width: 100%;
  min-height: 24px;
}
.props-static {
  color: #334155;
  font-size: 13px;
}
.props-link-row .props-link {
  padding: 0;
  max-width: calc(100% - 26px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.props-link-row .props-link-gear {
  flex: none;
  padding: 0 2px;
  color: #64748b;
}
.props-link-row .props-link-gear:hover {
  color: #2563eb;
}
/* 节点表单弹窗 */
.node-form-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
.node-form-pick :deep(.el-input__inner) {
  cursor: pointer;
}
.node-form-rename {
  cursor: pointer;
  color: var(--el-color-primary);
  font-size: 15px;
}
.node-form-reset {
  margin-top: 4px;
  padding: 0;
}
.node-form-pick {
  display: flex;
  flex-direction: column;
}
.node-form-pick-search {
  margin-bottom: 10px;
}
.node-form-ref-tip {
  margin-top: 2px;
  padding: 4px 8px;
  border-radius: 4px;
  background: #fef9ec;
  border: 1px solid #fde68a;
  color: #a16207;
  font-size: 12px;
  line-height: 1.6;
}
.node-form-design-wrap {
  max-height: 62vh;
  overflow-y: auto;
}
/* 全屏：设计器铺满弹窗，避免底部大片留白 */
.node-form-design-wrap.is-fullscreen {
  max-height: none;
  height: auto;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
/* 只作用于外层「页面设计」：内部右侧面板也有 el-tabs，不能一并压缩，否则字段列表会被压成 0 高 */
.node-form-design-wrap.is-fullscreen :deep(.wm-designer) {
  min-height: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.node-form-design-wrap.is-fullscreen :deep(.wm-designer.wm-full) {
  min-height: 0 !important;
  height: auto;
  flex: 1;
}
.node-form-design-wrap.is-fullscreen :deep(.wm-designer.wm-full .wm-canvas-wrap) {
  max-height: none;
}
.node-form-design-wrap.is-fullscreen :deep(.wm-designer.wm-full .wm-pp) {
  max-height: none !important;
}
/* 右侧字段面板顶部：节点模板名称 + 表单编辑入口（画布让出空间） */
.ndh-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.ndh-title {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.2;
}
.ndh-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.ndh-actions :deep(.el-button) {
  margin-left: 0;
}
.ndh-tip {
  font-size: 11px;
  color: #b45309;
  line-height: 1.5;
}
/* 顶部右侧：关闭/保存（给右上原生 X 让出空间，避免 3px 级重叠） */
.node-design-head-btns { margin-left: auto; margin-right: 40px; display: flex; align-items: center; gap: 8px; flex: none; }
.node-field-edit-wrap {
  max-height: 66vh;
  overflow-y: auto;
}
.node-field-edit-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
}
.node-field-edit-footer .field-tip {
  font-size: 12px;
  color: #64748b;
}
.node-field-edit-footer .el-button {
  margin-left: auto;
}
/* 操作菜单弹窗（对标泛微：按钮表格 + 签字意见设置） */
.op-menu-table {
  width: 100%;
}
.op-menu-table :deep(.el-input__inner) {
  text-align: left;
}
.op-menu-na {
  color: var(--el-text-color-placeholder);
}
.op-menu-sign {
  margin-top: 12px;
  padding: 12px 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
}
.op-menu-sign-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}
.op-menu-sign-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.op-menu-sign-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.op-menu-sign-row {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
}
.op-menu-sign-row :deep(.el-checkbox) {
  margin-right: 0;
}

/* 四页签：基本信息 / 表单管理 / 流转设置 / 高级设置 */
.designer-tabs {
  padding: 0 16px 16px;
}
.designer-tabs :deep(.el-tab-pane) {
  padding-top: 14px;
}
.design-basic-form {
  max-width: 660px;
}
/* 高级设置页的流程编号摘要行：状态 + 段结构 + 配置入口，详细配置收在弹框里 */
.no-rule-row {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafbfe;
}
.no-rule-row .nr-main {
  flex: 1;
  min-width: 0;
}
.no-rule-row .nr-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.no-rule-row .nr-segs {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  line-height: 1.5;
  color: #1d4ed8;
  word-break: break-all;
}
.no-rule-row > .el-button {
  flex: none;
}
.no-rule-row .nr-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}
/* 弹框内解除设计页 660px 的窄栏限制，段编辑器才排得开 */
.nr-dialog-form.design-basic-form {
  max-width: none;
}
.nr-dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.nr-dialog-footer .nr-footer-tip {
  font-size: 12px;
  color: #94a3b8;
  text-align: left;
}
.nr-dialog-footer .nr-footer-btns {
  display: flex;
  gap: 8px;
  flex: none;
}

/* 流程编号示例预览：等宽字体，一眼看出拼出来的单号 */
.no-preview {
  display: inline-block;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
  color: #1d4ed8;
  background: #eff6ff;
  border: 1px dashed #bfdbfe;
  border-radius: 6px;
  padding: 2px 10px;
}

/* 编号段编排：一行一段，段类型 + 参数 + 上下移/删除 */
.no-seg-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafbfe;
}
.no-seg-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.no-seg-row .seg-lab {
  font-size: 12px;
  color: #94a3b8;
}
.no-seg-row .seg-ops {
  display: inline-flex;
  align-items: center;
  margin-left: auto;
}
.no-seg-row .seg-ops .el-button + .el-button {
  margin-left: 2px;
}
/* 选择框字段的「代字」配置：整行占满，排在操作按钮下面 */
.no-seg-row .no-code-box {
  order: 9;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding-left: 114px;
}
.no-code-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 16px;
}
.no-code-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.no-code-label {
  font-size: 12px;
  color: #606266;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/* 编号预览卡：大号等宽单号 + 说明（跟在段编辑下面，改一段就能立刻看到） */
.no-preview-card {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
  padding: 10px 14px;
  border: 1px dashed #bfdbfe;
  border-radius: 8px;
  background: #f8fbff;
}
.no-preview-card .np-lab {
  font-size: 12px;
  color: #64748b;
  flex: none;
}
.no-preview-card .np-value {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #1d4ed8;
}
.no-preview-card .np-tip {
  font-size: 12px;
  color: #94a3b8;
}

/* 版本标签行：V1/V2/... 点击弹版本编辑 */
.version-tab-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px 0;
  flex-wrap: wrap;
}
.version-row-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  flex: none;
}
.version-tab {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 14px;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s;
}
.version-tab:hover {
  border-color: #3b82f6;
  color: #1d4ed8;
}
.version-tab.is-active {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #1d4ed8;
  font-weight: 600;
}
.version-tab .vt-badge,
.vt-badge {
  background: #10b981;
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
}
.version-row-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 4px;
}
/* 版本多了折叠入口：与标签同视觉，但用虚线区分「不是某个版本」 */
.version-more {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border: 1px dashed #94a3b8;
  border-radius: 14px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s;
}
.version-more:hover {
  border-color: #3b82f6;
  color: #1d4ed8;
}
.version-all-btn {
  margin-left: 2px;
}

/* 表单管理二级页签：基本信息/编辑字段 */
.form-sub-tabs {
  padding: 0 8px;
}
.form-sub-tabs :deep(.el-tab-pane) {
  padding-top: 10px;
}

/* 图形编辑独立工具栏（按钮放画布上方） */

/* 图形化编辑弹窗：三个二级页签（图形编辑/节点信息/出口信息） */
.graphic-body {
  flex-direction: column;
}
/* ===== 图形编辑全屏弹窗高度链（浏览器实测定位，非推断） =====
 实测环境 1280x720，打开该弹窗后逐层量取 getBoundingClientRect：
 .el-dialog__body 高 234px、display:block、overflow:visible
 .graphic-body 高 234px（被内容撑开，不是撑满）
 .graphic-editor-tabs 高 234px
 .el-tabs__content 高 194px、display:block
 .graphic-pane 高 194px
 .graphic-main 高 153px
 .designer-canvas 高 580px ← 画布自身高度远大于其容器
 .djs-container 高 580px（网格底图在这里）
 两个致命点：
 1) .el-dialog__body 没有确定高度，graphic-body 只能被内容撑到 234px；
 而画布自身是 580px，于是溢出到容器之外（bottom 854 > 视口 720），
 被 .el-overlay 的 overflow:auto 裁掉 —— 视觉上就是"下半屏空白把流程图压住"。
 2) el-dialog 通过 teleport 挂到 body 下，.el-dialog__body 上不带本组件作用域属性，
 所以之前写的 :deep(.designer-dialog .el-dialog__body) 编译成
 [data-v-xxx] .designer-dialog .el-dialog__body 后实际一条都没命中（已用
 body.matches() 逐一验证为 false），改样式自然不生效。
 因此这里用全局（非 scoped）规则给出两级确定高度，并把 el-tabs__content 补成 flex 容器，
 让 pane 真正吃满，画布 100% 铺开、网格覆盖到底部。
 注意：这些规则必须写在 scoped 样式之外（见文件底部 <style lang="scss"> 全局块），
 写在 scoped 块里会被编译成 .el-dialog__body[data-v-xxx]，而该节点由 teleport 生成、
 不带作用域属性（实测 bodyAttrs = "id,class"），规则永远匹配不上。 */
/* ：画布 + 右侧弹出节点信息面板（可收缩手柄 ‹/›） */
.graphic-main {
  flex: 1;
  display: flex;
  min-height: 0;
  position: relative;
}
.graphic-main .designer-canvas {
  flex: 1;
  min-width: 0;
}
/* 左侧节点面板收起/展开：画布左边缘悬浮胶囊按钮（左右箭头 + 文案，收起后提示展开） */
.palette-toggle {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 20;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  height: 30px;
  padding: 0 10px 0 7px;
  border: 1px solid #cfe0f5;
  border-radius: 15px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.14);
  color: #3b82f6;
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.18s cubic-bezier(0.16, 0.84, 0.44, 1);
  .el-icon {
    font-size: 14px;
    font-weight: 700;
  }
}
.palette-toggle:hover {
  color: #1d4ed8;
  border-color: #93c5fd;
  background: #eff6ff;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.22);
  transform: translateY(-50%) scale(1.04);
}
.palette-toggle:active {
  transform: translateY(-50%) scale(0.98);
}
/* 收起状态：按钮贴住画布左边缘，突出「展开」 */
.palette-toggle.is-collapsed {
  left: 12px;
  color: #fff;
  background: #3b82f6;
  border-color: #3b82f6;
  box-shadow: 0 3px 12px rgba(37, 99, 235, 0.35);
}
.palette-toggle.is-collapsed:hover {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
}
.props-handle {
  width: 16px;
  flex: none;
  background: #f1f5f9;
  border-left: 1px solid #e5e7eb;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 12px;
}
.props-handle span {
  writing-mode: vertical-lr;
  letter-spacing: 2px;
}
.props-handle:hover {
  background: #e0f2fe;
  color: #1d4ed8;
}
.graphic-main .designer-props {
  width: 320px;
  flex: none;
}
/* ===== 分支条件：选字段/运算符/值 配置区（属性面板与出口条件弹窗共用） ===== */
.cond-section {
  margin: 6px 0 12px;
  padding: 10px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #f8fbff;
}
.cond-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.cond-head-title {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}
/* 默认出口开关：网关兜底分支，不参与条件判断 */
.cond-default {
  margin-bottom: 10px;
  padding: 8px;
  border: 1px dashed #bfdbfe;
  border-radius: 6px;
  background: #fff;
}
.cond-default-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.cond-default-label {
  font-size: 13px;
  color: #1e293b;
}
.cond-row {
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}
.cond-row + .cond-row {
  margin-top: 8px;
}
.cond-line {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cond-line + .cond-line {
  margin-top: 6px;
}
.cond-when {
  flex: none;
  width: 32px;
  text-align: center;
  font-size: 12px;
  color: #64748b;
}
.cond-join {
  flex: none;
  width: 64px;
}
.cond-field {
  flex: 1;
  min-width: 0;
}
.cond-del {
  flex: none;
  padding: 0 2px;
}
.cond-op {
  flex: none;
  width: 108px;
}
.cond-value {
  flex: 1;
  min-width: 0;
}
.cond-novalue {
  font-size: 12px;
  color: #94a3b8;
}
.cond-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.cond-preview {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  margin-top: 8px;
  padding: 6px 8px;
  border: 1px dashed #bfdbfe;
  border-radius: 6px;
  background: #fff;
}
.cond-preview-label {
  flex: none;
  font-size: 12px;
  line-height: 18px;
  color: #64748b;
}
.cond-preview code {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  line-height: 18px;
  color: #2563eb;
  word-break: break-all;
}
.cond-opt-label {
  float: left;
}
.cond-opt-field {
  float: right;
  margin-left: 12px;
  font-size: 12px;
  color: #94a3b8;
}
/* 顶部模式徽标：打开编辑页后一眼看出当前在编辑哪一类 */
.mode-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  color: #bfdbfe;
  background: rgba(59, 130, 246, 0.22);
  border: 1px solid rgba(147, 197, 253, 0.45);
  letter-spacing: 0.5px;
}
/* ===== 流转设置空状态：无流程图时画布中央的引导卡片 ===== */
.flow-empty {
  position: absolute;
  inset: 0;
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 24px;
  text-align: center;
  background: radial-gradient(circle at 50% 42%, rgba(239, 246, 255, 0.96) 0%, rgba(248, 250, 252, 0.9) 45%, rgba(248, 250, 252, 0.72) 100%);
}
.flow-empty .fe-art {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.flow-empty .fe-node {
  display: inline-block;
  flex: none;
}
.flow-empty .fe-start {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 2px solid #22c55e;
  background: #dcfce7;
}
.flow-empty .fe-task {
  width: 74px;
  height: 34px;
  border-radius: 8px;
  border: 2px solid #3b82f6;
  background: #eff6ff;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.12);
}
.flow-empty .fe-end {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 2px solid #dc2626;
  background: #fee2e2;
}
.flow-empty .fe-line {
  width: 26px;
  height: 2px;
  background: #cbd5e1;
  position: relative;
}
.flow-empty .fe-line::after {
  content: '';
  position: absolute;
  right: -1px;
  top: -3px;
  border-left: 6px solid #cbd5e1;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
}
.flow-empty .fe-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}
.flow-empty .fe-desc {
  font-size: 13px;
  line-height: 1.7;
  color: #64748b;
  max-width: 420px;
}
.flow-empty .fe-steps {
  display: flex;
  gap: 18px;
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}
/* 空状态时隐藏底下的画布（保留 DOM，保证预览实例仍可初始化/后续显示） */
.flow-tabbar .designer-canvas.canvas-hidden {
  visibility: hidden;
}
.flow-empty .fe-steps b {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  margin-right: 4px;
  border-radius: 50%;
  background: #dbeafe;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
}
/* ===== 流转设置：顶部工具条 + 自适应画布 =====
 高度不再用 calc(100vh - 140px) 估算：实测（窗口 738px 高）画布底部到 777px，
 而弹窗体底部只有 746px，多出的 31px 被 overflow 裁掉。
 现在由 .designer-tabs.flow-active 参与弹窗体的纵向 flex 分配，高度精确等于剩余空间。 */
.flow-tabbar {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}
.flow-toolbar {
  flex: none;
  height: 36px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 10px 0 12px;
  background: #f8fafc;
  border: 1px solid #e8eef7;
  border-bottom: none;
  border-radius: 6px 6px 0 0;
}
.flow-toolbar .ft-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.flow-toolbar .ft-title {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
}
.flow-toolbar .ft-count {
  flex: none;
  padding: 1px 8px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #eff6ff;
  font-size: 12px;
  color: #2563eb;
  white-space: nowrap;
}
.flow-toolbar .ft-sub {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 窄窗口（分屏/小屏）优先保证标题与按钮完整，操作提示让位，避免被压成半截省略号 */
@media (max-width: 1200px) {
  .flow-toolbar .ft-sub {
    display: none;
  }
}
.flow-toolbar .ft-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.flow-toolbar .ft-right .el-button + .el-button {
  margin-left: 0;
}
.flow-tabbar > .designer-body {
  position: relative;
  flex: 1 1 auto;
  height: auto;
  min-height: 0;
  border-radius: 0 0 6px 6px;
  overflow: hidden;
}
/* 流转设置激活时，把 el-tabs 与内容区接入弹窗体的纵向 flex，画布高度精确吃满剩余空间 */
.designer-tabs.flow-active {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.designer-tabs.flow-active :deep(.el-tabs__content) {
  flex: 1 1 auto;
  min-height: 0;
}
.designer-tabs.flow-active :deep(.el-tab-pane) {
  height: 100%;
}
/* 收紧页签横线到工具条之间的距离（页签外边距 + 内容内边距合计 6px，约为原来 18px 的 1/3） */
.designer-tabs.flow-active :deep(.el-tabs__header) {
  margin-bottom: 4px;
}
.designer-tabs.flow-active :deep(.el-tab-pane) {
  padding-top: 2px;
}
/* 单一模式容器：直接撑满 graphic-body（不再有二级 el-tabs 包裹层） */
.graphic-pane,
.graphic-single-pane {
  flex: 1 1 auto;
  display: flex;
  min-height: 0;
  overflow: hidden;
}
/* 图形模式：左侧节点面板 + 右侧画布；节点/出口模式为单列 */
.graphic-pane {
  flex-direction: row;
}
.graphic-single-pane {
  flex-direction: column;
}
/* v-show 隐藏时必须彻底退出布局：否则 flex 容器里仍按原尺寸占位，画布会盖住节点/出口表格 */
.graphic-pane[style*='display: none'],
.graphic-single-pane[style*='display: none'] {
  display: none !important;
  flex: none;
}
.graphic-single-pane {
  padding: 10px 8px 8px;
  overflow-y: auto;
}

/* ===== 图形编辑工具栏（参考：浅色扁平按钮条，按组分区，主操作右置） ===== */
.graphic-toolbar {
  flex: none;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: #fff;
  border-bottom: 1px solid #e8eef7;
}
.graphic-toolbar .gt-group {
  display: flex;
  align-items: center;
  gap: 2px;
}
.graphic-toolbar .gt-group .el-button + .el-button {
  margin-left: 0;
}
.graphic-toolbar :deep(.el-button.is-link) {
  color: #475569;
  font-size: 13px;
  height: 26px;
  padding: 4px 8px;
}
.graphic-toolbar :deep(.el-button.is-link:hover) {
  color: #2563eb;
  background: #f4f8ff;
}
.graphic-toolbar :deep(.el-button.is-link.is-disabled) {
  color: #c7d2e0;
}
.graphic-toolbar .gt-divider {
  width: 1px;
  height: 16px;
  background: #e2e8f0;
  margin: 0 8px;
}
.graphic-toolbar .gt-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.graphic-toolbar .gt-right .el-button + .el-button {
  margin-left: 0;
}
.graphic-toolbar .gt-zoom {
  font-size: 12px;
  color: #94a3b8;
}

/* ===== 节点面板（参考：左侧竖向白色面板，事件/节点分组，彩色图标在上文字在下） ===== */
.palette-host {
  flex: none;
  width: 156px;
  overflow-y: auto;
  overflow-x: hidden;
  background: #fff;
  border-right: 1px solid #e8eef7;
}
.palette-host:empty {
  display: none;
}
/* 隐藏 bpmn-js 默认的 palette 容器样式，只借用其拖拽能力 */
.palette-host :deep(.djs-palette) {
  position: relative !important;
  left: 0 !important;
  top: 0 !important;
  width: 100% !important;
  border: none;
  border-radius: 0;
  box-shadow: none;
  background: transparent;
  padding: 0;
}
.palette-host :deep(.djs-palette .djs-palette-toggle) {
  display: none;
}
.palette-host :deep(.djs-palette .djs-palette-entries) {
  display: block;
}
.palette-host :deep(.djs-palette .djs-palette-entries .group) {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px 8px;
  padding: 8px 10px 10px;
}
/* 分组标题：事件（普通标题）/ 节点（灰底通栏分隔条） */
.palette-host :deep(.djs-palette .group::before) {
  grid-column: 1 / -1;
  padding: 2px 0 4px;
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}
.palette-host :deep(.djs-palette .group[data-group='event']::before) {
  content: '事件';
}
.palette-host :deep(.djs-palette .group[data-group='task']::before) {
  content: '节点';
  margin: 6px -10px 4px;
  padding: 7px 10px;
  background: #f4f5f7;
  border-top: 1px solid #e8ebf0;
  border-bottom: 1px solid #e8ebf0;
}

/* 节点按钮：无边框，彩色图标 + 文字（悬浮淡蓝底） */
.palette-host :deep(.djs-palette .entry) {
  width: auto;
  height: 58px;
  margin: 0;
  padding: 2px 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 7px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #334155;
  font-size: 13px;
  line-height: 1;
  transition: background 0.15s ease;
  cursor: pointer;
}
.palette-host :deep(.djs-palette .entry:hover) {
  background: #f5f9ff;
}
.palette-host :deep(.djs-palette .entry .wm-pal-label) {
  font-size: 13px;
  color: #334155;
  line-height: 1;
}
.palette-host :deep(.djs-palette .entry:hover .wm-pal-label) {
  color: #2563eb;
}

/* 节点图标：圆形 / 菱形 / 圆角方形底 + 白色符号（与画布节点配色一致） */
.palette-host :deep(.wm-node-icon) {
  width: 32px;
  height: 32px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.palette-host :deep(.wm-node-icon-start) {
  border-radius: 50%;
  background: #4caf50;
}
.palette-host :deep(.wm-node-icon-end) {
  border-radius: 50%;
  background: #e74c3c;
}
.palette-host :deep(.wm-node-icon-exclusive),
.palette-host :deep(.wm-node-icon-parallel),
.palette-host :deep(.wm-node-icon-inclusive) {
  background: #f0a63c;
  clip-path: polygon(50% 0%, 100% 50%, 50% 100%, 0% 50%);
}
.palette-host :deep(.wm-node-icon-task) {
  border-radius: 8px;
  background: #4a90e2;
}
/* 画布网格背景（浅灰网格线）+ 高度链补全（tab-pane/djs-container 撑满，避免下半纯白空白） */
.palette-host :deep(.djs-canvas) {
  outline: none;
}
.designer-canvas :deep(.djs-container) {
  height: 100% !important;
  width: 100% !important;
}
.designer-canvas .djs-container {
  background-color: #fff;
  background-image: linear-gradient(#f1f5f9 1px, transparent 1px), linear-gradient(90deg, #f1f5f9 1px, transparent 1px);
  background-size: 24px 24px;
}
.graphic-pane .graphic-main {
  flex: 1;
  min-height: 0;
}
/* ===== 画布节点视觉（与左侧节点面板图标保持同一套配色/造型） ===== */
/* 开始节点：绿色圆形（白色三角由 decorateWmShape 插入 SVG） */
.designer-canvas :deep(.wm-start.djs-shape .djs-visual circle),
.designer-canvas :deep(.wm-start.djs-shape .djs-visual ellipse) {
  fill: #4caf50;
  stroke: #43a047;
  stroke-width: 2;
}
.designer-canvas :deep(.wm-start.djs-shape:hover .djs-visual circle) {
  fill: #43a047;
}
/* 结束节点：红色实心 + 深色粗环（终止样式，白色方块由 decorateWmShape 插入） */
.designer-canvas :deep(.wm-end.djs-shape .djs-visual circle),
.designer-canvas :deep(.wm-end.djs-shape .djs-visual ellipse) {
  fill: #e74c3c;
  stroke: #1f2937;
  stroke-width: 4;
}
.designer-canvas :deep(.wm-end.djs-shape:hover .djs-visual circle) {
  fill: #dc2626;
}
/* 用户任务：蓝色实心圆角卡片（白色人像由 decorateWmShape 插入） */
.designer-canvas :deep(.wm-task.djs-shape .djs-visual rect) {
  fill: #4a90e2;
  stroke: #3b82f6;
  stroke-width: 1;
}
.designer-canvas :deep(.wm-task.djs-shape:hover .djs-visual rect) {
  fill: #3b82f6;
}
/* 发起人（重新提交）节点：灰白虚线卡片（正常流转不经过，只在驳回退回发起人时使用） */
.designer-canvas :deep(.wm-initiator.djs-shape .djs-visual rect) {
  fill: #f8fafc;
  stroke: #94a3b8;
  stroke-width: 2;
  stroke-dasharray: 6 4;
}
.designer-canvas :deep(.wm-initiator.djs-shape:hover .djs-visual rect) {
  fill: #eef2f7;
}
.designer-canvas :deep(.wm-initiator.djs-shape .djs-visual text) {
  fill: #475569;
}
/* 网关：橙色菱形 + 白色网关符号（互斥 ✕ / 并行 ＋ / 相容 ○） */
.designer-canvas :deep(.wm-gateway.djs-shape .djs-visual polygon) {
  fill: #f0a63c;
  stroke: #d98b1f;
  stroke-width: 2;
}
.designer-canvas :deep(.wm-gateway.djs-shape:hover .djs-visual polygon) {
  fill: #e89a2a;
}
.designer-canvas :deep(.wm-gateway.djs-shape .djs-visual path) {
  fill: #fff;
  stroke: #fff;
}
.designer-canvas :deep(.wm-gateway.djs-shape .djs-visual circle) {
  fill: none;
  stroke: #fff;
  stroke-width: 2.5;
}
/* 装饰图元不参与鼠标命中，避免影响节点拖拽/选中 */
.designer-canvas :deep(.wm-deco) {
  pointer-events: none;
}
/* 开始/结束事件的名字是 bpmn-js 的「外部标签」，是一个独立的 label 元素；
 * 用户任务的名字画在矩形内部、和节点同属一个元素（点哪都选整块卡片）。
 * 事件名被搬进圆圈后，点名字会单独选中这段文字（画面上弹出一个套在圆圈里的小方框），
 * 与「图标里的小标识是整个节点的一部分」的观感不一致。
 * 这里让事件名整体不吃鼠标事件（含内部 djs-hit），命中穿透到下层的事件图形：
 * 点名字 / 点左上角的小三角（小方块）都选中整个开始（结束）节点，与用户任务完全一致；
 * 改名不受影响 —— bpmn-js 双击图形本身就会激活它自己的 label 编辑器。 */
.designer-canvas :deep(.wm-event-label),
.designer-canvas :deep(.wm-event-label *) {
  pointer-events: none !important;
}
/* 开始/结束事件的名称已搬进圆内（utils/bpmnSizes#normalizeEventLabelPositions 改写 BPMNLabel 坐标），
 * 与用户任务同款白字粗体（颜色由 markEventLabel 写内联样式），字号略收避免贴边 */
.designer-canvas :deep(.wm-event-label text.djs-label) {
  font-size: 12px !important;
}
/* 出口拖动排序手柄（十字箭头，cursor:move） */
.drag-handle {
  display: inline-block;
  cursor: move;
  color: #94a3b8;
  font-size: 13px;
  letter-spacing: -2px;
  margin-right: 6px;
  user-select: none;
}
.drag-handle:hover {
  color: #2563eb;
}
/* 退回/驳回出口：红色虚线 + 红色箭头 */
.designer-canvas :deep(.wm-return.djs-connection .djs-visual > path) {
  stroke: #dc2626 !important;
  stroke-width: 2 !important;
  stroke-dasharray: 6, 4 !important;
  marker-end: url('#sequenceflow-end-white-black-red') !important;
}
.designer-canvas :deep(.wm-return.djs-connection .djs-visual > path:nth-child(2)) {
  fill: #dc2626 !important;
  stroke: #dc2626 !important;
}
</style>

<!-- ===== 图形编辑全屏弹窗高度链（必须放在非 scoped 全局块） =====
 为什么不能写在 scoped 块里：本弹窗的 DOM 由 el-dialog 通过 teleport 挂到 body 下，
 .el-dialog__body 自身不带本组件作用域属性（浏览器实测其 attrs 只有 id/class），
 若写在 scoped 块里，:deep() 会被编译成 .el-dialog__body[data-v-xxx]，
 该选择器永远匹配不上，样式静默失效 —— 这正是之前"改了没效果"的直接原因。
 高度链实测（1280x720，修复前）：
 .el-dialog__body 234px / display:block → .graphic-body 234px → .el-tabs__content 194px
 → .graphic-pane 194px → .graphic-main 153px，而 .designer-canvas 自身高 580px，
 画布溢出容器 134px 后被 .el-overlay 的 overflow:auto 裁掉，
 视觉上即"下半屏被一块空白压住、网格只覆盖上半段"。
 修复思路：让 .el-dialog__body 成为纵向 flex 容器并撑满，逐级把
 .graphic-body → .el-tabs__content → .el-tab-pane → .graphic-main → .designer-canvas
 打通为可计算的弹性高度，使 djs-container 的网格底图铺满整个剩余区域。 -->
<style lang="scss">
/* ===== 节点表单设计弹窗：全屏时用弹性布局铺满，删除底部留白 ===== */
.node-design-dialog.is-fullscreen {
  display: flex;
  flex-direction: column;
}
.node-design-dialog.is-fullscreen .el-dialog__body {
  flex: 1;
  min-height: 0;
  padding: 8px 12px 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
/* ===== 设计器头部（必须放全局块） =====
 1) 弹窗由 el-dialog 通过 teleport 挂到 body 下，DOM 不带本组件作用域属性；
 写进 scoped 块（含 @use 的 flow-model.scss）会被编译成 [data-v-xxx]，永远匹配不上。
 2) 底色统一采用《统一开发规范》弹窗头部标准蓝色渐变
 linear-gradient(135deg,#1e3a8a 0%,#2563eb 60%,#3b82f6 100%)，与 .rd-dialog 及其他业务弹窗一致。 */
.designer-dialog .el-dialog__header {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%) !important;
  background-color: #2563eb !important;
  padding: 10px 20px;
  margin-right: 0;
  border-bottom: none;
  border-radius: 6px 6px 0 0;
}
.designer-dialog .el-dialog__header .designer-title {
  color: #fff;
}
.designer-dialog .el-dialog__header .designer-title .designer-model-name {
  color: #f1f5f9;
}
.designer-dialog .el-dialog__headerbtn .el-dialog__close {
  color: #fff;
}

.designer-dialog .el-dialog__body {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100vh - 60px);
}
.designer-dialog .el-dialog__body > .designer-body {
  flex: 1 1 auto;
  min-height: 0;
  height: auto;
}
.designer-dialog .graphic-body > .graphic-pane,
.designer-dialog .graphic-body > .graphic-single-pane {
  flex: 1 1 auto;
  min-height: 0;
}
/* 画布自身铺满其容器，网格底图随之延伸到底部 */
.designer-dialog .graphic-main .designer-canvas {
  height: 100%;
  min-height: 0;
}
/* 历史版本只读 XML 预览 */
.version-xml {
  margin: 0;
  max-height: 420px;
  overflow: auto;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--ink-900, #0f172a);
  color: #cbd5e1;
  font-family: ui-monospace, Menlo, monospace;
  font-size: 12px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
