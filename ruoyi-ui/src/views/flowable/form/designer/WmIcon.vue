<template>
  <svg class="wm-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"
    stroke-linecap="round" stroke-linejoin="round" :width="size" :height="size" aria-hidden="true">
    <path :d="d" />
  </svg>
</template>

<script setup name="WmIcon">
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: Number, default: 15 }
})

/** 统一线性图标（对标 Excel/泛微工具条观感） */
const ICONS = {
  'align-left': 'M3 5h18M3 9h11M3 13h14M3 17h8',
  'align-center': 'M3 5h18M7 9h10M5 13h14M8 17h8',
  'align-right': 'M3 5h18M11 9h10M7 13h14M13 17h8',
  'align-top': 'M5 3v18M9 3v11M13 3v15M17 3v9',
  'align-middle': 'M5 3v18M9 6v12M13 4v16M17 8v8',
  'align-bottom': 'M5 3v18M9 10v11M13 6v15M17 12v9',
  'indent-in': 'M3 5h18M3 12h9M3 19h18M16 8l4 4-4 4',
  'indent-out': 'M3 5h18M3 12h9M3 19h18M20 8l-4 4 4 4',
  painter: 'M4 20c3.5 0 4-4.5 6.5-7.5l2.5 2.5C11 18.5 7.5 20 4 20zM13 11l5.5-5.5 3 3L16 14l-3-3z',
  bucket: 'M12 3.5l5.2 6.2a5.2 5.2 0 1 1-10.4 0L12 3.5zM4 21h16',
  'border-grid': 'M4 4h16v16H4zM4 12h16M12 4v16',
  merge: 'M4 4h7v7H4zM13 4h7v7h-7zM4 13h16v7H4z',
  split: 'M4 4h16v7H4zM4 13h7v7H4zM13 13h7v7h-7z',
  pencil: 'M5 19l2.5-6.5L16 4l4 4-8.5 8.5L5 19zM14 6l4 4',
  'insert-row': 'M4 4h16v16H4zM4 12h16M12 9v6M9.5 12h5',
  'insert-col': 'M4 4h16v16H4zM12 4v16M8 9v6M5 12h6',
  'delete-row': 'M4 4h16v16H4zM4 12h16M9 12h6',
  'delete-col': 'M4 4h16v16H4zM12 4v16M5 12h6',
  eraser: 'M4 16l9-9 6 6-6 6H8l-4-4zM10 10l6 6M4 20h16',
  broom: 'M16 3l5 5-9 9H7l-3-4 12-10zM10 17l4-4',
  trash: 'M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v5M14 11v5'
}

const d = computed(() => ICONS[props.name] || '')
</script>

<style scoped>
.wm-icon { display: inline-block; vertical-align: middle; flex: none; }
</style>
