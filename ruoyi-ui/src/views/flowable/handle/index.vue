<template>
  <div class="app-container flowable-handle-page">
    <!-- 顶部：第一行标题 + 操作按钮；第二行页签（对齐发起页与开发规范第12章） -->
    <div class="hd-header">
      <div class="hd-titlebar">
        <div class="hd-title">
          <span class="hd-glyph"></span>
          <span class="hd-name">{{ task.title || task.processDefinitionName || '任务办理' }}</span>
          <span class="hd-ver" v-if="task.taskName">{{ task.taskName }}</span>
          <span class="hd-form" v-if="task.processDefinitionName">
            <el-icon><Document /></el-icon>{{ task.processDefinitionName }}
          </span>
        </div>
        <!-- 操作按钮由设计器「操作菜单」驱动：节点配置了哪些就显示哪些，未配置时回退默认菜单 -->
        <div class="hd-actions">
          <!-- 退回发起人（申请）节点：表单可编辑，提供保存（暂存修改，不提交流转） -->
          <el-button v-if="initiatorNode" icon="Finished" :loading="saving" v-hasPermi="['flowable:task:complete']" @click="onSaveTaskForm">保 存</el-button>
          <template v-for="m in menuList" :key="m">
            <el-button v-if="m === 'submit'" type="primary" icon="Check" :loading="submitting" v-hasPermi="['flowable:task:complete']" @click="submitApprove('approved')">{{ menuLabel('submit') }}</el-button>
            <el-button v-else-if="m === 'reject' && canReturn" type="danger" icon="CloseBold" :loading="submitting" v-hasPermi="['flowable:task:complete']" @click="submitApprove('rejected')">{{ menuLabel('reject') }}</el-button>
            <el-button v-else-if="m === 'transfer'" icon="Switch" :loading="acting" v-hasPermi="['flowable:task:complete']" @click="pickUser('transfer')">{{ menuLabel('transfer') }}</el-button>
            <el-button v-else-if="m === 'addSign'" icon="EditPen" :loading="acting" v-hasPermi="['flowable:task:complete']" @click="pickUser('addSign')">{{ menuLabel('addSign') }}</el-button>
            <el-button v-else-if="m === 'cc'" icon="Promotion" :loading="acting" v-hasPermi="['flowable:task:complete']" @click="pickUser('cc')">{{ menuLabel('cc') }}</el-button>
            <el-button v-else-if="m === 'attach'" icon="Paperclip" @click="openAttachDialog">附 件</el-button>
            <el-button v-else-if="m === 'print'" icon="Printer" @click="doPrint">{{ menuLabel('print') }}</el-button>
            <el-button v-else-if="m === 'terminate'" type="danger" plain icon="CircleClose" :loading="acting" v-hasPermi="['flowable:task:complete']" @click="doTerminate">{{ menuLabel('terminate') }}</el-button>
          </template>
        </div>
      </div>
      <div class="hd-tabs">
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'form' }" @click="activeTab = 'form'">流程表单</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'diagram' }" @click="switchTab('diagram')">流程图</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'status' }" @click="switchTab('status')">流程状态</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'resource' }" @click="switchTab('resource')">
          相关资源<span class="hd-tab-badge" v-if="resourceList.length">{{ resourceList.length }}</span>
        </button>
      </div>
    </div>

    <div class="hd-body" v-loading="loading">
      <!-- ===== 页签：流程表单 ===== -->
      <div class="hd-pane" v-show="activeTab === 'form'">
        <section class="rd-card" v-if="formFields.length">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">▤</span>申请表单</div>
            <span class="hd-edit-tip" v-if="hasEditFields">标亮字段可修改，修改随本次办理一并保存</span>
          </div>
          <div class="rd-card-body" style="display: block">
            <!-- 与「流程发起」页一致：按表单设计器布局（网格布局）渲染后台 HTML 表单；无布局时回退字段列表 -->
            <div class="hd-form-wrap">
              <FormGridRender v-if="gridLayout" ref="gridRenderRef" :grid="gridLayout" :fields="gridFields" :values="formData" :overrides="ruleOverrides" :rule-errors="ruleErrors" :rule-detail-errors="ruleDetailErrors" :editable="gridEditable" @detail-change="runFormRules" />
              <el-form v-else :model="formData" label-width="130px" size="default" class="hd-form">
                <el-row :gutter="0">
                  <el-col v-for="f in visibleFields" :key="f.field" :span="columnSpan(f)">
                    <el-form-item :label="f.label">
                      <dyn-field v-if="f._perm === 'edit'" :f="f" v-model="formData[f.field]" v-model:model-ids="formData[f.field + '__id']" />
                      <FileUpload v-else-if="f.rtype === 'attach' || isAttachLikeValue(formData[f.field])" :model-value="formData[f.field]" disabled :limit="f.attachLimit || 5" />
                      <span v-else class="hd-ro">{{ displayValue(formData[f.field]) }}</span>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>
          </div>
        </section>
        <section class="rd-card" v-else-if="!loading">
          <div class="rd-card-body" style="display: block">
            <div class="rd-empty">
              <p class="rd-empty-text">{{ showCommentCard ? '该节点未绑定业务表单，可直接在下方填写签字意见后办理' : '该节点未绑定业务表单' }}</p>
            </div>
          </div>
        </section>

        <!-- 签字意见（签字意见紧邻表单下方）
 显示条件：节点启用了签字意见，且表单里没有可填的「签字意见」字段；
 表单已放该字段时直接填表单里的那个，避免出现两个意见框 -->
        <section class="rd-card" v-if="showCommentCard">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">✎</span>签字意见<span v-if="signOpinionRequired" class="hd-required-mark">*</span><span v-if="signOpinionRequired" class="hd-required-tip">必填</span></div>
            <div class="hd-opinion-tools">
              <span class="hd-opinion-count" v-if="commentText">{{ commentText.length }} 字</span>
              <el-button link type="primary" size="small" :disabled="!commentText" @click="comment = ''">清 空</el-button>
            </div>
          </div>
          <div class="rd-card-body">
            <!-- 常用意见：点击追加到签字意见（「常用意见」快捷选择）
 短语支持个人自定义：本人配置优先，未配置时用系统默认（字典 flow_opinion_preset） -->
            <div class="hd-presets">
              <span class="hd-presets-label">常用意见</span>
              <button type="button" class="hd-preset" v-for="p in opinionPresets" :key="p" @click="appendOpinion(p)">{{ p }}</button>
              <button type="button" class="hd-preset-set" title="配置我的常用意见" @click="presetOpen = true">
                <el-icon><Setting /></el-icon>设置
              </button>
            </div>
            <!-- 签字意见：富文本编辑器（与「通知公告-内容」同一 Editor 组件，可调字体/字号/粗细/颜色等） -->
            <div class="rd-opinion-editor">
              <editor v-model="comment" :min-height="140" />
            </div>
          </div>
        </section>

        <!-- 审批意见：签批意见表（节点 / 办理人 / 动作 / 时间 / 意见 / 环节附件 + 抄送） -->
        <FlowOpinion :tasks="historyTasks" :attachments="attachList" :cc-list="ccList" :loading="historyLoading" />
      </div>

      <!-- ===== 页签：流程图 ===== -->
      <div class="hd-pane" v-show="activeTab === 'diagram'">
        <section class="rd-card">
          <div class="rd-card-body" style="display: block">
            <div class="hd-diagram" v-loading="diagramLoading">
              <!-- 与发起页/后台设计器同一套 bpmn-js 实时渲染（矢量、可缩放），并按实例状态高亮 -->
              <wm-process-diagram
                v-if="activeTab === 'diagram'"
                :xml="diagramData.xml"
                :done-ids="diagramData.doneIds"
                :active-ids="diagramData.activeIds"
                :done-flow-ids="diagramData.doneFlowIds"
                :node-info="diagramData.nodes"
                :height="560"
                :empty-text="diagramEmptyText"
              />
            </div>
          </div>
        </section>
      </div>

      <!-- ===== 页签：流程状态（统一由 FlowStatus 承载，与「流程详情」同口径） ===== -->
      <div class="hd-pane" v-show="activeTab === 'status'">
        <FlowStatus :tasks="historyTasks" :loading="historyLoading" />
      </div>

      <!-- ===== 页签：相关资源（表单附件 + 各环节附件汇总，不再提供上传入口） ===== -->
      <div class="hd-pane" v-show="activeTab === 'resource'">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">▣</span>相关资源</div>
          </div>
          <div class="rd-card-body" style="display: block">
            <div class="table-wrap">
              <el-table :data="resourceList" v-loading="attachLoading" border size="small" class="app-table">
                <el-table-column label="资源名称" min-width="240">
                  <template #default="{ row }">
                    <el-link type="primary" @click.prevent="handlePreview(row)" :underline="false">
                      {{ row.fileName }}
                    </el-link>
                    <span class="hd-attach-size">{{ fileSizeText(row.fileSize) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="上传人" width="120">
                  <template #default="{ row }">{{ row.uploaderName || '-' }}</template>
                </el-table-column>
                <el-table-column label="上传时间" width="170" align="center">
                  <template #default="{ row }">{{ row.createTime || '-' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="130" align="center">
                  <template #default="{ row }">
                    <span class="hd-attach-ops">
                      <el-link type="success" underline="never" @click="handlePreview(row)">预览</el-link>
                      <el-link type="primary" underline="never" @click="handleDownload(row)">下载</el-link>
                      <el-button v-if="row.attachId" link type="danger" icon="Delete" @click="removeAttach(row)">删除</el-button>
                    </span>
                  </template>
                </el-table-column>
                <template #empty>
                  <div class="rd-empty"><p class="rd-empty-text">暂无附件，表单上传的附件与各环节附件会汇总在此</p></div>
                </template>
              </el-table>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- 文件预览弹窗（相关资源页签：图片/PDF/Word/Excel/视频/文本） -->
    <file-preview ref="filePreviewRef" />

    <!-- 提交审批弹窗（与发起页同一套「后续任务」样式）：确认下一环节与办理人 -->
    <el-dialog v-model="nextPickOpen" append-to-body width="840px" draggable :close-on-click-modal="false" class="submit-dialog">
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
            <!-- 已解析出办理人（无需提交时选人）：一行紧凑展示 -->
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
        <span class="sd-footer-tip">提交后流程将流转至下一环节</span>
        <div class="sd-footer-actions">
          <el-button @click="nextPickOpen = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="confirmNextPick">确认提交</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 驳回（退回）：选择退回环节（「退回」口径——退回给已走过的环节，而不是继续往下走） -->
    <el-dialog v-model="returnOpen" title="退回：选择要退回的环节" width="600px" append-to-body :close-on-click-modal="false" class="rd-dialog">
      <div v-if="returnLoading" class="rt-loading">正在解析可退回环节…</div>
      <template v-else>
        <div v-if="!returnNodes.length" class="rt-empty">当前是第一个办理环节，没有可退回的环节。</div>
        <div v-else class="rt-list">
          <div v-for="n in returnNodes" :key="n.id" class="rt-node" :class="{ 'is-picked': returnPicked === n.id }" @click="returnPicked = n.id">
            <span class="rt-radio"></span>
            <div class="rt-main">
              <div class="rt-head">
                <span class="rt-name">{{ n.name }}</span>
                <span class="rt-tag" v-if="n.type === 'initiator'">发起人</span>
              </div>
              <div class="rt-meta">
                <span v-if="n.lastHandler">处理人：{{ n.lastHandler }}</span>
                <span v-if="n.time">{{ n.time }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="rt-tip">退回后流程回到所选环节，由该环节办理人重新办理；被退回的流程会进入对方「待办流程 - 退回」页签。</div>
      </template>
      <template #footer>
        <el-button size="small" @click="returnOpen = false">取 消</el-button>
        <el-button size="small" type="danger" :loading="submitting" :disabled="!returnNodes.length" @click="confirmReturn">确认退回</el-button>
      </template>
    </el-dialog>

    <!-- 常用意见设置（个人自定义，只影响本人） -->
    <FlowOpinionPreset v-model="presetOpen" @saved="onPresetsSaved" />

    <!-- 人员选择（转办 / 加签 / 抄送共用）
 这三件事都是「把流程交/告知组织内的人」，选人范围应覆盖全组织，
 不能受「我的数据权限」限制（否则普通员工只能选到自己） -->
    <UserPicker ref="userPickerRef" :multiple="userPickMode === 'cc'" scope="all" :title="userPickTitle" @confirm="onUserPicked" />

    <!-- 打印专用视图：屏幕不显示，window.print() 时作为唯一输出（节点打印模板 / 显示模板） -->
    <FlowPrintView v-if="printReady" :meta="printCfg.meta" :fields="printCfg.fields" :grid="printCfg.grid" :values="printCfg.values" :template-name="printCfg.templateName" />

    <!-- 附件上传 -->
    <el-dialog v-model="attachOpen" title="上传附件" width="520px" append-to-body class="rd-dialog">
      <el-upload drag multiple :show-file-list="false" :action="uploadFileUrl" :headers="uploadHeaders" :before-upload="beforeAttachUpload" :on-success="onAttachUploaded" :on-error="onAttachError">
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击选择</em></div>
      </el-upload>
      <div class="hd-attach-tip">附件按流程归档，可在「相关资源」页签查看；单个文件不超过 50MB</div>
      <template #footer>
        <el-button size="small" @click="attachOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowTaskHandle">
import request from '@/utils/request';
import { completeTask, saveTaskForm, delegateTask, addSignTask, terminateTask, sendTaskCc, getReturnNodes, getTaskPrintTemplate, getOrgPickerDepts } from '@/api/flowable/flow';
import { listFlowAttach, addFlowAttach, delFlowAttach } from '@/api/flowable/attach';
import { listCcByInstance } from '@/api/flowable/cc';
import { listOpinionPresets } from '@/api/flowable/opinionPreset';
import { DynField } from './dyn-field';
import { normalizeHandleField, handleDisplayValue } from './field-norm';
import FormGridRender from '@/components/FormGridRender.vue';
import { parseExt } from '@/views/flowable/common/form-rules/constants';
import { runRules, runFormScripts } from '@/views/flowable/common/form-rules/engine';
import FileUpload from '@/components/FileUpload/index.vue';
import WmProcessDiagram from '@/components/WmProcessDiagram.vue';
import FlowStatus from '@/components/FlowStatus/index.vue';
import FlowOpinion from '@/components/FlowOpinion/index.vue';
import FlowOpinionPreset from '@/components/FlowOpinionPreset/index.vue';
import FlowPrintView from '@/components/FlowPrintView/index.vue';
import UserPicker from '@/components/UserPicker/index.vue';
import { parseLayout, isFlowSysField } from '@/views/flowable/form/designer/converters';
import { collectFormAttachments } from '@/views/flowable/common/form-attachments';
import { useRoute, useRouter } from 'vue-router';
import { Document, UploadFilled, Setting } from '@element-plus/icons-vue';
import { getToken } from '@/utils/auth';
import { SYS_READONLY_FIELDS } from '@/utils/flow-system-fields.js';
import { htmlToText } from '@/utils';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

/** URL 参数：/flow/task/handle?taskId=xxx&taskName=yyy&title=zzz&instanceId=iii&defName=ddd */
const task = ref({
  taskId: route.query.taskId || '',
  taskName: route.query.taskName || '',
  title: route.query.title || '',
  instanceId: route.query.instanceId || '',
  processDefinitionName: route.query.defName || '',
  startUserName: route.query.startUserName || '',
  createTime: route.query.createTime || '',
});

const loading = ref(true);
const submitting = ref(false);
/** 保存（暂存表单修改）loading */
const saving = ref(false);
/** 转办/加签/抄送/终止 等操作的统一 loading */
const acting = ref(false);
const activeTab = ref('form');
const comment = ref('');

/* ===== 签字意见：常用意见快捷短语（支持个人自定义） ===== */
/** 接口也取不到时的兜底短语（正常情况后端一定返回系统默认） */
const DEFAULT_PRESETS = ['同意', '情况属实，同意办理', '同意，请领导审批', '请补充相关材料后再提交', '不同意，请修改后重新提交'];
/** 当前生效的常用意见：本人自定义优先，未自定义时后端返回系统默认 */
const opinionPresetList = ref([]);
/** 常用意见设置弹窗 */
const presetOpen = ref(false);
const opinionPresets = computed(() => (opinionPresetList.value.length ? opinionPresetList.value : DEFAULT_PRESETS));

/** 加载常用意见（/flowable/opinionPreset/list 已按「个人 → 系统默认」的优先级返回当前生效列表） */
function loadOpinionPresets() {
  listOpinionPresets()
    .then((res) => {
      const d = (res && res.data) || {};
      opinionPresetList.value = Array.isArray(d.current) ? d.current : [];
    })
    .catch(() => {
      /* 取不到时用前端兜底短语，不阻塞办理 */
    });
}

/** 设置弹窗保存 / 恢复默认后即时刷新 chips */
function onPresetsSaved(list) {
  opinionPresetList.value = Array.isArray(list) ? list.slice() : [];
}
/** 签字意见纯文本（统计字数 + 判断是否为空） */
const commentText = computed(() => htmlToText(comment.value || '').trim());
/** 点击常用意见：追加到富文本末尾（不覆盖已手写的内容） */
function appendOpinion(text) {
  if (!text) return;
  const cur = comment.value || '';
  const empty = !htmlToText(cur).trim();
  comment.value = empty ? '<p>' + text + '</p>' : cur + '<p>' + text + '</p>';
}

/** 提交时指定下一环节办理人：弹窗状态 */
const nextPickOpen = ref(false);
const nextLoading = ref(false);
const nextNodes = ref([]);
const pendingOutcome = ref('approved');

/** 驳回（退回）：可退回环节弹窗状态 */
const returnOpen = ref(false);
const returnLoading = ref(false);
const returnNodes = ref([]);
const returnPicked = ref('');
/** 当前环节之前是否还有可退回的环节：没有（如发起/申请节点）就不显示退回按钮 */
const canReturn = ref(false);

/** 表单字段（节点字段权限：edit 可编辑 / readonly 只读 / hidden 不渲染） */
const formFields = ref([]);
const formData = ref({});

// ===== 交互规则引擎（flow_form.ext_json，办理页同样生效）=====
const ruleOverrides = ref({});
/** 规则实时校验错误：{ [field]: 提示文案 }，随字段变化即时刷新（不用等提交） */
const ruleErrors = ref({});
/** 明细行规则校验错误：{ '表名|行号|字段': 提示文案 }，传 FormGridRender 行内标红 */
const ruleDetailErrors = ref({});
const activeFormRules = computed(() => (taskExtJson.value ? parseExt(taskExtJson.value).rules : []));
/** 初载标志（对标泛微「值变化才触发」机制）：
 *  打开办理页只应用字段状态（必填/显隐/只读/动态选项），不重算 derive——
 *  计算值已随流程变量落库，直接回显存量值；之后字段/明细值变化才全量触发规则 */
let formInitDone = false;
/** 节点编辑上下文门控（对标泛微「节点表单设计器内实现联动」）：
 *  发起人（申请填单）节点始终执行；普通办理节点仅在节点字段权限配置了可编辑字段时执行；
 *  节点自定义模板模式由模板单元格控制编辑/只读，规则不叠加；
 *  纯审批只读节点一律不执行——后续节点是否可编辑/必填由「节点表单设置」控制 */
function ruleEditableCtx() {
  return initiatorNode.value || (!nodeFormMode.value && formFields.value.some((f) => f._perm === 'edit'));
}
/** 高级脚本执行：phase=load/change/submit；仅发起人/可编辑节点执行（审批只读节点改值无意义） */
function runScriptsPhase(phase) {
  if (!ruleEditableCtx() || !taskExtJson.value) return '';
  const scripts = parseExt(taskExtJson.value).scripts || [];
  if (!scripts.length) return '';
  const detailData = (gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() : {}) || {};
  try {
    runFormScripts(scripts, phase, {
      get: (f) => formData.value[f],
      set: (f, v) => {
        if (formData.value[f] !== v) formData.value[f] = v;
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
function runFormRules() {
  if (!activeFormRules.value.length) {
    ruleOverrides.value = {};
    ruleErrors.value = {};
    ruleDetailErrors.value = {};
    return;
  }
  // 初载回显：只应用字段状态（required/visible/readonly/options），不执行 derive 重算——
  // 计算值（合计、大写等）已随流程变量落库，直接回显存量值；避免每次打开都全量重算浪费资源
  if (!formInitDone) {
    const result0 = runRules(
      activeFormRules.value.filter((r) => r && r.enabled !== false && r.type !== 'derive' && r.type !== 'validate' && r.type !== 'script'),
      formFields.value || [],
      formData.value || {},
      (formData.value && formData.value.detailData) || {},
      {}
    );
    ruleOverrides.value = result0.states || {};
    ruleErrors.value = {};
    ruleDetailErrors.value = {};
    return;
  }
  const detailData = (gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() : {}) || {};
  // 只读审批节点：仅执行展示型规则——derive（明细合计、大写等随明细数据回显）
  // 与 visible（字段显隐，审批人看到的界面与填写态一致）；
  // 不产生「解除只读/必填」覆盖与校验提示——是否可编辑/必填由节点表单设置控制
  if (!ruleEditableCtx()) {
    // 初载时 FormGridRender 可能尚未挂载，明细数据兜底取变量里的 detailData
    const dd = detailData && Object.keys(detailData).length ? detailData : ((formData.value && formData.value.detailData) || {});
    const result = runRules(activeFormRules.value.filter((r) => r && r.enabled !== false && (r.type === 'derive' || r.type === 'visible')), formFields.value || [], formData.value || {}, dd, {});
    ruleOverrides.value = result.states || {}; // 只含 visible 覆盖（derive 不产生 states）
    ruleErrors.value = {};
    ruleDetailErrors.value = {};
    for (const [k, v] of Object.entries(result.values || {})) {
      if (formData.value[k] !== v) formData.value[k] = v;
    }
    return;
  }
  // 可编辑节点同样要兜底：初载时 FormGridRender 尚未挂载，getDetailData() 为空，
  // derive（明细合计）会用空明细算出 0 覆盖变量里的存量值（保存后刷新合计变空就是这个原因）
  const dd2 = detailData && Object.keys(detailData).length ? detailData : ((formData.value && formData.value.detailData) || {});
  const result = runRules(activeFormRules.value, formFields.value || [], formData.value || {}, dd2, {});
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
  for (const [k, v] of Object.entries(result.values || {})) {
    if (formData.value[k] !== v) formData.value[k] = v;
  }
  // 高级脚本（字段变化时机）：仅可编辑节点，脚本写回的值经 watch 再触发一轮校验收敛
  runChangeScripts();
}
watch(
  () => formData.value,
  () => runFormRules(),
  { deep: true }
);
/** 提交前校验：可编辑节点先跑高级脚本（submit 时机可拦截），再跑规则校验。返回错误文案，无错误返回空串 */
function validateFormRules() {
  if (!ruleEditableCtx()) return '';
  const scriptErr = runScriptsPhase('submit');
  if (scriptErr) return scriptErr;
  if (!activeFormRules.value.length) return '';
  const detailData = (gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() : {}) || {};
  const { errors } = runRules(activeFormRules.value, formFields.value || [], formData.value || {}, detailData, { validate: true });
  return errors && errors.length ? (formFields.value.find((f) => f.field === errors[0].field)?.label || errors[0].field) + ' ' + errors[0].message : '';
}

/** 当前节点使用节点表单模板（字段编辑/只读由模板网格属性控制，实时生效） */
const nodeFormMode = ref(false);
const visibleFields = computed(() => formFields.value.filter((f) => f._perm !== 'hidden' && !isFlowSysField(f.field)));
const hasEditFields = computed(() => nodeFormMode.value || formFields.value.some((f) => f._perm === 'edit'));
/** 网格是否可编辑：节点表单模板由单元格属性控制；普通表单按节点字段权限控制（发起人节点默认全可编辑） */
const gridEditable = computed(() => nodeFormMode.value || hasEditFields.value);
/** 网格字段：无节点模板时把字段权限映射为 readonly，保证只读字段仍按纯文本回显 */
const gridFields = computed(() => (nodeFormMode.value ? formFields.value : formFields.value.map((f) => ({ ...f, readonly: f._perm !== 'edit' }))));
/** 附件值兜底：历史表单 rtype 丢失时，值本身是 /profile/ 文件路径（逗号分隔）也按附件回显 */
function isAttachLikeValue(v) {
  if (!v) return false;
  const s = Array.isArray(v) ? v.join(',') : String(v).trim();
  return /(^|,)\s*\/?profile\//i.test(s);
}
function displayValue(v) {
  return handleDisplayValue(v);
}

/** 表单设计器布局（grid 模式 → 与「流程发起」页同一渲染口径，展示后台 HTML 表单） */
const gridLayout = ref(null);
/** 任务表单的交互规则（ext_json）：规则引擎运行态用 */
const taskExtJson = ref('');
/** 网格实例：办理时取走明细行数据（明细不在 formFields 里，需单独回写流程变量） */
const gridRenderRef = ref(null);

/** 流转记录 / 流程图（页签数据按需加载） */
const historyTasks = ref([]);
/** 流程图：BPMN XML + 实例状态（已流转/当前停留/已流转连线） */
const diagramData = ref({ xml: '', doneIds: [], activeIds: [], doneFlowIds: [], nodes: {} });
const diagramLoading = ref(false);
const historyLoaded = ref(false);
const historyLoading = ref(false);
const diagramLoaded = ref(false);

/* ===== 节点操作菜单（设计器「操作菜单」配置 → 办理页按钮） ===== */
/** 审批节点未配置操作菜单时的默认菜单：提交、退回、打印、抄送 */
const DEFAULT_MENUS = ['submit', 'reject', 'print', 'cc'];
/** 发起人（申请）节点未配置时的默认菜单：只需提交、打印、抄送 */
const INITIATOR_MENUS = ['submit', 'print', 'cc'];
/** 当前节点是否为发起人（申请）节点：退回后由申请人重新提交 */
const initiatorNode = ref(false);
const opMenus = ref([]);
/** 按钮明细（设计器配置的自定义名称 / 默认审批意见）：key → {name, defaultOpinion} */
const opMenuDetails = ref({});
/** 按钮默认文案（未配置自定义名称时使用） */
const MENU_LABELS = { submit: '提 交', reject: '退 回', transfer: '转 办', addSign: '加 签', terminate: '终 止', print: '打 印', cc: '抄 送' };
/** 按钮显示文案：设计器配置了自定义名称就用自定义名称 */
function menuLabel(key) {
  const d = opMenuDetails.value[key];
  const name = d && d.name ? String(d.name).trim() : '';
  return name || MENU_LABELS[key] || key;
}
const menuConfigured = computed(() => opMenus.value.length > 0);
const menuList = computed(() => {
  if (menuConfigured.value) {
    if (!initiatorNode.value) return opMenus.value;
    // 申请节点只保留发起场景按钮，历史配置里若没有可用按钮则回退默认
    const list = opMenus.value.filter((m) => INITIATOR_MENUS.includes(m));
    return list.length ? list : INITIATOR_MENUS;
  }
  return initiatorNode.value ? INITIATOR_MENUS : DEFAULT_MENUS;
});

/* ===== 签字意见：节点级开关 + 与表单字段去重 ===== */
/** 节点是否启用签字意见（设计器节点属性「签字意见」开关，默认启用） */
const signOpinionEnabled = ref(true);
/** 节点是否要求签字意见必填（设计器「操作菜单」弹窗里的签字意见设置） */
const signOpinionRequired = ref(false);
/** 节点默认签字意见：进入办理页自动预填 */
const signOpinionDefault = ref('');

/** 网格布局里是否真的摆了某个字段（字段未拖入布局时，即使表单定义了也填不到） */
function gridHasField(field) {
  const cells = (gridLayout.value && gridLayout.value.cells) || [];
  return cells.some((c) => c && c.type === 'field' && c.field === field);
}

/** 表单里可见的「签字意见」字段（hidden 字段等于没放，仍然给默认意见框） */
const formSignOpinionField = computed(() => {
  const f = formFields.value.find((x) => x.field === 'signOpinion');
  if (!f || f._perm === 'hidden') return null;
  // 网格布局：必须在单元格里真的出现，否则用户看不到也填不了，仍回退默认意见框
  if (gridLayout.value && !gridHasField('signOpinion')) return null;
  return f;
});

/** 表单里的签字意见字段是否真的能填（可编辑；节点表单模板由单元格属性控制编辑/只读） */
const formSignOpinionUsable = computed(() => {
  const f = formSignOpinionField.value;
  if (!f) return false;
  return f._perm === 'edit' || !!nodeFormMode.value;
});

/**
 * 默认签字意见卡片：
 * - 节点关闭了签字意见 → 不显示；
 * - 表单里已放可填的「签字意见」字段 → 不显示（用表单字段，避免两个意见框）；
 * - 其余情况默认显示（用户不用往表单里加字段也有意见框）。
 */
const showCommentCard = computed(() => signOpinionEnabled.value && !formSignOpinionUsable.value);

/* ===== 人员选择（转办 / 加签 / 抄送） ===== */
const userPickerRef = ref(null);
const userPickMode = ref('transfer');
const userPickTitle = computed(() => {
  if (userPickMode.value === 'transfer') return '转办：选择接收人';
  if (userPickMode.value === 'addSign') return '加签：选择加签人';
  return '抄送：选择接收人（可多选）';
});

/* ===== 流程附件（相关资源） ===== */
/** 各环节上传并登记到流程实例的附件（含签字意见环节附件） */
const attachList = ref([]);
/** 统一文件预览弹窗（相关资源页签预览用） */
const filePreviewRef = ref(null);
/** 表单附件字段里已上传的文件（随流程变量保存，没有附件登记记录；上传人/时间取流程发起人 + 发起时间） */
const formAttachList = computed(() =>
  collectFormAttachments(formFields.value, formData.value, {
    uploaderName: task.value.startUserName || '',
    createTime: task.value.createTime || '',
  })
);
/** 相关资源页签展示口径：表单附件 + 各环节附件 */
const resourceList = computed(() => (attachList.value || []).concat(formAttachList.value));
const attachLoading = ref(false);
const attachOpen = ref(false);
/** 抄送记录：审批意见区底部展示「抄送」 */
const ccList = ref([]);
const uploadFileUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload';
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + getToken() }));

const diagramEmptyText = computed(() => {
  if (!task.value.instanceId) return '暂无流程图（缺少流程实例标识）';
  return '暂无流程图（流程定义缺少图形信息，请在设计器重新保存并部署）';
});

/** 系统计算字段：无论节点权限如何都只读（不在办理时由人工填）——定义见 utils/flow-system-fields.js */

/**
 * 单个字段的有效权限：
 * 节点配了就用节点配置；发起人（申请）节点未配的字段默认「可编辑」，
 * 这样「退回发起人」后申请人能改表重新提交。
 */
function resolvePerm(f, perms, editableAll) {
  const p = (perms || {})[f.field];
  if (p) return p;
  if (editableAll && !SYS_READONLY_FIELDS.includes(f.field)) return 'edit';
  return 'readonly';
}

/** 加载节点字段权限 + 绑定表单 + 当前流程变量值 + 节点操作菜单 */
async function loadTaskForm() {
  if (!task.value.taskId) {
    loading.value = false;
    return;
  }
  try {
    const res = await request({ url: '/flowable/task/fieldPerms/' + task.value.taskId, method: 'get' });
    const d = res.data || {};
    let perms = {};
    try {
      perms = JSON.parse(d.fieldPerms || '{}') || {};
    } catch (e) {}
    let fields = [];
    try {
      fields = JSON.parse(d.fieldsJson || '[]') || [];
    } catch (e) {}
    const variables = d.variables || {};
    nodeFormMode.value = !!d.nodeForm;
    taskExtJson.value = d.extJson || '';
    // 发起人（申请）节点不受节点字段权限配置约束：未配权限的字段默认按“可编辑”处理，
    // 否则退回发起人后申请人改不了表单，等于白退。
    const editableAll = !!d.editableAll;
    // 发起人（申请）节点：操作菜单只保留提交 / 打印 / 抄送
    initiatorNode.value = editableAll;
    // 节点操作菜单：设计器勾选的按钮，未配置（空数组）时前端回退默认菜单
    opMenus.value = Array.isArray(d.opMenus) ? d.opMenus : [];
    // 按钮明细：自定义名称 / 默认审批意见（key → 明细对象）
    const detailMap = {};
    if (Array.isArray(d.opMenuDetails)) {
      d.opMenuDetails.forEach((it) => {
        if (it && it.key) detailMap[it.key] = { name: it.name || '', defaultOpinion: it.defaultOpinion || '' };
      });
    }
    opMenuDetails.value = detailMap;
    // 节点「签字意见」开关：后端已按节点属性返回，未返回时默认启用
    signOpinionEnabled.value = typeof d.signOpinion === 'boolean' ? d.signOpinion : true;
    // 签字意见必填与默认意见（设计器签字意见设置）
    signOpinionRequired.value = d.signOpinionRequired === true;
    signOpinionDefault.value = (d.signOpinionDefault || '').trim();
    // 默认意见自动预填（意见框未被表单字段替代时）
    if (showCommentCard.value && signOpinionDefault.value && !comment.value) {
      comment.value = signOpinionDefault.value;
    }
    formFields.value = fields.map((f) => normalizeHandleField(f, resolvePerm(f, perms, editableAll)));
    const data = {};
    for (const f of formFields.value) data[f.field] = variables[f.field];
    // 明细表数据随流程变量走：交给 FormGridRender 按 values.detailData 回填明细行
    // （不放进去的话，办理页/详情页的明细表永远是空白行）
    data.detailData = variables.detailData;
    formData.value = data;
    // 初载本轮 watch 只应用字段状态不重算 derive，执行完（nextTick 后）再放行「值变化触发」
    formInitDone = false;
    // 与「流程发起」页统一：解析表单设计器布局，grid 模式用 FormGridRender 还原后台 HTML 表单
    const parsed = parseLayout(d.layoutJson);
    gridLayout.value = parsed.mode === 'grid' && parsed.grid ? parsed.grid : null;
    // 高级脚本（打开表单时机）：表单数据就位后执行一次（仅可编辑节点生效），
    // 同时标记初载完成——之后字段/明细值变化才全量触发交互规则
    nextTick(() => {
      formInitDone = true;
      runScriptsPhase('load');
    });
  } catch (e) {
    formInitDone = true;
    formFields.value = [];
    formData.value = {};
    gridLayout.value = null;
    nodeFormMode.value = false;
    opMenus.value = [];
    opMenuDetails.value = {};
    initiatorNode.value = false;
    signOpinionEnabled.value = true;
    signOpinionRequired.value = false;
    signOpinionDefault.value = '';
  } finally {
    loading.value = false;
  }
}

/** 加载流转记录 */
function loadHistory() {
  if (historyLoaded.value || !task.value.instanceId) return;
  historyLoaded.value = true;
  historyLoading.value = true;
  request({ url: '/flowable/process/history/' + task.value.instanceId, method: 'get' })
    .then((res) => {
      historyTasks.value = res.data || [];
    })
    .catch(() => {
      historyLoaded.value = false;
    })
    .finally(() => {
      historyLoading.value = false;
    });
}

/** 加载流程图（BPMN XML + 高亮节点，前端 bpmn-js 实时渲染） */
function loadDiagram() {
  if (diagramLoaded.value || !task.value.instanceId) return;
  diagramLoaded.value = true;
  diagramLoading.value = true;
  request({ url: '/flowable/process/diagramData/' + task.value.instanceId, method: 'get' })
    .then((res) => {
      const d = res.data || {};
      diagramData.value = {
        xml: d.xml || '',
        doneIds: d.doneIds || [],
        activeIds: d.activeIds || [],
        doneFlowIds: d.doneFlowIds || [],
        // 节点人员：节点下显示审批人 + 点击节点看已操作/未审批/已查看
        nodes: d.nodes || {},
      };
    })
    .catch(() => {
      diagramData.value = { xml: '', doneIds: [], activeIds: [], doneFlowIds: [], nodes: {} };
    })
    .finally(() => {
      diagramLoading.value = false;
    });
}

/** 记录「已查看」：供流程图「已查看人员」展示（打开办理页即算看过该节点，失败不影响办理） */
function recordNodeViewed() {
  if (!task.value.taskId) return;
  request({ url: '/flowable/process/nodeViewed/' + task.value.taskId, method: 'post' }).catch(() => {});
}

/** 页签切换：流程图/流程状态按需加载，避免首屏多余请求 */
function switchTab(tab) {
  activeTab.value = tab;
  if (tab === 'diagram') loadDiagram();
  if (tab === 'status') loadHistory();
  if (tab === 'resource') loadAttach();
}

/** 提交办理（同意/驳回）：可编辑字段值随办理一并提交 */
function submitApprove(outcome) {
  if (!task.value.taskId) {
    proxy.$modal.msgWarning('缺少任务标识，无法办理');
    return;
  }
  // 按钮级默认审批意见（设计器配置）：意见为空时点击按钮自动预填
  const menuKey = outcome === 'approved' ? 'submit' : 'reject';
  const btnDetail = opMenuDetails.value[menuKey] || {};
  const btnDefaultOpinion = (btnDetail.defaultOpinion || '').trim();
  if (showCommentCard.value && btnDefaultOpinion && !commentText.value) {
    comment.value = btnDefaultOpinion;
  }
  // 签字意见必填校验（设计器节点级设置）：默认意见框或表单意见字段为空时拦截
  if (signOpinionRequired.value) {
    const opinionEmpty = showCommentCard.value
      ? !commentText.value
      : formSignOpinionField.value && !htmlToText(formData.value.signOpinion || '').trim();
    if (opinionEmpty) {
      proxy.$modal.msgWarning('请填写签字意见');
      return;
    }
  }
  // 规则引擎校验（validate 规则）：先于明细校验，命中直接拦截
  const ruleMsg = validateFormRules();
  if (ruleMsg) {
    proxy.$modal.msgWarning(ruleMsg);
    return;
  }
  // 可编辑节点：先校验明细表必填列（明细行不在 el-form 模型里，需单独校验）
  if (gridEditable.value && gridRenderRef.value && gridRenderRef.value.validateDetails) {
    const msgs = gridRenderRef.value.validateDetails();
    if (msgs.length) {
      proxy.$modal.msgWarning(msgs[0]);
      return;
    }
  }
  // 驳回 = 退回：先选要退回到的前置环节，选定后再提交（不再往下走）
  if (outcome !== 'approved') {
    openReturnDialog();
    return;
  }
  // 与发起页一致：提交前先展示「下一环节及办理人」确认框；
  // 下一环节有「提交时指定」的节点时在这里选人，没有则仅确认后续流转
  submitting.value = true;
  fetchNextNodes()
    .then((nodes) => {
      if (nodes && nodes.length) {
        nextNodes.value = nodes;
        pendingOutcome.value = outcome;
        submitting.value = false;
        nextPickOpen.value = true;
        return null;
      }
      return doComplete(outcome).then((res) => ({ res, outcome }));
    })
    .then((ret) => {
      if (!ret) return;
      handleCompleteResult(ret.res, ret.outcome);
    })
    .catch((e) => {
      if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('办理失败：' + (e?.message || e));
    })
    .finally(() => {
      if (!nextPickOpen.value) submitting.value = false;
    });
}

/* ===== 驳回（退回）：列出当前环节之前走过的环节，选一个退回去 ===== */
/** 只有存在「当前环节之前办理过」的环节时才显示退回按钮 */
function refreshCanReturn() {
  if (!task.value.taskId) {
    canReturn.value = false;
    return;
  }
  getReturnNodes(task.value.taskId)
    .then((res) => {
      canReturn.value = ((res && res.data) || []).length > 0;
    })
    .catch(() => {
      canReturn.value = false;
    });
}

/** 打开「退回」弹窗并加载可退回环节 */
function openReturnDialog() {
  returnOpen.value = true;
  returnPicked.value = '';
  returnNodes.value = [];
  returnLoading.value = true;
  getReturnNodes(task.value.taskId)
    .then((res) => {
      const list = (res && res.data) || [];
      returnNodes.value = list;
      // 默认选中最后一项（即紧邻的上一个环节，最常见诉求）
      if (list.length) returnPicked.value = list[list.length - 1].id;
    })
    .catch((e) => {
      proxy.$modal.msgError('解析可退回环节失败：' + (e?.message || e));
    })
    .finally(() => {
      returnLoading.value = false;
    });
}

/** 确认退回：把流程退回所选环节，由该环节办理人重新办理 */
function confirmReturn() {
  const node = returnNodes.value.find((n) => n.id === returnPicked.value);
  if (!node) {
    proxy.$modal.msgWarning('请选择要退回的环节');
    return;
  }
  submitting.value = true;
  doComplete('rejected', null, node.id)
    .then((res) => {
      returnOpen.value = false;
      handleCompleteResult(res, 'rejected', node.name);
    })
    .catch((e) => {
      proxy.$modal.msgError('退回失败：' + (e?.message || e));
    })
    .finally(() => {
      submitting.value = false;
    });
}

/** 「提交时指定」弹窗里只需选人的节点 */
const nextPickNodes = computed(() => (nextNodes.value || []).filter((n) => n.needPick));

/* ===== 与发起页一致：「提交时指定」候选办理人选择 ===== */
function pickablePeople(n) {
  return (n.people || []).filter((p) => p.id);
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

function togglePickAll(n, checked) {
  const set = new Set(n.picked || []);
  visiblePickIds(n).forEach((id) => (checked ? set.add(id) : set.delete(id)));
  n.picked = Array.from(set);
}

/** 组织全量部门树（跨部门候选时展示层级） */
const orgDeptTree = ref([]);

function ensureOrgDeptTree() {
  if (orgDeptTree.value.length) return Promise.resolve();
  return getOrgPickerDepts()
    .then((res) => {
      orgDeptTree.value = (res && res.data) || [];
    })
    .catch(() => {
      orgDeptTree.value = [];
    });
}

function indexDeptByName(nodes, parentId, map) {
  (nodes || []).forEach((nd) => {
    const label = nd.label || nd.deptName || '';
    if (label) map[label] = { id: nd.id, label, parentId };
    indexDeptByName(nd.children, nd.id, map);
  });
  return map;
}

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

/** 解析下一环节节点与候选范围（失败不阻塞办理） */
function fetchNextNodes() {
  nextLoading.value = true;
  return request({ url: '/flowable/participant/nextByTask/' + task.value.taskId, method: 'post' })
    .then(async (res) => {
      const nodes = ((res && res.data && res.data.nodes) || []).map((n) => ({
        id: n.id,
        name: n.name || n.id,
        modeText: { candidate: '候选办理', runtime: '提交时指定' }[n.handleMode] || '指定办理',
        perfText: { and: '会签', seq: '串签', claim: '抢占' }[n.performMode] || '抢占',
        people: (n.people || []).map((p) => ({
          id: String(p.userId),
          label: p.userName || p.loginName || String(p.userId),
          dept: p.deptName || '',
        })),
        dynamic: [],
        needPick: !!n.needPick,
        picked: [],
        userKw: '',
        deptKw: '',
        deptId: null,
        deptTree: [],
        multiDept: false,
        auto: !(n.people || []).length,
      }));
      if (nodes.some((n) => n.needPick && pickablePeople(n).length > 1)) {
        await ensureOrgDeptTree();
        nodes.forEach(buildNodeDeptTree);
      }
      return nodes;
    })
    .catch(() => [])
    .finally(() => {
      nextLoading.value = false;
    });
}

/** 确认选人并提交 */
function confirmNextPick() {
  const target = nextPickNodes.value;
  const missing = target.find((n) => pickablePeople(n).length && !(n.picked || []).length);
  if (missing) {
    proxy.$modal.msgWarning('请为「' + missing.name + '」选择办理人');
    return;
  }
  const extra = {};
  target.forEach((n) => {
    if (n.picked.length) extra['nextAssignee_' + n.id] = n.picked.slice();
  });
  const outcome = pendingOutcome.value;
  doComplete(outcome, extra)
    .then((res) => {
      nextPickOpen.value = false;
      handleCompleteResult(res, outcome);
    })
    .catch((e) => {
      proxy.$modal.msgError('办理失败：' + (e?.message || e));
    })
    .finally(() => {
      submitting.value = false;
    });
}

/** 真正提交任务：可编辑字段 + 明细 + 下一环节指定办理人 + 退回目标节点 */
function doComplete(outcome, extra, targetActivityId) {
  submitting.value = true;
  const variables = {};
  for (const f of formFields.value) {
    if (f._perm === 'edit') variables[f.field] = formData.value[f.field];
  }
  // 明细行一并回写（未改动时原样带回，保证变量里始终是完整明细）
  const detail = gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() : null;
  if (detail && Object.keys(detail).length) variables.detailData = detail;
  // 表单里的签字意见一并写进流程变量（网格布局由单元格直接改 formData，字段权限未必是 edit）
  if (!showCommentCard.value && formSignOpinionField.value && formData.value.signOpinion !== undefined) {
    variables.signOpinion = formData.value.signOpinion;
  }
  Object.assign(variables, extra || {});
  // 签字意见来源：表单放了「签字意见」字段就用表单值，否则用默认意见框；
  // 富文本纯文本为空时按空意见提交，避免把 <p><br></p> 写进流转意见
  const opinionHtml = showCommentCard.value ? comment.value || '' : formData.value.signOpinion || '';
  const opinion = htmlToText(opinionHtml).trim() ? opinionHtml : '';
  return completeTask({
    taskId: task.value.taskId,
    comment: opinion,
    outcome,
    // 驳回（退回）：目标环节 ID，后端据此把流程 changeActivityState 退回该节点
    targetActivityId: outcome === 'rejected' ? targetActivityId : undefined,
    variables: Object.keys(variables).length ? variables : undefined,
  });
}

/** 保存（暂存表单修改，退回发起人场景）：只回写可编辑字段，不办理、不流转 */
function onSaveTaskForm() {
  const variables = {};
  for (const f of formFields.value) {
    if (f._perm === 'edit') variables[f.field] = formData.value[f.field];
  }
  // 明细行一并回写（未改动时原样带回，保证变量里始终是完整明细）
  const detail = gridRenderRef.value && gridRenderRef.value.getDetailData ? gridRenderRef.value.getDetailData() : null;
  if (detail && Object.keys(detail).length) variables.detailData = detail;
  if (!Object.keys(variables).length) {
    proxy.$modal.msgWarning('当前没有可保存的表单修改');
    return;
  }
  saving.value = true;
  saveTaskForm({ taskId: task.value.taskId, variables })
    .then(() => {
      proxy.$modal.msgSuccess('已保存，表单修改已更新到流程');
    })
    .catch((e) => {
      proxy.$modal.msgError('保存失败：' + (e?.message || e));
    })
    .finally(() => {
      saving.value = false;
    });
}

/** 办理完成后的统一处理（加签回流 / 正常提示） */
function handleCompleteResult(res, outcome, returnToName) {
  // 加签（委派）任务：办理后回到原办理人，流程未推进
  if (res && res.delegated) {
    proxy.$modal.msgSuccess(res.msg || '加签办理完成，任务已回到原办理人');
    backToList();
    return;
  }
  if (outcome === 'approved') {
    proxy.$modal.msgSuccess('已提交');
  } else {
    proxy.$modal.msgSuccess(returnToName ? '已退回至「' + returnToName + '」' : '已退回');
  }
  backToList();
}

/** 打开人员选择（转办 / 加签 / 抄送） */
function pickUser(mode) {
  if (!task.value.taskId) {
    proxy.$modal.msgWarning('缺少任务标识，无法操作');
    return;
  }
  userPickMode.value = mode;
  if (userPickerRef.value) userPickerRef.value.open();
}

/** 人员选择回调：按当前操作类型分发 */
function onUserPicked(users) {
  const list = (Array.isArray(users) ? users : [users]).filter((u) => u && u.userId);
  if (!list.length) return;
  const mode = userPickMode.value;
  if (mode === 'transfer') doTransfer(list[0]);
  else if (mode === 'addSign') doAddSign(list[0]);
  else if (mode === 'cc') doSendCc(list);
}

/** 转办：任务改派给他人办理 */
function doTransfer(user) {
  const name = user.nickName || user.userName;
  proxy.$modal
    .confirm('确认把该任务转办给「' + name + '」？转办后由对方办理，您将不再收到该任务。')
    .then(() => {
      acting.value = true;
      return delegateTask(task.value.taskId, user.userId, '');
    })
    .then(() => {
      proxy.$modal.msgSuccess('已转办给 ' + name);
      backToList();
    })
    .catch((e) => {
      if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('转办失败：' + (e?.message || e));
    })
    .finally(() => {
      acting.value = false;
    });
}

/** 加签：先由加签人办理，办完回到当前办理人 */
function doAddSign(user) {
  const name = user.nickName || user.userName;
  proxy.$modal
    .confirm('确认加签给「' + name + '」？加签人办理完成后，任务回到您这里继续办理。')
    .then(() => {
      acting.value = true;
      return addSignTask(task.value.taskId, user.userId, '加签给' + name);
    })
    .then((res) => {
      proxy.$modal.msgSuccess(res?.msg || '已加签');
      backToList();
    })
    .catch((e) => {
      if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('加签失败：' + (e?.message || e));
    })
    .finally(() => {
      acting.value = false;
    });
}

/** 抄送：写入待阅，接收人在「待办流程-待阅」查看 */
function doSendCc(users) {
  const names = users.map((u) => u.nickName || u.userName).join('、');
  proxy.$modal
    .confirm('确认将该流程抄送给「' + names + '」？被抄送人可在「待办流程-待阅」中查看。')
    .then(() => {
      acting.value = true;
      return sendTaskCc({
        taskId: task.value.taskId,
        userIds: users.map((u) => u.userId),
        comment: '办理页手动抄送',
      });
    })
    .then((res) => {
      proxy.$modal.msgSuccess(res?.msg || '已抄送给 ' + users.length + ' 人');
    })
    .catch((e) => {
      if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('抄送失败：' + (e?.message || e));
    })
    .finally(() => {
      acting.value = false;
    });
}

/** 终止：办理人强制结束流程 */
function doTerminate() {
  proxy.$modal
    .prompt('终止后流程将立即结束且无法恢复，请填写终止原因：')
    .then(({ value }) => {
      acting.value = true;
      return terminateTask(task.value.taskId, (value || '').trim() || '办理人终止');
    })
    .then(() => {
      proxy.$modal.msgSuccess('流程已终止');
      backToList();
    })
    .catch((e) => {
      if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('终止失败：' + (e?.message || e));
    })
    .finally(() => {
      acting.value = false;
    });
}

/** 打印态：打印视图是否就绪（就绪后挂载 FlowPrintView，再调 window.print） */
const printReady = ref(false);
/** 本次打印使用的模板：{ meta, fields, grid, values, templateName } */
const printCfg = ref({ meta: {}, fields: [], grid: null, values: {}, templateName: '' });

/** 安全解析 JSON（模板快照异常时回退到当前节点显示模板） */
function safeJson(text, fallback) {
  try {
    const v = JSON.parse(text || '');
    return v === null || v === undefined ? fallback : v;
  } catch (e) {
    return fallback;
  }
}

/**
 * 打印：
 * 1. 优先用节点在设计器里配置的打印模板；
 * 2. 该节点没配置则自动回退到「当前节点显示模板」（页面上正在看的表单）；
 * 3. 统一交给打印专用视图按 A4 输出，不再把页签/意见表/按钮一起打进纸面。
 */
async function doPrint() {
  if (!task.value.taskId) {
    proxy.$modal.msgWarning('缺少任务标识，无法打印');
    return;
  }
  const cfg = {
    meta: {
      processName: task.value.processDefinitionName,
      title: task.value.title,
      nodeName: task.value.taskName,
      instanceId: task.value.instanceId,
      printUser: '',
    },
    fields: formFields.value,
    grid: gridLayout.value,
    values: formData.value,
    templateName: '',
  };
  try {
    const res = await getTaskPrintTemplate(task.value.taskId);
    const d = (res && res.data) || {};
    cfg.meta = Object.assign(cfg.meta, d.meta || {});
    if (d.configured) {
      const tplLayout = safeJson(d.layoutJson, null);
      cfg.fields = safeJson(d.fieldsJson, formFields.value);
      cfg.grid = (tplLayout && tplLayout.grid) || (tplLayout && tplLayout.rows ? tplLayout : null) || gridLayout.value;
      cfg.templateName = d.templateName || '节点打印模板';
    }
  } catch (e) {
    // 模板接口异常不影响打印：继续用当前节点显示模板
  }
  printCfg.value = cfg;
  printReady.value = true;
  await nextTick();
  setTimeout(() => {
    window.print();
    setTimeout(() => {
      printReady.value = false;
    }, 300);
  }, 120);
}

/** 通知待办列表页自动刷新：写入 localStorage，列表页通过 storage 事件跨标签页感知 */
function notifyListRefresh() {
  try {
    localStorage.setItem('flow:task:changed', String(Date.now()));
  } catch (e) {
    /* 隐私模式等场景写入失败时，列表页仍有「切回标签页自动刷新」兜底 */
  }
}

/** 返回待办列表（操作完成后当前任务已不属于自己；同时通知其它标签页的列表自动刷新） */
function backToList() {
  notifyListRefresh();
  if (window.history.length > 1) router.back();
  else router.push('/flow/todo');
}

/* ===== 流程附件 ===== */
function openAttachDialog() {
  if (!task.value.instanceId) {
    proxy.$modal.msgWarning('缺少流程实例标识，无法上传附件');
    return;
  }
  attachOpen.value = true;
}

function loadAttach() {
  if (!task.value.instanceId) return;
  attachLoading.value = true;
  listFlowAttach(task.value.instanceId)
    .then((res) => {
      attachList.value = res.data || [];
    })
    .catch(() => {
      attachList.value = [];
    })
    .finally(() => {
      attachLoading.value = false;
    });
}

/** 加载抄送记录（审批意见区底部「抄送：张三、李四」） */
function loadCc() {
  if (!task.value.instanceId) return;
  listCcByInstance(task.value.instanceId)
    .then((res) => {
      ccList.value = res.data || [];
    })
    .catch(() => {
      ccList.value = [];
    });
}

function beforeAttachUpload(file) {
  if (file.size > 50 * 1024 * 1024) {
    proxy.$modal.msgWarning('单个文件不能超过 50MB');
    return false;
  }
  return true;
}

/** 上传成功后登记附件归属（文件本体已由 /common/upload 落盘） */
function onAttachUploaded(res, file) {
  if (!res || res.code !== 200) {
    proxy.$modal.msgError((res && res.msg) || '附件上传失败');
    return;
  }
  addFlowAttach({
    instanceId: task.value.instanceId,
    taskId: task.value.taskId,
    taskName: task.value.taskName,
    fileName: res.originalFilename || (file && file.name) || '附件',
    fileUrl: res.fileName,
    fileSize: (file && file.size) || 0,
  })
    .then(() => {
      proxy.$modal.msgSuccess('附件已上传');
      loadAttach();
    })
    .catch(() => {
      proxy.$modal.msgError('附件登记失败');
    });
}

function onAttachError() {
  proxy.$modal.msgError('附件上传失败，请稍后重试');
}

function removeAttach(row) {
  proxy.$modal
    .confirm('确认删除附件「' + row.fileName + '」？')
    .then(() => delFlowAttach(row.attachId))
    .then(() => {
      proxy.$modal.msgSuccess('已删除');
      loadAttach();
    })
    .catch(() => {});
}

/** 附件访问地址（<a> 无法携带 Authorization，通过 token 参数访问） */
function fileHref(url) {
  if (!url) return '';
  return import.meta.env.VITE_APP_BASE_API + '/common/file/access?resource=' + encodeURIComponent(url) + '&token=' + getToken();
}

/** 预览：图片/PDF/Word/Excel/视频/文本统一走文件预览弹窗（与流程详情相关资源一致） */
function handlePreview(row) {
  if (!row || !row.fileUrl) return;
  filePreviewRef.value?.open(row.fileUrl, row.fileName);
}

/** 下载：<a> 无法携带 Authorization，带 Token 取 Blob 后触发下载 */
function handleDownload(row) {
  if (!row || !row.fileUrl) return;
  fetch(fileHref(row.fileUrl), { headers: { Authorization: 'Bearer ' + getToken() } })
    .then((res) => {
      if (!res.ok) throw new Error('HTTP ' + res.status);
      return res.blob();
    })
    .then((blob) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = row.fileName || '附件';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      setTimeout(() => URL.revokeObjectURL(url), 200);
    })
    .catch(() => proxy.$modal.msgError('下载失败，请稍后重试'));
}

function fileSizeText(size) {
  const n = Number(size || 0);
  if (!n) return '';
  if (n < 1024) return n + ' B';
  if (n < 1024 * 1024) return (n / 1024).toFixed(1) + ' KB';
  return (n / 1024 / 1024).toFixed(1) + ' MB';
}

loadTaskForm();
refreshCanReturn();
recordNodeViewed();
loadHistory();
loadAttach();
loadCc();
loadOpinionPresets();
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

/* ===== 顶部：标题行 + 页签行（与发起页 launch-header 同一口径） ===== */
.hd-header {
  padding: 12px 20px 0;
  background: #fff;
}
.hd-titlebar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  padding: 2px 0 10px;
}
.hd-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1 1 auto;
}
.hd-glyph {
  width: 4px;
  height: 18px;
  background: var(--brand-600, #4f46e5);
  border-radius: 2px;
  flex: none;
}
.hd-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.hd-ver {
  font-size: 12px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--brand-50, #eef2ff);
  color: var(--brand-700, #4338ca);
  flex: none;
}
.hd-form {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--ink-500, #64748b);
  padding-left: 10px;
  border-left: 1px solid var(--ink-200, #e2e8f0);
  white-space: nowrap;
}
.hd-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
  flex: 0 1 auto;
}
.hd-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

/* 签字意见富文本编辑器：铺满卡片，工具栏与容器边框协调 */
.rd-opinion-editor {
  width: 100%;
}
.rd-opinion-editor :deep(.ql-toolbar.ql-snow) {
  border-color: #dbe3ec;
  border-radius: 4px 4px 0 0;
  padding: 4px 6px;
  flex-wrap: wrap;
}
.rd-opinion-editor :deep(.ql-container.ql-snow) {
  border-color: #dbe3ec;
  border-radius: 0 0 4px 4px;
  font-size: 13px;
}
.rd-opinion-editor :deep(.ql-editor) {
  line-height: 1.7;
}

/* 签字意见：右上角字数 / 清空 */
.hd-opinion-tools {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.hd-opinion-count {
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}
/* 签字意见必填标记（设计器节点级「意见必填」） */
.hd-required-mark {
  margin-left: 2px;
  color: var(--el-color-danger);
  font-weight: 600;
}
.hd-required-tip {
  margin-left: 6px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 18px;
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9, #fef0f0);
  border-radius: 3px;
}

/* 常用意见：快捷短语 chips */
.hd-presets {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}
.hd-presets-label {
  font-size: 12px;
  color: var(--ink-500, #64748b);
  line-height: 24px;
  margin-right: 2px;
  flex: none;
}
.hd-preset {
  border: 1px solid #d7e2f0;
  background: #f7fafd;
  color: #2f5c8f;
  font-size: 12px;
  line-height: 1;
  padding: 6px 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s cubic-bezier(0.16, 0.84, 0.44, 1);
}
.hd-preset:hover {
  border-color: var(--brand-600, #4f46e5);
  color: var(--brand-700, #4338ca);
  background: var(--brand-50, #eef2ff);
}
.hd-preset:active {
  transform: translateY(1px);
}
/* 常用意见设置入口：弱化为文字按钮，不与短语 chips 抢视觉 */
.hd-preset-set {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  padding: 6px 4px;
  margin-left: 2px;
  color: var(--ink-400, #94a3b8);
  border-radius: 4px;
  transition: color 0.15s;
}
.hd-preset-set:hover {
  color: var(--brand-600, #4f46e5);
}

/* 页签行：下划线风格（对齐开发规范第12章 详情页签） */
.hd-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  border-bottom: 1px solid var(--ink-200, #e2e8f0);
}
.hd-tab {
  position: relative;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 9px 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-500, #64748b);
  transition: color 0.15s cubic-bezier(0.16, 0.84, 0.44, 1);
}
.hd-tab:hover {
  color: var(--brand-600, #4f46e5);
}
.hd-tab.is-active {
  color: var(--brand-600, #4f46e5);
}
.hd-tab.is-active::after {
  content: '';
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: -1px;
  height: 2px;
  background: var(--brand-600, #4f46e5);
  border-radius: 2px;
}
.hd-tab-badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
  margin-left: 6px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: var(--brand-50, #eef2ff);
  color: var(--brand-700, #4338ca);
  text-align: center;
}

/* ===== 内容区 ===== */
.hd-body {
  padding: 14px 20px 28px;
  background: #f8fafc;
  min-height: calc(100vh - 105px);
}
.hd-pane {
  width: 100%;
}
.hd-pane :deep(.rd-page) {
  max-width: none;
}

/* 申请表单：只读值（与审批页口径一致） */
.hd-form :deep(.el-form-item__label) {
  font-size: 14px;
  color: var(--ink-700, #334155);
}
.hd-ro {
  display: block;
  color: var(--ink-900, #0f172a);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}
.hd-edit-tip {
  margin-left: auto;
  font-size: 12px;
  font-weight: 400;
  color: var(--ink-400, #94a3b8);
}

/* 流程图 */
.hd-diagram {
  padding: 4px;
  background: var(--ink-50, #f8fafc);
  border: 1px solid var(--ink-200, #e2e8f0);
  border-radius: var(--r-md, 10px);
  min-height: 320px;
}

/* 相关资源（流程附件） */
.hd-attach-size {
  margin-left: 8px;
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}
/* 相关资源操作列：预览 / 下载 / 删除 之间留间距 */
.hd-attach-ops {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.hd-attach-tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}

/* 流程状态页签：内容由 FlowStatus 组件承载，此处不再单独定义统计卡样式 */

/* ===== 提交审批弹窗（与发起页「后续任务」同一套样式） ===== */
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

/* ===== 驳回（退回）：可退回环节选择 ===== */
.rt-loading,
.rt-empty {
  padding: 22px 0;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}
.rt-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 46vh;
  overflow: auto;
}
.rt-node {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition:
    border-color 0.15s,
    background 0.15s,
    box-shadow 0.15s;
}
.rt-node:hover {
  border-color: #c7d2fe;
  background: #f8faff;
}
.rt-node.is-picked {
  border-color: var(--brand-600, #4f46e5);
  background: #f5f6ff;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.08);
}
.rt-radio {
  flex: none;
  width: 14px;
  height: 14px;
  margin-top: 3px;
  border-radius: 50%;
  border: 1px solid #cbd5e1;
  background: #fff;
  position: relative;
}
.rt-node.is-picked .rt-radio {
  border-color: var(--brand-600, #4f46e5);
}
.rt-node.is-picked .rt-radio::after {
  content: '';
  position: absolute;
  inset: 2px;
  border-radius: 50%;
  background: var(--brand-600, #4f46e5);
}
.rt-main {
  flex: 1 1 auto;
  min-width: 0;
}
.rt-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.rt-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
}
.rt-tag {
  font-size: 11px;
  line-height: 1;
  padding: 3px 6px;
  border-radius: 4px;
  background: #eff6ff;
  color: #1d4ed8;
  flex: none;
}
.rt-meta {
  margin-top: 4px;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #94a3b8;
}
.rt-tip {
  margin-top: 12px;
  font-size: 12px;
  line-height: 1.6;
  color: #64748b;
  background: #f8fafc;
  border-radius: 4px;
  padding: 8px 10px;
}

@media (max-width: 1100px) {
  .hd-title {
    flex: 1 1 100%;
  }
  .hd-actions {
    flex: 1 1 100%;
    justify-content: flex-start;
  }
}
</style>
