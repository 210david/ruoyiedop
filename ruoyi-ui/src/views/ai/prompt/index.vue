<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span style="font-size: 16px; font-weight: 600">提示词模板管理</span>
          <el-tag type="info" size="small">修改保存后即时生效，无需重启；支持 {"{占位符}"} 由系统自动填充</el-tag>
        </div>
      </template>

      <el-alert type="info" :closable="false" style="margin-bottom: 12px"
        title="各AI功能的提示词模板。未配置时使用系统内置默认模板；此处修改仅影响该功能的提示词，占位符如 {input} 会替换为实际输入内容。" />

      <el-table v-loading="loading" :data="promptList">
        <el-table-column label="功能" min-width="200">
          <template #default="scope">
            <el-tag size="small" style="margin-right: 6px">{{ moduleName(scope.row.module) }}</el-tag>
            <span>{{ scope.row.featureName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="配置键" prop="configKey" min-width="220" show-overflow-tooltip />
        <el-table-column label="模板内容" min-width="260">
          <template #default="scope">
            <span class="tpl-preview">{{ preview(scope.row.template) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['ai:prompt:edit']">修改</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !promptList.length" description="暂无自定义提示词模板（各功能使用系统内置默认模板）" />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editOpen" :title="'编辑提示词 - ' + (editRow ? editRow.featureName : '')" width="700px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="配置键">
          <el-input :model-value="editRow ? editRow.configKey : ''" disabled />
        </el-form-item>
        <el-form-item label="模板内容">
          <el-input v-model="editTemplate" type="textarea" :rows="14" placeholder="输入提示词模板，支持 {占位符}" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="saving" @click="handleSave">保 存</el-button>
        <el-button @click="editOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiPrompt">
import { listPrompt, updatePrompt } from '@/api/ai/manage'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const promptList = ref([])
const editOpen = ref(false)
const editRow = ref(null)
const editTemplate = ref('')
const saving = ref(false)

const moduleNames = {
  platform: '平台', dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产'
}

function moduleName(m) { return moduleNames[m] || m }

function preview(tpl) {
  const t = tpl || ''
  return t.length > 80 ? t.substring(0, 80) + '…' : (t || '（默认模板）')
}

function getList() {
  loading.value = true
  listPrompt().then(res => {
    promptList.value = res.data || []
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleEdit(row) {
  editRow.value = row
  editTemplate.value = row.template || ''
  editOpen.value = true
}

function handleSave() {
  if (!editTemplate.value.trim()) {
    proxy.$modal.msgWarning('模板内容不能为空')
    return
  }
  saving.value = true
  updatePrompt({ configKey: editRow.value.configKey, template: editTemplate.value }).then(() => {
    saving.value = false
    editOpen.value = false
    proxy.$modal.msgSuccess('保存成功，已即时生效')
    getList()
  }).catch(() => { saving.value = false })
}

getList()
</script>

<style scoped>
.tpl-preview {
  color: #606266;
  font-size: 13px;
}
</style>
