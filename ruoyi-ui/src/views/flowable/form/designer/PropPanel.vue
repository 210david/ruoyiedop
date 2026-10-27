<template>
  <div class="wm-pp">
    <div class="wm-pp-head">
      <div class="wm-pp-row"><span class="k">模板名称</span><span class="v">{{ formName || '未命名表单' }}</span></div>
      <div class="wm-pp-row"><span class="k">表　　单</span><span class="v">{{ formName || '—' }}（{{ formKey || '—' }}）</span></div>
    </div>
    <el-tabs v-model="tab" class="wm-pp-tabs">
      <el-tab-pane label="主表字段" name="fields">
        <div class="wm-pp-search">
          <el-input v-model="kw" size="small" placeholder="搜索字段" clearable @input="kw = $event">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="wm-pp-hint">标签和字段完全独立：拖入“字段”只放字段控件，需要标签时再拖“标签”。已添加项不能重复添加，删除后恢复。</div>
        <div class="wm-pp-table">
          <div class="wm-pp-tr wm-pp-th"><span>标签</span><span>字段</span></div>
          <div class="wm-pp-scroll">
            <div v-for="f in filteredFields" :key="f.field" class="wm-pp-tr wm-pp-td">
              <button class="wm-pp-part nm" :draggable="!placedLabels.has(f.field)" :disabled="placedLabels.has(f.field)"
                @dragstart="e => startDrag(e, 'label', f)" @click="onPickLabel(f)"
                :title="placedLabels.has(f.field) ? '该标签已添加到表单' : '添加标签：' + f.label">
                {{ f.label }}<i v-if="placedLabels.has(f.field)" class="ok">✔</i>
              </button>
              <button class="wm-pp-part cd" :draggable="!placed.has(f.field)" :disabled="placed.has(f.field)"
                @dragstart="e => startDrag(e, 'field', f)" @click="onPickField(f)"
                :title="placed.has(f.field) ? '该字段已添加到表单' : '添加字段控件：' + f.label">
                {{ f.label }}<i v-if="placed.has(f.field)" class="ok">✔</i>
              </button>
            </div>
            <div v-if="!filteredFields || !filteredFields.length" class="wm-pp-empty">暂无匹配字段</div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="流转意见" name="flow">
        <div class="wm-pp-empty">流转意见按流程节点展示，属于 P2 阶段能力</div>
      </el-tab-pane>
    </el-tabs>
    <div class="wm-pp-sec">
      <div class="wm-pp-title">单元格属性</div>
      <template v-if="activeCell">
        <div class="wm-pp-prop">
          <label>内容类型</label>
          <el-select v-model="typeModel" size="small" @change="v => $emit('patch', { type: v, ...(v === 'text' ? { field: undefined } : { text: '' }) })">
            <el-option value="title" label="标题行" /><el-option value="text" label="文本" />
            <el-option value="note" label="说明" /><el-option value="field" label="字段" />
          </el-select>
        </div>
        <div class="wm-pp-prop" v-if="['title', 'text', 'note'].includes(activeCell.type)">
          <label>文本内容</label>
          <el-input v-model="textModel" type="textarea" :rows="2" placeholder="输入单元格文本" @change="v => $emit('patch', { text: v })" />
        </div>
        <div class="wm-pp-prop" v-if="activeCell.type === 'field'">
          <label>绑定字段</label>
          <div class="bind-row">
            <el-input :model-value="bindLabel" readonly size="small" />
            <el-button size="small" icon="Search" @click="$emit('pick')" />
            <el-button size="small" icon="CircleClose" @click="$emit('clear-bind')" title="清除绑定" />
          </div>
        </div>
        <div class="wm-pp-prop">
          <label>字体 / 字号</label>
          <div class="row2">
            <el-select v-model="fontModel" size="small" @change="v => $emit('set', { k: 'font', v })">
              <el-option v-for="f in fonts" :key="f" :label="f === 'Microsoft YaHei' ? '微软雅黑' : f" :value="f" />
            </el-select>
            <el-select v-model="sizeModel" size="small" style="width:70px" @change="v => $emit('set', { k: 'fontSize', v: Number(v) })">
              <el-option v-for="s in [10, 11, 12, 14, 16, 18, 20, 24]" :key="s" :label="s" :value="s" />
            </el-select>
          </div>
        </div>
        <div class="wm-pp-prop">
          <label>样式 / 颜色</label>
          <div class="row2">
            <button class="wm-btn" :class="{ on: activeCell.style.bold }" @click="$emit('toggle', 'bold')"><b>B</b></button>
            <button class="wm-btn" :class="{ on: activeCell.style.italic }" @click="$emit('toggle', 'italic')"><i>I</i></button>
            <button class="wm-btn" :class="{ on: activeCell.style.underline }" @click="$emit('toggle', 'underline')"><u>U</u></button>
            <el-color-picker :model-value="activeCell.style.color || '#333333'" size="small" @update:model-value="v => $emit('set', { k: 'color', v: v || '#333333' })" />
            <el-color-picker :model-value="activeCell.style.bg || ''" size="small" @update:model-value="v => $emit('set', { k: 'bg', v: v || '' })" />
          </div>
        </div>
        <div class="wm-pp-prop">
          <label>对齐</label>
          <div class="row2">
            <el-select v-model="alignModel" size="small" @change="v => $emit('set', { k: 'align', v })">
              <el-option value="left" label="左对齐" /><el-option value="center" label="居中" /><el-option value="right" label="右对齐" />
            </el-select>
            <el-select v-model="valignModel" size="small" @change="v => $emit('set', { k: 'valign', v })">
              <el-option value="top" label="顶对齐" /><el-option value="middle" label="垂直居中" /><el-option value="bottom" label="底对齐" />
            </el-select>
          </div>
        </div>
      </template>
      <div v-else class="wm-pp-empty">点击画布单元格后在此设置属性</div>
    </div>
    <div class="wm-pp-sec" v-if="fieldDef">
      <div class="wm-pp-title">字段信息</div>
      <div class="wm-pp-row"><span class="k">显示名</span><span class="v">{{ fieldDef.label }}</span></div>
      <div class="wm-pp-row"><span class="k">字段名</span><span class="v">{{ fieldDef.field }}</span></div>
      <div class="wm-pp-row"><span class="k">表现形式</span><span class="v">{{ formLabel(fieldDef.form) }}</span></div>
      <div class="wm-pp-row"><span class="k">数据类型</span><span class="v">{{ fieldDef.dtype || 'text' }}</span></div>
      <div class="wm-pp-row"><span class="k">必　填</span><span class="v">{{ fieldDef.required ? '是' : '否' }}</span></div>
    </div>
  </div>
</template>

<script setup name="WmPropPanel">
import { computed, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  formName: String,
  formKey: String,
  fields: { type: Array, default: () => [] },
  placed: { type: Object, default: () => new Set() },
  placedLabels: { type: Object, default: () => new Set() },
  activeCell: { type: Object, default: null },
  fieldDefs: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['bind', 'insert-label', 'clear-bind', 'pick', 'patch', 'set', 'toggle'])
/** 标签与字段使用独立拖拽协议，匹配泛微右侧两列分别添加的交互。 */
function startDrag(e, kind, f) {
  const used = kind === 'field' ? props.placed.has(f.field) : props.placedLabels.has(f.field)
  if (!e.dataTransfer || used) return
  e.dataTransfer.effectAllowed = 'copy'
  e.dataTransfer.setData('text/plain', `wm${kind}:${f.field}`)
}
/** 标签、字段分别只允许添加一次；删除画布内容后对应入口自动恢复。 */
function onPickLabel(f) { if (!props.placedLabels.has(f.field)) emit('insert-label', f) }
function onPickField(f) { if (!props.placed.has(f.field)) emit('bind', f.field) }

const tab = ref('fields')
const kw = ref('')
/** 字段搜索过滤（标签/字段名匹配） */
const filteredFields = computed(() => {
  const k = (kw.value || '').trim().toLowerCase()
  if (!k) return props.fields || []
  return (props.fields || []).filter(f => (f.label || '').toLowerCase().includes(k) || (f.field || '').toLowerCase().includes(k))
})
const fonts = ['Microsoft YaHei', 'SimSun', 'SimHei', 'KaiTi', 'FangSong', 'DengXian']
const FORM_LABELS = { text: '单行文本框', textarea: '多行文本框', browse: '浏览按钮', check: 'Check框', select: '选择框', attach: '附件上传', date: '日期框', datetime: '日期时间框', number: '数字框', radio: '单选框', switch: '开关框', rate: '评分框' }

const typeModel = computed({
  get: () => (props.activeCell ? props.activeCell.type : 'text'),
  set: () => {}
})
const textModel = computed({ get: () => (props.activeCell ? props.activeCell.text || '' : ''), set: () => {} })
const fontModel = computed({ get: () => (props.activeCell ? (props.activeCell.style.font || 'Microsoft YaHei') : 'Microsoft YaHei'), set: () => {} })
const sizeModel = computed({ get: () => (props.activeCell ? (props.activeCell.style.fontSize || 12) : 12), set: () => {} })
const alignModel = computed({ get: () => (props.activeCell ? (props.activeCell.style.align || 'left') : 'left'), set: () => {} })
const valignModel = computed({ get: () => (props.activeCell ? (props.activeCell.style.valign || 'middle') : 'middle'), set: () => {} })

const bindLabel = computed(() => {
  const f = props.activeCell && props.activeCell.field
  if (!f) return '未绑定'
  const def = props.fieldDefs[f]
  return def ? (def.label + '（' + f + '）') : f
})
const fieldDef = computed(() => {
  const f = props.activeCell && props.activeCell.field
  return f ? props.fieldDefs[f] : null
})
function formLabel(k) { return FORM_LABELS[k] || k || '—' }
</script>

<style lang="scss" scoped>
.wm-pp {
  width: 268px; flex: none; border: 1px solid #e2e8f0; border-radius: 8px; background: #fff;
  display: flex; flex-direction: column; overflow: auto; max-height: 560px;
}
.wm-pp-head { padding: 8px 10px; border-bottom: 1px solid #f1f5f9; background: #fafbfe; }
.wm-pp-row { display: flex; font-size: 12px; line-height: 1.9; }
.wm-pp-row .k { width: 62px; flex: none; color: #94a3b8; }
.wm-pp-row .v { flex: 1; color: #0f172a; word-break: break-all; }
.wm-pp-tabs { padding: 0 6px; }
/* 泛微式两列字段清单：标签 | 字段，表头 + 搜索框 */
.wm-pp-search { padding: 6px 2px 4px; }
.wm-pp-hint { margin: 0 2px 6px; padding: 6px 8px; border-radius: 4px; background: #f0f4ff; color: #6470a8; font-size: 11px; line-height: 1.5; }
.wm-pp-table { display: flex; flex-direction: column; border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden; margin: 0 2px; }
.wm-pp-tr { display: grid; grid-template-columns: 1fr 1fr; align-items: stretch; }
.wm-pp-th { background: #f5f7fa; color: #475569; font-size: 12px; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
.wm-pp-th span { padding: 6px 10px; }
.wm-pp-th span + span { border-left: 1px solid #e2e8f0; }
.wm-pp-scroll { max-height: 220px; overflow: auto; }
.wm-pp-td { min-height: 31px; border-bottom: 1px solid #f1f5f9; }
.wm-pp-td:last-child { border-bottom: none; }
.wm-pp-part { min-width: 0; border: 0; background: #fff; padding: 6px 10px; text-align: left; cursor: grab; font: inherit; }
.wm-pp-part + .wm-pp-part { border-left: 1px solid #f1f5f9; }
.wm-pp-part:hover:not(:disabled) { color: #2F3C8E; background: #f0f4ff; }
.wm-pp-td .nm { color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.wm-pp-td .cd { color: #64748b; font-size: 11px; display: inline-flex; align-items: center; gap: 4px; white-space: nowrap; overflow: hidden; }
.wm-pp-td .cd .ok { color: #16a34a; font-style: normal; }
.wm-pp-part:disabled { color: #cbd5e1; background: #fafbfe; cursor: not-allowed; }
.wm-pp-sec { border-top: 1px solid #f1f5f9; padding: 8px 10px; }
.wm-pp-title { font-size: 12px; font-weight: 600; color: #0f172a; margin-bottom: 6px; }
.wm-pp-prop { margin-bottom: 8px; }
.wm-pp-prop label { display: block; font-size: 11px; color: #94a3b8; margin-bottom: 3px; }
.wm-pp-prop .row2 { display: flex; align-items: center; gap: 6px; }
.wm-pp-prop .row2 .el-select { flex: 1; }
.wm-btn { min-width: 24px; height: 22px; padding: 0 5px; border: 1px solid transparent; border-radius: 4px; background: #f1f5f9; cursor: pointer; font-size: 12px; color: #334155; }
.wm-btn.on { background: #2F3C8E; color: #fff; }
.bind-row { display: flex; gap: 4px; }
.bind-row .el-input { flex: 1; }
.wm-pp-empty { font-size: 12px; color: #94a3b8; text-align: center; padding: 12px 4px; }
</style>
