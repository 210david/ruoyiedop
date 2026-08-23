<template>
  <div class="app-container mms-flowcard-page">
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
          <label>流转卡号</label>
          <div class="control">
            <el-input v-model="queryParams.cardCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>当前工序</label>
          <div class="control">
            <el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_flowcard_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
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
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:flowcard:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:flowcard:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:flowcard:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:flowcard:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_flowcard_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="流转卡号" prop="cardCode" key="cardCode" :width="colWidth('cardCode', 150)" resizable v-if="columns.cardCode.visible" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 130)" resizable v-if="columns.batchNo.visible" />
          <el-table-column label="当前工序顺序号" prop="opSeq" key="opSeq" :width="colWidth('opSeq', 110)" resizable align="center" v-if="columns.opSeq.visible" />
          <el-table-column label="当前工序" prop="processName" key="processName" :width="colWidth('processName', 140)" resizable show-overflow-tooltip v-if="columns.processName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="打印次数" prop="printCount" key="printCount" :width="colWidth('printCount', 90)" resizable align="center" v-if="columns.printCount.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:flowcard:query']">详情</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:flowcard:edit']">修改</el-button>
              <el-button link type="success" icon="Printer" @click="handlePrint(scope.row)" v-hasPermi="['mms:flowcard:print']">打印</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改 Dialog ===== -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="流转卡号" prop="cardCode"><el-input v-model="form.cardCode" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请输入" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="当前工序顺序号" prop="opSeq"><el-input-number v-model="form.opSeq" :min="0" placeholder="请输入" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="当前工序" prop="processName"><el-input v-model="form.processName" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" placeholder="请选择"><el-option v-for="d in mms_flowcard_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="6" width="20" height="12" rx="2"/><path d="M6 10h.01M10 10h.01M14 10h4"/></svg>
          </div>
          <span class="rd-detail-header-title">流转卡详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.cardCode">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewData.cardCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">流转卡号</span><div class="rd-value">{{ viewData.cardCode || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">当前工序顺序号</span><div class="rd-value">{{ viewData.opSeq != null ? viewData.opSeq : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">当前工序</span><div class="rd-value">{{ viewData.processName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status" class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span><span v-else class="text-muted">—</span></div></div>
            <div class="rd-item"><span class="rd-label">打印次数</span><div class="rd-value">{{ viewData.printCount != null ? viewData.printCount : 0 }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div>
          </div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="流转卡管理业务操作说明" width="984px" append-to-body>
      <div class="status-help-content">
        <!-- 一、流转卡释义 -->
        <h4>一、流转卡释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是流转卡？</div>
          <div class="highlight-card-body">
            <strong>流转卡（Flow Card）</strong>是生产管控中跟踪在制品（WIP）在工序间流转的跟踪单据。流转卡关联工单编号、批次号和当前工序，记录流转状态和打印次数，通过工序流转记录实现在制品的精确追踪和批次追溯。<br/><br/>
            流转卡是<strong>MES（制造执行系统）</strong>中在制品管理的核心载体，向上对接工单的工序拆分与排产计划，向下驱动工序间的物料流转、质量追踪和进度回报，满足精益生产对在制品可追溯、可量化、可管控的要求。
          </div>
        </div>

        <!-- 二、流转卡状态流转图 -->
        <h4>二、流转卡状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">流转中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">工序流转</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="primary">流转中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="danger">异常停流</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已停流</el-tag>
            <el-tag size="small" type="info">异常终止</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="流转中">流转卡新建后的初始状态。在制品正在工序间流转，可修改流转卡信息或打印。工序流转完成后状态自动变为已完成</el-descriptions-item>
          <el-descriptions-item label="已完成">流转卡已完成全部工序流转，在制品已完工。不可再修改，数据归档</el-descriptions-item>
          <el-descriptions-item label="已停流">流转卡因异常情况停止流转，不再执行。需排查异常原因后重新创建流转卡</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>流转卡号：</strong>流转卡的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>工单编号：</strong>关联的生产工单编号<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>批次号：</strong>在制品的批次编号，用于批次追溯<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>当前工序顺序号：</strong>当前所在工序的序号</p>
            <p>• <strong>当前工序：</strong>当前正在执行的工序名称</p>
            <p>• <strong>状态：</strong>流转卡的当前流转状态</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">备注信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>备注：</strong>流转卡的补充说明信息</p>
          </div>
        </div>

        <!-- 五、流转卡生命周期管控 -->
        <h4>五、流转卡生命周期管控</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是流转卡生命周期管控？</div>
          <div class="highlight-card-body">
            <strong>流转卡生命周期管控</strong>是生产管控的核心机制，通过状态流转实现流转卡从创建到完工的全过程管理。每个状态对应特定的可执行操作，确保在制品流转有序可控、可追溯。流转卡的工序流转机制确保在制品进度实时可查，打印机制支持实物卡片的多次输出与次数记录。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">异常处理规则</div>
          <div class="highlight-card-body">
            <p>1. <strong>已完成的流转卡无法修改：</strong>流转卡完成全部工序后进入「已完成」状态，不允许再编辑基本信息，确保流转数据的一致性</p>
            <p>2. <strong>已停流的流转卡无法恢复：</strong>流转卡停流后终止，如需继续生产需重新创建流转卡</p>
            <p>3. <strong>打印次数记录：</strong>每次打印流转卡系统自动累计打印次数，确保打印操作可追溯</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>流转卡修改和打印操作均记录操作日志，确保全流程可追溯</p>
          </div>
        </div>

        <!-- 六、业务操作流程 -->
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建流转卡：</strong>点击「新增」创建流转卡，填写工单编号、批次号和工序信息，保存后流转卡号自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>工序流转：</strong>流转卡随在制品在工序间流转，记录当前工序状态，支持随时查看流转进度
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>打印管理：</strong>点击「打印」可输出实物流转卡，支持多次打印并自动记录打印次数
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>点击「详情」查看流转卡完整信息，包括基本信息、状态和打印记录
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>异常停流：</strong>流转中如遇异常情况，流转卡停止流转，需排查原因后重新创建
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowCard">
import { listFlowCard, getFlowCard, addFlowCard, updateFlowCard, delFlowCard, printFlowCard } from "@/api/mms/flowcard";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_flowcard_status } = proxy.useDict("mms_flowcard_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_flowcard_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","vc0","vc1"])

const dataList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const showStatusHelp = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusCounts = ref({});

const statusTabList = computed(() => {
  if (!mms_flowcard_status.value) return [];
  return mms_flowcard_status.value.map(d => ({ label: d.label, value: d.value }));
});

const defaultColumns = {
  cardCode: { label: '流转卡号', visible: true },
  workOrderNo: { label: '工单编号', visible: true },
  batchNo: { label: '批次号', visible: true },
  opSeq: { label: '当前工序顺序号', visible: true },
  processName: { label: '当前工序', visible: true },
  status: { label: '状态', visible: true },
  printCount: { label: '打印次数', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_flowcard_columns')
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
  let count = 0;
  if (queryParams.value.cardCode) count++;
  if (queryParams.value.workOrderNo) count++;
  if (queryParams.value.batchNo) count++;
  if (queryParams.value.processName) count++;
  if (queryParams.value.status) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    cardCode: undefined,
    workOrderNo: undefined,
    batchNo: undefined,
    processName: undefined,
    status: undefined,
    params: {}
  },
  rules: {
    workOrderNo: [{ required: true, message: "请输入工单编号", trigger: "blur" }],
    batchNo: [{ required: true, message: "请输入批次号", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listFlowCard(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    loadStatusCounts();
  });
}

function loadStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 };
  if (queryParams.value.cardCode) baseQuery.cardCode = queryParams.value.cardCode;
  if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo;
  if (queryParams.value.batchNo) baseQuery.batchNo = queryParams.value.batchNo;
  if (queryParams.value.processName) baseQuery.processName = queryParams.value.processName;
  listFlowCard(proxy.addDateRange(baseQuery, dateRange.value)).then(res => {
    const counts = { all: res.total };
    if (mms_flowcard_status.value) {
      mms_flowcard_status.value.forEach(d => { counts[d.value] = 0; });
      (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; });
    }
    statusCounts.value = counts;
  }).catch(() => {});
}

function handleQuery() {
  showAdvanced.value = false;
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value.cardCode = undefined;
  queryParams.value.workOrderNo = undefined;
  queryParams.value.batchNo = undefined;
  queryParams.value.processName = undefined;
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
  ids.value = selection.map(item => item.cardId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    cardId: undefined,
    cardCode: undefined,
    workOrderId: undefined,
    workOrderNo: undefined,
    batchNo: undefined,
    opSeq: undefined,
    processName: undefined,
    status: undefined,
    qrContent: undefined,
    printCount: undefined,
    remark: undefined,
  };
  proxy.resetForm("formRef");
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增流转卡";
}

function handleUpdate(row) {
  reset();
  const id = row.cardId || ids.value[0];
  getFlowCard(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改流转卡";
  });
}

function handleView(row) {
  getFlowCard(row.cardId).then(response => {
    viewData.value = response.data;
    viewOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.cardId != null) {
        updateFlowCard(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addFlowCard(form.value).then(response => {
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
  const delIds = row.cardId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的流转卡？').then(function() {
    return delFlowCard(delIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleExport() {
  proxy.download("mms/flowcard/export", { ...queryParams.value }, `flowcard_${new Date().getTime()}.xlsx`);
}

function handlePrint(row) {
  proxy.$modal.confirm('是否确认打印流转卡"' + row.cardCode + '"？').then(function() {
    return printFlowCard(row.cardId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("打印成功");
  }).catch(() => {});
}

function statusLabel(status) {
  const item = mms_flowcard_status.value.find(d => d.value == status);
  return item ? item.label : '—';
}

function badgeClass(status) {
  const map = { '0': 'blue', '1': 'green', '2': 'red' };
  return map[status] || 'gray';
}

function statusTabClass(value) {
  const map = { '0': 'tab-audit', '1': 'tab-done', '2': 'tab-reject' };
  return map[value] || '';
}

getList();
</script>

<style scoped>
.mms-flowcard-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.mms-flowcard-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-flowcard-page .filter-card { padding:14px 20px 16px; }
.mms-flowcard-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-flowcard-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-flowcard-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-flowcard-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-flowcard-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-flowcard-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-flowcard-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-flowcard-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-flowcard-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-flowcard-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-flowcard-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-flowcard-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-flowcard-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-flowcard-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-flowcard-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-flowcard-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-flowcard-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-flowcard-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-flowcard-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-flowcard-page .field .control :deep(.el-select) { width:100%; }
.mms-flowcard-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-flowcard-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-flowcard-page .toolbar .left, .mms-flowcard-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-flowcard-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-flowcard-page .table-wrap { overflow-x:auto; }
.mms-flowcard-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-flowcard-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-flowcard-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-flowcard-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-flowcard-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-flowcard-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-flowcard-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-flowcard-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-flowcard-page .badge.amber .dot { background:var(--amber-500); }
.mms-flowcard-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-flowcard-page .badge.blue .dot { background:var(--blue-500); }
.mms-flowcard-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-flowcard-page .badge.green .dot { background:var(--green-500); }
.mms-flowcard-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .mms-flowcard-page .badge.red .dot { background:var(--red-500); }
.mms-flowcard-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-flowcard-page .badge.gray .dot { background:var(--ink-400); }
.mms-flowcard-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-flowcard-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-flowcard-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-flowcard-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-flowcard-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-flowcard-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-flowcard-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-flowcard-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-flowcard-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-flowcard-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-flowcard-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-flowcard-page .status-tab.tab-audit .dot { background:var(--blue-500); } .mms-flowcard-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .mms-flowcard-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.mms-flowcard-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-flowcard-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-flowcard-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-flowcard-page .status-tab.tab-reject .dot { background:var(--red-500); } .mms-flowcard-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); } .mms-flowcard-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
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
.mms-flowcard-page .rd-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 12px; overflow: hidden; }
.mms-flowcard-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; cursor: pointer; background: #f8fafc; border-bottom: 1px solid #e5e7eb; }
.mms-flowcard-page .rd-card-header:hover { background: #f1f5f9; }
.mms-flowcard-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-flowcard-page .rd-card-icon { display: flex; align-items: center; color: #6366f1; }
.mms-flowcard-page .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; cursor: pointer; color: #94a3b8; transition: transform .2s; }
.mms-flowcard-page .rd-collapse-btn.is-collapsed { transform: rotate(180deg); }
.mms-flowcard-page .rd-card-body { padding: 16px; }
.mms-flowcard-page .text-muted { color: #94a3b8; }
.mms-flowcard-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.mms-flowcard-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.mms-flowcard-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
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
@media (max-width:1100px) { .mms-flowcard-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mms-flowcard-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
