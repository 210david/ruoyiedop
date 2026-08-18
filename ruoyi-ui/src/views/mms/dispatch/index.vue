<template>
  <div class="app-container mms-dispatch-page">
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
          <label>派工单号</label>
          <div class="control">
            <el-input v-model="queryParams.dispatchNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单编号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>工序名称</label>
          <div class="control">
            <el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>产能单元</label>
          <div class="control">
            <el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_dispatch_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>班组</label>
          <div class="control">
            <el-input v-model="queryParams.teamName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:dispatch:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:dispatch:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:dispatch:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:dispatch:export']">导出</el-button>
        </div>
        <div class="right">
          <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_dispatch_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="派工单号" prop="dispatchNo" key="dispatchNo" :width="colWidth('dispatchNo', 150)" resizable v-if="columns.dispatchNo.visible" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="工序序号" prop="opSeq" key="opSeq" :width="colWidth('opSeq', 90)" resizable align="center" v-if="columns.opSeq.visible" />
          <el-table-column label="工序名称" prop="processName" key="processName" :width="colWidth('processName', 140)" resizable show-overflow-tooltip v-if="columns.processName.visible" />
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 140)" resizable show-overflow-tooltip v-if="columns.resourceName.visible" />
          <el-table-column label="班组" prop="teamName" key="teamName" :width="colWidth('teamName', 120)" resizable v-if="columns.teamName.visible" />
          <el-table-column label="派工人员" prop="userIds" key="userIds" :width="colWidth('userIds', 150)" resizable show-overflow-tooltip v-if="columns.userIds.visible" />
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="合格数量" prop="goodQty" key="goodQty" :width="colWidth('goodQty', 100)" resizable align="center" v-if="columns.goodQty.visible" />
          <el-table-column label="不良数量" prop="defectQty" key="defectQty" :width="colWidth('defectQty', 100)" resizable align="center" v-if="columns.defectQty.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="计划开始" prop="planStart" key="planStart" :width="colWidth('planStart', 160)" resizable align="center" v-if="columns.planStart.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.planStart) }}</span></template>
          </el-table-column>
          <el-table-column label="计划结束" prop="planEnd" key="planEnd" :width="colWidth('planEnd', 160)" resizable align="center" v-if="columns.planEnd.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.planEnd) }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:dispatch:query']">详情</el-button>
              <el-button v-if="scope.row.status === '0'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:dispatch:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="success" icon="VideoPlay" @click="handleStart(scope.row)" v-hasPermi="['mms:dispatch:start']">开工</el-button>
              <el-button v-if="scope.row.status === '1'" link type="primary" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['mms:dispatch:finish']">完工</el-button>
              <el-button v-if="scope.row.status === '0' || scope.row.status === '1'" link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['mms:dispatch:cancel']">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改 Dialog ===== -->
    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="派工单号" prop="dispatchNo">
              <el-input v-model="form.dispatchNo" placeholder="自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单编号" prop="workOrderNo">
              <el-input v-model="form.workOrderNo" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工序序号" prop="opSeq">
              <el-input-number v-model="form.opSeq" :min="1" placeholder="请输入" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工序名称" prop="processName">
              <el-input v-model="form.processName" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产能单元" prop="resourceName">
              <el-input v-model="form.resourceName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班组" prop="teamName">
              <el-input v-model="form.teamName" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="派工人员" prop="userIds">
              <el-input v-model="form.userIds" placeholder="请输入人员" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划数量" prop="planQty">
              <el-input-number v-model="form.planQty" :min="0" :precision="2" placeholder="请输入" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始" prop="planStart">
              <el-date-picker v-model="form.planStart" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束" prop="planEnd">
              <el-date-picker v-model="form.planEnd" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="800px" append-to-body draggable>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/><path d="M9 3V1h4v2"/><path d="M9 3v6h4V3"/></svg>
          </div>
          <span class="rd-detail-header-title">派工单详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.dispatchNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewData.dispatchNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">派工单号</span><div class="rd-value">{{ viewData.dispatchNo || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">工序序号</span><div class="rd-value">{{ viewData.opSeq != null ? viewData.opSeq : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ viewData.processName || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">班组</span><div class="rd-value">{{ viewData.teamName || '-' }}</div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">派工人员</span><div class="rd-value">{{ viewData.userIds || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ viewData.goodQty != null ? viewData.goodQty : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">不良数量</span><div class="rd-value">{{ viewData.defectQty != null ? viewData.defectQty : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span></div></div>
          <div class="rd-item"><span class="rd-label">计划开始</span><div class="rd-value">{{ viewData.planStart ? parseTime(viewData.planStart) : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">计划结束</span><div class="rd-value">{{ viewData.planEnd ? parseTime(viewData.planEnd) : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">实际开始</span><div class="rd-value">{{ viewData.actualStart ? parseTime(viewData.actualStart) : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">实际结束</span><div class="rd-value">{{ viewData.actualEnd ? parseTime(viewData.actualEnd) : '-' }}</div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div>
          <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div>
        </div>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="派工管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、派工单释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是派工单？</div>
          <div class="highlight-card-body">
            <strong>派工单（Dispatch Order）</strong>是制造管理中将工单工序任务分配到具体产能单元和人员的执行单据。派工单关联工单编号、工序序号、产能单元、班组和派工人员，记录计划数量与实际完成数量，是实现工序级任务派发与进度追踪的核心载体。<br/><br/>
            派工单遵循 <strong>MES 派工管理规范</strong>，通过开工→完工→取消的状态流转，实现生产任务的精细化管控和过程可追溯。
          </div>
        </div>

        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning" size="small">待开工</el-tag>
          </div>
          <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          <div class="flow-item">
            <el-tag type="primary" size="small">进行中</el-tag>
          </div>
          <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          <div class="flow-item">
            <el-tag type="success" size="small">已完成</el-tag>
          </div>
          <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          <div class="flow-item">
            <el-tag type="info" size="small">已取消</el-tag>
          </div>
        </div>

        <h4>三、状态说明</h4>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="待开工">派工单已创建，尚未开始执行，可修改、开工或取消。</el-descriptions-item>
          <el-descriptions-item label="进行中">派工单已开工，正在执行生产任务，可完工或取消。</el-descriptions-item>
          <el-descriptions-item label="已完成">派工单已完工，实际生产数量已回写，不可再修改。</el-descriptions-item>
          <el-descriptions-item label="已取消">派工单已取消作废，不再有效，不可再操作。</el-descriptions-item>
        </el-descriptions>

        <h4>四、表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">填写说明</div>
          <div class="highlight-card-body">
            <p><strong>派工单号：</strong>系统自动生成，无需手动输入。</p>
            <p><strong>工单编号：</strong>必填，关联对应生产工单。</p>
            <p><strong>工序序号 / 工序名称：</strong>必填，对应工单中的工序信息。</p>
            <p><strong>产能单元：</strong>必填，指定执行该任务的产能单元。</p>
            <p><strong>班组：</strong>选填，指定执行任务的班组。</p>
            <p><strong>派工人员：</strong>必填，指定执行任务的操作人员。</p>
            <p><strong>计划数量：</strong>必填，该工序的计划生产数量。</p>
            <p><strong>计划开始 / 计划结束：</strong>选填，设定任务计划执行时间窗口。</p>
          </div>
        </div>

        <h4>五、生命周期管控</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">关键管控节点</div>
          <div class="highlight-card-body">
            <p>1. <strong>创建</strong>：新建派工单后状态为「待开工」，可修改和取消。</p>
            <p>2. <strong>开工</strong>：点击「开工」按钮后状态变为「进行中」，开始记录实际执行信息。</p>
            <p>3. <strong>完工</strong>：点击「完工」按钮后状态变为「已完成」，回写合格数量和不良数量。</p>
            <p>4. <strong>取消</strong>：在「待开工」或「进行中」状态可取消派工单，取消后状态变为「已取消」。</p>
          </div>
        </div>

        <h4>六、异常处理规则</h4>
        <div class="highlight-card highlight-danger">
          <div class="highlight-card-title">异常场景与处理</div>
          <div class="highlight-card-body">
            <p><strong>已开工的派工单无法修改：</strong>派工单开工后进入「进行中」状态，不允许再编辑基本信息。</p>
            <p><strong>已完工的派工单无法取消：</strong>完工状态为终态，不可再取消或修改。</p>
            <p><strong>取消后不可恢复：</strong>派工单取消后作废，如需重新执行需新建派工单。</p>
          </div>
        </div>

        <h4>七、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>新增派工单：</strong>填写工单编号、工序信息和派工人员，保存后状态为「待开工」</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>开工：</strong>确认开工后状态变为「进行中」，开始执行生产任务</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>完工：</strong>生产完成后点击完工，回写实际数量，状态变为「已完成」</el-timeline-item>
          <el-timeline-item type="info" :hollow="true"><strong>取消：</strong>在待开工或进行中状态可取消派工单</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Dispatch">
import { listDispatch, getDispatch, addDispatch, updateDispatch, delDispatch,
         startDispatch, finishDispatch, cancelDispatch } from "@/api/mms/dispatch";
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_dispatch_status } = proxy.useDict("mms_dispatch_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_dispatch_index')

const dataList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const showStatusHelp = ref(false);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusCounts = ref({});

// 状态标签列表
const statusTabList = computed(() => {
  if (!mms_dispatch_status.value) return [];
  return mms_dispatch_status.value.map(d => ({ label: d.label, value: d.value }));
});

// 列显隐配置
const defaultColumns = {
  dispatchNo: { label: '派工单号', visible: true },
  workOrderNo: { label: '工单编号', visible: true },
  opSeq: { label: '工序序号', visible: true },
  processName: { label: '工序名称', visible: true },
  resourceName: { label: '产能单元', visible: true },
  teamName: { label: '班组', visible: true },
  userIds: { label: '派工人员', visible: true },
  planQty: { label: '计划数量', visible: true },
  goodQty: { label: '合格数量', visible: true },
  defectQty: { label: '不良数量', visible: true },
  status: { label: '状态', visible: true },
  planStart: { label: '计划开始', visible: true },
  planEnd: { label: '计划结束', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_dispatch_columns')
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

// 当前已选筛选条件数
const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.value.dispatchNo) count++;
  if (queryParams.value.workOrderNo) count++;
  if (queryParams.value.processName) count++;
  if (queryParams.value.resourceName) count++;
  if (queryParams.value.status) count++;
  if (queryParams.value.teamName) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dispatchNo: undefined,
    workOrderNo: undefined,
    processName: undefined,
    resourceName: undefined,
    teamName: undefined,
    status: undefined,
    params: {}
  },
  rules: {
    workOrderNo: [{ required: true, message: "请输入工单编号", trigger: "blur" }],
    opSeq: [{ required: true, message: "请输入工序序号", trigger: "blur" }],
    processName: [{ required: true, message: "请输入工序名称", trigger: "blur" }],
    planQty: [{ required: true, message: "请输入计划数量", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listDispatch(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    updateStatusCounts(response.rows);
  });
}

function updateStatusCounts(rows) {
  const counts = { all: total.value };
  if (mms_dispatch_status.value) {
    mms_dispatch_status.value.forEach(d => {
      counts[d.value] = rows.filter(r => r.status === d.value).length;
    });
  }
  statusCounts.value = counts;
}

function handleQuery() {
  showAdvanced.value = false;
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value.dispatchNo = undefined;
  queryParams.value.workOrderNo = undefined;
  queryParams.value.processName = undefined;
  queryParams.value.resourceName = undefined;
  queryParams.value.teamName = undefined;
  queryParams.value.status = undefined;
  dateRange.value = [];
  queryParams.value.params = {};
  activeStatusTab.value = 'all';
  handleQuery();
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status;
  queryParams.value.status = status === "all" ? undefined : status;
  handleQuery();
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop;
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc';
  } else {
    queryParams.value.params.orderByColumn = undefined;
    queryParams.value.params.isAsc = undefined;
  }
  getList();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dispatchId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    dispatchNo: undefined,
    workOrderId: undefined,
    workOrderNo: undefined,
    opSeq: undefined,
    processId: undefined,
    processName: undefined,
    resourceId: undefined,
    resourceName: undefined,
    teamId: undefined,
    teamName: undefined,
    userIds: undefined,
    planQty: undefined,
    goodQty: undefined,
    defectQty: undefined,
    planStart: undefined,
    planEnd: undefined,
    status: undefined,
    remark: undefined,
  };
  proxy.resetForm("formRef");
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增派工单";
}

function handleUpdate(row) {
  reset();
  const id = row.dispatchId || ids.value[0];
  getDispatch(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改派工单";
  });
}

function handleView(row) {
  getDispatch(row.dispatchId).then(response => {
    viewData.value = response.data;
    viewOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.dispatchId != null) {
        updateDispatch(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addDispatch(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function cancel() {
  open.value = false;
  reset();
}

function handleDelete(row) {
  const delIds = row.dispatchId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的派工单？').then(function() {
    return delDispatch(delIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleExport() {
  proxy.download("mms/dispatch/export", { ...queryParams.value }, `dispatch_${new Date().getTime()}.xlsx`);
}

// ===== 业务操作 =====

function handleStart(row) {
  proxy.$modal.confirm('是否确认开工派工单"' + row.dispatchNo + '"？').then(function() {
    return startDispatch(row.dispatchId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("开工成功");
  }).catch(() => {});
}

function handleFinish(row) {
  proxy.$modal.confirm('是否确认完工派工单"' + row.dispatchNo + '"？').then(function() {
    return finishDispatch(row.dispatchId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("完工成功");
  }).catch(() => {});
}

function handleCancel(row) {
  proxy.$modal.confirm('是否确认取消派工单"' + row.dispatchNo + '"？').then(function() {
    return cancelDispatch(row.dispatchId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("取消成功");
  }).catch(() => {});
}

// ===== 字典辅助函数 =====
function statusLabel(status) {
  const item = mms_dispatch_status.value.find(d => d.value == status);
  return item ? item.label : '-';
}

function badgeClass(status) {
  const map = {
    '0': 'amber',    // 待开工
    '1': 'blue',     // 进行中
    '2': 'green',    // 已完成
    '3': 'gray'      // 已取消
  };
  return map[status] || 'gray';
}

function statusTabClass(value) {
  const map = {
    '0': 'tab-draft',
    '1': 'tab-audit',
    '2': 'tab-done',
    '3': 'tab-void'
  };
  return map[value] || '';
}

getList();
</script>

<style scoped>
.mms-dispatch-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.mms-dispatch-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-dispatch-page .filter-card { padding:14px 20px 16px; }
.mms-dispatch-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-dispatch-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-dispatch-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-dispatch-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-dispatch-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-dispatch-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-dispatch-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-dispatch-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-dispatch-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-dispatch-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-dispatch-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-dispatch-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-dispatch-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-dispatch-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-dispatch-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-dispatch-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-dispatch-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-dispatch-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-dispatch-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-dispatch-page .field .control :deep(.el-select) { width:100%; }
.mms-dispatch-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-dispatch-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-dispatch-page .toolbar .left, .mms-dispatch-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-dispatch-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-dispatch-page .table-wrap { overflow-x:auto; }
.mms-dispatch-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-dispatch-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-dispatch-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-dispatch-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-dispatch-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-dispatch-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-dispatch-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-dispatch-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-dispatch-page .badge.amber .dot { background:var(--amber-500); }
.mms-dispatch-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-dispatch-page .badge.blue .dot { background:var(--blue-500); }
.mms-dispatch-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-dispatch-page .badge.green .dot { background:var(--green-500); }
.mms-dispatch-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .mms-dispatch-page .badge.red .dot { background:var(--red-500); }
.mms-dispatch-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-dispatch-page .badge.gray .dot { background:var(--ink-400); }
.mms-dispatch-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-dispatch-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-dispatch-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-dispatch-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-dispatch-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-dispatch-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-dispatch-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-dispatch-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-dispatch-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-dispatch-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-dispatch-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-dispatch-page .status-tab.tab-draft .dot { background:var(--amber-500); } .mms-dispatch-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .mms-dispatch-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.mms-dispatch-page .status-tab.tab-audit .dot { background:var(--blue-500); } .mms-dispatch-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .mms-dispatch-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.mms-dispatch-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-dispatch-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-dispatch-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-dispatch-page .status-tab.tab-void .dot { background:var(--ink-400); } .mms-dispatch-page .status-tab.tab-void .count { background:var(--ink-100); color:var(--ink-500); } .mms-dispatch-page .status-tab.is-active.tab-void .count { background:var(--ink-400); color:#fff; }
.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }
.rd-page { max-width: 760px; margin: 0 auto; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
@media (max-width:1100px) { .mms-dispatch-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mms-dispatch-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
