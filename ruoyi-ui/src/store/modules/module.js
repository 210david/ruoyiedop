import { defineStore } from 'pinia'
import { getEnabledModules } from '@/api/system/module'

const useModuleStore = defineStore(
  'module',
  {
    state: () => ({
      /** 启用模块标识列表（登录后首次访问时懒加载） */
      enabledKeys: [],
      /** 是否已完成加载 */
      loaded: false
    }),
    actions: {
      /** 加载启用模块列表（会话内仅请求一次，失败不缓存以便下次重试） */
      loadEnabled() {
        if (this.loaded) {
          return Promise.resolve(this.enabledKeys)
        }
        return getEnabledModules().then(res => {
          this.enabledKeys = res.data || []
          this.loaded = true
          return this.enabledKeys
        }).catch(() => this.enabledKeys)
      },
      /** 判断模块是否启用（未加载完成前默认视为启用，仅在确认停用后返回false） */
      isEnabled(key) {
        this.loadEnabled()
        return this.loaded ? this.enabledKeys.includes(key) : true
      }
    }
  })

export default useModuleStore
