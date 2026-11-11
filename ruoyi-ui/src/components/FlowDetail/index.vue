<template>
  <el-dialog
    :model-value="open"
    fullscreen
    append-to-body
    :show-close="false"
    :class="['rd-dialog', 'flow-detail-dialog', { 'fd-standalone': standalone }]"
    @update:model-value="(v) => emit('update:open', v)"
  >
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </div>
        <span class="rd-detail-header-title">流程详情</span>
        <div class="rd-detail-header-sub" v-if="detailData.processDefinitionName">
          <div class="rd-detail-header-divider"></div>
          <span class="rd-detail-header-no">{{ detailData.processDefinitionName }}（{{ detailData.instanceId }}）</span>
        </div>
        <slot name="header-extra"></slot>
        <!-- 独立详情页：弹窗自带头部是唯一出口，关闭按钮放这里，不再另占一条底部操作条 -->
        <div class="rd-detail-header-ops" v-if="standalone">
          <el-button size="small" icon="Close" @click="emit('update:open', false)">关 闭</el-button>
        </div>
      </div>
    </template>

    <div class="detail-tabs">
      <button class="detail-tab" :class="{ 'is-active': detailTab === 'form' }" @click="detailTab = 'form'">流程表单</button>
      <button class="detail-tab" :class="{ 'is-active': detailTab === 'diagram' }" @click="detailTab = 'diagram'">流程图</button>
      <button class="detail-tab" :class="{ 'is-active': detailTab === 'status' }" @click="detailTab = 'status'">流程状态</button>
      <button class="detail-tab" :class="{ 'is-active': detailTab === 'resource' }" @click="detailTab = 'resource'">相关资源</button>
    </div>

    <!-- 页签：流程表单 -->
    <div class="rd-page" v-show="detailTab === 'form'">
      <section class="rd-card">
        <div class="rd-card-body" style="display: block">
          <FormGridRender v-if="gridLayout" :grid="gridLayout" :fields="formFields" :values="detailData.variables" />
          <template v-else-if="formGroups.length">
            <template v-for="(grp, gi) in formGroups" :key="gi">
              <div class="form-section-title" v-if="grp.title">{{ grp.title }}</div>
              <div class="rd-grid">
                <div class="rd-item" v-for="f in grp.fields" :key="f.field" :style="f._span === 24 ? '' : ''">
                  <span class="rd-label">{{ f.label }}</span>
                  <div class="rd-value">{{ formatValue(f, detailData.variables?.[f.field]) }}</div>
                </div>
              </div>
            </template>
          </template>
          <div class="rd-grid" v-else>
            <div class="rd-item" v-for="(v, k) in detailData.variables || {}" :key="k">
              <span class="rd-label">{{ k }}</span>
              <div class="rd-value">{{ v ?? '-' }}</div>
            </div>
            <div class="rd-empty" v-if="!hasVariables">
              <p class="rd-empty-text">{{ emptyFormText }}</p>
            </div>
          </div>
        </div>
      </section>
      <!-- 审批意见：签批意见表（节点 / 办理人 / 动作 / 时间 / 意见 / 环节附件 + 抄送） -->
      <FlowOpinion :tasks="historyTasks" :attachments="attachList" :cc-list="ccList" :loading="statusLoading" />
    </div>

    <!-- 页签：流程图（独立详情页下卡片铺满剩余高度，不再留出下方空白） -->
    <div class="rd-page rd-page--diagram" v-show="detailTab === 'diagram'">
      <section class="rd-card">
        <div class="rd-card-body rd-card-body--diagram">
          <div class="diagram-wrap">
            <!-- 与发起页/后台设计器同一套 bpmn-js 实时渲染，按实例状态高亮（图例由组件自带） -->
            <wm-process-diagram
              v-if="detailTab === 'diagram'"
              :xml="diagramData.xml"
              :done-ids="diagramData.doneIds"
              :active-ids="diagramData.activeIds"
              :done-flow-ids="diagramData.doneFlowIds"
              :node-info="diagramData.nodes"
              :height="560"
              :fill="standalone"
              :empty-text="diagramEmptyText"
            />
          </div>
        </div>
      </section>
    </div>

    <!-- 页签：流程状态（统计概览 + 当前节点 + 流程记录，统一由 FlowStatus 承载） -->
    <div class="fs-host" v-show="detailTab === 'status'">
      <FlowStatus :tasks="historyTasks" :loading="statusLoading" />
    </div>

    <!-- 页签：相关资源（流程过程中各节点上传的附件汇总） -->
    <div class="rd-page" v-show="detailTab === 'resource'">
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title"><span class="rd-card-icon">▤</span>相关资源</div>
          <span class="resource-count" v-if="attachList.length">共 {{ attachList.length }} 个附件</span>
        </div>
        <div class="rd-card-body" style="display: block">
          <el-table :data="attachList" border size="small" empty-text="流程过程中未上传附件">
            <el-table-column label="附件名称" min-width="240">
              <template #default="scope">
                <a class="resource-link" :href="fileHref(scope.row.fileUrl)" target="_blank" rel="noopener">{{ scope.row.fileName || '附件' }}</a>
                <span class="resource-size" v-if="fileSizeText(scope.row.fileSize)">{{ fileSizeText(scope.row.fileSize) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="所属节点" min-width="140" align="center">
              <template #default="scope">{{ scope.row.taskName || '-' }}</template>
            </el-table-column>
            <el-table-column label="上传人" width="120" align="center">
              <template #default="scope">{{ scope.row.uploaderName || '-' }}</template>
            </el-table-column>
            <el-table-column label="上传时间" width="170" align="center">
              <template #default="scope">{{ scope.row.createTime || '-' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </section>
    </div>

    <template #footer v-if="!standalone">
      <el-button @click="emit('update:open', false)">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="FlowDetail">
import request from '@/utils/request';
import FormGridRender from '@/components/FormGridRender.vue';
import WmProcessDiagram from '@/components/WmProcessDiagram.vue';
import FlowStatus from '@/components/FlowStatus/index.vue';
import FlowOpinion from '@/components/FlowOpinion/index.vue';
import { listFlowAttach } from '@/api/flowable/attach';
import { listCcByInstance } from '@/api/flowable/cc';
import { getToken } from '@/utils/auth';
import { parseLayout } from '@/views/flowable/form/designer/converters';

const props = defineProps({
  open: { type: Boolean, default: false },
  instanceId: { type: String, default: '' },
  /* 附件列过滤：业务表单生成的数据表中，哪些列属于附件（由调用方按流程传） */
  attachmentFields: { type: Array, default: () => [] },
  /**
   * 独立详情页模式：弹窗即页面，正文纵向铺满视口、流程图卡片吃掉剩余高度，
   * 关闭按钮收进头部、不占底部操作条（页面本身不再画一条重复的标题栏）。
   */
  standalone: { type: Boolean, default: false },
});
const emit = defineEmits(['update:open']);

const detailData = ref({});
const historyTasks = ref([]);
const statusLoading = ref(false);
/** 环节附件（审批意见表 + 相关资源页签共用） */
const attachList = ref([]);
/** 抄送记录（审批意见表底部「抄送」） */
const ccList = ref([]);
const diagramData = ref({ xml: '', doneIds: [], activeIds: [], doneFlowIds: [], nodes: {} });
const detailTab = ref('form');
const formFields = ref([]);
const formLayout = ref({ sections: [] });
/** 网格布局（mode=grid 时走 FormGridRender 只读回显） */
const gridLayout = ref(null);

/** 表单展示分组：按 layoutJson 分组；无布局时整体单组展示 */
const formGroups = computed(() => {
  const all = formFields.value;
  if (!all.length) return [];
  const sections = formLayout.value.sections || [];
  if (!sections.length) return [{ title: '', fields: all }];
  const byField = {};
  all.forEach((f) => {
    byField[f.field] = f;
  });
  const used = new Set();
  const groups = [];
  for (const sec of sections) {
    const fields = (sec.rows || [])
      .flat()
      .filter((c) => c && c.field && byField[c.field])
      .map((c) => {
        used.add(c.field);
        return byField[c.field];
      });
    if (fields.length) groups.push({ title: sec.title || '', fields });
  }
  const rest = all.filter((f) => !used.has(f.field));
  if (rest.length) groups.push({ title: sections.length ? '其他信息' : '', fields: rest });
  return groups;
});

/** 流程图空态文案：接口未返回图 / 流程定义缺少图形段时显示 */
const diagramEmptyText = computed(() => {
  if (!props.instanceId) return '暂无流程图（缺少流程实例标识）';
  return '暂无流程图（流程定义缺少图形信息，请在设计器重新保存并部署）';
});

/** 是否存在流程变量（有表单数据） */
const hasVariables = computed(() => {
  const v = detailData.value.variables;
  return !!(v && Object.keys(v).length);
});

/**
 * 表单页签空态文案（精准区分三种情况）：
 * 1. 流程定义无绑定表单 → 该流程未绑定表单
 * 2. 已绑定表单但本实例无数据 → 该实例发起时未填写表单
 * 3. 其他（接口返回异常等） → 暂无表单数据
 */
const emptyFormText = computed(() => {
  if (formFields.value.length) {
    // 说明流程已绑定表单（拿到了字段定义），只是本实例没有数据
    return '该实例发起时未填写表单';
  }
  if (detailData.value.processDefinitionKey) {
    // 有流程定义键但没解析到字段：表单未绑定或表单未设计字段
    return '该流程未绑定表单（或表单尚未设计字段）';
  }
  return '暂无表单数据';
});

/* select 选项转中文 */
function formatValue(field, val) {
  if (val === undefined || val === null || val === '') return '-';
  if (field.type === 'select' && Array.isArray(field.options)) {
    return field.options.includes(val) ? val : val;
  }
  return val;
}

/**
 * 加载绑定表单：优先按流程定义 Key 查询（/flowable/form/byProcess）；
 * 若实例未返回 processDefinitionKey（个别历史实例），回退用当前任务节点 key 兜底查询，
 * 避免「申请表单」空白。
 */
async function loadFormFields(processKey) {
  formFields.value = [];
  formLayout.value = { sections: [] };
  gridLayout.value = null;
  let key = processKey;
  if (!key && props.instanceId) {
    key = await resolveProcessKeyByTask(props.instanceId);
  }
  if (!key) return;
  try {
    const res = await request({ url: '/flowable/form/byProcess/' + key, method: 'get' });
    const form = res.data;
    if (form && form.fieldsJson) {
      formFields.value = JSON.parse(form.fieldsJson) || [];
    }
    if (form && form.layoutJson) {
      const p = parseLayout(form.layoutJson);
      if (p.mode === 'grid' && p.grid) gridLayout.value = p.grid;
      else formLayout.value = { sections: p.sections };
    }
  } catch (e) {}
}

/** 兜底：通过当前待办任务的 fieldPerms 接口（其内部按流程定义回查表单）取流程定义 Key */
async function resolveProcessKeyByTask(instanceId) {
  try {
    const t = await request({ url: '/flowable/task/todoList', method: 'get', params: { pageNum: 1, pageSize: 200 } });
    const hit = (t.rows || []).find((r) => r.instanceId === instanceId);
    return hit ? hit.processDefinitionKey : '';
  } catch (e) {
    return '';
  }
}

async function load() {
  if (!props.instanceId) return;
  detailData.value = {};
  historyTasks.value = [];
  attachList.value = [];
  ccList.value = [];
  diagramData.value = { xml: '', doneIds: [], activeIds: [], doneFlowIds: [], nodes: {} };
  formFields.value = [];
  formLayout.value = { sections: [] };
  gridLayout.value = null;
  detailTab.value = 'form';
  request({ url: '/flowable/process/' + props.instanceId, method: 'get' }).then((res) => {
    detailData.value = res.data || {};
    loadFormFields(detailData.value.processDefinitionKey);
  });
  statusLoading.value = true;
  request({ url: '/flowable/process/history/' + props.instanceId, method: 'get' })
    .then((res) => {
      historyTasks.value = res.data || [];
    })
    .finally(() => {
      statusLoading.value = false;
    });
  // 环节附件 + 抄送：供审批意见表展示「环节附件」「抄送人」与「相关资源」页签
  listFlowAttach(props.instanceId)
    .then((res) => {
      attachList.value = res.data || [];
    })
    .catch(() => {
      attachList.value = [];
    });
  listCcByInstance(props.instanceId)
    .then((res) => {
      ccList.value = res.data || [];
    })
    .catch(() => {
      ccList.value = [];
    });
  // 流程图：取 BPMN XML + 已流转/当前节点，前端 bpmn-js 实时渲染（不再取 PNG 位图）
  request({ url: '/flowable/process/diagramData/' + props.instanceId, method: 'get' })
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
    });
}

/* 附件访问地址（<a> 无法携带 Authorization，通过 token 参数访问） */
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

/**
 * 打开即加载：
 * - 列表页弹窗入口：open 由 false → true，监听触发；
 * - 独立详情页入口：挂载时 open 已为 true，@open 不会再次触发，需 immediate 立即拉取。
 */
watch(
  () => [props.open, props.instanceId],
  ([open, instanceId]) => {
    if (open && instanceId) load();
  },
  { immediate: true },
);
</script>

<style lang="scss" scoped>
.detail-tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--rd-border, #e5e7eb);
  margin-bottom: 12px;
  .form-section-title {
    font-size: 14px;
    font-weight: 600;
    color: #0f172a;
    border-left: 3px solid var(--el-color-primary);
    padding-left: 8px;
    margin: 8px 0 4px;
  }
  .detail-tab {
    background: none;
    border: none;
    padding: 10px 16px;
    font-size: 14px;
    color: var(--rd-muted, #6b7280);
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: color 0.15s;
    &:hover {
      color: var(--el-color-primary);
    }
    &.is-active {
      color: var(--el-color-primary);
      font-weight: 600;
      border-bottom-color: var(--el-color-primary);
    }
  }
}
.resource-empty {
  text-align: center;
  color: var(--rd-muted, #9ca3af);
  font-size: 13px;
  padding: 14px 0 4px;
}
.resource-count {
  font-size: 12px;
  color: var(--rd-muted, #6b7280);
}
.resource-link {
  color: var(--el-color-primary);
  text-decoration: none;
}
.resource-link:hover {
  text-decoration: underline;
}
.resource-size {
  margin-left: 6px;
  font-size: 12px;
  color: var(--rd-muted, #9ca3af);
}
/* 流程状态页签：横向铺开（统计卡 + 两张宽表），不再受 rd-page 的 840px 收窄限制 */
.fs-host {
  padding-top: 2px;
}
.diagram-wrap {
  min-height: 120px;
}

/* ===== 独立详情页：一张卡片铺满全屏 =====
 * 弹窗自身是 display:block，正文不会撑满视口，流程图卡片只有 460px 高，下方全空；
 * 这里把弹窗改成纵向 flex，正文吃掉剩余高度，流程图页签的卡片（及其画布）一路撑到底。
 * 注意：.el-dialog 根节点由 Element Plus 渲染、不承载本组件的作用域属性，
 * 因此这三条必须用 :global 命中，写在 scoped 里会静默失效。 */
:global(.flow-detail-dialog.fd-standalone) {
  display: flex;
  flex-direction: column;
}
:global(.flow-detail-dialog.fd-standalone .el-dialog__header) {
  flex: none;
}
:global(.flow-detail-dialog.fd-standalone .el-dialog__body) {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: auto;
}
/* 头部右侧的关闭出口（独立页唯一出口，替代原来的底部操作条） */
.flow-detail-dialog .rd-detail-header-ops {
  margin-left: auto;
  flex: none;
}

/* 各页签都是弹窗正文的 flex 子项：全局 .rd-page 的 margin:0 auto 会让它们缩成内容宽度
 * （相关资源、流程状态页签会突然变窄），这里统一取消自动外边距并拉满宽度 */
.flow-detail-dialog.fd-standalone .rd-page,
.flow-detail-dialog.fd-standalone .fs-host {
  width: 100%;
  margin-left: 0;
  margin-right: 0;
}

.flow-detail-dialog.fd-standalone .rd-page--diagram {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
}
.flow-detail-dialog.fd-standalone .rd-page--diagram .rd-card {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
}
.flow-detail-dialog.fd-standalone .rd-page--diagram .rd-card-body--diagram {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.flow-detail-dialog.fd-standalone .rd-page--diagram .diagram-wrap {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.flow-detail-dialog.fd-standalone .rd-page--diagram .diagram-wrap > * {
  flex: 1 1 auto;
  min-height: 0;
}
</style>
