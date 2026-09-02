<template>
  <div class="app-container mms-calendar-page">
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
          <label>日期类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.dayType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_calendar_daytype" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工作日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
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
          <span class="status-tab is-active"><span class="dot"></span><span>工厂日历</span></span>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:calendar:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:calendar:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:calendar:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:calendar:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="工作日期" prop="workDate" align="center" width="120">
            <template #default="scope"><span>{{ parseTime(scope.row.workDate, '{y}-{m}-{d}') }}</span></template>
          </el-table-column>
          <el-table-column label="日期类型" prop="dayType" align="center" width="100">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.dayType)"><span class="dot"></span>{{ dayTypeLabel(scope.row.dayType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="可用工时" prop="availHours" align="center" width="100" />
          <el-table-column label="状态" prop="status" align="center" width="80">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '启用' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" show-overflow-tooltip />
          <el-table-column label="创建时间" prop="createTime" align="center" width="160">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:calendar:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:calendar:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日历信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工作日期" prop="workDate"><el-date-picker v-model="form.workDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="日期类型" prop="dayType"><el-select v-model="form.dayType" placeholder="请选择"><el-option v-for="d in mms_calendar_daytype" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="可用工时" prop="availHours"><el-input-number v-model="form.availHours" :min="0" :precision="2" :step="0.5" style="width:100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">启用</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">工厂日历详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.workDate"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ parseTime(viewData.workDate, '{y}-{m}-{d}') }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日历信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">工作日期</span><div class="rd-value">{{ viewData.workDate ? parseTime(viewData.workDate, '{y}-{m}-{d}') : '—' }}</div></div><div class="rd-item"><span class="rd-label">日期类型</span><div class="rd-value"><span v-if="viewData.dayType" class="badge" :class="badgeClass(viewData.dayType)"><span class="dot"></span>{{ dayTypeLabel(viewData.dayType) }}</span><span v-else class="text-muted">—</span></div></div><div class="rd-item"><span class="rd-label">可用工时</span><div class="rd-value">{{ viewData.availHours != null ? viewData.availHours : '—' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status === '0'" class="badge green"><span class="dot"></span>启用</span><span v-else-if="viewData.status === '1'" class="badge gray"><span class="dot"></span>停用</span><span v-else class="text-muted">—</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div><div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="工厂日历业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、工厂日历释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是工厂日历？</div>
          <div class="highlight-card-body">
            <strong>工厂日历（Factory Calendar）</strong>是生产管控中定义工厂工作日和休息日的主数据。工厂日历记录每个日期的工作/休息状态和班次安排，是产能计算、排产优化和交期评估的基础依据。<br/><br/>
            工厂日历遵循 <strong>MES 排产日历管理规范</strong>，通过工作日/休息日/节假日设置支持排产系统排除非工作日，确保生产计划与工厂实际工作时间匹配。
          </div>
        </div>
        <h4>二、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>设置日历：</strong>点击「新增」设置日期的工作/休息状态和班次安排</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>批量维护：</strong>可批量设置一段时间内的工作日和休息日</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>排产引用：</strong>排产系统自动排除休息日和节假日，确保生产计划可行</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Calendar">
import { listCalendar, getCalendar, addCalendar, updateCalendar, delCalendar } from "@/api/mms/calendar";
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_calendar_daytype, mms_status } = proxy.useDict("mms_calendar_daytype", "mms_status");
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","vc0","vc1"])

const dataList = ref([]);
const open = ref(false); const viewOpen = ref(false); const viewData = ref({});
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]); const showStatusHelp = ref(false);

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, dayType: undefined, status: undefined, params: {} },
  rules: {
    workDate: [{ required: true, message: "请选择工作日期", trigger: "change" }],
    dayType: [{ required: true, message: "请选择日期类型", trigger: "change" }],
    availHours: [{ required: true, message: "请输入可用工时", trigger: "blur" }]
  }
});
const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listCalendar(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.dayType = undefined; queryParams.value.status = undefined; dateRange.value = []; proxy.resetForm("queryForm"); handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.calendarId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { calendarId: undefined, workDate: undefined, dayType: undefined, availHours: 8, status: "0", remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增工厂日历"; }
function handleUpdate(row) {
  reset();
  const id = row.calendarId || ids.value[0];
  getCalendar(id).then(response => { form.value = response.data; open.value = true; title.value = "修改工厂日历"; });
}
function handleView(row) { const id = row.calendarId || ids.value[0]; getCalendar(id).then(response => { viewData.value = response.data; viewOpen.value = true; }); }
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.calendarId != null) {
        updateCalendar(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addCalendar(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}
function cancel() { open.value = false; reset(); }
function handleDelete(row) {
  const delIds = row.calendarId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的日历记录？').then(function() { return delCalendar(delIds); }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}
function handleExport() { proxy.download("mms/calendar/export", { ...queryParams.value }, `calendar_${new Date().getTime()}.xlsx`); }
function dayTypeLabel(val) { if (val === null || val === undefined || val === '') return '—'; const item = mms_calendar_daytype.value?.find(d => d.value == val); return item ? item.label : '—'; }
function badgeClass(val) { const map = { '0': 'green', '1': 'amber', '2': 'red' }; return map[val] || 'gray'; }
getList();
</script>

<style scoped>
.mms-calendar-page { padding-top: 10px; }
.mms-calendar-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-calendar-page .filter-card { padding: 14px 20px 16px; }
.mms-calendar-page .filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.mms-calendar-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-calendar-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-calendar-page .adv-link { font-size: 14px; color: #64748b; text-decoration: none; display: flex; align-items: center; gap: 4px; cursor: pointer; }
.mms-calendar-page .adv-link:hover { color: #4f46e5; }
.mms-calendar-page .adv-link .chev { transition: transform .2s; }
.mms-calendar-page .adv-link.is-open .chev { transform: rotate(180deg); }
.mms-calendar-page .filter-bar { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 12px 16px; }
.mms-calendar-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-calendar-page .filter-info { font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 6px; }
.mms-calendar-page .filter-buttons { display: flex; gap: 8px; }
.mms-calendar-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-calendar-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-calendar-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-calendar-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-calendar-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-calendar-page .field .control :deep(.el-select) { width: 100%; }
.mms-calendar-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; min-height: 34px; height: 34px; }
.mms-calendar-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-calendar-page .toolbar .left, .mms-calendar-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-calendar-page .toolbar-divider { width: 1px; height: 18px; background: #e2e8f0; margin: 0 4px; }
.mms-calendar-page .table-wrap { overflow-x: auto; }
.mms-calendar-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.mms-calendar-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.mms-calendar-page .badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; } .mms-calendar-page .badge.amber .dot { background: #f59e0b; }
.mms-calendar-page .badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; } .mms-calendar-page .badge.green .dot { background: #10b981; }
.mms-calendar-page .badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; } .mms-calendar-page .badge.red .dot { background: #ef4444; }
.mms-calendar-page .badge.gray { background: #f1f5f9; color: #64748b; border-color: #e2e8f0; } .mms-calendar-page .badge.gray .dot { background: #94a3b8; }
.mms-calendar-page .rd-page { max-width: 620px; margin: 0 auto; }
.mms-calendar-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.mms-calendar-page .rd-item { display: flex; align-items: center; gap: 12px; }
.mms-calendar-page .rd-item--full { grid-column: 1 / -1; }
.mms-calendar-page .rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.mms-calendar-page .rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.mms-calendar-page .rd-value--muted { color: #9ca3af; font-style: italic; }
.mms-calendar-page .rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.mms-calendar-page .rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.mms-calendar-page .rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; }
.mms-calendar-page .rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; }
.mms-calendar-page .rd-detail-header-sub { display: flex; align-items: center; gap: 8px; }
.mms-calendar-page .rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); }
.mms-calendar-page .rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); }
.mms-calendar-page .rd-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 12px; overflow: hidden; }
.mms-calendar-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; cursor: pointer; background: #f8fafc; border-bottom: 1px solid #e5e7eb; }
.mms-calendar-page .rd-card-header:hover { background: #f1f5f9; }
.mms-calendar-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-calendar-page .rd-card-icon { display: flex; align-items: center; color: #6366f1; }
.mms-calendar-page .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; cursor: pointer; color: #94a3b8; transition: transform .2s; }
.mms-calendar-page .rd-collapse-btn.is-collapsed { transform: rotate(180deg); }
.mms-calendar-page .rd-card-body { padding: 16px; }
.mms-calendar-page .text-muted { color: #94a3b8; }
@media (max-width: 900px) { .mms-calendar-page .filter-bar { grid-template-columns: 1fr; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
