import axios from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { blobValidate } from '@/utils/ruoyi'
import { saveAs } from 'file-saver'

const baseURL = import.meta.env.VITE_APP_BASE_API
let downloadLoadingInstance

/**
 * 隐藏 iframe 方式下载 Blob（Chrome 兼容核心方法）
 * Chrome 的用户手势安全策略会阻止异步回调中的 link.click() 和 file-saver 的 saveAs()，
 * 导致"只有下载任务，没有实际文件下载"。
 * 隐藏 iframe 方式通过设置 src 触发浏览器原生下载，不受用户手势限制。
 */
function downloadBlob(blob, filename, mimeType = 'application/octet-stream') {
  const finalBlob = blob.type ? blob : new Blob([blob], { type: mimeType })
  const blobUrl = URL.createObjectURL(finalBlob)
  const iframe = document.createElement('iframe')
  iframe.style.display = 'none'
  document.body.appendChild(iframe)
  // 在 iframe 内部创建 <a> 标签触发下载，保留文件名
  const doc = iframe.contentDocument || iframe.contentWindow.document
  const link = doc.createElement('a')
  link.href = blobUrl
  link.download = filename
  doc.body.appendChild(link)
  link.click()
  setTimeout(() => {
    document.body.removeChild(iframe)
    URL.revokeObjectURL(blobUrl)
  }, 1000)
}

export default {
  name(name, isDelete = true) {
    if (!name) {
      ElMessage.warning('下载文件名为空')
      return
    }
    var url = baseURL + "/common/download?fileName=" + encodeURIComponent(name) + "&delete=" + isDelete
    downloadLoadingInstance = ElLoading.service({ text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)" })
    axios({
      method: 'get',
      url: url,
      responseType: 'blob',
      timeout: 300000, // 下载超时设为 5 分钟
      headers: { 'Authorization': 'Bearer ' + getToken() }
    }).then((res) => {
      const isBlob = blobValidate(res.data)
      if (isBlob) {
        const filename = decodeURIComponent(res.headers['download-filename'] || name)
        downloadBlob(res.data, filename)
      } else {
        this.printErrMsg(res.data)
      }
      downloadLoadingInstance.close()
    }).catch((r) => {
      console.error('下载失败:', r)
      ElMessage.error('下载文件出现错误，请联系管理员！')
      downloadLoadingInstance.close()
    })
  },
  resource(resource) {
    if (!resource) {
      ElMessage.warning('下载地址为空')
      return
    }
    var url = baseURL + "/common/download/resource?resource=" + encodeURIComponent(resource)
    downloadLoadingInstance = ElLoading.service({ text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)" })
    axios({
      method: 'get',
      url: url,
      responseType: 'blob',
      timeout: 300000, // 下载超时设为 5 分钟
      headers: { 'Authorization': 'Bearer ' + getToken() }
    }).then((res) => {
      const isBlob = blobValidate(res.data)
      if (isBlob) {
        const filename = decodeURIComponent(res.headers['download-filename'] || 'resource')
        downloadBlob(res.data, filename)
      } else {
        this.printErrMsg(res.data)
      }
      downloadLoadingInstance.close()
    }).catch((r) => {
      console.error('下载失败:', r)
      ElMessage.error('下载文件出现错误，请联系管理员！')
      downloadLoadingInstance.close()
    })
  },
  zip(url, name) {
    var url = baseURL + url
    downloadLoadingInstance = ElLoading.service({ text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)", })
    axios({
      method: 'get',
      url: url,
      responseType: 'blob',
      timeout: 300000, // 下载超时设为 5 分钟
      headers: { 'Authorization': 'Bearer ' + getToken() }
    }).then((res) => {
      const isBlob = blobValidate(res.data)
      if (isBlob) {
        downloadBlob(res.data, name, 'application/zip')
      } else {
        this.printErrMsg(res.data)
      }
      downloadLoadingInstance.close()
    }).catch((r) => {
      console.error(r)
      ElMessage.error('下载文件出现错误，请联系管理员！')
      downloadLoadingInstance.close()
    })
  },
  saveAs(blob, name, mimeType) {
    downloadBlob(blob, name, mimeType)
  },
  async printErrMsg(data) {
    const resText = await data.text()
    const rspObj = JSON.parse(resText)
    const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
    ElMessage.error(errMsg)
  }
}
