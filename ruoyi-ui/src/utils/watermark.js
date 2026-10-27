/**
 * 页面水印工具
 * 根据系统参数 sys.index.watermarkEnabled 控制是否显示水印
 * 水印内容为当前登录用户名 + 时间，覆盖整个页面
 * 修改参数后调用 refreshWatermark() 立即生效，无需重启或刷新浏览器
 */
import useUserStore from '@/store/modules/user'

let watermarkEl = null
let observer = null

/** 创建水印 canvas 并返回 base64 图片 */
function createWatermarkCanvas(text) {
  const canvas = document.createElement('canvas')
  canvas.width = 300
  canvas.height = 200
  const ctx = canvas.getContext('2d')
  // 水印颜色：深灰色半透明，清晰但不过于突兀
  ctx.fillStyle = 'rgba(0, 0, 0, 0.08)'
  ctx.font = '16px "Microsoft YaHei", "PingFang SC", Arial, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  // 旋转 -25 度
  ctx.translate(canvas.width / 2, canvas.height / 2)
  ctx.rotate(-25 * Math.PI / 180)
  // 绘制用户名
  ctx.fillText(text, 0, -10)
  // 绘制时间
  const now = new Date()
  const timeStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  ctx.font = '13px "Microsoft YaHei", "PingFang SC", Arial, sans-serif'
  ctx.fillText(timeStr, 0, 15)
  return canvas.toDataURL('image/png')
}

/** 显示水印 */
function showWatermark() {
  // 先移除已有水印，确保不会重复
  if (watermarkEl) {
    hideWatermark()
  }
  const userStore = useUserStore()
  const userName = userStore.name || userStore.nickName || 'unknown'
  const dataUrl = createWatermarkCanvas(userName)
  watermarkEl = document.createElement('div')
  watermarkEl.style.position = 'fixed'
  watermarkEl.style.top = '0'
  watermarkEl.style.left = '0'
  watermarkEl.style.width = '100%'
  watermarkEl.style.height = '100%'
  watermarkEl.style.pointerEvents = 'none'
  watermarkEl.style.zIndex = '9999'
  watermarkEl.style.backgroundImage = `url(${dataUrl})`
  watermarkEl.style.backgroundRepeat = 'repeat'
  watermarkEl.dataset.watermark = 'true'
  document.body.appendChild(watermarkEl)
  // 使用 MutationObserver 防止水印被手动删除
  observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      // 水印被删除时重新添加
      if (mutation.type === 'childList') {
        const removed = Array.from(mutation.removedNodes)
        if (removed.includes(watermarkEl)) {
          document.body.appendChild(watermarkEl)
        }
      }
      // 水印样式被修改时恢复
      if (mutation.type === 'attributes' && mutation.target === watermarkEl) {
        watermarkEl.style.display = 'block'
        watermarkEl.style.opacity = '1'
        watermarkEl.style.visibility = 'visible'
      }
    }
  })
  observer.observe(document.body, { childList: true, subtree: false })
  observer.observe(watermarkEl, { attributes: true })
}

/** 隐藏水印 */
function hideWatermark() {
  if (observer) {
    observer.disconnect()
    observer = null
  }
  if (watermarkEl && watermarkEl.parentNode) {
    watermarkEl.parentNode.removeChild(watermarkEl)
  }
  watermarkEl = null
}

/**
 * 根据系统参数初始化水印
 * 在用户登录后、Layout组件挂载时调用
 * 每次调用都会重新从后端获取最新参数值，确保修改后立即生效
 */
export async function initWatermark() {
  try {
    const { getConfigKey } = await import('@/api/system/config')
    const res = await getConfigKey('sys.index.watermarkEnabled')
    // AjaxResult.success(Object data) 返回 {code:200, msg:'操作成功', data: 参数值}
    // 兼容两种取值方式
    const val = res.data !== undefined ? res.data : res.msg
    const enabled = val === 'true' || val === true
    if (enabled) {
      showWatermark()
    } else {
      hideWatermark()
    }
  } catch (e) {
    // 如果获取参数失败，不显示水印
    hideWatermark()
  }
}

/** 刷新水印（参数变更后调用，立即生效） */
export function refreshWatermark() {
  hideWatermark()
  return initWatermark()
}

/** 移除水印（退出登录时调用） */
export function removeWatermark() {
  hideWatermark()
}
