/**
 * 布局转换器：旧 sections（分组式）布局 / 字段清单 → Excel 网格初始化；
 * layoutJson 解析与瘦身序列化。纯函数，无 Vue 依赖。
 */
import { createGrid, makeCell, normalizeGrid, serializeGrid, hydrateStyle } from './useGridMatrix.js'

const GRID_COLS = 4

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

/** 字段清单 → 网格（标题行 + 两栏字段，对标泛微"从字段初始化"） */
export function fieldsToGrid(fields) {
  const grid = createGrid(20, GRID_COLS)
  let r = 0
  grid.cells.push(makeCell(r, 0, { type: 'title', text: '表单标题', rs: 1, cs: GRID_COLS, style: hydrateStyle({ bold: true, fontSize: 16, align: 'center' }) }))
  r++
  let c = 0
  for (const f of fields || []) {
    if (!f || !f.field) continue
    if (c + 2 > GRID_COLS) { r++; c = 0 }
    if (r >= grid.rows) break
    grid.cells.push(makeCell(r, c, { type: 'field', field: f.field, rs: 1, cs: 2 }))
    c += 2
  }
  grid.rows = Math.max(r + 10, 30)
  grid.rowHeights = Array.from({ length: grid.rows }, () => 30)
  return grid
}

/** layoutJson 字符串 → { mode, sections, grid }；旧结构（无 mode）自动识别为 section */
export function parseLayout(json) {
  let obj = {}
  try { obj = JSON.parse(json || '{}') || {} } catch (e) { obj = {} }
  const sections = Array.isArray(obj.sections) ? obj.sections : []
  const mode = obj.mode === 'grid' ? 'grid' : (obj.grid ? 'grid' : (obj.mode === 'section' ? 'section' : 'section'))
  const grid = obj.grid ? normalizeGrid(obj.grid) : null
  return { mode: grid ? 'grid' : (mode === 'grid' && !grid ? 'section' : mode), sections, grid }
}

/** 布局对象 → layoutJson 字符串（网格走瘦身序列化；双布局并存时都保留） */
export function serializeLayout(layout) {
  const out = { mode: (layout && layout.mode) || 'section' }
  if (layout && layout.sections && layout.sections.length) out.sections = layout.sections
  if (layout && layout.grid) out.grid = serializeGrid(layout.grid)
  return JSON.stringify(out)
}
