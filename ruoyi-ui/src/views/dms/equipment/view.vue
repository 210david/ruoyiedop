<template>
  <el-dialog v-model="visible" width="780px" append-to-body draggable class="rd-dialog">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
        <span class="rd-detail-header-title">设备详情</span>
        <div class="rd-detail-header-sub" v-if="info.equipmentCode">
          <div class="rd-detail-header-divider"></div>
          <span class="rd-detail-header-no">编号：{{ info.equipmentCode }}</span>
        </div>
      </div>
    </template>
    <div v-loading="loading" class="rd-page">
      <!-- 基本信息 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('v4')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.v4">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">设备编号</span><div class="rd-value">{{ info.equipmentCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">设备名称</span><div class="rd-value">{{ info.equipmentName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">设备分类</span><div class="rd-value">{{ info.categoryName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">设备状态</span><div class="rd-value"><dict-tag :options="dms_equipment_status" :value="info.equipmentStatus" /></div></div>
            <div class="rd-item"><span class="rd-label">型号</span><div class="rd-value">{{ info.model || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">序列号</span><div class="rd-value">{{ info.serialNumber || '-' }}</div></div>
          </div>
        </div>
      </section>
      <!-- 资产信息 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('v3')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>资产信息</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.v3">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">制造商</span><div class="rd-value">{{ info.manufacturer || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ info.supplier || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">购置日期</span><div class="rd-value">{{ parseTime(info.purchaseDate, '{y}-{m}-{d}') || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">原值</span><div class="rd-value">{{ info.originalValue != null ? '¥' + info.originalValue : '-' }}</div></div>
          </div>
        </div>
      </section>
      <!-- 使用信息 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('v2')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg></span>使用信息</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.v2">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value">{{ info.responsibleName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">使用部门</span><div class="rd-value">{{ info.deptName || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">安装位置</span><div class="rd-value">{{ info.installLocation || '-' }}</div></div>
          </div>
        </div>
      </section>
      <!-- 图片与附件 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('v1')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></span>图片与附件</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.v1">
          <div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">设备图片</span><div class="rd-value"><image-preview v-if="info.equipmentImage" :src="info.equipmentImage" :width="100" :height="100" /><span v-else>-</span></div></div>
            <div class="rd-item rd-item--full">
              <span class="rd-label">附件资料</span>
              <div class="rd-value">
                <template v-if="info.attachmentUrl">
                  <div v-for="(file, idx) in info.attachmentUrl.split(',')" :key="idx" style="line-height: 28px; display: flex; align-items: center; gap: 4px;">
                    <el-link :underline="false" type="primary" @click="handlePreview(file)"><el-icon><View /></el-icon>预览</el-link>
                    <el-link :href="baseUrl + file" :underline="false" target="_blank" type="success" style="margin-left: 10px;"><el-icon><Download /></el-icon>下载</el-link>
                    <span style="margin-left: 10px; font-size: 13px; color: #606266;">{{ getFileName(file) }}</span>
                  </div>
                </template>
                <span v-else>-</span>
              </div>
            </div>
          </div>
        </div>
      </section>
      <!-- 其他信息 -->
      <section class="rd-card">
        <div class="rd-card-header" @click="toggleCard('v0')">
          <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
          <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
        </div>
        <div class="rd-card-body" v-show="!collapsedCards.v0">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">启用状态</span><div class="rd-value"><el-tag :type="info.status === '0' ? 'success' : 'danger'">{{ info.status === '0' ? '正常' : '停用' }}</el-tag></div></div>
            <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ info.createBy || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ parseTime(info.createTime) || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ info.remark || '-' }}</div></div>
          </div>
        </div>
      </section>
    </div>
    <!-- 文件预览弹窗 -->
    <file-preview ref="filePreviewRef" />
  </el-dialog>
</template>

<script setup name="DmsEquipmentViewDrawer">
import { getEquipment } from '@/api/dms/equipment'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['v4', 'v3', 'v2', 'v1', 'v0'])

const { proxy } = getCurrentInstance()
const { dms_equipment_status } = proxy.useDict('dms_equipment_status')

const visible = ref(false)
const loading = ref(false)
const info = reactive({})
const baseUrl = import.meta.env.VITE_APP_BASE_API

const open = async (equipmentId) => {
  visible.value = true
  loading.value = true
  try {
    const res = await getEquipment(equipmentId)
    Object.keys(info).forEach(k => { info[k] = undefined })
    Object.assign(info, res.data)
  } finally {
    loading.value = false
  }
}

function getFileName(url) {
  if (url.lastIndexOf('/') > -1) {
    return url.slice(url.lastIndexOf('/') + 1)
  }
  return url
}

/** 预览附件 */
function handlePreview(file) {
  proxy.$refs.filePreviewRef.open(file, getFileName(file))
}

defineExpose({ open })
</script>
