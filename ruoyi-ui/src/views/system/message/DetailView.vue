<template>
  <el-dialog v-model="visible" width="840px" append-to-body draggable :close-on-click-modal="false" class="message-detail-dialog" @close="handleClose">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
        </div>
        <div class="rd-detail-header-main">
          <div class="rd-detail-header-title">消息详情</div>
          <div class="rd-detail-header-sub" v-if="detail">
            <span class="rd-detail-header-no" v-if="detail.messageId">编号：{{ detail.messageId }}</span>
            <span class="msg-type-badge" :class="typeClass">{{ typeLabel }}</span>
            <span class="msg-level-badge" :class="levelClass" v-if="detail.messageLevel">{{ levelLabel }}</span>
          </div>
          <div class="rd-detail-header-sub" v-else>
            <span class="rd-detail-header-placeholder">查看消息详细信息</span>
          </div>
        </div>
      </div>
    </template>

    <div v-loading="loading" class="rd-page">
      <!-- 空状态 -->
      <div v-if="!detail && !loading" class="rd-empty">
        <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
        <p class="rd-empty-text">暂无消息数据</p>
      </div>

      <template v-if="detail">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" /><line x1="9" y1="15" x2="15" y2="15" />
                </svg>
              </span>
              基本信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item rd-item--full">
                <span class="rd-label"><span class="rd-label-dot"></span>消息标题</span>
                <div class="rd-value rd-value--large">{{ detail.messageTitle || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>消息类型</span>
                <div class="rd-value">{{ typeLabel }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>消息级别</span>
                <div class="rd-value">{{ levelLabel }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>业务来源</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !detail.bizSource }">{{ bizSourceLabel }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>业务ID</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !detail.bizId }">{{ detail.bizId || '暂无' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label"><span class="rd-label-dot"></span>创建时间</span>
                <div class="rd-value">{{ detail.createTime || '—' }}</div>
              </div>
              <div class="rd-item rd-item--full" v-if="detail.redirectUrl">
                <span class="rd-label"><span class="rd-label-dot"></span>跳转链接</span>
                <div class="rd-value">{{ detail.redirectUrl }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 消息内容 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="4" y1="6" x2="20" y2="6" /><line x1="4" y1="12" x2="20" y2="12" /><line x1="4" y1="18" x2="14" y2="18" />
                </svg>
              </span>
              消息内容
            </div>
          </div>
          <div class="rd-card-body">
            <div v-if="hasContent" class="msg-content" v-html="detail.messageContent" />
            <div v-else class="rd-empty">
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              <p class="rd-empty-text">暂无内容</p>
            </div>
          </div>
        </section>

        <!-- 操作区 -->
        <div class="msg-actions" v-if="detail.redirectUrl">
          <el-button type="primary" size="large" @click="handleRedirect">
            <el-icon style="margin-right: 4px"><Position /></el-icon>
            前往处理
          </el-button>
        </div>
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
import { getMessage } from '@/api/system/message'
import { useMessageStore } from '@/store/modules/message'
import { useDict } from '@/utils/dict'
import { useRouter } from 'vue-router'

const emit = defineEmits(['read'])

const messageStore = useMessageStore()
const router = useRouter()
const visible = ref(false)
const loading = ref(false)
const detail = ref(null)

const { sys_message_biz_source } = useDict('sys_message_biz_source')

const typeMap = {
  '1': { label: '系统通知', class: 'msg-type-notify' },
  '2': { label: '业务预警', class: 'msg-type-alert' },
  '3': { label: '审批消息', class: 'msg-type-approval' },
  '4': { label: '待办事项', class: 'msg-type-todo' }
}

const levelMap = {
  '1': { label: '普通', class: 'msg-level-normal' },
  '2': { label: '重要', class: 'msg-level-important' },
  '3': { label: '紧急', class: 'msg-level-urgent' }
}


const typeLabel = computed(() => typeMap[detail.value?.messageType]?.label || '消息')
const typeClass = computed(() => typeMap[detail.value?.messageType]?.class || 'msg-type-notify')
const levelLabel = computed(() => levelMap[detail.value?.messageLevel]?.label || '普通')
const levelClass = computed(() => levelMap[detail.value?.messageLevel]?.class || 'msg-level-normal')
const bizSourceLabel = computed(() => {
  if (!detail.value?.bizSource) return '暂无'
  const item = sys_message_biz_source.value.find(d => d.value == detail.value.bizSource)
  return item ? item.label : detail.value.bizSource
})
const hasContent = computed(() => {
  const content = detail.value && detail.value.messageContent
  return content != null && String(content).trim() !== ''
})

function open(payload) {
  let id = null
  let preset = null
  if (payload != null && typeof payload === 'object') {
    id = payload.messageId
    if (payload.messageContent != null) {
      preset = payload
    }
  } else {
    id = payload
  }
  visible.value = true
  if (preset) {
    detail.value = preset
    if (id && !preset.isRead) {
      // 通过 store 标记已读（会同步更新铃铛未读数）
      messageStore.markRead(id).then(() => emit('read', id)).catch(() => {})
    }
    return
  }
  if (id == null || id === '') {
    detail.value = null
    return
  }
  loading.value = true
  detail.value = null
  getMessage(id).then(res => {
    detail.value = res.data
    if (!res.data.isRead) {
      // 通过 store 标记已读（会同步更新铃铛未读数）
      messageStore.markRead(id).then(() => emit('read', id)).catch(() => {})
    }
  }).catch(() => {
    detail.value = null
  }).finally(() => {
    loading.value = false
  })
}

function handleClose() {
  visible.value = false
  detail.value = null
  loading.value = false
}

function handleRedirect() {
  if (detail.value?.redirectUrl) {
    visible.value = false
    router.push(detail.value.redirectUrl)
  }
}

defineExpose({
  open
})
</script>

<style scoped>
/* ===== 覆盖 el-dialog 默认样式 ===== */
:deep(.message-detail-dialog) { border-radius: 12px; overflow: hidden; }
:deep(.message-detail-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.message-detail-dialog .el-dialog__headerbtn) { top: 14px; right: 14px; z-index: 10; }
:deep(.message-detail-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.message-detail-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.message-detail-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.message-detail-dialog .rd-page) { max-width: none; }

/* ===== 标题横幅 ===== */
.rd-detail-header {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%);
  border-radius: 12px 12px 0 0;
  position: relative; overflow: hidden;
}
.rd-detail-header::before {
  content: ''; position: absolute;
  top: -30px; right: -15px;
  width: 140px; height: 140px; border-radius: 50%;
  background: radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%);
  pointer-events: none;
}
.rd-detail-header-icon {
  display: flex; align-items: center; justify-content: center;
  width: 40px; height: 40px; border-radius: 10px;
  background: rgb(255 255 255 / 0.2);
  border: 1px solid rgb(255 255 255 / 0.25);
  color: #fff; flex-shrink: 0;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 12px rgb(0 0 0 / 0.1);
}
.rd-detail-header-main {
  display: flex; flex-direction: column; gap: 4px;
  min-width: 0; padding-right: 36px;
}
.rd-detail-header-title { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: -0.02em; line-height: 1.3; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-no { font-size: 13px; font-weight: 500; color: rgb(255 255 255 / 0.8); font-variant-numeric: tabular-nums; }
.rd-detail-header-placeholder { font-size: 13px; color: rgb(255 255 255 / 0.6); }

/* ===== 标题横幅中的类型/级别徽章 ===== */
.msg-type-badge, .msg-level-badge {
  display: inline-flex; align-items: center;
  padding: 1px 8px; border-radius: 999px;
  font-size: 11px; font-weight: 600; letter-spacing: 0.5px;
  border: 1px solid transparent;
}
.msg-type-notify { background: rgba(59,130,246,.2); color: #bfdbfe; border-color: rgba(59,130,246,.3); }
.msg-type-alert { background: rgba(239,68,68,.2); color: #fecaca; border-color: rgba(239,68,68,.3); }
.msg-type-approval { background: rgba(245,158,11,.2); color: #fde68a; border-color: rgba(245,158,11,.3); }
.msg-type-todo { background: rgba(139,92,246,.2); color: #ddd6fe; border-color: rgba(139,92,246,.3); }

.msg-level-normal { background: rgba(148,163,184,.2); color: #e2e8f0; border-color: rgba(148,163,184,.3); }
.msg-level-important { background: rgba(245,158,11,.25); color: #fde68a; border-color: rgba(245,158,11,.35); }
.msg-level-urgent { background: rgba(239,68,68,.25); color: #fecaca; border-color: rgba(239,68,68,.35); }

/* ===== 页面容器 ===== */
.rd-page { max-width: 840px; margin: 0 auto; }

/* ===== 卡片 ===== */
.rd-card {
  background: #fff; border-radius: 12px; border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden;
  margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4,0,0.2,1);
  animation: rdFadeIn 0.4s ease-out forwards;
}
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff);
  border-bottom: 1px solid #f3f4f6;
}
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 8px;
  background: #fff; border: 1px solid #e5e7eb; color: #2563eb;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}
.rd-card-body { padding: 14px 16px; }

/* ===== 信息网格 ===== */
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label {
  flex: 0 0 auto; min-width: 72px;
  display: flex; align-items: center;
  font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap;
}
.rd-label-dot { display: none; }
.rd-value {
  flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827;
  line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0;
}
.rd-value--large { font-size: 18px; font-weight: 700; }
.rd-value--muted { color: #9ca3af; font-style: italic; }

/* ===== 消息内容 ===== */
.msg-content {
  font-size: 14px; line-height: 1.85; color: #2d3748; word-break: break-word;
}
.msg-content :deep(p) { margin: 0 0 1em; }
.msg-content :deep(h1), .msg-content :deep(h2), .msg-content :deep(h3) {
  font-weight: 700; color: #111827; margin: 1.4em 0 0.6em;
}
.msg-content :deep(h1) { font-size: 18px; }
.msg-content :deep(h2) { font-size: 16px; }
.msg-content :deep(h3) { font-size: 14px; }
.msg-content :deep(a) { color: #2563eb; text-decoration: underline; }
.msg-content :deep(img) { max-width: 100%; border-radius: 4px; margin: 8px 0; }
.msg-content :deep(ul), .msg-content :deep(ol) { padding-left: 20px; margin: 0 0 1em; }
.msg-content :deep(li) { margin-bottom: 4px; }
.msg-content :deep(blockquote) {
  border-left: 3px solid #d1d5db; margin: 1em 0; padding: 6px 16px;
  color: #6b7280; background: #f9fafb;
}
.msg-content :deep(table) { border-collapse: collapse; width: 100%; margin: 1em 0; font-size: 13px; }
.msg-content :deep(table th), .msg-content :deep(table td) { border: 1px solid #e5e7eb; padding: 7px 12px; }
.msg-content :deep(table th) { background: #f9fafb; font-weight: 600; }

/* ===== 空状态 ===== */
.rd-empty {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 20px; text-align: center; color: #9ca3af;
  background: #f9fafb; border-radius: 12px; border: 1px dashed #e5e7eb;
}
.rd-empty-icon { margin-bottom: 4px; color: #d1d5db; }
.rd-empty-text { font-size: 14px; font-weight: 500; margin: 0; }

/* ===== 操作区 ===== */
.msg-actions {
  display: flex; justify-content: center; padding: 16px 0 4px;
}

/* ===== 进入动画 ===== */
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .rd-grid { grid-template-columns: 1fr; }
  .rd-card-header { padding: 8px 12px; }
  .rd-card-body { padding: 12px; }
}
</style>
