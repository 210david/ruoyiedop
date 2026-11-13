<template>
  <!-- 企业微信集成：业务逻辑与模板复用共享面板组件，连接器固定为 wecom -->
  <attendance-binding-panel connector-type="wecom" />
</template>

<script setup name="Wecom">
import AttendanceBindingPanel from '../components/AttendanceBindingPanel.vue'
</script>
