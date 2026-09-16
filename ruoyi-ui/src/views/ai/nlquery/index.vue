<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span style="font-size: 16px; font-weight: 600">智能查询（自然语言查数）</span>
          <el-tag type="warning" size="small">仅白名单表 · 只读查询 · 自动限100行</el-tag>
        </div>
      </template>

      <div class="query-box">
        <el-input v-model="question" size="large" clearable
          placeholder="用自然语言提问，如：上月故障次数最多的三台设备是哪些？"
          @keyup.enter="handleQuery" :disabled="querying">
          <template #append>
            <el-button :loading="querying" @click="handleQuery">查 询</el-button>
          </template>
        </el-input>
        <div class="query-tips">
          示例：<el-link @click="question='本月工单数量按状态分布如何？'; handleQuery()">本月工单数量按状态分布</el-link>
          <el-divider direction="vertical" />
          <el-link @click="question='故障次数最多的前5台设备'; handleQuery()">故障最多的前5台设备</el-link>
          <el-divider direction="vertical" />
          <el-link @click="question='各供应商的供货金额排名'; handleQuery()">供应商供货金额排名</el-link>
        </div>
      </div>

      <!-- 错误提示 -->
      <el-alert v-if="error" :title="error" type="warning" :closable="false" style="margin-top: 12px" show-icon />

      <!-- 结果 -->
      <div v-if="resultColumns.length" class="result-box">
        <div class="result-bar">
          <span>共 {{ resultRows.length }} 行</span>
          <el-tooltip content="AI生成的查询语句（只读）" placement="top">
            <span class="sql-link" @click="sqlVisible = !sqlVisible">{{ sqlVisible ? '隐藏SQL' : '查看SQL' }}</span>
          </el-tooltip>
        </div>
        <pre v-if="sqlVisible" class="sql-view">{{ resultSql }}</pre>
        <el-table :data="resultRows" border size="small" max-height="480">
          <el-table-column v-for="col in resultColumns" :key="col" :label="col" :prop="col" min-width="120" show-overflow-tooltip />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup name="AiNlQuery">
import { nlQuery } from '@/api/ai/manage'

const question = ref('')
const querying = ref(false)
const error = ref('')
const resultColumns = ref([])
const resultRows = ref([])
const resultSql = ref('')
const sqlVisible = ref(false)

function handleQuery() {
  const q = (question.value || '').trim()
  if (!q) return
  querying.value = true
  error.value = ''
  resultColumns.value = []
  resultRows.value = []
  nlQuery(q).then(res => {
    querying.value = false
    const d = res.data || {}
    if (d.error) {
      error.value = d.error
      if (d.sql) { resultSql.value = d.sql; }
      return
    }
    resultColumns.value = d.columns || []
    resultRows.value = d.rows || []
    resultSql.value = d.sql || ''
    if (!resultRows.value.length) {
      error.value = '查询结果为空，请尝试调整问题表述'
    }
  }).catch(() => { querying.value = false })
}
</script>

<style scoped>
.query-box { max-width: 860px; }
.query-tips { margin-top: 8px; font-size: 12px; color: #909399; display: flex; align-items: center; gap: 4px; }
.result-box { margin-top: 16px; }
.result-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-size: 13px; color: #909399; }
.sql-link { color: #409eff; cursor: pointer; }
.sql-view { background: #f5f7fa; border: 1px solid #e4e7ed; border-radius: 6px; padding: 10px 14px; font-size: 12px; margin: 0 0 8px 0; white-space: pre-wrap; word-break: break-all; }
</style>
