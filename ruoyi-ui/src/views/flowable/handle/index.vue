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
          <template v-for="m in menuList" :key="m">
            <el-button v-if="m === 'submit'" type="primary" icon="Check" :loading="submitting" v-hasPermi="['flowable:task:complete']" @click="submitApprove('approved')">同 意</el-button>
            <el-button v-else-if="m === 'reject'" type="danger" icon="CloseBold" :loading="submitting" v-hasPermi="['flowable:task:complete']" @click="submitApprove('rejected')">驳 回</el-button>
            <el-button v-else-if="m === 'transfer'" icon="Switch" :loading="acting" v-hasPermi="['flowable:task:complete']" @click="pickUser('transfer')">转 办</el-button>
            <el-button v-else-if="m === 'addSign'" icon="EditPen" :loading="acting" v-hasPermi="['flowable:task:complete']" @click="pickUser('addSign')">加 签</el-button>
            <el-button v-else-if="m === 'cc'" icon="Promotion" :loading="acting" v-hasPermi="['flowable:task:complete']" @click="pickUser('cc')">抄 送</el-button>
            <el-button v-else-if="m === 'attach'" icon="Paperclip" @click="openAttachDialog">附 件</el-button>
            <el-button v-else-if="m === 'print'" icon="Printer" @click="doPrint">打 印</el-button>
            <el-button v-else-if="m === 'terminate'" type="danger" plain icon="CircleClose" :loading="acting" v-hasPermi="['flowable:task:complete']" @click="doTerminate">终 止</el-button>
          </template>
        </div>
      </div>
      <div class="hd-tabs">
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'form' }" @click="activeTab = 'form'">流程表单</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'diagram' }" @click="switchTab('diagram')">流程图</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'status' }" @click="switchTab('status')">流程状态</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'resource' }" @click="switchTab('resource')">
          相关资源<span class="hd-tab-badge" v-if="attachList.length">{{ attachList.length }}</span>
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
              <FormGridRender v-if="gridLayout" ref="gridRenderRef" :grid="gridLayout" :fields="formFields" :values="formData" :editable="nodeFormMode" />
              <el-form v-else :model="formData" label-width="130px" size="default" class="hd-form">
                <el-row :gutter="0">
                  <el-col v-for="f in visibleFields" :key="f.field" :span="columnSpan(f)">
                    <el-form-item :label="f.label">
                      <dyn-field v-if="f._perm === 'edit'" :f="f" v-model="formData[f.field]" v-model:model-ids="formData[f.field + '__id']" />
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
            <div class="rd-card-title"><span class="rd-card-icon">✎</span>签字意见</div>
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

      <!-- ===== 页签：相关资源（流程附件汇总） ===== -->
      <div class="hd-pane" v-show="activeTab === 'resource'">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">▣</span>相关资源</div>
            <el-button v-if="showAttachMenu" type="primary" plain size="small" icon="Upload" v-hasPermi="['flowable:task:complete']" @click="openAttachDialog">上传附件</el-button>
          </div>
          <div class="rd-card-body" style="display: block">
            <div class="table-wrap">
              <el-table :data="attachList" v-loading="attachLoading" border size="small" class="app-table">
                <el-table-column label="资源名称" min-width="240">
                  <template #default="{ row }">
                    <el-link type="primary" :href="fileHref(row.fileUrl)" target="_blank" :underline="false">
                      {{ row.fileName }}
                    </el-link>
                    <span class="hd-attach-size">{{ fileSizeText(row.fileSize) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="taskName" label="上传环节" width="140" show-overflow-tooltip />
                <el-table-column prop="uploaderName" label="上传人" width="120" />
                <el-table-column prop="createTime" label="上传时间" width="170" align="center" />
                <el-table-column label="操作" width="80" align="center">
                  <template #default="{ row }">
                    <el-button link type="danger" icon="Delete" @click="removeAttach(row)">删除</el-button>
                  </template>
                </el-table-column>
                <template #empty>
                  <div class="rd-empty"><p class="rd-empty-text">暂无附件，可通过右上「上传附件」补充流程资料</p></div>
                </template>
              </el-table>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- 提交时指定下一环节办理人（节点取人方式=提交时指定时弹出） -->
    <el-dialog v-model="nextPickOpen" title="指定下一环节办理人" width="560px" append-to-body :close-on-click-modal="false" class="rd-dialog">
      <div v-if="nextLoading" class="np-loading">正在解析下一环节…</div>
      <div v-for="n in nextPickNodes" :key="n.id" class="np-node">
        <div class="np-head">
          <span class="np-name">{{ n.name }}</span>
          <span class="np-mode">{{ n.perfText }}</span>
        </div>
        <el-checkbox-group v-if="n.people.length" v-model="n.picked" class="np-range">
          <el-checkbox v-for="p in n.people" :key="p.id" :value="p.id">{{ p.name }}</el-checkbox>
        </el-checkbox-group>
        <div v-else class="np-empty">系统未解析出具体人员，提交后按节点配置自动指派</div>
      </div>
      <div class="np-tip">该环节设置了「提交时指定」：从系统解析出的范围内选人，选定的人直接成为下一环节办理人（不会超出配置范围）。</div>
      <template #footer>
        <el-button size="small" @click="nextPickOpen = false">取 消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="confirmNextPick">确认并提交</el-button>
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
import { completeTask, delegateTask, addSignTask, terminateTask, sendTaskCc, getReturnNodes, getTaskPrintTemplate } from '@/api/flowable/flow';
import { listFlowAttach, addFlowAttach, delFlowAttach } from '@/api/flowable/attach';
import { listCcByInstance } from '@/api/flowable/cc';
import { listOpinionPresets } from '@/api/flowable/opinionPreset';
import { DynField } from './dyn-field';
import { normalizeHandleField, handleDisplayValue } from './field-norm';
import FormGridRender from '@/components/FormGridRender.vue';
import WmProcessDiagram from '@/components/WmProcessDiagram.vue';
import FlowStatus from '@/components/FlowStatus/index.vue';
import FlowOpinion from '@/components/FlowOpinion/index.vue';
import FlowOpinionPreset from '@/components/FlowOpinionPreset/index.vue';
import FlowPrintView from '@/components/FlowPrintView/index.vue';
import UserPicker from '@/components/UserPicker/index.vue';
import { parseLayout } from '@/views/flowable/form/designer/converters';
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

/** 表单字段（节点字段权限：edit 可编辑 / readonly 只读 / hidden 不渲染） */
const formFields = ref([]);
const formData = ref({});

/** 当前节点使用节点表单模板（字段编辑/只读由模板网格属性控制，实时生效） */
const nodeFormMode = ref(false);
const visibleFields = computed(() => formFields.value.filter((f) => f._perm !== 'hidden'));
const hasEditFields = computed(() => nodeFormMode.value || formFields.value.some((f) => f._perm === 'edit'));
function displayValue(v) {
  return handleDisplayValue(v);
}

/** 表单设计器布局（grid 模式 → 与「流程发起」页同一渲染口径，展示后台 HTML 表单） */
const gridLayout = ref(null);
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
/** 节点未配置操作菜单时的默认菜单（保持原有办理页体验，并补上「抄送」「打印」） */
const DEFAULT_MENUS = ['submit', 'reject', 'transfer', 'cc', 'print'];
const opMenus = ref([]);
const menuConfigured = computed(() => opMenus.value.length > 0);
const menuList = computed(() => (menuConfigured.value ? opMenus.value : DEFAULT_MENUS));

/* ===== 签字意见：节点级开关 + 与表单字段去重 ===== */
/** 节点是否启用签字意见（设计器节点属性「签字意见」开关，默认启用） */
const signOpinionEnabled = ref(true);

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
/** 附件上传入口：未配置时默认可用（相关资源页签） */
const showAttachMenu = computed(() => !menuConfigured.value || opMenus.value.includes('attach'));

/* ===== 人员选择（转办 / 加签 / 抄送） ===== */
const userPickerRef = ref(null);
const userPickMode = ref('transfer');
const userPickTitle = computed(() => {
  if (userPickMode.value === 'transfer') return '转办：选择接收人';
  if (userPickMode.value === 'addSign') return '加签：选择加签人';
  return '抄送：选择接收人（可多选）';
});

/* ===== 流程附件（相关资源） ===== */
const attachList = ref([]);
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
    // 发起人（申请）节点不受节点字段权限配置约束：未配权限的字段默认按“可编辑”处理，
    // 否则退回发起人后申请人改不了表单，等于白退。
    const editableAll = !!d.editableAll;
    // 节点操作菜单：设计器勾选的按钮，未配置（空数组）时前端回退默认菜单
    opMenus.value = Array.isArray(d.opMenus) ? d.opMenus : [];
    // 节点「签字意见」开关：后端未返回（老流程未配置）时跟随操作菜单——未配置菜单视为全默认，含签字意见
    signOpinionEnabled.value = typeof d.signOpinion === 'boolean' ? d.signOpinion : !opMenus.value.length || opMenus.value.includes('comment');
    formFields.value = fields.map((f) => normalizeHandleField(f, resolvePerm(f, perms, editableAll)));
    const data = {};
    for (const f of formFields.value) data[f.field] = variables[f.field];
    // 明细表数据随流程变量走：交给 FormGridRender 按 values.detailData 回填明细行
    // （不放进去的话，办理页/详情页的明细表永远是空白行）
    data.detailData = variables.detailData;
    formData.value = data;
    // 与「流程发起」页统一：解析表单设计器布局，grid 模式用 FormGridRender 还原后台 HTML 表单
    const parsed = parseLayout(d.layoutJson);
    gridLayout.value = parsed.mode === 'grid' && parsed.grid ? parsed.grid : null;
  } catch (e) {
    formFields.value = [];
    formData.value = {};
    gridLayout.value = null;
    nodeFormMode.value = false;
    opMenus.value = [];
    signOpinionEnabled.value = true;
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
  // 可编辑节点：先校验明细表必填列（明细行不在 el-form 模型里，需单独校验）
  if (nodeFormMode.value && gridRenderRef.value && gridRenderRef.value.validateDetails) {
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
  proxy.$modal
    .confirm('确认同意该任务？')
    .then(() => {
      // 同意：先看下一环节有没有设置「提交时指定」
      return fetchNextNodes().then((nodes) => {
        const need = (nodes || []).filter((n) => n.needPick);
        if (need.length) {
          nextNodes.value = nodes;
          pendingOutcome.value = outcome;
          submitting.value = false;
          nextPickOpen.value = true;
          return null;
        }
        return doComplete(outcome).then((res) => ({ res, outcome }));
      });
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

/** 解析下一环节节点与候选范围（失败不阻塞办理） */
function fetchNextNodes() {
  nextLoading.value = true;
  return request({ url: '/flowable/participant/nextByTask/' + task.value.taskId, method: 'post' })
    .then((res) =>
      ((res && res.data && res.data.nodes) || []).map((n) => ({
        id: n.id,
        name: n.name || n.id,
        needPick: !!n.needPick,
        perfText: { and: '会签', seq: '串签', claim: '抢占' }[n.performMode] || '抢占',
        people: (n.people || []).map((p) => ({ id: String(p.userId), name: p.userName || p.loginName || p.userId })),
        picked: [],
      })),
    )
    .catch(() => [])
    .finally(() => {
      nextLoading.value = false;
    });
}

/** 确认选人并提交 */
function confirmNextPick() {
  const target = nextPickNodes.value;
  const missing = target.find((n) => n.people.length && !n.picked.length);
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

/** 办理完成后的统一处理（加签回流 / 正常提示） */
function handleCompleteResult(res, outcome, returnToName) {
  // 加签（委派）任务：办理后回到原办理人，流程未推进
  if (res && res.delegated) {
    proxy.$modal.msgSuccess(res.msg || '加签办理完成，任务已回到原办理人');
    backToList();
    return;
  }
  if (outcome === 'approved') {
    proxy.$modal.msgSuccess('已同意');
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
      return delegateTask(task.value.taskId, user.userId, '转办给' + name);
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

/** 返回待办列表（操作完成后当前任务已不属于自己） */
function backToList() {
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

function fileSizeText(size) {
  const n = Number(size || 0);
  if (!n) return '';
  if (n < 1024) return n + ' B';
  if (n < 1024 * 1024) return (n / 1024).toFixed(1) + ' KB';
  return (n / 1024 / 1024).toFixed(1) + ' MB';
}

loadTaskForm();
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
.hd-attach-tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
}

/* 流程状态页签：内容由 FlowStatus 组件承载，此处不再单独定义统计卡样式 */

/* ===== 提交时指定下一环节办理人 ===== */
.np-node {
  padding: 10px 0;
  border-bottom: 1px dashed #e5e7eb;
}
.np-node:last-of-type {
  border-bottom: none;
}
.np-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.np-name {
  font-weight: 600;
  color: var(--ink-900, #0f172a);
}
.np-mode {
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  border-radius: 3px;
  padding: 1px 6px;
}
.np-range {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
}
.np-empty,
.np-loading {
  color: #94a3b8;
  font-size: 13px;
}
.np-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #64748b;
  background: #f8fafc;
  border-radius: 4px;
  padding: 8px 10px;
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
