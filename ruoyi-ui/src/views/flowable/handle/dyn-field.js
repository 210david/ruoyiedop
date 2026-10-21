import { defineComponent, h, resolveComponent } from 'vue'
import { ElSelect, ElOption, ElRadioGroup, ElRadio, ElCheckbox, ElCheckboxGroup, ElSwitch, ElRate, ElInput, ElInputNumber, ElDatePicker } from 'element-plus'

/**
 * 办理页动态字段控件：仅渲染节点字段权限为「编辑」的字段。
 * rtype 由 normalizeHandleField 归一化，与发起页 DynField 口径一致（去掉 browse 浏览按钮）。
 */
export const DynField = defineComponent({
  name: 'HandleDynField',
  props: {
    f: { type: Object, required: true },
    modelValue: { type: [String, Number, Boolean, Array], default: undefined }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const set = v => emit('update:modelValue', v)
    return () => {
      const f = props.f
      const v = props.modelValue
      switch (f.rtype) {
        case 'select': return h(ElSelect, {
          modelValue: v, 'onUpdate:modelValue': set, style: 'width:100%', clearable: true, placeholder: f.placeholder || '请选择'
        }, () => (f.optionList || []).map(o => h(ElOption, { key: o.value, label: o.label, value: o.value })))
        case 'radio': return h(ElRadioGroup, { modelValue: v, 'onUpdate:modelValue': set },
          () => (f.optionList || []).map(o => h(ElRadio, { key: o.value, value: o.value }, () => o.label)))
        case 'checkbox': return h(ElCheckboxGroup, {
          modelValue: v ? String(v).split(',').filter(Boolean) : [],
          'onUpdate:modelValue': arr => set((arr || []).join(','))
        }, () => (f.optionList || []).map(o => h(ElCheckbox, { key: o.value, value: o.value }, () => o.label)))
        case 'check': return h(ElCheckbox, { modelValue: v, 'onUpdate:modelValue': set, trueValue: '1', falseValue: '0' }, () => '是')
        case 'switch': return h(ElSwitch, { modelValue: v, 'onUpdate:modelValue': set, activeValue: '1', inactiveValue: '0' })
        case 'rate': return h(ElRate, { modelValue: v, 'onUpdate:modelValue': set })
        case 'attach': return h(resolveComponent('FileUpload'), { modelValue: v, 'onUpdate:modelValue': set, limit: f.attachLimit || 5, fileSize: 50 })
        case 'textarea': return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, type: 'textarea', rows: f.rows || 4, placeholder: f.placeholder || '请输入' })
        case 'number': return h(ElInputNumber, { modelValue: v, 'onUpdate:modelValue': set, controls: false, style: 'width:100%', placeholder: f.placeholder || '请输入' })
        case 'date': return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'date', valueFormat: 'YYYY-MM-DD', placeholder: f.placeholder || '选择日期', style: 'width:100%' })
        case 'datetime': return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss', placeholder: f.placeholder || '选择日期时间', style: 'width:100%' })
        default: return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, placeholder: f.placeholder || '请输入' })
      }
    }
  }
})

export default DynField
