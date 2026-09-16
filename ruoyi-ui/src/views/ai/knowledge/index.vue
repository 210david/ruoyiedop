<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 左侧：文档管理 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span style="font-weight: 600">知识库文档</span>
              <el-button type="primary" icon="Plus" @click="uploadOpen = true" v-hasPermi="['ai:knowledge:add']">上传文档</el-button>
            </div>
          </template>

          <el-form :inline="true" style="margin-bottom: 4px">
            <el-form-item label="模块">
              <el-select v-model="queryParams.module" placeholder="全部模块" clearable style="width: 150px" @change="handleQuery">
                <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
              </el-select>
            </el-form-item>
            <el-form-item label="名称">
              <el-input v-model="queryParams.docName" placeholder="文档名称" clearable style="width: 160px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loading" :data="docList">
            <el-table-column label="文档名称" prop="docName" min-width="220" show-overflow-tooltip />
            <el-table-column label="模块" width="100" align="center">
              <template #default="scope">{{ moduleNames[scope.row.module] || scope.row.module }}</template>
            </el-table-column>
            <el-table-column label="类型" prop="fileType" width="70" align="center" />
            <el-table-column label="切片数" prop="chunkCount" width="80" align="center" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag :type="statusType[scope.row.status]" size="small">{{ statusText[scope.row.status] || scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="上传人" prop="createBy" width="100" align="center" />
            <el-table-column label="操作" width="80" align="center">
              <template #default="scope">
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['ai:knowledge:remove']">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize" @pagination="getList" />
        </el-card>
      </el-col>

      <!-- 右侧：知识问答 -->
      <el-col :span="10">
        <el-card shadow="never" class="qa-card">
          <template #header>
            <div class="card-header">
              <span style="font-weight: 600">知识库问答</span>
              <el-select v-model="qaModule" size="small" style="width: 130px">
                <el-option label="全平台" value="" />
                <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
              </el-select>
            </div>
          </template>

          <div class="qa-history" ref="qaHistoryRef">
            <div v-if="!qaHistory.length" class="qa-empty">
              <el-empty description="上传文档后即可提问，AI将基于知识库内容回答并标注引用来源" />
            </div>
            <div v-for="(item, i) in qaHistory" :key="i" class="qa-item">
              <div class="qa-question"><span class="qa-tag q">问</span>{{ item.question }}</div>
              <div class="qa-answer">
                <span class="qa-tag a">答</span>
                <div class="qa-answer-body">{{ item.answer }}</div>
                <div class="qa-sources" v-if="item.sources && item.sources.length">
                  来源：<el-tag v-for="(s, j) in item.sources" :key="j" size="small" type="info" style="margin-right: 4px">{{ s.docName }}</el-tag>
                </div>
              </div>
            </div>
          </div>

          <div class="qa-input">
            <el-input v-model="question" placeholder="输入问题，回车提问" @keyup.enter="handleAsk"
              :disabled="asking" clearable />
            <el-button type="primary" :loading="asking" @click="handleAsk" v-hasPermi="['ai:knowledge:qa']">提问</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadOpen" title="上传知识文档" width="480px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="所属模块">
          <el-select v-model="uploadModule" style="width: 100%">
            <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档文件">
          <el-upload ref="uploadRef" drag :auto-upload="false" :limit="1"
            :on-change="f => (uploadFile = f.raw)" :on-remove="() => (uploadFile = null)">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处或<em>点击选择</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 pdf / docx / md / txt（扫描版PDF暂不支持），单文件 ≤ 20MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传并解析</el-button>
        <el-button @click="uploadOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiKnowledge">
import { listKnowledge, uploadKnowledge, delKnowledge, knowledgeQa } from '@/api/ai/knowledge'
import { UploadFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const docList = ref([])
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, module: undefined, docName: undefined })

const moduleNames = {
  platform: '平台公共', dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产'
}
const statusText = { '0': '已解析', '1': '解析中', '2': '解析失败' }
const statusType = { '0': 'success', '1': 'warning', '2': 'danger' }

// 问答
const qaModule = ref('')
const question = ref('')
const asking = ref(false)
const qaHistory = ref([])

// 上传
const uploadOpen = ref(false)
const uploadModule = ref('platform')
const uploadFile = ref(null)
const uploading = ref(false)

function getList() {
  loading.value = true
  listKnowledge(queryParams.value).then(res => {
    docList.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除文档《' + row.docName + '》及其全部知识切片？').then(() => {
    return delKnowledge(row.docId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleUpload() {
  if (!uploadFile.value) {
    proxy.$modal.msgWarning('请先选择文件')
    return
  }
  uploading.value = true
  const fd = new FormData()
  fd.append('file', uploadFile.value)
  fd.append('module', uploadModule.value)
  uploadKnowledge(fd).then(() => {
    uploading.value = false
    uploadOpen.value = false
    uploadFile.value = null
    proxy.$modal.msgSuccess('上传解析成功')
    getList()
  }).catch(() => { uploading.value = false })
}

function handleAsk() {
  const q = (question.value || '').trim()
  if (!q) return
  asking.value = true
  qaHistory.value.push({ question: q, answer: '', sources: [] })
  question.value = ''
  const item = qaHistory.value[qaHistory.value.length - 1]
  knowledgeQa({ question: q, module: qaModule.value }).then(res => {
    asking.value = false
    const d = res.data || {}
    item.answer = d.answer || '未返回答案'
    item.sources = d.sources || []
  }).catch(() => {
    asking.value = false
    item.answer = '提问失败，请稍后重试'
  })
}

getList()
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.qa-card :deep(.el-card__body) { display: flex; flex-direction: column; height: 560px; }
.qa-history { flex: 1; overflow-y: auto; padding-right: 4px; }
.qa-empty { height: 100%; display: flex; align-items: center; }
.qa-item { margin-bottom: 14px; }
.qa-question { font-weight: 600; color: #303133; margin-bottom: 6px; display: flex; gap: 8px; align-items: baseline; }
.qa-answer { background: #f8fafc; border: 1px solid #e8edf3; border-radius: 8px; padding: 10px 12px; }
.qa-tag { flex: none; font-size: 12px; border-radius: 4px; padding: 1px 6px; color: #fff; }
.qa-tag.q { background: #409eff; }
.qa-tag.a { background: #059669; margin-top: 2px; }
.qa-answer-body { display: inline; white-space: pre-wrap; font-size: 13px; color: #303133; line-height: 1.8; margin-left: 4px; }
.qa-sources { margin-top: 8px; font-size: 12px; color: #909399; }
.qa-input { display: flex; gap: 8px; margin-top: 10px; }
</style>
