<template>
  <div class="wm-designer" :class="{ 'wm-full': fullscreen }" v-show="active">
    <MenuBar :preview="preview" @cmd="onCmd" @help="helpOpen = true" @save="$emit('save')" @toggle-preview="preview = !preview" />
    <FormatToolbar v-if="!preview" :has-sel="!!rect" :pos-label="posLabel" :style-obj="activeStyle"
      :can-merge="canMergeSel" :can-split="canSplitSel" :painter="!!painterStyle"
      @set="onSetStyle" @toggle="onToggleStyle" @align="v => onSetStyle({ k: 'align', v })"
      @valign="v => onSetStyle({ k: 'valign', v })" @indent="onIndent" @border="onBorder"
      @merge="doMerge" @split="doSplit" @cell-dialog="onCellDialog"
      @ins-row="insRowBelow" @ins-col="insColRight" @del-row="delRows" @del-col="delCols"
      @clear-content="clearContent" @clear-format="clearStyles" @clear-all="clearAll"
      @painter="togglePainter" />
    <template v-if="grid">
      <div class="wm-body">
        <div class="wm-canvas-wrap" v-show="!preview" ref="canvasWrapRef">
          <div class="wm-canvas" :class="{ 'no-lines': !gridLines }">
            <table class="wm-grid" @mousedown="onBlankMousedown">
              <colgroup>
                <col :style="rhStyle" />
                <col v-for="(w, i) in grid.colWidths" :key="i" :style="{ width: w + 'px' }" />
                <col class="wm-fill-col" />
              </colgroup>
              <thead>
                <tr>
                  <th class="wm-corner" :style="rhStyle" @click="selAll"><span class="wm-rz-v" @mousedown.stop.prevent="startRowHeadDrag($event)" @click.stop.prevent></span></th>
                  <th v-for="(w, ci) in grid.colWidths" :key="'c' + ci" :class="{ on: colSel(ci) }"
                    @mousedown.stop.prevent="selCol(ci, $event)" @mouseenter="headerEnter('col', ci)" @contextmenu.prevent.stop="openColCtx($event, ci)">
                    {{ colName(ci) }}<span class="wm-rz-v" @mousedown.stop.prevent="startColDrag($event, ci)" @click.stop.prevent></span>
                  </th>
                  <th class="wm-fill-cell"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, r) in rowViews" :key="r" :style="{ height: grid.rowHeights[r] + 'px' }">
                  <td class="wm-rowhead" :class="{ on: rowSel(r) }" :style="rhStyle"
                    @mousedown.stop.prevent="selRow(r, $event)" @mouseenter="headerEnter('row', r)" @contextmenu.prevent.stop="openRowCtx($event, r)">
                    {{ r + 1 }}<span class="wm-rz-h" @mousedown.stop.prevent="startRowDrag($event, r)" @click.stop.prevent></span>
                  </td>
                  <template v-for="(e, ci) in row" :key="r + '-' + ci">
                    <td v-if="e.kind !== 'skip'" class="wm-cell" :class="cellCls(e)" :style="cellCss(e.cell && e.cell.style)"
                      :rowspan="e.rs || 1" :colspan="e.cs || 1"
                      @mousedown.stop.prevent="onCellDown($event, e.r, e.c)" @mouseenter="onCellEnter(e.r, e.c)"
                      @dblclick.stop="startEdit(e.r, e.c)" @contextmenu.prevent.stop="openCtx($event, e.r, e.c)"
                      @dragover.prevent @drop.prevent="onDrop($event, e.r, e.c)">
                      <template v-if="editing && editing.r === e.r && editing.c === e.c">
                        <input class="wm-edit" v-model="editText" v-focus @keyup.enter.prevent="commitEdit" @blur="commitEdit" />
                      </template>
                      <template v-else-if="e.cell">
                        <span v-if="e.cell.type === 'title'" class="wm-t" :class="{ ph: !e.cell.text }">{{ e.cell.text || '标题' }}</span>
                        <span v-else-if="e.cell.type === 'note'" class="wm-t" :class="{ ph: !e.cell.text }">{{ e.cell.text || '说明文字…' }}</span>
                        <span v-else-if="e.cell.type === 'field'" class="wm-f">
                          <i v-if="e.cell.attr === 'req'" class="wm-attr req" title="必填">*</i>
                          <i v-else-if="e.cell.attr === 'ro'" class="wm-attr ro" title="只读">👁</i>
                          <span class="wm-ctrl" :title="fieldLabel(e.cell.field) + '（' + e.cell.field + '）'">
                            <i class="wm-box"><b>{{ fieldPlaceholder(e.cell.field) }}</b></i>
                            <i v-if="ctrlIcon(e.cell.field)" class="wm-ico">{{ ctrlIcon(e.cell.field) }}</i>
                          </span>
                        </span>
                        <span v-else class="wm-t" :class="{ ph: !e.cell.text }"><template v-if="e.cell.text">{{ e.cell.text }}</template><template v-else-if="e.r === rect?.r1 && e.c === rect?.c1">双击输入文本</template></span>
                      </template>
                      <template v-else><span v-if="e.r === rect?.r1 && e.c === rect?.c1" class="wm-t ph wm-cell-tip">双击输入文本</span></template>
                    </td>
                  </template>
                  <td class="wm-fill-cell"></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <PropPanel v-show="!preview" :form-name="form.formName" :form-key="form.formKey" :fields="fields"
          :placed="placedSet" :placed-labels="placedLabelSet" :active-cell="activeCell" :field-defs="fieldMap"
          @bind="f => bindField(f.field || f)" @insert-label="insertLabel" @clear-bind="clearBind" @pick="openPicker"
          @patch="patchActive" @set="onSetStyle" @toggle="onToggleStyle" />
      </div>
    </template>
    <div v-if="ctx.show" class="wm-ctx" :style="{ left: ctx.x + 'px', top: ctx.y + 'px' }">
      <div v-for="(it, i) in ctxItems" :key="i" class="wm-ctx-i" :class="{ dis: it.dis, danger: it.danger }" @click="!it.dis && it.run()">{{ it.label }}</div>
    </div>
    <el-drawer v-model="helpOpen" title="Excel 网格设计器 · 操作说明" size="430px" append-to-body>
      <div class="wm-help-list">
        <div><b>选择</b>：单击选格；Shift+单击扩展选区；按住拖动框选；点击行号/列标选中整行/整列</div>
        <div><b>格式化</b>：顶部工具条设置字体/字号/颜色/对齐/边框，作用于选中格</div>
        <div><b>合并/拆分</b>：选中区域后点工具条"合/拆"</div>
        <div><b>格式刷</b>：选中样式格 → 点"刷" → 单击目标格应用</div>
        <div><b>双击</b>：标题/文本/说明格原地编辑；字段格打开绑定选择</div>
        <div><b>标签/字段</b>：完全独立添加；拖入字段只放字段控件，需要标签时单独拖入标签</div>
        <div><b>行高列宽</b>：拖动行号下缘/列标右缘调整</div>
        <div><b>右键</b>：插入/删除行列、合并拆分、转类型、绑定、清除</div>
        <div><b>快捷键</b>：Ctrl+Z 撤销 / Ctrl+Y 重做 / Delete 或 Backspace 清除选中单元格 / Ctrl+B 加粗 / Ctrl+I 斜体 / Ctrl+U 下划线</div>
      </div>
    </el-drawer>
    <el-dialog v-model="pickerOpen" title="绑定字段" width="480px" append-to-body :close-on-click-modal="false">
      <el-select v-model="pickerField" filterable placeholder="搜索并选择字段" style="width: 100%">
        <el-option v-for="f in availableFields" :key="f.field" :label="f.label + '（' + f.field + '）'" :value="f.field" />
      </el-select>
      <template #footer>
        <el-button type="primary" @click="confirmPicker">确 定</el-button>
        <el-button @click="pickerOpen = false">取 消</el-button>
      </template>
    </el-dialog>
    <CellFormatDialog v-model="fmtOpen" :cell="activeCell" />
    <!-- 全屏预览（泛微式：预览弹出新页面全屏展示表单效果） -->
    <el-dialog v-model="preview" :title="'表单预览 · ' + (form.formName || '')" fullscreen append-to-body
      class="wm-preview-dialog" :close-on-click-modal="false">
      <div class="wm-preview-page">
        <FormGridRender :grid="grid" :fields="fields" />
      </div>
    </el-dialog>
    <ResizeDialog v-model:open="rzOpen" :kind="rzKind" :index="rzIndex" :current="rzCurrent" :total-px="rzTotal" @apply="applyResize" />
  </div>
</template>

<script setup name="WmExcelGridDesigner">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import MenuBar from './DesignerMenuBar.vue'
import FormatToolbar from './FormatToolbar.vue'
import CellFormatDialog from './CellFormatDialog.vue'
import ResizeDialog from './ResizeDialog.vue'
import PropPanel from './PropPanel.vue'
import FormGridRender from '@/components/FormGridRender.vue'
import './designer.scss'
import { buildMatrix, buildRowsView, cellCss, createGrid, findAnchorCell, canMergeRange, mergeRange, splitCell, insertRow, deleteRow, insertCol, deleteCol, resizeRow, resizeCol, hydrateStyle, makeCell, serializeGrid, normalizeGrid } from './useGridMatrix.js'

const props = defineProps({
  modelValue: { type: Object, default: null },
  fields: { type: Array, default: () => [] },
  form: { type: Object, default: () => ({}) },
  active: Boolean,
  fullscreen: Boolean
})
const emit = defineEmits(['update:modelValue', 'save'])

if (!props.modelValue) emit('update:modelValue', createGrid())
const grid = computed(() => props.modelValue)
watch(grid, g => { if (g && (!Array.isArray(g.rowHeights) || g.rowHeights.length !== g.rows)) g.rowHeights = Array.from({ length: g.rows }, (_, i) => (g.rowHeights && g.rowHeights[i]) || 30) })

// ---- 选择与编辑 ----
const selRange = ref(null)          // {r1,c1,r2,c2}
const editing = ref(null)           // {r,c}
const editText = ref('')
const painterStyle = ref(null)
const preview = ref(false)
const gridLines = ref(true)
const helpOpen = ref(false)
const pickerOpen = ref(false)
const pickerField = ref(undefined)
const fmtOpen = ref(false)
const canvasWrapRef = ref(null)

/** 按画布可视高度自动补行：网格始终填满画布区，表下不留空白 */
function fillRows() {
  const g = grid.value
  const wrap = canvasWrapRef.value
  if (!g || !wrap || preview.value) return
  const need = Math.ceil((wrap.clientHeight - 8) / (GRID_DEFAULT.rowH || 30))
  if (need > g.rows) {
    g.rows = need
    while (g.rowHeights.length < need) g.rowHeights.push(30)
  }
}
watch([() => props.fullscreen, () => props.active, preview], () => nextTick(fillRows))
watch(grid, () => nextTick(fillRows))
let dragging = false

const rect = computed(() => {
  const s = selRange.value
  if (!s) return null
  return { r1: Math.min(s.r1, s.r2), c1: Math.min(s.c1, s.c2), r2: Math.max(s.r1, s.r2), c2: Math.max(s.c1, s.c2) }
})
const activeCell = computed(() => {
  const t = rect.value
  return t ? findAnchorCell(grid.value, t.r1, t.c1) : null
})
const activeStyle = computed(() => (activeCell.value ? activeCell.value.style : null))
const canMergeSel = computed(() => {
  const t = rect.value
  return !!t && (t.r1 !== t.r2 || t.c1 !== t.c2 || (activeCell.value && (activeCell.value.rs > 1 || activeCell.value.cs > 1))) &&
    canMergeRange(grid.value, t.r1, t.c1, t.r2, t.c2)
})
const canSplitSel = computed(() => !!activeCell.value && (activeCell.value.rs > 1 || activeCell.value.cs > 1))
const fieldMap = computed(() => {
  const m = {}
  for (const f of props.fields || []) m[f.field] = f
  return m
})
const placedSet = computed(() => {
  const s = new Set()
  for (const c of grid.value.cells) if (c.type === 'field' && c.field) s.add(c.field)
  return s
})
/** 标签使用状态独立跟踪；兼容历史布局中与字段同名且位于字段左侧的文本格。残留空标签不视为已使用。 */
const placedLabelSet = computed(() => {
  const s = new Set()
  for (const c of grid.value.cells) if (c.labelField && c.text) s.add(c.labelField)
  for (const c of grid.value.cells) {
    if (c.type !== 'field' || !c.field) continue
    const left = grid.value.cells.find(x => x.r === c.r && x.c + (x.cs || 1) === c.c && x.type === 'text')
    if (left && left.text === fieldLabel(c.field)) s.add(c.field)
  }
  return s
})
/** 绑定下拉可选项：仅未使用的字段；当前格已绑定的字段保留可选（便于换绑） */
const availableFields = computed(() => {
  const cur = activeCell.value && activeCell.value.field
  return (props.fields || []).filter(f => !placedSet.value.has(f.field) || f.field === cur)
})
const posLabel = computed(() => {  const t = rect.value
  if (!t) return ''
  const cell = activeCell.value
  let desc = '空白格'
  if (cell) {
    if (cell.type === 'field') desc = fieldLabel(cell.field) + (cell.rs > 1 || cell.cs > 1 ? `（合并 ${cell.rs}x${cell.cs}）` : '')
    else if (cell.type === 'title') desc = '标题行'
    else if (cell.type === 'note') desc = '说明'
    else desc = cell.text ? cell.text.slice(0, 10) : '文本格'
  }
  return `R${t.r1 + 1} ${colName(t.c1)} · ${desc}`
})
const rowViews = computed(() => {
  if (!grid.value) return []
  // 隐藏感知行视图：拖到 0 的行/列从渲染中剔除，合并格跨度自动收缩
  return buildRowsView(grid.value)
})
const hasHidden = computed(() => {
  const g = grid.value
  if (!g) return false
  return g.colWidths.some(w => w === 0) || g.rowHeights.some(h => h === 0)
})
function unhideAll() {
  const g = grid.value
  if (!hasHidden.value) return
  pushUndo()
  g.colWidths = g.colWidths.map(w => (w === 0 ? 110 : w))
  g.rowHeights = g.rowHeights.map(h => (h === 0 ? 30 : h))
}

function colName(i) { let s = '', n = i + 1; while (n > 0) { n--; s = String.fromCharCode(65 + (n % 26)) + s; n = Math.floor(n / 26) } return s }
/** 序号列宽（可拖拽调整，默认 34px） */
const rhStyle = computed(() => { const w = (grid.value && grid.value.rowheadW) || 34; return { width: w + 'px', minWidth: w + 'px', maxWidth: w + 'px' } })
function fieldLabel(f) { const d = fieldMap.value[f]; return d ? d.label : (f || '未绑定') }
function ctrlIcon(f) {
  const d = fieldMap.value[f]
  if (!d) return ''
  return { browse: '⌕', date: '▣', datetime: '▣', select: '▾', radio: '◉', attach: '📎', number: '123', check: '✓', switch: '◉' }[d.form] || ''
}
/** 设计态展示真实字段名称与控件类型，避免字段格看起来像空白表单。 */
function fieldPlaceholder(f) {
  const d = fieldMap.value[f]
  if (!d) return f || '未绑定字段'
  const suffix = { select: '（请选择）', radio: '（请选择）', browse: '（点击选择）', date: '（选择日期）', datetime: '（选择时间）', attach: '（上传附件）', check: '（勾选）', switch: '（开关）' }[d.form] || ''
  return (d.label || f) + suffix
}
function cellCls(e) {
  const t = rect.value
  const inSel = !!t && e.r >= t.r1 && e.r <= t.r2 && e.c >= t.c1 && e.c <= t.c2
  // 多格选择（含整行/整列/全表/框选）：泛微式浅色底纹 + 选区外圈描边，不逐格描边；仅单格描边
  const multi = !!t && (t.r1 !== t.r2 || t.c1 !== t.c2)
  const band = !!t && inSel && multi
  const cls = {
    sel: inSel && !band,
    band,
    lead: !!t && inSel && e.r === t.r1 && e.c === t.c1,
    'band-t': band && e.r === t.r1,
    'band-b': band && e.r === t.r2,
    'band-l': band && e.c === t.c1,
    'band-r': band && e.c === t.c2
  }
  cls['t-' + (e.cell ? e.cell.type : 'empty')] = true
  return cls
}

// ---- 撤销/重做（快照栈，50 步） ----
const undoStack = ref([])
const redoStack = ref([])
function pushUndo() {
  undoStack.value.push(JSON.stringify(serializeGrid(grid.value)))
  if (undoStack.value.length > 50) undoStack.value.shift()
  redoStack.value = []
}
function undo() {
  if (!undoStack.value.length) return
  redoStack.value.push(JSON.stringify(serializeGrid(grid.value)))
  emit('update:modelValue', normalizeGrid(JSON.parse(undoStack.value.pop())))
}
function redo() {
  if (!redoStack.value.length) return
  undoStack.value.push(JSON.stringify(serializeGrid(grid.value)))
  emit('update:modelValue', normalizeGrid(JSON.parse(redoStack.value.pop())))
}

// ---- 选择操作 ----
function onBlankMousedown(e) {
  if (e.button !== 0) return
  if (!e.target.closest('td.wm-cell') && !e.target.closest('th') && !e.target.closest('td.wm-rowhead')) { selRange.value = null }
}
function onCellDown(e, r, c) {
  if (e.button !== 0) return // 右键/中键不重置选区，由 contextmenu 决定
  closeCtx()
  if (painterStyle.value) { applyPainterTo(r, c); return }
  if (e.shiftKey && selRange.value) selRange.value = { r1: selRange.value.r1, c1: selRange.value.c1, r2: r, c2: c }
  else { selRange.value = { r1: r, c1: c, r2: r, c2: c }; dragging = true }
}
function onCellEnter(r, c) { if (dragging && selRange.value) selRange.value = { ...selRange.value, r2: r, c2: c } }
// ---- 行列头选择（Excel 式：拖拽多选 / Shift 扩选 / 右键保留批量选区） ----
// 注意：仅左键（button=0）才执行选中，右键 mousedown 不动选区，由 contextmenu 决定去留
const headerDrag = reactive({ mode: null })
function selRow(r, e) {
  if (e && e.button !== 0) return
  closeCtx()
  const shift = e && e.shiftKey && selRange.value && selRange.value.c1 === 0 && selRange.value.c2 === grid.value.cols - 1
  if (shift) selRange.value = { r1: Math.min(selRange.value.r1, r), c1: 0, r2: Math.max(selRange.value.r2, r), c2: grid.value.cols - 1 }
  else selRange.value = { r1: r, c1: 0, r2: r, c2: grid.value.cols - 1 }
  headerDrag.mode = 'row'
}
function selCol(c, e) {
  if (e && e.button !== 0) return
  closeCtx()
  const shift = e && e.shiftKey && selRange.value && selRange.value.r1 === 0 && selRange.value.r2 === grid.value.rows - 1
  if (shift) selRange.value = { r1: 0, c1: Math.min(selRange.value.c1, c), r2: grid.value.rows - 1, c2: Math.max(selRange.value.c2, c) }
  else selRange.value = { r1: 0, c1: c, r2: grid.value.rows - 1, c2: c }
  headerDrag.mode = 'col'
}
function headerEnter(type, idx) {
  if (!headerDrag.mode || headerDrag.mode !== type || !selRange.value) return
  if (type === 'row') selRange.value = { r1: selRange.value.r1, c1: 0, r2: idx, c2: grid.value.cols - 1 }
  else selRange.value = { r1: 0, c1: selRange.value.c1, r2: grid.value.rows - 1, c2: idx }
}
function openRowCtx(e, r) {
  const t = rect.value
  const inSel = !!t && t.c1 === 0 && t.c2 === grid.value.cols - 1 && r >= t.r1 && r <= t.r2
  if (!inSel) selRange.value = { r1: r, c1: 0, r2: r, c2: grid.value.cols - 1 }
  ctx.x = e.clientX
  ctx.y = e.clientY
  ctx.r = r
  ctx.c = 0
  ctx.show = true
}
function openColCtx(e, c) {
  const t = rect.value
  const inSel = !!t && t.r1 === 0 && t.r2 === grid.value.rows - 1 && c >= t.c1 && c <= t.c2
  if (!inSel) selRange.value = { r1: 0, c1: c, r2: grid.value.rows - 1, c2: c }
  ctx.x = e.clientX
  ctx.y = e.clientY
  ctx.r = 0
  ctx.c = c
  ctx.show = true
}
function selAll() { closeCtx(); selRange.value = { r1: 0, c1: 0, r2: grid.value.rows - 1, c2: grid.value.cols - 1 } }
function rowSel(r) { const t = rect.value; return !!t && t.r1 <= r && t.r2 >= r }
function colSel(c) { const t = rect.value; return !!t && t.c1 <= c && t.c2 >= c }

// ---- 编辑 ----
function startEdit(r, c) {
  const cell = findAnchorCell(grid.value, r, c)
  if (cell && cell.type === 'field') { selRange.value = { r1: r, c1: c, r2: r, c2: c }; openPicker(); return }
  if (cell && cell.rs > 1) { r = cell.r; c = cell.c }
  editing.value = { r, c }
  editText.value = cell ? (cell.text || '') : ''
}
const vFocus = { mounted: el => el.focus() }
function commitEdit() {
  if (!editing.value) return
  const { r, c } = editing.value
  const cell = findAnchorCell(grid.value, r, c)
  const val = editText.value
  pushUndo()
  if (cell && ['title', 'text', 'note'].includes(cell.type)) cell.text = val
  else if (!cell && val.trim()) grid.value.cells.push(makeCell(r, c, { type: 'text', text: val }))
  else if (cell && !['title', 'text', 'note'].includes(cell.type) && !cell.field) { cell.type = 'text'; cell.text = val }
  editing.value = null
}
function patchActive(patch) {
  const cell = activeCell.value
  if (!cell) return
  pushUndo()
  Object.assign(cell, patch)
}

// ---- 字段绑定 ----
function openPicker() {
  if (!rect.value) { ElMessage.warning('请先选中单元格'); return }
  pickerField.value = activeCell.value ? activeCell.value.field : undefined
  pickerOpen.value = true
}
/** 单格字段绑定：用于字段属性面板和绑定弹窗，保留精细排版能力。 */
function placeField(r, c, field) {
  const cell = findAnchorCell(grid.value, r, c)
  if (cell) {
    if (cell.rs > 1 || cell.cs > 1) splitCell(grid.value, cell.r, cell.c)
    cell.type = 'field'; cell.field = field; cell.text = ''; cell.labelLeft = undefined
  } else {
    grid.value.cells.push(makeCell(r, c, { type: 'field', field }))
  }
}
function bindField(field) {
  const t = rect.value
  if (!t) { ElMessage.warning('请先选中要放置字段的单元格'); return }
  if (!field) return
  pushUndo()
  placeField(t.r1, t.c1, field)
}
/** 标签独立添加为文本格，可与字段控件分别排版。 */
function insertLabel(f) {
  const t = rect.value
  if (!t) { ElMessage.warning('请先选中要放置标签的单元格'); return }
  if (!f) return
  pushUndo()
  placeLabel(t.r1, t.c1, f.field)
}
function placeLabel(r, c, field) {
  const label = fieldLabel(field)
  const cell = findAnchorCell(grid.value, r, c)
  if (cell) {
    if (cell.rs > 1 || cell.cs > 1) splitCell(grid.value, cell.r, cell.c)
    cell.type = 'text'; cell.text = label; cell.field = undefined; cell.labelField = field
  } else {
    grid.value.cells.push(makeCell(r, c, { type: 'text', text: label, labelField: field }))
  }
}
function confirmPicker() {
  if (pickerField.value) bindField(pickerField.value)
  pickerOpen.value = false
}
function clearBind() {
  const t = rect.value
  if (!t) return
  pushUndo()
  for (const cell of [...grid.value.cells]) {
    if (cell.type === 'field' && cell.field && cell.r >= t.r1 && cell.r <= t.r2 && cell.c >= t.c1 && cell.c <= t.c2) {
      cell.type = 'text'; cell.text = ''; cell.field = undefined; cell.labelField = undefined
    }
  }
}

// ---- 样式操作（作用于选区内全部锚点格，空白格首次设置时创建） ----
function forUnits(mut) {
  const t = rect.value
  if (!t) return
  for (let r = t.r1; r <= t.r2; r++) for (let c = t.c1; c <= t.c2; c++) {
    const own = findAnchorCell(grid.value, r, c)
    if (own) { if (own.r === r && own.c === c) mut(own) }
    else { const fresh = makeCell(r, c); grid.value.cells.push(fresh); mut(fresh) }
  }
}
function onSetStyle(p) {
  if (!rect.value) return
  pushUndo()
  forUnits(cell => { cell.style[p.k] = p.v })
}
function onToggleStyle(k) {
  if (!rect.value || !activeCell.value) return
  const next = !activeCell.value.style[k]
  pushUndo()
  forUnits(cell => { cell.style[k] = next })
}
function onBorder(p) {
  if (!rect.value) return
  pushUndo()
  const e = p.preset === 'none' ? { s: 0 } : { s: Number(p.style) || 1, c: p.color || '#333333' }
  forUnits(cell => {
    const sides = (p.preset === 'all' || p.preset === 'outer') ? ['top', 'right', 'bottom', 'left'] : [p.preset]
    for (const s of sides) cell.style.borders[s] = { ...e }
  })
}
function clearStyles() {
  if (!rect.value) return
  pushUndo()
  forUnits(cell => { cell.style = hydrateStyle({}) })
}
function togglePainter() {
  if (!rect.value || !activeCell.value) { ElMessage.warning('请先选中要吸取样式的单元格'); return }
  painterStyle.value = JSON.parse(JSON.stringify(activeCell.value.style))
  ElMessage.success('格式刷已吸取样式，点击目标格应用（Esc 取消）')
}
function applyPainterTo(r, c) {
  const cell = findAnchorCell(grid.value, r, c)
  if (!cell) return
  pushUndo()
  cell.style = hydrateStyle(JSON.parse(JSON.stringify(painterStyle.value)))
  painterStyle.value = null
}

// ---- 合并/拆分 ----
function doMerge() {
  const t = rect.value
  if (!t) return
  pushUndo()
  const carrier = mergeRange(grid.value, t.r1, t.c1, t.r2, t.c2)
  if (carrier) selRange.value = { r1: carrier.r, c1: carrier.c, r2: carrier.r, c2: carrier.c }
  else { redoStack.value.pop(); ElMessage.warning('选区包含跨区单元格，无法合并') }
}
function doSplit() {
  const cell = activeCell.value
  if (!cell) return
  pushUndo()
  splitCell(grid.value, cell.r, cell.c)
  selRange.value = { r1: cell.r, c1: cell.c, r2: cell.r, c2: cell.c }
}

// ---- 插入/删除行列 ----
function anchorPos() {
  const t = rect.value
  return t ? { r: t.r2, c: t.c2 } : { r: grid.value.rows - 1, c: grid.value.cols - 1 }
}
function insRowAbove() { pushUndo(); insertRow(grid.value, anchorPos().r) }
function insRowBelow() { pushUndo(); insertRow(grid.value, anchorPos().r + 1) }
function insColLeft() { pushUndo(); insertCol(grid.value, anchorPos().c) }
function insColRight() { pushUndo(); insertCol(grid.value, anchorPos().c + 1) }
function delRows() {
  const t = rect.value
  if (!t) return
  pushUndo()
  for (let r = t.r2; r >= t.r1; r--) deleteRow(grid.value, r)
  selRange.value = null
}
function delCols() {
  const t = rect.value
  if (!t) return
  pushUndo()
  for (let c = t.c2; c >= t.c1; c--) deleteCol(grid.value, c)
  selRange.value = null
}
function insTitle() {
  pushUndo()
  const at = anchorPos().r + 1
  insertRow(grid.value, at)
  grid.value.cells.push(makeCell(at, 0, { type: 'title', text: '标题', rs: 1, cs: grid.value.cols, style: hydrateStyle({ bold: true, fontSize: 16, align: 'center' }) }))
  selRange.value = { r1: at, c1: 0, r2: at, c2: 0 }
}
function insNote() {
  pushUndo()
  const at = anchorPos().r + 1
  insertRow(grid.value, at)
  grid.value.cells.push(makeCell(at, 0, { type: 'note', text: '', rs: 1, cs: grid.value.cols, style: hydrateStyle({ color: '#e0342b' }) }))
  selRange.value = { r1: at, c1: 0, r2: at, c2: 0 }
}
function clearGrid() { pushUndo(); grid.value.cells = []; selRange.value = null }

// ---- 缩进 / 全部清除 / 单元格格式 ----
function onIndent(dir) {
  if (!rect.value) return
  pushUndo()
  forUnits(cell => { cell.style.indent = Math.max(0, Math.min(8, (cell.style.indent || 0) + dir)) })
}
function clearAll() {
  const t = rect.value
  if (!t) return
  pushUndo()
  for (const cell of [...grid.value.cells]) {
    if (cell.r >= t.r1 && cell.r <= t.r2 && cell.c >= t.c1 && cell.c <= t.c2) {
      if (cell.type === 'field') { cell.type = 'text'; cell.field = undefined }
      cell.text = ''
      cell.labelField = undefined
      cell.style = hydrateStyle({})
    }
  }
}
function onCellDialog() {
  if (!activeCell.value) { ElMessage.warning('请先选中单元格'); return }
  pushUndo()
  fmtOpen.value = true
}

// ---- 行高/列宽设置弹窗（右键菜单入口，复刻泛微） ----
const rzOpen = ref(false)
const rzKind = ref('row')
const rzIndex = ref(0)
const rzCurrent = ref(30)
const rzTotal = ref(1000)
function openResize(kind, index) {
  pushUndo()
  rzKind.value = kind
  rzIndex.value = index
  rzCurrent.value = kind === 'row' ? grid.value.rowHeights[index] : grid.value.colWidths[index]
  rzTotal.value = kind === 'col' ? grid.value.colWidths.reduce((a, b) => a + b, 0) : grid.value.rowHeights.reduce((a, b) => a + b, 0)
  rzOpen.value = true
}
function applyResize({ kind, index, px }) {
  if (kind === 'row') resizeRow(grid.value, index, px)
  else resizeCol(grid.value, index, px)
}
/** 对整行/整列选区批量应用设置 */
function applyResizeToSel(kind) {
  const t = rect.value
  if (!t) return
  const fullRows = t.c1 === 0 && t.c2 === grid.value.cols - 1
  const fullCols = t.r1 === 0 && t.r2 === grid.value.rows - 1
  if (kind === 'row' && fullRows) return openResize('row', t.r2)
  if (kind === 'col' && fullCols) return openResize('col', t.c2)
  // 局部选区：按锚点所在行列设置
  return openResize(kind, kind === 'row' ? t.r2 : t.c2)
}

function formTitle() { return (props.form && props.form.formName) || '表单标题' }
function replaceGrid(g) { emit('update:modelValue', g); selRange.value = null }

/** 样式模板：tmp_fields 从字段初始化；tmp_apply/tmp_fee 复刻参考图版式（标题+红说明+两栏字段[+明细区]） */
function applyTemplate(kind) {
  pushUndo()
  const cols = 4
  if (kind === 'tmp_fields') {
    const cells = [makeCell(0, 0, { type: 'title', text: formTitle(), rs: 1, cs: cols, style: hydrateStyle({ bold: true, fontSize: 16, align: 'center' }) })]
    let r = 1, c = 0
    for (const f of (props.fields || [])) {
      if (!f.field) continue
      if (c + 2 > cols) { r++; c = 0 }
      cells.push(makeCell(r, c, { type: 'text', text: f.label, style: hydrateStyle({ align: 'center' }) }))
      cells.push(makeCell(r, c + 1, { type: 'field', field: f.field, labelLeft: true }))
      c += 2
    }
    const g = createGrid(Math.max(r + 10, 25), cols)
    g.cells = cells
    replaceGrid(g)
    return
  }
  const cells = [
    makeCell(0, 0, { type: 'title', text: formTitle(), rs: 1, cs: cols, style: hydrateStyle({ bold: true, fontSize: 16, align: 'center' }) }),
    makeCell(1, 0, { type: 'note', text: kind === 'tmp_fee' ? '有报销明细的必须填写明细表' : '请如实填写以下内容', rs: 1, cs: cols, style: hydrateStyle({ color: '#e0342b', align: 'center', bg: '#fff7cc' }) })
  ]
  let r = 2, c = 0
  for (const f of (props.fields || [])) {
    if (!f.field) continue
    if (c + 2 > cols) { r++; c = 0 }
    cells.push(makeCell(r, c, { type: 'text', text: f.label, style: hydrateStyle({ align: 'center' }) }))
    cells.push(makeCell(r, c + 1, { type: 'field', field: f.field, labelLeft: true }))
    c += 2
  }
  if (kind === 'tmp_fee' && (props.fields || []).some(f => f.field)) {
    r += 1
    cells.push(makeCell(r, 0, { type: 'text', text: '明细表1', rs: 1, cs: cols, style: hydrateStyle({ bold: true, bg: '#f5f7fa', align: 'center' }) }))
    for (let i = 1; i <= 2; i++) cells.push(makeCell(r + i, 0, { type: 'text', text: '', rs: 1, cs: cols }))
    r += 2
  }
  const g = createGrid(Math.max(r + 10, 25), cols)
  g.cells = cells
  replaceGrid(g)
}

// ---- 拖拽落格：标签与字段完全独立，拖入字段只放字段控件（泛微一致） ----
function onDrop(e, r, c) {
  const data = e.dataTransfer ? e.dataTransfer.getData('text/plain') || '' : ''
  const sep = data.indexOf(':')
  if (sep < 0) return
  const kind = data.slice(0, sep)
  const field = data.slice(sep + 1)
  if (!field || !['wmlabel', 'wmfield'].includes(kind)) return
  pushUndo()
  if (kind === 'wmlabel') placeLabel(r, c, field)
  else placeField(r, c, field)
}

// ---- 右键菜单 ----
const ctx = reactive({ show: false, x: 0, y: 0, r: 0, c: 0 })
function openCtx(e, r, c) {
  const t = rect.value
  const inside = !!t && r >= t.r1 && r <= t.r2 && c >= t.c1 && c <= t.c2
  if (!inside) {
    // 右键点在选区外：重置为被点单元格（Excel 行为）；点在选区内：保留批量选区
    const cell = findAnchorCell(grid.value, r, c)
    if (cell) selRange.value = { r1: cell.r, c1: cell.c, r2: cell.r + cell.rs - 1, c2: cell.c + cell.cs - 1 }
    else selRange.value = { r1: r, c1: c, r2: r, c2: c }
  }
  ctx.x = e.clientX
  ctx.y = e.clientY
  ctx.r = r
  ctx.c = c
  ctx.show = true
}
function closeCtx() { ctx.show = false }
const ctxItems = computed(() => {
  const cell = findAnchorCell(grid.value, ctx.r, ctx.c)
  const t = rect.value
  return [
    { label: (cell && cell.type === 'field' && cell.attr === 'ro' ? '✔ ' : '') + '👁 只读', run: () => setAttr('ro'), dis: !cell || cell.type !== 'field' },
    { label: (cell && cell.type === 'field' && (!cell.attr || cell.attr === 'edit') ? '✔ ' : '') + '✏ 编辑', run: () => setAttr('edit'), dis: !cell || cell.type !== 'field' },
    { label: (cell && cell.type === 'field' && cell.attr === 'req' ? '✔ ' : '') + '❗必填', run: () => setAttr('req'), dis: !cell || cell.type !== 'field' },
    { label: '上方插入一行', run: insRowAbove },
    { label: '下方插入一行', run: insRowBelow },
    { label: '左侧插入一列', run: insColLeft },
    { label: '右侧插入一列', run: insColRight },
    { label: '删除选中行', run: delRows, dis: !t, danger: true },
    { label: '删除选中列', run: delCols, dis: !t, danger: true },
    { label: '合并单元格', run: doMerge, dis: !canMergeSel.value },
    { label: '拆分单元格', run: doSplit, dis: !canSplitSel.value },
    { label: '绑定字段…', run: () => openPicker(), dis: !cell },
    { label: '清除绑定', run: clearBind, dis: !cell || cell.type !== 'field' },
    { label: '转为说明行', run: () => toType('note'), dis: !cell },
    { label: '转为标题行', run: () => toType('title'), dis: !cell },
    { label: '行高…', run: () => applyResizeToSel('row'), dis: !t },
    { label: '列宽…', run: () => applyResizeToSel('col'), dis: !t },
    { label: '清除内容', run: clearContent, dis: !t },
    { label: '清除格式', run: clearStyles, dis: !t },
    { label: '取消隐藏行列', run: unhideAll, dis: !hasHidden.value },
    { label: '删除该单元格', run: delCell, dis: !cell, danger: true }
  ]
})
/** 设置字段格展示属性：只读 ro / 编辑 edit / 必填 req（泛微右键同款）。
 * 批量选择时作用于选区内全部字段格；无选区时作用于被右键的单格。 */
function setAttr(attr) {
  const t = rect.value
  if (t) {
    pushUndo()
    for (const c of grid.value.cells) {
      if (c.type === 'field' && c.r >= t.r1 && c.r <= t.r2 && c.c >= t.c1 && c.c <= t.c2) c.attr = attr
    }
    return
  }
  const cell = findAnchorCell(grid.value, ctx.r, ctx.c)
  if (!cell || cell.type !== 'field') return
  pushUndo()
  cell.attr = attr
}
function toType(type) {
  const cell = findAnchorCell(grid.value, ctx.r, ctx.c)
  if (!cell) return
  pushUndo()
  if (type === 'note' || type === 'title') {
    cell.type = type
    cell.text = cell.text || ''
    cell.field = undefined
    cell.c = 0
    cell.cs = grid.value.cols
    cell.rs = 1
  } else { cell.type = 'text'; cell.text = ''; cell.field = undefined; cell.labelField = undefined }
}
function clearContent() {
  const t = rect.value
  if (!t) return
  pushUndo()
  for (const cell of [...grid.value.cells]) {
    if (cell.r >= t.r1 && cell.r <= t.r2 && cell.c >= t.c1 && cell.c <= t.c2) {
      if (cell.type === 'field') { cell.type = 'text'; cell.field = undefined }
      cell.text = ''
      cell.labelField = undefined
    }
  }
}
function delCell() {
  const cell = findAnchorCell(grid.value, ctx.r, ctx.c)
  if (!cell) return
  pushUndo()
  grid.value.cells = grid.value.cells.filter(x => x !== cell)
  selRange.value = null
}

// ---- 行高/列宽拖拽 ----
const resizing = reactive({ mode: null, idx: -1, start: 0, orig: 0 })
function startColDrag(e, ci) {
  if (e.button !== 0) return
  pushUndo()
  resizing.mode = 'col'; resizing.idx = ci; resizing.start = e.clientX; resizing.orig = grid.value.colWidths[ci]
  document.addEventListener('mousemove', onResizeMove)
  document.addEventListener('mouseup', onResizeEnd)
}
function startRowDrag(e, ri) {
  if (e.button !== 0) return
  pushUndo()
  resizing.mode = 'row'; resizing.idx = ri; resizing.start = e.clientY; resizing.orig = grid.value.rowHeights[ri]
  document.addEventListener('mousemove', onResizeMove)
  document.addEventListener('mouseup', onResizeEnd)
}
function onResizeMove(e) {
  if (!resizing.mode) return
  if (resizing.mode === 'col') resizeCol(grid.value, resizing.idx, resizing.orig + e.clientX - resizing.start)
  else if (resizing.mode === 'row') resizeRow(grid.value, resizing.idx, resizing.orig + e.clientY - resizing.start)
  else if (resizing.mode === 'rowhead') grid.value.rowheadW = Math.max(24, Math.min(120, Math.round(resizing.orig + e.clientX - resizing.start)))
}
function onResizeEnd() {
  resizing.mode = null
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', onResizeEnd)
}
/** 序号列宽拖拽（行号列不在 colWidths 内，单独调整 rowheadW） */
function startRowHeadDrag(e) {
  if (e.button !== 0) return
  pushUndo()
  resizing.mode = 'rowhead'; resizing.start = e.clientX; resizing.orig = grid.value.rowheadW || 34
  document.addEventListener('mousemove', onResizeMove)
  document.addEventListener('mouseup', onResizeEnd)
}

// ---- 菜单命令分发 ----
function onCmd(cmd) {
  closeCtx()
  if (cmd === 'tmp_clear') return clearGrid()
  if (cmd.startsWith('tmp_')) return applyTemplate(cmd)
  const map = {
    fmt_gridlines: () => { gridLines.value = !gridLines.value },
    ins_row_above: insRowAbove, ins_row_below: insRowBelow, ins_col_left: insColLeft, ins_col_right: insColRight,
    ins_title: insTitle, ins_note: insNote, fld_bind: openPicker, fld_clear: clearBind,
    sty_clear: clearStyles, sty_painter: togglePainter
  }
  const fn = map[cmd]
  if (fn) return fn()
  if (cmd === 'fmt_panel') return ElMessage.info('单元格格式在右侧属性面板编辑')
  if (cmd === 'fld_info') return showFieldInfo()
  ElMessage.info('该功能属于 P1/P2 阶段，敬请期待')
}
function showFieldInfo() {
  const cell = activeCell.value
  if (!cell || cell.type !== 'field' || !cell.field) { ElMessage.info('当前格未绑定字段'); return }
  const d = fieldMap.value[cell.field]
  ElMessage.info(d ? '字段信息：' + d.label + ' / ' + d.field + ' / ' + (d.form || 'text') + ' / 必填:' + (d.required ? '是' : '否') : '字段定义未找到：' + cell.field)
}

// ---- 键盘 ----
function onKeydown(e) {
  if (!props.active || preview.value) return
  const tag = (e.target.tagName || '').toLowerCase()
  if (tag === 'input' || tag === 'textarea' || e.target.isContentEditable) return
  if (e.key === 'Escape') { painterStyle.value = null; selRange.value = null; return }
  if (e.key === 'Delete' || e.key === 'Backspace') { if (rect.value) { clearContent(); e.preventDefault() } }
  else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z' && !e.shiftKey) { if (undoStack.value.length) { undo(); e.preventDefault() } }
  else if ((e.ctrlKey || e.metaKey) && (e.key.toLowerCase() === 'y' || (e.shiftKey && e.key.toLowerCase() === 'z'))) { if (redoStack.value.length) { redo(); e.preventDefault() } }
  else if ((e.ctrlKey || e.metaKey) && ['b', 'i', 'u'].includes(e.key.toLowerCase()) && rect.value) {
    onToggleStyle({ b: 'bold', i: 'italic', u: 'underline' }[e.key.toLowerCase()])
    e.preventDefault()
  }
}
function onWinMouseup() { dragging = false; headerDrag.mode = null }
function onWinResize() { fillRows() }
onMounted(() => { window.addEventListener('keydown', onKeydown); window.addEventListener('mouseup', onWinMouseup); window.addEventListener('resize', onWinResize); nextTick(fillRows) })
onBeforeUnmount(() => { window.removeEventListener('keydown', onKeydown); window.removeEventListener('mouseup', onWinMouseup); window.removeEventListener('resize', onWinResize) })
</script>
