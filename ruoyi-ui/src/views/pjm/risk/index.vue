<template>
  <div class="app-container pjm-list-page pjm-risk-page">
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
          <label>选择项目</label>
          <div class="control">
            <el-input :model-value="selectedProjectLabel" placeholder="点击选择项目" readonly style="cursor:pointer" @click="openProjectPicker">
              <template #append><el-button icon="Search" @click.stop="openProjectPicker" /></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.riskType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in riskTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleStatusTabClick(queryParams.status || 'all')">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.level" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in levelOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工程类别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.engineeringCategory" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in engineeringCategoryOptions" :key="d.value" :label="d.label" :value="d.value" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- 状态标签 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="d in statusOptions" :key="d.value" class="status-tab"
                  :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]"
                  @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pjm:risk:add']">登记</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleEdit()" v-hasPermi="['pjm:risk:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['pjm:risk:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_risk_columns_v2" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="riskList" border
                  @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="ID" prop="riskId" key="riskId" :width="colWidth('riskId', 80)" resizable align="center" v-if="columns.riskId.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.riskId }}</span></template>
          </el-table-column>
          <el-table-column label="类型" prop="riskType" key="riskType" :width="colWidth('riskType', 100)" resizable align="center" v-if="columns.riskType.visible">
            <template #default="scope">
              <span class="badge" :class="riskTypeBadge(scope.row.riskType)">{{ riskTypeLabel(scope.row.riskType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="标题" prop="title" key="title" :min-width="colWidth('title', 220)" resizable show-overflow-tooltip v-if="columns.title.visible" />
          <el-table-column label="所属项目" prop="projectName" key="projectName" :min-width="colWidth('projectName', 180)" resizable show-overflow-tooltip v-if="columns.projectName.visible">
            <template #default="scope">{{ scope.row.projectName || '-' }}</template>
          </el-table-column>
          <!-- ★ 工程类别列（badge 胶囊样式，同"类型"列一致） -->
          <el-table-column label="工程类别" prop="engineeringCategory" key="engineeringCategory" :width="colWidth('engineeringCategory', 140)" resizable align="center" v-if="columns.engineeringCategory?.visible !== false">
            <template #default="scope">
              <span class="badge" :class="engCatBadge(scope.row.engineeringCategory)">{{ engCatLabel(scope.row.engineeringCategory) }}</span>
            </template>
          </el-table-column>
          <!-- ★ 关联标准列 -->
          <el-table-column label="关联标准" prop="riskStdCode" key="riskStdCode" width="280" resizable show-overflow-tooltip v-if="columns.riskStdCode?.visible !== false">
            <template #default="scope">{{ scope.row.riskStdCode || '-' }}</template>
          </el-table-column>
          <el-table-column label="等级" prop="level" key="level" :width="colWidth('level', 100)" resizable align="center" v-if="columns.level.visible">
            <template #default="scope">
              <span class="badge" :class="levelBadge(scope.row.level)">{{ levelLabel(scope.row.level) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 110)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadge(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="负责人" prop="ownerName" key="ownerName" :width="colWidth('ownerName', 110)" resizable align="center" v-if="columns.ownerName.visible">
            <template #default="scope">{{ scope.row.ownerName || '-' }}</template>
          </el-table-column>
          <el-table-column label="识别日期" prop="occurDate" key="occurDate" :width="colWidth('occurDate', 120)" resizable align="center" v-if="columns.occurDate.visible">
            <template #default="scope">{{ scope.row.occurDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="解决日期" prop="resolveDate" key="resolveDate" :width="colWidth('resolveDate', 120)" resizable align="center" v-if="columns.resolveDate.visible">
            <template #default="scope">{{ scope.row.resolveDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['pjm:risk:edit']">处理</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pjm:risk:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改弹框 ===== -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Warning /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="riskRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" type="button">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="所属项目" prop="projectId">
                    <el-input :model-value="formProjectLabel" placeholder="点击选择项目" readonly style="width:100%;cursor:pointer" @click="openFormProjectPicker">
                      <template #append><el-button icon="Search" @click.stop="openFormProjectPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="类型" prop="riskType">
                    <el-select v-model="form.riskType" style="width:100%">
                      <el-option v-for="d in riskTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工程类别">
                    <el-tag v-if="form.engineeringCategory" size="large" type="info" effect="plain" round>
                      {{ engCatLabel(form.engineeringCategory) }}
                    </el-tag>
                    <el-tag v-else size="large" type="info" effect="plain" round class="is-warn">
                      （自动跟随项目）
                    </el-tag>
                    <el-button type="warning" plain icon="CollectionTag" style="margin-left:10px" @click="openStdMatch">从国家标准识别</el-button>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入风险/问题标题" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="关联标准">
                    <el-input v-model="form.riskStdCode" readonly placeholder="识别后自动回填 GB 编号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="等级">
                    <el-select v-model="form.level" style="width:100%">
                      <el-option v-for="d in levelOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态">
                    <el-select v-model="form.status" style="width:100%">
                      <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="负责人">
                    <el-input v-model="form.ownerName" readonly placeholder="请选择负责人" @click="openOwnerPicker">
                      <template #append><el-button icon="Search" @click="openOwnerPicker" /></template>
                      <template #suffix>
                        <el-icon v-if="form.ownerName" style="cursor:pointer" @click.stop="clearOwner"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="识别日期">
                    <el-date-picker v-model="form.occurDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Document /></el-icon></span>描述与措施
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" type="button">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="描述">
                <el-input v-model="form.description" type="textarea" :rows="3" placeholder="风险/问题详细描述" />
              </el-form-item>
              <el-form-item label="应对措施">
                <el-input v-model="form.solution" type="textarea" :rows="3" placeholder="应对措施/解决方案" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ★ 国家标准风险点匹配弹窗 -->
    <el-dialog v-model="stdOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><CollectionTag /></el-icon></div>
          <span class="rd-detail-header-title">国家标准风险点匹配</span>
          <span class="rd-detail-header-divider"></span>
          <span class="rd-detail-header-no" style="display:flex;align-items:center;gap:6px;">
            工程类别：
            <el-select v-model="stdEngineeringCategory" size="small" style="width:140px" @change="reloadStd">
              <el-option v-for="d in engineeringCategoryOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </span>
        </div>
      </template>
      <div class="std-match-toolbar">
        <el-input v-model="stdKeyword" placeholder="按风险点名称 / GB 编号 / 区域搜索" style="width:360px" clearable @keyup.enter="reloadStd" />
        <el-button type="primary" icon="Search" :loading="stdLoading" @click="reloadStd">搜索</el-button>
        <el-button icon="Refresh" :loading="stdLoading" @click="stdKeyword = ''; reloadStd()">全部</el-button>
        <span style="margin-left:auto;color:#909399;font-size:12px;">共 {{ stdList.length }} 条匹配</span>
      </div>
      <el-table v-loading="stdLoading" :data="stdList" border size="small" class="app-table" height="380">
        <el-table-column type="index" label="#" width="46" align="center" />
        <el-table-column label="风险点编码" prop="risk_code" width="120">
          <template #default="{row}"><span class="col-mono">{{ row.risk_code || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="风险点名称" prop="risk_name" min-width="170" show-overflow-tooltip />
        <el-table-column label="区域" prop="area_name" width="120" show-overflow-tooltip />
        <el-table-column label="GB 标准/条款" prop="gb_std" min-width="180" show-overflow-tooltip>
          <template #default="{row}"><span style="color:#e6a23c">{{ row.gb_std || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="风险等级" width="90" align="center">
          <template #default="{row}">
            <el-tag v-if="row.risk_level" :type="({ '1':'info','2':'warning','3':'danger','4':'danger' })[row.risk_level] || 'info'" size="small" effect="light">
              { '1':'一般','2':'较大','3':'重大','4':'特别重大' }[row.risk_level] || row.risk_level
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="控制措施" min-width="200" show-overflow-tooltip>
          <template #default="{row}">
            <el-tooltip placement="top" effect="light">
              <template #content>
                <div style="max-width:420px">
                  <div v-if="row.hazard_desc"><b>风险描述：</b>{{ row.hazard_desc }}</div>
                  <div v-if="row.accident_type"><b>事故类型：</b>{{ row.accident_type }}</div>
                  <div v-if="row.measure_engine"><b>工程技术措施：</b>{{ row.measure_engine }}</div>
                  <div v-if="row.measure_manage"><b>管理措施：</b>{{ row.measure_manage }}</div>
                  <div v-if="row.measure_training"><b>培训教育：</b>{{ row.measure_training }}</div>
                  <div v-if="row.measure_ppe"><b>个体防护：</b>{{ row.measure_ppe }}</div>
                  <div v-if="row.measure_emergency"><b>应急处置：</b>{{ row.measure_emergency }}</div>
                </div>
              </template>
              <span style="cursor:help;color:#409eff;">鼠标悬停查看详情 →</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="applyStdToForm(row)">应用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="stdOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 负责人选择（组织机构） -->
    <UserPicker ref="ownerPickerRef" title="选择风险责任人" @confirm="onOwnerConfirm" />
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
    <ProjectPicker ref="formProjectPickerRef" @confirm="onFormProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmRisk">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose } from '@element-plus/icons-vue'
import UserPicker from '@/components/UserPicker/index.vue'
import ProjectPicker from '@/components/ProjectPicker/index.vue'
import { listRisk, getRisk, addRisk, updateRisk, delRisk, standardMatchRisk } from '@/api/pjm/risk'
import { listProject } from '@/api/pjm/project'
import { fetchAllPages, downloadXlsx } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
// ★ useDict 返回的 toRefs 对象 key 是原始 dictType 名称（pjm_engineering_category），解构 key 必须严格匹配
const { pjm_engineering_category } = proxy.useDict('pjm_engineering_category')
// ★ pjm_engineering_category 是 ref 对象，computed 解包后 v-for 能正确遍历
const engineeringCategoryOptions = computed(() => pjm_engineering_category.value || [])
const engCatLabel = (v) => engineeringCategoryOptions.value.find(x => String(x.value) === String(v))?.label || v || '未选择'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2'])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_risk_index_v2')

const loading = ref(false)
const riskList = ref([])
const total = ref(0)
const projectOptions = ref([])
const open = ref(false)
const title = ref('')
const riskRef = ref()
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const activeStatusTab = ref('all')
const statusCounts = ref({})

const columns = ref({
  riskId: { label: 'ID', visible: true },
  riskType: { label: '类型', visible: true },
  title: { label: '标题', visible: true },
  projectName: { label: '所属项目', visible: true },
  engineeringCategory: { label: '工程类别', visible: true },
  riskStdCode: { label: '关联标准', visible: true },
  level: { label: '等级', visible: true },
  status: { label: '状态', visible: true },
  ownerName: { label: '负责人', visible: true },
  occurDate: { label: '识别日期', visible: true },
  resolveDate: { label: '解决日期', visible: true }
})

const riskTypeOptions = [
  { value: '1', label: '安全' }, { value: '2', label: '质量' },
  { value: '3', label: '进度' }, { value: '4', label: '成本' },
  { value: '5', label: '环保' }, { value: '6', label: '合同' },
  { value: '7', label: '外部协调' }
]
const riskTypeLabel = (v) => riskTypeOptions.find(x => x.value === v)?.label || '未分类'
const riskTypeBadge = (v) => ({ '1': 'red', '2': 'amber', '3': 'blue', '4': 'violet', '5': 'green', '6': 'gray', '7': 'gray' }[v] || 'gray')

// ★ 工程类别 badge 颜色映射（11 类各分配一个协调色，同 riskTypeBadge 风格）
const engCatBadge = (v) => ({
  '1': 'blue',     // 建筑工程 蓝
  '2': 'green',    // 市政公用 绿
  '3': 'amber',    // 电力 琥珀
  '4': 'red',      // 石油化工 红（高危）
  '5': 'violet',   // 机电 紫
  '6': 'cyan',     // 水利水电 青
  '7': 'orange',   // 矿山 橙
  '8': 'indigo',   // 铁路 靛
  '9': 'teal',     // 公路 蓝绿
  '10': 'pink',    // 民航 粉
  '11': 'gray'     // 通信 灰
}[String(v)] || 'gray')

const statusOptions = [
  { value: '0', label: '开放' }, { value: '1', label: '处理中' },
  { value: '2', label: '已解决' }, { value: '3', label: '已关闭' }
]
const levelOptions = [
  { value: '1', label: '低' }, { value: '2', label: '中' },
  { value: '3', label: '高' }, { value: '4', label: '紧急' }
]

const queryParams = reactive({ pageNum: 1, pageSize: 10, projectId: null, riskType: null, status: null, level: null, engineeringCategory: null })
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
const form = ref({})
// 表单内项目浏览框
const formProjectPickerRef = ref()
const formProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(form.value.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openFormProjectPicker() { formProjectPickerRef.value?.open(form.value.projectId) }
function onFormProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  form.value.projectId = p.projectId
}
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  riskType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

/** 负责人选择（组织机构） */
const ownerPickerRef = ref()
function openOwnerPicker() { ownerPickerRef.value.open(form.value.ownerId) }
function onOwnerConfirm(u) {
  form.value.ownerId = u.userId
  form.value.ownerName = u.nickName
}
function clearOwner() {
  form.value.ownerId = null
  form.value.ownerName = null
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.projectId) count++
  if (queryParams.riskType) count++
  if (queryParams.status) count++
  if (queryParams.level) count++
  return count
})

const levelLabel = (v) => levelOptions.find(x => x.value === v)?.label || v
const levelBadge = (v) => ({ '1': 'gray', '2': 'blue', '3': 'amber', '4': 'red' }[v] || 'gray')
const statusLabel = (v) => statusOptions.find(x => x.value === v)?.label || v
const statusBadge = (v) => ({ '0': 'red', '1': 'amber', '2': 'green', '3': 'gray' }[v] || 'gray')
const statusTabClass = (v) => ({ '0': 'tab-reject', '1': 'tab-draft', '2': 'tab-approved', '3': 'tab-void' }[v] || '')

function loadProjects() {
  listProject({ pageNum: 1, pageSize: 200 }).then(res => { projectOptions.value = res.rows || [] })
}

function getList() {
  loading.value = true
  listRisk(queryParams).then(res => {
    riskList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

/** 状态标签数字：按当前项目/类型条件统计（不含状态本身） */
function loadStatusCounts() {
  listRisk({ ...queryParams, status: undefined, pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    statusOptions.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
  loadStatusCounts()
}

function resetQuery() {
  queryParams.projectId = null
  queryParams.riskType = null
  queryParams.status = null
  queryParams.level = null
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.status = status === 'all' ? null : status
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.riskId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = { riskId: null, projectId: queryParams.projectId, taskId: null, riskType: '1', engineeringCategory: null, riskPointId: null, riskStdCode: null, title: null, level: '2', status: '0', ownerId: null, ownerName: null, occurDate: null, resolveDate: null, solution: null, description: null }
  // ★ 带出所选项目的工程类别（后端已在 pjm_project 上有 engineering_category 字段）
  if (form.value.projectId) {
    const hit = projectOptions.value.find(p => p.projectId === form.value.projectId)
    if (hit?.engineeringCategory) form.value.engineeringCategory = String(hit.engineeringCategory)
  }
  // ★ 兜底：确保工程类别始终是 string（后端 JSON 可能序列化 number）
  if (form.value.engineeringCategory != null && form.value.engineeringCategory !== '') {
    form.value.engineeringCategory = String(form.value.engineeringCategory)
  }
}

// ============ ★ 国家标准风险点匹配 ============
const stdOpen = ref(false)
const stdLoading = ref(false)
const stdKeyword = ref('')
const stdEngineeringCategory = ref('')
const stdList = ref([])

/** ★ 选项目后自动带出项目的工程类别（仅在工程类别未显式填过时带出，避免覆盖用户手动选择） */
watch(() => form.value.projectId, (newPid, oldPid) => {
  if (newPid == null || newPid === oldPid) return
  const hit = projectOptions.value.find(p => p.projectId === newPid)
  if (hit?.engineeringCategory && !form.value.engineeringCategory) {
    // ★ 后端返回 number 类型，前端 dict :value 是 string，显式转 string 避免 el-select 严格等于失效
    form.value.engineeringCategory = String(hit.engineeringCategory)
  }
})

// ★ 弹窗里选工程类别时，同步回主表单（双向联动）
watch(stdEngineeringCategory, (v) => {
  if (v && v !== form.value.engineeringCategory) {
    form.value.engineeringCategory = String(v)
  }
})

// ★ 工程类别由所选项目自动带出，不再手动维护

function openStdMatch() {
  stdKeyword.value = ''
  stdEngineeringCategory.value = form.value.engineeringCategory || ''
  stdOpen.value = true
  reloadStd()
}
function reloadStd() {
  if (!stdEngineeringCategory.value) {
    ElMessage.warning('请先选择工程类别')
    stdList.value = []
    return
  }
  stdLoading.value = true
  standardMatchRisk(stdEngineeringCategory.value, stdKeyword.value || null).then(res => {
    stdList.value = res.data || []
  }).finally(() => { stdLoading.value = false })
}
function applyStdToForm(row) {
  // ★ 同步工程类别到表单（若表单为空时把弹窗选择的类别带过去）
  if (!form.value.engineeringCategory && stdEngineeringCategory.value) {
    form.value.engineeringCategory = String(stdEngineeringCategory.value)
  }
  // 回填到表单：风险点ID、标准条款、标题（如空则自动填）、等级（如空则根据风险点风险等级转换）、描述/应对措施（如空则自动合并控制措施）
  form.value.riskPointId = row.risk_point_id
  form.value.riskStdCode = row.gb_std
  if (!form.value.title) {
    form.value.title = row.risk_name
  }
  if (!form.value.description && row.hazard_desc) {
    form.value.description = row.hazard_desc
  }
  // 从 4 级风险点等级映射到 4 级风险等级（大→低）
  if (!form.value.level && row.risk_level) {
    const m = { '1': '2', '2': '3', '3': '4', '4': '4' }
    form.value.level = m[row.risk_level] || '3'
  }
  // 合并工程技术+管理+培训+应急等措施作为应对措施（如原应对措施为空时自动填入）
  if (!form.value.solution) {
    const parts = []
    if (row.measure_engine) parts.push('工程技术措施：' + row.measure_engine)
    if (row.measure_manage) parts.push('管理措施：' + row.measure_manage)
    if (row.measure_training) parts.push('培训教育：' + row.measure_training)
    if (row.measure_ppe) parts.push('个体防护：' + row.measure_ppe)
    if (row.measure_emergency) parts.push('应急处置：' + row.measure_emergency)
    if (parts.length) form.value.solution = parts.join('\n')
  }
  ElMessage.success(`已应用：${row.risk_name}（${row.gb_std || '无标准条款'}）`)
  stdOpen.value = false
}

function handleAdd() {
  reset()
  title.value = '登记风险/问题'
  open.value = true
}

function handleEdit(row) {
  const riskId = row?.riskId || ids.value[0]
  getRisk(riskId).then(res => {
    form.value = res.data
    // ★ 工程类别后端可能返回 number（JSON 序列化），dict :value 是 string，必须 String() 强制转换
    if (form.value.engineeringCategory != null && form.value.engineeringCategory !== '') {
      form.value.engineeringCategory = String(form.value.engineeringCategory)
    } else if (form.value.projectId) {
      // 老记录没工程类别，从所属项目自动补带
      const hit = projectOptions.value.find(p => p.projectId === form.value.projectId)
      if (hit?.engineeringCategory) form.value.engineeringCategory = String(hit.engineeringCategory)
    }
    title.value = '处理风险/问题'
    open.value = true
  })
}

function submitForm() {
  riskRef.value.validate(valid => {
    if (!valid) return
    const action = form.value.riskId ? updateRisk(form.value) : addRisk(form.value)
    action.then(() => {
      ElMessage.success(form.value.riskId ? '修改成功' : '登记成功')
      open.value = false
      getList()
      loadStatusCounts()
    })
  })
}

function handleDelete(row) {
  const riskIds = row?.riskId ? [row.riskId] : ids.value
  ElMessageBox.confirm('确认删除该条记录吗？', '提示', { type: 'warning' }).then(() => {
    delRisk(riskIds.join(',')).then(() => {
      ElMessage.success('删除成功')
      getList()
      loadStatusCounts()
    })
  }).catch(() => {})
}

/** 导出（前端导出，条件与列表一致） */
async function handleExport() {
  const rows = await fetchAllPages(listRisk, { ...queryParams })
  if (!rows.length) {
    ElMessage.warning('当前筛选下无数据可导出')
    return
  }
  const headers = ['ID', '类型', '标题', '所属项目', '等级', '状态', '负责人', '识别日期', '解决日期']
  downloadXlsx(`pjm_risk_${new Date().getTime()}`, headers, rows.map(r => [
    r.riskId || '', riskTypeLabel(r.riskType), r.title || '', r.projectName || '',
    levelLabel(r.level), statusLabel(r.status), r.ownerName || '', r.occurDate || '', r.resolveDate || ''
  ]))
}

onMounted(() => {
  loadProjects()
  getList()
  loadStatusCounts()
})
</script>
