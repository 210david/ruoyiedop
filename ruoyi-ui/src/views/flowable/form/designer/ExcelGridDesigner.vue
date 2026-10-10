<template>
  <div class="wm-designer" :class="{ 'wm-full': fullscreen }" v-show="active">
    <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="onImagePicked" />
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
            <!-- 选区外框叠加层：单层细线矩形，随选区行列自动定位（替代逐格内阴影，多列/多行左边线不再缺失） -->
            <div v-if="selBoxStyle" class="wm-selbox" :style="selBoxStyle"></div>
            <table class="wm-grid" @mousedown="onBlankMousedown">
              <colgroup>
                <col :style="rhStyle" />
                <col v-for="(w, i) in grid.colWidths" :key="i" :style="{ width: colWStyle(w) }" />
                <col class="wm-fill-col" />
              </colgroup>
              <thead>
                <tr>
                  <th class="wm-corner" :style="rhStyle" @click="selAll"><span class="wm-rz-v" @mousedown.stop.prevent="startRowHeadDrag($event)" @click.stop.prevent></span></th>
                  <th v-for="(w, ci) in grid.colWidths" :key="'c' + ci" :class="{ on: colSel(ci) }"
                    @mousedown.stop.prevent="selCol(ci, $event)" @mouseenter="headerEnter('col', ci, $event)" @contextmenu.prevent.stop="openColCtx($event, ci)">
                    {{ colName(ci) }}<span class="wm-rz-v" @mousedown.stop.prevent="startColDrag($event, ci)" @click.stop.prevent></span>
                  </th>
                  <th class="wm-fill-cell"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, r) in rowViews" :key="r" :style="{ height: grid.rowHeights[r] + 'px' }">
                  <td class="wm-rowhead" :class="{ on: rowSel(r) }" :style="rhStyle"
                    @mousedown.stop.prevent="selRow(r, $event)" @mouseenter="headerEnter('row', r, $event)" @contextmenu.prevent.stop="openRowCtx($event, r)">
                    {{ r + 1 }}<span class="wm-rz-h" @mousedown.stop.prevent="startRowDrag($event, r)" @click.stop.prevent></span>
                  </td>
                  <template v-for="(e, ci) in row" :key="r + '-' + ci">
                    <td v-if="e.kind !== 'skip'" class="wm-cell" :class="[cellCls(e), { editing: editing && editing.r === e.r && editing.c === e.c }]" :style="cellCss(e.cell && e.cell.style)"
                      :rowspan="e.rs || 1" :colspan="e.cs || 1"
                      @mousedown.stop.prevent="onCellDown($event, e.r, e.c)" @mouseenter="onCellEnter($event, e.r, e.c)"
                      @dblclick.stop="startEdit(e.r, e.c)" @contextmenu.prevent.stop="openCtx($event, e.r, e.c)"
                      @dragover.prevent @drop.prevent="onDrop($event, e.r, e.c)">
                      <template v-if="editing && editing.r === e.r && editing.c === e.c">
                        <input class="wm-edit" v-model="editText" v-focus @keyup.enter.prevent="commitEdit" @blur="commitEdit" />
                      </template>
                      <template v-else-if="e.cell">
                        <span v-if="e.cell.type === 'title'" class="wm-t" :class="{ ph: !e.cell.text }">{{ e.cell.text || '标题' }}</span>
                        <span v-else-if="e.cell.type === 'note'" class="wm-t" :class="{ ph: !e.cell.text }">{{ e.cell.text || '说明文字…' }}</span>
                        <span v-else-if="e.cell.type === 'field'" class="wm-f" :class="'attr-' + cellAttr(e.cell)">
                          <i v-if="cellAttr(e.cell) === 'req'" class="wm-attr req" title="必填">*</i>
                          <span class="wm-ctrl" :title="fieldLabel(e.cell.field) + '（' + e.cell.field + '）'">
                            <i class="wm-box"><b>{{ fieldPlaceholder(e.cell.field) }}</b></i>
                            <i v-if="ctrlIcon(e.cell.field)" class="wm-ico">{{ ctrlIcon(e.cell.field) }}</i>
                          </span>
                        </span>
                        <span v-else-if="e.cell.type === 'image'" class="wm-img-wrap" :class="'img-' + ((e.cell.style && e.cell.style.align) || 'center')">
                          <img v-if="e.cell.src" :src="e.cell.src" class="wm-img" alt="图片" />
                          <span v-else class="wm-t ph">图片</span>
                        </span>
                        <span v-else class="wm-t" :class="{ ph: !e.cell.text }"><template v-if="e.cell.text">{{ e.cell.text }}</template><template v-else-if="isSingleActiveCell(e.r, e.c)">双击输入文本</template></span>
                      </template>
                      <template v-else><span v-if="isSingleActiveCell(e.r, e.c)" class="wm-t ph wm-cell-tip">双击输入文本</span></template>
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
        <div><b>选择</b>：单击选格；Shift+单击扩展选区；按住拖动框选；点击行号/列标选中整行/整列；Ctrl+点击 追加/移除选区（单元格、行号、列标均支持；多选后加粗、清除、删行列等批量生效）</div>
        <div><b>复制/粘贴</b>：选中区域后 Ctrl+C 复制整块（含样式/合并/字段绑定），选目标位置 Ctrl+V 粘贴覆盖；Ctrl+Z 撤销 / Ctrl+Y 重做</div>
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
    <!-- 全屏预览（泛微式）：字段渲染真实控件可模拟输入，必填可校验，模拟发起页填写效果 -->
    <el-dialog v-model="preview" :title="'表单预览 · ' + (form.formName || '')" fullscreen append-to-body
      class="wm-preview-dialog" :close-on-click-modal="false" @open="resetPreviewValues">
      <div class="wm-preview-page">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom:14px"
          title="预览模式（数据不会保存）" description="请按实际填写体验表单效果，必填字段提交时会触发校验" />
        <el-form ref="pvFormRef" :model="pvValues" label-position="top">
          <FormGridRender :grid="grid" :fields="fields" :values="pvValues" editable />
        </el-form>
        <div class="wm-preview-actions">
          <el-button type="primary" @click="validatePreview">模拟提交校验</el-button>
          <el-button @click="resetPreviewValues">重 置</el-button>
        </div>
      </div>
    </el-dialog>
    <ResizeDialog v-model:open="rzOpen" :kind="rzKind" :index="rzIndex" :current="rzCurrent" :current-is-pct="rzIsPct" :total-px="rzTotal" @apply="applyResize" />
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
import { buildMatrix, buildRowsView, cellCss, createGrid, findAnchorCell, canMergeRange, mergeRange, splitCell, insertRow, deleteRow, insertCol, deleteCol, resizeRow, resizeCol, hydrateStyle, makeCell, serializeGrid, normalizeGrid, GRID_DEFAULT, hiddenSets, isPct, pctToPx, colWStyle } from './useGridMatrix.js'

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
const selRange = ref(null)          // {r1,c1,r2,c2} 主选区（普通/Shift）
const selRanges = ref([])           // Ctrl+点击 追加的多选区（批量选择）
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

// ---- 全屏预览：模拟发起页填写（数据不保存，重开预览重置） ----
const pvFormRef = ref(null)
const pvValues = reactive({})
function resetPreviewValues() {
  for (const k of Object.keys(pvValues)) delete pvValues[k]
}
function validatePreview() {
  if (!pvFormRef.value) return
  pvFormRef.value.validate(isValid => {
    if (isValid) ElMessage.success('校验通过：必填字段已填写完整')
    else ElMessage.warning('存在未填写的必填字段，请补全后再提交')
  })
}

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
/** 全部有效选区（主选区 + Ctrl 追加选区）：批量操作统一遍历入口；越界残片自动过滤 */
const allRanges = computed(() => {
  const g = grid.value
  const list = []
  if (rect.value) list.push(rect.value)
  for (const t of selRanges.value) {
    if (!t) continue
    const r1 = Math.max(0, t.r1), c1 = Math.max(0, t.c1)
    const r2 = Math.min(g.rows - 1, t.r2), c2 = Math.min(g.cols - 1, t.c2)
    if (r2 >= r1 && c2 >= c1) list.push({ r1, c1, r2, c2 })
  }
  return list
})
/** 点位是否落在任一选区内（渲染高亮用） */
function inAnyRange(r, c) {
  return allRanges.value.some(t => r >= t.r1 && r <= t.r2 && c >= t.c1 && c <= t.c2)
}
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
/** 选区外框叠加层定位：直接从渲染后的真实 DOM（表头 th / 数据行 tr）取矩形，
 *  避免手工累加 rowHeights 与实际渲染高度（内容撑高、表头高度）不一致导致的框线错位 */
const selBoxStyle = computed(() => {
  const t = rect.value
  // Ctrl 多选时选框叠加层不绘制（多段矩形无法用单层外框表达，高亮由单元格样式呈现）
  if (!t || !grid.value || preview.value || selRanges.value.length) return null
  if (t.r1 === t.r2 && t.c1 === t.c2) return null
  const g = grid.value
  const hid = hiddenSets(g)
  const canvas = canvasWrapRef.value && canvasWrapRef.value.querySelector('.wm-canvas')
  const table = canvas && canvas.querySelector('table.wm-grid')
  if (!canvas || !table || !table.tBodies.length || !table.tHead) return null
  // 可见行（隐藏行不渲染）在 tbody 中的下标
  const visRows = []
  for (let r = 0; r < g.rows; r++) if (!hid.rows.has(r)) visRows.push(r)
  const i1 = visRows.indexOf(t.r1)
  const i2 = visRows.indexOf(t.r2)
  const ths = table.tHead.rows[0] ? table.tHead.rows[0].cells : []
  if (i1 < 0 || i2 < 0 || !ths[t.c1 + 1] || !ths[t.c2 + 1]) return null
  const base = canvas.getBoundingClientRect()
  const rTop = table.tBodies[0].rows[i1].getBoundingClientRect()
  const rBottom = table.tBodies[0].rows[i2].getBoundingClientRect()
  const cLeft = ths[t.c1 + 1].getBoundingClientRect()
  const cRight = ths[t.c2 + 1].getBoundingClientRect()
  const x = cLeft.left - base.left
  const y = rTop.top - base.top
  const w = cRight.right - cLeft.left
  const h = rBottom.bottom - rTop.top
  if (w <= 0 || h <= 0) return null
  return { left: x + 'px', top: y + 'px', width: w + 'px', height: h + 'px' }
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
/** 字段格展示属性：设计器右键设置优先，字段必填定义兜底（与 FormGridRender.cellAttr 同口径） */
function cellAttr(cell) {
  if (cell && cell.attr) return cell.attr
  const d = fieldMap.value[cell && cell.field]
  return d && d.required ? 'req' : 'edit'
}
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
/** 占位提示仅在单格选中时显示：多格/整行整列选区内不显示，避免干扰泛微式选中效果 */
function isSingleActiveCell(r, c) {
  const t = rect.value
  return !!t && t.r1 === t.r2 && t.c1 === t.c2 && t.r1 === r && t.c1 === c
}
function cellCls(e) {
  const t = rect.value
  // Ctrl 多选：点位落在任一选区内即高亮；band（灰底）仅按主选区计算
  const inSel = inAnyRange(e.r, e.c)
  // 泛微式多格选区：选区内浅灰底 + 外圈黑色粗框（band-t/b/l/r 标记边缘格）；单格选中为蓝色描边
  const multi = !!t && (t.r1 !== t.r2 || t.c1 !== t.c2)
  const band = !!t && inSel && multi
  const cls = {
    sel: inSel && !band,
    band,
    'band-t': band && e.r === t.r1,
    'band-b': band && e.r === t.r2,
    'band-l': band && e.c === t.c1,
    'band-r': band && e.c === t.c2
  }
  // 空文本占位格（仅为承载样式而创建的 cell.type='text' 且无文字）按 empty 处理，
  // 避免套用 t-text 标签格的浅蓝底，被误认为"设置边框却被标了蓝色背景"
  // 用户输入的普通文本格（无 labelField）同样按 empty 处理：输入后不自动加浅蓝背景，
  // 浅蓝标签底仅保留给通过"插入标签"创建的标签格（cell.labelField 标记）
  let tKind = e.cell ? e.cell.type : 'empty'
  if (e.cell && e.cell.type === 'text') tKind = (e.cell.text && e.cell.labelField) ? 'text' : 'empty'
  cls['t-' + tKind] = true
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
  // 点击其他单元格时先提交未完成的编辑：mousedown.prevent 阻止了默认失焦，blur 不会触发，必须显式提交
  if (editing.value) commitEdit()
  // 格式刷激活态：跳过格式刷/shift 分支，但照常设置选区并进入拖拽，松开时按最终选区应用
  const painting = !!painterStyle.value
  if (!painting && (e.ctrlKey || e.metaKey)) {
    // Ctrl/Cmd+点击：多选批量选择——点位已在某选区内则移除该选区，否则追加单格选区
    const idx = allRanges.value.findIndex(t => r >= t.r1 && r <= t.r2 && c >= t.c1 && c <= t.c2)
    if (idx >= 0) {
      const base = rect.value ? 1 : 0 // allRanges 前段为主选区
      if (idx === 0) selRange.value = null
      else selRanges.value.splice(idx - base, 1)
    } else {
      if (!rect.value) selRange.value = expandSelRange(r, c, r, c)
      else selRanges.value = [...selRanges.value, expandSelRange(r, c, r, c)]
    }
    dragging = false
    return
  }
  if (!painting && selRanges.value.length) selRanges.value = [] // 普通点击清空 Ctrl 追加选区
  if (!painting && e.shiftKey && selRange.value) selRange.value = expandSelRange(selRange.value.r1, selRange.value.c1, r, c)
  else { selRange.value = expandSelRange(r, c, r, c); dragging = true; dragStart = { x: e.clientX, y: e.clientY } }
}
/** 拖拽扩选位移阈值：mousedown 点移动超过该距离才视为有意拖拽框选。
 *  修复"单击却选中两个格"：光标停在格边缘时，mousedown 触发重渲染/字体加载导致浏览器
 *  重新派发 mouseenter，光标在按下瞬间"漂移"进相邻格；位移小于阈值的一律忽略。 */
const DRAG_THRESHOLD = 5
let dragStart = null
function movedPastThreshold(e) {
  return !!dragStart && Math.hypot(e.clientX - dragStart.x, e.clientY - dragStart.y) > DRAG_THRESHOLD
}
function onCellEnter(e, r, c) {
  // 三重防护：dragging 且左键按下且位移超过阈值才扩展选区（过滤残留 dragging、伪 enter、边缘漂移）
  if (dragging && selRange.value && (e.buttons & 1) && movedPastThreshold(e)) selRange.value = expandSelRange(selRange.value.r1, selRange.value.c1, r, c)
}
// 泛微/Excel 选择规则：选区碰到合并格自动扩展为整个合并格（整行/整列），
// 避免选框叠加层只画在部分列、灰色底铺满整格的错位"拆分"观感
function expandSelRange(r1, c1, r2, c2) {
  const g = grid.value
  let a = Math.min(r1, r2), b = Math.min(c1, c2), d = Math.max(r1, r2), e2 = Math.max(c1, c2)
  for (let guard = 0; guard < 8; guard++) {
    let changed = false
    for (const x of g.cells) {
      const rs = x.rs || 1, cs = x.cs || 1
      if (x.r > d || x.r + rs - 1 < a || x.c > e2 || x.c + cs - 1 < b) continue
      if (x.r < a) { a = x.r; changed = true }
      if (x.c < b) { b = x.c; changed = true }
      if (x.r + rs - 1 > d) { d = x.r + rs - 1; changed = true }
      if (x.c + cs - 1 > e2) { e2 = x.c + cs - 1; changed = true }
    }
    if (!changed) break
  }
  return { r1: a, c1: b, r2: d, c2: e2 }
}
// ---- 行列头选择（Excel 式：拖拽多选 / Shift 扩选 / 右键保留批量选区） ----
// 注意：仅左键（button=0）才执行选中，右键 mousedown 不动选区，由 contextmenu 决定去留
const headerDrag = reactive({ mode: null })
function selRow(r, e) {
  if (e && e.button !== 0) return
  closeCtx()
  // Ctrl/Cmd+点击行号：多选批量选择整行——已选该行则移除，否则追加；普通点击清空追加选区
  if (e && (e.ctrlKey || e.metaKey)) {
    const row = { r1: r, c1: 0, r2: r, c2: grid.value.cols - 1 }
    const i = selRanges.value.findIndex(t => t.r1 === r && t.r2 === r && t.c1 === 0 && t.c2 === grid.value.cols - 1)
    if (i >= 0) selRanges.value.splice(i, 1)
    else selRanges.value = [...selRanges.value, row]
    headerDrag.mode = null
    return
  }
  selRanges.value = []
  const shift = e && e.shiftKey && selRange.value && selRange.value.c1 === 0 && selRange.value.c2 === grid.value.cols - 1
  if (shift) selRange.value = { r1: Math.min(selRange.value.r1, r), c1: 0, r2: Math.max(selRange.value.r2, r), c2: grid.value.cols - 1 }
  else selRange.value = { r1: r, c1: 0, r2: r, c2: grid.value.cols - 1 }
  headerDrag.mode = 'row'
}
function selCol(c, e) {
  if (e && e.button !== 0) return
  closeCtx()
  // Ctrl/Cmd+点击列标：多选批量选择整列——已选该列则移除，否则追加
  if (e && (e.ctrlKey || e.metaKey)) {
    const col = { r1: 0, c1: c, r2: grid.value.rows - 1, c2: c }
    const i = selRanges.value.findIndex(t => t.c1 === c && t.c2 === c && t.r1 === 0 && t.r2 === grid.value.rows - 1)
    if (i >= 0) selRanges.value.splice(i, 1)
    else selRanges.value = [...selRanges.value, col]
    headerDrag.mode = null
    return
  }
  selRanges.value = []
  const shift = e && e.shiftKey && selRange.value && selRange.value.r1 === 0 && selRange.value.r2 === grid.value.rows - 1
  if (shift) selRange.value = { r1: 0, c1: Math.min(selRange.value.c1, c), r2: grid.value.rows - 1, c2: Math.max(selRange.value.c2, c) }
  else selRange.value = { r1: 0, c1: c, r2: grid.value.rows - 1, c2: c }
  headerDrag.mode = 'col'
}
function headerEnter(type, idx, e) {
  if (!headerDrag.mode || headerDrag.mode !== type || !selRange.value) return
  // 同 onCellEnter：仅左键按下且位移超过阈值时扩展；Ctrl 多选态不进入拖拽扩选
  if (!e || !(e.buttons & 1) || !movedPastThreshold(e) || e.ctrlKey || e.metaKey) return
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
function selAll() { closeCtx(); selRange.value = { r1: 0, c1: 0, r2: grid.value.rows - 1, c2: grid.value.cols - 1 }; selRanges.value = [] }
function rowSel(r) { const t = rect.value; return !!t && t.r1 <= r && t.r2 >= r }
function colSel(c) { const t = rect.value; return !!t && t.c1 <= c && t.c2 >= c }

// ---- 编辑 ----
function startEdit(r, c) {
  const cell = findAnchorCell(grid.value, r, c)
  if (cell && cell.type === 'field') { selRange.value = expandSelRange(r, c, r, c); openPicker(); return }
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
    // 不再静默拆散合并格：直接写入锚点格，合并保持（字段覆盖整个合并区）
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
    // 不再静默拆散合并格：直接写入锚点格，合并保持（标签覆盖整个合并区）
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
  if (!allRanges.value.length) return
  pushUndo()
  for (const t of allRanges.value) {
    for (const cell of [...grid.value.cells]) {
      if (cell.type === 'field' && cell.field && cell.r >= t.r1 && cell.r <= t.r2 && cell.c >= t.c1 && cell.c <= t.c2) {
        cell.type = 'text'; cell.text = ''; cell.field = undefined; cell.labelField = undefined
      }
    }
  }
}

// ---- 样式操作（作用于全部选区内锚点格，空白格首次设置时创建；Ctrl 多选批量生效） ----
function forUnits(mut) {
  for (const t of allRanges.value) for (let r = t.r1; r <= t.r2; r++) for (let c = t.c1; c <= t.c2; c++) {
    const own = findAnchorCell(grid.value, r, c)
    if (own) { if (own.r === r && own.c === c) mut(own) }
    else { const fresh = makeCell(r, c); grid.value.cells.push(fresh); mut(fresh) }
  }
}
function onSetStyle(p) {
  if (!allRanges.value.length) return
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
  if (!allRanges.value.length) return
  pushUndo()
  const e = p.preset === 'none' ? { s: 0 } : { s: Number(p.style) || 1, c: p.color || '#333333' }
  // 内部横线/竖线（Excel 语义）：按格子在选区内的位置决定各边——水平内线=非首行画上边、非末行画下边；垂直内线=非首列画左边、非末列画右边；选区外边缘一律清除
  const innerH = p.preset === 'hinner'
  const innerV = p.preset === 'vinner'
  const all = ['top', 'right', 'bottom', 'left']
  const g = grid.value
  // 邻格镜像：collapse 表格中同宽同样式边框按"上/左侧格优先"裁决，
  // 单元格自身的上/左边框会被相邻格的默认灰色格线覆盖（表现为"四边框只有右和下可见、仅上/左边框完全不可见"）。
  // 因此把每条边的设置/清除同步镜像到相邻格的对侧边，与 Excel 共享边语义一致，保证边框真实渲染。
  const DELTA = { top: [-1, 0, 'bottom'], bottom: [1, 0, 'top'], left: [0, -1, 'right'], right: [0, 1, 'left'] }
  // 跨度超出的邻格（如上方是更宽的合并格）无法只写重叠段：写它会污染段外区域
  // （表现为"合并格加外框时 A2/F2 上方也带出黑线"）。改为跳过写邻格，
  // 并把锚点该侧边框加粗到 2px（e.w），靠 collapse 裁决"宽者优先"赢过邻格默认灰线。
  const setSide = (cell, side, val) => {
    if (!cell.style || typeof cell.style.borders !== 'object' || !cell.style.borders) cell.style.borders = {}
    cell.style.borders[side] = val
  }
  // Ctrl 多选：内部横线/竖线按各选区独立计算；单选区行为不变
  forUnits(cell => {
    const owner = allRanges.value.find(t => cell.r >= t.r1 && cell.r <= t.r2 && cell.c >= t.c1 && cell.c <= t.c2) || rect.value
    const t = owner || rect.value
    let sides
    if (innerH) sides = all.filter(s => (s === 'top' && cell.r > t.r1) || (s === 'bottom' && cell.r < t.r2))
    else if (innerV) sides = all.filter(s => (s === 'left' && cell.c > t.c1) || (s === 'right' && cell.c < t.c2))
    else if (p.preset === 'all' || p.preset === 'outer' || p.preset === 'none') sides = all
    else sides = [p.preset]
    for (const s of all) {
      const val = sides.includes(s) ? { ...e } : { s: 0 }
      setSide(cell, s, val)
    }
    mirrorBorders(cell)
  })
}
// 遍历全部选区内锚点格（去重、不新建格子）：内容/清除类操作共用，Ctrl 多选批量生效
function eachAnchor(mut) {
  const seen = new Set()
  for (const t of allRanges.value) for (let r = t.r1; r <= t.r2; r++) for (let c = t.c1; c <= t.c2; c++) {
    const cell = findAnchorCell(grid.value, r, c)
    if (cell && !seen.has(cell.id)) { seen.add(cell.id); mut(cell) }
  }
}
// 清除共享边上邻格镜像的对侧边框：边框是成对镜像写入的，只清本格会留下邻格残留黑线
const CLEAR_DELTA = { top: [-1, 0, 'bottom'], bottom: [1, 0, 'top'], left: [0, -1, 'right'], right: [0, 1, 'left'] }
// 共享边镜像（写入方向）：把 cell 已设置的边框镜像到邻格对侧，保证 collapse 表格中边框真实渲染。
// 与 onBorder 的写入逻辑共用；格式刷应用格式时同样调用，解决"复制后框线被邻格默认灰线压掉"的问题。
function mirrorBorders(cell) {
  const g = grid.value
  const rs = cell.rs || 1, cs = cell.cs || 1
  const bump = {}
  for (const s of ['top', 'right', 'bottom', 'left']) {
    const val = cell.style && cell.style.borders && cell.style.borders[s]
    if (!val) continue
    const opp = { top: 'bottom', bottom: 'top', left: 'right', right: 'left' }[s]
    const horizontal = s === 'top' || s === 'bottom'
    const count = horizontal ? cs : rs
    for (let i = 0; i < count; i++) {
      let nr, nc
      if (s === 'top') { nr = cell.r - 1; nc = cell.c + i }
      else if (s === 'bottom') { nr = cell.r + rs; nc = cell.c + i }
      else if (s === 'left') { nr = cell.r + i; nc = cell.c - 1 }
      else { nr = cell.r + i; nc = cell.c + cs }
      if (nr < 0 || nc < 0 || nr >= g.rows || nc >= g.cols) continue
      let n = findAnchorCell(g, nr, nc)
      if (!n) { if (val.s === 0) continue; n = makeCell(nr, nc); g.cells.push(n) }
      if (n === cell) continue // 合并格内部：对侧边仍归属本格，跳过避免自覆盖
      // 邻格沿边方向跨度与共享段不一致（更宽/更高）时跳过写它，标记加粗（宽者优先赢得 collapse 裁决）
      const segStart = horizontal ? cell.c : cell.r
      const segEnd = horizontal ? cell.c + cs - 1 : cell.r + rs - 1
      const nStart = horizontal ? n.c : n.r
      const nEnd = horizontal ? n.c + (n.cs || 1) - 1 : n.r + (n.rs || 1) - 1
      if (val.s !== 0 && (nStart < segStart || nEnd > segEnd)) { bump[s] = true; continue }
      if (!n.style) n.style = hydrateStyle({})
      if (!n.style.borders || typeof n.style.borders !== 'object') n.style.borders = {}
      n.style.borders[opp] = { ...val }
    }
  }
  for (const s of Object.keys(bump)) {
    const cur = cell.style.borders[s]
    if (cur && cur.s !== 0) cell.style.borders[s] = { ...cur, w: 2 }
  }
}
function clearMirrorBorders(cell) {
  const g = grid.value
  const rs = cell.rs || 1, cs = cell.cs || 1
  for (const s of ['top', 'right', 'bottom', 'left']) {
    const [, , opp] = CLEAR_DELTA[s]
    const horizontal = s === 'top' || s === 'bottom'
    const count = horizontal ? cs : rs
    for (let i = 0; i < count; i++) {
      let nr, nc
      if (s === 'top') { nr = cell.r - 1; nc = cell.c + i }
      else if (s === 'bottom') { nr = cell.r + rs; nc = cell.c + i }
      else if (s === 'left') { nr = cell.r + i; nc = cell.c - 1 }
      else { nr = cell.r + i; nc = cell.c + cs }
      const n = findAnchorCell(g, nr, nc)
      if (n && n !== cell && n.style && n.style.borders) delete n.style.borders[opp]
    }
  }
}
function clearStyles() {
  if (!allRanges.value.length) return
  pushUndo()
  forUnits(cell => {
    cell.style = hydrateStyle({})
    clearMirrorBorders(cell)
  })
}
function togglePainter() {
  if (!rect.value || !activeCell.value) { ElMessage.warning('请先选中要吸取样式的单元格'); return }
  painterStyle.value = JSON.parse(JSON.stringify(activeCell.value.style))
  selRange.value = null // 吸取后清空选区，进入目标框选态（泛微同款：按钮保持高亮直到应用）
  ElMessage.success('格式刷已吸取样式，框选目标单元格后松开应用（Esc 取消）')
}
// 格式刷应用：源样式完整复制（含边框共享边镜像），支持多格框选与 Ctrl 多选区；应用后自动退出激活态
function applyPainterTo(r, c) {
  const ranges = allRanges.value.length ? allRanges.value : [expandSelRange(r, c, r, c)]
  const src = painterStyle.value
  if (!src || !ranges.length) { painterStyle.value = null; return }
  pushUndo()
  const seen = new Set()
  for (const t of ranges) for (let rr = t.r1; rr <= t.r2; rr++) for (let cc = t.c1; cc <= t.c2; cc++) {
    const cell = findAnchorCell(grid.value, rr, cc)
    if (!cell || seen.has(cell.id)) continue
    seen.add(cell.id)
    cell.style = hydrateStyle(JSON.parse(JSON.stringify(src)))
    mirrorBorders(cell)
  }
  painterStyle.value = null
}

// ---- 复制/粘贴（Ctrl+C / Ctrl+V：按主选区整块复制，粘贴到当前选区锚点，覆盖目标区） ----
const clipboard = ref(null)
function copySel() {
  const t = rect.value
  if (!t) { ElMessage.warning('请先选中要复制的单元格'); return }
  const seen = new Set()
  const cells = []
  const dr = -t.r1, dc = -t.c1
  for (let r = t.r1; r <= t.r2; r++) for (let c = t.c1; c <= t.c2; c++) {
    const cell = findAnchorCell(grid.value, r, c)
    if (!cell || seen.has(cell.id)) continue
    seen.add(cell.id)
    const snap = JSON.parse(JSON.stringify(cell))
    snap.r = cell.r + dr; snap.c = cell.c + dc
    cells.push(snap)
  }
  clipboard.value = { w: t.c2 - t.c1 + 1, h: t.r2 - t.r1 + 1, cells }
  ElMessage.success(`已复制 ${t.r2 - t.r1 + 1} 行 × ${t.c2 - t.c1 + 1} 列，Ctrl+V 粘贴到目标位置`)
}
function pasteSel() {
  const t = rect.value
  if (!t) { ElMessage.warning('请先选中粘贴目标位置'); return }
  if (!clipboard.value || !clipboard.value.cells.length) { ElMessage.warning('剪贴板为空，请先 Ctrl+C 复制'); return }
  pushUndo()
  const g = grid.value
  for (const src of clipboard.value.cells) {
    const tr = Math.min(t.r1 + src.r, g.rows - 1)
    const tc = Math.min(t.c1 + src.c, g.cols - 1)
    const rs = Math.min(src.rs || 1, g.rows - tr)
    const cs = Math.min(src.cs || 1, g.cols - tc)
    // 覆盖目标区：移除目标 footprint 内的既有锚点格，再写入深拷贝（新 id）
    for (const cell of [...g.cells]) {
      if (cell.r + (cell.rs || 1) - 1 >= tr && cell.r <= tr + rs - 1 && cell.c + (cell.cs || 1) - 1 >= tc && cell.c <= tc + cs - 1) {
        g.cells = g.cells.filter(x => x !== cell)
      }
    }
    const clone = { ...JSON.parse(JSON.stringify(src)), id: 'p' + Date.now() + Math.random().toString(36).slice(2, 6), r: tr, c: tc, rs, cs }
    g.cells.push(clone)
  }
  selRange.value = expandSelRange(t.r1, t.c1, Math.min(t.r1 + clipboard.value.h - 1, g.rows - 1), Math.min(t.c1 + clipboard.value.w - 1, g.cols - 1))
  ElMessage.success('粘贴完成')
}

// ---- 插入图片（etype 6）：选图后写入锚点格，dataURL 存储 ----
const imageInput = ref(null)
function insImage() {
  if (!rect.value) { ElMessage.warning('请先选中要插入图片的单元格'); return }
  if (imageInput.value) imageInput.value.click()
}
function onImagePicked(e) {
  const file = e.target.files && e.target.files[0]
  e.target.value = ''
  if (!file) return
  if (!/^image\//.test(file.type)) { ElMessage.warning('请选择图片文件'); return }
  if (file.size > 2 * 1024 * 1024) { ElMessage.warning('图片不能超过 2MB'); return }
  const reader = new FileReader()
  reader.onload = () => {
    const t = rect.value
    if (!t) return
    pushUndo()
    const cell = findAnchorCell(grid.value, t.r1, t.c1)
    if (cell) {
      if (cell.rs > 1 || cell.cs > 1) { /* 保持合并结构，图片铺满合并格 */ }
      cell.type = 'image'; cell.src = reader.result; cell.field = undefined; cell.text = ''; cell.labelField = undefined; cell.labelLeft = undefined; cell.attr = undefined
    } else {
      grid.value.cells.push(makeCell(t.r1, t.c1, { type: 'image', src: reader.result }))
    }
    selRange.value = expandSelRange(t.r1, t.c1, t.r1, t.c1)
    ElMessage.success('图片已插入')
  }
  reader.readAsDataURL(file)
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
  if (!allRanges.value.length) return
  pushUndo()
  // 全部选区的行按降序删除（Ctrl 多选批量生效）
  const rows = [...new Set(allRanges.value.flatMap(t => { const a = []; for (let r = t.r2; r >= t.r1; r--) a.push(r); return a }))].sort((a, b) => b - a)
  for (const r of rows) deleteRow(grid.value, r)
  selRange.value = null; selRanges.value = []
}
function delCols() {
  if (!allRanges.value.length) return
  pushUndo()
  const cols = [...new Set(allRanges.value.flatMap(t => { const a = []; for (let c = t.c2; c >= t.c1; c--) a.push(c); return a }))].sort((a, b) => b - a)
  for (const c of cols) deleteCol(grid.value, c)
  selRange.value = null; selRanges.value = []
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
  if (!allRanges.value.length) return
  pushUndo()
  eachAnchor(cell => {
    if (cell.type === 'field') { cell.type = 'text'; cell.field = undefined }
    if (cell.type === 'image') cell.src = undefined
    cell.text = ''
    cell.labelField = undefined
    cell.labelLeft = undefined
    cell.attr = undefined
    cell.detail = undefined
    cell.style = hydrateStyle({})
    clearMirrorBorders(cell)
  })
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
const rzIsPct = ref(false)
/** 画布内容区像素总宽（百分比折算基准，随窗口/全屏自适应；canvasWrapRef 见上方预览填充区） */
const canvasTotalPx = computed(() => {
  const w = canvasWrapRef.value && canvasWrapRef.value.clientWidth
  return w && w > 100 ? Math.round(w - ((grid.value && grid.value.rowheadW) || 34)) : 1000
})
function openResize(kind, index) {
  pushUndo()
  rzKind.value = kind
  rzIndex.value = index
  const cur = kind === 'row' ? grid.value.rowHeights[index] : grid.value.colWidths[index]
  rzIsPct.value = kind === 'col' && isPct(cur)
  rzCurrent.value = isPct(cur) ? parseFloat(cur) : cur
  // 百分比折算基准：非百分比列宽的像素总和（基准列原样计入，含拖为 0 的隐藏列）
  rzTotal.value = kind === 'col'
    ? grid.value.colWidths.reduce((a, w) => a + (typeof w === 'number' ? w : pctToPx(w, canvasTotalPx.value)), 0)
    : grid.value.rowHeights.reduce((a, b) => a + b, 0)
  rzOpen.value = true
}
function applyResize({ kind, index, px, pct }) {
  if (kind === 'row') resizeRow(grid.value, index, px)
  else if (pct) resizeCol(grid.value, index, pct)
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
  // 展示属性三项（只读/编辑/必填）仅在字段格右键时出现：非字段格不显示（原为置灰）
  const attrItems = cell && cell.type === 'field' ? [
    { label: (cell.attr === 'ro' ? '✔ ' : '') + '🔒 只读', run: () => setAttr('ro') },
    { label: (!cell.attr || cell.attr === 'edit' ? '✔ ' : '') + '✏ 编辑', run: () => setAttr('edit') },
    { label: (cell.attr === 'req' ? '✔ ' : '') + '❗必填', run: () => setAttr('req') }
  ] : []
  return [
    ...attrItems,
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
 * 批量选择时作用于全部选区内的字段格（Ctrl 多选批量生效）；无选区时作用于被右键的单格。 */
function setAttr(attr) {
  if (allRanges.value.length) {
    pushUndo()
    for (const t of allRanges.value) {
      for (const c of grid.value.cells) {
        if (c.type === 'field' && c.r >= t.r1 && c.r <= t.r2 && c.c >= t.c1 && c.c <= t.c2) c.attr = attr
      }
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
  if (!allRanges.value.length) return
  pushUndo()
  eachAnchor(cell => {
    if (cell.type === 'field') { cell.type = 'text'; cell.field = undefined; cell.attr = undefined; cell.detail = undefined }
    if (cell.type === 'image') cell.src = undefined
    cell.text = ''
    cell.labelField = undefined
    cell.labelLeft = undefined
  })
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
  resizing.mode = 'col'; resizing.idx = ci; resizing.start = e.clientX; resizing.orig = isPct(grid.value.colWidths[ci]) ? pctToPx(grid.value.colWidths[ci], canvasTotalPx.value) : grid.value.colWidths[ci]
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
    ins_image: insImage,
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
  if (e.key === 'Escape') { painterStyle.value = null; selRange.value = null; selRanges.value = []; return }
  if (e.key === 'Delete' || e.key === 'Backspace') { if (rect.value) { clearContent(); e.preventDefault() } }
  else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z' && !e.shiftKey) { if (undoStack.value.length) { undo(); e.preventDefault() } }
  else if ((e.ctrlKey || e.metaKey) && (e.key.toLowerCase() === 'y' || (e.shiftKey && e.key.toLowerCase() === 'z'))) { if (redoStack.value.length) { redo(); e.preventDefault() } }
  else if ((e.ctrlKey || e.metaKey) && ['b', 'i', 'u'].includes(e.key.toLowerCase()) && rect.value) {
    onToggleStyle({ b: 'bold', i: 'italic', u: 'underline' }[e.key.toLowerCase()])
    e.preventDefault()
  }
  else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'c' && rect.value) { copySel(); e.preventDefault() }
  else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'v' && rect.value) { pasteSel(); e.preventDefault() }
}
function onWinMouseup() {
  // 格式刷激活态：拖拽框选目标完毕松开时，对最终选区应用格式并自动退出激活态
  if (painterStyle.value && selRange.value) applyPainterTo(selRange.value.r1, selRange.value.c1)
  dragging = false; headerDrag.mode = null; dragStart = null
}
/** 窗口失焦时复位拖拽状态：mouseup 在窗口外丢失时 dragging 残留，是偶发误扩选的根源之一 */
function onWinBlur() { dragging = false; headerDrag.mode = null; dragStart = null }
function onWinResize() { fillRows() }
onMounted(() => { window.addEventListener('keydown', onKeydown); window.addEventListener('mouseup', onWinMouseup); window.addEventListener('blur', onWinBlur); window.addEventListener('resize', onWinResize); nextTick(fillRows) })
onBeforeUnmount(() => { window.removeEventListener('keydown', onKeydown); window.removeEventListener('mouseup', onWinMouseup); window.removeEventListener('blur', onWinBlur); window.removeEventListener('resize', onWinResize) })
</script>
