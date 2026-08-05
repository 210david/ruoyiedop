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
 * 使用 file-saver 库处理下载，避免 Chrome 的 user gesture 安全限制
 * @param {Blob} blob - 文件 Blob 对象
 * @param {string} filename - 下载文件名
 * @param {string} mimeType - MIME 类型
 */
function downloadBlob(blob, filename, mimeType = 'application/octet-stream') {
  const finalBlob = blob.type ? blob : new Blob([blob], { type: mimeType })
  saveAs(finalBlob, filename)
}

export default {
  name(name, isDelete = true) {
    var url = baseURL + "/common/download?fileName=" + encodeURIComponent(name) + "&delete=" + isDelete
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
    })
  },
  resource(resource) {
    var url = baseURL + "/common/download/resource?resource=" + encodeURIComponent(resource)
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
