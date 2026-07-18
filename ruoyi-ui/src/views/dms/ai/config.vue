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

        <el-form-item label="大模型API地址" v-show="form.enabled">
          <el-input v-model="form.apiUrl" placeholder="如 https://api.deepseek.com/v1/chat/completions" />
          <div class="form-tip">兼容OpenAI格式的API地址</div>
        </el-form-item>

        <el-form-item label="大模型API Key" v-show="form.enabled">
          <el-input v-model="form.apiKey" type="password" show-password placeholder="填入您的API密钥" />
          <div class="form-tip">在模型服务商后台获取</div>
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
          <el-button @click="handleReset">重 置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="DmsAiConfig">
import { getAiConfig, updateAiConfig } from '@/api/dms/aiConfig'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const saving = ref(false)
const originalForm = ref({})

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
      apiKey: d.apiKey || '',
      modelName: d.modelName || 'deepseek-chat',
      repairAssistant: d.repairAssistant === true || d.repairAssistant === 'true',
      dispatchRecommend: d.dispatchRecommend === true || d.dispatchRecommend === 'true',
      knowledgeQa: d.knowledgeQa === true || d.knowledgeQa === 'true',
      monthlyLimit: parseInt(d.monthlyLimit) || 1000
    }
    originalForm.value = { ...form.value }
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
