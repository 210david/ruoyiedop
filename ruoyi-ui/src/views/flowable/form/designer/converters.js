/**
 * 布局转换器：旧 sections（分组式）布局 / 字段清单 → Excel 网格初始化；
 * layoutJson 解析与瘦身序列化。纯函数，无 Vue 依赖。
 */
import { buildMatrix, completeBorderMirror, createGrid, makeCell, normalizeGrid, serializeGrid, hydrateStyle } from './useGridMatrix.js'

const GRID_COLS = 4

/** 模板统一细边框色：标准申请单/分组卡片式共用（需为模块级，stdFormGrid 与 expenseCardGrid 都引用） */
const BORDER = '#e2e8f0'

/** 标准申请单列宽：6 列 = 左留白10% / 标签15% / 值25% / 标签15% / 值25% / 右留白10% */
export const STD_FORM_COL_WIDTHS = ['10%', '15%', '25%', '15%', '25%', '10%']
/** 标准申请单不铺进画布的系统字段（签字意见/打印次数/流程状态由管理员按需手动放置） */
const STD_FORM_SKIP = new Set(['signOpinion', 'printCount', 'procStatus'])

/** 流程系统字段：物理数据列必须保留（发起/办理/列表要用），但不再默认铺进表单画布 */
export const FLOW_SYS_FIELD_KEYS = new Set(['title', 'processNo', 'urgency', 'signOpinion', 'printCount', 'procStatus'])

/** 是否流程系统字段 */
export function isFlowSysField(field) { return !!field && FLOW_SYS_FIELD_KEYS.has(field) }

/**
 * 清掉布局中绑定流程系统字段的单元格（用于历史自动生成的数据）。
 * 历史版本会把标题/紧急程度/签字意见/流程状态/打印次数等自动补进画布，导致用户没添加也会显示。
 * 新版本（layoutJson 带 v>=3 标记）中系统字段由管理员主动放置，不再清理。
 * @returns {number} 被清理的单元格数量
 */
export function stripFlowSysCells(grid) {
  if (!grid || !Array.isArray(grid.cells)) return 0
  const before = grid.cells.length
  // 字段格与它前面的标签格（labelField 指向系统字段）一起清理，避免留下孤立标签
  grid.cells = grid.cells.filter(c => {
    if (!c) return true
    if (c.type === 'field' && isFlowSysField(c.field)) return false
    if (c.labelField && isFlowSysField(c.labelField)) return false
    return true
  })
  return before - grid.cells.length
}

/** 布局版本：3 起系统字段（标题/流程编号/紧急程度/签字意见/打印次数/流程状态）仅在管理员主动放置后显示 */
export const LAYOUT_VERSION = 3

/** 旧 sections 布局 → 网格（span/24 → 列区间映射，分组标题 → 标题行，说明 → 说明格红字） */
export function sectionsToGrid(sections) {
  const grid = createGrid(20, GRID_COLS)
  let r = 0
  for (const sec of sections || []) {
    if (sec.title && r < grid.rows) {
      grid.cells.push(makeCell(r, 0, { type: 'title', text: sec.title, rs: 1, cs: GRID_COLS, style: hydrateStyle({ bold: true, fontSize: 14, align: 'center', bg: '#f5f7fa' }) }))
      r++
    }
    for (const row of sec.rows || []) {
      if (r >= grid.rows) break
      let c = 0
      for (const old of row) {
        const spanCols = Math.max(1, Math.round(((old.span || 12) / 24) * GRID_COLS))
        const use = Math.min(spanCols, GRID_COLS - c)
        if (use <= 0) break
        if (old.note) grid.cells.push(makeCell(r, c, { type: 'note', text: old.note, rs: 1, cs: use, style: hydrateStyle({ color: '#e0342b' }) }))
        else if (old.field) grid.cells.push(makeCell(r, c, { type: 'field', field: old.field, rs: 1, cs: use }))
        c += use
        if (c >= GRID_COLS) break
      }
      r++
    }
  }
  grid.rows = Math.max(r + 8, 25)
  grid.rowHeights = Array.from({ length: grid.rows }, () => 30)
  return grid
}

/**
 * 标准申请单版式（对标请假申请表单）：
 *   第1行：流程名称（整行居中标题）
 *   第2行：流程编号（右侧标签 + 值）
 *   第3行：标题独占一行
 *   其后：其余字段按表单字段顺序两两成行（标签15% + 值25% ×2）
 *   多行文本、附件类字段独占一行，值跨 65%
 * 6 列固定占比：10% / 15% / 25% / 15% / 25% / 10%，表格区域统一细边框。
 * @param {Array} fields 字段清单（主表字段，可含流程系统字段）
 * @param {string} formName 流程名称（表单名称）
 */
export function stdFormGrid(fields, formName) {
  const list = (fields || []).filter(f => f && f.field && (!f.table || f.table === 'main'))
  const has = k => list.some(f => f.field === k)
  const grid = createGrid(30, 6)
  // 分组卡片式列宽（对标费用报销表单）：左右留白各 1%，标签 16% / 值 33% ×2
  grid.colWidths = ['1%', '16%', '33%', '16%', '33%', '1%']
  const cells = []
  const labelCell = (r, c, f) => makeCell(r, c, { type: 'text', text: f.label || f.field, labelField: f.field, style: hydrateStyle({ align: 'center' }) })
  // 流程名称
  cells.push(makeCell(0, 0, { type: 'title', text: formName || '表单标题', rs: 1, cs: 6, style: hydrateStyle({ bold: true, fontSize: 16, align: 'center' }) }))
  let r = 1
  // 流程编号：右侧标签 + 值（提交后自动生成）
  if (has('processNo')) {
    cells.push(makeCell(r, 3, { type: 'text', text: '流程编号：', style: hydrateStyle({ align: 'right', color: '#475569' }) }))
    cells.push(makeCell(r, 4, { type: 'field', field: 'processNo' }))
    r++
  }
  // 标题独占一行，值跨标签+值+标签列
  if (has('title')) {
    cells.push(makeCell(r, 1, { type: 'text', text: '标题', labelField: 'title', style: hydrateStyle({ align: 'center' }) }))
    cells.push(makeCell(r, 2, { type: 'field', field: 'title', rs: 1, cs: 3 }))
    r++
  }
  const startRow = r
  const seen = new Set(['title', 'processNo'])
  let slot = 0 // 0=本行左半待放；1=本行右半待放
  for (const f of list) {
    if (seen.has(f.field) || STD_FORM_SKIP.has(f.field)) continue
    seen.add(f.field)
    const solo = f.form === 'textarea' || f.form === 'attach'
    if (solo) {
      if (slot === 1) { r++; slot = 0 }
      cells.push(labelCell(r, 1, f))
      cells.push(makeCell(r, 2, { type: 'field', field: f.field, rs: 1, cs: 3 }))
      r++
    } else if (slot === 0) {
      cells.push(labelCell(r, 1, f))
      cells.push(makeCell(r, 2, { type: 'field', field: f.field }))
      slot = 1
    } else {
      cells.push(labelCell(r, 3, f))
      cells.push(makeCell(r, 4, { type: 'field', field: f.field }))
      r++
      slot = 0
    }
  }
  if (slot === 1) r++
  const endRow = r - 1
  grid.cells = cells
  grid.rows = Math.max(endRow + 12, 30)
  grid.rowHeights = Array.from({ length: grid.rows }, () => 30)
  // 边框从「标题」行开始画（无标题字段时从字段区首行开始）：流程编号行不画线框；
  // 标题行的上边线即整张表格的起始线
  const frameStart = has('title') ? startRow - 1 : startRow
  if (endRow >= frameStart) paintRange(grid, frameStart, 1, endRow, 4, { s: 1, c: '#333333' })
  // 补齐共享边镜像：paintRange 只给框内格写边，框外侧邻格（流程编号行、左右空白列）没有对侧边框；
  // collapse 表格下相邻格无边框的一侧会赢过另一侧的深色边框，导致「标题」上边线被上一行的浅灰格线盖掉。
  // 载入已保存布局时 normalizeGrid 会做同样的补齐（所以预览正常），套用模板当场生成时也必须补齐。
  completeBorderMirror(grid)
  return grid
}

/** 给矩形区域（含空白格）统一画边框：设计态/运行态只渲染 style 上有边框的格，空白格也要补出来 */
function paintRange(grid, r1, c1, r2, c2, border) {
  const M = buildMatrix(grid)
  for (let r = r1; r <= r2; r++) {
    for (let c = c1; c <= c2; c++) {
      const e = M[r] && M[r][c]
      if (e && !e.anchor) continue // 合并格覆盖区由锚点格携带边框
      let cell = e ? e.cell : null
      if (!cell) {
        cell = makeCell(r, c, { type: 'text', text: '' })
        grid.cells.push(cell)
        M[r][c] = { cell, anchor: true }
      }
      if (!cell.style) cell.style = hydrateStyle({})
      cell.style.borders = { top: { ...border }, right: { ...border }, bottom: { ...border }, left: { ...border } }
    }
  }
}

/** 字段清单 → 网格（标准申请单版式，等价于设计器「从字段一键初始化」） */
export function fieldsToGrid(fields) {
  return stdFormGrid(fields, '表单标题')
}

/** 明细列初始清单：由字段定义生成（与设计器 initDtColumns 同口径） */
function detailColumnsOf(fields, dt) {
  return (fields || []).filter(f => f && f.table === dt).map(f => ({ field: f.field, label: f.label, width: 0, sum: false, required: !!f.required }))
}

/**
 * 分组卡片式版式（对标费用报销表单当前设计）：
 *   第1行：流程名称（整行居中标题）
 *   第2行：流程编号（右侧标签 + 值）
 *   「申请信息」分组卡片：字段两两成行（标签浅灰底细边框 + 值），多行文本/附件独占一行
 *   每张明细表各成一个分组卡片，明细占位条整行放置（双击进入明细编辑）
 * 运行态由 FormGridRender 识别 type=group 渲染为分组卡片（与费用报销表单一致）。
 * @param {Array} fields 字段清单（主表字段 + 明细表字段）
 * @param {string} formName 表单名称
 */
export function expenseCardGrid(fields, formName) {
  const list = (fields || []).filter(f => f && f.field && (!f.table || f.table === 'main'))
  const has = k => list.some(f => f.field === k)
  const grid = createGrid(30, 6)
  // 分组卡片式列宽（对标费用报销表单）：左右留白各 1%，标签 16% / 值 33% ×2
  grid.colWidths = ['1%', '16%', '33%', '16%', '33%', '1%']
  const cells = []
  // 样式口径（与费用报销表单一致）：标签浅灰底/灰蓝文字，线框统一浅色细实线
  const labelCell = (r, c, f) => makeCell(r, c, { type: 'text', text: f.label || f.field, labelField: f.field, style: hydrateStyle({ align: 'center', bg: '#f8fafc', color: '#475569' }) })
  cells.push(makeCell(0, 0, { type: 'title', text: formName || '表单标题', rs: 1, cs: 6, style: hydrateStyle({ bold: true, fontSize: 16, align: 'center' }) }))
  let r = 1
  if (has('processNo')) {
    cells.push(makeCell(r, 3, { type: 'text', text: '流程编号：', style: hydrateStyle({ align: 'right', color: '#475569' }) }))
    cells.push(makeCell(r, 4, { type: 'field', field: 'processNo' }))
    r++
  }
  // 分组卡片标题行：运行态渲染为分组卡片（卡片内首行字段已由 FormGridRender 留白）
  cells.push(makeCell(r, 0, { type: 'group', text: '申请信息', rs: 1, cs: 6 }))
  r++
  const startRow = r
  const seen = new Set(['title', 'processNo'])
  let slot = 0
  for (const f of list) {
    if (seen.has(f.field) || STD_FORM_SKIP.has(f.field)) continue
    seen.add(f.field)
    const solo = f.form === 'textarea' || f.form === 'attach'
    if (solo) {
      if (slot === 1) { r++; slot = 0 }
      cells.push(labelCell(r, 1, f))
      cells.push(makeCell(r, 2, { type: 'field', field: f.field, rs: 1, cs: 3 }))
      r++
    } else if (slot === 0) {
      cells.push(labelCell(r, 1, f))
      cells.push(makeCell(r, 2, { type: 'field', field: f.field }))
      slot = 1
    } else {
      cells.push(labelCell(r, 3, f))
      cells.push(makeCell(r, 4, { type: 'field', field: f.field }))
      r++
      slot = 0
    }
  }
  if (slot === 1) r++
  // 明细表：各成一个分组卡片，明细占位条整行放置
  const dts = [...new Set((fields || []).filter(f => f && f.table && f.table !== 'main').map(f => f.table))]
  dts.forEach((dt, i) => {
    const name = '明细表' + (i + 1)
    cells.push(makeCell(r, 0, { type: 'group', text: name, rs: 1, cs: 6 }))
    r++
    cells.push(makeCell(r, 0, {
      type: 'detail', text: name, rs: 1, cs: 6,
      detail: { table: dt, title: name, columns: detailColumnsOf(fields, dt), props: { allowAdd: true, allowEdit: true, allowDelete: true, mustAdd: false, defaultRows: 1 } }
    }))
    r++
  })
  grid.cells = cells
  grid.rows = Math.max(r + 12, 30)
  grid.rowHeights = Array.from({ length: grid.rows }, () => 30)
  // 字段区统一细边框：必须在 grid.cells/rows 就位后再画，否则 buildMatrix 拿到空表、画出的边框格会被整表覆盖丢失
  if (r - dts.length * 2 - 1 >= startRow) paintRange(grid, startRow, 1, r - dts.length * 2 - 1, 4, { s: 1, c: BORDER })
  // 补齐共享边镜像：理由同 stdFormGrid，避免框外侧邻格的无边框赢过框内深色边框
  completeBorderMirror(grid)
  return grid
}

/** layoutJson 字符串 → { mode, sections, grid }；旧结构（无 mode）自动识别为 section */
export function parseLayout(json) {
  let obj = {}
  try { obj = JSON.parse(json || '{}') || {} } catch (e) { obj = {} }
  const sections = Array.isArray(obj.sections) ? obj.sections : []
  const mode = obj.mode === 'grid' ? 'grid' : (obj.grid ? 'grid' : (obj.mode === 'section' ? 'section' : 'section'))
  // 旧版本（v<3）会自动把系统字段铺进画布：先清理，避免用户没添加也会显示；
  // v>=3 布局里系统字段是管理员主动放置的，原样保留
  if (obj.grid && !(Number(obj.v) >= LAYOUT_VERSION)) stripFlowSysCells(obj.grid)
  const grid = obj.grid ? normalizeGrid(obj.grid) : null
  return { mode: grid ? 'grid' : (mode === 'grid' && !grid ? 'section' : mode), sections, grid }
}

/** 布局对象 → layoutJson 字符串（网格走瘦身序列化；双布局并存时都保留） */
export function serializeLayout(layout) {
  const out = { mode: (layout && layout.mode) || 'section', v: LAYOUT_VERSION }
  if (layout && layout.sections && layout.sections.length) out.sections = layout.sections
  if (layout && layout.grid) out.grid = serializeGrid(layout.grid)
  return JSON.stringify(out)
}
