import axios from 'axios'
import { ElNotification , ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { tansParams, blobValidate } from '@/utils/ruoyi'
import cache from '@/plugins/cache'
import { saveAs } from 'file-saver'
import useUserStore from '@/store/modules/user'
import router from '@/router'

let downloadLoadingInstance
// 是否显示重新登录
export let isRelogin = { show: false }

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // 超时
  timeout: 10000
})

// request拦截器
service.interceptors.request.use(config => {
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
  // 是否需要防止数据重复提交
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
  // 间隔时间(ms)，小于此时间视为重复提交
  const interval = (config.headers || {}).interval || 1000
  if (getToken() && !isToken) {
    config.headers['Authorization'] = 'Bearer ' + getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
  }
  // get请求映射params参数
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.params = {}
    config.url = url
  }
  if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
    const requestObj = {
      url: config.url,
      data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
      time: new Date().getTime()
    }
    const requestSize = Object.keys(JSON.stringify(requestObj)).length // 请求数据大小
    const limitSize = 5 * 1024 * 1024 // 限制存放数据5M
    if (requestSize >= limitSize) {
      console.warn(`[${config.url}]: ` + '请求数据大小超出允许的5M限制，无法进行防重复提交验证。')
      return config
    }
    const sessionObj = cache.session.getJSON('sessionObj')
    if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
      cache.session.setJSON('sessionObj', requestObj)
    } else {
      const s_url = sessionObj.url                // 请求地址
      const s_data = sessionObj.data              // 请求数据
      const s_time = sessionObj.time              // 请求时间
      if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
        const message = '数据正在处理，请勿重复提交'
        console.warn(`[${s_url}]: ` + message)
        return Promise.reject(new Error(message))
      } else {
        cache.session.setJSON('sessionObj', requestObj)
      }
    }
  }
  return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(res => {
    // 未设置状态码则默认成功状态
    const code = res.data.code || 200
    // 获取错误信息
    const msg = errorCode[code] || res.data.msg || errorCode['default']
    // 二进制数据则直接返回（Chrome 兼容：当后端返回 JSON 错误时需特殊处理）
    if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
      // Chrome 兼容：当后端返回 JSON 错误（如 401/500）时，Content-Type 为 application/json
      // 需在此处判断，避免将错误信息当作文件下载
      const contentType = res.headers['content-type'] || ''
      if (contentType.includes('application/json')) {
        // 后端返回了 JSON，说明是错误信息，转为文本解析
        const blob = res.request.responseType === 'arraybuffer' ? new Blob([res.data]) : res.data
        return blob.text().then(text => {
          let rspObj
          try { rspObj = JSON.parse(text) } catch { rspObj = { msg: '下载失败' } }
          const errCode = rspObj.code || 500
          const errMsg = errorCode[errCode] || rspObj.msg || errorCode['default']
          if (errCode === 401) {
            if (!isRelogin.show) {
              isRelogin.show = true
              ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', { confirmButtonText: '重新登录', cancelButtonText: '取消', type: 'warning' }).then(() => {
                isRelogin.show = false
                // 获取当前路由路径作为 redirect 参数
                const currentPath = router.currentRoute.value.fullPath
                useUserStore().logOut().then(() => {
                  // 使用 Vue Router 跳转，保留当前页面路径作为 redirect 参数
                  router.push({ path: '/login', query: { redirect: currentPath } })
                })
              }).catch(() => {
                isRelogin.show = false
              })
            }
            return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
          }
          ElMessage.error(errMsg)
          return Promise.reject(new Error(errMsg))
        })
      }
      return res.data
    }
    if (code === 401) {
      if (!isRelogin.show) {
        isRelogin.show = true
        ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', { confirmButtonText: '重新登录', cancelButtonText: '取消', type: 'warning' }).then(() => {
          isRelogin.show = false
          // 获取当前路由路径作为 redirect 参数
          const currentPath = router.currentRoute.value.fullPath
          useUserStore().logOut().then(() => {
            // 使用 Vue Router 跳转，保留当前页面路径作为 redirect 参数
            router.push({ path: '/login', query: { redirect: currentPath } })
          })
        }).catch(() => {
          isRelogin.show = false
        })
      }
      return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
    } else if (code === 500) {
      // suppressError: 非关键请求（如列宽配置、状态计数）失败时不弹错误提示
      if (!res.config?.suppressError) {
        ElMessage({ message: msg, type: 'error' })
      }
      return Promise.reject(new Error(msg))
    } else if (code === 601) {
      if (!res.config?.suppressError) {
        ElMessage({ message: msg, type: 'warning' })
      }
      return Promise.reject(new Error(msg))
    } else if (code !== 200) {
      ElNotification.error({ title: msg })
      return Promise.reject('error')
    } else {
      return  Promise.resolve(res.data)
    }
  },
  error => {
    console.log('err' + error)
    // suppressError: 非关键请求失败时不弹错误提示
    if (!error.config?.suppressError) {
      let { message } = error
      if (message == "Network Error") {
        message = "后端接口连接异常"
      } else if (message.includes("timeout")) {
        message = "系统接口请求超时"
      } else if (message.includes("Request failed with status code")) {
        message = "系统接口" + message.slice(-3) + "异常"
      }
      ElMessage({ message: message, type: 'error', duration: 5 * 1000 })
    }
    return Promise.reject(error)
  }
)

// Chrome 兼容下载核心方法
// 问题根因：Chrome 的用户手势安全策略会阻止异步回调中的某些下载操作。
// 经过多次测试，最终确认可靠方案：
//   使用 document.createElement('a') + link.download + link.click() 方式。
//   关键点：
//     1. href 必须是 blob: 同源 URL（不是跨域 URL）
//     2. 必须设置 download 属性
//     3. <a> 标签必须 append 到 document.body 后再 click
//     4. click 后延迟移除 <a> 标签和释放 blob URL
//   满足以上条件时，Chrome 允许在异步回调中触发下载。
//   注意：window.location.href = blobUrl 方式不可用，会导致 SPA 页面被导航走。
function saveBlobWithIframe(blob, filename) {
  const blobUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = blobUrl
  link.download = filename
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  // 延迟清理，确保下载已触发
  setTimeout(() => {
    document.body.removeChild(link)
    URL.revokeObjectURL(blobUrl)
  }, 200)
}

// 根据文件扩展名获取 MIME 类型（不硬编码，支持多种文件格式）
function getMimeType(filename) {
  if (!filename) return 'application/octet-stream'
  const ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
  const mimeMap = {
    'xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'xls': 'application/vnd.ms-excel',
    'csv': 'text/csv',
    'pdf': 'application/pdf',
    'zip': 'application/zip',
    'doc': 'application/msword',
    'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'txt': 'text/plain',
    'png': 'image/png',
    'jpg': 'image/jpeg',
    'jpeg': 'image/jpeg',
    'gif': 'image/gif'
  }
  return mimeMap[ext] || 'application/octet-stream'
}

// 通用下载方法（兼容 Chrome/Firefox/Edge/Safari）
// Chrome 兼容要点：
//   1. 不硬编码 MIME 类型，根据文件扩展名动态判断
//   2. 增加超时时间到 5 分钟，避免大文件导出超时
//   3. 使用隐藏 iframe 方式触发下载，不受 Chrome 用户手势安全策略限制
//      （file-saver 的 saveAs 和 link.click() 在异步回调中都会被 Chrome 阻止）
export function download(url, params, filename, config) {
  downloadLoadingInstance = ElLoading.service({ text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)" })
  return service.post(url, params, {
    transformRequest: [(params) => { return tansParams(params) }],
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
    timeout: 300000, // 下载/导出超时时间设为 5 分钟，避免大文件导出超时
    ...config
  }).then(async (data) => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      // 根据文件扩展名推断 MIME 类型，不硬编码为 Excel
      const mimeType = getMimeType(filename)
      const blob = data.type ? data : new Blob([data], { type: mimeType })
      // 使用隐藏 iframe 方式下载，Chrome 兼容
      saveBlobWithIframe(blob, filename)
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
      ElMessage.error(errMsg)
    }
    downloadLoadingInstance.close()
  }).catch((r) => {
    console.error(r)
    ElMessage.error('下载文件出现错误，请联系管理员！')
    downloadLoadingInstance.close()
  })
}

export default service
