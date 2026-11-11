/**
 * 网格字段控件渲染（P1）：按字段定义渲染真实 Element 控件。
 * 与发起页 DynField 同逻辑，但自包含（rtype/选项内部推导），供 FormGridRender 使用，
 * 避免组件反向引用页面模块造成循环依赖。
 */
import { defineComponent, h, resolveComponent } from 'vue'
import { ElSelect, ElOption, ElRadioGroup, ElRadio, ElCheckbox, ElCheckboxGroup, ElSwitch, ElRate, ElInput, ElInputNumber, ElDatePicker } from 'element-plus'
import FormBrowse from '@/components/FormBrowse/index.vue'
import { htmlToText } from '@/utils'

/**
 * 系统字段固定渲染类型：
 * - 紧急程度：单选按钮组（圆点，档位全部平铺展示）
 * - 签字意见：富文本编辑器（与「通知公告-内容」同一个 Editor 组件，可设字体/字号/粗细等）
 * 不依赖字段配置里存下的 form（历史表单存的是 select / textarea）。
 */
const RTYPE_FORCED = { urgency: 'radio', signOpinion: 'editor' }

/** 字段定义 → 渲染类型 rtype（兼容新版 {form,dtype} 与旧版 {type}） */
export function rtypeOf(f) {
  if (!f) return 'text'
  const forced = f.field ? RTYPE_FORCED[f.field] : ''
  if (forced) return forced
  if (f.rtype) return f.rtype
  if (f.form) {
    if (['check', 'browse', 'attach', 'select', 'textarea', 'radio', 'checkbox', 'switch', 'rate', 'date', 'datetime', 'number'].includes(f.form)) return f.form
    return f.dtype === 'datetime' ? 'datetime' : f.dtype === 'date' ? 'date' : (f.dtype === 'float' || f.dtype === 'int') ? 'number' : 'text'
  }
  const t = f.type
  if (['number', 'date', 'datetime', 'textarea', 'select', 'radio', 'check', 'browse', 'attach', 'switch', 'rate'].includes(t)) return t
  return 'text'
}

/** 字段定义 → 选项列表 [{label,value}]（selectOptions 键值对优先，options 串兼容） */
export function optionListOf(f) {
  if (!f) return []
  if (Array.isArray(f.selectOptions) && f.selectOptions.length) {
    return f.selectOptions.map(o => typeof o === 'object' ? { label: o.label, value: o.value !== undefined ? o.value : o.label } : { label: o, value: o })
  }
  const raw = f.options
  const arr = Array.isArray(raw) ? raw : String(raw || '').split(/[,，、;；]/).map(s => s.trim()).filter(Boolean)
  return arr.map(o => {
    if (typeof o === 'object') return { label: o.label, value: o.value !== undefined ? o.value : o.label }
    const idx = String(o).indexOf(':')
    if (idx > 0) return { label: String(o).substring(0, idx).trim(), value: String(o).substring(idx + 1).trim() }
    return { label: o, value: o }
  })
}

/** 值 → 显示文本（select/radio/checkbox 按选项映射 label；空值返回占位） */
export function displayTextOf(f, val, placeholder = '—') {
  if (val === undefined || val === null || val === '') return placeholder
  const rt = rtypeOf(f)
  // 富文本按纯文本回显，避免时间轴/只读表格里露出 HTML 标签
  if (rt === 'editor') return htmlToText(val) || placeholder
  if (rt === 'select' || rt === 'radio') {
    const hit = optionListOf(f).find(o => String(o.value) === String(val))
    return hit ? hit.label : val
  }
  if (rt === 'checkbox') {
    // 复选勾选：数据库以逗号分隔多值存储，逐值映射为显示值
    return String(val).split(',').map(s => {
      const hit = optionListOf(f).find(o => String(o.value) === String(s.trim()))
      return hit ? hit.label : s.trim()
    }).join(',')
  }
  return val
}

export default defineComponent({
  name: 'FormGridControl',
  props: {
    f: { type: Object, required: true },
    modelValue: { type: [String, Number, Boolean], default: undefined },
    /** 浏览按钮伴生值：已选 ID（逗号分隔），仅用于弹框回显 */
    idValue: { type: [String, Array], default: '' },
    /** 浏览按钮附加信息（人员带回部门），存到 `${field}__extra` */
    extraValue: { type: Object, default: null },
    styleObj: { type: Object, default: null },
    disabled: { type: Boolean, default: false },
    required: { type: Boolean, default: false }
  },
  emits: ['update:modelValue', 'update:idValue', 'update:extraValue', 'browse'],
  setup(props, { emit }) {
    const set = v => emit('update:modelValue', v)
    const setId = v => emit('update:idValue', v)
    const setExtra = v => emit('update:extraValue', v)
    return () => {
      const f = props.f
      const v = props.modelValue
      const fmt = props.styleObj && props.styleObj.fmt
      const rt = rtypeOf(f)
      // 日期格式：单元格格式弹窗选择的 pattern → valueFormat；时间分类 → datetime
      const DATE_VF = { '2013/3/14': 'YYYY/M/D', '2013-3-14': 'YYYY-M-D', '2013年3月14日': 'YYYY年M月D日', '2013年3月': 'YYYY年M月', '3月14日': 'M月D日', '2013/3/14 10:13 AM': 'YYYY/M/D hh:mm A', '2013/3/14 10:13': 'YYYY/M/D HH:mm' }
      const dateVf = (fmt && fmt.pattern && DATE_VF[fmt.pattern]) || 'YYYY-MM-DD'
      const isTime = fmt && fmt.cat === 'time'
      const numPrec = fmt && (fmt.cat === 'number' || fmt.cat === 'percent') ? (fmt.decimals == null ? 2 : fmt.decimals) : undefined
      const dis = props.disabled
      switch (rt) {
        case 'select': return h(ElSelect, { modelValue: v, 'onUpdate:modelValue': set, style: 'width:100%', placeholder: f.placeholder || '请选择', disabled: dis },
          () => optionListOf(f).map(o => h(ElOption, { key: o.value, label: o.label, value: o.value })))
        case 'radio': return h(ElRadioGroup, { modelValue: v, 'onUpdate:modelValue': set, disabled: dis },
          () => optionListOf(f).map(o => h(ElRadio, { key: o.value, value: o.value }, () => o.label)))
        case 'editor': return h(resolveComponent('Editor'), {
          modelValue: v || '',
          'onUpdate:modelValue': set,
          minHeight: f.editorHeight || 140,
          readOnly: dis
        })
        case 'checkbox': return h(ElCheckboxGroup, {
          modelValue: v ? String(v).split(',').filter(Boolean) : [],
          'onUpdate:modelValue': arr => set((arr || []).join(',')),
          disabled: dis
        }, () => optionListOf(f).map(o => h(ElCheckbox, { key: o.value, value: o.value }, () => o.label)))
        case 'check': return h(ElCheckbox, { modelValue: v, 'onUpdate:modelValue': set, trueValue: '1', falseValue: '0', disabled: dis })
        case 'switch': return h(ElSwitch, { modelValue: v, 'onUpdate:modelValue': set, activeValue: '1', inactiveValue: '0', disabled: dis })
        case 'rate': return h(ElRate, { modelValue: v, 'onUpdate:modelValue': set, disabled: dis })
        // 浏览按钮：交由 FormBrowse 按字段配置的数据源打开对应弹框选择器（人员/部门/客户/物料/设备…）
        case 'browse': return h(FormBrowse, {
          f,
          modelValue: v,
          modelIds: props.idValue,
          modelExtra: props.extraValue,
          disabled: dis,
          'onUpdate:modelValue': set,
          'onUpdate:modelIds': setId,
          'onUpdate:modelExtra': setExtra
        })
        case 'attach': return h(resolveComponent('FileUpload'), { modelValue: v, 'onUpdate:modelValue': set, limit: f.attachLimit || 5, fileSize: 50, disabled: dis, inlineTip: true, reqStar: props.required })
        case 'textarea': return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, type: 'textarea', rows: f.rows || 4, placeholder: f.placeholder, disabled: dis })
        case 'number': return h(ElInputNumber, { modelValue: v, 'onUpdate:modelValue': set, controls: false, precision: numPrec, style: 'width:100%', placeholder: f.placeholder || '请输入', disabled: dis })
        case 'date': return isTime
          ? h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss', placeholder: f.placeholder || '选择时间', style: 'width:100%', disabled: dis })
          : h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'date', valueFormat: dateVf, placeholder: f.placeholder || '选择日期', style: 'width:100%', disabled: dis })
        case 'datetime': return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss', placeholder: f.placeholder || '选择日期时间', style: 'width:100%', disabled: dis })
        default: return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, placeholder: f.placeholder || '请输入', disabled: dis })
      }
    }
  }
})
