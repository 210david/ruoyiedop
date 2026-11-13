<template>
  <!-- 钉钉集成：业务逻辑与模板复用共享面板组件，连接器固定为 dingtalk -->
  <attendance-binding-panel connector-type="dingtalk" />
</template>

<script setup name="Attendance">
import AttendanceBindingPanel from '../components/AttendanceBindingPanel.vue'
</script>
