<template>
  <div class="report-container">
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="24" style="text-align: right;">
        <el-button type="primary" icon="Printer" @click="handlePrint">打印报告</el-button>
        <el-button icon="Close" @click="handleClose">关闭</el-button>
      </el-col>
    </el-row>

    <div v-loading="loading" class="report-content" id="printArea">
      <div v-if="reportData" class="report-paper">
        <!-- 报告标题 -->
        <div class="report-header">
          <h1>质量检验报告</h1>
          <div class="report-subtitle">INSPECTION REPORT</div>
        </div>

        <!-- 任务基本信息 -->
        <table class="report-table report-table--info">
          <tr><th colspan="4">任务信息</th></tr>
          <tr>
            <th width="15%">任务编号</th><td>{{ reportData.taskNo }}</td>
            <th width="15%">检验类型</th><td><dict-tag :options="qms_insp_type" :value="reportData.taskType" /></td>
          </tr>
          <tr>
            <th>来源单号</th><td>{{ reportData.sourceNo || '-' }}</td>
            <th>来源类型</th><td>{{ reportData.sourceType || '-' }}</td>
          </tr>
          <tr>
            <th>物料编码</th><td>{{ reportData.materialCode }}</td>
            <th>物料名称</th><td>{{ reportData.materialName }}</td>
          </tr>
          <tr>
            <th>供应商</th><td>{{ reportData.supplierName || '-' }}</td>
            <th>批次号</th><td>{{ reportData.batchNo || '-' }}</td>
          </tr>
          <tr>
            <th>送检数量</th><td>{{ reportData.inspectQty }}</td>
            <th>抽样数量</th><td>{{ reportData.sampleSize }} (样本量)</td>
          </tr>
          <tr>
            <th>AQL等级</th><td>{{ reportData.aqlLevel }}</td>
            <th>严格度</th><td>{{ reportData.inspectLevel === '1' ? '正常' : reportData.inspectLevel === '2' ? '加严' : '放宽' }}</td>
          </tr>
          <tr>
            <th>接收数(Ac)</th><td>{{ reportData.acVal }}</td>
            <th>拒收数(Re)</th><td>{{ reportData.reVal }}</td>
          </tr>
        </table>

        <!-- 检验明细 -->
        <table class="report-table report-table--items">
          <tr><th colspan="6">检验明细</th></tr>
          <tr>
            <th width="8%">序号</th>
            <th width="25%">检验项目</th>
            <th width="15%">规格要求</th>
            <th width="15%">实测值</th>
            <th width="12%">单项判定</th>
            <th>缺陷代码</th>
          </tr>
          <tr v-for="(item, index) in reportData.itemList" :key="index">
            <td>{{ index + 1 }}</td>
            <td>{{ item.stdName }}</td>
            <td>{{ item.specReq || '-' }}</td>
            <td>{{ item.measuredVal }}</td>
            <td><dict-tag :options="qms_insp_result" :value="item.itemResult" /></td>
            <td>{{ item.defectCode || '-' }}</td>
          </tr>
        </table>

        <!-- 判定结果 -->
        <table class="report-table report-table--result">
          <tr><th colspan="4">判定结果</th></tr>
          <tr>
            <th width="20%">最高缺陷等级</th>
            <td width="30%">
              <dict-tag :options="qms_defect_level" :value="reportData.defectLevel" />
            </td>
            <th width="20%">批量判定</th>
            <td width="30%">
              <dict-tag :options="qms_insp_result" :value="reportData.inspectResult" />
            </td>
          </tr>
          <tr>
            <th>检验员</th><td>{{ reportData.inspectorName }}</td>
            <th>检验时间</th><td>{{ parseTime(reportData.inspectTime) }}</td>
          </tr>
          <tr>
            <th>判定时间</th><td colspan="3">{{ parseTime(reportData.judgeTime) }}</td>
          </tr>
        </table>

        <!-- 电子签名区 -->
        <div class="report-esig">
          <h3>电子签名记录</h3>
          <table class="report-table">
            <tr><th>签名含义</th><th>签名用户</th><th>签名时间</th><th>签名意见</th><th>签名结果</th></tr>
            <tr v-if="reportData.esigList && reportData.esigList.length > 0" v-for="esig in reportData.esigList" :key="esig.esigId">
              <td>{{ esig.sigMeaning }}</td>
              <td>{{ esig.nickName }} ({{ esig.userName }})</td>
              <td>{{ parseTime(esig.sigTime) }}</td>
              <td>{{ esig.sigOpinion || '-' }}</td>
              <td>{{ esig.sigResult === 'pass' ? '通过' : esig.sigResult === 'fail' ? '不通过' : esig.sigResult }}</td>
            </tr>
            <tr v-else><td colspan="5" style="text-align:center;">无电子签名记录</td></tr>
          </table>
        </div>

        <!-- 页脚 -->
        <div class="report-footer">
          <div class="report-footer__left">
            <span>生成时间：{{ parseTime(new Date()) }}</span>
          </div>
          <div class="report-footer__right">
            <span>质量管理系统（QMS）生成</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="QmsInspTaskReport">
import { getTaskReport } from '@/api/qms/task'
import { useRoute, useRouter } from 'vue-router'

const { proxy } = getCurrentInstance()
const { qms_insp_type, qms_insp_result, qms_defect_level } = proxy.useDict('qms_insp_type', 'qms_insp_result', 'qms_defect_level')

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const reportData = ref(null)

function loadData() {
  const taskId = route.query.taskId
  if (!taskId)
  {
    proxy.$modal.msgError("任务ID不能为空")
    return
  }
  loading.value = true
  getTaskReport(taskId).then(response =>
  {
    reportData.value = response.data
    loading.value = false
  }).catch(() =>
  {
    loading.value = false
  })
}

function handlePrint()
{
  window.print()
}

function handleClose()
{
  router.back()
}

loadData()
</script>

<style scoped>
.report-container { background: #f5f5f5; padding: 20px; min-height: 100vh; }
.report-content { background: #fff; border-radius: 8px; padding: 40px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.report-paper { max-width: 800px; margin: 0 auto; }
.report-header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 20px; margin-bottom: 20px; }
.report-header h1 { font-size: 28px; font-weight: bold; margin: 0 0 8px 0; color: #333; }
.report-subtitle { font-size: 14px; color: #666; letter-spacing: 2px; }

.report-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 14px; }
.report-table th, .report-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
.report-table th { background: #f5f7fa; font-weight: 600; color: #333; }
.report-table td { color: #666; }

.report-table--info th { width: 15%; background: #fafafa; }
.report-table--result th { background: #fafafa; }
.report-table--items th { background: #fafafa; text-align: center; }
.report-table--items td:first-child, .report-table--items td:nth-child(5), .report-table--items td:nth-child(6) { text-align: center; }

.report-esig { margin-top: 30px; }
.report-esig h3 { font-size: 16px; font-weight: 600; color: #333; margin: 0 0 10px 0; }

.report-footer { display: flex; justify-content: space-between; margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; color: #999; font-size: 12px; }

@media print {
  .report-container { background: #fff; padding: 0; }
  .report-content { padding: 20px; box-shadow: none; }
  .el-button { display: none !important; }
  .el-row[style*="margin-bottom"] { display: none; }
}
</style>