<template>
  <!-- 知识问答图标：对话气泡 + 内嵌翻开书本 + AI 闪光 -->
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true" class="ai-knowledge-icon">
    <!-- 对话气泡（含尾巴） -->
    <path
      d="M12 3.2c-5.1 0-9.2 3.4-9.2 7.7 0 2.4 1.3 4.5 3.4 5.9-.1 1-.5 2.1-1.5 3.1-.2.2 0 .6.3.6 1.9-.1 3.4-.8 4.4-1.5.8.2 1.7.3 2.6.3 5.1 0 9.2-3.4 9.2-7.7S17.1 3.2 12 3.2Z"
      fill="currentColor" fill-opacity="0.18" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
    <!-- 翻开的书本 -->
    <path d="M12 9.1c-.9-.8-2.2-1.2-3.6-1.2-.4 0-.8 0-1.1.1v6.3c.3-.1.7-.1 1.1-.1 1.4 0 2.7.4 3.6 1.2.9-.8 2.2-1.2 3.6-1.2.4 0 .8 0 1.1.1V7.9c-.3-.1-.7-.1-1.1-.1-1.4 0-2.7.4-3.6 1.3Z"
      fill="currentColor" fill-opacity="0.35" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" />
    <path d="M12 9.1v6.3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
    <!-- AI 闪光（右上角） -->
    <path d="M18.6 2.2l.55 1.55L20.7 4.3l-1.55.55-.55 1.55-.55-1.55L16.5 4.3l1.55-.55.55-1.55Z"
      fill="currentColor" />
  </svg>
</template>

<script setup name="AiKnowledgeIcon">
defineProps({
  size: { type: [Number, String], default: 17 }
})
</script>

<style scoped>
.ai-knowledge-icon { display: block; }
</style>
