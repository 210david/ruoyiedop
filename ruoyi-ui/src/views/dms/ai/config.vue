<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span style="font-size: 16px; font-weight: 600">AI 功能配置</span>
          <el-tag type="info" size="small">配置保存后立即生效</el-tag>
        </div>
      </template>

      <el-form :model="form" label-width="180px" style="max-width: 700px" v-loading="loading">
        <!-- 总开关 -->
        <el-divider content-position="left">基础配置</el-divider>
        <el-form-item label="AI功能总开关">
          <el-switch v-model="form.enabled" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">关闭后所有AI功能不可用，降级为关键词匹配</div>
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
          <el-input v-model="form.apiUrl" placeholder="如 https://api.deepseek.com/v1/chat/completions" />
          <div class="form-tip">兼容OpenAI格式的API地址</div>
        </el-form-item>

        <el-form-item label="大模型API Key" v-show="form.enabled">
          <el-input v-model="form.apiKey" type="password" autocomplete="new-password"
            :placeholder="apiKeyConfigured ? '已配置（不可查看），留空则继续使用原密钥' : '填入您的API密钥'" />
          <div class="form-tip">在模型服务商后台获取；保存后系统不回显，留空表示沿用原密钥</div>
        </el-form-item>

        <el-form-item label="模型名称" v-show="form.enabled">
          <el-input v-model="form.modelName" placeholder="如 deepseek-chat / qwen-plus / ernie-bot" />
          <div class="form-tip">不同服务商的模型名称不同</div>
        </el-form-item>

        <el-form-item label="月度调用上限" v-show="form.enabled">
          <el-input-number v-model="form.monthlyLimit" :min="0" :max="100000" controls-position="right" style="width: 200px" />
          <span class="form-tip-inline">次/月</span>
          <div class="form-tip">超出上限后自动关闭AI功能</div>
        </el-form-item>

        <!-- 功能开关 -->
        <el-divider content-position="left">功能开关</el-divider>

        <el-form-item label="智能报修助手" v-show="form.enabled">
          <el-switch v-model="form.repairAssistant" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">自然语言描述故障，AI自动识别填充工单字段</div>
        </el-form-item>

        <el-form-item label="智能派工推荐" v-show="form.enabled">
          <el-switch v-model="form.dispatchRecommend" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">基于负载+绩效+响应速度推荐维修人</div>
        </el-form-item>

        <el-form-item label="维修知识问答" v-show="form.enabled">
          <el-switch v-model="form.knowledgeQa" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">基于历史工单+FAQ生成维修参考回答</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保 存 配 置</el-button>
          <el-button type="success" plain @click="handleTest" v-show="form.enabled" :loading="testing">测 试 连 接</el-button>
          <el-button @click="handleReset">重 置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="DmsAiConfig">
import { getAiConfig, updateAiConfig, testAiConfig } from '@/api/dms/aiConfig'
import { ElMessageBox } from 'element-plus'
import { h } from 'vue'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const saving = ref(false)
const testing = ref(false)
const apiKeyConfigured = ref(false)
const originalForm = ref({})

// 主流大模型服务商预设（OpenAI兼容格式），选择后自动填入，仍可手动修改
const providerPresets = [
  { name: 'DeepSeek（深度求索）', url: 'https://api.deepseek.com/v1/chat/completions', model: 'deepseek-chat' },
  { name: '通义千问（阿里百炼）', url: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', model: 'qwen-plus' },
  { name: '智谱清言 GLM', url: 'https://open.bigmodel.cn/api/paas/v4/chat/completions', model: 'glm-4-air' },
  { name: 'Kimi（月之暗面）', url: 'https://api.moonshot.cn/v1/chat/completions', model: 'moonshot-v1-8k' },
  { name: '豆包（火山方舟）', url: 'https://ark.cn-beijing.volces.com/api/v3/chat/completions', model: 'doubao-pro-32k' },
  { name: '文心一言（百度千帆）', url: 'https://qianfan.baidubce.com/v2/chat/completions', model: 'ernie-4.0-8k' },
  { name: 'Kimi/自建中转（自定义）', url: '', model: '' }
]

const provider = ref('')

/** 选择服务商后自动填入API地址与模型名 */
function applyProvider(name) {
  const p = providerPresets.find(item => item.name === name)
  if (!p) return
  if (p.url) form.value.apiUrl = p.url
  if (p.model) form.value.modelName = p.model
}

/** 测试大模型连接（使用当前表单配置，未保存也可测试） */
function handleTest() {
  if (!form.value.apiUrl) {
    proxy.$modal.msgWarning('请先填写大模型API地址')
    return
  }
  testing.value = true
  testAiConfig({ apiUrl: form.value.apiUrl, apiKey: form.value.apiKey, modelName: form.value.modelName }).then(res => {
    testing.value = false
    showTestResult(res.data || {}, { apiUrl: form.value.apiUrl, modelName: form.value.modelName })
  }).catch(() => { testing.value = false })
}

/** 格式化响应耗时：1秒内显示毫秒，否则显示秒 */
function formatLatency(ms) {
  const v = Number(ms) || 0
  return v < 1000 ? v + ' ms' : (v / 1000).toFixed(1) + ' 秒'
}

/** 测试结果弹窗：状态图标 + 明细卡片 + 原因建议，结构化展示 */
function showTestResult(d, ctx) {
  const ok = !!d.success
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
          ? `响应耗时 ${formatLatency(d.latencyMs)} · 大模型可正常调用`
          : '请根据下方建议排查后重新测试')
      ])
    ])
  ]

  if (ok) {
    blocks.push(h('div', { class: 'ai-test-card' }, [
      row('API 地址', ctx.apiUrl),
      row('模型名称', ctx.modelName || '（默认）'),
      row('模型回复', d.reply || 'OK')
    ]))
  } else {
    blocks.push(h('div', { class: 'ai-test-card is-error' }, [
      h('div', { class: 'ai-test-block-title' }, '原因与建议'),
      h('div', { class: 'ai-test-err-text' }, d.error || '连接失败，请检查API地址、密钥和模型名称')
    ]))
    blocks.push(h('div', { class: 'ai-test-card' }, [
      row('API 地址', ctx.apiUrl),
      row('模型名称', ctx.modelName || '（默认）')
    ]))
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

const data = reactive({
  form: {
    enabled: false,
    apiUrl: '',
    apiKey: '',
    modelName: 'deepseek-chat',
    repairAssistant: false,
    dispatchRecommend: false,
    knowledgeQa: false,
    monthlyLimit: 1000
  }
})

const { form } = toRefs(data)

function loadConfig() {
  loading.value = true
  getAiConfig().then(res => {
    const d = res.data
    form.value = {
      enabled: d.enabled === true || d.enabled === 'true',
      apiUrl: d.apiUrl || '',
      apiKey: '',
      modelName: d.modelName || 'deepseek-chat',
      repairAssistant: d.repairAssistant === true || d.repairAssistant === 'true',
      dispatchRecommend: d.dispatchRecommend === true || d.dispatchRecommend === 'true',
      knowledgeQa: d.knowledgeQa === true || d.knowledgeQa === 'true',
      monthlyLimit: parseInt(d.monthlyLimit) || 1000
    }
    originalForm.value = { ...form.value }
    apiKeyConfigured.value = d.apiKeyConfigured === true
    // 回显服务商下拉（按已保存API地址匹配预设）
    const matched = providerPresets.find(p => p.url && p.url === form.value.apiUrl)
    provider.value = matched ? matched.name : ''
    loading.value = false
  })
}

function handleSave() {
  saving.value = true
  updateAiConfig(form.value).then(() => {
    proxy.$modal.msgSuccess('配置保存成功')
    saving.value = false
    originalForm.value = { ...form.value }
  }).catch(() => { saving.value = false })
}

function handleReset() {
  form.value = { ...originalForm.value }
}

loadConfig()
</script>

<style scoped>
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
