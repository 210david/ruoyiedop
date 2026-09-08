<template>
  <el-dialog :title="title" v-model="visible" width="720px" append-to-body @close="handleClose" class="excel-import-dialog">
    <!-- 顶层页签：上传 / 导入结果 -->
    <el-tabs v-model="topTab" class="import-tabs">
      <!-- ===== 页签1：文件上传 ===== -->
      <el-tab-pane name="upload">
        <template #label>
          <span class="tab-label" :class="{ 'tab-label--active': topTab === 'upload' }">
            <el-icon><Upload /></el-icon>
            文件上传
          </span>
        </template>

        <div class="tab-pane-content">
          <!-- 导入提示说明 -->
          <div v-if="tips && tips.length" class="tips-card">
            <div class="tips-card-header">
              <el-icon class="tips-icon"><InfoFilled /></el-icon>
              <span class="tips-title">导入说明</span>
            </div>
            <div class="tips-list">
              <div v-for="(tip, index) in tips" :key="index" class="tip-row">
                <span class="tip-num">{{ index + 1 }}</span>
                <span class="tip-text">{{ tip }}</span>
              </div>
            </div>
          </div>

          <!-- 上传区域 -->
          <div class="upload-section">
            <el-upload
              ref="uploadRef"
              :limit="1"
              accept=".xlsx, .xls"
              :headers="uploadHeaders"
              :action="uploadUrl"
              :disabled="isUploading"
              :on-progress="handleProgress"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :on-success="handleSuccess"
              :on-error="handleError"
              :on-exceed="handleExceed"
              :auto-upload="false"
              drag
              class="upload-dragger"
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
            </el-upload>

            <!-- 上传区域下方操作栏 -->
            <div class="upload-options-bar">
              <div class="options-left">
                <el-checkbox v-model="updateSupport" class="update-checkbox">{{ updateSupportLabel }}</el-checkbox>
                <div v-if="updateSupport && updateKeyOptions && updateKeyOptions.length" class="update-key-selector">
                  <span class="update-key-label">匹配字段</span>
                  <el-radio-group v-model="updateKey" size="small">
                    <el-radio v-for="opt in updateKeyOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="options-right">
                <el-link v-if="templateUrl" type="primary" underline="never" class="template-link" @click="handleDownloadTemplate">
                  <el-icon><Download /></el-icon> 下载导入模板
                </el-link>
              </div>
            </div>
            <div class="upload-hint">仅允许导入 xls、xlsx 格式文件</div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ===== 页签2：导入结果 ===== -->
      <el-tab-pane v-if="importResult" name="result">
        <template #label>
          <span class="tab-label" :class="{ 'tab-label--active': topTab === 'result' }">
            <el-icon><Document /></el-icon>
            导入结果
            <el-badge v-if="importResult.failureNum > 0" :value="importResult.failureNum" class="tab-badge" type="danger" />
          </span>
        </template>

        <div class="tab-pane-content">
          <!-- 结果概要卡片 -->
          <div class="result-card" :class="resultCardClass">
            <div class="result-card-left">
              <div class="result-icon-wrap" :class="resultCardClass">
                <el-icon v-if="importResult.failureNum > 0 && importResult.successNum === 0"><CircleCloseFilled /></el-icon>
                <el-icon v-else-if="importResult.failureNum > 0"><WarningFilled /></el-icon>
                <el-icon v-else><CircleCheckFilled /></el-icon>
              </div>
              <div class="result-info">
                <div class="result-title">{{ resultHeader }}</div>
                <div class="result-subtitle">{{ importResult.msg }}</div>
              </div>
            </div>
            <div class="result-card-right">
              <div v-if="importResult.successNum > 0" class="result-stat stat-success">
                <span class="stat-num">{{ importResult.successNum }}</span>
                <span class="stat-label">成功</span>
              </div>
              <div v-if="importResult.failureNum > 0" class="result-stat stat-failure">
                <span class="stat-num">{{ importResult.failureNum }}</span>
                <span class="stat-label">失败</span>
              </div>
            </div>
          </div>

          <!-- 有结构化明细时用子页签展示 -->
          <el-tabs
            v-if="importResult.successList || importResult.failureList"
            v-model="activeSubTab"
            class="result-sub-tabs"
            type="card"
          >
            <!-- 成功数据子页签 -->
            <el-tab-pane
              v-if="importResult.successList && importResult.successList.length > 0"
              name="success"
            >
              <template #label>
                <span class="sub-tab-label sub-tab-label--success">
                  <el-icon><CircleCheck /></el-icon>
                  成功数据（{{ importResult.successList.length }}）
                </span>
              </template>
              <div class="tab-table-wrap">
                <el-table :data="successPageData" size="small" border stripe max-height="360" style="width: 100%">
                  <el-table-column v-for="col in successColumns" :key="col.key" :prop="col.key" :label="col.label" :min-width="col.width || 100" show-overflow-tooltip />
                </el-table>
              </div>
              <div class="table-pagination">
                <el-pagination
                  v-model:current-page="successPage.current"
                  v-model:page-size="successPage.size"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="importResult.successList.length"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                  small
                />
              </div>
            </el-tab-pane>

            <!-- 失败数据子页签 -->
            <el-tab-pane
              v-if="importResult.failureList && importResult.failureList.length > 0"
              name="failure"
            >
              <template #label>
                <span class="sub-tab-label sub-tab-label--danger">
                  <el-icon><CircleClose /></el-icon>
                  失败数据（{{ importResult.failureList.length }}）
                </span>
              </template>
              <div class="failure-toolbar">
                <el-button type="danger" plain icon="Download" size="small" @click="exportFailureData">导出失败数据</el-button>
                <span class="failure-hint">下载失败数据 Excel，修正后重新导入</span>
              </div>
              <div class="tab-table-wrap">
                <el-table :data="failurePageData" size="small" border stripe max-height="320" style="width: 100%">
                  <el-table-column v-for="col in failureColumns" :key="col.key" :prop="col.key" :label="col.label" :min-width="col.width || 100" show-overflow-tooltip>
                    <template v-if="col.key === 'errorReason'" #default="{ row }">
                      <span class="error-reason-cell">{{ row[col.key] }}</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div class="table-pagination">
                <el-pagination
                  v-model:current-page="failurePage.current"
                  v-model:page-size="failurePage.size"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="importResult.failureList.length"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                  small
                />
              </div>
            </el-tab-pane>

            <!-- 无结构化明细时回退到HTML展示 -->
            <el-tab-pane
              v-if="(!importResult.successList || importResult.successList.length === 0) && (!importResult.failureList || importResult.failureList.length === 0)"
              label="详情" name="detail"
            >
              <div class="result-detail" v-html="importResult.msg"></div>
            </el-tab-pane>
          </el-tabs>

          <!-- 无子页签时的回退展示 -->
          <div v-if="!importResult.successList && !importResult.failureList" class="result-detail" v-html="importResult.msg"></div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="importResult && topTab === 'result'" @click="handleReset">重新导入</el-button>
        <el-button v-if="importResult && topTab === 'result'" @click="topTab = 'upload'">返回上传</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="isUploading" v-if="topTab === 'upload'">
          {{ isUploading ? '导入中...' : '确 定' }}
        </el-button>
        <el-button @click="visible = false">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { getToken } from '@/utils/auth'
import * as XLSX from 'xlsx'
import {
  UploadFilled, Download, InfoFilled, Upload, Document,
  CircleCheckFilled, CircleCloseFilled, WarningFilled,
  CircleCheck, CircleClose
} from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const props = defineProps({
  title: { type: String, default: '数据导入' },
  width: { type: String, default: '400px' },
  action: { type: String, required: true },
  templateAction: { type: String, default: '' },
  templateFileName: { type: String, default: 'template' },
  updateSupportLabel: { type: String, default: '是否更新已经存在的数据' },
  tips: { type: Array, default: () => [] },
  updateKeyOptions: { type: Array, default: () => [] },
  defaultUpdateKey: { type: String, default: '' },
  columnLabels: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['success'])

const uploadRef = ref(null)
const visible = ref(false)
const selectedFile = ref(null)
const isUploading = ref(false)
const updateSupport = ref(false)
const updateKey = ref(props.defaultUpdateKey)
const importResult = ref(null)
const topTab = ref('upload')
const activeSubTab = ref('success')
const successPage = reactive({ current: 1, size: 10 })
const failurePage = reactive({ current: 1, size: 10 })
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + getToken() }))

// 前端分页：成功数据当前页
const successPageData = computed(() => {
  if (!importResult.value?.successList?.length) return []
  const list = importResult.value.successList
  const start = (successPage.current - 1) * successPage.size
  return list.slice(start, start + successPage.size)
})

// 前端分页：失败数据当前页
const failurePageData = computed(() => {
  if (!importResult.value?.failureList?.length) return []
  const list = importResult.value.failureList
  const start = (failurePage.current - 1) * failurePage.size
  return list.slice(start, start + failurePage.size)
})

const uploadUrl = computed(() => {
  let url = import.meta.env.VITE_APP_BASE_API + props.action + '?updateSupport=' + (updateSupport.value ? 1 : 0)
  if (updateSupport.value && updateKey.value) {
    url += '&updateKey=' + updateKey.value
  }
  return url
})

const templateUrl = computed(() => !!props.templateAction)

const resultHeader = computed(() => {
  if (!importResult.value) return ''
  if (importResult.value.failureNum > 0 && importResult.value.successNum > 0) return '部分导入成功'
  if (importResult.value.failureNum > 0) return '导入失败'
  return '导入成功'
})

const resultCardClass = computed(() => {
  if (!importResult.value) return ''
  if (importResult.value.failureNum > 0 && importResult.value.successNum === 0) return 'is-error'
  if (importResult.value.failureNum > 0) return 'is-warning'
  return 'is-success'
})

// 默认列标签映射（可被 props.columnLabels 覆盖）
const defaultLabels = {
  // 考勤
  employeeNo: '工号', employeeName: '姓名', recordDate: '考勤日期', clockIn: '上班打卡', clockOut: '下班打卡',
  clockTime: '打卡时间', machineNo: '考勤机号',
  // 设备
  equipmentCode: '设备编号', equipmentName: '设备名称', model: '型号', assetCode: '资产编号',
  serialNumber: '序列号', manufacturer: '制造商',
  // 供应商
  supplierCode: '供应商编号', supplierName: '供应商名称', unifiedCreditCode: '统一信用代码',
  supplierType: '供应商类型', contactPerson: '联系人', contactPhone: '联系电话',
  // 物料
  materialCode: '物料编码', materialName: '物料名称', specModel: '规格型号', unit: '单位',
  materialType: '物料类型',
  // 客户
  customerNo: '客户编号', customerName: '企业名称', customerLevel: '客户等级',
  contactName: '联系人', phone: '电话',
  // 员工
  deptName: '部门', position: '职位', idCard: '身份证号', gender: '性别',
  orgName: '部门', positionName: '职位',
  // BOM
  bomName: 'BOM名称', productCode: '产品编码', productName: '产品名称', version: '版本号',
  baseQty: '基础数量', baseUnit: '基础单位',
  // 参训人员
  userName: '账号', nickName: '昵称',
  // 供货清单
  supplierNameLabel: '供应商', materialCodeLabel: '物料编码',
  price: '单价', currency: '币种', leadTime: '交货周期',
  // 盘点
  locationCode: '库位编码', batchNo: '批次号', bookQty: '账面数量', actualQty: '实盘数量',
  diffQty: '差异数量', diffReason: '差异原因',
  // 通用
  result: '结果', errorReason: '失败原因'
}

const successColumns = computed(() => {
  if (!importResult.value?.successList?.length) return []
  const labels = { ...defaultLabels, ...props.columnLabels }
  const keys = Object.keys(importResult.value.successList[0])
  return keys.map(k => ({ key: k, label: labels[k] || k, width: k === 'errorReason' ? 260 : k === 'equipmentName' || k === 'employeeName' ? 100 : 130 }))
})

const failureColumns = computed(() => {
  if (!importResult.value?.failureList?.length) return []
  const labels = { ...defaultLabels, ...props.columnLabels }
  const keys = Object.keys(importResult.value.failureList[0])
  return keys.map(k => ({ key: k, label: labels[k] || k, width: k === 'errorReason' ? 260 : k === 'equipmentName' || k === 'employeeName' ? 100 : 130 }))
})

function open() {
  updateSupport.value = false
  updateKey.value = props.defaultUpdateKey
  isUploading.value = false
  importResult.value = null
  topTab.value = 'upload'
  successPage.current = 1
  failurePage.current = 1
  visible.value = true
  nextTick(() => {
    selectedFile.value = null
    uploadRef.value?.clearFiles()
    clearInputValue()
  })
}

function handleReset() {
  importResult.value = null
  isUploading.value = false
  selectedFile.value = null
  topTab.value = 'upload'
  successPage.current = 1
  failurePage.current = 1
  uploadRef.value?.clearFiles()
  clearInputValue()
}

function handleClose() {
  isUploading.value = false
  selectedFile.value = null
  importResult.value = null
  topTab.value = 'upload'
  successPage.current = 1
  failurePage.current = 1
  uploadRef.value?.clearFiles()
  clearInputValue()
}

function handleDownloadTemplate() {
  proxy.download(props.templateAction, {}, `${props.templateFileName}_${new Date().getTime()}.xlsx`)
}

function handleProgress() { isUploading.value = true }

const handleFileChange = (file, fileList) => {
  if (file.status === 'ready') {
    selectedFile.value = file
    importResult.value = null
    clearInputValue()
  }
}

function handleExceed(files) {
  uploadRef.value?.clearFiles()
  const file = files[0]
  if (file) {
    uploadRef.value?.handleStart(file)
    selectedFile.value = { name: file.name, size: file.size, raw: file }
    importResult.value = null
  }
  clearInputValue()
}

function clearInputValue() {
  nextTick(() => {
    const input = uploadRef.value?.$el?.querySelector('input[type="file"]')
    if (input) input.value = ''
  })
}

const handleFileRemove = (file, fileList) => { selectedFile.value = null }

function handleSuccess(response) {
  isUploading.value = false
  const msg = response.msg || ''
  const successNum = response.successNum || 0
  const failureNum = response.failureNum || 0
  const successList = response.successList || null
  const failureList = response.failureList || null

  if (successNum > 0 || failureNum > 0) {
    importResult.value = { successNum, failureNum, msg, successList, failureList }
    if (failureNum > 0) {
      activeSubTab.value = 'failure'
    } else {
      activeSubTab.value = 'success'
    }
    if (successNum > 0) emit('success')
    if (failureNum === 0 && successNum > 0) {
      proxy.$modal.msgSuccess('导入成功')
    }
    // 重置分页
    successPage.current = 1
    failurePage.current = 1
    // 导入完成后自动切换到结果页签
    topTab.value = 'result'
  } else {
    importResult.value = {
      successNum: 0, failureNum: 1,
      msg: msg || '导入失败，请检查文件格式或网络后重试'
    }
    topTab.value = 'result'
  }
}

function handleError(error) {
  isUploading.value = false
  let errMsg = '导入失败，请检查文件格式或网络后重试'
  try {
    const response = JSON.parse(error.message)
    if (response.msg) errMsg = response.msg
  } catch (e) {
    if (error.message) errMsg = error.message
  }
  importResult.value = { successNum: 0, failureNum: 1, msg: errMsg }
  failurePage.current = 1
  topTab.value = 'result'
}

function handleSubmit() {
  const file = selectedFile.value
  if (!file || file.length === 0 || !file.name.toLowerCase().endsWith('.xls') && !file.name.toLowerCase().endsWith('.xlsx')) {
    proxy.$modal.msgError('请选择后缀为 xls 或 xlsx 的文件')
    return
  }
  if (updateSupport.value && props.updateKeyOptions && props.updateKeyOptions.length && !updateKey.value) {
    proxy.$modal.msgWarning('请选择更新匹配字段')
    return
  }
  uploadRef.value.submit()
}

function exportFailureData() {
  if (!importResult.value?.failureList?.length) return
  const data = importResult.value.failureList
  const cols = failureColumns.value
  const header = cols.map(c => c.label)
  const keys = cols.map(c => c.key)
  const rows = data.map(row => keys.map(k => row[k] ?? ''))
  const aoa = [header, ...rows]
  const ws = XLSX.utils.aoa_to_sheet(aoa)
  ws['!cols'] = cols.map(c => ({ wch: Math.max((c.width || 100) / 8, 10) }))
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '失败数据')
  XLSX.writeFile(wb, `导入失败数据_${new Date().getTime()}.xlsx`)
}

defineExpose({ open })
</script>

<style scoped>
/* ===== 整体弹窗 ===== */
.excel-import-dialog :deep(.el-dialog__body) {
  padding: 0 24px 8px;
}

/* ===== 顶层页签 ===== */
.import-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  border-bottom: 1px solid #e2e8f0;
}
.import-tabs :deep(.el-tabs__nav) {
  border: none !important;
}
.import-tabs :deep(.el-tabs__item) {
  border: none !important;
  padding: 0 20px !important;
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  border-bottom: 2px solid transparent !important;
  transition: all 0.2s;
}
.import-tabs :deep(.el-tabs__item.is-active) {
  border-bottom-color: var(--el-color-primary) !important;
  font-weight: 600;
}
.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 14px;
}
.tab-label--active {
  color: var(--el-color-primary);
}
.tab-badge {
  margin-left: 4px;
}
.tab-badge :deep(.el-badge__content) {
  font-size: 11px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
}

/* 页签内容区域 */
.tab-pane-content {
  padding-top: 16px;
}

/* ===== 导入提示卡片 ===== */
.tips-card {
  background: #f0f7ff;
  border: 1px solid #d0e3fb;
  border-radius: 8px;
  padding: 12px 16px 14px;
  margin-bottom: 16px;
}
.tips-card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.tips-icon {
  font-size: 16px;
  color: var(--el-color-primary);
}
.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d4ed8;
}
.tips-list {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.tip-row {
  display: flex;
  align-items: flex-start;
  line-height: 1.7;
}
.tip-num {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--el-color-primary);
  color: #fff;
  font-size: 11px;
  text-align: center;
  line-height: 18px;
  margin-right: 8px;
  margin-top: 2px;
}
.tip-text {
  flex: 1;
  font-size: 13px;
  color: #475569;
}

/* ===== 上传区域 ===== */
.upload-section {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
  background: #fafbfc;
}
.upload-dragger :deep(.el-upload-dragger) {
  padding: 24px 16px;
  border-radius: 8px;
  border: 2px dashed #cbd5e1;
  background: #fff;
  transition: border-color 0.2s, background 0.2s;
}
.upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: var(--el-color-primary);
  background: #f8faff;
}
.upload-dragger :deep(.el-upload-dragger .el-upload__text) {
  font-size: 14px;
  color: #64748b;
  margin-top: 6px;
}
.upload-dragger :deep(.el-upload-dragger .el-upload__text em) {
  color: var(--el-color-primary);
  font-style: normal;
}
.upload-icon {
  font-size: 40px;
  color: #94a3b8;
}

/* ===== 上传操作栏 ===== */
.upload-options-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  flex-wrap: wrap;
  gap: 8px;
}
.options-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.update-checkbox {
  font-size: 13px;
}
.update-key-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #f1f5f9;
  border-radius: 6px;
}
.update-key-label {
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
}
.template-link {
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.upload-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}

/* ===== 结果概要卡片 ===== */
.result-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-radius: 10px;
  margin-bottom: 14px;
  border: 1px solid;
  transition: all 0.3s;
}
.result-card.is-success {
  background: linear-gradient(135deg, #f0fdf4, #ecfdf5);
  border-color: #bbf7d0;
}
.result-card.is-warning {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border-color: #fde68a;
}
.result-card.is-error {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  border-color: #fecaca;
}
.result-card-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.result-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.result-icon-wrap.is-success {
  background: #dcfce7;
  color: #16a34a;
}
.result-icon-wrap.is-warning {
  background: #fef3c7;
  color: #d97706;
}
.result-icon-wrap.is-error {
  background: #fee2e2;
  color: #dc2626;
}
.result-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}
.result-subtitle {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}
.result-card-right {
  display: flex;
  gap: 20px;
}
.result-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 50px;
}
.stat-num {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}
.stat-success .stat-num { color: #16a34a; }
.stat-failure .stat-num { color: #dc2626; }
.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 3px;
}

/* ===== 子页签区域 ===== */
.result-sub-tabs {
  border-radius: 8px;
  overflow: hidden;
}
.result-sub-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  border-radius: 8px 8px 0 0;
}
.result-sub-tabs :deep(.el-tabs__nav) {
  border: none !important;
}
.result-sub-tabs :deep(.el-tabs__item) {
  border: none !important;
  padding: 0 16px !important;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
  border-bottom: 2px solid transparent !important;
}
.result-sub-tabs :deep(.el-tabs__item.is-active) {
  border-bottom-color: var(--el-color-primary) !important;
}
.sub-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.sub-tab-label--success { color: #16a34a; }
.sub-tab-label--danger { color: #dc2626; }

.tab-table-wrap {
  border: 1px solid #e2e8f0;
  border-top: none;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
}

/* 失败工具栏 */
.failure-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #fef2f2;
  border-left: 3px solid #ef4444;
  border-radius: 0;
}
.failure-hint {
  font-size: 12px;
  color: #94a3b8;
}

.error-reason-cell {
  color: #dc2626;
  font-size: 12px;
}

/* 表格分页栏 */
.table-pagination {
  display: flex;
  justify-content: flex-end;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-top: none;
  border-radius: 0 0 8px 8px;
  background: #fafbfc;
}
.table-pagination :deep(.el-pagination) {
  --el-pagination-button-color: #64748b;
}
.table-pagination :deep(.el-pagination .el-pager li) {
  font-size: 12px;
}
.table-pagination :deep(.el-pagination .el-select .el-input__wrapper) {
  min-width: 90px;
}

/* 回退展示 */
.result-detail {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.8;
  color: #64748b;
}
.result-detail :deep(br) { margin-bottom: 2px; }

/* ===== 底部按钮 ===== */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
