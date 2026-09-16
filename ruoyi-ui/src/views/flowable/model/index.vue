<template>
  <div class="app-container flowable-model-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>模型名称</label>
          <div class="control">
            <el-input v-model="queryParams.modelName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>模型Key</label>
          <div class="control">
            <el-input v-model="queryParams.modelKey" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Document /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程分类</label>
          <div class="control is-select">
            <el-select v-model="queryParams.category" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>部署状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.deployed" placeholder="全部" clearable @change="handleQuery">
              <el-option label="已部署" value="1" />
              <el-option label="未部署" value="0" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:model:add']">新增</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_model_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="模型名称" prop="modelName" min-width="160" show-overflow-tooltip>
            <template #default="scope"><span class="col-strong">{{ scope.row.modelName }}</span></template>
          </el-table-column>
          <el-table-column label="模型Key" prop="modelKey" min-width="140" show-overflow-tooltip>
            <template #default="scope"><span class="col-mono">{{ scope.row.modelKey }}</span></template>
          </el-table-column>
          <el-table-column label="流程分类" prop="category" width="110" align="center">
            <template #default="scope">
              <span class="badge violet">{{ categoryLabel(scope.row.category) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="版本" prop="version" width="80" align="center" />
          <el-table-column label="部署状态" prop="deployed" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.deployed === '1' ? 'green' : 'gray'">
                <span class="dot"></span>{{ scope.row.deployed === '1' ? '已部署' : '未部署' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" align="center" />
          <el-table-column label="操作" width="260" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['flowable:model:edit']">设计</el-button>
                <el-button link type="success" icon="Promotion" @click="handleDeploy(scope.row)" v-hasPermi="['flowable:model:deploy']">部署</el-button>
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['flowable:model:query']">查看</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:model:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- ===== 新增模型弹窗 ===== -->
    <el-dialog v-model="addOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 19l7-7 3 3-7 7-3-3z"/><path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z"/></svg>
          </div>
          <span class="rd-detail-header-title">新增流程模型</span>
        </div>
      </template>
      <el-form ref="modelRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="如：请假审批流程" maxlength="50" />
        </el-form-item>
        <el-form-item label="模型Key" prop="modelKey">
          <el-input v-model="form.modelKey" placeholder="如：leave_process（英文，流程唯一标识）" maxlength="60" />
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入流程用途描述" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="addOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 流程设计器弹窗（bpmn-js + 自研属性面板） ===== -->
    <el-dialog v-model="designOpen" fullscreen append-to-body class="designer-dialog" :close-on-click-modal="false" destroy-on-close @opened="onDesignerOpened">
      <template #header>
        <div class="designer-header">
          <div class="designer-title">
            <span class="glyph"></span>
            流程设计器
            <span class="designer-model-name">{{ designModel.modelName }}</span>
            <span class="badge blue"><span class="dot"></span>Key: {{ designModel.modelKey }}</span>
          </div>
          <div class="designer-actions">
            <el-tooltip content="撤销"><el-button icon="RefreshLeft" @click="cmdUndo" /></el-tooltip>
            <el-tooltip content="重做"><el-button icon="RefreshRight" @click="cmdRedo" /></el-tooltip>
            <el-tooltip content="放大"><el-button icon="ZoomIn" @click="zoom(0.1)" /></el-tooltip>
            <el-tooltip content="缩小"><el-button icon="ZoomOut" @click="zoom(-0.1)" /></el-tooltip>
            <el-tooltip content="适应画布"><el-button icon="FullScreen" @click="zoomReset" /></el-tooltip>
            <el-tooltip content="删除选中"><el-button icon="Delete" @click="deleteSelected" /></el-tooltip>
            <el-divider direction="vertical" />
            <el-button icon="Upload" @click="importLocalXml">导入XML</el-button>
            <el-button icon="Download" @click="exportXml">导出XML</el-button>
            <el-button type="primary" icon="Check" @click="saveDesigner" v-hasPermi="['flowable:model:edit']">保存</el-button>
            <el-button @click="designOpen = false">关 闭</el-button>
          </div>
        </div>
      </template>
      <div class="designer-body">
        <div class="designer-canvas" ref="canvasRef"></div>
        <!-- 自研属性面板：选中节点后配置 -->
        <div class="designer-props">
          <template v-if="selectedElement">
            <div class="props-title">
              <span class="glyph"></span> 节点属性
              <el-tag size="small" effect="plain" style="margin-left:8px">{{ selectedElement.type.replace('bpmn:', '') }}</el-tag>
            </div>
            <el-form label-width="86px" size="small" class="props-form">
              <el-form-item label="节点ID">
                <el-input v-model="selectedElement.id" disabled />
              </el-form-item>
              <el-form-item :label="selectedElement.isSequenceFlow ? '连线名称' : '节点名称'">
                <el-input v-model="selectedElement.name" :placeholder="selectedElement.isSequenceFlow ? '如：3天以上' : '如：部门经理审批'" @change="updateElementProp('name', selectedElement.name)" />
              </el-form-item>

              <!-- 用户任务属性 -->
              <template v-if="selectedElement.isUserTask">
                <el-form-item label="办理人">
                  <el-select v-model="selectedElement.assignee" placeholder="选择用户或留空" filterable clearable allow-create style="width:100%" @change="updateAssignee">
                    <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="String(u.userId)" />
                  </el-select>
                  <div class="props-tip">也可直接输入用户ID；审批任务将出现在其待办</div>
                </el-form-item>
                <el-form-item label="候选组">
                  <el-select v-model="selectedElement.candidateGroups" placeholder="选择角色或留空" filterable clearable allow-create style="width:100%" @change="updateCandidateGroups">
                    <el-option v-for="r in roleOptions" :key="r.roleId" :label="r.roleName + '（' + r.roleKey + '）'" :value="String(r.roleId)" />
                  </el-select>
                  <div class="props-tip">角色下所有用户均可认领办理</div>
                </el-form-item>
                <el-form-item label="多实例">
                  <el-switch v-model="selectedElement.multiInstance" @change="toggleMultiInstance" />
                  <div class="props-tip">开启即会签（所有人办理完才通过）</div>
                </el-form-item>
                <el-form-item label="表单Key">
                  <el-input v-model="selectedElement.formKey" placeholder="关联业务表单Key（可选）" @change="updateFormKey" />
                  <div class="props-tip">原型能力：任务办理页可按此 Key 加载业务表单</div>
                </el-form-item>
              </template>

              <!-- 连线条件（网关分支） -->
              <template v-if="selectedElement.isSequenceFlow">
                <el-form-item label="分支条件">
                  <el-input v-model="selectedElement.condition" type="textarea" :rows="2"
                    placeholder="如：${days > 3}" @change="updateCondition" />
                  <div class="props-tip">
                    JUEL 表达式，引用发起流程时传入的变量。{{ selectedElement.sourceIsGateway ? '当前连线路径于网关出口，按顺序判断。' : '提示：仅排他/包容网关出口的连线条件生效。' }}
                  </div>
                </el-form-item>
                <el-form-item label=" ">
                  <el-button size="small" @click="clearCondition">清除条件</el-button>
                </el-form-item>
              </template>
            </el-form>
          </template>
          <template v-else>
            <div class="props-empty">
              <el-icon :size="28"><Pointer /></el-icon>
              <p>点击画布中的节点<br/>配置审批人 / 会签等属性</p>
            </div>
          </template>
          <div class="props-help">
            <div class="props-help-title">使用提示</div>
            <ul>
              <li>左侧面板拖入节点，<b>用户任务</b>为审批节点</li>
              <li>审批节点填写「办理人ID」或「候选组ID」</li>
              <li>网关分支条件通过连线上的条件表达式配置（XML）</li>
              <li>保存后点击列表「部署」发布流程</li>
            </ul>
          </div>
        </div>
      </div>
      <input ref="xmlFileRef" type="file" accept=".xml,.bpmn" style="display:none" @change="onXmlFileChange" />
    </el-dialog>

    <!-- ===== 查看流程图弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="1000px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </div>
          <span class="rd-detail-header-title">流程图预览</span>
          <div class="rd-detail-header-sub" v-if="viewModel.modelName">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ viewModel.modelName }}（{{ viewModel.modelKey }}）</span>
          </div>
        </div>
      </template>
      <div class="viewer-wrap">
        <div class="viewer-canvas" ref="viewerRef"></div>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowModel">
import { listModel, getModel, addModel, updateModel, delModel, deployModel } from '@/api/flowable/flow'
import request from '@/utils/request'
import { ElMessageBox } from 'element-plus'
import { h } from 'vue'
import { Search, Filter, Document, Pointer, RefreshLeft, RefreshRight, ZoomIn, ZoomOut, FullScreen } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { flowable_category } = proxy.useDict('flowable_category')
const { collapsedCards, toggleCard } = useDetailCard(["v1"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_model_index')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const addOpen = ref(false)
const designOpen = ref(false)
const viewOpen = ref(false)
const designModel = ref({})
const viewModel = ref({})
const canvasRef = ref(null)
const viewerRef = ref(null)
const xmlFileRef = ref(null)
const selectedElement = ref(null)
let bpmnModeler = null
let bpmnViewer = null
let modeling = null
let selectedEl = null   // 当前选中的 bpmn 元素（非响应式）
const userOptions = ref([])
const roleOptions = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, modelName: undefined, modelKey: undefined, category: undefined, deployed: undefined },
  rules: {
    modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
    modelKey: [
      { required: true, message: '请输入模型Key', trigger: 'blur' },
      { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '仅允许字母开头，含字母/数字/下划线', trigger: 'blur' }
    ]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.modelName) count++
  if (queryParams.value.modelKey) count++
  if (queryParams.value.category) count++
  if (queryParams.value.deployed) count++
  return count
})

const defaultColumns = {
  modelName: { label: '模型名称', visible: true },
  modelKey: { label: '模型Key', visible: true },
  category: { label: '流程分类', visible: true },
  version: { label: '版本', visible: true },
  deployed: { label: '部署状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
const columns = ref(loadColumnVisibility())
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_model_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

function categoryLabel(value) {
  const item = flowable_category.value.find(d => d.value == value)
  return item ? item.label : (value || '-')
}

function getList() {
  loading.value = true
  listModel(queryParams.value).then(res => {
    let rows = res.rows || []
    if (queryParams.value.deployed) {
      rows = rows.filter(r => r.deployed === queryParams.value.deployed)
    }
    list.value = rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.modelName = undefined
  queryParams.value.modelKey = undefined
  queryParams.value.category = undefined
  queryParams.value.deployed = undefined
  handleQuery()
}

function handleAdd() {
  form.value = { modelName: undefined, modelKey: undefined, category: 'oa', description: undefined }
  addOpen.value = true
}

function submitAdd() {
  proxy.$refs['modelRef'].validate(valid => {
    if (valid) {
      addModel(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功，请点击"设计"绘制流程图')
        addOpen.value = false
        getList()
      })
    }
  })
}

// ===== bpmn-js 设计器 =====
async function onDesignerOpened() {
  // 设计器必需样式：工具面板/连线菜单/字体图标
  await import('bpmn-js/dist/assets/diagram-js.css')
  await import('bpmn-js/dist/assets/bpmn-js.css')
  await import('bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css')
  const { default: BpmnModeler } = await import('bpmn-js/lib/Modeler.js')
  bpmnModeler = new BpmnModeler({ container: canvasRef.value })
  modeling = bpmnModeler.get('modeling')
  // 选中事件 → 刷新属性面板
  const eventBus = bpmnModeler.get('eventBus')
  eventBus.on('selection.changed', e => {
    const el = e.newSelection[0]
    if (el) loadSelectedElement(el)
    else { selectedEl = null; selectedElement.value = null }
  })
  // 加载用户/角色选项（供办理人/候选组选择器）
  request({ url: '/system/user/list', method: 'get', params: { pageNum: 1, pageSize: 200 } })
    .then(res => { userOptions.value = res.rows || [] }).catch(() => {})
  request({ url: '/system/role/list', method: 'get', params: { pageNum: 1, pageSize: 100 } })
    .then(res => { roleOptions.value = res.rows || [] }).catch(() => {})
  const xml = designModel.value.bpmnXml || ''
  if (!xml) {
    try { await newBlankDiagram() } catch (e) {
      proxy.$modal.msgError('创建流程图失败：' + e.message)
    }
    return
  }
  try {
    await bpmnModeler.importXML(xml)
  } catch (e) {
    // 无 DI 坐标的新模型：自动创建新画布
    try {
      await newBlankDiagram()
      proxy.$modal.msgWarning('原模型缺少图形信息，已创建新画布，保存后即为正常流程图')
    } catch (e2) {
      proxy.$modal.msgError('流程图导入失败：' + e.message)
    }
  }
}

/** 创建新画布：根流程 ID 对齐模型 Key，并默认生成 开始→结束 连线 */
async function newBlankDiagram() {
  await bpmnModeler.createDiagram()
  const canvas = bpmnModeler.get('canvas')
  const root = canvas.getRootElement()
  const modeling = bpmnModeler.get('modeling')
  const elementFactory = bpmnModeler.get('elementFactory')
  modeling.updateProperties(root, {
    id: designModel.value.modelKey,
    name: designModel.value.modelName || designModel.value.modelKey
  })
  // 默认生成 开始 → 结束 及连线
  const start = (root.children || []).find(c => c.type === 'bpmn:StartEvent')
  if (start) {
    modeling.updateProperties(start, { name: '开始' })
    const endAttrs = { name: '结束' }
    const endShape = modeling.createShape(
      elementFactory.createElement('bpmn:EndEvent', endAttrs, root),
      { x: start.x + start.width + 140, y: start.y + start.height / 2 - 18 },
      root
    )
    modeling.connect(start, endShape)
  }
}

function loadSelectedElement(el) {
  selectedEl = el
  const bo = el.businessObject
  const isUserTask = el.type === 'bpmn:UserTask'
  const isSequenceFlow = el.type === 'bpmn:SequenceFlow'
  let condition = ''
  if (isSequenceFlow && bo.conditionExpression) {
    condition = bo.conditionExpression.body || ''
  }
  selectedElement.value = {
    id: bo.id,
    name: bo.name || '',
    type: el.type,
    isUserTask,
    isSequenceFlow,
    sourceIsGateway: !!(el.source && el.source.type && el.source.type.includes('Gateway')),
    condition,
    assignee: (bo.get && bo.get('flowable:assignee')) || bo.assignee || '',
    candidateGroups: (bo.get && bo.get('flowable:candidateGroups')) || bo.candidateGroups || '',
    formKey: (bo.get && bo.get('flowable:formKey')) || bo.formKey || '',
    multiInstance: !!bo.loopCharacteristics
  }
}

function updateElementProp(key, value) {
  if (selectedEl) modeling.updateProperties(selectedEl, { [key]: value })
}

function updateAssignee() {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  modeling.updateModdleProperties(selectedEl, bo, { 'flowable:assignee': selectedElement.value.assignee || undefined })
}

function updateCandidateGroups() {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  modeling.updateModdleProperties(selectedEl, bo, { 'flowable:candidateGroups': selectedElement.value.candidateGroups || undefined })
}

function updateFormKey() {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  modeling.updateModdleProperties(selectedEl, bo, { 'flowable:formKey': selectedElement.value.formKey || undefined })
}

function updateCondition() {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  const expr = (selectedElement.value.condition || '').trim()
  if (expr) {
    const moddle = bpmnModeler.get('moddle')
    const cond = moddle.create('bpmn:FormalExpression', { body: expr })
    modeling.updateProperties(selectedEl, { conditionExpression: cond })
  } else {
    modeling.updateProperties(selectedEl, { conditionExpression: undefined })
  }
}

function clearCondition() {
  selectedElement.value.condition = ''
  updateCondition()
}

// ===== 工具栏命令 =====
function cmdUndo() { bpmnModeler.get('commandStack').undo() }
function cmdRedo() { bpmnModeler.get('commandStack').redo() }
function zoom(delta) { bpmnModeler.get('canvas').zoom(bpmnModeler.get('canvas').zoom() + delta) }
function zoomReset() { bpmnModeler.get('canvas').zoom('fit-viewport', 'auto') }
function deleteSelected() {
  const sel = bpmnModeler.get('selection').get()
  if (!sel.length) { proxy.$modal.msgWarning('请先在画布中选中要删除的节点/连线'); return }
  modeling.removeElements(sel)
}

/** 保存前校验：用户任务办理人配置、结束节点存在性 */
function validateDiagram() {
  const root = bpmnModeler.get('canvas').getRootElement()
  const issues = []
  let hasEnd = false
  ;(root.children || []).forEach(c => {
    if (c.type === 'bpmn:EndEvent') hasEnd = true
    if (c.type === 'bpmn:UserTask') {
      const bo = c.businessObject
      const a = (bo.get && bo.get('flowable:assignee')) || ''
      const g = (bo.get && bo.get('flowable:candidateGroups')) || ''
      if (!a && !g && !bo.loopCharacteristics) {
        issues.push('「' + (bo.name || bo.id) + '」未配置办理人或候选组')
      }
    }
  })
  if (!hasEnd) issues.push('流程缺少结束节点')
  return issues
}

function toggleMultiInstance(val) {
  if (!selectedEl) return
  const bo = selectedEl.businessObject
  if (val) {
    // 开启会签：并行多实例
    const moddle = bpmnModeler.get('moddle')
    const loop = moddle.create('bpmn:MultiInstanceLoopCharacteristics', {
      isSequential: false,
      completionCondition: '${nrOfCompletedInstances >= nrOfInstances}'
    })
    modeling.updateModdleProperties(selectedEl, bo, { loopCharacteristics: loop })
  } else {
    modeling.updateModdleProperties(selectedEl, bo, { loopCharacteristics: undefined })
  }
}

async function handleDesign(row) {
  const res = await getModel(row.modelId)
  designModel.value = res.data
  selectedElement.value = null
  designOpen.value = true
}

async function saveDesigner() {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })
    // 保存前校验
    const issues = validateDiagram()
    if (issues.length) {
      try {
        await ElMessageBox.confirm(
          h => h('div', null, [
            h('p', { style: 'margin-bottom:8px;font-weight:600' }, '存在 ' + issues.length + ' 个问题：'),
            h('ul', { style: 'padding-left:18px;margin:0;color:#e6a23c' }, issues.map(t => h('li', null, t))),
            h('p', { style: 'margin-top:10px;color:#909399;font-size:13px' }, '仍可保存，但该流程部署后可能无法正常发起。')
          ]),
          '保存前校验提醒',
          { confirmButtonText: '仍要保存', cancelButtonText: '返回修改', type: 'warning', draggable: true }
        )
      } catch (e) {
        return // 用户选择返回修改
      }
    }
    await updateModel({
      modelId: designModel.value.modelId,
      modelName: designModel.value.modelName,
      category: designModel.value.category,
      description: designModel.value.description,
      bpmnXml: xml
    })
    proxy.$modal.msgSuccess('流程设计已保存，可点击"部署"发布流程')
    getList()
  } catch (e) {
    proxy.$modal.msgError('保存失败：' + (e.message || e))
  }
}

async function exportXml() {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })
    const blob = new Blob([xml], { type: 'application/xml' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = (designModel.value.modelKey || 'process') + '.bpmn20.xml'
    link.click()
    URL.revokeObjectURL(link.href)
  } catch (e) {
    proxy.$modal.msgError('导出失败')
  }
}

function importLocalXml() { xmlFileRef.value.click() }

async function onXmlFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const text = await file.text()
  try {
    await bpmnModeler.importXML(text)
    proxy.$modal.msgSuccess('XML导入成功，记得点击"保存"')
  } catch (err) {
    proxy.$modal.msgError('XML解析失败：' + err.message)
  }
  e.target.value = ''
}

// ===== 查看流程图 =====
async function handleView(row) {
  const res = await getModel(row.modelId)
  viewModel.value = res.data
  viewOpen.value = true
  await nextTick()
  const { default: Viewer } = await import('bpmn-js/lib/NavigatedViewer.js')
  if (bpmnViewer) { try { bpmnViewer.destroy() } catch (e) {} bpmnViewer = null }
  bpmnViewer = new Viewer({ container: viewerRef.value })
  try {
    await bpmnViewer.importXML(res.data.bpmnXml || emptyXml(res.data.modelKey, res.data.modelName))
    bpmnViewer.get('canvas').zoom('fit-viewport', 'auto')
  } catch (e) {
    proxy.$modal.msgWarning('暂无流程图内容，请先在设计器中绘制')
  }
}

function emptyXml(key, name) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:flowable="http://flowable.org/bpmn" id="Definitions_1" targetNamespace="http://flowable.org/bpmn">
  <bpmn:process id="${key}" name="${name || key}" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始"/>
  </bpmn:process>
</bpmn:definitions>`
}

function handleDeploy(row) {
  proxy.$modal.confirm('确认部署流程模型"' + row.modelName + '"？部署后即可发起该流程').then(() => deployModel(row.modelId)).then(() => {
    proxy.$modal.msgSuccess('部署成功')
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除模型"' + row.modelName + '"？').then(() => delModel(row.modelId)).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

getList()
onActivated(() => { getList() })
</script>

<style lang="scss" scoped>
@use './flow-model.scss';
</style>
