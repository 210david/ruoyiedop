/**
 * 流程办理页字段归一化（与发起页 normalizeField 口径一致）
 * 新版 {form,dtype} 与旧版 {type} 统一为渲染类型 rtype；select/radio/checkbox 兼容键值对/字符串/数组
 * @param {Object} f 表单字段定义
 * @param {String} perm 节点字段权限：edit / readonly / hidden
 */
export function normalizeHandleField(f, perm) {
  const g = { ...f }
  let t = f.type
  if (f.form) {
    if (['check', 'browse', 'attach', 'select', 'textarea', 'radio', 'checkbox', 'switch', 'rate'].includes(f.form)) t = f.form
    else if (f.form === 'date' || f.form === 'datetime') t = f.form
    else if (f.form === 'number') t = 'number'
    else t = f.dtype === 'datetime' ? 'datetime' : f.dtype === 'date' ? 'date' : (f.dtype === 'float' || f.dtype === 'int') ? 'number' : 'text'
  } else if (!t) t = 'text'
  g.rtype = t
  if (t === 'select' || t === 'radio' || t === 'checkbox') {
    if (Array.isArray(f.selectOptions) && f.selectOptions.length) {
      g.optionList = f.selectOptions.map(o => typeof o === 'object' ? { label: o.label, value: o.value !== undefined ? o.value : o.label } : { label: o, value: o })
    } else if (Array.isArray(f.options)) {
      g.optionList = f.options.map(o => typeof o === 'object' ? o : { label: o, value: o })
    } else if (typeof f.options === 'string' && f.options) {
      g.optionList = f.options.split(/[,，]/).map(s => s.trim()).filter(Boolean).map(s => ({ label: s, value: s }))
    } else {
      g.optionList = []
    }
  }
  g._perm = perm === 'edit' || perm === 'hidden' ? perm : 'readonly'
  return g
}

/** 只读值显示（空值统一为 -） */
export function handleDisplayValue(v) {
  if (v === undefined || v === null || v === '') return '-'
  if (Array.isArray(v)) return v.length ? v.join('、') : '-'
  if (typeof v === 'boolean') return v ? '是' : '否'
  return String(v)
}
