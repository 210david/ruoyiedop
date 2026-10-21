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
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>模型Key</label>
          <div class="control">
            <el-input v-model="queryParams.modelKey" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Document /></el-icon></template>
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
              <template #default="scope"><span class="col-strong">{{ scope.row.modelName }}</span></template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'modelKey'" label="模型Key" prop="modelKey" key="modelKey" :min-width="colWidth('modelKey', 140)" resizable show-overflow-tooltip>
              <template #default="scope"><span class="col-mono">{{ scope.row.modelKey }}</span></template>
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
            <el-table-column v-else-if="colKey === 'deployed'" label="部署状态" prop="deployed" key="deployed" :width="colWidth('deployed', 100)" resizable align="center">
              <template #default="scope">
                <span class="badge" :class="scope.row.deployed === '1' ? 'green' : 'gray'">
                  <span class="dot"></span>{{ scope.row.deployed === '1' ? '已部署' : '未部署' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column v-else-if="colKey === 'createTime'" label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 170)" resizable align="center" />
          </template>
          <el-table-column label="操作" key="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['flowable:model:edit']">设计</el-button>
                <el-button link type="success" icon="Promotion" @click="handleDeploy(scope.row)" v-hasPermi="['flowable:model:deploy']">部署</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:model:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- ===== 新增模型弹窗 ===== -->
    <el-dialog v-model="addOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 19l7-7 3 3-7 7-3-3z"/><path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z"/></svg>
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
            <template #suffix><el-icon class="picker-suffix-icon" @click="openFormPicker"><Search /></el-icon></template>
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

    <!-- ===== 部署方式弹窗（泛微式：存为新版 / 覆盖当前版本） ===== -->
    <el-dialog v-model="deployOpen" width="520px" append-to-body draggable class="rd-dialog" title="部署流程模型" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="模型名称">
          <span class="badge blue"><span class="dot"></span>{{ deployRow?.modelName || '—' }}</span>
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
          <span class="form-tip" style="margin-left:12px">{{ deployMode === 'replace' ? '覆盖模式自动设为活动版本' : '开启后新发起流程优先使用该版本' }}</span>
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

    <!-- ===== 版本管理编辑弹框（泛微式：版本号/版本备注/设为活动版本） ===== -->
    <el-dialog v-model="versionOpen" width="480px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
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
          <span class="form-tip" style="margin-left:12px">{{ versionEdit.versionActive ? '已是当前活动版本' : (versionEdit.suspended ? '该版本已挂起，请先激活' : '开启后新发起流程优先使用该版本') }}</span>
        </el-form-item>
        <el-form-item label="版本状态">
          <span class="badge" :class="versionEdit.suspended ? 'gray' : 'green'">
            <span class="dot"></span>{{ versionEdit.suspended ? '已挂起' : '已激活' }}
          </span>
          <span class="form-tip" style="margin-left:12px">
            {{ versionEdit.suspended ? '挂起中：该版本不可发起新流程' : '正常：该版本可发起新流程' }}
          </span>
        </el-form-item>
        <el-form-item label="操作">
          <el-button v-if="!versionEdit.suspended" type="warning" plain icon="VideoPause" :loading="versionStateLoading"
            v-hasPermi="['flowable:model:edit']" @click="handleVersionSuspend(true)">挂起该版本</el-button>
          <el-button v-else type="success" plain icon="VideoPlay" :loading="versionStateLoading"
            v-hasPermi="['flowable:model:edit']" @click="handleVersionSuspend(false)">激活该版本</el-button>
          <div class="form-tip" style="margin-top:6px">挂起后该版本不可发起新流程，运行中实例不受影响</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitVersionEdit">确 定</el-button>
        <el-button @click="versionOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 流程设计器弹窗（bpmn-js + 自研属性面板） ===== -->
    <el-dialog v-model="designOpen" fullscreen append-to-body class="designer-dialog" :close-on-click-modal="false" destroy-on-close @opened="onDesignerOpened">
      <template #header>
        <div class="designer-header">
          <div class="designer-title">
            <span class="glyph"></span>
            流程设计器
            <span class="designer-model-name">{{ designModel.modelName }}</span>
            <span class="badge blue"><span class="dot"></span>Key: {{ designModel.modelKey }}</span>
          </div>
          <div class="designer-actions">
            <el-button type="primary" icon="Check" @click="saveDesigner" v-hasPermi="['flowable:model:edit']">保存</el-button>
            <el-button @click="designOpen = false">关 闭</el-button>
          </div>
        </div>
      </template>
      <!-- 泛微式版本标签行：V1/V2/... 点击弹出版本编辑（版本备注/设为活动版本） -->
      <div class="version-tab-row" v-if="designVersions.length">
        <span class="version-row-label"><el-icon><Clock /></el-icon> 版本管理</span>
        <span class="version-tab" v-for="v in designVersions" :key="v.modelId"
          :class="{ 'is-active': designVersionSelected && designVersionSelected.modelId === v.modelId }"
          @click="openVersionEditor(v)">
          V{{ v.version }}
          <span class="vt-badge" v-if="v.activeFlag === '1'">当前</span>
        </span>
        <span class="version-row-tip">点击版本号可编辑备注、设为活动版本</span>
      </div>
      <el-tabs v-model="designTab" class="designer-tabs" @tab-change="onDesignTabChange">
        <!-- ===== 基本信息（泛微式：流程基本信息） ===== -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form label-width="110px" class="design-basic-form">
            <el-form-item label="模型名称" required><el-input v-model="designModel.modelName" placeholder="如：请假审批流程" maxlength="50" /></el-form-item>
            <el-form-item label="模型Key"><el-input :model-value="designModel.modelKey" disabled /><div class="form-tip">流程唯一标识，创建后不可修改</div></el-form-item>
            <el-form-item label="流程分类"><el-select v-model="designModel.category" placeholder="请选择" style="width:100%">
              <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
            </el-select></el-form-item>
            <el-form-item label="流程描述"><el-input v-model="designModel.description" type="textarea" :rows="3" maxlength="200" placeholder="流程用途说明" /></el-form-item>
            <el-form-item label="关联表单"><el-select v-model="designModel.formId" filterable clearable placeholder="选择流程表单（发起时渲染填写）" style="width:100%">
              <el-option v-for="f in formOptions" :key="f.formId" :label="f.formName" :value="f.formId" />
            </el-select><div class="form-tip">也可在"表单管理"页签查看表单详情；修改后点击右上"保存"生效</div></el-form-item>
            <el-form-item label="当前状态">
              <span class="badge" :class="designModel.deployed === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ designModel.deployed === '1' ? '已部署' : '未部署' }}</span>
              <span class="badge blue" style="margin-left:8px" v-if="designActiveVersion"><span class="dot"></span>当前版本 V{{ designActiveVersion }}</span>
              <span class="badge gray" style="margin-left:8px" v-else><span class="dot"></span>未设置活动版本</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- ===== 表单管理（泛微式：基本信息/编辑字段二级页签，表单设计引用组件） ===== -->
        <el-tab-pane label="表单管理" name="form">
          <el-tabs v-model="formTab" type="card" class="form-sub-tabs">
            <el-tab-pane label="基本信息" name="basic">
          <el-form label-width="110px" class="design-basic-form" v-if="designModel.formId">
            <el-form-item label="表单名称"><el-input :model-value="boundFormInfo.formName || '—'" disabled /></el-form-item>
            <el-form-item label="数据库表名"><el-input :model-value="boundFormInfo.tableName || '未生成'" disabled /><div class="form-tip">表单保存设计后自动生成，此处只读展示</div></el-form-item>
            <el-form-item label="表单描述"><el-input :model-value="boundFormInfo.formDesc || '—'" disabled /></el-form-item>
            <el-form-item label="字段数"><el-input :model-value="boundFormInfo.fieldCount != null ? boundFormInfo.fieldCount + ' 个' : '—'" disabled /></el-form-item>
            <el-form-item label="更换表单"><el-select v-model="designModel.formId" filterable clearable placeholder="选择流程表单" style="width:100%">
              <el-option v-for="f in formOptions" :key="f.formId" :label="f.formName" :value="f.formId" />
            </el-select><div class="form-tip">更换后点击右上"保存"生效；发起流程时按此表单渲染填写</div></el-form-item>
          </el-form>
          <el-form label-width="110px" class="design-basic-form" v-else>
            <el-form-item label="关联表单"><el-select v-model="designModel.formId" filterable clearable placeholder="当前未关联表单，请选择" style="width:100%">
              <el-option v-for="f in formOptions" :key="f.formId" :label="f.formName" :value="f.formId" />
            </el-select><div class="form-tip">关联后发起流程时按此表单渲染填写；保存后可在此编辑字段、配置字段权限</div></el-form-item>
          </el-form>
            </el-tab-pane>
            <el-tab-pane label="编辑字段" name="fields">
              <FormFieldsEditor v-if="designModel.formId" :form-id="designModel.formId" @saved="onFormFieldsSaved" />
              <div v-else class="form-tip" style="padding:16px 12px">请先在"基本信息"中关联表单并点击右上"保存"后，再编辑字段</div>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
        <!-- ===== 流转设置（图形编辑 + 节点属性，参照泛微节点信息） ===== -->
        <el-tab-pane label="流转设置" name="flow">
          <!-- 流转设置编辑入口（泛微式：在子页签行顶部，管图形编辑/节点/出口三个子页签） -->
          <div class="graph-toolbar">
            <el-button size="small" type="primary" icon="EditPen" @click="openGraphicEditor">编 辑</el-button>
            <span class="graph-tip">点击"编辑"进入独立编辑页面：流程图拖拽绘制、节点/出口新增删除、统一保存</span>
          </div>
          <el-tabs v-model="flowTab" type="card" class="form-sub-tabs">
            <el-tab-pane label="图形编辑" name="graph">
      <div class="designer-body">
        <div class="designer-canvas" ref="canvasRef"></div>
      </div>
            </el-tab-pane>
            <!-- ===== 节点信息（表格方式展示节点，参照泛微节点信息） ===== -->
            <el-tab-pane label="节点信息" name="nodes">
              <div class="batch-toolbar">
                <span class="field-tip">只读展示：节点名称/操作者等编辑请点"编 辑"进入独立流程图编辑页面</span>
              </div>
              <el-table :data="flowNodes" border size="small" max-height="420" class="field-table" highlight-current-row>
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column label="节点名称" min-width="140" align="center">
                  <template #default="scope">{{ scope.row.name || scope.row.id }}</template>
                </el-table-column>
                <el-table-column label="节点类型" width="110" align="center">
                  <template #default="scope"><span class="badge blue"><span class="dot"></span>{{ scope.row.typeLabel }}</span></template>
                </el-table-column>
                <el-table-column label="分支语义" width="110" align="center">
                  <template #default="scope">{{ branchRoleLabelFor(scope.row.type, scope.row.id, flowExits) }}</template>
                </el-table-column>
                <el-table-column label="操作者（办理人）" min-width="160" align="center">
                  <template #default="scope"><span v-if="scope.row.assignee">{{ scope.row.assignee }}</span><span v-else style="color:#cbd5e1">未设置</span></template>
                </el-table-column>
                <el-table-column label="候选组" min-width="150" align="center">
                  <template #default="scope"><span v-if="scope.row.candidateGroups">{{ scope.row.candidateGroups }}</span><span v-else style="color:#cbd5e1">—</span></template>
                </el-table-column>
                <el-table-column label="表单内容" width="110" align="center">
                  <template #default="scope"><span v-if="scope.row.formKey">{{ scope.row.formKey }}</span><span v-else style="color:#cbd5e1">Html模式</span></template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <!-- ===== 出口信息（连线与分支条件只读展示） ===== -->
            <el-tab-pane label="出口信息" name="exits">
              <div class="batch-toolbar">
                <span class="field-tip">只读展示：连线名称/分支条件编辑请点"编 辑"进入独立流程图编辑页面</span>
              </div>
              <el-table :data="flowExits" border size="small" max-height="420" class="field-table" highlight-current-row>
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column label="连线名称" min-width="130" align="center">
                  <template #default="scope">{{ scope.row.name || '—' }}</template>
                </el-table-column>
                <el-table-column label="来源节点" min-width="120" align="center"><template #default="scope">{{ scope.row.sourceName || scope.row.sourceId }}</template></el-table-column>
                <el-table-column label="目标节点" min-width="120" align="center"><template #default="scope">{{ scope.row.targetName || scope.row.targetId }}</template></el-table-column>
                <el-table-column label="分支条件" min-width="200" align="center">
                  <template #default="scope"><span v-if="scope.row.condition">{{ scope.row.condition }}</span><span v-else style="color:#cbd5e1">—</span></template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
        <!-- ===== 高级设置（泛微式：高级功能设置） ===== -->
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
              <span class="form-tip" style="margin-left:12px">锁定后保存设计时需二次确认，防止误改已上线流程</span>
            </el-form-item>
            <el-form-item label="帮助文档"><el-input v-model="designModel.helpDoc" placeholder="帮助文档链接或说明（可选）" maxlength="200" /></el-form-item>
            <el-form-item label="显示顺序"><el-input-number v-model="designModel.orderNum" :min="0" :max="999" :controls="false" style="width:160px" /><div class="form-tip">数字越小排序越靠前</div></el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <input ref="xmlFileRef" type="file" accept=".xml,.bpmn" style="display:none" @change="onXmlFileChange" />
    </el-dialog>

    <!-- ===== 图形化编辑独立全屏弹窗（泛微式：点击"编辑"进入独立编辑页面，关闭返回列表） ===== -->
    <el-dialog v-model="graphicOpen" fullscreen append-to-body class="designer-dialog" :close-on-click-modal="false" destroy-on-close @opened="onGraphicOpened">
      <template #header>
        <div class="designer-header">
          <div class="designer-title">
            <span class="glyph"></span>
            流程图编辑
            <span class="designer-model-name">{{ designModel.modelName }}</span>
            <span class="badge blue"><span class="dot"></span>Key: {{ designModel.modelKey }}</span>
          </div>
          <div class="designer-actions">
            <el-tooltip content="撤销"><el-button icon="RefreshLeft" @click="graphicCmd('undo')" /></el-tooltip>
            <el-tooltip content="重做"><el-button icon="RefreshRight" @click="graphicCmd('redo')" /></el-tooltip>
            <el-tooltip content="放大"><el-button icon="ZoomIn" @click="graphicZoom(0.1)" /></el-tooltip>
            <el-tooltip content="缩小"><el-button icon="ZoomOut" @click="graphicZoom(-0.1)" /></el-tooltip>
            <el-tooltip content="适应画布"><el-button icon="FullScreen" @click="graphicZoomReset" /></el-tooltip>
            <el-tooltip content="删除选中"><el-button icon="Delete" @click="graphicDelete" /></el-tooltip>
            <el-divider direction="vertical" />
            <el-button icon="Upload" @click="graphicImportXml">导入XML</el-button>
            <el-button icon="Download" @click="graphicExportXml">导出XML</el-button>
            <el-button type="primary" icon="Check" @click="graphicSave" v-hasPermi="['flowable:model:edit']">保存</el-button>
            <el-button @click="graphicClose">关 闭</el-button>
          </div>
        </div>
      </template>
      <div class="designer-body graphic-body">
        <el-tabs v-model="graphicTab" type="card" class="graphic-editor-tabs">
          <el-tab-pane label="图形编辑" name="graph" class="graphic-pane">
        <div class="palette-host" ref="paletteHostRef"></div>
        <div class="graphic-main">
          <div class="designer-canvas" ref="graphicCanvasRef"></div>
          <div class="props-handle" v-if="selectedElement" @click="propsCollapsed = !propsCollapsed" :title="propsCollapsed ? '展开节点信息' : '收起节点信息'"><span>{{ propsCollapsed ? '‹' : '›' }}</span></div>
          <div class="designer-props" v-if="selectedElement && !propsCollapsed">
          <template v-if="selectedElement">
            <div class="props-title">
              <span class="glyph"></span> 节点信息
              <el-tag size="small" effect="plain" style="margin-left:8px">{{ selectedElement.type.replace('bpmn:', '') }}</el-tag>
            </div>
            <el-form label-width="86px" size="small" class="props-form">
              <el-form-item label="节点ID">
                <el-input v-model="selectedElement.id" disabled />
              </el-form-item>
              <el-form-item :label="selectedElement.isSequenceFlow ? '连线名称' : '节点名称'">
                <el-input v-model="selectedElement.name" :placeholder="selectedElement.isSequenceFlow ? '如：3天以上' : '如：部门经理审批'" @change="updateElementProp('name', selectedElement.name)" />
              </el-form-item>

              <!-- 用户任务属性 -->
              <template v-if="selectedElement.isUserTask">
                <el-form-item label="办理人">
                  <el-select v-model="selectedElement.assignee" placeholder="选择用户或留空" filterable clearable allow-create style="width:100%" @change="updateAssignee">
                    <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="String(u.userId)" />
                  </el-select>
                  <div class="props-tip">也可直接输入用户ID；审批任务将出现在其待办</div>
                </el-form-item>
                <el-form-item label="候选组">
                  <el-select v-model="selectedElement.candidateGroups" placeholder="选择角色或留空" filterable clearable allow-create style="width:100%" @change="updateCandidateGroups">
                    <el-option v-for="r in roleOptions" :key="r.roleId" :label="r.roleName + '（' + r.roleKey + '）'" :value="String(r.roleId)" />
                  </el-select>
                  <div class="props-tip">角色下所有用户均可认领办理</div>
                </el-form-item>
                <el-form-item label="多实例">
                  <el-switch v-model="selectedElement.multiInstance" @change="toggleMultiInstance" />
                  <div class="props-tip">开启即会签（所有人办理完才通过）</div>
                </el-form-item>
                <el-form-item label="表单Key">
                  <el-input v-model="selectedElement.formKey" placeholder="关联业务表单Key（可选）" @change="updateFormKey" />
                  <div class="props-tip">原型能力：任务办理页可按此 Key 加载业务表单</div>
                </el-form-item>
                <el-form-item label="字段权限">
                  <el-button size="small" style="width:100%" @click="openFieldPerms">配置本节点字段权限</el-button>
                  <div class="props-tip">{{ fieldPermsSummary }}</div>
                </el-form-item>
              </template>

              <!-- 分支语义（业务语义层）：审批节点的出口/入口数即分支起点/合并点，不写回 XML -->
              <template v-if="selectedElement.isUserTask || selectedElement.isBranchNode">
                <el-form-item label="分支语义">
                  <el-select v-model="selectedElement.branchRole" style="width:100%" @change="updateBranchRole">
                    <el-option v-for="r in BRANCH_ROLES" :key="r.value" :label="r.label" :value="r.value" />
                  </el-select>
                  <div class="props-tip">{{ branchRoleTip }}</div>
                </el-form-item>
              </template>

              <!-- 连线条件（网关分支） -->
              <template v-if="selectedElement.isSequenceFlow">
                <el-form-item label="分支条件">
                  <el-input v-model="selectedElement.condition" type="textarea" :rows="2"
                    placeholder="如：${days > 3}" @change="updateCondition" />
                  <div class="props-tip">
                    JUEL 表达式，引用发起流程时传入的变量。{{ selectedElement.sourceIsGateway ? '当前连线路径于网关出口，按顺序判断。' : '提示：仅排他/包容网关出口的连线条件生效。' }}
                  </div>
                </el-form-item>
                <el-form-item label=" ">
                  <el-button size="small" @click="clearCondition">清除条件</el-button>
                </el-form-item>
              </template>
            </el-form>
          </template>
          <div class="props-help">
            <div class="props-help-title">使用提示</div>
            <ul>
              <li>顶部按钮组独立：撤销/重做、缩放、删除、导入/导出XML、保存</li>
              <li>点"保存"同步回设计器画布并保存到系统</li>
              <li>点"关 闭"返回流转设置；未保存的改动不生效</li>
            </ul>
          </div>
        </div>
        </div>
          </el-tab-pane>
          <el-tab-pane label="节点信息" name="nodes">
            <div class="batch-toolbar">
              <el-button-group>
                <el-button size="small" icon="Plus" @click="openAddNode" title="新增节点" />
                <el-button size="small" icon="Minus" :disabled="!graphicSelectedRow" @click="removeGraphicNode" title="删除选中节点" />
              </el-button-group>
              <span class="field-tip">选中行后加减：新增节点在选中节点后追加；修改实时写入流程图，点"保存"生效</span>
            </div>
            <el-table :data="graphicNodes" border size="small" max-height="420" class="field-table" highlight-current-row @current-change="onGraphicNodeSelect">
              <el-table-column type="index" label="序号" width="55" align="center" />
              <el-table-column label="节点名称" min-width="140" align="center">
                <template #default="scope"><el-input v-model="scope.row.name" placeholder="如：部门审批" size="small" @change="updateNodeProp(scope.row, 'name', scope.row.name)" /></template>
              </el-table-column>
              <el-table-column label="节点类型" width="110" align="center">
                <template #default="scope"><span class="badge blue"><span class="dot"></span>{{ scope.row.typeLabel }}</span></template>
              </el-table-column>
              <el-table-column label="分支语义" width="110" align="center">
                <template #default="scope">
                  <span v-if="scope.row.isUserTask || isGatewayType(scope.row.type)">{{ branchRoleLabel(scope.row) }}</span>
                  <span v-else style="color:#cbd5e1">—</span>
                </template>
              </el-table-column>
              <el-table-column label="操作者（办理人）" min-width="160" align="center">
                <template #default="scope">
                  <el-select v-if="scope.row.isUserTask" v-model="scope.row.assignee" placeholder="未设置" clearable filterable allow-create size="small" style="width:100%" @change="updateNodeAssignee(scope.row)">
                    <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="String(u.userId)" />
                  </el-select>
                  <span v-else style="color:#cbd5e1">—</span>
                </template>
              </el-table-column>
              <el-table-column label="候选组" min-width="150" align="center">
                <template #default="scope">
                  <el-select v-if="scope.row.isUserTask" v-model="scope.row.candidateGroups" placeholder="未设置" clearable filterable allow-create size="small" style="width:100%" @change="updateNodeCandidate(scope.row)">
                    <el-option v-for="r in roleOptions" :key="r.roleId" :label="r.roleName + '（' + r.roleKey + '）'" :value="String(r.roleId)" />
                  </el-select>
                  <span v-else style="color:#cbd5e1">—</span>
                </template>
              </el-table-column>
              <el-table-column label="表单内容" width="110" align="center">
                <template #default="scope"><span v-if="scope.row.formKey">{{ scope.row.formKey }}</span><span v-else style="color:#cbd5e1">Html模式</span></template>
              </el-table-column>
              <el-table-column label="操作组" width="110" align="center">
                <template #default="scope">
                  <el-button v-if="scope.row.isUserTask" link type="primary" size="small" @click="openOperatorGroups(scope.row)">
                    <el-icon><Setting /></el-icon>
                    <span style="margin-left:2px">{{ operatorGroupSummary(scope.row) }}</span>
                  </el-button>
                  <span v-else style="color:#cbd5e1">—</span>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="出口信息" name="exits">
            <div class="batch-toolbar">
              <el-button-group>
                <el-button size="small" icon="Plus" @click="openAddLine" title="新增出口" />
                <el-button size="small" icon="Minus" :disabled="!graphicSelectedLine" @click="removeGraphicLine" title="删除选中出口" />
              </el-button-group>
              <el-select v-model="exitFilterNodeId" placeholder="******请选择当前节点******" clearable filterable size="small" style="width:220px;margin-left:12px" @change="onExitFilterChange">
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
              <el-table-column label="源节点" min-width="110" align="center"><template #default="scope">{{ scope.row.sourceName || scope.row.sourceId }}</template></el-table-column>
              <el-table-column label="目标节点" min-width="110" align="center"><template #default="scope">{{ scope.row.targetName || scope.row.targetId }}</template></el-table-column>
              <el-table-column label="出口名称" min-width="120" align="center">
                <template #default="scope"><el-input v-model="scope.row.name" placeholder="如：3天以上" size="small" @change="updateNodeProp(scope.row, 'name', scope.row.name)" /></template>
              </el-table-column>
              <el-table-column label="是否退回" width="80" align="center">
                <template #default="scope">
                  <el-checkbox :model-value="!!scope.row.isReturn" @change="val => updateExitMeta(scope.row, 'isReturn', val)" />
                </template>
              </el-table-column>
              <el-table-column label="出口条件" width="120" align="center">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click="openExitRules(scope.row)">
                    <el-icon><Setting /></el-icon>
                    <el-icon v-if="scope.row.condition" style="color:#22c55e;margin-left:2px"><Check /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column label="附加规则" width="90" align="center">
                <template #default="scope">
                  <el-input v-model="scope.row.extraRule" placeholder="规则编号" size="small" @change="val => updateExitMeta(scope.row, 'extraRule', val)" />
                </template>
              </el-table-column>
              <el-table-column label="生成编号" width="80" align="center">
                <template #default="scope">
                  <el-checkbox :model-value="!!scope.row.genNo" @change="val => updateExitMeta(scope.row, 'genNo', val)" />
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
      <input ref="graphicFileRef" type="file" accept=".xml,.bpmn" style="display:none" @change="onGraphicFileChange" />
    </el-dialog>

    <!-- 新增节点弹框（编辑页面：在选中节点后追加） -->
    <el-dialog v-model="addNodeOpen" width="440px" append-to-body draggable class="rd-dialog" title="新增节点" :close-on-click-modal="false">
      <el-form label-width="90px" size="small">
        <el-form-item label="节点类型">
          <el-select v-model="addNodeForm.type" style="width:100%">
            <el-option v-for="t in ADD_NODE_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分支方式" v-if="addNodeForm.type === 'bpmn:ExclusiveGateway'">
          <el-select v-model="addNodeForm.branchMode" style="width:100%">
            <el-option v-for="b in ADD_BRANCH_MODES" :key="b.value" :label="b.label" :value="b.value" />
          </el-select>
          <div class="form-tip">分支方式仅决定落成的网关类型，出口条件仍可回车在画布/出口信息中调整</div>
        </el-form-item>
        <el-form-item label="节点名称"><el-input v-model="addNodeForm.name" placeholder="如：总经理审批（可留空）" maxlength="30" /></el-form-item>
        <el-form-item label="来源节点"><el-input :model-value="graphicSelectedRow ? (graphicSelectedRow.name || graphicSelectedRow.id) : '—'" disabled /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAddNode">确 定</el-button>
        <el-button @click="addNodeOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 新增出口弹框（编辑页面：来源→目标连线） -->
    <el-dialog v-model="addLineOpen" width="440px" append-to-body draggable class="rd-dialog" title="新增出口" :close-on-click-modal="false">
      <el-form label-width="90px" size="small">
        <el-form-item label="来源节点">
          <el-select v-model="addLineForm.sourceId" filterable placeholder="选择来源节点" style="width:100%">
            <el-option v-for="n in graphicNodes" :key="n.id" :label="(n.name || n.id) + '（' + n.typeLabel + '）'" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标节点">
          <el-select v-model="addLineForm.targetId" filterable placeholder="选择目标节点" style="width:100%">
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

    <!-- ===== 出口条件规则配置弹窗（参照泛微出口条件设置：规则列表 + AND/OR + 规则引用） ===== -->
    <el-dialog v-model="exitRulesOpen" width="760px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><Setting /></el-icon></div>
          <div class="rd-detail-header-title">出口条件设置（{{ exitRulesRow ? (exitRulesRow.name || exitRulesRow.id) : '' }}）</div>
        </div>
      </template>
      <div class="batch-toolbar" style="margin-bottom:10px">
        <span class="rules-label">规则间关系：</span>
        <el-radio-group v-model="exitRulesForm.join" size="small">
          <el-radio-button value="AND">AND</el-radio-button>
          <el-radio-button value="OR">OR</el-radio-button>
        </el-radio-group>
        <el-button size="small" type="primary" icon="Plus" style="margin-left:12px" @click="addRuleRow">添加规则</el-button>
        <el-button size="small" icon="Minus" :disabled="exitRulesForm.rows.length <= 1" @click="removeRuleRow">删除规则</el-button>
        <span class="field-tip" style="margin-left:12px">多条规则按 AND/OR 组合为一个 JUEL 表达式写入出口条件</span>
      </div>
      <el-table :data="exitRulesForm.rows" border size="small" max-height="300" class="field-table">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="字段（流程变量）" min-width="150" align="center">
          <template #default="scope"><el-input v-model="scope.row.field" placeholder="如：days" size="small" /></template>
        </el-table-column>
        <el-table-column label="运算符" width="130" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.op" size="small" style="width:100%">
              <el-option v-for="o in RULE_OPS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="值（逗号分隔多个值）" min-width="180" align="center">
          <template #default="scope"><el-input v-model="scope.row.value" placeholder="如：3 / 张三,李四" size="small" /></template>
        </el-table-column>
        <el-table-column label="条件预览" min-width="180" align="center">
          <template #default="scope"><span style="font-family:monospace">{{ ruleToExpr(scope.row) || '—' }}</span></template>
        </el-table-column>
      </el-table>
      <div class="batch-toolbar" style="margin-top:10px">
        <span class="rules-label">从其他出口引用规则：</span>
        <el-select v-model="exitRulesCopyFrom" placeholder="选择已配置条件的出口" clearable filterable size="small" style="width:240px">
          <el-option v-for="e in otherRuleExits" :key="e.id" :label="(e.name || e.id) + '（' + (e.condition || '') + '）'" :value="e.id" />
        </el-select>
        <el-button size="small" style="margin-left:8px" @click="applyCopyExitRules" :disabled="!exitRulesCopyFrom">复 制</el-button>
      </div>
      <div class="batch-toolbar" style="margin-top:6px">
        <span class="rules-label">最终表达式：</span>
        <span style="font-family:monospace;color:#2563eb">{{ exitRulesExpr || '（空）' }}</span>
      </div>
      <template #footer>
        <el-button type="primary" @click="saveExitRules">保 存</el-button>
        <el-button @click="clearExitRules">清除条件</el-button>
        <el-button @click="exitRulesOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 节点编辑操作组弹窗（参照泛微「编辑操作组」：8 种操作者类型 + 多组操作者 + 条件/批次/会签） ===== -->
    <el-dialog v-model="opGroupsOpen" width="820px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><Setting /></el-icon></div>
          <div class="rd-detail-header-title">编辑操作组（{{ opGroupsNodeName }}）</div>
        </div>
      </template>
      <div class="batch-toolbar" style="margin-bottom:10px">
        <el-button size="small" type="primary" icon="Plus" @click="addOperatorGroup">添加操作者设置</el-button>
        <span class="field-tip">一个节点可配置多组操作者；同一批次操作者并行办理，跨批次按顺序串行；点"保存"生效</span>
      </div>
      <el-collapse v-model="opGroupsActiveNames">
        <el-collapse-item v-for="(g, gi) in opGroupsForm" :key="gi" :name="String(gi)">
          <template #title>
            <span style="font-weight:600">操作组 {{ gi + 1 }}</span>
            <span style="margin-left:8px;font-size:12px;color:#64748b">{{ opGroupTitle(g) }}</span>
            <el-button link type="danger" size="small" style="margin-left:12px" @click.stop="removeOperatorGroup(gi)">删除操作组</el-button>
          </template>
          <el-form label-width="86px" size="small" class="props-form">
            <el-form-item label="操作组名称">
              <el-input v-model="g.name" placeholder="如：财务确认" maxlength="30" style="width:220px" />
            </el-form-item>
            <el-form-item label="操作者类型">
              <el-radio-group v-model="g.type">
                <el-radio v-for="t in OPERATOR_TYPES" :key="t.value" :value="t.value" style="margin-right:8px">{{ t.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="g.type === 'user'" label="指定人">
              <el-select v-model="g.userId" placeholder="选择用户" filterable clearable style="width:100%" @change="val => { g.refName = opUserLabel(val) }">
                <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="String(u.userId)" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="g.type === 'role'" label="指定角色">
              <el-select v-model="g.roleId" placeholder="选择角色" filterable clearable style="width:100%" @change="val => { g.refName = opRoleLabel(val) }">
                <el-option v-for="r in roleOptions" :key="r.roleId" :label="r.roleName + '（' + r.roleKey + '）'" :value="String(r.roleId)" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="g.type === 'dept' || g.type === 'subDept'" :label="opTypeLabel(g.type)">
              <el-tree-select v-model="g.refId" :data="deptTreeOptions" :props="{ label: 'label', children: 'children' }" node-key="id" value-key="id" check-strictly filterable placeholder="选择部门" clearable style="width:100%" @node-click="() => { g.refName = opDeptLabel(g.refId) }" @change="val => { g.refName = opDeptLabel(val) }" />
            </el-form-item>
            <el-form-item v-if="g.type === 'post'" label="指定岗位">
              <el-select v-model="g.refId" placeholder="选择岗位" filterable clearable style="width:100%" @change="val => { g.refName = opPostLabel(val) }">
                <el-option v-for="p in postOptions" :key="p.postId" :label="p.postName + '（' + p.postCode + '）'" :value="String(p.postId)" />
              </el-select>
            </el-form-item>
            <el-form-item label="办理方式">
              <el-select v-model="g.counterSign" style="width:180px" @change="val => { g.counterSign = val }">
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
              <el-input-number v-model="g.batch" :min="0" :max="99" :controls="false" style="width:120px" />
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
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
          </div>
          <span class="rd-detail-header-title">字段权限：{{ fieldPermsNodeName }}</span>
        </div>
      </template>
      <div class="field-perms-tip">控制该审批节点办理人在任务审批页看到的表单字段：可编辑 / 只读 / 隐藏。未配置的字段默认只读展示。</div>
      <div class="field-perms-copybar" v-if="otherPermNodes.length">
        <span class="fpc-label">复制其他节点：</span>
        <el-select v-model="copyPermFrom" size="small" placeholder="选择已配置权限的节点" style="width:220px">
          <el-option v-for="n in otherPermNodes" :key="n.id" :label="n.name + '（' + n.summary + '）'" :value="n.id" />
        </el-select>
        <el-button size="small" type="primary" plain :disabled="!copyPermFrom" @click="applyCopyPerms">复制</el-button>
      </div>
      <el-table :data="fieldPermsRows" border size="small" max-height="360" class="field-perms-table" v-loading="fieldPermsLoading">
        <el-table-column type="index" label="序号" key="序号" width="55" align="center" />
        <el-table-column label="字段" min-width="150">
          <template #default="s"><span class="col-strong">{{ s.row.label }}</span><em class="fp-field">{{ s.row.field }}</em></template>
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
  </div>
</template>

<script setup name="FlowModel">
import { listModel, getModel, addModel, updateModel, delModel, deployModel, listDefinition, updateDefinitionRemark, setActiveDefinition, changeDefinitionState } from '@/api/flowable/flow'
import { allForms } from '@/api/flowable/form'
import FormPicker from '@/components/FormPicker/index.vue'
import request from '@/utils/request'
import { ElMessageBox } from 'element-plus'
import { h, nextTick, ref, computed, watch, reactive, toRefs } from 'vue'
import { Search, Filter, Document, Pointer, RefreshLeft, RefreshRight, ZoomIn, ZoomOut, FullScreen, Setting, Check } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { useColumnResize } from '@/composables/useColumnResize'
import { useColumnDrag } from '@/composables/useColumnDrag'
import FormFieldsEditor from '../form/components/FormFieldsEditor.vue'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { flowable_category } = proxy.useDict('flowable_category')
const { collapsedCards, toggleCard } = useDetailCard(["v1"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_model_index')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const addOpen = ref(false)
const designOpen = ref(false)
const designModel = ref({})
const canvasRef = ref(null)
const xmlFileRef = ref(null)
const selectedElement = ref(null)
let bpmnModeler = null
let modeling = null
let selectedEl = null   // 当前选中的 bpmn 元素（非响应式）
let selectedElSource = 'main' // 选中元素来源：main=设计器只读视图已弃用编辑，graphic=独立编辑页
/** 按选中元素所属实例取 modeling（只读视图无 modeling，编辑统一在独立编辑页） */
function currentModeling() {
  return (selectedElSource === 'graphic' && graphicModeler) ? graphicModeler.get('modeling') : modeling
}
const userOptions = ref([])
const roleOptions = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, modelName: undefined, modelKey: undefined, category: undefined, deployed: undefined },
  rules: {
    modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
    modelKey: [
      { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '仅允许字母开头，含字母/数字/下划线', trigger: 'blur' }
    ],
    category: [{ required: true, message: '请选择流程分类', trigger: 'change' }],
    formId: [{ required: true, message: '请选择关联表单', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.modelName) count++
  if (queryParams.value.modelKey) count++
  if (queryParams.value.category) count++
  if (queryParams.value.deployed) count++
  return count
})

const defaultColumns = {
  modelName: { label: '模型名称', visible: true },
  modelKey: { label: '模型Key', visible: true },
  category: { label: '流程分类', visible: true },
  version: { label: '版本', visible: true },
  deployed: { label: '部署状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
const columns = ref(loadColumnVisibility())
// 列顺序拖拽持久化（序号列也可拖动，与列宽拖拽共存）
const { sortedColumnKeys, initSortable } = useColumnDrag('flowable_model_index', ['序号', 'modelName', 'modelKey', 'category', 'formName', 'version', 'deployed', 'createTime'], tableRef, colWidth)
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_model_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

function categoryLabel(value) {
  const item = flowable_category.value.find(d => d.value == value)
  return item ? item.label : (value || '-')
}

function getList() {
  loading.value = true
  listModel(queryParams.value).then(res => {
    let rows = res.rows || []
    if (queryParams.value.deployed) {
      rows = rows.filter(r => r.deployed === queryParams.value.deployed)
    }
    list.value = rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
    initSortable()
  }).catch(() => { loading.value = false })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.modelName = undefined
  queryParams.value.modelKey = undefined
  queryParams.value.category = undefined
  queryParams.value.deployed = undefined
  handleQuery()
}

function handleAdd() {
  form.value = { modelName: undefined, modelKey: undefined, category: 'oa', description: undefined, formId: undefined }
  loadFormOptions()
  addOpen.value = true
}

/**
 * 自动生成模型Key：统一 wf_ 前缀 + 名称转写
 * - 英文名：wf_ + 小写下划线（如 Test Auto Flow → wf_test_auto_flow）
 * - 中文名：wf_ + 时间戳 + 随机数兜底
 * 仅作为「用户未填写」时的默认值，用户可手工覆盖
 */
function buildAutoModelKey() {
  const name = (form.value.modelName || '').trim()
  const ascii = name.replace(/[^a-zA-Z0-9]+/g, '_').replace(/^_+|_+$/g, '').toLowerCase()
  if (ascii && /^[a-z]/.test(ascii)) return ('wf_' + ascii).slice(0, 60)
  // 纯中文/数字开头：用 wf_ + 时间戳（秒级）+ 3位随机，保证可通过 key 正则且不易撞
  const ts = Math.floor(Date.now() / 1000).toString().slice(-8)
  const rnd = Math.floor(Math.random() * 900 + 100)
  return 'wf_' + ts + rnd
}

/** 模型名称失焦：Key 为空时自动填充 */
function onModelNameBlur() {
  if (!form.value.modelKey && form.value.modelName) {
    form.value.modelKey = buildAutoModelKey()
  }
}

// ===== 关联表单设置（模型侧绑定表单，一个表单可关联多个模型；列表"表单"入口已移除，关联在设计弹窗"基本信息"完成） =====
const formOptions = ref([])
function loadFormOptions() {
  allForms().then(res => { formOptions.value = res.data || [] })
}

// ===== 关联表单弹框选择器（开发规范：业务实体选择须用弹框选择器，禁止 el-select） =====
const formPickerRef = ref(null)
/** 已选表单名称（弹框选择器为只读输入 + 选择按钮的展示口径） */
const selectedFormName = computed(() => {
  if (!form.value.formId) return ''
  const hit = formOptions.value.find(f => String(f.formId) === String(form.value.formId))
  return hit ? hit.formName : (form.value.formName || '已选择表单 #' + form.value.formId)
})
function openFormPicker() {
  formPickerRef.value?.open(form.value.formId)
}
function onFormPickerConfirm(sel) {
  form.value.formId = sel.formId
  form.value.formName = sel.formName
}

function submitAdd() {
  proxy.$refs['modelRef'].validate(valid => {
    if (valid) {
      // Key 留空时兜底自动生成（正常由名称失焦时填充）
      if (!form.value.modelKey) {
        form.value.modelKey = buildAutoModelKey()
      }
      addModel(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功，请点击"设计"绘制流程图')
        addOpen.value = false
        getList()
      })
    }
  })
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
  'Delete': '删除',
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
  'Loop': '循环',
  'Parallel multi-instance': '并行多实例（会签）',
  'Sequential multi-instance': '顺序多实例（依次审批）',
  'Participant multiplicity': '参与者多实例',
  'Ad-hoc': '临时（Ad-hoc）子流程',
  'Toggle non-interrupting': '切换为非中断事件',
  'Collection': '集合',
  // ===== 修改类型菜单（ReplaceMenu） =====
  'Task': '任务',
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
  'Transaction': '事务子流程',
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
  'Expanded pool/participant': '展开的池/参与者'
}
function bpmnTranslate(template, replacements) {
  const text = bpmnTranslations[template] || template
  return text.replace(/{([^}]+)}/g, (_, key) => (replacements && replacements[key]) || '{' + key + '}')
}
const bpmnChineseTranslate = { translate: ['value', bpmnTranslate] }

async function onDesignerOpened() {
  // 设计器必需样式：工具面板/连线菜单/字体图标
  await import('bpmn-js/dist/assets/diagram-js.css')
  await import('bpmn-js/dist/assets/bpmn-js.css')
  await import('bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css')
  // 只读视图：NavigatedViewer（可平移缩放、无编辑面板/快捷面板）——编辑统一进独立编辑页面
  const { default: NavigatedViewer } = await import('bpmn-js/lib/NavigatedViewer.js')
  bpmnModeler = new NavigatedViewer({
    container: canvasRef.value,
    additionalModules: [bpmnChineseTranslate]
  })
  modeling = null
  // 加载用户/角色选项（供独立编辑页面办理人/候选组选择器）
  request({ url: '/system/user/list', method: 'get', params: { pageNum: 1, pageSize: 200 } })
    .then(res => { userOptions.value = res.rows || [] })
    .catch(e => { proxy.$modal.msgWarning('用户列表加载失败，办理人下拉将为空：' + (e?.message || e)) })
  request({ url: '/system/role/list', method: 'get', params: { pageNum: 1, pageSize: 100 } })
    .then(res => { roleOptions.value = res.rows || [] })
    .catch(e => { proxy.$modal.msgWarning('角色列表加载失败，候选组下拉将为空：' + (e?.message || e)) })
  const xml = designModel.value.bpmnXml || ''
  if (!xml) {
    // 只读视图：无流程图时提示（绘制在独立编辑页面进行）
    proxy.$modal.msgWarning('该模型暂无流程图，点"编 辑"开始绘制')
    return
  }
  try {
    await bpmnModeler.importXML(xml)
    bpmnModeler.get('canvas').zoom('fit-viewport', 'auto')
    // 布局稳定后二次适配：内容垂直水平居中，避免贴顶、下方大片空白
    setTimeout(() => { try { bpmnModeler.get('canvas').zoom('fit-viewport', 'auto') } catch (e) {} }, 150)
    applyWmCanvasMarkers(bpmnModeler)
  } catch (e) {
    proxy.$modal.msgError('流程图导入失败：' + (e.message || e))
  }
}

/** 泛微式退回线自动重布局：退回/驳回出口绕行（源上方 → 横向跨越 → 目标顶部），其余连线走默认布局 */
let wmReturnExitChecker = null
function applyWmReturnRoutes(modeler) {
  if (!modeler || !wmReturnExitChecker) { console.log('[wm-debug] applyWmReturnRoutes skip: no modeler/checker', !!modeler, !!wmReturnExitChecker); return }
  try {
    const layouter = modeler.get('layouter')
    const modeling = modeler.get('modeling')
    if (!layouter || !modeling) { console.log('[wm-debug] no layouter/modeling'); return }
    const registry = modeler.get('elementRegistry')
    const flows = registry.filter(el => el.type === 'bpmn:SequenceFlow')
    console.log('[wm-debug] flows:', flows.length, 'checker:', typeof wmReturnExitChecker)
    flows.forEach(el => {
      const hit = wmReturnExitChecker(el)
      if (!hit) return
      const waypoints = layouter.layoutConnection(el, { connectionStart: undefined, connectionEnd: undefined })
      if (waypoints && waypoints.length) {
        modeling.updateWaypoints(el, waypoints)
      }
    })
    // updateWaypoints 触发连线重渲染会丢失 marker，重布局后重新应用视觉标记（红虚线）
    applyWmCanvasMarkers(modeler)
  } catch (e) { console.log('[wm-debug] applyWmReturnRoutes error:', e && e.message) }
}

/** 泛微式画布视觉标记：开始=绿色圆、审批节点=右下出口小箭头、归档=终止样式、退回出口=红色虚线 */
function applyWmCanvasMarkers(modeler) {
  if (!modeler) return
  try {
    const canvas = modeler.get('canvas')
    const registry = modeler.get('elementRegistry')
    registry.forEach(el => {
      if (el.type === 'bpmn:StartEvent') canvas.addMarker(el.id, 'wm-start')
      else if (el.type === 'bpmn:EndEvent') canvas.addMarker(el.id, 'wm-end')
      else if (el.type === 'bpmn:UserTask' || el.type === 'bpmn:Task' || el.type === 'bpmn:ServiceTask') canvas.addMarker(el.id, 'wm-task')
      else if (el.type === 'bpmn:SequenceFlow') {
        const bo = el.businessObject
        let meta = {}
        try { meta = JSON.parse((bo.get && bo.get('flowable:exitMeta')) || bo.$attrs && bo.$attrs['flowable:exitMeta'] || '{}') || {} } catch (e) {}
        const isReturn = !!meta.isReturn || /退回|驳回/.test(bo.name || '')
        if (isReturn) canvas.addMarker(el.id, 'wm-return')
        else canvas.removeMarker(el.id, 'wm-return')
      }
    })
  } catch (e) {}
}

/** 创建新画布：根流程 ID 对齐模型 Key，并默认生成 开始→结束 连线 */
async function newBlankDiagram() {
  await bpmnModeler.createDiagram()
  const canvas = bpmnModeler.get('canvas')
  const root = canvas.getRootElement()
  const modeling = bpmnModeler.get('modeling')
  const elementFactory = bpmnModeler.get('elementFactory')
  modeling.updateProperties(root, {
    id: designModel.value.modelKey,
    name: designModel.value.modelName || designModel.value.modelKey
  })
  // 默认生成 开始 → 结束 及连线
  const start = (root.children || []).find(c => c.type === 'bpmn:StartEvent')
  if (start) {
    modeling.updateProperties(start, { name: '开始' })
    const endAttrs = { name: '结束' }
    const endShape = modeling.createShape(
      elementFactory.createElement('bpmn:EndEvent', endAttrs, root),
      { x: start.x + start.width + 140, y: start.y + start.height / 2 - 18 },
      root
    )
    // 创建后显式补写节点名，确保画布显示“结束”文字
    modeling.updateProperties(endShape, { name: '结束' })
    modeling.connect(start, endShape)
  }
}

function loadSelectedElement(el) {
  selectedEl = el
  const bo = el.businessObject
  const isUserTask = el.type === 'bpmn:UserTask'
  const isSequenceFlow = el.type === 'bpmn:SequenceFlow'
  const isBranchNode = isGatewayType(el.type)
  let condition = ''
  if (isSequenceFlow && bo.conditionExpression) {
    condition = bo.conditionExpression.body || ''
  }
  selectedElement.value = {
    id: bo.id,
    name: bo.name || '',
    type: el.type,
    isUserTask,
    isSequenceFlow,
    isBranchNode,
    sourceIsGateway: !!(el.source && el.source.type && el.source.type.includes('Gateway')),
    condition,
    assignee: (bo.get && bo.get('flowable:assignee')) || bo.assignee || '',
    candidateGroups: (bo.get && bo.get('flowable:candidateGroups')) || bo.candidateGroups || '',
    formKey: (bo.get && bo.get('flowable:formKey')) || bo.formKey || '',
    fieldPerms: parseFieldPerms(bo),
    multiInstance: !!bo.loopCharacteristics,
    // 分支语义为界面派生值，不持久化到 XML
    branchRole: deriveBranchRole(el.type, bo.id)
  }
}

function updateElementProp(key, value) {
  if (selectedEl) currentModeling().updateProperties(selectedEl, { [key]: value })
}

function updateAssignee() {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  currentModeling().updateModdleProperties(selectedEl, bo, { 'flowable:assignee': selectedElement.value.assignee || undefined })
}

function updateCandidateGroups() {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  currentModeling().updateModdleProperties(selectedEl, bo, { 'flowable:candidateGroups': selectedElement.value.candidateGroups || undefined })
}

function updateFormKey() {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  currentModeling().updateModdleProperties(selectedEl, bo, { 'flowable:formKey': selectedElement.value.formKey || undefined })
}

// ===== 分支语义（界面派生状态，不写回 XML，引擎零影响） =====
const BRANCH_ROLES = [
  { value: 'none', label: '一般' },
  { value: 'fork', label: '分支起点' },
  { value: 'join', label: '分支合并点' }
]
/** 取节点出/入口数量（用于派生分支语义与出口提示） */
function countNodeLinks(nodeId) {
  let out = 0, inn = 0
  graphicExits.value.forEach(e => {
    if (e.sourceId === nodeId) out++
    if (e.targetId === nodeId) inn++
  })
  return { out, inn }
}
/** 派生分支语义：网关类或出口≥2 → 分支起点；入口≥2 → 合并点；否则一般 */
function deriveBranchRole(type, nodeId) {
  const { out, inn } = countNodeLinks(nodeId)
  if (isGatewayType(type) || out >= 2) return 'fork'
  if (inn >= 2) return 'join'
  return 'none'
}
const branchRoleTip = computed(() => {
  const se = selectedElement.value
  if (!se) return ''
  const { out, inn } = countNodeLinks(se.id)
  const real = deriveBranchRole(se.type, se.id)
  const roleText = { none: '一般（单进单出）', fork: '分支起点', join: '分支合并点' }[real] || '一般'
  const gap = se.branchRole && se.branchRole !== real
    ? '<br/>当前形状实际为「' + roleText + '」，与所选不一致：请在出口信息中增删连线使其一致'
    : ''
  return '出口 ' + out + ' 条 / 入口 ' + inn + ' 条，实际形状：' + roleText + gap
})
function updateBranchRole() {
  // 语义标签仅前端派生展示，不修改任何 BPMN 属性（引擎零影响）
  const se = selectedElement.value
  if (!se) return
  const real = deriveBranchRole(se.type, se.id)
  if (se.branchRole !== real) {
    proxy.$modal.msgWarning('分支语义由图形形状派生：请在「出口信息」中增减连线后再确认')
  }
}
/** 节点信息表格用：按节点行派生分支语义文案 */
function branchRoleLabel(row) {
  const role = deriveBranchRole(row.type, row.id)
  return { none: '一般', fork: '分支起点', join: '分支合并点' }[role] || '一般'
}
/** 只读节点信息表格用：出口数据源可显式传入（该表格读 flowExits，非 graphicExits） */
function branchRoleLabelFor(type, nodeId, exits) {
  const list = exits || []
  let out = 0, inn = 0
  list.forEach(e => {
    if (e.sourceId === nodeId) out++
    if (e.targetId === nodeId) inn++
  })
  const role = (isGatewayType(type) || out >= 2) ? 'fork' : (inn >= 2 ? 'join' : 'none')
  return { none: '一般', fork: '分支起点', join: '分支合并点' }[role] || '一般'
}

// ===== 节点字段权限（存节点扩展属性 flowable:fieldPerms，随 BPMN XML 保存部署） =====
const fieldPermsOpen = ref(false)
const fieldPermsLoading = ref(false)
const fieldPermsNodeName = ref('')
const fieldPermsRows = ref([])

function parseFieldPerms(bo) {
  const raw = (bo.get && bo.get('flowable:fieldPerms')) || bo.fieldPerms || ''
  if (!raw) return {}
  try { return JSON.parse(raw) || {} } catch (e) { return {} }
}
const fieldPermsSummary = computed(() => {
  if (!selectedElement.value || !selectedElement.value.isUserTask) return ''
  const perms = selectedElement.value.fieldPerms || {}
  const keys = Object.keys(perms)
  if (!keys.length) return '未配置：默认全部只读展示'
  const edit = keys.filter(k => perms[k] === 'edit').length
  const hidden = keys.filter(k => perms[k] === 'hidden').length
  return `可编辑 ${edit} / 只读 ${keys.length - edit - hidden} / 隐藏 ${hidden}`
})

async function openFieldPerms() {
  if (!selectedEl) return
  fieldPermsNodeName.value = selectedElement.value.name || selectedElement.value.id
  const perms = parseFieldPerms(selectedEl.businessObject)
  copyPermFrom.value = ''
  fieldPermsOpen.value = true
  fieldPermsLoading.value = true
  try {
    // 模型绑定的表单字段（模型需先关联表单）
    const formId = designModel.value.formId
    if (!formId) { fieldPermsRows.value = []; return }
    const res = await request({ url: '/flowable/form/' + formId, method: 'get' })
    const f = res.data || {}
    let fields = []
    try { fields = JSON.parse(f.fieldsJson || '[]') } catch (e) {}
    fieldPermsRows.value = fields.map(fd => ({ label: fd.label, field: fd.field, perm: perms[fd.field] || 'readonly' }))
  } catch (e) {
    fieldPermsRows.value = []
  } finally {
    fieldPermsLoading.value = false
  }
}

// ===== 复制其他节点权限 =====
const copyPermFrom = ref('')

/** 画布上其他已配置字段权限的用户任务节点 */
const otherPermNodes = computed(() => {
  if (!bpmnModeler) return []
  try {
    const canvas = bpmnModeler.get('canvas')
    const root = canvas.getRootElement()
    const nodes = []
    for (const el of (root.children || [])) {
      if (el.type !== 'bpmn:UserTask' || el.id === selectedElement.value?.id) continue
      const perms = parseFieldPerms(el.businessObject)
      const keys = Object.keys(perms)
      if (!keys.length) continue
      const edit = keys.filter(k => perms[k] === 'edit').length
      const hidden = keys.filter(k => perms[k] === 'hidden').length
      nodes.push({ id: el.id, name: el.businessObject.name || el.id, perms, summary: `可编辑${edit}/只读${keys.length - edit - hidden}/隐藏${hidden}` })
    }
    return nodes
  } catch (e) { return [] }
})

function applyCopyPerms() {
  const src = otherPermNodes.value.find(n => n.id === copyPermFrom.value)
  if (!src || !fieldPermsRows.value.length) return
  for (const r of fieldPermsRows.value) {
    r.perm = src.perms[r.field] || 'readonly'
  }
  proxy.$modal.msgSuccess('已复制「' + src.name + '」的权限配置，可继续调整后确定')
}

function saveFieldPerms() {
  if (!selectedEl) { fieldPermsOpen.value = false; return }
  const perms = {}
  for (const r of fieldPermsRows.value) {
    if (r.perm !== 'readonly') perms[r.field] = r.perm  // 只读为默认态，仅存差异项，保持 XML 精简
  }
  const json = Object.keys(perms).length ? JSON.stringify(perms) : undefined
  modeling.updateModdleProperties(selectedEl, selectedEl.businessObject, { 'flowable:fieldPerms': json })
  // bpmn-js moddle 未知属性可能不序列化，兜底写入 extensionAttributes
  try {
    const bo = selectedEl.businessObject
    bo.$attrs['flowable:fieldPerms'] = json
  } catch (e) {}
  selectedElement.value.fieldPerms = perms
  fieldPermsOpen.value = false
  proxy.$modal.msgSuccess('字段权限已保存到节点，流程保存后生效')
}

function updateCondition() {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  const expr = (selectedElement.value.condition || '').trim()
  const m = currentModeling()
  if (expr) {
    const moddle = bpmnModeler.get('moddle')
    const cond = moddle.create('bpmn:FormalExpression', { body: expr })
    m.updateProperties(selectedEl, { conditionExpression: cond })
  } else {
    m.updateProperties(selectedEl, { conditionExpression: undefined })
  }
}

function clearCondition() {
  selectedElement.value.condition = ''
  updateCondition()
}

// ===== 工具栏命令 =====
function cmdUndo() { bpmnModeler.get('commandStack').undo() }
function cmdRedo() { bpmnModeler.get('commandStack').redo() }
function zoom(delta) { bpmnModeler.get('canvas').zoom(bpmnModeler.get('canvas').zoom() + delta) }
function zoomReset() { bpmnModeler.get('canvas').zoom('fit-viewport', 'auto') }
function deleteSelected() {
  const sel = bpmnModeler.get('selection').get()
  if (!sel.length) { proxy.$modal.msgWarning('请先在画布中选中要删除的节点/连线'); return }
  modeling.removeElements(sel)
}

/** 保存前校验：用户任务办理人配置、结束节点存在性 */
function validateDiagram() {
  const root = bpmnModeler.get('canvas').getRootElement()
  const issues = []
  let hasEnd = false
  ;(root.children || []).forEach(c => {
    if (c.type === 'bpmn:EndEvent') hasEnd = true
    if (c.type === 'bpmn:UserTask') {
      const bo = c.businessObject
      const a = (bo.get && bo.get('flowable:assignee')) || ''
      const g = (bo.get && bo.get('flowable:candidateGroups')) || ''
      if (!a && !g && !bo.loopCharacteristics) {
        issues.push('「' + (bo.name || bo.id) + '」未配置办理人或候选组')
      }
    }
  })
  if (!hasEnd) issues.push('流程缺少结束节点')
  return issues
}

function toggleMultiInstance(val) {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  if (val) {
    // 开启会签：并行多实例
    const moddle = bpmnModeler.get('moddle')
    const loop = moddle.create('bpmn:MultiInstanceLoopCharacteristics', {
      isSequential: false,
      completionCondition: '${nrOfCompletedInstances >= nrOfInstances}'
    })
    modeling.updateModdleProperties(selectedEl, bo, { loopCharacteristics: loop })
  } else {
    modeling.updateModdleProperties(selectedEl, bo, { loopCharacteristics: undefined })
  }
}

async function handleDesign(row) {
  const res = await getModel(row.modelId)
  designModel.value = {
    ...res.data,
    deployed: res.data.deployed || row.deployed,
    version: res.data.version || row.version,
    deployMode: res.data.deployMode || 'new',
    locked: !!res.data.locked,
    helpDoc: res.data.helpDoc || '',
    orderNum: res.data.orderNum != null ? res.data.orderNum : 0
  }
  loadFormOptions()
  loadDesignVersions()
  selectedElement.value = null
  designTab.value = 'flow'
  designOpen.value = true
}

// ===== 泛微式版本管理（设计器内版本标签行 + 版本编辑弹框） =====
const designVersions = ref([])
const designVersionSelected = ref(null)
/** 表单管理二级页签：基本信息/编辑字段 */
const formTab = ref('basic')
/** 组件内保存字段设计后刷新表单选项（字段数等回显同步） */
function onFormFieldsSaved() {
  loadFormOptions()
  loadDesignVersions()
}

// ===== 流转设置二级页签：图形编辑/节点信息/出口信息（表格与画布互通，参照泛微节点信息） =====
const flowTab = ref('graph')
const flowNodes = ref([])
const flowExits = ref([])
const NODE_TYPE_LABELS = {
  'bpmn:StartEvent': '开始', 'bpmn:EndEvent': '结束', 'bpmn:UserTask': '审批节点',
  'bpmn:ServiceTask': '服务节点', 'bpmn:ExclusiveGateway': '排他网关',
  'bpmn:ParallelGateway': '并行网关', 'bpmn:InclusiveGateway': '包容网关', 'bpmn:Task': '任务节点'
}

// ===== 业务语义层（泛微式简化）：界面只暴露 4 类节点，底层仍写标准 BPMN 元素 =====
/** 界面节点类型（仅用于展示与新建引导，不影响 XML 结构） */
const SEMANTIC_NODE_LABELS = {
  'bpmn:UserTask': '审批节点', 'bpmn:ExclusiveGateway': '分支节点', 'bpmn:ParallelGateway': '分支节点',
  'bpmn:StartEvent': '发起节点', 'bpmn:Task': '发起节点', 'bpmn:EndEvent': '结束节点'
}
/** 节点语义标签：审批/发起/结束/分支；无法识别的类型（如历史脏数据 label）兜底为「未识别」 */
function semanticNodeLabel(type) {
  if (!type) return '未识别'
  if (SEMANTIC_NODE_LABELS[type]) return SEMANTIC_NODE_LABELS[type]
  if (type.includes('Gateway')) return '分支节点'
  const t = type.replace('bpmn:', '')
  // 已知 BPMN 元素但不在业务语义内（服务节点/通用任务等）→ 归入未识别，不再作为业务节点引导
  return NODE_TYPE_LABELS[type] ? t : '未识别'
}
/** 是否网关类元素（分支/汇聚） */
function isGatewayType(type) { return !!type && type.includes('Gateway') }

/** 从 BPMN 模型解析节点/连线清单（节点信息/出口信息表格数据源） */
function refreshFlowModelData() {
  if (!bpmnModeler) return
  try {
    const registry = bpmnModeler.get('elementRegistry')
    flowNodes.value = registry
      .filter(el => el.businessObject && el.businessObject.$instanceOf && el.businessObject.$instanceOf('bpmn:FlowNode'))
      .map(el => {
        const bo = el.businessObject
        return {
          id: bo.id,
          name: bo.name || '',
          type: el.type,
          typeLabel: semanticNodeLabel(el.type),
          isUserTask: el.type === 'bpmn:UserTask',
          assignee: (bo.get && bo.get('flowable:assignee')) || bo.assignee || '',
          candidateGroups: (bo.get && bo.get('flowable:candidateGroups')) || bo.candidateGroups || '',
          formKey: (bo.get && bo.get('flowable:formKey')) || bo.formKey || ''
        }
      })
    flowExits.value = registry
      .filter(el => el.type === 'bpmn:SequenceFlow')
      .map(el => {
        const bo = el.businessObject
        return {
          id: bo.id,
          name: bo.name || '',
          sourceId: bo.sourceRef ? bo.sourceRef.id : '',
          sourceName: (bo.sourceRef && bo.sourceRef.name) || '',
          targetId: bo.targetRef ? bo.targetRef.id : '',
          targetName: (bo.targetRef && bo.targetRef.name) || '',
          condition: bo.conditionExpression ? bo.conditionExpression.body || '' : ''
        }
      })
  } catch (e) { flowNodes.value = []; flowExits.value = [] }
}
watch(flowTab, tab => { if (tab !== 'graph') refreshFlowModelData() })

/** 表格编辑：节点属性（名称等）实时写入 BPMN（编辑弹窗的独立实例） */
function updateNodeProp(row, key, value) {
  if (!graphicModeler) return
  const el = graphicModeler.get('elementRegistry').get(row.id)
  if (el) graphicModeler.get('modeling').updateProperties(el, { [key]: value })
  refreshGraphicData()
}

/** 表格编辑：节点办理人（编辑弹窗的独立实例） */
function updateNodeAssignee(row) {
  if (!graphicModeler) return
  const el = graphicModeler.get('elementRegistry').get(row.id)
  if (el) graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, { 'flowable:assignee': row.assignee || undefined })
  refreshGraphicData()
}

/** 表格编辑：节点候选组（编辑弹窗的独立实例） */
function updateNodeCandidate(row) {
  if (!graphicModeler) return
  const el = graphicModeler.get('elementRegistry').get(row.id)
  if (el) graphicModeler.get('modeling').updateModdleProperties(el, el.businessObject, { 'flowable:candidateGroups': row.candidateGroups || undefined })
  refreshGraphicData()
}

/** 表格编辑：连线分支条件（编辑弹窗的独立实例，与画布右侧属性面板同一写法） */
function updateExitCondition(row) {
  if (!graphicModeler) return
  const el = graphicModeler.get('elementRegistry').get(row.id)
  if (!el) return
  const expr = (row.condition || '').trim()
  const m = graphicModeler.get('modeling')
  if (expr) {
    const moddle = graphicModeler.get('moddle')
    const cond = moddle.create('bpmn:FormalExpression', { body: expr })
    m.updateProperties(el, { conditionExpression: cond })
  } else {
    m.updateProperties(el, { conditionExpression: undefined })
  }
  refreshGraphicData()
}

/** 编辑弹窗节点/出口清单（从独立实例解析） */
function refreshGraphicData() {
  if (!graphicModeler) { graphicNodes.value = []; graphicExits.value = []; return }
  try {
    const registry = graphicModeler.get('elementRegistry')
    graphicNodes.value = registry
      .filter(el => el.businessObject && el.businessObject.$instanceOf && el.businessObject.$instanceOf('bpmn:FlowNode'))
      .map(el => {
        const bo = el.businessObject
        return {
          id: bo.id,
          name: bo.name || '',
          type: el.type,
          typeLabel: semanticNodeLabel(el.type),
          isUserTask: el.type === 'bpmn:UserTask',
          assignee: (bo.get && bo.get('flowable:assignee')) || bo.assignee || '',
          candidateGroups: (bo.get && bo.get('flowable:candidateGroups')) || bo.candidateGroups || '',
          formKey: (bo.get && bo.get('flowable:formKey')) || bo.formKey || '',
          operatorGroups: parseOperatorGroups(bo),
          multiInstance: !!bo.loopCharacteristics
        }
      })
    graphicExits.value = registry
      .filter(el => el.type === 'bpmn:SequenceFlow')
      .map(el => {
        const bo = el.businessObject
        let meta = {}
        try { meta = JSON.parse((bo.get && bo.get('flowable:exitMeta')) || bo.$attrs && bo.$attrs['flowable:exitMeta'] || '{}') || {} } catch (e) {}
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
          genNo: !!meta.genNo
        }
      })
  } catch (e) { graphicNodes.value = []; graphicExits.value = [] }
}

/** 出口信息：按当前节点过滤（泛微式「请选择当前节点」） */
const exitFilterNodeId = ref('')
const filteredGraphicExits = computed(() => {
  if (!exitFilterNodeId.value) return graphicExits.value
  return graphicExits.value.filter(e => e.sourceId === exitFilterNodeId.value)
})
function onExitFilterChange() {
  graphicSelectedLine.value = null
}

/** 出口元数据（是否退回/附加规则/生成编号）写回 BPMN 扩展属性 flowable:exitMeta（同 fieldPerms 模式） */
function updateExitMeta(row, key, val) {
  if (!graphicModeler) return
  const el = graphicModeler.get('elementRegistry').get(row.id)
  if (!el) return
  let meta = {}
  try { meta = JSON.parse((el.businessObject.get && el.businessObject.get('flowable:exitMeta')) || '{}') || {} } catch (e) {}
  meta[key] = val
  const json = (meta.isReturn || meta.extraRule || meta.genNo) ? JSON.stringify(meta) : undefined
  try {
    el.businessObject.$attrs['flowable:exitMeta'] = json
  } catch (e) {}
  row[key] = key === 'isReturn' || key === 'genNo' ? !!val : (val || '')
  applyWmCanvasMarkers(graphicModeler)
  applyWmReturnRoutes(graphicModeler)
  proxy.$modal.msgSuccess(key === 'isReturn' ? '已标记为退回出口，点"保存"生效' : '出口设置已更新，点"保存"生效')
}

/** 同源出口顺序调整（上移/下移）：顺序即排他/包容网关判断优先级（写回 BPMN flowElements 顺序，保存后生效） */
function canMoveExit(row, dir) {
  if (!row || !row.sourceId) return false
  const siblings = graphicExits.value.filter(e => e.sourceId === row.sourceId)
  if (siblings.length < 2) return false
  const idx = siblings.findIndex(e => e.id === row.id)
  return dir === -1 ? idx > 0 : idx < siblings.length - 1 && idx !== -1
}
function moveExitOrder(row, dir) {
  if (!graphicModeler || !canMoveExit(row, dir)) return
  const el = graphicModeler.get('elementRegistry').get(row.id)
  const root = graphicModeler.get('canvas').getRootElement()
  const flowElements = root.businessObject.flowElements || []
  const siblings = flowElements.filter(f => f.$type === 'bpmn:SequenceFlow' && f.sourceRef && f.sourceRef.id === row.sourceId)
  const idx = siblings.findIndex(f => f.id === row.id)
  if (idx === -1) return
  const swapWith = dir === -1 ? siblings[idx - 1] : siblings[idx + 1]
  if (!swapWith) return
  const i1 = flowElements.indexOf(el.businessObject)
  const i2 = flowElements.indexOf(swapWith)
  if (i1 === -1 || i2 === -1) return
  flowElements[i1] = swapWith
  flowElements[i2] = el.businessObject
  // 直接改写 flowElements 顺序（不进撤销栈）：仅影响 XML 输出顺序与网关判断优先级
  refreshGraphicData()
  proxy.$modal.msgSuccess('出口顺序已调整，点"保存"生效')
}

// ===== 出口拖动排序（泛微式拖动手柄，sortablejs + 同源出口顺序写回 BPMN） =====
const exitTableRef = ref(null)
let exitDragSortable = null

/** 拖拽结束：oldIndex/newIndex 映射过滤后行数据，重排同源出口在 BPMN flowElements 中的顺序 */
function onExitDragEnd(evt) {
  const fromRow = filteredGraphicExits.value[evt.oldIndex]
  const toRow = filteredGraphicExits.value[evt.newIndex]
  if (!fromRow || !toRow || fromRow.id === toRow.id) return
  if (fromRow.sourceId !== toRow.sourceId) {
    proxy.$modal.msgWarning('仅同源节点的出口之间可拖动排序，已还原')
    refreshGraphicData()
    return
  }
  if (!graphicModeler) return
  const el = graphicModeler.get('elementRegistry').get(fromRow.id)
  const target = graphicModeler.get('elementRegistry').get(toRow.id)
  const root = graphicModeler.get('canvas').getRootElement()
  const flowElements = root.businessObject.flowElements || []
  const siblings = flowElements.filter(f => f.$type === 'bpmn:SequenceFlow' && f.sourceRef && f.sourceRef.id === fromRow.sourceId)
  const i1 = siblings.findIndex(f => f.id === fromRow.id)
  const i2 = siblings.findIndex(f => f.id === toRow.id)
  if (i1 === -1 || i2 === -1) return
  // 重排：将 from 移除后按目标位置重新插入
  siblings.splice(i1, 1)
  const targetIdx = siblings.findIndex(f => f.id === toRow.id)
  siblings.splice(targetIdx, 0, el.businessObject)
  // 写回 flowElements：按新顺序替换同源兄弟在数组中的位置
  let cursor = 0
  for (let i = 0; i < flowElements.length; i++) {
    const f = flowElements[i]
    if (f.$type === 'bpmn:SequenceFlow' && f.sourceRef && f.sourceRef.id === fromRow.sourceId) {
      flowElements[i] = siblings[cursor++]
    }
  }
  // 直接改写 flowElements 顺序（不进撤销栈）：仅影响 XML 输出顺序与网关判断优先级
  refreshGraphicData()
  proxy.$modal.msgSuccess('出口顺序已调整，点"保存"生效')
}

/** exits 页签激活时初始化表格行拖拽（仅 exits 页签的表格 tbody）；watch 注册在 graphicTab 定义之后（见文件尾部） */
function initExitDragSort() {
  if (exitDragSortable) { exitDragSortable.destroy(); exitDragSortable = null }
  if (!exitTableRef.value) return
  const tbody = exitTableRef.value.$el.querySelector('.el-table__body-wrapper tbody')
  if (!tbody) return
  exitDragSortable = Sortable.create(tbody, {
    handle: '.drag-handle',
    animation: 150,
    onEnd: onExitDragEnd
  })
}

// ===== 出口条件规则弹窗（参照泛微出口条件设置：多规则 + AND/OR 组合为 JUEL 表达式） =====
const exitRulesOpen = ref(false)
const exitRulesRow = ref(null)
const exitRulesCopyFrom = ref('')
const exitRulesForm = ref({ join: 'AND', rows: [] })
const RULE_OPS = [
  { value: 'eq', label: '等于 ==', expr: '{v} == {f}' },
  { value: 'neq', label: '不等于 !=', expr: '{v} != {f}' },
  { value: 'gt', label: '大于 >', expr: '{f} > {v}' },
  { value: 'gte', label: '大于等于 >=', expr: '{f} >= {v}' },
  { value: 'lt', label: '小于 <', expr: '{f} < {v}' },
  { value: 'lte', label: '小于等于 <=', expr: '{f} <= {v}' },
  { value: 'in', label: '属于（多值）', expr: null },
  { value: 'notin', label: '不属于（多值）', expr: null }
]

/** 单条规则 → JUEL 片段 */
function ruleToExpr(r) {
  if (!r || !r.field) return ''
  const f = '${' + r.field + '}'
  const op = RULE_OPS.find(o => o.value === r.op) || RULE_OPS[0]
  const raw = (r.value || '').trim()
  if (!raw) return ''
  const isNum = /^-?\d+(\.\d+)?$/.test(raw)
  if (op.value === 'in' || op.value === 'notin') {
    const vals = raw.split(/[,，]/).map(s => s.trim()).filter(Boolean)
    if (!vals.length) return ''
    const test = op.value === 'in' ? vals : undefined
    if (op.value === 'in') {
      // 变量在值集合中：任一相等即通过
      return vals.map(v => (isNum || /^-?\d+$/.test(v) ? `${f} == ${v}` : `${f} == '${v}'`)).join(' || ')
    }
    return vals.map(v => (isNum || /^-?\d+$/.test(v) ? `${f} != ${v}` : `${f} != '${v}'`)).join(' && ')
  }
  const val = isNum ? raw : `'${raw}'`
  // 大小比较类按 泛微口径 值放右侧
  if (['gt', 'gte', 'lt', 'lte'].includes(op.value)) return `${f} ${op.expr.split(' ')[1]} ${val}`
  return `${f} ${op.expr.split(' ')[1]} ${val}`
}

/** 多规则按 AND/OR 拼接为最终 JUEL 表达式 */
const exitRulesExpr = computed(() => {
  const parts = (exitRulesForm.value.rows || []).map(ruleToExpr).filter(Boolean)
  if (!parts.length) return ''
  if (parts.length === 1) return parts[0]
  const join = exitRulesForm.value.join === 'OR' ? ' || ' : ' && '
  return parts.map(p => (p.includes(' && ') || p.includes(' || ') ? `(${p})` : p)).join(join)
})

/** 打开条件规则弹窗：从现有 conditionExpression 反解（简单表达式拆分为单规则） */
function openExitRules(row) {
  exitRulesRow.value = row
  exitRulesCopyFrom.value = ''
  const cond = (row.condition || '').trim()
  if (!cond) {
    exitRulesForm.value = { join: 'AND', rows: [{ field: '', op: 'eq', value: '' }] }
  } else {
    // 反解：提取 ${var} 与比较/属于结构；复杂表达式保留为只读单行
    const rows = []
    const re = /\$\{(\w+)\}\s*(==|!=|>=|<=|>|<)\s*'([^']*)'|\$\{(\w+)\}\s*(==|!=|>=|<=|>|<)\s*(-?\d+(?:\.\d+)?)/g
    let m
    while ((m = re.exec(cond)) !== null) {
      const field = m[1] || m[4]
      const opSym = m[2] || m[5]
      const val = m[3] != null ? m[3] : m[6]
      const opMap = { '==': 'eq', '!=': 'neq', '>': 'gt', '>=': 'gte', '<': 'lt', '<=': 'lte' }
      rows.push({ field, op: opMap[opSym] || 'eq', value: val })
    }
    if (!rows.length) {
      rows.push({ field: '', op: 'eq', value: '' })
    }
    exitRulesForm.value = { join: cond.includes('||') ? 'OR' : 'AND', rows }
  }
  exitRulesOpen.value = true
}

function addRuleRow() {
  exitRulesForm.value.rows.push({ field: '', op: 'eq', value: '' })
}
function removeRuleRow() {
  if (exitRulesForm.value.rows.length > 1) exitRulesForm.value.rows.pop()
}

/** 从其他出口引用规则（规则库复用：该模型内已配置条件的出口） */
const otherRuleExits = computed(() => {
  return graphicExits.value.filter(e => e.condition && (!exitRulesRow.value || e.id !== exitRulesRow.value.id))
})
function applyCopyExitRules() {
  const src = otherRuleExits.value.find(e => e.id === exitRulesCopyFrom.value)
  if (!src) return
  openExitRulesInto(src.condition)
  proxy.$modal.msgSuccess('已复制「' + (src.name || src.id) + '」的规则，可继续调整后保存')
}
function openExitRulesInto(cond) {
  const condTrim = (cond || '').trim()
  if (!condTrim) { exitRulesForm.value = { join: 'AND', rows: [{ field: '', op: 'eq', value: '' }] }; return }
  const rows = []
  const re = /\$\{(\w+)\}\s*(==|!=|>=|<=|>|<)\s*'([^']*)'|\$\{(\w+)\}\s*(==|!=|>=|<=|>|<)\s*(-?\d+(?:\.\d+)?)/g
  let m
  while ((m = re.exec(condTrim)) !== null) {
    const field = m[1] || m[4]
    const opSym = m[2] || m[5]
    const val = m[3] != null ? m[3] : m[6]
    const opMap = { '==': 'eq', '!=': 'neq', '>': 'gt', '>=': 'gte', '<': 'lt', '<=': 'lte' }
    rows.push({ field, op: opMap[opSym] || 'eq', value: val })
  }
  if (!rows.length) rows.push({ field: '', op: 'eq', value: '' })
  exitRulesForm.value = { join: condTrim.includes('||') ? 'OR' : 'AND', rows }
}

/** 保存：最终表达式写回出口条件（conditionExpression），同时刷新表格 */
function saveExitRules() {
  if (!exitRulesRow.value) return
  const expr = exitRulesExpr.value
  const row = graphicExits.value.find(e => e.id === exitRulesRow.value.id)
  if (row) {
    row.condition = expr
    updateExitCondition(row)
  }
  exitRulesOpen.value = false
  proxy.$modal.msgSuccess(expr ? '出口条件已保存，点"保存"生效' : '出口条件已清除，点"保存"生效')
}
function clearExitRules() {
  if (!exitRulesRow.value) return
  const row = graphicExits.value.find(e => e.id === exitRulesRow.value.id)
  if (row) {
    row.condition = ''
    updateExitCondition(row)
  }
  exitRulesOpen.value = false
  proxy.$modal.msgSuccess('出口条件已清除，点"保存"生效')
}

// ===== 节点编辑操作组弹窗（参照泛微「编辑操作组」：8 种操作者类型 + 多组 + 条件/批次/会签） =====
const OPERATOR_TYPES = [
  { value: 'user', label: '指定人' },
  { value: 'dept', label: '指定部门' },
  { value: 'subDept', label: '指定分部' },
  { value: 'role', label: '指定角色' },
  { value: 'post', label: '指定岗位' },
  { value: 'all', label: '所有人' },
  { value: 'creator', label: '流程创建人' },
  { value: 'nodeOp', label: '节点操作者' }
]
const opGroupsOpen = ref(false)
const opGroupsNodeName = ref('')
const opGroupsNodeId = ref('')
const opGroupsActiveNames = ref(['0'])
let opGroupsTargetRow = null

/** 解析节点操作组（BPMN 扩展属性 flowable:operatorGroups，同 exitMeta 模式） */
function parseOperatorGroups(bo) {
  let raw = ''
  try { raw = (bo.get && bo.get('flowable:operatorGroups')) || bo.$attrs && bo.$attrs['flowable:operatorGroups'] || '' } catch (e) {}
  if (!raw) return []
  try { return JSON.parse(raw) || [] } catch (e) { return [] }
}

/** 节点信息表格操作组摘要（如「2组·财务确认」） */
function operatorGroupSummary(row) {
  if (!row || !row.isUserTask) return '—'
  const groups = row.operatorGroups || []
  if (!groups.length) return '未设置'
  const names = groups.map(g => g.name || (g.refName || OPERATOR_TYPES.find(t => t.value === g.type)?.label || '')).filter(Boolean)
  return groups.length + '组' + (names.length ? '·' + names.slice(0, 2).join('/') : '')
}
function opGroupTitle(g) {
  const t = OPERATOR_TYPES.find(x => x.value === g.type)
  const ref = g.refName ? '：' + g.refName : ''
  const counter = { single: '单人', or: '或签', and: '会签' }[g.counterSign] || ''
  return ((t && t.label) || '') + ref + (counter ? ' · ' + counter : '')
}
function opTypeLabel(t) {
  const x = OPERATOR_TYPES.find(v => v.value === t)
  return (x && x.label) || ''
}
function opUserLabel(id) {
  const u = userOptions.value.find(x => String(x.userId) === String(id))
  return u ? (u.nickName + '（' + u.userName + '）') : ''
}
function opRoleLabel(id) {
  const r = roleOptions.value.find(x => String(x.roleId) === String(id))
  return r ? (r.roleName + '（' + r.roleKey + '）') : ''
}
function opDeptLabel(id) {
  const find = list => {
    for (const n of (list || [])) {
      if (String(n.id) === String(id)) return n.label
      const r = find(n.children)
      if (r) return r
    }
    return null
  }
  return find(deptTreeOptions.value) || ''
}
function opPostLabel(id) {
  const p = postOptions.value.find(x => String(x.postId) === String(id))
  return p ? (p.postName + '（' + p.postCode + '）') : ''
}

/** 部门树/岗位选项（操作组指定部门/分部/岗位选择器） */
const deptTreeOptions = ref([])
const postOptions = ref([])
function loadOperatorRefOptions() {
  request({ url: '/system/user/deptTree', method: 'get' })
    .then(res => { deptTreeOptions.value = res.data || [] })
    .catch(e => { proxy.$modal.msgWarning('部门树加载失败，部门选择器将为空：' + (e?.message || e)) })
  request({ url: '/system/post/list', method: 'get', params: { pageNum: 1, pageSize: 200 } })
    .then(res => { postOptions.value = res.rows || [] })
    .catch(e => { proxy.$modal.msgWarning('岗位列表加载失败，岗位选择器将为空：' + (e?.message || e)) })
}

/** 打开操作组弹窗：从 BPMN 扩展属性回填；无数据时按现有 assignee/candidateGroups 初始化一组 */
function openOperatorGroups(row) {
  if (!row || !row.isUserTask) return
  opGroupsTargetRow = row
  opGroupsNodeId.value = row.id
  opGroupsNodeName.value = row.name || row.id
  let groups = row.operatorGroups || []
  if (!groups.length) {
    if (row.assignee) {
      groups = [{ name: undefined, type: 'user', userId: row.assignee, refName: opUserLabel(row.assignee), counterSign: 'single', condition: '', batch: 0 }]
    } else if (row.candidateGroups) {
      groups = [{ name: undefined, type: 'role', roleId: row.candidateGroups, refName: opRoleLabel(row.candidateGroups), counterSign: 'single', condition: '', batch: 0 }]
    } else {
      groups = [{ name: undefined, type: 'user', userId: undefined, refName: '', counterSign: 'single', condition: '', batch: 0 }]
    }
  }
  opGroupsForm.value = groups
  opGroupsActiveNames.value = ['0']
  loadOperatorRefOptions()
  opGroupsOpen.value = true
}
function addOperatorGroup() {
  opGroupsForm.value.push({ name: undefined, type: 'user', userId: undefined, roleId: undefined, refName: '', counterSign: 'single', condition: '', batch: 0 })
  opGroupsActiveNames.value = [String(opGroupsForm.value.length - 1)]
}
function removeOperatorGroup(gi) {
  if (!opGroupsForm.value.length) return
  opGroupsForm.value.splice(gi, 1)
}

/** 保存操作组：写回 BPMN 扩展属性 + 同步首个指定人/角色到节点办理人 + 会签联动多实例 */
function saveOperatorGroups() {
  if (!graphicModeler || !opGroupsNodeId.value) return
  const el = graphicModeler.get('elementRegistry').get(opGroupsNodeId.value)
  if (!el) { opGroupsOpen.value = false; return }
  const groups = (opGroupsForm.value || []).filter(g =>
    g.type === 'user' ? g.userId
      : g.type === 'role' ? g.roleId
      : (g.type === 'dept' || g.type === 'subDept' || g.type === 'post') ? g.refId
      : true)
  const json = groups.length ? JSON.stringify(groups) : undefined
  const bo = el.businessObject
  try { bo.$attrs['flowable:operatorGroups'] = json } catch (e) {}
  // 同步节点基础办理人/候选组（兼容运行态既有链路）；部门/分部/岗位运行态由 OperatorGroupTaskListener 解析候选用户
  const first = groups[0]
  const m = graphicModeler.get('modeling')
  m.updateModdleProperties(el, bo, {
    'flowable:assignee': first && first.type === 'user' ? (first.userId || undefined) : undefined,
    'flowable:candidateGroups': first && first.type === 'role' ? (first.roleId || undefined) : undefined
  })
  // 会签/或签联动多实例（会签=并行、或签=串行、单人=无）
  const counter = first && first.counterSign
  if (counter === 'and' || counter === 'or') {
    const moddle = graphicModeler.get('moddle')
    const loop = moddle.create('bpmn:MultiInstanceLoopCharacteristics', {
      isSequential: counter === 'or',
      completionCondition: counter === 'and' ? '${nrOfCompletedInstances >= nrOfInstances}' : undefined
    })
    m.updateModdleProperties(el, bo, { loopCharacteristics: loop })
  } else {
    m.updateModdleProperties(el, bo, { loopCharacteristics: undefined })
  }
  // 刷新表格行数据
  if (opGroupsTargetRow) {
    opGroupsTargetRow.operatorGroups = groups
    opGroupsTargetRow.assignee = first && first.type === 'user' ? (first.userId || '') : ''
    opGroupsTargetRow.candidateGroups = first && first.type === 'role' ? (first.roleId || '') : ''
    opGroupsTargetRow.multiInstance = !!(counter === 'and' || counter === 'or')
  }
  opGroupsOpen.value = false
  proxy.$modal.msgSuccess('操作组已保存到节点，点"保存"生效')
}

function onGraphicNodeSelect(row) {
  graphicSelectedRow.value = row
  if (row && graphicModeler) {
    const el = graphicModeler.get('elementRegistry').get(row.id)
    if (el) graphicModeler.get('selection').select(el)
  }
}
function onGraphicLineSelect(row) {
  graphicSelectedLine.value = row
  if (row && graphicModeler) {
    const el = graphicModeler.get('elementRegistry').get(row.id)
    if (el) graphicModeler.get('selection').select(el)
  }
}

// 新增节点（在选中节点后追加）：界面仅 4 类业务语义，底层仍写标准 BPMN 元素
const ADD_NODE_TYPES = [
  { value: 'bpmn:UserTask', label: '审批节点' },
  { value: 'bpmn:ExclusiveGateway', label: '分支节点' },
  { value: 'bpmn:StartEvent', label: '发起节点' },
  { value: 'bpmn:EndEvent', label: '结束节点' }
]
// 分支方式：仅当节点类型为「分支节点」时需要选择（默认排他网关）
const ADD_BRANCH_MODES = [
  { value: 'bpmn:ExclusiveGateway', label: '排他分支（按出口条件选一条）' },
  { value: 'bpmn:ParallelGateway', label: '并行分支（全部出口同时激活）' }
]
function openAddNode() {
  if (!graphicSelectedRow.value) { proxy.$modal.msgWarning('请先在表格中选中一个节点（作为新节点的来源）'); return }
  addNodeForm.value = { type: 'bpmn:UserTask', branchMode: 'bpmn:ExclusiveGateway', name: undefined }
  addNodeOpen.value = true
}
function confirmAddNode() {
  if (!graphicModeler || !graphicSelectedRow.value) return
  const source = graphicModeler.get('elementRegistry').get(graphicSelectedRow.value.id)
  if (!source) { proxy.$modal.msgWarning('选中节点已不存在'); return }
  // 「分支节点」按所选分支方式落成标准网关元素；其余类型直接用语义类型
  const realType = addNodeForm.value.type === 'bpmn:ExclusiveGateway'
    ? (addNodeForm.value.branchMode || 'bpmn:ExclusiveGateway')
    : addNodeForm.value.type
  graphicModeler.get('modeling').appendShape(source, { type: realType, name: addNodeForm.value.name || undefined })
  addNodeOpen.value = false
  refreshGraphicData()
}
function removeGraphicNode() {
  if (!graphicModeler || !graphicSelectedRow.value) return
  const el = graphicModeler.get('elementRegistry').get(graphicSelectedRow.value.id)
  if (el) graphicModeler.get('modeling').removeElements([el])
  graphicSelectedRow.value = null
  refreshGraphicData()
}

// 新增出口（来源→目标连线）
function openAddLine() {
  addLineForm.value = { sourceId: undefined, targetId: undefined, name: undefined }
  addLineOpen.value = true
}
function confirmAddLine() {
  if (!graphicModeler || !addLineForm.value.sourceId || !addLineForm.value.targetId) { proxy.$modal.msgWarning('请选择来源/目标节点'); return }
  if (addLineForm.value.sourceId === addLineForm.value.targetId) { proxy.$modal.msgWarning('来源/目标节点不能相同'); return }
  const src = graphicModeler.get('elementRegistry').get(addLineForm.value.sourceId)
  const dst = graphicModeler.get('elementRegistry').get(addLineForm.value.targetId)
  if (!src || !dst) { proxy.$modal.msgWarning('来源/目标节点不存在'); return }
  graphicModeler.get('modeling').connect(src, dst, { type: 'bpmn:SequenceFlow', name: addLineForm.value.name || undefined })
  addLineOpen.value = false
  refreshGraphicData()
}
function removeGraphicLine() {
  if (!graphicModeler || !graphicSelectedLine.value) return
  const el = graphicModeler.get('elementRegistry').get(graphicSelectedLine.value.id)
  if (el) graphicModeler.get('modeling').removeElements([el])
  graphicSelectedLine.value = null
  refreshGraphicData()
}

// ===== 图形化编辑独立全屏弹窗（泛微式：点击"编辑"进入独立编辑页面，关闭返回列表） =====
const graphicOpen = ref(false)
const graphicCanvasRef = ref(null)
const paletteHostRef = ref(null)
// 泛微式节点信息面板：右侧弹出，可收缩（点节点自动展开，手柄 ‹/› 切换）
const propsCollapsed = ref(false)
const graphicFileRef = ref(null)
let graphicModeler = null
// 编辑弹窗：三个二级页签与节点/出口清单
const graphicTab = ref('graph')
const graphicNodes = ref([])
const graphicExits = ref([])
const graphicSelectedRow = ref(null)
const graphicSelectedLine = ref(null)
const addNodeOpen = ref(false)
const addNodeForm = ref({ type: 'bpmn:UserTask', branchMode: 'bpmn:ExclusiveGateway', name: undefined })
const addLineOpen = ref(false)
const addLineForm = ref({ sourceId: undefined, targetId: undefined, name: undefined })

/** 打开独立编辑页面（复制当前模型到独立实例） */
function openGraphicEditor() {
  if (!designModel.value.modelKey) return
  graphicOpen.value = true
}

/** 独立编辑页初始化：独立 bpmn-js 实例 + 导入当前模型 */
async function onGraphicOpened() {
  await import('bpmn-js/dist/assets/diagram-js.css')
  await import('bpmn-js/dist/assets/bpmn-js.css')
  await import('bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css')
  const { default: BpmnModeler } = await import('bpmn-js/lib/Modeler.js')
  const { default: WmReturnLayouter, isReturnExit } = await import('./WmReturnLayouter.js')
  wmReturnExitChecker = isReturnExit
  graphicModeler = new BpmnModeler({
    container: graphicCanvasRef.value,
    additionalModules: [bpmnChineseTranslate, { layouter: ['type', WmReturnLayouter] }]
  })
  // 选中事件 → 属性面板互通（独立实例的选中元素共享同一面板状态）
  const gEventBus = graphicModeler.get('eventBus')
  gEventBus.on('selection.changed', e => {
    selectedElSource = 'graphic'
    const el = e.newSelection[0]
    if (el) { loadSelectedElement(el); propsCollapsed.value = false }
    else { selectedEl = null; selectedElement.value = null }
  })
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })
    await graphicModeler.importXML(xml)
    graphicModeler.get('canvas').zoom('fit-viewport', 'auto')
    // 布局稳定后二次适配：避免弹窗打开瞬间容器尺寸未定导致内容贴顶、下方大片空白
    setTimeout(() => { try { graphicModeler.get('canvas').zoom('fit-viewport', 'auto') } catch (e) {} }, 150)
    applyWmCanvasMarkers(graphicModeler)
    applyWmReturnRoutes(graphicModeler)
    // 泛微式：palette 移出画布容器，放到画布上方独立工具条区域（占位布局，不遮挡画布内容与视口计算）
    await nextTick()
    const paletteHostEl = paletteHostRef.value
    const paletteEl = graphicCanvasRef.value && graphicCanvasRef.value.querySelector('.djs-palette')
    if (paletteHostEl && paletteEl) paletteHostEl.appendChild(paletteEl)
    refreshGraphicData()
  } catch (e) {
    proxy.$modal.msgError('加载流程图失败：' + (e.message || e))
  }
}

function graphicCmd(cmd) {
  if (!graphicModeler) return
  const stack = graphicModeler.get('commandStack')
  if (cmd === 'undo') stack.undo()
  else stack.redo()
}
function graphicZoom(delta) { if (graphicModeler) graphicModeler.get('canvas').zoom(graphicModeler.get('canvas').zoom() + delta) }
function graphicZoomReset() { if (graphicModeler) graphicModeler.get('canvas').zoom('fit-viewport', 'auto') }
function graphicDelete() {
  if (!graphicModeler) return
  const sel = graphicModeler.get('selection').get()
  if (sel && sel.length) graphicModeler.get('modeling').removeElements(sel)
}

/** 独立页面保存：同步回主画布 + 持久化到后端（与 saveDesigner 同一校验/锁定逻辑） */
async function graphicSave() {
  if (!graphicModeler || !designModel.value.modelId) return
  try {
    if (designModel.value.locked) {
      try {
        await ElMessageBox.confirm('该流程已锁定，确认仍要保存修改？', '流程锁定提醒',
          { confirmButtonText: '仍要保存', cancelButtonText: '返回修改', type: 'warning', draggable: true })
      } catch (e) { return }
    }
    applyWmReturnRoutes(graphicModeler)
    const { xml } = await graphicModeler.saveXML({ format: true })
    // 同步回设计器画布（主 modeler 重新导入同一份模型）
    if (bpmnModeler) {
      await bpmnModeler.importXML(xml)
      bpmnModeler.get('canvas').zoom('fit-viewport', 'auto')
    }
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
      bpmnXml: xml
    })
    proxy.$modal.msgSuccess('流程图已保存，已同步回设计器')
    getList()
  } catch (e) {
    proxy.$modal.msgError('保存失败：' + (e.message || e))
  }
}

/** 关闭返回列表（流转设置-图形编辑），未保存的改动不生效 */
function graphicClose() {
  graphicOpen.value = false
}

/** 独立页面导入/导出 XML */
function graphicImportXml() { graphicFileRef.value && graphicFileRef.value.click() }
async function onGraphicFileChange(e) {
  const file = e.target.files[0]
  if (!file || !graphicModeler) return
  try {
    const text = await file.text()
    await graphicModeler.importXML(text)
    graphicModeler.get('canvas').zoom('fit-viewport', 'auto')
    proxy.$modal.msgSuccess('XML 已导入，点"保存"生效')
  } catch (e) {
    proxy.$modal.msgError('XML 导入失败：' + (e.message || e))
  }
  e.target.value = ''
}
async function graphicExportXml() {
  if (!graphicModeler) return
  try {
    const { xml } = await graphicModeler.saveXML({ format: true })
    const blob = new Blob([xml], { type: 'application/xml' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = (designModel.value.modelKey || 'process') + '.bpmn20.xml'
    link.click()
    URL.revokeObjectURL(link.href)
  } catch (e) { proxy.$modal.msgError('导出失败') }
}
/** 当前活动版本号（基本信息页签与版本标签行共用同一数据口径） */
const designActiveVersion = computed(() => {
  const act = designVersions.value.find(v => v.activeFlag === '1')
  return act ? act.version : null
})
const versionOpen = ref(false)
const versionEdit = ref({ version: null, remark: '', setActive: false, versionActive: false, suspended: false })

/** 加载该模型全部已部署版本（供版本标签行展示） */
function loadDesignVersions() {
  const key = designModel.value.modelKey
  if (!key) { designVersions.value = []; return }
  listDefinition({ modelKey: key, pageNum: 1, pageSize: 50 }).then(res => {
    designVersions.value = res.rows || []
  }).catch(() => { designVersions.value = [] })
}

/** 点击版本标签 → 打开泛微式版本编辑弹框 */
function openVersionEditor(v) {
  designVersionSelected.value = v
  versionEdit.value = {
    version: v.version,
    remark: v.remark || '',
    setActive: v.activeFlag === '1',
    versionActive: v.activeFlag === '1',
    suspended: !!v.suspended,
    modelId: v.modelId
  }
  versionOpen.value = true
}

/** 保存版本编辑：先存备注，勾选时再设为活动版本 */
function submitVersionEdit() {
  const edit = versionEdit.value
  const modelId = edit.modelId
  updateDefinitionRemark(modelId, edit.remark || '').then(() => {
    if (edit.setActive) {
      return setActiveDefinition(modelId)
    }
    return null
  }).then(() => {
    proxy.$modal.msgSuccess('版本信息已更新')
    versionOpen.value = false
    loadDesignVersions()
  }).catch(e => { proxy.$modal.msgError('版本信息更新失败：' + (e?.message || e || '请稍后重试')) })
}

/** 版本挂起/激活（由流程定义页收敛而来，入口统一收在版本编辑弹框） */
const versionStateLoading = ref(false)
function handleVersionSuspend(suspend) {
  const edit = versionEdit.value
  const label = suspend ? '挂起' : '激活'
  let tip = `确认${label}版本 V${edit.version}？`
  if (suspend) {
    tip = `确认挂起版本 V${edit.version}？挂起后该版本不可发起新流程，运行中实例不受影响`
    if (edit.versionActive) {
      tip = `版本 V${edit.version} 是当前活动版本，挂起后新发起将无法使用该版本（需先激活其他版本或本版本）。确认挂起？`
    }
  }
  proxy.$modal.confirm(tip).then(() => {
    versionStateLoading.value = true
    return changeDefinitionState(edit.modelId, suspend)
  }).then(() => {
    versionStateLoading.value = false
    proxy.$modal.msgSuccess(`已${label}版本 V${edit.version}`)
    edit.suspended = suspend
    loadDesignVersions()
    getList()
  }).catch(() => { versionStateLoading.value = false })
}

// ===== 泛微式四页签：基本信息 / 表单管理 / 流转设置 / 高级设置 =====
const designTab = ref('flow')
/** 表单管理页签：当前关联表单详情（来自表单选项，兜底用模型详情字段） */
const boundFormInfo = computed(() => {
  const fid = designModel.value.formId
  if (!fid) return {}
  const f = formOptions.value.find(x => String(x.formId) === String(fid))
  if (f) {
    let fieldCount = null
    try {
      const arr = JSON.parse(f.fieldsJson || 'null')
      fieldCount = Array.isArray(arr) ? arr.length : null
    } catch (e) {}
    return { formName: f.formName, tableName: f.tableName, formDesc: f.formDesc, fieldCount }
  }
  return { formName: designModel.value.formName, tableName: designModel.value.formTableName, formDesc: null, fieldCount: designModel.value.formFieldCount }
})
/** 切回流转设置页签时重设画布视口（隐藏期间渲染尺寸会失效） */
function onDesignTabChange(tab) {
  if (tab === 'flow' && bpmnModeler) {
    proxy.$nextTick(() => {
      try { bpmnModeler.get('canvas').zoom('fit-viewport', 'auto') } catch (e) {}
    })
  }
}

async function saveDesigner() {
  try {
    // 流程锁定：锁定后保存需二次确认，防止误改已上线流程
    if (designModel.value.locked) {
      try {
        await ElMessageBox.confirm('该流程已锁定，确认仍要保存修改？', '流程锁定提醒',
          { confirmButtonText: '仍要保存', cancelButtonText: '返回修改', type: 'warning', draggable: true })
      } catch (e) { return }
    }
    const { xml } = await bpmnModeler.saveXML({ format: true })
    // 保存前校验
    const issues = validateDiagram()
    if (issues.length) {
      try {
        await ElMessageBox.confirm(
          h => h('div', null, [
            h('p', { style: 'margin-bottom:8px;font-weight:600' }, '存在 ' + issues.length + ' 个问题：'),
            h('ul', { style: 'padding-left:18px;margin:0;color:#e6a23c' }, issues.map(t => h('li', null, t))),
            h('p', { style: 'margin-top:10px;color:#909399;font-size:13px' }, '仍可保存，但该流程部署后可能无法正常发起。')
          ]),
          '保存前校验提醒',
          { confirmButtonText: '仍要保存', cancelButtonText: '返回修改', type: 'warning', draggable: true }
        )
      } catch (e) {
        return // 用户选择返回修改
      }
    }
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
      bpmnXml: xml
    })
    proxy.$modal.msgSuccess('流程设计已保存，可点击"部署"发布流程')
    getList()
  } catch (e) {
    proxy.$modal.msgError('保存失败：' + (e.message || e))
  }
}

async function exportXml() {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })
    const blob = new Blob([xml], { type: 'application/xml' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = (designModel.value.modelKey || 'process') + '.bpmn20.xml'
    link.click()
    URL.revokeObjectURL(link.href)
  } catch (e) {
    proxy.$modal.msgError('导出失败')
  }
}

function importLocalXml() { xmlFileRef.value.click() }

async function onXmlFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const text = await file.text()
  try {
    await bpmnModeler.importXML(text)
    proxy.$modal.msgSuccess('XML导入成功，记得点击"保存"')
  } catch (err) {
    proxy.$modal.msgError('XML解析失败：' + err.message)
  }
  e.target.value = ''
}

// ===== 查看流程图入口已移除（与设计弹窗图形编辑只读视图重复），只读查看走 设计→流转设置→图形编辑 =====

// ===== 部署方式弹窗（泛微式：存为新版 / 覆盖当前版本） =====
const deployOpen = ref(false)
const deployRow = ref(null)
const deployMode = ref('new')
const deployRemark = ref('')
const deploySetActive = ref(true)

function handleDeploy(row) {
  deployRow.value = row
  // 预选高级设置中的默认部署方式
  deployMode.value = row.deployMode === 'replace' ? 'replace' : 'new'
  deployRemark.value = ''
  deploySetActive.value = true
  deployOpen.value = true
}

function submitDeploy() {
  const row = deployRow.value
  if (!row) return
  const tip = deployMode.value === 'replace'
    ? '覆盖当前版本：部署后自动停用旧版本，运行中的流程继续走完，新发起使用新版本'
    : '存为新版本：新旧版本并存，新发起优先使用活动版本'
  deployModel(row.modelId, deployMode.value, deployRemark.value, deploySetActive.value).then(() => {
    proxy.$modal.msgSuccess('部署成功：' + tip)
    deployOpen.value = false
    getList()
  }).catch(e => { proxy.$modal.msgError('部署失败：' + (e?.message || e || '请检查流程图是否已保存')) })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除模型"' + row.modelName + '"？').then(() => delModel(row.modelId)).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}

getList()
onActivated(() => { getList() })
// exits 页签激活时初始化出口表格行拖拽（graphicTab 定义之后注册，避免 TDZ）
watch([graphicTab, filteredGraphicExits], ([tab]) => {
  if (tab === 'exits') nextTick(initExitDragSort)
})
</script>

<style lang="scss" scoped>
@use './flow-model.scss';

.field-perms-tip { font-size: 12px; color: #64748b; margin-bottom: 10px; line-height: 1.5; }
.field-perms-copybar { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.field-perms-copybar .fpc-label { font-size: 12px; color: #475569; flex: none; }
.field-perms-table { width: 100%; }
.field-perms-table .fp-field { font-style: normal; color: #94a3b8; font-size: 11px; margin-left: 6px; }
.field-perms-empty { text-align: center; color: #94a3b8; font-size: 13px; padding: 20px 0; }

/* 泛微式四页签：基本信息 / 表单管理 / 流转设置 / 高级设置 */
.designer-tabs { padding: 0 16px 16px; }
.designer-tabs :deep(.el-tab-pane) { padding-top: 14px; }
.design-basic-form { max-width: 660px; }

/* 泛微式版本标签行：V1/V2/... 点击弹版本编辑 */
.version-tab-row { display: flex; align-items: center; gap: 8px; padding: 10px 16px 0; flex-wrap: wrap; }
.version-row-label { display: inline-flex; align-items: center; gap: 4px; font-size: 13px; font-weight: 600; color: #475569; flex: none; }
.version-tab { display: inline-flex; align-items: center; gap: 4px; padding: 4px 12px; border: 1px solid #cbd5e1; border-radius: 14px; font-size: 12px; color: #475569; cursor: pointer; transition: all .15s; }
.version-tab:hover { border-color: #3b82f6; color: #1d4ed8; }
.version-tab.is-active { background: #eff6ff; border-color: #3b82f6; color: #1d4ed8; font-weight: 600; }
.version-tab .vt-badge { background: #10b981; color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 8px; }
.version-row-tip { font-size: 12px; color: #94a3b8; margin-left: 4px; }

/* 表单管理二级页签：基本信息/编辑字段 */
.form-sub-tabs { padding: 0 8px; }
.form-sub-tabs :deep(.el-tab-pane) { padding-top: 10px; }

/* 图形编辑独立工具栏（泛微式：按钮放画布上方） */
.graph-toolbar { display: flex; align-items: center; gap: 10px; padding: 8px 12px; border-bottom: 1px solid #e5e7eb; flex-wrap: wrap; }
.graph-toolbar .graph-tip { font-size: 12px; color: #94a3b8; margin-left: 4px; }

/* 图形化编辑弹窗：三个二级页签（图形编辑/节点信息/出口信息） */
.graphic-body { flex-direction: column; }
/* ===== 图形编辑全屏弹窗高度链（浏览器实测定位，非推断） =====
   实测环境 1280x720，打开该弹窗后逐层量取 getBoundingClientRect：
     .el-dialog__body        高 234px、display:block、overflow:visible
     .graphic-body           高 234px（被内容撑开，不是撑满）
     .graphic-editor-tabs    高 234px
     .el-tabs__content       高 194px、display:block
     .graphic-pane           高 194px
     .graphic-main           高 153px
     .designer-canvas        高 580px  ← 画布自身高度远大于其容器
     .djs-container          高 580px（网格底图在这里）
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
/* 泛微式：画布 + 右侧弹出节点信息面板（可收缩手柄 ‹/›） */
.graphic-main { flex: 1; display: flex; min-height: 0; }
.graphic-main .designer-canvas { flex: 1; min-width: 0; }
.props-handle {
  width: 16px; flex: none;
  background: #f1f5f9; border-left: 1px solid #e5e7eb;
  cursor: pointer; user-select: none;
  display: flex; align-items: center; justify-content: center;
  color: #64748b; font-size: 12px;
}
.props-handle span { writing-mode: vertical-lr; letter-spacing: 2px; }
.props-handle:hover { background: #e0f2fe; color: #1d4ed8; }
.graphic-main .designer-props { width: 320px; flex: none; }
.graphic-editor-tabs { flex: 1 1 auto; display: flex; flex-direction: column; min-height: 0; padding: 0 8px; }
.graphic-editor-tabs :deep(.el-tabs__header) { flex: none; margin-bottom: 0; }
.graphic-editor-tabs :deep(.el-tabs__content) { flex: 1 1 auto; min-height: 0; height: auto; overflow: hidden; }
.graphic-editor-tabs :deep(.el-tab-pane[name='graph'] .designer-body) { height: 100%; }

/* ===== 泛微式工具面板顶部化（palette 移出画布、占位水平横排：分组分隔 + hover 效果，不遮挡画布） ===== */
.palette-host { border-bottom: 1px solid #e5e7eb; background: #f8fafc; min-height: 40px; }
.palette-host:empty { display: none; }
.palette-host :deep(.djs-palette) {
  position: relative !important;
  left: 0 !important;
  top: 0 !important;
  width: 100% !important;
  border-radius: 0;
  border: none;
  box-shadow: none;
  background: transparent;
  padding: 2px 6px;
}
.palette-host :deep(.djs-palette .djs-palette-toggle) { display: none; }
.palette-host :deep(.djs-palette .djs-palette-entries) { display: flex; flex-wrap: wrap; align-items: center; }
.palette-host :deep(.djs-palette .djs-palette-entries .group) {
  display: flex; flex-wrap: wrap; align-items: center;
  padding: 0 8px;
  border-right: 1px dashed #d1d5db;
}
.palette-host :deep(.djs-palette .djs-palette-entries .group:last-child) { border-right: none; }
.palette-host :deep(.djs-palette .entry) {
  width: 34px; height: 32px; margin: 2px;
  box-sizing: border-box;
  border-radius: 4px;
  line-height: 32px;
}
.palette-host :deep(.djs-palette .entry:hover) { background: #e0f2fe; color: #1d4ed8; }
/* 画布网格背景（泛微式：浅灰网格线）+ 高度链补全（tab-pane/djs-container 撑满，避免下半纯白空白） */
.palette-host :deep(.djs-canvas) { outline: none; }
.designer-canvas :deep(.djs-container) { height: 100% !important; width: 100% !important; }
.designer-canvas .djs-container {
  background-color: #fff;
  background-image: linear-gradient(#f1f5f9 1px, transparent 1px), linear-gradient(90deg, #f1f5f9 1px, transparent 1px);
  background-size: 24px 24px;
}
.graphic-editor-tabs :deep(.el-tab-pane.graphic-pane) { height: 100%; display: flex; flex-direction: column; overflow: hidden; }
.graphic-pane .palette-host { flex: none; }
.graphic-pane .graphic-main { flex: 1; min-height: 0; }
/* ===== 泛微式画布节点视觉（P0 图形编辑画布） ===== */
/* 开始节点：绿色圆形 */
.designer-canvas :deep(.wm-start.djs-shape circle),
.designer-canvas :deep(.wm-start.djs-shape ellipse) { fill: #22c55e; stroke: #16a34a; stroke-width: 2; }
.designer-canvas :deep(.wm-start.djs-shape:hover circle) { fill: #16a34a; }
/* 归档（结束）节点：深红终止样式 */
.designer-canvas :deep(.wm-end.djs-shape circle),
.designer-canvas :deep(.wm-end.djs-shape ellipse) { fill: #fef2f2; stroke: #dc2626; stroke-width: 2; }
.designer-canvas :deep(.wm-end.djs-shape:hover circle) { fill: #fecaca; }
/* 审批节点：绿色描边圆角矩形 + 右下角出口小箭头标识 */
.designer-canvas :deep(.wm-task.djs-shape rect) { stroke: #22c55e; stroke-width: 2; fill: #f0fdf4; }
.designer-canvas :deep(.wm-task.djs-shape:hover rect) { stroke: #16a34a; }
.designer-canvas :deep(.wm-task.djs-shape)::after {
  content: '▸';
  position: absolute;
  right: -2px;
  bottom: -4px;
  width: 14px;
  height: 14px;
  border-radius: 7px;
  background: #22c55e;
  color: #fff;
  font-size: 9px;
  line-height: 14px;
  text-align: center;
  pointer-events: none;
}
/* 出口拖动排序手柄（泛微式：十字箭头，cursor:move） */
.drag-handle { display: inline-block; cursor: move; color: #94a3b8; font-size: 13px; letter-spacing: -2px; margin-right: 6px; user-select: none; }
.drag-handle:hover { color: #2563eb; }
/* 退回/驳回出口：红色虚线 + 红色箭头 */
.designer-canvas :deep(.wm-return.djs-connection .djs-visual > path) {
  stroke: #dc2626 !important;
  stroke-width: 2 !important;
  stroke-dasharray: 6, 4 !important;
  marker-end: url('#sequenceflow-end-white-black-red') !important;
}
.designer-canvas :deep(.wm-return.djs-connection .djs-visual > path:nth-child(2)) { fill: #dc2626 !important; stroke: #dc2626 !important; }
</style>

<!-- ===== 图形编辑全屏弹窗高度链（必须放在非 scoped 全局块） =====
 为什么不能写在 scoped 块里：本弹窗的 DOM 由 el-dialog 通过 teleport 挂到 body 下，
 .el-dialog__body 自身不带本组件作用域属性（浏览器实测其 attrs 只有 id/class），
 若写在 scoped 块里，:deep() 会被编译成 .el-dialog__body[data-v-xxx]，
 该选择器永远匹配不上，样式静默失效 —— 这正是之前"改了没效果"的直接原因。
 高度链实测（1280x720，修复前）：
   .el-dialog__body 234px / display:block  → .graphic-body 234px → .el-tabs__content 194px
   → .graphic-pane 194px → .graphic-main 153px，而 .designer-canvas 自身高 580px，
   画布溢出容器 134px 后被 .el-overlay 的 overflow:auto 裁掉，
   视觉上即"下半屏被一块空白压住、网格只覆盖上半段"。
 修复思路：让 .el-dialog__body 成为纵向 flex 容器并撑满，逐级把
 .graphic-body → .el-tabs__content → .el-tab-pane → .graphic-main → .designer-canvas
 打通为可计算的弹性高度，使 djs-container 的网格底图铺满整个剩余区域。 -->
<style lang="scss">
.designer-dialog .el-dialog__body {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100vh - 60px);
}
.designer-dialog .el-dialog__body > .designer-body { flex: 1 1 auto; min-height: 0; height: auto; }
.designer-dialog .graphic-body > .graphic-editor-tabs { flex: 1 1 auto; min-height: 0; height: auto; }
.designer-dialog .graphic-editor-tabs > .el-tabs__content {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}
.designer-dialog .graphic-editor-tabs > .el-tabs__content > .el-tab-pane { flex: 1 1 auto; min-height: 0; }
/* 画布自身铺满其容器，网格底图随之延伸到底部 */
.designer-dialog .graphic-main .designer-canvas { height: 100%; min-height: 0; }
</style>
