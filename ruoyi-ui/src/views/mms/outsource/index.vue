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
          <el-table-column label="外协日期" prop="outDate" width="120" align="center"><template #default="scope">{{ scope.row.outDate ? parseTime(scope.row.outDate, '{y}-{m}-{d}') : '-' }}</template></el-table-column>
          <el-table-column label="计划回厂" prop="planBackDate" width="120" align="center"><template #default="scope">{{ scope.row.planBackDate ? parseTime(scope.row.planBackDate, '{y}-{m}-{d}') : '-' }}</template></el-table-column>
          <el-table-column label="实际回厂" prop="actualBackDate" width="120" align="center"><template #default="scope">{{ scope.row.actualBackDate ? parseTime(scope.row.actualBackDate, '{y}-{m}-{d}') : '-' }}</template></el-table-column>
          <el-table-column label="回厂数量" prop="backQty" width="100" align="center" />
          <el-table-column label="损耗数量" prop="lossQty" width="100" align="center" />
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:outsource:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:outsource:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="外协单号" prop="outsourceNo"><el-input v-model="form.outsourceNo" placeholder="自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="工序序号" prop="opSeq"><el-input-number v-model="form.opSeq" :min="1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工序名称" prop="processName"><el-input v-model="form.processName" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="供应商" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="外协数量" prop="outQty"><el-input-number v-model="form.outQty" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status"><el-option v-for="d in mms_outsource_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="外协日期" prop="outDate"><el-date-picker v-model="form.outDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计划回厂" prop="planBackDate"><el-date-picker v-model="form.planBackDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实际回厂" prop="actualBackDate"><el-date-picker v-model="form.actualBackDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="Outsource">
import { listOutsource, getOutsource, addOutsource, updateOutsource, delOutsource } from "@/api/mms/outsource";
import { Filter, RefreshLeft, ArrowDown } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const { mms_outsource_status } = proxy.useDict("mms_outsource_status");
const dataList = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
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
function submitForm() { proxy.$refs["formRef"].validate(v => { if (v) { if (form.value.outsourceId != null) { updateOutsource(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addOutsource(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.outsourceId || ids.value; proxy.$modal.confirm('确认删除选中的外协记录？').then(() => delOutsource(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/outsource/export", { ...queryParams.value }, `outsource_${new Date().getTime()}.xlsx`); }
function statusLabel(s) { const i = mms_outsource_status.value?.find(d => d.value == s); return i ? i.label : '-'; }
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
@media (max-width: 1100px) { .mms-outsource-page .filter-bar { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 720px) { .mms-outsource-page .filter-bar { grid-template-columns: 1fr; } }
</style>
