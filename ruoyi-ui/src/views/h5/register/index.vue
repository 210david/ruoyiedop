<template>
  <div class="h5-register-page">
    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <div class="loading-spinner"></div>
      <p>加载活动中...</p>
    </div>

    <!-- 错误提示 -->
    <div v-else-if="loadError" class="error-wrap">
      <div class="error-icon">!</div>
      <p class="error-title">{{ loadError }}</p>
      <p class="error-desc">请检查链接是否正确，或联系活动主办方</p>
    </div>

    <!-- 报名成功 -->
    <div v-else-if="successData" class="success-wrap">
      <!-- 顶部装饰 -->
      <div class="success-hero">
        <div class="success-check">
          <svg viewBox="0 0 52 52" class="checkmark">
            <circle class="checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
            <path class="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
          </svg>
        </div>
        <h2>报名成功</h2>
        <p class="success-subtitle">{{ successData.activityName }}</p>
      </div>

      <!-- 签到二维码 -->
      <div class="qr-card">
        <div class="qr-header">
          <span class="qr-label">您的专属签到码</span>
        </div>
        <div class="qr-code-area">
          <canvas ref="qrCanvas" class="qr-canvas"></canvas>
        </div>
        <div class="sign-in-code-display">
          <span class="code-label">签到码</span>
          <span class="code-value">{{ successData.signInCode }}</span>
        </div>
        <div class="qr-tips">
          <p>请保存此页面，活动当天出示签到码完成签到</p>
        </div>
      </div>

      <!-- 报名信息 -->
      <div class="info-card">
        <div class="info-row">
          <span class="info-label">姓名</span>
          <span class="info-value">{{ successData.contactName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">手机号</span>
          <span class="info-value">{{ successData.contactPhone }}</span>
        </div>
      </div>

      <button class="btn-secondary" @click="goBack">返回活动详情</button>
    </div>

    <!-- 报名表单 -->
    <div v-else class="register-wrap">
      <!-- 活动信息卡片 -->
      <div class="activity-hero">
        <div class="hero-overlay"></div>
        <div class="hero-content">
          <div class="activity-badge">
            <span>{{ activityInfo.activityForm === '0' ? '线上活动' : '线下活动' }}</span>
          </div>
          <h1 class="activity-title">{{ activityInfo.activityName }}</h1>
          <div class="activity-meta">
            <div class="meta-item">
              <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              <span>{{ formatDate(activityInfo.startTime) }} ~ {{ formatDate(activityInfo.endTime) }}</span>
            </div>
            <div class="meta-item" v-if="activityInfo.location">
              <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
              </svg>
              <span>{{ activityInfo.location }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 报名进度 -->
      <div class="progress-card" v-if="activityInfo.targetCount">
        <div class="progress-header">
          <span class="progress-title">报名进度</span>
          <span class="progress-count">{{ activityInfo.signupCount || 0 }} / {{ activityInfo.targetCount }}</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
        <div class="progress-remaining" v-if="activityInfo.remainingCount !== undefined">
          剩余 {{ activityInfo.remainingCount }} 个名额
        </div>
      </div>

      <!-- 活动简介 -->
      <div class="summary-card" v-if="activityInfo.summary">
        <h3 class="card-title">活动简介</h3>
        <p class="summary-text">{{ activityInfo.summary }}</p>
      </div>

      <!-- 报名表单 -->
      <div class="form-card">
        <h3 class="card-title">填写报名信息</h3>
        <form @submit.prevent="handleSubmit" class="register-form">
          <div class="form-group">
            <label class="form-label">企业名称 <span class="required">*</span></label>
            <input
              v-model="form.companyName"
              type="text"
              class="form-input"
              placeholder="请输入企业名称"
              maxlength="100"
            />
          </div>

          <div class="form-group">
            <label class="form-label">姓名 <span class="required">*</span></label>
            <input
              v-model="form.contactName"
              type="text"
              class="form-input"
              placeholder="请输入您的姓名"
              maxlength="20"
            />
          </div>

          <div class="form-group">
            <label class="form-label">手机号 <span class="required">*</span></label>
            <input
              v-model="form.contactPhone"
              type="tel"
              class="form-input"
              placeholder="请输入手机号码"
              maxlength="11"
              @input="form.contactPhone = form.contactPhone.replace(/[^0-9]/g, '')"
            />
          </div>

          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input
              v-model="form.contactEmail"
              type="email"
              class="form-input"
              placeholder="请输入邮箱（选填）"
              maxlength="100"
            />
          </div>

          <div class="form-group">
            <label class="form-label">职位</label>
            <input
              v-model="form.position"
              type="text"
              class="form-input"
              placeholder="请输入职位（选填）"
              maxlength="50"
            />
          </div>

          <div class="form-error" v-if="formError">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="error-svg">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <span>{{ formError }}</span>
          </div>

          <button type="submit" class="btn-submit" :disabled="submitting">
            <span v-if="submitting" class="btn-spinner"></span>
            <span v-else>立即报名</span>
          </button>
        </form>
      </div>

      <!-- 底部安全提示 -->
      <div class="security-note">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="security-icon">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
        </svg>
        <span>您的信息将受到严格保护，仅用于本次活动报名</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import QRCode from 'qrcode'
import { getPublicActivityInfo, submitRegistration } from '@/api/mk/public'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const loadError = ref('')
const submitting = ref(false)
const formError = ref('')
const successData = ref(null)
const activityInfo = ref({})
const qrCanvas = ref(null)

const form = reactive({
  companyName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  position: ''
})

const progressPercent = computed(() => {
  if (!activityInfo.value.targetCount || !activityInfo.value.signupCount) return 0
  return Math.min(100, Math.round((activityInfo.value.signupCount / activityInfo.value.targetCount) * 100))
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

const validateForm = () => {
  formError.value = ''

  if (!form.companyName || form.companyName.trim().length < 2) {
    formError.value = '请输入企业名称（至少2个字符）'
    return false
  }
  if (!form.contactName || form.contactName.trim().length < 2) {
    formError.value = '请输入姓名（至少2个字符）'
    return false
  }
  if (!form.contactPhone || !/^1[3-9]\d{9}$/.test(form.contactPhone)) {
    formError.value = '请输入有效的手机号码'
    return false
  }
  if (form.contactEmail && !/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(form.contactEmail)) {
    formError.value = '请输入有效的邮箱地址'
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true
  formError.value = ''

  try {
    const res = await submitRegistration(
      {
        activityId: parseInt(route.params.activityId),
        companyName: form.companyName.trim(),
        contactName: form.contactName.trim(),
        contactPhone: form.contactPhone.trim(),
        contactEmail: form.contactEmail.trim(),
        position: form.position.trim()
      },
      route.query.token
    )

    successData.value = res.data

    // 生成签到二维码
    await nextTick()
    if (qrCanvas.value) {
      // 二维码内容为签到码，签到时扫描后跳转签到页
      const signInUrl = `${window.location.origin}/h5/signin?code=${res.data.signInCode}`
      await QRCode.toCanvas(qrCanvas.value, signInUrl, {
        width: 200,
        margin: 1,
        color: { dark: '#1a1a2e', light: '#ffffff' }
      })
    }
  } catch (err) {
    formError.value = err.msg || '报名失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  // 重新加载页面回到报名表单
  window.location.reload()
}

onMounted(async () => {
  const activityId = route.params.activityId
  const token = route.query.token

  if (!activityId || !token) {
    loadError.value = '访问链接无效'
    loading.value = false
    return
  }

  try {
    const res = await getPublicActivityInfo(activityId, token)
    activityInfo.value = res.data
  } catch (err) {
    loadError.value = err.msg || '活动信息加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* ===== 基础重置 ===== */
.h5-register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #f8f9fe 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: #1a1a2e;
  -webkit-font-smoothing: antialiased;
}

/* ===== 加载中 ===== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 16px;
  color: #666;
}
.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e0e0e0;
  border-top-color: #4f6ef7;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ===== 错误提示 ===== */
.error-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px;
  text-align: center;
}
.error-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
  color: #fff;
  font-size: 32px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}
.error-title { font-size: 18px; font-weight: 600; margin-bottom: 8px; }
.error-desc { font-size: 14px; color: #999; }

/* ===== 活动Hero ===== */
.activity-hero {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 24px 32px;
  overflow: hidden;
}
.hero-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><circle cx="50" cy="50" r="40" fill="none" stroke="rgba(255,255,255,0.05)" stroke-width="2"/></svg>');
  background-size: 120px;
}
.hero-content { position: relative; z-index: 1; }
.activity-badge {
  display: inline-block;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 20px;
  padding: 4px 14px;
  margin-bottom: 14px;
}
.activity-badge span {
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}
.activity-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  line-height: 1.4;
  margin: 0 0 18px 0;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.activity-meta { display: flex; flex-direction: column; gap: 8px; }
.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255,255,255,0.9);
  font-size: 13px;
}
.meta-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

/* ===== 进度卡片 ===== */
.progress-card {
  margin: -16px 16px 0;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
  position: relative;
  z-index: 2;
}
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.progress-title { font-size: 14px; font-weight: 600; color: #333; }
.progress-count { font-size: 14px; font-weight: 600; color: #4f6ef7; }
.progress-bar {
  height: 8px;
  background: #f0f0f5;
  border-radius: 4px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4f6ef7, #764ba2);
  border-radius: 4px;
  transition: width 0.6s ease;
}
.progress-remaining {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

/* ===== 简介卡片 ===== */
.summary-card {
  margin: 16px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 12px 0;
}
.summary-text {
  font-size: 14px;
  color: #666;
  line-height: 1.7;
  margin: 0;
}

/* ===== 表单卡片 ===== */
.form-card {
  margin: 16px;
  background: #fff;
  border-radius: 16px;
  padding: 24px 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.register-form { display: flex; flex-direction: column; gap: 18px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.required { color: #ff4d4f; }
.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid #e8e8ef;
  border-radius: 12px;
  font-size: 15px;
  color: #1a1a2e;
  background: #fafafe;
  transition: all 0.3s;
  box-sizing: border-box;
  outline: none;
}
.form-input:focus {
  border-color: #4f6ef7;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(79, 110, 247, 0.1);
}
.form-input::placeholder { color: #bbb; }

.form-error {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 10px;
  font-size: 13px;
  color: #ff4d4f;
}
.error-svg { width: 16px; height: 16px; flex-shrink: 0; }

/* ===== 提交按钮 ===== */
.btn-submit {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #4f6ef7, #764ba2);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 6px;
}
.btn-submit:active { transform: scale(0.98); }
.btn-submit:disabled { opacity: 0.7; cursor: not-allowed; }
.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

/* ===== 安全提示 ===== */
.security-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 20px 16px 32px;
  font-size: 12px;
  color: #aaa;
}
.security-icon { width: 14px; height: 14px; flex-shrink: 0; }

/* ===== 成功页面 ===== */
.success-wrap {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 16px 40px;
}
.success-hero {
  width: 100%;
  background: linear-gradient(135deg, #4f6ef7 0%, #764ba2 100%);
  padding: 48px 24px 36px;
  text-align: center;
  border-radius: 0 0 28px 28px;
  margin-bottom: -12px;
  position: relative;
  z-index: 1;
}
.success-check {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
}
.checkmark {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  stroke-width: 2;
  stroke: #fff;
  stroke-miterlimit: 10;
  box-shadow: inset 0 0 0 rgba(255,255,255,0.3);
}
.checkmark-circle {
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  stroke-width: 2;
  stroke-miterlimit: 10;
  stroke: #fff;
  fill: none;
  animation: stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}
.checkmark-check {
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
  animation: stroke 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.6s forwards;
}
@keyframes stroke { to { stroke-dashoffset: 0; } }

.success-hero h2 {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px 0;
}
.success-subtitle {
  font-size: 14px;
  color: rgba(255,255,255,0.85);
  margin: 0;
}

.qr-card {
  width: 100%;
  max-width: 360px;
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.08);
  position: relative;
  z-index: 2;
  text-align: center;
}
.qr-header { margin-bottom: 16px; }
.qr-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.qr-code-area {
  display: flex;
  justify-content: center;
  padding: 16px;
  background: #f8f9fe;
  border-radius: 16px;
  margin-bottom: 16px;
}
.qr-canvas { border-radius: 8px; }
.sign-in-code-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #f0f4ff, #f8f0ff);
  border-radius: 12px;
  margin-bottom: 12px;
}
.code-label {
  font-size: 13px;
  color: #999;
}
.code-value {
  font-size: 24px;
  font-weight: 800;
  color: #4f6ef7;
  letter-spacing: 3px;
}
.qr-tips p {
  font-size: 12px;
  color: #aaa;
  margin: 0;
  line-height: 1.6;
}

.info-card {
  width: 100%;
  max-width: 360px;
  background: #fff;
  border-radius: 16px;
  padding: 16px 20px;
  margin-top: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 14px; color: #999; }
.info-value { font-size: 14px; color: #333; font-weight: 500; }

.btn-secondary {
  width: 100%;
  max-width: 360px;
  padding: 14px;
  margin-top: 20px;
  background: #fff;
  color: #4f6ef7;
  border: 1.5px solid #4f6ef7;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-secondary:active { transform: scale(0.98); }
</style>
