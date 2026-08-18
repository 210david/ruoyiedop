<template>
  <div class="app-container mms-factory-page">
    <el-row :gutter="12">
      <!-- 左侧树：工厂/车间/产线层级 -->
      <el-col :span="5" :xs="24">
        <div class="surface tree-card">
          <div class="tree-head">
            <span class="filter-title"><span class="glyph"></span> 组织层级</span>
          </div>
          <div class="tree-search">
            <el-input v-model="deptName" placeholder="搜索名称" clearable prefix-icon="Search" />
          </div>
          <div class="tree-body">
            <el-tree :data="treeData" :props="{ label: 'label', children: 'children' }" :expand-on-click-node="false" default-expand-all ref="deptTreeRef" highlight-current @node-click="handleNodeClick" />
          </div>
        </div>
      </el-col>

      <!-- 右侧表格区 -->
      <el-col :span="19" :xs="24">
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
              <label>编码</label>
              <div class="control">
                <el-input v-model="queryParams.code" placeholder="请输入编码" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>名称</label>
              <div class="control">
                <el-input v-model="queryParams.name" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
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
            <div class="field">
              <label>当前层级</label>
              <div class="control">
                <el-input :model-value="currentLevel" disabled />
              </div>
            </div>
            <div class="field" v-show="showAdvanced">
              <label>创建日期</label>
              <div class="control">
                <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info">
              <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，当前层级：{{ currentLevel }}
            </div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- ===== Table Section ===== -->
        <div class="surface">
          <!-- Status Tabs + Tip -->
          <div class="status-tabs">
            <div class="tabs-track">
              <span class="status-tab is-active"><span class="dot"></span><span>{{ currentLevel }}管理</span></span>
            </div>
            <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
          </div>
          <!-- Toolbar -->
          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:factory:add']">新增</el-button>
              <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:factory:edit']">修改</el-button>
              <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:factory:remove']">删除</el-button>
              <div class="toolbar-divider"></div>
              <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:factory:export']">导出</el-button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_factory_columns" />
            </div>
          </div>

          <!-- Table -->
          <div class="table-wrap">
            <el-table ref="tableRef" v-loading="loading" :data="dataList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column :label="currentLevel + '编码'" prop="code" key="code" :width="colWidth('code', 140)" resizable v-if="columns.code.visible" />
              <el-table-column :label="currentLevel + '名称'" prop="name" key="name" :width="colWidth('name', 180)" resizable show-overflow-tooltip v-if="columns.name.visible" />
              <el-table-column v-if="currentLevel === '车间' && columns.factoryName.visible" label="所属工厂" prop="factoryName" key="factoryName" :width="colWidth('factoryName', 140)" resizable align="center" />
              <el-table-column v-if="currentLevel === '产线' && columns.workshopName.visible" label="所属车间" prop="workshopName" key="workshopName" :width="colWidth('workshopName', 140)" resizable align="center" />
              <el-table-column v-if="currentLevel === '产线' && columns.lineType.visible" label="产线类型" prop="lineType" key="lineType" :width="colWidth('lineType', 100)" resizable align="center">
                <template #default="scope">
                  <span class="badge" :class="lineTypeBadge(scope.row.lineType).cls">
                    <span class="dot"></span>{{ lineTypeBadge(scope.row.lineType).label }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column v-if="currentLevel === '产线' && columns.cycleTime.visible" label="节拍(秒)" prop="cycleTime" key="cycleTime" :width="colWidth('cycleTime', 100)" resizable align="center" />
              <el-table-column v-if="currentLevel === '工位' && columns.lineName.visible" label="所属产线" prop="lineName" key="lineName" :width="colWidth('lineName', 140)" resizable align="center" />
              <el-table-column v-if="currentLevel === '工位' && columns.stationType.visible" label="工位类型" prop="stationType" key="stationType" :width="colWidth('stationType', 100)" resizable align="center">
                <template #default="scope">
                  <span class="badge" :class="stationTypeBadge(scope.row.stationType).cls">
                    <span class="dot"></span>{{ stationTypeBadge(scope.row.stationType).label }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column v-if="currentLevel === '工厂' && columns.address.visible" label="地址" prop="address" key="address" :width="colWidth('address', 200)" resizable show-overflow-tooltip />
              <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                    <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
                <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
              </el-table-column>
              <el-table-column label="操作" width="180" align="center" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                  <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:factory:edit']">修改</el-button>
                  <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:factory:remove']">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
        </div>
      </el-col>
    </el-row>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="dialog.visible" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></div>
          <span class="rd-detail-header-title">{{ dialog.title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- ===== 卡片一：基本信息 ===== -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_basic')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
                基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e_basic">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="编码" prop="code">
                    <el-input v-model="form.code" placeholder="请输入编码" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入名称" />
                  </el-form-item>
                </el-col>
              </el-row>
              <!-- 工厂特有：地址 -->
              <el-form-item v-if="currentLevel === '工厂'" label="地址" prop="address">
                <el-input v-model="form.address" placeholder="请输入地址" />
              </el-form-item>
              <!-- 车间特有：所属工厂 -->
              <el-form-item v-if="currentLevel === '车间'" label="所属工厂" prop="factoryId">
                <el-select v-model="form.factoryId" filterable placeholder="请选择工厂" style="width: 100%">
                  <el-option v-for="item in factoryOptions" :key="item.factoryId" :label="item.factoryName" :value="item.factoryId" />
                </el-select>
              </el-form-item>
              <!-- 产线特有：所属车间 + 产线类型 + 节拍 -->
              <el-row :gutter="20" v-if="currentLevel === '产线'">
                <el-col :span="12">
                  <el-form-item label="所属车间" prop="workshopId">
                    <el-select v-model="form.workshopId" filterable placeholder="请选择车间" style="width: 100%">
                      <el-option v-for="item in workshopOptions" :key="item.workshopId" :label="item.workshopName" :value="item.workshopId" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="产线类型" prop="lineType">
                    <el-select v-model="form.lineType" placeholder="请选择" style="width: 100%">
                      <el-option label="自动化" value="0" />
                      <el-option label="半自动" value="1" />
                      <el-option label="手工" value="2" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item v-if="currentLevel === '产线'" label="节拍(秒)" prop="cycleTime">
                <el-input-number v-model="form.cycleTime" :min="0" controls-position="right" />
              </el-form-item>
              <!-- 工位特有：所属产线 + 工位类型 -->
              <el-row :gutter="20" v-if="currentLevel === '工位'">
                <el-col :span="12">
                  <el-form-item label="所属产线" prop="lineId">
                    <el-select v-model="form.lineId" filterable placeholder="请选择产线" style="width: 100%">
                      <el-option v-for="item in lineOptions" :key="item.lineId" :label="item.lineName" :value="item.lineId" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工位类型" prop="stationType">
                    <el-select v-model="form.stationType" placeholder="请选择" style="width: 100%">
                      <el-option label="加工" value="0" />
                      <el-option label="检验" value="1" />
                      <el-option label="包装" value="2" />
                      <el-option label="暂存" value="3" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <!-- ===== 卡片二：其他信息 ===== -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_other')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e_other">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio value="0">正常</el-radio>
                  <el-radio value="1">停用</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="3" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewOpen" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></div>
          <div class="rd-detail-header-main">
            <span class="rd-detail-header-title">{{ currentLevel }}详情</span>
            <div class="rd-detail-header-sub" v-if="viewData.code">
              <span class="rd-detail-header-divider"></span>
              <span class="rd-detail-header-no">编码：{{ viewData.code }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- ===== 卡片一：基本信息 ===== -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">编码</span><div class="rd-value">{{ viewData.code || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">名称</span><div class="rd-value">{{ viewData.name || '-' }}</div></div>
              <!-- 工厂特有：地址 -->
              <div class="rd-item rd-item--full" v-if="currentLevel === '工厂'"><span class="rd-label">地址</span><div class="rd-value">{{ viewData.address || '-' }}</div></div>
              <!-- 车间特有：所属工厂 -->
              <div class="rd-item" v-if="currentLevel === '车间'"><span class="rd-label">所属工厂</span><div class="rd-value">{{ viewData.factoryName || '-' }}</div></div>
              <!-- 产线特有：所属车间 + 产线类型 -->
              <div class="rd-item" v-if="currentLevel === '产线'"><span class="rd-label">所属车间</span><div class="rd-value">{{ viewData.workshopName || '-' }}</div></div>
              <div class="rd-item" v-if="currentLevel === '产线'"><span class="rd-label">产线类型</span><div class="rd-value">{{ lineTypeBadge(viewData.lineType).label }}</div></div>
              <div class="rd-item" v-if="currentLevel === '产线'"><span class="rd-label">节拍(秒)</span><div class="rd-value">{{ viewData.cycleTime ?? '-' }}</div></div>
              <!-- 工位特有：所属产线 + 工位类型 -->
              <div class="rd-item" v-if="currentLevel === '工位'"><span class="rd-label">所属产线</span><div class="rd-value">{{ viewData.lineName || '-' }}</div></div>
              <div class="rd-item" v-if="currentLevel === '工位'"><span class="rd-label">工位类型</span><div class="rd-value">{{ stationTypeBadge(viewData.stationType).label }}</div></div>
            </div>
          </div>
        </section>

        <!-- ===== 卡片二：其他信息 ===== -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_other')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>
              其他信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_other">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ viewData.status === '0' ? '正常' : '停用' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.remark"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="工厂组织管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、工厂组织管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是工厂组织管理？</div>
          <div class="highlight-card-body">
            <strong>工厂组织管理（Factory Organization）</strong>是生产管控中定义工厂层级组织结构的主数据。支持多级工厂组织（公司→工厂→车间→产线→工序），是工单排产、产能规划和成本核算的基础依据。<br/><br/>
            工厂组织遵循 <strong>MES 组织架构管理规范</strong>，通过层级结构实现生产资源的归属管理，支持按车间/产线维度的产能统计和效率分析。
          </div>
        </div>
        <h4>二、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>创建组织节点：</strong>点击「新增」创建工厂/车间/产线等组织节点</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>层级管理：</strong>支持多级层级，通过切换层级查看不同层级的组织节点</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>资源关联：</strong>将产能资源和工序关联到产线/车间，支持排产和成本核算</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Factory">
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { listFactory, addFactory, updateFactory, delFactory, listWorkshop, addWorkshop, updateWorkshop, delWorkshop, listLine, addLine, updateLine, delLine, listStation, addStation, updateStation, delStation } from '@/api/mms/factory'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_factory_index')
const { collapsedCards, toggleCard } = useDetailCard(['v_basic', 'v_other', 'e_basic', 'e_other'])

const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref([])
const deptName = ref('')
const currentLevel = ref('工厂')
const treeData = ref([])
const factoryOptions = ref([])
const workshopOptions = ref([])
const lineOptions = ref([])

const dateRange = ref([])
const queryParams = reactive({ pageNum: 1, pageSize: 10, code: '', name: '', status: undefined, params: {} })
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const viewOpen = ref(false)
const showStatusHelp = ref(false)
const viewData = ref({})
const rules = {
  code: [{ required: true, message: '编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
}

// 当前选中的层级类型和父ID
const currentType = ref('factory')
const currentParentId = ref(null)

// ===== Column Visibility =====
const defaultColumns = {
  code: { label: '编码', visible: true },
  name: { label: '名称', visible: true },
  factoryName: { label: '所属工厂', visible: true },
  workshopName: { label: '所属车间', visible: true },
  lineName: { label: '所属产线', visible: true },
  lineType: { label: '产线类型', visible: true },
  cycleTime: { label: '节拍', visible: true },
  stationType: { label: '工位类型', visible: true },
  address: { label: '地址', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_factory_columns')
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

const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.code) count++
  if (queryParams.name) count++
  if (queryParams.status !== undefined && queryParams.status !== '') count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

// ===== Badge helpers =====
function lineTypeBadge(type) {
  const map = {
    '0': { cls: 'blue', label: '自动化' },
    '1': { cls: 'amber', label: '半自动' },
    '2': { cls: 'gray', label: '手工' }
  }
  return map[type] || { cls: 'gray', label: '-' }
}

function stationTypeBadge(type) {
  const map = {
    '0': { cls: 'blue', label: '加工' },
    '1': { cls: 'amber', label: '检验' },
    '2': { cls: 'green', label: '包装' },
    '3': { cls: 'gray', label: '暂存' }
  }
  return map[type] || { cls: 'gray', label: '-' }
}

// 加载树
async function loadTree() {
  const res = await listFactory({})
  factoryOptions.value = res.rows || []
  treeData.value = (res.rows || []).map(f => ({
    label: f.factoryName,
    id: f.factoryId,
    type: 'factory',
    children: []
  }))
}

// 节点点击
function handleNodeClick(data) {
  if (data.type === 'factory') {
    currentLevel.value = '车间'
    currentType.value = 'workshop'
    currentParentId.value = { factoryId: data.id }
    queryParams.factoryId = data.id
    getList()
  } else if (data.type === 'workshop') {
    currentLevel.value = '产线'
    currentType.value = 'line'
    currentParentId.value = { workshopId: data.id }
    queryParams.workshopId = data.id
    getList()
  } else if (data.type === 'line') {
    currentLevel.value = '工位'
    currentType.value = 'station'
    currentParentId.value = { lineId: data.id }
    queryParams.lineId = data.id
    getList()
  } else {
    currentLevel.value = '工厂'
    currentType.value = 'factory'
    currentParentId.value = null
    delete queryParams.factoryId
    delete queryParams.workshopId
    delete queryParams.lineId
    getList()
  }
}

// 查询列表
async function getList() {
  loading.value = true
  try {
    let res
    const queryData = proxy.addDateRange(queryParams, dateRange.value)
    if (currentType.value === 'factory') {
      res = await listFactory(queryData)
    } else if (currentType.value === 'workshop') {
      res = await listWorkshop(queryData)
    } else if (currentType.value === 'line') {
      res = await listLine(queryData)
    } else {
      res = await listStation(queryData)
    }
    dataList.value = (res.rows || []).map(item => {
      if (currentType.value === 'factory') return { ...item, code: item.factoryCode, name: item.factoryName }
      if (currentType.value === 'workshop') return { ...item, code: item.workshopCode, name: item.workshopName }
      if (currentType.value === 'line') return { ...item, code: item.lineCode, name: item.lineName }
      return { ...item, code: item.stationCode, name: item.stationName }
    })
    total.value = res.total || 0
    applySavedWidths()
  } finally {
    loading.value = false
  }
}

function handleQuery() { showAdvanced.value = false; queryParams.pageNum = 1; getList() }
function resetQuery() {
  queryParams.code = ''
  queryParams.name = ''
  queryParams.status = undefined
  dateRange.value = []
  queryParams.params = {}
  handleQuery()
}
function handleSelectionChange(selection) { single.value = selection.length !== 1; multiple.value = !selection.length }

function handleAdd() {
  reset()
  dialog.visible = true
  dialog.title = '新增' + currentLevel.value
}

function reset() {
  form.value = { status: '0' }
  proxy.resetForm('formRef')
}

function handleUpdate(row) {
  reset()
  const data = row || dataList.value.find(d => d === proxy.singleSelection)
  if (currentType.value === 'factory') {
    form.value = { ...data, code: data.factoryCode, name: data.factoryName }
  } else if (currentType.value === 'workshop') {
    form.value = { ...data, code: data.workshopCode, name: data.workshopName }
  } else if (currentType.value === 'line') {
    form.value = { ...data, code: data.lineCode, name: data.lineName }
  } else {
    form.value = { ...data, code: data.stationCode, name: data.stationName }
  }
  dialog.visible = true
  dialog.title = '修改' + currentLevel.value
}

function handleView(row) {
  const data = { ...row }
  if (currentType.value === 'factory') { data.code = data.factoryCode; data.name = data.factoryName }
  else if (currentType.value === 'workshop') { data.code = data.workshopCode; data.name = data.workshopName }
  else if (currentType.value === 'line') { data.code = data.lineCode; data.name = data.lineName }
  else { data.code = data.stationCode; data.name = data.stationName }
  viewData.value = data
  viewOpen.value = true
}

async function submitForm() {
  await proxy.$refs.formRef.validate()
  if (currentType.value === 'factory') {
    form.value.factoryCode = form.value.code
    form.value.factoryName = form.value.name
    if (form.value.factoryId) { await updateFactory(form.value) } else { await addFactory(form.value) }
  } else if (currentType.value === 'workshop') {
    form.value.workshopCode = form.value.code
    form.value.workshopName = form.value.name
    if (form.value.workshopId) { await updateWorkshop(form.value) } else { await addWorkshop(form.value) }
  } else if (currentType.value === 'line') {
    form.value.lineCode = form.value.code
    form.value.lineName = form.value.name
    if (form.value.lineId) { await updateLine(form.value) } else { await addLine(form.value) }
  } else {
    form.value.stationCode = form.value.code
    form.value.stationName = form.value.name
    if (form.value.stationId) { await updateStation(form.value) } else { await addStation(form.value) }
  }
  proxy.$modal.msgSuccess('操作成功')
  dialog.visible = false
  getList()
}

function handleDelete(row) {
  const ids = row ? [row.factoryId || row.workshopId || row.lineId || row.stationId] : proxy.multipleSelection.map(d => d.factoryId || d.workshopId || d.lineId || d.stationId)
  proxy.$modal.confirm('确认删除选中的数据？').then(async () => {
    if (currentType.value === 'factory') { await delFactory(ids) }
    else if (currentType.value === 'workshop') { await delWorkshop(ids) }
    else if (currentType.value === 'line') { await delLine(ids) }
    else { await delStation(ids) }
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleExport() {
  proxy.download('/mms/factory/export', { ...queryParams }, '工厂建模数据.xlsx')
}
function cancel() { dialog.visible = false; reset() }

onMounted(() => { loadTree(); getList() })
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-factory-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}

.mms-factory-page .el-row { margin: 0; }

.mms-factory-page .tree-card {
  padding: 0;
  height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
}

.mms-factory-page .tree-card .tree-head {
  padding: 12px 16px;
  border-bottom: 1px solid var(--ink-200);
}

.mms-factory-page .tree-card .tree-head .filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700);
}

.mms-factory-page .tree-card .tree-head .filter-title .glyph {
  width: 4px;
  height: 14px;
  background: var(--brand-600);
  border-radius: 2px;
}

.mms-factory-page .tree-card .tree-search {
  padding: 10px 16px;
  border-bottom: 1px solid var(--ink-100);
}

.mms-factory-page .tree-card .tree-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.mms-factory-page .tree-card .tree-body :deep(.el-tree) {
  background: transparent;
}

.mms-factory-page .tree-card .tree-body :deep(.el-tree .el-tree-node__content) {
  height: 34px;
  border-radius: 6px;
  padding: 0 8px;
  transition: background 0.15s;
}

.mms-factory-page .tree-card .tree-body :deep(.el-tree .el-tree-node__content:hover) {
  background: var(--ink-50);
}

.mms-factory-page .tree-card .tree-body :deep(.el-tree .el-tree-node.is-current > .el-tree-node__content) {
  background: var(--brand-50);
  color: var(--brand-700);
  font-weight: 600;
}

/* ===== rd-dialog / rd-detail-header / rd-card styles ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.rd-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.rd-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width: 100% !important; }

.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-main { display: flex; flex-direction: column; gap: 4px; min-width: 0; padding-right: 36px; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }

.rd-page { max-width: 100%; margin: 0 auto; }
.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); flex-shrink: 0; }
.rd-card-body { padding: 18px 20px; }
.rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; border-radius: 6px; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
@media (max-width: 768px) { .rd-grid { grid-template-columns: 1fr; } .rd-card-header { padding: 12px 16px; } .rd-card-body { padding: 16px; } }
</style>