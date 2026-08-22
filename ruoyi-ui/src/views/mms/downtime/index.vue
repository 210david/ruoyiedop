<template>
  <div class="app-container mms-downtime-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>停机单号</label>
          <div class="control"><el-input v-model="queryParams.downtimeNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>产能单元</label>
          <div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_downtime_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>停机日期</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 支持回车快速搜索</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <span class="status-tab is-active"><span class="dot"></span><span>停机管理</span></span>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:downtime:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:downtime:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:downtime:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:downtime:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="停机单号" prop="downtimeNo" width="150" />
          <el-table-column label="产能单元" prop="resourceName" width="140" show-overflow-tooltip />
          <el-table-column label="开始时间" prop="startTime" width="160" align="center"><template #default="scope">{{ parseTime(scope.row.startTime) }}</template></el-table-column>
          <el-table-column label="结束时间" prop="endTime" width="160" align="center"><template #default="scope">{{ scope.row.endTime ? parseTime(scope.row.endTime) : '—' }}</template></el-table-column>
          <el-table-column label="停机类型" prop="dtType" width="100" align="center" />
          <el-table-column label="停机时长(分)" prop="minutes" width="110" align="center" />
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'red' : 'green'"><span class="dot"></span>{{ scope.row.status === '0' ? '停机中' : '已恢复' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="停机原因" prop="reason" show-overflow-tooltip />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:downtime:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:downtime:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="停机单号" prop="downtimeNo"><el-input v-model="form.downtimeNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="产能单元" prop="resourceName"><el-input v-model="form.resourceName" placeholder="请输入" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>停机详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="停机类型" prop="dtType"><el-input v-model="form.dtType" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">停机中</el-radio><el-radio value="1">已恢复</el-radio></el-radio-group></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>描述信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="停机原因" prop="reason"><el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">停机记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.downtimeNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.downtimeNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">停机单号</span><div class="rd-value">{{ viewData.downtimeNo || '—' }}</div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '—' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status === '0'" class="badge red"><span class="dot"></span>停机中</span><span v-else-if="viewData.status === '1'" class="badge green"><span class="dot"></span>已恢复</span><span v-else class="text-muted">—</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>停机详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewData.startTime ? parseTime(viewData.startTime) : '—' }}</div></div><div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ viewData.endTime ? parseTime(viewData.endTime) : '—' }}</div></div><div class="rd-item"><span class="rd-label">停机类型</span><div class="rd-value">{{ viewData.dtType || '—' }}</div></div><div class="rd-item"><span class="rd-label">停机时长(分)</span><div class="rd-value">{{ viewData.minutes != null ? viewData.minutes : '—' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>描述信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">停机原因</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.reason }">{{ viewData.reason || '暂无' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div><div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="停机管理业务操作说明" width="984px" append-to-body>
      <div class="status-help-content">
        <!-- 一、停机管理释义 -->
        <h4>一、停机管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是停机管理？</div>
          <div class="highlight-card-body">
            <strong>停机管理（Downtime Management）</strong>是生产管控中记录生产设备/产线停机事件的单据。停机管理记录停机开始/结束时间、停机类型、停机时长和停机原因，支持设备利用率分析和产能损耗统计。<br/><br/>
            停机管理是<strong>MES（制造执行系统）</strong>中设备效率管理的核心载体，通过停机时长统计支持 OEE（设备综合效率）计算，停机类型分类（计划停机/故障停机/换型停机等）支持根因分析和改善措施制定，满足精益生产对设备运行状态可追溯、可量化、可管控的要求。
          </div>
        </div>

        <!-- 二、停机状态流转图 -->
        <h4>二、停机状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="danger">停机中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">填写结束时间</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已恢复</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="停机中">设备/产线已停机，尚未恢复。可修改停机信息、填写结束时间恢复生产。填写结束时间后状态自动变为已恢复</el-descriptions-item>
          <el-descriptions-item label="已恢复">设备/产线已恢复生产，停机时长已自动计算。不可再修改停机信息，数据归档用于统计分析</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>停机单号：</strong>停机记录的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>产能单元：</strong>发生停机的设备/产线名称<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">停机详情区</div>
          <div class="highlight-card-body">
            <p>• <strong>开始时间：</strong>停机开始的准确时间<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>结束时间：</strong>停机恢复的时间，填写后系统自动计算停机时长</p>
            <p>• <strong>停机类型：</strong>停机类型分类，如计划停机、故障停机、换型停机等</p>
            <p>• <strong>状态：</strong>停机记录的当前状态，包括停机中和已恢复</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">描述信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>停机原因：</strong>详细描述停机的原因和经过</p>
            <p>• <strong>备注：</strong>停机记录的补充说明信息</p>
          </div>
        </div>

        <!-- 五、停机管理生命周期管控 -->
        <h4>五、停机管理生命周期管控</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是停机管理生命周期管控？</div>
          <div class="highlight-card-body">
            <strong>停机管理生命周期管控</strong>是生产管控的核心机制，通过状态流转实现停机记录从创建到恢复的全过程管理。每个状态对应特定的可执行操作，确保设备停机事件有序可控、可追溯。停机时长的自动计算机制确保设备效率数据准确归集，停机类型分类支持根因分析和持续改善。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">异常处理规则</div>
          <div class="highlight-card-body">
            <p>1. <strong>已恢复的停机记录无法修改：</strong>停机记录恢复后进入「已恢复」状态，不允许再编辑基本信息，确保停机数据的一致性</p>
            <p>2. <strong>停机时长自动计算：</strong>填写结束时间后系统自动计算停机时长（分钟），无需手动输入</p>
            <p>3. <strong>停机类型准确分类：</strong>停机类型影响 OEE 计算和根因分析，需准确填写</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>停机记录的创建和恢复操作均记录操作日志，确保全流程可追溯</p>
          </div>
        </div>

        <!-- 六、业务操作流程 -->
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="danger" :hollow="true">
            <strong>记录停机：</strong>设备/产线停机时点击「新增」创建停机记录，填写产能单元、开始时间和停机类型
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>记录恢复：</strong>停机结束后填写结束时间，系统自动计算停机时长，状态变为已恢复
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>点击「查看」查看停机记录完整信息，包括停机时长、停机原因等
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>分析统计：</strong>停机数据用于设备利用率分析、OEE 计算和产能损耗统计
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Downtime">
import { listDowntime, getDowntime, addDowntime, updateDowntime, delDowntime } from "@/api/mms/downtime";
import { useDetailCard } from '@/composables/useDetailCard'
import { Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const { mms_downtime_status } = proxy.useDict("mms_downtime_status");
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])
const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref(""); const dateRange = ref([]); const showStatusHelp = ref(false);
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, downtimeNo: undefined, resourceName: undefined, status: undefined, params: {} },
  rules: { resourceName: [{ required: true, message: "请输入产能单元", trigger: "blur" }], startTime: [{ required: true, message: "请选择开始时间", trigger: "change" }] }
});
const { queryParams, form, rules } = toRefs(data);
function getList() { loading.value = true; listDowntime(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.downtimeNo = undefined; queryParams.value.resourceName = undefined; queryParams.value.status = undefined; dateRange.value = []; handleQuery(); }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.downtimeId); single.value = sel.length !== 1; multiple.value = !sel.length; }
function reset() { form.value = { downtimeId: undefined, downtimeNo: undefined, resourceName: undefined, startTime: undefined, endTime: undefined, dtType: undefined, status: "0", reason: undefined, remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增停机记录"; }
function handleUpdate(row) { reset(); getDowntime(row.downtimeId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = "修改停机记录"; }); }
function handleView(row) { const id = row.downtimeId || ids.value[0]; getDowntime(id).then(res => { viewData.value = res.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(v => { if (v) { if (form.value.downtimeId != null) { updateDowntime(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addDowntime(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.downtimeId || ids.value; proxy.$modal.confirm('确认删除选中的停机记录？').then(() => delDowntime(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/downtime/export", { ...queryParams.value }, `downtime_${new Date().getTime()}.xlsx`); }
getList();
</script>

<style scoped>
.mms-downtime-page { padding-top: 10px; }
.mms-downtime-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-downtime-page .filter-card { padding: 14px 20px 16px; }
.mms-downtime-page .filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.mms-downtime-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-downtime-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-downtime-page .adv-link { font-size: 14px; color: #64748b; cursor: pointer; display: flex; align-items: center; gap: 4px; }
.mms-downtime-page .adv-link:hover { color: #4f46e5; }
.mms-downtime-page .adv-link .chev { transition: transform .2s; }
.mms-downtime-page .adv-link.is-open .chev { transform: rotate(180deg); }
.mms-downtime-page .filter-bar { display: grid; grid-template-columns: repeat(4, minmax(0,1fr)); gap: 12px 16px; }
.mms-downtime-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-downtime-page .filter-info { font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 6px; }
.mms-downtime-page .filter-buttons { display: flex; gap: 8px; }
.mms-downtime-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-downtime-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-downtime-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-downtime-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-downtime-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-downtime-page .field .control :deep(.el-select) { width: 100%; }
.mms-downtime-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; min-height: 34px; height: 34px; }
.mms-downtime-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-downtime-page .toolbar .left, .mms-downtime-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-downtime-page .toolbar-divider { width: 1px; height: 18px; background: #e2e8f0; margin: 0 4px; }
.mms-downtime-page .table-wrap { overflow-x: auto; }
.mms-downtime-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.mms-downtime-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.mms-downtime-page .badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; } .mms-downtime-page .badge.red .dot { background: #ef4444; }
.mms-downtime-page .badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; } .mms-downtime-page .badge.green .dot { background: #10b981; }
.mms-downtime-page .rd-page { max-width: 760px; margin: 0 auto; }
.mms-downtime-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.mms-downtime-page .rd-item { display: flex; align-items: center; gap: 12px; }
.mms-downtime-page .rd-item--full { grid-column: 1 / -1; }
.mms-downtime-page .rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.mms-downtime-page .rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.mms-downtime-page .rd-value--muted { color: #9ca3af; font-style: italic; }
.mms-downtime-page .rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.mms-downtime-page .rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.mms-downtime-page .rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; }
.mms-downtime-page .rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; }
.mms-downtime-page .rd-detail-header-sub { display: flex; align-items: center; gap: 8px; }
.mms-downtime-page .rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); }
.mms-downtime-page .rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); }
.mms-downtime-page .rd-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 12px; overflow: hidden; }
.mms-downtime-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; cursor: pointer; background: #f8fafc; border-bottom: 1px solid #e5e7eb; }
.mms-downtime-page .rd-card-header:hover { background: #f1f5f9; }
.mms-downtime-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-downtime-page .rd-card-icon { display: flex; align-items: center; color: #6366f1; }
.mms-downtime-page .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; cursor: pointer; color: #94a3b8; transition: transform .2s; }
.mms-downtime-page .rd-collapse-btn.is-collapsed { transform: rotate(180deg); }
.mms-downtime-page .rd-card-body { padding: 16px; }
.mms-downtime-page .text-muted { color: #94a3b8; }
.mms-downtime-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s; flex-shrink:0; white-space:nowrap; }
.mms-downtime-page .tip-pill:hover { background:#fffbeb; border-color:#f59e0b; color:#7c2d12; }
.mms-downtime-page .tip-pill .el-icon { font-size:14px; color:#b45309; }
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
@media (max-width: 1100px) { .mms-downtime-page .filter-bar { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 720px) { .mms-downtime-page .filter-bar { grid-template-columns: 1fr; } }
</style>
