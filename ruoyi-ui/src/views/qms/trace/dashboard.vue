<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>已关联工单</span></template>
          <div class="big-num" style="color:#67c23a">{{ data.linkedOrders || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>断点工单</span></template>
          <div class="big-num" style="color:#f56c6c">{{ data.breakOrders || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>谱系完整率</span></template>
          <div class="big-num" :style="{color: (data.completenessRate||0) >= 98 ? '#67c23a' : '#e6a23c'}">{{ data.completenessRate || 0 }}%</div>
          <div style="text-align:center;font-size:12px;color:#909399;margin-top:8px">目标 ≥98%，差距 {{ data.gap || 0 }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt15">
      <template #header><span>断点清单</span></template>
      <el-table :data="breakList" border size="small">
        <el-table-column label="投入批次" prop="parentBatchNo" min-width="130" />
        <el-table-column label="工单号" prop="workOrderNo" min-width="110" />
        <el-table-column label="物料" min-width="150"><template #default="s">{{ s.row.materialCode }} / {{ s.row.materialName }}</template></el-table-column>
        <el-table-column label="断点原因" prop="breakReason" min-width="200" />
        <el-table-column label="创建时间" prop="createTime" min-width="145" />
      </el-table>
      <el-empty v-if="!breakList.length" description="无断点数据" />
    </el-card>
  </div>
</template>

<script setup name="QmsTraceDashboard">
import { ref, getCurrentInstance } from 'vue'
import { traceCompleteness, traceBreakList } from '@/api/qms/trace'

const { proxy } = getCurrentInstance()
const data = ref({})
const breakList = ref([])

traceCompleteness().then(res => { data.value = res.data })
traceBreakList().then(res => { breakList.value = res.data })
</script>

<style scoped>
.big-num { font-size: 42px; font-weight: bold; text-align: center; }
</style>
