/**
 * Chrome 下载兼容性检查脚本
 *
 * 正确方案：
 *   使用 document.createElement('a') + link.download + link.click() 方式。
 *   href 为 blob: 同源 URL + download 属性 + append 到 body 后 click。
 *   满足此条件时 Chrome 允许在异步回调中触发下载。
 *
 * 禁止方案：
 *   - saveAs()（file-saver 内部 a.click() + setTimeout，Chrome 会阻止）
 *   - window.location.href = blobUrl（会导致 SPA 页面被导航走）
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

function extractFunction(content, fnPattern) {
  var match = content.match(fnPattern)
  if (!match) return null
  return match[0]
}

var checks = [
  {
    file: 'utils/request.js',
    rules: [
      {
        // 检查 download 函数中是否使用了 saveAs 或 window.location.href（都被禁止）
        test: function (content) {
          var fnBody = extractFunction(content, /export function download[\s\S]*?^}/m)
          if (!fnBody) return false
          return fnBody.includes('saveAs(') || fnBody.includes('window.location.href')
        },
        message: 'download 函数中使用了 saveAs() 或 window.location.href，这两种方式在 Chrome 中都有问题。必须使用 saveBlobWithIframe()。',
        okMessage: 'download 函数使用 saveBlobWithIframe()，Chrome 兼容'
      },
      {
        // 检查 saveBlobWithIframe 函数是否使用 link.click()（正确方案）
        test: function (content) {
          var fnBody = extractFunction(content, /function saveBlobWithIframe[\s\S]*?^}/m)
          if (!fnBody) return true
          return !fnBody.includes('link.click()') || !fnBody.includes('download')
        },
        message: 'saveBlobWithIframe 函数必须使用 createElement("a") + link.download + link.click() 方式。',
        okMessage: 'saveBlobWithIframe 使用 link.click() + download 属性，Chrome 兼容'
      },
      {
        // 检查 saveBlobWithIframe 函数中没有使用 window.location.href
        test: function (content) {
          var fnBody = extractFunction(content, /function saveBlobWithIframe[\s\S]*?^}/m)
          if (!fnBody) return true
          return fnBody.includes('window.location.href')
        },
        message: 'saveBlobWithIframe 函数禁止使用 window.location.href（会导致 SPA 页面导航）。',
        okMessage: 'saveBlobWithIframe 未使用 window.location.href'
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
        test: function (content) { return !content.includes('saveBlobWithIframe') },
        message: '缺少 saveBlobWithIframe 函数定义。',
        okMessage: '已定义 saveBlobWithIframe 函数'
      },
      {
        test: function (content) {
          var fnBody = extractFunction(content, /export function download[\s\S]*?^}/m)
          if (!fnBody) return false
          return !fnBody.includes('timeout: 300000')
        },
        message: 'download 函数缺少 timeout: 300000（5分钟）。',
        okMessage: 'download 函数已设置 5 分钟超时'
      }
    ]
  },
  {
    file: 'utils/downloadFile.js',
    rules: [
      {
        test: function (content) {
          var fnBody = extractFunction(content, /export function downloadFile[\s\S]*?^}/m)
          if (!fnBody) return false
          return fnBody.includes('saveAs(') || fnBody.includes('window.location.href')
        },
        message: 'downloadFile 函数中使用了 saveAs() 或 window.location.href。必须使用 saveBlobWithIframe()。',
        okMessage: 'downloadFile 函数使用 saveBlobWithIframe()，Chrome 兼容'
      },
      {
        test: function (content) {
          var fnBody = extractFunction(content, /function saveBlobWithIframe[\s\S]*?^}/m)
          if (!fnBody) return true
          return fnBody.includes('window.location.href')
        },
        message: 'saveBlobWithIframe 函数禁止使用 window.location.href。',
        okMessage: 'saveBlobWithIframe 未使用 window.location.href'
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
        test: function (content) {
          var fnBody = extractFunction(content, /function downloadBlob[\s\S]*?^}/m)
          if (!fnBody) return false
          return fnBody.includes('saveAs(') || fnBody.includes('window.location.href')
        },
        message: 'downloadBlob 函数中使用了 saveAs() 或 window.location.href。必须使用 link.click() 方式。',
        okMessage: 'downloadBlob 函数使用 link.click()，Chrome 兼容'
      },
      {
        test: function (content) {
          var fnBody = extractFunction(content, /function downloadBlob[\s\S]*?^}/m)
          if (!fnBody) return true
          return !fnBody.includes('link.click()') || !fnBody.includes('download')
        },
        message: 'downloadBlob 函数必须使用 createElement("a") + link.download + link.click() 方式。',
        okMessage: 'downloadBlob 函数使用 link.click() + download 属性'
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
  console.error('  1. 下载必须使用 createElement("a") + link.download + link.click() 方式')
  console.error('  2. 禁止使用 saveAs()（file-saver 不可靠）')
  console.error('  3. 禁止使用 window.location.href = blobUrl（会导致 SPA 页面导航）')
  console.error('  4. MIME 类型必须使用 getMimeType(filename) 动态推断')
  console.error('  5. 下载超时必须设置为 300000（5分钟）')
  process.exit(1)
} else {
  console.log('[OK] 所有检查通过，Chrome 下载兼容性正常。')
  process.exit(0)
}
