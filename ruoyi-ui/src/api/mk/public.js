/**
 * 营销活动公开接口 API（P3-15: 报名链接）
 *
 * 安全说明：
 * - 使用独立的axios实例，不携带JWT Token
 * - 所有请求均需携带安全令牌(token)
 * - 报名接口有IP限流保护
 */
import axios from 'axios'

// 创建公开接口专用axios实例（不携带JWT Token）
const publicService = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000
})

// 响应拦截器
publicService.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(res)
    }
    return res
  },
  error => {
    let message = '网络异常，请稍后重试'
    if (error.response && error.response.data && error.response.data.msg) {
      message = error.response.data.msg
    } else if (error.message) {
      message = error.message
    }
    return Promise.reject({ msg: message })
  }
)

// 获取活动报名信息（公开接口）
export function getPublicActivityInfo(activityId, token) {
  return publicService({
    url: '/mk/public/activity/' + activityId,
    method: 'get',
    params: { token }
  })
}

// 提交报名（公开接口）
export function submitRegistration(data, token) {
  return publicService({
    url: '/mk/public/register',
    method: 'post',
    params: { token },
    data: data
  })
}

// 签到码签到（公开接口）
export function signInByCode(signInCode) {
  return publicService({
    url: '/mk/public/signIn',
    method: 'post',
    params: { signInCode }
  })
}
