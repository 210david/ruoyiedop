<template>
  <div class="fgr-wrap">
    <table class="fgr-table" v-if="grid">
      <colgroup><col v-for="(w, i) in grid.colWidths" :key="i" :style="{ width: colWStyle(w) }" /><col class="fgr-fill-col" /></colgroup>
      <tbody>
        <tr v-for="(row, r) in rowViews" :key="r" :style="{ height: grid.rowHeights[r] + 'px' }">
          <template v-for="(e, ci) in row" :key="r + '-' + ci">
            <td v-if="e.kind !== 'skip'" :rowspan="e.rs || 1" :colspan="e.cs || 1"
              class="fgr-cell" :class="e.cell ? 't-' + e.cell.type : ''" :style="cellStyle(e.cell && e.cell.style)">
              <template v-if="e.cell">
                <div v-if="e.cell.type === 'title'" class="fgr-title">{{ e.cell.text || '' }}</div>
                <div v-else-if="e.cell.type === 'note'" class="fgr-note">{{ e.cell.text || '' }}</div>
                <div v-else-if="e.cell.type === 'field'" class="fgr-field">
                  <!-- 标签为独立文本格，字段格仅渲染控件/值 -->
                  <template v-if="editable && values">
                    <el-form-item :prop="e.cell.field"
                      :rules="fieldReq(e.cell.field) || cellAttr(e.cell) === 'req' ? [{ required: true, message: '请填写' + fieldLabel(e.cell.field), trigger: ['blur', 'change'] }] : []"
                      class="fgr-fi">
                      <i v-if="fieldReq(e.cell.field) || cellAttr(e.cell) === 'req'" class="req" title="必填">*</i>
                      <grid-control :f="fieldDef(e.cell.field)" :style-obj="e.cell.style" :model-value="values[e.cell.field]"
                        :disabled="cellAttr(e.cell) === 'ro'"
                        @update:model-value="v => (values[e.cell.field] = v)" @browse="fd => $emit('browse', fd)" />
                    </el-form-item>
                  </template>
                  <!-- 详情页 / 预览：泛微式控件回显（空值显示占位提示，带控件图标与必填标记） -->
                  <template v-else>
                    <span class="val" :class="[{ ph: displayValue(e.cell.field, e.cell.style) === null }, 'attr-' + cellAttr(e.cell)]">
                      <i v-if="cellAttr(e.cell) === 'req'" class="req" title="必填">*</i>
                      <span class="val-text">{{ displayValue(e.cell.field, e.cell.style) || placeholderOf(e.cell.field) }}</span>
                      <i v-if="iconOf(e.cell.field)" class="f-ico">{{ iconOf(e.cell.field) }}</i>
                    </span>
                  </template>
                </div>
                <div v-else-if="e.cell.type === 'detail'" class="fgr-detail-block">
                  <div class="fgr-dbar">
                    <span class="fgr-dname"><i class="dot"></i>{{ e.cell.text || '明细表' }}</span>
                    <span v-if="editable && values"><el-button size="small" link type="primary" @click="addDetailRow(e.cell.detail && e.cell.detail.table)">＋ 添加明细</el-button></span>
                  </div>
                  <table class="fgr-dtable" v-if="detailFieldsOf(e.cell.detail && e.cell.detail.table).length">
                    <thead><tr>
                      <th style="width:48px">序号</th>
                      <th v-for="f in detailFieldsOf(e.cell.detail && e.cell.detail.table)" :key="f.field">{{ f.label }}<i v-if="f.required" class="req">*</i></th>
                      <th v-if="editable && values" style="width:48px">操作</th>
                    </tr></thead>
                    <tbody>
                      <tr v-for="(row, ri) in (detailRows[(e.cell.detail && e.cell.detail.table)] || [])" :key="row._key">
                        <td style="text-align:center;color:#94a3b8">{{ ri + 1 }}</td>
                        <td v-for="f in detailFieldsOf(e.cell.detail && e.cell.detail.table)" :key="f.field">
                          <template v-if="editable && values">
                            <grid-control :f="fieldDef(f.field)" :model-value="row[f.field]"
                              @update:model-value="v => (row[f.field] = v)" @browse="fd => $emit('browse', fd)" />
                          </template>
                          <template v-else><span class="val-text">{{ displayTextOf(fieldDef(f.field), row[f.field], '—') }}</span></template>
                        </td>
                        <td v-if="editable && values" style="text-align:center">
                          <el-button size="small" link type="danger" @click="removeDetailRow(e.cell.detail && e.cell.detail.table, ri)">✕</el-button>
                        </td>
                      </tr>
                      <tr v-if="((e.cell.detail && e.cell.detail.sumFields) || []).length && (detailRows[(e.cell.detail && e.cell.detail.table)] || []).length" class="fgr-dsum">
                        <td style="text-align:center">合计</td>
                        <td v-for="f in detailFieldsOf(e.cell.detail && e.cell.detail.table)" :key="f.field" style="text-align:right;color:#7c3aed;font-weight:600">
                          <template v-if="(e.cell.detail.sumFields || []).includes(f.field)">Σ {{ detailSumValue(e.cell, f.field) }}</template>
                        </td>
                        <td v-if="editable && values"></td>
                      </tr>
                    </tbody>
                  </table>
                  <div v-else class="fgr-dempty">{{ e.cell.text || '明细表' }}暂无字段，请到「编辑字段」页签添加明细表字段</div>
                </div>
                <div v-else-if="e.cell.type === 'image'" class="fgr-img-wrap" :class="'img-' + ((e.cell.style && e.cell.style.align) || 'center')">
                  <img v-if="e.cell.src" :src="e.cell.src" class="fgr-img" alt="图片" />
                  <span v-else class="fgr-text ph">图片</span>
                </div>
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
import { buildRowsView, cellCss, colWStyle } from '@/views/flowable/form/designer/useGridMatrix.js'
// 渲染态不显示编辑网格默认线框，只显示用户绘制的边框
const cellStyle = st => cellCss(st, true)

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

// ===== 运行时明细表（参照泛微 e-cology）：行管理 + 表尾 Σ 合计 =====
import { ref, watch } from 'vue'
const detailRows = ref({})
/** 明细表字段清单（按 table 分组） */
function detailFieldsOf(dt) {
  return (props.fields || []).filter(f => f.table === dt)
}
function blankDetailRow(fs) {
  const row = { _key: 'r' + Math.random().toString(36).slice(2, 8) }
  fs.forEach(f => { row[f.field] = undefined })
  return row
}
/** 明细块变化时初始化默认 1 行（泛微"新增默认明细行数"） */
watch(() => props.grid, g => {
  const rows = {}
  for (const c of ((g && g.cells) || [])) {
    if (c.type === 'detail' && c.detail && c.detail.table) {
      const fs = detailFieldsOf(c.detail.table)
      rows[c.detail.table] = fs.length ? [blankDetailRow(fs)] : []
    }
  }
  detailRows.value = rows
}, { immediate: true, deep: false })
function addDetailRow(dt) {
  if (!dt) return
  if (!detailRows.value[dt]) detailRows.value[dt] = []
  detailRows.value[dt].push(blankDetailRow(detailFieldsOf(dt)))
}
function removeDetailRow(dt, idx) {
  detailRows.value[dt].splice(idx, 1)
  if (!detailRows.value[dt].length) addDetailRow(dt)
}
function detailSumValue(cell, field) {
  const rows = detailRows.value[cell.detail.table] || []
  return rows.reduce((s, r) => s + (Number(r[field]) || 0), 0)
}
/** 父组件可取走明细行数据（提交时随流程变量上抛） */
function getDetailData() {
  const out = {}
  for (const [dt, rows] of Object.entries(detailRows.value || {})) {
    if (rows.length) out[dt] = rows.map(({ _key, ...rest }) => rest)
  }
  return out
}
defineExpose({ getDetailData })
</script>

<style lang="scss" scoped>
.fgr-wrap { width: 100%; box-sizing: border-box; }
.fgr-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
.fgr-fill-col { width: auto; }
.fgr-fill-cell { background: #fbfcfe; }
.fgr-cell { padding: 5px 8px; font-size: 13px; background: #fff; vertical-align: middle; min-width: 0; }
.fgr-cell.t-title { text-align: center; }
/* 下划线/删除线：textDecoration 由 cellCss 设置在 td 上，块级/弹性子元素需显式继承才能渲染 */
.fgr-cell .fgr-title, .fgr-cell .fgr-note, .fgr-cell .fgr-text, .fgr-cell .val-text { text-decoration: inherit; }
.fgr-title { font-weight: 600; }
.fgr-note { color: #e0342b; font-size: 12px; }
.fgr-field { display: flex; align-items: flex-start; gap: 8px; min-width: 0; }
.fgr-field .lbl { color: #475569; flex: none; max-width: 45%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-top: 5px; }
.fgr-field .req { color: #f56c6c; font-style: normal; margin-right: 2px; }
.fgr-field .val { flex: 1; color: #0f172a; border: 1px solid #dbe3ec; border-radius: 3px; background: #fbfcfe; min-height: 24px; word-break: break-all; display: inline-flex; align-items: center; gap: 6px; padding: 2px 8px; }
.fgr-field .val.ph .val-text { color: #b6c2d2; }
.fgr-field .val .req { color: #f56c6c; font-style: normal; font-weight: 700; flex: none; }
/* 只读字段回显置灰：虚线灰边 + 灰底 + 灰字，与可编辑回显区分 */
.fgr-field .val.attr-ro { border: 1px dashed #c9d4de; background: #f4f6f8; }
.fgr-field .val.attr-ro .val-text { color: #9aa8b5; }
.fgr-field .val.attr-ro .f-ico { color: #b9c4cf; }
/* 必填字段回显：浅红提示边呼应红星 */
.fgr-field .val.attr-req { border-color: #f3c1c1; }
.fgr-field .val .val-text { flex: 1; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.fgr-field .val .f-ico { font-style: normal; color: #94a3b8; font-size: 11px; flex: none; }
.fgr-fi { margin-bottom: 0 !important; flex: 1; min-width: 0; display: flex; align-items: flex-start; }
/* 编辑态必填标识：控件前红色星号（与只读回显 .val 内红星同口径） */
.fgr-fi .req { color: #f56c6c; font-style: normal; margin-right: 2px; line-height: 26px; flex: none; }
.fgr-fi :deep(.el-form-item__error) { position: static; padding-top: 2px; }
.fgr-detail { color: #64748b; font-size: 12px; }
/* ===== 运行时明细表块（参照泛微 e-cology） ===== */
.fgr-detail-block { border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden; width: 100%; }
.fgr-dbar { display: flex; align-items: center; justify-content: space-between; background: #2F3C8E; color: #fff; padding: 5px 10px; }
.fgr-dname { display: inline-flex; align-items: center; gap: 5px; font-size: 13px; font-weight: 600; }
.fgr-dname .dot { width: 6px; height: 6px; border-radius: 50%; background: #fbbf24; }
.fgr-dtable { width: 100%; border-collapse: collapse; font-size: 13px; }
.fgr-dtable th { background: #fef3c7; color: #92400e; padding: 6px 8px; border: 1px solid #fde68a; font-weight: 600; text-align: center; white-space: nowrap; }
.fgr-dtable th .req { color: #f56c6c; font-style: normal; margin-left: 2px; }
.fgr-dtable td { border: 1px solid #eef2f7; padding: 4px 6px; }
.fgr-dsum td { background: #f8fafc; }
.fgr-dempty { padding: 16px; text-align: center; color: #94a3b8; font-size: 13px; }
.fgr-text { white-space: pre-wrap; }
/* 图片格：图片铺满格子、object-fit contain 等比缩放，行高列宽拖动完全不受图片限制；对齐用 object-position */
.fgr-img-wrap { position: relative; display: block; width: 100%; height: 100%; min-height: 24px; min-width: 0; overflow: hidden; }
.fgr-img { position: absolute; inset: 0; width: 100%; height: 100%; min-width: 0; min-height: 0; max-width: none; max-height: none; object-fit: contain; object-position: center; display: block; }
.fgr-img-wrap.img-left .fgr-img { object-position: left center; }
.fgr-img-wrap.img-right .fgr-img { object-position: right center; }
</style>
