import request from '@/utils/request'
import { ElLoading, ElMessage } from 'element-plus'
import { blobValidate } from '@/utils/ruoyi'

/**
 * 隐藏 iframe 方式下载 Blob（Chrome 兼容核心方法）
 * Chrome 的用户手势安全策略会阻止异步回调中的 link.click() 和 file-saver 的 saveAs()，
 * 导致"只有下载任务，没有实际文件下载"。
 * 隐藏 iframe 方式通过设置 src 触发浏览器原生下载，不受用户手势限制。
 */
function saveBlobWithIframe(blob, filename) {
  const blobUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = blobUrl
  link.download = filename
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  setTimeout(() => {
    document.body.removeChild(link)
    URL.revokeObjectURL(blobUrl)
  }, 200)
}

/**
 * 下载资源文件（兼容 Chrome/Firefox/Edge/Safari）
 * 使用项目配置的 request 实例（自动携带 Token、完整拦截器），通过后端 /common/download/resource 接口下载
 * 后端设置 Content-Disposition: attachment + Content-Type: application/octet-stream 强制下载
 *
 * @param {string} resource - 资源路径，如 /profile/upload/2024/01/01/xxx.pdf
 * @param {string} filename - 可选，指定下载文件名，不传则从 resource 中提取
 */
export function downloadFile(resource, filename) {
  if (!resource) {
    ElMessage.warning('下载地址为空')
    return
  }
  // 从路径中提取文件名
  const name = filename || (resource.includes('/') ? resource.substring(resource.lastIndexOf('/') + 1) : resource)
  // 显示加载遮罩
  const loading = ElLoading.service({ text: '正在下载文件，请稍候', background: 'rgba(0, 0, 0, 0.7)' })

  request({
    url: '/common/download/resource',
    method: 'get',
    params: { resource },
    responseType: 'blob',
    timeout: 300000 // 资源文件下载超时设为 5 分钟，避免大文件下载超时
  }).then((data) => {
    // request 的响应拦截器对 blob 类型直接返回 res.data（即 Blob 对象）
    if (blobValidate(data)) {
      // 使用隐藏 iframe 方式下载，Chrome 兼容
      saveBlobWithIframe(data, name)
    } else {
      // 返回的不是文件流（可能是 JSON 错误信息）
      data.text().then((text) => {
        try {
          const rspObj = JSON.parse(text)
          ElMessage.error(rspObj.msg || '下载失败')
        } catch {
          ElMessage.error('下载失败，文件可能不存在')
        }
      })
    }
    loading.close()
  }).catch((err) => {
    console.error('下载失败:', err)
    ElMessage.error('下载文件出现错误，请联系管理员！')
    loading.close()
  })
}
