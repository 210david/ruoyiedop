/**
 * 轻量公式解析器
 *
 * 管理员输入自然公式，例如：round(amount * (1 + taxRate / 100), 2)
 * 解析为受限 AST：{ field } / { value } / { fn, args }
 * 运行时不使用 eval / new Function，前后端解释同一份 AST。
 */

const FUNC_ALIAS = {
  '+': 'add',
  '-': 'sub',
  '*': 'mul',
  '/': 'div',
  '%': 'mod'
}

function tokenize (text) {
  const tokens = []
  let i = 0
  const s = String(text || '')
  while (i < s.length) {
    const ch = s[i]
    if (/\s/.test(ch)) { i++; continue }
    if (/[0-9]/.test(ch) || (ch === '.' && /[0-9]/.test(s[i + 1]))) {
      let j = i
      while (j < s.length && /[0-9.]/.test(s[j])) j++
      tokens.push({ t: 'num', v: s.slice(i, j) })
      i = j
      continue
    }
    if (ch === '"' || ch === "'") {
      const quote = ch
      let j = i + 1
      let v = ''
      while (j < s.length && s[j] !== quote) {
        if (s[j] === '\\' && j + 1 < s.length) { v += s[j + 1]; j += 2; continue }
        v += s[j++]
      }
      if (j >= s.length) throw new Error('字符串缺少结束引号')
      tokens.push({ t: 'str', v })
      i = j + 1
      continue
    }
    if (/[A-Za-z_$]/.test(ch)) {
      let j = i
      while (j < s.length && /[A-Za-z0-9_$.]/.test(s[j])) j++
      const v = s.slice(i, j)
      if (v === 'true' || v === 'false') {
        tokens.push({ t: 'bool', v: v === 'true' })
      } else {
        tokens.push({ t: 'id', v })
      }
      i = j
      continue
    }
    if ('+-*/(),%'.includes(ch)) {
      tokens.push({ t: 'op', v: ch })
      i++
      continue
    }
    throw new Error(`不支持的字符：${ch}`)
  }
  return tokens
}

export function parseFormula (text) {
  const src = String(text || '').trim()
  if (!src) return { ast: null, error: '' }
  try {
    const tokens = tokenize(src)
    let pos = 0
    const peek = () => tokens[pos]
    const next = () => tokens[pos++]
    const expect = (v) => {
      const t = tokens[pos]
      if (!t || t.v !== v) throw new Error(`缺少 ${v}`)
      pos++
    }

    function parseExpr () {
      let left = parseTerm()
      while (peek() && peek().t === 'op' && (peek().v === '+' || peek().v === '-')) {
        const op = next().v
        left = { fn: FUNC_ALIAS[op], args: [left, parseTerm()] }
      }
      return left
    }
    function parseTerm () {
      let left = parseUnary()
      while (peek() && peek().t === 'op' && ['*', '/', '%'].includes(peek().v)) {
        const op = next().v
        left = { fn: FUNC_ALIAS[op], args: [left, parseUnary()] }
      }
      return left
    }
    function parseUnary () {
      const t = peek()
      if (t && t.t === 'op' && (t.v === '-' || t.v === '+')) {
        next()
        const node = parseUnary()
        return t.v === '-' ? { fn: 'sub', args: [{ value: 0 }, node] } : node
      }
      return parsePrimary()
    }
    function parsePrimary () {
      const t = next()
      if (!t) throw new Error('公式不完整')
      if (t.t === 'num') return { value: Number(t.v) }
      if (t.t === 'str') return { value: t.v }
      if (t.t === 'bool') return { value: t.v }
      if (t.t === 'id') {
        if (peek() && peek().t === 'op' && peek().v === '(') {
          next()
          const args = []
          if (peek() && peek().v !== ')') {
            args.push(parseExpr())
            while (peek() && peek().v === ',') {
              next()
              args.push(parseExpr())
            }
          }
          expect(')')
          return { fn: t.v, args }
        }
        return { field: t.v }
      }
      if (t.t === 'op' && t.v === '(') {
        const node = parseExpr()
        expect(')')
        return node
      }
      throw new Error(`无法识别的符号：${t.v}`)
    }

    const ast = parseExpr()
    if (pos < tokens.length) throw new Error(`存在多余内容：${tokens[pos].v}`)
    return { ast, error: '' }
  } catch (e) {
    return { ast: null, error: e.message }
  }
}

export function formatFormula (ast) {
  if (!ast) return ''
  if (Object.prototype.hasOwnProperty.call(ast, 'value')) {
    return typeof ast.value === 'string' ? JSON.stringify(ast.value) : String(ast.value)
  }
  if (Object.prototype.hasOwnProperty.call(ast, 'field')) return ast.field
  const args = (ast.args || []).map(formatFormula).join(', ')
  return `${ast.fn}(${args})`
}

export function collectFormulaFields (ast, out = new Set()) {
  if (!ast || typeof ast !== 'object') return out
  if (typeof ast.field === 'string') out.add(ast.field)
  if (Array.isArray(ast.args)) ast.args.forEach((a) => collectFormulaFields(a, out))
  return out
}
