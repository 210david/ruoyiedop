<template>
  <div class="app-container mms-oee-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>产能单元</label><div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>日期范围</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="toolbar">
        <div class="left"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:oee:export']">导出</el-button></div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="dataList" border class="app-table">
          <el-table-column label="日期" prop="snapshotDate" width="120" align="center"><template #default="scope">{{ parseTime(scope.row.snapshotDate, '{y}-{m}-{d}') }}</template></el-table-column>
          <el-table-column label="产能单元" prop="resourceName" width="140" show-overflow-tooltip />
          <el-table-column label="工单编号" prop="workOrderNo" width="140" />
          <el-table-column label="计划工时" prop="plannedHours" width="100" align="center" />
          <el-table-column label="运行工时" prop="runHours" width="100" align="center" />
          <el-table-column label="停机工时" prop="downtimeHours" width="100" align="center" />
          <el-table-column label="可用率" prop="availability" width="90" align="center"><template #default="scope">{{ (scope.row.availability * 100).toFixed(2) }}%</template></el-table-column>
          <el-table-column label="表现率" prop="performance" width="90" align="center"><template #default="scope">{{ (scope.row.performance * 100).toFixed(2) }}%</template></el-table-column>
          <el-table-column label="质量率" prop="qualityRate" width="90" align="center"><template #default="scope">{{ (scope.row.qualityRate * 100).toFixed(2) }}%</template></el-table-column>
          <el-table-column label="OEE" prop="oee" width="90" align="center"><template #default="scope"><span class="badge" :class="oeeBadgeClass(scope.row.oee)"><span class="dot"></span>{{ (scope.row.oee * 100).toFixed(2) }}%</span></template></el-table-column>
          <el-table-column label="合格数量" prop="goodQty" width="100" align="center" />
          <el-table-column label="不良数量" prop="defectQty" width="100" align="center" />
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>
<script setup name="Oee">
import { listOee } from "@/api/mms/oee";
import { Filter, RefreshLeft } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const dataList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0); const dateRange = ref([]);
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, resourceName: undefined, workOrderNo: undefined, params: {} } });
const { queryParams } = toRefs(data);
function getList() { loading.value = true; listOee(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.resourceName = undefined; queryParams.value.workOrderNo = undefined; dateRange.value = []; handleQuery(); }
function handleExport() { proxy.download("mms/oee/export", { ...queryParams.value }, `oee_${new Date().getTime()}.xlsx`); }
function oeeBadgeClass(val) { if (val >= 0.85) return 'green'; if (val >= 0.6) return 'amber'; return 'red'; }
getList();
</script>
<style scoped>
.mms-oee-page { padding-top: 10px; }
.mms-oee-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-oee-page .filter-card { padding: 14px 20px 16px; }
.mms-oee-page .filter-head { margin-bottom: 12px; }
.mms-oee-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-oee-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-oee-page .filter-bar { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 12px 16px; }
.mms-oee-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-oee-page .filter-info { font-size: 13px; color: #64748b; }
.mms-oee-page .filter-buttons { display: flex; gap: 8px; }
.mms-oee-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-oee-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-oee-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-oee-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-oee-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-oee-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-oee-page .toolbar .left, .mms-oee-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-oee-page .table-wrap { overflow-x: auto; }
.mms-oee-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.mms-oee-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.mms-oee-page .badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; } .mms-oee-page .badge.green .dot { background: #10b981; }
.mms-oee-page .badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; } .mms-oee-page .badge.amber .dot { background: #f59e0b; }
.mms-oee-page .badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; } .mms-oee-page .badge.red .dot { background: #ef4444; }
</style>
