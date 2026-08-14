<template>
  <div class="app-container">
    <!-- 追溯查询区 -->
    <el-card shadow="never" class="mb10">
      <template #header><span>质量追溯查询</span></template>
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
        <el-form-item label="追溯方向" prop="direction">
          <el-radio-group v-model="queryParams.direction">
            <el-radio-button label="backward">反向追溯（成品→原料）</el-radio-button>
            <el-radio-button label="forward">正向追溯（原料→成品）</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable style="width: 240px" @keyup.enter="handleTrace" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleTrace" :loading="loading">开始追溯</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 追溯结果 -->
    <el-card shadow="never" v-if="traceData">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>追溯结果{{ traceData.remark ? '（' + traceData.remark + '）' : '' }}</span>
          <el-button type="warning" plain icon="Printer" @click="handlePrint" v-hasPermi="['qms:trace:report']">打印/导出追溯报告</el-button>
        </div>
      </template>

      <!-- 统计信息 -->
      <el-descriptions :column="4" border size="small" class="mb15">
        <el-descriptions-item label="追溯批次">{{ queryParams.direction === 'forward' ? traceData.parentBatchNo : traceData.childBatchNo }}</el-descriptions-item>
        <el-descriptions-item label="追溯方向">{{ queryParams.direction === 'forward' ? '正向（原料→成品）' : '反向（成品→原料）' }}</el-descriptions-item>
        <el-descriptions-item label="关联节点数">{{ countNodes(traceData) }}</el-descriptions-item>
        <el-descriptions-item label="追溯时间">{{ traceData.traceTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 追溯树 -->
      <el-table :data="traceData.children || []" row-key="id" :tree-props="{ children: 'children' }" border default-expand-all>
        <el-table-column :label="queryParams.direction === 'forward' ? '产出批次' : '投入批次'" prop="queryParams.direction === 'forward' ? childBatchNo : parentBatchNo'" min-width="160">
          <template #default="scope">
            <el-tag :type="scope.row.breakFlag === 1 ? 'danger' : 'success'" size="small">
              {{ queryParams.direction === 'forward' ? (scope.row.childBatchNo || '断点') : (scope.row.parentBatchNo || '断点') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="批次号" min-width="140">
          <template #default="scope">
            {{ queryParams.direction === 'forward' ? (scope.row.parentBatchNo || '-') : (scope.row.childBatchNo || '-') }}
          </template>
        </el-table-column>
        <el-table-column label="物料" min-width="180">
          <template #default="scope">
            <span v-if="scope.row.materialCode">{{ scope.row.materialCode }} / {{ scope.row.materialName }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="工单号" prop="workOrderNo" min-width="120" />
        <el-table-column label="工序" prop="opName" min-width="100" />
        <el-table-column label="供应商" prop="supplierName" min-width="140" />
        <el-table-column label="投入数量" prop="qtyIn" min-width="90" align="center" />
        <el-table-column label="产出数量" prop="qtyOut" min-width="90" align="center" />
        <el-table-column label="来源" prop="sourceType" min-width="80">
          <template #default="scope">
            <dict-tag :options="qms_genealogy_source" :value="scope.row.sourceType" />
          </template>
        </el-table-column>
        <el-table-column label="操作员" prop="operator" min-width="80" />
        <el-table-column label="谱系时间" prop="traceTime" min-width="150" />
        <el-table-column label="断点" min-width="60" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.breakFlag === 1" type="danger" size="small">断点</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="关联记录" min-width="120" align="center">
          <template #default="scope">
            <el-badge :value="(scope.row.inspectTasks?.length || 0) + (scope.row.ncrList?.length || 0)" :hidden="!scope.row.inspectTasks?.length && !scope.row.ncrList?.length" type="primary">
              <el-button text size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            </el-badge>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 空提示 -->
    <el-empty v-else-if="!loading" description="请输入批次号并选择追溯方向后开始追溯" />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" title="关联质量记录" width="800px" append-to-body>
      <el-tabs v-model="detailTab">
        <el-tab-pane label="检验记录" name="insp">
          <el-table :data="currentDetail.inspectTasks || []" border size="small">
            <el-table-column label="任务编号" prop="taskNo" min-width="120" />
            <el-table-column label="检验类型" prop="taskType" min-width="80" />
            <el-table-column label="物料" prop="materialName" min-width="150" />
            <el-table-column label="批次" prop="batchNo" min-width="120" />
            <el-table-column label="判定结果" min-width="80">
              <template #default="s">
                <el-tag :type="s.row.inspectResult === '1' ? 'success' : 'danger'" size="small">{{ s.row.inspectResult === '1' ? '合格' : '不合格' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="检验时间" prop="judgeTime" min-width="150" />
          </el-table>
          <el-empty v-if="!currentDetail.inspectTasks?.length" description="无检验记录" :image-size="60" />
        </el-tab-pane>
        <el-tab-pane label="NCR记录" name="ncr">
          <el-table :data="currentDetail.ncrList || []" border size="small">
            <el-table-column label="NCR编号" prop="ncrNo" min-width="120" />
            <el-table-column label="物料" prop="materialName" min-width="150" />
            <el-table-column label="缺陷等级" min-width="80">
              <template #default="s">
                <el-tag :type="parseInt(s.row.defectLevel) <= 2 ? 'danger' : 'warning'" size="small">
                  {{ { '1': '致命', '2': '严重', '3': '一般', '4': '轻微' }[s.row.defectLevel] || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="处置" min-width="80">
              <template #default="s">{{ { '1': '退货', '2': '挑选', '3': '返工', '4': '报废', '5': '让步' }[s.row.disposition] || '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" prop="ncrStatus" min-width="80" />
            <el-table-column label="发现时间" prop="discoverTime" min-width="150" />
          </el-table>
          <el-empty v-if="!currentDetail.ncrList?.length" description="无NCR记录" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup name="QmsTraceSearch">
import { ref, reactive, getCurrentInstance } from 'vue'
import { forwardTrace, backwardTrace } from '@/api/qms/trace'

const { proxy } = getCurrentInstance()
const { qms_genealogy_source } = proxy.useDict('qms_genealogy_source')

const loading = ref(false)
const traceData = ref(null)
const detailOpen = ref(false)
const detailTab = ref('insp')
const currentDetail = ref({})
const queryParams = reactive({ direction: 'backward', batchNo: '' })

function handleTrace() {
  if (!queryParams.batchNo) { proxy.$modal.msgWarning('请输入批次号'); return }
  loading.value = true
  traceData.value = null
  const api = queryParams.direction === 'forward' ? forwardTrace : backwardTrace
  api(queryParams.batchNo).then(res => {
    traceData.value = res.data
    if (!res.data.children?.length) { proxy.$modal.msgWarning('未找到谱系数据，该批次可能未建立谱系关联') }
  }).finally(() => { loading.value = false })
}

function resetQuery() { queryParams.batchNo = ''; traceData.value = null }

function countNodes(node) {
  let count = 0
  if (node.children) { for (const c of node.children) { count++; count += countNodes(c) } }
  return count
}

function handleViewDetail(row) { currentDetail.value = row; detailTab.value = 'insp'; detailOpen.value = true }

function handlePrint() { proxy.$modal.msgSuccess('追溯报告打印功能（PDF导出待对接文件服务）') }
</script>
