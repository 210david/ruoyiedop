/**
 * BPMN 节点尺寸规范化（XML 层，纯文本处理，不依赖 bpmn-js 实例）
 *
 * 背景：设计器新建的处理节点是 100x80、开始/结束事件是 80x80；
 *      但历史 / 脚本部署的模型里存在 100x60 这类被压扁的节点（部门审批节点即如此），
 *      在流程图里明显比相邻节点矮一截，节点名贴着上下边框，看着「太窄」；
 *      更有 bpmn-js 默认的 36x36 开始/结束事件，配 100x80 的处理节点时小得像个点。
 *
 * 口径：
 *  1) 处理节点 height 抬到不低于 minHeight（默认 80），以垂直中心为基准上下对称撑开；
 *  2) 开始/结束事件放大到不小于 size（默认 80，与设计器 WM_EVENT_SIZE 一致），以中心为基准撑开，
 *     同时把进出该事件的连线端点按同一中心缩放映射到新边线，避免连线插进圆里；
 *     事件自带的标签（BPMNLabel Bounds）随圆下移，保持圆与标签的间距。
 * 处理节点只动高度（水平中心、宽度、节点内文字不动）；事件只动尺寸（圆心不动）。
 *
 * 两个函数都是幂等的：已经达标的节点原样返回。
 */

/** 设计器标准处理节点高度 */
const DEFAULT_MIN_HEIGHT = 80

/** 设计器标准开始/结束事件尺寸（与 views/flowable/model 的 WM_EVENT_SIZE 保持一致） */
export const DEFAULT_EVENT_SIZE = 80

/** 参与高度规范化的 BPMN 处理节点标签（事件、网关、泳道不在此列） */
const TASK_TAGS = [
  'userTask',
  'task',
  'serviceTask',
  'scriptTask',
  'manualTask',
  'businessRuleTask',
  'sendTask',
  'receiveTask',
  'callActivity'
]

/** 参与尺寸规范化的 BPMN 事件标签 */
const EVENT_TAGS = ['startEvent', 'endEvent']

const BPMN_PREFIX = '(?:(?:bpmn|bpmn2):)?'

const TASK_TAG_RE = new RegExp('<' + BPMN_PREFIX + '(' + TASK_TAGS.join('|') + ')\\b([^>]*)>', 'g')
const EVENT_TAG_RE = new RegExp('<' + BPMN_PREFIX + '(' + EVENT_TAGS.join('|') + ')\\b([^>]*)>', 'g')
const FLOW_TAG_RE = new RegExp('<' + BPMN_PREFIX + '(?:sequenceFlow|messageFlow)\\b([^>]*)>', 'g')

const SHAPE_BLOCK_RE = /<(?:bpmndi:)?BPMNShape\b[^>]*bpmnElement="([^"]+)"[^>]*>[\s\S]*?<\/(?:bpmndi:)?BPMNShape>/g
const EDGE_BLOCK_RE = /<(?:bpmndi:)?BPMNEdge\b[^>]*bpmnElement="([^"]+)"[^>]*>[\s\S]*?<\/(?:bpmndi:)?BPMNEdge>/g
const BOUNDS_RE = /<(omgdc|dc):Bounds\b([^>]*?)\/>/
const WAYPOINT_RE = /<(omgdi|di):waypoint\b([^>]*?)\/>/g
const LABEL_BLOCK_RE = /(<(?:bpmndi:)?BPMNLabel\b[^>]*>)([\s\S]*?)(<\/(?:bpmndi:)?BPMNLabel>)/

/** 取属性值（属性顺序不固定，按名取） */
function readAttr(attrs, name) {
  const m = new RegExp('\\s' + name + '="([^"]*)"').exec(attrs)
  return m ? m[1] : ''
}

/** 数值保留一位小数，避免导出后出现 539.9999999 这类脏数据 */
function round1(n) {
  return Math.round(n * 10) / 10
}

/** 改写一组数值属性（x / y / width / height，属性可能缺失，缺失时补上） */
function setAttrs(attrs, box) {
  let next = attrs
  Object.keys(box).forEach(key => {
    const value = box[key]
    const re = new RegExp('\\s' + key + '="[^"]*"')
    if (re.test(next)) next = next.replace(re, ' ' + key + '="' + value + '"')
    else next += ' ' + key + '="' + value + '"'
  })
  return next
}

/** XML 是否带图形段（没有 DI 时 bpmn-js 直接报错，规范化也无意义） */
function hasDiagram(xml) {
  return /<(?:bpmndi:)?BPMNDiagram[\s>]/.test(xml)
}

/** 收集指定标签的元素 id（walk 一遍开标签，属性顺序无关） */
function collectTagIds(xml, tagRe, filterTag) {
  const ids = new Set()
  let m
  tagRe.lastIndex = 0
  while ((m = tagRe.exec(xml))) {
    if (filterTag && !filterTag(m[1])) continue
    const id = readAttr(m[2], 'id')
    if (id) ids.add(id)
  }
  return ids
}

/**
 * 把处理节点的高度抬到不低于 minHeight（垂直居中撑开）
 * @param {string} xml BPMN 2.0 XML 文本
 * @param {number} [minHeight=80] 处理节点最小高度
 * @returns {string} 处理后的 XML；入参非字符串时原样返回
 */
export function normalizeTaskShapeHeights(xml, minHeight = DEFAULT_MIN_HEIGHT) {
  if (!xml || typeof xml !== 'string') return xml
  if (!hasDiagram(xml)) return xml

  // 1) 收集处理节点的 id（只处理真实存在的处理节点，避免误伤同名 id）
  const taskIds = collectTagIds(xml, TASK_TAG_RE)
  if (!taskIds.size) return xml

  // 2) 改写对应 BPMNShape 的 Bounds
  return xml.replace(SHAPE_BLOCK_RE, (block, id) => {
    if (!taskIds.has(id)) return block
    return block.replace(BOUNDS_RE, (bounds, prefix, attrs) => {
      const h = parseFloat(readAttr(attrs, 'height'))
      const y = parseFloat(readAttr(attrs, 'y'))
      if (!isFinite(h) || !isFinite(y) || h >= minHeight) return bounds
      const nextY = round1(y - (minHeight - h) / 2)
      const nextAttrs = setAttrs(attrs, { y: nextY, height: minHeight })
      return '<' + prefix + ':Bounds' + nextAttrs + '/>'
    })
  })
}

/**
 * 把开始/结束事件放大到不小于 size（默认 80），并同步修正与之相连的连线端点与标签位置。
 *
 * 为什么要动连线端点：diagram-js 导入时直接采用 DI 里的 waypoint，不做「裁剪到图形边界」，
 * 只把圆撑大而不动端点，连线会插进圆里（视觉上被实心圆盖住，但拖拽/导出后位置是错的）。
 * 这里按「以圆心为基准等比映射」处理：边线中点仍落在新边线中点上。
 *
 * @param {string} xml BPMN 2.0 XML 文本
 * @param {number} [size=80] 事件最小尺寸（正方形）
 * @returns {string} 处理后的 XML；入参非字符串时原样返回
 */
export function normalizeEventShapeSizes(xml, size = DEFAULT_EVENT_SIZE) {
  if (!xml || typeof xml !== 'string') return xml
  if (!hasDiagram(xml)) return xml

  const eventIds = collectTagIds(xml, EVENT_TAG_RE)
  if (!eventIds.size) return xml

  /** 被放大事件的旧/新包围盒：id -> { x, y, w, h, nw, nh } */
  const resized = new Map()

  // 1) 撑大事件圆（含标签下移）
  const shaped = xml.replace(SHAPE_BLOCK_RE, (block, id) => {
    if (!eventIds.has(id)) return block
    let dy = 0
    const next = block.replace(BOUNDS_RE, (bounds, prefix, attrs) => {
      const w = parseFloat(readAttr(attrs, 'width'))
      const h = parseFloat(readAttr(attrs, 'height'))
      const x = parseFloat(readAttr(attrs, 'x'))
      const y = parseFloat(readAttr(attrs, 'y'))
      if (!isFinite(w) || !isFinite(h) || !isFinite(x) || !isFinite(y)) return bounds
      if (w >= size && h >= size) return bounds
      const nw = Math.max(w, size)
      const nh = Math.max(h, size)
      const nx = round1(x - (nw - w) / 2)
      const ny = round1(y - (nh - h) / 2)
      resized.set(id, { x, y, w, h, nw, nh })
      // 圆的下沿相对原来下移了 (nh - h) / 2，标签跟着下移同样的距离
      dy = (nh - h) / 2
      const nextAttrs = setAttrs(attrs, { x: nx, y: ny, width: nw, height: nh })
      return '<' + prefix + ':Bounds' + nextAttrs + '/>'
    })
    if (!dy) return next
    // 事件自带标签坐标（BPMNLabel 有 Bounds）时，标签不随图形自动重排，需手工下移
    return next.replace(LABEL_BLOCK_RE, (labelBlock, open, inner, close) => {
      const shifted = inner.replace(BOUNDS_RE, (bounds, prefix, attrs) => {
        const ly = parseFloat(readAttr(attrs, 'y'))
        if (!isFinite(ly)) return bounds
        return '<' + prefix + ':Bounds' + setAttrs(attrs, { y: round1(ly + dy) }) + '/>'
      })
      return open + shifted + close
    })
  })
  if (!resized.size) return xml

  // 2) 连线端点跟着事件新边线走（只有首/末端点才需要，中间拐点不动）
  /** 连线 id -> { sourceRef, targetRef } */
  const flows = new Map()
  let fm
  FLOW_TAG_RE.lastIndex = 0
  while ((fm = FLOW_TAG_RE.exec(xml))) {
    const attrs = fm[1]
    const id = readAttr(attrs, 'id')
    const sourceRef = readAttr(attrs, 'sourceRef')
    const targetRef = readAttr(attrs, 'targetRef')
    if (id && sourceRef && targetRef) flows.set(id, { sourceRef, targetRef })
  }
  if (!flows.size) return shaped

  /** 以事件圆心为基准等比映射点坐标 */
  const mapPoint = (info, px, py) => {
    const cx = info.x + info.w / 2
    const cy = info.y + info.h / 2
    const sx = info.nw / info.w
    const sy = info.nh / info.h
    return { x: round1(cx + (px - cx) * sx), y: round1(cy + (py - cy) * sy) }
  }

  return shaped.replace(EDGE_BLOCK_RE, (block, id) => {
    const flow = flows.get(id)
    if (!flow) return block
    const sInfo = resized.get(flow.sourceRef)
    const tInfo = resized.get(flow.targetRef)
    if (!sInfo && !tInfo) return block
    const total = (block.match(WAYPOINT_RE) || []).length
    if (!total) return block
    let i = -1
    return block.replace(WAYPOINT_RE, (wp, prefix, attrs) => {
      i += 1
      const info = i === 0 ? sInfo : (i === total - 1 ? tInfo : null)
      if (!info) return wp
      const px = parseFloat(readAttr(attrs, 'x'))
      const py = parseFloat(readAttr(attrs, 'y'))
      if (!isFinite(px) || !isFinite(py)) return wp
      const np = mapPoint(info, px, py)
      return '<' + prefix + ':waypoint' + setAttrs(attrs, { x: np.x, y: np.y }) + '/>'
    })
  })
}

/**
 * 流程图统一尺寸口径：先放大开始/结束事件，再撑高被压扁的处理节点。
 * 只读流程图（WmProcessDiagram）与设计器画布都走这一个入口，保证「看」和「编辑」是同一张图。
 *
 * @param {string} xml BPMN 2.0 XML 文本
 * @param {{ eventSize?: number, minTaskHeight?: number }} [options]
 * @returns {string} 处理后的 XML
 */
export function normalizeDiagramSizes(xml, options) {
  const opts = options || {}
  const withEvents = normalizeEventShapeSizes(xml, opts.eventSize || DEFAULT_EVENT_SIZE)
  return normalizeTaskShapeHeights(withEvents, opts.minTaskHeight || DEFAULT_MIN_HEIGHT)
}

export default normalizeDiagramSizes
