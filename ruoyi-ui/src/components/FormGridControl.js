/**
 * 网格字段控件渲染（P1）：按字段定义渲染真实 Element 控件。
 * 与发起页 DynField 同逻辑，但自包含（rtype/选项内部推导），供 FormGridRender 使用，
 * 避免组件反向引用页面模块造成循环依赖。
 */
import { defineComponent, h, resolveComponent } from 'vue'
import { ElSelect, ElOption, ElRadioGroup, ElRadio, ElCheckbox, ElSwitch, ElRate, ElInput, ElInputNumber, ElDatePicker, ElButton } from 'element-plus'

/** 字段定义 → 渲染类型 rtype（兼容新版 {form,dtype} 与旧版 {type}） */
export function rtypeOf(f) {
  if (!f) return 'text'
  if (f.rtype) return f.rtype
  if (f.form) {
    if (['check', 'browse', 'attach', 'select', 'textarea', 'radio', 'switch', 'rate', 'date', 'datetime', 'number'].includes(f.form)) return f.form
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

/** 值 → 显示文本（select/radio 按选项映射 label；空值返回占位） */
export function displayTextOf(f, val, placeholder = '—') {
  if (val === undefined || val === null || val === '') return placeholder
  const rt = rtypeOf(f)
  if (rt === 'select' || rt === 'radio') {
    const hit = optionListOf(f).find(o => String(o.value) === String(val))
    return hit ? hit.label : val
  }
  return val
}

export default defineComponent({
  name: 'FormGridControl',
  props: { f: { type: Object, required: true }, modelValue: { type: [String, Number, Boolean], default: undefined }, styleObj: { type: Object, default: null }, disabled: { type: Boolean, default: false } },
  emits: ['update:modelValue', 'browse'],
  setup(props, { emit }) {
    const set = v => emit('update:modelValue', v)
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
        case 'select': return h(ElSelect, { modelValue: v, 'onUpdate:modelValue': set, style: 'width:100%', disabled: dis },
          () => optionListOf(f).map(o => h(ElOption, { key: o.value, label: o.label, value: o.value })))
        case 'radio': return h(ElRadioGroup, { modelValue: v, 'onUpdate:modelValue': set, disabled: dis },
          () => optionListOf(f).map(o => h(ElRadio, { key: o.value, value: o.value }, () => o.label)))
        case 'check': return h(ElCheckbox, { modelValue: v, 'onUpdate:modelValue': set, trueValue: '1', falseValue: '0', disabled: dis }, () => '是')
        case 'switch': return h(ElSwitch, { modelValue: v, 'onUpdate:modelValue': set, activeValue: '1', inactiveValue: '0', disabled: dis })
        case 'rate': return h(ElRate, { modelValue: v, 'onUpdate:modelValue': set, disabled: dis })
        case 'browse': return h('div', { style: 'display:flex;gap:8px;width:100%' }, [
          h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, readonly: true, disabled: dis, placeholder: '点击右侧按钮选择数据' }),
          h(ElButton, { onClick: () => emit('browse', f), disabled: dis }, () => '浏览…')
        ])
        case 'attach': return h(resolveComponent('FileUpload'), { modelValue: v, 'onUpdate:modelValue': set, limit: f.attachLimit || 5, fileSize: 50, disabled: dis })
        case 'textarea': return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, type: 'textarea', rows: f.rows || 4, disabled: dis })
        case 'number': return h(ElInputNumber, { modelValue: v, 'onUpdate:modelValue': set, controls: false, precision: numPrec, style: 'width:100%', disabled: dis })
        case 'date': return isTime
          ? h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss', placeholder: '选择时间', style: 'width:100%', disabled: dis })
          : h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'date', valueFormat: dateVf, placeholder: '选择日期', style: 'width:100%', disabled: dis })
        case 'datetime': return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss', placeholder: '选择日期时间', style: 'width:100%', disabled: dis })
        default: return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, disabled: dis })
      }
    }
  }
})
