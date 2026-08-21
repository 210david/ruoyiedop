<template>
  <div class="app-container mms-outsource-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>外协单号</label><div class="control"><el-input v-model="queryParams.outsourceNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>供应商</label><div class="control"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_outsource_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>外协日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track">
        <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span></button>
        <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span></button>
      </div></div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:outsource:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:outsource:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:outsource:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:outsource:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="外协单号" prop="outsourceNo" width="150" />
          <el-table-column label="工单编号" prop="workOrderNo" width="140" />
          <el-table-column label="工序" prop="processName" width="120" show-overflow-tooltip />
          <el-table-column label="供应商" prop="supplierName" width="140" show-overflow-tooltip />
          <el-table-column label="外协数量" prop="outQty" width="100" align="center" />
          <el-table-column label="单价" prop="unitPrice" width="100" align="center" />
          <el-table-column label="总金额" prop="totalAmount" width="120" align="center" />
          <el-table-column label="外协日期" prop="outDate" width="120" align="center"><template #default="scope">{{ scope.row.outDate ? parseTime(scope.row.outDate, '{y}-{m}-{d}') : '—' }}</template></el-table-column>
          <el-table-column label="计划回厂" prop="planBackDate" width="120" align="center"><template #default="scope">{{ scope.row.planBackDate ? parseTime(scope.row.planBackDate, '{y}-{m}-{d}') : '—' }}</template></el-table-column>
          <el-table-column label="实际回厂" prop="actualBackDate" width="120" align="center"><template #default="scope">{{ scope.row.actualBackDate ? parseTime(scope.row.actualBackDate, '{y}-{m}-{d}') : '—' }}</template></el-table-column>
          <el-table-column label="回厂数量" prop="backQty" width="100" align="center" />
          <el-table-column label="损耗数量" prop="lossQty" width="100" align="center" />
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:outsource:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:outsource:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="820px" append-to-body draggable class="rd-dialog">
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="外协单号" prop="outsourceNo"><el-input v-model="form.outsourceNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请输入" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序序号" prop="opSeq"><el-input-number v-model="form.opSeq" :min="1" style="width:100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="工序名称" prop="processName"><el-input v-model="form.processName" placeholder="请输入" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>外协信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="外协数量" prop="outQty"><el-input-number v-model="form.outQty" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status"><el-option v-for="d in mms_outsource_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日期信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="外协日期" prop="outDate"><el-date-picker v-model="form.outDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="计划回厂" prop="planBackDate"><el-date-picker v-model="form.planBackDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="实际回厂" prop="actualBackDate"><el-date-picker v-model="form.actualBackDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">外协单详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.outsourceNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.outsourceNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">外协单号</span><div class="rd-value">{{ viewData.outsourceNo || '—' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '—' }}</div></div><div class="rd-item"><span class="rd-label">工序序号</span><div class="rd-value">{{ viewData.opSeq != null ? viewData.opSeq : '—' }}</div></div><div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ viewData.processName || '—' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>外协信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '—' }}</div></div><div class="rd-item"><span class="rd-label">外协数量</span><div class="rd-value">{{ viewData.outQty != null ? viewData.outQty : '—' }}</div></div><div class="rd-item"><span class="rd-label">单价</span><div class="rd-value">{{ viewData.unitPrice != null ? viewData.unitPrice : '—' }}</div></div><div class="rd-item"><span class="rd-label">总金额</span><div class="rd-value">{{ viewData.totalAmount != null ? viewData.totalAmount : '—' }}</div></div><div class="rd-item"><span class="rd-label">回厂数量</span><div class="rd-value">{{ viewData.backQty != null ? viewData.backQty : '—' }}</div></div><div class="rd-item"><span class="rd-label">损耗数量</span><div class="rd-value">{{ viewData.lossQty != null ? viewData.lossQty : '—' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status" class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span><span v-else class="text-muted">—</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日期信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">外协日期</span><div class="rd-value">{{ viewData.outDate ? parseTime(viewData.outDate, '{y}-{m}-{d}') : '—' }}</div></div><div class="rd-item"><span class="rd-label">计划回厂</span><div class="rd-value">{{ viewData.planBackDate ? parseTime(viewData.planBackDate, '{y}-{m}-{d}') : '—' }}</div></div><div class="rd-item"><span class="rd-label">实际回厂</span><div class="rd-value">{{ viewData.actualBackDate ? parseTime(viewData.actualBackDate, '{y}-{m}-{d}') : '—' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div><div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Outsource">
import { listOutsource, getOutsource, addOutsource, updateOutsource, delOutsource } from "@/api/mms/outsource";
import { useDetailCard } from '@/composables/useDetailCard'
import { Filter, RefreshLeft, ArrowDown } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const { mms_outsource_status } = proxy.useDict("mms_outsource_status");
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3"])
const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref(""); const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusTabList = computed(() => mms_outsource_status.value ? mms_outsource_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, outsourceNo: undefined, workOrderNo: undefined, supplierName: undefined, status: undefined, params: {} },
  rules: { workOrderNo: [{ required: true, message: "请输入工单编号", trigger: "blur" }], supplierName: [{ required: true, message: "请输入供应商", trigger: "blur" }], outQty: [{ required: true, message: "请输入外协数量", trigger: "blur" }] }
});
const { queryParams, form, rules } = toRefs(data);
function getList() { loading.value = true; listOutsource(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.outsourceNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.supplierName = undefined; queryParams.value.status = undefined; dateRange.value = []; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.outsourceId); single.value = sel.length !== 1; multiple.value = !sel.length; }
function reset() { form.value = { outsourceId: undefined, outsourceNo: undefined, workOrderNo: undefined, opSeq: undefined, processName: undefined, supplierName: undefined, outQty: undefined, unitPrice: undefined, outDate: undefined, planBackDate: undefined, actualBackDate: undefined, status: "0", remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增外协单"; }
function handleUpdate(row) { reset(); getOutsource(row.outsourceId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = "修改外协单"; }); }
function handleView(row) { const id = row.outsourceId || ids.value[0]; getOutsource(id).then(res => { viewData.value = res.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(v => { if (v) { if (form.value.outsourceId != null) { updateOutsource(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addOutsource(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.outsourceId || ids.value; proxy.$modal.confirm('确认删除选中的外协记录？').then(() => delOutsource(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/outsource/export", { ...queryParams.value }, `outsource_${new Date().getTime()}.xlsx`); }
function statusLabel(s) { if (s === null || s === undefined || s === '') return '—'; const i = mms_outsource_status.value?.find(d => d.value == s); return i ? i.label : '—'; }
function badgeClass(s) { return { '0': 'amber', '1': 'blue', '2': 'green' }[s] || 'gray'; }
function statusTabClass(v) { return { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done' }[v] || ''; }
getList();
</script>

<style scoped>
.mms-outsource-page { padding-top: 10px; }
.mms-outsource-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-outsource-page .filter-card { padding: 14px 20px 16px; }
.mms-outsource-page .filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.mms-outsource-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-outsource-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-outsource-page .adv-link { font-size: 14px; color: #64748b; cursor: pointer; display: flex; align-items: center; gap: 4px; }
.mms-outsource-page .adv-link:hover { color: #4f46e5; }
.mms-outsource-page .adv-link .chev { transition: transform .2s; }
.mms-outsource-page .adv-link.is-open .chev { transform: rotate(180deg); }
.mms-outsource-page .filter-bar { display: grid; grid-template-columns: repeat(4, minmax(0,1fr)); gap: 12px 16px; }
.mms-outsource-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-outsource-page .filter-info { font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 6px; }
.mms-outsource-page .filter-buttons { display: flex; gap: 8px; }
.mms-outsource-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-outsource-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-outsource-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-outsource-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-outsource-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-outsource-page .field .control :deep(.el-select) { width: 100%; }
.mms-outsource-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; min-height: 34px; height: 34px; }
.mms-outsource-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-outsource-page .toolbar .left, .mms-outsource-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-outsource-page .toolbar-divider { width: 1px; height: 18px; background: #e2e8f0; margin: 0 4px; }
.mms-outsource-page .table-wrap { overflow-x: auto; }
.mms-outsource-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.mms-outsource-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.mms-outsource-page .badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; } .mms-outsource-page .badge.amber .dot { background: #f59e0b; }
.mms-outsource-page .badge.blue { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; } .mms-outsource-page .badge.blue .dot { background: #3b82f6; }
.mms-outsource-page .badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; } .mms-outsource-page .badge.green .dot { background: #10b981; }
.mms-outsource-page .badge.gray { background: #f1f5f9; color: #64748b; border-color: #e2e8f0; } .mms-outsource-page .badge.gray .dot { background: #94a3b8; }
.mms-outsource-page .status-tabs { display: flex; align-items: center; gap: 12px; padding: 6px 10px 6px 12px; border-bottom: 1px solid #e2e8f0; background: #fff; }
.mms-outsource-page .tabs-track { display: flex; align-items: center; gap: 4px; flex: 1; overflow-x: auto; }
.mms-outsource-page .status-tab { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: 6px; font-size: 14px; color: #64748b; cursor: pointer; white-space: nowrap; border: 1px solid transparent; background: transparent; }
.mms-outsource-page .status-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: #cbd5e1; }
.mms-outsource-page .status-tab:hover { background: #f8fafc; color: #334155; }
.mms-outsource-page .status-tab.is-active { background: #eef2ff; color: #4338ca; font-weight: 600; border-color: #c7d2fe; }
.mms-outsource-page .status-tab.is-active .dot { background: #6366f1; }
.mms-outsource-page .rd-page { max-width: 760px; margin: 0 auto; }
.mms-outsource-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.mms-outsource-page .rd-item { display: flex; align-items: center; gap: 12px; }
.mms-outsource-page .rd-item--full { grid-column: 1 / -1; }
.mms-outsource-page .rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.mms-outsource-page .rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.mms-outsource-page .rd-value--muted { color: #9ca3af; font-style: italic; }
.mms-outsource-page .rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.mms-outsource-page .rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.mms-outsource-page .rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; }
.mms-outsource-page .rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; }
.mms-outsource-page .rd-detail-header-sub { display: flex; align-items: center; gap: 8px; }
.mms-outsource-page .rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); }
.mms-outsource-page .rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); }
.mms-outsource-page .rd-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 12px; overflow: hidden; }
.mms-outsource-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; cursor: pointer; background: #f8fafc; border-bottom: 1px solid #e5e7eb; }
.mms-outsource-page .rd-card-header:hover { background: #f1f5f9; }
.mms-outsource-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-outsource-page .rd-card-icon { display: flex; align-items: center; color: #6366f1; }
.mms-outsource-page .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; cursor: pointer; color: #94a3b8; transition: transform .2s; }
.mms-outsource-page .rd-collapse-btn.is-collapsed { transform: rotate(180deg); }
.mms-outsource-page .rd-card-body { padding: 16px; }
.mms-outsource-page .text-muted { color: #94a3b8; }
@media (max-width: 1100px) { .mms-outsource-page .filter-bar { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 720px) { .mms-outsource-page .filter-bar { grid-template-columns: 1fr; } }
</style>
