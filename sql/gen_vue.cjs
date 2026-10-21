const fs = require('fs');
const path = 'e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-ui/src/views/hr/employee/index.vue';
// Read existing file to see what's there
const content = fs.readFileSync(path, 'utf8');
// The file was partially written. We need to rewrite it completely.
// Strategy: Write the file in append mode, chunk by chunk
const chunks = [];
chunks.push(`<template>
  <div class="app-container hr-employee-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> \u7b5b\u9009\u6761\u4ef6</div></div>
      <div class="filter-bar">
        <div class="field"><label>\u5de5\u53f7</label><div class="control"><el-input v-model="queryParams.employeeNo" placeholder="\u8bf7\u8f93\u5165\u5de5\u53f7" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>\u59d3\u540d</label><div class="control"><el-input v-model="queryParams.employeeName" placeholder="\u8bf7\u8f93\u5165\u59d3\u540d" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>\u8eab\u4efd\u8bc1\u53f7</label><div class="control"><el-input v-model="queryParams.idCard" placeholder="\u8bf7\u8f93\u5165\u8eab\u4efd\u8bc1\u53f7" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>\u5458\u5de5\u72b6\u6001</label><div class="control"><el-select v-model="queryParams.employeeStatus" placeholder="\u8bf7\u9009\u62e9" clearable style="width: 100%"><el-option label="\u5728\u804c" value="\u5728\u804c" /><el-option label="\u8bd5\u7528" value="\u8bd5\u7528" /><el-option label="\u79bb\u804c" value="\u79bb\u804c" /></el-select></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> \u5df2\u9009 {{ activeFilterCount }} \u4e2a\u6761\u4ef6</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">\u91cd\u7f6e</el-button><el-button type="primary" icon="Search" @click="handleQuery">\u641c\u7d22</el-button></div></div>
    </div>
`);

console.log('Script loaded, content length:', content.length);
