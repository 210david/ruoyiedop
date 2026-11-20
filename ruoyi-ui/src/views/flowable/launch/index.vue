<template>
  <div class="app-container flowable-launch-page">
    <!-- 列表模式：仅菜单「流程发起」时渲染（独立页 /flow/launch/create 不显示列表，避免跳转闪烁） -->
    <template v-if="!isStandaloneLaunch">
      <!-- ===== 搜索 ===== -->
      <div class="surface filter-card" v-show="showSearch">
        <div class="filter-head">
          <div class="filter-title"><span class="glyph"></span> 可发起的流程</div>
          <div class="control" style="width: 280px">
            <el-input v-model="keyword" placeholder="搜索流程名称" clearable>
              <template #prefix
                ><el-icon><Search /></el-icon
              ></template>
            </el-input>
          </div>
        </div>
      </div>

      <!-- ===== 分类区块 + 流程名称列表（参照e9新建流程） ===== -->
      <div class="surface" v-loading="loading">
        <div class="status-tabs">
          <div class="tabs-track">
            <button class="status-tab" :class="{ 'is-active': activeCategory === 'all' }" @click="activeCategory = 'all'">
              <span class="dot"></span><span>全部</span><span class="count">{{ defs.length }}</span>
            </button>
            <button v-for="c in categoryGroups" :key="c.value" class="status-tab" :class="{ 'is-active': activeCategory === c.value }" @click="activeCategory = c.value">
              <span class="dot"></span><span>{{ c.label }}</span
              ><span class="count">{{ c.items.length }}</span>
            </button>
          </div>
        </div>

        <div class="cat-groups">
          <section class="cat-card" v-for="g in visibleGroups" :key="g.value">
            <div class="cat-top" :class="cardColor(g.value)"></div>
            <div class="cat-head">
              <div class="cat-icon">
                <el-icon :size="20"><Files /></el-icon>
              </div>
              <div class="cat-name">{{ g.label }}</div>
              <div class="cat-count">{{ g.items.length }}</div>
            </div>
            <div class="cat-list">
              <div class="flow-item" v-for="d in g.items" :key="d.definitionId" @click="handleLaunch(d)">
                <el-icon class="flow-glyph" :size="16"><Promotion /></el-icon>
                <span class="flow-name">{{ d.modelName || d.modelKey }}</span>
                <span class="flow-ver">v{{ d.version }}</span>
                <el-icon class="flow-go"><ArrowRight /></el-icon>
              </div>
            </div>
          </section>
          <el-empty v-if="!loading && !visibleGroups.length" description="暂无可发起的流程，请先在后台部署" style="grid-column: 1/-1" />
        </div>
      </div>
    </template>

    <!-- ===== 独立发起页引导态：弹窗打开前页面为空，必须有可见的加载/失败兜底，否则就是「白板」 ===== -->
    <div v-if="isStandaloneLaunch && !open && (launchBooting || launchError)" class="launch-boot">
      <div v-if="launchError" class="boot-card boot-error">
        <el-icon class="boot-icon"><WarningFilled /></el-icon>
        <div class="boot-title">流程发起页加载失败</div>
        <div class="boot-desc">{{ launchError }}</div>
        <div class="boot-sub">流程：{{ route.query.name || current.modelName || route.query.key || '-' }}</div>
        <div class="boot-ops">
          <el-button type="primary" icon="Refresh" @click="retryLaunch">重新加载</el-button>
          <el-button icon="Back" @click="backFromLaunchError">返回待办列表</el-button>
        </div>
      </div>
      <div v-else class="boot-card">
        <el-icon class="boot-icon is-loading"><Loading /></el-icon>
        <div class="boot-title">正在加载流程表单…</div>
        <div class="boot-desc">{{ route.query.name || '请稍候，正在取流程定义与绑定表单' }}</div>
      </div>
    </div>

    <!-- ===== 发起流程：全屏页面（顶部：标题 + 页签 + 操作按钮；下方：内容区） ===== -->
    <el-dialog v-model="open" fullscreen append-to-body class="launch-dialog" :close-on-click-modal="false" destroy-on-close :show-close="false">
      <template #header>
        <div class="launch-header">
          <!-- 第一行：标题 + 操作按钮 -->
          <div class="launch-titlebar">
            <div class="lt-title">
              <span class="lt-glyph"></span>
              <span class="lt-name">{{ current.modelName || '发起流程' }}</span>
              <span class="lt-ver" v-if="current.version">V{{ current.version }}</span>
              <span class="lt-form" v-if="boundForm">
                <el-icon><Document /></el-icon>{{ boundForm.formName }}
              </span>
            </div>
            <div class="lt-actions">
              <el-button type="primary" icon="Check" :loading="submitting" @click="doLaunch">提交</el-button>
              <el-button icon="Finished" :loading="draftSaving" @click="saveDraft">保存</el-button>
            </div>
          </div>
          <!-- 第二行：页签 -->
          <div class="launch-tabs">
            <button class="launch-tab" :class="{ 'is-active': launchTab === 'form' }" @click="launchTab = 'form'">流程表单</button>
            <button class="launch-tab" :class="{ 'is-active': launchTab === 'diagram' }" @click="launchTab = 'diagram'">流程图</button>
            <button class="launch-tab" :class="{ 'is-active': launchTab === 'status' }" @click="launchTab = 'status'">流程状态</button>
            <button class="launch-tab" :class="{ 'is-active': launchTab === 'resource' }" @click="launchTab = 'resource'">相关资源</button>
          </div>
        </div>
      </template>

      <div class="launch-body">
        <!-- 提交成功结果条：发起成功的单据已经离开本页，必须留下单号与入口，
 否则空表单会让人怀疑「到底提交成功了没有」 -->
        <div v-if="launchedInfo" class="launch-done">
          <el-icon class="ld-icon"><CircleCheck /></el-icon>
          <span class="ld-text">流程已发起：{{ launchedInfo.title || '（未命名）' }}</span>
          <span class="ld-no">单号 {{ launchedInfo.processNo || launchedInfo.instanceId }}</span>
          <span class="ld-ops">
            <el-button size="small" type="primary" link @click="openLaunchedDetail">查看流程详情</el-button>
            <el-button size="small" link @click="launchedInfo = null">继续发起</el-button>
          </span>
        </div>

        <!-- 页签：流程表单 -->
        <div class="launch-pane" v-show="launchTab === 'form'">
          <!-- 与费用报销单等办理页一致：整个表单包在「申请表单」卡片里 -->
          <section class="launch-card" v-if="boundForm && boundForm.fields && boundForm.fields.length">
            <div class="launch-card-head">
              <span class="lc-icon">▤</span>申请表单
            </div>
            <div class="launch-card-body">
              <el-form ref="dynRef" :model="dynData" label-width="120px" hide-required-asterisk>
              <FormGridRender v-if="layoutMode === 'grid' && layoutGrid" ref="gridRenderRef" :grid="layoutGrid" :fields="boundForm.fields" :values="dynData" :editable-fields="noRuleEditableFields" :overrides="ruleOverrides" :rule-errors="ruleErrors" :rule-detail-errors="ruleDetailErrors" editable @detail-change="runFormRules" />
              <template v-else>
                <!-- 简易模式顶部流程名称：与分组标题一起构成表单层级 -->
                <div class="dyn-form-title">{{ boundForm.formName }}</div>
                <template v-for="(grp, gi) in renderGroups" :key="gi">
                  <div class="dyn-section-title" v-if="grp.title">{{ grp.title }}</div>
                  <el-row :gutter="8">
                    <el-col v-for="c in grp.cells" :key="c.key" :span="c.span || 24">
                      <div v-if="c.detailTable" class="dyn-detail-block">
                        <div class="dyn-detail-bar">
                          <span class="dyn-detail-name"
                            ><el-icon><Grid /></el-icon> {{ detailTableLabel(c.detailTable) }}</span
                          >
                          <span class="dyn-detail-ops">
                            <el-button size="small" link type="primary" icon="Plus" @click="addDetailRow(c.detailTable)">添加明细</el-button>
                          </span>
                        </div>
                        <table class="dyn-detail-table" v-if="detailTableFields(c.detailTable).length">
                          <thead>
                            <tr>
                              <th style="width: 48px">序号</th>
                              <th v-for="f in detailTableFields(c.detailTable)" :key="f.field">{{ f.label }}<span v-if="showDynDetailReqStar(c.detailTable, f)" style="color: #ef4444"> *</span></th>
                              <th style="width: 48px">操作</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(row, ri) in detailRows[c.detailTable] || []" :key="row._key">
                              <td style="text-align: center; color: #94a3b8">{{ ri + 1 }}</td>
                              <td v-for="f in detailTableFields(c.detailTable)" :key="f.field">
                                <dyn-field :f="f" v-model="row[f.field]" v-model:model-ids="row[f.field + '__id']" v-model:model-extra="row[f.field + '__extra']" />
                              </td>
                              <td style="text-align: center">
                                <el-button size="small" link type="danger" icon="Delete" @click="removeDetailRow(c.detailTable, ri)" />
                              </td>
                            </tr>
                            <tr v-if="!(detailRows[c.detailTable] || []).length" class="dyn-detail-norow">
                              <td :colspan="detailTableFields(c.detailTable).length + 2">暂无明细数据，无需填写可直接提交；如需填写请点击「添加明细」</td>
                            </tr>
                            <tr v-if="(detailRows[c.detailTable] || []).length" class="dyn-detail-sum-row">
                              <td style="text-align: center">合计</td>
                              <td v-for="f in detailTableFields(c.detailTable)" :key="f.field" style="text-align: right; color: #7c3aed; font-weight: 600">
                                <template v-if="(c.detailSum || []).includes(f.field)">Σ {{ detailSumValue(c, f.field) }}</template>
                              </td>
                              <td></td>
                            </tr>
                          </tbody>
                        </table>
                        <div v-else class="dyn-detail-empty">明细表{{ c.detailTable }}暂无字段，请先到表单管理"编辑字段"中添加</div>
                      </div>
                      <div v-else-if="c.note" class="dyn-note" :style="dynCellStyle(c)">{{ c.note }}</div>
                      <div v-else-if="c.style && (c.style.bgColor || c.style.border)" class="dyn-cell-wrap" :style="dynCellStyle(c)">
                        <el-form-item :prop="c.f.field" :rules="c.f.required ? [{ required: true, message: '请填写' + c.f.label, trigger: ['blur', 'change'] }] : []">
                          <template #label>
                            <span class="dyn-label">{{ c.f.label }}<i v-if="showDynReqStar(c.f)" class="dyn-req">*</i></span>
                          </template>
                          <span v-if="isFrozenField(c.f)" class="dyn-ro">{{ dynData[c.f.field] || '' }}</span>
                          <dyn-field v-else :f="c.f" v-model="dynData[c.f.field]" v-model:model-ids="dynData[c.f.field + '__id']" v-model:model-extra="dynData[c.f.field + '__extra']" />
                        </el-form-item>
                      </div>
                      <el-form-item
                        v-else
                        :prop="c.f.field"
                        :style="dynCellStyle(c)"
                        :rules="c.f.required ? [{ required: true, message: '请填写' + c.f.label, trigger: ['blur', 'change'] }] : []"
                      >
                        <template #label>
                          <span class="dyn-label">{{ c.f.label }}<i v-if="showDynReqStar(c.f)" class="dyn-req">*</i></span>
                        </template>
                        <span v-if="isFrozenField(c.f)" class="dyn-ro">{{ dynData[c.f.field] || '' }}</span>
                        <dyn-field v-else :f="c.f" v-model="dynData[c.f.field]" v-model:model-ids="dynData[c.f.field + '__id']" v-model:model-extra="dynData[c.f.field + '__extra']" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </template>
              </template>
            </el-form>
            </div>
          </section>
          <div v-else>
            <el-alert
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 14px"
              title="该流程未绑定表单"
              description="仍可直接发起流程；如需填写业务表单，请先到后台表单管理设计表单并在模型基本信息中关联表单"
            />
          </div>
        </div>

        <!-- 页签：流程图（发起前预览，未产生实例，展示设计态流程） -->
        <div class="launch-pane" v-show="launchTab === 'diagram'">
          <section class="launch-card">
            <div class="launch-card-head"><span class="lc-icon">◈</span>流程图（发起前预览）</div>
            <div class="launch-card-body">
              <div class="diagram-wrap">
                <!-- 用 bpmn-js 渲染 BPMN XML：与后台设计器同一套配色，并自适应缩放居中（位图版会黑白且偏下）
 v-if 挂在页签上：首屏不加载 bpmn-js，且首次渲染时容器已有尺寸，fit-viewport 才能正确居中 -->
                <wm-process-diagram v-if="launchTab === 'diagram'" :xml="previewDiagramXml" :height="460" empty-text="暂无流程图，请先在设计器中绘制并部署" />
              </div>
            </div>
          </section>
        </div>

        <!-- 页签：流程状态（发起前无实例；与流程详情 / 办理页统一由 FlowStatus 承载） -->
        <div class="launch-pane" v-show="launchTab === 'status'">
          <FlowStatus :tasks="launchStatusTasks" />
        </div>

        <!-- 页签：相关资源（发起前汇总表单里已上传的附件；关联流程等其他资源后续再扩展） -->
        <div class="launch-pane" v-show="launchTab === 'resource'">
          <section class="launch-card">
            <div class="launch-card-head">
              <span class="lc-icon">⊞</span>相关资源
              <span class="resource-count" v-if="launchResources.length">共 {{ launchResources.length }} 个附件</span>
            </div>
            <div class="launch-card-body">
              <el-table :data="launchResources" border size="small" :empty-text="boundForm ? '流程表单中尚未上传附件' : '该流程未绑定表单，暂无附件资源'">
                <el-table-column label="资源名称" min-width="280" show-overflow-tooltip>
                  <template #default="scope">
                    <a class="resource-link" :href="fileHref(scope.row.url)" target="_blank" rel="noopener">{{ scope.row.name }}</a>
                  </template>
                </el-table-column>
                <el-table-column label="创建人" prop="uploader" width="140" align="center" />
                <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
              </el-table>
            </div>
          </section>
        </div>
      </div>
    </el-dialog>

    <!-- 提交审批弹窗（「后续任务」）：提交前展示下一步节点与办理人，确认后发送 -->
    <el-dialog v-model="submitOpen" append-to-body width="840px" draggable :close-on-click-modal="false" class="submit-dialog">
      <template #header>
        <div class="sd-header">
          <span class="sd-header-icon"><el-icon><Promotion /></el-icon></span>
          <div>
            <div class="sd-header-title">提交审批</div>
            <div class="sd-header-sub">确认后续环节与办理人，提交后流程将立即流转</div>
          </div>
        </div>
      </template>
      <div class="sd-section">
        <div class="sd-section-head">
          <span class="sd-section-title">后续任务</span>
          <span v-if="nextNodes.length" class="sd-section-count">{{ nextNodes.length }} 个环节</span>
        </div>
        <div v-if="nextLoading" class="sd-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在获取下一步审批人…</span>
        </div>
        <template v-else>
          <div class="sd-node" v-for="(n, ni) in nextNodes" :key="n.id">
            <div class="sd-node-head">
              <span class="sd-node-step">{{ ni + 1 }}</span>
              <span class="sd-node-name">{{ n.name }}</span>
              <span class="sd-node-mode">
                <el-tag size="small" effect="plain" round>{{ n.modeText }}</el-tag>
                <el-tag size="small" effect="plain" type="info" round>{{ n.perfText }}</el-tag>
              </span>
            </div>
            <!-- 已解析出办理人（无需提交时选人）：一行紧凑展示，不再铺满标签 -->
            <div class="sd-people" v-if="!n.needPick && n.people.length">
              <span class="sd-people-label">办理人</span>
              <span class="sd-person" v-for="(p, pi) in n.people" :key="pi">
                <span class="sd-cand-avatar">{{ p.label.slice(0, 1) }}</span>{{ p.label }}
              </span>
            </div>
            <!-- 取人方式=提交时指定：在系统解析出的候选范围内按组织勾选 -->
            <div v-if="n.needPick" class="sd-pick">
              <div class="sd-pick-head">
                <span class="sd-pick-title">选择办理人</span>
                <span class="sd-pick-count">
                  已选 <b>{{ (n.picked || []).length }}</b> / {{ pickablePeople(n).length }}
                </span>
              </div>
              <template v-if="pickablePeople(n).length">
                <div class="sd-pick-body">
                  <!-- 候选跨多个部门：左侧组织树，点部门筛右侧人员 -->
                  <div class="sd-org" v-if="n.multiDept">
                    <el-input v-model="n.deptKw" size="small" clearable :prefix-icon="Search" placeholder="搜索组织" @input="(v) => filterOrgTree(n, v)" />
                    <div class="sd-org-tree">
                      <el-tree
                        :ref="(el) => setOrgTreeRef(n, el)"
                        :data="n.deptTree"
                        :props="{ label: 'label', children: 'children' }"
                        node-key="id"
                        :expand-on-click-node="false"
                        :default-expand-all="true"
                        :filter-node-method="filterOrgNode"
                        highlight-current
                        @node-click="(d) => (n.deptId = d && d.id ? d.id : null)"
                      >
                        <template #default="{ data }">
                          <span class="sd-org-node">
                            <span class="sd-org-name" :title="data.label">{{ data.label }}</span>
                            <span class="sd-org-count">{{ data.count }}</span>
                          </span>
                        </template>
                      </el-tree>
                    </div>
                  </div>
                  <div class="sd-users">
                    <div class="sd-pick-bar">
                      <el-input v-model="n.userKw" class="sd-pick-search" size="small" clearable :prefix-icon="Search" placeholder="搜索姓名" />
                      <el-checkbox
                        size="small"
                        :model-value="isAllPicked(n)"
                        :indeterminate="isSomePicked(n)"
                        @change="(v) => togglePickAll(n, v)"
                      >全选</el-checkbox>
                    </div>
                    <div class="sd-cand" :class="{ 'is-tree': n.multiDept }">
                      <div class="sd-cand-group" v-for="g in pickGroups(n)" :key="g.dept">
                        <div class="sd-cand-dept" v-if="!n.multiDept">{{ g.dept }}</div>
                        <el-checkbox-group v-model="n.picked" class="sd-cand-list">
                          <el-checkbox v-for="p in g.users" :key="p.id" :value="p.id">
                            <span class="sd-cand-avatar">{{ p.label.slice(0, 1) }}</span>{{ p.label }}
                          </el-checkbox>
                        </el-checkbox-group>
                      </div>
                      <div class="sd-cand-none" v-if="!pickGroups(n).length">没有匹配的人员</div>
                    </div>
                  </div>
                </div>
              </template>
              <span v-else class="sd-pick-empty">未解析出候选范围，提交后按节点配置自动指派</span>
            </div>
            <div v-if="n.auto" class="sd-auto">
              <el-icon><InfoFilled /></el-icon>
              <span>节点未配置具体办理人，提交后由引擎按组织关系自动指派</span>
            </div>
            <div v-else-if="n.dynamic.length" class="sd-auto">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ n.dynamic.join('、') }}：提交后按组织关系自动匹配</span>
            </div>
          </div>
          <div v-if="!nextNodes.length" class="sd-empty">
            <el-icon><InfoFilled /></el-icon>
            <span>未解析到下一步审批节点，提交后由流程引擎自动流转</span>
          </div>
        </template>
      </div>
      <template #footer>
        <span class="sd-footer-tip">提交后可在「我发起的」中查看流转进度</span>
        <div class="sd-footer-actions">
          <el-button @click="submitOpen = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="confirmSubmit">发送</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 提交成功回执（仿照：提交后立刻把流程图摆出来，标出流程停在哪一环，
 用户点「确认」即关闭本页 —— 独立发起页关标签页，列表内发起则关闭发起弹窗） -->
    <el-dialog v-model="resultOpen" append-to-body width="920px" top="6vh" :show-close="false" :close-on-click-modal="false" class="result-dialog">
      <template #header>
        <div class="rs-header">
          <el-icon class="rs-icon"><CircleCheck /></el-icon>
          <span class="rs-title">流程已提交</span>
          <span class="rs-sub" v-if="resultInfo.title">{{ resultInfo.title }}</span>
        </div>
      </template>
      <div class="rs-body">
        <div class="rs-meta">
          <div class="rs-meta-item">
            <span class="rs-k">流 程</span>
            <span class="rs-v">{{ resultInfo.processName || '-' }}</span>
          </div>
          <div class="rs-meta-item">
            <span class="rs-k">单 号</span>
            <span class="rs-v rs-mono">{{ resultInfo.instanceId || '-' }}</span>
          </div>
          <div class="rs-meta-item" v-if="resultStatusText">
            <span class="rs-k">当前环节</span>
            <span class="rs-v rs-current">{{ resultStatusText }}</span>
          </div>
        </div>
        <div class="rs-diagram">
          <wm-process-diagram
            v-if="resultOpen"
            :xml="resultDiagram.xml"
            :done-ids="resultDiagram.doneIds"
            :active-ids="resultDiagram.activeIds"
            :done-flow-ids="resultDiagram.doneFlowIds"
            :node-info="resultDiagram.nodes"
            :height="400"
            empty-text="流程图暂时取不到，可点「查看流程详情」查看完整流转信息"
          />
        </div>
      </div>
      <template #footer>
        <el-button v-if="resultInfo.instanceId" icon="View" @click="openResultDetail">查看流程详情</el-button>
        <el-button type="primary" icon="Check" @click="confirmResult">确 认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowLaunch">
import { listDefinition, getOrgPickerDepts } from '@/api/flowable/flow';
import { formByProcess, launch } from '@/api/flowable/form';
import { getDraft, listDraft, saveDraft as saveDraftApi, delDraft } from '@/api/flowable/draft';
import request from '@/utils/request';
import { parseTime } from '@/utils/ruoyi';
import { getToken } from '@/utils/auth';
import useUserStore from '@/store/modules/user';
import { Search, Promotion, ArrowRight, Files, CircleCheck, Loading, InfoFilled, WarningFilled } from '@element-plus/icons-vue';
import FormGridRender from '@/components/FormGridRender.vue';
import WmProcessDiagram from '@/components/WmProcessDiagram.vue';
import FlowStatus from '@/components/FlowStatus/index.vue';
import { parseLayout, isFlowSysField } from '@/views/flowable/form/designer/converters';
import { isSysReadonlyField } from '@/utils/flow-system-fields.js';
import { applyFieldDefaults, currentUserCtx, syncPersonDept, mainFieldsOf } from '@/views/flowable/common/field-defaults';
import { parseExt } from '@/views/flowable/common/form-rules/constants';
import { runRules, runFormScripts } from '@/views/flowable/common/form-rules/engine';
import { prepareWorkdayCalendar } from '@/views/flowable/common/form-rules/workday';
import { useRoute, useRouter } from 'vue-router';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const loading = ref(true);
const showSearch = ref(true);
const defs = ref([]);
const keyword = ref('');
const activeCategory = ref('all');
const open = ref(false);
const current = ref({});
const boundForm = ref(null);
/**
 * 独立发起页（新标签页 /flow/launch/create）的引导状态。
 * 该页面没有流程列表，唯一的可视内容就是发起弹窗：弹窗打开前页面是一片空白，
 * 接口一慢/一报错用户就只看到「白板」且无从判断原因，所以必须有加载态与失败态兜底。
 */
const launchBooting = ref(false);
const launchError = ref('');
let launchSeq = 0;

// ===== 交互规则引擎（flow_form.ext_json）=====
// 运行态：字段变化 → 引擎重算 → states 传 FormGridRender（必填/显隐/只读）+ 计算值回填 dynData
// 注意：dynData 必须在下方 watch 之前声明，否则 watch 注册时读取 dynData 会触发
// 「Cannot access 'dynData' before initialization」，整个组件挂载失败 → 页面白板。
const dynData = ref({});
const ruleOverrides = ref({});
/** 规则实时校验错误：{ [field]: 提示文案 }，随字段变化即时刷新（不用等提交） */
const ruleErrors = ref({});
/** 明细行规则校验错误：{ '表名|行号|字段': 提示文案 }，传 FormGridRender 行内标红 */
const ruleDetailErrors = ref({});
const activeFormRules = computed(() => (boundForm.value ? parseExt(boundForm.value.extJson).rules : []));
/** 高级脚本执行：phase=load/change/submit；发起页始终是可编辑上下文。返回错误文案（submit 拦截用） */
function runScriptsPhase(phase) {
  if (!boundForm.value) return '';
  const scripts = parseExt(boundForm.value.extJson).scripts || [];
  if (!scripts.length) return '';
  const detailData = (gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() : {}) || {};
  try {
    runFormScripts(scripts, phase, {
      get: (f) => dynData.value[f],
      set: (f, v) => {
        if (dynData.value[f] !== v) dynData.value[f] = v;
      },
      detailData,
      rows: (t) => detailData[t] || [],
      fail: (msg) => {
        throw new Error(msg);
      },
    });
    return '';
  } catch (e) {
    return (e && e.message) || '高级脚本执行出错';
  }
}
/** change 脚本防重入：脚本 set → watch → runFormRules → 脚本，一轮内只执行一次 */
let scriptRunning = false;
function runChangeScripts() {
  if (scriptRunning) return;
  scriptRunning = true;
  try {
    runScriptsPhase('change');
  } finally {
    scriptRunning = false;
  }
}
function runFormRules(changedField, detailField) {
  if (!activeFormRules.value.length) {
    ruleOverrides.value = {};
    ruleErrors.value = {};
    ruleDetailErrors.value = {};
    runChangeScripts();
    return;
  }
  const detailData = (gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() : {}) || {};
  // workDays 规则函数需要企业日历数据：按当前值区间预加载（已缓存则立即返回），随后同步求值
  prepareWorkdayCalendar(activeFormRules.value, dynData.value || {}).then(() => {
    // 明细变化时传 [表名, 字段名]，trigger 含任一即命中；主表变化传字段名
    const cf = detailField ? [changedField, detailField] : changedField;
    const result = runRules(activeFormRules.value, boundForm.value.fields || [], dynData.value || {}, detailData, { changedField: cf });
    ruleOverrides.value = result.states || {};
    // 校验类规则实时提示：引擎每次重算都会执行 validate 规则，把错误映射到字段格内联展示
    const errs = {};
    const dErrs = {};
    for (const e of result.errors || []) {
      if (e.table) {
        const key = e.table + '|' + e.rowIndex + '|' + e.field;
        if (!dErrs[key]) dErrs[key] = e.message;
      } else if (!errs[e.field]) {
        errs[e.field] = e.message;
      }
    }
    ruleErrors.value = errs;
    ruleDetailErrors.value = dErrs;
    // 计算/带出结果回填：值相同不写，写不发生变化时 watch 链自然收敛
    for (const [k, v] of Object.entries(result.values || {})) {
      if (dynData.value[k] !== v) dynData.value[k] = v;
    }
    // 高级脚本（字段变化时机）：规则重算后执行，脚本写回的值经 watch 再触发一轮校验收敛
    runChangeScripts();
  });
}
watch(
  () => dynData.value,
  () => runFormRules(),
  { deep: true }
);
/** 提交前校验：先跑高级脚本（submit 时机可拦截），再跑规则校验。返回第一条错误文案，无错误返回空串 */
async function validateFormRules() {
  const scriptErr = runScriptsPhase('submit');
  if (scriptErr) return scriptErr;
  if (!activeFormRules.value.length) return '';
  const detailData = (gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() : {}) || {};
  // workDays 需要日历数据：预加载后求值（已缓存则立即返回）
  await prepareWorkdayCalendar(activeFormRules.value, dynData.value || {});
  const { errors } = runRules(activeFormRules.value, boundForm.value.fields || [], dynData.value || {}, detailData, { validate: true });
  if (!errors || !errors.length) return '';
  const e0 = errors[0];
  const label = boundForm.value.fields.find((f) => f.field === e0.field)?.label || e0.field;
  return (e0.table ? label + ' 第' + (e0.rowIndex + 1) + '行 ' : label + ' ') + e0.message;
}
const userCtx = ref({});
const userOptions = ref([]);
const submitting = ref(false);
/** 当前草稿ID：保存草稿后回写，后续保存走更新，提交成功后清理 */
const draftId = ref(null);
const draftSaving = ref(false);
/** 提交审核弹窗：展示下一步节点与办理人（替代原先的「发起信息」卡片） */
const submitOpen = ref(false);
/** 发起成功后的结果（单号 + 标题）：独立发起页清空表单后靠它告诉用户「已经发出去了」 */
const launchedInfo = ref(null);
const nextNodes = ref([]);
const nextLoading = ref(false);
/** 提交结果回执：{ instanceId, title, processName, finished } */
const resultOpen = ref(false);
const resultInfo = ref({ instanceId: '', title: '', processName: '', finished: false });
/** 回执里的实例流程图：BPMN XML + 已流转/当前节点（取自 /flowable/process/diagramData） */
const resultDiagram = ref({ xml: '', doneIds: [], activeIds: [], doneFlowIds: [], nodes: {} });
/** 全屏发起页页签：流程表单 / 流程图 / 流程状态 / 相关资源 */
const launchTab = ref('form');
/** 发起前流程图预览：BPMN XML（由 bpmn-js 渲染，未产生实例，不做节点高亮） */
const previewDiagramXml = ref('');
/**
 * 发起前流程状态：尚未产生实例，只构造「发起节点」一条待办理记录，
 * 交给 FlowStatus 统一渲染（与流程详情 / 办理页同一套视觉与统计口径）。
 */
const launchStatusTasks = computed(() => [
  {
    taskName: '发起节点（申请人提交）',
    assigneeName: userStore.nickName || userStore.name || '',
    assigneeId: userStore.id || '',
    outcome: '',
    createTime: '',
    endTime: '',
    comment: '',
  },
]);
/** 附件上传时间：表单只存文件地址，前端按「本次填写首次出现」记录，用于相关资源列表展示 */
const resourceFileTimes = reactive({});
/**
 * 相关资源：发起前只汇总流程表单（主表 + 明细表）里已上传的附件，
 * 与泛微一致展示「资源名称 / 创建人 / 创建时间」；关联流程等资源后续再扩展。
 */
const launchResources = computed(() => {
  const rows = [];
  const attachFields = (boundForm.value?.fields || []).filter((f) => f.rtype === 'attach');
  if (!attachFields.length) return rows;
  const detailData = collectDetailData();
  const pushFiles = (value) => {
    String(value == null ? '' : value)
      .split(',')
      .map((v) => v.trim())
      .filter(Boolean)
      .forEach((url) => {
        rows.push({
          url,
          name: fileNameOf(url),
          uploader: userStore.nickName || userStore.name || '',
          createTime: resourceFileTimes[url] || '',
        });
      });
  };
  attachFields.forEach((f) => {
    if (!f.table || f.table === 'main') pushFiles(dynData.value[f.field]);
    else (detailData[f.table] || []).forEach((row) => pushFiles(row[f.field]));
  });
  return rows;
});
// 记录附件首次出现时间：上传/回填后即写入，供「创建时间」列展示（接口无该字段，只能前端记录）
watch(
  launchResources,
  (rows) => {
    const now = parseTime(new Date());
    rows.forEach((r) => {
      if (!resourceFileTimes[r.url]) resourceFileTimes[r.url] = now;
    });
  },
  { immediate: true },
);
/** 表单布局解析：mode grid → FormGridRender 网格渲染；section → 分组渲染 */
const layoutParsed = ref({ mode: 'section', sections: [], grid: null });
const layoutMode = computed(() => layoutParsed.value.mode);
const layoutGrid = computed(() => layoutParsed.value.grid);
const gridRenderRef = ref(null);

const filteredDefs = computed(() => {
  if (!keyword.value) return defs.value;
  return defs.value.filter((d) => (d.modelName || '').includes(keyword.value) || d.modelKey.includes(keyword.value));
});

// 按分类分组
const categoryGroups = computed(() => {
  const map = {};
  filteredDefs.value.forEach((d) => {
    const key = d.category || 'biz';
    (map[key] = map[key] || { value: key, label: catLabel(key), items: [] }).items.push(d);
  });
  return Object.values(map);
});
const visibleGroups = computed(() => (activeCategory.value === 'all' ? categoryGroups.value : categoryGroups.value.filter((g) => g.value === activeCategory.value)));
function catLabel(v) {
  const { flowable_category } = proxy.useDict('flowable_category');
  const item = flowable_category.value.find((d) => d.value == v);
  return item ? item.label : v || '未分类';
}
function cardColor(v) {
  return { oa: 'c-blue', hr: 'c-green', fin: 'c-amber', pms: 'c-red', biz: 'c-violet' }[v] || 'c-blue';
}

function getList() {
  loading.value = true;
  listDefinition({ pageNum: 1, pageSize: 100 })
    .then((res) => {
      // 已挂起 / 高级设置已锁定的流程不可发起，直接不进列表，避免点进去填完表单才被后端拦下
      defs.value = (res.rows || []).filter((d) => !d.suspended && !d.locked);
      loading.value = false;
    })
    .catch(() => {
      loading.value = false;
    });
}

function loadUsers() {
  request({ url: '/system/user/list', method: 'get', params: { pageNum: 1, pageSize: 200 } })
    .then((res) => {
      userOptions.value = res.rows || [];
    })
    .catch(() => {
      /* 仅用于把审批人 ID 译成姓名，取不到时直接显示 ID */
    });
}

/** 加载发起前流程图预览（按流程 key 取已部署定义的 BPMN XML，交给 bpmn-js 渲染）
 * 说明：与 POST 图片接口不同，这里拿的是 XML 文本，走普通 JSON 请求（自动带 Authorization）
 */
function loadPreviewDiagram() {
  previewDiagramXml.value = '';
  if (!current.value.modelKey) return;
  request({
    url: '/flowable/definition/xmlByKey/' + current.value.modelKey,
    method: 'get',
  })
    .then((res) => {
      previewDiagramXml.value = res.data || '';
    })
    .catch(() => {
      previewDiagramXml.value = '';
    });
}

/** 附件地址 → 展示用文件名（地址可能带目录前缀） */
function fileNameOf(url) {
  const s = String(url || '');
  const i = s.lastIndexOf('/');
  return i > -1 ? s.slice(i + 1) : s;
}
/** 附件访问地址（<a> 无法携带 Authorization，通过 token 参数访问） */
function fileHref(url) {
  if (!url) return '#';
  return import.meta.env.VITE_APP_BASE_API + '/common/file/access?resource=' + encodeURIComponent(url) + '&token=' + getToken();
}

/**
 * 收集明细行数据。
 * 关键：网格模式（layoutJson.mode = grid）下明细行住在 FormGridRender 组件内部，
 * 父级的 detailRows 虽然会被 initDetailRows() 填成一行空白（renderGroups 会给未布局的明细表补默认块），
 * 但那份数据没有渲染、用户也从没编辑过 —— 如果优先读它，提交出去的明细永远是空行，
 * 表现出来就是"明细填了、提交后打开是空的"。所以网格模式必须以组件实例为准。
 */
function collectDetailData() {
  const fromGrid = gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() || {} : null;
  if (layoutMode.value === 'grid' && layoutGrid.value) return fromGrid || {};
  const rows = {};
  for (const [dt, list] of Object.entries(detailRows.value || {})) {
    if (list.length) rows[dt] = list.map(({ _key, ...rest }) => rest);
  }
  if (Object.keys(rows).length) return rows;
  return fromGrid || {};
}

/**
 * 保存草稿：登记表单内容到 flow_draft，不发起流程、不产生待办任务；
 * 之后可在「待办流程-草稿」页签看到并继续填写提交。
 */
function saveDraft() {
  if (!current.value || !current.value.modelKey) {
    proxy.$modal.msgWarning('流程尚未加载完成，请稍后再试');
    return;
  }
  draftSaving.value = true;
  saveDraftApi({
    draftId: draftId.value,
    processKey: current.value.modelKey,
    processName: current.value.modelName || '',
    formId: boundForm.value ? boundForm.value.formId : null,
    title: submitTitle(),
    formData: JSON.stringify({ dynData: { ...(dynData.value || {}) }, detailData: collectDetailData() }),
  })
    .then((res) => {
      // AjaxResult 把 draftId 放在返回体顶层（res.draftId），兼容放在 data 里的情况
      const newId = res && (res.draftId || (res.data && res.data.draftId));
      if (newId) {
        draftId.value = newId;
        syncDraftQuery(newId);
        proxy.$modal.msgSuccess('已保存草稿，可在「待办流程-草稿」继续填写');
        return;
      }
      // 兜底：没拿到 ID 时按「流程 + 标题」认领刚保存的草稿，避免同一次发起重复插入
      return adoptDraftByTitle();
    })
    .catch(() => {
      proxy.$modal.msgError('草稿保存失败');
    })
    .finally(() => {
      draftSaving.value = false;
    });
}

/** 兜底认领：接口未回传草稿ID时，取同流程同标题的最近一条草稿作为当前草稿 */
function adoptDraftByTitle() {
  return listDraft({ pageNum: 1, pageSize: 10, processKey: current.value.modelKey, title: submitTitle() })
    .then((r) => {
      const row = (r.rows || [])[0];
      if (row && row.draftId) {
        draftId.value = row.draftId;
        syncDraftQuery(row.draftId);
      }
      proxy.$modal.msgSuccess('已保存草稿，可在「待办流程-草稿」继续填写');
    })
    .catch(() => {
      proxy.$modal.msgSuccess('已保存草稿，可在「待办流程-草稿」继续填写');
    });
}

/**
 * 草稿ID同步到地址栏：独立发起页只有在 URL 带 draftId 时才恢复草稿内容，
 * 保存草稿后不同步的话，刷新会被当成一次新的发起（打开空白新表单）。
 */
function syncDraftQuery(id) {
  if (!isStandaloneLaunch.value || !id) return;
  if (String(route.query.draftId || '') === String(id)) return;
  router.replace({ path: route.path, query: { ...route.query, draftId: String(id) } });
}

/** 提交成功后草稿已删除：从地址栏摘掉 draftId，避免刷新时再去找一份不存在的草稿 */
function clearDraftQuery() {
  if (!isStandaloneLaunch.value || !route.query.draftId) return;
  const query = { ...route.query };
  delete query.draftId;
  router.replace({ path: route.path, query });
}

/** 关闭全屏发起页 */
function closeLaunch() {
  open.value = false;
}

/** 字段归一化：新版 {form,dtype} 与旧版 {type} 统一为渲染类型 rtype；select 兼容键值对/字符串/数组 */
function normalizeField(f) {
  const g = { ...f };
  let t = f.type;
  if (f.form) {
    if (['check', 'browse', 'attach', 'select', 'textarea', 'radio', 'checkbox', 'switch', 'rate'].includes(f.form)) t = f.form;
    else if (f.form === 'date' || f.form === 'datetime') t = f.form;
    else if (f.form === 'number') t = 'number';
    else t = f.dtype === 'datetime' ? 'datetime' : f.dtype === 'date' ? 'date' : f.dtype === 'float' || f.dtype === 'int' ? 'number' : 'text';
  } else if (!t) t = 'text';
  // 系统字段固定渲染：紧急程度→单选按钮组；签字意见→富文本编辑器
  if (f.field === 'urgency') t = 'radio';
  if (f.field === 'signOpinion') t = 'editor';
  g.rtype = t;
  if (t === 'select' || t === 'radio' || t === 'checkbox') {
    // 新版键值对 selectOptions: [{label,value}]；旧版 options 字符串/数组兼容（label=value）
    if (Array.isArray(f.selectOptions) && f.selectOptions.length) {
      g.optionList = f.selectOptions.map((o) => (typeof o === 'object' ? { label: o.label, value: o.value !== undefined ? o.value : o.label } : { label: o, value: o }));
    } else {
      const raw = f.options;
      const arr = Array.isArray(raw)
        ? raw
        : String(raw || '')
            .split(/[,，、;；]/)
            .map((s) => s.trim())
            .filter(Boolean);
      g.optionList = arr.map((o) => (typeof o === 'object' ? o : parseOptionPair(o)));
    }
  }
  return g;
}

/** 单个选项文本 "显示值:存储值" → {label,value} */
function parseOptionPair(s) {
  const idx = String(s).indexOf(':');
  if (idx > 0)
    return {
      label: String(s).substring(0, idx).trim(),
      value: String(s)
        .substring(idx + 1)
        .trim(),
    };
  return { label: s, value: s };
}

async function handleLaunch(d) {
  // ：点击流程在浏览器新标签页打开独立发起页（不在当前页面弹窗）
  const routeUrl = router.resolve({
    path: '/flow/launch/create',
    query: { key: d.modelKey, name: d.modelName || '', version: d.version || '' },
  }).href;
  window.open(routeUrl, '_blank');
}

/** 独立发起页初始化：按 URL 参数加载流程定义与绑定表单 */
async function initLaunchFromRoute() {
  // 每次引导递增序号：重试与快速切换流程时，旧的一轮回来不要再改动状态
  const seq = ++launchSeq;
  launchError.value = '';
  const key = route.query.key;
  if (!key) {
    // 缺少流程标识（例如手动粘贴地址打开）：给出可操作提示，而不是停在白板
    launchBooting.value = false;
    launchError.value = '缺少流程标识，无法发起，请从「流程发起」列表中选择流程';
    proxy.$modal.msgWarning('缺少流程标识，无法发起');
    return;
  }
  launchBooting.value = true;
  loading.value = false;
  try {
    // 独立页没有列表数据：按 key 反查流程定义，补齐流程名称/版本（标题默认值依赖流程名称）
    let hit = defs.value.find((x) => x.modelKey === key);
    if (!hit) {
      try {
        const res = await listDefinition({ modelKey: key, pageNum: 1, pageSize: 10 });
        if (seq !== launchSeq) return;
        defs.value = res.rows || [];
        hit = defs.value.find((x) => x.modelKey === key);
      } catch (e) {
        /* 取不到时退回 URL 参数 */
      }
    }
    if (seq !== launchSeq) return;
    // 锁定 / 挂起的流程即使拿到直链地址也不能发起（后端同样会拦截，这里先给出可读提示）
    if (hit && (hit.locked || hit.suspended)) {
      const nm = hit.modelName || hit.modelKey;
      launchBooting.value = false;
      launchError.value = hit.locked ? `流程「${nm}」已在高级设置中锁定，暂不可发起` : `流程「${nm}」已挂起，暂不可发起`;
      return;
    }
    const target = hit || {
      modelKey: key,
      modelName: route.query.name || '',
      version: route.query.version ? Number(route.query.version) : undefined,
    };
    // prepareLaunch 内部已有 try/finally，保证即使加载失败也会打开弹窗并给出提示
    await prepareLaunch(target);
    if (seq !== launchSeq) return;
    // 草稿续填：从「待办流程-草稿」点「继续填写」进入时，回填上次保存的内容
    if (route.query.draftId) await restoreDraft(route.query.draftId);
  } catch (e) {
    // 兜底：任何未预料的异常都要留下可读的失败提示，不允许静默白板
    if (seq !== launchSeq) return;
    launchError.value = (e && (e.message || e.msg)) || '表单加载异常，请重试';
  } finally {
    if (seq === launchSeq) launchBooting.value = false;
  }
}

/** 独立发起页失败后重试：清掉错误重新引导一次 */
function retryLaunch() {
  launchError.value = '';
  initLaunchFromRoute();
}

/** 独立发起页放弃引导：返回待办列表（被浏览器拦截 window.close 时也能有去处） */
function backFromLaunchError() {
  router.push('/flow/todo');
}

/** 草稿回填：恢复主表字段与明细行 */
async function restoreDraft(id) {
  try {
    const res = await getDraft(id);
    const d = (res && res.data) || res;
    if (!d || !d.draftId) return;
    draftId.value = d.draftId;
    let payload = {};
    try {
      payload = JSON.parse(d.formData || '{}') || {};
    } catch (e) {
      payload = {};
    }
    if (payload.dynData) dynData.value = { ...dynData.value, ...payload.dynData };
    const detailData = payload.detailData || {};
    const tables = Object.keys(detailData);
    if (tables.length) {
      // 简易分组模式：直接回填 detailRows
      const simple = {};
      tables.forEach((t) => {
        simple[t] = (detailData[t] || []).map((r) => ({ _key: 'r' + Math.random().toString(36).slice(2, 8), ...r }));
      });
      detailRows.value = { ...detailRows.value, ...simple };
      // 网格模式：交给 FormGridRender 覆盖（组件未提供 setter 时忽略）
      nextTick(() => {
        if (gridRenderRef.value && gridRenderRef.value.setDetailData) gridRenderRef.value.setDetailData(detailData);
      });
    }
    proxy.$modal.msgSuccess('已载入草稿，可继续填写后提交');
  } catch (e) {
    // 草稿不存在或无权访问：按新建处理，不阻塞发起
  }
}

/** 流程编号规则（发起页只关心：是否启用、是否允许改号、下一号长什么样）
 * fields：编号规则里用到的表单字段（如「类型」下拉），这些字段的值变了，编号的默认值也要跟着变
 */
const noRule = ref({ enabled: false, allowEdit: false, preview: '', fields: [] });
const noRuleEditableFields = computed(() => (noRule.value.enabled && noRule.value.allowEdit ? ['processNo'] : []));
/** 发起人是否自己改过编号：改过就不再跟着表单字段自动重算，避免把人改的号冲掉 */
let noRuleAuto = true;
/** 上一次自动填进去的编号，用来区分「自动填的」与「人改的」 */
let noRuleLastAuto = '';
async function loadNoRule(modelKey) {
  noRule.value = { enabled: false, allowEdit: false, preview: '', fields: [] };
  noRuleAuto = true;
  await refreshNoRulePreview(modelKey);
  // 允许改号时先填上「下一号」作为默认值，用户改成什么就用什么（重号后端会自动改回规则号）
  applyAutoNo();
}
/** 取「下一号」：带上当前表单值，编号里的表单字段段（含代字）才能拼对 */
async function refreshNoRulePreview(modelKey) {
  const key = modelKey || current.value.modelKey;
  if (!key) return;
  try {
    const res = await request({
      url: '/flowable/processNo/preview',
      method: 'post',
      data: { processKey: key, sample: { ...dynData.value } },
    });
    const d = res.data || {};
    const segs = (d.rule && d.rule.segments) || [];
    noRule.value = {
      enabled: !!d.enabled,
      allowEdit: !!d.allowEdit,
      preview: d.preview || noRule.value.preview,
      fields: segs.filter((s) => s.type === 'field' && s.field).map((s) => s.field),
    };
  } catch (e) {
    /* 无编号规则：按只读处理 */
  }
}
/** 把「下一号」填进流程编号；已有非自动填的号（草稿/人改的）不动 */
function applyAutoNo() {
  if (!noRule.value.enabled || !noRule.value.allowEdit) return;
  if (dynData.value.processNo && dynData.value.processNo !== noRuleLastAuto) return;
  noRuleLastAuto = noRule.value.preview || '';
  dynData.value.processNo = noRuleLastAuto;
}
// 编号用到的表单字段一改就重算默认号（编号里有「类型」这类选项字段时，号要跟着变）
let noRuleRefreshTimer = null;
watch(
  () => (noRule.value.fields || []).map((f) => String(dynData.value[f] == null ? '' : dynData.value[f])).join('\u0001'),
  () => {
    // 发起人自己改过号就不再自动重算，尊重他的输入
    if (!noRuleAuto) return;
    clearTimeout(noRuleRefreshTimer);
    noRuleRefreshTimer = setTimeout(() => {
      noRuleRefreshTimer = null;
      refreshNoRulePreview().then(() => applyAutoNo());
    }, 300);
  },
);
// 人改了编号：后续不再自动覆盖
watch(
  () => dynData.value.processNo,
  (v) => {
    if (v !== noRuleLastAuto) noRuleAuto = false;
  },
);

async function prepareLaunch(d) {
  current.value = d;
  dynData.value = {};
  boundForm.value = null;
  Object.keys(resourceFileTimes).forEach((k) => delete resourceFileTimes[k]);
  launchTab.value = 'form';
  try {
    const res = await formByProcess(d.modelKey);
    if (res.data && res.data.fieldsJson) {
      let fields = [];
      try {
        fields = (JSON.parse(res.data.fieldsJson) || []).map(normalizeField);
      } catch (e) {
        // 字段定义坏了：仍然要打开空表单，并明确告诉用户原因，不能白板
        proxy.$modal.msgError('表单字段定义解析失败，请联系管理员检查表单设计');
      }
      layoutParsed.value = parseLayout(res.data.layoutJson);
      boundForm.value = { ...res.data, fields, layoutJson: res.data.layoutJson };
      // 以下每一步都单独隔离：编号规则、默认值、明细行、规则引擎任一环抛错，
      // 都不能把已经拿到的表单丢掉，更不能让 open 永远为 false 停在白板
      try {
        await loadNoRule(d.modelKey);
      } catch (e) {
        /* 编号规则不可用时按无规则处理 */
      }
      try {
        initDetailRows();
        applyDefaultTitle();
        // 字段默认值：固定值 / 当天日期同步填；当前用户、当前部门需异步取，拿到后再补一次（取不到就不填）
        applyDefaultsAll(null);
        // 规则引擎首跑：带出默认值相关的计算/显隐/必填状态
        runFormRules();
        // 高级脚本（打开表单时机）：默认值就位后执行一次
        nextTick(() => runScriptsPhase('load'));
      } catch (e) {
        /* 默认值与规则初始化失败：保留空表单可填 */
      }
      // 默认值填好后重算一次编号：编号里的表单字段段要用这些值
      refreshNoRulePreview(d.modelKey)
        .then(() => applyAutoNo())
        .catch(() => {});
      currentUserCtx()
        .then((ctx) => {
          userCtx.value = ctx;
          applyDefaultsAll(ctx);
        })
        .catch(() => {});
    }
  } catch (e) {
    /* 无绑定表单 */
  }
  // 注意：这里**不要**再从 localStorage 回填任何「上次填写内容」。
  // 历史版本写过一个 flow_launch_draft_<modelKey> 的本地缓存，且只写不清，
  // 导致每次新发起都会把上一次填的值（文本、选择、勾选、附件）带出来，
  // 看起来像"新表单怎么已有值"。草稿恢复现在统一走「保存草稿 + draftId + 后端接口」，
  // 新建流程必须是全空表单。
  try {
    loadUsers();
  } catch (e) {
    /* 人员列表失败不阻塞发起 */
  }
  try {
    loadPreviewDiagram();
  } catch (e) {
    /* 流程图失败不阻塞发起 */
  }
  // finally 语义：无论上面哪一步出问题，发起弹窗都必须打开，
  // 打开后用户至少能看到「该流程未绑定表单」等明确内容，而不是空页面
  try {
    open.value = true;
  } finally {
    launchBooting.value = false;
  }
}

// ===== 页面布局渲染（按表单 layoutJson 分组分栏；无布局回退单栏） =====
const renderGroups = computed(() => {
  const all = boundForm.value?.fields || [];
  if (!all.length) return [];
  let layout = { sections: [] };
  try {
    const o = JSON.parse(boundForm.value?.layoutJson || '{}');
    if (Array.isArray(o.sections)) layout = o;
  } catch (e) {}
  // 主表与明细表允许同名字段（如「部门」）：布局里的普通单元格只认主表字段
  const byMainField = {};
  all.forEach((f) => {
    if (!f.table || f.table === 'main') byMainField[f.field] = f;
  });
  const used = new Set();
  const detailUsed = new Set();
  const groups = [];
  let noteSeq = 0;
  let dtSeq = 0;
  const spanOf = (s) => {
    const n = Number(s);
    return [6, 8, 12, 24].includes(n) || (n > 6 && n < 24) ? n : 24;
  };
  for (const sec of layout.sections) {
    const cells = (sec.rows || [])
      .flat()
      // v>=3 布局里管理员主动放置的系统字段（标题/流程编号/紧急程度）要正常渲染
      .filter((c) => c && ((c.field && byMainField[c.field]) || c.note || c.detailTable))
      .map((c) => {
        if (c.detailTable) {
          detailUsed.add(c.detailTable);
          return { key: 'dtb' + ++dtSeq, detailTable: c.detailTable, detailSum: c.detailSum || [], span: 24, style: {} };
        }
        if (c.note) return { key: 'note' + ++noteSeq, note: c.note, span: spanOf(c.span), style: c };
        used.add(c.field);
        return { key: c.field, f: { ...byMainField[c.field], label: c.label || byMainField[c.field].label }, span: spanOf(c.span), style: c };
      });
    if (cells.length) groups.push({ title: sec.title || '', cells });
  }
  // 未布局字段回退区：仅主表业务字段（明细字段由明细表块承载；系统字段需管理员主动放置才显示）
  const rest = all.filter((f) => !used.has(f.field) && (!f.table || f.table === 'main') && !isFlowSysField(f.field));
  if (rest.length) groups.push({ title: layout.sections.length ? '其他信息' : '', cells: rest.map((f) => ({ key: f.field, f, span: 24, style: {} })) });
  // 布局中未出现但字段里存在的明细表：追加默认明细表块，避免明细字段丢失
  for (const dt of new Set(all.filter((f) => f.table && f.table !== 'main').map((f) => f.table))) {
    if (!detailUsed.has(dt)) groups.push({ title: '', cells: [{ key: 'dtb_auto_' + dt, detailTable: dt, detailSum: [], span: 24, style: {} }] });
  }
  return groups;
});

// ===== 明细表运行时（可编辑表格 + 添加/删除行 + 表尾合计） =====
const detailRows = ref({});
/** 人员→部门联动：只盯「带出部门」规则里的人员字段（主表 + 明细行），避免表单任何输入都触发 */
const deptLinkKey = computed(() => {
  const parts = [];
  const push = (fs, vals) => {
    for (const f of fs) {
      if (!f || f.defaultMode !== 'fieldUserDept') continue;
      const src = f.defaultField;
      const extra = vals[src + '__extra'];
      parts.push(`${f.field}=${vals[src]}|${vals[src + '__id']}|${extra ? extra.deptName : ''}`);
    }
  };
  push(mainFieldsOf(boundForm.value && boundForm.value.fields), dynData.value);
  for (const t of Object.keys(detailRows.value || {})) {
    const dfs = detailTableFields(t);
    for (const row of detailRows.value[t] || []) push(dfs, row);
  }
  return parts.join('~');
});
watch(deptLinkKey, () => syncDeptLinks(userCtx.value));
/** 按布局初始化各明细表行（默认 1 行，"新增默认明细行数"） */
function initDetailRows() {
  // 网格模式：明细行由 FormGridRender 自己维护，这里不再造一份空白行，
  // 否则 collectDetailData() 有可能读到这份"没人编辑过的空行"，提交时把真实明细覆盖掉
  if (layoutMode.value === 'grid' && layoutGrid.value) {
    detailRows.value = {};
    return;
  }
  const rows = {};
  for (const grp of renderGroups.value) {
    for (const c of grp.cells) {
      if (c.detailTable && !rows[c.detailTable]) {
        const fs = detailTableFields(c.detailTable);
        rows[c.detailTable] = fs.length ? [blankDetailRow(fs)] : [];
      }
    }
  }
  detailRows.value = rows;
}
function detailTableFields(dt) {
  return (boundForm.value?.fields || []).filter((f) => f.table === dt);
}
/**
 * 应用字段默认值：主表 + 简易明细行。
 * ctx 为 null 时只填不依赖用户的默认值（固定值、当天日期/时间），
 * 拿到当前用户上下文后再调一次补「当前用户 / 当前部门」以及人员带出部门。
 */
function applyDefaultsAll(ctx) {
  const fs = mainFieldsOf(boundForm.value && boundForm.value.fields);
  applyFieldDefaults(fs, dynData.value, ctx);
  for (const t of Object.keys(detailRows.value || {})) {
    const dfs = detailTableFields(t);
    if (!dfs.length) continue;
    for (const row of detailRows.value[t] || []) applyFieldDefaults(dfs, row, ctx);
  }
  syncDeptLinks(ctx || {});
}
/** 人员→部门联动（主表 + 明细行）：只处理配了「随人员字段带出部门」的部门字段 */
function syncDeptLinks(ctx) {
  const fs = mainFieldsOf(boundForm.value && boundForm.value.fields);
  syncPersonDept(fs, dynData.value, ctx);
  for (const t of Object.keys(detailRows.value || {})) {
    const dfs = detailTableFields(t);
    if (!dfs.some((f) => f && f.defaultMode === 'fieldUserDept')) continue;
    for (const row of detailRows.value[t] || []) syncPersonDept(dfs, row, ctx);
  }
}
/** 明细表显示名：dt1 → 明细表1 */
function detailTableLabel(dt) {
  const set = [...new Set((boundForm.value?.fields || []).filter((f) => f.table && f.table !== 'main').map((f) => f.table))];
  const idx = set.indexOf(dt);
  return '明细表' + (idx >= 0 ? String(idx + 1) : dt.slice(2));
}
function blankDetailRow(fs) {
  const row = { _key: 'r' + Math.random().toString(36).slice(2, 8) };
  fs.forEach((f) => {
    row[f.field] = undefined;
  });
  // 新增行同样享有字段默认值（固定值 / 当前用户 / 当前部门 / 当天日期），已有值不覆盖
  const ctx = userCtx.value || {};
  applyFieldDefaults(fs, row, ctx);
  syncPersonDept(fs, row, ctx);
  return row;
}
function addDetailRow(dt) {
  if (!detailRows.value[dt]) detailRows.value[dt] = [];
  detailRows.value[dt].push(blankDetailRow(detailTableFields(dt)));
}
function removeDetailRow(dt, idx) {
  const rows = detailRows.value[dt] || [];
  if (idx < 0 || idx >= rows.length) return;
  // 删到 0 行是合法状态：明细是否必须有数据由提交校验决定，不要在此处偷偷补一行
  rows.splice(idx, 1);
}
/** 表尾合计：对 detailSum 字段逐列求和（仅数值） */
function detailSumValue(cell, field) {
  const rows = detailRows.value[cell.detailTable] || [];
  return rows.reduce((s, r) => s + (Number(r[field]) || 0), 0);
}

/** 布局单元格样式 → 发起页内联样式（字体/字号/粗斜体/文字色/背景/边框） */
function dynCellStyle(c) {
  const s = c && c.style ? c.style : {};
  const o = {};
  if (s.fontFamily) o.fontFamily = s.fontFamily;
  if (s.fontSize) o.fontSize = s.fontSize + 'px';
  if (s.bold) o.fontWeight = '700';
  if (s.italic) o.fontStyle = 'italic';
  if (s.color) o.color = s.color;
  if (s.bgColor) o.backgroundColor = s.bgColor;
  if (s.borderColor) o.borderColor = s.borderColor;
  if (s.border === 'all') {
    o.border = '1px solid ' + (s.borderColor || '#94a3b8');
    o.borderRadius = '4px';
    o.padding = '4px 8px';
    o.marginBottom = '4px';
  }
  if (s.border === 'bottom') {
    o.borderBottom = '2px solid ' + (s.borderColor || '#94a3b8');
    o.paddingBottom = '4px';
  }
  return o;
}

/** 系统只读字段（流程编号/流程状态/打印次数）：发起页只展示不编辑；
 * 但编号规则开了「允许手工修改」时，流程编号要放开给发起人填 */
function isFrozenField(f) {
  if (!f) return false;
  if (f.field === 'processNo' && noRule.value.enabled && noRule.value.allowEdit) return false;
  return !!f.readonly || isSysReadonlyField(f.field);
}

/** 附件类型约束 → file-upload 组件 accept */
function attachAccept(f) {
  const t = (f && f.attachType) || 'all';
  if (t === 'image') return ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp'];
  if (t === 'doc') return ['doc', 'docx', 'pdf', 'xls', 'xlsx', 'ppt', 'pptx', 'txt'];
  return undefined;
}

/** 默认标题：流程名称-申请人-日期（表单「标题」默认值） */
function genDefaultTitle(d) {
  const name = (d && d.modelName) || (current.value && current.value.modelName) || '';
  const who = userStore.nickName || userStore.name || '';
  return [name, who, formatDay(new Date())].filter(Boolean).join('-');
}
function formatDay(dt) {
  const p = (n) => String(n).padStart(2, '0');
  return dt.getFullYear() + '-' + p(dt.getMonth() + 1) + '-' + p(dt.getDate());
}
/** 表单存在「标题」字段时写入默认值（用户可改）；无该字段时不弄虚作假打扰用户 */
function applyDefaultTitle() {
  const fields = (boundForm.value && boundForm.value.fields) || [];
  const hasTitle = fields.some((f) => f.field === 'title' || f.label === '标题');
  if (!hasTitle) return;
  const cur = dynData.value && dynData.value.title;
  if (cur === undefined || cur === null || String(cur).trim() === '') {
    dynData.value.title = genDefaultTitle(current.value);
  }
}
/** 提交给后端的流程标题：优先表单里填的标题，其次自动生成 */
function submitTitle() {
  const t = dynData.value && dynData.value.title;
  return t !== undefined && t !== null && String(t).trim() ? String(t) : genDefaultTitle(current.value);
}
/**
 * 提交给流程引擎的变量：剔除浏览按钮的「__id」伴生键。
 * __id 只是为了再次打开弹框时回显高亮，属前端状态，不该写进流程变量；
 * 只剔除「确实存在同名字段」的伴生键，避免误伤业务里本来就以 __id 结尾的字段。
 */
function formVariables() {
  const src = dynData.value || {};
  const names = new Set(((boundForm.value && boundForm.value.fields) || []).map((f) => f.field));
  const out = {};
  Object.keys(src).forEach((k) => {
    if (k.length > 4 && k.endsWith('__id') && names.has(k.slice(0, -4))) return;
    out[k] = src[k];
  });
  return out;
}

/** 提交：先校验表单，再弹出「后续任务」确认（显示下一步审批人） */
function doLaunch() {
  validateForm().then((ok) => {
    if (!ok) return;
    submitOpen.value = true;
    loadNextNodes();
  });
}

function validateForm() {
  return new Promise((resolve) => {
    const hasForm = boundForm.value && boundForm.value.fields && boundForm.value.fields.length;
    const formRef = proxy.$refs['dynRef'];
    if (!hasForm || !formRef) {
      resolve(true);
      return;
    }
    formRef.validate(async (valid) => {
      if (!valid) {
        resolve(false);
        return;
      }
      // 规则引擎校验（validate 规则）：先于明细校验，命中直接拦截
      const ruleMsg = await validateFormRules();
      if (ruleMsg) {
        launchTab.value = 'form';
        proxy.$modal.msgWarning(ruleMsg);
        resolve(false);
        return;
      }
      // 主表通过后再校明细表：明细行的必填列不在 el-form 模型里，必须单独校
      resolve(validateDetailRequired());
    });
  });
}

/** 空值判定：undefined / null / 纯空白字符串 / 空数组 均视为未填 */
function isBlankVal(v) {
  if (v === undefined || v === null) return true;
  if (typeof v === 'string') return v.trim() === '';
  if (Array.isArray(v)) return v.length === 0;
  return false;
}

/**
 * 分组回退模式的必填星号（泛微口径）：有值隐藏，清空后恢复。
 * 网格模式由 FormGridRender 自己按值计算，这里只覆盖没有 grid 布局时的分组渲染。
 */
function showDynReqStar(f) {
  if (!f || !f.required) return false;
  return isBlankVal(dynData[f.field]);
}

/** 分组回退模式的明细列必填星号：明细还没行或任一行该列为空就显示，全填后隐藏 */
function showDynDetailReqStar(table, f) {
  if (!f || !f.required) return false;
  const rows = detailRows.value[table] || [];
  if (!rows.length) return true;
  return rows.some((r) => isBlankVal(r[f.field]));
}

/**
 * 明细表必填校验（提交前置）：
 * - 网格模式：明细行由 FormGridRender 维护，校验与标红交给组件；
 * - 分组模式：明细行在本页 detailRows，逐行校必填列。
 * 不通过时切回「流程表单」页签并提示第一条错误，避免用户在隐藏页签里找不到问题。
 */
function validateDetailRequired() {
  let msg = '';
  const grid = gridRenderRef.value;
  if (layoutMode.value === 'grid' && grid && grid.validateDetails) {
    const msgs = grid.validateDetails();
    if (msgs.length) msg = msgs[0];
  } else {
    const msgs = [];
    for (const grp of renderGroups.value) {
      for (const c of grp.cells) {
        if (!c.detailTable) continue;
        const fs = detailTableFields(c.detailTable);
        const rows = detailRows.value[c.detailTable] || [];
        const reqs = fs.filter((f) => f && f.required);
        if (!reqs.length) continue;
        const filled = rows.filter((r) => fs.some((f) => !isBlankVal(r[f.field])));
        // 空表放行：必填列只约束已存在的行（整表是否必须有数据由明细块「必须填写明细」控制）
        if (!filled.length) continue;
        reqs.forEach((f) =>
          filled.forEach((r) => {
            if (isBlankVal(r[f.field])) msgs.push(detailTableLabel(c.detailTable) + '：请填写「' + f.label + '」');
          }),
        );
      }
    }
    if (msgs.length) msg = msgs[0];
  }
  if (msg) {
    launchTab.value = 'form';
    proxy.$modal.msgWarning(msg);
    return false;
  }
  return true;
}

/** 解析下一步审批节点：优先走后端参与者解析接口（把节点上配置的参与人解析成具体人员） */
async function loadNextNodes() {
  nextLoading.value = true;
  nextNodes.value = [];
  try {
    const res = await request({
      url: '/flowable/participant/next/' + current.value.modelKey,
      method: 'post',
      data: { variables: formVariables() },
    });
    const nodes = (res && res.data && res.data.nodes) || [];
    if (nodes.length) {
      await setNextNodes(nodes.map(toNextNode));
    } else {
      await loadNextNodesFromXml();
    }
  } catch (e) {
    // 接口不可用（旧后端 / 流程未部署）或解析失败：回退到 BPMN 解析，均失败则不阻塞提交
    try {
      await loadNextNodesFromXml();
    } catch (e2) {
      nextNodes.value = [];
    }
  } finally {
    nextLoading.value = false;
  }
}

/** 后端返回的节点 → 弹窗展示结构 */
function toNextNode(n) {
  const people = (n.people || [])
    .filter((p) => p && String(p.userId || '') !== '')
    .map((p) => ({
      id: String(p.userId),
      label: p.userName || p.loginName || p.userId,
      dept: p.deptName || '',
    }));
  const dynamic = (n.unresolved || []).filter((u) => u && !people.some((p) => p.label === u));
  return {
    id: n.id,
    name: n.name || '审批节点',
    modeText: n.handleMode === 'candidate' ? '候选办理' : n.handleMode === 'runtime' ? '提交时指定' : '指定办理',
    perfText: { and: '会签', seq: '串签', claim: '抢占' }[n.performMode] || '抢占',
    people,
    dynamic,
    needPick: !!n.needPick,
    picked: [],
    userKw: '',
    deptKw: '',
    deptId: null,
    deptTree: [],
    multiDept: false,
    auto: people.length === 0 && dynamic.length === 0,
  };
}

/** 可勾选的候选办理人（提交时指定环节用） */
function pickablePeople(n) {
  return (n.people || []).filter((p) => p.id);
}

/** 当前节点可见的候选办理人（部门树筛选 + 姓名搜索） */
function pickPeople(n) {
  const kw = (n.userKw || '').trim().toLowerCase();
  let list = pickablePeople(n);
  if (n.multiDept && n.deptId) {
    const ids = collectSubtreeIds(findTreeData(n.deptTree, n.deptId));
    list = list.filter((p) => ids.includes(p.deptKey));
  }
  if (kw) list = list.filter((p) => (p.label || '').toLowerCase().includes(kw));
  return list;
}

/** 可见候选人按部门分组（单部门时用于显示部门小标题） */
function pickGroups(n) {
  const groups = [];
  pickPeople(n).forEach((p) => {
    const dept = p.dept || '未归属部门';
    let g = groups.find((x) => x.dept === dept);
    if (!g) {
      g = { dept, users: [] };
      groups.push(g);
    }
    g.users.push(p);
  });
  return groups;
}

/** 当前筛选结果里的全部候选人ID */
function visiblePickIds(n) {
  return pickPeople(n).map((p) => p.id);
}

function isAllPicked(n) {
  const ids = visiblePickIds(n);
  return ids.length > 0 && ids.every((id) => (n.picked || []).includes(id));
}

function isSomePicked(n) {
  const ids = visiblePickIds(n);
  const count = ids.filter((id) => (n.picked || []).includes(id)).length;
  return count > 0 && count < ids.length;
}

/** 全选 / 取消全选（只作用于当前搜索结果） */
function togglePickAll(n, checked) {
  const set = new Set(n.picked || []);
  visiblePickIds(n).forEach((id) => (checked ? set.add(id) : set.delete(id)));
  n.picked = Array.from(set);
}

/* ===== 提交时指定：组织树 ===== */
/** 组织全量部门树（懒加载一次，供候选跨部门时展示层级） */
const orgDeptTree = ref([]);

function ensureOrgDeptTree() {
  if (orgDeptTree.value.length) return Promise.resolve();
  // 用流程模块的组织全量部门树：不受数据范围限制，且发起人都已具备 flowable:task:complete 权限
  return getOrgPickerDepts()
    .then((res) => {
      orgDeptTree.value = (res && res.data) || [];
    })
    .catch(() => {
      orgDeptTree.value = [];
    });
}

/** 部门树扁平索引：label → {id,label,parentId} */
function indexDeptByName(nodes, parentId, map) {
  (nodes || []).forEach((nd) => {
    const label = nd.label || nd.deptName || '';
    if (label) map[label] = { id: nd.id, label, parentId };
    indexDeptByName(nd.children, nd.id, map);
  });
  return map;
}

/** 只保留有候选人的部门分支，count 为该部门（含下级）候选人数 */
function pruneDeptTree(nodes, own) {
  const out = [];
  (nodes || []).forEach((nd) => {
    const children = pruneDeptTree(nd.children, own);
    const self = own[nd.id] || 0;
    const total = self + children.reduce((acc, c) => acc + c.count, 0);
    if (self > 0 || children.length) {
      out.push({ id: nd.id, label: nd.label || nd.deptName || '', count: total, children });
    }
  });
  return out;
}

function findTreeData(nodes, id) {
  for (const nd of nodes || []) {
    if (nd.id === id) return nd;
    const hit = findTreeData(nd.children, id);
    if (hit) return hit;
  }
  return null;
}

function collectSubtreeIds(node, out) {
  const acc = out || [];
  if (!node) return acc;
  acc.push(node.id);
  (node.children || []).forEach((c) => collectSubtreeIds(c, acc));
  return acc;
}

/** 节点候选范围 → 组织树（候选跨多个部门时才展示，单部门保持平铺列表） */
function buildNodeDeptTree(n) {
  n.deptTree = [];
  n.deptId = null;
  n.multiDept = false;
  const people = pickablePeople(n);
  if (!people.length) return;
  const byName = indexDeptByName(orgDeptTree.value, null, {});
  const own = {};
  people.forEach((p) => {
    const hit = p.dept ? byName[p.dept] : null;
    p.deptKey = hit ? hit.id : 'x:' + (p.dept || '');
    if (hit) own[hit.id] = (own[hit.id] || 0) + 1;
  });
  const kinds = new Set(people.map((p) => String(p.deptKey)));
  n.multiDept = kinds.size > 1 && Object.keys(byName).length > 0;
  if (!n.multiDept) return;
  const branches = pruneDeptTree(orgDeptTree.value, own);
  n.deptTree = [{ id: 0, label: '全部人员', count: people.length, children: branches }];
}

/** 候选区左侧组织树实例（每个节点一个，按节点ID存放） */
const orgTreeRefs = {};

function setOrgTreeRef(n, el) {
  if (el) orgTreeRefs[n.id] = el;
  else delete orgTreeRefs[n.id];
}

function filterOrgNode(value, data) {
  return !value || (data.label || '').indexOf(value) !== -1;
}

function filterOrgTree(n, value) {
  const tree = orgTreeRefs[n.id];
  if (tree && tree.filter) tree.filter(value);
}



/** 兜底：取当前流程定义 BPMN，从开始事件沿出口找到首个（或并行多个）用户任务 */
async function loadNextNodesFromXml() {
  const defRes = await listDefinition({ modelKey: current.value.modelKey });
  const def = (defRes.rows || [])[0];
  if (!def || !def.modelId) return;
  const xmlRes = await request({ url: '/flowable/definition/xml/' + def.modelId, method: 'get' });
  // 后端 success(String) 重载会把 XML 放进 msg（而非 data），两种都兼容
  const raw = (xmlRes && (xmlRes.data || xmlRes.msg)) || '';
  const xml = typeof raw === 'string' && raw.indexOf('<') === 0 ? raw : '';
  if (!xml) return;
  await setNextNodes(parseNextNodes(xml));
}

/** 写入弹窗节点，并按候选部门情况生成组织树（单部门不建树） */
async function setNextNodes(nodes) {
  nextNodes.value = nodes;
  if (!nodes.some((n) => n.needPick && pickablePeople(n).length > 1)) return;
  await ensureOrgDeptTree();
  nodes.forEach(buildNodeDeptTree);
}

function parseNextNodes(xml) {
  let doc;
  try {
    doc = new DOMParser().parseFromString(xml, 'text/xml');
  } catch (e) {
    return [];
  }
  if (!doc || doc.getElementsByTagName('parsererror').length) return [];
  const byId = {};
  Array.from(doc.getElementsByTagName('*')).forEach((el) => {
    const id = el.getAttribute && el.getAttribute('id');
    if (id) byId[id] = el;
  });
  const flows = Array.from(doc.getElementsByTagName('sequenceFlow'));
  const start = doc.getElementsByTagName('startEvent')[0];
  if (!start) return [];
  const tasks = [];
  const seen = new Set();
  const walk = (el, depth) => {
    if (!el || depth > 8) return;
    const id = el.getAttribute('id');
    if (!id || seen.has(id)) return;
    seen.add(id);
    const tag = el.tagName || el.nodeName;
    if (tag === 'userTask') {
      tasks.push(el);
      return;
    }
    if (tag === 'endEvent' || tag === 'subProcess') return;
    // 网关等中间节点：沿出口继续向下找（并行网关会产出多个下一节点）
    flows
      .filter((f) => f.getAttribute('sourceRef') === id)
      .forEach((f) => {
        walk(byId[f.getAttribute('targetRef')], depth + 1);
      });
  };
  flows
    .filter((f) => f.getAttribute('sourceRef') === start.getAttribute('id'))
    .forEach((f) => {
      walk(byId[f.getAttribute('targetRef')], 1);
    });
  return tasks.map(describeNode);
}

/** BPMN 用户任务 → 展示用节点信息（名称/办理方式/办理人） */
function describeNode(el) {
  const attr = (n) => el.getAttribute(n) || '';
  let cfg = {};
  try {
    cfg = JSON.parse(attr('flowable:participantConfig') || '{}') || {};
  } catch (e) {
    cfg = {};
  }
  const candidateGroups = attr('flowable:candidateGroups');
  const handleMode = cfg.handleMode || (candidateGroups ? 'candidate' : 'assign');
  const performMode = cfg.performMode || 'claim';
  const people = [];
  const dynamic = [];
  const push = (label) => {
    if (!label) return;
    if (people.some((p) => p.label === label)) return;
    people.push({ id: '', label, dept: '' });
  };
  const assignee = attr('flowable:assignee');
  if (assignee) push(assignee === '${initiator}' ? '发起人本人' : userLabel(assignee), 'user');
  (attr('flowable:candidateUsers') || '')
    .split(',')
    .filter(Boolean)
    .forEach((id) => push(userLabel(id)));
  (candidateGroups || '')
    .split(',')
    .filter(Boolean)
    .forEach((g) => {
      if (!dynamic.includes(g)) dynamic.push(g);
    });
  (cfg.participants || []).forEach((p) => {
    if (p.type === 'user' && p.id) push(userLabel(p.id));
    else {
      const label = p.name || p.type || '按组织关系自动匹配';
      if (!dynamic.includes(label)) dynamic.push(label);
    }
  });
  return {
    id: attr('id'),
    name: attr('name') || '审批节点',
    modeText: handleMode === 'candidate' ? '候选办理' : handleMode === 'runtime' ? '提交时指定' : '指定办理',
    perfText: { and: '会签', seq: '串签', claim: '抢占' }[performMode] || '抢占',
    people,
    dynamic,
    needPick: false,
    picked: [],
    userKw: '',
    deptKw: '',
    deptId: null,
    deptTree: [],
    multiDept: false,
    auto: people.length === 0 && dynamic.length === 0,
  };
}

/** 用户ID → 姓名（取不到时返回原值） */
function userLabel(id) {
  const hit = (userOptions.value || []).find((u) => String(u.userId) === String(id));
  return hit ? hit.nickName || hit.userName || String(id) : String(id);
}

function confirmSubmit() {
  // 后续任务还在解析时先不提交，避免漏掉「提交时指定」的环节
  if (nextLoading.value) {
    proxy.$modal.msgWarning('正在获取下一环节办理人，请稍候再发送');
    return;
  }
  submitting.value = true;
  const detailData = collectDetailData();
  // 明细属性校验：必须填写明细（mustAdd）→ 至少一行且必填列已填（网格模式经实例校验）
  if (gridRenderRef.value && gridRenderRef.value.validateDetails) {
    const msgs = gridRenderRef.value.validateDetails();
    if (msgs.length) {
      proxy.$modal.msgWarning(msgs[0]);
      submitting.value = false;
      return;
    }
  }
  const variables = formVariables();
  if (Object.keys(detailData).length) variables.detailData = detailData;
  // 下一环节「提交时指定」：把选定的人写进变量，后端监听器创建任务时采纳
  for (const n of nextNodes.value || []) {
    if (!n.needPick) continue;
    if (pickablePeople(n).length && !(n.picked || []).length) {
      proxy.$modal.msgWarning('请为「' + n.name + '」选择办理人');
      submitting.value = false;
      return;
    }
    if ((n.picked || []).length) variables['nextAssignee_' + n.id] = n.picked.slice();
  }
  const title = submitTitle();
  launch({
    definitionKey: current.value.modelKey,
    formId: boundForm.value ? boundForm.value.formId : null,
    title,
    variables,
  })
    .then((res) => {
      // 后端返回 { instanceId }（历史版本把实例ID塞在 msg 里，这里两种都兼容）
      const instanceId = (res && res.data && res.data.instanceId) || '';
      // 已经正式发起，草稿不再需要（失败不影响发起结果）
      if (draftId.value) {
        delDraft(draftId.value).catch(() => {});
        draftId.value = null;
        clearDraftQuery();
      }
      submitOpen.value = false;
      resetAfterLaunch();
      // 独立发起页保留结果条：万一浏览器拦下关闭标签页，用户回到页面还能看到单号与详情入口
      if (isStandaloneLaunch.value) {
        launchedInfo.value = instanceId ? { instanceId, title } : null;
        // 回执优先显示流程编号（发起时由后端按编号规则生成），取不到再退回实例ID
        if (instanceId) {
          request({ url: '/flowable/process/' + instanceId, method: 'get' })
            .then((r) => {
              const no = r && r.data && r.data.processNo;
              if (no && launchedInfo.value && launchedInfo.value.instanceId === instanceId) launchedInfo.value.processNo = no;
            })
            .catch(() => {});
        }
      }
      // 仿照：不再只弹一句 toast，而是把流程图摆出来让用户确认流程走向
      openResultDialog(instanceId, title);
    })
    .catch(() => {
      // 提交失败停留在填写页，错误提示由 request 统一给出
    })
    .finally(() => {
      submitting.value = false;
    });
}

/**
 * 发起成功后把本页恢复成「全新一单」：清主表值、清明细行，标题回到默认值。
 * 不清的话，用户在同一页再次提交会把上一次的内容又发一遍（也正是"表单里怎么还有值"的来源）。
 */
function resetAfterLaunch() {
  dynData.value = {};
  detailRows.value = {};
  const grid = gridRenderRef.value;
  if (grid && grid.setDetailData) {
    const tables = ((layoutGrid.value && layoutGrid.value.cells) || []).filter((c) => c.type === 'detail' && c.detail && c.detail.table).map((c) => c.detail.table);
    if (tables.length) {
      const blank = {};
      tables.forEach((t) => {
        blank[t] = [];
      });
      grid.setDetailData(blank);
    }
  }
  applyDefaultTitle();
  launchTab.value = 'form';
}

/** 结果条「查看流程详情」：新标签页打开详情页（不冲掉当前发起页，方便继续发起） */
function openLaunchedDetail() {
  if (!launchedInfo.value) return;
  const url = router.resolve({
    path: '/flow/detail/create',
    query: { instanceId: launchedInfo.value.instanceId, defName: current.value.modelName || '', title: launchedInfo.value.title || '' },
  }).href;
  window.open(url, '_blank');
}

/* ===== 提交成功回执：流程图 + 当前环节（仿照表单） ===== */

/** 从 BPMN XML 里按 ID 反查节点名称（会签/并行时当前停留节点可能有多个） */
function nodeNamesById(xml, ids) {
  if (!xml || !ids || !ids.length) return [];
  try {
    const doc = new DOMParser().parseFromString(xml, 'text/xml');
    if (!doc || doc.getElementsByTagName('parsererror').length) return [];
    const map = {};
    Array.from(doc.getElementsByTagName('*')).forEach((el) => {
      const id = el.getAttribute && el.getAttribute('id');
      if (id) map[id] = el.getAttribute('name') || '';
    });
    return ids.map((id) => map[id]).filter(Boolean);
  } catch (e) {
    return [];
  }
}

/** 回执「当前环节」：已办结按结束展示；否则取当前停留节点名，取不到再退回提交前解析出的下一环节 */
const resultStatusText = computed(() => {
  if (resultInfo.value.finished) return '流程已办结';
  const names = nodeNamesById(resultDiagram.value.xml, resultDiagram.value.activeIds);
  if (names.length) return names.join('、');
  const next = (nextNodes.value || []).map((n) => n.name).filter(Boolean);
  return next.length ? next.join('、') : '';
});

/** 打开回执弹窗：先出壳（流程图加载中），再拉实例流程图数据填入 */
function openResultDialog(instanceId, title) {
  resultInfo.value = {
    instanceId: instanceId || '',
    title: title || '',
    processName: current.value.modelName || '',
    finished: false,
  };
  resultDiagram.value = { xml: '', doneIds: [], activeIds: [], doneFlowIds: [], nodes: {} };
  resultOpen.value = true;
  // 列表内发起时先收起全屏发起弹窗，回执独立一层显示在列表之上
  if (!isStandaloneLaunch.value) open.value = false;
  if (!instanceId) {
    // 极端情况没拿到实例ID：退回发起前的流程预览，至少让用户看清流程走向
    resultDiagram.value = { xml: previewDiagramXml.value || '', doneIds: [], activeIds: [], doneFlowIds: [], nodes: {} };
    return;
  }
  request({ url: '/flowable/process/diagramData/' + instanceId, method: 'get' })
    .then((res) => {
      const d = (res && res.data) || {};
      resultInfo.value.finished = !!d.finished;
      resultDiagram.value = {
        // 定义缺图时退回发起前预览，避免回执里一片空白
        xml: d.xml || previewDiagramXml.value || '',
        doneIds: d.doneIds || [],
        activeIds: d.activeIds || [],
        doneFlowIds: d.doneFlowIds || [],
        nodes: d.nodes || {},
      };
    })
    .catch(() => {
      resultDiagram.value = { xml: previewDiagramXml.value || '', doneIds: [], activeIds: [], doneFlowIds: [], nodes: {} };
    });
}

/** 回执「查看流程详情」：新标签页打开详情，不打断回执 */
function openResultDetail() {
  const info = resultInfo.value;
  if (!info.instanceId) return;
  const url = router.resolve({
    path: '/flow/detail/create',
    query: { instanceId: info.instanceId, defName: info.processName || '', title: info.title || '' },
  }).href;
  window.open(url, '_blank');
}

/**
 * 回执「确认」：关掉回执并关闭本页（提交闭环）。
 * - 独立发起页由列表页 window.open 打开，window.close() 可直接生效；
 * - 被浏览器拦下（例如手动粘贴地址打开）时退到待办列表，不停在已提交的空表单上；
 * - 列表内发起的场景只关回执，页面本身继续用。
 */
function confirmResult() {
  resultOpen.value = false;
  if (!isStandaloneLaunch.value) return;
  window.close();
  setTimeout(() => {
    if (!window.closed) router.replace('/flow/todo');
  }, 150);
}

/** 是否独立发起页（新标签页打开：/flow/launch/create?key=xxx） */
const isStandaloneLaunch = computed(() => route.path === '/flow/launch/create');

if (isStandaloneLaunch.value) {
  // 独立发起页：直接加载表单详情，不展示流程列表
  // 先立起引导态，避免首个 await 之前的那一帧仍是纯白页面
  launchBooting.value = true;
  initLaunchFromRoute();
} else {
  // 列表页：加载可发起流程
  getList();
}
</script>

<script>
import { defineComponent, h, resolveComponent } from 'vue';
import { ElSelect, ElOption, ElRadioGroup, ElRadio, ElCheckbox, ElCheckboxGroup, ElSwitch, ElRate, ElInput, ElInputNumber, ElDatePicker } from 'element-plus';
import FormBrowse from '@/components/FormBrowse/index.vue';

/** 动态字段控件：按 rtype 渲染对应 Element 控件（布局样式渲染共用） */
export const DynField = defineComponent({
  name: 'DynField',
  props: {
    f: { type: Object, required: true },
    modelValue: { type: [String, Number, Boolean], default: undefined },
    /** 浏览按钮伴生值：已选 ID（逗号分隔），仅用于弹框回显（兼容数字 ID 与数组） */
    modelIds: { type: [String, Number, Array], default: '' },
    /** 浏览按钮附加信息（人员带回部门） */
    modelExtra: { type: Object, default: null },
  },
  emits: ['update:modelValue', 'update:modelIds', 'update:modelExtra'],
  setup(props, { emit }) {
    const set = (v) => emit('update:modelValue', v);
    const setId = (v) => emit('update:modelIds', v);
    const setExtra = (v) => emit('update:modelExtra', v);
    return () => {
      const f = props.f;
      const v = props.modelValue;
      switch (f.rtype) {
        case 'select':
          return h(ElSelect, { modelValue: v, 'onUpdate:modelValue': set, style: 'width:100%', placeholder: f.placeholder || '请选择' }, () =>
            (f.optionList || []).map((o) => h(ElOption, { key: o.value, label: o.label, value: o.value })),
          );
        case 'radio':
          return h(ElRadioGroup, { modelValue: v, 'onUpdate:modelValue': set }, () => (f.optionList || []).map((o) => h(ElRadio, { key: o.value, value: o.value }, () => o.label)));
        case 'editor':
          return h(resolveComponent('Editor'), { modelValue: v || '', 'onUpdate:modelValue': set, minHeight: f.editorHeight || 140 });
        case 'checkbox':
          return h(
            ElCheckboxGroup,
            {
              modelValue: v ? String(v).split(',').filter(Boolean) : [],
              'onUpdate:modelValue': (arr) => set((arr || []).join(',')),
            },
            () => (f.optionList || []).map((o) => h(ElCheckbox, { key: o.value, value: o.value }, () => o.label)),
          );
        case 'check':
          return h(ElCheckbox, { modelValue: v, 'onUpdate:modelValue': set, trueValue: '1', falseValue: '0' }, () => '是');
        case 'switch':
          return h(ElSwitch, { modelValue: v, 'onUpdate:modelValue': set, activeValue: '1', inactiveValue: '0' });
        case 'rate':
          return h(ElRate, { modelValue: v, 'onUpdate:modelValue': set });
        // 浏览按钮：按字段配置的数据源打开对应的弹框选择器（与网格表单共用 FormBrowse）
        case 'browse':
          return h(FormBrowse, {
            f,
            modelValue: v,
            modelIds: props.modelIds,
            modelExtra: props.modelExtra,
            'onUpdate:modelValue': set,
            'onUpdate:modelIds': setId,
            'onUpdate:modelExtra': setExtra,
          });
        case 'attach':
          return h(resolveComponent('FileUpload'), { modelValue: v, 'onUpdate:modelValue': set, limit: f.attachLimit || 5, fileSize: 50 });
        case 'textarea':
          return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, type: 'textarea', rows: f.rows || 4, placeholder: f.placeholder });
        case 'number':
          return h(ElInputNumber, { modelValue: v, 'onUpdate:modelValue': set, controls: false, style: 'width:100%', placeholder: f.placeholder || '请输入' });
        case 'date':
          return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'date', valueFormat: 'YYYY-MM-DD', placeholder: f.placeholder || '选择日期', style: 'width:100%' });
        case 'datetime':
          return h(ElDatePicker, {
            modelValue: v,
            'onUpdate:modelValue': set,
            type: 'datetime',
            valueFormat: 'YYYY-MM-DD HH:mm:ss',
            placeholder: f.placeholder || '选择日期时间',
            style: 'width:100%',
          });
        default:
          return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, placeholder: f.placeholder || '请输入' });
      }
    };
  },
});
export default { name: 'FlowLaunch', components: { 'dyn-field': DynField } };
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

/* ===== 独立发起页引导态（加载中 / 加载失败）：撑满视口居中，杜绝白板 ===== */
.launch-boot {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 84px);
  padding: 24px;
  background: #f8fafc;
}
.launch-boot .boot-card {
  width: 100%;
  max-width: 420px;
  padding: 28px 24px;
  text-align: center;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}
.launch-boot .boot-icon {
  font-size: 30px;
  color: var(--brand-600, #4f46e5);
  margin-bottom: 12px;
}
.launch-boot .boot-error .boot-icon {
  color: #ef4444;
}
.launch-boot .boot-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}
.launch-boot .boot-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
  word-break: break-all;
}
.launch-boot .boot-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}
.launch-boot .boot-ops {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

/* 系统只读字段（流程编号等）：纯文本回显，不带输入框边框 */
.dyn-ro {
  color: #334155;
  font-size: 13px;
  line-height: 32px;
}

/* 分组模式（无网格布局时的回退渲染）的必填星号：
 * 星号跟随标签、随「已填写/已清空」显隐（泛微口径），不用 Element 自带的常驻星号 */
.dyn-label {
  display: inline-flex;
  align-items: baseline;
}
.dyn-req {
  color: #f56c6c;
  font-style: normal;
  font-weight: 700;
  margin-left: 2px;
}

/* 简易模式顶部流程名称（表单标题）：居中大字，与预览保持一致 */
.dyn-form-title {
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  padding: 2px 0 12px;
}
.dyn-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  border-left: 3px solid var(--brand-600, #4f46e5);
  padding-left: 8px;
  margin: 6px 0 12px;
}
.dyn-note {
  background: #fffbeb;
  border: 1px dashed #fcd34d;
  border-radius: 6px;
  color: #b45309;
  font-size: 12px;
  padding: 6px 10px;
  margin-bottom: 4px;
  line-height: 1.5;
}
.dyn-cell-wrap {
  border-radius: 4px;
  padding: 4px 8px;
  margin-bottom: 4px;
}

/* ===== 运行时明细表块（表单运行态） ===== */
.dyn-detail-block {
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
  margin: 4px 0 10px;
}
.dyn-detail-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  padding: 5px 10px;
}
.dyn-detail-name {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}
.dyn-detail-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.dyn-detail-table th {
  background: #fef3c7;
  color: #92400e;
  padding: 6px 8px;
  border: 1px solid #fde68a;
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
}
.dyn-detail-table td {
  border: 1px solid #eef2f7;
  padding: 4px 6px;
}
.dyn-detail-sum-row td {
  background: #f8fafc;
}
.dyn-detail-norow td {
  padding: 12px 10px;
  text-align: center;
  color: #94a3b8;
  font-size: 12.5px;
  background: #fbfcfe;
}
.dyn-detail-empty {
  padding: 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

.flowable-launch-page .cat-groups {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  padding: 16px 20px 20px;
}
.flowable-launch-page .cat-card {
  border: 1px solid var(--ink-200);
  border-radius: var(--r-lg);
  overflow: hidden;
  background: #fff;
  box-shadow: var(--shadow-card);
}
.flowable-launch-page .cat-top {
  height: 4px;
}
.flowable-launch-page .c-blue {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
}
.flowable-launch-page .c-green {
  background: linear-gradient(90deg, #10b981, #34d399);
}
.flowable-launch-page .c-amber {
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
}
.flowable-launch-page .c-red {
  background: linear-gradient(90deg, #ef4444, #f87171);
}
.flowable-launch-page .c-violet {
  background: linear-gradient(90deg, #8b5cf6, #a78bfa);
}
.flowable-launch-page .cat-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--ink-100);
}
.flowable-launch-page .cat-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: var(--brand-50);
  color: var(--brand-600);
  display: grid;
  place-items: center;
}
.flowable-launch-page .cat-name {
  font-weight: 600;
  color: var(--ink-900);
  flex: 1;
}
.flowable-launch-page .cat-count {
  font-size: 12px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--ink-100);
  color: var(--ink-500);
}
.flowable-launch-page .cat-list {
  padding: 6px 8px;
}
.flowable-launch-page .flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: background 0.15s var(--ease-out);
}
.flowable-launch-page .flow-item:hover {
  background: var(--brand-50);
}
.flowable-launch-page .flow-glyph {
  color: var(--brand-500);
  flex-shrink: 0;
}
.flowable-launch-page .flow-name {
  flex: 1;
  font-size: 14px;
  color: var(--ink-700);
}
.flowable-launch-page .flow-ver {
  font-size: 12px;
  color: var(--ink-400);
}
.flowable-launch-page .flow-go {
  color: var(--ink-300);
}
.flowable-launch-page .flow-item:hover .flow-go {
  color: var(--brand-600);
}
@media (max-width: 1200px) {
  .flowable-launch-page .cat-groups {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* ===== 全屏发起页：顶部（标题 + 页签 + 操作按钮） + 内容区 ===== */
.launch-header {
  padding: 0 4px;
}

/* 第一行：标题左、操作按钮右 */
.launch-titlebar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 2px 0 10px;
}
.launch-titlebar .lt-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.launch-titlebar .lt-glyph {
  width: 4px;
  height: 18px;
  background: var(--brand-600, #4f46e5);
  border-radius: 2px;
  flex: none;
}
.launch-titlebar .lt-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
  white-space: nowrap;
}
.launch-titlebar .lt-ver {
  font-size: 12px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--brand-50, #eef2ff);
  color: var(--brand-700, #4338ca);
  flex: none;
}
.launch-titlebar .lt-form {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--ink-500, #64748b);
  padding-left: 10px;
  border-left: 1px solid var(--ink-200, #e2e8f0);
  white-space: nowrap;
}
.launch-titlebar .lt-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: none;
}

/* 第二行：页签行（下划线风格，与流程详情组件口径一致） */
.launch-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  border-bottom: 1px solid var(--ink-200, #e2e8f0);
}
.launch-tab {
  position: relative;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 9px 16px;
  font-size: 14px;
  color: var(--ink-500, #64748b);
  transition: color 0.15s var(--ease-out, cubic-bezier(0.16, 0.84, 0.44, 1));
}
.launch-tab:hover {
  color: var(--brand-600, #4f46e5);
}
.launch-tab.is-active {
  color: var(--brand-600, #4f46e5);
  font-weight: 600;
}
.launch-tab.is-active::after {
  content: '';
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: -1px;
  height: 2px;
  background: var(--brand-600, #4f46e5);
  border-radius: 2px;
}

/* 内容区：卡片式，铺满剩余高度 */
.launch-body {
  padding: 10px 20px 24px;
  background: #fff;
  min-height: 100%;
}

/* 顶部页签与「申请表单」卡片间距固定 10px：去掉弹窗头部默认下内边距，间距只由 .launch-body 控制 */
:global(.launch-dialog .el-dialog__header) {
  padding-bottom: 0;
}
:global(.launch-dialog .el-dialog__body) {
  padding: 0;
}

/* 提交成功结果条 */
.launch-done {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  font-size: 13px;
  color: #166534;
}
.launch-done .ld-icon {
  color: #16a34a;
  font-size: 16px;
}
.launch-done .ld-text {
  font-weight: 600;
}
.launch-done .ld-no {
  color: #15803d;
  font-family: ui-monospace, Menlo, monospace;
  font-size: 12px;
}
.launch-done .ld-ops {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}
.launch-pane {
  width: 100%;
}
.launch-card {
  background: #fff;
  border: 1px solid var(--ink-200, #e2e8f0);
  border-radius: var(--r-lg, 14px);
  box-shadow: var(--shadow-card, 0 1px 2px rgba(15, 23, 42, 0.04));
  overflow: hidden;
  margin-bottom: 14px;
}
.launch-card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 11px 16px;
  border-bottom: 1px solid var(--ink-100, #f1f5f9);
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
}
.launch-card-head .lc-icon {
  color: var(--brand-600, #4f46e5);
}
.launch-card-body {
  padding: 16px;
}

/* 流程图预览 */
.diagram-wrap {
  width: 100%;
}
.diagram-wrap :deep(.wm-diagram) {
  min-height: 360px;
}

.resource-count {
  margin-left: auto;
  color: var(--ink-400, #94a3b8);
  font-size: 12px;
  font-weight: 400;
}
.resource-link {
  color: var(--brand-600, #4f46e5);
  text-decoration: none;
}
.resource-link:hover {
  text-decoration: underline;
}

/* ===== 提交审批弹窗：后续任务（下一步审批人） ===== */
/* dialog 外框 teleport 到 body，scoped 命中不到，需 :global */
:global(.submit-dialog) {
  border-radius: 12px;
  overflow: hidden;
  max-width: 94vw;
}
:global(.submit-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 16px 20px 14px;
  border-bottom: 1px solid #f1f5f9;
}
:global(.submit-dialog .el-dialog__headerbtn) {
  top: 14px;
  right: 14px;
}
:global(.submit-dialog .el-dialog__body) {
  padding: 16px 20px 8px;
}
:global(.submit-dialog .el-dialog__footer) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 16px;
  border-top: 1px solid #f1f5f9;
}
.sd-header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: move;
  user-select: none;
}
.sd-header-icon {
  width: 34px;
  height: 34px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 17px;
}
.sd-header-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
  line-height: 20px;
}
.sd-header-sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
  line-height: 17px;
}
.sd-section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.sd-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
}
.sd-section-count {
  padding: 0 8px;
  border-radius: 10px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  line-height: 18px;
}
.sd-loading {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f8fafc;
  color: var(--ink-500, #64748b);
  font-size: 13px;
}
.sd-node {
  border: 1px solid #e8eef7;
  border-radius: 10px;
  background: #fbfcfe;
  padding: 12px 14px;
  margin-bottom: 10px;
}
.sd-node:last-child {
  margin-bottom: 0;
}
.sd-node-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sd-node-step {
  width: 20px;
  height: 20px;
  flex: none;
  border-radius: 50%;
  background: #2563eb;
  color: #fff;
  font-size: 12px;
  line-height: 20px;
  text-align: center;
}
.sd-node-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
}
.sd-node-mode {
  display: inline-flex;
  gap: 6px;
  margin-left: auto;
}
.sd-people {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}
.sd-people-label {
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}
.sd-person {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px 2px 3px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid #e2e8f0;
  font-size: 14px;
  color: var(--ink-900, #0f172a);
  line-height: 20px;
}
.sd-auto {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding: 7px 10px;
  border-radius: 6px;
  background: #f8fafc;
  color: var(--ink-400, #94a3b8);
  font-size: 12px;
  line-height: 18px;
}
.sd-pick {
  margin-top: 10px;
  padding: 10px 12px 12px;
  border: 1px solid #e8eef7;
  border-radius: 8px;
  background: #fff;
}
.sd-pick-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}
.sd-pick-title {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}
.sd-pick-count {
  font-size: 13px;
  color: var(--ink-400, #94a3b8);
}
.sd-pick-count b {
  color: #2563eb;
  font-weight: 600;
}
.sd-pick-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
/* 多部门：组织树 + 人员列表两栏 */
.sd-pick-body {
  display: flex;
  gap: 12px;
}
.sd-org {
  width: 235px;
  flex: none;
  min-width: 0;
  padding-right: 10px;
  border-right: 1px solid #f1f5f9;
}
.sd-org-tree {
  margin-top: 6px;
  height: 216px;
  overflow: auto;
  padding-right: 2px;
}
.sd-org-tree :deep(.el-tree-node__content) {
  height: 30px;
  border-radius: 4px;
}
.sd-org-tree :deep(.el-tree-node__label) {
  font-size: 14px;
}
.sd-org-node {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-width: 0;
  padding-right: 4px;
}
.sd-org-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}
.sd-org-count {
  flex: none;
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}
.sd-users {
  flex: 1;
  min-width: 0;
}
.sd-pick-search {
  flex: 1;
  min-width: 0;
}
.sd-cand {
  max-height: 216px;
  overflow: auto;
  padding-right: 2px;
}
.sd-cand-group + .sd-cand-group {
  margin-top: 8px;
}
.sd-cand-dept {
  font-size: 13px;
  line-height: 22px;
  color: var(--ink-400, #94a3b8);
}
.sd-cand-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 2px 8px;
}
/* 组织树模式：右侧两列展示，充分利用加宽后的空间 */
.sd-cand.is-tree .sd-cand-list {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 2px 12px;
}
.sd-cand-list :deep(.el-checkbox) {
  margin-right: 0;
  height: 30px;
  min-width: 0;
}
.sd-cand-list :deep(.el-checkbox__label) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--ink-900, #0f172a);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sd-cand-avatar {
  width: 20px;
  height: 20px;
  flex: none;
  border-radius: 50%;
  background: var(--brand-600, #4f46e5);
  color: #fff;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.sd-cand-none {
  padding: 8px 0;
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}
.sd-pick-empty {
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}
.sd-empty {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;
  color: var(--ink-400, #94a3b8);
  font-size: 13px;
}
.sd-footer-tip {
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}

/* ===== 提交成功回执：流程图（仿照，提交后确认流程走向再关页） ===== */
/* dialog 外框由 Element Plus 内部渲染并 teleport 到 body，scoped 选择器命中不到，只能用 :global */
:global(.result-dialog) {
  max-width: 94vw;
}
:global(.result-dialog .el-dialog__header) {
  padding: 16px 20px 12px;
  border-bottom: 1px solid #f1f5f9;
}
:global(.result-dialog .el-dialog__body) {
  padding: 16px 20px 6px;
}
:global(.result-dialog .el-dialog__footer) {
  padding: 10px 20px 16px;
}
.rs-header {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.rs-header .rs-icon {
  color: #16a34a;
  font-size: 20px;
  flex: none;
}
.rs-header .rs-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
  flex: none;
}
.rs-header .rs-sub {
  font-size: 13px;
  color: var(--ink-500, #64748b);
  padding-left: 10px;
  border-left: 1px solid var(--ink-200, #e2e8f0);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rs-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 28px;
  padding: 10px 14px;
  margin-bottom: 12px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
}
.rs-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  min-width: 0;
}
.rs-meta-item .rs-k {
  color: #15803d;
  flex: none;
}
.rs-meta-item .rs-v {
  color: var(--ink-900, #0f172a);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rs-meta-item .rs-mono {
  font-family: ui-monospace, Menlo, monospace;
  font-size: 12px;
  font-weight: 500;
}
.rs-meta-item .rs-current {
  color: #b45309;
}
.rs-diagram {
  min-height: 220px;
}
.rs-diagram :deep(.wm-diagram) {
  border-radius: 10px;
}

@media (max-width: 900px) {
  .launch-titlebar {
    flex-wrap: wrap;
  }
}
</style>
