<template>
  <div class="app-container upload-path-page">
    <el-card shadow="never" class="section-card">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span class="section-title"><span class="section-dot"></span>文件上传根目录</span>
          <el-tag type="info" size="small">修改后即时生效，无需重启服务</el-tag>
        </div>
      </template>

      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        <template #title>用于存放系统所有上传文件（知识库文档、头像、附件等）。修改路径后，请务必执行「迁移文件」，把历史文件搬到新目录，否则会出现文件读取失败。</template>
      </el-alert>

      <!-- 当前生效信息 -->
      <el-descriptions :column="2" border v-loading="loading" style="margin-bottom: 18px">
        <el-descriptions-item label="当前生效路径">
          <span class="mono">{{ info.effectivePath || '—' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="配置来源">
          <el-tag :type="sourceTagType" size="small">{{ info.sourceText || '—' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目录状态">
          <el-tag v-if="info.exists && info.writable" type="success" size="small">正常（存在且可写）</el-tag>
          <el-tag v-else-if="info.exists && !info.writable" type="danger" size="small">不可写，请检查权限</el-tag>
          <el-tag v-else type="warning" size="small">不存在</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目录占用">
          <span v-if="info.usage">{{ info.usage.fileCount }} 个文件 / {{ info.usage.totalSizeText }}</span>
          <span v-else>—</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 修改路径 -->
      <el-form label-width="140px" style="max-width: 760px">
        <el-form-item label="修改上传路径">
          <el-input v-model="newPath" :disabled="!info.editable" placeholder="如 D:/EDOP/uploadPath 或 /home/edop/uploadPath" clearable>
            <template #append>
              <el-button @click="newPath = info.effectivePath" :disabled="!info.editable">填入当前值</el-button>
            </template>
          </el-input>
          <div class="form-tip">
            必须为绝对路径；目录不存在时系统会自动创建。不允许使用系统目录（如 C:/Windows）。
          </div>
          <div class="form-tip" v-if="!info.editable" style="color: #e6a23c">
            当前路径由环境变量 EDOP_UPLOAD_PATH 指定，优先级高于页面配置，页面修改不生效。如需改用页面配置，请移除该环境变量并重启服务。
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving" :disabled="!info.editable">保 存 并 生 效</el-button>
          <el-button icon="Refresh" @click="loadInfo">刷 新</el-button>
        </el-form-item>
      </el-form>

      <!-- 文件迁移 -->
      <el-divider content-position="left">文件迁移</el-divider>
      <el-form label-width="140px" style="max-width: 760px">
        <el-form-item label="源目录">
          <el-input v-model="migrateFrom" placeholder="旧上传目录，如 D:/ruoyi/uploadPath" clearable />
          <div class="form-tip">
            把「源目录」下的全部文件复制到「当前生效路径」。已存在且大小相同的文件会自动跳过，不会重复复制。
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" plain icon="FolderOpened" @click="handleMigrate" :loading="migrating">执 行 迁 移</el-button>
          <span class="ops-tip">源目录不会删除，迁移后请自行确认无误再清理</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="UploadPath">
import { ref, computed, getCurrentInstance } from 'vue'
import { getUploadPathInfo, updateUploadPath, migrateUploadPath } from '@/api/system/uploadPath'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const saving = ref(false)
const migrating = ref(false)
const info = ref({})
const newPath = ref('')
const migrateFrom = ref('')

const sourceTagType = computed(() => {
  const s = info.value.source
  if (s === 'env') return 'warning'
  if (s === 'db') return 'success'
  return 'info'
})

function loadInfo() {
  loading.value = true
  getUploadPathInfo().then(res => {
    info.value = res.data || {}
    if (!newPath.value) newPath.value = info.value.effectivePath || ''
  }).finally(() => { loading.value = false })
}

function handleSave() {
  if (!newPath.value || !newPath.value.trim()) {
    proxy.$modal.msgWarning('请输入上传路径')
    return
  }
  const oldPath = info.value.effectivePath
  const changed = oldPath && oldPath !== newPath.value.trim()
  const doSave = () => {
    saving.value = true
    updateUploadPath(newPath.value.trim()).then(res => {
      proxy.$modal.msgSuccess(res.msg || '上传路径已更新并即时生效')
      // 修改后自动带出迁移源目录，方便一步完成迁移
      if (changed) migrateFrom.value = oldPath
      loadInfo()
    }).finally(() => { saving.value = false })
  }
  if (changed) {
    proxy.$modal.confirm(`确认把上传路径从\n${oldPath}\n修改为\n${newPath.value.trim()}\n\n修改后请执行「迁移文件」，否则历史文件将无法读取。`).then(doSave).catch(() => {})
  } else {
    doSave()
  }
}

function handleMigrate() {
  if (!migrateFrom.value || !migrateFrom.value.trim()) {
    proxy.$modal.msgWarning('请输入待迁移的源目录')
    return
  }
  const from = migrateFrom.value.trim()
  const to = info.value.effectivePath
  if (from === to) {
    proxy.$modal.msgWarning('源目录与当前上传目录相同，无需迁移')
    return
  }
  proxy.$modal.confirm(`确认把\n${from}\n下的所有文件迁移到\n${to}\n？\n\n（源目录不会被删除）`).then(() => {
    migrating.value = true
    migrateUploadPath(from).then(res => {
      const d = res.data || {}
      if (d.errorCount > 0) {
        proxy.$modal.msgWarning(`迁移完成：成功 ${d.fileCount} 个文件，失败 ${d.errorCount} 个，请查看后端日志`)
      } else {
        proxy.$modal.msgSuccess(`迁移完成：共 ${d.fileCount} 个文件、${d.dirCount} 个目录`)
      }
      loadInfo()
    }).finally(() => { migrating.value = false })
  }).catch(() => {})
}

loadInfo()
</script>

<style scoped>
.section-card { border-radius: 10px; }
.section-title { font-size: 15px; font-weight: 600; color: #1f2937; display: inline-flex; align-items: center; }
.section-dot { width: 6px; height: 6px; border-radius: 50%; background: #1d4ed8; display: inline-block; margin-right: 8px; }
.form-tip { font-size: 12px; color: #9ca3af; line-height: 1.6; margin-top: 4px; }
.ops-tip { font-size: 12px; color: #9ca3af; margin-left: 12px; }
.mono { font-family: Consolas, Monaco, monospace; font-size: 13px; color: #1d4ed8; }
</style>
