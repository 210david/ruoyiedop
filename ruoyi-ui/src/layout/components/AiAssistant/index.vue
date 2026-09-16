<template>
  <div class="ai-assistant-entry" id="header-ai-assistant">
    <button class="ai-entry-btn" title="AI助手（知识库问答）" @click="open = !open">
      <el-icon :size="17"><MagicStick /></el-icon>
    </button>
    <transition name="el-zoom-in-top">
      <div v-if="open" class="ai-assistant-panel">
        <div class="ai-panel-head">
          <span class="ai-panel-title">AI 助手</span>
          <span class="ai-panel-sub">基于知识库 · 引用真实文档</span>
          <el-button link size="small" @click="open = false">收起</el-button>
        </div>
        <div class="ai-panel-body" ref="bodyRef">
          <div v-if="!messages.length" class="ai-empty">提问示例：设备操作规范、质量标准、安全制度……<br />AI 将基于知识库文档回答并标注来源。</div>
          <div v-for="(m, i) in messages" :key="i" :class="['ai-msg', m.role]">
            <span class="ai-msg-tag">{{ m.role === 'user' ? '问' : '答' }}</span>
            <div class="ai-msg-body">
              {{ m.text }}
              <div v-if="m.sources && m.sources.length" class="ai-msg-sources">
                来源：<el-tag v-for="(s, j) in m.sources" :key="j" size="small" type="info">{{ s.docName }}</el-tag>
              </div>
            </div>
          </div>
          <div v-if="asking" class="ai-msg assistant">
            <span class="ai-msg-tag">答</span>
            <div class="ai-msg-body ai-typing">正在检索知识库并生成答案…</div>
          </div>
        </div>
        <div class="ai-panel-input">
          <el-input v-model="question" size="small" placeholder="输入问题，回车发送"
            :disabled="asking" @keyup.enter="handleAsk" clearable />
          <el-button type="primary" size="small" :loading="asking" @click="handleAsk">发送</el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup name="AiAssistant">
/**
 * 全局AI助手入口（P2）：导航栏浮动对话面板
 * 复用 RAG 知识问答接口（/ai/knowledge/qa），增量插入，不改动既有导航逻辑
 */
import { ref, nextTick } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'
import { knowledgeQa } from '@/api/ai/knowledge'

const open = ref(false)
const question = ref('')
const asking = ref(false)
const messages = ref([])
const bodyRef = ref()

async function handleAsk() {
  const q = (question.value || '').trim()
  if (!q || asking.value) return
  messages.value.push({ role: 'user', text: q })
  question.value = ''
  asking.value = true
  scrollToBottom()
  try {
    const res = await knowledgeQa({ question: q })
    const d = res.data || {}
    messages.value.push({ role: 'assistant', text: d.answer || '未返回答案', sources: d.sources || [] })
  } catch (e) {
    messages.value.push({ role: 'assistant', text: '提问失败，请稍后重试' })
  } finally {
    asking.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}
</script>

<style scoped>
.ai-assistant-entry {
  display: inline-flex;
  align-items: center;
  margin-right: 4px;
}
.ai-entry-btn {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 8px rgba(99, 102, 241, 0.3);
  transition: transform 0.15s;
}
.ai-entry-btn:hover { transform: translateY(-1px); }
.ai-assistant-panel {
  position: absolute;
  right: 0;
  top: 46px;
  width: 380px;
  height: 460px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 3000;
  overflow: hidden;
}
.ai-panel-head {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #f5f3ff, #eef2ff);
}
.ai-panel-title { font-weight: 700; color: #4f46e5; }
.ai-panel-sub { flex: 1; font-size: 11px; color: #909399; }
.ai-panel-body { flex: 1; overflow-y: auto; padding: 10px 14px; }
.ai-empty { font-size: 12px; color: #909399; text-align: center; margin-top: 40px; line-height: 2; }
.ai-msg { margin-bottom: 10px; display: flex; gap: 6px; align-items: flex-start; }
.ai-msg.user { justify-content: flex-end; }
.ai-msg.user .ai-msg-body { background: #eef2ff; }
.ai-msg.assistant .ai-msg-body { background: #f8fafc; }
.ai-msg-tag { flex: none; font-size: 11px; border-radius: 4px; padding: 1px 5px; color: #fff; background: #6366f1; }
.ai-msg.user .ai-msg-tag { background: #909399; }
.ai-msg-body { font-size: 13px; color: #303133; line-height: 1.7; padding: 6px 10px; border-radius: 8px; white-space: pre-wrap; word-break: break-all; max-width: 85%; }
.ai-msg-sources { margin-top: 6px; font-size: 11px; color: #909399; }
.ai-typing { color: #909399; font-style: italic; }
.ai-panel-input { display: flex; gap: 8px; padding: 10px 12px; border-top: 1px solid #f0f0f0; }
</style>
