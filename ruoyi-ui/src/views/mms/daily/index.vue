<template>
  <div class="app-container mms-daily-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>日期范围</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info">支持日期范围筛选</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="toolbar">
        <div class="left"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:daily:export']">导出</el-button></div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="dataList" border class="app-table">
          <el-table-column label="统计日期" prop="statDate" width="120" align="center"><template #default="scope">{{ parseTime(scope.row.statDate, '{y}-{m}-{d}') }}</template></el-table-column>
          <el-table-column label="工单数" prop="workOrderCount" width="100" align="center" />
          <el-table-column label="已完工数" prop="completedCount" width="100" align="center" />
          <el-table-column label="在制数" prop="inProgressCount" width="100" align="center" />
          <el-table-column label="异常数" prop="abnormalCount" width="100" align="center">
            <template #default="scope"><span v-if="scope.row.abnormalCount > 0" class="badge red"><span class="dot"></span>{{ scope.row.abnormalCount }}</span><span v-else>{{ scope.row.abnormalCount }}</span></template>
          </el-table-column>
          <el-table-column label="计划数量" prop="totalPlanQty" width="120" align="center" />
          <el-table-column label="合格数量" prop="totalGoodQty" width="120" align="center" />
          <el-table-column label="不良数量" prop="totalDefectQty" width="120" align="center" />
          <el-table-column label="平均OEE" prop="avgOee" width="100" align="center"><template #default="scope">{{ (scope.row.avgOee * 100).toFixed(2) }}%</template></el-table-column>
          <el-table-column label="报工次数" prop="reportCount" width="100" align="center" />
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>
<script setup name="Daily">
import { listDaily } from "@/api/mms/daily";
const { proxy } = getCurrentInstance();
const dataList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0); const dateRange = ref([]);
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, params: {} } });
const { queryParams } = toRefs(data);
function getList() { loading.value = true; listDaily(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { dateRange.value = []; handleQuery(); }
function handleExport() { proxy.download("mms/daily/export", { ...queryParams.value }, `daily_${new Date().getTime()}.xlsx`); }
getList();
</script>
<style scoped>
.mms-daily-page { padding-top: 10px; }
.mms-daily-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-daily-page .filter-card { padding: 14px 20px 16px; }
.mms-daily-page .filter-head { margin-bottom: 12px; }
.mms-daily-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-daily-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-daily-page .filter-bar { display: grid; grid-template-columns: 1fr; gap: 12px 16px; max-width: 400px; }
.mms-daily-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-daily-page .filter-info { font-size: 13px; color: #64748b; }
.mms-daily-page .filter-buttons { display: flex; gap: 8px; }
.mms-daily-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-daily-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-daily-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-daily-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-daily-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-daily-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-daily-page .toolbar .left, .mms-daily-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-daily-page .table-wrap { overflow-x: auto; }
.mms-daily-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.mms-daily-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.mms-daily-page .badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; } .mms-daily-page .badge.red .dot { background: #ef4444; }
</style>
