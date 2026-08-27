<template>
  <el-dialog v-model="visible" width="780px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog message-detail-dialog" @close="handleClose">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
        </div>
        <div class="rd-detail-header-main">
          <span class="rd-detail-header-title">消息详情</span>
          <div class="rd-detail-header-sub" v-if="detail">
            <span class="rd-detail-header-no" v-if="detail.messageId">编号：{{ detail.messageId }}</span>
            <div class="rd-detail-header-divider"></div>
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
          <div class="rd-card-header" @click="toggleCard('m1')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" /><line x1="16" y1="13" x2="8" y2="13" /><line x1="16" y1="17" x2="8" y2="17" />
                </svg>
              </span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.m1 }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15" /></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.m1">
            <div class="rd-grid">
              <div class="rd-item rd-item--full">
                <span class="rd-label">消息标题</span>
                <div class="rd-value rd-value--large">{{ detail.messageTitle || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">消息类型</span>
                <div class="rd-value"><el-tag :type="typeTagType" size="small">{{ typeLabel }}</el-tag></div>
              </div>
              <div class="rd-item">
                <span class="rd-label">消息级别</span>
                <div class="rd-value"><el-tag :type="levelTagType" size="small">{{ levelLabel }}</el-tag></div>
              </div>
              <div class="rd-item">
                <span class="rd-label">业务来源</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !detail.bizSource }">{{ bizSourceLabel }}</div>
              </div>
              <div class="rd-item" v-if="detail.bizEntryName">
                <span class="rd-label">处理入口</span>
                <div class="rd-value"><el-tag type="primary" size="small">{{ detail.bizEntryName }}</el-tag></div>
              </div>
              <div class="rd-item">
                <span class="rd-label">业务ID</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !detail.bizId }">{{ detail.bizId || '暂无' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">通知时间</span>
                <div class="rd-value">{{ detail.createTime || '—' }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 消息内容 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('m2')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="4" y1="6" x2="20" y2="6" /><line x1="4" y1="12" x2="20" y2="12" /><line x1="4" y1="18" x2="14" y2="18" />
                </svg>
              </span>
              消息内容
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.m2 }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15" /></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.m2">
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

      </template>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关 闭</el-button>
        <el-button type="primary" v-if="detail?.redirectUrl" @click="handleRedirect">
          <el-icon style="margin-right: 4px"><Position /></el-icon>
          前往{{ detail.bizEntryName || '处理' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { getMessage } from '@/api/system/message'
import { useMessageStore } from '@/store/modules/message'
import { useDict } from '@/utils/dict'
import { useDetailCard } from '@/composables/useDetailCard'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['read'])

const messageStore = useMessageStore()
const router = useRouter()
const visible = ref(false)
const loading = ref(false)
const detail = ref(null)

const { collapsedCards, toggleCard } = useDetailCard(["m1", "m2"])

const { sys_message_biz_source } = useDict('sys_message_biz_source')

const typeMap = {
  '1': { label: '系统通知', class: 'msg-type-notify', tagType: 'info' },
  '2': { label: '业务预警', class: 'msg-type-alert', tagType: 'danger' },
  '3': { label: '审批消息', class: 'msg-type-approval', tagType: 'warning' },
  '4': { label: '待办事项', class: 'msg-type-todo', tagType: '' }
}

const levelMap = {
  '1': { label: '普通', class: 'msg-level-normal', tagType: 'info' },
  '2': { label: '重要', class: 'msg-level-important', tagType: 'warning' },
  '3': { label: '紧急', class: 'msg-level-urgent', tagType: 'danger' }
}

const typeLabel = computed(() => typeMap[detail.value?.messageType]?.label || '消息')
const typeClass = computed(() => typeMap[detail.value?.messageType]?.class || 'msg-type-notify')
const typeTagType = computed(() => typeMap[detail.value?.messageType]?.tagType || 'info')
const levelLabel = computed(() => levelMap[detail.value?.messageLevel]?.label || '普通')
const levelClass = computed(() => levelMap[detail.value?.messageLevel]?.class || 'msg-level-normal')
const levelTagType = computed(() => levelMap[detail.value?.messageLevel]?.tagType || 'info')
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
  if (!detail.value?.redirectUrl) return
  const raw = detail.value.redirectUrl
  // 解析 path 和 query 参数
  const [path, queryString] = raw.split('?')
  const query = {}
  if (queryString) {
    queryString.split('&').forEach(pair => {
      const [k, v] = pair.split('=')
      if (k) query[decodeURIComponent(k)] = v ? decodeURIComponent(v) : ''
    })
  }
  // 检查路由是否存在
  const resolved = router.resolve(path)
  if (resolved.matched.length === 0 || resolved.matched.some(r => r.name === 'NotFound')) {
    ElMessage.warning('处理页面暂未配置，请联系管理员')
    return
  }
  visible.value = false
  router.push({ path, query })
}

defineExpose({
  open
})
</script>

<style scoped>
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

/* ===== 消息内容（富文本） ===== */
.msg-content {
  font-size: 14px; line-height: 1.85; color: #2d3748; word-break: break-word;
  min-height: 120px; padding: 4px 0;
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

</style>
