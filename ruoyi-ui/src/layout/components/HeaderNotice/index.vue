<template>
  <div>
    <el-popover ref="noticePopover" placement="bottom-end" :width="340" trigger="manual" v-model:visible="noticeVisible" popper-class="notice-popover">
      <!-- 弹出内容 -->
      <div class="notice-header">
        <span class="notice-title">消息中心</span>
        <div class="notice-header-actions">
          <span class="notice-mark-all" @click="handleMarkAllRead" v-if="messageStore.unreadCount > 0">全部已读</span>
          <span class="notice-view-all" @click="goToMessageCenter">查看全部</span>
        </div>
      </div>
      <div v-if="messageStore.loading" class="notice-loading">
        <el-icon class="is-loading"><Loading /></el-icon> 加载中...
      </div>
      <div v-else-if="messageStore.noticeList.length === 0" class="notice-empty">
        <el-icon style="font-size:24px;display:block;margin-bottom:6px;"><Postcard /></el-icon>
        暂无消息
      </div>
      <div v-else>
        <div v-for="item in messageStore.noticeList" :key="item.messageId" class="notice-item" :class="{ 'is-read': item.isRead }" @click="previewNotice(item)">
          <el-tag size="small" :type="messageTypeStyle(item.messageType).tagType" class="notice-tag">
            {{ messageTypeStyle(item.messageType).label }}
          </el-tag>
          <span class="notice-item-title">{{ item.messageTitle }}</span>
          <span class="notice-item-date">{{ formatTime(item.createTime) }}</span>
        </div>
      </div>

      <!-- 触发器 -->
      <template #reference>
        <div class="right-menu-item hover-effect notice-trigger" @click="goToMessageCenter">
          <svg-icon icon-class="bell" />
          <span v-if="messageStore.unreadCount > 0" class="notice-badge">{{ messageStore.unreadCount }}</span>
        </div>
      </template>
    </el-popover>

    <!-- 预览弹窗 -->
    <message-detail-view ref="messageViewRef" @read="onDetailRead" />
  </div>
</template>

<script setup>
import MessageDetailView from '@/views/system/message/DetailView'
import { useMessageStore } from '@/store/modules/message'
import { useRouter } from 'vue-router'

const router = useRouter()
const messageStore = useMessageStore()
const noticeVisible = ref(false)
const { proxy } = getCurrentInstance()

const typeMap = {
  '1': { label: '通知', tagType: 'primary' },
  '2': { label: '预警', tagType: 'danger' },
  '3': { label: '审批', tagType: 'warning' },
  '4': { label: '待办', tagType: 'info' }
}

function messageTypeStyle(type) {
  return typeMap[type] || { label: '消息', tagType: 'primary' }
}

function formatTime(time) {
  if (!time) return ''
  const str = String(time)
  return str.length >= 16 ? str.substring(5, 16) : str
}

// 组件挂载时从 store 加载（如果 store 还没有数据）
onMounted(() => {
  if (messageStore.noticeList.length === 0) {
    messageStore.loadNoticeTop()
  }
})

// 点击铃铛跳转到消息中心页面
function goToMessageCenter() {
  noticeVisible.value = false
  router.push('/message')
}

// 预览消息详情（从铃铛弹窗点击）
function previewNotice(item) {
  // 通过 store 标记已读（会同步更新未读数）
  if (!item.isRead) {
    messageStore.markRead(item.messageId)
  }
  proxy.$refs["messageViewRef"].open(item)
}

// 详情组件标记已读后的回调
function onDetailRead(messageId) {
  // store 已经在 DetailView 调用 markMessageRead 时更新了
  // 但如果详情是从铃铛弹窗打开的，需要确保 store 知道
  // 这里重新拉取确保数据一致
  messageStore.loadNoticeTop()
}

// 全部已读
function handleMarkAllRead() {
  messageStore.markAllRead()
}
</script>

<style lang="scss" scoped>
.notice-trigger {
  position: relative;
  transform: translateX(-6px);
  .svg-icon { width: 1.2em; height: 1.2em; vertical-align: -0.2em; }
  .notice-badge {
    position: absolute;
    top: 7px;
    right: -3px;
    background: #f56c6c;
    color: #fff;
    border-radius: 10px;
    font-size: 10px;
    height: 16px;
    line-height: 16px;
    padding: 0 4px;
    min-width: 16px;
    text-align: center;
    white-space: nowrap;
    pointer-events: none;
  }
}
.notice-popover { padding: 0 !important; }
.notice-popover .notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #f7f9fb;
  border-bottom: 1px solid #eee;
  font-size: 13px;
  font-weight: 600;
  color: #333;
}
.notice-popover .notice-header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.notice-popover .notice-mark-all,
.notice-popover .notice-view-all {
  font-size: 12px;
  color: var(--el-color-primary);
  font-weight: normal;
  cursor: pointer;
}
.notice-popover .notice-mark-all:hover,
.notice-popover .notice-view-all:hover { color: #2b7cc1; }
.notice-popover .notice-loading,
.notice-popover .notice-empty {
  padding: 24px;
  text-align: center;
  color: #bbb;
  font-size: 12px;
  line-height: 1.8;
}
.notice-popover .notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
}
.notice-popover .notice-item:last-child { border-bottom: none; }
.notice-popover .notice-item:hover { background: #f7f9fb; }
.notice-popover .notice-item.is-read .notice-tag,
.notice-popover .notice-item.is-read .notice-item-title,
.notice-popover .notice-item.is-read .notice-item-date { opacity: 0.45; filter: grayscale(1); color: #999; }
.notice-popover .notice-tag { flex-shrink: 0; }
.notice-popover .notice-item-title {
  flex: 1;
  font-size: 12px;
  color: #333;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.notice-popover .notice-item-date {
  flex-shrink: 0;
  font-size: 11px;
  color: #bbb;
}
</style>
