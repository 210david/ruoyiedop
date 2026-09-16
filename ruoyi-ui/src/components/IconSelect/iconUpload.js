import request from '@/utils/request'
import { getToken } from '@/utils/auth'

/**
 * 上传自定义 SVG 图标到服务器
 * 后端将 SVG 文件保存到 src/assets/icons/svg/ 目录
 * 同时返回 SVG 内容用于前端动态注册
 */
export function uploadIcon(file) {
   const formData = new FormData()
   formData.append('file', file)
   return request({
      url: '/system/menu/icon/upload',
      method: 'post',
      data: formData,
      timeout: 30000,
      headers: {
         'Content-Type': false,
         'repeatSubmit': false
      }
   })
}
