<template>
  <div class="app-container flowable-aigen-page">
    <!-- ===== 筛选条件卡片 ===== -->
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
          <label>任务名称</label>
          <div class="control">
            <el-input v-model="queryParams.taskName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程名称</label>
          <div class="control">
            <el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><DocumentAdd /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程Key</label>
          <div class="control">
            <el-input v-model="queryParams.processKey" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><CollectionTag /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建人</label>
          <div class="control">
            <el-input v-model="queryParams.createBy" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="filter-actions">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 任务列表 ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button class="status-tab tab-pending" :class="{ 'is-active': activeStatusTab === '1' }" @click="handleStatusTabClick('1')">
            <span class="dot"></span><span>待确认</span><span class="count">{{ statusCounts['1'] }}</span>
          </button>
          <button class="status-tab tab-approved" :class="{ 'is-active': activeStatusTab === '2' }" @click="handleStatusTabClick('2')">
            <span class="dot"></span><span>已生成</span><span class="count">{{ statusCounts['2'] }}</span>
          </button>
          <button class="status-tab tab-void" :class="{ 'is-active': activeStatusTab === '3' }" @click="handleStatusTabClick('3')">
            <span class="dot"></span><span>失败</span><span class="count">{{ statusCounts['3'] }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="helpVisible = true">
          <el-icon><QuestionFilled /></el-icon>业务操作说明
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="MagicStick" @click="openWizard" v-hasPermi="['flowable:aigen:analyze']">AI生成流程</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete" v-hasPermi="['flowable:aigen:remove']">批量删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_aigen_index" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table"
          @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="80" align="center" />
          <el-table-column label="任务名称" prop="taskName" key="taskName" :min-width="colWidth('taskName', 180)" align="left" show-overflow-tooltip resizable v-if="columns.taskName.visible">
            <template #default="scope"><span class="col-strong">{{ scope.row.taskName || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="流程名称" prop="processName" key="processName" :min-width="colWidth('processName', 170)" align="left" show-overflow-tooltip resizable v-if="columns.processName.visible">
            <template #default="scope">{{ scope.row.processName || '—' }}</template>
          </el-table-column>
          <el-table-column label="流程Key" prop="processKey" key="processKey" :min-width="colWidth('processKey', 170)" align="left" show-overflow-tooltip resizable v-if="columns.processKey.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.processKey || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="流程节点" prop="nodeCount" key="nodeCount" :width="colWidth('nodeCount', 105)" align="center" resizable v-if="columns.nodeCount.visible">
            <template #default="scope">{{ scope.row.nodeCount || 0 }} 个</template>
          </el-table-column>
          <el-table-column label="表单字段" prop="fieldCount" key="fieldCount" :width="colWidth('fieldCount', 105)" align="center" resizable v-if="columns.fieldCount.visible">
            <template #default="scope">{{ scope.row.fieldCount || 0 }} 个</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 110)" align="center" resizable v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusMeta(scope.row.status).cls">
                <span class="dot"></span>{{ statusMeta(scope.row.status).label }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 170)" align="center" resizable v-if="columns.createTime.visible" />
          <el-table-column label="创建人" prop="createBy" key="createBy" :width="colWidth('createBy', 110)" align="center" resizable v-if="columns.createBy.visible" />
          <el-table-column label="操作" width="240" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['flowable:aigen:query']">查看</el-button>
                <el-button link type="warning" icon="RefreshRight" @click="handleReanalyzeRow(scope.row)" v-hasPermi="['flowable:aigen:analyze']">重新解析</el-button>
                <el-button link type="success" icon="MagicStick" :disabled="scope.row.status !== '1'" @click="handleContinue(scope.row)" v-hasPermi="['flowable:aigen:generate']">生成</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:aigen:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== AI生成向导 ===== -->
    <el-dialog v-model="wizardVisible" title="AI流程生成" width="1000px" top="5vh" draggable
      :close-on-click-modal="false" class="aigen-wizard-dialog" @closed="resetWizard">
      <el-steps :active="wizardStep" finish-status="success" align-center class="wizard-steps">
        <el-step title="上传方案" />
        <el-step title="确认结构" />
        <el-step title="生成草稿" />
      </el-steps>

      <!-- 步骤1：上传 -->
      <div v-if="wizardStep === 0" class="wizard-body">
        <el-form label-width="90px">
          <el-form-item label="任务名称">
            <el-input v-model="taskName" placeholder="留空则使用文件名" maxlength="100" style="max-width:420px" />
          </el-form-item>
          <el-form-item label="流程方案">
            <el-upload ref="uploadRef" class="plan-upload" drag :auto-upload="false" :limit="1"
              accept=".pdf,.docx,.md,.txt,.png,.jpg,.jpeg,.bmp,.webp" :on-change="onFileChange" :on-remove="onFileRemove">
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">将流程开发方案拖到此处，或<em>点击选择文件</em></div>
              <template #tip>
                <div class="el-upload__tip">支持 pdf / docx / md / txt 文档，及 png / jpg / jpeg / bmp / webp 截图（图片不超过 20MB）；扫描版 PDF 请转成图片上传</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="备注说明">
            <el-input v-model="remark" type="textarea" :rows="3" maxlength="500" show-word-limit
              placeholder="选填。可补充生成要求，例如：审批按岗位设置；金额超过5000需总经理审批；表单需要采购明细子表并必填附件；表单字段按“基本信息/采购信息”分组等"
              style="max-width:640px" />
          </el-form-item>
        </el-form>
        <el-alert type="info" :closable="false" show-icon
          title="AI会分两次并行解析：先抽取审批节点与连线，再抽取表单字段与布局；填写备注说明可进一步提升准确度。生成结果仅保存为草稿，确认后才会写入流程模型与表单。" />
      </div>

      <!-- 步骤2：确认 -->
      <div v-if="wizardStep === 1" class="wizard-body">
        <el-alert v-if="wizardTask && wizardTask.status === '3'" type="error" :closable="false" show-icon
          :title="'解析失败：' + (wizardTask.errorMsg || 'AI未能返回有效结果')" style="margin-bottom:12px" />

        <template v-if="parsed">
          <div class="meta-grid">
            <el-form-item label="流程名称" label-width="82px">
              <el-input v-model="parsed.processName" maxlength="100" />
            </el-form-item>
            <el-form-item label="流程Key" label-width="82px">
              <el-input v-model="parsed.processKey" maxlength="50" placeholder="小写字母/数字/下划线" />
            </el-form-item>
            <el-form-item label="流程分类" label-width="82px">
              <el-input v-model="parsed.category" maxlength="64" placeholder="可留空" />
            </el-form-item>
          </div>

          <el-alert v-if="parsed.warnings && parsed.warnings.length" type="warning" :closable="false" show-icon style="margin-bottom:10px">
            <template #title>
              <div v-for="(w, i) in parsed.warnings" :key="'w' + i">{{ w }}</div>
            </template>
          </el-alert>
          <el-alert v-if="parsed.unresolved && parsed.unresolved.length" type="error" :closable="false" show-icon style="margin-bottom:10px">
            <template #title>
              <div>以下办理人未匹配到系统角色/部门，生成后请在流程模型中手动配置：</div>
              <div>{{ parsed.unresolved.join('；') }}</div>
            </template>
          </el-alert>

          <el-tabs v-model="activePreviewTab">
            <el-tab-pane :label="'流程结构（' + (parsed.nodes ? parsed.nodes.length : 0) + '）'" name="process">
              <el-table :data="parsed.nodes || []" border size="small" max-height="340">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column label="类型" width="110" align="center">
                  <template #default="scope">
                    <span class="badge" :class="nodeTypeMeta(scope.row.type).cls">
                      <span class="dot"></span>{{ nodeTypeMeta(scope.row.type).label }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="节点名称（可修改）" min-width="220">
                  <template #default="scope">
                    <el-input v-model="scope.row.name" size="small" maxlength="100" />
                  </template>
                </el-table-column>
                <el-table-column label="办理人（AI自动匹配）" min-width="240">
                  <template #default="scope">
                    <template v-if="scope.row.participants && scope.row.participants.length">
                      <el-tag v-for="(p, i) in scope.row.participants" :key="'p' + i" size="small"
                        :type="p.type === 'role' ? 'primary' : 'success'" style="margin:2px 4px 2px 0">
                        {{ p.name }}
                      </el-tag>
                    </template>
                    <span v-else-if="scope.row.type === 'userTask'" class="text-muted">未匹配，生成后手动配置</span>
                    <span v-else class="text-muted">—</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane :label="'流程连线（' + (parsed.flows ? parsed.flows.length : 0) + '）'" name="flows">
              <el-table :data="parsed.flows || []" border size="small" max-height="340">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column label="起点" min-width="170">
                  <template #default="scope">{{ nodeName(scope.row.from) }}</template>
                </el-table-column>
                <el-table-column label="终点" min-width="170">
                  <template #default="scope">{{ nodeName(scope.row.to) }}</template>
                </el-table-column>
                <el-table-column label="条件表达式" min-width="240">
                  <template #default="scope"><span class="col-mono">{{ scope.row.condition || '—' }}</span></template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane :label="'表单字段（' + (parsed.form && parsed.form.fields ? parsed.form.fields.length : 0) + '）'" name="fields">
              <el-table :data="(parsed.form && parsed.form.fields) || []" border size="small" max-height="340">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column label="字段名" width="150" align="left">
                  <template #default="scope"><span class="col-mono">{{ scope.row.field }}</span></template>
                </el-table-column>
                <el-table-column label="显示名称（可修改）" min-width="180">
                  <template #default="scope">
                    <el-input v-model="scope.row.label" size="small" maxlength="100" />
                  </template>
                </el-table-column>
                <el-table-column label="控件类型" width="130" align="center">
                  <template #default="scope">
                    <el-select v-model="scope.row.form" size="small">
                      <el-option v-for="f in formTypeOptions" :key="f.value" :label="f.label" :value="f.value" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="所属表" width="100" align="center">
                  <template #default="scope">
                    <span class="badge" :class="scope.row.table === 'main' ? 'gray' : 'violet'">
                      <span class="dot"></span>{{ scope.row.table === 'main' ? '主表' : '明细' }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="必填" width="80" align="center">
                  <template #default="scope">
                    <el-switch v-model="scope.row.required" />
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>

          <div class="form-tip" style="margin-top:10px">
            提示：生成后会同步创建表单数据表（明细表一并创建），可在「表单管理」调整布局与字段，在「流程模型」调整节点与办理人。
          </div>
        </template>
      </div>

      <!-- 步骤3：完成 -->
      <div v-if="wizardStep === 2 && generated" class="wizard-body">
        <el-result icon="success" title="流程草稿已生成" :sub-title="'流程模型：' + generated.modelName + '　表单：' + generated.formName">
          <template #extra>
            <el-descriptions :column="2" border size="small" style="max-width:640px;margin:0 auto">
              <el-descriptions-item label="流程Key">{{ generated.processKey }}</el-descriptions-item>
              <el-descriptions-item label="表单Key">{{ generated.formKey || '—' }}</el-descriptions-item>
              <el-descriptions-item label="流程节点">{{ generated.nodeCount }} 个</el-descriptions-item>
              <el-descriptions-item label="表单字段">{{ generated.fieldCount }} 个</el-descriptions-item>
            </el-descriptions>
          </template>
        </el-result>
        <el-alert type="warning" :closable="false" show-icon
          title="当前仅保存为草稿，尚未部署。请到「流程管理 → 流程模型」确认流程结构与办理人后手动部署并测试发起。" />
      </div>

      <template #footer>
        <div v-if="wizardStep === 0">
          <el-button @click="wizardVisible = false">取 消</el-button>
          <el-button type="primary" :loading="analyzing" @click="startAnalyze">开始解析</el-button>
        </div>
        <div v-else-if="wizardStep === 1">
          <el-button @click="wizardStep = 0">上一步</el-button>
          <el-button :loading="analyzing" @click="handleReanalyze">重新解析</el-button>
          <el-button type="primary" :loading="generating" :disabled="!parsed" @click="handleGenerate">生成流程草稿</el-button>
        </div>
        <div v-else>
          <el-button type="primary" @click="wizardVisible = false">完 成</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 任务详情 ===== -->
    <el-dialog v-model="detailVisible" title="AI流程生成任务详情" width="900px" top="6vh" draggable>
      <el-descriptions v-if="detailTask" :column="2" border size="small">
        <el-descriptions-item label="任务名称">{{ detailTask.taskName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="方案文件">{{ detailTask.fileName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注说明" :span="2">{{ detailTask.remark || '—' }}</el-descriptions-item>
        <el-descriptions-item label="流程名称">{{ detailTask.processName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="流程Key">{{ detailTask.processKey || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <span class="badge" :class="statusMeta(detailTask.status).cls">
            <span class="dot"></span>{{ statusMeta(detailTask.status).label }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailTask.createBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailTask.createTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="流程节点 / 表单字段">{{ detailTask.nodeCount || 0 }} / {{ detailTask.fieldCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="流程模型ID">{{ detailTask.modelId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="表单Key">{{ detailTask.formKey || '—' }}</el-descriptions-item>
        <el-descriptions-item label="失败原因" :span="2" v-if="detailTask.errorMsg">
          <span style="color:#b91c1c">{{ detailTask.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="detailParsed" class="detail-json">
        <div class="detail-json-title">结构化解析结果</div>
        <pre class="json-preview">{{ prettyJson(detailParsed) }}</pre>
      </div>
    </el-dialog>

    <!-- ===== 业务操作说明 ===== -->
    <el-dialog v-model="helpVisible" title="AI流程生成 · 业务操作说明" width="840px" top="8vh" draggable>
      <div class="help-body">
        <h4>一、状态流转</h4>
        <div class="flow-steps">
          <span class="step">上传方案</span><span class="arrow">→</span>
          <span class="step">AI解析</span><span class="arrow">→</span>
          <span class="step">待确认</span><span class="arrow">→</span>
          <span class="step">生成草稿</span><span class="arrow">→</span>
          <span class="step">手动部署</span>
        </div>
        <h4>二、各状态说明</h4>
        <ul>
          <li><b>解析中</b>：已上传方案，AI正并行识别流程节点与表单字段。</li>
          <li><b>待确认</b>：AI解析完成，可修改流程名称、节点名称与字段显示名后生成。</li>
          <li><b>已生成</b>：已创建表单与流程模型草稿，但尚未部署，不会对业务产生影响。</li>
          <li><b>失败</b>：文档无法提取文字、AI调用失败或返回结果不合法，可查看失败原因并重新解析。</li>
        </ul>
        <h4>三、重点业务规则</h4>
        <ul>
          <li>流程结构与表单字段由AI分两次并行解析后合并，单次输出更聚焦，解析速度更快、字段遗漏更少。</li>
          <li>备注说明用于补充方案未覆盖的要求（审批层级、金额条件、必填项、明细表、字段分组等），重新解析时可修改并生效。</li>
          <li>生成结果默认只保存为草稿，必须在「流程模型」中确认并手动部署后才可发起流程。</li>
          <li>AI只负责抽取结构与字段，BPMN由后端按标准模板生成，避免模型直接写XML产生语法错误。</li>
          <li>办理人由AI从系统角色/部门目录中匹配；未匹配的节点会提示，需生成后手动配置。</li>
          <li>表单字段名会自动规范为小写字母/数字/下划线；明细子表与附件字段会在创建表单时一并建表。</li>
          <li>流程Key重复时会自动追加序号，不会覆盖已有流程模型。</li>
        </ul>
        <h4>四、业务操作流程</h4>
        <ol>
          <li>准备流程开发方案（审批步骤、表单要素、办理岗位等），建议使用 Word 或 Markdown。</li>
          <li>点击「AI生成流程」，上传方案并填写任务名称；如有补充要求，填写「备注说明」，点击「开始解析」。</li>
          <li>在「确认结构」页核对流程节点、连线与表单字段，必要时修改名称/控件类型/必填项。</li>
          <li>点击「生成流程草稿」，系统创建表单数据表、表单与流程模型草稿。</li>
          <li>前往「流程管理 → 表单管理」完善布局，再前往「流程模型」补充办理人并部署。</li>
        </ol>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="FlowAiGen">
import { listAiGenTask, getAiGenTask, analyzePlan, reanalyzeTask, generateFlow, delAiGenTask } from '@/api/flowable/aigen'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, ArrowDown, CollectionTag, DocumentAdd, User, QuestionFilled, UploadFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_aigen_index')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const selectedIds = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })

const statusOptions = [
  { label: '解析中', value: '0' },
  { label: '待确认', value: '1' },
  { label: '已生成', value: '2' },
  { label: '失败', value: '3' }
]

const columns = ref({
  taskName: { label: '任务名称', visible: true },
  processName: { label: '流程名称', visible: true },
  processKey: { label: '流程Key', visible: true },
  nodeCount: { label: '流程节点', visible: true },
  fieldCount: { label: '表单字段', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true },
  createBy: { label: '创建人', visible: true }
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    taskName: undefined,
    processName: undefined,
    processKey: undefined,
    status: undefined,
    createBy: undefined,
    params: {}
  }
})
const { queryParams } = toRefs(data)

const statusMap = {
  '0': { label: '解析中', cls: 'blue' },
  '1': { label: '待确认', cls: 'amber' },
  '2': { label: '已生成', cls: 'green' },
  '3': { label: '失败', cls: 'red' }
}
function statusMeta(status) {
  return statusMap[status] || { label: status || '—', cls: 'gray' }
}

const nodeTypeMap = {
  startEvent: { label: '开始', cls: 'green' },
  userTask: { label: '审批', cls: 'blue' },
  exclusiveGateway: { label: '条件网关', cls: 'amber' },
  endEvent: { label: '结束', cls: 'gray' }
}
function nodeTypeMeta(type) {
  return nodeTypeMap[type] || { label: type || '—', cls: 'gray' }
}

const formTypeOptions = [
  { label: '单行文本', value: 'text' },
  { label: '多行文本', value: 'textarea' },
  { label: '数字', value: 'number' },
  { label: '日期', value: 'date' },
  { label: '时间', value: 'time' },
  { label: '日期时间', value: 'datetime' },
  { label: '下拉选择', value: 'select' },
  { label: '单选框', value: 'radio' },
  { label: '多选框', value: 'checkbox' },
  { label: '附件', value: 'attach' },
  { label: '人员/部门', value: 'browse' }
]

// ===== 向导状态 =====
const wizardVisible = ref(false)
const wizardStep = ref(0)
const wizardTask = ref(null)
const parsed = ref(null)
const activePreviewTab = ref('process')
const taskName = ref('')
const remark = ref('')
const uploadFile = ref(null)
const uploadRef = ref(null)
const analyzing = ref(false)
const generating = ref(false)
const generated = ref(null)

// ===== 详情 =====
const detailVisible = ref(false)
const detailTask = ref(null)
const detailParsed = ref(null)

const helpVisible = ref(false)

function getList() {
  loading.value = true
  listAiGenTask(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

/** 状态标签栏数量统计 */
function loadStatusCounts() {
  const base = { pageNum: 1, pageSize: 1 }
  Promise.all([
    listAiGenTask({ ...base, status: '0' }),
    listAiGenTask({ ...base, status: '1' }),
    listAiGenTask({ ...base, status: '2' }),
    listAiGenTask({ ...base, status: '3' }),
    listAiGenTask(base)
  ]).then(([r0, r1, r2, r3, all]) => {
    statusCounts.value = {
      all: all.total || 0,
      '0': r0.total || 0,
      '1': r1.total || 0,
      '2': r2.total || 0,
      '3': r3.total || 0
    }
  }).catch(() => {
    statusCounts.value = { all: 0, '0': 0, '1': 0, '2': 0, '3': 0 }
  })
}

function handleStatusTabClick(key) {
  activeStatusTab.value = key
  queryParams.value.status = key === 'all' ? undefined : key
  handleQuery()
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1,
    pageSize: queryParams.value.pageSize,
    taskName: undefined,
    processName: undefined,
    processKey: undefined,
    status: undefined,
    createBy: undefined,
    params: {}
  }
  activeStatusTab.value = 'all'
  showAdvanced.value = false
  getList()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.taskId)
}

// ===== 向导 =====
function openWizard() {
  resetWizard()
  wizardVisible.value = true
}

function resetWizard() {
  wizardStep.value = 0
  wizardTask.value = null
  parsed.value = null
  activePreviewTab.value = 'process'
  taskName.value = ''
  remark.value = ''
  uploadFile.value = null
  uploadRef.value && uploadRef.value.clearFiles()
  generated.value = null
}

function onFileChange(file) {
  uploadFile.value = file.raw
}

function onFileRemove() {
  uploadFile.value = null
}

function startAnalyze() {
  if (!uploadFile.value) {
    proxy.$modal.msgWarning('请先选择流程开发方案文件')
    return
  }
  analyzing.value = true
  const formData = new FormData()
  formData.append('file', uploadFile.value)
  if (taskName.value) {
    formData.append('taskName', taskName.value)
  }
  if (remark.value) {
    formData.append('remark', remark.value)
  }
  analyzePlan(formData).then(res => {
    wizardTask.value = res.data.task
    parsed.value = res.data.result
    wizardStep.value = 1
    activePreviewTab.value = 'process'
    if (wizardTask.value && wizardTask.value.status === '3') {
      proxy.$modal.msgError('解析失败：' + (wizardTask.value.errorMsg || '请检查方案内容'))
    } else {
      proxy.$modal.msgSuccess('AI解析完成，请确认流程结构与表单字段')
    }
    getList()
    loadStatusCounts()
  }).finally(() => { analyzing.value = false })
}

function handleReanalyze() {
  if (!wizardTask.value) {
    return
  }
  analyzing.value = true
  reanalyzeTask(wizardTask.value.taskId, remark.value).then(res => {
    wizardTask.value = res.data.task
    parsed.value = res.data.result
    activePreviewTab.value = 'process'
    if (wizardTask.value && wizardTask.value.status === '3') {
      proxy.$modal.msgError('解析失败：' + (wizardTask.value.errorMsg || '请重试'))
    } else {
      proxy.$modal.msgSuccess('重新解析完成')
    }
    getList()
    loadStatusCounts()
  }).finally(() => { analyzing.value = false })
}

function handleGenerate() {
  if (!parsed.value || !wizardTask.value) {
    return
  }
  generating.value = true
  generateFlow({ taskId: wizardTask.value.taskId, resultJson: JSON.stringify(parsed.value) }).then(res => {
    generated.value = res.data
    wizardStep.value = 2
    proxy.$modal.msgSuccess('流程草稿生成成功')
    getList()
    loadStatusCounts()
  }).finally(() => { generating.value = false })
}

function handleContinue(row) {
  getAiGenTask(row.taskId).then(res => {
    wizardTask.value = res.data.task
    parsed.value = res.data.result
    remark.value = res.data.task?.remark || ''
    if (!parsed.value) {
      proxy.$modal.msgWarning('该任务没有可用的解析结果，请先重新解析')
      return
    }
    wizardStep.value = 1
    activePreviewTab.value = 'process'
    wizardVisible.value = true
  })
}

function handleReanalyzeRow(row) {
  getAiGenTask(row.taskId).then(res => {
    wizardTask.value = res.data.task
    parsed.value = res.data.result
    taskName.value = res.data.task?.taskName || ''
    remark.value = res.data.task?.remark || ''
    wizardVisible.value = true
    wizardStep.value = 1
    handleReanalyze()
  })
}

function handleView(row) {
  getAiGenTask(row.taskId).then(res => {
    detailTask.value = res.data.task
    detailParsed.value = res.data.result
    detailVisible.value = true
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除任务「' + (row.taskName || row.taskId) + '」？已生成的表单与流程模型草稿不会被删除。').then(() => {
    return delAiGenTask(row.taskId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
    loadStatusCounts()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e))
  })
}

function handleBatchDelete() {
  proxy.$modal.confirm('确认删除选中的 ' + selectedIds.value.length + ' 个任务？已生成的表单与流程模型草稿不会被删除。').then(() => {
    return delAiGenTask(selectedIds.value.join(','))
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
    loadStatusCounts()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('批量删除失败：' + (e?.message || e))
  })
}

function nodeName(id) {
  const node = (parsed.value && parsed.value.nodes || []).find(n => n.id === id)
  return node ? node.name : id
}

function prettyJson(obj) {
  try {
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return ''
  }
}

getList()
loadStatusCounts()
onActivated(() => {
  getList()
  loadStatusCounts()
})
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.flowable-aigen-page {
  .tip-pill {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    height: 30px;
    padding: 0 12px;
    background: #fffaf0;
    border: 1px solid #fde68a;
    color: #92400e;
    border-radius: 999px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    white-space: nowrap;
    flex-shrink: 0;
    transition: all .15s;
  }
  .tip-pill:hover {
    background: #fffbeb;
    border-color: #f59e0b;
    color: #78350f;
  }
}

.wizard-steps {
  margin: 8px 0 22px;
}

.wizard-body {
  min-height: 300px;
  padding: 4px 8px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 18px;
  margin-bottom: 4px;

  :deep(.el-form-item__label) {
    padding-right: 10px;
  }
}

.plan-upload {
  width: 100%;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    padding: 26px 10px;
  }
}

.text-muted {
  color: #9ca3af;
}

.detail-json {
  margin-top: 16px;

  .detail-json-title {
    font-weight: 600;
    color: #374151;
    margin-bottom: 8px;
  }
}

.json-preview {
  max-height: 380px;
  overflow: auto;
  margin: 0;
  padding: 12px 14px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-all;
}

.help-body {
  max-height: 66vh;
  overflow: auto;
  padding: 0 6px;
  color: #374151;
  font-size: 13px;
  line-height: 1.9;

  h4 {
    margin: 14px 0 8px;
    color: #111827;
    font-size: 14px;
  }

  ul, ol {
    margin: 0;
    padding-left: 22px;
  }

  .flow-steps {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;

    .step {
      padding: 4px 12px;
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: 999px;
      color: #1d4ed8;
      font-weight: 500;
    }

    .arrow {
      color: #9ca3af;
    }
  }
}
</style>
