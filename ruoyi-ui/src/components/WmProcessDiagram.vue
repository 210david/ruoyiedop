<template>
  <div class="wm-diagram-root" :class="{ 'wm-diagram-root--fill': fill }">
    <div v-if="showLegend" class="wm-diagram-legend">
      <span v-if="showAssigneeHint" class="lg-item lg-hint">点击节点查看办理人 / 查看 / 抄送等操作明细</span>
      <span class="lg-item"><i class="lg-dot lg-active"></i>当前停留节点</span>
      <span class="lg-item"><i class="lg-dot lg-done"></i>已流转</span>
      <span class="lg-item"><i class="lg-dot lg-pending"></i>未流转</span>
      <span class="lg-item" v-if="(skippedIds || []).length"><i class="lg-dot lg-skipped"></i>干预跳过</span>
    </div>
    <!-- fill 模式高度由 flex 决定（铺满外层卡片），其余场景按内容收紧后的高度 -->
    <div class="wm-diagram" :style="fill ? null : { height: heightPx }">
      <div ref="canvasRef" class="wm-diagram-canvas"></div>
      <div v-if="state === 'loading' || state === 'empty'" class="wm-diagram-mask">{{ msg }}</div>
    </div>
  </div>
</template>

<script setup name="WmProcessDiagram">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { htmlToText } from '@/utils';
import { normalizeDiagramSizes } from '@/utils/bpmnSizes';

/**
 * 只读流程图（BPMN 渲染）
 *
 * 为什么不用后端 /diagram 的 PNG：
 * 1) Flowable 默认出图是黑白线框，与后台设计器（bpmn-js 同款配色）不一致；
 * 2) 服务端画图靠字体度量算文字位置，中文常出现空白/错位，且位图缩放后发虚；
 * 3) 位图在容器里只能按宽度铺，容易看起来偏下、不居中。
 * 这里直接用 bpmn-js NavigatedViewer 渲染 BPMN XML，配色沿用设计器那套 marker，
 * 并用 fit-viewport 自适应缩放居中，随容器尺寸变化自动重新居中。
 */
const props = defineProps({
  /** BPMN 2.0 XML 文本 */
  xml: { type: String, default: '' },
  /** 画布高度：数字按 px 处理 */
  height: { type: [Number, String], default: 440 },
  /** 无图/解析失败时的提示文案 */
  emptyText: { type: String, default: '暂无流程图，请先在设计器中绘制并部署' },
  /** 已流转节点 ID（实例图高亮：实色） */
  doneIds: { type: Array, default: () => [] },
  /** 当前停留节点 ID（实例图高亮：红框 + 外发光） */
  activeIds: { type: Array, default: () => [] },
  /** 已流转的连线 ID（实例图高亮：绿色路径） */
  doneFlowIds: { type: Array, default: () => [] },
  /** 被管理员干预跳过的节点 ID（实例图高亮：灰色虚线 + 标注「干预跳过」） */
  skippedIds: { type: Array, default: () => [] },
  /** 是否显示图例（无状态数据时自动不显示） */
  legend: { type: Boolean, default: true },
  /**
   * 自适应缩放的上限：默认 1（保持原始比例、不放大），
   * 小流程在大容器里也按真实大小渲染，避免节点被撑得过大。
   */
  maxScale: { type: Number, default: 1 },
  /**
   * 铺满容器：画布高度由外层容器决定（弹窗/全屏卡片场景），
   * 不再按内容收紧高度，否则小流程会在大容器里留出一大片空白。
   */
  fill: { type: Boolean, default: false },
  /**
   * 节点人员信息（/flowable/process/diagramData 返回的 nodes）：
   * nodeId → { nodeId, name, done:[], pending:[], viewed:[] }
   * 运行期流程图：节点下方直接显示审批人，点击节点弹浮层看已操作/未审批/已查看人员。
   */
  nodeInfo: { type: Object, default: () => ({}) },
  /** 是否在节点下方显示审批人（发起前预览无数据，自然不显示） */
  showAssignees: { type: Boolean, default: true },
});

const canvasRef = ref(null);
/** loading | ok | empty */
const state = ref('loading');
const msg = ref('流程图加载中…');
/** 画布高度：固定用传入的 height（不再按内容收紧），图形会同时按宽、高两个方向放大到铺满 */
const heightPx = computed(() => (typeof props.height === 'number' ? props.height + 'px' : props.height));
/** 有实例状态数据时才展示图例（发起前预览不展示） */
const showLegend = computed(() => props.legend && ((props.activeIds || []).length > 0 || (props.doneIds || []).length > 0));
/** 有节点人员数据时才提示「点击节点查看办理人」 */
const showAssigneeHint = computed(() => props.showAssignees && Object.keys(props.nodeInfo || {}).length > 0);

let viewer = null;
let renderToken = 0;
let resizeObserver = null;
let fitTimer = null;
// 记录上一次画布尺寸（宽高都看）：页签切换 / 弹窗动画只改高度时也要重新居中
let lastSize = '';

/** 是否有 bpmndi 图形段：缺失时 bpmn-js importXML 会抛 “no diagram to display” */
function hasDiagramInfo(xml) {
  return /<bpmndi:BPMNDiagram[\s>]/.test(xml || '');
}

/** 技术发起人节点 → 原开始事件：退回仍走 applyNode，图上只显示开始节点 */
let initiatorAliases = new Map();
let visibleNodeInfo = {};

/**
 * 只读图隐藏部署时注入的「重新提交」技术节点及其出线。
 * 该节点仅用于让 Flowable 把流程退回发起人，不属于用户设计的流程图。
 */
function prepareDiagramXml(xml) {
  initiatorAliases = new Map();
  if (typeof DOMParser === 'undefined' || typeof XMLSerializer === 'undefined') return xml;
  try {
    const doc = new DOMParser().parseFromString(xml, 'application/xml');
    if (doc.getElementsByTagName('parsererror').length) return xml;
    const elements = Array.from(doc.getElementsByTagName('*'));
    const start = elements.find((el) => el.localName === 'startEvent' && el.getAttribute('id'));
    if (!start) return xml;
    const startId = start.getAttribute('id');
    const hiddenIds = new Set();
    elements.forEach((el) => {
      if (el.localName !== 'userTask') return;
      const flag = el.getAttribute('flowable:initiatorNode') || el.getAttribute('initiatorNode');
      const id = el.getAttribute('id');
      if (id && (flag === 'true' || flag === '1')) {
        initiatorAliases.set(id, startId);
        hiddenIds.add(id);
      }
    });
    if (!hiddenIds.size) return xml;
    elements.forEach((el) => {
      if (el.localName === 'sequenceFlow' && hiddenIds.has(el.getAttribute('sourceRef'))) {
        hiddenIds.add(el.getAttribute('id'));
      }
    });
    elements.forEach((el) => {
      if ((el.localName === 'BPMNShape' || el.localName === 'BPMNEdge') && hiddenIds.has(el.getAttribute('bpmnElement'))) {
        el.parentNode.removeChild(el);
      }
    });
    return new XMLSerializer().serializeToString(doc);
  } catch (e) {
    initiatorAliases = new Map();
    return xml;
  }
}

function displayNodeId(id) {
  return initiatorAliases.get(id) || id;
}

async function ensureViewer() {
  if (viewer) return viewer;
  await import('bpmn-js/dist/assets/diagram-js.css');
  await import('bpmn-js/dist/assets/bpmn-js.css');
  await import('bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css');
  const { default: NavigatedViewer } = await import('bpmn-js/lib/NavigatedViewer.js');
  viewer = new NavigatedViewer({ container: canvasRef.value });
  return viewer;
}

/**
 * 自适应缩放并居中。
 * 不用 canvas.zoom('fit-viewport','auto')：容器宽度为 0（弹窗首帧 / 页签刚显示）时它内部会静默放弃，
 * 之后尺寸恢复了也不会补一次，结果图被画在视口外，看起来就是空白。
 * 这里自己按内容包围盒算 viewbox：
 * - 缩放同时受容器宽和高限制，且不超过 maxScale（默认 2）——图会铺满可视区域，
 * 而不是固定 100% 缩成中间一小块；
 * - 画布高度固定用传入的 height（铺满模式跟容器），不再按内容收紧。
 */
function fitViewport() {
  if (!viewer) return;
  const el = canvasRef.value;
  if (!el) return;
  const w = el.clientWidth;
  if (!w) return;
  try {
    const canvas = viewer.get('canvas');
    const box = canvas.viewbox();
    const inner = box && box.inner;
    if (!inner || !inner.width || !inner.height) return;
    // 留一点边距，避免图形贴边
    const pad = 0.92;
    const limit = props.maxScale > 0 ? props.maxScale : 1;
    const maxH = typeof props.height === 'number' ? props.height : parseFloat(props.height) || 460;
    // 画布高度：铺满模式跟容器走，其余用传入的 height（不再按内容收紧）
    const h = props.fill ? el.clientHeight || maxH : maxH;
    const scale = Math.min((w * pad) / inner.width, (h * pad) / inner.height, limit);
    if (!isFinite(scale) || scale <= 0) return;
    canvas.viewbox({
      x: inner.x - (w / scale - inner.width) / 2,
      y: inner.y - (h / scale - inner.height) / 2,
      width: w / scale,
      height: h / scale,
    });
  } catch (e) {
    // 图形为空等情况忽略，不影响页面
  }
}

/**
 * 是否为部署时注入的「发起人（重新提交）」节点。
 * flowable:initiatorNode 不在 bpmn-moddle 描述符里，bo.get() 取不到，只能读 $attrs。
 */
function isInitiatorShape(el) {
  const bo = el && el.businessObject;
  if (!bo) return false;
  const v = (bo.get && bo.get('flowable:initiatorNode')) || (bo.$attrs && bo.$attrs['flowable:initiatorNode']);
  return v === true || v === 'true';
}

/** 配色标记：开始绿圆 / 结束红终止 / 发起人灰虚线卡 / 用户任务蓝卡 / 网关橙菱形 / 退回红线 */
function markShapes() {
  try {
    const canvas = viewer.get('canvas');
    const registry = viewer.get('elementRegistry');
    registry.getAll().forEach((el) => {
      if (el.type === 'bpmn:StartEvent') canvas.addMarker(el.id, 'wm-start');
      else if (el.type === 'bpmn:EndEvent') canvas.addMarker(el.id, 'wm-end');
      else if (isInitiatorShape(el)) canvas.addMarker(el.id, 'wm-initiator');
      else if (el.type === 'bpmn:UserTask' || el.type === 'bpmn:Task' || el.type === 'bpmn:ServiceTask') canvas.addMarker(el.id, 'wm-task');
      else if (/Gateway$/.test(el.type || '')) canvas.addMarker(el.id, 'wm-gateway');
      else if (el.type === 'bpmn:SequenceFlow') {
        const name = el.businessObject && el.businessObject.name;
        if (name && /退回|驳回/.test(name)) canvas.addMarker(el.id, 'wm-return');
      }
    });
  } catch (e) {
    // 标记失败仅影响配色，不阻断渲染
  }
}

const SVG_NS = 'http://www.w3.org/2000/svg';

/**
 * 开始/结束事件统一画成「实心圆 + 白色符号」，与后台设计器和后端生成的流程图保持一致。
 * bpmn-js 对无事件类型的开始/结束事件只画一个空心圆，有 eventDefinition 时又画一套自己的图标
 * （字体图标缺字时会退化成空心方块），两种都不是后端图的样子，这里统一接管：
 * 先清掉事件圆框以外的内置子图形，再按图形尺寸补一个白色三角（开始）/ 白色方块（结束）。
 *
 * 节点名已经由 utils/bpmnSizes#normalizeEventLabelPositions 搬进圆内（与用户任务「名字写在框里」
 * 同一观感），所以符号缩小并让到圆的左上角，位置对齐用户任务左上角的人像图标，
 * 名称居中写在圆里，两者互不遮挡。
 */
function decorateEvents() {
  if (!viewer) return;
  try {
    const canvas = viewer.get('canvas');
    const registry = viewer.get('elementRegistry');
    // 圆内的节点名是独立的 label 元素，需要单独打标记才能切成白字（默认样式是圆下方的深灰字）
    registry.getAll().forEach((el) => {
      const target = el && el.labelTarget;
      if (!target) return;
      if (target.type !== 'bpmn:StartEvent' && target.type !== 'bpmn:EndEvent') return;
      try {
        canvas.addMarker(el.id, 'wm-event-label');
      } catch (e) {}
    });
    registry.getAll().forEach((el) => {
      if (el.type !== 'bpmn:StartEvent' && el.type !== 'bpmn:EndEvent') return;
      // canvas.getGraphics 返回外层 g.djs-element，可视图形在它内部的 g.djs-visual 里
      const root = canvas.getGraphics(el);
      const gfx = root && root.querySelector ? root.querySelector('.djs-visual') || root : null;
      if (!gfx || !el.width || !el.height) return;
      Array.prototype.slice.call(gfx.children || []).forEach((child) => {
        const tag = (child.tagName || '').toLowerCase();
        if (tag === 'circle') return;
        // 节点名称是独立的 djs-label，不能被当成内置图标清掉
        if (child.classList && (child.classList.contains('djs-label') || child.classList.contains('wm-event-symbol'))) return;
        gfx.removeChild(child);
      });
      if (gfx.querySelector('.wm-event-symbol')) return;
      const r = Math.min(el.width, el.height) / 2;
      const cx = el.width / 2;
      const cy = el.height / 2;
      // 符号缩小并放在圆圈左上角（对齐用户任务左上角的人像图标位置），名称居中写在圆里
      const sx = cx - r * 0.45;
      const sy = cy - r * 0.45;
      let symbol;
      if (el.type === 'bpmn:StartEvent') {
        symbol = document.createElementNS(SVG_NS, 'path');
        symbol.setAttribute('d', 'M ' + (sx - r * 0.16) + ' ' + (sy - r * 0.2) + ' L ' + (sx + r * 0.22) + ' ' + sy + ' L ' + (sx - r * 0.16) + ' ' + (sy + r * 0.2) + ' Z');
      } else {
        const half = r * 0.13;
        symbol = document.createElementNS(SVG_NS, 'rect');
        symbol.setAttribute('x', sx - half);
        symbol.setAttribute('y', sy - half);
        symbol.setAttribute('width', half * 2);
        symbol.setAttribute('height', half * 2);
      }
      symbol.setAttribute('class', 'wm-event-symbol');
      gfx.appendChild(symbol);
    });
  } catch (e) {
    // 图标缺失不影响主图渲染
  }
}

/** 实例状态高亮：已流转=实色、当前停留=红框发光、未流转=淡出；连线同理 */
function applyStatus() {
  if (!viewer) return;
  const hasStatus = (props.doneIds || []).length > 0 || (props.activeIds || []).length > 0;
  if (!hasStatus) return;
  try {
    const canvas = viewer.get('canvas');
    const registry = viewer.get('elementRegistry');
    const done = new Set((props.doneIds || []).map(displayNodeId));
    const active = new Set((props.activeIds || []).map(displayNodeId));
    const skipped = new Set((props.skippedIds || []).map(displayNodeId));
    const doneFlows = new Set(props.doneFlowIds || []);
    const statusMarkers = ['wm-done', 'wm-active', 'wm-pending', 'wm-skipped', 'wm-flow-done', 'wm-flow-pending'];
    // 事件名称已经画在圆里（独立 label 元素，不跟图形的 opacity 一起走），
    // 未流转时得单独补一个淡出标记，否则白字会浮在淡化的圆上
    const eventLabels = new Map();
    registry.getAll().forEach((el) => {
      const target = el && el.labelTarget;
      if (!target) return;
      if (target.type !== 'bpmn:StartEvent' && target.type !== 'bpmn:EndEvent') return;
      try {
        canvas.removeMarker(el.id, 'wm-label-pending');
      } catch (e) {}
      const arr = eventLabels.get(target.id) || [];
      arr.push(el);
      eventLabels.set(target.id, arr);
    });
    registry.getAll().forEach((el) => {
      // 只处理 BPMN 元素：跳过画布根节点与标签
      if (!el || !el.type || el.type.indexOf('bpmn:') !== 0 || el.labelTarget) return;
      // 切换实例时先清掉上一份状态标记，避免残留
      statusMarkers.forEach((c) => {
        try {
          canvas.removeMarker(el.id, c);
        } catch (e) {}
      });
      if (el.waypoints || el.type === 'bpmn:SequenceFlow') {
        // 退回/驳回线保持红色虚线语义，不参与「已流转=绿」覆盖
        let isReturn = false;
        try {
          isReturn = canvas.hasMarker(el.id, 'wm-return');
        } catch (e) {
          isReturn = false;
        }
        if (isReturn) return;
        canvas.addMarker(el.id, doneFlows.has(el.id) ? 'wm-flow-done' : 'wm-flow-pending');
        return;
      }
      // 容器类元素（流程/协作/泳道）不参与着色
      if (el.type === 'bpmn:Process' || el.type === 'bpmn:Collaboration' || /Lane$/.test(el.type)) return;
      if (active.has(el.id)) canvas.addMarker(el.id, 'wm-active');
      // 被管理员干预跳过的节点：不按「已流转」画绿卡，改成灰底虚线，和真正办理过的节点区分开
      else if (skipped.has(el.id)) canvas.addMarker(el.id, 'wm-skipped');
      else if (done.has(el.id)) canvas.addMarker(el.id, 'wm-done');
      else {
        canvas.addMarker(el.id, 'wm-pending');
        (eventLabels.get(el.id) || []).forEach((labelEl) => {
          try {
            canvas.addMarker(labelEl.id, 'wm-label-pending');
          } catch (e) {}
        });
      }
    });
  } catch (e) {
    // 高亮失败不影响基础图渲染
  }
}

/* ===== 运行期节点人员（节点下显示审批人 + 点击节点看未审批/已查看/已操作） =====
 * 数据来自后端 /flowable/process/diagramData 的 nodes（按节点头的 taskDefinitionKey 聚合）：
 * done 已操作人员（有办理意见，含动作/意见/时间）
 * pending 未审批操作人（assignee=已签收待办 / candidate=候选池 / skipped=撤销终止时被跳过）
 * viewed 已查看人员（打开过办理页但还没办理，见 flow_node_viewer 表）
 * 标签与浮层都用 bpmn-js overlays 挂载，随画布缩放/平移自动跟随，不用自己算坐标。 */

/** 办理动作 → 文案（与「审批意见」表格同一口径；未知动作原样显示） */
const ACTION_TEXT = {
  start: '发起提交',
  approved: '同意',
  rejected: '驳回',
  delegate: '转办',
  addSign: '加签',
  assign: '改派',
  intervene: '干预',
  terminate: '终止',
  forceEnd: '强制结束',
  cancel: '撤销',
  canceled: '撤销',
  end: '流程结束',
  undone: '未办理',
};
/** 未审批人员类型 → 标签 */
const KIND_TEXT = { assignee: '待办', candidate: '候选', skipped: '未办理' };

/** 当前打开的浮层所属节点ID（null=未打开） */
const popoverNodeId = ref(null);

function getOverlays() {
  if (!viewer) return null;
  try {
    return viewer.get('overlays');
  } catch (e) {
    return null;
  }
}

function resolveVisibleNodeInfo() {
  const resolved = {};
  Object.entries(props.nodeInfo || {}).forEach(([id, source]) => {
    const targetId = displayNodeId(id);
    const node = source || {};
    const target = resolved[targetId];
    if (!target) {
      resolved[targetId] = {
        ...node,
        nodeId: targetId,
        done: [...(node.done || [])],
        pending: [...(node.pending || [])],
        viewed: [...(node.viewed || [])],
        cc: [...(node.cc || [])],
        delegate: [...(node.delegate || [])],
        intervene: [...(node.intervene || [])],
      };
      return;
    }
    // 开始事件自身的名称优先；技术节点的人员记录合并到开始节点展示
    if (id === targetId && node.name) target.name = node.name;
    ['done', 'pending', 'viewed', 'cc', 'delegate', 'intervene'].forEach((key) => {
      target[key] = [...(target[key] || []), ...(node[key] || [])];
    });
  });
  return resolved;
}

function nodeOf(id) {
  return (id && visibleNodeInfo[id]) || null;
}

function isActiveNode(id) {
  return (props.activeIds || []).some((rawId) => displayNodeId(rawId) === id);
}

function isDoneNode(id) {
  return (props.doneIds || []).some((rawId) => displayNodeId(rawId) === id);
}

/** 是否被管理员干预跳过（节点被强制结束、没人办理） */
function isSkippedNode(id) {
  return (props.skippedIds || []).some((rawId) => displayNodeId(rawId) === id);
}

/** 节点下方显示的审批人：当前节点显示待办人 + 查看状态，已流转节点显示最后一次办理人 + 动作 */
function assigneeText(node, el) {
  if (!node) return '';
  const pending = node.pending || [];
  const done = node.done || [];
  const skippedNode = isSkippedNode(node.nodeId);
  const isEvent = el && (el.type === 'bpmn:StartEvent' || el.type === 'bpmn:EndEvent');
  // 被干预跳过的节点没有办理人，退回到「未审批」人员里取当时待办的人
  const source = isActiveNode(node.nodeId) && pending.length
    ? pending
    : (done.length || !skippedNode ? done : pending);
  const names = [];
  source.forEach((p) => {
    const name = p.userName || p.userId;
    if (name && names.indexOf(name) < 0) names.push(name);
  });
  if (!names.length) return '';
  if (names.length > 1) return names[0] + ' 等' + names.length + '人';
  const name = names[0];
  // 开始/结束事件空间小，只显示人名，状态放到点击浮层里
  if (isEvent) return name;
  if (skippedNode && pending.length) return name + '·干预跳过';
  if (isActiveNode(node.nodeId) && pending.length) {
    const person = pending[0];
    if (person.kind === 'candidate') return name + '·候选';
    const viewed = person.viewed || person.viewTime;
    return name + (viewed ? '·已查看' : '·未查看');
  }
  if (done.length) {
    const action = done[done.length - 1].action;
    if (action === 'start' || action === 'end' || action === 'cancel' || action === 'canceled') return name;
    return name + '·' + (ACTION_TEXT[action] || '已提交');
  }
  return name;
}

function escapeHtml(value) {
  return String(value == null ? '' : value).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
}

/** 后端时间是 yyyy-MM-dd HH:mm:ss，浮层里只留「MM-dd HH:mm」节省宽度 */
function shortTime(value) {
  if (!value) return '';
  const s = String(value);
  const m = s.match(/(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})/);
  return m ? m[2] + '-' + m[3] + ' ' + m[4] + ':' + m[5] : s;
}

/** 接收 → 操作（或查看）的耗时，与流程状态「耗时」同一口径 */
function durationText(begin, end) {
  if (!begin || !end) return '';
  const b = new Date(String(begin).replace(/-/g, '/'));
  const e = new Date(String(end).replace(/-/g, '/'));
  const ms = e - b;
  if (!isFinite(ms) || ms < 0) return '';
  const min = Math.floor(ms / 60000);
  if (min < 1) return '不足1分钟';
  if (min < 60) return min + '分钟';
  const hours = Math.floor(min / 60);
  if (hours < 24) return hours + '小时' + (min % 60 ? (min % 60) + '分钟' : '');
  const days = Math.floor(hours / 24);
  return days + '天' + (hours % 24 ? (hours % 24) + '小时' : '');
}

/**
 * 浮层里的一行（对标泛微流程图浮层）：
 * 头像 + 姓名 + 状态/动作 + 接收时间/操作时间/查看时间/耗时 + 签字意见
 */
function personRow(person, tagText) {
  const name = person.userName || person.userId || '-';
  const tags = [];
  if (tagText) tags.push(tagText);
  if (person.action) tags.push(ACTION_TEXT[person.action] || person.action);
  const viewAt = person.viewTime || (!person.action ? person.time : null);
  const times = [];
  if (person.createTime) times.push('接收 ' + shortTime(person.createTime));
  if (person.action && person.time) times.push('操作 ' + shortTime(person.time));
  if (!person.action && viewAt) times.push('查看 ' + shortTime(viewAt));
  const cost = durationText(person.createTime, person.action ? person.time : viewAt);
  if (cost) times.push('耗时 ' + cost);
  let comment = '';
  if (person.comment) {
    try {
      comment = htmlToText(person.comment).replace(/\s+/g, ' ').trim();
    } catch (e) {
      comment = '';
    }
  }
  return (
    '<div class="wm-pop-row">' +
    '<span class="wm-pop-avatar">' +
    escapeHtml(String(name).slice(0, 1)) +
    '</span>' +
    '<span class="wm-pop-main">' +
    '<span class="wm-pop-name">' +
    escapeHtml(name) +
    '</span>' +
    (tags.length ? '<span class="wm-pop-meta">' + escapeHtml(tags.join(' · ')) + '</span>' : '') +
    (times.length ? '<span class="wm-pop-meta">' + escapeHtml(times.join(' · ')) + '</span>' : '') +
    (comment ? '<span class="wm-pop-cmt">' + escapeHtml(comment) + '</span>' : '') +
    '</span></div>'
  );
}

/** 抄送记录行：抄送人 → 接收人，已读/未读 + 阅读时间 + 抄送时间 */
function ccRow(cc) {
  const sender = cc.senderName || cc.senderId || '-';
  const receiver = cc.receiverName || cc.receiverId || '-';
  const read = String(cc.readFlag) === '1';
  const meta = [read ? '已读' : '未读'];
  if (read && cc.readTime) meta.push(shortTime(cc.readTime));
  if (cc.time) meta.push('抄送 ' + shortTime(cc.time));
  return (
    '<div class="wm-pop-row">' +
    '<span class="wm-pop-avatar">' +
    escapeHtml(String(receiver).slice(0, 1)) +
    '</span>' +
    '<span class="wm-pop-main">' +
    '<span class="wm-pop-name">' +
    escapeHtml(sender) +
    ' → ' +
    escapeHtml(receiver) +
    '</span>' +
    '<span class="wm-pop-meta">' +
    escapeHtml(meta.join(' · ')) +
    '</span>' +
    '</span></div>'
  );
}

/** 转办记录行：转办人 → 被转办人，转办时间 + 转办说明 */
function delegateRow(item) {
  const from = item.fromName || item.fromId || '-';
  const to = item.toName || item.toId || '-';
  const meta = [];
  if (item.time) meta.push('转办 ' + shortTime(item.time));
  // 「转办给XXX」这类系统自动文案与上面的「转办人 → 被转办人」重复，不再展示
  const remark = /^转办给/.test(String(item.comment || '').trim()) ? '' : item.comment;
  return (
    '<div class="wm-pop-row">' +
    '<span class="wm-pop-avatar">' +
    escapeHtml(String(to).slice(0, 1)) +
    '</span>' +
    '<span class="wm-pop-main">' +
    '<span class="wm-pop-name">' +
    escapeHtml(from) +
    ' → ' +
    escapeHtml(to) +
    '</span>' +
    (meta.length ? '<span class="wm-pop-meta">' + escapeHtml(meta.join(' · ')) + '</span>' : '') +
    (remark ? '<span class="wm-pop-cmt">' + escapeHtml(remark) + '</span>' : '') +
    '</span></div>'
  );
}

/** 管理员干预记录行：管理员 + 调整时间 + 「来源节点 → 目标节点」+ 指定办理人 + 说明 */
function interveneRow(item) {
  const name = item.userName || item.userId || '管理员';
  const fromNames = item.fromNames && item.fromNames.length
    ? item.fromNames.join('、')
    : item.fromName;
  const to = item.toName || '';
  const meta = [];
  if (item.time) meta.push('干预 ' + shortTime(item.time));
  if (to) meta.push((fromNames ? fromNames + ' → ' : '至 ') + to);
  if (item.assigneeName) meta.push('办理人 ' + item.assigneeName);
  const remark = item.comment ? String(item.comment) : '';
  return (
    '<div class="wm-pop-row">' +
    '<span class="wm-pop-avatar wm-pop-avatar--admin">' +
    escapeHtml(String(name).slice(0, 1)) +
    '</span>' +
    '<span class="wm-pop-main">' +
    '<span class="wm-pop-name">' +
    escapeHtml(name) +
    ' · 管理员干预</span>' +
    (meta.length ? '<span class="wm-pop-meta">' + escapeHtml(meta.join(' · ')) + '</span>' : '') +
    (remark ? '<span class="wm-pop-cmt">' + escapeHtml(remark) + '</span>' : '') +
    '</span></div>'
  );
}

/**
 * 浮层内容：节点名 + 状态 + 未审批/已查看/已操作/转办/抄送五段。
 *
 * 人员过多时每段默认只展示最重要的前 POPOVER_ROW_LIMIT 条，
 * 其余折叠为「展开剩余 N 条」，避免浮层被撑高、遮挡流程图。
 */
function buildPopoverHtml(node) {
  const pending = node.pending || [];
  const done = node.done || [];
  const viewed = node.viewed || [];
  const cc = node.cc || [];
  const delegate = node.delegate || [];
  const intervene = node.intervene || [];
  const parts = [];
  const section = (title, rows, cls) => {
    if (!rows.length) return;
    const visible = rows.slice(0, POPOVER_ROW_LIMIT);
    const rest = rows.slice(POPOVER_ROW_LIMIT);
    let html =
      '<div class="wm-pop-sec ' + (cls || '') + '">' +
      '<div class="wm-pop-sec-title">' + title + '<em>' + rows.length + '</em></div>' +
      visible.join('');
    if (rest.length) {
      html +=
        '<div class="wm-pop-sec-rest" hidden>' + rest.join('') + '</div>' +
        '<button type="button" class="wm-pop-more-btn" data-rest="' + rest.length + '">展开剩余 ' + rest.length + ' 条</button>';
    }
    html += '</div>';
    parts.push(html);
  };
  section(
    '未审批操作人',
    pending.map((p) => personRow(p, (KIND_TEXT[p.kind] || '') + ' · ' + (p.viewed || p.viewTime ? '已查看' : '未查看'))),
    'is-todo',
  );
  section(
    '已查看人员',
    viewed.map((p) => personRow(p, '已查看')),
    'is-view',
  );
  section(
    '已操作人员',
    done.map((p) => personRow(p, '已提交')),
    'is-done',
  );
  section(
    '转办记录',
    delegate.map((item) => delegateRow(item)),
    'is-delegate',
  );
  section(
    '抄送记录',
    cc.map((item) => ccRow(item)),
    'is-cc',
  );
  section(
    '管理员干预',
    intervene.map((item) => interveneRow(item)),
    'is-intervene',
  );
  if (!parts.length) parts.push('<div class="wm-pop-empty">该节点暂无办理人信息</div>');
  const active = isActiveNode(node.nodeId);
  const skippedNode = isSkippedNode(node.nodeId);
  const stateText = skippedNode ? '干预跳过' : active ? '审批中' : isDoneNode(node.nodeId) ? '已审批' : '未流转';
  const stateCls = skippedNode ? 'is-skip' : active ? 'is-active' : isDoneNode(node.nodeId) ? 'is-done' : 'is-idle';
  return (
    '<div class="wm-pop-head">' +
    '<span class="wm-pop-title">' +
    escapeHtml(node.name || node.nodeId) +
    '</span>' +
    '<span class="wm-pop-state ' +
    stateCls +
    '">' +
    stateText +
    '</span>' +
    '</div><div class="wm-pop-body">' +
    parts.join('') +
    '</div>'
  );
}

/** 清理节点人员标签与浮层（重新 importXML 前必须调，否则 overlay 会挂在已销毁的图形上） */
function clearNodeOverlays() {
  const overlays = getOverlays();
  popoverNodeId.value = null;
  if (!overlays) return;
  try {
    overlays.clear();
  } catch (e) {
    /* 画布未就绪时忽略 */
  }
}

/**
 * 把每个节点的人员标签贴到节点上（审批人直接显示在流程节点框里）。
 * - 用户任务：贴到框内底部，白色小字，与节点名形成「节点名 / 办理人」两行；
 * - 开始/结束事件：与用户任务一致贴进圆内底部（名称居中、符号在左上角，底部留给办理人）；
 * - 节点名过长（换两行占满高度）时才退化成贴在图形下方，避免文字叠在一起。
 */
function applyNodeInfo() {
  visibleNodeInfo = resolveVisibleNodeInfo();
  const overlays = getOverlays();
  if (!overlays) return;
  try {
    overlays.remove({ type: 'wm-assignee' });
  } catch (e) {}
  let registry = null;
  let canvas = null;
  try {
    registry = viewer.get('elementRegistry');
    canvas = viewer.get('canvas');
  } catch (e) {
    return;
  }
  // 先清上一轮的可点击标记（切换实例/刷新后节点集合会变）
  try {
    registry.getAll().forEach((el) => {
      try {
        canvas.removeMarker(el.id, 'wm-clickable');
      } catch (e) {}
    });
  } catch (e) {}
  const info = visibleNodeInfo;
  if (!props.showAssignees || !Object.keys(info).length) return;
  Object.keys(info).forEach((id) => {
    const el = registry.get(id);
    const node = info[id];
    if (!el || !node) return;
    try {
      canvas.addMarker(id, 'wm-clickable');
    } catch (e) {}
    const text = assigneeText(node, el);
    if (!text) return;
    // 节点名太长（换两行）时框内底部放不下审批人 → 退化成贴在框下方，避免文字叠在一起
    let nameBottom = 0;
    try {
      const gfx = canvas.getGraphics(el);
      const nameTextEl = gfx && gfx.querySelector ? gfx.querySelector('.djs-visual > text') : null;
      if (nameTextEl && nameTextEl.getBBox) {
        const bb = nameTextEl.getBBox();
        nameBottom = bb.y + bb.height;
      }
    } catch (e) {
      nameBottom = 0;
    }
    const outside = nameBottom > (el.height || 0) - 15;
    const isEvent = el.type === 'bpmn:StartEvent' || el.type === 'bpmn:EndEvent';
    const label = document.createElement('div');
    label.className = 'wm-node-assignee' + (isActiveNode(id) ? ' is-active' : '') + (outside ? ' is-outside' : '');
    label.setAttribute('data-element-id', id);
    label.setAttribute('title', (node.name ? node.name + '：' : '') + text);
    label.textContent = text;
    // 框内标签宽度限制在节点宽度内，超出省略（圆形事件再收紧一点，避免文字顶出圆弧）
    label.style.maxWidth = Math.max(40, (el.width || 100) - (isEvent ? 24 : 10)) + 'px';
    const position = outside ? { top: (el.height || 0) + 2, left: (el.width || 0) / 2 } : { top: Math.max(0, (el.height || 0) - 17), left: (el.width || 0) / 2 };
    try {
      overlays.add(id, 'wm-assignee', { position, html: label });
    } catch (e) {
      /* 单个标签失败不影响其它节点 */
    }
  });
}

function closePopover() {
  if (popoverNodeId.value == null) return;
  popoverNodeId.value = null;
  const overlays = getOverlays();
  if (!overlays) return;
  try {
    overlays.remove({ type: 'wm-popover' });
  } catch (e) {}
}

/** 浮层固定宽度（与 CSS .wm-node-pop 的 width 一致，用于边界钳制） */
const POPOVER_WIDTH = 300;
/** 浮层每段默认展示条数，超出折叠（人员/抄送过多时保持浮层高度稳定） */
const POPOVER_ROW_LIMIT = 3;

/** 打开节点浮层：节点在视口上半部分就往下弹，否则往上弹，避免浮层被画布裁掉 */
function openPopover(el) {
  const node = nodeOf(el.id);
  const overlays = getOverlays();
  if (!node || !overlays) return;
  closePopover();
  let below = true;
  let scale = 1;
  let shift = 0;
  try {
    const canvas = viewer.get('canvas');
    const box = canvas.viewbox();
    below = el.y + (el.height || 0) / 2 < box.y + box.height / 2;
    scale = box.scale > 0 ? box.scale : 1;
    // 靠画布左右边缘的节点（开始/结束）浮层会溢出容器，这里按可用宽度反向平移回来
    const viewWidth = canvasRef.value ? canvasRef.value.clientWidth : 0;
    if (viewWidth) {
      const centerX = (el.x + (el.width || 0) / 2 - box.x) * scale;
      const half = POPOVER_WIDTH / 2 + 10;
      if (centerX - half < 0) shift = (half - centerX) / scale;
      else if (centerX + half > viewWidth) shift = (viewWidth - centerX - half) / scale;
    }
  } catch (e) {}
  const card = document.createElement('div');
  card.className = 'wm-node-pop' + (below ? '' : ' is-above');
  card.innerHTML = buildPopoverHtml(node);
  // 分段折叠：点击「展开剩余 N 条」就地展开/收起，浮层高度随内容自适应
  card.addEventListener('click', (ev) => {
    const btn = ev.target.closest && ev.target.closest('.wm-pop-more-btn');
    if (!btn) return;
    ev.stopPropagation();
    const rest = btn.previousElementSibling;
    if (!rest || !rest.classList.contains('wm-pop-sec-rest')) return;
    const expanded = !rest.hidden;
    rest.hidden = expanded;
    btn.classList.toggle('is-open', !expanded);
    btn.textContent = expanded ? '展开剩余 ' + (btn.dataset.rest || '') + ' 条' : '收起';
  });
  // 浮层内容不随画布缩放（scale:false），但 offset 仍按图形坐标 → 换算成固定的 10px 视觉间距
  const left = (el.width || 0) / 2 + shift;
  const gap = (el.height || 0) + Math.round(10 / scale);
  const position = below ? { top: gap, left } : { bottom: gap, left };
  try {
    overlays.add(el.id, 'wm-popover', { position, scale: false, html: card });
    popoverNodeId.value = el.id;
  } catch (e) {
    /* 浮层失败不影响主图 */
  }
}

/**
 * 画布点击：命中节点或节点下的审批人标签 → 弹浮层；再点一次收起；点空白处关闭。
 * 说明：只读查看器（NavigatedViewer）没有业务交互模块，这里用 DOM 委托，
 * 通过 bpmn-js 写在 <g class="djs-element" data-element-id="…"> 上的 ID 反查图形。
 */
function onCanvasClick(ev) {
  const target = ev && ev.target;
  if (!target || !target.closest) return;
  // 浮层内部点击（滚动、选中文字）不关闭浮层
  if (target.closest('.wm-node-pop')) return;
  let id = null;
  const label = target.closest('.wm-node-assignee');
  if (label) {
    id = label.getAttribute('data-element-id');
  } else {
    const g = target.closest('g.djs-element');
    id = g ? g.getAttribute('data-element-id') : null;
  }
  let element = null;
  if (id && viewer) {
    try {
      element = viewer.get('elementRegistry').get(id);
    } catch (e) {
      element = null;
    }
  }
  // 点到节点名称文字时拿到的是 label 元素，需回溯到图形本身
  const shape = element && (element.labelTarget || element);
  if (!shape || !nodeOf(shape.id)) {
    closePopover();
    return;
  }
  if (popoverNodeId.value === shape.id) {
    closePopover();
    return;
  }
  openPopover(shape);
}

/** Esc 关闭浮层 */
function onKeyDown(ev) {
  if (ev && ev.key === 'Escape') closePopover();
}

async function render() {
  const token = ++renderToken;
  const xml = (props.xml || '').trim();
  if (!xml) {
    state.value = 'empty';
    msg.value = props.emptyText;
    return;
  }
  if (!hasDiagramInfo(xml)) {
    state.value = 'empty';
    msg.value = '流程图缺少图形信息（bpmndi），请在设计器重新保存并部署后再查看';
    return;
  }
  state.value = 'loading';
  msg.value = '流程图加载中…';
  try {
    await nextTick();
    const v = await ensureViewer();
    if (token !== renderToken) return;
    // 重新导入前先清掉上一份图的人员标签/浮层（overlay 挂在图形上，图形销毁后必须重建）
    clearNodeOverlays();
    // 历史模型/脚本部署的图里，开始结束事件常是 bpmn-js 默认的 36x36（比 100x80 的处理节点小一圈），
    // 被压扁的处理节点还有 100x60 这类：都在这儿按设计器口径统一（事件 80x80、节点不低于 100x80）
    await v.importXML(normalizeDiagramSizes(prepareDiagramXml(xml)));
    state.value = 'ok';
    fitViewport();
    markShapes();
    decorateEvents();
    applyStatus();
    applyNodeInfo();
    // 弹窗/页签展开有动画，尺寸会变两次：120ms 与 420ms 各居中一次，确保最终落在视口中间
    window.clearTimeout(fitTimer);
    const refit = () => {
      if (token === renderToken) fitViewport();
    };
    window.setTimeout(refit, 120);
    fitTimer = window.setTimeout(refit, 420);
  } catch (e) {
    state.value = 'empty';
    msg.value = '流程图渲染失败：' + (e && e.message ? e.message : e);
  }
}

/** 容器尺寸变化（首帧隐藏 / 页签切换 / 弹窗动画 / 窗口缩放）后重新居中 */
function onResize() {
  const el = canvasRef.value;
  if (!el) return;
  const w = el.clientWidth;
  const h = el.clientHeight;
  if (!w || !h) return;
  const size = w + 'x' + h;
  if (size === lastSize) return;
  lastSize = size;
  window.clearTimeout(fitTimer);
  fitTimer = window.setTimeout(fitViewport, 60);
}

watch(
  () => props.xml,
  () => render(),
);
// 状态数据变化（切换实例 / 办理完刷新）：只重贴高亮与人员标签，不重新 importXML
watch(
  () => [props.doneIds, props.activeIds, props.doneFlowIds, props.skippedIds, props.nodeInfo],
  () => {
    if (state.value !== 'ok') return;
    applyStatus();
    applyNodeInfo();
  },
  { deep: true },
);

onMounted(() => {
  render();
  if (typeof ResizeObserver !== 'undefined' && canvasRef.value) {
    resizeObserver = new ResizeObserver(onResize);
    resizeObserver.observe(canvasRef.value);
  }
  window.addEventListener('resize', onResize);
  if (canvasRef.value) canvasRef.value.addEventListener('click', onCanvasClick);
  window.addEventListener('keydown', onKeyDown);
});

onBeforeUnmount(() => {
  window.clearTimeout(fitTimer);
  window.removeEventListener('resize', onResize);
  window.removeEventListener('keydown', onKeyDown);
  if (canvasRef.value) canvasRef.value.removeEventListener('click', onCanvasClick);
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }
  popoverNodeId.value = null;
  if (viewer) {
    try {
      viewer.destroy();
    } catch (e) {}
    viewer = null;
  }
});
</script>

<style lang="scss" scoped>
.wm-diagram-root {
  width: 100%;
}
/* 铺满模式：根节点纵向撑满，图例占自然高度，画布吃掉剩余空间 */
.wm-diagram-root--fill {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.wm-diagram-root--fill .wm-diagram {
  flex: 1 1 auto;
  min-height: 0;
}
.wm-diagram-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--ink-500, #64748b);
}
.wm-diagram-legend .lg-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.wm-diagram-legend .lg-dot {
  width: 14px;
  height: 10px;
  border-radius: 2px;
  display: inline-block;
  box-sizing: border-box;
}
.wm-diagram-legend .lg-active {
  background: #eff6ff;
  border: 2px solid #ef4444;
}
.wm-diagram-legend .lg-done {
  background: #22c55e;
}
.wm-diagram-legend .lg-pending {
  background: #e2e8f0;
}
.wm-diagram-legend .lg-skipped {
  background: #f1f5f9;
  border: 1.5px dashed #94a3b8;
}
.wm-diagram {
  position: relative;
  width: 100%;
  background: #fff;
  border: 1px solid var(--ink-200, #e2e8f0);
  border-radius: 8px;
  overflow: hidden;
}
.wm-diagram-canvas {
  width: 100%;
  height: 100%;
}
.wm-diagram-canvas :deep(.djs-container) {
  width: 100% !important;
  height: 100% !important;
  background: transparent;
}
.wm-diagram-canvas :deep(.djs-canvas) {
  outline: none;
}
.wm-diagram-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-400, #94a3b8);
  font-size: 13px;
  background: #fff;
}

/* ===== 配色（与后台设计器画布同一套） =====
 * 注意：bpmn-js 渲染器把 fill/stroke 写成 SVG 元素的内联 style，
 * 外部 CSS 即使特异性更高也压不过内联样式，因此配色必须带 !important。 */
/* 开始/结束事件：与后台设计器 / 后端生成的流程图同一视觉 —— 实心圆 + 白色符号。
 * 符号（三角/方块）由 decorateEvents() 绘制，这里只负责颜色。
 * 注意：bpmn-js 渲染器把 fill/stroke 写成 SVG 元素的内联 style，外部 CSS 即使特异性更高也压不过内联样式，
 * 因此配色必须带 !important。 */
.wm-diagram-canvas :deep(.wm-start.djs-shape .djs-visual > circle) {
  fill: #4caf50 !important;
  stroke: #43a047 !important;
  stroke-width: 2 !important;
}
.wm-diagram-canvas :deep(.wm-end.djs-shape .djs-visual > circle) {
  fill: #e74c3c !important;
  stroke: #1f2937 !important;
  stroke-width: 4 !important;
}
.wm-diagram-canvas :deep(.wm-event-symbol) {
  fill: #fff !important;
  stroke: none !important;
  /* 白三角/白方块只是圆圈里的装饰：不参与命中，点它等于点整个事件节点
   * （与用户任务左上角的人像图标一致，不会变成「点小图标没反应」） */
  pointer-events: none !important;
}
/* 用户任务：蓝色圆角卡片 + 白色粗体节点名 */
.wm-diagram-canvas :deep(.wm-task.djs-shape .djs-visual rect) {
  fill: #4a90e2 !important;
  stroke: #3b82f6 !important;
  stroke-width: 1 !important;
}
.wm-diagram-canvas :deep(.wm-task.djs-shape .djs-visual text) {
  fill: #fff !important;
  font-weight: 700;
}
/* 节点名称：bpmn-js 默认 11px 在 1:1 比例下偏小，与后端流程图对齐到 13px */
.wm-diagram-canvas :deep(.djs-label) {
  font-size: 13px !important;
  fill: #1f2937 !important;
}
.wm-diagram-canvas :deep(.wm-task.djs-shape .djs-visual text.djs-label) {
  fill: #fff !important;
}
/* 开始/结束事件：名称已经搬进圆内（见 utils/bpmnSizes#normalizeEventLabelPositions），
 * 与用户任务同款白字粗体；字号略收，保证 2~4 字在圆内不贴边 */
.wm-diagram-canvas :deep(.wm-event-label .djs-visual text.djs-label),
.wm-diagram-canvas :deep(.wm-event-label .djs-visual tspan) {
  fill: #fff !important;
  font-weight: 700 !important;
}
.wm-diagram-canvas :deep(.wm-event-label text.djs-label) {
  font-size: 12px !important;
}
/* 未流转事件的圆会淡出，圆内的名称跟着一起淡出 */
.wm-diagram-canvas :deep(.wm-label-pending .djs-visual) {
  opacity: 0.42;
}
/* 发起人（重新提交）节点：灰白虚线卡片，正常流转不经过，只在驳回退回发起人时使用；
 * 与蓝色审批卡片区分开，避免被当成多画出来的一个申请节点 */
.wm-diagram-canvas :deep(.wm-initiator.djs-shape .djs-visual rect) {
  fill: #f8fafc !important;
  stroke: #94a3b8 !important;
  stroke-width: 2 !important;
  stroke-dasharray: 6 4 !important;
}
.wm-diagram-canvas :deep(.wm-initiator.djs-shape .djs-visual text.djs-label) {
  fill: #475569 !important;
}
/* 退回发起人时它就是「当前停留节点」：底色是浅灰，沿用全局的白色当前节点文字会看不见，
 * 这里覆盖成浅红底 + 深红字，边框仍交由 .wm-active 的红色粗框与光晕表现 */
.wm-diagram-canvas :deep(.wm-initiator.wm-active.djs-shape .djs-visual rect) {
  fill: #fff1f2 !important;
}
.wm-diagram-canvas :deep(.wm-initiator.wm-active.djs-shape .djs-visual text) {
  fill: #b91c1c !important;
  font-weight: 700;
}
/* 网关：橙色菱形 + 白色网关符号 */
.wm-diagram-canvas :deep(.wm-gateway.djs-shape .djs-visual polygon) {
  fill: #f0a63c !important;
  stroke: #d98b1f !important;
  stroke-width: 2 !important;
}
.wm-diagram-canvas :deep(.wm-gateway.djs-shape .djs-visual path) {
  fill: #fff !important;
  stroke: #fff !important;
}
.wm-diagram-canvas :deep(.wm-gateway.djs-shape .djs-visual circle) {
  fill: none !important;
  stroke: #fff !important;
  stroke-width: 2.5 !important;
}
/* 退回/驳回出口：红色虚线 */
.wm-diagram-canvas :deep(.wm-return.djs-connection .djs-visual > path) {
  stroke: #dc2626 !important;
  stroke-width: 2 !important;
  stroke-dasharray: 6, 4 !important;
}
.wm-diagram-canvas :deep(.wm-return.djs-connection .djs-visual > path:nth-child(2)) {
  fill: #dc2626 !important;
  stroke: #dc2626 !important;
}

/* ===== 实例状态：已流转 / 当前停留 / 未流转（仅实例图带状态数据时生效） ===== */
/* 未流转：整块淡出，与已流转形成对比 */
.wm-diagram-canvas :deep(.wm-pending.djs-shape .djs-visual) {
  opacity: 0.42;
}
.wm-diagram-canvas :deep(.wm-flow-pending.djs-connection .djs-visual > path) {
  stroke: #cbd5e1 !important;
}
/* 已流转：保持设计器配色并实色，路径标绿 */
.wm-diagram-canvas :deep(.wm-flow-done.djs-connection .djs-visual > path) {
  stroke: #22c55e !important;
  stroke-width: 2 !important;
}
/* 被管理员干预跳过的节点：灰底 + 虚线边框，与真正办理过的蓝色实色卡片区分开 */
.wm-diagram-canvas :deep(.wm-task.wm-skipped.djs-shape .djs-visual > rect) {
  fill: #f1f5f9 !important;
  stroke: #94a3b8 !important;
  stroke-width: 2 !important;
  stroke-dasharray: 6 4 !important;
}
.wm-diagram-canvas :deep(.wm-task.wm-skipped.djs-shape .djs-visual text.djs-label) {
  fill: #475569 !important;
}
/* 当前停留节点：红色粗框 + 外发光（与后端 PNG 的红色当前节点同一语义） */
.wm-diagram-canvas :deep(.wm-active.djs-shape .djs-visual > rect),
.wm-diagram-canvas :deep(.wm-active.djs-shape .djs-visual > circle),
.wm-diagram-canvas :deep(.wm-active.djs-shape .djs-visual > ellipse),
.wm-diagram-canvas :deep(.wm-active.djs-shape .djs-visual > polygon) {
  stroke: #ef4444 !important;
  stroke-width: 3 !important;
}
/* 当前节点内的文字在深色底上要可读 */
.wm-diagram-canvas :deep(.wm-active.djs-shape .djs-visual > text) {
  fill: #fff !important;
  font-weight: 700;
}
.wm-diagram-canvas :deep(.wm-active.djs-shape .djs-visual) {
  filter: drop-shadow(0 0 6px rgba(239, 68, 68, 0.55));
}

/* ===== 运行期节点人员（节点下显示审批人 + 点击节点弹浮层） ===== */
.wm-diagram-legend .lg-hint {
  margin-right: auto;
  color: var(--ink-400, #94a3b8);
}
/* 有办理人信息的节点给出手型光标，提示可点 */
.wm-diagram-canvas :deep(.wm-clickable.djs-shape) {
  cursor: pointer;
}
/* 节点框内的审批人：与节点名叠成两行，白字 + 轻阴影保证在实色卡片上可读 */
.wm-diagram-canvas :deep(.wm-node-assignee) {
  position: absolute;
  transform: translateX(-50%);
  padding: 0 2px;
  border-radius: 3px;
  color: #fff;
  font-size: 10px;
  line-height: 13px;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.45);
  cursor: pointer;
  user-select: none;
}
.wm-diagram-canvas :deep(.wm-node-assignee:hover) {
  background: rgba(255, 255, 255, 0.22);
}
/* 当前审批节点：办理人加粗，一眼看出卡在谁身上 */
.wm-diagram-canvas :deep(.wm-node-assignee.is-active) {
  font-weight: 700;
}
/* 节点名过长、框内放不下办理人时，标签退化成贴在图形下方 */
.wm-diagram-canvas :deep(.wm-node-assignee.is-outside) {
  padding: 0 5px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  color: #475569;
  font-weight: 500;
  text-shadow: none;
}

/* 点击节点后的浮层：节点名 + 状态 + 未审批操作人/已查看人员/已操作人员 */
.wm-diagram-canvas :deep(.wm-node-pop) {
  position: absolute;
  transform: translateX(-50%);
  width: 300px;
  max-height: 340px;
  /* 纵向 flex：标题固定、正文内部滚动，展开/收起时只有内容变化，浮层不溢出画布 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  border: 1px solid #dbe3ee;
  border-radius: 8px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.18);
  font-size: 12px;
  color: #1f2937;
  z-index: 20;
  cursor: default;
}
/* 节点在视口下半部分时往上弹 */
.wm-diagram-canvas :deep(.wm-node-pop.is-above) {
  transform: translate(-50%, -100%);
}
.wm-diagram-canvas :deep(.wm-pop-head) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border-bottom: 1px solid #eef2f7;
  border-radius: 8px 8px 0 0;
  background: #f8fafc;
  flex: none;
}
.wm-diagram-canvas :deep(.wm-pop-title) {
  font-weight: 600;
  color: #0f172a;
  /* 节点名过长时省略，避免把固定宽度的浮层撑变形 */
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.wm-diagram-canvas :deep(.wm-pop-state) {
  flex: none;
  padding: 1px 6px;
  border-radius: 9px;
  font-size: 11px;
}
.wm-diagram-canvas :deep(.wm-pop-state.is-active) {
  color: #dc2626;
  background: #fef2f2;
  border: 1px solid #fecaca;
}
.wm-diagram-canvas :deep(.wm-pop-state.is-done) {
  color: #15803d;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}
.wm-diagram-canvas :deep(.wm-pop-state.is-idle) {
  color: #64748b;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}
.wm-diagram-canvas :deep(.wm-pop-state.is-skip) {
  color: #6d28d9;
  background: #f5f3ff;
  border: 1px solid #ddd6fe;
}
.wm-diagram-canvas :deep(.wm-pop-body) {
  padding: 6px 10px 8px;
  /* 内容超长时只在正文内滚动，标题栏固定，浮层整体高度/位置不变形 */
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
}
.wm-diagram-canvas :deep(.wm-pop-sec-rest[hidden]) {
  display: none;
}
.wm-diagram-canvas :deep(.wm-pop-more-btn) {
  display: block;
  width: 100%;
  margin: 2px 0 0;
  padding: 2px 0;
  border: none;
  background: none;
  color: #2f8ee0;
  font-size: 11px;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
}
.wm-diagram-canvas :deep(.wm-pop-more-btn:hover) {
  text-decoration: underline;
}
.wm-diagram-canvas :deep(.wm-pop-more-btn.is-open) {
  color: #94a3b8;
}
.wm-diagram-canvas :deep(.wm-pop-sec + .wm-pop-sec) {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px dashed #eef2f7;
}
.wm-diagram-canvas :deep(.wm-pop-sec-title) {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
}
.wm-diagram-canvas :deep(.wm-pop-sec-title em) {
  font-style: normal;
  padding: 0 5px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #475569;
  font-weight: 500;
}
.wm-diagram-canvas :deep(.wm-pop-sec.is-todo .wm-pop-sec-title) {
  color: #b45309;
}
.wm-diagram-canvas :deep(.wm-pop-sec.is-done .wm-pop-sec-title) {
  color: #15803d;
}
.wm-diagram-canvas :deep(.wm-pop-sec.is-cc .wm-pop-sec-title) {
  color: #1d4ed8;
}
.wm-diagram-canvas :deep(.wm-pop-sec.is-delegate .wm-pop-sec-title) {
  color: #7c3aed;
}
.wm-diagram-canvas :deep(.wm-pop-sec.is-intervene .wm-pop-sec-title) {
  color: #6d28d9;
}
.wm-diagram-canvas :deep(.wm-pop-row) {
  display: flex;
  gap: 6px;
  padding: 3px 0;
}
.wm-diagram-canvas :deep(.wm-pop-avatar) {
  flex: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e0edff;
  color: #1d4ed8;
  font-size: 11px;
  line-height: 20px;
  font-weight: 600;
  text-align: center;
}
.wm-diagram-canvas :deep(.wm-pop-avatar--admin) {
  background: #ede9fe;
  color: #6d28d9;
}
.wm-diagram-canvas :deep(.wm-pop-main) {
  display: flex;
  /* 姓名 / 状态 / 时间同一行排布，只有签字意见独占下一行，浮层更紧凑 */
  flex-direction: row;
  flex-wrap: wrap;
  align-items: baseline;
  column-gap: 6px;
  min-width: 0;
  line-height: 1.45;
}
.wm-diagram-canvas :deep(.wm-pop-name) {
  color: #0f172a;
  font-weight: 500;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.wm-diagram-canvas :deep(.wm-pop-meta) {
  color: #94a3b8;
  font-size: 11px;
}
.wm-diagram-canvas :deep(.wm-pop-cmt) {
  display: -webkit-box;
  /* 签字意见始终独占一行，避免挤在姓名/时间后面 */
  flex: 0 0 100%;
  margin-top: 1px;
  color: #475569;
  font-size: 11px;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.wm-diagram-canvas :deep(.wm-pop-empty) {
  padding: 8px 0;
  color: #94a3b8;
  text-align: center;
}
</style>
