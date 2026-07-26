<template>
  <component :is="type" v-bind="linkProps()">
    <slot />
  </component>
</template>

<script setup>
import { isExternal } from '@/utils/validate'

const props = defineProps({
  to: {
    type: [String, Object],
    required: true
  }
})

const isExt = computed(() => {
  return isExternal(props.to)
})

const type = computed(() => {
  if (isExt.value) {
    return 'a'
  }
  return 'router-link'
})

function linkProps() {
  if (isExt.value) {
    let href = props.to
    // 处理内部大屏路由，转换为完整 URL
    if (typeof props.to === 'object') {
      const path = props.to.path || '/'
      const query = props.to.query || {}
      const queryString = new URLSearchParams(query).toString()
      href = window.location.origin + path + (queryString ? '?' + queryString : '')
    } else if (props.to.startsWith('/dms/dashboard/screen')) {
      href = window.location.origin + props.to
    }
    return {
      href: href,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return {
    to: props.to
  }
}
</script>
