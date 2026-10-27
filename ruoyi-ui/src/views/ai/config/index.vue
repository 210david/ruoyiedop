<template>
  <div class="app-container ai-config-page">
    <el-card shadow="never" class="section-card">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span class="section-title"><span class="section-dot"></span>大模型配置</span>
          <el-tag type="info" size="small">配置保存后立即生效，全平台所有AI功能共用</el-tag>
        </div>
      </template>

      <el-form :model="form" label-width="180px" style="max-width: 700px" v-loading="loading">
        <el-form-item label="AI功能总开关">
          <el-switch v-model="form.enabled" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">关闭后所有模块AI功能不可用，降级为备选逻辑</div>
        </el-form-item>

        <el-form-item label="大模型服务商" v-show="form.enabled">
          <el-select v-model="provider" placeholder="选择服务商自动填入（可再手动修改）" style="width: 100%" clearable @change="applyProvider">
            <el-option v-for="p in providerPresets" :key="p.name" :label="p.name" :value="p.name">
              <span style="float: left">{{ p.name }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 12px">{{ p.model }}</span>
            </el-option>
          </el-select>
          <div class="form-tip">内置主流OpenAI兼容服务商，选择后自动填入地址与模型名，仍可手动修改</div>
        </el-form-item>

        <el-form-item label="大模型API地址" v-show="form.enabled">
          <el-input v-model="form.apiUrl" placeholder="如 https://api.xiaomimimo.com/v1/chat/completions" />
          <div class="form-tip">兼容OpenAI格式的API地址</div>
        </el-form-item>

        <el-form-item label="大模型API Key" v-show="form.enabled">
          <el-input v-model="form.apiKey" type="password" autocomplete="new-password"
            :placeholder="apiKeyConfigured ? '已配置（不可查看），留空则继续使用原密钥' : '填入您的API密钥'" />
          <div class="form-tip">在模型服务商后台获取；保存后系统不回显，留空表示沿用原密钥</div>
        </el-form-item>

        <el-form-item label="模型名称" v-show="form.enabled">
          <el-input v-model="form.modelName" placeholder="如 deepseek-chat / qwen-plus / mimo-v2.5" />
          <div class="form-tip">不同服务商的模型名称不同</div>
        </el-form-item>

        <el-form-item label="月度调用上限" v-show="form.enabled">
          <el-input-number v-model="form.monthlyLimit" :min="0" :max="100000" controls-position="right" style="width: 200px" />
          <span class="form-tip-inline">次/月</span>
          <div class="form-tip">超出上限后自动降级，业务不中断；本月已调用大模型 <b>{{ monthlyUsed }}</b> 次（限额按调用次数计）<span v-if="monthlyQuestions !== null">，用户提问 <b>{{ monthlyQuestions }}</b> 次（一次提问内部可能多次调用）</span></div>
        </el-form-item>
        <el-form-item label="智能查数（NL2Query）">
          <el-switch v-model="form.nl2queryEnabled" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">允许用户用自然语言查询业务数据；可查范围为库中放行前缀表减去黑名单表（ai.nl2query.blockedTables）</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSaveMain" :loading="saving">保 存 配 置</el-button>
          <el-button type="success" plain icon="Connection" @click="handleTest" :loading="testing">测 试 连 接</el-button>
          <el-button @click="handleResetMain">重 置</el-button>
          <span class="ops-tip">保存 / 测试 / 重置 仅作用于大模型配置；测试无需先保存</span>
        </el-form-item>

      </el-form>
    </el-card>

    <el-card shadow="never" class="section-card rag-card" v-loading="loading">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span class="section-title"><span class="section-dot dot-green"></span>RAG 向量检索<span class="section-sub">知识库语义搜索（可选）</span></span>
          <el-tag :type="embeddingReady ? 'success' : 'info'" size="small">{{ embeddingReady ? '已启用语义检索' : '未配置 · 知识库使用关键词检索' }}</el-tag>
        </div>
      </template>

      <el-form :model="form" label-width="180px" style="max-width: 700px" v-show="form.enabled">
        <el-form-item label="Embedding服务商">
          <el-select v-model="embeddingProvider" placeholder="选择服务商自动填入（可再手动修改）" style="width: 100%" clearable @change="applyEmbeddingProvider">
            <el-option v-for="p in embeddingPresets" :key="p.name" :label="p.name" :value="p.name">
              <span style="float: left">{{ p.name }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 12px">{{ p.model }}</span>
            </el-option>
          </el-select>
          <div class="form-tip">内置主流Embedding服务商，选择后自动填入地址与模型名，仍可手动修改</div>
        </el-form-item>
        <el-form-item label="Embedding API地址">
            <el-input v-model="form.embeddingUrl" placeholder="如 https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings" />
            <div class="form-tip">OpenAI兼容的向量化接口地址；不配置时知识库使用关键词检索（功能可用，语义精度略降）</div>
          </el-form-item>
          <el-form-item label="Embedding API Key">
            <el-input v-model="form.embeddingKey" type="password" autocomplete="new-password"
              :placeholder="embeddingKeyConfigured ? '已配置（不可查看），留空则继续使用原密钥' : '填入向量化服务密钥，留空则沿用对话模型密钥'" />
            <div class="form-tip">与对话模型可不同厂商；留空时自动沿用上方对话模型的Key</div>
          </el-form-item>
          <el-form-item label="Embedding模型名称">
            <el-input v-model="form.embeddingModel" placeholder="如 text-embedding-v3 / embedding-3" />
            <div class="form-tip">以向量服务商控制台为准</div>
          </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveRag" :loading="savingRag">保 存 RAG 配 置</el-button>
          <el-button type="success" plain icon="Connection" @click="handleTestEmbedding" :loading="testingEmbedding">测 试 Embedding</el-button>
          <el-button @click="handleResetRag">重 置</el-button>
          <span class="ops-tip">保存 / 测试 / 重置 仅作用于 RAG 配置；Key 留空时测试沿用对话模型密钥</span>
        </el-form-item>
      </el-form>
    </el-card>

  </div>
</template>

<script setup name="AiConfig">
import { getAiCenterConfig, updateAiCenterConfig, testAiCenterConfig } from '@/api/ai/center'
import { updateAiEmbeddingConfig, testEmbedding } from '@/api/ai/manage'
import { ElMessageBox } from 'element-plus'
import { h } from 'vue'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const saving = ref(false)
const savingRag = ref(false)
const testingEmbedding = ref(false)
const testing = ref(false)
const apiKeyConfigured = ref(false)
const embeddingKeyConfigured = ref(false)

// 主流Embedding服务商预设（OpenAI兼容向量化接口）
const embeddingPresets = [
  { name: '通义千问（阿里百炼）', url: 'https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings', model: 'text-embedding-v3' },
  { name: '智谱清言 GLM', url: 'https://open.bigmodel.cn/api/paas/v4/embeddings', model: 'embedding-3' },
  { name: '硅基流动 SiliconFlow', url: 'https://api.siliconflow.cn/v1/embeddings', model: 'BAAI/bge-large-zh-v1.5' },
  { name: 'OpenAI', url: 'https://api.openai.com/v1/embeddings', model: 'text-embedding-3-small' },
  { name: '百度千帆', url: 'https://qianfan.baidubce.com/v2/embeddings', model: 'bge-large-zh' },
  { name: '自定义/自建中转', url: '', model: '' }
]
const embeddingProvider = ref('')

/** 选择Embedding服务商后自动填入 */
function applyEmbeddingProvider(name) {
  const p = embeddingPresets.find(item => item.name === name)
  if (!p) return
  if (p.url) form.value.embeddingUrl = p.url
  if (p.model) form.value.embeddingModel = p.model
}
const embeddingReady = computed(() => !!(form.value.embeddingUrl && form.value.embeddingModel && (form.value.embeddingKey || embeddingKeyConfigured.value)))
const monthlyUsed = ref(0)
const monthlyQuestions = ref(null)
const originalForm = ref({})

// 主流大模型服务商预设（OpenAI兼容格式），选择后自动填入，仍可手动修改
const providerPresets = [
  { name: 'DeepSeek（深度求索）', url: 'https://api.deepseek.com/v1/chat/completions', model: 'deepseek-chat' },
  { name: '小米MiMo', url: 'https://api.xiaomimimo.com/v1/chat/completions', model: 'mimo-v2.5' },
  { name: '通义千问（阿里百炼）', url: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', model: 'qwen-plus' },
  { name: '智谱清言 GLM', url: 'https://open.bigmodel.cn/api/paas/v4/chat/completions', model: 'glm-4-air' },
  { name: 'Kimi（月之暗面）', url: 'https://api.moonshot.cn/v1/chat/completions', model: 'moonshot-v1-8k' },
  { name: '豆包（火山方舟）', url: 'https://ark.cn-beijing.volces.com/api/v3/chat/completions', model: 'doubao-pro-32k' },
  { name: '文心一言（百度千帆）', url: 'https://qianfan.baidubce.com/v2/chat/completions', model: 'ernie-4.0-8k' },
  { name: '自定义/自建中转', url: '', model: '' }
]

const provider = ref('')

const data = reactive({
  form: {
    enabled: false,
    apiUrl: '',
    apiKey: '',
    modelName: 'deepseek-chat',
    monthlyLimit: 1000,
    nl2queryEnabled: false,
    embeddingUrl: '',
    embeddingKey: '',
    embeddingModel: ''
  }
})

const { form } = toRefs(data)

function loadConfig() {
  loading.value = true
  getAiCenterConfig().then(res => {
    const d = res.data
    form.value = {
      enabled: d.enabled === true || d.enabled === 'true',
      apiUrl: d.apiUrl || '',
      apiKey: '',
      modelName: d.modelName || 'deepseek-chat',
      monthlyLimit: parseInt(d.monthlyLimit) || 1000,
      nl2queryEnabled: d.nl2queryEnabled === true || d.nl2queryEnabled === 'true',
      embeddingUrl: d.embeddingUrl || '',
      embeddingKey: '',
      embeddingModel: d.embeddingModel || ''
    }
    originalForm.value = { ...form.value }
    apiKeyConfigured.value = d.apiKeyConfigured === true
    embeddingKeyConfigured.value = d.embeddingKeyConfigured === true
    const embedMatched = embeddingPresets.find(p => p.url && p.url === form.value.embeddingUrl)
    embeddingProvider.value = embedMatched ? embedMatched.name : ''
    monthlyUsed.value = d.monthlyUsed || 0
    monthlyQuestions.value = d.monthlyQuestions ?? null
    const matched = providerPresets.find(p => p.url && p.url === form.value.apiUrl)
    provider.value = matched ? matched.name : ''
    loading.value = false
  }).catch(() => {
    loading.value = false
    proxy.$modal.msgError('AI配置加载失败，页面显示数据可能不准确，请刷新重试')
  })
}

function applyProvider(name) {
  const p = providerPresets.find(item => item.name === name)
  if (!p) return
  if (p.url) form.value.apiUrl = p.url
  if (p.model) form.value.modelName = p.model
}

/** 保存大模型配置（仅主字段，不影响RAG分区） */
function handleSaveMain() {
  saving.value = true
  updateAiCenterConfig({
    enabled: form.value.enabled,
    apiUrl: form.value.apiUrl,
    apiKey: form.value.apiKey,
    modelName: form.value.modelName,
    monthlyLimit: form.value.monthlyLimit,
    nl2queryEnabled: form.value.nl2queryEnabled
  }).then(() => {
    proxy.$modal.msgSuccess('大模型配置保存成功')
    saving.value = false
    originalForm.value = { ...originalForm.value, enabled: form.value.enabled, apiUrl: form.value.apiUrl, modelName: form.value.modelName, monthlyLimit: form.value.monthlyLimit }
    apiKeyConfigured.value = true
  }).catch(() => { saving.value = false })
}

/** 保存RAG向量检索配置（分区保存，仅embedding三项） */
function handleSaveRag() {
  savingRag.value = true
  updateAiEmbeddingConfig({
    embeddingUrl: form.value.embeddingUrl,
    embeddingKey: form.value.embeddingKey,
    embeddingModel: form.value.embeddingModel
  }).then(() => {
    proxy.$modal.msgSuccess('RAG配置保存成功，已即时生效')
    savingRag.value = false
    originalForm.value = { ...originalForm.value, embeddingUrl: form.value.embeddingUrl, embeddingModel: form.value.embeddingModel }
    if (form.value.embeddingKey) embeddingKeyConfigured.value = true
  }).catch(() => { savingRag.value = false })
}

/** 主配置重置：仅还原大模型卡片字段 */
function handleResetMain() {
  form.value.enabled = originalForm.value.enabled
  form.value.apiUrl = originalForm.value.apiUrl
  form.value.apiKey = ''
  form.value.modelName = originalForm.value.modelName
  form.value.monthlyLimit = originalForm.value.monthlyLimit
  form.value.nl2queryEnabled = originalForm.value.nl2queryEnabled
  proxy.$modal.msgSuccess('已还原为最近保存的大模型配置（密钥留空表示沿用已存密钥）')
}

/** RAG重置：仅还原embedding三项 */
function handleResetRag() {
  form.value.embeddingUrl = originalForm.value.embeddingUrl
  form.value.embeddingKey = ''
  form.value.embeddingModel = originalForm.value.embeddingModel
  proxy.$modal.msgSuccess('已还原为最近保存的RAG配置（密钥留空表示沿用已存密钥）')
}

/** 测试Embedding连接（未保存也可测；Key留空沿用已存密钥） */
function handleTestEmbedding() {
  if (!form.value.embeddingUrl) {
    proxy.$modal.msgWarning('请先填写Embedding API地址')
    return
  }
  testingEmbedding.value = true
  testEmbedding({
    embeddingUrl: form.value.embeddingUrl,
    embeddingKey: form.value.embeddingKey,
    embeddingModel: form.value.embeddingModel
  }).then(res => {
    testingEmbedding.value = false
    const d = res.data || {}
    const rows = [
      ['API 地址', form.value.embeddingUrl],
      ['模型名称', form.value.embeddingModel || '（默认）']
    ]
    if (d.success) rows.push(['向量维度', String(d.dims || '?')])
    showResultDialog({
      ok: d.success,
      latencyMs: d.latencyMs,
      subText: d.success ? `响应耗时 ${formatLatency(d.latencyMs)} · 向量化服务可正常调用` : undefined,
      rows,
      error: d.error
    })
  }).catch(() => { testingEmbedding.value = false })
}

function handleReset() {
  form.value = { ...originalForm.value }
}

function handleTest() {
  if (!form.value.apiUrl) {
    proxy.$modal.msgWarning('请先填写大模型API地址')
    return
  }
  testing.value = true
  testAiCenterConfig({ apiUrl: form.value.apiUrl, apiKey: form.value.apiKey, modelName: form.value.modelName }).then(res => {
    testing.value = false
    showTestResult(res.data || {}, { apiUrl: form.value.apiUrl, modelName: form.value.modelName })
  }).catch(() => { testing.value = false })
}

function formatLatency(ms) {
  const v = Number(ms) || 0
  return v < 1000 ? v + ' ms' : (v / 1000).toFixed(1) + ' 秒'
}

/**
 * 通用结构化测试结果弹窗（大模型 / Embedding 共用）
 * @param {Object} opt { ok, latencyMs, rows: [[label, value]], error, subText }
 */
function showResultDialog(opt) {
  const ok = !!opt.ok
  const svgProps = { viewBox: '0 0 24 24', width: 24, height: 24, fill: 'none', stroke: '#fff', 'stroke-width': 3, 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }
  const icon = h('div', { class: ['ai-test-icon', ok ? 'is-ok' : 'is-fail'] }, [
    ok
      ? h('svg', svgProps, [h('path', { d: 'M20 6L9 17l-5-5' })])
      : h('svg', svgProps, [h('line', { x1: 18, y1: 6, x2: 6, y2: 18 }), h('line', { x1: 6, y1: 6, x2: 18, y2: 18 })])
  ])
  const row = (label, value) => h('div', { class: 'ai-test-row' }, [
    h('span', { class: 'ai-test-label' }, label),
    h('span', { class: 'ai-test-value' }, value)
  ])

  const blocks = [
    h('div', { class: 'ai-test-head' }, [
      icon,
      h('div', null, [
        h('div', { class: 'ai-test-title' }, ok ? '连接成功' : '连接失败'),
        h('div', { class: 'ai-test-sub' }, ok
          ? (opt.subText || `响应耗时 ${formatLatency(opt.latencyMs)} · 服务可正常调用`)
          : '请根据下方建议排查后重新测试')
      ])
    ])
  ]

  if (ok) {
    blocks.push(h('div', { class: 'ai-test-card' },
      (opt.rows || []).map(r => row(r[0], r[1]))
    ))
  } else {
    blocks.push(h('div', { class: 'ai-test-card is-error' }, [
      h('div', { class: 'ai-test-block-title' }, '原因与建议'),
      h('div', { class: 'ai-test-err-text' }, opt.error || '连接失败，请检查配置后重试')
    ]))
    if (opt.rows && opt.rows.length) {
      blocks.push(h('div', { class: 'ai-test-card' }, opt.rows.map(r => row(r[0], r[1]))))
    }
  }

  ElMessageBox({
    title: ok ? '测试通过' : '测试失败',
    message: h('div', { class: 'ai-test-result' }, blocks),
    confirmButtonText: '知道了',
    showCancelButton: false,
    customClass: 'ai-test-msgbox',
    center: true
  }).catch(() => {})
}

/** 大模型连接测试（复用通用弹窗） */
function showTestResult(d, ctx) {
  const rows = [
    ['API 地址', ctx.apiUrl],
    ['模型名称', ctx.modelName || '（默认）']
  ]
  if (d.success) rows.push(['模型回复', d.content || d.reply || 'OK'])
  showResultDialog({
    ok: d.success,
    latencyMs: d.latencyMs,
    subText: d.success ? `响应耗时 ${formatLatency(d.latencyMs)} · 大模型可正常调用` : undefined,
    rows,
    error: d.error
  })
}

loadConfig()
</script>

<style scoped>
.ai-config-page .section-card {
  margin-bottom: 16px;
  border-radius: 10px;
}
.ai-config-page .rag-card {
  border-left: 3px solid #95d475;
}
.ai-config-page .ops-card {
  margin-bottom: 4px;
  background: #fafbfc;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.section-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  display: inline-block;
}
.section-dot.dot-green { background: #95d475; }
.section-sub {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 4px;
}
.ops-tip {
  margin-left: 14px;
  font-size: 12px;
  color: #c0c4cc;
}
.form-tip {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
  margin-top: 2px;
}
.form-tip-inline {
  margin-left: 8px;
  color: #999;
  font-size: 13px;
}
</style>

<!-- AI测试结果弹窗样式：MessageBox挂在body下，不能scoped -->
<style>
.ai-test-msgbox {
  width: 480px;
  border-radius: 14px;
  padding-bottom: 4px;
}
.ai-test-msgbox .el-message-box__header {
  padding-bottom: 0;
}
.ai-test-msgbox .el-message-box__title {
  font-size: 15px;
  font-weight: 600;
  color: #606266;
}
.ai-test-result {
  padding: 2px 4px 6px;
}
.ai-test-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 6px 0 16px;
}
.ai-test-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}
.ai-test-icon.is-ok {
  background: linear-gradient(135deg, #34d399, #059669);
  box-shadow: 0 6px 14px rgba(5, 150, 105, 0.28);
}
.ai-test-icon.is-fail {
  background: linear-gradient(135deg, #f87171, #dc2626);
  box-shadow: 0 6px 14px rgba(220, 38, 38, 0.28);
}
.ai-test-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
}
.ai-test-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
}
.ai-test-card {
  background: #f8fafc;
  border: 1px solid #e8edf3;
  border-radius: 10px;
  padding: 10px 14px;
  margin-bottom: 10px;
  text-align: left;
}
.ai-test-card.is-error {
  background: #fef2f2;
  border-color: #fecaca;
}
.ai-test-row {
  display: flex;
  font-size: 13px;
  line-height: 1.9;
}
.ai-test-label {
  color: #909399;
  width: 74px;
  flex: none;
}
.ai-test-value {
  color: #303133;
  word-break: break-all;
}
.ai-test-block-title {
  font-size: 12px;
  color: #dc2626;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 4px;
}
.ai-test-err-text {
  font-size: 13px;
  color: #7f1d1d;
  line-height: 1.8;
}
</style>
