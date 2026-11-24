<template>
  <div class="fgr-wrap" ref="rootRef">
    <div class="fgr-cards" v-if="gridView" :class="{ 'fgr-cards-mode': hasGroupCell }">
      <section v-for="b in cardBlocks" :key="b.key" class="fgr-card" :class="{ 'is-plain': !b.title }">
        <div v-if="b.title" class="fgr-card-head"><i class="fgr-card-icon"></i><span>{{ b.title }}</span></div>
        <table class="fgr-table">
          <colgroup>
            <col v-for="(w, i) in gridView.colWidths" :key="i" :style="{ width: colWStyle(w) }" />
            <col class="fgr-fill-col" />
          </colgroup>
          <tbody>
            <tr v-for="item in b.rows" :key="item.r" :style="{ height: (gridView.rowHeights[item.r] || 30) + 'px' }">
              <template v-for="(e, ci) in item.cells" :key="item.r + '-' + ci">
            <td v-if="e.kind !== 'skip'" :rowspan="e.rs || 1" :colspan="e.cs || 1" class="fgr-cell" :class="[e.cell ? 't-' + e.cell.type : '', { 't-label': isLabelCell(e), 'is-ov-hidden': e.cell && e.cell.type === 'field' && ovHidden(e.cell.field) }]" :style="cellStyle(e.cell && e.cell.style)">
              <template v-if="e.cell">
                <div v-if="e.cell.type === 'title'" class="fgr-title">{{ e.cell.text || '' }}</div>
                <div v-else-if="e.cell.type === 'note'" class="fgr-note">{{ e.cell.text || '' }}</div>
                <div v-else-if="e.cell.type === 'field'" class="fgr-field">
                  <!-- 标签为独立文本格，字段格仅渲染控件/值 -->
                  <template v-if="editable && values">
                    <!-- 设计器把该列配为「只读」：按只读口径渲染；
                         仅只读字段的附件走禁用态卡片（可预览/下载），可编辑附件仍走上传控件 -->
                    <grid-control
                      v-if="cellAttr(e.cell) === 'ro' && (isAttach(e.cell.field) || isAttachValue(values?.[e.cell.field])) && hasValue(e.cell.field)"
                      :f="attachDefOf(e.cell.field)"
                      :model-value="values?.[e.cell.field]"
                      disabled
                    />
                    <span v-else-if="cellAttr(e.cell) === 'ro'" class="val attr-ro">
                      <span class="val-text">{{ displayValue(e.cell.field, e.cell.style) || '' }}</span>
                    </span>
                    <el-form-item
                      v-else
                      :prop="e.cell.field"
                      label-width="0"
                      :rules="fieldItemRules(e.cell)"
                      :error="props.ruleErrors[e.cell.field] || ''"
                      class="fgr-fi"
                    >
                      <grid-control
                        :f="(isAttach(e.cell.field) || isAttachValue(values?.[e.cell.field])) ? attachDefOf(e.cell.field) : fieldDef(e.cell.field)"
                        :style-obj="e.cell.style"
                        :model-value="values[e.cell.field]"
                        :id-value="values[e.cell.field + '__id']"
                        :extra-value="values[e.cell.field + '__extra']"
                        :required="showReqStar(e.cell)"
                        :options-override="(props.overrides[e.cell.field] || {}).options"
                        @update:model-value="(v) => (values[e.cell.field] = v)"
                        @update:id-value="(v) => (values[e.cell.field + '__id'] = v)"
                        @update:extra-value="(v) => (values[e.cell.field + '__extra'] = v)"
                      />
                      <!-- 附件控件的星号由 FileUpload 内部紧跟在“选取文件”按钮后渲染，这里不再重复 -->
                      <i v-if="showReqStar(e.cell) && !isAttach(e.cell.field)" class="req" title="必填">*</i>
                    </el-form-item>
                  </template>
                  <!-- 详情页 / 预览：只读回显——纯文本，不画输入框边框/底色，也不挂控件图标 -->
                  <template v-else>
                    <!-- 附件字段：沿用标准附件控件（禁用态），可预览、可下载，不能露出 /profile 路径 -->
                    <grid-control
                      v-if="(isAttach(e.cell.field) || isAttachValue(values?.[e.cell.field])) && hasValue(e.cell.field)"
                      :f="attachDefOf(e.cell.field)"
                      :model-value="values?.[e.cell.field]"
                      disabled
                    />
                    <span v-else class="val" :class="[{ ph: displayValue(e.cell.field, e.cell.style) === null }, 'attr-' + cellAttr(e.cell)]">
                      <span class="val-text">{{ displayValue(e.cell.field, e.cell.style) || '' }}</span>
                    </span>
                    <!-- 只读回显不带必填星号：必填是填写期的约束，查看态显示星号只会让人误以为还要填 -->
                  </template>
                </div>
                <div v-else-if="e.cell.type === 'detail'" class="fgr-detail-block">
                  <!-- 分组卡片模式下卡片标题已标明明细名，只读态没有「添加明细」按钮，
                       空标题栏会渲染成一条悬空的灰线，因此仅在确实有内容时才渲染 -->
                  <div class="fgr-dbar" v-if="!hasGroupCell || (editable && values && (!e.cell.detail.props || e.cell.detail.props.allowAdd))">
                    <span class="fgr-dname"><i class="dot"></i>{{ e.cell.text || '明细表' }}</span>
                    <span v-if="editable && values && (!e.cell.detail.props || e.cell.detail.props.allowAdd)"
                      ><el-button size="small" link type="primary" @click="addDetailRow(e.cell.detail && e.cell.detail.table)">＋ 添加明细</el-button></span
                    >
                  </div>
                  <table class="fgr-dtable" v-if="((e.cell.detail && e.cell.detail.columns) || []).length">
                    <colgroup>
                      <col style="width: 48px" />
                      <col v-for="col in e.cell.detail.columns || []" :key="col.field" :style="{ width: detailColWidth(col) }" />
                      <col v-if="showDetailOpCol(e)" style="width: 48px" />
                    </colgroup>
                    <thead>
                      <tr>
                        <th style="width: 48px">序号</th>
                        <th v-for="col in e.cell.detail.columns || []" :key="col.field" :style="{ textAlign: col.align || 'center' }">{{ col.label || col.field }}<i v-if="editable && values && showColReqStar(col, e.cell.detail && e.cell.detail.table)" class="req">*</i></th>
                        <th v-if="showDetailOpCol(e)" style="width: 48px">操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, ri) in detailRows[e.cell.detail && e.cell.detail.table] || []" :key="row._key">
                        <td style="text-align: center; color: #94a3b8">{{ ri + 1 }}</td>
                        <td v-for="col in e.cell.detail.columns || []" :key="col.field" :style="{ textAlign: col.align || 'center' }" :class="{ 'fgr-dcell-error': isCellError(e.cell.detail && e.cell.detail.table, ri, col.field), 'is-ov-hidden': ovHidden(col.field) }" :title="detailRuleErrText(e.cell.detail && e.cell.detail.table, ri, col.field)">
                          <template v-if="editable && values">
                            <!-- 明细列设为「只读」：只读附件走禁用态卡片（可预览/下载），其余按只读口径渲染纯文本 -->
                            <grid-control
                              v-if="colAttr(col) === 'ro' && (isAttach(col.field) || isAttachValue(row[col.field])) && row[col.field]"
                              :f="attachDefOf(col.field, e.cell.detail && e.cell.detail.table)"
                              :model-value="row[col.field]"
                              disabled
                            />
                            <span v-else-if="colAttr(col) === 'ro'" class="val-text fgr-dro">{{ displayTextOf(fieldDef(col.field), row[col.field], '—') }}</span>
                            <grid-control
                              v-else
                              :f="(isAttach(col.field) || isAttachValue(row[col.field])) ? attachDefOf(col.field, e.cell.detail && e.cell.detail.table) : fieldDef(col.field)"
                              :model-value="row[col.field]"
                              :id-value="row[col.field + '__id']"
                              :extra-value="row[col.field + '__extra']"
                              :options-override="(props.overrides[col.field] || {}).options"
                              @update:model-value="(v) => { row[col.field] = v; emit('detail-change', e.cell.detail && e.cell.detail.table, col.field); }"
                              @update:id-value="(v) => (row[col.field + '__id'] = v)"
                              @update:extra-value="(v) => (row[col.field + '__extra'] = v)"
                            />
                          </template>
                          <template v-else>
                            <grid-control
                              v-if="(isAttach(col.field) || isAttachValue(row[col.field])) && row[col.field]"
                              :f="attachDefOf(col.field, e.cell.detail && e.cell.detail.table)"
                              :model-value="row[col.field]"
                              disabled
                            />
                            <span v-else class="val-text">{{ displayTextOf(fieldDef(col.field), row[col.field], '—') }}</span>
                          </template>
                        </td>
                        <td v-if="showDetailOpCol(e)" style="text-align: center">
                          <el-button
                            size="small"
                            link
                            type="danger"
                            :disabled="!canDeleteRow(e.cell.detail && e.cell.detail.table)"
                            :title="canDeleteRow(e.cell.detail && e.cell.detail.table) ? '删除该行' : '该明细表设置了「必须填写明细」，至少需保留一行'"
                            @click="removeDetailRow(e.cell.detail && e.cell.detail.table, ri)"
                            >✕</el-button
                          >
                        </td>
                      </tr>
                      <!-- 明细为空：给出明确占位，避免"删完一行又冒出一行"的错觉 -->
                      <tr v-if="!(detailRows[e.cell.detail && e.cell.detail.table] || []).length" class="fgr-dempty-row">
                        <td :colspan="detailColSpan(e)">
                          {{ mustAddOf(e.cell) ? '该明细表为必填，暂无数据，请点击「＋ 添加明细」新增一行' : '暂无明细数据，无需填写可直接提交；如需填写请点击「＋ 添加明细」' }}
                        </td>
                      </tr>
                      <tr v-if="(e.cell.detail.columns || []).some((c) => c.sum) && (detailRows[e.cell.detail && e.cell.detail.table] || []).length" class="fgr-dsum">
                        <td style="text-align: center">合计</td>
                        <td v-for="col in e.cell.detail.columns || []" :key="col.field" :style="{ textAlign: col.align || 'right', color: '#7c3aed', fontWeight: 600 }">
                          <template v-if="col.sum">Σ {{ detailSumValue(e.cell, col.field) }}</template>
                        </td>
                        <td v-if="showDetailOpCol(e)"></td>
                      </tr>
                    </tbody>
                  </table>
                  <div v-else class="fgr-dempty">{{ e.cell.text || '明细表' }}暂无字段，请到「编辑字段」页签添加明细表字段</div>
                </div>
                <div v-else-if="e.cell.type === 'image'" class="fgr-img-wrap" :class="'img-' + ((e.cell.style && e.cell.style.align) || 'center')">
                  <img v-if="e.cell.src" :src="e.cell.src" class="fgr-img" alt="图片" />
                  <span v-else class="fgr-text ph">图片</span>
                </div>
                <div v-else class="fgr-text">{{ labelTextOf(e.cell) }}</div>
              </template>
            </td>
              </template>
              <td class="fgr-fill-cell"></td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </div>
</template>

<script setup name="WmFormGridRender">
/**
 * 网格运行态渲染：
 * - editable=true + values：发起页可编辑，field 单元格渲染真实控件（FormGridControl），
 * 必填校验通过 el-form-item（须置于 el-form 内）；浏览按钮由 FormBrowse 自带弹框选择器。
 * - 否则：详情页/预览只读回显（select/radio 选项值→中文映射）。
 * 设计态与运行态共用 useGridMatrix 的 cellCss 样式规则，保证"所见即所得"。
 */
import { computed } from 'vue';
import { ElMessage } from 'element-plus';
import GridControl, { displayTextOf, rtypeOf } from '@/components/FormGridControl.js';
import { isSysReadonlyField } from '@/utils/flow-system-fields.js';
import { applyFieldDefaults, currentUserCtx, syncPersonDerived } from '@/views/flowable/common/field-defaults';
import { buildRowsView, cellCss, colWStyle } from '@/views/flowable/form/designer/useGridMatrix.js';
import { toRmbUpper } from '@/utils/rmb';
// 渲染态不显示编辑网格默认线框，只显示用户绘制的边框
const cellStyle = (st) => cellCss(st, true);
/** 标签格显示文字：labelField 绑定且字段存在时实时取字段最新标签（字段改名后运行态同步），否则用格内文本 */
function labelTextOf(cell) {
  if (cell && cell.type === 'text' && cell.labelField) {
    const f = (props.fields || []).find((x) => x && x.field === cell.labelField);
    if (f && f.label) return f.label;
  }
  return (cell && cell.text) || '';
}
/** 标签格：设计器「插入标签」创建的文本格（labelField 标记），运行态沿用设计器的默认浅蓝底 */
function isLabelCell(e) {
  const c = e && e.cell;
  return !!(c && c.type === 'text' && c.labelField && labelTextOf(c));
}

const props = defineProps({
  grid: { type: Object, default: null },
  fields: { type: Array, default: () => [] },
  values: { type: Object, default: null },
  editable: { type: Boolean, default: false },
  // 强制可编辑的字段（如编号规则开了「允许手工修改」时的 processNo）
  editableFields: { type: Array, default: () => [] },
  // 规则引擎状态覆盖：{ [field]: { required, visible, readonly } }
  overrides: { type: Object, default: () => ({}) },
  // 规则引擎实时校验错误：{ [field]: 提示文案 }（主表字段格内联展示，随字段变化实时刷新）
  ruleErrors: { type: Object, default: () => ({}) },
  // 规则引擎明细行校验错误：{ '表名|行号|字段': 提示文案 }（明细单元格红框 + 悬浮提示）
  ruleDetailErrors: { type: Object, default: () => ({}) },
});

/** 明细数据变化事件：单元格编辑 / 行增删时通知父组件（规则引擎据此重算联动） */
const emit = defineEmits(['detail-change']);

/** 规则引擎显隐覆盖：true = 隐藏（占位不渲染内容，保持表格矩阵不塌） */
function ovHidden(field) {
  const ov = props.overrides && props.overrides[field];
  return !!(ov && ov.visible === false);
}

const fieldMap = computed(() => {
  const m = {};
  for (const f of props.fields || []) m[f.field] = f;
  return m;
});
function fieldLabel(field) {
  const f = field && fieldMap.value[field];
  return f ? f.label : field || '';
}
function fieldDef(field) {
  return (field && fieldMap.value[field]) || { field, rtype: 'text' };
}
function fieldReq(field) {
  const ov = props.overrides && props.overrides[field];
  if (ov && ov.required) return true;
  const f = field && fieldMap.value[field];
  return !!(f && f.required);
}
/** 是否附件类字段（星号需紧贴按钮，由上传组件内部渲染） */
function isAttach(field) {
  const f = field && fieldMap.value[field];
  return !!f && rtypeOf(f) === 'attach';
}
/** 附件兜底渲染字段定义：历史表单 rtype 丢失时也强制走附件控件，保证只读态可预览/下载 */
function attachDefOf(field, table) {
  const f = table ? fieldDefOfTable(field, table) : fieldMap.value[field];
  return f ? { ...f, rtype: 'attach' } : { field, rtype: 'attach' };
}
/** 附件值兜底识别：历史表单字段定义可能丢失 rtype（渲染成裸 /profile 路径文本），
 *  只要值本身是 /profile/ 文件路径（逗号分隔）就按附件回显，保证只读态也能预览/下载 */
function isAttachValue(v) {
  if (!v) return false;
  const s = Array.isArray(v) ? v.join(',') : String(v).trim();
  return /(^|,)\s*\/?profile\//i.test(s);
}
/** 字段格展示属性：显式放开的字段（允许改号的流程编号）优先，其次字段自身只读，再次设计器右键设置，最后字段必填定义兜底 */function cellAttr(cell) {
  const field = cell && cell.field;
  if (field && props.editableFields.includes(field)) return cell && cell.attr === 'req' ? 'req' : 'edit';
  const f = field && fieldMap.value[field];
  // 规则引擎状态覆盖：显式解除只读 / 只读 / 必填，优先级高于设计器设置
  // （默认只读字段选「是」冲抵后由 readonly 规则解除只读并出必填星号）
  const ov = props.overrides && props.overrides[field];
  if (ov && ov.readonly === false) return fieldReq(field) ? 'req' : 'edit';
  if (ov && ov.readonly) return 'ro';
  if (ov && ov.required) return 'req';
  if (f && (f.readonly || isSysReadonlyField(f.field))) return 'ro';
  if (cell && cell.attr) return cell.attr;
  return fieldReq(cell && cell.field) ? 'req' : 'edit';
}
/** 附件类字段是否已有值（只读态决定渲染附件控件还是空值占位） */
function hasValue(field) {
  const v = props.values ? props.values[field] : undefined;
  return !!(v && String(v).trim());
}
/**
 * 必填星号是否显示（泛微口径）：有值就不显示，清空后又显示。
 * 星号是「还没填」的提示，填完还给用户一个红点只会让人觉得还没填对；
 * 一旦清空（含下拉清空、日期清空、附件删空）就恢复显示，保证必填约束仍然可见。
 */
function showReqStar(cell) {
  if (!cell) return false;
  const attr = cellAttr(cell);
  if (attr === 'ro') return false;
  // 与 fieldItemRules 校验口径一致：字段定义必填 / 规则引擎覆盖必填 / 设计器单元格必填，任一成立即显示星号。
  // 修复：设计器单元格 attr 配成非 req（如 edit）但字段定义 required=true 时，校验会拦截提交而星号被遮蔽不显示
  if (!fieldReq(cell.field) && attr !== 'req') return false;
  return isFieldUnfilled(cell.field);
}
/** 字段是否「还没填」：勾选框/开关取消勾选后值是 '0'，语义上仍是未填，星号要回来 */
function isFieldUnfilled(field) {
  const v = props.values ? props.values[field] : undefined;
  if (isBlankValue(v)) return true;
  const rt = rtypeOf(fieldDef(field));
  if ((rt === 'check' || rt === 'switch') && String(v) === '0') return true;
  return false;
}
/**
 * 明细列必填星号是否显示（泛微口径）：明细还没有行，或任一行该列为空 → 显示；
 * 所有行该列都已填 → 隐藏，清空某行又恢复显示。
 */
function showColReqStar(col, table) {
  if (!col || colAttr(col) !== 'req') return false;
  const rows = detailRows.value[table] || [];
  if (!rows.length) return true;
  // 勾选/开关列值为 '0' 时同样视为未填，与主表口径一致
  const rt = rtypeOf(fieldDefOfTable(col.field, table));
  return rows.some((r) => {
    const v = r[col.field];
    if (isBlankValue(v)) return true;
    return (rt === 'check' || rt === 'switch') && String(v) === '0';
  });
}
function displayValue(field, style) {
  const f = (field && fieldMap.value[field]) || { field };
  const v = props.values ? props.values[field] : undefined;
  if (v === undefined || v === null || v === '') return null;
  const fmt = style && style.fmt;
  if (fmt && fmt.cat) {
    const d = fmt.decimals;
    if (fmt.cat === 'percent') {
      const n = Number(v);
      if (!isNaN(n)) return (n * 100).toFixed(d == null ? 0 : d) + '%';
    } else if (fmt.cat === 'number') {
      const n = Number(v);
      if (!isNaN(n)) return n.toLocaleString('zh-CN', { minimumFractionDigits: d == null ? 2 : d, maximumFractionDigits: d == null ? 2 : d });
    } else if (fmt.cat === 'rmb') {
      // 人民币大写：详情/预览/打印（FlowPrintView 同走本渲染层）统一回显
      return toRmbUpper(v) || null;
    }
  }
  return displayTextOf(f, v, '—');
}

/**
 * 只渲染布局里实际放置的单元格。
 * 系统字段（标题/流程编号/紧急程度/签字意见/打印次数/流程状态）不再自动补到空白格：
 * 管理员在页面设计里主动放上去才会显示，避免用户没添加也被默认带出。
 */
const gridView = computed(() => props.grid);

/**
 * 运行态空行过滤：整行都是「无内容」的行（空占位格/空文本格）直接不渲染。
 * 设计器里拖动产生的空行在运行态只会留出一段空白（如标题上方的大空隙），
 * 渲染层去掉即可，设计态矩阵不受影响；
 * 被合并格纵向覆盖的行不能删（rowspan 按 tr 计数，删了会错位）。
 */
function isBlankLine(line) {
  if (!line || !line.length) return true;
  return line.every((e) => !e || e.kind === 'empty' || !e.cell || (e.cell.type === 'text' && !e.cell.text));
}
function rowCoveredByMerge(grid, r) {
  return (grid.cells || []).some((c) => c.r < r && r < c.r + (c.rs || 1));
}

const rowViews = computed(() => {
  if (!gridView.value) return [];
  const out = [];
  buildRowsView(gridView.value).forEach((line) => {
    if (!line.length) return;
    // 行条目自带原始行号（隐藏行会被 buildRowsView 跳过，不能按下标当行号）
    const r = line[0].r;
    // 空行且未被合并格覆盖：运行态直接不渲染（保留原始行号，行高取值不错位）
    if (isBlankLine(line) && !rowCoveredByMerge(gridView.value, r)) return;
    out.push({ r, cells: line });
  });
  return out;
});

/**
 * 卡片分组：布局中出现 type=group 的单元格时，以该行为界把表格切成多个卡片。
 * - group 行本身不进入表格，作为卡片标题（与业务模块弹框表单的分组卡片一致）
 * - 没有 group 单元格时只有一个 is-plain 卡片，保持原有网格外观不变
 */
const hasGroupCell = computed(() => !!gridView.value && (gridView.value.cells || []).some((c) => c && (c.group || c.type === 'group')));
const cardBlocks = computed(() => {
  const rows = rowViews.value || [];
  const blocks = [];
  let cur = { key: 'b0', title: '', rows: [] };
  rows.forEach((row, idx) => {
    const cells = row.cells;
    const g = (cells || []).find((e) => e && e.cell && (e.cell.group || e.cell.type === 'group'));
    if (g) {
      if (cur.rows.length) blocks.push(cur);
      cur = { key: 'b' + idx, title: g.cell.text || '', rows: [] };
      return;
    }
    cur.rows.push(row);
  });
  if (cur.rows.length) blocks.push(cur);
  return blocks;
});

// ===== 运行时明细表（表单运行态）：行管理 + 表尾 Σ 合计 =====
import { ref, watch, nextTick } from 'vue';
const detailRows = ref({});
/** 当前用户上下文：明细列默认值（当前用户 / 当前部门）取用 */
const userCtx = ref({});
/** 某张表的字段（明细列取定义时按表名过滤，避免与主表同名字段混用） */
function fieldDefOfTable(field, table) {
  const same = (props.fields || []).filter((x) => x && x.table === table);
  if (same.length) return same.find((x) => x.field === field) || null;
  // 老表单的明细字段可能没打表名，此时退回按名字找
  return (props.fields || []).find((x) => x && x.field === field) || null;
}
/** 某明细表对应的字段定义（默认值配置在字段定义上，列定义只存展示属性） */
function detailDefsOf(dt) {
  const out = [];
  for (const c of (props.grid && props.grid.cells) || []) {
    if (c.type !== 'detail' || !c.detail || c.detail.table !== dt) continue;
    for (const col of c.detail.columns || []) {
      const f = fieldDefOfTable(col.field, dt);
      if (f) out.push(f);
    }
  }
  return out;
}
/** 人员→部门/岗位联动（明细行）：只盯配置了「随人员带出部门/岗位」的源字段，避免每次输入都计算 */
const dLinkKey = computed(() => {
  const parts = [];
  for (const dt of Object.keys(detailRows.value || {})) {
    const defs = detailDefsOf(dt);
    for (const f of defs) {
      if (!f || !['fieldUserDept', 'fieldUserPost'].includes(f.defaultMode) || !f.defaultField) continue;
      const s = f.defaultField;
      for (const row of detailRows.value[dt] || []) {
        const ex = row[s + '__extra'];
        parts.push(`${dt}.${f.field}=${row[s]}|${row[s + '__id']}|${ex ? ex.deptName : ''}|${ex ? ex.postName : ''}`);
      }
    }
  }
  return parts.join('~');
});
watch(dLinkKey, (v) => {
  if (!v) return;
  for (const dt of Object.keys(detailRows.value || {})) {
    const defs = detailDefsOf(dt);
    if (!defs.some((f) => f && ['fieldUserDept', 'fieldUserPost'].includes(f.defaultMode))) continue;
    for (const row of detailRows.value[dt] || []) syncPersonDerived(defs, row, userCtx.value);
  }
});
// 当前用户信息是异步取的：拿到后再补一次明细默认值（首次同步填充时还拿不到当前用户/部门）
currentUserCtx().then((c) => {
  userCtx.value = c;
  for (const dt of Object.keys(detailRows.value || {})) {
    const defs = detailDefsOf(dt);
    if (!defs.length) continue;
    for (const row of detailRows.value[dt] || []) {
      applyFieldDefaults(defs, row, c);
      syncPersonDerived(defs, row, c);
    }
  }
});
/** 明细必填未填的单元格（key = 表名|行号|字段），提交校验时置位，任意编辑后清空 */
const detailErrors = ref({});
const rootRef = ref(null);
watch(
  detailRows,
  () => {
    if (Object.keys(detailErrors.value).length) detailErrors.value = {};
  },
  { deep: true },
);
/** 明细列宽：设计器拖拽/设置的百分比优先，未设置自动均分 */
function detailColWidth(col) {
  return col && col.width ? col.width + '%' : 'auto';
}
/** 明细列展示属性：编辑/只读/必填（规则引擎覆盖优先，支持显式解除只读，设计器列配置次之） */
function colAttr(col) {
  // 规则引擎覆盖：明细列显式解除只读 / 只读 / 必填
  const ov = props.overrides && props.overrides[col && col.field];
  if (ov && ov.readonly === false) return ov.required ? 'req' : 'edit';
  if (ov && ov.readonly) return 'ro';
  if (ov && ov.required) return 'req';
  return (col && col.attr) || 'edit';
}
/** 明细表「操作」列是否渲染：仅在可编辑且允许删除行时显示，
    没启用删除功能时操作列没有实际动作，不渲染避免一列空壳 */
function showDetailOpCol(e) {
  if (!props.editable || !props.values) return false;
  const p = (e && e.cell && e.cell.detail && e.cell.detail.props) || {};
  return p.allowDelete !== false;
}
/** 明细列字段清单（columns 自包含，含 field/label/width/sum/required） */
function blankDetailRow(cols, table) {
  const row = { _key: 'r' + Math.random().toString(36).slice(2, 8) };
  cols.forEach((c) => {
    row[c.field] = undefined;
  });
  // 新增行同样带出字段默认值（固定值 / 当前用户 / 当天日期），已有值不覆盖
  const defs = cols.map((c) => fieldDefOfTable(c.field, table)).filter(Boolean);
  applyFieldDefaults(defs, row, userCtx.value);
  syncPersonDerived(defs, row, userCtx.value);
  return row;
}
/** 明细块变化时初始化默认 1 行（"新增默认明细行数"）；只读回显不预置空行 */
watch(
  () => props.grid,
  (g) => {
    const rows = {};
    for (const c of (g && g.cells) || []) {
      if (c.type === 'detail' && c.detail && c.detail.table) {
        const cols = c.detail.columns || [];
        // 查看态（详情页/办理页只读字段）没有明细就该是空的，
        // 预置一行空白会渲染成 "1 — —"，看起来像有一条空记录
        if (!props.editable || !props.values) {
          rows[c.detail.table] = [];
          continue;
        }
        // 默认行数可为 0（设计器允许"从空开始"）；「必须填写明细」时至少预置 1 行
        const n = Number(c.detail.props && c.detail.props.defaultRows);
        const base = Number.isFinite(n) ? n : 1;
        const count = Math.min(20, Math.max(c.detail.props && c.detail.props.mustAdd ? 1 : 0, base));
        rows[c.detail.table] = cols.length ? Array.from({ length: count }, () => blankDetailRow(cols, c.detail.table)) : [];
      }
    }
    detailRows.value = rows;
  },
  { immediate: true, deep: false },
);
/**
 * 明细行回填：流程变量里的 detailData（形状 {表名: [行对象]}）到达时覆盖默认空白行。
 * 详情页/办理页的 values 就是后端返回的 variables，所以只读回显也走这里；
 * 发起页草稿恢复同样是 values.detailData 换引用后自动生效。
 */
watch(
  () => props.values && props.values.detailData,
  (d) => {
    if (d && typeof d === 'object' && Object.keys(d).length) setDetailData(d);
  },
  { immediate: true, deep: false },
);
/** 明细块属性：布局里可能完全没有 props（老表单/Markdown 直连），按默认值兜底 */
function detailBlockOf(dt) {
  return ((props.grid && props.grid.cells) || []).find((c) => c.type === 'detail' && c.detail && c.detail.table === dt) || null;
}
function detailPropsOf(dt) {
  const block = detailBlockOf(dt);
  const p = (block && block.detail && block.detail.props) || {};
  return { allowAdd: p.allowAdd !== false, allowDelete: p.allowDelete !== false, mustAdd: !!p.mustAdd, defaultRows: p.defaultRows };
}
/** 「必须填写明细」= 至少保留一行，此时最后一行不可删 */
function mustAddOf(cell) {
  return !!(cell && cell.detail && cell.detail.props && cell.detail.props.mustAdd);
}
function canDeleteRow(dt) {
  const p = detailPropsOf(dt);
  if (!p.allowDelete) return false;
  const rows = detailRows.value[dt] || [];
  return !(p.mustAdd && rows.length <= 1);
}
/** 空表占位行的跨列数：序号 + 数据列 + 操作列（操作列仅在可编辑且允许删除时渲染） */
function detailColSpan(e) {
  const cols = (e && e.cell && e.cell.detail && e.cell.detail.columns) || [];
  return 1 + cols.length + (showDetailOpCol(e) ? 1 : 0);
}
function addDetailRow(dt) {
  if (!dt) return;
  if (!detailPropsOf(dt).allowAdd) {
    ElMessage.warning('该明细表不允许新增行');
    return;
  }
  const block = detailBlockOf(dt);
  if (!detailRows.value[dt]) detailRows.value[dt] = [];
  detailRows.value[dt].push(blankDetailRow((block && block.detail.columns) || [], dt));
  emit('detail-change', dt);
}
/** 删除明细行：mustAdd=false 允许删到 0 行（明细可为空）；mustAdd=true 保留最后一行 */
function removeDetailRow(dt, idx) {
  const rows = detailRows.value[dt] || [];
  if (idx < 0 || idx >= rows.length) return;
  if (detailPropsOf(dt).mustAdd && rows.length <= 1) {
    ElMessage.warning('该明细表设置了「必须填写明细」，至少需保留一行');
    return;
  }
  rows.splice(idx, 1);
  emit('detail-change', dt);
}
function detailSumValue(cell, field) {
  const rows = detailRows.value[cell.detail.table] || [];
  return rows.reduce((s, r) => s + (Number(r[field]) || 0), 0);
}
/** 父组件回填明细行（草稿恢复 / 回显复用） */
function setDetailData(data) {
  if (!data || typeof data !== 'object') return;
  const rows = { ...(detailRows.value || {}) };
  for (const [dt, list] of Object.entries(data)) {
    rows[dt] = (list || []).map((r) => ({ _key: 'r' + Math.random().toString(36).slice(2, 8), ...r }));
  }
  detailRows.value = rows;
}
/** 父组件可取走明细行数据（提交时随流程变量上抛） */
function getDetailData() {
  const out = {};
  for (const [dt, rows] of Object.entries(detailRows.value || {})) {
    // 剔除"整行没填"的空行：明细块默认预置一行空白，用户不填时不该把它当业务数据提交
    const filled = rows
      .map(({ _key, ...rest }) => {
        // 浏览按钮的伴生 ID（field__id）只用于弹框回显，不随明细数据提交
        const clean = {};
        Object.keys(rest).forEach((k) => {
          if (!k.startsWith('__')) clean[k] = rest[k];
        });
        return clean;
      })
      .filter((r) => Object.values(r).some((v) => v !== undefined && v !== null && String(v).trim() !== ''));
    if (filled.length) out[dt] = filled;
  }
  return out;
}
/**
 * 明细属性校验：
 * - 「必须填写明细」(mustAdd) → 整表至少要有一行有数据，否则拦截；
 * - 列上的「必填」(req) 只约束"已经存在的行"，空表不因必填列被拦（空表一律放行）。
 * 返回错误文案，并把未填单元格标红。
 */
function validateDetails() {
  const msgs = [];
  const marks = {};
  for (const c of (props.grid && props.grid.cells) || []) {
    if (c.type !== 'detail' || !c.detail || !c.detail.table) continue;
    const table = c.detail.table;
    const cols = c.detail.columns || [];
    const title = c.detail.title || c.text || '明细表';
    const rows = detailRows.value[table] || [];
    const mustAdd = !!(c.detail.props && c.detail.props.mustAdd);
    const reqCols = cols.filter((col) => colAttr(col) === 'req');
    // 只把「有数据」的行当成待提交行（与 getDetailData 剔除空行的口径一致），避免用户多加的空行误报
    const dataRows = rows.map((row, ri) => ({ row, ri })).filter(({ row }) => rowHasValue(row, cols));
    if (!dataRows.length) {
      // 空表：只有「必须填写明细」才拦；必填列不约束空表
      if (mustAdd) {
        msgs.push(title + '：该明细表为必填，请至少填写一行明细');
        // 有预置空白行时把必填列标红，指引用户从哪一列开始填
        reqCols.forEach((col) => {
          marks[table + '|0|' + col.field] = true;
        });
      }
      continue;
    }
    reqCols.forEach((col) => {
      dataRows.forEach(({ row, ri }) => {
        if (isBlankValue(row[col.field])) {
          marks[table + '|' + ri + '|' + col.field] = true;
          msgs.push(title + ' 第' + (ri + 1) + ' 行：请填写「' + (col.label || col.field) + '」');
        }
      });
    });
  }
  detailErrors.value = marks;
  if (msgs.length) nextTick(() => scrollToFirstDetailError());
  return msgs;
}
/** 主表字段格的校验规则：设计器必填 + 规则引擎实时校验错误（值变化即提示，无需等提交） */
function fieldItemRules(cell) {
  const field = cell && cell.field;
  const rules = [];
  if (fieldReq(field) || cellAttr(cell) === 'req') {
    // 本组件的 el-form-item 无 el-form/model 上下文（Grid 布局自管数据），
    // Element 的 required 校验会取不到值而误报，改为闭包直读当前表单数据
    rules.push({
      validator: (r, v, cb) => cb(isBlankValue(props.values && props.values[field]) ? new Error('请填写' + fieldLabel(field)) : undefined),
      trigger: ['blur', 'change']
    });
  }
  const msg = props.ruleErrors && props.ruleErrors[field];
  if (msg) {
    rules.push({ validator: () => false, message: msg, trigger: ['blur', 'change'] });
  }
  return rules;
}
/** 明细行是否有任意值（用于判定"是不是真填了"） */
function rowHasValue(row, cols) {
  if (!row) return false;
  return cols.some((col) => !isBlankValue(row[col.field]));
}
/** 空值判定：undefined / null / 纯空白字符串 / 空数组 均视为未填 */
function isBlankValue(v) {
  if (v === undefined || v === null) return true;
  if (typeof v === 'string') return v.trim() === '';
  if (Array.isArray(v)) return v.length === 0;
  return false;
}
/** 明细单元格是否被标为错误（模板里加红框）：设计器必填 marks ∪ 规则引擎行内错误 */
function isCellError(table, ri, field) {
  const key = (table || '') + '|' + ri + '|' + field;
  return !!detailErrors.value[key] || !!(props.ruleDetailErrors && props.ruleDetailErrors[key]);
}
/** 明细单元格的规则引擎错误文案（悬浮提示用；无错误返回空串不显示 title） */
function detailRuleErrText(table, ri, field) {
  const key = (table || '') + '|' + ri + '|' + field;
  return (props.ruleDetailErrors && props.ruleDetailErrors[key]) || '';
}
/** 提交校验失败后把第一个错误单元格滚入可视区，避免用户在长表单里找不到哪一行没填 */
function scrollToFirstDetailError() {
  const box = rootRef.value;
  const el = box && box.querySelector('.fgr-dcell-error');
  if (el && el.scrollIntoView) el.scrollIntoView({ block: 'center', behavior: 'smooth' });
}
defineExpose({ getDetailData, validateDetails, setDetailData });
</script>

<style lang="scss" scoped>
.fgr-wrap {
  width: 100%;
  box-sizing: border-box;
}
/* ===== 卡片分组（布局含 type=group 时启用，与业务模块弹框表单一致） ===== */
.fgr-cards {
  width: 100%;
}
.fgr-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  margin-bottom: 7px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}
.fgr-card.is-plain {
  background: transparent;
  border: none;
  border-radius: 0;
  margin-bottom: 0;
  box-shadow: none;
  overflow: visible;
}
.fgr-card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 11px 16px 10px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}
/* 分组卡片内字段表格与卡片头/底留出距离：首行不贴卡片标题、末行不贴卡片底边，
   与明细块周围的留白一致（is-plain 无卡片外观的布局不受影响） */
.fgr-card:not(.is-plain) > .fgr-table {
  margin-top: 4px;
  margin-bottom: 8px;
}
.fgr-card-icon {
  position: relative;
  display: inline-block;
  width: 18px;
  height: 18px;
  border-radius: 5px;
  background: #eef2ff;
  flex: none;
}
.fgr-card-icon::after {
  content: '';
  position: absolute;
  inset: 5px;
  border-radius: 2px;
  background: #4f46e5;
}
.fgr-cards-mode .fgr-cell {
  /* 规则引擎隐藏：保留占位（表格矩阵不塌），内容不可见 */
  &.is-ov-hidden { visibility: hidden; }
  padding: 7px 6px;
  background: transparent;
}
.fgr-cards-mode .fgr-cell.t-label {
  /* 标签格外观（背景/边框）尽量由表单设计器的单元格样式驱动：
     cellCss 内联样式优先级最高，设计器里给标签格设置背景色/边框即可生效；
     这里只保留文字基线样式，不再写死背景与线框 */
  background: transparent;
  color: #475569;
  font-weight: 500;
}
.fgr-cards-mode .fgr-fill-cell {
  background: transparent;
}
.fgr-cards-mode .fgr-detail-block {
  border: 1px solid #e8edf3;
  border-radius: 8px;
  /* 合计行与明细块下边框留白：避免合计贴边 */
  padding-bottom: 12px;
}
.fgr-cards-mode .fgr-dsum td {
  padding-top: 8px;
  padding-bottom: 8px;
}
/* 明细块与卡片边框之间留出少量内边距：明细块边框不与卡片边框重合；
   垂直方向强制贴顶：cellCss 会给每个 td 写内联 vertical-align:middle，
   行高富余时明细块居中，上方悬出一条空带——这里 !important 压过内联样式 */
.fgr-cards-mode .fgr-cell.t-detail {
  padding: 0 2px 6px;
  vertical-align: top !important;
}
.fgr-cards-mode .fgr-dbar {
  /* 填写态：标题栏正常占一行，右侧放「＋添加明细」按钮。
     早期用 absolute top:-30px 悬浮到卡片标题行，但 dbar 在卡片模式下
     高度为 0，明细单元格被压缩时宽度也为 0，right:4px 相对零宽容器定位，
     按钮会被定位到容器左边界之外而不可见、不可点，因此改为正常布局 */
  position: relative;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: transparent;
  border-bottom: none;
  padding: 2px 2px 4px;
}
.fgr-cards-mode .fgr-dbar > span {
  position: static;
}
/* 「＋添加明细」按钮美化：浅蓝底胶囊样式，与分组卡片浅蓝色系呼应 */
.fgr-dbar .el-button {
  height: 26px;
  padding: 0 12px;
  border-radius: 13px;
  font-size: 13px;
  background: #eef5fd;
  border: 1px solid #bcd3ec;
  color: #2469b3;
  transition: background-color 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
}
.fgr-dbar .el-button:hover,
.fgr-dbar .el-button:focus {
  background: #dceafa;
  border-color: #8fb6e3;
  color: #1a4f95;
}
.fgr-dbar .el-button:active {
  background: #cfe1f7;
  box-shadow: inset 0 1px 2px rgba(26, 79, 149, 0.15);
}
/* 分组卡片标题已说明是哪个明细表，块内不再重复标题，只保留「添加明细」操作 */
.fgr-cards-mode .fgr-dname {
  display: none;
}
.fgr-cards-mode .fgr-dtable th {
  background: #f8fafc;
  color: #475569;
  border-color: #eef2f7;
}
.fgr-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}
.fgr-fill-col {
  width: auto;
}
.fgr-fill-cell {
  background: #fbfcfe;
}
.fgr-cell {
  /* 规则引擎隐藏：保留占位（表格矩阵不塌），内容不可见 */
  &.is-ov-hidden { visibility: hidden; }
  padding: 5px 8px;
  font-size: 13px;
  background: #fff;
  vertical-align: middle;
  min-width: 0;
}
.fgr-cell.t-title {
  text-align: center;
  /* 标题贴顶：不按行高中对齐，标题上方只留极小间距；
     cellCss 会写内联 vertical-align:middle，需要 !important 压过；
     需要更大留白时在设计器里调大标题行高即可（行高全部生效为内容下方空间） */
  vertical-align: top !important;
  padding-top: 3px;
}
/* 标签格默认浅蓝底：与设计器 .wm-cell.t-text 保持一致；用户自定义背景（cellCss 内联）优先 */
.fgr-cell.t-label {
  background: #dbe9f6;
}
/* 下划线/删除线：textDecoration 由 cellCss 设置在 td 上，块级/弹性子元素需显式继承才能渲染 */
.fgr-cell .fgr-title,
.fgr-cell .fgr-note,
.fgr-cell .fgr-text,
.fgr-cell .val-text {
  text-decoration: inherit;
}
.fgr-title {
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
}
.fgr-note {
  color: #e0342b;
  font-size: 12px;
}
.fgr-field {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}
.fgr-field .lbl {
  color: #475569;
  flex: none;
  max-width: 45%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-top: 5px;
}
.fgr-field .req {
  color: #f56c6c;
  font-style: normal;
  margin-right: 2px;
}
/* ===== 只读回显：纯文本，不画输入框 =====
 * 之前是灰底 + 1px 边框，虽然不能输入，但看起来就是一堆禁用输入框，和只读表单的设计完全不同。*/
.fgr-field .val {
  flex: 1;
  color: #0f172a;
  background: none;
  border: 0;
  border-radius: 0;
  padding: 2px 0;
  min-height: 22px;
  word-break: break-all;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.fgr-field .val.ph .val-text {
  color: #c3ccd8;
}
.fgr-field .val .val-text {
  flex: 1;
  min-width: 0;
  white-space: pre-wrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 只读字段（本节点无编辑权）仅用文字色区分，不再用虚线框/灰底 */
.fgr-field .val.attr-ro .val-text {
  color: #64748b;
}
/* 只读不显示控件图标（⌕ / ▾ / ▣ …），只读表单只有文字 */
.fgr-field .val .f-ico {
  display: none;
}
.fgr-fi {
  margin-bottom: 0 !important;
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: flex-start;
}
/* 编辑态必填标识：控件后红色星号（「输入框 *」） */
.fgr-fi .req {
  color: #f56c6c;
  font-style: normal;
  margin-left: 6px;
  margin-right: 2px;
  line-height: 26px;
  flex: none;
}
/* 只读回显的星号跟在回显框后面（间距由 .fgr-field 的 gap 提供） */
.fgr-field > .req {
  color: #f56c6c;
  font-style: normal;
  margin: 0;
  padding-top: 5px;
  flex: none;
}
/* 星号顶部对齐控件首行（多行文本时居中会飘到框中间），4px 补偿单行控件(32px)的垂直居中 */
.fgr-fi :deep(.el-form-item__content) > .req {
  margin-top: 4px;
}
.fgr-fi :deep(.el-form-item__error) {
  position: static;
  padding-top: 2px;
}
/* ===== 主表字段控件铺满单元格（与明细表输入口径一致） =====
 问题：el-form-item__content 默认 inline-flex，内部 ElInput 无宽度约束时按内容收缩，
 单元格右侧留白、控件偏短，视觉上像"靠右"。
 修复：内容块撑满 + 文本/数字/日期/选择类控件统一 width:100%。 */
/* 必填星号必须与控件同一行：Element Plus 的 __content 默认 flex-wrap: wrap，
 星号占位后够不下 width:100% 的控件，控件会被换到下一行、星号孤立成行（很难看）。
 这里禁止换行 + 放开子项收缩，控件自然填满剩余宽度。 */
.fgr-fi :deep(.el-form-item__content) {
  flex: 1;
  min-width: 0;
  width: 100%;
  flex-wrap: nowrap;
  align-items: flex-start;
}
.fgr-fi :deep(.el-form-item__content) > *:not(.req) {
  min-width: 0;
}
.fgr-fi :deep(.el-input),
.fgr-fi :deep(.el-input-number),
.fgr-fi :deep(.el-date-editor),
.fgr-fi :deep(.el-select),
.fgr-fi :deep(.el-textarea) {
  width: 100%;
}
.fgr-fi :deep(.el-input-number .el-input__wrapper) {
  width: 100%;
}
.fgr-fi :deep(.el-textarea__inner) {
  width: 100%;
  box-sizing: border-box;
}
/* 单选按钮组（紧急程度等系统字段）：圆点 + 档位平铺，收窄间距，窄单元格内不挤成两行 */
.fgr-fi :deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 0 14px;
  width: 100%;
  line-height: 26px;
}
.fgr-fi :deep(.el-radio) {
  margin-right: 0;
  height: 26px;
}
/* 签字意见等富文本字段：编辑器铺满单元格，工具栏紧凑，与表单边框风格一致 */
.fgr-fi :deep(.editor),
.fgr-fi :deep(.ql-toolbar),
.fgr-fi :deep(.ql-container) {
  width: 100%;
  box-sizing: border-box;
}
.fgr-fi :deep(.ql-toolbar.ql-snow) {
  border-color: #dbe3ec;
  padding: 4px 6px;
  flex-wrap: wrap;
}
.fgr-fi :deep(.ql-container.ql-snow) {
  border-color: #dbe3ec;
  font-size: 13px;
}
.fgr-fi :deep(.ql-editor) {
  min-height: 110px;
  line-height: 1.7;
}
.fgr-field > .fgr-fi {
  width: 100%;
}
/* 只读附件控件：与字段值区同宽，文件列表不溢出单元格 */
.fgr-field > :deep(.upload-file) {
  flex: 1;
  min-width: 0;
}
.fgr-detail {
  color: #64748b;
  font-size: 12px;
}
/* ===== 运行时明细表块（表单运行态） ===== */
/* 明细格去掉单元格内边距：让明细块边框与主表字段表格的左右边框对齐 */
.fgr-cell.t-detail {
  padding: 0;
}
.fgr-detail-block {
  border: 1px solid #c9d9ea;
  border-radius: 6px;
  overflow: hidden;
  width: 100%;
}
/* 明细标题栏：与主表标签格同色系（浅蓝底 + 深蓝字），避免深浅反差过大 */
.fgr-dbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #dbe9f6;
  border-bottom: 1px solid #c9d9ea;
  color: #24476e;
  padding: 7px 10px;
}
.fgr-dname {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  color: #24476e;
}
.fgr-dname .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2f6fd0;
}
.fgr-dtable {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.fgr-dtable th {
  background: #fef3c7;
  color: #92400e;
  padding: 6px 8px;
  border: 1px solid #fde68a;
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
}
.fgr-dtable th .req {
  color: #f56c6c;
  font-style: normal;
  margin-left: 2px;
}
.fgr-dtable td {
  border: 1px solid #eef2f7;
  padding: 4px 6px;
}
/* 明细必填未填：单元格浅红底 + 输入框红边（提交校验后高亮，编辑即消除） */
.fgr-dtable td.fgr-dcell-error {
  /* 明细列规则隐藏：占位不渲染内容（同主表 is-ov-hidden 口径） */
  &.is-ov-hidden { visibility: hidden; }
  background: #fff5f5;
  box-shadow: inset 0 0 0 1px #f8b4b4;
}
.fgr-dtable td.fgr-dcell-error :deep(.el-input__wrapper),
.fgr-dtable td.fgr-dcell-error :deep(.el-select__wrapper),
.fgr-dtable td.fgr-dcell-error :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #f56c6c inset;
  background: #fff;
}
.fgr-dsum td {
  background: #f8fafc;
}
/* 明细表只读列：纯文本回显（不再渲染禁用输入框） */
.fgr-dtable .fgr-dro {
  display: inline-block;
  color: #334155;
  padding: 0 2px;
  line-height: 24px;
}
/* 明细无数据时的占位行：虚线底 + 灰字，提示"可直接提交/点添加明细" */
.fgr-dtable tr.fgr-dempty-row td {
  padding: 12px 10px;
  text-align: center;
  color: #94a3b8;
  font-size: 12.5px;
  background: #fbfcfe;
}
.fgr-dempty {
  padding: 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}
.fgr-text {
  white-space: pre-wrap;
}
/* 图片格：图片铺满格子、object-fit contain 等比缩放，行高列宽拖动完全不受图片限制；对齐用 object-position */
.fgr-img-wrap {
  position: relative;
  display: block;
  width: 100%;
  height: 100%;
  min-height: 24px;
  min-width: 0;
  overflow: hidden;
}
.fgr-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
  max-width: none;
  max-height: none;
  object-fit: contain;
  object-position: center;
  display: block;
}
.fgr-img-wrap.img-left .fgr-img {
  object-position: left center;
}
.fgr-img-wrap.img-right .fgr-img {
  object-position: right center;
}
</style>
