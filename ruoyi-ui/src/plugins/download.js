import axios from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { blobValidate } from '@/utils/ruoyi'
import { saveAs } from 'file-saver'

const baseURL = import.meta.env.VITE_APP_BASE_API
let downloadLoadingInstance

/**
 * 通用文件下载方法（兼容 Chrome/Firefox/Edge/Safari）
 * 优先使用原生 <a download> 方式，失败时回退到 file-saver
 * @param {Blob} blob - 文件 Blob 对象
 * @param {string} filename - 下载文件名
 * @param {string} mimeType - MIME 类型
 */
function downloadBlob(blob, filename, mimeType = 'application/octet-stream') {
  const finalBlob = blob.type ? blob : new Blob([blob], { type: mimeType })
  // 方式1：原生 <a download> + blob URL（Chrome 最兼容）
  try {
    const blobUrl = URL.createObjectURL(finalBlob)
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = filename
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    // 延迟释放 blob URL，确保下载已触发
    setTimeout(() => URL.revokeObjectURL(blobUrl), 200)
    return
  } catch (e) {
    console.warn('原生下载失败，回退到 file-saver:', e)
  }
  // 方式2：回退到 file-saver
  try {
    saveAs(finalBlob, filename)
  } catch (e) {
    console.error('file-saver 下载也失败:', e)
    ElMessage.error('文件下载失败，请尝试右键另存为')
  }
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
