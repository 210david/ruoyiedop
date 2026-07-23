<template>
  <div class="app-container">
    <!-- 顶部信息栏 -->
    <el-page-header @back="goBack" class="mb16">
      <template #content>
        <span class="text-large font-600 mr-3">{{ activity.activityName || '活动详情' }}</span>
        <el-tag v-if="activity.activityStatus" :type="statusTagType(activity.activityStatus)" size="small">
          {{ statusLabel(activity.activityStatus) }}
        </el-tag>
      </template>
      <template #extra>
        <el-button @click="goBack">返回列表</el-button>
      </template>
    </el-page-header>

    <el-tabs v-model="activeTab" v-loading="loading">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <!-- 活动信息 -->
        <el-descriptions title="活动信息" :column="3" border class="detail-group">
          <el-descriptions-item label="活动编号">{{ activity.activityNo }}</el-descriptions-item>
          <el-descriptions-item label="活动名称">{{ activity.activityName }}</el-descriptions-item>
          <el-descriptions-item label="活动类型"><dict-tag :options="marketing_activity_type" :value="activity.activityType" /></el-descriptions-item>
          <el-descriptions-item label="活动形式"><dict-tag :options="marketing_activity_form" :value="activity.activityForm" /></el-descriptions-item>
          <el-descriptions-item label="活动状态"><dict-tag :options="marketing_activity_status" :value="activity.activityStatus" /></el-descriptions-item>
          <el-descriptions-item label="活动地点">{{ activity.location }}</el-descriptions-item>
        </el-descriptions>
        <!-- 时间与预算 -->
        <el-descriptions title="时间与预算" :column="3" border class="detail-group">
          <el-descriptions-item label="开始时间">{{ activity.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ activity.endTime }}</el-descriptions-item>
          <el-descriptions-item label="活动预算">{{ activity.budget }} 元</el-descriptions-item>
          <el-descriptions-item label="实际花费">{{ activity.cost }} 元</el-descriptions-item>
          <el-descriptions-item label="目标人数">{{ activity.targetCount }} 人</el-descriptions-item>
          <el-descriptions-item label="目标线索数">{{ activity.targetLeadCount }} 个</el-descriptions-item>
        </el-descriptions>
        <!-- 负责信息 -->
        <el-descriptions title="负责信息" :column="3" border class="detail-group">
          <el-descriptions-item label="负责人">{{ activity.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ activity.ownerPhone }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ activity.deptName }}</el-descriptions-item>
        </el-descriptions>
        <!-- 活动描述 -->
        <el-descriptions title="活动描述" :column="3" border class="detail-group">
          <el-descriptions-item label="活动简介" :span="3">{{ activity.summary }}</el-descriptions-item>
          <el-descriptions-item label="活动详情" :span="3">{{ activity.content }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ activity.remark }}</el-descriptions-item>
        </el-descriptions>
        <!-- 附件资料 -->
        <el-descriptions title="附件资料" :column="3" border class="detail-group">
          <el-descriptions-item label="附件" :span="3">
            <span v-if="!attachmentFileList.length">无</span>
            <div v-else>
              <div v-for="(file, index) in attachmentFileList" :key="index" class="detail-attachment-item">
                <el-link :underline="false" @click="handleAttachmentPreview(file)" type="primary">
                  <el-icon style="margin-right: 4px"><Document /></el-icon>
                  <span>{{ file.name }}</span>
                </el-link>
              </div>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <!-- 参与人列表 -->
      <el-tab-pane name="participants">
        <template #label>参与人<el-badge :value="participants.length" :hidden="participants.length === 0" class="ml5" /></template>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddParticipant" v-hasPermi="['marketing:participant:add']">新增参与人</el-button></el-col>
        </el-row>
        <el-table :data="participants" border size="small">
          <el-table-column label="企业名称" prop="companyName" show-overflow-tooltip />
          <el-table-column label="联系人" prop="contactName" width="100" />
          <el-table-column label="手机号" prop="contactPhone" width="130" />
          <el-table-column label="职位" prop="position" width="100" />
          <el-table-column label="状态" prop="participateStatus" width="90" align="center">
            <template #default="scope"><dict-tag :options="marketing_participate_status" :value="scope.row.participateStatus" /></template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleSignIn(scope.row)" v-if="scope.row.participateStatus !== '1'" v-hasPermi="['marketing:participant:edit']">签到</el-button>
              <el-button link type="primary" size="small" @click="handleConvertLead(scope.row)" v-if="!scope.row.leadId" v-hasPermi="['marketing:participant:convert']">转线索</el-button>
              <el-tag v-if="scope.row.leadId" type="success" size="small">已转线索</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 活动效果 -->
      <el-tab-pane label="活动效果" name="result">
        <el-descriptions :column="3" border class="mb16">
          <el-descriptions-item label="实际参与">{{ activity.actualCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="获取线索">{{ activity.leadCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="MQL数量">{{ activity.mqlCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="SQL数量">{{ activity.sqlCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="转化商机">{{ activity.opportunityCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="成交金额">{{ activity.dealAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="实际ROI(%)">{{ activity.actualRoi || 0 }}%</el-descriptions-item>
        </el-descriptions>
        <el-button type="warning" icon="DataAnalysis" @click="handleReview" v-hasPermi="['marketing:activity:review']" v-if="activity.activityStatus === '3'">一键复盘</el-button>
      </el-tab-pane>

      <!-- 报名推广 -->
      <el-tab-pane label="报名推广" name="promote" v-loading="promoteLoading">
        <el-row :gutter="20">
          <el-col :span="10">
            <el-card shadow="hover">
              <template #header><span>报名二维码</span></template>
              <div style="text-align: center;">
                <canvas ref="promoteQrRef" style="border-radius: 8px; border: 1px solid #ebeef5;"></canvas>
                <div style="margin-top: 12px;">
                  <el-button type="primary" size="small" icon="Download" @click="downloadPromoteQr">下载二维码</el-button>
                  <el-button type="success" size="small" icon="Promotion" @click="openPromotePage">预览报名页</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="14">
            <el-card shadow="hover">
              <template #header><span>报名链接</span></template>
              <el-input :model-value="promoteUrl" readonly style="margin-bottom: 16px;">
                <template #append>
                  <el-button @click="copyPromoteUrl">复制链接</el-button>
                </template>
              </el-input>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="报名人数">{{ activity.signupCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="签到人数">{{ activity.signedCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="目标人数">{{ activity.targetCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="转化线索">{{ activity.convertedLeadCount || 0 }}</el-descriptions-item>
              </el-descriptions>
              <el-divider content-position="left">使用说明</el-divider>
              <div style="font-size: 13px; color: #606266; line-height: 2;">
                <p><b>1. 线上推广</b>：复制链接通过微信/邮件/短信发给客户</p>
                <p><b>2. 线下推广</b>：下载二维码印制在海报/易拉宝/桌牌上</p>
                <p><b>3. 现场签到</b>：客户报名后获得签到码，活动当天扫码签到</p>
                <p><b>4. 线索转化</b>：签到后在"参与人"Tab一键转线索</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 操作日志 -->
      <el-tab-pane label="操作日志" name="logs">
        <el-timeline>
          <el-timeline-item v-for="log in statusLogs" :key="log.logId" :timestamp="log.createTime" placement="top">
            <el-card>
              <p><b>{{ log.operatorName }}</b> 将状态从
                <el-tag size="small" :type="statusTagType(log.fromStatus)">{{ statusLabel(log.fromStatus) }}</el-tag>
                变更为
                <el-tag size="small" :type="statusTagType(log.toStatus)">{{ statusLabel(log.toStatus) }}</el-tag>
              </p>
              <p v-if="log.remark" class="text-gray">备注：{{ log.remark }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="statusLogs.length === 0" description="暂无操作日志" />
      </el-tab-pane>
    </el-tabs>

    <!-- 文件在线预览 -->
    <file-preview ref="filePreviewRef" />

    <!-- 复盘弹窗 -->
    <el-dialog title="活动复盘" v-model="reviewOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="实际参与（签到）">{{ reviewData.signedCount }}</el-descriptions-item>
        <el-descriptions-item label="获取线索">{{ reviewData.leadCount }}</el-descriptions-item>
        <el-descriptions-item label="MQL数量">{{ reviewData.mqlCount }}</el-descriptions-item>
        <el-descriptions-item label="SQL数量">{{ reviewData.sqlCount }}</el-descriptions-item>
        <el-descriptions-item label="转化商机">{{ reviewData.opportunityCount }}</el-descriptions-item>
        <el-descriptions-item label="成交金额">{{ reviewData.dealAmount }}</el-descriptions-item>
        <el-descriptions-item label="活动预算">{{ reviewData.budget }}</el-descriptions-item>
        <el-descriptions-item label="实际ROI">{{ reviewData.actualRoi }}%</el-descriptions-item>
      </el-descriptions>
      <div class="mt16">
        <el-alert title="系统已根据参与人签到和线索转化数据自动统计，您可修改后保存" type="info" :closable="false" />
      </div>
      <template #footer>
        <el-button type="primary" @click="saveReviewData">保存复盘</el-button>
        <el-button @click="reviewOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 新增参与人弹窗 -->
    <el-dialog title="新增参与人" v-model="participantOpen" width="600px" append-to-body>
      <el-form ref="participantRef" :model="participantForm" :rules="participantRules" label-width="100px">
        <el-form-item label="从联系人库选择">
          <el-select v-model="participantForm.contactId" filterable clearable placeholder="选择后自动带出信息" style="width: 100%" @change="onContactSelect">
            <el-option v-for="c in contactOptions" :key="c.contactId" :label="c.name + ' (' + (c.customerName||'') + ')'" :value="c.contactId" />
          </el-select>
        </el-form-item>
        <el-form-item label="企业名称" prop="companyName"><el-input v-model="participantForm.companyName" /></el-form-item>
        <el-form-item label="联系人" prop="contactName"><el-input v-model="participantForm.contactName" /></el-form-item>
        <el-form-item label="手机号" prop="contactPhone"><el-input v-model="participantForm.contactPhone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="participantForm.contactEmail" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="participantForm.position" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitParticipant">确 定</el-button>
        <el-button @click="participantOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkActivityDetail">
import { useRoute, useRouter } from 'vue-router'
import QRCode from 'qrcode'
import { getActivity, getActivityStatusLogs, reviewActivity, saveReview, getRegisterUrl } from '@/api/mk/activity'
import { getParticipantsByActivity, addParticipant, signInParticipant, convertToLead } from '@/api/mk/participant'
import { listContact } from '@/api/mk/contact'

const filePreviewRef = ref(null)
const attachmentFileList = computed(() => {
  if (!activity.value.attachments) return []
  return activity.value.attachments.split(',').filter(url => url).map(url => ({
    name: url.substring(url.lastIndexOf('/') + 1),
    url: url
  }))
})
function handleAttachmentPreview(file) {
  filePreviewRef.value?.open(file.url, file.name)
}

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const { marketing_activity_type, marketing_activity_form, marketing_activity_status, marketing_participate_status } = proxy.useDict('marketing_activity_type', 'marketing_activity_form', 'marketing_activity_status', 'marketing_participate_status')

const activityId = route.params && route.params.activityId
const loading = ref(true)
const activeTab = ref('basic')
const activity = ref({})
const participants = ref([])
const statusLogs = ref([])
const reviewOpen = ref(false)
const reviewData = ref({})
const participantOpen = ref(false)
const contactOptions = ref([])

const participantForm = ref({})
const participantRules = {
  companyName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
  contactName: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
}
// P3-15: 报名推广
const promoteUrl = ref('')
const promoteLoading = ref(false)
const promoteQrRef = ref(null)

function statusLabel(val) {
  const map = { '0': '草稿', '1': '已发布', '2': '进行中', '3': '已结束', '4': '已归档', '5': '已取消' }
  return map[val] || val || '-'
}
function statusTagType(val) {
  const map = { '0': 'info', '1': 'primary', '2': 'success', '3': 'warning', '4': 'info', '5': 'danger' }
  return map[val] || 'info'
}

function getActivityData() {
  loading.value = true
  getActivity(activityId).then(res => {
    activity.value = res.data
    loading.value = false
  })
}

function getParticipants() {
  getParticipantsByActivity(activityId).then(res => { participants.value = res.data || [] })
}

function getLogs() {
  getActivityStatusLogs(activityId).then(res => { statusLogs.value = res.data || [] })
}

function getContactOptions() {
  listContact({ pageNum: 1, pageSize: 9999 }).then(res => { contactOptions.value = res.rows })
}

function goBack() { router.push('/mk/activity/list') }

// 参与人操作
function handleAddParticipant() {
  participantForm.value = { activityId: activityId, source: '0' }
  if (contactOptions.value.length === 0) getContactOptions()
  participantOpen.value = true
}
function onContactSelect(contactId) {
  if (contactId) {
    const c = contactOptions.value.find(i => i.contactId === contactId)
    if (c) {
      participantForm.value.companyName = c.customerName
      participantForm.value.contactName = c.name
      participantForm.value.contactPhone = c.phone
      participantForm.value.contactEmail = c.email
      participantForm.value.position = c.position
    }
  }
}
function submitParticipant() {
  proxy.$refs['participantRef'].validate(valid => {
    if (valid) {
      addParticipant(participantForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        participantOpen.value = false
        getParticipants()
      })
    }
  })
}
function handleSignIn(row) {
  proxy.$modal.confirm('确认签到？').then(() => signInParticipant(row.participantId)).then(() => {
    proxy.$modal.msgSuccess('签到成功')
    getParticipants()
  }).catch(() => {})
}
function handleConvertLead(row) {
  proxy.$modal.confirm('确认将该参与人转化为线索？').then(() => convertToLead(row.participantId)).then(() => {
    proxy.$modal.msgSuccess('转线索成功')
    getParticipants()
  }).catch(() => {})
}

// 复盘
function handleReview() {
  reviewActivity(activityId).then(res => {
    reviewData.value = res.data
    reviewOpen.value = true
  })
}
function saveReviewData() {
  const data = {
    activityId: activityId,
    actualCount: reviewData.value.signedCount,
    leadCount: reviewData.value.leadCount,
    mqlCount: reviewData.value.mqlCount,
    sqlCount: reviewData.value.sqlCount,
    opportunityCount: reviewData.value.opportunityCount,
    dealAmount: reviewData.value.dealAmount,
    actualRoi: reviewData.value.actualRoi,
    cost: reviewData.value.budget
  }
  saveReview(data).then(() => {
    proxy.$modal.msgSuccess('复盘保存成功')
    reviewOpen.value = false
    getActivityData()
  })
}

getActivityData()
getParticipants()
getLogs()
loadPromoteUrl()

// P3-15: 加载报名推广链接
async function loadPromoteUrl() {
  promoteLoading.value = true
  try {
    const res = await getRegisterUrl(activityId)
    promoteUrl.value = window.location.origin + res.url
    await nextTick()
    if (promoteQrRef.value) {
      await QRCode.toCanvas(promoteQrRef.value, promoteUrl.value, {
        width: 200, margin: 2, color: { dark: '#1a1a2e', light: '#ffffff' }
      })
    }
  } finally {
    promoteLoading.value = false
  }
}
function copyPromoteUrl() {
  navigator.clipboard.writeText(promoteUrl.value).then(() => {
    proxy.$modal.msgSuccess('链接已复制到剪贴板')
  }).catch(() => {
    const input = document.createElement('input')
    input.value = promoteUrl.value
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    proxy.$modal.msgSuccess('链接已复制到剪贴板')
  })
}
function downloadPromoteQr() {
  if (!promoteQrRef.value) return
  const link = document.createElement('a')
  link.download = '活动报名二维码_' + (activity.value.activityName || '') + '.png'
  link.href = promoteQrRef.value.toDataURL('image/png')
  link.click()
}
function openPromotePage() {
  window.open(promoteUrl.value, '_blank')
}
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.ml5 { margin-left: 5px; }
.mt16 { margin-top: 16px; }
.text-gray { color: #999; }
.detail-group {
  margin-bottom: 24px;
}
.detail-group :deep(.el-descriptions__title) {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
.detail-attachment-item {
  margin-bottom: 6px;
}
</style>
