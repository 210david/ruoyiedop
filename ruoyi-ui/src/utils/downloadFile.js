import request from '@/utils/request'
import { ElLoading, ElMessage } from 'element-plus'
import { blobValidate } from '@/utils/ruoyi'

/**
 * 下载资源文件（兼容 Chrome/Firefox/Edge/Safari）
 * 使用项目配置的 request 实例（自动携带 Token、完整拦截器），通过后端 /common/download/resource 接口下载
 * 后端设置 Content-Disposition: attachment + Content-Type: application/octet-stream 强制下载
 * 前端使用 Blob URL + <a download> 触发保存，blob: 为同源 URL，Chrome 不会忽略 download 属性
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
    responseType: 'blob'
  }).then((data) => {
    // request 的响应拦截器对 blob 类型直接返回 res.data（即 Blob 对象）
    if (blobValidate(data)) {
      // 创建同源 Blob URL，Chrome 不会忽略 <a download> 属性
      const blobUrl = URL.createObjectURL(data)
      const link = document.createElement('a')
      link.href = blobUrl
      link.download = name
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      // 延迟释放 Blob URL，确保下载已触发
      setTimeout(() => URL.revokeObjectURL(blobUrl), 200)
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
