import { defineComponent, h, resolveComponent } from 'vue'
import { ElSelect, ElOption, ElRadioGroup, ElRadio, ElCheckbox, ElSwitch, ElRate, ElInput, ElInputNumber, ElDatePicker, ElButton } from 'element-plus'

/** 动态字段控件（审批页复用）：按 rtype 渲染对应 Element 控件 */
export const DynField = defineComponent({
  name: 'DynField',
  props: { f: { type: Object, required: true }, modelValue: { type: [String, Number, Boolean], default: undefined } },
  emits: ['update:modelValue', 'browse'],
  setup(props, { emit }) {
    const set = v => emit('update:modelValue', v)
    return () => {
      const f = props.f
      const v = props.modelValue
      switch (f.rtype) {
        case 'select': return h(ElSelect, { modelValue: v, 'onUpdate:modelValue': set, style: 'width:100%' },
          () => (f.optionList || []).map(o => h(ElOption, { key: o.value, label: o.label, value: o.value })))
        case 'radio': return h(ElRadioGroup, { modelValue: v, 'onUpdate:modelValue': set },
          () => (f.optionList || []).map(o => h(ElRadio, { key: o.value, value: o.value }, () => o.label)))
        case 'check': return h(ElCheckbox, { modelValue: v, 'onUpdate:modelValue': set, trueValue: '1', falseValue: '0' }, () => '是')
        case 'switch': return h(ElSwitch, { modelValue: v, 'onUpdate:modelValue': set, activeValue: '1', inactiveValue: '0' })
        case 'rate': return h(ElRate, { modelValue: v, 'onUpdate:modelValue': set })
        case 'browse': return h('div', { style: 'display:flex;gap:8px;width:100%' }, [
          h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, readonly: true, placeholder: '审批页只读展示' })
        ])
        case 'attach': return h(resolveComponent('FileUpload'), { modelValue: v, 'onUpdate:modelValue': set, limit: f.attachLimit || 5, fileSize: 50 })
        case 'textarea': return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, type: 'textarea', rows: f.rows || 4 })
        case 'number': return h(ElInputNumber, { modelValue: v, 'onUpdate:modelValue': set, controls: false, style: 'width:100%' })
        case 'date': return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'date', valueFormat: 'YYYY-MM-DD', placeholder: '选择日期', style: 'width:100%' })
        case 'datetime': return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss', placeholder: '选择日期时间', style: 'width:100%' })
        default: return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set })
      }
    }
  }
})
export default DynField
