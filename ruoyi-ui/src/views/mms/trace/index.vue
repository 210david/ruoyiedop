<template>
  <div class="app-container mms-trace-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>产出批次</label><div class="control"><el-input v-model="queryParams.batchOut" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>投入批次</label><div class="control"><el-input v-model="queryParams.batchIn" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>物料编码</label><div class="control"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="toolbar">
        <div class="left"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:trace:export']">导出</el-button></div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="dataList" border class="app-table">
          <el-table-column label="产出批次" prop="batchOut" width="160" />
          <el-table-column label="投入批次" prop="batchIn" width="160" />
          <el-table-column label="工序顺序号" prop="opSeq" width="100" align="center" />
          <el-table-column label="物料编码" prop="materialCode" width="160" />
          <el-table-column label="使用数量" prop="qtyUsed" width="120" align="center" />
          <el-table-column label="创建人" prop="createBy" width="120" align="center" />
          <el-table-column label="创建时间" prop="createTime" width="180" align="center"><template #default="scope">{{ parseTime(scope.row.createTime) }}</template></el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>
<script setup name="Trace">
import { listTrace } from "@/api/mms/trace";
import { Filter, RefreshLeft } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const dataList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0);
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, batchOut: undefined, batchIn: undefined, materialCode: undefined, params: {} } });
const { queryParams } = toRefs(data);
function getList() { loading.value = true; listTrace(queryParams.value).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.batchOut = undefined; queryParams.value.batchIn = undefined; queryParams.value.materialCode = undefined; handleQuery(); }
function handleExport() { proxy.download("mms/trace/export", { ...queryParams.value }, `trace_${new Date().getTime()}.xlsx`); }
getList();
</script>
<style scoped>
.mms-trace-page { padding-top: 10px; }
.mms-trace-page .surface { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; box-shadow: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); overflow: hidden; margin-bottom: 8px; }
.mms-trace-page .filter-card { padding: 14px 20px 16px; }
.mms-trace-page .filter-head { margin-bottom: 12px; }
.mms-trace-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-trace-page .filter-title .glyph { width: 4px; height: 14px; background: #4f46e5; border-radius: 2px; }
.mms-trace-page .filter-bar { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 12px 16px; }
.mms-trace-page .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e2e8f0; }
.mms-trace-page .filter-info { font-size: 13px; color: #64748b; }
.mms-trace-page .filter-buttons { display: flex; gap: 8px; }
.mms-trace-page .field { display: flex; flex-direction: column; gap: 6px; }
.mms-trace-page .field label { font-size: 14px; font-weight: 500; color: #334155; }
.mms-trace-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; }
.mms-trace-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.mms-trace-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; height: 34px; line-height: 34px; }
.mms-trace-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.mms-trace-page .toolbar .left, .mms-trace-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.mms-trace-page .table-wrap { overflow-x: auto; }
@media (max-width: 900px) { .mms-trace-page .filter-bar { grid-template-columns: 1fr; } }
</style>
