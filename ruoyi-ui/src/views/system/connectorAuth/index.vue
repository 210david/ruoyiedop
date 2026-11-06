<template>
  <div class="app-container connector-auth-page">
    <div class="auth-card">
      <!-- ===== 头部：徽标 + 标题 ===== -->
      <div class="brand-row">
        <div class="brand-badge edop">EDOP</div>
        <div class="brand-link">
          <svg viewBox="0 0 24 24" width="22" height="22">
            <path d="M4 9h13l-3-3M20 15H7l3 3" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <div class="brand-badge wb">WB</div>
      </div>
      <h1 class="page-title">连接 WorkBuddy 助手</h1>
      <p class="page-desc">
        使用您的 EDOP 登录身份，一键获取连接器专属令牌。<br />
        数据范围与您本人在 EDOP 中的权限完全一致（只读查询）。
      </p>

      <!-- ===== 未授权：一键授权 ===== -->
      <div class="section-box" v-if="!authResult">
        <div class="grant-row">
          <el-button type="primary" class="grant-btn" :loading="authorizing" @click="handleAuth">
            {{ authorizing ? '授权中...' : '一键授权' }}
          </el-button>
        </div>
        <div class="grant-tip">已识别当前登录用户，无需输入账号密码；授权来源默认记录为「{{ clientDesc }}」，令牌有效期 90 天自动续期，可随时吊销。</div>
      </div>

      <!-- ===== 授权成功：配置展示 ===== -->
      <div class="section-box token-section" v-else>
        <div class="warn-box">
          <span class="warn-icon">⚠</span>
          <span>以下配置含您的专属令牌，<b>仅显示这一次，请立即复制保存</b>（令牌有效期 90 天自动续期，管理员可随时吊销）</span>
        </div>

        <div class="usage-line">
          使用方法：WorkBuddy → 自定义连接器 → <span class="kbd">配置 MCP</span> → 整段粘贴以下内容 → 保存启用
        </div>

        <pre class="code-block">{{ mcpConfigJson }}</pre>

        <el-button type="primary" class="copy-main-btn" :class="{ copied: copied }" @click="copyConfig">
          {{ copied ? '✓ 已复制，去 WorkBuddy 粘贴即可' : '复制配置，去 WorkBuddy 粘贴' }}
        </el-button>

        <div class="merge-hint">（若编辑器中已有其他连接器条目，请只把 "edop-assistant": {...} 这一块合并进去）</div>

        <div class="token-label">您的专属令牌（仅用于向管理员报修时核对身份）</div>
        <div class="token-box" @click="copyText(authResult.grantToken, '令牌已复制')">{{ authResult.grantToken }}</div>

        <div class="again-row">
          <el-button link type="primary" @click="resetResult">再次授权（如更换电脑）</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 我的授权 ===== -->
    <div class="auth-card list-card">
      <div class="list-head">
        <span class="list-title">我的授权</span>
        <span class="list-hint">换电脑、怀疑泄露时请吊销后重新授权</span>
        <el-button icon="Refresh" circle size="small" @click="getMyGrants" />
      </div>

      <el-table v-loading="myLoading" :data="myGrants" class="list-table" size="default">
        <el-table-column label="授权来源" prop="clientDesc" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="90" align="center">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="最近使用" prop="lastUsedTime" width="170" align="center">
          <template #default="scope">
            <span>{{ parseTime(scope.row.lastUsedTime) || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="过期时间" prop="expireTime" width="170" align="center">
          <template #default="scope">
            <span>{{ parseTime(scope.row.expireTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button link type="danger" :disabled="scope.row.status === '1'" @click="handleRevoke(scope.row)">吊销</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!myLoading && myGrants.length === 0" description="暂无授权，点击上方「一键授权」发起首次授权" :image-size="72" />
    </div>
  </div>
</template>

<script setup name="ConnectorAuth">
import { createMyGrant, getConnectorConfig, listMyGrant, revokeMyGrant } from "@/api/system/connectorGrant"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const authorizing = ref(false)
const authResult = ref(null)
const copied = ref(false)
const gatewayUrl = ref('')
const clientDesc = ref('WorkBuddy')
const myGrants = ref([])
const myLoading = ref(false)

/** MCP 客户端配置 JSON（与网关授权页生成结构一致） */
const mcpConfigJson = computed(() => {
  if (!authResult.value || !gatewayUrl.value) return ''
  return JSON.stringify({
    mcpServers: {
      "edop-assistant": {
        type: "streamablehttp",
        url: gatewayUrl.value.replace(/\/+$/, '') + '/mcp',
        headers: { Authorization: 'Bearer ' + authResult.value.grantToken }
      }
    }
  }, null, 2)
})

/** 下发网关地址 */
function loadConfig() {
  getConnectorConfig().then(res => {
    gatewayUrl.value = res.gatewayUrl || ''
  })
}

/** 一键授权：基于当前登录态直接颁发 */
function handleAuth() {
  authorizing.value = true
  copied.value = false
  createMyGrant({ clientDesc: clientDesc.value || 'WorkBuddy' }).then(res => {
    if (res.code === 200 && res.grantToken) {
      authResult.value = { grantToken: res.grantToken, expireTime: res.expireTime }
      proxy.$modal.msgSuccess('授权成功，请立即复制保存配置')
      getMyGrants()
    } else {
      proxy.$modal.msgError(res.msg || '授权失败')
    }
  }).catch(err => {
    proxy.$modal.msgError('授权请求失败：' + (err && err.message ? err.message : '网络或后端服务异常，请确认后端已重启至最新版本'))
  }).finally(() => {
    authorizing.value = false
  })
}

/** 复制整段 MCP 配置 */
function copyConfig() {
  copyText(mcpConfigJson.value, '配置已复制')
  copied.value = true
}

/** 复制文本 */
function copyText(text, tip) {
  navigator.clipboard.writeText(text).then(() => {
    proxy.$modal.msgSuccess(tip)
  }).catch(() => {
    proxy.$modal.msgError('复制失败，请手动选择文本复制')
  })
}

/** 回到授权态（重新颁发） */
function resetResult() {
  authResult.value = null
  copied.value = false
}

/** 查询我的授权列表 */
function getMyGrants() {
  myLoading.value = true
  listMyGrant().then(res => {
    myGrants.value = res.data || []
    myLoading.value = false
  }).catch(() => { myLoading.value = false })
}

/** 吊销本人授权 */
function handleRevoke(row) {
  proxy.$modal.confirm('确认吊销该授权吗？吊销后对应客户端将立即失去访问能力，需重新授权。').then(() => {
    return revokeMyGrant(row.grantId)
  }).then(() => {
    getMyGrants()
    proxy.$modal.msgSuccess('吊销成功')
  }).catch(() => {})
}

loadConfig()
getMyGrants()
</script>

<style lang="scss" scoped>
.connector-auth-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 16px 40px;
}

.auth-card {
  width: 100%;
  max-width: 820px;
  background: var(--el-bg-color, #fff);
  border-radius: 14px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.08);
  padding: 32px 40px;
}

.list-card {
  margin-top: 20px;
  padding: 24px 28px;
}

/* ===== 头部 ===== */
.brand-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;

  .brand-badge {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 16px;
    color: #fff;
    letter-spacing: 0.5px;

    &.edop {
      background: #1e3a8a;
      box-shadow: 0 4px 12px rgba(30, 58, 138, 0.35);
    }

    &.wb {
      background: #111827;
      box-shadow: 0 4px 12px rgba(17, 24, 39, 0.3);
    }
  }

  .brand-link {
    color: #94a3b8;
    display: flex;
  }
}

.page-title {
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary, #1f2d3d);
  margin: 18px 0 10px;
}

.page-desc {
  text-align: center;
  font-size: 13px;
  line-height: 1.9;
  color: var(--el-text-color-secondary, #64748b);
  margin: 0 0 24px;
}

/* ===== 一键授权 ===== */
.section-box {
  background: var(--el-fill-color-lighter, #f8fafc);
  border: 1px solid var(--el-border-color-lighter, #e2e8f0);
  border-radius: 10px;
  padding: 20px;
}

.grant-row {
  display: flex;
  justify-content: center;

  .grant-btn {
    min-width: 200px;
    height: 44px;
    font-size: 15px;
    border-radius: 10px;
  }
}

.grant-tip {
  margin-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary, #94a3b8);
  line-height: 1.7;
}

/* ===== 授权结果 ===== */
.token-section {
  background: var(--el-fill-color-lighter, #f8fafc);
}

.warn-box {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.7;
  color: #92400e;

  .warn-icon {
    font-size: 15px;
  }

  b {
    color: #b45309;
  }
}

.usage-line {
  margin: 16px 2px 10px;
  font-size: 13px;
  color: var(--el-text-color-regular, #334155);
  line-height: 1.8;

  .kbd {
    background: #e0e7ff;
    color: #3730a3;
    border-radius: 4px;
    padding: 1px 7px;
    font-size: 12px;
    font-weight: 600;
  }
}

.code-block {
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 10px;
  padding: 16px 18px;
  font-family: Consolas, Monaco, monospace;
  font-size: 12.5px;
  line-height: 1.7;
  overflow-x: auto;
  white-space: pre;
  margin: 0 0 14px;

  &::-webkit-scrollbar {
    height: 8px;
  }

  &::-webkit-scrollbar-thumb {
    background: #334155;
    border-radius: 4px;
  }
}

.copy-main-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  background: #1e3a8a;
  border-color: #1e3a8a;

  &:hover,
  &:focus {
    background: #2542a3;
    border-color: #2542a3;
  }

  &.copied {
    background: #14532d;
    border-color: #14532d;

    &:hover,
    &:focus {
      background: #166534;
      border-color: #166534;
    }
  }
}

.merge-hint {
  margin-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary, #94a3b8);
  line-height: 1.7;
}

.token-label {
  margin-top: 18px;
  font-size: 12px;
  color: var(--el-text-color-secondary, #64748b);
}

.token-box {
  margin-top: 8px;
  background: var(--el-bg-color, #fff);
  border: 1px solid var(--el-border-color, #d3dce6);
  border-radius: 8px;
  padding: 12px 14px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  word-break: break-all;
  cursor: pointer;
  color: var(--el-text-color-regular, #334155);

  &:hover {
    border-color: #1e3a8a;
  }
}

.again-row {
  margin-top: 14px;
  text-align: center;
}

/* ===== 我的授权 ===== */
.list-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;

  .list-title {
    font-size: 15px;
    font-weight: 700;
    color: var(--el-text-color-primary, #1f2d3d);
  }

  .list-hint {
    flex: 1;
    font-size: 12px;
    color: var(--el-text-color-secondary, #94a3b8);
  }
}
</style>
