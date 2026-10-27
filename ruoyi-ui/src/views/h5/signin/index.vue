<template>
  <div class="h5-signin-page">
    <!-- 签到结果 -->
    <div v-if="result" class="result-wrap" :class="result.type">
      <div class="result-icon" :class="result.type">
        <svg v-if="result.type === 'success'" viewBox="0 0 52 52" class="result-svg">
          <circle cx="26" cy="26" r="25" fill="none" stroke="currentColor" stroke-width="2"/>
          <path fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="result-svg">
          <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
      </div>
      <h2 class="result-title">{{ result.title }}</h2>
      <p class="result-desc" v-if="result.desc">{{ result.desc }}</p>
      <div class="result-info" v-if="result.data">
        <div class="result-row" v-if="result.data.activityName">
          <span class="result-label">活动</span>
          <span class="result-value">{{ result.data.activityName }}</span>
        </div>
        <div class="result-row" v-if="result.data.contactName">
          <span class="result-label">签到人</span>
          <span class="result-value">{{ result.data.contactName }}</span>
        </div>
        <div class="result-row" v-if="result.data.signTime">
          <span class="result-label">签到时间</span>
          <span class="result-value">{{ formatTime(result.data.signTime) }}</span>
        </div>
      </div>
      <button class="btn-action" @click="reset">继续签到</button>
    </div>

    <!-- 签到输入 -->
    <div v-else class="signin-wrap">
      <div class="signin-hero">
        <div class="hero-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="hero-svg">
            <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
            <circle cx="12" cy="13" r="4"/>
          </svg>
        </div>
        <h1>活动签到</h1>
        <p>请输入签到码或扫描二维码完成签到</p>
      </div>

      <div class="input-card">
        <div class="form-group">
          <label class="form-label">签到码</label>
          <input
            v-model="signInCode"
            type="text"
            class="code-input"
            placeholder="请输入8位签到码"
            maxlength="8"
            @input="signInCode = signInCode.toUpperCase().replace(/[^A-Z0-9]/g, '')"
            @keyup.enter="handleSignIn"
          />
        </div>

        <div class="input-error" v-if="error">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="error-svg">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>{{ error }}</span>
        </div>

        <button class="btn-signin" :disabled="submitting || !signInCode" @click="handleSignIn">
          <span v-if="submitting" class="btn-spinner"></span>
          <span v-else>确认签到</span>
        </button>
      </div>

      <div class="tips-card">
        <h3 class="tips-title">签到说明</h3>
        <ul class="tips-list">
          <li>签到码为报名成功后系统生成的8位字符</li>
          <li>签到码可在报名成功页面的二维码中获取</li>
          <li>每个签到码仅可使用一次</li>
          <li>签到开放时间：活动开始前1小时 ~ 活动结束</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { signInByCode } from '@/api/mk/public'

const route = useRoute()

const signInCode = ref('')
const submitting = ref(false)
const error = ref('')
const result = ref(null)

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

const handleSignIn = async () => {
  if (!signInCode.value || signInCode.value.length !== 8) {
    error.value = '请输入8位签到码'
    return
  }

  submitting.value = true
  error.value = ''

  try {
    const res = await signInByCode(signInCode.value)
    result.value = {
      type: 'success',
      title: '签到成功',
      desc: '欢迎参加本次活动',
      data: res.data
    }
  } catch (err) {
    result.value = {
      type: 'error',
      title: '签到失败',
      desc: err.msg || '请检查签到码是否正确'
    }
  } finally {
    submitting.value = false
  }
}

const reset = () => {
  result.value = null
  signInCode.value = ''
  error.value = ''
}

onMounted(() => {
  // 如果URL中携带了签到码（扫码进入），自动签到
  const code = route.query.code
  if (code) {
    signInCode.value = code.toUpperCase()
    handleSignIn()
  }
})
</script>

<style scoped>
.h5-signin-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #f8f9fe 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: #1a1a2e;
  -webkit-font-smoothing: antialiased;
}

/* ===== 签到输入页 ===== */
.signin-wrap {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0 16px 40px;
}

.signin-hero {
  text-align: center;
  padding: 60px 0 32px;
}
.hero-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #4f6ef7, #764ba2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(79, 110, 247, 0.3);
}
.hero-svg {
  width: 36px;
  height: 36px;
  color: #fff;
}
.signin-hero h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #1a1a2e;
}
.signin-hero p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.input-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px 20px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
  margin-bottom: 20px;
}
.form-group { margin-bottom: 16px; }
.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}
.code-input {
  width: 100%;
  padding: 16px;
  border: 2px solid #e8e8ef;
  border-radius: 12px;
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  letter-spacing: 8px;
  color: #4f6ef7;
  background: #fafafe;
  outline: none;
  transition: all 0.3s;
  box-sizing: border-box;
}
.code-input:focus {
  border-color: #4f6ef7;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(79, 110, 247, 0.1);
}
.code-input::placeholder {
  font-size: 15px;
  font-weight: 400;
  letter-spacing: 1px;
  color: #ccc;
}

.input-error {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 10px;
  font-size: 13px;
  color: #ff4d4f;
  margin-bottom: 16px;
}
.error-svg { width: 16px; height: 16px; flex-shrink: 0; }

.btn-signin {
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
}
.btn-signin:active { transform: scale(0.98); }
.btn-signin:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.tips-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.tips-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin: 0 0 12px 0;
}
.tips-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #999;
  line-height: 2;
}

/* ===== 签到结果页 ===== */
.result-wrap {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  text-align: center;
}
.result-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}
.result-icon.success {
  background: linear-gradient(135deg, #52c41a, #389e0d);
  color: #fff;
  box-shadow: 0 8px 24px rgba(82, 196, 26, 0.3);
}
.result-icon.error {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
  color: #fff;
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.3);
}
.result-svg { width: 48px; height: 48px; }
.result-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px 0;
}
.result-desc {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px 0;
}
.result-info {
  width: 100%;
  max-width: 320px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  margin-bottom: 24px;
}
.result-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}
.result-row:last-child { border-bottom: none; }
.result-label { font-size: 14px; color: #999; }
.result-value { font-size: 14px; color: #333; font-weight: 500; }

.btn-action {
  width: 100%;
  max-width: 320px;
  padding: 14px;
  background: #fff;
  color: #4f6ef7;
  border: 1.5px solid #4f6ef7;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-action:active { transform: scale(0.98); }
</style>
