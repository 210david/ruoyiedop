/**
 * 人民币金额大写转换（前端统一工具）
 * 流程表单「单元格格式 → 数字 → 人民币大写」分类使用：
 * 发起页可编辑回显、详情/预览只读回显、FlowPrintView 打印模板共用本函数。
 * 库里仍存数字原值，大写仅是展示层格式（思路同 Excel [DBNum2]）。
 */

const DIG = '零壹贰叁肆伍陆柒捌玖'
const UNIT = ['', '拾', '佰', '仟']
const BIG = ['', '万', '亿', '兆']

/** 4 位一段 → 中文大写（段内零的处理） */
function segUpper(s) {
  let out = ''
  let zero = false
  for (let i = 0; i < s.length; i++) {
    const d = +s[i]
    const pos = s.length - 1 - i
    if (d === 0) {
      zero = !!out
    } else {
      if (zero) { out += '零'; zero = false }
      out += DIG[d] + UNIT[pos]
    }
  }
  return out
}

/** 金额数字 → 中文大写金额，如 123456.78 → 壹拾贰万叁仟肆佰伍拾陆圆柒角捌分 */
export function toRmbUpper(n) {
  const num = Number(n)
  if (n === '' || n === null || n === undefined || !isFinite(num)) return ''
  if (num === 0) return '零圆整'
  const neg = num < 0 ? '负' : ''
  const [intStr, decStr = ''] = Math.abs(num).toFixed(2).split('.')

  // 整数部分按 4 位分段（万/亿/兆）
  const parts = []
  let s = intStr
  while (s.length > 4) { parts.unshift(s.slice(-4)); s = s.slice(0, -4) }
  parts.unshift(s)

  let out = ''
  for (let i = 0; i < parts.length; i++) {
    const seg = segUpper(parts[i])
    if (!seg) continue
    // 本段数值不足 1000 且前面已有段：跨段补零（如 100000010 → 壹亿零壹拾）
    if (out && +parts[i] < 1000) out += '零'
    out += seg + BIG[parts.length - 1 - i]
  }

  const jiao = +(decStr[0] || 0)
  const fen = +(decStr[1] || 0)
  // 整数为 0 时省略「圆」：0.05 → 伍分、0.5 → 伍角
  if (intStr === '0') {
    if (!jiao && !fen) return neg + '零圆整'
    if (jiao) out += DIG[jiao] + '角'
    else if (fen) out += '零'
    if (fen) out += DIG[fen] + '分'
    return neg + out
  }
  out += '圆'
  if (!jiao && !fen) return neg + out + '整'
  if (jiao) out += DIG[jiao] + '角'
  else if (fen) out += '零'
  if (fen) out += DIG[fen] + '分'
  return neg + out
}
