<template>
  <div class="app-container">
    <el-row :gutter="0">
      <el-col :span="14" :offset="5">
        <el-card shadow="hover" class="config-card">
          <template #header>
            <div class="config-header">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 8px; vertical-align: middle;"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
              <span style="font-size: 16px; font-weight: 600;">企业信息查询配置</span>
            </div>
          </template>

          <el-form ref="configRef" :model="form" label-width="100px" v-loading="loading" style="margin-top: 10px;">
            <el-form-item label="启用状态">
              <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" style="width: 200px" />
            </el-form-item>

            <el-form-item label="API提供商">
              <el-select v-model="form.provider" placeholder="请选择" style="width: 100%">
                <el-option label="天眼查" value="tianyancha" />
                <el-option label="企查查" value="qichacha" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>

            <el-form-item label="API地址">
              <el-input v-model="form.apiUrl" placeholder="如 https://open.tianyancha.com/cloud-originapi/companyinfo" />
            </el-form-item>

            <el-form-item label="API密钥">
              <el-input v-model="form.apiKey" type="password" show-password placeholder="输入API Key" />
            </el-form-item>

            <el-form-item label="API Secret" v-if="form.provider === 'qichacha'">
              <el-input v-model="form.apiSecret" type="password" show-password placeholder="企查查专用：输入API Secret（用于Token鉴权）" />
              <div style="color: #999; font-size: 12px; margin-top: 5px;">
                企查查需要使用 SecretKey 计算 Token，格式：MD5(Key + Timespan + SecretKey)
              </div>
            </el-form-item>

            <el-divider />

            <el-form-item label="测试查询">
              <div style="display: flex; gap: 8px; width: 100%">
                <el-input v-model="testKeyword" placeholder="请输入统一社会信用代码查询" style="flex: 1" @keyup.enter="handleTest" />
                <el-button type="primary" plain icon="Search" :loading="testLoading" @click="handleTest">测试</el-button>
              </div>
            </el-form-item>

            <div v-if="testResult" style="margin: 0 0 16px 100px;">
              <el-alert :title="testSuccess ? '查询成功' : '查询失败'" :type="testSuccess ? 'success' : 'error'" :closable="false" show-icon style="margin-bottom: 10px" />
              <div v-if="testSuccess" class="rd-page"><section class="rd-card"><div class="rd-card-body" style="display:block"><div class="rd-grid">
                <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ testResult.enterpriseName || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">法人代表</span><div class="rd-value">{{ testResult.legalPerson || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">注册资本</span><div class="rd-value">{{ testResult.registeredCapital || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">成立日期</span><div class="rd-value">{{ testResult.establishedDate || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">统一信用代码</span><div class="rd-value">{{ testResult.creditCode || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">企业状态</span><div class="rd-value">{{ testResult.enterpriseStatus || '-' }}</div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">注册地址</span><div class="rd-value">{{ testResult.address || '-' }}</div></div>
              </div></div></section></div>
            </div>
          </el-form>

          <div style="text-align: center; padding: 10px 0;">
            <el-button type="primary" @click="handleSave" :loading="saving">保 存 配 置</el-button>
            <el-button @click="handleReset">重 置</el-button>
          </div>

          <el-divider />
          <div style="padding: 0 20px; color: #999; font-size: 13px; line-height: 1.8;">
            <p><strong>使用说明：</strong></p>
            <p>1. 选择API提供商（天眼查/企查查/自定义），填写API地址和密钥</p>
            <p>2. 保存配置后，供应商信息页面中的「查询企业」按钮将调用真实API自动回填企业信息</p>
            <p>3. 可使用「测试查询」验证API配置是否正确</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="PmsEnterpriseConfig">
import { getEnterpriseConfig, saveEnterpriseConfig, testEnterpriseLookup } from '@/api/pms/enterprise'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const saving = ref(false)
const testLoading = ref(false)
const testKeyword = ref('')
const testResult = ref(null)
const testSuccess = ref(false)

const form = reactive({
  enabled: true,
  provider: 'tianyancha',
  apiUrl: '',
  apiKey: '',
  apiSecret: ''
})

function loadConfig() {
  loading.value = true
  getEnterpriseConfig().then(res => {
    const data = res.data || {}
    form.enabled = data.enabled === 'true'
    form.provider = data.provider || 'tianyancha'
    form.apiUrl = data.apiUrl || ''
    form.apiKey = data.apiKey || ''
    form.apiSecret = data.apiSecret || ''
  }).finally(() => {
    loading.value = false
  })
}

function handleSave() {
  saving.value = true
  const data = {
    enabled: form.enabled ? 'true' : 'false',
    provider: form.provider,
    apiUrl: form.apiUrl,
    apiKey: form.apiKey,
    apiSecret: form.apiSecret
  }
  saveEnterpriseConfig(data).then(() => {
    proxy.$modal.msgSuccess('配置保存成功')
  }).finally(() => {
    saving.value = false
  })
}

function handleTest() {
  if (!testKeyword.value?.trim()) {
    proxy.$modal.msgWarning('请输入统一社会信用代码')
    return
  }
  testLoading.value = true
  testResult.value = null
  testEnterpriseLookup(testKeyword.value).then(res => {
    testResult.value = res.data
    testSuccess.value = true
    proxy.$modal.msgSuccess('查询成功')
  }).catch(err => {
    testResult.value = { error: err.message }
    testSuccess.value = false
  }).finally(() => {
    testLoading.value = false
  })
}

function handleReset() {
  loadConfig()
  testResult.value = null
  testKeyword.value = ''
}

loadConfig()
</script>

<style scoped>
.config-card {
  max-width: 700px;
  margin: 0 auto;
}
.config-header {
  display: flex;
  align-items: center;
}
</style>
