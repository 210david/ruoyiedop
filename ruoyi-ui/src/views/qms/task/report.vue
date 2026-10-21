<template>
  <div class="report-standalone">
    <!-- 工具栏（打印时隐藏） -->
    <div class="report-toolbar no-print">
      <div class="report-toolbar__left">
        <span class="report-toolbar__title">质量检验报告</span>
      </div>
      <div class="report-toolbar__right">
        <el-button type="primary" icon="Printer" @click="handlePrint" :disabled="!reportData">打印报告</el-button>
        <el-button type="success" icon="Download" @click="handleDownloadPDF" :loading="pdfLoading" :disabled="!reportData">下载PDF</el-button>
        <el-button icon="Close" @click="handleClose">关闭</el-button>
      </div>
    </div>

    <!-- 报告内容区域 -->
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
            <th width="15%">检验类型</th><td>{{ dictLabel(qms_insp_type, reportData.taskType) }}</td>
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
            <th>抽样数量<el-tooltip content="样本量(n)，即从检验批中抽取的样本数量，由批量大小和检验水平查表确定" placement="top"><el-icon class="report-tip"><QuestionFilled /></el-icon></el-tooltip></th><td>{{ reportData.sampleSize }} (样本量)</td>
          </tr>
          <tr>
            <th>AQL等级<el-tooltip content="AQL（Acceptable Quality Limit）即可接受质量限，表示检验批中允许的最大不合格品率，以百分比表示" placement="top"><el-icon class="report-tip"><QuestionFilled /></el-icon></el-tooltip></th><td>{{ reportData.aqlLevel ? reportData.aqlLevel + '%' : '-' }}</td>
            <th>严格度<el-tooltip content="抽样检验的严格度等级，分为：正常检验、加严检验、放宽检验" placement="top"><el-icon class="report-tip"><QuestionFilled /></el-icon></el-tooltip></th><td>{{ reportData.inspectLevel === '1' ? '正常' : reportData.inspectLevel === '2' ? '加严' : '放宽' }}</td>
          </tr>
          <tr>
            <th>接收数(Ac)<el-tooltip content="Ac为接收数（Acceptance Number），即抽样检验中允许的最大不合格品数，当样本中的不合格品数≤Ac时判定批合格" placement="top"><el-icon class="report-tip"><QuestionFilled /></el-icon></el-tooltip></th><td>{{ reportData.acVal }}</td>
            <th>拒收数(Re)<el-tooltip content="Re为拒收数（Rejection Number），即抽样检验中拒收的最小不合格品数，当样本中的不合格品数≥Re时判定批不合格" placement="top"><el-icon class="report-tip"><QuestionFilled /></el-icon></el-tooltip></th><td>{{ reportData.reVal }}</td>
          </tr>
        </table>

        <!-- 检验明细 -->
        <table class="report-table report-table--items">
          <tr><th colspan="7">检验明细</th></tr>
          <tr>
            <th width="8%">序号</th>
            <th width="22%">检验项目</th>
            <th width="12%">规格要求</th>
            <th width="12%">实测值</th>
            <th width="10%">单项判定</th>
            <th width="12%">缺陷代码</th>
            <th width="10%">缺陷数量</th>
          </tr>
          <tr v-for="(item, index) in reportData.itemList" :key="index">
            <td>{{ index + 1 }}</td>
            <td>{{ item.stdName }}</td>
            <td>{{ item.specReq || '-' }}</td>
            <td>{{ item.measuredVal }}</td>
            <td>{{ dictLabel(qms_insp_result, item.itemResult) }}</td>
            <td>{{ item.defectCode || '-' }}</td>
            <td>{{ item.defectQty != null ? item.defectQty : '-' }}</td>
          </tr>
        </table>

        <!-- 判定结果 -->
        <table class="report-table report-table--result">
          <tr><th colspan="4">判定结果</th></tr>
          <tr>
            <th width="20%">最高缺陷等级<el-tooltip content="本次检验中所有不合格缺陷中最高（最严重）的缺陷等级，通常分为：致命缺陷、严重缺陷、一般缺陷、轻微缺陷" placement="top"><el-icon class="report-tip"><QuestionFilled /></el-icon></el-tooltip></th>
            <td width="30%">{{ dictLabel(qms_defect_level, reportData.defectLevel) }}</td>
            <th width="20%">批量判定</th>
            <td width="30%">{{ dictLabel(qms_insp_result, reportData.inspectResult) }}</td>
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
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { qms_insp_type, qms_insp_result, qms_defect_level } = proxy.useDict('qms_insp_type', 'qms_insp_result', 'qms_defect_level')

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const reportData = ref(null)
const pdfLoading = ref(false)

function dictLabel(options, value) {
  if (!options || value === null || value === undefined || value === '') return '-'
  const item = options.find(d => d.value == value)
  return item ? item.label : String(value)
}

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

async function handleDownloadPDF()
{
  if (!reportData.value) return
  pdfLoading.value = true
  try
  {
    const printArea = document.getElementById('printArea')
    const canvas = await html2canvas(printArea, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false
    })
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const pdfWidth = pdf.internal.pageSize.getWidth()
    const pdfHeight = pdf.internal.pageSize.getHeight()
    const imgWidth = pdfWidth
    const imgHeight = (canvas.height * imgWidth) / canvas.width

    let heightLeft = imgHeight
    let position = 0

    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pdfHeight

    while (heightLeft >= 0)
    {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pdfHeight
    }

    pdf.save(`质量检验报告_${reportData.value.taskNo}.pdf`)
    proxy.$modal.msgSuccess('PDF下载成功')
  }
  catch (error)
  {
    console.error('PDF生成失败:', error)
    proxy.$modal.msgError('PDF生成失败，请重试')
  }
  finally
  {
    pdfLoading.value = false
  }
}

loadData()
</script>

<style scoped>
/* ===== 独立页面容器 ===== */
.report-standalone { background: #f0f2f5; min-height: 100vh; }

/* ===== 工具栏 ===== */
.report-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 24px; background: #fff; border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04); position: sticky; top: 0; z-index: 10;
}
.report-toolbar__title { font-size: 18px; font-weight: 600; color: #1f1f1f; }
.report-toolbar__right { display: flex; gap: 8px; }

/* ===== 报告内容 ===== */
.report-content { background: #fff; max-width: 960px; margin: 24px auto; padding: 40px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.report-paper { max-width: 100%; margin: 0 auto; }

/* ===== 报告标题 ===== */
.report-header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 20px; margin-bottom: 20px; }
.report-header h1 { font-size: 28px; font-weight: bold; margin: 0 0 8px 0; color: #333; }
.report-subtitle { font-size: 14px; color: #666; letter-spacing: 2px; }

/* ===== 报告表格 ===== */
.report-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 14px; }
.report-table th, .report-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
.report-table th { background: #f5f7fa; font-weight: 600; color: #333; }
.report-table td { color: #666; }

.report-table--info th { width: 15%; background: #fafafa; }
.report-table--result th { background: #fafafa; }
.report-table--items th { background: #fafafa; text-align: center; }
.report-table--items td:first-child, .report-table--items td:nth-child(5), .report-table--items td:nth-child(6) { text-align: center; }

/* ===== 电子签名 ===== */
.report-esig { margin-top: 30px; }
.report-esig h3 { font-size: 16px; font-weight: 600; color: #333; margin: 0 0 10px 0; }

/* ===== 页脚 ===== */
.report-footer { display: flex; justify-content: space-between; margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; color: #999; font-size: 12px; }

/* ===== 说明提示图标 ===== */
.report-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; vertical-align: middle; }
.report-tip:hover { color: #909399; }

/* ===== 打印样式 ===== */
@media print {
  /* 隐藏工具栏 */
  .no-print { display: none !important; }
  /* 隐藏说明提示图标 */
  .report-tip { display: none !important; }
  /* 重置页面背景和布局 */
  .report-standalone { background: #fff; }
  .report-content { max-width: none; margin: 0; padding: 20px; box-shadow: none; border-radius: 0; }
  /* 确保表格颜色在打印中可见 */
  .report-table th { background: #f5f7fa !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
  .report-table--info th, .report-table--result th, .report-table--items th { background: #fafafa !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
}
</style>
