<template>
  <div class="app-container qms-aqlplan-page">
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
          <label>AQL等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.aqlLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="v in aqlLevels" :key="v" :label="v" :value="v" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>字码</label>
          <div class="control">
            <el-input v-model="queryParams.codeLetter" placeholder="如G/H/J" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>检验严格度</label>
          <div class="control is-select">
            <el-select v-model="queryParams.inspectLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="1" />
              <el-option label="加严" value="2" />
              <el-option label="放宽" value="3" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
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
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:aqlplan:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <button type="button" class="tip-pill" @click="showStatusHelp = true">
            <el-icon><QuestionFilled /></el-icon>
            <span>业务操作说明</span>
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_aqlplan_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="planList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="方案ID" prop="planId" key="planId" :width="colWidth('planId', 80)" resizable align="center" v-if="columns.planId.visible" />          <el-table-column label="AQL等级" prop="aqlLevel" key="aqlLevel" :width="colWidth('aqlLevel', 100)" resizable align="center" v-if="columns.aqlLevel.visible" />
          <el-table-column label="字码" prop="codeLetter" key="codeLetter" :width="colWidth('codeLetter', 80)" resizable align="center" v-if="columns.codeLetter.visible" />
          <el-table-column label="样本量(n)" prop="sampleSize" key="sampleSize" :width="colWidth('sampleSize', 100)" resizable align="center" v-if="columns.sampleSize.visible" />
          <el-table-column label="接收数(Ac)" prop="acVal" key="acVal" :width="colWidth('acVal', 100)" resizable align="center" v-if="columns.acVal.visible" />
          <el-table-column label="拒收数(Re)" prop="reVal" key="reVal" :width="colWidth('reVal', 100)" resizable align="center" v-if="columns.reVal.visible" />
          <el-table-column label="检验严格度" prop="inspectLevel" key="inspectLevel" :width="colWidth('inspectLevel', 110)" resizable align="center" v-if="columns.inspectLevel.visible">
            <template #default="scope">
              <span class="badge" :class="inspectLevelBadgeClass(scope.row.inspectLevel)">
                <span class="dot"></span>{{ inspectLevelLabel(scope.row.inspectLevel) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="批量范围" prop="batchRange" key="batchRange" :width="colWidth('batchRange', 140)" resizable align="center" v-if="columns.batchRange.visible">
            <template #default="scope">
              {{ scope.row.batchMin || 0 }} ~ {{ scope.row.batchMax || '∞' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 80)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== View Dialog ===== -->
    <el-dialog v-model="viewOpen" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">抽样方案详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.planId">
              <span class="rd-detail-header-no">方案ID：{{ viewData.planId }}</span>
              <span class="badge" :class="viewData.status === '0' ? 'green' : 'gray'">
                <span class="dot"></span>{{ viewData.status === '0' ? '正常' : '停用' }}
              </span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/></svg>
              </span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">方案ID</span>
                <div class="rd-value">{{ viewData.planId || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">AQL等级</span>
                <div class="rd-value">{{ viewData.aqlLevel || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">字码</span>
                <div class="rd-value">{{ viewData.codeLetter || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">检验严格度</span>
                <div class="rd-value">
                  <span class="badge" :class="inspectLevelBadgeClass(viewData.inspectLevel)">
                    <span class="dot"></span>{{ inspectLevelLabel(viewData.inspectLevel) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_sample')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
              </span>
              抽样参数
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_sample }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_sample" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">样本量(n)</span>
                <div class="rd-value">{{ viewData.sampleSize != null ? viewData.sampleSize : '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">接收数(Ac)</span>
                <div class="rd-value">{{ viewData.acVal != null ? viewData.acVal : '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">拒收数(Re)</span>
                <div class="rd-value">{{ viewData.reVal != null ? viewData.reVal : '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">批量范围</span>
                <div class="rd-value">{{ viewData.batchMin || 0 }} ~ {{ viewData.batchMax || '∞' }}</div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('v_remark')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
              </span>
              备注
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_remark }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_remark" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full">
                <span class="rd-label">备注</span>
                <div class="rd-value">{{ viewData.remark || '-' }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="抽样方案业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、标准来源 -->
        <h4>一、标准来源</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">标准来源说明</div>
          <div class="highlight-card-body">
            <p>• <strong>国家标准：</strong>本抽样方案数据来源于国家标准 <strong>GB/T 2828.1-2012《计数抽样检验程序 第1部分：按接收质量限(AQL)检索的逐批检验抽样计划》</strong>，该标准等同采用国际标准 ISO 2859-1:1999。</p>
            <p>• <strong>适用范围：</strong>适用于连续批的计数抽样检验，通过可接收质量限（AQL）和批量大小确定抽样方案，广泛用于进料检验、过程检验和成品检验等质量控制环节。</p>
            <p>• <strong>数据维护：</strong>抽样方案为系统基础主数据，依据国标预先录入并维护，业务模块通过引用AQL等级和检验严格度自动匹配对应的抽样参数。</p>
          </div>
        </div>

        <!-- 二、核心概念说明 -->
        <h4>二、核心概念说明</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心概念</div>
          <div class="highlight-card-body">
            <p>• <strong>AQL（接收质量限）：</strong>可接受的最高过程质量水平，常用值有 0.65、1.0、1.5、2.5 等。AQL 值越小，质量要求越严格。</p>
            <p>• <strong>字码（样本量字码）：</strong>根据批量大小和检验水平（特殊水平 S-1~S-4 或一般水平 I~III）查表确定，用字母 A~R 表示，对应不同的样本量。</p>
            <p>• <strong>样本量(n)：</strong>从检验批中抽取的样本数量，由字码查表确定。</p>
            <p>• <strong>接收数(Ac)：</strong>样本中允许的最大不合格品数，不合格品数 ≤ Ac 时判该批合格予以接收。</p>
            <p>• <strong>拒收数(Re)：</strong>样本中判该批不合格的最小不合格品数，不合格品数 ≥ Re 时判该批不合格予以拒收。通常 Re = Ac + 1。</p>
            <p>• <strong>检验严格度：</strong>分为正常、加严、放宽三种。正常检验为默认状态；连续检验不合格时转为加严；连续检验合格时转为放宽。</p>
            <p>• <strong>批量范围：</strong>每条方案适用的批量区间（下限~上限），系统根据实际批量自动匹配对应的抽样方案。</p>
          </div>
        </div>

        <!-- 三、检验判定流程 -->
        <h4>三、检验判定流程</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">确定批量N</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">查字码</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">查抽样方案(n, Ac, Re)</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag>抽取n个样本检验</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">d≤Ac 接收</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">d≥Re 拒收</el-tag>
          </div>
        </div>

        <!-- 四、使用说明 -->
        <h4>四、使用说明</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">使用说明</div>
          <div class="highlight-card-body">
            <p>• <strong>只读模式：</strong>抽样方案为标准基础数据，不支持新增、修改和删除操作，仅支持查看和导出。</p>
            <p>• <strong>数据引用：</strong>检验任务、进货检验等业务模块在创建时选择AQL等级，系统自动根据批量匹配抽样方案并计算样本量和判定标准。</p>
            <p>• <strong>严格度转换规则：</strong>正常检验时连续5批中有2批被拒收 → 转为加严；加严检验时连续5批均被接收 → 恢复正常；正常检验时连续10批均被接收且生产稳定 → 转为放宽；放宽检验时有1批被拒收 → 恢复正常。</p>
            <p>• <strong>状态说明：</strong>「正常」状态的方案可被业务模块引用；「停用」状态的方案不再被新业务引用，但已关联的历史记录不受影响。</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsAqlPlan">
import { listAqlPlan, getAqlPlan } from '@/api/qms/aqlplan'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_aqlplan_index')
const { collapsedCards, toggleCard } = useDetailCard(['v_basic', 'v_sample', 'v_remark'])

const planList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const ids = ref([])
const total = ref(0)
const aqlLevels = ['0.25', '0.40', '0.65', '1.0', '1.5', '2.5', '4.0', '6.5', '10.0']

const queryParams = ref({ pageNum: 1, pageSize: 10, aqlLevel: undefined, codeLetter: undefined, inspectLevel: undefined, status: undefined })

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  planId: { label: '方案ID', visible: true },
  aqlLevel: { label: 'AQL等级', visible: true },
  codeLetter: { label: '字码', visible: true },
  sampleSize: { label: '样本量(n)', visible: true },
  acVal: { label: '接收数(Ac)', visible: true },
  reVal: { label: '拒收数(Re)', visible: true },
  inspectLevel: { label: '检验严格度', visible: true },
  batchRange: { label: '批量范围', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('qms_aqlplan_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.aqlLevel) count++
  if (queryParams.value.codeLetter) count++
  if (queryParams.value.inspectLevel) count++
  if (queryParams.value.status) count++
  return count
})

function inspectLevelLabel(val) {
  const map = { '1': '正常', '2': '加严', '3': '放宽' }
  return map[val] || '-'
}

function inspectLevelBadgeClass(val) {
  const map = { '1': 'blue', '2': 'red', '3': 'green' }
  return map[val] || 'gray'
}

function getList() {
  loading.value = true
  listAqlPlan(queryParams.value).then(res => {
    planList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.aqlLevel = undefined
  queryParams.value.codeLetter = undefined
  queryParams.value.inspectLevel = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.planId)
}

function handleView(row) {
  getAqlPlan(row.planId).then(res => {
    viewData.value = res.data
    viewOpen.value = true
  })
}

function handleExport() {
  proxy.download('qms/aqlplan/export', { ...queryParams.value }, `aqlplan_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.qms-aqlplan-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-aqlplan-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-aqlplan-page .filter-card { padding:14px 20px 16px; }
.qms-aqlplan-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-aqlplan-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-aqlplan-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-aqlplan-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-aqlplan-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-aqlplan-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-aqlplan-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-aqlplan-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-aqlplan-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-aqlplan-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-aqlplan-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-aqlplan-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-aqlplan-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-aqlplan-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-aqlplan-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-aqlplan-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-aqlplan-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-aqlplan-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-aqlplan-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-aqlplan-page .field .control :deep(.el-select) { width:100%; }
.qms-aqlplan-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-aqlplan-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-aqlplan-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-aqlplan-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-aqlplan-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-aqlplan-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-aqlplan-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-aqlplan-page .btn-soft .el-icon { font-size:14px; }
.qms-aqlplan-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-aqlplan-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-aqlplan-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-aqlplan-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-aqlplan-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
/* ===== Tip Pill ===== */
.qms-aqlplan-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.qms-aqlplan-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
.qms-aqlplan-page .table-wrap { overflow-x:auto; }
.qms-aqlplan-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-aqlplan-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-aqlplan-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-aqlplan-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-aqlplan-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-aqlplan-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-aqlplan-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-aqlplan-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-aqlplan-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-aqlplan-page .badge.green .dot { background:var(--green-500); }
.qms-aqlplan-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-aqlplan-page .badge.gray .dot { background:var(--ink-400); }
.qms-aqlplan-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.qms-aqlplan-page .badge.blue .dot { background:var(--blue-500); }
.qms-aqlplan-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-aqlplan-page .badge.red .dot { background:var(--red-500); }
.qms-aqlplan-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }

/* ===== View Dialog (detail-page-style-guide) ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 14px; right: 14px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.rd-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.rd-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width: none; }

.rd-detail-header { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -30px; right: -15px; width: 140px; height: 140px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; border-radius: 10px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 4px 12px rgb(0 0 0 / 0.1); }
.rd-detail-header-main { display: flex; flex-direction: column; gap: 4px; min-width: 0; padding-right: 36px; }
.rd-detail-header-title { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: -0.02em; line-height: 1.3; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.rd-detail-header-no { font-size: 13px; font-weight: 500; color: rgb(255 255 255 / 0.8); font-variant-numeric: tabular-nums; }

.rd-page { max-width: 840px; margin: 0 auto; }
.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-header .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; border-radius: 6px; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-card-header .rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-card-header .rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-card-header .rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-body { padding: 14px 16px; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }

@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }

/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height: 520px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.status-help-content .flow-item { display: flex; align-items: center; gap: 8px; }
.status-help-content .flow-arrow { color: #909399; font-size: 16px; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-card-body p { margin: 4px 0; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }

/* ===== 响应式 ===== */
@media (max-width:1100px) { .qms-aqlplan-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:768px) { .rd-grid { grid-template-columns: 1fr; } }
@media (max-width:720px) { .qms-aqlplan-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-aqlplan-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
