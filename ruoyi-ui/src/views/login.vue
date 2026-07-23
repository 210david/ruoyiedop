<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="deco-circle circle-1"></div>
      <div class="deco-circle circle-2"></div>
      <div class="deco-circle circle-3"></div>
      <div class="deco-ring ring-1"></div>
      <div class="deco-ring ring-2"></div>
    </div>

    <!-- 主卡片容器 -->
    <div class="login-card">
      <!-- 左侧信息栏 -->
      <div class="info-section">
        <div class="info-content">
          <!-- Logo -->
          <div class="logo-wrap">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9 22 9 12 15 12 15 22"></polyline>
              </svg>
            </div>
            <div class="logo-text">
              <span class="logo-main">EDOP</span>
              <span class="logo-sub">ENTERPRISE DIGITAL OPERATION PLATFORM</span>
            </div>
          </div>

          <!-- 主标题 -->
          <h1 class="main-title">
            <span class="title-white">企业数字化</span>
            <span class="title-blue">运营平台</span>
          </h1>

          <!-- 描述 -->
          <p class="desc-text">
            整合企业核心业务流程，实现数据驱动决策，提升运营效率，助力企业数字化转型。
          </p>

          <!-- 功能列表 -->
          <ul class="feature-list">
            <li class="feature-item">
              <span class="check-icon">✓</span>
              <span>全链路业务协同管理</span>
            </li>
            <li class="feature-item">
              <span class="check-icon">✓</span>
              <span>实时数据智能分析</span>
            </li>
            <li class="feature-item">
              <span class="check-icon">✓</span>
              <span>多维度运营决策支持</span>
            </li>
          </ul>
        </div>

        <!-- 版权 -->
        <div class="copyright">
          © 2026 EDOP Enterprise Platform. All rights reserved.
        </div>
      </div>

      <!-- 右侧登录栏 -->
      <div class="login-section">
        <div class="login-content">
          <!-- 欢迎区 -->
          <div class="welcome-area">
            <h2 class="welcome-title">欢迎回来</h2>
            <p class="welcome-subtitle">请登录您的账户以继续</p>
          </div>

          <!-- 登录表单 -->
          <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
            <!-- 用户名 -->
            <el-form-item prop="username">
              <div class="custom-input-wrapper">
                <svg class="input-prefix-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                <el-input
                  v-model="loginForm.username"
                  type="text"
                  auto-complete="off"
                  placeholder="请输入用户名"
                  class="dark-input"
                />
              </div>
            </el-form-item>

            <!-- 密码 -->
            <el-form-item prop="password">
              <div class="custom-input-wrapper">
                <svg class="input-prefix-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  auto-complete="off"
                  placeholder="请输入密码"
                  class="dark-input"
                  show-password
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>

            <!-- 验证码 -->
            <el-form-item prop="code" v-if="captchaEnabled">
              <div class="captcha-wrapper">
                <div class="custom-input-wrapper captcha-input">
                  <svg class="input-prefix-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                    <line x1="9" y1="9" x2="15" y2="15"></line>
                    <line x1="15" y1="9" x2="9" y2="15"></line>
                  </svg>
                  <el-input
                    v-model="loginForm.code"
                    auto-complete="off"
                    placeholder="请输入验证码"
                    class="dark-input"
                    @keyup.enter="handleLogin"
                  />
                </div>
                <div class="captcha-img" @click="getCode">
                  <img :src="codeUrl" alt="验证码" />
                </div>
              </div>
            </el-form-item>

            <!-- 选项 -->
            <div class="form-options">
              <el-checkbox v-model="loginForm.rememberMe" class="custom-checkbox">
                <span class="checkbox-label">记住密码</span>
              </el-checkbox>
              <a href="javascript:void(0)" class="forgot-link" @click="handleForgot">忘记密码?</a>
            </div>

            <!-- 登录按钮 -->
            <el-form-item class="btn-item">
              <el-button
                :loading="loading"
                type="primary"
                class="submit-btn"
                @click.prevent="handleLogin"
              >
                <span v-if="!loading">
                  登录
                  <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"></polyline>
                  </svg>
                </span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 版本 -->
          <div class="version-text">EDOP Enterprise Platform v1.0</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入用户名" }],
  password: [{ required: true, trigger: "blur", message: "请输入密码" }]
}

const loading = ref(false)
const redirect = ref(undefined)

// 验证码相关
const codeUrl = ref("")
const captchaEnabled = ref(true)

// 获取验证码
function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

// 页面加载时检查记住的用户名密码并获取验证码
onMounted(() => {
  getCookie()
  getCode()
})

// 获取Cookie中的记住密码信息
function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  
  if (username && rememberMe) {
    loginForm.value.username = username
    loginForm.value.rememberMe = Boolean(rememberMe)
    
    // 如果存在密码，解密后填充
    if (password) {
      try {
        loginForm.value.password = decrypt(password)
      } catch (e) {
        console.error('密码解密失败', e)
        // 解密失败时清除密码
        loginForm.value.password = ''
      }
    }
  } else {
    // 如果没有记住密码，检查 localStorage 中的用户名
    const rememberedUsername = localStorage.getItem('rememberedUsername')
    if (rememberedUsername) {
      loginForm.value.username = rememberedUsername
      loginForm.value.rememberMe = true
    }
  }
}

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      
      // 记住密码逻辑
      if (loginForm.value.rememberMe) {
        localStorage.setItem('rememberedUsername', loginForm.value.username)
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        localStorage.removeItem('rememberedUsername')
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        getCode()
      })
    } else {
      ElMessage.warning('请填写用户名和密码')
    }
  })
}

function handleForgot() {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style lang="scss" scoped>
// 外层页面
.login-page {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #0A1430 0%, #1A143A 50%, #0A1430 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

// 背景装饰
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(64, 169, 255, 0.1);

  &.circle-1 {
    width: 300px;
    height: 300px;
    top: -100px;
    left: -100px;
  }

  &.circle-2 {
    width: 200px;
    height: 200px;
    bottom: 10%;
    left: 20%;
  }

  &.circle-3 {
    width: 150px;
    height: 150px;
    top: 20%;
    right: 10%;
  }
}

.deco-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(64, 169, 255, 0.1);

  &.ring-1 {
    width: 400px;
    height: 400px;
    top: -150px;
    right: -100px;
  }

  &.ring-2 {
    width: 250px;
    height: 250px;
    bottom: -80px;
    left: 30%;
  }
}

// 主卡片
.login-card {
  width: 90%;
  max-width: 1200px;
  aspect-ratio: 16 / 9;
  max-height: 90vh;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

// 左侧信息栏
.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 50px;
  background: linear-gradient(135deg, rgba(22, 37, 112, 0.6) 0%, rgba(64, 42, 128, 0.4) 100%);
}

.info-content {
  max-width: 450px;
}

// Logo
.logo-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 50px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  color: #fff;

  svg {
    width: 100%;
    height: 100%;
  }
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-main {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
  line-height: 1;
}

.logo-sub {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 0.5px;
  margin-top: 4px;
}

// 主标题
.main-title {
  margin: 0 0 20px 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.title-white {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 4px;
  line-height: 1.2;
}

.title-blue {
  font-size: 32px;
  font-weight: 700;
  color: #40A9FF;
  line-height: 1.2;
}

// 描述
.desc-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.8;
  margin: 0 0 30px 0;
}

// 功能列表
.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.check-icon {
  width: 20px;
  height: 20px;
  background: rgba(64, 169, 255, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3684FF;
  font-size: 11px;
  flex-shrink: 0;
}

// 版权
copyright {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

// 右侧登录栏
.login-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px;
  background: rgba(10, 20, 48, 0.5);
}

.login-content {
  width: 100%;
  max-width: 350px;
}

// 欢迎区
.welcome-area {
  margin-bottom: 30px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0;
}

// 表单
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;

    &.btn-item {
      margin-top: 24px;
      margin-bottom: 0;
    }
  }
}

// 自定义输入框
.custom-input-wrapper {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 0 14px;
  height: 48px;
  transition: all 0.3s;

  &:hover {
    border-color: rgba(64, 169, 255, 0.5);
  }

  &:focus-within {
    border-color: #3684FF;
    background: rgba(255, 255, 255, 0.08);
  }

  .input-prefix-icon {
    width: 18px;
    height: 18px;
    color: rgba(255, 255, 255, 0.4);
    flex-shrink: 0;
    margin-right: 10px;
  }
}

// 深色输入框 - 强制覆盖 Element Plus 样式
:deep(.dark-input) {
  flex: 1;

  .el-input__wrapper {
    background-color: transparent !important;
    background: transparent !important;
    box-shadow: none !important;
    padding: 0 !important;
  }

  .el-input__inner {
    height: 46px;
    color: #fff !important;
    font-size: 14px;
    background: transparent !important;

    &::placeholder {
      color: rgba(255, 255, 255, 0.3) !important;
    }
  }

  // 密码框特殊处理
  &.el-input--password {
    .el-input__wrapper {
      background-color: transparent !important;
      background: transparent !important;
    }
  }
}

// 选项
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.custom-checkbox {
  :deep(.el-checkbox__input) {
    .el-checkbox__inner {
      background: transparent;
      border-color: rgba(255, 255, 255, 0.3);
      width: 14px;
      height: 14px;

      &::after {
        border-color: #3684FF;
        border-width: 2px;
      }
    }

    &.is-checked .el-checkbox__inner {
      background: transparent;
      border-color: #3684FF;
    }
  }

  :deep(.el-checkbox__label) {
    padding-left: 6px;
  }
}

.checkbox-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.forgot-link {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: color 0.2s;

  &:hover {
    color: #3684FF;
  }
}

// 登录按钮
.submit-btn {
  width: 100%;
  height: 48px;
  background: #3684FF;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #fff;
  transition: all 0.3s;

  &:hover {
    background: #2a75e6;
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(54, 132, 255, 0.4);
  }

  span {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }

  .btn-icon {
    width: 16px;
    height: 16px;
  }
}

// 版本
.version-text {
  text-align: center;
  margin-top: 30px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

// 验证码区域
.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;

  .captcha-input {
    flex: 1;
  }

  .captcha-img {
    width: 120px;
    height: 48px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    flex-shrink: 0;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s;

    &:hover {
      border-color: rgba(64, 169, 255, 0.5);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

// 响应式
@media (max-width: 900px) {
  .login-card {
    flex-direction: column;
    width: 95%;
    max-height: 90vh;
    aspect-ratio: auto;
    overflow-y: auto;
  }

  .info-section,
  .login-section {
    padding: 30px 40px;
  }

  .title-white {
    font-size: 32px;
  }

  .title-blue {
    font-size: 24px;
  }

  .info-section {
    min-height: auto;
  }
}
</style>
