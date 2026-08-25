import { defineStore } from 'pinia'
import { listMessageTop, markMessageRead, markMessageReadAll } from '@/api/system/message'

/**
 * 全局消息状态管理
 * 跨组件共享：未读数、顶部消息列表
 * 当任何地方标记已读后，通过 store 统一刷新，确保铃铛和消息中心页面实时同步
 */
export const useMessageStore = defineStore('message', {
  state: () => ({
    /** 未读消息数 */
    unreadCount: 0,
    /** 顶部消息列表（铃铛弹窗用） */
    noticeList: [],
    /** 是否正在加载 */
    loading: false,
    /** 最后一次刷新时间戳 */
    lastRefreshAt: 0
  }),
  actions: {
    /**
     * 加载顶部消息列表 + 未读数（从后端拉取）
     * HeaderNotice onMounted 时调用，以及标记已读后调用
     */
    async loadNoticeTop() {
      this.loading = true
      try {
        const res = await listMessageTop()
        this.noticeList = res.data || []
        this.unreadCount = res.unreadCount !== undefined
          ? res.unreadCount
          : this.noticeList.filter(n => !n.isRead).length
      } catch (e) {
        this.noticeList = []
        this.unreadCount = 0
      } finally {
        this.loading = false
        this.lastRefreshAt = Date.now()
      }
    },

    /**
     * 标记单条消息为已读
     * 同时更新本地状态（乐观更新），然后异步刷新后端数据
     * @param {number} messageId
     */
    async markRead(messageId) {
      // 乐观更新：先本地减少未读数
      const item = this.noticeList.find(n => n.messageId === messageId)
      if (item && !item.isRead) {
        item.isRead = true
        this.unreadCount = Math.max(0, this.unreadCount - 1)
      }
      // 调用后端
      try {
        await markMessageRead(messageId)
      } catch (e) {
        // 失败则重新加载确保一致性
        await this.loadNoticeTop()
      }
    },

    /**
     * 批量标记已读
     * @param {string} ids 逗号分隔的ID字符串
     */
    async markReadAll(ids) {
      // 乐观更新
      const idArr = String(ids).split(',').map(Number)
      let changed = 0
      this.noticeList.forEach(n => {
        if (idArr.includes(n.messageId) && !n.isRead) {
          n.isRead = true
          changed++
        }
      })
      this.unreadCount = Math.max(0, this.unreadCount - changed)
      try {
        await markMessageReadAll(ids)
      } catch (e) {
        await this.loadNoticeTop()
      }
    },

    /**
     * 全部已读（铃铛弹窗中的"全部已读"按钮）
     */
    async markAllRead() {
      const ids = this.noticeList.filter(n => !n.isRead).map(n => n.messageId).join(',')
      if (!ids) return
      // 乐观更新
      this.noticeList.forEach(n => { n.isRead = true })
      this.unreadCount = 0
      try {
        await markMessageReadAll(ids)
      } catch (e) {
        await this.loadNoticeTop()
      }
    }
  }
})
