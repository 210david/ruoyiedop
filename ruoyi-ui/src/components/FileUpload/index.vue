<template>
  <div class="upload-file">
    <!-- 一行式布局（表单网格用）：按钮 / 必填星号 / 提示文字 同一行排布，避免提示区占满整行 -->
    <div class="upload-file-main" :class="{ 'is-inline': inlineTip }">
      <el-upload
        v-if="!disabled"
        multiple
        :action="uploadFileUrl"
        :before-upload="handleBeforeUpload"
        :file-list="fileList"
        :data="data"
        :limit="limit"
        :on-error="handleUploadError"
        :on-exceed="handleExceed"
        :on-success="handleUploadSuccess"
        :on-progress="handleUploadProgress"
        :show-file-list="false"
        :headers="headers"
        class="upload-file-uploader"
        ref="fileUpload"
      >
        <!-- 上传按钮 -->
        <el-button type="primary">选取文件</el-button>
      </el-upload>
      <!-- 必填星号：紧跟“选取文件”按钮，不被满宽的提示区推到最右 -->
      <i v-if="reqStar && !disabled" class="upload-file-req" title="必填">*</i>
      <!-- 上传提示：仅保留大小与格式约束 -->
      <div class="el-upload__tip" v-if="showTip && !disabled">
        单个文件不超过 <b style="color: #f56c6c">{{ effectiveFileSize }}MB</b><template v-if="fileType">，支持 <b style="color: #f56c6c">{{ fileType.join("/") }}</b> 格式</template>
      </div>
    </div>
    <!-- 上传进度（仅在上传中显示，避免只有转圈无法判断进度） -->
    <div v-if="uploading" class="upload-file-progress">
      <el-progress :percentage="uploadPercent" :status="uploadPercent >= 100 ? 'success' : undefined" :stroke-width="10" />
      <div class="upload-file-progress-text">正在上传：{{ uploadFileName }}（{{ uploadPercent }}%），请稍候...</div>
    </div>
    <!-- 文件列表（编辑态） -->
    <transition-group
      v-if="!disabled"
      ref="uploadFileList" class="upload-file-list el-upload-list el-upload-list--text" name="el-fade-in-linear" tag="ul">
      <li :key="file.uid" class="el-upload-list__item ele-upload-list__item-content" v-for="(file, index) in fileList">
        <el-link :href="buildSecureUrl(file.url)" underline="never" target="_blank">
          <span class="el-icon-document"> {{ getFileName(file.name) }} </span>
        </el-link>
        <div class="ele-upload-list__item-content-action">
          <el-link underline="never" @click="handlePreview(file)" type="success">&nbsp;预览</el-link>
          <el-link underline="never" @click="handleDownload(file)" type="primary">&nbsp;下载</el-link>
          <el-link underline="never" @click="handleDelete(index)" type="danger" v-if="!disabled">&nbsp;删除</el-link>
        </div>
      </li>
    </transition-group>
    <!-- 只读回显（审批/详情页）：文件卡片 + 预览/下载按钮，不显示删除与上传区 -->
    <div v-if="disabled && fileList.length" class="upload-file-ro">
      <div class="upload-file-ro-item" v-for="file in fileList" :key="file.uid">
        <svg class="upload-file-ro-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        <span class="upload-file-ro-name" :title="getFileName(file.name)">{{ getFileName(file.name) }}</span>
        <span class="upload-file-ro-actions">
          <button type="button" class="upload-file-ro-btn is-view" @click="handlePreview(file)">预览</button>
          <button type="button" class="upload-file-ro-btn is-down" @click="handleDownload(file)">下载</button>
        </span>
      </div>
    </div>
    <!-- 文件预览 -->
    <file-preview ref="filePreviewRef" />
  </div>
</template>

<script setup>
import { getToken } from "@/utils/auth"
import { getUploadMaxSize } from "@/api/common/uploadLimit"
import Sortable from 'sortablejs'

// Token 可能会过期，使用 computed 每次上传时动态获取最新 Token

const props = defineProps({
  modelValue: [String, Object, Array],
  // 上传接口地址
  action: {
    type: String,
    default: "/common/upload"
  },
  // 上传携带的参数
  data: {
    type: Object
  },
  // 数量限制
  limit: {
    type: Number,
    default: 5
  },
  // 大小限制(MB)
  fileSize: {
    type: Number,
    default: 50
  },
  // 文件类型, 例如['png', 'jpg', 'jpeg']
  fileType: {
    type: Array,
    default: () => ["doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "pdf"]
  },
  // 是否显示提示
  isShowTip: {
    type: Boolean,
    default: true
  },
  // 禁用组件（仅查看文件）
  disabled: {
    type: Boolean,
    default: false
  },
  // 拖动排序
  drag: {
    type: Boolean,
    default: true
  },
  // 行内排版：上传按钮 / 必填星号 / 提示说明 排在同一行（表单网格单元格里用）
  inlineTip: {
    type: Boolean,
    default: false
  },
  // 是否在按钮后渲染必填星号（由表单渲染层传入，避免星号被容器推到单元格最右）
  reqStar: {
    type: Boolean,
    default: false
  }
})

const { proxy } = getCurrentInstance()
const emit = defineEmits()
const number = ref(0)
// 真实的在途上传请求数：成功/失败回调各递减一次，避免计数错位导致 loading 永不关闭
const inflightCount = ref(0)
const uploadList = ref([])
const baseUrl = import.meta.env.VITE_APP_BASE_API
const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + props.action) // 上传文件服务器地址
const headers = computed(() => ({ Authorization: "Bearer " + getToken() }))
const fileList = ref([])
// 上传进度状态
const uploading = ref(false)
const uploadPercent = ref(0)
const uploadFileName = ref('')
const showTip = computed(
  () => props.isShowTip && (props.fileType || props.fileSize)
)

// 后台配置的单文件上限（MB）。未取到配置时沿用组件 props，保证离线可用。
const serverMaxMb = ref(0)
/** 实际生效的上限：取「props 与后台配置」的较小值，避免前端提示大于网关限制 */
const effectiveFileSize = computed(() => {
  if (serverMaxMb.value > 0) {
    return Math.min(props.fileSize || serverMaxMb.value, serverMaxMb.value)
  }
  return props.fileSize
})
getUploadMaxSize().then(res => {
  const mb = Number(res.maxSizeMb)
  if (mb > 0) serverMaxMb.value = mb
}).catch(() => { /* 取不到配置时静默回落，不阻塞上传 */ })

/**
 * 将 /profile/... 资源路径转换为安全访问 URL
 * <a> 标签无法携带自定义 Authorization Header，通过 token 参数传递
 */
function buildSecureUrl(url) {
  if (!url) return ''
  return baseUrl + '/common/file/access?resource=' + encodeURIComponent(url) + '&token=' + getToken()
}

watch(() => props.modelValue, val => {
  if (val) {
    let temp = 1
    // 首先将值转为数组
    const list = Array.isArray(val) ? val : props.modelValue.split(',')
    // 然后将数组转为对象数组
    fileList.value = list.map(item => {
      if (typeof item === "string") {
        item = { name: item, url: item }
      }
      item.uid = item.uid || new Date().getTime() + temp++
      return item
    })
  } else {
    fileList.value = []
    return []
  }
},{ deep: true, immediate: true })

// 上传前校检格式和大小
function handleBeforeUpload(file) {
  // 校检文件类型
  if (props.fileType.length) {
    const fileName = file.name.split('.')
    const fileExt = fileName[fileName.length - 1]
    const isTypeOk = props.fileType.indexOf(fileExt) >= 0
    if (!isTypeOk) {
      proxy.$modal.msgError(`文件格式不正确，请上传${props.fileType.join("/")}格式文件!`)
      return false
    }
  }
  // 校检文件名是否包含特殊字符
  if (file.name.includes(',')) {
    proxy.$modal.msgError('文件名不正确，不能包含英文逗号!')
    return false
  }
  // 校检文件大小
  const limitMb = effectiveFileSize.value
  if (limitMb) {
    const isLt = file.size / 1024 / 1024 < limitMb
    if (!isLt) {
      proxy.$modal.msgError(`上传文件大小不能超过 ${limitMb} MB!`)
      return false
    }
  }
  // 记录当前上传文件，用于进度展示
  uploadFileName.value = file.name
  uploadPercent.value = 0
  uploading.value = true
  proxy.$modal.loading("正在上传文件，请稍候...")
  number.value++
  inflightCount.value++
  return true
}

// 文件个数超出
function handleExceed() {
  proxy.$modal.msgError(`上传文件数量不能超过 ${props.limit} 个!`)
}

// 上传进度
function handleUploadProgress(event, file) {
  uploading.value = true
  if (file && file.name) uploadFileName.value = file.name
  const percent = event && event.percent ? Math.floor(event.percent) : 0
  // 浏览器上传到 100% 后仍需等待服务端写入磁盘，避免直接跳满造成误解
  uploadPercent.value = percent >= 100 ? 99 : percent
}

// 上传失败
function handleUploadError(err) {
  inflightCount.value = Math.max(0, inflightCount.value - 1)
  uploading.value = false
  uploadPercent.value = 0
  // 大文件被网关拦截时给出可判断的提示，而不是笼统的「上传文件失败」
  let reason = ''
  try {
    const body = err && err.message ? JSON.parse(err.message) : null
    if (body && body.msg) reason = body.msg
  } catch (e) { /* 非 JSON 响应，忽略 */ }
  const msg = err && err.message ? String(err.message) : ''
  if (msg.indexOf('413') > -1) {
    reason = '文件超过服务器允许的大小上限，请联系管理员调整上传限制'
  } else if (msg.indexOf('504') > -1 || msg.indexOf('timeout') > -1) {
    reason = '上传超时，文件较大时请分卷上传或稍后重试'
  }
  proxy.$modal.msgError(reason ? `上传文件失败：${reason}` : "上传文件失败，请检查网络或文件大小后重试")
  proxy.$modal.closeLoading()
}

// 上传成功回调
function handleUploadSuccess(res, file) {
  inflightCount.value = Math.max(0, inflightCount.value - 1)
  uploading.value = false
  uploadPercent.value = 100
  if (res.code === 200) {
    uploadList.value.push({ name: res.fileName, url: res.fileName })
    uploadedSuccessfully()
  } else {
    number.value--
    proxy.$modal.closeLoading()
    proxy.$modal.msgError(res.msg)
    proxy.$refs.fileUpload.handleRemove(file)
    uploadedSuccessfully()
  }
}

// 预览文件
function handlePreview(file) {
  proxy.$refs.filePreviewRef.open(file.url, getFileName(file.name))
}

// 下载文件（<a> 无法携带 Authorization，改为带 Token 的请求取 Blob 后触发下载）
function handleDownload(file) {
  const fileName = getFileName(file.name)
  fetch(buildSecureUrl(file.url), { headers: { Authorization: 'Bearer ' + getToken() } })
    .then((res) => {
      if (!res.ok) throw new Error('HTTP ' + res.status)
      return res.blob()
    })
    .then((blob) => {
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      setTimeout(() => URL.revokeObjectURL(url), 200)
    })
    .catch(() => proxy.$modal.msgError('文件下载失败'))
}

// 删除文件
function handleDelete(index) {
  fileList.value.splice(index, 1)
  emit("update:modelValue", listToString(fileList.value))
}

// 上传结束处理
function uploadedSuccessfully() {
  // 仍有在途请求时等待最后一个请求结束后统一收尾
  if (inflightCount.value > 0) {
    return
  }
  if (uploadList.value.length > 0) {
    fileList.value = fileList.value.filter(f => f.url !== undefined).concat(uploadList.value)
    uploadList.value = []
    emit("update:modelValue", listToString(fileList.value))
  }
  number.value = 0
  uploading.value = false
  uploadPercent.value = 0
  proxy.$modal.closeLoading()
}

// 获取文件名称
function getFileName(name) {
  // 如果是url那么取最后的名字 如果不是直接返回
  if (name.lastIndexOf("/") > -1) {
    return name.slice(name.lastIndexOf("/") + 1)
  } else {
    return name
  }
}

// 对象转成指定字符串分隔
function listToString(list, separator) {
  let strs = ""
  separator = separator || ","
  for (let i in list) {
    if (list[i].url) {
      strs += list[i].url + separator
    }
  }
  return strs != '' ? strs.substr(0, strs.length - 1) : ''
}

// 初始化拖拽排序
onMounted(() => {
  if (props.drag && !props.disabled) {
    nextTick(() => {
      const element = proxy.$refs.uploadFileList?.$el || proxy.$refs.uploadFileList
      Sortable.create(element, {
        ghostClass: 'file-upload-darg',
        onEnd: (evt) => {
          const movedItem = fileList.value.splice(evt.oldIndex, 1)[0]
          fileList.value.splice(evt.newIndex, 0, movedItem)
          emit('update:modelValue', listToString(fileList.value))
        }
      })
    })
  }
})
</script>
<style scoped lang="scss">
.file-upload-darg {
  opacity: 0.5;
  background: #c8ebfb;
}
.upload-file-uploader {
  margin-bottom: 5px;
}
/* 行内排版：按钮与提示说明同一行，提示文字自适应剩余宽度，不够时自动折到下一行 */
.upload-file-main.is-inline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}
.upload-file-main.is-inline .upload-file-uploader {
  margin-bottom: 0;
}
.upload-file-main.is-inline .el-upload__tip {
  flex: 1 1 260px;
  min-width: 0;
  margin: 0;
  line-height: 20px;
}
.upload-file-req {
  color: #f56c6c;
  font-style: normal;
  font-weight: 700;
  line-height: 32px;
  flex: none;
}
.upload-file-list .el-upload-list__item {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
  transition: none !important;
}
.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
.upload-file-progress {
  margin: 6px 0 10px;
  max-width: 420px;
}
.upload-file-progress-text {
  margin-top: 4px;
  font-size: 12px;
  color: #606266;
  line-height: 18px;
  word-break: break-all;
}
/* ===== 只读回显（审批/详情页）文件卡片 ===== */
.upload-file-ro {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}
.upload-file-ro-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border: 1px solid #e2eaf5;
  border-radius: 6px;
  background: #f8fafd;
  min-width: 0;
}
.upload-file-ro-item:hover {
  border-color: #b9cfe9;
  background: #f1f6fc;
}
.upload-file-ro-icon {
  flex: none;
  width: 16px;
  height: 16px;
  color: #4b7fb5;
}
.upload-file-ro-name {
  flex: 1 1 auto;
  min-width: 0;
  font-size: 13px;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.upload-file-ro-actions {
  flex: none;
  display: inline-flex;
  gap: 6px;
}
.upload-file-ro-btn {
  padding: 2px 10px;
  font-size: 12px;
  line-height: 18px;
  border-radius: 12px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s;
}
.upload-file-ro-btn.is-view {
  color: #0f9d6f;
  border-color: #bfe5d6;
  background: #f0faf6;
}
.upload-file-ro-btn.is-view:hover {
  background: #0f9d6f;
  color: #fff;
}
.upload-file-ro-btn.is-down {
  color: #2f6fb5;
  border-color: #c3d8ee;
  background: #f0f6fc;
}
.upload-file-ro-btn.is-down:hover {
  background: #2f6fb5;
  color: #fff;
}
</style>
