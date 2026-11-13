<template>
  <div class="app-container pjm-list-page pjm-invoice-page">
    <!-- ===== 筛选卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field" style="grid-column: span 2">
          <label>选择项目</label>
          <div class="control">
            <el-input :model-value="selectedProjectLabel" placeholder="点击选择项目" readonly style="cursor:pointer" @click="openProjectPicker">
              <template #append><el-button icon="Search" @click.stop="openProjectPicker" /></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>开票单号</label>
          <div class="control">
            <el-input v-model="queryParams.invoiceNo" placeholder="模糊搜索单号" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>发票类型</label>
          <div class="control">
            <el-select v-model="queryParams.invoiceType" placeholder="全部" clearable style="width:100%">
              <el-option v-for="dict in pjm_invoice_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>发票状态</label>
          <div class="control">
            <el-select v-model="queryParams.invoiceStatus" placeholder="全部" clearable style="width:100%">
              <el-option v-for="dict in pjm_invoice_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>发票抬头</label>
          <div class="control">
            <el-input v-model="queryParams.invoiceTitle" placeholder="模糊搜索发票抬头" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>开票日期</label>
          <div class="control">
            <el-date-picker v-model="invoiceDateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:100%" @change="onInvoiceDateRangeChange" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 开票记录 ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track"><span class="tree-label">开票管理</span></div>
        <span class="badge green" v-if="queryParams.projectId && currentProject">
          {{ currentProject.projectNo }} {{ currentProject.projectName }}
        </span>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pjm:invoice:add']">登记开票</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleEdit()" v-hasPermi="['pjm:invoice:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['pjm:invoice:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_invoice_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="invoiceList" border class="app-table"
                  @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="开票单号" prop="invoiceNo" key="invoiceNo" :width="colWidth('invoiceNo', 170)" resizable v-if="columns.invoiceNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.invoiceNo }}</span></template>
          </el-table-column>
          <el-table-column label="所属项目" prop="projectName" key="projectName" :min-width="colWidth('projectName', 200)" resizable show-overflow-tooltip v-if="columns.projectName.visible">
            <template #default="scope">
              <span class="col-mono">{{ scope.row.projectNo }}</span> {{ scope.row.projectName }}
            </template>
          </el-table-column>
          <el-table-column label="发票类型" prop="invoiceType" key="invoiceType" :width="colWidth('invoiceType', 140)" resizable align="center" v-if="columns.invoiceType.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.invoiceType)">
                <span class="dot"></span>{{ typeLabel(scope.row.invoiceType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="invoiceStatus" key="invoiceStatus" :width="colWidth('invoiceStatus', 100)" resizable align="center" v-if="columns.invoiceStatus.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.invoiceStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.invoiceStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="开票金额(元)" prop="invoiceAmount" key="invoiceAmount" :width="colWidth('invoiceAmount', 140)" resizable align="right" class-name="col-num" v-if="columns.invoiceAmount.visible">
            <template #default="scope"><span class="col-amt">{{ money(scope.row.invoiceAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="税率(%)" prop="taxRate" key="taxRate" :width="colWidth('taxRate', 90)" resizable align="right" class-name="col-num" v-if="columns.taxRate.visible">
            <template #default="scope">{{ scope.row.taxRate != null ? scope.row.taxRate + '%' : '-' }}</template>
          </el-table-column>
          <el-table-column label="开票日期" prop="invoiceDate" key="invoiceDate" :width="colWidth('invoiceDate', 120)" resizable align="center" v-if="columns.invoiceDate.visible">
            <template #default="scope">{{ scope.row.invoiceDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="发票抬头" prop="invoiceTitle" key="invoiceTitle" :min-width="colWidth('invoiceTitle', 180)" resizable show-overflow-tooltip v-if="columns.invoiceTitle.visible">
            <template #default="scope">{{ scope.row.invoiceTitle || '-' }}</template>
          </el-table-column>
          <el-table-column label="关联回款单号" prop="paymentNo" key="paymentNo" :width="colWidth('paymentNo', 160)" resizable v-if="columns.paymentNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.paymentNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" key="remark" :min-width="colWidth('remark', 140)" resizable show-overflow-tooltip v-if="columns.remark.visible">
            <template #default="scope">{{ scope.row.remark || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['pjm:invoice:query']">详情</el-button>
                <el-button v-if="scope.row.invoiceStatus === '0'" link type="success" icon="CircleCheck" @click="handleIssue(scope.row)" v-hasPermi="['pjm:invoice:issue']">开票</el-button>
                <el-button v-if="scope.row.invoiceStatus === '1'" link type="danger" icon="CircleClose" @click="handleVoid(scope.row)" v-hasPermi="['pjm:invoice:void']">作废</el-button>
                <el-button v-if="scope.row.invoiceStatus === '0'" link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['pjm:invoice:edit']">修改</el-button>
                <el-button v-if="scope.row.invoiceStatus === '0'" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pjm:invoice:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改弹框 ===== -->
    <el-dialog v-model="open" width="calc(100vw - 172px)" top="3vh" append-to-body draggable class="rd-dialog pj-wide-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Tickets /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.invoiceNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ form.invoiceNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="invoiceRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><MagicStick /></el-icon></span>发票智能识别
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="发票识别">
                    <div class="recognize-area">
                      <el-upload ref="recognizeUploadRef" :auto-upload="true" :show-file-list="false" accept=".pdf" :before-upload="handleBeforeRecognize" :http-request="handleRecognizeUpload" :disabled="recognizing">
                        <el-button type="success" icon="MagicStick" :loading="recognizing">{{ recognizing ? '识别中...' : '上传PDF发票识别' }}</el-button>
                      </el-upload>
                      <span class="recognize-tip">仅支持PDF格式电子发票，上传后自动识别并回填开票金额、税率、税额、开票日期、发票抬头等</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="24" v-if="recognizedFields.length">
                  <el-tag v-for="f in recognizedFields" :key="f.label" type="success" class="recognized-tag">{{ f.label }}：{{ f.value }}</el-tag>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>开票信息
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="所属项目" prop="projectId">
                    <el-input :model-value="formProjectLabel" readonly placeholder="点击选择项目" style="cursor:pointer" @click="openFormProjectPicker" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="发票类型" prop="invoiceType">
                    <el-select v-model="form.invoiceType" placeholder="请选择" style="width:100%">
                      <el-option v-for="dict in pjm_invoice_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="开票金额" prop="invoiceAmount">
                    <el-input-number v-model="form.invoiceAmount" :min="0.01" :precision="2" controls-position="right" style="width:100%" @change="computeTaxAmount" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="税率(%)">
                    <el-input-number v-model="form.taxRate" :min="0" :max="100" :precision="2" controls-position="right" style="width:100%" @change="computeTaxAmount" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="税额">
                    <el-input-number v-model="form.taxAmount" :min="0" :precision="2" controls-position="right" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="开票日期" prop="invoiceDate">
                    <el-date-picker v-model="form.invoiceDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="发票抬头">
                    <el-input v-model="form.invoiceTitle" placeholder="发票抬头单位名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="纳税人识别号">
                    <el-input v-model="form.taxNo" placeholder="纳税人识别号" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="关联回款">
                    <el-select v-model="form.paymentId" placeholder="可选：关联回款记录" clearable filterable style="width:100%">
                      <el-option v-for="p in paymentOptions" :key="p.paymentId" :label="p.paymentNo + '（' + (p.periodNo ? '第' + p.periodNo + '期 ' : '') + money(p.amount) + '元）'" :value="p.paymentId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Document /></el-icon></span>其他信息
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="备注">
                    <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="开票说明等" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Paperclip /></el-icon></span>发票附件
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-form-item label="发票附件" prop="invoiceAttachment">
                <UploadFile
                  :model-value="form.invoiceAttachment"
                  @update:model-value="v => form.invoiceAttachment = v"
                  :limit="5" :fileSize="50"
                  :fileType="['pdf', 'jpg', 'jpeg', 'png', 'zip', 'rar', 'doc', 'docx']" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 开票详情弹窗（规范第十节：只读布局） -->
    <el-dialog v-model="detailOpen" width="calc(100vw - 172px)" top="3vh" append-to-body draggable class="rd-dialog pj-wide-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Tickets /></el-icon></div>
          <span class="rd-detail-header-title">开票详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.invoiceNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ detailData.invoiceNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>开票信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">所属项目</span><div class="rd-value">{{ detailData.projectNo || '-' }} {{ detailData.projectName || '' }}</div></div>
              <div class="rd-item"><span class="rd-label">开票单号</span><div class="rd-value">{{ detailData.invoiceNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发票类型</span><div class="rd-value"><dict-tag :options="pjm_invoice_type" :value="detailData.invoiceType" /></div></div>
              <div class="rd-item"><span class="rd-label">发票状态</span><div class="rd-value"><dict-tag :options="pjm_invoice_status" :value="detailData.invoiceStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">开票金额</span><div class="rd-value rd-amount">{{ detailData.invoiceAmount != null ? '¥' + money(detailData.invoiceAmount) : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">税率</span><div class="rd-value">{{ detailData.taxRate != null ? detailData.taxRate + '%' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">税额</span><div class="rd-value">{{ detailData.taxAmount != null ? '¥' + money(detailData.taxAmount) : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开票日期</span><div class="rd-value">{{ detailData.invoiceDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发票抬头</span><div class="rd-value">{{ detailData.invoiceTitle || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">纳税人识别号</span><div class="rd-value">{{ detailData.taxNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联回款单号</span><div class="rd-value"><span class="col-mono">{{ detailData.paymentNo || '-' }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><Document /></el-icon></span>其他信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ detailData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="attachmentList.length > 0">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><Paperclip /></el-icon></span>发票附件</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item" v-for="(url, i) in attachmentList" :key="i">
                <span class="rd-label">附件{{ i + 1 }}</span>
                <div class="rd-value"><el-link type="primary" underline="never" @click="previewAttachment(url)">点击预览</el-link></div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 附件预览（全站统一 FilePreview 组件） -->
    <file-preview ref="detailFilePreviewRef" />
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
    <ProjectPicker ref="formProjectPickerRef" @confirm="onFormProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmInvoice">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listInvoice, getInvoice, addInvoice, updateInvoice, delInvoice, issueInvoice, voidInvoice, recognizeInvoice } from '@/api/pjm/invoice'
import request from '@/utils/request'
import { listProject } from '@/api/pjm/project'
import { listPayment } from '@/api/pjm/payment'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import ProjectPicker from '@/components/ProjectPicker/index.vue'
import UploadFile from '@/components/FileUpload/index.vue'

const { proxy } = getCurrentInstance()
const { pjm_invoice_type, pjm_invoice_status } = proxy.useDict('pjm_invoice_type', 'pjm_invoice_status')

const { collapsedCards, toggleCard } = useDetailCard(['c0', 'c1', 'c2', 'c3'])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_invoice_index')

const loading = ref(false)
const invoiceList = ref([])
const total = ref(0)
const projectOptions = ref([])
const paymentOptions = ref([])
const open = ref(false)
const title = ref('')
const invoiceRef = ref()
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)

const columns = ref({
  invoiceNo: { label: '开票单号', visible: true },
  projectName: { label: '所属项目', visible: true },
  invoiceType: { label: '发票类型', visible: true },
  invoiceStatus: { label: '状态', visible: true },
  invoiceAmount: { label: '开票金额(元)', visible: true },
  taxRate: { label: '税率(%)', visible: true },
  invoiceDate: { label: '开票日期', visible: true },
  invoiceTitle: { label: '发票抬头', visible: true },
  paymentNo: { label: '关联回款单号', visible: true },
  remark: { label: '备注', visible: true }
})

const queryParams = reactive({ pageNum: 1, pageSize: 10, projectId: null, invoiceNo: null, invoiceType: null, invoiceStatus: null, invoiceTitle: null, params: { beginInvoiceDate: '', endInvoiceDate: '' } })
const invoiceDateRange = ref([])
const projectPickerRef = ref()
const formProjectPickerRef = ref()
const selectedProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(queryParams.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
const currentProject = computed(() => projectOptions.value.find(x => String(x.projectId) === String(queryParams.projectId)) || null)
function openProjectPicker() { projectPickerRef.value?.open(queryParams.projectId) }
function onProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  queryParams.projectId = p.projectId
  getList()
}
const form = ref({})
const formProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(form.value.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openFormProjectPicker() { formProjectPickerRef.value?.open(form.value.projectId) }
function onFormProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  form.value.projectId = p.projectId
}
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  invoiceAmount: [{ required: true, message: '请输入开票金额', trigger: 'blur' }],
  invoiceDate: [{ required: true, message: '请选择开票日期', trigger: 'change' }],
  invoiceAttachment: [{ required: true, message: '发票附件必须上传', trigger: 'change' }]
}

/** 发票智能识别（复用营销模块识别服务：上传PDF自动回填开票信息） */
const recognizing = ref(false)
const recognizedFields = ref([])
const recognizeUploadRef = ref()

function handleBeforeRecognize(file) {
  const fileName = file.name.split('.')
  const fileExt = fileName[fileName.length - 1].toLowerCase()
  if (fileExt !== 'pdf') { ElMessage.error('仅支持PDF格式发票文件!'); return false }
  if (file.size / 1024 / 1024 > 20) { ElMessage.error('上传文件大小不能超过 20 MB!'); return false }
  return true
}

function handleRecognizeUpload(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  recognizing.value = true
  recognizedFields.value = []
  recognizeInvoice(formData).then(res => {
    const data = res.data
    if (data) {
      if (data.invoiceType) form.value.invoiceType = data.invoiceType
      if (data.invoiceDate) form.value.invoiceDate = data.invoiceDate
      if (data.invoiceAmount != null) form.value.invoiceAmount = Number(data.invoiceAmount)
      if (data.taxAmount != null) form.value.taxAmount = Number(data.taxAmount)
      if (data.taxRate != null) form.value.taxRate = Number(data.taxRate)
      if (data.invoiceTitle) form.value.invoiceTitle = data.invoiceTitle
      if (data.taxNo) form.value.taxNo = data.taxNo
      const fieldLabels = { invoiceNo: '发票号码', invoiceType: '发票类型', invoiceDate: '开票日期', invoiceAmount: '金额', taxAmount: '税额', taxRate: '税率(%)', invoiceTitle: '发票抬头', taxNo: '纳税人识别号' }
      recognizedFields.value = Object.keys(fieldLabels).filter(k => data[k] != null && data[k] !== '').map(k => ({ label: fieldLabels[k], value: data[k] }))
      if (recognizedFields.value.length > 0) { ElMessage.success('识别成功，已回填' + recognizedFields.value.length + '项信息') } else { ElMessage.warning('未识别到发票信息，请检查PDF文件是否为电子发票') }
      uploadRecognizedAttachment(options.file)
    } else { ElMessage.warning('未识别到发票信息') }
  }).catch(e => { ElMessage.error('发票识别失败：' + (e?.message || e)) }).finally(() => { recognizing.value = false })
}

/** 识别成功后把该PDF同时作为发票附件上传（满足附件必传要求） */
function uploadRecognizedAttachment(file) {
  const formData = new FormData()
  formData.append('file', file)
  request({ url: '/common/upload', method: 'post', data: formData, timeout: 60000, headers: { 'repeatSubmit': false, 'Content-Type': false } }).then(res => {
    if (res.fileName) { form.value.invoiceAttachment = res.fileName }
  }).catch(e => { ElMessage.error('发票附件上传失败：' + (e?.message || e)) })
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.projectId) count++
  if (queryParams.invoiceNo) count++
  if (queryParams.invoiceType) count++
  if (queryParams.invoiceStatus) count++
  if (queryParams.invoiceTitle) count++
  if (invoiceDateRange.value && invoiceDateRange.value.length) count++
  return count
})

function money(v) {
  if (v == null) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function badgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'gray' }; return map[status] || 'gray' }
function statusLabel(status) { const item = pjm_invoice_status.value.find(d => d.value == status); return item ? item.label : '-' }
function typeBadgeClass(type) { const map = { '0': 'blue', '1': 'violet', '2': 'amber' }; return map[type] || 'gray' }
function typeLabel(type) { const item = pjm_invoice_type.value.find(d => d.value == type); return item ? item.label : '-' }

function computeTaxAmount() {
  const amount = Number(form.value.invoiceAmount || 0)
  const rate = Number(form.value.taxRate || 0)
  if (amount > 0 && rate > 0) {
    form.value.taxAmount = Math.round(amount * rate / 100 * 100) / 100
  }
}

function onInvoiceDateRangeChange(range) {
  queryParams.params.beginInvoiceDate = (range && range[0]) || ''
  queryParams.params.endInvoiceDate = (range && range[1]) || ''
}

function loadProjects() {
  listProject({ pageNum: 1, pageSize: 200 }).then(res => { projectOptions.value = res.rows || [] })
}

/** 加载项目下的回款记录（供弱关联下拉选择） */
function loadPaymentOptions() {
  if (!form.value.projectId) { paymentOptions.value = []; return }
  listPayment({ pageNum: 1, pageSize: 100, projectId: form.value.projectId }).then(res => {
    paymentOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listInvoice(queryParams).then(res => {
    invoiceList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.projectId = null
  queryParams.invoiceNo = null
  queryParams.invoiceType = null
  queryParams.invoiceStatus = null
  queryParams.invoiceTitle = null
  invoiceDateRange.value = []
  queryParams.params.beginInvoiceDate = ''
  queryParams.params.endInvoiceDate = ''
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.invoiceId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    invoiceId: null, projectId: queryParams.projectId, paymentId: null,
    invoiceType: null, invoiceAmount: null, taxRate: null, taxAmount: null,
    invoiceTitle: null, taxNo: null,
    invoiceDate: new Date().toISOString().slice(0, 10),
    invoiceAttachment: null, remark: null
  }
  paymentOptions.value = []
}

function handleAdd() {
  reset()
  title.value = '登记开票'
  open.value = true
}

function handleEdit(row) {
  const invoiceId = row?.invoiceId || ids.value[0]
  getInvoice(invoiceId).then(res => {
    form.value = res.data
    title.value = '修改开票记录'
    open.value = true
    loadPaymentOptions()
  })
}

function submitForm() {
  invoiceRef.value.validate(valid => {
    if (!valid) return
    const action = form.value.invoiceId ? updateInvoice(form.value) : addInvoice(form.value)
    action.then(() => {
      ElMessage.success(form.value.invoiceId ? '修改成功' : '登记成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const invoiceIds = row?.invoiceId ? [row.invoiceId] : ids.value
  ElMessageBox.confirm('确认删除该开票记录吗？', '提示', { type: 'warning' }).then(() => {
    delInvoice(invoiceIds.join(',')).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

/** 开票确认（待开票 -> 已开票） */
function handleIssue(row) {
  ElMessageBox.confirm('确认对开票单【' + row.invoiceNo + '】执行开票确认吗？', '提示', { type: 'warning' }).then(() => {
    issueInvoice(row.invoiceId).then(() => {
      ElMessage.success('开票确认成功')
      getList()
    })
  }).catch(() => {})
}

/** 开票作废（已开票 -> 已作废） */
function handleVoid(row) {
  ElMessageBox.confirm('确认作废开票单【' + row.invoiceNo + '】吗？', '提示', { type: 'warning' }).then(() => {
    voidInvoice(row.invoiceId).then(() => {
      ElMessage.success('发票已作废')
      getList()
    })
  }).catch(() => {})
}

/** 开票详情弹窗（规范第十节：只读布局） */
const detailOpen = ref(false)
const detailData = ref({})
const detailFilePreviewRef = ref(null)
const attachmentList = computed(() => String(detailData.value.invoiceAttachment || '').split(',').map(u => u.trim()).filter(Boolean))

function handleDetail(row) {
  getInvoice(row.invoiceId).then(res => {
    detailData.value = res.data || {}
    detailOpen.value = true
  })
}

function previewAttachment(url) {
  detailFilePreviewRef.value?.open(url, url.split('/').pop() || '发票附件')
}

onMounted(() => {
  loadProjects()
  getList()
})
</script>

<style scoped>
.pjm-invoice-page .tree-empty { padding: 24px 0; }
</style>
