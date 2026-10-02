<template>
  <div class="fgr-wrap">
    <table class="fgr-table" v-if="grid">
      <colgroup><col v-for="(w, i) in grid.colWidths" :key="i" :style="{ width: w + 'px' }" /><col class="fgr-fill-col" /></colgroup>
      <tbody>
        <tr v-for="(row, r) in rowViews" :key="r" :style="{ height: grid.rowHeights[r] + 'px' }">
          <template v-for="(e, ci) in row" :key="r + '-' + ci">
            <td v-if="e.kind !== 'skip'" :rowspan="e.rs || 1" :colspan="e.cs || 1"
              class="fgr-cell" :class="e.cell ? 't-' + e.cell.type : ''" :style="cellCss(e.cell && e.cell.style)">
              <template v-if="e.cell">
                <div v-if="e.cell.type === 'title'" class="fgr-title">{{ e.cell.text || '' }}</div>
                <div v-else-if="e.cell.type === 'note'" class="fgr-note">{{ e.cell.text || '' }}</div>
                <div v-else-if="e.cell.type === 'field'" class="fgr-field">
                  <!-- 标签为独立文本格，字段格仅渲染控件/值 -->
                  <template v-if="editable && values">
                    <el-form-item :prop="e.cell.field"
                      :rules="fieldReq(e.cell.field) || cellAttr(e.cell) === 'req' ? [{ required: true, message: '请填写' + fieldLabel(e.cell.field), trigger: ['blur', 'change'] }] : []"
                      class="fgr-fi">
                      <grid-control :f="fieldDef(e.cell.field)" :style-obj="e.cell.style" :model-value="values[e.cell.field]"
                        :disabled="cellAttr(e.cell) === 'ro'"
                        @update:model-value="v => (values[e.cell.field] = v)" @browse="fd => $emit('browse', fd)" />
                    </el-form-item>
                  </template>
                  <!-- 详情页 / 预览：泛微式控件回显（空值显示占位提示，带控件图标与必填标记） -->
                  <template v-else>
                    <span class="val" :class="{ ph: displayValue(e.cell.field, e.cell.style) === null }">
                      <i v-if="cellAttr(e.cell) === 'req'" class="req" title="必填">*</i>
                      <i v-else-if="cellAttr(e.cell) === 'ro'" class="ro" title="只读">👁</i>
                      <span class="val-text">{{ displayValue(e.cell.field, e.cell.style) || placeholderOf(e.cell.field) }}</span>
                      <i v-if="iconOf(e.cell.field)" class="f-ico">{{ iconOf(e.cell.field) }}</i>
                    </span>
                  </template>
                </div>
                <div v-else-if="e.cell.type === 'detail'" class="fgr-detail">明细表：{{ e.cell.detail || '—' }}</div>
                <div v-else class="fgr-text">{{ e.cell.text || '' }}</div>
              </template>
            </td>
          </template>
          <td class="fgr-fill-cell"></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup name="WmFormGridRender">
/**
 * 网格运行态渲染：
 * - editable=true + values：发起页可编辑，field 单元格渲染真实控件（FormGridControl），
 *   必填校验通过 el-form-item（须置于 el-form 内），浏览按钮 @browse 上抛给页面打开选择弹窗。
 * - 否则：详情页/预览只读回显（select/radio 选项值→中文映射）。
 * 设计态与运行态共用 useGridMatrix 的 cellCss 样式规则，保证"所见即所得"。
 */
import { computed } from 'vue'
import GridControl, { displayTextOf } from '@/components/FormGridControl.js'
import { buildRowsView, cellCss } from '@/views/flowable/form/designer/useGridMatrix.js'

const props = defineProps({
  grid: { type: Object, default: null },
  fields: { type: Array, default: () => [] },
  values: { type: Object, default: null },
  editable: { type: Boolean, default: false }
})
defineEmits(['browse'])

const fieldMap = computed(() => {
  const m = {}
  for (const f of props.fields || []) m[f.field] = f
  return m
})
function fieldLabel(field) {
  const f = field && fieldMap.value[field]
  return f ? f.label : (field || '')
}
function fieldDef(field) {
  return (field && fieldMap.value[field]) || { field, rtype: 'text' }
}
function fieldReq(field) {
  const f = field && fieldMap.value[field]
  return !!(f && f.required)
}
/** 字段格展示属性：设计器右键设置优先，字段必填定义兜底 */
function cellAttr(cell) {
  if (cell && cell.attr) return cell.attr
  return fieldReq(cell && cell.field) ? 'req' : 'edit'
}
function iconOf(field) {
  const f = field && fieldMap.value[field]
  if (!f) return ''
  return { browse: '⌕', date: '▣', datetime: '▣', select: '▾', radio: '◉', attach: '📎', number: '123', check: '✓', switch: '◉' }[f.form] || ''
}
function placeholderOf(field) {
  const f = field && fieldMap.value[field]
  const tip = { select: '请选择', radio: '请选择', browse: '点击选择', date: '选择日期', datetime: '选择时间', attach: '上传附件' }[(f && f.form) || '']
  return tip || '请输入'
}
function displayValue(field, style) {
  const f = (field && fieldMap.value[field]) || { field }
  const v = props.values ? props.values[field] : undefined
  if (v === undefined || v === null || v === '') return null
  const fmt = style && style.fmt
  if (fmt && fmt.cat) {
    const d = fmt.decimals
    if (fmt.cat === 'percent') {
      const n = Number(v)
      if (!isNaN(n)) return (n * 100).toFixed(d == null ? 0 : d) + '%'
    } else if (fmt.cat === 'number') {
      const n = Number(v)
      if (!isNaN(n)) return n.toLocaleString('zh-CN', { minimumFractionDigits: d == null ? 2 : d, maximumFractionDigits: d == null ? 2 : d })
    }
  }
  return displayTextOf(f, v, '—')
}

const rowViews = computed(() => {
  if (!props.grid) return []
  return buildRowsView(props.grid)
})
</script>

<style lang="scss" scoped>
.fgr-wrap { min-width: max-content; }
.fgr-table { border-collapse: collapse; table-layout: fixed; }
.fgr-fill-col { width: auto; }
.fgr-fill-cell { background: #fbfcfe; }
.fgr-cell { border: 1px solid #dbe3ec; padding: 5px 8px; font-size: 13px; background: #fff; vertical-align: middle; min-width: 0; }
.fgr-cell.t-title { text-align: center; }
.fgr-title { font-weight: 600; }
.fgr-note { color: #e0342b; font-size: 12px; }
.fgr-field { display: flex; align-items: flex-start; gap: 8px; min-width: 0; }
.fgr-field .lbl { color: #475569; flex: none; max-width: 45%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-top: 5px; }
.fgr-field .req { color: #f56c6c; font-style: normal; margin-right: 2px; }
.fgr-field .val { flex: 1; color: #0f172a; border: 1px solid #dbe3ec; border-radius: 3px; background: #fbfcfe; min-height: 24px; word-break: break-all; display: inline-flex; align-items: center; gap: 6px; padding: 2px 8px; }
.fgr-field .val.ph .val-text { color: #b6c2d2; }
.fgr-field .val .req { color: #f56c6c; font-style: normal; font-weight: 700; flex: none; }
.fgr-field .val .ro { color: #94a3b8; font-style: normal; font-size: 11px; flex: none; }
.fgr-field .val .val-text { flex: 1; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.fgr-field .val .f-ico { font-style: normal; color: #94a3b8; font-size: 11px; flex: none; }
.fgr-fi { margin-bottom: 0 !important; flex: 1; min-width: 0; }
.fgr-fi :deep(.el-form-item__error) { position: static; padding-top: 2px; }
.fgr-detail { color: #64748b; font-size: 12px; }
.fgr-text { white-space: pre-wrap; }
</style>
