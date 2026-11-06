<template>
  <div class="app-container pjm-list-page pjm-attachment-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>所属项目</label>
          <div class="control">
            <el-input :model-value="selectedProjectLabel" placeholder="点击选择项目" readonly style="cursor:pointer" @click="openProjectPicker">
              <template #append><el-button icon="Search" @click.stop="openProjectPicker" /></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>资料类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.attachmentType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in attachmentTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>文件名</label>
          <div class="control">
            <el-input v-model="queryParams.fileName" placeholder="输入文件名关键字" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Upload" @click="handleUpload" v-hasPermi="['pjm:project:add']">上传资料</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['pjm:project:remove']">
            <el-icon><Delete /></el-icon> 批量删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_attachment_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="attachmentList" border
                  @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="70" resizable align="center" />
          <el-table-column label="文件名" prop="fileName" key="fileName" :min-width="260" resizable show-overflow-tooltip>
            <template #default="scope">
              <el-link :href="buildSecureUrl(scope.row.fileUrl)" target="_blank" type="primary" underline="hover">
                <el-icon style="margin-right:4px"><Document /></el-icon>{{ scope.row.fileName }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="所属项目" prop="projectName" key="projectName" :min-width="180" resizable show-overflow-tooltip>
            <template #default="scope">{{ getProjectName(scope.row.projectId) }}</template>
          </el-table-column>
          <el-table-column label="资料类型" prop="attachmentType" key="attachmentType" width="120" resizable align="center">
            <template #default="scope">
              <span class="badge" :class="typeBadge(scope.row.attachmentType)">{{ typeLabel(scope.row.attachmentType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="大小" prop="fileSize" key="fileSize" width="110" resizable align="right">
            <template #default="scope">{{ formatSize(scope.row.fileSize) }}</template>
          </el-table-column>
          <el-table-column label="扩展名" prop="fileExt" key="fileExt" width="90" resizable align="center">
            <template #default="scope">{{ scope.row.fileExt ? '.' + scope.row.fileExt.toUpperCase() : '-' }}</template>
          </el-table-column>
          <el-table-column label="上传人" prop="createBy" key="createBy" width="110" resizable align="center">
            <template #default="scope">{{ scope.row.createBy || '-' }}</template>
          </el-table-column>
          <el-table-column label="上传时间" prop="createTime" key="createTime" width="170" resizable align="center">
            <template #default="scope">{{ scope.row.createTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" key="remark" :min-width="160" resizable show-overflow-tooltip>
            <template #default="scope">{{ scope.row.remark || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handlePreview(scope.row)">预览</el-button>
                <el-button link type="primary" icon="Download" @click="handleDownload(scope.row)">下载</el-button>
                <el-button link type="warning" icon="Edit" @click="handleEditRemark(scope.row)">备注</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pjm:project:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 上传对话框 ===== -->
    <el-dialog v-model="uploadOpen" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <span class="rd-detail-header-title">上传项目资料</span>
        </div>
      </template>
      <el-form ref="uploadRef" :model="uploadForm" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="所属项目" required>
                    <el-input :model-value="uploadProjectLabel" placeholder="点击选择项目" readonly style="width:100%;cursor:pointer" @click="openUploadProjectPicker">
                      <template #append><el-button icon="Search" @click.stop="openUploadProjectPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="资料类型" required>
                    <el-select v-model="uploadForm.attachmentType" placeholder="请选择类型" style="width:100%">
                      <el-option v-for="d in attachmentTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="备注">
                    <el-input v-model="uploadForm.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="可选" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="19" x2="12" y2="12"/><polyline points="9 15 12 12 15 15"/></svg></span>文件上传</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="选择文件" required>
                <el-upload
                  ref="uploaderRef"
                  multiple
                  style="width:100%"
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  :before-upload="beforeUpload"
                  :on-success="onUploadSuccess"
                  :on-error="onUploadError"
                  :file-list="uploadFileList"
                  :limit="50"
                  :file-size="100"
                >
                  <el-button type="primary" icon="Upload">选取文件</el-button>
                  <template #tip>
                    <div class="el-upload__tip">单文件 ≤ 100MB，支持 doc/docx/xls/xlsx/pdf/ppt/pptx/zip/rar/jpg/png 等，单次最多 50 个</div>
                  </template>
                </el-upload>
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="uploading" @click="doUpload">保存资料记录</el-button>
          <el-button @click="uploadOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 编辑备注对话框 ===== -->
    <el-dialog v-model="remarkOpen" width="420px" title="编辑备注" append-to-body>
      <el-form :model="remarkForm" label-width="80px">
        <el-form-item label="文件名">
          <el-input :model-value="remarkForm.fileName" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="remarkForm.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="填写备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="remarkOpen = false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>

    <!-- 文件预览组件 -->
    <file-preview ref="filePreviewRef" />
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
    <ProjectPicker ref="uploadProjectPickerRef" @confirm="onUploadProjectPickerConfirm" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, getCurrentInstance, onMounted } from 'vue'
import { ArrowDown, Filter, Document, Upload, View, Download, Edit, Delete } from '@element-plus/icons-vue'
import { listAttachment, addAttachment, updateAttachment, delAttachment } from '@/api/pjm/attachment'
import { listProject } from '@/api/pjm/project'
import { getToken } from '@/utils/auth'
import FilePreview from '@/components/FilePreview/index.vue'
import ProjectPicker from '@/components/ProjectPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { collapsedCards, toggleCard } = useDetailCard(['c0', 'c1'])
const { pjm_project_attachment_type } = proxy.useDict('pjm_project_attachment_type')
const attachmentTypeOptions = computed(() => (pjm_project_attachment_type.value || []).map(d => ({ ...d, value: String(d.value) })))

const baseUrl = import.meta.env.VITE_APP_BASE_API
const uploadAction = baseUrl + '/common/upload'
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + getToken() }))

// ========== 查询条件 ==========
const showSearch = ref(true)
const showAdvanced = ref(false)
const loading = ref(false)
const attachmentList = ref([])
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const projectOptions = ref([])
const queryParams = reactive({ pageNum: 1, pageSize: 10, projectId: null, attachmentType: null, fileName: '' })
const projectPickerRef = ref()
const selectedProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(queryParams.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openProjectPicker() { projectPickerRef.value?.open(queryParams.projectId) }
function onProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  queryParams.projectId = p.projectId
  handleQuery()
}
const columns = reactive({ fileName: { visible: true }, attachmentType: { visible: true }, fileSize: { visible: true }, fileExt: { visible: true } })
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pjm_attachment_index')

const activeFilterCount = computed(() => {
  let c = 0
  if (queryParams.projectId) c++
  if (queryParams.attachmentType) c++
  if (queryParams.fileName) c++
  return c
})

// ========== 字典辅助 ==========
const typeLabel = (v) => attachmentTypeOptions.value.find(x => x.value === String(v))?.label || '-'
const typeBadge = (v) => ({ '1': 'blue', '2': 'green', '3': 'cyan', '4': 'amber', '5': 'violet', '6': 'indigo', '7': 'teal', '8': 'orange', '9': 'pink', '10': 'gray' }[String(v)] || 'gray')
const formatSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(2) + ' MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}
function buildSecureUrl(url) {
  if (!url) return ''
  return baseUrl + '/common/file/access?resource=' + encodeURIComponent(url) + '&token=' + getToken()
}

// ========== 项目名称 ==========
const projectNameMap = computed(() => {
  const m = {}
  projectOptions.value.forEach(p => { m[p.projectId] = p.projectName })
  return m
})
const getProjectName = (pid) => projectNameMap.value[pid] || '-'

// ========== 列表 ==========
function getList() {
  loading.value = true
  listAttachment(queryParams).then(res => {
    attachmentList.value = res.rows
    total.value = res.total
  }).finally(() => loading.value = false)
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() {
  queryParams.projectId = null; queryParams.attachmentType = null; queryParams.fileName = ''
  handleQuery()
}
function handleSelectionChange(sels) { ids.value = sels.map(s => s.attachmentId); multiple.value = !sels.length }

// ========== 项目选项 ==========
function loadProjects() {
  listProject({ pageNum: 1, pageSize: 9999 }).then(res => { projectOptions.value = res.rows || [] }).catch(() => {})
}

// ========== 删除 ==========
function handleDelete(row) {
  const idsToDel = row?.attachmentId ? [row.attachmentId] : ids.value
  if (!idsToDel.length) return
  proxy.$modal.confirm('确认删除选中的项目资料？').then(() => {
    delAttachment(idsToDel).then(() => { proxy.$modal.msgSuccess('删除成功'); getList() })
  }).catch(() => {})
}

// ========== 上传 ==========
const uploadOpen = ref(false)
const uploadRef = ref()
const uploaderRef = ref()
const uploading = ref(false)
const uploadForm = reactive({ projectId: null, attachmentType: null, remark: '' })
const uploadFileList = ref([])
const pendingFiles = ref([]) // 上传成功后待保存的文件元数据列表

function handleUpload() {
  pendingFiles.value = []
  uploadFileList.value = []
  Object.assign(uploadForm, { projectId: queryParams.projectId || null, attachmentType: null, remark: '' })
  collapsedCards.c0 = false; collapsedCards.c1 = false
  uploadOpen.value = true
}
// 上传弹窗内项目浏览框
const uploadProjectPickerRef = ref()
const uploadProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(uploadForm.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openUploadProjectPicker() { uploadProjectPickerRef.value?.open(uploadForm.projectId) }
function onUploadProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  uploadForm.projectId = p.projectId
}
function beforeUpload(file) {
  if (uploadFileList.value.length >= 50) { proxy.$modal.msgError('单次最多 50 个文件'); return false }
  const isLt = file.size / 1024 / 1024 < 100
  if (!isLt) { proxy.$modal.msgError('单文件大小不能超过 100MB'); return false }
  return true
}
function onUploadSuccess(res, file) {
  if (res.code === 200) {
    const ext = (file.name.split('.').pop() || '').toLowerCase()
    pendingFiles.value.push({
      file_name: file.name,
      file_url: res.fileName,
      file_size: file.size,
      file_ext: ext
    })
  } else {
    proxy.$modal.msgError(res.msg || '上传失败')
  }
}
function onUploadError() {
  proxy.$modal.msgError('文件上传失败，请检查网络')
}
function doUpload() {
  if (!uploadForm.projectId) { proxy.$modal.msgError('请选择所属项目'); return }
  if (!uploadForm.attachmentType) { proxy.$modal.msgError('请选择资料类型'); return }
  if (!pendingFiles.value.length) { proxy.$modal.msgError('请先选择文件上传'); return }

  uploading.value = true
  const savePromises = pendingFiles.value.map(f => {
    const payload = {
      projectId: uploadForm.projectId,
      attachmentType: String(uploadForm.attachmentType),
      fileName: f.file_name,
      fileUrl: f.file_url,
      fileSize: f.file_size,
      fileExt: f.file_ext,
      remark: uploadForm.remark
    }
    return addAttachment(payload)
  })

  Promise.all(savePromises).then(() => {
    proxy.$modal.msgSuccess(`成功保存 ${pendingFiles.value.length} 条资料记录`)
    uploadOpen.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('保存失败，请检查数据')
  }).finally(() => { uploading.value = false })
}

// ========== 预览 / 下载 ==========
function handlePreview(row) {
  if (!row.fileUrl) { proxy.$modal.msgError('文件路径缺失'); return }
  proxy.$refs.filePreviewRef.open(row.fileUrl, row.fileName)
}
function handleDownload(row) {
  const url = buildSecureUrl(row.fileUrl)
  const a = document.createElement('a')
  a.href = url
  a.download = row.fileName
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

// ========== 编辑备注 ==========
const remarkOpen = ref(false)
const remarkForm = reactive({ attachmentId: null, fileName: '', remark: '' })
function handleEditRemark(row) {
  remarkForm.attachmentId = row.attachmentId
  remarkForm.fileName = row.fileName
  remarkForm.remark = row.remark || ''
  remarkOpen.value = true
}
function saveRemark() {
  updateAttachment({ attachmentId: remarkForm.attachmentId, remark: remarkForm.remark }).then(() => {
    proxy.$modal.msgSuccess('备注已更新')
    remarkOpen.value = false
    getList()
  })
}

// ========== 初始化 ==========
onMounted(() => {
  loadProjects()
  getList()
})
</script>

<style scoped lang="scss">
.pjm-attachment-page { min-height: 0; }
.action-btn-row { white-space: nowrap; }
</style>
