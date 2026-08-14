/**
 * Chrome 下载兼容性检查脚本
 *
 * 问题背景：
 *   Chrome 的用户手势安全策略要求 link.click() 必须在用户手势的同步调用栈中执行。
 *   异步回调（axios .then()、setTimeout 等）中调用 link.click() 会被 Chrome 阻止，
 *   导致"只有下载任务，没有实际文件下载"。
 *   file-saver 的 saveAs() 内部也是用 a.click() + setTimeout，同样会被阻止。
 *   此问题在 Firefox/Edge 中不会出现，所以容易被忽视。
 *
 * 正确方案：
 *   使用 saveBlobWithIframe()（隐藏 iframe 方式）触发下载，不受用户手势限制。
 *   iframe 内部的 link.click() 是安全的，因为它在独立的文档上下文中执行。
 *
 * 使用方法：
 *   node scripts/check-download-compat.cjs
 *   或 npm run check:download
 *   建议在 git pre-commit hook 中调用此脚本。
 */

var fs = require('fs')
var path = require('path')

var ROOT = path.resolve(__dirname, '..')
var SRC = path.join(ROOT, 'src')

var hasError = false

function checkFile(filePath, rules) {
  var fullPath = path.join(SRC, filePath)
  if (!fs.existsSync(fullPath)) {
    console.error('[ERROR] 文件不存在: ' + filePath)
    hasError = true
    return
  }

  var rawContent = fs.readFileSync(fullPath, 'utf-8')
  // 移除注释行后再检查，避免注释中的关键词导致误报
  var content = rawContent.replace(/^\s*\/\/.*$/gm, '').replace(/^\s*\*.*$/gm, '')

  for (var i = 0; i < rules.length; i++) {
    var rule = rules[i]
    if (rule.test(content)) {
      console.error('[FAIL] ' + filePath + ': ' + rule.message)
      hasError = true
    } else {
      console.log('[PASS] ' + filePath + ': ' + rule.okMessage)
    }
  }
}

/**
 * 从文件内容中提取指定函数体（排除注释）
 * @param {string} content - 文件内容（已移除注释）
 * @param {string} fnPattern - 函数匹配正则字符串
 * @returns {string|null} 函数体文本
 */
function extractFunction(content, fnPattern) {
  var match = content.match(fnPattern)
  if (!match) return null
  return match[0]
}

/**
 * 检查函数体中是否有在主文档上下文（非 iframe 内部）使用 link.click() 或 saveAs()
 * iframe 内部的 link.click() 是安全的，不报错
 */
function hasUnsafeDownloadCall(fnBody) {
  if (!fnBody) return false
  // 如果函数体中有 link.click()，检查它是否在 iframe 内部
  if (fnBody.includes('link.click()')) {
    // 如果同时包含 iframe 和 doc.createElement('a')，说明 link.click() 在 iframe 内部
    if (fnBody.includes('iframe') && (fnBody.includes('doc.createElement') || fnBody.includes('doc.body'))) {
      return false // 安全：在 iframe 内部
    }
    return true // 不安全：在主文档上下文
  }
  // saveAs() 在任何地方都不允许（file-saver 内部用 a.click() + setTimeout）
  if (fnBody.includes('saveAs(')) {
    return true
  }
  return false
}

var checks = [
  {
    file: 'utils/request.js',
    rules: [
      {
        // 检查 download 函数中是否有不安全的下载调用
        test: function (content) {
          var fnBody = extractFunction(content, /export function download[\s\S]*?^}/m)
          return hasUnsafeDownloadCall(fnBody)
        },
        message: 'download 函数中使用了不安全的下载方式（主文档 link.click() 或 saveAs()）。必须使用 saveBlobWithIframe()。',
        okMessage: 'download 函数使用 saveBlobWithIframe()，Chrome 兼容'
      },
      {
        // 检查是否硬编码了 Excel MIME 类型
        test: function (content) {
          return content.includes("new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })")
        },
        message: 'download 函数中硬编码了 Excel MIME 类型，必须使用 getMimeType() 动态推断。',
        okMessage: 'download 函数使用 getMimeType() 动态推断 MIME 类型'
      },
      {
        // 检查是否有 saveBlobWithIframe 函数定义
        test: function (content) { return !content.includes('saveBlobWithIframe') },
        message: '缺少 saveBlobWithIframe 函数定义，这是 Chrome 兼容下载的核心方法。',
        okMessage: '已定义 saveBlobWithIframe 函数'
      },
      {
        // 检查 download 函数是否有 timeout: 300000
        test: function (content) {
          var fnBody = extractFunction(content, /export function download[\s\S]*?^}/m)
          if (!fnBody) return false
          return !fnBody.includes('timeout: 300000')
        },
        message: 'download 函数缺少 timeout: 300000（5分钟），大文件导出会超时。',
        okMessage: 'download 函数已设置 5 分钟超时'
      }
    ]
  },
  {
    file: 'utils/downloadFile.js',
    rules: [
      {
        // 检查 downloadFile 函数（排除 saveBlobWithIframe 函数体）中是否有不安全调用
        test: function (content) {
          // 提取 downloadFile 函数体
          var fnBody = extractFunction(content, /export function downloadFile[\s\S]*?^}/m)
          return hasUnsafeDownloadCall(fnBody)
        },
        message: 'downloadFile 函数中使用了不安全的下载方式。必须使用 saveBlobWithIframe()。',
        okMessage: 'downloadFile 函数使用 saveBlobWithIframe()，Chrome 兼容'
      },
      {
        test: function (content) { return !content.includes('saveBlobWithIframe') },
        message: '缺少 saveBlobWithIframe 函数定义。',
        okMessage: '已定义 saveBlobWithIframe 函数'
      }
    ]
  },
  {
    file: 'plugins/download.js',
    rules: [
      {
        // 检查 downloadBlob 函数中是否有不安全的下载调用
        test: function (content) {
          var fnBody = extractFunction(content, /function downloadBlob[\s\S]*?^}/m)
          // downloadBlob 本身就是实现 iframe 下载的函数，检查它是否用了 saveAs 或主文档 link.click()
          if (!fnBody) return false
          // 如果用了 saveAs 就不安全
          if (fnBody.includes('saveAs(')) return true
          // 如果有 link.click() 但没有 iframe，就不安全
          if (fnBody.includes('link.click()') && !fnBody.includes('iframe')) return true
          return false
        },
        message: 'downloadBlob 函数中使用了不安全的下载方式。必须使用隐藏 iframe 方式。',
        okMessage: 'downloadBlob 函数使用隐藏 iframe 方式，Chrome 兼容'
      },
      {
        test: function (content) { return !content.includes('iframe') },
        message: 'downloadBlob 函数缺少隐藏 iframe 方式实现。',
        okMessage: 'downloadBlob 函数已使用隐藏 iframe 方式'
      }
    ]
  }
]

console.log('=== Chrome 下载兼容性检查 ===\n')

for (var i = 0; i < checks.length; i++) {
  checkFile(checks[i].file, checks[i].rules)
}

console.log('')

if (hasError) {
  console.error('[X] 检查失败！存在 Chrome 下载兼容性问题，请修复后再提交。')
  console.error('')
  console.error('修复指南：')
  console.error('  1. 异步下载必须使用 saveBlobWithIframe()（隐藏 iframe 方式），禁止主文档 link.click() 和 saveAs()')
  console.error('  2. MIME 类型必须使用 getMimeType(filename) 动态推断，禁止硬编码')
  console.error('  3. 下载超时必须设置为 300000（5分钟）')
  console.error('  4. 参考开发规范文档 4.3.7 节「文件下载/导出浏览器兼容规范」')
  process.exit(1)
} else {
  console.log('[OK] 所有检查通过，Chrome 下载兼容性正常。')
  process.exit(0)
}
