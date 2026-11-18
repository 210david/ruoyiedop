/**
 * 表单规则常量与结构定义
 *
 * 规则存储于 flow_form.ext_json：
 * { v: 1, rules: [...], scripts: [...] }
 */

export const RULE_TYPES = [
  { value: 'derive', label: '带出/计算', desc: '由其他字段计算或带出目标字段的值' },
  { value: 'required', label: '动态必填', desc: '满足条件时目标字段必填' },
  { value: 'visible', label: '显示/隐藏', desc: '满足条件时显示或隐藏目标字段' },
  { value: 'readonly', label: '只读/可编辑', desc: '满足条件时锁定或解锁目标字段' },
  { value: 'options', label: '动态选项', desc: '满足条件时替换目标字段的下拉选项' },
  { value: 'validate', label: '值校验', desc: '提交前校验，可拦截提交' },
  { value: 'script', label: '高级脚本', desc: '规则无法覆盖时使用 JavaScript（需权限）' }
]

export const COMPARE_OPS = [
  { value: 'eq', label: '等于' },
  { value: 'ne', label: '不等于' },
  { value: 'gt', label: '大于' },
  { value: 'gte', label: '大于等于' },
  { value: 'lt', label: '小于' },
  { value: 'lte', label: '小于等于' },
  { value: 'contains', label: '包含' },
  { value: 'notContains', label: '不包含' },
  { value: 'in', label: '属于（多值）' },
  { value: 'notIn', label: '不属于（多值）' },
  { value: 'between', label: '介于' },
  { value: 'isEmpty', label: '为空' },
  { value: 'isNotEmpty', label: '不为空' },
  { value: 'regex', label: '匹配正则' }
]

/** 比较时无需填写右值的操作符 */
export const UNARY_OPS = ['isEmpty', 'isNotEmpty']

export const VALIDATORS = [
  { value: 'required', label: '必填' },
  { value: 'range', label: '数值范围' },
  { value: 'regex', label: '正则匹配' },
  { value: 'crossField', label: '字段比较' },
  { value: 'detailSum', label: '明细合计' },
  { value: 'custom', label: '自定义表达式' },
  { value: 'unique', label: '唯一性（仅后端）' }
]

export const PHASES = [
  { value: 'load', label: '打开表单时' },
  { value: 'change', label: '字段变化时' },
  { value: 'submit', label: '提交前' }
]

/** 表达式可用函数（前后端一致） */
export const EXPR_FUNCTIONS = [
  { name: 'add', label: '加', usage: 'add(a, b)' },
  { name: 'sub', label: '减', usage: 'sub(a, b)' },
  { name: 'mul', label: '乘', usage: 'mul(a, b)' },
  { name: 'div', label: '除', usage: 'div(a, b)' },
  { name: 'mod', label: '取余', usage: 'mod(a, b)' },
  { name: 'round', label: '四舍五入', usage: 'round(n, 位数)' },
  { name: 'floor', label: '向下取整', usage: 'floor(n)' },
  { name: 'ceil', label: '向上取整', usage: 'ceil(n)' },
  { name: 'abs', label: '绝对值', usage: 'abs(n)' },
  { name: 'min', label: '最小值', usage: 'min(a, b, ...)' },
  { name: 'max', label: '最大值', usage: 'max(a, b, ...)' },
  { name: 'if', label: '条件', usage: 'if(条件, 真值, 假值)' },
  { name: 'concat', label: '拼接文本', usage: 'concat(a, b)' },
  { name: 'len', label: '文本长度', usage: 'len(text)' },
  { name: 'upper', label: '转大写', usage: 'upper(text)' },
  { name: 'lower', label: '转小写', usage: 'lower(text)' },
  { name: 'today', label: '今天', usage: 'today()' },
  { name: 'now', label: '当前时间', usage: 'now()' },
  { name: 'dateDiff', label: '日期相差天数', usage: 'dateDiff(开始, 结束)' },
  { name: 'dateAdd', label: '日期加天数', usage: 'dateAdd(日期, 天数)' },
  { name: 'sum', label: '求和', usage: 'sum(a, b, ...)' },
  { name: 'rmbUpper', label: '人民币大写', usage: 'rmbUpper(金额字段)' },
  { name: 'avg', label: '平均值', usage: 'avg(a, b, ...)' },
  { name: 'detailSum', label: '明细列合计', usage: 'detailSum(dt1, amount)' },
  { name: 'detailCount', label: '明细行数', usage: 'detailCount(dt1)' }
]

export function emptyExt () {
  return { v: 1, rules: [], scripts: [] }
}

export function parseExt (raw) {
  if (!raw) return emptyExt()
  let data = raw
  if (typeof raw === 'string') {
    try {
      data = JSON.parse(raw)
    } catch (e) {
      return emptyExt()
    }
  }
  if (!data || typeof data !== 'object') return emptyExt()
  return {
    v: data.v || 1,
    rules: Array.isArray(data.rules) ? data.rules : [],
    scripts: Array.isArray(data.scripts) ? data.scripts : []
  }
}

export function stringifyExt (ext) {
  const data = ext || emptyExt()
  if (!data.rules?.length && !data.scripts?.length) return ''
  return JSON.stringify({ v: 1, rules: data.rules || [], scripts: data.scripts || [] })
}

export function createRuleId () {
  return 'r_' + Math.random().toString(36).slice(2, 8) + Date.now().toString(36).slice(-3)
}

export function createScriptId () {
  return 'sc_' + Math.random().toString(36).slice(2, 8) + Date.now().toString(36).slice(-3)
}

/** 规则默认值 */
export function createRule (type) {
  const base = {
    id: createRuleId(),
    type,
    name: '',
    enabled: true,
    scope: 'main',
    order: 10,
    trigger: [],
    when: null
  }
  if (type === 'derive') {
    return { ...base, name: '新建计算规则', target: '', mode: 'always', expr: null, mapping: [] }
  }
  if (type === 'validate') {
    return {
      ...base,
      name: '新建校验规则',
      target: '',
      phase: ['submit'],
      message: '',
      level: 'error',
      action: { validator: 'required' }
    }
  }
  if (type === 'options') {
    return {
      ...base,
      name: '新建选项规则',
      target: '',
      action: { options: [] }
    }
  }
  if (type === 'visible') {
    return { ...base, name: '新建显隐规则', target: '', action: { visible: false } }
  }
  if (type === 'readonly') {
    return { ...base, name: '新建只读规则', target: '', action: { readonly: true } }
  }
  if (type === 'required') {
    return { ...base, name: '新建必填规则', target: '', action: { required: true }, message: '' }
  }
  if (type === 'script') {
    return {
      ...base,
      name: '新建高级脚本',
      phase: ['change'],
      fields: [],
      code: ''
    }
  }
  return base
}
