﻿﻿﻿<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="企业名称" prop="companyName"><el-input v-model="queryParams.companyName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="线索等级" prop="leadGrade">
        <el-select v-model="queryParams.leadGrade" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_lead_grade" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审批状态" prop="receiveStatus">
        <el-select v-model="queryParams.receiveStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_lead_receive_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="info" plain icon="Guide" @click="showHelp = !showHelp">公海池说明</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-alert v-if="showHelp" type="info" :closable="false" style="margin-bottom: 12px">
      <template #title>
        <div style="line-height: 1.8">
          <strong>公海池业务说明：</strong><br/>
          1. <strong>数据来源：</strong>新增线索时未指定负责人（默认 isPublic=1），或管理员手动将线索退回公海；<br/>
          2. <strong>领取流程：</strong>点击「申请领取」提交审批申请 → 管理员审批通过后线索自动分配给申请人；<br/>
          3. <strong>审批状态：</strong>未申请（可申请）、待审批（等待管理员处理）、已批准（已分配）、已拒绝（退回公海可重新申请）。
        </div>
      </template>
    </el-alert>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @header-dragend="onHeaderDragEnd">
      <el-table-column label="线索编号" prop="leadNo" :width="colWidth('leadNo', 150)" resizable />
      <el-table-column label="企业名称" prop="companyName" :width="colWidth('companyName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="联系人" prop="contactName" :width="colWidth('contactName', 100)" resizable />
      <el-table-column label="手机号" prop="contactPhone" :width="colWidth('contactPhone', 130)" resizable />
      <el-table-column label="线索来源" prop="leadSource" :width="colWidth('leadSource', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_customer_source" :value="scope.row.leadSource" /></template>
      </el-table-column>
      <el-table-column label="等级" prop="leadGrade" :width="colWidth('leadGrade', 80)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_lead_grade" :value="scope.row.leadGrade" /></template>
      </el-table-column>
      <el-table-column label="评分" prop="leadScore" :width="colWidth('leadScore', 80)" resizable align="center" />
      <el-table-column label="审批状态" prop="receiveStatus" :width="colWidth('receiveStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_lead_receive_status" :value="scope.row.receiveStatus || '0'" /></template>
      </el-table-column>
      <el-table-column label="申请人" prop="receiveApplyUserName" :width="colWidth('receiveApplyUserName', 100)" resizable />
      <el-table-column label="申请时间" prop="receiveApplyTime" :width="colWidth('receiveApplyTime', 160)" resizable />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="success" icon="Pointer" @click="handleReceive(scope.row)" v-hasPermi="['marketing:lead:receive']" v-if="!scope.row.receiveStatus || scope.row.receiveStatus === '0' || scope.row.receiveStatus === '3'">申请领取</el-button>
          <el-button link type="warning" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['marketing:lead:assign']" v-if="scope.row.receiveStatus === '1'">审批</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 线索详情对话框 -->
    <el-dialog v-model="viewOpen" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">线索详情</span>
        </div>
      </template>
      <el-collapse v-model="viewActiveNames">
        <el-collapse-item title="线索信息" name="lead">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">线索编号</span><div class="rd-value">{{ viewForm.leadNo }}</div></div>
            <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ viewForm.companyName }}</div></div>
            <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ viewForm.contactName }}</div></div>
            <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ viewForm.contactPhone }}</div></div>
            <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ viewForm.contactEmail }}</div></div>
            <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value">{{ viewForm.position }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="企业信息" name="company">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="viewForm.industry" /></div></div>
            <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="viewForm.companySize" /></div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value">{{ viewForm.address }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="需求与评分" name="requirement">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">线索来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="viewForm.leadSource" /></div></div>
            <div class="rd-item"><span class="rd-label">线索评分</span><div class="rd-value">{{ viewForm.leadScore }}</div></div>
            <div class="rd-item"><span class="rd-label">线索等级</span><div class="rd-value"><dict-tag :options="marketing_lead_grade" :value="viewForm.leadGrade" /></div></div>
            <div class="rd-item"><span class="rd-label">线索状态</span><div class="rd-value"><dict-tag :options="marketing_lead_status" :value="viewForm.leadStatus" /></div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">需求描述</span><div class="rd-value">{{ viewForm.requirementDesc }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="领取审批信息" name="receive">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">审批状态</span><div class="rd-value"><dict-tag :options="marketing_lead_receive_status" :value="viewForm.receiveStatus || '0'" /></div></div>
            <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ viewForm.receiveApplyUserName }}</div></div>
            <div class="rd-item"><span class="rd-label">申请时间</span><div class="rd-value">{{ viewForm.receiveApplyTime }}</div></div>
            <div class="rd-item"><span class="rd-label">审批备注</span><div class="rd-value">{{ viewForm.receiveRemark }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="其他信息" name="other">
          <div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewForm.remark }}</div></div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveOpen" width="750px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">领取审批</span>
        </div>
      </template>
      <el-collapse v-model="approveActiveNames" class="approve-collapse">
        <el-collapse-item title="线索基本信息" name="basic">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">线索编号</span><div class="rd-value">{{ approveForm.leadNo }}</div></div>
            <div class="rd-item"><span class="rd-label">线索来源</span><div class="rd-value"><dict-tag :options="marketing_customer_source" :value="approveForm.leadSource" /></div></div>
            <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ approveForm.companyName }}</div></div>
            <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ approveForm.contactName }}</div></div>
            <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ approveForm.contactPhone }}</div></div>
            <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ approveForm.contactEmail }}</div></div>
            <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value">{{ approveForm.position }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">详细地址</span><div class="rd-value">{{ approveForm.address }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="企业画像" name="company">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">所属行业</span><div class="rd-value"><dict-tag :options="marketing_industry" :value="approveForm.industry" /></div></div>
            <div class="rd-item"><span class="rd-label">企业规模</span><div class="rd-value"><dict-tag :options="marketing_company_size" :value="approveForm.companySize" /></div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="需求与评分" name="requirement">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">线索等级</span><div class="rd-value"><dict-tag :options="marketing_lead_grade" :value="approveForm.leadGrade" /></div></div>
            <div class="rd-item"><span class="rd-label">线索评分</span><div class="rd-value">{{ approveForm.leadScore }}</div></div>
            <div class="rd-item"><span class="rd-label">线索状态</span><div class="rd-value"><dict-tag :options="marketing_lead_status" :value="approveForm.leadStatus" /></div></div>
            <div class="rd-item"><span class="rd-label">最后跟进</span><div class="rd-value">{{ approveForm.lastFollowTime || '—' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">需求描述</span><div class="rd-value">{{ approveForm.requirementDesc }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="申请信息" name="apply">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">审批状态</span><div class="rd-value"><dict-tag :options="marketing_lead_receive_status" :value="approveForm.receiveStatus || '1'" /></div></div>
            <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ approveForm.receiveApplyUserName }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">申请时间</span><div class="rd-value">{{ approveForm.receiveApplyTime }}</div></div>
          </div>
        </el-collapse-item>
      </el-collapse>
      <el-form ref="approveRef" :model="approveForm" label-width="80px" style="margin-top: 16px">
        <el-form-item label="审批备注" prop="receiveRemark">
          <el-input v-model="approveForm.receiveRemark" type="textarea" :rows="3" placeholder="请输入审批备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="success" @click="confirmApprove">通过</el-button>
        <el-button type="danger" @click="confirmReject">拒绝</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkLeadPool">
import { useRouter } from 'vue-router'
import { listLead, getLead, receiveLead, approveReceive, rejectReceive } from '@/api/mk/lead'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])

const router = useRouter()
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_lead_pool')
const { marketing_lead_status, marketing_lead_grade, marketing_customer_source, marketing_industry, marketing_company_size, marketing_lead_receive_status } = proxy.useDict('marketing_lead_status', 'marketing_lead_grade', 'marketing_customer_source', 'marketing_industry', 'marketing_company_size', 'marketing_lead_receive_status')

const list = ref([])
const viewOpen = ref(false)
const approveOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showHelp = ref(false)
const total = ref(0)
const viewForm = ref({})
const approveForm = ref({})
const viewActiveNames = ref(['lead', 'company', 'requirement', 'receive', 'other'])
const approveActiveNames = ref(['basic', 'company', 'requirement', 'apply'])

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, companyName: undefined, leadGrade: undefined, receiveStatus: undefined, isPublic: '1' }
})
const { queryParams } = toRefs(data)

function getList() { loading.value = true; listLead(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.isPublic = '1'; handleQuery() }
function handleView(row) { router.push('/mk/lead-detail/' + row.leadId) }
function handleReceive(row) {
  proxy.$modal.confirm('确认申请领取线索"' + row.companyName + '"？提交后需等待管理员审批。').then(() => receiveLead(row.leadId, {})).then(() => { getList(); proxy.$modal.msgSuccess('申请已提交，等待审批') }).catch(() => {})
}
function handleApprove(row) {
  approveForm.value = {
    leadId: row.leadId,
    leadNo: row.leadNo,
    companyName: row.companyName,
    contactName: row.contactName,
    contactPhone: row.contactPhone,
    contactEmail: row.contactEmail,
    position: row.position,
    address: row.address,
    industry: row.industry,
    companySize: row.companySize,
    leadSource: row.leadSource,
    leadGrade: row.leadGrade,
    leadScore: row.leadScore,
    leadStatus: row.leadStatus,
    requirementDesc: row.requirementDesc,
    lastFollowTime: row.lastFollowTime,
    receiveStatus: row.receiveStatus,
    receiveApplyUserName: row.receiveApplyUserName,
    receiveApplyTime: row.receiveApplyTime,
    receiveRemark: undefined
  }
  approveOpen.value = true
}
function confirmApprove() {
  approveReceive(approveForm.value.leadId, { receiveRemark: approveForm.value.receiveRemark }).then(() => {
    proxy.$modal.msgSuccess('审批通过，线索已分配')
    approveOpen.value = false
    getList()
  })
}
function confirmReject() {
  rejectReceive(approveForm.value.leadId, { receiveRemark: approveForm.value.receiveRemark }).then(() => {
    proxy.$modal.msgSuccess('已拒绝领取申请')
    approveOpen.value = false
    getList()
  })
}
getList()
</script>

<style scoped>
.approve-collapse :deep(.el-collapse-item__header) {
  font-weight: 600;
  font-size: 14px;
}
</style>
