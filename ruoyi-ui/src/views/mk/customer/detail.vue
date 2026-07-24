<template>
  <div class="app-container">
    <el-page-header @back="goBack" class="mb16">
      <template #content>
        <span class="text-large font-600 mr-3">{{ customer.customerName || '客户详情' }}</span>
        <el-tag v-if="customer.customerLevel" :type="levelTagType(customer.customerLevel)" size="small">{{ levelLabel(customer.customerLevel) }}</el-tag>
        <el-tag v-if="customer.customerStatus" :type="statusTagType(customer.customerStatus)" size="small" class="ml5">{{ statusLabel(customer.customerStatus) }}</el-tag>
        <el-tag v-if="!customer.userId" type="warning" size="small" class="ml5">公海客户</el-tag>
      </template>
      <template #extra>
        <el-button type="primary" @click="handleEdit" v-hasPermi="['marketing:customer:edit']">编辑</el-button>
        <el-button @click="goBack">返回列表</el-button>
      </template>
    </el-page-header>

    <el-tabs v-model="activeTab" v-loading="loading">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <!-- 客户信息 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">客户编号</span><div class="rd-value">{{ customer.customerNo }}</div></div>
          <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ customer.customerName }}</div></div>
          <div class="rd-item"><span class="rd-label">信用代码</span><div class="rd-value">{{ customer.creditCode }}</div></div>
          <div class="rd-item"><span class="rd-label">客户等级</span><div class="rd-value"><dict-tag :options="marketing_customer_level" :value="customer.customerLevel" /></div></div>
          <div class="rd-item"><span class="rd-label">客户来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="customer.customerSource" /></div></div>
          <div class="rd-item"><span class="rd-label">客户状态</span><div class="rd-value"><dict-tag :options="marketing_customer_status" :value="customer.customerStatus" /></div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">客户标签</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ customer.tagNames || '暂无' }}</div></div>
        </div>

        <!-- 企业信息 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="customer.industry" /></div></div>
          <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="customer.companySize" /></div></div>
          <div class="rd-item"><span class="rd-label">官网</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ customer.website || '暂无' }}</div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ customer.address || '暂无' }}</div></div>
        </div>

        <!-- 业务信息 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">首次接触</span><div class="rd-value">{{ customer.firstContactDate }}</div></div>
          <div class="rd-item"><span class="rd-label">合作开始</span><div class="rd-value">{{ customer.cooperationDate }}</div></div>
          <div class="rd-item"><span class="rd-label">累计交易额</span><div class="rd-value">{{ customer.totalAmount }}</div></div>
        </div>

        <!-- 负责信息 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ customer.userName || '未分配（公海）' }}</div></div>
          <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ customer.deptName || '暂无' }}</div></div>
          <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ customer.createTime }}</div></div>
        </div>

        <!-- 补充信息 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': true }">{{ customer.remark || '暂无' }}</div></div>
        </div>

        <!-- 状态流转操作 -->
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">当前状态</span><div class="rd-value"><dict-tag :options="marketing_customer_status" :value="customer.customerStatus" /></div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">状态操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleStatusChange('1')" v-if="customer.customerStatus === '0'" v-hasPermi="['marketing:customer:edit']">转为签约</el-button>
            <el-button size="small" type="success" plain @click="handleStatusChange('2')" v-if="customer.customerStatus === '1'" v-hasPermi="['marketing:customer:edit']">转为合作中</el-button>
            <el-button size="small" type="warning" plain @click="handleStatusChange('3')" v-if="customer.customerStatus === '2'" v-hasPermi="['marketing:customer:edit']">暂停合作</el-button>
            <el-button size="small" type="success" plain @click="handleStatusChange('2')" v-if="customer.customerStatus === '3'" v-hasPermi="['marketing:customer:edit']">恢复合作</el-button>
            <el-button size="small" type="danger" plain @click="handleStatusChange('4')" v-if="['0','1','2','3'].includes(customer.customerStatus)" v-hasPermi="['marketing:customer:edit']">标记流失</el-button>
            <el-button size="small" type="primary" plain @click="handleStatusChange('0')" v-if="customer.customerStatus === '4'" v-hasPermi="['marketing:customer:edit']">重新激活</el-button></div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">分配操作</span><div class="rd-value"><el-button size="small" type="primary" plain @click="handleAssign" v-if="customer.userId" v-hasPermi="['marketing:customer:assign']">转移分配</el-button>
            <el-button size="small" type="warning" plain @click="handleRelease" v-if="customer.userId" v-hasPermi="['marketing:customer:edit']">释放到公海</el-button>
            <el-button size="small" type="success" plain @click="handleClaim" v-if="!customer.userId" v-hasPermi="['marketing:customer:claim']">领取客户</el-button></div></div>
        </div>
      </el-tab-pane>

      <!-- 联系人列表 -->
      <el-tab-pane name="contacts">
        <template #label>联系人<el-badge :value="contacts.length" :hidden="contacts.length === 0" class="ml5" /></template>
        <el-table :data="contacts" border size="small">
          <el-table-column label="姓名" prop="name" width="100">
            <template #default="scope">
              <el-button link type="primary" @click="goContactDetail(scope.row)">{{ scope.row.name }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="职位" prop="position" width="120" show-overflow-tooltip />
          <el-table-column label="手机号" prop="phone" width="130" />
          <el-table-column label="邮箱" prop="email" show-overflow-tooltip />
          <el-table-column label="关键联系人" prop="isKey" width="100" align="center">
            <template #default="scope"><el-tag :type="scope.row.isKey === '1' ? 'danger' : 'info'" size="small">{{ scope.row.isKey === '1' ? '是' : '否' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="主要联系人" prop="isPrimary" width="100" align="center">
            <template #default="scope"><el-tag :type="scope.row.isPrimary === '1' ? 'success' : 'info'" size="small">{{ scope.row.isPrimary === '1' ? '是' : '否' }}</el-tag></template>
          </el-table-column>
        </el-table>
        <el-empty v-if="contacts.length === 0" description="暂无联系人" />
      </el-tab-pane>

      <!-- 商机列表 -->
      <el-tab-pane name="opportunities">
        <template #label>商机<el-badge :value="opportunities.length" :hidden="opportunities.length === 0" class="ml5" /></template>
        <el-table :data="opportunities" border size="small">
          <el-table-column label="商机编号" prop="opportunityNo" width="150" />
          <el-table-column label="商机名称" prop="opportunityName" show-overflow-tooltip />
          <el-table-column label="销售阶段" prop="stageName" width="100" align="center" />
          <el-table-column label="预计金额" prop="expectedAmount" width="120" align="right" />
          <el-table-column label="赢率" prop="winRate" width="80" align="center">
            <template #default="scope">{{ scope.row.winRate }}%</template>
          </el-table-column>
          <el-table-column label="商机状态" prop="opportunityStatus" width="100" align="center">
            <template #default="scope"><dict-tag :options="marketing_opportunity_status" :value="scope.row.opportunityStatus" /></template>
          </el-table-column>
        </el-table>
        <el-empty v-if="opportunities.length === 0" description="暂无商机" />
      </el-tab-pane>

      <!-- 合同列表 -->
      <el-tab-pane name="contracts">
        <template #label>合同<el-badge :value="contracts.length" :hidden="contracts.length === 0" class="ml5" /></template>
        <el-table :data="contracts" border size="small">
          <el-table-column label="合同编号" prop="contractNo" width="150" />
          <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
          <el-table-column label="合同金额" prop="contractAmount" width="120" align="right" />
          <el-table-column label="签订日期" prop="signDate" width="120" />
          <el-table-column label="合同状态" prop="contractStatus" width="100" align="center">
            <template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template>
          </el-table-column>
        </el-table>
        <el-empty v-if="contracts.length === 0" description="暂无合同" />
      </el-tab-pane>

      <!-- 订单列表 -->
      <el-tab-pane name="orders">
        <template #label>订单<el-badge :value="orders.length" :hidden="orders.length === 0" class="ml5" /></template>
        <el-table :data="orders" border size="small">
          <el-table-column label="订单编号" prop="orderNo" width="150" />
          <el-table-column label="订单名称" prop="orderName" show-overflow-tooltip />
          <el-table-column label="订单金额" prop="orderAmount" width="120" align="right" />
          <el-table-column label="下单日期" prop="orderDate" width="120" />
          <el-table-column label="订单状态" prop="orderStatus" width="100" align="center">
            <template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template>
          </el-table-column>
        </el-table>
        <el-empty v-if="orders.length === 0" description="暂无订单" />
      </el-tab-pane>

      <!-- 互动记录 -->
      <el-tab-pane name="interactions">
        <template #label>互动记录<el-badge :value="interactions.length" :hidden="interactions.length === 0" class="ml5" /></template>
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
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="interactions.length === 0" description="暂无互动记录" />
      </el-tab-pane>

      <!-- 参与活动 -->
      <el-tab-pane name="activities">
        <template #label>参与活动<el-badge :value="activities.length" :hidden="activities.length === 0" class="ml5" /></template>
        <el-table :data="activities" border size="small">
          <el-table-column label="活动名称" prop="activityName" show-overflow-tooltip />
          <el-table-column label="联系人" prop="contactName" width="100" />
          <el-table-column label="企业名称" prop="companyName" width="180" show-overflow-tooltip />
          <el-table-column label="参与状态" prop="participateStatus" width="100" align="center">
            <template #default="scope"><dict-tag :options="marketing_participate_status" :value="scope.row.participateStatus" /></template>
          </el-table-column>
          <el-table-column label="签到时间" prop="signTime" width="160" />
        </el-table>
        <el-empty v-if="activities.length === 0" description="暂无活动参与记录" />
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
          <span class="rd-detail-header-title">分配客户</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="assignUserId" filterable clearable placeholder="请选择（留空释放到公海）" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAssign">确 定</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkCustomerDetail">
import { useRoute, useRouter } from 'vue-router'
import { getCustomer, changeCustomerStatus, assignCustomer, releaseToPool, claimCustomer } from '@/api/mk/customer'
import { listContact } from '@/api/mk/contact'
import { listOpportunity } from '@/api/mk/opportunity'
import { listContract } from '@/api/mk/contract'
import { listOrder } from '@/api/mk/order'
import { listInteraction } from '@/api/mk/interaction'
import { listParticipant } from '@/api/mk/participant'
import { listUser } from '@/api/system/user'

const route = useRoute()
const router = useRouter()
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])
const { proxy } = getCurrentInstance()
const {
  marketing_customer_level, marketing_customer_source, marketing_customer_status,
  marketing_industry, marketing_company_size,
  marketing_opportunity_status, marketing_contract_status, marketing_order_status,
  marketing_interaction_type, marketing_participate_status
} = proxy.useDict(
  'marketing_customer_level', 'marketing_customer_source', 'marketing_customer_status',
  'marketing_industry', 'marketing_company_size',
  'marketing_opportunity_status', 'marketing_contract_status', 'marketing_order_status',
  'marketing_interaction_type', 'marketing_participate_status'
)

const customerId = route.params && route.params.customerId
const loading = ref(true)
const activeTab = ref('basic')
const customer = ref({})
const contacts = ref([])
const opportunities = ref([])
const contracts = ref([])
const orders = ref([])
const interactions = ref([])
const activities = ref([])
const timeline = ref([])
const userOptions = ref([])
const assignOpen = ref(false)
const assignUserId = ref(null)

function levelTagType(val) {
  return { '1': 'danger', '2': 'warning', '3': '', '4': 'info' }[val] || ''
}
function levelLabel(val) {
  return { '1': '关键大客户', '2': '重要客户', '3': '普通客户', '4': '潜在客户' }[val] || ''
}
function statusTagType(val) {
  return { '0': 'primary', '1': 'success', '2': 'success', '3': 'warning', '4': 'info' }[val] || ''
}
function statusLabel(val) {
  return { '0': '意向客户', '1': '签约客户', '2': '合作中', '3': '暂停合作', '4': '流失客户' }[val] || ''
}

function getCustomerData() {
  loading.value = true
  getCustomer(customerId).then(res => {
    customer.value = res.data
    loading.value = false
  })
}

function getContacts() {
  listContact({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    contacts.value = res.rows || []
  })
}

function getOpportunities() {
  listOpportunity({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    opportunities.value = res.rows || []
  })
}

function getContracts() {
  listContract({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    contracts.value = res.rows || []
  })
}

function getOrders() {
  listOrder({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    orders.value = res.rows || []
  })
}

function getInteractions() {
  listInteraction({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    interactions.value = res.rows || []
  })
}

function getActivities() {
  listParticipant({ customerId: customerId, pageNum: 1, pageSize: 999 }).then(res => {
    activities.value = res.rows || []
  })
}

function buildTimeline() {
  const items = []
  interactions.value.forEach(i => {
    items.push({ time: i.interactTime, title: '互动记录', desc: i.content, type: 'primary' })
    if (i.nextTime) {
      items.push({ time: i.nextTime, title: '计划跟进', desc: i.nextContent || '', type: 'warning' })
    }
  })
  opportunities.value.forEach(o => {
    items.push({ time: o.createTime, title: '商机：' + o.opportunityName, desc: '预计金额：' + o.expectedAmount, type: 'success' })
  })
  contracts.value.forEach(c => {
    items.push({ time: c.signDate || c.createTime, title: '合同：' + c.contractName, desc: '金额：' + c.contractAmount, type: 'success' })
  })
  orders.value.forEach(o => {
    items.push({ time: o.orderDate || o.createTime, title: '订单：' + o.orderName, desc: '金额：' + o.orderAmount, type: 'success' })
  })
  activities.value.forEach(p => {
    items.push({ time: p.signTime || p.createTime, title: '参与活动：' + p.activityName, desc: '状态：' + (p.participateStatus === '1' ? '已签到' : '已报名'), type: 'success' })
  })
  items.sort((a, b) => (b.time || '').localeCompare(a.time || ''))
  timeline.value = items
}

function getUserOptions() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => {
    userOptions.value = res.rows.filter(u => u.userId !== 1)
  })
}

function goBack() { router.push('/mk/customer/list') }
function handleEdit() { router.push('/mk/customer/list') }
function goContactDetail(row) { router.push('/mk/contact-detail/' + row.contactId) }

// 状态流转
function handleStatusChange(status) {
  const statusText = statusLabel(status)
  proxy.$modal.confirm('确认将客户状态变更为"' + statusText + '"？').then(() => {
    changeCustomerStatus(customerId, status).then(() => {
      proxy.$modal.msgSuccess('状态变更成功')
      getCustomerData()
    })
  }).catch(() => {})
}

// 分配/转移
function handleAssign() {
  assignUserId.value = customer.value.userId || null
  assignOpen.value = true
}
function confirmAssign() {
  const data = { userId: assignUserId.value }
  const user = userOptions.value.find(u => u.userId === assignUserId.value)
  if (user) {
    data.deptId = user.deptId
    data.userName = user.nickName
  }
  assignCustomer(customerId, data).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getCustomerData()
  })
}

// 释放到公海
function handleRelease() {
  proxy.$modal.confirm('确认将该客户释放到公海？释放后其他销售人员可以领取。').then(() => {
    releaseToPool(customerId).then(() => {
      proxy.$modal.msgSuccess('已释放到公海')
      getCustomerData()
    })
  }).catch(() => {})
}

// 领取客户
function handleClaim() {
  proxy.$modal.confirm('确认领取该客户？领取后您将成为该客户的负责人。').then(() => {
    claimCustomer(customerId).then(() => {
      proxy.$modal.msgSuccess('领取成功')
      getCustomerData()
    })
  }).catch(() => {})
}

// 监听tab切换时加载对应数据
watch(activeTab, (val) => {
  if (val === 'contacts' && contacts.value.length === 0) getContacts()
  if (val === 'opportunities' && opportunities.value.length === 0) getOpportunities()
  if (val === 'contracts' && contracts.value.length === 0) getContracts()
  if (val === 'orders' && orders.value.length === 0) getOrders()
  if (val === 'interactions' && interactions.value.length === 0) getInteractions()
  if (val === 'activities' && activities.value.length === 0) getActivities()
  if (val === 'timeline') {
    if (contacts.value.length === 0) getContacts()
    if (opportunities.value.length === 0) getOpportunities()
    if (contracts.value.length === 0) getContracts()
    if (orders.value.length === 0) getOrders()
    if (interactions.value.length === 0) getInteractions()
    if (activities.value.length === 0) getActivities()
    setTimeout(() => buildTimeline(), 800)
  }
})

getCustomerData()
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
