<template>
  <el-dialog v-model="dialogVisible" width="860px" append-to-body draggable class="rd-dialog" @close="$emit('update:visible', false)">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" /><line x1="16" y1="13" x2="8" y2="13" /><line x1="16" y1="17" x2="8" y2="17" /><polyline points="10 9 9 9 8 9" />
          </svg>
        </div>
        <span class="rd-detail-header-title">操作日志详细</span>
        <div class="rd-detail-header-sub" v-if="form.operId">
          <div class="rd-detail-header-divider"></div>
          <span class="rd-detail-header-no">编号：{{ form.operId }}</span>
        </div>
      </div>
    </template>
    <div class="operlog-detail-wrap">
      <!-- 基本信息 -->
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title">
            <span class="rd-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" /><line x1="12" y1="16" x2="12" y2="12" /><line x1="12" y1="8" x2="12.01" y2="8" />
              </svg>
            </span>
            基本信息
          </div>
        </div>
        <div class="rd-card-body">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">操作模块</span><span class="rd-value">{{ form.title }}</span></div>
            <div class="rd-item"><span class="rd-label">业务类型</span><span class="rd-value">{{ typeLabel }}</span></div>
            <div class="rd-item"><span class="rd-label">操作时间</span><span class="rd-value">{{ form.operTime }}</span></div>
            <div class="rd-item"><span class="rd-label">执行状态</span><span class="rd-value">
              <el-tag v-if="form.status === 0" type="success" size="small">正常</el-tag>
              <el-tag v-else type="danger" size="small">异常</el-tag>
            </span></div>
          </div>
        </div>
      </section>

      <!-- 操作人员 -->
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title">
            <span class="rd-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
              </svg>
            </span>
            操作人员
          </div>
        </div>
        <div class="rd-card-body">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">操作人员</span><span class="rd-value">{{ form.operName }}</span></div>
            <div class="rd-item" v-if="form.deptName"><span class="rd-label">所属部门</span><span class="rd-value">{{ form.deptName }}</span></div>
            <div class="rd-item rd-item--full"><span class="rd-label">操作地址</span><span class="rd-value">{{ form.operIp }}&nbsp;&nbsp;<span class="operlog-location">{{ form.operLocation }}</span></span></div>
          </div>
        </div>
      </section>

      <!-- 请求信息 -->
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title">
            <span class="rd-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="16 18 22 12 16 6" /><polyline points="8 6 2 12 8 18" />
              </svg>
            </span>
            请求信息
          </div>
        </div>
        <div class="rd-card-body">
          <div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">请求地址</span><span class="rd-value">
              <span :class="'method-tag method-' + form.requestMethod">{{ form.requestMethod }}</span>
              {{ form.operUrl }}
            </span></div>
            <div class="rd-item rd-item--full"><span class="rd-label">操作方法</span><span class="rd-value mono">{{ form.method }}</span></div>
            <div class="rd-item"><span class="rd-label">消耗时间</span><span class="rd-value">{{ form.costTime }} 毫秒</span></div>
          </div>
        </div>
      </section>

      <!-- 请求参数 -->
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title">
            <span class="rd-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="17 8 12 3 7 8" /><line x1="12" y1="3" x2="12" y2="15" />
              </svg>
            </span>
            请求参数
          </div>
          <div class="rd-card-actions">
            <el-button size="small" :icon="CopyDocument" @click="copyText(form.operParam)">复制</el-button>
          </div>
        </div>
        <div class="rd-card-body">
          <pre class="code-pre">{{ formatJson(form.operParam) }}</pre>
        </div>
      </section>

      <!-- 返回参数 -->
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title">
            <span class="rd-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="7 10 12 15 17 10" /><line x1="12" y1="15" x2="12" y2="3" />
              </svg>
            </span>
            返回参数
          </div>
          <div class="rd-card-actions">
            <el-button size="small" :icon="CopyDocument" @click="copyText(form.jsonResult)">复制</el-button>
          </div>
        </div>
        <div class="rd-card-body">
          <pre class="code-pre">{{ formatJson(form.jsonResult) }}</pre>
        </div>
      </section>

      <!-- 异常信息 -->
      <section class="rd-card" v-if="form.status !== 0">
        <div class="rd-card-header">
          <div class="rd-card-title">
            <span class="rd-card-icon" style="color: #ef4444;">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
            </span>
            异常信息
          </div>
        </div>
        <div class="rd-card-body">
          <div class="operlog-error-msg">{{ form.errorMsg }}</div>
        </div>
      </section>
    </div>
  </el-dialog>
</template>

<script setup>
import { CopyDocument } from '@element-plus/icons-vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  row: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const { sys_oper_type } = useDict('sys_oper_type')

const form = computed(() => props.row || {})
const typeLabel = computed(() => selectDictLabel(sys_oper_type.value, form.value.businessType) || '-')

function formatJson(str) {
  if (!str) return '（无数据）'
  try { return JSON.stringify(JSON.parse(str), null, 2) } catch { return str }
}

function copyText(str) {
  const text = formatJson(str)
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => ElMessage({ message: '已复制', type: 'success', duration: 1500 }))
  } else {
    const ta = document.createElement('textarea')
    ta.value = text
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    ElMessage({ message: '已复制', type: 'success', duration: 1500 })
  }
}
</script>

<style scoped>
/* ===== Operlog Detail - extends rd-dialog ===== */
.operlog-detail-wrap { display: flex; flex-direction: column; gap: 0; }

/* rd-card header cursor default for operlog (no collapse) */
.operlog-detail-wrap .rd-card .rd-card-header { cursor: default; }

/* method tag */
.method-tag { display: inline-flex; align-items: center; justify-content: center; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; letter-spacing: .05em; margin-right: 8px; }
.method-GET { background: #dbeafe; color: #1d4ed8; }
.method-POST { background: #dcfce7; color: #15803d; }
.method-PUT { background: #fef3c7; color: #b45309; }
.method-DELETE { background: #fee2e2; color: #b91c1c; }
.method-OTHER { background: #f1f5f9; color: #475569; }

/* mono text */
.operlog-detail-wrap .mono { font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', monospace; font-size: 13px; word-break: break-all; }

/* location text */
.operlog-location { color: #94a3b8; font-size: 13px; }

/* code block */
.operlog-detail-wrap .code-pre { background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px 16px; font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', monospace; font-size: 13px; line-height: 1.6; color: #334155; white-space: pre-wrap; word-break: break-all; max-height: 240px; overflow-y: auto; margin: 0; }

/* error message */
.operlog-detail-wrap .operlog-error-msg { background: #fef2f2; border: 1px solid #fecaca; border-left: 4px solid #ef4444; border-radius: 8px; padding: 12px 16px; font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', monospace; font-size: 13px; line-height: 1.6; color: #991b1b; white-space: pre-wrap; word-break: break-all; }
</style>
