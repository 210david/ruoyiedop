/**
 * Excel 网格设计器 — 纯函数内核（不依赖 Vue，可 Node 单测）
 * 数据模型对标泛微 eformdesign：
 *   rowheads/colheads  → rowHeights[]/colWidths[]（px）
 *   ec(id="r,c" + colspan/rowspan) → cells[{ r,c,rs,cs }]
 *   etype(1文本/2字段名/3表单内容/6图片/7明细表/12标签页) → type(title/text/note/field/image/detail/tab)
 */
export const GRID_DEFAULT = { rows: 30, cols: 8, rowH: 30 }
export const FONT_DEF = 'Microsoft YaHei'
export const SIZE_DEF = 12
export const COLOR_DEF = '#333333'
export const BORDER_DEF = { s: 1, c: '#dbe3ec' }

let _seq = 0
export function uid() { return 'c' + Date.now().toString(36) + '_' + (++_seq).toString(36) }

export function createGrid(rows = GRID_DEFAULT.rows, cols = GRID_DEFAULT.cols) {
  return {
    rows, cols,
    rowHeights: Array.from({ length: rows }, () => 30),
    colWidths: Array.from({ length: cols }, (_, i) => (i === 0 ? 90 : 110)),
    rowheadW: 34,
    cells: []
  }
}

export function makeCell(r, c, extra = {}) {
  // style 必须经 hydrateStyle 补全默认键（含 borders），否则空白格首次设置边框时 cell.style.borders 为 undefined 会抛错
  return { id: uid(), r, c, rs: 1, cs: 1, type: 'text', text: '', style: hydrateStyle({}), ...extra }
}

/** 占位矩阵：M[r][c] = {cell,anchor:true} | {skip:true} | null(空白格) */
export function buildMatrix(g) {
  const M = Array.from({ length: g.rows }, () => Array.from({ length: g.cols }, () => null))
  for (const cell of g.cells) {
    const r = Math.max(0, Math.min(cell.r, g.rows - 1))
    const c = Math.max(0, Math.min(cell.c, g.cols - 1))
    for (let i = 0; i < (cell.rs || 1) && r + i < g.rows; i++) {
      for (let j = 0; j < (cell.cs || 1) && c + j < g.cols; j++) {
        if (i === 0 && j === 0) M[r][c] = { cell, anchor: true }
        else if (!M[r + i][c + j]) M[r + i][c + j] = { skip: true, cell }
      }
    }
  }
  return M
}

/** 位置(r,c)所在宿主单元格（含被合并覆盖时返回锚点格） */
export function findAnchorCell(g, r, c) {
  return g.cells.find(x => r >= x.r && r < x.r + x.rs && c >= x.c && c < x.c + x.cs) || null
}

/** 选区可合并：选区内每格的宿主单元格必须完整落在选区内 */
export function canMergeRange(g, r1, c1, r2, c2) {
  if (r2 < r1 || c2 < c1 || r1 < 0 || c1 < 0 || r2 >= g.rows || c2 >= g.cols) return false
  for (let r = r1; r <= r2; r++) for (let c = c1; c <= c2; c++) {
    const cell = findAnchorCell(g, r, c)
    if (!cell) continue
    if (cell.r < r1 || cell.r + cell.rs - 1 > r2 || cell.c < c1 || cell.c + cell.cs - 1 > c2) return false
  }
  return true
}

/** 合并选区：选区内保留一个承载格（优先有内容者），其余丢弃 */
export function mergeRange(g, r1, c1, r2, c2) {
  if (!canMergeRange(g, r1, c1, r2, c2)) return null
  const inside = g.cells.filter(x => x.r >= r1 && x.r + x.rs - 1 <= r2 && x.c >= c1 && x.c + x.cs - 1 <= c2)
  const carrier = inside.find(x => x.type === 'field' || x.type === 'note' || (x.text && x.text.trim())) || inside[0]
  if (carrier) {
    g.cells = g.cells.filter(x => !inside.includes(x) || x === carrier)
    carrier.r = r1; carrier.c = c1
    carrier.rs = r2 - r1 + 1; carrier.cs = c2 - c1 + 1
    return carrier
  }
  const fresh = makeCell(r1, c1, { rs: r2 - r1 + 1, cs: c2 - c1 + 1 })
  g.cells.push(fresh)
  return fresh
}

export function splitCell(g, r, c) {
  const cell = findAnchorCell(g, r, c)
  if (!cell) return null
  cell.rs = 1; cell.cs = 1
  return cell
}

export function insertRow(g, at) {
  g.rowHeights.splice(at, 0, GRID_DEFAULT.rowH)
  g.rows++
  for (const x of g.cells) {
    if (x.r >= at) x.r++
    else if (x.r + x.rs > at) x.rs++
  }
}

export function deleteRow(g, at) {
  if (g.rows <= 1) return
  g.rowHeights.splice(at, 1)
  g.rows--
  g.cells = g.cells.filter(x => x.r !== at)
  for (const x of g.cells) {
    if (x.r > at) x.r--
    else if (x.r + x.rs > at) x.rs--
  }
}

export function insertCol(g, at) {
  g.colWidths.splice(at, 0, 130)
  g.cols++
  for (const x of g.cells) {
    if (x.c >= at) x.c++
    else if (x.c + x.cs > at) x.cs++
  }
}

export function deleteCol(g, at) {
  if (g.cols <= 1) return
  g.colWidths.splice(at, 1)
  g.cols--
  g.cells = g.cells.filter(x => x.c !== at)
  for (const x of g.cells) {
    if (x.c > at) x.c--
    else if (x.c + x.cs > at) x.cs--
  }
}

const PCT_RE = /^\d+(\.\d+)?%$/
/** 百分比列宽（字符串 "N%"，1~100）；其余为像素数值 */
export function isPct(w) { return typeof w === 'string' && PCT_RE.test(w) && parseFloat(w) > 0 && parseFloat(w) <= 100 }
/** 百分比 → 像素（基准为画布内容区总宽） */
export function pctToPx(w, totalPx) { return Math.round((totalPx || 1000) * parseFloat(w) / 100) }

export function resizeRow(g, r, h) { if (g.rowHeights[r] !== undefined) g.rowHeights[r] = Math.max(0, Math.round(h)) }
export function resizeCol(g, c, w) {
  if (g.colWidths[c] === undefined) return
  if (typeof w === 'string') { if (isPct(w)) g.colWidths[c] = w; return }
  g.colWidths[c] = Math.max(0, Math.round(w))
}
/** 列宽渲染值：px 数值 → "Npx"；百分比字符串原样返回（相对画布自适应） */
export function colWStyle(w) { return typeof w === 'number' ? w + 'px' : w }

/** 隐藏的行/列集合（尺寸为 0 即隐藏） */
export function hiddenSets(g) {
  return {
    rows: new Set(g.rowHeights.map((h, i) => (h === 0 ? i : -1)).filter(i => i >= 0)),
    cols: new Set(g.colWidths.map((w, i) => (w === 0 ? i : -1)).filter(i => i >= 0))
  }
}

/** 行视图（隐藏感知）：跳过隐藏行/列，合并格有效跨度自动收缩（Excel 隐藏行为） */
export function buildRowsView(g) {
  const hid = hiddenSets(g)
  const M = buildMatrix(g)
  const rows = []
  for (let r = 0; r < g.rows; r++) {
    if (hid.rows.has(r)) continue
    const line = []
    for (let c = 0; c < g.cols; c++) {
      if (hid.cols.has(c)) continue
      const e = M[r][c]
      if (!e) { line.push({ kind: 'empty', r, c, cell: null, rs: 1, cs: 1 }); continue }
      if (!e.anchor) continue // 被合并覆盖：由锚点格的 td 跨度覆盖
      const cell = e.cell
      let hidR = 0, hidC = 0
      for (let i = 0; i < cell.rs; i++) if (hid.rows.has(r + i)) hidR++
      for (let j = 0; j < cell.cs; j++) if (hid.cols.has(c + j)) hidC++
      const rs = cell.rs - hidR, cs = cell.cs - hidC
      if (rs <= 0 || cs <= 0) continue
      line.push({ kind: 'cell', r, c, cell, rs, cs })
    }
    rows.push(line)
  }
  return rows
}

/** 样式默认值填充（载入时） */
export function hydrateStyle(st) {
  const s = { font: FONT_DEF, fontSize: SIZE_DEF, bold: false, italic: false, underline: false, strike: false, color: COLOR_DEF, bg: '', align: 'left', valign: 'middle', borders: {}, indent: 0, fmt: null }
  if (st) for (const k of Object.keys(st)) s[k] = st[k]
  if (!s.borders || typeof s.borders !== 'object') s.borders = {}
  return s
}

function slimStyle(st) {
  if (!st) return undefined
  const o = {}
  if (st.font && st.font !== FONT_DEF) o.font = st.font
  if (st.fontSize && st.fontSize !== SIZE_DEF) o.fontSize = st.fontSize
  if (st.bold) o.bold = true
  if (st.italic) o.italic = true
  if (st.underline) o.underline = true
  if (st.strike) o.strike = true
  if (st.color && st.color !== COLOR_DEF) o.color = st.color
  if (st.bg) o.bg = st.bg
  if (st.align && st.align !== 'left') o.align = st.align
  if (st.valign && st.valign !== 'middle') o.valign = st.valign
  if (st.indent) o.indent = st.indent
  if (st.fmt) o.fmt = st.fmt
  if (st.borders) {
    const b = {}
    for (const k of ['top', 'right', 'bottom', 'left']) {
      const e = st.borders[k]
      if (e && (e.s === 0 || e.s > 1 || (e.c && e.c !== BORDER_DEF.c))) b[k] = { s: e.s || 1, c: e.c || BORDER_DEF.c }
    }
    if (Object.keys(b).length) o.borders = b
  }
  return Object.keys(o).length ? o : undefined
}

function slimCell(x) {
  const c = { id: x.id, r: x.r, c: x.c, rs: x.rs || 1, cs: x.cs || 1, type: x.type || 'text' }
  if (c.type === 'field' && x.field) c.field = x.field
  if (['title', 'text', 'note', 'detail'].includes(c.type)) c.text = x.text || ''
  if (c.type === 'image' && x.src) c.src = x.src
  if (x.labelField) c.labelField = x.labelField
  if (x.attr) c.attr = x.attr
  if (x.detail) c.detail = x.detail
  const st = slimStyle(x.style)
  if (st) c.style = st
  return c
}

/** 瘦身序列化：空文本格且无样式且未合并的占位格不落盘；样式仅存非默认值 */
export function serializeGrid(g) {
  const cells = g.cells.map(slimCell)
    .filter(c => !(c.type === 'text' && !c.text && !c.style && c.rs === 1 && c.cs === 1))
  return { rows: g.rows, cols: g.cols, rowHeights: g.rowHeights, colWidths: g.colWidths, rowheadW: g.rowheadW || 34, cells }
}

/** 载入规范化：坐标钳制、锚点去重、样式补默认值 */
export function normalizeGrid(obj) {
  const g = createGrid(obj && obj.rows > 0 ? obj.rows : GRID_DEFAULT.rows, obj && obj.cols > 0 ? obj.cols : GRID_DEFAULT.cols)
  if (Array.isArray(obj && obj.rowHeights)) obj.rowHeights.forEach((h, i) => { if (i < g.rows && h >= 20) g.rowHeights[i] = h })
  if (Array.isArray(obj && obj.colWidths)) obj.colWidths.forEach((w, i) => { if (i < g.cols && (isPct(w) || (typeof w === 'number' && w >= 40))) g.colWidths[i] = w })
  g.rowheadW = Math.max(24, Math.min(120, (obj && obj.rowheadW) || 34))
  const seen = new Set()
  for (const raw of ((obj && obj.cells) || [])) {
    if (!raw || typeof raw !== 'object') continue
    const r = Math.max(0, Math.min(raw.r | 0, g.rows - 1))
    const c = Math.max(0, Math.min(raw.c | 0, g.cols - 1))
    const key = r + ',' + c
    if (seen.has(key)) continue
    const cell = { id: raw.id || uid(), r, c, rs: Math.max(1, raw.rs | 0), cs: Math.max(1, raw.cs | 0), type: raw.type || 'text', style: hydrateStyle(raw.style) }
    if (cell.type === 'field') cell.field = raw.field
    if (['title', 'text', 'note', 'detail'].includes(cell.type)) cell.text = raw.text || ''
    if (cell.type === 'image' && raw.src) cell.src = raw.src
    if (raw.labelField) cell.labelField = raw.labelField
    if (raw.attr) cell.attr = raw.attr
    if (raw.detail) cell.detail = raw.detail
    seen.add(key)
    g.cells.push(cell)
  }
  return g
}

/** 单元格样式 → 内联 CSS（设计态与运行态共用） */
// noGrid=true 为运行/预览渲染模式：不显示默认浅灰编辑网格线，s:0（无边框）= 真正无边框，只保留用户绘制的边框
export function cellCss(st, noGrid) {
  const o = {}
  if (!st) return o
  o.fontFamily = st.font || FONT_DEF
  o.fontSize = (st.fontSize || SIZE_DEF) + 'px'
  if (st.bold) o.fontWeight = 700
  if (st.italic) o.fontStyle = 'italic'
  const deco = [st.underline ? 'underline' : '', st.strike ? 'line-through' : ''].filter(Boolean).join(' ')
  if (deco) o.textDecoration = deco
  if (st.color) o.color = st.color
  if (st.bg) o.background = st.bg
  o.textAlign = st.align || 'left'
  o.verticalAlign = st.valign || 'middle'
  if (st.indent) o.textIndent = (st.indent * 8) + 'px'
  const B = st.borders || {}
  const wMap = { 1: '1px', 2: '2px', 3: '1px', 4: '1px', 5: '4px' }
  const sMap = { 1: 'solid', 2: 'solid', 3: 'dashed', 4: 'dotted', 5: 'double' }
  for (const k of ['top', 'right', 'bottom', 'left']) {
    const e = B[k]
    if (!e) continue
    const key = 'border' + k[0].toUpperCase() + k.slice(1)
    // s:0（无边框）≠ border:none：collapse 表格中 none 会把默认浅色背景网格线一并擦除。
    // 泛微口径：无边框 = 清掉用户加的边框、恢复表格默认背景线；只有合并单元格内部才真正无线。
    // e.w：宽度覆盖（px）——共享边邻格跨度超出时，锚点侧边加粗以赢得 collapse 裁决（宽者优先）
    if (e.s === 0) { if (!noGrid) o[key] = '1px solid ' + (BORDER_DEF.c || '#dbe3ec') }
    else o[key] = (e.w ? e.w + 'px' : (wMap[e.s] || '1px')) + ' ' + (sMap[e.s] || 'solid') + ' ' + (e.c || '#333333')
  }
  return o
}
