<template>
  <el-dialog :title="title" v-model="visible" :width="dialogWidth" append-to-body @close="handleClose">
    <!-- ===== 导入提示说明 ===== -->
    <div v-if="tips && tips.length" class="import-tips">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          <div class="tips-title">导入说明</div>
        </template>
        <div class="tips-body">
          <div v-for="(tip, index) in tips" :key="index" class="tip-item">
            <span class="tip-num">{{ index + 1 }}</span>
            <span class="tip-text">{{ tip }}</span>
          </div>
        </div>
      </el-alert>
    </div>

    <!-- ===== 上传区域 ===== -->
    <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :headers="headers" :action="uploadUrl" :disabled="isUploading" :on-progress="handleProgress" :on-change="handleFileChange" :on-remove="handleFileRemove" :on-success="handleSuccess" :auto-upload="false" drag>
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip text-center">
          <!-- 更新选项 -->
          <div class="upload-options">
            <el-checkbox v-model="updateSupport"> {{ updateSupportLabel }} </el-checkbox>
            <!-- 更新匹配字段选择 -->
            <div v-if="updateSupport && updateKeyOptions && updateKeyOptions.length" class="update-key-selector">
              <span class="update-key-label">匹配字段：</span>
              <el-radio-group v-model="updateKey" size="small">
                <el-radio v-for="opt in updateKeyOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
              </el-radio-group>
            </div>
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link v-if="templateUrl" type="primary" underline="never" style="font-size: 12px; vertical-align: baseline" @click="handleDownloadTemplate">下载模板</el-link>
        </div>
      </template>
    </el-upload>

    <!-- ===== 导入结果展示 ===== -->
    <div v-if="importResult" class="import-result">
      <el-divider content-position="left">
        <span :class="resultHeaderClass">{{ resultHeader }}</span>
      </el-divider>
      <div class="result-summary">
        <el-tag v-if="importResult.successNum > 0" type="success" class="result-tag">成功 {{ importResult.successNum }} 条</el-tag>
        <el-tag v-if="importResult.failureNum > 0" type="danger" class="result-tag">失败 {{ importResult.failureNum }} 条</el-tag>
      </div>
      <div class="result-detail" v-html="importResult.msg"></div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="importResult" @click="handleReset">重新导入</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="isUploading">{{ isUploading ? '导入中...' : '确 定' }}</el-button>
        <el-button @click="visible = false">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance()

const props = defineProps({
  // 对话框标题
  title: {
    type: String,
    default: '数据导入'
  },
  // 对话框宽度
  width: {
    type: String,
    default: '400px'
  },
  // 上传接口地址（必传）
  action: {
    type: String,
    required: true
  },
  // 模板下载接口地址，不传则不显示下载模板链接
  templateAction: {
    type: String,
    default: ''
  },
  // 模板文件名前缀
  templateFileName: {
    type: String,
    default: 'template'
  },
  // 覆盖更新勾选框的说明文字
  updateSupportLabel: {
    type: String,
    default: '是否更新已经存在的数据'
  },
  // 导入提示说明数组，每项为一条提示
  tips: {
    type: Array,
    default: () => []
  },
  // 更新匹配字段选项，勾选更新后显示
  // 格式: [{ value: 'supplierName', label: '供应商名称' }, ...]
  updateKeyOptions: {
    type: Array,
    default: () => []
  },
  // 默认更新匹配字段
  defaultUpdateKey: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['success'])

const uploadRef = ref(null)
const visible = ref(false)
const selectedFile = ref(null)
const isUploading = ref(false)
const updateSupport = ref(false)
const updateKey = ref(props.defaultUpdateKey)
const importResult = ref(null)
const headers = { Authorization: 'Bearer ' + getToken() }

// 有提示或更新选项时加宽对话框
const dialogWidth = computed(() => {
  if (props.tips.length || (props.updateKeyOptions && props.updateKeyOptions.length)) {
    return '560px'
  }
  return props.width
})

const uploadUrl = computed(() => {
  let url = import.meta.env.VITE_APP_BASE_API + props.action + '?updateSupport=' + (updateSupport.value ? 1 : 0)
  if (updateSupport.value && updateKey.value) {
    url += '&updateKey=' + updateKey.value
  }
  return url
})

const templateUrl = computed(() => !!props.templateAction)

// 结果标题
const resultHeader = computed(() => {
  if (!importResult.value) return ''
  if (importResult.value.failureNum > 0 && importResult.value.successNum > 0) {
    return '部分导入成功'
  }
  if (importResult.value.failureNum > 0) {
    return '导入失败'
  }
  return '导入成功'
})

const resultHeaderClass = computed(() => {
  if (!importResult.value) return ''
  if (importResult.value.failureNum > 0 && importResult.value.successNum === 0) {
    return 'result-header-error'
  }
  if (importResult.value.failureNum > 0) {
    return 'result-header-warning'
  }
  return 'result-header-success'
})

// 打开对话框（供父组件通过 ref 调用）
function open() {
  updateSupport.value = false
  updateKey.value = props.defaultUpdateKey
  isUploading.value = false
  importResult.value = null
  visible.value = true
  nextTick(() => {
    selectedFile.value = null
    uploadRef.value?.clearFiles()
  })
}

// 重置导入状态，保留对话框打开
function handleReset() {
  importResult.value = null
  isUploading.value = false
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

// 关闭时清理
function handleClose() {
  isUploading.value = false
  selectedFile.value = null
  importResult.value = null
  uploadRef.value?.clearFiles()
}

// 下载模板
function handleDownloadTemplate() {
  proxy.download(props.templateAction, {}, `${props.templateFileName}_${new Date().getTime()}.xlsx`)
}

// 上传进度
function handleProgress() {
  isUploading.value = true
}

/** 文件选择处理 */
const handleFileChange = (file, fileList) => {
  selectedFile.value = file
  // 选择新文件时清除上次的结果
  importResult.value = null
}

/** 文件删除处理 */
const handleFileRemove = (file, fileList) => {
  selectedFile.value = null
}

// 上传成功
function handleSuccess(response) {
  isUploading.value = false

  // 解析后端返回的结果
  // code=200: 全部成功或部分成功（msg中包含明细）
  // code=500: 全部失败或系统错误
  const code = response.code || 200
  const msg = response.msg || ''

  if (code === 200) {
    // 成功（含部分成功）
    const successNum = response.successNum || 0
    const failureNum = response.failureNum || 0

    importResult.value = {
      successNum,
      failureNum,
      msg: msg
    }

    // 如果有成功的数据，刷新列表
    if (successNum > 0) {
      emit('success')
    }

    // 全部成功则关闭对话框
    if (failureNum === 0 && successNum > 0) {
      proxy.$modal.msgSuccess('导入成功')
      visible.value = false
    }
    // 部分成功或全部失败，保持对话框打开，显示结果，用户可点击"重新导入"
  } else {
    // 系统错误
    importResult.value = {
      successNum: 0,
      failureNum: 1,
      msg: msg || '导入失败，请检查文件格式或网络后重试'
    }
  }
}

// 提交上传
function handleSubmit() {
  const file = selectedFile.value
  if (!file || file.length === 0 || !file.name.toLowerCase().endsWith('.xls') && !file.name.toLowerCase().endsWith('.xlsx')) {
    proxy.$modal.msgError("请选择后缀为 “xls”或“xlsx”的文件。")
    return
  }
  // 如果勾选了更新但未选择匹配字段，提示
  if (updateSupport.value && props.updateKeyOptions && props.updateKeyOptions.length && !updateKey.value) {
    proxy.$modal.msgWarning('请选择更新匹配字段')
    return
  }
  uploadRef.value.submit()
}

defineExpose({ open })
</script>

<style scoped>
.import-tips {
  margin-bottom: 16px;
}
.tips-title {
  font-weight: 600;
  font-size: 14px;
}
.tips-body {
  margin-top: 6px;
}
.tip-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 4px;
  line-height: 1.6;
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
  margin-right: 6px;
  margin-top: 1px;
}
.tip-text {
  flex: 1;
  font-size: 13px;
  color: #606266;
}
.upload-options {
  margin-bottom: 8px;
}
.update-key-selector {
  margin-top: 8px;
  padding: 8px 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  text-align: left;
}
.update-key-label {
  font-size: 13px;
  color: #606266;
  margin-right: 8px;
}
.import-result {
  margin-top: 16px;
}
.result-header-success {
  color: var(--el-color-success);
  font-weight: 600;
}
.result-header-warning {
  color: var(--el-color-warning);
  font-weight: 600;
}
.result-header-error {
  color: var(--el-color-danger);
  font-weight: 600;
}
.result-summary {
  margin-bottom: 8px;
}
.result-tag {
  margin-right: 8px;
}
.result-detail {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.8;
  color: #606266;
}
.result-detail :deep(br) {
  margin-bottom: 2px;
}
</style>
