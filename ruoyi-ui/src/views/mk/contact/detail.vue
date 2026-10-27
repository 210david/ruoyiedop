<template>
  <div class="app-container">
    <el-page-header @back="goBack" class="mb16">
      <template #content>
        <span class="text-large font-600 mr-3">{{ contact.name || '联系人详情' }}</span>
        <el-tag v-if="contact.isKey === '1'" type="danger" size="small">关键联系人</el-tag>
        <el-tag v-if="contact.isPrimary === '1'" type="success" size="small" class="ml5">主要联系人</el-tag>
      </template>
      <template #extra>
        <el-button type="primary" @click="handleEdit" v-hasPermi="['marketing:contact:edit']">编辑</el-button>
        <el-button @click="goBack">返回列表</el-button>
      </template>
    </el-page-header>

    <el-tabs v-model="activeTab" v-loading="loading">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <!-- 个人信息 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">所属客户</span><div class="rd-value">{{ contact.customerName }}</div></div>
          <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ contact.name }}</div></div>
          <div class="rd-item"><span class="rd-label">性别</span><div class="rd-value">{{ contact.gender === '0' ? '男' : '女' }}</div></div>
          <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value">{{ contact.position }}</div></div>
          <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ contact.department }}</div></div>
          <div class="rd-item"><span class="rd-label">角色标签</span><div class="rd-value"><dict-tag :options="marketing_contact_role" :value="contact.roleTag" /></div></div>
        </div>

        <!-- 联系方式 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ contact.phone }}</div></div>
          <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ contact.email }}</div></div>
          <div class="rd-item"><span class="rd-label">微信号</span><div class="rd-value">{{ contact.wechat }}</div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">QQ号</span><div class="rd-value">{{ contact.qq }}</div></div>
        </div>

        <!-- 归属与跟进 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">归属销售</span><div class="rd-value">{{ contact.ownerUserName || '未分配' }}</div></div>
          <div class="rd-item"><span class="rd-label">最后联系时间</span><div class="rd-value">{{ contact.lastContactTime }}</div></div>
          <div class="rd-item"><span class="rd-label">下次联系时间</span><div class="rd-value">{{ contact.nextContactTime }}</div></div>
        </div>

        <!-- 补充信息 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">个人特点</span><div class="rd-value">{{ contact.personalTrait }}</div></div>
          <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ contact.remark }}</div></div>
        </div>
      </el-tab-pane>

      <!-- 互动记录 -->
      <el-tab-pane name="interactions">
        <template #label>互动记录<el-badge :value="interactions.length" :hidden="interactions.length === 0" class="ml5" /></template>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddInteraction" v-hasPermi="['marketing:interaction:add']">新增互动</el-button></el-col>
        </el-row>
        <el-timeline>
          <el-timeline-item v-for="item in interactions" :key="item.recordId" :timestamp="item.interactTime" placement="top" :type="item.nextTime ? 'warning' : 'primary'">
            <el-card>
              <div class="interaction-item">
                <span class="interaction-type"><dict-tag :options="marketing_interaction_type" :value="item.interactType" /></span>
                <span class="interaction-content">{{ item.content }}</span>
              </div>
              <div v-if="item.nextTime" class="interaction-next">
                <el-tag size="small" type="warning">下次跟进：{{ item.nextTime }}</el-tag>
                <span v-if="item.nextContent" class="ml10 text-gray">{{ item.nextContent }}</span>
              </div>
              <div class="interaction-meta">
                <span class="text-gray">跟进人：{{ item.userName }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="interactions.length === 0" description="暂无互动记录" />
      </el-tab-pane>

      <!-- 参与过的活动 -->
      <el-tab-pane name="activities">
        <template #label>参与活动<el-badge :value="activityParticipants.length" :hidden="activityParticipants.length === 0" class="ml5" /></template>
        <el-table :data="activityParticipants" border size="small">
          <el-table-column label="活动名称" prop="activityName" show-overflow-tooltip />
          <el-table-column label="企业名称" prop="companyName" width="180" show-overflow-tooltip />
          <el-table-column label="参与状态" prop="participateStatus" width="100" align="center">
            <template #default="scope"><dict-tag :options="marketing_participate_status" :value="scope.row.participateStatus" /></template>
          </el-table-column>
          <el-table-column label="签到时间" prop="signTime" width="160" />
        </el-table>
        <el-empty v-if="activityParticipants.length === 0" description="暂无活动参与记录" />
      </el-tab-pane>

      <!-- 联系时间线 -->
      <el-tab-pane label="联系时间线" name="timeline">
        <el-timeline>
          <el-timeline-item v-for="(item, idx) in timeline" :key="idx" :timestamp="item.time" placement="top" :type="item.type">
            <el-card>
              <p><b>{{ item.title }}</b></p>
              <p class="text-gray">{{ item.desc }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="timeline.length === 0" description="暂无时间线" />
      </el-tab-pane>
    </el-tabs>

    <!-- 新增互动弹窗 -->
    <el-dialog v-model="interactionOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></div>
          <span class="rd-detail-header-title">新增互动记录</span>
        </div>
      </template>
      <el-form ref="interactionRef" :model="interactionForm" :rules="interactionRules" label-width="100px">
        <el-form-item label="互动类型" prop="interactType">
          <el-select v-model="interactionForm.interactType" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_interaction_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="互动时间" prop="interactTime">
          <el-date-picker v-model="interactionForm.interactTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="互动内容" prop="content"><el-input v-model="interactionForm.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="下次跟进时间"><el-date-picker v-model="interactionForm.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item>
        <el-form-item label="下次跟进内容"><el-input v-model="interactionForm.nextContent" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitInteraction">确 定</el-button>
        <el-button @click="interactionOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkContactDetail">
import { useRoute, useRouter } from 'vue-router'
import { getContact } from '@/api/mk/contact'
import { listInteraction, addInteraction } from '@/api/mk/interaction'
import { listParticipant } from '@/api/mk/participant'

const route = useRoute()
const router = useRouter()
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])
const { proxy } = getCurrentInstance()
const { marketing_contact_role, marketing_interaction_type, marketing_participate_status } = proxy.useDict('marketing_contact_role', 'marketing_interaction_type', 'marketing_participate_status')

const contactId = route.params && route.params.contactId
const loading = ref(true)
const activeTab = ref('basic')
const contact = ref({})
const interactions = ref([])
const activityParticipants = ref([])
const timeline = ref([])
const interactionOpen = ref(false)

const interactionForm = ref({})
const interactionRules = {
  interactType: [{ required: true, message: '请选择互动类型', trigger: 'change' }],
  interactTime: [{ required: true, message: '请选择互动时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入互动内容', trigger: 'blur' }]
}

function getContactData() {
  loading.value = true
  getContact(contactId).then(res => {
    contact.value = res.data
    loading.value = false
  })
}

function getInteractions() {
  listInteraction({ contactId: contactId, pageNum: 1, pageSize: 999 }).then(res => { interactions.value = res.rows || [] })
}

function getActivityParticipants() {
  // 通过联系人手机号查询参与记录
  listParticipant({ contactPhone: contact.value.phone, pageNum: 1, pageSize: 999 }).then(res => {
    // 过滤出contactId匹配的记录
    activityParticipants.value = (res.rows || []).filter(p => p.contactId == contactId || p.contactPhone === contact.value.phone)
  })
}

function buildTimeline() {
  const items = []
  // 互动记录
  interactions.value.forEach(i => {
    items.push({ time: i.interactTime, title: '互动记录', desc: i.content, type: 'primary' })
    if (i.nextTime) {
      items.push({ time: i.nextTime, title: '计划跟进', desc: i.nextContent || '', type: 'warning' })
    }
  })
  // 活动参与
  activityParticipants.value.forEach(p => {
    items.push({ time: p.signTime || p.createTime, title: '参与活动：' + p.activityName, desc: '状态：' + (p.participateStatus === '1' ? '已签到' : '已报名'), type: 'success' })
  })
  // 按时间倒序
  items.sort((a, b) => (b.time || '').localeCompare(a.time || ''))
  timeline.value = items
}

function goBack() { router.push('/mk/customer/contact') }
function handleEdit() { router.push('/mk/customer/contact') }

function handleAddInteraction() {
  interactionForm.value = {
    contactId: contactId,
    customerId: contact.value.customerId,
    contactName: contact.value.name,
    interactTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
    userId: undefined
  }
  interactionOpen.value = true
}

function submitInteraction() {
  proxy.$refs['interactionRef'].validate(valid => {
    if (valid) {
      addInteraction(interactionForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        interactionOpen.value = false
        getInteractions()
      })
    }
  })
}

// 监听tab切换时加载对应数据
watch(activeTab, (val) => {
  if (val === 'activities' && activityParticipants.value.length === 0) {
    getActivityParticipants()
  }
  if (val === 'timeline') {
    if (activityParticipants.value.length === 0) getActivityParticipants()
    setTimeout(() => buildTimeline(), 500)
  }
})

getContactData()
getInteractions()
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.mb8 { margin-bottom: 8px; }
.ml5 { margin-left: 5px; }
.ml10 { margin-left: 10px; }
.text-gray { color: #999; }
.interaction-item { margin-bottom: 8px; }
.interaction-type { margin-right: 10px; }
.interaction-content { font-size: 14px; }
.interaction-next { margin-top: 8px; }
.interaction-meta { margin-top: 4px; font-size: 12px; }

.info-group { margin-bottom: 20px; }
.info-group:last-child { margin-bottom: 0; }
</style>
