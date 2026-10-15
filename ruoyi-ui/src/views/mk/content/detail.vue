<template>
  <div class="app-container">
    <!-- 页面级标题横幅 -->
    <div class="detail-page-header" v-loading="loading">
      <div class="detail-page-header-back">
        <el-button @click="goBack" icon="ArrowLeft" circle />
      </div>
      <div class="detail-page-header-main">
        <div class="detail-page-header-title">{{ content.title || (isNew ? '新建内容' : '内容详情') }}</div>
        <div class="detail-page-header-sub" v-if="content.contentNo">
          <span class="detail-page-header-no">编号：{{ content.contentNo }}</span>
          <dict-tag v-if="content.auditStatus" :options="marketing_content_audit_status" :value="content.auditStatus" />
        </div>
        <div class="detail-page-header-sub" v-else>
          <span class="detail-page-header-placeholder">{{ isNew ? '填写内容信息，配置渠道内容包后保存' : '查看内容详细信息' }}</span>
        </div>
      </div>
      <div class="detail-page-header-actions">
        <template v-if="editable">
          <el-button type="primary" icon="Check" @click="handleSave" v-hasPermi="[isNew ? 'marketing:content:add' : 'marketing:content:edit']">保 存</el-button>
        </template>
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </div>

    <div class="rd-detail-page" v-loading="loading">
      <!-- ===== 基本信息 ===== -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('basic')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.basic">
          <el-form v-if="editable" ref="contentRef" :model="form" :rules="rules" label-width="100px">
            <el-row>
              <el-col :span="12"><el-form-item label="内容标题" prop="title"><el-input v-model="form.title" placeholder="请输入内容标题" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="内容类型" prop="contentType">
                <el-select v-model="form.contentType" placeholder="请选择内容类型" style="width: 100%">
                  <el-option v-for="d in marketing_content_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="关联活动" prop="activityId">
                <el-input v-model="form.activityName" readonly placeholder="点击选择关联活动（选填）" style="width: 100%" @click="openActivityPicker">
                  <template #append>
                    <el-button icon="Search" @click="openActivityPicker" />
                  </template>
                  <template #suffix>
                    <el-icon v-if="form.activityId" class="clear-icon" @click.stop="clearActivity"><CircleClose /></el-icon>
                  </template>
                </el-input>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="计划发布日期" prop="planPublishDate">
                <el-date-picker v-model="form.planPublishDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择计划发布日期" style="width: 100%" />
              </el-form-item></el-col>              <el-col :span="24"><el-form-item label="创作主题" prop="theme">
                <el-input v-model="form.theme" type="textarea" :rows="3" placeholder="创作主题/要点，作为 AI 生成各渠道文案的输入依据" />
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item></el-col>
            </el-row>
          </el-form>
          <div v-else class="rd-grid">
            <div class="rd-item"><span class="rd-label">内容编号</span><div class="rd-value col-mono">{{ content.contentNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">内容标题</span><div class="rd-value">{{ content.title || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">内容类型</span><div class="rd-value"><dict-tag :options="marketing_content_type" :value="content.contentType" /></div></div>
            <div class="rd-item"><span class="rd-label">关联活动</span><div class="rd-value" :class="{ 'rd-value--muted': !content.activityName }">{{ content.activityName || '暂无' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划发布日期</span><div class="rd-value">{{ content.planPublishDate ? String(content.planPublishDate).substring(0, 10) : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ content.userName || '—' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">创作主题</span><div class="rd-value" :class="{ 'rd-value--muted': !content.theme }">{{ content.theme || '暂无' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !content.remark }">{{ content.remark || '暂无' }}</div></div>
          </div>
        </div>
      </section>

      <!-- ===== 内容包（渠道文案） ===== -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('package')">
          <div class="rd-card-title">
            <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg></span>内容包（渠道文案）
            <span class="pkg-count" v-if="pkgs.length">{{ pkgs.length }}</span>
          </div>
          <div class="pkg-actions" @click.stop v-if="editable">
            <el-button v-if="aiModuleEnabled" size="small" icon="MagicStick" :loading="aiGenerating" @click="handleAiGenerate">AI 生成文案</el-button>
            <el-button size="small" type="primary" plain icon="Plus" @click="addPackage">添加内容包</el-button>
          </div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.package }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.package">
          <div v-if="pkgs.length === 0" class="pkg-empty">
            <svg class="pkg-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>
            <p class="pkg-empty-text">{{ editable ? '暂无内容包，点击"添加内容包"为各渠道定制文案，或使用 AI 一键生成' : '暂无内容包' }}</p>
          </div>
          <div v-for="(pkg, index) in pkgs" :key="index" class="package-block">
            <div class="package-block-head">
              <el-checkbox v-if="editable && aiModuleEnabled" v-model="pkg.aiChecked" size="small" title="勾选后参与 AI 生成">AI</el-checkbox>
              <span class="badge" :class="channelBadgeClass(pkg.channelType)"><span class="dot"></span>{{ channelLabel(pkg.channelType) }}</span>
              <span class="badge" :class="packageStatusBadgeClass(pkg.packageStatus)">{{ packageStatusLabel(pkg.packageStatus) }}</span>
              <span class="package-block-title">{{ pkg.packageTitle || '未命名内容包' }}</span>
              <el-button v-if="editable" link type="danger" icon="Delete" @click="removePackage(index)">删除</el-button>
            </div>
            <template v-if="editable">
              <el-form label-width="100px" @submit.prevent>
                <el-row>
                  <el-col :span="12"><el-form-item label="发布渠道" required>
                    <el-input v-model="pkg.channelName" readonly placeholder="点击选择渠道账号" style="width: 100%" @click="openChannelPicker(index)">
                      <template #append>
                        <el-button icon="Search" @click="openChannelPicker(index)" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="pkg.channelId" class="clear-icon" @click.stop="clearChannel(pkg)"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="包标题">
                    <el-input v-model="pkg.packageTitle" placeholder="该渠道发布用的标题" />
                  </el-form-item></el-col>
                  <el-col :span="24"><el-form-item label="正文">
                    <el-input v-model="pkg.packageBody" type="textarea" :rows="4" placeholder="该渠道发布用的正文文案" />
                  </el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="话题标签">
                    <el-input v-model="pkg.topicTags" placeholder="多个话题用英文逗号分隔" />
                  </el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="封面图">
                    <div class="cover-upload">
                      <el-upload :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false" accept="image/*" :on-success="res => handleCoverSuccess(res, pkg)">
                        <el-button icon="Upload">上传封面</el-button>
                      </el-upload>
                      <div class="cover-preview" v-if="pkg.coverUrl">
                        <el-image :src="coverSrc(pkg.coverUrl)" fit="cover" class="cover-img" :preview-src-list="[coverSrc(pkg.coverUrl)]" preview-teleported />
                        <el-link type="danger" :underline="false" @click="pkg.coverUrl = undefined">删除</el-link>
                      </div>
                    </div>
                  </el-form-item></el-col>
                  <el-col :span="24"><el-form-item label="素材引用">
                    <div class="material-ref">
                      <div class="material-ref-tags" v-if="pkg.selectedMaterials.length">
                        <el-tag v-for="(m, i) in pkg.selectedMaterials" :key="m.materialId" closable size="small" @close="removeMaterial(pkg, i)">{{ m.title }}</el-tag>
                      </div>
                      <el-button size="small" icon="Collection" @click="openMaterialPicker(index)">选择素材</el-button>
                    </div>
                  </el-form-item></el-col>
                </el-row>
              </el-form>
            </template>
            <div v-else class="rd-grid">
              <div class="rd-item"><span class="rd-label">发布渠道</span><div class="rd-value">{{ pkg.channelName || channelLabel(pkg.channelType) }}</div></div>
              <div class="rd-item"><span class="rd-label">包标题</span><div class="rd-value">{{ pkg.packageTitle || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">正文</span><div class="rd-value pkg-body">{{ pkg.packageBody || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">话题标签</span><div class="rd-value">{{ pkg.topicTags || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">封面图</span>
                <div class="rd-value">
                  <el-image v-if="pkg.coverUrl" :src="coverSrc(pkg.coverUrl)" fit="cover" class="cover-img" :preview-src-list="[coverSrc(pkg.coverUrl)]" preview-teleported />
                  <span v-else>—</span>
                </div>
              </div>
              <div class="rd-item rd-item--full"><span class="rd-label">素材引用</span><div class="rd-value" :class="{ 'rd-value--muted': !pkg.selectedMaterials.length }">{{ materialNames(pkg) }}</div></div>
            </div>
          </div>
        </div>
      </section>

      <!-- ===== 发布跟踪（审核通过后） ===== -->
      <section class="rd-card" v-if="content.auditStatus === '2'">
        <div class="rd-card-header" @click="toggleCard('publish')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></span>发布跟踪</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.publish }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.publish">
          <el-table :data="pkgs" border size="small">
            <el-table-column label="渠道" width="120" align="center">
              <template #default="scope"><span class="badge" :class="channelBadgeClass(scope.row.channelType)"><span class="dot"></span>{{ channelLabel(scope.row.channelType) }}</span></template>
            </el-table-column>
            <el-table-column label="包标题" prop="packageTitle" min-width="160" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.packageTitle || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope"><span class="badge" :class="packageStatusBadgeClass(scope.row.packageStatus)">{{ packageStatusLabel(scope.row.packageStatus) }}</span></template>
            </el-table-column>
            <el-table-column label="发布链接" min-width="200" show-overflow-tooltip>
              <template #default="scope">
                <el-link v-if="scope.row.publishUrl" type="primary" :underline="false" :href="scope.row.publishUrl" target="_blank">{{ scope.row.publishUrl }}</el-link>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column label="发布时间" prop="publishTime" width="170" align="center">
              <template #default="scope">{{ scope.row.publishTime || '—' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="170" align="center" fixed="right" class-name="col-action">
              <template #default="scope">
                <div class="action-btn-row">
                  <el-button link type="primary" icon="DocumentCopy" @click="copyText(scope.row.packageBody)" :disabled="!scope.row.packageBody">复制文案</el-button>
                  <el-button link type="primary" icon="Position" @click="openPublishDialog(scope.row)" v-hasPermi="['marketing:content:publish']">回填链接</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>

      <!-- ===== 审核记录（时间线，置于所有业务卡片之后） ===== -->
      <section class="rd-card" v-if="content.contentId && (content.auditLogList && content.auditLogList.length || content.auditStatus === '3')">
        <div class="rd-card-header" @click="toggleCard('auditLog')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.auditLog }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.auditLog" style="display:block">
          <el-alert v-if="editable && content.auditStatus === '3'" type="warning" :closable="false" show-icon class="reject-alert">
            <template #title>该内容已被驳回，请根据审核意见修改后重新提交</template>
          </el-alert>
          <div class="rd-timeline" v-if="content.auditLogList && content.auditLogList.length">
            <div class="rd-timeline-item" v-for="log in content.auditLogList" :key="log.logId">
              <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
              <div class="rd-timeline-content">
                <div class="rd-timeline-header">
                  <span class="rd-timeline-title">
                    <el-tag v-if="log.auditAction === '0'" size="small" effect="light" round>提交审核</el-tag>
                    <el-tag v-else-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                    <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                  </span>
                  <span class="rd-timeline-time">{{ log.auditTime }}</span>
                </div>
                <div class="rd-timeline-body">
                  <div class="rd-item"><span class="rd-label">操作人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                </div>
                <div class="rd-timeline-comment" v-if="log.auditRemark && log.auditAction !== '0'">
                  <strong>审核意见：</strong>{{ log.auditRemark }}
                </div>
              </div>
            </div>
          </div>
          <div class="rd-empty" v-else>
            <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
            <p class="rd-empty-text">暂无审核记录</p>
          </div>
        </div>
      </section>
    </div>

    <!-- 素材选择器 -->
    <mk-material-picker ref="materialPickerRef" multiple title="选择素材" @confirm="onMaterialConfirm" />
    <activity-picker ref="activityPickerRef" title="选择关联活动" @confirm="onActivityPickerConfirm" />
    <channel-picker ref="channelPickerRef" title="选择渠道账号" @confirm="onChannelConfirm" />

    <!-- 发布链接回填弹窗 -->
    <el-dialog v-model="publishOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></div>
          <span class="rd-detail-header-title">回填发布链接</span>
        </div>
      </template>
      <el-form label-width="90px" @submit.prevent>
        <el-form-item label="内容包">
          <span style="font-weight: 600">{{ publishForm.packageTitle }}</span>
        </el-form-item>
        <el-form-item label="发布链接" required>
          <el-input v-model="publishForm.publishUrl" placeholder="请输入该渠道发布后的链接（https://...）" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPublish">确 定</el-button>
        <el-button @click="publishOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkContentDetail">
import { useRoute, useRouter, onBeforeRouteUpdate } from 'vue-router'
import { getContent, addContent, updateContent, publishContent, aiGenerateContent } from '@/api/mk/content'
import { listMaterial } from '@/api/mk/material'
import { getToken } from '@/utils/auth'
import { CircleClose } from '@element-plus/icons-vue'
import useModuleStore from '@/store/modules/module'

/* 模块联动：AI中心停用时隐藏页面内AI功能按钮 */
const moduleStore = useModuleStore()
const aiModuleEnabled = computed(() => moduleStore.isEnabled('ai'))
import { useDetailCard } from '@/composables/useDetailCard'
import MkMaterialPicker from '@/components/MkMaterialPicker/index.vue'
import ActivityPicker from '@/components/ActivityPicker/index.vue'
import ChannelPicker from '@/components/ChannelPicker/index.vue'

const { collapsedCards, toggleCard } = useDetailCard(['basic', 'package', 'publish', 'auditLog'])

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const { marketing_content_type, marketing_content_audit_status, marketing_package_status, marketing_channel_type } = proxy.useDict('marketing_content_type', 'marketing_content_audit_status', 'marketing_package_status', 'marketing_channel_type')

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = { Authorization: 'Bearer ' + getToken() }

const loading = ref(true)
const aiGenerating = ref(false)
const content = ref({})
const form = ref({})
const pkgs = ref([])
const allMaterials = ref([])
const materialPickerRef = ref(null)
const activityPickerRef = ref(null)
const channelPickerRef = ref(null)
const currentPkgIndex = ref(-1)
const publishOpen = ref(false)
const publishForm = ref({ packageId: undefined, packageTitle: undefined, publishUrl: undefined })

const rules = {
  title: [{ required: true, message: '内容标题不能为空', trigger: 'blur' }],
  contentType: [{ required: true, message: '内容类型不能为空', trigger: 'change' }],
  planPublishDate: [{ required: true, message: '计划发布日期不能为空', trigger: 'change' }]
}

/** 当前路由内容ID（路由参数变化时动态取，兜底缓存复用场景） */
const currentRouteId = () => (route.params && route.params.contentId)
const contentId = computed(() => currentRouteId())
/** 新建模式（路由 0）或已存在内容 */
const isNew = computed(() => !currentRouteId() || String(currentRouteId()) === '0')
/** 仅草稿/已驳回可编辑（与后端提交审核的前置状态一致） */
const editable = computed(() => isNew.value || content.value.auditStatus === '0' || content.value.auditStatus === '3')

/** 加载内容详情（主档+内容包集合） */
function loadData() {
  if (isNew.value) { loading.value = false; resetForm(); return }
  loading.value = true
  getContent(contentId.value).then(res => {
    const data = res.data || {}
    content.value = data
    form.value = {
      contentId: data.contentId,
      title: data.title,
      contentType: data.contentType,
      theme: data.theme,
      activityId: data.activityId,
      activityName: data.activityName,
      planPublishDate: data.planPublishDate ? String(data.planPublishDate).substring(0, 10) : undefined,
      remark: data.remark
    }
    pkgs.value = (data.packageList || []).map(p => ({ ...p, aiChecked: true, selectedMaterials: [] }))
    hydrateSelectedMaterials()
    loading.value = false
  }).catch(() => { loading.value = false })
}

function resetForm() {
  content.value = {}
  form.value = { title: undefined, contentType: undefined, theme: undefined, activityId: undefined, activityName: undefined, planPublishDate: undefined, remark: undefined }
  pkgs.value = []
}

/** 回显素材引用：按 materialIds 匹配素材对象（用于选择器回显与展示） */
function hydrateSelectedMaterials() {
  const needHydrate = pkgs.value.some(p => p.materialIds && p.materialIds.trim())
  if (!needHydrate) return
  const match = () => {
    pkgs.value.forEach(p => {
      if (p.materialIds) {
        const ids = p.materialIds.split(',').map(s => s.trim()).filter(Boolean)
        p.selectedMaterials = ids.map(id => allMaterials.value.find(m => String(m.materialId) === String(id))).filter(Boolean)
      }
    })
  }
  if (allMaterials.value.length === 0) {
    listMaterial({ pageNum: 1, pageSize: 9999 }).then(res => {
      allMaterials.value = res.rows || []
      match()
    }).catch(() => {})
  } else {
    match()
  }
}

/** 内容包操作 */
function addPackage() {
  pkgs.value.push({ packageId: undefined, channelId: undefined, channelType: undefined, channelName: undefined, packageTitle: undefined, packageBody: undefined, topicTags: undefined, coverUrl: undefined, materialIds: undefined, packageStatus: '0', aiChecked: true, selectedMaterials: [] })
}
function removePackage(index) { pkgs.value.splice(index, 1) }
/** 选择渠道账号后带出渠道类型 */
function onChannelConfirm(ch) {
  if (currentPkgIndex.value < 0 || currentPkgIndex.value >= pkgs.value.length) return
  const pkg = pkgs.value[currentPkgIndex.value]
  pkg.channelId = ch.channelId
  pkg.channelName = ch.accountName
  pkg.channelType = ch.channelType
}
function clearChannel(pkg) {
  pkg.channelId = undefined
  pkg.channelName = undefined
  pkg.channelType = undefined
}
/** 关联活动选择器 */
function openActivityPicker() { activityPickerRef.value.open(form.value.activityId) }
function onActivityPickerConfirm(activity) {
  form.value.activityId = activity.activityId
  form.value.activityName = activity.activityName
}
function clearActivity() {
  form.value.activityId = undefined
  form.value.activityName = undefined
}
/** 封面上传成功：取 res.url（截取 /profile/ 相对路径存储，避免域名端口变更后失效） */
function handleCoverSuccess(res, pkg) {
  if (res.code !== 200) { proxy.$modal.msgError(res.msg || '上传失败'); return }
  let url = res.url || ''
  const idx = url.indexOf('/profile/')
  if (idx >= 0) url = url.substring(idx)
  pkg.coverUrl = url
  proxy.$modal.msgSuccess('封面上传成功')
}

/** 封面预览地址：/profile/ 资源需经 /common/file/access 携带 token 访问（img 无法带 Authorization 头） */
function coverSrc(url) {
  if (!url) return ''
  const idx = url.indexOf('/profile/')
  const resource = idx >= 0 ? url.substring(idx) : url
  if (!resource.startsWith('/profile/')) {
    return url.startsWith('http') ? url : import.meta.env.VITE_APP_BASE_API + url
  }
  return import.meta.env.VITE_APP_BASE_API + '/common/file/access?resource=' + encodeURIComponent(resource) + '&token=' + getToken()
}

/** 渠道账号选择器 */
function openChannelPicker(index) {
  currentPkgIndex.value = index
  channelPickerRef.value.open(pkgs.value[index].channelId)
}

/** 素材引用 */
function openMaterialPicker(index) {
  currentPkgIndex.value = index
  materialPickerRef.value.open(pkgs.value[index].selectedMaterials)
}
function onMaterialConfirm(rows) {
  if (currentPkgIndex.value >= 0 && currentPkgIndex.value < pkgs.value.length) {
    pkgs.value[currentPkgIndex.value].selectedMaterials = rows
  }
}
function removeMaterial(pkg, idx) { pkg.selectedMaterials.splice(idx, 1) }
function materialNames(pkg) {
  if (!pkg.selectedMaterials || !pkg.selectedMaterials.length) return '暂无'
  return pkg.selectedMaterials.map(m => m.title).join('、')
}

/** AI 生成各渠道文案草稿：按勾选渠道调用，结果按 channelType 回填对应包（仅前端填充，不自动保存） */
function handleAiGenerate() {
  const checked = pkgs.value.filter(p => p.aiChecked && p.channelType)
  if (checked.length === 0) { proxy.$modal.msgWarning('请先添加内容包并选择渠道，勾选后再使用 AI 生成'); return }
  if (!form.value.theme && !form.value.title) { proxy.$modal.msgWarning('请先填写创作主题或标题，作为 AI 生成依据'); return }

  const doGenerate = () => {
    aiGenerating.value = true
    aiGenerateContent({ contentId: content.value.contentId, channelTypes: checked.map(p => p.channelType) }).then(res => {
      const list = res.data || []
      let filled = 0
      list.forEach(item => {
        const pkg = checked.find(p => p.channelType === item.channelType)
        if (pkg) {
          pkg.packageTitle = item.title || pkg.packageTitle
          pkg.packageBody = item.body || pkg.packageBody
          pkg.topicTags = item.tags || pkg.topicTags
          filled++
        }
      })
      proxy.$modal.msgSuccess(`已生成 ${filled} 个渠道文案草稿，请检查调整后保存`)
    }).finally(() => { aiGenerating.value = false })
  }

  // 新增未保存时：先静默保存主档拿到 contentId，再调 AI 生成（用户无感知）
  if (!content.value.contentId) {
    proxy.$refs['contentRef'].validate(valid => {
      if (!valid) return
      const payload = {
        ...form.value,
        packageList: pkgs.value.map(p => ({
          packageId: p.packageId,
          contentId: p.contentId,
          channelId: p.channelId,
          channelType: p.channelType,
          packageTitle: p.packageTitle,
          packageBody: p.packageBody,
          topicTags: p.topicTags,
          coverUrl: p.coverUrl,
          materialIds: p.selectedMaterials.map(m => m.materialId).join(','),
          packageStatus: p.packageStatus,
          publishUrl: p.publishUrl,
          publishTime: p.publishTime
        }))
      }
      aiGenerating.value = true
      addContent(payload).then(res => {
        // 后端返回新记录ID，回填后同步路由参数，页面从"新增"变为"编辑"
        const newId = res.data != null ? res.data : (res.contentId != null ? res.contentId : null)
        if (newId != null) {
          content.value.contentId = newId
          form.value.contentId = newId
          window.history.replaceState(null, '', '/mk/content-detail/' + newId)
        }
        doGenerate()
      }).catch(() => { aiGenerating.value = false })
    })
    return
  }
  doGenerate()
}

/** 保存：主档+packageList 整体提交（后端级联保存） */
function handleSave() {
  proxy.$refs['contentRef'].validate(valid => {
    if (!valid) return
    const pending = pkgs.value.filter(p => !p.channelId)
    if (pending.length > 0) { proxy.$modal.msgError(`有 ${pending.length} 个内容包未选择渠道，请选择或删除后再保存`); return }
    const channelIds = pkgs.value.map(p => p.channelId)
    if (new Set(channelIds).size !== channelIds.length) { proxy.$modal.msgError('存在重复渠道的内容包，同一渠道只能有一个内容包'); return }
    const payload = {
      ...form.value,
      packageList: pkgs.value.map(p => ({
        packageId: p.packageId,
        contentId: p.contentId,
        channelId: p.channelId,
        channelType: p.channelType,
        packageTitle: p.packageTitle,
        packageBody: p.packageBody,
        topicTags: p.topicTags,
        coverUrl: p.coverUrl,
        materialIds: p.selectedMaterials.map(m => m.materialId).join(','),
        packageStatus: p.packageStatus,
        publishUrl: p.publishUrl,
        publishTime: p.publishTime
      }))
    }
    if (payload.contentId != null) {
      updateContent(payload).then(() => { proxy.$modal.msgSuccess('保存成功'); loadData() })
    } else {
      addContent(payload).then(() => { proxy.$modal.msgSuccess('新增成功'); goBack() })
    }
  })
}

/** 发布跟踪：复制正文到剪贴板 */
function copyText(text) {
  if (!text) return
  const done = () => proxy.$modal.msgSuccess('文案已复制到剪贴板')
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(done).catch(() => fallbackCopy(text, done))
  } else {
    fallbackCopy(text, done)
  }
}
function fallbackCopy(text, done) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  document.execCommand('copy')
  document.body.removeChild(textarea)
  done()
}

/** 发布跟踪：回填发布链接 */
function openPublishDialog(pkg) {
  publishForm.value = { packageId: pkg.packageId, packageTitle: pkg.channelName ? `${pkg.channelName} - ${pkg.packageTitle || '未命名'}` : (pkg.packageTitle || '未命名'), publishUrl: pkg.publishUrl }
  publishOpen.value = true
}
function submitPublish() {
  if (!publishForm.value.publishUrl || !publishForm.value.publishUrl.trim()) { proxy.$modal.msgError('请输入发布链接'); return }
  publishContent(publishForm.value.packageId, { publishUrl: publishForm.value.publishUrl.trim() }).then(() => {
    proxy.$modal.msgSuccess('发布链接已回填')
    publishOpen.value = false
    loadData()
  })
}

/** 字典标签与徽章颜色 */
function channelLabel(type) { const item = marketing_channel_type.value.find(d => d.value == type); return item ? item.label : '未选渠道' }
function channelBadgeClass(type) {
  const item = marketing_channel_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}
function packageStatusLabel(status) { const item = marketing_package_status.value.find(d => d.value == status); return item ? item.label : '待发布' }
function packageStatusBadgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'gray' }; return map[status] || 'amber' }

function goBack() { router.push('/mk/promotion/content') }

// 兜底：keep-alive 缓存下路由参数变化时重新加载数据，避免旧状态残留导致只读
onBeforeRouteUpdate((to, from) => {
  if (to.path !== from.path) { loadData(); window.scrollTo(0, 0) }
})

loadData()
</script>

<style scoped>
/* 独立详情页：rd-page 容器取消 max-width 限制 */
.rd-detail-page {
  max-width: none;
}

/* 页面级标题横幅 */
.detail-page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%);
  border-radius: 12px;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}
.detail-page-header::before {
  content: '';
  position: absolute;
  top: -30px;
  right: -15px;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%);
  pointer-events: none;
}
.detail-page-header-back {
  flex-shrink: 0;
}
.detail-page-header-main {
  flex: 1;
  min-width: 0;
}
.detail-page-header-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1.3;
}
.detail-page-header-sub {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
  flex-wrap: wrap;
}
.detail-page-header-no {
  font-size: 13px;
  font-weight: 500;
  color: rgb(255 255 255 / 0.8);
  font-variant-numeric: tabular-nums;
}
.detail-page-header-placeholder {
  font-size: 13px;
  color: rgb(255 255 255 / 0.6);
}
.detail-page-header-actions {
  flex-shrink: 0;
  padding-right: 8px;
}

/* 内容包卡片头右侧操作 */
.pkg-actions {
  margin-left: auto;
  margin-right: 12px;
  display: inline-flex;
  gap: 8px;
}
.pkg-count {
  margin-left: 8px;
  padding: 1px 8px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
}

/* 内容包区块 */
.package-block {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px 16px 4px;
  margin-bottom: 12px;
  background: #f8fafc;
}
.package-block-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.package-block-title {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
  margin-right: auto;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 360px;
}
.pkg-empty {
  text-align: center;
  padding: 32px 0;
}
.pkg-empty-icon {
  color: #cbd5e1;
}
.pkg-empty-text {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 13px;
}

/* 封面上传 */
.cover-upload {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}
.cover-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
/* 驳回提示（规范 3.6） */
.reject-alert {
  margin-bottom: 16px;
}
.cover-img {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

/* 选择器输入框清除图标 */
.clear-icon {
  color: #c0c4cc;
  cursor: pointer;
}
.clear-icon:hover {
  color: #909399;
}

/* 素材引用 */
.material-ref {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.material-ref-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 只读正文保留换行 */
.pkg-body {
  white-space: pre-wrap;
  word-break: break-word;
}

/* 徽章（详情页脱离 mk-list-page 容器，组件内自含样式） */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  border: 1px solid transparent;
}
.badge.blue { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
.badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; }
.badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.badge.gray { background: #f1f5f9; color: #64748b; border-color: #e2e8f0; }
.badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 5px;
  background: currentColor;
}

/* 等宽字体 */
.col-mono {
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.01em;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
