<template>
  <el-drawer title="设备详情" v-model="visible" direction="rtl" size="60%" append-to-body :before-close="handleClose" class="detail-drawer">
    <div v-loading="loading" class="drawer-content">
      <!-- 基本信息 -->
      <h4 class="section-header">基本信息</h4>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">设备编号：</label>
            <span class="info-value plaintext">{{ info.equipmentCode }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">设备名称：</label>
            <span class="info-value plaintext">{{ info.equipmentName }}</span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">设备分类：</label>
            <span class="info-value plaintext">{{ info.categoryName }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">设备状态：</label>
            <span class="info-value plaintext">
              <dict-tag :options="dms_equipment_status" :value="info.equipmentStatus" />
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">型号：</label>
            <span class="info-value plaintext">{{ info.model }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">序列号：</label>
            <span class="info-value plaintext">{{ info.serialNumber }}</span>
          </div>
        </el-col>
      </el-row>

      <!-- 资产信息 -->
      <h4 class="section-header">资产信息</h4>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">制造商：</label>
            <span class="info-value plaintext">{{ info.manufacturer }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">供应商：</label>
            <span class="info-value plaintext">{{ info.supplier }}</span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">购置日期：</label>
            <span class="info-value plaintext">{{ parseTime(info.purchaseDate, '{y}-{m}-{d}') }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">原值：</label>
            <span class="info-value plaintext">{{ info.originalValue ? '¥' + info.originalValue : '-' }}</span>
          </div>
        </el-col>
      </el-row>

      <!-- 使用信息 -->
      <h4 class="section-header">使用信息</h4>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">责任人：</label>
            <span class="info-value plaintext">{{ info.responsibleName }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">使用部门：</label>
            <span class="info-value plaintext">{{ info.deptName }}</span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="24">
          <div class="info-item">
            <label class="info-label">安装位置：</label>
            <span class="info-value plaintext">{{ info.installLocation }}</span>
          </div>
        </el-col>
      </el-row>

      <!-- 图片与附件 -->
      <h4 class="section-header">图片与附件</h4>
      <el-row :gutter="20" class="mb8">
        <el-col :span="24">
          <div class="info-item">
            <label class="info-label">设备图片：</label>
            <span class="info-value">
              <image-preview v-if="info.equipmentImage" :src="info.equipmentImage" :width="100" :height="100" />
              <span v-else class="info-value plaintext">暂无</span>
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="24">
          <div class="info-item">
            <label class="info-label">附件资料：</label>
            <span class="info-value">
              <template v-if="info.attachmentUrl">
                <div v-for="(file, idx) in info.attachmentUrl.split(',')" :key="idx" class="attachment-link">
                  <el-link :underline="false" type="primary" @click="handlePreview(file)">
                    <el-icon><View /></el-icon>
                    预览
                  </el-link>
                  <el-link :href="baseUrl + file" :underline="false" target="_blank" type="success" class="ml10">
                    <el-icon><Download /></el-icon>
                    下载
                  </el-link>
                  <span class="attachment-name">{{ getFileName(file) }}</span>
                </div>
              </template>
              <span v-else class="info-value plaintext">暂无</span>
            </span>
          </div>
        </el-col>
      </el-row>

      <!-- 其他信息 -->
      <h4 class="section-header">其他信息</h4>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">启用状态：</label>
            <span class="info-value plaintext">
              <el-tag size="small" :type="info.status === '0' ? 'success' : 'danger'">{{ info.status === '0' ? '正常' : '停用' }}</el-tag>
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="24">
          <div class="info-item">
            <label class="info-label">备注：</label>
            <span class="info-value plaintext">{{ info.remark || '-' }}</span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">创建人：</label>
            <span class="info-value plaintext">{{ info.createBy }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">创建时间：</label>
            <span class="info-value plaintext">{{ parseTime(info.createTime) }}</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 文件预览弹窗 -->
    <file-preview ref="filePreviewRef" />
  </el-drawer>
</template>

<script setup name="DmsEquipmentViewDrawer">
import { getEquipment } from '@/api/dms/equipment'

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
    Object.assign(info, res.data)
  } finally {
    loading.value = false
  }
}

function handleClose() {
  visible.value = false
  Object.keys(info).forEach(k => { info[k] = undefined })
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

<style scoped lang="scss">
.detail-drawer .drawer-content {
  padding: 0 20px 20px;
}
.section-header {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.section-header:first-child {
  margin-top: 0;
}
.info-item {
  display: flex;
  align-items: flex-start;
  padding: 6px 0;
}
.info-label {
  width: 90px;
  flex-shrink: 0;
  font-size: 13px;
  color: #909399;
  text-align: right;
  padding-right: 8px;
}
.info-value {
  flex: 1;
  font-size: 13px;
  color: #303133;
  word-break: break-all;
}
.info-value.plaintext {
  line-height: 24px;
}
.attachment-link {
  line-height: 28px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.attachment-link .ml10 {
  margin-left: 10px;
}
.attachment-name {
  margin-left: 10px;
  font-size: 13px;
  color: #606266;
}
</style>
