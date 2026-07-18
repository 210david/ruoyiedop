<template>
  <el-dialog :title="title" v-model="visible" width="900px" append-to-body :before-close="handleClose" class="file-preview-dialog">
    <div v-loading="loading" element-loading-text="正在加载文件，请稍候..." class="preview-container">
      <!-- 图片预览 -->
      <div v-if="previewType === 'image'" class="image-preview-wrap">
        <img :src="fullUrl" alt="预览" class="preview-image" @click="handleImageClick" />
      </div>

      <!-- PDF预览 -->
      <iframe v-else-if="previewType === 'pdf'" :src="fullUrl" class="pdf-preview-frame" frameborder="0"></iframe>

      <!-- Word预览 -->
      <div v-else-if="previewType === 'word'" ref="wordContainer" class="word-preview-wrap"></div>

      <!-- Excel预览 -->
      <div v-else-if="previewType === 'excel'" class="excel-preview-wrap">
        <div v-html="excelHtml" class="excel-content"></div>
      </div>

      <!-- 视频预览 -->
      <video v-else-if="previewType === 'video'" :src="fullUrl" controls class="video-preview"></video>

      <!-- 不支持的格式 -->
      <div v-else class="unsupported-wrap">
        <el-icon :size="48" color="#c0c4cc"><Document /></el-icon>
        <p class="unsupported-text">该文件格式暂不支持在线预览</p>
        <p class="unsupported-filename">{{ fileName }}</p>
        <el-button type="primary" :icon="Download" @click="handleDownload">下载文件</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { renderAsync } from 'docx-preview'
import * as XLSX from 'xlsx'
import { Download } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance()
const visible = ref(false)
const loading = ref(false)
const title = ref('文件预览')
const fileName = ref('')
const fileUrl = ref('')
const baseUrl = import.meta.env.VITE_APP_BASE_API
const fullUrl = computed(() => baseUrl + fileUrl.value)

const previewType = ref('')
const excelHtml = ref('')
const wordContainer = ref(null)

const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp']
const pdfExts = ['pdf']
const wordExts = ['docx', 'doc']
const excelExts = ['xlsx', 'xls']
const videoExts = ['mp4', 'avi', 'rmvb', 'webm']

function getExtension(name) {
  const idx = name.lastIndexOf('.')
  return idx > -1 ? name.slice(idx + 1).toLowerCase() : ''
}

function getPreviewType(ext) {
  if (imageExts.includes(ext)) return 'image'
  if (pdfExts.includes(ext)) return 'pdf'
  if (wordExts.includes(ext)) return 'word'
  if (excelExts.includes(ext)) return 'excel'
  if (videoExts.includes(ext)) return 'video'
  return ''
}

/** 打开预览 */
async function open(url, name) {
  fileUrl.value = url
  fileName.value = name || getFileNameFromUrl(url)
  title.value = '预览：' + fileName.value
  visible.value = true
  loading.value = true
  excelHtml.value = ''

  const ext = getExtension(fileName.value)
  previewType.value = getPreviewType(ext)

  await nextTick()

  try {
    if (previewType.value === 'word') {
      await previewWord(url, ext)
    } else if (previewType.value === 'excel') {
      await previewExcel(url)
    }
  } catch (e) {
    proxy.$modal.msgError('文件加载失败：' + e.message)
    previewType.value = ''
  } finally {
    loading.value = false
  }

  // 图片和PDF不需要loading
  if (previewType.value === 'image' || previewType.value === 'pdf' || previewType.value === 'video') {
    loading.value = false
  }
}

/** Word预览（仅支持.docx，.doc格式不支持） */
async function previewWord(url, ext) {
  if (ext === 'doc') {
    proxy.$modal.msgWarning('旧版.doc格式不支持在线预览，请下载后查看')
    previewType.value = ''
    return
  }
  const blob = await fetchBlob(url)
  await nextTick()
  await renderAsync(blob, wordContainer.value, null, {
    className: 'docx',
    inWrapper: true,
    ignoreWidth: false,
    ignoreHeight: false,
    breakPages: true,
    experimental: true,
    useBase64URL: true
  })
}

/** Excel预览 */
async function previewExcel(url) {
  const blob = await fetchBlob(url)
  const arrayBuffer = await blob.arrayBuffer()
  const workbook = XLSX.read(arrayBuffer, { type: 'array' })
  let html = ''
  workbook.SheetNames.forEach(sheetName => {
    const sheet = workbook.Sheets[sheetName]
    html += `<h3 class="excel-sheet-name">${sheetName}</h3>`
    const tableHtml = XLSX.utils.sheet_to_html(sheet, { editable: false })
    html += `<div class="excel-sheet-table">${tableHtml}</div>`
  })
  excelHtml.value = html
}

/** 获取文件Blob */
async function fetchBlob(url) {
  const response = await fetch(baseUrl + url, {
    headers: { Authorization: 'Bearer ' + getToken() }
  })
  if (!response.ok) throw new Error('HTTP ' + response.status)
  return response.blob()
}

function getFileNameFromUrl(url) {
  if (url.lastIndexOf('/') > -1) {
    return url.slice(url.lastIndexOf('/') + 1)
  }
  return url
}

function handleDownload() {
  const link = document.createElement('a')
  link.href = fullUrl.value
  link.download = fileName.value
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function handleImageClick() {
  // 使用浏览器原生图片查看
  window.open(fullUrl.value, '_blank')
}

function handleClose() {
  visible.value = false
  previewType.value = ''
  excelHtml.value = ''
  fileUrl.value = ''
  fileName.value = ''
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.preview-container {
  min-height: 400px;
  max-height: 70vh;
  overflow: auto;
}

.image-preview-wrap {
  text-align: center;
}
.preview-image {
  max-width: 100%;
  max-height: 65vh;
  cursor: pointer;
}

.pdf-preview-frame {
  width: 100%;
  height: 70vh;
  border: none;
}

.word-preview-wrap {
  max-height: 70vh;
  overflow: auto;
  padding: 20px;
  background: #f5f5f5;

  :deep(.docx-wrapper) {
    background: #f5f5f5;
    padding: 10px;
  }
  :deep(.docx-wrapper > section.docx) {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    margin-bottom: 20px;
  }
}

.excel-preview-wrap {
  max-height: 70vh;
  overflow: auto;
  padding: 10px;

  :deep(.excel-sheet-name) {
    margin: 16px 0 8px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    border-bottom: 2px solid #409eff;
    padding-bottom: 4px;
  }
  :deep(.excel-sheet-table) {
    overflow-x: auto;
  }
  :deep(.excel-sheet-table table) {
    border-collapse: collapse;
    width: 100%;
    font-size: 12px;
  }
  :deep(.excel-sheet-table td),
  :deep(.excel-sheet-table th) {
    border: 1px solid #d0d0d0;
    padding: 4px 8px;
    white-space: nowrap;
    min-width: 60px;
  }
  :deep(.excel-sheet-table tr:first-child td) {
    background: #f5f7fa;
    font-weight: 600;
  }
}

.video-preview {
  width: 100%;
  max-height: 70vh;
}

.unsupported-wrap {
  text-align: center;
  padding: 60px 20px;

  .unsupported-text {
    margin: 16px 0 8px;
    font-size: 14px;
    color: #909399;
  }
  .unsupported-filename {
    margin: 0 0 20px;
    font-size: 13px;
    color: #c0c4cc;
    word-break: break-all;
  }
}
</style>
