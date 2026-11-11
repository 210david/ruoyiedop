<template>
  <div class="app-container kingdee-page" v-loading="loading">

    <!-- ===== 顶部状态栏 ===== -->
    <el-card shadow="never" class="status-bar-card">
      <div class="status-row">
        <div class="status-cell">
          <div class="status-icon" :class="form.enabled ? 'ok' : 'off'">
            <el-icon v-if="form.enabled" :size="18"><Connection /></el-icon>
            <el-icon v-else :size="18"><CircleClose /></el-icon>
          </div>
          <div class="status-text">
            <div class="status-label">金蝶云星辰对接</div>
            <div class="status-value">
              <el-tag v-if="form.enabled" type="success" size="small" effect="dark">已启用</el-tag>
              <el-tag v-else type="info" size="small" effect="dark">未启用</el-tag>
              <span v-if="form.enabled && lastTestResult" class="sub-status">
                · 上次测试
                <el-tag v-if="lastTestResult.success" type="success" size="small">成功 · {{ formatLatency(lastTestResult.latencyMs) }}</el-tag>
                <el-tag v-else type="danger" size="small">失败</el-tag>
              </span>
              <span v-else-if="form.enabled && !lastTestResult" class="sub-status">· 未测试</span>
              <span v-else class="sub-status">· 填写配置并保存后启用</span>
            </div>
          </div>
        </div>

        <div class="status-cell">
          <div class="status-icon neutral"><el-icon :size="18"><Link /></el-icon></div>
          <div class="status-text">
            <div class="status-label">接口地址</div>
            <div class="status-value mono">{{ form.baseUrl || '未配置' }}</div>
          </div>
        </div>

        <div class="status-cell">
          <div class="status-icon neutral"><el-icon :size="18"><Key /></el-icon></div>
          <div class="status-text">
            <div class="status-label">授权状态</div>
            <div class="status-value">
              <el-tag v-if="appSecretConfigured" type="success" size="small">密钥已配置</el-tag>
              <el-tag v-else type="warning" size="small">密钥未配置</el-tag>
            </div>
          </div>
        </div>

        <div class="status-actions">
          <el-switch v-model="form.enabled" active-text="开启" inactive-text="关闭" inline-prompt />
          <el-button type="success" icon="Connection" @click="handleTest" :loading="testing" size="default">测试连通</el-button>
        </div>
      </div>
    </el-card>

    <!-- ===== 两列布局 ===== -->
    <el-row :gutter="16" v-show="form.enabled">

      <!-- 左列：配置表单 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title"><span class="section-dot"></span>对接参数配置</span>
          </template>

          <el-form ref="formRef" :model="form" label-width="130px" class="kd-form">

            <!-- 基础信息 -->
            <div class="form-group-title">
              <el-icon><Setting /></el-icon>基础信息
            </div>

            <el-form-item label="API 根地址">
              <el-input v-model="form.baseUrl" placeholder="http://api.kingdee.com/jdy/v2">
                <template #prefix><el-icon><Globe /></el-icon></template>
              </el-input>
              <div class="form-tip">金蝶云星辰 API 根路径，标准版/专业版/旗舰版均为此值</div>
            </el-form-item>

            <el-form-item label="应用ID">
              <el-input v-model="form.clientId" placeholder="如 205022" @blur="validateClientId" maxlength="20" />
              <div class="form-tip">在 open.jdy.com 创建应用时分配的 ClientID</div>
            </el-form-item>

            <el-form-item label="签名密钥">
              <el-input v-model="form.clientSecret" type="password" autocomplete="new-password" show-password
                :placeholder="clientSecretConfigured ? '已配置，留空则沿用原密钥' : '粘贴 open.jdy.com 应用的 ClientSecret'" />
              <div class="form-tip secret-tip">
                <el-icon><Lock /></el-icon>固定不变的签名密钥，用于所有接口的 X-Api-Signature 计算
              </div>
            </el-form-item>

            <el-form-item label="授权 Key">
              <el-input v-model="form.appKey" placeholder="粘贴金蝶开放平台授权 Key" show-password />
            </el-form-item>

            <el-form-item label="授权密钥">
              <el-input v-model="form.appSecret" type="password" autocomplete="new-password" show-password
                :placeholder="appSecretConfigured ? '已配置，留空则沿用原密钥' : '粘贴金蝶开放平台授权密钥'" />
              <div class="form-tip secret-tip">
                <el-icon><Lock /></el-icon>24h 刷新，仅用于换 app-token 时的 app_signature URL 参数
              </div>
            </el-form-item>

            <el-form-item label="路由地址">
              <el-input v-model="form.routerAddr" placeholder="如 https://tf.jdy.com/">
                <template #prefix><el-icon><Position /></el-icon></template>
              </el-input>
              <div class="form-tip">账套所属 IDC 域名，授权回调返回的 domain 字段</div>
            </el-form-item>

            <!-- 高级设置 -->
            <div class="form-group-title">
              <el-icon><Operation /></el-icon>高级设置
              <el-tooltip placement="top" effect="light" class="group-help">
                <template #content>
                  <div style="max-width:220px;line-height:1.6">
                    影响请求的稳定性和性能，一般保持默认即可
                  </div>
                </template>
                <el-icon class="help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>

            <el-form-item label="HTTP 超时">
              <el-input-number v-model="form.timeout" :min="5000" :max="120000" :step="5000"
                controls-position="right" style="width: 170px" />
              <span class="form-tip-inline">毫秒</span>
            </el-form-item>

            <el-form-item label="重试次数">
              <el-input-number v-model="form.retryTimes" :min="0" :max="5" controls-position="right" style="width: 170px" />
              <span class="form-tip-inline">次（不含首次）</span>
            </el-form-item>

            <el-form-item label="定时 Cron">
              <el-input v-model="form.syncCron" placeholder="0 */30 * * * ?" style="width:170px">
                <template #prefix><el-icon><Timer /></el-icon></template>
              </el-input>
              <span class="form-tip-inline">需在 Quartz 任务中同步更新</span>
            </el-form-item>

            <!-- 操作区 -->
            <el-form-item class="form-actions">
              <el-button type="primary" @click="handleSave" :loading="saving" size="default" icon="Check">
                保存配置
              </el-button>
              <el-button @click="handleReset" icon="RefreshLeft">重置</el-button>
              <el-button type="warning" plain icon="View" @click="handleShowSecret">显示已存密钥</el-button>
              <span class="saved-tip" v-if="lastSavedAt">
                <el-icon><CircleCheckFilled /></el-icon>已保存于 {{ lastSavedAt }}
              </span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右列：同步 + 帮助 -->
      <el-col :xs="24" :lg="10">

        <!-- 同步卡片 -->
        <el-card shadow="never" class="section-card sync-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title"><span class="section-dot dot-green"></span>立即同步</span>
              <div style="display:flex;gap:8px">
                <el-tag type="warning" effect="light" size="small">幂等 · 可重复执行</el-tag>
                <el-button type="primary" size="small" icon="RefreshRight" @click="handleSyncAll" :loading="syncingAll">一键全量</el-button>
              </div>
            </div>
          </template>

          <div class="sync-grid">
            <div class="sync-item" @click="handleSyncCustomer" :class="{ loading: syncingCustomer }">
              <div class="sync-ic user-ic">
                <el-icon :size="24" v-if="!syncingCustomer"><User /></el-icon>
                <el-icon :size="24" v-else class="spin-icon"><Loading /></el-icon>
              </div>
              <div class="sync-info">
                <div class="sync-name">客户主数据</div>
                <div class="sync-desc">金蝶 ERP → 若依 MK 模块</div>
              </div>
              <el-icon class="sync-arrow"><CaretRight /></el-icon>
            </div>

            <div class="sync-item" @click="handleSyncMaterial" :class="{ loading: syncingMaterial }">
              <div class="sync-ic goods-ic">
                <el-icon :size="24" v-if="!syncingMaterial"><Goods /></el-icon>
                <el-icon :size="24" v-else class="spin-icon"><Loading /></el-icon>
              </div>
              <div class="sync-info">
                <div class="sync-name">物料主数据</div>
                <div class="sync-desc">金蝶 bd/material → 若依 WMS 物料</div>
              </div>
              <el-icon class="sync-arrow"><CaretRight /></el-icon>
            </div>

            <div class="sync-item" @click="handleSyncSupplier" :class="{ loading: syncingSupplier }">
              <div class="sync-ic supp-ic">
                <el-icon :size="24" v-if="!syncingSupplier"><OfficeBuilding /></el-icon>
                <el-icon :size="24" v-else class="spin-icon"><Loading /></el-icon>
              </div>
              <div class="sync-info">
                <div class="sync-name">供应商主数据</div>
                <div class="sync-desc">金蝶 bd/supplier → 若依 WMS 供应商</div>
              </div>
              <el-icon class="sync-arrow"><CaretRight /></el-icon>
            </div>
          </div>

          <!-- 上次同步结果 -->
          <div class="sync-result" v-if="lastSyncResult">
            <div class="sync-result-title">
              <el-icon><DataLine /></el-icon>上次同步结果
              <span class="sync-result-time">{{ lastSyncResult.durationMs ? formatLatency(lastSyncResult.durationMs) : '' }}</span>
            </div>
            <div class="sync-stats">
              <div class="stat-item total">
                <div class="stat-num">{{ lastSyncResult.total ?? '-' }}</div>
                <div class="stat-label">总计</div>
              </div>
              <div class="stat-item inserted">
                <div class="stat-num">{{ lastSyncResult.inserted ?? '-' }}</div>
                <div class="stat-label">新增</div>
              </div>
              <div class="stat-item updated">
                <div class="stat-num">{{ lastSyncResult.updated ?? '-' }}</div>
                <div class="stat-label">更新</div>
              </div>
              <div class="stat-item skipped">
                <div class="stat-num">{{ lastSyncResult.skipped ?? '-' }}</div>
                <div class="stat-label">跳过</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 帮助卡片 -->
        <el-card shadow="never" class="section-card help-card">
          <template #header>
            <span class="section-title"><span class="section-dot dot-purple"></span>快速帮助</span>
          </template>
          <div class="help-list">
            <div class="help-item">
              <el-icon class="help-ic"><Document /></el-icon>
              <div class="help-body">
                <div class="help-title">应用创建</div>
                <div class="help-desc">在 open.jdy.com 创建应用，完成实时接收授权，拿到 AppKey / AppSecret</div>
              </div>
            </div>
            <div class="help-item">
              <el-icon class="help-ic"><MagicStick /></el-icon>
              <div class="help-body">
                <div class="help-title">测试连通</div>
                <div class="help-desc">保存后点"测试连通"，系统会自动获取 app-token 验证全部参数</div>
              </div>
            </div>
            <div class="help-item">
              <el-icon class="help-ic"><Refresh /></el-icon>
              <div class="help-body">
                <div class="help-title">定时同步</div>
                <div class="help-desc">启用后建议配置 Quartz 定时任务（invokeTarget=kingdeeSyncJob.runAll），默认每 30 分钟全量同步</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 空状态：未启用时 ===== -->
    <el-card shadow="never" class="section-card empty-card" v-show="!form.enabled">
      <el-empty description="金蝶对接未启用">
        <template #image>
          <div class="empty-ic-wrap">
            <el-icon :size="64" color="#c0c4cc"><Connection /></el-icon>
          </div>
        </template>
        <div class="empty-guide">
          <div class="empty-step"><b>1.</b> 在 open.jdy.com 创建云星辰应用并完成授权</div>
          <div class="empty-step"><b>2.</b> 填入 ClientID / AppKey / AppSecret / RouterAddr</div>
          <div class="empty-step"><b>3.</b> 保存后点"测试连通"验证签名鉴权</div>
          <div class="empty-step"><b>4.</b> 开启开关，开始同步客户 / 物料 / 供应商</div>
        </div>
      </el-empty>
    </el-card>

  </div>
</template>

<script setup name="KingdeeConfig">
import { getKingdeeConfig, updateKingdeeConfig, testKingdeeConfig,
  syncCustomer, syncMaterial, syncSupplier, syncAll } from '@/api/kingdee/index'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const saving = ref(false)
const testing = ref(false)
const syncingCustomer = ref(false)
const syncingMaterial = ref(false)
const syncingSupplier = ref(false)
const syncingAll = ref(false)
const clientSecretConfigured = ref(false)
const appSecretConfigured = ref(false)
const originalForm = ref({})
const lastSavedAt = ref('')
const lastTestResult = ref(null)
const lastSyncResult = ref(null)

const form = ref({
  enabled: false,
  baseUrl: 'https://apikingdee.com/jdy',
  clientId: '',
  clientSecret: '',
  appKey: '',
  appSecret: '',
  routerAddr: 'https://tf.jdy.com/',
  timeout: 30000,
  retryTimes: 2,
  syncCron: '0 */30 * * * ?'
})

function loadConfig(showSecret) {
  loading.value = true
  getKingdeeConfig(showSecret).then(res => {
    const d = res.data || {}
    form.value = {
      enabled: d.enabled === true || d.enabled === 'true',
      baseUrl: d.baseUrl || 'https://apikingdee.com/jdy',
      clientId: d.clientId || '',
      // 如果后端返回了明文（showSecret=true），直接填入；否则留空让用户手动填
      clientSecret: d.clientSecret && !d.clientSecret.includes('****') ? d.clientSecret : '',
      appKey: d.appKey || '',
      appSecret: d.appSecret && !d.appSecret.includes('****') ? d.appSecret : '',
      routerAddr: d.routerAddr || 'https://tf.jdy.com/',
      timeout: parseInt(d.timeout) || 30000,
      retryTimes: parseInt(d.retryTimes) || 2,
      syncCron: d.syncCron || '0 */30 * * * ?'
    }
    clientSecretConfigured.value = d.clientSecretConfigured === true || d.clientSecretConfigured === 'true'
    appSecretConfigured.value = d.appSecretConfigured === true || d.appSecretConfigured === 'true'
    originalForm.value = { ...form.value }
    loading.value = false
  }).catch(() => {
    loading.value = false
    proxy.$modal.msgError('金蝶配置加载失败，请检查后端服务状态')
  })
}

function validateClientId() {
  if (form.value.clientId && !/^\d+$/.test(form.value.clientId)) {
    proxy.$modal.msgWarning('应用ID 通常是纯数字')
  }
}

function handleSave() {
  if (!form.value.baseUrl.startsWith('http')) {
    proxy.$modal.msgError('API 根地址必须以 http:// 或 https:// 开头')
    return
  }
  saving.value = true
  const payload = { ...form.value }
  if (!payload.clientSecret) delete payload.clientSecret
  if (!payload.appSecret) delete payload.appSecret

  updateKingdeeConfig(payload).then(() => {
    proxy.$modal.msgSuccess('配置保存成功，已即时生效')
    saving.value = false
    // 刷新状态标志（不传 showSecret，拿到打码快照只用来更新 Configured 状态）
    getKingdeeConfig().then(res => {
      const d = res.data || {}
      clientSecretConfigured.value = d.clientSecretConfigured === true || d.clientSecretConfigured === 'true'
      appSecretConfigured.value = d.appSecretConfigured === true || d.appSecretConfigured === 'true'
      originalForm.value = { ...form.value }
    })
    lastSavedAt.value = formatNow()
  }).catch(() => { saving.value = false })
}

function handleTest() {
  testing.value = true
  const started = Date.now()
  testKingdeeConfig().then(res => {
    testing.value = false
    const d = res.data || {}
    const duration = Date.now() - started
    lastTestResult.value = { success: !!d.success, latencyMs: d.latencyMs || duration, error: d.error }
    if (d.success) {
      proxy.$modal.msgSuccess(`连通性测试通过 · 耗时 ${formatLatency(d.latencyMs)}`)
    } else {
      proxy.$alert(
        `<div style="line-height:1.8">
          连通性测试失败<br/>
          <b>耗时</b>：${formatLatency(d.latencyMs)}<br/>
          <b>错误</b>：${d.error || '未知错误'}<br/><br/>
          <span style="color:#909399;font-size:12px">
            常见原因：app_token 接口路径不正确 / app_secret 未配置 / 网络无法访问金蝶 API
          </span>
         </div>`,
        '测试失败',
        { dangerouslyUseHTMLString: true, confirmButtonText: '好的', type: 'error' }
      )
    }
  }).catch(() => {
    testing.value = false
    lastTestResult.value = { success: false, latencyMs: 0, error: '请求失败' }
  })
}

function handleReset() {
  form.value = { ...originalForm.value, clientSecret: '', appSecret: '' }
  proxy.$modal.msgSuccess('已还原为最近保存的配置')
}

function handleShowSecret() {
  loadConfig(true)
  proxy.$modal.msgSuccess('已加载明文密钥到表单，建议看完后点重置隐藏')
}

function doSync(name, fn, loadingRef) {
  loadingRef.value = true
  fn().then(res => {
    loadingRef.value = false
    // 若依 axios 拦截器 return res.data → res 已经是后端 body: {code, msg, statistics, samples, ...}
    const data = res || {}
    const stat = data.statistics || data
    const elapsedMs = data.elapsedMs || 0
    const samples = data.samples || []
    const skipReasons = data.skipReasons || []

    // 构建样本表格
    let sampleHtml = ''
    if (samples.length > 0) {
      sampleHtml = `
        <div style="margin-top:12px">
          <div style="color:#606266;font-weight:600;margin-bottom:6px">📋 处理详情（前 ${samples.length} 条）</div>
          <table style="width:100%;font-size:12px;border-collapse:collapse">
            <thead><tr style="background:#f5f7fa;color:#909399">
              <th style="padding:4px 6px;text-align:left">编号</th>
              <th style="padding:4px 6px;text-align:left">名称</th>
              <th style="padding:4px 6px;text-align:left">操作</th>
              <th style="padding:4px 6px;text-align:left">备注</th>
            </tr></thead>
            <tbody>${samples.map(s => `
              <tr style="border-bottom:1px solid #f0f0f0">
                <td style="padding:4px 6px;color:#606266;font-family:monospace">${s.no || ''}</td>
                <td style="padding:4px 6px">${s.name || ''}</td>
                <td style="padding:4px 6px">${actionTag(s.action)}</td>
                <td style="padding:4px 6px;color:#909399">${s.remark || '-'}</td>
              </tr>`).join('')}</tbody>
          </table>
        </div>`
    }

    // 跳过原因
    let skipHtml = ''
    if (skipReasons.length > 0) {
      skipHtml = `
        <div style="margin-top:10px;padding:8px 10px;background:#fef0f0;border:1px solid #fde2e2;border-radius:4px">
          <div style="color:#f56c6c;font-weight:600;margin-bottom:4px">⚠️ 跳过原因（${skipReasons.length} 条）</div>
          ${skipReasons.map(r => `<div style="color:#909399;font-size:12px">• ${r}</div>`).join('')}
        </div>`
    }

    lastSyncResult.value = { ...stat, elapsedMs, target: name }

    proxy.$alert(
      `<div style="line-height:1.8;max-height:70vh;overflow:auto">
        <div style="font-size:14px;margin-bottom:8px"><b>${name}</b>同步完成</div>
        <div style="display:flex;gap:16px;margin:8px 0;flex-wrap:wrap">
          <div style="text-align:center;padding:8px 16px;background:#f5f7fa;border-radius:6px">
            <div style="font-size:22px;font-weight:700;color:#606266">${stat.total ?? 0}</div>
            <div style="color:#909399;font-size:12px">总计</div>
          </div>
          <div style="text-align:center;padding:8px 16px;background:#f0f9eb;border-radius:6px">
            <div style="font-size:22px;font-weight:700;color:#67c23a">${stat.inserted ?? 0}</div>
            <div style="color:#909399;font-size:12px">新增</div>
          </div>
          <div style="text-align:center;padding:8px 16px;background:#fdf6ec;border-radius:6px">
            <div style="font-size:22px;font-weight:700;color:#e6a23c">${stat.updated ?? 0}</div>
            <div style="color:#909399;font-size:12px">更新</div>
          </div>
          <div style="text-align:center;padding:8px 16px;background:#fef0f0;border-radius:6px">
            <div style="font-size:22px;font-weight:700;color:#f56c6c">${stat.skipped ?? 0}</div>
            <div style="color:#909399;font-size:12px">跳过</div>
          </div>
        </div>
        <div style="color:#909399;font-size:12px;margin-bottom:4px">⏱️ 耗时 ${formatLatency(elapsedMs)}</div>
        ${sampleHtml}
        ${skipHtml}
       </div>`,
      `${name}同步结果`,
      { dangerouslyUseHTMLString: true, confirmButtonText: '好的', customClass: 'kingdee-sync-dialog' }
    )
  }).catch(() => { loadingRef.value = false })
}

function actionTag(action) {
  if (action === '新增') return '<span style="color:#67c23a;background:#f0f9eb;padding:1px 6px;border-radius:3px;font-size:12px">＋新增</span>'
  if (action === '更新') return '<span style="color:#e6a23c;background:#fdf6ec;padding:1px 6px;border-radius:3px;font-size:12px">∼更新</span>'
  return action || '-'
}

const handleSyncCustomer = () => doSync('客户', syncCustomer, syncingCustomer)
const handleSyncMaterial = () => doSync('物料', syncMaterial, syncingMaterial)
const handleSyncSupplier = () => doSync('供应商', syncSupplier, syncingSupplier)

function handleSyncAll() {
  syncingAll.value = true
  const started = Date.now()
  syncAll().then(res => {
    syncingAll.value = false
    const all = res?.modules || {}
    let total=0, ins=0, upd=0, sk=0
    const html = Object.entries(all).map(([k, v]) => {
      const s = v?.statistics || {}
      total += (s.total || 0); ins += (s.inserted || 0); upd += (s.updated || 0); sk += (s.skipped || 0)
      return `<tr><td style="padding-right:16px;color:#909399">${labelOf(k)}</td><td>${s.total || 0}</td><td style="color:#67c23a">+${s.inserted || 0}</td><td style="color:#e6a23c">~${s.updated || 0}</td><td style="color:#f56c6c">x${s.skipped || 0}</td></tr>`
    }).join('')
    lastSyncResult.value = { total, inserted: ins, updated: upd, skipped: sk, durationMs: Date.now() - started }
    proxy.$alert(
      `<div style="line-height:2">
        全模块同步完成！<br/>
        <table style="margin:8px 0;font-size:13px;border-collapse:collapse">
          <tr style="color:#606266;font-weight:600"><th style="padding-right:16px;text-align:left">模块</th><th>总计</th><th>新增</th><th>更新</th><th>跳过</th></tr>
          ${html}
          <tr style="border-top:1px solid #ebeef5;margin-top:4px"><td style="padding-right:16px;font-weight:600">合计</td><td><b>${total}</b></td><td style="color:#67c23a">+${ins}</td><td style="color:#e6a23c">~${upd}</td><td style="color:#f56c6c">x${sk}</td></tr>
        </table>
        <div style="margin-top:6px;color:#909399;font-size:12px">耗时 ${formatLatency(lastSyncResult.value.durationMs)}</div>
       </div>`,
      '全量同步结果',
      { dangerouslyUseHTMLString: true, confirmButtonText: '好的' }
    )
  }).catch(() => { syncingAll.value = false })
}

function labelOf(k) {
  return { customer:'客户', material:'物料', supplier:'供应商' }[k] || k
}

function formatLatency(ms) {
  const v = Number(ms) || 0
  return v < 1000 ? v + ' ms' : (v / 1000).toFixed(1) + ' 秒'
}
function formatNow() {
  const d = new Date()
  return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}:${d.getSeconds().toString().padStart(2,'0')}`
}

loadConfig()
</script>

<style scoped>
.kingdee-page { padding: 0; max-width: 1440px; }
.status-bar-card { margin-bottom: 16px; border-radius: 10px; background: linear-gradient(135deg, #f5f7fa 0%, #eef2ff 100%); border: 1px solid #e4e7ed; }
.status-row { display: flex; align-items: center; gap: 32px; flex-wrap: wrap; }
.status-cell { display: flex; align-items: center; gap: 12px; flex: 0 0 auto; }
.status-icon { width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; }
.status-icon.ok { background: linear-gradient(135deg,#67c23a,#4cae20); }
.status-icon.off { background: linear-gradient(135deg,#909399,#73767a); }
.status-icon.neutral { background: linear-gradient(135deg,#409eff,#2b7de9); }
.status-text .status-label { font-size: 12px; color: #909399; }
.status-text .status-value { font-size: 14px; font-weight: 600; color: #303133; margin-top: 2px; }
.status-text .status-value.mono { font-family: Menlo,Consolas,monospace; font-size: 12px; color: #606266; max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.status-text .sub-status { font-size: 12px; color: #909399; font-weight: 400; margin-left: 6px; }
.status-actions { margin-left: auto; display: flex; align-items: center; gap: 12px; }

.section-card { border-radius: 10px; border: 1px solid #ebeef5; }
.section-title { font-size: 15px; font-weight: 700; color: #303133; display: inline-flex; align-items: center; gap: 8px; }
.section-dot { width: 8px; height: 8px; border-radius: 50%; background: #409eff; display: inline-block; }
.section-dot.dot-green { background: #67c23a; }
.section-dot.dot-purple { background: #9b59b6; }

.kd-form :deep(.el-form-item) { margin-bottom: 18px; }
.form-group-title { font-size: 13px; font-weight: 700; color: #606266; margin: 24px 0 12px; padding-bottom: 8px; border-bottom: 1px dashed #ebeef5; display: flex; align-items: center; gap: 6px; }
.form-group-title .help-icon { cursor: help; color: #c0c4cc; font-size: 14px; }
.form-group-title .help-icon:hover { color: #409eff; }
.form-tip { font-size: 12px; color: #909399; line-height: 1.5; margin-top: 4px; }
.form-tip.secret-tip { color: #e6a23c; display: inline-flex; align-items: center; gap: 4px; }
.form-tip-inline { margin-left: 8px; color: #909399; font-size: 13px; }
.form-actions { margin-top: 24px !important; padding-top: 16px; border-top: 1px solid #ebeef5; }
.saved-tip { margin-left: 12px; color: #67c23a; font-size: 12px; display: inline-flex; align-items: center; gap: 4px; }
.help-icon { color: #c0c4cc; cursor: help; font-size: 14px; }

.sync-card { margin-bottom: 16px; border-left: 3px solid #67c23a; }
.sync-grid { display: flex; flex-direction: column; gap: 10px; }
.sync-item { display: flex; align-items: center; gap: 14px; padding: 14px 16px; border-radius: 8px; border: 1px solid #ebeef5; cursor: pointer; transition: all .2s; background: #fafbfc; }
.sync-item:hover:not(.disabled) { border-color: #409eff; background: #ecf5ff; transform: translateY(-1px); }
.sync-item.loading { pointer-events: none; }
.sync-item.disabled { cursor: not-allowed; opacity: .65; }
.sync-ic { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
.sync-ic.user-ic { background: linear-gradient(135deg,#409eff,#2b7de9); }
.sync-ic.goods-ic { background: linear-gradient(135deg,#67c23a,#4cae20); }
.sync-ic.supp-ic { background: linear-gradient(135deg,#e6a23c,#cf8e47); }
.sync-ic.gray { background: linear-gradient(135deg,#b1b3b8,#909399); }
.spin-icon { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.sync-info { flex: 1; }
.sync-name { font-weight: 600; color: #303133; }
.sync-desc { font-size: 12px; color: #909399; margin-top: 3px; }
.sync-arrow { color: #c0c4cc; font-size: 18px; }

.sync-result { margin-top: 16px; padding: 12px 14px; background: #f4f4f5; border-radius: 8px; }
.sync-result-title { font-size: 12px; font-weight: 600; color: #606266; display: flex; align-items: center; gap: 6px; }
.sync-result-time { margin-left: auto; color: #909399; font-weight: 400; }
.sync-stats { display: flex; gap: 12px; margin-top: 10px; }
.stat-item { flex: 1; text-align: center; padding: 8px 0; background: #fff; border-radius: 6px; }
.stat-num { font-size: 18px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 11px; color: #909399; margin-top: 2px; }
.stat-item.total .stat-num { color: #303133; }
.stat-item.inserted .stat-num { color: #67c23a; }
.stat-item.updated .stat-num { color: #e6a23c; }
.stat-item.skipped .stat-num { color: #f56c6c; }

.help-card { border-left: 3px solid #9b59b6; }
.help-list { display: flex; flex-direction: column; gap: 12px; }
.help-item { display: flex; gap: 10px; padding: 4px 0; }
.help-ic { width: 28px; height: 28px; border-radius: 6px; background: #f3e8fa; color: #9b59b6; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 14px; }
.help-title { font-size: 13px; font-weight: 600; color: #303133; }
.help-desc { font-size: 12px; color: #909399; margin-top: 2px; line-height: 1.5; }

.empty-card { text-align: center; padding: 16px 0; }
.empty-ic-wrap { width: 100px; height: 100px; margin: 0 auto; border-radius: 50%; background: #f5f7fa; display: flex; align-items: center; justify-content: center; }
.empty-guide { display: inline-flex; flex-direction: column; gap: 8px; margin-top: 8px; text-align: left; }
.empty-step { color: #606266; font-size: 13px; }
.empty-step b { display: inline-block; width: 22px; height: 22px; line-height: 22px; text-align: center; background: #409eff; color: #fff; border-radius: 50%; font-size: 11px; margin-right: 8px; }
</style>
