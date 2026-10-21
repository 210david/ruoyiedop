<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
        </div>
        <span class="rd-detail-header-title">用户详情</span>
        <div class="rd-detail-header-sub" v-if="info.userName">
          <div class="rd-detail-header-divider"></div>
          <span class="rd-detail-header-no">{{ info.userName }}</span>
        </div>
      </div>
    </template>
    <div class="rd-page" v-loading="loading">
      <!-- 基本信息 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('basic')">
          <div class="rd-card-title">
            <span class="rd-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </span>
            基本信息
          </div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="18 15 12 9 6 15" />
            </svg>
          </button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.basic">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">用户昵称</span><div class="rd-value">{{ info.nickName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">登录账号</span><div class="rd-value">{{ info.userName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">归属部门</span><div class="rd-value">{{ (info.dept && info.dept.deptName) || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">用户性别</span><div class="rd-value">{{ sexLabel }}</div></div>
            <div class="rd-item"><span class="rd-label">手机号码</span><div class="rd-value">{{ info.phonenumber || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ info.email || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">用户状态</span><div class="rd-value">
              <el-tag size="small" :type="info.status === '0' ? 'success' : 'danger'">{{ info.status === '0' ? '正常' : '停用' }}</el-tag>
            </div></div>
            <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ info.remark || '-' }}</div></div>
          </div>
        </div>
      </section>
      <!-- 岗位与角色 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('role')">
          <div class="rd-card-title">
            <span class="rd-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </span>
            岗位与角色
          </div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.role }" aria-label="折叠">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="18 15 12 9 6 15" />
            </svg>
          </button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.role">
          <div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">岗位</span><div class="rd-value">{{ postNames || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">角色</span><div class="rd-value">{{ roleNames || '-' }}</div></div>
          </div>
        </div>
      </section>
      <!-- 其他信息 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('other')">
          <div class="rd-card-title">
            <span class="rd-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="16" x2="12" y2="12" />
                <line x1="12" y1="8" x2="12.01" y2="8" />
              </svg>
            </span>
            其他信息
          </div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="18 15 12 9 6 15" />
            </svg>
          </button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.other">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">创建者</span><div class="rd-value">{{ info.createBy || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ info.createTime || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">更新者</span><div class="rd-value">{{ info.updateBy || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">更新时间</span><div class="rd-value">{{ info.updateTime || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">最后登录IP</span><div class="rd-value">{{ info.loginIp || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">最后登录时间</span><div class="rd-value">{{ info.loginDate || '-' }}</div></div>
          </div>
        </div>
      </section>
    </div>
    <template #footer>
      <el-button @click="handleClose">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { getUser } from '@/api/system/user'
import { useDetailCard } from '@/composables/useDetailCard'

const { collapsedCards, toggleCard } = useDetailCard(['basic', 'role', 'other'])

const visible = ref(false)
const loading = ref(false)
const info = reactive({})
const postOptions = ref([])
const roleOptions = ref([])

const { sys_user_sex } = useDict("sys_user_sex")

const sexLabel = computed(() => selectDictLabel(sys_user_sex.value, info.sex) || '-')

const postNames = computed(() => {
  if (!postOptions.value.length || !info.postIds) return ''
  return postOptions.value.filter(p => info.postIds?.includes(p.postId)).map(p => p.postName).join('、') || ''
})

const roleNames = computed(() => {
  if (!roleOptions.value.length || !info.roleIds) return ''
  return roleOptions.value.filter(r => info.roleIds?.includes(r.roleId)).map(r => r.roleName).join('、') || ''
})

const open = async (userId) => {
  visible.value = true
  loading.value = true
  try {
    const res = await getUser(userId)
    Object.assign(info, res.data || {})
    postOptions.value = res.posts || []
    roleOptions.value = res.roles || []
    info.postIds = res.postIds || []
    info.roleIds = res.roleIds || []
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

function handleClose() {
  visible.value = false
}

defineExpose({
  open
})
</script>
