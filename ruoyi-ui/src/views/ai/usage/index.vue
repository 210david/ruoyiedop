<template>
  <div class="app-container" v-loading="loading">
    <!-- 用量总览卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-label">本月已调用</div>
            <div class="stat-value">{{ monthlyUsed }}</div>
            <div class="stat-sub">限额 {{ monthlyLimit > 0 ? monthlyLimit + ' 次/月' : '不限' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-label">近{{ days }}天总调用</div>
            <div class="stat-value">{{ total.total || 0 }}</div>
            <div class="stat-sub">平均耗时 {{ total.avgDuration || 0 }}ms</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-label">成功 / 降级</div>
            <div class="stat-value ok">{{ total.success || 0 }} <span class="sep">/</span> {{ total.degraded || 0 }}</div>
            <div class="stat-sub">降级=AI失败走备选逻辑</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-label">失败 / 限额拦截</div>
            <div class="stat-value err">{{ total.failed || 0 }} <span class="sep">/</span> {{ total.limited || 0 }}</div>
            <div class="stat-sub">失败较多请检查AI配置</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模块统计 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>按模块统计（近{{ days }}天）</span>
          <el-radio-group v-model="days" size="small" @change="load">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
            <el-radio-button :value="90">近90天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="moduleStat" style="width: 100%">
        <el-table-column label="模块" prop="module" width="180">
          <template #default="scope">{{ moduleNames[scope.row.module] || scope.row.module }}</template>
        </el-table-column>
        <el-table-column label="总调用" prop="total" width="100" />
        <el-table-column label="成功" prop="success" width="100" />
        <el-table-column label="降级" prop="degraded" width="100" />
        <el-table-column label="失败" prop="failed" width="100" />
        <el-table-column label="限额拦截" prop="limited" width="100" />
        <el-table-column label="占比">
          <template #default="scope">
            <el-progress :percentage="percentOf(scope.row.total)" :stroke-width="12" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 功能统计 -->
    <el-card shadow="never" class="table-card">
      <template #header><span>按功能统计（TOP 20）</span></template>
      <el-table :data="featureStat" style="width: 100%">
        <el-table-column label="模块" width="160">
          <template #default="scope">{{ moduleNames[scope.row.module] || scope.row.module }}</template>
        </el-table-column>
        <el-table-column label="功能" prop="feature" min-width="160" />
        <el-table-column label="总调用" prop="total" width="100" />
        <el-table-column label="成功" prop="success" width="100" />
        <el-table-column label="平均耗时" prop="avgDuration" width="120">
          <template #default="scope">{{ scope.row.avgDuration || 0 }} ms</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="AiUsage">
import { getAiUsage } from '@/api/ai/center'

const loading = ref(true)
const days = ref(30)
const total = ref({})
const moduleStat = ref([])
const featureStat = ref([])
const monthlyUsed = ref(0)
const monthlyLimit = ref(0)

const moduleNames = {
  dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产', platform: '平台'
}

function percentOf(val) {
  const sum = moduleStat.value.reduce((a, b) => a + (Number(b.total) || 0), 0)
  return sum > 0 ? Math.round((Number(val) || 0) * 100 / sum) : 0
}

function load() {
  loading.value = true
  getAiUsage(days.value).then(res => {
    const d = res.data || {}
    total.value = d.total || {}
    moduleStat.value = d.moduleStat || []
    featureStat.value = d.featureStat || []
    monthlyUsed.value = d.monthlyUsed || 0
    monthlyLimit.value = d.monthlyLimit || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

load()
</script>

<style scoped>
.stat-row { margin-bottom: 16px; }
.stat-item { padding: 4px 0; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-value.ok { color: #059669; }
.stat-value.err { color: #dc2626; }
.stat-value .sep { color: #c0c4cc; font-weight: 400; }
.stat-sub { font-size: 12px; color: #909399; margin-top: 6px; }
.table-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
