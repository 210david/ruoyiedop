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
        title="各AI功能的提示词模板。未配置（默认）时使用系统内置默认模板；点修改可自定义并即时生效，恢复默认将删除自定义配置。" />

      <el-table v-loading="loading" :data="filteredList">
        <el-table-column label="功能" min-width="200">
          <template #default="scope">
            <el-tag size="small" style="margin-right: 6px">{{ moduleName(scope.row.module) }}</el-tag>
            <span>{{ scope.row.featureName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="配置键" prop="configKey" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.noPrompt" type="info" size="small">算法功能</el-tag>
            <el-tag v-else-if="scope.row.customized" type="success" size="small">已自定义</el-tag>
            <el-tag v-else type="warning" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="模板内容" min-width="260">
          <template #default="scope">
            <span class="tpl-preview">{{ scope.row.noPrompt ? '（本地算法实现，无LLM提示词）' : preview(scope.row.template) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button v-if="!scope.row.noPrompt" link type="primary" icon="Edit"
              @click="handleEdit(scope.row)" v-hasPermi="['ai:prompt:edit']">修改</el-button>
            <el-button v-if="scope.row.customized" link type="warning" icon="RefreshLeft"
              @click="handleReset(scope.row)" v-hasPermi="['ai:prompt:edit']">恢复默认</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !filteredList.length" description="没有匹配的功能模板" />
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
        <el-alert type="warning" :closable="false"
          :title="editRow && editRow.customized ? '当前为自定义模板，保存后即时生效；点列表中的恢复默认可回退系统内置模板。' : '当前显示的是系统内置默认模板（未保存过自定义）。可直接修改后保存，保存后即时生效。'" />
        <el-alert type="info" :closable="false" style="margin-top: 8px"
          title='占位符说明：{prompt} 代表业务组装的原始提示词（含业务数据），建议保留；不含 {prompt} 时将整体替换原始提示词。' />
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="saving" @click="handleSave">保 存</el-button>
        <el-button @click="editOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiPrompt">
import { listPrompt, updatePrompt, resetPrompt, getPromptDefaults } from '@/api/ai/manage'

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

/* 功能白名单（与后端 AiPromptController.FEATURE_NAMES 保持一致）
   noPrompt: 该功能为本地算法实现，无LLM提示词，不提供模板编辑 */
const FEATURES = [
  { module: 'dms', feature: 'repair_assistant', featureName: '智能报修' },
  { module: 'dms', feature: 'dispatch_recommend', featureName: '智能派工推荐', noPrompt: true },
  { module: 'dms', feature: 'knowledge_qa', featureName: '维修知识问答 / 知识库问答' },
  { module: 'dms', feature: 'faq_generate', featureName: 'FAQ自动沉淀' },
  { module: 'dms', feature: 'complete_report', featureName: '完工报告生成' },
  { module: 'qms', feature: 'ncr_advice', featureName: 'NCR处置建议' },
  { module: 'qms', feature: 'report_draft', featureName: '质量报告初稿' },
  { module: 'mms', feature: 'abnormal_advice', featureName: '异常诊断建议' },
  { module: 'mms', feature: 'daily_report', featureName: '生产日报生成' },
  { module: 'pms', feature: 'request_advice', featureName: '采购申请建议' },
  { module: 'pms', feature: 'price_summary', featureName: '比价分析摘要' }
]

/* 内置默认提示词（后端集中注册，供默认状态回显） */
const defaultsMap = ref({})

/* 全量功能固定展示：白名单 + 已保存配置合并，标注 默认/已自定义 */
const mergedList = computed(() => {
  const savedMap = {}
  promptList.value.forEach(item => { savedMap[item.configKey] = item })
  return FEATURES.map(f => {
    const configKey = 'ai.prompt.' + f.module + '.' + f.feature
    const saved = savedMap[configKey]
    return {
      configKey,
      module: f.module,
      featureName: f.featureName,
      noPrompt: !!f.noPrompt,
      template: saved ? saved.template : (defaultsMap.value[f.module + '.' + f.feature] || ''),
      customized: !!saved
    }
  })
})

const filteredList = computed(() => mergedList.value)

function moduleName(m) { return moduleNames[m] || m }

function preview(tpl) {
  const t = tpl || ''
  return t.length > 80 ? t.substring(0, 80) + '…' : (t || '（使用系统内置默认模板）')
}

function getList() {
  loading.value = true
  Promise.all([
    listPrompt(),
    getPromptDefaults().then(res => { defaultsMap.value = res.data || {} }).catch(() => {})
  ]).then(([res]) => {
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

function handleReset(row) {
  proxy.$modal.confirm('确认将「' + row.featureName + '」恢复为系统内置默认模板？').then(() => {
    return resetPrompt(row.configKey)
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '已恢复默认模板')
    getList()
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.tpl-preview {
  color: #606266;
  font-size: 13px;
}
</style>
