<template>
  <div class="wm-diagram-root" :class="{ 'wm-diagram-root--fill': fill }">
    <div v-if="showLegend" class="wm-diagram-legend">
      <span v-if="showAssigneeHint" class="lg-item lg-hint">点击节点查看办理人</span>
      <span class="lg-item"><i class="lg-dot lg-active"></i>当前停留节点</span>
      <span class="lg-item"><i class="lg-dot lg-done"></i>已流转</span>
      <span class="lg-item"><i class="lg-dot lg-pending"></i>未流转</span>
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
  /** 是否显示图例（无状态数据时自动不显示） */
  legend: { type: Boolean, default: true },
  /**
   * 自适应缩放的上限：默认 2（允许放大），小流程在大容器里也能铺满，
   * 让图“占满可视区域”；传 1 则保持原始比例不放大。
   */
  maxScale: { type: Number, default: 2 },
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
    const limit = props.maxScale > 0 ? props.maxScale : 2;
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

/** 配色标记：开始绿圆 / 结束红终止 / 用户任务蓝卡 / 网关橙菱形 / 退回红线 */
function markShapes() {
  try {
    const canvas = viewer.get('canvas');
    const registry = viewer.get('elementRegistry');
    registry.getAll().forEach((el) => {
      if (el.type === 'bpmn:StartEvent') canvas.addMarker(el.id, 'wm-start');
      else if (el.type === 'bpmn:EndEvent') canvas.addMarker(el.id, 'wm-end');
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
 */
function decorateEvents() {
  if (!viewer) return;
  try {
    const canvas = viewer.get('canvas');
    const registry = viewer.get('elementRegistry');
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
      let symbol;
      if (el.type === 'bpmn:StartEvent') {
        symbol = document.createElementNS(SVG_NS, 'path');
        symbol.setAttribute('d', 'M ' + (cx - r * 0.26) + ' ' + (cy - r * 0.42) + ' L ' + (cx + r * 0.46) + ' ' + cy + ' L ' + (cx - r * 0.26) + ' ' + (cy + r * 0.42) + ' Z');
      } else {
        const half = r * 0.33;
        symbol = document.createElementNS(SVG_NS, 'rect');
        symbol.setAttribute('x', cx - half);
        symbol.setAttribute('y', cy - half);
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
    const done = new Set(props.doneIds || []);
    const active = new Set(props.activeIds || []);
    const doneFlows = new Set(props.doneFlowIds || []);
    const statusMarkers = ['wm-done', 'wm-active', 'wm-pending', 'wm-flow-done', 'wm-flow-pending'];
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
      else if (done.has(el.id)) canvas.addMarker(el.id, 'wm-done');
      else canvas.addMarker(el.id, 'wm-pending');
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

function nodeOf(id) {
  const all = props.nodeInfo || {};
  return (id && all[id]) || null;
}

function isActiveNode(id) {
  return (props.activeIds || []).indexOf(id) >= 0;
}

function isDoneNode(id) {
  return (props.doneIds || []).indexOf(id) >= 0;
}

/** 节点下方显示的审批人：当前节点显示待办人，已流转节点显示最后一次办理人；多人折叠成「张三 等3人」 */
function assigneeText(node) {
  if (!node) return '';
  const pending = node.pending || [];
  const source = isActiveNode(node.nodeId) && pending.length ? pending : node.done || [];
  const names = [];
  source.forEach((p) => {
    const name = p.userName || p.userId;
    if (name && names.indexOf(name) < 0) names.push(name);
  });
  if (!names.length) return '';
  return names.length > 1 ? names[0] + ' 等' + names.length + '人' : names[0];
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

/** 浮层里的一行：头像（姓名首字）+ 姓名 + 动作/时间 + 签字意见 */
function personRow(person, tagText) {
  const name = person.userName || person.userId || '-';
  const meta = [];
  if (tagText) meta.push(tagText);
  if (person.action) meta.push(ACTION_TEXT[person.action] || person.action);
  const time = shortTime(person.time);
  if (time) meta.push(time);
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
    (meta.length ? '<span class="wm-pop-meta">' + escapeHtml(meta.join(' · ')) + '</span>' : '') +
    (comment ? '<span class="wm-pop-cmt">' + escapeHtml(comment) + '</span>' : '') +
    '</span></div>'
  );
}

/** 浮层内容：节点名 + 状态 + 未审批/已查看/已操作三段（顺序：未审批 / 已查看 / 已操作） */
function buildPopoverHtml(node) {
  const pending = node.pending || [];
  const done = node.done || [];
  const viewed = node.viewed || [];
  const parts = [];
  const section = (title, rows, cls) => {
    if (!rows.length) return;
    parts.push('<div class="wm-pop-sec ' + (cls || '') + '">' + '<div class="wm-pop-sec-title">' + title + '<em>' + rows.length + '</em></div>' + rows.join('') + '</div>');
  };
  section(
    '未审批操作人',
    pending.map((p) => personRow(p, KIND_TEXT[p.kind] || '')),
    'is-todo',
  );
  section(
    '已查看人员',
    viewed.map((p) => personRow(p, '')),
    'is-view',
  );
  section(
    '已操作人员',
    done.map((p) => personRow(p, '')),
    'is-done',
  );
  if (!parts.length) parts.push('<div class="wm-pop-empty">该节点暂无办理人信息</div>');
  const active = isActiveNode(node.nodeId);
  const stateText = active ? '审批中' : isDoneNode(node.nodeId) ? '已审批' : '未流转';
  const stateCls = active ? 'is-active' : isDoneNode(node.nodeId) ? 'is-done' : 'is-idle';
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
 * - 开始/结束事件：圆圈太小放不下多个字，仍贴在图形下方。
 */
function applyNodeInfo() {
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
  const info = props.nodeInfo || {};
  if (!props.showAssignees || !Object.keys(info).length) return;
  Object.keys(info).forEach((id) => {
    const el = registry.get(id);
    const node = info[id];
    if (!el || !node) return;
    try {
      canvas.addMarker(id, 'wm-clickable');
    } catch (e) {}
    const text = assigneeText(node);
    if (!text) return;
    const isEvent = el.type === 'bpmn:StartEvent' || el.type === 'bpmn:EndEvent';
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
    const outside = isEvent || nameBottom > (el.height || 0) - 15;
    const label = document.createElement('div');
    label.className = 'wm-node-assignee' + (isActiveNode(id) ? ' is-active' : '') + (outside ? ' is-outside' : '');
    label.setAttribute('data-element-id', id);
    label.setAttribute('title', (node.name ? node.name + '：' : '') + text);
    label.textContent = text;
    // 框内标签宽度限制在节点宽度内，超出省略
    label.style.maxWidth = Math.max(40, (el.width || 100) - 10) + 'px';
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
const POPOVER_WIDTH = 258;

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
    await v.importXML(normalizeDiagramSizes(xml));
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
  () => [props.doneIds, props.activeIds, props.doneFlowIds, props.nodeInfo],
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
/* 开始/结束事件：圆圈太小放不下字，标签贴在图形下方 */
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
  width: 258px;
  max-height: 280px;
  overflow: auto;
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
}
.wm-diagram-canvas :deep(.wm-pop-title) {
  font-weight: 600;
  color: #0f172a;
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
.wm-diagram-canvas :deep(.wm-pop-body) {
  padding: 6px 10px 8px;
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
.wm-diagram-canvas :deep(.wm-pop-main) {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.45;
}
.wm-diagram-canvas :deep(.wm-pop-name) {
  color: #0f172a;
  font-weight: 500;
}
.wm-diagram-canvas :deep(.wm-pop-meta) {
  color: #94a3b8;
  font-size: 11px;
}
.wm-diagram-canvas :deep(.wm-pop-cmt) {
  display: -webkit-box;
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
