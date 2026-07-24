<template>
  <div class="app-container">
    <el-page-header @back="goBack" class="mb16">
      <template #content>
        <span class="text-large font-600 mr-3">{{ lead.companyName || '线索详情' }}</span>
        <el-tag v-if="lead.leadGrade" :type="gradeTagType(lead.leadGrade)" size="small">{{ gradeLabel(lead.leadGrade) }}</el-tag>
        <el-tag v-if="lead.leadStatus" :type="statusTagType(lead.leadStatus)" size="small" class="ml5">{{ statusLabel(lead.leadStatus) }}</el-tag>
        <el-tag v-if="lead.isPublic === '1'" type="warning" size="small" class="ml5">公海</el-tag>
      </template>
      <template #extra>
        <el-button type="primary" @click="handleEdit" v-hasPermi="['marketing:lead:edit']">编辑</el-button>
        <el-button type="success" @click="handleConvert" v-if="lead.leadStatus !== '4' && lead.leadStatus !== '5'" v-hasPermi="['marketing:lead:edit']">转化为客户</el-button>
        <el-button @click="goBack">返回列表</el-button>
      </template>
    </el-page-header>

    <el-tabs v-model="activeTab" v-loading="loading">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">线索编号</span><div class="rd-value">{{ lead.leadNo }}</div></div>
          <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ lead.companyName }}</div></div>
          <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ lead.contactName }}</div></div>
          <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ lead.contactPhone }}</div></div>
          <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.contactEmail || '暂无' }}</div></div>
          <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.position || '暂无' }}</div></div>
        </div>

        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="lead.industry" /></div></div>
          <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="lead.companySize" /></div></div>
          <div class="rd-item"><span class="rd-label">线索来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="lead.leadSource" /></div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.address || '暂无' }}</div></div>
        </div>

        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">线索评分</span><div class="rd-value">{{ lead.leadScore }}</div></div>
          <div class="rd-item"><span class="rd-label">线索等级</span><div class="rd-value"><dict-tag :options="marketing_lead_grade" :value="lead.leadGrade" /></div></div>
          <div class="rd-item"><span class="rd-label">线索状态</span><div class="rd-value"><dict-tag :options="marketing_lead_status" :value="lead.leadStatus" /></div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">需求描述</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.requirementDesc || '暂无' }}</div></div>
        </div>

        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ lead.userName || '未分配（公海）' }}</div></div>
          <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.deptName || '暂无' }}</div></div>
          <div class="rd-item"><span class="rd-label">领取时间</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.receiveTime || '暂无' }}</div></div>
          <div class="rd-item"><span class="rd-label">最后跟进</span><div class="rd-value">{{ lead.lastFollowTime || '未跟进' }}</div></div>
          <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ lead.createTime }}</div></div>
        </div>

        <!-- 转化信息 -->
        <el-descriptions v-if="lead.convertCustomerId" :column="3" border title="转化信息" class="info-group">
          <div class="rd-item"><span class="rd-label">转化状态</span><div class="rd-value"><el-tag type="success" size="small">已转化</el-tag></div></div>
          <div class="rd-item"><span class="rd-label">转化时间</span><div class="rd-value">{{ lead.convertTime }}</div></div>
          <div class="rd-item"><span class="rd-label">转化客户</span><div class="rd-value"><el-button link type="primary" @click="goCustomerDetail(lead.convertCustomerId)">查看客户</el-button></div></div>
        </div>

        <!-- 无效信息 -->
        <el-descriptions v-if="lead.leadStatus === '5'" :column="3" border title="无效信息" class="info-group">
          <div class="rd-item"><span class="rd-label">无效原因</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.ineffectiveReason || '暂无' }}</div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">无效说明</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.ineffectiveRemark || '暂无' }}</div></div>
        </div>

        <!-- 状态流转操作 -->
        <el-descriptions :column="3" border title="状态管理" class="info-group" v-if="lead.leadStatus !== '4' && lead.leadStatus !== '5'">
          <div class="rd-item"><span class="rd-label">当前状态</span><div class="rd-value"><dict-tag :options="marketing_lead_status" :value="lead.leadStatus" /></div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">状态操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleStatusChange('1')" v-if="lead.leadStatus === '0'" v-hasPermi="['marketing:lead:edit']">标记已联系</el-button>
            <el-button size="small" type="success" plain @click="handleStatusChange('2')" v-if="lead.leadStatus === '0' || lead.leadStatus === '1'" v-hasPermi="['marketing:lead:edit']">标记有意向</el-button>
            <el-button size="small" type="warning" plain @click="handleInvalidate" v-hasPermi="['marketing:lead:edit']">标记无效</el-button></div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">分配操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleAssign" v-if="lead.userId" v-hasPermi="['marketing:lead:assign']">转移分配</el-button>
            <el-button size="small" type="warning" plain @click="handleRelease" v-if="lead.userId" v-hasPermi="['marketing:lead:edit']">退回公海</el-button></div></div>
        </div>

        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ lead.remark || '暂无' }}</div></div>
        </div>
      </el-tab-pane>

      <!-- 跟进记录 -->
      <el-tab-pane name="interactions">
        <template #label>跟进记录<el-badge :value="interactions.length" :hidden="interactions.length === 0" class="ml5" /></template>
        <div style="margin-bottom: 12px">
          <el-button type="primary" plain icon="Plus" size="small" @click="handleAddInteraction" v-hasPermi="['marketing:interaction:add']">新增跟进</el-button>
        </div>
        <el-timeline>
          <el-timeline-item v-for="item in interactions" :key="item.recordId" :timestamp="item.interactTime" placement="top" :type="item.nextTime ? 'warning' : 'primary'">
            <el-card>
              <div class="interaction-item">
                <span class="interaction-type"><dict-tag :options="marketing_interaction_type" :value="item.interactType" /></span>
                <span class="interaction-content">{{ item.content }}</span>
              </div>
              <div v-if="item.contactName" class="interaction-meta">
                <span class="text-gray">联系人：{{ item.contactName }}</span>
              </div>
              <div v-if="item.nextTime" class="interaction-next">
                <el-tag size="small" type="warning">下次跟进：{{ item.nextTime }}</el-tag>
                <span v-if="item.nextContent" class="text-gray ml5">{{ item.nextContent }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="interactions.length === 0" description="暂无跟进记录" />
      </el-tab-pane>

      <!-- 时间线 -->
      <el-tab-pane label="时间线" name="timeline">
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

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">分配线索</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="assignUserId" filterable clearable placeholder="请选择负责人" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAssign">确 定</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 无效标记弹窗 -->
    <el-dialog v-model="invalidateOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">标记线索无效</span>
        </div>
      </template>
      <el-form ref="invalidateRef" :model="invalidateForm" label-width="80px">
        <el-form-item label="无效原因" prop="ineffectiveReason">
          <el-select v-model="invalidateForm.ineffectiveReason" placeholder="请选择无效原因" style="width: 100%">
            <el-option label="电话空号/停机" value="phone_invalid" />
            <el-option label="客户明确拒绝" value="rejected" />
            <el-option label="需求不匹配" value="no_match" />
            <el-option label="重复线索" value="duplicate" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="无效说明" prop="ineffectiveRemark">
          <el-input v-model="invalidateForm.ineffectiveRemark" type="textarea" :rows="3" placeholder="请输入无效说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmInvalidate">确认标记无效</el-button>
        <el-button @click="invalidateOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 快速新增跟进弹窗 -->
    <el-dialog v-model="interactionOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div>
          <span class="rd-detail-header-title">新增跟进</span>
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
        <el-form-item label="互动内容" prop="content">
          <el-input v-model="interactionForm.content" type="textarea" :rows="3" placeholder="请输入跟进内容" />
        </el-form-item>
        <el-form-item label="跟进人" prop="userId">
          <el-select v-model="interactionForm.userId" filterable clearable placeholder="请选择跟进人" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="下次跟进">
          <el-date-picker v-model="interactionForm.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间（可选）" style="width: 100%" />
        </el-form-item>
        <el-form-item label="跟进内容">
          <el-input v-model="interactionForm.nextContent" type="textarea" :rows="2" placeholder="下次跟进内容（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAddInteraction">确 定</el-button>
        <el-button @click="interactionOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkLeadDetail">
import { useRoute, useRouter } from 'vue-router'
import { getLead, updateLead, convertLead, assignLead, releaseLeadToPool, invalidateLead, updateFollowTime } from '@/api/mk/lead'
import { listInteraction, addInteraction } from '@/api/mk/interaction'
import { listUser } from '@/api/system/user'

const route = useRoute()
const router = useRouter()
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])
const { proxy } = getCurrentInstance()
const {
  marketing_lead_status, marketing_lead_grade, marketing_customer_source,
  marketing_industry, marketing_company_size, marketing_interaction_type
} = proxy.useDict(
  'marketing_lead_status', 'marketing_lead_grade', 'marketing_customer_source',
  'marketing_industry', 'marketing_company_size', 'marketing_interaction_type'
)

const leadId = route.params && route.params.leadId
const loading = ref(true)
const activeTab = ref('basic')
const lead = ref({})
const interactions = ref([])
const timeline = ref([])
const userOptions = ref([])

// 分配相关
const assignOpen = ref(false)
const assignUserId = ref(null)

// 无效标记
const invalidateOpen = ref(false)
const invalidateForm = ref({})

// 新增跟进
const interactionOpen = ref(false)
const interactionForm = ref({})
const interactionRules = {
  interactType: [{ required: true, message: '请选择互动类型', trigger: 'change' }],
  interactTime: [{ required: true, message: '请选择互动时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入互动内容', trigger: 'blur' }]
}

function gradeTagType(val) {
  return { 'A': 'danger', 'B': 'warning', 'C': '', 'D': 'info' }[val] || ''
}
function gradeLabel(val) {
  return { 'A': 'A级', 'B': 'B级', 'C': 'C级', 'D': 'D级' }[val] || ''
}
function statusTagType(val) {
  return { '0': 'info', '1': 'primary', '2': 'warning', '4': 'success', '5': 'danger' }[val] || ''
}
function statusLabel(val) {
  return { '0': '待跟进', '1': '已联系', '2': '有意向', '4': '已转化', '5': '无效' }[val] || ''
}

function getLeadData() {
  loading.value = true
  getLead(leadId).then(res => {
    lead.value = res.data
    loading.value = false
  })
}

function getInteractions() {
  listInteraction({ leadId: leadId, pageNum: 1, pageSize: 999 }).then(res => {
    interactions.value = res.rows || []
  })
}

function getUserOptions() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => {
    userOptions.value = res.rows.filter(u => u.userId !== 1)
  })
}

function buildTimeline() {
  const items = []
  if (lead.value.createTime) {
    items.push({ time: lead.value.createTime, title: '线索创建', desc: '企业：' + lead.value.companyName, type: 'primary' })
  }
  if (lead.value.receiveTime) {
    items.push({ time: lead.value.receiveTime, title: '线索领取', desc: '负责人：' + (lead.value.userName || '-'), type: 'success' })
  }
  interactions.value.forEach(i => {
    items.push({ time: i.interactTime, title: '跟进记录', desc: i.content, type: 'primary' })
    if (i.nextTime) {
      items.push({ time: i.nextTime, title: '计划跟进', desc: i.nextContent || '', type: 'warning' })
    }
  })
  if (lead.value.convertTime) {
    items.push({ time: lead.value.convertTime, title: '线索转化', desc: '已转化为客户', type: 'success' })
  }
  items.sort((a, b) => (b.time || '').localeCompare(a.time || ''))
  timeline.value = items
}

function goBack() { router.push('/mk/lead/list') }
function handleEdit() { router.push('/mk/lead/list') }
function goCustomerDetail(customerId) { router.push('/mk/customer-detail/' + customerId) }

// 状态流转
function handleStatusChange(status) {
  const statusText = statusLabel(status)
  proxy.$modal.confirm('确认将线索状态变更为"' + statusText + '"？').then(() => {
    updateLead({ leadId: leadId, leadStatus: status }).then(() => {
      proxy.$modal.msgSuccess('状态变更成功')
      getLeadData()
    })
  }).catch(() => {})
}

// 转化
function handleConvert() {
  proxy.$modal.confirm('确认将线索"' + lead.value.companyName + '"转化为客户？转化后将自动创建客户和联系人。').then(() => {
    convertLead(leadId, {}).then(res => {
      proxy.$modal.msgSuccess('转化成功')
      getLeadData()
      // 跳转到新创建的客户详情页
      if (res.convertCustomerId) {
        router.push('/mk/customer-detail/' + res.convertCustomerId)
      }
    })
  }).catch(() => {})
}

// 分配
function handleAssign() {
  assignUserId.value = lead.value.userId || null
  assignOpen.value = true
}
function confirmAssign() {
  const data = { userId: assignUserId.value }
  const user = userOptions.value.find(u => u.userId === assignUserId.value)
  if (user) {
    data.deptId = user.deptId
    data.userName = user.nickName
    if (user.dept) data.deptName = user.dept.deptName
  }
  assignLead(leadId, data).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getLeadData()
  })
}

// 退回公海
function handleRelease() {
  proxy.$modal.confirm('确认将该线索退回公海？退回后其他销售人员可以领取。').then(() => {
    releaseLeadToPool(leadId).then(() => {
      proxy.$modal.msgSuccess('已退回公海')
      getLeadData()
    })
  }).catch(() => {})
}

// 标记无效
function handleInvalidate() {
  invalidateForm.value = { ineffectiveReason: undefined, ineffectiveRemark: undefined }
  invalidateOpen.value = true
}
function confirmInvalidate() {
  if (!invalidateForm.value.ineffectiveReason) {
    proxy.$modal.msgWarning('请选择无效原因')
    return
  }
  invalidateLead(leadId, invalidateForm.value).then(() => {
    proxy.$modal.msgSuccess('已标记为无效')
    invalidateOpen.value = false
    getLeadData()
  })
}

// 新增跟进
function handleAddInteraction() {
  interactionForm.value = {
    leadId: leadId,
    interactType: undefined,
    interactTime: undefined,
    content: undefined,
    userId: lead.value.userId,
    nextTime: undefined,
    nextContent: undefined
  }
  interactionOpen.value = true
}
function confirmAddInteraction() {
  proxy.$refs['interactionRef'].validate(valid => {
    if (valid) {
      addInteraction(interactionForm.value).then(() => {
        proxy.$modal.msgSuccess('跟进记录添加成功')
        interactionOpen.value = false
        getInteractions()
        // 更新线索最后跟进时间
        updateFollowTime(leadId).then(() => {
          getLeadData()
        })
      })
    }
  })
}

// 监听tab切换
watch(activeTab, (val) => {
  if (val === 'interactions' && interactions.value.length === 0) getInteractions()
  if (val === 'timeline') {
    if (interactions.value.length === 0) getInteractions()
    setTimeout(() => buildTimeline(), 500)
  }
})

getLeadData()
getUserOptions()
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.ml5 { margin-left: 5px; }
.text-gray { color: #999; }
.interaction-item { margin-bottom: 8px; }
.interaction-type { margin-right: 10px; }
.interaction-content { font-size: 14px; }
.interaction-next { margin-top: 8px; }
.interaction-meta { margin-top: 4px; font-size: 12px; }
.info-group { margin-bottom: 20px; }
.info-group:last-child { margin-bottom: 0; }
</style>
