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
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
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
          <el-table-column label="结束时间" prop="endTime" width="160" align="center"><template #default="scope">{{ scope.row.endTime ? parseTime(scope.row.endTime) : '-' }}</template></el-table-column>
          <el-table-column label="停机类型" prop="dtType" width="100" align="center" />
          <el-table-column label="停机时长(分)" prop="minutes" width="110" align="center" />
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'red' : 'green'"><span class="dot"></span>{{ scope.row.status === '0' ? '停机中' : '已恢复' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="停机原因" prop="reason" show-overflow-tooltip />
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:downtime:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:downtime:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="停机单号" prop="downtimeNo"><el-input v-model="form.downtimeNo" placeholder="自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产能单元" prop="resourceName"><el-input v-model="form.resourceName" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="停机类型" prop="dtType"><el-input v-model="form.dtType" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">停机中</el-radio><el-radio value="1">已恢复</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="停机原因" prop="reason"><el-input v-model="form.reason" type="textarea" placeholder="请输入" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="停机管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、停机管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是停机管理？</div>
          <div class="highlight-card-body">
            <strong>停机管理（Downtime Management）</strong>是生产管控中记录生产设备/产线停机事件的单据。停机管理记录停机开始/结束时间、停机类型、停机时长和停机原因，支持设备利用率分析和产能损耗统计。<br/><br/>
            停机管理遵循 <strong>MES 设备效率管理规范</strong>，通过停机时长统计支持 OEE（设备综合效率）计算，停机类型分类（计划停机/故障停机/换型停机等）支持根因分析和改善措施制定。
          </div>
        </div>
        <h4>二、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="danger" :hollow="true"><strong>记录停机：</strong>设备/产线停机时点击「新增」创建停机记录，填写开始时间和停机类型</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>记录结束：</strong>停机结束后填写结束时间，系统自动计算停机时长</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>分析统计：</strong>停机数据用于设备利用率分析和产能损耗统计</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Downtime">
import { listDowntime, getDowntime, addDowntime, updateDowntime, delDowntime } from "@/api/mms/downtime";
import { Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const { mms_downtime_status } = proxy.useDict("mms_downtime_status");
const dataList = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
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
@media (max-width: 1100px) { .mms-downtime-page .filter-bar { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 720px) { .mms-downtime-page .filter-bar { grid-template-columns: 1fr; } }
</style>
